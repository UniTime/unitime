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

/*[CONSTRUCTOR MARKER END]*/
	
	public static String featureTypeDisplayString() {
		return "Global";		
	}

	/**
	 * @param sisReference
	 * @return
	 */
	public static GlobalRoomFeature featureWithSisReference(Session session, String sisReference) {
		for (GlobalRoomFeature grf: RoomFeature.getAllGlobalRoomFeatures(session))
			if (sisReference.equals(grf.getSisReference()))
				return grf;
		return null;
	}
    
    public String toString() {
        return getLabel();
    }
    
	public static GlobalRoomFeature findGlobalRoomFeatureForLabel(Session session, String label){
		GlobalRoomFeatureDAO grfDao = new GlobalRoomFeatureDAO();
		List features = grfDao.getSession().createCriteria(GlobalRoomFeature.class)
			.add(Restrictions.eq("label", label))
			.add(Restrictions.eq("session.uniqueId", session.getUniqueId()))
			.setCacheable(true).list();

		if (features.size() == 1){
			return((GlobalRoomFeature) features.get(0));
		}
		return(null);
	}
    
	public static GlobalRoomFeature findGlobalRoomFeatureForAbbv(Session session, String label){
		GlobalRoomFeatureDAO grfDao = new GlobalRoomFeatureDAO();
		List features = grfDao.getSession().createCriteria(GlobalRoomFeature.class)
			.add(Restrictions.eq("abbv", label))
			.add(Restrictions.eq("session.uniqueId", session.getUniqueId()))
			.setCacheable(true).list();

		if (features.size() == 1){
			return((GlobalRoomFeature) features.get(0));
		}
		return(null);
	}

	public String htmlLabel() {
        return "<span "+
            "title='"+getLabel()+" (" + (getFeatureType() == null ? "global" : getFeatureType().getReference()) + ")'>"+
            getLabel() + (getFeatureType() == null ? "" : " (" + getFeatureType().getReference() + ")")+
            "</span>";
    }

	public Object clone(){
		GlobalRoomFeature newFeature = new GlobalRoomFeature();
		newFeature.setLabel(getLabel());
		newFeature.setAbbv(getAbbv());
		newFeature.setSession(getSession());
		newFeature.setSisReference(getSisReference());
		newFeature.setSisValue(getSisValue());
		newFeature.setFeatureType(getFeatureType());
		return(newFeature);
	}

}
