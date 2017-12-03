package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.ViewedConfirmation;
import com.opencommunity.openTeamOneServer.data.ViewedConfirmationKey;
import org.springframework.data.repository.CrudRepository;

public interface ViewedConfirmationRepository extends CrudRepository<ViewedConfirmation, ViewedConfirmationKey> {
	// for message confirmations
	Iterable<ViewedConfirmation> findByMessageId(String messageId);
	// for viewedCount (simple version)
	long countByMessageIdAndPersonIdNot(String messageId, String personId);
	// for badgeCount etc.
	ViewedConfirmation findTopByPersonIdAndRoomIdOrderByMessagePostedAtDesc(String personId, String roomId);
	// for viewedAt etc.
	ViewedConfirmation findTopByMessageIdAndPersonId(String messageId, String personId);
	// for debugging
	Iterable<ViewedConfirmation> findByPersonId(String personId);
}
