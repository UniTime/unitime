package org.unitime.timetable.spring;

import java.util.HashMap;
import java.util.Map;

public class SimpleUserContext implements UserContext {
	private String iId, iName, iRole;
	private Long iSessionId;
	private Map<String, String> iProperties = new HashMap<String, String>();

	@Override
	public String getExternalUserId() { return iId; }
	
	public void setExternalUsetId(String id) { iId = id; }

	@Override
	public String getName() { return iName; }
	
	public void setName(String name) { iName = name; }

	@Override
	public Long getCurrentAcademicSessionId() { return iSessionId; }
	
	public void setCurrentAcademicSessionId(Long sessioId) { iSessionId = sessioId; }

	@Override
	public String getCurrentRole() { return iRole; }
	
	public void setCurrentRole(String role) { iRole = role; }

	@Override
	public boolean hasRole(String role) { return role == null ? iRole == null : role.equals(iRole); }

	@Override
	public String getProperty(String key) { return iProperties.get(key); }

	@Override
	public void setProperty(String key, String value) {
		if (value == null)
			iProperties.remove(key);
		else
			iProperties.put(key, value);
	}

	@Override
	public Map<String, String> getProperties() { return iProperties; }

	@Override
	public boolean hasDepartment(Long departmentId) { return false; }
}
