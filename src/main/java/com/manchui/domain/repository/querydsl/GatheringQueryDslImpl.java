package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.GatheringListResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
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

    // 모임 찾기 및 목록 조회 (비회원)
    @Override
    public Page<GatheringListResponse> getGatheringListByGuest(Pageable pageable, String query, String location, String startDate, String endDate, String category) {

        // 기본 쿼리
        JPAQuery<GatheringListResponse> queryBuilder = queryFactory
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
                                        .where(image.gatheringId.eq(gathering.id))
                                , "gatheringImage"),
                        gathering.gatheringDate,
                        gathering.dueDate,
                        gathering.maxUsers,
                        Expressions.as(
                                select(attendance.count())
                                        .from(attendance)
                                        .where(attendance.gathering.id.eq(gathering.id)
                                                .and(attendance.deletedAt.isNull()))
                                , "currentUsers"
                        ),
                        gathering.isOpened,
                        gathering.isCanceled,
                        gathering.isClosed,
                        gathering.createdAt,
                        gathering.updatedAt,
                        gathering.deletedAt,
                        gathering.isHearted
                ))
                .from(gathering)
                .leftJoin(gathering.user, user)
                .where(gathering.isCanceled.eq(false)); // 목록 조회 시에는 취소되지 않은 모임들만 반환

        // 쿼리 필터링
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

        // 쿼리 실행
        List<GatheringListResponse> gatheringList = queryBuilder
                .orderBy(gathering.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 필터가 적용된 queryBuilder를 기반으로 total 값 계산
        JPAQuery<Long> countQuery = queryBuilder.clone()
                .select(gathering.count());

        Long total = countQuery.fetchOne();

        return new PageImpl<>(gatheringList, pageable, total);
    }

    @Override
    public Page<GatheringListResponse> getGatheringListByUser(String email, Pageable pageable, String query, String location, String startDate, String endDate, String category) {

        // 기본 쿼리
        JPAQuery<GatheringListResponse> queryBuilder = queryFactory
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
                                        .where(image.gatheringId.eq(gathering.id))
                                , "gatheringImage"),
                        gathering.gatheringDate,
                        gathering.dueDate,
                        gathering.maxUsers,
                        Expressions.as(
                                select(attendance.count())
                                        .from(attendance)
                                        .where(attendance.gathering.id.eq(gathering.id)
                                                .and(attendance.deletedAt.isNull())
                                        )
                                , "currentUsers"
                        ),
                        gathering.isOpened,
                        gathering.isCanceled,
                        gathering.isClosed,
                        gathering.createdAt,
                        gathering.updatedAt,
                        gathering.deletedAt,
                        // 사용자가 해당 모임을 좋아요 눌렀는지 여부 확인 로직 수정
                        Expressions.as(
                                JPAExpressions
                                        .select(heart.count())
                                        .from(heart)
                                        .where(
                                                heart.user.email.eq(email) // 이메일로 사용자 확인
                                                        .and(heart.gathering.id.eq(gathering.id)) // gathering id와 일치하는지 확인
                                        )
                                        .gt(0L), // 0보다 큰지 비교하여 boolean 값 반환
                                "isHearted")
                ))
                .from(gathering)
                .leftJoin(gathering.user, user)
                .where(gathering.isCanceled.eq(false)); // 목록 조회 시에는 취소되지 않은 모임들만 반환

        // 쿼리 필터링
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

        // 쿼리 실행
        List<GatheringListResponse> gatheringList = queryBuilder
                .orderBy(gathering.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(gathering.count())
                .from(gathering)
                .where(gathering.isCanceled.eq(false)) // 목록 조회 시에는 취소되지 않은 모임들만 반환
                .fetchOne();

        return new PageImpl<>(gatheringList, pageable, total);
    }

}
