package com.opencommunity.openTeamOneServer;

import org.springframework.data.repository.CrudRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;


interface TenantParameterRepository extends CrudRepository<TenantParameter, String> {
	List<TenantParameter> findAll();
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
