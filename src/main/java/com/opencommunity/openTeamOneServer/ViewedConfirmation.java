package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.util.ArrayList;

interface ViewedConfirmationRepository extends CrudRepository<ViewedConfirmation, String> {
}

@Entity
@IdClass(ViewedConfirmationKey.class)
public class ViewedConfirmation {

	@Id
	public String messageId;
	@Id
	public String personId;

	public ViewedConfirmation() {
	}

	public ViewedConfirmation(String messageId, String personId) {
		this.messageId = messageId;
		this.personId = personId;
	}

	public ViewedConfirmation(JSONObject item) {
		try {
			messageId = JsonUtil.getString(item, "messageId");
			personId = JsonUtil.getString(item, "personId");
		} catch (JSONException e) { }
	}

	public JSONObject toJson() throws JSONException {
		JSONObject roomMember = new JSONObject();
		roomMember.put("messageId", messageId);
		roomMember.put("personId", personId);
		return roomMember;
	}

	public static Iterable<ViewedConfirmation> fromJsonList(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<ViewedConfirmation> viewedConfirmationList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			viewedConfirmationList.add(new ViewedConfirmation(array.getJSONObject(i)));
		return viewedConfirmationList;
	}

	public static JSONArray toJsonList(Iterable<ViewedConfirmation> viewedConfirmations) throws JSONException {
		JSONArray array = new JSONArray();
		for (ViewedConfirmation viewedConfirmation : viewedConfirmations)
			array.put(viewedConfirmation.toJson());
		return array;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	@Override
	public String toString() {
		return "ViewedConfirmation{" +
				"messageId='" + messageId + '\'' +
				", personId='" + personId + '\'' +
				'}';
	}

}
