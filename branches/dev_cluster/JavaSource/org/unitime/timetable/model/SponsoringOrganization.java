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

import java.util.List;

import org.unitime.timetable.model.base.BaseSponsoringOrganization;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;



public class SponsoringOrganization extends BaseSponsoringOrganization implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SponsoringOrganization () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SponsoringOrganization (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public int compareTo(Object o) {
		SponsoringOrganization so1 = (SponsoringOrganization) this;
		SponsoringOrganization so2 = (SponsoringOrganization) o;
    	int cmp = so1.getName().compareTo(so2.getName());
    	if (cmp!=0) return cmp;
    	else return so1.getUniqueId().compareTo(so2.getUniqueId());
	}

    public static List<SponsoringOrganization> findAll() {
        return new SponsoringOrganizationDAO().getSession().createQuery(
                "select so from SponsoringOrganization so order by so.name"
                ).setCacheable(true).list();
    }
    
}