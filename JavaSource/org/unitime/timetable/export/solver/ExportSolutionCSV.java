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
import java.io.PrintWriter;

import org.cpsolver.ifs.util.CSVFile;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CommonSolverInterface;
import org.unitime.timetable.solver.SolverProxy;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:solution.csv")
public class ExportSolutionCSV extends ExportSolutionXML {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Override
	public String reference() {
		return "solution.csv";
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
		CSVFile csv = null;
		if (type == SolverType.COURSE && helper.getParameter("solution") != null) {
			Solution solution = SolutionDAO.getInstance().get(Long.valueOf(helper.getParameter("solution")));
			if (solution == null) throw new IllegalArgumentException(MESSAGES.errorSolutionDoesNotExist(helper.getParameter("solution")));
			csv = new CSVFile();
			solution.export(csv, UserProperty.NameFormat.get(helper.getSessionContext().getUser()));
		} else {
			if (solver == null) throw new IllegalArgumentException(MESSAGES.warnSolverNotStarted());
	        if (solver.isWorking()) throw new IllegalArgumentException(MESSAGES.warnSolverIsWorking());
	        
	        switch (type) {
	        case COURSE:
	        	helper.getSessionContext().checkPermission(solver.getProperties().getPropertyLongArry("General.SolverGroupId", null), "SolverGroup", Right.SolverSolutionExportCsv);
	        	csv = ((SolverProxy)solver).export(CONSTANTS.useAmPm());
				break;
			default:
				throw new IllegalArgumentException("Feature not implemented.");
			}
		}
		
        helper.setup("text/csv", type.name().toLowerCase() + "-solution.csv", false);
        
        PrintWriter writer = helper.getWriter();

        if (csv.getHeader() != null)
			writer.println(csv.getHeader().toString());
		if (csv.getLines() != null)
			for (CSVFile.CSVLine line: csv.getLines())
				writer.println(line.toString());
		
		writer.flush(); writer.close();
	}

}