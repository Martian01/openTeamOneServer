package com.opencommunity.openTeamOneServer;

import java.io.Serializable;

public class SubscriptionKey implements Serializable {

	private static final long serialVersionUID = -1L;

	public String targetType;
	public String appId;
	public String deviceToken;
	public String userId;

}
