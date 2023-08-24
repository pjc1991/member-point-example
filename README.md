# 회원 적립금 API 개발 예시

## 요약

이 프로젝트는 회원 적립금 API를 개발하기 위한 예시입니다.

## 프로젝트 기능

이 프로젝트는 다음의 기능을 가지고 있습니다.

- 회원별 적립금 합계 조회
- 회원별 적립금 적립/사용 내역 조회 (페이징)
- 회원별 적립금 적립
- 회원별 적립금 사용 (먼저 적립된 순서로 사용)
- 회원 적립금 만료 
- 회원 적립금 사용 취소 (Rollback 목적)

## 사용하는 기술 

다음의 기술을 사용해서 구성했습니다.

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Data Redis
- JUnit 5
- QueryDSL
- Blazed-Persistence
- H2 Database
- Redis (Embedded)
- Redis (Docker)
- PostgreSQL (Docker)
- Docker
- Docker-Compose

## 프로젝트 실행 방법

### Gradle 을 이용한 방법

1. 이 리포지터리를 클론하고 루트 디렉토리로 이동합니다.

```shell
git clone https://github.com/pjc1991/member-point-example.git &&
cd member-point-example
```

2. 다음의 명령어로 테스트를 실행합니다.

```shell
./gradlew test
```

3. 다음의 명령어로 애플리케이션을 실행합니다.

```shell
./gradlew bootRun
```

### Docker 를 이용한 방법

1. 이 리포지터리를 클론하고 루트 디렉토리로 이동합니다.

```shell
git clone https://github.com/pjc1991/member-point-example.git &&
cd member-point-example
```

2. 다음의 명령어로 Docker 이미지를 빌드합니다.

```shell
docker-compose build
```

3. 다음의 명령어로 Docker 컨테이너를 실행합니다.

```shell
docker-compose up
# CTRL + C 로 종료합니다.
```

4. 다음의 명령어로 컨테이너를 종료합니다.

```shell
docker-compose down
```

5. 어플리케이션에 변화가 있었을 경우 다시 빌드하고 실행합니다.

```shell
docker-compose down &&
docker-compose build &&
docker-compose up
```

---

## 도메인 설계

### 회원 적립금 이벤트 도메인 
```java
// MemberPointEvent.java
/**
 * 회원 적립금 이벤트 내역 도메인
 */
@Entity
@Table(name = "MEMBER_POINT_EVENT")
public class MemberPointEvent {

  /**
   * 회원 적립금 이벤트 ID
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  /**
   * 회원
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "MEMBER_ID", nullable = false)
  private Member member;

  /**
   * 회원 적립금 적립/사용 금액
   * 적립: 양수, 사용: 음수
   */
  @Column(name = "AMOUNT", nullable = false)
  private int amount;

  /**
   * 회원 적립금 적립/사용 내역
   */
  @OneToMany(mappedBy = "memberPointEvent", cascade = CascadeType.ALL)
  private Set<MemberPointDetail> memberPointDetails = new HashSet<>();

  /**
   * 회원 적립금 만료 시점
   */
  @Column(name = "EXPIRE_AT")
  private LocalDateTime expireAt;

  /**
   * 회원 적립금 생성 시점
   */
  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  /**
   * 회원 적립금 이벤트 종류
   */
  @Column(name = "TYPE", nullable = false)
  @Enumerated(EnumType.STRING)
  private MemberPointEventType type;
  
  /**
   * 회원 적립금 이벤트 종류
   */
  public enum MemberPointEventType {
    EARN, // 적립
    USE, // 사용
    EXPIRE, // 만료
  }
}
```
### 회원 적립금 상세 도메인 
```java
// MemberPointDetail.java
/**
 * 회원 적립금 상세 내역 도메인
 */
@Entity
@Table(name = "MEMBER_POINT_DETAIL")
public class MemberPointDetail {

  /**
   * 회원 적립금 상세 내역 ID
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  /**
   * 회원 적립금 이벤트
   * 이 상세 내역을 생성한 이벤트
   */
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "MEMBER_POINT_EVENT_ID", nullable = false)
  private MemberPointEvent memberPointEvent;

  /**
   * 회원 적립금 상세 내역 그룹 ID
   * 합계를 계산할 때 이 ID를 기준으로 합계를 계산합니다. (GROUP BY)
   * 이 상세 내역이 적립금 사용, 만료일 경우 사용 대상이 되는 상세 내역의 ID
   */
  @Column(name = "MEMBER_POINT_DETAIL_GROUP_ID")
  private Long memberPointDetailGroupId;

  /**
   * 회원 적립금 상세 내역 환불 ID
   * 이 상세 내역이 적립금 환불일 경우, 대상이 되는 상세 내역의 ID
   */
  @Column(name = "MEMBER_POINT_DETAIL_REFUND_ID")
  private Long memberPointDetailRefundId;

  /**
   * 포인트 적립/사용량
   * 적립: 양수, 사용: 음수
   */
  @Column(name = "AMOUNT", nullable = false)
  private int amount;


  /**
   * 발생 시점
   */
  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  /**
   * 만료 시점
   */
  @Column(name = "EXPIRE_AT", nullable = false)
  private LocalDateTime expireAt;
  
  /**
   * 회원 적립금 상세 내역의 타입 열거형입니다.
   */
  public enum MemberPointDetailType {
    USE,
    EARN,
    EXPIRE,
    CANCEL
  }
}
```

