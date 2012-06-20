package org.unitime.timetable.spring;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unitime.commons.User;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.util.Constants;

public class LegacyUserContext implements UserContext, UserContext.CanSetCurrentSessionId, UserContext.CanUseChameleon {
	private User iUser = null;
	
	public LegacyUserContext(User user) {
		iUser = user;
	}

	@Override
	public String getExternalUserId() { return iUser.getId(); }

	@Override
	public String getName() { return iUser.getName(); }

	@Override
	public Long getCurrentAcademicSessionId() { return (Long)iUser.getAttribute(Constants.SESSION_ID_ATTR_NAME); }
	
	@Override
	public void setCurrentAcademicSessionId(Long sessionId) { iUser.setAttribute(Constants.SESSION_ID_ATTR_NAME, sessionId);}

	@Override
	public String getCurrentRole() { return iUser.getRole(); }
	
	@Override
	public boolean hasRole(String role) { return iUser.getRoles() != null && iUser.getRoles().contains(role); }

	@Override
	public String getProperty(String key) {
		return UserData.getProperty(getExternalUserId(), key);
	}

	@Override
	public void setProperty(String key, String value) {
		UserData.setProperty(getExternalUserId(), key, value);
	}

	@Override
	public Map<String, String> getProperties() {
		return UserData.getProperties(getExternalUserId());
	}
	
	private Set<Long> iDepartments = null;
	protected Set<Long> getDepartments() {
		if (iDepartments == null) {
			iDepartments = new HashSet<Long>();
			TimetableManager mgr = TimetableManager.findByExternalId(iUser.getId());
			if (mgr != null) {
				for (Department dept: mgr.getDepartments())
					iDepartments.add(dept.getUniqueId());
			}
		}
		return iDepartments;
	}

	@Override
	public boolean hasDepartment(Long departmentId) {
		return getDepartments().contains(departmentId);
	}

	@Override
	public String getOriginalExternalUserId() {
		return (String)iUser.getAttribute("authUserExtId");
	}

}
