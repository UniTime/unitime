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

import java.util.Collection;
import java.util.List;

import org.unitime.timetable.model.base.BaseDepartmentRoomFeature;
import org.unitime.timetable.model.dao.DepartmentRoomFeatureDAO;




public class DepartmentRoomFeature extends BaseDepartmentRoomFeature {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public DepartmentRoomFeature () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public DepartmentRoomFeature (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public DepartmentRoomFeature (
		java.lang.Long uniqueId,
		java.lang.String label) {

		super (
			uniqueId,
			label);
	}

/*[CONSTRUCTOR MARKER END]*/
	public static String featureTypeDisplayString() {
		return "Department";		
	}
	
	public String getDeptCode() {
		return (getDepartment()==null?null:getDepartment().getDeptCode());
	}

	public String htmlLabel() {
		return "<span "+
		"style='color:#"+getDepartment().getRoomSharingColor(null)+";font-weight:bold;' "+
		"title='"+getLabel()+
		" ("+(getDepartment().isExternalManager().booleanValue()?getDepartment().getExternalMgrLabel():getDepartment().getName())+")'>"+
		getLabel() +
		"</span>";
	}
	
	/**
	 * @return Room feature label with the word (Department) appended to it
	 */
	public String getLabelWithType() {
	    return getLabel() + " (Department)";
	}
    
    public String toString() {
        return getLabel();
    }
    
	public static Collection getAllRoomFeaturesForSession(Session session){
		if (session == null){
			return(null);
		}
		return((new DepartmentRoomFeatureDAO()).
				getSession().
				createQuery("select distinct d from DepartmentRoomFeature d where d.department.session.uniqueId=:sessionId order by label").
				setLong("sessionId", session.getUniqueId().longValue()).
				setCacheable(true).
				list());
	}

    
	public Object clone(){
		DepartmentRoomFeature newFeature = new DepartmentRoomFeature();
		newFeature.setLabel(getLabel());
		newFeature.setDepartment(getDepartment());
		return(newFeature);
	}

	public DepartmentRoomFeature findSameFeatureInSession(Session session) {
		if (session == null){
			return(null);
		}
		List l =(new DepartmentRoomFeatureDAO()).
				getSession().
				createQuery("select distinct d from DepartmentRoomFeature d where d.department.session.uniqueId=:sessionId and d.label=:label and d.department.deptCode=:deptCode").
				setLong("sessionId", session.getUniqueId().longValue()).
				setString("deptCode", getDepartment().getDeptCode()).
				setString("label", getLabel()).
				setCacheable(true).
				list();
		if (l.size() == 1){
			return((DepartmentRoomFeature)l.get(0));
		} else {
			return(null);
		}
	}
	
}