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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.SecurityMessages;
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
	protected static SecurityMessages MSG = Localization.create(SecurityMessages.class); 
	private static Log sLog = LogFactory.getLog(UniTimePermissionCheck.class);
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	PermissionDepartment permissionDepartment;
	
	@Autowired
	PermissionSession permissionSession;
	
	@Override
    public void checkPermission(UserContext user, Serializable targetId, String targetType, Right right) throws AccessDeniedException {
		if (user == null)
			throw new AccessDeniedException(MSG.noAuthentication(right == null ? "NULL" : right.toString()));

		if (user.getCurrentAuthority() == null)
			throw new AccessDeniedException(MSG.noAuthority(right == null ? "NULL" : right.toString()));
		
		if (right == null)
			throw new AccessDeniedException(MSG.noRight());

		if (!user.getCurrentAuthority().hasRight(right))
			throw new AccessDeniedException(MSG.missingRight(right.toString()));

		if (targetType == null && right.hasType())
			targetType = right.type().getSimpleName();
		
		if (targetType == null) return;
		
		if (targetId != null && targetId instanceof Collection) {
			for (Serializable id: (Collection<Serializable>) targetId)
				checkPermission(user, id, targetType, right);
			return;
		}
		
		if (targetId != null && targetId.getClass().isArray()) {
			for (Serializable id: (Serializable[])targetId)
				checkPermission(user, id, targetType, right);
			return;
		}
		
		try {
			String className = targetType;
			if (className.indexOf('.') < 0) className = "org.unitime.timetable.model." + className;

			// Special cases
			
			if (targetId == null && Session.class.getName().equals(className))
				targetId = user.getCurrentAcademicSessionId();
			
			if (targetId == null && Department.class.getName().equals(className)) {
				
				AccessDeniedException firstDenial = null;
				for (Department d: Department.getUserDepartments(user)) {
					try {
						checkPermission(user, d, right);
						return;
					} catch (AccessDeniedException e) {
						if (firstDenial == null) firstDenial = e;
					}
				}
				
				if (firstDenial != null) throw firstDenial;
				throw new AccessDeniedException(MSG.noDepartment(right.toString()));
				
			}
			
			if (targetId == null && SubjectArea.class.getName().equals(className)) {
				
				AccessDeniedException firstDenial = null;
				for (SubjectArea sa: SubjectArea.getUserSubjectAreas(user)) {
					try {
						checkPermission(user, sa, right);
						return;
					} catch (AccessDeniedException e) {
						if (firstDenial == null) firstDenial = e;
					}
				}
				
				if (firstDenial != null) throw firstDenial;
				throw new AccessDeniedException(MSG.noSubject(right.toString()));
			}
			
			if (targetId == null)
				throw new AccessDeniedException(MSG.noDomainObject(right.toString(), targetType));
			
			if (targetId instanceof String && Department.class.getName().equals(className)) {
				Department dept = Department.findByDeptCode((String)targetId, user.getCurrentAcademicSessionId());
				if (dept != null) {
					checkPermission(user, dept, right);
					return;
				}
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
			if (domainObject == null)
				throw new AccessDeniedException(MSG.domainObjectNotExists(right.toString(), targetType));
			checkPermission(user, domainObject, right);
		} catch (AccessDeniedException e) {
			throw e;
		} catch (Exception e) {
			throw new AccessDeniedException(MSG.permissionCheckFailedException(right.toString(), targetType, e.getMessage()));
		}
	}
    
	@Override
    public void checkPermission(UserContext user, Object domainObject, Right right) throws AccessDeniedException {
		if (user == null)
			throw new AccessDeniedException(MSG.noAuthentication(right == null ? "NULL" : right.toString()));

		if (user.getCurrentAuthority() == null)
			throw new AccessDeniedException(MSG.noAuthority(right == null ? "NULL" : right.toString()));
		
		if (right == null)
			throw new AccessDeniedException(MSG.noRight());

		if (!user.getCurrentAuthority().hasRight(right))
			throw new AccessDeniedException(MSG.missingRight(right.toString()));
		
		if (domainObject == null)
			return;

		if (domainObject instanceof Collection) {
			for (Object o: (Collection<?>) domainObject)
				checkPermission(user, o, right);
			return;
		}
		
		if (domainObject.getClass().isArray()) {
			for (Object o: (Object[]) domainObject)
				checkPermission(user, o, right);
			return;
		}

		if (right.hasType() && !right.type().isInstance(domainObject))
			throw new AccessDeniedException(MSG.wrongDomainObject(right.toString(), domainObject.getClass().getSimpleName(), right.type().getSimpleName()));
		
		try {
			Permission<?> perm = (Permission<?>)applicationContext.getBean("permission" + right.name(), Permission.class);
			if (perm != null && perm.type().isInstance(domainObject)) {
				if ((Boolean)perm.getClass().getMethod("check", UserContext.class, perm.type()).invoke(perm, user, domainObject)) {
					return;
				} else {
					throw new AccessDeniedException(MSG.permissionCheckFailed(right.toString(), domainObject.toString()));
				}
			}
		} catch (BeansException e) {
		} catch (AccessDeniedException e) {
			throw e;
		} catch (Exception e) {
			throw new AccessDeniedException(MSG.permissionCheckFailedException(right.toString(), domainObject.toString(), e.getMessage()));
		}
		
		if (domainObject instanceof Session) {
			if (permissionSession.check(user, (Session)domainObject)) {
				return;
			} else {
				throw new AccessDeniedException(MSG.sessionCheckFailed(right.toString(), domainObject.toString()));
			}
		}
		
		if (domainObject instanceof Department) {
			if (permissionDepartment.check(user, (Department)domainObject)) {
				return;
			} else {
				throw new AccessDeniedException(MSG.departmentCheckFailed(right.toString(), domainObject.toString()));
			}
		}
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
