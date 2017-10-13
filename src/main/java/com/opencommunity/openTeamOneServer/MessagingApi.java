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
	@Autowired
	private MessageRepository messageRepository;
	@Autowired
	private AttachmentRepository attachmentRepository;
	@Autowired
	private ViewedConfirmationRepository viewedConfirmationRepository;

	@RequestMapping(method = RequestMethod.POST, value = "/device/subscription")
	public ResponseEntity<String> deviceSubscription(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultResponse(HttpStatus.OK); // TODO
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/device/subscription")
	public ResponseEntity<String> deviceSubscriptionDelete(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultResponse(HttpStatus.OK); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/me")
	public ResponseEntity<String> me(HttpServletRequest request) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Person me = personRepository.findOne(user.personId);
		if (me != null)
			body.put("loginPerson", me.toJson());
		TenantParameter tpName = tenantParameterRepository.findOne("name");
		TenantParameter tpPictureId = tenantParameterRepository.findOne("pictureId");
		if (tpName != null || tpPictureId != null) {
			JSONObject tenant = new JSONObject();
			if (tpName != null)
				tenant.put("name", tpName.value);
			if (tpPictureId != null)
				tenant.put("pictureId", tpPictureId.value);
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
			if (user.personId != null && user.hasUserRole)
				contactIds.add(user.personId);
		return contactIds;
	}

	private String getPrivateRoomId(String personId1, String personId2) {
		if (personId1 == null || personId2 == null || personId1.equals(personId2))
			return null;
		Set<String> roomIds = new HashSet<>();
		Iterable<Room> rooms = roomRepository.findByRoomType("private");
		for (Room room : rooms) {
			Iterable<RoomMember> roomMembers = roomMemberRepository.findByRoomId(room.roomId);
			Set<String> set = new HashSet<>();
			for (RoomMember roomMember : roomMembers)
				set.add(roomMember.personId);
			if (set.contains(personId1) && set.contains(personId2) && set.size() == 2)
				roomIds.add(room.roomId);
		}
		return roomIds.size() == 1 ? roomIds.iterator().next() : null;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/person/{personId}")
	public ResponseEntity<String> person(HttpServletRequest request, @PathVariable String personId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Person person = personId == null ? null : personRepository.findOne(personId);
		if (person != null) {
			JSONObject personJson = person.toJson();
			personJson.put("isContact", getContactIds().contains(personId));
			JsonUtil.put(personJson, "privateRoomId", getPrivateRoomId(personId, user.personId));
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
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Set<String> contactIds = getContactIds();
		Iterable<Person> persons = personRepository.findAll();
		JSONArray contactsJson = new JSONArray();
		for (Person person : persons)
			if (contactIds.contains(person.personId)) {
				JSONObject personJson = person.toJson();
				personJson.put("isContact", true);
				JsonUtil.put(personJson, "privateRoomId", getPrivateRoomId(person.personId, user.personId));
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
				Room room = new Room(null, "PM", "PM", "private", null, System.currentTimeMillis());
				privateRoomId = room.roomId;
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
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Person person = contactId == null || !getContactIds().contains(contactId) ? null : personRepository.findOne(contactId);
		if (person == null || person.personId.equals(user.personId))
			return Util.defaultResponse(HttpStatus.NOT_FOUND);
		String privateRoomId = getPrivateRoomId(contactId, user.personId);
		if (privateRoomId == null)
			privateRoomId = createPrivateRoom(person.personId, user.personId); // create the room on-the-fly
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
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Iterable<Room> rooms = roomRepository.findAll();
		Iterable<RoomMember> roomMembers = roomMemberRepository.findAll();
		Set<String> userRoomIds = new HashSet<>();
		Map<String, JSONArray> byRoomId = new HashMap<>();
		for (RoomMember roomMember : roomMembers) {
			if (user.personId.equals(roomMember.personId))
				userRoomIds.add(roomMember.roomId);
			JSONArray array = byRoomId.get(roomMember.roomId);
			if (array == null) {
				array = new JSONArray();
				byRoomId.put(roomMember.roomId, array);
			}
			array.put(roomMember.personId);
		}
		JSONArray roomsJson = new JSONArray();
		for (Room room : rooms)
			if (userRoomIds.contains(room.roomId)) {
				JSONObject roomJson = room.toJson();
				JSONObject roomData = roomJson.getJSONObject("roomData");
				JSONArray currentMemberIds = byRoomId.get(room.roomId);
				if (currentMemberIds == null)
					currentMemberIds = new JSONArray();
				roomData.put("currentMemberIds", currentMemberIds);
				JSONObject roomContent = roomJson.getJSONObject("roomContent");
				ViewedConfirmation viewedConfirmation = viewedConfirmationRepository.findTopByRoomIdOrderByTimestampDesc(room.roomId);
				if (viewedConfirmation != null) {
					long watermark = viewedConfirmation.timestamp;
					long count = messageRepository.countByRoomIdAndCreatedAtGreaterThan(room.roomId, watermark);
					roomContent.put("badgeCount", count);
				}
				Message latestMessage = messageRepository.findTopByRoomIdOrderByCreatedAtDesc(room.roomId);
				if (latestMessage != null)
					roomContent.put("latestMessage", latestMessage.toJson());
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
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Iterable<RoomMember> roomMembers = roomMemberRepository.findByRoomId(roomId);
		boolean found = false;
		for (RoomMember roomMember : roomMembers)
			if (found = roomMember.personId.equals(user.personId))
				break;
		if (!found)
			return Util.defaultResponse(HttpStatus.FORBIDDEN);
		body.put("roomMembers", RoomMember.toJsonArray(roomMembers));
		//
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(body.toString(), httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesSince")
	public ResponseEntity<String> roomMessagesSince(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		// TODO: validation of roomId
		return Util.defaultResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesUntil")
	public ResponseEntity<String> roomMessagesBefore(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		// TODO: validation of roomId
		return Util.defaultResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/message")
	public ResponseEntity<String> roomMessage(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		// TODO: validation of roomId
		return Util.defaultResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/viewedConfirmation") // query param "until" contains millis
	public ResponseEntity<String> roomViewedConfirmation(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		// TODO: validation of roomId
		return Util.defaultResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/message/{messageId}/confirmations")
	public ResponseEntity<String> messageConfirmations(HttpServletRequest request, @PathVariable String messageId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		// TODO: validation of roomId
		return Util.defaultResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/message/{messageId}")
	public ResponseEntity<String> messageDelete(HttpServletRequest request, @PathVariable String messageId) throws JSONException {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		User user = session == null ? null : userRepository.findOne(session.userId);
		if (user == null)
			return Util.defaultResponse(HttpStatus.UNAUTHORIZED);
		//
		// TODO: validation of owner
		return Util.defaultResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	/* The following API calls are intentionally not implemented */

	//@RequestMapping(method = RequestMethod.GET, value = "/message/{messageId}")
	//@RequestMapping(method = RequestMethod.POST, value = "/message/{messageId}/readConfirmation")
	//@RequestMapping(method = RequestMethod.POST, value = "/messages/viewedConfirmation}")
	//@RequestMapping(method = RequestMethod.GET, value = "/messagesSince")
	//@RequestMapping(method = RequestMethod.GET, value = "/messagesUntil")

}
