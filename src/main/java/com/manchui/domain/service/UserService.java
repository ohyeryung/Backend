package com.manchui.domain.service;

import com.manchui.domain.dto.CustomUserDetails;
import com.manchui.domain.dto.User.*;
import com.manchui.domain.entity.*;
import com.manchui.domain.repository.*;
import com.manchui.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.manchui.global.exception.ErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageServiceImpl imageService;
    private final ImageRepository imageRepository;
    private final GatheringRepository gatheringRepository;
    private final AttendanceRepository attendanceRepository;
    private final ReviewRepository reviewRepository;

    // 유저 객체 검증
    public User checkUser(String email) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new CustomException(USER_NOT_FOUND);
        }
        return user;
    }

    //유저 정보 반환
    public UserInfoResponse getUserInfo(String userEmail) {
        User user = userRepository.findByEmail(userEmail);

        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .image(user.getProfileImagePath())
                .createdAt(user.getCreatedAt())
                .build();
    }

    //유저 정보 수정
    @Transactional
    public UUID editUserInfo(String userEmail, UserEditInfoRequest userEditInfoRequest) {

        User user = userRepository.findByEmail(userEmail);
        MultipartFile image = userEditInfoRequest.getImage();
        String name = userEditInfoRequest.getName();
        Long imageId = imageService.uploadUserProfileImage(image);
        user.editName(name);
        Image findImage = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));
        String filePath = findImage.getFilePath();
        user.editProfileImagePath(filePath);
        return user.getId();
    }

    //유저 조회
    public User findByUserId(UUID userId) {

        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    //유저 이름 중복 확인
    public void checkName(String name, String userEmail) {
        if (userRepository.findByEmail(userEmail).getName().equals(name)) {
            return;
        }
        if (userRepository.existsByName(name)) {
            throw new CustomException(ILLEGAL_USERNAME_DUPLICATION);
        }
    }

    //내가 작성한 모임 목록 조회
    public UserWrittenGatheringsResponse getWrittenGatheringList(String userEmail, Pageable pageable) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort());

        User user = userRepository.findByEmail(userEmail);

        Page<Gathering> gatheringList = gatheringRepository.findByUserEquals(user, pageRequest);

        //DTO에 맞게 변환
        Page<GatheringInfo> writtenGatheringList = gatheringList.map(m -> {
            String imagePath = imageRepository.findByGatheringId(m.getId()).getFilePath();
            int participantUsers = attendanceRepository.countByGatheringAndDeletedAtIsNull(m);

            return new GatheringInfo(m.getId(), m.getGroupName(), m.getCategory(), m.getLocation(),
                    imagePath, m.getGatheringDate(), m.getDueDate(),
                    m.getMaxUsers(), participantUsers, m.isOpened(), m.isCanceled(), m.isClosed(), m.getCreatedAt(),
                    m.getUpdatedAt(), m.getDeletedAt());
        });

        return new UserWrittenGatheringsResponse(
                writtenGatheringList.getNumberOfElements(), writtenGatheringList,
                writtenGatheringList.getSize(), writtenGatheringList.getNumber() + 1,
                writtenGatheringList.getTotalPages());
    }

    //사용자가 참여한 모임 목록 조회
    public UserParticipatedGatheringResponse getParticipatedGatheringList(String userEmail, Pageable pageable) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort());

        User user = userRepository.findByEmail(userEmail);

        //유저 모임 참여 엔티티 조회
        List<Attendance> userAttendance = attendanceRepository.findByUserAndDeletedAtIsNull(user);
        //참여한 모임 ID 목록
        List<Long> gatheringIdList = new ArrayList<>();

        for (Attendance attendance : userAttendance) {
            //모임 취소 X, 사용자가 모임 생성자인 경우
            if (!attendance.getGathering().isCanceled() && !attendance.getUser().equals(attendance.getGathering().getUser())) {
                gatheringIdList.add(attendance.getGathering().getId());
            }
        }

        //참여한 모임 페이징 조회
        Page<Gathering> gatheringList = gatheringRepository.findByIdIn(gatheringIdList, pageRequest);

        //GatheringInfo DTO로 변환
        Page<GatheringInfo> participatedGatheringList = gatheringList.map(m -> {

            String filePath = imageRepository.findByGatheringId(m.getId()).getFilePath();
            int participantUsers = attendanceRepository.countByGatheringAndDeletedAtIsNull(m);


            return new GatheringInfo(m.getId(), m.getGroupName(), m.getCategory(), m.getLocation()
                    , filePath, m.getGatheringDate(), m.getDeletedAt(), m.getMaxUsers(), participantUsers
                    , m.isOpened(), m.isCanceled(), m.isClosed(), m.getCreatedAt(), m.getUpdatedAt(), m.getDeletedAt());
        });

        return new UserParticipatedGatheringResponse(gatheringList.getNumberOfElements(), participatedGatheringList
                , participatedGatheringList.getSize(), participatedGatheringList.getNumber() + 1, participatedGatheringList.getTotalPages());
    }

    //내가 작성한 목록 조회
    public UserWrittenReviewsResponse getWrittenReviews(String userEmail, Pageable pageable) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort());

        User user = userRepository.findByEmail(userEmail);
        //리뷰 페이징 조회
        Page<Review> reviews = reviewRepository.findByUser(user, pageRequest);
        //사용자가 작성한 리뷰정보 DTO로 매핑
        Page<WrittenReviewInfo> writtenReviewInfos = reviews.map(m -> {

            String filePath = imageRepository.findByGatheringId(m.getGathering().getId()).getFilePath();

            return new WrittenReviewInfo(m.getGathering().getId(), m.getScore(),
                    m.getGathering().getGroupName(), m.getGathering().getCategory(),
                    m.getGathering().getLocation(), m.getComment(),filePath, m.getGathering().getGatheringDate(), m.getCreatedAt(), m.getUpdatedAt());
        });
        //응답 데이터 반환
        return new UserWrittenReviewsResponse(writtenReviewInfos.getNumberOfElements(), writtenReviewInfos,
                writtenReviewInfos.getSize(), writtenReviewInfos.getNumber() + 1, writtenReviewInfos.getTotalPages());
    }

    //리뷰 작성 가능한 모임 목록 조회
    public UserReviewableGatheringsResponse getReviewableGatherings(String userEmail, Pageable pageable) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort());

        User user = userRepository.findByEmail(userEmail);
        //참가 취소하지 않은 Attendance 조회
        List<Attendance> attendances = attendanceRepository.findByUserAndDeletedAtIsNull(user);

        //참가한 모임 Id 리스트
        List<Long> attendedGatheringIds = new ArrayList<>();

        for (Attendance attendance : attendances) {
            //모임 취소 X, 삭제 X, 모임의 모임 날짜를 지난 경우
            if (!attendance.getGathering().isCanceled() && attendance.getGathering().getDeletedAt() == null
                    && attendance.getGathering().getGatheringDate().isBefore(LocalDateTime.now())) {
                attendedGatheringIds.add(attendance.getGathering().getId());
            }
        }

        //삭제하지 않은 리뷰 리스트
        List<Review> reviews = reviewRepository.findByUserAndDeletedAtIsNull(user);

        //리뷰 작성한 모임 id 리스트
        List<Long> reviewedGatheringIds = new ArrayList<>();
        for (Review review : reviews) {
            //작성한 리뷰의 모임이 취소 X, 삭제 X, 모임의 모임 날짜를 지난 경우
            if (!review.getGathering().isCanceled() && review.getGathering().getDeletedAt() == null
                    && review.getGathering().getGatheringDate().isBefore(LocalDateTime.now()) ) {
                reviewedGatheringIds.add(review.getGathering().getId());
            }
        }

        //리뷰 작성 가능한 모임Id 리스트
        List<Long> reviewableGatheringIds = new ArrayList<>();

        for (Long attendedGatheringId : attendedGatheringIds) {
            //참여한 모임에서 리뷰 작성하지 않은 모임인 경우
            if (!reviewedGatheringIds.contains(attendedGatheringId)) {
                reviewableGatheringIds.add(attendedGatheringId);
            }
        }
        //리뷰 작성한 모임 페이징 조회
        Page<Gathering> reviewableGatheringInfos = gatheringRepository.findByIdIn(reviewableGatheringIds, pageRequest);

        //ReviewableGatheringInfo DTO에 맞게 변환
        Page<ReviewableGatheringInfo> map = reviewableGatheringInfos.map(m -> {

            String filePath = imageRepository.findByGatheringId(m.getId()).getFilePath();
            int participantUsers = attendanceRepository.countByGatheringAndDeletedAtIsNull(m);

            return new ReviewableGatheringInfo(m.getId(), m.getGroupName(), m.getCategory(),
                    m.getLocation(), filePath, m.getGatheringDate(), m.getMaxUsers(),
                    participantUsers, m.getCreatedAt(), m.getUpdatedAt());
        });

        return new UserReviewableGatheringsResponse(map.getNumberOfElements(), map, map.getSize(), map.getNumber() + 1, map.getTotalPages());
    }
}