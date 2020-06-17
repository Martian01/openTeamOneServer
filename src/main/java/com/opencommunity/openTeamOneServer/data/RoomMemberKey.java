package com.opencommunity.openTeamOneServer.data;

import java.io.Serializable;

public class RoomMemberKey implements Serializable {

	private static final long serialVersionUID = -1L;

	public String roomId;
	public Integer personId;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RoomMemberKey)) return false;

		RoomMemberKey that = (RoomMemberKey) o;

		return roomId.equals(that.roomId) && personId.equals(that.personId);
	}

	@Override
	public int hashCode() {
		int result = roomId.hashCode();
		result = 31 * result + personId.hashCode();
		return result;
	}
}

