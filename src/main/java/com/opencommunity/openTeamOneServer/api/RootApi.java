package com.opencommunity.openTeamOneServer.api;

import com.opencommunity.openTeamOneServer.data.Session;
import com.opencommunity.openTeamOneServer.data.User;
import com.opencommunity.openTeamOneServer.persistence.TenantParameterRepository;
import com.opencommunity.openTeamOneServer.persistence.UserRepository;
import com.opencommunity.openTeamOneServer.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class RootApi {

	@Autowired
	private TenantParameterRepository tenantParameterRepository;
	@Autowired
	private UserRepository userRepository;

	@RequestMapping(method = RequestMethod.GET, value = "/sap/sports/pe/api/messaging/versions")
	public ResponseEntity<String> versions(HttpServletRequest request) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // fallback to Basic Auth
		if (user == null)
			return Util.httpStaleSessionResponse(request);
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
		return Util.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/sap/sports/fnd/db/services/public/xs/token.xsjs")
	public ResponseEntity<String> fndToken(HttpServletRequest request) {
		return Util.httpCsrfResponse;
	}

	@RequestMapping(method = RequestMethod.GET, value = {"", "/", "/admin", "/admin/"})
	public ResponseEntity<String> root(HttpServletRequest request) {
		Session session = Util.getSession(request);
		User user = Util.getSessionUser(session, userRepository);
		return Util.httpForwardResponse(request, tenantParameterRepository, user, null);
	}

}
