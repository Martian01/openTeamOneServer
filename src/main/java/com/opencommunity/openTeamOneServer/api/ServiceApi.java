package com.opencommunity.openTeamOneServer.api;

import com.opencommunity.openTeamOneServer.data.*;
import com.opencommunity.openTeamOneServer.persistence.*;
import com.opencommunity.openTeamOneServer.util.JsonUtil;
import com.opencommunity.openTeamOneServer.util.Util;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/svc")
public class ServiceApi {

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

	/* AJAX Services (with JSON responses) */

	@RequestMapping(method = RequestMethod.GET, value = "/sessioninfo")
	public ResponseEntity<String>svcSession(HttpServletRequest request) throws JSONException {
		String sessionId = Util.getSessionId(request);
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		//
		JSONObject body = new JSONObject();
		JSONObject item;
		if (session != null) {
			item = new JSONObject();
			item.put("startTime", JsonUtil.toIsoDate(session.startTime));
			item.put("lastAccessTime", JsonUtil.toIsoDate(session.lastAccessTime));
			body.put("session", item);
			User user = session.userId == null ? null : userRepository.findOne(session.userId);
			if (user != null) {
				item = new JSONObject();
				item.put("userId", user.userId);
				item.put("hasAdminRole", user.hasAdminRole);
				item.put("hasUserRole", user.hasUserRole);
				body.put("user", item);
				Person person = user.personId == null ? null : personRepository.findOne(user.personId);
				if (person != null)
					body.put("person", person.toJson());
				if (user.hasAdminRole) {
					TenantParameter tp = tenantParameterRepository.findOne("dataDirectory");
					if (tp != null && tp.value != null)
						body.put("dataDirectory", tp.value);
				}
			}
		}
		//
		return Util.httpOkResponse(body);
	}

	/* CRUD Services for TenantParameter */

