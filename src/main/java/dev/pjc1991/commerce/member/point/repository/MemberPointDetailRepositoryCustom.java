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
        // 적립금 총합을 계산하기 위해 회원 상세 내역에서 그룹 ID 기준으로 그룹 쿼리를 실행합니다.
        QMemberPointDetail memberPointDetail = QMemberPointDetail.memberPointDetail;

        JPQLQuery<Integer> query = from(memberPointDetail)
                .select(memberPointDetail.amount.sum());

        query.where(memberPointDetail.memberPointEvent.memberId.eq(memberId));
        query.having(memberPointDetail.amount.sum().gt(0));
        query.groupBy(memberPointDetail.memberPointDetailUseId);

        /*
         * SELECT
         *     SUM(MEMBER_POINT_DETAIL.AMOUNT)
         * FROM
         *     MEMBER_POINT_DETAIL MEMBER_POINT_DETAIL
         * INNER JOIN
         *     MEMBER_POINT_EVENT MEMBER_POINT_EVENT
         * WHERE
         *     MEMBER_POINT_EVENT.MEMBER_ID = ?
         * GROUP BY
         *    MEMBER_POINT_DETAIL.MEMBER_POINT_DETAIL_USE_ID
         */

        Integer result = query.fetchOne();

        if (result == null) {
            return 0;
        }

        return result;
    }
}
