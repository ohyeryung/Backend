package com.manchui.domain.service;

import com.manchui.domain.dto.review.*;
import com.manchui.domain.entity.Attendance;
import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.Review;
import com.manchui.domain.entity.User;
import com.manchui.domain.repository.AttendanceRepository;
import com.manchui.domain.repository.ReviewRepository;
import com.manchui.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.manchui.global.exception.ErrorCode.*;

@Slf4j
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

        // 마감된 모임이면서 모임 일자가 현재 시점으로부터 지난 모임만 후기 등록이 가능, 후기 유효성 검증 및 마감된 상태 체크
        Gathering gathering = gatheringReader.checkGatheringStatusIsClosed(gatheringId);

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
        log.info("{} 유저가 모임 id {}에 후기를 등록하였습니다.", user.getName(), gatheringId);

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

        Review review = validateUserAndReview(email, reviewId);

        // 모임 검증 (취소된 모임일 경우 예외 반환)
        Gathering gathering = gatheringReader.checkGatheringStatusIsCanceled(review.getGathering().getId());

        // 후기 수정
        review.update(updateRequest, review.getUser(), gathering);
        log.info("모임 id {}의 후기 id {}이 수정되었습니다.", gathering.getId(), reviewId);

        return review.toResponseDto();
    }

    /**
     * 2. 후기 삭제
     * 작성자: 오예령
     *
     * @param email    유저 email
     * @param reviewId 후기 id
     */
    @Override
    @Transactional
    public void deleteReview(String email, Long reviewId) {

        Review review = validateUserAndReview(email, reviewId);

        // 모임 검증 (취소된 모임일 경우 예외 반환)
        gatheringReader.checkGatheringStatusIsCanceled(review.getGathering().getId());

        // 후기 소프트 삭제
        review.softDelete();
        log.info("모임 id {}의 후기 id {}이 삭제되었습니다.", review.getGathering().getId(), reviewId);
    }

    /**
     * 3. 전체 후기 조회
     * 작성자: 오예령
     *
     * @param pageable  페이징 처리 시 필요한 항목
     * @param query     검색어
     * @param location  모임 장소 필터링
     * @param startDate 시작 날짜
     * @param endDate   끝 날짜
     * @param category  카테고리 필터링
     * @param sort      정렬 기준 (평점 높은 순, 평점 낮은 순)
     * @return 요청한 범위 내에 속하는 페이징 처리된 후기 리스트 반환
     */
    @Override
    public ReviewDetailPagingResponse searchReview(Pageable pageable, String query, String location, String startDate, String endDate, String category, String sort, int score) {

        // 전체 후기의 평균 평점 및 각 평점 별 후기 개수 계산 메서드
        ReviewScoreInfo scoreInfo = reviewRepository.getScoreStatistics(query, location, category, startDate, endDate, score);
        Page<ReviewDetailInfo> reviewDetailInfoList = reviewRepository.getReviewDetailInfo(pageable, query, location, startDate, endDate, category, sort, score);

        return new ReviewDetailPagingResponse(reviewDetailInfoList, scoreInfo);
    }

    private Review validateUserAndReview(String email, Long reviewId) {

        // 유저 검증
        User user = userService.checkUser(email);

        // 후기 검증 (이미 삭제 처리된 후기는 수정/삭제 불가능)
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId).orElseThrow(
                () -> new CustomException(REVIEW_NOT_FOUND)
        );

        // 본인 후기인지 확인
        if (!review.getUser().equals(user)) {
            throw new CustomException(PERMISSION_DENIED);
        }

        return review;
    }

}
