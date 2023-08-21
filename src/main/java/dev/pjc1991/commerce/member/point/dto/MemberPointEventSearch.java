package dev.pjc1991.commerce.member.point.dto;

import dev.pjc1991.commerce.dto.PageSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberPointEventSearch extends PageSearch {

    // 회원 아이디
    private Integer memberId;
}
