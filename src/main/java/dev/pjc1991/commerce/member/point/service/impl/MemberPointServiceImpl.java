package dev.pjc1991.commerce.member.point.service.impl;
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

    @Override
    public MemberPointEvent earnMemberPoint(MemberPointCreateRequest memberPointCreate) {
        return null;
    }

    @Override
    public MemberPointEvent useMemberPoint(MemberPointUseRequest memberPointUse) {
        return null;
    }
}
