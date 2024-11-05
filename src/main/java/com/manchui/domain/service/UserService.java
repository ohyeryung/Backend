package com.manchui.domain.service;

import com.manchui.domain.dto.User.*;
import com.manchui.domain.entity.*;
import com.manchui.domain.repository.*;
import com.manchui.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.manchui.global.exception.ErrorCode.*;

@Service
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
    public void checkName(String name) {
        if (userRepository.existsByName(name)) {
            throw new CustomException(ILLEGAL_USERNAME_DUPLICATION);
        }
    }

    //내가 작성한 모임 목록 조회
    public UserWrittenGatheringsResponse getWrittenGatheringList(String userEmail, Pageable pageable) {

        User user = userRepository.findByEmail(userEmail);

        Page<Gathering> gatheringList = gatheringRepository.findByUserEquals(user, pageable);

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
                writtenGatheringList.getSize(), writtenGatheringList.getNumber(),
                writtenGatheringList.getTotalPages());
    }

    //사용자가 참여한 모임 목록 조회
    public UserParticipatedGatheringResponse getParticipatedGatheringList(String userEmail, Pageable pageable) {

        User user = userRepository.findByEmail(userEmail);

        //유저 모임 참여 엔티티 조회
        List<Attendance> userAttendance = attendanceRepository.findByUserEquals(user);
        List<Long> gatheringIdList = new ArrayList<>();
        //참여한 모임 ID 목록
        for (Attendance attendance : userAttendance) {
            Long gatheringId = attendance.getGathering().getId();
            gatheringIdList.add(gatheringId);
        }

        Page<Gathering> gatheringList = gatheringRepository.findByIdIn(gatheringIdList, pageable);

        Page<GatheringInfo> participatedGatheringList = gatheringList.map(m -> {

            String filePath = imageRepository.findByGatheringId(m.getId()).getFilePath();
            int participantUsers = attendanceRepository.countByGatheringAndDeletedAtIsNull(m);


            return new GatheringInfo(m.getId(), m.getGroupName(), m.getCategory(), m.getLocation()
                    , filePath, m.getGatheringDate(), m.getDeletedAt(), m.getMaxUsers(), participantUsers
                    , m.isOpened(), m.isCanceled(), m.isClosed(), m.getCreatedAt(), m.getUpdatedAt(), m.getDeletedAt());
        });

        return new UserParticipatedGatheringResponse(gatheringList.getNumberOfElements(), participatedGatheringList
                , participatedGatheringList.getSize(), participatedGatheringList.getNumber(), participatedGatheringList.getTotalPages());
    }

    //내가 작성한 목록 조회
    public UserWrittenReviewsResponse getWrittenReviews(String userEmail, Pageable pageable) {

        User user = userRepository.findByEmail(userEmail);
        //리뷰 페이징 조회
        Page<Review> reviews = reviewRepository.findByUser(user, pageable);
        //사용자가 작성한 리뷰정보 DTO로 매핑
        Page<WrittenReviewInfo> writtenReviewInfos = reviews.map(m -> {

            String filePath = imageRepository.findByGatheringId(m.getGathering().getId()).getFilePath();

            return new WrittenReviewInfo(m.getGathering().getId(), m.getScore(),
                    m.getGathering().getGroupName(), m.getGathering().getCategory(),
                    m.getGathering().getLocation(), filePath, m.getGathering().getGatheringDate(), m.getCreatedAt(), m.getUpdatedAt());
        });
        //응답 데이터 반환
        return new UserWrittenReviewsResponse(writtenReviewInfos.getNumberOfElements(), writtenReviewInfos,
                writtenReviewInfos.getSize(), writtenReviewInfos.getNumber(), writtenReviewInfos.getTotalPages());
    }

}
