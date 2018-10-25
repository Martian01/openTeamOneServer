package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, String> {
	// for latest message
	Message findTopByRoomIdAndIsDeletedFalseOrderByPostedAtDesc(String roomId);
	// for badge count
	long countByRoomIdAndIsDeletedFalseAndPostedAtGreaterThan(String roomId, long postedAt);
	// for viewed confirmations
	Iterable<Message> findByRoomIdAndIsDeletedFalseAndPostedAtGreaterThan(String roomId, long postedAt);
	Iterable<Message> findByRoomIdAndIsDeletedFalseAndPostedAtGreaterThanAndPostedAtLessThanEqual(String roomId, long postedAtLow, long postedAtHigh);
	Iterable<Message> findTop1ByRoomIdAndIsDeletedFalseOrderByPostedAtDesc(String roomId);
	Iterable<Message> findTop1ByRoomIdAndIsDeletedFalseAndPostedAtLessThanEqualOrderByPostedAtDesc(String roomId, long postedAt);
	// for messages since
	Iterable<Message> findByUpdatedAtGreaterThanAndPostedAtGreaterThanEqual(long updatedAt, long postedAt);
	Iterable<Message> findByUpdatedAtGreaterThan(long updatedAt);
	Iterable<Message> findByPostedAtGreaterThanEqual(long postedAt);
	// for room messages since
	Iterable<Message> findByRoomIdAndUpdatedAtGreaterThanAndPostedAtGreaterThanEqual(String roomId, long updatedAt, long postedAt);
	Iterable<Message> findByRoomIdAndUpdatedAtGreaterThan(String roomId, long updatedAt);
	Iterable<Message> findByRoomIdAndPostedAtGreaterThanEqual(String roomId, long postedAt);
	// for messages until
	Page<Message> findByPostedAtLessThanOrderByPostedAtDesc(long postedAt, Pageable pageable);
	Page<Message> findByOrderByPostedAtDesc(Pageable pageable);
	Iterable<Message> findByPostedAtBetween(long postedAtLow, long postedAtHigh);
	Iterable<Message> findByPostedAtLessThan(long postedAt);
	// for room messages until
	Page<Message> findByRoomIdAndPostedAtLessThanOrderByPostedAtDesc(String roomId, long postedAt, Pageable pageable);
	Page<Message> findByRoomIdOrderByPostedAtDesc(String roomId, Pageable pageable);
	Iterable<Message> findByRoomIdAndPostedAtBetween(String roomId, long postedAtLow, long postedAtHigh);
	Iterable<Message> findByRoomIdAndPostedAtLessThan(String roomId, long postedAt);
	// for debugging
	Iterable<Message> findByRoomId(String roomId);
	Message findTopByClientMessageId(String clientMessageId);
}
