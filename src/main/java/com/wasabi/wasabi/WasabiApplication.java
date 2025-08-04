package com.wasabi.wasabi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.wasabi.wasabi")
public class WasabiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WasabiApplication.class, args);
	}

}
