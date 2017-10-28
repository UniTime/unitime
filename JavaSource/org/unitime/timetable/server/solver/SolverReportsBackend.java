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
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.TableInterface;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolverReportsRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolverReportsResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.TableInterface.Alignment;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellBoolean;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellChange;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellClickableClassName;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellInterface;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellMultiLine;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellRooms;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellText;
import org.unitime.timetable.gwt.shared.TableInterface.TableHeaderIterface;
import org.unitime.timetable.gwt.shared.TableInterface.TableRowInterface;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
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
        	for (int i = 0; i < response.getTables().size() - 1; i++)
        		for (TableHeaderIterface h: response.getTables().get(i).getHeader())
        			h.setDescription(null);
        }
        
		ViolatedDistrPreferencesReport violatedDistrPreferencesReport = solver.getViolatedDistrPreferencesReport();
		if (violatedDistrPreferencesReport != null && !violatedDistrPreferencesReport.getGroups().isEmpty())
			response.addTable(getViolatedDistrPreferencesReportTable(violatedDistrPreferencesReport));
			
		DiscouragedInstructorBtbReport discouragedInstructorBtbReportReport = solver.getDiscouragedInstructorBtbReport();
		if (discouragedInstructorBtbReportReport!=null && !discouragedInstructorBtbReportReport.getGroups().isEmpty())
			response.addTable(getDiscouragedInstructorBtbReportReportTable(discouragedInstructorBtbReportReport));
        
		StudentConflictsReport studentConflictsReport = solver.getStudentConflictsReport();
		if (studentConflictsReport!=null && !studentConflictsReport.getGroups().isEmpty())
			response.addTable(getStudentConflictsReportTable(studentConflictsReport));
        
		DeptBalancingReport deptBalancingReport = solver.getDeptBalancingReport();
		if (deptBalancingReport != null && !deptBalancingReport.getGroups().isEmpty())
			response.addTable(getDeptBalancingReportTable(deptBalancingReport));
		
		SameSubpartBalancingReport sameSubpartBalancingReport = solver.getSameSubpartBalancingReport();
		if (sameSubpartBalancingReport!=null && !sameSubpartBalancingReport.getGroups().isEmpty())
			response.addTable(getSameSubpartBalancingReportTable(sameSubpartBalancingReport));

		PerturbationReport perturbationReport = solver.getPerturbationReport();
		if (perturbationReport!=null && !perturbationReport.getGroups().isEmpty())
			response.addTable(getPerturbationReportTable(perturbationReport));
		
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(true))
			response.addPreference(new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2bgColor(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), pref.getAbbreviation(), false));
		
		return response;
	}
	
	public static TableInterface getRoomReportTable(RoomReport report, RoomType type) {
		TableInterface table = new TableInterface("report-rooms" + (type == null ? "" : "-" + type.getReference().toLowerCase()), type == null ? MESSAGES.reportRoomAllocationNonUnivLocs() : MESSAGES.reportRoomAllocation(type.getLabel()));
		
		table.setHeader(
				new TableHeaderIterface(MESSAGES.colRoomReportGroup()).setDescription(MESSAGES.reportRoomAlocDescGroup()),
				new TableHeaderIterface(MESSAGES.colRoomReportActualSizes()).setDescription(MESSAGES.reportRoomAlocDescSize()),
				new TableHeaderIterface(MESSAGES.colRoomReportNbrRooms()).setDescription(MESSAGES.reportRoomAlocDescNbrRooms()),
				new TableHeaderIterface(MESSAGES.colRoomReportClassUse()).setDescription(MESSAGES.reportRoomAlocDescClassUse()),
				new TableHeaderIterface(MESSAGES.colRoomReportClassShould()).setDescription(MESSAGES.reportRoomAlocDescClassShould()),
				new TableHeaderIterface(MESSAGES.colRoomReportClassMust()).setDescription(MESSAGES.reportRoomAlocDescClassMust()),
				new TableHeaderIterface(MESSAGES.colRoomReportHourUse()).setDescription(MESSAGES.reportRoomAlocDescHourUse()),
				new TableHeaderIterface(MESSAGES.colRoomReportHourShould()).setDescription(MESSAGES.reportRoomAlocDescHourShould()),
				new TableHeaderIterface(MESSAGES.colRoomReportHourMust()).setDescription(MESSAGES.reportRoomAlocDescHourMust())
				);
		
		int nrLines = 0;
        
        try {
            int nrAllRooms = 0, nrAllLectureUse = 0, nrAllLectureShouldUse = 0;
            double allSlotsUse = 0.0, allSlotsShouldUse = 0.0;
            
            TreeSet groups = new TreeSet(new Comparator() {
                public int compare(Object o1, Object o2) {
                    RoomReport.RoomAllocationGroup g1 = (RoomReport.RoomAllocationGroup)o1;
                    RoomReport.RoomAllocationGroup g2 = (RoomReport.RoomAllocationGroup)o2;
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

                table.addRow(new TableRowInterface(
                		new TableCellInterface<Integer>(g.getMinRoomSize(), MESSAGES.reportRoomRange(String.valueOf(g.getMinRoomSize()), (g.getMaxRoomSize() == Integer.MAX_VALUE ? "\u221E" : String.valueOf(g.getMaxRoomSize())))),
                		new TableCellInterface<Integer>(g.getActualMinRoomSize(), MESSAGES.reportRoomRange(String.valueOf(g.getActualMinRoomSize()), String.valueOf(g.getActualMaxRoomSize()))),
                		new TableCellInterface<Integer>(g.getNrRooms(), g.getNrRooms()+" (" + g.getNrRoomsThisSizeOrBigger() + ")"),
                		new TableCellInterface<Integer>(g.getLecturesUse(), g.getLecturesUse() + " (" + nrAllLectureUse + ")"),
                		new TableCellInterface<Integer>(g.getLecturesShouldUse(), g.getLecturesShouldUse() + " (" + nrAllLectureShouldUse + ")"),
                		new TableCellInterface<Integer>(g.getLecturesMustUse(), g.getLecturesMustUse() + " (" + g.getLecturesMustUseThisSizeOrBigger() + ")"),
                		new TableCellInterface<Double>(factor*g.getSlotsUse()/g.getNrRooms(), sDoubleFormat.format(factor*g.getSlotsUse()/g.getNrRooms())+" ("+sDoubleFormat.format(factor*allSlotsUse/nrAllRooms)+")"),
                		new TableCellInterface<Double>(factor*g.getSlotsShouldUse()/g.getNrRooms(), sDoubleFormat.format(factor*g.getSlotsShouldUse()/g.getNrRooms())+" ("+sDoubleFormat.format(factor*allSlotsShouldUse/nrAllRooms)+")"),
                		new TableCellInterface<Double>(factor*g.getSlotsMustUse()/g.getNrRooms(), sDoubleFormat.format(factor*g.getSlotsMustUse()/g.getNrRooms())+" ("+sDoubleFormat.format(factor*g.getSlotsMustUseThisSizeOrBigger()/g.getNrRoomsThisSizeOrBigger())+")")
                		));
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
        if (nrLines == 0 && !table.hasErrorMessage()) return null;
		Collections.sort(table.getRows(), new Comparator<TableInterface.TableRowInterface>() {
			@Override
			public int compare(TableRowInterface r1, TableRowInterface r2) {
				return r1.compareTo(r2, 0, false);
			}
		});
 
        return table;
	}
	
	public static TableInterface getDeptBalancingReportTable(DeptBalancingReport deptBalancingReport) {
		TableInterface table = new TableInterface("dept-balancing", MESSAGES.reportDepartmentalBalancing());
		TableHeaderIterface[] header = new TableHeaderIterface[2 + deptBalancingReport.getSlotsPerDayNoEvening() / 6];
		header[0] = new TableHeaderIterface(MESSAGES.colDepartment());
		header[1] = new TableHeaderIterface(MESSAGES.colPenalty());
		for (int i = 0; i < deptBalancingReport.getSlotsPerDayNoEvening() / 6; i++)
			header[2 + i] = new TableHeaderIterface(Constants.slot2str(deptBalancingReport.getFirstDaySlot() + i * 6)).setAlignment(Alignment.CENTER);
		table.setHeader(header);
		
		try {
        	for (Iterator it=deptBalancingReport.getGroups().iterator();it.hasNext();) {
        		DeptBalancingReport.DeptBalancingGroup g = (DeptBalancingReport.DeptBalancingGroup)it.next();
        		
        		TableCellInterface[] line = new TableCellInterface[2 + deptBalancingReport.getSlotsPerDayNoEvening() / 6];
        		line[0] = new TableCellInterface<String>(g.getDepartmentName());
        		
        		int penalty = 0;
        		for (int i = 0; i < deptBalancingReport.getSlotsPerDayNoEvening() / 6; i++) {
        			int slot = deptBalancingReport.getFirstDaySlot() + i*6;
        			int usage = g.getUsage(slot);
        			int limit = g.getLimit(slot);
        			if (usage > limit)
        				penalty += g.getExcess(slot);
        			Vector classes = new Vector(g.getClasses(slot));
        			Collections.sort(classes);
        			TableCellMultiLine cell = new TableCellMultiLine();
        			int u = 0; boolean over = false;
        			for (Enumeration e=classes.elements();e.hasMoreElements();) {
        				ClassAssignmentDetails ca = (ClassAssignmentDetails)e.nextElement();
        				TableCellClickableClassName x = new TableCellClickableClassName(ca.getClazz().getClassId(), ca.getClazz().getName());
        				int nrMeetings = 0;
        				for (int j = deptBalancingReport.getFirstWorkDay(); j<=deptBalancingReport.getLastWorkDay(); j++)
        					if ((Constants.DAY_CODES[j%7]&ca.getTime().getDays())!=0) nrMeetings++;
        				u+=nrMeetings;
        				if (u>limit && !over) {
        					over=true;
        					if (cell.last() != null)
        						cell.last().setUnderlined(true);
        				}
        				x.setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref()));
        				cell.add(x);
        			}
        			TableCellInterface h = new TableCellInterface(limit == 0 ? "" : usage + " / " + limit);
        			if (usage > limit) h.setColor("red");
        			cell.getChunks().add(0, h);
        			line[i + 2] = cell;
        		}
        		line[1] = new TableCellInterface<Integer>(penalty); if (penalty > 0) line[1].setColor("red");
        		table.addRow(new TableRowInterface(line));
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
		Collections.sort(table.getRows(), new Comparator<TableInterface.TableRowInterface>() {
			@Override
			public int compare(TableRowInterface r1, TableRowInterface r2) {
				return r1.compareTo(r2, 0, true);
			}
		});
		table.setShowPrefLegend(true);
        return table;
	}
	
	public static TableInterface getViolatedDistrPreferencesReportTable(ViolatedDistrPreferencesReport report) {
		TableInterface table = new TableInterface("dist-pref", MESSAGES.reportViolatedDistributionPreferences());
		table.setHeader(
				new TableHeaderIterface(MESSAGES.colDistrubutionType()),
				new TableHeaderIterface(MESSAGES.colPreference()),
				new TableHeaderIterface(MESSAGES.colViolations()).setAlignment(Alignment.RIGHT),
				new TableHeaderIterface(MESSAGES.colClass()),
				new TableHeaderIterface(MESSAGES.colDate()),
				new TableHeaderIterface(MESSAGES.colTime()),
				new TableHeaderIterface(MESSAGES.colRoom())
				);
        try {
        	for (Iterator i = report.getGroups().iterator(); i.hasNext(); ) {
        		ViolatedDistrPreferencesReport.ViolatedDistrPreference g = (ViolatedDistrPreferencesReport.ViolatedDistrPreference)i.next();
        		
        		TableCellMultiLine classes = new TableCellMultiLine();
        		TableCellMultiLine dates = new TableCellMultiLine();
        		TableCellMultiLine times = new TableCellMultiLine();
        		TableCellMultiLine rooms = new TableCellMultiLine();
        		
        		for (Enumeration e=g.getClasses().elements();e.hasMoreElements();) {
        			ClassAssignmentDetails ca = (ClassAssignmentDetails)e.nextElement();
        			TimeInfo time = ca.getTime();
        			
        			TableInterface.TableCellRooms room = new TableInterface.TableCellRooms();
        			if (ca.getRoom() != null) {
            	    	for (int j = 0; j<ca.getRoom().length; j++) {
            	    		room.add(
            	    				ca.getRoom()[j].getName(),
            	    				ca.getRoom()[j].getColor(),
            	    				ca.getRoom()[j].getId(),
            	    				PreferenceLevel.int2string(ca.getRoom()[j].getPref()));
            	    	}
            	    }
        			
        			classes.add(new TableInterface.TableCellClickableClassName(ca.getClazz().getClassId(), ca.getClazz().getName()).setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref())));
        			dates.add(new TableCellInterface(time.getDatePatternName()).setColor(PreferenceLevel.int2color(time.getDatePatternPreference())));
        			times.add(new TableInterface.TableCellTime(time.getDaysName() + " " + time.getStartTime() + " - " + time.getEndTime())
        					.setId(ca.getClazz().getClassId() + "," + time.getDays() + "," + time.getStartSlot()).setColor(PreferenceLevel.int2color(time.getPref())));
        			rooms.add(room);
        		}
        		
        		table.addRow(new TableRowInterface(
        				new TableCellInterface<String>(g.getName()),
        				new TableCellInterface<String>(PreferenceLevel.getPreferenceLevel(PreferenceLevel.int2prolog(g.getPreference())).getPrefName()).setColor(PreferenceLevel.int2color(g.getPreference())),
        				new TableCellInterface<Integer>(g.getNrViolations()),
        				classes,
        				dates,
        				times,
        				rooms));
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
		Collections.sort(table.getRows(), new Comparator<TableInterface.TableRowInterface>() {
			@Override
			public int compare(TableRowInterface r1, TableRowInterface r2) {
				return r1.compareTo(r2, 0, true);
			}
		});
		table.setShowPrefLegend(true);
        return table;
	}
	
	public static TableInterface getDiscouragedInstructorBtbReportReportTable(DiscouragedInstructorBtbReport report) {
		TableInterface table = new TableInterface("instructor-btb", MESSAGES.reportInstructorBackToBackPreferences());
		table.setHeader(
				new TableHeaderIterface(MESSAGES.colInstructor()),
				new TableHeaderIterface(MESSAGES.colPreference()),
				new TableHeaderIterface(MESSAGES.colDistance()),
				new TableHeaderIterface(MESSAGES.colClass()),
				new TableHeaderIterface(MESSAGES.colDate()),
				new TableHeaderIterface(MESSAGES.colTime()),
				new TableHeaderIterface(MESSAGES.colRoom())
				);

		try {
        	for (Iterator i = report.getGroups().iterator(); i.hasNext(); ) {
        		DiscouragedInstructorBtbReport.DiscouragedBtb g = (DiscouragedInstructorBtbReport.DiscouragedBtb)i.next();
        		
        		TableCellMultiLine classes = new TableCellMultiLine();
        		TableCellMultiLine dates = new TableCellMultiLine();
        		TableCellMultiLine times = new TableCellMultiLine();
        		TableCellMultiLine rooms = new TableCellMultiLine();
        		
    			classes.add(new TableInterface.TableCellClickableClassName(g.getFirst().getClazz().getClassId(), g.getFirst().getClazz().getName()).setColor(PreferenceLevel.prolog2color(g.getFirst().getClazz().getPref())));
    			dates.add(new TableCellInterface(g.getFirst().getTime().getDatePatternName()).setColor(PreferenceLevel.int2color(g.getFirst().getTime().getDatePatternPreference())));
    			times.add(new TableInterface.TableCellTime(g.getFirst().getTime().getDaysName() + " " + g.getFirst().getTime().getStartTime() + " - " + g.getFirst().getTime().getEndTime())
    					.setId(g.getFirst().getClazz().getClassId() + "," + g.getFirst().getTime().getDays() + "," + g.getFirst().getTime().getStartSlot()).setColor(PreferenceLevel.int2color(g.getFirst().getTime().getPref())));
    			TableInterface.TableCellRooms froom = new TableInterface.TableCellRooms();
    			if (g.getFirst().getRoom() != null) {
        	    	for (int j = 0; j<g.getFirst().getRoom().length; j++) {
        	    		froom.add(
        	    				g.getFirst().getRoom()[j].getName(),
        	    				g.getFirst().getRoom()[j].getColor(),
        	    				g.getFirst().getRoom()[j].getId(),
        	    				PreferenceLevel.int2string(g.getFirst().getRoom()[j].getPref()));
        	    	}
        	    }
    			rooms.add(froom);
    			
    			classes.add(new TableInterface.TableCellClickableClassName(g.getSecond().getClazz().getClassId(), g.getSecond().getClazz().getName()).setColor(PreferenceLevel.prolog2color(g.getSecond().getClazz().getPref())));
    			dates.add(new TableCellInterface(g.getSecond().getTime().getDatePatternName()).setColor(PreferenceLevel.int2color(g.getSecond().getTime().getDatePatternPreference())));
    			times.add(new TableInterface.TableCellTime(g.getSecond().getTime().getDaysName() + " " + g.getSecond().getTime().getStartTime() + " - " + g.getSecond().getTime().getEndTime())
    					.setId(g.getSecond().getClazz().getClassId() + "," + g.getSecond().getTime().getDays() + "," + g.getSecond().getTime().getStartSlot()).setColor(PreferenceLevel.int2color(g.getSecond().getTime().getPref())));
    			TableInterface.TableCellRooms sroom = new TableInterface.TableCellRooms();
    			if (g.getSecond().getRoom() != null) {
        	    	for (int j = 0; j<g.getSecond().getRoom().length; j++) {
        	    		sroom.add(
        	    				g.getSecond().getRoom()[j].getName(),
        	    				g.getSecond().getRoom()[j].getColor(),
        	    				g.getSecond().getRoom()[j].getId(),
        	    				PreferenceLevel.int2string(g.getSecond().getRoom()[j].getPref()));
        	    	}
        	    }
    			rooms.add(sroom);
    			
    			table.addRow(new TableRowInterface(
    					new TableCellInterface<String>(g.getInstructorName()),
    					new TableCellInterface<String>(PreferenceLevel.getPreferenceLevel(g.getPreference()).getPrefName()).setColor(PreferenceLevel.prolog2color(g.getPreference())),
        				new TableCellInterface<Double>(g.getDistance(), sDoubleFormat.format(g.getDistance())),
        				classes,
        				dates,
        				times,
        				rooms));
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
		Collections.sort(table.getRows(), new Comparator<TableInterface.TableRowInterface>() {
			@Override
			public int compare(TableRowInterface r1, TableRowInterface r2) {
				return r1.compareTo(r2, 0, true);
			}
		});
		table.setShowPrefLegend(true);
        return table;
	}

	public static TableInterface getStudentConflictsReportTable(StudentConflictsReport report) {
		boolean hasImportant = false;
		boolean hasInstructor = false;
		for (JenrlInfo g: (Set<JenrlInfo>)report.getGroups()) {
			if (g.isImportant()) hasImportant = true;
			if (g.isInstructor()) hasInstructor = true;
		}
		TableInterface table = new TableInterface("student-conf", MESSAGES.reportStudentConflicts());
		table.setHeader(
				new TableHeaderIterface(MESSAGES.colNrConflicts()),
				new TableHeaderIterface(MESSAGES.colClass()),
				new TableHeaderIterface(MESSAGES.colDate()),
				new TableHeaderIterface(MESSAGES.colTime()),
				new TableHeaderIterface(MESSAGES.colRoom()),
				new TableHeaderIterface(MESSAGES.colStudentConflictHard()),
				new TableHeaderIterface(MESSAGES.colStudentConflictDistance()),
				new TableHeaderIterface(MESSAGES.colStudentConflictFixed()),
				new TableHeaderIterface(MESSAGES.colStudentConflictCommitted()),
				new TableHeaderIterface(MESSAGES.colStudentConflictImportant()).setVisible(hasImportant),
				new TableHeaderIterface(MESSAGES.colStudentConflictInstructor()).setVisible(hasInstructor),
				new TableHeaderIterface(MESSAGES.colCurriculum())
				);
        try {
        	int total[] = new int [] { 0, 0, 0, 0, 0, 0, 0};
        	for (Iterator i=report.getGroups().iterator();i.hasNext();) {
        		JenrlInfo g = (JenrlInfo)i.next();
        		
        		if (Math.round(g.getJenrl()) <= 0) continue;
        		
        		TableCellMultiLine classes = new TableCellMultiLine();
        		TableCellMultiLine dates = new TableCellMultiLine();
        		TableCellMultiLine times = new TableCellMultiLine();
        		TableCellMultiLine rooms = new TableCellMultiLine();
        		
    			classes.add(new TableInterface.TableCellClickableClassName(g.getFirst().getClazz().getClassId(), g.getFirst().getClazz().getName()).setColor(PreferenceLevel.prolog2color(g.getFirst().getClazz().getPref())));
    			dates.add(new TableCellInterface(g.getFirst().getTime().getDatePatternName()).setColor(PreferenceLevel.int2color(g.getFirst().getTime().getDatePatternPreference())));
    			times.add(new TableInterface.TableCellTime(g.getFirst().getTime().getDaysName() + " " + g.getFirst().getTime().getStartTime() + " - " + g.getFirst().getTime().getEndTime())
    					.setId(g.getFirst().getClazz().getClassId() + "," + g.getFirst().getTime().getDays() + "," + g.getFirst().getTime().getStartSlot()).setColor(PreferenceLevel.int2color(g.getFirst().getTime().getPref())));
    			TableInterface.TableCellRooms froom = new TableInterface.TableCellRooms();
    			if (g.getFirst().getRoom() != null) {
        	    	for (int j = 0; j<g.getFirst().getRoom().length; j++) {
        	    		froom.add(
        	    				g.getFirst().getRoom()[j].getName(),
        	    				g.getFirst().getRoom()[j].getColor(),
        	    				g.getFirst().getRoom()[j].getId(),
        	    				PreferenceLevel.int2string(g.getFirst().getRoom()[j].getPref()));
        	    	}
        	    }
    			rooms.add(froom);
    			
    			classes.add(new TableInterface.TableCellClickableClassName(g.getSecond().getClazz().getClassId(), g.getSecond().getClazz().getName()).setColor(PreferenceLevel.prolog2color(g.getSecond().getClazz().getPref())));
    			dates.add(new TableCellInterface(g.getSecond().getTime().getDatePatternName()).setColor(PreferenceLevel.int2color(g.getSecond().getTime().getDatePatternPreference())));
    			times.add(new TableInterface.TableCellTime(g.getSecond().getTime().getDaysName() + " " + g.getSecond().getTime().getStartTime() + " - " + g.getSecond().getTime().getEndTime())
    					.setId(g.getSecond().getClazz().getClassId() + "," + g.getSecond().getTime().getDays() + "," + g.getSecond().getTime().getStartSlot()).setColor(PreferenceLevel.int2color(g.getSecond().getTime().getPref())));
    			TableInterface.TableCellRooms sroom = new TableInterface.TableCellRooms();
    			if (g.getSecond().getRoom() != null) {
        	    	for (int j = 0; j<g.getSecond().getRoom().length; j++) {
        	    		sroom.add(
        	    				g.getSecond().getRoom()[j].getName(),
        	    				g.getSecond().getRoom()[j].getColor(),
        	    				g.getSecond().getRoom()[j].getId(),
        	    				PreferenceLevel.int2string(g.getSecond().getRoom()[j].getPref()));
        	    	}
        	    }
    			rooms.add(sroom);
    			
    			table.addRow(new TableRowInterface(
    					new TableCellInterface<Long>(Math.round(g.getJenrl())),
    					classes,
    					dates,
    					times,
    					rooms,
    					new TableCellBoolean(g.isHard() ? Boolean.TRUE : null).setFormattedValue(g.isHard() ? MESSAGES.exportTrue() : MESSAGES.exportFalse()),
    					new TableCellInterface<Double>(g.isDistance() ? new Double(g.getDistance()) : null).setFormattedValue(g.isDistance() ? MESSAGES.reportDistanceInMeter((int)Math.round(g.getDistance())) : ""),
    					new TableCellBoolean(g.isFixed() ? Boolean.TRUE : null).setFormattedValue(g.isFixed() ? MESSAGES.exportTrue() : MESSAGES.exportFalse()),
    					new TableCellBoolean(g.isCommited() ? Boolean.TRUE : null).setFormattedValue(g.isCommited() ? MESSAGES.exportTrue() : MESSAGES.exportFalse()),
    					new TableCellBoolean(g.isImportant() ? Boolean.TRUE : null).setFormattedValue(g.isImportant() ? MESSAGES.exportTrue() : MESSAGES.exportFalse()),
    					new TableCellBoolean(g.isInstructor() ? Boolean.TRUE : null).setFormattedValue(g.isInstructor() ? MESSAGES.exportTrue() : MESSAGES.exportFalse()),
    					new TableCellText(g.getCurriculumText())
    					));

        		total[0] += Math.round(g.getJenrl());
        		if (g.isHard()) total[1] += Math.round(g.getJenrl());
        		if (g.isDistance()) total[2] += Math.round(g.getJenrl());
        		if (g.isFixed()) total[3] += Math.round(g.getJenrl());
        		if (g.isCommited()) total[4] += Math.round(g.getJenrl());
        		if (g.isImportant()) total[5] += Math.round(g.getJenrl());
        		if (g.isInstructor()) total[6] += (g.isInstructor() ? 1 : 0);
        	}
        	
        	table.addRow(new TableRowInterface(
					new TableCellInterface<Long>((long)total[0]),
					new TableCellText(MESSAGES.reportTotal()).setStyleName("italic"),
					new TableCellText(""),
					new TableCellText(""),
					new TableCellText(""),
					new TableCellBoolean(null).setFormattedValue(String.valueOf(total[1])),
					new TableCellInterface<Integer>(1000*total[2]).setFormattedValue(String.valueOf(total[2])),
					new TableCellBoolean(null).setFormattedValue(String.valueOf(total[3])),
					new TableCellBoolean(null).setFormattedValue(String.valueOf(total[4])),
					new TableCellBoolean(null).setFormattedValue(String.valueOf(total[5])),
					new TableCellBoolean(null).setFormattedValue(String.valueOf(total[6])),
					new TableCellText("")
					));
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
		Collections.sort(table.getRows(), new Comparator<TableInterface.TableRowInterface>() {
			@Override
			public int compare(TableRowInterface r1, TableRowInterface r2) {
				return r1.compareTo(r2, 0, false);
			}
		});
		table.setShowPrefLegend(true);
        return table;
	}
	
	public static TableInterface getSameSubpartBalancingReportTable(SameSubpartBalancingReport report) {
		TableInterface table = new TableInterface("section-balanc", MESSAGES.reportSectionBalancing());
		TableHeaderIterface[] header = new TableHeaderIterface[2 + report.getSlotsPerDayNoEvening() / 6];
		header[0] = new TableHeaderIterface(MESSAGES.colDepartment());
		header[1] = new TableHeaderIterface(MESSAGES.colPenalty());
		for (int i = 0; i < report.getSlotsPerDayNoEvening() / 6; i++)
			header[2 + i] = new TableHeaderIterface(Constants.slot2str(report.getFirstDaySlot() + i * 6)).setAlignment(Alignment.CENTER);
		table.setHeader(header);
		
        try {
        	for (Iterator it=report.getGroups().iterator();it.hasNext();) {
        		SameSubpartBalancingReport.SameSubpartBalancingGroup g = (SameSubpartBalancingReport.SameSubpartBalancingGroup)it.next();
        		
        		TableCellInterface[] line = new TableCellInterface[2 + report.getSlotsPerDayNoEvening() / 6];
        		line[0] = new TableCellInterface<String>(g.getName());
        		
        		int penalty = 0;
        		for (int i = 0; i < report.getSlotsPerDayNoEvening() / 6; i++) {
        			int slot = report.getFirstDaySlot() + i*6;
        			int usage = g.getUsage(slot);
        			int limit = g.getLimit(slot);
        			if (usage > limit)
        				penalty += g.getExcess(slot);
        			Vector classes = new Vector(g.getClasses(slot));
        			Collections.sort(classes);
        			TableCellMultiLine cell = new TableCellMultiLine();
        			int u = 0; boolean over = false;
        			for (Enumeration e=classes.elements();e.hasMoreElements();) {
        				ClassAssignmentDetails ca = (ClassAssignmentDetails)e.nextElement();
        				TableCellClickableClassName x = new TableCellClickableClassName(ca.getClazz().getClassId(), ca.getClazz().getName());
        				int nrMeetings = 0;
        				for (int j = report.getFirstWorkDay(); j<=report.getLastWorkDay(); j++)
        					if ((Constants.DAY_CODES[j%7]&ca.getTime().getDays())!=0) nrMeetings++;
        				u+=nrMeetings;
        				if (u>limit && !over) {
        					over=true;
        					if (cell.last() != null)
        						cell.last().setUnderlined(true);
        				}
        				x.setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref()));
        				cell.add(x);
        			}
        			TableCellInterface h = new TableCellInterface(limit == 0 ? "" : usage + " / " + limit);
        			if (usage > limit) h.setColor("red");
        			cell.getChunks().add(0, h);
        			line[i + 2] = cell;
        		}
        		line[1] = new TableCellInterface<Integer>(penalty); if (penalty > 0) line[1].setColor("red");
        		table.addRow(new TableRowInterface(line));
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
		Collections.sort(table.getRows(), new Comparator<TableInterface.TableRowInterface>() {
			@Override
			public int compare(TableRowInterface r1, TableRowInterface r2) {
				return r1.compareTo(r2, 0, true);
			}
		});
		table.setShowPrefLegend(true);
        return table;
	}
	
	public static TableCellInterface dispNumber(int value) {
		return new TableCellInterface<Integer>(value, value == 0 ? "" : value <= 0 ? String.valueOf(value) : "+" + String.valueOf(value)).setColor(value < 0 ? "green" : value > 0 ? "red" : null);
	}
	
	public static TableCellInterface dispNumber(double value) {
		return new TableCellInterface<Double>(value, Math.round(1000.0 * value) == 0.0 ? "" : (value >= 0.0005 ? "+" : "") + sDF.format(value)).setColor(value < 0 ? "green" : value > 0 ? "red" : null);
	}
	
	public static TableInterface getPerturbationReportTable(PerturbationReport report) {
		TableInterface table = new TableInterface("perturbations", MESSAGES.reportPerturbations());
		table.setHeader(
				new TableHeaderIterface(MESSAGES.colClass()).setDescription(MESSAGES.reportPertClass()),
				new TableHeaderIterface(MESSAGES.colDate()).setDescription(MESSAGES.reportPertDate()),
				new TableHeaderIterface(MESSAGES.colTime()).setDescription(MESSAGES.reportPertTime()),
				new TableHeaderIterface(MESSAGES.colRoom()).setDescription(MESSAGES.reportPertRoom()),
				new TableHeaderIterface(MESSAGES.colShortDist()).setDescription(MESSAGES.reportPertDistance()),
				new TableHeaderIterface(MESSAGES.colPerturbationStudents()).setDescription(MESSAGES.reportPertStudents()),
				new TableHeaderIterface(MESSAGES.colPerturbationStudentsTime()).setDescription(MESSAGES.reportPertStudentsTime()),
				new TableHeaderIterface(MESSAGES.colPerturbationStudentsRoom()).setDescription(MESSAGES.reportPertStudentsRoom()),
				new TableHeaderIterface(MESSAGES.colPerturbationStudentsBuilding()).setDescription(MESSAGES.reportPertStudentsBuilding()),
				new TableHeaderIterface(MESSAGES.colPerturbationInstructor()).setDescription(MESSAGES.reportPertInstructor()),
				new TableHeaderIterface(MESSAGES.colPerturbationInstructorTime()).setDescription(MESSAGES.reportPertInstructorTime()),
				new TableHeaderIterface(MESSAGES.colPerturbationInstructorRoom()).setDescription(MESSAGES.reportPertInstructorRoom()),
				new TableHeaderIterface(MESSAGES.colPerturbationInstructorBuilding()).setDescription(MESSAGES.reportPertInstructorBuilding()),
				new TableHeaderIterface(MESSAGES.colPerturbationRoom()).setDescription(MESSAGES.reportPertRoomChange()),
				new TableHeaderIterface(MESSAGES.colPerturbationBuilding()).setDescription(MESSAGES.reportPertBuildingChange()),
				new TableHeaderIterface(MESSAGES.colPerturbationTime()).setDescription(MESSAGES.reportPertTimeChange()),
				new TableHeaderIterface(MESSAGES.colPerturbationDay()).setDescription(MESSAGES.reportPertDayChange()),
				new TableHeaderIterface(MESSAGES.colPerturbationHour()).setDescription(MESSAGES.reportPertHourChange()),
				new TableHeaderIterface(MESSAGES.colPerturbationTooFarStudent()).setDescription(MESSAGES.reportPertTooFarStudents()),
				new TableHeaderIterface(MESSAGES.colPerturbationTooFarInstructor()).setDescription(MESSAGES.reportPertTooFarInstructor()),
				new TableHeaderIterface(MESSAGES.colPerturbationDeltaStudentConflicts()).setDescription(MESSAGES.reportPertDeltaStudentConf()),
				new TableHeaderIterface(MESSAGES.colPerturbationNewStudentConflicts()).setDescription(MESSAGES.reportPertNewStudentConf()),
				new TableHeaderIterface(MESSAGES.colPerturbationDeltaTimePref()).setDescription(MESSAGES.reportPertDeltaTimePref()),
				new TableHeaderIterface(MESSAGES.colPerturbationDeltaRoomPref()).setDescription(MESSAGES.reportPertDeltaRoomPref()),
				new TableHeaderIterface(MESSAGES.colPerturbationDeltaInstructorBTB()).setDescription(MESSAGES.reportPertDeltaInstructorBTBPref())
				);
				
        try {
        	for (Iterator i=report.getGroups().iterator();i.hasNext();) {
        		PerturbationReport.PerturbationGroup g = (PerturbationReport.PerturbationGroup)i.next();
        		ClassAssignmentDetails ca = g.getClazz();
        		ClassAssignmentDetails.ClassInfo clazz = ca.getClazz();
        		
        		ClassAssignmentDetails.TimeInfo timeBefore = ca.getTime();
        		ClassAssignmentDetails.TimeInfo timeAfter = ca.getAssignedTime();
        		
    	    	TableCellChange date = new TableCellChange(
    	    			timeBefore == null ? null : new TableCellInterface(timeBefore.getDatePatternName()).setColor(PreferenceLevel.int2color(timeBefore.getDatePatternPreference())),
    	    			timeAfter == null ? null : new TableCellInterface(timeAfter.getDatePatternName()).setColor(PreferenceLevel.int2color(timeAfter.getDatePatternPreference())));
    	    	
    	    	TableCellChange time = new TableCellChange(
    	    			timeBefore == null ? null : new TableInterface.TableCellTime(timeBefore.getDaysName() + " " + timeBefore.getStartTime() + " - " + timeBefore.getEndTime())
    	    	    			.setId(clazz.getClassId() + "," + timeBefore.getDays() + "," + timeBefore.getStartSlot()).setColor(PreferenceLevel.int2color(timeBefore.getPref())),
    	    	    	timeAfter == null ? null : new TableInterface.TableCellTime(timeAfter.getDaysName() + " " + timeAfter.getStartTime() + " - " + timeAfter.getEndTime())
    	    	    	    	.setId(clazz.getClassId() + "," + timeAfter.getDays() + "," + timeAfter.getStartSlot()).setColor(PreferenceLevel.int2color(timeAfter.getPref())));
    	    	
    	    	ClassAssignmentDetails.RoomInfo[] roomBefore = ca.getRoom();
    	    	ClassAssignmentDetails.RoomInfo[] roomAfter = ca.getAssignedRoom();
    	    	TableCellChange room = new TableCellChange();
    	    	if (roomBefore != null) {
    	    		TableCellRooms beforeRooms = new TableCellRooms();
        	    	for (int j = 0; j < roomBefore.length; j++) {
        	    		beforeRooms.add(
        	    				roomBefore[j].getName(),
        	    				roomBefore[j].getColor(),
        	    				roomBefore[j].getId(),
        	    				PreferenceLevel.int2string(roomBefore[j].getPref()));
        	    	}
        	    	room.setFirst(beforeRooms);
        	    }
    	    	if (roomAfter != null) {
    	    		TableCellRooms afterRooms = new TableCellRooms();
    	    		for (int j = 0; j < roomAfter.length; j++) {
    	    			afterRooms.add(
    	    					roomAfter[j].getName(),
    	    					roomAfter[j].getColor(),
    	    					roomAfter[j].getId(),
        	    				PreferenceLevel.int2string(roomAfter[j].getPref()));
        	    	}
        	    	room.setSecond(afterRooms);
        	    	if (roomAfter.length == 0 && roomBefore == null)
        	    		room.setFirst(new TableCellRooms());
        	    }
        		
        		table.addRow(new TableRowInterface(g.getClazz().getClazz().getClassId(), 
        				"gwt.jsp?page=suggestions&menu=hide&id=" + clazz.getClassId(),
    	    			MESSAGES.dialogSuggestions(),
    	    			new TableCellClickableClassName(clazz.getClassId(), clazz.getName()).setColor(PreferenceLevel.prolog2color(clazz.getPref())),
    	    			date, time, room,
    	    			new TableCellInterface<Double>(g.distance).setFormattedValue((Math.round(g.distance) > 0 ? MESSAGES.reportDistanceInMeter((int)Math.round(g.distance)) : "")),
        				dispNumber(g.affectedStudents),
        				dispNumber(g.affectedStudentsByTime),
        				dispNumber(g.affectedStudentsByRoom),
        				dispNumber(g.affectedStudentsByBldg),
        				dispNumber(g.affectedInstructors),
        				dispNumber(g.affectedInstructorsByTime),
        				dispNumber(g.affectedInstructorsByRoom),
        				dispNumber(g.affectedInstructorsByBldg),
        				dispNumber(g.differentRoom),
        				dispNumber(g.differentBuilding),
        				dispNumber(g.differentTime),
        				dispNumber(g.differentDay),
        				dispNumber(g.differentHour),
        				dispNumber(g.tooFarForStudents),
        				dispNumber(g.tooFarForInstructors),
        				dispNumber(g.deltaStudentConflicts),
        				dispNumber(g.newStudentConflicts),
        				dispNumber(Math.round(g.deltaTimePreferences)),
        				dispNumber(g.deltaRoomPreferences),
        				dispNumber(g.deltaInstructorDistancePreferences)
        				));
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.setErrorMessage(MESSAGES.failedToComputeReport(e.getMessage()));
        }
		Collections.sort(table.getRows(), new Comparator<TableInterface.TableRowInterface>() {
			@Override
			public int compare(TableRowInterface r1, TableRowInterface r2) {
				return r1.compareTo(r2, 0, true);
			}
		});
        return table;
	}

}
