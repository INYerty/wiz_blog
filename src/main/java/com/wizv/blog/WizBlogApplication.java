package com.wizv.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.wizv.blog.mapper")
public class WizBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(WizBlogApplication.class, args);
    }

}
