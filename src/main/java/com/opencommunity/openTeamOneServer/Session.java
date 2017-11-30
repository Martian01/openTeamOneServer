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
	public boolean iosMode;

	public Session(String userId, boolean iosMode) {
		sessionId = Util.getUuid();
		this.userId = userId;
		startTime = System.currentTimeMillis();
		lastAccessTime = startTime;
		this.iosMode = iosMode;
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

	private static final long sessionMaximumAge = 1800000L;

	private static Map<String, Session> currentSessions = new HashMap<>();

	public static Session getSession(@NotNull String sessionId) {
		synchronized (currentSessions) {
			long now = System.currentTimeMillis();
			Session session = currentSessions.get(sessionId);
			if (session != null) {
				if (now - session.lastAccessTime > sessionMaximumAge) {
					currentSessions.remove(sessionId);
					return null;
				}
				session.lastAccessTime = now;
			}
			return session;
		}
	}

	public static Session newSession(@NotNull String userId, boolean iosMode) {
		synchronized (currentSessions) {
			Session session = new Session(userId, iosMode);
			currentSessions.put(session.sessionId, session);
			return session;
		}
	}

	public static void invalidateSession(@NotNull String sessionId) {
		synchronized (currentSessions) {
			currentSessions.remove(sessionId);
		}
	}

	public static void invalidateOldSessions() {
		synchronized (currentSessions) {
			long now = System.currentTimeMillis();
			for (Session session : currentSessions.values())
				if (now - session.lastAccessTime > sessionMaximumAge)
					currentSessions.remove(session.sessionId);
		}
	}

}
