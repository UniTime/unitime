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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeCourseInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeGroupInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreePlaceHolderInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class DegreePlanTable extends UniTimeTable<Object> implements TakesValue<DegreePlanInterface>{
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	
	private DegreePlanInterface iPlan;
	
	public DegreePlanTable() {
		addStyleName("unitine-DegreePlanTable");
		setAllowSelection(true);
		setAllowMultiSelect(false);
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		UniTimeTableHeader hIndent = new UniTimeTableHeader("");
		hIndent.setWidth("16px");
		header.add(hIndent);
		UniTimeTableHeader hName = new UniTimeTableHeader(MESSAGES.colDegreeItemName(), 2);
		hName.setWidth("120px");
		header.add(hName);
		UniTimeTableHeader hTitle = new UniTimeTableHeader(MESSAGES.colDegreeItemDescription());
		hTitle.setWidth("250px");
		header.add(hTitle);
		UniTimeTableHeader hLimit = new UniTimeTableHeader(MESSAGES.colLimit());
		hLimit.setWidth("70px");
		header.add(hLimit);
		UniTimeTableHeader hCredit = new UniTimeTableHeader(MESSAGES.colCredit());
		hCredit.setWidth("50px");
		header.add(hCredit);
		UniTimeTableHeader hNote = new UniTimeTableHeader(MESSAGES.colNote());
		hNote.setWidth("250px");
		header.add(hNote);
		addRow(null, header);
	}

	@Override
	public DegreePlanInterface getValue() {
		return iPlan;
	}

	@Override
	public void setValue(DegreePlanInterface plan) {
		iPlan = plan;
		clearTable(1);
		if (plan.getGroup() != null)
			addGroup(1, plan.getGroup().getMaxDepth(), plan.getGroup(), null);
	}
	
	protected void addGroup(int depth, int maxDepth, DegreeGroupInterface group, DegreeGroupInterface parent) {
		if (depth > 1) {
			List<Widget> row = new ArrayList<Widget>();
			P indent = new P("indent");
			for (int d = 1; d < depth - 1; d++)
				indent.add(new Image(RESOURCES.indentMiddleLine()));
			indent.add(new Image(RESOURCES.indentTopLine()));
			for (int d = depth + 1; d <= maxDepth; d++)
				indent.add(new Image(RESOURCES.indentTopSpace()));
			row.add(indent);
			if (parent != null && parent.isChoice()) {
				row.add(new ChoiceButton(parent, group));
				row.add(new GroupTitleCell(group.toString(MESSAGES), true));
			} else {
				row.add(new GroupTitleCell(group.toString(MESSAGES), false));
			}
			addRow(group, row);
		}
		if (group.hasCourses()) {
			for (DegreeCourseInterface course: group.getCourses()) {
				if (!course.hasCourses()) {
					List<Widget> row = new ArrayList<Widget>();
					P indent = new P("indent");
					for (int d = 1; d < depth; d++)
						indent.add(new Image(RESOURCES.indentMiddleLine()));
					for (int d = depth + 1; d <= maxDepth; d++)
						indent.add(new Image(RESOURCES.indentBlankSpace()));
					row.add(indent);
					if (group.isChoice()) {
						row.add(new ChoiceButton(group, course));
						row.add(new CourseLabel(MESSAGES.course(course.getSubject(), course.getCourse()), true));
					} else {
						row.add(new CourseLabel(MESSAGES.course(course.getSubject(), course.getCourse()), false));
					}
					row.add(new TitleLabel(course.getTitle() == null ? "" : course.getTitle()));
					row.add(new CourseNotOfferedLabel(MESSAGES.plannedCourseNotOffered(MESSAGES.course(course.getSubject(), course.getCourse()))));
					addRow(course, row);
				} else if (course.getCourses().size() > 1 && !group.isChoice()) {
					List<Widget> row = new ArrayList<Widget>();
					P indent = new P("indent");
					for (int d = 1; d < depth; d++)
						indent.add(new Image(RESOURCES.indentMiddleLine()));
					indent.add(new Image(RESOURCES.indentTopLine()));
					for (int d = depth + 2; d <= maxDepth; d++)
						indent.add(new Image(RESOURCES.indentTopSpace()));
					row.add(indent);
					if (group.isChoice() && !course.hasCourses()) {
						row.add(new ChoiceButton(group, course));
						row.add(new GroupTitleCell(course.hasTitle() ? MESSAGES.courseNameWithTitle(course.getSubject(), course.getCourse(), course.getTitle()) : MESSAGES.course(course.getSubject(), course.getCourse()), true));
					} else {
						row.add(new GroupTitleCell(course.hasTitle() ? MESSAGES.courseNameWithTitle(course.getSubject(), course.getCourse(), course.getTitle()) : MESSAGES.course(course.getSubject(), course.getCourse()), false));
					}
					addRow(course, row);
				}
				if (course.hasCourses()) {
					for (Iterator<CourseAssignment> i = course.getCourses().iterator(); i.hasNext(); ) {
						CourseAssignment ca = i.next();
						List<Widget> row = new ArrayList<Widget>();
						P indent = new P("indent");
						for (int d = 1; d < depth; d++)
							indent.add(new Image(RESOURCES.indentMiddleLine()));
						if (course.getCourses().size() == 1 || group.isChoice())
							indent.add(new Image(RESOURCES.indentBlankSpace()));
						else if (i.hasNext())
							indent.add(new Image(RESOURCES.indentMiddleLine()));
						else
							indent.add(new Image(RESOURCES.indentLastLine()));
						for (int d = depth + 2; d <= maxDepth; d++)
							indent.add(new Image(RESOURCES.indentBlankSpace()));
						row.add(indent);
						if (group.isChoice()) {
							row.add(new ChoiceButton(group, course, ca));
							row.add(new CourseLabel(MESSAGES.course(ca.getSubject(), ca.getCourseNbr()), true));
						} else if (course.hasMultipleCourses()) {
							row.add(new ChoiceButton(course, ca));
							row.add(new CourseLabel(MESSAGES.course(ca.getSubject(), ca.getCourseNbr()), true));
						} else {
							row.add(new CourseLabel(MESSAGES.course(ca.getSubject(), ca.getCourseNbr()), false));
						}
						row.add(new TitleLabel(ca.getTitle() == null ? "" : ca.getTitle()));
						row.add(new HTML(ca.getLimit() == null || ca.getLimit() == 0 || ca.getEnrollment() == null ? "" : ca.getLimit() < 0 ? "&infin;" : (ca.getLimit() - ca.getEnrollment()) + " / " + ca.getLimit(), false));
						row.add(new Label(ca.hasCredit() ? ca.getCreditAbbv() : "", false));
						row.add(new NoteCell(ca.getNote()));
						addRow(ca, row);
					}
				}
			}
		}
		if (group.hasGroups()) {
			for (DegreeGroupInterface g: group.getGroups())
				addGroup(depth + 1, maxDepth, g, group);
		}
		if (group.hasPlaceHolders()) {
			for (DegreePlaceHolderInterface p: group.getPlaceHolders()) {
				List<Widget> row = new ArrayList<Widget>();
				P indent = new P("indent");
				for (int d = 1; d < depth; d++)
					indent.add(new Image(RESOURCES.indentMiddleLine()));
				for (int d = depth + 1; d <= maxDepth; d++)
					indent.add(new Image(RESOURCES.indentBlankSpace()));
				row.add(indent);
				row.add(new PlaceHolderCell(p.getName()));
				addRow(p, row);
			}
		}
		if (depth > 1 && getRowCount() > 1) {
			P indent = (P)getWidget(getRowCount() - 1, 0);
			indent.remove(depth - 2);
			indent.insert(new Image(RESOURCES.indentLastLine()), depth - 2);
		}
	}
	
	public static class GroupTitleCell extends Label implements UniTimeTable.HasColSpan {
		private boolean iHasChoice;
		
		public GroupTitleCell(String label, boolean hasChoice) {
			super(label, false);
			addStyleName("grouplabel");
			iHasChoice = hasChoice;
			setTitle(label);
		}

		@Override
		public int getColSpan() { return (iHasChoice ? 5 : 6); }
	}
	
	public static class CourseLabel extends Label implements UniTimeTable.HasColSpan {
		private boolean iHasChoice;
		
		public CourseLabel(String label, boolean hasChoice) {
			super(label, false);
			addStyleName("course");
			iHasChoice = hasChoice;
		}

		@Override
		public int getColSpan() { return (iHasChoice ? 1 : 2); }
	}
	
	public static class CourseNotOfferedLabel extends Label implements UniTimeTable.HasColSpan {

		public CourseNotOfferedLabel(String label) {
			super(label, false);
			addStyleName("error");
		}

		@Override
		public int getColSpan() { return 3; }
	}
	
	public static class TitleLabel extends Label {
		
		public TitleLabel(String label) {
			super(label, false);
			addStyleName("title");
		}
	}
	
	public static class PlaceHolderCell extends Label implements UniTimeTable.HasColSpan {
		public PlaceHolderCell(String label) {
			super(label, false);
			addStyleName("placeholder");
			setTitle(label);
		}

		@Override
		public int getColSpan() { return 6; }
	}
	
	public static class ChoiceButton extends RadioButton {
		public ChoiceButton(final DegreeGroupInterface parent, final DegreeCourseInterface course) {
			super(parent.getId(), "");
			setValue(course.isSelected());
			setTitle(MESSAGES.hintChoiceGroupSelection(MESSAGES.course(course.getSubject(), course.getCourse())));
			addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (parent.hasCourses()) {
						for (DegreeCourseInterface c: parent.getCourses())
							c.setSelected(course.getId().equals(c.getId()) ? event.getValue() : false);
					}
					if (parent.hasGroups()) {
						for (DegreeGroupInterface g: parent.getGroups()) {
							g.setSelected(false);
						}
					}
				}
			});
		}
		
		public ChoiceButton(final DegreeGroupInterface parent, final DegreeGroupInterface group) {
			super(parent.getId(), "");
			setValue(group.isSelected());
			setTitle(MESSAGES.hintChoiceGroupSelection(group.toString(MESSAGES)));
			addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (parent.hasCourses()) {
						for (DegreeCourseInterface c: parent.getCourses())
							c.setSelected(false);
					}
					if (parent.hasGroups()) {
						for (DegreeGroupInterface g: parent.getGroups()) {
							g.setSelected(group.getId().equals(g.getId()) ? event.getValue() : false);
						}
					}
				}
			});
		}
		
		public ChoiceButton(final DegreeCourseInterface parent, final CourseAssignment course) {
			super(parent.getId(), "");
			setTitle(MESSAGES.hintChoiceGroupSelection(MESSAGES.course(course.getSubject(), course.getCourseNbr())));
			setValue(course.getCourseId().equals(parent.getCourseId()));
			addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (event.getValue()) {
						parent.setCourseId(course.getCourseId());
						parent.setName(course.getCourseName());
					} else {
						parent.setCourseId(null);
						parent.setName(null);
					}
				}
			});
		}
		
		public ChoiceButton(final DegreeGroupInterface group, final DegreeCourseInterface parent, final CourseAssignment course) {
			super(group.getId(), "");
			setTitle(MESSAGES.hintChoiceGroupSelection(MESSAGES.course(course.getSubject(), course.getCourseNbr())));
			setValue(course.getCourseId().equals(parent.getCourseId()) && parent.isSelected());
			addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (event.getValue()) {
						parent.setCourseId(course.getCourseId());
						parent.setName(course.getCourseName());
					} else {
						parent.setCourseId(null);
						parent.setName(null);
					}
					if (group.hasCourses()) {
						for (DegreeCourseInterface c: group.getCourses())
							c.setSelected(parent.getId().equals(c.getId()) ? event.getValue() : false);
					}
					if (group.hasGroups()) {
						for (DegreeGroupInterface g: group.getGroups()) {
							g.setSelected(false);
						}
					}
				}
			});
		}
	}
	
	public static class NoteCell extends Label {
		public NoteCell(String label) {
			super(label == null ? "" : label, false);
			if (label != null && !label.isEmpty())
				setTitle(label);
			addStyleName("note");
		}
	}

}
