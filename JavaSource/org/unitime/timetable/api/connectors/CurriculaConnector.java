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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.gwt.server.CurriculaServlet;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumFilterRpcRequest;
import org.unitime.timetable.gwt.shared.CurriculumInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.MajorInterface;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.curricula.CurriculumFilterBackend;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@Service("/api/curricula")
public class CurriculaConnector extends ApiConnector {
	
	@Override
	public void doGet(final ApiHelper helper) throws IOException {
		Long curriculumId = helper.getOptinalParameterLong("id", null);
		if (curriculumId != null) {
			Curriculum curriculum = CurriculumDAO.getInstance().get(curriculumId);
			if (curriculum == null)
				throw new IllegalArgumentException("Curriculum " + curriculumId + " does not exist.");
			final Long sessionId = curriculum.getAcademicArea().getSessionId();
			helper.getSessionContext().checkPermissionAnyAuthority(sessionId, "Session", Right.ApiRetrieveCurricula);
			
			CurriculaServlet servlet = new CurriculaServlet() {
	    		@Override
	    		protected SessionContext getSessionContext() { return helper.getSessionContext(); }
	    		@Override
	    		protected Long getAcademicSessionId() { return sessionId; }
	    	};
	    	
	    	helper.setResponse(servlet.loadCurriculum(curriculumId));
	    	return;
		}
		
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		CurriculumFilterRpcRequest filter = new CurriculumFilterRpcRequest();
		filter.setCommand(FilterRpcRequest.Command.ENUMERATE);
		filter.setSessionId(sessionId);
    	for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
    		String command = e.nextElement();
    		if (command.equals("c:text")) {
    			filter.setText(helper.getParameter("c:text"));
    		} else if (command.startsWith("c:")) {
    			for (String value: helper.getParameterValues(command))
    				filter.addOption(command.substring(2), value);
    		}
    	}
    	
    	helper.getSessionContext().checkPermissionAnyAuthority(sessionId, "Session", Right.ApiRetrieveCurricula);
    	
    	CurriculaServlet servlet = null;
    	if (helper.getOptinalParameterBoolean("details", false)) {
        	servlet = new CurriculaServlet() {
        		@Override
        		protected SessionContext getSessionContext() { return helper.getSessionContext(); }
        		@Override
        		protected Long getAcademicSessionId() { return helper.getAcademicSessionId(); }
        	};
    	}
    	
    	TreeSet<CurriculumInterface> results = new TreeSet<CurriculumInterface>();
		for (Curriculum c: CurriculumFilterBackend.curricula(sessionId, filter.getOptions(), new Query(filter.getText()), -1, null, Department.findAll(sessionId))) {
			if (servlet != null) {
				results.add(servlet.loadCurriculum(c.getUniqueId()));
			} else {
				CurriculumInterface ci = new CurriculumInterface();
				ci.setId(c.getUniqueId());
				ci.setAbbv(c.getAbbv());
				ci.setName(c.getName());
				ci.setMultipleMajors(c.isMultipleMajors());
				DepartmentInterface di = new DepartmentInterface();
				di.setId(c.getDepartment().getUniqueId());
				di.setAbbv(c.getDepartment().getAbbreviation());
				di.setCode(c.getDepartment().getDeptCode());
				di.setName(c.getDepartment().getName());
				ci.setDepartment(di);
				AcademicAreaInterface ai = new AcademicAreaInterface();
				ai.setId(c.getAcademicArea().getUniqueId());
				ai.setAbbv(c.getAcademicArea().getAcademicAreaAbbreviation());
				ai.setName(Constants.curriculaToInitialCase(c.getAcademicArea().getTitle()));
				ci.setAcademicArea(ai);
				for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
					PosMajor major = i.next();
					MajorInterface mi = new MajorInterface();
					mi.setId(major.getUniqueId());
					mi.setCode(major.getCode());
					mi.setName(Constants.curriculaToInitialCase(major.getName()));
					ci.addMajor(mi);
				}
				results.add(ci);
			}
		}
		
    	helper.setResponse(results);
	}

	@Override
	protected String getName() {
		return "curricula";
	}
}
