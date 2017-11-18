package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.util.ArrayList;

interface RoomMemberRepository extends CrudRepository<RoomMember, RoomMemberKey> {
	// for members
	Iterable<RoomMember> findByRoomId(String roomId);
	// for rooms
	Iterable<RoomMember> findByPersonId(String personId);
	// full key
	RoomMember findTopByRoomIdAndPersonId(String roomId, String personId);
	// for private rooms
	RoomMember findTopByRoomIdAndPersonIdNot(String roomId, String personId);
	// for specific membership (validation)
	long countByRoomIdAndPersonId(String roomId, String personId);
}

@Entity
@IdClass(RoomMemberKey.class)
public class RoomMember {

	@Id
	@Column(length = 32)
	public String roomId;
	@Id
	@Column(length = 32)
	public String personId;

	public RoomMember() {
	}

	public RoomMember(String roomId, String personId) {
		this.roomId = roomId;
		this.personId = personId;
	}

	public RoomMember(JSONObject item) throws JSONException {
		roomId = JsonUtil.getString(item, "roomId");
		personId = JsonUtil.getString(item, "personId");
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("roomId", roomId);
		item.put("personId", personId);
		return item;
	}

	public static Iterable<RoomMember> fromJsonArray(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<RoomMember> roomMemberList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			roomMemberList.add(new RoomMember(array.getJSONObject(i)));
		return roomMemberList;
	}

	public static JSONArray toJsonArray(Iterable<RoomMember> roomMembers) throws JSONException {
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
		String output = getClass().getSimpleName();
		try {
			output += toJson().toString();
		} catch (JSONException e) { }
		return output;
	}

}
