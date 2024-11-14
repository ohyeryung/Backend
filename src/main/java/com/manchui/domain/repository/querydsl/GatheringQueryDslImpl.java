package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.gathering.GatheringListResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.manchui.domain.entity.QAttendance.attendance;
import static com.manchui.domain.entity.QGathering.gathering;
import static com.manchui.domain.entity.QHeart.heart;
import static com.manchui.domain.entity.QImage.image;
import static com.manchui.domain.entity.QUser.user;
import static com.querydsl.jpa.JPAExpressions.select;

@Slf4j
public class GatheringQueryDslImpl implements GatheringQueryDsl {

    private final JPAQueryFactory queryFactory;

    public GatheringQueryDslImpl(EntityManager em) {

        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<GatheringListResponse> getHeartList(String email, Pageable pageable, String query, String location, String startDate, String endDate, String category, String sort, boolean available) {

        return getHeartGatheringList(email, pageable, query, location, startDate, endDate, category, sort, available);
    }

    // dueDate가 지난 모임의 isClosed 상태 업데이트
    private void updateIsClosedStatus() {

        long updatedCount = queryFactory.update(gathering)
                .set(gathering.isClosed, true)
                .where(gathering.dueDate.before(LocalDateTime.now())
                        .and(gathering.isClosed.eq(false)))
                .execute();

        log.info("모임의 isClosed 상태가 {}번 업데이트되었습니다.", updatedCount);
    }

    // Gathering 목록 쿼리를 수행하고 필터를 적용하는 메서드
    private Page<GatheringListResponse> getHeartGatheringList(String email, Pageable pageable, String query, String location, String startDate, String endDate, String category, String sort, boolean available) {

        updateIsClosedStatus();

        JPAQuery<GatheringListResponse> queryBuilder = buildHeartGatheringQuery(email);
        applyFilters(queryBuilder, query, location, startDate, endDate, category, sort);

        // 참여 가능한 모임만 조회
        if (available) {
            queryBuilder.where(gathering.maxUsers.gt(
                    select(attendance.count())
                            .from(attendance)
                            .where(attendance.gathering.id.eq(gathering.id)
                                    .and(attendance.deletedAt.isNull()))
            ));
        }

        // dueDate 체크: 이 조건은 이미 상태 업데이트 로직에서 처리됨
        queryBuilder.where(gathering.dueDate.after(LocalDateTime.now()));

        // 찜한 모임만 필터링
        queryBuilder.where(heart.user.email.eq(email));

        return executePagedQuery(queryBuilder, pageable);
    }

    // 필터링 적용
    private void applyFilters(JPAQuery<GatheringListResponse> queryBuilder, String query, String location, String startDate, String endDate, String category, String sort) {

        if (StringUtils.hasText(query)) {
            queryBuilder.where(gathering.groupName.contains(query));
        }

        if (StringUtils.hasText(location)) {
            queryBuilder.where(gathering.location.contains(location));
        }

        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            queryBuilder.where(gathering.gatheringDate.between(start.atStartOfDay(), end.atTime(23, 59, 59)));
        }

        if (StringUtils.hasText(category)) {
            queryBuilder.where(gathering.category.eq(category));
        }

        // 정렬 조건 적용
        if ("closeDate".equals(sort)) {
            queryBuilder.orderBy(gathering.dueDate.asc());
        } else {
            queryBuilder.orderBy(gathering.createdAt.desc());
        }
    }

    private JPAQuery<GatheringListResponse> buildHeartGatheringQuery(String email) {

        log.info("{}가 요청한 찜한 모임 목록 조회 쿼리 실행", email);

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
                        gathering.minUsers,
                        Expressions.as(
                                select(attendance.count())
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
                                select(heart.count())
                                        .from(heart)
                                        .where(
                                                heart.user.email.eq(email)
                                                        .and(heart.gathering.id.eq(gathering.id))
                                        ).gt(0L),
                                "isHearted"
                        )
                ))
                .from(gathering)
                .leftJoin(gathering.user, user)
                .leftJoin(heart).on(heart.gathering.id.eq(gathering.id)
                        .and(heart.user.email.eq(email)))
                .where(gathering.isCanceled.eq(false)
                        .and(gathering.isClosed.eq(false)));
    }


    // 페이징 처리된 쿼리 결과를 실행하는 메서드
    private Page<GatheringListResponse> executePagedQuery(JPAQuery<GatheringListResponse> queryBuilder, Pageable pageable) {

        List<GatheringListResponse> gatheringList = queryBuilder
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        log.info("조회된 모임의 수: {}", gatheringList.size());

        long total = fetchTotalCount(queryBuilder);

        return new PageImpl<>(gatheringList, pageable, total);
    }

    // 전체 데이터 개수 조회 메서드
    private long fetchTotalCount(JPAQuery<GatheringListResponse> queryBuilder) {

        return Optional.ofNullable(
                queryBuilder.clone()
                        .offset(0)
                        .limit(Long.MAX_VALUE)
                        .select(gathering.count())
                        .fetchOne()
        ).orElse(0L);
    }

}
