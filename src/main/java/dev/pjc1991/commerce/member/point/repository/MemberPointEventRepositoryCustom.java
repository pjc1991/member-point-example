package dev.pjc1991.commerce.member.point.repository;

import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

@Repository
public class MemberPointEventRepositoryCustom extends QuerydslRepositorySupport {

    public MemberPointEventRepositoryCustom() {
        super(MemberPointEvent.class);
    }


}
