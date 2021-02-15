package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.*;
import org.springframework.data.repository.*;

public interface RoomMemberRepository extends CrudRepository<RoomMember, RoomMemberKey> {
	// for members
	Iterable<RoomMember> findByRoomId(Integer roomId);
	// for rooms
	Iterable<RoomMember> findByPersonId(Integer personId);
	// full key
	RoomMember findTopByRoomIdAndPersonId(Integer roomId, Integer personId);
	// for private rooms
	RoomMember findTopByRoomIdAndPersonIdNot(Integer roomId, Integer personId);
	// for specific membership (validation)
	long countByRoomIdAndPersonId(Integer roomId, Integer personId);
}
