package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.*;
import org.json.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class User {

	@Id
	@Column
	public Integer userIdHash;

	@Column(length = 16)
	public String userId;
	@Column(length = 200)
	public String passwordHash;
	@Column
	public Integer personId;
	@Column
	public boolean hasUserRole;
	@Column
	public boolean hasAdminRole;

	public User() { }

	public User(String userId, String password, Integer personId, boolean hasUserRole, boolean hasAdminRole) {
		this.userId = userId == null || userId.length() == 0 ? Integer.toString(RestLib.getRandomInt()) : userId.toLowerCase();
		setPassword(password);
		this.personId = personId;
		this.hasUserRole = hasUserRole;
		this.hasAdminRole = hasAdminRole;
		normalize();
	}

	public User(JSONObject item) throws JSONException {
		userId = JsonUtil.getString(item, "userId");
		userId = userId == null || userId.length() == 0 ? Integer.toString(RestLib.getRandomInt()) : userId.toLowerCase();
		// accept unencrypted password or password hash
		String password = JsonUtil.getString(item, "password");
		if (password != null)
			setPassword(password);
		else
			passwordHash = JsonUtil.getString(item, "passwordHash");
		personId = JsonUtil.getInteger(item, "personId");
		hasUserRole = JsonUtil.getBoolean(item, "hasUserRole");
		hasAdminRole = JsonUtil.getBoolean(item, "hasAdminRole");
		normalize();
	}

	public void normalize() {
		if (userId != null) userIdHash = userId.hashCode();
	}

	public JSONObject toJson(boolean withPasswordHash) throws JSONException {
		JSONObject item = new JSONObject();
		item.put("userId", userId);
		if (withPasswordHash)
			item.put("passwordHash", passwordHash);
		item.put("personId", personId);
		item.put("hasUserRole", hasUserRole);
		item.put("hasAdminRole", hasAdminRole);
		return item;
	}

	public static Iterable<User> fromJsonArray(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<User> userList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			userList.add(new User(array.getJSONObject(i)));
		return userList;
	}

	public static JSONArray toJsonArray(Iterable<User> users, boolean withPasswordHash) throws JSONException {
		JSONArray array = new JSONArray();
		for (User user : users)
			array.put(user.toJson(withPasswordHash));
		return array;
	}

	public Integer getUserIdHash() {
		return userIdHash;
	}

	public void setUserIdHash(Integer userIdHash) {
		this.userIdHash = userIdHash;
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

	public Integer getPersonId() {
		return personId;
	}

	public void setPersonId(Integer personId) {
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
		String output = getClass().getSimpleName();
		try {
			output += toJson(false).toString();
		} catch (JSONException e) { }
		return output;
	}

	public void setPassword(String password) {
		passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
	}

	public boolean matches(String password) {
		return password != null && BCrypt.checkpw(password, passwordHash);
	}
}