--- 

## 기능 명세

### API - 회원별 적립금 합계 조회

#### 요청
```bash
curl -X GET http://localhost:8080/member/1/point/total
```

- Method: GET
- URL: /member/{memberId}/point/total
- Path Variable
  - memberId: 회원 ID

#### 응답
```json
{
  "memberId": 1,
  "totalPoint": 1000 
}
```

- memberId : 회원 ID (long)
- totalPoint : 적립금 합계 (Integer)

---

#### 기능 구조

1. MemberPointDetail 테이블을 조회하여 AMOUNT 칼럼을 합산합니다.
2. 이 때 조회 조건 (WHERE) 은 MEMBER_ID 와 EXPIRE_AT 을 사용합니다.
3. 자주 사용되는 쿼리이고 많은 행을 조회해야할 수 있으므로, REDIS 를 이용하여 캐싱합니다. 
4. 값이 변경될 가능성이 있는 적립, 사용, 사용 취소가 일어날 경우 캐싱을 삭제합니다.

---

>dev.pjc1991.commerce.member.point.service.MemberPointService#getMemberPointTotal

구체적인 코드는 해당 경로에서 확인 가능합니다.

### API - 회원별 적립금 적립/사용 내역 조회 (페이징)

#### 요청
```bash
curl -X GET http://localhost:8080/member/1/point
```

- Method: GET
- URL: /member/{memberId}/point
- Path Variable
  - memberId: 회원 ID
- Request Body
  - page: 페이지 번호 (Integer, default: 0)
  - size: 페이지 크기 (Integer, default: 10)

#### 응답
```json
{
  "content": [
    {
      "id": 4,
      "memberId": 1,
      "amount": 1000,
      "type": "EARN",
      "createdAt": "2023-08-21T17:27:05.465056",
      "expireAt": "2024-08-21T17:27:05.465056"
    },
    {
      "id": 3,
      "memberId": 1,
      "amount": 1000,
      "type": "EARN",
      "createdAt": "2023-08-21T17:27:05.053648",
      "expireAt": "2024-08-21T17:27:05.053648"
    },
    {
      "id": 2,
      "memberId": 1,
      "amount": 1000,
      "type": "EARN",
      "createdAt": "2023-08-21T17:26:36.744625",
      "expireAt": "2024-08-21T17:26:36.744625"
    },
    {
      "id": 1,
      "memberId": 1,
      "amount": 1000,
      "type": "EARN",
      "createdAt": "2023-08-21T17:26:36.328703",
      "expireAt": "2024-08-21T17:26:36.328703"
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 4,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 4,
  "empty": false
} 
```
- contents : 적립금 내역 (List)
  - id : 적립금 내역 ID (long)
  - memberId : 회원 ID (long)
  - amount : 적립금 (Integer)
  - type : 적립금 내역 타입 (Enum)
    - EARN : 적립
    - USE : 사용
    - EXPIRE : 만료
    - CANCEL : 취소
    - REFUND : 환불
  - createdAt : 적립일 (LocalDateTime)
  - expireAt : 적립금 만료일 (LocalDateTime)
- pageable : 페이징 정보 (Pageable) (Spring Data JPA 구현체)
- last : 마지막 페이지 여부 (Boolean)
- totalElements : 전체 요소 수 (Integer)
- totalPages : 전체 페이지 수 (Integer)
- first : 첫 페이지 여부 (Boolean)
- size : 페이지 크기 (Integer)
- number : 현재 페이지 번호 (Integer)
- sort : 정렬 정보 (Sort) (Spring Data JPA 구현체)
  - empty : 정렬 정보가 비어있는지 여부 (Boolean)
  - sorted : 정렬 여부 (Boolean)
  - unsorted : 정렬되지 않았는지 여부 (Boolean)
