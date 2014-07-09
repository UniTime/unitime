/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.MenuInterface.UserInfoInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.UserInfoRpcRequest;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.AnonymousUserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(UserInfoRpcRequest.class)
public class UserInfoBackend implements GwtRpcImplementation<UserInfoRpcRequest, UserInfoInterface> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

    @Autowired 
	private SessionContext sessionContext;

	@Override
	public UserInfoInterface execute(UserInfoRpcRequest request, SessionContext context) {
		UserInfoInterface ret = new UserInfoInterface();
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		try {
			
			UserContext user = sessionContext.getUser();
			if (user == null || user instanceof AnonymousUserContext) return null;
			
			ret.addPair(MESSAGES.fieldName(), user.getName());
			ret.setName(user.getName());
			
			String dept = "";
			if (user.getCurrentAuthority() != null)
				for (Qualifiable q: user.getCurrentAuthority().getQualifiers("Department")) {
 					if (!dept.isEmpty()) dept += ",";
 					dept += "<span title='"+q.getQualifierLabel()+"'>"+q.getQualifierReference()+"</span>";
				}
			ret.addPair(MESSAGES.fieldDepartment(), dept);
	 		
	 		String role = (user.getCurrentAuthority() == null ? null : user.getCurrentAuthority().getLabel());
	 		if (role == null) role = MESSAGES.noRole();
	 		ret.addPair(MESSAGES.fieldRole(), role);
	 		ret.setRole(role);
	 		
	 		ret.setChameleon(sessionContext.hasPermission(Right.Chameleon) || (user instanceof UserContext.Chameleon));

		} finally {
			hibSession.close();
		}
		return ret;
	}

}
