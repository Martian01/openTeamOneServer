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
	Message findTopByRoomIdAndIsDeletedFalseOrderByPostedAtDesc(String roomId);
	// for badge count
	long countByRoomIdAndIsDeletedFalseAndPostedAtGreaterThan(String roomId, long postedAt);
	// for viewed confirmations
	Iterable<Message> findByRoomIdAndIsDeletedFalseAndPostedAtGreaterThan(String roomId, long postedAt);
	Iterable<Message> findByRoomIdAndIsDeletedFalseAndPostedAtGreaterThanAndPostedAtLessThanEqual(String roomId, long postedAtLow, long postedAtHigh);
	Iterable<Message> findTop1ByRoomIdAndIsDeletedFalseOrderByPostedAtDesc(String roomId);
	Iterable<Message> findTop1ByRoomIdAndIsDeletedFalseAndPostedAtLessThanEqualOrderByPostedAtDesc(String roomId, long postedAt);
	// for messages since
	Iterable<Message> findByRoomIdAndUpdatedAtGreaterThanAndPostedAtGreaterThanEqual(String roomId, long updatedAt, long postedAt);
	Iterable<Message> findByRoomIdAndUpdatedAtGreaterThan(String roomId, long updatedAt);
	Iterable<Message> findByRoomIdAndPostedAtGreaterThanEqual(String roomId, long postedAt);
	// for messages until
	Page<Message> findByRoomIdAndPostedAtLessThanOrderByPostedAtDesc(String roomId, long postedAt, Pageable pageable);
	Page<Message> findByRoomIdOrderByPostedAtDesc(String roomId, Pageable pageable);
	Iterable<Message> findByRoomIdAndPostedAtBetween(String roomId, long postedAtLow, long postedAtHigh);
	Iterable<Message> findByRoomIdAndPostedAtLessThan(String roomId, long postedAt);
	// for debugging
	Iterable<Message> findByRoomId(String roomId);
	Message findTopByClientMessageId(String clientMessageId);
}

@Entity
public class Message {

	@Id
	@Column(length = 32)
	public String messageId;
	@Column(length = 32)
	public String clientMessageId;
	@Column(length = 32)
	public String roomId;
	@Column(length = 32)
	public String senderPersonId;
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

	public Message(String messageId, String clientMessageId, String roomId, String senderPersonId, long postedAt, String text, boolean isDeleted, long updatedAt) {
		this.messageId =  messageId == null || messageId.length() == 0 ? Util.getUuid() : messageId;
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
		roomId = JsonUtil.getString(item, "roomId");
		senderPersonId = JsonUtil.getString(item, "senderPersonId");
		postedAt = JsonUtil.getIsoDate(item, "postedAt");
		text = JsonUtil.getString(item, "text");
		isDeleted = JsonUtil.getBoolean(item, "isDeleted");
		updatedAt = JsonUtil.getIsoDate(item, "updatedAt");
		//
		if (messageId == null || messageId.length() == 0)
			messageId = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("messageId", messageId);
		item.put("clientMessageId", clientMessageId);
		item.put("roomId", roomId);
		item.put("senderPersonId", senderPersonId);
		item.put("postedAt", JsonUtil.toIsoDate(postedAt));
		item.put("text", text);
		item.put("isDeleted", isDeleted);
		item.put("updatedAt", JsonUtil.toIsoDate(updatedAt));
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
		String output = getClass().getSimpleName();
		try {
			output += toJson().toString();
		} catch (JSONException e) { }
		return output;
	}
}
