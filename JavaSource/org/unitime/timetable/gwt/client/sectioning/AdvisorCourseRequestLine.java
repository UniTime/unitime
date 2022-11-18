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
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.widgets.CourseFinder;
import org.unitime.timetable.gwt.client.widgets.CourseFinderClasses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDetails;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDialog;
import org.unitime.timetable.gwt.client.widgets.CourseFinderFactory;
import org.unitime.timetable.gwt.client.widgets.CourseFinderFreeTime;
import org.unitime.timetable.gwt.client.widgets.CourseFinderMultipleCourses;
import org.unitime.timetable.gwt.client.widgets.CourseRequestBox;
import org.unitime.timetable.gwt.client.widgets.CourseSelection;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionEvent;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionHandler;
import org.unitime.timetable.gwt.client.widgets.DataProvider;
import org.unitime.timetable.gwt.client.widgets.FreeTimeParser;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.Validator;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CourseMessage;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextArea;

/**
 * @author Tomas Muller
 */
public class AdvisorCourseRequestLine implements HasValue<Request> {
	protected static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	protected static final SectioningServiceAsync sSectioningService = GWT.create(SectioningService.class);
	
	private P iP, iButtons;
	
	private boolean iAlternate;
	private int iPriority;
	private StudentSectioningContext iContext;
	private List<CourseSelectionBox> iCourses = new ArrayList<CourseSelectionBox>();
	private AdvisorCourseRequestLine iPrevious = null, iNext = null;
	private Validator<CourseSelection> iValidator = null;
	private SpecialRegistrationContext iSpecReg;
	private ImageButton iDelete;
	private UniTimeTextBox iCredit;
	private CheckBox iWaitList;
	private CheckBox iCritical;
	private WaitListMode iWaitListMode = WaitListMode.None;
	private TextArea iNotes;
	private Timer iTimer;
	private Integer iCriticalCheck = null;
	
