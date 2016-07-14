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
package org.unitime.timetable.server.instructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPageRequest;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(TeachingRequestsPageRequest.class)
public class TeachingRequestsPageBackend extends InstructorSchedulingBackendHelper implements GwtRpcImplementation<TeachingRequestsPageRequest, GwtRpcResponseList<TeachingRequestInfo>> {
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;
	
	@Override
	public GwtRpcResponseList<TeachingRequestInfo> execute(TeachingRequestsPageRequest request, SessionContext context) {
		context.checkPermission(Right.InstructorSchedulingSolver);
		context.setAttribute(SessionAttribute.OfferingsSubjectArea, request.getSubjectAreaId() == null ? "-1" : String.valueOf(request.getSubjectAreaId()));
		InstructorSchedulingProxy solver = instructorSchedulingSolverService.getSolver();
		if (solver != null)
			return new GwtRpcResponseList<TeachingRequestInfo>(solver.getTeachingRequests(request.getSubjectAreaId(), request.isAssigned()));
		else {
			Set<String> commonItypes = getCommonItypes();
			String nameFormat = UserProperty.NameFormat.get(context.getUser());
			
			GwtRpcResponseList<TeachingRequestInfo> ret = new GwtRpcResponseList<TeachingRequestInfo>();
			org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
			List<Class_> classes = null;
			if (request.getSubjectAreaId() == null) {
				List<Long> subjectAreaIds = new ArrayList<Long>();
				for (SubjectArea sa: SubjectArea.getUserSubjectAreas(context.getUser(), true)) {
					for (DepartmentalInstructor di: sa.getDepartment().getInstructors())
						if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
							subjectAreaIds.add(sa.getUniqueId());
							break;
						}
				}
				if (subjectAreaIds.isEmpty()) return ret;
				classes = (List<Class_>)hibSession.createQuery(
						"select distinct c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co " +
						"left join fetch c.schedulingSubpart as ss left join fetch c.classInstructors as ci left join fetch ci.instructor as di " +
						"left join fetch c.preferences as cp left join fetch ss.preferences as sp left join fetch di.preferences as dip " +
						"where co.subjectArea.uniqueId in :subjectAreaIds and co.isControl = true and c.cancelled = false and " +
						"(c.teachingLoad is not null or c.schedulingSubpart.teachingLoad is not null) and " +
						"((c.nbrInstructors is null and c.schedulingSubpart.nbrInstructors > 0) or c.nbrInstructors > 0)")
						.setParameterList("subjectAreaIds", subjectAreaIds).setCacheable(true).list();
			} else {
				classes = (List<Class_>)hibSession.createQuery(
						"select distinct c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co " +
						"left join fetch c.schedulingSubpart as ss left join fetch c.classInstructors as ci left join fetch ci.instructor as di " +
						"left join fetch c.preferences as cp left join fetch ss.preferences as sp left join fetch di.preferences as dip " +
						"where co.subjectArea.uniqueId = :subjectAreaId and co.isControl = true and c.cancelled = false and " +
		    			"(c.teachingLoad is not null or c.schedulingSubpart.teachingLoad is not null) and " +
		    			"((c.nbrInstructors is null and c.schedulingSubpart.nbrInstructors > 0) or c.nbrInstructors > 0)")
		    			.setLong("subjectAreaId", request.getSubjectAreaId()).setCacheable(true).list();
			}
	    	Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
	    	for (Class_ clazz: classes) {
	    		if (!clazz.isInstructorAssignmentNeeded()) continue;
	    		TeachingRequestInfo info = getRequestForClass(clazz, commonItypes, nameFormat);
	    		if (info != null) {
	    			if (request.isAssigned() && info.getNrAssignedInstructors() > 0)
	    				ret.add(info);
	    			else if (!request.isAssigned() && info.getNrAssignedInstructors() < info.getNrInstructors())
	    				ret.add(info);
	    		}
	    	}
	    	Collections.sort(ret);
	    	return ret;
		}
	}
}
