/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.admin;

import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.security.SessionContext;

public interface AdminTable {
	public SimpleEditInterface.PageName name();
	
	public SimpleEditInterface load(SessionContext context, org.hibernate.Session hibSession);
	
	public void save(SimpleEditInterface data, SessionContext context, org.hibernate.Session hibSession);
	
	public abstract void save(SimpleEditInterface.Record record, SessionContext context, org.hibernate.Session hibSession);
	
	public abstract void update(SimpleEditInterface.Record record, SessionContext context, org.hibernate.Session hibSession);
	
	public abstract void delete(SimpleEditInterface.Record record, SessionContext context, org.hibernate.Session hibSession);
}
