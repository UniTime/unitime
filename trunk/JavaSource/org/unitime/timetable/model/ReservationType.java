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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseReservationType;
import org.unitime.timetable.model.dao.ReservationTypeDAO;




public class ReservationType extends BaseReservationType {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ReservationType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ReservationType (Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public ReservationType (
		Long uniqueId,
		java.lang.String reference) {

		super (
			uniqueId,
			reference);
	}

/*[CONSTRUCTOR MARKER END]*/


	/*
	 * @return all Reservation Types
	 */
	public static ArrayList getAll() throws HibernateException {
		return (ArrayList) (new ReservationTypeDAO()).findAll();
	}

    /** Request attribute name for available reservation types **/
    public static String RESVTYPE_ATTR_NAME = "reservationTypeList";  
    
    /** Reservation Type List **/
    private static Vector resvTypeList = null;
    
	/**
	 * Retrieves all reservation types in the database
	 * ordered by column label
	 * @param refresh true - refreshes the list from database
	 * @return Vector of ReservationType objects
	 */
    public static synchronized Vector getReservationTypeList(boolean refresh) {
        if(resvTypeList!=null && !refresh)
            return resvTypeList;
        
        ReservationTypeDAO rdao = new ReservationTypeDAO();
        Vector orderList = new Vector();
        orderList.addElement(Order.asc("label"));

        List l = rdao.findAll(orderList);
        resvTypeList = new Vector(l);
        return resvTypeList;
    }

    /**
     * Gets the reservation type object matching the particular reference value
     * @param ref Reference value
     * @return ReservationType object if found, null if not found
     */
    public static ReservationType getReservationTypebyRef (String ref) {
        ReservationTypeDAO rdao = new ReservationTypeDAO();
        org.hibernate.Session hibSession = rdao.getSession();
        List l = hibSession.createCriteria(ReservationType.class)
        			.add(Restrictions.eq("reference", ref))
        			.setCacheable(true)
        			.list();
        
        if (l==null || l.size()==0)
            return null;
        
        return (ReservationType) l.get(0);
    }
}