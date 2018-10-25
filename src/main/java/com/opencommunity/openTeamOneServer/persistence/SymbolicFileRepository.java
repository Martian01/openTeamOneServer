package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.SymbolicFile;
import org.springframework.data.repository.CrudRepository;

public interface SymbolicFileRepository extends CrudRepository<SymbolicFile, String> {
	Iterable<SymbolicFile> findByReferenceIdOrderByPositionAsc(String referenceId);
}