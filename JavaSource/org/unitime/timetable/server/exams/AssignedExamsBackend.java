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
package org.unitime.timetable.server.exams;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.AssignedExamsRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingsResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.SubpartDetailBackend;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(AssignedExamsRequest.class)
public class AssignedExamsBackend implements GwtRpcImplementation<AssignedExamsRequest, OfferingsResponse> {
	protected static ExaminationMessages MESSAGES = Localization.create(ExaminationMessages.class);
	protected static GwtMessages GWT = Localization.create(GwtMessages.class);
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	@Override
	public OfferingsResponse execute(AssignedExamsRequest request, SessionContext context) {
		context.checkPermission(Right.AssignedExaminations);
		
		String examType = request.getFilter().getParameterValue("examType");
		ExamType type = null;
		try {
			type = ExamTypeDAO.getInstance().get(Long.valueOf(examType));
		} catch (Exception e) {}
		if (type == null)
			type = ExamType.findByReference(examType);
		if (type == null)
			throw new GwtRpcException(MESSAGES.messageNoExamType());
		
		String subjectArea = request.getFilter().getParameterValue("subjectArea");
		if (subjectArea == null || subjectArea.isEmpty())
			throw new GwtRpcException(MESSAGES.messageNoSubject());

		for (FilterParameterInterface p: request.getFilter().getParameters()) {
			if ("subjectArea".equals(p.getName())) {
				context.setAttribute(SessionAttribute.OfferingsSubjectArea, p.getValue() != null ? p.getValue() : p.getDefaultValue());
			} else if ("examType".equals(p.getName())) {
				context.setAttribute(SessionAttribute.ExamType, Long.valueOf(p.getValue() != null ? p.getValue() : p.getDefaultValue()));
			} else if (p.getValue() != null) {
				context.getUser().setProperty("ExamReport." + p.getName(), p.getValue());
			}
		}
		
		OfferingsResponse response = new OfferingsResponse();
		
		BackTracker.markForBack(
				context, 
				"assignedExams?examType=" + type.getUniqueId() + "&subjectArea=" + request.getFilter().getParameterValue("subjectArea"),
				MESSAGES.backAssignedExaminations(), 
				true, true);
		
		AssignedExamsTableBuilder builder = new AssignedExamsTableBuilder(context, request.getBackType(), request.getBackId());
		
		response.add(builder.generateAssignedExamsTable(type, examinationSolverService.getSolver(), request.getFilter(), subjectArea.split(",")));
		
		TableInterface legend = new TableInterface();
		legend.addProperty(SubpartDetailBackend.getLegend(false));
		response.add(legend);
		
		return response;
	}

}
