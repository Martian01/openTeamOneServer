package com.opencommunity.openTeamOneServer;

import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;


interface PersonRepository extends CrudRepository<Person, String> {
	List<Person> findAll();
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
		personId = Util.getUuid();
	}

	public Person(String lastName, String firstName, String nickName, String pictureId) {
		personId = Util.getUuid();
		this.lastName = lastName;
		this.firstName = firstName;
		this.nickName = nickName;
		this.pictureId = pictureId;
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
