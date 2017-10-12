package com.opencommunity.openTeamOneServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/sap/hana/xs/formLogin")
public class SessionApi {

	@Autowired
	private UserRepository userRepository;

	private static final String unsafeToken = "unsafe";
	private static final String safeToken = "4711";

	@RequestMapping(method = RequestMethod.GET, value = "/token.xsjs")
	@ResponseBody
	public ResponseEntity<String> token(HttpServletRequest request) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		//
		String body = "{}";
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		if ("Fetch".equals(request.getHeader("X-CSRF-Token")))
			httpHeaders.set("X-CSRF-Token", session == null ? unsafeToken : safeToken);
		return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/login.xscfunc")
	@ResponseBody
	public ResponseEntity<String> login(HttpServletRequest request, @RequestBody String input) {
		Map<String, String> formData = Util.split(input);
		String userId = formData.get("xs-username").toLowerCase();
		String password = formData.get("xs-password");
		User sessionUser = userId != null ? userRepository.findOne(userId) : null;
		Session session = sessionUser != null && sessionUser.matches(password) ? Session.newSession(userId) : null;
		//
		String body = "{\"login\":" + Boolean.toString(session != null) + ",\"pwdChange\":false,\"username\":\"" + userId + "\"}";
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		if (session != null)
			httpHeaders.set("Set-Cookie", session.sessionId);
		if ("Fetch".equals(request.getHeader("X-CSRF-Token")))
			httpHeaders.set("X-CSRF-Token", session == null ? unsafeToken : safeToken);
		return new ResponseEntity<>(body, httpHeaders, session == null ? HttpStatus.FORBIDDEN : HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/logout.xscfunc")
	@ResponseBody
	public ResponseEntity<String> logout(HttpServletRequest request) {
		String sessionId = request.getHeader("Cookie");
		if (sessionId != null)
			Session.invalidateSession(sessionId);
		//
		String body = "{}";
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		if ("Fetch".equals(request.getHeader("X-CSRF-Token")))
			httpHeaders.set("X-CSRF-Token", unsafeToken);
		return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/pwchange.xscfunc")
	@ResponseBody
	public ResponseEntity<String> pwchange(HttpServletRequest request, @RequestBody String input) {
		Map<String, String> formData = Util.split(input);
		String userId = formData.get("xs-username").toLowerCase();
		String passwordNew = formData.get("xs-password-new");
		String passwordOld = formData.get("xs-password-old");
		boolean passwordOldConfirmed = false;
		if (passwordOld == null) {
			String sessionId = request.getHeader("Cookie");
			Session session = sessionId == null ? null : Session.getSession(sessionId);
			passwordOldConfirmed = session != null & userId.equals(session.userId);
		}
		User sessionUser = userId != null ? userRepository.findOne(userId) : null;
		Session session = sessionUser != null && passwordNew != null && (sessionUser.matches(passwordOld) || passwordOldConfirmed) ? Session.newSession(userId) : null;
		if (session != null) {
			sessionUser.setPassword(passwordNew);
			userRepository.save(sessionUser);
		}
		//
		String body = "{\"login\":" + Boolean.toString(session != null) + ",\"pwdChange\":false,\"username\":\"" + userId + "\"}";
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		if (session != null)
			httpHeaders.set("Set-Cookie", session.sessionId);
		if ("Fetch".equals(request.getHeader("X-CSRF-Token")))
			httpHeaders.set("X-CSRF-Token", session == null ? unsafeToken : safeToken);
		return new ResponseEntity<>(body, httpHeaders, session == null ? HttpStatus.FORBIDDEN : HttpStatus.OK);
	}

}
