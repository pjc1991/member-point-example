package dev.pjc1991.commerce.member.point.service.impl;

import dev.pjc1991.commerce.member.domain.Member;
import dev.pjc1991.commerce.member.point.domain.MemberPointDetail;
import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import dev.pjc1991.commerce.member.point.dto.*;
import dev.pjc1991.commerce.member.point.exception.*;
import dev.pjc1991.commerce.member.point.repository.MemberPointDetailRepository;
import dev.pjc1991.commerce.member.point.repository.MemberPointDetailRepositoryCustom;
import dev.pjc1991.commerce.member.point.repository.MemberPointEventRepositoryCustom;
import dev.pjc1991.commerce.member.point.repository.MemberPointEventRepository;
import dev.pjc1991.commerce.member.point.service.MemberPointService;
import dev.pjc1991.commerce.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
@Slf4j
@CacheConfig(cacheNames = "memberPoint")
public class MemberPointServiceImpl implements MemberPointService {

    private final MemberPointEventRepository memberPointEventRepository;
    private final MemberPointEventRepositoryCustom memberPointEventRepositoryCustom;
    private final MemberPointDetailRepository memberPointDetailRepository;
    private final MemberPointDetailRepositoryCustom memberPointDetailRepositoryCustom;
    private final MemberService memberService;

    private final RedissonClient redissonClient;

    private final MemberPointService self;


    /**
     * 생성자 주입 방식을 사용합니다.
     * 자가 주입을 위해 @Lazy 를 사용합니다.
     *
     * @param memberPointEventRepository        회원 적립금 이벤트 레포지토리
     * @param memberPointEventRepositoryCustom  회원 적립금 이벤트 레포지토리 커스텀 (QueryDSL)
     * @param memberPointDetailRepository       회원 적립금 상세 내역 레포지토리
     * @param memberPointDetailRepositoryCustom 회원 적립금 상세 내역 레포지토리 커스텀 (QueryDSL)
     * @param memberService                     회원 서비스
     * @param self                              자가 주입된 인스턴스
     */
    @Lazy
    public MemberPointServiceImpl(
            MemberPointEventRepository memberPointEventRepository
            , MemberPointEventRepositoryCustom memberPointEventRepositoryCustom
            , MemberPointDetailRepository memberPointDetailRepository
            , MemberPointDetailRepositoryCustom memberPointDetailRepositoryCustom
            , MemberService memberService
            , RedissonClient redissonClient
            , MemberPointService self
    ) {
        this.memberPointEventRepository = memberPointEventRepository;
        this.memberPointEventRepositoryCustom = memberPointEventRepositoryCustom;
        this.memberPointDetailRepository = memberPointDetailRepository;
        this.memberPointDetailRepositoryCustom = memberPointDetailRepositoryCustom;
        this.memberService = memberService;
        this.redissonClient = redissonClient;
        this.self = self;
    }

    /**
     * 회원 적립금 합계 조회
     * 적립금 합계 내역을 조회합니다.
     *
     * @param memberId 회원 아이디
     * @return 회원 적립금 합계 (int)
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "memberPointTotal", key = "#memberId")
    public int getMemberPointTotal(long memberId) {
        // 회원이 존재하지 않으면 회원 서비스에서 예외를 발생시킵니다.
        memberService.getMemberReferenceById(memberId);
        return memberPointDetailRepositoryCustom.getMemberPointTotal(memberId);
    }

    /**
     * 회원 적립금 합계 조회 (Response)
     * 적립금 합계 내역을 조회해서, DTO 형태로 반환합니다.
     *
     * @param memberId 회원 아이디
     * @return 회원 적립금 합계 (MemberPointTotalResponse)
     */
    @Override
    @Transactional(readOnly = true)
    public MemberPointTotalResponse getMemberPointTotalResponse(long memberId) {
        return new MemberPointTotalResponse(memberId, self.getMemberPointTotal(memberId));
    }

