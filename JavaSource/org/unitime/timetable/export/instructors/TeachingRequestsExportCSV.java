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
package org.unitime.timetable.export.instructors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsPage.SingleTeachingAssingment;
import org.unitime.timetable.gwt.client.instructor.TeachingRequestsTable;
import org.unitime.timetable.gwt.client.instructor.TeachingRequestsTable.COLUMN;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPageRequest;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.server.instructor.TeachingRequestsPageBackend;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:teaching-requests.csv")
public class TeachingRequestsExportCSV implements Exporter {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final StudentSectioningMessages SECTMSG = Localization.create(StudentSectioningMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Override
	public String reference() { return "teaching-requests.csv"; }

	@Override
	public void export(ExportHelper helper) throws IOException {
		TeachingRequestsPageRequest request = new TeachingRequestsPageRequest();
		for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
    		String command = e.nextElement();
    		if (command.equals("r:text")) {
    			request.getFilter().setText(helper.getParameter("r:text"));
    		} else if (command.startsWith("r:")) {
    			for (String value: helper.getParameterValues(command))
    				request.getFilter().addOption(command.substring(2), value);
    		}
		}
		if (helper.getParameter("subjectId") != null) {
			request.getFilter().addOption("subjectId", helper.getParameter("subjectId"));
		} else if (helper.getParameter("subject") != null) {
			Long sessionId = helper.getAcademicSessionId();
			if (sessionId == null)
				throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
			SubjectArea subject = SubjectArea.findByAbbv(sessionId, helper.getParameter("subject"));
			if (subject == null)
				throw new IllegalArgumentException("Subject area " + helper.getParameter("subject") + " does not exist.");
			request.getFilter().addOption("subjectId", subject.getUniqueId().toString());
		}
		if (helper.getParameter("offeringId") != null) {
			request.getFilter().addOption("offeringId", helper.getParameter("offeringId"));
		} else if (helper.getParameter("course") != null) {
			Long sessionId = helper.getAcademicSessionId();
			if (sessionId == null)
				throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
			CourseOffering course = CourseOffering.findByName(helper.getParameter("course"), sessionId);
			if (course == null)
				throw new IllegalArgumentException("Course offering " + helper.getParameter("course") + " does not exist.");
			request.getFilter().addOption("offeringId", course.getInstructionalOffering().getUniqueId().toString());
		}
		if (helper.getParameter("assigned") != null) {
			request.getFilter().addOption("assigned", ("true".equalsIgnoreCase(helper.getParameter("assigned")) || "1".equals(helper.getParameter("assigned"))) ? "true" : "false");
		}
		
		List<SingleTeachingAssingment> list = new ArrayList<SingleTeachingAssingment>();
		boolean hasInstructors = false;
		for (TeachingRequestInfo req: new TeachingRequestsPageBackend().execute(request, helper.getSessionContext())) {
			if (!request.getFilter().hasOption("assigned")) {
				if (req.hasInstructors()) {
					for (InstructorInfo instructor: req.getInstructors()) {
						list.add(new SingleTeachingAssingment(instructor, req));
						hasInstructors = true;
					}
				} else {
					list.add(new SingleTeachingAssingment(null, req));
				}
			} else if ("true".equalsIgnoreCase(request.getFilter().getOption("assigned")) && req.hasInstructors()) {
				for (InstructorInfo instructor: req.getInstructors()) {
					if (!instructor.isMatchingFilter()) continue;
					list.add(new SingleTeachingAssingment(instructor, req));
					hasInstructors = true;
				}
			} else if (!"true".equalsIgnoreCase(request.getFilter().getOption("assigned")) && req.getNrAssignedInstructors() < req.getNrInstructors()) {
				list.add(new SingleTeachingAssingment(null, req));
			}
		}
		if (helper.getParameter("sort") != null) {
			int sort = Integer.parseInt(helper.getParameter("sort"));
			if (sort != 0) {
				TeachingRequestsTable.TableComparator cmp = new TeachingRequestsTable.TableComparator(COLUMN.values()[Math.abs(sort) - 1]);
				Collections.sort(list, sort < 0 ? Collections.reverseOrder(cmp) : cmp);
			}
		}
		int cookie = 0xffff;
		if (helper.getParameter("columns") != null)
			cookie = Integer.parseInt(helper.getParameter("columns"));
		export(request, list, helper, hasInstructors, cookie);
	}
	
	protected void export(TeachingRequestsPageRequest request, List<SingleTeachingAssingment> list, ExportHelper helper, boolean hasInstructors, int cookie) throws IOException {
		Printer out = new CSVPrinter(helper.getWriter(), false);
		helper.setup(out.getContentType(), reference(), false);
		
		List<Column> columns = new ArrayList<Column>();
		for (COLUMN column: COLUMN.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				Column c = new Column(column, idx);
				if (isColumnVisible(c, hasInstructors, cookie))
					columns.add(c);
			}
		}
		
