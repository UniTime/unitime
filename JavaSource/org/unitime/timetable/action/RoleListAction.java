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

import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoleListForm;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;


/**
 * @author Tomas Muller
 */
@Action(value = "selectPrimaryRole", results = {
		@Result(name = "getUserSelectedRole", type = "tiles", location = "selectPrimaryRole.tiles"),
		@Result(name = "getDefaultAcadSession", type = "tiles", location = "selectAcadSession.tiles"),
		@Result(name = "success", type = "redirect", location = "/main.action"),
		@Result(name = "fail", type = "tiles", location = "selectPrimaryRole.tiles"),
		@Result(name = "loginRequired", type = "redirect", location = "/loginRequired.action"),
		@Result(name = "norole", type = "redirect", location = "/main.action")
	})
@TilesDefinitions({
@TilesDefinition(name = "selectPrimaryRole.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Select User Role"),
		@TilesPutAttribute(name = "body", value = "/selectPrimaryRole.jsp"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	}),
@TilesDefinition(name = "selectAcadSession.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Select Academic Session"),
		@TilesPutAttribute(name = "body", value = "/selectPrimaryRole.jsp"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	})
})
public class RoleListAction extends UniTimeAction<RoleListForm> {
	private static final long serialVersionUID = -1366311288296904417L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String list, target;
	public String getList() { return list; }
	public void setList(String list) { this.list = list; }
	public String getTraget() { return target; }
	public void setTarget(String target) { this.target = target; }

    public String execute() throws Exception {
    	if (form == null) {
    		form = new RoleListForm();
    		form.reset();
    	}
    	if (target != null) form.setTarget(target);

        UserContext user = null;
    	try {
    		user = (UserContext)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	} catch (Exception e) {}
    	
        if (user == null) return "loginRequired";
        
        if (user.getAuthorities().isEmpty()) return "norole";
        
        // Form submitted
        if (form.getAuthority() != null) {
        	UserAuthority authority = user.getAuthority(form.getAuthority());
        	if (authority != null) {
        		user.setCurrentAuthority(authority);
            	for (SessionAttribute s: SessionAttribute.values())
            		sessionContext.removeAttribute(s);
        	}
        	if (form.getTarget() != null && !form.getTarget().isEmpty()) {
        		response.sendRedirect(form.getTarget());
        		return null;
        	} else {
        		return "success";
        	}
        }

        UserAuthority authority = setupAuthorities(request, user);

        // Role/session list not requested -- try assign default role/session first 
        if (!"Y".equals(list) && authority != null) {
        	user.setCurrentAuthority(authority);
        	if (form.getTarget() != null && !form.getTarget().isEmpty()) {
        		response.sendRedirect(form.getTarget());
        		return null;
        	} else {
        		return "success";
        	}
        }
        
    	Set<String> roles = new HashSet<String>();
    	for (UserAuthority a: user.getAuthorities())
    		roles.add(a.getRole());
        
        switch (roles.size()) {
		case 0:
			return "norole";
		case 1:
			return "getDefaultAcadSession";
		default:
			return "getUserSelectedRole";
        }
    }
    
    private UserAuthority setupAuthorities(HttpServletRequest request, UserContext user) {
    	WebTable.setOrder(sessionContext,"roleLists.ord",request.getParameter("ord"), -2);
    	
    	Set<String> roles = new HashSet<String>();
    	for (UserAuthority authority: user.getAuthorities())
    		roles.add(authority.getRole());
    	
    	WebTable table = new WebTable(4,
    			roles.size() > 1 ? MSG.sectSelectUserRoleAndSession() : MSG.sectSelectAcademicSession(),
        		"selectPrimaryRole.action?list=Y&ord=%%",
                new String[] { MSG.columnUserRole(),
                		MSG.columnAcademicSession(), 
                		MSG.columnAcademicInitiative(),
                		MSG.columnAcademicSessionStatus() },
                new String[] { "left", "left", "left", "left"},
                new boolean[] { true, true, true, true});
    	
    	int nrLines = 0;
    	UserAuthority firstAuthority = null;
    	for (UserAuthority authority: user.getAuthorities()) {
    		Session session = (authority.getAcademicSession() == null ? null : SessionDAO.getInstance().get((Long)authority.getAcademicSession().getQualifierId()));
    		if (session == null) continue;
    		

    		String onClick =
    				"onClick=\"document.getElementById('authority').value='" + authority.getAuthority() + "';document.getElementById('form').submit();\"";
    		
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
            table.addLine(new String[] {"<i><font color='red'>" + MSG.warnNoRoleForUser(user.getName() == null ? user.getUsername() : user.getName()) + "</font></i>",null,null,null}, null);
 	    
        if (nrLines == 1 && firstAuthority != null)
        	user.setCurrentAuthority(firstAuthority);
        
 	    request.setAttribute(Roles.USER_ROLES_ATTR_NAME, table.printTable(WebTable.getOrder(sessionContext,"roleLists.ord")));
 	    
    	return user.getCurrentAuthority();
    }

}
