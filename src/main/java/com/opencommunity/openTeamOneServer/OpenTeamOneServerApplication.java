package com.opencommunity.openTeamOneServer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;


@SpringBootApplication
public class OpenTeamOneServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenTeamOneServerApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(final TenantParameterRepository tpr, final UserRepository ur, final PersonRepository pr) {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				// data creation
				tpr.save(new TenantParameter("name", "OpenTeamOne"));
				tpr.save(new TenantParameter("pictureId", "Pic00"));
				Person p;
				pr.save(p = new Person("Alt", "Achim", "Ach", null));
				ur.save(new User("admin01", "pass", p.getPersonId(), false, true));
				pr.save(p = new Person("Meier", "Thomas", "Tom", "Pic01"));
				ur.save(new User("player01", "pass", p.getPersonId(), true, false));
				pr.save(p = new Person("Schmidt", "Peter", "Pötte", "Pic02"));
				ur.save(new User("player02", "pass", p.getPersonId(), true, false));
				pr.save(p = new Person("Hansen", "Hans", "Haha", "Pic03"));
				ur.save(new User("player03", "pass", p.getPersonId(), true, false));
				// logging on console
				List<TenantParameter> tenantParameters = tpr.findAll();
				List<User> users = ur.findAll();
				List<Person> persons = pr.findAll();
				System.out.println("\nTenant parameters:");
				for (TenantParameter tp : tenantParameters)
					System.out.println(tp.toString());
				System.out.println("\nUsers:");
				for (User user : users)
					System.out.println(user.toString());
				System.out.println("\nPersons:");
				for (Person person : persons)
					System.out.println(person.toString());
				System.out.println("\n");
			}
		};
	}

}
