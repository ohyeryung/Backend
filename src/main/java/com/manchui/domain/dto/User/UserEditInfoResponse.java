package com.manchui.domain.dto.User;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UserEditInfoResponse {

    private UUID id;
    private String name;
    private String profileImagePath;
}
