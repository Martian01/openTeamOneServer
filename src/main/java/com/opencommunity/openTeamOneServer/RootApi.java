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
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
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
		return Util.httpResponse(body);
	}

	@RequestMapping("*") // does not work for some unknown reason
	public ResponseEntity<String> fallback(HttpServletRequest request) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpResponse(HttpStatus.SERVICE_UNAVAILABLE);
	}

}
