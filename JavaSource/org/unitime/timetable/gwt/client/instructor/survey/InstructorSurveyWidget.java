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
package org.unitime.timetable.gwt.client.instructor.survey;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.instructor.InstructorCookie;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Course;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyData;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Preferences;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyPage.InstructorTimePreferences;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyPage.PreferencesReadOnlyTable;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyPage.ReadOnlyNote;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class InstructorSurveyWidget extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static DateTimeFormat sTimeStampFormat = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	private Long iInstructorId;
	private UniTimeHeaderPanel iHeader;
	private SimpleForm iForm;
	private InstructorSurveyData iSurvey;
	
	public InstructorSurveyWidget() {
		iForm = new SimpleForm();
		iForm.addStyleName("unitime-InstructorSurveyPage");
		
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectInstructorSurvey());
		iHeader.setCollapsible(InstructorCookie.getInstance().isShowSurveyDetails());
		iHeader.setTitleStyleName("unitime3-HeaderTitle");
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				InstructorCookie.getInstance().setShowSurveyDetails(event.getValue());
				if (iForm.getRowCount() <= 1) {
					open();
				} else {
					for (int row = 1; row < iForm.getRowCount(); row++) {
						iForm.getRowFormatter().setVisible(row, event.getValue());
					}
				}
			}
		});
		iHeader.addButton("survey", MESSAGES.buttonEditInstructorSurvey(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeFrameDialog.openDialog(MESSAGES.sectInstructorSurvey(),
						"gwt.jsp?page=instructorSurvey&menu=hide&id=" + iSurvey.getExternalId(),
						"900","90%");
			}
		});
		iHeader.setEnabled("survey", false);
		iHeader.addButton("apply", MESSAGES.buttonApplyInstructorSurveyPreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(MESSAGES.questionApplyInstructorSurveyPreferences(), new Command() {
					@Override
					public void execute() {
						iHeader.showLoading();
						RPC.execute(new InstructorSurveyInterface.InstructorSurveyApplyRequest(iInstructorId), new AsyncCallback<GwtRpcResponseNull>() {
							@Override
							public void onFailure(Throwable caught) {
								iHeader.setErrorMessage(caught.getMessage());						
							}
							@Override
							public void onSuccess(GwtRpcResponseNull result) {
								Window.Location.reload();
							}
						});
					}
				});
			}
		});
		iHeader.setEnabled("apply", false);
		
		iForm.addHeaderRow(iHeader);
		
		initWidget(iForm);
	}
	
	public void insert(final RootPanel panel) {
		iInstructorId = Long.valueOf(panel.getElement().getInnerText());
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
		if (iHeader.isCollapsible())
			open();
	}
	
	protected void open() {
		iHeader.showLoading();
		RPC.execute(new InstructorSurveyInterface.InstructorSurveyRequest(iInstructorId), new AsyncCallback<InstructorSurveyInterface.InstructorSurveyData>() {
			@Override
			public void onFailure(Throwable t) {
				iHeader.setErrorMessage(MESSAGES.failedToLoadPage(t.getMessage()));
			}
			@Override
			public void onSuccess(InstructorSurveyData survey) {
				setValue(survey);
				iHeader.clearMessage();
			}
		});
	}
	
	protected void setValue(InstructorSurveyData survey) {
		iSurvey = survey;
		if (iForm.getRowCount() > 1) {
			iForm.clear();
			iForm.addHeaderRow(iHeader);
		}
		iHeader.setEnabled("survey", survey.isEditable());
		iHeader.setEnabled("apply", survey.isCanApply());
		
		if (survey.hasEmail()) {
			iForm.addRow(MESSAGES.propEmail(), new Label(survey.getEmail()));
		}
		
		if (survey.getSubmitted() != null) {
			iForm.addRow(MESSAGES.propSubmitted(), new Label(sTimeStampFormat.format(survey.getSubmitted())));	
		} else {
			Label notSubmitted = new Label(MESSAGES.notSubbitted());
			notSubmitted.addStyleName("not-submitted");
			iForm.addRow(MESSAGES.propSubmitted(), notSubmitted);
		}
		
		if (!survey.getTimePrefs().isEmpty()) {
			InstructorTimePreferences tp = new InstructorTimePreferences(false);
			tp.setModel(survey.getTimePrefs());
			tp.setMode(survey.getTimePrefs().getModes().get(0), true);
			tp.setEditable(false);
			iForm.addRow(MESSAGES.propTimePrefs(), tp.getPanel());
			if (!tp.getReason().getText().isEmpty()) {
				iForm.addRow("", new ReadOnlyNote(tp.getReason().getText()));
			}
		}
		
		if (survey.hasRoomPreferences()) {
			for (Preferences p: survey.getRoomPreferences()) {
				if (p.hasSelections())
					iForm.addRow(p.getType(), new PreferencesReadOnlyTable(p, survey.getPrefLevels()));
			}
		}
		if (survey.hasDistributionPreferences() && survey.getDistributionPreferences().hasSelections()) {
			iForm.addRow(survey.getDistributionPreferences().getType(), new PreferencesReadOnlyTable(survey.getDistributionPreferences(), survey.getPrefLevels()));
		}
		
		if (survey.hasNote()) {
			iForm.addRow(MESSAGES.propOtherPreferences(), new ReadOnlyNote(survey.getNote()));
		}
		
		InstructorSurveyCourseTable courses = new InstructorSurveyCourseTable(survey.getCustomFields(), false);
		if (survey.hasCourses()) {
			for (Course ci: survey.getCourses()) {
				if (ci.hasCustomFields())
					courses.addRow(ci);
			}
		}
		courses.addMouseClickListener(new MouseClickListener<InstructorSurveyInterface.Course>() {
			@Override
			public void onMouseClick(TableEvent<Course> event) {
				if (event.getData() != null && event.getData().getId() != null)
					ToolBox.open("instructionalOfferingDetail.action?op=view&co=" + event.getData().getId());
			}
		});
		if (courses.getRowCount() > 1)
			iForm.addRow(MESSAGES.propInstructorCoursePreferences(), courses);
		
		if (survey.isEditable())
			iForm.addBottomRow(iHeader.clonePanel(""));
	}
}
