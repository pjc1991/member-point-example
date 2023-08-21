package dev.pjc1991.commerce.member.point.controller;

import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import dev.pjc1991.commerce.member.point.dto.MemberPointCreateRequest;
import dev.pjc1991.commerce.member.point.dto.MemberPointEventSearch;
import dev.pjc1991.commerce.member.point.dto.MemberPointTotalResponse;
import dev.pjc1991.commerce.member.point.dto.MemberPointUseRequest;
import dev.pjc1991.commerce.member.point.service.MemberPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MemberPointController {

    private final MemberPointService memberPointService;

    /**
     * 회원 적립금 합계 조회
     * @param memberId
     * 회원 아이디
     * @return
     * 회원 적립금 합계 응답 오브젝트
     * memberId: 회원 아이디, totalPoint: 적립금 합계
     */
    @GetMapping("/member/{memberId}/point/total")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getMemberPointTotal(@PathVariable int memberId) {
        MemberPointTotalResponse response = new MemberPointTotalResponse(memberId, memberPointService.getMemberPointTotal(memberId));
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 적립금 적립/사용 내역 조회
     * @param memberId
     * 회원 아이디
     * @param search
     * 검색 조건
     * page : 페이지 번호, size : 페이지 사이즈
     * @return
     */
    @GetMapping("/member/{memberId}/point")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getMemberPointEvents(@PathVariable int memberId, MemberPointEventSearch search) {
        search.setMemberId(memberId);
        Page<MemberPointEvent> memberPointEvents = memberPointService.getMemberPointEvents(search);
        return ResponseEntity.ok(memberPointEvents);
    }

    /**
     * 회원 적립금 적립
     * @param memberId
     * 회원 아이디
     * @param memberPointCreate
     * 적립금 생성 요청 오브젝트
     * amount: 적립금 금액
     */
    @PostMapping("/member/{memberId}/point/earn")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> earnMemberPoint(@PathVariable int memberId, MemberPointCreateRequest memberPointCreate) {
        memberPointCreate.setMemberId(memberId);
        MemberPointEvent memberPointEvent = memberPointService.earnMemberPoint(memberPointCreate);
        return ResponseEntity.ok(memberPointEvent);
    }

    /**
     * 회원 적립금 사용
     * @param memberId
     * 회원 아이디
     * @param memberPointUseRequest
     * 적립금 사용 요청 오브젝트
     * amount: 적립금 사용금액
     * @return
     */
    @PostMapping("/member/{memberId}/point/use")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> useMemberPoint(@PathVariable int memberId, MemberPointUseRequest memberPointUseRequest) {
        memberPointUseRequest.setMemberId(memberId);
        MemberPointEvent memberPointEvent = memberPointService.useMemberPoint(memberPointUseRequest);
        return ResponseEntity.ok(memberPointEvent);
    }
}
