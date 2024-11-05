package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.review.ReviewDetailInfo;
import com.manchui.domain.dto.review.ReviewInfo;
import com.manchui.domain.dto.review.ReviewScoreInfo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.manchui.domain.entity.QGathering.gathering;
import static com.manchui.domain.entity.QImage.image;
import static com.manchui.domain.entity.QReview.review;
import static com.manchui.domain.entity.QUser.user;
import static com.querydsl.jpa.JPAExpressions.select;

@Slf4j
public class ReviewQueryDslImpl implements ReviewQueryDsl {

    private final JPAQueryFactory queryFactory;

    public ReviewQueryDslImpl(EntityManager em) {

        this.queryFactory = new JPAQueryFactory(em);
    }

    // 점수 통계 가져오는 메서드
    public ReviewScoreInfo getScoreStatisticsByGathering(Long gatheringId) {

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

    // 전체 후기 조회 시 점수 통계 가져오는 메서드
    @Override
    public ReviewScoreInfo getScoreStatistics(String query, String location, String category, String startDate, String endDate) {

        BooleanBuilder builder = new BooleanBuilder();

        // 조건에 맞는 필터링 추가
        applyFiltersForStatistics(builder, query, location, startDate, endDate, category);

        // 평균 점수 계산
        Double avgScore = queryFactory
                .select(review.score.avg())
                .from(review)
                .where(builder)  // 조건이 없으면 전체 점수 평균을 계산
                .fetchOne();

        double averageScore = Optional.ofNullable(avgScore).orElse(0.0);
        log.info("후기 목록 조회 시 계산된 평균 점수 : {}", averageScore);

        // 각 점수별 카운트 계산
        long fiveScoreCount = countScore(builder, 5);
        long fourScoreCount = countScore(builder, 4);
        long threeScoreCount = countScore(builder, 3);
        long twoScoreCount = countScore(builder, 2);
        long oneScoreCount = countScore(builder, 1);

        // 점수 통계 정보를 포함한 ReviewScoreInfo 객체 반환
        return new ReviewScoreInfo(averageScore, fiveScoreCount, fourScoreCount, threeScoreCount, twoScoreCount, oneScoreCount);
    }

    // 점수 카운트를 위한 메서드
    private long countScore(BooleanBuilder builder, int score) {

        Long count = queryFactory
                .select(review.count())
                .from(review)
                .where(builder) // 필터 조건을 먼저 적용
                .where(review.score.eq(score)) // 점수 조건을 별도로 추가
                .fetchOne();

        return Optional.ofNullable(count).orElse(0L);
    }


    // 필터링 조건을 적용하는 메서드 (통계 전용)
    private void applyFiltersForStatistics(BooleanBuilder builder, String query, String location, String startDate, String endDate, String category) {
        
        if (query != null && !query.isEmpty()) {
            builder.and(review.comment.contains(query)
                    .or(gathering.groupName.contains(query)));
        }

        if (location != null && !location.isEmpty()) {
            builder.and(gathering.location.eq(location));
        }

        if (category != null && !category.isEmpty()) {
            builder.and(gathering.category.eq(category));
        }

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            builder.and(review.createdAt.between(start.atStartOfDay(), end.atTime(23, 59, 59)));
        }
    }

    @Override
    public Page<ReviewDetailInfo> getReviewDetailInfo(Pageable pageable, String query, String location, String startDate, String endDate, String category, String sort) {

        JPAQuery<ReviewDetailInfo> queryBuilder = queryFactory
                .select(
                        Projections.constructor(
                                ReviewDetailInfo.class,
                                gathering.id,
                                gathering.groupName,
                                Expressions.as(
                                        select(image.filePath)
                                                .from(image)
                                                .where(image.gatheringId.eq(gathering.id)),
                                        "gatheringImage"
                                ),
                                gathering.category,
                                gathering.location,
                                user.name,
                                user.profileImagePath,
                                review.id,
                                review.score,
                                review.comment,
                                review.createdAt,
                                review.updatedAt
                        )
                )
                .from(review)
                .leftJoin(review.gathering, gathering)
                .leftJoin(review.user, user);

        // 필터링 적용
        applyFilters(queryBuilder, query, location, startDate, endDate, category, sort);

        // 페이징 적용
        List<ReviewDetailInfo> reviewDetailInfoList = queryBuilder
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(review.count())
                        .from(review)
                        .leftJoin(review.gathering, gathering)
                        .leftJoin(review.user, user)
                        .where(applyFiltersForTotalCount(query, location, startDate, endDate, category)) // 필터를 적용한 메서드 호출
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(reviewDetailInfoList, pageable, total);

    }

    // 필터링된 총 개수를 위한 메서드
    private BooleanBuilder applyFiltersForTotalCount(String query, String location, String startDate, String endDate, String category) {

        BooleanBuilder builder = new BooleanBuilder();

        if (query != null && !query.isEmpty()) {
            builder.and(review.comment.contains(query)
                    .or(gathering.groupName.contains(query)));
        }

        if (location != null && !location.isEmpty()) {
            builder.and(gathering.location.contains(location));
        }

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            builder.and(review.createdAt.between(start.atStartOfDay(), end.atTime(23, 59, 59)));
        }

        if (category != null && !category.isEmpty()) {
            builder.and(gathering.category.eq(category));
        }

        return builder;
    }

    // 필터링 적용
    private void applyFilters(JPAQuery<ReviewDetailInfo> queryBuilder, String query, String location, String startDate, String endDate, String category, String sort) {

        if (query != null && !query.isEmpty()) {
            queryBuilder.where(review.comment.contains(query)
                    .or(gathering.groupName.contains(query)));
        }

        if (location != null && !location.isEmpty()) {
            queryBuilder.where(gathering.location.contains(location));
        }

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            queryBuilder.where(review.createdAt.between(start.atStartOfDay(), end.atTime(23, 59, 59)));
        }

        if (category != null && !category.isEmpty()) {
            queryBuilder.where(gathering.category.eq(category));
        }

        // 정렬 조건 적용
        if ("ratingDesc".equals(sort)) {
            queryBuilder.orderBy(review.score.desc());
        } else if ("ratingAsc".equals(sort)) {
            queryBuilder.orderBy(review.score.asc());
        } else {
            queryBuilder.orderBy(review.createdAt.desc());
        }
    }

}
