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
    int getMemberPointTotal(long memberId);

    /**
     * 회원 적립금 합계 조회 (Response)
     *
     * @param memberId 회원 아이디
     * @return 회원 적립금 합계 (MemberPointTotalResponse)
     */
    MemberPointTotalResponse getMemberPointTotalResponse(long memberId);

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
     * 회원 적립금의 캐시를 초기화합니다.
     * 현재 테스트에서 사용하고 있습니다.
     *
     * @param memberId 회원 아이디
     */
    void clearMemberPointTotalCache(long memberId);

    /**
     * 회원 적립금 만료 처리
     * 만료 시간이 지난 회원 적립금을 만료 처리합니다.
     * 스케쥴링을 통해 매일 자정에 실행됩니다.
     */
    void expireMemberPoint();

    /**
     * 회원 적립금 만료 시간 변경 (테스트 전용)
     * 해당 회원 적립금의 만료 시간을 강제로 변경합니다.
     * 테스트 전용입니다.
     *
     * @param memberPointEventId 회원 적립금 이벤트 아이디
     *                           (MemberPointEvent.id)
     * @param expireAt           만료 시간
     *                           (LocalDateTime)
     */
    void changeExpireAt(long memberPointEventId, LocalDateTime expireAt);

    /**
     * 회원 적립금 일치성 검사 (테스트)
     * 해당 회원의 적립금이 선입선출 형태로 사용되었는지 확인합니다.
     * 테스트 전용입니다.
     * @param memberId 회원 아이디
     */
    void checkMemberPoint(long memberId);

}
