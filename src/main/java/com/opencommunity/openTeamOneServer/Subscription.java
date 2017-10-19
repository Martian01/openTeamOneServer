package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.util.ArrayList;

interface SubscriptionRepository extends CrudRepository<Subscription, SubscriptionKey> {
}

@Entity
@IdClass(SubscriptionKey.class)
public class Subscription {

	@Id
	public String targetType;
	@Id
	public String appId;
	@Id
	public String deviceToken;
	@Id
	public String userId;
	@Column
	public String language;
	@Column
	public String clientAccountId;
	@Column
	public boolean userConsent;
	@Column
	public long changedAt;

	public Subscription() {
	}

	public Subscription(SubscriptionLog log) {
		targetType = log.targetType;
		appId = log.appId;
		deviceToken = log.deviceToken;
		userId = log.userId;
		language = log.language;
		clientAccountId = log.clientAccountId;
		userConsent = log.userConsent;
		changedAt = log.changedAt;
	}

	public Subscription(JSONObject item) throws JSONException {
		targetType = JsonUtil.getString(item, "targetType");
		appId = JsonUtil.getString(item, "appId");
		deviceToken = JsonUtil.getString(item, "deviceToken");
		userId = JsonUtil.getString(item, "userId");
		language = JsonUtil.getString(item, "language");
		clientAccountId = JsonUtil.getString(item, "clientAccountId");
		userConsent = JsonUtil.getBoolean(item, "userConsent");
		changedAt = JsonUtil.getIsoDate(item, "changedAt");
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("targetType", targetType);
		item.put("appId", appId);
		item.put("deviceToken", deviceToken);
		item.put("userId", userId);
		item.put("language", language);
		item.put("clientAccountId", clientAccountId);
		item.put("userConsent", userConsent);
		item.put("changedAt", JsonUtil.toIsoDate(changedAt));
		return item;
	}

	public static Iterable<Subscription> fromJsonArray(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<Subscription> subscriptionList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			subscriptionList.add(new Subscription(array.getJSONObject(i)));
		return subscriptionList;
	}

	public static JSONArray toJsonArray(Iterable<Subscription> subscriptions) throws JSONException {
		JSONArray array = new JSONArray();
		for (Subscription subscription : subscriptions)
			array.put(subscription.toJson());
		return array;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getClientAccountId() {
		return clientAccountId;
	}

	public void setClientAccountId(String clientAccountId) {
		this.clientAccountId = clientAccountId;
	}

	public boolean isUserConsent() {
		return userConsent;
	}

	public void setUserConsent(boolean userConsent) {
		this.userConsent = userConsent;
	}

	public long getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(long changedAt) {
		this.changedAt = changedAt;
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
