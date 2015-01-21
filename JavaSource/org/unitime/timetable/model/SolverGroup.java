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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.base.BaseSolverGroup;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.UserQualifier;
import org.unitime.timetable.security.rights.Right;



/**
 * @author Tomas Muller
 */
public class SolverGroup extends BaseSolverGroup implements Comparable, Qualifiable {
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
    
    public static Set<SolverGroup> findBySessionId(Long sessionId) {
    	return new TreeSet<SolverGroup>(
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
    	return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(sg.getUniqueId() == null ? -1 : sg.getUniqueId());
    }
    
    public Solution getCommittedSolution() {
    	if (getSolutions() == null) return null;
    	for (Iterator i=getSolutions().iterator();i.hasNext();) {
    		Solution s = (Solution)i.next();
    		if (s.isCommited().booleanValue()) return s;
    	}
    	return null;
    }
    
    public String toString() {
    	return getName();
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

	@Override
	public Serializable getQualifierId() {
		return getUniqueId();
	}

	@Override
	public String getQualifierType() {
		return getClass().getSimpleName();
	}

	@Override
	public String getQualifierReference() {
		return getAbbv();
	}

	@Override
	public String getQualifierLabel() {
		return getName();
	}
	
	public static TreeSet<SolverGroup> getUserSolverGroups(UserContext user) {
		TreeSet<SolverGroup> solverGroups = new TreeSet<SolverGroup>();
		if (user == null || user.getCurrentAuthority() == null) return solverGroups;
		if (user.getCurrentAuthority().hasRight(Right.DepartmentIndependent))
			solverGroups.addAll(SolverGroup.findBySessionId(user.getCurrentAcademicSessionId()));
		else
			for (UserQualifier q: user.getCurrentAuthority().getQualifiers("SolverGroup"))
				solverGroups.add(SolverGroupDAO.getInstance().get((Long)q.getQualifierId()));
		return solverGroups;
	}
	
}
