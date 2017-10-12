package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Util {

	public static String getUuid() {
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}

	public static Map<String, String> split(String url) {
		Map<String, String> map = new HashMap<>();
		String[] assignments = url.split("&");
		for (int i = 0; i < assignments.length; i++) {
			String[] parts = assignments[i].split("=");
			if (parts.length == 2) {
				try {
					map.put(parts[0], URLDecoder.decode(parts[1], "UTF-8"));
				} catch (Exception e) { }
			}
		}
		return map;
	}

	public static ResponseEntity<String> defaultStringResponse(HttpStatus httpStatus) {
		HttpHeaders httpHeaders= new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>("{}", httpHeaders, httpStatus);
	}

	public static int getInt(JSONObject item, String key, int defaultValue) throws JSONException {
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

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public static String toIsoDate(long millis) {
		return sdf.format(new Date(millis));
	}

	public static long getIsoDate(JSONObject item, String key) throws JSONException {
		String date = item == null || item.isNull(key) ? null : item.getString(key);
		try {
			return sdf.parse(date).getTime();
		} catch (ParseException e) {
			throw new JSONException("Wrong iso date format");
		}
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
		if (item != null && key != null && value != null)
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
