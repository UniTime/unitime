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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolutionChangesRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolutionChangesResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableSolver.RecordedAssignment;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails.RoomInfo;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.webutil.BackTracker;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SolutionChangesRequest.class)
public class SolutionChangesBackend implements GwtRpcImplementation<SolutionChangesRequest, SolutionChangesResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static DecimalFormat sDF = new DecimalFormat("0.###",new java.text.DecimalFormatSymbols(Locale.US));
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public SolutionChangesResponse execute(SolutionChangesRequest request, SessionContext context) {
		context.checkPermission(Right.SolutionChanges);
		SolutionChangesResponse response = new SolutionChangesResponse();
		
		context.getUser().setProperty("SuggestionsModel.simpleMode", request.getFilter().getParameterValue("simpleMode"));
		boolean simple = "1".equals(request.getFilter().getParameterValue("simpleMode"));
		
		context.getUser().setProperty("SolutionChanges.reference", request.getFilter().getParameterValue("reference"));
		int reference = Integer.valueOf(request.getFilter().getParameterValue("reference"));
		
		context.getUser().setProperty("SolutionChanges.reversedMode", request.getFilter().getParameterValue("reversedMode"));
		boolean reversed = "1".equals(request.getFilter().getParameterValue("reversedMode"));
		
		boolean usePrefStyles = CommonValues.Yes.eq(UserProperty.HighContrastPreferences.get(context.getUser()));
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		
		List<RecordedAssignment> changes = null;
		if (solver==null) {
			response.setMessage(MESSAGES.errorNoSolverLoaded());
        } else if (reference == 0) {
        	if (solver.bestSolutionInfo() == null)
        		response.setMessage(MESSAGES.errorNoBestSolutionSaved());
        	else
        		changes = solver.getChangesToBest();
        } else if (reference == 1) {
        	changes = solver.getChangesToInitial();
        } else if (reference == 2) {
        	String solutionIdsStr = (String)context.getAttribute(SessionAttribute.SelectedSolution);
        	if (solutionIdsStr == null || solutionIdsStr.isEmpty()) {
        		response.setMessage(MESSAGES.errorNoSolutionSelected());
    		} else {
    			changes = new ArrayList<RecordedAssignment>();
    			for (StringTokenizer s = new StringTokenizer(solutionIdsStr,","); s.hasMoreTokens();) {
    				Long solutionId = Long.valueOf(s.nextToken());
    				List<RecordedAssignment> ch = solver.getChangesToSolution(solutionId);
    				if (ch != null)
    					changes.addAll(ch);
    			}
    		}
        } else {
        	changes = new ArrayList<RecordedAssignment>();
        	boolean hasCommittedSolution = false;
        	for (Long ownerId: solver.getProperties().getPropertyLongArry("General.SolverGroupId", null)) {
        		Long solutionId = SolutionDAO.getInstance().getSession().createQuery(
        				"select uniqueId from Solution where owner.uniqueId = :ownerId and commited = true", Long.class
        				).setParameter("ownerId", ownerId).setMaxResults(1).uniqueResult();
        		if (solutionId != null) {
        			hasCommittedSolution = true;
        			List<RecordedAssignment> ch = solver.getChangesToSolution(solutionId);
    				if (ch != null)
    					changes.addAll(ch);
        		}
        	}
        	if (!hasCommittedSolution)
        		response.setMessage(MESSAGES.errorListSolutionsNoCommitted());
        }
		
		if (changes != null)
			for (RecordedAssignment assignment: changes) {
				ClassAssignmentDetails before = (assignment.getBefore()==null?null:assignment.getBefore().getDetails(context, solver, false));
    	    	ClassAssignmentDetails after = (assignment.getAfter()==null?null:assignment.getAfter().getDetails(context, solver, false));
    	    	if (reversed) {
    	    		ClassAssignmentDetails x = after; after = before; before = x;
    	    	}
    	    	ClassAssignmentDetails ca = (after==null?before:after);
    	    	
        		ClassAssignmentDetails.TimeInfo timeBefore = (before == null ? null : before.getTime());
        		ClassAssignmentDetails.TimeInfo timeAfter = (after == null ? null : after.getTime());
    	    	
    	    	CellInterface d = new CellInterface(); d.setNoWrap(true);
        		if (timeBefore!=null && (timeAfter==null || !timeBefore.getDatePatternId().equals(timeAfter.getDatePatternId()))) {
        			d.addItem(timeBefore.toDateCell(usePrefStyles)).add(" \u2192 ");
        			d.setComparable(timeBefore.getDatePatternName(), -timeBefore.getDays(), timeBefore.getStartSlot(), timeBefore.getMin());
        		} if (timeBefore==null && timeAfter!=null)
        			d.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
        			.add(" \u2192 ");
        		if (timeAfter == null) {
        			d.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
        		} else {
        			d.addItem(timeAfter.toDateCell(usePrefStyles));
        			if (!d.hasComparable()) d.setComparable(timeAfter.getDatePatternName(), -timeAfter.getDays(), timeAfter.getStartSlot(), timeAfter.getMin());
        		}
        		
        		CellInterface t = new CellInterface(); t.setNoWrap(true);
        		if (timeBefore!=null && (timeAfter==null || timeBefore.getDays() != timeAfter.getDays() || timeBefore.getStartSlot() != timeAfter.getStartSlot() || timeBefore.getMin() != timeAfter.getMin())) {
        			t.addItem(timeBefore.toCell(usePrefStyles)).add(" \u2192 ");
        			t.setComparable(-timeBefore.getDays(), timeBefore.getStartSlot(), timeBefore.getMin());
        		}
        		if (timeBefore==null && timeAfter!=null)
        			t.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
        			.add(" \u2192 ");
        		if (timeAfter == null) {
        			t.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
        		} else {
        			t.addItem(timeAfter.toCell(usePrefStyles));
        			if (!t.hasComparable()) t.setComparable(-timeAfter.getDays(), timeAfter.getStartSlot(), timeAfter.getMin());
        		}
    	    	
    	    	String link = "id=" + ca.getClazz().getClassId();
    	    	if (timeBefore != null)
    	    		link += "&days=" + timeBefore.getDays() + "&slot=" + timeBefore.getStartSlot() + "&pid=" + timeBefore.getPatternId() + "&did=" + before.getTime().getDatePatternId();
    	    	
    	    	ClassAssignmentDetails.RoomInfo[] roomBefore = (before == null ? null : before.getRoom());
    	    	ClassAssignmentDetails.RoomInfo[] roomAfter = (after == null ? null : after.getRoom());
    	    	
    	    	CellInterface r = new CellInterface(); r.setNoWrap(true);
        		if (roomBefore!=null && (roomAfter==null || !toId(roomBefore).equals(toId(roomAfter))))
        			r.addItem(toRoomCell(roomBefore, usePrefStyles)).add(" \u2192 ");
        		if (roomBefore==null && roomAfter!=null)
        			r.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
        			.add(" \u2192 ");
        		if (roomAfter == null)
        			r.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
        		else
        			r.addItem(toRoomCell(roomAfter, usePrefStyles));
        		
        		if (roomBefore != null)
        			link += "&room=" + toId(roomBefore);
    	    	
        		CellInterface instructors = new CellInterface().setClassName("collection");
        	    if (ca.getInstructor() != null)
        	    	for (int i = 0; i < ca.getInstructor().length; i++) {
        	    		instructors.add(ca.getInstructor()[i].getName() + (i + 1 < ca.getInstructor().length ? ", " : ""))
        	    			.setColor(ca.getInstructor()[i].getColor()).setNoWrap(true).setClassName("item");
        	    	}
        	    
    	        AssignmentPreferenceInfo bInf = (before==null?null:before.getInfo());
    	        AssignmentPreferenceInfo aInf = (after==null?null:after.getInfo());
    	        if (aInf==null) aInf = new AssignmentPreferenceInfo();
    	        if (bInf==null) bInf = new AssignmentPreferenceInfo();
    	        
    	        CellInterface studentConfs = new CellInterface().setTextAlignment(Alignment.RIGHT);
    			studentConfs.setNoWrap(true);
    			studentConfs.addItem(dispNumber(aInf.getNrStudentConflicts() - bInf.getNrStudentConflicts()));
    			if (aInf.getNrCommitedStudentConflicts()-bInf.getNrCommitedStudentConflicts()!=0) {
    				if (studentConfs.getNrItems() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    				studentConfs.addItem(dispNumber("c", aInf.getNrCommitedStudentConflicts()-bInf.getNrCommitedStudentConflicts()));
        	    }
        	    if (aInf.getNrDistanceStudentConflicts()-bInf.getNrDistanceStudentConflicts()!=0) {
    				if (studentConfs.getNrItems() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    				studentConfs.addItem(dispNumber("d", aInf.getNrDistanceStudentConflicts()-bInf.getNrDistanceStudentConflicts()));
        	    }
        	    if (aInf.getNrHardStudentConflicts()-bInf.getNrHardStudentConflicts()!=0) {
    				if (studentConfs.getNrItems() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    				studentConfs.addItem(dispNumber("h", aInf.getNrHardStudentConflicts()-bInf.getNrHardStudentConflicts()));
        	    }
        	    if (studentConfs.getNrItems()>1) studentConfs.add(")");
        	    studentConfs.setComparable(
        	    		aInf.getNrStudentConflicts() - bInf.getNrStudentConflicts(),
        	    		aInf.getNrCommitedStudentConflicts()-bInf.getNrCommitedStudentConflicts(),
        	    		aInf.getNrDistanceStudentConflicts()-bInf.getNrDistanceStudentConflicts(),
        	    		aInf.getNrHardStudentConflicts()-bInf.getNrHardStudentConflicts());
        	    
        	    boolean showClassDetail = (solver == null && context.hasPermission(ca.getClazz().getClassId(), "Class_", Right.ClassDetail));
        	    
    	    	LineInterface line = response.addLine();
    	    	line.setId(ca.getClazz().getClassId());
    	    	line.setURL(showClassDetail ? "classDetail.action?cid=" + ca.getClazz().getClassId() : "suggestions?menu=hide&" + link);
    	    	line.setDialog(showClassDetail ? null : MESSAGES.dialogSuggestions());
    	    	
    	    	CellInterface clazz = line.addCell().setClassName("collection");
    	    	clazz.add(ca.getClazz().getName()).setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref())).setInline(false).setNoWrap(true);

    	    	line.addCell(d);
    	    	line.addCell(t);
    	    	line.addCell(r);
    	    	line.addCell(instructors);
    	    	line.addCell(studentConfs);
        	    
        	    if (!simple) {
        	    	line.addCell(dispNumber(aInf.getTimePreference()-bInf.getTimePreference()));
        	    	line.addCell(dispNumber(aInf.sumRoomPreference()-bInf.sumRoomPreference()));
        	    	line.addCell(dispNumber(aInf.getGroupConstraintPref()-bInf.getGroupConstraintPref()));
        	    	line.addCell(dispNumber(aInf.getBtbInstructorPreference()-bInf.getBtbInstructorPreference()));
        	    	line.addCell(dispNumber(aInf.getUselessHalfHours()-bInf.getUselessHalfHours()));
        	    	line.addCell(dispNumber(aInf.getTooBigRoomPreference()-bInf.getTooBigRoomPreference()));
        	    	line.addCell(dispNumber(aInf.getDeptBalancPenalty()-bInf.getDeptBalancPenalty()));
        	    	line.addCell(dispNumber(aInf.getSpreadPenalty()-bInf.getSpreadPenalty()));
        	    	line.addCell(dispNumber(aInf.getPerturbationPenalty()-bInf.getPerturbationPenalty()));
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
		response.setId("SolutionChanges");
		response.setName(MESSAGES.sectSolutionChanges());
		response.setClassName("unitime-DataTable");
		
		SolverPageBackend.fillSolverWarnings(context, solver, SolverType.COURSE, response);
		BackTracker.markForBack(context, "solutionChanges", MESSAGES.pageSolutionChanges(), true, true);
		
		if (ApplicationProperty.TimeGridShowCrosslists.isTrue())
			addCrosslistedNames(response, ApplicationProperty.SolverShowClassSufix.isTrue(), ApplicationProperty.SolverShowConfiguratioName.isTrue());
		
		return response;
	}

	public static CellInterface dispNumber(int value) {
		return dispNumber(null, value);
	}
	
	public static CellInterface dispNumber(String prefix, int value) {
		return new CellInterface().setText(value == 0 ? "" : (prefix == null ? "" : prefix) +  (value <= 0 ? String.valueOf(value) : "+" + String.valueOf(value))).setComparable(value)
				.setColor(value < 0 ? "#1d6600" : value > 0 ? "#b80000" : null)
				.setTextAlignment(Alignment.RIGHT);
	}
	
	public static CellInterface dispNumber(double value) {
		return dispNumber(null, value);
	}
	
	public static CellInterface dispNumber(String prefix, double value) {
		return new CellInterface().setText(Math.round(1000.0 * value) == 0.0 ? "" : (prefix == null ? "" : prefix) + (value >= 0.0005 ? "+" : "") + sDF.format(value)).setComparable(value)
				.setColor(value < 0 ? "#1d6600" : value > 0 ? "#b80000" : null)
				.setTextAlignment(Alignment.RIGHT);
	}
	
	private static String toId(ClassAssignmentDetails.RoomInfo[] rooms) {
		String ret = "";
		if (rooms != null)
			for (RoomInfo rm: rooms) {
				ret += (ret.isEmpty() ? "" : ",") + rm.getId();
			}
		return ret;
	}
	
	private static  CellInterface toRoomCell(ClassAssignmentDetails.RoomInfo[] rooms, boolean usePrefStyles) {
		CellInterface r = new CellInterface();
		if (rooms != null)
			for (RoomInfo rm: rooms) {
				if (r.hasItems()) r.add(", ").setInline(true);
				r.addItem(rm.toCell(usePrefStyles).setInline(true));
			}
		return r;
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
					"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr",
					Object[].class).setParameterList("classIds", id2row.keySet(), Long.class).setCacheable(true).list()) {
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
							"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr",
							Object[].class).setParameterList("classIds", ids, Long.class).setCacheable(true).list()) {
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
