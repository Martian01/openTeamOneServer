package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.RoomMember;
import com.opencommunity.openTeamOneServer.data.RoomMemberKey;
import org.springframework.data.repository.CrudRepository;

public interface RoomMemberRepository extends CrudRepository<RoomMember, RoomMemberKey> {
	// for members
	Iterable<RoomMember> findByRoomId(String roomId);
	// for rooms
	Iterable<RoomMember> findByPersonId(Integer personId);
	// full key
	RoomMember findTopByRoomIdAndPersonId(String roomId, Integer personId);
	// for private rooms
	RoomMember findTopByRoomIdAndPersonIdNot(String roomId, Integer personId);
	// for specific membership (validation)
	long countByRoomIdAndPersonId(String roomId, Integer personId);
}
