package com.opencommunity.openTeamOneServer.util;

import com.opencommunity.openTeamOneServer.data.Message;
import com.opencommunity.openTeamOneServer.data.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Notification {

	private static Notification singleton = new Notification();

	public static void pushToSubscribedDevices(Message message, User user) {
		System.out.println("STATIC");
		singleton._pushToSubscribedDevices(message, user);
	}

	private static final String SQL_QUERY =
			"select s.app_id, s.device_token, s.target_type, s.client_account_id from subscription s " +
					"join user u on u.user_id = s.user_id " +
					"join room_member rm on rm.person_id = u.person_id " +
					"where s.is_active = 1 and s.changed_at > ? and rm.room_id = ?";

	private static final long HORIZON = 1000L * 60 * 60 * 24 * 28;

	private static Map<String, String> endPoints = new HashMap<String, String>() {{
		put("com.sap.sports.teamone", "TeamOne");
		put("com.sap.sports.teamone.local", "TeamOneLocal");
		put("com.sap.sports.teamone.release", "TeamOneRelease");
	}};

	private static class Recipient {
		String endPoint;
		String token;
		JSONObject payload;
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void _pushToSubscribedDevices(Message message, User user) {
		System.out.println("INSTANCE");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("RUNNABLE");
					Long subscriptionDate = System.currentTimeMillis() - HORIZON;
					List<Recipient> recipients = jdbcTemplate.query(
							SQL_QUERY,
							new Object[]{subscriptionDate, message.roomId},
							new RowMapper<Recipient>() {
								@Override
								public Recipient mapRow(ResultSet resultSet, int i) throws SQLException {
									String appId = resultSet.getString(1);
									String token = resultSet.getString(2);
									String targetType = resultSet.getString(3);
									String accountId = resultSet.getString(4);
									System.out.println("appId=" + appId + ", token=" + token.substring(0, 10) + ", targetType=" + targetType + ", accountId=" + accountId);
									if (appId == null || token == null || targetType == null || accountId == null)
										return null;
									//
									String endPoint = endPoints.get(appId);
									System.out.println("endPoint=" + endPoint);
									if (endPoint == null)
										return null;
									//
									Recipient recipient = new Recipient();
									recipient.endPoint = targetType + "/" + endPoint;
									recipient.token = token;
									if ("fcm".equals(targetType))
										recipient.payload = getFcmPayload(accountId);
									else if ("aps".equals(targetType))
										recipient.payload = getApsPayload(accountId, user);
									return recipient;
								}
							}
					);
					//
					System.out.println("POST SQL");
					for (Recipient recipient : recipients) {
						System.out.println("Recipient " + recipient.endPoint + ":" + recipient.token.substring(0, 10) + ":" + recipient.payload);
					}
					//
					Map<String, Map<JSONObject, Set<String>>> endPointMap = new HashMap<>();
					for (Recipient recipient : recipients) {
						if (endPointMap.get(recipient.endPoint) == null)
							endPointMap.put(recipient.endPoint, new HashMap<>());
						Map<JSONObject, Set<String>> payloadMap = endPointMap.get(recipient.endPoint);
						if (payloadMap.get(recipient.payload) == null)
							payloadMap.put(recipient.payload, new HashSet<>());
						Set<String> deviceSet = payloadMap.get(recipient.payload);
						deviceSet.add(recipient.token);
					}
					//
					for (String endPoint : endPointMap.keySet()) {
						System.out.println("MAP endpoint " + endPoint);
						Map<JSONObject, Set<String>> payloadMap = endPointMap.get(endPoint);
						for (JSONObject payload : payloadMap.keySet()) {
							System.out.println("-MAP payload " + payload);
							Set<String> deviceSet = payloadMap.get(payload);
							for (String device : deviceSet) {
								System.out.println("--MAP device " + device);
							}
						}
					}
					//
					for (String endPoint : endPointMap.keySet()) {
						JSONObject root = new JSONObject();
						JSONArray notifications = new JSONArray();
						root.put("notifications", notifications);
						Map<JSONObject, Set<String>> payloadMap = endPointMap.get(endPoint);
						for (JSONObject payload : payloadMap.keySet()) {
							JSONObject notification = new JSONObject();
							notifications.put(notification);
							JSONArray devices = new JSONArray();
							notification.put("message", payload);
							notification.put("devices", devices);
							Set<String> deviceSet = payloadMap.get(payload);
							for (String device : deviceSet) {
								devices.put(device);
							}
						}
						_sendToNotificationHub(endPoint, root.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			private JSONObject getFcmPayload(String accountId) {
				try {
					JSONObject fcmPayload = new JSONObject();
					JSONObject data = new JSONObject();
					fcmPayload.put("data", data);
					data.put("messageType", 1);
					data.put("clientAccountId", accountId);
					data.put("roomId", message.roomId);
					data.put("messageId", message.messageId);
					data.put("timestamp", message.postedAt);
					return fcmPayload;
				} catch (Exception ignored) { }
				return null;
			}

			private JSONObject getApsPayload(String accountId, User user) {
				try {
					String messageText = message.text == null || message.text.length() <= 1600 ? message.text :
							message.text.substring(0, 1600);
					if (user.personId.equals(message.senderPersonId)) {
						JSONObject apsPayload = new JSONObject();
						JSONObject aps = new JSONObject();
						apsPayload.put("aps", aps);
						aps.put("content-available", 1);
						JSONObject data = new JSONObject();
						apsPayload.put("data", data);
						data.put("clientAccountId", accountId);
						data.put("roomId", message.roomId);
						data.put("messageId", message.messageId);
						data.put("senderId", message.senderPersonId);
						data.put("timestamp", message.postedAt);
						data.put("message", messageText);
						return apsPayload;
					}
					if (messageText == null || messageText.length() == 0)
						messageText = "You have received a new message";
					JSONObject apsPayload = new JSONObject();
					JSONObject aps = new JSONObject();
					apsPayload.put("aps", aps);
					aps.put("content-available", 1);
					aps.put("badge", 1);
					aps.put("sound", "default");
					aps.put("alert", messageText);
					JSONObject data = new JSONObject();
					apsPayload.put("data", data);
					data.put("clientAccountId", accountId);
					data.put("roomId", message.roomId);
					data.put("messageId", message.messageId);
					data.put("senderId", message.senderPersonId);
					data.put("timestamp", message.postedAt);
					return apsPayload;
				} catch (Exception ignored) { }
				return null;
			}

		}).run();
	}

	private static final String NHUB_URL = "https://sportsone-nhub-test.cfapps.eu10.hana.ondemand.com/api/nhub/send/";

	private static final String AUTH_HEADER = "Basic dGVzdHNwb3J0czpTcG9ydHNTdWl0ZTE=";

	private void _sendToNotificationHub(String endPoint, String jsonBody) throws Exception {
		System.out.println("SEND endpoint: " + endPoint + ", body: " +jsonBody);
		byte[] body = jsonBody.getBytes(StandardCharsets.UTF_8);
		URL url = new URL(NHUB_URL + endPoint);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", AUTH_HEADER);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(body);
			outputStream.flush();
			outputStream.close();
			int responseCode = conn.getResponseCode();
			//if (responseCode > 299)
				System.out.println("NHUB response " + responseCode);
		}
		finally {
			conn.disconnect();
		}
	}}
