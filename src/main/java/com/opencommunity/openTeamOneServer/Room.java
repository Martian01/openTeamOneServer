package com.opencommunity.openTeamOneServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

interface RoomRepository extends CrudRepository<Room, String> {
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
			roomId = Util.getString(item, "roomId");
			JSONObject roomStatus = Util.getJSONObject(item, "roomStatus");
			changedAt = Util.getIsoDate(roomStatus, "dataChangedAt");
			JSONObject roomData = Util.getJSONObject(item, "roomData");
			shortName = Util.getString(roomData, "shortName");
			name = Util.getString(roomData, "name", "(null)");
			pictureId = Util.getString(roomData, "pictureId");
			roomType = Util.getString(roomData, "roomType");
		} catch (JSONException e) { }
		if (roomId == null)
			roomId = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject roomStatus = new JSONObject();
		roomStatus.put("dataChangedAt", Util.toIsoDate(changedAt));
		JSONObject roomData = new JSONObject();
		Util.put(roomData, "shortName", shortName);
		Util.put(roomData, "name", name);
		Util.put(roomData, "roomType", roomType);
		Util.put(roomData, "pictureId", pictureId);
		JSONObject room = new JSONObject();
		room.put("roomId", roomId);
		room.put("roomStatus", roomStatus);
		room.put("roomData", roomData);
		room.put("roomContent", new JSONObject());
		return room;
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
