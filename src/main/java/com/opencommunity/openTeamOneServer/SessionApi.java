package com.opencommunity.openTeamOneServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

	// Note: CSRF protection is irrelevant for mobile clients, and taken care of for browsers by SameSite cookies

	private final ResponseEntity<String> httpCsrfResponse = ResponseEntity.status(HttpStatus.OK)
			.header("x-csrf-token", "792E926C333D4BB88AF219F83CDA2CE1") // iOS app wants lower case header
			.contentType(MediaType.TEXT_PLAIN)
			.body(null);

	@RequestMapping(method = RequestMethod.GET, value = "/token.xsjs")
	public ResponseEntity<String> token(HttpServletRequest request) {
		return request.getHeader("Authorization") != null ? httpCsrfResponse : Util.httpOkResponse; // iOS vs. Android app
	}

	@RequestMapping(method = RequestMethod.POST, value = "/login.xscfunc")
	public ResponseEntity<String> login(HttpServletRequest request, @RequestBody String requestBody) throws JSONException {
		Session session = null;
		Map<String, String> formData = Util.splitQueryString(requestBody);
		String userId = formData.get("xs-username");
		String password = formData.get("xs-password");
		if (userId != null) {
			userId = userId.toLowerCase();
			User user = userRepository.findOne(userId);
			if (user != null && user.hasUserRole && user.personId != null) {
				Person person = personRepository.findOne(user.personId);
				if (person != null && user.matches(password))
					session = Session.newSession(userId, request.getHeader("X-CSRF-TOKEN") != null);
			}
		}
		//
		JSONObject body = new JSONObject();
		body.put("login", session != null);
		body.put("pwdChange", false);
		JsonUtil.put(body, "username", userId);
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.set("Set-Cookie", Util.getSessionCookie(session == null ? null : session.sessionId));
		return new ResponseEntity<>(body.toString(), httpHeaders, session == null ? HttpStatus.FORBIDDEN : HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/pwchange.xscfunc")
	public ResponseEntity<String> pwchange(HttpServletRequest request, @RequestBody String requestBody) throws JSONException {
		Session session = null;
		Map<String, String> formData = Util.splitQueryString(requestBody);
		String userId = formData.get("xs-username");
		String passwordNew = formData.get("xs-password-new");
		String passwordOld = formData.get("xs-password-old");
		if (userId != null) {
			userId = userId.toLowerCase();
			User user = userRepository.findOne(userId);
			boolean passwordOldConfirmed = false;
			if (passwordOld == null) {
				String oldSessionId = Util.getSessionId(request);
				if (oldSessionId != null) {
					Session oldSession = Session.getSession(oldSessionId);
					if (oldSession != null) {
						User oldSessionUser = userRepository.findOne(oldSession.userId);
						if (oldSessionUser != null)
							passwordOldConfirmed = userId.equals(oldSession.userId);
					}
				}
			}
			if (user != null && passwordNew != null && (passwordOldConfirmed || user.matches(passwordOld)))
				session = Session.newSession(userId, request.getHeader("X-CSRF-TOKEN") != null);
			if (session != null) {
				user.setPassword(passwordNew);
				userRepository.save(user);
			}
		}
		//
		JSONObject body = new JSONObject();
		body.put("login", session != null);
		body.put("pwdChange", false);
		JsonUtil.put(body, "username", userId);
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.set("Set-Cookie", Util.getSessionCookie(session == null ? null : session.sessionId));
		return new ResponseEntity<>(body.toString(), httpHeaders, session == null ? HttpStatus.FORBIDDEN : HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/logout.xscfunc")
	public ResponseEntity<String> logout(HttpServletRequest request) {
		HttpHeaders httpHeaders= new HttpHeaders();
		String sessionId = Util.getSessionId(request);
		if (sessionId != null) {
			Session.invalidateSession(sessionId);
			httpHeaders.set("Set-Cookie", Util.getSessionCookie(null));
		}
		//
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>("{}", httpHeaders, HttpStatus.OK);
	}

}
