/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.TreeSet;

import org.unitime.timetable.model.base.BaseItypeDesc;
import org.unitime.timetable.model.dao.ItypeDescDAO;




public class ItypeDesc extends BaseItypeDesc implements Comparable {
	private static final long serialVersionUID = 1L;

    public static String[] sBasicTypes = new String[] {"Extended","Basic"}; 

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ItypeDesc () {
		super();
	}

/*[CONSTRUCTOR MARKER END]*/

    /** Request attribute name for available itypes **/
    public static String ITYPE_ATTR_NAME = "itypesList";
    
    /**
     * @return Returns the itypes.
     */
    public static TreeSet findAll(boolean basic) {
        return new TreeSet(
                new ItypeDescDAO().
                getSession().
                createQuery("select i from ItypeDesc i"+(basic?" where i.basic=1":"")).
                setCacheable(true).
                list());
    }

    public String getBasicType() {
        if (getBasic()>=0 && getBasic()<sBasicTypes.length) return sBasicTypes[getBasic()];
        return "Unknown";
    }
    
    public int compareTo(Object o) {
        if (o==null || !(o instanceof ItypeDesc)) return -1;
        return getItype().compareTo(((ItypeDesc)o).getItype());
    }
    
}
