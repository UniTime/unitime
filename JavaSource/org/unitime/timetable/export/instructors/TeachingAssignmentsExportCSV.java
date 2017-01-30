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
import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsTable.COLUMN;
import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsPage.SingleTeachingAssingment;
import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsTable;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingAssignmentsPageRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.server.instructor.TeachingAssignmentsBackend;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:teaching-assignments.csv")
public class TeachingAssignmentsExportCSV implements Exporter {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final StudentSectioningMessages SECTMSG = Localization.create(StudentSectioningMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Override
	public String reference() { return "teaching-assignments.csv"; }

	@Override
	public void export(ExportHelper helper) throws IOException {
		TeachingAssignmentsPageRequest request = new TeachingAssignmentsPageRequest();
		for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
    		String command = e.nextElement();
    		if (command.equals("r:text")) {
    			request.getFilter().setText(helper.getParameter("r:text"));
    		} else if (command.startsWith("r:")) {
    			for (String value: helper.getParameterValues(command))
    				request.getFilter().addOption(command.substring(2), value);
    		}
		}

		if (helper.getParameter("departmentId") != null) {
			request.getFilter().setOption("departmentId", helper.getParameter("departmentId"));
		} else if (helper.getParameter("department") != null) {
			Long sessionId = helper.getAcademicSessionId();
			if (sessionId == null)
				throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
			Department department = Department.findByDeptCode(helper.getParameter("department"), sessionId);
			if (department == null)
				throw new IllegalArgumentException("Department " + helper.getParameter("department") + " does not exist.");
			request.getFilter().setOption("departmentId", department.getUniqueId().toString());
		} else if (helper.getParameter("deptCode") != null) {
			Long sessionId = helper.getAcademicSessionId();
			if (sessionId == null)
				throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
			Department department = Department.findByDeptCode(helper.getParameter("deptCode"), sessionId);
			if (department == null)
				throw new IllegalArgumentException("Department " + helper.getParameter("deptCode") + " does not exist.");
			request.getFilter().setOption("departmentId", department.getUniqueId().toString());
		}
		
		List<SingleTeachingAssingment> list = new ArrayList<SingleTeachingAssingment>();
		boolean hasRequests = false;
		for (InstructorInfo instructor: new TeachingAssignmentsBackend().execute(request, helper.getSessionContext())) {
			if (instructor.getAssignedRequests().isEmpty()) {
				list.add(new SingleTeachingAssingment(instructor, null));
			} else {
				for (TeachingRequestInfo req: instructor.getAssignedRequests()) {
					if (!req.isMatchingFilter()) continue;
					list.add(new SingleTeachingAssingment(instructor, req));
					hasRequests = true;
				}
			}
		}
		if (helper.getParameter("sort") != null) {
			int sort = Integer.parseInt(helper.getParameter("sort"));
			if (sort != 0) {
				TeachingAssignmentsTable.TableComparator cmp = new TeachingAssignmentsTable.TableComparator(COLUMN.values()[Math.abs(sort) - 1]);
				Collections.sort(list, sort < 0 ? Collections.reverseOrder(cmp) : cmp);
			}
		}
		int cookie = 0xffff;
		if (helper.getParameter("columns") != null)
			cookie = Integer.parseInt(helper.getParameter("columns"));
		export(request, list, helper, hasRequests, cookie);
	}
	
	protected void export(TeachingAssignmentsPageRequest request, List<SingleTeachingAssingment> list, ExportHelper helper, boolean hasRequests, int cookie) throws IOException {
		Printer out = new CSVPrinter(helper.getWriter(), false);
		helper.setup(out.getContentType(), reference(), false);
		
		List<Column> columns = new ArrayList<Column>();
		for (COLUMN column: COLUMN.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				Column c = new Column(column, idx);
				if (isColumnVisible(c, hasRequests, cookie))
					columns.add(c);
			}
		}
		
