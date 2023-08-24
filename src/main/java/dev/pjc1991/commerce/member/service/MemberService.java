package dev.pjc1991.commerce.member.service;

import dev.pjc1991.commerce.member.domain.Member;

/**
 * 회원 서비스 인터페이스
 */
public interface MemberService {

    /**
     * 회원 엔티티 조회
     * @param memberId 회원 아이디
     * @return 회원 엔티티
     */
    Member getMemberById(long memberId);

    /**
     * 회원 엔티티 조회 (참조)
     * 참조체만 반환해서 성능을 향상시킵니다. (영속성 컨텍스트에는 없음)
     * @param memberId 회원 아이디
     * @return 회원 엔티티 (참조)
     */
    Member getMemberReferenceById(long memberId);

    /**
     * 회원 엔티티 캐시 초기화
     * @param memberId
     */
    void resetMemberCache(long memberId);
}
