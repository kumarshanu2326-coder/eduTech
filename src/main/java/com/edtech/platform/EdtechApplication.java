package com.edtech.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EdtechApplication {
    public static void main(String[] args) {
        SpringApplication.run(EdtechApplication.class, args);
    }
}