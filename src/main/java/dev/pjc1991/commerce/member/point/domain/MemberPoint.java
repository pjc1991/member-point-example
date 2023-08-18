package dev.pjc1991.commerce.member.point.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 회원 적립금 도메인
 *
 */
@Entity
@Table(name = "MEMBER_POINT")
@Getter
public class MemberPoint {

    /**
     * 회원 적립금 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    /**
     * 회원 ID
     */
    @Column(name = "MEMBER_ID", nullable = false)
    private int memberId;

    /**
     * 회원 적립금 적립/사용 금액
     * 적립: 양수, 사용: 음수
     */
    @Column(name = "AMOUNT", nullable = false)
    private int amount;

    /**
     * 회원 적립금 만료 시점
     */
    @Column(name = "EXPIRE_AT")
    private LocalDateTime expireAt;

    /**
     * 회원 적립금 생성 시점
     */
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

}