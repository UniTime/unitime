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

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.form.DepartmentEditForm;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Action(value = "departmentEdit", results = {
		@Result(name = "add", type = "tiles", location = "departmentAdd.tiles"),
		@Result(name = "edit", type = "tiles", location = "departmentEdit.tiles"),
		@Result(name = "back", type = "redirect", location="/departmentList.action", params = {
				"anchor", "${form.id}"})
	})
@TilesDefinitions({
@TilesDefinition(name = "departmentAdd.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Add Department"),
		@TilesPutAttribute(name = "body", value = "/admin/departmentEdit.jsp")
	}),
@TilesDefinition(name = "departmentEdit.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Edit Department"),
		@TilesPutAttribute(name = "body", value = "/admin/departmentEdit.jsp")
	})
})
public class DepartmentEditAction extends UniTimeAction<DepartmentEditForm> {
	private static final long serialVersionUID = 338267053887154529L;
	protected static final GwtMessages MSG = Localization.create(GwtMessages.class);
	
	private Long id;
	private Integer deleteId;
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Integer getDeleteId() { return deleteId; }
	public void setDeleteId(Integer deleteId) { this.deleteId = deleteId; }

	@Override
	public String execute() throws Exception {
		if (form == null) form = new DepartmentEditForm();
	
		// Check Access
		sessionContext.checkPermission(Right.Departments);
		
		// Read operation to be performed
		if (op == null) op = form.getOp();
		
		request.setAttribute(Department.DEPT_ATTR_NAME, Department.findAllNonExternal(sessionContext.getUser().getCurrentAcademicSessionId()));
		
        // Edit
        if ("Edit".equalsIgnoreCase(op)) {
            Department department = DepartmentDAO.getInstance().get(id);
            if (department!=null) {
            	if (sessionContext.hasPermission(department, Right.DepartmentLimitedEdit)) {
            		form.setFullyEditable(false);
            	} else {
            		form.setFullyEditable(true);
            		sessionContext.checkPermission(department, Right.DepartmentEdit);
            	}
            	form.load(department);
            	return "edit";
            }
        }
        
        // Add
        if(stripAccessKey(MSG.buttonAddDepartment()).equalsIgnoreCase(op)) {
        	form.reset();
        	form.setSessionId(sessionContext.getUser().getCurrentAcademicSessionId());
        	form.setInheritInstructorPreferences(true);
        	form.setAllowStudentScheduling(true);
        	form.setFullyEditable(true);
    		sessionContext.checkPermission(Right.DepartmentAdd);
        	return "add";
        }
        
        // Update/Save
        if (stripAccessKey(MSG.buttonUpdate()).equalsIgnoreCase(op) || stripAccessKey(MSG.buttonSave()).equalsIgnoreCase(op)) {
            // Validate input
            form.validate(this);
            if (hasFieldErrors()) {
                return (form.getId() != null ? "edit" : "add");
            } else {
            	if (form.getId() == null || form.getId().equals(0l))
            		sessionContext.checkPermission(Right.DepartmentAdd);
            	else if (!form.isFullyEditable())
            		sessionContext.checkPermission(form.getId(), "Department", Right.DepartmentLimitedEdit);
            	else
            		sessionContext.checkPermission(form.getId(), "Department", Right.DepartmentEdit);
            	form.save(sessionContext);
            }
        }
        
        if (stripAccessKey(MSG.buttonDependentAddStatus()).equals(op)) {
        	form.addBlankDependentDepartment();
        	return (form.getId() != null ? "edit" : "add");
        }
        
        if (stripAccessKey(MSG.buttonDependentDeleteAll()).equals(op)) {
        	form.deleteAllDependentDepartments();
        	return (form.getId() != null ? "edit" : "add");
        }

        if (stripAccessKey(MSG.buttonDeleteLine()).equals(op) && deleteId != null) {
        	form.deleteDependentDepartment(deleteId);
        	return (form.getId() != null ? "edit" : "add");
        }
        
        // Delete
        if (stripAccessKey(MSG.buttonDelete()).equals(op)) {
        	doDelete();
        }
        
        return "back";
	}

	/**
	 * Delete a department
	 * @param request
	 * @param form
	 */
	private void doDelete() throws Exception{
		
		sessionContext.checkPermission(form.getId(), "Department", Right.DepartmentDelete);
		
        org.hibernate.Session hibSession = DepartmentDAO.getInstance().getSession();
        Transaction tx = null;
        try {
            tx = hibSession.beginTransaction();
            Department department = DepartmentDAO.getInstance().get(form.getId(), hibSession);
            if (department.isExternalManager().booleanValue()) {
                for (Class_ clazz: hibSession.
                        createQuery("select c from Class_ c where c.managingDept.uniqueId=:deptId", Class_.class).
                        setParameter("deptId", department.getUniqueId()).list()) {
                    if (clazz.getSchedulingSubpart().getManagingDept().equals(department)) {
                        // Clear all room preferences from the subpart
                        for (Iterator j = clazz.getSchedulingSubpart().getPreferences().iterator(); j.hasNext(); ) {
                            Object pref = j.next();
                            if (!(pref instanceof TimePref)) j.remove();
                        }
                        clazz.getSchedulingSubpart().deleteAllDistributionPreferences(hibSession);
                        hibSession.merge(clazz.getSchedulingSubpart());
                    }
                    clazz.setManagingDept(clazz.getControllingDept(), sessionContext.getUser(), hibSession);
                    // Clear all room preferences from the class
                    for (Iterator j = clazz.getPreferences().iterator(); j.hasNext(); ) {
                        Object pref = j.next();
                        if (!(pref instanceof TimePref)) j.remove();
                    }
                    clazz.deleteAllDistributionPreferences(hibSession);
                    hibSession.merge(clazz);
                }
            } else {
                hibSession.createMutationQuery(
                        "delete StudentClassEnrollment e where e.clazz.uniqueId in " +
                        "(select c.uniqueId from Class_ c, CourseOffering co where " +
                        "co.isControl=true and " +
                        "c.schedulingSubpart.instrOfferingConfig.instructionalOffering=co.instructionalOffering and "+
                        "co.subjectArea.department.uniqueId=:deptId)").
                        setParameter("deptId", department.getUniqueId()).
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
            hibSession.remove(department);
            tx.commit();
            HibernateUtil.clearCache();
        } catch (HibernateException e) {
            try {
                if (tx!=null && tx.isActive()) tx.rollback();
            } catch (Exception e1) { }
            throw e;
        }
		
	}
	
	public String getSession() {
		return sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel();
	}
}
