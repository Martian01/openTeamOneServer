package com.opencommunity.openTeamOneServer.util;

import com.opencommunity.openTeamOneServer.data.Message;
import com.opencommunity.openTeamOneServer.data.TenantParameter;
import com.opencommunity.openTeamOneServer.persistence.TenantParameterRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class Notification {

	private static Notification instance;

	public Notification() {
		instance = this;
	}

	@PostConstruct
	private void init() { }

	public static void pushToSubscribedDevices(Message message) {
		instance._pushToSubscribedDevices(message);
	}

	private static final String SQL_QUERY =
			"select s.target_type, s.app_id, s.device_token, s.client_account_id, u.person_id from subscription s " +
					"join user u on u.user_id = s.user_id " +
					"join room_member rm on rm.person_id = u.person_id " +
					"where s.is_active = 1 and rm.room_id = ? and s.changed_at >= ?";

	private static final long MILLIS_PER_DAY = 1000L * 60 * 60 * 24;

	private static final Map<String, String> endPoints = new HashMap<String, String>() {{
		put("fcm/com.sap.sports.teamone", "fcm/TeamOne");
		put("aps/com.sap.sports.teamone", "apns/TeamOne");
		put("aps/com.sap.sports.teamone.local", "apns/TeamOneLocal");
		put("aps/com.sap.sports.teamone.release", "apns/TeamOneRelease");
	}};

	private static class Recipient {
		String endPoint;
		String token;
		JSONObject payload;
	}

	@Autowired
	private TenantParameterRepository tenantParameterRepository;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void _pushToSubscribedDevices(Message message) {
		TenantParameter tp = tenantParameterRepository.findById("nhubUrl").orElse(null);
		final String nhubUrl = tp == null ? null : tp.value;
		tp = tenantParameterRepository.findById("nhubAuthHeader").orElse(null);
		final String nhubAuthHeader = tp == null ? null : tp.value;
		if (nhubUrl == null || nhubAuthHeader == null)
			return;
		//
		new Thread() {
			@Override
			public void run() {
				try {
					TenantParameter tp = tenantParameterRepository.findById("horizonDays").orElse(null);
					Long horizonDays = tp == null ? null : Long.getLong(tp.value);
					Long subscriptionHorizon = horizonDays == null ? 0L : System.currentTimeMillis() - horizonDays * MILLIS_PER_DAY;
					List<Recipient> recipients = jdbcTemplate.query(
							SQL_QUERY,
							new Object[]{message.roomId, subscriptionHorizon},
							new RowMapper<Recipient>() {
								@Override
								public Recipient mapRow(ResultSet resultSet, int i) throws SQLException {
									String targetType = resultSet.getString(1);
									String appId = resultSet.getString(2);
									String token = resultSet.getString(3);
									String accountId = resultSet.getString(4);
									String personId = resultSet.getString(5);
									if (targetType == null || appId == null || token == null || accountId == null || personId == null)
										return null;
									//
									String endPoint = endPoints.get(targetType + "/" + appId);
									if (endPoint == null)
										return null;
									//
									Recipient recipient = new Recipient();
									recipient.endPoint = endPoint;
									recipient.token = token;
									if ("fcm".equals(targetType))
										recipient.payload = getFcmPayload(accountId);
									else if ("aps".equals(targetType))
										recipient.payload = getApsPayload(accountId, personId);
									return recipient;
								}
							}
					);
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
						_sendToNotificationHub(nhubUrl + endPoint, nhubAuthHeader, root.toString());
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

			private JSONObject getApsPayload(String accountId, String personId) {
				try {
					String messageText = message.text == null || message.text.length() <= 1600 ? message.text :
							message.text.substring(0, 1600);
					if (personId.equals(message.senderPersonId)) {
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

		}.run();
	}

	private void _sendToNotificationHub(String endPointUrl, String authHeader, String jsonBody) throws Exception {
		URL url = new URL(endPointUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", authHeader);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
			outputStream.flush();
			outputStream.close();
			int responseCode = conn.getResponseCode();
			if (responseCode < 300)
				return;
			System.out.println("NHUB response " + responseCode);
			InputStream errorStream = conn.getErrorStream();
			if (errorStream == null)
				System.out.println("errorStream is null");
			else {
				byte[] serverResponseBody = StreamUtil.readStream(errorStream);
				errorStream.close();
				System.out.println("errorStream: " + new String(serverResponseBody, StandardCharsets.UTF_8));
			}
		}
		finally {
			conn.disconnect();
		}
	}

}
