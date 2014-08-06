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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.NaturalOrderComparator;
import org.unitime.timetable.model.base.BaseSubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.UserContext;

/**
 * @author Tomas Muller, Heston Fernandes
 */
public class SubjectArea extends BaseSubjectArea implements Comparable<SubjectArea> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3256439188198207794L;
	
	private static HashMap subjectAreas = new HashMap(150);
	private static List subjChanges = null;
	
    /** Request attribute name for available subject areas **/
    public static String SUBJ_AREA_ATTR_NAME = "subjectAreaList";
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public SubjectArea () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SubjectArea (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	/**
	 * Retrieves all subject areas academic session
	 * @param sessionId academic session
	 * @return List of SubjectArea objects
	 */
	public static List getSubjectAreaList(Long sessionId) 
			throws HibernateException {
	    
	    SubjectAreaDAO subjDAO = new SubjectAreaDAO();
	    Session hibSession = subjDAO.getSession();
	    List subjs = hibSession.createCriteria(SubjectArea.class)
				    .add(Restrictions.eq("session.uniqueId", sessionId))
				    .list();
		return subjs;
	}
	
	/**
	 * Load subject abbreviation changes
	 * @param sessionId
	 */
	public static void loadSubjAbbvChanges(Long sessionId) {
		
		subjChanges = 
			SubjectHistory.getSubjectHistoryList(sessionId);
	}
	
	/**
	 * Load Subject Areas
	 */
	public static void loadSubjectAreas(Long sessionId) {
		
		List subjAreas = getSubjectAreaList(sessionId);
		
		for(int i = 0; i < subjAreas.size(); i++) {
			SubjectArea subjArea = (SubjectArea)subjAreas.get(i);
			String subjAreaAbbreviation = 
				subjArea.getSubjectAreaAbbreviation();
			subjectAreas.put(subjAreaAbbreviation, subjArea);
		}
	}

	/**
	 * Get the Subject Area
	 * @param subjectAreaAbbr
	 * @return SubjectArea
	 */
	public static SubjectArea getSubjectArea(String subjectAreaAbbr) {
		
		return (SubjectArea)subjectAreas.get(subjectAreaAbbr);
	}
	
	/**
	 * Retrieve a subject area for a given abbreviation and academic session
	 * @param sessionId
	 * @param subjectAreaAbbr
	 * @return null if no matches found
	 */
	public static SubjectArea findByAbbv (Long sessionId, String subjectAreaAbbr) {
	    SubjectAreaDAO subjDAO = new SubjectAreaDAO();
	    Session hibSession = subjDAO.getSession();
	    List subjs = hibSession.createCriteria(SubjectArea.class)
				    .add(Restrictions.eq("session.uniqueId", sessionId))
				    .add(Restrictions.eq("subjectAreaAbbreviation", subjectAreaAbbr))
				    .list();
	    
	    if (subjs==null || subjs.size()==0)
	    	return null;
	    
		return (SubjectArea)subjs.get(0);
	}
	
	/**
	 * Get the current (updated) subject area abbreviation
	 * @param subjectAreaAbbr
	 * @return SubjectArea
	 */
	 public static SubjectArea getUpdatedSubjectArea (
			 String subjectAreaAbbr) {
		 
		 String sa = subjectAreaAbbr;
		 
		 for(int i = 0; i < subjChanges.size(); i++) {
			 SubjectHistory sh = 
				 (SubjectHistory)subjChanges.get(i);
			 if(sa.equalsIgnoreCase(sh.getOldValue())) {
				 sa = sh.getNewValue();
			 }
		 }
		 
		 return getSubjectArea(sa);
	 }
	 
	public ArrayList getManagers() {
		if (getDepartment() != null){
			ArrayList al = new ArrayList();
			al.addAll(getDepartment().getTimetableManagers());
			return(al);
		} else {
			return(new ArrayList());
		}
	}

	public int compareTo(SubjectArea s) {
		int cmp = new NaturalOrderComparator().compare(
				getSubjectAreaAbbreviation() == null ? "" : getSubjectAreaAbbreviation(),
				s.getSubjectAreaAbbreviation() == null ? "" : s.getSubjectAreaAbbreviation());
		if (cmp != 0) return cmp;
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(s.getUniqueId() == null ? -1 : s.getUniqueId());
	}

	public String toString() {
	    return this.getSubjectAreaAbbreviation() + " - " + this.getTitle();
	}
	
	public Long getSessionId(){
		if (this.getSession() != null) {
			return(this.getSession().getUniqueId());
		} else {
			return(null);
		}
	}
	
	public Object clone(){
		SubjectArea newSubjectArea = new SubjectArea();
		newSubjectArea.setDepartment(getDepartment());
		newSubjectArea.setExternalUniqueId(getExternalUniqueId());
		newSubjectArea.setTitle(getTitle());
		newSubjectArea.setSession(getSession());
		newSubjectArea.setSubjectAreaAbbreviation(getSubjectAreaAbbreviation());
		return(newSubjectArea);
	}
	
	public SubjectArea findSameSubjectAreaInSession(org.unitime.timetable.model.Session session){
		String query = "select sa from SubjectArea sa where sa.department.session.uniqueId = " + session.getUniqueId().toString();
		query += " and sa.subjectAreaAbbreviation = '" + this.getSubjectAreaAbbreviation() + "'";
		SubjectAreaDAO saDao = new SubjectAreaDAO();
		List l = saDao.getQuery(query).list();
		if(l != null && l.size() == 1){
			return((SubjectArea) l.get(0));
		} else {
			return(null);
		}
	}

	/**
	 * Check if a subject area has offered classes
	 * @return
	 */
	public boolean hasOfferedCourses() {
		Set courses = getCourseOfferings();
		for (Object co: courses) {
			if ( !((CourseOffering)co).getInstructionalOffering().isNotOffered())
				return true;
		}
		return false;
	}
	
	public static TreeSet<SubjectArea> getAllSubjectAreas(Long sessionId) {
		return new TreeSet<SubjectArea>(
				SubjectAreaDAO.getInstance().getQuery("from SubjectArea where session.uniqueId = :sessionId")
				.setLong("sessionId", sessionId).setCacheable(true).list());
	}
	
	public static TreeSet<SubjectArea> getUserSubjectAreas(UserContext user) {
		return getUserSubjectAreas(user, true);
	}
	
	public static TreeSet<SubjectArea> getUserSubjectAreas(UserContext user, boolean allSubjectsIfExternalManager) {
		TreeSet<SubjectArea> subjectAreas = new TreeSet<SubjectArea>();
		if (user == null || user.getCurrentAuthority() == null) return subjectAreas;
		for (Department department: Department.getUserDepartments(user)) {
			if (department.isExternalManager() && allSubjectsIfExternalManager) {
				subjectAreas.addAll(department.getSession().getSubjectAreas());
				break;
			} else {
				subjectAreas.addAll(department.getSubjectAreas());
			}
		}
		return subjectAreas;
	}

}
