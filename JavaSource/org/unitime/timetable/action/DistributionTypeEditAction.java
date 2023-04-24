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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.DistributionTypeEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ComboBoxLookup;


/** 
 * @author Tomas Muller
 */
@Action(value = "distributionTypeEdit", results = {
		@Result(name = "showEdit", type = "tiles", location = "distributionTypeEdit.tiles"),
		@Result(name = "showDistributionTypeList", type = "redirect", location = "/distributionTypeList.action", params = 
		{"anchor", "${form.requirementId}"})
	})
@TilesDefinition(name = "distributionTypeEdit.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Edit Distribution Type"),
		@TilesPutAttribute(name = "body", value = "/admin/distributionTypeEdit.jsp")
	})
public class DistributionTypeEditAction extends UniTimeAction<DistributionTypeEditForm> {
	private static final long serialVersionUID = -1856283238950150575L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private Long id;
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	@Override
	public String execute() throws Exception {
		if (form == null)
			form = new DistributionTypeEditForm();

		sessionContext.checkPermission(Right.DistributionTypeEdit);
    
		// Read operation to be performed
		if (op == null) op = form.getOp();

		Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
		
		if (op == null) {
			form.setDistributionType(DistributionTypeDAO.getInstance().get(id), sessionId);
		}
		
        List<Department> list = DistributionTypeDAO.getInstance().getSession()
        		.createQuery("from Department session.uniqueId = :sessionId order by deptCode", Department.class)
        		.setParameter("sessionId", sessionId)
        		.list();
        List<ComboBoxLookup> availableDepts = new ArrayList<ComboBoxLookup>();
    	for (Department d: list) {
    		availableDepts.add(new ComboBoxLookup(d.getLabel(), d.getUniqueId().toString()));
    	}
    	request.setAttribute(Department.DEPT_ATTR_NAME, availableDepts);
    	request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, PreferenceLevel.getPreferenceLevelList());

    	if (MSG.actionUpdateDistributionType().equals(op)) {
			DistributionTypeDAO dao = DistributionTypeDAO.getInstance();
			Transaction tx = null;
			try {
                org.hibernate.Session hibSession = dao.getSession();
			    if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                    tx = hibSession.beginTransaction();
                DistributionType distType = dao.get(form.getUniqueId());
                DistributionType x = form.getDistributionType();
                distType.setAbbreviation(x.getAbbreviation());
                distType.setAllowedPref(x.getAllowedPref());
                distType.setDescr(x.getDescr());
                distType.setInstructorPref(x.isInstructorPref()==null?Boolean.FALSE:x.isInstructorPref());
                distType.setLabel(x.getLabel());
                distType.setVisible(x.isVisible() == null ? Boolean.FALSE : x.isVisible());
                HashSet<Department> oldDepts = new HashSet<Department>(distType.getDepartments());
                for (Long departmentId: form.getDepartmentIds()) {
                    Department d = (DepartmentDAO.getInstance()).get(departmentId,hibSession);
                    if (d==null) continue;
                    if (oldDepts.remove(d)) {
                        //not changed -> do nothing
                    } else {
                        distType.getDepartments().add(d);
                    }
                }
                for (Department d: oldDepts) {
                    if (d.getSessionId().equals(sessionId))
                    	distType.getDepartments().remove(d);
                }
                hibSession.saveOrUpdate(distType);
                ChangeLog.addChange(
                        hibSession, 
                        sessionContext, 
                        distType, 
                        ChangeLog.Source.DIST_TYPE_EDIT, 
                        ChangeLog.Operation.UPDATE, 
                        null, 
                        null);
                if (tx!=null) tx.commit();
			} catch (Exception e) {
			    if (tx!=null) tx.rollback();
                throw e;
			}

			return "showDistributionTypeList";
		}
		if (MSG.actionBackToDistributionTypes().equals(op)) {
			return "showDistributionTypeList";
		}
        if (MSG.actionAddDepartment().equals(op)) {
			if (form.getDepartmentId()==null || form.getDepartmentId().longValue()<0)
				addFieldError("form.department", MSG.errorNoDepartmentSelected());
			else {
				boolean contains = form.getDepartmentIds().contains(form.getDepartmentId());
				if (contains)
					addFieldError("form.department", MSG.errorDepartmentAlreadyListed());
			}
            if (!hasFieldErrors()) {
            	form.getDepartmentIds().add(form.getDepartmentId());
            }
        }

        if (MSG.actionRemoveDepartment().equals(op)) {
			if (form.getDepartmentId()==null || form.getDepartmentId().longValue()<0)
				addFieldError("form.department", MSG.errorNoDepartmentSelected());
			else {
				boolean contains = form.getDepartmentIds().contains(form.getDepartmentId());
				if (!contains)
					addFieldError("form.department", MSG.errorDepartmentNotListed());
			}
			if (!hasFieldErrors()) {
            	form.getDepartmentIds().remove(form.getDepartmentId());
            }	
        }
		return "showEdit";
	}
}
