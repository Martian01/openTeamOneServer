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
	private String personId;
	@Column
	private String lastName;
	@Column
	private String firstName;
	@Column
	private String nickName;
	@Column
	private String pictureId;

	protected Person() {
	}

	public Person(String lastName, String firstName, String nickName, String pictureId) {
		personId = Util.getUuid();
		this.lastName = lastName;
		this.firstName = firstName;
		this.nickName = nickName;
		this.pictureId = pictureId;
	}

	public Person(JSONObject item) {
		try {
			personId = JsonUtil.getString(item, "personId");
			lastName = JsonUtil.getString(item, "lastName");
			firstName = JsonUtil.getString(item, "firstName");
			nickName = JsonUtil.getString(item, "nickName");
			pictureId = JsonUtil.getString(item, "pictureId");
		} catch (JSONException e) { }
		if (personId == null)
			personId = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject person = new JSONObject();
		person.put("personId", personId);
		JsonUtil.put(person, "lastName", lastName);
		JsonUtil.put(person, "firstName", firstName);
		JsonUtil.put(person, "nickName", nickName);
		JsonUtil.put(person, "pictureId", pictureId);
		return person;
	}

	public static Iterable<Person> fromJsonList(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<Person> personList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			personList.add(new Person(array.getJSONObject(i)));
		return personList;
	}

	public static JSONArray toJsonList(Iterable<Person> persons) throws JSONException {
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
		return "Person{" +
				"personId='" + personId + '\'' +
				", lastName='" + lastName + '\'' +
				", firstName='" + firstName + '\'' +
				", nickName='" + nickName + '\'' +
				", pictureId='" + pictureId + '\'' +
				'}';
	}
}
