package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.gathering.GatheringCursorPagingResponse;
import com.manchui.domain.dto.gathering.GatheringListResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.manchui.domain.entity.QAttendance.attendance;
import static com.manchui.domain.entity.QGathering.gathering;
import static com.manchui.domain.entity.QHeart.heart;
import static com.manchui.domain.entity.QImage.image;
import static com.manchui.domain.entity.QUser.user;

@Slf4j
public class GatheringCursorQueryDslImpl implements GatheringCursorQueryDsl {

    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private final JPAQueryFactory queryFactory;

    public GatheringCursorQueryDslImpl(EntityManager em) {

        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public GatheringCursorPagingResponse getGatheringListByGuest(Long cursor, int size, String query, String location, String startDate, String endDate, String category, String sort, boolean available) {

        return getGatheringList(null, cursor, size, query, location, startDate, endDate, category, sort, available);
    }

    @Override
    public GatheringCursorPagingResponse getGatheringListByUser(String email, Long cursor, int size, String query, String location, String startDate, String endDate, String category, String sort, boolean available) {

        return getGatheringList(email, cursor, size, query, location, startDate, endDate, category, sort, available);
    }

    private GatheringCursorPagingResponse getGatheringList(String email, Long cursor, int size, String query, String location, String startDate, String endDate, String category, String sort, boolean available) {

        updateIsClosedStatus();

        log.info("{} 모임 목록 조회 요청", (email != null) ? email : "비회원");

        String sortField = (sort == null) ? DEFAULT_SORT_FIELD : sort;

        // 커서 조건을 제외한 기본 조건 빌더
        BooleanBuilder baseConditions = new BooleanBuilder()
                .and(buildFilterConditions(query, location, startDate, endDate, category));

        if (available) {
            baseConditions.and(gathering.maxUsers.gt(
                    JPAExpressions
                            .select(attendance.count())
                            .from(attendance)
                            .where(attendance.gathering.id.eq(gathering.id)
                                    .and(attendance.deletedAt.isNull()))
            ));
        }

        // 1. 목록 조회 쿼리 (커서 조건 포함)
        BooleanBuilder listConditions = new BooleanBuilder(baseConditions);
        if (cursor != null) {
            listConditions.and(gathering.id.lt(cursor));
        }

        List<GatheringListResponse> gatheringList = queryFactory
                .select(buildGatheringListProjection(email))
                .from(gathering)
                .leftJoin(gathering.user, user)
                .where(listConditions)
                .orderBy(sortField.equals("closeDate") ? gathering.dueDate.asc() : gathering.createdAt.desc())
                .limit(size)
                .fetch();

        // 2. 전체 개수 조회 쿼리 (커서 조건 제외)
        long gatheringCount = Optional.ofNullable(
                queryFactory
                        .select(gathering.count())
                        .from(gathering)
                        .where(baseConditions)
                        .fetchOne()
        ).orElse(0L);

        return new GatheringCursorPagingResponse(gatheringList, (int) gatheringCount);
    }


    private ConstructorExpression<GatheringListResponse> buildGatheringListProjection(String email) {

        return Projections.constructor(
                GatheringListResponse.class,
                user.name.as("name"),
                user.profileImagePath.as("profileImage"),
                gathering.id.as("gatheringId"),
                gathering.groupName,
                gathering.category,
                gathering.location,
                Expressions.as(
                        queryFactory.select(image.filePath)
                                .from(image)
                                .where(image.gatheringId.eq(gathering.id)),
                        "gatheringImage"
                ),
                gathering.gatheringDate,
                gathering.dueDate,
                gathering.maxUsers,
                gathering.minUsers,
                Expressions.as(
                        queryFactory.select(attendance.count())
                                .from(attendance)
                                .where(attendance.gathering.id.eq(gathering.id)
                                        .and(attendance.deletedAt.isNull())),
                        "currentUsers"
                ),
                gathering.isOpened,
                gathering.isClosed,
                gathering.createdAt,
                gathering.updatedAt,
                gathering.deletedAt,
                Expressions.as(
                        queryFactory.select(heart.count())
                                .from(heart)
                                .where(
                                        heart.gathering.id.eq(gathering.id)
                                                .and(email != null ? heart.user.email.eq(email) : null)
                                ).gt(0L),
                        "isHearted"
                )
        );
    }

    private BooleanExpression buildFilterConditions(String query, String location, String startDate, String endDate, String category) {

        BooleanExpression condition = gathering.isCanceled.eq(false).and(gathering.isClosed.eq(false));

        if (query != null && !query.isEmpty()) {
            condition = condition.and(gathering.groupName.contains(query));
        }

        if (location != null && !location.isEmpty()) {
            condition = condition.and(gathering.location.contains(location));
        }

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            condition = condition.and(gathering.gatheringDate.between(start.atStartOfDay(), end.atTime(23, 59, 59)));
        }

        if (category != null && !category.isEmpty()) {
            condition = condition.and(gathering.category.eq(category));
        }

        return condition;
    }

    private void updateIsClosedStatus() {

        long updatedCount = queryFactory.update(gathering)
                .set(gathering.isClosed, true)
                .where(gathering.dueDate.before(LocalDateTime.now())
                        .and(gathering.isClosed.eq(false)))
                .execute();

        log.info("모임의 isClosed 상태가 {} 번 업데이트되었습니다.", updatedCount);
    }

}
