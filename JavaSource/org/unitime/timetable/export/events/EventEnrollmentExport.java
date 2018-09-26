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
package org.unitime.timetable.export.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.events.EventEnrollmentsBackend;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable.EnrollmentComparator;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Conflict;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.EventInterface.EventEnrollmentsRpcRequest;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;


/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:event-enrollments.csv")
public class EventEnrollmentExport implements Exporter {
	public static final StudentSectioningMessages MESSAGES = Localization.create(StudentSectioningMessages.class);
	public static final GwtMessages GWT_MSG = Localization.create(GwtMessages.class);
	protected static Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_REQUEST);
	protected static Format<Date> sTSF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	
	@Override
	public String reference() { return "event-enrollments.csv"; }

	@Override
	public void export(ExportHelper helper) throws IOException {
		String eventId = helper.getParameter("event");
		if (eventId == null)
			throw new IllegalArgumentException("Event parameter not provided.");
		Class_ clazz = null;
		Event event = null;
		if (Long.valueOf(eventId) < 0)
			clazz = Class_DAO.getInstance().get(-Long.valueOf(eventId));
		else {
			event = EventDAO.getInstance().get(Long.valueOf(eventId));
			if (event != null && event instanceof CourseEvent)
				event = CourseEventDAO.getInstance().get(event.getUniqueId());
			else if (event != null && event instanceof ExamEvent)
				event = ExamEventDAO.getInstance().get(event.getUniqueId());
			else if (event != null && event instanceof ClassEvent)
				event = ClassEventDAO.getInstance().get(event.getUniqueId());
		}
		if (clazz == null && event == null)
			throw new IllegalArgumentException("Given event does not exist.");
		
		EventEnrollmentsRpcRequest request = new EventEnrollmentsRpcRequest();
		
		request.setEventId(event != null ? event.getUniqueId() : -clazz.getUniqueId());
		Session session = (event != null ? event.getSession() : clazz.getControllingDept().getSession());
		
		EventContext context = new EventContext(helper.getSessionContext(), helper.getSessionContext().getUser(), session != null ? session.getUniqueId() : null);
		
		int sort = 0;
		if (helper.getParameter("sort") != null)
			sort = Integer.parseInt(helper.getParameter("sort")); 
		
		List<Enrollment> enrollments = new EventEnrollmentsBackend().execute(request, context);
		export(enrollments, helper,
				"1".equalsIgnoreCase(helper.getParameter("suffix")),
				sort,
				helper.getParameter("subpart"));
	}
	
	protected Printer createPrinter(ExportHelper helper) throws IOException {
		Printer out = new CSVPrinter(helper.getWriter(), false);
		helper.setup(out.getContentType(), reference(), false);
		return out;
	}
	
	protected void export(List<Enrollment> enrollments, ExportHelper helper, boolean suffix, int sort, String sortBySubpart) throws IOException {
		Printer out = createPrinter(helper);

		if (enrollments == null) enrollments = new ArrayList<Enrollment>();

		if (sort != 0) {
			boolean asc = (sort > 0);
			EnrollmentComparator.SortBy sortBy = EnrollmentComparator.SortBy.values()[Math.abs(sort) - 1];
			Collections.sort(enrollments, new EnrollmentComparator(sortBy));
			if (!asc) Collections.reverse(enrollments);
		} else if (sortBySubpart != null && !sortBySubpart.isEmpty()) {
			boolean asc = !sortBySubpart.startsWith("-");
			Collections.sort(enrollments, new EnrollmentComparator(asc ? sortBySubpart : sortBySubpart.substring(1), suffix));
			if (!asc) Collections.reverse(enrollments);
		} else {
			Collections.sort(enrollments, new Comparator<ClassAssignmentInterface.Enrollment>() {
				@Override
				public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
					int cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
					if (cmp != 0) return cmp;
					return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
				}
			});
		}
		
		boolean hasExtId = false;
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (e.getStudent().isCanShowExternalId()) { hasExtId = true; break; }
		}
		
		List<String> header = new ArrayList<String>();
		if (hasExtId)
			header.add(MESSAGES.colStudentExternalId());
		header.add(MESSAGES.colStudent());
		header.add(GWT_MSG.colEmail());
		
		boolean crosslist = false;
		Long courseId = null;
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (courseId == null) courseId = e.getCourseId();
			else if (e.getCourseId() != courseId) { crosslist = true; break; }
			if (e.getCourse() != null && e.getCourse().hasCrossList()) { crosslist = true; break; }
		}
		
		if (crosslist)
			header.add(MESSAGES.colCourse());
		
		boolean hasPriority = false, hasArea = false, hasMajor = false, hasGroup = false, hasAcmd = false, hasAlternative = false, hasReservation = false, hasRequestedDate = false, hasEnrolledDate = false, hasConflict = false, hasMessage = false;
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (e.getPriority() > 0) hasPriority = true;
			if (e.isAlternative()) hasAlternative = true;
			if (e.getStudent().hasArea()) hasArea = true;
			if (e.getStudent().hasMajor()) hasMajor = true;
			if (e.getStudent().hasGroup()) hasGroup = true;
			if (e.getStudent().hasAccommodation()) hasAcmd = true;
			if (e.getReservation() != null) hasReservation = true;
			if (e.getRequestedDate() != null) hasRequestedDate = true;
			if (e.getEnrolledDate() != null) hasEnrolledDate = true;
			if (e.hasConflict()) hasConflict = true;
			if (e.hasEnrollmentMessage()) hasMessage = true;
		}
		
		if (hasPriority)
			header.add(MESSAGES.colPriority());
		
		if (hasAlternative)
			header.add(MESSAGES.colAlternative());
		
		if (hasArea) {
			header.add(MESSAGES.colArea());
			header.add(MESSAGES.colClassification());
		}
		
		if (hasMajor)
			header.add(MESSAGES.colMajor());
		
		if (hasGroup)
			header.add(MESSAGES.colGroup());
		
		if (hasAcmd)
			header.add(MESSAGES.colAccommodation());
		
		if (hasReservation)
			header.add(MESSAGES.colReservation());
		
		final TreeSet<String> subparts = new TreeSet<String>();
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (e.hasClasses())
				for (ClassAssignmentInterface.ClassAssignment c: e.getClasses())
					subparts.add(c.getSubpart());
		}
		
		for (final String subpart: subparts) {
			header.add(subpart);
		}
		
		if (hasRequestedDate)
			header.add(MESSAGES.colRequestTimeStamp());
		
		if (hasEnrolledDate)
			header.add(MESSAGES.colEnrollmentTimeStamp());
		
		if (hasMessage)
			header.add(MESSAGES.colMessage());

		if (hasConflict) {
			header.add(MESSAGES.colConflictType());
			header.add(MESSAGES.colConflictName());
			header.add(MESSAGES.colConflictDate());
			header.add(MESSAGES.colConflictTime());
			header.add(MESSAGES.colConflictRoom());
		}
		
		out.printHeader(header.toArray(new String[header.size()]));
		out.flush();
		
		
		
		for (ClassAssignmentInterface.Enrollment enrollment: enrollments) {
			List<String> line = new ArrayList<String>();
			if (hasExtId)
				line.add(enrollment.getStudent().isCanShowExternalId() ? enrollment.getStudent().getExternalId() : "");
			line.add(enrollment.getStudent().getName());
			line.add(enrollment.getStudent().getEmail());
			if (crosslist)
				line.add(enrollment.getCourseName());
			if (hasPriority)
				line.add(enrollment.getPriority() <= 0 ? "" : MESSAGES.priority(enrollment.getPriority()));
			if (hasAlternative)
				line.add(enrollment.getAlternative());
			if (hasArea) {
				line.add(enrollment.getStudent().getArea("\n"));
				line.add(enrollment.getStudent().getClassification("\n"));
			}
			if (hasMajor)
				line.add(enrollment.getStudent().getMajor("\n"));
			if (hasGroup)
				line.add(enrollment.getStudent().getGroup("\n"));
			if (hasAcmd)
				line.add(enrollment.getStudent().getAccommodation("\n"));
			if (hasReservation)
				line.add(enrollment.getReservation() == null ? "" : enrollment.getReservation());
			if (!subparts.isEmpty()) {
				if (!enrollment.hasClasses()) {
					line.add(enrollment.isWaitList() ? MESSAGES.courseWaitListed() : MESSAGES.courseNotEnrolled());
					for (int i = 1; i < subparts.size(); i++) line.add("");
				} else for (String subpart: subparts) {
					line.add(enrollment.getClasses(subpart, ", ", suffix));
				}
			}
			if (hasRequestedDate)
				line.add(enrollment.getRequestedDate() == null ? "" : sDF.format(enrollment.getRequestedDate()));
			if (hasEnrolledDate)
				line.add(enrollment.getEnrolledDate() == null ? "" : sDF.format(enrollment.getEnrolledDate()));
			if (hasMessage)
				line.add(enrollment.hasEnrollmentMessage() ? enrollment.getEnrollmentMessage() : "");
			if (hasConflict) {
				if (enrollment.hasConflict()) {
					String name = "", type = "", date = "", time = "", room = "";
					for (Conflict conflict: enrollment.getConflicts()) {
						if (!name.isEmpty()) { name += "\n"; type += "\n"; date += "\n"; time += "\n"; room += "\n"; }
						name += conflict.getName();
						type += conflict.getType();
						date += conflict.getDate();
						time += conflict.getTime();
						room += conflict.getRoom();
					}
					line.add(type);
					line.add(name);
					line.add(date);
					line.add(time);
					line.add(room);
				} else {
					line.add("");
					line.add("");
					line.add("");
					line.add("");
					line.add("");
				}
			}
			out.printLine(line.toArray(new String[line.size()]));
		}
		
		out.flush(); out.close();
	}

}
