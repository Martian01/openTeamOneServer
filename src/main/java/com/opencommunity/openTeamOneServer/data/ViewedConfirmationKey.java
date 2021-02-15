package com.opencommunity.openTeamOneServer.data;

import java.io.*;

public class ViewedConfirmationKey implements Serializable {

	private static final long serialVersionUID = -1L;

	public Integer messageId;
	public Integer personId;

	public ViewedConfirmationKey() { }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ViewedConfirmationKey)) return false;
		ViewedConfirmationKey other = (ViewedConfirmationKey) o;
		return messageId.equals(other.messageId) && personId.equals(other.personId);
	}

	@Override
	public int hashCode() {
		int result = messageId.hashCode();
		result = 31 * result + personId.hashCode();
		return result;
	}
}