    /**
     * 회원 적립금 적립/사용 내역 조회
     * 적립금 적립/사용 내역을 조회합니다.
     *
     * @param search (MemberPointEventSearch) page : 페이지 번호, size : 페이지 사이즈, memberId : 회원 아이디
     * @return 회원 적립금 적립/사용 내역 (Page<MemberPointEvent>)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MemberPointEvent> getMemberPointEvents(MemberPointEventSearch search) {
        // 회원이 존재하지 않으면 회원 서비스에서 예외를 발생시킵니다.
        memberService.getMemberReferenceById(search.getMemberId());
        return memberPointEventRepositoryCustom.getMemberPointEvents(search);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberPointEvent getMemberPointEvent(long memberPointEventId) {
        return memberPointEventRepository.findById(memberPointEventId).orElseThrow(() -> new MemberPointEventNotFound("회원 적립금 이벤트가 존재하지 않습니다."));
    }

    @Override
    public MemberPointEventResponse getMemberPointEventResponse(long memberPointEventId) {
        return new MemberPointEventResponse(self.getMemberPointEvent(memberPointEventId));
    }

    /**
     * 회원 적립금 적립/사용 내역 조회 (Response)
     * 적립금 적립/사용 내역을 조회해서, DTO 형태로 반환합니다.
     *
     * @param search (MemberPointEventSearch) page : 페이지 번호, size : 페이지 사이즈, memberId : 회원 아이디
     * @return 회원 적립금 적립/사용 내역 DTO (Page<MemberPointEventResponse>)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MemberPointEventResponse> getMemberPointEventResponses(MemberPointEventSearch search) {
        return self.getMemberPointEvents(search).map(MemberPointEventResponse::new);
    }

    /**
     * 회원에게 적립금 적립
     * 적립금을 적립할 때, 적립금 적립 이벤트와 적립금 적립 상세 내역을 나눠서 저장합니다.
     *
     * @param memberPointCreate 회원 적립금 생성에 필요한 값을 담고 있는 DTO
     * @return 회원 적립금 적립 내역 (MemberPointEvent)
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "memberPointTotal", key = "#memberPointCreate.memberId")
    })
    public MemberPointEvent earnMemberPoint(MemberPointCreateRequest memberPointCreate) {
        // 회원을 조회합니다.
        Member member = memberService.getMemberReferenceById(memberPointCreate.getMemberId());
        memberPointCreate.setOwner(member);

        // 회원 적립금 이벤트를 생성합니다.
        MemberPointEvent event = MemberPointEvent.earnMemberPoint(memberPointCreate);
        event = memberPointEventRepository.save(event);

        // 회원 적립금 상세 내역을 생성합니다.
        MemberPointDetail detail = MemberPointDetail.earnMemberPointDetail(event);
        memberPointDetailRepository.save(detail);

        // 회원 적립금 상세 내역의 그룹 아이디를 업데이트합니다.
        detail.updateGroupIdSelf();
        memberPointDetailRepository.save(detail);
        return event;
    }

    /**
     * 회원 적립금 적립 (Response)
     * 적립금을 적립하고, 적립금 적립 이벤트를 DTO 형태로 반환합니다.
     *
     * @param memberPointCreate (MemberPointCreateRequest) memberId : 회원 아이디, amount : 적립금
     * @return 회원 적립금 적립 DTO (MemberPointEventResponse)
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "memberPointTotal", key = "#memberPointCreate.memberId")
    })
    public MemberPointEventResponse earnMemberPointResponse(MemberPointCreateRequest memberPointCreate) {
        return new MemberPointEventResponse(earnMemberPoint(memberPointCreate));
    }

    /**
     * 회원 적립금 사용
     * 적립금을 사용할 때, 적립금 사용 이벤트와 적립금 상세 내역을 나눠서 저장합니다.
     * 거정 먼저 적립된 적립금부터 사용하기 위해서 적립금 상세 내역을 조회하고, 적립금 상세 내역을 순회하며 사용하려는 적립금의 잔액을 차감합니다.
     *
     * @param memberPointUseRequest (MemberPointUseRequest) memberId : 회원 아이디, amount : 적립금
     * @return 회원 적립금 사용 내역 (MemberPointEvent)
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "memberPointTotal", key = "#memberPointUseRequest.memberId")
    })
    public MemberPointEvent useMemberPoint(MemberPointUseRequest memberPointUseRequest) {
        // 회원을 조회합니다.
        Member member = memberService.getMemberReferenceById(memberPointUseRequest.getMemberId());
        memberPointUseRequest.setOwner(member);

        // 포인트 사용은 동시성 문제를 일으킬 수 있으므로 Redisson 을 사용해서 Lock 을 걸어줍니다.
        String lockKey = "memberPointUseLock#" + memberPointUseRequest.getMemberId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(2, 5, java.util.concurrent.TimeUnit.SECONDS)) {
                throw new MemberPointConcurrentException("동시에 적립금 사용 요청이 들어왔습니다. 잠시 후 다시 시도해주세요.");
            }
            // 현 시점에서 사용 가능한 적립금의 총액을 계산합니다.
            int memberPointTotal = self.getMemberPointTotal(memberPointUseRequest.getMemberId());
            // 사용하려는 적립금이 총액보다 크다면 예외를 발생시킵니다.
            if (memberPointTotal - memberPointUseRequest.getAmount() < 0) {
                throw new NotEnoughPointException("적립금이 부족합니다.");
            }

            // 회원 적립금 사용 이벤트를 생성합니다.
            MemberPointEvent useEvent = MemberPointEvent.useMemberPoint(memberPointUseRequest);
            useEvent = memberPointEventRepository.save(useEvent);

            // 회원 적립금 상세 내역을 생성합니다.
            List<MemberPointDetail> memberPointDetails = createMemberPointDetailUse(memberPointUseRequest, useEvent);

            // 회원 적립금 상세 내역을 저장합니다.
            memberPointDetailRepository.saveAll(memberPointDetails);
            // 회원 적립금 상세 내역의 그룹 아이디를 업데이트합니다.
            memberPointDetails.forEach(MemberPointDetail::updateRefundGroupIdSelf);
            memberPointDetailRepository.saveAll(memberPointDetails);

            return useEvent;

        } catch (InterruptedException e) {
            throw new MemberPointConcurrentException("동시에 적립금 사용 요청이 들어왔습니다. 잠시 후 다시 시도해주세요.");
        } finally {
            lock.unlock();
        }

    }

    /**
     * 회원 적립금 사용 (Response)
     * 적립금을 사용하고, 적립금 사용 이벤트를 DTO 형태로 반환합니다.
     *
     * @param memberPointUse (MemberPointUseRequest) memberId : 회원 아이디, amount : 적립금
     * @return 회원 적립금 사용 내역 DTO (MemberPointEventResponse)
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "memberPointTotal", key = "#memberPointUse.memberId")
    })
    public MemberPointEventResponse useMemberPointResponse(MemberPointUseRequest memberPointUse) {
        return new MemberPointEventResponse(useMemberPoint(memberPointUse));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "memberPointTotal", key = "#memberPointUse.memberId")
    })
    public MemberPointEvent rollbackMemberPointUse(long memberPointEventId) {
        // 회원 적립금 이벤트를 조회합니다.
        MemberPointEvent event = memberPointEventRepository.findById(memberPointEventId).orElseThrow(() -> new MemberPointEventNotFound("회원 적립금 이벤트가 존재하지 않습니다."));

        // 회원 적립금 이벤트의 타입이 사용이 아니면 예외를 발생시킵니다.
        if (event.getType() != MemberPointEvent.MemberPointEventType.USE) {
            throw new BadMemberPointTypeException("회원 적립금 이벤트의 타입이 사용이 아닙니다.");
        }

        // 회원 적립금 이벤트의 상세 내역을 합산합니다.
        int usedAmount = event.getMemberPointDetails().stream().mapToInt(MemberPointDetail::getAmount).sum();

        // 회원 적립금 이벤트의 상세 내역의 합산 금액이 0 이라면 이미 사용 취소된 이벤트입니다.
        if (usedAmount == 0) {
            throw new MemberPointAlreadyRollbackedException("이미 롤백된 회원 적립금 이벤트입니다.");
        }

        // 회원 적립금 이벤트의 상세 내역의 합산 금액이 0 보다 크다면 데이터 처리가 잘못된 것입니다.
        if (usedAmount > 0) {
            throw new MemberPointAmountBrokenException("회원 적립금 이벤트의 상세 내역의 합산 금액이 0 보다 큽니다.");
        }


        // 회원 적립금 이벤트의 상세 내역을 조회해, 적립금 사용 내역을 순회하며 롤백 상세 내역을 생성합니다.
        // 롤백 상세 내역은 적립금 사용 내역의 반대로 생성하며, 적립금 사용 이벤트의 상세 내역 그룹 아이디를 참조합니다.
        List<MemberPointDetail> rollbacks = event.getMemberPointDetails().stream().map(detail -> MemberPointDetail.rollbackMemberPointDetail(detail, event)).toList();
        memberPointDetailRepository.saveAll(rollbacks);

        return event;
    }

    /**
     * 회원 적립금 사용 취소
     * 사용된 적립금 사용을 취소합니다. 환불이 아니고 롤백이기 때문에, MemberPointEvent 의 상세 내역을 삭제하지 않습니다.
     *
     * @param memberPointEventId
     * @return 회원 적립금 사용 이벤트
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "memberPointTotal", key = "#memberId")
    })
    public MemberPointEventResponse rollbackMemberPointUseResponse(long memberId, long memberPointEventId) {
        return new MemberPointEventResponse(rollbackMemberPointUse(memberPointEventId));

    }

    /**
     * 회원 적립금 합계의 캐시를 초기화합니다.
     *
     * @param memberId 회원 아이디
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "memberPointTotal", key = "#memberId")
    })
    public void clearMemberPointTotalCache(long memberId) {
        // 적립금 합계의 캐시를 초기화합니다. CacheEvict 어노테이션을 사용하므로 별도의 코드가 필요하지 않습니다.
    }

    /**
     * 회원 적립금 만료 처리
     * 적립금 만료 시점을 지난 적립금 상세 내역을 조회하고, 적립금 만료 이벤트와 적립금 만료 상세 내역을 생성합니다.
     */
    @Override
    public void expireMemberPoint() {
        // 적립금 만료 시점을 지난 적립금 상세 내역을 조회합니다.
        List<MemberPointDetailRemain> memberPointDetails = memberPointDetailRepositoryCustom.getMemberPointDetailExpired();
        log.info("만료 예정 적립금 상세 내역 {}건을 확인했습니다. ", memberPointDetails.size());
        // 적립금 상세 내역을 순회하며 적립금 만료 이벤트를 생성합니다.
        for (MemberPointDetailRemain memberPointDetailRemain : memberPointDetails) {
            log.info("상세 내역 아이디 : {}, 만료 금액 : {}", memberPointDetailRemain.getMemberPointDetailGroupId(), memberPointDetailRemain.getRemain());
            // 회원 인스턴스가 필요하므로 리퍼런스 인스턴스를 생성합니다.
            Member owner = memberService.getMemberReferenceById(memberPointDetailRemain.getMemberId());
            memberPointDetailRemain.setOwner(owner);

            // 적립금 만료 이벤트를 생성합니다.
            MemberPointEvent expireEvent = MemberPointEvent.expireMemberPoint(memberPointDetailRemain);
            memberPointEventRepository.save(expireEvent);

            MemberPointDetail expireDetail = MemberPointDetail.expireMemberPointDetail(memberPointDetailRemain, expireEvent);
            memberPointDetailRepository.save(expireDetail);

            // 회원 적립금 상세 내역의 환불 그룹 아이디를 업데이트합니다.
            expireDetail.updateRefundGroupIdSelf();
            memberPointDetailRepository.save(expireDetail);

            // 만료된 적립금 회원의 캐싱을 초기화합니다.
            self.clearMemberPointTotalCache(memberPointDetailRemain.getMemberId());
        }
    }

