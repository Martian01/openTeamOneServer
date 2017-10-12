package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/sap/sports/pe/api/messaging/v2/service/rest/messaging")
public class MessagingApi {

	@Autowired
	private TenantParameterRepository tenantParameterRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private RoomRepository roomRepository;


	@RequestMapping(method = RequestMethod.POST, value = "/device/subscription")
	@ResponseBody
	public ResponseEntity<String> deviceSubscription(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.OK); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/me")
	@ResponseBody
	public ResponseEntity<String> me(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		User user = userRepository.findOne(session.userId);
		Person person = user == null ? null : personRepository.findOne(user.getPersonId());
		if (person != null)
			body.put("loginPerson", person.toJson());
		//
		TenantParameter tpName = tenantParameterRepository.findOne("name");
		TenantParameter tpPictureId = tenantParameterRepository.findOne("pictureId");
		if (tpName != null || tpPictureId != null) {
			JSONObject tenant = new JSONObject();
			if (tpName != null)
				tenant.put("name", tpName.getValue());
			if (tpPictureId != null)
				tenant.put("pictureId", tpPictureId.getValue());
			body.put("tenant", tenant);
		}
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body.toString(), httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/contacts")
	@ResponseBody
	public ResponseEntity<String> contacts(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Iterable<User> users = userRepository.findAll();
		Set<String> contactIds = new HashSet<>();
		for (User user : users)
			if (user.getPersonId() != null && user.isHasUserRole())
				contactIds.add(user.getPersonId());
		Iterable<Person> persons = personRepository.findAll();
		JSONArray contactsJson = new JSONArray();
		for (Person person : persons)
			if (contactIds.contains(person.getPersonId())) {
				JSONObject personJson = person.toJson();
				personJson.put("isContact", true);
				// TODO: add private room id if applicable
				//personJson.put("privateRoomId", null);
				contactsJson.put(personJson);
			}
		body.put("contacts", contactsJson);
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body.toString(), httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/contact/{contactId}/roomId")
	@ResponseBody
	public ResponseEntity<String> contactRoomId(HttpServletRequest request, @PathVariable String contactId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/rooms")
	@ResponseBody
	public ResponseEntity<String> rooms(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Iterable<Room> rooms = roomRepository.findAll();
		JSONArray roomsJson = new JSONArray();
		for (Room room : rooms)
			if (true) { // TODO: check if session user is a member
				JSONObject roomJson = room.toJson();
				JSONObject roomData = roomJson.getJSONObject("roomData");
				JSONArray currentMemberIds = new JSONArray();
				// TODO: add room members
				roomData.put("currentMemberIds", currentMemberIds);
				JSONObject roomContent = roomJson.getJSONObject("roomContent");
				// TODO: find badge count and latest message
				roomContent.put("badgeCount", 0);
				//roomContent.put("latestMessage", null);
				roomsJson.put(roomJson);
			}
		body.put("rooms", roomsJson);
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body.toString(), httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesSince")
	@ResponseBody
	public ResponseEntity<String> roomMessagesSince(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesBefore")
	@ResponseBody
	public ResponseEntity<String> roomMessagesBefore(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/viewedConfirmation")
	@ResponseBody
	public ResponseEntity<String> roomviewedConfirmation(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

}
