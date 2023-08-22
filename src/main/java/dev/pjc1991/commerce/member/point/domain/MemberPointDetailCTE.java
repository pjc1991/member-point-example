package dev.pjc1991.commerce.member.point.domain;

import com.blazebit.persistence.CTE;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@CTE
@Entity
public class MemberPointDetailCTE {

    @Id
    private Long id;
}
