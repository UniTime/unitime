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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.admin.SolverGroupsPage.SolverGroupEditRequest;
import org.unitime.timetable.gwt.client.admin.SolverGroupsPage.SolverGroupEditResponse;
import org.unitime.timetable.gwt.client.admin.SolverGroupsPage.SolverGroupInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

@GwtRpcImplements(SolverGroupEditRequest.class)
public class SolverGroupEditBackend implements GwtRpcImplementation<SolverGroupEditRequest, SolverGroupEditResponse> {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Override
	public SolverGroupEditResponse execute(SolverGroupEditRequest request, SessionContext context) {
		context.checkPermission(Right.SolverGroups);
		
		switch(request.getOperation()) {
		case ADD:
			SolverGroupEditResponse addResponse = new SolverGroupEditResponse();
			addResponse.setSessionId(context.getUser().getCurrentAcademicSessionId());
			addResponse.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			setupLookups(context, addResponse, null);
			return addResponse;
		case EDIT:
			SolverGroupEditResponse response = new SolverGroupEditResponse();
			response.setSessionId(context.getUser().getCurrentAcademicSessionId());
			response.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			
			SolverGroup group = SolverGroupDAO.getInstance().get(request.getSolverGroupId());
			SolverGroupInterface groupInterface = new SolverGroupInterface();
			groupInterface.setAbbreviation(group.getAbbv());
			groupInterface.setSolverGroupId(group.getUniqueId());
			groupInterface.setName(group.getName());
			for (Department dept: group.getDepartments())
				groupInterface.addDepartmentId(dept.getUniqueId());
			for (TimetableManager mgr: group.getTimetableManagers())
				groupInterface.addManagerId(mgr.getUniqueId());
			response.setSolverGroup(groupInterface);
			response.setCanDelete(group.getSolutions().isEmpty());
			response.setCanEditDepartments(group.getSolutions().isEmpty());
			
			setupLookups(context, response, group);
			return response;
		case SAVE:
			SolverGroup other = SolverGroup.findBySessionIdAbbv(context.getUser().getCurrentAcademicSessionId(), request.getSolverGroup().getAbbreviation());
			if (other!=null && !other.getUniqueId().equals(request.getSolverGroupId()))
				throw new GwtRpcException(MSG.errorAlreadyExists(request.getSolverGroup().getAbbreviation()));
			other = SolverGroup.findBySessionIdName(context.getUser().getCurrentAcademicSessionId(), request.getSolverGroup().getName());
			if (other!=null && !other.getUniqueId().equals(request.getSolverGroupId()))
				throw new GwtRpcException(MSG.errorAlreadyExists(request.getSolverGroup().getName()));
			
			SolverGroupEditResponse saveResponse = new SolverGroupEditResponse();
			saveResponse.setSolverGroup(new SolverGroupInterface());
			saveResponse.getSolverGroup().setSolverGroupId(saveSolverGroup(context, request.getSolverGroup()));
			saveResponse.setSessionId(context.getUser().getCurrentAcademicSessionId());
			saveResponse.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			
			return saveResponse;
		case DELETE:
			deleteSolverGroup(context, request.getSolverGroupId());
			return null;
		case DELETE_ALL:
			deleteAllSolverGroups(context);
			return null;
		case AUTO_SETUP:
			autoSetupSolverGroups(context);
			return null;
		}
		
		return null;
	}
	
	protected String managerName(TimetableManager mgr, NameFormat nameFormat, Long currentSessionId) {
		String ret = nameFormat.format(mgr);
		String depts = "";
		Roles primary = mgr.getPrimaryRole();
		int idx = 0;
    	for (Department d: new TreeSet<Department>(mgr.departmentsForSession(currentSessionId))) {
    		if (idx == 5) { depts += "..."; break; }
    		depts += (depts.isEmpty() ? "" : ", ") + d.getDeptCode();
    		idx ++;
    	}
    	if (!depts.isEmpty())
    		ret += " (" + (primary != null ? primary.getReference() + ": " : "") + depts + ")";
    	else if (primary != null)
    		ret += " (" + primary.getReference() + ")";
		return ret;
	}
	
