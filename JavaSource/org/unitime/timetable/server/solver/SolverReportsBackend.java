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
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolverReportsRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolverReportsResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.SubpartDetailBackend;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails.RoomInfo;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails.TimeInfo;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.DeptBalancingReport;
import org.unitime.timetable.solver.ui.DiscouragedInstructorBtbReport;
import org.unitime.timetable.solver.ui.JenrlInfo;
import org.unitime.timetable.solver.ui.PerturbationReport;
import org.unitime.timetable.solver.ui.RoomReport;
import org.unitime.timetable.solver.ui.SameSubpartBalancingReport;
import org.unitime.timetable.solver.ui.StudentConflictsReport;
import org.unitime.timetable.solver.ui.ViolatedDistrPreferencesReport;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SolverReportsRequest.class)
public class SolverReportsBackend implements GwtRpcImplementation<SolverReportsRequest, SolverReportsResponse>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Formats.Format<Number> sDoubleFormat = Formats.getNumberFormat("0.00");
	protected static DecimalFormat sDF = new DecimalFormat("0.###",new java.text.DecimalFormatSymbols(Locale.US));
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public SolverReportsResponse execute(SolverReportsRequest request, SessionContext context) {
		context.checkPermission(Right.SolutionReports);
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		if (solver == null) throw new GwtRpcException(MESSAGES.warnSolverNotLoaded());
		
		Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
		BitSet sessionDays = session.getDefaultDatePattern().getPatternBitSet();
		int startDayDayOfWeek = Constants.getDayOfWeek(session.getDefaultDatePattern().getStartDate());
		Float nrWeeks = null;
		if (ApplicationProperty.TimetableGridUtilizationSkipHolidays.isFalse()) {
			DatePattern dp = session.getDefaultDatePatternNotNull();
			if (dp != null)
				nrWeeks = dp.getEffectiveNumberOfWeeks();
		}
		
		boolean usePrefStyles = CommonValues.Yes.eq(UserProperty.HighContrastPreferences.get(context.getUser()));
		
		SolverReportsResponse response = new SolverReportsResponse();
		
		for (RoomType type : RoomType.findAll()) {
            RoomReport roomReport = solver.getRoomReport(sessionDays, startDayDayOfWeek, type.getUniqueId(), nrWeeks);
            if (roomReport != null && !roomReport.getGroups().isEmpty()) {
            	response.addTable(getRoomReportTable(roomReport, type));
            }
        }
        RoomReport roomReport = solver.getRoomReport(sessionDays, startDayDayOfWeek, null, nrWeeks);
        if (roomReport != null && !roomReport.getGroups().isEmpty()) {
        	response.addTable(getRoomReportTable(roomReport, null));
        }
        
        if (response.hasTables()) {
        	// only the last one for the room reports
        	TableInterface table = response.getTables().get(response.getTables().size() - 1);
    		table.addProperty(MESSAGES.colRoomReportGroup()).setHtml(MESSAGES.reportRoomAlocDescGroup());
    		table.addProperty(MESSAGES.colRoomReportActualSizes()).setHtml(MESSAGES.reportRoomAlocDescSize());
    		table.addProperty(MESSAGES.colRoomReportNbrRooms()).setHtml(MESSAGES.reportRoomAlocDescNbrRooms());
    		table.addProperty(MESSAGES.colRoomReportClassUse()).setHtml(MESSAGES.reportRoomAlocDescClassUse());
    		table.addProperty(MESSAGES.colRoomReportClassShould()).setHtml(MESSAGES.reportRoomAlocDescClassShould());
    		table.addProperty(MESSAGES.colRoomReportClassMust()).setHtml(MESSAGES.reportRoomAlocDescClassMust());
    		table.addProperty(MESSAGES.colRoomReportHourUse()).setHtml(MESSAGES.reportRoomAlocDescHourUse());
    		table.addProperty(MESSAGES.colRoomReportHourShould()).setHtml(MESSAGES.reportRoomAlocDescHourShould());
    		table.addProperty(MESSAGES.colRoomReportHourMust()).setHtml(MESSAGES.reportRoomAlocDescHourMust());
        }
        
		ViolatedDistrPreferencesReport violatedDistrPreferencesReport = solver.getViolatedDistrPreferencesReport();
		if (violatedDistrPreferencesReport != null && !violatedDistrPreferencesReport.getGroups().isEmpty())
			response.addTable(getViolatedDistrPreferencesReportTable(violatedDistrPreferencesReport, false));
			
		DiscouragedInstructorBtbReport discouragedInstructorBtbReportReport = solver.getDiscouragedInstructorBtbReport();
		if (discouragedInstructorBtbReportReport!=null && !discouragedInstructorBtbReportReport.getGroups().isEmpty())
			response.addTable(getDiscouragedInstructorBtbReportReportTable(discouragedInstructorBtbReportReport, false));
        
		StudentConflictsReport studentConflictsReport = solver.getStudentConflictsReport();
		if (studentConflictsReport!=null && !studentConflictsReport.getGroups().isEmpty())
			response.addTable(getStudentConflictsReportTable(studentConflictsReport, false));
        
		DeptBalancingReport deptBalancingReport = solver.getDeptBalancingReport();
		if (deptBalancingReport != null && !deptBalancingReport.getGroups().isEmpty())
			response.addTable(getDeptBalancingReportTable(deptBalancingReport));
		
		SameSubpartBalancingReport sameSubpartBalancingReport = solver.getSameSubpartBalancingReport();
		if (sameSubpartBalancingReport!=null && !sameSubpartBalancingReport.getGroups().isEmpty())
			response.addTable(getSameSubpartBalancingReportTable(sameSubpartBalancingReport));

		PerturbationReport perturbationReport = solver.getPerturbationReport();
		if (perturbationReport!=null && !perturbationReport.getGroups().isEmpty())
			response.addTable(getPerturbationReportTable(perturbationReport, usePrefStyles));
		
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(true))
			response.addPreference(new PreferenceInterface(pref.getUniqueId(),
					PreferenceLevel.prolog2color(pref.getPrefProlog()), PreferenceLevel.prolog2bgColor(pref.getPrefProlog()),
					pref.getPrefProlog(), pref.getPrefName(), pref.getAbbreviation(), false));
		
		if (response.hasTables()) {
        	for (int i = 0; i < response.getTables().size(); i++) {
        		response.getTables().get(i).addStyle("white-space: nowrap");
        		for (LineInterface header: response.getTables().get(i).getHeader())
        			for (CellInterface cell: header.getCells()) {
        				cell.setSortable(true);
        				cell.setClassName("unitime-ClickableTableHeader");
        	    		cell.setText(cell.getText().replace("<br>", "\n"));
        	    		cell.addStyle("white-space: pre-wrap;");
        			}
        	}
        }
		
		return response;
	}
	
	public static TableInterface getRoomReportTable(RoomReport report, RoomType type) {
		TableInterface table = new TableInterface("report-rooms" + (type == null ? "" : "-" + type.getReference().toLowerCase()), type == null ? MESSAGES.reportRoomAllocationNonUnivLocs() : MESSAGES.reportRoomAllocation(type.getLabel()));
		
		LineInterface header = table.addHeader();
		header.addCell(MESSAGES.colRoomReportGroup());
		header.addCell(MESSAGES.colRoomReportActualSizes());
		header.addCell(MESSAGES.colRoomReportNbrRooms());
		header.addCell(MESSAGES.colRoomReportClassUse());
		header.addCell(MESSAGES.colRoomReportClassShould());
		header.addCell(MESSAGES.colRoomReportClassMust());
		header.addCell(MESSAGES.colRoomReportHourUse());
		header.addCell(MESSAGES.colRoomReportHourShould());
		header.addCell(MESSAGES.colRoomReportHourMust());
		
		int nrLines = 0;
        
        try {
            int nrAllRooms = 0, nrAllLectureUse = 0, nrAllLectureShouldUse = 0;
            double allSlotsUse = 0.0, allSlotsShouldUse = 0.0;
            
            TreeSet<RoomReport.RoomAllocationGroup> groups = new TreeSet<RoomReport.RoomAllocationGroup>(new Comparator<RoomReport.RoomAllocationGroup>() {
                public int compare(RoomReport.RoomAllocationGroup g1, RoomReport.RoomAllocationGroup g2) {
                    return -Double.compare(g1.getMinRoomSize(),g2.getMinRoomSize());
                }
            });
            groups.addAll(report.getGroups());
            
        	for (Iterator i=groups.iterator();i.hasNext();) {
        		RoomReport.RoomAllocationGroup g = (RoomReport.RoomAllocationGroup)i.next();
        		if (g.getNrRooms()==0) continue;
        		
        		double factor = ((double)Constants.SLOT_LENGTH_MIN) / 60.0;
                
                nrAllRooms+=g.getNrRooms();
                allSlotsUse+=g.getSlotsUse();
                allSlotsShouldUse+=g.getSlotsShouldUse();
                nrAllLectureUse+=g.getLecturesUse();
                nrAllLectureShouldUse+=g.getLecturesShouldUse();
                
                nrLines++;
                
                LineInterface line = table.addLine();
                line.addCell(MESSAGES.reportRoomRange(String.valueOf(g.getMinRoomSize()), (g.getMaxRoomSize() == Integer.MAX_VALUE ? "\u221E" : String.valueOf(g.getMaxRoomSize()))))
                	.setComparable(g.getMinRoomSize());
                line.addCell(MESSAGES.reportRoomRange(String.valueOf(g.getActualMinRoomSize()), String.valueOf(g.getActualMaxRoomSize())))
                	.setComparable(g.getActualMinRoomSize());
                line.addCell(g.getNrRooms()+" (" + g.getNrRoomsThisSizeOrBigger() + ")").setComparable(g.getNrRooms(), g.getMinRoomSize());
                line.addCell(g.getLecturesUse() + " (" + nrAllLectureUse + ")").setComparable(g.getLecturesUse(), g.getMinRoomSize());
                line.addCell(g.getLecturesShouldUse() + " (" + nrAllLectureShouldUse + ")").setComparable(g.getLecturesShouldUse(), g.getMinRoomSize());
                line.addCell(g.getLecturesMustUse() + " (" + g.getLecturesMustUseThisSizeOrBigger() + ")").setComparable(g.getLecturesMustUse(), g.getMinRoomSize());
                line.addCell(sDoubleFormat.format(factor*g.getSlotsUse()/g.getNrRooms())+" ("+sDoubleFormat.format(factor*allSlotsUse/nrAllRooms)+")")
                	.setComparable(factor*g.getSlotsUse()/g.getNrRooms(), g.getMinRoomSize());
                line.addCell(sDoubleFormat.format(factor*g.getSlotsShouldUse()/g.getNrRooms())+" ("+sDoubleFormat.format(factor*allSlotsShouldUse/nrAllRooms)+")")
                	.setComparable(factor*g.getSlotsShouldUse()/g.getNrRooms(), g.getMinRoomSize());
                line.addCell(sDoubleFormat.format(factor*g.getSlotsMustUse()/g.getNrRooms())+" ("+sDoubleFormat.format(factor*g.getSlotsMustUseThisSizeOrBigger()/g.getNrRoomsThisSizeOrBigger())+")")
                	.setComparable(factor*g.getSlotsMustUse()/g.getNrRooms(), g.getMinRoomSize());
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
        if (nrLines == 0 && !table.hasErrorMessage()) return null;
        table.setDefaultSortCookie(MESSAGES.colRoomReportGroup());

        return table;
	}
	
	public static TableInterface getDeptBalancingReportTable(DeptBalancingReport deptBalancingReport) {
		TableInterface table = new TableInterface("dept-balancing", MESSAGES.reportDepartmentalBalancing());
		LineInterface header = table.addHeader();
		header.addCell(MESSAGES.colDepartment());
		header.addCell(MESSAGES.colPenalty()).setTextAlignment(Alignment.RIGHT);
		for (int i = 0; i < deptBalancingReport.getSlotsPerDayNoEvening() / 6; i++)
			header.addCell(Constants.slot2str(deptBalancingReport.getFirstDaySlot() + i * 6)).setTextAlignment(Alignment.CENTER);
		
		try {
        	for (Iterator it=deptBalancingReport.getGroups().iterator();it.hasNext();) {
        		DeptBalancingReport.DeptBalancingGroup g = (DeptBalancingReport.DeptBalancingGroup)it.next();
        		
        		LineInterface line = table.addLine();
        		line.addCell(g.getDepartmentName());
        		CellInterface p = line.addCell().setTextAlignment(Alignment.RIGHT);
        		
        		int penalty = 0;
        		for (int i = 0; i < deptBalancingReport.getSlotsPerDayNoEvening() / 6; i++) {
        			int slot = deptBalancingReport.getFirstDaySlot() + i*6;
        			int usage = g.getUsage(slot);
        			int limit = g.getLimit(slot);
        			if (usage > limit)
        				penalty += g.getExcess(slot);
        			Vector classes = new Vector(g.getClasses(slot));
        			Collections.sort(classes);
        			CellInterface cell = line.addCell().setTextAlignment(Alignment.CENTER);
        			int u = 0; boolean over = false;
        			CellInterface ug = cell.add(null);
        			CellInterface prv = ug;
        			for (Enumeration e=classes.elements();e.hasMoreElements();) {
        				ClassAssignmentDetails ca = (ClassAssignmentDetails)e.nextElement();
        				CellInterface c = cell.add(ca.getClazz().getName()).setInline(false);
        				c.setUrl("suggestions?menu=hide&id=" + ca.getClazz().getClassId());
        				c.setDialog(MESSAGES.dialogSuggestions());
        				int nrMeetings = 0;
        				for (int j = deptBalancingReport.getFirstWorkDay(); j<=deptBalancingReport.getLastWorkDay(); j++)
        					if ((Constants.DAY_CODES[j%7]&ca.getTime().getDays())!=0) nrMeetings++;
        				u+=nrMeetings;
        				if (u>limit && !over) {
        					over=true;
        					prv.addStyle("text-decoration: underline;");
        				}
        				c.setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref()));
        				prv = c;
        			}
        			ug.setText(limit == 0 ? "" : usage + " / " + limit).setInline(false);
        			if (usage > limit) ug.setColor("#b80000");
        		}
        		p.setText(String.valueOf(penalty)).setComparable(penalty, g.getDepartmentName()).setColor(penalty > 0 ? "#b80000" : null);
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
		table.setDefaultSortCookie(MESSAGES.colDepartment());
		table.addProperty(SubpartDetailBackend.getLegend(false));
        return table;
	}
	
	public static TableInterface getViolatedDistrPreferencesReportTable(ViolatedDistrPreferencesReport report, boolean usePrefStyles) {
		TableInterface table = new TableInterface("dist-pref", MESSAGES.reportViolatedDistributionPreferences());
		LineInterface header = table.addHeader();
		header.addCell(MESSAGES.colDistrubutionType());
		header.addCell(MESSAGES.colPreference());
		header.addCell(MESSAGES.colViolations()).setTextAlignment(Alignment.RIGHT);
		header.addCell(MESSAGES.colClass());
		header.addCell(MESSAGES.colDate());
		header.addCell(MESSAGES.colTime());
		header.addCell(MESSAGES.colRoom());
        try {
        	for (Iterator i = report.getGroups().iterator(); i.hasNext(); ) {
        		ViolatedDistrPreferencesReport.ViolatedDistrPreference g = (ViolatedDistrPreferencesReport.ViolatedDistrPreference)i.next();
        		LineInterface line = table.addLine();
        		line.addCell(g.getName());
        		line.addCell(PreferenceLevel.getPreferenceLevel(PreferenceLevel.int2prolog(g.getPreference())).getPrefName())
        			.setColor(PreferenceLevel.int2color(g.getPreference()))
        			.setComparable(g.getPreference(), g.getName());
        		line.addCell(String.valueOf(g.getNrViolations())).setComparable(g.getNrViolations(), g.getName()).setTextAlignment(Alignment.RIGHT);
        		CellInterface classes = line.addCell();
        		CellInterface dates = line.addCell();
        		CellInterface times = line.addCell();
        		CellInterface rooms = line.addCell();
        		
        		for (Enumeration e=g.getClasses().elements();e.hasMoreElements();) {
        			ClassAssignmentDetails ca = (ClassAssignmentDetails)e.nextElement();
        			TimeInfo time = ca.getTime();
        			
        			CellInterface c = classes.add(ca.getClazz().getName()).setNoWrap(true).setInline(false)
        					.setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref()));
    				c.setUrl("suggestions?menu=hide&id=" + ca.getClazz().getClassId());
    				c.setDialog(MESSAGES.dialogSuggestions());
    				dates.addItem(time.toDateCell(usePrefStyles).setInline(false));
        			times.addItem(time.toCell().setInline(false));
        			rooms.addItem(toRoomCell(ca.getRoom(), usePrefStyles).setInline(false));
        		}
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
        table.setDefaultSortCookie(MESSAGES.colDistrubutionType());
        table.addProperty(SubpartDetailBackend.getLegend(false));
        return table;
	}
	
	public static TableInterface getDiscouragedInstructorBtbReportReportTable(DiscouragedInstructorBtbReport report, boolean usePrefStyles) {
		TableInterface table = new TableInterface("instructor-btb", MESSAGES.reportInstructorBackToBackPreferences());
		table.setHeader(
				(MESSAGES.colInstructor()),
				(MESSAGES.colPreference()),
				(MESSAGES.colDistance()),
				(MESSAGES.colClass()),
				(MESSAGES.colDate()),
				(MESSAGES.colTime()),
				(MESSAGES.colRoom())
				);

		try {
        	for (Iterator i = report.getGroups().iterator(); i.hasNext(); ) {
        		DiscouragedInstructorBtbReport.DiscouragedBtb g = (DiscouragedInstructorBtbReport.DiscouragedBtb)i.next();
        		LineInterface line = table.addLine();
        		line.addCell(g.getInstructorName());
        		line.addCell(PreferenceLevel.getPreferenceLevel(g.getPreference()).getPrefName())
        			.setColor(PreferenceLevel.prolog2color(g.getPreference()))
        			.setComparable(PreferenceLevel.prolog2int(g.getPreference()), g.getInstructorName());
        		line.addCell(g.getDistance() == 0.0 ? MESSAGES.notApplicable() : g.getDistance() < 0.0 ? MESSAGES.breakTime(sDF.format(- g.getDistance())) : MESSAGES.roomDistance(sDoubleFormat.format(g.getDistance())))
        			.setComparable(g.getDistance(), g.getInstructorName());
        		CellInterface classes = line.addCell();
        		CellInterface dates = line.addCell();
        		CellInterface times = line.addCell();
        		CellInterface rooms = line.addCell();
        		
    			CellInterface c = classes.add(g.getFirst().getClazz().getName()).setNoWrap(true).setInline(false)
    					.setColor(PreferenceLevel.prolog2color(g.getFirst().getClazz().getPref()));
				c.setUrl("suggestions?menu=hide&id=" + g.getFirst().getClazz().getClassId());
				c.setDialog(MESSAGES.dialogSuggestions());
				dates.addItem(g.getFirst().getTime().toDateCell(usePrefStyles).setInline(false));
    			times.addItem(g.getFirst().getTime().toCell().setInline(false));
    			rooms.addItem(toRoomCell(g.getFirst().getRoom(), usePrefStyles).setInline(false));
    			
    			c = classes.add(g.getSecond().getClazz().getName()).setNoWrap(true).setInline(false)
    					.setColor(PreferenceLevel.prolog2color(g.getSecond().getClazz().getPref()));
				c.setUrl("suggestions?menu=hide&id=" + g.getSecond().getClazz().getClassId());
				c.setDialog(MESSAGES.dialogSuggestions());
				dates.addItem(g.getSecond().getTime().toDateCell(usePrefStyles).setInline(false));
    			times.addItem(g.getSecond().getTime().toCell().setInline(false));
    			rooms.addItem(toRoomCell(g.getSecond().getRoom(), usePrefStyles).setInline(false));
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
        table.setDefaultSortCookie(MESSAGES.colInstructor());
        table.addProperty(SubpartDetailBackend.getLegend(false));
        return table;
	}

	public static TableInterface getStudentConflictsReportTable(StudentConflictsReport report, boolean usePrefStyles) {
		boolean hasHard = false;
		boolean hasDistance = false;
		boolean hasFixed = false;
		boolean hasCommitted = false;
		boolean hasImportant = false;
		boolean hasInstructor = false;
		boolean hasWorkday = false;
		for (JenrlInfo g: (Set<JenrlInfo>)report.getGroups()) {
			if (g.isHard()) hasHard = true;
			if (g.isDistance()) hasDistance = true;
			if (g.isFixed()) hasFixed = true;
			if (g.isCommited()) hasCommitted = true;
			if (g.isImportant()) hasImportant = true;
			if (g.isInstructor()) hasInstructor = true;
			if (g.isWorkDay()) hasWorkday = true;
		}
		TableInterface table = new TableInterface("student-conf", MESSAGES.reportStudentConflicts());
		LineInterface header = table.addHeader();
		header.addCell(MESSAGES.colNrConflicts()).setTextAlignment(Alignment.RIGHT);
		header.addCell(MESSAGES.colClass());
		header.addCell(MESSAGES.colDate());
		header.addCell(MESSAGES.colTime());
		header.addCell(MESSAGES.colRoom());
		if (hasHard) header.addCell(MESSAGES.colStudentConflictHard());
		if (hasDistance) header.addCell(MESSAGES.colStudentConflictDistance());
		if (hasFixed) header.addCell(MESSAGES.colStudentConflictFixed());
		if (hasCommitted) header.addCell(MESSAGES.colStudentConflictCommitted());
		if (hasImportant) header.addCell(MESSAGES.colStudentConflictImportant());
		if (hasInstructor) header.addCell(MESSAGES.colStudentConflictInstructor());
		if (hasWorkday) header.addCell(MESSAGES.colStudentConflictWorkday());
		header.addCell(MESSAGES.colCurriculum());
        try {
        	int total[] = new int [] { 0, 0, 0, 0, 0, 0, 0, 0};
        	for (Iterator i=report.getGroups().iterator();i.hasNext();) {
        		JenrlInfo g = (JenrlInfo)i.next();
        		long jenrl = Math.round(g.getJenrl());  
        		
        		if (jenrl <= 0) continue;
        		
        		LineInterface line = table.addLine();
        		line.addCell(String.valueOf(jenrl)).setComparable(jenrl).setTextAlignment(Alignment.RIGHT);
        		
        		CellInterface classes = line.addCell();
        		CellInterface dates = line.addCell();
        		CellInterface times = line.addCell();
        		CellInterface rooms = line.addCell();
        		
    			CellInterface c = classes.add(g.getFirst().getClazz().getName()).setNoWrap(true).setInline(false)
    					.setColor(PreferenceLevel.prolog2color(g.getFirst().getClazz().getPref()));
				c.setUrl("suggestions?menu=hide&id=" + g.getFirst().getClazz().getClassId());
				c.setDialog(MESSAGES.dialogSuggestions());
				dates.addItem(g.getFirst().getTime().toDateCell(usePrefStyles).setInline(false));
    			times.addItem(g.getFirst().getTime().toCell().setInline(false));
    			rooms.addItem(toRoomCell(g.getFirst().getRoom(), usePrefStyles).setInline(false));
    			
    			c = classes.add(g.getSecond().getClazz().getName()).setNoWrap(true).setInline(false)
    					.setColor(PreferenceLevel.prolog2color(g.getSecond().getClazz().getPref()));
				c.setUrl("suggestions?menu=hide&id=" + g.getSecond().getClazz().getClassId());
				c.setDialog(MESSAGES.dialogSuggestions());
				dates.addItem(g.getSecond().getTime().toDateCell(usePrefStyles).setInline(false));
    			times.addItem(g.getSecond().getTime().toCell().setInline(false));
    			rooms.addItem(toRoomCell(g.getSecond().getRoom(), usePrefStyles).setInline(false));
    			
    			if (hasHard) setBoolean(line.addCell().setComparable(g.isHard(), jenrl), g.isHard());
    			if (hasDistance) line.addCell(g.isDistance() ? MESSAGES.reportDistanceInMeter((int)Math.round(g.getDistance())) : "")
    				.setComparable(g.isDistance(), -g.getDistance(), jenrl).setTextAlignment(Alignment.CENTER);
    			if (hasFixed) setBoolean(line.addCell().setComparable(g.isFixed(), jenrl), g.isFixed());
    			if (hasCommitted) setBoolean(line.addCell().setComparable(g.isCommited(), jenrl), g.isCommited());
    			if (hasImportant) setBoolean(line.addCell().setComparable(g.isImportant(), jenrl), g.isImportant());
    			if (hasInstructor) setBoolean(line.addCell().setComparable(g.isInstructor(), jenrl), g.isInstructor());
    			if (hasWorkday) setBoolean(line.addCell().setComparable(g.isWorkDay(), jenrl), g.isWorkDay());
    			line.addCell(g.getCurriculumText()).setComparable(g.getCurriculumText(), jenrl).setNoWrap(true);

        		total[0] += Math.round(g.getJenrl());
        		if (g.isHard()) total[1] += Math.round(g.getJenrl());
        		if (g.isDistance()) total[2] += Math.round(g.getJenrl());
        		if (g.isFixed()) total[3] += Math.round(g.getJenrl());
        		if (g.isCommited()) total[4] += Math.round(g.getJenrl());
        		if (g.isImportant()) total[5] += Math.round(g.getJenrl());
        		if (g.isInstructor()) total[6] += (g.isInstructor() ? 1 : 0);
        		if (g.isWorkDay()) total[7] += Math.round(g.getJenrl());
        	}
        	
    		LineInterface line = table.addLine();
    		line.addCell(String.valueOf(total[0])).setComparable(total[0]).setTextAlignment(Alignment.RIGHT);
    		line.addCell(MESSAGES.reportTotal()).addStyle("font-style: italic;");
    		line.addCell("");
    		line.addCell("");
    		line.addCell("");
			if (hasHard) line.addCell(String.valueOf(total[1])).setTextAlignment(Alignment.CENTER);
			if (hasDistance) line.addCell(String.valueOf(total[2])).setTextAlignment(Alignment.CENTER);
			if (hasFixed) line.addCell(String.valueOf(total[3])).setTextAlignment(Alignment.CENTER);
			if (hasCommitted) line.addCell(String.valueOf(total[4])).setTextAlignment(Alignment.CENTER);
			if (hasImportant) line.addCell(String.valueOf(total[5])).setTextAlignment(Alignment.CENTER);
			if (hasInstructor) line.addCell(String.valueOf(total[6])).setTextAlignment(Alignment.CENTER);
			if (hasWorkday) line.addCell(String.valueOf(total[7])).setTextAlignment(Alignment.CENTER);
			line.addCell("");
    		line.setPropertyIndex(-1);
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
        table.setDefaultSortCookie("!" + MESSAGES.colNrConflicts());
        table.addProperty(SubpartDetailBackend.getLegend(false));
        return table;
	}
	
	protected static void setBoolean(CellInterface cell, boolean val) {
		if (val) {
			cell.setTextAlignment(Alignment.CENTER).addImage().setSource("images/accept.png").setAlt(MESSAGES.exportTrue()).setTitle(MESSAGES.exportTrue());
		} else {
			cell.setTextAlignment(Alignment.CENTER).addImage().setSource("images/cross.png").setAlt(MESSAGES.exportFalse()).setTitle(MESSAGES.exportFalse());
		}
	}
	
	public static TableInterface getSameSubpartBalancingReportTable(SameSubpartBalancingReport report) {
		TableInterface table = new TableInterface("section-balanc", MESSAGES.reportSectionBalancing());
		LineInterface header = table.addHeader();
		header.addCell(MESSAGES.colDepartment());
		header.addCell(MESSAGES.colPenalty()).setTextAlignment(Alignment.RIGHT);
		for (int i = 0; i < report.getSlotsPerDayNoEvening() / 6; i++)
			header.addCell(Constants.slot2str(report.getFirstDaySlot() + i * 6)).setTextAlignment(Alignment.CENTER);
		
        try {
        	for (Iterator it=report.getGroups().iterator();it.hasNext();) {
        		SameSubpartBalancingReport.SameSubpartBalancingGroup g = (SameSubpartBalancingReport.SameSubpartBalancingGroup)it.next();
        		
        		LineInterface line = table.addLine();
        		line.addCell(g.getName());
        		CellInterface p = line.addCell().setTextAlignment(Alignment.RIGHT);
        		
        		int penalty = 0;
        		for (int i = 0; i < report.getSlotsPerDayNoEvening() / 6; i++) {
        			int slot = report.getFirstDaySlot() + i*6;
        			int usage = g.getUsage(slot);
        			int limit = g.getLimit(slot);
        			if (usage > limit)
        				penalty += g.getExcess(slot);
        			Vector classes = new Vector(g.getClasses(slot));
        			Collections.sort(classes);
        			CellInterface cell = line.addCell().setTextAlignment(Alignment.CENTER);
        			int u = 0; boolean over = false;
        			CellInterface ug = cell.add(null);
        			CellInterface prv = ug;
        			for (Enumeration e=classes.elements();e.hasMoreElements();) {
        				ClassAssignmentDetails ca = (ClassAssignmentDetails)e.nextElement();
        				CellInterface c = cell.add(ca.getClazz().getName()).setInline(false);
        				c.setUrl("suggestions?menu=hide&id=" + ca.getClazz().getClassId());
        				c.setDialog(MESSAGES.dialogSuggestions());
        				int nrMeetings = 0;
        				for (int j = report.getFirstWorkDay(); j<=report.getLastWorkDay(); j++)
        					if ((Constants.DAY_CODES[j%7]&ca.getTime().getDays())!=0) nrMeetings++;
        				u+=nrMeetings;
        				if (u>limit && !over) {
        					over=true;
        					prv.addStyle("text-decoration: underline;");
        				}
        				c.setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref()));
        				prv = c;
        			}
        			ug.setText(limit == 0 ? "" : usage + " / " + limit).setInline(false);
        			if (usage > limit) ug.setColor("#b80000");
        		}
        		p.setText(String.valueOf(penalty)).setComparable(penalty, g.getName()).setColor(penalty > 0 ? "#b80000" : null);
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
		table.setDefaultSortCookie(MESSAGES.colDepartment());
		table.addProperty(SubpartDetailBackend.getLegend(false));
        return table;
	}
	
	public static CellInterface dispNumber(int value) {
		return new CellInterface().setText(String.valueOf(value)).setComparable(value)
				.setColor(value < 0 ? "#1d6600" : value > 0 ? "#b80000" : null)
				.setTextAlignment(Alignment.RIGHT);
	}
	
	public static CellInterface dispNumber(long value) {
		return new CellInterface().setText(String.valueOf(value)).setComparable(value)
				.setColor(value < 0 ? "#1d6600" : value > 0 ? "#b80000" : null)
				.setTextAlignment(Alignment.RIGHT);
	}
	
	public static CellInterface dispNumber(double value) {
		return new CellInterface().setText(Math.round(1000.0 * value) == 0.0 ? "" : (value >= 0.0005 ? "+" : "") + sDF.format(value)).setComparable(value)
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
	
	public static TableInterface getPerturbationReportTable(PerturbationReport report, boolean usePrefStyles) {
		TableInterface table = new TableInterface("perturbations", MESSAGES.reportPerturbations());
		table.setHeader(
				MESSAGES.colClass(),
				MESSAGES.colDate(),
				MESSAGES.colTime(),
				MESSAGES.colRoom(),
				MESSAGES.colShortDist(),
				MESSAGES.colPerturbationStudents(),
				MESSAGES.colPerturbationStudentsTime(),
				MESSAGES.colPerturbationStudentsRoom(),
				MESSAGES.colPerturbationStudentsBuilding(),
				MESSAGES.colPerturbationInstructor(),
				MESSAGES.colPerturbationInstructorTime(),
				MESSAGES.colPerturbationInstructorRoom(),
				MESSAGES.colPerturbationInstructorBuilding(),
				MESSAGES.colPerturbationRoom(),
				MESSAGES.colPerturbationBuilding(),
				MESSAGES.colPerturbationTime(),
				MESSAGES.colPerturbationDay(),
				MESSAGES.colPerturbationHour(),
				MESSAGES.colPerturbationTooFarStudent(),
				MESSAGES.colPerturbationTooFarInstructor(),
				MESSAGES.colPerturbationDeltaStudentConflicts(),
				MESSAGES.colPerturbationNewStudentConflicts(),
				MESSAGES.colPerturbationDeltaTimePref(),
				MESSAGES.colPerturbationDeltaRoomPref(),
				MESSAGES.colPerturbationDeltaInstructorBTB());
		for (int i = 5; i < table.getMaxColumns(); i++)
			table.getHeader().get(0).getCell(i).setTextAlignment(Alignment.RIGHT);
			
		table.addProperty(MESSAGES.colClass()).setText(MESSAGES.reportPertClass());
		table.addProperty(MESSAGES.colDate()).setText(MESSAGES.reportPertDate());
		table.addProperty(MESSAGES.colTime()).setText(MESSAGES.reportPertTime());
		table.addProperty(MESSAGES.colRoom()).setText(MESSAGES.reportPertRoom());
		table.addProperty(MESSAGES.colShortDist()).setText(MESSAGES.reportPertDistance());
		table.addProperty(MESSAGES.colPerturbationStudents()).setText(MESSAGES.reportPertStudents());
		table.addProperty(MESSAGES.colPerturbationStudentsTime()).setText(MESSAGES.reportPertStudentsTime());
		table.addProperty(MESSAGES.colPerturbationStudentsRoom()).setText(MESSAGES.reportPertStudentsRoom());
		table.addProperty(MESSAGES.colPerturbationStudentsBuilding()).setText(MESSAGES.reportPertStudentsBuilding());
		table.addProperty(MESSAGES.colPerturbationInstructor()).setText(MESSAGES.reportPertInstructor());
		table.addProperty(MESSAGES.colPerturbationInstructorTime()).setText(MESSAGES.reportPertInstructorTime());
		table.addProperty(MESSAGES.colPerturbationInstructorRoom()).setText(MESSAGES.reportPertInstructorRoom());
		table.addProperty(MESSAGES.colPerturbationInstructorBuilding()).setText(MESSAGES.reportPertInstructorBuilding());
		table.addProperty(MESSAGES.colPerturbationRoom()).setText(MESSAGES.reportPertRoomChange());
		table.addProperty(MESSAGES.colPerturbationBuilding()).setText(MESSAGES.reportPertBuildingChange());
		table.addProperty(MESSAGES.colPerturbationTime()).setText(MESSAGES.reportPertTimeChange());
		table.addProperty(MESSAGES.colPerturbationDay()).setText(MESSAGES.reportPertDayChange());
		table.addProperty(MESSAGES.colPerturbationHour()).setText(MESSAGES.reportPertHourChange());
		table.addProperty(MESSAGES.colPerturbationTooFarStudent()).setText(MESSAGES.reportPertTooFarStudents());
		table.addProperty(MESSAGES.colPerturbationTooFarInstructor()).setText(MESSAGES.reportPertTooFarInstructor());
		table.addProperty(MESSAGES.colPerturbationDeltaStudentConflicts()).setText(MESSAGES.reportPertDeltaStudentConf());
		table.addProperty(MESSAGES.colPerturbationNewStudentConflicts()).setText(MESSAGES.reportPertNewStudentConf());
		table.addProperty(MESSAGES.colPerturbationDeltaTimePref()).setText(MESSAGES.reportPertDeltaTimePref());
		table.addProperty(MESSAGES.colPerturbationDeltaRoomPref()).setText(MESSAGES.reportPertDeltaRoomPref());
		table.addProperty(MESSAGES.colPerturbationDeltaInstructorBTB()).setText(MESSAGES.reportPertDeltaInstructorBTBPref());		
        try {
        	for (Iterator i=report.getGroups().iterator();i.hasNext();) {
        		PerturbationReport.PerturbationGroup g = (PerturbationReport.PerturbationGroup)i.next();
        		ClassAssignmentDetails ca = g.getClazz();
        		
        		ClassAssignmentDetails.TimeInfo timeBefore = ca.getTime();
        		ClassAssignmentDetails.TimeInfo timeAfter = ca.getAssignedTime();
        		
        		LineInterface line = table.addLine();
        		line.setId(ca.getClazz().getClassId());
        		line.setURL("suggestions?menu=hide&id=" + ca.getClazz().getClassId());
        		line.setDialog(MESSAGES.dialogSuggestions());
        		
        		line.addCell(ca.getClazz().getName());
        		
        		CellInterface d = line.addCell(); d.setNoWrap(true);
        		if (timeBefore!=null && (timeAfter==null || !timeBefore.getDatePatternId().equals(timeAfter.getDatePatternId())))
        			d.addItem(timeBefore.toDateCell(usePrefStyles)).add(" \u2192 ");
        		if (timeBefore==null && timeAfter!=null)
        			d.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
        			.add(" \u2192 ");
        		if (timeAfter == null)
        			d.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
        		else
        			d.addItem(timeAfter.toDateCell(usePrefStyles));
        		
        		CellInterface t = line.addCell(); t.setNoWrap(true);
        		if (timeBefore!=null && (timeAfter==null || timeBefore.getDays() != timeAfter.getDays() || timeBefore.getStartSlot() != timeAfter.getStartSlot() || timeBefore.getMin() != timeAfter.getMin()))
        			t.addItem(timeBefore.toCell(usePrefStyles)).add(" \u2192 ");
        		if (timeBefore==null && timeAfter!=null)
        			t.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
        			.add(" \u2192 ");
        		if (timeAfter == null)
        			t.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
        		else
        			t.addItem(timeAfter.toCell(usePrefStyles));
        		
    	    	ClassAssignmentDetails.RoomInfo[] roomBefore = ca.getRoom();
    	    	ClassAssignmentDetails.RoomInfo[] roomAfter = ca.getAssignedRoom();

    	    	CellInterface r = line.addCell(); r.setNoWrap(true);
    	    	if (roomBefore!=null && (roomAfter==null || !toId(roomBefore).equals(toId(roomAfter))))
        			r.addItem(toRoomCell(roomBefore, usePrefStyles)).add(" \u2192 ");
        		if (roomBefore==null && roomAfter!=null)
        			r.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
        			.add(" \u2192 ");
        		if (roomAfter == null)
        			r.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
        		else
        			r.addItem(toRoomCell(roomAfter, usePrefStyles));
        		
        		line.addCell(Math.round(g.distance) > 0 ? MESSAGES.reportDistanceInMeter((int)Math.round(g.distance)) : "").setComparable(g.distance, ca.getClazz().getName()).setTextAlignment(Alignment.RIGHT);
        		line.addCell(dispNumber(g.affectedStudents));
        		line.addCell(dispNumber(g.affectedStudentsByTime));
        		line.addCell(dispNumber(g.affectedStudentsByRoom));
        		line.addCell(dispNumber(g.affectedStudentsByBldg));
        		line.addCell(dispNumber(g.affectedInstructors));
        		line.addCell(dispNumber(g.affectedInstructorsByTime));
        		line.addCell(dispNumber(g.affectedInstructorsByRoom));
        		line.addCell(dispNumber(g.affectedInstructorsByBldg));
        		line.addCell(dispNumber(g.differentRoom));
        		line.addCell(dispNumber(g.differentBuilding));
        		line.addCell(dispNumber(g.differentTime));
        		line.addCell(dispNumber(g.differentDay));
        		line.addCell(dispNumber(g.differentHour));
        		line.addCell(dispNumber(g.tooFarForStudents));
        		line.addCell(dispNumber(g.tooFarForInstructors));
        		line.addCell(dispNumber(g.deltaStudentConflicts));
        		line.addCell(dispNumber(g.newStudentConflicts));
        		line.addCell(dispNumber(Math.round(g.deltaTimePreferences)));
        		line.addCell(dispNumber(g.deltaRoomPreferences));
        		line.addCell(dispNumber(g.deltaInstructorDistancePreferences));
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
        table.setDefaultSortCookie(MESSAGES.colClass());
        return table;
	}

}
