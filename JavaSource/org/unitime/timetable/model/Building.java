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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;


import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseBuilding;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.server.rooms.RoomDetailsBackend.UrlSigner;




/**
 * @author Tomas Muller, James Marshall, Zuzana Mullerova
 */
public class Building extends BaseBuilding implements Comparable {

/**
	 * 
	 */
	private static final long serialVersionUID = 3256440313428981557L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Building () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Building (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/** Request attribute name for available buildings **/
    public static String BLDG_LIST_ATTR_NAME = "bldgsList";

    /**
     * @return Building Identifier of the form {Abbr} - {Name}
     */
    public String getAbbrName() {
        return this.getAbbreviation() + " - " + this.getName();
    }

    /**
     * Dummy setter - does nothing (Do not use)
     */
    public void setAbbrName(String abbrName) {
        
    }
    
    /**
     * @return Building Identifier of the form {Abbr} - {Name}
     */
    public String toString() {
        return getAbbrName();        
    }
    
    public int compareTo(Object o) {
    	if (o==null || !(o instanceof Building)) return -1;
    	Building b = (Building)o;
    	int cmp = getAbbreviation().compareTo(b.getAbbreviation());
    	if (cmp!=0) return cmp;
    	return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(b.getUniqueId() == null ? -1 : b.getUniqueId());
    }
    
    /**
     * 
     * @param bldgAbbv
     * @param sessionId
     * @return
     * @throws Exception
     */
	public static Building findByBldgAbbv(String bldgAbbv, Long sessionId) {
		List bldgs = (new BuildingDAO()).getQuery(
				"SELECT distinct b FROM Building b "+ 
				"WHERE b.session.uniqueId=:sessionId AND b.abbreviation=:bldgAbbv").
				setLong("sessionId", sessionId.longValue()).
				setString("bldgAbbv", bldgAbbv).
				list();
		
		if (!bldgs.isEmpty()) return (Building)bldgs.get(0);

		return null;
	}
	
    public static Building findByName(String name, Long sessionId) {
        return (Building)(new BuildingDAO()).getSession().createQuery(
                "select b from Building b where b.session.uniqueId=:sessionId and b.name=:name").
                setLong("sessionId", sessionId.longValue()).
                setString("name", name).
                uniqueResult();
    }

    /*
	 * Update building information using External Building
	 * @param sessionId
	 */
    public static void updateBuildings(Long sessionId) {
        
        Session currentSession = Session.getSessionById(sessionId);
        TreeSet currentBuildings = new TreeSet(currentSession.getBuildings());
        Hashtable updateBuildings = ExternalBuilding.getBuildings(sessionId);
        
        Iterator b = currentBuildings.iterator();
        BuildingDAO bldgDAO = new BuildingDAO();
        
        while(b.hasNext()) {
            Building bldg = (Building)b.next();
            String externalUniqueId = bldg.getExternalUniqueId();
            if(externalUniqueId != null) {
            	ExternalBuilding extBldg = 
            		(ExternalBuilding)updateBuildings.get(externalUniqueId);
            	if(extBldg != null) {
	                if(updateBldgInfo(bldg, extBldg)) {
	                	bldgDAO.update(bldg);
	                }
	                updateBuildings.remove(extBldg.getExternalUniqueId());
            	} else {
            		if(checkBuildingDelete(bldg)) {
            			currentSession.getBuildings().remove(bldg);
            			bldgDAO.delete(bldg);
            		}
            	}
            }
        }
        
        Iterator eb = (updateBuildings.values()).iterator();
        while(eb.hasNext()) {
            ExternalBuilding extBldg = (ExternalBuilding)eb.next();
            Building newBldg = new Building();
            newBldg.setAbbreviation(extBldg.getAbbreviation());
            newBldg.setCoordinateX(extBldg.getCoordinateX());
            newBldg.setCoordinateY(extBldg.getCoordinateY());
            newBldg.setName(extBldg.getDisplayName());
            newBldg.setSession(currentSession);
            newBldg.setExternalUniqueId(extBldg.getExternalUniqueId());
            bldgDAO.save(newBldg);
        }
        
        return;
    }
    
	/*
	 * Update building information
	 * @param bldg (Building)
	 * @param extBldg (ExternalBuilding)
	 * @return update  (True if updates are made)
	 */
	private static boolean updateBldgInfo(Building bldg, ExternalBuilding extBldg) {
		
		boolean updated = false;
		
		if(!bldg.getAbbreviation().equals(extBldg.getAbbreviation())) {
			bldg.setAbbreviation(extBldg.getAbbreviation());
			updated = true;
		}
		if(!bldg.getName().equals(extBldg.getDisplayName())) {
			bldg.setName(extBldg.getDisplayName());
			updated = true;
		}
		if (!ToolBox.equals(bldg.getCoordinateX(), extBldg.getCoordinateX())) {
			bldg.setCoordinateX(extBldg.getCoordinateX());
			updated = true;
		}
		if (!ToolBox.equals(bldg.getCoordinateY(), extBldg.getCoordinateY())) {
			bldg.setCoordinateY(extBldg.getCoordinateY());
			updated = true;
		}
		
		return updated;
	}
	
	/*
	 * Check if building can be deleted
	 * @param bldg
	 * @return boolean  (True if building can be deleted)
	 */
	private static boolean checkBuildingDelete(Building bldg) {
		
		boolean result = false;;
		
		List rooms = (new RoomDAO()).getQuery(
				"from Room as rm " + 
				"where rm.building.uniqueId = " + (bldg.getUniqueId()).longValue()).
				list();
		
		if(rooms.isEmpty()) {
			result = true;
		}
		return result;
	}
	
	public Object clone() {
		Building b = new Building();
		b.setAbbreviation(getAbbreviation());
		b.setCoordinateX(getCoordinateX());
		b.setCoordinateY(getCoordinateY());
		b.setExternalUniqueId(getExternalUniqueId());
		b.setName(getName());
		b.setSession(getSession());
		return b;
	}

	public Building findSameBuildingInSession(Session newSession) throws Exception{
		if (newSession == null){
			return(null);
		}

		Building newBuilding = Building.findByBldgAbbv(this.getAbbreviation(), newSession.getUniqueId());
		if (newBuilding == null && this.getExternalUniqueId() != null){
			newBuilding = Building.findByExternalIdAndSession(getExternalUniqueId(), newSession);
		}
		return(newBuilding);

	}
	
	public static Building findByExternalIdAndSession(String externalId, Session session){
	    return findByExternalIdAndSession(externalId, session.getUniqueId());
	}

    public static Building findByExternalIdAndSession(String externalId, Long sessionId){
        BuildingDAO bDao = new BuildingDAO();
        List bldgs = bDao.getSession().createCriteria(Building.class)
            .add(Restrictions.eq("externalUniqueId", externalId))
            .add(Restrictions.eq("session.uniqueId", sessionId))
            .setCacheable(true).list();
            
        if (bldgs != null && bldgs.size() == 1){
            return((Building) bldgs.get(0));
        }

        return(null);

    }

    public static List<Building> findAll(Long sessionId) {
		return new BuildingDAO().getSession().createQuery(
				"select b from Building b where b.session.uniqueId=:sessionId order by b.abbreviation").
				setLong("sessionId", sessionId).setCacheable(true).list();
	}

    @Deprecated
    public String getHtmlHint() {
    	String hint = getName();
    	String minimap = ApplicationProperty.RoomHintMinimapUrl.value();
    	if (minimap != null && getCoordinateX() != null && getCoordinateY() != null) {
    		minimap = minimap
    				.replace("%x", getCoordinateX().toString())
    				.replace("%y", getCoordinateY().toString())
    				.replace("%n", getAbbreviation())
    				.replace("%i", getExternalUniqueId() == null ? "" : getExternalUniqueId());
	    	String apikey = ApplicationProperty.RoomMapStaticApiKey.value();
	    	if (apikey != null && !apikey.isEmpty()) {
	    		minimap += "&key=" + apikey;
		    	String secret = ApplicationProperty.RoomMapStaticSecret.value();
	    		if (secret != null && !secret.isEmpty()) {
	    			try {
	    				minimap += "&signature=" + new UrlSigner(secret).signRequest(minimap);
					} catch (Exception e) {}
	    		}
	    	}
	    	hint += "<br><img src=\\'" + minimap + "\\' border=\\'0\\' style=\\'border: 1px solid #9CB0CE;\\'/>";
    	}
    	return hint;
    }
}
