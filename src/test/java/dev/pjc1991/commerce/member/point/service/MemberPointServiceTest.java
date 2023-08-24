package dev.pjc1991.commerce.member.point.service;

import dev.pjc1991.commerce.member.domain.Member;
import dev.pjc1991.commerce.member.dto.MemberSignupRequest;
import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import dev.pjc1991.commerce.member.point.dto.MemberPointCreateRequest;
import dev.pjc1991.commerce.member.point.dto.MemberPointEventResponse;
import dev.pjc1991.commerce.member.point.dto.MemberPointEventSearch;
import dev.pjc1991.commerce.member.point.dto.MemberPointUseRequest;
import dev.pjc1991.commerce.member.point.exception.MemberPointAlreadyRollbackedException;
import dev.pjc1991.commerce.member.point.repository.MemberPointDetailRepository;
import dev.pjc1991.commerce.member.point.repository.MemberPointEventRepository;
import dev.pjc1991.commerce.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.Rollback;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberPointServiceTest {

    private static final Logger log = LoggerFactory.getLogger(MemberPointServiceTest.class);
    private static final long TEST_MEMBER_ID = 1;
    private static final int TEST_POINT_AMOUNT = 10000;

    @Autowired
    MemberPointEventRepository memberPointEventRepository;
    @Autowired
    MemberPointDetailRepository memberPointDetailRepository;
    @Autowired
    MemberPointService memberPointService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager entityManager;


    /**
     * 테스트가 시작되기 전에 실행됩니다.
     * 더미 데이터를 생성합니다.
     */
    @BeforeEach
    void setup() {
        // 테스트가 시작되기 전에 더미 데이터를 생성합니다.
        int testCount = 100;
        for (int i = 0; i < testCount; i++) {
            Member member = createDummyMember();
            MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(member.getId(), Math.toIntExact(Math.round(Math.random() * TEST_POINT_AMOUNT)) + 1);
            MemberPointEvent event = memberPointService.earnMemberPoint(memberPointCreateRequest);
        }
    }


    /**
     * 테스트를 위해 더미 회원 데이터를 생성합니다.
     * @return 더미 회원
     */
    private Member createDummyMember() {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest();
        memberSignupRequest.setName(UUID.randomUUID().toString().substring(0, 10));
        Member member = memberRepository.save(Member.signup(memberSignupRequest));
        return member;
    }

    /**
     * 테스트가 끝난 후에는 실행됩니다.
     * 캐시를 제거하고, 적립금이 선입선출로 사용되었는지 확인합니다.
     */
    @AfterEach
    void tearDown() {
        // 테스트가 끝난 후에는 캐시를 비웁니다.
        memberPointService.clearMemberPointTotalCache(TEST_MEMBER_ID);
        memberPointService.checkMemberPoint(TEST_MEMBER_ID);
    }

    /**
     * 적립금 합계를 조회합니다.
     */
    @Test
    void getMemberPointTotal() {
        // given

        // 시간 측정을 위해 스톱워치를 시작합니다.
        StopWatch stopWatch = new StopWatch();

        // 현재 금액을 조회합니다.
        int currentPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // 랜덤한 금액을 적립합니다.
        int testPointAmount = Math.toIntExact(Math.round(Math.random() * TEST_POINT_AMOUNT)) + 1;

        // 랜덤한 금액을 사용합니다.
        int testPointUseAmount = Math.toIntExact(Math.round(Math.random() * testPointAmount) + 1);

        // 예상 금액을 계산합니다.
        int expectedPoint = currentPoint + testPointAmount - testPointUseAmount;

        // 적립금 생성 요청 오브젝트를 생성합니다.
        MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(TEST_MEMBER_ID, testPointAmount);

        // 적립금을 추가합니다.
        stopWatch.start("earnMemberPoint");
        memberPointService.earnMemberPoint(memberPointCreateRequest);
        stopWatch.stop();


        // 적립금을 사용합니다.
        stopWatch.start("useMemberPoint");
        memberPointService.useMemberPoint(getTestMemberPointUseRequest(TEST_MEMBER_ID, testPointUseAmount));
        stopWatch.stop();

        // when

        // 적립금 합계를 조회합니다.
        stopWatch.start("getMemberPointTotal");
        int result = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);
        stopWatch.stop();

        // then

        // stopWatch.prettyPrint()를 통해 측정된 시간을 출력합니다.
        System.out.println(stopWatch.prettyPrint());

        // 적립금 합계가 예상한 값과 같은지 확인합니다.
        log.info("예상되는 적립금 : {}", expectedPoint);
        log.info("실제 적립금 : {}", result);

        assertEquals(expectedPoint, result);
    }

    /**
     * 적립금 적립/사용 내역을 조회합니다.
     */
    @Test
    void getMemberPointEvents() {
        // given

        // 랜덤한 갯수의 적립금을 적립합니다.
        int maxTestCount = 100;
        int testCount = Math.toIntExact(Math.round(Math.random() * maxTestCount));
        log.info("테스트 횟수 : {}", testCount);

        // 적립금 적립/사용 내역을 조회합니다.
        // 검색 파라미터를 담은 오브젝트를 생성합니다.
        int page = 0;
        int size = 10;
        MemberPointEventSearch search = getMemberPointEventSearch(TEST_MEMBER_ID, page, size);
        Page<MemberPointEvent> before = memberPointService.getMemberPointEvents(search);

        // 적립금을 적립합니다.
        for (int i = 0; i < testCount; i++) {
            MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(TEST_MEMBER_ID, Math.toIntExact(Math.round(Math.random() * TEST_POINT_AMOUNT)) + 1);
            memberPointService.earnMemberPoint(memberPointCreateRequest);
        }

        // when

        // 적립금 적립/사용 내역을 조회합니다.
        Page<MemberPointEvent> after = memberPointService.getMemberPointEvents(search);

        // then

        // 조회된 적립금 적립/사용 내역의 갯수가 예상한 값과 같은지 확인합니다.
        long expected = before.getTotalElements() + testCount;
        log.info("예상된 갯수: {}", expected);
        log.info("실제 갯수: {}", after.getTotalElements());
        assertEquals(expected, after.getTotalElements());

        // 정상적으로 페이징이 되었는지 확인합니다.
        long modifier = before.getTotalElements() % search.getSize();
        long expectedSize = modifier == 0 ? search.getSize() : modifier;
        log.info("예상된 페이지 크기 : {}", expectedSize);
        log.info("실제 페이지 크기 : {}", after.getSize());
        assertEquals(expectedSize, after.getSize());
    }

    /**
     * 적립금 적립/사용 내역을 조회합니다.
     */
    @Test
    void earnMemberPoint() {
        // given

        // 현재 금액을 조회합니다.
        int currentPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // 랜덤한 금액을 적립합니다.
        int testPointAmount = Math.toIntExact(Math.round(Math.random() * 100000) + 1);

        // 예상 금액을 계산합니다.
        int expectedPoint = currentPoint + testPointAmount;

        // when

        // 적립금 생성 요청 오브젝트를 생성합니다.
        MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(TEST_MEMBER_ID, testPointAmount);

        // 적립금을 추가합니다.
        MemberPointEvent result = memberPointService.earnMemberPoint(memberPointCreateRequest);

        // then

        // 적립금 적립/사용 내역이 정상적으로 생성되었는지 확인합니다.
        assertNotNull(result);

        // 생성된 객체의 값이 정상적인지 확인합니다.
        assertEquals(TEST_MEMBER_ID, result.getMember().getId());
        assertEquals(testPointAmount, result.getAmount());

        // 적립금 합계가 예상한 값과 같은지 확인합니다.
        int resultPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);
        log.info("예상되는 적립금 합계 : {}", expectedPoint);
        log.info("실제 적립금 합계 : {}", resultPoint);
        assertEquals(expectedPoint, resultPoint);

    }

    /**
     * 적립금 적립/사용 내역을 조회합니다.
     */
    @Test
    void useMemberPoint() {
        // given

        // 현재 금액을 조회합니다.
        int currentPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);
        log.info("현재 적립금: {}", currentPoint);

        // 랜덤한 금액을 적립합니다.
        int testPointEarnAmount = Math.toIntExact(Math.round(Math.random() * TEST_POINT_AMOUNT)) + 1;
        log.info("적립 예정 금액: {}", testPointEarnAmount);

        // 랜덤한 금액을 사용합니다. 사용할 금액은 적립금 적립 금액보다 작거나 같습니다.
        int testPointUseAmount = Math.toIntExact(Math.round(Math.random() * testPointEarnAmount)) + 1;
        log.info("사용 예정 금액: {}", testPointUseAmount);

        // 예상 금액을 계산합니다.
        int expectedPoint = currentPoint + testPointEarnAmount - testPointUseAmount;
        log.info("최종 결과 예상 금액 : {}", expectedPoint);

        // 적립금 생성 요청 오브젝트를 생성합니다.
        MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(TEST_MEMBER_ID, testPointEarnAmount);

        // 적립금을 추가합니다.
        MemberPointEvent event = memberPointService.earnMemberPoint(memberPointCreateRequest);

        // when

        // 적립금 사용 요청 오브젝트를 생성합니다.
        MemberPointUseRequest memberPointUseRequest = getTestMemberPointUseRequest(TEST_MEMBER_ID, testPointUseAmount);

        // 적립금을 사용합니다.
        MemberPointEvent result = memberPointService.useMemberPoint(memberPointUseRequest);

        // then

        // 적립금 적립/사용 내역이 정상적으로 생성되었는지 확인합니다.
        log.info("적립금 사용 이벤트 : {}", result);
        assertNotNull(result);

        // 생성된 객체의 값이 정상적인지 확인합니다.
        log.info("예상 회원 아이디 : {}", TEST_MEMBER_ID);
        log.info("실제 회원 아이디 : {}", result.getMember().getId());
        assertEquals(TEST_MEMBER_ID, result.getMember().getId());

        log.info("예상되는 금액 : {}", -testPointUseAmount);
        log.info("실제 금액 : {}", result.getAmount());
        assertEquals(-testPointUseAmount, result.getAmount());

        // 적립금 합계가 예상한 값과 같은지 확인합니다.
        log.info("예상 적립금 합계 : {}", expectedPoint);
        log.info("실제 적립금 합계 : {}", memberPointService.getMemberPointTotal(TEST_MEMBER_ID));
        assertEquals(expectedPoint, memberPointService.getMemberPointTotal(TEST_MEMBER_ID));
    }

    /**
     * 다수의 적립금 적립 및 사용을 테스트합니다.
     * 성능 확인을 위한 스트레스 테스트입니다.
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100, 1000})
    void earnAndUseMemberPointMultipleTimes(int numberOfTest) {
        // given

        // 랜덤한 금액을 적립합니다.
        int maxTestPointAmount = TEST_POINT_AMOUNT;
        int currentPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // when

        // 다수의 적립금 적립 및 사용의 성능을 테스트하기 위해 스톱워치를 시작합니다.
        StopWatch stopWatch = new StopWatch();

        log.info("테스트 횟수 : {}", numberOfTest);
        log.info("최대 생성 적립금 금액: {}", maxTestPointAmount);

        log.info("Member Point Earn Start");
        stopWatch.start("Member Point Earn");

        // 적립금을 추가합니다.
        for (int i = 0; i < numberOfTest; i++) {
            int amountPointEarn = Math.toIntExact(Math.round(Math.random() * maxTestPointAmount)) + 1;
            MemberPointEvent event = memberPointService.earnMemberPoint(getTestMemberPointCreateRequest(TEST_MEMBER_ID, amountPointEarn));
            currentPoint += event.getAmount();
            log.info("현재 금액 : {}, 추가 적립액 : {}", currentPoint, amountPointEarn);
        }
        stopWatch.stop();

        log.info("Member Point Use Start");
        stopWatch.start("Member Point Use");
        int useCount = 0;

        // 적립금 사용의 경우에는 적립보다 자주 발생하지 않습니다.
        // 빌드 시간을 줄이기 위해 최대 100회로 제한합니다.
        int useTestLimit = 100;
        int maxUseCount = Math.min(numberOfTest, useTestLimit);

        for (int i = 0; i < maxUseCount; i++) {

            // 금액을 모두 사용하면 루프를 종료합니다.
            if (currentPoint == 0) {
                break;
            }

            // 랜덤한 금액을 사용합니다. 사용 금액은 0 이상이어야 합니다.
            int amountPointUse = Math.toIntExact(Math.round(Math.random() * maxTestPointAmount)) + 1;

            // 사용 금액이 현재 금액보다 크면 현재 금액만큼 사용합니다.
            if (amountPointUse > currentPoint) {
                amountPointUse = currentPoint;
            }

            // 적립금을 사용합니다.
            MemberPointEvent event = memberPointService.useMemberPoint(getTestMemberPointUseRequest(TEST_MEMBER_ID, amountPointUse));

            // 현재 금액을 갱신합니다.
            currentPoint -= amountPointUse;

            // 사용 횟수를 증가시킵니다.
            useCount++;
            log.info("현재 금액: {}, 사용 금액: {}, 사용 횟수 : {}", currentPoint, amountPointUse, useCount);
        }
        stopWatch.stop();
        // then

        // stopWatch.prettyPrint()를 통해 측정된 시간을 출력합니다.
        System.out.println(stopWatch.prettyPrint());

        // 각 작업당 1회에 사용된 시간을 확인합니다.
        long totalEarn = stopWatch.getTaskInfo()[0].getTimeMillis();
        long earnTime = totalEarn / numberOfTest;

        long totalUse = stopWatch.getTaskInfo()[1].getTimeMillis();
        long useTime = totalUse / useCount;
        log.info("회원 적립금 적립 횟수 : {}회", numberOfTest);
        log.info("회원 적립금 1회 적립에 소요된 시간 : {}ms", earnTime);
        log.info("회원 적립금 사용 횟수 : {}회", useCount);
        log.info("회원 적립금 1회 사용에 소요된 시간 : {}ms", useTime);
        log.info("회원 적립금 적립에 총 소요된 시간 : {}ms", totalEarn);
        log.info("회원 적립금 사용에 총 소요된 시간 : {}ms", totalUse);
        log.info("적립과 사용에 소요된 총 시간 : {} 초", (totalEarn + totalUse) / 1000);

        // 적립금 합계가 예상한 값과 같은지 확인합니다.
        int memberPointTotal = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        log.info("현재 예상 적립금 : {}", currentPoint);
        log.info("최총 적립금 : {}", memberPointTotal);

        assertEquals(currentPoint, memberPointTotal);
    }

    /**
     * 다수의 적립금 적립을 테스트합니다.
     * Rollback을 False 로 두고 더미 데이터를 생성할 수도 있습니다.
     */
    @Test
    void createMassiveRowsTest() {
        // given

        // 많은 수의 행을 생성합니다.
        final int MASSIVE_ROWS = 100;

        // 랜덤한 금액을 적립합니다.
        int maxTestPointAmount = TEST_POINT_AMOUNT;
        int currentPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // when

        // 다수의 적립금 적립 성능을 테스트하기 위해 스톱워치를 시작합니다.
        StopWatch stopWatch = new StopWatch();

        log.info("테스트 횟수 : {}", MASSIVE_ROWS);
        log.info("최대 적립금액: {}", maxTestPointAmount);

        log.info("Member Point Earn Start");
        stopWatch.start("Member Point Earn");
        // 적립금을 추가합니다.
        for (int i = 0; i < MASSIVE_ROWS; i++) {
            int amountPointEarn = Math.toIntExact(Math.round(Math.random() * maxTestPointAmount)) + 1;
            MemberPointEvent event = memberPointService.earnMemberPoint(getTestMemberPointCreateRequest(TEST_MEMBER_ID, amountPointEarn));
            currentPoint += event.getAmount();
        }
        stopWatch.stop();

        // then

        // stopWatch.prettyPrint()를 통해 측정된 시간을 출력합니다.
        System.out.println(stopWatch.prettyPrint());

        // 각 작업당 1회에 사용된 시간을 확인합니다.
        long totalEarn = stopWatch.getTaskInfo()[0].getTimeMillis();
        long earnTime = totalEarn / MASSIVE_ROWS;

        log.info("회원 적립금 적립 횟수 : {}ms", MASSIVE_ROWS);
        log.info("회원 적립금 1회 적립에 소요된 시간 : {}ms", earnTime);
        log.info("회원 적립금 적립에 총 소요된 시간 : {}ms", totalEarn);

        // 적립금 합계가 예상한 값과 같은지 확인합니다.
        int memberPointTotal = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        log.info("현재 예상 적립금 : {}", currentPoint);
        log.info("최총 적립금 : {}", memberPointTotal);
        assertEquals(currentPoint, memberPointTotal);
    }


    /**
     * 회원 적립금 만료 기능을 테스트합니다.
     * 만료될 적립금이 필요하므로, 임의로 만료 시점이 과거인 적립금을 생성합니다.
     */
    @Test
    void expireMemberPoint() {
        // given

        // 현재 금액을 조회합니다.
        int currentPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // 만료 처리할 적립금을 생성합니다.
        int maxTestPointAmount = TEST_POINT_AMOUNT;
        int amountPointEarn1 = Math.toIntExact(Math.round(Math.random() * maxTestPointAmount)) + 1;
        int amountPointEarn2 = Math.toIntExact(Math.round(Math.random() * maxTestPointAmount)) + 1;

        // 만료 처리할 적립금을 생성합니다.
        MemberPointEvent event = memberPointService.earnMemberPoint(getTestMemberPointCreateRequest(TEST_MEMBER_ID, amountPointEarn1));

        // 만료 처리할 적립금의 일부를 사용합니다.
        LocalDateTime past = LocalDateTime.now().minusMonths(5);
        int amountPointUse = Math.toIntExact(Math.round(Math.random() * amountPointEarn1) + 1);

        MemberPointEvent use = memberPointService.useMemberPoint(getTestMemberPointUseRequest(TEST_MEMBER_ID, amountPointUse));

        // 만료 처리할 적립금의 만료 시점을 과거로 설정합니다.
        memberPointService.changeExpireAt(event.getId(), past);

        // 만료 처리되지 않을 적립금을 생성합니다.
        MemberPointEvent event2 = memberPointService.earnMemberPoint(getTestMemberPointCreateRequest(TEST_MEMBER_ID, amountPointEarn2));

        // when
        // 만료 처리를 수행합니다.
        memberPointService.expireMemberPoint();

        // then

        // 가장 최근에 생성된 적립금 이벤트들을 조회합니다.
        MemberPointEventSearch search = getMemberPointEventSearch(TEST_MEMBER_ID, 0, 10);
        Page<MemberPointEvent> memberPointEvents = memberPointService.getMemberPointEvents(search);

        // 가장 마지막에 실행된 이벤트는 만료 처리여야 합니다.
        MemberPointEvent latest = memberPointEvents.getContent().stream().findFirst().orElseThrow();
        log.info("가장 마지막 실행된 이벤트 ID : {}", latest.getId());
        log.info("가장 마지막 실행된 이벤트 만료 시점 : {}", latest.getExpireAt());
        log.info("가장 마지막 실행된 이벤트 금액 : {}", latest.getAmount());
        log.info("가장 마지막 실행된 이벤트 종류 : {}", latest.getType());
        assertEquals(MemberPointEvent.MemberPointEventType.EXPIRE, latest.getType());

        // 만료 처리된 적립금의 금액이 예상한 값과 같은지 확인합니다.
        int expectedPoint = currentPoint + amountPointEarn2;
        int resultPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);
        log.info("예상된 적립금 : {}", expectedPoint);
        log.info("실제 적립금 : {}", resultPoint);
        assertEquals(expectedPoint, resultPoint);
    }


    @Test
    void rollbackMemberPointUseResponse() {
        // given

        // 적립금을 적립합니다.
        MemberPointEvent earn = memberPointService.earnMemberPoint(getTestMemberPointCreateRequest(TEST_MEMBER_ID, 10000));

        // 적립 금액을 확인합니다.
        int before = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // 적립 내역을 확인합니다.
        MemberPointEventSearch search = getMemberPointEventSearch(TEST_MEMBER_ID, 0, 10);
        Page<MemberPointEvent> memberPointEvents = memberPointService.getMemberPointEvents(search);

        // 적립금을 사용합니다.
        MemberPointEvent use = memberPointService.useMemberPoint(getTestMemberPointUseRequest(TEST_MEMBER_ID, 1000));
        int afterUse = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);
        Page<MemberPointEvent> memberPointEventsAfterUse = memberPointService.getMemberPointEvents(search);

        // when

        // 적립금 사용을 취소합니다.
        MemberPointEventResponse rollback = memberPointService.rollbackMemberPointUseResponse(TEST_MEMBER_ID, use.getId());

        // then

        // 금액이 정상적인지 확인합니다.
        int afterRollback = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);
        log.info("적립금 적립 이후 금액 : {}", before);
        log.info("적립금 사용 이후 금액 : {}", afterUse);
        log.info("적립금 사용 취소 이후 금액 : {}", afterRollback);

        assertEquals(before, afterRollback);

        // 적립 내역에 취소된 이벤트가 있는지 확인합니다.
        Page<MemberPointEvent> memberPointEventsAfterRollback = memberPointService.getMemberPointEvents(search);
        memberPointEventsAfterUse.get().forEach(event -> log.info("취소 전 타입 {} 금액 : {}", event.getType(), event.getAmount()));
        memberPointEventsAfterRollback.get().forEach(event -> log.info("취소 후 타입 {} 금액 : {}", event.getType(), event.getAmount()));
        // 적립금 사용 취소 이후 적립금 사용 이벤트가 조회 되지 않아야 합니다.
        assertFalse(memberPointEventsAfterRollback.getContent().stream().anyMatch(event -> event.getId() == use.getId()));
        // 사용 이후와 롤백 이후 조회되는 이벤트 갯수가 같아서는 안됩니다.
        assertNotEquals(memberPointEventsAfterUse.getTotalElements(), memberPointEventsAfterRollback.getTotalElements());
        // 한번 더 롤백을 수행하면 실패해야 합니다.
        entityManager.flush();
        entityManager.clear();
        assertThrows(MemberPointAlreadyRollbackedException.class, () -> {
            MemberPointEventResponse rollbackAgain = memberPointService.rollbackMemberPointUseResponse(TEST_MEMBER_ID, use.getId());
        });
    }

    @Execution(value = ExecutionMode.CONCURRENT)
    @Rollback(value = false)
    @RepeatedTest(1000)
    void memberPointUseConcurrencyTest() {
        // given

        // 적립금을 적립합니다.
        MemberPointEvent earn = memberPointService.earnMemberPoint(getTestMemberPointCreateRequest(TEST_MEMBER_ID, 10000));
        log.info("적립금 적립 금액 : {}", earn.getAmount());

        // 적립 금액을 확인합니다.
        int before = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);
        log.info("적립금 적립 이전 금액 : {}", before);

        long memberPointDetailId = earn.getMemberPointDetails().stream().findFirst().orElseThrow().getMemberPointDetailGroupId();


        // when

        // 적립금을 사용합니다.
        memberPointDetailRepository.findById(memberPointDetailId).orElseThrow();
        MemberPointEvent event = memberPointService.useMemberPoint(getTestMemberPointUseRequest(TEST_MEMBER_ID, 100));
        log.info("적립금 사용 이벤트 : {}", event.getId());

        // then

        // 금액이 정상적인지 확인합니다.
        int after = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);
        log.info("적립금 적립 이후 금액 : {}", before);
        log.info("적립금 사용 이후 금액 : {}", after);

    }


    /*
     * 이하는 테스트용 유틸리티 메소드들입니다.
     */


    /**
     * 테스트용 적립금 생성 요청 DTO를 생성합니다.
     *
     * @param amount   적립할 회원 적립금 금액
     * @param memberId 적립할 회원 ID
     * @return 적립금 생성 요청 DTO
     */
    private static MemberPointUseRequest getTestMemberPointUseRequest(long memberId, int amount) {
        MemberPointUseRequest memberPointUseRequest = new MemberPointUseRequest();
        memberPointUseRequest.setMemberId(memberId);
        memberPointUseRequest.setAmount(amount);
        return memberPointUseRequest;
    }

    /**
     * 테스트용 적립금 사용 요청 DTO를 생성합니다.
     *
     * @param amount   사용할 회원 적립금 금액
     * @param memberId 사용할 회원 ID
     * @return 적립금 사용 요청 DTO
     */
    private static MemberPointCreateRequest getTestMemberPointCreateRequest(long memberId, int amount) {
        MemberPointCreateRequest memberPointCreateRequest = new MemberPointCreateRequest();
        memberPointCreateRequest.setMemberId(memberId);
        memberPointCreateRequest.setAmount(amount);
        return memberPointCreateRequest;
    }

    /**
     * 테스트용 적립금 적립/사용 내역 검색 DTO를 생성합니다.
     *
     * @param memberId 적립/사용 내역을 조회할 회원 ID
     * @param page     패아자 번호
     * @param size     페이지 크기
     * @return 적립/사용 내역 검색 DTO
     */
    private static MemberPointEventSearch getMemberPointEventSearch(long memberId, int page, int size) {
        MemberPointEventSearch search = new MemberPointEventSearch();
        search.setMemberId(memberId);
        search.setPage(page);
        search.setSize(size);
        return search;
    }

}