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
package org.unitime.timetable.spring.security;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

public class UniTimeSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
	
    @Override
    protected SecurityExpressionRoot createSecurityExpressionRoot(Authentication authentication, MethodInvocation invocation) {
    	MyMethodSecurityExpressionRoot root = new MyMethodSecurityExpressionRoot(authentication);
        root.setThis(invocation.getThis());
        root.setPermissionEvaluator(getPermissionEvaluator());
        
        return root;
    }
    
    class MyMethodSecurityExpressionRoot extends SecurityExpressionRoot {
        private Object filterObject;
        private Object returnObject;
        private Object target;

        MyMethodSecurityExpressionRoot(Authentication a) {
            super(a);
        }

        public void setFilterObject(Object filterObject) {
            this.filterObject = filterObject;
        }

        public Object getFilterObject() {
            return filterObject;
        }

        public void setReturnObject(Object returnObject) {
            this.returnObject = returnObject;
        }

        public Object getReturnObject() {
            return returnObject;
        }

        void setThis(Object target) {
            this.target = target;
        }

        public Object getThis() {
            return target;
        }
        
        public boolean hasPermission(Object permission) {
        	if (getPrincipal() == null || !(getPrincipal() instanceof UserContext)) return false;
        	UserContext user = (UserContext)getPrincipal();
        	if (user.getCurrentAcademicSessionId() == null) return false;
        	return hasPermission(user.getCurrentAcademicSessionId(), Session.class.getName(), permission);
        }
        
        public boolean hasRight(String right) {
        	try {
        		return ((UserContext)getPrincipal()).getCurrentAuthority().hasRight(Right.valueOf(right));
        	} catch (Exception e) {
        		return false;
        	}
        }

        public boolean hasAnyRight(String... right) {
        	try {
        		UserContext user = (UserContext)getPrincipal();
        		for (String r: right)
        			if (user.getCurrentAuthority().hasRight(Right.valueOf(r))) return true;
        		return false;
        	} catch (Exception e) {
        		return false;
        	}
        }
}
}
