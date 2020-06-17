package com.opencommunity.openTeamOneServer.api;

import com.opencommunity.openTeamOneServer.data.Person;
import com.opencommunity.openTeamOneServer.data.Session;
import com.opencommunity.openTeamOneServer.data.User;
import com.opencommunity.openTeamOneServer.persistence.PersonRepository;
import com.opencommunity.openTeamOneServer.persistence.UserRepository;
import com.opencommunity.openTeamOneServer.util.JsonUtil;
import com.opencommunity.openTeamOneServer.util.RestLib;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/sap/hana/xs/formLogin")
public class SessionApi {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private RestLib restLib;

	@RequestMapping(method = RequestMethod.GET, value = "/token.xsjs")
	public ResponseEntity<String> loginToken(HttpServletRequest request) {
		return restLib.httpCsrfResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/login.xscfunc")
	public ResponseEntity<String> login(HttpServletRequest request, @RequestBody String requestBody) throws JSONException {
		Session session = null;
		Map<String, String> formData = restLib.splitQueryString(requestBody);
		String userId = formData.get("xs-username");
		String password = formData.get("xs-password");
		if (userId == null || password == null)
			return restLib.httpForbiddenSessionResponse;
		userId = userId.toLowerCase();
		User user = userRepository.findById(userId).orElse(null);
		if (user == null)
			return restLib.httpForbiddenSessionResponse;
		if (user.hasUserRole && user.personId != null) {
			Person person = personRepository.findById(user.personId).orElse(null);
			if (person != null && user.matches(password))
				session = Session.newSession(userId, iosMode(request));
		}
		return session == null ? restLib.httpForbiddenSessionResponse : sessionResponse(session, withCsrfToken(request));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/pwchange.xscfunc")
	public ResponseEntity<String> pwchange(HttpServletRequest request, @RequestBody String requestBody) throws JSONException {
		Session session = null;
		Map<String, String> formData = restLib.splitQueryString(requestBody);
		String userId = formData.get("xs-username");
		String passwordNew = formData.get("xs-password-new");
		String passwordOld = formData.get("xs-password-old");
		if (userId == null || passwordNew == null)
			return restLib.httpForbiddenSessionResponse;
		userId = userId.toLowerCase();
		User user = userRepository.findById(userId).orElse(null);
		if (user == null)
			return restLib.httpForbiddenSessionResponse;
		boolean passwordOldConfirmed = false;
		if (passwordOld == null) {
			String oldSessionId = restLib.getSessionId(request);
			if (oldSessionId != null) {
				Session oldSession = Session.getSession(oldSessionId);
				if (oldSession != null) {
					User oldSessionUser = userRepository.findById(oldSession.userId).orElse(null);
					if (oldSessionUser != null)
						passwordOldConfirmed = userId.equals(oldSession.userId);
				}
			}
		}
		if (passwordOldConfirmed || user.matches(passwordOld)) {
			session = Session.newSession(userId, iosMode(request));
			user.setPassword(passwordNew);
			userRepository.save(user);
		}
		//
		return session == null ? restLib.httpForbiddenSessionResponse : sessionResponse(session, withCsrfToken(request));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/logout.xscfunc")
	public ResponseEntity<String> logout(HttpServletRequest request) {
		String sessionId = restLib.getSessionId(request);
		if (sessionId != null)
			Session.invalidateSession(sessionId);
		//
		return restLib.httpNoSessionResponse;
	}

	/* helper functions */

	private boolean iosMode(HttpServletRequest request) {
		String header = request.getHeader("user-agent");
		return header != null && (header.contains("iPhone") || header.contains("iPad"));
	}

	private boolean withCsrfToken(HttpServletRequest request) {
		String header = request.getHeader("x-csrf-token");
		return header != null && header.toLowerCase().equals("fetch");
	}

	/*
	private void logHeaders(HttpServletRequest request) {
		Enumeration<String> headers = request.getHeaderNames();
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			System.out.println("# " + header + "\n");
			Enumeration<String> values = request.getHeaders(header);
			while (values.hasMoreElements())
				System.out.println(">   " + values.nextElement() + "\n");
		}
	}
	*/

	private ResponseEntity<String> sessionResponse(Session session, boolean withCsrfToken) throws JSONException {
		JSONObject body = new JSONObject();
		body.put("login", true);
		body.put("pwdChange", false);
		JsonUtil.put(body, "username", session.userId);
		//
		return withCsrfToken ?
				ResponseEntity.status(HttpStatus.OK)
						.header("Set-Cookie", restLib.getSessionCookie(session.sessionId))
						.header("x-csrf-token", restLib.defaultCsrfToken) // iOS app wants lower case header
						.contentType(MediaType.APPLICATION_JSON)
						.body(body.toString()) :
				ResponseEntity.status(HttpStatus.OK)
						.header("Set-Cookie", restLib.getSessionCookie(session.sessionId))
						.contentType(MediaType.APPLICATION_JSON)
						.body(body.toString());
	}
}
