package com.opencommunity.openTeamOneServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


interface UserRepository extends CrudRepository<User, String> {
}

@Entity
public class User {
	@Id
	private String userId;
	@Column
	private String passwordHash;
	@Column
	private String personId;
	@Column
	private boolean hasUserRole;
	@Column
	private boolean hasAdminRole;

	private static String hash(String password) {
		return Integer.toHexString(password.hashCode());
	}

	protected User() {
	}

	public User(String userId, String password, String personId, boolean hasUserRole, boolean hasAdminRole) {
		this.userId = userId.toLowerCase();
		this.passwordHash = hash(password);
		this.personId = personId;
		this.hasUserRole = hasUserRole;
		this.hasAdminRole = hasAdminRole;
	}

	public User(JSONObject item) {
		try {
			userId = Util.getString(item, "userId");
			passwordHash = Util.getString(item, "passwordHash");
			personId = Util.getString(item, "personId");
			hasUserRole = Util.getBoolean(item, "hasUserRole");
			hasAdminRole = Util.getBoolean(item, "hasAdminRole");
		} catch (JSONException e) { }
	}

	public JSONObject toJson() throws JSONException {
		JSONObject user = new JSONObject();
		user.put("userId", userId);
		user.put("passwordHash", passwordHash);
		user.put("personId", personId);
		user.put("hasUserRole", hasUserRole);
		user.put("hasAdminRole", hasAdminRole);
		return user;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public boolean isHasUserRole() {
		return hasUserRole;
	}

	public void setHasUserRole(boolean hasUserRole) {
		this.hasUserRole = hasUserRole;
	}

	public boolean isHasAdminRole() {
		return hasAdminRole;
	}

	public void setHasAdminRole(boolean hasAdminRole) {
		this.hasAdminRole = hasAdminRole;
	}

	@Override
	public String toString() {
		return "User{" +
				"userId='" + userId + '\'' +
				", passwordHash='" + passwordHash + '\'' +
				", personId='" + personId + '\'' +
				", hasUserRole=" + hasUserRole +
				", hasAdminRole=" + hasAdminRole +
				'}';
	}

	public void setPassword(String password) {
		this.passwordHash = hash(password);
	}

	public boolean matches(String password) {
		return password != null && passwordHash.equals(hash(password));
	}
}