    /**
     * 회원 적립금 만료 시점 변경 (테스트 전용)
     * 테스트 코드에서만 사용합니다.
     *
     * @param memberPointEventId 회원 적립금 이벤트 아이디
     * @param expireAt           변경할 만료 시점
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "memberPointTotal", allEntries = true)
    })
    public void changeExpireAt(long memberPointEventId, LocalDateTime expireAt, LocalDateTime createdAt) {
        // 테스트 전용 코드입니다.
        log.warn("회원 적립금 이벤트의 만료 시점을 변경합니다. 이 메소드는 테스트 코드에서만 사용합니다. 변경할 만료 시점: {}", expireAt);

        // 회원 적립금 이벤트를 조회합니다.
        MemberPointEvent memberPointEvent = memberPointEventRepository.findById(memberPointEventId).orElseThrow(() -> new MemberPointEventNotFound("회원 적립금 이벤트가 존재하지 않습니다."));
        if (memberPointEvent.getType() != MemberPointEvent.MemberPointEventType.EARN) {
            throw new BadMemberPointTypeException("회원 적립금 이벤트의 타입이 적립이 아닙니다.");
        }

        // 만료 시점을 변경합니다.
        memberPointEvent.setExpireAt(expireAt, createdAt);

        // 회원 적립금 이벤트의 상세 내역 그룹 아이디를 찾습니다.
        // type 이 EARN 인 이벤트는 상세 내역 그룹이 하나만 존재합니다.
        Long memberPointDetailGroupId = memberPointEvent.getMemberPointDetails().stream().filter(memberPointDetail -> memberPointDetail.getType() == MemberPointDetail.MemberPointDetailType.EARN)
                .findFirst().orElseThrow(() -> new MemberPointDetailNotFoundException("회원 적립금 상세 내역이 존재하지 않습니다.")).getId();

        // 회원 적립금 이벤트의 상세 내역 그룹을 모두 조회합니다.
        List<MemberPointDetail> memberPointDetailGroups = memberPointDetailRepository.findByMemberPointDetailGroupId(memberPointDetailGroupId);

        // 회원 적립금 이벤트의 상세 내역 그룹을 순회하며 만료 시점을 변경합니다.
        List<MemberPointEvent> memberPointDetailGroupEvents = memberPointDetailGroups.stream().map(MemberPointDetail::getMemberPointEvent).toList();

        // 만료 시점을 변경합니다.
        memberPointDetailGroupEvents.forEach(memberPointEvent1 -> memberPointEvent1.setExpireAt(expireAt, createdAt));
    }

    /**
     * 회원 적립금 일치성 검사 (테스트)
     * 해당 회원의 적립금이 선입선출 형태로 사용되었는지 확인합니다.
     * 만약, 적립금이 선입선출 형태로 사용되지 않았다면 예외를 발생시킵니다.
     *
     * @param memberId 회원 아이디
     */
    @Override
    @Transactional(readOnly = true)
    @Caching(evict = {
            @CacheEvict(value = "memberPointTotal", key = "#memberId")
    })
    public void checkMemberPoint(long memberId) {
        // 해당 회원의 적립금 상세 그룹을 조회합니다.
        List<MemberPointDetailRemain> memberPointDetails = memberPointDetailRepositoryCustom.getMemberPointRemains(memberId);
        if (memberPointDetails.isEmpty()) {
            log.info("적립금이 없습니다.");
            return;
        }

        // 사용 금액이 0이 아닌 가장 최신 적립금 상세 그룹을 찾습니다.
        MemberPointDetailRemain LatestUsed = memberPointDetails.get(0);

        for (MemberPointDetailRemain row : memberPointDetails) {
            if (row.getUsed() != 0 && row.getCreatedAt().isAfter(LatestUsed.getCreatedAt())) {
                LatestUsed = row;
            }
        }

        // 목록을 순회하면서, 선입선출 위반사항이 있는지 확인합니다.
        for (MemberPointDetailRemain row : memberPointDetails) {
            // 잔액이 0 보다 작다면 예외를 발생시킵니다.
            if (row.getRemain() < 0) {
                throw new MemberPointAmountBrokenException("적립금 상세 내역의 잔액이 0 보다 작습니다.");
            }
            // 만약 가장 최신 적립금 상세 그룹의 생성 시점보다 이전에 생성된 적립금 상세 그룹이고, 잔액이 0이 아니라면 예외를 발생시킵니다.
            if (row.getCreatedAt().isBefore(LatestUsed.getCreatedAt()) && row.getRemain() != 0) {
                log.error("적립금이 선입선출 형태로 사용되지 않았습니다.");
                log.error("row.getMemberPointDetailGroupId() : {}", row.getMemberPointDetailGroupId());
                log.error("row.getCreatedAt() : {}", row.getCreatedAt());
                log.error("row.getRemain() : {}", row.getRemain());
                log.error("LatestUsed.getMemberPointDetailGroupId() : {}", LatestUsed.getMemberPointDetailGroupId());
                log.error("LatestUsed.getCreatedAt() : {}", LatestUsed.getCreatedAt());

                throw new MemberPointNoFirstInFirstOutException("적립금이 선입선출 형태로 사용되지 않았습니다.");
            }
        }
        log.info("적립금이 선입선출 형태로 사용되었습니다.");

    }

