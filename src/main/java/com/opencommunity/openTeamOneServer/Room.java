package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

interface RoomRepository extends CrudRepository<Room, String> {
	Iterable<Room> findByRoomType(String roomType);
}

@Entity
public class Room {

	@Id
	public String roomId;
	@Column
	public String name;
	@Column
	public String shortName;
	@Column
	public String roomType;
	@Column
	public String pictureId;
	@Column
	public long changedAt;

	public Room() {
	}

	public Room(String roomId, String name, String shortName, String roomType, String pictureId, long changedAt) {
		this.roomId = roomId == null || roomId.length() == 0 ? Util.getUuid() : roomId;
		this.name = name;
		this.shortName = shortName;
		this.roomType = roomType;
		this.pictureId = pictureId;
		this.changedAt = changedAt;
	}

	public Room(JSONObject item) throws JSONException {
		roomId = JsonUtil.getString(item, "roomId");
		name = JsonUtil.getString(item, "name");
		shortName = JsonUtil.getString(item, "shortName");
		roomType = JsonUtil.getString(item, "roomType");
		pictureId = JsonUtil.getString(item, "pictureId");
		changedAt = JsonUtil.getIsoDate(item, "changedAt");
		//
		if (roomId == null || roomId.length() == 0)
			roomId = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("roomId", roomId);
		item.put("name", name);
		item.put("shortName", shortName);
		item.put("roomType", roomType);
		item.put("pictureId", pictureId);
		item.put("changedAt", JsonUtil.toIsoDate(changedAt));
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

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
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

	public String getPictureId() {
		return pictureId;
	}

	public void setPictureId(String pictureId) {
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
