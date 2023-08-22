package dev.pjc1991.commerce.member.point.repository;

import dev.pjc1991.commerce.member.point.domain.MemberPointDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberPointDetailRepository extends JpaRepository<MemberPointDetail, Long> {
    List<MemberPointDetail> findByMemberPointDetailGroupId(Long memberPointDetailGroupId);
    long countByMemberPointEventMemberId(Integer memberId);
}