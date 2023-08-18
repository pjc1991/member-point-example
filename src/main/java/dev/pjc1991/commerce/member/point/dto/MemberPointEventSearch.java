package dev.pjc1991.commerce.member.point.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberPointEventSearch {
    private int memberId;
    private int page;
    private int size;
    private int total;
    private int offset;
}
