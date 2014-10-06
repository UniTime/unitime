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

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoleListForm;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;


/**
 * MyEclipse Struts
 * Creation date: 03-17-2005
 *
 * XDoclet definition:
 * @struts:action path="/selectPrimaryRole" name="roleListForm" input="/selectPrimaryRole.jsp" scope="request" validate="true"
 * @struts:action-forward name="success" path="/main.jsp" contextRelative="true"
 * @struts:action-forward name="fail" path="/selectPrimaryRole.jsp" contextRelative="true"
 *
 * @author Tomas Muller
 */
@Service("/selectPrimaryRole")
public class RoleListAction extends Action {

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
    	RoleListForm roleListForm = (RoleListForm) form;

        UserContext user = null;
    	try {
    		user = (UserContext)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	} catch (Exception e) {}
    	
        if (user == null) return mapping.findForward("loginRequired");
        
        if (user.getAuthorities().isEmpty()) return mapping.findForward("norole");
        
        // Form submitted
        if (roleListForm.getAuthority() != null) {
        	UserAuthority authority = user.getAuthority(roleListForm.getAuthority());
        	if (authority != null) {
        		user.setCurrentAuthority(authority);
            	for (SessionAttribute s: SessionAttribute.values())
            		sessionContext.removeAttribute(s);
        	}
        	if (roleListForm.getTarget() != null && !roleListForm.getTarget().isEmpty()) {
        		response.sendRedirect(roleListForm.getTarget());
        		return null;
        	} else {
        		return mapping.findForward("success");
        	}
        }

        UserAuthority authority = setupAuthorities(request, user);

        // Role/session list not requested -- try assign default role/session first 
        if (!"Y".equals(request.getParameter("list")) && authority != null) {
        	user.setCurrentAuthority(authority);
        	if (roleListForm.getTarget() != null && !roleListForm.getTarget().isEmpty()) {
        		response.sendRedirect(roleListForm.getTarget());
        		return null;
        	} else {
        		return mapping.findForward("success");
        	}
        }
        
    	Set<String> roles = new HashSet<String>();
    	for (UserAuthority a: user.getAuthorities())
    		roles.add(a.getRole());
        
        switch (roles.size()) {
		case 0:
			return mapping.findForward("norole");
		case 1:
			return mapping.findForward("getDefaultAcadSession");
		default:
			return mapping.findForward("getUserSelectedRole");
        }
    }
    
    private UserAuthority setupAuthorities(HttpServletRequest request, UserContext user) {
    	WebTable.setOrder(sessionContext,"roleLists.ord",request.getParameter("ord"), -2);
    	
    	Set<String> roles = new HashSet<String>();
    	for (UserAuthority authority: user.getAuthorities())
    		roles.add(authority.getRole());
    	
    	WebTable table = new WebTable(4,"Select " + (roles.size() > 1 ? "User Role &amp; " : "") + "Academic Session",
        		"selectPrimaryRole.do?list=Y&ord=%%",
                new String[] { "User Role", "Academic Session", "Academic Initiative", "Academic Session Status" },
                new String[] { "left", "left", "left", "left"},
                new boolean[] { true, true, true, true});
    	
    	int nrLines = 0;
    	UserAuthority firstAuthority = null;
    	for (UserAuthority authority: user.getAuthorities()) {
    		Session session = (authority.getAcademicSession() == null ? null : SessionDAO.getInstance().get((Long)authority.getAcademicSession().getQualifierId()));
    		if (session == null) continue;
    		

    		String onClick =
    				"onClick=\"roleListForm.authority.value='" + authority.getAuthority() + "';roleListForm.submit();\"";
    		
    		String bgColor = (authority.equals(user.getCurrentAuthority()) ? "rgb(168,187,225)" : null);
    		
    		table.addLine(
    				onClick,
    				new String[] {
    						authority.getLabel(),
    						session.getAcademicYear()+" "+session.getAcademicTerm(),
    						session.getAcademicInitiative(),
    						(session.getStatusType()==null?"":session.getStatusType().getLabel())},
    				new Comparable[] {
    						new MultiComparable(authority.toString(), session.getSessionBeginDateTime(), session.getAcademicInitiative()),
    						new MultiComparable(session.getSessionBeginDateTime(), session.getAcademicInitiative(), authority.toString()),
    						new MultiComparable(session.getAcademicInitiative(), session.getSessionBeginDateTime(), authority.toString()),
    						new MultiComparable(session.getStatusType()==null ? -1 : session.getStatusType().getOrd(), session.getAcademicInitiative(), session.getSessionBeginDateTime(), authority.toString())
    					}
    				).setBgColor(bgColor);
    		
    		if (firstAuthority == null) firstAuthority = authority;
    		
    		nrLines++;
    	}
    	
        if (user.getCurrentAuthority() == null && nrLines == 0)
            table.addLine(new String[] {"<i><font color='red'>No user role and/or academic session associated with the user " + (user.getName() == null ? user.getUsername() : user.getName()) + ".</font></i>",null,null,null}, null);
 	    
        if (nrLines == 1 && firstAuthority != null)
        	user.setCurrentAuthority(firstAuthority);
        
 	    request.setAttribute(Roles.USER_ROLES_ATTR_NAME, table.printTable(WebTable.getOrder(sessionContext,"roleLists.ord")));
 	    
    	return user.getCurrentAuthority();
    }

}