	protected void setupLookups(SessionContext context, SolverGroupEditResponse response, SolverGroup group) {
		Long currentSessionId = response.getSessionId();
		if (group != null)
			for (Department dept: group.getDepartments())
				response.addDepartment(dept.getUniqueId(), dept.getLabel());
		for (Department dept: Department.findAll(currentSessionId)) {
			if (dept.getSolverGroup() == null && (!dept.getSubjectAreas().isEmpty() || dept.isExternalManager()))
				response.addDepartment(dept.getUniqueId(), dept.getLabel());
		}
		if (response.hasDepartments())
			Collections.sort(response.getDepartments());
		
		List<TimetableManager> managers = new ArrayList<TimetableManager>(
				TimetableManagerDAO.getInstance().getSession().createQuery(
				"select distinct m from TimetableManager m inner join m.departments d where " +
				"d.session.uniqueId = :sessionId and (d.externalManager = true or d.subjectAreas is not empty)",
				TimetableManager.class).setParameter("sessionId", currentSessionId).list());
		final NameFormat nameFormat = NameFormat.fromReference(context.getUser().getProperty(UserProperty.NameFormat));
		for (TimetableManager mgr: managers) {
			response.addManager(mgr.getUniqueId(), managerName(mgr, nameFormat, currentSessionId));
		}
		if (group != null)
			for (TimetableManager mgr: group.getTimetableManagers())
				if (response.getManager(mgr.getUniqueId()) == null)
					response.addManager(mgr.getUniqueId(), managerName(mgr, nameFormat, currentSessionId));
		if (response.hasManagers())
			Collections.sort(response.getManagers());
	}
	
