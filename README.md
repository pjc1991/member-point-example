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
  "__작성_예정__" : "작성 예정"
}
```

### 회원별 적립금 적립

#### 요청
```bash
curl -X POST http://localhost:8080/member/1/point/earn \
  -H 'Content-Type: application/json' \
  -d '{
    "amount": 1000
  }'
```

#### 응답
```json
{
  "__작성_예정__" : "작성 예정"
}
```

### 회원별 적립금 사용 (먼저 적립된 순서로 사용)

#### 요청
```bash
curl -X POST http://localhost:8080/member/1/point/use \
    -H 'Content-Type: application/json' \
    -d '{
        "amount": 1000
    }'
```

#### 응답
```json
{
  "__작성_예정__" : "작성 예정"
}
```


