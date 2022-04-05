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
package org.unitime.timetable.gwt.client.courseofferings;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.sectioning.CourseDetailsWidget;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CoordinatorInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseCreditFormatInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseCreditTypeInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseCreditUnitTypeInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingCheckExists;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingCheckExistsInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingCheckPermissions;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingConstantsInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingConstantsRequest;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingPermissionsInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingPropertiesInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingPropertiesRequest;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseTypeInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.GetCourseOfferingRequest;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.GetCourseOfferingResponse;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.InstructorInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.OfferingConsentTypeInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.OverrideTypeInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.ResponsibilityInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.SubjectAreaInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.UpdateCourseOfferingAction;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.UpdateCourseOfferingRequest;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.WaitListInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

public class CourseOfferingEdit extends Composite {

	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	Logger logger = java.util.logging.Logger.getLogger("CourseOfferingEdit");

	private SimpleForm iPanel;
	private SimpleForm iInstructorPanel;
	private SimpleForm overrideTypesForm;
	private UniTimeHeaderPanel iTitleAndButtons;
	private UniTimeWidget<UniTimeTextBox> iCourseNumber, iTitle, iUnits, iMaxUnits, iExternalId;
	private UniTimeTextBox iNewEnrollmentDeadline, iClassChangesDeadline, iCourseDropDeadline;
	private UniTimeWidget<ListBox> iConsent, iFundingDepartment, iSubjectArea, iCredit, iCreditType, iCreditUnitType, iCourseDemands, iAlternativeCourseOfferings, iCourseType, iWaitListing;
	private UniTimeWidget<TextArea> iScheduleNote, iRequestsNotes;
	private Label iCreditText, iScheduleNoteText, iConsentText, iCourseDemandsText, iRequestNotesText, iTitleText, iNewEnrollmentDeadlineText, iClassChangesDeadlineText;
	private Label iCourseDropDeadlineText, iReservationOnlyText, iWaitListingText;

	private UniTimeTable iInstructorsTable;
	private Button iAddCoordinatorButton;
	private String iCourseNbrRegex;
	private String iCourseNbrInfo;
	private String iDefaultTeachingResponsibilityId;
	private Boolean iCourseOfferingNumberMustBeUnique;
	private Boolean iCourseOfferingNumberUpperCase;
	private Boolean iAllowAlternativeCourseOfferings;
	private Boolean iCoursesFundingDepartmentsEnabled;
	private Boolean iCanEditExternalIds;
	private Boolean iCanShowExternalIds;
	private Boolean iAllowDemandCourseOfferings;
	private Image waitListingImage;
	
	private int iPrefRowsAdded;
	
	private int iNewEnrollmentDeadlineLine, iClassChangesDeadlineLine, iCourseDropDeadlineLine, iAltCourseOfferingLine, iFundingDeptLine, iCourseNumberLine, iTitleLine, iReservationOnlyLine;
	private int iExternalIdLine, iCourseTypeLine, iConsentLine, iInstructorPanelLine, iRequestsNotesLine, iScheduleNoteLine;
	private int iCreditSectionLine, iCreditTextLine, iCatalogLinkLabelLine, iCourseUrlProviderLine, iOverrideTypeLine, iDescEnrollmentDeadlinesLine;
	private int iCourseDemandsLine, iWaitListingLine, iScheduleNoteTextLine, iConsentTextLine, iCourseDemandsTextLine, iRequestNotesTextLine, iTitleTextLine;
	private int iNewEnrollmentDeadlineTextLine, iClassChangesDeadlineTextLine, iCourseDropDeadlineTextLine, iReservationOnlyTextLine, iWaitListingTextLine;
	
	private P iNewEnrollmentDeadlinePanel, iCourseDropDeadlinePanel, iClassChangesDeadlinePanel, iReservationOnlyPanel, iWaitListingPanel;
	private P iNewEnrollmentDeadlineNote, iCourseDropDeadlineNote, iClassChangesDeadlineNote, iDescEnrollmentDeadlinesNote;

	private Anchor iCatalogLinkLabel;
	
	private CourseDetailsWidget iCourseUrlProvider;
	
	private CheckBox iByReservationOnly, iFractional, iOverrideType;
	
	private CourseOfferingInterface iCourseOffering = null;
	private String courseAbbr;
	
	private Boolean iIsAdd = false;
	private Boolean iIsEdit = false;
	private Boolean iIsReload = false;
	private Boolean iCanAddCourseOffering = false;
	private Boolean iCanEditCourseOffering = false;
	private Boolean iCanEditCourseOfferingNote = false;
	private Boolean iCanEditCourseOfferingCoordinators = false;
	private Boolean iIsControl = false;
	
	private Long iCourseOfferingId;
	private Long iSubjAreaId;
	private Long iInstructionalOfferingId;
	private String iCourseNbr;
	
