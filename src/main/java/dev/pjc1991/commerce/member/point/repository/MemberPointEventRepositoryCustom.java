package dev.pjc1991.commerce.member.point.repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import dev.pjc1991.commerce.dto.PageResponse;
import dev.pjc1991.commerce.member.point.domain.MemberPointDetail;
import dev.pjc1991.commerce.member.point.domain.MemberPointEvent;
import dev.pjc1991.commerce.member.point.domain.QMemberPointDetail;
import dev.pjc1991.commerce.member.point.domain.QMemberPointEvent;
import dev.pjc1991.commerce.member.point.dto.MemberPointEventSearch;
import org.springframework.data.domain.Page;
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


    /**
     * 회원 적립금 적립/사용 내역 조회
     *
     * @param search 페이지 검색을 위한 검색 조건
     * @return page 회원 적립금 적립/사용 내역
     */
    public Page<MemberPointEvent> getMemberPointEvents(MemberPointEventSearch search) {
        QMemberPointEvent memberPointEvent = QMemberPointEvent.memberPointEvent;
        // 서브쿼리에서 사용하는 테이블의 alias를 분리합니다. (그룹 함수를 사용하기 위함)
        QMemberPointEvent memberPointEvent2 = QMemberPointEvent.memberPointEvent;

        QMemberPointDetail memberPointDetail = QMemberPointDetail.memberPointDetail;

        // 회원 적립금 이벤트를 조회합니다.
        JPQLQuery<MemberPointEvent> query = from(memberPointEvent);

        // 회원 아이디로 조회합니다.
        query.where(
                QMemberPointEvent.memberPointEvent.member.id.eq(search.getMemberId()),
                QMemberPointEvent.memberPointEvent.id.notIn(
                        // 이벤트의 타입이 USE 이면서 상세 내역에 타입이 CANCEL 이 포함된 이벤트를 제외합니다.
                        JPAExpressions.select(memberPointEvent2.id)
                                .from(memberPointDetail)
                                .innerJoin(memberPointEvent2).on(memberPointDetail.memberPointEvent.id.eq(memberPointEvent2.id))
                                .where(memberPointDetail.type.eq(MemberPointDetail.MemberPointDetailType.CANCEL))
                )
        );

        // 최신순으로 정렬합니다.
        query.orderBy(
                memberPointEvent.createdAt.desc(),
                memberPointEvent.id.desc()
        );

        // 페이징 처리를 합니다.
        if (search.isPaging()) {
            query.limit(search.getSize());
            query.offset(search.getOffset());
        }

        List<MemberPointEvent> list = query.fetch();
        long totalCount = query.fetchCount();

        return new PageResponse<>(list, PageRequest.of(search.getPage(), search.getSize(), Sort.by("createdAt", "id").descending()), totalCount);
    }

}
