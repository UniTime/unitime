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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class UniTimeSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
	
    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, MethodInvocation invocation) {
    	MyMethodSecurityExpressionRoot root = new MyMethodSecurityExpressionRoot(authentication);
        root.setThis(invocation.getThis());
        root.setPermissionEvaluator(getPermissionEvaluator());
        
        return root;
    }
    
    class MyMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
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
        	try {
        		return super.hasPermission(null, null, permission);
        	} catch (AccessDeniedException e) {
        		return false;
        	}
        }
        
        public boolean checkPermission(Object permission) {
        	return super.hasPermission(null, null, permission);
        }
        
        @Override
        public boolean hasPermission(Object targetId, String targetType, Object permission) {
        	try {
        		return super.hasPermission(targetId, targetType, permission);
        	} catch (AccessDeniedException e) {
        		return false;
        	}
        }
        
        public boolean checkPermission(Object targetId, String targetType, Object permission) {
        	return super.hasPermission(targetId, targetType, permission);
        }
        
        @Override
        public boolean hasPermission(Object target, Object permission) {
        	try {
        		return super.hasPermission(target, permission);
        	} catch (AccessDeniedException e) {
        		return false;
        	}
        }
        
        public boolean checkPermission(Object target, Object permission) {
        	return super.hasPermission(target, permission);
        }
    }
}