    /**
     * 회원 적립금 사용 상세 내역 생성
     * 적립금을 사용할 떄, 적립금 상세 내역을 생성합니다.
     * 적립된 적립금을 선입선출로 사용해야 합니다.
     *
     * @param memberPointUseRequest 적립금 사용 요청
     * @param useEvent              적립금 사용 이벤트
     * @return 생성된 적립금 상세 내역 리스트
     */
    private List<MemberPointDetail> createMemberPointDetailUse(MemberPointUseRequest memberPointUseRequest, MemberPointEvent useEvent) {
        // 적립금 상세 조회를 위해 사용할 검색 조건입니다.
        MemberPointDetailSearch search = new MemberPointDetailSearch();
        search.setSize(100);
        search.setMemberId(memberPointUseRequest.getMemberId());

        // 사용하려는 적립금의 잔액입니다.
        int useAmountRemain = memberPointUseRequest.getAmount();

        // 생성된 적립금 상세 내역을 담을 리스트입니다.
        List<MemberPointDetail> memberPointDetails = new ArrayList<>();

        // 무한 루프를 방지하기 위해 적립금 상세 내역의 총 개수를 조회합니다.
        long totalCount = memberPointDetailRepository.countByMemberPointEventMemberId(memberPointUseRequest.getMemberId());

        // 잔액이 0이 될 때까지 반복합니다.
        while (useAmountRemain > 0) {
            // 현재 페이지의 적립금 상세 내역을 조회하고, 사용하려는 적립금의 잔액을 차감합니다.
            useAmountRemain = clearMemberPointUse(useEvent, search, useAmountRemain, memberPointDetails);

            // 잔액이 0이 되면 반복을 종료합니다.
            if (useAmountRemain == 0) {
                break;
            }

            // 오류로 인해 무한 루프하는 것을 방지합니다.
            if (search.getOffset() > totalCount) {
                throw new MemberPointUseInfiniteLoopException("비정상적으로 반복문이 진행되고 있습니다. ");
            }

        }

        return memberPointDetails;
    }

