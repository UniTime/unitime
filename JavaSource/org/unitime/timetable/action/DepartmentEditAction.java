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
package org.unitime.timetable.action;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.form.DepartmentEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@Service("/departmentEdit")
public class DepartmentEditAction extends Action {
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
		    
			DepartmentEditForm myForm = (DepartmentEditForm) form;
			MessageResources rsc = getResources(request);
		
			// Check Access
			sessionContext.checkPermission(Right.Departments);
			
			// Read operation to be performed
			String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
			
	        // Edit
	        if(rsc.getMessage("op.edit").equalsIgnoreCase(op)) {
	            String id = request.getParameter("id");
	            Department department = (new DepartmentDAO()).get(Long.valueOf(id));
	            if (department!=null) {
	            	sessionContext.checkPermission(department, Right.DepartmentEdit);
	            	myForm.load(department);
	            	return mapping.findForward("edit");
	            }
	        }
	        
	        // Add
	        if(rsc.getMessage("button.addDepartment").equalsIgnoreCase(op)) {
            	myForm.reset(mapping, request);
            	myForm.setSessionId(sessionContext.getUser().getCurrentAcademicSessionId());
        		sessionContext.checkPermission(Right.DepartmentAdd);
            	return mapping.findForward("add");
	        }
	        
	        // Update/Save
	        if (rsc.getMessage("button.updateDepartment").equalsIgnoreCase(op)
	        		|| rsc.getMessage("button.saveDepartment").equalsIgnoreCase(op)) {
	            // Validate input
	            ActionMessages errors = myForm.validate(mapping, request);
	            if(errors.size()>0) {
	                saveErrors(request, errors);
	                if (myForm.getId()!=null)
	                	return mapping.findForward("edit");
	                else
	                	return mapping.findForward("add");
	            } else {
	            	if (myForm.getId() == null || myForm.getId().equals(0l))
	            		sessionContext.checkPermission(Right.DepartmentAdd);
	            	else
	            		sessionContext.checkPermission(myForm.getId(), "Department", Right.DepartmentEdit);
	            	myForm.save(sessionContext);
	            }
	        }
	        
	        // Delete
	        if(rsc.getMessage("button.deleteDepartment").equalsIgnoreCase(op)) {
	        	doDelete(myForm);
	        }
	        
        	if (myForm.getId()!=null)
           		request.setAttribute(Constants.JUMP_TO_ATTR_NAME, myForm.getId().toString());
        	
	        return mapping.findForward("back");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}

	/**
	 * Delete a department
	 * @param request
	 * @param myForm
	 */
	private void doDelete(DepartmentEditForm frm) throws Exception{
		
		sessionContext.checkPermission(frm.getId(), "Department", Right.DepartmentDelete);
		
        org.hibernate.Session hibSession = new DepartmentDAO().getSession();
        Transaction tx = null;
        try {
            tx = hibSession.beginTransaction();
            Department department = new DepartmentDAO().get(frm.getId(), hibSession);
            if (department.isExternalManager().booleanValue()) {
                for (Iterator i=hibSession.
                        createQuery("select c from Class_ c where c.managingDept.uniqueId=:deptId").
                        setLong("deptId", department.getUniqueId()).iterate(); i.hasNext();) {
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
                    clazz.setManagingDept(clazz.getControllingDept());
                    // Clear all room preferences from the class
                    for (Iterator j = clazz.getPreferences().iterator(); j.hasNext(); ) {
                        Object pref = j.next();
                        if (!(pref instanceof TimePref)) j.remove();
                    }
                    clazz.deleteAllDistributionPreferences(hibSession);
                    hibSession.saveOrUpdate(clazz);
                }
            } else {
                hibSession.createQuery(
                        "delete StudentClassEnrollment e where e.clazz.uniqueId in " +
                        "(select c.uniqueId from Class_ c, CourseOffering co where " +
                        "co.isControl=1 and " +
                        "c.schedulingSubpart.instrOfferingConfig.instructionalOffering=co.instructionalOffering and "+
                        "co.subjectArea.department.uniqueId=:deptId)").
                        setLong("deptId", department.getUniqueId()).
                        executeUpdate();
            }
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    department, 
                    ChangeLog.Source.DEPARTMENT_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    null);
            hibSession.delete(department);
            tx.commit();
            HibernateUtil.clearCache();
        } catch (HibernateException e) {
            try {
                if (tx!=null && tx.isActive()) tx.rollback();
            } catch (Exception e1) { }
            throw e;
        }
		
	}
}
