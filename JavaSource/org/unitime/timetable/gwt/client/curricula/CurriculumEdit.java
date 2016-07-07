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
package org.unitime.timetable.gwt.client.curricula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.curricula.CurriculaClassifications.ExpectedChangedEvent;
import org.unitime.timetable.gwt.client.curricula.CurriculaClassifications.NameChangedEvent;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumStudentsInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.MajorInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * @author Tomas Muller
 */
public class CurriculumEdit extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);

	private SimpleForm iCurriculaTable;
	
	private UniTimeHeaderPanel iTitleAndButtons;
	
	private UniTimeWidget<TextBox> iCurriculumAbbv, iCurriculumName;
	private UniTimeWidget<ListBox> iCurriculumMajors, iCurriculumArea, iCurriculumDept;
	private UniTimeWidget<CheckBox> iMultipleMajor;
	private CurriculaClassificationsPanel iCurriculumClasfTable = null;
	
	private boolean iDefaultAbbv = false, iDefaultName = false;
	
	private List<AcademicAreaInterface> iAreas = new ArrayList<AcademicAreaInterface>();
	private List<DepartmentInterface> iDepts = new ArrayList<DepartmentInterface>();
	private List<MajorInterface> iMajors = new ArrayList<MajorInterface>();
	private List<AcademicClassificationInterface> iClassifications = new ArrayList<AcademicClassificationInterface>();
	private CurriculumInterface iCurriculum = null;
	
	private CurriculaCourses iCurriculumCourses;
	
	private List<EditFinishedHandler> iEditFinishedHandlers = new ArrayList<EditFinishedHandler>();
	
	private boolean iAreaHasNoMajors = false;
	
	private Mode iMode;
	
	private boolean iSaved = false;
	
	private NavigationProvider iNavigation = null;
	
	public static enum Mode {
		ADD(MESSAGES.pageAddCurriculum(), true, true),
		EDIT(MESSAGES.pageEditCurriculum(), true, true),
		DETAILS(MESSAGES.pageCurriculumDetails(), false, false),
		DIALOG(null, true, false);
		
		private String iTitle;
		private boolean iEditable, iEditableDetails;
		Mode(String title, boolean editable, boolean details) { iTitle = title; iEditable = editable; iEditableDetails = details; }
		public boolean hasTitle() { return iTitle != null; }
		public String getTitle() { return iTitle; }
		public boolean isEditable() { return iEditable; }
		public boolean areDetailsEditable() { return iEditableDetails; }
	}
	
	public CurriculumEdit(NavigationProvider navigation) {
		iNavigation = navigation;

		ClickHandler backHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iMode == Mode.EDIT) { // back to detail screen
					// loadCurriculum(Mode.DETAILS);
					reload(Mode.DETAILS);
				} else {
					EditFinishedEvent e = new EditFinishedEvent();
					for (EditFinishedHandler h: iEditFinishedHandlers) {
						if (iSaved)
							h.onSave(e);
						else
							h.onBack(e);
					}
				}
			}
		};
		
		ClickHandler saveHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (saveCurriculum()) {
					showLoading(MESSAGES.waitSavingCurriculum(iCurriculum.getName()));
					iService.saveCurriculum(iCurriculum, new AsyncCallback<Long>() {
						@Override
						public void onFailure(Throwable caught) {
							hideLoading();
							iTitleAndButtons.setErrorMessage(MESSAGES.failedValidation(caught.getMessage()));
							UniTimeNotifications.error(MESSAGES.failedValidation(caught.getMessage()), caught);
						}
						@Override
						public void onSuccess(Long result) {
							if (iMode == Mode.EDIT) { // back to details page
								iCurriculum.setId(result);
								reload(Mode.DETAILS);
								iSaved = true;
							} else {
								EditFinishedEvent e = new EditFinishedEvent();
								for (EditFinishedHandler h: iEditFinishedHandlers) {
									h.onSave(e);
								}
							}
							hideLoading();
						}
					});
				} else {
					iTitleAndButtons.setErrorMessage(MESSAGES.failedValidationSeeBelow());
				}
			}
		};
		
		ClickHandler deleteHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!Window.confirm(MESSAGES.confirmDeleteThisCurriculum())) return;
				showLoading(MESSAGES.waitDeletingCurriculum(iCurriculum.getName()));
				iService.deleteCurriculum(iCurriculum.getId(), new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						iTitleAndButtons.setErrorMessage(MESSAGES.failedDelete(iCurriculum.getName(), caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedDelete(iCurriculum.getName(), caught.getMessage()), caught);
						hideLoading();
					}
					@Override
					public void onSuccess(Boolean result) {
						EditFinishedEvent e = new EditFinishedEvent();
						for (EditFinishedHandler h: iEditFinishedHandlers) {
							h.onDelete(e);
						}
						hideLoading();
					}
				});
			}
		};
		
		ClickHandler printHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.print();
			}
		};
		
		ClickHandler editHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadCurriculum(Mode.EDIT);
			}
		};
		
		ClickHandler nextHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final CurriculumInterface next = (iNavigation == null ? null : iNavigation.next(iCurriculum));
				if (next == null) {
					iTitleAndButtons.setErrorMessage(MESSAGES.errorNoNextCurriculum());
					return;
				}
				if (getMode().isEditable()) {
					if (saveCurriculum()) {
						showLoading(MESSAGES.waitSavingCurriculum(iCurriculum.getName()));
						iService.saveCurriculum(iCurriculum, new AsyncCallback<Long>() {
							@Override
							public void onFailure(Throwable caught) {
								hideLoading();
								iTitleAndButtons.setErrorMessage(MESSAGES.failedValidation(caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedValidation(caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(Long result) {
								iSaved = false;
								iCurriculum = next;
								reload(iMode);
								iNavigation.onChange(iCurriculum);
								hideLoading();
							}
						});
					} else {
						iTitleAndButtons.setErrorMessage(MESSAGES.failedValidationSeeBelow());
					}
				} else {
					iSaved = false;
					iCurriculum = next;
					reload(iMode);
					iNavigation.onChange(iCurriculum);
				}
			}
		};
		
		ClickHandler previousHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final CurriculumInterface previous = (iNavigation == null ? null : iNavigation.previous(iCurriculum));
				if (previous == null) {
					iTitleAndButtons.setErrorMessage(MESSAGES.errorNoPreviousCurriculum());
					return;
				}
				if (getMode().isEditable()) {
					if (saveCurriculum()) {
						showLoading(MESSAGES.waitSavingCurriculum(iCurriculum.getName()));
						iService.saveCurriculum(iCurriculum, new AsyncCallback<Long>() {
							@Override
							public void onFailure(Throwable caught) {
								hideLoading();
								iTitleAndButtons.setErrorMessage(MESSAGES.failedValidation(caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedValidation(caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(Long result) {
								iSaved = false;
								iCurriculum = previous;
								reload(iMode);
								iNavigation.onChange(iCurriculum);
								hideLoading();
							}
						});
					} else {
						iTitleAndButtons.setErrorMessage(MESSAGES.failedValidationSeeBelow());
					}
				} else {
					iSaved = false;
					iCurriculum = previous;
					reload(iMode);
					iNavigation.onChange(iCurriculum);
				}
			}
		};

		iCurriculaTable = new SimpleForm();

		iTitleAndButtons = new UniTimeHeaderPanel(MESSAGES.headerCurriculumDetails());
		iTitleAndButtons.addButton("edit", MESSAGES.buttonEdit(), 75, editHandler);
		iTitleAndButtons.addButton("save", MESSAGES.buttonSave(), 75, saveHandler);
		iTitleAndButtons.addButton("previous", MESSAGES.buttonPrevious(), 75, previousHandler);
		iTitleAndButtons.addButton("next", MESSAGES.buttonNext(), 75, nextHandler);
		iTitleAndButtons.addButton("delete", MESSAGES.buttonDelete(), 75, deleteHandler);
		iTitleAndButtons.addButton("print", MESSAGES.buttonPrint(), 75, printHandler);
		iTitleAndButtons.addButton("back", MESSAGES.buttonBack(), 75, backHandler);
		
		
		iCurriculaTable.addHeaderRow(iTitleAndButtons);
		
		iCurriculumAbbv = new UniTimeWidget<TextBox>(new UniTimeTextBox(40, 300, ValueBoxBase.TextAlignment.LEFT));
		iCurriculaTable.addRow(MESSAGES.propAbbreviation(), iCurriculumAbbv);
		iCurriculumAbbv.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iDefaultAbbv = false;
				iCurriculumAbbv.clearHint();
			}
		});

		iCurriculumName = new UniTimeWidget<TextBox>(new UniTimeTextBox(100, 700));
		iCurriculaTable.addRow(MESSAGES.propName(), iCurriculumName);
		iCurriculumName.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iDefaultName = false;
				iCurriculumName.clearHint();
			}
		});

		iCurriculumArea = new UniTimeWidget<ListBox>(new ListBox());
		iCurriculumArea.getWidget().setMultipleSelect(false);
		iCurriculumArea.getWidget().setWidth("300px");
		iCurriculumArea.getWidget().setStyleName("unitime-TextBox");
		iCurriculumArea.getWidget().setVisibleItemCount(1);
		iCurriculaTable.addRow(MESSAGES.propAcademicArea(), iCurriculumArea);
		
		iCurriculumArea.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if (iDefaultName || iDefaultAbbv) {
					try {
						if (iCurriculumArea.getWidget().getSelectedIndex() == 0) {
							if (iDefaultAbbv) iCurriculumAbbv.getWidget().setText("");
							if (iDefaultName) iCurriculumName.getWidget().setText("");
						} else {
							AcademicAreaInterface area = iAreas.get(iCurriculumArea.getWidget().getSelectedIndex() - 1);
							if (iDefaultAbbv) iCurriculumAbbv.getWidget().setText(area.getAbbv());
							if (iDefaultName) iCurriculumName.getWidget().setText(area.getName());
						}
					} catch (Exception e) {}
				}
				iCurriculumArea.clearHint();
				loadMajors(true, true);
			}
		});
		
		iMultipleMajor = new UniTimeWidget<CheckBox>(new CheckBox(MESSAGES.infoMultipleMajorsOff()));
		iMultipleMajor.getWidget().setValue(false);
		iMultipleMajor.getWidget().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				loadMajors(true, true);
				iCurriculumArea.clearHint();
				iMultipleMajor.getWidget().setText(event.getValue() ? MESSAGES.infoMultipleMajorsOn() : MESSAGES.infoMultipleMajorsOff());
			}
		});
		iMultipleMajor.addStyleName("unitime-CurriculumMultipleMajors");
		iCurriculaTable.addRow(MESSAGES.propMultipleMajors(), iMultipleMajor);
		

		iCurriculumMajors = new UniTimeWidget<ListBox>(new ListBox());
		iCurriculumMajors.getWidget().setMultipleSelect(true);
		iCurriculumMajors.getWidget().setWidth("300px");
		iCurriculumMajors.getWidget().setStyleName("unitime-TextBox");
		iCurriculumMajors.getWidget().setVisibleItemCount(3);
		iCurriculumMajors.getWidget().setHeight("100px");
		iCurriculaTable.addRow(MESSAGES.propMajorOrMajors(), iCurriculumMajors);
		
		iCurriculumMajors.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				try {
					String defaultAbbv = "", defaultName = "";
					AcademicAreaInterface area = iAreas.get(iCurriculumArea.getWidget().getSelectedIndex() - 1);
					defaultAbbv = area.getAbbv();
					defaultName = area.getName();
					String majors = "";
					for (int i = 0; i < iCurriculumMajors.getWidget().getItemCount(); i++) {
						if (iCurriculumMajors.getWidget().isItemSelected(i)) {
							MajorInterface m = iMajors.get(i);
							if (!defaultAbbv.contains("/")) { defaultAbbv += "/"; defaultName += " / "; }
							else { defaultAbbv += ","; defaultName += ", "; }
							defaultAbbv += m.getCode();
							defaultName += m.getName();
							if (!majors.isEmpty()) majors += "<br>";
							majors += m.getName();
						}
					}
					if (defaultName.length() > 100) defaultName = defaultName.substring(0, 100);
					if (defaultAbbv.length() > 40) defaultAbbv = defaultAbbv.substring(0, 40);
					if (iDefaultAbbv) iCurriculumAbbv.getWidget().setText(defaultAbbv);
					if (iDefaultName) iCurriculumName.getWidget().setText(defaultName);
					iCurriculumMajors.setPrintText(majors);
				} catch (Exception e) {}
				loadEnrollments(true, true);
			}
		});

		iCurriculumDept = new UniTimeWidget<ListBox>(new ListBox());
		iCurriculumDept.getWidget().setMultipleSelect(false);
		iCurriculumDept.getWidget().setWidth("300px");
		iCurriculumDept.getWidget().setStyleName("unitime-TextBox");
		iCurriculumDept.getWidget().setVisibleItemCount(1);
		iCurriculaTable.addRow(MESSAGES.propDepartment(), iCurriculumDept);
		
		iCurriculaTable.addRow(MESSAGES.propLastChange(), new Label("",false));
		iCurriculaTable.getRowFormatter().setVisible(7, false);
		
		iCurriculumDept.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCurriculumDept.clearHint();
			}
		});
		
		iCurriculaTable.addHeaderRow(MESSAGES.headerCurriculumClassifications());
		
		iCurriculumClasfTable = new CurriculaClassificationsPanel(new CurriculaClassifications());
		
		iCurriculaTable.addRow(iCurriculumClasfTable);
		

		iCurriculumCourses = new CurriculaCourses();

		iCurriculaTable.addHeaderRow(MESSAGES.headerCourseProjections());
	
		iCurriculaTable.addRow(iCurriculumCourses);
		
		iCurriculaTable.addNotPrintableBottomRow(iTitleAndButtons.clonePanel(null));
		
		initWidget(iCurriculaTable);
	}
	
	public Mode getMode() { return iMode; }
	
	private void loadCurriculum(Mode mode) {
		iMode = mode;

		if (iMode.hasTitle())
			UniTimePageLabel.getInstance().setPageName(iMode.getTitle());
		
		if (iCurriculum.getId() == null) {
			iDefaultAbbv = true; iDefaultName = true;
		} else {
			iDefaultAbbv = false; iDefaultName = false;
		}
		if (iDepts.isEmpty() || iAreas.isEmpty())
			iCurriculum.setEditable(false);
		iCurriculumAbbv.clearHint();
		iCurriculumName.clearHint();
		iCurriculumArea.clearHint();
		iMultipleMajor.clearHint();
		iCurriculumDept.clearHint();
		iCurriculumClasfTable.clearHint();

		iTitleAndButtons.clearMessage();
		iTitleAndButtons.setEnabled("delete", iMode == Mode.DETAILS && iCurriculum.getId() != null && iCurriculum.isEditable());
		iTitleAndButtons.setEnabled("save", iCurriculum.isEditable() && iMode.isEditable());
		iTitleAndButtons.setEnabled("edit", iCurriculum.isEditable() && !iMode.isEditable());
		iTitleAndButtons.setEnabled("print", iMode == Mode.DETAILS);
		iTitleAndButtons.setEnabled("previous", iNavigation != null && iNavigation.previous(iCurriculum) != null);
		iTitleAndButtons.setEnabled("next", iNavigation != null && iNavigation.next(iCurriculum) != null);

		if (iCurriculum.hasLastChange() && iMode == Mode.DETAILS) {
			((Label)iCurriculaTable.getWidget(7, 1)).setText(iCurriculum.getLastChange());
			iCurriculaTable.getRowFormatter().setVisible(7, true);
		} else {
			iCurriculaTable.getRowFormatter().setVisible(7, false);
		}

		iCurriculumAbbv.getWidget().setText(iCurriculum.getAbbv());
		iCurriculumAbbv.getWidget().setReadOnly(!iCurriculum.isEditable() || !iMode.areDetailsEditable() || !iMode.isEditable());
		iCurriculumName.getWidget().setText(iCurriculum.getName());
		iCurriculumName.getWidget().setReadOnly(!iCurriculum.isEditable() || !iMode.areDetailsEditable() || !iMode.isEditable());
		iCurriculumArea.getWidget().setSelectedIndex(0);
		if (iCurriculum.getAcademicArea() != null) {
			for (int i = 0; i < iAreas.size(); i++)
				if (iAreas.get(i).getId().equals(iCurriculum.getAcademicArea().getId()))
					iCurriculumArea.getWidget().setSelectedIndex(1 + i);
		}
		iCurriculumArea.setText(iCurriculum.getAcademicArea() == null ? "" : iCurriculum.getAcademicArea().getName());
		iCurriculumArea.setReadOnly(!iCurriculum.isEditable() || !iMode.areDetailsEditable() || !iMode.isEditable());
		iCurriculumDept.getWidget().setSelectedIndex(0);
		if (iCurriculum.getDepartment() != null) {
			for (int i = 0; i < iDepts.size(); i++)
				if (iDepts.get(i).getId().equals(iCurriculum.getDepartment().getId()))
					iCurriculumDept.getWidget().setSelectedIndex(1 + i);
		}
		iCurriculumDept.setText(iCurriculum.getDepartment() == null ? "" : iCurriculum.getDepartment().getLabel());
		iCurriculumDept.setReadOnly(!iCurriculum.isEditable() || !iMode.areDetailsEditable() || !iMode.isEditable());
		
		iMultipleMajor.setReadOnly(!iCurriculum.isEditable() || !iMode.areDetailsEditable() || !iMode.isEditable());
		iMultipleMajor.getWidget().setValue(iCurriculum.isMultipleMajors());
		iMultipleMajor.getWidget().setText(iCurriculum.isMultipleMajors() ? MESSAGES.infoMultipleMajorsOn() : MESSAGES.infoMultipleMajorsOff());
		iMultipleMajor.setReadOnlyWidget(new Check(iCurriculum.isMultipleMajors(), MESSAGES.infoMultipleMajorsOn(), MESSAGES.infoMultipleMajorsOff()));

		iCurriculumMajors.setReadOnly(!iCurriculum.isEditable() || !iMode.areDetailsEditable() || !iMode.isEditable());
		iCurriculumMajors.setText(iCurriculum.getCodeMajorNames("<br>"));
		iCurriculumMajors.setPrintText(iCurriculum.getCodeMajorNames("<br>"));
		loadMajors(iMode.areDetailsEditable(), false);
		iCurriculumClasfTable.populate(iCurriculum.getClassifications());
		iCurriculumClasfTable.setReadOnly(!iCurriculum.isEditable() || !iMode.isEditable());
		iCurriculumCourses.populate(iCurriculum, iMode.isEditable());
		for (int col = 0; col < iClassifications.size(); col++) {
			if (iCurriculumClasfTable.getWidget().getExpected(col) == null)
				iCurriculumCourses.setVisible(col, false);
		}
	}
	
	public boolean saveCurriculum() {
		boolean ret = true;

		iCurriculum.setAbbv(iCurriculumAbbv.getWidget().getText());
		if (iCurriculum.getAbbv().isEmpty()) {
			iCurriculumAbbv.setErrorHint(MESSAGES.hintCurriculumAbbreviationNotSet());
			ret = false;
		}

		iCurriculum.setName(iCurriculumName.getWidget().getText());
		if (iCurriculum.getName().isEmpty()) {
			iCurriculumName.setErrorHint(MESSAGES.hintCurriculumNameNotSet());
			ret = false;
		}
		
		iCurriculum.setMultipleMajors(iMultipleMajor.getWidget().getValue());
		
		if (iCurriculumArea.getWidget().getSelectedIndex() <= 0) {
			iCurriculumArea.setErrorHint(MESSAGES.hintAcademicAreaNotSelected());
			ret = false;
		} else {
			AcademicAreaInterface a = new AcademicAreaInterface();
			a.setId(Long.valueOf(iCurriculumArea.getWidget().getValue(iCurriculumArea.getWidget().getSelectedIndex())));
			iCurriculum.setAcademicArea(a);
		}
		
		if (iCurriculum.hasMajors()) { iCurriculum.getMajors().clear(); }
		for (int i = 0; i < iCurriculumMajors.getWidget().getItemCount(); i++) {
			if (iCurriculumMajors.getWidget().isItemSelected(i)) {
				MajorInterface m = new MajorInterface();
				m.setId(Long.valueOf(iCurriculumMajors.getWidget().getValue(i)));
				iCurriculum.addMajor(m);
			}
		}
		if (!iCurriculum.hasMajors() && !iMultipleMajor.getWidget().getValue())
			for (int i = 0; i < iCurriculumMajors.getWidget().getItemCount(); i++) {
				MajorInterface m = new MajorInterface();
				m.setId(Long.valueOf(iCurriculumMajors.getWidget().getValue(i)));
				iCurriculum.addMajor(m);
			}
		
		if (iCurriculumMajors.getWidget().getItemCount() == 0 && iCurriculumArea.getWidget().getSelectedIndex() > 0 && !iAreaHasNoMajors && !iMultipleMajor.getWidget().getValue()) {
			iCurriculumArea.setErrorHint(MESSAGES.hintAcademicAreaHasNoMajors());
			ret = false;
		}
		
		if (iCurriculumDept.getWidget().getSelectedIndex() <= 0) {
			iCurriculumDept.setErrorHint(MESSAGES.hintControllingDepartmentNotSelected());
			ret = false;
		} else {
			DepartmentInterface d = new DepartmentInterface();
			d.setId(Long.valueOf(iCurriculumDept.getWidget().getValue(iCurriculumDept.getWidget().getSelectedIndex())));
			iCurriculum.setDepartment(d);
		}
		
		if (!iCurriculumClasfTable.getWidget().saveCurriculum(iCurriculum)) {
			ret = false;
		}
		
		if (!iCurriculum.hasClassifications()) {
			iCurriculumClasfTable.setErrorHint(MESSAGES.hintNoStudentExpectations());
			ret = false;
		}
		
		if (!iCurriculumCourses.saveCurriculum(iCurriculum)) {
			ret = false;
		}
		
		return ret;
	}

	private void loadMajors(final boolean showEmptyCourses, final boolean changed) {
		if (iCurriculumArea.getWidget().getSelectedIndex() > 0) {
			showLoading("Loading majors ...");
			iService.loadMajors(iCurriculum.getId(),
					Long.valueOf(iCurriculumArea.getWidget().getValue(iCurriculumArea.getWidget().getSelectedIndex())),
					iMultipleMajor.getWidget().getValue(),
					new AsyncCallback<TreeSet<MajorInterface>>() {

						@Override
						public void onFailure(Throwable caught) {
							hideLoading();
						}

						@Override
						public void onSuccess(TreeSet<MajorInterface> result) {
							if (result == null) {
								iAreaHasNoMajors = true;
								result = new TreeSet<MajorInterface>();
							} else {
								iAreaHasNoMajors = false;
							}
							String defaultAbbv = "", defaultName = "";
							AcademicAreaInterface area = null;
							try {
								if (iCurriculumArea.getWidget().getSelectedIndex() > 0) {
									area = iAreas.get(iCurriculumArea.getWidget().getSelectedIndex() - 1);
									defaultAbbv = area.getAbbv();
									defaultName = area.getName();
								}
							} catch (Exception e) {}
							
							iMajors.clear(); iMajors.addAll(result);
							iCurriculumMajors.getWidget().clear();
							int idx = 0;
							boolean allSelected = true;
							CurriculumCookie.getInstance().getCurriculaDisplayMode();
							for (MajorInterface m: result) {
								iCurriculumMajors.getWidget().addItem(m.getCode() + " - " + m.getName(), m.getId().toString());
								if (iCurriculum != null && iCurriculum.hasMajors()) {
									iCurriculumMajors.getWidget().setItemSelected(idx, iCurriculum.getMajors().contains(m));
									if (iCurriculum.getMajors().contains(m)) {
										if (!defaultAbbv.contains("/")) { defaultAbbv += "/"; defaultName += " / "; }
										else { defaultAbbv += ","; defaultName += ", "; }
										defaultAbbv += m.getCode();
										defaultName += m.getName();
									} else {
										allSelected = false;
									}
								}
								idx++;
							}
							if (defaultName.length() > 100) defaultName = defaultName.substring(0, 100);
							if (defaultAbbv.length() > 40) defaultAbbv = defaultAbbv.substring(0, 40);
							iDefaultAbbv = defaultAbbv.equals(iCurriculumAbbv.getWidget().getText());
							iDefaultName = defaultName.equalsIgnoreCase(iCurriculumName.getWidget().getText());
							if (!iDefaultAbbv && allSelected && area != null && area.getAbbv().equals(iCurriculumAbbv.getWidget().getText()))
								iDefaultAbbv = true;
							if (!iDefaultName && allSelected && area != null && area.getName().equalsIgnoreCase(iCurriculumName.getWidget().getText()))
								iDefaultName = true;
							iCurriculumMajors.getWidget().setVisibleItemCount(iCurriculumMajors.getWidget().getItemCount() <= 3 ? 3 : iCurriculumMajors.getWidget().getItemCount() > 10 ? 10 : iCurriculumMajors.getWidget().getItemCount());
							loadEnrollments(showEmptyCourses, changed);
							hideLoading();
						}
					});
		} else {
			iCurriculumMajors.getWidget().clear();
		}
	}
	
	private void loadEnrollments(final boolean showEmptyCourses, final boolean changed) {
		if (iCurriculumArea.getWidget().getSelectedIndex() >= 0) {
			final Long areaId = Long.valueOf(iCurriculumArea.getWidget().getValue(iCurriculumArea.getWidget().getSelectedIndex()));
			final List<Long> majorIds = new ArrayList<Long>();
			for (int i = 0; i < iCurriculumMajors.getWidget().getItemCount(); i++) {
				if (iCurriculumMajors.getWidget().isItemSelected(i)) majorIds.add(Long.valueOf(iCurriculumMajors.getWidget().getValue(i)));
			}
			
			if (majorIds.isEmpty() && !iMultipleMajor.getWidget().getValue()) {
				for (int i = 0; i < iCurriculumMajors.getWidget().getItemCount(); i++) {
					majorIds.add(Long.valueOf(iCurriculumMajors.getWidget().getValue(i)));
				}
			}
			if (majorIds.isEmpty() && !iMultipleMajor.getWidget().getValue() && !iAreaHasNoMajors) return;
			
			showLoading(MESSAGES.waitLoadingCourseEnrollments());
			iService.computeEnrollmentsAndLastLikes(areaId, majorIds, iMultipleMajor.getWidget().getValue(), new AsyncCallback<HashMap<String,CurriculumStudentsInterface[]>>() {

				@Override
				public void onFailure(Throwable caught) {
					hideLoading();
				}

				@Override
				public void onSuccess(final HashMap<String, CurriculumStudentsInterface[]> result) {
					if (iMultipleMajor.getWidget().getValue() && iMode.isEditable() && changed) {
						iService.loadTemplate(areaId, majorIds, new AsyncCallback<CurriculumInterface>() {
							@Override
							public void onSuccess(CurriculumInterface template) {
								iCurriculumCourses.populateTemplate(template);
								CurriculumStudentsInterface[] x = result.get("");
								for (int col = 0; col < iClassifications.size(); col++) {
									iCurriculumClasfTable.getWidget().setEnrollment(col, x == null || x[col] == null ? null : x[col].getEnrollment());
									iCurriculumClasfTable.getWidget().setLastLike(col, x == null || x[col] == null ? null : x[col].getLastLike());
									iCurriculumClasfTable.getWidget().setProjection(col, x == null || x[col] == null ? null : x[col].getProjection());
									iCurriculumClasfTable.getWidget().setRequested(col, x == null || x[col] == null ? null : x[col].getRequested());
								}
								iCurriculumCourses.updateEnrollmentsAndLastLike(result, showEmptyCourses);
								if (iCurriculumClasfTable.isShowingAllColumns())
									iCurriculumClasfTable.getWidget().showAllColumns();
								else
									iCurriculumClasfTable.getWidget().hideEmptyColumns();
								iCurriculumClasfTable.getWidget().hideEmptyRows();
								hideLoading();
							}
							
							@Override
							public void onFailure(Throwable caught) {
								CurriculumStudentsInterface[] x = result.get("");
								for (int col = 0; col < iClassifications.size(); col++) {
									iCurriculumClasfTable.getWidget().setEnrollment(col, x == null || x[col] == null ? null : x[col].getEnrollment());
									iCurriculumClasfTable.getWidget().setLastLike(col, x == null || x[col] == null ? null : x[col].getLastLike());
									iCurriculumClasfTable.getWidget().setProjection(col, x == null || x[col] == null ? null : x[col].getProjection());
									iCurriculumClasfTable.getWidget().setRequested(col, x == null || x[col] == null ? null : x[col].getRequested());
								}
								iCurriculumCourses.updateEnrollmentsAndLastLike(result, showEmptyCourses);
								if (iCurriculumClasfTable.isShowingAllColumns())
									iCurriculumClasfTable.getWidget().showAllColumns();
								else
									iCurriculumClasfTable.getWidget().hideEmptyColumns();
								iCurriculumClasfTable.getWidget().hideEmptyRows();
								hideLoading();
							}
						});
					} else {
						CurriculumStudentsInterface[] x = result.get("");
						for (int col = 0; col < iClassifications.size(); col++) {
							iCurriculumClasfTable.getWidget().setEnrollment(col, x == null || x[col] == null ? null : x[col].getEnrollment());
							iCurriculumClasfTable.getWidget().setLastLike(col, x == null || x[col] == null ? null : x[col].getLastLike());
							iCurriculumClasfTable.getWidget().setProjection(col, x == null || x[col] == null ? null : x[col].getProjection());
							iCurriculumClasfTable.getWidget().setRequested(col, x == null || x[col] == null ? null : x[col].getRequested());
						}
						iCurriculumCourses.updateEnrollmentsAndLastLike(result, showEmptyCourses);
						if (iCurriculumClasfTable.isShowingAllColumns())
							iCurriculumClasfTable.getWidget().showAllColumns();
						else
							iCurriculumClasfTable.getWidget().hideEmptyColumns();
						iCurriculumClasfTable.getWidget().hideEmptyRows();
						hideLoading();
					}
				}
			});
		}
	}
	
	public void showLoading(String message) { LoadingWidget.getInstance().show(message); }
	
	public void hideLoading() { LoadingWidget.getInstance().hide(); }
	
	public static class EditFinishedEvent {
		
	}
	
	public static interface EditFinishedHandler {
		public void onBack(EditFinishedEvent evt);
		public void onDelete(EditFinishedEvent evt);
		public void onSave(EditFinishedEvent evt);
	}
	
	public void addEditFinishedHandler(EditFinishedHandler h) {
		iEditFinishedHandlers.add(h);
	}
	
	public void addNew() {
		iSaved = false;
		iCurriculum = new CurriculumInterface();
		iCurriculum.setEditable(true);
		if (iDepts.size() == 1) {
			DepartmentInterface d = iDepts.get(0);
			iCurriculumDept.getWidget().setSelectedIndex(1);
			iCurriculum.setDepartment(d);
		}
		loadCurriculum(Mode.ADD);
	}
	
	public void edit(CurriculumInterface curriculum, boolean detailsEditable) {
		iSaved = false;
		iCurriculum = curriculum;
		loadCurriculum(detailsEditable ? Mode.DETAILS : Mode.DIALOG);
	}

	public void setupAreas(TreeSet<AcademicAreaInterface> result) {
		iAreas.clear(); iAreas.addAll(result);
		iCurriculumArea.getWidget().clear();
		iCurriculumArea.getWidget().addItem(MESSAGES.itemSelect(), "");
		for (AcademicAreaInterface area: result) {
			iCurriculumArea.getWidget().addItem(area.getAbbv() + " - " + area.getName(), area.getId().toString());
		}
	}
	
	public void setupDepartments(TreeSet<DepartmentInterface> result) {
		iDepts.clear(); iDepts.addAll(result);
		iCurriculumDept.getWidget().clear();
		iCurriculumDept.getWidget().addItem(MESSAGES.itemSelect(), "");
		for (DepartmentInterface dept: result) {
			iCurriculumDept.getWidget().addItem(dept.getLabel(), dept.getId().toString());
		}
	}
	
	public void setupClassifications(TreeSet<AcademicClassificationInterface> result) {
		iClassifications.clear(); iClassifications.addAll(result);
		iCurriculumClasfTable.getWidget().setup(iClassifications);
		iCurriculumCourses.link(iCurriculumClasfTable.getWidget());
	}
	
	public void showOnlyCourses(TreeSet<CourseInterface> courses) {
		iCurriculumCourses.showOnlyCourses(courses);
	}
	
	private static class CurriculaClassificationsPanel extends UniTimeWidget<CurriculaClassifications> {
		private Label iHint;
		
		public CurriculaClassificationsPanel(CurriculaClassifications classifications) {
			super(classifications);
			
			iHint = new Label();
			iHint.setStyleName("unitime-Hint");
			iHint.setVisible(true);
			iHint.addStyleName("unitime-NoPrint");
			getPanel().insert(iHint, 1);
			
			iHint.setText(MESSAGES.hintShowAllColumns());
			iHint.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (isShowingAllColumns()) {
						getWidget().hideEmptyColumns();
						iHint.setText(MESSAGES.hintShowAllColumns());
					} else {
						getWidget().showAllColumns();
						iHint.setText(MESSAGES.hintHideEmptyColumns());
					}
				}
			});
			
			getWidget().addExpectedChangedHandler(new CurriculaClassifications.ExpectedChangedHandler() {
				@Override
				public void expectedChanged(ExpectedChangedEvent e) {
					clearHint();
				}
			});
			
			getWidget().addNameChangedHandler(new CurriculaClassifications.NameChangedHandler() {
				@Override
				public void nameChanged(NameChangedEvent e) {
					clearHint();
				}
			});
		}
		
		public boolean isShowingAllColumns() { return iHint.getText().equals(MESSAGES.hintHideEmptyColumns()); }
		
		public void populate(TreeSet<CurriculumClassificationInterface> classifications) {
			getWidget().populate(classifications);
			if (isShowingAllColumns())
				getWidget().showAllColumns();
			else
				getWidget().hideEmptyColumns();
			getWidget().hideEmptyRows();
		}
		
		public void setReadOnly(boolean readOnly) {
			getWidget().setEnabled(!readOnly);
			iHint.setVisible(!readOnly);
		}
	}
	
	public void reload(final Mode mode) {
		showLoading(MESSAGES.waitLoadingCurriculumWithName(iCurriculum.getName()));
		iService.loadCurriculum(iCurriculum.getId(), new AsyncCallback<CurriculumInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				hideLoading();
			}
			@Override
			public void onSuccess(CurriculumInterface result) {
				iCurriculum = result;
				loadCurriculum(mode);
				hideLoading();
			}
		});
	}
	
	public static interface NavigationProvider {
		public CurriculumInterface previous(CurriculumInterface curriculum);
		public CurriculumInterface next(CurriculumInterface curriculum);
		public void onChange(CurriculumInterface curriculum);
	}
	
	public CurriculumInterface getCurriculum() {
		return iCurriculum;
	}
	
	static class Check extends P {
		Check(boolean value, String onMessage, String offMessage) {
			Image image = new Image(value ? RESOURCES.on() : RESOURCES.off());
			image.addStyleName("image");
			add(image);
			InlineHTML text = new InlineHTML(value ? onMessage : offMessage);
			text.addStyleName("message");
			add(text);
			if (value)
				addStyleName("check-enabled");
			else
				addStyleName("check-disabled");
		}
	}
}
