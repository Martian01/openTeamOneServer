package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.JsonUtil;
import com.opencommunity.openTeamOneServer.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Session {

	public String sessionId;
	public String userId;
	public long startTime;
	public long lastAccessTime;

	public Session(String userId, boolean iosMode) {
		sessionId = Util.getUuid();
		if (iosMode)
			sessionId += IOS_MARKER;
		this.userId = userId;
		startTime = System.currentTimeMillis();
		lastAccessTime = startTime;
	}

	public Session(JSONObject item) throws JSONException {
		sessionId = JsonUtil.getString(item, "sessionId");
		userId = JsonUtil.getString(item, "userId");
		startTime = JsonUtil.getIsoDate(item, "startTime");
		lastAccessTime = JsonUtil.getIsoDate(item, "lastAccessTime");
		//
		if (sessionId == null || sessionId.length() == 0)
			sessionId = Util.getUuid();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("sessionId", sessionId);
		item.put("userId", userId);
		item.put("startTime", JsonUtil.toIsoDate(startTime));
		item.put("lastAccessTime", JsonUtil.toIsoDate(lastAccessTime));
		return item;
	}

	public static Iterable<Session> fromJsonArray(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<Session> sessionList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			sessionList.add(new Session(array.getJSONObject(i)));
		return sessionList;
	}

	public static JSONArray toJsonArray(Iterable<Session> sessions) throws JSONException {
		JSONArray array = new JSONArray();
		for (Session session : sessions)
			array.put(session.toJson());
		return array;
	}

	public static JSONArray toJsonArray() throws JSONException {
		return toJsonArray(currentSessions.values());
	}

	@Override
	public String toString() {
		String output = getClass().getSimpleName();
		try {
			output += toJson().toString();
		} catch (JSONException e) { }
		return output;
	}

	private static final String IOS_MARKER = "~";

	public static boolean iosMode(String sessionId) {
		return sessionId != null && sessionId.endsWith(IOS_MARKER);
	}

	public boolean iosMode() {
		return iosMode(sessionId);
	}

	private static final long sessionMaximumAge = 1800000L; // 30 minutes

	private static Map<String, Session> currentSessions = new HashMap<>();

	public static Session findSession(@NotNull String sessionId) {
		synchronized (currentSessions) {
			return currentSessions.get(sessionId);
		}
	}

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

	@NotNull
	public static Session newSession(@NotNull String userId, boolean iosMode) {
		synchronized (currentSessions) {
			_invalidateOldSessions(); // bit of housekeeping
			Session session = new Session(userId, iosMode);
			currentSessions.put(session.sessionId, session);
			return session;
		}
	}

	public static void updateSession(@NotNull Session session) {
		synchronized (currentSessions) {
			currentSessions.put(session.sessionId, session);
		}
	}

	public static void invalidateSession(@NotNull String sessionId) {
		synchronized (currentSessions) {
			currentSessions.remove(sessionId);
		}
	}

	private static void _invalidateOldSessions() {
		long now = System.currentTimeMillis();
		Iterator<Session> it = currentSessions.values().iterator();
		while (it.hasNext())
			if (now - it.next().lastAccessTime > sessionMaximumAge)
				it.remove(); // removes the entire map entry
	}

}
