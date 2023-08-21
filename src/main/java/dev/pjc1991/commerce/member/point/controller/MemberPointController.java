package dev.pjc1991.commerce.member.point.controller;

import dev.pjc1991.commerce.member.point.dto.MemberPointTotalResponse;
import dev.pjc1991.commerce.member.point.service.MemberPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    public ResponseEntity<?> getMemberPointTotal(@PathVariable int memberId) {
        MemberPointTotalResponse response = new MemberPointTotalResponse(memberId, memberPointService.getMemberPointTotal(memberId));
        return ResponseEntity.ok(response);
    }
}
