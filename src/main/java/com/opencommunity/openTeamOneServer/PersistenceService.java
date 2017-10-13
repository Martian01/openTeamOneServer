package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PersistenceService {

	private TenantParameterRepository tpr;
	private UserRepository ur;
	private PersonRepository pr;
	private RoomRepository rr;
	private RoomMemberRepository rmr;
	private MessageRepository mr;
	private AttachmentRepository ar;
	private ViewedConfirmationRepository vcr;

	public PersistenceService(TenantParameterRepository tpr, UserRepository ur, PersonRepository pr, RoomRepository rr, RoomMemberRepository rmr, MessageRepository mr, AttachmentRepository ar, ViewedConfirmationRepository vcr) {
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
		Iterable<User> viewedConfirmations = User.fromJsonArray(item);
		if (viewedConfirmations != null)
			ur.save(viewedConfirmations);
	}

	public void importModelData() throws JSONException {
		long now = System.currentTimeMillis();
		//
		tpr.save(new TenantParameter("name", "OpenTeamOne"));
		tpr.save(new TenantParameter("pictureId", "Pic00"));
		//
		Person p0, p1, p2, p3;
		pr.save(p0 = new Person(null, "Byrd", "Robert", "Dickie", null));
		pr.save(p1 = new Person(null, "Tank", "Thomas", "Tom", "Pic01"));
		pr.save(p2 = new Person(null, "Smith", "Peter", "Pete", "Pic02"));
		pr.save(p3 = new Person(null, "Potter", "Harry", "Arry", "Pic03"));
		//
		ur.save(new User("admin01", "pass", p0.personId, false, true));
		ur.save(new User("player01", "pass", p1.personId, true, false));
		ur.save(new User("player02", "pass", p2.personId, true, false));
		ur.save(new User("player03", "pass", p3.personId, true, false));
		//
		Room r1, r2, r3;
		rr.save(r1 = new Room(null, "Team Room", "TR", "group", "Pic04", now - 100000L));
		rr.save(r2 = new Room(null, "General", "GN", "group", "Pic05", now - 200000L));
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
		mr.save(m4 = new Message(null, "cm4", r2.roomId, p1.personId, now - 200000, "Sorry, I cannot. \uD83D\uDE1E", false, now - 200000));
		mr.save(m5 = new Message(null, "cm5", r3.roomId, p1.personId, now - 600000, "Hi Peter", false, now - 600000));
		mr.save(m6 = new Message(null, "cm6", r3.roomId, p2.personId, now - 300000, "Hey Thomas, everything allright?", false, now - 300000));
		//
		ar.save(new Attachment(null, m3.messageId, "Attachment 1", "image/png", "FileId1"));
		//
		vcr.save(new ViewedConfirmation(m1.messageId, p1.personId, m1.roomId, m1.createdAt));
		vcr.save(new ViewedConfirmation(m1.messageId, p2.personId, m1.roomId, m1.createdAt));
		vcr.save(new ViewedConfirmation(m1.messageId, p3.personId, m1.roomId, m1.createdAt));
		vcr.save(new ViewedConfirmation(m2.messageId, p1.personId, m2.roomId, m2.createdAt));
		vcr.save(new ViewedConfirmation(m2.messageId, p3.personId, m2.roomId, m2.createdAt));
		vcr.save(new ViewedConfirmation(m3.messageId, p1.personId, m3.roomId, m3.createdAt));
		vcr.save(new ViewedConfirmation(m3.messageId, p2.personId, m3.roomId, m3.createdAt));
		vcr.save(new ViewedConfirmation(m3.messageId, p3.personId, m3.roomId, m3.createdAt));
		vcr.save(new ViewedConfirmation(m4.messageId, p1.personId, m4.roomId, m4.createdAt));
		vcr.save(new ViewedConfirmation(m4.messageId, p2.personId, m4.roomId, m4.createdAt));
		vcr.save(new ViewedConfirmation(m5.messageId, p1.personId, m5.roomId, m5.createdAt));
		vcr.save(new ViewedConfirmation(m5.messageId, p2.personId, m5.roomId, m5.createdAt));
		vcr.save(new ViewedConfirmation(m6.messageId, p2.personId, m6.roomId, m6.createdAt));
	}

}
