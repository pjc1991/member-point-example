package dev.pjc1991.commerce.member.point.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.blazebit.persistence.querydsl.JPQLNextExpressions;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import dev.pjc1991.commerce.member.point.domain.MemberPointDetail;
import dev.pjc1991.commerce.member.point.domain.QMemberPointDetail;
import dev.pjc1991.commerce.member.point.domain.QMemberPointDetailRemainCTE;
import dev.pjc1991.commerce.member.point.dto.MemberPointDetailRemain;
import dev.pjc1991.commerce.member.point.dto.MemberPointDetailSearch;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Slf4j
public class MemberPointDetailRepositoryCustom extends QuerydslRepositorySupport {

    private final EntityManager entityManager;
    private final CriteriaBuilderFactory cbf;

    public MemberPointDetailRepositoryCustom(
            EntityManager entityManager
            , CriteriaBuilderFactory cbf
    ) {
        super(MemberPointDetail.class);
        this.entityManager = entityManager;
        this.cbf = cbf;
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

        query.where(
                memberPointDetail.memberPointEvent.memberId.eq(memberId)
                , memberPointDetail.expireAt.after(LocalDateTime.now())
        );

        /*
         * SELECT
         *     SUM(MEMBER_POINT_DETAIL.AMOUNT)
         * FROM
         *     MEMBER_POINT_DETAIL MEMBER_POINT_DETAIL
         * INNER JOIN
         *     MEMBER_POINT_EVENT MEMBER_POINT_EVENT
         * ON
         *     MEMBER_POINT_DETAIL.MEMBER_POINT_EVENT_ID = MEMBER_POINT_EVENT.ID
         * WHERE
         *     MEMBER_POINT_EVENT.MEMBER_ID = ?
         */

        Integer result = query.fetchOne();

        if (result == null) {
            return 0;
        }

        return result;
    }

    /**
     * 사용할 수 있는 가장 오래된 적립금 상세 내역부터 조회합니다.
     *
     * @param search 회원 적립금 상세 내역 조회 파라메터를 담은 오브젝트입니다.
     * @return 회원 적립금 상세 내역을 담은 페이지 오브젝트입니다.
     */
    public List<MemberPointDetailRemain> getMemberPointDetailAvailable(MemberPointDetailSearch search) {
        // 적립금 상세 내역에서 회원 아이디로 조회합니다.
        // 회원 아이디가 없으면 예외를 발생시킵니다.
        if (search.getMemberId() == null) {
            throw new IllegalArgumentException("memberId 가 null 입니다.");
        }

        QMemberPointDetail memberPointDetail = QMemberPointDetail.memberPointDetail;
        DateTimePath<LocalDateTime> createdAt = Expressions.dateTimePath(LocalDateTime.class, "createdAt");

        /*
         * SELECT
         *   MEMBER_POINT_DETAIL.MEMBER_POINT_DETAIL_GROUP_ID AS MEMBER_POINT_DETAIL_GROUP_ID,
         *   SUM(MEMBER_POINT_DETAIL.AMOUNT) AS REMAIN,
         *   MIN(MEMBER_POINT_DETAIL.CREATED_AT) AS CREATED_AT
         * FROM
         *   MEMBER_POINT_DETAIL MEMBER_POINT_DETAIL
         * INNER JOIN
         *   MEMBER_POINT_EVENT MEMBER_POINT_EVENT
         * ON
         *   MEMBER_POINT_DETAIL.MEMBER_POINT_EVENT_ID = MEMBER_POINT_EVENT.ID
         * WHERE
         *   MEMBER_POINT_EVENT.MEMBER_ID = ?
         * AND
         *   MEMBER_POINT_EVENT.EXPIRE_AT > ?
         * GROUP BY
         *   MEMBER_POINT_DETAIL.MEMBER_POINT_DETAIL_GROUP_ID
         * ORDER BY
         *   CREATED_AT ASC
         * LIMIT ?, ?
         */

        QMemberPointDetailRemainCTE cte = new QMemberPointDetailRemainCTE("mpdr");
        BlazeJPAQuery<MemberPointDetailRemain> query = new BlazeJPAQuery<>(entityManager, cbf)
                .with(cte, JPQLNextExpressions.select(
                        JPQLNextExpressions.bind(cte.id, memberPointDetail.memberPointDetailGroupId),
                        JPQLNextExpressions.bind(cte.remain, memberPointDetail.amount.sum()),
                        JPQLNextExpressions.bind(cte.createdAt, memberPointDetail.createdAt.min()),
                        JPQLNextExpressions.bind(cte.expireAt, memberPointDetail.expireAt.min()))
                        .from(memberPointDetail)
                        .where(memberPointDetail.memberPointEvent.memberId.eq(search.getMemberId()),
                                memberPointDetail.expireAt.after(LocalDateTime.now())))
                .select(Projections.constructor(MemberPointDetailRemain.class,
                        cte.id,
                        cte.remain,
                        cte.createdAt,
                        cte.expireAt))
                .from(cte);

        List<MemberPointDetailRemain> result = query.fetch();
        result.forEach(row -> log.info("row: {}", row));
        return result;
    }

}
