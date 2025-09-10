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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.sectioning.DegreePlanDialog.AssignmentProvider;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestPriority;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeCourseInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeGroupInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeMultiSelectionInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreePlaceHolderInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class DegreePlanTable extends UniTimeTable<Object> implements TakesValue<DegreePlanInterface>{
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	
	private DegreePlanInterface iPlan;
	private TakesValue<CourseRequestInterface> iRequests;
	private AssignmentProvider iAssignments;
	
	public DegreePlanTable(StudentSectioningPage.Mode mode, TakesValue<CourseRequestInterface> requests, AssignmentProvider assignments) {
		iRequests = requests;
		iAssignments = assignments;
		addStyleName("unitine-DegreePlanTable");
		setAllowSelection(true);
		setAllowMultiSelect(false);
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		UniTimeTableHeader hIndent = new UniTimeTableHeader("");
		header.add(hIndent);
		UniTimeTableHeader hName = new UniTimeTableHeader(MESSAGES.colDegreeItemName(), 2);
		header.add(hName);
		UniTimeTableHeader hTitle = new UniTimeTableHeader(MESSAGES.colDegreeItemDescription());
		header.add(hTitle);
		UniTimeTableHeader hLimit = new UniTimeTableHeader(MESSAGES.colLimit());
		header.add(hLimit);
		UniTimeTableHeader hCredit = new UniTimeTableHeader(MESSAGES.colCredit());
		header.add(hCredit);
		UniTimeTableHeader hNote = new UniTimeTableHeader(MESSAGES.colNote());
		header.add(hNote);
		UniTimeTableHeader hReq = new UniTimeTableHeader(MESSAGES.colRequestPriority(), 3);
		header.add(hReq);
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
		if (plan.getGroup() != null) {
			fixSelection(iRequests.getValue(), plan.getGroup(), null);
			addGroup(1, plan.getGroup().getMaxDepth(), iRequests.getValue(), plan.getGroup(), null);
		}
		updateBackground();
		refreshSelection();
	}
	
	private int toScore(CourseRequestInterface requests, CourseRequestInterface.RequestPriority priority) {
		if (priority == null) return 0;
		int size = requests.getCourses().size() + requests.getAlternatives().size();
		int index = priority.getPriority() - 1 + (priority.isAlternative() ? requests.getCourses().size() : 0);
		return 3 * (size + index) - priority.getChoice();
	}
	
	protected int getSelectionScore(CourseRequestInterface requests, DegreeCourseInterface course, CourseAssignment assignment) {
		int ret = 0;
		if (course.isSelected())
			ret += 1;
		if (assignment != null) {
			CourseRequestInterface.RequestPriority priority = requests.getRequestPriority(assignment);
			ret += toScore(requests, priority);
			if (isSaved(assignment)) {
				if (isLast(assignment)) ret += 1000;
			} else if (isLast(assignment)) ret += 100;
		} else {
			CourseRequestInterface.RequestPriority priority = requests.getRequestPriority(course);
			ret += toScore(requests, priority);
		}
		return ret;
	}
	
	protected int getSelectionScore(CourseRequestInterface requests, DegreeGroupInterface unionGroup) {
		int ret = 0;
		if (unionGroup.isSelected())
			ret += 1;
		if (unionGroup.hasCourses()) {
			int sum = 0;
			for (DegreeCourseInterface course: unionGroup.getCourses()) {
				if (course.hasCourses()) {
					double max = 0;
					for (CourseAssignment ca: course.getCourses())
						max = Math.max(max, getSelectionScore(requests, course, ca));
					sum += max;
				} else {
					sum += getSelectionScore(requests, course, null);
				}
			}
			ret += Math.round((1.0 / unionGroup.getCourses().size()) * sum);
		}
		return ret;
	}
	
	protected void fixSelection(CourseRequestInterface requests, DegreeGroupInterface group, DegreeGroupInterface parent) {
		if (group.isChoice()) {
			int bestSelection = 0;
			DegreeGroupInterface bestGroup = null;
			DegreeCourseInterface bestCourse = null;
			CourseAssignment bestCA = null;
			if (group.hasCourses()) {
				for (DegreeCourseInterface course: group.getCourses()) {
					if (course.hasCourses()) {
						for (CourseAssignment ca: course.getCourses()) {
							int selection = getSelectionScore(requests, course, ca);
							if (selection > bestSelection) {
								bestSelection = selection;
								bestGroup = null; bestCourse = course; bestCA =ca;
							}
						}
					} else {
						int selection = getSelectionScore(requests, course, null);
						if (selection > bestSelection) {
							bestSelection = selection;
							bestGroup = null; bestCourse = course; bestCA = null;
						}
					}
					course.setCourseId(null);
					course.setSelected(false);
				}
			}
			if (group.hasGroups()) {
				for (DegreeGroupInterface ug: group.getGroups()) {
					int selection = getSelectionScore(requests, ug);
					if (selection > bestSelection) {
						bestSelection = selection;
						bestGroup = ug; bestCourse = null; bestCA = null;
					}
				}
			}
			if (bestGroup != null) {
				bestGroup.setSelected(true);
			} else if (bestCourse != null) {
				bestCourse.setSelected(true);
				bestCourse.setCourseId(bestCA == null ? null : bestCA.getCourseId());
			}
		} else if (parent != null && parent.isChoice()) {
			if (group.hasCourses())
				for (DegreeCourseInterface course: group.getCourses()) {
					if (parent.isSelected()) {
						int bestSelection = 0;
						CourseAssignment bestCA = null;
						if (course.hasCourses()) {
							for (CourseAssignment ca: course.getCourses()) {
								int selection = getSelectionScore(requests, course, ca);
								if (selection > bestSelection) {
									bestSelection = selection; bestCA = ca;
								}
							}
						}
						course.setSelected(true);
						course.setCourseId(bestCA == null ? null : bestCA.getCourseId());
					} else {
						course.setSelected(false); course.setCourseId(null);
					}
				}
		} else if (group.hasCourses()) {
			for (DegreeCourseInterface course: group.getCourses()) {
				int bestSelection = -1;
				CourseAssignment bestCA = null;
				if (course.hasCourses()) {
					for (CourseAssignment ca: course.getCourses()) {
						int selection = getSelectionScore(requests, course, ca);
						if (selection > bestSelection) {
							bestSelection = selection; bestCA = ca;
						}
					}
				}
				course.setSelected(true);
				course.setCourseId(bestCA == null ? null : bestCA.getCourseId());
			}
		}
		if (group.hasGroups())
			for (DegreeGroupInterface g: group.getGroups())
				fixSelection(requests, g, group);
	}
	
	protected void addCourse(int depth, int maxDepth, CourseRequestInterface requests, DegreeGroupInterface group, DegreeCourseInterface course) {
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
			row.add(new Label());
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
			if (course.isCritical())
				row.add(new CriticalCell(course));
			else
				row.add(new Label());
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
					row.add(new ChoiceButton(group, course, ca, requests));
					row.add(new CourseLabel(MESSAGES.course(ca.getSubject(), ca.getCourseNbr()), true));
				} else if (course.hasMultipleCourses()) {
					row.add(new ChoiceButton(course, ca, requests));
					row.add(new CourseLabel(MESSAGES.course(ca.getSubject(), ca.getCourseNbr()), true));
				} else {
					row.add(new CourseLabel(MESSAGES.course(ca.getSubject(), ca.getCourseNbr()), false));
				}
				row.add(new TitleLabel(ca.getTitle() == null ? "" : ca.getTitle()));
				row.add(new HTML(ca.getLimit() == null || ca.getLimit() == 0 || ca.getEnrollment() == null ? "" : ca.getLimit() < 0 ? "&infin;" : (ca.getLimit() - ca.getEnrollment()) + " / " + ca.getLimit(), false));
				row.add(new Label(ca.hasCredit() ? ca.getCreditAbbv() : "", false));
				row.add(new NoteCell(ca.getNote()));
				row.add(new RequestPriorityCell(requests, ca));
				Image icon = null;
				if (isSaved(ca)) {
					if (isLast(ca)) {
						icon = new Image(RESOURCES.saved());
						icon.setTitle(MESSAGES.saved(MESSAGES.course(ca.getSubject(), ca.getCourseNbr())));
					} else {
						icon = new Image(RESOURCES.unassignment());
						icon.setTitle(MESSAGES.unassignment(MESSAGES.course(ca.getSubject(), ca.getCourseNbr())));
					}
				} else if (isLast(ca)) {
					icon = new Image(RESOURCES.assignment());
					icon.setTitle(MESSAGES.assignment(MESSAGES.course(ca.getSubject(), ca.getCourseNbr())));
				}
				if (icon != null) {
					icon.addStyleName("icon");
					row.add(icon);
				} else
					row.add(new Label());
				if (course.isCritical() && !(course.getCourses().size() > 1 && !group.isChoice()))
					row.add(new CriticalCell(course));
				else
					row.add(new Label());
				addRow(ca, row);
			}
		}
	}
	
	protected void addGroup(int depth, int maxDepth, CourseRequestInterface requests, DegreeGroupInterface group, DegreeGroupInterface parent) {
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
			if (group.isCritical())
				row.add(new CriticalCell(group));
			else
				row.add(new Label());
			addRow(group, row);
		}
		if (group.hasCourses()) {
			for (DegreeCourseInterface course: group.getCourses()) {
				if (course.isCritical())
					addCourse(depth, maxDepth, requests, group, course);
			}
		}
		if (group.hasGroups()) {
			for (DegreeGroupInterface g: group.getGroups())
				if (g.isCritical() && depth == 1)
					addGroup(depth + 1, maxDepth, requests, g, group);
		}
		
		if (group.hasCourses()) {
			for (DegreeCourseInterface course: group.getCourses()) {
				if (!course.isCritical())
					addCourse(depth, maxDepth, requests, group, course);
			}
		}
		if (group.hasGroups()) {
			for (DegreeGroupInterface g: group.getGroups())
				if (!g.isCritical() || depth != 1)
					addGroup(depth + 1, maxDepth, requests, g, group);
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
		public int getColSpan() { return (iHasChoice ? 7 : 8); }
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
		public int getColSpan() { return 5; }
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
		public int getColSpan() { return 9; }
	}
	
	public static class CriticalCell extends Image {
		public CriticalCell(final DegreeCourseInterface course) {
			super(RESOURCES.degreePlanCritical());
			setTitle(MESSAGES.hintCriticalCourse(course.getCourseName()));
			setAltText(MESSAGES.hintCriticalCourse(course.getCourseName()));
			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					UniTimeConfirmationDialog.info(MESSAGES.hintCriticalCourse(course.getCourseName()));
				}
			});
		}
		
		public CriticalCell(final DegreeGroupInterface group) {
			super(RESOURCES.degreePlanCritical());
			setTitle(MESSAGES.hintCriticalGroup(group.isPlaceHolder() ? group.getDescription() : group.toString(MESSAGES)));
			setAltText(MESSAGES.hintCriticalGroup(group.isPlaceHolder() ? group.getDescription() : group.toString(MESSAGES)));
			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					UniTimeConfirmationDialog.info(MESSAGES.hintCriticalGroup(group.isPlaceHolder() ? group.getDescription() : group.toString(MESSAGES)));
				}
			});
		}
	}
	
	public static interface HasRefresh {
		public void refresh();
	}
	
	public void refreshSelection() {
		for (int i = 0; i < getRowCount(); i++)
			for (int j = 0; j < getCellCount(i); j++) {
				Widget w = getWidget(i, j);
				if (w != null && w instanceof HasRefresh)
					((HasRefresh)w).refresh();
			}
	}
	
	public class ChoiceButton extends CheckBox implements HasRefresh {
		private DegreeMultiSelectionInterface iParent;
		private String iId;
		
		public ChoiceButton(final DegreeGroupInterface parent, final DegreeCourseInterface course) {
			super("");
			if (course.isSelected()) course.setSelected(false);
			setEnabled(false);
			iParent = parent; iId = course.getId();
			refresh();
			setTitle(MESSAGES.hintChoiceGroupSelection(MESSAGES.course(course.getSubject(), course.getCourse())));
			addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iParent.setMultiSelection(iId, event.getValue());
					if (!iParent.hasMultiSelection() || iParent.getMultiSelection(iId) == 0) {
						if (parent.hasCourses()) {
							for (DegreeCourseInterface c: parent.getCourses())
								c.setSelected(iId.equals(c.getId()) ? event.getValue() : false);
						}
						if (parent.hasGroups()) {
							for (DegreeGroupInterface g: parent.getGroups()) {
								g.setSelected(false);
							}
						}
					}
					updateBackground();
					refreshSelection();
				}
			});
		}
		
		public ChoiceButton(final DegreeGroupInterface parent, final DegreeGroupInterface group) {
			super("");
			setEnabled(false);
			if (group.hasCourses())
				for (DegreeCourseInterface course: group.getCourses())
					if (course.hasCourses()) { setEnabled(true); break; }
			iParent = parent; iId = group.getId();
			if (!iParent.hasMultiSelection() && group.isSelected() && isEnabled())
				iParent.setMultiSelection(iId, true);
			if (!isEnabled() && group.isSelected())
				group.setSelected(false);
			refresh();
			setTitle(MESSAGES.hintChoiceGroupSelection(group.toString(MESSAGES)));
			addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iParent.setMultiSelection(iId, event.getValue());
					if (!iParent.hasMultiSelection() || iParent.getMultiSelection(iId) == 0) {
						if (parent.hasCourses()) {
							for (DegreeCourseInterface c: parent.getCourses())
								c.setSelected(false);
						}
						if (parent.hasGroups()) {
							for (DegreeGroupInterface g: parent.getGroups()) {
								g.setSelected(iId.equals(g.getId()) ? event.getValue() : false);
							}
						}
					}
					updateBackground();
					refreshSelection();
				}
			});
		}
		
		public ChoiceButton(final DegreeCourseInterface parent, final CourseAssignment course, CourseRequestInterface requests) {
			super("");
			iParent = parent; iId = parent.getId() + ":" + course.getCourseId();
			if (!iParent.hasMultiSelection() && course.getCourseId().equals(parent.getCourseId())) {
				iParent.setMultiSelection(iId, true);
				RequestPriority p = requests.getRequestPriority(course);
				if (p != null)
					for (RequestedCourse rc: p.getRequest().getRequestedCourse()) {
						if (parent.hasCourse(rc.getCourseId())) iParent.setMultiSelection(parent.getId() + ":" + rc.getCourseId(), true);
					}
			}
			refresh();
			setTitle(MESSAGES.hintChoiceGroupSelection(MESSAGES.course(course.getSubject(), course.getCourseNbr())));
			addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iParent.setMultiSelection(iId, event.getValue());
					if (!iParent.hasMultiSelection() || iParent.getMultiSelection(iId) == 0) {
						if (event.getValue()) {
							parent.setCourseId(course.getCourseId());
						} else {
							parent.setCourseId(null);
						}
					}
					updateBackground();
					refreshSelection();
				}
			});
		}
		
		public ChoiceButton(final DegreeGroupInterface group, final DegreeCourseInterface parent, final CourseAssignment course, CourseRequestInterface requests) {
			super("");
			iParent = group; iId = parent.getId() + ":" + course.getCourseId();
			if (!iParent.hasMultiSelection() && course.getCourseId().equals(parent.getCourseId())) {
				iParent.setMultiSelection(iId, true);
				RequestPriority p = requests.getRequestPriority(course);
				if (p != null)
					for (RequestedCourse rc: p.getRequest().getRequestedCourse()) {
						DegreeCourseInterface c = group.getCourse(rc.getCourseId());
						if (c != null) iParent.setMultiSelection(c.getId() + ":" + rc.getCourseId(), true);
					}
			}
			refresh();
			setTitle(MESSAGES.hintChoiceGroupSelection(MESSAGES.course(course.getSubject(), course.getCourseNbr())));
			addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iParent.setMultiSelection(iId, event.getValue());
					if (!iParent.hasMultiSelection() || iParent.getMultiSelection(iId) == 0) {
						if (event.getValue()) {
							parent.setCourseId(course.getCourseId());
						} else {
							parent.setCourseId(null);
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
					updateBackground();
					refreshSelection();
				}
			});
		}

		@Override
		public void refresh() {
			int selection = iParent.getMultiSelection(iId);
			setValue(selection >= 0);
			setText(selection < 0 ? "" : String.valueOf(1 + selection));
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
	
	public static class RequestPriorityCell extends Label {
		
		public RequestPriorityCell(CourseRequestInterface requests, CourseAssignment assignment) {
			CourseRequestInterface.RequestPriority rp = requests.getRequestPriority(assignment);
			setText(rp == null ? "" : rp.toString(MESSAGES));
			setWordWrap(false);
			addStyleName("request");
		}
	}
	
	public boolean canChoose(int row) {
		if (row <= 1 || row >= getRowCount()) return false;
		Widget w = getWidget(row, 1);
		return (w != null && w instanceof ChoiceButton);
	}
	
	public void chooseRow(int row, boolean value) {
		if (row <= 1 || row >= getRowCount()) return;
		Widget w = getWidget(row, 1);
		if (w != null && w instanceof ChoiceButton) {
			((ChoiceButton)w).setValue(value, true);
		}
	}
	
	public boolean isLast(CourseAssignment course) {
		if (iAssignments != null && iAssignments.getLastAssignment() != null) {
			for (ClassAssignmentInterface.CourseAssignment c: iAssignments.getLastAssignment().getCourseAssignments())
				if (course.getCourseId().equals(c.getCourseId()) && c.isAssigned())
					return true;
		}
		return false;
	}
	
	public boolean isSaved(CourseAssignment course) {
		if (iAssignments != null && iAssignments.getSavedAssignment() != null) {
			 for (ClassAssignmentInterface.CourseAssignment c: iAssignments.getSavedAssignment().getCourseAssignments())
				 if (course.getCourseId().equals(c.getCourseId()) && c.isAssigned())
					 return true;
		}
		return false;
	}
	
	protected boolean isLast(RequestedCourse course) {
		if (course == null || course.isEmpty()) return false;
		if (iAssignments != null && iAssignments.getLastAssignment() != null) {
			for (ClassAssignmentInterface.CourseAssignment c: iAssignments.getLastAssignment().getCourseAssignments())
				if (course.equals(c) && c.isAssigned())
					return true;
		}
		return false;
	}
	
	protected boolean isSaved(RequestedCourse course) {
		if (course == null || course.isEmpty()) return false;
		if (iAssignments != null && iAssignments.getSavedAssignment() != null) {
			 for (ClassAssignmentInterface.CourseAssignment c: iAssignments.getSavedAssignment().getCourseAssignments())
				 if (course.equals(c) && c.isAssigned())
					 return true;
		}
		return false;
	}
	
	public CourseRequestInterface createRequests(WaitListMode wl, Integer criticalLevel) {
		CourseRequestInterface requests = iRequests.getValue();
		// 1. delete all requests that are not assigned
		for (Iterator<CourseRequestInterface.Request> i = requests.getCourses().iterator(); i.hasNext(); ) {
			CourseRequestInterface.Request request = i.next();
			if (!request.isCanDelete()) continue;
			if (request.hasRequestedCourse()) {
				for (Iterator<RequestedCourse> j = request.getRequestedCourse().iterator(); j.hasNext(); ) { 
					RequestedCourse course = j.next();
					if (isLast(course)) {
						// Only drop the request if the student selected a different course (from a choice)
						if (iPlan.hasCourse(course) && !iPlan.isCourseSelected(course))
							j.remove();
					} else {
						j.remove();
					}
				}
			}
			if (!request.hasRequestedCourse()) i.remove();
		}
		// 2. move all assigned alternate requests up into the requests table
		for (Iterator<CourseRequestInterface.Request> i = requests.getAlternatives().iterator(); i.hasNext(); ) {
			CourseRequestInterface.Request request = i.next();
			if (!request.isCanDelete()) continue;
			if (request.hasRequestedCourse()) {
				for (Iterator<RequestedCourse> j = request.getRequestedCourse().iterator(); j.hasNext(); ) { 
					RequestedCourse course = j.next();
					if (isLast(course)) {
						// Only drop the request if the student selected a different course (from a choice)
						if (iPlan.hasCourse(course) && !iPlan.isCourseSelected(course))
							j.remove();
					} else {
						j.remove();
					}
				}
			}
			if (!request.isEmpty())	requests.getCourses().add(request);
			i.remove();
		}
		// 3. put in all selected courses, skip those that are already in there
		for (DegreeCourseInterface course: iPlan.listSelected(false)) {
			CourseAssignment ca = course.getSelectedCourse(true);
			if (ca != null) {
				RequestedCourse rc = new RequestedCourse();
				rc.setCourseId(ca.getCourseId());
				rc.setCourseName(CONSTANTS.showCourseTitle() ? MESSAGES.courseNameWithTitle(ca.getSubject(), ca.getCourseNbr(), ca.getTitle()) : MESSAGES.course(ca.getSubject(), ca.getCourseNbr()));
				rc.setCourseTitle(ca.getTitle());
				rc.setCredit(ca.guessCreditRange());
				rc.setCanWaitList(ca.isCanWaitList());
				rc.setParentCourseId(ca.getParentCourseId());
				
				CourseRequestInterface.RequestPriority p = requests.getRequestPriority(ca);
				
				List<CourseAssignment> alternatives = iPlan.listAlternatives(course);
				
				CourseRequestInterface.Request r = new CourseRequestInterface.Request();
				r.addRequestedCourse(rc);
				if (wl == WaitListMode.NoSubs && iPlan.isCourseCritical(rc)) r.setNoSub(true);
				if (criticalLevel != null && iPlan.isCourseCritical(rc)) r.setCritical(criticalLevel);
				if (p != null) {
					r = p.getRequest();
					if (r.isReadOnly()) continue;
				} else {
					requests.getCourses().add(r);
				}
				
				r.setFilter(iPlan.getPlaceHolder(course));
				r.setAdvisorNote(iPlan.getPlaceHolder(course));
				for (CourseAssignment altCa: alternatives) {
					p = requests.getRequestPriority(altCa);
					if (p != null) continue;
					RequestedCourse altRc = new RequestedCourse();
					altRc.setCourseId(altCa.getCourseId());
					altRc.setCourseName(CONSTANTS.showCourseTitle() ? MESSAGES.courseNameWithTitle(altCa.getSubject(), altCa.getCourseNbr(), altCa.getTitle()) : MESSAGES.course(altCa.getSubject(), altCa.getCourseNbr()));
					altRc.setCourseTitle(altCa.getTitle());
					altRc.setCredit(altCa.guessCreditRange());
					altRc.setCanWaitList(altCa.isCanWaitList());
					altRc.setParentCourseId(altCa.getParentCourseId());
					r.addRequestedCourse(altRc);
				}
			}
		}
		
		return requests;
	}
	
	public void updateBackground() {
		CourseRequestInterface requests = iRequests.getValue();
		Set<String> selectedCourses = new HashSet<String>();
		Set<Long> selectedCourseIds = new HashSet<Long>();
		for (DegreeCourseInterface course: iPlan.listSelected(false)) {
			if (course.getId() != null) selectedCourses.add(course.getId());
			if (course.getCourseId() != null) selectedCourseIds.add(course.getCourseId());
			for (CourseAssignment ca: iPlan.listAlternatives(course)) {
				selectedCourseIds.add(ca.getCourseId());
			}
		}
		for (int row = 1; row < getRowCount(); row++) {
			Object data = getData(row);
			if (data != null && data instanceof CourseAssignment) {
				CourseAssignment course = (CourseAssignment) data;
				RequestPriority rp = requests.getRequestPriority(course);
				boolean requested = (rp != null);
				boolean selected = selectedCourseIds.contains(course.getCourseId());
				String color = null;
				if (selected) {
					if (!requested)
						color = "#D7FFD7"; // will be added if Apply is used
				} else {
					if (requested && rp.getRequest().isCanDelete())
						color = "#FFD7D7"; // will be removed, not enrolled
				}
				setBackGroundColor(row, color);
			} else if (data != null && data instanceof DegreeGroupInterface) {
				DegreeGroupInterface group = (DegreeGroupInterface)data;
				if (group.isChoice()) {
					String color = null;
					if (!group.hasSelection())
						color = "#FFF0AB";
					setBackGroundColor(row, color);
				}
			} else if (data != null && data instanceof DegreeCourseInterface) {
				DegreeCourseInterface course = (DegreeCourseInterface)data;
				String color = null;
				if (course.getId() != null && selectedCourses.contains(course.getId()) && course.getCourseId() == null)
					color = "#FFF0AB";
				setBackGroundColor(row, color);
			}
		}
	}
}