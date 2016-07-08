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

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.rooms.RoomCookie;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentChangesRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentChangesResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.ChangesType;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingDisplayMode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class TeachingAssignmentsChangesPage extends SimpleForm {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static final StudentSectioningMessages SECTMSG = GWT.create(StudentSectioningMessages.class);
	protected static NumberFormat sTeachingLoadFormat = NumberFormat.getFormat(CONSTANTS.teachingLoadFormat());
	private UniTimeHeaderPanel iFilterPanel;
	private ListBox iFilter;
	private TeachingRequestsPagePropertiesResponse iProperties;
	private UniTimeTable<SingleTeachingAssingment> iTable;
	private TeachingRequestDetailPage iDetail = null;
	
	public TeachingAssignmentsChangesPage() {
		iFilterPanel = new UniTimeHeaderPanel(MESSAGES.propAssignmentChangesBase());
		iFilter = new ListBox();
		iFilter.setStyleName("unitime-TextBox");
		iFilterPanel.getPanel().insert(iFilter, 2);
		iFilterPanel.getPanel().setCellVerticalAlignment(iFilter, HasVerticalAlignment.ALIGN_MIDDLE);
		iFilter.getElement().getStyle().setMarginLeft(5, Unit.PX);
		for (String base: CONSTANTS.assignmentChangesBase())
			iFilter.addItem(base);
		iFilter.setSelectedIndex(InstructorCookie.getInstance().getAssignmentChangesBase());
		iFilterPanel.addButton("search", MESSAGES.buttonSearch(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				search();
			}
		});
		iFilterPanel.setEnabled("search", false);
		addRow(iFilterPanel);
		
		iTable = new UniTimeTable<SingleTeachingAssingment>();
		iTable.setVisible(false);
		iTable.addStyleName("unitime-TeachingAssignmentChanges");
		addRow(iTable);
		
		iTable.addMouseClickListener(new MouseClickListener<SingleTeachingAssingment>() {
			@Override
			public void onMouseClick(TableEvent<SingleTeachingAssingment> event) {
				if (event.getData() != null) {
					if (iDetail == null) {
						iDetail = new TeachingRequestDetailPage(iProperties) {
							@Override
							protected void onAssignmentChanged(List<AssignmentInfo> assignments) {
								if (iTable.isVisible()) search();
							}
						};
						iDetail.addCloseHandler(new CloseHandler<PopupPanel>() {
							@Override
							public void onClose(CloseEvent<PopupPanel> event) {
								iTable.clearHover();
							}
						});
					}
					iDetail.showRequestDetail(event.getData().getRequest().getRequestId());
				}
			}
		});
		
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingPage());
		RPC.execute(new TeachingRequestsPagePropertiesRequest(), new AsyncCallback<TeachingRequestsPagePropertiesResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilterPanel.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(TeachingRequestsPagePropertiesResponse result) {
				LoadingWidget.getInstance().hide();
				iProperties = result;
				iFilterPanel.setEnabled("search", true);
			}
		});
	}
	
	void search() {
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingTeachingAssignments());
		InstructorCookie.getInstance().setAssignmentChangesBase(iFilter.getSelectedIndex());
		RPC.execute(new AssignmentChangesRequest(ChangesType.values()[iFilter.getSelectedIndex()]), new AsyncCallback<AssignmentChangesResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilterPanel.setErrorMessage(MESSAGES.failedToLoadTeachingAssignments(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadTeachingAssignments(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(AssignmentChangesResponse result) {
				LoadingWidget.getInstance().hide();
				populate(result.getChanges());
				iTable.setVisible(true);
			}
		});
	}
	
	void populate(List<AssignmentInfo> results) {
		iTable.clearTable();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		UniTimeTableHeader sortHeader = null; COLUMN sortColumn = null; boolean asc = true;
		int sort = InstructorCookie.getInstance().getSortAssignmentChangesBy();
		for (final COLUMN column: COLUMN.values()) {
			final UniTimeTableHeader h = getHeader(column);
			h.addOperation(new UniTimeTableHeader.Operation() {
				@Override
				public void execute() {
					iTable.sort(h, new TableComparator(column));
					InstructorCookie.getInstance().setSortAssignmentChangesBy(h.getOrder() ? 1 + column.ordinal() : -1 - column.ordinal());
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
			if (column.getColSpan() > 1)
				for (int i = 1; i < column.getColSpan(); i++)
					header.add(new UniTimeTableHeader("&nbsp;"));
		}
		iTable.addRow(null, header);
		for (AssignmentInfo assignment: results) {
			SingleTeachingAssingment sta = new SingleTeachingAssingment(assignment.getRequest(), assignment.getRequest().getInstructor(assignment.getIndex()), assignment.getInstructor());
			List<Widget> line = new ArrayList<Widget>();
			for (COLUMN column: COLUMN.values()) {
				for (int i = 0; i < column.getColSpan(); i++) {
					Widget cell = getCell(column, i, sta);
					if (cell == null) cell = new Label();
					line.add(cell);
				}
			}
			iTable.addRow(sta, line);
		}
		if (sortHeader != null)
			iTable.sort(sortHeader, new TableComparator(sortColumn), asc);
		for (final COLUMN column: COLUMN.values()) {
			final UniTimeTableHeader h = header.get(column.getColIndex());
			if (column.isCanHide()) {
				header.get(0).getOperations().add(header.get(0).getOperations().size() - 1, new UniTimeTableHeader.Operation() {
					@Override
					public void execute() {
						boolean visible = !InstructorCookie.getInstance().isAssignmentChangesColumnVisible(column.ordinal());
						InstructorCookie.getInstance().setAssignmentChangesColumnVisible(column.ordinal(), visible);
						for (int i = 0; i < column.getColSpan(); i++)
							iTable.setColumnVisible(column.getColIndex() + i, visible);
						if (COLUMN.NAME == column && !visible) {
							InstructorCookie.getInstance().setAssignmentChangesColumnVisible(COLUMN.EXTERNAL_ID.ordinal(), true);
							for (int i = 0; i < column.getColSpan(); i++)
								iTable.setColumnVisible(COLUMN.EXTERNAL_ID.getColIndex() + i, true);
						} else if (COLUMN.EXTERNAL_ID == column && !visible) {
							InstructorCookie.getInstance().setAssignmentChangesColumnVisible(COLUMN.NAME.ordinal(), true);
							for (int i = 0; i < column.getColSpan(); i++)
								iTable.setColumnVisible(COLUMN.NAME.getColIndex() + i, true);
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
						if (InstructorCookie.getInstance().isAssignmentChangesColumnVisible(column.ordinal()))
							return MESSAGES.opHide(h.getHTML().replace("<br>", " "));
						else
							return MESSAGES.opShow(h.getHTML().replace("<br>", " "));
					}
				});
				h.addOperation(new UniTimeTableHeader.Operation() {
					@Override
					public void execute() {
						boolean visible = !InstructorCookie.getInstance().isAssignmentChangesColumnVisible(column.ordinal());
						InstructorCookie.getInstance().setAssignmentChangesColumnVisible(column.ordinal(), visible);
						for (int i = 0; i < column.getColSpan(); i++)
							iTable.setColumnVisible(column.getColIndex() + i, visible);
						if (COLUMN.NAME == column && !visible) {
							InstructorCookie.getInstance().setAssignmentChangesColumnVisible(COLUMN.EXTERNAL_ID.ordinal(), true);
							for (int i = 0; i < column.getColSpan(); i++)
								iTable.setColumnVisible(COLUMN.EXTERNAL_ID.getColIndex() + i, true);
						} else if (COLUMN.EXTERNAL_ID == column && !visible) {
							InstructorCookie.getInstance().setAssignmentChangesColumnVisible(COLUMN.NAME.ordinal(), true);
							for (int i = 0; i < column.getColSpan(); i++)
								iTable.setColumnVisible(COLUMN.NAME.getColIndex() + i, true);
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
						if (InstructorCookie.getInstance().isAssignmentChangesColumnVisible(column.ordinal()))
							return MESSAGES.opHideItem(h.getHTML().replace("<br>", " "));
						else
							return MESSAGES.opShowItem(h.getHTML().replace("<br>", " "));
					}
				});
				for (int i = 0; i < column.getColSpan(); i++)
					iTable.setColumnVisible(column.getColIndex() + i, !column.isCanHide() || InstructorCookie.getInstance().isAssignmentChangesColumnVisible(column.ordinal()));
			}
		}
		hideDuplicateRequests();
	}
	
	public void hideDuplicateRequests() {
		TeachingRequestInfo last = null;
		for (int i = 0; i < iTable.getRowCount(); i++) {
			SingleTeachingAssingment ta = iTable.getData(i);
			if (ta == null) {
				last = null; continue;
			}
			if (ta.getRequest().equals(last)) {
				int col = 0;
				for (final COLUMN column: COLUMN.values()) {
					for (int j = 0; j < column.getColSpan(); j++) {
						iTable.getCellFormatter().setStyleName(i, col, null);
						if (!column.isHasInstructor())
							iTable.getWidget(i, col).setVisible(false);
						col ++;
					}
				}
			} else {
				int col = 0;
				for (final COLUMN column: COLUMN.values()) {
					for (int j = 0; j < column.getColSpan(); j++) {
						iTable.getCellFormatter().setStyleName(i, col, "first-line");
						if (!column.isHasInstructor())
							iTable.getWidget(i, col).setVisible(true);
						col ++;
					}
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
					iTable.refreshTable();
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
					iTable.refreshTable();
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
					iTable.refreshTable();
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
					iTable.refreshTable();
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
							iTable.refreshTable();
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
	
	public Widget getCell(COLUMN column, int index, SingleTeachingAssingment assignment) {
		TeachingRequestInfo request = assignment.getRequest();
		InstructorInfo instructor = assignment.getInstructor();
		InstructorInfo baseInstructor = assignment.getBaseInstructor();
		switch (column) {
		case COURSE:
			return new Label(request.getCourse().getCourseName());
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
			switch (index) {
			case 0:
				InstructorExternalIdCell cell1 = new InstructorExternalIdCell(iProperties);
				cell1.setValue(baseInstructor);
				return cell1;
			case 1:
				return new HTML(MESSAGES.assignmentArrow());
			case 2:
				InstructorExternalIdCell cell2 = new InstructorExternalIdCell(iProperties);
				cell2.setValue(instructor);
				return cell2;
			}
		case NAME:
			switch (index) {
			case 0:
				InstructorNameCell cell1 = new InstructorNameCell(iProperties);
				cell1.setValue(baseInstructor);
				return cell1;
			case 1:
				return new HTML(MESSAGES.assignmentArrow());
			case 2:
				InstructorNameCell cell2 = new InstructorNameCell(iProperties);
				cell2.setValue(instructor);
				return cell2;
			}
		case ATTRIBUTE_PREFS:
			return new PreferenceCell(iProperties, request.getAttributePreferences());
		case INSTRUCTOR_PREFS:
			return new PreferenceCell(iProperties, request.getInstructorPreferences());
		case COURSE_PREF:
			if (instructor != null)
				return new PreferenceCell(iProperties, instructor.getCoursePreferences());
			else if (baseInstructor != null)
				return new PreferenceCell(iProperties, baseInstructor.getCoursePreferences());
			else
				return null;
		case DISTRIBUTION_PREF:
			if (instructor != null)
				return new PreferenceCell(iProperties, instructor.getDistributionPreferences());
			else if (baseInstructor != null)
				return new PreferenceCell(iProperties, baseInstructor.getDistributionPreferences());
			else
				return null;
		case TIME_PREF:
			if (instructor != null)
				return new TimePreferenceCell(iProperties, instructor);
			else if (baseInstructor != null)
				return new TimePreferenceCell(iProperties, baseInstructor);
			else
				return null;
		case ATTRIBUTES:
			if (instructor != null)
				return new AttributesCell(instructor.getAttributes());
			else if (baseInstructor != null)
				return new AttributesCell(baseInstructor.getAttributes());
			else
				return null;
		case ASSIGNED_LOAD:
			if (instructor == null) return null;
			return new Label(sTeachingLoadFormat.format(instructor.getAssignedLoad()) + " / " + sTeachingLoadFormat.format(instructor.getMaxLoad()));
		case OBJECTIVES:
			return new ObjectivesCell(iProperties, baseInstructor == null ? null : baseInstructor.getValues(), instructor == null ? null : instructor.getValues());
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
		EXTERNAL_ID(true, true, 3),
		NAME(true, true, 3),
		ASSIGNED_LOAD(true, true),
		ATTRIBUTES(true, true),
		COURSE_PREF(true, true),
		TIME_PREF(true, true),
		DISTRIBUTION_PREF(true, true),
		OBJECTIVES(true, true),
		;
		
		private boolean iCanHide;
		private boolean iHasInstructor;
		private int iColSpan;
		
		COLUMN(boolean canHide, boolean hasInstructor, int colSpan) { iCanHide = canHide; iHasInstructor = hasInstructor; iColSpan = colSpan; }
		COLUMN(boolean canHide, boolean hasInstructor) { this(canHide, hasInstructor, 1); }
		COLUMN(boolean canHide) { this(canHide, false, 1); }
		
		public boolean isVisible(boolean assigned) {
			return (assigned || !iHasInstructor);
		}
		public boolean isHasInstructor() { return iHasInstructor; }
		public boolean isCanHide() { return iCanHide; }
		public int flag() { return 1 << ordinal(); }
		public int getColSpan() { return iColSpan; }
		public int getColIndex() {
			int ret = 0;
			for (int i = 0; i < ordinal(); i++)
				ret += values()[i].getColSpan();
			return ret;
		}
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
			if (column.isHasInstructor() && i1 == null && i2 == null) {
				i1 = a1.getBaseInstructor(); i2 = a2.getBaseInstructor();
			}
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
	
	public static class SingleTeachingAssingment {
		InstructorInfo iBaseInstructor;
		InstructorInfo iInstructor;
		TeachingRequestInfo iRequest;
		
		public SingleTeachingAssingment(TeachingRequestInfo request, InstructorInfo instructor, InstructorInfo base) {
			iInstructor = instructor; iRequest = request; iBaseInstructor = base;
		}
		
		public boolean hasInstructor() { return iInstructor != null; }
		public InstructorInfo getInstructor() { return iInstructor; }
		public boolean hasBaseInstructor() { return iBaseInstructor != null; }
		public InstructorInfo getBaseInstructor() { return iBaseInstructor; }
		public boolean hasRequest() { return iRequest != null; }
		public TeachingRequestInfo getRequest() { return iRequest; }
	}
}