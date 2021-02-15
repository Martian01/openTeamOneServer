package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.*;
import org.springframework.data.repository.*;

public interface SymbolicFileRepository extends CrudRepository<SymbolicFile, Integer> {
	Iterable<SymbolicFile> findByReferenceIdOrderByPositionAsc(Integer referenceId);
}
