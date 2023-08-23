package dev.pjc1991.commerce.member.point.dto;

import dev.pjc1991.commerce.member.domain.Member;
import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import dev.pjc1991.commerce.member.point.exception.BadMemberPointAmountException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberPointUseRequest {
    private Long memberId;
    private Integer amount;
    private Member owner;

    public void setAmount(int amount) {
        if (amount <= 0) {
            throw new BadMemberPointAmountException("사용 금액은 0보다 작을 수 없습니다.");
        }
        this.amount = amount;
    }
}
