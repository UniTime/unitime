/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.dao;

import org.hibernate.criterion.Order;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.base.BaseSessionDAO;


/**
 * @author Tomas Muller
 */
public class SessionDAO extends BaseSessionDAO {

	/**
	 * Default constructor.  Can be used in place of getInstance()
	 */
	public SessionDAO () {}
	
	public Order getDefaultOrder() {
		return Order.desc(Session.PROP_SESSION_BEGIN_DATE_TIME);
	}


}
