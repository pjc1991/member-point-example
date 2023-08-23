package dev.pjc1991.commerce.member.point.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberPointUseRequest {
    private Long memberId;
    private Integer amount;

    public void setAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 작을 수 없습니다.");
        }
        this.amount = amount;
    }
}
