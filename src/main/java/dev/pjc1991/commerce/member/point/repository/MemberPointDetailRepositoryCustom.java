package dev.pjc1991.commerce.member.point.repository;

import com.querydsl.jpa.JPQLQuery;
import dev.pjc1991.commerce.member.point.domain.MemberPointDetail;
import dev.pjc1991.commerce.member.point.domain.QMemberPointDetail;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

@Repository
public class MemberPointDetailRepositoryCustom extends QuerydslRepositorySupport {

    public MemberPointDetailRepositoryCustom() {
        super(MemberPointDetail.class);
    }

    /**
     * 회원의 적립금 총합을 조회합니다.
     *
     * @param memberId 회원 아이디
     * @return 회원의 적립금 총합
     */
    public int getMemberPointTotal(int memberId) {
        // 적립금 총합을 계산하기 위해 적립금 상세 내역에서 회원 아이디로 조회합니다.
        QMemberPointDetail memberPointDetail = QMemberPointDetail.memberPointDetail;

        JPQLQuery<Integer> query = from(memberPointDetail)
                .select(memberPointDetail.amount.sum());

        query.where(memberPointDetail.memberPointEvent.memberId.eq(memberId));

        /*
         * SELECT
         *     SUM(MEMBER_POINT_DETAIL.AMOUNT)
         * FROM
         *     MEMBER_POINT_DETAIL MEMBER_POINT_DETAIL
         * INNER JOIN
         *     MEMBER_POINT_EVENT MEMBER_POINT_EVENT
         * WHERE
         *     MEMBER_POINT_EVENT.MEMBER_ID = ?
         */

        Integer result = query.fetchOne();

        if (result == null) {
            return 0;
        }

        return result;
    }
}
