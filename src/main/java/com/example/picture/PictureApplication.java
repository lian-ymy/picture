package com.example.picture;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.example.picture.mapper")
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true)
public class PictureApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureApplication.class, args);
    }

}
