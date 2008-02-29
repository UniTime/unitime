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
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.HibernateException;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.base.BaseDistributionType;
import org.unitime.timetable.model.dao.DistributionTypeDAO;




public class DistributionType extends BaseDistributionType implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public DistributionType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public DistributionType (Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public DistributionType (
		Long uniqueId,
		java.lang.String reference) {

		super (
			uniqueId,
			reference);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static Set findAll()  throws HibernateException {
		return findAll(false,false);
	}

	public static Set findAll(boolean instructorPrefOnly, boolean examPref) throws HibernateException {
    	return new TreeSet((new DistributionTypeDAO()).
			getSession().
			createQuery("select t from DistributionType t where t.examPref="+examPref+(instructorPrefOnly?" and t.instructorPref=true":"")).
			setCacheable(true).
			list());
	}
	
	public static Set findApplicable(HttpServletRequest request, boolean instructorPrefOnly, boolean examPref) throws Exception {
    	User user = Web.getUser(request.getSession());
    	Session session = Session.getCurrentAcadSession(user);
    	TimetableManager mgr = TimetableManager.getManager(user);
    	if (user.isAdmin()) return findAll(instructorPrefOnly, examPref);
    	TreeSet ret = new TreeSet();
    	for (Iterator i=findAll(instructorPrefOnly,examPref).iterator();i.hasNext();) {
    		DistributionType dt = (DistributionType)i.next();
    		Set depts = dt.getDepartments(session.getUniqueId());
    		if (depts.isEmpty()) {
    			ret.add(dt);
    		} else {
    			boolean contains = false;
    			for (Iterator j=mgr.getDepartments().iterator();!contains && j.hasNext();) {
    				Department d = (Department)j.next();
    				if (depts.contains(d)) contains = true; 
    			}
    			if (contains)
    				ret.add(dt);
    		}
    	}
    	return ret;
    }

	public static Set findApplicable(Department dept, boolean instructorPrefOnly, boolean examPref) throws Exception {
		if (dept==null) return findAll(instructorPrefOnly, examPref);
		TreeSet ret = new TreeSet();
    	for (Iterator i=findAll(instructorPrefOnly, examPref).iterator();i.hasNext();) {
    		DistributionType dt = (DistributionType)i.next();
    		Set depts = dt.getDepartments(dept.getSession().getUniqueId());
    		if (depts.isEmpty() || depts.contains(dept))
    			ret.add(dt);
    	}
    	return ret;
    }
	
	public boolean isApplicable(Department dept) {
		if (getDepartments().isEmpty()) return true;
		Set depts = getDepartments(dept.getSession().getUniqueId()); 
		return depts.isEmpty() || depts.contains(dept);
	}

    /** Request attribute name for available distribution types **/
    public static String DIST_TYPE_ATTR_NAME = "distributionTypeList";
	
    public boolean isAllowed(PreferenceLevel pref) {
    	return (getAllowedPref()==null || getAllowedPref().indexOf(PreferenceLevel.prolog2char(pref.getPrefProlog()))>=0); 
    }
    
    public Set getDepartments(Long sessionId) {
    	TreeSet ret = new TreeSet();
    	for (Iterator i=getDepartments().iterator();i.hasNext();) {
    		Department d = (Department)i.next();
    		if (sessionId==null || d.getSession().getUniqueId().equals(sessionId))
    			ret.add(d);
    	}
    	return ret;
    }
    
    public int compareTo(Object o) {
    	if (o==null || !(o instanceof DistributionType)) return -1;
    	DistributionType dt = (DistributionType)o;
    	int cmp = getLabel().compareTo(dt.getLabel());
    	if (cmp!=0) return cmp;
    	return getRequirementId().compareTo(dt.getRequirementId());
    }
    
    public String toString() {
        return getLabel();
    }
}