/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.spring.security;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.evaluation.PermissionCheck;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("unitimePermissionEvaluator")
public class UniTimePermissionEvaluator implements PermissionEvaluator {
	private static Log sLog = LogFactory.getLog(UniTimePermissionEvaluator.class);
	
	@Autowired
	PermissionCheck unitimePermissionCheck;

	@Override
	public boolean hasPermission(Authentication authentication, Object domainObject, Object permission) {
		try {
			UserContext user = (UserContext)authentication.getPrincipal();
			Right right = (permission instanceof Right ? (Right)permission : Right.valueOf(permission.toString()));
			return unitimePermissionCheck.hasPermission(user, domainObject, right);
		} catch (Exception e) {
			sLog.warn("Failed to evaluate permission " + permission + " for " + domainObject + ": " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
		try {
			UserContext user = (UserContext)authentication.getPrincipal();
			Right right = (permission instanceof Right ? (Right)permission : Right.valueOf(permission.toString()));
			return unitimePermissionCheck.hasPermission(user, targetId, targetType, right);
		} catch (Exception e) {
			sLog.warn("Failed to evaluate permission " + permission + " for " + targetType + "@"+ targetId + ": " + e.getMessage());
			return false;
		}
	}
	
	

}
