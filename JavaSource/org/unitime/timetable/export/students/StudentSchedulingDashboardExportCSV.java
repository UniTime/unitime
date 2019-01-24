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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
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
		
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId != null && helper.getSessionContext().isAuthenticated() && !sessionId.equals(helper.getSessionContext().getUser().getCurrentAcademicSessionId())) {
			UserAuthority preferredAuthority = null;
			for (UserAuthority auth: helper.getSessionContext().getUser().getAuthorities(null, new SimpleQualifier("Session", sessionId))) {
				if (preferredAuthority == null && auth.hasRight(Right.StudentSectioningSolverDashboard)) {
					preferredAuthority = auth;
				} else if ((preferredAuthority == null || !preferredAuthority.hasRight(Right.StudentSchedulingAdmin)) && auth.hasRight(Right.StudentSchedulingAdvisor)) {
					preferredAuthority = auth;
				} else if (auth.hasRight(Right.StudentSchedulingAdmin)) {
					preferredAuthority = auth;
				}
			}
			if (preferredAuthority != null)
				helper.getSessionContext().getUser().setCurrentAuthority(preferredAuthority);
		}
		
		int tab = 0;
		try { tab = Integer.parseInt(helper.getParameter("tab")); } catch (Exception e) {}
		String query = helper.getParameter("query");
		if (query == null) query = "";
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
    			Collections.sort(enrollments, new EnrollmentComparator(sortBy, asc));
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
    			Collections.sort(students, new StudentComparator(sortBy, asc, helper.getParameter("g")));
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
		boolean hasEnrollment = false, hasWaitList = false,  hasArea = false, hasMajor = false, hasGroup = false, hasAcmd = false, hasReservation = false,
				hasRequestedDate = false, hasEnrolledDate = false, hasConsent = false, hasReqCredit = false, hasCredit = false, hasDistances = false, hasOverlaps = false,
				hasFreeTimeOverlaps = false, hasPrefIMConfs = false, hasPrefSecConfs = false, hasNote = false, hasEmailed = false, hasOverride = false;
		Set<String> groupTypes = new TreeSet<String>();
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
				if (e.getStudent() != null && e.getStudent().isCanShowExternalId()) hasExtId = true;
				if (e.getStudent().hasGroups()) groupTypes.addAll(e.getStudent().getGroupTypes());
			}
		
		List<String> header = new ArrayList<String>();
		if (hasExtId)
			header.add(MESSAGES.colStudentExternalId());
		
		header.add(MESSAGES.colStudent());
		if (hasArea) {
			header.add(MESSAGES.colArea());
			header.add(MESSAGES.colClassification());
		}
		
		if (hasMajor)
			header.add(MESSAGES.colMajor());
		
		if (hasGroup)
			header.add(MESSAGES.colGroup());
		
		header.addAll(groupTypes);
		
		if (hasAcmd)
			header.add(MESSAGES.colAccommodation());
		
		header.add(MESSAGES.colStatus());
		
		if (hasEnrollment)
			header.add(MESSAGES.colEnrollment());
		
		if (hasWaitList)
			header.add(MESSAGES.colWaitListed());
		
		if (hasReservation)
			header.add(MESSAGES.colReservation());
		
		if (hasConsent)
			header.add(MESSAGES.colConsent());
		
		if (hasOverride)
			header.add(MESSAGES.colPendingOverrides().replace("<br>", "\n"));
		
		if (hasReqCredit)
			header.add(MESSAGES.colRequestCredit().replace("<br>", "\n"));
		
		if (hasCredit)
			header.add(MESSAGES.colEnrollCredit().replace("<br>", "\n"));
		
		if (hasDistances) {
			header.add(MESSAGES.colDistanceConflicts().replace("<br>", "\n"));
			header.add(MESSAGES.colLongestDistance().replace("<br>", "\n"));
		}
		
		if (hasOverlaps)
			header.add(MESSAGES.colOverlapMins());
		
		if (hasFreeTimeOverlaps)
			header.add(MESSAGES.colFreeTimeOverlapMins());
		
		if (hasPrefIMConfs)
			header.add(MESSAGES.colPrefInstrMethConfs().replace("<br>", "\n"));
		
		if (hasPrefSecConfs)
			header.add(MESSAGES.colPrefSectionConfs().replace("<br>", "\n"));
		
		if (hasRequestedDate)
			header.add(MESSAGES.colRequestTimeStamp());
		
		if (hasEnrolledDate)
			header.add(MESSAGES.colEnrollmentTimeStamp());
		
		if (hasNote)
			header.add(MESSAGES.colStudentNote());
		
		if (hasEmailed)
			header.add(MESSAGES.colEmailTimeStamp());
		
		out.printHeader(header.toArray(new String[header.size()]));
		out.flush();
		
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_REQUEST);
		if (students != null)
			for (StudentInfo info: students) {
				List<String> line = new ArrayList<String>();
				if (info.getStudent() != null) {
					if (hasExtId)
						line.add(info.getStudent().isCanShowExternalId() ? info.getStudent().getExternalId() : "");
					line.add(info.getStudent().getName());
					if (hasArea) {
						line.add(info.getStudent().getArea("\n"));
						line.add(info.getStudent().getClassification("\n"));
					}
					if (hasMajor)
						line.add(info.getStudent().getMajor("\n"));
					if (hasGroup)
						line.add(info.getStudent().getGroup("\n"));
					for (String g: groupTypes)
						line.add(info.getStudent().getGroup(g, "\n"));
					if (hasAcmd)
						line.add(info.getStudent().getAccommodation("\n"));
					line.add(info.getStatus());
					if (hasEnrollment)
						line.add(number(info.getEnrollment(), info.getTotalEnrollment()));
					if (hasWaitList)
						line.add(waitlist(info));
					if (hasReservation)
						line.add(number(info.getReservation(), info.getTotalReservation()));
					if (hasConsent)
						line.add(number(info.getConsentNeeded(), info.getTotalConsentNeeded()));
					if (hasOverride)
						line.add(number(info.getOverrideNeeded(), info.getTotalOverrideNeeded()));
					if (hasReqCredit)
						line.add(reqCredit(info.getRequestCreditMin(), info.getRequestCreditMax(), info.getTotalRequestCreditMin(), info.getTotalRequestCreditMax()));
					if (hasCredit)
						line.add(credit(info));
					if (hasDistances) {
						line.add(number(info.getNrDistanceConflicts(), info.getTotalNrDistanceConflicts()));
						line.add(number(info.getLongestDistanceMinutes(), info.getTotalLongestDistanceMinutes()));
					}
					if (hasOverlaps)
						line.add(number(info.getOverlappingMinutes(), info.getTotalOverlappingMinutes()));
					if (hasFreeTimeOverlaps)
						line.add(number(info.getFreeTimeOverlappingMins(), info.getTotalFreeTimeOverlappingMins()));
					if (hasPrefIMConfs)
						line.add(number(info.getPrefInstrMethConflict(), info.getTotalPrefInstrMethConflict()));
					if (hasPrefSecConfs)
						line.add(number(info.getPrefSectionConflict(), info.getTotalPrefSectionConflict()));
					if (hasRequestedDate)
						line.add((info.getRequestedDate() == null ? null : df.format(info.getRequestedDate())));
					if (hasEnrolledDate)
						line.add((info.getEnrolledDate() == null ? null : df.format(info.getEnrolledDate())));
					if (hasNote)
						line.add((info.hasNote() ? info.getNote() : ""));
					if (hasEmailed)
						line.add((info.getEmailDate() == null ? null : df.format(info.getEmailDate())));
				} else {
					line.add(MESSAGES.total());
					if (hasExtId)
						line.add("");
					line.add(number(null, students.size() - 1));
					if (hasArea) {
						line.add("");
						line.add("");
					}
					if (hasMajor)
						line.add("");
					if (hasGroup)
						line.add("");
					for (@SuppressWarnings("unused") String g: groupTypes)
						line.add("");
					if (hasAcmd)
						line.add("");
					if (hasEnrollment)
						line.add(number(info.getEnrollment(), info.getTotalEnrollment()));
					if (hasWaitList)
						line.add(waitlist(info));
					if (hasReservation)
						line.add(number(info.getReservation(), info.getTotalReservation()));
					if (hasConsent)
						line.add(number(info.getConsentNeeded(), info.getTotalConsentNeeded()));
					if (hasOverride)
						line.add(number(info.getOverrideNeeded(), info.getTotalOverrideNeeded()));
					if (hasReqCredit)
						line.add(reqCredit(info.getRequestCreditMin(), info.getRequestCreditMax(), info.getTotalRequestCreditMin(), info.getTotalRequestCreditMax()));
					if (hasCredit)
						line.add(credit(info));
					if (hasDistances) {
						line.add(number(info.getNrDistanceConflicts(), info.getTotalNrDistanceConflicts()));
						line.add(number(info.getLongestDistanceMinutes(), info.getTotalLongestDistanceMinutes()));
					}
					if (hasOverlaps)
						line.add(number(info.getOverlappingMinutes(), info.getTotalOverlappingMinutes()));
					if (hasFreeTimeOverlaps)
						line.add(number(info.getFreeTimeOverlappingMins(), info.getTotalFreeTimeOverlappingMins()));
					if (hasPrefIMConfs)
						line.add(number(info.getPrefInstrMethConflict(), info.getTotalPrefInstrMethConflict()));
					if (hasPrefSecConfs)
						line.add(number(info.getPrefSectionConflict(), info.getTotalPrefSectionConflict()));
					if (hasRequestedDate)
						line.add("");
					if (hasEnrolledDate)
						line.add("");
					if (hasNote)
						line.add("");
					if (hasEmailed)
						line.add("");
				}
				out.printLine(line.toArray(new String[line.size()]));				
			}
		
		out.flush(); out.close();
	}
	
	protected void populateChangeLogTable(ExportHelper helper, List<SectioningAction> changelog) throws IOException {
		Printer out = new CSVPrinter(helper.getWriter(), false);
		helper.setup(out.getContentType(), reference(), false);
		
		boolean hasExtId = false;
		if (changelog != null)
			for (SectioningAction e: changelog) {
				if (e.getStudent() != null && e.getStudent().isCanShowExternalId()) { hasExtId = true; break; }
			}
		if (!hasExtId) out.hideColumn(0);
		
		out.printHeader(
				MESSAGES.colStudentExternalId(),
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
						log.getStudent().isCanShowExternalId() ? log.getStudent().getExternalId() : null,
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
				return (wait == 0 ? String.valueOf(unasg) : wait == unasg ? wait + MESSAGES.csvWaitListSign() : (unasg - wait) + " + " + wait + MESSAGES.csvWaitListSign()) +
						(topWaitingPriority != null ? MESSAGES.csvFirstWaitListedPrioritySign(topWaitingPriority) : "");
						
			} else {
				return ((wait == 0 ? String.valueOf(unasg) : wait == unasg ? wait + MESSAGES.csvWaitListSign() : (unasg - wait) + " + " + wait + MESSAGES.csvWaitListSign()) + " / " + tUnasg) +
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
	
	public String credit(StudentInfo info) {
		Float value = info.getCredit();
		Float total = info.getTotalCredit();
		if (total != null && total > 0f) {
			if (total.equals(value)) {
				String html = sCreditFormat.format(total);
				if (info.hasIMTotalCredit()) {
					html += " (";
					for (Iterator<String> i = info.getTotalCreditIMs().iterator(); i.hasNext();) {
						String im = i.next();
						html += im + ": " + sCreditFormat.format(info.getIMTotalCredit(im));
						if (i.hasNext()) html += ", ";
					}
					html += ")";
				}
				return html;
			} else {
				String html = sCreditFormat.format(value) + " / " + sCreditFormat.format(total);
				if (info.hasIMCredit()) {
					html += " (";
					for (Iterator<String> i = info.getCreditIMs().iterator(); i.hasNext();) {
						String im = i.next();
						html += im + ": " + sCreditFormat.format(info.getIMCredit(im));
						if (i.hasNext()) html += ", ";
					}
					html += ")";
				}
				return html;
			}
		} else {
			return "";
		}
	}

}
