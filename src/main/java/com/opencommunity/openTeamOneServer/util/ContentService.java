package com.opencommunity.openTeamOneServer.util;

import com.opencommunity.openTeamOneServer.data.*;
import com.opencommunity.openTeamOneServer.persistence.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Iterator;

@Service
public class ContentService {

	private static ContentService instance = null;

	/* public services */

	public static JSONObject exportToJson() throws JSONException {
		if (instance != null)
			return instance._exportToJson();
		return null;
	}

	public static JSONObject getSummary() throws JSONException {
		if (instance != null)
			return instance._getSummary();
		return null;
	}

	public static void importFromJson(JSONObject jsonObject, boolean delete, boolean includeConfiguration, String protectedUserId) throws JSONException {
		if (instance != null)
			instance._importFromJson(jsonObject, delete, includeConfiguration, protectedUserId);
	}

	public static void deleteAll(boolean includeConfiguration, String protectedUserId) throws JSONException {
		if (instance != null)
			instance._deleteAll(includeConfiguration, protectedUserId);
	}

	/* instance methods and properties */

	@Autowired
	private TenantParameterRepository tpr;
	@Autowired
	private UserRepository ur;
	@Autowired
	private PersonRepository pr;
	@Autowired
	private RoomRepository rr;
	@Autowired
	private RoomMemberRepository rmr;
	@Autowired
	private MessageRepository mr;
	@Autowired
	private SymbolicFileRepository sfr;
	@Autowired
	private ViewedConfirmationRepository vcr;
	@Autowired
	private SubscriptionRepository sr;

	public ContentService() {
		instance = this;
	}

	@PostConstruct
	private void init() {
		if (!tpr.findById("name").isPresent())
			tpr.save(new TenantParameter("name", "OpenTeamOne"));
		if (!tpr.findById("pictureId").isPresent())
			tpr.save(new TenantParameter("pictureId", "tenant"));
		if (!tpr.findById("startPageNoLogon").isPresent())
			tpr.save(new TenantParameter("startPageNoLogon", "/default/index.html"));
		if (!tpr.findById("startPageLogon").isPresent())
			tpr.save(new TenantParameter("startPageLogon", "/default/index.html"));
		if (!tpr.findById("startPageAdmin").isPresent())
			tpr.save(new TenantParameter("startPageAdmin", "/default/admin/index.html"));
		if (!tpr.findById("startPageUser").isPresent())
			tpr.save(new TenantParameter("startPageUser", "/default/user/index.html"));
		if (!tpr.findById("dataDirectory").isPresent())
			tpr.save(new TenantParameter("dataDirectory", "/tmp"));
		//
		if (ur.countByHasAdminRoleTrue() == 0)
			ur.save(new User("admin", "admin", null, false, true));
	}

	private JSONObject _getSummary() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("tenantParameters", tpr.count());
		jsonObject.put("users", ur.count());
		jsonObject.put("persons", pr.count());
		jsonObject.put("rooms", rr.count());
		jsonObject.put("roomMembers", rmr.count());
		jsonObject.put("messages", mr.count());
		jsonObject.put("files", sfr.count());
		jsonObject.put("viewedConfirmations", vcr.count());
		jsonObject.put("subscriptions", sr.count());
		return jsonObject;
	}

	private JSONObject _exportToJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("tenantParameters", TenantParameter.toJsonArray(tpr.findAll()));
		jsonObject.put("users", User.toJsonArray(ur.findAll(), true));
		jsonObject.put("persons", Person.toJsonArray(pr.findAll()));
		jsonObject.put("rooms", Room.toJsonArray(rr.findAll()));
		jsonObject.put("roomMembers", RoomMember.toJsonArray(rmr.findAll()));
		jsonObject.put("messages", Message.toJsonArray(mr.findAll()));
		jsonObject.put("files", SymbolicFile.toJsonArray(sfr.findAll()));
		jsonObject.put("viewedConfirmations", ViewedConfirmation.toJsonArray(vcr.findAll()));
		jsonObject.put("subscriptions", Subscription.toJsonArray(sr.findAll()));
		return jsonObject;
	}

	private void _importFromJson(JSONObject jsonObject, boolean delete, boolean includeConfiguration, String protectedUserId) throws JSONException {
		JSONArray item;
		item = JsonUtil.getJSONArray(jsonObject, "tenantParameters");
		if (item != null && includeConfiguration) {
			if (delete)
				tpr.deleteAll();
			tpr.saveAll(TenantParameter.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "users");
		if (item != null && protectedUserId != null) {
			Iterable<User> users;
			if (delete) {
				users = includeConfiguration ? ur.findByUserIdNot(protectedUserId) : ur.findByHasAdminRoleFalseAndUserIdNot(protectedUserId);
				ur.deleteAll(users);
			}
			users = User.fromJsonArray(item);
			Iterator<User> iterator = users.iterator();
			while (iterator.hasNext()) {
				User user = iterator.next();
				if ((!includeConfiguration && user.hasAdminRole) || protectedUserId.equals(user.userId))
					iterator.remove();
			}
			ur.saveAll(users);
		}
		item = JsonUtil.getJSONArray(jsonObject, "persons");
		if (item != null) {
			if (delete)
				pr.deleteAll();
			pr.saveAll(Person.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "rooms");
		if (item != null) {
			if (delete)
				rr.deleteAll();
			rr.saveAll(Room.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "roomMembers");
		if (item != null) {
			if (delete)
				rmr.deleteAll();
			rmr.saveAll(RoomMember.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "messages");
		if (item != null) {
			if (delete)
				mr.deleteAll();
			mr.saveAll(Message.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "files");
		if (item != null) {
			if (delete)
				sfr.deleteAll();
			sfr.saveAll(SymbolicFile.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "viewedConfirmations");
		if (item != null) {
			if (delete)
				vcr.deleteAll();
			vcr.saveAll(ViewedConfirmation.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "subscriptions");
		if (item != null) {
			if (delete)
				sr.deleteAll();
			sr.saveAll(Subscription.fromJsonArray(item));
		}
	}

	private void _deleteAll(boolean includeConfiguration, String protectedUserId) {
		if (includeConfiguration)
			tpr.deleteAll();
		if (protectedUserId != null)
			ur.deleteAll(includeConfiguration ? ur.findByUserIdNot(protectedUserId) : ur.findByHasAdminRoleFalseAndUserIdNot(protectedUserId));
		pr.deleteAll();
		rr.deleteAll();
		rmr.deleteAll();
		mr.deleteAll();
		sfr.deleteAll();
		vcr.deleteAll();
	}

}