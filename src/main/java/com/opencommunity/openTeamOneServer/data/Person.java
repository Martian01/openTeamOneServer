package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.*;
import org.json.*;

import javax.persistence.*;
import java.util.*;


@Entity
public class Person {
	@Id
	@Column
	public Integer personId;
	@Column(length = 50)
	public String lastName;
	@Column(length = 50)
	public String firstName;
	@Column(length = 50)
	public String nickName;
	@Column
	public Integer pictureId;

	public Person() { }

	public Person(Integer personId, String lastName, String firstName, String nickName, Integer pictureId) {
		this.personId = personId == null ? RestLib.getRandomInt() : personId;
		this.lastName = lastName;
		this.firstName = firstName;
		this.nickName = nickName;
		this.pictureId = pictureId;
	}

	public Person(JSONObject item) throws JSONException {
		personId = JsonUtil.getInteger(item, "personId");
		if (personId == null)
			personId = RestLib.getRandomInt();
		lastName = JsonUtil.getString(item, "lastName");
		firstName = JsonUtil.getString(item, "firstName");
		nickName = JsonUtil.getString(item, "nickName");
		pictureId = JsonUtil.getInteger(item, "pictureId");
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

	public Integer getPersonId() {
		return personId;
	}

	public void setPersonId(Integer personId) {
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

	public Integer getPictureId() {
		return pictureId;
	}

	public void setPictureId(Integer pictureId) {
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
