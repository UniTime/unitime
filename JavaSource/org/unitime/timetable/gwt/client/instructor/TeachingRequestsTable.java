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
package org.unitime.timetable.gwt.client.instructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsPage.SingleTeachingAssingment;
import org.unitime.timetable.gwt.client.rooms.RoomCookie;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingDisplayMode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class TeachingRequestsTable extends UniTimeTable<SingleTeachingAssingment> {
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final StudentSectioningMessages SECTMSG = GWT.create(StudentSectioningMessages.class);
	protected static NumberFormat sTeachingLoadFormat = NumberFormat.getFormat(CONSTANTS.teachingLoadFormat());
	
	private TeachingRequestDetailPage iDetail = null;
	private TeachingRequestsPagePropertiesResponse iProperties;
	private Boolean iAssigned;
	
	public TeachingRequestsTable() {
		this(null);
	}
	
	public TeachingRequestsTable(Boolean assigned) {
		iAssigned = assigned;
		addStyleName("unitime-TeachingRequests");
		addMouseClickListener(new MouseClickListener<SingleTeachingAssingment>() {
			@Override
			public void onMouseClick(TableEvent<SingleTeachingAssingment> event) {
				if (event.getData() != null && iProperties != null) {
					if (iDetail == null) {
						iDetail = new TeachingRequestDetailPage(iProperties) {
							@Override
							protected void onAssignmentChanged(List<AssignmentInfo> assignments) {
								onAssignmentChanged(assignments);
							}
						};
						iDetail.addCloseHandler(new CloseHandler<PopupPanel>() {
							@Override
							public void onClose(CloseEvent<PopupPanel> event) {
								clearHover();
							}
						});
					}
					iDetail.showRequestDetail(event.getData().getRequest().getRequestId());
				}
			}
		});
	}
	
	protected void onAssignmentChanged(List<AssignmentInfo> assignments) {}
	
	public void setProperties(TeachingRequestsPagePropertiesResponse properties) {
		iProperties = properties;
	}
	
	void populate(GwtRpcResponseList<TeachingRequestInfo> results) {
		clearTable();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		UniTimeTableHeader sortHeader = null; COLUMN sortColumn = null; boolean asc = true;
		int sort = InstructorCookie.getInstance().getSortTeachingRequestsBy(iAssigned);
		for (final COLUMN column: COLUMN.values())
			if (column.isVisible(iAssigned)) {
				final UniTimeTableHeader h = getHeader(column);
				h.addOperation(new UniTimeTableHeader.Operation() {
					@Override
					public void execute() {
						sort(h, new TableComparator(column));
						InstructorCookie.getInstance().setSortTeachingRequestsBy(iAssigned, h.getOrder() ? 1 + column.ordinal() : -1 - column.ordinal());
						hideDuplicateRequests();
					}
					@Override
					public boolean isApplicable() {
						return true;
					}
					@Override
					public boolean hasSeparator() {
						return true;
					}
					@Override
					public String getName() {
						return MESSAGES.opSortBy(h.getHTML().replace("<br>", " "));
					}
				});
				header.add(h);
				if (sort != 0 && Math.abs(sort) - 1 == column.ordinal()) {
					sortHeader = h; sortColumn = column; asc = sort > 0;
				}
			}
		addRow(null, header);
		boolean hasInstructor = false;
		for (TeachingRequestInfo request: results) {
			if (iAssigned == null) {
				if (request.hasInstructors()) {
					for (InstructorInfo instructor: request.getInstructors()) {
						hasInstructor = true;
						List<Widget> line = new ArrayList<Widget>();
						for (COLUMN column: COLUMN.values()) {
							if (column.isVisible(iAssigned)) {
								Widget cell = getCell(column, request, instructor);
								if (cell == null) cell = new Label();
								line.add(cell);
							}
						}
						addRow(new SingleTeachingAssingment(instructor, request), line);
					}
				} else {
					List<Widget> line = new ArrayList<Widget>();
					for (COLUMN column: COLUMN.values()) {
						if (column.isVisible(iAssigned)) {
							Widget cell = getCell(column, request, null);
							if (cell == null) cell = new Label();
							line.add(cell);
						}
					}
					addRow(new SingleTeachingAssingment(null, request), line);
				}
			} else if (iAssigned && request.hasInstructors())
				for (InstructorInfo instructor: request.getInstructors()) {
					List<Widget> line = new ArrayList<Widget>();
					for (COLUMN column: COLUMN.values()) {
						if (column.isVisible(iAssigned)) {
							Widget cell = getCell(column, request, instructor);
							if (cell == null) cell = new Label();
							line.add(cell);
						}
					}
					addRow(new SingleTeachingAssingment(instructor, request), line);
				}
			else if (!iAssigned && request.getNrAssignedInstructors() < request.getNrInstructors()) {
				List<Widget> line = new ArrayList<Widget>();
				for (COLUMN column: COLUMN.values()) {
					if (column.isVisible(iAssigned)) {
						Widget cell = getCell(column, request, null);
						if (cell == null) cell = new Label();
						line.add(cell);
					}
				}
				addRow(new SingleTeachingAssingment(null, request), line);
			}
		}
		if (sortHeader != null)
			sort(sortHeader, new TableComparator(sortColumn), asc);
		int col = 0;
		for (final COLUMN column: COLUMN.values())
			if (column.isVisible(iAssigned)) {
				final UniTimeTableHeader h = header.get(col);
				final int colIdx = col;
				if (column.isCanHide()) {
					header.get(0).getOperations().add(header.get(0).getOperations().size() - 1, new UniTimeTableHeader.Operation() {
						@Override
						public void execute() {
							boolean visible = !InstructorCookie.getInstance().isTeachingRequestsColumnVisible(iAssigned, column.ordinal());
							InstructorCookie.getInstance().setTeachingRequestsColumnVisible(iAssigned, column.ordinal(), visible);
							setColumnVisible(colIdx, visible);
							if (COLUMN.NAME == column && !visible) {
								InstructorCookie.getInstance().setTeachingRequestsColumnVisible(iAssigned, COLUMN.EXTERNAL_ID.ordinal(), true);
								setColumnVisible(colIdx - 1, true);
							} else if (COLUMN.EXTERNAL_ID == column && !visible) {
								InstructorCookie.getInstance().setTeachingRequestsColumnVisible(iAssigned, COLUMN.NAME.ordinal(), true);
								setColumnVisible(colIdx + 1, true);
							}
						}
						@Override
						public boolean isApplicable() {
							return true;
						}
						@Override
						public boolean hasSeparator() {
							return false;
						}
						@Override
						public String getName() {
							if (InstructorCookie.getInstance().isTeachingRequestsColumnVisible(iAssigned, column.ordinal()))
								return MESSAGES.opHide(h.getHTML().replace("<br>", " "));
							else
								return MESSAGES.opShow(h.getHTML().replace("<br>", " "));
						}
					});
					h.addOperation(new UniTimeTableHeader.Operation() {
						@Override
						public void execute() {
							boolean visible = !InstructorCookie.getInstance().isTeachingRequestsColumnVisible(iAssigned, column.ordinal());
							InstructorCookie.getInstance().setTeachingRequestsColumnVisible(iAssigned, column.ordinal(), visible);
							setColumnVisible(colIdx, visible);
							if (COLUMN.NAME == column && !visible) {
								InstructorCookie.getInstance().setTeachingRequestsColumnVisible(iAssigned, COLUMN.EXTERNAL_ID.ordinal(), true);
								setColumnVisible(colIdx - 1, true);
							} else if (COLUMN.EXTERNAL_ID == column && !visible) {
								InstructorCookie.getInstance().setTeachingRequestsColumnVisible(iAssigned, COLUMN.NAME.ordinal(), true);
								setColumnVisible(colIdx + 1, true);
							}
						}
						@Override
						public boolean isApplicable() {
							return true;
						}
						@Override
						public boolean hasSeparator() {
							return true;
						}
						@Override
						public String getName() {
							if (InstructorCookie.getInstance().isTeachingRequestsColumnVisible(iAssigned, column.ordinal()))
								return MESSAGES.opHideItem(h.getHTML().replace("<br>", " "));
							else
								return MESSAGES.opShowItem(h.getHTML().replace("<br>", " "));
						}
					});
							
				}
				setColumnVisible(col, !column.isCanHide() || InstructorCookie.getInstance().isTeachingRequestsColumnVisible(iAssigned, column.ordinal()));
				if (iAssigned == null && !hasInstructor && column.isCanHide() && column.isHasInstructor())
					setColumnVisible(col, false);
				col ++;
			}
		hideDuplicateRequests();
	}
	
	public void hideDuplicateRequests() {
		if (iAssigned != null && !iAssigned) return;
		TeachingRequestInfo last = null;
		for (int i = 0; i < getRowCount(); i++) {
			SingleTeachingAssingment ta = getData(i);
			if (ta == null) {
				last = null; continue;
			}
			if (ta.getRequest().equals(last)) {
				for (final COLUMN column: COLUMN.values()) {
					getCellFormatter().setStyleName(i, column.ordinal(), null);
					if (!column.isHasInstructor())
						getWidget(i, column.ordinal()).setVisible(false);
				}
			} else {
				for (final COLUMN column: COLUMN.values()) {
					getCellFormatter().setStyleName(i, column.ordinal(), "first-line");
					if (!column.isHasInstructor())
						getWidget(i, column.ordinal()).setVisible(true);
				}
			}
			last = ta.getRequest();
		}
	}
	
	public UniTimeTableHeader getHeader(COLUMN column) {
		switch (column) {
		case COURSE:
			return new UniTimeTableHeader(MESSAGES.colCourse());
		case SECTION:
			return new UniTimeTableHeader(MESSAGES.colSection());
		case TIME:
			return new UniTimeTableHeader(MESSAGES.colTime());
		case DATE:
			return new UniTimeTableHeader(MESSAGES.colDate());
		case ROOM:
			return new UniTimeTableHeader(MESSAGES.colRoom());
		case LOAD:
			return new UniTimeTableHeader(MESSAGES.colTeachingLoad());
		case ATTRIBUTE_PREFS:
			return new UniTimeTableHeader(MESSAGES.colAttributePreferences());
		case INSTRUCTOR_PREFS:
			return new UniTimeTableHeader(MESSAGES.colInstructorPreferences());
		case EXTERNAL_ID:
			return new UniTimeTableHeader(MESSAGES.colExternalId());
		case NAME:
			return new UniTimeTableHeader(MESSAGES.colNamePerson());
		case ATTRIBUTES:
			return new UniTimeTableHeader(MESSAGES.colAttributes());
		case COURSE_PREF:
			return new UniTimeTableHeader(MESSAGES.colCoursePreferences());
		case DISTRIBUTION_PREF:
			return new UniTimeTableHeader(MESSAGES.colDistributionPreferences());
		case TIME_PREF:
			UniTimeTableHeader timePrefHeader = new UniTimeTableHeader(MESSAGES.colTimePreferences());
			timePrefHeader.addOperation(new UniTimeTableHeader.Operation() {
				@Override
				public void execute() {
					RoomCookie.getInstance().setOrientation(true, RoomCookie.getInstance().areRoomsHorizontal());
					refreshTable();
				}
				@Override
				public boolean isApplicable() { return !RoomCookie.getInstance().isGridAsText(); }
				@Override
				public boolean hasSeparator() { return false; }
				@Override
				public String getName() { return MESSAGES.opOrientationAsText(); }
			});
			timePrefHeader.addOperation(new UniTimeTableHeader.Operation() {
				@Override
				public void execute() {
					RoomCookie.getInstance().setOrientation(false, RoomCookie.getInstance().areRoomsHorizontal());
					refreshTable();
				}
				@Override
				public boolean isApplicable() { return RoomCookie.getInstance().isGridAsText(); }
				@Override
				public boolean hasSeparator() { return false; }
				@Override
				public String getName() { return MESSAGES.opOrientationAsGrid(); }
			});
			timePrefHeader.addOperation(new UniTimeTableHeader.Operation() {
				@Override
				public void execute() {
					RoomCookie.getInstance().setOrientation(false, true);
					refreshTable();
				}
				@Override
				public boolean isApplicable() { return !RoomCookie.getInstance().isGridAsText() && !RoomCookie.getInstance().areRoomsHorizontal(); }
				@Override
				public boolean hasSeparator() { return false; }
				@Override
				public String getName() { return MESSAGES.opOrientationHorizontal(); }
			});
			timePrefHeader.addOperation(new UniTimeTableHeader.Operation() {
				@Override
				public void execute() {
					RoomCookie.getInstance().setOrientation(false, false);
					refreshTable();
				}
				@Override
				public boolean isApplicable() { return !RoomCookie.getInstance().isGridAsText() && RoomCookie.getInstance().areRoomsHorizontal(); }
				@Override
				public boolean hasSeparator() { return false; }
				@Override
				public String getName() { return MESSAGES.opOrientationVertical(); }
			});
			if (iProperties != null && iProperties.hasModes() && !RoomCookie.getInstance().isGridAsText()) {
				for (int i = 0; i < iProperties.getModes().size(); i++) {
					final RoomSharingDisplayMode mode = iProperties.getModes().get(i);
					final int index = i;
					timePrefHeader.addOperation(new UniTimeTableHeader.Operation() {
						@Override
						public void execute() {
							RoomCookie.getInstance().setMode(RoomCookie.getInstance().areRoomsHorizontal(), mode.toHex());
							refreshTable();
						}
						@Override
						public boolean isApplicable() { return !RoomCookie.getInstance().isGridAsText() && !mode.toHex().equals(RoomCookie.getInstance().getMode()); }
						@Override
						public boolean hasSeparator() { return (index == 0 || (index == 1 && iProperties.getModes().get(0).toHex().equals(RoomCookie.getInstance().getMode()))); }
						@Override
						public String getName() { return mode.getName(); }
					});
				}
			}
			return timePrefHeader;
		case ASSIGNED_LOAD:
			return new UniTimeTableHeader(MESSAGES.colAssignedLoad());
		case OBJECTIVES:
			return new UniTimeTableHeader(MESSAGES.colObjectives());
		case ASSIGNED_INSTRUCTORS:
			return new UniTimeTableHeader(MESSAGES.colAssignedInstructors());
		default:
			return new UniTimeTableHeader(column.name());
		}
	}
	
	public Widget getCell(COLUMN column, final TeachingRequestInfo request, final InstructorInfo instructor) {
		switch (column) {
		case COURSE:
			return new Label(request.getCourse().getCourseName(), false);
		case SECTION:
			P p = new P("sections");
			for (SectionInfo s: request.getSections()) {
				P i = new P("section");
				i.setText(s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId()));
				if (s.isCommon()) i.addStyleName("common");
				p.add(i);
			}
			return p;
		case TIME:
			p = new P("times");
			for (SectionInfo s: request.getSections()) {
				P i = new P("time");
				i.setHTML(s.getTime() == null ? SECTMSG.arrangeHours() : s.getTime());
				if (s.isCommon()) i.addStyleName("common");
				p.add(i);
			}
			return p;
		case DATE:
			p = new P("dates");
			for (SectionInfo s: request.getSections()) {
				P i = new P("date");
				i.setHTML(s.getDate() == null ? SECTMSG.noDate() : s.getDate());
				if (s.isCommon()) i.addStyleName("common");
				p.add(i);
			}
			return p;
		case ROOM:
			p = new P("rooms");
			for (SectionInfo s: request.getSections()) {
				P i = new P("room");
				i.setHTML(s.getRoom() == null ? SECTMSG.noRoom() : s.getRoom());
				if (s.isCommon()) i.addStyleName("common");
				p.add(i);
			}
			return p;
		case LOAD:
			return new Label(sTeachingLoadFormat.format(request.getLoad()));
		case EXTERNAL_ID:
			if (instructor == null || instructor.getExternalId() == null) return null;
			Label extId = new Label(instructor.getExternalId());
			if (instructor.getTeachingPreference() != null && !"0".equals(instructor.getTeachingPreference())) {
				PreferenceInterface pref = iProperties.getPreference(instructor.getTeachingPreference());
				if (pref != null) {
					extId.setTitle(pref.getName() + " " + instructor.getExternalId());
					extId.getElement().getStyle().setColor(pref.getColor());
				}
			}
			return extId;
		case NAME:
			if (instructor == null || instructor.getInstructorName() == null) return null;
			Label name = new Label(instructor.getInstructorName());
			if (instructor.getTeachingPreference() != null && !"0".equals(instructor.getTeachingPreference())) {
				PreferenceInterface pref = iProperties.getPreference(instructor.getTeachingPreference());
				if (pref != null) {
					name.setTitle(pref.getName() + " " + instructor.getInstructorName());
					name.getElement().getStyle().setColor(pref.getColor());
				}
			}
			return name;
		case ATTRIBUTE_PREFS:
			return new PreferenceCell(iProperties, request.getAttributePreferences());
		case INSTRUCTOR_PREFS:
			return new PreferenceCell(iProperties, request.getInstructorPreferences());
		case COURSE_PREF:
			if (instructor == null) return null;
			return new PreferenceCell(iProperties, instructor.getCoursePreferences());
		case DISTRIBUTION_PREF:
			if (instructor == null) return null;
			return new PreferenceCell(iProperties, instructor.getDistributionPreferences());
		case TIME_PREF:
			if (instructor == null) return null;
			return new TimePreferenceCell(iProperties, instructor);
		case ATTRIBUTES:
			if (instructor == null) return null;
			return new AttributesCell(instructor.getAttributes());
		case ASSIGNED_LOAD:
			if (instructor == null) return null;
			return new Label(sTeachingLoadFormat.format(instructor.getAssignedLoad()) + " / " + sTeachingLoadFormat.format(instructor.getMaxLoad()));
		case OBJECTIVES:
			if (instructor == null) return null;
			return new ObjectivesCell(iProperties, instructor.getValues());
		case ASSIGNED_INSTRUCTORS:
			return new Label(request.getNrAssignedInstructors() + " / " + request.getNrInstructors());
		default:
			return null;
		}
	}
	
	public static enum COLUMN {
		COURSE(false),
		SECTION(false),
		TIME(true),
		DATE(true),
		ROOM(true),
		LOAD(true),
		ASSIGNED_INSTRUCTORS(true),
		ATTRIBUTE_PREFS(true),
		INSTRUCTOR_PREFS(true),
		EXTERNAL_ID(true, true),
		NAME(true, true),
		ASSIGNED_LOAD(true, true),
		ATTRIBUTES(true, true),
		COURSE_PREF(true, true),
		TIME_PREF(true, true),
		DISTRIBUTION_PREF(true, true),
		OBJECTIVES(true, true),
		;
		
		private boolean iCanHide;
		private boolean iHasInstructor;
		
		COLUMN(boolean canHide, boolean hasInstructor) { iCanHide = canHide; iHasInstructor = hasInstructor; }
		COLUMN(boolean canHide) { this(canHide, false); }
		
		public boolean isVisible(Boolean assigned) {
			return (assigned == null || assigned || !iHasInstructor);
		}
		public boolean isHasInstructor() { return iHasInstructor; }
		public boolean isCanHide() { return iCanHide; }
		public int flag() { return 1 << ordinal(); }
	}
	
	public static class TableComparator implements Comparator<SingleTeachingAssingment> {
		private COLUMN iColumn;
		
		public TableComparator(COLUMN column) {
			iColumn = column;
		}
		
		protected int compareSections(COLUMN column, SectionInfo s1, SectionInfo s2) {
			switch (column) {
			case SECTION:
				return compareOthers(s1, s2);
			case TIME:
				return compareStrings(s1.getTime(), s2.getTime());
			case DATE:
				return compareStrings(s1.getDate(), s2.getDate());
			case ROOM:
				return compareStrings(s1.getRoom(), s2.getRoom());
			default:
				return 0;
			}
		}
		
		protected int comparePreferences(List<PreferenceInfo> p1, List<PreferenceInfo> p2) {
			Iterator<PreferenceInfo> i1 = p1.iterator();
			Iterator<PreferenceInfo> i2 = p2.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				int cmp = i1.next().compareTo(i2.next());
				if (cmp != 0) return cmp;
			}
			if (i2.hasNext()) return -1;
			if (i1.hasNext()) return 1;
			return (i1.hasNext() ? 1 : i2.hasNext() ? -1 : 0);
		}
		
		private int compareByColumn(COLUMN column, SingleTeachingAssingment a1, SingleTeachingAssingment a2) {
			TeachingRequestInfo r1 = a1.getRequest(), r2 = a2.getRequest();
			InstructorInfo i1 = a1.getInstructor(), i2 = a2.getInstructor();
			if (column.isHasInstructor() && (i1 == null || i2 == null))
				return compareBooleans(i1 == null, i2 == null);
			switch (column) {
			case COURSE:
				return compareOthers(r1.getCourse(), r2.getCourse());
			case SECTION:
			case TIME:
			case DATE:
			case ROOM:
				Iterator<SectionInfo> it1 = r1.getSections().iterator();
				Iterator<SectionInfo> it2 = r2.getSections().iterator();
				while (it1.hasNext() && it2.hasNext()) {
					int cmp = compareSections(column, it1.next(), it2.next());
					if (cmp != 0) return cmp;
				}
				if (it2.hasNext()) return -1;
				if (it1.hasNext()) return 1;
				return (it1.hasNext() ? 1 : it2.hasNext() ? -1 : 0);
			case ASSIGNED_LOAD:
				int cmp = compareNumbers(i1.getAssignedLoad(), i2.getAssignedLoad());
				if (cmp != 0) return cmp;
				return compareNumbers(i1.getMaxLoad(), i2.getMaxLoad());
			case NAME:
				return compareStrings(i1.getInstructorName(), i2.getInstructorName());
			case EXTERNAL_ID:
				return compareStrings(i1.getExternalId(), i2.getExternalId());
			case LOAD:
				return compareNumbers(r1.getLoad(), r2.getLoad());
			case OBJECTIVES:
				TreeSet<String> keys = new TreeSet<String>(i1.getValues().keySet());
				keys.addAll(i2.getValues().keySet());
				for (String key: keys) {
					Double d1 = i1.getValues().get(key);
					Double d2 = i2.getValues().get(key);
					cmp = compareNumbers(d1, d2);
					if (cmp != 0) return cmp;
				}
				return 0;
			case ATTRIBUTES:
				TreeSet<String> attributes = new TreeSet<String>();
				for (AttributeInterface a: i1.getAttributes()) attributes.add(a.getName());
				for (AttributeInterface a: i2.getAttributes()) attributes.add(a.getName());
				for (String a: attributes) {
					cmp = compareBooleans(i1.hasAttribute(a), i2.hasAttribute(a));
					if (cmp != 0) return cmp;
				}
				return 0;
			case ATTRIBUTE_PREFS:
				return comparePreferences(r1.getAttributePreferences(), r2.getAttributePreferences());
			case COURSE_PREF:
				return comparePreferences(i1.getCoursePreferences(), i2.getCoursePreferences());
			case INSTRUCTOR_PREFS:
				return comparePreferences(r1.getInstructorPreferences(), r2.getInstructorPreferences());
			case DISTRIBUTION_PREF:
				return comparePreferences(i1.getDistributionPreferences(), i2.getDistributionPreferences());
			case TIME_PREF:
				return comparePreferences(i1.getTimePreferences(), i2.getTimePreferences());
			case ASSIGNED_INSTRUCTORS:
				cmp = compareNumbers(r1.getNrAssignedInstructors(), r2.getNrAssignedInstructors());
				if (cmp != 0) return cmp;
				return -compareNumbers(r1.getNrInstructors(), r2.getNrInstructors());
			default:
				return 0;
			}
		}
		
		@Override
		public int compare(SingleTeachingAssingment a1, SingleTeachingAssingment a2) {
			int cmp = compareByColumn(iColumn, a1, a2);
			if (cmp != 0) return cmp;
			return a1.getRequest().compareTo(a2.getRequest());
		}

		protected int compareStrings(String s1, String s2) {
			if (s1 == null || s1.isEmpty()) {
				return (s2 == null || s2.isEmpty() ? 0 : 1);
			} else {
				return (s2 == null || s2.isEmpty() ? -1 : s1.compareToIgnoreCase(s2));
			}
		}
		
		protected int compareNumbers(Number n1, Number n2) {
			return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? 1 : Double.compare(n1.doubleValue(), n2.doubleValue())); 
		}
		
		protected int compareBooleans(Boolean b1, Boolean b2) {
			return (b1 == null ? b2 == null ? 0 : -1 : b2 == null ? 1 : (b1.booleanValue() == b2.booleanValue()) ? 0 : (b1.booleanValue() ? 1 : -1));
		}
		
		protected int compareOthers(Comparable c1, Comparable c2) {
			return (c1 == null ? c2 == null ? 0 : -1 : c2 == null ? 1 : c1.compareTo(c2));
		}
	}
}
