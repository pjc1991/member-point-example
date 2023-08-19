package dev.pjc1991.commerce.member.point.repository;

import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberPointEventRepository extends JpaRepository<MemberPointEvent, Long> {
}