package com.opencommunity.openTeamOneServer.api;

import com.opencommunity.openTeamOneServer.data.*;
import com.opencommunity.openTeamOneServer.persistence.*;
import com.opencommunity.openTeamOneServer.util.JsonUtil;
import com.opencommunity.openTeamOneServer.util.Util;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
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
	private SymbolicFileRepository symbolicFileRepository;
	@Autowired
	private ViewedConfirmationRepository viewedConfirmationRepository;
	@Autowired
	private SubscriptionRepository subscriptionRepository;

	/* API implementation */

	@RequestMapping(method = RequestMethod.POST, value = "/device/subscription")
	public ResponseEntity<String> deviceSubscription(HttpServletRequest request, @RequestBody String input) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
		//
		if (input == null)
			return Util.httpBadRequestResponse;
		Subscription subscription = getSubscription(new JSONObject(input), user.userId);
		if (subscription == null)
			return Util.httpBadRequestResponse;
		subscriptionRepository.save(subscription);
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/device/subscription")
	public ResponseEntity<String> deviceSubscriptionDelete(HttpServletRequest request, @RequestBody String input) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
		//
		if (input == null)
			return Util.httpBadRequestResponse;
		SubscriptionKey key = getSubscriptionKey(new JSONObject(input), user.userId);
		if (key == null)
			return Util.httpBadRequestResponse;
		Subscription subscription = subscriptionRepository.findTopByTargetTypeAndAppIdAndDeviceTokenAndUserId(key.targetType, key.appId, key.deviceToken, key.userId);
		if (subscription != null) {
			subscription.isActive = false;
			subscription.changedAt = System.currentTimeMillis();
			subscriptionRepository.save(subscription);
		}
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/me")
	public ResponseEntity<String> me(HttpServletRequest request) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
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
		return Util.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/person/{personId}")
	public ResponseEntity<String> person(HttpServletRequest request, @PathVariable String personId) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
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
		return Util.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/contacts")
	public ResponseEntity<String> contacts(HttpServletRequest request) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
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
		return Util.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/contact/{contactId}/roomId")
	public ResponseEntity<String> contactRoomId(HttpServletRequest request, @PathVariable String contactId) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
		//
		JSONObject body = new JSONObject();
		Person person = contactId == null || !getContactIds().contains(contactId) ? null : personRepository.findOne(contactId);
		if (person == null || person.personId.equals(user.personId))
			return Util.httpNotFoundResponse;
		String privateRoomId = getPrivateRoomId(contactId, user.personId);
		if (privateRoomId == null)
			privateRoomId = createPrivateRoom(person.personId, user.personId); // create the room on-the-fly
		body.put("roomId", privateRoomId);
		//
		return Util.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/rooms")
	public ResponseEntity<String> rooms(HttpServletRequest request) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
		//
		JSONObject body = new JSONObject();
		Iterable<Room> rooms = roomRepository.findAll();
		Iterable<RoomMember> roomMembers = roomMemberRepository.findAll();
		body.put("rooms", roomsToJsonArray(rooms, roomMembers, user.personId));
		//
		return Util.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/members")
	public ResponseEntity<String> roomMembers(HttpServletRequest request, @PathVariable String roomId) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
		//
		if (!isRoomMember(roomId, user.personId))
			return Util.httpForbiddenResponse;
		//
		JSONObject body = new JSONObject();
		Iterable<RoomMember> roomMembers = roomMemberRepository.findByRoomId(roomId);
		body.put("roomMembers", RoomMember.toJsonArray(roomMembers));
		//
		return Util.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesSince")
	public ResponseEntity<String> roomMessagesSince(HttpServletRequest request, @PathVariable String roomId, @RequestParam(required = false) Long since, @RequestParam(required = false) Long notBefore) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
		//
		if (!isRoomMember(roomId, user.personId))
			return Util.httpForbiddenResponse;
		//
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
				messages = getMessagesByPage(roomId, 100, null);
			}
		}
		body.put("messages", messagesToJsonArray(messages, user.personId));
		//
		return Util.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}/messagesUntil")
	public ResponseEntity<String> roomMessagesUntil(HttpServletRequest request, @PathVariable String roomId, @RequestParam(required = false) Integer count, @RequestParam(required = false) Long until) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
		//
		if (!isRoomMember(roomId, user.personId))
			return Util.httpForbiddenResponse;
		//
		JSONObject body = new JSONObject();
		Iterable<Message> messages;
		if (count != null) {
			messages = getMessagesByPage(roomId, count, until);
		} else {
			if (until != null) {
				messages = messageRepository.findByRoomIdAndPostedAtLessThan(roomId, until);
			} else {
				messages = getMessagesByPage(roomId, 100, null);
			}
		}
		body.put("messages", messagesToJsonArray(messages, user.personId));
		//
		return Util.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/message")
	public ResponseEntity<String> roomMessage(MultipartHttpServletRequest multipartRequest, @PathVariable String roomId) throws Exception {
		Session session = Util.getSession(multipartRequest);
		User user = session == null ? Util.getBasicAuthContact(multipartRequest, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(multipartRequest);
		//
		if (!isRoomMember(roomId, user.personId))
			return Util.httpForbiddenResponse;
		//
		if (multipartRequest == null)
			return Util.httpBadRequestResponse;
		long now = System.currentTimeMillis();
		// scan message part
		String parameter = multipartRequest.getParameter("message");
		if (parameter == null)
			return Util.httpBadRequestResponse;
		ProtoMessage protoMessage = getProtoMessage(new JSONObject(parameter));
		if (protoMessage == null)
			return Util.httpBadRequestResponse;
		// add mime types and save attachments in file system
		if (protoMessage.protoSymbolicFiles != null) {
			// first pass for validation of all attachments
			for (ProtoSymbolicFile protoSymbolicFile : protoMessage.protoSymbolicFiles)
				if (multipartRequest.getFile(protoSymbolicFile.clientId) == null)
					return Util.httpBadRequestResponse;
			// second pass for writing attachments
			File directory = Util.getDataDirectory(tenantParameterRepository, SymbolicFile.DIRECTORY_ATTACHMENTS);
			if (directory == null)
				return Util.httpInternalErrorResponse;
			for (ProtoSymbolicFile protoSymbolicFile : protoMessage.protoSymbolicFiles) {
				MultipartFile multipartFile = multipartRequest.getFile(protoSymbolicFile.clientId);
				protoSymbolicFile.mimeType = multipartFile.getContentType();
				File file = new File(directory, protoSymbolicFile.fileId);
				Util.writeFile(multipartFile.getInputStream(), file);
			}
		}
		// create BOs
		Message message = new Message(protoMessage.messageId, protoMessage.clientId, roomId, user.personId, now, protoMessage.text, false, now);
		ArrayList<SymbolicFile> symbolicFiles = null;
		if (protoMessage.protoSymbolicFiles != null) {
			symbolicFiles = new ArrayList<>();
			int i = 0;
			for (ProtoSymbolicFile protoSymbolicFile : protoMessage.protoSymbolicFiles)
				symbolicFiles.add(new SymbolicFile(protoSymbolicFile.fileId, protoSymbolicFile.mimeType, protoSymbolicFile.text, protoMessage.messageId, i++, SymbolicFile.DIRECTORY_ATTACHMENTS));
		}
		// save without lock by saving the dependent items first
		symbolicFileRepository.save(symbolicFiles);
		messageRepository.save(message);
		// construct response
		JSONObject body = new JSONObject();
		JsonUtil.put(body, "message", messageToJson(message, user.personId, symbolicFiles));
		//
		return Util.httpResponse(body, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room/{roomId}/viewedConfirmation")
	public ResponseEntity<String> roomViewedConfirmation(HttpServletRequest request, @PathVariable String roomId, @RequestParam(required = false) Long until) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
		//
		if (!isRoomMember(roomId, user.personId))
			return Util.httpForbiddenResponse;
		//
		long now = System.currentTimeMillis();
		Iterable<Message> messages;
		Long watermark = getWatermark(user.personId, roomId);
		if (watermark != null) {
			if (until != null) {
				messages = messageRepository.findByRoomIdAndIsDeletedFalseAndPostedAtGreaterThanAndPostedAtLessThanEqual(roomId, watermark, until);
			} else {
				messages = messageRepository.findByRoomIdAndIsDeletedFalseAndPostedAtGreaterThan(roomId, watermark);
			}
		} else {
			if (until != null) {
				messages = messageRepository.findTop1ByRoomIdAndIsDeletedFalseAndPostedAtLessThanEqualOrderByPostedAtDesc(roomId, until);
			} else {
				messages = messageRepository.findTop1ByRoomIdAndIsDeletedFalseOrderByPostedAtDesc(roomId);
			}
		}
		ArrayList<ViewedConfirmation> confirmations = new ArrayList<>();
		for (Message message : messages)
			confirmations.add(new ViewedConfirmation(message.messageId, user.personId, message.roomId, message.postedAt, now));
		viewedConfirmationRepository.save(confirmations);
		//
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/message/{messageId}/confirmations")
	public ResponseEntity<String> messageConfirmations(HttpServletRequest request, @PathVariable String messageId) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
		//
		Message message = messageId == null ? null : messageRepository.findOne(messageId);
		if (message == null)
			return Util.httpNotFoundResponse;
		if (!user.personId.equals(message.senderPersonId))
			return Util.httpForbiddenResponse;
		//
		JSONObject body = new JSONObject();
		Iterable<ViewedConfirmation> confirmations = viewedConfirmationRepository.findByMessageId(messageId);
		body.put("confirmations", confirmationsToJsonArray(confirmations));
		//
		return Util.httpOkResponse(body);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/message/{messageId}")
	public ResponseEntity<String> messageDelete(HttpServletRequest request, @PathVariable String messageId) throws JSONException {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpStaleSessionResponse(request);
		//
		Message message = messageId == null ? null : messageRepository.findOne(messageId);
		if (message == null)
			return Util.httpGoneResponse;
		if (!user.personId.equals(message.senderPersonId))
			return Util.httpForbiddenResponse;
		//
		// delete attachments (DB objects and files in the file system)
		Iterable<SymbolicFile> symbolicFiles = symbolicFileRepository.findByReferenceIdOrderByPositionAsc(messageId);
		for (SymbolicFile symbolicFile : symbolicFiles) {
			File file = Util.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId);
			if (file == null)
				return Util.httpInternalErrorResponse;
			if (file.exists())
				file.delete();
		}
		symbolicFileRepository.delete(symbolicFiles);
		// delete text and mark message as deleted
		message.text = null;
		message.isDeleted = true;
		message.updatedAt = System.currentTimeMillis();
		messageRepository.save(message);
		//
		return Util.httpOkResponse;
	}

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
				// save without lock by saving the dependent items first
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

	private Iterable<Message> getMessagesByPage(String roomId, int count, Long until) {
		if (count > 0) {
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
				return messageRepository.findByRoomIdAndPostedAtBetween(roomId, min, max);
			}
		}
		return new ArrayList<>();
	}

	/* API JSON formats */

	/* the following JSON formats are simply different from the straight-forward entity JSON formats */

	private JSONObject personToJson(Person person) throws JSONException {
		if (person == null)
			return null;
		JSONObject item = new JSONObject();
		item.put("personId", person.personId);
		JsonUtil.put(item, "lastName", person.lastName);
		JsonUtil.put(item, "firstName", person.firstName);
		JsonUtil.put(item, "nickName", person.nickName);
		JsonUtil.put(item, "pictureId", person.pictureId);
		return item;
	}

	private JSONObject confirmationToJson(ViewedConfirmation confirmation) throws JSONException {
		if (confirmation == null)
			return null;
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

	private JSONObject symbolicFileToJson(SymbolicFile symbolicFile) throws JSONException {
		if (symbolicFile == null)
			return null;
		JSONObject sapSportsFile = new JSONObject();
		sapSportsFile.put("fileId", symbolicFile.fileId);
		sapSportsFile.put("mimeType", symbolicFile.mimeType);
		JSONObject attachmentContent = new JSONObject();
		JsonUtil.put(attachmentContent, "text", symbolicFile.text);
		JsonUtil.put(attachmentContent, "mimeType", "application/vnd.sap.sports.file");
		JsonUtil.put(attachmentContent, "sapSportsFile", sapSportsFile);
		JSONObject item = new JSONObject();
		item.put("assetId", symbolicFile.fileId);
		item.put("assetContent", attachmentContent);
		return item;
	}

	private JSONArray symbolicFilesToJsonArray(Iterable<SymbolicFile> symbolicFiles) throws JSONException {
		JSONArray array = new JSONArray();
		for (SymbolicFile symbolicFile : symbolicFiles)
			array.put(symbolicFileToJson(symbolicFile));
		return array;
	}

	/* the following JSON formats are complex and include data from outside the actual entity parsed */

	private JSONObject messageToJson(Message message, String personId, Iterable<SymbolicFile> symbolicFiles) throws JSONException {
		if (message == null)
			return null;
		JSONObject messageContent = new JSONObject();
		messageContent.put("roomId", message.roomId);
		messageContent.put("senderPersonId", message.senderPersonId);
		messageContent.put("postedAt", JsonUtil.toIsoDate(message.postedAt));
		messageContent.put("text", message.text);
		messageContent.put("isOwnMessage", personId.equals(message.senderPersonId));
		if (symbolicFiles != null)
			messageContent.put("assets", symbolicFilesToJsonArray(symbolicFiles));
		JSONObject messageStatus = new JSONObject();
		messageStatus.put("isDeleted", message.isDeleted);
		messageStatus.put("updatedAt", JsonUtil.toIsoDate(message.updatedAt));
		JSONObject postedMessageStatus = new JSONObject();
		postedMessageStatus.put("viewedCount", viewedConfirmationRepository.countByMessageIdAndPersonIdNot(message.messageId, personId));
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

	private JSONObject messageToJson(Message message, String personId) throws JSONException {
		if (message == null)
			return null;
		return messageToJson(message, personId, symbolicFileRepository.findByReferenceIdOrderByPositionAsc(message.messageId));
	}

	private JSONArray messagesToJsonArray(Iterable<Message> messages, String personId) throws JSONException {
		JSONArray array = new JSONArray();
		for (Message message : messages)
			array.put(messageToJson(message, personId));
		return array;
	}

	private String prefix(String str, int n) {
		return str == null || str.length() <= n ? str : str.substring(0, n);
	}

	private String[] getPrivateRoomNames(String roomId, String personId) {
		String roomName = null;
		String shortRoomName = null;
		RoomMember partnerMember = roomMemberRepository.findTopByRoomIdAndPersonIdNot(roomId, personId);
		Person partner = partnerMember == null || partnerMember.personId == null ? null : personRepository.findOne(partnerMember.personId);
		if (partner != null) {
			String nickName = partner.nickName == null ? "" : partner.nickName.trim();
			String firstName = partner.firstName == null ? "" : partner.firstName.trim();
			String lastName = partner.lastName == null ? "" : partner.lastName.trim();
			roomName = nickName.length() == 0 ? (firstName + " " + lastName).trim() : nickName;
			shortRoomName = prefix(nickName, 4).trim();
			if (shortRoomName.length() == 0)
				shortRoomName = prefix(firstName, 1) + prefix(lastName, 1);
		}
		return new String[] {roomName, shortRoomName};
	}

	private JSONObject roomToJson(Room room, JSONArray currentMemberIds, String personId) throws JSONException {
		if (room == null)
			return null;
		boolean isPrivateRoom = "private".equals(room.roomType);
		String[] privateRoomNames = isPrivateRoom ? getPrivateRoomNames(room.roomId, personId) : null;
		JSONObject roomStatus = new JSONObject();
		roomStatus.put("dataChangedAt", JsonUtil.toIsoDate(room.changedAt));
		JSONObject roomData = new JSONObject();
		JsonUtil.put(roomData, "name", isPrivateRoom ? privateRoomNames[0] : room.name);
		JsonUtil.put(roomData, "shortName", isPrivateRoom ? privateRoomNames[1] : room.shortName);
		JsonUtil.put(roomData, "roomType", room.roomType);
		JsonUtil.put(roomData, "pictureId", room.pictureId);
		JsonUtil.put(roomData, "currentMemberIds", currentMemberIds);
		JSONObject roomContent = new JSONObject();
		Long watermark = getWatermark(personId, room.roomId);
		if (watermark != null) {
			long count = messageRepository.countByRoomIdAndIsDeletedFalseAndPostedAtGreaterThan(room.roomId, watermark);
			roomContent.put("badgeCount", count);
		}
		Message latestMessage = messageRepository.findTopByRoomIdAndIsDeletedFalseOrderByPostedAtDesc(room.roomId);
		JsonUtil.put(roomContent, "latestMessage", messageToJson(latestMessage, personId));
		JSONObject item = new JSONObject();
		item.put("roomId", room.roomId);
		item.put("roomStatus", roomStatus);
		item.put("roomData", roomData);
		item.put("roomContent", roomContent);
		return item;
	}

	private JSONArray roomsToJsonArray(Iterable<Room> rooms, Iterable<RoomMember> roomMembers, String personId) throws JSONException {
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
			if (userRooms.contains(room.roomId))
				array.put(roomToJson(room, membersMap.get(room.roomId), personId));
		return array;
	}

	private class ProtoSymbolicFile {
		String fileId;
		String clientId;
		String text;
		String mimeType;

		public ProtoSymbolicFile() {
			fileId = Util.getUuid();
		}
	}

	private ProtoSymbolicFile getProtoSymbolicFile(JSONObject item) throws JSONException {
		if (item == null)
			return null;
		ProtoSymbolicFile protoSymbolicFile = new ProtoSymbolicFile();
		protoSymbolicFile.clientId = JsonUtil.getString(item, "payloadMultipartName");
		if (protoSymbolicFile.clientId == null)
			return null;
		protoSymbolicFile.text = JsonUtil.getString(item, "text");
		return protoSymbolicFile;
	}

	private class ProtoMessage {
		String messageId;
		String clientId;
		String text;
		List<ProtoSymbolicFile> protoSymbolicFiles;

		public ProtoMessage() {
			messageId = Util.getUuid();
		}
	}

	private ProtoMessage getProtoMessage(JSONObject item) throws JSONException {
		if (item == null)
			return null;
		ProtoMessage protoMessage = new ProtoMessage();
		protoMessage.clientId = JsonUtil.getString(item, "clientMessageId");
		if (protoMessage.clientId == null)
			return null;
		JSONObject messageContent = JsonUtil.getJSONObject(item, "messageContent");
		if (messageContent == null)
			return null;
		protoMessage.text = JsonUtil.getString(messageContent, "text");
		JSONArray attachments = JsonUtil.getJSONArray(messageContent, "assets");
		if (attachments != null) {
			protoMessage.protoSymbolicFiles = new ArrayList<>();
			for (int i = 0; i < attachments.length(); i++) {
				JSONObject attachmentContent = JsonUtil.getJSONObject(attachments.getJSONObject(i), "assetContent");
				ProtoSymbolicFile protoSymbolicFile = getProtoSymbolicFile(attachmentContent);
				if (protoSymbolicFile == null)
					return null;
				protoMessage.protoSymbolicFiles.add(protoSymbolicFile);
			}
		}
		return protoMessage;
	}

	private Subscription getSubscription(JSONObject item, String userId) throws JSONException {
		if (item == null)
			return null;
		Subscription subscription = new Subscription();
		subscription.targetType = JsonUtil.getString(item, "targetType");
		subscription.appId = JsonUtil.getString(item, "appId");
		subscription.deviceToken = JsonUtil.getString(item, "deviceToken");
		subscription.userId = userId;
		subscription.language = JsonUtil.getString(item, "language");
		subscription.clientAccountId = JsonUtil.getString(item, "clientAccountId");
		subscription.userConsent = JsonUtil.getBoolean(item, "userConsent", Boolean.FALSE);
		subscription.isActive = JsonUtil.getBoolean(item, "isActive", Boolean.TRUE);
		subscription.changedAt = System.currentTimeMillis();
		JSONObject stats = JsonUtil.getJSONObject(item, "stats");
		subscription.deviceId = JsonUtil.getString(stats, "deviceId");
		subscription.deviceType = JsonUtil.getString(stats, "deviceType");
		subscription.osVersion = JsonUtil.getString(stats, "osVersion");
		subscription.encryption = JsonUtil.getString(stats, "encryption");
		subscription.appVersion = JsonUtil.getString(stats, "appVersion");
		return subscription;
	}

	private SubscriptionKey getSubscriptionKey(JSONObject item, String userId) throws JSONException {
		if (item == null)
			return null;
		SubscriptionKey subscriptionKey = new SubscriptionKey();
		subscriptionKey.targetType = JsonUtil.getString(item, "targetType");
		subscriptionKey.appId = JsonUtil.getString(item, "appId");
		subscriptionKey.deviceToken = JsonUtil.getString(item, "deviceToken");
		subscriptionKey.userId = userId;
		return subscriptionKey;
	}

}

