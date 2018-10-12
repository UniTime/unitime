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
package org.unitime.timetable.dataexchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.cpsolver.ifs.util.DataProperties;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

/**
 * @author Tomas Muller
 */
public class SolverImport extends BaseImport {

	@Override
	public void loadXml(Element root) throws Exception {
        try {
            beginTransaction();
            SolverServerService solverService = (SolverServerService)SpringApplicationContextHolder.getBean("solverServerService");
            if (root.getName().equalsIgnoreCase("timetable") || root.getName().equalsIgnoreCase("coursett")) {
            	root.setName("timetable");
            	SolverProxy solver = solverService.getCourseSolverContainer().createSolver(getManager().getExternalUniqueId(), getConfig(root));
            	solver.importXml(toData(root));
            } else if (root.getName().equalsIgnoreCase("examtt")) {
            	ExamSolverProxy solver = solverService.getExamSolverContainer().createSolver(getManager().getExternalUniqueId(), getConfig(root));
            	solver.importXml(toData(root));
            } else if (root.getName().equalsIgnoreCase("sectioning")) {
            	StudentSolverProxy solver = solverService.getStudentSolverContainer().createSolver(getManager().getExternalUniqueId(), getConfig(root));
            	solver.importXml(toData(root));
            } else if (root.getName().equalsIgnoreCase("instructor-schedule")) {
            	InstructorSchedulingProxy solver = solverService.getInstructorSchedulingContainer().createSolver(getManager().getExternalUniqueId(), getConfig(root));
            	solver.importXml(toData(root));
            } else {
            	throw new Exception("Given XML file is not a solver file.");
            }
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
	
	protected DataProperties getConfig(Element root) {
		DataProperties config = new DataProperties();
		if (root.element("configuration") != null)
			for (Iterator i = root.element("configuration").elementIterator("property"); i.hasNext(); ) {
    			Element e = (Element)i.next();
    			config.setProperty(e.attributeValue("name"), e.getText());
    		}
		config.setProperty("General.OwnerPuid", getManager().getExternalUniqueId());
		Session session = Session.getSessionUsingInitiativeYearTerm(
				root.attributeValue("initiative", root.attributeValue("campus")),
				root.attributeValue("year"), root.attributeValue("term"));
		if (session != null) {
			config.setProperty("General.SessionId", session.getUniqueId().toString());
		} else {
			Long sessionId = config.getPropertyLong("General.SessionId", null);
			if (sessionId == null) {
				throw new RuntimeException("Academic session id not provided.");
			}
			session = SessionDAO.getInstance().get(sessionId, getHibSession());
			if (session == null) {
				throw new RuntimeException("Academic session " + sessionId + " does not exist.");
			}
		}
		config.setProperty("General.Save","false");
		config.setProperty("General.CreateNewSolution","false");
		config.setProperty("General.Unload","false");
		return config;
	}
	
	protected byte[] toData(Element root) throws UnsupportedEncodingException, IOException {
		Element configEl = root.element("configuration");
		if (configEl != null) root.remove(configEl);
		
		ByteArrayOutputStream ret = new ByteArrayOutputStream();
		(new XMLWriter(ret, OutputFormat.createCompactFormat())).write(root.getDocument());
        ret.flush(); ret.close();
        return ret.toByteArray();
	}

}
