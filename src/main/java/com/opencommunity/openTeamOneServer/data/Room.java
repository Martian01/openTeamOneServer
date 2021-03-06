package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.*;
import org.json.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Room {

	@Id
	@Column
	public Integer roomId;
	@Column(length = 50)
	public String name;
	@Column(length = 4)
	public String shortName;
	@Column(length = 10)
	public String roomType;
	@Column
	public Integer pictureId;
	@Column
	public long changedAt;

	public Room() { }

	public Room(Integer roomId, String name, String shortName, String roomType, Integer pictureId, long changedAt) {
		this.roomId = roomId == null ? RestLib.getRandomInt() : roomId;
		this.name = name;
		this.shortName = shortName;
		this.roomType = roomType;
		this.pictureId = pictureId;
		this.changedAt = changedAt;
	}

	public Room(JSONObject item) throws JSONException {
		roomId = JsonUtil.getInteger(item, "roomId");
		if (roomId == null)
			roomId = RestLib.getRandomInt();
		name = JsonUtil.getString(item, "name");
		shortName = JsonUtil.getString(item, "shortName");
		roomType = JsonUtil.getString(item, "roomType");
		pictureId = JsonUtil.getInteger(item, "pictureId");
		changedAt = JsonUtil.getIsoDate(item, "changedAt");
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("roomId", roomId);
		item.put("name", name);
		item.put("shortName", shortName);
		item.put("roomType", roomType);
		item.put("pictureId", pictureId);
		item.put("changedAt", TimeUtil.toIsoDateString(changedAt));
		return item;
	}

	public static Iterable<Room> fromJsonArray(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<Room> roomList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			roomList.add(new Room(array.getJSONObject(i)));
		return roomList;
	}

	public static JSONArray toJsonArray(Iterable<Room> rooms) throws JSONException {
		JSONArray array = new JSONArray();
		for (Room room : rooms)
			array.put(room.toJson());
		return array;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getRoomType() {
		return roomType;
	}

	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}

	public Integer getPictureId() {
		return pictureId;
	}

	public void setPictureId(Integer pictureId) {
		this.pictureId = pictureId;
	}

	public long getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(long changedAt) {
		this.changedAt = changedAt;
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
