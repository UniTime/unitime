/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
