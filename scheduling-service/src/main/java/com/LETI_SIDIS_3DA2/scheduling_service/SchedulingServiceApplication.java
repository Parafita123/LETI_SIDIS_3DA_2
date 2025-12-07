package com.LETI_SIDIS_3DA2.scheduling_service;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class SchedulingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchedulingServiceApplication.class, args);
	}

}
