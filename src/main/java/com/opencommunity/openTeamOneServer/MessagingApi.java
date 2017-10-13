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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	@Autowired
	private RoomMemberRepository roomMemberRepository;

	@RequestMapping(method = RequestMethod.POST, value = "/device/subscription")
	public ResponseEntity<String> deviceSubscription(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.OK); // TODO
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/device/subscription")
	public ResponseEntity<String> deviceSubscriptionDelete(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.OK); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/me")
	public ResponseEntity<String> me(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Person me = personRepository.findOne(user.getPersonId());
		if (me != null)
			body.put("loginPerson", me.toJson());
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

	private Set<String> getContactIds() {
		Iterable<User> users = userRepository.findAll();
		Set<String> contactIds = new HashSet<>();
		for (User user : users)
			if (user.getPersonId() != null && user.isHasUserRole())
				contactIds.add(user.getPersonId());
		return contactIds;
	}

/*
	private Set<String> getRoomIds(String roomType, String... personIds) {
		Set<String> roomIds = new HashSet<>();
		Iterable<Room> rooms = roomType == null ? roomRepository.findAll() : roomRepository.findByRoomType(roomType);
		for (Room room : rooms) {
			Iterable<RoomMember> roomMembers = roomMemberRepository.findByRoomId(room.getRoomId());
			boolean foundAll = false;
			for (String personId : personIds) {
				boolean foundPersonId = false;
				for (RoomMember roomMember : roomMembers) {
					foundPersonId = personId.equals(roomMember.getPersonId());
					if (foundPersonId)
						break;
				}
				foundAll = foundPersonId;
				if (!foundAll)
					break;
			}
			if (foundAll)
				roomIds.add(room.getRoomId());
		}
		return roomIds;
	}

	private String getPrivateRoomId(String personId1, String personId2) {
		if (personId1.equals(personId2))
			return null;
		Set<String> roomIds = getRoomIds("private", personId1, personId2);
		return roomIds.size() == 1 ? roomIds.iterator().next() : null;
	}
*/

	private String getPrivateRoomId(String personId1, String personId2) {
		if (personId1 == null || personId2 == null || personId1.equals(personId2))
			return null;
		Set<String> roomIds = new HashSet<>();
		Iterable<Room> rooms = roomRepository.findByRoomType("private");
		for (Room room : rooms) {
			Iterable<RoomMember> roomMembers = roomMemberRepository.findByRoomId(room.getRoomId());
			Set<String> set = new HashSet<>();
			for (RoomMember roomMember : roomMembers)
				set.add(roomMember.getPersonId());
			if (set.contains(personId1) && set.contains(personId2) && set.size() == 2)
				roomIds.add(room.getRoomId());
		}
		return roomIds.size() == 1 ? roomIds.iterator().next() : null;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/person/{personId}")
	public ResponseEntity<String> person(HttpServletRequest request, @PathVariable String personId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Person person = personId == null ? null : personRepository.findOne(personId);
		if (person != null) {
			JSONObject personJson = person.toJson();
			personJson.put("isContact", getContactIds().contains(personId));
			JsonUtil.put(personJson, "privateRoomId", getPrivateRoomId(personId, user.getPersonId()));
			body.put("person", personJson);
		}
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body.toString(), httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/contacts")
	public ResponseEntity<String> contacts(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Set<String> contactIds = getContactIds();
		Iterable<Person> persons = personRepository.findAll();
		JSONArray contactsJson = new JSONArray();
		for (Person person : persons)
			if (contactIds.contains(person.getPersonId())) {
				JSONObject personJson = person.toJson();
				personJson.put("isContact", true);
				JsonUtil.put(personJson, "privateRoomId", getPrivateRoomId(person.getPersonId(), user.getPersonId()));
				contactsJson.put(personJson);
			}
		body.put("contacts", contactsJson);
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body.toString(), httpHeaders, HttpStatus.OK);
	}

	private String createPrivateRoom(String personId1, String personId2) {
		String privateRoomId;
		synchronized(Room.class) {
			privateRoomId = getPrivateRoomId(personId1, personId2);
			if (privateRoomId == null) {
				Room room = new Room("PM", "PM", "private", null);
				privateRoomId = room.getRoomId();
				// create the room members first assuming concurrent reads will read a room before its members...
				// the clean solution is a read-write lock
				roomMemberRepository.save(new RoomMember(privateRoomId, personId1));
				roomMemberRepository.save(new RoomMember(privateRoomId, personId2));
				roomRepository.save(room);
			}
		}
		return privateRoomId;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/contact/{contactId}/roomId")
	public ResponseEntity<String> contactRoomId(HttpServletRequest request, @PathVariable String contactId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Person person = contactId == null || !getContactIds().contains(contactId) ? null : personRepository.findOne(contactId);
		if (person == null || person.getPersonId().equals(user.getPersonId()))
			return Util.defaultStringResponse(HttpStatus.NOT_FOUND);
		String privateRoomId = getPrivateRoomId(contactId, user.getPersonId());
		if (privateRoomId == null)
			privateRoomId = createPrivateRoom(person.getPersonId(), user.getPersonId()); // create the room on-the-fly
		body.put("roomId", privateRoomId);
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body.toString(), httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/rooms")
	public ResponseEntity<String> rooms(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Iterable<Room> rooms = roomRepository.findAll();
		Iterable<RoomMember> roomMembers = roomMemberRepository.findAll();
		Set<String> userRoomIds = new HashSet<>();
		Map<String, JSONArray> byRoomId = new HashMap<>();
		for (RoomMember roomMember : roomMembers) {
			if (user.getPersonId().equals(roomMember.getPersonId()))
				userRoomIds.add(roomMember.getRoomId());
			JSONArray array = byRoomId.get(roomMember.getRoomId());
			if (array == null) {
				array = new JSONArray();
				byRoomId.put(roomMember.getRoomId(), array);
			}
			array.put(roomMember.getPersonId());
		}
		JSONArray roomsJson = new JSONArray();
		for (Room room : rooms)
			if (userRoomIds.contains(room.getRoomId())) {
				JSONObject roomJson = room.toJson();
				JSONObject roomData = roomJson.getJSONObject("roomData");
				JSONArray currentMemberIds = byRoomId.get(room.getRoomId());
				if (currentMemberIds == null)
					currentMemberIds = new JSONArray();
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

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/members")
	public ResponseEntity<String> roomMembers(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesSince")
	public ResponseEntity<String> roomMessagesSince(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesBefore")
	public ResponseEntity<String> roomMessagesBefore(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/message")
	public ResponseEntity<String> roomMessage(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/viewedConfirmation")
	public ResponseEntity<String> roomViewedConfirmation(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/message/{messageId}/confirmations")
	public ResponseEntity<String> messageConfirmations(HttpServletRequest request, @PathVariable String messageId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/message/{messageId}")
	public ResponseEntity<String> messageDelete(HttpServletRequest request, @PathVariable String messageId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	/* The following API calls are intentionally not implemented */

	//@RequestMapping(method = RequestMethod.GET, value = "/message/{messageId}")
	//@RequestMapping(method = RequestMethod.POST, value = "/message/{messageId}/readConfirmation")
	//@RequestMapping(method = RequestMethod.POST, value = "/messages/viewedConfirmation}")
	//@RequestMapping(method = RequestMethod.GET, value = "/messagesSince")
	//@RequestMapping(method = RequestMethod.GET, value = "/messagesBefore")

}
