package com.opencommunity.openTeamOneServer.data;

import java.io.Serializable;

public class ViewedConfirmationKey implements Serializable {

	private static final long serialVersionUID = -2L;

	public String messageId;
	public String personId;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ViewedConfirmationKey)) return false;

		ViewedConfirmationKey that = (ViewedConfirmationKey) o;

		return messageId.equals(that.messageId) && personId.equals(that.personId);
	}

	@Override
	public int hashCode() {
		int result = messageId.hashCode();
		result = 31 * result + personId.hashCode();
		return result;
	}
}

