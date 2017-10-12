package com.opencommunity.openTeamOneServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


interface TenantParameterRepository extends CrudRepository<TenantParameter, String> {
}

@Entity
public class TenantParameter {
	@Id
	private String key;
	@Column
	private String value;

	public TenantParameter() {
	}

	public TenantParameter(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public TenantParameter(JSONObject item) {
		try {
			key = Util.getString(item, "key");
			value = Util.getString(item, "value");
		} catch (JSONException e) { }
	}

	public JSONObject toJson() throws JSONException {
		JSONObject tenantParameter = new JSONObject();
		tenantParameter.put("key", key);
		tenantParameter.put("value", value);
		return tenantParameter;
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
