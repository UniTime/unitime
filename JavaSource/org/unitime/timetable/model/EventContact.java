/* 
 * UniTime 3.1 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC
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

import org.hibernate.FlushMode;
import org.unitime.timetable.model.base.BaseEventContact;
import org.unitime.timetable.model.dao.EventContactDAO;



/**
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
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

/*[CONSTRUCTOR MARKER END]*/

	public static EventContact findByExternalUniqueId(String externalUniqueId) {
	    return (EventContact)new EventContactDAO().getSession().
	        createQuery("select c from EventContact c where c.externalUniqueId=:externalUniqueId").
	        setString("externalUniqueId", externalUniqueId).
	        setFlushMode(FlushMode.MANUAL).
	        uniqueResult();
	}

	public static EventContact findByEmail(String email) {
	    List<EventContact> ec = (List<EventContact>)new EventContactDAO().getSession().
	        createQuery("select c from EventContact c where c.emailAddress=:emailAddress").
	        setString("emailAddress", email).list();
	    if (ec.isEmpty()) return null; 
	    else return ec.get(0);
	}
	
    public String getShortName() {
        StringBuffer sb = new StringBuffer();
        if (getFirstName()!=null && getFirstName().length()>0) {
            sb.append(getFirstName().substring(0,1).toUpperCase());
            sb.append(". ");
        }
        if (getLastName()!=null && getLastName().length()>0) {
            sb.append(getLastName().substring(0,1).toUpperCase());
            sb.append(getLastName().substring(1,Math.min(10,getLastName().length())).toLowerCase().trim());
        }
        return sb.toString();
    }

    public String getName() {
        return ((getLastName() == null ? "" : getLastName().trim()) + ", "+ 
                (getFirstName() == null ? "" : getFirstName().trim()) + " "+
                (getMiddleName() == null ? "" : getMiddleName().trim()));
    }
}
