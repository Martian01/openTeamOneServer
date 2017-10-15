package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/sap/sports/pe/api/messaging/v2/service/rest/messaging")
public class MessagingApi {

	/* database interface */

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

	/* API implementation */

	@RequestMapping(method = RequestMethod.POST, value = "/device/subscription")
	public ResponseEntity<String> deviceSubscription(HttpServletRequest request) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpResponse(HttpStatus.OK); // TODO
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/device/subscription")
	public ResponseEntity<String> deviceSubscriptionDelete(HttpServletRequest request) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpResponse(HttpStatus.OK); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/me")
	public ResponseEntity<String> me(HttpServletRequest request) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Person me = personRepository.findOne(user.personId);
		if (me != null)
			body.put("loginPerson", personToJson(me));
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
		return Util.httpResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/person/{personId}")
	public ResponseEntity<String> person(HttpServletRequest request, @PathVariable String personId) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Person person = personId == null ? null : personRepository.findOne(personId);
		if (person != null) {
			JSONObject item = personToJson(person);
			item.put("isContact", isContact(personId));
			JsonUtil.put(item, "privateRoomId", getPrivateRoomId(personId, user.personId));
			body.put("person", item);
		}
		//
		return Util.httpResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/contacts")
	public ResponseEntity<String> contacts(HttpServletRequest request) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Set<String> contactIds = getContactIds();
		Iterable<Person> persons = personRepository.findAll();
		JSONArray contactsJson = new JSONArray();
		for (Person person : persons)
			if (contactIds.contains(person.personId)) {
				JSONObject item = personToJson(person);
				item.put("isContact", true);
				JsonUtil.put(item, "privateRoomId", getPrivateRoomId(person.personId, user.personId));
				contactsJson.put(item);
			}
		body.put("contacts", contactsJson);
		//
		return Util.httpResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/contact/{contactId}/roomId")
	public ResponseEntity<String> contactRoomId(HttpServletRequest request, @PathVariable String contactId) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		JSONObject body = new JSONObject();
		Person person = contactId == null || !getContactIds().contains(contactId) ? null : personRepository.findOne(contactId);
		if (person == null || person.personId.equals(user.personId))
			return Util.httpResponse(HttpStatus.NOT_FOUND);
		String privateRoomId = getPrivateRoomId(contactId, user.personId);
		if (privateRoomId == null)
			privateRoomId = createPrivateRoom(person.personId, user.personId); // create the room on-the-fly
		body.put("roomId", privateRoomId);
		//
		return Util.httpResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/rooms")
	public ResponseEntity<String> rooms(HttpServletRequest request) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
//System.out.println("ROM 1: " + ViewedConfirmation.toJsonArray(viewedConfirmationRepository.findByPersonId(user.personId)).toString());
		JSONObject body = new JSONObject();
		Iterable<Room> rooms = roomRepository.findAll();
		Iterable<RoomMember> roomMembers = roomMemberRepository.findAll();
		body.put("rooms", roomsToJsonArray(rooms, roomMembers, user.personId));
		//
		return Util.httpResponse(body);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/members")
	public ResponseEntity<String> roomMembers(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		if (!isRoomMember(roomId, user.personId))
			return Util.httpResponse(HttpStatus.FORBIDDEN);
		//
		JSONObject body = new JSONObject();
		Iterable<RoomMember> roomMembers = roomMemberRepository.findByRoomId(roomId);
		body.put("roomMembers", RoomMember.toJsonArray(roomMembers));
		//
		return Util.httpResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesSince")
	public ResponseEntity<String> roomMessagesSince(HttpServletRequest request, @PathVariable String roomId, @RequestParam(required = false) Long since, @RequestParam(required = false) Long notBefore) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		if (!isRoomMember(roomId, user.personId))
			return Util.httpResponse(HttpStatus.FORBIDDEN);
		//
//System.out.println("MSG 1: " + Message.toJsonArray(messageRepository.findByRoomId(roomId)).toString());
		JSONObject body = new JSONObject();
		Iterable<Message> messages;
		if (since != null) {
			if (notBefore != null) {
				messages = messageRepository.findByRoomIdAndUpdatedAtGreaterThanAndPostedAtGreaterThanEqual(roomId, since, notBefore);
			} else {
				messages = messageRepository.findByRoomIdAndUpdatedAtGreaterThan(roomId, since);
			}
		} else {
			if (notBefore != null) {
				messages = messageRepository.findByRoomIdAndPostedAtGreaterThanEqual(roomId, notBefore);
			} else {
				messages = messageRepository.findTop100ByRoomId(roomId);
			}
		}
//System.out.println("MSG R: " + Message.toJsonArray(messages).toString());
		body.put("messages", messagesToJsonArray(messages, user.personId));
		//
		return Util.httpResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesUntil")
	public ResponseEntity<String> roomMessagesUntil(HttpServletRequest request, @PathVariable String roomId, @RequestParam(required = false) Integer count, @RequestParam(required = false) Long until) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		if (!isRoomMember(roomId, user.personId))
			return Util.httpResponse(HttpStatus.FORBIDDEN);
		//
		JSONObject body = new JSONObject();
		Iterable<Message> messages;
		if (count != null) {
			Page<Message> page;
			Pageable pageable = new PageRequest(0, count);
			if (until != null) {
				page = messageRepository.findByRoomIdAndPostedAtLessThanOrderByPostedAtDesc(roomId, until, pageable);
			} else {
				page = messageRepository.findByRoomIdOrderByPostedAtDesc(roomId, pageable);
			}
			// re-read messages by posting time
			// (for the unlikely case that the end of the interval falls in between messages with the same posting time)
			List<Message> content = page.getContent();
			if (content.size() > 0) {
				long max = content.get(0).postedAt;
				long min = content.get(content.size() - 1).postedAt;
				messages = messageRepository.findByRoomIdAndPostedAtBetween(roomId, min, max);
			} else
				messages = new ArrayList<>();
		} else {
			if (until != null) {
				messages = messageRepository.findByRoomIdAndPostedAtLessThan(roomId, until);
			} else {
				messages = messageRepository.findTop100ByRoomId(roomId);
			}
		}
		body.put("messages", messagesToJsonArray(messages, user.personId));
		//
		return Util.httpResponse(body);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/message")
	public ResponseEntity<String> roomMessage(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		if (!isRoomMember(roomId, user.personId))
			return Util.httpResponse(HttpStatus.FORBIDDEN);
		//
		return Util.httpResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/viewedConfirmation")
	public ResponseEntity<String> roomViewedConfirmation(HttpServletRequest request, @PathVariable String roomId, @RequestParam(required = false) Long until) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		if (!isRoomMember(roomId, user.personId))
			return Util.httpResponse(HttpStatus.FORBIDDEN);
		//
		long now = System.currentTimeMillis();
//System.out.println("CNF 1: " + ViewedConfirmation.toJsonArray(viewedConfirmationRepository.findByPersonId(user.personId)).toString());
		Iterable<Message> messages;
		Long watermark = getWatermark(user.personId, roomId);
//System.out.println("Watermark: " + JsonUtil.toIsoDate(watermark));
//System.out.println("Until    : " + JsonUtil.toIsoDate(until));
		if (watermark != null) {
			if (until != null) {
				messages = messageRepository.findByRoomIdAndPostedAtGreaterThanAndPostedAtLessThanEqual(roomId, watermark, until);
			} else {
				messages = messageRepository.findByRoomIdAndPostedAtGreaterThan(roomId, watermark);
			}
		} else {
			if (until != null) {
				messages = messageRepository.findTop1ByRoomIdAndPostedAtLessThanEqualOrderByPostedAtDesc(roomId, until);
			} else {
				messages = messageRepository.findTop1ByRoomIdOrderByPostedAtDesc(roomId);
			}
		}
//System.out.println("CNF R: " + Message.toJsonArray(messages).toString());
		ArrayList<ViewedConfirmation> confirmations = new ArrayList<>();
		for (Message message : messages)
			confirmations.add(new ViewedConfirmation(message.messageId, user.personId, message.roomId, message.postedAt, now));
//System.out.println("CNF N: " + ViewedConfirmation.toJsonArray(confirmations).toString());
		viewedConfirmationRepository.save(confirmations);
//System.out.println("CNF 2: " + ViewedConfirmation.toJsonArray(viewedConfirmationRepository.findByPersonId(user.personId)).toString());
		//
		return Util.httpResponse(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/message/{messageId}/confirmations")
	public ResponseEntity<String> messageConfirmations(HttpServletRequest request, @PathVariable String messageId) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		Message message = messageId == null ? null : messageRepository.findOne(messageId);
		if (message == null)
			return Util.httpResponse(HttpStatus.GONE);
		if (!user.personId.equals(message.senderPersonId))
			return Util.httpResponse(HttpStatus.FORBIDDEN);
		//
		JSONObject body = new JSONObject();
		Iterable<ViewedConfirmation> confirmations = viewedConfirmationRepository.findByMessageId(messageId);
		body.put("confirmations", confirmationsToJsonArray(confirmations));
		//
		return Util.httpResponse(body);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/message/{messageId}")
	public ResponseEntity<String> messageDelete(HttpServletRequest request, @PathVariable String messageId) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		Message message = messageId == null ? null : messageRepository.findOne(messageId);
		if (message == null)
			return Util.httpResponse(HttpStatus.GONE);
		if (!user.personId.equals(message.senderPersonId))
			return Util.httpResponse(HttpStatus.FORBIDDEN);
		//
//System.out.println("DEL 1: " + Message.toJsonArray(messageRepository.findByRoomId(message.roomId)).toString());
		// TODO: physically delete text and attachments
		message.isDeleted = true;
		message.updatedAt = System.currentTimeMillis();
		messageRepository.save(message);
//System.out.println("DEL 2: " + Message.toJsonArray(messageRepository.findByRoomId(message.roomId)).toString());
		//
		return Util.httpResponse(HttpStatus.OK);
	}

	/* The following API calls are intentionally not implemented */

	//@RequestMapping(method = RequestMethod.GET, value = "/message/{messageId}")
	//@RequestMapping(method = RequestMethod.POST, value = "/message/{messageId}/readConfirmation")
	//@RequestMapping(method = RequestMethod.POST, value = "/messages/viewedConfirmation}")
	//@RequestMapping(method = RequestMethod.GET, value = "/messagesSince")
	//@RequestMapping(method = RequestMethod.GET, value = "/messagesUntil")

	/* helper functions */

	private boolean isContact(String personId) {
		return personId != null && userRepository.countByPersonIdAndHasUserRoleTrue(personId) > 0;
	}

	private boolean isRoomMember(String roomId, String personId) {
		return roomId != null && personId != null && roomMemberRepository.countByRoomIdAndPersonId(roomId, personId) > 0;
	}

	private Set<String> getContactIds() {
		Iterable<User> users = userRepository.findByPersonIdNotNullAndHasUserRoleTrue();
		Set<String> contactIds = new HashSet<>();
		for (User user : users)
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

	private String createPrivateRoom(String personId1, String personId2) {
		String privateRoomId;
		synchronized(Room.class) {
			privateRoomId = getPrivateRoomId(personId1, personId2);
			if (privateRoomId == null) {
				Room room = new Room(null, "PM", "PM", "private", null, System.currentTimeMillis());
				privateRoomId = room.roomId;
				// create the room members first assuming concurrent reads will read a room before its members...
				// the clean solution would be a read-write lock
				roomMemberRepository.save(new RoomMember(privateRoomId, personId1));
				roomMemberRepository.save(new RoomMember(privateRoomId, personId2));
				roomRepository.save(room);
			}
		}
		return privateRoomId;
	}

	private Long getWatermark(String personId, String roomId) {
		ViewedConfirmation viewedConfirmation = viewedConfirmationRepository.findTopByPersonIdAndRoomIdOrderByMessagePostedAtDesc(personId, roomId);
		return viewedConfirmation == null ? null : viewedConfirmation.messagePostedAt;
	}

	/* API JSON parsers */

	/* no re-use of BO parsers since the formats differ too much and include mixed data */

	public JSONObject personToJson(Person person) throws JSONException {
		JSONObject item = new JSONObject();
		item.put("personId", person.personId);
		JsonUtil.put(item, "lastName", person.lastName);
		JsonUtil.put(item, "firstName", person.firstName);
		JsonUtil.put(item, "nickName", person.nickName);
		JsonUtil.put(item, "pictureId", person.pictureId);
		return item;
	}

	private JSONObject confirmationToJson(ViewedConfirmation confirmation) throws JSONException {
		JSONObject item = new JSONObject();
		item.put("personId", confirmation.personId);
		item.put("viewedAt", JsonUtil.toIsoDate(confirmation.confirmedAt));
		return item;
	}

	private JSONArray confirmationsToJsonArray(Iterable<ViewedConfirmation> confirmations) throws JSONException {
		JSONArray array = new JSONArray();
		for (ViewedConfirmation confirmation : confirmations)
			array.put(confirmationToJson(confirmation));
		return array;
	}

	public JSONObject attachmentToJson(Attachment attachment) throws JSONException {
		JSONObject sapSportsFile = new JSONObject();
		sapSportsFile.put("fileId", attachment.fileId);
		sapSportsFile.put("mimeType", attachment.mimeType);
		JSONObject attachmentContent = new JSONObject();
		JsonUtil.put(attachmentContent, "text", attachment.text);
		JsonUtil.put(attachmentContent, "mimeType", "application/vnd.sap.sports.file");
		JsonUtil.put(attachmentContent, "sapSportsFile", sapSportsFile);
		JSONObject item = new JSONObject();
		item.put("assetId", attachment.attachmentId);
		item.put("assetContent", attachmentContent);
		return item;
	}

	public JSONArray attachmentsToJsonArray(Iterable<Attachment> attachments) throws JSONException {
		JSONArray array = new JSONArray();
		for (Attachment attachment : attachments)
			array.put(attachmentToJson(attachment));
		return array;
	}

	private JSONObject messageToJson(Message message, String personId) throws JSONException {
		JSONObject messageContent = new JSONObject();
		messageContent.put("roomId", message.roomId);
		messageContent.put("senderPersonId", message.senderPersonId);
		messageContent.put("postedAt", JsonUtil.toIsoDate(message.postedAt));
		messageContent.put("text", message.text);
		messageContent.put("isOwnMessage", personId.equals(message.senderPersonId));
		Iterable<Attachment> attachments = attachmentRepository.findByMessageId(message.messageId);
		if (attachments != null)
			messageContent.put("assets", attachmentsToJsonArray(attachments));
		JSONObject messageStatus = new JSONObject();
		messageStatus.put("isDeleted", message.isDeleted);
		messageStatus.put("updatedAt", JsonUtil.toIsoDate(message.updatedAt));
		JSONObject postedMessageStatus = new JSONObject();
		postedMessageStatus.put("viewedCount", viewedConfirmationRepository.countByMessageId(message.messageId));
		messageStatus.put("postedMessageStatus", postedMessageStatus);
		JSONObject receivedMessageStatus = new JSONObject();
		ViewedConfirmation confirmation = viewedConfirmationRepository.findTopByMessageIdAndPersonId(message.messageId, personId);
		if (confirmation != null)
			receivedMessageStatus.put("viewedAt", JsonUtil.toIsoDate(confirmation.confirmedAt)); // semantics of this field is unclear at best
		messageStatus.put("receivedMessageStatus", receivedMessageStatus);
		JSONObject item = new JSONObject();
		item.put("messageId", message.messageId);
		item.put("clientMessageId", message.clientMessageId);
		item.put("messageContent", messageContent);
		item.put("messageStatus", messageStatus);
		return item;
	}

	public JSONArray messagesToJsonArray(Iterable<Message> messages, String personId) throws JSONException {
		JSONArray array = new JSONArray();
		for (Message message : messages)
			array.put(messageToJson(message, personId));
		return array;
	}

	public JSONArray roomsToJsonArray(Iterable<Room> rooms, Iterable<RoomMember> roomMembers, String personId) throws JSONException {
		// set up two helper maps
		Set<String> userRooms = new HashSet<>();
		Map<String, JSONArray> membersMap = new HashMap<>();
		for (RoomMember roomMember : roomMembers) {
			if (personId.equals(roomMember.personId))
				userRooms.add(roomMember.roomId);
			JSONArray membersPerRoom = membersMap.get(roomMember.roomId);
			if (membersPerRoom == null) {
				membersPerRoom = new JSONArray();
				membersMap.put(roomMember.roomId, membersPerRoom);
			}
			membersPerRoom.put(roomMember.personId);
		}
		// process the rooms
		JSONArray array = new JSONArray();
		for (Room room : rooms)
			if (userRooms.contains(room.roomId)) {
				JSONObject roomStatus = new JSONObject();
				roomStatus.put("dataChangedAt", JsonUtil.toIsoDate(room.changedAt));
				JSONObject roomData = new JSONObject();
				JsonUtil.put(roomData, "name", room.name);
				JsonUtil.put(roomData, "shortName", room.shortName);
				JsonUtil.put(roomData, "roomType", room.roomType);
				JsonUtil.put(roomData, "pictureId", room.pictureId);
				JSONArray currentMemberIds = membersMap.get(room.roomId);
				if (currentMemberIds == null)
					currentMemberIds = new JSONArray();
				roomData.put("currentMemberIds", currentMemberIds);
				JSONObject roomContent = new JSONObject();
				Long watermark = getWatermark(personId, room.roomId);
				if (watermark != null) {
					long count = messageRepository.countByRoomIdAndPostedAtGreaterThan(room.roomId, watermark);
					roomContent.put("badgeCount", count);
				}
				Message latestMessage = messageRepository.findTopByRoomIdAndIsDeletedFalseOrderByPostedAtDesc(room.roomId);
				if (latestMessage != null)
					roomContent.put("latestMessage", messageToJson(latestMessage, personId));
				JSONObject item = new JSONObject();
				item.put("roomId", room.roomId);
				item.put("roomStatus", roomStatus);
				item.put("roomData", roomData);
				item.put("roomContent", roomContent);
				array.put(item);
			}
		return array;
	}

}
