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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.HasAriaLabel;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.client.widgets.FilterBox.SuggestionsProvider;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.IdValue;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class CourseRequestBox extends P implements CourseSelection {
	protected static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	private CourseRequestFilterBox iFilter;
	protected HTML iError;
	
	private Map<String, CourseAssignment> iValidCourseNames = new HashMap<String, CourseAssignment>();

	private DataProvider<String, Collection<CourseAssignment>> iDataProvider;
	private DataProvider<CourseAssignment, Collection<ClassAssignment>> iSectionProvider;
	private FreeTimeParser iFreeTimeParser = null;
	private CourseFinder iCourseFinder = null;
	private CourseFinderFactory iCourseFinderFactory = null;
	private List<Validator<CourseSelection>> iValidators = new ArrayList<Validator<CourseSelection>>();
	
	private boolean iShowCourses = false;
	private RequestedCourse iLastCourse = null;
	private SpecialRegistrationContext iSpecReg;
	private boolean iCanDelete = true;
	private boolean iCanChangePriority = true;
	private boolean iCanChangeAlternatives = true;
	
	public CourseRequestBox() {
		this(false, null);
	}
	
	public CourseRequestBox(boolean showCourses) {
		this(showCourses, null);
	}
	
	
	public CourseRequestBox(boolean showCourses, SpecialRegistrationContext specreg) {
		super("unitime-CourseRequestBox");
		iShowCourses = showCourses;
		iSpecReg = specreg;
		
		iFilter = new CourseRequestFilterBox(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// if (isEnabled())
				openDialogAsync();
			}
		}) {
			@Override
			protected void applySuggestion(Suggestion suggestion) {
				if (suggestion instanceof CourseSuggestion) {
					CourseRequestBox.this.setValue(((CourseSuggestion)suggestion).getRequestedCourse());
				} else {
					super.applySuggestion(suggestion);
				}
			}
		};
		add(iFilter);
		
		iFilter.addFilter(new FilterBox.StaticSimpleFilter("method", MESSAGES.tagInstructionalMethod()));
		iFilter.addFilter(new FilterBox.StaticSimpleFilter("section", MESSAGES.tagSection()));

		iFilter.setSuggestionsProvider(new SuggestionsProvider() {
			@Override
			public void getSuggestions(List<Chip> chips, final String text, final AsyncCallback<Collection<Suggestion>> callback) {
				if (text.equals(getHint())) return;
				if (iLastCourse != null && iLastCourse.isCourse() && text.startsWith(iLastCourse.getCourseName() + " ") && iValidCourseNames.containsKey(iLastCourse.getCourseName().toLowerCase())) {
					final CourseAssignment course = iValidCourseNames.get(iLastCourse.getCourseName().toLowerCase());
					final String query = text.substring(iLastCourse.getCourseName().length() + 1).trim();
					iSectionProvider.getData(course, new AsyncCallback<Collection<ClassAssignment>>() {
						@Override
						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
						}
						
						@Override
						public void onSuccess(Collection<ClassAssignment> result) {
							List<Suggestion> suggestions = new ArrayList<Suggestion>();
							if (course.hasInstructionalMethods()) {
								for (IdValue im: course.getInstructionalMethods()) {
									if (im.getValue().toLowerCase().startsWith(query.toLowerCase())) {
										suggestions.add(new CourseSuggestion(course, im, false));
										if (iSpecReg == null || iSpecReg.isCanRequire())
											suggestions.add(new CourseSuggestion(course, im, true));
									} else if ((im.getValue() + "!").equalsIgnoreCase(query) && (iSpecReg == null || iSpecReg.isCanRequire())) {
										suggestions.add(new CourseSuggestion(course, im, true));
									}
								}
							}
							for (ClassAssignment clazz: result) {
								if (clazz.isCancelled() || (!clazz.isSaved() && !clazz.isAvailable() && !isSpecialRegistration())) continue;
								if (clazz.getSection().equalsIgnoreCase(query) || clazz.getSelection().getText().equalsIgnoreCase(query)) {
									suggestions.add(new CourseSuggestion(course, clazz, false));
									if (iSpecReg == null || iSpecReg.isCanRequire())
										suggestions.add(new CourseSuggestion(course, clazz, true));
								} else if (((clazz.getSection() + "!").equalsIgnoreCase(query) || (clazz.getSelection().getText() + "!").equalsIgnoreCase(query)) && (iSpecReg == null || iSpecReg.isCanRequire())) {
									suggestions.add(new CourseSuggestion(course, clazz, true));
								} else if (clazz.getSection().toLowerCase().startsWith(query.toLowerCase()) || clazz.getSelection().getText().toLowerCase().startsWith(query.toLowerCase())) {
									suggestions.add(new CourseSuggestion(course, clazz, false));
								} else if (clazz.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()).toLowerCase().startsWith(query.toLowerCase()))
									suggestions.add(new CourseSuggestion(course, clazz, false));
								else if (clazz.hasInstructors())
									for (String instructor: clazz.getInstructors())
										if (instructor.toLowerCase().startsWith(query.toLowerCase())) {
											suggestions.add(new CourseSuggestion(course, clazz, false));
											break;
										}
							}
							callback.onSuccess(suggestions);
						}
					});
					return;
				}
				iDataProvider.getData(text, new AsyncCallback<Collection<CourseAssignment>>(){
					@Override
					public void onFailure(Throwable caught) {
						iValidCourseNames.clear();
						if (iFreeTimeParser != null) {
							iFreeTimeParser.getData(text, new AsyncCallback<List<FreeTime>>() {
								@Override
								public void onFailure(Throwable e) {
									callback.onFailure(e);
								}
								@Override
								public void onSuccess(List<FreeTime> freeTimes) {
									String ft = iFreeTimeParser.freeTimesToString(freeTimes);
									List<Suggestion> suggestions = new ArrayList<Suggestion>();
									Suggestion suggestion = new Suggestion(ft, ft, MESSAGES.hintFreeTimeRequest());
									suggestions.add(suggestion);
									callback.onSuccess(suggestions);
								}
							});
						} else {
							callback.onFailure(caught);
						}
					}

					@Override
					public void onSuccess(Collection<CourseAssignment> result) {
						iValidCourseNames.clear();
						List<Suggestion> suggestions = new ArrayList<Suggestion>();
						if (result != null)
							for (CourseAssignment course: result) {
								Suggestion suggestion = new CourseSuggestion(course);
								suggestions.add(suggestion);
								iValidCourseNames.put(suggestion.getReplacementString().toLowerCase(), course);
								if (course.getClassAssignments() != null) {
									for (ClassAssignment clazz: course.getClassAssignments())
										suggestions.add(new CourseSuggestion(course, clazz, false));
								}
								if (result.size() <= 5 && course.hasInstructionalMethodSelection()) {
									for (IdValue im: course.getInstructionalMethods()) {
										suggestions.add(new CourseSuggestion(course, im, false));
									}
								}
							}
						callback.onSuccess(suggestions);
					}
				});
			}
		});
		
		iError = new HTML();
		iError.setStyleName("unitime-ErrorHint");
		iError.setVisible(false);
		Roles.getPresentationRole().setAriaHiddenState(iError.getElement(), true);
		add(iError);
		
		iFilter.getValueBox().addBlurHandler(new BlurHandler() {
			public void onBlur(BlurEvent event) {
				if (getText().isEmpty() && iError.isVisible()) iError.setVisible(false);
			}
		});
		iFilter.getValueBox().addFocusHandler(new FocusHandler() {
			public void onFocus(FocusEvent event) {
				if (iError.isVisible() && !iError.getText().isEmpty())
					AriaStatus.getInstance().setText(iError.getText());
			}
		});
		iFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String value = iFilter.getText();
				if (iLastCourse == null || !iLastCourse.isCourse() || !value.startsWith(iLastCourse.getCourseName())) {
					iFilter.removeAllChips();
				} else if (!value.equals(iLastCourse.getCourseName())) {
					iFilter.setText(iLastCourse.getCourseName());
				}
				iFilter.resizeFilterIfNeeded();
				CourseSelectionEvent.fire(CourseRequestBox.this, getValue());
			}
		});
		iFilter.addSelectionHandler(new SelectionHandler<FilterBox.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				iLastCourse = getValue();
			}
		});
	}
	
	protected boolean isSpecialRegistration() {
		return iSpecReg != null && iSpecReg.isEnabled();
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<RequestedCourse> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public RequestedCourse getValue() {
		String courseName = iFilter.getText().trim();
		if (courseName.isEmpty()) return new RequestedCourse();
		if (iLastCourse != null && iLastCourse.isCourse() && courseName.startsWith(iLastCourse.getCourseName() + " "))
			courseName = iLastCourse.getCourseName();
		CourseAssignment course = iValidCourseNames.get(courseName.toLowerCase());
		RequestedCourse ret = new RequestedCourse();
		if (course != null) {
			ret.setCourseId(course.getCourseId());
			ret.setCourseName(course.getCourseName());
			ret.setCourseTitle(course.getTitle());
			ret.setCredit(course.guessCreditRange());
			if (iLastCourse != null && iLastCourse.isCourse() && iLastCourse.hasCourseId() && courseName.equalsIgnoreCase(iLastCourse.getCourseName())) {
				ret.setStatus(iLastCourse.getStatus());
				ret.setStatusNote(iLastCourse.getStatusNote());
				ret.setOverrideExternalId(iLastCourse.getOverrideExternalId());
				ret.setOverrideTimeStamp(iLastCourse.getOverrideTimeStamp());
				ret.setRequestId(iLastCourse.getRequestId());
				ret.setRequestorNote(iLastCourse.getRequestorNote());
			} else
				ret.setStatus(RequestedCourseStatus.NEW_REQUEST);
		} else if (iLastCourse != null && iLastCourse.isCourse() && iLastCourse.hasCourseId() && courseName.equalsIgnoreCase(iLastCourse.getCourseName())) {
			ret.setCourseId(iLastCourse.getCourseId());
			ret.setCourseName(courseName);
			ret.setCourseTitle(iLastCourse.getCourseTitle());
			ret.setCredit(iLastCourse.getCredit());
			ret.setStatus(iLastCourse.getStatus());
			ret.setOverrideExternalId(iLastCourse.getOverrideExternalId());
			ret.setOverrideTimeStamp(iLastCourse.getOverrideTimeStamp());
			ret.setStatusNote(iLastCourse.getStatusNote());
			ret.setRequestId(iLastCourse.getRequestId());
			ret.setRequestorNote(iLastCourse.getRequestorNote());
		} else if (iFreeTimeParser != null) {
			try {
				ret.setFreeTime(iFreeTimeParser.parseFreeTime(courseName));
			} catch (IllegalArgumentException e) {
				ret.setCourseName(courseName);
			}
		} else {
			ret.setCourseName(courseName);
		}
		for (Chip chip: iFilter.getChips("section")) {
			if (chip instanceof PreferenceChip)
				ret.setSelectedClass(((PreferenceChip)chip).getPreference(), true);
		}
		for (Chip chip: iFilter.getChips("method")) {
			if (chip instanceof PreferenceChip)
				ret.setSelectedIntructionalMethod(((PreferenceChip)chip).getPreference(), true);
		}
		if (!iFilter.isEnabled() && ret.isCourse()) ret.setReadOnly(true);
		ret.setCanDelete(iCanDelete);
		ret.setCanChangePriority(iCanChangePriority);
		ret.setCanChangeAlternatives(iCanChangeAlternatives);
		return ret;
		
	}

	@Override
	public void setValue(RequestedCourse value) {
		iLastCourse = value;
		iFilter.removeAllChips();
		if (value == null || value.isEmpty()) {
			iFilter.setText("");
			setError(null);
			setEnabled(true);
			iCanDelete = true;
			iCanChangePriority = true;
			iCanChangeAlternatives = true;
		} else {
			if (value.isCourse()) {
				iFilter.setText(value.getCourseName());
				if (value.hasSelectedIntructionalMethods())
					for (Preference im: value.getSelectedIntructionalMethods())
						iFilter.addChip(new PreferenceChip("method", im).withTranslatedCommand(MESSAGES.tagInstructionalMethod()), false);
				if (value.hasSelectedClasses())
					for (Preference clazz: value.getSelectedClasses())
						iFilter.addChip(new PreferenceChip("section", clazz).withTranslatedCommand(MESSAGES.tagSection()), false);
			} else if (value.isFreeTime() && iFreeTimeParser != null) {
				iFilter.setText(iFreeTimeParser.freeTimesToString(value.getFreeTime()));
			} else {
				iFilter.setText("");
			}
			setEnabled(!value.isReadOnly());
			iCanDelete = value.isCanDelete();
			iCanChangePriority = value.isCanChangePriority();
			iCanChangeAlternatives = value.isCanChangeAlternatives();
		}
		iFilter.resizeFilterIfNeeded();
	}
	
	public boolean hasValue() {
		RequestedCourse value = getValue();
		return value != null && !value.isEmpty();
	}
	
	public boolean isCanDelete() {
		return iCanDelete;
	}
	
	public boolean isCanChangePriority() {
		return iCanChangePriority;
	}
	
	
	public boolean isCanChangeAlternatives() {
		return iCanChangeAlternatives;
	}

	@Override
	public void setValue(RequestedCourse value, boolean fireEvents) {
		setValue(value);
		if (fireEvents)
			CourseSelectionEvent.fire(CourseRequestBox.this, value);		
	}
	
	@Override
	public void setHint(String hint) {
		iFilter.setHint(hint);
	}

	@Override
	public String getHint() {
		return iFilter.getHint();
	}

	@Override
	public boolean isEnabled() {
		return iFilter.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			iFilter.setEnabled(true);
			iFilter.setFilterFinderFace(null);
			iFilter.getFilterFinderButton().setTitle(iFilter.getFilterFinderButton().getAltText());
		} else {
			iFilter.setEnabled(false);
			if (!getText().isEmpty()) {
				iFilter.setFilterFinderFace(RESOURCES.finderAssigned());
				iFilter.getFilterFinderButton().setTitle(MESSAGES.saved(getText()));
			} else {
				iFilter.setFilterFinderFace(null);
				iFilter.getFilterFinderButton().setTitle(iFilter.getFilterFinderButton().getAltText());
			}
		}
	}
	
	@Override
	public void setSuggestions(DataProvider<String, Collection<CourseAssignment>> provider) {
		iDataProvider = provider;
	}
	
	public void setSectionsProvider(DataProvider<CourseAssignment, Collection<ClassAssignment>> provider) {
		iSectionProvider = provider;
	}

	@Override
	public void setFreeTimes(FreeTimeParser parser) {
		iFreeTimeParser = parser;
		iFreeTimeParser.setValidCourseNames(iValidCourseNames);
	}
	
	@Override
	public FreeTimeParser getFreeTimes() {
		return iFreeTimeParser;
	}
	
	public boolean isAllowFreeTime() {
		return iFreeTimeParser != null;
	}
	
	@Override
	public void setCourseFinderFactory(CourseFinderFactory factory) {
		iCourseFinderFactory = factory;
	}
	
	@Override
	public CourseFinder getCourseFinder() {
		if (iCourseFinder == null) {
			iCourseFinder = iCourseFinderFactory.createCourseFinder();
			iCourseFinder.addSelectionHandler(new SelectionHandler<RequestedCourse>() {
				@Override
				public void onSelection(SelectionEvent<RequestedCourse> event) {
					select(event.getSelectedItem());
				}
			});
			if (iCourseFinder instanceof HasCloseHandlers) {
				((HasCloseHandlers)iCourseFinder).addCloseHandler(new CloseHandler<PopupPanel>() {
					public void onClose(CloseEvent<PopupPanel> event) {
						Scheduler.get().scheduleDeferred(new ScheduledCommand() {
							public void execute() {
								iFilter.setFocus(true);
							}
						});
					}
				});
			}
		}
		return iCourseFinder;
	}
	
	public void select(RequestedCourse rc) {
		if (isEnabled())
			setValue(rc, true);
	}

	@Override
	public void setLabel(String title, String finderTitle) {
		iFilter.setAriaLabel(title);
		iFilter.getFilterFinderButton().setAltText(finderTitle);
	}
	
	@Override
	public void hideSuggestionList() {
		iFilter.hideSuggestions();
	}
	
	@Override
	public void showSuggestionList() {
		iFilter.showSuggestions();
	}

	@Override
	public void setWidth(String width) {
		iFilter.setWidth(width);
	}
	
	@Override
	public void setError(String error) {
		iError.setStyleName("unitime-ErrorHint");
		if (error == null || error.isEmpty()) {
			iError.setHTML("");
			iError.setVisible(false);
		} else {
			iError.setHTML(error);
			iError.setVisible(true);
			iFilter.setStatus(error);
			// AriaStatus.getInstance().setText(error);
		}
	}
	
	public void setWarning(String error) {
		iError.setStyleName("unitime-WarningHint");
		if (error == null || error.isEmpty()) {
			iError.setHTML("");
			iError.setVisible(false);
		} else {
			iError.setHTML(error);
			iError.setVisible(true);
			iFilter.setStatus(error);
			// AriaStatus.getInstance().setText(error);
		}
	}
	
	@Override
	public String getError() {
		return (iError.isVisible() ? iError.getText() : null);
	}

	@Override
	public void setAccessKey(char a) {
		iFilter.setAccessKey(a);
	}
	
	@Override
	public int getTabIndex() {
		return iFilter.getTabIndex();
	}

	@Override
	public void setFocus(boolean focused) {
		iFilter.setFocus(focused);
		if (focused) iFilter.getValueBox().selectAll();
	}

	@Override
	public void setTabIndex(int index) {
		iFilter.setTabIndex(index);
	}
	
	private void openDialogAsync() {
        GWT.runAsync(new RunAsyncCallback() {
        	public void onSuccess() {
        		openDialog();
        	}
        	public void onFailure(Throwable reason) {
        		UniTimeNotifications.error(MESSAGES.failedToLoadTheApp(reason.getMessage()));
        	}
        });
	}
	
	protected void openDialog() {
		getCourseFinder().setValue(getValue(), true);
		getCourseFinder().setEnabled(isEnabled());
		getCourseFinder().findCourse();
	}


	@Override
	public void addValidator(Validator<CourseSelection> validator) {
		iValidators.add(validator);
	}
	
	@Override
	public String validate() {
		if (getValue().isEmpty()) {
			setError(null);
			return null;
		}
		if (iFreeTimeParser != null) {
			try {
				iFreeTimeParser.parseFreeTime(iFilter.getText());
				setError(null);
				return null;
			} catch (IllegalArgumentException e) {
				if (iFilter.getText().toLowerCase().startsWith(CONSTANTS.freePrefix().toLowerCase())) {
					setError(MESSAGES.invalidFreeTime());
					return e.getMessage();
				}
			}
		}
		for (Validator<CourseSelection> validator: iValidators) {
			String message = validator.validate(this);
			if (message != null) {
				setError(message);
				return message;
			}
		}
		setError(null);
		return null;
	}

	@Override
	public HandlerRegistration addCourseSelectionHandler(CourseSelectionHandler handler) {
		return addHandler(handler, CourseSelectionEvent.getType());
	}
	
	@Override
	public String getText() {
		return iFilter.getText();
	}
	
	@Override
	public void setText(String text) {
		iFilter.setValue(text);
	}
	
	public class CourseSuggestion  extends FilterBox.Suggestion {
		RequestedCourse iCourse = null;
		
		public CourseSuggestion(CourseAssignment course) {
			super(course.getCourseName(),
				!course.hasUniqueName() || iShowCourses ? course.getCourseNameWithTitle() : course.getCourseName(),
				course.hasTitle() ? course.getTitle() : MESSAGES.hintCourseWithNoTitle());
			iCourse = new RequestedCourse();
			iCourse.setCourseId(course.getCourseId());
			iCourse.setCourseName(!course.hasUniqueName() || iShowCourses ? course.getCourseNameWithTitle() : course.getCourseName());
			iCourse.setCourseTitle(course.getTitle());
			iCourse.setCredit(course.guessCreditRange());
			if (getText().equals(course.getCourseName()) || getText().startsWith(course.getCourseName() + " ") || 
				getText().equals(course.getCourseNameWithTitle()) || getText().startsWith(course.getCourseNameWithTitle() + " ")) {
				for (Chip chip: iFilter.getChips("section"))
					if (chip instanceof PreferenceChip)
						iCourse.setSelectedClass(((PreferenceChip)chip).getPreference(), true);
				for (Chip chip: iFilter.getChips("method"))
					if (chip instanceof PreferenceChip)
						iCourse.setSelectedIntructionalMethod(((PreferenceChip)chip).getPreference(), true);
			}
		}
		
		public CourseSuggestion(CourseAssignment course, IdValue im, boolean required) {
			this(course);
			setDisplayString(course.getCourseName() + " " + im.getValue() + (required ? "!" : ""));
			setHint("<span class='item-hint'>" + (required ? MESSAGES.hintRequiredInstructionalMethod() : MESSAGES.hintInstructionalMethod()) + "</span>");
			if (iCourse.hasSelectedIntructionalMethods()) iCourse.getSelectedIntructionalMethods().clear();
			iCourse.setSelectedIntructionalMethod(new Preference(im.getId(), im.getValue(), required), true);
		}
		
		public CourseSuggestion(CourseAssignment course, ClassAssignment clazz, boolean required) {
			this(course);
			setDisplayString(course.getCourseName() + " " + clazz.getSelection(required));
			String section = (clazz.getSelection(required).getText().startsWith(clazz.getSubpart()) ? "" : clazz.getSubpart() + " ") +
					clazz.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.emailArrangeHours()) +
					(clazz.hasNote() ? " " + clazz.getNote() : ""); 
			setHint("<span class='item-hint'>" + (required ? MESSAGES.hintRequiredSection(section) : section) + "</span>");
			iCourse.setSelectedClass(clazz.getSelection(required), !iCourse.isSelectedClass(clazz.getSelection(required)));
		}
		
		public RequestedCourse getRequestedCourse() { return iCourse; }
	}
	
	public static class CourseRequestFilterBox extends FilterBox {
		private Image iFilterFinder = null;
		private String iHint = "";
		
		public CourseRequestFilterBox(ClickHandler finderClickHandler) {
			super();
			iFilterFinder = new Image(RESOURCES.finder());
			iFilterFinder.setAltText(MESSAGES.altOpenFilter());
			iFilterFinder.setTitle(MESSAGES.altOpenFilter());
			iFilterFinder.addStyleName("button-image");
	        insert(iFilterFinder, getWidgetIndex(iFilterOpen));
	        Roles.getDocumentRole().setAriaHiddenState(iFilterFinder.getElement(), true);
	        iFilterFinder.addClickHandler(finderClickHandler);
	        
	        iFilterClear.setAltText(MESSAGES.altClearCourseRequest());
	        iFilterClear.setTitle(MESSAGES.altClearCourseRequest());
	        
	        remove(iFilterOpen); remove(iFilterClose);
	        
	        getValueBox().addKeyDownHandler(new KeyDownHandler() {
				public void onKeyDown(KeyDownEvent event) {
					if (!isEnabled()) return;
					if ((event.getNativeEvent().getKeyCode()=='F' || event.getNativeEvent().getKeyCode()=='f') && (event.isControlKeyDown() || event.isAltKeyDown())) {
						hideSuggestions();
						iFilterFinder.getElement().dispatchEvent(Document.get().createClickEvent(1, 0, 0, 0, 0, false, false, false, false));
					} else if (event.isControlKeyDown() || event.isAltKeyDown()) {
						for (int i = 0; i < getWidgetCount(); i++) {
							Widget w = getWidget(i);
							if (w instanceof FilterOperation && w.isVisible() && ((FilterOperation)w).getAccessKey() != null && event.getNativeEvent().getKeyCode() == ((FilterOperation)w).getAccessKey()) {
								w.getElement().dispatchEvent(Document.get().createClickEvent(1, 0, 0, 0, 0, false, false, false, false));
							}
						}
					}
				}
			});
			getValueBox().addBlurHandler(new BlurHandler() {
				public void onBlur(BlurEvent event) {
					if (getText().isEmpty()) {
						if (!iHint.isEmpty()) {
							iFilter.setText(iHint);
							getValueBox().addStyleName("hint");
						}
					}
				}
			});
			getValueBox().addFocusHandler(new FocusHandler() {
				public void onFocus(FocusEvent event) {
					if (!iHint.isEmpty() && iFilter.getText().equals(iHint)) {
						getValueBox().removeStyleName("hint");
						iFilter.setText("");
					}
				}
			});
			addSelectionHandler(new SelectionHandler<FilterBox.Suggestion>() {
				@Override
				public void onSelection(SelectionEvent<Suggestion> event) {
					getValueBox().removeStyleName("hint");
				}
			});
		}
		
		protected void setFilterFinderFace(ImageResource face) {
			if (face == null)
				iFilterFinder.setResource(RESOURCES.finder());
			else
				iFilterFinder.setResource(face);
		}
		
		protected Image getFilterFinderButton() { return iFilterFinder; }
		
		@Override
		protected void resizeFilterIfNeeded() {
			if (!isAttached()) return;
			ChipPanel last = getLastChipPanel();
			if (iFilterClear.isAttached())
				iFilterClear.setVisible(isEnabled() && (!getText().isEmpty() || last != null));
			iFilterFinder.setVisible(isEnabled() || !getText().isEmpty());
			int buttonWidth = 0;
			for (int i = 0; i < getWidgetCount(); i++) {
				Widget w = getWidget(i);
				if (w instanceof FilterOperation)
					((FilterOperation)w).onBeforeResize(this);
				if (w instanceof Image && w.isAttached() && w.isVisible())
					buttonWidth += w.getElement().getOffsetWidth() + 2;
			}
			int w = 0;
			for (int i = 0; i < getWidgetCount(); i++) {
				if (getWidget(i) instanceof TextBox) continue;
				w += 2 + getWidget(i).getOffsetWidth();
			}
			int width = getElement().getClientWidth() - w;
			if (width < 100) {
				width = getElement().getClientWidth() - buttonWidth;
			}
			iFilter.getElement().getStyle().setWidth(width, Unit.PX);
			if (isSuggestionsShowing())
				iSuggestionsPopup.moveRelativeTo(this);
			if (isFilterPopupShowing())
				iFilterPopup.moveRelativeTo(this);
		}
		
		@Override
		public void addChip(Chip chip, boolean fireEvents) {
			final ChipPanel panel = new ChipPanel(chip, getChipColor(chip));
			panel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					remove(panel);
					resizeFilterIfNeeded();
					setAriaLabel(toAriaString());
					ValueChangeEvent.fire(CourseRequestFilterBox.this, getValue());
				}
			});
			insert(panel, getWidgetIndex(iFilterFinder));
			resizeFilterIfNeeded();
			setAriaLabel(toAriaString());
			if (fireEvents)
				ValueChangeEvent.fire(this, getValue());
		}
		
		public String getText() {
			return iFilter.getText().equals(iHint) ? "" : iFilter.getText();
		}
		
		public void setText(String text) {
			iFilter.setText(text);
			if (!iFocus && text.isEmpty() && !iHint.isEmpty()) {
				iFilter.setText(iHint);
				getValueBox().addStyleName("hint");
			} else {
				getValueBox().removeStyleName("hint");
			}
		}
		
		public void setHint(String hint) {
			if (iFilter.getText().equals(iHint)) {
				if (!iFocus) {
					iFilter.setText(hint);
					if (!hint.isEmpty())
						getValueBox().addStyleName("hint");
				} else {
					iFilter.setText("");
					getValueBox().removeStyleName("hint");
				}
			}
			iHint = hint;
		}

		public String getHint() {
			return iHint;
		}
	}
	
	public void addOperation(FilterOperation operation, boolean beforeFinder) {
		operation.addStyleName("button-image");
		if (beforeFinder)
			iFilter.add(operation);
		else
			iFilter.insert(operation, iFilter.getWidgetIndex(iFilter.iFilterFinder));
        Roles.getDocumentRole().setAriaHiddenState(operation.getElement(), true);
	}
	
	public void addStatus(FilterOperation operation) {
		operation.addStyleName("status-image");
		iFilter.insert(operation, 0);
	}
	
	public void removeOperation(FilterOperation operation) {
		iFilter.remove(operation);
	}
	
	public void removeClearOperation() {
		iFilter.remove(iFilter.iFilterClear);
	}

	public void resizeFilterIfNeeded() {
		iFilter.resizeFilterIfNeeded();
	}
	
	public static class FilterOperation extends Image {
		private Character iAccessKey;
		
		public FilterOperation(ImageResource resource, Character accessKey) {
			super(resource);
			iAccessKey = accessKey;
	        Roles.getDocumentRole().setAriaHiddenState(getElement(), true);
		}
		
		public FilterOperation(ImageResource resource) {
			this(resource, null);
		}
		
		public void onBeforeResize(CourseRequestFilterBox filter) {
		}
		
		public Character getAccessKey() { return iAccessKey; }
	}
	
	public class FilterStatus extends FilterOperation implements HasAriaLabel {

		public FilterStatus(ImageResource resource, Character accessKey) {
			super(resource, accessKey);
			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					click();
				}
			});
		}
		
		public void click() {
			if (isVisible()) {
				if (iSpecReg == null || !iSpecReg.isAllowChangeRequestNote() || !iSpecReg.getChangeRequestorNoteInterface().changeRequestorNote(getValue()))
					UniTimeConfirmationDialog.info(getAltText());
			}
		}
		
		public FilterStatus(ImageResource resource) {
			this(resource, null);
		}
		
		public void setStatus(ImageResource icon, String message) {
			setVisible(true);
			setResource(icon);
			setTitle(message);
			setAriaLabel(message);
		}
		
		public void clearStatus() {
			setVisible(false);
		}

		@Override
		public String getAriaLabel() {
			return getAltText();
		}

		@Override
		public void setAriaLabel(String text) {
			setAltText(text);
		}
	}
	
	public static class PreferenceChip extends Chip {
		Preference iPreference;
		
		public PreferenceChip(String command, Preference preference) {
			super(command, preference.toString());
			iPreference = preference;
		}
		
		public Preference getPreference() { return iPreference; }
	}
}
