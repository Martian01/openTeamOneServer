package com.opencommunity.openTeamOneServer;

import com.opencommunity.openTeamOneServer.util.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OpenTeamOneServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenTeamOneServerApplication.class, args);
	}

	@Autowired
	private ContentService contentService;

	@Bean
	public CommandLineRunner loadContent() {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				System.out.println("\n" + contentService.getSummary().toString(4) + "\n");
			}
		};
	}

}