	protected Long saveSolverGroup(SessionContext context, SolverGroupInterface groupInterface) {
		Transaction tx = null;
    	org.hibernate.Session hibSession = SolverGroupDAO.getInstance().getSession();
		Long ret = null;
        try {
        	tx = hibSession.beginTransaction();
        	
        	SolverGroup group = null;
        	if (groupInterface.getSolverGroupId() == null) {
        		group = new SolverGroup();
            	group.setAbbv(groupInterface.getAbbreviation());
            	group.setName(groupInterface.getName());
        		group.setDepartments(new HashSet<Department>());
        		group.setTimetableManagers(new HashSet<TimetableManager>());
        		group.setSolutions(new HashSet<Solution>());
        		group.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
        		hibSession.persist(group);
        	} else {
        		group = SolverGroupDAO.getInstance().get(groupInterface.getSolverGroupId(), hibSession);
            	group.setAbbv(groupInterface.getAbbreviation());
            	group.setName(groupInterface.getName());
        	}
        	ret = group.getUniqueId();
        	
        	Set<TimetableManager> remainingManagers = new HashSet<TimetableManager>(group.getTimetableManagers());
        	if (groupInterface.hasManagerIds())
        		for (Long mgrId: groupInterface.getManagerIds()) {
        			TimetableManager mgr = TimetableManagerDAO.getInstance().get(mgrId, hibSession);
        			if (!remainingManagers.remove(mgr)) {
        				group.addToTimetableManagers(mgr);
        				mgr.addToSolverGroups(group);
        				hibSession.merge(mgr);
        			}
        		}
        	for (TimetableManager mgr: remainingManagers) {
        		group.getTimetableManagers().remove(mgr);
        		mgr.getSolverGroups().remove(group);
        		hibSession.merge(mgr);
        	}
        	
        	Set<Department> remainingDepartments = new HashSet<Department>(group.getDepartments());
        	if (groupInterface.hasDepartmentIds())
        		for (Long deptId: groupInterface.getDepartmentIds()) {
        			Department dept = DepartmentDAO.getInstance().get(deptId, hibSession);
        			if (!remainingDepartments.remove(dept)) {
        				if (dept.getSolverGroup() != null)
        					throw new GwtRpcException("Department " + dept.getLabel() + " already belongs to " + dept.getSolverGroup().getName() + " solver group.");
        				dept.setSolverGroup(group);
        				group.addToDepartments(dept);
        				hibSession.merge(dept);
        			}
        		}
        	for (Department dept: remainingDepartments) {
        		group.getDepartments().remove(dept);
        		dept.setSolverGroup(null);
        		hibSession.merge(dept);
        	}
        	
        	hibSession.merge(group);
        	
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    group, 
                    ChangeLog.Source.SOLVER_GROUP_EDIT, 
                    groupInterface.getSolverGroupId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE, 
                    null, 
                    null);
        	
			if (tx!=null) tx.commit();
	    } catch (Exception e) {
	    	if (tx!=null) tx.rollback();
	    	throw e;
	    }
        return ret;
	}
	
	protected void deleteSolverGroup(SessionContext context, Long groupId) {
        org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().getSession();
        Transaction tx = null;
        
        try {
        	tx = hibSession.beginTransaction();
        	SolverGroup group = SolverGroupDAO.getInstance().get(groupId, hibSession);
        	
    		for (Department dept: group.getDepartments()) {
    			dept.setSolverGroup(null);
    			hibSession.merge(dept);
    		}
    		for (TimetableManager mgr: group.getTimetableManagers()) {
    			mgr.getSolverGroups().remove(group);
    			hibSession.merge(mgr);
    		}
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    group, 
                    ChangeLog.Source.SOLVER_GROUP_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    null);
    		hibSession.remove(group);
        	
           	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected void deleteAllSolverGroups(SessionContext context) {
        org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().getSession();
        Transaction tx = null;
        
        try {
        	tx = hibSession.beginTransaction();
        	
        	for (SolverGroup group: SolverGroup.findBySessionId(context.getUser().getCurrentAcademicSessionId())) {
        		if (!group.getSolutions().isEmpty()) continue;
        		
        		for (Department dept: group.getDepartments()) {
        			dept.setSolverGroup(null);
        			hibSession.merge(dept);
        		}
        		for (TimetableManager mgr: group.getTimetableManagers()) {
        			mgr.getSolverGroups().remove(group);
        			hibSession.merge(mgr);
        		}

                ChangeLog.addChange(
                        hibSession, 
                        context, 
                        group, 
                        ChangeLog.Source.SOLVER_GROUP_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        null, 
                        null);
        		hibSession.remove(group);
        	}
        	
        	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected void autoSetupSolverGroups(SessionContext context) {
        org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().getSession();
        Transaction tx = null;
        
        try {
        	tx = hibSession.beginTransaction();
        	
        	Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession);
        	
        	TreeSet<Department> allDepts = new TreeSet<Department>(new Comparator<Department>() {
        		public int compare(Department d1, Department d2) {
        			int cmp = -Double.compare(d1.getTimetableManagers().size(),d2.getTimetableManagers().size());
        			if (cmp!=0) return cmp;
        			return d1.getUniqueId().compareTo(d2.getUniqueId());
        		}
        	});
        	allDepts.addAll(session.getDepartments());
        	
        	for (Department d: allDepts) {
        		if (d.getSolverGroup()!=null) continue;
        		
        		if (d.isExternalManager().booleanValue()) {
        			SolverGroup sg = new SolverGroup();
        			sg.setAbbv(d.getExternalMgrAbbv());
        			sg.setName(d.getDeptCode()+" - "+d.getExternalMgrLabel().replaceAll(" Manager", ""));
        			sg.setSession(session);
        			sg.setTimetableManagers(new HashSet<TimetableManager>());
        			hibSession.persist(sg);
        			d.setSolverGroup(sg);
        			hibSession.merge(d);
    				for (TimetableManager mgr: d.getTimetableManagers()) {
    					mgr.getSolverGroups().add(sg);
    					sg.getTimetableManagers().add(mgr);
    					hibSession.merge(mgr);
    				}
    				ChangeLog.addChange(
    	                    hibSession, 
    	                    context, 
    	                    sg, 
    	                    ChangeLog.Source.SOLVER_GROUP_EDIT, 
    	                    ChangeLog.Operation.CREATE, 
    	                    null, 
    	                    null);
        		} else if (!d.getSubjectAreas().isEmpty() && !d.getTimetableManagers().isEmpty()) {
        			Set<Department> depts = null;
        			for (TimetableManager mgr: d.getTimetableManagers()) {
        				Set<Department> myDepts = mgr.departmentsForSession(session.getUniqueId());
        				if (depts==null) 
        					depts = new HashSet<Department>(myDepts);
        				else
        					depts.retainAll(myDepts);
        			}
    				for (Iterator<Department> j=depts.iterator();j.hasNext();) {
    					Department x = j.next();
    					if (x.getSolverGroup()!=null || x.getSubjectAreas().isEmpty())
    						j.remove();
    				}
        			if (!depts.isEmpty()) {
        				StringBuffer abbv = new StringBuffer();
        				StringBuffer name = new StringBuffer();
        				HashSet<TimetableManager> mgrs = new HashSet<TimetableManager>();
        				for (Department x: depts) {
        					mgrs.addAll(x.getTimetableManagers());
        					abbv.append(x.getShortLabel().trim());
        					if (name.length()>0) name.append(", ");
        					name.append(x.getLabel());
        				}
        				SolverGroup sg = new SolverGroup();
        				sg.setAbbv(abbv.length() <= 50 ? abbv.toString() : abbv.toString().substring(0,47)+"...");
        				sg.setName(name.length() <= 50 ? name.toString() : name.toString().substring(0,47)+"...");
        				sg.setTimetableManagers(new HashSet<TimetableManager>());
        				sg.setSession(session);
        				hibSession.persist(sg);
        				for (Department x: depts) {
        					x.setSolverGroup(sg);
        					hibSession.merge(x);
        				}
        				for (TimetableManager mgr: mgrs) {
        					mgr.getSolverGroups().add(sg);
        					sg.getTimetableManagers().add(mgr);
        					hibSession.merge(mgr);
        				}
        				ChangeLog.addChange(
        	                    hibSession, 
        	                    context, 
        	                    sg, 
        	                    ChangeLog.Source.SOLVER_GROUP_EDIT, 
        	                    ChangeLog.Operation.CREATE, 
        	                    null, 
        	                    null);
        			}
        		}
        	}

        	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}

}
