package com.manchui.domain.entity;

import com.manchui.domain.dto.review.ReviewCreateRequest;
import com.manchui.domain.dto.review.ReviewCreateResponse;
import com.manchui.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "review")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("후기 id")
    private Long id;

    @Column(name = "score", nullable = false)
    @Comment("후기 평점")
    private int score;

    @Column(name = "comment", nullable = false)
    @Comment("후기 내용")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    @Comment("모임 id")
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("회원 id")
    private User user;

    public ReviewCreateResponse toResponseDto() {

        return ReviewCreateResponse.builder()
                .gatheringId(gathering.getId())
                .score(score)
                .comment(comment)
                .build();
    }

    public void update(ReviewCreateRequest updateRequest, User user, Gathering gathering) {

        this.score = updateRequest.getScore();
        this.comment = updateRequest.getComment();
        this.gathering = gathering;
        this.user = user;
    }

}
