package com.opencommunity.openTeamOneServer;

import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Util {

	public static String getUuid() {
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}

	public static Map<String, String> splitQueryString(String queryString) {
		Map<String, String> map = new HashMap<>();
		if (queryString != null) {
			String[] assignments = queryString.split("&");
			for (String assignment : assignments) {
				String[] parts = assignment.trim().split("=");
				if (parts.length == 2)
					try {
						map.put(parts[0], URLDecoder.decode(parts[1], "UTF-8"));
					} catch (Exception e) {
					}
			}
		}
		return map;
	}

	public static Map<String, String> splitCookieString(String cookieString) {
		Map<String, String> map = new HashMap<>();
		if (cookieString != null) {
			String[] components = cookieString.split(";");
			for (String component : components) {
				String[] parts = component.trim().split("=");
				if (parts.length == 2)
					map.put(parts[0], parts[1]);
			}
		}
		return map;
	}

	private static final String SESSION_COOKIE_NAME = "sid";

	public static String getSessionCookie(String sessionId) {
		return sessionId == null
				? SESSION_COOKIE_NAME + "=_; HttpOnly; Path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT"
				: SESSION_COOKIE_NAME + "=" + sessionId + "; HttpOnly; Path=/";
	}

	public static String getSessionId(HttpServletRequest request) {
		String cookieString = request.getHeader("Cookie");
		return cookieString == null ? null : splitCookieString(cookieString).get(SESSION_COOKIE_NAME);
	}

	public static User getSessionUser(HttpServletRequest request, UserRepository userRepository) {
		String sessionId = getSessionId(request);
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		return session == null ? null : userRepository.findOne(session.userId);
	}

	public static User getSessionContact(HttpServletRequest request, UserRepository userRepository) {
		User user = getSessionUser(request, userRepository);
		return user == null || user.personId == null || !user.hasUserRole ? null : user;
	}

	public static User getSessionAdmin(HttpServletRequest request, UserRepository userRepository) {
		User user = getSessionUser(request, userRepository);
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

	public static ResponseEntity<String> httpStringResponse(JSONObject body, HttpStatus httpStatus) {
		return ResponseEntity.status(httpStatus)
				.contentType(MediaType.APPLICATION_JSON)
				.body(body.toString());
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

	public static ResponseEntity<String> httpForwardResponse(String targetUri) {
		if (targetUri == null)
			return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body("Success");
		return ResponseEntity.status(HttpStatus.SEE_OTHER)
				.header("Location", targetUri)
				.contentType(MediaType.TEXT_PLAIN)
				.body(null);
	}

	private static final int MAX_BUFFER_SIZE = 2 * 1024 * 1024;

	private static void pipeStream(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte[MAX_BUFFER_SIZE];
		int bytesRead = inputStream.read(buffer, 0, MAX_BUFFER_SIZE);
		while (bytesRead > 0) {
			outputStream.write(buffer, 0, bytesRead);
			bytesRead = inputStream.read(buffer, 0, MAX_BUFFER_SIZE);
		}
	}

	public static void writeFile(InputStream inputStream, File targetFile) throws IOException {
		if (targetFile.exists())
			targetFile.delete();
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
		Util.pipeStream(inputStream, outputStream);
		outputStream.close();
	}

	public static void writeFile(byte[] content, File targetFile) throws IOException {
		if (targetFile.exists())
			targetFile.delete();
		FileOutputStream outputStream = new FileOutputStream(targetFile);
		outputStream.write(content, 0 , content.length);
		outputStream.close();
	}

	public static byte[] readFile(File file) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		pipeStream(inputStream, outputStream);
		outputStream.close();
		inputStream.close();
		return outputStream.toByteArray();
	}

}