- numberOfElements : 현재 페이지의 요소 수 (Integer)
- empty : 현재 페이지가 비어있는지 여부 (Boolean)
---

#### 기능 구조

1. MemberPointEvent 테이블을 조회해 검색 조건에 맞는 행들을 조회합니다.
2. 검색 조건은 MemberId 입니다.
3. 롤백 처리가 된 적립금 사용 내역은 제외하기 위해서, 서브 쿼리로 추가 조건을 줍니다.
4. 페이징 값을 이용해 페이징 처리를 하고 출력합니다. 

---
>dev.pjc1991.commerce.member.point.service.MemberPointService#getMemberPointEvents

구체적인 코드는 해당 경로에서 확인 가능합니다.
### API - 회원별 적립금 적립

#### 요청
```bash
curl -X POST http://localhost:8080/member/1/point/earn \
  -H 'Content-Type: application/json' \
  -d '{
    "amount": 1000
  }'
```

- Method: POST
- URL: /member/{memberId}/point/earn
- Path Variable
  - memberId: 회원 ID
- Request Body 
  - amount: 적립금 (Integer)

#### 응답
```json
{
  "id": 1,
  "memberId": 1,
  "amount": 1000,
  "type": "EARN",
  "createdAt": "2023-08-21T17:07:35.04654",
  "expireAt": "2024-08-21T17:07:35.04654"
}
```

- id : 적립금 내역 ID (long)
- memberId : 회원 ID (long)
- amount : 적립금 (Integer)
- type : 적립금 내역 타입 (Enum)
  - EARN : 적립
- createdAt : 적립일 (LocalDateTime)
- expireAt : 적립금 만료일 (LocalDateTime)
- 
---

#### 기능 구조

1. 요청값에 따라서 MemberPointEvent 에 행을 삽입합니다.
2. MemberPointEvent 과 1:N 관계를 가지는 MemberPointDetail 테이블에도 행을 삽입합니다.
3. MemberPointDetail 에 행을 삽입한 이후, 해당 행의 MemberPointDetailGroupId 를 자기 자신의 값으로 업데이트 해줍니다.  
4. 결과 값으로 MemberPointEvent 를 반환합니다.

---

>dev.pjc1991.commerce.member.point.service.MemberPointService#earnMemberPoint

구체적인 코드는 해당 경로에서 확인 가능합니다.

### API - 회원별 적립금 사용 (먼저 적립된 순서로 사용)

#### 요청
```bash
curl -X POST http://localhost:8080/member/1/point/use \
    -H 'Content-Type: application/json' \
    -d '{
        "amount": 300
    }'
```

- Method: POST
- URL: /member/{memberId}/point/use
- Path Variable
  - memberId: 회원 ID
- Request Body
  - amount: 사용금액 (Integer)

#### 응답
```json
{
  "id": 2,
  "memberId": 1,
  "amount": -300,
  "type": "USE",
  "createdAt": "2023-08-21T17:37:39.755156",
  "expireAt": null
}   
```

- id : 적립금 내역 ID (long)
- memberId : 회원 ID (long)
- amount : 금액 (Integer)
  - 사용된 적립금액은 음수로 표현합니다.
- type : 적립금 내역 타입 (Enum)
  - USE : 사용
- createdAt : 적립일 (LocalDateTime)
- expireAt : 적립금 만료일 (LocalDateTime)
  - 적립금 사용 내역은 만료일이 없으므로 null로 표현합니다.
  
---

#### 기능 구조

1. MemberPointDetail 을 조회하여, 사용 가능한 금액을 조회합니다.
2. 조회된 금액이 요청값보다 작다면, 적립금 사용이 불가능하므로 예외를 발생시킵니다.
3. MemberPointEvent 를 생성합니다. 
4. MemberPointEvent 와 1:N 관계를 가지는 MemberPointDetail 테이블에도 행을 삽입합니다. 
5. 이 때, MemberPointDetail 테이블을 MemberPointDetailGroupId 로 그룹화하여 Amount 를 합산합니다. (MemberPointDetailRemain)
6. 합산값이 0보다 크면서, CreateAt 이 가장 오래된 값이 선입선출에 의해 사용되어야 할 MemberPointDetail 입니다. 
7. 새로 삽입하는 행의 MemberPointDetailGroupId 를 사용되어야 할 MemberPointDetail 의 MemberPointDetailGroupId 로 설정합니다. 
8. 선입선출의 사용대상이 되는 행의 Amount와 요청값의 Amount 중 더 작은 값을 Amount 로 설정하고, 요청값의 Amount 를 감산합니다.
9. 후에 MemberPointDetailGroupId 로 다시 그룹 쿼리를 실행했을 때, 결과값이 새로 삽입한 행의 Amount 만큼 감산됩니다. 
10. 사용할 요청값이 0이 될 때까지 4~9번을 반복합니다.
11. 결과값으로 MemberPointEvent 를 반환합니다.

