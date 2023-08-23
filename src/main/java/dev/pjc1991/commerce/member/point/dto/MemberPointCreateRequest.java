package dev.pjc1991.commerce.member.point.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberPointCreateRequest {
    private Long memberId;
    private Integer amount;
}
