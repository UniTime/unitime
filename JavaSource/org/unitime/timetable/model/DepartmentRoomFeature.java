/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import java.util.Collection;

import org.unitime.timetable.model.base.BaseDepartmentRoomFeature;
import org.unitime.timetable.model.dao.DepartmentRoomFeatureDAO;




/**
 * @author Tomas Muller
 */
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
		" ("+(getDepartment().isExternalManager().booleanValue()?getDepartment().getExternalMgrLabel():getDepartment().getName())+(getFeatureType() == null ? "" : " " + getFeatureType().getReference())+")'>"+
		getLabel() + (getFeatureType() == null ? "" : " (" + getFeatureType().getReference() + ")") + 
		"</span>";
	}
	
	/**
	 * @return Room feature label with the word (Department) appended to it
	 */
	public String getLabelWithType() {
	    return getLabel() + (getFeatureType() == null ? " (Department)" : " (Department " + getFeatureType().getReference() + ")");
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
		newFeature.setAbbv(getAbbv());
		newFeature.setDepartment(getDepartment());
		newFeature.setFeatureType(getFeatureType());
		return(newFeature);
	}
	
}
