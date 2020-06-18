package com.opencommunity.openTeamOneServer.data;

import java.io.Serializable;

public class RoomMemberKey implements Serializable {

	private static final long serialVersionUID = -1L;

	public Integer roomId;
	public Integer personId;

	public RoomMemberKey() { }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RoomMemberKey)) return false;
		RoomMemberKey other = (RoomMemberKey) o;
		return roomId.equals(other.roomId) && personId.equals(other.personId);
	}

	@Override
	public int hashCode() {
		int result = roomId.hashCode();
		result = 31 * result + personId.hashCode();
		return result;
	}
}

