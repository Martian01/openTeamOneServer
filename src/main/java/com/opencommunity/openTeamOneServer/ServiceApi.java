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
@RequestMapping("/svc")
public class ServiceApi {

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(method = RequestMethod.POST, value = "/ui/login")
	public ResponseEntity<String> uiLogin(HttpServletRequest request, @RequestBody String input) throws JSONException {
		Map<String, String> formData = Util.splitQueryString(input);
		String userId = formData.get("ui-username");
		String password = formData.get("ui-password");
		String forward = formData.get("ui-forward");
		if (userId != null)
			userId = userId.toLowerCase();
		User user = userId == null ? null : userRepository.findOne(userId);
		Session session = user != null && user.matches(password) ? Session.newSession(userId) : null;
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.set("Set-Cookie", Util.getSessionCookie(session == null ? null : session.sessionId));
		if (forward != null)
			httpHeaders.set("Location", forward);
		httpHeaders.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<>(session == null ? "Error" : "Success", httpHeaders, forward == null ? (session == null ? HttpStatus.FORBIDDEN : HttpStatus.OK) : HttpStatus.SEE_OTHER);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/ui/logout")
	public ResponseEntity<String> uiLogout(HttpServletRequest request, @RequestBody String input) throws JSONException {
		Map<String, String> formData = Util.splitQueryString(input);
		String forward = formData.get("ui-forward");
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		String sessionId = Util.getSessionId(request);
		if (sessionId != null) {
			Session.invalidateSession(sessionId);
			httpHeaders.set("Set-Cookie", Util.getSessionCookie(null));
		}
		//
		if (forward != null)
			httpHeaders.set("Location", forward);
		httpHeaders.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<>("Success", httpHeaders, forward == null ? HttpStatus.OK : HttpStatus.SEE_OTHER);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/db/export")
	public ResponseEntity<String> versions(HttpServletRequest request) throws JSONException {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		body.put("export", "tbd");
		//
		return Util.httpStringResponse(body);
	}

}
