package dev.pjc1991.commerce.member.point.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원 적립금 상세 내역 도메인
 * 먼저 적립된 적립금이 먼저 사용되고, 남은 금액만 만료 처리할 수 있도록
 * 적립된 적립금마다 따로 상세 내역을 관리합니다.
 */
@Getter
@Entity
@Table(name = "MEMBER_POINT_DETAIL")
public class MemberPointDetail {

    /**
     * 회원 적립금 상세 내역 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    /**
     * 회원 적립금 이벤트 ID
     * 이 상세 내역을 생성한 이벤트의 ID
     */
    @Column(name = "MEMBER_POINT_EVENT_ID", nullable = false)
    private Long memberPointEventSourceId;

    /**
     * 회원 적립금 상세 내역 사용 ID
     * 이 상세 내역이 적립금 사용, 만료일 경우 사용 대상이 되는 상세 내역의 ID
     */
    @Column(name = "MEMBER_POINT_DETAIL_USE_ID")
    private Long memberPointDetailUseId;

    /**
     * 회원 적립금 상세 내역 환불 ID
     * 이 상세 내역이 적립금 환불일 경우, 대상이 되는 상세 내역의 ID
     */
    @Column(name = "MEMBER_POINT_DETAIL_REFUND_ID")
    private Long memberPointDetailRefundId;

    /**
     * 포인트 적립/사용량
     * 적립: 양수, 사용: 음수
     */
    @Column(name = "AMOUNT", nullable = false)
    private int amount;


    /**
     * 발생 시점
     */
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

}