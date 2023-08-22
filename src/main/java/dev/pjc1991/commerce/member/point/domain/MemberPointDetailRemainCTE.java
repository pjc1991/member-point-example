package dev.pjc1991.commerce.member.point.domain;

import com.blazebit.persistence.CTE;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@CTE
@Entity
@Getter
@Setter
public class MemberPointDetailRemainCTE {

    @Id
    @Column(name = "MEMBER_POINT_DETAIL_GROUP_ID")
    private Long id;

    @Column(name = "REMAIN")
    private Integer remain;

    @Column(name = "EXPIRE_AT")
    private LocalDateTime expireAt;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

}
