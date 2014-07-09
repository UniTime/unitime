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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.UserDataInterface.SetUserDataRpcRequest;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SetUserDataRpcRequest.class)
public class SetUserDataBackend implements GwtRpcImplementation<SetUserDataRpcRequest, GwtRpcResponseNull> {
    @Autowired 
	private SessionContext sessionContext;

	@Override
	public GwtRpcResponseNull execute(SetUserDataRpcRequest request, SessionContext context) {
		UserContext user = sessionContext.getUser();
		if (user == null) return null;
		for (Map.Entry<String, String> u: request.entrySet())
			UserData.setProperty(user.getExternalUserId(), u.getKey(), u.getValue());
		return null;
	}

}
