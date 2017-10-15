package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContentService {

	private TenantParameterRepository tpr;
	private UserRepository ur;
	private PersonRepository pr;
	private RoomRepository rr;
	private RoomMemberRepository rmr;
	private MessageRepository mr;
	private AttachmentRepository ar;
	private ViewedConfirmationRepository vcr;

	public ContentService(TenantParameterRepository tpr, UserRepository ur, PersonRepository pr, RoomRepository rr, RoomMemberRepository rmr, MessageRepository mr, AttachmentRepository ar, ViewedConfirmationRepository vcr) {
		this.tpr = tpr;
		this.ur = ur;
		this.pr = pr;
		this.rr = rr;
		this.rmr = rmr;
		this.mr = mr;
		this.ar = ar;
		this.vcr = vcr;
	}

	public JSONObject exportJson() throws JSONException {
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

	public void importJson(JSONObject jsonObject) throws JSONException {
		JSONArray item;
		item = JsonUtil.getJSONArray(jsonObject, "tenantParameters");
		Iterable<TenantParameter> tenantParameters = TenantParameter.fromJsonArray(item);
		if (tenantParameters != null)
			tpr.save(tenantParameters);
		item = JsonUtil.getJSONArray(jsonObject, "users");
		Iterable<User> users = User.fromJsonArray(item);
		if (users != null)
			ur.save(users);
		item = JsonUtil.getJSONArray(jsonObject, "persons");
		Iterable<Person> persons = Person.fromJsonArray(item);
		if (persons != null)
			pr.save(persons);
		item = JsonUtil.getJSONArray(jsonObject, "rooms");
		Iterable<Room> rooms = Room.fromJsonArray(item);
		if (rooms != null)
			rr.save(rooms);
		item = JsonUtil.getJSONArray(jsonObject, "roomMembers");
		Iterable<RoomMember> roomMembers = RoomMember.fromJsonArray(item);
		if (roomMembers != null)
			rmr.save(roomMembers);
		item = JsonUtil.getJSONArray(jsonObject, "messages");
		Iterable<Message> messages = Message.fromJsonArray(item);
		if (messages != null)
			mr.save(messages);
		item = JsonUtil.getJSONArray(jsonObject, "attachments");
		Iterable<Attachment> attachments = Attachment.fromJsonArray(item);
		if (attachments != null)
			ar.save(attachments);
		item = JsonUtil.getJSONArray(jsonObject, "viewedConfirmations");
		Iterable<ViewedConfirmation> viewedConfirmations = ViewedConfirmation.fromJsonArray(item);
		if (viewedConfirmations != null)
			vcr.save(viewedConfirmations);
	}

	public void createModelData() throws JSONException {
		long now = System.currentTimeMillis();
		//
		tpr.save(new TenantParameter("name", "OpenTeamOne"));
		tpr.save(new TenantParameter("pictureId", "tenant"));
		tpr.save(new TenantParameter("pagestyle", "default"));
		tpr.save(new TenantParameter("dataDirectory", "/var/cache/openTeamOne"));
		//
		Person p0, p1, p2, p3;
		pr.save(p0 = new Person(null, "Byrd", "Robert", "Dickie", null));
		pr.save(p1 = new Person(null, "Tank", "Thomas", "Tom", "profile1"));
		pr.save(p2 = new Person(null, "Smith", "Peter", null, "profile2"));
		pr.save(p3 = new Person(null, "Potter", "Harry", null, "profile3"));
		//
		ur.save(new User("admin01", "pass", p0.personId, false, true));
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
