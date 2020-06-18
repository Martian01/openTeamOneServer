package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.Room;
import org.springframework.data.repository.CrudRepository;

public interface RoomRepository extends CrudRepository<Room, Integer> {
	Iterable<Room> findByRoomType(String roomType);
}