---

>dev.pjc1991.commerce.member.point.service.MemberPointService#useMemberPoint

구체적인 코드는 해당 경로에서 확인 가능합니다.

### API - 회원 적립금 사용 취소

#### 요청
```bash
curl -X DELETE http://localhost:8080/member/point/1
```
- Method: DELETE
- URL: /member/point/{memberPointEventId}
- Path Variable
  - memberPointEventId: 적립금 내역 ID

#### 응답
```json
{
  "id": 1,
  "memberId": 1,
  "amount": -1000,
  "type": "USE",
  "createdAt": "2023-08-21T17:07:35.04654",
  "expireAt": "2024-08-21T17:07:35.04654"
}
```
(기존에 사용된 적립금 내역을 반환합니다.)
- id : 적립금 내역 ID (long)
- memberId : 회원 ID (long)
- amount : 적립금 (Integer)
  - 사용된 적립금액은 음수로 표현합니다.
- type : 적립금 내역 타입 (Enum)
  - USE : 사용
- createdAt : 적립일 (LocalDateTime)
- expireAt : 적립금 만료일 (LocalDateTime)
  - 적립금 사용 내역은 만료일이 없으므로 null로 표현합니다.

---

#### 기능 구조

1. MemberPointEvent 를 조회합니다.
2. 해당 이벤트의 타입이 USE 가 아니라면 예외를 발생시킵니다.
3. 해당 이벤트의 MemberPointDetail 을 조회해 Amount 를 합산한 값을 확인합니다. 0 이라면 이미 사용 취소된 상태이므로 예외를 발생시킵니다.
4. 해당 이벤트의 MemberPointDetail 의 Amount 값에 -1 을 곱한 값을 지닌 MemberPointDetail(타입 CANCEL)을 해당 이벤트에 추가합니다. 
5. 적립금 이벤트 내역을 조회할 때는 CANCEL 이 포함된 이벤트는 조회되지 않고, 합산금액을 계산할 때는 사용값이 상쇄되어 합산되지 않습니다.

---

>dev.pjc1991.commerce.member.point.service.MemberPointService#rollbackMemberPointUse

구체적인 코드는 해당 경로에서 확인 가능합니다.

---
### 스케쥴 - 회원 적립금 만료

매일 00시 00분 00초에 회원 적립금 만료 스케쥴이 실행됩니다.

```java
// MemberPointScheduledTask.java

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberPointScheduledTasks {

  private final MemberPointService memberPointService;

  /**
   * 회원 적립금 만료 처리
   * 매일 00:00:00에 실행됩니다.
   */
  @Scheduled(cron = "0 0 0 * * *")
  public void expireMemberPoint() {
    // StopWatch를 사용하여 실행 시간을 측정합니다.
    StopWatch stopWatch = new StopWatch();

    log.info("expireMemberPoint start");
    stopWatch.start();
    memberPointService.expireMemberPoint();
    stopWatch.stop();
    log.info("expireMemberPoint end");

    log.info("expireMemberPoint 총 : {}ms", stopWatch.getTotalTimeMillis());
  }
}
```

---

#### 기능 구조

1. 크론을 이용해 매일 자정 ScheduledTasks 를 실행합니다.
2. MemberPointDetail 테이블을 조회해, 만료시간이 현 시각보다 이전이면서, 그룹별 금액 합계가 0보다 큰 그룹들을 조회합니다. (MemberPointDetailRemain)
3. 해당 그룹에 대해, 회원 적립금 만료 이벤트와 그에 따른 회원 적립금 상세 내역을 생성합니다. 
4. 만료 내역을 생성할 때 시스템에 로그를 남깁니다.

---

>dev.pjc1991.commerce.member.point.component.MemberPointScheduledTask.java
>dev.pjc1991.commerce.member.point.service.MemberPointService#expireMemberPoint

구체적인 코드는 해당 경로에서 확인 가능합니다.
