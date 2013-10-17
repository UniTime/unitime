/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.action;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ChameleonForm;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.ChameleonUserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 10-23-2006
 * 
 * XDoclet definition:
 * @struts:action path="/chameleon" name="chameleonForm" input="/admin/chameleon.jsp" scope="request"
 *
 * @author Tomas Muller
 */
@Service("/chameleon")
public class ChameleonAction extends Action {

    // --------------------------------------------------------- Instance Variables

    // --------------------------------------------------------- Methods
	
	@Autowired SessionContext sessionContext;

    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {
    	
    	UserContext user = sessionContext.getUser();
    	if (user != null && user instanceof UserContext.Chameleon)
    		user = ((UserContext.Chameleon)user).getOriginalUserContext();
    	else
    		sessionContext.checkPermission(Right.Chameleon);
    	    	
        MessageResources rsc = getResources(request);
        
        ChameleonForm frm = (ChameleonForm) form;
		ActionMessages errors = new ActionMessages();

        String op = (request.getParameter("op")==null) 
					? (frm.getOp()==null || frm.getOp().length()==0)
					        ? (request.getAttribute("op")==null)
					                ? null
					                : request.getAttribute("op").toString()
					        : frm.getOp()
					: request.getParameter("op");		        
	        
		if(op==null || op.trim().length()==0)
		    op = rsc.getMessage("op.view");
		
		frm.setOp(op);
        
		// First Access - display blank form
        if ( op.equals(rsc.getMessage("op.view")) ) {
            LookupTables.setupTimetableManagers(request);
        }        
		
        // Change User
        if ( op.equals(rsc.getMessage("button.changeUser")) ) {
            try {
                doSwitch(request, frm, user);
                return mapping.findForward("reload");
			}
			catch(Exception e) {
				Debug.error(e);
	            errors.add("exception", 
	                    new ActionMessage("errors.generic", e.getMessage()) );
	            saveErrors(request, errors);
	            LookupTables.setupTimetableManagers(request);
				return mapping.findForward("displayForm");
			}
        }        
		
        return mapping.findForward("displayForm");
    }

    /**
     * Reads in new user attributes and reloads Timetabling for the new user
     * @param request
     * @param frm
     * @param u
     */
    private void doSwitch(
            HttpServletRequest request, 
            ChameleonForm frm, 
            UserContext user) throws Exception {
    	
		for (SessionAttribute a: SessionAttribute.values())
			request.getSession().removeAttribute(a.key());
    	
    	if (user instanceof UserContext.Chameleon)
    		user = ((UserContext.Chameleon)user).getOriginalUserContext();
    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if (authentication instanceof ChameleonAuthentication)
    		authentication = ((ChameleonAuthentication)authentication).getOriginalAuthentication();
    	
    	if (user.getExternalUserId().equals(frm.getPuid())) {
    		SecurityContextHolder.getContext().setAuthentication(authentication);
    	} else {
    		SecurityContextHolder.getContext().setAuthentication(
        			new ChameleonAuthentication(
        					authentication, new ChameleonUserContext(frm.getPuid(), user)
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
