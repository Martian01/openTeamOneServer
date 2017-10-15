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

interface ViewedConfirmationRepository extends CrudRepository<ViewedConfirmation, ViewedConfirmationKey> {
	// for message confirmations
	Iterable<ViewedConfirmation> findByMessageId(String messageId);
	// for viewedCount (simple version)
	long countByMessageId(String messageId);
	// for badgeCount etc.
	ViewedConfirmation findTopByPersonIdAndRoomIdOrderByMessagePostedAtDesc(String personId, String messageRoomId);
	// for viewedAt
	ViewedConfirmation findTopByMessageIdAndPersonId(String messageId, String personId);
	// for debugging
	Iterable<ViewedConfirmation> findByPersonId(String personId);
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
	public long messagePostedAt;
	@Column
	public long confirmedAt;

	public ViewedConfirmation() {
	}

	public ViewedConfirmation(String messageId, String personId, String roomId, long messagePostedAt, long confirmedAt) {
		this.messageId = messageId;
		this.personId = personId;
		this.roomId = roomId;
		this.messagePostedAt = messagePostedAt;
		this.confirmedAt = confirmedAt;
	}

	public ViewedConfirmation(JSONObject item) throws JSONException {
		messageId = JsonUtil.getString(item, "messageId");
		personId = JsonUtil.getString(item, "personId");
		roomId = JsonUtil.getString(item, "roomId");
		messagePostedAt = JsonUtil.getIsoDate(item, "messagePostedAt");
		confirmedAt = JsonUtil.getIsoDate(item, "confirmedAt");
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("messageId", messageId);
		item.put("personId", personId);
		item.put("roomId", roomId);
		item.put("messagePostedAt", JsonUtil.toIsoDate(messagePostedAt));
		item.put("confirmedAt", JsonUtil.toIsoDate(confirmedAt));
		return item;
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

	public long getMessagePostedAt() {
		return messagePostedAt;
	}

	public void setMessagePostedAt(long messagePostedAt) {
		this.messagePostedAt = messagePostedAt;
	}

	public long getConfirmedAt() {
		return confirmedAt;
	}

	public void setConfirmedAt(long confirmedAt) {
		this.confirmedAt = confirmedAt;
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
