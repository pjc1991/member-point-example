package dev.pjc1991.commerce.member.point.domain;

import dev.pjc1991.commerce.member.point.dto.MemberPointCreateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 회원 적립금 이벤트 도메인
 *
 */
@Entity
@Table(name = "MEMBER_POINT")
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
    private Set<MemberPointDetail> memberPointDetails;

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
        MemberPointEvent memberPointEvent = new MemberPointEvent();
        memberPointEvent.memberId = memberPointCreate.getMemberId();
        memberPointEvent.amount = memberPointCreate.getAmount();
        memberPointEvent.createdAt = LocalDateTime.now();
        memberPointEvent.expireAt = memberPointEvent.createdAt.plusMonths(MEMBER_POINT_EXPIRE_MONTH);
        return memberPointEvent;
    }
}