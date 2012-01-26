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
package org.unitime.timetable.security.spring;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.permissions.SimpleDepartmentPermission;
import org.unitime.timetable.security.permissions.SimpleSessionPermission;
import org.unitime.timetable.security.rights.Right;

@Service("unitimePermissionEvaluator")
public class UniTimePermissionEvaluator implements PermissionEvaluator {
	private static Log sLog = LogFactory.getLog(UniTimePermissionEvaluator.class);
	
	@Autowired
	SimpleDepartmentPermission permissionDepartment;
	
	@Autowired
	SimpleSessionPermission permissionSession;
	
	@Autowired
	ApplicationContext applicationContext;

	@Override
	public boolean hasPermission(Authentication authentication, Object domainObject, Object permission) {
		if (domainObject != null && domainObject instanceof Collection) {
			for (Object o: (Collection<?>)domainObject) {
				if (!hasPermission(authentication, o, permission)) return false;
			}
			return true;
		}
		try {
			sLog.info("Checking " + permission + " for " + domainObject);
			if (!authentication.isAuthenticated()) {
				sLog.info("   ... not authenticated");
				return false;
			}
			if (authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof UniTimeUser)) {
				sLog.info("   ... bad principal");
				return false;
			}
			Right right = (permission instanceof Right ? (Right)permission : Right.valueOf(permission.toString()));
			if (right == null) {
				sLog.info("   ... unknown permission " + permission);
				return false;
			}
			UniTimeUser user = (UniTimeUser)authentication.getPrincipal();
			sLog.info("   ... user: " + user.getName() + " " + authentication.getAuthorities());
			if (!user.hasRole()) {
				sLog.info("   ... user has no role");
				return false;
			}
			Exception ex = null;
			try {
				Permission<?> perm = (Permission<?>)applicationContext.getBean("permission" + permission);
				if (perm != null) {
					if ((Boolean)perm.getClass().getMethod("check", UniTimeUser.class, domainObject.getClass()).invoke(perm, user, domainObject)) {
						return true;
					} else {
						sLog.info("   ... permission check failed");
						return false;
					}
				}
			} catch (Exception e) {
				ex = e;
			}
			if (domainObject instanceof Session) {
				if (permissionSession.check(user, (Session)domainObject)) {
					return true;
				} else {
					sLog.info("   ... session check failed");
					return false;
				}
			} else if (domainObject instanceof Department) {
				if (permissionDepartment.check(user, (Department)domainObject)) {
					return true;
				} else {
					sLog.info("   ... session check failed");
					return false;
				}
			} else {
				sLog.info("   ... permission check failed (" + (ex == null ? "no check found" : ex.getMessage() + ")"));
				ex.printStackTrace();
				return false;
			}
		} catch (Exception e) {
			sLog.warn("Failed to evaluate permission " + permission + " for " + domainObject + ": " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
		if (targetId != null && targetId instanceof Collection) {
			for (Serializable id: (Collection<Serializable>)targetId) {
				if (!hasPermission(authentication, id, targetType, permission)) return false;
			}
			return true;
		}
		try {
			sLog.info("Checking " + permission + " for " + targetType + "@" + targetId);
			if (targetId == null) {
				sLog.info("  ... no id");
				return false;
			}
			if (!(targetId instanceof Long)) {
				try {
					targetId = (Serializable)targetId.getClass().getMethod("getUniqueId").invoke(targetId);
				} catch (Exception e) {}
				try {
					targetId = (Serializable)targetId.getClass().getMethod("getId").invoke(targetId);
				} catch (Exception e) {}
			}
			String className = targetType;
			if (className.indexOf('.') < 0) className = "org.unitime.timetable.model." + className;
			return hasPermission(authentication, new _RootDAO().getSession().get(Class.forName(className), targetId), permission);
		} catch (Exception e) {
			sLog.warn("Failed to evaluate permission " + permission + " for " + targetType + "@ "+ targetId + ": " + e.getMessage());
			return false;
		}
	}
	
	

}
