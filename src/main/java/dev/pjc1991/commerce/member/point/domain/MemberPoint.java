package dev.pjc1991.commerce.member.point.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBER_POINT")
@Getter
public class MemberPoint {

    /**
     * 회원 포인트 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_POINT_ID", nullable = false)
    private Long memberPointId;

    /**
     * 회원 ID
     */
    @Column(name = "MEMBER_ID", nullable = false)
    private int memberId;

    /**
     * 회원 포인트 금액
     */
    @Column(name = "MEMBER_POINT_AMOUNT", nullable = false)
    private int memberPointAmount;

    /**
     * 회원 포인트 만료 시점
     */
    @Column(name = "EXPIRE_AT", nullable = false)
    private LocalDateTime expireAt;

    /**
     * 회원 포인트 생성 시점
     */
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

}