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

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * @author Tomas Muller
 */
@Service("unitimePermissionEvaluatorNoException")
public class UniTimePermissionEvaluatorNoException extends UniTimePermissionEvaluator {
	
	@Override
	public boolean hasPermission(Authentication authentication, Object domainObject, Object permission) {
		try {
			return super.hasPermission(authentication, domainObject, permission);
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
		try {
			return super.hasPermission(authentication, targetId, targetType, permission);
		} catch (Exception e) {
			return false;
		}
	}

}
