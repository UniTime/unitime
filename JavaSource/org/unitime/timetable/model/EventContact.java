/* 
 * UniTime 3.1 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2008, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */ 
 
package org.unitime.timetable.model;

import java.util.List;

import org.unitime.timetable.model.base.BaseEventContact;
import org.unitime.timetable.model.dao.EventContactDAO;



public class EventContact extends BaseEventContact {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public EventContact () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public EventContact (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public EventContact (
		java.lang.Long uniqueId,
		java.lang.String emailAddress,
		java.lang.String phone) {

		super (
			uniqueId,
			emailAddress,
			phone);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static EventContact findByExternalUniqueId(String externalUniqueId) {
	    return (EventContact)new EventContactDAO().getSession().
	        createQuery("select c from EventContact c where c.externalUniqueId=:externalUniqueId").
	        setString("externalUniqueId", externalUniqueId).uniqueResult();
	}

	public static EventContact findByEmail(String email) {
	    List<EventContact> ec = (List<EventContact>)new EventContactDAO().getSession().
	        createQuery("select c from EventContact c where c.emailAddress=:emailAddress").
	        setString("emailAddress", email).list();
	    if (ec.isEmpty()) return null; 
	    else return ec.get(0);
	}

}
