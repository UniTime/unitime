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
package org.unitime.timetable.gwt.client.instructor.survey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.gwt.client.admin.AdminCookie;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Course;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.CourseColumn;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.CustomField;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyPage.Note;
import org.unitime.timetable.gwt.client.rooms.RoomsTable.SortOperation;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionEvent;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionHandler;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class InstructorSurveyCourseTable extends UniTimeTable<Course> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private CourseColumn iSortBy = null;
	private boolean iAsc = true;
	private int iCustom = 0;
	private List<CustomField> iCustomFields;
	private boolean iEditable = true;
	private Long iSessionId;
	
	public InstructorSurveyCourseTable(Long sessionId, List<CustomField> cf, boolean editable) {
		iSessionId = sessionId;
		iCustomFields = cf;
		iEditable = editable;
		addStyleName("unitime-InstructorSurveyCourseTable");
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (final CourseColumn col: CourseColumn.values()) {
			for (int i = 0; i < getColSpan(col); i++) {
				final UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(col, i));
				final int index = i;
				if (CourseComparator.isApplicable(col)) {
					Operation op = new SortOperation() {
						@Override
						public void execute() { doSort(col, index); }
						@Override
						public boolean isApplicable() { return getRowCount() > 1 && h.isVisible(); }
						@Override
						public boolean hasSeparator() { return false; }
						@Override
						public String getName() { return MESSAGES.opSortBy(getColumnName()); }
						@Override
						public String getColumnName() { return h.getHTML().replace("<br>", " "); }
					};
					h.addOperation(op);
				}
				header.add(h);
			}
		}
		addRow(null, header);
		
		for (int i = 0; i < getCellCount(0); i++)
			getCellFormatter().setStyleName(0, i, "unitime-ClickableTableHeader");
		
		setSortBy(AdminCookie.getInstance().getSortSurveyCourses());
	}
	
	public int getColSpan(CourseColumn column) {
		switch (column) {
		case CUSTOM:
			return (iCustomFields == null ? 0 : iCustomFields.size());
		default:
			return 1;
		}
	}
	
	public String getColumnName(CourseColumn column, int index) {
		switch (column) {
		case COURSE: return MESSAGES.colCourse();
		case CUSTOM: return iCustomFields.get(index).getName();
		default: return column.name();
		}
	}
	
	public Widget getColumnWidget(CourseColumn column, final Course course, final int index) {
		switch (column) {
		case COURSE:
			final CourseSelectionBox box = new CourseSelectionBox(iSessionId);
			box.addStyleName("course-box");
			box.setValue(course);
			box.addCourseSelectionHandler(new CourseSelectionHandler() {
				@Override
				public void onCourseSelection(CourseSelectionEvent event) {
					if (event.getValue() == null) {
						course.setId(null);
						course.setCourseName(null);
						course.setCourseTitle(null);
					} else {
						course.setId(event.getValue().getCourseId());
						course.setCourseName(event.getValue().getCourseName());
						course.setCourseTitle(event.getValue().getCourseTitle());
						int row = getRowForWidget(box);
						if (row == getRowCount() - 1) {
							addRow(new Course());
						}
					}
				}
			});
			box.setEnabled(iEditable);
			return box;
		case CUSTOM:
			final CustomField cf = iCustomFields.get(index);
			if (iEditable) {
				Note custom = new Note();
				custom.setCharacterWidth(cf.getLength());
				custom.getElement().setAttribute("maxlength", "2048");
				custom.addStyleName("custom-" + cf.getId());
				custom.setEnabled(iEditable);
				if (course != null && course.hasCustomField(cf)) {
					custom.setText(course.getCustomField(cf));
					custom.resizeNotes();
				}
				custom.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						course.setCustomField(cf, event.getValue());
					}
				});
				return custom;
			} else {
				Label custom = new Label();
				if (course != null && course.hasCustomField(cf))
					custom.setText(course.getCustomField(cf));
				custom.getElement().getStyle().setWidth(6.77 * cf.getLength(), Unit.PX);
				custom.addStyleName("readonly-note");
				return custom;
			}
		default:
			return null;
		}
	}
	
	public void addRow(Course course) {
		List<Widget> line = new ArrayList<Widget>();
		for (CourseColumn col: CourseColumn.values())
			for (int i = 0; i < getColSpan(col); i++)
				line.add(getColumnWidget(col, course, i));
		addRow(course, line);
	}
	
	public void setData(List<Course> courses) {
		clearTable(1);
		if (courses != null)
			for (Course course: courses)
				addRow(course);
		sort();
	}
	
	protected void doSort(CourseColumn column, int index) {
		if (column == iSortBy && index == iCustom) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iCustom = index;
			iAsc = true;
		}
		AdminCookie.getInstance().setSortSurveyCourses(getSortBy());
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() {
		if (iSortBy == CourseColumn.CUSTOM) {
			return iAsc ? CourseColumn.values().length + 1 + iCustom : -CourseColumn.values().length - iCustom - 1;
		} else {
			return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal();
		}
	}
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
			iCustom = 0;
		} else if (sortBy > 0) {
			if (sortBy >= CourseColumn.values().length) {
				iCustom = sortBy - CourseColumn.values().length - 1;
				iSortBy = CourseColumn.CUSTOM;
				iAsc = true;
			} else {
				iSortBy = CourseColumn.values()[sortBy - 1];
				iAsc = true;
				iCustom = 0;
			}
		} else {
			if (-sortBy >= CourseColumn.values().length) {
				iCustom = -sortBy - CourseColumn.values().length - 1;
				iSortBy = CourseColumn.CUSTOM;
				iAsc = false;
			} else {
				iSortBy = CourseColumn.values()[-1 - sortBy];
				iAsc = false;
				iCustom = 0;
			}
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		UniTimeTableHeader header = getHeader(iSortBy.ordinal());
		if (iSortBy == CourseColumn.CUSTOM)
			header = getHeader(CourseColumn.CUSTOM.ordinal() + iCustom);
		CustomField cf = (iCustom >= 0 && iCustomFields != null && iCustom < iCustomFields.size() ? iCustomFields.get(iCustom) : null);
		sort(header, new CourseComparator(iSortBy, cf, true), iAsc);
	}

	public static class CourseComparator implements Comparator<Course>{
		private CourseColumn iColumn;
		private boolean iAsc;
		private CustomField iCustomField;
		
		public CourseComparator(CourseColumn column, CustomField cf, boolean asc) {
			iColumn = column;
			iAsc = asc;
			iCustomField = cf;
		}
		
		public int compareById(Course r1, Course r2) {
			return compare(r1.getId(), r2.getId());
		}
		
		public int compareByName(Course r1, Course r2) {
			return compare(r1.getCourseName(), r2.getCourseName());
		}
		
		public int compareByCustom(Course r1, Course r2) {
			if (iCustomField != null)
				return compare(r1.getCustomField(iCustomField), r2.getCustomField(iCustomField));
			else
				return 0;
		}

		protected int compareByColumn(Course r1, Course r2) {
			switch (iColumn) {
			case COURSE: return compareByName(r1, r2);
			case CUSTOM: return compareByCustom(r1, r2);
			default: return compareByName(r1, r2);
			}
		}
		
		public static boolean isApplicable(CourseColumn column) {
			switch (column) {
			case COURSE:
			case CUSTOM:
				return true;
			default:
				return false;
			}
		}
		
		@Override
		public int compare(Course r1, Course r2) {
			if (r1.isEmpty()) return (r2.isEmpty() ? 0 : 1);
			if (r2.isEmpty()) return -1;
			int cmp = compareByColumn(r1, r2);
			if (cmp == 0) cmp = compareByName(r1, r2);
			if (cmp == 0) cmp = compareByCustom(r1, r2);
			if (cmp == 0) cmp = compareById(r1, r2);
			return (iAsc ? cmp : -cmp);
		}
		
		protected int compare(String s1, String s2) {
			if (s1 == null || s1.isEmpty()) {
				return (s2 == null || s2.isEmpty() ? 0 : 1);
			} else {
				return (s2 == null || s2.isEmpty() ? -1 : s1.compareToIgnoreCase(s2));
			}
		}
		
		protected int compare(Number n1, Number n2) {
			return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? 1 : Double.compare(n1.doubleValue(), n2.doubleValue())); 
		}
	}
}
