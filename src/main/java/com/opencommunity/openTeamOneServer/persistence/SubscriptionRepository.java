package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.*;
import org.springframework.data.repository.*;

public interface SubscriptionRepository extends CrudRepository<Subscription, SubscriptionKey> {
}
