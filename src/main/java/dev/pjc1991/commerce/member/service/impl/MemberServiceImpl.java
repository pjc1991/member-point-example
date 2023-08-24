package dev.pjc1991.commerce.member.service.impl;

import dev.pjc1991.commerce.member.domain.Member;
import dev.pjc1991.commerce.member.exception.MemberNotFoundException;
import dev.pjc1991.commerce.member.repository.MemberRepository;
import dev.pjc1991.commerce.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 회원 서비스 구현체
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 엔티티 조회
     * @param memberId 회원 아이디
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    @Caching(cacheable = {
            @Cacheable(value = "member", key = "#memberId", unless = "#result == null")
    })
    public Member getMemberById(long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다."));
    }

    /**
     * 회원 엔티티 조회 (참조)
     * @param memberId 회원 아이디
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    @Caching(cacheable = {
            @Cacheable(value = "memberReference", key = "#memberId", unless = "#result == null")
    })
    public Member getMemberReferenceById(long memberId) {
        Optional<Member> optional = memberRepository.findById(memberId);
        if(optional.isEmpty()) {
            throw new MemberNotFoundException("회원을 찾을 수 없습니다.");
        }

        return memberRepository.getReferenceById(memberId);

    }

    /**
     * 회원 엔티티 캐시 초기화
     * 캐시 초기화는 어노테이션에 의해 이루어집니다.
     * @param memberId 회원 아이디
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "member", key = "#memberId"),
            @CacheEvict(value = "memberReference", key = "#memberId")
    })
    public void resetMemberCache(long memberId) {
        // 이 함수 자체는 아무 일도 하지 않습니다.
    }
}
