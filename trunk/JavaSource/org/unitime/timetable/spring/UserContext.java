package org.unitime.timetable.spring;

import java.util.Map;

public interface UserContext {
	
	public String getExternalUserId();
	
	public String getName();
	
	public Long getCurrentAcademicSessionId();
	
	public String getCurrentRole();
	
	public boolean hasDepartment(Long departmentId);
	
	public boolean hasRole(String role);
	
	public String getProperty(String key);
	
	public void setProperty(String key, String value);
	
	public Map<String, String> getProperties();

	
	public static interface CanSetCurrentSessionId {
		public void setCurrentAcademicSessionId(Long sessionId);
	}
	
	public static interface CanUseChameleon {
		public String getOriginalExternalUserId();
	}
}
