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

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.gwt.client.departments.DepartmentsEdit.UpdateDepartmentRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.CurriculaException;
import org.unitime.timetable.gwt.shared.DepartmentInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExternalDepartmentStatusType;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

//import java.util.Iterator;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.form.DepartmentEditForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.util.Constants;


@GwtRpcImplements(UpdateDepartmentRequest.class)
public class UpdateDepartmentBackend implements GwtRpcImplementation<UpdateDepartmentRequest, DepartmentInterface>{

	@Override
	public DepartmentInterface execute(UpdateDepartmentRequest request, SessionContext context) {
		Transaction tx = null;
		org.hibernate.Session hibSession = DepartmentDAO.getInstance().getSession();
		Department department = null;
		switch (request.getAction()) {
		case CREATE:			
			context.checkPermission(Right.DepartmentAdd);
			department = saveOrUpdate(request.getDepartment(), context);               
            //department = DepartmentDAO.getInstance().get( request.getDepartment().getId(), hibSession);
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
			//department = DepartmentDAO.getInstance().get( request.getDepartment().getId(), hibSession);
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
            	department.setDistributionPrefPriority(new Integer(0));
    			acadSession.addTodepartments(department);
    			department.setExternalStatusTypes(new HashSet<ExternalDepartmentStatusType>());
            }
                       
            department.setDeptCode(departmentInterface.getDeptCode());
            department.setAllowReqTime(new Boolean(departmentInterface.getAllowReqTime()));
            department.setAbbreviation(departmentInterface.getAbbreviation());
            department.setName(departmentInterface.getName()); 
            department.setExternalUniqueId(departmentInterface.getExternalId() != null && departmentInterface.getExternalId().isEmpty() ? null : departmentInterface.getExternalId());
            department.setDistributionPrefPriority(new Integer(departmentInterface.getDistributionPrefPriority()));
            department.setExternalManager(new Boolean(departmentInterface.getExternalManager()));
            department.setExternalMgrAbbv(departmentInterface.getExternalMgrAbbv());
            department.setAllowReqDistribution(new Boolean(departmentInterface.getAllowReqDistribution()));          
            department.setExternalFundingDept(new Boolean(departmentInterface.getExternalFundingDept()));  
            department.setExternalMgrLabel(departmentInterface.getExternalMgrLabel());            
            department.setAllowReqRoom(new Boolean(departmentInterface.getAllowReqRoom()));
            department.setAllowEvents(departmentInterface.getAllowEvents());
            department.setAllowStudentScheduling(departmentInterface.getAllowStudentScheduling());
            department.setInheritInstructorPreferences(departmentInterface.getInheritInstructorPreferences());  
            department.setStatusType(DepartmentStatusType.findByRef(departmentInterface.getStatusTypeStr()));     
            //external dept///////////////           
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
            	hibSession.delete(t);
           }
            
            if (department.getUniqueId() == null) {
            	departmentInterface.setId((Long)hibSession.save(department));
            } else {
            	hibSession.update(department);
            }
		
	    } finally {
	    	hibSession.flush();
			hibSession.close();
		}
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
        	Department department = new DepartmentDAO().get(DepartmentInterface.getId(), hibSession);
        	
        	 if (department.isExternalManager().booleanValue()) {
                 for (Iterator i=hibSession.createQuery("select c from Class_ c where c.managingDept.uniqueId=:deptId").setLong("deptId", department.getUniqueId()).iterate(); i.hasNext();) {
                     Class_ clazz = (Class_)i.next();
                     if (clazz.getSchedulingSubpart().getManagingDept().equals(department)) {
                         // Clear all room preferences from the subpart
                         for (Iterator j = clazz.getSchedulingSubpart().getPreferences().iterator(); j.hasNext(); ) {
                             Object pref = j.next();
                             if (!(pref instanceof TimePref)) j.remove();
                         }
                         clazz.getSchedulingSubpart().deleteAllDistributionPreferences(hibSession);
                         hibSession.saveOrUpdate(clazz.getSchedulingSubpart());
                     }
                     clazz.setManagingDept(clazz.getControllingDept(), context.getUser(), hibSession);
                     // Clear all room preferences from the class
                     for (Iterator j = clazz.getPreferences().iterator(); j.hasNext(); ) {
                         Object pref = j.next();
                         if (!(pref instanceof TimePref)) j.remove();
                     }
                     clazz.deleteAllDistributionPreferences(hibSession);
                     hibSession.saveOrUpdate(clazz);
                 }
             }       		
        	 else {
                 hibSession.createQuery(
                         "delete StudentClassEnrollment e where e.clazz.uniqueId in " +
                         "(select c.uniqueId from Class_ c, CourseOffering co where " +
                         "co.isControl=true and " +
                         "c.schedulingSubpart.instrOfferingConfig.instructionalOffering=co.instructionalOffering and "+
                         "co.subjectArea.department.uniqueId=:deptId)").
                         setLong("deptId", department.getUniqueId()).
                         executeUpdate();
             }             

                hibSession.delete(department);
    			tx.commit();
    			HibernateUtil.clearCache();     	
	    } catch (Exception e) {
	    	if (tx!=null && tx.isActive()) tx.rollback();
	    	throw new GwtRpcException(e.getMessage(), e);
	    }
	}

}
