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
package org.unitime.timetable.export.students;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusPage.ChangeLogComparator;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusPage.EnrollmentComparator;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusPage.StudentComparator;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.EnrollmentInfo;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.SectioningAction;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.StudentInfo;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:student-dashboard.csv")
public class StudentSchedulingDashboardExportCSV implements Exporter {
	protected static StudentSectioningMessages MESSAGES = Localization.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	protected static DecimalFormat sCreditFormat = new DecimalFormat("0.#");
	
	@Autowired private ApplicationContext applicationContext;

	@Override
	public String reference() {
		return "student-dashboard.csv";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		boolean online = "1".equals(helper.getParameter("online"));
		int tab = 0;
		try { tab = Integer.parseInt(helper.getParameter("tab")); } catch (Exception e) {}
		String query = helper.getParameter("query");
		int sort = 0;
		try { sort = Integer.parseInt(helper.getParameter("sort")); } catch (Exception e) {}
		
		SectioningStatusFilterRpcRequest filter = new SectioningStatusFilterRpcRequest();
    	// filter.setSessionId(helper.getAcademicSessionId());
    	for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
    		String command = e.nextElement();
    		if (command.equals("f:text")) {
    			filter.setText(helper.getParameter("e:text"));
    		} else if (command.startsWith("f:")) {
    			for (String value: helper.getParameterValues(command))
    				filter.addOption(command.substring(2), value);
    		}
    	}
    	
