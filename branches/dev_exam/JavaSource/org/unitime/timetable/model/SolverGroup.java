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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseSolverGroup;
import org.unitime.timetable.model.dao.SolverGroupDAO;



public class SolverGroup extends BaseSolverGroup implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SolverGroup () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SolverGroup (Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public SolverGroup (
		Long uniqueId,
		java.lang.String name,
		java.lang.String abbv,
		org.unitime.timetable.model.Session session) {

		super (
			uniqueId,
			name,
			abbv,
			session);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public boolean isExternalManager(){
		for(Iterator i=getDepartments().iterator();i.hasNext();) {
			Department d = (Department)i.next();
			if (d.isExternalManager().booleanValue()) return true;
		}
		return false;
	}
	
    public Collection getClasses() {
    	Vector classes = new Vector(); 
    	for (Iterator i=getDepartments().iterator();i.hasNext();) {
    		Department d = (Department)i.next();
    		classes.addAll(d.getClasses());
    	}
    	return classes;
    }
    
	public Collection getNotAssignedClasses(Solution solution) {
    	Vector classes = new Vector(); 
    	for (Iterator i=getDepartments().iterator();i.hasNext();) {
    		Department d = (Department)i.next();
    		classes.addAll(d.getNotAssignedClasses(solution));
    	}
    	return classes;
	}
    public Set getDistributionPreferences() {
    	TreeSet prefs = new TreeSet();
    	for (Iterator i=getDepartments().iterator();i.hasNext();) {
    		Department d = (Department)i.next();
    		prefs.addAll(d.getDistributionPreferences());
    	}    	
    	return prefs;
    }
    
    public static Set findBySessionId(Long sessionId) {
    	return new TreeSet(
    			(new SolverGroupDAO()).
    			getSession().
    			createQuery("select sg from SolverGroup sg where sg.session.uniqueId=:sessionId").
    			setLong("sessionId", sessionId.longValue()).
    			setCacheable(true).list());
    }
    
    public static SolverGroup findBySessionIdName(Long sessionId, String name) {
    	List groups = (new SolverGroupDAO()).
			getSession().
			createQuery("select sg from SolverGroup sg where sg.session.uniqueId=:sessionId and sg.name=:name").
			setLong("sessionId", sessionId.longValue()).
			setString("name", name).
			setCacheable(true).
			list();
    	if (groups.isEmpty()) return null;
    	return (SolverGroup)groups.get(0);
    }
    
    public static SolverGroup findBySessionIdAbbv(Long sessionId, String abbv) {
    	List groups = (new SolverGroupDAO()).
			getSession().
			createQuery("select sg from SolverGroup sg where sg.session.uniqueId=:sessionId and sg.abbv=:abbv").
			setLong("sessionId", sessionId.longValue()).
			setString("abbv", abbv).
			setCacheable(true).
			list();
    	if (groups.isEmpty()) return null;
    	return (SolverGroup)groups.get(0);
    }

    public int compareTo(Object o) {
    	if (o==null || !(o instanceof SolverGroup)) return -1;
    	SolverGroup sg = (SolverGroup)o;
    	int cmp = getName().compareTo(sg.getName());
    	if (cmp!=0) return cmp;
    	return getUniqueId().compareTo(sg.getUniqueId());
    }
    
    public Solution getCommittedSolution() {
    	for (Iterator i=getSolutions().iterator();i.hasNext();) {
    		Solution s = (Solution)i.next();
    		if (s.isCommited().booleanValue()) return s;
    	}
    	return null;
    }
    
    public String toString() {
    	return getName();
    }
    
	public boolean canAudit() {
		for (Iterator j=getDepartments().iterator();j.hasNext();) {
			Department department = (Department)j.next();
			if (!department.effectiveStatusType().canAudit()) 
				return false;
		}
		return true;
	}
	
	public boolean canTimetable() {
		for (Iterator j=getDepartments().iterator();j.hasNext();) {
			Department department = (Department)j.next();
			if (!department.effectiveStatusType().canTimetable())
				return false;
		}
		return true;
	}
    
    public boolean canCommit(User user) {
        if (user.isAdmin()) return true;
        if (user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE)) return false;
        return canCommit();
    }

    public boolean canCommit() {
		for (Iterator j=getDepartments().iterator();j.hasNext();) {
			Department department = (Department)j.next();
			if (!department.effectiveStatusType().canCommit())
				return false;
		}
		return true;
	}
	
	public int getMinDistributionPriority() {
		int ret = Integer.MAX_VALUE;
		for (Iterator i=getDepartments().iterator();i.hasNext();) {
			Department d = (Department)i.next();
			ret = Math.min(ret, d.getDistributionPrefPriority().intValue());
		}
		return ret;
	}

	public int getMaxDistributionPriority() {
		int ret = Integer.MIN_VALUE;
		for (Iterator i=getDepartments().iterator();i.hasNext();) {
			Department d = (Department)i.next();
			ret = Math.max(ret, d.getDistributionPrefPriority().intValue());
		}
		return ret;
	}
	
	public Object clone() {
		SolverGroup sg = new SolverGroup();
		sg.setSession(getSession());
		sg.setAbbv(getAbbv());
		sg.setName(getName());
		return sg;
	}
	
}