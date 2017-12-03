package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
	// for contacts
	long countByPersonIdAndHasUserRoleTrue(String personId);
	Iterable<User> findByPersonIdNotNullAndHasUserRoleTrue();
	// for initialisation
	long countByHasAdminRoleTrue();
	// for deletion
	Iterable<User> findByUserIdNot(String userId);
	Iterable<User> findByHasAdminRoleFalseAndUserIdNot(String userId);
}