		String[] header = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++)
			header[i] = getColumnName(columns.get(i)).replace("<br>", "\n");
		out.printHeader(header);
		out.flush();

		InstructorInfo last = null;
		for (SingleTeachingAssingment a: list) {
			String[] row = new String[columns.size()];
			if (last != null && last.equals(a.getInstructor())) {
				for (int i = 0; i < columns.size(); i++) {
					if (columns.get(i).getColumn().isHasRequest())
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
			last = a.getInstructor();
		}
		
		out.flush(); out.close();
	}
	
	public int getNbrCells(COLUMN column) {
		return 1;
	}
	
	public boolean isColumnVisible(Column column, boolean hasRequests, int cookie) {
		boolean visible = !column.getColumn().isCanHide() || (cookie & (1 << column.getColumn().ordinal())) != 0;
		if (!hasRequests && column.getColumn().isHasRequest())
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
		default:
			return column.getColumn().name();
		}
	}
	
	public String getCell(TeachingRequestInfo request, final InstructorInfo instructor, Column column) {
		switch (column.getColumn()) {
		case COURSE:
			if (request == null) return null;
			return request.getCourse().getCourseName();
		case SECTION:
			if (request == null) return null;
			String sections = "";
			for (SectionInfo s: request.getSections()) {
				sections += (sections.isEmpty() ? "" : "\n") + s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId());
			}
			return sections;
		case TIME:
			if (request == null) return null;
			String times = "";
			for (SectionInfo s: request.getSections()) {
				times += (times.isEmpty() ? "" : "\n") + (s.getTime() == null ? SECTMSG.arrangeHours() : s.getTime());
			}
			return times;
		case DATE:
			if (request == null) return null;
			String dates = "";
			for (SectionInfo s: request.getSections()) {
				dates += (dates.isEmpty() ? "" : "\n") + (s.getDate() == null ? SECTMSG.noDate() : s.getDate());
			}
			return dates;
		case ROOM:
			if (request == null) return null;
			String rooms = "";
			for (SectionInfo s: request.getSections()) {
				rooms += (rooms.isEmpty() ? "" : "\n") + (s.getRoom() == null ? SECTMSG.noRoom() : s.getRoom());
			}
			return rooms;
		case LOAD:
			if (request == null) return null;
			return Formats.getNumberFormat(CONSTANTS.teachingLoadFormat()).format(request.getLoad());
		case EXTERNAL_ID:
			return (instructor == null ? null : instructor.getExternalId());
		case NAME:
			return (instructor == null ? null : instructor.getInstructorName());
		case ATTRIBUTE_PREFS:
			if (request == null) return null;
			return preferences(request.getAttributePreferences());
		case INSTRUCTOR_PREFS:
			if (request == null) return null;
			return preferences(request.getInstructorPreferences());
		case COURSE_PREF:
			return preferences(instructor.getCoursePreferences());
		case DISTRIBUTION_PREF:
			return preferences(instructor.getDistributionPreferences());
		case TIME_PREF:
			return preferences(instructor.getTimePreferences());
		case ATTRIBUTES:
			String attributes = "";
			for (AttributeInterface attribute: instructor.getAttributes()) {
				attributes += (attributes.isEmpty() ? "" : "\n") + attribute.getName() + (attribute.hasType() ? " (" + attribute.getType().getLabel() + ")" : "");
			}
			return attributes;
		case ASSIGNED_LOAD:
			return Formats.getNumberFormat(CONSTANTS.teachingLoadFormat()).format(instructor.getAssignedLoad()) + " / " + Formats.getNumberFormat(CONSTANTS.teachingLoadFormat()).format(instructor.getMaxLoad());
		case OBJECTIVES:
			if (request == null) return null;
			String objectives = "";
			for (String key: new TreeSet<String>(request.getValues().keySet())) {
				Double value = request.getValues().get(key);
				if (value == null || Math.abs(value) < 0.001) continue;
				objectives += (objectives.isEmpty() ? "" : "\n") + key + ": " + (value > 0.0 ? "+": "") + Formats.getNumberFormat(CONSTANTS.teachingLoadFormat()).format(value);
			}
			return objectives;
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
