package com.LETI_SIDIS_3DA_2.physician_service;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class PhysicianServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhysicianServiceApplication.class, args);
	}

}
