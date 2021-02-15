package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.*;
import org.json.*;

import javax.persistence.*;
import java.util.*;

@Entity
@IdClass(RoomMemberKey.class)
public class RoomMember {

	@Id
	@Column
	public Integer roomId;
	@Id
	@Column
	public Integer personId;

	public RoomMember() { }

	public RoomMember(Integer roomId, Integer personId) {
		this.roomId = roomId;
		this.personId = personId;
	}

	public RoomMember(JSONObject item) throws JSONException {
		roomId = JsonUtil.getInteger(item, "roomId");
		personId = JsonUtil.getInteger(item, "personId");
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

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public Integer getPersonId() {
		return personId;
	}

	public void setPersonId(Integer personId) {
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
