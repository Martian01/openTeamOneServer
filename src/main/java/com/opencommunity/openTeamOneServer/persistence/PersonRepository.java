package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, Integer> {
}
