package com.opencommunity.openTeamOneServer;

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
	private AttachmentRepository ar;
	@Autowired
	private ViewedConfirmationRepository vcr;
	@Autowired
	private SubscriptionLogRepository slr;

	public ContentService() {
		instance = this;
	}

	@PostConstruct
	private void init() {
		if (tpr.findOne("name") == null)
			tpr.save(new TenantParameter("name", "OpenTeamOne"));
		if (tpr.findOne("pictureId") == null)
			tpr.save(new TenantParameter("pictureId", "tenant"));
		if (tpr.findOne("startPageNoLogon") == null)
			tpr.save(new TenantParameter("startPageNoLogon", "/admin/default/index.html"));
		if (tpr.findOne("startPageNoAdmin") == null)
			tpr.save(new TenantParameter("startPageNoAdmin", "/admin/default/index.html"));
		if (tpr.findOne("startPageAdmin") == null)
			tpr.save(new TenantParameter("startPageAdmin", "/admin/default/index.html"));
		if (tpr.findOne("dataDirectory") == null)
			tpr.save(new TenantParameter("dataDirectory", "/tmp"));
		//
		if (ur.countByHasAdminRoleTrue() == 0)
			ur.save(new User("admin", "admin", null, false, true));
	}

	private JSONObject _exportToJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("tenantParameters", TenantParameter.toJsonArray(tpr.findAll()));
		jsonObject.put("users", User.toJsonArray(ur.findAll()));
		jsonObject.put("persons", Person.toJsonArray(pr.findAll()));
		jsonObject.put("rooms", Room.toJsonArray(rr.findAll()));
		jsonObject.put("roomMembers", RoomMember.toJsonArray(rmr.findAll()));
		jsonObject.put("messages", Message.toJsonArray(mr.findAll()));
		jsonObject.put("attachments", Attachment.toJsonArray(ar.findAll()));
		jsonObject.put("viewedConfirmations", ViewedConfirmation.toJsonArray(vcr.findAll()));
		jsonObject.put("subscriptionLogs", SubscriptionLog.toJsonArray(slr.findAll()));
		return jsonObject;
	}

	private void _importFromJson(JSONObject jsonObject, boolean delete, boolean includeConfiguration, String protectedUserId) throws JSONException {
		JSONArray item;
		item = JsonUtil.getJSONArray(jsonObject, "tenantParameters");
		if (item != null && includeConfiguration) {
			if (delete)
				tpr.deleteAll();
			tpr.save(TenantParameter.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "users");
		if (item != null && protectedUserId != null) {
			Iterable<User> users;
			if (delete) {
				users = includeConfiguration ? ur.findByUserIdNot(protectedUserId) : ur.findByHasAdminRoleFalseAndUserIdNot(protectedUserId);
				ur.delete(users);
			}
			users = User.fromJsonArray(item);
			Iterator<User> iterator = users.iterator();
			while (iterator.hasNext()) {
				User user = iterator.next();
				if ((!includeConfiguration && user.hasAdminRole) || protectedUserId.equals(user.userId))
					iterator.remove();
			}
			ur.save(users);
		}
		item = JsonUtil.getJSONArray(jsonObject, "persons");
		if (item != null) {
			if (delete)
				pr.deleteAll();
			pr.save(Person.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "rooms");
		if (item != null) {
			if (delete)
				rr.deleteAll();
			rr.save(Room.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "roomMembers");
		if (item != null) {
			if (delete)
				rmr.deleteAll();
			rmr.save(RoomMember.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "messages");
		if (item != null) {
			if (delete)
				mr.deleteAll();
			mr.save(Message.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "attachments");
		if (item != null) {
			if (delete)
				ar.deleteAll();
			ar.save(Attachment.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "viewedConfirmations");
		if (item != null) {
			if (delete)
				vcr.deleteAll();
			vcr.save(ViewedConfirmation.fromJsonArray(item));
		}
		item = JsonUtil.getJSONArray(jsonObject, "subscriptionLogs");
		if (item != null) {
			if (delete)
				slr.deleteAll();
			slr.save(SubscriptionLog.fromJsonArray(item));
		}
	}

	private void _deleteAll(boolean includeConfiguration, String protectedUserId) {
		if (includeConfiguration)
			tpr.deleteAll();
		if (protectedUserId != null)
			ur.delete(includeConfiguration ? ur.findByUserIdNot(protectedUserId) : ur.findByHasAdminRoleFalseAndUserIdNot(protectedUserId));
		pr.deleteAll();
		rr.deleteAll();
		rmr.deleteAll();
		mr.deleteAll();
		ar.deleteAll();
		vcr.deleteAll();
	}

}
