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
package org.unitime.timetable.server.administration.session;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.admin.TimetableManagersPage.ManagerRoleInterface;
import org.unitime.timetable.gwt.client.admin.TimetableManagersPage.TimetableManagerEditRequest;
import org.unitime.timetable.gwt.client.admin.TimetableManagersPage.TimetableManagerEditResponse;
import org.unitime.timetable.gwt.client.admin.TimetableManagersPage.TimetableManagerInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.RolesDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

@GwtRpcImplements(TimetableManagerEditRequest.class)
public class TimetableManagerEditBackend implements GwtRpcImplementation<TimetableManagerEditRequest, TimetableManagerEditResponse> {
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public TimetableManagerEditResponse execute(TimetableManagerEditRequest request, SessionContext context) {
		context.checkPermission(Right.TimetableManagers);
		
		switch(request.getOperation()) {
		case ADD:
			context.checkPermission(Right.TimetableManagerAdd);
			TimetableManagerEditResponse addResponse = new TimetableManagerEditResponse();
			addResponse.setSessionId(context.getUser().getCurrentAcademicSessionId());
			addResponse.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			addResponse.setManager(new TimetableManagerInterface());
			
			setupLookups(context, addResponse, null);
			
			return addResponse;
		case EDIT:
			context.checkPermission(request.getManagerId(), Right.TimetableManagerEdit);
			TimetableManagerEditResponse response = new TimetableManagerEditResponse();
			response.setSessionId(context.getUser().getCurrentAcademicSessionId());
			response.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			TimetableManagerInterface manager = new TimetableManagerInterface();
			manager.setManagerId(request.getManagerId());
			response.setManager(manager);
			response.setCanDelete(context.hasPermission(request.getManagerId(), Right.TimetableManagerDelete));
			
			NameFormat nameFormat = NameFormat.fromReference(context.getUser().getProperty(UserProperty.NameFormat));
			
			TimetableManager mgr = TimetableManagerDAO.getInstance().get(manager.getManagerId());
			manager.setExternalId(mgr.getExternalUniqueId());
			manager.setFirstName(mgr.getFirstName());
			manager.setMiddleName(mgr.getMiddleName());
			manager.setLastName(mgr.getLastName());
			manager.setAcadTitle(mgr.getAcademicTitle());
			manager.setEmail(mgr.getEmailAddress());
			manager.setFormattedName(nameFormat.format(mgr));
			
			for (ManagerRole role: mgr.getManagerRoles())
				manager.addManagerRole(role.getRole().getRoleId(), Boolean.TRUE.equals(role.isPrimary()), !Boolean.FALSE.equals(role.getReceiveEmails()));
			for (Department dept: mgr.getDepartments())
				if (dept.getSessionId().equals(response.getSessionId()))
					manager.addDepartmentId(dept.getUniqueId());
			for (SolverGroup sg: mgr.getSolverGroups())
				if (sg.getSession().getUniqueId().equals(response.getSessionId()))
					manager.addSolverGroupId(sg.getUniqueId());
			setupLookups(context, response, mgr);
			
			return response;
		case DELETE:
			context.checkPermission(request.getManagerId(), Right.TimetableManagerDelete);
			
			deleteManager(context, request.getManagerId());
			return null;
			
		case SAVE:
			if (request.getManagerId() == null)
				context.checkPermission(Right.TimetableManagerAdd);
			else
				context.checkPermission(request.getManagerId(), Right.TimetableManagerEdit);

			TimetableManager other = TimetableManager.findByExternalId(request.getManager().getExternalId());
        	if (other!=null && !other.getUniqueId().equals(request.getManagerId()))
        		throw new GwtRpcException(MSG.errorManagerDuplicate());
			
			TimetableManagerEditResponse saveResponse = new TimetableManagerEditResponse();
			saveResponse.setManager(new TimetableManagerInterface());
			saveResponse.getManager().setManagerId(saveManager(context, request.getManager()));
			saveResponse.setSessionId(context.getUser().getCurrentAcademicSessionId());
			saveResponse.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			
			return saveResponse;
		}
		return null;
	}
	
    private boolean sameDepartments(TimetableManager mgr, Long s1, Long s2) {
    	TreeSet<String> d1 = new TreeSet<String>();
    	TreeSet<String> d2 = new TreeSet<String>();
    	for (Department d: mgr.getDepartments()) {
    		if (s1.equals(d.getSessionId())) d1.add(d.getDeptCode());
    		if (s2.equals(d.getSessionId())) d2.add(d.getDeptCode());
    	}
    	return d1.equals(d2);
    }
    
    private boolean sameSolverGroups(TimetableManager mgr, Long s1, Long s2) {
    	TreeSet<String> d1 = new TreeSet<String>();
    	TreeSet<String> d2 = new TreeSet<String>();
    	for (SolverGroup d: mgr.getSolverGroups()) {
    		if (s1.equals(d.getSession().getUniqueId())) d1.add(d.getAbbv());
    		if (s2.equals(d.getSession().getUniqueId())) d2.add(d.getAbbv());
    	}
    	return d1.equals(d2);
    }
	
