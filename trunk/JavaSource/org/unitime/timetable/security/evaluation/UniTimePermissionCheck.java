/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.security.evaluation;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.permissions.Permission.PermissionDepartment;
import org.unitime.timetable.security.permissions.Permission.PermissionSession;
import org.unitime.timetable.security.rights.Right;

@Service("unitimePermissionCheck")
public class UniTimePermissionCheck implements PermissionCheck, InitializingBean {
	private static Log sLog = LogFactory.getLog(UniTimePermissionCheck.class);
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	PermissionDepartment permissionDepartment;
	
	@Autowired
	PermissionSession permissionSession;
	
	@Override
    public boolean checkPermission(UserContext user, Serializable targetId, String targetType, Right right) {
		if (targetType == null && right != null && right.hasType())
			targetType = right.type().getSimpleName();
		
		if (targetType == null)
			return checkPermission(user, null, right);
		
		if (targetId instanceof Collection) {
			for (Serializable id: (Collection<Serializable>)targetId) {
				if (!checkPermission(user, id, targetType, right)) return false;
			}
			return true;
		}
		
		try {
			sLog.info("Checking " + right + " for " + targetType + "@" + targetId);
			String className = targetType;
			if (className.indexOf('.') < 0) className = "org.unitime.timetable.model." + className;

			// Special cases
			
			if (targetId == null && Session.class.getName().equals(className))
				targetId = user.getCurrentAcademicSessionId();
			
			if (targetId == null && Department.class.getName().equals(className)) {
				
				if (user.getCurrentAuthority() == null) {
					sLog.info("   ... no role");
					return false;
				}
				
				for (Department d: Department.getUserDepartments(user))
					if (checkPermission(user, d, right)) return true;
				
				return false;
			}
			
			if (targetId == null && SubjectArea.class.getName().equals(className)) {
				
				if (user.getCurrentAuthority() == null) {
					sLog.info("   ... no role");
					return false;
				}
				
				for (SubjectArea sa: SubjectArea.getUserSubjectAreas(user))
					if (checkPermission(user, sa, right)) return true;
				
				return false;
			}
			
			if (targetId == null) {
				sLog.info("   ... no id");
				return false;
			}
			
			if (targetId instanceof String && Department.class.getName().equals(className)) {
				Department dept = Department.findByDeptCode((String)targetId, user.getCurrentAcademicSessionId());
				if (dept != null) 
					return checkPermission(user, dept, right);
				else
					return false;
			}
			
			if (targetId instanceof String) {
				try {
					targetId = Long.valueOf((String)targetId);
				} catch (NumberFormatException e) {}
			}
			if (!(targetId instanceof Long)) {
				try {
					targetId = (Serializable)targetId.getClass().getMethod("getUniqueId").invoke(targetId);
				} catch (Exception e) {}
				try {
					targetId = (Serializable)targetId.getClass().getMethod("getId").invoke(targetId);
				} catch (Exception e) {}
			}
			
			Object domainObject = new _RootDAO().getSession().get(Class.forName(className), targetId);
			if (domainObject == null) {
				sLog.info("   ... no match");
				return false;
			} else { 
				return checkPermission(user, domainObject, right);
			}
		} catch (Exception e) {
			sLog.warn("Failed to evaluate permission " + right + " for " + targetType + "@ "+ targetId + ": " + e.getMessage());
			return false;
		}
	}
    
	@Override
    public boolean checkPermission(UserContext user, Object domainObject, Right right) {

		if (domainObject != null && domainObject instanceof Collection) {
			for (Object o: (Collection<?>)domainObject) {
				if (!checkPermission(user, o, right)) return false;
			}
			return true;
		}
		
		
		sLog.info("Checking " + right + " for " + domainObject);
		if (user == null) {
			sLog.info("   ... not authenticated");
			return false;
		}
		
		if (user.getCurrentAuthority() == null) {
			sLog.info("   ... no role");
			return false;
		}
		
		if (right == null) {
			sLog.info("   ... no right");
			return false;
		}
		
		if (!user.getCurrentAuthority().hasRight(right)) {
			sLog.info("   ... right check failed");
			return false;
		}

		if (domainObject == null) {
			sLog.info("   ... no object");
			return true;
		}
		
		if (right.hasType() && !right.type().isInstance(domainObject)) {
			sLog.warn("   ... wrong domain type (" + domainObject.getClass().getSimpleName() + " != " + right.type().getSimpleName() + ")");
		}
		
		sLog.info("   ... user: " + user.getName() + " " + user.getCurrentAuthority());
		
		try {
			Permission<?> perm = (Permission<?>)applicationContext.getBean("permission" + right.name(), Permission.class);
			if (perm != null && perm.type().isInstance(domainObject)) {
				if ((Boolean)perm.getClass().getMethod("check", UserContext.class, perm.type()).invoke(perm, user, domainObject)) {
					return true;
				} else {
					sLog.info("   ... permission " + right + " check failed");
					return false;
				}
			} else if (perm == null) {
				sLog.info("   ... permission " + right + " not found");
			} else {
				sLog.info("   ... permission " + right + " has different type (" + perm.type().getSimpleName() + " != " + domainObject.getClass().getSimpleName() + ")");
			}
		} catch (BeansException e) {
		} catch (Exception e) {
			sLog.warn("   ... permission " + right + " check failed: " + e.getMessage(), e);
			return false;
		}
		
		if (domainObject instanceof Session) {
			if (permissionSession.check(user, (Session)domainObject)) {
				return true;
			} else {
				sLog.info("   ... session check failed");
				return false;
			}
		}
		
		if (domainObject instanceof Department) {
			if (permissionDepartment.check(user, (Department)domainObject)) {
				return true;
			} else {
				sLog.info("   ... session check failed");
				return false;
			}
		}
		
		return false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (Right r: Right.values()) {
			try {
				if (r.hasType()) {
					try {
						Permission<?> p = (Permission<?>)applicationContext.getBean("permission" + r.name());
						if (p == null) {
							sLog.warn("No permission found for " + r + " (" + r.type().getSimpleName() + ").");
						} else if (!r.type().equals(p.type())) {
							sLog.warn("Permission " + r + " (" + r.type().getSimpleName() + ") has a wrong type (" + p.type().getSimpleName() + ").");
						}
					} catch (BeansException e) {
						sLog.warn("Failed to find a permission " + r + " (" + r.type().getSimpleName() + "): " + e.getMessage());
					}
				}
			} catch (Exception e) {
				sLog.error("Failed to check permission " + r + " (" + r.type().getSimpleName() + "): " + e.getMessage(), e);
			}
		}
	}

}
