package com.manchui.domain.controller;

import com.manchui.domain.dto.JoinDTO;
import com.manchui.domain.dto.NameDTO;
import com.manchui.domain.service.JoinService;
import com.manchui.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Join")
@Slf4j
@RestController
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @PostMapping("/api/auths/signup")
    public ResponseEntity<SuccessResponse<Void>> signup(@Valid @RequestBody JoinDTO joinDTO) {

        joinService.joinProcess(joinDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.successWithNoData("사용자 생성 성공"));
    }

    @PostMapping("/api/auths/check-name")
    public ResponseEntity<SuccessResponse<Void>> checkName(@RequestBody NameDTO nameDTO) {

        joinService.checkName(nameDTO);

        return ResponseEntity.ok().body(SuccessResponse.successWithNoData("사용 가능한 이름 입니다."));
    }

}
