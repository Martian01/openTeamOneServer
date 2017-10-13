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
}

@Entity
public class Attachment {

	@Id
	public String attachmentId;
	@Column
	public String messageId;
	@Column
	public String text;
	@Column
	public String mimeType;
	@Column
	public String fileId;

	//public String roomId;
	//public String feedId;
	//public long createdAt;

	// Video parameters
	//public int displayTimeCorrection = 0;
	//public int offsetStart = 0;
	//public int offsetEnd = 0;

	// Questionnaire parameters
	//public Questionnaire questionnaire;

	// InfoPackage parameters
	//public InfoPackage infoPackage;

	public Attachment() {
	}

	public Attachment(String attachmentId, String messageId, String text, String mimeType, String fileId) {
		this.attachmentId = attachmentId == null ? Util.getUuid() : attachmentId;
		this.messageId = messageId;
		this.text = text;
		this.mimeType = mimeType;
		this.fileId = fileId;
	}

	public Attachment(JSONObject item) throws JSONException {
		attachmentId = JsonUtil.getString(item, "assetId");
		JSONObject attachmentContent = JsonUtil.getJSONObject(item, "assetContent");
		text = JsonUtil.getString(attachmentContent, "text");
		mimeType = JsonUtil.getString(attachmentContent, "mimeType");
		JSONObject sapSportsFile = JsonUtil.getJSONObject(attachmentContent, "sapSportsFile");
		fileId = JsonUtil.getString(sapSportsFile, "fileId");
		mimeType = JsonUtil.getString(sapSportsFile, "mimeType");
		//
		if (attachmentId == null)
			attachmentId = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject sapSportsFile = new JSONObject();
		sapSportsFile.put("fileId", fileId);
		sapSportsFile.put("mimeType", mimeType);
		JSONObject attachmentContent = new JSONObject();
		JsonUtil.put(attachmentContent, "text", text);
		JsonUtil.put(attachmentContent, "mimeType", "application/vnd.sap.sports.file");
		JsonUtil.put(attachmentContent, "sapSportsFile", sapSportsFile);
		JSONObject attachment = new JSONObject();
		attachment.put("assetId", attachmentId);
		attachment.put("assetContent", attachmentContent);
		return attachment;
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

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	@Override
	public String toString() {
		return "Attachment{" +
				"attachmentId='" + attachmentId + '\'' +
				", messageId='" + messageId + '\'' +
				", text='" + text + '\'' +
				", mimeType='" + mimeType + '\'' +
				", fileId='" + fileId + '\'' +
				'}';
	}

}
