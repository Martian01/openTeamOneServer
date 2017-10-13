package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.util.ArrayList;

interface RoomMemberRepository extends CrudRepository<RoomMember, String> {
	Iterable<RoomMember> findByRoomId(String roomId);
	//Iterable<RoomMember> findByPersonId(String personId);
}

@Entity
@IdClass(RoomMemberKey.class)
public class RoomMember {

	@Id
	public String roomId;
	@Id
	public String personId;

	public RoomMember() {
	}

	public RoomMember(String roomId, String personId) {
		this.roomId = roomId;
		this.personId = personId;
	}

	public RoomMember(JSONObject item) {
		try {
			roomId = JsonUtil.getString(item, "roomId");
			personId = JsonUtil.getString(item, "personId");
		} catch (JSONException e) { }
	}

	public JSONObject toJson() throws JSONException {
		JSONObject roomMember = new JSONObject();
		roomMember.put("roomId", roomId);
		roomMember.put("personId", personId);
		return roomMember;
	}

	public static Iterable<RoomMember> fromJsonList(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<RoomMember> roomMemberList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			roomMemberList.add(new RoomMember(array.getJSONObject(i)));
		return roomMemberList;
	}

	public static JSONArray toJsonList(Iterable<RoomMember> roomMembers) throws JSONException {
		JSONArray array = new JSONArray();
		for (RoomMember roomMember : roomMembers)
			array.put(roomMember.toJson());
		return array;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	@Override
	public String toString() {
		return "RoomMember{" +
				"roomId='" + roomId + '\'' +
				", personId='" + personId + '\'' +
				'}';
	}
}
