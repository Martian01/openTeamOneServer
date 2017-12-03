package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.BCrypt;
import com.opencommunity.openTeamOneServer.util.JsonUtil;
import com.opencommunity.openTeamOneServer.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;


@Entity
public class User {
	@Id
	@Column(length = 32)
	public String userId;
	@Column(length = 200)
	public String passwordHash;
	@Column(length = 32)
	public String personId;
	@Column
	public boolean hasUserRole;
	@Column
	public boolean hasAdminRole;

	public User() {
	}

	public User(String userId, String password, String personId, boolean hasUserRole, boolean hasAdminRole) {
		this.userId = userId == null || userId.length() == 0 ? Util.getRandomString(8) : userId.toLowerCase();
		setPassword(password);
		this.personId = personId;
		this.hasUserRole = hasUserRole;
		this.hasAdminRole = hasAdminRole;
	}

	public User(JSONObject item) throws JSONException {
		userId = JsonUtil.getString(item, "userId");
		userId = userId == null || userId.length() == 0 ? Util.getRandomString(8) : userId.toLowerCase();
		// accept unencrypted password or password hash
		String password = JsonUtil.getString(item, "password");
		if (password != null)
			setPassword(password);
		else
			passwordHash = JsonUtil.getString(item, "passwordHash");
		personId = JsonUtil.getString(item, "personId");
		hasUserRole = JsonUtil.getBoolean(item, "hasUserRole");
		hasAdminRole = JsonUtil.getBoolean(item, "hasAdminRole");
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
