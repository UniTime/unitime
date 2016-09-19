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
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaSuggestBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author Tomas Muller
 */
public class CourseSelectionSuggestBox extends P implements CourseSelection {
	protected static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	protected AriaSuggestBox iSuggest;
	private String iLastSuggestion;
	protected ImageButton iFinderButton;
	private Label iError;
	
	private String iHint = "";
	private Set<String> iValidCourseNames = new HashSet<String>();
	
	private DataProvider<String, Collection<CourseAssignment>> iDataProvider;
	private FreeTimeParser iFreeTimeParser = null;
	private CourseFinder iCourseFinder = null;
	private CourseFinderFactory iCourseFinderFactory = null;
	private List<Validator<CourseSelection>> iValidators = new ArrayList<Validator<CourseSelection>>();
	
	private boolean iShowCourses = false, iShowDefaultSuggestions = false;
	private boolean iSaved = false;
	
	public CourseSelectionSuggestBox() {
		this(false, false);
	}
	
	public CourseSelectionSuggestBox(boolean showCourses, boolean showDefaultSuggestions) {
		super("unitime-CourseSelectionBox");
		iShowCourses = showCourses;
		iShowDefaultSuggestions = showDefaultSuggestions;
		
		SuggestOracle courseOfferingOracle = new SuggestOracle() {
			public void requestSuggestions(Request request, Callback callback) {
				if (request.getQuery().equals(iHint)) return;
				iDataProvider.getData(request.getQuery(), new SuggestCallback(request, callback));
			}
			public void requestDefaultSuggestions(Request request, Callback callback) {
				if (iShowDefaultSuggestions)
					iDataProvider.getData("", new SuggestCallback(request, callback));
				else
					super.requestDefaultSuggestions(request, callback);
				
			}
			public boolean isDisplayStringHTML() { return true; }
		};
		
		iSuggest = new AriaSuggestBox(courseOfferingOracle);
		iSuggest.setStyleName("unitime-TextBoxHint");
		iSuggest.addStyleName("text");
		add(iSuggest);

		iFinderButton = new ImageButton(RESOURCES.search_picker(), RESOURCES.search_picker_Down(), RESOURCES.search_picker_Over(), RESOURCES.search_picker_Disabled());
		iFinderButton.setTabIndex(-1);
		iFinderButton.addStyleName("button");
		add(iFinderButton);
		
		iError = new Label();
		iError.setStyleName("unitime-ErrorHint");
		iError.setVisible(false);
		Roles.getPresentationRole().setAriaHiddenState(iError.getElement(), true);
		iError.addStyleName("error");
		add(iError);
				
		iFinderButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (iSuggest.isEnabled()) {
					openDialogAsync();
				}
			}
		});
		
		iSuggest.addSelectionHandler(new SelectionHandler<Suggestion>() {
			public void onSelection(SelectionEvent<Suggestion> event) {
				String text = event.getSelectedItem().getReplacementString();
				iLastSuggestion = text;
				CourseSelectionEvent.fire(CourseSelectionSuggestBox.this, text, !text.isEmpty());
			}
		});
		iSuggest.getValueBox().addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				boolean valid = false;
				String text = iSuggest.getText();
				if (text.equalsIgnoreCase(iLastSuggestion))
					valid = true;
				else for (String course: iValidCourseNames) {
					if (course.equalsIgnoreCase(text)) {
						valid = true; break;
					}
				}
				CourseSelectionEvent.fire(CourseSelectionSuggestBox.this, text, valid);
			}
		});
		iSuggest.getValueBox().addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				if (!iSuggest.isEnabled()) return;
				if ((event.getNativeEvent().getKeyCode()=='F' || event.getNativeEvent().getKeyCode()=='f') && (event.isControlKeyDown() || event.isAltKeyDown())) {
					iSuggest.hideSuggestionList();
					openDialogAsync();
				}
				if (event.getNativeEvent().getKeyCode()==KeyCodes.KEY_ESCAPE) {
					iSuggest.hideSuggestionList();
				}
				if ((event.getNativeEvent().getKeyCode()=='L' || event.getNativeEvent().getKeyCode()=='l') && (event.isControlKeyDown() || event.isAltKeyDown())) {
					iSuggest.showSuggestionList();
				}
			}
		});
		iSuggest.getValueBox().addBlurHandler(new BlurHandler() {
			public void onBlur(BlurEvent event) {
				if (iSuggest.getText().isEmpty()) {
					if (iError.isVisible()) iError.setVisible(false);
					if (!iHint.isEmpty()) {
						iSuggest.setText(iHint);
						iSuggest.setStyleName("unitime-TextBoxHint");
					}
				}
			}
		});
		iSuggest.getValueBox().addFocusHandler(new FocusHandler() {
			public void onFocus(FocusEvent event) {
				iSuggest.setStyleName("gwt-SuggestBox");
				if (!iHint.isEmpty() && iSuggest.getText().equals(iHint)) iSuggest.setText("");
				if (!iError.getText().isEmpty())
					AriaStatus.getInstance().setText(iError.getText());
			}
		});
	}

	@Override
	public String getValue() {
		return (iSuggest.getText().equals(iHint) ? "" : iSuggest.getText());
	}

	@Override
	public void setValue(String value) {
		setValue(value, false);
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
	
	private void openDialog() {
		getCourseFinder().setValue(iSuggest.getText().equals(iHint)?"":iSuggest.getText(), true);
		getCourseFinder().findCourse();
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		if (value == null || value.isEmpty()) {
			iSuggest.setText(iHint);
			if (!iHint.isEmpty())
				iSuggest.setStyleName("unitime-TextBoxHint");
			else
				iSuggest.setStyleName("gwt-SuggestBox");
			setError(null);
		} else {
			iSuggest.setText(value);
			if (iSuggest.getText().isEmpty()) {
				if (!iHint.isEmpty()) {
					iSuggest.setText(iHint);
					iSuggest.setStyleName("unitime-TextBoxHint");
				} else {
					iSuggest.setStyleName("gwt-SuggestBox");
				}
			} else {
				iSuggest.setStyleName("gwt-SuggestBox");
			}
		}
		if (fireEvents)
			CourseSelectionEvent.fire(CourseSelectionSuggestBox.this, value, value != null && !value.isEmpty());
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void setHint(String hint) {
		if (iSuggest.getText().equals(iHint)) {
			iSuggest.setText(hint);
			if (!hint.isEmpty())
				iSuggest.setStyleName("unitime-TextBoxHint");
		}
		iHint = hint;
	}

	@Override
	public String getHint() {
		return iHint;
	}

	@Override
	public boolean isEnabled() {
		return iSuggest.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			iSuggest.setEnabled(true);
			iFinderButton.setEnabled(true);
			iFinderButton.setTabIndex(0);
		} else {
			iSuggest.setEnabled(false);
			iFinderButton.setEnabled(false);
			iFinderButton.setTabIndex(-1);
			if (iSaved) iFinderButton.setFace(RESOURCES.search_picker_Assigned());
		}
	}
	
	public void setSaved(boolean saved) {
		iSaved = saved;
		if (!isEnabled())
			iFinderButton.setFace(saved ? RESOURCES.search_picker_Assigned() : RESOURCES.search_picker_Disabled());
		iFinderButton.setTitle(saved ? MESSAGES.saved(getValue()) : iFinderButton.getAltText());
	}
	
	public boolean isSaved() {
		return iSaved;
	}

	@Override
	public void setSuggestions(DataProvider<String, Collection<CourseAssignment>> provider) {
		iDataProvider = provider;
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
	
	public class SuggestCallback implements AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> {
		private Request iRequest;
		private Callback iCallback;
		
		public SuggestCallback(Request request, Callback callback) {
			iRequest = request;
			iCallback = callback;
		}
		
		public void onFailure(final Throwable caught) {
			iValidCourseNames.clear();
			final ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			if (iFreeTimeParser != null) {
				iFreeTimeParser.getData(iRequest.getQuery(), new AsyncCallback<List<FreeTime>>() {
					@Override
					public void onFailure(Throwable e) {
						if (iRequest.getQuery().toLowerCase().startsWith(CONSTANTS.freePrefix().toLowerCase())) {
							suggestions.add(new SimpleSuggestion("<font color='red'>"+e.getMessage()+"</font>", "", e.getMessage()));
							// setStatus(e.getMessage());
						} else {
							suggestions.add(new SimpleSuggestion("<font color='red'>"+caught.getMessage()+"</font>", "", caught.getMessage()));
							// setStatus(caught.getMessage());
						}
					}

					@Override
					public void onSuccess(List<FreeTime> freeTimes) {
						String status = "";
						for (CourseRequestInterface.FreeTime ft: freeTimes) {
							status += ft.toAriaString(CONSTANTS.longDays(), CONSTANTS.useAmPm()) + " ";
						}
						String ft = iFreeTimeParser.freeTimesToString(freeTimes);
						Suggestion suggestion = new SimpleSuggestion(ft, ft, ARIA.courseFinderSelectedFreeTime(status)); 
						suggestions.add(suggestion);
						// setStatus(status + " matches the entered text. Press enter to select it.");
					}
				});
			} else {
				suggestions.add(new SimpleSuggestion("<font color='red'>"+caught.getMessage()+"</font>", "", caught.getMessage()));
				// setStatus(caught.getMessage());
			}
			iCallback.onSuggestionsReady(iRequest, new Response(suggestions));
		}

		public void onSuccess(Collection<ClassAssignmentInterface.CourseAssignment> result) {
			ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			iValidCourseNames.clear();
			for (ClassAssignmentInterface.CourseAssignment suggestion: result) {
				String courseName = MESSAGES.courseName(suggestion.getSubject(), suggestion.getCourseNbr());
				String courseNameWithTitle = (suggestion.getTitle() == null ? courseName : MESSAGES.courseNameWithTitle(suggestion.getSubject(), suggestion.getCourseNbr(), suggestion.getTitle()));
				if (suggestion.hasUniqueName() && !iShowCourses) {
					suggestions.add(new SimpleSuggestion(courseNameWithTitle, courseName, suggestion.getTitle() == null ? courseName : courseName + " " + suggestion.getTitle()));
					iValidCourseNames.add(courseName);
				} else {
					suggestions.add(new SimpleSuggestion(courseNameWithTitle, courseNameWithTitle, suggestion.getTitle() == null ? courseName : courseName + " " + suggestion.getTitle()));
					iValidCourseNames.add(courseNameWithTitle);
				}
			}
			iCallback.onSuggestionsReady(iRequest, new Response(suggestions));
		}
	}
	
	public static class SimpleSuggestion implements Suggestion, AriaSuggestBox.HasStatus {
		private String iDisplay, iReplace, iStatus;

		public SimpleSuggestion(String display, String replace, String status) {
			iDisplay = display;
			iReplace = replace;
			iStatus = status;
		}
		
		public SimpleSuggestion(String display, String replace) {
			this(display, replace, display);
		}
		
		public SimpleSuggestion(String replace) {
			this(replace, replace, replace);
		}

		@Override
		public String getDisplayString() {
			return iDisplay;
		}

		@Override
		public String getReplacementString() {
			return iReplace;
		}

		@Override
		public String getStatusString() {
			return iStatus;
		}
	}
	
	@Override
	public void setCourseFinderFactory(CourseFinderFactory factory) {
		iCourseFinderFactory = factory;
	}
	
	@Override
	public CourseFinder getCourseFinder() {
		if (iCourseFinder == null) {
			iCourseFinder = iCourseFinderFactory.createCourseFinder();
			iCourseFinder.addSelectionHandler(new SelectionHandler<String>() {
				@Override
				public void onSelection(SelectionEvent<String> event) {
					setValue(event.getSelectedItem(), true);
				}
			});
			if (iCourseFinder instanceof HasCloseHandlers) {
				((HasCloseHandlers)iCourseFinder).addCloseHandler(new CloseHandler<PopupPanel>() {
					public void onClose(CloseEvent<PopupPanel> event) {
						Scheduler.get().scheduleDeferred(new ScheduledCommand() {
							public void execute() {
								iSuggest.setFocus(true);
							}
						});
					}
				});
			}
		}
		return iCourseFinder;
	}

	@Override
	public void setLabel(String title, String finderTitle) {
		iSuggest.setAriaLabel(title);
		iFinderButton.setAltText(finderTitle);
	}
	
	@Override
	public void hideSuggestionList() {
		iSuggest.hideSuggestionList();
	}
	
	@Override
	public void showSuggestionList() {
		iSuggest.showSuggestionList();
	}

	@Override
	public void setWidth(String width) {
		iSuggest.setWidth(width);
	}
	
	@Override
	public void setError(String error) {
		if (error == null || error.isEmpty()) {
			iError.setText("");
			iError.setVisible(false);
		} else {
			iError.setText(error);
			iError.setVisible(true);
			iSuggest.setStatus(error);
			// AriaStatus.getInstance().setText(error);
		}
	}
	
	@Override
	public String getError() {
		return (iError.isVisible() ? iError.getText() : null);
	}

	@Override
	public void setAccessKey(char a) {
		iSuggest.setAccessKey(a);
	}
	
	@Override
	public int getTabIndex() {
		return iSuggest.getTabIndex();
	}

	@Override
	public void setFocus(boolean focused) {
		iSuggest.setFocus(focused);
		if (focused) iSuggest.getValueBox().selectAll();
	}

	@Override
	public void setTabIndex(int index) {
		iSuggest.setTabIndex(index);
	}

	@Override
	public HandlerRegistration addCourseSelectionHandler(CourseSelectionHandler handler) {
		return addHandler(handler, CourseSelectionEvent.getType());
	}

	@Override
	public boolean isFreeTime() {
		if (iFreeTimeParser == null || getValue().isEmpty()) return false;
		try {
			iFreeTimeParser.parseFreeTime(getValue());
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public void addValidator(Validator<CourseSelection> validator) {
		iValidators.add(validator);
	}
	
	public String validate() {
		if (getValue().isEmpty()) {
			setError(null);
			return null;
		}
		if (iFreeTimeParser != null) {
			try {
				iFreeTimeParser.parseFreeTime(getValue());
				setError(null);
				return null;
			} catch (IllegalArgumentException e) {
				if (getValue().toLowerCase().startsWith(CONSTANTS.freePrefix().toLowerCase())) {
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
}