	private void setupLookups(SessionContext context, TimetableManagerEditResponse response, TimetableManager mgr) {
    	Long currentSessionId = response.getSessionId();
		for (Department dept: Department.findAll(context.getUser().getCurrentAcademicSessionId()))
			response.addDepartment(dept.getUniqueId(), dept.getLabel());
		
		for (SolverGroup sg: SolverGroup.findBySessionId(context.getUser().getCurrentAcademicSessionId()))
			response.addSolverGroup(sg.getUniqueId(), sg.getName());
		
		for (Roles role: Roles.findAll(true)) {
			if (response.getManager() != null && response.getManager().getManagerRole(role.getRoleId()) != null) {
				response.addRole(role.getRoleId(), role.getAbbv());
			} else {
				if (role.hasRight(Right.SessionIndependent) && !context.hasPermission(Right.SessionIndependent)) continue;
				if (!role.isManager() || !role.isEnabled()) continue;
				response.addRole(role.getRoleId(), role.getAbbv());
			}
		}    	
    	
        boolean past = true;
        for (Session session: SessionDAO.getInstance().getSession().createQuery(
        		"select s from Session s, Session z " +
        		"where z.uniqueId = :sessionId and s.academicInitiative = z.academicInitiative and (s.uniqueId = :sessionId or bitand(s.statusType.status, 7572918) > 0) " +
        		"order by s.sessionBeginDateTime", Session.class)
        		.setParameter("sessionId", currentSessionId)
        		.setCacheable(true).list()) {
        	response.addOtherSession(session.getUniqueId(), session.getLabel());
        	if (session.getUniqueId().equals(currentSessionId)) {
        		if (response.getManager() != null)
        			response.getManager().addSessionId(session.getUniqueId());
        		past = false;
        	} else if (response.getManager() != null) {
        		if (mgr == null && !past) {
        			response.getManager().addSessionId(session.getUniqueId());
        		} else if (mgr != null && sameDepartments(mgr, currentSessionId, session.getUniqueId()) && sameSolverGroups(mgr, currentSessionId, session.getUniqueId())) {
        			response.getManager().addSessionId(session.getUniqueId());
        		}
        	}
        }
    }
	
