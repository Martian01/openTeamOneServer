package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.*;

public interface MessageRepository extends CrudRepository<Message, Integer> {
	// for latest message
	Message findTopByRoomIdAndIsDeletedFalseOrderByPostedAtDesc(Integer roomId);
	// for badge count
	long countByRoomIdAndIsDeletedFalseAndPostedAtGreaterThan(Integer roomId, long postedAt);
	// for viewed confirmations
	Iterable<Message> findByRoomIdAndIsDeletedFalseAndPostedAtGreaterThan(Integer roomId, long postedAt);
	Iterable<Message> findByRoomIdAndIsDeletedFalseAndPostedAtGreaterThanAndPostedAtLessThanEqual(Integer roomId, long postedAtLow, long postedAtHigh);
	Iterable<Message> findTop1ByRoomIdAndIsDeletedFalseOrderByPostedAtDesc(Integer roomId);
	Iterable<Message> findTop1ByRoomIdAndIsDeletedFalseAndPostedAtLessThanEqualOrderByPostedAtDesc(Integer roomId, long postedAt);
	// for messages since
	Iterable<Message> findByUpdatedAtGreaterThanAndPostedAtGreaterThanEqual(long updatedAt, long postedAt);
	Iterable<Message> findByUpdatedAtGreaterThan(long updatedAt);
	Iterable<Message> findByPostedAtGreaterThanEqual(long postedAt);
	// for room messages since
	Iterable<Message> findByRoomIdAndUpdatedAtGreaterThanAndPostedAtGreaterThanEqual(Integer roomId, long updatedAt, long postedAt);
	Iterable<Message> findByRoomIdAndUpdatedAtGreaterThan(Integer roomId, long updatedAt);
	Iterable<Message> findByRoomIdAndPostedAtGreaterThanEqual(Integer roomId, long postedAt);
	// for messages until
	Page<Message> findByPostedAtLessThanOrderByPostedAtDesc(long postedAt, Pageable pageable);
	Page<Message> findByOrderByPostedAtDesc(Pageable pageable);
	Iterable<Message> findByPostedAtBetween(long postedAtLow, long postedAtHigh);
	Iterable<Message> findByPostedAtLessThan(long postedAt);
	// for room messages until
	Page<Message> findByRoomIdAndPostedAtLessThanOrderByPostedAtDesc(Integer roomId, long postedAt, Pageable pageable);
	Page<Message> findByRoomIdOrderByPostedAtDesc(Integer roomId, Pageable pageable);
	Iterable<Message> findByRoomIdAndPostedAtBetween(Integer roomId, long postedAtLow, long postedAtHigh);
	Iterable<Message> findByRoomIdAndPostedAtLessThan(Integer roomId, long postedAt);
	// for debugging
	Iterable<Message> findByRoomId(Integer roomId);
	Message findTopByClientMessageId(String clientMessageId);
}
