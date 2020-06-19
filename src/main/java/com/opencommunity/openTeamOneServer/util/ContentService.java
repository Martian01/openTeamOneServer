package com.opencommunity.openTeamOneServer.util;

import com.opencommunity.openTeamOneServer.data.*;
import com.opencommunity.openTeamOneServer.persistence.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Iterator;

@Component
public class ContentService {

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

	@PostConstruct
	public void init() {
		if (tpr.findTopByName("tenantName") == null)
			tpr.save(new TenantParameter("tenantName", "Open Team One"));
		if (tpr.findTopByName("startPageNoLogon") == null)
			tpr.save(new TenantParameter("startPageNoLogon", "/default/index.html"));
		if (tpr.findTopByName("startPageLogon") == null)
			tpr.save(new TenantParameter("startPageLogon", "/default/index.html"));
		if (tpr.findTopByName("startPageAdmin") == null)
			tpr.save(new TenantParameter("startPageAdmin", "/default/admin/index.html"));
		if (tpr.findTopByName("startPageUser") == null)
			tpr.save(new TenantParameter("startPageUser", "/default/user/index.html"));
		if (tpr.findTopByName("dataDirectory") == null)
			tpr.save(new TenantParameter("dataDirectory", "/opt/openTeamOneServer/data"));
		prepareDataDirectory("/opt/openTeamOneServer/data");
		//
		if (ur.countByHasAdminRoleTrue() == 0)
			ur.save(new User("admin", "admin", null, false, true));
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void prepareDataDirectory(String dirName) {
		File dataDir = new File(dirName);
		boolean rc = dataDir.mkdirs();
		File subDir = new File(dataDir, "attachments");
		rc = subDir.mkdirs();
		subDir = new File(dataDir, "profiles");
		rc = subDir.mkdirs();
		subDir = new File(dataDir, "snapshots");
		rc = subDir.mkdirs();
	}

	public JSONObject getSummary() throws JSONException {
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

	public JSONObject exportToJson() throws JSONException {
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

	public void importFromJson(JSONObject jsonObject, boolean delete, boolean includeConfiguration, String protectedUserId) throws JSONException {
		JSONArray item;
		item = JsonUtil.getJSONArray(jsonObject, "tenantParameters");
		if (item != null && includeConfiguration) {
			if (delete)
				tpr.deleteAll();
			tpr.saveAll(TenantParameter.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "forcedParameters");
		if (item != null) {
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

	public void deleteAll(boolean includeConfiguration, String protectedUserId) {
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
