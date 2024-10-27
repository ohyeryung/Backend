package com.manchui.domain.service;

import com.manchui.domain.dto.GatheringCreateRequest;
import com.manchui.domain.dto.GatheringCreateResponse;
import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.Image;
import com.manchui.domain.repository.GatheringRepository;
import com.manchui.domain.repository.ImageRepository;
import com.manchui.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public GatheringCreateResponse createGathering(GatheringCreateRequest createRequest) {

        // TODO : 유저 검증

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

        // 2. 객체 생성 및 저장 (TODO : 유저 객체 추가)
        Gathering initGathering = createRequest.toRegisterEntity(gatheringDate, dueDate);
        Gathering gathering = gatheringRepository.save(initGathering);

        // 3. 이미지 저장
        Long gatheringId = gathering.getId();
        imageService.uploadGatheringImage(createRequest.getGatheringImage(), gatheringId);

        // 4. 반환 객체 생성 및 반환
        Image image = imageRepository.findByGatheringId(gatheringId);

        return gathering.toResponseDto(image.getFilePath());
    }

}
