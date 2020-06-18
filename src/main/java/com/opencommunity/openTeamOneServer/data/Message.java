package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.JsonUtil;
import com.opencommunity.openTeamOneServer.util.RestLib;
import com.opencommunity.openTeamOneServer.util.TimeUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

@Entity
public class Message {

	@Id
	@Column
	public Integer messageId;
	@Column(length = 32)
	public String clientMessageId;
	@Column
	public Integer roomId;
	@Column
	public Integer senderPersonId;
	@Column
	public long postedAt;
	@Column(length = 5000)
	public String text;
	@Column
	public boolean isDeleted;
	@Column
	public long updatedAt;

	public Message() {
	}

	public Message(Integer messageId, String clientMessageId, Integer roomId, Integer senderPersonId, long postedAt, String text, boolean isDeleted, long updatedAt) {
		this.messageId =  messageId == null ? RestLib.getRandomInt() : messageId;
		this.clientMessageId = clientMessageId;
		this.roomId = roomId;
		this.senderPersonId = senderPersonId;
		this.postedAt = postedAt;
		this.text = text;
		this.isDeleted = isDeleted;
		this.updatedAt = updatedAt;
	}

	public Message(JSONObject item) throws JSONException {
		messageId = JsonUtil.getIntegerString(item, "messageId");
		if (messageId == null)
			messageId = RestLib.getRandomInt();
		clientMessageId = JsonUtil.getString(item, "clientMessageId");
		roomId = JsonUtil.getIntegerString(item, "roomId");
		senderPersonId = JsonUtil.getIntegerString(item, "senderPersonId");
		postedAt = JsonUtil.getIsoDate(item, "postedAt");
		text = JsonUtil.getString(item, "text");
		isDeleted = JsonUtil.getBoolean(item, "isDeleted");
		updatedAt = JsonUtil.getIsoDate(item, "updatedAt");
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("messageId", messageId);
		item.put("clientMessageId", clientMessageId);
		item.put("roomId", roomId);
		item.put("senderPersonId", senderPersonId);
		item.put("postedAt", TimeUtil.toIsoDateString(postedAt));
		item.put("text", text);
		item.put("isDeleted", isDeleted);
		item.put("updatedAt", TimeUtil.toIsoDateString(updatedAt));
		return item;
	}

	public static Iterable<Message> fromJsonArray(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<Message> messageList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			messageList.add(new Message(array.getJSONObject(i)));
		return messageList;
	}

	public static JSONArray toJsonArray(Iterable<Message> messages) throws JSONException {
		JSONArray array = new JSONArray();
		for (Message message : messages)
			array.put(message.toJson());
		return array;
	}

	public Integer getMessageId() {
		return messageId;
	}

	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}

	public String getClientMessageId() {
		return clientMessageId;
	}

	public void setClientMessageId(String clientMessageId) {
		this.clientMessageId = clientMessageId;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public Integer getSenderPersonId() {
		return senderPersonId;
	}

	public void setSenderPersonId(Integer senderPersonId) {
		this.senderPersonId = senderPersonId;
	}

	public long getPostedAt() {
		return postedAt;
	}

	public void setPostedAt(long postedAt) {
		this.postedAt = postedAt;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean deleted) {
		isDeleted = deleted;
	}

	public long getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(long updatedAt) {
		this.updatedAt = updatedAt;
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
