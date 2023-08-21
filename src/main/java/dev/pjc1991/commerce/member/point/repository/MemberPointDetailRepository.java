package dev.pjc1991.commerce.member.point.repository;

import dev.pjc1991.commerce.member.point.domain.MemberPointDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberPointDetailRepository extends JpaRepository<MemberPointDetail, Long> {
    long countByMemberPointEventMemberId(Integer memberId);
}