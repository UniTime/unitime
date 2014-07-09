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
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.MenuInterface.SessionInfoInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.SessionInfoRpcRequest;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SessionInfoRpcRequest.class)
public class SessionInfoBackend implements GwtRpcImplementation<SessionInfoRpcRequest, SessionInfoInterface> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

    @Autowired 
	private SessionContext sessionContext;

	@Override
	public SessionInfoInterface execute(SessionInfoRpcRequest request, SessionContext context) {
		SessionInfoInterface ret = new SessionInfoInterface();
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		try {
			UserContext user = sessionContext.getUser();
			if (user == null) return null;
			
			if (user.getCurrentAcademicSessionId() == null) {
				if (sessionContext.hasPermissionAnyAuthority(Right.HasRole)) {
					ret.addPair(MESSAGES.fieldSession(), MESSAGES.notSelected());
					ret.addPair(MESSAGES.fieldDatabase(), HibernateUtil.getDatabaseName());
					ret.setSession(MESSAGES.notSelected());
			 		return ret;
				}
				return null;
			}
			
			Session session = SessionDAO.getInstance().get(user.getCurrentAcademicSessionId(), hibSession);
	 		
			ret.addPair(MESSAGES.fieldSession(), session.getLabel());
 			ret.addPair(MESSAGES.fieldStatus(), session.getStatusType().getLabel());
 			ret.setSession(session.getLabel());
	 		
 			ret.addPair(MESSAGES.fieldDatabase(), HibernateUtil.getDatabaseName());
	 		
		} finally {
			hibSession.close();
		}		
		return ret;
	}

}
