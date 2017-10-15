package com.opencommunity.openTeamOneServer;

import org.json.JSONException;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class Session {

	public String sessionId;
	public String userId;
	public long startTime;
	public long lastAccessTime;

	public String csrfToken;

	public Session(String userId) {
		sessionId = Util.getUuid();
		this.userId = userId;
		startTime = System.currentTimeMillis();
		lastAccessTime = startTime;
		csrfToken = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("sessionId", sessionId);
		item.put("userId", userId);
		item.put("startTime", JsonUtil.toIsoDate(startTime));
		item.put("lastAccessTime", JsonUtil.toIsoDate(lastAccessTime));
		return item;
	}

	@Override
	public String toString() {
		String output = getClass().getSimpleName();
		try {
			output += toJson().toString();
		} catch (JSONException e) { }
		return output;
	}

	public String getNewCsrfToken() {
		return csrfToken = Util.getUuid();
	}

	private static final long sessionMaximumAge = 1800000L;

	private static Map<String, Session> currentSessions = new HashMap<>();

	public static Session getSession(@NotNull String sessionId) {
		Session session = currentSessions.get(sessionId);
		if (session != null)
			session.lastAccessTime = System.currentTimeMillis();
		return session;
	}

	public static Session newSession(@NotNull String userId) {
		Session session = new Session(userId);
		currentSessions.put(session.sessionId, session);
		return session;
	}

	public static String newSessionId(@NotNull String userId) {
		return newSession(userId).sessionId;
	}

	public static void invalidateSession(@NotNull String sessionId) {
		currentSessions.remove(sessionId);
	}

	public static void invalidateOldSessions(long maximumAge) {
		long now = System.currentTimeMillis();
		for (Session session : currentSessions.values())
			if (now - session.lastAccessTime > maximumAge)
				invalidateSession(session.sessionId);
	}

	public static void invalidateOldSessions() {
		invalidateOldSessions(sessionMaximumAge);
	}

}
