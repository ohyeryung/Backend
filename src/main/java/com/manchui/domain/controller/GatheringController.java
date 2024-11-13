package com.manchui.domain.controller;

import com.manchui.domain.dto.CustomUserDetails;
import com.manchui.domain.dto.gathering.GatheringCreateRequest;
import com.manchui.domain.dto.gathering.GatheringCreateResponse;
import com.manchui.domain.dto.gathering.GatheringInfoResponse;
import com.manchui.domain.dto.gathering.GatheringPagingResponse;
import com.manchui.domain.service.GatheringService;
import com.manchui.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gatherings")
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

    @Operation(summary = "모임 목록 조회", description = "회원 또는 비회원이 요청하는 모임의 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청한 목록이 반환되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "요청 값을 확인해주세요."),
            @ApiResponse(responseCode = "404", description = "해당하는 모임이 없습니다.")
    })
    @GetMapping("/public")
    public ResponseEntity<SuccessResponse<GatheringPagingResponse>> getGatherings(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                  @RequestParam(defaultValue = "1") int page,
                                                                                  @RequestParam int size,
                                                                                  @RequestParam(required = false) String query,
                                                                                  @RequestParam(required = false) String location,
                                                                                  @RequestParam(required = false) String startDate,
                                                                                  @RequestParam(required = false) String endDate,
                                                                                  @RequestParam(required = false) String category,
                                                                                  @RequestParam(required = false) String sort,
                                                                                  @RequestParam(required = false) boolean available) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        GatheringPagingResponse response = gatheringService.getGatherings(userDetails, pageable, query, location, startDate, endDate, category, sort, available);

        return ResponseEntity.ok(SuccessResponse.successWithData(response));
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

    @Operation(summary = "모임 참여 신청 취소", description = "모임에 참여했던 신청을 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모임 참여 신청이 취소되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "해당하는 모임이 없습니다.")
    })
    @DeleteMapping("/{gatheringId}/cancel")
    public ResponseEntity<SuccessResponse<String>> joinCancelGathering(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long gatheringId) {

        gatheringService.joinCancelGathering(userDetails.getUsername(), gatheringId);
        return ResponseEntity.status(200).body(SuccessResponse.successWithNoData("모임 참여 신청이 취소되었습니다."));
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

    @Operation(summary = "모임 좋아요 취소", description = "모임에 눌렀던 좋아요를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모임에 누른 좋아요가 취소되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "해당하는 모임이 없습니다.")
    })
    @DeleteMapping("/{gatheringId}/heart")
    public ResponseEntity<SuccessResponse<String>> heartCancelGathering(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long gatheringId) {

        gatheringService.heartCancelGathering(userDetails.getUsername(), gatheringId);
        return ResponseEntity.ok().body(SuccessResponse.successWithNoData("모임에 누른 좋아요가 취소되었습니다."));
    }

    @Operation(summary = "모임 상세 조회 (비회원)", description = "비회원이 요청하는 모임의 상세 내용을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비회원이 요청한 모임의 상세 내용이 반환되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "해당하는 모임이 없습니다.")
    })
    @GetMapping("/public/{gatheringId}/reviews")
    public ResponseEntity<SuccessResponse<GatheringInfoResponse>> getGatheringInfoByGuest(@PathVariable Long gatheringId,
                                                                                          @RequestParam(defaultValue = "1") int page,
                                                                                          @RequestParam int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(SuccessResponse.successWithData(gatheringService.getGatheringInfoByGuest(gatheringId, pageable)));
    }

    @Operation(summary = "모임 상세 조회 (회원)", description = "회원이 요청하는 모임의 상세 내용을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원이 요청한 모임의 상세 내용이 반환되었습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "해당하는 모임이 없습니다.")
    })
    @GetMapping("/{gatheringId}/reviews")
    public ResponseEntity<SuccessResponse<GatheringInfoResponse>> getGatheringInfoByUser(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                         @PathVariable Long gatheringId,
                                                                                         @RequestParam(defaultValue = "1") int page,
                                                                                         @RequestParam int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(SuccessResponse.successWithData(gatheringService.getGatheringInfoByUser(userDetails.getUsername(), gatheringId, pageable)));
    }

    @Operation(summary = "찜한 모임 목록 조회", description = "회원이 찜한 모임 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원의 찜한 모임 목록입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "찜한 모임이 없습니다.")
    })
    @PatchMapping("/{gatheringId}/cancel")
    public ResponseEntity<SuccessResponse<String>> cancelGathering(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                   @PathVariable Long gatheringId) {

        gatheringService.cancelGathering(userDetails.getUsername(), gatheringId);
        return ResponseEntity.ok(SuccessResponse.successWithNoData("모임이 정상적으로 취소되었습니다."));
    }

    @Operation(summary = "찜한 모임 목록 조회", description = "회원이 찜한 모임 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원의 찜한 모임 목록입니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "찜한 모임이 없습니다.")
    })
    @GetMapping("/heart")
    public ResponseEntity<SuccessResponse<GatheringPagingResponse>> getHeartList(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                 @RequestParam(defaultValue = "1") int page,
                                                                                 @RequestParam int size,
                                                                                 @RequestParam(required = false) String query,
                                                                                 @RequestParam(required = false) String location,
                                                                                 @RequestParam(required = false) String startDate,
                                                                                 @RequestParam(required = false) String endDate,
                                                                                 @RequestParam(required = false) String category,
                                                                                 @RequestParam(required = false) String sort,
                                                                                 @RequestParam(required = false) boolean available) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(SuccessResponse.successWithData(gatheringService.getHeartList(userDetails.getUsername(), pageable, query, location, startDate, endDate, category, sort, available)));
    }

}

