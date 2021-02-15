package com.opencommunity.openTeamOneServer;

import com.opencommunity.openTeamOneServer.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;

@SpringBootApplication
public class OpenTeamOneServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenTeamOneServerApplication.class, args);
	}

	@Autowired
	private ContentService contentService;

	@Bean
	public CommandLineRunner printContentSummary() {
		return args -> System.out.println("\n" + contentService.getSummary().toString(4) + "\n");
	}

}
