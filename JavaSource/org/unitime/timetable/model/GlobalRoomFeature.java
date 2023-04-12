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




import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import java.util.TreeMap;

import org.unitime.timetable.model.base.BaseGlobalRoomFeature;
import org.unitime.timetable.model.dao.GlobalRoomFeatureDAO;




/**
 * @author Tomas Muller
 */
@Entity
@DiscriminatorValue("global")
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
		return GlobalRoomFeatureDAO.getInstance().getSession()
				.createQuery("from GlobalRoomFeature where label = :label and session.uniqueId = :sessionId", GlobalRoomFeature.class)
				.setParameter("label", label)
				.setParameter("sessionId", session.getUniqueId())
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
	}
    
	public static GlobalRoomFeature findGlobalRoomFeatureForAbbv(Session session, String label){
		return GlobalRoomFeatureDAO.getInstance().getSession()
				.createQuery("from GlobalRoomFeature where abbv = :label and session.uniqueId = :sessionId", GlobalRoomFeature.class)
				.setParameter("label", label)
				.setParameter("sessionId", session.getUniqueId())
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
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
