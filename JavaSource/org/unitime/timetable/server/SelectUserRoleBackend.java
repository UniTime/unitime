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

import java.util.HashSet;
import java.util.Set;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.page.SelectUserRolePage.SelectUserRoleReponse;
import org.unitime.timetable.gwt.client.page.SelectUserRolePage.SelectUserRoleRequest;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;

@GwtRpcImplements(SelectUserRoleRequest.class)
public class SelectUserRoleBackend implements GwtRpcImplementation<SelectUserRoleRequest, SelectUserRoleReponse>{
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public SelectUserRoleReponse execute(SelectUserRoleRequest request, SessionContext context) {
        UserContext user = context.getUser();
 		SelectUserRoleReponse response = new SelectUserRoleReponse();
    	
 		// loginRequired
 		if (user == null) {
 			response.setUrl("loginRequired.action");
 			return response;
 		}
 		
 		// no roles
 		if (user.getAuthorities().isEmpty()) {
 			response.setUrl("main.action");
 			return response;
 		}
 		
 		if (request.hasAuthority()) {
 			UserAuthority authority = user.getAuthority(request.getAuthority());
 	    	if (authority != null) {
 	    		user.setCurrentAuthority(authority);
 	        	for (SessionAttribute s: SessionAttribute.values())
 	        		context.removeAttribute(s);
 	 	    	if (request.hasTarget()) {
 	 	    		response.setUrl(request.getTarget());
 	 	    	} else {
 	 	    		// success 	    		
 	 	    		response.setUrl("main.action");
 	 	    	}
 	 	    	return response;
 	    	}
 		}
 		
 		if (user.getCurrentAuthority() != null) {
 			request.setAuthority(user.getCurrentAuthority().getAuthority());
 			if (!request.isList()) {
 				if (request.hasTarget()) {
 	        		response.setUrl(request.getTarget());
 	 	    	} else {
 	 	    		// success 	    		
 	 	    		response.setUrl("main.action");
 	 	    	}
 			}
 		}
 		
 		UserAuthority authority = setupAuthorities(request, user, response);
 		
        // Role/session list not requested -- try assign default role/session first 
        if (!request.isList() && authority != null) {
        	user.setCurrentAuthority(authority);
        	for (SessionAttribute s: SessionAttribute.values())
        		context.removeAttribute(s);
        	if (request.hasTarget()) {
        		response.setUrl(request.getTarget());
 	    	} else {
 	    		// success 	    		
 	    		response.setUrl("main.action");
 	    	}
        }
        
        if (!request.isList())
        	response.setMessage(MSG.infoNoDefaultAuthority());

		return response;
	}
	
    private UserAuthority setupAuthorities(SelectUserRoleRequest request, UserContext user, SelectUserRoleReponse response) {
    	TableInterface table = new TableInterface();
        table.setId("UserRoles");
        table.setDefaultSortCookie("!" + MSG.columnAcademicSession());
    	response.setTable(table);
    	
    	Set<String> roles = new HashSet<String>();
    	LineInterface header = table.addHeader();
    	header.addCell(MSG.columnUserRole()).setSortable(true);
    	header.addCell(MSG.columnAcademicSession()).setSortable(true);
    	header.addCell(MSG.columnAcademicInitiative()).setSortable(true);
    	header.addCell(MSG.columnAcademicSessionStatus()).setSortable(true);
    	for (CellInterface h: header.getCells())
        	h.setClassName("WebTableHeader");
    	
    	String lastAuthority = UserProperty.LastAuthority.get(user);

    	int nrLines = 0;
    	UserAuthority firstAuthority = null;
    	for (UserAuthority authority: user.getAuthorities()) {
    		Session session = (authority.getAcademicSession() == null ? null : SessionDAO.getInstance().get((Long)authority.getAcademicSession().getQualifierId()));
    		if (session == null) continue;
    		boolean active = (session.getStatusType() != null && session.getStatusType().isActive());
    		if (!active) response.setHasInactive(true);
    		if (!request.isIncludeInactive() && !active && !authority.getAuthority().equals(request.getAuthority())) continue;
    		LineInterface line = table.addLine();
    		line.setURL("#" + authority.getAuthority());
    		if (!request.hasAuthority() && authority.getAuthority().equals(lastAuthority))
    			line.setClassName("unitime-TableRowSelected" + (active ? "" : " inactive-session"));
    		else if (authority.getAuthority().equals(request.getAuthority()))
    			line.setClassName("unitime-TableRowSelected" + (active ? "" : " inactive-session"));
    		else if (!active)
    			line.setClassName("inactive-session");
    		
    		line.addCell(authority.getLabel())
    			.setComparable(authority.getLabel(), session.getSessionBeginDateTime(), session.getAcademicInitiative());
    		line.addCell(session.getAcademicYear()+" "+session.getAcademicTerm())
    			.setComparable(session.getSessionBeginDateTime(), session.getAcademicInitiative(), authority.getLabel());
    		line.addCell(session.getAcademicInitiative())
				.setComparable(session.getAcademicInitiative(), session.getSessionBeginDateTime(), authority.getLabel());
    		line.addCell(session.getStatusType() == null ? "" : session.getStatusType().getLabel())
				.setComparable(session.getStatusType()==null ? -1 : session.getStatusType().getOrd(), session.getSessionBeginDateTime(), session.getAcademicInitiative(), authority.getLabel());
    		if (firstAuthority == null) firstAuthority = authority;
    		roles.add(authority.getRole());
    		
    		nrLines++;
    	}
    	
    	if (roles.size() > 1) {
    		table.setName(MSG.sectSelectUserRoleAndSession());
    		response.setPageName("Select User Role");
    	} else {
    		table.setName(MSG.sectSelectAcademicSession());
    		response.setPageName("Select Academic Session");
    	}
    	
        if (user.getCurrentAuthority() == null && nrLines == 0)
        	table.setErrorMessage(MSG.warnNoRoleForUser(user.getName() == null ? user.getUsername() : user.getName()));
 	    
        if (nrLines == 1 && firstAuthority != null)
        	user.setCurrentAuthority(firstAuthority);
        
    	return user.getCurrentAuthority();
    }

}
