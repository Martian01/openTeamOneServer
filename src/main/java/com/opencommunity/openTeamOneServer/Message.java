package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

interface MessageRepository extends CrudRepository<Message, String> {
	//Iterable<Message> findByRoomIdOrderByCreatedAtDesc(String RoomId);
	Message findTopByRoomIdOrderByCreatedAtDesc(String RoomId);
	long countByRoomIdAndCreatedAtGreaterThan(String RoomId, long createdAt);
}

@Entity
public class Message {

	@Id
	public String messageId;
	@Column
	public String clientMessageId;
	@Column
	public String roomId;
	@Column
	public String senderPersonId;
	@Column
	public long createdAt;
	@Column
	public String text;
	@Column
	public boolean isDeleted;
	@Column
	public long updatedAt;

	//public Attachment[] attachments;
	//public boolean isOwnMessage;
	//public int viewedCount;
	//public long viewedAt;

	//public boolean readConfirmationRequested;
	//public int readConfirmedCount;
	//public long readConfirmedAt;

	public Message() {
	}

	public Message(String messageId, String clientMessageId, String roomId, String senderPersonId, long createdAt, String text, boolean isDeleted, long updatedAt) {
		this.messageId =  messageId == null ? Util.getUuid() : messageId;
		this.clientMessageId = clientMessageId;
		this.roomId = roomId;
		this.senderPersonId = senderPersonId;
		this.createdAt = createdAt;
		this.text = text;
		this.isDeleted = isDeleted;
		this.updatedAt = updatedAt;
	}

	public Message(JSONObject item) throws JSONException {
		messageId = JsonUtil.getString(item, "messageId");
		clientMessageId = JsonUtil.getString(item, "clientMessageId");
		JSONObject messageContent = JsonUtil.getJSONObject(item, "messageData");
		roomId = JsonUtil.getString(messageContent, "roomId");
		senderPersonId = JsonUtil.getString(messageContent, "senderPersonId");
		createdAt = JsonUtil.getIsoDate(messageContent, "postedAt");
		text = JsonUtil.getString(messageContent, "text");
		JSONObject messageStatus = JsonUtil.getJSONObject(item, "messageStatus");
		isDeleted = JsonUtil.getBoolean(messageStatus, "isDeleted");
		updatedAt = JsonUtil.getIsoDate(messageStatus, "updatedAt");
		//
		if (messageId == null)
			messageId = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject messageContent = new JSONObject();
		JsonUtil.put(messageContent, "roomId", roomId);
		JsonUtil.put(messageContent, "senderPersonId", senderPersonId);
		JsonUtil.put(messageContent, "postedAt", JsonUtil.toIsoDate(createdAt));
		JsonUtil.put(messageContent, "text", text);
		JSONObject messageStatus = new JSONObject();
		messageStatus.put("isDeleted", isDeleted);
		messageStatus.put("updatedAt", JsonUtil.toIsoDate(updatedAt));
		messageStatus.put("postedMessageStatus", new JSONObject());
		messageStatus.put("receivedMessageStatus", new JSONObject());
		JSONObject message = new JSONObject();
		message.put("messageId", messageId);
		message.put("clientMessageId", clientMessageId);
		message.put("messageContent", messageContent);
		message.put("messageStatus", messageStatus);
		return message;
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

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getClientMessageId() {
		return clientMessageId;
	}

	public void setClientMessageId(String clientMessageId) {
		this.clientMessageId = clientMessageId;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getSenderPersonId() {
		return senderPersonId;
	}

	public void setSenderPersonId(String senderPersonId) {
		this.senderPersonId = senderPersonId;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
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
		return "Message{" +
				"messageId='" + messageId + '\'' +
				", clientMessageId='" + clientMessageId + '\'' +
				", roomId='" + roomId + '\'' +
				", senderPersonId='" + senderPersonId + '\'' +
				", createdAt=" + createdAt +
				", text='" + text + '\'' +
				", isDeleted=" + isDeleted +
				", updatedAt=" + updatedAt +
				'}';
	}
}
