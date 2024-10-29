package com.manchui.domain.controller;

import com.manchui.domain.dto.CustomUserDetails;
import com.manchui.domain.dto.GatheringCreateRequest;
import com.manchui.domain.dto.GatheringCreateResponse;
import com.manchui.domain.dto.GatheringPagingResponse;
import com.manchui.domain.service.GatheringService;
import com.manchui.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "모임 목록 조회 (비회원)", description = "비회원이 요청하는 모임의 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비회원이 요청한 목록이 반환되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "요청 값을 확인해주세요."),
            @ApiResponse(responseCode = "404", description = "해당하는 모임이 없습니다.")
    })
    @GetMapping("/public")
    public ResponseEntity<SuccessResponse<GatheringPagingResponse>> getGatheringByGuest(@PageableDefault Pageable pageable,
                                                                                        @RequestParam String query,
                                                                                        @RequestParam String location,
                                                                                        @RequestParam String date) {

        return ResponseEntity.ok(SuccessResponse.successWithData(gatheringService.getGatheringByGuest(pageable, query, location, date)));

    }

    @Operation(summary = "모임 목록 조회)", description = "회원이 요청하는 모임의 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원이 요청한 목록이 반환되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "요청 값을 확인해주세요."),
            @ApiResponse(responseCode = "404", description = "해당하는 모임이 없습니다.")
    })
    @GetMapping("")
    public ResponseEntity<SuccessResponse<GatheringPagingResponse>> getGatheringByUser(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                       @PageableDefault Pageable pageable,
                                                                                       @RequestParam String query,
                                                                                       @RequestParam String location,
                                                                                       @RequestParam String date) {

        return ResponseEntity.ok(SuccessResponse.successWithData(gatheringService.getGatheringByUser(userDetails.getUsername(), pageable, query, location, date)));

    }

    @Operation(summary = "모임 참여", description = "모임에 참여합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모임 참여 신청 완료되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "해당하는 모임이 없습니다.")
    })
    @PostMapping("/{gatheringId}/attendance")
    public ResponseEntity<SuccessResponse<String>> joinGathering(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long gatheringId) {

        gatheringService.joinGathering(userDetails.getUsername(), gatheringId);
        return ResponseEntity.status(201).body(SuccessResponse.successWithNoData("모임 참여 신청 완료되었습니다."));
    }

    @Operation(summary = "모임 좋아요", description = "모임에 좋아요를 누릅니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모임에 좋아요를 눌렀습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "해당하는 모임이 없습니다.")
    })
    @PostMapping("/{gatheringId}/heart")
    public ResponseEntity<SuccessResponse<String>> heartGathering(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long gatheringId) {

        gatheringService.heartGathering(userDetails.getUsername(), gatheringId);
        return ResponseEntity.status(201).body(SuccessResponse.successWithNoData("모임에 좋아요를 눌렀습니다."));
    }

}
