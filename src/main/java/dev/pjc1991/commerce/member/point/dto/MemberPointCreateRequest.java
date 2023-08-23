package dev.pjc1991.commerce.member.point.dto;

import dev.pjc1991.commerce.member.domain.Member;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberPointCreateRequest {
    private Long memberId;
    private Integer amount;
    private Member owner;
}
