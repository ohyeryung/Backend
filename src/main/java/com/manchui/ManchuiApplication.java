package com.manchui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ManchuiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ManchuiApplication.class, args);
	}

}
