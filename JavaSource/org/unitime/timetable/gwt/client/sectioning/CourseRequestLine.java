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

import org.unitime.timetable.gwt.client.aria.AriaCheckBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.widgets.CourseFinder;
import org.unitime.timetable.gwt.client.widgets.CourseFinderClasses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderCourses;
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
import org.unitime.timetable.gwt.client.widgets.Validator;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;

/**
 * @author Tomas Muller
 */
public class CourseRequestLine extends P implements HasValue<Request> {
	protected static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	protected static final SectioningServiceAsync sSectioningService = GWT.create(SectioningService.class);
	
	private boolean iAlternate;
	private int iPriority;
	private AcademicSessionProvider iSessionProvider;
	private List<CourseSelectionBox> iCourses = new ArrayList<CourseSelectionBox>();
	private AriaCheckBox iWaitList = null;
	private CourseRequestLine iPrevious = null, iNext = null;
	private Validator<CourseSelection> iValidator = null;
	private SpecialRegistrationContext iSpecReg;
	private boolean iSectioning, iOnline;
	private ImageButton iDelete;
	
	public CourseRequestLine(boolean online, AcademicSessionProvider session, int priority, boolean alternate, Validator<CourseSelection> validator, boolean sectioning, SpecialRegistrationContext specreg) {
		super("unitime-CourseRequestLine");
		iOnline = online;
		iSessionProvider = session;
		iValidator = validator;
		iPriority = priority;
		iSectioning = sectioning;
		iAlternate = alternate;
		iSpecReg = specreg;
		
		P line = new P("line");
		if (alternate) line.addStyleName("alternative");
		
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
		
		P buttons = new P("buttons");
		if (!alternate) {
			iWaitList = new AriaCheckBox();
			iWaitList.setAriaLabel(ARIA.titleRequestedWaitList(1 + priority));
			iWaitList.addStyleName("wait-list");
			iWaitList.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					ValueChangeEvent.fire(CourseRequestLine.this, getValue());
				}
			});
			buttons.add(iWaitList);
		} else {
			addStyleName("nowaitlist");
		}
		
		P up = new P("blank");
		buttons.add(up);
		
		P down = new P("blank");
		buttons.add(down);

		iDelete = new ImageButton(RESOURCES.delete(), RESOURCES.delete_Down(), RESOURCES.delete_Over());
		iDelete.addStyleName("unitime-NoPrint");
		iDelete.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				delete();
				ValueChangeEvent.fire(CourseRequestLine.this, getValue());
			}
		});
		iDelete.addStyleName("delete");
		iDelete.setAltText(ARIA.altDeleteRequest(priority + 1));
		buttons.add(iDelete);
		line.add(buttons);
		add(line);
	}
	
	public boolean isWaitListVisible() {
		return iWaitList != null && iWaitList.isVisible();
	}
	
	public void setWaitListVisible(boolean visible) {
		if (iWaitList != null) {
			iWaitList.setVisible(visible);
			changeVisibleStyle();
		}
	}
	
	public boolean getWaitList() { return iWaitList != null && iWaitList.getValue(); }
	public void setWaitList(boolean value) {
		if (iWaitList != null) iWaitList.setValue(value);
	}
	
	public void setPrevious(CourseRequestLine previous) {
		iPrevious = previous;
		P line = (P)getWidget(0);
		P buttons = (P)line.getWidget(2);
		int idx = (iWaitList == null ? 0 : 1);
		if (iPrevious == null) {
			if (buttons.getWidget(idx) instanceof ImageButton) {
				buttons.remove(idx);
				P up = new P("blank");
				buttons.add(up);
				buttons.insert(up, idx);
			}
		} else {
			ImageButton up = null;
			if (buttons.getWidget(idx) instanceof ImageButton) {
				up = (ImageButton)buttons.getWidget(idx);
			} else {
				buttons.remove(idx);
				up = new ImageButton(RESOURCES.up(), RESOURCES.up_Down(), RESOURCES.up_Over());
				up.addStyleName("unitime-NoPrint");
				up.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						up();
						ValueChangeEvent.fire(CourseRequestLine.this, getValue());
					}
				});
				up.addStyleName("up");
				buttons.insert(up, idx);
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
	
	public void setNext(CourseRequestLine next) {
		iNext = next;
		P line = (P)getWidget(0);
		P buttons = (P)line.getWidget(2);
		int idx = (iWaitList == null ? 1 : 2);
		if (iNext == null) {
			if (buttons.getWidget(idx) instanceof ImageButton) {
				buttons.remove(idx);
				P down = new P("blank");
				buttons.add(down);
				buttons.insert(down, idx);
			}
		} else {
			ImageButton down = null;
			if (buttons.getWidget(idx) instanceof ImageButton) {
				down = (ImageButton)buttons.getWidget(idx);
			} else {
				buttons.remove(idx);
				down = new ImageButton(RESOURCES.down(), RESOURCES.down_Down(), RESOURCES.down_Over());
				down.addStyleName("unitime-NoPrint");
				down.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						down();
						ValueChangeEvent.fire(CourseRequestLine.this, getValue());
					}
				});
				down.addStyleName("down");
				buttons.insert(down, idx);
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
	
	public List<? extends CourseSelectionBox> getCourses() {
		return iCourses;
	}
	
	public void fixTitles() {
		for (int i = 0; i < getWidgetCount(); i++) {
			P line = (P)getWidget(i);
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
	}
	
	public boolean isAlternate() { return iAlternate; }
	public int getPriority() { return iPriority; }
	
	public void delete() {
		if (iNext != null && isAlternate() == iNext.isAlternate()) {
			setValue(iNext.getValue());
			iNext.delete();
		} else {
			iCourses.get(0).setValue(null);
			if (iWaitList != null && iWaitList.isVisible()) { iWaitList.setEnabled(true); iWaitList.setValue(false); }
			for (int i = iCourses.size() - 1; i > 0; i--) {
				deleteAlternative(i);
			}
		}
	}
	
	public void deleteAlternative(int index) {
		iCourses.remove(index).dispose();
		remove(index);
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

		insert(line, index);
		box.setValue(null);
		fixTitles();
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Request> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Request getValue() {
		Request ret = new Request();
		for (CourseSelectionBox box: iCourses) {
			RequestedCourse rc = box.getValue();
			if (!rc.isEmpty()) ret.addRequestedCourse(rc);
		}
		if (iWaitList != null && iWaitList.isVisible()) {
			ret.setWaitList(iWaitList.getValue());
		}
		return (ret.isEmpty() ? null : ret);
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
		P line = (P)getWidget(0);
		P buttons = (P)line.getWidget(2);
		int upIdx = (iWaitList == null ? 0 : 1);
		if (buttons.getWidget(upIdx) instanceof ImageButton) {
			((ImageButton)buttons.getWidget(upIdx)).setEnabled(enabled);
			((ImageButton)buttons.getWidget(upIdx)).setVisible(areArrowsVisible() && enabled);
		}
	}

	public void setDownArrowEnabled(boolean enabled) {
		P line = (P)getWidget(0);
		P buttons = (P)line.getWidget(2);
		int downIdx = (iWaitList == null ? 1 : 2);
		if (buttons.getWidget(downIdx) instanceof ImageButton) {
			((ImageButton)buttons.getWidget(downIdx)).setEnabled(enabled);
			((ImageButton)buttons.getWidget(downIdx)).setVisible(areArrowsVisible() && enabled);
		}
	}

	@Override
	public void setValue(Request value, boolean fireEvents) {
		if (value == null) {
			if (iWaitList != null) iWaitList.setValue(false);
			iCourses.get(0).setValue(null, true);
			for (int i = iCourses.size() - 1; i > 0; i--)
				deleteAlternative(i);
		} else {
			if (iWaitList != null) iWaitList.setValue(value.isWaitList());
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
		}
		if (iWaitList != null && iWaitList.isVisible()) {
			iWaitList.setEnabled(value == null || !value.isReadOnly());
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
					CourseFinder finder = new CourseFinderDialog();
					
					CourseFinder.CourseFinderTab<Collection<CourseAssignment>> courses = null;
					if (!alternative) {
						iCourseFinderMultipleCourses = new CourseFinderMultipleCourses(CONSTANTS.showCourseTitle(), CONSTANTS.courseFinderSuggestWhenEmpty(), CONSTANTS.courseFinderShowRequired(), iSpecReg);
						courses = iCourseFinderMultipleCourses;
					} else {
						courses = new CourseFinderCourses(CONSTANTS.showCourseTitle(), CONSTANTS.courseFinderSuggestWhenEmpty(), CONSTANTS.courseFinderShowRequired(), iSpecReg);
					}
					courses.setDataProvider(new DataProvider<String, Collection<CourseAssignment>>() {
						@Override
						public void getData(String source, AsyncCallback<Collection<CourseAssignment>> callback) {
							sSectioningService.listCourseOfferings(iSessionProvider.getAcademicSessionId(), source, null, callback);
						}
					});
					CourseFinderDetails details = new CourseFinderDetails();
					details.setDataProvider(new DataProvider<CourseAssignment, String>() {
						@Override
						public void getData(CourseAssignment source, AsyncCallback<String> callback) {
							sSectioningService.retrieveCourseDetails(iSessionProvider.getAcademicSessionId(), source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
						}
					});
					CourseFinderClasses classes = new CourseFinderClasses(true, iSpecReg, courses.getRequiredCheckbox());
					classes.setDataProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
						@Override
						public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
							sSectioningService.listClasses(iOnline, iSessionProvider.getAcademicSessionId(), source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
						}
					});
					courses.setCourseDetails(details, classes);
					if (getFreeTimes() != null) {
						CourseFinderFreeTime free = new CourseFinderFreeTime();
						free.setDataProvider(getFreeTimes());
						finder.setTabs(courses, free);
					} else {
						finder.setTabs(courses);
					}
					return finder;
				}
			});

			setSuggestions(new DataProvider<String, Collection<CourseAssignment>>() {
				@Override
				public void getData(String source, AsyncCallback<Collection<CourseAssignment>> callback) {
					sSectioningService.listCourseOfferings(iSessionProvider.getAcademicSessionId(), source, 20, callback);
				}
			});
			setSectionsProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
				@Override
				public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
					sSectioningService.listClasses(iOnline, iSessionProvider.getAcademicSessionId(), source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
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
					ValueChangeEvent.fire(CourseRequestLine.this, CourseRequestLine.this.getValue());
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
						setVisible(isCanChangeAlternatives() && (!iSectioning || isEnabled()) && !filter.getText().isEmpty() && iCourses.size() != getIndex() + 1);
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
						setVisible(isCanChangeAlternatives() && (!iSectioning || isEnabled()) && getValue().isCourse() && iCourses.size() == getIndex() + 1);
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
						setVisible(isCanChangeAlternatives() && (!iSectioning || isEnabled()) && !filter.getText().isEmpty() && next != null && next.getValue().isCourse());
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
						setVisible(isCanChangeAlternatives() && (!iSectioning || isEnabled()) && getValue().isCourse() && iCourses.size() == 1);
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
			
			if (!iSectioning) {
				iStatus = new FilterStatus(RESOURCES.requestEnrolled()); iStatus.clearStatus();
				addStatus(iStatus);
			}
		}
		
		@Override
		public void select(RequestedCourse rc) {
			if (isEnabled()) {
				if (iCourseFinderMultipleCourses != null && !iCourseFinderMultipleCourses.getCheckedCourses().isEmpty()) {
					List<RequestedCourse> list = iCourseFinderMultipleCourses.getCheckedCourses();
					int courses = list.size();
					if (courses == 1) courses = 2;
					while (iCourses.size() < courses) {
						insertAlternative(iCourses.size());
					}
					while (iCourses.size() > courses) {
						deleteAlternative(iCourses.size() - 1);
					}
					for (int i = 0; i < courses; i++)
						iCourses.get(i).setValue(i < list.size() ? list.get(i) : null, true);
				} else {
					setValue(rc, true);
				}
			}
		}
		
		public void setStatus(ImageResource icon, String message) {
			if (iStatus != null) {
				iStatus.setStatus(icon, message);
				resizeFilterIfNeeded();
			}
		}
		
		public void clearStatus() {
			if (iStatus != null) {
				iStatus.clearStatus();
				resizeFilterIfNeeded();
			}
		}
		
		@Override
		public void setValue(RequestedCourse rc) {
			super.setValue(rc);
			if (iSectioning || rc == null || rc.getStatus() == null) {
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
	}
	
	private boolean iArrowsVisible = true;
	public void setArrowsVisible(boolean visible) {
		iArrowsVisible = visible;
		changeVisibleStyle();
	}
	public boolean areArrowsVisible() {
		return iArrowsVisible;
	}
	
	protected void changeVisibleStyle() {
		setStyleName("noarrows", !areArrowsVisible() && isWaitListVisible());
		setStyleName("nowaitlist", areArrowsVisible() && !isWaitListVisible());
		setStyleName("noarrowswaitlist", !areArrowsVisible() && !isWaitListVisible());
	}
}
