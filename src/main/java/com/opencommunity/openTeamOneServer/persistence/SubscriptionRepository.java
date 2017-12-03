package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.Subscription;
import com.opencommunity.openTeamOneServer.data.SubscriptionKey;
import org.springframework.data.repository.CrudRepository;

public interface SubscriptionRepository extends CrudRepository<Subscription, SubscriptionKey> {
	Subscription findTopByTargetTypeAndAppIdAndDeviceTokenAndUserId(String targetType, String appId, String deviceToken, String userId);
}
