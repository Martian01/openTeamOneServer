package com.opencommunity.openTeamOneServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/svc")
public class ServiceApi {

	@Autowired
	private TenantParameterRepository tenantParameterRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PersonRepository personRepository;

	/* AJAX Services (with JSON responses) */

	@RequestMapping(method = RequestMethod.GET, value = "/session")
	public ResponseEntity<String>svcSession(HttpServletRequest request) throws JSONException {
		String sessionId = Util.getSessionId(request);
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		//
		JSONObject body = new JSONObject();
		if (session != null) {
			body.put("session", session.toJson());
			User user = session.userId == null ? null : userRepository.findOne(session.userId);
			if (user != null) {
				body.put("user", user.toJson());
				Person person = user.personId == null ? null : personRepository.findOne(user.personId);
				if (person != null)
					body.put("person", person.toJson());
				if (user.hasAdminRole) {
					//body.put("tenantParameters", TenantParameter.toJsonArray(tenantParameterRepository.findAll()));
					TenantParameter tp = tenantParameterRepository.findOne("dataDirectory");
					if (tp != null && tp.value != null)
						body.put("dataDirectory", tp.value);
				}
			}
		}
		//
		return Util.httpStringResponse(body);
	}

	/* CRUD Services for Person */

	@RequestMapping(method = RequestMethod.GET, value = "/persons")
	public ResponseEntity<String> personsGet(HttpServletRequest request) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpStringResponse(Person.toJsonArray(personRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/person/{personId}")
	public ResponseEntity<String> personGet(HttpServletRequest request, @PathVariable String personId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		Person person = personId == null ? null : personRepository.findOne(personId);
		if (person == null)
			return Util.httpStringResponse(HttpStatus.NOT_FOUND);
		//
		return Util.httpStringResponse(person.toJson());
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/person/{personId}")
	public ResponseEntity<String> personPut(HttpServletRequest request, @PathVariable String personId, @RequestBody String requestBody) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		if (personId == null || requestBody == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		JSONObject item = new JSONObject(requestBody);
		if (!personId.equals(item.get("personId")))
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		Person person = new Person(item);
		personRepository.save(person);
		//
		return Util.httpStringResponse(HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/person/{personId}")
	public ResponseEntity<String> personDelete(HttpServletRequest request, @PathVariable String personId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		Person person = personId == null ? null : personRepository.findOne(personId);
		if (person == null)
			return Util.httpStringResponse(HttpStatus.GONE);
		personRepository.delete(person);
		//
		return Util.httpStringResponse(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/person")
	public ResponseEntity<String> personPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		if (requestBody == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		Person person = new Person(new JSONObject(requestBody));
		personRepository.save(person);
		//
		return Util.httpStringResponse(person.toJson(), HttpStatus.CREATED);
	}

}
