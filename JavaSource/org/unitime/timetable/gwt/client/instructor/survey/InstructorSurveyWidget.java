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

import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.instructor.InstructorCookie;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Course;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.IdLabel;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyData;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveySaveRequest;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.PrefLevel;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Preferences;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Problem;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Selection;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyPage.InstructorTimePreferences;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyPage.ReadOnlyNote;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingDisplayMode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

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
	private CheckBox iShowDifferences;
	
	public InstructorSurveyWidget() {
		iForm = new SimpleForm();
		iForm.setCellPadding(1);
		iForm.addStyleName("unitime-InstructorSurveyPage");
		
		iShowDifferences = new CheckBox(MESSAGES.instructorSurveyCompareWithInstructorPrefs());
		iShowDifferences.setValue(InstructorCookie.getInstance().isHighlightSurveyChanges());
		iShowDifferences.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				InstructorCookie.getInstance().setHighlightSurveyChanges(iShowDifferences.getValue());
				setValue(iSurvey);
			}
		});
		
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
		iHeader.addButton("delete", MESSAGES.buttonDeleteInstructorSurvey(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final Command delete = new Command() {
					@Override
					public void execute() {
						iHeader.showLoading();
						RPC.execute(new InstructorSurveyInterface.InstructorSurveyDeleteRequest(iInstructorId), new AsyncCallback<GwtRpcResponseNull>() {
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
				};
				if (iSurvey.isEmpty()) {
					delete.execute();
				} else {
					UniTimeConfirmationDialog.confirm(MESSAGES.questionDeleteInstructorSurveys(), delete);
				}
			}
		});
		iHeader.setEnabled("delete", false);		
		iHeader.addButton("submit", MESSAGES.buttonSubmitInstructorSurvey(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				iHeader.clearMessage();
				LoadingWidget.showLoading(MESSAGES.waitUpdatingInstructorSurvey());
				InstructorSurveySaveRequest req = new InstructorSurveySaveRequest(iSurvey, true);
				req.setChanged(false);
				req.setInstructorId(iInstructorId);
				RPC.execute(req, new AsyncCallback<InstructorSurveyData>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.hideLoading();
						UniTimeNotifications.error(caught.getMessage());
						iHeader.setErrorMessage(caught.getMessage());
					}

					@Override
					public void onSuccess(InstructorSurveyData result) {
						LoadingWidget.hideLoading();
						UniTimeNotifications.info(MESSAGES.infoInstructorSurveyUpdated());
						setValue(result);
					}
				});
			}
		});
		iHeader.setEnabled("submit", false);
		iHeader.addButton("unsubmit", MESSAGES.buttonUnsubmitInstructorSurvey(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				iHeader.clearMessage();
				LoadingWidget.showLoading(MESSAGES.waitUpdatingInstructorSurvey());
				InstructorSurveySaveRequest req = new InstructorSurveySaveRequest(iSurvey, false);
				req.setChanged(false); req.setUnsubmit(true);
				req.setInstructorId(iInstructorId);
				RPC.execute(req, new AsyncCallback<InstructorSurveyData>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.hideLoading();
						UniTimeNotifications.error(caught.getMessage());
						iHeader.setErrorMessage(caught.getMessage());
					}

					@Override
					public void onSuccess(InstructorSurveyData result) {
						LoadingWidget.hideLoading();
						UniTimeNotifications.info(MESSAGES.infoInstructorSurveyUpdated());
						setValue(result);
						if (result.hasPopupMessage()) {
							if (result.isPopupWarning())
								UniTimeNotifications.warn(result.getPopupMessage());
							else
								UniTimeNotifications.info(result.getPopupMessage());
						}
					}
				});
			}
		});
		iHeader.setEnabled("unsubmit", false);
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
	
	protected boolean isShowDifferences() {
		return (iSurvey.getSubmitted() != null || (iSurvey.getApplied() != null && iSurvey.getAppliedDeptCode() != null)) && iShowDifferences.getValue();
	}
	
	protected void setValue(InstructorSurveyData survey) {
		iSurvey = survey;
		if (iForm.getRowCount() > 1) {
			iForm.clear();
			iForm.addHeaderRow(iHeader);
		}
		iHeader.setEnabled("submit", survey.isEditable() && survey.isAdmin() && survey.getSubmitted() == null);
		iHeader.setEnabled("delete", survey.isEditable() && survey.isAdmin() && survey.getSubmitted() == null && (survey.isEmpty() || survey.isCanDelete()));
		iHeader.setEnabled("unsubmit", survey.isEditable() && survey.isAdmin() && survey.getSubmitted() != null);
		iHeader.setEnabled("apply", survey.isCanApply());
		
		if (survey.hasEmail()) {
			iForm.addRow(MESSAGES.propEmail(), new Label(survey.getEmail()));
		}
		
		if (survey.getChanged() != null && survey.getChangedBy() != null) {
			iForm.addRow(MESSAGES.propLastChange(), new Label(MESSAGES.lastChange(sTimeStampFormat.format(survey.getChanged()), survey.getChangedBy())));
		}
		if (survey.getSubmitted() != null) {
			iForm.addRow(MESSAGES.propSubmitted(), new Label(sTimeStampFormat.format(survey.getSubmitted())));	
		} else {
			Label notSubmitted = new Label(MESSAGES.notSubbitted());
			notSubmitted.addStyleName("not-submitted");
			iForm.addRow(MESSAGES.propSubmitted(), notSubmitted);
		}
		if (survey.getApplied() != null && survey.getAppliedDeptCode() != null) {
			iForm.addRow(MESSAGES.propLastApplied(), new Label(MESSAGES.lastApply(sTimeStampFormat.format(survey.getApplied()), survey.getAppliedDeptCode())));
		}
		if (iSurvey.getSubmitted() != null || (iSurvey.getApplied() != null && iSurvey.getAppliedDeptCode() != null))
			iForm.addRow("", iShowDifferences);
		if (survey.getApplied() != null && survey.getChanged() != null && survey.getApplied().before(survey.getChanged())) {
			Label updatedAfterApplied = new Label(MESSAGES.surveyUpdatedAfterApply());
			updatedAfterApplied.addStyleName("updated-after-applied");
			iForm.addRow("", updatedAfterApplied);
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
			if (isShowDifferences()) {
				if (survey.getTimePrefs().getProblem() == Problem.LEVEL_CHANGED) {
					RoomSharingDisplayMode mode = survey.getTimePrefs().getModes().get(survey.getTimePrefs().getDefaultMode());
					int day = mode.getFirstDay();
					while (true) {
						for (int slot = mode.getFirstSlot(); slot <= mode.getLastSlot(); slot += mode.getStep()) {
							InstructorTimePreferences.Cell cell = getCell(tp.getPanel(), day, slot);
							if (cell == null) continue;
							char ch = '2';
							try {
								if (survey.getTimePrefs().getInstructorPattern().length() <= 336)
									ch = survey.getTimePrefs().getInstructorPattern().charAt(48 * day + slot / 6);
								else
									ch = survey.getTimePrefs().getInstructorPattern().charAt(288 * day + slot);
							} catch (IndexOutOfBoundsException e) {}
							if (ch != survey.getTimePrefs().id2char(survey.getTimePrefs().getOption(day, slot).getId()))
								cell.addStyleName("preference-changed");
						}
						if (day == mode.getLastDay()) break;
						day = (1 + day) % 7;
					}
				}
			}
		}
		
		if (survey.hasRoomPreferences()) {
			for (Preferences p: survey.getRoomPreferences()) {
				if (p.hasSelections(iShowDifferences.getValue()))
					iForm.addRow(p.getType(), new PreferencesReadOnlyTable(p, survey.getPrefLevels()));
			}
		}
		if (survey.hasDistributionPreferences() && survey.getDistributionPreferences().hasSelections(iShowDifferences.getValue())) {
			iForm.addRow(survey.getDistributionPreferences().getType(), new PreferencesReadOnlyTable(survey.getDistributionPreferences(), survey.getPrefLevels()));
		}
		
		if (survey.hasNote()) {
			iForm.addRow(MESSAGES.propOtherPreferences(), new ReadOnlyNote(survey.getNote()));
		}
		
		InstructorSurveyCourseTable courses = new InstructorSurveyCourseTable(survey.getSessionId(), survey.getCustomFields(), false);
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
	
	class PreferencesReadOnlyTable extends P {
		PreferencesReadOnlyTable(Preferences preferences, List<PrefLevel> options) {
			super("preference-table");
			if (preferences.hasSelections()) {
				for (IdLabel item: preferences.getItems()) {
					Selection selection = preferences.getSelection(item.getId());
					if (selection != null) {
						PrefLevel level = null;
						PrefLevel instructorLevel = null;
						for (PrefLevel prefLevel: options) {
							if (prefLevel.getId().equals(selection.getLevel())) level = prefLevel;
							if (prefLevel.getId().equals(selection.getInstructorLevel())) instructorLevel = prefLevel;
						}
						if (!isShowDifferences() && level == null) continue;
						add(new PreferenceLine(item, level, instructorLevel, selection));
					}
				}
			}
		}
		
		class PreferenceLine extends P {
			
			PreferenceLine(IdLabel item, PrefLevel level, PrefLevel instructor, Selection selection) {
				super("preference-line", "preference-line-readonly");
				P line1 = new P("first-line");
				add(line1);
				
				if (level != null) {
					P preference = new P("preference-cell");
					preference.setText(level.getTitle());
					if (isShowDifferences() && selection.getProblem() == Problem.DIFFERENT_DEPT)
						preference.addStyleName("different-dept");
					else
						preference.getElement().getStyle().setColor(level.getColor());
					line1.add(preference);
				}

				P it = new P("preference-cell");
				it.setText(item.getLabel());
				if (isShowDifferences() && selection.getProblem() == Problem.DIFFERENT_DEPT)
					it.addStyleName("different-dept");
				else if (level != null)
					it.getElement().getStyle().setColor(level.getColor());
				line1.add(it);
				
				if (isShowDifferences() && instructor != null) {
					if (level != null) {
						P ct = new P("preference-cell", "pref-changed-to");
						ct.setText(MESSAGES.instructorSurveyPreferenceLevelChangedTo());
						line1.add(ct);
					} else {
						P ct = new P("preference-cell", "pref-set-to");
						ct.setText(MESSAGES.instructorSurveyPreferenceAdded());
						line1.add(ct);
					}
					
					P ip = new P("preference-cell", "new-preference-level");
					ip.setText(instructor.getTitle());
					ip.getElement().getStyle().setColor(instructor.getColor());
					line1.add(ip);
				}
				
				if (isShowDifferences() && selection.getProblem() == Problem.NOT_APPLIED) {
					P ip = new P("preference-cell", "pref-not-set");
					ip.setText(MESSAGES.instructorSurveyPreferenceNotSet());
					line1.add(ip);
				}

				/*
				if (item.hasDescription()) {
					P description = new P("description");
					description.setVisible(true);
					description.setHTML(item.getDescription());
					if (isShowDifferences() && selection.getProblem() == Problem.DIFFERENT_DEPT)
						description.addStyleName("different-dept");
					P line2 = new P("second-line");
					line2.add(description);
					add(line2);
				}
				*/
				
				if (level != null && level.isHard() && selection.hasNote()) {
					P reason = new P("reason");
					reason.setVisible(false);
					reason.setVisible(true);
					reason.setText(selection.getNote());
					if (isShowDifferences() && selection.getProblem() == Problem.DIFFERENT_DEPT)
						reason.addStyleName("different-dept");
					P line2 = new P("second-line");
					line2.add(reason);
					add(line2);
				}
			}
		}
	}
	
	public InstructorTimePreferences.Cell getCell(Widget prefs, int day, int slot) {
		if (prefs instanceof InstructorTimePreferences.Cell) {
			InstructorTimePreferences.Cell cell = (InstructorTimePreferences.Cell)prefs;
			if (cell.getDay() == day && cell.getSlot() == slot) return cell;
			return null;
		} else {
			if (prefs instanceof ComplexPanel) {
				ComplexPanel p = (ComplexPanel)prefs;
				for (int i = 0; i < p.getWidgetCount(); i++) {
					InstructorTimePreferences.Cell cell = getCell(p.getWidget(i), day, slot);
					if (cell != null) return cell;
				}
			}
			return null;
		}
	}
}
