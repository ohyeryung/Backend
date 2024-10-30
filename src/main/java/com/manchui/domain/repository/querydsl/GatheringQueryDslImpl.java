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
    public Page<GatheringListResponse> getGatheringListByGuest(Pageable pageable, String query, String location, String date) {
        // 기본 쿼리
        JPAQuery<GatheringListResponse> queryBuilder = queryFactory
                .select(Projections.constructor(GatheringListResponse.class,
                        user.name.as("name"),
                        user.profileImagePath.as("profileImage"),
                        gathering.id.as("gatheringId"),
                        gathering.groupName,
                        gathering.category,
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
                                        .where(attendance.gathering.id.eq(gathering.id))
                                , "currentUsers"
                        ),
                        gathering.isOpened,
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

        if (date != null && !date.isEmpty()) {
            String[] dateRange = date.split(" - ");
            if (dateRange.length == 2) {
                LocalDate startDate = LocalDate.parse(dateRange[0]);
                LocalDate endDate = LocalDate.parse(dateRange[1]);
                queryBuilder.where(gathering.gatheringDate.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
            }
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

    @Override
    public Page<GatheringListResponse> getGatheringListByUser(String email, Pageable pageable, String query, String location, String date) {

        // 기본 쿼리
        JPAQuery<GatheringListResponse> queryBuilder = queryFactory
                .select(Projections.constructor(GatheringListResponse.class,
                        user.name.as("name"),
                        user.profileImagePath.as("profileImage"),
                        gathering.id.as("gatheringId"),
                        gathering.groupName,
                        gathering.category,
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
                                        .where(attendance.gathering.id.eq(gathering.id))
                                , "currentUsers"
                        ),
                        gathering.isOpened,
                        gathering.isClosed,
                        gathering.createdAt,
                        gathering.updatedAt,
                        gathering.deletedAt,
                        // TODO : 좋아요 기능 구현 후 수정할 부분
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

        if (date != null && !date.isEmpty()) {
            String[] dateRange = date.split(" - ");
            if (dateRange.length == 2) {
                LocalDate startDate = LocalDate.parse(dateRange[0]);
                LocalDate endDate = LocalDate.parse(dateRange[1]);
                queryBuilder.where(gathering.gatheringDate.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
            }
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
