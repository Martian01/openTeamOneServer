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
	public CommandLineRunner demo(final UserRepository ur, final PersonRepository pr) {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				// data creation
				Person p;
				pr.save(p = new Person("Alt", "Achim", "Ach", null));
				ur.save(new User("admin01", "pass", p.getPersonId(), false, true));
				pr.save(p = new Person("Meier", "Thomas", "Tom", "Pic01"));
				ur.save(new User("player01", "pass", p.getPersonId(), true, false));
				pr.save(p = new Person("Schmidt", "Peter", "Pötte", "Pic02"));
				ur.save(new User("player02", "pass", p.getPersonId(), true, false));
				pr.save(p = new Person("Hansen", "Hans", "Haha", "Pic03"));
				ur.save(new User("player03", "pass", p.getPersonId(), true, false));
				// data retrieval
				System.out.println("\nUsers:");
				for (User user : ur.findAll())
					System.out.println(user.toString());
				System.out.println("\nPersons:");
				for (Person person : pr.findAll())
					System.out.println(person.toString());
			}
		};
	}

}