	@RequestMapping(method = RequestMethod.GET, value = "/parameters")
	public ResponseEntity<String> parametersGet(HttpServletRequest request) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		return Util.httpOkResponse(TenantParameter.toJsonArray(tenantParameterRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/parameter/{parameterId}")
	public ResponseEntity<String> parameterGet(HttpServletRequest request, @PathVariable String parameterId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		TenantParameter parameter = parameterId == null ? null : tenantParameterRepository.findOne(parameterId);
		if (parameter == null)
			return Util.httpNotFoundResponse;
		//
		return Util.httpOkResponse(parameter.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/parameter/{parameterId}")
	public ResponseEntity<String> parameterDelete(HttpServletRequest request, @PathVariable String parameterId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		TenantParameter parameter = parameterId == null ? null : tenantParameterRepository.findOne(parameterId);
		if (parameter == null)
			return Util.httpGoneResponse;
		tenantParameterRepository.delete(parameter);
		//
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/parameter")
	public ResponseEntity<String> parameterPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		TenantParameter parameter = new TenantParameter(new JSONObject(requestBody));
		tenantParameterRepository.save(parameter);
		//
		return Util.httpResponse(parameter.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for User */

	@RequestMapping(method = RequestMethod.GET, value = "/users")
	public ResponseEntity<String> usersGet(HttpServletRequest request) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		return Util.httpOkResponse(User.toJsonArray(userRepository.findAll(), false));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/user/{userId}")
	public ResponseEntity<String> userGet(HttpServletRequest request, @PathVariable String userId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		User targetUser = userId == null ? null : userRepository.findOne(userId);
		if (targetUser == null)
			return Util.httpNotFoundResponse;
		//
		return Util.httpOkResponse(targetUser.toJson(false));
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/user/{userId}")
	public ResponseEntity<String> userDelete(HttpServletRequest request, @PathVariable String userId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		User targetUser = userId == null ? null : userRepository.findOne(userId);
		if (targetUser == null)
			return Util.httpGoneResponse;
		// it is not allowed to delete the logon user
		if (user.userId.equals(userId))
			return Util.httpForbiddenResponse;
		userRepository.delete(targetUser);
		//
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/user")
	public ResponseEntity<String> userPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		User targetUser = new User(new JSONObject(requestBody));
		// it is not allowed to remove the admin role from the logon user
		if (user.userId.equals(targetUser.userId) && !targetUser.hasAdminRole)
			return Util.httpForbiddenResponse;
		// if password was not provided, try and re-use existing password
		if (targetUser.passwordHash == null) {
			User oldUser = userRepository.findOne(targetUser.userId);
			if (oldUser != null)
				targetUser.passwordHash = oldUser.passwordHash;
		}
		userRepository.save(targetUser);
		//
		return Util.httpResponse(targetUser.toJson(false), HttpStatus.CREATED);
	}

	/* CRUD Services for Person */

	@RequestMapping(method = RequestMethod.GET, value = "/persons")
	public ResponseEntity<String> personsGet(HttpServletRequest request) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		return Util.httpOkResponse(Person.toJsonArray(personRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/person/{personId}")
	public ResponseEntity<String> personGet(HttpServletRequest request, @PathVariable String personId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		Person person = personId == null ? null : personRepository.findOne(personId);
		if (person == null)
			return Util.httpNotFoundResponse;
		//
		return Util.httpOkResponse(person.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/person/{personId}")
	public ResponseEntity<String> personDelete(HttpServletRequest request, @PathVariable String personId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		Person person = personId == null ? null : personRepository.findOne(personId);
		if (person == null)
			return Util.httpGoneResponse;
		personRepository.delete(person);
		//
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/person")
	public ResponseEntity<String> personPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		Person person = new Person(new JSONObject(requestBody));
		personRepository.save(person);
		//
		return Util.httpResponse(person.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for Room */

	@RequestMapping(method = RequestMethod.GET, value = "/rooms")
	public ResponseEntity<String> roomsGet(HttpServletRequest request) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		return Util.httpOkResponse(Room.toJsonArray(roomRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}")
	public ResponseEntity<String> roomGet(HttpServletRequest request, @PathVariable String roomId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		Room room = roomId == null ? null : roomRepository.findOne(roomId);
		if (room == null)
			return Util.httpNotFoundResponse;
		//
		return Util.httpOkResponse(room.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/room/{roomId}")
	public ResponseEntity<String> roomDelete(HttpServletRequest request, @PathVariable String roomId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		Room room = roomId == null ? null : roomRepository.findOne(roomId);
		if (room == null)
			return Util.httpGoneResponse;
		roomRepository.delete(room);
		//
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room")
	public ResponseEntity<String> roomPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		Room room = new Room(new JSONObject(requestBody));
		roomRepository.save(room);
		//
		return Util.httpResponse(room.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for RoomMember */

	@RequestMapping(method = RequestMethod.GET, value = "/members")
	public ResponseEntity<String> roomMembersGet(HttpServletRequest request) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		return Util.httpOkResponse(RoomMember.toJsonArray(roomMemberRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/member/{roomId}/{personId}")
	public ResponseEntity<String> roomMemberGet(HttpServletRequest request, @PathVariable String roomId, @PathVariable String personId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		RoomMember roomMember = roomId == null || personId == null ? null : roomMemberRepository.findTopByRoomIdAndPersonId(roomId, personId);
		if (roomMember == null)
			return Util.httpNotFoundResponse;
		//
		return Util.httpOkResponse(roomMember.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/member/{roomId}/{personId}")
	public ResponseEntity<String> roomMemberDelete(HttpServletRequest request, @PathVariable String roomId, @PathVariable String personId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		RoomMember roomMember = roomId == null || personId == null ? null : roomMemberRepository.findTopByRoomIdAndPersonId(roomId, personId);
		if (roomMember == null)
			return Util.httpGoneResponse;
		roomMemberRepository.delete(roomMember);
		//
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/member")
	public ResponseEntity<String> roomMemberPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		RoomMember roomMember = new RoomMember(new JSONObject(requestBody));
		if (roomMember.roomId == null || roomMember.personId == null)
			return Util.httpBadRequestResponse;
		roomMemberRepository.save(roomMember);
		//
		return Util.httpResponse(roomMember.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for Message */

	@RequestMapping(method = RequestMethod.GET, value = "/messages")
	public ResponseEntity<String> messagesGet(HttpServletRequest request) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		return Util.httpOkResponse(Message.toJsonArray(messageRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/message/{messageId}")
	public ResponseEntity<String> messageGet(HttpServletRequest request, @PathVariable String messageId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		Message message = messageId == null ? null : messageRepository.findOne(messageId);
		if (message == null)
			return Util.httpNotFoundResponse;
		//
		return Util.httpOkResponse(message.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/message/{messageId}")
	public ResponseEntity<String> messageDelete(HttpServletRequest request, @PathVariable String messageId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		Message message = messageId == null ? null : messageRepository.findOne(messageId);
		if (message == null)
			return Util.httpGoneResponse;
		messageRepository.delete(message);
		//
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/message")
	public ResponseEntity<String> messagePost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		Message message = new Message(new JSONObject(requestBody));
		messageRepository.save(message);
		//
		return Util.httpResponse(message.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for ViewedConfirmation */

	@RequestMapping(method = RequestMethod.GET, value = "/confirmations")
	public ResponseEntity<String> viewedConfirmationsGet(HttpServletRequest request) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		return Util.httpOkResponse(ViewedConfirmation.toJsonArray(viewedConfirmationRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/confirmation/{messageId}/{personId}")
	public ResponseEntity<String> viewedConfirmationGet(HttpServletRequest request, @PathVariable String messageId, @PathVariable String personId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		ViewedConfirmation viewedConfirmation = messageId == null || personId == null ? null : viewedConfirmationRepository.findTopByMessageIdAndPersonId(messageId, personId);
		if (viewedConfirmation == null)
			return Util.httpNotFoundResponse;
		//
		return Util.httpOkResponse(viewedConfirmation.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/confirmation/{messageId}/{personId}")
	public ResponseEntity<String> viewedConfirmationDelete(HttpServletRequest request, @PathVariable String messageId, @PathVariable String personId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		ViewedConfirmation viewedConfirmation = messageId == null || personId == null ? null : viewedConfirmationRepository.findTopByMessageIdAndPersonId(messageId, personId);
		if (viewedConfirmation == null)
			return Util.httpGoneResponse;
		viewedConfirmationRepository.delete(viewedConfirmation);
		//
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/confirmation")
	public ResponseEntity<String> viewedConfirmationPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		ViewedConfirmation viewedConfirmation = new ViewedConfirmation(new JSONObject(requestBody));
		if (viewedConfirmation.messageId == null || viewedConfirmation.personId == null)
			return Util.httpBadRequestResponse;
		viewedConfirmationRepository.save(viewedConfirmation);
		//
		return Util.httpResponse(viewedConfirmation.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for Subscription */

	@RequestMapping(method = RequestMethod.GET, value = "/subscriptions")
	public ResponseEntity<String> subscriptionsGet(HttpServletRequest request) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		return Util.httpOkResponse(Subscription.toJsonArray(subscriptionRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/subscription/{targetType}/{appId}/{deviceToken}/{userId}")
	public ResponseEntity<String> subscriptionGet(HttpServletRequest request, @PathVariable String targetType, @PathVariable String appId, @PathVariable String deviceToken, @PathVariable String userId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (!user.userId.equals(userId))
			return Util.httpForbiddenResponse;
		Subscription subscription = targetType == null || appId == null || deviceToken == null || userId == null ? null : subscriptionRepository.findTopByTargetTypeAndAppIdAndDeviceTokenAndUserId(targetType, appId, deviceToken, userId);
		if (subscription == null)
			return Util.httpNotFoundResponse;
		//
		return Util.httpOkResponse(subscription.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/subscription/{targetType}/{appId}/{deviceToken}/{userId}")
	public ResponseEntity<String> subscriptionDelete(HttpServletRequest request, @PathVariable String targetType, @PathVariable String appId, @PathVariable String deviceToken, @PathVariable String userId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		Subscription subscription = targetType == null || appId == null || deviceToken == null || userId == null ? null : subscriptionRepository.findTopByTargetTypeAndAppIdAndDeviceTokenAndUserId(targetType, appId, deviceToken, userId);
		if (subscription == null)
			return Util.httpGoneResponse;
		subscriptionRepository.delete(subscription);
		//
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/subscription")
	public ResponseEntity<String> subscriptionPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		Subscription subscription = new Subscription(new JSONObject(requestBody));
		if (subscription.targetType == null || subscription.appId == null || subscription.deviceToken == null || subscription.userId == null)
			return Util.httpBadRequestResponse;
		subscriptionRepository.save(subscription);
		//
		return Util.httpResponse(subscription.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for SymbolicFile */

	@RequestMapping(method = RequestMethod.GET, value = "/files")
	public ResponseEntity<String> symbolicFilesGet(HttpServletRequest request) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		return Util.httpOkResponse(SymbolicFile.toJsonArray(symbolicFileRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/file/{fileId}")
	public ResponseEntity<String> symbolicFileGet(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		SymbolicFile symbolicFile = fileId == null ? null : symbolicFileRepository.findOne(fileId);
		if (symbolicFile == null)
			return Util.httpNotFoundResponse;
		//
		return Util.httpOkResponse(symbolicFile.toJson());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/file/{fileId}/content")
	public ResponseEntity<Resource> symbolicFileContentGet(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResourceResponse;
		//
		SymbolicFile symbolicFile = symbolicFileRepository.findOne(fileId);
		if (symbolicFile == null)
			return Util.httpNotFoundResourceResponse;
		File file = Util.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId);
		if (file == null)
			return Util.httpInternalErrorResourceResponse;
		if (!file.canRead()) {
			return Util.httpNotFoundResourceResponse;
		}
		Resource resource = new ByteArrayResource(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
		return Util.httpResourceResponse(resource, MediaType.parseMediaType(symbolicFile.mimeType));
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/file/{fileId}")
	public ResponseEntity<String> symbolicFileDelete(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		SymbolicFile symbolicFile = fileId == null ? null : symbolicFileRepository.findOne(fileId);
		if (symbolicFile == null)
			return Util.httpGoneResponse;
		if (deleteFileContent(symbolicFile) == null)
			return Util.httpInternalErrorResponse;
		symbolicFileRepository.delete(symbolicFile);
		//
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/file")
	public ResponseEntity<String> personPut(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		JSONObject item = new JSONObject(requestBody);
		SymbolicFile symbolicFile = new SymbolicFile(item);
		symbolicFileRepository.save(symbolicFile);
		//
		return Util.httpCreatedResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/file")
	public ResponseEntity<String> symbolicFilePost(MultipartHttpServletRequest multipartRequest) throws Exception {
		Session session = Util.getSession(multipartRequest);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (multipartRequest == null)
			return Util.httpBadRequestResponse;
		String parameter = multipartRequest.getParameter("file");
		if (parameter == null)
			return Util.httpBadRequestResponse;
		SymbolicFile symbolicFile = new SymbolicFile(new JSONObject(parameter));
		//
		MultipartFile multipartFile = multipartRequest.getFile("fileContent");
		if (multipartFile == null)
			return Util.httpBadRequestResponse;
		// delete old content
		File file = deleteFileContent(symbolicFile);
		if (file == null)
			return Util.httpInternalErrorResponse;
		// save byte stream under same fileId
		Util.writeFile(multipartFile.getInputStream(), file);
		// save descriptor
		symbolicFileRepository.save(symbolicFile);
		//
		return Util.httpResponse(symbolicFile.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for Session */

	@RequestMapping(method = RequestMethod.GET, value = "/sessions")
	public ResponseEntity<String> sessionsGet(HttpServletRequest request) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		return Util.httpOkResponse(Session.toJsonArray());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/session/{sessionId}")
	public ResponseEntity<String> sessionGet(HttpServletRequest request, @PathVariable String sessionId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		Session targetSession = sessionId == null ? null : Session.findSession(sessionId);
		if (targetSession == null)
			return Util.httpNotFoundResponse;
		//
		return Util.httpOkResponse(targetSession.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/session/{sessionId}")
	public ResponseEntity<String> sessionDelete(HttpServletRequest request, @PathVariable String sessionId) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		Session targetSession = sessionId == null ? null : Session.findSession(sessionId);
		if (targetSession == null)
			return Util.httpGoneResponse;
		// it is not allowed to delete the logon session
		if (sessionId.equals(session.sessionId))
			return Util.httpForbiddenResponse;
		Session.invalidateSession(sessionId);
		//
		return Util.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/session")
	public ResponseEntity<String> sessionPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionAdmin(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		Session targetSession = new Session(new JSONObject(requestBody));
		// it is not allowed to modify the logon session
		if (targetSession.sessionId.equals(session.sessionId))
			return Util.httpForbiddenResponse;
		Session.updateSession(targetSession);
		//
		return Util.httpResponse(targetSession.toJson(), HttpStatus.CREATED);
	}

	/* User Self Service */

	@RequestMapping(method = RequestMethod.POST, value = "/self/picture")
	public ResponseEntity<String> selfPicturePost(MultipartHttpServletRequest multipartRequest) throws Exception {
		Session session = Util.getSession(multipartRequest);
		User user = Util.getSessionUser(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		Person person;
		if (user.personId == null) {
			person = new Person();
			user.personId = person.personId = Util.getUuid();
			userRepository.save(user);
		} else {
			person = personRepository.findOne(user.personId);
			if (person == null)
				return Util.httpInternalErrorResponse;
		}
		//
		if (multipartRequest == null)
			return Util.httpBadRequestResponse;
		String mimeType = multipartRequest.getParameter("mimeType");
		if (mimeType == null)
			return Util.httpBadRequestResponse;
		//
		MultipartFile multipartFile = multipartRequest.getFile("fileContent");
		if (multipartFile == null)
			return Util.httpBadRequestResponse;
		// delete old content and old symbolic file
		if (person.pictureId != null) {
			SymbolicFile oldSymbolicFile = symbolicFileRepository.findOne(person.pictureId);
			File file = deleteFileContent(oldSymbolicFile);
			if (file == null)
				return Util.httpInternalErrorResponse;
			symbolicFileRepository.delete(oldSymbolicFile.fileId);
		}
		// save byte stream under new fileId
		SymbolicFile symbolicFile = new SymbolicFile(null, mimeType, null, null, 0, SymbolicFile.DIRECTORY_PROFILES);
		File file = Util.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId);
		if (file == null) { // unlikely
			person.pictureId = null;
			personRepository.save(person);
			return Util.httpInternalErrorResponse;
		}
		Util.writeFile(multipartFile.getInputStream(), file);
		// save descriptor
		symbolicFileRepository.save(symbolicFile);
		// save person
		person.pictureId = symbolicFile.fileId;
		personRepository.save(person);
		//
		return Util.httpResponse(person.toJson(), HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/self/person")
	public ResponseEntity<String> selfDataPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionUser(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		Person person = new Person(new JSONObject(requestBody));
		if (user.personId == null) {
			user.personId = person.personId;
			userRepository.save(user);
		} else {
			if (!user.personId.equals(person.personId))
				return Util.httpForbiddenResponse;
			Person oldPerson = personRepository.findOne(user.personId);
			if (oldPerson == null)
				return Util.httpInternalErrorResponse;
			if (!Util.equal(oldPerson.pictureId, person.pictureId))
				return Util.httpForbiddenResponse;
		}
		personRepository.save(person);
		//
		return Util.httpResponse(person.toJson(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/self/password")
	public ResponseEntity<String> selfPasswordPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = Util.getSession(request);
		User user = Util.getSessionUser(session, userRepository);
		if (user == null)
			return Util.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return Util.httpBadRequestResponse;
		String password = new JSONObject(requestBody).getString("password");
		if (password == null || password.length() == 0)
			return Util.httpBadRequestResponse;
		user.setPassword(password);
		userRepository.save(user);
		//
		return Util.httpOkResponse;
	}

	/* helper functions */

	private File deleteFileContent(SymbolicFile symbolicFile) {
		if (symbolicFile == null)
			return null;
		File file = Util.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId);
		if (file != null && file.exists())
			file.delete();
		return file;
	}

}
