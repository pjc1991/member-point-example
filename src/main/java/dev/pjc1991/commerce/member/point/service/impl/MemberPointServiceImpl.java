package dev.pjc1991.commerce.member.point.service.impl;
import dev.pjc1991.commerce.member.point.domain.MemberPointDetail;
import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import dev.pjc1991.commerce.member.point.dto.MemberPointCreateRequest;
import dev.pjc1991.commerce.member.point.dto.MemberPointEventSearch;
import dev.pjc1991.commerce.member.point.dto.MemberPointUseRequest;
import dev.pjc1991.commerce.member.point.repository.MemberPointDetailRepository;
import dev.pjc1991.commerce.member.point.repository.MemberPointEventRepository;
import dev.pjc1991.commerce.member.point.service.MemberPointService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberPointServiceImpl implements MemberPointService {

    private final MemberPointEventRepository memberPointEventRepository;
    private final MemberPointDetailRepository memberPointDetailRepository;


    @Override
    public int getMemberPointTotal(int memberId) {
        return 0;
    }

    @Override
    public Page<MemberPointEvent> getMemberPointEvents(MemberPointEventSearch search) {
        return null;
    }

    /**
     * 회원에게 적립금을 적립합니다.
     *
     * @param memberPointCreate 회원 적립금 생성에 필요한 값을 담고 있는 DTO
     * @return 회원 적립금 이벤트
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

    @Override
    public MemberPointEvent useMemberPoint(MemberPointUseRequest memberPointUse) {
        return null;
    }
}
