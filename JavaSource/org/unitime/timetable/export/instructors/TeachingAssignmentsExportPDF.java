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
import java.util.List;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.export.PDFPrinter.F;
import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsTable.COLUMN;
import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsPage.SingleTeachingAssingment;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingAssignmentsPageRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.webutil.RequiredTimeTable;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:teaching-assignments.pdf")
public class TeachingAssignmentsExportPDF extends TeachingAssignmentsExportCSV {
	@Override
	public String reference() { return "teaching-assignments.pdf"; }

	protected void export(TeachingAssignmentsPageRequest request, List<SingleTeachingAssingment> list, ExportHelper helper, boolean hasRequests, int cookie) throws IOException {
		PDFPrinter out = new PDFPrinter(helper.getOutputStream(), false);
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
		
		boolean vertical = true;
		if (helper.getParameter("vertical") != null)
			vertical = "1".equals(helper.getParameter("vertical"));
		else
			vertical = RequiredTimeTable.getTimeGridVertical(helper.getSessionContext().getUser());
		String mode = helper.getParameter("mode");
		if (mode == null)
			mode = RequiredTimeTable.getTimeGridSize(helper.getSessionContext().getUser());
		boolean grid = true;
		if (helper.getParameter("grid") != null)
			grid = "1".equals(helper.getParameter("grid"));
		else
			grid = !RequiredTimeTable.getTimeGridAsText(helper.getSessionContext().getUser());

		InstructorInfo last = null;
		for (SingleTeachingAssingment a: list) {
			A[] row = new A[columns.size()];
			if (last != null && last.equals(a.getInstructor())) {
				for (int i = 0; i < columns.size(); i++) {
					if (columns.get(i).getColumn().isHasRequest()) {
						row[i] = getPdfCell(a.getRequest(), a.getInstructor(), columns.get(i), grid, vertical, mode);
						if (row[i] == null) row[i] = new A();
					} else {
						row[i] = new A();
					}
					row[i].set(F.NOSEPARATOR);
				}
			} else {
				for (int i = 0; i < columns.size(); i++)
					row[i] = getPdfCell(a.getRequest(), a.getInstructor(), columns.get(i), grid, vertical, mode);
			}
			out.printLine(row);
			out.flush();
			last = a.getInstructor();
		}
		
		if (list.isEmpty()) {
			out.printLine(new A[]{ new A(MESSAGES.errorNoData(), F.ITALIC).color("#FF0000") });
		}
		
		out.flush(); out.close();
	}
	
