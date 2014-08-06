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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseLong;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLStoreRpcRequest;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(HQLStoreRpcRequest.class)
public class HQLStoreBackend implements GwtRpcImplementation<HQLStoreRpcRequest, GwtRpcResponseLong>{
	@Autowired 
	private SessionContext sessionContext;

	@Override
	@PreAuthorize("(#query.id != null and checkPermission(#query.id, 'SavedHQL', 'HQLReportEdit')) or (#query.id == null and checkPermission('HQLReportAdd'))")
	public GwtRpcResponseLong execute(HQLStoreRpcRequest query, SessionContext context) {
		if (SavedHQL.Flag.ADMIN_ONLY.isSet(query.getFlags()))
			sessionContext.checkPermission(Right.HQLReportsAdminOnly);
		org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
		SavedHQL hql = null;
		if (query.getId() != null) {
			hql = SavedHQLDAO.getInstance().get(query.getId(), hibSession);
		}
		if (hql == null) {
			hql = new SavedHQL();
		}
		hql.setName(query.getName());
		hql.setDescription(query.getDescription());
		hql.setType(query.getFlags());
		hql.setQuery(query.getQuery());
		hibSession.saveOrUpdate(hql);
		hibSession.flush();
		hibSession.refresh(hql);
		return new GwtRpcResponseLong(hql.getUniqueId());
	}

}