    	SectioningService service = (SectioningService)applicationContext.getBean("sectioning.gwt");
    	if (tab == 0) {
    		Set<Long> courseIds = new HashSet<Long>();
    		if (helper.getParameter("c") != null)
        		for (String value: helper.getParameterValues("c"))
        			courseIds.add(Long.valueOf(value));
    		List<EnrollmentInfo> enrollments = service.findEnrollmentInfos(online, query, filter, null);
    		if (enrollments != null && sort != 0) {
    			boolean asc = (sort > 0);
    			EnrollmentComparator.SortBy sortBy = EnrollmentComparator.SortBy.values()[Math.abs(sort) - 1];
    			Collections.sort(enrollments, new EnrollmentComparator(sortBy));
    			if (!asc) Collections.reverse(enrollments);
    		}
    		if (!courseIds.isEmpty() && enrollments != null) {
    			List<EnrollmentInfo> allEnrollments = new ArrayList<EnrollmentInfo>();
    			for (EnrollmentInfo e: enrollments) {
    				allEnrollments.add(e);
    				if (e.getCourseId() != null && courseIds.contains(e.getCourseId())) {
    					List<EnrollmentInfo> classEnrollments = service.findEnrollmentInfos(online, query, filter, e.getCourseId());
    					if (classEnrollments != null)
    						allEnrollments.addAll(classEnrollments);
    				}
    			}
    			populateCourseTable(helper, allEnrollments);
    		} else {
    			populateCourseTable(helper, enrollments);
    		}
    		
    	} else if (tab == 1) {
    		List<StudentInfo> students = service.findStudentInfos(online, query, filter);
    		if (students != null && sort != 0) {
    			boolean asc = (sort > 0);
    			StudentComparator.SortBy sortBy = StudentComparator.SortBy.values()[Math.abs(sort) - 1];
    			Collections.sort(students, new StudentComparator(sortBy));
    			if (!asc) Collections.reverse(students);
    		}
    		populateStudentTable(helper, online, students);
    	} else if (online) {
    		List<SectioningAction> changelog = service.changeLog(query);
    		if (changelog != null && sort != 0) {
    			boolean asc = (sort > 0);
    			ChangeLogComparator.SortBy sortBy = ChangeLogComparator.SortBy.values()[Math.abs(sort) - 1];
    			Collections.sort(changelog, new ChangeLogComparator(sortBy));
    			if (!asc) Collections.reverse(changelog);
    		}
    		populateChangeLogTable(helper, changelog);
    	} else {
    		throw new IllegalArgumentException("Wrong tab and online parameter combination.");
    	}
	}
	
	protected void populateCourseTable(ExportHelper helper, List<EnrollmentInfo> enrollments) throws IOException {
		Printer out = new CSVPrinter(helper.getWriter(), false);
		helper.setup(out.getContentType(), reference(), false);
		
		out.printHeader(
				MESSAGES.colSubject() + "\n  " + MESSAGES.colSubpart(),
				MESSAGES.colCourse() + "\n" + MESSAGES.colClass(),
				MESSAGES.colTitle() + "\n" + MESSAGES.colTime(),
				"\n" + MESSAGES.colDate(),
				MESSAGES.colConsent() + "\n" + MESSAGES.colRoom(),
				MESSAGES.colAvailable(),
				MESSAGES.colProjection(),
				MESSAGES.colEnrollment(),
				MESSAGES.colWaitListed(),
				MESSAGES.colUnassignedAlternative().replace("<br>", "\n"),
				MESSAGES.colReserved(),
				MESSAGES.colNeedConsent().replace("<br>", "\n"),
				MESSAGES.colNeedOverride().replace("<br>", "\n"));
		out.flush();
		
		if (enrollments != null)
			for (EnrollmentInfo e: enrollments) {
				if (e.getConfigId() == null) {
					out.printLine(
							e.getSubject(),
							e.getCourseNbr(),
							e.getTitle(),"",
							e.getConsent(),
							(e.getCourseId() == null ? number(e.getAvailable(), e.getLimit()) : available(e)),
							number(null, e.getProjection()),
							number(e.getEnrollment(), e.getTotalEnrollment()),
							waitlist(e),
							number(e.getUnassignedAlternative(), e.getTotalUnassignedAlternative()),
							number(e.getReservation(), e.getTotalReservation()),
							number(e.getConsentNeeded(), e.getTotalConsentNeeded()),
							number(e.getOverrideNeeded(), e.getTotalOverrideNeeded())
							);
				} else {
					out.printLine(
							"  " + (e.getSubpart() == null ? "" : e.getIndent("  ") + e.getSubpart()),
							(e.getClazz() == null ? "" : e.getIndent("  ") + e.getClazz()),
							(e.getAssignment().getDays().isEmpty()  ? "" : e.getAssignment().getDaysString(CONSTANTS.shortDays()) + " " + e.getAssignment().getStartString(CONSTANTS.useAmPm()) + " - " + e.getAssignment().getEndString(CONSTANTS.useAmPm())),
							(!e.getAssignment().hasDatePattern()  ? "" : e.getAssignment().getDatePattern()),
							e.getAssignment().getRooms(","),
							(e.getCourseId() == null ? number(e.getAvailable(), e.getLimit()) : available(e)),
							number(null, e.getProjection()),
							number(e.getEnrollment(), e.getTotalEnrollment()),
							waitlist(e),
							number(e.getUnassignedAlternative(), e.getTotalUnassignedAlternative()),
							number(e.getReservation(), e.getTotalReservation()),
							number(e.getConsentNeeded(), e.getTotalConsentNeeded()),
							number(e.getOverrideNeeded(), e.getTotalOverrideNeeded())
							);
				}
			}
		
		out.flush(); out.close();
	}
	
	protected void populateStudentTable(ExportHelper helper, boolean online, List<StudentInfo> students) throws IOException {
		Printer out = new CSVPrinter(helper.getWriter(), false);
		helper.setup(out.getContentType(), reference(), false);
		
		boolean hasExtId = false;
		if (students != null)
			for (ClassAssignmentInterface.StudentInfo e: students) {
				if (e.getStudent() != null && e.getStudent().isCanShowExternalId()) { hasExtId = true; break; }
			}
		if (!hasExtId) out.hideColumn(0);
		
		boolean hasEnrollment = false, hasWaitList = false,  hasArea = false, hasMajor = false, hasGroup = false, hasAcmd = false, hasReservation = false,
				hasRequestedDate = false, hasEnrolledDate = false, hasConsent = false, hasReqCredit = false, hasCredit = false, hasDistances = false, hasOverlaps = false,
				hasFreeTimeOverlaps = false, hasPrefIMConfs = false, hasPrefSecConfs = false, hasNote = false, hasEmailed = false, hasOverride = false;
		if (students != null)
			for (ClassAssignmentInterface.StudentInfo e: students) {
				if (e.getStudent() == null) continue;
				if (e.getTotalEnrollment() != null && e.getTotalEnrollment() > 0) hasEnrollment = true;
				if (e.getTotalUnassigned() != null && e.getTotalUnassigned() > 0) hasWaitList = true;
				if (e.getStudent().hasArea()) hasArea = true;
				if (e.getStudent().hasMajor()) hasMajor = true;
				if (e.getStudent().hasGroup()) hasGroup = true;
				if (e.getStudent().hasAccommodation()) hasAcmd = true;
				if (e.getTotalReservation() != null && e.getTotalReservation() > 0) hasReservation = true;
				if (e.getRequestedDate() != null) hasRequestedDate = true;
				if (e.getEnrolledDate() != null) hasEnrolledDate = true;
				if (e.getTotalConsentNeeded() != null && e.getTotalConsentNeeded() > 0) hasConsent = true;
				if (e.getTotalOverrideNeeded() != null && e.getTotalOverrideNeeded() > 0) hasOverride = true;
				if (e.hasTotalRequestCredit()) hasReqCredit = true;
				if (e.hasTotalCredit()) hasCredit = true;
				if (e.hasTotalDistanceConflicts()) hasDistances = true;
				if (e.hasOverlappingMinutes()) hasOverlaps = true;
				if (e.hasFreeTimeOverlappingMins()) hasFreeTimeOverlaps = true;
				if (e.hasTotalPrefInstrMethConflict()) hasPrefIMConfs = true;
				if (e.hasTotalPrefSectionConflict()) hasPrefSecConfs = true;
				if (e.hasNote()) hasNote = true;
				if (e.getEmailDate() != null) hasEmailed = true;
			}
		if (!hasArea) { out.hideColumn(2); out.hideColumn(3); }
		if (!hasMajor) out.hideColumn(4);
		if (!hasGroup) out.hideColumn(5);
		if (!hasAcmd) out.hideColumn(6);
		if (!hasEnrollment) out.hideColumn(8);
		if (!hasWaitList) out.hideColumn(9);
		if (!hasReservation) out.hideColumn(10);
		if (!hasConsent) out.hideColumn(11);
		if (!hasOverride) out.hideColumn(12);
		if (!hasReqCredit) out.hideColumn(13);
		if (!hasCredit) out.hideColumn(14);
		if (!hasDistances) { out.hideColumn(15); out.hideColumn(16); }
		if (!hasOverlaps) { out.hideColumn(17); }
		if (!hasFreeTimeOverlaps) { out.hideColumn(18); }
		if (!hasPrefIMConfs) { out.hideColumn(19); }
		if (!hasPrefSecConfs) { out.hideColumn(20); }
		if (!hasRequestedDate) out.hideColumn(21);
		if (!hasEnrolledDate) out.hideColumn(22);
		if (!hasNote) out.hideColumn(23);
		if (!hasEmailed) out.hideColumn(24);
		
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_REQUEST);
		
		out.printHeader(
				MESSAGES.colStudentExternalId(), // 0
				MESSAGES.colStudent(), // 1
				MESSAGES.colArea(), // 2
				MESSAGES.colClassification(), // 3
				MESSAGES.colMajor(), // 4
				MESSAGES.colGroup(), // 5
				MESSAGES.colAccommodation(), // 6
				MESSAGES.colStatus(), // 7
				MESSAGES.colEnrollment(), // 8
				MESSAGES.colWaitListed(), // 9
				MESSAGES.colReservation(), // 10
				MESSAGES.colConsent(), // 11
				MESSAGES.colPendingOverrides().replace("<br>", "\n"), // 12
				MESSAGES.colRequestCredit().replace("<br>", "\n"), // 13
				MESSAGES.colEnrollCredit().replace("<br>", "\n"), // 14
				MESSAGES.colDistanceConflicts().replace("<br>", "\n"), // 15
				MESSAGES.colLongestDistance().replace("<br>", "\n"), // 16
				MESSAGES.colOverlapMins(), // 17
				MESSAGES.colFreeTimeOverlapMins(), // 18
				MESSAGES.colPrefInstrMethConfs().replace("<br>", "\n"), // 19
				MESSAGES.colPrefSectionConfs().replace("<br>", "\n"), // 20
				MESSAGES.colRequestTimeStamp(), // 21
				MESSAGES.colEnrollmentTimeStamp(), // 22
				MESSAGES.colStudentNote(), // 23
				MESSAGES.colEmailTimeStamp() // 24
				);
				
				
		out.flush();
		
		if (students != null)
			for (StudentInfo info: students) {
				if (info.getStudent() != null) {
					out.printLine(
							(info.getStudent().isCanShowExternalId() ? info.getStudent().getExternalId() : ""),
							info.getStudent().getName(),
							info.getStudent().getArea("<br>"),
							info.getStudent().getClassification("<br>"),
							info.getStudent().getMajor("<br>"),
							info.getStudent().getGroup("<br>"),
							info.getStudent().getAccommodation("<br>"),
							info.getStatus(),
							number(info.getEnrollment(), info.getTotalEnrollment()),
							waitlist(info),
							number(info.getReservation(), info.getTotalReservation()),
							number(info.getConsentNeeded(), info.getTotalConsentNeeded()),
							number(info.getOverrideNeeded(), info.getTotalOverrideNeeded()),
							reqCredit(info.getRequestCreditMin(), info.getRequestCreditMax(), info.getTotalRequestCreditMin(), info.getTotalRequestCreditMax()),
							credit(info.getCredit(), info.getTotalCredit()),
							number(info.getNrDistanceConflicts(), info.getTotalNrDistanceConflicts()),
							number(info.getLongestDistanceMinutes(), info.getTotalLongestDistanceMinutes()),
							number(info.getOverlappingMinutes(), info.getTotalOverlappingMinutes()),
							number(info.getFreeTimeOverlappingMins(), info.getTotalFreeTimeOverlappingMins()),
							number(info.getPrefInstrMethConflict(), info.getTotalPrefInstrMethConflict()),
							number(info.getPrefSectionConflict(), info.getTotalPrefSectionConflict()),
							(info.getRequestedDate() == null ? null : df.format(info.getRequestedDate())),
							(info.getEnrolledDate() == null ? null : df.format(info.getEnrolledDate())),
							(info.hasNote() ? info.getNote() : ""),
							(info.getEmailDate() == null ? null : df.format(info.getEmailDate()))
							);
					
				} else {
					out.printLine(
							MESSAGES.total(),
							(hasExtId ? null : MESSAGES.total()),
							number(null, students.size() - 1),
							null,
							null,
							null,
							null,
							null,
							number(info.getEnrollment(), info.getTotalEnrollment()),
							waitlist(info),
							number(info.getReservation(), info.getTotalReservation()),
							number(info.getConsentNeeded(), info.getTotalConsentNeeded()),
							number(info.getOverrideNeeded(), info.getTotalOverrideNeeded()),
							reqCredit(info.getRequestCreditMin(), info.getRequestCreditMax(), info.getTotalRequestCreditMin(), info.getTotalRequestCreditMax()),
							credit(info.getCredit(), info.getTotalCredit()),
							number(info.getNrDistanceConflicts(), info.getTotalNrDistanceConflicts()),
							number(info.getLongestDistanceMinutes(), info.getTotalLongestDistanceMinutes()),
							number(info.getOverlappingMinutes(), info.getTotalOverlappingMinutes()),
							number(info.getFreeTimeOverlappingMins(), info.getTotalFreeTimeOverlappingMins()),
							number(info.getPrefInstrMethConflict(), info.getTotalPrefInstrMethConflict()),
							number(info.getPrefSectionConflict(), info.getTotalPrefSectionConflict()),
							null,
							null,
							null,
							null
							);
				}
			}
		
		out.flush(); out.close();
	}
	
	protected void populateChangeLogTable(ExportHelper helper, List<SectioningAction> changelog) throws IOException {
		Printer out = new CSVPrinter(helper.getWriter(), false);
		helper.setup(out.getContentType(), reference(), false);
		
		out.printHeader(
				MESSAGES.colStudent(),
				MESSAGES.colOperation(),
				MESSAGES.colTimeStamp(),
				MESSAGES.colExecutionTime(),
				MESSAGES.colResult(),
				MESSAGES.colUser(),
				MESSAGES.colMessage()
				);
		out.flush();
		
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
		Formats.Format<Number> nf = Formats.getNumberFormat(CONSTANTS.executionTimeFormat());
		
		if (changelog != null)
			for (ClassAssignmentInterface.SectioningAction log: changelog) {
				out.printLine(
						log.getStudent().getName(),
						log.getOperation(),
						df.format(log.getTimeStamp()),
						(log.getWallTime() == null ? "" : nf.format(0.001 * log.getWallTime())),
						log.getResult(),
						log.getUser(),
						(log.getMessage() == null ? null : log.getMessage().replace("<br>", "\n")));
			}
		
		out.flush(); out.close();
	}
	
	public String number(Integer value, Integer total) {
		if (value == null) {
			if (total != null)
				return (total == 0 ? "-" : total < 0 ? "\u221e" : total.toString());
			else
				return null;
		} else {
			if (value.equals(total))
				return (total == 0 ? "-" : total < 0 ? "\u221e" : total.toString());
			else
				return ((value < 0 ? "\u221e" : value.toString()) + " / " + (total < 0 ? "\u221e" : total.toString()));
		}
	}
	
	public String available(EnrollmentInfo e) {
		int other = (e.getOther() == null ? 0 : e.getOther());
		if (e.getLimit() == null) {
			return "-";
		} else if (e.getLimit() < 0) {
			if (e.getAvailable() != null && e.getAvailable() == 0) {
				return "\u221e" + MESSAGES.csvReservationSign();
			} else {
				return "\u221e";
			}
		} else {
			if (e.getAvailable() == e.getLimit() - e.getTotalEnrollment() - other) {
				return e.getAvailable() + " / " + e.getLimit();
			} else if (e.getAvailable() == 0 && e.getLimit() > e.getTotalEnrollment() + other) {
				return (e.getLimit() - e.getTotalEnrollment() - other) + MESSAGES.csvReservationSign() + " / " + e.getLimit();
			} else {
				return e.getAvailable() + " + " + (e.getLimit() - e.getTotalEnrollment() - e.getAvailable() - other) + MESSAGES.csvReservationSign() + " / " + e.getLimit();
			}
		}
	}
	
	public String waitlist(int wait, int tWait, int unasg, int tUnasg, Integer topWaitingPriority) {
		if (tWait == 0 || tWait == tUnasg) {
			// no wait-list or all wait-listed
			if (unasg == tUnasg) {
				return (unasg == 0 ? "-" : String.valueOf(unasg)) + (tWait > 0 ? MESSAGES.csvWaitListSign() : "") +
						(topWaitingPriority != null ? MESSAGES.csvFirstWaitListedPrioritySign(topWaitingPriority) : "");
			} else {
				return (unasg + " / " + tUnasg) + (tWait > 0 ? MESSAGES.csvWaitListSign() : "") +
						(topWaitingPriority != null ? MESSAGES.csvFirstWaitListedPrioritySign(topWaitingPriority) : "");
			}
		} else {
			if (wait == tWait && unasg == tUnasg) {
				return (wait == 0 ? String.valueOf(unasg) : wait == unasg ? wait + MESSAGES.csvWaitListSign() : (unasg - wait) + " + " + wait + MESSAGES.htmlWaitListSign()) +
						(topWaitingPriority != null ? MESSAGES.csvFirstWaitListedPrioritySign(topWaitingPriority) : "");
						
			} else {
				return ((wait == 0 ? String.valueOf(unasg) : wait == unasg ? wait + MESSAGES.csvWaitListSign() : (unasg - wait) + " + " + wait + MESSAGES.htmlWaitListSign()) + " / " + tUnasg) +
						(topWaitingPriority != null ? MESSAGES.csvFirstWaitListedPrioritySign(topWaitingPriority) : "");
			}
		}
	}
		
	public String waitlist(StudentInfo e) {
		return waitlist(e.hasWaitlist() ? e.getWaitlist() : 0,
			e.hasTotalWaitlist() ? e.getTotalWaitlist() : 0,
			e.hasUnassigned() ? e.getUnassigned() : 0,
			e.hasTotalUnassigned() ? e.getTotalUnassigned() : 0,
			e.getTopWaitingPriority());
	}
	
	public String waitlist(EnrollmentInfo e) {
		return waitlist(e.hasWaitlist() ? e.getWaitlist() : 0,
			e.hasTotalWaitlist() ? e.getTotalWaitlist() : 0,
			e.hasUnassigned() ? e.getUnassignedPrimary() : 0,
			e.hasTotalUnassigned() ? e.getTotalUnassignedPrimary() : 0,
			null);
	}
	
	public String reqCredit(float min, float max, float totalMin, float totalMax) {
		if (totalMax > 0f) {
			if (min == totalMin && max == totalMax)
				return (totalMin == totalMax ? sCreditFormat.format(totalMin) : sCreditFormat.format(totalMin) + " - " + sCreditFormat.format(totalMax));
			else
				return ((min == max ? sCreditFormat.format(min) : sCreditFormat.format(min) + " - " + sCreditFormat.format(max)) + " / " + 
						(totalMin == totalMax ? sCreditFormat.format(totalMin) : sCreditFormat.format(totalMin) + " - " + sCreditFormat.format(totalMax)));
		} else {
			return null;
		}
	}
	
	public String credit(Float value, Float total) {
		if (total != null && total > 0f) {
			if (total.equals(value))
				return (sCreditFormat.format(total));
			else
				return (sCreditFormat.format(value) + " / " + sCreditFormat.format(total));
		} else {
			return null;
		}
	}

}
