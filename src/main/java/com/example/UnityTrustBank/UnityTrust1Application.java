package com.example.UnityTrustBank;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UnityTrust1Application {

	public static void main(String[] args) {
		SpringApplication.run(UnityTrust1Application.class, args);
		System.out.println("Application Start Now ");
	}
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

}
