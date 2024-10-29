package com.manchui.domain.controller;

import com.manchui.domain.dto.GatheringCreateRequest;
import com.manchui.domain.service.GatheringService;
import com.manchui.global.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gathering")
@Slf4j
public class GatheringController {

    private final GatheringService gatheringService;

    @PostMapping("")
    public ResponseEntity<?> createGathering(@ModelAttribute @Valid GatheringCreateRequest createRequest) {

        return ResponseEntity.status(201).body(SuccessResponse.successWithData(gatheringService.createGathering(createRequest)));

    }
}
