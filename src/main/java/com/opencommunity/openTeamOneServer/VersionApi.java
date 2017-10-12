package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/sap/sports/pe/api/messaging")
public class VersionApi {

	@RequestMapping(method = RequestMethod.GET, value = "/versions")
	@ResponseBody
	public ResponseEntity<String> versions(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
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
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body.toString(), httpHeaders, HttpStatus.OK);
	}

}
