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

import org.unitime.timetable.model.base.BaseEventType;
import org.unitime.timetable.model.dao.EventTypeDAO;



public class EventType extends BaseEventType {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public EventType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public EventType (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public EventType (
		java.lang.Long uniqueId,
		java.lang.String reference) {

		super (
			uniqueId,
			reference);
	}

/*[CONSTRUCTOR MARKER END]*/
	public static final String sEventTypeClass = "class";
	public static final String sEventTypeFinalExam = "final";
	public static final String sEventTypeEveningExam = "evening";
	public static final String sEventTypeOtherWithConflicts = "otherWithConflict";
	public static final String sEventTypeOtherNoConflicts = "otherNoConflict";
	public static final String sEventTypeSpecial = "special";
	
	public static EventType findByReference(String reference) {
		return (EventType)new EventTypeDAO().getSession().createQuery(
				"select t from EventType t where t.reference=:reference").
				setString("reference", reference).
				setCacheable(true).uniqueResult();
	}

}