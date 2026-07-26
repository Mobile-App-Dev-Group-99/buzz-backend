package com.buzzapp.attendance_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AttendanceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttendanceServiceApplication.class, args);
	}

}
