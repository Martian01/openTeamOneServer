package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.*;
import org.json.*;

import javax.persistence.*;
import java.util.*;

@Entity
@IdClass(SubscriptionKey.class)
public class Subscription {

	@Id
	@Column
	public Integer targetTypeHash;
	@Id
	@Column
	public Integer appIdHash;
	@Id
	@Column
	public Integer deviceTokenHash;
	@Id
	@Column
	public Integer userIdHash;

	@Column(length = 16)
	public String targetType;
	@Column(length = 32)
	public String appId;
	@Column(length = 200)
	public String deviceToken;
	@Column(length = 16)
	public String userId;

	@Column(length = 5)
	public String language;
	@Column(length = 100)
	public String clientAccountId;
	@Column
	public boolean userConsent;
	@Column
	public boolean isActive;
	@Column
	public long changedAt;

	@Column(length = 32)
	public String deviceId;
	@Column(length = 32)
	public String deviceType;
	@Column(length = 32)
	public String osVersion;
	@Column(length = 32)
	public String encryption;
	@Column(length = 32)
	public String appVersion;

	public Subscription() { }

	public Subscription(
			String targetType,
			String appId,
			String deviceToken,
			String userId,
			String language,
			String clientAccountId,
			boolean userConsent,
			boolean isActive,
			long changedAt,
			String deviceId,
			String deviceType,
			String osVersion,
			String encryption,
			String appVersion
	) {
		this.targetType = targetType;
		this.appId = appId;
		this.deviceToken = deviceToken;
		this.userId = userId;
		this.language = language;
		this.clientAccountId = clientAccountId;
		this.userConsent = userConsent;
		this.isActive = isActive;
		this.changedAt = changedAt;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.osVersion = osVersion;
		this.encryption = encryption;
		this.appVersion = appVersion;
		normalize();
	}

	public Subscription(JSONObject item) throws JSONException {
		targetType = JsonUtil.getString(item, "targetType");
		appId = JsonUtil.getString(item, "appId");
		deviceToken = JsonUtil.getString(item, "deviceToken");
		userId = JsonUtil.getString(item, "userId");
		language = JsonUtil.getString(item, "language");
		clientAccountId = JsonUtil.getString(item, "clientAccountId");
		userConsent = JsonUtil.getBoolean(item, "userConsent");
		isActive = JsonUtil.getBoolean(item, "isActive");
		changedAt = JsonUtil.getIsoDate(item, "changedAt");
		deviceId = JsonUtil.getString(item, "deviceId");
		deviceType = JsonUtil.getString(item, "deviceType");
		osVersion = JsonUtil.getString(item, "osVersion");
		encryption = JsonUtil.getString(item, "encryption");
		appVersion = JsonUtil.getString(item, "appVersion");
		normalize();
	}

	public void normalize() {
		if (targetType != null) targetTypeHash = targetType.hashCode();
		if (appId != null) appIdHash = appId.hashCode();
		if (deviceToken != null) deviceTokenHash = deviceToken.hashCode();
		if (userId != null) userIdHash = userId.hashCode();
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
		item.put("isActive", isActive);
		item.put("changedAt", TimeUtil.toIsoDateString(changedAt));
		item.put("deviceId", deviceId);
		item.put("deviceType", deviceType);
		item.put("osVersion", osVersion);
		item.put("encryption", encryption);
		item.put("appVersion", appVersion);
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

	public static JSONArray toJsonArray(Iterable<Subscription> subscriptionLogs) throws JSONException {
		JSONArray array = new JSONArray();
		for (Subscription subscription : subscriptionLogs)
			array.put(subscription.toJson());
		return array;
	}

	public Integer getTargetTypeHash() {
		return targetTypeHash;
	}

	public void setTargetTypeHash(Integer targetTypeHash) {
		this.targetTypeHash = targetTypeHash;
	}

	public Integer getAppIdHash() {
		return appIdHash;
	}

	public void setAppIdHash(Integer appIdHash) {
		this.appIdHash = appIdHash;
	}

	public Integer getDeviceTokenHash() {
		return deviceTokenHash;
	}

	public void setDeviceTokenHash(Integer deviceTokenHash) {
		this.deviceTokenHash = deviceTokenHash;
	}

	public Integer getUserIdHash() {
		return userIdHash;
	}

	public void setUserIdHash(Integer userIdHash) {
		this.userIdHash = userIdHash;
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

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public long getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(long changedAt) {
		this.changedAt = changedAt;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getEncryption() {
		return encryption;
	}

	public void setEncryption(String encryption) {
		this.encryption = encryption;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
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
