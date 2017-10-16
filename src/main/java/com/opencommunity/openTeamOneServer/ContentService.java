package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Iterator;

@Service
public class ContentService {

	private static ContentService instance = null;

	private static String dataDirectory = null;
	private static String pageStyle = null;

	/* public services */

	public static String getDataDirectory() {
		if (dataDirectory == null && instance != null) {
			TenantParameter tp = instance.tpr.findOne("dataDirectory");
			if (tp == null)
				System.out.println("Error: data directory not configured");
			else
				dataDirectory = tp.value;
		}
		return dataDirectory;
	}

	public static String getPageStyle() {
		if (pageStyle == null && instance != null) {
			TenantParameter tp = instance.tpr.findOne("pageStyle");
			if (tp == null)
				System.out.println("Error: page style not configured");
			else
				pageStyle = tp.value;
		}
		return pageStyle;
	}

	public static JSONObject exportToJson() throws JSONException {
		if (instance != null)
			return instance._exportToJson();
		return null;
	}

	public static void importFromJson(JSONObject jsonObject, boolean delete, String protectedUserId) throws JSONException {
		if (instance != null)
			instance._importFromJson(jsonObject, delete, protectedUserId);
	}

	public static void deleteAll(String protectedUserId) throws JSONException {
		if (instance != null)
			instance._deleteAll(protectedUserId);
	}

	public static void loadModelData() throws JSONException {
		if (instance != null)
			instance._loadModelData();
	}

	/* instance methods and properties */

	@Autowired
	private TenantParameterRepository tpr;
	@Autowired
	private UserRepository ur;
	@Autowired
	private PersonRepository pr;
	@Autowired
	private RoomRepository rr;
	@Autowired
	private RoomMemberRepository rmr;
	@Autowired
	private MessageRepository mr;
	@Autowired
	private AttachmentRepository ar;
	@Autowired
	private ViewedConfirmationRepository vcr;

	public ContentService() {
		instance = this;
	}

	@PostConstruct
	private void init() {
		if (tpr.findOne("name") == null)
			tpr.save(new TenantParameter("name", "OpenTeamOne"));
		if (tpr.findOne("pictureId") == null)
			tpr.save(new TenantParameter("pictureId", "logo"));
		if (tpr.findOne("pageStyle") == null)
			tpr.save(new TenantParameter("pageStyle", "default"));
		if (tpr.findOne("dataDirectory") == null)
			tpr.save(new TenantParameter("dataDirectory", "/tmp"));
		//
		if (ur.countByHasAdminRoleTrue() == 0)
			ur.save(new User("admin", "admin", null, false, true));
	}

