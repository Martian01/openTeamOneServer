package com.opencommunity.openTeamOneServer;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
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

	public static User getCurrentUser(HttpServletRequest request, UserRepository userRepository) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		return user == null || user.personId == null ? null : user;
	}

	public static ResponseEntity<String> httpResponse(HttpStatus httpStatus) {
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>("{}", httpHeaders, httpStatus);
	}

	public static ResponseEntity<String> httpResponse(JSONObject body, HttpStatus httpStatus) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body.toString(), httpHeaders, httpStatus);
	}

	public static ResponseEntity<String> httpResponse(JSONObject body) {
		return httpResponse(body, HttpStatus.OK);
	}

}
