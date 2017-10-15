package com.opencommunity.openTeamOneServer;

import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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

	private static User getCurrentSession(HttpServletRequest request, UserRepository userRepository) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		return session == null ? null : userRepository.findOne(session.userId);
	}

	public static User getCurrentUser(HttpServletRequest request, UserRepository userRepository) {
		User user = getCurrentSession(request, userRepository);
		return user == null || user.personId == null || !user.hasUserRole ? null : user;
	}

	public static User getCurrentAdminUser(HttpServletRequest request, UserRepository userRepository) {
		User user = getCurrentSession(request, userRepository);
		return user == null || !user.hasAdminRole ? null : user;
	}

	private static String errorJsonString = "{}";
	private static Resource errorJsonResource = new ByteArrayResource(errorJsonString.getBytes());

	public static ResponseEntity<Resource> httpResourceResponse(HttpStatus httpStatus) {
		return ResponseEntity.status(httpStatus)
				.contentType(MediaType.APPLICATION_JSON)
				.body(errorJsonResource);
	}

	public static ResponseEntity<Resource> httpResourceResponse(Resource resource, MediaType mediaType) {
		return ResponseEntity.ok()
				.contentType(mediaType)
				.body(resource);
	}

	public static ResponseEntity<String> httpStringResponse(HttpStatus httpStatus) {
		return ResponseEntity.status(httpStatus)
				.contentType(MediaType.APPLICATION_JSON)
				.body(errorJsonString);
	}

	public static ResponseEntity<String> httpStringResponse(JSONObject body) {
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(body.toString());
	}

}
