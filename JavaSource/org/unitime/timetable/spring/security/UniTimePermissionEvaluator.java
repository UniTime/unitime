/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
