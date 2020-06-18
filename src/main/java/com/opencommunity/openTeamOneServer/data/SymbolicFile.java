package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.JsonUtil;
import com.opencommunity.openTeamOneServer.util.RestLib;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

@Entity
public class SymbolicFile {

	public static final String DIRECTORY_ATTACHMENTS = "attachments";
	public static final String DIRECTORY_PROFILES = "profiles";
	public static final String DIRECTORY_SNAPSHOTS = "snapshots";

	@Id
	@Column(length = 32)
	public String fileId;
	@Column(length = 100)
	public String mimeType;
	@Column(length = 200)
	public String text;
	@Column(length = 32)
	public String referenceId;
	@Column
	public Integer position;
	@Column(length = 20)
	public String directory;

	public SymbolicFile() {
	}

	public SymbolicFile(String fileId, String mimeType, String text, String referenceId, int position, String directory) {
		this.fileId = fileId == null ? RestLib.getUuid() : fileId;
		this.mimeType = mimeType;
		this.text = text;
		this.referenceId = referenceId;
		this.position = position;
		this.directory = directory;
	}

	public SymbolicFile(JSONObject item) throws JSONException {
		fileId = JsonUtil.getString(item, "fileId");
		mimeType = JsonUtil.getString(item, "mimeType");
		text = JsonUtil.getString(item, "text");
		referenceId = JsonUtil.getString(item, "referenceId");
		position = JsonUtil.getInt(item, "position", 0);
		directory = JsonUtil.getString(item, "directory");
		//
		if (fileId == null || fileId.length() == 0)
			fileId = RestLib.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("fileId", fileId);
		item.put("mimeType", mimeType);
		item.put("text", text);
		item.put("referenceId", referenceId);
		item.put("position", position);
		item.put("directory", directory);
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

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
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
