package com.manchui.domain.dto.User;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserEditInfoRequest {

    @Size(min = 2, max = 40, message = "이름은 최소 2자, 최대 40자여야 합니다.")
    private String name;
    private MultipartFile image;
}
