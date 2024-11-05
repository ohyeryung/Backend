package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.review.ReviewInfo;
import com.manchui.domain.dto.review.ReviewScoreInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.manchui.domain.entity.QReview.review;
import static com.manchui.domain.entity.QUser.user;

public class ReviewQueryDslImpl implements ReviewQueryDsl {

    private final JPAQueryFactory queryFactory;

    public ReviewQueryDslImpl(EntityManager em) {

        this.queryFactory = new JPAQueryFactory(em);
    }

    // 점수 통계 가져오는 메서드
    public ReviewScoreInfo getScoreStatistics(Long gatheringId) {
        // 평균 점수 계산
        Double avgScore = queryFactory
                .select(review.score.avg())
                .from(review)
                .where(review.gathering.id.eq(gatheringId))
                .fetchOne();

        double averageScore = Optional.ofNullable(avgScore).orElse(0.0);

        // 각 점수별 카운트 계산
        long fiveScoreCount = Optional.ofNullable(queryFactory
                .select(review.count())
                .from(review)
                .where(review.gathering.id.eq(gatheringId).and(review.score.eq(5)))
                .fetchOne()).orElse(0L);

        long fourScoreCount = Optional.ofNullable(queryFactory
                .select(review.count())
                .from(review)
                .where(review.gathering.id.eq(gatheringId).and(review.score.eq(4)))
                .fetchOne()).orElse(0L);

        long threeScoreCount = Optional.ofNullable(queryFactory
                .select(review.count())
                .from(review)
                .where(review.gathering.id.eq(gatheringId).and(review.score.eq(3)))
                .fetchOne()).orElse(0L);

        long twoScoreCount = Optional.ofNullable(queryFactory
                .select(review.count())
                .from(review)
                .where(review.gathering.id.eq(gatheringId).and(review.score.eq(2)))
                .fetchOne()).orElse(0L);

        long oneScoreCount = Optional.ofNullable(queryFactory
                .select(review.count())
                .from(review)
                .where(review.gathering.id.eq(gatheringId).and(review.score.eq(1)))
                .fetchOne()).orElse(0L);

        return new ReviewScoreInfo(averageScore, fiveScoreCount, fourScoreCount, threeScoreCount, twoScoreCount, oneScoreCount);
    }

    // ReviewInfo 리스트를 가져오는 메서드
    @Override
    public Page<ReviewInfo> getReviewInfoList(Pageable pageable, Long gatheringId) {

        List<ReviewInfo> reviewInfoList = queryFactory
                .select(
                        Projections.constructor(
                                ReviewInfo.class,
                                user.name,
                                user.profileImagePath,
                                review.score,
                                review.comment,
                                review.createdAt,
                                review.updatedAt
                        )
                )
                .from(review)
                .leftJoin(review.user, user)
                .where(review.gathering.id.eq(gatheringId))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(review.count())
                        .from(review)
                        .where(review.gathering.id.eq(gatheringId))
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(reviewInfoList, pageable, total);
    }

}
