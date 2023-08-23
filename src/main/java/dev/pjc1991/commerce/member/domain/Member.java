package dev.pjc1991.commerce.member.domain;

import dev.pjc1991.commerce.member.dto.MemberSignupRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원 도메인
 */

@Getter
@Setter
@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    public static Member signup(MemberSignupRequest memberSignupRequest) {
        Member member = new Member();
        member.setName(memberSignupRequest.getName());
        return member;
    }
}