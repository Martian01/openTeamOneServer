package com.opencommunity.openTeamOneServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sap/sports/pe/api/messaging/v2/service/rest/messaging")
public class MessagingApi {

	@Autowired
	private TenantParameterRepository tenantParameterRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PersonRepository personRepository;


	@RequestMapping(method = RequestMethod.POST, value = "/device/subscription")
	@ResponseBody
	public ResponseEntity<String> deviceSubscription(HttpServletRequest request) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.OK); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/me")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultMapResponse(HttpStatus.UNAUTHORIZED);
		//
		User user = userRepository.findOne(session.userId);
		Person person = user == null ? null : personRepository.findOne(user.getPersonId());
		Map<String, Object> tenant = new HashMap<>();
		TenantParameter tp;
		tp = tenantParameterRepository.findOne("name");
		tenant.put("name", tp == null ? null : tp.getValue());
		tp = tenantParameterRepository.findOne("pictureId");
		tenant.put("pictureId", tp == null ? null : tp.getValue());
		Map<String, Object> loginPerson = new HashMap<>();
		loginPerson.put("personId", person == null ? null : person.getPersonId());
		loginPerson.put("lastName", person == null ? null : person.getLastName());
		loginPerson.put("firstName", person == null ? null : person.getFirstName());
		loginPerson.put("nickName", person == null ? null : person.getNickName());
		loginPerson.put("pictureId", person == null ? null : person.getPictureId());
		Map<String, Object> body = new HashMap<>();
		body.put("loginPerson", loginPerson);
		body.put("tenant", tenant);
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/contacts")
	@ResponseBody
	public ResponseEntity<String> contacts(HttpServletRequest request) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/rooms")
	@ResponseBody
	public ResponseEntity<String> rooms(HttpServletRequest request) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesSince")
	@ResponseBody
	public ResponseEntity<String> roomMessagesSince(HttpServletRequest request, @PathVariable String roomId) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesBefore")
	@ResponseBody
	public ResponseEntity<String> roomMessagesBefore(HttpServletRequest request, @PathVariable String roomId) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/viewedConfirmation")
	@ResponseBody
	public ResponseEntity<String> roomviewedConfirmation(HttpServletRequest request, @PathVariable String roomId) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

}
