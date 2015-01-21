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
package org.unitime.timetable.security.evaluation;

import java.io.Serializable;

import org.springframework.security.access.AccessDeniedException;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public interface PermissionCheck {
	
    public void checkPermission(UserContext user, Serializable targetId, String targetType, Right right) throws AccessDeniedException;
    
    public void checkPermission(UserContext user, Object targetObject, Right right) throws AccessDeniedException;
    
    public void checkPermissionAnyAuthority(UserContext user, Serializable targetId, String targetType, Right right, Qualifiable... filter) throws AccessDeniedException;
    
    public void checkPermissionAnyAuthority(UserContext user, Object targetObject, Right right, Qualifiable... filter) throws AccessDeniedException;
    
    public boolean hasPermission(UserContext user, Serializable targetId, String targetType, Right right);
    
    public boolean hasPermission(UserContext user, Object targetObject, Right right);
    
    public boolean hasPermissionAnyAuthority(UserContext user, Serializable targetId, String targetType, Right right, Qualifiable... filter);
    
    public boolean hasPermissionAnyAuthority(UserContext user, Object targetObject, Right right, Qualifiable... filter);

}