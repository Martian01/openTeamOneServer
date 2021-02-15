package com.opencommunity.openTeamOneServer.api;

import com.opencommunity.openTeamOneServer.data.*;
import com.opencommunity.openTeamOneServer.persistence.*;
import com.opencommunity.openTeamOneServer.util.*;
import org.json.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.*;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/ui")
public class ServiceUiApi {

	@Autowired
	private TenantParameterRepository tenantParameterRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private ContentService contentService;
	@Autowired
	private RestLib restLib;

	/* Services with UI control (forwarding responses) */

	@RequestMapping(method = RequestMethod.POST, value = "/login")
	public ResponseEntity<String> login(HttpServletRequest request, @RequestBody String requestBody) throws JSONException {
		Session session = null;
		User user = null;
		Map<String, String> formData = restLib.splitQueryString(requestBody);
		String userId = formData.get("username");
		String password = formData.get("password");
		String forward = formData.get("forward");
		if (userId != null) {
			userId = userId.toLowerCase();
			user = userRepository.findTopByUserId(userId);
			if (user != null && user.matches(password))
				session = Session.newSession(userId, false);
		}
		//
		return ResponseEntity.status(HttpStatus.SEE_OTHER)
				.header("Location", forward == null ? restLib.getDefaultTarget(request, session == null ? null : user) : forward)
				.header("Set-Cookie", restLib.getSessionCookie(session == null ? null : session.sessionId))
				.contentType(MediaType.TEXT_PLAIN)
				.body(null);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/logout")
	public ResponseEntity<String> logout(HttpServletRequest request, @RequestBody String requestBody) throws JSONException {
		Map<String, String> formData = restLib.splitQueryString(requestBody);
		String forward = formData.get("forward");
		//
		String sessionId = restLib.getSessionId(request);
		if (sessionId != null)
			Session.invalidateSession(sessionId);
		//
		return ResponseEntity.status(HttpStatus.SEE_OTHER)
				.header("Location", forward == null ? restLib.getDefaultTarget(request, null) : forward)
				.header("Set-Cookie", restLib.getSessionCookie(null))
				.contentType(MediaType.TEXT_PLAIN)
				.body(null);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/snapshot/save")
	public ResponseEntity<String> snapshotSave(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Map<String, String> formData = restLib.splitQueryString(requestBody);
		String filename = formData.get("filename");
		String forward = formData.get("forward");
		if (filename == null)
			return restLib.httpBadRequestResponse;
		File file = StreamUtil.getFile(tenantParameterRepository, SymbolicFile.DIRECTORY_SNAPSHOTS, filename);
		if (file == null)
			return restLib.httpInternalErrorResponse;
		JSONObject jsonContent = contentService.exportToJson();
		String stringContent = jsonContent.toString();
		StreamUtil.writeFile(stringContent.getBytes("UTF-8"), file);
		//
		return restLib.httpForwardResponse(request, user, forward);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/snapshot/load")
	public ResponseEntity<String> snapshotLoad(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Map<String, String> formData = restLib.splitQueryString(requestBody);
		String filename = formData.get("filename");
		boolean includeConfiguration = "on".equals(formData.get("config"));
		String forward = formData.get("forward");
		if (filename == null)
			return restLib.httpBadRequestResponse;
		File file = StreamUtil.getFile(tenantParameterRepository, SymbolicFile.DIRECTORY_SNAPSHOTS, filename);
		if (file == null)
			return restLib.httpInternalErrorResponse;
		if (!file.canRead())
			return restLib.httpNotFoundResponse;
		String stringContent = new String(StreamUtil.readFile(file), "UTF-8");
		JSONObject jsonContent = new JSONObject(stringContent);
		contentService.importFromJson(jsonContent, true, includeConfiguration, user.userId);
		//
		return restLib.httpForwardResponse(request, user, forward);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/tenant/parameter")
	public ResponseEntity<String> tenantParameter(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Map<String, String> formData = restLib.splitQueryString(requestBody);
		String key = formData.get("name");
		String value = formData.get("value");
		String forward = formData.get("forward");
		if (key == null || value == null)
			return restLib.httpBadRequestResponse;
		TenantParameter tp = new TenantParameter(key, value);
		tenantParameterRepository.save(tp);
		//
		if ("dataDirectory".equals(tp.name))
			contentService.prepareDataDirectory(tp.value);
		//
		return restLib.httpForwardResponse(request, user, forward);
	}

}
