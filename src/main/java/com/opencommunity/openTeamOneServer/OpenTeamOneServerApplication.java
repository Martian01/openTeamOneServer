package com.opencommunity.openTeamOneServer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@SpringBootApplication
public class OpenTeamOneServerApplication extends WebMvcConfigurerAdapter {

	public static void main(String[] args) {
		SpringApplication.run(OpenTeamOneServerApplication.class, args);
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer matcher) {
		matcher.setUseSuffixPatternMatch(false);
	}

	@Bean
	public CommandLineRunner initPersistence(
			TenantParameterRepository tpr,
			UserRepository ur,
			PersonRepository pr,
			RoomRepository rr,
			RoomMemberRepository rmr,
			MessageRepository mr,
			AttachmentRepository ar,
			ViewedConfirmationRepository vcr
	) {
		final ContentService contentService = new ContentService(tpr, ur, pr, rr, rmr, mr, ar, vcr);
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				contentService.createModelData();
				System.out.println("\n" + contentService.exportJson().toString(4) + "\n");
			}
		};
	}

}
