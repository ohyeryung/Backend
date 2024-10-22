package com.manchui.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailDTO {

    private String email;

    public EmailDTO(String email) {
        this.email = email;
    }
}
