package com.opencommunity.openTeamOneServer.data;

import com.opencommunity.openTeamOneServer.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

@Entity
public class TenantParameter {

	@Id
	@Column
	public Integer nameHash;

	@Column(length = 16)
	public String name;
	@Column(length = 64)
	public String value;

	public TenantParameter() { }

	public TenantParameter(String name, String value) {
		this.name = name;
		this.value = value;
		normalize();
	}

	public TenantParameter(JSONObject item) throws JSONException {
		name = JsonUtil.getString(item, "name");
		value = JsonUtil.getString(item, "value");
		normalize();
	}

	public void normalize() {
		if (name != null) nameHash = name.hashCode();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject item = new JSONObject();
		item.put("name", name);
		item.put("value", value);
		return item;
	}

	public static Iterable<TenantParameter> fromJsonArray(JSONArray array) throws JSONException {
		if (array == null)
			return null;
		ArrayList<TenantParameter> tenantParameterList = new ArrayList<>();
		for (int i = 0; i < array.length(); i++)
			tenantParameterList.add(new TenantParameter(array.getJSONObject(i)));
		return tenantParameterList;
	}

	public static JSONArray toJsonArray(Iterable<TenantParameter> tenantParameters) throws JSONException {
		JSONArray array = new JSONArray();
		for (TenantParameter tenantParameter : tenantParameters)
			array.put(tenantParameter.toJson());
		return array;
	}

	public Integer getNameHash() {
		return nameHash;
	}

	public void setNameHash(Integer nameHash) {
		this.nameHash = nameHash;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		String output = getClass().getSimpleName();
		try {
			output += toJson().toString();
		} catch (JSONException e) { }
		return output;
	}

}
