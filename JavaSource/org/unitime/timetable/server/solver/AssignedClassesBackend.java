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
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignedClassesRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignedClassesResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
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
		boolean usePrefStyles = CommonValues.Yes.eq(UserProperty.HighContrastPreferences.get(context.getUser()));
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		String subjects = request.getFilter().getParameterValue("subjectArea");
		context.setAttribute(SessionAttribute.OfferingsSubjectArea, isAllSubjects(subjects) ? Constants.ALL_OPTION_VALUE : request.getFilter().getParameterValue("subjectArea"));
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
			
			CellInterface studentConfs = new CellInterface().setTextAlignment(Alignment.RIGHT);
			studentConfs.setComparable(ci.getNrStudentConflicts(), ci.getNrCommitedStudentConflicts(),
					ci.getNrDistanceStudentConflicts(), ci.getNrHardStudentConflicts());
			studentConfs.setNoWrap(true);
			studentConfs.addItem(dispNumber(ci.getNrStudentConflicts()));

			if (ci.getNrCommitedStudentConflicts()!=0) {
				if (studentConfs.getNrItems() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
				studentConfs.addItem(dispNumber("c", ci.getNrCommitedStudentConflicts()));
    	    }
    	    if (ci.getNrDistanceStudentConflicts()!=0) {
    	    	if (studentConfs.getNrItems() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    	    	studentConfs.addItem(dispNumber("d", ci.getNrDistanceStudentConflicts()));
    	    }
    	    if (ci.getNrHardStudentConflicts()!=0) {
    	    	if (studentConfs.getNrItems() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    	    	studentConfs.addItem(dispNumber("h", ci.getNrHardStudentConflicts()));
    	    }
    	    if (studentConfs.getNrItems()>1) studentConfs.add(")");
    	    
    	    CellInterface rooms = new CellInterface();
    	    if (ca.getAssignedRoom() != null) {
    	    	for (int i=0;i<ca.getAssignedRoom().length;i++) {
    	    		if (i > 0) rooms.add(", ");
    	    		rooms.addRoom(ca.getAssignedRoom()[i].getName(),
    	    				usePrefStyles ? null : ca.getAssignedRoom()[i].getColor(),
    	    				ca.getAssignedRoom()[i].getId(),
    	    				PreferenceLevel.int2string(ca.getAssignedRoom()[i].getPref()),
    	    				usePrefStyles ? "pref-" + PreferenceLevel.int2char(ca.getAssignedRoom()[i].getPref()) : null
    	    				);
    	    	}
    	    } else if (ca.getRoom()!=null) {
    	    	for (int i=0;i<ca.getRoom().length;i++) {
    	    		if (i > 0) rooms.add(", ");
    	    		rooms.addRoom(
    	    				ca.getRoom()[i].getName(),
    	    				usePrefStyles ? null : ca.getRoom()[i].getColor(),
    	    				ca.getRoom()[i].getId(),
    	    				PreferenceLevel.int2string(ca.getRoom()[i].getPref()),
    	    				usePrefStyles ? "pref-" + PreferenceLevel.int2char(ca.getRoom()[i].getPref()) : null
    	    				);
    	    	}
    	    }
    	    CellInterface instructors = new CellInterface().setClassName("collection");
    	    if (ca.getInstructor() != null)
    	    	for (int i = 0; i < ca.getInstructor().length; i++) {
    	    		instructors.add(ca.getInstructor()[i].getName() + (i + 1 < ca.getInstructor().length ? ", " : ""))
    	    			.setColor(ca.getInstructor()[i].getColor()).setNoWrap(true).setClassName("item");
    	    	}
    	    TimeInfo time = (ca.getAssignedTime() != null ? ca.getAssignedTime() : ca.getTime());
    	    
    	    boolean showClassDetail = (solver == null && context.hasPermission(ca.getClazz().getClassId(), "Class_", Right.ClassDetail));
    	    
	    	LineInterface line = response.addLine();
	    	line.setId(ca.getClazz().getClassId());
	    	line.setURL(showClassDetail ? "classDetail.action?cid=" + ca.getClazz().getClassId() : "suggestions?menu=hide&id="+ca.getClazz().getClassId());
	    	line.setDialog(showClassDetail ? null : MESSAGES.dialogSuggestions());
	    	CellInterface clazz = line.addCell().setClassName("collection");
	    	clazz.add(ca.getClazz().getName()).setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref())).setInline(false).setNoWrap(true);
	    	line.addCell().add(time.getDatePatternName())
	    		.setColor(usePrefStyles ? null : PreferenceLevel.int2color(time.getDatePatternPreference()))
	    		.setClassName(usePrefStyles ? "pref-" + PreferenceLevel.int2char(time.getPref()) : null);
	    	line.addCell().setComparable(getOrder(time))
	    		.add(time.getDaysName() + " " + time.getStartTime() + " - " + time.getEndTime())
	    		.setColor(usePrefStyles ? null : PreferenceLevel.int2color(time.getPref()))
	    		.setClassName(usePrefStyles ? "pref-" + PreferenceLevel.int2char(time.getPref()) : null)
	    		.setNoWrap(true)
	    		.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement, '" + ca.getClazz().getClassId() + "," + time.getDays() + "," + time.getStartSlot() + "')")
	    		.setMouseOut("$wnd.hideGwtTimeHint()")
	    		;
	    	line.addCell(rooms);
	    	line.addCell(instructors);
	    	line.addCell(studentConfs);
	    	if (!simple) {
    	    	line.addCell(dispNumber(ci.getTimePreference()));
    	    	line.addCell(dispNumber(ci.sumRoomPreference()));
    	    	line.addCell(dispNumber(ci.getGroupConstraintPref()));
    	    	line.addCell(dispNumber(ci.getBtbInstructorPreference()));
    	    	line.addCell(dispNumber(ci.getUselessHalfHours()));
    	    	line.addCell(dispNumber(ci.getTooBigRoomPreference()));
    	    	line.addCell(dispNumber(ci.getDeptBalancPenalty()));
    	    	line.addCell(dispNumber(ci.getSpreadPenalty()));
    	    	line.addCell(dispNumber(ci.getPerturbationPenalty()));
	    	}
		}
		
		LineInterface line = response.addHeader();
		line.addCell(MESSAGES.colClass());
		line.addCell(MESSAGES.colDate());
		line.addCell(MESSAGES.colTime());
		line.addCell(MESSAGES.colRoom());
		line.addCell(MESSAGES.colInstructor());
		if (simple) {
			line.addCell(MESSAGES.colNrStudentConflicts());
		} else {
			line.addCell(MESSAGES.colShortStudentConflicts());
			line.addCell(MESSAGES.colShortTimePref());
			line.addCell(MESSAGES.colShortRoomPref());
			line.addCell(MESSAGES.colShortDistPref());
			line.addCell(MESSAGES.colShortInstructorBtbPref());
			line.addCell(MESSAGES.colShortUselessHalfHours());
			line.addCell(MESSAGES.colShortTooBigRooms());
			line.addCell(MESSAGES.colShortDepartmentBalance());
			line.addCell(MESSAGES.colShortSameSubpartBalance());
			line.addCell(MESSAGES.colShortPerturbations());
		}
		for (int i = 0; i < line.getNrCells(); i++) {
			CellInterface cell = line.getCell(i);
			cell.setSortable(true);
			cell.setClassName("unitime-ClickableTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		if (i > 4)
    			cell.setTextAlignment(Alignment.RIGHT);
		}
		response.setId("AssignedClasses");
		response.setName(MESSAGES.sectAssignedClasses());
		response.setClassName("unitime-DataTable");
		
		SolverPageBackend.fillSolverWarnings(context, solver, SolverType.COURSE, response);
		BackTracker.markForBack(context, "assignedClasses", MESSAGES.pageAssignedClasses(), true, true);
		
		if (ApplicationProperty.TimeGridShowCrosslists.isTrue())
			addCrosslistedNames(response, ApplicationProperty.SolverShowClassSufix.isTrue(), ApplicationProperty.SolverShowConfiguratioName.isTrue());
		
		return response;
	}
	
	public CellInterface dispNumber(int value) {
		return new CellInterface().setText(String.valueOf(value)).setComparable(value)
				.setColor(value < 0 ? "#1d6600" : value > 0 ? "#b80000" : null)
				.setTextAlignment(Alignment.RIGHT);
	}
	
	public CellInterface dispNumber(String prefix, int value) {
		return new CellInterface().setText(prefix + value).setComparable(value)
				.setColor(value < 0 ? "#1d6600" : value > 0 ? "#b80000" : null)
				.setTextAlignment(Alignment.RIGHT);
	}
	
	public CellInterface dispNumber(double value) {
		return new CellInterface().setText(sDF.format(value)).setComparable(value)
				.setColor(value < 0 ? "#1d6600" : value > 0 ? "#b80000" : null)
				.setTextAlignment(Alignment.RIGHT);
	}
	
	public static void addCrosslistedNames(TableInterface table, boolean showClassSuffix, boolean showConfigNames) {
		Map<Long, LineInterface> id2row = new HashMap<Long, LineInterface>();
		if (table.hasLines())
			for (LineInterface row: table.getLines()) {
				if (row.hasId()) id2row.put(row.getId(), row);
			}
		if (id2row.isEmpty()) return;
		if (id2row.size() <= 1000) {
			for (Object[] o: Class_DAO.getInstance().getSession().createQuery(
					"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
					"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr", Object[].class)
					.setParameterList("classIds", id2row.keySet(), Long.class).setCacheable(true).list()) {
				Class_ clazz = (Class_)o[0];
				CourseOffering course = (CourseOffering)o[1];
				LineInterface row = id2row.get(clazz.getUniqueId());
				if (row != null)
					row.getCell(0).add(clazz.getClassLabel(course, showClassSuffix, showConfigNames))
						.setIndent(1).setInline(false).setClassName("alternative").setNoWrap(true).setColor("#767676");
			}
		} else {
			List<Long> ids = new ArrayList<Long>(1000);
			for (Long id: id2row.keySet()) {
				ids.add(id);
				if (ids.size() == 1000) {
					for (Object[] o: Class_DAO.getInstance().getSession().createQuery(
							"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr", Object[].class)
							.setParameterList("classIds", ids, Long.class).setCacheable(true).list()) {
						Class_ clazz = (Class_)o[0];
						CourseOffering course = (CourseOffering)o[1];
						LineInterface row = id2row.get(clazz.getUniqueId());
						if (row != null)
							row.getCell(0).add(clazz.getClassLabel(course, showClassSuffix, showConfigNames))
								.setIndent(1).setInline(false).setClassName("alternative").setNoWrap(true).setColor("#767676");
					}
					ids.clear();
				}
			}
			if (!ids.isEmpty()) {
				for (Object[] o: Class_DAO.getInstance().getSession().createQuery(
						"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
						"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr", Object[].class)
						.setParameterList("classIds", ids, Long.class).setCacheable(true).list()) {
					Class_ clazz = (Class_)o[0];
					CourseOffering course = (CourseOffering)o[1];
					LineInterface row = id2row.get(clazz.getUniqueId());
					if (row != null)
						row.getCell(0).add(clazz.getClassLabel(course, showClassSuffix, showConfigNames))
							.setIndent(1).setInline(false).setClassName("alternative").setNoWrap(true).setColor("#767676");
				}
			}
		}
	}
}
