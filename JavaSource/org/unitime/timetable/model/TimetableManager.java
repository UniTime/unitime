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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.base.BaseTimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.NameInterface;


/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
@Table(name = "timetable_manager")
public class TimetableManager extends BaseTimetableManager implements Comparable, Qualifiable, NameInterface {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public TimetableManager () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public TimetableManager (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static TimetableManager findByExternalId(String externalId){
		if (externalId == null || externalId.length() == 0) return null;
		return TimetableManagerDAO.getInstance().getSession()
				.createQuery("from TimetableManager where externalUniqueId = :externalId", TimetableManager.class)
				.setParameter("externalId", externalId)
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
	}
	
	public static TimetableManager getWithUniqueId(Long uniqueId){
		TimetableManagerDAO tmDao = TimetableManagerDAO.getInstance();
		return(tmDao.get(uniqueId));
	}
		
	@Transient
	public boolean isExternalManager(){
		boolean isExternal = false; 
		Department d = null;
		for(Iterator it = this.getDepartments().iterator(); it.hasNext(); ){
			d = (Department) it.next();
			if (d.isExternalManager().booleanValue()){
				isExternal = true;
			}
		}
		return(isExternal);
	}
	
	public Set departmentsForSession(Long sessionId){
		HashSet l = new HashSet();
		if (this.getDepartments() != null){
			Department d = null;
			for (Iterator it = this.getDepartments().iterator(); it.hasNext();){
				d = (Department) it.next();
				if (d.getSessionId().equals(sessionId)){
					l.add(d);
				}
			}
		}
		return(l);
	}
	
	public Set sessionsCanManage(){
		TreeSet sessions = new TreeSet();
		Department dept = null;
		for(Iterator it = getDepartments().iterator(); it.hasNext();){
			dept = (Department) it.next();
			sessions.add(dept.getSession());
		}
		return(sessions);
	}
	
	public boolean hasLastName() {
		return getLastName() != null && !getLastName().isEmpty();
	}
	
	public boolean hasFirstName() {
		return getFirstName() != null && !getFirstName().isEmpty();
	}
	
	public boolean hasMiddleName() {
		return getMiddleName() != null && !getMiddleName().isEmpty();
	}
	
	@Transient
	public String getName() {
		return (hasLastName() ? getLastName() : "") +
				(hasFirstName() || hasMiddleName() ? "," : "") +
				(hasFirstName() ? " " + getFirstName() : "") +
				(hasMiddleName() ? " " + getMiddleName() : "");
	}
	
    public String getName(String instructorNameFormat) {
    	return NameFormat.fromReference(instructorNameFormat).format(this);
    }

    public Collection getClasses(Session session) {
    	Vector classes = new Vector(); 
    	for (Iterator i=departmentsForSession(session.getUniqueId()).iterator();i.hasNext();) {
    		Department d = (Department)i.next();
    		classes.addAll(d.getClasses());
    	}
    	return classes;
    }
	public Collection getNotAssignedClasses(Solution solution) {
    	Vector classes = new Vector(); 
    	for (Iterator i=departmentsForSession(solution.getSession().getUniqueId()).iterator();i.hasNext();) {
    		Department d = (Department)i.next();
    		classes.addAll(d.getNotAssignedClasses(solution));
    	}
    	return classes;
	}
    
    public Set getDistributionPreferences(Session session) {
    	TreeSet prefs = new TreeSet();
    	for (Iterator i=departmentsForSession(session.getUniqueId()).iterator();i.hasNext();) {
    		Department d = (Department)i.next();
    		prefs.addAll(d.getDistributionPreferences());
    	}    	
    	return prefs;
    }	
    
    public Set getSolverGroups(Session session) {
    	TreeSet groups = new TreeSet();
    	for (Iterator i=getSolverGroups().iterator();i.hasNext();) {
    		SolverGroup g = (SolverGroup)i.next();
    		if (session.equals(g.getSession())) groups.add(g);
    	}
    	return groups;
    }
    
	@Transient
	public String getShortName() {
        StringBuffer sb = new StringBuffer();
        if (getFirstName()!=null && getFirstName().length()>0) {
            sb.append(getFirstName().substring(0,1).toUpperCase());
            sb.append(". ");
        }
        if (getLastName()!=null && getLastName().length()>0) {
            sb.append(getLastName().substring(0,1).toUpperCase());
            sb.append(getLastName().substring(1,Math.min(10,getLastName().length())).toLowerCase().trim());
        }
        return sb.toString();
	}
	
	public int compareTo(Object o) {
		if (o==null || !(o instanceof TimetableManager)) return -1;
		TimetableManager m = (TimetableManager)o;
		int cmp = getName().compareTo(m.getName());
		if (cmp!=0) return cmp;
		return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(m.getUniqueId() == null ? -1 : m.getUniqueId());
	}
	
	public String toString() { return getName(); }

    /** Request attribute name for manager list **/
    public static String MGR_LIST_ATTR_NAME = "managerList";  
    
   /**
	 * Retrieves all consent types in the database
	 * ordered by column last name, first name
     * @return Vector of TimetableManager objects
     */
	@Transient
    public static List<TimetableManager> getManagerList() {
		return TimetableManagerDAO.getInstance().getSession().createQuery(
				"from TimetableManager order by lastName, firstName", TimetableManager.class)
				.setCacheable(true).list();
    }
    
	@Transient
    public Roles getPrimaryRole() {
        for (Iterator i=getManagerRoles().iterator();i.hasNext();) {
            ManagerRole role = (ManagerRole)i.next();
            if (role.isPrimary()) return role.getRole();
        }
        if (getManagerRoles().size()==1) return ((ManagerRole)getManagerRoles().iterator().next()).getRole();
        return null;
    }

	@Override
	@Transient
	public Serializable getQualifierId() {
		return getUniqueId();
	}

	@Override
	@Transient
	public String getQualifierType() {
		return getClass().getSimpleName();
	}

	@Override
	@Transient
	public String getQualifierReference() {
		return getExternalUniqueId();
	}

	@Override
	@Transient
	public String getQualifierLabel() {
		return getName();
	}
}
