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
package org.unitime.timetable.gwt.client.reservations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionEvent;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionHandler;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.services.ReservationService;
import org.unitime.timetable.gwt.services.ReservationServiceAsync;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.ReservationException;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface.Course;
import org.unitime.timetable.gwt.shared.ReservationInterface.CourseReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.Area;
import org.unitime.timetable.gwt.shared.ReservationInterface.Areas;
import org.unitime.timetable.gwt.shared.ReservationInterface.Curriculum;
import org.unitime.timetable.gwt.shared.ReservationInterface.DefaultExpirationDates;
import org.unitime.timetable.gwt.shared.ReservationInterface.IdName;
import org.unitime.timetable.gwt.shared.ReservationInterface.LCReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.Offering;
import org.unitime.timetable.gwt.shared.ReservationInterface.ReservationDefaultExpirationDatesRpcRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Tomas Muller
 *
 */
public class ReservationEdit extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	private List<EditFinishedHandler> iEditFinishedHandlers = new ArrayList<EditFinishedHandler>();
	
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iTitleAndButtons;
	private UniTimeWidget<UniTimeTextBox> iLimit;
	private UniTimeWidget<SingleDateSelector> iStartDate;
	private UniTimeWidget<SingleDateSelector> iExpirationDate;
	private RestrictionsTable iRestrictions;
	private HashMap<Long, Area> iAreas = new HashMap<Long, Area>();
	private HashMap<Long, Curriculum> iCurricula = new HashMap<Long, Curriculum>();
	private ListBox iClassifications, iMajors, iMinors, iConcentrations;
	private int iMinorRow = 0, iConcentrationRow = 0;
	private UniTimeWidget<ListBox> iType, iArea, iCourse, iGroup, iCurriculum;
	private UniTimeWidget<TextArea> iStudents;
	private ReservationInterface iReservation;
	private ReservationCourseSelectionBox iCourseBox;
	private Lookup iLookup;
	private DefaultExpirationDates iExpirations = null;
	private UniTimeWidget<TextBox> iStudentFilter;
	
	private final ReservationServiceAsync iReservationService = GWT.create(ReservationService.class);
	private final CurriculaServiceAsync iCurriculaService = GWT.create(CurriculaService.class);
	private int iStartDateLine, iExpirationLine, iReservedSpaceLine, iGroupLine, iCourseLine, iAreaLine, iStudentsLine, iCurriculumLine, iInclusionLine, iFilterLine;
	
	private CheckBox iCanOverlap, iMustBeUsed, iOverLimit, iAlwaysExpired;
	private ListBox iInclusive;
	private int iOverrideLine;
	
	private Offering iOffering = null;
	
	public ReservationEdit(boolean standAlone) {
		if (standAlone) {
			addEditFinishedHandler(new EditFinishedHandler() {
				@Override
				public void onSave(EditFinishedEvent evt) {
					ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.action?io=" + iOffering.getId() + "#reservations");
				}
				
				@Override
				public void onDelete(EditFinishedEvent evt) {
					ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.action?io=" + iOffering.getId() + "#reservations");
				}
				
				@Override
				public void onBack(EditFinishedEvent evt) {
					ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.action?io=" + iOffering.getId() + "#reservations");
				}

				@Override
				public void onFailure(Throwable caught) {
					new Timer() {
						@Override
						public void run() {
							ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.action?io=" + iOffering.getId() + "#reservations");
						}
					}.schedule(5000);
				}

				@Override
				public boolean hasNext(EditFinishedEvent evt) {
					String ids = Window.Location.getParameter("reservations");
					if (ids != null) {
						String[] arr = ids.split(",");
						for (int i = 0; i < arr.length - 1; i++) {
							if (evt.getReservationId().toString().equals(arr[i])) return true;
						}
					}
					return false;
				}

				@Override
				public boolean hasPrevious(EditFinishedEvent evt) {
					String ids = Window.Location.getParameter("reservations");
					if (ids != null) {
						String[] arr = ids.split(",");
						for (int i = 1; i < arr.length; i++) {
							if (evt.getReservationId().toString().equals(arr[i])) return true;
						}
					}
					return false;
				}

				@Override
				public void onNext(EditFinishedEvent evt) {
					String ids = Window.Location.getParameter("reservations");
					if (ids != null) {
						String[] arr = ids.split(",");
						for (int i = 0; i < arr.length - 1; i++) {
							if (evt.getReservationId().toString().equals(arr[i])) {
								load(arr[i + 1]); return;
							}
						}
					}
					onSave(evt);
				}

				@Override
				public void onPrevious(EditFinishedEvent evt) {
					String ids = Window.Location.getParameter("reservations");
					if (ids != null) {
						String[] arr = ids.split(",");
						for (int i = 1; i < arr.length; i++) {
							if (evt.getReservationId().toString().equals(arr[i])) {
								load(arr[i - 1]); return;
							}
						}
					}
					onSave(evt);
				}
			});
			
			if (Window.Location.getParameter("id") == null && Window.Location.getParameter("offering") == null) {
				throw new ReservationException(MESSAGES.errorReservationOrOfferingIdNotProvided());
			}
		}
		
		iPanel = new SimpleForm();
		initWidget(iPanel);

		iTitleAndButtons = new UniTimeHeaderPanel(MESSAGES.sectReservationDetails());
		iTitleAndButtons.addButton("save", MESSAGES.buttonSave(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iTitleAndButtons.clearMessage();
				ReservationInterface r = validate();
				if (r == null) {
					iTitleAndButtons.setErrorMessage(MESSAGES.failedValidationCheckForm());
				} else {
					LoadingWidget.getInstance().show(MESSAGES.waitSavingReservation());
					iReservationService.save(r, new AsyncCallback<Long>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iTitleAndButtons.setErrorMessage(caught.getMessage());
						}
						@Override
						public void onSuccess(Long result) {
							LoadingWidget.getInstance().hide();
							EditFinishedEvent e = new EditFinishedEvent(result);
							for (EditFinishedHandler h: iEditFinishedHandlers)
								h.onSave(e);
						}
					});
				}
			}
		});
		iTitleAndButtons.addButton("previous", MESSAGES.buttonPrevious(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iTitleAndButtons.clearMessage();
				ReservationInterface r = validate();
				if (r == null) {
					iTitleAndButtons.setErrorMessage(MESSAGES.failedValidationCheckForm());
				} else {
					LoadingWidget.getInstance().show(MESSAGES.waitSavingReservation());
					iReservationService.save(r, new AsyncCallback<Long>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iTitleAndButtons.setErrorMessage(caught.getMessage());
						}
						@Override
						public void onSuccess(Long result) {
							LoadingWidget.getInstance().hide();
							EditFinishedEvent e = new EditFinishedEvent(result);
							for (EditFinishedHandler h: iEditFinishedHandlers)
								h.onPrevious(e);
						}
					});
				}
			}
		});
		iTitleAndButtons.setEnabled("previous", false);
		iTitleAndButtons.addButton("next", MESSAGES.buttonNext(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iTitleAndButtons.clearMessage();
				ReservationInterface r = validate();
				if (r == null) {
					iTitleAndButtons.setErrorMessage(MESSAGES.failedValidationCheckForm());
				} else {
					LoadingWidget.getInstance().show(MESSAGES.waitSavingReservation());
					iReservationService.save(r, new AsyncCallback<Long>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iTitleAndButtons.setErrorMessage(caught.getMessage());
						}
						@Override
						public void onSuccess(Long result) {
							LoadingWidget.getInstance().hide();
							EditFinishedEvent e = new EditFinishedEvent(result);
							for (EditFinishedHandler h: iEditFinishedHandlers)
								h.onNext(e);
						}
					});
				}
			}
		});
		iTitleAndButtons.setEnabled("next", false);
		iTitleAndButtons.addButton("delete", MESSAGES.buttonDelete(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iReservation == null) {
					iTitleAndButtons.setErrorMessage(MESSAGES.errorCannotDeleteUnsavedReservation());
				} else {
					if (!Window.confirm(MESSAGES.confirmDeleteReservation())) return;
					LoadingWidget.getInstance().show(MESSAGES.waitDeletingReservation());
					iReservationService.delete(iReservation.getId(), new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iTitleAndButtons.setErrorMessage(caught.getMessage());
						}
						@Override
						public void onSuccess(Boolean result) {
							LoadingWidget.getInstance().hide();
							EditFinishedEvent e = new EditFinishedEvent(iReservation == null ? null : iReservation.getId());
							for (EditFinishedHandler h: iEditFinishedHandlers)
								h.onDelete(e);
						}
					});
				}
			}
		});
		iTitleAndButtons.setEnabled("delete", false);
		iTitleAndButtons.addButton("back", MESSAGES.buttonBack(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				EditFinishedEvent e = new EditFinishedEvent(iReservation == null ? null : iReservation.getId());
				for (EditFinishedHandler h: iEditFinishedHandlers)
					h.onBack(e);
			}
		});
		
		iPanel.addHeaderRow(iTitleAndButtons);
		
		iCourseBox = new ReservationCourseSelectionBox();
		iCourseBox.setWidth("130px");
		iCurriculaService.getApplicationProperty(new String[] {"unitime.curricula.courseWidth"}, new AsyncCallback<String[]>() {
			@Override
			public void onSuccess(String[] ret) {
				if (ret != null && ret.length >= 1 && ret[0] != null && ret[0].length() > 0) {
					iCourseBox.setWidth(ret[0]);
				}
			}
			@Override
			public void onFailure(Throwable e) {}
		});
		iPanel.addRow(MESSAGES.propInstructionalOffering(), iCourseBox);
		iPanel.getCellFormatter().getElement(iPanel.getRowCount() - 1, 0).getStyle().setWidth(100, Unit.PX);
		
		iType = new UniTimeWidget<ListBox>(new ListBox());
		iType.getWidget().setStyleName("unitime-TextBox");
		iType.getWidget().addItem(MESSAGES.itemSelect(), "");
		iType.getWidget().addItem(MESSAGES.reservationIndividual(), "individual");
		iType.getWidget().addItem(MESSAGES.reservationStudentGroup(), "group");
		iType.getWidget().addItem(MESSAGES.reservationCurriculum(), "curriculum");
		iType.getWidget().addItem(MESSAGES.reservationCourse(), "course");
		iType.getWidget().addItem(MESSAGES.reservationLearningCommunity(), "lc");
		for (ReservationInterface.OverrideType t: ReservationInterface.OverrideType.values()) {
			if (t.isEditable())
				if (CONSTANTS.reservationOverrideTypeName()[t.ordinal()] != null) {
					iType.getWidget().addItem(CONSTANTS.reservationOverrideTypeName()[t.ordinal()], t.getReference());
				} else {
					iType.getWidget().addItem("Override: " + t.name(), t.getReference());
				}
		}
		iType.getWidget().addItem(MESSAGES.reservationIndividualOverride(), "individual-override");
		iType.getWidget().addItem(MESSAGES.reservationStudentGroupOverride(), "group-override");
		iType.getWidget().addItem(MESSAGES.reservationCurriculumOverride(), "curriculum-override");
		iType.getWidget().addItem(MESSAGES.reservationUniversalOverride(), "universal");
		iType.getWidget().setSelectedIndex(0);
		iType.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iType.clearHint();
			}
		});
		iPanel.addRow(MESSAGES.propType(), iType);
		iType.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				typeChanged(true);
			}
		});

		iLimit = new UniTimeWidget<UniTimeTextBox>(new UniTimeTextBox(4, ValueBoxBase.TextAlignment.RIGHT));
		iReservedSpaceLine = iPanel.addRow(MESSAGES.propReservedSpace(), iLimit);
		
		P properties = new P("unitime-OverrideProperties");
		iCanOverlap = new CheckBox(MESSAGES.checkCanOverlap());
		iOverLimit = new CheckBox(MESSAGES.checkCanOverLimit());
		iMustBeUsed = new CheckBox(MESSAGES.checkMustBeUsed());
		iAlwaysExpired = new CheckBox(MESSAGES.checkAllwaysExpired()); iAlwaysExpired.setValue(true);
		properties.add(iCanOverlap);
		properties.add(iOverLimit);
		properties.add(iMustBeUsed);
		properties.add(iAlwaysExpired);
		iAlwaysExpired.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iPanel.getRowFormatter().setVisible(iExpirationLine, !event.getValue());
				iPanel.getRowFormatter().setVisible(iStartDateLine, !event.getValue());
				iPanel.getRowFormatter().setVisible(iInclusionLine, !event.getValue());
			}
		});
		
		iOverrideLine = iPanel.addRow(MESSAGES.propOverrideProperties(), properties);
		
		iStartDate = new UniTimeWidget<SingleDateSelector>(new SingleDateSelector());
		iStartDate.getWidget().addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iStartDate.clearHint();
			}
		});
		iStartDateLine = iPanel.addRow(MESSAGES.propStartDate(), iStartDate);
		
		iExpirationDate = new UniTimeWidget<SingleDateSelector>(new SingleDateSelector());
		iExpirationDate.getWidget().addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iExpirationDate.clearHint();
			}
		});
		iExpirationLine = iPanel.addRow(MESSAGES.propExpirationDate(), iExpirationDate);
		
		iRestrictions = new RestrictionsTable(iLimit);
		iPanel.addRow(MESSAGES.propRestrictions(), iRestrictions);
		iPanel.getCellFormatter().setVerticalAlignment(iPanel.getRowCount() - 1, 0, HasVerticalAlignment.ALIGN_TOP);
		
		iInclusive = new ListBox();
		iInclusive.addItem(MESSAGES.reservationInclusiveNotSet(), "");
		iInclusive.addItem(MESSAGES.reservationInclusiveTrue(), "true");
		iInclusive.addItem(MESSAGES.reservationInclusiveFalse(), "false");
		iInclusionLine = iPanel.addRow(MESSAGES.propInclusive(), iInclusive);
		
		iStudents = new UniTimeWidget<TextArea>(new TextArea());
		iStudents.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iStudents.clearHint();
				iLimit.getWidget().setValue(String.valueOf(iStudents.getWidget().getText().split("\n").length), true);
			}
		});
		iStudents.getWidget().setStyleName("unitime-TextArea");
		iStudents.getWidget().setVisibleLines(10);
		iStudents.getWidget().setCharacterWidth(80);
		VerticalPanel students = new VerticalPanel();
		students.add(iStudents);
		Button lookup = new Button(MESSAGES.buttonLookup());
		lookup.setAccessKey('l');
		iLookup = new Lookup();
		lookup.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iLookup.center();
			}
		});
		iLookup.setOptions("mustHaveExternalId,source=students");
		iLookup.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				PersonInterface student = event.getValue();
				if (student != null) {
					iStudents.getWidget().setValue(iStudents.getWidget().getValue() + (iStudents.getWidget().getValue().isEmpty() ? "" : "\n")
							+ student.getId() + " " + student.getLastName() + ", " + student.getFirstName() + (student.getMiddleName() == null ? "" : " " + student.getMiddleName()), true);
					iLimit.getWidget().setValue(String.valueOf(iStudents.getWidget().getText().split("\n").length), true);
				}
			}
		});
		students.add(lookup);
		students.setCellHorizontalAlignment(lookup, HasHorizontalAlignment.ALIGN_RIGHT);
		iPanel.addRow(MESSAGES.propStudents(), students);
		iPanel.getCellFormatter().setVerticalAlignment(iPanel.getRowCount() - 1, 0, HasVerticalAlignment.ALIGN_TOP);
		iStudentsLine = iPanel.getRowCount() - 1;


		iGroup = new UniTimeWidget<ListBox>(new ListBox());
		iGroup.getWidget().setStyleName("unitime-TextBox");
		iGroup.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iGroup.clearHint();
			}
		});
		iGroup.getWidget().addItem(MESSAGES.itemSelect(), "");
		iGroup.getWidget().setSelectedIndex(0);
		iPanel.addRow(MESSAGES.propStudentGroup(), iGroup);
		iGroupLine = iPanel.getRowCount() - 1;

		iCourse = new UniTimeWidget<ListBox>(new ListBox());
		iCourse.getWidget().setStyleName("unitime-TextBox");
		iCourse.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCourse.clearHint();
				String cid = iCourse.getWidget().getValue(iCourse.getWidget().getSelectedIndex());
				String val = iType.getWidget().getValue(iType.getWidget().getSelectedIndex());
				if ("course".equals(val))
					for (Course course: iOffering.getCourses()) {
						if (course.getId().toString().equals(cid))
							iLimit.getWidget().setValue(course.getLimit() == null ? "" : course.getLimit().toString(), true);
					}
			}
		});
		iPanel.addRow(MESSAGES.propCourse(), iCourse);
		iCourseLine = iPanel.getRowCount() - 1;
		
		iCurriculum = new UniTimeWidget<ListBox>(new ListBox());
		iCurriculum.getWidget().setStyleName("unitime-TextBox");
		iCurriculum.getWidget().addItem(MESSAGES.itemNone(), "");
		iCurriculum.getWidget().setSelectedIndex(0);
		iPanel.addRow(MESSAGES.propCurriculum(), iCurriculum);
		iCurriculumLine = iPanel.getRowCount() - 1;
		
		iArea = new UniTimeWidget<ListBox>(new ListBox());
		iArea.getWidget().setStyleName("unitime-TextBox");
		iArea.getWidget().setWidth("300px");
		iArea.getWidget().setMultipleSelect(true);
		iArea.getWidget().setVisibleItemCount(3);
		iArea.getWidget().setHeight("100px");
		iPanel.addRow(MESSAGES.propAcademicArea(), iArea);
		iAreaLine = iPanel.getRowCount() - 1;
		iClassifications = new ListBox();
		iClassifications.setMultipleSelect(true);
		iClassifications.setWidth("300px");
		iClassifications.setStyleName("unitime-TextBox");
		iClassifications.setVisibleItemCount(3);
		iClassifications.setHeight("100px");
		iPanel.addRow(MESSAGES.propClassifications(), iClassifications);
		iMajors = new ListBox();
		iMajors.setMultipleSelect(true);
		iMajors.setWidth("300px");
		iMajors.setStyleName("unitime-TextBox");
		iMajors.setVisibleItemCount(3);
		iMajors.setHeight("100px");
		iPanel.addRow(MESSAGES.propMajors(), iMajors);
		iConcentrations = new ListBox();
		iConcentrations.setMultipleSelect(true);
		iConcentrations.setWidth("300px");
		iConcentrations.setStyleName("unitime-TextBox");
		iConcentrations.setVisibleItemCount(3);
		iConcentrations.setHeight("100px");
		iConcentrationRow = iPanel.addRow(MESSAGES.propConcentrations(), iConcentrations);
		iMinors = new ListBox();
		iMinors.setMultipleSelect(true);
		iMinors.setWidth("300px");
		iMinors.setStyleName("unitime-TextBox");
		iMinors.setVisibleItemCount(3);
		iMinors.setHeight("100px");
		iMinorRow = iPanel.addRow(MESSAGES.propMinors(), iMinors);
		
		iStudentFilter = new UniTimeWidget<TextBox>(new TextBox());
		iStudentFilter.getWidget().setStyleName("unitime-TextBox");
		iStudentFilter.getWidget().setMaxLength(521);
		iStudentFilter.getWidget().setWidth("500px");
		iPanel.addRow(MESSAGES.propStudentFilter(), iStudentFilter);
		iFilterLine =  iPanel.getRowCount() - 1;

		iCurriculum.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				curriculumChanged();
				iCurriculum.clearHint();
			}
		});
		iArea.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				areaChangedKeepSelection();
				iArea.clearHint();
			}
		});
		iClassifications.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String curId = (iCurriculum.getWidget().getSelectedIndex() < 0 ? "" : iCurriculum.getWidget().getValue(iCurriculum.getWidget().getSelectedIndex()));
				if (!curId.isEmpty()) {
					Curriculum c = iCurricula.get(Long.valueOf(curId));
					int limit = 0;
					boolean noneSelected = true;
					classifcations: for (int i = 0; i < iClassifications.getItemCount(); i++) {
						if (iClassifications.isItemSelected(i)) {
							for (IdName f: c.getClassifications()) {
								if (f.getId().toString().equals(iClassifications.getValue(i))) {
									limit += f.getLimit();
									noneSelected = false;
									continue classifcations;
								}
							}
						}
					}
					if (noneSelected) limit = c.getLimit();
					iLimit.getWidget().setValue(limit == 0 ? "" : String.valueOf(limit), true);
				}
			}
		});
		iMajors.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCurriculum.getWidget().setSelectedIndex(0);
				majorChangedKeepSelection();
			}
		});
		iConcentrations.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCurriculum.getWidget().setSelectedIndex(0);
			}
		});
		iMinors.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCurriculum.getWidget().setSelectedIndex(0);
			}
		});
		iStudentFilter.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iStudentFilter.clearHint();
			}
		});
		
		iPanel.addNotPrintableBottomRow(iTitleAndButtons.clonePanel(null));
		
		if (standAlone) {
			load(Window.Location.getParameter("id"));
		} else {
			new InitializationChain(new InitStudentGroups(), new InitCurricula(), new InitExpirationDates()).execute(new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {
					iTitleAndButtons.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
				}
				@Override
				public void onSuccess(Boolean result) {
				}
			});
		}
		
		iCourseBox.addCourseSelectionHandler(new CourseSelectionHandler() {			
			@Override
			public void onCourseSelection(CourseSelectionEvent event) {
				if (event.getValue() != null && event.getValue().isCourse()) {
					iReservationService.getOfferingByCourseName(event.getValue().getCourseName(), new AsyncCallback<Offering>() {
						@Override
						public void onFailure(Throwable caught) {
							iCourseBox.setError(caught.getMessage());
						}
						@Override
						public void onSuccess(Offering result) {
							setOffering(result);
						}
					});
				} else {
					setOffering(null);
				}
			}
		});
	}
		
	protected void load(String id) {
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingReservation());
		if (id == null) {
			new InitializationChain(new InitStudentGroups(), new InitCurricula(), new InitExpirationDates()).execute(new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {
					loadingFailed(caught);
				}
				@Override
				public void onSuccess(Boolean result) {
					setReservation(null);
					LoadingWidget.getInstance().hide();
				}
			});
		} else {
			iReservationService.getReservation(Long.valueOf(id), new AsyncCallback<ReservationInterface>() {
				public void onFailure(Throwable caught) {
					loadingFailed(caught);
				}
				@Override
				public void onSuccess(final ReservationInterface reservation) {
					if (reservation instanceof ReservationInterface.CurriculumReservation) {
						new InitializationChain(new InitCurricula(), new InitExpirationDates()).execute(new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								loadingFailed(caught);
							}
							@Override
							public void onSuccess(Boolean result) {
								setReservation(reservation);
								LoadingWidget.getInstance().hide();
							}
						});
					} else if (reservation instanceof ReservationInterface.GroupReservation) {
						new InitializationChain(new InitStudentGroups(), new InitExpirationDates()).execute(new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								loadingFailed(caught);
							}

							@Override
							public void onSuccess(Boolean result) {
								setReservation(reservation);
								LoadingWidget.getInstance().hide();
							}
						});
					} else if (reservation instanceof ReservationInterface.LCReservation) {
						new InitializationChain(new InitStudentGroups(), new InitExpirationDates()).execute(new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								loadingFailed(caught);
							}

							@Override
							public void onSuccess(Boolean result) {
								setReservation(reservation);
								LoadingWidget.getInstance().hide();
							}
						});
					} else {
						setReservation(reservation);
						LoadingWidget.getInstance().hide();
					}
				};
			});
		}
	}
	
	private void loadingFailed(Throwable caught) {
		LoadingWidget.getInstance().hide();
		UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()));
		iTitleAndButtons.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
		for (EditFinishedHandler h: iEditFinishedHandlers)
			h.onFailure(caught);
	}
	
	public void setReservation(ReservationInterface r) {
		iReservation = r;
		UniTimePageLabel.getInstance().setPageName(iReservation == null ? MESSAGES.pageAddReservation() : MESSAGES.pageEditReservation());
		iTitleAndButtons.setEnabled("delete", iReservation != null);
		iTitleAndButtons.clearMessage();
		iLimit.clearHint();
		iType.clearHint();
		iAlwaysExpired.setValue(true);
		
		Long offeringId = (iReservation == null ? null : iReservation.getOffering().getId());
		Long courseId =  null;
		if (iReservation != null && iReservation instanceof CourseReservation) {
			CourseReservation cr = (CourseReservation)iReservation;
			courseId = (cr.getCourse() == null ? null : cr.getCourse().getId());
		}
		if (iReservation != null && iReservation instanceof LCReservation) {
			LCReservation lc = (LCReservation)iReservation;
			courseId = (lc.getCourse() == null ? null : lc.getCourse().getId());
		}
		if (offeringId == null) {
			if (Window.Location.getParameter("offering") != null)
				offeringId =  Long.valueOf(Window.Location.getParameter("offering"));
		}

		if (offeringId == null) {
			iOffering = null;
			iCourseBox.setEnabled(true);
			iCourseBox.setValue((RequestedCourse)null, false);
			iCourseBox.setError(null);
			iLimit.getWidget().setValue("", true);
			iExpirationDate.getWidget().setValue(null);
			iStartDate.getWidget().setValue(null);
			iRestrictions.clear();
			iType.getWidget().setSelectedIndex(0);
			iGroup.getWidget().setSelectedIndex(0);
			for (int i = 0; i < iArea.getWidget().getItemCount(); i++)
				iArea.getWidget().setItemSelected(i, false);
			iClassifications.clear();
			iStudents.getWidget().setText("");
			iCourse.getWidget().clear();
			iMajors.clear();
			iConcentrations.clear();
			iMinors.clear();
			iType.setReadOnly(false);
			iCurricula.clear();
			iCurriculum.getWidget().clear();
			iCurriculum.getWidget().addItem(MESSAGES.itemNone(), "");
			iCurriculum.getWidget().setSelectedIndex(0);
			iInclusive.setSelectedIndex(0);
			areaChanged();
			typeChanged(true);
		} else {
			iCourseBox.setEnabled(false);
			LoadingWidget.getInstance().show(MESSAGES.waitLoadingReservation());
			iReservationService.getOffering(offeringId, courseId, new AsyncCallback<Offering>() {
				@Override
				public void onFailure(Throwable caught) {
					loadingFailed(caught);
				}

				@Override
				public void onSuccess(Offering result) {
					setOffering(result);
					LoadingWidget.getInstance().hide();
				}
			});
		}
		
		if (iReservation == null) {
			iTitleAndButtons.setEnabled("previous", false);
			iTitleAndButtons.setEnabled("next", false);
		} else {
			EditFinishedEvent e = new EditFinishedEvent(iReservation.getId());
			for (EditFinishedHandler h: iEditFinishedHandlers) {
				iTitleAndButtons.setEnabled("previous", h.hasPrevious(e));
				iTitleAndButtons.setEnabled("next", h.hasNext(e));
			}
		}
	}
	
	public void setOffering(Offering offering) {
		iOffering = offering;
		iCourseBox.setError(null);
		
		if (iOffering == null) return;
		
		if (iOffering.isUnlockNeeded())
			iCourseBox.setError(MESSAGES.hintOfferingIsLocked(iOffering.getAbbv()));

		if (!iCourseBox.isEnabled()) {
			RequestedCourse rc = new RequestedCourse();
			Course course = iOffering.getControllingCourse();
			if (iReservation != null && iReservation instanceof CourseReservation) {
				CourseReservation cr = (CourseReservation)iReservation;
				if (cr.getCourse() != null) course = cr.getCourse();
			}
			if (iReservation != null && iReservation instanceof LCReservation) {
				LCReservation lc = (LCReservation)iReservation;
				if (lc.getCourse() != null) course = lc.getCourse();
			}
			if (course != null) {
				rc.setCourseId(course.getId());
				rc.setCourseName(course.getAbbv());
			} else {
				rc.setCourseName(iOffering.getAbbv());
			}
			iCourseBox.setValue(rc, false);
		}

		iRestrictions.setOffering(iOffering);
		
		iCourse.getWidget().clear();
		iCourse.getWidget().addItem(MESSAGES.itemSelect(), "");
		for (Course course: iOffering.getCourses()) {
			iCourse.getWidget().addItem(course.getAbbv() + (course.getName() == null || course.getName().isEmpty() ? "" : " - " + course.getName()), course.getId().toString());
		}
		
		typeChanged(true);
		populate();
		
		iCurricula.clear();
		iCurriculum.getWidget().clear();
		iCurriculum.getWidget().addItem(MESSAGES.itemNone(), "");
		iCurriculum.getWidget().setSelectedIndex(0);
		iReservationService.getCurricula(iOffering.getId(), new AsyncCallback<List<Curriculum>>() {
			@Override
			public void onFailure(Throwable caught) {
				loadingFailed(caught);
			}
			@Override
			public void onSuccess(List<Curriculum> result) {
				int nrAreasSelected = 0;
				String lastSelectedAreaId = null;
				for (int i = 0; i < iArea.getWidget().getItemCount(); i++) {
					if (iArea.getWidget().isItemSelected(i)) {
						nrAreasSelected ++;
						lastSelectedAreaId = iArea.getWidget().getValue(i);
					}
				}
				curricula: for (Curriculum curriculum: result) {
					iCurricula.put(curriculum.getId(), curriculum);
					iCurriculum.getWidget().addItem(curriculum.getAbbv() + " - " + curriculum.getName() + " (" + curriculum.getLimit() + ")", curriculum.getId().toString());
					if (nrAreasSelected == 1 && curriculum.getArea().getId().toString().equals(lastSelectedAreaId)) {
						majors: for (int i = 0; i < iMajors.getItemCount(); i++) {
							if (iMajors.isItemSelected(i)) {
								for (IdName m: curriculum.getMajors()) {
									if (m.getId().toString().equals(iMajors.getValue(i))) continue majors; 
								}
								continue curricula;
							} else {
								for (IdName m: curriculum.getMajors()) {
									if (m.getId().toString().equals(iMajors.getValue(i))) continue curricula; 
								}
								continue majors;
							}
						}
						iCurriculum.getWidget().setSelectedIndex(iCurriculum.getWidget().getItemCount() - 1);
					}
				}
				String val = iType.getWidget().getValue(iType.getWidget().getSelectedIndex());
				iPanel.getRowFormatter().setVisible(iCurriculumLine, "curriculum".equals(val) && iCurriculum.getWidget().getItemCount() > 1);
			}
		});
	}
	
	private ReservationInterface.OverrideType getOverrideType(String ref) {
		for (ReservationInterface.OverrideType t: ReservationInterface.OverrideType.values())
			if (t.getReference().equals(ref)) return t;
		return null;
	}
	
	private void typeChanged(boolean setExpiration) {
		String val = iType.getWidget().getValue(iType.getWidget().getSelectedIndex());
		iPanel.getRowFormatter().setVisible(iStudentsLine, "individual".equals(val) || "individual-override".equals(val) || getOverrideType(val) != null);
		iPanel.getRowFormatter().setVisible(iCourseLine, "course".equals(val) || "lc".equals(val));
		iPanel.getRowFormatter().setVisible(iGroupLine, "group".equals(val) || "group-override".equals(val) || "lc".equals(val));
		iPanel.getRowFormatter().setVisible(iCurriculumLine, ("curriculum".equals(val) || "curriculum-override".equals(val)) && iCurriculum.getWidget().getItemCount() > 1);
		iPanel.getRowFormatter().setVisible(iAreaLine, "curriculum".equals(val) || "curriculum-override".equals(val));
		iPanel.getRowFormatter().setVisible(1 + iAreaLine, "curriculum".equals(val) || "curriculum-override".equals(val));
		iPanel.getRowFormatter().setVisible(2 + iAreaLine, "curriculum".equals(val) || "curriculum-override".equals(val));
		iPanel.getRowFormatter().setVisible(iConcentrationRow, ("curriculum".equals(val) || "curriculum-override".equals(val)) && iConcentrations.getItemCount() > 0);
		iPanel.getRowFormatter().setVisible(iMinorRow, ("curriculum".equals(val) || "curriculum-override".equals(val)) && iMinors.getItemCount() > 0);
		iPanel.getRowFormatter().setVisible(iFilterLine, "universal".equals(val));
		iPanel.getRowFormatter().setVisible(iExpirationLine, getOverrideType(val) == null || getOverrideType(val).isCanHaveExpirationDate());
		iPanel.getRowFormatter().setVisible(iStartDateLine, getOverrideType(val) == null || getOverrideType(val).isCanHaveExpirationDate());
		iPanel.getRowFormatter().setVisible(iInclusionLine, getOverrideType(val) == null || getOverrideType(val).isCanHaveExpirationDate());
		if (("individual-override".equals(val) || "group-override".equals(val) || "curriculum-override".equals(val) || "universal".equals(val)) && iAlwaysExpired.getValue()) {
			iPanel.getRowFormatter().setVisible(iExpirationLine, false);
			iPanel.getRowFormatter().setVisible(iStartDateLine, false);
			iPanel.getRowFormatter().setVisible(iInclusionLine, false);
		}
		iPanel.getRowFormatter().setVisible(iReservedSpaceLine, getOverrideType(val) == null || getOverrideType(val).isCanHaveExpirationDate() || !getOverrideType(val).isExpired());
		iPanel.getRowFormatter().setVisible(iOverrideLine, "individual-override".equals(val) || "group-override".equals(val) || "curriculum-override".equals(val) || "universal".equals(val)); 
		if ("course".equals(val)) {
			iLimit.getWidget().setReadOnly(true);
			iLimit.getWidget().setValue("", true);
			if (iOffering != null) {
				String cid = iCourse.getWidget().getValue(iCourse.getWidget().getSelectedIndex());
				for (Course course: iOffering.getCourses()) {
					if (course.getId().toString().equals(cid))
						iLimit.getWidget().setValue(course.getLimit() == null ? "" : course.getLimit().toString(), true);
				}
			}
		} else if ("individual".equals(val) || getOverrideType(val) != null || "individual-override".equals(val)) {
			iLimit.getWidget().setReadOnly(true);
			iLimit.getWidget().setValue(String.valueOf(iStudents.getWidget().getText().split("\n").length), true);
		} else {
			iLimit.getWidget().setReadOnly(false);
		}
		if (setExpiration && iExpirations != null) {
			iExpirationDate.getWidget().setValueInServerTimeZone(iExpirations.getExpirationDate(val));
			iStartDate.getWidget().setValueInServerTimeZone(iExpirations.getStartDate(val));
		}
	}
	
	private void areaChanged() {
		iMajors.clear();
		iMinors.clear();
		iClassifications.clear();
		int nrAreasSelected = 0;
		String lastSelectedArea = "";
		for (int i = 0; i < iArea.getWidget().getItemCount(); i++)
			if (iArea.getWidget().isItemSelected(i)) nrAreasSelected++;
		for (int i = 0; i < iArea.getWidget().getItemCount(); i++) {
			if (iArea.getWidget().isItemSelected(i)) {
				String id = iArea.getWidget().getValue(i);
				Area c = iAreas.get(Long.valueOf(id));
				for (IdName major: c.getMajors()) {
					iMajors.addItem((nrAreasSelected >1 ? c.getAbbv() + "/":"") + major.getAbbv() + " - " + major.getName(), major.getId().toString());
				}
				for (IdName minor: c.getMinors()) {
					iMinors.addItem((nrAreasSelected >1 ? c.getAbbv() + "/":"") + minor.getAbbv() + " - " + minor.getName(), minor.getId().toString());
				}
				if (iClassifications.getItemCount() == 0)
					for (IdName clasf: c.getClassifications()) {
						iClassifications.addItem(clasf.getAbbv() + " - " + clasf.getName(), clasf.getId().toString());
					}
				lastSelectedArea = id;
			}
		}
		iPanel.getRowFormatter().setVisible(iMinorRow, iMinors.getItemCount() > 0);
		String curId = (iCurriculum.getWidget().getSelectedIndex() < 0 ? "" : iCurriculum.getWidget().getValue(iCurriculum.getWidget().getSelectedIndex()));
		if (!curId.isEmpty()) {
			if (nrAreasSelected != 1)
				iCurriculum.getWidget().setSelectedIndex(0);
			else {
				Curriculum c = iCurricula.get(Long.valueOf(curId));
				if (!c.getArea().getId().toString().equals(lastSelectedArea))
					iCurriculum.getWidget().setSelectedIndex(0);
			}
		}
		majorChanged();
	}
	
	protected Area getSelectedArea(Long majorId) {
		for (int i = 0; i < iArea.getWidget().getItemCount(); i++) {
			if (iArea.getWidget().isItemSelected(i)) {
				String id = iArea.getWidget().getValue(i);
				Area c = iAreas.get(Long.valueOf(id));
				if (c.getMajor(majorId) != null)
					return c;
			}
		}
		return null;
	}
	
	private void majorChanged() {
		iConcentrations.clear();
		int nrMajorsSelected = 0;
		for (int i = 0; i < iMajors.getItemCount(); i++)
			if (iMajors.isItemSelected(i)) nrMajorsSelected++;
		for (int i = 0; i < iMajors.getItemCount(); i++) {
			if (iMajors.isItemSelected(i)) {
				Long majorId = Long.valueOf(iMajors.getValue(i));
				Area c = getSelectedArea(majorId);
				IdName m = c.getMajor(majorId);
				for (IdName conc: c.getConcentrations()) {
					if (conc.getParentId().equals(majorId))
						iConcentrations.addItem((nrMajorsSelected > 1 ? c.getAbbv() + "/" + m.getAbbv() + "-":"") + conc.getAbbv() + " - " + conc.getName(), conc.getId().toString());
				}
			}
		}
		iPanel.getRowFormatter().setVisible(iConcentrationRow, iConcentrations.getItemCount() > 0);
	}
	
	private void curriculumChanged() {
		String id = iCurriculum.getWidget().getValue(iCurriculum.getWidget().getSelectedIndex());
		if (!id.isEmpty()) {
			iMajors.clear();
			iMinors.clear();
			iClassifications.clear();
			Curriculum c = iCurricula.get(Long.valueOf(id));
			for (int i = 0; i < iArea.getWidget().getItemCount(); i++) {
				iArea.getWidget().setItemSelected(i, c.getArea().getId().toString().equals(iArea.getWidget().getValue(i)));
			}
			areaChanged();
			for (int i = 0; i < iMajors.getItemCount(); i++) {
				String majorId = iMajors.getValue(i);
				boolean hasMajor = false;
				for (IdName m: c.getMajors())
					if (m.getId().toString().equals(majorId)) { hasMajor = true; break; }
				iMajors.setItemSelected(i, hasMajor);
			}
			for (int i = 0; i < iMinors.getItemCount(); i++) {
				iMinors.setItemSelected(i, false);
			}
			for (int i = 0; i < iClassifications.getItemCount(); i++) {
				String clasfId = iClassifications.getValue(i);
				boolean hasClasf = false;
				for (IdName m: c.getClassifications())
					if (m.getId().toString().equals(clasfId)) { hasClasf = true; break; }
				iClassifications.setItemSelected(i, hasClasf);
			}
			iLimit.getWidget().setValue(c.getLimit() == null ? "" : c.getLimit().toString(), true);
			majorChanged();
		}
	}
	
	private void areaChangedKeepSelection() {
		Set<String> majors = new HashSet<String>();
		for (int i = 0; i < iMajors.getItemCount(); i++)
			if (iMajors.isItemSelected(i)) majors.add(iMajors.getValue(i));
		Set<String> minors = new HashSet<String>();
		for (int i = 0; i < iMinors.getItemCount(); i++)
			if (iMinors.isItemSelected(i)) minors.add(iMinors.getValue(i));
		Set<String> classifications = new HashSet<String>();
		for (int i = 0; i < iClassifications.getItemCount(); i++)
			if (iClassifications.isItemSelected(i)) classifications.add(iClassifications.getValue(i));
		areaChanged();
		if (!majors.isEmpty()) {
			for (int i = 0; i < iMajors.getItemCount(); i++)
				if (majors.contains(iMajors.getValue(i))) iMajors.setItemSelected(i, true);
		}
		if (iMajors.getSelectedIndex() < 0 && iReservation != null && iReservation instanceof ReservationInterface.CurriculumReservation) {
			Areas areas = ((ReservationInterface.CurriculumReservation)iReservation).getCurriculum();
			if (areas != null && areas.getMajors() != null)
				for (IdName id: areas.getMajors())
					for (int i = 0; i < iMajors.getItemCount(); i++)
						if (id.getId().toString().equals(iMajors.getValue(i))) {
							iMajors.setItemSelected(i, true);
							break;
						}
		}
		if (!minors.isEmpty())
			for (int i = 0; i < iMinors.getItemCount(); i++)
				if (minors.contains(iMinors.getValue(i))) iMinors.setItemSelected(i, true);
		if (iMinors.getSelectedIndex() < 0 && iReservation != null && iReservation instanceof ReservationInterface.CurriculumReservation) {
			Areas areas = ((ReservationInterface.CurriculumReservation)iReservation).getCurriculum();
			if (areas != null && areas.getMinors() != null)
				for (IdName id: areas.getMinors())
					for (int i = 0; i < iMinors.getItemCount(); i++)
						if (id.getId().toString().equals(iMinors.getValue(i))) {
							iMinors.setItemSelected(i, true);
							break;
						}
		}
		if (!classifications.isEmpty())
			for (int i = 0; i < iClassifications.getItemCount(); i++)
				if (classifications.contains(iClassifications.getValue(i))) iClassifications.setItemSelected(i, true);
		if (iClassifications.getSelectedIndex() < 0 && iReservation != null && iReservation instanceof ReservationInterface.CurriculumReservation) {
			Areas areas = ((ReservationInterface.CurriculumReservation)iReservation).getCurriculum();
			if (areas != null && areas.getClassifications() != null)
				for (IdName id: areas.getClassifications())
					for (int i = 0; i < iClassifications.getItemCount(); i++)
						if (id.getId().toString().equals(iClassifications.getValue(i))) {
							iClassifications.setItemSelected(i, true);
							break;
						}
		}
		majorChangedKeepSelection();
	}
	
	protected void majorChangedKeepSelection() {
		Set<String> concentrations = new HashSet<String>();
		for (int i = 0; i < iConcentrations.getItemCount(); i++)
			if (iConcentrations.isItemSelected(i)) concentrations.add(iConcentrations.getValue(i));
		majorChanged();
		if (!concentrations.isEmpty())
			for (int i = 0; i < iConcentrations.getItemCount(); i++)
				if (concentrations.contains(iConcentrations.getValue(i))) iConcentrations.setItemSelected(i, true);
		if (iConcentrations.getSelectedIndex() < 0 && iReservation != null && iReservation instanceof ReservationInterface.CurriculumReservation) {
			Areas areas = ((ReservationInterface.CurriculumReservation)iReservation).getCurriculum();
			if (areas != null && areas.getConcentrations() != null)
				for (IdName id: areas.getConcentrations())
					for (int i = 0; i < iConcentrations.getItemCount(); i++)
						if (id.getId().toString().equals(iConcentrations.getValue(i))) {
							iConcentrations.setItemSelected(i, true);
							break;
						}
		}
	}
	
	public void populate() {
		if (iReservation == null) return;
		iLimit.getWidget().setValue(iReservation.getLimit() == null ? "" : iReservation.getLimit().toString());
		iInclusive.setSelectedIndex(iReservation.hasInclusive() ? iReservation.isInclusive() ? 1 : 2 : 0);
		iCanOverlap.setValue(iReservation.isAllowOverlaps());
		iAlwaysExpired.setValue(iReservation.isAlwaysExpired());
		iOverLimit.setValue(iReservation.isOverLimit());
		iMustBeUsed.setValue(iReservation.isMustBeUsed());
		iExpirationDate.getWidget().setValueInServerTimeZone(iReservation.getExpirationDate());
		iStartDate.getWidget().setValueInServerTimeZone(iReservation.getStartDate());
		iRestrictions.populate(iReservation);
		if (iReservation instanceof ReservationInterface.OverrideReservation) {
			select(iType.getWidget(), ((ReservationInterface.OverrideReservation)iReservation).getType().getReference());
			String students = "";
			for (IdName student: ((ReservationInterface.OverrideReservation) iReservation).getStudents())
				students += (students.isEmpty() ? "" : "\n") + student.getAbbv() + " " + student.getName();
			iStudents.getWidget().setText(students);
		} else if (iReservation instanceof ReservationInterface.IndividualReservation) {
			select(iType.getWidget(), iReservation.isOverride() ? "individual-override" : "individual");
			String students = "";
			for (IdName student: ((ReservationInterface.IndividualReservation) iReservation).getStudents())
				students += (students.isEmpty() ? "" : "\n") + student.getAbbv() + " " + student.getName();
			iStudents.getWidget().setText(students);
		} else if (iReservation instanceof ReservationInterface.GroupReservation) {
			select(iType.getWidget(), iReservation.isOverride() ? "group-override" : "group");
			select(iGroup.getWidget(), ((ReservationInterface.GroupReservation) iReservation).getGroup().getId().toString());
		} else if (iReservation instanceof ReservationInterface.CourseReservation) {
			select(iType.getWidget(), "course");
			select(iCourse.getWidget(), ((ReservationInterface.CourseReservation) iReservation).getCourse().getId().toString());
		} else if (iReservation instanceof ReservationInterface.LCReservation) {
			select(iType.getWidget(), "lc");
			select(iGroup.getWidget(), ((ReservationInterface.LCReservation) iReservation).getGroup().getId().toString());
			select(iCourse.getWidget(), ((ReservationInterface.LCReservation) iReservation).getCourse().getId().toString());
		} else if (iReservation instanceof ReservationInterface.CurriculumReservation) {
			select(iType.getWidget(), iReservation.isOverride() ? "curriculum-override" : "curriculum");
			Areas curriculum = ((ReservationInterface.CurriculumReservation) iReservation).getCurriculum();
			for (int i = 0; i < iArea.getWidget().getItemCount(); i++) {
				Long id = Long.valueOf(iArea.getWidget().getValue(i));
				boolean selected = false;
				for (IdName aa: curriculum.getAreas())
					if (aa.getId().equals(id)) selected = true;
				iArea.getWidget().setItemSelected(i, selected);
			}
			areaChanged();
			for (int i = 0; i < iMajors.getItemCount(); i++) {
				Long id = Long.valueOf(iMajors.getValue(i));
				boolean selected = false;
				for (IdName mj: curriculum.getMajors())
					if (mj.getId().equals(id)) selected = true;
				iMajors.setItemSelected(i, selected);
			}
			for (int i = 0; i < iMinors.getItemCount(); i++) {
				Long id = Long.valueOf(iMinors.getValue(i));
				boolean selected = false;
				for (IdName mj: curriculum.getMinors())
					if (mj.getId().equals(id)) selected = true;
				iMinors.setItemSelected(i, selected);
			}
			for (int i = 0; i < iClassifications.getItemCount(); i++) {
				Long id = Long.valueOf(iClassifications.getValue(i));
				boolean selected = false;
				for (IdName clasf: curriculum.getClassifications())
					if (clasf.getId().equals(id)) selected = true;
				iClassifications.setItemSelected(i, selected);
			}
			majorChanged();
			for (int i = 0; i < iConcentrations.getItemCount(); i++) {
				Long id = Long.valueOf(iConcentrations.getValue(i));
				boolean selected = false;
				for (IdName mj: curriculum.getConcentrations())
					if (mj.getId().equals(id)) selected = true;
				iConcentrations.setItemSelected(i, selected);
			}
		} else if (iReservation instanceof ReservationInterface.UniversalReservation) {
			select(iType.getWidget(), "universal");
			iStudentFilter.getWidget().setText(((ReservationInterface.UniversalReservation)iReservation).getFilter());
		}
		typeChanged(false);
		iType.setReadOnly(true);
		iType.setText(iType.getWidget().getItemText(iType.getWidget().getSelectedIndex()));
	}
	
	private void select(ListBox l, String value) {
		for (int i = 0; i < l.getItemCount(); i++) {
			if (l.getValue(i).equals(value)) {
				l.setSelectedIndex(i);
				return;
			}
		}
	}
	
	public ReservationInterface validate() {
		boolean ok = true;
		String type = iType.getWidget().getValue(iType.getWidget().getSelectedIndex());
		if (type.isEmpty()) {
			iType.setErrorHint(MESSAGES.hintReservationTypeNotSelected());
			ok = false;
		} else {
			iType.clearHint();
		}
		ReservationInterface r;
		if ("individual".equals(type) || "individual-override".equals(type) || getOverrideType(type) != null) {
			if ("individual".equals(type) || "individual-override".equals(type)) {
				r = new ReservationInterface.IndividualReservation();
				r.setOverride("individual-override".equals(type));
			} else { 
				r = new ReservationInterface.OverrideReservation(getOverrideType(type));
			}
			if (iStudents.getWidget().getText().isEmpty()) {
				iStudents.setErrorHint(MESSAGES.hintNoStudentsProvided());
				ok = false;
			} else {
				RegExp rx = RegExp.compile("^([a-zA-Z0-9]+)[ ,;\\|]?(.*)$");
				for (String student: iStudents.getWidget().getText().split("\\n")) {
					if (student.trim().isEmpty()) continue;
					try {
						MatchResult m = rx.exec(student.trim());
						if (m == null) {
							iStudents.setErrorHint(MESSAGES.hintLineXIsNotValidStudent(student));
							ok = false;
							break;
						}
						IdName s = new IdName();
						s.setAbbv(m.getGroup(1));
						s.setName(m.getGroupCount() <= 2 ? "" : m.getGroup(2).trim());
						((ReservationInterface.IndividualReservation) r).getStudents().add(s);
						if (((ReservationInterface.IndividualReservation) r).getStudents().isEmpty()) {
							iStudents.setErrorHint(MESSAGES.hintNoStudentsProvided());
							ok = false;
						}
					} catch (Exception e) {
						iStudents.setErrorHint(MESSAGES.hintLineXIsNotValidStudentException(student, e.getMessage()));
						ok = false;
					}
				}
			}
		} else if ("group".equals(type) || "group-override".equals(type)) {
			r = new ReservationInterface.GroupReservation();
			r.setOverride("group-override".equals(type));
			String gid = iGroup.getWidget().getValue(iGroup.getWidget().getSelectedIndex());
			if (gid.isEmpty()) {
				iGroup.setErrorHint(MESSAGES.hintStudentGroupNotProvided());
				ok = false;
			} else {
				IdName group = new IdName();
				group.setId(Long.valueOf(gid));
				group.setName(iGroup.getWidget().getItemText(iGroup.getWidget().getSelectedIndex()));
				((ReservationInterface.GroupReservation) r).setGroup(group);
			}
		} else if ("lc".equals(type)) {
			r = new ReservationInterface.LCReservation();
			String gid = iGroup.getWidget().getValue(iGroup.getWidget().getSelectedIndex());
			if (gid.isEmpty()) {
				iGroup.setErrorHint(MESSAGES.hintStudentGroupNotProvided());
				ok = false;
			} else {
				IdName group = new IdName();
				group.setId(Long.valueOf(gid));
				group.setName(iGroup.getWidget().getItemText(iGroup.getWidget().getSelectedIndex()));
				((ReservationInterface.LCReservation) r).setGroup(group);
			}
			String cid = iCourse.getWidget().getValue(iCourse.getWidget().getSelectedIndex());
			if (cid.isEmpty()) {
				iCourse.setErrorHint(MESSAGES.hintCourseNotProvided());
				ok = false;
			} else {
				Course course = new Course();
				course.setId(Long.valueOf(cid));
				course.setName(iCourse.getWidget().getItemText(iCourse.getWidget().getSelectedIndex()));
				((ReservationInterface.LCReservation) r).setCourse(course);
			}
		} else if ("course".equals(type)) {
			r = new ReservationInterface.CourseReservation();
			String cid = iCourse.getWidget().getValue(iCourse.getWidget().getSelectedIndex());
			if (cid.isEmpty()) {
				iCourse.setErrorHint(MESSAGES.hintCourseNotProvided());
				ok = false;
			} else {
				Course course = new Course();
				course.setId(Long.valueOf(cid));
				course.setName(iCourse.getWidget().getItemText(iCourse.getWidget().getSelectedIndex()));
				((ReservationInterface.CourseReservation) r).setCourse(course);
			}
		} else if ("curriculum".equals(type) || "curriculum-override".equals(type)) {
			r = new ReservationInterface.CurriculumReservation();
			r.setOverride("curriculum-override".equals(type));
			Areas curriculum = new Areas();
			for (int i = 0; i < iArea.getWidget().getItemCount(); i++ ) {
				if (iArea.getWidget().isItemSelected(i)) {
					Area area = new Area();
					area.setId(Long.valueOf(iArea.getWidget().getValue(i)));
					area.setName(iArea.getWidget().getItemText(i));
					curriculum.getAreas().add(area);
				}
			}
			if (curriculum.getAreas().isEmpty()) {
				iArea.setErrorHint(MESSAGES.hintAcademicAreaNotProvided());
				ok = false;
			} else {
				for (int i = 0; i < iMajors.getItemCount(); i++ ) {
					if (iMajors.isItemSelected(i)) {
						IdName mj = new IdName();
						mj.setId(Long.valueOf(iMajors.getValue(i)));
						mj.setName(iMajors.getItemText(i));
						curriculum.getMajors().add(mj);
					}
				}
				for (int i = 0; i < iMinors.getItemCount(); i++ ) {
					if (iMinors.isItemSelected(i)) {
						IdName mj = new IdName();
						mj.setId(Long.valueOf(iMinors.getValue(i)));
						mj.setName(iMinors.getItemText(i));
						curriculum.getMinors().add(mj);
					}
				}
				for (int i = 0; i < iClassifications.getItemCount(); i++ ) {
					if (iClassifications.isItemSelected(i)) {
						IdName clasf = new IdName();
						clasf.setId(Long.valueOf(iClassifications.getValue(i)));
						clasf.setName(iClassifications.getItemText(i));
						curriculum.getClassifications().add(clasf);
					}
				}
				for (int i = 0; i < iConcentrations.getItemCount(); i++ ) {
					if (iConcentrations.isItemSelected(i)) {
						IdName cc = new IdName();
						cc.setId(Long.valueOf(iConcentrations.getValue(i)));
						cc.setName(iConcentrations.getItemText(i));
						curriculum.getConcentrations().add(cc);
					}
				}
				((ReservationInterface.CurriculumReservation) r).setCurriculum(curriculum);
			}
		} else if ("universal".equals(type)) {
			r = new ReservationInterface.UniversalReservation();
			r.setOverride(true);
			((ReservationInterface.UniversalReservation) r).setFilter(iStudentFilter.getWidget().getText());
			if (iStudentFilter.getWidget().getText().isEmpty()) {
				iStudentFilter.setErrorHint(MESSAGES.hintReservationNoFilter());
				ok = false;
			}
		} else if ("".equals(type)) {
			iType.setErrorHint(MESSAGES.hintReservationTypeNotSelected());
			return null;
		} else {
			iType.setErrorHint(MESSAGES.hintReservationTypeNotSupported(type));
			return null;
		}
		if (iExpirationDate.getWidget().getValue() == null && !iExpirationDate.getWidget().getText().isEmpty()) {
			iExpirationDate.setErrorHint(MESSAGES.hintExpirationDateNotValid());
			ok = false;
		} else {
			r.setExpirationDate(iExpirationDate.getWidget().getValueInServerTimeZone());
			iExpirationDate.clearHint();
		}
		if (iStartDate.getWidget().getValue() == null && !iStartDate.getWidget().getText().isEmpty()) {
			iStartDate.setErrorHint(MESSAGES.hintStartDateNotValid());
			ok = false;
		} else {
			r.setStartDate(iStartDate.getWidget().getValueInServerTimeZone());
			iStartDate.clearHint();
		}
		if (r.getStartDate() != null && r.getExpirationDate() != null && r.getExpirationDate().before(r.getStartDate())) {
			iExpirationDate.setErrorHint(MESSAGES.hintExpirationDateNotAfterStartDate());
			ok = false;
		}
		if (!"individual".equals(type) && !"individual-override".equals(type) && getOverrideType(type) == null) {
			if (iLimit.getWidget().getText().isEmpty()) {
				r.setLimit(null);
			} else {
				try {
					r.setLimit(Integer.parseInt(iLimit.getWidget().getText()));
				} catch (Exception e) {
					iLimit.setErrorHint(MESSAGES.hintReservationLimitNotValid());
					ok = false;
				}
			}
		}
		if (r.isOverride()) {
			r.setMustBeUsed(iMustBeUsed.getValue());
			r.setAlwaysExpired(iAlwaysExpired.getValue());
			r.setOverLimit(iOverLimit.getValue());
			r.setAllowOverlaps(iCanOverlap.getValue());
		}
		if (iPanel.getRowFormatter().isVisible(iInclusionLine))
			r.setInclusive(iInclusive.getSelectedIndex() == 0 ? null : iInclusive.getSelectedIndex() == 1 ? Boolean.TRUE : Boolean.FALSE);
		else
			r.setInclusive(null);
		if (iReservation != null)
			r.setId(iReservation.getId());
		Offering o = new Offering();
		if (iOffering == null) {
			iCourseBox.setError(MESSAGES.hintOfferingNotProvided());
			ok = false;
		} else if (iOffering.isUnlockNeeded()) {
			iCourseBox.setError(MESSAGES.hintOfferingIsLocked(iOffering.getAbbv()));
			ok = false;
		} else {
			o.setId(iOffering.getId());
			o.setName(iOffering.getName());
		}
		r.setOffering(o);
		iRestrictions.validate(r);
		return (ok ? r : null);
	}

	public static class EditFinishedEvent {
		private Long iReservationId;
		public EditFinishedEvent(Long reservationId) {
			iReservationId = reservationId;
		}
		public Long getReservationId() {
			return iReservationId;
		}
	}

	public static interface EditFinishedHandler {
		public void onBack(EditFinishedEvent evt);
		public void onDelete(EditFinishedEvent evt);
		public void onSave(EditFinishedEvent evt);
		public void onFailure(Throwable caught);
		public boolean hasNext(EditFinishedEvent evt);
		public boolean hasPrevious(EditFinishedEvent evt);
		public void onNext(EditFinishedEvent evt);
		public void onPrevious(EditFinishedEvent evt);
	}

	public void addEditFinishedHandler(EditFinishedHandler h) {
		iEditFinishedHandlers.add(h);
	}
	
	public static interface Initialization {
		public void execute(AsyncCallback<Boolean> callback);
	}
	
	public class InitExpirationDates implements Initialization {
		@Override
		public void execute(final AsyncCallback<Boolean> callback) {
			RPC.execute(new ReservationDefaultExpirationDatesRpcRequest(), new AsyncCallback<DefaultExpirationDates>() {
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				@Override
				public void onSuccess(DefaultExpirationDates result) {
					iExpirations = result;
					if (iExpirations.hasInclusive())
						iInclusive.setItemText(0, iExpirations.isInclusive() ? MESSAGES.reservationInclusiveDefaultTrue() : MESSAGES.reservationInclusiveDefaultFalse());
					callback.onSuccess(true);
				}
			});
		}
	}
	
	public class InitStudentGroups implements Initialization {

		@Override
		public void execute(final AsyncCallback<Boolean> callback) {
			iReservationService.getStudentGroups(new AsyncCallback<List<IdName>>() {
				@Override
				public void onFailure(Throwable caught) {
					iType.getWidget().removeItem(2);
					callback.onFailure(caught);
				}
				@Override
				public void onSuccess(List<IdName> result) {
					if (result.isEmpty()) {
						iType.getWidget().removeItem(2);
					} else {
						for (IdName group: result) {
							iGroup.getWidget().addItem(group.getAbbv() + " - " + group.getName() + " (" + group.getLimit() + ")", group.getId().toString());
						}
					}
					callback.onSuccess(true);
				}
			});			
		}

	}
	
	public class InitCurricula implements Initialization {

		@Override
		public void execute(final AsyncCallback<Boolean> callback) {
			iReservationService.getAreas(new AsyncCallback<List<Area>>() {
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
				
				@Override
				public void onSuccess(List<Area> result) {
					for (Area c: result) {
						iArea.getWidget().addItem(c.getAbbv() + " - " + c.getName(), c.getId().toString());
						iAreas.put(c.getId(), c);
					}
					callback.onSuccess(true);
				}
			});
		}
	}
	
	public static class InitializationChain implements Initialization {
		private Initialization[] iInitializations;
		
		public InitializationChain(Initialization... initializations) {
			iInitializations = initializations;
		}

		@Override
		public void execute(AsyncCallback<Boolean> callback) {
			execute(0, callback);
		}
		
		private void execute(final int index, final AsyncCallback<Boolean> callback) {
			if (index >= iInitializations.length) {
				callback.onSuccess(true);
			} else {
				iInitializations[index].execute(new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
					@Override
					public void onSuccess(Boolean result) {
						if (result) {
							execute(1 + index, callback);
						} else {
							callback.onSuccess(false);
						}
					}
				});
			}
		}
	}
	
}
