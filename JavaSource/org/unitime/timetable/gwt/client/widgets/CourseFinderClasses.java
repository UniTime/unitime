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
package org.unitime.timetable.gwt.client.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.aria.AriaCheckBox;
import org.unitime.timetable.gwt.client.aria.AriaHiddenLabel;
import org.unitime.timetable.gwt.client.rooms.RoomHint;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.IdValue;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class CourseFinderClasses extends UniTimeTable<ClassAssignment> implements CourseFinder.CourseFinderCourseDetails<CourseAssignment, Collection<ClassAssignment>> {
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	private CourseAssignment iValue = null;
	private DataProvider<CourseAssignment, Collection<ClassAssignment>> iDataProvider = null;
	private Set<Preference> iSelectedClasses = new HashSet<Preference>();
	private SpecialRegistrationContext iSpecReg;
	private CheckBox iRequired = null;
	
	public CourseFinderClasses(boolean allowSelection) {
		this(allowSelection, null, null);
	}
	
	public CourseFinderClasses(boolean allowSelection, SpecialRegistrationContext specreg) {
		this(allowSelection, specreg, null);
	}
	
	public CourseFinderClasses(boolean allowSelection, SpecialRegistrationContext specreg, CheckBox required) {
		super();
		setAllowSelection(allowSelection);
		iSpecReg = specreg;
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		if (isAllowSelection())
			header.add(new UniTimeTableHeader(MESSAGES.colClassSelection()));
		header.add(new UniTimeTableHeader(MESSAGES.colSubpart()));
		header.add(new UniTimeTableHeader(MESSAGES.colClass()));
		header.add(new UniTimeTableHeader(MESSAGES.colLimit()));
		header.add(new UniTimeTableHeader(MESSAGES.colDays()));
		header.add(new UniTimeTableHeader(MESSAGES.colStart()));
		header.add(new UniTimeTableHeader(MESSAGES.colEnd()));
		header.add(new UniTimeTableHeader(MESSAGES.colDate()));
		header.add(new UniTimeTableHeader(MESSAGES.colRoom()));
		header.add(new UniTimeTableHeader(MESSAGES.colInstructor()));
		header.add(new UniTimeTableHeader(MESSAGES.colParent()));
		header.add(new UniTimeTableHeader(MESSAGES.colHighDemand()));
		header.add(new UniTimeTableHeader(MESSAGES.colNoteIcon()));
		addRow(null, header);
		setEmptyMessage(MESSAGES.courseSelectionNoCourseSelected());
		if (isAllowSelection()) {
			addMouseClickListener(new UniTimeTable.MouseClickListener<ClassAssignment>() {
				@Override
				public void onMouseClick(UniTimeTable.TableEvent<ClassAssignment> event) {
					selectClass(event.getRow(), isSelected(event.getRow()));
				}
			});
			addMouseDoubleClickListener(new UniTimeTable.MouseDoubleClickListener<ClassAssignmentInterface.ClassAssignment>() {
				@Override
				public void onMouseDoubleClick(UniTimeTable.TableEvent<ClassAssignmentInterface.ClassAssignment> event) {
					selectClass(event.getRow(), isSelected(event.getRow()));
				}
			});
			iRequired = required;
			if (iRequired != null)
				iRequired.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						for (Preference p: iSelectedClasses)
							p.setRequired(event.getValue());
					}
				});
		}
	}
	
	protected boolean isSpecialRegistration() {
		return iSpecReg != null && iSpecReg.isEnabled();
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setValue(CourseAssignment value) {
		if (value == null) {
			iValue = value;
			clearTable(1);
			setEmptyMessage(MESSAGES.courseSelectionNoCourseSelected());
		} else if (!value.equals(iValue)) {
			iValue = value;
			clearTable(1);
			setEmptyMessage(MESSAGES.courseSelectionLoadingClasses());
			iDataProvider.getData(value, new AsyncCallback<Collection<ClassAssignment>>() {
				public void onFailure(Throwable caught) {
					clearTable(1);
					setEmptyMessage(caught.getMessage());
				}
				public void onSuccess(Collection<ClassAssignment> result) {
					clearTable(1);
					if (!result.isEmpty()) {
						Long lastSubpartId = null;
						for (final ClassAssignment clazz: result) {
							final Preference p = getSelection(clazz);
							List<Widget> line = new ArrayList<Widget>();
							if (isAllowSelection()) {
								if (!clazz.isCancelled() && (clazz.isSaved() || clazz.isAvailable() || isSpecialRegistration())) {
									AriaCheckBox ch = new Selection();
									ch.setValue(iSelectedClasses.contains(p));
									ch.setAriaLabel(ARIA.courseFinderPreferClass(MESSAGES.clazz(clazz.getSubject(), clazz.getCourseNbr(), clazz.getSubpart(), clazz.getSection())));
									ch.setEnabled(isEnabled());
									ch.addClickHandler(new ClickHandler() {
										@Override
										public void onClick(ClickEvent event) {
											event.stopPropagation();
										}
									});
									ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
										@Override
										public void onValueChange(ValueChangeEvent<Boolean> event) {
											setSelected(getRow(clazz.getClassId()), event.getValue());
											if (event.getValue())
												iSelectedClasses.add(p);
											else
												iSelectedClasses.remove(p);
										}
									});
									line.add(ch);									
								} else {
									line.add(new HTML("&nbsp;"));
								}
							}
							line.add(new Label(clazz.getSubpart(), false));
							line.add(new Label(clazz.getSection(), false));
							line.add(new HTML(clazz.getLimitString(), false));
							if (clazz.isAssigned()) {
								line.add(new Label(clazz.getDaysString(CONSTANTS.shortDays()), false));
								line.add(new Label(clazz.getStartString(CONSTANTS.useAmPm()), false));
								line.add(new Label(clazz.getEndString(CONSTANTS.useAmPm()), false));
							} else {
								line.add(new ArrangeHours());
							}
							line.add(new Label(clazz.hasDatePattern() ? clazz.getDatePattern() : "", false));
							line.add(new Rooms(clazz.getRooms(), ","));
							line.add(new RoomsOrInstructors(clazz.getInstructors(), ","));
							line.add(new Label(clazz.getParentSection() != null ? clazz.getParentSection() : ""));
							line.add(clazz.isSaved() ? new WebTable.IconCell(RESOURCES.saved(), MESSAGES.saved(clazz.getSubpart() + " " + clazz.getSection()), null).getWidget() :
								clazz.isCancelled() ? new WebTable.IconCell(RESOURCES.cancelled(), MESSAGES.classCancelled(clazz.getSubpart() + " " + clazz.getSection()), null).getWidget() :
								clazz.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()), null).getWidget() :
								new Label());
							line.add(clazz.hasNote() && !clazz.getNote().equals(iValue.getNote()) ? new WebTable.IconCell(RESOURCES.note(), clazz.getNote(), "").getWidget() : new Label());
							if (!clazz.isSaved() && !clazz.isAvailable() && !isSpecialRegistration())
								line.add(new AriaHiddenLabel(ARIA.courseFinderClassNotAvailable(
										MESSAGES.clazz(clazz.getSubject(), clazz.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
										clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours())));
							else
								line.add(new AriaHiddenLabel(ARIA.courseFinderClassAvailable(
										MESSAGES.clazz(clazz.getSubject(), clazz.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
										clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours(),
										clazz.getLimitString())));
							int row = addRow(clazz, line);
							if (lastSubpartId != null && !clazz.getSubpartId().equals(lastSubpartId))
								for (int c = 0; c < getCellCount(row); c++)
									getCellFormatter().addStyleName(row, c, "top-border-dashed");
							if (clazz.isCancelled() || (!clazz.isSaved() && !clazz.isAvailable()))
								for (int c = 0; c < getCellCount(row); c++)
									getCellFormatter().addStyleName(row, c, "text-gray");
							if (isAllowSelection() && !clazz.isCancelled() && (clazz.isSaved() || clazz.isAvailable() || isSpecialRegistration()) && iSelectedClasses.contains(p))
								setSelected(row, true);
							lastSubpartId = clazz.getSubpartId();
						}
					} else {
						setEmptyMessage(MESSAGES.courseSelectionNoClasses(MESSAGES.courseName(iValue.getSubject(), iValue.getCourseNbr())));
					}
				}
			});
		}
	}

	@Override
	public CourseAssignment getValue() {
		return iValue;
	}

	@Override
	public void setDataProvider(DataProvider<CourseAssignment, Collection<ClassAssignment>> provider) {
		iDataProvider = provider;
	}

	@Override
	public String getName() {
		return MESSAGES.courseSelectionClasses();
	}
	
	public static class RoomsOrInstructors extends P {
		public RoomsOrInstructors(List<String> list, String delimiter) {
			super("itemize");
			if (list != null)
				for (Iterator<String> i = list.iterator(); i.hasNext(); ) {
					P p = new P(DOM.createSpan(), "item");
					p.setText(i.next() + (i.hasNext() ? delimiter : ""));
					add(p);
				}
		}	
	}
	
	public static class Rooms extends P {
		public Rooms(List<IdValue> list, String delimiter) {
			super("itemize");
			if (list != null)
				for (Iterator<IdValue> i = list.iterator(); i.hasNext(); ) {
					final P p = new P(DOM.createSpan(), "item");
					final IdValue room = i.next();
					p.setText(room.getValue() + (i.hasNext() ? delimiter : ""));
					if (room.getId() != null) {
						p.addMouseOverHandler(new MouseOverHandler() {
							@Override
							public void onMouseOver(MouseOverEvent event) {
								RoomHint.showHint(p.getElement(), room.getId(), null, null, true);
							}
						});
						p.addMouseOutHandler(new MouseOutHandler() {
							@Override
							public void onMouseOut(MouseOutEvent event) {
								RoomHint.hideHint();
							}
						});
					}
					add(p);
				}
		}	
	}
	
	public static class ArrangeHours extends HTML implements UniTimeTable.HasColSpan {
		public ArrangeHours() {
			super(MESSAGES.arrangeHours(), false);
		}
		@Override
		public int getColSpan() {
			return 3;
		}
	}
	
	public Boolean isClassSelected(int row) {
		Widget w = getWidget(row, 0);
		if (w != null && w instanceof CheckBox) {
			return ((CheckBox)w).getValue();
		} else {
			return null;
		}
	}
	
	public boolean isClassSelected(Long classId) {
		if (!isAllowSelection()) return false;
		for (int row = 1; row < getRowCount(); row++ ) {
			ClassAssignment record = getData(row);
			if (record != null && record.getClassId().equals(classId))
				return isClassSelected(row);
		}
		return false;
	}
	
	public int getRow(Long classId) {
		for (int row = 1; row < getRowCount(); row++ ) {
			ClassAssignment record = getData(row);
			if (record != null && record.getClassId().equals(classId))
				return row;
		}
		return -1;
	}
	
	public void selectClass(Long classId, boolean value) {
		if (!isAllowSelection()) return;
		for (int row = 1; row < getRowCount(); row++ ) {
			ClassAssignment record = getData(row);
			if (record != null && record.getClassId().equals(classId)) {
				selectClass(row, value);
				break;
			}
		}
	}
	
	protected boolean isSelectedClassRequired(Long id) {
		if (id == null) return false;
		for (Preference p: iSelectedClasses)
			if (p.getId().equals(id)) return p.isRequired();
		return iRequired != null && iRequired.isEnabled() && iRequired.getValue();
	}
	
	protected Preference getSelection(ClassAssignment a) {
		return a.getSelection(isSelectedClassRequired(a.getClassId()));
	}
	
	public void selectClass(int row, boolean value) {
		Widget w = getWidget(row, 0);
		if (w != null && w instanceof CheckBox) {
			((CheckBox)w).setValue(value);
			ClassAssignment a = getData(row);
			if (value)
				iSelectedClasses.add(getSelection(a));
			else
				iSelectedClasses.remove(getSelection(a));
		}
	}
	
	public CheckBox getClassSelection(int row) {
		Widget w = getWidget(row, 0);
		if (w != null && w instanceof CheckBox) {
			return (CheckBox)w;
		} else {
			return null;
		}
	}
	
	@Override
	public boolean isCanSelectRow(int row) {
		return getClassSelection(row) != null;
	}
	
	@Override
	public void onSetValue(RequestedCourse course) {
		iSelectedClasses.clear();
		if (course != null && course.hasSelectedClasses()) {
			iSelectedClasses.addAll(course.getSelectedClasses());
			for (Preference p: course.getSelectedClasses())
				if (p.isRequired() && iRequired != null) iRequired.setValue(true);
		}
		for (int row = 1; row < getRowCount(); row++) {
			ClassAssignment a = getData(row);
			CheckBox ch = getClassSelection(row);
			if (ch != null && a != null) {
				ch.setValue(iSelectedClasses.contains(getSelection(a)));
				setSelected(row, iSelectedClasses.contains(getSelection(a)));
			}
		}
	}

	@Override
	public void onGetValue(RequestedCourse course) {
		course.setSelectedClasses(null);
		for (int row = 1; row < getRowCount(); row++) {
			ClassAssignment clazz = getData(row);
			if (clazz == null) continue;
			Widget w = getWidget(row, 0);
			if (w != null && w instanceof CheckBox && ((CheckBox)w).getValue())
				course.setSelectedClass(getSelection(clazz), true);
		}
	}
	
	public static class Selection extends AriaCheckBox implements UniTimeTable.HasCellAlignment {
		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_CENTER;
		}
	}
	
	public Set<Preference> getAllSelectedClasses() {
		return iSelectedClasses;
	}
}