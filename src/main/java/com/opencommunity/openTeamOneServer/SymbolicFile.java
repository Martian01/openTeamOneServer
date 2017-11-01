package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

interface SymbolicFileRepository extends CrudRepository<SymbolicFile, String> {
	Iterable<SymbolicFile> findByReferenceIdOrderByPositionAsc(String referenceId);
}

@Entity
public class SymbolicFile {

	@Id
	public String fileId;
	@Column
	public String mimeType;
	@Column
	public String text;
	@Column
	public String referenceId;
	@Column
	public int position;

	public SymbolicFile() {
	}

	public SymbolicFile(String fileId, String mimeType, String text, String referenceId, int position) {
		this.fileId = fileId == null ? Util.getUuid() : fileId;
		this.mimeType = mimeType;
		this.text = text;
		this.referenceId = referenceId;
		this.position = position;
	}

	public SymbolicFile(JSONObject item) throws JSONException {
		fileId = JsonUtil.getString(item, "fileId");
		mimeType = JsonUtil.getString(item, "mimeType");
		text = JsonUtil.getString(item, "text");
		referenceId = JsonUtil.getString(item, "referenceId");
		position = JsonUtil.getInt(item, "position", 0);
		//
		if (fileId == null || fileId.length() == 0)
			fileId = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("fileId", fileId);
		item.put("mimeType", mimeType);
		item.put("text", text);
		item.put("referenceId", referenceId);
		item.put("position", position);
		return item;
	}

	public static Iterable<SymbolicFile> fromJsonArray(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<SymbolicFile> symbolicFileList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			symbolicFileList.add(new SymbolicFile(array.getJSONObject(i)));
		return symbolicFileList;
	}

	public static JSONArray toJsonArray(Iterable<SymbolicFile> symbolicFiles) throws JSONException {
		JSONArray array = new JSONArray();
		for (SymbolicFile symbolicFile : symbolicFiles)
			array.put(symbolicFile.toJson());
		return array;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
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

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
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
