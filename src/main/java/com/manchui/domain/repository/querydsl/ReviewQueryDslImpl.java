package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.review.ReviewInfo;
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
                                review.createdAt
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
