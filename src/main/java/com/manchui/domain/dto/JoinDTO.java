package com.manchui.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinDTO {

    @NotEmpty(message = "이름을 입력해주세요.")
    @Size(min = 2, max = 40, message = "이름은 최소 2자, 최대 40자여야 합니다.")
    private String name;
    @NotEmpty(message = "이메일을 입력해주세요.")
    private String email;
    @NotEmpty(message = "비밀번호를 입력해주세요.")
    private String password;
    @NotEmpty(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;
}
