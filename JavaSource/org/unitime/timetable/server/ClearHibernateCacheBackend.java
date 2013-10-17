/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server;

import org.unitime.commons.hibernate.util.HibernateUtil;

import org.unitime.timetable.gwt.client.admin.ClearHibernateCache.ClearHibernateCacheRequest;
import org.unitime.timetable.gwt.client.admin.ClearHibernateCache.ClearHibernateCacheResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ClearHibernateCacheRequest.class)
public class ClearHibernateCacheBackend implements GwtRpcImplementation<ClearHibernateCacheRequest, ClearHibernateCacheResponse> {

	@Override
	public ClearHibernateCacheResponse execute(ClearHibernateCacheRequest request, SessionContext context) {
		context.checkPermission(Right.ClearHibernateCache);
		HibernateUtil.clearCache();
		return new ClearHibernateCacheResponse();
	}

}
