package dev.pjc1991.commerce.member.point.domain;

import dev.pjc1991.commerce.member.point.dto.MemberPointDetailRemain;
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
     * 만료 시점
     * 회원 적립금 적립 이벤트의 만료 시점과 동일하지만,
     * 사용 또는 환불로 인해 만료되는 경우에도 그룹 ID를 기준으로 만료 시점을 계산할 수 있도록
     * 사용한 적립금의 적립 이벤트와 동일한 만료 시점을 저장합니다. (적립 시점의 만료 시점)
     * 이것은 이미 만료된 적립금과 그 사용 내역을 검색에서 제외해 검색 속도를 늘리기 위함입니다. (쿼리 최적화)
     */
    @Column(name = "EXPIRE_AT", nullable = false)
    private LocalDateTime expireAt;

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
        memberPointDetail.expireAt = earnEvent.getExpireAt();
        earnEvent.getMemberPointDetails().add(memberPointDetail);

        return memberPointDetail;

    }

    public static MemberPointDetail useMemberPointDetail(MemberPointEvent useEvent, MemberPointDetailRemain remain, int useAmount) {
        if (useEvent == null) {
            throw new IllegalArgumentException("회원 적립금 사용 이벤트가 null 입니다.");
        }

        if (useAmount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0 이하일 수 없습니다.");
        }

        MemberPointDetail memberPointDetail = new MemberPointDetail();
        memberPointDetail.memberPointEvent = useEvent;
        memberPointDetail.memberPointDetailGroupId = remain.getMemberPointDetailGroupId();
        memberPointDetail.amount = -useAmount;
        memberPointDetail.createdAt = LocalDateTime.now();
        memberPointDetail.expireAt = remain.getExpireAt();
        useEvent.getMemberPointDetails().add(memberPointDetail);

        return memberPointDetail;


    }

    /**
     * 회원 적립금 적립 발생 상세 내역에 그룹 ID와 환불 대상 ID를 설정합니다.
     */
    public void updateGroupIdSelf() {
        // 총합을 계산할 때 이 상세내역 기준으로 계산할 수 있도록 자신의 ID를 그룹 ID로 설정합니다.
        this.memberPointDetailGroupId = this.id;
    }

    /**
     * 회원 적립금 사용시 상세 내역에 자기 자신을 환불 대상 ID로 설정합니다.
     */
    public void updateRefundGroupIdSelf() {
        // 환불 대상이 되는 상세 내역의 ID를 환불 대상 ID로 설정합니다.
        this.memberPointDetailRefundId = this.id;
    }
}