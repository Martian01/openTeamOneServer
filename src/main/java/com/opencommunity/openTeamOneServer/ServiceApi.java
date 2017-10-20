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
import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/svc")
public class ServiceApi {

	@Autowired
	private TenantParameterRepository tenantParameterRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PersonRepository personRepository;

	/* Services with UI control (forwarding responses) */

	@RequestMapping(method = RequestMethod.POST, value = "/ui/login")
	public ResponseEntity<String> uiLogin(HttpServletRequest request, @RequestBody String input) throws JSONException {
		Map<String, String> formData = Util.splitQueryString(input);
		String userId = formData.get("username");
		String password = formData.get("password");
		String forward = formData.get("forward");
		if (userId != null)
			userId = userId.toLowerCase();
		User user = userId == null ? null : userRepository.findOne(userId);
		Session session = user != null && user.matches(password) ? Session.newSession(userId) : null;
		//
		return ResponseEntity.status(HttpStatus.SEE_OTHER)
				.header("Location", forward == null ? Util.getDefaultTarget(tenantParameterRepository, user) : forward)
				.header("Set-Cookie", Util.getSessionCookie(session == null ? null : session.sessionId))
				.contentType(MediaType.TEXT_PLAIN)
				.body(null);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/ui/logout")
	public ResponseEntity<String> uiLogout(HttpServletRequest request, @RequestBody String input) throws JSONException {
		Map<String, String> formData = Util.splitQueryString(input);
		String forward = formData.get("forward");
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		String sessionId = Util.getSessionId(request);
		if (sessionId != null) {
			Session.invalidateSession(sessionId);
			httpHeaders.set("Set-Cookie", Util.getSessionCookie(null));
		}
		//
		return Util.httpForwardResponse(tenantParameterRepository, null, forward);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/ui/snapshot/save")
	public ResponseEntity<String> snapshotSave(HttpServletRequest request, @RequestBody String input) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		Map<String, String> formData = Util.splitQueryString(input);
		String filename = formData.get("filename");
		String forward = formData.get("forward");
		if (filename == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		File directory = Util.getDataDirectory(tenantParameterRepository, "snapshots");
		if (directory == null)
			return Util.httpStringResponse(HttpStatus.INTERNAL_SERVER_ERROR);
		File file = new File(directory, filename);
		JSONObject jsonContent = ContentService.exportToJson();
		String stringContent = jsonContent.toString();
		Util.writeFile(stringContent.getBytes("UTF-8"), file);
		//
		return Util.httpForwardResponse(tenantParameterRepository, user, forward);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/ui/snapshot/load")
	public ResponseEntity<String> snapshotLoad(HttpServletRequest request, @RequestBody String input) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		Map<String, String> formData = Util.splitQueryString(input);
		String filename = formData.get("filename");
		boolean includeConfiguration = "on".equals(formData.get("config"));
		String forward = formData.get("forward");
		if (filename == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		File directory = Util.getDataDirectory(tenantParameterRepository, "snapshots");
		if (directory == null)
			return Util.httpStringResponse(HttpStatus.INTERNAL_SERVER_ERROR);
		File file = new File(directory, filename);
		if (!file.canRead())
			return Util.httpStringResponse(HttpStatus.NOT_FOUND);
		String stringContent = new String(Util.readFile(file), "UTF-8");
		JSONObject jsonContent = new JSONObject(stringContent);
		ContentService.importFromJson(jsonContent, true, includeConfiguration, user.userId);
		//
		return Util.httpForwardResponse(tenantParameterRepository, user, forward);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/ui/tenant/parameter")
	public ResponseEntity<String> tenantParameter(HttpServletRequest request, @RequestBody String input) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		Map<String, String> formData = Util.splitQueryString(input);
		String key = formData.get("key");
		String value = formData.get("value");
		String forward = formData.get("forward");
		if (key == null || value == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		TenantParameter tp = new TenantParameter(key, value);
		tenantParameterRepository.save(tp);
		//
		return Util.httpForwardResponse(tenantParameterRepository, user, forward);
	}

	/* AJAX Services (JSON responses) */

	@RequestMapping(method = RequestMethod.GET, value = "/session/info")
	public ResponseEntity<String> uiSession(HttpServletRequest request) throws JSONException {
		String sessionId = Util.getSessionId(request);
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		//
		JSONObject body = new JSONObject();
		if (session != null) {
			body.put("session", session.toJson());
			User user = session.userId == null ? null : userRepository.findOne(session.userId);
			if (user != null) {
				body.put("user", user.toJson());
				Person person = user.personId == null ? null : personRepository.findOne(user.personId);
				if (person != null)
					body.put("person", person.toJson());
			}
		}
		//
		return Util.httpStringResponse(body);
	}

}
