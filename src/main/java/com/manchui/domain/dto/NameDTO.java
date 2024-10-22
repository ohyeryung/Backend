package com.manchui.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NameDTO {

    private String name;

    public NameDTO(String name) {
        this.name = name;
    }
}
