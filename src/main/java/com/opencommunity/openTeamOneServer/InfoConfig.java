package com.opencommunity.openTeamOneServer;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.info.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import java.net.*;

@Component
public class InfoConfig implements InfoContributor {

	@Autowired
	@Qualifier("serviceInstanceId")
	private String serviceInstanceId;

	@Override
	public void contribute(Info.Builder builder) {
		builder.withDetail("serviceInstanceId", serviceInstanceId);
	}

	@Bean(name="serviceInstanceId")
	public String serviceInstanceId() {
		try {
			String hostName = InetAddress.getLocalHost().getHostName();
			return hostName.startsWith("teamone-") ? hostName : "teamone-" + hostName;
		} catch (Exception ignored) { }
		return "teamone";
	}

}
