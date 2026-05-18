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
package org.unitime.timetable.server.exams;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.ExaminationsTableBuilder;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DistributionConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamInstructorInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.webutil.Navigation;

public class ExamReportsTableBuilder extends ExaminationsTableBuilder {

	public ExamReportsTableBuilder(SessionContext context, String backType, String backId) {
		super(context, backType, backId);
	}
	
	public static enum ExamReport {
		ExamAssignmentReport,
		RoomAssignmentReport,
		Statistics,
		PeriodUsage,
		NrExamsADay,
		RoomSplits,
		ViolatedDistributions,
		DirectStudentConflicts,
		More2ADayStudentConflicts,
		BackToBackStudentConflicts,
		IndividualStudentConflicts,
		IndividualDirectStudentConflicts,
		IndividualBackToBackStudentConflicts,
		IndividualMore2ADayStudentConflicts,
		DirectInstructorConflicts,
		More2ADayInstructorConflicts,
		BackToBackInstructorConflicts,
		IndividualInstructorConflicts,
		IndividualDirectInstructorConflicts,
		IndividualBackToBackInstructorConflicts,
		IndividualMore2ADayInstructorConflicts,
		IndividualStudentSchedule,
		IndividualInstructorSchedule,
		;
		
		public String getLabel() { return getReportName(this); }
	}

	public static String getReportName(ExamReport report) {
		switch (report) { 
		case ExamAssignmentReport: return XMSG.reportExamAssignmentReport();
		case RoomAssignmentReport: return XMSG.reportRoomAssignmentReport();
		case Statistics: return XMSG.reportStatistics();
		case PeriodUsage: return XMSG.reportPeriodUsage();
		case NrExamsADay: return XMSG.reportNrExamsADay();
		case RoomSplits: return XMSG.reportRoomSplits();
		case ViolatedDistributions: return XMSG.reportViolatedDistributions();
		case DirectStudentConflicts: return XMSG.reportDirectStudentConflicts();
		case More2ADayStudentConflicts: return XMSG.reportMore2ADayStudentConflicts();
		case BackToBackStudentConflicts: return XMSG.reportBackToBackStudentConflicts();
		case IndividualStudentConflicts: return XMSG.reportIndividualStudentConflicts();
		case IndividualDirectStudentConflicts: return XMSG.reportIndividualDirectStudentConflicts();
		case IndividualBackToBackStudentConflicts: return XMSG.reportIndividualBackToBackStudentConflicts();
		case IndividualMore2ADayStudentConflicts: return XMSG.reportIndividualMore2ADayStudentConflicts();
		case DirectInstructorConflicts: return XMSG.reportDirectInstructorConflicts();
		case More2ADayInstructorConflicts: return XMSG.reportMore2ADayInstructorConflicts();
		case BackToBackInstructorConflicts: return XMSG.reportBackToBackInstructorConflicts();
		case IndividualInstructorConflicts: return XMSG.reportIndividualInstructorConflicts();
		case IndividualDirectInstructorConflicts: return XMSG.reportIndividualDirectInstructorConflicts();
		case IndividualBackToBackInstructorConflicts: return XMSG.reportIndividualBackToBackInstructorConflicts();
		case IndividualMore2ADayInstructorConflicts: return XMSG.reportIndividualMore2ADayInstructorConflicts();
		case IndividualStudentSchedule: return XMSG.reportIndividualStudentSchedule();
		case IndividualInstructorSchedule: return XMSG.reportIndividualInstructorSchedule();
		default: return report.name();
		}
	}
	
	public TableInterface generateExamReportTable(
            ExamType examType, 
            ExamSolverProxy solver,
            FilterInterface filter, 
            String[] subjectAreaIds){
		
		Set<Long> ids = new HashSet<Long>();
		for (String id: subjectAreaIds) {
			try {
				ids.add(Long.valueOf(id));
			} catch (NumberFormatException e) {
				SubjectArea subject = SubjectArea.findByAbbv(getCurrentAcademicSessionId(), id);
				if (subject != null)
					ids.add(subject.getUniqueId());
			}
		}
		
		Collection<ExamAssignmentInfo> assignedExams = getAssignedExams(solver, getSessionContext(), examType, ids);
		return generateExamReportTable(assignedExams, filter);
	}
	
	public static Collection<ExamAssignmentInfo> getAssignedExams(ExamSolverProxy solver, SessionContext context, ExamType type, Set<Long> subjectAreaIds) {
		if (solver != null && solver.getExamTypeId().equals(type.getUniqueId())) {
			return solver.getAssignedExams(subjectAreaIds);
		} else {
			if (ApplicationProperty.ExaminationCacheConflicts.isTrue()) {
		    	CacheAssignedExams cache = new CacheAssignedExams(context.getUser().getCurrentAcademicSessionId(), type.getUniqueId());
		        return cache.getAssignedExams(subjectAreaIds);
			} else {
				return Exam.findAssignedExams(context.getUser().getCurrentAcademicSessionId(), subjectAreaIds, type.getUniqueId());
			}
		}
	}
	
	public TableInterface generateExamReportTable(Collection<ExamAssignmentInfo> assignedExams, FilterInterface filter) {
        ExamReport report = ExamReport.valueOf(filter.getParameterValue("report"));
        TableInterface ret = null;
        switch (report) {
		case ExamAssignmentReport:
			ret = generateAssignmentReport(assignedExams, filter);
			break;
		case RoomAssignmentReport:
			ret = generateRoomReport(assignedExams, filter);
			break;
		case PeriodUsage:
			ret = generatePeriodUsageReport(assignedExams, filter);
			break;
		case Statistics:
			ret = generateStatisticsReport(assignedExams, filter);
			break;
		case RoomSplits:
			ret = generateRoomSplitReport(assignedExams, filter);
			break;
		case ViolatedDistributions:
			ret = generateViolatedDistributionsReport(assignedExams, filter);
			break;
        case IndividualStudentConflicts:
        	ret = generateIndividualConflictsReport(assignedExams, filter, true, true, true, true);
        	break;
        case IndividualDirectStudentConflicts:
        	ret = generateIndividualConflictsReport(assignedExams, filter, true, true, false, false);
        	break;
        case IndividualBackToBackStudentConflicts:
        	ret = generateIndividualConflictsReport(assignedExams, filter, true, false, false, true);
        	break;
        case IndividualMore2ADayStudentConflicts:
        	ret = generateIndividualConflictsReport(assignedExams, filter, true, false, true, false);
        	break;
        case IndividualInstructorConflicts:
        	ret = generateIndividualConflictsReport(assignedExams, filter, false, true, true, true);
        	break;
        case IndividualDirectInstructorConflicts:
        	ret = generateIndividualConflictsReport(assignedExams, filter, false, true, false, false);
        	break;
        case IndividualBackToBackInstructorConflicts:
        	ret = generateIndividualConflictsReport(assignedExams, filter, false, false, false, true);
        	break;
        case IndividualMore2ADayInstructorConflicts:
        	ret = generateIndividualConflictsReport(assignedExams, filter, false, false, true, false);
        	break;
        case DirectStudentConflicts:
        	ret = generateDirectConflictsReport(assignedExams, filter, true);
        	break;
        case DirectInstructorConflicts:
        	ret = generateDirectConflictsReport(assignedExams, filter, false);
        	break;
        case BackToBackStudentConflicts:
        	ret = generateBackToBackConflictsReport(assignedExams, filter, true);
        	break;
        case BackToBackInstructorConflicts:
        	ret = generateBackToBackConflictsReport(assignedExams, filter, false);
        	break;
        case More2ADayStudentConflicts:
        	ret = generate2MoreADayConflictsReport(assignedExams, filter, true);
        	break;
        case More2ADayInstructorConflicts:
        	ret = generate2MoreADayConflictsReport(assignedExams, filter, false);
        	break;
        case NrExamsADay:
        	ret = generateNrExamsADayReport(assignedExams, filter);
        	break;
        case IndividualStudentSchedule:
        	ret = generateIndividualAssignmentReport(assignedExams, filter, true);
        	break;
        case IndividualInstructorSchedule:
        	ret = generateIndividualAssignmentReport(assignedExams, filter, false);
        	break;
		default:
			ret = new TableInterface();
			
			ret.setName(report.getLabel());
			ret.setId(report.name());
			ret.setErrorMessage(report.getLabel() + " report not implemented.");
		}
		
		ret.setName(getReportName(report));
		ret.setId(report.name());
		
		boolean hasBack = false;
		List<Long> examIds = new ArrayList<Long>();
		if (ret.hasLines())
			for (LineInterface line: ret.getLines())
				if (line.getId() != null && !examIds.contains(line.getId())) {
					examIds.add(line.getId());
					if (line.getId().toString().equals(getBackId()) && line.hasCells()) {
						line.getCell(0).addAnchor("back");
		            	hasBack = true;
		            }
				}
		
        if (!isSimple()) {
        	Navigation.set(getSessionContext(), Navigation.sInstructionalOfferingLevel, examIds);
        	ret.setNavigationLevel(Navigation.sInstructionalOfferingLevel);
        }

        if (!hasBack && "Exam".equals(getBackType()) && ret.hasHeader())
        	ret.getHeader().get(0).getCells().get(0).addAnchor("back");
        
        if (ret.hasHeader())
        	for (CellInterface cell: ret.getHeader().get(0).getCells()) {
        		cell.setClassName("WebTableHeader");
        		cell.setText(cell.getText().replace("<br>", "\n"));
        		cell.addStyle("white-space: pre-wrap;");
        		if (!cell.hasSortable()) cell.setSortable(true);
        	}
        
        if (filter.getParameterValue("sort") == null)
        	ret.sort(0, true);

		return ret;
	}
	
	
	protected boolean match(FilterInterface filter, String name) {
		String textFilter = filter.getParameterValue("filter");
	    if (textFilter == null || textFilter.trim().isEmpty()) return true;
	    String n = (name == null ? "" : name).toUpperCase();
        StringTokenizer stk1 = new StringTokenizer(textFilter.toUpperCase(),";");
        while (stk1.hasMoreTokens()) {
            StringTokenizer stk2 = new StringTokenizer(stk1.nextToken()," ,");
            boolean match = true;
            while (match && stk2.hasMoreTokens()) {
                String token = stk2.nextToken().trim();
                if (token.length()==0) continue;
                if (token.indexOf('*')>=0 || token.indexOf('?')>=0) {
                    try {
                        String tokenRegExp = "\\s+"+token.replaceAll("\\.", "\\.").replaceAll("\\?", ".+").replaceAll("\\*", ".*")+"\\s";
                        if (!Pattern.compile(tokenRegExp).matcher(" "+n+" ").find()) match = false;
                    } catch (PatternSyntaxException e) { match = false; }
                } else if (n.indexOf(token)<0) match = false;
            }
            if (match) return true;
        }
        return false;
	}
	
	protected boolean match(FilterInterface filter, ExamAssignment exam) {
	    if (exam==null) return false;
	    if ("1".equals(filter.getParameterValue("showSections"))) {
	        for (ExamSectionInfo section : exam.getSections())
	            if (match(filter, section.getName())) return true;
	        return false;
	    } else {
	        return match(filter, exam.getExamName());
	    }
	}
	
	protected void fillConflicts(CellInterface sc, int dc, int m2d, int btb, int dbtb, String name, String cmp) {
		if (dc !=0 || m2d !=0 || btb != 0 || dbtb != 0) {
        	sc.add(String.valueOf(dc)).setColor(dc > 0 ? PreferenceLevel.prolog2color("P") : null);
        	sc.add(", ");
        	sc.add(String.valueOf(m2d)).setColor(m2d > 0 ? PreferenceLevel.prolog2color("2") : null);
        	sc.add(", ");
        	sc.add(String.valueOf(btb)).setColor(btb > 0 ? PreferenceLevel.prolog2color("1") : null);
        	if (dbtb > 0)
        		sc.add(" (" + XMSG.prefixDistanceConclict() + dbtb + ")").setColor(PreferenceLevel.prolog2color("1"));
        }
		if (cmp == null)
			sc.setComparable(-dc, -m2d, -btb, -dbtb, name);
		else
			sc.setComparable(cmp, -dc, -m2d, -btb, -dbtb, name);
	}
	
