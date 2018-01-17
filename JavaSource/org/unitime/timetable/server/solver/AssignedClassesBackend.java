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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.type.LongType;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.TableInterface;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignedClassesRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignedClassesResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellInterface;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellMulti;
import org.unitime.timetable.gwt.shared.TableInterface.TableHeaderIterface;
import org.unitime.timetable.gwt.shared.TableInterface.TableRowInterface;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.SuggestionsModel;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails.TimeInfo;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(AssignedClassesRequest.class)
public class AssignedClassesBackend implements GwtRpcImplementation<AssignedClassesRequest, AssignedClassesResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static DecimalFormat sDF = new DecimalFormat("0.###",new java.text.DecimalFormatSymbols(Locale.US));
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	public static boolean isAllSubjects(String subjects) {
		if (subjects == null || subjects.isEmpty() || subjects.equals(Constants.ALL_OPTION_VALUE)) return true;
		for (String id: subjects.split(","))
			if (Constants.ALL_OPTION_VALUE.equals(id)) return true;
		return false;
	}
	
	public static int getOrder(TimeInfo time) {
		int firstWorkDay = ApplicationProperty.TimePatternFirstDayOfWeek.intValue();
		int days = time.getDays();
		if (firstWorkDay != 0) {
			days = time.getDays() << firstWorkDay;
			days = (days & 127) + (days >> 7);
		}
		return (Constants.sDayCode2Order[days] << 18) + (time.getStartSlot() << 11) + time.getMin();
	}

	@Override
	public AssignedClassesResponse execute(AssignedClassesRequest request, SessionContext context) {
		context.checkPermission(Right.AssignedClasses);
		AssignedClassesResponse response = new AssignedClassesResponse();
		
		context.getUser().setProperty("SuggestionsModel.simpleMode", request.getFilter().getParameterValue("simpleMode"));
		boolean simple = "1".equals(request.getFilter().getParameterValue("simpleMode"));
		SuggestionsModel model = (SuggestionsModel)context.getAttribute("Suggestions.model");
		if (model != null)
			model.setSimpleMode(simple);
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		String subjects = request.getFilter().getParameterValue("subjectArea");
		context.setAttribute(SessionAttribute.OfferingsSubjectArea, isAllSubjects(subjects) ? Constants.ALL_OPTION_VALUE : request.getFilter().getParameterValue("subjectArea"));
		String instructorNameFormat = UserProperty.NameFormat.get(context.getUser());

		String solutionIdsStr = (String)context.getAttribute(SessionAttribute.SelectedSolution);
		if (solver == null) {
	    	if (solutionIdsStr == null || solutionIdsStr.isEmpty()) {
	    		for (SolverGroup g: SolverGroup.getUserSolverGroups(context.getUser())) {
	        		for (Long id: (List<Long>)SolutionDAO.getInstance().getSession().createQuery(
	        				"select s.uniqueId from Solution s where s.commited = true and s.owner = :groupId")
	        				.setLong("groupId", g.getUniqueId()).setCacheable(true).list()) {
	        			if (solutionIdsStr == null)
	        				solutionIdsStr = id.toString();
	        			else
	        				solutionIdsStr += (solutionIdsStr.isEmpty() ? "" : ",") + id;
	        		}
	    		}
	    	}
			if (solutionIdsStr == null || solutionIdsStr.isEmpty()) 
				throw new GwtRpcException(MESSAGES.errorAssignedClassesNoSolution());
		}
		
		List<ClassAssignmentDetails> assignedClasses = new ArrayList<ClassAssignmentDetails>();
		if (isAllSubjects(subjects)) {
			if (solver != null) {
				assignedClasses = solver.getAssignedClasses();
			} else {
				org.hibernate.Session hibSession = SolutionDAO.getInstance().getSession();
				for (String solutionId: solutionIdsStr.split(",")) {
					Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionId));
					try {
						for (Assignment a: solution.getAssignments()) {
							assignedClasses.add(new ClassAssignmentDetails(solution, a, false, hibSession, instructorNameFormat));
						}
					} catch (ObjectNotFoundException e) {
						hibSession.refresh(solution);
						for (Assignment a: solution.getAssignments()) {
							assignedClasses.add(new ClassAssignmentDetails(solution, a, false, hibSession, instructorNameFormat));
						}
					}
				}
			}
		} else {
			org.hibernate.Session hibSession = SolutionDAO.getInstance().getSession();
			for (String id: subjects.split(",")) {
				String prefix = request.getFilter().getParameter("subjectArea").getOptionText(id) + " ";
				if (solver != null) {
					assignedClasses.addAll(solver.getAssignedClasses(prefix));
				} else {
					for (String solutionId: solutionIdsStr.split(",")) {
    					Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionId));
    					try {
    						for (Assignment a: solution.getAssignments()) {
    							if (prefix != null && !a.getClassName().startsWith(prefix)) continue;
    							assignedClasses.add(new ClassAssignmentDetails(solution, a, false, hibSession, instructorNameFormat));
    						}
    					} catch (ObjectNotFoundException e) {
    						hibSession.refresh(solution);
    						for (Assignment a: solution.getAssignments()) {
    							if (prefix != null && !a.getClassName().startsWith(prefix)) continue;
    							assignedClasses.add(new ClassAssignmentDetails(solution, a, false, hibSession, instructorNameFormat));
    						}
    					}
    				}
				}
			}
		}
		
		Collections.sort(assignedClasses);
		for (ClassAssignmentDetails ca: assignedClasses) {
			AssignmentPreferenceInfo ci = ca.getInfo();
			
			TableCellMulti studentConfs = new TableCellMulti();
			studentConfs.add(dispNumber(ci.getNrStudentConflicts()));

			if (ci.getNrCommitedStudentConflicts()!=0) {
				if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
				studentConfs.add(dispNumber(ci.getNrCommitedStudentConflicts()).setFormattedValue("c" + ci.getNrCommitedStudentConflicts()));
    	    }
    	    if (ci.getNrDistanceStudentConflicts()!=0) {
    	    	if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    	    	studentConfs.add(dispNumber(ci.getNrDistanceStudentConflicts()).setFormattedValue("d" + ci.getNrDistanceStudentConflicts()));
    	    }
    	    if (ci.getNrHardStudentConflicts()!=0) {
    	    	if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    	    	studentConfs.add(dispNumber(ci.getNrHardStudentConflicts()).setFormattedValue("h" + ci.getNrHardStudentConflicts()));
    	    }
    	    if (studentConfs.getNrChunks()>1) studentConfs.add(")");
    	    
    	    TableInterface.TableCellRooms rooms = new TableInterface.TableCellRooms();
    	    if (ca.getAssignedRoom() != null) {
    	    	for (int i=0;i<ca.getAssignedRoom().length;i++) {
    	    		rooms.add(
    	    				ca.getAssignedRoom()[i].getName(),
    	    				ca.getAssignedRoom()[i].getColor(),
    	    				ca.getAssignedRoom()[i].getId(),
    	    				PreferenceLevel.int2string(ca.getAssignedRoom()[i].getPref()));
    	    	}
    	    } else if (ca.getRoom()!=null) {
    	    	for (int i=0;i<ca.getRoom().length;i++) {
    	    		rooms.add(
    	    				ca.getRoom()[i].getName(),
    	    				ca.getRoom()[i].getColor(),
    	    				ca.getRoom()[i].getId(),
    	    				PreferenceLevel.int2string(ca.getRoom()[i].getPref()));
    	    	}
    	    }
    	    TableInterface.TableCellItems instructors = new TableInterface.TableCellItems();
    	    if (ca.getInstructor() != null)
    	    	for (int i = 0; i < ca.getInstructor().length; i++) {
    	    		instructors.add(ca.getInstructor()[i].getName(), ca.getInstructor()[i].getColor(), ca.getInstructor()[i].getId());
    	    	}
    	    TimeInfo time = (ca.getAssignedTime() != null ? ca.getAssignedTime() : ca.getTime());
    	    
    	    boolean showClassDetail = (solver == null && context.hasPermission(ca.getClazz().getClassId(), "Class_", Right.ClassDetail));
    	    
    	    if (simple)
    	    	response.addRow(new TableRowInterface(
    	    			ca.getClazz().getClassId(),
    	    			(showClassDetail ? "classDetail.do?cid=" + ca.getClazz().getClassId() : "gwt.jsp?page=suggestions&menu=hide&id="+ca.getClazz().getClassId()),
    	    			(showClassDetail ? null : MESSAGES.dialogSuggestions()),
    	    			new TableInterface.TableCellClassName(ca.getClazz().getName()).setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref())),
    	    			new TableCellInterface(time.getDatePatternName()).setColor(PreferenceLevel.int2color(time.getDatePatternPreference())),
    	    			new TableInterface.TableCellTime(time.getDaysName() + " " + time.getStartTime() + " - " + time.getEndTime()).setOrder(getOrder(time))
    	    				.setId(ca.getClazz().getClassId() + "," + time.getDays() + "," + time.getStartSlot()).setColor(PreferenceLevel.int2color(time.getPref())),
    	    			rooms,
    	    			instructors,
    	    			studentConfs
    	    			));
    	    else
    	    	response.addRow(new TableRowInterface(
    	    			ca.getClazz().getClassId(),
    	    			(showClassDetail ? "classDetail.do?cid=" + ca.getClazz().getClassId() : "gwt.jsp?page=suggestions&menu=hide&id="+ca.getClazz().getClassId()),
        	    		(showClassDetail ? null : MESSAGES.dialogSuggestions()),
        	    		new TableInterface.TableCellClassName(ca.getClazz().getName()).setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref())),
        	    		new TableCellInterface(time.getDatePatternName()).setColor(PreferenceLevel.int2color(time.getDatePatternPreference())),
        	    		new TableInterface.TableCellTime(time.getDaysName() + " " + time.getStartTime() + " - " + time.getEndTime()).setOrder(getOrder(time))
    	    				.setId(ca.getClazz().getClassId() + "," + time.getDays() + "," + time.getStartSlot()).setColor(PreferenceLevel.int2color(time.getPref())),
        	    		rooms,
        	    		instructors,
        	    		studentConfs,
        	    		dispNumber(ci.getTimePreference()),
        	    		dispNumber(ci.sumRoomPreference()),
        	    		dispNumber(ci.getGroupConstraintPref()),
        	    		dispNumber(ci.getBtbInstructorPreference()),
        	    		dispNumber(ci.getUselessHalfHours()),
        	    		dispNumber(ci.getTooBigRoomPreference()),
        	    		dispNumber(ci.getDeptBalancPenalty()),
        	    		dispNumber(ci.getSpreadPenalty()),
        	    		dispNumber(ci.getPerturbationPenalty())
        	    		));
		}
		
		if (simple)
			response.setHeader(
					new TableHeaderIterface(MESSAGES.colClass()),
					new TableHeaderIterface(MESSAGES.colDate()),
					new TableHeaderIterface(MESSAGES.colTime()),
					new TableHeaderIterface(MESSAGES.colRoom()),
					new TableHeaderIterface(MESSAGES.colInstructor()),
					new TableHeaderIterface(MESSAGES.colNrStudentConflicts()));
		else
			response.setHeader(
					new TableHeaderIterface(MESSAGES.colClass()),
					new TableHeaderIterface(MESSAGES.colDate()),
					new TableHeaderIterface(MESSAGES.colTime()),
					new TableHeaderIterface(MESSAGES.colRoom()),
					new TableHeaderIterface(MESSAGES.colInstructor()),
					new TableHeaderIterface(MESSAGES.colShortStudentConflicts()),
					new TableHeaderIterface(MESSAGES.colShortTimePref()),
					new TableHeaderIterface(MESSAGES.colShortRoomPref()),
					new TableHeaderIterface(MESSAGES.colShortDistPref()),
					new TableHeaderIterface(MESSAGES.colShortInstructorBtbPref()),
					new TableHeaderIterface(MESSAGES.colShortUselessHalfHours()),
					new TableHeaderIterface(MESSAGES.colShortTooBigRooms()),
					new TableHeaderIterface(MESSAGES.colShortDepartmentBalance()),
					new TableHeaderIterface(MESSAGES.colShortSameSubpartBalance()),
					new TableHeaderIterface(MESSAGES.colShortPerturbations()));
		
		SolverPageBackend.fillSolverWarnings(context, solver, SolverType.COURSE, response);
		BackTracker.markForBack(context, "gwt.jsp?page=assignedClasses", MESSAGES.pageAssignedClasses(), true, true);
		
		if (ApplicationProperty.TimeGridShowCrosslists.isTrue())
			addCrosslistedNames(response, ApplicationProperty.SolverShowClassSufix.isTrue(), ApplicationProperty.SolverShowConfiguratioName.isTrue());
		
		return response;
	}
	
	public TableCellInterface dispNumber(int value) {
		return new TableCellInterface<Integer>(value).setColor(value < 0 ? "green" : value > 0 ? "red" : null);
	}
	
	public TableCellInterface dispNumber(double value) {
		return new TableCellInterface<Double>(value, sDF.format(value)).setColor(value < 0 ? "green" : value > 0 ? "red" : null);
	}
	
	public static void addCrosslistedNames(TableInterface table, boolean showClassSuffix, boolean showConfigNames) {
		Map<Long, TableInterface.TableRowInterface> id2row = new HashMap<Long, TableInterface.TableRowInterface>();
		for (TableInterface.TableRowInterface row: table.getRows()) {
			if (row.hasId()) id2row.put(row.getId(), row);
		}
		if (id2row.isEmpty()) return;
		if (id2row.size() <= 1000) {
			for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(
					"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
					"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr").setParameterList("classIds", id2row.keySet(), LongType.INSTANCE).setCacheable(true).list()) {
				Class_ clazz = (Class_)o[0];
				CourseOffering course = (CourseOffering)o[1];
				TableInterface.TableRowInterface row = id2row.get(clazz.getUniqueId());
				if (row != null)
					((TableInterface.TableCellClassName)row.getCell(0)).addAlternative(clazz.getClassLabel(course, showClassSuffix, showConfigNames));
			}
		} else {
			List<Long> ids = new ArrayList<Long>(1000);
			for (Long id: id2row.keySet()) {
				ids.add(id);
				if (ids.size() == 1000) {
					for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(
							"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr").setParameterList("classIds", ids, LongType.INSTANCE).setCacheable(true).list()) {
						Class_ clazz = (Class_)o[0];
						CourseOffering course = (CourseOffering)o[1];
						TableInterface.TableRowInterface row = id2row.get(clazz.getUniqueId());
						if (row != null)
							((TableInterface.TableCellClassName)row.getCell(0)).addAlternative(clazz.getClassLabel(course, showClassSuffix, showConfigNames));
					}
					ids.clear();
				}
			}
			if (!ids.isEmpty()) {
				for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(
						"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
						"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr").setParameterList("classIds", ids, LongType.INSTANCE).setCacheable(true).list()) {
					Class_ clazz = (Class_)o[0];
					CourseOffering course = (CourseOffering)o[1];
					TableInterface.TableRowInterface row = id2row.get(clazz.getUniqueId());
					if (row != null)
						((TableInterface.TableCellClassName)row.getCell(0)).addAlternative(clazz.getClassLabel(course, showClassSuffix, showConfigNames));
				}
			}
		}
	}
}
