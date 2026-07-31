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
package org.unitime.timetable.server.administration.solver;

import java.util.Collections;
import java.util.HashSet;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.admin.DistributionTypesPage.DistributionTypeEditRequest;
import org.unitime.timetable.gwt.client.admin.DistributionTypesPage.DistributionTypeEditResponse;
import org.unitime.timetable.gwt.client.admin.DistributionTypesPage.DistributionTypeInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(DistributionTypeEditRequest.class)
public class DistributionTypeEditBackend implements GwtRpcImplementation<DistributionTypeEditRequest, DistributionTypeEditResponse> {
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public DistributionTypeEditResponse execute(DistributionTypeEditRequest request, SessionContext context) {
		context.checkPermission(Right.DistributionTypeEdit);
		switch (request.getOperation()) {
		case EDIT:
			DistributionTypeEditResponse response = new DistributionTypeEditResponse();
			DistributionType type = DistributionTypeDAO.getInstance().get(request.getTypeId());
			if (type == null)
				throw new GwtRpcException(MSG.errorDoesNotExists(MSG.propertyDistributionType()));
			
			DistributionTypeInterface d = new DistributionTypeInterface();
			d.setTypeId(type.getUniqueId());
			d.setId(d.getId());
			d.setReference(type.getReference());
			d.setAbbreviation(type.getAbbreviation());
			d.setName(type.getLabel());
			d.setDescription(type.getDescr());
			d.setSequencing(type.isSequencingRequired());
			d.setExam(type.isExamPref());
			d.setInstructor(type.isInstructorPref());
			d.setSurvey(type.isSurvey());
			d.setVisible(type.isVisible());
			Long sessionId = context.getUser().getCurrentAcademicSessionId();
			for (Department dept: type.getDepartments())
				if (dept.getSessionId().equals(sessionId))
					d.addDepartmentId(dept.getUniqueId());
			if (type.getAllowedPref() != null)
				for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false))
					if (type.getAllowedPref().indexOf(PreferenceLevel.prolog2char(pref.getPrefProlog())) >= 0)
						d.addPreferenceId(pref.getUniqueId());
			response.setType(d);
			
			setupLookups(context, response, type);
			return response;
		case SAVE:
			DistributionTypeEditResponse saveResponse = new DistributionTypeEditResponse();
			saveResponse.setType(new DistributionTypeInterface());
			saveResponse.getType().setTypeId(updateDistributionType(context, request.getType()));
			return saveResponse;
		}
		
		return null;
	}
	
	protected void setupLookups(SessionContext context, DistributionTypeEditResponse response, DistributionType type) {
		Long sessionId = context.getUser().getCurrentAcademicSessionId();

		for (Department dept: DepartmentDAO.getInstance().getSession()
    			.createQuery("from Department where session.uniqueId = :sessionId order by deptCode", Department.class)
    			.setParameter("sessionId", context.getUser().getCurrentAcademicSessionId())
    			.list()) {
			if (dept.isExternalManager() || !dept.getSubjectAreas().isEmpty())
				response.addDepartment(dept.getUniqueId(), dept.getLabel());
		}
		if (type != null)
			for (Department dept: type.getDepartments()) {
				if (!dept.getSessionId().equals(sessionId)) continue;
				if (response.getDepartment(dept.getUniqueId()) == null)
					response.addDepartment(dept.getUniqueId(), dept.getLabel());
			}
		if (response.hasDepartments())
			Collections.sort(response.getDepartments());
		
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false))
			response.addPreference(pref.getUniqueId(), pref.getPrefName(), PreferenceLevel.prolog2color(pref.getPrefProlog()));
	}
	
	protected Long updateDistributionType(SessionContext context, DistributionTypeInterface x) {
        org.hibernate.Session hibSession = DistributionTypeDAO.getInstance().getSession();
        Long sessionId = context.getUser().getCurrentAcademicSessionId();
        Transaction tx = null;
        try {
        	tx = hibSession.beginTransaction();
        	
            DistributionType distType = DistributionTypeDAO.getInstance().get(x.getTypeId(), hibSession);
            distType.setAbbreviation(x.getAbbreviation());
            String allowedPref = "";
            for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false))
            	if (x.hasPreferenceId(pref.getUniqueId())) allowedPref += PreferenceLevel.prolog2char(pref.getPrefProlog());
            distType.setAllowedPref(allowedPref.isEmpty() ? null :allowedPref);
            distType.setDescr(x.getDescription());
            distType.setInstructorPref(x.isInstructor() != null && x.isInstructor().booleanValue());
            distType.setSurvey(x.isSurvey() != null && x.isSurvey().booleanValue());
            distType.setLabel(x.getName());
            distType.setVisible(x.isVisible() != null && x.isVisible().booleanValue());
            HashSet<Department> oldDepts = new HashSet<Department>(distType.getDepartments());
            if (x.hasDepartmentIds())
                for (Long departmentId: x.getDepartmentIds()) {
                    Department d = DepartmentDAO.getInstance().get(departmentId, hibSession);
                    if (d==null) continue;
                    if (!oldDepts.remove(d)) {
                        distType.getDepartments().add(d);
                    }
                }
            for (Department d: oldDepts) {
                if (d.getSessionId().equals(sessionId))
                	distType.getDepartments().remove(d);
            }
            hibSession.merge(distType);
            
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    distType, 
                    ChangeLog.Source.DIST_TYPE_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    null);

           	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
        return x.getTypeId();
	}

}
