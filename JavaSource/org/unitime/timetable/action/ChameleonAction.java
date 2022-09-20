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
package org.unitime.timetable.action;

import java.util.Collection;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ChameleonForm;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.ChameleonUserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;


/** 
 * @author Tomas Muller
 */
@Service("/chameleon")
@Action(value = "chameleon", results = {
		@Result(name = "displayForm", type = "tiles", location = "chameleon.tiles"),
		@Result(name = "reload", type = "redirect", location = "/selectPrimaryRole.action")
	})
@TilesDefinition(name = "chameleon.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Chameleon"),
		@TilesPutAttribute(name = "body", value = "/admin/chameleon.jsp"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	})
public class ChameleonAction extends UniTimeAction<ChameleonForm> {
	private static final long serialVersionUID = -8274614976659449939L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String uid, uname;
	public String getUid() { return uid; }
	public void setUid(String uid) { this.uid = uid; }
	public String getUname() { return uname; }
	public void setUname(String uname) { this.uname = uname; }

    public String execute() throws Exception {
    	if (form == null) {
    		form = new ChameleonForm();
    		form.reset();
    	}
    	
    	UserContext user = sessionContext.getUser();
    	if (user != null && user instanceof UserContext.Chameleon)
    		user = ((UserContext.Chameleon)user).getOriginalUserContext();
    	else
    		sessionContext.checkPermission(Right.Chameleon);
    	    	
        form.setCanLookup(sessionContext.hasPermission(Right.HasRole));

        if (op == null) op = form.getOp();
        else form.setOp(op);

		// Lookup
		if (uid != null && !uid.isEmpty() && ApplicationProperty.ChameleonAllowLookup.isTrue()) {
			form.setPuid(uid);
			form.setName(uname);
			op = MSG.actionChangeUser();
		}
        
		// First Access - display blank form
        if (op == null || op.isEmpty()) {
            if (user != null)
            	form.setPuid(user.getExternalUserId());
        }
		
        // Change User
        if (MSG.actionChangeUser().equals(op)) {
        	if (form.getPuid() == null || form.getPuid().isEmpty()) {
        		addActionError(MSG.warnNoUser());
        	} else {
                try {
                    doSwitch(user);
                    return "reload";
    			} catch(Exception e) {
    				Debug.error(e);
    				addActionError(e.getMessage());
    			}
        	}
        }
		
        LookupTables.setupTimetableManagers(request);
        return "displayForm";
    }

    /**
     * Reads in new user attributes and reloads Timetabling for the new user
     */
    private void doSwitch(UserContext user) throws Exception {
    	
		for (SessionAttribute a: SessionAttribute.values())
			request.getSession().removeAttribute(a.key());
    	
    	if (user instanceof UserContext.Chameleon)
    		user = ((UserContext.Chameleon)user).getOriginalUserContext();
    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if (authentication instanceof ChameleonAuthentication)
    		authentication = ((ChameleonAuthentication)authentication).getOriginalAuthentication();
    	
    	if (user.getExternalUserId().equals(form.getPuid())) {
    		SecurityContextHolder.getContext().setAuthentication(authentication);
    	} else {
    		SecurityContextHolder.getContext().setAuthentication(
        			new ChameleonAuthentication(
        					authentication, new ChameleonUserContext(form.getPuid(), form.getName(), user)
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
