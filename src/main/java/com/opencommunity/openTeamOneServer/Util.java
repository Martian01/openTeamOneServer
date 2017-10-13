package com.opencommunity.openTeamOneServer;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Util {

	public static String getUuid() {
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}

	public static Map<String, String> split(String url) {
		Map<String, String> map = new HashMap<>();
		String[] assignments = url.split("&");
		for (int i = 0; i < assignments.length; i++) {
			String[] parts = assignments[i].split("=");
			if (parts.length == 2) {
				try {
					map.put(parts[0], URLDecoder.decode(parts[1], "UTF-8"));
				} catch (Exception e) { }
			}
		}
		return map;
	}

	public static ResponseEntity<String> defaultResponse(HttpStatus httpStatus) {
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>("{}", httpHeaders, httpStatus);
	}

}