	protected CellInterface addDate(LineInterface line, ExamAssignment exam, ExamSectionInfo section, String cmp) {
    	CellInterface date = (line == null ? new CellInterface().setText(exam.getDate(false)) : line.addCell(exam.getDate(false)));
		date.setColor(PreferenceLevel.prolog2color(exam.getPeriodPref()));
		if (cmp == null)
			date.setComparable(exam.getPeriod().getStartDate(), exam.getPeriod().getStartSlot(), (section == null ? exam.getExamName() : section.getName()));
		else
			date.setComparable(cmp, exam.getPeriod().getStartDate(), exam.getPeriod().getStartSlot(), (section == null ? exam.getExamName() : section.getName()));
    	if (ApplicationProperty.LegacyPeriodPreferences.isTrue()) {
    		date.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement,'" + exam.getExamId() + "," + exam.getPeriodId() + "');");
    		date.setMouseOut("$wnd.hideGwtTimeHint();");
    	} else {
    		date.setMouseOver("$wnd.showGwtExamPeriodPreferencesHint($wnd.lastMouseOverElement, '" + exam.getExamId() + "','" + exam.getPeriodId() + "');");
    		date.setMouseOut("$wnd.hideGwtPeriodPreferencesHint();");
    	}
    	date.setNoWrap(true);
    	return date;
	}
	
	protected CellInterface addTime(LineInterface line, ExamAssignment exam, ExamSectionInfo section, String cmp) {
    	CellInterface time = (line == null ? new CellInterface().setText(exam.getTime(false)) : line.addCell(exam.getTime(false)));
		time.setColor(PreferenceLevel.prolog2color(exam.getPeriodPref()));
		if (cmp == null)
			time.setComparable(exam.getPeriod().getStartSlot(), (section == null ? exam.getExamName() : section.getName()));
		else
			time.setComparable(cmp, exam.getPeriod().getStartSlot(), (section == null ? exam.getExamName() : section.getName()));
    	if (ApplicationProperty.LegacyPeriodPreferences.isTrue()) {
    		time.setMouseOver("$wnd.showGwtExamPeriodPreferencesHint($wnd.lastMouseOverElement, '" + exam.getExamId() + "','" + exam.getPeriodId() + "');");
    		time.setMouseOut("$wnd.hideGwtPeriodPreferencesHint();");
    	} else {
    		time.setMouseOver("$wnd.showGwtExamPeriodPreferencesHint($wnd.lastMouseOverElement, '" + exam.getExamId() + "','" + exam.getPeriodId() + "');");
    		time.setMouseOut("$wnd.hideGwtPeriodPreferencesHint();");
    	}
    	time.setNoWrap(true);
    	return time;
	}
	
	protected CellInterface addTime(LineInterface line, ExamInfo exam, ExamSectionInfo section, String cmp) {
		if (exam instanceof ExamAssignment) {
			return addTime(line, (ExamAssignment)exam, section, cmp);
		} else {
			return (line == null ? new CellInterface() : line.addCell());
		}
	}
	
	protected CellInterface addDate(LineInterface line, ExamInfo exam, ExamSectionInfo section, String cmp) {
		if (exam instanceof ExamAssignment) {
			return addDate(line, (ExamAssignment)exam, section, cmp);
		} else {
			return (line == null ? new CellInterface() : line.addCell());
		}
	}
	
	protected void addDateAndTime(LineInterface line, ExamAssignment exam, ExamSectionInfo section, String cmp) {
		addDate(line, exam, section, cmp);
		addTime(line, exam, section, cmp);
	}
	
	protected void addRoomAndCap(LineInterface line, ExamAssignment exam, ExamSectionInfo section) {
		CellInterface rooms = line.addCell();
    	CellInterface roomCaps = line.addCell().setTextAlignment(Alignment.RIGHT);
    	if (exam.getRooms() != null)
            for (ExamRoomInfo room : exam.getRooms()) {
            	if (rooms.hasItems()) { rooms.add(", "); roomCaps.add(", "); }
            	CellInterface c = rooms.add(room.getName())
            			.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');")
            			.setMouseOut("$wnd.hideGwtRoomHint();");
            	c.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
            	c = roomCaps.add(String.valueOf(room.getCapacity()))
            			.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');")
            			.setMouseOut("$wnd.hideGwtRoomHint();");
            	c.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
            }
    	rooms.setComparable(exam.getRoomsName(":"), (section == null ? exam.getExamName() : section.getName()));
    	rooms.setNoWrap(true);
    	roomCaps.setComparable(-exam.getRoomsCapacity(), (section == null ? exam.getExamName() : section.getName()));
    	roomCaps.setNoWrap(true);
	}
	
	protected CellInterface addRoom(LineInterface line, ExamInfo exam, ExamSectionInfo section) {
		CellInterface rooms = (line == null ? new CellInterface() : line.addCell());
		if (exam instanceof ExamAssignment) {
	    	if (((ExamAssignment)exam).getRooms() != null)
	            for (ExamRoomInfo room : ((ExamAssignment)exam).getRooms()) {
	            	if (rooms.hasItems()) { rooms.add(", "); }
	            	CellInterface c = rooms.add(room.getName())
	            			.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');")
	            			.setMouseOut("$wnd.hideGwtRoomHint();");
	            	c.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
	            }
	    	rooms.setComparable(((ExamAssignment)exam).getRoomsName(":"), (section == null ? exam.getExamName() : section.getName()));
		}
		rooms.setNoWrap(true);
    	return rooms;
	}
	
	public TableInterface generateAssignmentReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter) {
		TableInterface ret = new TableInterface();
		
		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		
		LineInterface header = ret.addHeader();
        header.addCell(showSection ? XMSG.colOwner() : XMSG.colExamination());
        header.addCell(XMSG.colEnrollment()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colSeatingType()).setTextAlignment(Alignment.CENTER);
        header.addCell(XMSG.colDate());
        header.addCell(XMSG.colTime());
        header.addCell(XMSG.colRoom());
        header.addCell(XMSG.colRoomCapacity()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colInstructor());
        header.addCell(XMSG.colStudentConflicts());
        header.addCell(XMSG.colInstructorConflicts());

        for (ExamAssignmentInfo exam : exams) {
        	if (showSection) {
                boolean firstSection = true; 
                for (ExamSectionInfo section : exam.getSections()) {
                    if (!match(filter, section.getName())) continue;
                    
                    LineInterface line = ret.addLine();
                    if (firstSection) line.setId(exam.getExamId());
                    if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                    	line.setURL("examDetail.action?examId=" + exam.getExamId());
                    
                    line.addCell(section.getName());
                    line.addCell(String.valueOf(section.getNrStudents())).setTextAlignment(Alignment.RIGHT)
                    	.setComparable(section.getNrStudents(), section.getName());
                    line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                    	.setTextAlignment(Alignment.CENTER)
                    	.setComparable(exam.getSeatingType(), section.getName());
                    
                    addDateAndTime(line, exam, section, null);
                    addRoomAndCap(line, exam, section);
                    
                	line.addCell(exam.getInstructorName("; ")).setComparable(exam.getInstructorName(":"), section.getName());

                    fillConflicts(line.addCell(),
                    		exam.getNrDirectConflicts(section),
                    		exam.getNrMoreThanTwoConflicts(section),
                    		exam.getNrBackToBackConflicts(section),
                    		exam.getNrDistanceBackToBackConflicts(section),
                    		section.getName(), null);

                    fillConflicts(line.addCell(),
                    		exam.getNrInstructorDirectConflicts(section),
                    		exam.getNrInstructorMoreThanTwoConflicts(section),
                    		exam.getNrInstructorBackToBackConflicts(section),
                    		exam.getNrInstructorDistanceBackToBackConflicts(section),
                    		section.getName(), null);

                    firstSection = false;
                }
            } else {
            	if (!match(filter, exam.getExamName())) continue;
            	
            	LineInterface line = ret.addLine();
                line.setId(exam.getExamId());
                if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                	line.setURL("examDetail.action?examId=" + exam.getExamId());
                
                line.addCell(exam.getExamName());
                line.addCell(String.valueOf(exam.getNrStudents())).setTextAlignment(Alignment.RIGHT)
                	.setComparable(exam.getNrStudents(), exam.getExamName());
                line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                	.setTextAlignment(Alignment.CENTER)
                	.setComparable(exam.getSeatingType(), exam.getExamName());
                
                addDateAndTime(line, exam, null, null);
                addRoomAndCap(line, exam, null);
            	
            	line.addCell(exam.getInstructorName("; ")).setComparable(exam.getInstructorName(":"), exam.getExamName());

                fillConflicts(line.addCell(),
                		exam.getNrDirectConflicts(),
                		exam.getNrMoreThanTwoConflicts(),
                		exam.getNrBackToBackConflicts(),
                		exam.getNrDistanceBackToBackConflicts(),
                		exam.getExamName(), null);

                fillConflicts(line.addCell(),
                		exam.getNrInstructorDirectConflicts(),
                		exam.getNrInstructorMoreThanTwoConflicts(),
                		exam.getNrInstructorBackToBackConflicts(),
                		exam.getNrInstructorDistanceBackToBackConflicts(),
                		exam.getExamName(), null);
            }
        }
        
		return ret;
		
	}
	
	public TableInterface generateRoomReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter) {
		TableInterface ret = new TableInterface();
		ret.setBlankWhenSame(true);
		
		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		
		LineInterface header = ret.addHeader();
        header.addCell(XMSG.colRoom());
        header.addCell(XMSG.colRoomCapacity()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colExamCapacity()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colDate());
        header.addCell(XMSG.colTime());
        header.addCell(showSection ? XMSG.colOwner() : XMSG.colExamination());
        header.addCell(XMSG.colEnrollment()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colSeatingType()).setTextAlignment(Alignment.CENTER);
        header.addCell(XMSG.colInstructor());
        header.addCell(XMSG.colStudentConflicts());
        header.addCell(XMSG.colInstructorConflicts());

        for (ExamAssignmentInfo exam : exams) {
            boolean match = false;
            for (ExamRoomInfo room : exam.getRooms()) {
                if (match(filter, room.getName())) { match = true; break; }
            }
            if (!match) continue;
        	if (showSection) {
                boolean firstRoom = true;
        		for (ExamSectionInfo section : exam.getSections()) {
                    for (ExamRoomInfo room : exam.getRooms()) {
                        if (!match(filter, room.getName())) continue;
                        
                        LineInterface line = ret.addLine();
                        if (firstRoom)
                        	line.setId(exam.getExamId());
                        if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                        	line.setURL("examDetail.action?examId=" + exam.getExamId());
                    
                        line.addCell(room.getName());
                        line.addCell(String.valueOf(room.getCapacity())).setTextAlignment(Alignment.RIGHT)
                    		.setComparable(room.getCapacity(), section.getName());
                        line.addCell(String.valueOf(room.getExamCapacity())).setTextAlignment(Alignment.RIGHT)
                			.setComparable(room.getExamCapacity(), section.getName());
                        
                        addDateAndTime(line, exam, section, room.getName()); 
                        
                        line.addCell(section.getName())
                        	.setComparable(room.getName(), section.getName());
                        
                        line.addCell(String.valueOf(section.getNrStudents())).setTextAlignment(Alignment.RIGHT)
                    		.setComparable(room.getName(), section.getNrStudents(), section.getName());
                        line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                    		.setTextAlignment(Alignment.CENTER)
                    		.setComparable(room.getName(), exam.getSeatingType(), section.getName());
                        
                        line.addCell(exam.getInstructorName("; "))
                        	.setComparable(room.getName(), exam.getInstructorName(":"), section.getName());
                        
                        fillConflicts(line.addCell(),
                        		exam.getNrDirectConflicts(section),
                        		exam.getNrMoreThanTwoConflicts(section),
                        		exam.getNrBackToBackConflicts(section),
                        		exam.getNrDistanceBackToBackConflicts(section),
                        		section.getName(), room.getName());

                        fillConflicts(line.addCell(),
                        		exam.getNrInstructorDirectConflicts(section),
                        		exam.getNrInstructorMoreThanTwoConflicts(section),
                        		exam.getNrInstructorBackToBackConflicts(section),
                        		exam.getNrInstructorDistanceBackToBackConflicts(section),
                        		section.getName(), room.getName());
                        
                        firstRoom = false;
                    }
        		}
            } else {
            	boolean firstRoom = true;
                for (ExamRoomInfo room : exam.getRooms()) {
                    if (!match(filter, room.getName())) continue;
                    
                    LineInterface line = ret.addLine();
                    if (firstRoom)
                    	line.setId(exam.getExamId());
                    if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                    	line.setURL("examDetail.action?examId=" + exam.getExamId());
                
                    line.addCell(room.getName());
                    line.addCell(String.valueOf(room.getCapacity())).setTextAlignment(Alignment.RIGHT)
                		.setComparable(room.getCapacity(), exam.getExamName());
                    line.addCell(String.valueOf(room.getExamCapacity())).setTextAlignment(Alignment.RIGHT)
            			.setComparable(room.getExamCapacity(), exam.getExamName());
                    
                    addDateAndTime(line, exam, null, room.getName()); 

                    line.addCell(exam.getExamName())
                    	.setComparable(room.getName(), exam.getExamName());
                    
                    line.addCell(String.valueOf(exam.getNrStudents())).setTextAlignment(Alignment.RIGHT)
                		.setComparable(room.getName(), exam.getNrStudents(), exam.getExamName());
                    line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                		.setTextAlignment(Alignment.CENTER)
                		.setComparable(room.getName(), exam.getSeatingType(), exam.getExamName());
                    
                    line.addCell(exam.getInstructorName("; "))
                    	.setComparable(room.getName(), exam.getInstructorName(":"), exam.getExamName());
                    
                    fillConflicts(line.addCell(),
                    		exam.getNrDirectConflicts(),
                    		exam.getNrMoreThanTwoConflicts(),
                    		exam.getNrBackToBackConflicts(),
                    		exam.getNrDistanceBackToBackConflicts(),
                    		exam.getExamName(), room.getName());

                    fillConflicts(line.addCell(),
                    		exam.getNrInstructorDirectConflicts(),
                    		exam.getNrInstructorMoreThanTwoConflicts(),
                    		exam.getNrInstructorBackToBackConflicts(),
                    		exam.getNrInstructorDistanceBackToBackConflicts(),
                    		exam.getExamName(), room.getName());
                    
                    firstRoom = false;
                }
            }
        }
        
		return ret;
	}
	
	public TableInterface generatePeriodUsageReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter) {
		TableInterface ret = new TableInterface();
		
		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		
		int limits[] = new int[] { 10, 50, 100, 500};
		
		LineInterface header = ret.addHeader();
        header.addCell(XMSG.colDate());
        header.addCell(XMSG.colTime());
        header.addCell(showSection ? XMSG.colOwner() : XMSG.colExamination());
        header.addCell(XMSG.colTotalEnrollment()).setTextAlignment(Alignment.RIGHT);
        for (int limit: limits)
        	header.addCell((showSection ? XMSG.colOwner() : XMSG.colExamination()) + "\n" + XMSG.withNOrMoreStudents(limit))
        		.setTextAlignment(Alignment.RIGHT);

        int tnrExams = 0, tnrStudents = 0;
        int[] tnrExamsLm =  new int[limits.length];
        for (int i = 0; i < limits.length; i++)
        	tnrExamsLm[i] = 0;

        String examType = filter.getParameterValue("examType");
		ExamType type = null;
		try {
			type = ExamTypeDAO.getInstance().get(Long.valueOf(examType));
		} catch (Exception e) {}
		if (type == null) type = ExamType.findByReference(examType);
		
		DecimalFormat df2 = new DecimalFormat("#,##0");
        for (ExamPeriod period: ExamPeriod.findAll(getCurrentAcademicSessionId(), type.getUniqueId())) {
        	LineInterface line = ret.addLine();
        	line.addCell(period.getStartDateLabel())
        		.setColor(PreferenceLevel.prolog2color(period.getPrefLevel().getPrefProlog()))
        		.setComparable(0, period.getDateOffset(), period.getStartSlot());
        	line.addCell(period.getStartTimeLabel()+" - "+period.getEndTimeLabel())
        		.setColor(PreferenceLevel.prolog2color(period.getPrefLevel().getPrefProlog()))
        		.setComparable(0, period.getStartSlot(), period.getDateOffset());
        	
            int nrExams = 0, nrStudents = 0;
            int[] nrExamsLm =  new int[limits.length];
            for (int i = 0; i < limits.length; i++)
            	nrExamsLm[i] = 0;
            for (ExamAssignmentInfo exam : exams) {
                if (!period.getUniqueId().equals(exam.getPeriodId())) continue;
                if (showSection) {
                    for (ExamSectionInfo section : exam.getSections()) {
                        if (!match(filter, section.getName())) continue;
                        nrExams++;
                        nrStudents+=section.getNrStudents();
                        for (int i = 0; i < limits.length; i++)
                        	if (section.getNrStudents() >= limits[i]) nrExamsLm[i]++;
                    }
                } else {
                    if (!match(filter, exam.getExamName())) continue;
                    nrExams++;
                    int nrStudentsThisExam = exam.getStudentIds().size(); 
                    nrStudents+=nrStudentsThisExam;
                    for (int i = 0; i < limits.length; i++)
                    	if (nrStudentsThisExam >= limits[i]) nrExamsLm[i]++;
                }
            }
            if (nrExams==0) continue;
            line.addCell(df2.format(nrExams)).setComparable(0, nrExams).setTextAlignment(Alignment.RIGHT);
            line.addCell(df2.format(nrStudents)).setComparable(0, nrStudents).setTextAlignment(Alignment.RIGHT);
            for (int i = 0; i < limits.length; i++) {
            	line.addCell(df2.format(nrExamsLm[i])).setComparable(0, nrExamsLm[i]).setTextAlignment(Alignment.RIGHT);
            	tnrExamsLm[i] += nrExamsLm[i];
            }
            tnrExams += nrExams;
            tnrStudents += nrStudents;
        }
        LineInterface line = ret.addLine();
        line.addCell(XMSG.colTotals())
        	.addStyle("font-weight: bold;")
        	.setClassName("top-border-dashed")
			.setComparable(1, 0, 0);
        line.addCell("")
        	.setClassName("top-border-dashed")
			.setComparable(1, 0, 0);
        line.addCell(df2.format(tnrExams)).setComparable(1, tnrExams).setTextAlignment(Alignment.RIGHT)
        	.addStyle("font-weight: bold;").setClassName("top-border-dashed");
        line.addCell(df2.format(tnrStudents)).setComparable(1, tnrStudents).setTextAlignment(Alignment.RIGHT)
        	.addStyle("font-weight: bold;").setClassName("top-border-dashed");
        for (int i = 0; i < limits.length; i++)
        	line.addCell(df2.format(tnrExamsLm[i])).setComparable(1, tnrExamsLm[i]).setTextAlignment(Alignment.RIGHT)
        		.addStyle("font-weight: bold;").setClassName("top-border-dashed");
                
		return ret;
	}
	
	public TableInterface generateStatisticsReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter) {
		TableInterface ret = new TableInterface();
		
		LineInterface header = ret.addHeader();
        header.addCell(XMSG.colName());
        header.addCell(XMSG.colValue()).setTextAlignment(Alignment.RIGHT).setSortable(false);

        int row=0;
        int sdc=0,sdcna=0,sbtb=0,sdbtb=0,sm2d=0;
        int idc=0,idcna=0,ibtb=0,idbtb=0,im2d=0;
        HashSet<Long>[] sct = new HashSet[] {new HashSet(),new HashSet(),new HashSet(),new HashSet()};
        HashSet<Long> students = new HashSet<Long>();
        int studentExams = 0;
        DecimalFormat df1 = new DecimalFormat("0.00");
        DecimalFormat df2 = new DecimalFormat("#,##0");
        int instructorExams = 0;
        HashSet<Long> instructors = new HashSet<Long>();
        
        for (ExamAssignmentInfo exam:exams) {
            for (ExamSectionInfo section:exam.getSections()) {
                sct[section.getOwnerType()].add(section.getOwnerId());
                students.addAll(section.getStudentIds());
            }
            studentExams += exam.getStudentIds().size();
            instructorExams += exam.getInstructors().size(); 
            for (DirectConflict dc : exam.getDirectConflicts()) {
                if (dc.getOtherExam()!=null && exam.compareTo(dc.getOtherExam())>=0 && exams.contains(dc.getOtherExam())) continue;
                sdc+=dc.getNrStudents(); 
                if (dc.getOtherExam()==null) sdcna+=dc.getNrStudents();
            }
            for (DirectConflict dc : exam.getInstructorDirectConflicts()) {
                if (dc.getOtherExam()!=null && exam.compareTo(dc.getOtherExam())>=0 && exams.contains(dc.getOtherExam())) continue;
                idc+=dc.getNrStudents();
                if (dc.getOtherExam()==null) idcna+=dc.getNrStudents();
            }
            for (BackToBackConflict btb : exam.getBackToBackConflicts()) {
                if (btb.getOtherExam()!=null && exam.compareTo(btb.getOtherExam())>=0 && exams.contains(btb.getOtherExam())) continue;
                sbtb+=btb.getNrStudents();
                if (btb.isDistance()) sdbtb+=btb.getNrStudents();
            }
            for (BackToBackConflict btb : exam.getInstructorBackToBackConflicts()) {
                if (btb.getOtherExam()!=null && exam.compareTo(btb.getOtherExam())>=0 && exams.contains(btb.getOtherExam())) continue;
                ibtb+=btb.getNrStudents();
                if (btb.isDistance()) idbtb+=btb.getNrStudents();
            }
            m2d: for (MoreThanTwoADayConflict m2d: exam.getMoreThanTwoADaysConflicts()) {
                for (ExamAssignment other : m2d.getOtherExams())
                    if (exam.compareTo(other)>=0 && exams.contains(other)) continue m2d;
                sm2d+=m2d.getNrStudents();
            }
            m2d: for (MoreThanTwoADayConflict m2d: exam.getInstructorMoreThanTwoADaysConflicts()) {
                for (ExamAssignment other : m2d.getOtherExams())
                    if (exam.compareTo(other)>=0 && exams.contains(other)) continue m2d;
                im2d+=m2d.getNrStudents();
            }
        }
        LineInterface line = ret.addLine();
        line.addCell(XMSG.propNumberOfExams()).setComparable(row++);
        line.addCell(df2.format(exams.size())).setTextAlignment(Alignment.RIGHT);
        String indent = "\t";
        for (int i=0;i<ExamOwner.sOwnerTypes.length;i++)
            if (!sct[i].isEmpty()) {
            	line = ret.addLine();
            	line.addCell(indent + XMSG.propOwnersWithAnExam(
            			i == ExamOwner.sOwnerTypeClass ? XMSG.typeClasses() :
            			i == ExamOwner.sOwnerTypeConfig ? XMSG.typeConfigs() :
            			i == ExamOwner.sOwnerTypeCourse ? XMSG.typeCourses() : XMSG.typeOfferings()))
            		.addStyle("padding-left: 20px;")
            		.setComparable(row++);
            	line.addCell(df2.format(sct[i].size()))
            		.setTextAlignment(Alignment.RIGHT);
            	
            }
        
        line = ret.addLine();
        line.addCell("").setComparable(row++);
        line.addCell("");
        
        line = ret.addLine();
        line.addCell(indent + XMSG.propStudentsEnrolledInClasses())
        	.addStyle("padding-left: 20px;")
			.setComparable(row++);
        line.addCell(df2.format(
        		StudentDAO.getInstance().getSession().createQuery("select count(distinct s) from Student s inner join s.classEnrollments c where s.session.uniqueId=:sessionId", Number.class)
                .setParameter("sessionId", getCurrentAcademicSessionId()).uniqueResult()))
        	.setTextAlignment(Alignment.RIGHT);

        line = ret.addLine();
        line.addCell(indent + XMSG.propStudentsHavingAnExam())
    		.addStyle("padding-left: 20px;")
    		.setComparable(row++);
        line.addCell(df2.format(students.size()))
    		.setTextAlignment(Alignment.RIGHT);
        
        line = ret.addLine();
        line.addCell(indent + XMSG.propStudentExamEnrollments())
			.addStyle("padding-left: 20px;")
			.setComparable(row++);
        line.addCell(df2.format(studentExams))
			.setTextAlignment(Alignment.RIGHT);

        line = ret.addLine();
        line.addCell("").setComparable(row++);
        line.addCell("");
        
        if (!instructors.isEmpty()) {
        	line = ret.addLine();
        	line.addCell(XMSG.propRegisteredInstructors())
				.setComparable(row++);
        	line.addCell(df2.format(
            		StudentDAO.getInstance().getSession().createQuery("select count(i.externalUniqueId) from DepartmentalInstructor i where i.department.session.uniqueId=:sessionId", Number.class)
            		.setParameter("sessionId", getCurrentAcademicSessionId()).uniqueResult()))
        		.setTextAlignment(Alignment.RIGHT);
        	
        	line = ret.addLine();
            line.addCell(indent + XMSG.propInstructorsHavingAnExam())
        		.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df2.format(instructors.size()))
        		.setTextAlignment(Alignment.RIGHT);
            
            line = ret.addLine();
            line.addCell(indent + XMSG.propInstructorExamEnrollments())
        		.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df2.format(instructorExams))
        		.setTextAlignment(Alignment.RIGHT);
            
            line = ret.addLine();
            line.addCell("").setComparable(row++);
            line.addCell("");
        }

        if (sdc>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propDirectStudentConflicts())
        		.setComparable(row++);
            line.addCell(df2.format(sdc))
        		.setTextAlignment(Alignment.RIGHT);
        }
        
        if (sdcna>0) {
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propConflictWithOtherExam())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df2.format(sdc-sdcna))
        		.setTextAlignment(Alignment.RIGHT);
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propStudentNotAvailable())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df2.format(sdcna))
        		.setTextAlignment(Alignment.RIGHT);
        }

        if (sm2d>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propStudentMoreThanTwoExamsADayConflicts())
        		.setComparable(row++);
            line.addCell(df2.format(sm2d))
        		.setTextAlignment(Alignment.RIGHT);
        }
        
        if (sbtb>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propStudentBackToBackConflicts())
        		.setComparable(row++);
            line.addCell(df2.format(sdbtb))
        		.setTextAlignment(Alignment.RIGHT);
        }
        if (sdbtb>0) {
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propStudentDistanceBackToBackConflicts())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df2.format(sdbtb))
        		.setTextAlignment(Alignment.RIGHT);
        }
        
        if (idc>0 || im2d>0 || ibtb>0) {
        	line = ret.addLine();
            line.addCell("").setComparable(row++);
            line.addCell("");
        }
        
        if (idc>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propDirectInstructorConflicts())
        		.setComparable(row++);
            line.addCell(df2.format(idc))
        		.setTextAlignment(Alignment.RIGHT);
        }
        
        if (idcna>0) {
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propConflictWithOtherExam())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df2.format(idc-idcna))
        		.setTextAlignment(Alignment.RIGHT);
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propStudentNotAvailable())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df2.format(idcna))
        		.setTextAlignment(Alignment.RIGHT);
        }

        if (im2d>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propInstructorMoreThanTwoExamsADayConflicts())
        		.setComparable(row++);
            line.addCell(df2.format(im2d))
        		.setTextAlignment(Alignment.RIGHT);
        }
        
        if (ibtb>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propInstructorBackToBackConflicts())
        		.setComparable(row++);
            line.addCell(df2.format(idbtb))
        		.setTextAlignment(Alignment.RIGHT);
        }
        if (idbtb>0) {
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propInstructorDistanceBackToBackConflicts())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df2.format(idbtb))
        		.setTextAlignment(Alignment.RIGHT);
        }

        line = ret.addLine();
        line.addCell("").setComparable(row++);
        line.addCell("");

        if (sdc>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propDirectStudentConflicts())
        		.setComparable(row++);
            line.addCell(df1.format(100.0*sdc/studentExams) + "%")
        		.setTextAlignment(Alignment.RIGHT);
        }
        
        if (sdcna>0) {
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propConflictWithOtherExam())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df1.format(100.0*(sdc-sdcna)/studentExams) + "%")
        		.setTextAlignment(Alignment.RIGHT);
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propStudentNotAvailable())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df1.format(100.0*sdcna/studentExams) + "%")
        		.setTextAlignment(Alignment.RIGHT);
        }

        if (sm2d>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propStudentMoreThanTwoExamsADayConflicts())
        		.setComparable(row++);
            line.addCell(df1.format(100.0*sm2d/studentExams)+"%")
        		.setTextAlignment(Alignment.RIGHT);
        }
        
        if (sbtb>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propStudentBackToBackConflicts())
        		.setComparable(row++);
            line.addCell(df1.format(100.0*sbtb/studentExams)+"%")
        		.setTextAlignment(Alignment.RIGHT);
        }
        if (sdbtb>0) {
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propStudentDistanceBackToBackConflicts())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df1.format(100.0*sdbtb/studentExams)+"%")
        		.setTextAlignment(Alignment.RIGHT);
        }

        if (idc>0 || im2d>0 || ibtb>0) {
        	line = ret.addLine();
            line.addCell("").setComparable(row++);
            line.addCell("");
        }
        
        if (idc>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propDirectInstructorConflicts())
        		.setComparable(row++);
            line.addCell(df1.format(100.0*idc/studentExams)+"%")
        		.setTextAlignment(Alignment.RIGHT);
        }
        
        if (idcna>0) {
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propConflictWithOtherExam())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df1.format(100.0*(idc-idcna)/studentExams)+"%")
        		.setTextAlignment(Alignment.RIGHT);
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propStudentNotAvailable())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df1.format(100.0*idcna/studentExams)+"%")
        		.setTextAlignment(Alignment.RIGHT);
        }

        if (im2d>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propInstructorMoreThanTwoExamsADayConflicts())
        		.setComparable(row++);
            line.addCell(df1.format(100.0*im2d/studentExams)+"%")
        		.setTextAlignment(Alignment.RIGHT);
        }
        
        if (ibtb>0) {
        	line = ret.addLine();
            line.addCell(XMSG.propInstructorBackToBackConflicts())
        		.setComparable(row++);
            line.addCell(df1.format(100.0*ibtb/studentExams)+"%")
        		.setTextAlignment(Alignment.RIGHT);
        }
        if (idbtb>0) {
        	line = ret.addLine();
        	line.addCell(indent + XMSG.propInstructorDistanceBackToBackConflicts())
    			.addStyle("padding-left: 20px;")
        		.setComparable(row++);
            line.addCell(df1.format(100.0*idbtb/studentExams)+"%")
        		.setTextAlignment(Alignment.RIGHT);
        }
                
		return ret;
	}
	
	public TableInterface generateRoomSplitReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter) {
		TableInterface ret = new TableInterface();

		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		LineInterface header = ret.addHeader();
		header.addCell(showSection ? XMSG.colOwner() : XMSG.colExamination());
        header.addCell(XMSG.colEnrollment()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colSeatingType()).setTextAlignment(Alignment.CENTER);
        header.addCell(XMSG.colDate());
        header.addCell(XMSG.colTime());
        header.addCell(XMSG.colAverageDistance()).setTextAlignment(Alignment.RIGHT);
        int maxSplits = 2;
        for (ExamAssignmentInfo exam : exams) {
            if (exam.getRooms() !=null && exam.getRooms().size() > maxSplits)
            	maxSplits = exam.getRooms().size();
        }
        for (int i = 1; i <= maxSplits; i++) {
        	String pos;
        	switch (i) {
        		case 1: pos = XMSG.col1stExam(); break;
        		case 2: pos = XMSG.col2ndExam(); break;
        		case 3: pos = XMSG.col3rdExam(); break;
        		default: pos = XMSG.colNthExam(i); break;
        	}
        	header.addCell(pos + " " + XMSG.colRoom());
        	header.addCell(pos + " " + XMSG.colRoom() + "\n" + XMSG.colRoomCapacity());
        }
        
        DecimalFormat df1 = new DecimalFormat("#,##0.0");
        for (ExamAssignmentInfo exam : exams) {
            if (exam.getRooms() == null || exam.getRooms().size() <= 1) continue;
            if (showSection) {
                boolean firstSection = true; 
                for (ExamSectionInfo section : exam.getSections()) {
                    if (!match(filter, section.getName())) continue;
                    double distance = 0;
                    for (ExamRoomInfo r1 : exam.getRooms())
                        for (ExamRoomInfo r2 : exam.getRooms())
                            if (r1.getLocationId().compareTo(r2.getLocationId())<0) distance += r1.getDistance(r2);
                    distance /= exam.getRooms().size() * (exam.getRooms().size() - 1) / 2;
                    
                    LineInterface line = ret.addLine();
                    if (firstSection) line.setId(exam.getExamId());
                    if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                    	line.setURL("examDetail.action?examId=" + exam.getExamId());
                    
                    line.addCell(section.getName());
                    line.addCell(df1.format(section.getNrStudents())).setTextAlignment(Alignment.RIGHT)
                    	.setComparable(section.getNrStudents(), section.getName());
                    line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                    	.setTextAlignment(Alignment.CENTER)
                    	.setComparable(exam.getSeatingType(), section.getName());
                    
                    addDateAndTime(line, exam, section, null);
                    
                    line.addCell(distance <= 0.001 ? "" : df1.format(distance) + " m").setTextAlignment(Alignment.RIGHT)
                    	.setComparable(-distance, section.getName());
                    
                    for (ExamRoomInfo room : exam.getRooms()) {
                    	CellInterface c = line.addCell(room.getName())
                    			.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');")
                    			.setMouseOut("$wnd.hideGwtRoomHint();");
                    	c.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
                    	c.setComparable(room.getName(), section.getName());
                    	c = line.addCell(df1.format(room.getCapacity()))
                    			.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');")
                    			.setMouseOut("$wnd.hideGwtRoomHint();");
                    	c.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
                    	c.setComparable(-room.getCapacity(), section.getName());
                    }
                    firstSection = false;
                }
            } else {
            	if (!match(filter, exam.getExamName())) continue;
                double distance = 0;
                for (ExamRoomInfo r1 : exam.getRooms())
                    for (ExamRoomInfo r2 : exam.getRooms())
                        if (r1.getLocationId().compareTo(r2.getLocationId())<0) distance += r1.getDistance(r2);
                distance /= exam.getRooms().size() * (exam.getRooms().size() - 1) / 2;
                
                LineInterface line = ret.addLine();
                line.setId(exam.getExamId());
                if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                	line.setURL("examDetail.action?examId=" + exam.getExamId());
                
                line.addCell(exam.getExamName());
                line.addCell(df1.format(exam.getNrStudents())).setTextAlignment(Alignment.RIGHT)
                	.setComparable(exam.getNrStudents(), exam.getExamName());
                line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                	.setTextAlignment(Alignment.CENTER)
                	.setComparable(exam.getSeatingType(), exam.getExamName());
                
                addDateAndTime(line, exam, null, null);
                
                line.addCell(distance <= 0.001 ? "" : df1.format(distance) + " m").setTextAlignment(Alignment.RIGHT)
                	.setComparable(-distance, exam.getExamName());
                
                for (ExamRoomInfo room : exam.getRooms()) {
                	CellInterface c = line.addCell(room.getName())
                			.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');")
                			.setMouseOut("$wnd.hideGwtRoomHint();");
                	c.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
                	c.setComparable(room.getName(), exam.getExamName());
                	c = line.addCell(df1.format(room.getCapacity()))
                			.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');")
                			.setMouseOut("$wnd.hideGwtRoomHint();");
                	c.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
                	c.setComparable(-room.getCapacity(), exam.getExamName());
                }
            }
        }
        
        return ret;
	}
	
	public TableInterface generateViolatedDistributionsReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter) {
		TableInterface ret = new TableInterface();

		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		LineInterface header = ret.addHeader();
        header.addCell(XMSG.colPreference());
        header.addCell(XMSG.colDistribution());
        header.addCell(showSection ? XMSG.colOwner() : XMSG.colExamination());
        header.addCell(XMSG.colEnrollment()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colSeatingType()).setTextAlignment(Alignment.CENTER);
        header.addCell(XMSG.colDate());
        header.addCell(XMSG.colTime());
        header.addCell(XMSG.colRoom());
        
        HashSet<DistributionConflict> conflicts = new HashSet<DistributionConflict>();
        for (ExamAssignmentInfo exam : exams) {
            if (!match(filter, exam)) continue;
            for (DistributionConflict conf : exam.getDistributionConflicts()) {
                if (conflicts.contains(conf)) continue;
                conf.getOtherExams().add(exam); conflicts.add(conf);
            }
        }
        for (DistributionConflict conf : conflicts) {
        	LineInterface line = ret.addLine();
        	
        	CellInterface pref = line.addCell().add(PreferenceLevel.prolog2string(conf.getPreference()));
        	CellInterface type = line.addCell().add(conf.getType());
        	if (isUsePrefStyles()) {
        		pref.setClassName("pref-" + PreferenceLevel.prolog2char(conf.getPreference()));
        		type.setClassName("pref-" + PreferenceLevel.prolog2char(conf.getPreference()));
        	} else {
        		pref.setColor(PreferenceLevel.prolog2color(conf.getPreference()));
        		type.setColor(PreferenceLevel.prolog2color(conf.getPreference()));
        	}
        	CellInterface classes = line.addCell();
        	CellInterface enrollment = line.addCell().setTextAlignment(Alignment.RIGHT);
        	CellInterface seating = line.addCell().setTextAlignment(Alignment.CENTER);
        	CellInterface date = line.addCell();
        	CellInterface time = line.addCell();
        	CellInterface room = line.addCell();
        	
        	List<Integer> enrl = new ArrayList<Integer>();
            List<Date> dates = new ArrayList<Date>();
            List<Integer> times = new ArrayList<Integer>();
            List<String> rooms = new ArrayList<String>();
            for (ExamInfo exam:conf.getOtherExams()) {
                if (showSection) {
                	boolean firstSection = true;
                	for (ExamSectionInfo section : exam.getSections()) {
                		classes.add(section.getName()).setNoWrap(true).setInline(false);
                		enrollment.add(String.valueOf(section.getNrStudents())).setNoWrap(true).setInline(false);
                		enrl.add(section.getNrStudents());
                		if (firstSection) {
                			seating.add(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                				.setNoWrap(true).setInline(false);
                			date.addItem(addTime(null, exam, section, null).setNoWrap(true).setInline(false));
                			time.addItem(addDate(null, exam, section, null).setNoWrap(true).setInline(false));
                			room.addItem(addRoom(null, exam, section).setNoWrap(true).setInline(false));
                			firstSection = false;
                		} else {
                			seating.add("").setNoWrap(true).setInline(false);
                			date.add("").setNoWrap(true).setInline(false);
                			time.add("").setNoWrap(true).setInline(false);
                			room.add("").setNoWrap(true).setInline(false);
                		}
                	}
                } else {
                	classes.add(exam.getExamName()).setNoWrap(true).setInline(false);
            		enrollment.add(String.valueOf(exam.getNrStudents())).setNoWrap(true).setInline(false);
            		enrl.add(exam.getNrStudents());
        			seating.add(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
        				.setNoWrap(true).setInline(false);
        			date.addItem(addTime(null, exam, null, null).setNoWrap(true).setInline(false));
        			time.addItem(addDate(null, exam, null, null).setNoWrap(true).setInline(false));
        			room.addItem(addRoom(null, exam, null).setNoWrap(true).setInline(false));
                }
                if (exam instanceof ExamAssignment) {
                	ExamAssignment ea = (ExamAssignment) exam;
                	if (ea.getPeriod() != null) {
                		dates.add(ea.getPeriod().getStartTime());
                		times.add(ea.getPeriod().getStartSlot());
                	}
                	rooms.add(ea.getRoomsName(":"));
                }
            }
            enrollment.setComparable(enrl.toArray(new Integer[enrl.size()]));
            date.setComparable(dates.toArray(new Date[dates.size()]));
            time.setComparable(times.toArray(new Integer[times.size()]));
            room.setComparable(rooms.toArray(new String[rooms.size()]));
        }
        
        return ret;
	}
	
	public TableInterface generateIndividualConflictsReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter,
			boolean studentConf, boolean direct, boolean m2d, boolean btb) {
		TableInterface ret = new TableInterface();
		ret.setBlankWhenSame(true);
		
		Hashtable<Long, Student> students = new Hashtable<Long, Student>();
		if (studentConf) {
            HashSet<Long> allStudentIds = new HashSet<Long>();
            for (ExamAssignmentInfo exam : exams) {
                if (direct) for (DirectConflict conflict : exam.getDirectConflicts()) {
                    allStudentIds.addAll(conflict.getStudents());
                }
                if (btb) for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                    allStudentIds.addAll(conflict.getStudents());
                }
                if (m2d) for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                    allStudentIds.addAll(conflict.getStudents());
                }
            }
            String inSet = null; int idx = 0;
            for (Iterator<Long> i=allStudentIds.iterator();i.hasNext();idx++) {
                if (idx==1000) {
                    for (Student s: StudentDAO.getInstance().getSession().createQuery("select s from Student s where s.uniqueId in ("+inSet+")", Student.class).list()) {
                        students.put(s.getUniqueId(), s);
                    }
                    idx = 0; inSet = null;
                }
                if (inSet==null)
                    inSet = i.next().toString();
                else
                    inSet += ","+i.next();
            }
            if (inSet!=null) {
            	for (Student s: StudentDAO.getInstance().getSession().createQuery("select s from Student s where s.uniqueId in ("+inSet+")", Student.class).list()) {
                    students.put(s.getUniqueId(), s);
                }
            }
	    }

		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		LineInterface header = ret.addHeader();
        header.addCell(studentConf ? XMSG.colStudentId() : XMSG.colInstructorId());
        header.addCell(XMSG.colStudentOrInstructorName());
        header.addCell(XMSG.colType());
        header.addCell(showSection ? XMSG.colOwner() : XMSG.colExamination());
        header.addCell(XMSG.colEnrollment()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colSeatingType()).setTextAlignment(Alignment.CENTER);
        header.addCell(XMSG.colDate());
        header.addCell(XMSG.colTime());
        header.addCell(XMSG.colRoom());
        if (btb)
        	header.addCell(XMSG.colDistance()).setTextAlignment(Alignment.RIGHT);
        
        DecimalFormat df1 = new DecimalFormat("#,##0.0");
        for (ExamAssignmentInfo exam : exams) {
            if (direct)
                for (DirectConflict conflict : (studentConf?exam.getDirectConflicts():exam.getInstructorDirectConflicts())) {
                    if (conflict.getOtherExam()!=null && exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                    for (Long studentId : conflict.getStudents()) {
                        String id = "", name = "";
                        if (studentConf) {
                            Student student = students.get(studentId);
                            id = student.getExternalUniqueId();
                            name = student.getName(getInstructorNameFormat());
                        } else {
                            DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(studentId);
                            id = instructor.getExternalUniqueId();
                            name = instructor.getName(getInstructorNameFormat());
                        }
                        if (!match(filter, id) && !match(filter, name)) continue;
                    	LineInterface line = ret.addLine();
                    	line.addCell(id).setNoWrap(true);
                    	line.addCell(name).setNoWrap(true);
                    	line.addCell(XMSG.conflictDirect()).setColor(PreferenceLevel.prolog2color("P"))
                    		.setNoWrap(true).setComparable(0, name, exam.getExamName());
                    	CellInterface classes = line.addCell();
                    	CellInterface enrollment = line.addCell().setTextAlignment(Alignment.RIGHT);
                    	CellInterface seating = line.addCell().setTextAlignment(Alignment.CENTER);
                    	CellInterface date = line.addCell();
                    	CellInterface time = line.addCell();
                    	CellInterface room = line.addCell();
                    	List<Integer> enrl = new ArrayList<Integer>();
                        List<Date> dates = new ArrayList<Date>();
                        List<Integer> times = new ArrayList<Integer>();
                        List<String> rooms = new ArrayList<String>();
                    	if (showSection) {
                        	boolean firstSection = true;
                        	for (ExamSectionInfo section : exam.getSections()) {
                        		if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                        		classes.add(section.getName()).setNoWrap(true).setInline(false);
                        		enrollment.add(String.valueOf(section.getNrStudents())).setNoWrap(true).setInline(false);
                        		enrl.add(section.getNrStudents());
                        		if (firstSection) {
                        			seating.add(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                        				.setNoWrap(true).setInline(false);
                        			date.addItem(addDate(null, exam, section, null).setNoWrap(true).setInline(false));
                        			time.addItem(addTime(null, exam, section, null).setNoWrap(true).setInline(false));
                        			room.addItem(addRoom(null, exam, section).setNoWrap(true).setInline(false));
                        			firstSection = false;
                        		} else {
                        			seating.add("").setNoWrap(true).setInline(false);
                        			date.add("").setNoWrap(true).setInline(false);
                        			time.add("").setNoWrap(true).setInline(false);
                        			room.add("").setNoWrap(true).setInline(false);
                        		}
                        	}
                        } else {
                        	classes.add(exam.getExamName()).setNoWrap(true).setInline(false);
                    		enrollment.add(String.valueOf(exam.getNrStudents())).setNoWrap(true).setInline(false);
                    		enrl.add(exam.getNrStudents());
                			seating.add(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                				.setNoWrap(true).setInline(false);
                			date.addItem(addDate(null, exam, null, null).setNoWrap(true).setInline(false));
                			time.addItem(addTime(null, exam, null, null).setNoWrap(true).setInline(false));
                			room.addItem(addRoom(null, exam, null).setNoWrap(true).setInline(false));
                        }
                    	if (exam.getPeriod() != null) {
                    		dates.add(exam.getPeriod().getStartTime());
                    		times.add(exam.getPeriod().getStartSlot());
                    	}
                    	rooms.add(exam.getRoomsName(":"));
                        if (conflict.getOtherExam() != null) {
                        	if (showSection) {
                            	boolean firstSection = true;
                            	for (ExamSectionInfo section : conflict.getOtherExam().getSections()) {
                            		if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                            		classes.add(section.getName()).setNoWrap(true).setInline(false);
                            		enrollment.add(String.valueOf(section.getNrStudents())).setNoWrap(true).setInline(false);
                            		enrl.add(section.getNrStudents());
                            		if (firstSection) {
                            			seating.add(Exam.sSeatingTypeNormal==conflict.getOtherExam().getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                            				.setNoWrap(true).setInline(false);
                            			date.addItem(addDate(null, conflict.getOtherExam(), section, null).setNoWrap(true).setInline(false));
                            			time.addItem(addTime(null, conflict.getOtherExam(), section, null).setNoWrap(true).setInline(false));
                            			room.addItem(addRoom(null, conflict.getOtherExam(), section).setNoWrap(true).setInline(false));
                            			firstSection = false;
                            		} else {
                            			seating.add("").setNoWrap(true).setInline(false);
                            			date.add("").setNoWrap(true).setInline(false);
                            			time.add("").setNoWrap(true).setInline(false);
                            			room.add("").setNoWrap(true).setInline(false);
                            		}
                            	}
                            } else {
                            	classes.add(conflict.getOtherExam().getExamName()).setNoWrap(true).setInline(false);
                        		enrollment.add(String.valueOf(conflict.getOtherExam().getNrStudents())).setNoWrap(true).setInline(false);
                        		enrl.add(conflict.getOtherExam().getNrStudents());
                    			seating.add(Exam.sSeatingTypeNormal==conflict.getOtherExam().getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                    				.setNoWrap(true).setInline(false);
                    			date.addItem(addDate(null, conflict.getOtherExam(), null, null).setNoWrap(true).setInline(false));
                    			time.addItem(addTime(null, conflict.getOtherExam(), null, null).setNoWrap(true).setInline(false));
                    			room.addItem(addRoom(null, conflict.getOtherExam(), null).setNoWrap(true).setInline(false));
                            }
                        	if (conflict.getOtherExam().getPeriod() != null) {
                        		dates.add(conflict.getOtherExam().getPeriod().getStartTime());
                        		times.add(conflict.getOtherExam().getPeriod().getStartSlot());
                        	}
                        	rooms.add(conflict.getOtherExam().getRoomsName(":"));
                        } else if (conflict.getOtherEventId()!=null) {
                        	classes.add(conflict.getOtherEventName()).setNoWrap(true).setInline(false);
                    		enrollment.add(String.valueOf(conflict.getOtherEventSize())).setNoWrap(true).setInline(false);
                    		enrl.add(conflict.getOtherEventSize());
                			seating.add(conflict.isOtherClass() ? XMSG.typeClass() : XMSG.typeEvent())
                				.setNoWrap(true).setInline(false);
                			date.add(conflict.getOtherEventDate()).setNoWrap(true).setInline(false);
                			time.add(conflict.getOtherEventTime()).setNoWrap(true).setInline(false);
                			room.add(conflict.getOtherEventRoom()).setNoWrap(true).setInline(false);
                        }
                        enrollment.setComparable(enrl.toArray(new Integer[enrl.size()]));
                        date.setComparable(dates.toArray(new Date[dates.size()]));
                        time.setComparable(times.toArray(new Integer[times.size()]));
                        room.setComparable(rooms.toArray(new String[rooms.size()]));
                    }
                }
            if (btb)
                for (BackToBackConflict conflict : (studentConf?exam.getBackToBackConflicts():exam.getInstructorBackToBackConflicts())) {
                    if (conflict.getOtherExam()!=null && exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                    for (Long studentId : conflict.getStudents()) {
                        String id = "", name = "";
                        if (studentConf) {
                            Student student = students.get(studentId);
                            id = student.getExternalUniqueId();
                            name = student.getName(getInstructorNameFormat());
                        } else {
                            DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(studentId);
                            id = instructor.getExternalUniqueId();
                            name = instructor.getName(getInstructorNameFormat());
                        }
                        if (!match(filter, id) && !match(filter, name)) continue;
                    	LineInterface line = ret.addLine();
                    	line.addCell(id);
                    	line.addCell(name);
                    	line.addCell(XMSG.conflictBackToBack()).setColor(PreferenceLevel.prolog2color("1"))
                    		.setNoWrap(true).setComparable(2, name, exam.getExamName());
                    	CellInterface classes = line.addCell();
                    	CellInterface enrollment = line.addCell().setTextAlignment(Alignment.RIGHT);
                    	CellInterface seating = line.addCell().setTextAlignment(Alignment.CENTER);
                    	CellInterface date = line.addCell();
                    	CellInterface time = line.addCell();
                    	CellInterface room = line.addCell();
                    	List<Integer> enrl = new ArrayList<Integer>();
                        List<Date> dates = new ArrayList<Date>();
                        List<Integer> times = new ArrayList<Integer>();
                        List<String> rooms = new ArrayList<String>();
                    	if (showSection) {
                        	boolean firstSection = true;
                        	for (ExamSectionInfo section : exam.getSections()) {
                        		if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                        		classes.add(section.getName()).setNoWrap(true).setInline(false);
                        		enrollment.add(String.valueOf(section.getNrStudents())).setNoWrap(true).setInline(false);
                        		enrl.add(section.getNrStudents());
                        		if (firstSection) {
                        			seating.add(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                        				.setNoWrap(true).setInline(false);
                        			date.addItem(addDate(null, exam, section, null).setNoWrap(true).setInline(false));
                        			time.addItem(addTime(null, exam, section, null).setNoWrap(true).setInline(false));
                        			room.addItem(addRoom(null, exam, section).setNoWrap(true).setInline(false));
                        			firstSection = false;
                        		} else {
                        			seating.add("").setNoWrap(true).setInline(false);
                        			date.add("").setNoWrap(true).setInline(false);
                        			time.add("").setNoWrap(true).setInline(false);
                        			room.add("").setNoWrap(true).setInline(false);
                        		}
                        	}
                        } else {
                        	classes.add(exam.getExamName()).setNoWrap(true).setInline(false);
                    		enrollment.add(String.valueOf(exam.getNrStudents())).setNoWrap(true).setInline(false);
                    		enrl.add(exam.getNrStudents());
                			seating.add(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                				.setNoWrap(true).setInline(false);
                			date.addItem(addDate(null, exam, null, null).setNoWrap(true).setInline(false));
                			time.addItem(addTime(null, exam, null, null).setNoWrap(true).setInline(false));
                			room.addItem(addRoom(null, exam, null).setNoWrap(true).setInline(false));
                        }
                    	if (exam.getPeriod() != null) {
                    		dates.add(exam.getPeriod().getStartTime());
                    		times.add(exam.getPeriod().getStartSlot());
                    	}
                    	rooms.add(exam.getRoomsName(":"));
                        if (conflict.getOtherExam() != null) {
                        	if (showSection) {
                            	boolean firstSection = true;
                            	for (ExamSectionInfo section : conflict.getOtherExam().getSections()) {
                            		if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                            		classes.add(section.getName()).setNoWrap(true).setInline(false);
                            		enrollment.add(String.valueOf(section.getNrStudents())).setNoWrap(true).setInline(false);
                            		enrl.add(section.getNrStudents());
                            		if (firstSection) {
                            			seating.add(Exam.sSeatingTypeNormal==conflict.getOtherExam().getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                            				.setNoWrap(true).setInline(false);
                            			date.addItem(addDate(null, conflict.getOtherExam(), section, null).setNoWrap(true).setInline(false));
                            			time.addItem(addTime(null, conflict.getOtherExam(), section, null).setNoWrap(true).setInline(false));
                            			room.addItem(addRoom(null, conflict.getOtherExam(), section).setNoWrap(true).setInline(false));
                            			firstSection = false;
                            		} else {
                            			seating.add("").setNoWrap(true).setInline(false);
                            			date.add("").setNoWrap(true).setInline(false);
                            			time.add("").setNoWrap(true).setInline(false);
                            			room.add("").setNoWrap(true).setInline(false);
                            		}
                            	}
                            } else {
                            	classes.add(conflict.getOtherExam().getExamName()).setNoWrap(true).setInline(false);
                        		enrollment.add(String.valueOf(conflict.getOtherExam().getNrStudents())).setNoWrap(true).setInline(false);
                        		enrl.add(conflict.getOtherExam().getNrStudents());
                    			seating.add(Exam.sSeatingTypeNormal==conflict.getOtherExam().getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                    				.setNoWrap(true).setInline(false);
                    			date.addItem(addDate(null, conflict.getOtherExam(), null, null).setNoWrap(true).setInline(false));
                    			time.addItem(addTime(null, conflict.getOtherExam(), null, null).setNoWrap(true).setInline(false));
                    			room.addItem(addRoom(null, conflict.getOtherExam(), null).setNoWrap(true).setInline(false));
                            }
                        	if (conflict.getOtherExam().getPeriod() != null) {
                        		dates.add(conflict.getOtherExam().getPeriod().getStartTime());
                        		times.add(conflict.getOtherExam().getPeriod().getStartSlot());
                        	}
                        	rooms.add(conflict.getOtherExam().getRoomsName(":"));
                        }
                        enrollment.setComparable(enrl.toArray(new Integer[enrl.size()]));
                        date.setComparable(dates.toArray(new Date[dates.size()]));
                        time.setComparable(times.toArray(new Integer[times.size()]));
                        room.setComparable(rooms.toArray(new String[rooms.size()]));
                        line.addCell(conflict.getDistance() < 0.001 ? "" : df1.format(conflict.getDistance()) + " m")
                        	.setTextAlignment(Alignment.RIGHT)
                        	.setComparable(-conflict.getDistance());
                    }
                }
            if (m2d)
            	conflicts: for (MoreThanTwoADayConflict conflict : (studentConf?exam.getMoreThanTwoADaysConflicts():exam.getInstructorMoreThanTwoADaysConflicts())) {
                    for (ExamAssignment other : conflict.getOtherExams())
                        if (exam.compareTo(other)>=0 && exams.contains(other)) continue conflicts;
                    for (Long studentId : conflict.getStudents()) {
                        String id = "", name = "";
                        if (studentConf) {
                            Student student = students.get(studentId);
                            id = student.getExternalUniqueId();
                            name = student.getName(getInstructorNameFormat());
                        } else {
                            DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(studentId);
                            id = instructor.getExternalUniqueId();
                            name = instructor.getName(getInstructorNameFormat());
                        }
                        if (!match(filter, id) && !match(filter, name)) continue;
                    	LineInterface line = ret.addLine();
                    	line.addCell(id);
                    	line.addCell(name);
                    	line.addCell(XMSG.conflictMoreThanTwoADay()).setColor(PreferenceLevel.prolog2color("2"))
                    		.setNoWrap(true).setComparable(1, name, exam.getExamName());
                    	CellInterface classes = line.addCell();
                    	CellInterface enrollment = line.addCell().setTextAlignment(Alignment.RIGHT);
                    	CellInterface seating = line.addCell().setTextAlignment(Alignment.CENTER);
                    	CellInterface date = line.addCell();
                    	CellInterface time = line.addCell();
                    	CellInterface room = line.addCell();
                    	List<Integer> enrl = new ArrayList<Integer>();
                        List<Date> dates = new ArrayList<Date>();
                        List<Integer> times = new ArrayList<Integer>();
                        List<String> rooms = new ArrayList<String>();
                    	if (showSection) {
                        	boolean firstSection = true;
                        	for (ExamSectionInfo section : exam.getSections()) {
                        		if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                        		classes.add(section.getName()).setNoWrap(true).setInline(false);
                        		enrollment.add(String.valueOf(section.getNrStudents())).setNoWrap(true).setInline(false);
                        		enrl.add(section.getNrStudents());
                        		if (firstSection) {
                        			seating.add(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                        				.setNoWrap(true).setInline(false);
                        			date.addItem(addDate(null, exam, section, null).setNoWrap(true).setInline(false));
                        			time.addItem(addTime(null, exam, section, null).setNoWrap(true).setInline(false));
                        			room.addItem(addRoom(null, exam, section).setNoWrap(true).setInline(false));
                        			firstSection = false;
                        		} else {
                        			seating.add("").setNoWrap(true).setInline(false);
                        			date.add("").setNoWrap(true).setInline(false);
                        			time.add("").setNoWrap(true).setInline(false);
                        			room.add("").setNoWrap(true).setInline(false);
                        		}
                        	}
                        } else {
                        	classes.add(exam.getExamName()).setNoWrap(true).setInline(false);
                    		enrollment.add(String.valueOf(exam.getNrStudents())).setNoWrap(true).setInline(false);
                    		enrl.add(exam.getNrStudents());
                			seating.add(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                				.setNoWrap(true).setInline(false);
                			date.addItem(addDate(null, exam, null, null).setNoWrap(true).setInline(false));
                			time.addItem(addTime(null, exam, null, null).setNoWrap(true).setInline(false));
                			room.addItem(addRoom(null, exam, null).setNoWrap(true).setInline(false));
                        }
                    	if (exam.getPeriod() != null) {
                    		dates.add(exam.getPeriod().getStartTime());
                    		times.add(exam.getPeriod().getStartSlot());
                    	}
                    	rooms.add(exam.getRoomsName(":"));
                    	for (ExamAssignment other : conflict.getOtherExams()) {
                    		if (showSection) {
                            	boolean firstSection = true;
                            	for (ExamSectionInfo section : other.getSections()) {
                            		if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                            		classes.add(section.getName()).setNoWrap(true).setInline(false);
                            		enrollment.add(String.valueOf(section.getNrStudents())).setNoWrap(true).setInline(false);
                            		enrl.add(section.getNrStudents());
                            		if (firstSection) {
                            			seating.add(Exam.sSeatingTypeNormal==other.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                            				.setNoWrap(true).setInline(false);
                            			date.addItem(addDate(null, other, section, null).setNoWrap(true).setInline(false));
                            			time.addItem(addTime(null, other, section, null).setNoWrap(true).setInline(false));
                            			room.addItem(addRoom(null, other, section).setNoWrap(true).setInline(false));
                            			firstSection = false;
                            		} else {
                            			seating.add("").setNoWrap(true).setInline(false);
                            			date.add("").setNoWrap(true).setInline(false);
                            			time.add("").setNoWrap(true).setInline(false);
                            			room.add("").setNoWrap(true).setInline(false);
                            		}
                            	}
                            } else {
                            	classes.add(other.getExamName()).setNoWrap(true).setInline(false);
                        		enrollment.add(String.valueOf(other.getNrStudents())).setNoWrap(true).setInline(false);
                        		enrl.add(other.getNrStudents());
                    			seating.add(Exam.sSeatingTypeNormal==other.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                    				.setNoWrap(true).setInline(false);
                    			date.addItem(addDate(null, other, null, null).setNoWrap(true).setInline(false));
                    			time.addItem(addTime(null, other, null, null).setNoWrap(true).setInline(false));
                    			room.addItem(addRoom(null, other, null).setNoWrap(true).setInline(false));
                            }
                        	if (other.getPeriod() != null) {
                        		dates.add(other.getPeriod().getStartTime());
                        		times.add(other.getPeriod().getStartSlot());
                        	}
                        	rooms.add(other.getRoomsName(":"));
                        }
                        enrollment.setComparable(enrl.toArray(new Integer[enrl.size()]));
                        date.setComparable(dates.toArray(new Date[dates.size()]));
                        time.setComparable(times.toArray(new Integer[times.size()]));
                        room.setComparable(rooms.toArray(new String[rooms.size()]));
                    }
                }
        }
        
        return ret;
	}
	
	public TableInterface generateDirectConflictsReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter, boolean studentConf) {
		TableInterface ret = new TableInterface();
		
		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		
		LineInterface header = ret.addHeader();
        header.addCell(XMSG.col1stExam() + " " + (showSection ? XMSG.colOwner() : XMSG.colExamination()));
        header.addCell(XMSG.col1stExam() + " " + XMSG.colEnrollment()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.col1stExam() + " " + XMSG.colSeatingType()).setTextAlignment(Alignment.CENTER);
        header.addCell(XMSG.col2ndExam() + " " + (showSection ? XMSG.colOwner() : XMSG.colExamination()));
        header.addCell(XMSG.col2ndExam() + " " + XMSG.colEnrollment()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.col2ndExam() + " " + XMSG.colSeatingType()).setTextAlignment(Alignment.CENTER);
        header.addCell(XMSG.colDate());
        header.addCell(XMSG.colTime());
        header.addCell(XMSG.conflictDirect()).setTextAlignment(Alignment.RIGHT);
        if (studentConf) header.addCell(XMSG.colDirectPercent()).setTextAlignment(Alignment.RIGHT);
        
        DecimalFormat df = new DecimalFormat("0.0");
        for (ExamAssignmentInfo exam : exams) {
        	if (!match(filter, exam)) continue;
        	for (DirectConflict conflict : (studentConf?exam.getDirectConflicts():exam.getInstructorDirectConflicts())) {
        		if (match(filter, conflict.getOtherExam()) && exam.compareTo(conflict.getOtherExam())>=0) continue;
        		ExamAssignment other = conflict.getOtherExam();
        		if (showSection) {
                	for (ExamSectionInfo section : exam.getSections()) {
                        for (ExamSectionInfo section1 : exam.getSections()) {
                            if (other!=null) {
                                for (ExamSectionInfo section2 : conflict.getOtherExam().getSections()) {
                                    if (!match(filter, section1.getName()) && !match(filter, section2.getName())) continue;
                                    int nrStudents = 0;
                                    if (studentConf) for (Long studentId : section1.getStudentIds()) {
                                        if (section2.getStudentIds().contains(studentId)) nrStudents++;
                                    } else nrStudents = conflict.getNrStudents();
                                    if (nrStudents==0) continue;
                                    
                                    LineInterface line = ret.addLine();
                                    line.addCell(section1.getName())
                                    	.setComparable(section1.getName(), section2.getName());
                                    line.addCell(String.valueOf(section1.getNrStudents()))
                                    	.setComparable(-section1.getNrStudents(), -section2.getNrStudents(), section1.getName(), section2.getName())
                                    	.setTextAlignment(Alignment.RIGHT);
                                    line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                                		.setComparable(exam.getSeatingType(), other.getSeatingTypeLabel(), section1.getName(), section2.getName())
                                		.setTextAlignment(Alignment.CENTER);
                                    line.addCell(section2.getName())
                                		.setComparable(section2.getName(), section1.getName());
                                    line.addCell(String.valueOf(section2.getNrStudents()))
                                		.setComparable(-section2.getNrStudents(), -section1.getNrStudents(), section2.getName(), section1.getName())
                                		.setTextAlignment(Alignment.RIGHT);
                                    line.addCell(Exam.sSeatingTypeNormal==other.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                            			.setComparable(other.getSeatingType(), exam.getSeatingTypeLabel(), section2.getName(), section1.getName())
                            			.setTextAlignment(Alignment.CENTER);
                                    addDate(line, exam, section, null);
                                    addTime(line, exam, section, null);
                                    line.addCell(String.valueOf(nrStudents))
                                		.setComparable(-nrStudents, section1.getName(), section2.getName())
                                		.setTextAlignment(Alignment.RIGHT);
                                    if (studentConf) line.addCell(df.format(100.0*nrStudents/Math.min(section1.getNrStudents(), section2.getNrStudents())))
                            			.setComparable(-100.0*nrStudents/Math.min(section1.getNrStudents(), section2.getNrStudents()), section1.getName(), section2.getName())
                            			.setTextAlignment(Alignment.RIGHT);
                                    if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                                    	line.setURL("examDetail.action?examId=" + exam.getExamId());
                                    line.setId(exam.getExamId());
                                }
                            } else if (conflict.getOtherEventId()!=null) {
                            	if (!match(filter, section1.getName())) continue;
                                int nrStudents = 0;
                                for (Long studentId : section1.getStudentIds())
                                    if (conflict.getStudents().contains(studentId)) nrStudents++;
                                if (nrStudents==0) continue;
                                LineInterface line = ret.addLine();
                                line.addCell(section1.getName())
                                	.setComparable(section1.getName(), conflict.getOtherEventName());
                                line.addCell(String.valueOf(section1.getNrStudents()))
                                	.setComparable(-section1.getNrStudents(), -conflict.getOtherEventSize(), section1.getName(), conflict.getOtherEventName())
                                	.setTextAlignment(Alignment.RIGHT);
                                line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                            		.setComparable(exam.getSeatingType(), -1, section1.getName(), conflict.getOtherEventName())
                            		.setTextAlignment(Alignment.CENTER);
                                line.addCell(conflict.getOtherEventName())
                            		.setComparable(conflict.getOtherEventName(), section1.getName());
                                line.addCell(String.valueOf(conflict.getOtherEventSize()))
                            		.setComparable(-conflict.getOtherEventSize(), -section1.getNrStudents(), conflict.getOtherEventName(), section1.getName())
                            		.setTextAlignment(Alignment.RIGHT);
                                line.addCell((conflict.isOtherClass() ? XMSG.typeClass() : XMSG.typeEvent()))
                        			.setComparable(-1, exam.getSeatingTypeLabel(), conflict.getOtherEventName(), section1.getName())
                        			.setTextAlignment(Alignment.CENTER);
                                addDate(line, exam, section, null);
                                addTime(line, exam, section, null);
                                line.addCell(String.valueOf(nrStudents))
                            		.setComparable(-nrStudents, section1.getName(), conflict.getOtherEventName())
                            		.setTextAlignment(Alignment.RIGHT);
                                if (studentConf) line.addCell(df.format(100.0*nrStudents/Math.min(section1.getNrStudents(), conflict.getOtherEventSize())))
                                	.setComparable(-100.0*nrStudents/Math.min(section1.getNrStudents(), conflict.getOtherEventSize()), section1.getName(), conflict.getOtherEventName())
                        			.setTextAlignment(Alignment.RIGHT);
                                if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                                	line.setURL("examDetail.action?examId=" + exam.getExamId());
                                line.setId(exam.getExamId());
                            }
                        }
                	}
        		} else {
        			if (other != null) {
                        LineInterface line = ret.addLine();
                        line.addCell(exam.getExamName())
                        	.setComparable(exam.getExamName(), other.getExamName());
                        line.addCell(String.valueOf(exam.getNrStudents()))
                        	.setComparable(-exam.getNrStudents(), -other.getNrStudents(), exam.getExamName(), other.getExamName())
                        	.setTextAlignment(Alignment.RIGHT);
                        line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                    		.setComparable(exam.getSeatingType(), other.getSeatingTypeLabel(), exam.getExamName(), other.getExamName())
                    		.setTextAlignment(Alignment.CENTER);
                        line.addCell(other.getExamName())
                    		.setComparable(other.getExamName(), exam.getExamName());
                        line.addCell(String.valueOf(other.getNrStudents()))
                    		.setComparable(-other.getNrStudents(), -exam.getNrStudents(), other.getExamName(), exam.getExamName())
                    		.setTextAlignment(Alignment.RIGHT);
                        line.addCell(Exam.sSeatingTypeNormal==other.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                			.setComparable(other.getSeatingType(), exam.getSeatingTypeLabel(), other.getExamName(), exam.getExamName())
                			.setTextAlignment(Alignment.CENTER);
                        addDate(line, exam, null, null);
                        addTime(line, exam, null, null);
                        line.addCell(String.valueOf(conflict.getNrStudents()))
                    		.setComparable(-conflict.getNrStudents(), exam.getExamName(), other.getExamName())
                    		.setTextAlignment(Alignment.RIGHT);
                        if (studentConf) line.addCell(df.format(100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), other.getNrStudents())))
                			.setComparable(-100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), other.getNrStudents()), exam.getExamName(), other.getExamName())
                			.setTextAlignment(Alignment.RIGHT);
                        if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                        	line.setURL("examDetail.action?examId=" + exam.getExamId());
                        line.setId(exam.getExamId());
        			} else if (conflict.getOtherEventId()!=null) {
        				LineInterface line = ret.addLine();
                        line.addCell(exam.getExamName())
                        	.setComparable(exam.getExamName(), conflict.getOtherEventName());
                        line.addCell(String.valueOf(exam.getNrStudents()))
                        	.setComparable(-exam.getNrStudents(), -conflict.getOtherEventSize(), exam.getExamName(), conflict.getOtherEventName())
                        	.setTextAlignment(Alignment.RIGHT);
                        line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                    		.setComparable(exam.getSeatingType(), -1, exam.getExamName(), conflict.getOtherEventName())
                    		.setTextAlignment(Alignment.CENTER);
                        line.addCell(conflict.getOtherEventName())
                    		.setComparable(conflict.getOtherEventName(), exam.getExamName());
                        line.addCell(String.valueOf(conflict.getOtherEventSize()))
                    		.setComparable(-conflict.getOtherEventSize(), -exam.getNrStudents(), conflict.getOtherEventName(), exam.getExamName())
                    		.setTextAlignment(Alignment.RIGHT);
                        line.addCell((conflict.isOtherClass() ? XMSG.typeClass() : XMSG.typeEvent()))
                			.setComparable(-1, exam.getSeatingTypeLabel(), conflict.getOtherEventName(), exam.getExamName())
                			.setTextAlignment(Alignment.CENTER);
                        addDate(line, exam, null, null);
                        addTime(line, exam, null, null);
                        line.addCell(String.valueOf(conflict.getNrStudents()))
                    		.setComparable(-conflict.getNrStudents(), exam.getExamName(), conflict.getOtherEventName())
                    		.setTextAlignment(Alignment.RIGHT);
                        if (studentConf) line.addCell(df.format(100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), conflict.getOtherEventSize())))
                			.setComparable(-100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), conflict.getOtherEventSize()), exam.getExamName(), conflict.getOtherEventName())
                			.setTextAlignment(Alignment.RIGHT);
                        if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                        	line.setURL("examDetail.action?examId=" + exam.getExamId());
                        line.setId(exam.getExamId());
        			}
        		}
        	}
        }
        
        return ret;
	}
	
	public TableInterface generateBackToBackConflictsReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter, boolean studentConf) {
		TableInterface ret = new TableInterface();
		
		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		
		LineInterface header = ret.addHeader();
        header.addCell(XMSG.col1stExam() + " " + (showSection ? XMSG.colOwner() : XMSG.colExamination()));
        header.addCell(XMSG.col1stExam() + " " + XMSG.colEnrollment()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.col1stExam() + " " + XMSG.colSeatingType()).setTextAlignment(Alignment.CENTER);
        header.addCell(XMSG.col2ndExam() + " " + (showSection ? XMSG.colOwner() : XMSG.colExamination()));
        header.addCell(XMSG.col2ndExam() + " " + XMSG.colEnrollment()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.col2ndExam() + " " + XMSG.colSeatingType()).setTextAlignment(Alignment.CENTER);
        header.addCell(XMSG.colDate());
        header.addCell(XMSG.colTime());
        header.addCell(XMSG.conflictBackToBack()).setTextAlignment(Alignment.RIGHT);
        if (studentConf) header.addCell(XMSG.colBackToBackPercent()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colDistanceMeters()).setTextAlignment(Alignment.RIGHT);
        
        DecimalFormat df = new DecimalFormat("#,##0.0");
        for (ExamAssignmentInfo exam : exams) {
        	if (!match(filter, exam)) continue;
        	for (BackToBackConflict conflict : (studentConf?exam.getBackToBackConflicts():exam.getInstructorBackToBackConflicts())) {
        		if (match(filter, conflict.getOtherExam()) && exam.compareTo(conflict.getOtherExam())>=0) continue;
        		ExamAssignment other = conflict.getOtherExam();
        		if (showSection) {
                	for (ExamSectionInfo section : exam.getSections()) {
                        for (ExamSectionInfo section1 : exam.getSections()) {
                            if (other!=null) {
                                for (ExamSectionInfo section2 : conflict.getOtherExam().getSections()) {
                                    if (!match(filter, section1.getName()) && !match(filter, section2.getName())) continue;
                                    int nrStudents = 0;
                                    if (studentConf) for (Long studentId : section1.getStudentIds()) {
                                        if (section2.getStudentIds().contains(studentId)) nrStudents++;
                                    } else nrStudents = conflict.getNrStudents();
                                    if (nrStudents==0) continue;
                                    
                                    LineInterface line = ret.addLine();
                                    line.addCell(section1.getName())
                                    	.setComparable(section1.getName(), section2.getName());
                                    line.addCell(String.valueOf(section1.getNrStudents()))
                                    	.setComparable(-section1.getNrStudents(), -section2.getNrStudents(), section1.getName(), section2.getName())
                                    	.setTextAlignment(Alignment.RIGHT);
                                    line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                                		.setComparable(exam.getSeatingType(), other.getSeatingTypeLabel(), section1.getName(), section2.getName())
                                		.setTextAlignment(Alignment.CENTER);
                                    line.addCell(section2.getName())
                                		.setComparable(section2.getName(), section1.getName());
                                    line.addCell(String.valueOf(section2.getNrStudents()))
                                		.setComparable(-section2.getNrStudents(), -section1.getNrStudents(), section2.getName(), section1.getName())
                                		.setTextAlignment(Alignment.RIGHT);
                                    line.addCell(Exam.sSeatingTypeNormal==other.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                            			.setComparable(other.getSeatingType(), exam.getSeatingTypeLabel(), section2.getName(), section1.getName())
                            			.setTextAlignment(Alignment.CENTER);
                                    addDate(line, exam, section, null);
                                    addTime(line, exam, section, null);
                                    line.addCell(String.valueOf(nrStudents))
                                		.setComparable(-nrStudents, section1.getName(), section2.getName())
                                		.setTextAlignment(Alignment.RIGHT);
                                    if (studentConf) line.addCell(df.format(100.0*nrStudents/Math.min(section1.getNrStudents(), section2.getNrStudents())))
                            			.setComparable(-100.0*nrStudents/Math.min(section1.getNrStudents(), section2.getNrStudents()), section1.getName(), section2.getName())
                            			.setTextAlignment(Alignment.RIGHT);
                                    line.addCell(conflict.getDistance() < 0.001 ? "" : df.format(conflict.getDistance()))
                                    	.setComparable(-conflict.getDistance(), section1.getName(), section2.getName())
                                    	.setTextAlignment(Alignment.RIGHT);
                                    if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                                    	line.setURL("examDetail.action?examId=" + exam.getExamId());
                                    line.setId(exam.getExamId());
                                }
                            }
                        }
                	}
        		} else {
        			if (other != null) {
                        LineInterface line = ret.addLine();
                        line.addCell(exam.getExamName())
                        	.setComparable(exam.getExamName(), other.getExamName());
                        line.addCell(String.valueOf(exam.getNrStudents()))
                        	.setComparable(-exam.getNrStudents(), -other.getNrStudents(), exam.getExamName(), other.getExamName())
                        	.setTextAlignment(Alignment.RIGHT);
                        line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                    		.setComparable(exam.getSeatingType(), other.getSeatingTypeLabel(), exam.getExamName(), other.getExamName())
                    		.setTextAlignment(Alignment.CENTER);
                        line.addCell(other.getExamName())
                    		.setComparable(other.getExamName(), exam.getExamName());
                        line.addCell(String.valueOf(other.getNrStudents()))
                    		.setComparable(-other.getNrStudents(), -exam.getNrStudents(), other.getExamName(), exam.getExamName())
                    		.setTextAlignment(Alignment.RIGHT);
                        line.addCell(Exam.sSeatingTypeNormal==other.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
                			.setComparable(other.getSeatingType(), exam.getSeatingTypeLabel(), other.getExamName(), exam.getExamName())
                			.setTextAlignment(Alignment.CENTER);
                        addDate(line, exam, null, null);
                        addTime(line, exam, null, null);
                        line.addCell(String.valueOf(conflict.getNrStudents()))
                    		.setComparable(-conflict.getNrStudents(), exam.getExamName(), other.getExamName())
                    		.setTextAlignment(Alignment.RIGHT);
                        if (studentConf) line.addCell(df.format(100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), other.getNrStudents())))
                			.setComparable(-100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), other.getNrStudents()), exam.getExamName(), other.getExamName())
                			.setTextAlignment(Alignment.RIGHT);
                        line.addCell(conflict.getDistance() < 0.001 ? "" : df.format(conflict.getDistance()))
                    		.setComparable(-conflict.getDistance(), exam.getExamName(), other.getExamName())
                    		.setTextAlignment(Alignment.RIGHT);
                        if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                        	line.setURL("examDetail.action?examId=" + exam.getExamId());
                        line.setId(exam.getExamId());
        			}
        		}
        	}
        }
        
        return ret;
	}
	
	public TableInterface generate2MoreADayConflictsReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter, boolean studentConf) {
		TableInterface ret = new TableInterface();
        int max = 0;
        for (ExamAssignmentInfo exam : exams) {
            if (!match(filter, exam)) continue;
            conflicts: for (MoreThanTwoADayConflict conflict : (studentConf?exam.getMoreThanTwoADaysConflicts():exam.getInstructorMoreThanTwoADaysConflicts())) {
                for (ExamAssignment other : conflict.getOtherExams()) 
                    if (match(filter, other) && exam.compareTo(other)>=0) continue conflicts;
                max = Math.max(max,conflict.getOtherExams().size()+1);
            }
        }
		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		
		LineInterface header = ret.addHeader();
        header.addCell(XMSG.colDate());
        for (int i = 0; i<Math.max(2, max); i++) {
        	String name = (i == 0 ? XMSG.col1stExam() : i == 1 ? XMSG.col2ndExam() : XMSG.colNthExam(1 + i));
        	header.addCell(name + " " + (showSection ? XMSG.colOwner() : XMSG.colExamination()));
        	header.addCell(name + " " + XMSG.colEnrollment()).setTextAlignment(Alignment.RIGHT);
            header.addCell(name + " " + XMSG.colTime());
        }
        header.addCell(XMSG.conflictMoreThanTwoADay()).setTextAlignment(Alignment.RIGHT);
        if (studentConf) header.addCell(XMSG.colMoreThanTwoADayPercent()).setTextAlignment(Alignment.RIGHT);
     
        if (max<=2) return ret;

        DecimalFormat df = new DecimalFormat("#,##0.0");
        for (ExamAssignmentInfo exam : exams) {
        	if (!match(filter, exam)) continue;
        	conflicts: for (MoreThanTwoADayConflict conflict : (studentConf?exam.getMoreThanTwoADaysConflicts():exam.getInstructorMoreThanTwoADaysConflicts())) {
                for (ExamAssignment other : conflict.getOtherExams())
                    if (match(filter, other) && exam.compareTo(other)>=0) continue conflicts;
                List<ExamAssignment> examsThisConf = new ArrayList<ExamAssignment>(max);
                examsThisConf.add(exam);
                examsThisConf.addAll(conflict.getOtherExams());
                Collections.sort(examsThisConf, new Comparator<ExamAssignment>() {
                    public int compare(ExamAssignment a1, ExamAssignment a2) {
                        return a1.compareTo(a2);
                    }
                });
                if (showSection) {
                	m2dReportAddLines(filter, ret, studentConf, max, examsThisConf, new ArrayList<ExamSectionInfo>(), null, false);
                } else {
                    LineInterface line = ret.addLine();
                    addDate(line, exam, null, null);
                    int minStudents = exam.getNrStudents();
                    int idx = 0;
                    for (ExamAssignment x: examsThisConf) {
                        line.addCell(exam.getExamName())
                    		.setComparable(exam.getExamName());
                        line.addCell(String.valueOf(exam.getNrStudents()))
                    		.setComparable(-exam.getNrStudents(), exam.getExamName())
                    		.setTextAlignment(Alignment.RIGHT);
                        addTime(line, exam, null, null);
                        if (x.getNrStudents() < minStudents) minStudents = x.getNrStudents();
                        idx ++;
                    }
                    while (idx < max) {
                    	line.addCell();
                    	line.addCell();
                    	line.addCell();
                    	idx ++;
                    }
                    line.addCell(String.valueOf(conflict.getNrStudents()))
                		.setComparable(-conflict.getNrStudents(), exam.getExamName())
                		.setTextAlignment(Alignment.RIGHT);
                    if (studentConf) line.addCell(df.format(100.0*conflict.getNrStudents()/minStudents))
            			.setComparable(-100.0*conflict.getNrStudents()/minStudents, exam.getExamName())
            			.setTextAlignment(Alignment.RIGHT);
                    if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                    	line.setURL("examDetail.action?examId=" + exam.getExamId());
                    line.setId(exam.getExamId());
                }

        	}
        }
        
        return ret;
	}
	
	protected void m2dReportAddLines(FilterInterface filter, TableInterface ret, boolean studentConf, int max, List<ExamAssignment> exams, List<ExamSectionInfo> sections, Set<Long> students, boolean match) {
		int idx = sections.size();
		if (sections.size() == exams.size()) {
			if (!match) return;
			// generate line
			DecimalFormat df = new DecimalFormat("#,##0.0");
			ExamAssignment exam = exams.get(0);
			LineInterface line = ret.addLine();
            addDate(line, exam, null, null);
            int minStudents = exam.getNrStudents();
            for (ExamSectionInfo section: sections) {
                line.addCell(section.getName())
            		.setComparable(section.getName())
            		.setNoWrap(true);
                line.addCell(String.valueOf(section.getNrStudents()))
            		.setComparable(-section.getNrStudents(), section.getName())
            		.setTextAlignment(Alignment.RIGHT)
            		.setNoWrap(true);
                addTime(line, exam, section, null);
                if (section.getNrStudents() < minStudents) minStudents = section.getNrStudents();
            }
            while (idx < max) {
            	line.addCell();
            	line.addCell();
            	line.addCell();
            	idx ++;
            }
            line.addCell(String.valueOf(students.size()))
        		.setComparable(-students.size(), exam.getExamName())
        		.setTextAlignment(Alignment.RIGHT);
            if (studentConf) line.addCell(df.format(100.0*students.size()/minStudents))
    			.setComparable(-100.0*students.size()/minStudents, exam.getExamName())
    			.setTextAlignment(Alignment.RIGHT);
            if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
            	line.setURL("examDetail.action?examId=" + exam.getExamId());
            line.setId(exam.getExamId());
			return;
		}
		ExamAssignment exam = exams.get(idx);
		for (ExamSectionInfo section: exam.getSections()) {
			Set<Long> newStudents = null;
			if (students == null) {
				if (studentConf)
					newStudents = new HashSet<Long>(section.getStudentIds());
				else {
					newStudents = new HashSet<Long>();
					for (ExamInstructorInfo i: section.getExam().getInstructors()) newStudents.add(i.getId());
				}
			} else {
				newStudents = new HashSet<Long>();
                for (Long studentId : students) {
                    if (studentConf && section.getStudentIds().contains(studentId)) newStudents.add(studentId);
                    if (!studentConf && section.getExam().hasInstructor(studentId)) newStudents.add(studentId);
                }
			}
			if (newStudents.isEmpty()) continue;
			sections.add(section);
			m2dReportAddLines(filter, ret, studentConf, max, exams,
					sections,
					newStudents,
					match || match(filter, section.getName()));
			sections.remove(idx);
		}
	}
	
	public TableInterface generateNrExamsADayReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter) {
		TableInterface ret = new TableInterface();
		
		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		String textFilter = filter.getParameterValue("filter");
		
		LineInterface header = ret.addHeader();
        header.addCell(XMSG.colDate());
        header.addCell(XMSG.colStudentsWithNoExam()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colStudentsWithOneExam()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colStudentsWithTwoExams()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colStudentsWithThreeExams()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colStudentsWithFourOrMoreExams()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colStudentBTBExams()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colStudentDistanceBTBExams()).setTextAlignment(Alignment.RIGHT);
        
        HashSet<Long> studentIds = new HashSet<Long>();
        Hashtable<Date,Hashtable<Long,Integer>> date2students = new Hashtable();
        Hashtable<Date,Integer> date2btb = new Hashtable();
        Hashtable<Date,Integer> date2dbtb = new Hashtable();
        for (ExamAssignmentInfo exam : exams) {
            if (!showSection && !match(filter, exam.getExamName())) continue;
            Hashtable<Long,Integer> students = date2students.get(exam.getPeriod().getStartDate());
            if (students==null) {
                students = new Hashtable<Long, Integer>(); date2students.put(exam.getPeriod().getStartDate(),students);
            }
            for (ExamSectionInfo section : exam.getSections()) {
                if (showSection && !match(filter, section.getName())) continue;
                studentIds.addAll(section.getStudentIds());
                for (Long studentId : section.getStudentIds()) {
                    Integer nrExamsThisDay = students.get(studentId);
                    students.put(studentId, 1+(nrExamsThisDay==null?0:nrExamsThisDay));
                }
                int btb = 0, dbtb = 0;
                for (Iterator i=exam.getBackToBackConflicts().iterator();i.hasNext();) {
                    BackToBackConflict conf = (BackToBackConflict)i.next();
                    if (exam.getPeriod().compareTo(conf.getOtherExam().getPeriod())>=0) continue;
                    if (showSection && textFilter != null && !textFilter.trim().isEmpty()) {
                        for (Enumeration e=conf.getStudents().elements();e.hasMoreElements();) {
                            Long studentId = (Long)e.nextElement();
                            if (section.getStudentIds().contains(studentId)) {
                                btb++;
                                if (conf.isDistance()) dbtb++;
                            }
                        }
                    } else {
                        btb += conf.getNrStudents();
                        if (conf.isDistance()) dbtb += conf.getNrStudents(); 
                    }
                }
                if (btb>0)
                    date2btb.put(exam.getPeriod().getStartDate(), btb + (date2btb.get(exam.getPeriod().getStartDate())==null?0:date2btb.get(exam.getPeriod().getStartDate())));
                if (dbtb>0)
                    date2dbtb.put(exam.getPeriod().getStartDate(), dbtb + (date2dbtb.get(exam.getPeriod().getStartDate())==null?0:date2dbtb.get(exam.getPeriod().getStartDate())));
            }
        }
        Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD);
        DecimalFormat df1 = new DecimalFormat("#,##0");
        int tNoExam = 0, tOneExam = 0, tTwoExams = 0, tThreeExams = 0, tFourExams = 0, tBtb = 0, tDistBtb = 0;
        for (Map.Entry<Date,Hashtable<Long,Integer>> entry : date2students.entrySet()) {
            int noExam = 0, oneExam = 0, twoExams = 0, threeExams = 0, fourExams = 0, btb = 0, dbtb = 0;
            for (Map.Entry<Long, Integer> student : entry.getValue().entrySet()) {
                if (student.getValue()==1) oneExam ++;
                else if (student.getValue()==2) twoExams ++;
                else if (student.getValue()==3) threeExams ++;
                else if (student.getValue()>=4) fourExams ++;
            }
            noExam = studentIds.size() - oneExam - twoExams - threeExams - fourExams;
            btb = (date2btb.get(entry.getKey())==null?0:date2btb.get(entry.getKey()));
            dbtb = (date2dbtb.get(entry.getKey())==null?0:date2dbtb.get(entry.getKey()));
            
            LineInterface line = ret.addLine();
            line.addCell(df.format(entry.getKey())).setComparable(0, entry.getKey());
            line.addCell(df1.format(noExam)).setComparable(0, noExam).setTextAlignment(Alignment.RIGHT);
            line.addCell(df1.format(oneExam)).setComparable(0, oneExam).setTextAlignment(Alignment.RIGHT);
            line.addCell(df1.format(twoExams)).setComparable(0, twoExams).setTextAlignment(Alignment.RIGHT);
            line.addCell(df1.format(threeExams)).setComparable(0, threeExams).setTextAlignment(Alignment.RIGHT);
            line.addCell(df1.format(fourExams)).setComparable(0, fourExams).setTextAlignment(Alignment.RIGHT);
            line.addCell(df1.format(btb)).setComparable(0, btb).setTextAlignment(Alignment.RIGHT);
            line.addCell(df1.format(dbtb)).setComparable(0, dbtb).setTextAlignment(Alignment.RIGHT);
            tNoExam += noExam;
            tOneExam += oneExam;
            tTwoExams += twoExams;
            tThreeExams += threeExams;
            tFourExams += fourExams;
            tBtb += btb;
            tDistBtb += dbtb;
        }
        
        LineInterface line = ret.addLine();
        line.addCell(XMSG.colTotals()).setComparable(1);
        line.addCell(df1.format(tNoExam)).setComparable(0, tNoExam).setTextAlignment(Alignment.RIGHT).addStyle("font-weight: bold;");
        line.addCell(df1.format(tOneExam)).setComparable(0, tOneExam).setTextAlignment(Alignment.RIGHT).addStyle("font-weight: bold;");
        line.addCell(df1.format(tTwoExams)).setComparable(0, tTwoExams).setTextAlignment(Alignment.RIGHT).addStyle("font-weight: bold;");
        line.addCell(df1.format(tThreeExams)).setComparable(0, tThreeExams).setTextAlignment(Alignment.RIGHT).addStyle("font-weight: bold;");
        line.addCell(df1.format(tFourExams)).setComparable(0, tFourExams).setTextAlignment(Alignment.RIGHT).addStyle("font-weight: bold;");
        line.addCell(df1.format(tBtb)).setComparable(0, tBtb).setTextAlignment(Alignment.RIGHT).addStyle("font-weight: bold;");
        line.addCell(df1.format(tDistBtb)).setComparable(0, tDistBtb).setTextAlignment(Alignment.RIGHT).addStyle("font-weight: bold;");
        
        return ret;
	}
	
	public TableInterface generateIndividualAssignmentReport(Collection<ExamAssignmentInfo> exams, FilterInterface filter, boolean student) {
		TableInterface ret = new TableInterface();

        Hashtable<Long, Student> students = new Hashtable<Long, Student>();
        if (student) {
            HashSet<Long> allStudentIds = new HashSet<Long>();
            for (ExamAssignmentInfo exam : exams)
                for (ExamSectionInfo section : exam.getSections())
                    allStudentIds.addAll(section.getStudentIds());
            String inSet = null; int idx = 0;
            for (Iterator<Long> i=allStudentIds.iterator();i.hasNext();idx++) {
                if (idx==1000) {
                    for (Student s: StudentDAO.getInstance().getSession().createQuery("select s from Student s where s.uniqueId in ("+inSet+")", Student.class).list()) {
                        students.put(s.getUniqueId(), s);
                    }
                    idx = 0; inSet = null;
                }
                if (inSet==null)
                    inSet = i.next().toString();
                else
                    inSet += ","+i.next();
            }
            if (inSet!=null) {
            	for (Student s: StudentDAO.getInstance().getSession().createQuery("select s from Student s where s.uniqueId in ("+inSet+")", Student.class).list()) {
                    students.put(s.getUniqueId(), s);
                }
            }
        }

		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		ret.setBlankWhenSame(true);
		
		LineInterface header = ret.addHeader();
        header.addCell((student ? XMSG.colStudentId() : XMSG.colInstructorId()));
        header.addCell(XMSG.colStudentOrInstructorName());
        header.addCell(showSection ? XMSG.colOwner() : XMSG.colExamination());
        header.addCell(XMSG.colDate());
        header.addCell(XMSG.colTime());
        header.addCell(XMSG.colRoom());
        if (student)
        	header.addCell(XMSG.colInstructor());
        
        for (ExamAssignmentInfo exam : exams) {
            if (showSection) {
                for (ExamSectionInfo section : exam.getSections()) {
                    if (student) {
                        for (Long studentId : section.getStudentIds()) {
                            Student s = students.get(studentId);
                            if (s==null) continue;
                            if (!match(filter, s.getExternalUniqueId()) && !match(filter, s.getName(getInstructorNameFormat()))) continue;
                            LineInterface line = ret.addLine();
                            line.setId(exam.getExamId());
                            if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                            	line.setURL("examDetail.action?examId=" + exam.getExamId());
                            line.addCell(s.getExternalUniqueId())
                            	.setComparable(s.getExternalUniqueId(), section.getName());
                            line.addCell(s.getName(getInstructorNameFormat()))
                            	.setComparable(s.getName(getInstructorNameFormat()), section.getName());
                            line.addCell(section.getName())
                        		.setComparable(section.getName(), s.getName(getInstructorNameFormat()));
                            addDate(line, exam, section, null);
                            addTime(line, exam, section, null);
                            addRoom(line, exam, section);
                            line.addCell(exam.getInstructorName("; ")).setComparable(exam.getInstructorName(":"), section.getName());
                        }
                    } else {
                    	for (ExamInstructorInfo instructor : section.getExam().getInstructors()) {
                            if (!match(filter, instructor.getExternalUniqueId()) && !match(filter, instructor.getName())) continue;
                            LineInterface line = ret.addLine();
                            line.setId(exam.getExamId());
                            if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                            	line.setURL("examDetail.action?examId=" + exam.getExamId());
                            line.addCell(instructor.getExternalUniqueId())
                            	.setComparable(instructor.getExternalUniqueId(), section.getName());
                            line.addCell(instructor.getName())
                            	.setComparable(instructor.getName(), section.getName());;
                            line.addCell(section.getName())
                            	.setComparable(section.getName(), instructor.getName());
                            addDate(line, exam, section, null);
                            addTime(line, exam, section, null);
                            addRoom(line, exam, section);
                    	}
                    }
                }
            } else {
            	if (student) {
                    for (Long studentId : exam.getStudentIds()) {
                        Student s = students.get(studentId);
                        if (s==null) continue;
                        if (!match(filter, s.getExternalUniqueId()) && !match(filter, s.getName(getInstructorNameFormat()))) continue;
                        LineInterface line = ret.addLine();
                        line.setId(exam.getExamId());
                        if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                        	line.setURL("examDetail.action?examId=" + exam.getExamId());
                        line.addCell(s.getExternalUniqueId())
                        	.setComparable(s.getExternalUniqueId(), exam.getExamName());
                        line.addCell(s.getName(getInstructorNameFormat()))
                        	.setComparable(s.getName(getInstructorNameFormat()), exam.getExamName());;
                        line.addCell(exam.getExamName())
                        	.setComparable(exam.getExamName(), s.getName(getInstructorNameFormat()));
                        addDate(line, exam, null, null);
                        addTime(line, exam, null, null);
                        addRoom(line, exam, null);
                        line.addCell(exam.getInstructorName("; ")).setComparable(exam.getInstructorName(":"), exam.getExamName());
                    }
                } else {
                	for (ExamInstructorInfo instructor : exam.getInstructors()) {
                        if (!match(filter, instructor.getExternalUniqueId()) && !match(filter, instructor.getName())) continue;
                        LineInterface line = ret.addLine();
                        line.setId(exam.getExamId());
                        if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationDetail))
                        	line.setURL("examDetail.action?examId=" + exam.getExamId());
                        line.addCell(instructor.getExternalUniqueId())
                        	.setComparable(instructor.getExternalUniqueId(), exam.getExamName());
                        line.addCell(instructor.getName())
                        	.setComparable(instructor.getName(), exam.getExamName());
                        line.addCell(exam.getExamName())
                        	.setComparable(exam.getExamName(), instructor.getName());
                        addDate(line, exam, null, null);
                        addTime(line, exam, null, null);
                        addRoom(line, exam, null);
                	}
                }
            }
        }
        
        return ret;
	}
	
}
