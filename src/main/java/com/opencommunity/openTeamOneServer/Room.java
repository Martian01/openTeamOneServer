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
	private String roomId;
	@Column
	private String shortName;
	@Column
	private String name;
	@Column
	private String roomType;
	@Column
	private String pictureId;
	@Column
	private long changedAt;

	public Room() {
	}

	public Room(String name, String shortName, String roomType, String pictureId) {
		this.roomId = Util.getUuid();
		this.shortName = shortName;
		this.name = name;
		this.roomType = roomType;
		this.pictureId = pictureId;
		this.changedAt = System.currentTimeMillis();
	}

	public Room(JSONObject item) {
		try {
			roomId = JsonUtil.getString(item, "roomId");
			JSONObject roomStatus = JsonUtil.getJSONObject(item, "roomStatus");
			changedAt = JsonUtil.getIsoDate(roomStatus, "dataChangedAt");
			JSONObject roomData = JsonUtil.getJSONObject(item, "roomData");
			shortName = JsonUtil.getString(roomData, "shortName");
			name = JsonUtil.getString(roomData, "name", "(null)");
			pictureId = JsonUtil.getString(roomData, "pictureId");
			roomType = JsonUtil.getString(roomData, "roomType");
		} catch (JSONException e) { }
		if (roomId == null)
			roomId = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject roomStatus = new JSONObject();
		roomStatus.put("dataChangedAt", JsonUtil.toIsoDate(changedAt));
		JSONObject roomData = new JSONObject();
		JsonUtil.put(roomData, "shortName", shortName);
		JsonUtil.put(roomData, "name", name);
		JsonUtil.put(roomData, "roomType", roomType);
		JsonUtil.put(roomData, "pictureId", pictureId);
		JSONObject room = new JSONObject();
		room.put("roomId", roomId);
		room.put("roomStatus", roomStatus);
		room.put("roomData", roomData);
		room.put("roomContent", new JSONObject());
		return room;
	}

	public static Iterable<Room> fromJsonList(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<Room> roomList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			roomList.add(new Room(array.getJSONObject(i)));
		return roomList;
	}

	public static JSONArray toJsonList(Iterable<Room> rooms) throws JSONException {
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

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		return "Room{" +
				"roomId='" + roomId + '\'' +
				", shortName='" + shortName + '\'' +
				", name='" + name + '\'' +
				", roomType='" + roomType + '\'' +
				", pictureId='" + pictureId + '\'' +
				", changedAt=" + changedAt +
				'}';
	}
}
