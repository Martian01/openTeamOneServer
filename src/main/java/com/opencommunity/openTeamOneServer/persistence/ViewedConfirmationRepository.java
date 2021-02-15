package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.*;
import org.springframework.data.repository.*;

public interface ViewedConfirmationRepository extends CrudRepository<ViewedConfirmation, ViewedConfirmationKey> {
	// for message confirmations
	Iterable<ViewedConfirmation> findByMessageId(Integer messageId);
	// for viewedCount (simple version)
	long countByMessageIdAndPersonIdNot(Integer messageId, Integer personId);
	// for badgeCount etc.
	ViewedConfirmation findTopByPersonIdAndRoomIdOrderByMessagePostedAtDesc(Integer personId, Integer roomId);
	// for viewedAt etc.
	ViewedConfirmation findTopByMessageIdAndPersonId(Integer messageId, Integer personId);
	// for debugging
	Iterable<ViewedConfirmation> findByPersonId(Integer personId);
}
