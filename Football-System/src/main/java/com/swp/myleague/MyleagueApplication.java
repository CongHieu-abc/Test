package com.swp.myleague;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   
public class MyleagueApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyleagueApplication.class, args);
	}

}
