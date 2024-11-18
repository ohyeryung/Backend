package com.manchui.domain.controller;

import com.manchui.domain.service.ReissueService;
import com.manchui.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Token")
@RestController
@RequiredArgsConstructor
public class ReissueController {

    private final ReissueService reissueService;

    @PostMapping("/api/auths/reissue")
    public ResponseEntity<SuccessResponse<Void>> reissue(HttpServletRequest request, HttpServletResponse response) {

        return reissueService.reissue(request, response);
    }

}