    /**
     * 회원 적립금 사용 순회
     * 현재 페이지의 적립금 상세 내역을 조회하고 사용하려는 적립금의 잔액을 차감하는 유틸리티 메소드입니다.
     * 잔액이 0이 될 때까지 이 메소드를 반복합니다.
     *
     * @param useEvent           적립금 사용 이벤트
     * @param search             적립금 상세 내역 조회를 위한 검색 조건
     * @param useAmountRemain    사용하려는 적립금의 잔액 (매 순회마다 차감됩니다.)
     * @param memberPointDetails 생성된 적립금 상세 내역을 담은 리스트
     * @return useAmountRemain
     * 사용하려는 적립금의 잔액
     */
    private int clearMemberPointUse(MemberPointEvent useEvent, MemberPointDetailSearch search, int useAmountRemain, List<MemberPointDetail> memberPointDetails) {
        // 현재 페이지의 적립금 상세 내역을 조회합니다.
        List<MemberPointDetailRemain> memberPointDetailAvailable = memberPointDetailRepositoryCustom.getMemberPointDetailAvailable(search);

        // 현재 페이지에 적립금 상세 내역이 없다면 다음 페이지를 검색합니다.
        if (memberPointDetailAvailable.isEmpty()) {
            search.setPage(search.getPage() + 1);
            return useAmountRemain;
        }

        // 현재 페이지의 적립금 상세 내역을 순회하며 사용하려는 적립금의 잔액을 차감합니다.
        for (MemberPointDetailRemain memberPointDetailRemain : memberPointDetailAvailable) {
            // 사용 금액과 적립금 상세 내역 중 작은 값으로 생성합니다.
            int useAmount = Math.min(useAmountRemain, memberPointDetailRemain.getRemain());

            // 적립금 상세 내역을 생성합니다.
            MemberPointDetail current = MemberPointDetail.useMemberPointDetail(useEvent, memberPointDetailRemain, useAmount);
            memberPointDetails.add(current);

            // 사용하려는 적립금의 잔액을 차감합니다.
            useAmountRemain -= useAmount;

            // 잔액이 0이 되면 반복을 종료합니다.
            if (useAmountRemain == 0) break;
        }
        // 다음 페이지를 검색합니다.
        search.setPage(search.getPage() + 1);
        return useAmountRemain;
    }
}
