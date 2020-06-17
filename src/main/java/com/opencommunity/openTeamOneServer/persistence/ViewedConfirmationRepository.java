package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.ViewedConfirmation;
import com.opencommunity.openTeamOneServer.data.ViewedConfirmationKey;
import org.springframework.data.repository.CrudRepository;

public interface ViewedConfirmationRepository extends CrudRepository<ViewedConfirmation, ViewedConfirmationKey> {
	// for message confirmations
	Iterable<ViewedConfirmation> findByMessageId(String messageId);
	// for viewedCount (simple version)
	long countByMessageIdAndPersonIdNot(String messageId, Integer personId);
	// for badgeCount etc.
	ViewedConfirmation findTopByPersonIdAndRoomIdOrderByMessagePostedAtDesc(Integer personId, String roomId);
	// for viewedAt etc.
	ViewedConfirmation findTopByMessageIdAndPersonId(String messageId, Integer personId);
	// for debugging
	Iterable<ViewedConfirmation> findByPersonId(Integer personId);
}
