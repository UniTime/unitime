/* 
 * UniTime 3.1 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
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
 
package org.unitime.timetable.model;

import java.util.List;

import org.unitime.timetable.model.base.BaseStandardEventNote;
import org.unitime.timetable.model.dao.StandardEventNoteDAO;



public class StandardEventNote extends BaseStandardEventNote {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public StandardEventNote () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public StandardEventNote (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

   public static List findAll() {
        return new StandardEventNoteDAO().getSession().createQuery(
                "select sen from StandardEventNote sen order by sen.reference"
                ).setCacheable(true).list();
    }	
   
   public String getLabel() {
       return getReference()+": "+getNote();
   }

}
