package com.manchui.domain.controller;

import com.manchui.domain.dto.CustomUserDetails;
import com.manchui.domain.dto.GatheringCreateRequest;
import com.manchui.domain.dto.GatheringCreateResponse;
import com.manchui.domain.service.GatheringService;
import com.manchui.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Operation(summary = "모임 생성", description = "새로운 모임을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "모임이 생성되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "입력값을 확인해주세요."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 계정입니다.")
    })
    @PostMapping("")
    public ResponseEntity<SuccessResponse<GatheringCreateResponse>> createGathering(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                    @ModelAttribute @Valid GatheringCreateRequest createRequest) {

        return ResponseEntity.status(201)
                .body(SuccessResponse.successWithData(
                        gatheringService.createGathering(userDetails.getUsername(), createRequest)));

    }

}
