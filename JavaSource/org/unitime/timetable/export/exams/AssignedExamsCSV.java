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
package org.unitime.timetable.export.exams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.courses.ClassesCSV;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.exams.AssignedExamsTableBuilder;
import org.unitime.timetable.solver.exam.ExamSolverProxy;

@Service("org.unitime.timetable.export.Exporter:assigned-exams.csv")
public class AssignedExamsCSV extends ClassesCSV {
	
	@Override
	public String reference() {
		return "assigned-exams.csv";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		helper.getSessionContext().checkPermissionAnySession(helper.getAcademicSessionId(), Right.AssignedExaminations);
		exportDataCsv(getAssignedExams(helper, examinationSolverService.getSolver()), helper);
	}
	
	protected static List<TableInterface> getAssignedExams(ExportHelper helper, ExamSolverProxy solver) {
    	List<TableInterface> response = new ArrayList<TableInterface>();
    	
    	AssignedExamsTableBuilder builder = new AssignedExamsTableBuilder(
    			helper.getSessionContext(),
    			helper.getParameter("backType"),
		        helper.getParameter("backId")
		        );
    	builder.setSimple(true);
    	
    	Filter filter = new Filter(helper);
    	String examType = filter.getParameterValue("examType");
		ExamType type = null;
		try {
			type = ExamTypeDAO.getInstance().get(Long.valueOf(examType));
		} catch (Exception e) {}
		if (type == null)
			type = ExamType.findByReference(examType);
		if (type == null)
			throw new GwtRpcException(EXAM.messageNoExamType());
		
		String subjectArea = filter.getParameterValue("subjectArea");
		if (subjectArea == null || subjectArea.isEmpty())
			throw new GwtRpcException(EXAM.messageNoSubject());
		
		response.add(sorted(builder.generateAssignedExamsTable(
				type,
				solver,
		        filter, 
		        helper.getParameter("subjectArea").split(",")), helper));

    	return response;
	}

}