	public A getPdfCell(TeachingRequestInfo request, final InstructorInfo instructor, Column column, boolean grid, boolean vertical, String mode) {
		switch (column.getColumn()) {
		case COURSE:
			if (request == null) return null;
			return new A(request.getCourse().getCourseName());
		case SECTION:
			if (request == null) return null;
			A sections = new A();
			for (SectionInfo s: request.getSections()) {
				if (s.isCommon())
					sections.add(new A(s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId()), F.ITALIC));
				else
					sections.add(new A(s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId())));
			}
			return sections;
		case TIME:
			if (request == null) return null;
			A times = new A();
			for (SectionInfo s: request.getSections()) {
				if (s.isCommon())
					times.add(new A(s.getTime() == null ? SECTMSG.arrangeHours() : s.getTime(), F.ITALIC));
				else
					times.add(new A(s.getTime() == null ? SECTMSG.arrangeHours() : s.getTime()));
			}
			return times;
		case DATE:
			if (request == null) return null;
			A dates = new A();
			for (SectionInfo s: request.getSections()) {
				if (s.isCommon())
					dates.add(new A(s.getDate() == null ? SECTMSG.noDate() : s.getDate(), F.ITALIC));
				else
					dates.add(new A(s.getDate() == null ? SECTMSG.noDate() : s.getDate()));
			}
			return dates;
		case ROOM:
			if (request == null) return null;
			A rooms = new A();
			for (SectionInfo s: request.getSections()) {
				if (s.isCommon())
					rooms.add(new A(s.getRoom() == null ? SECTMSG.noRoom() : s.getRoom(), F.ITALIC));
				else
					rooms.add(new A(s.getRoom() == null ? SECTMSG.noRoom() : s.getRoom()));
			}
			return rooms;
		case EXTERNAL_ID:
			if (instructor == null || instructor.getExternalId() == null) return null;
			A extId = new A(instructor.getExternalId());
			if (instructor.getTeachingPreference() != null && !"0".equals(instructor.getTeachingPreference()))
				extId.setColor(PreferenceLevel.prolog2color(instructor.getTeachingPreference()));
			return extId;
		case NAME:
			if (instructor == null || instructor.getInstructorName() == null) return null;
			A name = new A(instructor.getInstructorName());
			if (instructor.getTeachingPreference() != null && !"0".equals(instructor.getTeachingPreference()))
				name.setColor(PreferenceLevel.prolog2color(instructor.getTeachingPreference()));
			return name;
		case ATTRIBUTE_PREFS:
			if (request == null) return null;
			return pdfPreferences(request.getAttributePreferences());
		case INSTRUCTOR_PREFS:
			if (request == null) return null;
			return pdfPreferences(request.getInstructorPreferences());
		case COURSE_PREF:
			if (instructor == null) return null;
			return pdfPreferences(instructor.getCoursePreferences());
		case DISTRIBUTION_PREF:
			if (instructor == null) return null;
			return pdfPreferences(instructor.getDistributionPreferences());
		case TIME_PREF:
			if (instructor == null) return null;
			if (grid && instructor.getAvailability() != null && !instructor.getAvailability().isEmpty()) {
				RequiredTimeTable rtt = new RequiredTimeTable(new TimePattern().getTimePatternModel());
				rtt.getModel().setPreferences(instructor.getAvailability());
				try {
					rtt.getModel().setDefaultSelection(Integer.parseInt(mode));
				} catch (NumberFormatException e) {
					rtt.getModel().setDefaultSelection(mode);
				}
				return new A(rtt.createBufferedImage(vertical, false));				
			} else {
				return pdfPreferences(instructor.getTimePreferences());
			}
		case ATTRIBUTES:
			if (instructor == null) return null;
			A attributes = new A();
			for (AttributeInterface attribute: instructor.getAttributes()) {
				attributes.add(new A(attribute.getName() + (attribute.hasType() ? " (" + attribute.getType().getLabel() + ")" : "")));
			}
			return attributes;
		case OBJECTIVES:
			if (request == null) return null;
			A objectives = new A();
			for (String key: new TreeSet<String>(request.getValues().keySet())) {
				Double value = request.getValues().get(key);
				if (value == null || Math.abs(value) < 0.001) continue;
				A obj = new A(key + ": " + (value > 0.0 ? "+": "") + Formats.getNumberFormat(CONSTANTS.teachingLoadFormat()).format(value));
				if (key.endsWith(" Preferences")) {
					if (value <= -50.0) {
						obj.setColor(PreferenceLevel.prolog2color("R"));
					} else if (value <= -2.0) {
						obj.setColor(PreferenceLevel.prolog2color("-2"));
					} else if (value < 0.0) {
						obj.setColor(PreferenceLevel.prolog2color("-1"));
					} else if (value >= 50.0) {
						obj.setColor(PreferenceLevel.prolog2color("P"));
					} else if (value >= 2.0) {
						obj.setColor(PreferenceLevel.prolog2color("2"));
					} else if (value > 0.0) {
						obj.setColor(PreferenceLevel.prolog2color("1"));
					}
				} else if (value < 0.0) {
					obj.setColor("#0f821e");
				} else if (value > 0.0) {
					obj.setColor("#c81e14");
				}
				objectives.add(obj);
			}
			return objectives;
		default:
			return new A(getCell(request, instructor, column));
		}
	}
	
	public A pdfPreferences(List<PreferenceInfo> list) {
		if (list == null || list.isEmpty()) return null;
		A ret = new A();
		for (PreferenceInfo pref: list) {
			ret.add(new A(pref.getOwnerName()).color(PreferenceLevel.prolog2color(pref.getPreference())));
		}
		return ret;
	}
}
