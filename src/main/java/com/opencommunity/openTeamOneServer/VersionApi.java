package com.opencommunity.openTeamOneServer;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sap/sports/pe/api/messaging")
public class VersionApi {

	@RequestMapping(method = RequestMethod.GET, value = "/versions")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> versions(HttpServletRequest request) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultMapResponse(HttpStatus.UNAUTHORIZED);
		//
		Map<String, Object> teamOneAndroid = new HashMap<>();
		teamOneAndroid.put("required", 360);
		teamOneAndroid.put("recommended", 378);
		Map<String, Object> clients = new HashMap<>();
		clients.put("teamOneAndroid", teamOneAndroid);
		Map<String, Object> body = new HashMap<>();
		body.put("versions", new String[] {"V2"});
		body.put("current", "V2");
		body.put("clients", clients);
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/test")
	@ResponseBody
	public ResponseEntity<String> test(HttpServletRequest request) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		String body = "{\"versions\":[\"V2\"],\"current\":\"V2\",\"clients\":{\"teamOneAndroid\":{\"required\":360,\"recommended\":378}}}";
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
	}

}
