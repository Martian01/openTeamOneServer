package com.opencommunity.openTeamOneServer;

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
import javax.validation.constraints.NotNull;
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

	@RequestMapping(method = RequestMethod.GET, value = "/session")
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
		return Util.httpStringResponse(body);
	}

	/* CRUD Services for TenantParameter */

	@RequestMapping(method = RequestMethod.GET, value = "/parameters")
	public ResponseEntity<String> parametersGet(HttpServletRequest request) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpStringResponse(TenantParameter.toJsonArray(tenantParameterRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/parameter/{parameterId}")
	public ResponseEntity<String> parameterGet(HttpServletRequest request, @PathVariable String parameterId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		TenantParameter parameter = parameterId == null ? null : tenantParameterRepository.findOne(parameterId);
		if (parameter == null)
			return Util.httpStringResponse(HttpStatus.NOT_FOUND);
		//
		return Util.httpStringResponse(parameter.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/parameter/{parameterId}")
	public ResponseEntity<String> parameterDelete(HttpServletRequest request, @PathVariable String parameterId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		TenantParameter parameter = parameterId == null ? null : tenantParameterRepository.findOne(parameterId);
		if (parameter == null)
			return Util.httpStringResponse(HttpStatus.GONE);
		tenantParameterRepository.delete(parameter);
		//
		return Util.httpStringResponse(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/parameter")
	public ResponseEntity<String> parameterPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		if (requestBody == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		TenantParameter parameter = new TenantParameter(new JSONObject(requestBody));
		tenantParameterRepository.save(parameter);
		//
		return Util.httpStringResponse(parameter.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for User */

	@RequestMapping(method = RequestMethod.GET, value = "/users")
	public ResponseEntity<String> usersGet(HttpServletRequest request) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpStringResponse(User.toJsonArray(userRepository.findAll(), false));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/user/{userId}")
	public ResponseEntity<String> userGet(HttpServletRequest request, @PathVariable String userId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		User targetUser = userId == null ? null : userRepository.findOne(userId);
		if (targetUser == null)
			return Util.httpStringResponse(HttpStatus.NOT_FOUND);
		//
		return Util.httpStringResponse(targetUser.toJson(false));
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/user/{userId}")
	public ResponseEntity<String> userDelete(HttpServletRequest request, @PathVariable String userId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		User targetUser = userId == null ? null : userRepository.findOne(userId);
		if (targetUser == null)
			return Util.httpStringResponse(HttpStatus.GONE);
		// it is not allowed to delete the logon user
		if (user.userId.equals(userId))
			return Util.httpStringResponse(HttpStatus.FORBIDDEN);
		userRepository.delete(targetUser);
		//
		return Util.httpStringResponse(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/user")
	public ResponseEntity<String> userPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		if (requestBody == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		User targetUser = new User(new JSONObject(requestBody));
		// it is not allowed to remove the admin role from the logon user
		if (user.userId.equals(targetUser.userId) && !targetUser.hasAdminRole)
			return Util.httpStringResponse(HttpStatus.FORBIDDEN);
		// if password was not provided, try and re-use existing password
		if (targetUser.passwordHash == null) {
			User oldUser = userRepository.findOne(targetUser.userId);
			if (oldUser != null)
				targetUser.passwordHash = oldUser.passwordHash;
		}
		userRepository.save(targetUser);
		//
		return Util.httpStringResponse(targetUser.toJson(false), HttpStatus.CREATED);
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

	/* CRUD Services for Room */

	@RequestMapping(method = RequestMethod.GET, value = "/rooms")
	public ResponseEntity<String> roomsGet(HttpServletRequest request) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpStringResponse(Room.toJsonArray(roomRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/room/{roomId}")
	public ResponseEntity<String> roomGet(HttpServletRequest request, @PathVariable String roomId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		Room room = roomId == null ? null : roomRepository.findOne(roomId);
		if (room == null)
			return Util.httpStringResponse(HttpStatus.NOT_FOUND);
		//
		return Util.httpStringResponse(room.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/room/{roomId}")
	public ResponseEntity<String> roomDelete(HttpServletRequest request, @PathVariable String roomId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		Room room = roomId == null ? null : roomRepository.findOne(roomId);
		if (room == null)
			return Util.httpStringResponse(HttpStatus.GONE);
		roomRepository.delete(room);
		//
		return Util.httpStringResponse(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/room")
	public ResponseEntity<String> roomPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		if (requestBody == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		Room room = new Room(new JSONObject(requestBody));
		roomRepository.save(room);
		//
		return Util.httpStringResponse(room.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for RoomMember */

	@RequestMapping(method = RequestMethod.GET, value = "/members")
	public ResponseEntity<String> roomMembersGet(HttpServletRequest request) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpStringResponse(RoomMember.toJsonArray(roomMemberRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/member/{roomId}/{personId}")
	public ResponseEntity<String> roomMemberGet(HttpServletRequest request, @PathVariable String roomId, @PathVariable String personId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		RoomMember roomMember = roomId == null || personId == null ? null : roomMemberRepository.findTopByRoomIdAndPersonId(roomId, personId);
		if (roomMember == null)
			return Util.httpStringResponse(HttpStatus.NOT_FOUND);
		//
		return Util.httpStringResponse(roomMember.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/member/{roomId}/{personId}")
	public ResponseEntity<String> roomMemberDelete(HttpServletRequest request, @PathVariable String roomId, @PathVariable String personId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		RoomMember roomMember = roomId == null || personId == null ? null : roomMemberRepository.findTopByRoomIdAndPersonId(roomId, personId);
		if (roomMember == null)
			return Util.httpStringResponse(HttpStatus.GONE);
		roomMemberRepository.delete(roomMember);
		//
		return Util.httpStringResponse(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/member")
	public ResponseEntity<String> roomMemberPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		if (requestBody == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		RoomMember roomMember = new RoomMember(new JSONObject(requestBody));
		if (roomMember.roomId == null || roomMember.personId == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		roomMemberRepository.save(roomMember);
		//
		return Util.httpStringResponse(roomMember.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for Message */

	@RequestMapping(method = RequestMethod.GET, value = "/messages")
	public ResponseEntity<String> messagesGet(HttpServletRequest request) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpStringResponse(Message.toJsonArray(messageRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/message/{messageId}")
	public ResponseEntity<String> messageGet(HttpServletRequest request, @PathVariable String messageId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		Message message = messageId == null ? null : messageRepository.findOne(messageId);
		if (message == null)
			return Util.httpStringResponse(HttpStatus.NOT_FOUND);
		//
		return Util.httpStringResponse(message.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/message/{messageId}")
	public ResponseEntity<String> messageDelete(HttpServletRequest request, @PathVariable String messageId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		Message message = messageId == null ? null : messageRepository.findOne(messageId);
		if (message == null)
			return Util.httpStringResponse(HttpStatus.GONE);
		messageRepository.delete(message);
		//
		return Util.httpStringResponse(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/message")
	public ResponseEntity<String> messagePost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		if (requestBody == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		Message message = new Message(new JSONObject(requestBody));
		messageRepository.save(message);
		//
		return Util.httpStringResponse(message.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for ViewedConfirmation */

	@RequestMapping(method = RequestMethod.GET, value = "/confirmations")
	public ResponseEntity<String> viewedConfirmationsGet(HttpServletRequest request) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpStringResponse(ViewedConfirmation.toJsonArray(viewedConfirmationRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/confirmation/{messageId}/{personId}")
	public ResponseEntity<String> viewedConfirmationGet(HttpServletRequest request, @PathVariable String messageId, @PathVariable String personId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		ViewedConfirmation viewedConfirmation = messageId == null || personId == null ? null : viewedConfirmationRepository.findTopByMessageIdAndPersonId(messageId, personId);
		if (viewedConfirmation == null)
			return Util.httpStringResponse(HttpStatus.NOT_FOUND);
		//
		return Util.httpStringResponse(viewedConfirmation.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/confirmation/{messageId}/{personId}")
	public ResponseEntity<String> viewedConfirmationDelete(HttpServletRequest request, @PathVariable String messageId, @PathVariable String personId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		ViewedConfirmation viewedConfirmation = messageId == null || personId == null ? null : viewedConfirmationRepository.findTopByMessageIdAndPersonId(messageId, personId);
		if (viewedConfirmation == null)
			return Util.httpStringResponse(HttpStatus.GONE);
		viewedConfirmationRepository.delete(viewedConfirmation);
		//
		return Util.httpStringResponse(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/confirmation")
	public ResponseEntity<String> viewedConfirmationPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		if (requestBody == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		ViewedConfirmation viewedConfirmation = new ViewedConfirmation(new JSONObject(requestBody));
		if (viewedConfirmation.messageId == null || viewedConfirmation.personId == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		viewedConfirmationRepository.save(viewedConfirmation);
		//
		return Util.httpStringResponse(viewedConfirmation.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for Subscription */

	@RequestMapping(method = RequestMethod.GET, value = "/subscriptions")
	public ResponseEntity<String> subscriptionsGet(HttpServletRequest request) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpStringResponse(Subscription.toJsonArray(subscriptionRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/subscription/{targetType}/{appId}/{deviceToken}/{userId}")
	public ResponseEntity<String> subscriptionGet(HttpServletRequest request, @PathVariable String targetType, @PathVariable String appId, @PathVariable String deviceToken, @PathVariable String userId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		Subscription subscription = targetType == null || appId == null || deviceToken == null || userId == null ? null : subscriptionRepository.findTopByTargetTypeAndAppIdAndDeviceTokenAndUserId(targetType, appId, deviceToken, userId);
		if (subscription == null)
			return Util.httpStringResponse(HttpStatus.NOT_FOUND);
		//
		return Util.httpStringResponse(subscription.toJson());
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/subscription/{targetType}/{appId}/{deviceToken}/{userId}")
	public ResponseEntity<String> subscriptionDelete(HttpServletRequest request, @PathVariable String targetType, @PathVariable String appId, @PathVariable String deviceToken, @PathVariable String userId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		Subscription subscription = targetType == null || appId == null || deviceToken == null || userId == null ? null : subscriptionRepository.findTopByTargetTypeAndAppIdAndDeviceTokenAndUserId(targetType, appId, deviceToken, userId);
		if (subscription == null)
			return Util.httpStringResponse(HttpStatus.GONE);
		subscriptionRepository.delete(subscription);
		//
		return Util.httpStringResponse(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/subscription")
	public ResponseEntity<String> subscriptionPost(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		if (requestBody == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		Subscription subscription = new Subscription(new JSONObject(requestBody));
		if (subscription.targetType == null || subscription.appId == null || subscription.deviceToken == null || subscription.userId == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		subscriptionRepository.save(subscription);
		//
		return Util.httpStringResponse(subscription.toJson(), HttpStatus.CREATED);
	}

	/* CRUD Services for SymbolicFile */

	@RequestMapping(method = RequestMethod.GET, value = "/files")
	public ResponseEntity<String> symbolicFilesGet(HttpServletRequest request) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpStringResponse(SymbolicFile.toJsonArray(symbolicFileRepository.findAll()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/file/{fileId}")
	public ResponseEntity<String> symbolicFileGet(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		SymbolicFile symbolicFile = fileId == null ? null : symbolicFileRepository.findOne(fileId);
		if (symbolicFile == null)
			return Util.httpStringResponse(HttpStatus.NOT_FOUND);
		//
		return Util.httpStringResponse(symbolicFile.toJson());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/file/{fileId}/content")
	public ResponseEntity<Resource> symbolicFileContentGet(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpResourceResponse(HttpStatus.UNAUTHORIZED);
		//
		return sendFileContent(fileId);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/file/{fileId}")
	public ResponseEntity<String> symbolicFileDelete(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		SymbolicFile symbolicFile = fileId == null ? null : symbolicFileRepository.findOne(fileId);
		if (symbolicFile == null)
			return Util.httpStringResponse(HttpStatus.GONE);
		if (!deleteFileContent(symbolicFile))
			return Util.httpStringResponse(HttpStatus.INTERNAL_SERVER_ERROR);
		symbolicFileRepository.delete(symbolicFile);
		//
		return Util.httpStringResponse(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/file")
	public ResponseEntity<String> personPut(HttpServletRequest request, @RequestBody String requestBody) throws Exception {
		User user = Util.getSessionAdmin(request, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		if (requestBody == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		JSONObject item = new JSONObject(requestBody);
		SymbolicFile symbolicFile = new SymbolicFile(item);
		symbolicFileRepository.save(symbolicFile);
		//
		return Util.httpStringResponse(HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/file")
	public ResponseEntity<String> symbolicFilePost(MultipartHttpServletRequest multipartRequest) throws Exception {
		User user = Util.getSessionAdmin(multipartRequest, userRepository);
		if (user == null)
			return Util.httpStringResponse(HttpStatus.UNAUTHORIZED);
		//
		if (multipartRequest == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		String parameter = multipartRequest.getParameter("file");
		if (parameter == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		SymbolicFile symbolicFile = new SymbolicFile(new JSONObject(parameter));
		// save byte stream
		MultipartFile multipartFile = multipartRequest.getFile("fileContent");
		if (multipartFile == null)
			return Util.httpStringResponse(HttpStatus.BAD_REQUEST);
		File directory = Util.getDataDirectory(tenantParameterRepository, symbolicFile.directory);
		if (directory == null)
			return Util.httpStringResponse(HttpStatus.INTERNAL_SERVER_ERROR);
		File file = new File(directory, symbolicFile.fileId);
		Util.writeFile(multipartFile.getInputStream(), file);
		// save descriptor
		deleteFileContent(symbolicFileRepository.findOne(symbolicFile.fileId));
		symbolicFileRepository.save(symbolicFile);
		//
		return Util.httpStringResponse(symbolicFile.toJson(), HttpStatus.CREATED);
	}

	/* helper functions */

	private boolean deleteFileContent(SymbolicFile symbolicFile) {
		if (symbolicFile == null)
			return false;
		File file = Util.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId);
		if (file == null)
			return false;
		if (file.exists())
			file.delete();
		return true;
	}

	private ResponseEntity<Resource> sendFileContent(String fileId) throws Exception {
		SymbolicFile symbolicFile = symbolicFileRepository.findOne(fileId);
		if (symbolicFile == null)
			return Util.httpResourceResponse(HttpStatus.NOT_FOUND);
		File file = Util.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId);
		if (file == null)
			return Util.httpResourceResponse(HttpStatus.INTERNAL_SERVER_ERROR);
		if (!file.canRead()) {
			return Util.httpResourceResponse(HttpStatus.NOT_FOUND);
		}
		Resource resource = new ByteArrayResource(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
		return Util.httpResourceResponse(resource, MediaType.parseMediaType(symbolicFile.mimeType));
	}

}
