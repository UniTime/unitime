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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseTimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.Qualifiable;


/**
 * @author Tomas Muller
 */
public class TimetableManager extends BaseTimetableManager implements Comparable, Qualifiable {
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
		if (externalId == null || externalId.length() == 0){
			return(null);
		}
		TimetableManagerDAO tmDao = new TimetableManagerDAO();

		List mgrs = tmDao.getSession().createCriteria(TimetableManager.class)
			.add(Restrictions.eq("externalUniqueId", externalId))
			.setCacheable(true).list();
		if(mgrs != null && mgrs.size() == 1){
			return((TimetableManager) mgrs.get(0));
		} else
			return (null);		
	
	}
	
	public static TimetableManager getWithUniqueId(Long uniqueId){
		TimetableManagerDAO tmDao = new TimetableManagerDAO();
		return(tmDao.get(uniqueId));
	}
		
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
	
	public String getName() {
		return (getLastName()==null?"":getLastName()+", ")+
				(getFirstName()==null?"":getFirstName())+
			   (getMiddleName()==null?"": " " + getMiddleName());
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
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(m.getUniqueId() == null ? -1 : m.getUniqueId());
	}
	
	public String toString() { return getName(); }

    /** Request attribute name for manager list **/
    public static String MGR_LIST_ATTR_NAME = "managerList";  
    
   /**
	 * Retrieves all consent types in the database
	 * ordered by column last name, first name
     * @return Vector of TimetableManager objects
     */
    public static synchronized Vector getManagerList() {
        
        TimetableManagerDAO tdao = new TimetableManagerDAO();
        
        List l = tdao.findAll(Order.asc("lastName"), Order.asc("firstName"));
        if (l!=null)
            return new Vector(l);
        
        return null;
    }
    
    public Roles getPrimaryRole() {
        for (Iterator i=getManagerRoles().iterator();i.hasNext();) {
            ManagerRole role = (ManagerRole)i.next();
            if (role.isPrimary()) return role.getRole();
        }
        if (getManagerRoles().size()==1) return ((ManagerRole)getManagerRoles().iterator().next()).getRole();
        return null;
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
		return getExternalUniqueId();
	}

	@Override
	public String getQualifierLabel() {
		return getName();
	}
}