	private JSONObject _exportToJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("tenantParameters", TenantParameter.toJsonArray(tpr.findAll()));
		jsonObject.put("users", User.toJsonArray(ur.findAll()));
		jsonObject.put("persons", Person.toJsonArray(pr.findAll()));
		jsonObject.put("rooms", Room.toJsonArray(rr.findAll()));
		jsonObject.put("roomMembers", RoomMember.toJsonArray(rmr.findAll()));
		jsonObject.put("messages", Message.toJsonArray(mr.findAll()));
		jsonObject.put("attachments", Attachment.toJsonArray(ar.findAll()));
		jsonObject.put("viewedConfirmations", ViewedConfirmation.toJsonArray(vcr.findAll()));
		return jsonObject;
	}

	private void _importFromJson(JSONObject jsonObject, boolean delete, String protectedUserId) throws JSONException {
		JSONArray item;
		item = JsonUtil.getJSONArray(jsonObject, "tenantParameters");
		if (item != null) {
			if (delete) {
				tpr.deleteAll();
				dataDirectory = null;
				pageStyle = null;
			}
			tpr.save(TenantParameter.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "users");
		if (item != null && protectedUserId != null) {
			if (delete)
				ur.delete(ur.findByUserIdNot(protectedUserId));
			Iterable<User> users = User.fromJsonArray(item);
			Iterator<User> iterator = users.iterator();
			while (iterator.hasNext())
				if (protectedUserId.equals(iterator.next().userId))
					iterator.remove();
			ur.save(users);
		}
		item = JsonUtil.getJSONArray(jsonObject, "persons");
		if (item != null) {
			if (delete)
				pr.deleteAll();
			pr.save(Person.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "rooms");
		if (item != null) {
			if (delete)
				rr.deleteAll();
			rr.save(Room.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "roomMembers");
		if (item != null) {
			if (delete)
				rmr.deleteAll();
			rmr.save(RoomMember.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "messages");
		if (item != null) {
			if (delete)
				mr.deleteAll();
			mr.save(Message.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "attachments");
		if (item != null) {
			if (delete)
				ar.deleteAll();
			ar.save(Attachment.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "viewedConfirmations");
		if (item != null) {
			if (delete)
				vcr.deleteAll();
			vcr.save(ViewedConfirmation.fromJsonArray(item));
		}
	}

	private void _deleteAll(String protectedUserId) {
		tpr.deleteAll();
		dataDirectory = null;
		pageStyle = null;
		if (protectedUserId != null)
			ur.delete(ur.findByUserIdNot(protectedUserId));
		pr.deleteAll();
		rr.deleteAll();
		rmr.deleteAll();
		mr.deleteAll();
		ar.deleteAll();
		vcr.deleteAll();
	}

	private void _loadModelData() {
		long now = System.currentTimeMillis();
		//
		tpr.save(new TenantParameter("name", "OpenTeamOne"));
		tpr.save(new TenantParameter("pictureId", "tenant"));
		tpr.save(new TenantParameter("pageStyle", "default"));
		tpr.save(new TenantParameter("dataDirectory", "/var/cache/openTeamOne"));
		//
		Person p1, p2, p3;
		pr.save(p1 = new Person(null, "Tank", "Thomas", "Tom", "profile1"));
		pr.save(p2 = new Person(null, "Smith", "Peter", null, "profile2"));
		pr.save(p3 = new Person(null, "Potter", "Harry", null, "profile3"));
		//
		ur.save(new User("player01", "pass", p1.personId, true, false));
		ur.save(new User("player02", "pass", p2.personId, true, false));
		ur.save(new User("player03", "pass", p3.personId, true, false));
		//
		Room r1, r2, r3;
		rr.save(r1 = new Room(null, "Team Room", "TR", "group", "room1", now - 100000L));
		rr.save(r2 = new Room(null, "General", "GN", "group", "room2", now - 200000L));
		rr.save(r3 = new Room(null, "PM", "PM", "private", null, now - 300000L));
		//
		rmr.save(new RoomMember(r1.roomId, p1.personId));
		rmr.save(new RoomMember(r1.roomId, p2.personId));
		rmr.save(new RoomMember(r1.roomId, p3.personId));
		rmr.save(new RoomMember(r2.roomId, p1.personId));
		rmr.save(new RoomMember(r2.roomId, p2.personId));
		rmr.save(new RoomMember(r2.roomId, p3.personId));
		rmr.save(new RoomMember(r3.roomId, p1.personId));
		rmr.save(new RoomMember(r3.roomId, p2.personId));
		//
		Message m1, m2, m3, m4, m5, m6;
		mr.save(m1 = new Message(null, "cm1", r1.roomId, p1.personId, now - 500000, "Hello, this is my first message", false, now - 500000));
		mr.save(m2 = new Message(null, "cm2", r1.roomId, p3.personId, now - 100000, "Welcome! \uD83D\uDE0A", false, now - 100000));
		mr.save(m3 = new Message(null, "cm3", r2.roomId, p2.personId, now - 300000, "Can you see my picture?", false, now - 300000));
		mr.save(m4 = new Message(null, "cm4", r2.roomId, p1.personId, now - 200000, "Yeah, brilliant shot! \uD83D\uDC4D", false, now - 200000));
		mr.save(m5 = new Message(null, "cm5", r3.roomId, p1.personId, now - 600000, "Hi Peter", false, now - 600000));
		mr.save(m6 = new Message(null, "cm6", r3.roomId, p2.personId, now - 300000, "Hey Tom, everything allright?", false, now - 300000));
		//
		ar.save(new Attachment("tenant", "image/jpg", null, null));
		ar.save(new Attachment("profile1", "image/jpg", null, null));
		ar.save(new Attachment("profile2", "image/jpg", null, null));
		ar.save(new Attachment("profile3", "image/jpg", null, null));
		ar.save(new Attachment("room1", "image/jpg", null, null));
		ar.save(new Attachment("room2", "image/jpg", null, null));
		ar.save(new Attachment("sailing1", "image/jpg", null, m3.messageId));
		//
		vcr.save(new ViewedConfirmation(m1.messageId, p1.personId, m1.roomId, m1.postedAt, now));
		vcr.save(new ViewedConfirmation(m1.messageId, p2.personId, m1.roomId, m1.postedAt, now));
		vcr.save(new ViewedConfirmation(m1.messageId, p3.personId, m1.roomId, m1.postedAt, now));
		vcr.save(new ViewedConfirmation(m2.messageId, p1.personId, m2.roomId, m2.postedAt, now));
		vcr.save(new ViewedConfirmation(m2.messageId, p3.personId, m2.roomId, m2.postedAt, now));
		vcr.save(new ViewedConfirmation(m3.messageId, p1.personId, m3.roomId, m3.postedAt, now));
		vcr.save(new ViewedConfirmation(m3.messageId, p2.personId, m3.roomId, m3.postedAt, now));
		vcr.save(new ViewedConfirmation(m3.messageId, p3.personId, m3.roomId, m3.postedAt, now));
		vcr.save(new ViewedConfirmation(m4.messageId, p1.personId, m4.roomId, m4.postedAt, now));
		vcr.save(new ViewedConfirmation(m4.messageId, p2.personId, m4.roomId, m4.postedAt, now));
		vcr.save(new ViewedConfirmation(m5.messageId, p1.personId, m5.roomId, m5.postedAt, now));
		vcr.save(new ViewedConfirmation(m5.messageId, p2.personId, m5.roomId, m5.postedAt, now));
		vcr.save(new ViewedConfirmation(m6.messageId, p2.personId, m6.roomId, m6.postedAt, now));
	}

}
