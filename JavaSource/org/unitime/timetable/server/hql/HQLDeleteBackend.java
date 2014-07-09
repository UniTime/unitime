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
package org.unitime.timetable.server.hql;

import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseBoolean;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLDeleteRpcRequest;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(HQLDeleteRpcRequest.class)
public class HQLDeleteBackend implements GwtRpcImplementation<HQLDeleteRpcRequest, GwtRpcResponseBoolean>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	@PreAuthorize("checkPermission(#request.id, 'SavedHQL', 'HQLReportDelete')")
	public GwtRpcResponseBoolean execute(HQLDeleteRpcRequest request, SessionContext context) {
		if (request.getId() == null) throw new GwtRpcException(MESSAGES.errorNoReportProvided());
		org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
		SavedHQL hql = SavedHQLDAO.getInstance().get(request.getId(), hibSession);
		if (hql != null) {
			hibSession.delete(hql);
			hibSession.flush();
		}
		return new GwtRpcResponseBoolean(hql != null);
	}
}
