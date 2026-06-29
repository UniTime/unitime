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
package org.unitime.timetable.server.solver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.NotAssignedClassesRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.NotAssignedClassesResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.SolutionUnassignedClassesModel;
import org.unitime.timetable.solver.ui.UnassignedClassRow;
import org.unitime.timetable.solver.ui.UnassignedClassesModel;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(NotAssignedClassesRequest.class)
public class NotAssignedClassesBackend implements GwtRpcImplementation<NotAssignedClassesRequest, NotAssignedClassesResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static DecimalFormat sDF = new DecimalFormat("0.###",new java.text.DecimalFormatSymbols(Locale.US));
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public NotAssignedClassesResponse execute(NotAssignedClassesRequest request, SessionContext context) {
		context.checkPermission(Right.NotAssignedClasses);
		NotAssignedClassesResponse response = new NotAssignedClassesResponse();
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		String subjects = request.getFilter().getParameterValue("subjectArea");
		context.setAttribute(SessionAttribute.OfferingsSubjectArea, AssignedClassesBackend.isAllSubjects(subjects) ? Constants.ALL_OPTION_VALUE : request.getFilter().getParameterValue("subjectArea"));
		String instructorNameFormat = UserProperty.NameFormat.get(context.getUser());
		
		String solutionIdsStr = (String)context.getAttribute(SessionAttribute.SelectedSolution);
		if (solver == null) {
	    	if (solutionIdsStr == null || solutionIdsStr.isEmpty()) {
	    		for (SolverGroup g: SolverGroup.getUserSolverGroups(context.getUser())) {
	        		for (Long id: SolutionDAO.getInstance().getSession().createQuery(
	        				"select s.uniqueId from Solution s where s.commited = true and s.owner.uniqueId = :groupId", Long.class)
	        				.setParameter("groupId", g.getUniqueId()).setCacheable(true).list()) {
	        			if (solutionIdsStr == null)
	        				solutionIdsStr = id.toString();
	        			else
	        				solutionIdsStr += (solutionIdsStr.isEmpty() ? "" : ",") + id;
	        		}
	    		}
	    	}
			if (solutionIdsStr == null || solutionIdsStr.isEmpty()) 
				throw new GwtRpcException(MESSAGES.errorNotAssignedClassesNoSolution());
		}
		
		UnassignedClassesModel model = null;
		String[] prefixes = null;
		if (!AssignedClassesBackend.isAllSubjects(subjects)) {
			List<String> list = new ArrayList<String>();
			for (String id: subjects.split(",")) {
				list.add(request.getFilter().getParameter("subjectArea").getOptionText(id) + " ");
			}
			prefixes = list.toArray(new String[list.size()]);
		}
		if (solver != null) {
			model = solver.getUnassignedClassesModel(prefixes);
			response.setShowNote(true);
		} else {
			List<Solution> solutions = new ArrayList<Solution>();
			org.hibernate.Session hibSession = SolutionDAO.getInstance().getSession();
			for (String solutionId: solutionIdsStr.split(",")) {
				Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionId));
				if (solution != null) solutions.add(solution);
			}
			if (!solutions.isEmpty())
				model = new SolutionUnassignedClassesModel(solutions, hibSession, instructorNameFormat, prefixes);
		}
		
		if (model != null) {
			Collections.sort(model.rows());
			for (UnassignedClassRow ucr: model.rows()) {
				boolean showClassDetail = (solver == null && context.hasPermission(ucr.getId(), "Class_", Right.ClassDetail));
		    	LineInterface line = response.addLine();
		    	line.setId(ucr.getId());
		    	line.setURL(showClassDetail ? "classDetail.action?cid=" + ucr.getId() : "suggestions?menu=hide&id="+ucr.getId());
		    	line.setDialog(showClassDetail ? null : MESSAGES.dialogSuggestions());
		    	CellInterface clazz = line.addCell().setClassName("collection");
		    	clazz.add(ucr.getName()).setInline(false).setNoWrap(true);
		    	CellInterface instructors = line.addCell().setNoWrap(false);
		    	if (ucr.getInstructors() != null) {
		    		for (Iterator<String> i = ucr.getInstructors().iterator(); i.hasNext(); )
		    			instructors.add(i.next() + (i.hasNext() ? ", " : "")).setNoWrap(true);
		    	}
				line.addCell(String.valueOf(ucr.getNrStudents())).setComparable(ucr.getNrStudents()).setTextAlignment(Alignment.RIGHT);
		    	line.addCell(ucr.getInitial());
			}
		}
		
		LineInterface line = response.addHeader();
		line.addCell(MESSAGES.colClass());
		line.addCell(MESSAGES.colInstructor());
		line.addCell(MESSAGES.colNrAssignedStudents()).setTextAlignment(Alignment.RIGHT);
		line.addCell(MESSAGES.colInitialAssignment());
		for (CellInterface cell: line.getCells()) {
			cell.setSortable(true);
			cell.setClassName("unitime-ClickableTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
		}
		response.setId("NotAssignedClasses");
		response.setName(MESSAGES.sectNotAssignedClasses());
		response.setClassName("unitime-DataTable");
		
		SolverPageBackend.fillSolverWarnings(context, solver, SolverType.COURSE, response);
		BackTracker.markForBack(context, "notAssignedClasses", MESSAGES.pageNotAssignedClasses(), true, true);
		
		if (ApplicationProperty.TimeGridShowCrosslists.isTrue())
			AssignedClassesBackend.addCrosslistedNames(response, ApplicationProperty.SolverShowClassSufix.isTrue(), ApplicationProperty.SolverShowConfiguratioName.isTrue());
		
		return response;
	}

}
