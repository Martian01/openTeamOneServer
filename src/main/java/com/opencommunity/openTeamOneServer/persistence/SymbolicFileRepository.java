package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.SymbolicFile;
import org.springframework.data.repository.CrudRepository;

public interface SymbolicFileRepository extends CrudRepository<SymbolicFile, Integer> {
	Iterable<SymbolicFile> findByReferenceIdOrderByPositionAsc(Integer referenceId);
}
