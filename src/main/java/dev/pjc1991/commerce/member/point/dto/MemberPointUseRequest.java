package dev.pjc1991.commerce.member.point.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberPointUseRequest {
    private int memberId;
    private int amount;
}
