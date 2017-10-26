package com.opencommunity.openTeamOneServer;

import java.io.Serializable;

public class SubscriptionKey implements Serializable {

	private static final long serialVersionUID = -1L;

	public String targetType;
	public String appId;
	public String deviceToken;
	public String userId;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SubscriptionKey)) return false;

		SubscriptionKey that = (SubscriptionKey) o;

		return
			targetType.equals(that.targetType) &&
			appId.equals(that.appId) &&
			deviceToken.equals(that.deviceToken) &&
			userId.equals(that.userId);
	}

	@Override
	public int hashCode() {
		int result = targetType.hashCode();
		result = 31 * result + appId.hashCode();
		result = 31 * result + deviceToken.hashCode();
		result = 31 * result + userId.hashCode();
		return result;
	}
}
