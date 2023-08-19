package dev.pjc1991.commerce.member.point.service;

import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import dev.pjc1991.commerce.member.point.dto.MemberPointCreateRequest;
import dev.pjc1991.commerce.member.point.dto.MemberPointEventSearch;
import dev.pjc1991.commerce.member.point.dto.MemberPointUseRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberPointServiceTest {

    private static final Logger log = LoggerFactory.getLogger(MemberPointServiceTest.class);

    private static final int TEST_MEMBER_ID = 1;

    @Autowired
    MemberPointService memberPointService;

    @Test
    void getMemberPointTotal() {
        // given

        // 랜덤한 금액을 적립합니다.
        int testPointAmount = Math.toIntExact(Math.round(Math.random() * 100000));

        // 적립금 생성 요청 오브젝트를 생성합니다.
        MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(TEST_MEMBER_ID, testPointAmount);

        // 적립금을 추가합니다.
        memberPointService.earnMemberPoint(memberPointCreateRequest);

        // when

        // 적립금 합계를 조회합니다.
        int result = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // then

        // 적립금 합계가 예상한 값과 같은지 확인합니다.
        log.info("expected point: {}", testPointAmount);
        log.info("result point: {}", result);

        assertEquals(testPointAmount, result);
    }

    @Test
    void getMemberPointEvents() {
        // given

        // 랜덤한 갯수의 적립금을 적립합니다.
        int maxTestCount = 100;
        int testCount = Math.toIntExact(Math.round(Math.random() * maxTestCount));
        log.info("test count: {}", testCount);

        // 적립금 생성 요청 오브젝트를 생성합니다.
        int maxTestPointAmount = 100000;
        for (int i = 0; i < testCount; i++) {
            MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(TEST_MEMBER_ID, Math.toIntExact(Math.round(Math.random() * maxTestPointAmount)));
            memberPointService.earnMemberPoint(memberPointCreateRequest);
        }

        // when

        // 적립금 적립/사용 내역을 조회합니다.
        // 검색 파라미터를 담은 오브젝트를 생성합니다.
        int page = 0;
        int size = 10;
        MemberPointEventSearch search = getMemberPointEventSearch(TEST_MEMBER_ID, page, size);
        Page<MemberPointEvent> result = memberPointService.getMemberPointEvents(search);

        // then

        // 조회된 적립금 적립/사용 내역의 갯수가 예상한 값과 같은지 확인합니다.
        log.info("expected count: {}", testCount);
        log.info("result count: {}", result.getTotalElements());
        assertEquals(testCount, result.getTotalElements());

        // 정상적으로 페이징이 되었는지 확인합니다.
        int expectedSize = Math.min(testCount, search.getSize());
        log.info("expected size: {}", expectedSize);
        log.info("result size: {}", result.getSize());
        assertEquals(expectedSize, result.getSize());
    }

    @Test
    void earnMemberPoint() {
        // given

        // 현재 금액을 조회합니다.
        int currentPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // 랜덤한 금액을 적립합니다.
        int testPointAmount = Math.toIntExact(Math.round(Math.random() * 100000));

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
        assertEquals(TEST_MEMBER_ID, result.getMemberId());
        assertEquals(testPointAmount, result.getAmount());

        // 적립금 합계가 예상한 값과 같은지 확인합니다.
        int resultPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);
        log.info("expected point: {}", expectedPoint);
        log.info("result point: {}", resultPoint);
        assertEquals(expectedPoint, resultPoint);

    }

    @Test
    void useMemberPoint() {
        // given

        // 현재 금액을 조회한다.
        int currentPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // 랜덤한 금액을 적립한다.
        int maxTestPointAmount = 100000;
        int testPointEarnAmount = Math.toIntExact(Math.round(Math.random() * maxTestPointAmount));
        log.info("testPointEarnAmount: {}", testPointEarnAmount);

        // 랜덤한 금액을 사용한다. 사용할 금액은 적립금 적립 금액보다 작거나 같다.
        int testPointUseAmount = Math.toIntExact(Math.round(Math.random() * testPointEarnAmount));
        log.info("testPointUseAmount: {}", testPointUseAmount);

        // 예상 금액을 계산한다.
        int expectedPoint = currentPoint + testPointEarnAmount - testPointUseAmount;
        log.info("expectedPoint: {}", expectedPoint);

        // 적립금 생성 요청 오브젝트를 생성한다.
        MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(TEST_MEMBER_ID, testPointEarnAmount);

        // 적립금을 추가한다.
        memberPointService.earnMemberPoint(memberPointCreateRequest);

        // when

        // 적립금 사용 요청 오브젝트를 생성한다.
        MemberPointUseRequest memberPointUseRequest = getTestMemberPointUseRequest(TEST_MEMBER_ID, testPointUseAmount);

        // 적립금을 사용한다.
        MemberPointEvent result = memberPointService.useMemberPoint(memberPointUseRequest);

        // then

        // 적립금 적립/사용 내역이 정상적으로 생성되었는지 확인한다.
        log.info("result: {}", result);
        assertNotNull(result);

        // 생성된 객체의 값이 정상적인지 확인한다.
        log.info("expected member id: {}", TEST_MEMBER_ID);
        log.info("result member id: {}", result.getMemberId());
        assertEquals(TEST_MEMBER_ID, result.getMemberId());
        log.info("expected amount: {}", -testPointUseAmount);
        log.info("result amount: {}", result.getAmount());
        assertEquals(-testPointUseAmount, result.getAmount());

        // 적립금 합계가 예상한 값과 같은지 확인한다.
        log.info("expected point: {}", expectedPoint);
        log.info("result point: {}", memberPointService.getMemberPointTotal(TEST_MEMBER_ID));
        assertEquals(expectedPoint, memberPointService.getMemberPointTotal(TEST_MEMBER_ID));
    }

    /**
     * 테스트용 적립금 생성 요청 DTO를 생성합니다.
     *
     * @param amount
     * 적립할 회원 적립금 금액
     * @param memberId
     * 적립할 회원 ID
     * @return
     * 적립금 생성 요청 DTO
     */
    private static MemberPointUseRequest getTestMemberPointUseRequest(int memberId, int amount) {
        MemberPointUseRequest memberPointUseRequest = new MemberPointUseRequest();
        memberPointUseRequest.setMemberId(memberId);
        memberPointUseRequest.setAmount(amount);
        return memberPointUseRequest;
    }

    /**
     * 테스트용 적립금 사용 요청 DTO를 생성합니다.
     *
     * @param amount
     * 사용할 회원 적립금 금액
     * @param memberId
     * 사용할 회원 ID
     * @return
     * 적립금 사용 요청 DTO
     */
    private static MemberPointCreateRequest getTestMemberPointCreateRequest(int memberId, int amount) {
        MemberPointCreateRequest memberPointCreateRequest = new MemberPointCreateRequest();
        memberPointCreateRequest.setMemberId(memberId);
        memberPointCreateRequest.setAmount(amount);
        return memberPointCreateRequest;
    }

    /**
     * 테스트용 적립금 적립/사용 내역 검색 DTO를 생성합니다.
     *
     * @param memberId
     * 적립/사용 내역을 조회할 회원 ID
     * @param page
     * 패아자 번호
     * @param size
     * 페이지 크기
     * @return
     * 적립/사용 내역 검색 DTO
     */
    private static MemberPointEventSearch getMemberPointEventSearch(int memberId, int page, int size) {
        MemberPointEventSearch search = new MemberPointEventSearch();
        search.setMemberId(memberId);
        search.setPage(page);
        search.setSize(size);
        return search;
    }
}