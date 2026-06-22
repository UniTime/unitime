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
package org.unitime.timetable.server;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.client.admin.ChameleonPage.ChameleonRequest;
import org.unitime.timetable.gwt.client.admin.ChameleonPage.ChameleonResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.ChameleonUserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

@GwtRpcImplements(ChameleonRequest.class)
public class ChameleonBackend implements GwtRpcImplementation<ChameleonRequest, ChameleonResponse> {

	@Override
	public ChameleonResponse execute(ChameleonRequest request, SessionContext context) {
    	ChameleonResponse response = new ChameleonResponse();

    	UserContext user = context.getUser();
    	if (user != null && user instanceof UserContext.Chameleon) {
    		user = ((UserContext.Chameleon)user).getOriginalUserContext();
    		response.setExternalId(user.getExternalUserId());
    	} else
    		context.checkPermission(Right.Chameleon);

    	response.setCanLookup(context.hasPermission(Right.HasRole) && ApplicationProperty.ChameleonAllowLookup.isTrue());
    	
    	if (request.hasExternalId() && request.hasName()) {
    		doSwitch(context, request.getExternalId(), request.getName());
    	} else {
        	NameFormat nf = NameFormat.LAST_FIRST_MIDDLE_TITLE;
        	if (context.hasPermission(Right.EnrollmentsShowExternalId))
        		nf = NameFormat.LAST_FIRST_MIDDLE_TITLE_EXTERNAL;
    		for (TimetableManager m: TimetableManager.getManagerList()) {
    			response.addManager(m.getExternalUniqueId(), nf.format(m));
    		}
    	}
    	
		return response;
	}
	
	private void doSwitch(SessionContext context, String  externalId, String name) {
    	
		for (SessionAttribute a: SessionAttribute.values())
			context.removeAttribute(a.key());
    	
		UserContext user = context.getUser();
    	if (user instanceof UserContext.Chameleon)
    		user = ((UserContext.Chameleon)user).getOriginalUserContext();
    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if (authentication instanceof ChameleonAuthentication)
    		authentication = ((ChameleonAuthentication)authentication).getOriginalAuthentication();
    	
    	if (user.getExternalUserId().equals(externalId)) {
    		SecurityContextHolder.getContext().setAuthentication(authentication);
    	} else {
    		SecurityContextHolder.getContext().setAuthentication(
        			new ChameleonAuthentication(
        					authentication, new ChameleonUserContext(externalId, name, user)
        			));
    	}
    }
    
    public static class ChameleonAuthentication implements Authentication {
    	private static final long serialVersionUID = 1L;
		private Authentication iOriginalAuthentication;
    	private UserContext iUserContext;
    	
    	public ChameleonAuthentication(Authentication authentication, UserContext user) {
    		iOriginalAuthentication = authentication; iUserContext = user;
    		if (iOriginalAuthentication instanceof ChameleonAuthentication)
    			iOriginalAuthentication = ((ChameleonAuthentication)iOriginalAuthentication).getOriginalAuthentication();
    	}
    	
    	public Authentication getOriginalAuthentication() { return iOriginalAuthentication; }

		@Override
		public String getName() { return iUserContext.getName(); }

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return iUserContext.getAuthorities();
		}

		@Override
		public Object getCredentials() {
			return iOriginalAuthentication.getCredentials();
		}

		@Override
		public Object getDetails() {
			return iOriginalAuthentication.getDetails();
		}

		@Override
		public Object getPrincipal() {
			return iUserContext;
		}

		@Override
		public boolean isAuthenticated() {
			return iOriginalAuthentication.isAuthenticated();
		}

		@Override
		public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
			iOriginalAuthentication.setAuthenticated(isAuthenticated);
		}
    	
    }

}
