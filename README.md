# 회원 적립금 API 개발 예시

## 이 프로젝트는?

이 프로젝트는 회원 적립금 API를 개발하기 위한 예시입니다.

## 무슨 기능을 가지고 있나요?

이 프로젝트는 다음의 기능을 가지고 있습니다.

- 회원별 적립금 합계 조회
- 회원별 적립금 적립/사용 내역 조회 (페이징)
- 회원별 적립금 적립
- 회원별 적립금 사용 (먼저 적립된 순서로 사용)

## 무슨 기술을 사용하고 있나요?

다음의 기술을 사용해서 구성했습니다.

- Java 17
- Spring Boot
- Spring Data JPA
- H2 Database
- QueryDSL
- JUnit 5

## 어떻게 실행하나요?

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

---

## API 명세

### 회원별 적립금 합계 조회

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

- memberId : 회원 ID (Integer)
- totalPoint : 적립금 합계 (Integer)

### 회원별 적립금 적립/사용 내역 조회 (페이징)

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
      "createdAt": "2023-08-21T17:27:05.465056",
      "expireAt": "2024-08-21T17:27:05.465056"
    },
    {
      "id": 3,
      "memberId": 1,
      "amount": 1000,
      "createdAt": "2023-08-21T17:27:05.053648",
      "expireAt": "2024-08-21T17:27:05.053648"
    },
    {
      "id": 2,
      "memberId": 1,
      "amount": 1000,
      "createdAt": "2023-08-21T17:26:36.744625",
      "expireAt": "2024-08-21T17:26:36.744625"
    },
    {
      "id": 1,
      "memberId": 1,
      "amount": 1000,
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
  - id : 적립금 내역 ID (Integer)
  - memberId : 회원 ID (Integer)
  - amount : 적립금 (Integer)
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

### 회원별 적립금 적립

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
  "createdAt": "2023-08-21T17:07:35.04654",
  "expireAt": "2024-08-21T17:07:35.04654"
}
```

- id : 적립금 내역 ID (Integer)
- memberId : 회원 ID (Integer)
- amount : 적립금 (Integer)
- createdAt : 적립일 (LocalDateTime)
- expireAt : 적립금 만료일 (LocalDateTime)

### 회원별 적립금 사용 (먼저 적립된 순서로 사용)

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
  "createdAt": "2023-08-21T17:37:39.755156",
  "expireAt": null
}   
```

- id : 적립금 내역 ID (Integer)
- memberId : 회원 ID (Integer)
- amount : 금액 (Integer)
  - 사용된 적립금액은 음수로 표현합니다.
- createdAt : 적립일 (LocalDateTime)
- expireAt : 적립금 만료일 (LocalDateTime)
  - 적립금 사용 내역은 만료일이 없으므로 null로 표현합니다.


