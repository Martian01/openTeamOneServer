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
	public CommandLineRunner demo(final TenantParameterRepository tpr, final UserRepository ur, final PersonRepository pr, final RoomRepository rr) {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				// data creation
				tpr.save(new TenantParameter("name", "OpenTeamOne"));
				tpr.save(new TenantParameter("pictureId", "Pic00"));
				//
				Person p;
				pr.save(p = new Person("Alt", "Achim", "Ach", null));
				ur.save(new User("admin01", "pass", p.getPersonId(), false, true));
				pr.save(p = new Person("Meier", "Thomas", "Tom", "Pic01"));
				ur.save(new User("player01", "pass", p.getPersonId(), true, false));
				pr.save(p = new Person("Schmidt", "Peter", "Pötte", "Pic02"));
				ur.save(new User("player02", "pass", p.getPersonId(), true, false));
				pr.save(p = new Person("Hansen", "Hans", "Haha", "Pic03"));
				ur.save(new User("player03", "pass", p.getPersonId(), true, false));
				//
				rr.save(new Room("Team Room", "TR", "group", "Pic04"));
				rr.save(new Room("General", "G", "group", "Pic05"));
				rr.save(new Room("News", "NEW", "group", "Pic06"));
				// data retrieval
				Iterable<TenantParameter> tenantParameters = tpr.findAll();
				Iterable<User> users = ur.findAll();
				Iterable<Person> persons = pr.findAll();
				Iterable<Room> rooms = rr.findAll();
				// logging on console
				System.out.println("\nTenant parameters:");
				for (TenantParameter tp : tenantParameters)
					System.out.println(tp.toString());
				System.out.println("\nUsers:");
				for (User user : users)
					System.out.println(user.toString());
				System.out.println("\nPersons:");
				for (Person person : persons)
					System.out.println(person.toString());
				System.out.println("\nRooms:");
				for (Room room : rooms)
					System.out.println(room.toString());
				System.out.println("\n");
			}
		};
	}

}