		String[] header = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++)
			header[i] = getColumnName(columns.get(i)).replace("<br>", "\n");
		out.printHeader(header);
		out.flush();

		TeachingRequestInfo last = null;
		for (SingleTeachingAssingment a: list) {
			String[] row = new String[columns.size()];
			if (last != null && last.equals(a.getRequest())) {
				for (int i = 0; i < columns.size(); i++) {
					if (columns.get(i).getColumn().isHasInstructor())
						row[i] = getCell(a.getRequest(), a.getInstructor(), columns.get(i));
					else
						row[i] = null;
				}
			} else {
				for (int i = 0; i < columns.size(); i++)
					row[i] = getCell(a.getRequest(), a.getInstructor(), columns.get(i));
			}
			out.printLine(row);
			out.flush();
			last = a.getRequest();
		}
		
		out.flush(); out.close();
	}
	
	public int getNbrCells(COLUMN column) {
		return 1;
	}
	
	public boolean isColumnVisible(Column column, boolean hasInstructors, int cookie) {
		boolean visible = !column.getColumn().isCanHide() || (cookie & (1 << column.getColumn().ordinal())) != 0;
		if (!hasInstructors && column.getColumn().isCanHide() && column.getColumn().isHasInstructor())
			visible = false;
		return visible;
	}
	
	public String getColumnName(Column column) {
		switch (column.getColumn()) {
		case COURSE:
			return MESSAGES.colCourse();
		case SECTION:
			return MESSAGES.colSection();
		case TIME:
			return MESSAGES.colTime();
		case DATE:
			return MESSAGES.colDate();
		case ROOM:
			return MESSAGES.colRoom();
		case LOAD:
			return MESSAGES.colTeachingLoad();
		case ATTRIBUTE_PREFS:
			return MESSAGES.colAttributePreferences();
		case INSTRUCTOR_PREFS:
			return MESSAGES.colInstructorPreferences();
		case EXTERNAL_ID:
			return MESSAGES.colExternalId();
		case NAME:
			return MESSAGES.colNamePerson();
		case ATTRIBUTES:
			return MESSAGES.colAttributes();
		case COURSE_PREF:
			return MESSAGES.colCoursePreferences();
		case DISTRIBUTION_PREF:
			return MESSAGES.colDistributionPreferences();
		case TIME_PREF:
			return MESSAGES.colTimePreferences();
		case ASSIGNED_LOAD:
			return MESSAGES.colAssignedLoad();
		case OBJECTIVES:
			return MESSAGES.colObjectives();
		case ASSIGNED_INSTRUCTORS:
			return MESSAGES.colAssignedInstructors();
		default:
			return column.getColumn().name();
		}
	}
	
	public String getCell(TeachingRequestInfo request, final InstructorInfo instructor, Column column) {
		switch (column.getColumn()) {
		case COURSE:
			return request.getCourse().getCourseName();
		case SECTION:
			String sections = "";
			for (SectionInfo s: request.getSections()) {
				sections += (sections.isEmpty() ? "" : "\n") + s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId());
			}
			return sections;
		case TIME:
			String times = "";
			for (SectionInfo s: request.getSections()) {
				times += (times.isEmpty() ? "" : "\n") + (s.getTime() == null ? SECTMSG.arrangeHours() : s.getTime());
			}
			return times;
		case DATE:
			String dates = "";
			for (SectionInfo s: request.getSections()) {
				dates += (dates.isEmpty() ? "" : "\n") + (s.getDate() == null ? SECTMSG.noDate() : s.getDate());
			}
			return dates;
		case ROOM:
			String rooms = "";
			for (SectionInfo s: request.getSections()) {
				rooms += (rooms.isEmpty() ? "" : "\n") + (s.getRoom() == null ? SECTMSG.noRoom() : s.getRoom());
			}
			return rooms;
		case LOAD:
			return Formats.getNumberFormat(CONSTANTS.teachingLoadFormat()).format(request.getLoad());
		case EXTERNAL_ID:
			return (instructor == null ? null : instructor.getExternalId());
		case NAME:
			return (instructor == null ? null : instructor.getInstructorName());
		case ATTRIBUTE_PREFS:
			return preferences(request.getAttributePreferences());
		case INSTRUCTOR_PREFS:
			return preferences(request.getInstructorPreferences());
		case COURSE_PREF:
			if (instructor == null) return null;
			return preferences(instructor.getCoursePreferences());
		case DISTRIBUTION_PREF:
			if (instructor == null) return null;
			return preferences(instructor.getDistributionPreferences());
		case TIME_PREF:
			if (instructor == null) return null;
			return preferences(instructor.getTimePreferences());
		case ATTRIBUTES:
			if (instructor == null) return null;
			String attributes = "";
			for (AttributeInterface attribute: instructor.getAttributes()) {
				attributes += (attributes.isEmpty() ? "" : "\n") + attribute.getName() + (attribute.hasType() ? " (" + attribute.getType().getLabel() + ")" : "");
			}
			return attributes;
		case ASSIGNED_LOAD:
			if (instructor == null) return null;
			return Formats.getNumberFormat(CONSTANTS.teachingLoadFormat()).format(instructor.getAssignedLoad()) + " / " + Formats.getNumberFormat(CONSTANTS.teachingLoadFormat()).format(instructor.getMaxLoad());
		case OBJECTIVES:
			if (instructor == null) return null;
			String objectives = "";
			for (String key: new TreeSet<String>(instructor.getValues().keySet())) {
				Double value = instructor.getValues().get(key);
				if (value == null || Math.abs(value) < 0.001) continue;
				objectives += (objectives.isEmpty() ? "" : "\n") + key + ": " + (value > 0.0 ? "+": "") + Formats.getNumberFormat(CONSTANTS.teachingLoadFormat()).format(value);
			}
			return objectives;
		case ASSIGNED_INSTRUCTORS:
			return request.getNrAssignedInstructors() + " / " + request.getNrInstructors();
		default:
			return null;
		}
	}
	
	public String preferences(List<PreferenceInfo> list) {
		if (list == null || list.isEmpty()) return null;
		String ret = "";
		for (PreferenceInfo pref: list) {
			ret += (ret.isEmpty() ? "" : "\n") + PreferenceLevel.prolog2abbv(pref.getPreference()) + " " + pref.getOwnerName();
		}
		return ret;
	}

	protected static class Column {
		private COLUMN iColumn;
		private int iIndex;
		
		Column(COLUMN column, int index) { iColumn = column; iIndex = index; }
		
		public int getIndex() { return iIndex; }
		public COLUMN getColumn() { return iColumn; }
	}

}
