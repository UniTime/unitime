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

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.AdminCookie;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.CourseRequirement;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.CustomField;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorRequirementData;
import org.unitime.timetable.gwt.client.rooms.RoomsTable.SortOperation;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class InstructorRequirementsTable extends UniTimeTable<CourseRequirement> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private List<CustomField> iCustomFields;
	private boolean iCrossList;
	private Column iSortBy = null;
	private boolean iAsc = true;
	private int iCustom = 0;
	
	public InstructorRequirementsTable(final InstructorRequirementData data) {
		iCustomFields = data.getCustomFields();
		iCrossList = data.isCrossList();
		addStyleName("unitime-InstructorSurveyCourseTable");
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (final Column col: Column.values()) {
			for (int i = 0; i < getColSpan(col); i++) {
				final UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(col, i));
				final int index = i;
				if (CourseRequirementComparator.isApplicable(col)) {
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
		setSortBy(AdminCookie.getInstance().getSortCourseRequirements());
		
		setData(data.getInstructorRequirements());
		
		addMouseClickListener(new MouseClickListener<InstructorSurveyInterface.CourseRequirement>() {
			@Override
			public void onMouseClick(TableEvent<CourseRequirement> event) {
				if (event.getData() == null) return;
				if (data.isAdmin())
					UniTimeFrameDialog.openDialog(
							MESSAGES.sectInstructorSurvey(),
							"instructorSurvey?menu=hide&id=" + event.getData().getExternalId(),
							"900", "90%");
				else if (event.getData().getInstructorId() != null)
					ToolBox.open("instructorDetail.action?instructorId=" + event.getData().getInstructorId());
			}
		});
	}
	
	public void addRow(CourseRequirement course) {
		List<Widget> line = new ArrayList<Widget>();
		for (Column col: Column.values())
			for (int i = 0; i < getColSpan(col); i++)
				line.add(getColumnWidget(col, course, i));
		addRow(course, line);
	}
	
	public void setData(List<CourseRequirement> courses) {
		clearTable(1);
		if (courses != null)
			for (CourseRequirement course: courses)
				addRow(course);
		sort();
	}
	
	public int getColSpan(Column column) {
		switch (column) {
		case CUSTOM:
			return (iCustomFields == null ? 0 : iCustomFields.size());
		case COURSE:
			return (iCrossList ? 1 : 0);
		default:
			return 1;
		}
	}
	
	public String getColumnName(Column column, int index) {
		switch (column) {
		case COURSE: return MESSAGES.colCourse();
		case INSTRUCTOR: return MESSAGES.colInstructor();
		case CUSTOM: return iCustomFields.get(index).getName();
		case DIST: return MESSAGES.colDistribution();
		case TIME: return MESSAGES.colTime();
		case ROOM: return MESSAGES.colRoom();
		case OTHER: return MESSAGES.colOtherPref();
		default: return column.name();
		}
	}
	
	public Widget getColumnWidget(Column column, final CourseRequirement course, final int index) {
		switch (column) {
		case COURSE:
			Label courseLabel = new Label(course.getCourseName());
			if (course.hasCourseTitle())
				courseLabel.setTitle(course.getCourseTitle());
			return courseLabel;
		case INSTRUCTOR:
			Label instructorLabel = new Label(course.getInstructorName());
			return instructorLabel;
		case ROOM:
			HTML roomHtml = new HTML(course.getRoomHtml() == null ? "" : course.getRoomHtml());
			roomHtml.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
			return roomHtml;
		case TIME:
			HTML timeHtml = new HTML(course.getTimeHtml() == null ? "" : course.getTimeHtml());
			timeHtml.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
			return timeHtml;
		case DIST:
			HTML distHtml = new HTML(course.getDistHtml() == null ? "" : course.getDistHtml());
			distHtml.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
			return distHtml;
		case OTHER:
			Label otherLabel = new Label(course.getNote() == null ? "" : course.getNote());
			otherLabel.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
			return otherLabel;
		case CUSTOM:
			final CustomField cf = iCustomFields.get(index);
			Label custom = new Label();
			if (course != null && course.hasCustomField(cf))
				custom.setText(course.getCustomField(cf));
			custom.getElement().getStyle().setProperty("max-width", 6.77 * cf.getLength(), Unit.PX);
			custom.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
			return custom;
		default:
			return null;
		}
	}
	
	protected void doSort(Column column, int index) {
		if (column == iSortBy && index == iCustom) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iCustom = index;
			iAsc = true;
		}
		AdminCookie.getInstance().setSortCourseRequirements(getSortBy());
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() {
		if (iSortBy == Column.CUSTOM) {
			return iAsc ? Column.values().length + iCustom + 1 : -Column.values().length - iCustom - 1;
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
			if (sortBy >= Column.values().length) {
				iCustom = sortBy - Column.values().length - 1;
				iSortBy = Column.CUSTOM;
				iAsc = true;
			} else {
				iSortBy = Column.values()[sortBy - 1];
				iAsc = true;
				iCustom = 0;
			}
		} else {
			if (-sortBy >= Column.values().length) {
				iCustom = -sortBy - Column.values().length - 1;
				iSortBy = Column.CUSTOM;
				iAsc = true;
			} else {
				iSortBy = Column.values()[-1 - sortBy];
				iAsc = false;
				iCustom = 0;
			}
		}
		sort();
	}
	
	public UniTimeTableHeader getHeader(Column column, int index) {
		int offset = 0;
		for (Column c: Column.values()) {
			if (c == column) break;
			offset += getColSpan(c);
		}
		return getHeader(offset + index);
	}
	
	public void sort() {
		if (iSortBy == null) return;
		UniTimeTableHeader header = getHeader(iSortBy, iCustom);
		CustomField cf = (iCustom >= 0 && iCustomFields != null && iCustom < iCustomFields.size() ? iCustomFields.get(iCustom) : null);
		sort(header, new CourseRequirementComparator(iSortBy, cf, true), iAsc);
	}

	public static enum Column {
		INSTRUCTOR,
		COURSE,
		CUSTOM,
		TIME,
		ROOM,
		DIST,
		OTHER,
	}
	
	public static class CourseRequirementComparator implements Comparator<CourseRequirement>{
		private Column iColumn;
		private boolean iAsc;
		private CustomField iCustomField;
		
		public CourseRequirementComparator(Column column, CustomField cf, boolean asc) {
			iColumn = column;
			iAsc = asc;
			iCustomField = cf;
		}
		
		public int compareById(CourseRequirement r1, CourseRequirement r2) {
			return compare(r1.getId(), r2.getId());
		}
		
		public int compareByName(CourseRequirement r1, CourseRequirement r2) {
			return compare(r1.getCourseName(), r2.getCourseName());
		}
		
		public int compareByCustom(CourseRequirement r1, CourseRequirement r2) {
			if (iCustomField != null)
				return compare(r1.getCustomField(iCustomField), r2.getCustomField(iCustomField));
			else
				return 0;
		}
		
		public int compareByOther(CourseRequirement r1, CourseRequirement r2) {
			return compare(r1.getNote(), r2.getNote());
		}
		
		public int compareByInstructor(CourseRequirement r1, CourseRequirement r2) {
			return compare(r1.getInstructorName(), r2.getInstructorName());
		}
		
		public int compareByDist(CourseRequirement r1, CourseRequirement r2) {
			return compare(r1.getDist(), r2.getDist());
		}
		
		public int compareByTime(CourseRequirement r1, CourseRequirement r2) {
			return compare(r1.getTime(), r2.getTime());
		}
		
		public int compareByRoom(CourseRequirement r1, CourseRequirement r2) {
			return compare(r1.getRoom(), r2.getRoom());
		}

		protected int compareByColumn(CourseRequirement r1, CourseRequirement r2) {
			switch (iColumn) {
			case COURSE: return compareByName(r1, r2);
			case CUSTOM: return compareByCustom(r1, r2);
			case DIST: return compareByDist(r1, r2);
			case TIME: return compareByTime(r1, r2);
			case ROOM: return compareByRoom(r1, r2);
			case INSTRUCTOR: return compareByInstructor(r1, r2);
			case OTHER: return compareByOther(r1, r2);
			default: return compareByName(r1, r2);
			}
		}
		
		public static boolean isApplicable(Column column) {
			switch (column) {
			case COURSE:
			case CUSTOM:
			case DIST:
			case INSTRUCTOR:
			case OTHER:
			case ROOM:
			case TIME:
				return true;
			default:
				return false;
			}
		}
		
		@Override
		public int compare(CourseRequirement r1, CourseRequirement r2) {
			int cmp = compareByColumn(r1, r2);
			if (cmp == 0) cmp = compareByInstructor(r1, r2);
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