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

import org.unitime.timetable.gwt.client.aria.AriaSuggestBox;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradeMode;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseInfo;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author Tomas Muller
 */
public class RequestVariableTitleCourseDialog extends UniTimeDialogBox {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static GwtMessages GWT_MSG = GWT.create(GwtMessages.class);
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private ScheduleStatus iStatus;
	private StudentSectioningContext iContext;
	private VariableTitleCourseInfo iSelectedCourse;

	private SimpleForm iForm;
	private HTML iCourseDetails;
	private int iCourseDetailsLine;
	private TextArea iNote = null;
	private AriaTextBox iCourseName = null;
	private UniTimeWidget<AriaTextBox> iCourseTitle = null;
	private ListBox iCredit;
	private AriaSuggestBox iCourseSuggestBox = null;
	private UniTimeWidget<ListBox> iInstructor;
	private ListBox iGradeMode;
	private SingleDateSelector iDateFrom, iDateTo;
	private CheckBox iDisclaimer;
	private int iDisclaimerLine;
	private Timer iTimer;
	private Float iCurrentCredit, iMaxCredit;
	private Label iCreditMessage;
	private int iCreditLine;
	
	private UniTimeHeaderPanel iButtons;
	
	public RequestVariableTitleCourseDialog(StudentSectioningContext context, ScheduleStatus status) {
		super(true, true);
		addStyleName("unitime-RequestVariableTitleCourseDialog");
		setText(MESSAGES.dialogRequestVariableTitleCourse());
		iContext = context;
		iStatus = status;
		
		iForm = new SimpleForm();
		iCourseName = new AriaTextBox();
		iCourseName.getElement().setAttribute("autocomplete", "off");
		iCourseSuggestBox = new AriaSuggestBox(iCourseName, new VariableTitleCourseSuggestions());
		iTimer = new Timer() {
			@Override
			public void run() {
				onCourseChanged(iCourseName.getText());
			}
		};
		iCourseSuggestBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iTimer.schedule(500);
			}
		});
		iCourseSuggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				iTimer.cancel();
				if (event.getSelectedItem() instanceof VariableTitleCourseInfo) {
					onCourseChanged(((VariableTitleCourseInfo)event.getSelectedItem()).getCourseName());
				} else {
					setCourse(null);
				}
			}
		});
		iCourseSuggestBox.addStyleName("course");
		iForm.addRow(MESSAGES.propReqVTCourseCourse(), iCourseSuggestBox);
		
		iCourseDetails = new HTML();
		iCourseDetails.addStyleName("details");
		iCourseDetailsLine = iForm.addRow(MESSAGES.propReqVTCourseDetails(), iCourseDetails);
		iForm.getRowFormatter().setVisible(iCourseDetailsLine, false);
		
		iCourseTitle = new UniTimeWidget<AriaTextBox>(new AriaTextBox());
		iCourseTitle.getWidget().setStyleName("unitime-TextBox");
		iCourseTitle.getWidget().addStyleName("title");
		iCourseTitle.getWidget().setMaxLength(30);
		iCourseTitle.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCourseTitle.clearHint();
				iButtons.clearMessage();
			}
		});
		iForm.addRow(MESSAGES.propReqVTCourseTitle(), iCourseTitle);
		
		iCredit = new ListBox();
		iCredit.addStyleName("credit");
		iCredit.setEnabled(false);
		iForm.addRow(MESSAGES.propReqVTCourseCredit(), iCredit);
		iCredit.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				checkCredit();
			}
		});
		
		iInstructor = new UniTimeWidget<ListBox>(new ListBox());
		iInstructor.getWidget().addStyleName("instructor");
		iInstructor.getWidget().setEnabled(false);
		iForm.addRow(MESSAGES.propReqVTCourseInstructor(), iInstructor);
		iInstructor.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if ("-".equals(iInstructor.getWidget().getSelectedValue()))
						iInstructor.setHint(MESSAGES.hintReqVTCourseNoInstructorMatch());
				else
					iInstructor.clearHint();
			}
		});
		
		iGradeMode = new ListBox();
		iGradeMode.addStyleName("grade-mode");
		iGradeMode.setEnabled(false);
		iForm.addRow(MESSAGES.propReqVTCourseGradeMode(), iGradeMode);
		
		AbsolutePanel m = new AbsolutePanel();
		m.setStyleName("dates");
		P from = new P("from"); from.setText(MESSAGES.propReqVTCourseDatesFrom()); m.add(from);
		iDateFrom = new SingleDateSelector();
		m.add(iDateFrom);
		P to = new P("to"); to.setText(MESSAGES.propReqVTCourseDatesTo()); m.add(to);
		iDateTo = new SingleDateSelector();
		m.add(iDateTo);
		iForm.addRow(MESSAGES.propReqVTCourseDates(), m);
		iDateFrom.setEnabled(false);
		iDateTo.setEnabled(false);
		
		iNote = new TextArea();
		iNote.setStyleName("unitime-TextArea"); iNote.addStyleName("request-note");
		iNote.setVisibleLines(5);
		iNote.setCharacterWidth(80);
		iForm.addRow(MESSAGES.propReqVTCourseNote(), iNote);
		
		iDisclaimer = new CheckBox();
		iDisclaimer.addStyleName("disclaimer");
		iDisclaimer.setValue(false);
		iDisclaimer.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iButtons.setEnabled("submit", iDisclaimer.isEnabled() && iDisclaimer.getValue());
			}
		});
		iDisclaimerLine = iForm.addRow(MESSAGES.propReqVTCourseDisclaimer(), iDisclaimer);
		iForm.getRowFormatter().setVisible(iDisclaimerLine, false);
		
		iCreditMessage = new Label();
		iCreditMessage.addStyleName("credit");
		iCreditLine = iForm.addRow(MESSAGES.propReqVTMaxCredit(), iCreditMessage);
		iForm.getRowFormatter().setVisible(iCreditLine, false);
		
		iButtons = new UniTimeHeaderPanel();
		iButtons.addButton("submit", MESSAGES.buttonSubmitVariableTitleCourse(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (validate()) {
					hide();
					onSubmit();
				}
			}
		});
		iButtons.addButton("cancel", MESSAGES.buttonCloseVariableTitleCourse(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		iButtons.setEnabled("submit", false);
		iForm.addBottomRow(iButtons);
		
		setWidget(iForm);
	}
	
	public void requestVariableTitleCourse(Float currentCredit, Float maxCredit) {
		iCourseName.setText("");
		iCourseTitle.getWidget().setText("");
		iDisclaimer.setValue(false);
		iNote.setText("");
		iCredit.clear();
		iInstructor.getWidget().clear(); iInstructor.clearHint();
		iGradeMode.clear();
		iDateFrom.setValueInServerTimeZone(null); iDateTo.setValueInServerTimeZone(null);
		setCourse(null);
		iCurrentCredit = currentCredit; iMaxCredit = maxCredit;
		center();
		iCourseName.setFocus(true);
	}
	
	protected void onCourseChanged(String course) {
		if (course == null || course.isEmpty()) {
			setCourse(null);
		} else {
			if (iSelectedCourse != null && (iSelectedCourse.getCourseName().equalsIgnoreCase(course) || iSelectedCourse.getReplacementString().equalsIgnoreCase(course)))
				return;
			iSectioningService.getVariableTitleCourse(iContext, course, new AsyncCallback<VariableTitleCourseInfo>() {
				@Override
				public void onFailure(Throwable caught) {
					iStatus.error(MESSAGES.exceptionGetVariableTitleCourseInfo(caught.getMessage()));
				}

				@Override
				public void onSuccess(VariableTitleCourseInfo result) {
					if (result != null && result.equals(iSelectedCourse)) return;
					setCourse(result);					
				}
			});
		}
	}
	
	protected void setCourse(VariableTitleCourseInfo course) {
		iSelectedCourse = course;
		String lastCredit = iCredit.getSelectedValue();
		String lastInstructor = iInstructor.getWidget().getSelectedValue();
		String lastGradeMode = iGradeMode.getSelectedValue();
		iCredit.clear();
		iInstructor.getWidget().clear(); iInstructor.clearHint();
		iGradeMode.clear();
		iCourseDetails.setHTML("");
		iForm.getRowFormatter().setVisible(iCourseDetailsLine, false);
		iForm.getRowFormatter().setVisible(iDisclaimerLine, false);
		iForm.getRowFormatter().setVisible(iCreditLine, false);
		if (iSelectedCourse == null) {
			iCredit.setEnabled(false);
			iInstructor.getWidget().setEnabled(false);
			iGradeMode.setEnabled(false);
			iDisclaimer.setEnabled(false);
			iDateFrom.setEnabled(false);
			iDateTo.setEnabled(false);
		} else {
			if (iSelectedCourse.hasDetails()) {
				iCourseDetails.setHTML(iSelectedCourse.getDetails());
				iForm.getRowFormatter().setVisible(iCourseDetailsLine, true);
			}
			if (iSelectedCourse.hasAvailableCredits()) {
				for (float cred: iSelectedCourse.getAvailableCredits()) {
					iCredit.addItem(MESSAGES.credit(cred), String.valueOf(cred));
					if (lastCredit != null && lastCredit.equals(String.valueOf(cred))) {
						iCredit.setSelectedIndex(iCredit.getItemCount() - 1);
					}
				}
			}
			iCredit.setEnabled(iCredit.getItemCount() > 0);
			if (iDateFrom.getValueInServerTimeZone() == null)
				iDateFrom.setValueInServerTimeZone(iSelectedCourse.getStartDate());
			// iDateFrom.setEnabled(true);
			if (iDateTo.getValueInServerTimeZone() == null)
				iDateTo.setValueInServerTimeZone(iSelectedCourse.getEndDate());
			// iDateTo.setEnabled(true);
			if (iSelectedCourse.hasInstructors()) {
				iInstructor.getWidget().addItem(GWT_MSG.itemSelect(), "");
				for (InstructorInfo i: iSelectedCourse.getInstructors()) {
					iInstructor.getWidget().addItem(i.getName(), i.getId().toString());
					if (lastInstructor != null && lastInstructor.equals(i.getId().toString()))
						iInstructor.getWidget().setSelectedIndex(iInstructor.getWidget().getItemCount() - 1);
				}
				iInstructor.getWidget().addItem(MESSAGES.itemReqVTNoInstructor(), "-");
				if (lastInstructor != null && lastInstructor.equals("-")) {
					iInstructor.getWidget().setSelectedIndex(iInstructor.getWidget().getItemCount() - 1);
					iInstructor.setHint(MESSAGES.hintReqVTCourseNoInstructorMatch());
				}
			}
			iInstructor.getWidget().setEnabled(iInstructor.getWidget().getItemCount() > 0);
			if (iSelectedCourse.hasGradeModes()) {
				GradeMode selection = iSelectedCourse.getDefaultGradeMode();
				if (lastGradeMode != null)
					for (GradeMode gm: iSelectedCourse.getGradeModes())
						if (gm.getCode().equals(lastGradeMode)) selection = gm;
				for (GradeMode gm: iSelectedCourse.getGradeModes()) {
					iGradeMode.addItem(gm.getLabel(), gm.getCode());
					if (gm.equals(selection))
						iGradeMode.setSelectedIndex(iGradeMode.getItemCount() - 1);
				}
			}
			iGradeMode.setEnabled(iGradeMode.getItemCount() > 0);
			if (iSelectedCourse.hasDisclaimer()) {
				iDisclaimer.setText(iSelectedCourse.getDisclaimer());
				iDisclaimer.setEnabled(true);
				iForm.getRowFormatter().setVisible(iDisclaimerLine, true);
			}
			checkCredit();
		}
		iButtons.setEnabled("submit", iSelectedCourse != null && (!iSelectedCourse.hasDisclaimer() || iDisclaimer.getValue()));
	}
	
	protected void checkCredit() {
		if (iMaxCredit == null || iCurrentCredit == null || iCredit.getItemCount() == 0) {
			iForm.getRowFormatter().setVisible(iCreditLine, false);
		} else {
			float credit = iCurrentCredit + Float.valueOf(iCredit.getSelectedValue());
			if (credit > iMaxCredit) {
				iCreditMessage.setText(MESSAGES.varCreditMaxExceeded(credit, iMaxCredit));
				iForm.getRowFormatter().setVisible(iCreditLine, true);
			} else {
				iForm.getRowFormatter().setVisible(iCreditLine, false);
			}
		}
	}
	
	protected boolean validate() {
		boolean valid = true;
		if (iCourseTitle.getWidget().getText().isEmpty()) {
			iCourseTitle.setErrorHint(MESSAGES.errorReqVTCourseNoTitle());
			valid = false;
		}
		if (iInstructor.getWidget().isEnabled() && iInstructor.getWidget().getSelectedIndex() == 0) {
			iInstructor.setErrorHint(MESSAGES.errorReqVTCourseNoTitle());
			valid = false;
		}
		return valid;
	}
	
	protected void onSubmit() {
		final VariableTitleCourseRequest request = new VariableTitleCourseRequest(iContext);
		request.setCourse(iSelectedCourse);
		request.setCredit(Float.valueOf(iCredit.getSelectedValue()));
		request.setTitle(iCourseTitle.getWidget().getText());
		request.setNote(iNote.getText());
		request.setStartDate(iDateFrom.getValueInServerTimeZone());
		request.setEndDate(iDateTo.getValueInServerTimeZone());
		if (iCurrentCredit != null && iMaxCredit != null) {
			float credit = iCurrentCredit + Float.valueOf(iCredit.getSelectedValue());
			if (credit > iMaxCredit) {
				request.setMaxCredit(credit);
			}
		}
		if (iInstructor.getWidget().getSelectedIndex() > 0 && !"-".equals(iInstructor.getWidget().getSelectedValue()))
			request.setInstructor(new InstructorInfo(Long.valueOf(iInstructor.getWidget().getSelectedValue()), iInstructor.getWidget().getItemText(iInstructor.getWidget().getSelectedIndex())));
		if (iGradeMode.getItemCount() > 0)
			request.setGradeModeCode(iGradeMode.getSelectedValue());
		LoadingWidget.getInstance().show(MESSAGES.waitRequestVariableTitleCourse());
		iSectioningService.requestVariableTitleCourse(request, new AsyncCallback<VariableTitleCourseResponse>() {
			@Override
			public void onSuccess(final VariableTitleCourseResponse response) {
				LoadingWidget.getInstance().hide();
				if (response.hasRequests())
					iStatus.info(MESSAGES.statusVariableCourseRequested());
				else if (response.getCourse() != null) {
					UniTimeConfirmationDialog.confirm(MESSAGES.questionVariableCourseAlreadyExists(), new Command() {
						@Override
						public void execute() {
							request.setCheckIfExists(false);
							LoadingWidget.getInstance().show(MESSAGES.waitRequestVariableTitleCourse());
							iSectioningService.requestVariableTitleCourse(request, new AsyncCallback<VariableTitleCourseResponse>() {
								@Override
								public void onSuccess(VariableTitleCourseResponse response) {
									if (response.hasRequests())
										iStatus.info(MESSAGES.statusVariableCourseRequested());
									LoadingWidget.getInstance().hide();
									onChange(response);
								}
								
								@Override
								public void onFailure(Throwable caught) {
									iStatus.error(MESSAGES.exceptionRequestVariableTitleCourse(caught.getMessage()), caught);
									LoadingWidget.getInstance().hide();
								}
							});
						}
					}, new Command() {
						@Override
						public void execute() {
							onChange(response);
						}
					});
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iStatus.error(MESSAGES.exceptionRequestVariableTitleCourse(caught.getMessage()), caught);
				LoadingWidget.getInstance().hide();
			}
		});
	}
	
	protected void onChange(VariableTitleCourseResponse response) {}
	
	public static class SuggestionInterface implements IsSerializable, Suggestion {
		private String iDisplayString;
		private String iReplacementString;
		
		public SuggestionInterface() {}
		public SuggestionInterface(String display) {
			iDisplayString = display; iReplacementString = display;
		}
		public SuggestionInterface(String display, String replace) {
			iDisplayString = display; iReplacementString = replace;
		}
		
		@Override
		public String getDisplayString() { return iDisplayString; }

		@Override
		public String getReplacementString() { return iReplacementString; }
	}
	
	protected class VariableTitleCourseSuggestions extends SuggestOracle {

		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			iTimer.cancel();
			iSectioningService.listVariableTitleCourses(iContext, request.getQuery(), request.getLimit(), new AsyncCallback<Collection<VariableTitleCourseInfo>>() {
				
				@Override
				public void onSuccess(Collection<VariableTitleCourseInfo> result) {
					callback.onSuggestionsReady(request, new Response(result));
				}
				
				@Override
				public void onFailure(Throwable caught) {
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					suggestions.add(new SuggestionInterface("<font color='red'>"+caught.getMessage()+"</font>", ""));
					callback.onSuggestionsReady(request, new Response(suggestions));
				}
			});
		}
		
		@Override
		public boolean isDisplayStringHTML() { return true; }
	}

}