	protected void deleteManager(SessionContext context, Long managerId) {
        org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().getSession();
        Transaction tx = null;
        
        try {
        	tx = hibSession.beginTransaction();
            TimetableManager mgr = TimetableManagerDAO.getInstance().get(managerId, hibSession);
        	
        	ChangeLog.addChange(
                    hibSession, 
                    context, 
                    mgr, 
                    ChangeLog.Source.MANAGER_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    null);

           	for (ManagerRole mgrRole: mgr.getManagerRoles()) {
           	    hibSession.remove(mgrRole);
           	}
           	for (Department d: mgr.getDepartments()) {
           		d.getTimetableManagers().remove(mgr);
           		hibSession.merge(d);
           	}
           	for (SolverGroup sg: mgr.getSolverGroups()) {
           		sg.getTimetableManagers().remove(mgr);
           		hibSession.merge(sg);
           	}

            hibSession.remove(mgr);

           	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected Long saveManager(SessionContext context, TimetableManagerInterface manager) {
        Transaction tx = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        Long sessionId = context.getUser().getCurrentAcademicSessionId();
        Long ret = null;
        
        try {
            tx = hibSession.beginTransaction();
            
            TimetableManager mgr = null;
            if (manager.getManagerId() == null) {
            	mgr = new TimetableManager();
            	mgr.setManagerRoles(new HashSet<ManagerRole>());
            	mgr.setDepartments(new HashSet<Department>());
            	mgr.setSolverGroups(new HashSet<SolverGroup>());
            } else {
            	mgr = TimetableManagerDAO.getInstance().get(manager.getManagerId(), hibSession);
            }
            
            mgr.setFirstName(manager.getFirstName());
            mgr.setMiddleName(manager.getMiddleName());
            mgr.setLastName(manager.getLastName());
            mgr.setAcademicTitle(manager.getAcadTitle());
            mgr.setExternalUniqueId(manager.getExternalId());
            mgr.setEmailAddress(manager.getEmail());
            
            // Update Roles
            // Check if roles added or updated
            if (manager.hasManagerRoles())
            	for (ManagerRoleInterface r: manager.getManagerRoles()) {
                	// Check if role already exists
                	boolean found = false;
                	for (ManagerRole mr: mgr.getManagerRoles())
                		if (r.getRoleId().equals(mr.getRole().getRoleId())) {
                			found = true;
                			mr.setPrimary(r.isPrimary());
                			mr.setReceiveEmails(r.isReceiveEmails());
                			break;
                		}
                	// Role does not exist - add  
                	if (!found) {
                		ManagerRole mgrRole = new ManagerRole();
                		mgrRole.setRole(RolesDAO.getInstance().get(r.getRoleId(), hibSession));
        	       	    mgrRole.setTimetableManager(mgr);
        	       	    mgrRole.setPrimary(r.isPrimary());
        	       	    mgrRole.setReceiveEmails(r.isReceiveEmails());
        	       	    mgr.addToManagerRoles(mgrRole);
                	}
                }
            // Check if roles deleted
            for (Iterator<ManagerRole> j=mgr.getManagerRoles().iterator(); j.hasNext(); ) {
           	    ManagerRole mgrRole = j.next();
               	if (manager.getManagerRole(mgrRole.getRole().getUniqueId()) == null) j.remove();
           	}
            
            if (manager.getManagerId() == null)
            	hibSession.persist(mgr);
            else
            	hibSession.merge(mgr);
            ret = mgr.getUniqueId();	
            
            // Update departments
            // Check if depts added or updated
            if (manager.hasDepartmentIds())
            	for (Long departmentId: manager.getDepartmentIds()) {
            		boolean found = false;
            		for (Department dept: mgr.getDepartments()) {
            			if (dept.getUniqueId().equals(departmentId)) { found = true; break; }
            		}
            		if (!found) {
            			Department dept = DepartmentDAO.getInstance().get(departmentId, hibSession);
            			mgr.addToDepartments(dept);
            			dept.addToTimetableManagers(mgr);
                   	    hibSession.merge(dept);
            		}
                }
            // Check if depts deleted
            for (Iterator<Department> j=mgr.getDepartments().iterator(); j.hasNext(); ) {
           	    Department dept = j.next();
           	    if (!dept.getSessionId().equals(sessionId)) continue; //SKIP DEPARTMENTS OF DIFFERENT SESSIONS!!!
           	    if (!manager.hasDepartmentId(dept.getUniqueId())) {
               	    j.remove();
               	    dept.getTimetableManagers().remove(mgr);
               	    hibSession.merge(dept);
               	}
           	}
            
            // Update solver groups
            // Check if solver group added or updated
            if (manager.hasSolverGroupIds())
            	for (Long solverGroupId: manager.getSolverGroupIds()) {
            		boolean found = false;
            		for (SolverGroup sg: mgr.getSolverGroups()) {
            			if (sg.getUniqueId().equals(solverGroupId)) { found = true; break; }
            		}
            		if (!found) {
            			SolverGroup sg = SolverGroupDAO.getInstance().get(solverGroupId, hibSession);
            			mgr.addToSolverGroups(sg);
            			sg.addToTimetableManagers(mgr);
                   	    hibSession.merge(sg);
            		}
            	}
            // Check if depts deleted
            for (Iterator<SolverGroup> j=mgr.getSolverGroups().iterator(); j.hasNext(); ) {
           		SolverGroup sg = j.next();
           		if (!sg.getSession().getUniqueId().equals(sessionId)) continue; //SKIP DEPARTMENTS OF DIFFERENT SESSIONS!!!
           		if (!manager.hasSolverGroupId(sg.getUniqueId())) {
           			j.remove();
               	    sg.getTimetableManagers().remove(mgr);
               	    hibSession.merge(sg);
           		}
            }
            
            if (manager.hasSessionIds()) {
            	HashSet<Department> departments = new HashSet<Department>();
            	for (Department d: mgr.getDepartments())
            		if (d.getSession().getUniqueId().equals(sessionId)) departments.add(d);
            	HashSet<SolverGroup>  solverGroups = new HashSet<SolverGroup>();
            	for (SolverGroup sg: mgr.getSolverGroups())
            		if (sg.getSession().getUniqueId().equals(sessionId)) solverGroups.add(sg);
            	
            	for (Long otherSessionId: manager.getSessionIds()) {
            		if (otherSessionId.equals(sessionId)) continue;
            		HashSet<Department> mgrDepts = new HashSet<Department>(mgr.getDepartments());
           			for (Department dept: departments) {
           	       	    Department other = dept.findSameDepartmentInSession(otherSessionId);
           	       	    if (other != null && !mgrDepts.remove(other)) {
           	       	    	mgr.getDepartments().add(other);
           	       	    	other.getTimetableManagers().add(mgr);
           	       	    	hibSession.merge(other);
           	       	    }
           			}
           			for (Department other: mgrDepts) {
           				if (!other.getSessionId().equals(otherSessionId)) continue;
           				mgr.getDepartments().remove(other);
       	       	    	other.getTimetableManagers().remove(mgr);
       	       	    	hibSession.merge(other);
           			}
           			HashSet<SolverGroup> mgrSolverGrs = new HashSet<SolverGroup>(mgr.getSolverGroups());
           			for (SolverGroup sg: solverGroups) {
           				SolverGroup other = SolverGroup.findBySessionIdAbbv(otherSessionId, sg.getAbbv());
           				if (other != null && !mgrSolverGrs.remove(other)) {
           					mgr.getSolverGroups().add(other);
           					other.getTimetableManagers().add(mgr);
           		    		hibSession.merge(other);
           				}
           	       	}
           			for (SolverGroup other: mgrSolverGrs) {
           				if (!other.getSession().getUniqueId().equals(otherSessionId)) continue;
           				mgr.getSolverGroups().remove(other);
       	       	    	other.getTimetableManagers().remove(mgr);
       	       	    	hibSession.merge(other);
           			}
            	}
            }
            
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    mgr, 
                    ChangeLog.Source.MANAGER_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    null);
            
            tx.commit() ;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
        return ret;
	}

}
