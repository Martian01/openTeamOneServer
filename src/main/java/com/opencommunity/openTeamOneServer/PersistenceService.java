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

	public PersistenceService(TenantParameterRepository tpr, UserRepository ur, PersonRepository pr, RoomRepository rr, RoomMemberRepository rmr) {
		this.tpr = tpr;
		this.ur = ur;
		this.pr = pr;
		this.rr = rr;
		this.rmr = rmr;
	}

	public JSONObject exportJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("tenantParameters", TenantParameter.toJsonList(tpr.findAll()));
		jsonObject.put("users", User.toJsonList(ur.findAll()));
		jsonObject.put("persons", Person.toJsonList(pr.findAll()));
		jsonObject.put("rooms", Room.toJsonList(rr.findAll()));
		jsonObject.put("roomMembers", RoomMember.toJsonList(rmr.findAll()));
		return jsonObject;
	}

	public void importJson(JSONObject jsonObject) throws JSONException {
		JSONArray item;
		item = JsonUtil.getJSONArray(jsonObject, "tenantParameters");
		Iterable<TenantParameter> tenantParameters = TenantParameter.fromJsonList(item);
		if (tenantParameters != null)
			tpr.save(tenantParameters);
		item = JsonUtil.getJSONArray(jsonObject, "users");
		Iterable<User> users = User.fromJsonList(item);
		if (users != null)
			ur.save(users);
		item = JsonUtil.getJSONArray(jsonObject, "persons");
		Iterable<Person> persons = Person.fromJsonList(item);
		if (persons != null)
			pr.save(persons);
		item = JsonUtil.getJSONArray(jsonObject, "rooms");
		Iterable<Room> rooms = Room.fromJsonList(item);
		if (rooms != null)
			rr.save(rooms);
		item = JsonUtil.getJSONArray(jsonObject, "roomMembers");
		Iterable<RoomMember> roomMembers = RoomMember.fromJsonList(item);
		if (roomMembers != null)
			rmr.save(roomMembers);
	}

	public void importModelData() throws JSONException {
		tpr.save(new TenantParameter("name", "OpenTeamOne"));
		tpr.save(new TenantParameter("pictureId", "Pic00"));
		//
		Person a1, p1, p2, p3;
		pr.save(a1 = new Person("Alt", "Achim", "Ach", null));
		pr.save(p1 = new Person("Meier", "Thomas", "Tom", "Pic01"));
		pr.save(p2 = new Person("Schmidt", "Peter", "Pötte", "Pic02"));
		pr.save(p3 = new Person("Hansen", "Hans", "Haha", "Pic03"));
		//
		ur.save(new User("admin01", "pass", a1.getPersonId(), false, true));
		ur.save(new User("player01", "pass", p1.getPersonId(), true, false));
		ur.save(new User("player02", "pass", p2.getPersonId(), true, false));
		ur.save(new User("player03", "pass", p3.getPersonId(), true, false));
		//
		Room r1, r2, r3;
		rr.save(r1 = new Room("Team Room", "TR", "group", "Pic04"));
		rr.save(r2 = new Room("General", "GN", "group", "Pic05"));
		rr.save(r3 = new Room("PM", "PM", "private", null));
		//
		rmr.save(new RoomMember(r1.getRoomId(), p1.getPersonId()));
		rmr.save(new RoomMember(r1.getRoomId(), p2.getPersonId()));
		rmr.save(new RoomMember(r1.getRoomId(), p3.getPersonId()));
		rmr.save(new RoomMember(r2.getRoomId(), p1.getPersonId()));
		rmr.save(new RoomMember(r2.getRoomId(), p2.getPersonId()));
		rmr.save(new RoomMember(r2.getRoomId(), p3.getPersonId()));
		rmr.save(new RoomMember(r3.getRoomId(), p1.getPersonId()));
		rmr.save(new RoomMember(r3.getRoomId(), p2.getPersonId()));
	}

}
