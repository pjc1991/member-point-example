package dev.pjc1991.commerce.member.point.service.impl;

import dev.pjc1991.commerce.member.point.domain.MemberPointDetail;
import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import dev.pjc1991.commerce.member.point.dto.*;
import dev.pjc1991.commerce.member.point.repository.MemberPointDetailRepository;
import dev.pjc1991.commerce.member.point.repository.MemberPointDetailRepositoryCustom;
import dev.pjc1991.commerce.member.point.repository.MemberPointEventRepositoryCustom;
import dev.pjc1991.commerce.member.point.repository.MemberPointEventRepository;
import dev.pjc1991.commerce.member.point.service.MemberPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberPointServiceImpl implements MemberPointService {

    private final MemberPointEventRepository memberPointEventRepository;
    private final MemberPointEventRepositoryCustom memberPointEventRepositoryCustom;
    private final MemberPointDetailRepository memberPointDetailRepository;
    private final MemberPointDetailRepositoryCustom memberPointDetailRepositoryCustom;


    /**
     * 회원 적립금 합계 조회
     * 적립금 합계 내역을 조회합니다.
     *
     * @param memberId 회원 아이디
     * @return 회원 적립금 합계 (int)
     */
    @Override
    @Transactional(readOnly = true)
    public int getMemberPointTotal(int memberId) {
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
    public MemberPointTotalResponse getMemberPointTotalResponse(int memberId) {
        return new MemberPointTotalResponse(memberId, getMemberPointTotal(memberId));
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
        return memberPointEventRepositoryCustom.getMemberPointEvents(search);
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
        return getMemberPointEvents(search).map(MemberPointEventResponse::new);
    }

    /**
     * 회원에게 적립금 적립
     * 적립금을 적립할 때, 적립금 적립 이벤트와 적립금 적립 상세 내역을 나눠서 저장합니다.
     *
     * @param memberPointCreate 회원 적립금 생성에 필요한 값을 담고 있는 DTO
     * @return 회원 적립금 적립 내역 (MemberPointEvent)
     */
    @Override
    public MemberPointEvent earnMemberPoint(MemberPointCreateRequest memberPointCreate) {
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
    public MemberPointEvent useMemberPoint(MemberPointUseRequest memberPointUseRequest) {
        // 현 시점에서 사용 가능한 적립금의 총액을 계산합니다.
        int memberPointTotal = memberPointDetailRepositoryCustom.getMemberPointTotal(memberPointUseRequest.getMemberId());
        // 사용하려는 적립금이 총액보다 크다면 예외를 발생시킵니다.
        if (memberPointTotal - memberPointUseRequest.getAmount() < 0) {
            throw new RuntimeException("적립금이 부족합니다.");
        }

        // 회원 적립금 사용 이벤트를 생성합니다.
        MemberPointEvent useEvent = MemberPointEvent.useMemberPoint(memberPointUseRequest);
        useEvent = memberPointEventRepository.save(useEvent);

        // 회원 적립금 상세 내역을 생성합니다.
        List<MemberPointDetail> memberPointDetails = createMemberPointDetails(memberPointUseRequest, useEvent);

        // 회원 적립금 상세 내역을 저장합니다.
        memberPointDetailRepository.saveAll(memberPointDetails);
        // 회원 적립금 상세 내역의 그룹 아이디를 업데이트합니다.
        memberPointDetails.forEach(MemberPointDetail::updateRefundGroupIdSelf);
        memberPointDetailRepository.saveAll(memberPointDetails);
        return useEvent;
    }

    /**
     * 회원 적립금 사용 (Response)
     * 적립금을 사용하고, 적립금 사용 이벤트를 DTO 형태로 반환합니다.
     *
     * @param memberPointUse (MemberPointUseRequest) memberId : 회원 아이디, amount : 적립금
     * @return 회원 적립금 사용 내역 DTO (MemberPointEventResponse)
     */
    @Override
    public MemberPointEventResponse useMemberPointResponse(MemberPointUseRequest memberPointUse) {
        return new MemberPointEventResponse(useMemberPoint(memberPointUse));
    }

    private List<MemberPointDetail> createMemberPointDetails(MemberPointUseRequest memberPointUseRequest, MemberPointEvent useEvent) {
        // 적립금 상세 조회를 위해 사용할 검색 조건입니다.
        MemberPointDetailSearch search = new MemberPointDetailSearch();
        search.setSize(100);
        search.setMemberId(memberPointUseRequest.getMemberId());

        // 사용하려는 적립금의 잔액입니다.
        int useAmountRemain = memberPointUseRequest.getAmount();

        // 생성된 적립금 상세 내역을 담을 리스트입니다.
        List<MemberPointDetail> memberPointDetails = new ArrayList<>();

        long totalCount = memberPointDetailRepository.countByMemberPointEventMemberId(memberPointUseRequest.getMemberId());

        // 잔액이 0이 될 때까지 반복합니다.
        while (useAmountRemain > 0) {
            useAmountRemain = clearMemberPointUse(useEvent, search, useAmountRemain, memberPointDetails);

            if (useAmountRemain == 0) {
                break;
            }

            if (search.getOffset() > totalCount) {
                throw new RuntimeException("비정상적으로 반복문이 진행되고 있습니다. ");
            }

        }

        return memberPointDetails;
    }

    /**
     * 회원 적립금 사용 순회
     * 현재 페이지의 적립금 상세 내역을 조회하고 사용하려는 적립금의 잔액을 차감합니다.
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
        Page<MemberPointDetailRemain> memberPointDetailAvailable = memberPointDetailRepositoryCustom.getMemberPointDetailAvailable(search);
        // 현재 페이지의 적립금 상세 내역이 없다면 예외를 발생시킵니다.
        if (memberPointDetailAvailable.getTotalElements() == 0) {
            throw new RuntimeException("적립금이 부족합니다.");
        }

        // 현재 페이지의 적립금 상세 내역을 순회하며 사용하려는 적립금의 잔액을 차감합니다.
        for (MemberPointDetailRemain memberPointDetailRemain : memberPointDetailAvailable.getContent()) {
            // 사용 금액과 적립금 상세 내역 중 작은 값으로 생성합니다.
            int useAmount = Math.min(useAmountRemain, memberPointDetailRemain.getRemain());
            MemberPointDetail current = MemberPointDetail.useMemberPointDetail(useEvent, memberPointDetailRemain, useAmount);
            memberPointDetails.add(current);
            useAmountRemain -= useAmount;

            // 잔액이 0이 되면 반복을 종료합니다.
            if (useAmountRemain == 0) break;
        }
        return useAmountRemain;
    }
}
