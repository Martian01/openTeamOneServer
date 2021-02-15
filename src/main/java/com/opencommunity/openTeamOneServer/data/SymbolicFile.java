package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.*;
import org.json.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class SymbolicFile {

	public static final String DIRECTORY_ATTACHMENTS = "attachments";
	public static final String DIRECTORY_PROFILES = "profiles";
	public static final String DIRECTORY_SNAPSHOTS = "snapshots";

	@Id
	@Column
	public Integer fileId;
	@Column(length = 32)
	public String mimeType;
	@Column(length = 128)
	public String text;
	@Column
	public Integer referenceId;
	@Column
	public Integer position;
	@Column(length = 16)
	public String directory;

	public SymbolicFile() { }

	public SymbolicFile(Integer fileId, String mimeType, String text, Integer referenceId, int position, String directory) {
		this.fileId = fileId == null ? RestLib.getRandomInt() : fileId;
		this.mimeType = mimeType;
		this.text = text;
		this.referenceId = referenceId;
		this.position = position;
		this.directory = directory;
	}

	public SymbolicFile(JSONObject item) throws JSONException {
		fileId = JsonUtil.getInteger(item, "fileId");
		if (fileId == null)
			fileId = RestLib.getRandomInt();
		mimeType = JsonUtil.getString(item, "mimeType");
		text = JsonUtil.getString(item, "text");
		referenceId = JsonUtil.getInteger(item, "referenceId");
		position = JsonUtil.getInteger(item, "position");
		directory = JsonUtil.getString(item, "directory");
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

	public Integer getFileId() {
		return fileId;
	}

	public void setFileId(Integer fileId) {
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

	public Integer getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(Integer referenceId) {
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
