package com.opencommunity.openTeamOneServer.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

	public static Integer getInteger(JSONObject item, String key) throws JSONException {
		return getInteger(item, key, null);
	}

	public static Integer getInteger(JSONObject item, String key, Integer defaultValue) throws JSONException {
		return item == null || item.isNull(key) ? defaultValue : item.getInt(key);
	}

	public static boolean getBoolean(JSONObject item, String key) throws JSONException {
		return getBoolean(item, key, false);
	}

	public static boolean getBoolean(JSONObject item, String key, boolean defaultValue) throws JSONException {
		return item == null || item.isNull(key) ? defaultValue : item.getBoolean(key);
	}

	public static String getString(JSONObject item, String key) throws JSONException {
		return getString(item, key, null);
	}

	public static String getString(JSONObject item, String key, String defaultValue) throws JSONException {
		return item == null || item.isNull(key) ? defaultValue : item.getString(key);
	}

	public static JSONObject getJSONObject(JSONObject item, String key) throws JSONException {
		return item == null || item.isNull(key) ? null : item.getJSONObject(key);
	}

	public static JSONArray getJSONArray(JSONObject item, String key) throws JSONException {
		return item == null || item.isNull(key) ? null : item.getJSONArray(key);
	}

	public static long getIsoDate(JSONObject item, String key) throws JSONException {
		String date = item == null || item.isNull(key) ? null : item.getString(key);
			return TimeUtil.parseIsoDateTimeToMillis(date);
	}

	public static void put(JSONObject item, String key, int value) throws JSONException {
		if (item != null && key != null)
			item.put(key, value);
	}

	public static void put(JSONObject item, String key, boolean value) throws JSONException {
		if (item != null && key != null)
			item.put(key, value);
	}

	public static void put(JSONObject item, String key, String value) throws JSONException {
		if (item != null && key != null)
			item.put(key, value);
	}

	public static void put(JSONObject item, String key, JSONObject value) throws JSONException {
		if (item != null && key != null && value != null && value.length() > 0)
			item.put(key, value);
	}

	public static void put(JSONObject item, String key, JSONArray value) throws JSONException {
		if (item != null && key != null && value != null && value.length() > 0)
			item.put(key, value);
	}

	public static void put(JSONArray item, int value) throws JSONException {
		if (item != null)
			item.put(value);
	}

	public static void put(JSONArray item, boolean value) throws JSONException {
		if (item != null)
			item.put(value);
	}

	public static void put(JSONArray item, String value) throws JSONException {
		if (item != null && value != null)
			item.put(value);
	}

	public static void put(JSONArray item, JSONObject value) throws JSONException {
		if (item != null && value != null && value.length() > 0)
			item.put(value);
	}

	public static void put(JSONArray item, JSONArray value) throws JSONException {
		if (item != null && value != null && value.length() > 0)
			item.put(value);
	}

}
