package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.TenantParameter;
import org.springframework.data.repository.CrudRepository;

public interface TenantParameterRepository extends CrudRepository<TenantParameter, Integer> {
	TenantParameter findTopByName(String name);
}