	public CourseOfferingEdit() {
		iPanel = new SimpleForm();
		initWidget(iPanel);
		iPanel.addStyleName("unitime-CourseOfferingEdit");
		
		Long offeringId = null;
		String op = null;
		Long subjAreaId = null;
		Long courseOfferingId = null;
		
		if (Window.Location.getParameter("op") != null)
			op = Window.Location.getParameter("op");

		if ("editCourseOffering".equals(op)) {
			if (Window.Location.getParameter("offering") != null) {
				offeringId =  Long.valueOf(Window.Location.getParameter("offering"));
				iIsEdit = true;
				iIsAdd = false;
				iIsReload = false;
				courseOfferingId = new Long(offeringId);
				iCourseOfferingId = courseOfferingId;
			}
		} else if ("addCourseOffering".equals(op)) {
			iIsAdd = true;
			iIsEdit = false;
			iIsReload = false;
			if (Window.Location.getParameter("subjArea") != null && !Window.Location.getParameter("subjArea").isEmpty()) {
				subjAreaId = Long.valueOf(Window.Location.getParameter("subjArea"));
				iSubjAreaId = subjAreaId;
			} else {
				iSubjAreaId = subjAreaId;
			}
			if (Window.Location.getParameter("courseNbr") != null && !Window.Location.getParameter("courseNbr").isEmpty()) {
				iCourseNbr = Window.Location.getParameter("courseNbr");;
			} else {
				iCourseNbr = null;
			}
		}

		iTitleAndButtons = new UniTimeHeaderPanel("Course Offering");
		iTitleAndButtons.addButton("update", MESSAGES.buttonUpdate(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iIsEdit = true;
				iIsAdd = false;
				iIsReload = false;
				if (!validate(iIsEdit)) return;
				iTitleAndButtons.clearMessage();
				UpdateCourseOfferingRequest request = new UpdateCourseOfferingRequest();
				request.setAction(UpdateCourseOfferingAction.UPDATE);
				request.setCourseOffering(getValue());
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				
				if (iCourseOfferingNumberMustBeUnique) {
					CourseOfferingCheckExists existRequest = new CourseOfferingCheckExists();
					existRequest.setCourseNumber(request.getCourseOffering().getCourseNbr());
					existRequest.setSubjectAreaId(request.getCourseOffering().getSubjectAreaId());
					existRequest.setCourseOfferingId(iCourseOfferingId);
					existRequest.setIsEdit(iIsEdit);
					
					RPC.execute(existRequest, new AsyncCallback<CourseOfferingCheckExistsInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							handleError(caught);
						}
						@Override
						public void onSuccess(CourseOfferingCheckExistsInterface result) {
							if (result.getResponseText().isEmpty()) {
								RPC.execute(request, new AsyncCallback<CourseOfferingInterface>() {
									@Override
									public void onFailure(Throwable caught) {
										handleError(caught);
									}
									@Override
									public void onSuccess(CourseOfferingInterface result) {
										if (result.getErrorMessage() == null || result.getErrorMessage().isEmpty()) {
											LoadingWidget.getInstance().hide();
											iInstructionalOfferingId = result.getInstrOfferingId();
											afterSaveOrUpdate(iInstructionalOfferingId);
										} else {
											LoadingWidget.getInstance().hide();
											iTitleAndButtons.setErrorMessage(result.getErrorMessage());
											return;
										}
									}
								});
							} else {
								LoadingWidget.getInstance().hide();
								iTitleAndButtons.setErrorMessage(result.getResponseText());
								return;
							}
						}
					});
				} else {
					RPC.execute(request, new AsyncCallback<CourseOfferingInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							handleError(caught);
						}
						@Override
						public void onSuccess(CourseOfferingInterface result) {
							if (result.getErrorMessage() == null || result.getErrorMessage().isEmpty()) {
								LoadingWidget.getInstance().hide();
								iInstructionalOfferingId = result.getInstrOfferingId();
								afterSaveOrUpdate(iInstructionalOfferingId);
							} else {
								LoadingWidget.getInstance().hide();
								iTitleAndButtons.setErrorMessage(result.getErrorMessage());
								return;
							}
						}
					});	
				}
			}
		});
		iTitleAndButtons.addButton("save", MESSAGES.buttonSave(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iIsAdd = true;
				iIsEdit = false;
				iIsReload = false;
				if (!validate(iIsEdit)) return;
				iTitleAndButtons.clearMessage();
				UpdateCourseOfferingRequest request = new UpdateCourseOfferingRequest();
				request.setAction(UpdateCourseOfferingAction.CREATE);
				request.setCourseOffering(getValue());
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				
				if (iCourseOfferingNumberMustBeUnique) {
					CourseOfferingCheckExists existRequest = new CourseOfferingCheckExists();
					existRequest.setCourseNumber(request.getCourseOffering().getCourseNbr());
					existRequest.setSubjectAreaId(request.getCourseOffering().getSubjectAreaId());
					existRequest.setIsEdit(iIsEdit);
					
					RPC.execute(existRequest, new AsyncCallback<CourseOfferingCheckExistsInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							handleError(caught);
						}
						@Override
						public void onSuccess(CourseOfferingCheckExistsInterface result) {
							
							if (result.getResponseText().isEmpty()) {
								RPC.execute(request, new AsyncCallback<CourseOfferingInterface>() {
									@Override
									public void onFailure(Throwable caught) {
										handleError(caught);
									}
									@Override
									public void onSuccess(CourseOfferingInterface result) {
										if (result.getErrorMessage() == null || result.getErrorMessage().isEmpty()) {
											LoadingWidget.getInstance().hide();
											iInstructionalOfferingId = result.getInstrOfferingId();
											afterSaveOrUpdate(iInstructionalOfferingId);
										} else {
											LoadingWidget.getInstance().hide();
											iTitleAndButtons.setErrorMessage(result.getErrorMessage());
											return;
										}
									}
								});
							} else {
								LoadingWidget.getInstance().hide();
								iTitleAndButtons.setErrorMessage(result.getResponseText());
								return;
							}
						}
					});
				} else {
					RPC.execute(request, new AsyncCallback<CourseOfferingInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							handleError(caught);
						}
						@Override
						public void onSuccess(CourseOfferingInterface result) {
							if (result.getErrorMessage() == null || result.getErrorMessage().isEmpty()) {
								LoadingWidget.getInstance().hide();
								iInstructionalOfferingId = result.getInstrOfferingId();
								afterSaveOrUpdate(iInstructionalOfferingId);
							} else {
								LoadingWidget.getInstance().hide();
								iTitleAndButtons.setErrorMessage(result.getErrorMessage());
								return;
							}
						}
					});
				}
			}
		});
		
		if (iIsAdd) {
			iTitleAndButtons.addButton("back", MESSAGES.buttonBack(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					onBack(false, null);
				}
			});
		} else {
			iTitleAndButtons.addButton("back", MESSAGES.buttonBack(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					onBack(true, iCourseOfferingId);
				}
			});
		}

		iPanel.addHeaderRow(iTitleAndButtons);
		
		if (iIsAdd) {
			//Subject Area Dropdown
			
			iSubjectArea = new UniTimeWidget<ListBox>(new ListBox());
			iSubjectArea.getWidget().setStyleName("unitime-TextBox");
			if (subjAreaId == null) {
				iSubjectArea.getWidget().setSelectedIndex(0);
			}
			
			iSubjectArea.getWidget().addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					iSubjectArea.clearHint();
					Long subjectAreaId = new Long(iSubjectArea.getWidget().getSelectedValue());
					//Reload
					RPC.execute(new CourseOfferingCheckPermissions(null, subjectAreaId), new AsyncCallback<CourseOfferingPermissionsInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							handleError(caught);
						}
						@Override
						public void onSuccess(CourseOfferingPermissionsInterface result) {
							iCanAddCourseOffering = result.getCanAddCourseOffering();
							iCanEditCourseOffering = result.getCanEditCourseOffering();
							iCanEditCourseOfferingNote = result.getCanEditCourseOfferingNote();
							iCanEditCourseOfferingCoordinators = result.getCanEditCourseOfferingCoordinators();

							iIsReload = true;
							iIsAdd = false;
							iSubjAreaId = subjectAreaId;
							reload(iSubjAreaId);
						}
					});
				}
			});
			iPanel.addRow(MESSAGES.propSubject(), iSubjectArea);
		}
		
		iCourseNumber = new UniTimeWidget<UniTimeTextBox>(new UniTimeTextBox());
		iCourseNumber.getWidget().setMaxLength(40);
		iCourseNumber.getWidget().setVisibleLength(40);
		if (iIsAdd && iCourseNbr != null) {
			iCourseNumber.getWidget().setValue(iCourseNbr);
		}
		iCourseNumber.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCourseNumber.clearHint();
				iTitleAndButtons.clearMessage();
			}
		});
		iCourseNumberLine = iPanel.addRow(MESSAGES.propCourseNumber(), iCourseNumber);		
		
		iTitle = new UniTimeWidget<UniTimeTextBox>(new UniTimeTextBox());
		iTitle.getWidget().setMaxLength(200);
		iTitle.getWidget().setVisibleLength(100);
		iTitleLine = iPanel.addRow(MESSAGES.propTitle(), iTitle);
		
		iTitleText = new Label("Override");
		iTitleTextLine = iPanel.addRow(MESSAGES.propTitle(), iTitleText, 1);
		iPanel.getRowFormatter().setVisible(iTitleTextLine, false);
		
		iExternalId = new UniTimeWidget<UniTimeTextBox>(new UniTimeTextBox(30, ValueBoxBase.TextAlignment.LEFT));
		iExternalIdLine = iPanel.addRow(MESSAGES.propExternalId(), iExternalId);
		
		iCourseType = new UniTimeWidget<ListBox>(new ListBox());
		iCourseType.getWidget().setStyleName("unitime-TextBox");
		iCourseType.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCourseType.clearHint();
			}
		});
		iCourseTypeLine = iPanel.addRow(MESSAGES.propType(), iCourseType);

		iScheduleNote = new UniTimeWidget<TextArea>(new TextArea());
		iScheduleNote.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iScheduleNote.clearHint();
			}
		});
		iScheduleNote.getWidget().setStyleName("unitime-TextArea");
		iScheduleNote.getWidget().setVisibleLines(4);
		iScheduleNote.getWidget().setCharacterWidth(57);
		iScheduleNoteLine = iPanel.addRow(MESSAGES.propScheduleNote(), iScheduleNote);
		
		iScheduleNoteText = new Label("Override");
		iScheduleNoteTextLine = iPanel.addRow(MESSAGES.propScheduleNote(), iScheduleNoteText, 1);
		iPanel.getRowFormatter().setVisible(iScheduleNoteTextLine, false);

		//Consent Dropdown
		
		iConsent = new UniTimeWidget<ListBox>(new ListBox());
		iConsent.getWidget().setStyleName("unitime-TextBox");
		iConsent.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iConsent.clearHint();
			}
		});
		iConsentLine = iPanel.addRow(MESSAGES.propConsent(), iConsent);
		
		iConsentText = new Label("Override");
		iConsentTextLine = iPanel.addRow(MESSAGES.propConsent(), iConsentText, 1);
		iPanel.getRowFormatter().setVisible(iConsentTextLine, false);

		//Begin Credit section
		
		iCreditText = new Label("Override");
		iCreditTextLine = iPanel.addRow(MESSAGES.propCredit(), iCreditText, 1);

		SimpleForm indentedForm = new SimpleForm();
		
		iCredit = new UniTimeWidget<ListBox>(new ListBox());
		iCredit.getWidget().setStyleName("unitime-TextBox");
		iCredit.getWidget().setSelectedIndex(0);
		iCredit.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCredit.clearHint();
				toggleCreditSection();
			}
		});
		indentedForm.addRow(iCredit);
		
		indentedForm.removeStyleName("unitime-NotPrintableBottomLine");

		iCreditType = new UniTimeWidget<ListBox>(new ListBox());
		iCreditType.getWidget().setStyleName("unitime-TextBox");
		iCreditType.getWidget().setSelectedIndex(0);
		iCreditType.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCreditType.clearHint();
			}
		});
		indentedForm.addRow(MESSAGES.propCreditType(), iCreditType);
		iCreditType.getWidget().setEnabled(false);
		
		iCreditUnitType = new UniTimeWidget<ListBox>(new ListBox());
		iCreditUnitType.getWidget().setStyleName("unitime-TextBox");
		iCreditUnitType.getWidget().setSelectedIndex(0);
		iCreditUnitType.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCreditUnitType.clearHint();
			}
		});
		indentedForm.addRow(MESSAGES.propCreditUnitType(), iCreditUnitType);
		iCreditUnitType.getWidget().setEnabled(false);
		
		iUnits = new UniTimeWidget<UniTimeTextBox>(new UniTimeTextBox());
		iUnits.getWidget().setMaxLength(4);
		iUnits.getWidget().setVisibleLength(4);
		indentedForm.addRow(MESSAGES.propUnits(), iUnits);
		iUnits.getWidget().setEnabled(false);
		
		iMaxUnits = new UniTimeWidget<UniTimeTextBox>(new UniTimeTextBox());
		iMaxUnits.getWidget().setMaxLength(4);
		iMaxUnits.getWidget().setVisibleLength(4);
		indentedForm.addRow(MESSAGES.propMaxUnits(), iMaxUnits);
		iMaxUnits.getWidget().setEnabled(false);
		
		iFractional = new CheckBox();
		indentedForm.addRow(MESSAGES.propFractional(), iFractional);
		iFractional.setEnabled(false);
		
		iCreditSectionLine = iPanel.addRow(MESSAGES.propCredit(), indentedForm);
		
		indentedForm.getElement().getParentElement().setAttribute("class", "courseOfferingEditPadding");
		
		//End Credit section
		
		//Begin Take course demands from offering section
		
		iCourseDemands = new UniTimeWidget<ListBox>(new ListBox());
		iCourseDemands.getWidget().setStyleName("unitime-TextBox");
		iCourseDemands.getWidget().setSelectedIndex(0);
		iCourseDemands.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCourseDemands.clearHint();
			}
		});
		iCourseDemandsLine = iPanel.addRow(MESSAGES.propCourseDemands(), iCourseDemands);
		
		iCourseDemandsText = new Label("Override");
		iCourseDemandsTextLine = iPanel.addRow(MESSAGES.propCourseDemands(), iCourseDemandsText, 1);
		iPanel.getRowFormatter().setVisible(iCourseDemandsTextLine, false);

		//End Take course demands from offering section
		
		//Begin Alternative course offering
		
		iAlternativeCourseOfferings = new UniTimeWidget<ListBox>(new ListBox());
		iAlternativeCourseOfferings.getWidget().setStyleName("unitime-TextBox");
		iAlternativeCourseOfferings.getWidget().setSelectedIndex(0);
		iAlternativeCourseOfferings.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iAlternativeCourseOfferings.clearHint();
			}
		});
		iAltCourseOfferingLine = iPanel.addRow(MESSAGES.propAlternativeCourseOffering(), iAlternativeCourseOfferings);
		iPanel.getRowFormatter().setVisible(iAltCourseOfferingLine, false);
				
		//End Alternative course offering
		
		//Catalog Link Label
		iCatalogLinkLabel = new Anchor();
		iCatalogLinkLabelLine = iPanel.addRow(MESSAGES.propertyCourseCatalog(), iCatalogLinkLabel);
		iPanel.getRowFormatter().setVisible(iCatalogLinkLabelLine, false);

		//CourseURLProvider
		iCourseUrlProvider = new CourseDetailsWidget(true);
		iCourseUrlProvider.setVisible(true);
		iCourseUrlProviderLine = iPanel.addRow(MESSAGES.propertyCourseCatalog(), iCourseUrlProvider);
		iPanel.getRowFormatter().setVisible(iCourseUrlProviderLine, true);

		//Begin Coordinators section
		
		ClickHandler deleteCoordinator = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AriaButton clickedDeleteButton = (AriaButton) event.getSource();
				String clickedButtonId = clickedDeleteButton.getElement().getId();
				Integer clickedButtonIdInteger = Integer.valueOf(clickedButtonId);

				int rowCount = iInstructorsTable.getRowCount();
				for (int i = 1; i < rowCount; i++) {
					AriaButton deleteButton = (AriaButton) iInstructorsTable.getWidget(i, 3);
					String buttonId = deleteButton.getElement().getId();
					Integer buttonIdInteger = Integer.valueOf(buttonId);
					if (buttonIdInteger.equals(clickedButtonIdInteger)) {
						iInstructorsTable.removeRow(i);
						break;
					}
				}
			}
		};
		
		ClickHandler clickAddCoordinator = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RPC.execute(new CourseOfferingConstantsRequest(), new AsyncCallback<CourseOfferingConstantsInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						handleError(caught);
					}

					@Override
					public void onSuccess(CourseOfferingConstantsInterface result) {
						iPrefRowsAdded = result.getPrefRowsAdded();
						Integer maxId = getMaxId(iInstructorsTable);

						int i = 0;
						while (i < iPrefRowsAdded) {
							maxId++;
							List<Widget> widgets = new ArrayList<Widget>();
							UniTimeWidget<ListBox> instructorDropdown = new UniTimeWidget<ListBox>(new ListBox());
							UniTimeWidget<UniTimeTextBox> shareTextBox = new UniTimeWidget<UniTimeTextBox>(new UniTimeTextBox(4, ValueBoxBase.TextAlignment.LEFT));
							UniTimeWidget<ListBox> responsibilitiesDropdown = new UniTimeWidget<ListBox>(new ListBox());
							Button deleteButton = new AriaButton(MESSAGES.opGroupDelete());
							deleteButton.addStyleName("unitime-NoPrint");
							deleteButton.getElement().setId(maxId.toString());
							deleteButton.addClickHandler(deleteCoordinator);
							
							widgets.add(instructorDropdown);
							widgets.add(shareTextBox);
							widgets.add(responsibilitiesDropdown);
							widgets.add(deleteButton);
							
							iInstructorsTable.addRow(null, widgets);

							i++;
						}
						
						setInstructorDropdowns();
					}
				});
			}
		};
		
		iInstructorsTable = new UniTimeTable();
		
		List<Widget> instructorHeader = new ArrayList<Widget>();
		P nameColumnHeader = new P();
		nameColumnHeader.setText(MESSAGES.fieldName());
		instructorHeader.add(nameColumnHeader);
		
		P shareColumnHeader = new P();
		shareColumnHeader.setText(MESSAGES.colPercentShareInstructor());
		instructorHeader.add(shareColumnHeader);
		
		P responsibilityColumnHeader = new P();
		responsibilityColumnHeader.setText(MESSAGES.colTeachingResponsibility());
		instructorHeader.add(responsibilityColumnHeader);
		
		P emptyColumnHeader = new P();
		emptyColumnHeader.setText("");
		instructorHeader.add(emptyColumnHeader);

		iInstructorsTable.addRow(null, instructorHeader);

		iAddCoordinatorButton = new AriaButton(MESSAGES.buttonAddCoordinator());
		iAddCoordinatorButton.addStyleName("unitime-NoPrint");
		iAddCoordinatorButton.addClickHandler(clickAddCoordinator);

		iInstructorPanel = new SimpleForm();
		iInstructorPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iInstructorPanel.addRow(iInstructorsTable);
		iInstructorPanel.addRow(iAddCoordinatorButton);
		iInstructorPanelLine = iPanel.addRow(MESSAGES.propCoordinators(), iInstructorPanel);
		
		iInstructorPanel.getElement().getParentElement().setAttribute("class", "courseOfferingEditPadding");

		//End Coordinators section

		iByReservationOnly = new CheckBox(MESSAGES.checkByReservationOnly());
		iReservationOnlyLine = iPanel.addRow(MESSAGES.propByReservationOnly(), iByReservationOnly);

		Image checkMark = new Image(RESOURCES.on());
		checkMark.setAltText("ENABLED");
		checkMark.addStyleName("number");
		checkMark.setTitle(MESSAGES.descriptionByReservationOnly2());
		iReservationOnlyPanel = new P("deadline");
		iReservationOnlyPanel.add(checkMark);
		iReservationOnlyText = new Label(MESSAGES.descriptionByReservationOnly2());
		iReservationOnlyText.addStyleName("note");
		iReservationOnlyPanel.add(iReservationOnlyText);
		iReservationOnlyTextLine = iPanel.addRow(MESSAGES.propByReservationOnly(), iReservationOnlyPanel, 1);
		iPanel.getRowFormatter().setVisible(iReservationOnlyTextLine, false);
		

		iNewEnrollmentDeadline = new UniTimeTextBox(4, ValueBoxBase.TextAlignment.LEFT);
		iNewEnrollmentDeadline.addStyleName("number");
		iNewEnrollmentDeadlinePanel = new P("deadline");
		iNewEnrollmentDeadlinePanel.add(iNewEnrollmentDeadline);
		iNewEnrollmentDeadlineNote = new P("note");
		iNewEnrollmentDeadlinePanel.add(iNewEnrollmentDeadlineNote);
		iNewEnrollmentDeadlineLine = iPanel.addRow(MESSAGES.propNewEnrollmentDeadline(), iNewEnrollmentDeadlinePanel);
		
		iNewEnrollmentDeadlineText = new Label("Override");
		iNewEnrollmentDeadlineTextLine = iPanel.addRow(MESSAGES.propNewEnrollmentDeadline(), iNewEnrollmentDeadlineText, 1);
		iPanel.getRowFormatter().setVisible(iNewEnrollmentDeadlineTextLine, false);
		
		iClassChangesDeadline = new UniTimeTextBox(4, ValueBoxBase.TextAlignment.LEFT);
		iClassChangesDeadline.addStyleName("number");
		iClassChangesDeadlinePanel = new P("deadline");
		iClassChangesDeadlinePanel.add(iClassChangesDeadline);
		iClassChangesDeadlineNote = new P("note");
		iClassChangesDeadlinePanel.add(iClassChangesDeadlineNote);
		iClassChangesDeadlineLine = iPanel.addRow(MESSAGES.propClassChangesDeadline(), iClassChangesDeadlinePanel);
		
		iClassChangesDeadlineText = new Label("Override");
		iClassChangesDeadlineTextLine = iPanel.addRow(MESSAGES.propClassChangesDeadline(), iClassChangesDeadlineText, 1);
		iPanel.getRowFormatter().setVisible(iClassChangesDeadlineTextLine, false);
		
		iCourseDropDeadline = new UniTimeTextBox(4, ValueBoxBase.TextAlignment.LEFT);
		iCourseDropDeadline.addStyleName("number");
		iCourseDropDeadlinePanel = new P("deadline");
		iCourseDropDeadlinePanel.add(iCourseDropDeadline);
		iCourseDropDeadlineNote = new P("note");
		iCourseDropDeadlinePanel.add(iCourseDropDeadlineNote);
		iCourseDropDeadlineLine = iPanel.addRow(MESSAGES.propCourseDropDeadline(), iCourseDropDeadlinePanel);
		
		iCourseDropDeadlineText = new Label("Override");
		iCourseDropDeadlineTextLine = iPanel.addRow(MESSAGES.propCourseDropDeadline(), iCourseDropDeadlineText, 1);
		iPanel.getRowFormatter().setVisible(iCourseDropDeadlineTextLine, false);
		
		iDescEnrollmentDeadlinesNote = new P("note");
		iDescEnrollmentDeadlinesLine = iPanel.addRow("", iDescEnrollmentDeadlinesNote);

		iRequestsNotes = new UniTimeWidget<TextArea>(new TextArea());
		iRequestsNotes.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iRequestsNotes.clearHint();
			}
		});
		iRequestsNotes.getWidget().setStyleName("unitime-TextArea");
		iRequestsNotes.getWidget().setVisibleLines(4);
		iRequestsNotes.getWidget().setCharacterWidth(57);
		iRequestsNotesLine = iPanel.addRow(MESSAGES.propRequestsNotes(), iRequestsNotes);
		
		iRequestNotesText = new Label("Override");
		iRequestNotesTextLine = iPanel.addRow(MESSAGES.propRequestsNotes(), iRequestNotesText, 1);
		iPanel.getRowFormatter().setVisible(iRequestNotesTextLine, false);

		iFundingDepartment = new UniTimeWidget<ListBox>(new ListBox());
		iFundingDepartment.getWidget().setStyleName("unitime-TextBox");
		iFundingDepartment.getWidget().setSelectedIndex(0);
		iFundingDepartment.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iFundingDepartment.clearHint();
			}
		});
		iFundingDeptLine = iPanel.addRow(MESSAGES.propFundingDepartment(), iFundingDepartment);
		iPanel.getRowFormatter().setVisible(iFundingDeptLine, false);
		
		//Waitlisting
		iWaitListing = new UniTimeWidget<ListBox>(new ListBox());
		iWaitListing.getWidget().setStyleName("unitime-TextBox");
		iWaitListing.getWidget().setSelectedIndex(0);
		iWaitListing.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iWaitListing.clearHint();
			}
		});
		iWaitListingLine = iPanel.addRow(MESSAGES.propWaitListing(), iWaitListing);
		
		waitListingImage = new Image(RESOURCES.on());
		waitListingImage.setAltText("ENABLED");
		waitListingImage.addStyleName("number");
		waitListingImage.setTitle(MESSAGES.descWaitListEnabled());
		iWaitListingPanel = new P("deadline");
		iWaitListingPanel.add(waitListingImage);
		iWaitListingText = new Label();
		iWaitListingText.addStyleName("note");
		iWaitListingPanel.add(iWaitListingText);
		iWaitListingTextLine = iPanel.addRow(MESSAGES.propWaitListing(), iWaitListingPanel, 1);
		iPanel.getRowFormatter().setVisible(iWaitListingTextLine, false);
		
		//Override Types
		overrideTypesForm = new SimpleForm();
		iOverrideType = new CheckBox(MESSAGES.checkByReservationOnly());
		overrideTypesForm.addRow(iOverrideType);
		iOverrideTypeLine = iPanel.addRow(MESSAGES.propertyDisabledOverrides(), overrideTypesForm);
		overrideTypesForm.removeStyleName("unitime-NotPrintableBottomLine");
		iPanel.getRowFormatter().setVisible(iOverrideTypeLine, false);

		RPC.execute(new CourseOfferingCheckPermissions(courseOfferingId, subjAreaId), new AsyncCallback<CourseOfferingPermissionsInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				handleError(caught);
			}
			@Override
			public void onSuccess(CourseOfferingPermissionsInterface result) {
				iCanAddCourseOffering = result.getCanAddCourseOffering();
				iCanEditCourseOffering = result.getCanEditCourseOffering();
				iCanEditCourseOfferingNote = result.getCanEditCourseOfferingNote();
				iCanEditCourseOfferingCoordinators = result.getCanEditCourseOfferingCoordinators();

				if (iIsEdit) {
					RPC.execute(new GetCourseOfferingRequest(iCourseOfferingId), new AsyncCallback<GetCourseOfferingResponse>() {
						@Override
						public void onFailure(Throwable caught) {
							handleError(caught);
						}

						@Override
						public void onSuccess(GetCourseOfferingResponse result) {
							iCourseOffering = result.getCourseOffering();
							setValue(iCourseOffering, null);
							LoadingWidget.getInstance().hide();
						}
					});
				} else if (iIsAdd) {
					setValue(null, iSubjAreaId);
				}
			}
		});

        iPanel.addNotPrintableBottomRow(iTitleAndButtons.clonePanel(null));
	}
	
	private int getMaxId(UniTimeTable instructorTable) {
		Integer maxId = 0;
		int rowCount = instructorTable.getRowCount();
		for (int i = 1; i < rowCount; i++) {
			AriaButton deleteButton = (AriaButton) iInstructorsTable.getWidget(i, 3);
			String buttonId = deleteButton.getElement().getId();
			Integer buttonIdInteger = Integer.valueOf(buttonId);
			if (buttonIdInteger > maxId) {
				maxId = buttonIdInteger;
			}
		}
		
		return maxId;
	}
	
	protected void onBack(boolean iIsEdit, Long courseOfferingId) {
		if (iIsEdit) {
			Long instructionalOfferingId = null;
			instructionalOfferingId = new Long(iCourseOffering.getInstrOfferingId());
			ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?op=view&io=" + instructionalOfferingId);
		} else {
			ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingShowSearch.do");
		}
	}
	
	private void toggleCreditSection() {
		//Enable/Disable other Credit fields
		if (iCredit.getWidget().getSelectedValue() != null && !iCredit.getWidget().getSelectedValue().isEmpty()) {
			String credit = iCredit.getWidget().getSelectedValue();

			switch (credit) {
			case "arrangeHours":
				iCreditType.getWidget().setEnabled(true);
				iCreditUnitType.getWidget().setEnabled(true);
				iUnits.getWidget().setEnabled(false);
				iMaxUnits.getWidget().setEnabled(false);
				iFractional.setEnabled(false);
				break;
			case "fixedUnit":
				iCreditType.getWidget().setEnabled(true);
				iCreditUnitType.getWidget().setEnabled(true);
				iUnits.getWidget().setEnabled(true);
				iMaxUnits.getWidget().setEnabled(false);
				iFractional.setEnabled(false);					
				break;
			case "variableMinMax":
				iCreditType.getWidget().setEnabled(true);
				iCreditUnitType.getWidget().setEnabled(true);
				iUnits.getWidget().setEnabled(true);
				iMaxUnits.getWidget().setEnabled(true);
				iFractional.setEnabled(false);	
				break;
			case "variableRange":
				iCreditType.getWidget().setEnabled(true);
				iCreditUnitType.getWidget().setEnabled(true);
				iUnits.getWidget().setEnabled(true);
				iMaxUnits.getWidget().setEnabled(true);
				iFractional.setEnabled(true);
				break;
			default:
				iCreditType.getWidget().setEnabled(false);
				iCreditUnitType.getWidget().setEnabled(false);
				iUnits.getWidget().setEnabled(false);
				iMaxUnits.getWidget().setEnabled(false);
				iFractional.setEnabled(false);
				break;
			}
		} else {
			iCreditType.getWidget().setEnabled(false);
			iCreditUnitType.getWidget().setEnabled(false);
			iUnits.getWidget().setEnabled(false);
			iMaxUnits.getWidget().setEnabled(false);
			iFractional.setEnabled(false);
		}
	}
	
	protected void afterSaveOrUpdate(Long instructionalOfferingId) {
		ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?op=view&io=" + instructionalOfferingId);
	}
	
	public void constructInstructorsTable() {
		ClickHandler deleteCoordinator = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AriaButton clickedDeleteButton = (AriaButton) event.getSource();
				String clickedButtonId = clickedDeleteButton.getElement().getId();
				Integer clickedButtonIdInteger = Integer.valueOf(clickedButtonId);

				int rowCount = iInstructorsTable.getRowCount();
				for (int i = 1; i < rowCount; i++) {
					AriaButton deleteButton = (AriaButton) iInstructorsTable.getWidget(i, 3);
					String buttonId = deleteButton.getElement().getId();
					Integer buttonIdInteger = Integer.valueOf(buttonId);
					if (buttonIdInteger.equals(clickedButtonIdInteger)) {
						//remove row
						iInstructorsTable.removeRow(i);
						break;
					}
				}
			}
		};

		int i = 0;
		while (i < iPrefRowsAdded) {
			List<Widget> widgets = new ArrayList<Widget>();
			UniTimeWidget<ListBox> instructorDropdown = new UniTimeWidget<ListBox>(new ListBox());
			UniTimeWidget<UniTimeTextBox> shareTextBox = new UniTimeWidget<UniTimeTextBox>(new UniTimeTextBox(4, ValueBoxBase.TextAlignment.LEFT));
			UniTimeWidget<ListBox> responsibilitiesDropdown = new UniTimeWidget<ListBox>(new ListBox());
			Button deleteButton = new AriaButton(MESSAGES.opGroupDelete());
			deleteButton.addStyleName("unitime-NoPrint");
			deleteButton.addClickHandler(deleteCoordinator);
			
			widgets.add(instructorDropdown);
			widgets.add(shareTextBox);
			widgets.add(responsibilitiesDropdown);
			widgets.add(deleteButton);
			
			Integer rowNumber = iInstructorsTable.addRow(null, widgets);
			deleteButton.getElement().setId(rowNumber.toString());
			
			i++;
		}
	}
	
	public void setInstructorDropdowns() {
		RPC.execute(new CourseOfferingPropertiesRequest(false, iSubjAreaId), new AsyncCallback<CourseOfferingPropertiesInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				handleError(caught);
			}
			@Override
			public void onSuccess(CourseOfferingPropertiesInterface result) {
				iDefaultTeachingResponsibilityId = result.getDefaultTeachingResponsibilityId();

				for (int i = 1; i < iInstructorsTable.getRowCount(); i++) { //Skip the first row
					UniTimeWidget<ListBox> instructorDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 0);
					UniTimeWidget<ListBox> responsibilityDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 2);

					if (instructorDropdown.getWidget().getSelectedIndex() == 0) {
			            while (instructorDropdown.getWidget().getItemCount() != 0) {
			            	instructorDropdown.getWidget().removeItem(0);
			            }
						
						instructorDropdown.getWidget().addItem("-", "-");
						for (InstructorInterface instructorObject: result.getInstructors()) {
							instructorDropdown.getWidget().addItem(instructorObject.getLabel(), instructorObject.getId().toString());
						}
					}
					if (instructorDropdown.getWidget().getItemCount() == 0) {
						instructorDropdown.getWidget().addItem("-", "-");
						for (InstructorInterface instructorObject: result.getInstructors()) {
							instructorDropdown.getWidget().addItem(instructorObject.getLabel(), instructorObject.getId().toString());
						}
					}
					
					if (iDefaultTeachingResponsibilityId != null && !iDefaultTeachingResponsibilityId.isEmpty()) {
						if (responsibilityDropdown.getWidget().getItemCount() == 0) {
							for (ResponsibilityInterface responsibilityObject: result.getResponsibilities()) {
								responsibilityDropdown.getWidget().addItem(responsibilityObject.getLabel(), responsibilityObject.getId().toString());
							}
							
							int responsibilitiesDropdownIndex = 0;
							for (int j = 1; j < responsibilityDropdown.getWidget().getItemCount(); j++) {
								if (responsibilityDropdown.getWidget().getValue(j).equals(iDefaultTeachingResponsibilityId)) {
									responsibilitiesDropdownIndex = j;
									break;
								}
							}
								
							responsibilityDropdown.getWidget().setSelectedIndex(responsibilitiesDropdownIndex);
							
						}
					} else {
						if (responsibilityDropdown.getWidget().getSelectedIndex() == 0 || responsibilityDropdown.getWidget().getItemCount() == 0) {
				            while (responsibilityDropdown.getWidget().getItemCount() != 0) {
				            	responsibilityDropdown.getWidget().removeItem(0);
				            }
							
				            responsibilityDropdown.getWidget().addItem("-", "");
							for (ResponsibilityInterface responsibilityObject: result.getResponsibilities()) {
								responsibilityDropdown.getWidget().addItem(responsibilityObject.getLabel(), responsibilityObject.getId().toString());
							}
						}
					}
		        }
			}
		});
	}
	
	public void reload(Long subjAreaId) {
		iIsControl = true;
		iAllowDemandCourseOfferings = true;
		iCourseUrlProvider.populate(iSubjectArea, iCourseNumber, null);
		iCourseUrlProvider.setVisible(true);
		
		if (!iCanAddCourseOffering) {
			iPanel.getRowFormatter().setVisible(iCourseNumberLine, false);
			iTitle.getWidget().setReadOnly(true);
			iExternalId.getWidget().setReadOnly(true);
			iScheduleNote.getWidget().setReadOnly(true);
			iPanel.getRowFormatter().setVisible(iConsentLine, false);
			
			//Hide the instructors section
			iPanel.getRowFormatter().setVisible(iInstructorPanelLine, false);
			
			//Hide byReservationOnly and wk fields
			iPanel.getRowFormatter().setVisible(iReservationOnlyLine, false);
			
			//Hide wk rows
			iPanel.getRowFormatter().setVisible(iNewEnrollmentDeadlineLine, false);
			iPanel.getRowFormatter().setVisible(iClassChangesDeadlineLine, false);
			iPanel.getRowFormatter().setVisible(iCourseDropDeadlineLine, false);
			iPanel.getRowFormatter().setVisible(iDescEnrollmentDeadlinesLine, false);
			
			//Hide notes
			iPanel.getRowFormatter().setVisible(iRequestsNotesLine, false);
		}

		iCourseOffering = new CourseOfferingInterface();
		UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAddCourseOffering());
		RPC.execute(new CourseOfferingPropertiesRequest(false, iSubjAreaId), new AsyncCallback<CourseOfferingPropertiesInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				handleError(caught);
			}
			@Override
			public void onSuccess(CourseOfferingPropertiesInterface result) {
				for (SubjectAreaInterface subjectAreaItem: result.getSubjectAreas()) {
					iSubjectArea.getWidget().addItem(subjectAreaItem.getLabel(), subjectAreaItem.getId().toString());
				}
				if (subjAreaId != null) {
					int indexToFind = -1;
					for (int i = 0; i < iSubjectArea.getWidget().getItemCount(); i++) {
						//Match by Value
						if (iSubjectArea.getWidget().getValue(i).equals(subjAreaId.toString())) {
							indexToFind = i;
							break;
						}
					}
					iSubjectArea.getWidget().setSelectedIndex(indexToFind);
				}
				
				iPrefRowsAdded = 0;
				constructInstructorsTable();

				iDefaultTeachingResponsibilityId = result.getDefaultTeachingResponsibilityId();

				for (int i = 1; i < iInstructorsTable.getRowCount(); i++) {
					UniTimeWidget<ListBox> instructorDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 0);
					UniTimeWidget<ListBox> responsibilitiesDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 2);
					
					String selectedInstructor = instructorDropdown.getWidget().getSelectedValue();
					
					instructorDropdown.getWidget().clear();
					instructorDropdown.getWidget().addItem("-", "-");
					for (InstructorInterface instructorObject: result.getInstructors()) {
						instructorDropdown.getWidget().addItem(instructorObject.getLabel(), instructorObject.getId().toString());
					}
					
					int instructorDropdownIndex = 0;
					for (int j = 1; j < instructorDropdown.getWidget().getItemCount(); j++) {
						if (instructorDropdown.getWidget().getValue(j).equals(selectedInstructor)) {
							instructorDropdownIndex = j;
							break;
						}
					}
					
					instructorDropdown.getWidget().setSelectedIndex(instructorDropdownIndex);
					
					String selectedResponsibility = responsibilitiesDropdown.getWidget().getSelectedValue();
					
					responsibilitiesDropdown.getWidget().clear();
					if (iDefaultTeachingResponsibilityId == null || iDefaultTeachingResponsibilityId.isEmpty()) {
						responsibilitiesDropdown.getWidget().addItem("-", "");
					}
					for (ResponsibilityInterface responsibilityObject: result.getResponsibilities()) {
						responsibilitiesDropdown.getWidget().addItem(responsibilityObject.getLabel(), responsibilityObject.getId().toString());
					}
					
					int responsibilitiesDropdownIndex = 0;
					for (int j = 1; j < responsibilitiesDropdown.getWidget().getItemCount(); j++) {
						if (selectedResponsibility != null && !selectedResponsibility.isEmpty()) {
							if (responsibilitiesDropdown.getWidget().getValue(j).equals(selectedResponsibility)) {
								responsibilitiesDropdownIndex = j;
								break;
							}
						} else {
							if (iDefaultTeachingResponsibilityId != null && !iDefaultTeachingResponsibilityId.isEmpty()) {
								if (responsibilitiesDropdown.getWidget().getValue(j).equals(iDefaultTeachingResponsibilityId)) {
									responsibilitiesDropdownIndex = j;
									break;
								}
							}
						}
					}
					responsibilitiesDropdown.getWidget().setSelectedIndex(responsibilitiesDropdownIndex);
		        }
				
				if (result.getResponsibilities() == null || result.getResponsibilities().isEmpty()) {
					iInstructorsTable.setColumnVisible(2, false);
				} else {
					iInstructorsTable.setColumnVisible(2, true);
				}

				iCoursesFundingDepartmentsEnabled = result.getCoursesFundingDepartmentsEnabled();
				
				if (iCoursesFundingDepartmentsEnabled) {
					iFundingDepartment.getWidget().clear();
					for (DepartmentInterface fundingDepartment: result.getFundingDepartments()) {
						iFundingDepartment.getWidget().addItem(fundingDepartment.getLabel(), fundingDepartment.getId().toString());
					}
					iPanel.getRowFormatter().setVisible(iFundingDeptLine, true);
					
					if (result.getSubjectAreaEffectiveFundingDept() != null) {
						int fundingDeptDropdownIndex = 0;
						for (int i = 0; i < iFundingDepartment.getWidget().getItemCount(); i++) {
							if (iFundingDepartment.getWidget().getValue(i).equals(result.getSubjectAreaEffectiveFundingDept().toString())) {
								fundingDeptDropdownIndex = i;
								break;
							}
						}
						iFundingDepartment.getWidget().setSelectedIndex(fundingDeptDropdownIndex);
					}
				}
			}
		});
	}
	
	public void setValue(CourseOfferingInterface courseOffering, Long subjAreaId) {
		if (iIsAdd) {
			// Add Course Offering
			iTitleAndButtons.setEnabled("save", true);
			iTitleAndButtons.setEnabled("back", true);
			iTitleAndButtons.setEnabled("update", false);
			iTitleAndButtons.setHeaderTitle("");
			
			iIsControl = true;
			iAllowDemandCourseOfferings = true;
			iPanel.getRowFormatter().setVisible(iCreditTextLine, false);
			iCourseUrlProvider.populate(iSubjectArea, iCourseNumber, null);
			iCourseUrlProvider.setVisible(true);
			
			if (!iCanAddCourseOffering) {
				iPanel.getRowFormatter().setVisible(iCourseNumberLine, false);
				iTitle.getWidget().setReadOnly(true);
				iExternalId.getWidget().setReadOnly(true);
				iScheduleNote.getWidget().setReadOnly(true);
				iPanel.getRowFormatter().setVisible(iConsentLine, false);
				
				//Hide the instructors section
				iPanel.getRowFormatter().setVisible(iInstructorPanelLine, false);
				
				//Hide byReservationOnly and wk fields
				iPanel.getRowFormatter().setVisible(iReservationOnlyLine, false);
				
				//Hide wk rows
				iPanel.getRowFormatter().setVisible(iNewEnrollmentDeadlineLine, false);
				iPanel.getRowFormatter().setVisible(iClassChangesDeadlineLine, false);
				iPanel.getRowFormatter().setVisible(iCourseDropDeadlineLine, false);
				iPanel.getRowFormatter().setVisible(iDescEnrollmentDeadlinesLine, false);
				
				//Hide notes
				iPanel.getRowFormatter().setVisible(iRequestsNotesLine, false);
			}

			iCourseOffering = new CourseOfferingInterface();
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAddCourseOffering());
			RPC.execute(new CourseOfferingPropertiesRequest(false, iSubjAreaId, iCourseNbr), new AsyncCallback<CourseOfferingPropertiesInterface>() {
				@Override
				public void onFailure(Throwable caught) {
					handleError(caught);
				}
				@Override
				public void onSuccess(CourseOfferingPropertiesInterface result) {
					for (SubjectAreaInterface subjectAreaItem: result.getSubjectAreas()) {
						iSubjectArea.getWidget().addItem(subjectAreaItem.getAbbreviation(), subjectAreaItem.getId().toString());
					}
					if (subjAreaId != null) {
						int indexToFind = -1;
						for (int i = 0; i < iSubjectArea.getWidget().getItemCount(); i++) {
							//Match by Value
							if (iSubjectArea.getWidget().getValue(i).equals(subjAreaId.toString())) {
								indexToFind = i;
								break;
							}
						}
						iSubjectArea.getWidget().setSelectedIndex(indexToFind);
					}
					
					if (result.getCourseOfferingMustBeUnique()) {
						if (result.getInstructionalOfferingId() != null) {
							//Redirect
							ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?op=view&io=" + result.getInstructionalOfferingId());
						}
					}
					
					iPrefRowsAdded = result.getPrefRowsAdded();
					constructInstructorsTable();
					
					iCourseDropDeadlineNote.setText(MESSAGES.hintCourseDropDeadline(result.getWkDropDefault().toString()));
					iNewEnrollmentDeadlineNote.setText(MESSAGES.hintNewEnrollmentDeadline(result.getWkEnrollDefault().toString()));
					iClassChangesDeadlineNote.setText(MESSAGES.hintClassChangesDeadline(result.getWkChangeDefault().toString()));
					iDescEnrollmentDeadlinesNote.setText(MESSAGES.descriptionEnrollmentDeadlines(result.getWeekStartDayOfWeek().toString()));
					
					if (!result.getCourseTypes().isEmpty()) {
						iCourseType.getWidget().addItem(MESSAGES.itemSelect(), "");
						for (CourseTypeInterface courseType: result.getCourseTypes()) {
							iCourseType.getWidget().addItem(courseType.getLabel(), courseType.getId().toString());
						}
					} else {
						//Hide the courseTypes dropdown
						iPanel.getRowFormatter().setVisible(iCourseTypeLine, false);
					}

					iCredit.getWidget().addItem(MESSAGES.itemSelect(), "select");
					for (CourseCreditFormatInterface courseCreditFormat: result.getCourseCreditFormats()) {
						iCredit.getWidget().addItem(courseCreditFormat.getLabel(), courseCreditFormat.getReference().toString());
					}
					for (CourseCreditTypeInterface courseCreditType: result.getCourseCreditTypes()) {
						iCreditType.getWidget().addItem(courseCreditType.getLabel(), courseCreditType.getId().toString());
					}
					for (CourseCreditUnitTypeInterface courseCreditUnitType: result.getCourseCreditUnitTypes()) {
						iCreditUnitType.getWidget().addItem(courseCreditUnitType.getLabel(), courseCreditUnitType.getId().toString());
					}
					
					if (iCanAddCourseOffering) {
						if (iAllowDemandCourseOfferings) {
							if (result.getCourseDemands() == null || result.getCourseDemands().isEmpty()) {
								iPanel.getRowFormatter().setVisible(iCourseDemandsLine, false);
							} else {
								iCourseDemands.getWidget().addItem("", "none");
								for (CourseOfferingInterface courseOffering: result.getCourseDemands()) {
									iCourseDemands.getWidget().addItem(courseOffering.getLabel(), courseOffering.getId().toString());
								}
							}
						}
					}

					iConsent.getWidget().addItem(MESSAGES.consentNone(), "none");
					for (OfferingConsentTypeInterface offeringConsentType: result.getOfferingConsentTypes()) {
						iConsent.getWidget().addItem(offeringConsentType.getLabel(), offeringConsentType.getId().toString());
					}
					
					iDefaultTeachingResponsibilityId = result.getDefaultTeachingResponsibilityId();

					for (int i = 1; i < iInstructorsTable.getRowCount(); i++) {
						UniTimeWidget<ListBox> instructorDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 0);
						UniTimeWidget<ListBox> responsibilitiesDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 2);
						
						instructorDropdown.getWidget().addItem("-", "-");
						for (InstructorInterface instructorObject: result.getInstructors()) {
							instructorDropdown.getWidget().addItem(instructorObject.getLabel(), instructorObject.getId().toString());
						}
						
						if (iDefaultTeachingResponsibilityId == null || iDefaultTeachingResponsibilityId.isEmpty()) {
							responsibilitiesDropdown.getWidget().addItem("-", "");
						}
						for (ResponsibilityInterface responsibilityObject: result.getResponsibilities()) {
							responsibilitiesDropdown.getWidget().addItem(responsibilityObject.getLabel(), responsibilityObject.getId().toString());
						}
						
						if (iDefaultTeachingResponsibilityId != null && !iDefaultTeachingResponsibilityId.isEmpty()) {
							int responsibilitiesDropdownIndex = 0;
							for (int j = 1; j < responsibilitiesDropdown.getWidget().getItemCount(); j++) {
								if (responsibilitiesDropdown.getWidget().getValue(j).equals(iDefaultTeachingResponsibilityId)) {
									responsibilitiesDropdownIndex = j;
									break;
								}
							}
							
							responsibilitiesDropdown.getWidget().setSelectedIndex(responsibilitiesDropdownIndex);
						}
			        }
					
					if (result.getResponsibilities() == null || result.getResponsibilities().isEmpty()) {
						iInstructorsTable.setColumnVisible(2, false);
					} else {
						iInstructorsTable.setColumnVisible(2, true);
					}
					
					iCourseNbrRegex = result.getCourseNbrRegex();
					iCourseNbrInfo = result.getCourseNbrInfo();
					iCourseOfferingNumberMustBeUnique = result.getCourseOfferingMustBeUnique();
					
					iCourseOfferingNumberUpperCase = result.getCourseOfferingNumberUpperCase();
					
					if (result.getCourseUrlProvider() != null && !result.getCourseUrlProvider().isEmpty()) {
						iPanel.getRowFormatter().setVisible(iCourseUrlProviderLine, true);
					} else {
						iPanel.getRowFormatter().setVisible(iCourseUrlProviderLine, false);
					}
					
					iAllowAlternativeCourseOfferings = result.getAllowAlternativeCourseOfferings();
					
					if (iAllowAlternativeCourseOfferings) {
						if (result.getAltCourseOfferings() == null || result.getAltCourseOfferings().isEmpty()) {
							iPanel.getRowFormatter().setVisible(iAltCourseOfferingLine, false);
						} else {
							iAlternativeCourseOfferings.getWidget().addItem("", "none");
							for (CourseOfferingInterface courseOffering: result.getAltCourseOfferings()) {
								iAlternativeCourseOfferings.getWidget().addItem(courseOffering.getLabel(), courseOffering.getId().toString());
							}
							
							if (iCanAddCourseOffering) {
								iPanel.getRowFormatter().setVisible(iAltCourseOfferingLine, true);
							} else {
								iPanel.getRowFormatter().setVisible(iAltCourseOfferingLine, false);
							}
						}
					}
					
					iCoursesFundingDepartmentsEnabled = result.getCoursesFundingDepartmentsEnabled();
					
					if (iCoursesFundingDepartmentsEnabled) {
						for (DepartmentInterface fundingDepartment: result.getFundingDepartments()) {
							iFundingDepartment.getWidget().addItem(fundingDepartment.getLabel(), fundingDepartment.getId().toString());
						}
						
						if (result.getSubjectAreaEffectiveFundingDept() != null) {
							int altIndex = 0;
							for (int i = 0; i < iFundingDepartment.getWidget().getItemCount(); i++) {
								if (iFundingDepartment.getWidget().getValue(i).equals(result.getSubjectAreaEffectiveFundingDept().toString())) {
									altIndex = i;
									break;
								}
							}
							iFundingDepartment.getWidget().setSelectedIndex(altIndex);
						}

						iPanel.getRowFormatter().setVisible(iFundingDeptLine, true);
					}
					
					for (WaitListInterface waitListItem: result.getWaitLists()) {
						iWaitListing.getWidget().addItem(waitListItem.getLabel(), waitListItem.getValue());
					}
					
					iCanEditExternalIds = result.getCanEditExternalIds();
					iCanShowExternalIds = result.getCanShowExternalIds();
					
					if (iCanEditCourseOffering || iCanAddCourseOffering) {
						if (!iCanEditExternalIds) {
							iExternalId.getWidget().setReadOnly(true);
						} 
					}
					
					if (!iCanEditExternalIds) {
						iPanel.getRowFormatter().setVisible(iExternalIdLine, false);
					}
					
					//Get Override Types
					int overrideCounter = 0;
					for (OverrideTypeInterface overrideType: result.getOverrideTypes()) {
						if (overrideCounter == 0) {
							iOverrideType.setText(overrideType.getReference() + ": " + overrideType.getName());
							iOverrideType.setFormValue(overrideType.getId().toString());
						} else {
							CheckBox additionalOverrideType = new CheckBox();
							additionalOverrideType.setText(overrideType.getReference() + ": " + overrideType.getName());
							additionalOverrideType.setFormValue(overrideType.getId().toString());
							overrideTypesForm.addRow(additionalOverrideType);
						}
						overrideCounter++;
					}
					
					if (overrideCounter > 0) {
						iPanel.getRowFormatter().setVisible(iOverrideTypeLine, true);
					}
				}
			});
		} else {
			//Edit Course Offering
			iAllowDemandCourseOfferings = true;
			
			iPanel.getRowFormatter().setVisible(iCreditTextLine, false);
			
			if (iCanEditCourseOfferingCoordinators) {
				iFundingDepartment.getWidget().setEnabled(true);
			} else {
				iFundingDepartment.getWidget().setEnabled(false);
			}

			if (!iCanEditCourseOffering) {
				iPanel.getRowFormatter().setVisible(iCourseNumberLine, false);
				iPanel.getRowFormatter().setVisible(iConsentTextLine, false);
				iPanel.getRowFormatter().setVisible(iConsentLine, false);
				iPanel.getRowFormatter().setVisible(iCourseDemandsLine, false);
				iPanel.getRowFormatter().setVisible(iCourseDemandsTextLine, false);
				iPanel.getRowFormatter().setVisible(iTitleLine, false);
				iPanel.getRowFormatter().setVisible(iTitleTextLine, false);
				iTitle.getWidget().setReadOnly(true);
				if (!iCanEditCourseOfferingNote) {
					iScheduleNote.getWidget().setReadOnly(true);
					iPanel.getRowFormatter().setVisible(iScheduleNoteLine, false);
					iPanel.getRowFormatter().setVisible(iScheduleNoteTextLine, true);
					
					if (courseOffering.getScheduleBookNote() == null || courseOffering.getScheduleBookNote().isEmpty()) {
						iPanel.getRowFormatter().setVisible(iScheduleNoteLine, false);
						iPanel.getRowFormatter().setVisible(iScheduleNoteTextLine, false);
					}
				}
				iConsent.getWidget().setEnabled(false);
				iCourseDemands.getWidget().setEnabled(false);
				iPanel.getRowFormatter().setVisible(iCreditSectionLine, false);
				
				if (courseOffering.getIsControl() == true) {
					if (courseOffering.getCreditText() != null && !courseOffering.getCreditText().isEmpty()) {
						iPanel.getRowFormatter().setVisible(iCreditTextLine, true);
					}
				}
				
				if (courseOffering.getTitle() == null || courseOffering.getTitle().isEmpty()) {
					iPanel.getRowFormatter().setVisible(iTitleLine, false);
					iPanel.getRowFormatter().setVisible(iTitleTextLine, false);
				} else {
					iPanel.getRowFormatter().setVisible(iTitleTextLine, true);
				}
				
				if (!courseOffering.hasExternalId()) {
					iPanel.getRowFormatter().setVisible(iExternalIdLine, false);
				}

				if (courseOffering.getConsent() == null || courseOffering.getConsent() == -1) {
					iPanel.getRowFormatter().setVisible(iConsentTextLine, false);
				} else {
					iPanel.getRowFormatter().setVisible(iConsentTextLine, true);
				}
				
				if (courseOffering.getDemandOfferingId() == null) {
					iPanel.getRowFormatter().setVisible(iCourseDemandsTextLine, false);
				} else {
					iPanel.getRowFormatter().setVisible(iCourseDemandsTextLine, true);
				}
				
				iExternalId.getWidget().setReadOnly(true);
			}

			iCreditText.setText(courseOffering.getCreditText());
			iScheduleNoteText.setText(courseOffering.getScheduleBookNote());

			if (courseOffering.getIsControl() == true) {
				if (!iCanEditCourseOffering && !iCanEditCourseOfferingCoordinators) {
					//We should hide this if there are no instructors saved
					iPanel.getRowFormatter().setVisible(iInstructorPanelLine, false);
					
					if (courseOffering.getCoordinators().size() > 0) {
						for (int i = 1; i < iInstructorsTable.getRowCount(); i++) {
							UniTimeWidget<ListBox> instructorDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 0);
							UniTimeWidget<UniTimeTextBox> shareTextBox = (UniTimeWidget<UniTimeTextBox>) iInstructorsTable.getWidget(i, 1);
							UniTimeWidget<ListBox> responsibilitiesDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 2);
							Button deleteButton = (Button) iInstructorsTable.getWidget(i, 3);
							
							instructorDropdown.getWidget().setEnabled(false);
							shareTextBox.getWidget().setEnabled(false);
							responsibilitiesDropdown.getWidget().setEnabled(false);
							deleteButton.setEnabled(false);
				        }
						
						iAddCoordinatorButton.setEnabled(false);
					} else {
						//Hide instructors
						iPanel.getRowFormatter().setVisible(iInstructorPanelLine, false);
					}
				}
				
				if (!iCanEditCourseOffering) {					
					iPanel.getRowFormatter().setVisible(iReservationOnlyLine, false);
					iPanel.getRowFormatter().setVisible(iReservationOnlyTextLine, false);
					iPanel.getRowFormatter().setVisible(iNewEnrollmentDeadlineLine, false);
					iPanel.getRowFormatter().setVisible(iNewEnrollmentDeadlineTextLine, false);
					iPanel.getRowFormatter().setVisible(iClassChangesDeadlineLine, false);
					iPanel.getRowFormatter().setVisible(iClassChangesDeadlineTextLine, false);
					iPanel.getRowFormatter().setVisible(iCourseDropDeadlineLine, false);
					iPanel.getRowFormatter().setVisible(iCourseDropDeadlineTextLine, false);
					iByReservationOnly.setEnabled(false);
					iNewEnrollmentDeadline.setEnabled(false);
					iClassChangesDeadline.setEnabled(false);
					iCourseDropDeadline.setEnabled(false);
					
					if (courseOffering.getByReservationOnly() == true) {
						iPanel.getRowFormatter().setVisible(iReservationOnlyTextLine, true);
					}
					
					if (courseOffering.getLastWeekToEnroll() != null && courseOffering.getLastWeekToEnroll() >= 0) {
						iPanel.getRowFormatter().setVisible(iNewEnrollmentDeadlineTextLine, true);
					}
					
					if (courseOffering.getLastWeekToChange() != null && courseOffering.getLastWeekToChange() >= 0) {
						iPanel.getRowFormatter().setVisible(iClassChangesDeadlineTextLine, true);
					}
					
					if (courseOffering.getLastWeekToDrop() != null && courseOffering.getLastWeekToDrop() >= 0) {
						iPanel.getRowFormatter().setVisible(iCourseDropDeadlineTextLine, true);
					} else {
						iPanel.getRowFormatter().setVisible(iDescEnrollmentDeadlinesLine, false);
					}
					
					iPanel.getRowFormatter().setVisible(iRequestsNotesLine, false);
					iRequestsNotes.getWidget().setEnabled(false);
					
					if (courseOffering.getNotes() != null && !courseOffering.getNotes().isEmpty()) {
						iPanel.getRowFormatter().setVisible(iRequestNotesTextLine, true);
					} else {
						//Hide notes
						iPanel.getRowFormatter().setVisible(iRequestNotesTextLine, false);
					}
				}
			} else {
				iPanel.getRowFormatter().setVisible(iInstructorPanelLine, false);

				iPanel.getRowFormatter().setVisible(iReservationOnlyLine, false);

				iPanel.getRowFormatter().setVisible(iNewEnrollmentDeadlineLine, false);
				iPanel.getRowFormatter().setVisible(iClassChangesDeadlineLine, false);
				iPanel.getRowFormatter().setVisible(iCourseDropDeadlineLine, false);
				iPanel.getRowFormatter().setVisible(iDescEnrollmentDeadlinesLine, false);

				iPanel.getRowFormatter().setVisible(iRequestsNotesLine, false);
				
				//Change Waitlisting display
				iPanel.getRowFormatter().setVisible(iWaitListingLine, false);
				iPanel.getRowFormatter().setVisible(iWaitListingTextLine, true);
			}

			iTitleAndButtons.setEnabled("save", false);
			iTitleAndButtons.setEnabled("update", true);
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageEditCourseOffering());
			iTitleAndButtons.setEnabled("back", true);
			courseAbbr = courseOffering.getSubjectAreaAbbv() + ' ' + courseOffering.getCourseNbr();
			iTitleAndButtons.setHeaderTitle(courseAbbr);
			iCourseNumber.getWidget().setText(courseOffering.getCourseNbr());
			iTitle.getWidget().setValue(courseOffering.getTitle());
			iTitleText.setText(courseOffering.getTitle());
			iExternalId.getWidget().setValue(courseOffering.getExternalId());
			iScheduleNote.getWidget().setValue(courseOffering.getScheduleBookNote());

			//Credit
			
			if (courseOffering.getUnits() != null) {
				iUnits.getWidget().setValue(courseOffering.getUnits().toString());
			}
			if (courseOffering.getMaxUnits() != null) {
				iMaxUnits.getWidget().setValue(courseOffering.getMaxUnits().toString());
			}
			iFractional.setValue(courseOffering.getFractionalIncrementsAllowed());
			
			iCourseUrlProvider.populate(iSubjectArea, iCourseNumber, courseOffering.getSubjectAreaId());
			iCourseUrlProvider.setVisible(true);
			
			//Coordinators
			
			ClickHandler deleteCoordinator = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					AriaButton clickedDeleteButton = (AriaButton) event.getSource();
					String clickedButtonId = clickedDeleteButton.getElement().getId();
					Integer clickedButtonIdInteger = Integer.valueOf(clickedButtonId);

					int rowCount = iInstructorsTable.getRowCount();
					for (int i = 1; i < rowCount; i++) {
						AriaButton deleteButton = (AriaButton) iInstructorsTable.getWidget(i, 3);
						String buttonId = deleteButton.getElement().getId();
						Integer buttonIdInteger = Integer.valueOf(buttonId);
						if (buttonIdInteger.equals(clickedButtonIdInteger)) {
							iInstructorsTable.removeRow(i);
							break;
						}
					}
				}
			};

			iByReservationOnly.setValue(courseOffering.getByReservationOnly());
			if (courseOffering.getLastWeekToEnroll() != null) {
				iNewEnrollmentDeadline.setValue(courseOffering.getLastWeekToEnroll().toString());
				iNewEnrollmentDeadlineText.setText(MESSAGES.textLastWeekEnrollment(courseOffering.getLastWeekToEnroll().toString()));
			}
			if (courseOffering.getLastWeekToChange() != null) {
				iClassChangesDeadline.setValue(courseOffering.getLastWeekToChange().toString());
				iClassChangesDeadlineText.setText(MESSAGES.textLastWeekChange(courseOffering.getLastWeekToChange().toString()));
			}
			if (courseOffering.getLastWeekToDrop() != null) {
				iCourseDropDeadline.setValue(courseOffering.getLastWeekToDrop().toString());
				iCourseDropDeadlineText.setText(MESSAGES.textLastWeekDrop(courseOffering.getLastWeekToDrop().toString()));
			}
			iRequestsNotes.getWidget().setValue(courseOffering.getNotes());
			iRequestNotesText.setText(courseOffering.getNotes());
			
			if (courseOffering.getCatalogLinkLabel() != null) {
				iCatalogLinkLabel.setHref(courseOffering.getCatalogLinkLocation());
				iCatalogLinkLabel.setHTML(courseOffering.getCatalogLinkLabel());
				iPanel.getRowFormatter().setVisible(iCatalogLinkLabelLine, true);
			}

			RPC.execute(new CourseOfferingPropertiesRequest(true, courseOffering.getSubjectAreaId(), courseOffering.getId().toString()), new AsyncCallback<CourseOfferingPropertiesInterface>() {
				@Override
				public void onFailure(Throwable caught) {
					handleError(caught);
				}
				@Override
				public void onSuccess(CourseOfferingPropertiesInterface result) {
					if (!result.getCourseTypes().isEmpty()) {
						iCourseType.getWidget().addItem(MESSAGES.itemSelect(), "");
						for (CourseTypeInterface courseType: result.getCourseTypes()) {
							iCourseType.getWidget().addItem(courseType.getLabel(), courseType.getId().toString());
						}
					} else {
						//Hide the courseTypes dropdown
						iPanel.getRowFormatter().setVisible(iCourseTypeLine, false);
					}
					
					if (courseOffering.getCourseTypeId() != null) {
						int altIndex = -1;
						for (int i = 0; i < iCourseType.getWidget().getItemCount(); i++) {
							if (iCourseType.getWidget().getValue(i).equals(courseOffering.getCourseTypeId().toString())) {
								altIndex = i;
								break;
							}
						}
						iCourseType.getWidget().setSelectedIndex(altIndex);
					}
					
					if (!iCanEditCourseOffering) {
						if (courseOffering.getCourseTypeId() != null) {
							iCourseType.getWidget().setEnabled(false);
						} else {
							iPanel.getRowFormatter().setVisible(iCourseTypeLine, false);
						}
					}
					
					if (courseOffering.getWaitList() == null) {
						if (result.getWaitListDefault()) {
							iWaitListingText.setText(MESSAGES.waitListDefaultEnabled());
							waitListingImage.setTitle(MESSAGES.descWaitListEnabled());
						} else {
							waitListingImage.setUrl(RESOURCES.cancel().getSafeUri());
							waitListingImage.setTitle(MESSAGES.descWaitListDisabled());
							iWaitListingText.setText(MESSAGES.waitListDefaultDisabled());
						}
					} else if (courseOffering.getWaitList()) {
						iWaitListingText.setText(MESSAGES.waitListEnabled());
						waitListingImage.setTitle(MESSAGES.descWaitListEnabled());
					} else {
						waitListingImage.setUrl(RESOURCES.cancel().getSafeUri());
						waitListingImage.setTitle(MESSAGES.descWaitListDisabled());
						iWaitListingText.setText(MESSAGES.waitListDisabled());
					}
					
					iPrefRowsAdded = result.getPrefRowsAdded();
					constructInstructorsTable();
					
					iCourseDropDeadlineNote.setText(MESSAGES.hintCourseDropDeadline(result.getWkDropDefault().toString()));
					iNewEnrollmentDeadlineNote.setText(MESSAGES.hintNewEnrollmentDeadline(result.getWkEnrollDefault().toString()));
					iClassChangesDeadlineNote.setText(MESSAGES.hintClassChangesDeadline(result.getWkChangeDefault().toString()));
					iDescEnrollmentDeadlinesNote.setText(MESSAGES.descriptionEnrollmentDeadlines(result.getWeekStartDayOfWeek().toString()));

					iCourseNbrRegex = result.getCourseNbrRegex();
					iCourseNbrInfo = result.getCourseNbrInfo();
					iCourseOfferingNumberMustBeUnique = result.getCourseOfferingMustBeUnique();
					
					iCourseOfferingNumberUpperCase = result.getCourseOfferingNumberUpperCase();
					
					iCredit.getWidget().addItem(MESSAGES.itemSelect(), "select");
					for (CourseCreditFormatInterface courseCreditFormat: result.getCourseCreditFormats()) {
						iCredit.getWidget().addItem(courseCreditFormat.getLabel(), courseCreditFormat.getReference().toString());
					}
					for (CourseCreditTypeInterface courseCreditType: result.getCourseCreditTypes()) {
						iCreditType.getWidget().addItem(courseCreditType.getLabel(), courseCreditType.getId().toString());
					}
					for (CourseCreditUnitTypeInterface courseCreditUnitType: result.getCourseCreditUnitTypes()) {
						iCreditUnitType.getWidget().addItem(courseCreditUnitType.getLabel(), courseCreditUnitType.getId().toString());
					}
					
					for (WaitListInterface waitListItem: result.getWaitLists()) {
						iWaitListing.getWidget().addItem(waitListItem.getLabel(), waitListItem.getValue());
					}
					
					int waitListIndex = 0;
					for (int i = 0; i < iWaitListing.getWidget().getItemCount(); i++) {
						if (courseOffering.getWaitList() != null) {
							if (iWaitListing.getWidget().getValue(i).equals(courseOffering.getWaitList().toString())) {
								waitListIndex = i;
								break;
							}
						}
					}
					iWaitListing.getWidget().setSelectedIndex(waitListIndex);

					if (iAllowDemandCourseOfferings) {
						if (result.getCourseDemands() == null || result.getCourseDemands().isEmpty()) {
							iPanel.getRowFormatter().setVisible(iCourseDemandsLine, false);
						} else {
							iCourseDemands.getWidget().addItem("", "none");
							for (CourseOfferingInterface courseOffering: result.getCourseDemands()) {
								iCourseDemands.getWidget().addItem(courseOffering.getLabel(), courseOffering.getId().toString());
							}
						}
					}

					if (!iCanEditCourseOffering) {
						iCourseDemands.getWidget().setEnabled(false);
					}

					iConsent.getWidget().addItem(MESSAGES.consentNone(), "none");
					for (OfferingConsentTypeInterface offeringConsentType: result.getOfferingConsentTypes()) {
						iConsent.getWidget().addItem(offeringConsentType.getLabel(), offeringConsentType.getId().toString());
					}
					
					if (courseOffering.getConsent() != null && courseOffering.getConsent() != -1) {
						int consentIndex = 0;
						for (int i = 0; i < iConsent.getWidget().getItemCount(); i++) {
							if (iConsent.getWidget().getValue(i).equals(courseOffering.getConsent().toString())) {
								consentIndex = i;
								break;
							}
						}
						iConsent.getWidget().setSelectedIndex(consentIndex);
						iConsentText.setText(courseOffering.getConsentText());
					}

					if (courseOffering.getDemandOfferingId() != null) {
						int demandIndex = -1;
						for (int i = 0; i < iCourseDemands.getWidget().getItemCount(); i++) {
							if (iCourseDemands.getWidget().getValue(i).equals(courseOffering.getDemandOfferingId().toString())) {
								demandIndex = i;
								break;
							}
						}
						iCourseDemands.getWidget().setSelectedIndex(demandIndex);
						iCourseDemandsText.setText(courseOffering.getDemandOfferingText());
					} else {
						if (!iCanEditCourseOffering) {
							iPanel.getRowFormatter().setVisible(iCourseDemandsLine, false);
						}
					}

					if (courseOffering.getCoordinators().size() > 0) {
						int numberOfCoordinators = courseOffering.getCoordinators().size();
						Integer maxId = getMaxId(iInstructorsTable);
						
						int i = 0;
						while (i < numberOfCoordinators) {
							maxId++;
							List<Widget> widgets = new ArrayList<Widget>();
							UniTimeWidget<ListBox> instructorDropdown = new UniTimeWidget<ListBox>(new ListBox());
							UniTimeWidget<UniTimeTextBox> shareTextBox = new UniTimeWidget<UniTimeTextBox>(new UniTimeTextBox(4, ValueBoxBase.TextAlignment.LEFT));
							UniTimeWidget<ListBox> responsibilitiesDropdown = new UniTimeWidget<ListBox>(new ListBox());
							Button deleteButton = new AriaButton(MESSAGES.opGroupDelete());
							deleteButton.addStyleName("unitime-NoPrint");
							deleteButton.getElement().setId(maxId.toString());
							deleteButton.addClickHandler(deleteCoordinator);
							
							widgets.add(instructorDropdown);
							widgets.add(shareTextBox);
							widgets.add(responsibilitiesDropdown);
							widgets.add(deleteButton);
							
							iInstructorsTable.addRow(null, widgets);
							
							i++;
						}
					}
					
					iDefaultTeachingResponsibilityId = result.getDefaultTeachingResponsibilityId();

					for (int i = 1; i < iInstructorsTable.getRowCount(); i++) {
						UniTimeWidget<ListBox> instructorDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 0);
						UniTimeWidget<ListBox> responsibilitiesDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 2);
						
						instructorDropdown.getWidget().addItem("-", "-");
						for (InstructorInterface instructorObject: result.getInstructors()) {
							instructorDropdown.getWidget().addItem(instructorObject.getLabel(), instructorObject.getId().toString());
						}
						
						CoordinatorInterface coordinator = null;
						
						if (courseOffering.getCoordinators().size() >= i) {
							coordinator = courseOffering.getCoordinators().get(i-1);
						}
						
						if (iDefaultTeachingResponsibilityId == null || iDefaultTeachingResponsibilityId.isEmpty() || (coordinator != null && (coordinator.getResponsibilityId() == null || coordinator.getResponsibilityId().isEmpty()))) {
							responsibilitiesDropdown.getWidget().addItem("-", "");
						}
						for (ResponsibilityInterface responsibilityObject: result.getResponsibilities()) {
							responsibilitiesDropdown.getWidget().addItem(responsibilityObject.getLabel(), responsibilityObject.getId().toString());
						}
			        }
					
					//Set instructors if necessary
					for (int i = 1; i < iInstructorsTable.getRowCount(); i++) {
						UniTimeWidget<ListBox> instructorDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 0);
						UniTimeWidget<UniTimeTextBox> shareTextBox = (UniTimeWidget<UniTimeTextBox>) iInstructorsTable.getWidget(i, 1);
						UniTimeWidget<ListBox> responsibilitiesDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 2);

						if (courseOffering.getCoordinators().size() < i && (iDefaultTeachingResponsibilityId == null || iDefaultTeachingResponsibilityId.isEmpty())) {
							break;
						}
						
						if (courseOffering.getCoordinators().size() >= i) {
							CoordinatorInterface coordinator = courseOffering.getCoordinators().get(i-1);

							int instructorDropdownIndex = -1;
							for (int j = 1; j < instructorDropdown.getWidget().getItemCount(); j++) {
								if (instructorDropdown.getWidget().getValue(j).equals(coordinator.getInstructorId())) {
									instructorDropdownIndex = j;
									break;
								}
							}
							
							instructorDropdown.getWidget().setSelectedIndex(instructorDropdownIndex);
							
							shareTextBox.getWidget().setValue(coordinator.getPercShare());

							int responsibilitiesDropdownIndex = 0;
							for (int j = 1; j < responsibilitiesDropdown.getWidget().getItemCount(); j++) {
								if (responsibilitiesDropdown.getWidget().getValue(j).equals(coordinator.getResponsibilityId())) {
									responsibilitiesDropdownIndex = j;
									break;
								}
							}
							
							responsibilitiesDropdown.getWidget().setSelectedIndex(responsibilitiesDropdownIndex);
						} else if (iDefaultTeachingResponsibilityId != null && !iDefaultTeachingResponsibilityId.isEmpty()) {
							int responsibilitiesDropdownIndex = 0;
							for (int j = 1; j < responsibilitiesDropdown.getWidget().getItemCount(); j++) {
								if (responsibilitiesDropdown.getWidget().getValue(j).equals(iDefaultTeachingResponsibilityId)) {
									responsibilitiesDropdownIndex = j;
									break;
								}
							}
							responsibilitiesDropdown.getWidget().setSelectedIndex(responsibilitiesDropdownIndex);
						}
			        }
					
					if (result.getResponsibilities() == null || result.getResponsibilities().isEmpty()) {
						iInstructorsTable.setColumnVisible(2, false);
					} else {
						iInstructorsTable.setColumnVisible(2, true);
					}

					if (courseOffering.getIsControl() == true) {
						if (!iCanEditCourseOffering && !iCanEditCourseOfferingCoordinators) {
							if (courseOffering.getCoordinators().size() > 0) {
								for (int i = 1; i < iInstructorsTable.getRowCount(); i++) {
									UniTimeWidget<ListBox> instructorDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 0);
									UniTimeWidget<UniTimeTextBox> shareTextBox = (UniTimeWidget<UniTimeTextBox>) iInstructorsTable.getWidget(i, 1);
									UniTimeWidget<ListBox> responsibilitiesDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 2);
									Button deleteButton = (Button) iInstructorsTable.getWidget(i, 3);
									
									instructorDropdown.getWidget().setEnabled(false);
									shareTextBox.getWidget().setEnabled(false);
									responsibilitiesDropdown.getWidget().setEnabled(false);
									deleteButton.setEnabled(false);
						        }
								iAddCoordinatorButton.setEnabled(false);
							} 
						}
					}
					
					if (courseOffering.getCreditFormat() != null) {
						int creditIndex = 0;
						for (int i = 0; i < iCredit.getWidget().getItemCount(); i++) {
							if (iCredit.getWidget().getValue(i).equals(courseOffering.getCreditFormat())) {
								creditIndex = i;
								break;
							}
						}
						iCredit.getWidget().setSelectedIndex(creditIndex);
					}

					if (courseOffering.getCreditType() != null) {
						int creditTypeIndex = -1;
						for (int i = 0; i < iCreditType.getWidget().getItemCount(); i++) {
							if (iCreditType.getWidget().getValue(i).equals(courseOffering.getCreditType().toString())) {
								creditTypeIndex = i;
								break;
							}
						}
						iCreditType.getWidget().setSelectedIndex(creditTypeIndex);
					}

					if (courseOffering.getCreditUnitType() != null) {
						int creditUnitTypeIndex = -1;
						for (int i = 0; i < iCreditUnitType.getWidget().getItemCount(); i++) {
							if (iCreditUnitType.getWidget().getValue(i).equals(courseOffering.getCreditUnitType().toString())) {
								creditUnitTypeIndex = i;
								break;
							}
						}
						iCreditUnitType.getWidget().setSelectedIndex(creditUnitTypeIndex);
					}

					if (courseOffering.getCreditFormat() != null) {
						toggleCreditSection();
					}
					
					iAllowAlternativeCourseOfferings = result.getAllowAlternativeCourseOfferings();
					if (iAllowAlternativeCourseOfferings) {
						if (result.getAltCourseOfferings() == null || result.getAltCourseOfferings().isEmpty()) {
							iPanel.getRowFormatter().setVisible(iAltCourseOfferingLine, false);
						} else {
							iAlternativeCourseOfferings.getWidget().addItem("", "none");
							for (CourseOfferingInterface courseOffering: result.getAltCourseOfferings()) {
								iAlternativeCourseOfferings.getWidget().addItem(courseOffering.getLabel(), courseOffering.getId().toString());
							}

							if (courseOffering.getAlternativeCourseOfferingId() != null) {
								int altIndex = -1;
								for (int i = 0; i < iAlternativeCourseOfferings.getWidget().getItemCount(); i++) {
									if (iAlternativeCourseOfferings.getWidget().getValue(i).equals(courseOffering.getAlternativeCourseOfferingId().toString())) {
										altIndex = i;
										break;
									}
								}
								iAlternativeCourseOfferings.getWidget().setSelectedIndex(altIndex);
							}
							
							if (iCanEditCourseOffering) {
								iPanel.getRowFormatter().setVisible(iAltCourseOfferingLine, true);
							} else {
								iPanel.getRowFormatter().setVisible(iAltCourseOfferingLine, false);
							}
						}
					}
					
					if (result.getCourseUrlProvider() != null && !result.getCourseUrlProvider().isEmpty()) {
						iPanel.getRowFormatter().setVisible(iCourseUrlProviderLine, true);
					} else {
						iPanel.getRowFormatter().setVisible(iCourseUrlProviderLine, false);
					}
					
					iCoursesFundingDepartmentsEnabled = result.getCoursesFundingDepartmentsEnabled();
					
					if (iCoursesFundingDepartmentsEnabled) {
						for (DepartmentInterface fundingDepartment: result.getFundingDepartments()) {
							iFundingDepartment.getWidget().addItem(fundingDepartment.getLabel(), fundingDepartment.getId().toString());
						}
						
						if (courseOffering.getFundingDepartmentId() != null) {
							int altIndex = 0;
							for (int i = 0; i < iFundingDepartment.getWidget().getItemCount(); i++) {
								if (iFundingDepartment.getWidget().getValue(i).equals(courseOffering.getFundingDepartmentId().toString())) {
									altIndex = i;
									break;
								}
							}
							iFundingDepartment.getWidget().setSelectedIndex(altIndex);
						} else if (courseOffering.getEffectiveFundingDepartmentId() != null) {
							int altIndex = 0;
							for (int i = 0; i < iFundingDepartment.getWidget().getItemCount(); i++) {
								if (iFundingDepartment.getWidget().getValue(i).equals(courseOffering.getEffectiveFundingDepartmentId().toString())) {
									altIndex = i;
									break;
								}
							}
							iFundingDepartment.getWidget().setSelectedIndex(altIndex);
						}
						
						iPanel.getRowFormatter().setVisible(iFundingDeptLine, true);
					}
					
					iCanEditExternalIds = result.getCanEditExternalIds();
					iCanShowExternalIds = result.getCanShowExternalIds();
					
					if (iCanEditCourseOffering || iCanAddCourseOffering) {
						if (!iCanEditExternalIds) {
							iExternalId.getWidget().setReadOnly(true);
						} 
					}
					
					if (!iCanEditCourseOffering && (courseOffering.getExternalId() != null) && !courseOffering.getExternalId().isEmpty() && iCanShowExternalIds) {
						iPanel.getRowFormatter().setVisible(iExternalIdLine, true);
					}
					
					if (!iCanEditExternalIds && !iCanShowExternalIds) {
						iPanel.getRowFormatter().setVisible(iExternalIdLine, false);
					}

					//Get Override Types
					int overrideCounter = 0;
					for (OverrideTypeInterface overrideType: result.getOverrideTypes()) {
						if (overrideCounter == 0) {
							iOverrideType.setText(overrideType.getReference() + ": " + overrideType.getName());
							iOverrideType.setFormValue(overrideType.getId().toString());
							if (courseOffering.getCourseOverrides().contains(overrideType.getId().toString())) {
								iOverrideType.setValue(true);
							}
						} else {
							CheckBox additionalOverrideType = new CheckBox();
							additionalOverrideType.setText(overrideType.getReference() + ": " + overrideType.getName());
							additionalOverrideType.setFormValue(overrideType.getId().toString());
							overrideTypesForm.addRow(additionalOverrideType);
							if (courseOffering.getCourseOverrides().contains(overrideType.getId().toString())) {
								additionalOverrideType.setValue(true);
							}
						}
						overrideCounter++;
					}
					
					if (overrideCounter > 0) {
						iPanel.getRowFormatter().setVisible(iOverrideTypeLine, true);
					}
				}
			});
		}
	}
	
	private void handleError(Throwable caught) {
		LoadingWidget.getInstance().hide();
		iTitleAndButtons.setErrorMessage(MESSAGES.failedCreate(MESSAGES.objectCourseOffering(), caught.getMessage()));
		UniTimeNotifications.error(MESSAGES.failedCreate(MESSAGES.objectCourseOffering(), caught.getMessage()), caught);
		ToolBox.checkAccess(caught);
	}
	
	public CourseOfferingInterface getValue() {
		iCourseOffering.setTitle(iTitle.getWidget().getText());
		iCourseOffering.setScheduleBookNote(iScheduleNote.getWidget().getValue());
		Long subjectAreaId;
		
		if (iIsAdd) {
			subjectAreaId = new Long(iSubjectArea.getWidget().getSelectedValue());
			iCourseOffering.setSubjectAreaId(subjectAreaId);
		}
		
		iCourseOffering.setByReservationOnly(iByReservationOnly.getValue());

		try {
			iCourseOffering.setLastWeekToEnroll(Integer.parseInt(iNewEnrollmentDeadline.getValue()));
		} catch (Exception e) {
			iCourseOffering.setLastWeekToEnroll(null);
		}

		try {
			iCourseOffering.setLastWeekToChange(Integer.parseInt(iClassChangesDeadline.getValue()));
		} catch (Exception e) {
			iCourseOffering.setLastWeekToChange(null);
		}

		try {
			iCourseOffering.setLastWeekToDrop(Integer.parseInt(iCourseDropDeadline.getValue()));
		} catch (Exception e) {
			iCourseOffering.setLastWeekToDrop(null);
		}
		
		iCourseOffering.setNotes(iRequestsNotes.getWidget().getValue());
		
		if (iConsent.getWidget().getSelectedValue() != null && !iConsent.getWidget().getSelectedValue().equals("none")) {
			iCourseOffering.setConsent(new Long(iConsent.getWidget().getSelectedValue()));
		} else {
			iCourseOffering.setConsent(null);
		}
		
		if (iCredit.getWidget().getSelectedValue() != null && !iCredit.getWidget().getSelectedValue().equals("select")) {
			iCourseOffering.setCreditFormat(iCredit.getWidget().getSelectedValue());
			iCourseOffering.setCreditType(new Long(iCreditType.getWidget().getSelectedValue()));
			iCourseOffering.setCreditUnitType(new Long(iCreditUnitType.getWidget().getSelectedValue()));
			try {
				iCourseOffering.setUnits(iUnits.getWidget().getValue() == null ? 0 : Float.parseFloat(iUnits.getWidget().getValue()));
			} catch (NumberFormatException e) {
				iCourseOffering.setUnits((float) 0);
			}
			try {
				iCourseOffering.setMaxUnits(iMaxUnits.getWidget().getValue() == null ? 0 : Float.parseFloat(iMaxUnits.getWidget().getValue()));
			} catch (NumberFormatException e) {
				iCourseOffering.setMaxUnits((float) 0);
			}
			iCourseOffering.setFractionalIncrementsAllowed(iFractional.getValue());
        } else {
        	iCourseOffering.setCreditFormat(null);
        }
		
		iCourseOffering.clearInstructors();
		Integer rowCount = iInstructorsTable.getRowCount();

		for (int i = 1; i < rowCount; i++) {
			UniTimeWidget<ListBox> instructorDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 0);
			UniTimeWidget<UniTimeTextBox> shareTextBox = (UniTimeWidget<UniTimeTextBox>) iInstructorsTable.getWidget(i, 1);
			UniTimeWidget<ListBox> responsibilityDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 2);
			String instructorId = instructorDropdown.getWidget().getSelectedValue();
			String responsibilityId = responsibilityDropdown.getWidget().getSelectedValue();
			String percShare = shareTextBox.getWidget().getValue();

			if (!"-".equals(instructorId)) {
				CoordinatorInterface coordinator = new CoordinatorInterface();
				coordinator.setInstructorId(instructorId);
				coordinator.setPercShare(percShare);
				coordinator.setResponsibilityId(responsibilityId);
				iCourseOffering.addSendCoordinator(coordinator);
			}
		}
		
		if (iCourseDemands.getWidget().getSelectedValue() != null) {
			if ("none".equals(iCourseDemands.getWidget().getSelectedValue())) {
				iCourseOffering.setDemandOfferingId(null);
			} else {
				iCourseOffering.setDemandOfferingId(new Long (iCourseDemands.getWidget().getSelectedValue()));
			}
		}

		if (iAllowAlternativeCourseOfferings) {
			if (iAlternativeCourseOfferings.getWidget().getSelectedValue() != null) {
				if ("none".equals(iAlternativeCourseOfferings.getWidget().getSelectedValue())) {
					iCourseOffering.setAlternativeCourseOfferingId(null);
				} else {
					iCourseOffering.setAlternativeCourseOfferingId(new Long (iAlternativeCourseOfferings.getWidget().getSelectedValue()));
				}
			}
		}
		
		if (iCoursesFundingDepartmentsEnabled) {
			if (iFundingDepartment.getWidget().getSelectedValue() != null) {
				if ("none".equals(iFundingDepartment.getWidget().getSelectedValue())) {
					iCourseOffering.setFundingDepartmentId(null);
				} else {
					iCourseOffering.setFundingDepartmentId(new Long (iFundingDepartment.getWidget().getSelectedValue()));
				}
			}
		}
		
		if (iCanEditExternalIds) {
			iCourseOffering.setExternalId(iExternalId.getWidget().getText());
		}
		
		if (iWaitListing.getWidget().getSelectedValue() != null) {
			if (iWaitListing.getWidget().getSelectedValue().isEmpty()) {
				iCourseOffering.setWaitList(null);
			} else {
				iCourseOffering.setWaitList("true".equalsIgnoreCase(iWaitListing.getWidget().getSelectedValue()));
			}
		} else {
			iCourseOffering.setWaitList(null);
		}
		
		if (iCourseType.getWidget().getSelectedValue() != null) {
			if (iCourseType.getWidget().getSelectedValue().isEmpty()) {
				iCourseOffering.setCourseTypeId(null);
			} else {
				iCourseOffering.setCourseTypeId(new Long (iCourseType.getWidget().getSelectedValue()));
			}
		} else {
			iCourseOffering.setCourseTypeId(null);
		}
		
		iCourseOffering.clearCourseOverrides();
		Integer overrideRowCount = overrideTypesForm.getRowCount();

		for (int i = 0; i < overrideRowCount; i++) {			
			CheckBox overrideCheckBox = (CheckBox) overrideTypesForm.getWidget(i, 0);
			if (overrideCheckBox.getValue() == true) {
				String overrideTypeId = overrideCheckBox.getFormValue();
				iCourseOffering.addCourseOverride(overrideTypeId);
			}
		}

		return iCourseOffering;
	}
	
	protected boolean validate(Boolean iIsEdit) {
		boolean ok = true;
		
		String courseNumber = iCourseNumber.getWidget().getText();

		if (courseNumber != null && iCourseOfferingNumberUpperCase) {
			iCourseOffering.setCourseNbr(courseNumber.toUpperCase());
		}

		if (iIsAdd) {
			//Subject Area Check
			String subjectAreaId = iSubjectArea.getWidget().getSelectedValue();
			
			if (subjectAreaId == null || subjectAreaId.equals("0")) {
				iSubjectArea.setErrorHint(MESSAGES.errorSubjectRequired());
				iTitleAndButtons.setErrorMessage(MESSAGES.errorSubjectRequired());
				ok = false;
			}
		}
		
		if (courseNumber == null || courseNumber.trim().length() == 0) {
			iCourseNumber.setErrorHint(MESSAGES.errorCourseNumberIsEmpty());
			if (ok) {
				iTitleAndButtons.setErrorMessage(MESSAGES.errorCourseNumberIsEmpty());
			}
			ok = false;
		} else {
			try { 
				RegExp rx = RegExp.compile(iCourseNbrRegex);
				MatchResult match = rx.exec(iCourseNumber.getWidget().getText());

				if (match == null) {
					iCourseNumber.setErrorHint(iCourseNbrInfo);
					iTitleAndButtons.setErrorMessage(iCourseNbrInfo);
					ok = false;
				}
			}
			catch (Exception e) {
				iCourseNumber.setErrorHint(MESSAGES.errorCourseNumberCannotBeMatched(iCourseNbrRegex,e.getMessage()));
				iTitleAndButtons.setErrorMessage(MESSAGES.errorCourseNumberCannotBeMatched(iCourseNbrRegex,e.getMessage()));
				ok = false;
			}
		}
		
		//Instructor Validation
		
		Integer rowCount = iInstructorsTable.getRowCount();

		for (int i = 1; i < rowCount; i++) {
			UniTimeWidget<ListBox> instructorDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 0);
			UniTimeWidget<ListBox> responsibilitiesDropdown = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(i, 2);
			
			String instructorId = instructorDropdown.getWidget().getSelectedValue();
			String responsibilityId = responsibilitiesDropdown.getWidget().getSelectedValue();
			
			if (instructorId.equals("-")) {
				continue;
			}

			for (int j = i + 1; j < rowCount; j++) {
				UniTimeWidget<ListBox> instructorDropdown2 = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(j, 0);
				UniTimeWidget<ListBox> responsibilitiesDropdown2 = (UniTimeWidget<ListBox>) iInstructorsTable.getWidget(j, 2);
				
				String instructorId2 = instructorDropdown2.getWidget().getSelectedValue();
				String responsibilityId2 = responsibilitiesDropdown2.getWidget().getSelectedValue();

				if (instructorId.equals(instructorId2) && responsibilityId.equals(responsibilityId2)) {
					iTitleAndButtons.setErrorMessage(MESSAGES.errorDuplicateCoordinator());
					ok = false;
				}
			}
		}

		return ok;
	}
}
