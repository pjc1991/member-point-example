package dev.pjc1991.commerce.member.point.repository;

import com.querydsl.jpa.JPQLQuery;
import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import dev.pjc1991.commerce.member.point.domain.QMemberPointEvent;
import dev.pjc1991.commerce.member.point.dto.MemberPointEventSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MemberPointEventRepositoryCustom extends QuerydslRepositorySupport {

    public MemberPointEventRepositoryCustom() {
        super(MemberPointEvent.class);
    }


    public Page<MemberPointEvent> getMemberPointEvents(MemberPointEventSearch search) {
        QMemberPointEvent memberPointEvent = QMemberPointEvent.memberPointEvent;

        // 회원 적립금 이벤트를 조회합니다.
        JPQLQuery<MemberPointEvent> query = from(memberPointEvent);

        // 회원 아이디로 조회합니다.
        query.where(QMemberPointEvent.memberPointEvent.memberId.eq(search.getMemberId()));

        // 최신순으로 정렬합니다.
        query.orderBy(
                memberPointEvent.createdAt.desc(),
                memberPointEvent.id.desc()
        );

        // 페이징 처리를 합니다.
        query.limit(search.getSize());
        query.offset(search.getOffset());

        List<MemberPointEvent> list = query.fetch();
        long totalCount = query.fetchCount();

        return new PageImpl<>(list, PageRequest.of(search.getPage(), search.getSize(), Sort.by("createdAt", "id").descending()), totalCount);
    }

}
