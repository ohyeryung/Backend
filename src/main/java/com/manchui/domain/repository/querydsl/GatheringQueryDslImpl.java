package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.GatheringListResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static com.manchui.domain.entity.QAttendance.attendance;
import static com.manchui.domain.entity.QGathering.gathering;
import static com.manchui.domain.entity.QHeart.heart;
import static com.manchui.domain.entity.QImage.image;
import static com.manchui.domain.entity.QUser.user;
import static com.querydsl.jpa.JPAExpressions.select;

public class GatheringQueryDslImpl implements GatheringQueryDsl {

    private final JPAQueryFactory queryFactory;

    public GatheringQueryDslImpl(EntityManager em) {

        this.queryFactory = new JPAQueryFactory(em);
    }

    // 비회원 모임 목록 조회
    @Override
    public Page<GatheringListResponse> getGatheringListByGuest(Pageable pageable, String query, String location, String startDate, String endDate, String category) {

        JPAQuery<GatheringListResponse> queryBuilder = buildBaseQuery(null);
        applyFilters(queryBuilder, query, location, startDate, endDate, category);
        return executePagedQuery(queryBuilder, pageable);
    }

    // 회원 모임 목록 조회
    @Override
    public Page<GatheringListResponse> getGatheringListByUser(String email, Pageable pageable, String query, String location, String startDate, String endDate, String category) {

        JPAQuery<GatheringListResponse> queryBuilder = buildBaseQuery(email);
        applyFilters(queryBuilder, query, location, startDate, endDate, category);
        return executePagedQuery(queryBuilder, pageable);
    }

    // 공통 쿼리
    private JPAQuery<GatheringListResponse> buildBaseQuery(String email) {

        return queryFactory
                .select(Projections.constructor(GatheringListResponse.class,
                        user.name.as("name"),
                        user.profileImagePath.as("profileImage"),
                        gathering.id.as("gatheringId"),
                        gathering.groupName,
                        gathering.category,
                        gathering.location,
                        Expressions.as(
                                select(image.filePath)
                                        .from(image)
                                        .where(image.gatheringId.eq(gathering.id)),
                                "gatheringImage"
                        ),
                        gathering.gatheringDate,
                        gathering.dueDate,
                        gathering.maxUsers,
                        Expressions.as(
                                select(attendance.count())
                                        .from(attendance)
                                        .where(attendance.gathering.id.eq(gathering.id)
                                                .and(attendance.deletedAt.isNull())),
                                "currentUsers"
                        ),
                        gathering.isOpened,
                        gathering.isCanceled,
                        gathering.isClosed,
                        gathering.createdAt,
                        gathering.updatedAt,
                        gathering.deletedAt,
                        email != null ? Expressions.as(
                                select(heart.count())
                                        .from(heart)
                                        .where(
                                                heart.user.email.eq(email)
                                                        .and(heart.gathering.id.eq(gathering.id))
                                        ).gt(0L),
                                "isHearted"
                        ) : gathering.isHearted
                ))
                .from(gathering)
                .leftJoin(gathering.user, user)
                .where(gathering.isCanceled.eq(false));
    }

    // 필터링 적용
    private void applyFilters(JPAQuery<GatheringListResponse> queryBuilder, String query, String location, String startDate, String endDate, String category) {

        if (query != null && !query.isEmpty()) {
            queryBuilder.where(gathering.groupName.contains(query));
        }

        if (location != null && !location.isEmpty()) {
            queryBuilder.where(gathering.location.contains(location));
        }

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            queryBuilder.where(gathering.gatheringDate.between(start.atStartOfDay(), end.atTime(23, 59, 59)));
        }

        if (category != null && !category.isEmpty()) {
            queryBuilder.where(gathering.category.eq(category));
        }
    }

    // 페이징 처리 목록 생성
    private Page<GatheringListResponse> executePagedQuery(JPAQuery<GatheringListResponse> queryBuilder, Pageable pageable) {

        List<GatheringListResponse> gatheringList = queryBuilder
                .orderBy(gathering.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryBuilder.clone()
                .select(gathering.count())
                .fetchOne();

        return new PageImpl<>(gatheringList, pageable, total);
    }

}
