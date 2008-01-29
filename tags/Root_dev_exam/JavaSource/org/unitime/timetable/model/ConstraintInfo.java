/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.unitime.timetable.model.base.BaseConstraintInfo;




public class ConstraintInfo extends BaseConstraintInfo {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ConstraintInfo () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ConstraintInfo (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public ConstraintInfo (
		java.lang.Long uniqueId,
		org.dom4j.Document value) {

		super (
			uniqueId,
			value);
	}

/*[CONSTRUCTOR MARKER END]*/

	public String generateId() {
		Vector ids = new Vector();
		for (Iterator i=getAssignments().iterator();i.hasNext();)
			ids.add(((Assignment)i.next()).getUniqueId());
		Collections.sort(ids);
		StringBuffer sb = new StringBuffer("C");
		for (Enumeration e=ids.elements();e.hasMoreElements();) {
			Integer id = (Integer)e.nextElement();
			sb.append(id.toString());
			if (e.hasMoreElements())
				sb.append("-");
		}
		return sb.toString();
	}

}