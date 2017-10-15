package com.opencommunity.openTeamOneServer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class OpenTeamOneServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenTeamOneServerApplication.class, args);
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
		final ContentService ps = new ContentService(tpr, ur, pr, rr, rmr, mr, ar, vcr);
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				ps.createModelData();
				System.out.println("\n" + ps.exportJson().toString(4) + "\n");
			}
		};
	}

}
