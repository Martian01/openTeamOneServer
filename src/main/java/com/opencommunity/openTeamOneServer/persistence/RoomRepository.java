package com.opencommunity.openTeamOneServer.persistence;

import com.opencommunity.openTeamOneServer.data.*;
import org.springframework.data.repository.*;

public interface RoomRepository extends CrudRepository<Room, Integer> {
	Iterable<Room> findByRoomType(String roomType);
}
