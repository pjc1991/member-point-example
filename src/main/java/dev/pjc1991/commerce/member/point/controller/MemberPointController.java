package dev.pjc1991.commerce.member.point.controller;

import dev.pjc1991.commerce.member.point.dto.*;
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
     *
     * @param memberId 회원 아이디
     * @return 회원 적립금 합계 응답 오브젝트
     * memberId: 회원 아이디, totalPoint: 적립금 합계
     */
    @GetMapping("/member/{memberId}/point/total")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public MemberPointTotalResponse getMemberPointTotal(@PathVariable long memberId) {
        return memberPointService.getMemberPointTotalResponse(memberId);
    }

    /**
     * 회원 적립금 적립/사용 내역 조회
     *
     * @param memberId 회원 아이디
     * @param search   검색 조건
     *                 page : 페이지 번호, size : 페이지 사이즈
     * @return 회원 적립금 적립/사용 내역 Page 오브젝트
     */
    @GetMapping("/member/{memberId}/point")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Page<MemberPointEventResponse> getMemberPointEvents(@PathVariable long memberId, @RequestBody(required = false) MemberPointEventSearch search) {

        if (search == null) {
            search = new MemberPointEventSearch();
        }

        search.setMemberId(memberId);
        return memberPointService.getMemberPointEventResponses(search);
    }

    /**
     * 회원 적립금 적립
     *
     * @param memberId          회원 아이디
     * @param memberPointCreate 회원 적립금 적립 내역 오브젝트
     *                          amount: 적립금 금액
     */
    @PostMapping("/member/{memberId}/point/earn")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public MemberPointEventResponse earnMemberPoint(@PathVariable long memberId, @RequestBody MemberPointCreateRequest memberPointCreate) {
        memberPointCreate.setMemberId(memberId);
        return memberPointService.earnMemberPointResponse(memberPointCreate);
    }

    /**
     * 회원 적립금 사용
     *
     * @param memberId              회원 아이디
     * @param memberPointUseRequest 적립금 사용 요청 오브젝트
     *                              amount: 적립금 사용금액
     * @return 회원 적립금 사용 내역 오브젝트
     */
    @PostMapping("/member/{memberId}/point/use")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public MemberPointEventResponse useMemberPoint(@PathVariable long memberId, @RequestBody MemberPointUseRequest memberPointUseRequest) {
        memberPointUseRequest.setMemberId(memberId);
        return memberPointService.useMemberPointResponse(memberPointUseRequest);
    }

    /**
     * 회원 적립금 사용 취소
     */
    @DeleteMapping("/member/point/{memberPointEventId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public MemberPointEventResponse rollBackMemberPointUse(@PathVariable long memberPointEventId) {
        MemberPointEventResponse response = memberPointService.getMemberPointEventResponse(memberPointEventId);
        return memberPointService.rollbackMemberPointUseResponse(response.getMemberId(), memberPointEventId);
    }

    @GetMapping("/member/point/check/{memberId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> checkMemberPoint(@PathVariable long memberId) {
        memberPointService.checkMemberPoint(memberId);
        return ResponseEntity.ok("OK");
    }
}
