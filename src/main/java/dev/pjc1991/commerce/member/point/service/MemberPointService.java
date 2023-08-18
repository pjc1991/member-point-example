package dev.pjc1991.commerce.member.point.service;

import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import dev.pjc1991.commerce.member.point.dto.MemberPointCreateRequest;
import dev.pjc1991.commerce.member.point.dto.MemberPointEventSearch;
import dev.pjc1991.commerce.member.point.dto.MemberPointUseRequest;
import org.springframework.data.domain.Page;


public interface MemberPointService {

    /**
     * 회원 적립금 합계 조회
     * @param memberId
     * @return
     */
    int getMemberPointTotal(int memberId);

    /**
     * 회원 적립금 적립/사용 내역 조회
     * @param search
     * @return
     */
    Page<MemberPointEvent> getMemberPointEvents(MemberPointEventSearch search);

    /**
     * 회원 적립금 적립
     * @param memberPointCreate
     * @return
     */
    MemberPointEvent earnMemberPoint(MemberPointCreateRequest memberPointCreate);

    /**
     * 회원 적립금 사용
     * @param memberPointUse
     * @return
     */
    MemberPointEvent useMemberPoint(MemberPointUseRequest memberPointUse);

}
