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
package org.unitime.timetable.server.departments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.gwt.client.departments.DepartmentsEdit.UpdateDepartmentRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.DepartmentInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExternalDepartmentStatusType;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(UpdateDepartmentRequest.class)
public class UpdateDepartmentBackend implements GwtRpcImplementation<UpdateDepartmentRequest, DepartmentInterface>{

	@Override
	public DepartmentInterface execute(UpdateDepartmentRequest request, SessionContext context) {
		org.hibernate.Session hibSession = DepartmentDAO.getInstance().getSession();
		Department department = null;
		switch (request.getAction()) {
		case CREATE:			
			context.checkPermission(Right.DepartmentAdd);
			department = saveOrUpdate(request.getDepartment(), context);               
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    department, 
                    ChangeLog.Source.DEPARTMENT_EDIT, 
                    ChangeLog.Operation.CREATE , 
                    null, 
                    department);        	
			break;
		case UPDATE:
			context.checkPermission(request.getDepartment().getId(), "Department", Right.DepartmentEdit);
			department = saveOrUpdate(request.getDepartment(), context);
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    department, 
                    ChangeLog.Source.DEPARTMENT_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    department);  
			break;
		case DELETE:
			context.checkPermission(request.getDepartment().getId(), "Department", Right.DepartmentDelete);
			department = DepartmentDAO.getInstance().get( request.getDepartment().getId(), hibSession);
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    department, 
                    ChangeLog.Source.DEPARTMENT_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    null);
			delete(request.getDepartment(), context);
			break;
		}
		hibSession.flush();
		return request.getDepartment();
	}
	//Edit or create a department
	protected Department saveOrUpdate(DepartmentInterface departmentInterface, SessionContext context) throws GwtRpcException {
		Department department = null;
		try {
		org.unitime.timetable.model.Session acadSession = null;
		org.hibernate.Session hibSession = DepartmentDAO.getInstance().getSession();
		
        try {       	
        	        
            if (departmentInterface.getId() != null) {
            	department = DepartmentDAO.getInstance().get(departmentInterface.getId(), hibSession);
            }
            if (department==null) {
            	department = new Department();
            	acadSession = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()); 
            	department.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
            	department.setDistributionPrefPriority(0);
    			acadSession.addToDepartments(department);
    			department.setExternalStatusTypes(new HashSet<ExternalDepartmentStatusType>());
            }
                       
            department.setDeptCode(departmentInterface.getDeptCode());
            department.setAllowReqTime(departmentInterface.getAllowReqTime());
            department.setAbbreviation(departmentInterface.getAbbreviation());
            department.setName(departmentInterface.getName()); 
            department.setExternalUniqueId(departmentInterface.getExternalId() != null && departmentInterface.getExternalId().isEmpty() ? null : departmentInterface.getExternalId());
            department.setDistributionPrefPriority(departmentInterface.getDistributionPrefPriority());
            department.setExternalManager(departmentInterface.getExternalManager());
            department.setExternalMgrAbbv(departmentInterface.getExternalMgrAbbv());
            department.setAllowReqDistribution(departmentInterface.getAllowReqDistribution());          
            department.setExternalFundingDept(departmentInterface.getExternalFundingDept());  
            department.setExternalMgrLabel(departmentInterface.getExternalMgrLabel());            
            department.setAllowReqRoom(departmentInterface.getAllowReqRoom());
            department.setAllowEvents(departmentInterface.getAllowEvents());
            department.setAllowStudentScheduling(departmentInterface.getAllowStudentScheduling());
            department.setInheritInstructorPreferences(departmentInterface.getInheritInstructorPreferences());  
            department.setStatusType(DepartmentStatusType.findByRef(departmentInterface.getStatusTypeStr()));     
         
            List<ExternalDepartmentStatusType> statuses = new ArrayList<ExternalDepartmentStatusType>(department.getExternalStatusTypes());
            if (department.isExternalManager()) {
            	for (int i = 0; i < Math.min(departmentInterface.iDependentDepartments.size(), departmentInterface.iDependentStatuses.size()); i++) {
            		Long deptId = Long.valueOf((String)departmentInterface.iDependentDepartments.get(i));
            		String status =  departmentInterface.iDependentStatuses.get(i);
            		 DepartmentStatusType d = DepartmentStatusType.findByRef(status);
            		if (deptId >= 0 && !status.isEmpty()) {           		
            			ExternalDepartmentStatusType t = null;
            			for (Iterator<ExternalDepartmentStatusType> j = statuses.iterator(); j.hasNext(); ) {
            				ExternalDepartmentStatusType x = j.next();
            				if (deptId.equals(x.getDepartment().getUniqueId())) {
            					j.remove(); t = x; break;
            				}
            			}
            			if (t == null) {
            				t = new ExternalDepartmentStatusType();
            				t.setExternalDepartment(department);
            				t.setDepartment(DepartmentDAO.getInstance().get(deptId));
                			department.getExternalStatusTypes().add(t);
                			
            			}            
            			t.setStatusType(d);
            		}
            	}
            }
            for (ExternalDepartmentStatusType t: statuses) {
            	department.getExternalStatusTypes().remove(t);
            	hibSession.remove(t);
           }
            
            if (department.getUniqueId() == null) {
            	hibSession.persist(department);
            } else {
            	hibSession.merge(department);
            }
		
	    } finally {
	    	hibSession.flush();
		}
        departmentInterface.setId(department.getUniqueId());
	} catch (PageAccessException e) {
		throw e;
	} catch (Exception e) {
		throw new GwtRpcException(e.getMessage(), e);
	}
        return department;
	}
	
	//DELETE a department
	protected void delete(DepartmentInterface DepartmentInterface, SessionContext context) throws GwtRpcException {
		context.checkPermission(DepartmentInterface.getId(), "Department", Right.DepartmentDelete);
		org.hibernate.Session hibSession = DepartmentDAO.getInstance().getSession();
		Transaction tx = null;
        try { 
        	tx = hibSession.beginTransaction();
        	Department department = DepartmentDAO.getInstance().get(DepartmentInterface.getId(), hibSession);
        	
        	 if (department.isExternalManager().booleanValue()) {
        		 for (Class_ clazz: hibSession.createQuery(
        				 "select c from Class_ c where c.managingDept.uniqueId=:deptId", Class_.class)
        				 .setParameter("deptId", department.getUniqueId()).list()) {
                     if (clazz.getSchedulingSubpart().getManagingDept().equals(department)) {
                         // Clear all room preferences from the subpart
                         for (Iterator<Preference> j = clazz.getSchedulingSubpart().getPreferences().iterator(); j.hasNext(); ) {
                        	 Preference pref = j.next();
                             if (!(pref instanceof TimePref)) j.remove();
                         }
                         clazz.getSchedulingSubpart().deleteAllDistributionPreferences(hibSession);
                         hibSession.merge(clazz.getSchedulingSubpart());
                     }
                     clazz.setManagingDept(clazz.getControllingDept(), context.getUser(), hibSession);
                     // Clear all room preferences from the class
                     for (Iterator<Preference> j = clazz.getPreferences().iterator(); j.hasNext(); ) {
                    	 Preference pref = j.next();
                         if (!(pref instanceof TimePref)) j.remove();
                     }
                     clazz.deleteAllDistributionPreferences(hibSession);
                     hibSession.merge(clazz);
                 }
             }       		
        	 else {
                 hibSession.createMutationQuery(
                         "delete StudentClassEnrollment e where e.clazz.uniqueId in " +
                         "(select c.uniqueId from Class_ c, CourseOffering co where " +
                         "co.isControl=true and " +
                         "c.schedulingSubpart.instrOfferingConfig.instructionalOffering=co.instructionalOffering and "+
                         "co.subjectArea.department.uniqueId=:deptId)").
                         setParameter("deptId", department.getUniqueId()).
                         executeUpdate();
             }             

                hibSession.remove(department);
    			tx.commit();
    			HibernateUtil.clearCache();     	
	    } catch (Exception e) {
	    	if (tx!=null && tx.isActive()) tx.rollback();
	    	throw new GwtRpcException(e.getMessage(), e);
	    }
	}

}
