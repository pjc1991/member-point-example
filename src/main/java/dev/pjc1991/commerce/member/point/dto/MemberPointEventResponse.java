package dev.pjc1991.commerce.member.point.dto;

import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MemberPointEventResponse {

    private Long id;
    private Integer memberId;
    private Integer amount;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;

    public MemberPointEventResponse(MemberPointEvent entity) {
        this.id = entity.getId();
        this.memberId = entity.getMemberId();
        this.amount = entity.getAmount();
        this.createdAt = entity.getCreatedAt();
        this.expireAt = entity.getExpireAt();
        this.type = entity.getType().name();
    }
}
