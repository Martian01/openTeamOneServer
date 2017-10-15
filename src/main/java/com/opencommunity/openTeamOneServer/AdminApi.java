package com.opencommunity.openTeamOneServer;

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
@RequestMapping("/admin")
public class AdminApi {

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(method = RequestMethod.GET, value = "/db/export")
	public ResponseEntity<String> versions(HttpServletRequest request) throws JSONException {
		User user = Util.getCurrentAdminUser(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		body.put("export", "tbd");
		//
		return Util.httpStringResponse(body);
	}

}
