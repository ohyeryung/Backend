package com.manchui.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NameDTO {

    private String name;

    public NameDTO(String name) {
        this.name = name;
    }
}
