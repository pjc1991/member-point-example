package dev.pjc1991.commerce.member.point.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPointController {

    /**
     * 컨트롤러 테스트용 메소드입니다.
     * 추후 삭제합니다.
     * @return
     */
    @GetMapping("/hello")
    public ResponseEntity<?> Hello() {
        String hello = "Hello";
        return ResponseEntity.ok(hello);
    }
}
