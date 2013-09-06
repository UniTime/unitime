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
import java.util.StringTokenizer;

import org.hibernate.HibernateException;
import org.unitime.timetable.model.base.BaseRoomFeature;
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

/*[CONSTRUCTOR MARKER END]*/

	public static String featureTypeDisplayString() {
		return "";		
	}
	
	public static List<GlobalRoomFeature> getAllGlobalRoomFeatures(Long sessionId) throws HibernateException {
		return (List<GlobalRoomFeature>)RoomFeatureDAO.getInstance().getSession().createQuery(
				"from GlobalRoomFeature rf where rf.session.uniqueId = :sessionId order by label"
				).setLong("sessionId", sessionId).setCacheable(true).list();
	}

	public static List<GlobalRoomFeature> getAllGlobalRoomFeatures(Long sessionId, Long featureTypeId) throws HibernateException {
		if (featureTypeId == null || featureTypeId < 0) {
			return (List<GlobalRoomFeature>)RoomFeatureDAO.getInstance().getSession().createQuery(
					"from GlobalRoomFeature rf where rf.session.uniqueId = :sessionId and rf.featureType is null order by label"
					).setLong("sessionId", sessionId).setCacheable(true).list();
		} else {
			return (List<GlobalRoomFeature>)RoomFeatureDAO.getInstance().getSession().createQuery(
					"from GlobalRoomFeature rf where rf.session.uniqueId = :sessionId and rf.featureType = :featureTypeId order by label"
					).setLong("sessionId", sessionId).setLong("featureTypeId", featureTypeId).setCacheable(true).list();
		}
	}

	public static List<GlobalRoomFeature> getAllGlobalRoomFeatures(Session session) throws HibernateException {
		return getAllGlobalRoomFeatures(session.getUniqueId());
	}
		
	public static List<DepartmentRoomFeature> getAllDepartmentRoomFeatures(Department dept) throws HibernateException {
		if (dept==null) return null;
		return (List<DepartmentRoomFeature>)RoomFeatureDAO.getInstance().getSession().createQuery(
				"from DepartmentRoomFeature rf where rf.department.uniqueId = :deptId order by label"
				).setLong("deptId", dept.getUniqueId()).setCacheable(true).list();
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
    	return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(rf.getUniqueId() == null ? -1 : rf.getUniqueId());
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
	    return getLabel() + (getFeatureType() == null ? "" : " (" + getFeatureType().getReference() + ")");
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
    
    public RoomFeature findSameFeatureInSession(Session session) {
		if (session == null) return null;
		List matchingFeatures = null;
		if (this instanceof DepartmentRoomFeature) {
			matchingFeatures = RoomFeatureDAO.getInstance().getSession().createQuery(
				"select distinct d from DepartmentRoomFeature d where d.department.session.uniqueId=:sessionId and d.label=:label and d.department.deptCode=:deptCode")
				.setLong("sessionId", session.getUniqueId().longValue())
				.setString("deptCode", ((DepartmentRoomFeature)this).getDeptCode())
				.setString("label", getLabel())
				.setCacheable(true).list();
		} else {
			matchingFeatures = RoomFeatureDAO.getInstance().getSession().createQuery(
			"select g from GlobalRoomFeature g where g.session.uniqueId=:sessionId and g.label=:label")
			.setLong("sessionId", session.getUniqueId().longValue())
			.setString("label", getLabel())
			.setCacheable(true).list();
		}
		return (matchingFeatures.size() == 1 ? (RoomFeature)matchingFeatures.get(0) : null);
    }
	
}
