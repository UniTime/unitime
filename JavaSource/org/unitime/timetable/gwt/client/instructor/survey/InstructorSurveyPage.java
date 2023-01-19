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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Course;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.IdLabel;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyData;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveySaveRequest;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorTimePreferencesModel;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.PrefLevel;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Preferences;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Selection;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.rooms.RoomSharingWidget;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;

/**
 * @author Tomas Muller
 */
public class InstructorSurveyPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static DateTimeFormat sTimeStampFormat = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	
	private UniTimeTextBox iEmail;
	private InstructorTimePreferences iTimePrefs;
	private List<PreferencesTable> iRoomPrefs;
	private PreferencesTable iDistPrefs;
	private Note iPrefsNote;
	private InstructorSurveyCourseTable iCourses;
	
	private InstructorSurveyData iSurvey;
	private InstructorSurveyData iOriginal;
	private ListBox iSessionSelection;
	
	public InstructorSurveyPage() {
		iPanel = new SimpleForm(3);
		iPanel.addStyleName("unitime-InstructorSurveyPage");
		
		load(Location.getParameter("id"), Location.getParameter("session"));
		
		initWidget(iPanel);
		Window.addWindowClosingHandler(new Window.ClosingHandler() {
			@Override
			public void onWindowClosing(ClosingEvent event) {
				if (isChanged()) {
					if (LoadingWidget.getInstance().isShowing())
						LoadingWidget.getInstance().hide();
					event.setMessage(MESSAGES.queryLeaveChangesOnYourInstructorSurvey());
				}
			}
		});
	}
	
	protected void load(String externalId, String session) {
		LoadingWidget.showLoading(MESSAGES.waitLoadingPage());
		RPC.execute(new InstructorSurveyInterface.InstructorSurveyRequest(externalId, session), new AsyncCallback<InstructorSurveyInterface.InstructorSurveyData>() {
			@Override
			public void onFailure(Throwable t) {
				LoadingWidget.hideLoading();
				UniTimeNotifications.error(MESSAGES.failedToLoadPage(t.getMessage()), t);
				ToolBox.checkAccess(t);
			}

			@Override
			public void onSuccess(InstructorSurveyData survey) {
				setValue(survey);
				LoadingWidget.hideLoading();
			}
		});
	}
	
	public boolean isChanged() {
		return iOriginal != null && iOriginal.isEditable() && iOriginal.isChanged(getValue());
	}
	
	public void setValue(InstructorSurveyInterface.InstructorSurveyData survey) {
		iSurvey = survey;
		iOriginal = new InstructorSurveyData(survey);
		iPanel.clear();
		
		iHeader = new UniTimeHeaderPanel(survey.getFormattedName());
		iHeader.addButton("save", MESSAGES.buttonSaveInstructorSurvey(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				iHeader.clearMessage();
				LoadingWidget.showLoading(MESSAGES.waitSavingInstructorSurvey());
				RPC.execute(new InstructorSurveySaveRequest(getValue(), false), new AsyncCallback<InstructorSurveyData>() {
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
						if (ToolBox.hasParent())
							ToolBox.closeWindow();
					}
				});
			}
		});
		iHeader.addButton("submit", MESSAGES.buttonSubmitInstructorSurvey(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				iHeader.clearMessage();
				UniTimeConfirmationDialog.confirm(MESSAGES.questionSubmitInstructorSurvey(), new Command() {
					@Override
					public void execute() {
						LoadingWidget.showLoading(MESSAGES.waitSubmittingInstructorSurvey());
						RPC.execute(new InstructorSurveySaveRequest(getValue(), true), new AsyncCallback<InstructorSurveyData>() {
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
								if (ToolBox.hasParent())
									ToolBox.closeWindow();
							}
						});
					}
				});
			}
		});
		if (ToolBox.hasParent() || "hide".equals(Window.Location.getParameter("menu"))) {
			iHeader.addButton("close", MESSAGES.buttonCloseInstructorSurvey(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent e) {
					if (isChanged()) {
						if (LoadingWidget.getInstance().isShowing())
							LoadingWidget.getInstance().hide();
						UniTimeConfirmationDialog.confirm(MESSAGES.queryLeaveChangesOnInstructorSurvey(), new Command() {
							@Override
							public void execute() {
								ToolBox.closeWindow();
							}
						});
					} else {
						ToolBox.closeWindow();
					}
				}
			});
		}

		iHeader.setEnabled("save", iSurvey.isEditable());
		iHeader.setEnabled("submit", iSurvey.isEditable());
		iPanel.addHeaderRow(iHeader);
		
		iPanel.addRow(MESSAGES.propExternalId(), new Label(survey.getExternalId()));
		
		if (survey.hasSessions()) {
			iSessionSelection = new ListBox();
			for (AcademicSessionInfo session: survey.getSessions()) {
				iSessionSelection.addItem(session.getName(), session.getSessionId().toString());
				if (survey.getSessionId().equals(session.getSessionId()))
					iSessionSelection.setSelectedIndex(iSessionSelection.getItemCount() - 1);
			}
			if (iSessionSelection.getItemCount() <= 1)
				iSessionSelection.setEnabled(false);
			iSessionSelection.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					final String newSessionId = iSessionSelection.getSelectedValue();
					if (!newSessionId.equals(iSurvey.getSessionId().toString())) {
						if (isChanged()) {
							UniTimeConfirmationDialog d = new UniTimeConfirmationDialog(
									UniTimeConfirmationDialog.Type.CONFIRM,
									MESSAGES.queryLeaveChangesOnInstructorSurvey(),
									null, null,
									new Command() {
										@Override
										public void execute() {
											load(iSurvey.getExternalId(), newSessionId);
										}
									});
							d.addCloseHandler(new CloseHandler<PopupPanel>() {
								@Override
								public void onClose(CloseEvent<PopupPanel> event) {
									for (int i = 0; i < iSessionSelection.getItemCount(); i++) {
										if (iOriginal.getSessionId().toString().equals(iSessionSelection.getValue(i))) {
											iSessionSelection.setSelectedIndex(i);
											break;
										}
									}
								}
							});
							d.center();
						} else {
							load(iSurvey.getExternalId(), newSessionId);
						}
					}
				}
			});
			iPanel.addRow(MESSAGES.propAcademicSession(), iSessionSelection);
		}
		
		if (survey.isEditable()) {
			iEmail = new UniTimeTextBox();
			iEmail.addStyleName("email");
			if (survey.hasEmail()) iEmail.setText(survey.getEmail());
			iPanel.addRow(MESSAGES.propEmail(), iEmail);
		} else if (survey.hasEmail()) {
			iPanel.addRow(MESSAGES.propEmail(), new Label(survey.getEmail()));
		}
		
		if (survey.getSubmitted() != null) {
			iPanel.addRow(MESSAGES.propSubmitted(), new Label(sTimeStampFormat.format(survey.getSubmitted())));	
		}
		
		if (survey.hasDepartments()) {
			P depts = new P("departments");
			for (InstructorSurveyInterface.InstructorDepartment dept: survey.getDepartments()) {
				String label = dept.getLabel() + (dept.hasPosition() ? " (" + dept.getPosition().getLabel() + ")" : "");
				depts.add(new Label(label, false));
			}
			iPanel.addRow(MESSAGES.propDepartment(), depts);
		}
		
		iPanel.addHeaderRow(new UniTimeHeaderPanel(MESSAGES.sectGeneralPreferences()));
		iTimePrefs = new InstructorTimePreferences(survey.isEditable());
		iTimePrefs.setModel(survey.getTimePrefs());
		iTimePrefs.setMode(survey.getTimePrefs().getModes().get(0), true);
		iTimePrefs.getReason().setEnabled(iSurvey.isEditable());
		iPanel.addRow(iTimePrefs.getPanel());
		if (iSurvey.isEditable()) {
			iPanel.addRow("", iTimePrefs.getReason());
			iTimePrefs.getReason().resizeNotes();
		}
		else if (!iTimePrefs.getReason().getText().isEmpty()) {
			iPanel.addRow("", new ReadOnlyNote(iTimePrefs.getReason().getText()));
		}
			
		iRoomPrefs = new ArrayList<PreferencesTable>();
		if (survey.hasRoomPreferences()) {
			for (Preferences p: survey.getRoomPreferences()) {
				if (iSurvey.isEditable()) {
					PreferencesTable tab = new PreferencesTable(p, survey.getPrefLevels());
					iRoomPrefs.add(tab);
					iPanel.addRow(p.getType(), tab);
					tab.resizeNotes();
				} else if (p.hasSelections()) {
					iPanel.addRow(p.getType(), new PreferencesReadOnlyTable(p, survey.getPrefLevels()));
				}
			}
		}
		iDistPrefs = null;
		if (survey.hasDistributionPreferences()) {
			if (iSurvey.isEditable()) {
				iDistPrefs = new PreferencesTable(survey.getDistributionPreferences(), survey.getPrefLevels());
				iPanel.addRow(survey.getDistributionPreferences().getType(), iDistPrefs);
				iDistPrefs.resizeNotes();
			} else if (survey.getDistributionPreferences().hasSelections()) {
				iPanel.addRow(survey.getDistributionPreferences().getType(), new PreferencesReadOnlyTable(survey.getDistributionPreferences(), survey.getPrefLevels()));
			}
		}
		if (survey.isEditable()) {
			iPrefsNote = new Note();
			iPrefsNote.setText(survey.getNote());
			iPanel.addRow(MESSAGES.propOtherPreferences(), iPrefsNote);
			iPrefsNote.resizeNotes();
		} else if (survey.hasNote()) {
			iPanel.addRow(MESSAGES.propOtherPreferences(), new ReadOnlyNote(survey.getNote()));
		}
		
		iPanel.addHeaderRow(new UniTimeHeaderPanel(MESSAGES.sectCoursePreferences()));
		iCourses = new InstructorSurveyCourseTable(survey.getCustomFields(), survey.isEditable());
		if (survey.hasCourses()) {
			for (Course ci: survey.getCourses()) {
				if (!survey.isEditable() && !ci.hasCustomFields()) continue;
				iCourses.addRow(ci);
			}
		}
		if (survey.isEditable())
			for (int i = 0; i < 2; i++) {
				iCourses.addRow(new Course());
			}
		int row = iPanel.addRow(iCourses);
		iPanel.getCellFormatter().getElement(row, 0).getStyle().setPadding(0, Unit.PX);
		
		iFooter = iHeader.clonePanel("");
		iPanel.addBottomRow(iFooter);
		if (!iSurvey.isEditable()) {
			if (iSurvey.getSubmitted() != null)
				iHeader.setMessage(MESSAGES.infoInstructorSurveySubmitted(sTimeStampFormat.format(iSurvey.getSubmitted())));
			else
				iHeader.setMessage(MESSAGES.infoInstructorSurveyNotEditable());
		}
	}
	
	static class PreferencesTable extends P {
		static int lastId = 0;
		ChangeHandler iChangeHandler;
		HandlerRegistration iHandlerRegistration;
		Preferences iPreferences;
		
		PreferencesTable(Preferences preferences, List<PrefLevel> options) {
			super("preference-table");
			iPreferences = preferences;
			if (preferences.hasSelections()) {
				for (Selection selection: preferences.getSelections()) {
					PreferenceLine p = new PreferenceLine(iPreferences.getItems(), options);
					p.setValue(selection);
					add(p);
				}
			}
			add(new PreferenceLine(iPreferences.getItems(), options));
			
			iChangeHandler = new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent e) {
					if (((PreferenceLine)getWidget(getWidgetCount() - 1)).getId() != null) {
						PreferenceLine p = new PreferenceLine(iPreferences.getItems(), options);
						add(p);
						iHandlerRegistration.removeHandler();
						iHandlerRegistration = p.addChangeHandler(iChangeHandler);
						fixButtons();
					}
				}
			};
			fixButtons();
		}
		
		protected void update() {
			iPreferences.clearSelections();
			for (int i = 0; i < getWidgetCount(); i++) {
				PreferenceLine p = (PreferenceLine)getWidget(i);
				Selection selection = p.getValue();
				if (selection != null)
					iPreferences.addSelection(selection);
			}
		}
		
		protected void fixButtons() {
			if (iHandlerRegistration != null) 
				iHandlerRegistration.removeHandler();
			for (int i = 0; i < getWidgetCount(); i++) {
				PreferenceLine p = ((PreferenceLine)getWidget(i)); 
				if (i < getWidgetCount() - 1) {
					p.setButtonAdd(false);
				} else {
					p.setButtonAdd(true);
					iHandlerRegistration = p.addChangeHandler(iChangeHandler);		
				}
			}
		}
		
		protected void resizeNotes() {
			for (int i = 0; i < getWidgetCount(); i++) {
				PreferenceLine p = ((PreferenceLine)getWidget(i));
				p.iReason.resizeNotes();
			}
		}
		
		class PreferenceLine extends P implements HasChangeHandlers, TakesValue<Selection> {
			ListBox iList;
			List<RadioButton> iRadios;
			Image iButton;
			boolean iButtonAdd = false;
			List<PrefLevel> iOptions;
			Collection<IdLabel> iItems;
			P iDescription;
			Note iReason;
			
			PreferenceLine(Collection<IdLabel> items, List<PrefLevel> options) {
				super("preference-line");
				P line1 = new P("first-line");
				P line2 = new P("second-line");
				add(line1); add(line2);
				
				iOptions = options;
				iItems = items;

				iList = new ListBox();
				iList.addItem("-", "");
				for (IdLabel item: items)
					iList.addItem(item.getLabel(), item.getId().toString());
				iList.addStyleName("preference-cell");
				iList.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						fixOptions();
						PreferenceLine.this.fireEvent(event);
					}
				});
				line1.add(iList);

				iRadios = new ArrayList<RadioButton>();
				for (final PrefLevel option: options) {
					RadioButton opt = new RadioButton("pref" + lastId, option.getLabel());
					opt.setTitle(option.getTitle());
					opt.getElement().getStyle().setColor(option.getColor());
					opt.addStyleName("preference-cell");
					iRadios.add(opt);
					line1.add(opt);
					opt.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), PreferenceLine.this);
							PrefLevel level = PreferenceLine.this.getSelection();
							iReason.setVisible(level != null && level.isHard());
							if (iReason.isVisible()) {
								iDescription.removeStyleName("wide-description");
								iReason.setHint(MESSAGES.hintProvideReasonFor(level.getTitle(), iList.getSelectedItemText()));
							} else
								iDescription.addStyleName("wide-description");
						}
					});
				}
				
				iButton = new Image(RESOURCES.delete());
				iButton.setTitle(MESSAGES.titleDeleteRow());
				iButton.addStyleName("preference-cell");
				iButton.getElement().getStyle().setCursor(Cursor.POINTER);
				iButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (iButtonAdd) {
							PreferencesTable.this.add(new PreferenceLine(iItems, options));
						} else {
							PreferencesTable.this.remove(PreferenceLine.this);
						}
						PreferencesTable.this.fixButtons();
					}
				});
				line1.add(iButton);
				
				iDescription = new P("description", "wide-description");
				iDescription.setVisible(false);
				line2.add(iDescription);
				
				iReason = new Note(); iReason.addStyleName("reason");
				iReason.setCharacterWidth(49);
				iReason.setVisible(false);
				line2.add(iReason);

				fixOptions();
				lastId ++;
			}
			
			protected IdLabel getItem() {
				String id = iList.getSelectedValue();
				if (id == null || id.isEmpty()) return null;
				for (IdLabel item: iItems)
					if (item.getId().toString().equals(id)) return item;
				return null;
			}
			
			protected void fixOptions() {
				IdLabel item = getItem();
				if (item != null && item.hasDescription()) {
					iDescription.setVisible(true);
					iDescription.setHTML(item.getDescription());
					iReason.setCharacterWidth(49);
				} else {
					iDescription.setVisible(false);
					iReason.setCharacterWidth(95);
				}
				int nbrVisible = 0;
				RadioButton lastVisible = null;
				PrefLevel level = null;
				for (int i = 0; i < iOptions.size(); i++) {
					RadioButton opt = iRadios.get(i);
					PrefLevel option = iOptions.get(i);
					if (item == null || !item.isAllowedPref(option.getId())) {
						opt.setEnabled(false);
						opt.setVisible(false);
						opt.setValue(null);
					} else {
						opt.setEnabled(true);
						opt.setVisible(true);
						lastVisible = opt;
						nbrVisible ++;
						if (Boolean.TRUE.equals(opt.getValue()))
							level = option;
					}
				}
				if (nbrVisible == 1) lastVisible.setValue(true);
				iReason.setVisible(level != null && level.isHard());
				if (iReason.isVisible()) {
					iDescription.removeStyleName("wide-description");
					iReason.setHint(MESSAGES.hintProvideReasonFor(level.getTitle(), iList.getSelectedItemText()));
				} else
					iDescription.addStyleName("wide-description");
			}
			
			public PrefLevel getSelection() {
				for (int i = 0; i < iOptions.size(); i++) {
					RadioButton opt = iRadios.get(i);
					if (Boolean.TRUE.equals(opt.getValue()))
						return iOptions.get(i);
				}
				return null;
			}
			
			public Long getId() {
				String id = iList.getSelectedValue();
				if (id == null || id.isEmpty()) return null;
				if (getSelection() == null) return null;
				return Long.valueOf(id);
			}

			public void setId(Long id) {
				if (id == null) {
					iList.setSelectedIndex(0);
				} else {
					for (int i = 1; i < iList.getItemCount(); i++) {
						if (iList.getValue(i).equals(id.toString())) {
							iList.setSelectedIndex(i);
							break;
						}
					}
				}
			}

			@Override
			public HandlerRegistration addChangeHandler(ChangeHandler handler) {
				return addDomHandler(handler, ChangeEvent.getType());
			}
			
			public void setButtonAdd(boolean add) {
				iButtonAdd = add;
				if (add) {
					iButton.setResource(RESOURCES.add());
					iButton.setTitle(MESSAGES.titleAddRow());
				} else {
					iButton.setResource(RESOURCES.delete());
					iButton.setTitle(MESSAGES.titleDeleteRow());
				}
			}

			@Override
			public void setValue(Selection value) {
				if (value == null) {
					iList.setSelectedIndex(0);
					for (RadioButton b: iRadios)
						b.setValue(false);
					iReason.setText("");
				} else {
					for (int i = 0; i < iList.getItemCount(); i++) {
						if (value.getItem().toString().equals(iList.getValue(i))) {
							iList.setSelectedIndex(i); break;
						}
					}
					for (int i = 0; i < iOptions.size(); i++) {
						RadioButton opt = iRadios.get(i);
						opt.setValue(iOptions.get(i).getId().equals(value.getLevel()));
					}
					iReason.setText(value.getNote() == null ? "" : value.getNote());
				}
				fixOptions();
				iReason.setVisible(getSelection() != null && getSelection().isHard());
				iReason.resizeNotes();
			}

			@Override
			public Selection getValue() {
				IdLabel item = getItem();
				PrefLevel pref = getSelection();
				if (item != null && pref != null)
					return new Selection(item.getId(), pref.getId(), iReason.getText());
				return null;
			}
		}
		
	}
	
	static class PreferencesReadOnlyTable extends P {
		
		PreferencesReadOnlyTable(Preferences preferences, List<PrefLevel> options) {
			super("preference-table");
			if (preferences.hasSelections()) {
				for (Selection selection: preferences.getSelections()) {
					IdLabel item = preferences.getItem(selection.getItem());
					for (PrefLevel prefLevel: options) {
						if (prefLevel.getId().equals(selection.getLevel())) {
							add(new PreferenceLine(item, prefLevel, selection));
							break;
						}
					}
				}
			}
		}
		
		class PreferenceLine extends P {
			P iItem;
			P iPreference;
			P iDescription;
			P iReason;
			List<PrefLevel> iOptions;
			Collection<IdLabel> iItems;
			
			PreferenceLine(IdLabel item, PrefLevel preference, Selection selection) {
				super("preference-line", "preference-line-readonly");
				P line1 = new P("first-line");
				add(line1);
				
				iPreference = new P("preference-cell");
				iPreference.setText(preference.getTitle());
				iPreference.getElement().getStyle().setColor(preference.getColor());
				line1.add(iPreference);

				iItem = new P("preference-cell");
				iItem.setText(item.getLabel());
				iItem.getElement().getStyle().setColor(preference.getColor());
				line1.add(iItem);

				if (item.hasDescription()) {
					iDescription = new P("description");
					iDescription.setVisible(true);
					iDescription.setHTML(item.getDescription());
					P line2 = new P("second-line");
					line2.add(iDescription);
					add(line2);
				}
				
				if (preference.isHard() && selection.hasNote()) {
					iReason = new P("reason");
					iReason.setVisible(false);
					iReason.setVisible(true);
					iReason.setText(selection.getNote());
					P line2 = new P("second-line");
					line2.add(iReason);
					add(line2);
				}
			}
		}
	}
	
	public InstructorSurveyInterface.InstructorSurveyData getValue() {
		iSurvey.setEmail(iEmail.getValue());
		if (iSurvey.getTimePrefs() != null)
			iSurvey.getTimePrefs().setNote(iTimePrefs.getReason().getText());
		for (PreferencesTable pt: iRoomPrefs) {
			pt.update();
		}
		if (iDistPrefs != null) iDistPrefs.update();
		iSurvey.setNote(iPrefsNote.getText());
		iSurvey.clearCourses();
		for (Course c: iCourses.getData()) {
			if (c.hasCourseName() || c.hasCustomFields())
				iSurvey.addCourse(c);
		}
		return iSurvey;
	}
	
	public static class ReadOnlyNote extends Label {
		public ReadOnlyNote(String text) {
			addStyleName("read-only-note");
			setText(text);
		}
	}
	
	public static class Note extends TextArea {
		Timer iTimer;
		private String iHint = "";
		
		public Note() {
			setStyleName("unitime-TextArea");
			setHeight("45px");
			setCharacterWidth(95);
			getElement().setAttribute("maxlength", "2048");
			
			iTimer = new Timer() {
				@Override
				public void run() {
					resizeNotes();
				}
			};
			addKeyPressHandler(new KeyPressHandler() {
				@Override
				public void onKeyPress(KeyPressEvent event) {
					iTimer.schedule(10);
				}
			});
			addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					iTimer.schedule(10);
				}
			});
			addBlurHandler(new BlurHandler() {
				public void onBlur(BlurEvent event) {
					if (Note.super.getText().isEmpty()) {
						if (!iHint.isEmpty()) {
							Note.super.setText(iHint);
							addStyleName("notes-hint");
						}
					}
				}
			});
			addFocusHandler(new FocusHandler() {
				public void onFocus(FocusEvent event) {
					if (!iHint.isEmpty() && Note.super.getText().equals(iHint)) {
						Note.super.setText("");
						removeStyleName("notes-hint");
					}
				}
			});
		}
		
		@Override
		public String getText() {
			if (super.getText().equals(iHint)) return "";
			return super.getText();
		}
		
		@Override
		public void setText(String text) { 
			if (text == null || text.isEmpty()) {
				super.setText(iHint);
				if (!iHint.isEmpty())
					addStyleName("notes-hint");
				else
					removeStyleName("notes-hint");
			} else {
				super.setText(text);
				removeStyleName("notes-hint");
			}
		}
		
		public void resizeNotes() {
			if (!getText().isEmpty()) {
				setHeight(Math.max(45, getElement().getScrollHeight()) + "px");
			} else {
				setHeight("45px");
			}
		}
		
		public void setHint(String hint) {
			if (super.getText().equals(iHint)) {
				super.setText(hint);
				if (!hint.isEmpty())
					addStyleName("notes-hint");
				else
					removeStyleName("notes-hint");
			}
			iHint = hint;
		}
		
		public String getHint() { return iHint; }
		
		protected void checkHint() {
			if (getValue().isEmpty()) {
				
			}
		}
	}
	
	public static class InstructorTimePreferences extends RoomSharingWidget {
		Note iReason;
		
		public InstructorTimePreferences(boolean editable) {
			super(editable, false);
			
			iReason = new Note(); iReason.addStyleName("prohibited-times-reason");
			iReason.setVisible(false);
			iReason.setHint(MESSAGES.hintProvideReasonForProhibitedTimes());
			
			addValueChangeHandler(new ValueChangeHandler<RoomSharingModel>() {
				@Override
				public void onValueChange(ValueChangeEvent<RoomSharingModel> event) {
					iReason.setVisible(event.getValue() != null && event.getValue().countOptions(-7l)>0);
				}
			});
		}
		
		public Note getReason() {
			return iReason;
		}
		
		public void setModel(InstructorTimePreferencesModel model) {
			super.setModel(model);
			iReason.setText(model.getNote());
			iReason.resizeNotes();
			iReason.setVisible(model.countOptions(-7l)>0);
		}
	}
}
