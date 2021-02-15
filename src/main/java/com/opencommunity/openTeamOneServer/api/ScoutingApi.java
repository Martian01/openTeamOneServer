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
public class ScoutingApi {

	@Autowired
	private TenantParameterRepository tenantParameterRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private RestLib restLib;

	@RequestMapping(method = RequestMethod.GET, value = "/sap/sports/sct/api/mobile/versions")
	public ResponseEntity<String> versions(HttpServletRequest request) throws JSONException {
		Session session = restLib.getSession(request);
		User user = session == null ? restLib.getBasicAuthContact(request) : restLib.getSessionContact(session); // fallback to Basic Auth
		if (user == null)
			return restLib.httpStaleSessionResponse(request);
		//
		JSONObject teamOneAndroid = new JSONObject();
		teamOneAndroid.put("required", 146);
		teamOneAndroid.put("recommended", 146);
		JSONObject clients = new JSONObject();
		clients.put("scoutOneAndroid", teamOneAndroid);
		JSONArray versions = new JSONArray();
		versions.put("V1");
		JSONObject body = new JSONObject();
		body.put("current", "V1");
		body.put("versions", versions);
		body.put("clients", clients);
		//
		return restLib.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/sap/sports/sct/api/mobile/v1/service/rest/me")
	public ResponseEntity<String> me(HttpServletRequest request) throws JSONException {
		Session session = restLib.getSession(request);
		User user = session == null ? restLib.getBasicAuthContact(request) : restLib.getSessionContact(session); // fallback to Basic Auth
		if (user == null)
			return restLib.httpStaleSessionResponse(request);
		//
		JSONObject body = new JSONObject();
		Person me = personRepository.findById(user.personId).orElse(null);
		TenantParameter tpName = tenantParameterRepository.findTopByName("tenantName");
		TenantParameter tpPictureId = tenantParameterRepository.findTopByName("tenantPictureId");
		//
		if (me != null)
			body.put("loginPerson", personToJson(me));
		if (tpName != null || tpPictureId != null) {
			JSONObject tenant = new JSONObject();
			if (tpName != null)
				tenant.put("name", tpName.value);
			if (tpPictureId != null)
				tenant.put("pictureId", tpPictureId.value);
			body.put("tenant", tenant);
		}
		//
		return restLib.httpOkResponse(body);
	}

	private JSONObject personToJson(Person person) throws JSONException {
		if (person == null)
			return null;
		JSONObject item = new JSONObject();
		item.put("personId", person.personId);
		JsonUtil.put(item, "lastName", person.lastName);
		JsonUtil.put(item, "firstName", person.firstName);
		JsonUtil.put(item, "pictureId", person.pictureId);
		return item;
	}

}
