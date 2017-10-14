package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

interface MessageRepository extends CrudRepository<Message, String> {
	// for latest message
	Message findTopByRoomIdOrderByPostedAtDesc(String roomId);
	// for badge count
	long countByRoomIdAndPostedAtGreaterThan(String roomId, long postedAt);
	// for messagesSince
	Iterable<Message> findByRoomIdAndUpdatedAtGreaterThanAndPostedAtGreaterThanEqual(String roomId, long updatedAt, long postedAt);
	Iterable<Message> findByRoomIdAndUpdatedAtGreaterThan(String roomId, long updatedAt);
	Iterable<Message> findByRoomIdAndPostedAtGreaterThanEqual(String roomId, long postedAt);
	// for messagesUntil
	Page<Message> findByRoomIdAndPostedAtLessThanOrderByPostedAtDesc(String roomId, long postedAt, Pageable pageable);
	Page<Message> findByRoomIdOrderByPostedAtDesc(String roomId, Pageable pageable);
	Iterable<Message> findByRoomIdAndPostedAtBetween(String roomId, long postedAtLow, long postedAtHigh);
	Iterable<Message> findByRoomIdAndPostedAtLessThan(String roomId, long postedAt);
	// for viewed confirmations
	Iterable<Message> findByRoomIdAndSenderPersonIdAndPostedAtGreaterThan(String roomId, String senderPersonId, long postedAt);
	Iterable<Message> findByRoomIdAndSenderPersonIdAndPostedAtGreaterThanAndPostedAtLessThanEqual(String roomId, String senderPersonId, long postedAtLow, long postedAtHigh);
	Iterable<Message> findTop1ByRoomIdAndSenderPersonIdOrderByPostedAtDesc(String roomId, String senderPersonId);
	Iterable<Message> findTop1ByRoomIdAndSenderPersonIdAndPostedAtLessThanEqualOrderByPostedAtDesc(String roomId, String senderPersonId, long postedAt);
	// for all
	Iterable<Message> findByRoomId(String roomId);
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
	public long postedAt;
	@Column
	public String text;
	@Column
	public boolean isDeleted;
	@Column
	public long updatedAt;

	public Message() {
	}

	public Message(String messageId, String clientMessageId, String roomId, String senderPersonId, long postedAt, String text, boolean isDeleted, long updatedAt) {
		this.messageId =  messageId == null ? Util.getUuid() : messageId;
		this.clientMessageId = clientMessageId;
		this.roomId = roomId;
		this.senderPersonId = senderPersonId;
		this.postedAt = postedAt;
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
		postedAt = JsonUtil.getIsoDate(messageContent, "postedAt");
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
		JsonUtil.put(messageContent, "postedAt", JsonUtil.toIsoDate(postedAt));
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
		return "Message{" +
				"messageId='" + messageId + '\'' +
				", clientMessageId='" + clientMessageId + '\'' +
				", roomId='" + roomId + '\'' +
				", senderPersonId='" + senderPersonId + '\'' +
				", postedAt=" + postedAt +
				", text='" + text + '\'' +
				", isDeleted=" + isDeleted +
				", updatedAt=" + updatedAt +
				'}';
	}
}
