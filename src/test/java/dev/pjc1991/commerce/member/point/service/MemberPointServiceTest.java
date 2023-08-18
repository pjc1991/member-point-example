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
import org.springframework.test.annotation.Rollback;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(true)
class MemberPointServiceTest {

    private static final Logger log = LoggerFactory.getLogger(MemberPointServiceTest.class);

    private static final int TEST_MEMBER_ID = 1;

    @Autowired
    MemberPointService memberPointService;

    @Test
    void getMemberPointTotal() {
        // given

        // 랜덤한 금액을 적립한다.
        int testPointAmount = Math.toIntExact(Math.round(Math.random() * 100000));

        // 적립금 생성 요청 오브젝트를 생성한다.
        MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(TEST_MEMBER_ID, testPointAmount);

        // 적립금을 추가한다.
        memberPointService.earnMemberPoint(memberPointCreateRequest);

        // when

        // 적립금 합계를 조회한다.
        int result = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // then

        // 적립금 합계가 예상한 값과 같은지 확인한다.
        assertEquals(testPointAmount, result);
    }

    @Test
    void getMemberPointEvents() {
        // given

        // 랜덤한 갯수의 적립금을 적립한다.
        int testCount = Math.toIntExact(Math.round(Math.random() * 100));

        // 적립금 생성 요청 오브젝트를 생성한다.
        for (int i = 0; i < testCount; i++) {
            MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(TEST_MEMBER_ID, Math.toIntExact(Math.round(Math.random() * 100000)));
            memberPointService.earnMemberPoint(memberPointCreateRequest);
        }

        // when

        // 적립금 적립/사용 내역을 조회한다.
        // 검색 파라미터를 담은 오브젝트를 생성한다.
        MemberPointEventSearch search = new MemberPointEventSearch();
        search.setMemberId(TEST_MEMBER_ID);
        search.setPage(0);
        search.setSize(10);

        Page<MemberPointEvent> result = memberPointService.getMemberPointEvents(search);

        // then

        // 조회된 적립금 적립/사용 내역의 갯수가 예상한 값과 같은지 확인한다.
        assertEquals(testCount, result.getTotalElements());

        // 정상적으로 페이징이 되었는지 확인한다.
        int expectedSize = Math.min(testCount, 10);
        assertEquals(expectedSize, result.getSize());
    }

    @Test
    void earnMemberPoint() {
        // given

        // 현재 금액을 조회한다.
        int currentPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // 랜덤한 금액을 적립한다.
        int testPointAmount = Math.toIntExact(Math.round(Math.random() * 100000));

        // 예상 금액을 계산한다.
        int expectedPoint = currentPoint + testPointAmount;

        // when

        // 적립금 생성 요청 오브젝트를 생성한다.
        MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(TEST_MEMBER_ID, testPointAmount);

        // 적립금을 추가한다.
        MemberPointEvent result = memberPointService.earnMemberPoint(memberPointCreateRequest);

        // then

        // 적립금 적립/사용 내역이 정상적으로 생성되었는지 확인한다.
        assertNotNull(result);

        // 생성된 객체의 값이 정상적인지 확인한다.
        assertEquals(TEST_MEMBER_ID, result.getMemberId());
        assertEquals(testPointAmount, result.getAmount());

        // 적립금 합계가 예상한 값과 같은지 확인한다.
        assertEquals(expectedPoint, memberPointService.getMemberPointTotal(TEST_MEMBER_ID));

    }

    @Test
    void useMemberPoint() {
        // given

        // 현재 금액을 조회한다.
        int currentPoint = memberPointService.getMemberPointTotal(TEST_MEMBER_ID);

        // 랜덤한 금액을 적립한다.
        int testPointAmount = Math.toIntExact(Math.round(Math.random() * 100000));

        // 적립금 생성 요청 오브젝트를 생성한다.
        MemberPointCreateRequest memberPointCreateRequest = getTestMemberPointCreateRequest(TEST_MEMBER_ID, testPointAmount);

        // 적립금을 추가한다.
        memberPointService.earnMemberPoint(memberPointCreateRequest);

        // when

        // 랜덤한 금액을 사용한다.
        int usePointAmount = Math.toIntExact(Math.round(Math.random() * testPointAmount));

        // 적립금 사용 요청 오브젝트를 생성한다.
        MemberPointUseRequest memberPointUseRequest = getTestMemberPointUseRequest(usePointAmount, TEST_MEMBER_ID);

        // 적립금을 사용한다.
        MemberPointEvent result = memberPointService.useMemberPoint(memberPointUseRequest);

        // then

        // 적립금 적립/사용 내역이 정상적으로 생성되었는지 확인한다.
        assertNotNull(result);

        // 생성된 객체의 값이 정상적인지 확인한다.
        assertEquals(TEST_MEMBER_ID, result.getMemberId());
        assertEquals(usePointAmount, result.getAmount());

        // 적립금 합계가 예상한 값과 같은지 확인한다.
        assertEquals(testPointAmount - usePointAmount, memberPointService.getMemberPointTotal(TEST_MEMBER_ID));
    }

    /**
     * 테스트용 적립금 생성 요청 오브젝트를 생성합니다.
     *
     * @param amount
     * @param memberId
     * @return
     */
    private static MemberPointUseRequest getTestMemberPointUseRequest(int memberId, int amount) {
        MemberPointUseRequest memberPointUseRequest = new MemberPointUseRequest();
        memberPointUseRequest.setMemberId(memberId);
        memberPointUseRequest.setAmount(amount);
        return memberPointUseRequest;
    }

    /**
     * 테스트용 적립금 사용 요청 오브젝트를 생성합니다.
     *
     * @param amount
     * @param memberId
     * @return
     */
    private static MemberPointCreateRequest getTestMemberPointCreateRequest(int memberId, int amount) {
        MemberPointCreateRequest memberPointCreateRequest = new MemberPointCreateRequest();
        memberPointCreateRequest.setMemberId(memberId);
        memberPointCreateRequest.setAmount(amount);
        return memberPointCreateRequest;
    }
}