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
package org.unitime.timetable.server.courses;

import java.util.Iterator;

import org.hibernate.ObjectNotFoundException;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.CrossListGetRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.CrossListGetResponse;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.CrossListedCourse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(CrossListGetRequest.class)
public class CrossListGetBackend implements GwtRpcImplementation<CrossListGetRequest, CrossListGetResponse>{

	@Override
	public CrossListGetResponse execute(CrossListGetRequest request, SessionContext context) {
		InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(request.getOfferingId());
		context.checkPermission(io, Right.InstructionalOfferingCrossLists);
		
		CrossListGetResponse response = new CrossListGetResponse();
		response.setOfferingId(io.getUniqueId());
		response.setOfferingName(io.getCourseNameWithTitle());
		response.setLimit(io.getLimit());
		response.setUnlimited(io.hasUnlimitedEnrollment());
		response.setSingleCourseLimitAllowed(ApplicationProperty.ModifyCrossListSingleCourseLimit.isTrue());
		
        for (CourseOffering course: io.getCourseOfferings()) {
        	CrossListedCourse c = new CrossListedCourse();
        	c.setControl(course.isIsControl());
        	c.setCourseId(course.getUniqueId());
        	c.setCourseName(course.getCourseNameWithTitle());
        	c.setReserved(course.getReservation());
        	c.setProjected(course.getProjectedDemand());
        	c.setLastTerm(course.getDemand());
        	c.setCanEdit(context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent) || context.getUser().getCurrentAuthority().hasQualifier(course.getDepartment()));
        	c.setCanDelete(context.hasPermission(course, Right.CourseOfferingDeleteFromCrossList));
        	response.addCourse(c);
        	response.addAvailableCourse(c);
        }
        
		for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser())) {
			Iterator<CourseOffering> i = null;
			try {
				i = subject.getCourseOfferings().iterator();
			}
			catch (ObjectNotFoundException e) {
			    CourseOfferingDAO.getInstance().getSession().refresh(subject);
			    i = subject.getCourseOfferings().iterator();
			}
			for (;i.hasNext();) {
				CourseOffering course = i.next();
				if (course != null && course.getInstructionalOffering().isNotOffered()) {
					CrossListedCourse c = new CrossListedCourse();
		        	c.setControl(course.isIsControl());
		        	c.setCourseId(course.getUniqueId());
		        	c.setCourseName(course.getCourseNameWithTitle());
		        	c.setReserved(course.getReservation());
		        	c.setProjected(course.getProjectedDemand());
		        	c.setLastTerm(course.getDemand());
		        	response.addAvailableCourse(c);
				}
			}
		}

        return response;
	}

}
