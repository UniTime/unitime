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

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

/**
 * @author Tomas Muller
 */
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
