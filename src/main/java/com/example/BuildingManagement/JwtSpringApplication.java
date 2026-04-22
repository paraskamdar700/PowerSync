package com.example.BuildingManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JwtSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(JwtSpringApplication.class, args);


    }

}