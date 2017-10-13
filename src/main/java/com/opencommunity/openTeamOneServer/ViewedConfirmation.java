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

interface ViewedConfirmationRepository extends CrudRepository<ViewedConfirmation, String> {
	ViewedConfirmation findTopByRoomIdOrderByTimestampDesc(String RoomId);
}

@Entity
@IdClass(ViewedConfirmationKey.class)
public class ViewedConfirmation {

	@Id
	public String messageId;
	@Id
	public String personId;
	@Column
	public String roomId;
	@Column
	public long timestamp;

	public ViewedConfirmation() {
	}

	public ViewedConfirmation(String messageId, String personId, String roomId, long timestamp) {
		this.messageId = messageId;
		this.personId = personId;
		this.roomId = roomId;
		this.timestamp = timestamp;
	}

	public ViewedConfirmation(JSONObject item) throws JSONException {
		messageId = JsonUtil.getString(item, "messageId");
		personId = JsonUtil.getString(item, "personId");
		roomId = JsonUtil.getString(item, "roomId");
		timestamp = JsonUtil.getIsoDate(item, "timestamp");
	}

	public JSONObject toJson() throws JSONException {
		JSONObject roomMember = new JSONObject();
		roomMember.put("messageId", messageId);
		roomMember.put("personId", personId);
		roomMember.put("roomId", roomId);
		roomMember.put("timestamp", JsonUtil.toIsoDate(timestamp));
		return roomMember;
	}

	public static Iterable<ViewedConfirmation> fromJsonArray(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<ViewedConfirmation> viewedConfirmationList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			viewedConfirmationList.add(new ViewedConfirmation(array.getJSONObject(i)));
		return viewedConfirmationList;
	}

	public static JSONArray toJsonArray(Iterable<ViewedConfirmation> viewedConfirmations) throws JSONException {
		JSONArray array = new JSONArray();
		for (ViewedConfirmation viewedConfirmation : viewedConfirmations)
			array.put(viewedConfirmation.toJson());
		return array;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "ViewedConfirmation{" +
				"messageId='" + messageId + '\'' +
				", personId='" + personId + '\'' +
				", roomId='" + roomId + '\'' +
				", timestamp='" + timestamp + '\'' +
				'}';
	}

}