	public AdvisorCourseRequestLine(StudentSectioningContext context, int priority, boolean alternate, Validator<CourseSelection> validator, SpecialRegistrationContext specreg) {
		iP = new P("unitime-AdvisorCourseRequestLine");
		iContext = context;
		iValidator = validator;
		iPriority = priority;
		iAlternate = alternate;
		iSpecReg = specreg;
		
		P line = new P("line");
		P title = new P("title"); title.setText(alternate ? MESSAGES.courseRequestsAlternate(priority + 1) : MESSAGES.courseRequestsPriority(priority + 1));
		line.add(title);

		CourseSelectionBox box = new CourseSelectionBox(!alternate, false);
		if (alternate) {
			box.setLabel(ARIA.titleRequestedAlternate(1 + priority, String.valueOf((char)((int)'a' + priority))), ARIA.altRequestedAlternateFinder(1 + priority));
			box.setAccessKey((char)((int)'a' + priority));
		} else {
			box.setLabel(ARIA.titleRequestedCourse(1 + priority), ARIA.altRequestedCourseFinder(1 + priority));
			if (priority < 9)
				box.setAccessKey((char)((int)'1' + priority));
			else if (priority == 9)
				box.setAccessKey('0');
		}
		box.addStyleName("course");
		line.add(box);
		iCourses.add(box);
		iP.add(line);
		
		iButtons = new P("unitime-AdvisorCourseRequestButtons");
		
		P up = new P("blank");
		iButtons.add(up);
		
		P down = new P("blank");
		iButtons.add(down);

		iDelete = new ImageButton(RESOURCES.delete(), RESOURCES.delete_Down(), RESOURCES.delete_Over());
		iDelete.addStyleName("unitime-NoPrint");
		iDelete.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				delete();
				ValueChangeEvent.fire(AdvisorCourseRequestLine.this, getValue());
			}
		});
		iDelete.addStyleName("delete");
		iDelete.setAltText(ARIA.altDeleteRequest(priority + 1));
		iButtons.add(iDelete);
		iCredit = new UniTimeTextBox();
		iCredit.addStyleName("unitime-AdvisorCourseRequestCredit");
		iCredit.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				ValueChangeEvent.fire(AdvisorCourseRequestLine.this, getValue());
			}
		});
		iCredit.setMaxLength(10);
		iNotes = new TextArea();
		iNotes.setStyleName("unitime-TextArea");
		iNotes.setText("");
		iNotes.setHeight(23 + "px");
		iNotes.getElement().setAttribute("maxlength", "2048");
		iTimer = new Timer() {
			@Override
			public void run() {
				resizeNotes();
			}
		};
		iNotes.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				iTimer.schedule(10);
			}
		});
		iNotes.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iTimer.schedule(10);
			}
		});
		
		if (!alternate) {
			iCritical = new CheckBox(); iCritical.addStyleName("critical");
			iCritical.setEnabled(false);
			box.addCourseSelectionHandler(new CourseSelectionHandler() {
				@Override
				public void onCourseSelection(CourseSelectionEvent event) {
					iCritical.setEnabled(event.getValue() != null && event.getValue().hasCourseId());
					if (event.getValue() == null || !event.getValue().hasCourseId())
						iCritical.setValue(false);
				}
			});
		}
		
		if (!alternate) {
			iWaitList = new CheckBox(); iWaitList.addStyleName("waitlist");
			iWaitList.setEnabled(false);
			iNotes.addStyleName("notes-with-critical-and-waitlist");
			box.addCourseSelectionHandler(new CourseSelectionHandler() {
				@Override
				public void onCourseSelection(CourseSelectionEvent event) {
					boolean readOnly = event.getValue() != null && event.getValue().isReadOnly();
					boolean canSet = false;
					if (iWaitListMode == WaitListMode.WaitList) {
						canSet = event.getValue() != null && event.getValue().isCanWaitList();
					} else if (iWaitListMode == WaitListMode.NoSubs) {
						canSet = event.getValue() != null && event.getValue().isCanNoSub();
					}
					iWaitList.setEnabled(canSet && !readOnly);
					if (!canSet) iWaitList.setValue(false);
				}
			});
		} else {
			iNotes.addStyleName("notes-no-waitlist");
		}
	}
	
	public void insert(FlexTable table, int row) {
		table.setWidget(row, 0, iP);
		table.getFlexCellFormatter().setColSpan(row, 0, 2);
		table.getFlexCellFormatter().getElement(row, 0).getStyle().setProperty("max-width", 400, Unit.PX);
		table.setWidget(row, 1, iCredit);
		table.getFlexCellFormatter().getElement(row, 1).getStyle().setWidth(45, Unit.PX);
		table.setWidget(row, 2, iNotes);
		table.getFlexCellFormatter().setColSpan(row, 2, 3);
		if (iWaitList != null) {
			table.getFlexCellFormatter().setColSpan(row, 2, 1);
			table.setWidget(row, 3, iCritical);
			table.setWidget(row, 4, iWaitList);
			table.setWidget(row, 5, iButtons);
			table.getFlexCellFormatter().getElement(row, 3).getStyle().setProperty("max-width", 25, Unit.PX);
			table.getFlexCellFormatter().getElement(row, 4).getStyle().setProperty("max-width", 25, Unit.PX);
			table.getFlexCellFormatter().getElement(row, 5).getStyle().setWidth(75, Unit.PX);
		} else {
			table.setWidget(row, 3, iButtons);
			table.getFlexCellFormatter().getElement(row, 3).getStyle().setWidth(75, Unit.PX);
		}
	}
	
	public void setWaitListMode(WaitListMode wlMode) {
		iWaitListMode = wlMode;
		if (iWaitList != null) {
			iWaitList.setVisible(iWaitListMode == WaitListMode.WaitList || iWaitListMode == WaitListMode.NoSubs);
			if (iWaitList.isVisible() && iCritical.isVisible()) {
				iNotes.addStyleName("notes-with-critical-and-waitlist");
				iNotes.removeStyleName("notes-no-waitlist");
				iNotes.removeStyleName("notes-with-critical");
				iNotes.removeStyleName("notes-with-waitlist");
			} else if (iWaitList.isVisible()) {
				iNotes.addStyleName("notes-with-waitlist");
				iNotes.removeStyleName("notes-no-waitlist");
				iNotes.removeStyleName("notes-with-critical");
				iNotes.removeStyleName("notes-with-critical-and-waitlist");
			} else if (iCritical.isVisible()) {
				iNotes.addStyleName("notes-with-critical");
				iNotes.removeStyleName("notes-no-waitlist");
				iNotes.removeStyleName("notes-with-waitlist");
				iNotes.removeStyleName("notes-with-critical-and-waitlist");
			} else {
				iNotes.addStyleName("notes-no-waitlist");
				iNotes.removeStyleName("notes-with-waitlist");
				iNotes.removeStyleName("notes-with-critical");
				iNotes.removeStyleName("notes-with-critical-and-waitlist");
			}
		}
	}
	
	public void setCriticalCheck(Integer criticalCheck) {
		iCriticalCheck = criticalCheck;
		if (iCritical != null) {
			iCritical.setVisible(criticalCheck != null && criticalCheck.intValue() > 0);
			if (iWaitList.isVisible() && iCritical.isVisible()) {
				iNotes.addStyleName("notes-with-critical-and-waitlist");
				iNotes.removeStyleName("notes-no-waitlist");
				iNotes.removeStyleName("notes-with-critical");
				iNotes.removeStyleName("notes-with-waitlist");
			} else if (iWaitList.isVisible()) {
				iNotes.addStyleName("notes-with-waitlist");
				iNotes.removeStyleName("notes-no-waitlist");
				iNotes.removeStyleName("notes-with-critical");
				iNotes.removeStyleName("notes-with-critical-and-waitlist");
			} else if (iCritical.isVisible()) {
				iNotes.addStyleName("notes-with-critical");
				iNotes.removeStyleName("notes-no-waitlist");
				iNotes.removeStyleName("notes-with-waitlist");
				iNotes.removeStyleName("notes-with-critical-and-waitlist");
			} else {
				iNotes.addStyleName("notes-no-waitlist");
				iNotes.removeStyleName("notes-with-waitlist");
				iNotes.removeStyleName("notes-with-critical");
				iNotes.removeStyleName("notes-with-critical-and-waitlist");
			}
		}
	}
	
	public void setPrevious(AdvisorCourseRequestLine previous) {
		iPrevious = previous;
		if (iPrevious == null) {
			if (iButtons.getWidget(0) instanceof ImageButton) {
				iButtons.remove(0);
				P up = new P("blank");
				iButtons.add(up);
				iButtons.insert(up, 0);
			}
		} else {
			ImageButton up = null;
			if (iButtons.getWidget(0) instanceof ImageButton) {
				up = (ImageButton)iButtons.getWidget(0);
			} else {
				iButtons.remove(0);
				up = new ImageButton(RESOURCES.up(), RESOURCES.up_Down(), RESOURCES.up_Over());
				up.addStyleName("unitime-NoPrint");
				up.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						up();
						ValueChangeEvent.fire(AdvisorCourseRequestLine.this, getValue());
					}
				});
				up.addStyleName("up");
				iButtons.insert(up, 0);
			}
			if (isAlternate()) {
				if (iPrevious.isAlternate())
					up.setAltText(ARIA.altSwapAlternateRequest(getPriority() + 1, getPriority()));
				else
					up.setAltText(ARIA.altSwapCourseAlternateRequest(iPrevious.getPriority() + 1, getPriority() + 1));
			} else {
				up.setAltText(ARIA.altSwapCourseRequest(getPriority() + 1, getPriority()));
			}
		}
	}
	
	public void setNext(AdvisorCourseRequestLine next) {
		iNext = next;
		if (iNext == null) {
			if (iButtons.getWidget(1) instanceof ImageButton) {
				iButtons.remove(1);
				P down = new P("blank");
				iButtons.add(down);
				iButtons.insert(down, 1);
			}
		} else {
			ImageButton down = null;
			if (iButtons.getWidget(1) instanceof ImageButton) {
				down = (ImageButton)iButtons.getWidget(1);
			} else {
				iButtons.remove(1);
				down = new ImageButton(RESOURCES.down(), RESOURCES.down_Down(), RESOURCES.down_Over());
				down.addStyleName("unitime-NoPrint");
				down.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						down();
						ValueChangeEvent.fire(AdvisorCourseRequestLine.this, getValue());
					}
				});
				down.addStyleName("down");
				iButtons.insert(down, 1);
			}
			if (isAlternate()) {
				down.setAltText(ARIA.altSwapAlternateRequest(getPriority() + 1, getPriority() + 2));
			} else {
				if (iNext.isAlternate())
					down.setAltText(ARIA.altSwapCourseAlternateRequest(getPriority() + 1, iNext.getPriority() + 1));
				else
					down.setAltText(ARIA.altSwapCourseRequest(getPriority() + 1, getPriority() + 2));
			}
		}
	}
	
	public void up() {
		if (iPrevious != null) {
			Request r = getValue();
			setValue(iPrevious.getValue());
			iPrevious.setValue(r);
		}
	}
	
	public void down() {
		if (iNext != null) {
			Request r = getValue();
			setValue(iNext.getValue());
			iNext.setValue(r);
		}
	}
	
	public void computeCredits() {
		Float minCredit = null, maxCredit = null; 
		for (CourseSelectionBox box: iCourses) {
			RequestedCourse rc = box.getValue();
			if (rc.hasCredit()) {
				if (minCredit == null || minCredit > rc.getCreditMin()) 
					minCredit = rc.getCreditMin();
				if (maxCredit == null || maxCredit < rc.getCreditMax()) 
					maxCredit = rc.getCreditMax();
			}
		}
		if (minCredit != null) {
			if (minCredit < maxCredit) 
				iCredit.setValue(MESSAGES.creditRange(minCredit, maxCredit));
			else
				iCredit.setValue(MESSAGES.credit(minCredit));
		}
	}
	
	public float getCreditMin() {
		String cred = iCredit.getValue();
		if (cred.isEmpty()) return 0f;
		try {
			return Float.parseFloat(cred.replaceAll("\\s",""));
		} catch (NumberFormatException e) {}
		if (cred.contains("-")) {
			try {
				return Float.parseFloat(cred.substring(0, cred.indexOf('-')).replaceAll("\\s",""));
			} catch (NumberFormatException e) {}	
		}
		return 0f;
	}
	
	public float getCreditMax() {
		String cred = iCredit.getValue();
		if (cred.isEmpty()) return 0f;
		try {
			return Float.parseFloat(cred.replaceAll("\\s",""));
		} catch (NumberFormatException e) {}
		if (cred.contains("-")) {
			try {
				return Float.parseFloat(cred.substring(1 + cred.indexOf('-')).replaceAll("\\s",""));
			} catch (NumberFormatException e) {}	
		}
		return 0f;
	}
	
	public List<? extends CourseSelectionBox> getCourses() {
		return iCourses;
	}
	
	public void fixTitles() {
		for (int i = 0; i < iP.getWidgetCount(); i++) {
			P line = (P)iP.getWidget(i);
			P title = (P)line.getWidget(0);
			CourseSelectionBox box = (CourseSelectionBox)line.getWidget(1);
			if (i == 0) {
				title.setText(isAlternate() ? MESSAGES.courseRequestsAlternate(getPriority() + 1) : MESSAGES.courseRequestsPriority(getPriority() + 1));
				if (isAlternate()) {
					box.setLabel(ARIA.titleRequestedAlternate(1 + getPriority(), String.valueOf((char)((int)'a' + getPriority()))), ARIA.altRequestedAlternateFinder(1 + getPriority()));
				} else {
					box.setLabel(ARIA.titleRequestedCourse(1 + getPriority()), ARIA.altRequestedCourseFinder(1 + getPriority()));
				}
			} else {
				title.setText(MESSAGES.courseRequestsAlternative(i));
				if (isAlternate()) {
					if (i == 1)
						box.setLabel(ARIA.titleRequestedAlternateFirstAlternative(1 + getPriority()), ARIA.altRequestedAlternateFirstFinder(1 + getPriority()));
					else if (i == 2)
						box.setLabel(ARIA.titleRequestedAlternateSecondAlternative(1 + getPriority()), ARIA.altRequestedAlternateSecondFinder(1 + getPriority()));
					else
						box.setLabel(ARIA.titleRequestedAlternateNAlternative(i, 1 + getPriority()), ARIA.altRequestedNAlternateFinder(i, 1 + getPriority()));
				} else {
					if (i == 1)
						box.setLabel(ARIA.titleRequestedCourseFirstAlternative(1 + getPriority()), ARIA.altRequestedCourseFirstAlternativeFinder(1 + getPriority()));
					else if (i == 2)
						box.setLabel(ARIA.titleRequestedCourseSecondAlternative(1 + getPriority()), ARIA.altRequestedCourseSecondAlternativeFinder(1 + getPriority()));
					else
						box.setLabel(ARIA.titleRequestedCourseNAlternative(i, 1 + getPriority()), ARIA.altRequestedCourseNAlternativeFinder(i, 1 + getPriority()));
				}
			}
			box.resizeFilterIfNeeded();
			CourseSelectionEvent.fire(box, box.getValue());
		}
		resizeNotes();
	}
	
	private void resizeNotes() {
		if (!iNotes.getText().isEmpty()) {
			iNotes.setHeight(Math.max((23 + 27 * (iCourses.size() - 1)), iNotes.getElement().getScrollHeight()) + "px");
		} else {
			iNotes.setHeight((23 + 27 * (iCourses.size() - 1)) + "px");
		}
	}
	
	public boolean isAlternate() { return iAlternate; }
	public int getPriority() { return iPriority; }
	
	public void delete() {
		if (iNext != null && isAlternate() == iNext.isAlternate()) {
			setValue(iNext.getValue());
			iNext.delete();
		} else {
			iCourses.get(0).setValue(null);
			iCredit.setValue("");
			iNotes.setValue("");
			if (iCritical != null) { iCritical.setValue(false); iCritical.setEnabled(false); }
			if (iWaitList != null) { iWaitList.setValue(false); iWaitList.setEnabled(false); }
			for (int i = iCourses.size() - 1; i > 0; i--) {
				deleteAlternative(i);
			}
		}
	}
	
	public void deleteAlternative(int index) {
		iCourses.remove(index).dispose();
		iP.remove(index);
		fixTitles();
	}
	
	public void insertAlternative(int index) {
		P line = new P("alt-line");
		
		P title = new P("title"); title.setText(MESSAGES.courseRequestsAlternative(index));
		line.add(title);

		CourseSelectionBox box = new CourseSelectionBox(false, true);
		if (isAlternate()) {
			if (index == 1)
				box.setLabel(ARIA.titleRequestedAlternateFirstAlternative(1 + getPriority()), ARIA.altRequestedAlternateFirstFinder(1 + getPriority()));
			else if (index == 2)
				box.setLabel(ARIA.titleRequestedAlternateSecondAlternative(1 + getPriority()), ARIA.altRequestedAlternateSecondFinder(1 + getPriority()));
			else
				box.setLabel(ARIA.titleRequestedAlternateNAlternative(index, 1 + getPriority()), ARIA.altRequestedNAlternateFinder(index, 1 + getPriority()));
		} else {
			if (index == 1)
				box.setLabel(ARIA.titleRequestedCourseFirstAlternative(1 + getPriority()), ARIA.altRequestedCourseFirstAlternativeFinder(1 + getPriority()));
			else if (index == 2)
				box.setLabel(ARIA.titleRequestedCourseSecondAlternative(1 + getPriority()), ARIA.altRequestedCourseSecondAlternativeFinder(1 + getPriority()));
			else
				box.setLabel(ARIA.titleRequestedCourseNAlternative(index, 1 + getPriority()), ARIA.altRequestedCourseNAlternativeFinder(index, 1 + getPriority()));
		}
		box.addStyleName("course");
		line.add(box);
		iCourses.add(box);

		iP.insert(line, index);
		box.setValue(null);
		fixTitles();
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Request> handler) {
		return iP.addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Request getValue() {
		Request ret = new Request();
		for (CourseSelectionBox box: iCourses) {
			RequestedCourse rc = box.getValue();
			if (!rc.isEmpty()) ret.addRequestedCourse(rc);
		}
		ret.setFilter(iCourses.get(0).getCourseFinder().getFilter());
		ret.setAdvisorCredit(iCredit.getValue());
		ret.setAdvisorNote(iNotes.getValue());
		if (iWaitList == null || !iWaitList.isVisible()) {
			ret.setWaitList(null);
			ret.setNoSub(null);
		} else if (iWaitListMode == WaitListMode.WaitList) {
			ret.setWaitList(iWaitList.getValue());
			ret.setNoSub(null);
		} else if (iWaitListMode == WaitListMode.NoSubs) {
			ret.setWaitList(null);
			ret.setNoSub(iWaitList.getValue());
		} else {
			ret.setWaitList(null);
			ret.setNoSub(null);
		}
		if (iCritical != null && iCritical.isVisible() && iCritical.getValue()) {
			ret.setCritical(iCriticalCheck);
		}
		return (ret.isEmpty() && !ret.hasAdvisorCredit() && !ret.hasAdvisorNote() ? null : ret);
	}

	@Override
	public void setValue(Request value) {
		setValue(value, false);
	}
	
	public boolean isCanChangeAlternatives() {
		for (CourseSelectionBox box: iCourses)
			if (!box.isCanChangePriority()) return false;
		return true;
	}
	
	public void setUpArrowEnabled(boolean enabled) {
		if (iButtons.getWidget(0) instanceof ImageButton) {
			((ImageButton)iButtons.getWidget(0)).setEnabled(enabled);
			((ImageButton)iButtons.getWidget(0)).setVisible(enabled);
		}
	}

	public void setDownArrowEnabled(boolean enabled) {
		if (iButtons.getWidget(1) instanceof ImageButton) {
			((ImageButton)iButtons.getWidget(1)).setEnabled(enabled);
			((ImageButton)iButtons.getWidget(1)).setVisible(enabled);
		}
	}

	@Override
	public void setValue(Request value, boolean fireEvents) {
		if (value == null) {
			iCourses.get(0).setValue(null, true);
			for (int i = iCourses.size() - 1; i > 0; i--)
				deleteAlternative(i);
			iCredit.setValue("");
			iNotes.setValue("");
			if (iCritical != null) { iCritical.setValue(false); iCritical.setEnabled(false); }
			if (iWaitList != null) { iWaitList.setValue(false); iWaitList.setEnabled(false); }
		} else {
			int index = 0;
			if (value.hasRequestedCourse())
				for (RequestedCourse rc: value.getRequestedCourse()) {
					if (rc.isEmpty()) continue;
					if (iCourses.size() <= index) insertAlternative(index);
					iCourses.get(index).setValue(rc, true);
					index ++;
				}
			if (index == 0) { iCourses.get(0).setValue(null, true); index++; }
			else if (CONSTANTS.courseRequestAutomaticallyAddFirstAlternative() && !iAlternate && index == 1 && iCourses.get(0).getValue().hasCourseId() && iCourses.get(0).getValue().isCanDelete()) {
				iCourses.get(index).setValue(null, true);
				index ++;
			}
			for (int i = iCourses.size() - 1; i >= index; i--)
				deleteAlternative(i);
			if (value.hasFilter()) iCourses.get(0).getCourseFinder().setFilter(value.getFilter());
			iCredit.setValue(value.hasAdvisorCredit() ? value.getAdvisorCredit() : "");
			iNotes.setValue(value.hasAdvisorNote() ? value.getAdvisorNote() : "");
			if (iWaitList != null) {
				if (iWaitListMode == WaitListMode.WaitList && value.isCanWaitList()) {
					iWaitList.setValue(value.isWaitList());
					iWaitList.setEnabled(true);
				} else if (iWaitListMode == WaitListMode.NoSubs && value.isCanNoSub()) {
					iWaitList.setValue(value.isNoSub());
					iWaitList.setEnabled(true);
				} else {
					iWaitList.setValue(false);
					iWaitList.setEnabled(false);
				}
			}
			if (iCritical != null) {
				iCritical.setValue(value.getCritical() != null && value.getCritical() > 0 && value.hasCourseId());
				iCritical.setEnabled(value.hasCourseId());
			}
		}
		if (iDelete != null) {
			iDelete.setVisible(value == null || value.isCanDelete());
		}
		if (iPrevious != null) {
			boolean enabled = iPrevious.isCanChangeAlternatives() && isCanChangeAlternatives(); 
			setUpArrowEnabled(enabled); iPrevious.setDownArrowEnabled(enabled);
		}
		if (iNext != null) {
			boolean enabled = iNext.isCanChangeAlternatives() && isCanChangeAlternatives();
			setDownArrowEnabled(enabled); iNext.setUpArrowEnabled(enabled);
		}
		resizeNotes();
		computeCredits();
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}
	
	public String validate() {
		String failed = null;
		for (CourseSelectionBox box: iCourses) {
			String message = box.validate();
			if (failed == null && message != null) failed = message;
		}
		return failed;
	}
	
	public class CourseSelectionBox extends CourseRequestBox {
		private HandlerRegistration iCourseSelectionHandlerRegistration;
		private FilterStatus iStatus;
		private CourseFinderMultipleCourses iCourseFinderMultipleCourses;
		
		public CourseSelectionBox(boolean allowFreeTime, final boolean alternative) {
			super(CONSTANTS.showCourseTitle(), iSpecReg);
			if (allowFreeTime) {
				FreeTimeParser parser = new FreeTimeParser();
				setFreeTimes(parser);
			}
			
			setCourseFinderFactory(new CourseFinderFactory() {
				@Override
				public CourseFinder createCourseFinder() {
					CourseFinder finder = (alternative ? new CourseFinderDialog() : new SelectAllCourseFinderDialog());
					
					iCourseFinderMultipleCourses = new CourseFinderMultipleCourses(CONSTANTS.showCourseTitle(), CONSTANTS.courseFinderSuggestWhenEmpty(), CONSTANTS.courseFinderShowRequired(), iSpecReg, true);
					iCourseFinderMultipleCourses.setDataProvider(new DataProvider<String, Collection<CourseAssignment>>() {
						@Override
						public void getData(String source, AsyncCallback<Collection<CourseAssignment>> callback) {
							sSectioningService.listCourseOfferings(iContext, source, null, callback);
						}
					});
					CourseFinderDetails details = new CourseFinderDetails();
					details.setDataProvider(new DataProvider<CourseAssignment, String>() {
						@Override
						public void getData(CourseAssignment source, AsyncCallback<String> callback) {
							sSectioningService.retrieveCourseDetails(iContext, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
						}
					});
					CourseFinderClasses classes = new CourseFinderClasses(true, iSpecReg, iCourseFinderMultipleCourses.getRequiredCheckbox());
					classes.setDataProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
						@Override
						public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
							sSectioningService.listClasses(iContext, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
						}
					});
					iCourseFinderMultipleCourses.setCourseDetails(details, classes);
					if (getFreeTimes() != null) {
						CourseFinderFreeTime free = new CourseFinderFreeTime();
						free.setDataProvider(getFreeTimes());
						finder.setTabs(iCourseFinderMultipleCourses, free);
					} else {
						finder.setTabs(iCourseFinderMultipleCourses);
					}
					return finder;
				}
			});

			setSuggestions(new DataProvider<String, Collection<CourseAssignment>>() {
				@Override
				public void getData(String source, AsyncCallback<Collection<CourseAssignment>> callback) {
					sSectioningService.listCourseOfferings(iContext, source, 20, callback);
				}
			});
			setSectionsProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
				@Override
				public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
					sSectioningService.listClasses(iContext, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
				}
			});
			
			iCourseSelectionHandlerRegistration = addCourseSelectionHandler(new CourseSelectionHandler() {
				@Override
				public void onCourseSelection(CourseSelectionEvent event) {
					if (event.isValid()) setError("");
					CourseSelectionBox next = getNext();
					if (next != null) {
						if (event.getValue() == null || event.getValue().isFreeTime()) {
							next.setHint("");
						} else {
							next.resizeFilterIfNeeded();
							// next.setEnabled(event.isValid() || !next.getValue().isEmpty());
							if (event.isValid() && next.getValue().isEmpty()) {
								CourseSelectionBox prev = getPrevious();
								if (prev != null)
									next.setHint(MESSAGES.courseRequestsHintAlt2(prev.getText(), getText()));
								else
									next.setHint(MESSAGES.courseRequestsHintAlt(getText()));
							} else {
								next.setHint("");
							}
						}
					} else if (CONSTANTS.courseRequestAutomaticallyAddFirstAlternative() && !iAlternate && event.isValid() && event.getValue().isCourse() && event.getValue().isCanDelete() && getIndex() == 0) {
						insertAlternative(getCourses().size());
					}
					CourseSelectionBox prev = getPrevious();
					if (prev != null) {
						prev.resizeFilterIfNeeded();
					}
					ValueChangeEvent.fire(AdvisorCourseRequestLine.this, AdvisorCourseRequestLine.this.getValue());
					computeCredits();
				}
			});
			addValidator(new Validator<CourseSelection>() {
				public String validate(CourseSelection source) {
					if (getIndex() == 0) {
						if (isAlternate()) {
							if (getValue().isFreeTime()) return MESSAGES.validationAltFreeTime();
							
						}
					} else {
						if (!getValue().isEmpty() && getPrevious().getValue().isEmpty()) {
							if (getIndex() == 2)
								return MESSAGES.validationSecondAltWithoutFirst();
							return MESSAGES.validationNoCourse();
						}
						if (!getValue().isEmpty() && getPrevious().getValue().isFreeTime()) {
							return MESSAGES.validationFreeTimeWithAlt();
						}
						if (getValue().isFreeTime()) {
							return MESSAGES.validationAltFreeTime();
						}
					}
					return null;
				}
			});
			if (iValidator != null) addValidator(iValidator);
			if (alternative) {
				removeClearOperation();
				FilterOperation moveUp = new FilterOperation(RESOURCES.filterSwap(), 'S') {
					@Override
					public void onBeforeResize(CourseRequestFilterBox filter) {
						setVisible(isCanChangeAlternatives() && !filter.getText().isEmpty() && iCourses.size() != getIndex() + 1);
					}
				};
				moveUp.setTitle(MESSAGES.altFilterSwapWithAlternative());
				moveUp.setAltText(MESSAGES.altFilterSwapWithAlternative());
				moveUp.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						final CourseSelectionBox next = getNext();
						if (next != null && next.getValue().isCourse()) {
							RequestedCourse rc = getValue();
							setValue(next.getValue(), true);
							next.setValue(rc, true);
							Scheduler.get().scheduleDeferred(new ScheduledCommand() {
								@Override
								public void execute() {
									next.setFocus(true);
								}
							});
							return;
						}
						final CourseSelectionBox prev = getPrevious();
						if (prev != null) {
							RequestedCourse rc = prev.getValue();
							prev.setValue(getValue(), true);
							setValue(rc, true);
							Scheduler.get().scheduleDeferred(new ScheduledCommand() {
								@Override
								public void execute() {
									prev.setFocus(true);
								}
							});
						}
					}
				});
				addOperation(moveUp, true);
				
				FilterOperation remove = new FilterOperation(RESOURCES.filterRemoveAlternative(), 'X') {
					@Override
					public void onBeforeResize(CourseRequestFilterBox filter) {
						setVisible(isCanChangeAlternatives() && isEnabled());
					}
				};
				remove.setTitle(MESSAGES.altFilterRemoveAlternative());
				remove.setAltText(MESSAGES.altFilterRemoveAlternative());
				remove.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						final CourseRequestBox prev = getPrevious();
						deleteAlternative(getIndex());
						if (prev != null)
							Scheduler.get().scheduleDeferred(new ScheduledCommand() {
								@Override
								public void execute() {
									prev.setFocus(true);
								}
							});
					}
				});
				addOperation(remove, false);
				FilterOperation addAlternative = new FilterOperation(RESOURCES.filterAddAlternative(), 'A') {
					@Override
					public void onBeforeResize(CourseRequestFilterBox filter) {
						setVisible(isCanChangeAlternatives() && getValue().isCourse() && iCourses.size() == getIndex() + 1);
					}
				};
				addAlternative.setTitle(MESSAGES.altFilterAddAlternative());
				addAlternative.setAltText(MESSAGES.altFilterAddAlternative());
				addAlternative.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						insertAlternative(iCourses.size());
						Scheduler.get().scheduleDeferred(new ScheduledCommand() {
							@Override
							public void execute() {
								iCourses.get(iCourses.size()-1).setFocus(true);
							}
						});
					}
				});
				addOperation(addAlternative, true);
			} else {
				removeClearOperation();
				FilterOperation moveDown = new FilterOperation(RESOURCES.filterSwap(), 'S') {
					@Override
					public void onBeforeResize(CourseRequestFilterBox filter) {
						CourseSelectionBox next = getNext();
						setVisible(isCanChangeAlternatives() && !filter.getText().isEmpty() && next != null && next.getValue().isCourse());
					}
				};
				moveDown.setTitle(MESSAGES.altFilterSwapWithAlternative());
				moveDown.setAltText(MESSAGES.altFilterSwapWithAlternative());
				moveDown.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						final CourseSelectionBox next = getNext();
						if (next != null) {
							RequestedCourse rc = getValue();
							setValue(next.getValue(), true);
							next.setValue(rc, true);
							Scheduler.get().scheduleDeferred(new ScheduledCommand() {
								@Override
								public void execute() {
									next.setFocus(true);
								}
							});
						}
					}
				});
				addOperation(moveDown, true);
				
				FilterOperation remove = new FilterOperation(RESOURCES.filterRemoveAlternative(), 'X') {
					@Override
					public void onBeforeResize(CourseRequestFilterBox filter) {
						setVisible(isCanChangeAlternatives() && isEnabled());
					}
				};
				remove.setTitle(MESSAGES.altFilterClearCourseRequest());
				remove.setAltText(MESSAGES.altFilterClearCourseRequest());
				remove.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (iCourses.size() > 1) {
							RequestedCourse rc = iCourses.get(1).getValue();
							setValue(rc, true);
							deleteAlternative(1);
						} else if (!getValue().isEmpty()) {
							setValue((RequestedCourse)null, true);
						} else {
							delete();
						}
					}
				});
				addOperation(remove, false);
				FilterOperation addAlternative = new FilterOperation(RESOURCES.filterAddAlternative(), 'A') {
					@Override
					public void onBeforeResize(CourseRequestFilterBox filter) {
						setVisible(isCanChangeAlternatives() && getValue().isCourse() && iCourses.size() == 1);
					}
				};
				addAlternative.setTitle(MESSAGES.altFilterAddAlternative());
				addAlternative.setAltText(MESSAGES.altFilterAddAlternative());
				addAlternative.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						insertAlternative(iCourses.size());
						Scheduler.get().scheduleDeferred(new ScheduledCommand() {
							@Override
							public void execute() {
								iCourses.get(iCourses.size()-1).setFocus(true);
							}
						});
					}
				});
				addOperation(addAlternative, true);
			}
			
			iStatus = new FilterStatus(RESOURCES.requestEnrolled()); iStatus.clearStatus();
			addStatus(iStatus);
			iError.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					iStatus.click();
				}
			});
		}
		
		@Override
		public void select(RequestedCourse rc) {
			if (isEnabled()) {
				if (iCourseFinderMultipleCourses != null && !iCourseFinderMultipleCourses.getCheckedCourses().isEmpty()) {
					List<RequestedCourse> list = iCourseFinderMultipleCourses.getCheckedCourses();
					int courses = list.size();
					int index = iCourses.indexOf(this);
					if (index == 0 && courses == 1) courses = 2;
					while (iCourses.size() < courses + index) {
						insertAlternative(iCourses.size());
					}
					while (iCourses.size() > courses + index) {
						deleteAlternative(iCourses.size() - 1);
					}
					for (int i = 0; i < courses; i++)
						iCourses.get(index + i).setValue(i < list.size() ? list.get(i) : null, true);
				} else {
					setValue(rc, true);
				}
			}
		}
		
		@Override
		protected void openDialog() {
			if (iCourseFinderMultipleCourses != null && iCourseFinderMultipleCourses.getLastQuery() != null) {
				getCourseFinder().setFilter(iCourseFinderMultipleCourses.getLastQuery());
				List<RequestedCourse> list = new ArrayList<RequestedCourse>();
				int index = iCourses.indexOf(this);
				for (int i = index; i < iCourses.size(); i++) {
					CourseSelectionBox box = iCourses.get(i);
					RequestedCourse rc = box.getValue();
					if (rc != null && rc.isCourse())
						list.add(rc);
				}
				if (iCourseFinderMultipleCourses.setCheckedCourses(list)) {
					getCourseFinder().setEnabled(isEnabled());
					getCourseFinder().findCourse();
					iCourseFinderMultipleCourses.scrollToSelectedRow();
				} else {
					super.openDialog();
				}
			} else {
				super.openDialog();
			}
		}
		
		public void setStatus(ImageResource icon, String message) {
			if (iStatus != null) {
				iStatus.setStatus(icon, message);
				iError.getElement().getStyle().setCursor(Cursor.POINTER);
				resizeFilterIfNeeded();
			}
		}
		
		public void clearStatus() {
			if (iStatus != null) {
				iStatus.clearStatus();
				iError.getElement().getStyle().clearCursor();
				resizeFilterIfNeeded();
			}
		}
		
		@Override
		public void setValue(RequestedCourse rc) {
			super.setValue(rc);
			if (rc == null || rc.getStatus() == null) {
				clearStatus();
			} else {
				switch (rc.getStatus()) {
				case ENROLLED:
					setStatus(RESOURCES.requestEnrolled(), MESSAGES.enrolled(rc.getCourseName()));
					break;
				case OVERRIDE_REJECTED:
					setStatus(RESOURCES.requestRejected(), MESSAGES.overrideRejected(rc.getCourseName()));
					break;
				case OVERRIDE_PENDING:
					setStatus(RESOURCES.requestPending(), MESSAGES.overridePending(rc.getCourseName()));
					break;
				case OVERRIDE_CANCELLED:
					setStatus(RESOURCES.requestCancelled(), MESSAGES.overrideCancelled(rc.getCourseName()));
					break;
				case OVERRIDE_APPROVED:
					setStatus(RESOURCES.requestSaved(), MESSAGES.overrideApproved(rc.getCourseName()));
					break;
				case OVERRIDE_NOT_NEEDED:
					setStatus(RESOURCES.requestNotNeeded(), MESSAGES.overrideNotNeeded(rc.getCourseName()));
					break;
				case NEW_REQUEST:
					clearStatus();
					break;
				default:
					if (rc.isCourse())
						setStatus(RESOURCES.requestSaved(), MESSAGES.requested(rc.getCourseName()));
					else if (rc.isFreeTime()) {
						String  free = "";
						for (FreeTime ft: rc.getFreeTime()) {
							if (!free.isEmpty()) free += ", ";
							free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
						}
						setStatus(RESOURCES.requestSaved(), MESSAGES.requested(CONSTANTS.freePrefix() + free));
					}
				}
			}
			if (rc != null && rc.isFreeTime()) {
				for (int i = iCourses.size() - 1; i > 0; i--) {
					deleteAlternative(i);
				}				
			}
			CourseSelectionBox prev = getPrevious();
			// if (rc == null && prev != null && !prev.getValue().isCourse()) setEnabled(false);
			if (prev != null) {
				if ((rc != null && rc.isReadOnly()) || prev.getValue().isFreeTime()) {
					setHint("");
				} else {
					if (getIndex() == 1)
						setHint(MESSAGES.courseRequestsHintAlt(prev.getText()));
					else if (getIndex() == 2)
						setHint(MESSAGES.courseRequestsHintAlt2(iCourses.get(0).getText(), iCourses.get(1).getText()));
					else
						setHint(MESSAGES.courseRequestsHintAlt3(iCourses.get(0).getText(), iCourses.get(1).getText()));
				}
			}
		}
		
		public int getIndex() {
			return iCourses.indexOf(this);
		}
		
		public CourseSelectionBox getPrevious() {
			return (getIndex() > 0 ? iCourses.get(getIndex() - 1) : null);
		}
		
		public CourseSelectionBox getNext() {
			return (getIndex() + 1 < iCourses.size() ? iCourses.get(getIndex() + 1) : null);
		}
		
		public void dispose() {
			iCourseSelectionHandlerRegistration.removeHandler();
		}

		public void setErrors(CheckCoursesResponse messages) {
			String message = null;
			for (CourseMessage m: messages.getMessages(getText())) {
				if (message == null) {
					message = m.getMessage();
				} else {
					message += "\n" + m.getMessage();
				}
			}
			if (message != null) {
				String note = "";
				if (getValue().hasStatusNote()) note += "\n<span class='status-note'>" + getValue().getStatusNote() + "</span>";
				if (messages.isError(getText()))
					setError(message + note);
				else
					setWarning(message + note);
			} else {
				setInfo(null);
			}
		}
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		iP.fireEvent(event);
	}
}
