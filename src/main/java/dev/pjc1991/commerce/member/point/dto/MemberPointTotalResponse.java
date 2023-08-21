package dev.pjc1991.commerce.member.point.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberPointTotalResponse {

    private Integer memberId;
    private Integer totalPoint;

    public MemberPointTotalResponse(Integer memberId, Integer totalPoint) {
        this.memberId = memberId;
        this.totalPoint = totalPoint;
    }
}
