package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.*;
import org.json.*;

import javax.persistence.*;
import java.util.*;

@Entity
@IdClass(ViewedConfirmationKey.class)
public class ViewedConfirmation {

	@Id
	@Column
	public Integer messageId;
	@Id
	@Column
	public Integer personId;
	@Column
	public Integer roomId;
	@Column
	public long messagePostedAt;
	@Column
	public long confirmedAt;

	public ViewedConfirmation() { }

	public ViewedConfirmation(Integer messageId, Integer personId, Integer roomId, long messagePostedAt, long confirmedAt) {
		this.messageId = messageId;
		this.personId = personId;
		this.roomId = roomId;
		this.messagePostedAt = messagePostedAt;
		this.confirmedAt = confirmedAt;
	}

	public ViewedConfirmation(JSONObject item) throws JSONException {
		messageId = JsonUtil.getInteger(item, "messageId");
		personId = JsonUtil.getInteger(item, "personId");
		roomId = JsonUtil.getInteger(item, "roomId");
		messagePostedAt = JsonUtil.getIsoDate(item, "messagePostedAt");
		confirmedAt = JsonUtil.getIsoDate(item, "confirmedAt");
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("messageId", messageId);
		item.put("personId", personId);
		item.put("roomId", roomId);
		item.put("messagePostedAt", TimeUtil.toIsoDateString(messagePostedAt));
		item.put("confirmedAt", TimeUtil.toIsoDateString(confirmedAt));
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

	public Integer getMessageId() {
		return messageId;
	}

	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}

	public Integer getPersonId() {
		return personId;
	}

	public void setPersonId(Integer personId) {
		this.personId = personId;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
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
