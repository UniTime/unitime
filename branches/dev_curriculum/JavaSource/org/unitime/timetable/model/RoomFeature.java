/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.unitime.timetable.model.base.BaseRoomFeature;
import org.unitime.timetable.model.dao.DepartmentRoomFeatureDAO;
import org.unitime.timetable.model.dao.GlobalRoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;


public class RoomFeature extends BaseRoomFeature implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RoomFeature () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public RoomFeature (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public RoomFeature (
		java.lang.Long uniqueId,
		java.lang.String label) {

		super (
			uniqueId,
			label);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static String featureTypeDisplayString() {
		return "";		
	}
	
	/*
	 * @return all roomFeatures
	 */
	public static ArrayList getAllRoomFeatures() throws HibernateException {
		return (ArrayList) (new RoomFeatureDAO()).findAll(Order.asc("label"));
	}
	
	public static Collection getAllGlobalRoomFeatures() throws HibernateException {
		//return (new GlobalRoomFeatureDAO()).findAll(Order.asc("label"));
		return (new GlobalRoomFeatureDAO()).
			getSession().
			createCriteria(GlobalRoomFeature.class).
			addOrder(Order.asc("label")).setCacheable(true).list();
	}
		
	public static Collection getAllDepartmentRoomFeatures(Department dept) throws HibernateException {
		if (dept==null) return null;
		return (new DepartmentRoomFeatureDAO()).
			getSession().
			createQuery("select distinct d from DepartmentRoomFeature d where d.department.uniqueId=:deptId order by label").
			setLong("deptId", dept.getUniqueId().longValue()).
			setCacheable(true).
			list();
	}

	/*
	 * @return all RoomFeatures of a given class
	 */
	public static ArrayList getAllRoomFeatures(Class cls) throws HibernateException {
		ArrayList coll = new ArrayList();
		for (Iterator it = getAllRoomFeatures().iterator(); it.hasNext();) {
			RoomFeature rf = (RoomFeature) it.next();
			if (rf.getClass() == cls) {
				coll.add(rf);
			}
		}
		return coll;
	}

	/**
	 * @param id
	 * @return
	 * @throws HibernateException
	 */
	public static RoomFeature getRoomFeatureById(Long id) throws HibernateException {
		return (RoomFeature) (new RoomFeatureDAO()).get(id);
	}
	
	/**
	 * @param id
	 * @throws HibernateException
	 */
	public static void deleteRoomFeatureById(Long id) throws HibernateException {
		RoomFeature rf = RoomFeature.getRoomFeatureById(id);
		if (rf != null) {
			(new RoomFeatureDAO()).delete(rf);
		}
		
	}
		
	public void saveOrUpdate() throws HibernateException {
		(new RoomFeatureDAO()).saveOrUpdate(this);
	}

    /** Request attribute name for available room features **/
    public static String FEATURE_LIST_ATTR_NAME = "roomFeaturesList";
	
    /**
     * 
     */
    public int compareTo(Object o) {
    	if (o==null || !(o instanceof RoomFeature)) return -1;
    	RoomFeature rf = (RoomFeature)o;
    	int cmp = getLabel().compareTo(rf.getLabel());
    	if (cmp!=0) return cmp;
    	return getUniqueId().compareTo(rf.getUniqueId());
    }
    
    /**
     * 
     * @param location
     * @return
     */
	public boolean hasLocation (Location location) {
		return getRooms().contains(location);
	}
	
	/**
	 * @return Room feature label
	 */
	public String getLabelWithType() {
	    return getLabel();
	}
    
    public String getAbbv() {
        if (super.getAbbv()!=null && super.getAbbv().trim().length()>0) return super.getAbbv();
        StringBuffer sb = new StringBuffer();
        for (StringTokenizer stk = new StringTokenizer(getLabel()," ");stk.hasMoreTokens();) {
            String word = stk.nextToken();
            if ("and".equalsIgnoreCase(word))
                sb.append("&amp;");
            else if (word.replaceAll("[a-zA-Z\\.]*", "").length()==0) {
                for (int i=0;i<word.length();i++) {
                    if (i==0)
                        sb.append(word.substring(i,i+1).toUpperCase());
                    else if ((i==1 && word.length()>3) || (word.charAt(i)>='A' && word.charAt(i)<='Z'))
                        sb.append(word.charAt(i));
                }
            } else
                sb.append(word);
        }
        return sb.toString();
    }
	
}
