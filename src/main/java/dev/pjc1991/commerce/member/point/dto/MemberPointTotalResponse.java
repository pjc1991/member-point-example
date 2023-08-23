package dev.pjc1991.commerce.member.point.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberPointTotalResponse {

    private long memberId;
    private Integer totalPoint;

    public MemberPointTotalResponse(long memberId, Integer totalPoint) {
        this.memberId = memberId;
        this.totalPoint = totalPoint;
    }
}
