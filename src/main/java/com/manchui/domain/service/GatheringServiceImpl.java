package com.manchui.domain.service;

import com.manchui.domain.dto.*;
import com.manchui.domain.entity.*;
import com.manchui.domain.repository.AttendanceRepository;
import com.manchui.domain.repository.GatheringRepository;
import com.manchui.domain.repository.HeartRepository;
import com.manchui.domain.repository.ImageRepository;
import com.manchui.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.manchui.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatheringServiceImpl implements GatheringService {

    private final GatheringRepository gatheringRepository;

    private final ImageServiceImpl imageService;

    private final ImageRepository imageRepository;

    private final UserService userService;

    private final AttendanceRepository attendanceRepository;

    private final HeartRepository heartRepository;

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

        // 0. 유저 객체 검증
        User user = userService.checkUser(email);

        // 1. 날짜 검증
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 모임 날짜를 LocalDateTime으로 변환
        LocalDateTime gatheringDate = LocalDateTime.parse(createRequest.getGatheringDate(), formatter);

        // 모집 마감 일자는 모임 날짜 기준 최소 29시간 전으로 설정
        LocalDateTime dueDate = gatheringDate.minusHours(29);
        LocalDateTime now = LocalDateTime.now();

        // 1-1. 모임 날짜가 현재로부터 최소 하루 이후인지 확인
        if (!gatheringDate.isAfter(now.plusDays(1))) {
            throw new CustomException(ILLEGAL_GATHERING_DATE);
        }

        // 1-2. 모집 마감 일자가 생성 시점으로부터 최소 5시간 이후인지 확인
        if (!dueDate.isAfter(now)) {
            throw new CustomException(ILLEGAL_DUE_DATE);
        }

        // 2. 객체 생성 및 저장
        Gathering initGathering = createRequest.toRegisterEntity(user, gatheringDate, dueDate);
        Gathering gathering = gatheringRepository.save(initGathering);

        // 3. 이미지 저장
        Long gatheringId = gathering.getId();
        imageService.uploadGatheringImage(createRequest.getGatheringImage(), gatheringId);

        // 4. 반환 객체 생성 및 반환
        Image image = imageRepository.findByGatheringId(gatheringId);

        return gathering.toResponseDto(image.getFilePath());
    }

    /**
     * 1. 모임 찾기 및 목록 조회 (비회원)
     * 작성자: 오예령
     *
     * @param pageable 페이징 처리에 필요한 데이터
     * @param query    검색 키워드
     * @param location 위치
     * @param date     날짜
     * @return 요청한 범위에 대한 모임 List 반환
     */
    @Override
    public GatheringPagingResponse getGatheringByGuest(Pageable pageable, String query, String location, String date) {

        return new GatheringPagingResponse(gatheringRepository.getGatheringListByGuest(pageable, query, location, date));
    }

    /**
     * 1-1. 모임 찾기 및 목록 조회 (회원)
     * 작성자: 오예령
     *
     * @param email    유저 email
     * @param pageable 페이징 처리에 필요한 데이터
     * @param query    검색 키워드
     * @param location 위치
     * @param date     날짜
     * @return 요청한 범위에 대한 모임 List 반환
     */
    @Override
    public GatheringPagingResponse getGatheringByUser(String email, Pageable pageable, String query, String location, String date) {

        return new GatheringPagingResponse(gatheringRepository.getGatheringListByUser(email, pageable, query, location, date));
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

        // 0. 유저 검증
        User user = userService.checkUser(email);

        // 1. 모임 검증
        Gathering gathering = checkGathering(gatheringId);

        // 1-1. 모임 상태값 검증
        if (gathering.isCanceled()) {
            throw new CustomException(GATHERING_CANCELED);
        } else if (gathering.isClosed()) {
            throw new CustomException(GATHERING_CLOSED);
        }

        // 2. 참여 여부 및 deletedAt 검증
        Optional<Attendance> findAttendanceOpt = attendanceRepository.findByUserAndGathering(user, gathering);

        if (findAttendanceOpt.isPresent()) {
            Attendance findAttendance = findAttendanceOpt.get();

            // 만약 deletedAt이 null이 아니라면 기존 참여 기록이 소프트 삭제된 상태이므로 재참여 가능
            if (findAttendance.getDeletedAt() == null) {
                throw new CustomException(ALREADY_JOIN_GATHERING); // 이미 참여 중인 경우
            } else {
                // 소프트 삭제된 상태를 되돌리고 재참여 처리
                findAttendance.restore(); // restore() 메서드는 deletedAt을 null로 설정하는 메서드
                return;
            }
        }

        // 3. 새롭게 참여 기록 생성
        Attendance attendance = Attendance.builder()
                .user(user)
                .gathering(gathering)
                .build();

        attendanceRepository.save(attendance);
    }

    /**
     * 3. 모임 좋아요
     * 작성자: 오예령
     *
     * @param email       유저 email
     * @param gatheringId 모임 id
     */
    @Override
    @Transactional
    public void heartGathering(String email, Long gatheringId) {

        // 0. 유저 검증
        User user = userService.checkUser(email);

        // 1. 모임 검증
        Gathering gathering = checkGathering(gatheringId);

        // 1-1. 모임 상태값 검증
        if (gathering.isCanceled()) {
            throw new CustomException(GATHERING_CANCELED);
        } else if (gathering.isClosed()) {
            throw new CustomException(GATHERING_CLOSED);
        }

        // 이미 좋아요 표시를 한 모임이라면 예외 처리
        if (heartRepository.findByUserAndGathering(user, gathering).isPresent()) {
            throw new CustomException(ALREADY_HEART_GATHERING);
        }

        Heart heart = Heart.builder()
                .gathering(gathering)
                .user(user)
                .build();

        heartRepository.save(heart);
    }

    /**
     * 4. 모임 참여 신청 취소
     * 작성자: 오예령
     *
     * @param email       유저 email
     * @param gatheringId 모임 id
     */
    @Override
    @Transactional
    public void joinCancelGathering(String email, Long gatheringId) {

        User user = userService.checkUser(email);
        Gathering gathering = checkGathering(gatheringId);

        Attendance byUserAndGathering = attendanceRepository.findByUserAndGathering(user, gathering).orElseThrow(
                () -> new CustomException(ATTENDANCE_NOT_EXIST)
        );

        // 참여 내역의 실제 데이터를 삭제하지 않고 deletedAt 필드에 삭제된 시점만 기록
        byUserAndGathering.softDelete();

    }

    /**
     * 5. 모임 상세 조회 (비회원)
     * 작성자: 오예령
     *
     * @param gatheringId 모임 id
     * @return 해당하는 모임의 상세 내용
     */
    @Override
    public GatheringInfoResponse getGatheringInfoByGuest(Long gatheringId) {

        Gathering gathering = checkGathering(gatheringId);

        List<UserInfo> userInfoList = new ArrayList<>();
        List<ReviewInfo> reviewInfoList = new ArrayList<>();

        Image image = imageRepository.findByGatheringId(gatheringId);

        // 모임 참석자 리스트 조회
        List<Attendance> byGathering = attendanceRepository.findByGathering(gathering);

        // 참여자 인원 count
        int currentUsers = byGathering.size();
        log.info("currentUsers : {}", currentUsers);

        // 참석자 리스트를 userInfo 리스트로 변환
        for (Attendance attendance : byGathering) {
            String name = attendance.getUser().getName();
            String profileImagePath = attendance.getUser().getProfileImagePath();
            UserInfo userInfo = new UserInfo(name, profileImagePath);
            userInfoList.add(userInfo);
        }

        // TODO: 후기 리스트 추가

        return new GatheringInfoResponse(gathering, image.getFilePath(), currentUsers, userInfoList, reviewInfoList);
    }


    /**
     * 4-1. 모임 상세 조회 (회원)
     *
     * @param email       유저 email
     * @param gatheringId 모임 id
     * @return 해당하는 모임의 상세 내용
     */
    @Override
    public GatheringInfoResponse getGatheringInfoByUser(String email, Long gatheringId) {

        User user = userService.checkUser(email);
        Gathering gathering = checkGathering(gatheringId);

        List<UserInfo> userInfoList = new ArrayList<>();
        List<ReviewInfo> reviewInfoList = new ArrayList<>();

        Image image = imageRepository.findByGatheringId(gatheringId);

        // 모임 참석자 리스트 조회
        List<Attendance> byGathering = attendanceRepository.findByGathering(gathering);

        Optional<Heart> byUserAndGatheringId = heartRepository.findByUserAndGathering(user, gathering);

        boolean isHearted = byUserAndGatheringId.isPresent();
        log.info("isHearted: {}", isHearted);

        // 참여자 인원 count
        int currentUsers = byGathering.size();
        log.info("currentUsers : {}", currentUsers);

        // 참석자 리스트를 userInfo 리스트로 변환
        for (Attendance attendance : byGathering) {
            String name = attendance.getUser().getName();
            String profileImagePath = attendance.getUser().getProfileImagePath();
            UserInfo userInfo = new UserInfo(name, profileImagePath);
            userInfoList.add(userInfo);
        }

        // TODO: 후기 리스트 추가

        return new GatheringInfoResponse(gathering, image.getFilePath(), currentUsers, isHearted, userInfoList, reviewInfoList);
    }

    /**
     * 모임 유효성 검증
     * 작성자: 오예령
     *
     * @param gatheringId 모임 id
     */
    public Gathering checkGathering(Long gatheringId) {

        return gatheringRepository.findById(gatheringId).orElseThrow(
                () -> new CustomException(GATHERING_NOT_FOUND)
        );

    }


}

