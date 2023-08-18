package dev.pjc1991.commerce.member.point.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberPointCreateRequest {
    private int memberId;
    private int amount;
}
