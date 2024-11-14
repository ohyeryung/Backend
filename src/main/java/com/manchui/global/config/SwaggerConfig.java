package com.manchui.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .addTagsItem(new Tag().name("Join").description("회원가입 관련 API"))
                .addTagsItem(new Tag().name("Users").description("사용자 관련 API"))
                .addTagsItem(new Tag().name("Gatherings").description("모임 관련 API"))
                .addTagsItem(new Tag().name("Reviews").description("후기 관련 API"))
                .addTagsItem(new Tag().name("Token").description("토큰 관련 API"));
    }

    private Info apiInfo() {

        return new Info()
                .title("만취의 API 서버입니다.")
                .description("만 명이 즐기는 취미(만취)의 API 서버입니다.")
                .version("1.0");
    }

}
