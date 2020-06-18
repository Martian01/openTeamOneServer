package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
	User findTopByUserId(String userId);
	// for contacts
	long countByPersonIdAndHasUserRoleTrue(Integer personId);
	Iterable<User> findByPersonIdNotNullAndHasUserRoleTrue();
	// for initialisation
	long countByHasAdminRoleTrue();
	// for deletion
	Iterable<User> findByUserIdNot(String userId);
	Iterable<User> findByHasAdminRoleFalseAndUserIdNot(String userId);
}
