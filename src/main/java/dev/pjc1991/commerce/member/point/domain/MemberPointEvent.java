package dev.pjc1991.commerce.member.point.domain;

import dev.pjc1991.commerce.member.point.dto.MemberPointCreateRequest;
import dev.pjc1991.commerce.member.point.dto.MemberPointUseRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 회원 적립금 이벤트 도메인
 *
 */
@Entity
@Table(name = "MEMBER_POINT_EVENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPointEvent {

    /**
     * 회원 적립금 이벤트 ID
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
     * 회원 적립금 적립/사용 내역
     */
    @OneToMany(mappedBy = "memberPointEvent", cascade = CascadeType.ALL)
    private Set<MemberPointDetail> memberPointDetails = new HashSet<>();

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

    /**
     * 회원 적립금 만료 기간 (개월)
     * 이 기간이 지나면 적립금을 만료 처리합니다.
     */
    @Transient
    public static final int MEMBER_POINT_EXPIRE_MONTH = 12;


    /**
     * 회원 적립금 적립 이벤트를 생성합니다.
     * @param memberPointCreate
     * 회원 적립금 적립 요청 오브젝트
     * @return
     * 회원 적립금 적립 이벤트
     */
    public static MemberPointEvent earnMemberPoint(MemberPointCreateRequest memberPointCreate) {
        if (memberPointCreate.getAmount() < 0) {
            throw new RuntimeException("적립금 적립은 음수가 될 수 없습니다.");
        }

        if (memberPointCreate.getMemberId() == null) {
            throw new RuntimeException("회원 ID가 null입니다.");
        }

        MemberPointEvent memberPointEvent = new MemberPointEvent();
        memberPointEvent.memberId = memberPointCreate.getMemberId();
        memberPointEvent.amount = memberPointCreate.getAmount();
        memberPointEvent.createdAt = LocalDateTime.now();
        memberPointEvent.expireAt = memberPointEvent.createdAt.plusMonths(MEMBER_POINT_EXPIRE_MONTH);
        return memberPointEvent;
    }

    /**
     * 회원 적립금 사용 이벤트를 생성합니다.
     * @param memberPointUse
     * 회원 적립금 사용 요청 오브젝트
     * @return
     * 회원 적립금 사용 이벤트
     */
    public static MemberPointEvent useMemberPoint(MemberPointUseRequest memberPointUse) {
        if (memberPointUse.getAmount() < 0) {
            throw new RuntimeException("적립금 사용은 음수가 될 수 없습니다.");
        }
        MemberPointEvent memberPointEvent = new MemberPointEvent();
        memberPointEvent.memberId = memberPointUse.getMemberId();
        memberPointEvent.amount = -memberPointUse.getAmount();
        // 사용은 음수로 표현합니다.
        memberPointEvent.createdAt = LocalDateTime.now();
        memberPointEvent.expireAt = null;
        // 사용은 만료 시점이 없습니다.

        return memberPointEvent;
    }
}