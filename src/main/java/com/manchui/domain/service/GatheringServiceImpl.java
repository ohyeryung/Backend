package com.manchui.domain.service;

import com.manchui.domain.dto.CustomUserDetails;
import com.manchui.domain.dto.UserInfo;
import com.manchui.domain.dto.gathering.GatheringCreateRequest;
import com.manchui.domain.dto.gathering.GatheringCreateResponse;
import com.manchui.domain.dto.gathering.GatheringInfoResponse;
import com.manchui.domain.dto.gathering.GatheringPagingResponse;
import com.manchui.domain.dto.review.ReviewDetailPagingResponse;
import com.manchui.domain.dto.review.ReviewInfo;
import com.manchui.domain.dto.review.ReviewScoreInfo;
import com.manchui.domain.entity.*;
import com.manchui.domain.repository.*;
import com.manchui.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.manchui.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatheringServiceImpl implements GatheringService {

    private final GatheringDateUtils gatheringDateUtils;

    private final GatheringStore gatheringStore;

    private final GatheringReader gatheringReader;

    private final GatheringRepository gatheringRepository;

    private final ImageServiceImpl imageService;

    private final ImageRepository imageRepository;

    private final UserService userService;

    private final AttendanceRepository attendanceRepository;

    private final HeartRepository heartRepository;

    private final ReviewRepository reviewRepository;

    /**
     * 0. 모임 생성
     * 작성자 : 오예령
     *
     * @param email         유저 email
     * @param createRequest 모임 생성 시 필요한 데이터 집합
     * @return 생성된 모임 정보 반환
     */
    @Override
    @Transactional
    public GatheringCreateResponse createGathering(String email, GatheringCreateRequest createRequest) {

        // 0. 유저 검증
        User user = userService.checkUser(email);

        if (createRequest.getMinUsers() > createRequest.getMaxUsers()) throw new CustomException(ILLEGAL_MIN_USERS);

        // 1. 모임 날짜 계산 및 검증
        LocalDateTime gatheringDate = gatheringDateUtils.parseGatheringDate(createRequest.getGatheringDate());
        LocalDateTime dueDate = gatheringDateUtils.calculateDueDateFromGatheringDate(gatheringDate);
        gatheringDateUtils.checkGatheringDates(gatheringDate, dueDate);

        Optional<Gathering> findGatheringOpt = gatheringReader.findGathering(user, createRequest.getGroupName());

        // 모임 재생성 로직
        if (findGatheringOpt.isPresent()) {
            Gathering gathering = findGatheringOpt.get();

            // 마감되지 않은 중복된 이름의 모임이 존재한다면 예외 반환
            if (!gathering.isClosed()) throw new CustomException(ALREADY_USED_GROUP_NAME);

            log.info("기존에 만들었던 모임의 이름 : {}", gathering.getGroupName());

            // 이미지 업로드
            imageService.uploadGatheringImage(createRequest.getGatheringImage(), gathering.getId(), true);

            gathering.reopen(gatheringDate, dueDate, createRequest.getLocation(), createRequest.getMaxUsers(), createRequest.getMinUsers(), createRequest.getGatheringContent());
            gathering.updateTime();

            // 기존 참여자 및 좋아요 삭제
            attendanceRepository.findByGathering(gathering).forEach(attendance -> {
                if (!attendance.getUser().equals(user)) {
                    attendance.softDelete();
                }
            });
            heartRepository.deleteAll(heartRepository.findByGathering(gathering));

            log.info("모임 재생성: 주최자 {}가 모임 id{}의 '{}'을 다시 모집 중으로 변경했습니다.", user.getName(), gathering.getId(), gathering.getGroupName());
            Image image = imageRepository.findByGatheringId(gathering.getId());
            return gathering.toResponseDto(image.getFilePath());

        } else {
            // 2. 모임 및 이미지 객체 저장
            Gathering gathering = gatheringStore.saveGathering(createRequest, user, gatheringDate, dueDate);
            imageService.uploadGatheringImage(createRequest.getGatheringImage(), gathering.getId(), false);

            // 3. 주최자를 모임에 자동으로 참여시킴
            attendanceRepository.save(Attendance.builder().user(user).gathering(gathering).build());
            log.info("새 모임 생성: 주최자 {}가 모임 id{}의 '{}'에 자동으로 참여되었습니다.", user.getName(), gathering.getId(), gathering.getGroupName());

            Image image = imageRepository.findByGatheringId(gathering.getId());
            return gathering.toResponseDto(image.getFilePath());
        }
    }

    /**
     * 1. 모임 찾기 및 목록 조회
     * 작성자: 오예령
     *
     * @param userDetails 유저 정보 객체
     * @param pageable    페이징 처리에 필요한 데이터
     * @param query       검색 키워드
     * @param location    위치
     * @param startDate   시작 날짜
     * @param endDate     끝 날짜
     * @param category    모임 카테고리
     * @param sort        정렬 기준
     * @return 요청한 범위에 대한 모임 List 반환
     */
    @Override
    @Transactional
    public GatheringPagingResponse getGatherings(CustomUserDetails userDetails, Pageable pageable, String query, String location, String startDate, String endDate, String category, String sort, boolean available) {

        if (userDetails != null && !userDetails.isGuest()) {
            // 회원일 경우의 로직
            return new GatheringPagingResponse(gatheringRepository.getGatheringListByUser(userDetails.getUsername(), pageable, query, location, startDate, endDate, category, sort, available));
        } else {
            // 비회원일 경우의 로직
            return new GatheringPagingResponse(gatheringRepository.getGatheringListByGuest(pageable, query, location, startDate, endDate, category, sort, available));
        }
    }

    /**
     * 2. 모임 참여
     * 작성자: 오예령
     *
     * @param email       유저 email
     * @param gatheringId 모임 id
     */
    @Override
    @Transactional
    public void joinGathering(String email, Long gatheringId) {

        // 유저 및 모임 객체 검증
        User user = userService.checkUser(email);
        Gathering gathering = gatheringReader.checkGatheringStatus(gatheringId);

        int currentAttendanceCount = attendanceRepository.countByGatheringAndDeletedAtIsNull(gathering);

        if (currentAttendanceCount >= gathering.getMaxUsers()) { // 최대 인원 수 초과 체크
            log.warn("모임 id {}는 정원이 다 찼습니다.", gatheringId);
            throw new CustomException(GATHERING_FULL);
        }

        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndGathering(user, gathering);

        // 참여 내역 검증 및 저장
        if (existingAttendance.isPresent()) {
            handleExistingAttendance(existingAttendance.get());
        } else {
            gatheringStore.saveAttendance(user, gathering);
        }
        // 다시 조회하여 최신 인원 수 가져오기
        currentAttendanceCount = attendanceRepository.countByGatheringAndDeletedAtIsNull(gathering);

        // 모임의 개설 확정 상태값 변경 (최소 인원 충족 시 개설 확정 true)
        if (currentAttendanceCount == gathering.getMinUsers()) gathering.open();

        log.info("사용자 {}가 모임 id {}에 참여했습니다.", user.getName(), gatheringId);
    }

    /**
     * 3. 모임 참여 신청 취소
     * 작성자: 오예령
     *
     * @param email       유저 email
     * @param gatheringId 모임 id
     */
    @Override
    @Transactional
    public void joinCancelGathering(String email, Long gatheringId) {

        // 유저 및 모임 객체 검증
        User user = userService.checkUser(email);
        Gathering gathering = gatheringReader.checkGatheringStatus(gatheringId);

        // 모임 생성자(주최자)는 취소할 수 없음 -> 필수 참석!
        if (user.equals(gathering.getUser())) throw new CustomException(MUST_JOIN_IN);

        // 참여 내역이 있는 지 확인
        Attendance attendance = attendanceRepository.findByUserAndGathering(user, gathering)
                .orElseThrow(() -> new CustomException(ATTENDANCE_NOT_EXIST));

        attendance.softDelete();

        // 모임의 개설 확정 상태값 변경 (최소 인원 미충족 시 개설 확정 false)
        int currentAttendanceCount = attendanceRepository.countByGatheringAndDeletedAtIsNull(gathering);
        if (currentAttendanceCount < gathering.getMinUsers()) gathering.close();

        log.info("사용자 {}가 모임 id {}에 대한 참여 신청을 취소했습니다.", user.getName(), gatheringId);
    }

    /**
     * 4. 모임 좋아요
     * 작성자: 오예령
     *
     * @param email       유저 email
     * @param gatheringId 모임 id
     */
    @Override
    @Transactional
    public void heartGathering(String email, Long gatheringId) {

        // 유저 및 모임 객체 검증
        User user = userService.checkUser(email);
        Gathering gathering = gatheringReader.checkGatheringStatus(gatheringId);

        heartRepository.findByUserAndGathering(user, gathering)
                .ifPresent(heart -> {
                    log.warn("사용자 {}가 모임 id {}에 이미 좋아요를 눌렀습니다.", user.getName(), gatheringId);
                    throw new CustomException(ALREADY_HEART_GATHERING);
                });

        heartRepository.save(Heart.builder().gathering(gathering).user(user).build());
    }

    /**
     * 5. 모임 좋아요 취소
     * 작성자: 오예령
     *
     * @param email       유저 email
     * @param gatheringId 모임 id
     */
    @Override
    @Transactional
    public void heartCancelGathering(String email, Long gatheringId) {

        // 유저 및 모임 객체 검증
        User user = userService.checkUser(email);
        Gathering gathering = gatheringReader.checkGatheringStatus(gatheringId);

        Optional<Heart> heartOptional = heartRepository.findByUserAndGathering(user, gathering);

        // 좋아요가 없으면 예외 처리
        if (heartOptional.isEmpty()) {
            log.warn("사용자 {}가 모임 id {}에 좋아요를 누른 내역이 없습니다.", user.getName(), gatheringId);
            throw new CustomException(HEART_NOT_EXIST);
        }

        // 좋아요 취소 로직
        heartRepository.delete(heartOptional.get());
    }

    /**
     * 6. 모임 상세 조회 (비회원)
     * 작성자: 오예령
     *
     * @param gatheringId 모임 id
     * @param pageable    페이징 처리 시 필요한 항목
     * @return 해당하는 모임의 상세 내용
     */
    @Override
    public GatheringInfoResponse getGatheringInfoByGuest(Long gatheringId, Pageable pageable) {

        return createGatheringInfoResponse(gatheringId, pageable, false, null);
    }

    /**
     * 6-1. 모임 상세 조회 (회원)
     *
     * @param email       유저 email
     * @param gatheringId 모임 id
     * @return 해당하는 모임의 상세 내용
     */
    @Override
    public GatheringInfoResponse getGatheringInfoByUser(String email, Long gatheringId, Pageable pageable) {

        User user = userService.checkUser(email);
        return createGatheringInfoResponse(gatheringId, pageable, true, user);
    }

    /**
     * 7. 모임 모집 취소
     * 작성자: 오예령
     *
     * @param email       유저 email
     * @param gatheringId 모임 id
     */
    @Override
    @Transactional
    public void cancelGathering(String email, Long gatheringId) {

        User user = userService.checkUser(email);
        Gathering gathering = gatheringReader.checkGathering(gatheringId);

        if (!gathering.getUser().equals(user)) {
            log.warn("사용자 {}가 권한 없이 모임 id {}를 취소하려 했습니다.", user.getName(), gatheringId);
            throw new CustomException(UNAUTHORIZED_GATHERING_CANCEL);
        }
        gathering.cancel();
    }

    /**
     * 8. 찜한 모임 목록 조회
     * 작성자: 오예령
     *
     * @param email     유저 email
     * @param pageable  페이징 처리에 필요한 데이터
     * @param location  위치
     * @param startDate 시작 날짜
     * @param endDate   끝 날짜
     * @param category  모임 카테고리
     * @param sort      정렬 기준
     * @return 유저가 찜한 모임의 목록 반환
     */
    @Override
    @Transactional
    public GatheringPagingResponse getHeartList(String email, Pageable pageable, String query, String location, String startDate, String endDate, String category, String sort, boolean available) {

        return new GatheringPagingResponse(gatheringRepository.getHeartList(email, pageable, query, location, startDate, endDate, category, sort, available));
    }

    // 참여 여부 판단
    private void handleExistingAttendance(Attendance attendance) {

        if (attendance.getDeletedAt() == null) {
            log.warn("이미 참여 신청된 모임입니다.");
            throw new CustomException(ALREADY_JOIN_GATHERING);
        } else {
            attendance.restore();
            log.info("사용자가 모임에 다시 참여했습니다.");
        }
    }

    // 상세 조회 응답 객체 생성
    private GatheringInfoResponse createGatheringInfoResponse(Long gatheringId, Pageable pageable, boolean isUser, User user) {

        log.info("모임 id {} 의 상세 조회 응답 객체 생성 중입니다.", gatheringId);
        Gathering gathering = gatheringReader.checkGathering(gatheringId);
        List<UserInfo> userInfoList = gatheringReader.getUserInfoList(gathering);

        ReviewDetailPagingResponse reviewsList = getReviews(pageable, gatheringId);

        Image image = imageRepository.findByGatheringId(gatheringId);

        int currentUsers = userInfoList.size();
        log.info("현재 모임 id {}의 참여자 수: {}", gatheringId, currentUsers);

        boolean isHearted = isUser && heartRepository.findByUserAndGathering(user, gathering).isPresent();

        return new GatheringInfoResponse(gathering, image.getFilePath(), currentUsers, isHearted, userInfoList, reviewsList);
    }

    // 상세 조회 후기 관련 응답 객체 생성
    public ReviewDetailPagingResponse getReviews(Pageable pageable, Long gatheringId) {

        Page<ReviewInfo> pageList = reviewRepository.getReviewInfoList(pageable, gatheringId);
        ReviewScoreInfo scoreInfo = reviewRepository.getScoreStatisticsByGathering(gatheringId);

        return new ReviewDetailPagingResponse(pageList, scoreInfo);
    }

}
