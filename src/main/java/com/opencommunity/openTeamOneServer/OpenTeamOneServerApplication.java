package com.opencommunity.openTeamOneServer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@SpringBootApplication
public class OpenTeamOneServerApplication extends WebMvcConfigurerAdapter {

	@Override
	public void configurePathMatch(PathMatchConfigurer matcher) {
		matcher.setUseSuffixPatternMatch(false);
	}

	public static void main(String[] args) {
		SpringApplication.run(OpenTeamOneServerApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadContent() {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				ContentService.loadModelData();
				System.out.println("\n" + ContentService.exportToJson().toString(4) + "\n");
			}
		};
	}

}
