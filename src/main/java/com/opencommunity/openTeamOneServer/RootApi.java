package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class RootApi {

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(method = RequestMethod.GET, value = "/sap/sports/pe/api/messaging/versions")
	public ResponseEntity<String> versions(HttpServletRequest request) throws JSONException {
		User user = Util.getSessionContact(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject teamOneAndroid = new JSONObject();
		teamOneAndroid.put("required", 360);
		teamOneAndroid.put("recommended", 378);
		JSONObject clients = new JSONObject();
		clients.put("teamOneAndroid", teamOneAndroid);
		JSONArray versions = new JSONArray();
		versions.put("V2");
		JSONObject body = new JSONObject();
		body.put("current", "V2");
		body.put("versions", versions);
		body.put("clients", clients);
		//
		return Util.httpStringResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = {"", "/", "/admin", "/admin/"})
	public ResponseEntity<String> admin(HttpServletRequest request) throws JSONException {
		User user = Util.getSessionUser(request, userRepository);
		String page = user == null ? "/login.html" : (user.hasAdminRole ? "/index.html" : "/login2.html");
		String pageStyle = ContentService.getPageStyle();
		if (pageStyle == null)
			pageStyle = "default";
		return Util.httpForwardResponse("/admin/", pageStyle, page); // in theory the URI should be absolute...
	}

}
