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

import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingsRequest;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(OfferingsRequest.class)
public class OfferingsBackend implements GwtRpcImplementation<OfferingsRequest, GwtRpcResponseList<TableInterface>> {
	protected static CourseMessages MESSAGES = Localization.create(CourseMessages.class);
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	

	@Override
	public GwtRpcResponseList<TableInterface> execute(OfferingsRequest request, SessionContext context) {
		context.checkPermission(Right.InstructionalOfferings);
		
		String subjectArea = request.getFilter().getParameterValue("subjectArea");
		if (subjectArea == null || subjectArea.isEmpty())
			throw new GwtRpcException(MESSAGES.errorSubjectRequired());
		
		GwtRpcResponseList<TableInterface> response = new GwtRpcResponseList<TableInterface>();
		InstructionalOfferingTableBuilder builder = new InstructionalOfferingTableBuilder(context, request.getBackType(), request.getBackId());
		
		for (FilterParameterInterface p: request.getFilter().getParameters()) {
			if ("subjectArea".equals(p.getName())) {
				context.setAttribute(SessionAttribute.OfferingsSubjectArea, p.getValue() != null ? p.getValue() : p.getDefaultValue());
			} else if ("courseNbr".equals(p.getName())) {
				context.setAttribute(SessionAttribute.OfferingsCourseNumber, p.getValue() != null ? p.getValue() : p.getDefaultValue());
			} else if (p.getValue() != null) {
				context.getUser().setProperty("InstructionalOfferingList." + p.getName(), p.getValue());
			}
		}
		
		String courseNbr = request.getFilter().getParameterValue("courseNbr");
		try {
			String subjects = "";
			int count = 0;
			for (String id: subjectArea.split(",")) {
				SubjectArea sa = SubjectAreaDAO.getInstance().get(Long.valueOf(id));
				if (sa != null) {
					context.checkPermission(sa.getDepartment(), Right.InstructionalOfferings);
					count++;
					if (count == 1)
						subjects += sa.getSubjectAreaAbbreviation();
					else if (count <= 3)
						subjects += ", " + sa.getSubjectAreaAbbreviation();
					else if (count == 4)
						subjects += "\u2026";
				}
			}
			BackTracker.markForBack(
					context, 
					"offerings?subjectArea=" + request.getFilter().getParameterValue("subjectArea") +
						"&courseNbr=" + (courseNbr == null ? "" : URLEncoder.encode(courseNbr, "utf-8")),
					MESSAGES.labelInstructionalOfferings() + " (" + subjects + (courseNbr == null || courseNbr.isEmpty() ? "" : " " + courseNbr) + ")", 
					true, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		builder.generateTableForInstructionalOfferings(
				classAssignmentService.getAssignment(),
				examinationSolverService.getSolver(),
		        request.getFilter(), 
		        subjectArea.split(","), 
		        response);
		return response;
	}

}
