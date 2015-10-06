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
import java.util.List;

import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.curricula.CurriculaCourseSelectionBox;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionEvent;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionHandler;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.ReservationService;
import org.unitime.timetable.gwt.services.ReservationServiceAsync;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.ReservationException;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface.Clazz;
import org.unitime.timetable.gwt.shared.ReservationInterface.Config;
import org.unitime.timetable.gwt.shared.ReservationInterface.Course;
import org.unitime.timetable.gwt.shared.ReservationInterface.Area;
import org.unitime.timetable.gwt.shared.ReservationInterface.Curriculum;
import org.unitime.timetable.gwt.shared.ReservationInterface.DefaultExpirationDates;
import org.unitime.timetable.gwt.shared.ReservationInterface.IdName;
import org.unitime.timetable.gwt.shared.ReservationInterface.Offering;
import org.unitime.timetable.gwt.shared.ReservationInterface.ReservationDefaultExpirationDatesRpcRequest;
import org.unitime.timetable.gwt.shared.ReservationInterface.Subpart;

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
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
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
	private UniTimeWidget<SingleDateSelector> iExpirationDate;
	private Tree iStructure;
	private HashMap<Long, ConfigSelection> iConfigs = new HashMap<Long, ConfigSelection>();
	private HashMap<Long, ClassSelection> iClasses = new HashMap<Long, ClassSelection>();
	private HashMap<Long, Area> iAreas = new HashMap<Long, Area>();
	private HashMap<Long, Curriculum> iCurricula = new HashMap<Long, Curriculum>();
	private ListBox iClassifications, iMajors;
	private UniTimeWidget<ListBox> iType, iArea, iCourse, iGroup, iCurriculum;
	private UniTimeWidget<TextArea> iStudents;
	private ReservationInterface iReservation;
	private CurriculaCourseSelectionBox iCourseBox;
	private Lookup iLookup;
	private DefaultExpirationDates iExpirations = null;
	
	private final ReservationServiceAsync iReservationService = GWT.create(ReservationService.class);

	private int iExpirationLine, iReservedSpaceLine, iGroupLine, iCourseLine, iAreaLine, iStudentsLine, iCurriculumLine;
	
	private Offering iOffering = null;
	
	public ReservationEdit(boolean standAlone) {
		if (standAlone) {
			addEditFinishedHandler(new EditFinishedHandler() {
				@Override
				public void onSave(EditFinishedEvent evt) {
					ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?io=" + iOffering.getId() + "#reservations");
				}
				
				@Override
				public void onDelete(EditFinishedEvent evt) {
					ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?io=" + iOffering.getId() + "#reservations");
				}
				
				@Override
				public void onBack(EditFinishedEvent evt) {
					ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?io=" + iOffering.getId() + "#reservations");
				}

				@Override
				public void onFailure(Throwable caught) {
					new Timer() {
						@Override
						public void run() {
							ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?io=" + iOffering.getId() + "#reservations");
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
		
		iCourseBox = new CurriculaCourseSelectionBox();
		iCourseBox.setWidth("130px");
		iPanel.addRow(MESSAGES.propInstructionalOffering(), iCourseBox);
		iPanel.getCellFormatter().getElement(iPanel.getRowCount() - 1, 0).getStyle().setWidth(100, Unit.PX);
		
		iType = new UniTimeWidget<ListBox>(new ListBox());
		iType.getWidget().setStyleName("unitime-TextBox");
		iType.getWidget().addItem(MESSAGES.itemSelect(), "");
		iType.getWidget().addItem(MESSAGES.reservationIndividual(), "individual");
		iType.getWidget().addItem(MESSAGES.reservationStudentGroup(), "group");
		iType.getWidget().addItem(MESSAGES.reservationCurriculum(), "curriculum");
		iType.getWidget().addItem(MESSAGES.reservationCourse(), "course");
		for (ReservationInterface.OverrideType t: ReservationInterface.OverrideType.values()) {
			iType.getWidget().addItem(CONSTANTS.reservationOverrideTypeName()[t.ordinal()], t.getReference());
		}
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
		iLimit.getWidget().addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				computeLimit();
			}
		});
		iReservedSpaceLine = iPanel.addRow(MESSAGES.propReservedSpace(), iLimit);
		
		iExpirationDate = new UniTimeWidget<SingleDateSelector>(new SingleDateSelector());
		iExpirationDate.getWidget().addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iExpirationDate.clearHint();
			}
		});
		iExpirationLine = iPanel.addRow(MESSAGES.propExpirationDate(), iExpirationDate);
		
		iStructure = new Tree(RESOURCES, true);
		iPanel.addRow(MESSAGES.propRestrictions(), iStructure);
		iPanel.getCellFormatter().setVerticalAlignment(iPanel.getRowCount() - 1, 0, HasVerticalAlignment.ALIGN_TOP);
		
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
		iPanel.addRow("Curriculum:", iCurriculum);
		iCurriculumLine = iPanel.getRowCount() - 1;
		
		iArea = new UniTimeWidget<ListBox>(new ListBox());
		iArea.getWidget().setStyleName("unitime-TextBox");
		iArea.getWidget().addItem(MESSAGES.itemSelect(), "");
		iArea.getWidget().setSelectedIndex(0);
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
				areaChanged();
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
				if (event.isValid()) {
					iReservationService.getOfferingByCourseName(event.getCourse(), new AsyncCallback<Offering>() {
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
		
		Long offeringId = (iReservation == null ? null : iReservation.getOffering().getId());
		if (offeringId == null) {
			if (Window.Location.getParameter("offering") != null)
				offeringId =  Long.valueOf(Window.Location.getParameter("offering"));
		}

		if (offeringId == null) {
			iOffering = null;
			iCourseBox.setEnabled(true);
			iCourseBox.setValue("", false);
			iLimit.getWidget().setValue("", true);
			iExpirationDate.getWidget().setValue(null);
			iStructure.clear(); iClasses.clear(); iConfigs.clear();
			iType.getWidget().setSelectedIndex(0);
			iGroup.getWidget().setSelectedIndex(0);
			iArea.getWidget().setSelectedIndex(0);
			iClassifications.clear();
			iStudents.getWidget().setText("");
			iCourse.getWidget().clear();
			iMajors.clear();
			iType.setReadOnly(false);
			iCurricula.clear();
			iCurriculum.getWidget().clear();
			iCurriculum.getWidget().addItem(MESSAGES.itemNone(), "");
			iCurriculum.getWidget().setSelectedIndex(0);
			areaChanged();
			typeChanged(true);
		} else {
			iCourseBox.setEnabled(false);
			LoadingWidget.getInstance().show(MESSAGES.waitLoadingReservation());
			iReservationService.getOffering(offeringId, new AsyncCallback<Offering>() {
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
		if (iOffering == null) return;

		if (!iCourseBox.isEnabled())
			iCourseBox.setValue(iOffering.getAbbv(), false);

		iStructure.clear(); iClasses.clear(); iConfigs.clear();
		for (Config config: iOffering.getConfigs()) {
			TreeItem configItem = new TreeItem(new ConfigSelection(config));
			for (Subpart subpart: config.getSubparts()) {
				if (subpart.getParentId() == null)
					for (TreeItem item: addClasses(subpart, null))
						configItem.addItem(item);
			}
			iStructure.addItem(configItem);
		}
		
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
				curricula: for (Curriculum curriculum: result) {
					iCurricula.put(curriculum.getId(), curriculum);
					iCurriculum.getWidget().addItem(curriculum.getAbbv() + " - " + curriculum.getName() + " (" + curriculum.getLimit() + ")", curriculum.getId().toString());
					if (curriculum.getArea().getId().toString().equals(iArea.getWidget().getValue(iArea.getWidget().getSelectedIndex()))) {
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
		computeLimit();
	}
	
	private List<TreeItem> addClasses(Subpart subpart, Long parent) {
		List<TreeItem> ret = new ArrayList<TreeItem>();
		for (Clazz clazz: subpart.getClasses()) {
			if (parent != null && !parent.equals(clazz.getParentId())) continue;
			TreeItem clazzItem = new TreeItem(new ClassSelection(clazz));
			for (Subpart child: subpart.getConfig().getSubparts()) {
				if (subpart.getId().equals(child.getParentId()))
					for (TreeItem item: addClasses(child, clazz.getId()))
						clazzItem.addItem(item);
			}
			ret.add(clazzItem);
		}
		return ret;
	}
	
	private ReservationInterface.OverrideType getOverrideType(String ref) {
		for (ReservationInterface.OverrideType t: ReservationInterface.OverrideType.values())
			if (t.getReference().equals(ref)) return t;
		return null;
	}
	
	private void typeChanged(boolean setExpiration) {
		String val = iType.getWidget().getValue(iType.getWidget().getSelectedIndex());
		iPanel.getRowFormatter().setVisible(iStudentsLine, "individual".equals(val) || getOverrideType(val) != null);
		iPanel.getRowFormatter().setVisible(iCourseLine, "course".equals(val));
		iPanel.getRowFormatter().setVisible(iGroupLine, "group".equals(val));
		iPanel.getRowFormatter().setVisible(iCurriculumLine, "curriculum".equals(val) && iCurriculum.getWidget().getItemCount() > 1);
		iPanel.getRowFormatter().setVisible(iAreaLine, "curriculum".equals(val));
		iPanel.getRowFormatter().setVisible(1 + iAreaLine, "curriculum".equals(val));
		iPanel.getRowFormatter().setVisible(2 + iAreaLine, "curriculum".equals(val));
		iPanel.getRowFormatter().setVisible(iExpirationLine, getOverrideType(val) == null || getOverrideType(val).isCanHaveExpirationDate());
		iPanel.getRowFormatter().setVisible(iReservedSpaceLine, getOverrideType(val) == null || getOverrideType(val).isCanHaveExpirationDate() || !getOverrideType(val).isExpired());
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
		} else if ("individual".equals(val) || getOverrideType(val) != null) {
			iLimit.getWidget().setReadOnly(true);
			iLimit.getWidget().setValue(String.valueOf(iStudents.getWidget().getText().split("\n").length), true);
		} else {
			iLimit.getWidget().setReadOnly(false);
		}
		if (setExpiration && iExpirations != null) {
			iExpirationDate.getWidget().setValueInServerTimeZone(iExpirations.getExpirationDate(val));
		}
	}
	
	private void areaChanged() {
		iMajors.clear();
		iClassifications.clear();
		String id = iArea.getWidget().getValue(iArea.getWidget().getSelectedIndex());
		if (!id.isEmpty()) {
			Area c = iAreas.get(Long.valueOf(id));
			for (IdName major: c.getMajors()) {
				iMajors.addItem(major.getAbbv() + " - " + major.getName(), major.getId().toString());
			}
			for (IdName clasf: c.getClassifications()) {
				iClassifications.addItem(clasf.getAbbv() + " - " + clasf.getName(), clasf.getId().toString());
			}
		}
		String curId = (iCurriculum.getWidget().getSelectedIndex() < 0 ? "" : iCurriculum.getWidget().getValue(iCurriculum.getWidget().getSelectedIndex()));
		if (!curId.isEmpty()) {
			Curriculum c = iCurricula.get(Long.valueOf(curId));
			if (!c.getArea().getId().toString().equals(id))
				iCurriculum.getWidget().setSelectedIndex(0);
		}
	}
	
	private void curriculumChanged() {
		String id = iCurriculum.getWidget().getValue(iCurriculum.getWidget().getSelectedIndex());
		if (!id.isEmpty()) {
			iMajors.clear();
			iClassifications.clear();
			Curriculum c = iCurricula.get(Long.valueOf(id));
			for (int i = 0; i < iArea.getWidget().getItemCount(); i++) {
				if (c.getArea().getId().toString().equals(iArea.getWidget().getValue(i))) {
					iArea.getWidget().setSelectedIndex(i); break;
				}
			}
			areaChanged();
			for (int i = 0; i < iMajors.getItemCount(); i++) {
				String majorId = iMajors.getValue(i);
				boolean hasMajor = false;
				for (IdName m: c.getMajors())
					if (m.getId().toString().equals(majorId)) { hasMajor = true; break; }
				iMajors.setItemSelected(i, hasMajor);
			}
			for (int i = 0; i < iClassifications.getItemCount(); i++) {
				String clasfId = iClassifications.getValue(i);
				boolean hasClasf = false;
				for (IdName m: c.getClassifications())
					if (m.getId().toString().equals(clasfId)) { hasClasf = true; break; }
				iClassifications.setItemSelected(i, hasClasf);
			}
			iLimit.getWidget().setValue(c.getLimit() == null ? "" : c.getLimit().toString(), true);
		}
	}
	
	public void populate() {
		if (iReservation == null) return;
		iLimit.getWidget().setValue(iReservation.getLimit() == null ? "" : iReservation.getLimit().toString());
		iExpirationDate.getWidget().setValueInServerTimeZone(iReservation.getExpirationDate());
		for (Clazz c: iReservation.getClasses()) {
			ClassSelection s = iClasses.get(c.getId());
			if (s != null) {
				s.setValue(true);
				s.propagateUp(true);
				s.propagateDown(true);
			}
		}
		for (Config c: iReservation.getConfigs()) {
			ConfigSelection s = iConfigs.get(c.getId());
			if (s != null) {
				s.setValue(true);
				s.propagate(true);
			}
		}
		for (int i = 0; i < iStructure.getItemCount(); i++)
			openNodes(iStructure.getItem(i));
		if (iReservation instanceof ReservationInterface.OverrideReservation) {
			select(iType.getWidget(), ((ReservationInterface.OverrideReservation)iReservation).getType().getReference());
			String students = "";
			for (IdName student: ((ReservationInterface.OverrideReservation) iReservation).getStudents())
				students += (students.isEmpty() ? "" : "\n") + student.getAbbv() + " " + student.getName();
			iStudents.getWidget().setText(students);
		} else if (iReservation instanceof ReservationInterface.IndividualReservation) {
			select(iType.getWidget(), "individual");
			String students = "";
			for (IdName student: ((ReservationInterface.IndividualReservation) iReservation).getStudents())
				students += (students.isEmpty() ? "" : "\n") + student.getAbbv() + " " + student.getName();
			iStudents.getWidget().setText(students);
		} else if (iReservation instanceof ReservationInterface.GroupReservation) {
			select(iType.getWidget(), "group");
			select(iGroup.getWidget(), ((ReservationInterface.GroupReservation) iReservation).getGroup().getId().toString());
		} else if (iReservation instanceof ReservationInterface.CourseReservation) {
			select(iType.getWidget(), "course");
			select(iCourse.getWidget(), ((ReservationInterface.CourseReservation) iReservation).getCourse().getId().toString());
		} else if (iReservation instanceof ReservationInterface.CurriculumReservation) {
			select(iType.getWidget(), "curriculum");
			Area curriculum = ((ReservationInterface.CurriculumReservation) iReservation).getCurriculum();
			select(iArea.getWidget(), curriculum.getId().toString());
			areaChanged();
			for (int i = 0; i < iMajors.getItemCount(); i++) {
				Long id = Long.valueOf(iMajors.getValue(i));
				boolean selected = false;
				for (IdName mj: curriculum.getMajors())
					if (mj.getId().equals(id)) selected = true;
				iMajors.setItemSelected(i, selected);
			}
			for (int i = 0; i < iClassifications.getItemCount(); i++) {
				Long id = Long.valueOf(iClassifications.getValue(i));
				boolean selected = false;
				for (IdName clasf: curriculum.getClassifications())
					if (clasf.getId().equals(id)) selected = true;
				iClassifications.setItemSelected(i, selected);
			}
		}
		typeChanged(false);
		iType.setReadOnly(true);
		iType.setText(iType.getWidget().getItemText(iType.getWidget().getSelectedIndex()));
		computeLimit();
	}
	
	private void openNodes(TreeItem item) {
		CheckBox ch = (CheckBox)item.getWidget();
		if (!ch.getValue() || ch.isEnabled()) return;
		item.setState(true);
		for (int i = 0; i < item.getChildCount(); i++)
			openNodes(item.getChild(i));
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
		if ("individual".equals(type) || getOverrideType(type) != null) {
			if ("individual".equals(type))
				r = new ReservationInterface.IndividualReservation();
			else 
				r = new ReservationInterface.OverrideReservation(getOverrideType(type));
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
		} else if ("group".equals(type)) {
			r = new ReservationInterface.GroupReservation();
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
		} else if ("curriculum".equals(type)) {
			r = new ReservationInterface.CurriculumReservation();
			String aid = iArea.getWidget().getValue(iArea.getWidget().getSelectedIndex());
			if (aid.isEmpty()) {
				iArea.setErrorHint(MESSAGES.hintAcademicAreaNotProvided());
				ok = false;
			} else {
				Area curriculum = new Area();
				curriculum.setId(Long.valueOf(aid));
				curriculum.setName(iArea.getWidget().getItemText(iArea.getWidget().getSelectedIndex()));
				for (int i = 0; i < iMajors.getItemCount(); i++ ) {
					if (iMajors.isItemSelected(i)) {
						IdName mj = new IdName();
						mj.setId(Long.valueOf(iMajors.getValue(i)));
						mj.setName(iMajors.getItemText(i));
						curriculum.getMajors().add(mj);
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
				((ReservationInterface.CurriculumReservation) r).setCurriculum(curriculum);
			}
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
		if (!"individual".equals(type) && getOverrideType(type) == null) {
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
		if (iReservation != null)
			r.setId(iReservation.getId());
		Offering o = new Offering();
		if (iOffering == null) {
			iCourseBox.setError(MESSAGES.hintOfferingNotProvided());
			ok = false;
		} else {
			o.setId(iOffering.getId());
			o.setName(iOffering.getName());
		}
		r.setOffering(o);
		for (ConfigSelection config: iConfigs.values()) {
			if (config.getValue() && config.isEnabled()) {
				Config c = new Config();
				c.setId(config.getConfig().getId());
				c.setName(config.getConfig().getName());
				r.getConfigs().add(c);
			}
		}
		for (ClassSelection clazz: iClasses.values()) {
			if (clazz.getValue() && clazz.isEnabled()) {
				Clazz c = new Clazz();
				c.setId(clazz.getClazz().getId());
				c.setName(clazz.getClazz().getName());
				r.getClasses().add(c);
			}
		}
		return (ok ? r : null);
	}

	private class ClassSelection extends CheckBox implements ClickHandler {
		private Clazz iClazz;
		
		public ClassSelection(Clazz clazz) {
			setStyleName("unitime-LabelInsteadEdit");
			setText(clazz.getAbbv() + " (" + clazz.getLimit() + ")");
			iClazz = clazz;
			addClickHandler(this);
			iClasses.put(clazz.getId(), this);
		}
		
		public Clazz getClazz() { return iClazz; }

		@Override
		public void onClick(ClickEvent event) {
			propagateUp(getValue());
			propagateDown(getValue());
			computeLimit();
		}
		
		private void propagateUp(boolean selected) {
			if (getClazz().getParentId() == null) {
				if (!selected) {
					for (Subpart subpart: getClazz().getSubpart().getConfig().getSubparts()) {
						if (subpart.getParentId() != null) continue;
						for (Clazz c: subpart.getClasses()) {
							if (iClasses.get(c.getId()).getValue()) return;
						}
					}
				}
				ConfigSelection config = iConfigs.get(getClazz().getSubpart().getConfig().getId());
				if (config != null) {
					config.setValue(selected);
					config.setEnabled(!selected);
				}
			} else {
				ClassSelection parent = iClasses.get(getClazz().getParentId());
				if (!selected) {
					for (Clazz c: getClazz().getSubpart().getClasses()) {
						if (parent.getClazz().getId().equals(c.getParentId())) {
							if (iClasses.get(c.getId()).getValue()) return;
						}
					}
				}
				parent.setValue(selected);
				parent.setEnabled(!selected);
				parent.propagateUp(selected);
			}
		}
		
		private void propagateDown(boolean selected) {
			for (Subpart subpart: getClazz().getSubpart().getConfig().getSubparts()) {
				if (getClazz().getSubpart().getId().equals(subpart.getParentId())) {
					for (Clazz clazz: subpart.getClasses()) {
						if (getClazz().getId().equals(clazz.getParentId())) {
							ClassSelection child = iClasses.get(clazz.getId());
							child.setValue(selected);
							child.setEnabled(!selected);
							child.propagateDown(selected);
						}
					}
				}
			}
		}
	}

	private class ConfigSelection extends CheckBox implements ClickHandler {
		private Config iConfig;
		
		public ConfigSelection(Config config) {
			setStyleName("unitime-LabelInsteadEdit");
			setText(MESSAGES.selectionConfiguration(config.getAbbv(), config.getLimit() == null ? MESSAGES.configUnlimited() : config.getLimit().toString()));
			iConfig = config;
			iConfigs.put(config.getId(), this);
			addClickHandler(this);
		}
		
		public Config getConfig() { return iConfig; }
		
		@Override
		public void onClick(ClickEvent event) {
			propagate(getValue());
			computeLimit();
		}
		
		private void propagate(boolean selected) {
			for (Subpart subpart: getConfig().getSubparts()) {
				for (Clazz clazz: subpart.getClasses()) {
					ClassSelection child = iClasses.get(clazz.getId());
					child.setValue(selected);
					child.setEnabled(!selected);
				}
			}
		}
	}
	
	private void computeLimit() {
		if (iOffering == null) {
			iLimit.clearHint();
			return;
		}
		// if (iLimit.isReadOnly()) return;
		int total = 0, limit = -1;
		boolean totalUnlimited = false, unlimited = false;
		for (Config config: iOffering.getConfigs()) {
			for (Subpart subpart: config.getSubparts()) {
				int lim = 0; boolean selected = false;
				for (Clazz clazz: subpart.getClasses()) {
					ClassSelection child = iClasses.get(clazz.getId());
					if (child.getValue()) {
						lim += clazz.getLimit();
						selected = true;
					}
				}
				if (selected && (limit < 0 || limit > lim)) { limit = lim; }
			}
		}
		int lim = 0; boolean selected = false;
		for (Config config: iOffering.getConfigs()) {
			if (config.getLimit() == null)
				totalUnlimited = true;
			else
				total += config.getLimit();
			ConfigSelection cfg = iConfigs.get(config.getId());
			if (cfg != null && cfg.getValue()) {
				selected = true;
				if (cfg.getConfig().getLimit() == null)
					unlimited = true;
				else
					lim += cfg.getConfig().getLimit();
			}
		}
		if (selected && (limit < 0 || limit > lim)) { limit = lim; }
		int entered = Integer.MAX_VALUE;
		try {
			entered = Integer.parseInt(iLimit.getWidget().getValue());
		} catch (NumberFormatException e) {}
		if (limit >= 0 || unlimited) {
			if (unlimited || limit >= entered)
				iLimit.clearHint();
			else
				iLimit.setHint(limit == 0 ? MESSAGES.hintNoSpaceSelected() : limit == 1 ? MESSAGES.hintOnlyOneSpaceSelected() : MESSAGES.hintOnlyNSpacesSelected(limit));
		} else {
			if (!iOffering.isOffered())
				iLimit.setHint(MESSAGES.hintCourseNotOffered(iOffering.getAbbv()));
			else if (totalUnlimited || total >= entered || entered == Integer.MAX_VALUE)
				iLimit.clearHint();
			else
				iLimit.setHint(total == 0 ? MESSAGES.hintNoSpaceInCourse(iOffering.getAbbv()) : total == 1 ? MESSAGES.hintOnlyOneSpaceInCourse(iOffering.getAbbv()) : MESSAGES.hintOnlyNSpacesInCourse(total, iOffering.getAbbv()));
		}
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
