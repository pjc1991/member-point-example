package dev.pjc1991.commerce.member.point.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class MemberPointDetailRemain {

    // 회원 적립금 상세 그룹 ID
    private Long MemberPointDetailGroupId;

    // 회원 적립금 사용 후 잔액
    private Integer remain;

    // 회원 적립금 최초 적립액
    private Integer earn;

    // 회원 적립금 사용액
    private Integer used;

    // 회원 적립금 만료 시점
    private LocalDateTime expireAt;

    // 회원 적립금 적립 시점
    private LocalDateTime createdAt;

    // 회원 아이디
    private Long memberId;

    public MemberPointDetailRemain(
            Long MemberPointDetailGroupId,
            Integer remain,
            LocalDateTime expireAt,
            LocalDateTime createdAt
    ) {
        this.MemberPointDetailGroupId = MemberPointDetailGroupId;
        this.remain = remain;
        this.expireAt = expireAt;
        this.createdAt = createdAt;
    }

    public MemberPointDetailRemain(
            Long MemberPointDetailGroupId,
            Integer remain,
            LocalDateTime expireAt,
            LocalDateTime createdAt,
            long memberId
    ) {
        this.MemberPointDetailGroupId = MemberPointDetailGroupId;
        this.remain = remain;
        this.expireAt = expireAt;
        this.createdAt = createdAt;
        this.memberId = memberId;
    }

    public MemberPointDetailRemain(
            Long MemberPointDetailGroupId,
            Integer remain,
            Integer earn,
            Integer used,
            LocalDateTime expireAt,
            LocalDateTime createdAt,
            long memberId
    ){
        this.MemberPointDetailGroupId = MemberPointDetailGroupId;
        this.remain = remain;
        this.earn = earn;
        this.used = used;
        this.expireAt = expireAt;
        this.createdAt = createdAt;
        this.memberId = memberId;
    }

}
