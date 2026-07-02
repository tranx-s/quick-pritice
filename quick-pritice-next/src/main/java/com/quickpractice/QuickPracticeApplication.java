package com.quickpractice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.quickpractice.mapper")
public class QuickPracticeApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuickPracticeApplication.class, args);
    }
}
