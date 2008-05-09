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

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseGlobalRoomFeature;
import org.unitime.timetable.model.dao.GlobalRoomFeatureDAO;




public class GlobalRoomFeature extends BaseGlobalRoomFeature {
	private static final long serialVersionUID = 1L;

	public static TreeMap sisFeatureMap = null;
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public GlobalRoomFeature () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public GlobalRoomFeature (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public GlobalRoomFeature (
		java.lang.Long uniqueId,
		java.lang.String label) {

		super (
			uniqueId,
			label);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static String featureTypeDisplayString() {
		return "Global";		
	}

	/**
	 * @param sisReference
	 * @return
	 */
	public static GlobalRoomFeature featureWithSisReference(String sisReference) {
		for (Iterator it = RoomFeature.getAllRoomFeatures(GlobalRoomFeature.class).iterator(); it.hasNext();){
			GlobalRoomFeature grf = (GlobalRoomFeature)it.next();
			if (sisReference.equals(grf.getSisReference())) {
				return grf;
			}
		}
		return null;
	}
    
    public String toString() {
        return getLabel();
    }
    
	public static GlobalRoomFeature findGlobalRoomFeatureForLabel(String label){
		GlobalRoomFeatureDAO grfDao = new GlobalRoomFeatureDAO();
		List features = grfDao.getSession().createCriteria(GlobalRoomFeature.class)
			.add(Restrictions.eq("label", label))
			.setCacheable(true).list();

		if (features.size() == 1){
			return((GlobalRoomFeature) features.get(0));
		}
		return(null);
	}
    
    public String htmlLabel() {
        return "<span "+
            "title='"+getLabel()+" (global)'>"+
            getLabel() +
            "</span>";
    }

}