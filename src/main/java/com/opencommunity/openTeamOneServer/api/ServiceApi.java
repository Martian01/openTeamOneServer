package com.opencommunity.openTeamOneServer.api;

import com.opencommunity.openTeamOneServer.data.*;
import com.opencommunity.openTeamOneServer.persistence.*;
import com.opencommunity.openTeamOneServer.util.RestLib;
import com.opencommunity.openTeamOneServer.util.StreamUtil;
import com.opencommunity.openTeamOneServer.util.TimeUtil;
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
	@Autowired
	private RestLib restLib;

	/* AJAX Services (with JSON responses) */

	@RequestMapping(method = RequestMethod.GET, value = "/sessioninfo")
	public ResponseEntity<String>svcSession(HttpServletRequest request) throws JSONException {
		String sessionId = restLib.getSessionId(request);
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		//
		JSONObject body = new JSONObject();
		JSONObject item;
		if (session != null) {
			item = new JSONObject();
			item.put("startTime", TimeUtil.toIsoDateString(session.startTime));
			item.put("lastAccessTime", TimeUtil.toIsoDateString(session.lastAccessTime));
			body.put("session", item);
			User user = session.userId == null ? null : userRepository.findById(session.userId).orElse(null);
			if (user != null) {
				item = new JSONObject();
				item.put("userId", user.userId);
				item.put("hasAdminRole", user.hasAdminRole);
				item.put("hasUserRole", user.hasUserRole);
				body.put("user", item);
				Person person = user.personId == null ? null : personRepository.findById(user.personId).orElse(null);
				if (person != null)
					body.put("person", person.toJson());
				if (user.hasAdminRole) {
					TenantParameter tp = tenantParameterRepository.findById("dataDirectory").orElse(null);
					if (tp != null && tp.value != null)
						body.put("dataDirectory", tp.value);
				}
			}
		}
		//
		return restLib.httpOkResponse(body);
	}

	/* CRUD Services for TenantParameter */

	@RequestMapping(method = RequestMethod.GET, value = "/parameters")
	public ResponseEntity<String> parametersGet(HttpServletRequest request) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		return restLib.httpOkResponse(TenantParameter.toJsonArray(tenantParameterRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/parameter/{parameterId}")
	public ResponseEntity<String> parameterGet(HttpServletRequest request, @PathVariable String parameterId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		TenantParameter parameter = parameterId == null ? null : tenantParameterRepository.findById(parameterId).orElse(null);
		if (parameter == null)
			return restLib.httpNotFoundResponse;
		//
		return restLib.httpOkResponse(parameter.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/parameter/{parameterId}")
	public ResponseEntity<String> parameterDelete(HttpServletRequest request, @PathVariable String parameterId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		TenantParameter parameter = parameterId == null ? null : tenantParameterRepository.findById(parameterId).orElse(null);
		if (parameter == null)
			return restLib.httpGoneResponse;
		tenantParameterRepository.delete(parameter);
		//
		return restLib.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/parameter")
	public ResponseEntity<String> parameterPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		TenantParameter parameter = new TenantParameter(new JSONObject(requestBody));
		tenantParameterRepository.save(parameter);
		//
		return restLib.httpResponse(parameter.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for User */

	@RequestMapping(method = RequestMethod.GET, value = "/users")
	public ResponseEntity<String> usersGet(HttpServletRequest request) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		return restLib.httpOkResponse(User.toJsonArray(userRepository.findAll(), false));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/user/{userId}")
	public ResponseEntity<String> userGet(HttpServletRequest request, @PathVariable String userId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		User targetUser = userId == null ? null : userRepository.findById(userId).orElse(null);
		if (targetUser == null)
			return restLib.httpNotFoundResponse;
		//
		return restLib.httpOkResponse(targetUser.toJson(false));
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/user/{userId}")
	public ResponseEntity<String> userDelete(HttpServletRequest request, @PathVariable String userId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		User targetUser = userId == null ? null : userRepository.findById(userId).orElse(null);
		if (targetUser == null)
			return restLib.httpGoneResponse;
		// it is not allowed to delete the logon user
		if (user.userId.equals(userId))
			return restLib.httpForbiddenResponse;
		userRepository.delete(targetUser);
		//
		return restLib.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/user")
	public ResponseEntity<String> userPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		User targetUser = new User(new JSONObject(requestBody));
		// it is not allowed to remove the admin role from the logon user
		if (user.userId.equals(targetUser.userId) && !targetUser.hasAdminRole)
			return restLib.httpForbiddenResponse;
		// if password was not provided, try and re-use existing password
		if (targetUser.passwordHash == null) {
			User oldUser = userRepository.findById(targetUser.userId).orElse(null);
			if (oldUser != null)
				targetUser.passwordHash = oldUser.passwordHash;
		}
		userRepository.save(targetUser);
		//
		return restLib.httpResponse(targetUser.toJson(false), HttpStatus.CREATED);
	}

	/* CRUD Services for Person */

	@RequestMapping(method = RequestMethod.GET, value = "/persons")
	public ResponseEntity<String> personsGet(HttpServletRequest request) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		return restLib.httpOkResponse(Person.toJsonArray(personRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/person/{personId}")
	public ResponseEntity<String> personGet(HttpServletRequest request, @PathVariable Integer personId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Person person = personId == null ? null : personRepository.findById(personId).orElse(null);
		if (person == null)
			return restLib.httpNotFoundResponse;
		//
		return restLib.httpOkResponse(person.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/person/{personId}")
	public ResponseEntity<String> personDelete(HttpServletRequest request, @PathVariable Integer personId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Person person = personId == null ? null : personRepository.findById(personId).orElse(null);
		if (person == null)
			return restLib.httpGoneResponse;
		personRepository.delete(person);
		//
		return restLib.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/person")
	public ResponseEntity<String> personPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		Person person = new Person(new JSONObject(requestBody));
		personRepository.save(person);
		//
		return restLib.httpResponse(person.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for Room */

	@RequestMapping(method = RequestMethod.GET, value = "/rooms")
	public ResponseEntity<String> roomsGet(HttpServletRequest request) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		return restLib.httpOkResponse(Room.toJsonArray(roomRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}")
	public ResponseEntity<String> roomGet(HttpServletRequest request, @PathVariable Integer roomId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Room room = roomId == null ? null : roomRepository.findById(roomId).orElse(null);
		if (room == null)
			return restLib.httpNotFoundResponse;
		//
		return restLib.httpOkResponse(room.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/room/{roomId}")
	public ResponseEntity<String> roomDelete(HttpServletRequest request, @PathVariable Integer roomId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Room room = roomId == null ? null : roomRepository.findById(roomId).orElse(null);
		if (room == null)
			return restLib.httpGoneResponse;
		roomRepository.delete(room);
		//
		return restLib.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room")
	public ResponseEntity<String> roomPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		Room room = new Room(new JSONObject(requestBody));
		roomRepository.save(room);
		//
		return restLib.httpResponse(room.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for RoomMember */

	@RequestMapping(method = RequestMethod.GET, value = "/members")
	public ResponseEntity<String> roomMembersGet(HttpServletRequest request) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		return restLib.httpOkResponse(RoomMember.toJsonArray(roomMemberRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/member/{roomId}/{personId}")
	public ResponseEntity<String> roomMemberGet(HttpServletRequest request, @PathVariable Integer roomId, @PathVariable Integer personId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		RoomMember roomMember = roomId == null || personId == null ? null : roomMemberRepository.findTopByRoomIdAndPersonId(roomId, personId);
		if (roomMember == null)
			return restLib.httpNotFoundResponse;
		//
		return restLib.httpOkResponse(roomMember.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/member/{roomId}/{personId}")
	public ResponseEntity<String> roomMemberDelete(HttpServletRequest request, @PathVariable Integer roomId, @PathVariable Integer personId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		RoomMember roomMember = roomId == null || personId == null ? null : roomMemberRepository.findTopByRoomIdAndPersonId(roomId, personId);
		if (roomMember == null)
			return restLib.httpGoneResponse;
		roomMemberRepository.delete(roomMember);
		//
		return restLib.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/member")
	public ResponseEntity<String> roomMemberPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		RoomMember roomMember = new RoomMember(new JSONObject(requestBody));
		if (roomMember.roomId == null || roomMember.personId == null)
			return restLib.httpBadRequestResponse;
		roomMemberRepository.save(roomMember);
		//
		return restLib.httpResponse(roomMember.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for Message */

	@RequestMapping(method = RequestMethod.GET, value = "/messages")
	public ResponseEntity<String> messagesGet(HttpServletRequest request) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		return restLib.httpOkResponse(Message.toJsonArray(messageRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/message/{messageId}")
	public ResponseEntity<String> messageGet(HttpServletRequest request, @PathVariable Integer messageId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Message message = messageId == null ? null : messageRepository.findById(messageId).orElse(null);
		if (message == null)
			return restLib.httpNotFoundResponse;
		//
		return restLib.httpOkResponse(message.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/message/{messageId}")
	public ResponseEntity<String> messageDelete(HttpServletRequest request, @PathVariable Integer messageId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Message message = messageId == null ? null : messageRepository.findById(messageId).orElse(null);
		if (message == null)
			return restLib.httpGoneResponse;
		messageRepository.delete(message);
		//
		return restLib.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/message")
	public ResponseEntity<String> messagePost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		Message message = new Message(new JSONObject(requestBody));
		messageRepository.save(message);
		//
		return restLib.httpResponse(message.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for ViewedConfirmation */

	@RequestMapping(method = RequestMethod.GET, value = "/confirmations")
	public ResponseEntity<String> viewedConfirmationsGet(HttpServletRequest request) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		return restLib.httpOkResponse(ViewedConfirmation.toJsonArray(viewedConfirmationRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/confirmation/{messageId}/{personId}")
	public ResponseEntity<String> viewedConfirmationGet(HttpServletRequest request, @PathVariable Integer messageId, @PathVariable Integer personId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		ViewedConfirmation viewedConfirmation = messageId == null || personId == null ? null : viewedConfirmationRepository.findTopByMessageIdAndPersonId(messageId, personId);
		if (viewedConfirmation == null)
			return restLib.httpNotFoundResponse;
		//
		return restLib.httpOkResponse(viewedConfirmation.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/confirmation/{messageId}/{personId}")
	public ResponseEntity<String> viewedConfirmationDelete(HttpServletRequest request, @PathVariable Integer messageId, @PathVariable Integer personId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		ViewedConfirmation viewedConfirmation = messageId == null || personId == null ? null : viewedConfirmationRepository.findTopByMessageIdAndPersonId(messageId, personId);
		if (viewedConfirmation == null)
			return restLib.httpGoneResponse;
		viewedConfirmationRepository.delete(viewedConfirmation);
		//
		return restLib.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/confirmation")
	public ResponseEntity<String> viewedConfirmationPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		ViewedConfirmation viewedConfirmation = new ViewedConfirmation(new JSONObject(requestBody));
		if (viewedConfirmation.messageId == null || viewedConfirmation.personId == null)
			return restLib.httpBadRequestResponse;
		viewedConfirmationRepository.save(viewedConfirmation);
		//
		return restLib.httpResponse(viewedConfirmation.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for Subscription */

	@RequestMapping(method = RequestMethod.GET, value = "/subscriptions")
	public ResponseEntity<String> subscriptionsGet(HttpServletRequest request) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		return restLib.httpOkResponse(Subscription.toJsonArray(subscriptionRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/subscription/{targetType}/{appId}/{deviceToken}/{userId}")
	public ResponseEntity<String> subscriptionGet(HttpServletRequest request, @PathVariable String targetType, @PathVariable String appId, @PathVariable String deviceToken, @PathVariable String userId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (!user.userId.equals(userId))
			return restLib.httpForbiddenResponse;
		Subscription subscription = targetType == null || appId == null || deviceToken == null || userId == null ? null : subscriptionRepository.findTopByTargetTypeAndAppIdAndDeviceTokenAndUserId(targetType, appId, deviceToken, userId);
		if (subscription == null)
			return restLib.httpNotFoundResponse;
		//
		return restLib.httpOkResponse(subscription.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/subscription/{targetType}/{appId}/{deviceToken}/{userId}")
	public ResponseEntity<String> subscriptionDelete(HttpServletRequest request, @PathVariable String targetType, @PathVariable String appId, @PathVariable String deviceToken, @PathVariable String userId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Subscription subscription = targetType == null || appId == null || deviceToken == null || userId == null ? null : subscriptionRepository.findTopByTargetTypeAndAppIdAndDeviceTokenAndUserId(targetType, appId, deviceToken, userId);
		if (subscription == null)
			return restLib.httpGoneResponse;
		subscriptionRepository.delete(subscription);
		//
		return restLib.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/subscription")
	public ResponseEntity<String> subscriptionPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		Subscription subscription = new Subscription(new JSONObject(requestBody));
		if (subscription.targetType == null || subscription.appId == null || subscription.deviceToken == null || subscription.userId == null)
			return restLib.httpBadRequestResponse;
		subscriptionRepository.save(subscription);
		//
		return restLib.httpResponse(subscription.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for SymbolicFile */

	@RequestMapping(method = RequestMethod.GET, value = "/files")
	public ResponseEntity<String> symbolicFilesGet(HttpServletRequest request) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		return restLib.httpOkResponse(SymbolicFile.toJsonArray(symbolicFileRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/file/{fileId}")
	public ResponseEntity<String> symbolicFileGet(HttpServletRequest request, @PathVariable Integer fileId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		SymbolicFile symbolicFile = fileId == null ? null : symbolicFileRepository.findById(fileId).orElse(null);
		if (symbolicFile == null)
			return restLib.httpNotFoundResponse;
		//
		return restLib.httpOkResponse(symbolicFile.toJson());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/file/{fileId}/content")
	public ResponseEntity<Resource> symbolicFileContentGet(HttpServletRequest request, @PathVariable Integer fileId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResourceResponse;
		//
		SymbolicFile symbolicFile = symbolicFileRepository.findById(fileId).orElse(null);
		if (symbolicFile == null)
			return restLib.httpNotFoundResourceResponse;
		File file = StreamUtil.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId.toString());
		if (file == null)
			return restLib.httpInternalErrorResourceResponse;
		if (!file.canRead()) {
			return restLib.httpNotFoundResourceResponse;
		}
		Resource resource = new ByteArrayResource(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
		return restLib.httpResourceResponse(resource, MediaType.parseMediaType(symbolicFile.mimeType));
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/file/{fileId}")
	public ResponseEntity<String> symbolicFileDelete(HttpServletRequest request, @PathVariable Integer fileId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		SymbolicFile symbolicFile = fileId == null ? null : symbolicFileRepository.findById(fileId).orElse(null);
		if (symbolicFile == null)
			return restLib.httpGoneResponse;
		if (deleteFileContent(symbolicFile) == null)
			return restLib.httpInternalErrorResponse;
		symbolicFileRepository.delete(symbolicFile);
		//
		return restLib.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/file")
	public ResponseEntity<String> personPut(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		JSONObject item = new JSONObject(requestBody);
		SymbolicFile symbolicFile = new SymbolicFile(item);
		symbolicFileRepository.save(symbolicFile);
		//
		return restLib.httpCreatedResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/file")
	public ResponseEntity<String> symbolicFilePost(MultipartHttpServletRequest multipartRequest) throws Exception {
		Session session = restLib.getSession(multipartRequest);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (multipartRequest == null)
			return restLib.httpBadRequestResponse;
		String parameter = multipartRequest.getParameter("file");
		if (parameter == null)
			return restLib.httpBadRequestResponse;
		SymbolicFile symbolicFile = new SymbolicFile(new JSONObject(parameter));
		//
		MultipartFile multipartFile = multipartRequest.getFile("fileContent");
		if (multipartFile == null)
			return restLib.httpBadRequestResponse;
		// delete old content
		File file = deleteFileContent(symbolicFile);
		if (file == null)
			return restLib.httpInternalErrorResponse;
		// save byte stream under same fileId
		StreamUtil.writeFile(multipartFile.getInputStream(), file);
		// save descriptor
		symbolicFileRepository.save(symbolicFile);
		//
		return restLib.httpResponse(symbolicFile.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for Session */

	@RequestMapping(method = RequestMethod.GET, value = "/sessions")
	public ResponseEntity<String> sessionsGet(HttpServletRequest request) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		return restLib.httpOkResponse(Session.toJsonArray());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/session/{sessionId}")
	public ResponseEntity<String> sessionGet(HttpServletRequest request, @PathVariable String sessionId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Session targetSession = sessionId == null ? null : Session.findSession(sessionId);
		if (targetSession == null)
			return restLib.httpNotFoundResponse;
		//
		return restLib.httpOkResponse(targetSession.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/session/{sessionId}")
	public ResponseEntity<String> sessionDelete(HttpServletRequest request, @PathVariable String sessionId) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Session targetSession = sessionId == null ? null : Session.findSession(sessionId);
		if (targetSession == null)
			return restLib.httpGoneResponse;
		// it is not allowed to delete the logon session
		if (sessionId.equals(session.sessionId))
			return restLib.httpForbiddenResponse;
		Session.invalidateSession(sessionId);
		//
		return restLib.httpOkResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/session")
	public ResponseEntity<String> sessionPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionAdmin(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		Session targetSession = new Session(new JSONObject(requestBody));
		// it is not allowed to modify the logon session
		if (targetSession.sessionId.equals(session.sessionId))
			return restLib.httpForbiddenResponse;
		Session.updateSession(targetSession);
		//
		return restLib.httpResponse(targetSession.toJson(), HttpStatus.CREATED);
	}

	/* User Self Service */

	@RequestMapping(method = RequestMethod.POST, value = "/self/picture")
	public ResponseEntity<String> selfPicturePost(MultipartHttpServletRequest multipartRequest) throws Exception {
		Session session = restLib.getSession(multipartRequest);
		User user = restLib.getSessionUser(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		Person person;
		if (user.personId == null) {
			person = new Person();
			user.personId = person.personId = RestLib.getRandomInt();
			userRepository.save(user);
		} else {
			person = personRepository.findById(user.personId).orElse(null);
			if (person == null)
				return restLib.httpInternalErrorResponse;
		}
		//
		if (multipartRequest == null)
			return restLib.httpBadRequestResponse;
		String mimeType = multipartRequest.getParameter("mimeType");
		if (mimeType == null)
			return restLib.httpBadRequestResponse;
		//
		MultipartFile multipartFile = multipartRequest.getFile("fileContent");
		if (multipartFile == null)
			return restLib.httpBadRequestResponse;
		// delete old content and old symbolic file
		if (person.pictureId != null) {
			SymbolicFile oldSymbolicFile = symbolicFileRepository.findById(person.pictureId).orElse(null);
			File file = deleteFileContent(oldSymbolicFile);
			if (file == null)
				return restLib.httpInternalErrorResponse;
			symbolicFileRepository.deleteById(oldSymbolicFile.fileId);
		}
		// save byte stream under new fileId
		SymbolicFile symbolicFile = new SymbolicFile(null, mimeType, null, null, 0, SymbolicFile.DIRECTORY_PROFILES);
		File file = StreamUtil.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId.toString());
		if (file == null) { // unlikely
			person.pictureId = null;
			personRepository.save(person);
			return restLib.httpInternalErrorResponse;
		}
		StreamUtil.writeFile(multipartFile.getInputStream(), file);
		// save descriptor
		symbolicFileRepository.save(symbolicFile);
		// save person
		person.pictureId = symbolicFile.fileId;
		personRepository.save(person);
		//
		return restLib.httpResponse(person.toJson(), HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/self/person")
	public ResponseEntity<String> selfDataPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionUser(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		Person person = new Person(new JSONObject(requestBody));
		if (user.personId == null) {
			user.personId = person.personId;
			userRepository.save(user);
		} else {
			if (!user.personId.equals(person.personId))
				return restLib.httpForbiddenResponse;
			Person oldPerson = personRepository.findById(user.personId).orElse(null);
			if (oldPerson == null)
				return restLib.httpInternalErrorResponse;
			if (!RestLib.equal(oldPerson.pictureId, person.pictureId))
				return restLib.httpForbiddenResponse;
		}
		personRepository.save(person);
		//
		return restLib.httpResponse(person.toJson(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/self/password")
	public ResponseEntity<String> selfPasswordPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		Session session = restLib.getSession(request);
		User user = restLib.getSessionUser(session, userRepository);
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		if (requestBody == null)
			return restLib.httpBadRequestResponse;
		String password = new JSONObject(requestBody).getString("password");
		if (password == null || password.length() == 0)
			return restLib.httpBadRequestResponse;
		user.setPassword(password);
		userRepository.save(user);
		//
		return restLib.httpOkResponse;
	}

	/* helper functions */

	private File deleteFileContent(SymbolicFile symbolicFile) {
		if (symbolicFile == null)
			return null;
		File file = StreamUtil.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId.toString());
		if (file != null && file.exists())
			file.delete();
		return file;
	}

}
