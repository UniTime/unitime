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
package org.unitime.timetable.export.solver;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CommonSolverInterface;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:solution.xml")
public class ExportSolutionXML implements Exporter {

	@Override
	public String reference() {
		return "solution.xml";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		String t = helper.getParameter("type");
		if (t == null || t.isEmpty())
			throw new IllegalArgumentException("Type parameter was not provided.");
		SolverType type = null;
		try {
			type = SolverType.valueOf(t.toUpperCase());
		} catch (Exception e) {
			throw new IllegalArgumentException("Wrong solver type.");
		}
		CommonSolverInterface solver = getSolver(type);
		if (solver == null) throw new IllegalArgumentException("Solver is not started.");
        if (solver.isWorking()) throw new IllegalArgumentException("Solver is working, stop it first.");
        
        switch (type) {
        case COURSE:
            helper.getSessionContext().checkPermission(solver.getProperties().getPropertyLongArry("General.SolverGroupId", null), "SolverGroup", Right.SolverSolutionExportXml);
			break;
        case EXAM:
        	helper.getSessionContext().checkPermission(Right.ExaminationSolutionExportXml);
        	break;
        case STUDENT:
        	helper.getSessionContext().checkPermission(Right.StudentSectioningSolutionExportXml);
        	break;
		}
        
        byte[] buf = solver.exportXml();
        helper.setup("application/xml", type.name().toLowerCase() + "-solution.xml", true);
        OutputStream out = helper.getOutputStream(); 
        out.write(buf);
        out.flush(); out.close();
	}

	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;

	protected CommonSolverInterface getSolver(SolverType type) {
		switch (type) {
		case COURSE:
			return courseTimetablingSolverService.getSolver();
		case EXAM:
			return examinationSolverService.getSolver();
		case STUDENT:
			return studentSectioningSolverService.getSolver();
		default:
			throw new IllegalArgumentException("Invalid solver type " + type);
		}
	}
}
