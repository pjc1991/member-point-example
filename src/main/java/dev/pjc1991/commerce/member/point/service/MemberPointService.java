package dev.pjc1991.commerce.member.point.service;

import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import dev.pjc1991.commerce.member.point.dto.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;


public interface MemberPointService {

    /**
     * 회원 적립금 합계 조회
     *
     * @param memberId 회원 아이디
     * @return 회원 적립금 합계 (int)
     */
    int getMemberPointTotal(int memberId);

    /**
     * 회원 적립금 합계 조회 (Response)
     *
     * @param memberId 회원 아이디
     * @return 회원 적립금 합계 (MemberPointTotalResponse)
     */
    MemberPointTotalResponse getMemberPointTotalResponse(int memberId);

    /**
     * 회원 적립금 적립/사용 내역 조회
     *
     * @param search (MemberPointEventSearch) page : 페이지 번호, size : 페이지 사이즈, memberId : 회원 아이디
     * @return 회원 적립금 적립/사용 내역 (Page<MemberPointEvent>)
     */
    Page<MemberPointEvent> getMemberPointEvents(MemberPointEventSearch search);

    /**
     * 회원 적립금 적립/사용 내역 조회 (Response)
     *
     * @param search (MemberPointEventSearch) page : 페이지 번호, size : 페이지 사이즈, memberId : 회원 아이디
     * @return 회원 적립금 적립/사용 내역 (Page<MemberPointEventResponse>)
     */
    Page<MemberPointEventResponse> getMemberPointEventResponses(MemberPointEventSearch search);

    /**
     * 회원 적립금 적립
     *
     * @param memberPointCreate (MemberPointCreateRequest) memberId : 회원 아이디, amount : 적립금
     * @return 회원 적립금 적립 (MemberPointEvent)
     */
    MemberPointEvent earnMemberPoint(MemberPointCreateRequest memberPointCreate);

    /**
     * 회원 적립금 적립 (Response)
     *
     * @param memberPointCreate (MemberPointCreateRequest) memberId : 회원 아이디, amount : 적립금
     * @return 회원 적립금 적립 (MemberPointEventResponse)
     */
    MemberPointEventResponse earnMemberPointResponse(MemberPointCreateRequest memberPointCreate);

    /**
     * 회원 적립금 사용
     *
     * @param memberPointUse (MemberPointUseRequest) memberId : 회원 아이디, amount : 적립금
     * @return 회원 적립금 사용 (MemberPointEvent)
     */
    MemberPointEvent useMemberPoint(MemberPointUseRequest memberPointUse);

    /**
     * 회원 적립금 사용 (Response)
     *
     * @param memberPointUse (MemberPointUseRequest) memberId : 회원 아이디, amount : 적립금
     * @return 회원 적립금 사용 (MemberPointEventResponse)
     */
    MemberPointEventResponse useMemberPointResponse(MemberPointUseRequest memberPointUse);

    /**
     * 회원 적립금 캐시 초기화
     * @param memberId 회원 아이디
     */
    void clearCache(int memberId);

    /**
     * 회원 적립금 만료 처리
     */
    void expireMemberPoint();

    /**
     * 회원 적립금 만료 시간 변경 (테스트 전용)
     */
    void changeExpireAt(long memberPointEventId, LocalDateTime expireAt);

    /**
     * 회원 적립금 일치성 검사 (테스트)
     * 해당 회원의 적립금이 선입선출 형태로 사용되었는지 확인합니다.
     * @param memberId 회원 아이디
     */
    void checkMemberPoint(int memberId);

}
