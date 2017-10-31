package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

interface AttachmentRepository extends CrudRepository<Attachment, String> {
	Iterable<Attachment> findByMessageId(String messageId);
}

@Entity
public class Attachment {

	@Id
	public String attachmentId;
	@Column
	public String mimeType;
	@Column
	public String text;
	@Column
	public String messageId;

	public Attachment() {
	}

	public Attachment(String attachmentId, String mimeType, String text, String messageId) {
		this.attachmentId = attachmentId == null ? Util.getUuid() : attachmentId;
		this.mimeType = mimeType;
		this.text = text;
		this.messageId = messageId;
	}

	public Attachment(JSONObject item) throws JSONException {
		attachmentId = JsonUtil.getString(item, "attachmentId");
		mimeType = JsonUtil.getString(item, "mimeType");
		text = JsonUtil.getString(item, "text");
		messageId = JsonUtil.getString(item, "messageId");
		//
		if (attachmentId == null || attachmentId.length() == 0)
			attachmentId = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("attachmentId", attachmentId);
		item.put("mimeType", mimeType);
		item.put("text", text);
		item.put("messageId", messageId);
		return item;
	}

	public static Iterable<Attachment> fromJsonArray(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<Attachment> attachmentList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			attachmentList.add(new Attachment(array.getJSONObject(i)));
		return attachmentList;
	}

	public static JSONArray toJsonArray(Iterable<Attachment> attachments) throws JSONException {
		JSONArray array = new JSONArray();
		for (Attachment attachment : attachments)
			array.put(attachment.toJson());
		return array;
	}

	public String getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
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
