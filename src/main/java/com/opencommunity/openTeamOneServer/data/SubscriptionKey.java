package com.opencommunity.openTeamOneServer.data;

import java.io.Serializable;

public class SubscriptionKey implements Serializable {

	private static final long serialVersionUID = -1L;

	public Integer targetTypeHash;
	public Integer appIdHash;
	public Integer deviceTokenHash;
	public Integer userIdHash;

	public SubscriptionKey(String targetType, String appId, String deviceToken, String userId) {
		if (targetType != null) targetTypeHash = targetType.hashCode();
		if (appId != null) appIdHash = appId.hashCode();
		if (deviceToken != null) deviceTokenHash = deviceToken.hashCode();
		if (userId != null) userIdHash = userId.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SubscriptionKey)) return false;

		SubscriptionKey that = (SubscriptionKey) o;

		return
			targetTypeHash.equals(that.targetTypeHash) &&
			appIdHash.equals(that.appIdHash) &&
			deviceTokenHash.equals(that.deviceTokenHash) &&
			userIdHash.equals(that.userIdHash);
	}

	@Override
	public int hashCode() {
		int result = targetTypeHash.hashCode();
		result = 31 * result + appIdHash.hashCode();
		result = 31 * result + deviceTokenHash.hashCode();
		result = 31 * result + userIdHash.hashCode();
		return result;
	}
}
