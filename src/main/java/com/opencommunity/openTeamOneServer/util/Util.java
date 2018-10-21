package com.opencommunity.openTeamOneServer.util;

import com.opencommunity.openTeamOneServer.data.Session;
import com.opencommunity.openTeamOneServer.data.TenantParameter;
import com.opencommunity.openTeamOneServer.data.User;
import com.opencommunity.openTeamOneServer.persistence.TenantParameterRepository;
import com.opencommunity.openTeamOneServer.persistence.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	/* Misc */

	public static boolean equal(Object x, Object y) {
		return (x == null && y == null) || (x != null && y != null && x.equals(y));
	}

	public static String getUuid() {
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}

	public static String getRandomString(int length) {
		return UUID.randomUUID().toString().replace("-", "").substring(0, length).toLowerCase();
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

	/* Basic Auth services (for the iOS app) */

	public static String[] splitBasicAuthHeader(String authHeader) {
		if (authHeader != null && authHeader.startsWith("Basic ")) {
			try {
				String decodedHeader = URLDecoder.decode(new String(Base64.decodeFast(authHeader.substring(6)), "UTF-8"), "UTF-8");
				if (decodedHeader != null)
					return decodedHeader.split(":");
			} catch (Exception e) { }
		}
		return null;
	}


	public static User getBasicAuthUser(HttpServletRequest request, UserRepository userRepository) {
		String[] credentials = Util.splitBasicAuthHeader(request.getHeader("Authorization"));
		if (credentials == null || credentials.length != 2)
			return null;
		User user = userRepository.findById(credentials[0].toLowerCase()).orElse(null);
		if (user == null)
			return null;
		return user.matches(credentials[1]) ? user : null;
	}

	public static User getBasicAuthContact(HttpServletRequest request, UserRepository userRepository) {
		User user = getBasicAuthUser(request, userRepository);
		return user == null || user.personId == null || !user.hasUserRole ? null : user;
	}

	/* User sessions */

	private static final String SESSION_COOKIE_NAME = "sid";

	public static String getSessionCookie(String sessionId) {
		return sessionId == null
				? SESSION_COOKIE_NAME + "=_; Path=/; HttpOnly; SameSite=Strict; expires=Thu, 01 Jan 1970 00:00:00 GMT"
				: SESSION_COOKIE_NAME + "=" + sessionId + "; Path=/; HttpOnly; SameSite=Strict";
	}

	public static String getSessionId(HttpServletRequest request) {
		String cookieString = request.getHeader("Cookie");
		return cookieString == null ? null : splitCookieString(cookieString).get(SESSION_COOKIE_NAME);
	}

	public static Session getSession(HttpServletRequest request) {
		String sessionId = getSessionId(request);
		return sessionId == null ? null : Session.getSession(sessionId);
	}

	private static final Pattern serverUrlPattern = Pattern.compile("^(\\w+://[^/]+)");

	public static String getServerUrl(HttpServletRequest request) {
		String requestUrl = request.getRequestURL().toString();
		Matcher m = serverUrlPattern.matcher(requestUrl);
		return m.find() ? m.group(1) : "";
	}

	public static User getSessionUser(Session session, UserRepository userRepository) {
		return session == null ? null : userRepository.findById(session.userId).orElse(null);
	}

	public static User getSessionContact(Session session, UserRepository userRepository) {
		User user = getSessionUser(session, userRepository);
		return user == null || user.personId == null || !user.hasUserRole ? null : user;
	}

	public static User getSessionAdmin(Session session, UserRepository userRepository) {
		User user = getSessionUser(session, userRepository);
		return user == null || !user.hasAdminRole ? null : user;
	}

	/* HTTP String responses */

	public static ResponseEntity<String> httpOkResponse = ResponseEntity.status(HttpStatus.OK)
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<String> httpUnauthorizedResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<String> httpForbiddenResponse = ResponseEntity.status(HttpStatus.FORBIDDEN)
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<String> httpBadRequestResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<String> httpInternalErrorResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<String> httpNotFoundResponse = ResponseEntity.status(HttpStatus.NOT_FOUND)
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<String> httpGoneResponse = ResponseEntity.status(HttpStatus.GONE)
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<String> httpCreatedResponse = ResponseEntity.status(HttpStatus.CREATED)
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<String> httpNoSessionResponse = ResponseEntity.status(HttpStatus.OK)
			.header("Set-Cookie", getSessionCookie(null))
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<String> httpForbiddenSessionResponse = ResponseEntity.status(HttpStatus.FORBIDDEN)
			.header("Set-Cookie", getSessionCookie(null))
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<String> httpResponse(JSONObject body, HttpStatus httpStatus) {
		return ResponseEntity.status(httpStatus)
				.contentType(MediaType.APPLICATION_JSON)
				.body(body.toString());
	}

	public static ResponseEntity<String> httpOkResponse(JSONObject body) {
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(body.toString());
	}

	public static ResponseEntity<String> httpOkResponse(JSONArray body) {
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(body.toString());
	}

	public static ResponseEntity<String> httpStaleSessionResponse(HttpServletRequest request) {
		if (Session.iosMode(getSessionId(request)))
			return ResponseEntity.status(HttpStatus.SEE_OTHER)
				.header("Location", getServerUrl(request) + "/sap/hana/xs/formLogin/login.html")
				.contentType(MediaType.TEXT_PLAIN)
				.body(null);
		return httpUnauthorizedResponse;
	}

	public static String getDefaultTarget(HttpServletRequest request, TenantParameterRepository tpr, User user) {
		String parameter = user == null ? "startPageNoLogon" : (user.hasAdminRole ? "startPageAdmin" : (user.hasUserRole ? "startPageUser" : "startPageLogon"));
		TenantParameter tp = tpr.findById(parameter).orElse(null);
		return getServerUrl(request) + (tp == null ? "/default/index.html" : tp.value);
	}

	public static ResponseEntity<String> httpForwardResponse(HttpServletRequest request, TenantParameterRepository tpr, User user, String targetUri) {
		if (targetUri == null)
			targetUri = getDefaultTarget(request, tpr, user);
		return ResponseEntity.status(HttpStatus.SEE_OTHER)
				.header("Location", targetUri)
				.contentType(MediaType.TEXT_PLAIN)
				.body(null);
	}

	/* HTTP Resource responses */

	public static ResponseEntity<Resource> httpUnauthorizedResourceResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<Resource> httpInternalErrorResourceResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<Resource> httpNotFoundResourceResponse = ResponseEntity.status(HttpStatus.NOT_FOUND)
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	public static ResponseEntity<Resource> httpResourceResponse(Resource resource, MediaType mediaType) {
		return ResponseEntity.ok()
				.contentType(mediaType)
				.body(resource);
	}

	/* Files and Streams */

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

	public static File getDataDirectory(TenantParameterRepository tpr, String subdirectory) {
		TenantParameter tp = tpr.findById("dataDirectory").orElse(null);
		if (tp == null)
			return null;
		File directory = new File(tp.value);
		if (subdirectory != null)
			directory = new File(directory, subdirectory);
		directory.mkdirs();
		return directory.isDirectory() ? directory : null;
	}

	public static File getFile(TenantParameterRepository tpr, String subdirectory, String fileId) {
		File directory = getDataDirectory(tpr, subdirectory);
		return directory == null || fileId == null ? null : new File(directory, fileId);
	}

}
