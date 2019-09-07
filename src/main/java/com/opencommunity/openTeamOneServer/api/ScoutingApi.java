package com.opencommunity.openTeamOneServer.api;

import com.opencommunity.openTeamOneServer.data.Person;
import com.opencommunity.openTeamOneServer.data.Session;
import com.opencommunity.openTeamOneServer.data.TenantParameter;
import com.opencommunity.openTeamOneServer.data.User;
import com.opencommunity.openTeamOneServer.persistence.PersonRepository;
import com.opencommunity.openTeamOneServer.persistence.TenantParameterRepository;
import com.opencommunity.openTeamOneServer.persistence.UserRepository;
import com.opencommunity.openTeamOneServer.util.JsonUtil;
import com.opencommunity.openTeamOneServer.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ScoutingApi {

	@Autowired
	private TenantParameterRepository tenantParameterRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PersonRepository personRepository;

	@RequestMapping(method = RequestMethod.GET, value = "/sap/sports/sct/api/mobile/versions")
	public ResponseEntity<String> versions(HttpServletRequest request) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // fallback to Basic Auth
		if (user == null)
			return Util.httpStaleSessionResponse(request);
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
		return Util.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/sap/sports/sct/api/mobile/v1/service/rest/me")
	public ResponseEntity<String> me(HttpServletRequest request) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // fallback to Basic Auth
		if (user == null)
			return Util.httpStaleSessionResponse(request);
		//
		JSONObject body = new JSONObject();
		Person me = personRepository.findById(user.personId).orElse(null);
		TenantParameter tpName = tenantParameterRepository.findById("tenantName").orElse(null);
		TenantParameter tpPictureId = tenantParameterRepository.findById("tenantPictureId").orElse(null);
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
		return Util.httpOkResponse(body);
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
