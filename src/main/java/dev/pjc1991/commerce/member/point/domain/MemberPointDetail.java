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
     * 회원 적립금 이벤트
     * 이 상세 내역을 생성한 이벤트
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_POINT_EVENT_ID", nullable = false)
    private MemberPointEvent memberPointEvent;

    /**
     * 회원 적립금 상세 내역 그룹 ID
     * 합계를 계산할 때 이 ID를 기준으로 합계를 계산합니다. (GROUP BY)
     * 이 상세 내역이 적립금 사용, 만료일 경우 사용 대상이 되는 상세 내역의 ID
     */
    @Column(name = "MEMBER_POINT_DETAIL_GROUP_ID")
    private Long memberPointDetailGroupId;

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

    /**
     * 회원 적립금 적립 발생에 대한 상세 내역을 생성합니다.
     * @param earnEvent
     * 회원 적립금 적립 이벤트
     * @return
     * 회원 적립금 적립 상세 내역
     */
    public static MemberPointDetail earnMemberPointDetail(MemberPointEvent earnEvent) {
        if (earnEvent == null) {
            throw new IllegalArgumentException("회원 적립금 적립 이벤트가 null입니다.");
        }

        if (earnEvent.getAmount() < 0) {
            throw new IllegalArgumentException("회원 적립금 적립 이벤트의 적립/사용량이 음수입니다.");
        }

        MemberPointDetail memberPointDetail = new MemberPointDetail();
        memberPointDetail.memberPointEvent = earnEvent;
        memberPointDetail.amount = earnEvent.getAmount();
        memberPointDetail.createdAt = earnEvent.getCreatedAt();
        earnEvent.getMemberPointDetails().add(memberPointDetail);

        return memberPointDetail;

    }

    /**
     * 회원 적립금 적립 발생 상세 내역에 그룹 ID와 환불 대상 ID를 설정합니다.
     */
    public void updateGroupIdSelf() {
        // 총합을 계산할 때 이 상세내역 기준으로 계산할 수 있도록 자신의 ID를 그룹 ID로 설정합니다.
        this.memberPointDetailGroupId = this.id;
    }
}