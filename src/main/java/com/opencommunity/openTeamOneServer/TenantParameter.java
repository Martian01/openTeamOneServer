package com.opencommunity.openTeamOneServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;


interface TenantParameterRepository extends CrudRepository<TenantParameter, String> {
}

@Entity
public class TenantParameter {
	@Id
	public String key;
	@Column
	public String value;

	public TenantParameter() {
	}

	public TenantParameter(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public TenantParameter(JSONObject item) {
		try {
			key = JsonUtil.getString(item, "key");
			value = JsonUtil.getString(item, "value");
		} catch (JSONException e) { }
	}

	public JSONObject toJson() throws JSONException {
		JSONObject tenantParameter = new JSONObject();
		tenantParameter.put("key", key);
		tenantParameter.put("value", value);
		return tenantParameter;
	}

	public static Iterable<TenantParameter> fromJsonList(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<TenantParameter> tenantParameterList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			tenantParameterList.add(new TenantParameter(array.getJSONObject(i)));
		return tenantParameterList;
	}

	public static JSONArray toJsonList(Iterable<TenantParameter> tenantParameters) throws JSONException {
		JSONArray array = new JSONArray();
		for (TenantParameter tenantParameter : tenantParameters)
			array.put(tenantParameter.toJson());
		return array;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "TenantParameter{" +
				"key='" + key + '\'' +
				", value='" + value + '\'' +
				'}';
	}
}
