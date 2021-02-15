package com.opencommunity.openTeamOneServer.api;

import com.opencommunity.openTeamOneServer.data.*;
import com.opencommunity.openTeamOneServer.persistence.*;
import com.opencommunity.openTeamOneServer.util.*;
import org.json.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.*;

@RestController
public class RootApi {

	@Autowired
	private TenantParameterRepository tenantParameterRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RestLib restLib;

	@RequestMapping(method = RequestMethod.GET, value = "/sap/sports/pe/api/messaging/versions")
	public ResponseEntity<String> versions(HttpServletRequest request) throws JSONException {
		Session session = restLib.getSession(request);
		User user = session == null ? restLib.getBasicAuthContact(request) : restLib.getSessionContact(session); // fallback to Basic Auth
		if (user == null)
			return restLib.httpStaleSessionResponse(request);
		//
		JSONObject teamOneAndroid = new JSONObject();
		teamOneAndroid.put("required", 1);
		teamOneAndroid.put("recommended", 1);
		JSONObject clients = new JSONObject();
		clients.put("teamOneAndroid", teamOneAndroid);
		JSONArray versions = new JSONArray();
		versions.put("V2");
		JSONObject body = new JSONObject();
		body.put("current", "V2");
		body.put("versions", versions);
		body.put("clients", clients);
		//
		return restLib.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/sap/sports/fnd/db/services/public/xs/token.xsjs")
	public ResponseEntity<String> fndToken(HttpServletRequest request) {
		return restLib.httpCsrfResponse;
	}

	@RequestMapping(method = RequestMethod.GET, value = {"", "/", "/admin", "/admin/"})
	public ResponseEntity<String> root(HttpServletRequest request) {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionUser(session);
		return restLib.httpForwardResponse(request, user, null);
	}

}
