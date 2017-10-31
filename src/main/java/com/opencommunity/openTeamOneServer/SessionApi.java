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

	private static final String unsafeCsrfToken = "unsafe";

	@RequestMapping(method = RequestMethod.GET, value = "/token.xsjs")
	public ResponseEntity<String> token(HttpServletRequest request) {
		String sessionId = Util.getSessionId(request);
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		if ("Fetch".equals(request.getHeader("X-CSRF-Token")))
			httpHeaders.set("X-CSRF-Token", user == null ? unsafeCsrfToken : session.getNewCsrfToken());
		return new ResponseEntity<>("{}", httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/login.xscfunc")
	public ResponseEntity<String> login(HttpServletRequest request, @RequestBody String requestBody) throws JSONException {
		Map<String, String> formData = Util.splitQueryString(requestBody);
		String userId = formData.get("xs-username");
		String password = formData.get("xs-password");
		if (userId != null)
			userId = userId.toLowerCase();
		User user = userId != null ? userRepository.findOne(userId) : null;
		Person person = user != null && user.hasUserRole && user.personId != null ? personRepository.findOne(user.personId) : null;
		Session session = person != null && user.matches(password) ? Session.newSession(userId) : null;
		//
		JSONObject body = new JSONObject();
		body.put("login", session != null);
		body.put("pwdChange", false);
		JsonUtil.put(body, "username", userId);
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.set("Set-Cookie", Util.getSessionCookie(session == null ? null : session.sessionId));
		if ("Fetch".equals(request.getHeader("X-CSRF-Token")))
			httpHeaders.set("X-CSRF-Token", session == null ? unsafeCsrfToken : session.getNewCsrfToken());
		return new ResponseEntity<>(body.toString(), httpHeaders, session == null ? HttpStatus.FORBIDDEN : HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/pwchange.xscfunc")
	public ResponseEntity<String> pwchange(HttpServletRequest request, @RequestBody String requestBody) throws JSONException {
		Map<String, String> formData = Util.splitQueryString(requestBody);
		String userId = formData.get("xs-username").toLowerCase();
		String passwordNew = formData.get("xs-password-new");
		String passwordOld = formData.get("xs-password-old");
		boolean passwordOldConfirmed = false;
		if (passwordOld == null) {
			String oldSessionId = Util.getSessionId(request);
			Session oldSession = oldSessionId == null ? null : Session.getSession(oldSessionId);
			User oldSessionUser = oldSession == null ? null : userRepository.findOne(oldSession.userId);
			passwordOldConfirmed = oldSessionUser != null & userId.equals(oldSession.userId);
		}
		User sessionUser = userId != null ? userRepository.findOne(userId) : null;
		Session session = sessionUser != null && passwordNew != null && (sessionUser.matches(passwordOld) || passwordOldConfirmed) ? Session.newSession(userId) : null;
		if (session != null) {
			sessionUser.setPassword(passwordNew);
			userRepository.save(sessionUser);
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
		if ("Fetch".equals(request.getHeader("X-CSRF-Token")))
			httpHeaders.set("X-CSRF-Token", session == null ? unsafeCsrfToken : session.getNewCsrfToken());
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
