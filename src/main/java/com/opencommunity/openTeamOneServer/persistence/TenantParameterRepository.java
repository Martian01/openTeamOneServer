package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.*;
import org.springframework.data.repository.*;

public interface TenantParameterRepository extends CrudRepository<TenantParameter, Integer> {
	TenantParameter findTopByName(String name);
}
