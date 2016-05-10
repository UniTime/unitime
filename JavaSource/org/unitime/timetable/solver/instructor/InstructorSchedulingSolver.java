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
package org.unitime.timetable.solver.instructor;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ProblemLoader;
import org.cpsolver.ifs.util.ProblemSaver;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.instructor.model.InstructorSchedulingModel;
import org.cpsolver.instructor.model.TeachingAssignment;
import org.cpsolver.instructor.model.TeachingRequest;
import org.dom4j.Document;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.solver.AbstractSolver;
import org.unitime.timetable.solver.SolverDisposeListener;

/**
 * @author Tomas Muller
 */
public class InstructorSchedulingSolver extends AbstractSolver<TeachingRequest, TeachingAssignment, InstructorSchedulingModel> implements InstructorSchedulingProxy {
	
	public InstructorSchedulingSolver(DataProperties properties, SolverDisposeListener disposeListener) {
		super(properties, disposeListener);
	}

	@Override
	public SolverType getType() {
		return SolverType.INSTRUCTOR;
	}

	@Override
	protected ProblemSaver<TeachingRequest, TeachingAssignment, InstructorSchedulingModel> getDatabaseSaver( Solver<TeachingRequest, TeachingAssignment> solver) {
		return new InstructorSchedulingDatabaseSaver(solver);
	}

	@Override
	protected ProblemLoader<TeachingRequest, TeachingAssignment, InstructorSchedulingModel> getDatabaseLoader(InstructorSchedulingModel model, Assignment<TeachingRequest, TeachingAssignment> assignment) {
		return new InstructorSchedulingDatabaseLoader(model, assignment);
	}

	@Override
	protected InstructorSchedulingModel createModel(DataProperties properties) {
		return new InstructorSchedulingModel(properties);
	}

	@Override
	protected Document createCurrentSolutionBackup(boolean anonymize, boolean idconv) {
		if (anonymize) {
            getProperties().setProperty("Xml.Anonymize", "true");
            getProperties().setProperty("Xml.ShowNames", "false");
            getProperties().setProperty("Xml.ConvertIds", idconv ? "true" : "false");
            getProperties().setProperty("Xml.SaveInitial", "false");
            getProperties().setProperty("Xml.SaveBest", "false");
            getProperties().setProperty("Xml.SaveSolution", "true");
		} else {
            getProperties().setProperty("Xml.Anonymize", "false");
            getProperties().setProperty("Xml.ShowNames", "true");
            getProperties().setProperty("Xml.ConvertIds", "false");
            getProperties().setProperty("Xml.SaveInitial", "true");
            getProperties().setProperty("Xml.SaveBest", "true");
            getProperties().setProperty("Xml.SaveSolution", "true");
		}
		InstructorSchedulingModel model = (InstructorSchedulingModel)currentSolution().getModel();
		Document document = model.save(currentSolution().getAssignment());
		if (document == null) return null;
        if (!anonymize) {
            Progress p = Progress.getInstance(model);
            if (p != null)
            	Progress.getInstance(this).save(document.getRootElement());
        }
        return document;
	}

	@Override
	protected void restureCurrentSolutionFromBackup(Document document) {
		InstructorSchedulingModel model = (InstructorSchedulingModel)currentSolution().getModel();
		model.load(document, currentSolution().getAssignment());
        Progress p = Progress.getInstance(model);
        if (p != null) {
            p.load(document.getRootElement(), true);
            p.message(Progress.MSGLEVEL_STAGE, "Restoring from backup ...");
        }
	}
	
	

}
