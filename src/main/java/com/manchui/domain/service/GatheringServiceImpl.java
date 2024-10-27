package com.manchui.domain.service;

import com.manchui.domain.dto.GatheringCreateRequest;
import com.manchui.domain.dto.GatheringCreateResponse;
import com.manchui.domain.dto.GatheringPagingResponse;
import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.Image;
import com.manchui.domain.entity.User;
import com.manchui.domain.repository.GatheringRepository;
import com.manchui.domain.repository.ImageRepository;
import com.manchui.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.manchui.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatheringServiceImpl implements GatheringService {

    private final GatheringRepository gatheringRepository;

    private final ImageServiceImpl imageService;

    private final ImageRepository imageRepository;

    private final UserService userService;

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

        // 모임 날짜와 마감 일자를 LocalDateTime으로 변환
        LocalDateTime gatheringDate = LocalDateTime.parse(createRequest.getGatheringDate(), formatter);
        LocalDateTime dueDate = LocalDateTime.parse(createRequest.getDueDate(), formatter);
        LocalDateTime now = LocalDateTime.now();

        // 1-1. 모임 날짜가 현재로부터 최소 하루 뒤인지 확인
        if (!gatheringDate.isAfter(now.plusDays(1))) {
            throw new CustomException(ILLEGAL_GATHERING_DATE);
        }

        // 1-2. 마감 일자가 현재 시점과 같지 않고, 최소 하루 이후여야 함
        if (!dueDate.isAfter(now.plusDays(1))) {
            throw new CustomException(ILLEGAL_DUE_DATE);
        }

        // 1-3. 모임 날짜와 마감 일자가 최소 5시간 차이가 나는지 확인
        if (gatheringDate.isBefore(dueDate.plusHours(5))) {
            throw new CustomException(ILLEGAL_DATE_DIFFERENCE);
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


}
