package dev.pjc1991.commerce.member.point.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.pjc1991.commerce.member.point.dto.MemberPointCreateRequest;
import dev.pjc1991.commerce.member.point.dto.MemberPointDetailRemain;
import dev.pjc1991.commerce.member.point.dto.MemberPointUseRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 회원 적립금 이벤트 도메인
 */
@Entity
@Table(
        name = "MEMBER_POINT_EVENT"
        , indexes = {
        @Index(name = "IDX_MEMBER_POINT_EVENT_MEMBER_ID", columnList = "MEMBER_ID"),
        @Index(name = "IDX_MEMBER_POINT_EVENT_CREATED_AT", columnList = "CREATED_AT"),
        @Index(name = "IDX_MEMBER_POINT_EVENT_EXPIRE_AT", columnList = "EXPIRE_AT")
}
)
@Getter
@Slf4j
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
    @JsonIgnore
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
     * 회원 적립금 이벤트 종류
     */
    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberPointEventType type;

    /**
     * 회원 적립금 만료 기간 (개월)
     * 이 기간이 지나면 적립금을 만료 처리합니다.
     */
    @Transient
    public static final int MEMBER_POINT_EXPIRE_MONTH = 12;


    /**
     * 회원 적립금 적립 이벤트를 생성합니다.
     *
     * @param memberPointCreate 회원 적립금 적립 요청 오브젝트
     * @return 회원 적립금 적립 이벤트
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
        memberPointEvent.expireAt = LocalDateTime.of(memberPointEvent.createdAt.plusMonths(MEMBER_POINT_EXPIRE_MONTH).toLocalDate(), LocalTime.MAX);
        memberPointEvent.type = MemberPointEventType.EARN;
        return memberPointEvent;
    }

    /**
     * 회원 적립금 사용 이벤트를 생성합니다.
     *
     * @param memberPointUse 회원 적립금 사용 요청 오브젝트
     * @return 회원 적립금 사용 이벤트
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
        memberPointEvent.type = MemberPointEventType.USE;

        return memberPointEvent;
    }

    /**
     * 회원 적립금 만료 이벤트를 생성합니다.
     *
     * @param remain 만료될 회원 적립금 상세 내역 조회 결과
     * @return 회원 적립금 만료 이벤트
     */
    public static MemberPointEvent expireMemberPoint(MemberPointDetailRemain remain) {

        if (remain == null) {
            throw new RuntimeException("만료될 회원 적립금 상세 내역이 null입니다.");
        }

        if (remain.getExpireAt().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("만료될 회원 적립금 상세 내역의 만료 시점이 현재 시점보다 미래입니다.");
        }

        MemberPointEvent memberPointEvent = new MemberPointEvent();
        memberPointEvent.memberId = remain.getMemberId();
        memberPointEvent.amount = -remain.getRemain();
        // 만료는 사용으로 표현합니다.
        memberPointEvent.createdAt = LocalDateTime.now();
        // 만료는 현재 시점으로 표현합니다. 검색 쿼리에서 만료된 적립금을 제외하기 위함입니다.
        memberPointEvent.expireAt = LocalDateTime.now();
        memberPointEvent.type = MemberPointEventType.EXPIRE;

        return memberPointEvent;
    }

    /**
     * 회원 적립금의 만료 시점을 변경합니다.
     * 테스트 코드에서만 사용합니다.
     * @param localDateTime 변경할 만료 시점
     */
    public void setExpireAt(LocalDateTime localDateTime) {
        log.warn("회원 적립금 이벤트의 만료 시점을 변경합니다. 이 메소드는 테스트 코드에서만 사용합니다. 변경할 만료 시점: {}", localDateTime);
        this.expireAt = localDateTime;
        this.getMemberPointDetails().forEach(memberPointDetail -> memberPointDetail.setExpireAt(localDateTime));
    }

    public enum MemberPointEventType {

        EARN("적립"),
        USE("사용"),
        EXPIRE("만료"),
        REFUND("환불"),
        CANCEL("사용 취소");

        private final String description;

        MemberPointEventType(String description) {
            this.description = description;
        }


    }
}