/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseTimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;




public class TimetableManager extends BaseTimetableManager implements Comparable {
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

	/**
	 * Constructor for required fields
	 */
	public TimetableManager (
		java.lang.Long uniqueId,
		java.lang.String firstName,
		java.lang.String lastName) {

		super (
			uniqueId,
			firstName,
			lastName);
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
	
	public static TimetableManager getManager(User user){
	    if (user==null) return null;
		String idString = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		if (idString != null && idString.length() > 0){
			return(getWithUniqueId(new Long(idString)));
		} else {
			return(findByExternalId(user.getId()));	
		}
	}
	
	public static Set getSubjectAreas(User user) throws Exception {
	    Set saList = new TreeSet();
	    Session session = Session.getCurrentAcadSession(user);
	    if (user.isAdmin() || Roles.VIEW_ALL_ROLE.equals(user.getCurrentRole()) || Roles.EXAM_MGR_ROLE.equals(user.getCurrentRole())){
	    	return(session.getSubjectAreas());
	    }
		TimetableManager tm = TimetableManager.getManager(user);
	    if (tm != null && tm.getDepartments() != null){
	    	Department d = null;
	    	for (Iterator it = tm.departmentsForSession(session.getUniqueId()).iterator(); it.hasNext();){
	    		d = (Department) it.next();
	    		if (d.isExternalManager().booleanValue()){
	    			return(session.getSubjectAreas());
	    		} else {
	    			saList.addAll(d.getSubjectAreas());
	    		}
	    	}
	    }
        return saList;
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
	
	public boolean canAudit(Session session, User user) {
		if (user.isAdmin()) return true;
		if (user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE)) return false;
		if (user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE)) return false;
		for (Iterator i=getSolverGroups().iterator();i.hasNext();) {
			SolverGroup solverGroup = (SolverGroup)i.next();
			if (!solverGroup.getSession().equals(session)) continue;
			if (solverGroup.canAudit()) return true;
		}
		return false;
	}
	
	public boolean canSeeTimetable(Session session, User user) {
		if (user.isAdmin()) return true;
		if (user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE)) return true;
		if (user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE)) return true;
		if (canDoTimetable(session, user)) return true;
		for (Iterator i=getDepartments().iterator();i.hasNext();) {
			Department department = (Department)i.next();
			if (department.getSolverGroup()!=null && !department.getSolverGroup().getSolutions().isEmpty()) return true;
		}
		for (Iterator i=Department.findAllExternal(session.getUniqueId()).iterator();i.hasNext();) {
			Department department = (Department)i.next();
			if (department.getSolverGroup()!=null && department.getSolverGroup().getCommittedSolution()!=null) return true;
		}
		return false;
	}
	
	public boolean canDoTimetable(Session session, User user) {
		if (user.isAdmin()) return true;
		if (user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE)) return false;
		if (user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE)) return false;
		for (Iterator i=getSolverGroups().iterator();i.hasNext();) {
			SolverGroup solverGroup = (SolverGroup)i.next();
			if (!solverGroup.getSession().equals(session)) continue;
			if (solverGroup.canTimetable()) return true;
		}
		return false;
	}
	
	public boolean canEditExams(Session session, User user) {
        //admin
        if (Roles.ADMIN_ROLE.equals(user.getCurrentRole())) 
            return true;
        
        //timetable manager 
        if (Roles.DEPT_SCHED_MGR_ROLE.equals(user.getCurrentRole()))
            return session.getStatusType().canExamEdit();
        
        //exam manager
        if (Roles.EXAM_MGR_ROLE.equals(user.getCurrentRole()))
            return session.getStatusType().canExamTimetable();
        
        return false;
	}
	
    public boolean canSeeCourses(Session session, User user) {
        //can edit -> can view
        if (canSeeTimetable(session, user)) return true;
        
        //admin or exam manager
        if (Roles.ADMIN_ROLE.equals(user.getCurrentRole()) || Roles.VIEW_ALL_ROLE.equals(user.getCurrentRole()) || Roles.EXAM_MGR_ROLE.equals(user.getCurrentRole())) return true;
        
        if (Roles.DEPT_SCHED_MGR_ROLE.equals(user.getCurrentRole())) {
            TimetableManager mgr = getManager(user);
            for (Iterator i=mgr.getDepartments().iterator();i.hasNext();) {
                Department d = (Department)i.next();
                if (d.isExternalManager() && d.effectiveStatusType().canManagerView()) return true;
                if (!d.isExternalManager() && d.effectiveStatusType().canOwnerView()) return true;
            }
        }
        
        return false;
    }

	
	public boolean canSeeExams(Session session, User user) {
        //can edit -> can view
        if (canEditExams(session, user)) return true;
        
        //admin or exam manager
        if (Roles.ADMIN_ROLE.equals(user.getCurrentRole()) || Roles.EXAM_MGR_ROLE.equals(user.getCurrentRole())) 
            return true;
        
        //timetable manager or view all 
        if (Roles.DEPT_SCHED_MGR_ROLE.equals(user.getCurrentRole()) || Roles.VIEW_ALL_ROLE.equals(user.getCurrentRole()))
            return session.getStatusType().canExamView();
        
        return false;
    }

    public boolean canTimetableExams(Session session, User user) {
        if (Roles.ADMIN_ROLE.equals(user.getCurrentRole())) 
            return true;
        
        if (Roles.EXAM_MGR_ROLE.equals(user.getCurrentRole()))
            return session.getStatusType().canExamTimetable();
        
        return false;
    }

    public boolean canSectionStudents(Session session, User user) {
        if (Roles.ADMIN_ROLE.equals(user.getCurrentRole())) 
            return true;
        
        return false;
    }

    public boolean hasASolverGroup(Session session, User user) {
		if (user.isAdmin() || user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE) || user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE)) {
			return !SolverGroup.findBySessionId(session.getUniqueId()).isEmpty();
		} else {
			return !getSolverGroups(session).isEmpty();
		}
	}

    //needs to be implemented
    public static boolean canSeeEvents (User user) {
    	for (RoomType roomType : RoomType.findAll()) {
    	    if (roomType.countManagableRooms()>0) return true;
    	}
    	return false;
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
		return getUniqueId().compareTo(m.getUniqueId());
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
        
        Vector orderList = new Vector();
        orderList.addElement(Order.asc("lastName"));
        orderList.addElement(Order.asc("firstName"));
        
        List l = tdao.findAll(orderList);
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
}
