package dev.pjc1991.commerce.member.point.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MemberPointDetailRemain {

    // 회원 적립금 상세 그룹 ID
    private Long MemberPointDetailGroupId;

    // 회원 적립금 사용 후 잔액
    private Integer remain;

    // 회원 적립금 만료 시점
    private LocalDateTime expireAt;

    // 회원 적립금 적립 시점
    private LocalDateTime createdAt;

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

}
