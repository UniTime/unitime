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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.UserDataInterface;
import org.unitime.timetable.gwt.shared.UserDataInterface.GetUserDataRpcRequest;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(GetUserDataRpcRequest.class)
public class GetUserDataBackend implements GwtRpcImplementation<GetUserDataRpcRequest, UserDataInterface> {
    @Autowired 
	private SessionContext sessionContext;

	@Override
	public UserDataInterface execute(GetUserDataRpcRequest request, SessionContext context) {
		UserDataInterface ret = new UserDataInterface();
		UserContext user = sessionContext.getUser();
		if (user == null) return null;
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		try {
			for (UserData u: (List<UserData>)hibSession.createQuery(
					"from UserData u where u.externalUniqueId = :externalUniqueId and u.name in :names")
					.setString("externalUniqueId", user.getExternalUserId())
					.setParameterList("names", request)
					.setCacheable(true).list()) {
				ret.put(u.getName(), u.getValue());
			}
		} finally {
			hibSession.close();
		}
		return ret;
	}

}
