package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;


interface PersonRepository extends CrudRepository<Person, String> {
}

@Entity
public class Person {
	@Id
	public String personId;
	@Column
	public String lastName;
	@Column
	public String firstName;
	@Column
	public String nickName;
	@Column
	public String pictureId;

	public Person() {
	}

	public Person(String personId, String lastName, String firstName, String nickName, String pictureId) {
		this.personId = personId == null || personId.length() == 0 ? Util.getUuid() : personId;
		this.lastName = lastName;
		this.firstName = firstName;
		this.nickName = nickName;
		this.pictureId = pictureId;
	}

	public Person(JSONObject item) throws JSONException {
		personId = JsonUtil.getString(item, "personId");
		lastName = JsonUtil.getString(item, "lastName");
		firstName = JsonUtil.getString(item, "firstName");
		nickName = JsonUtil.getString(item, "nickName");
		pictureId = JsonUtil.getString(item, "pictureId");
		//
		if (personId == null || personId.length() == 0)
			personId = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("personId", personId);
		item.put("lastName", lastName);
		item.put("firstName", firstName);
		item.put("nickName", nickName);
		item.put("pictureId", pictureId);
		return item;
	}

	public static Iterable<Person> fromJsonArray(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<Person> personList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			personList.add(new Person(array.getJSONObject(i)));
		return personList;
	}

	public static JSONArray toJsonArray(Iterable<Person> persons) throws JSONException {
		JSONArray array = new JSONArray();
		for (Person person : persons)
			array.put(person.toJson());
		return array;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getPictureId() {
		return pictureId;
	}

	public void setPictureId(String pictureId) {
		this.pictureId = pictureId;
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
