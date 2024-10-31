package com.manchui.domain.controller;

import com.manchui.domain.dto.CustomUserDetails;
import com.manchui.domain.dto.User.UserEditInfoResponse;
import com.manchui.domain.dto.User.UserEditInfoRequest;
import com.manchui.domain.dto.User.UserInfoResponse;
import com.manchui.domain.entity.User;
import com.manchui.domain.service.UserService;
import com.manchui.global.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/auths/user")
    public ResponseEntity<SuccessResponse<UserInfoResponse>> userInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {

        String userEmail = userDetails.getUsername();
        UserInfoResponse userInfo = userService.getUserInfo(userEmail);

        return ResponseEntity.ok().body(SuccessResponse.successWithData(userInfo));
    }

    @PutMapping("/api/auths/user")
    public ResponseEntity<SuccessResponse<UserEditInfoResponse>> editUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          @ModelAttribute @Valid UserEditInfoRequest userEditInfoRequest) {

        String userEmail = userDetails.getUsername();
        userService.checkName(userEditInfoRequest.getName());
        UUID userId = userService.editUserInfo(userEmail, userEditInfoRequest);
        User user = userService.findByUserId(userId);
        UserEditInfoResponse response = UserEditInfoResponse.builder()
                .id(userId)
                .name(user.getName())
                .profileImagePath(user.getProfileImagePath())
                .build();

        return ResponseEntity.ok().body(SuccessResponse.successWithData(response));
    }

}
