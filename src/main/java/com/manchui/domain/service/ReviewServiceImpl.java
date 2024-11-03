package com.manchui.domain.service;

import com.manchui.domain.dto.review.ReviewCreateRequest;
import com.manchui.domain.dto.review.ReviewCreateResponse;
import com.manchui.domain.entity.Attendance;
import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.Review;
import com.manchui.domain.entity.User;
import com.manchui.domain.repository.AttendanceRepository;
import com.manchui.domain.repository.ReviewRepository;
import com.manchui.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.manchui.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final UserService userService;
    private final GatheringReader gatheringReader;
    private final AttendanceRepository attendanceRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 0. 후기 등록
     * 작성자: 오예령
     *
     * @param email         유저 email
     * @param gatheringId   모임 id
     * @param createRequest 후기 평점 및 내용
     * @return 생성된 후기 반환
     */
    @Override
    @Transactional
    public ReviewCreateResponse createReview(String email, Long gatheringId, ReviewCreateRequest createRequest) {

        // 유저 검증
        User user = userService.checkUser(email);

        // 마감된 모임만 후기 등록이 가능, 후기 유효성 검증 및 마감된 상태 체크
        Gathering gathering = gatheringReader.checkGatheringStatusClosed(gatheringId);

        // 유저가 해당 모임에 참석했는지 확인
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndGathering(user, gathering);
        if (existingAttendance.isEmpty()) throw new CustomException(ATTENDANCE_NOT_EXIST);

        // 유저가 해당 모임에 후기를 등록했었는지 확인
        Optional<Review> existingReview = reviewRepository.findByGatheringAndUser(gathering, user);
        if (existingReview.isPresent()) throw new CustomException(ALREADY_REVIEW_EXIST);

        Review review = Review.builder()
                .score(createRequest.getScore())
                .comment(createRequest.getComment())
                .gathering(gathering)
                .user(user)
                .build();

        reviewRepository.save(review);

        return review.toResponseDto();
    }

    /**
     * 1. 후기 수정
     * 작성자: 오예령
     *
     * @param email         유저 email
     * @param reviewId      후기 id
     * @param updateRequest 후기 평점 및 내용
     * @return 수정된 후기 반환
     */
    @Override
    @Transactional
    public ReviewCreateResponse updateReview(String email, Long reviewId, ReviewCreateRequest updateRequest) {

        // 유저 검증
        User user = userService.checkUser(email);

        // 후기 검증
        Review review = reviewRepository.findById(reviewId).orElseThrow(
                () -> new CustomException(REVIEW_NOT_FOUND)
        );

        // 모임 검증
        Long gatheringId = review.getGathering().getId();

        Gathering gathering = gatheringReader.checkGatheringStatusClosed(gatheringId);

        // 후기 수정
        review.update(updateRequest, user, gathering);

        return review.toResponseDto();
    }

}
