package dev.pjc1991.commerce.member.point.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.blazebit.persistence.querydsl.JPQLNextExpressions;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQuery;
import dev.pjc1991.commerce.member.point.domain.MemberPointDetail;
import dev.pjc1991.commerce.member.point.domain.QMemberPointDetail;
import dev.pjc1991.commerce.member.point.domain.QMemberPointDetailCTE;
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
                .innerJoin(memberPointDetail.memberPointEvent)
                .select(memberPointDetail.amount.sum())
                .where(
                        memberPointDetail.memberPointEvent.memberId.eq(memberId),
                        memberPointDetail.expireAt.after(LocalDateTime.now())
                );

        /*
        SELECT
          SUM(MEMBER_POINT_DETAIL.AMOUNT)
        FROM
          MEMBER_POINT_DETAIL MEMBER_POINT_DETAIL
        INNER JOIN
          MEMBER_POINT_EVENT MEMBER_POINT_EVENT
        ON
          MEMBER_POINT_DETAIL.MEMBER_POINT_EVENT_ID = MEMBER_POINT_EVENT.ID
        WHERE
          EMBER_POINT_EVENT.MEMBER_ID = ?
        AND
          MEMBER_POINT_DETAIL.EXPIRE_AT > ?
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
        
        /*
          WITH CTE AS (
              SELECT
                MPD.ID AS ID
            FROM
                MEMBER_POINT_DETAIL MPD
            INNER JOIN
                MEMBER_POINT_EVENT MPE
            ON
                MPD.MEMBER_POINT_EVENT_ID = MPE.ID
            WHERE
                MPE.MEMBER_ID = ?
            AND
                MPD.EXPIRE_AT > ?
            AND
                MPD.AMOUNT > 0
            ORDER BY
                MPD.CREATED_AT ASC, MPD.ID ASC
            LIMIT ? OFFSET ?)
          SELECT
              MPD.MEMBER_POINT_DETAIL_GROUP_ID AS MEMBER_POINT_DETAIL_GROUP_ID,
              SUM(MPD.AMOUNT) AS REMAIN,
              MIN(MPD.EXPIRE_AT) AS EXPIRE_AT,
              MIN(MPD.CREATED_AT) AS CREATED_AT
          FROM
              MEMBER_POINT_DETAIL MPD
          INNER JOIN
              CTE
          ON
              MPD.MEMBER_POINT_DETAIL_GROUP_ID = CTE.ID
          GROUP BY
              MPD.MEMBER_POINT_DETAIL_GROUP_ID
          HAVING
              REMAIN > 0
          ORDER BY
              MPD.CREATED_AT ASC, MPD.ID ASC

          CTE 를 이용해서 적립금 상세 내역 중 만료되지 않은 적립 내역만 상세 그룹 ID를 가져옵니다.
          이 때 LIMIT 를 이용해서 한번에 조회하는 행의 수를 줄일 수 있습니다.
          CTE 와 MEMBER_POINT_DETAIL 을 MEMBER_POINT_DETAIL_GROUP_ID 로 조회해서 적립금 상세 그룹 ID 별로 적립금을 합산합니다.
          HAVING 절을 이용해서 사용할 수 없는 (잔액이 남지 않은) 적립금 상세 그룹 ID 는 제외하고,
          ORDER BY 를 이용해서 가장 오래된 적립금 상세 내역부터 조회합니다.

         */

        QMemberPointDetailCTE cte = new QMemberPointDetailCTE("memberPointDetailCTE");
        QMemberPointDetail memberPointDetail = QMemberPointDetail.memberPointDetail;
        NumberExpression<Long> memberPointDetailGroupId = memberPointDetail.memberPointDetailGroupId;

        BlazeJPAQuery<MemberPointDetailRemain> query = new BlazeJPAQuery<MemberPointDetailRemain>(entityManager, cbf)
                .with(cte, JPQLNextExpressions
                        .select(JPQLNextExpressions.bind(cte.id, memberPointDetail.id))
                        .from(memberPointDetail)
                        .innerJoin(memberPointDetail.memberPointEvent)
                        .where(
                                memberPointDetail.memberPointEvent.memberId.eq(search.getMemberId()),
                                memberPointDetail.expireAt.after(LocalDateTime.now()),
                                memberPointDetail.amount.gt(0)
                        )
                        .orderBy(memberPointDetail.createdAt.asc(), memberPointDetail.id.asc())
                        .limit(search.getSize())
                        .offset(search.getOffset()))
                .select(Projections.constructor(MemberPointDetailRemain.class,
                        memberPointDetailGroupId,
                        memberPointDetail.amount.sum(),
                        memberPointDetail.expireAt.min(),
                        memberPointDetail.createdAt.min()))
                .from(memberPointDetail)
                .innerJoin(cte).on(memberPointDetailGroupId.eq(cte.id))
                .groupBy(memberPointDetail.memberPointDetailGroupId)
                .having(memberPointDetail.amount.sum().gt(0))
                .orderBy(
                        memberPointDetail.createdAt.min().asc()
                        , memberPointDetail.memberPointDetailGroupId.asc()
                );


        return query.fetch();
    }

    /**
     * 만료된 적립금 상세 내역을 조회합니다.
     *
     * @return
     */
    public List<MemberPointDetailRemain> getMemberPointDetailExpired() {
        JPQLQuery<MemberPointDetailRemain> query = from(QMemberPointDetail.memberPointDetail)

                .innerJoin(QMemberPointDetail.memberPointDetail.memberPointEvent)

                .select(Projections.constructor(MemberPointDetailRemain.class,
                        QMemberPointDetail.memberPointDetail.memberPointDetailGroupId,
                        QMemberPointDetail.memberPointDetail.amount.sum(),
                        QMemberPointDetail.memberPointDetail.expireAt.min(),
                        QMemberPointDetail.memberPointDetail.createdAt.min()))

                .where(
                        QMemberPointDetail.memberPointDetail.expireAt.before(LocalDateTime.now())
                )

                .groupBy(QMemberPointDetail.memberPointDetail.memberPointDetailGroupId)
                .having(QMemberPointDetail.memberPointDetail.amount.sum().gt(0))
                .orderBy(
                        QMemberPointDetail.memberPointDetail.createdAt.min().asc()
                        , QMemberPointDetail.memberPointDetail.memberPointDetailGroupId.asc()
                );

        return query.fetch();
    }

    public List<MemberPointDetailRemain> getMemberPointRemains(int memberId) {
        QMemberPointDetail memberPointDetail = QMemberPointDetail.memberPointDetail;
        JPQLQuery<MemberPointDetailRemain> query =
                from(memberPointDetail)
                        .innerJoin(memberPointDetail.memberPointEvent)
                        .select(Projections.constructor(MemberPointDetailRemain.class,
                                memberPointDetail.memberPointDetailGroupId, // 회원 적립금 상세 그룹 ID
                                memberPointDetail.amount.sum(), // 회원 적립금 사용 후 잔액
                                memberPointDetail.amount.max(), // 회원 적립금 최초 적립액
                                memberPointDetail.amount.max().subtract(memberPointDetail.amount.sum()), // 회원 적립금 사용액
                                memberPointDetail.expireAt.min(),
                                memberPointDetail.createdAt.min(),
                                memberPointDetail.memberPointEvent.memberId.max() // 회원 아이디
                        ))
                        .where(
                                memberPointDetail.memberPointEvent.memberId.eq(memberId),
                                memberPointDetail.expireAt.after(LocalDateTime.now())
                        )
                        .groupBy(memberPointDetail.memberPointDetailGroupId)
                        .orderBy(
                                memberPointDetail.createdAt.min().asc()
                                , memberPointDetail.memberPointDetailGroupId.asc()
                        );

        return query.fetch();
    }
}
