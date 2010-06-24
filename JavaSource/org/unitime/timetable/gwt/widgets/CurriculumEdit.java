package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumStudentsInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.MajorInterface;
import org.unitime.timetable.gwt.widgets.CurriculaClassifications.ExpectedChangedEvent;
import org.unitime.timetable.gwt.widgets.CurriculaClassifications.NameChangedEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CurriculumEdit extends Composite {
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);

	private VerticalPanel iCurriculumPanel = null;
	
	private Button[] iSave = null;
	private Button[] iDelete = null;
	private Button[] iBack = null;
	private Label[] iSaveError = null;
	private HorizontalPanel[] iButtons = null;

	private TextBox iCurriculumAbbv = null, iCurriculumName = null;
	private Label iCurriculumAbbvError = null, iCurriculumNameError = null, iCurriculumAreaError = null, iCurriculumDeptError = null, iCurriculumClasfTableError = null;
	private Label iCurriculumAreaLabel = null, iCurriculumDeptLabel = null, iCurriculumClasfTableHint = null;
	private HTML iCurriculumMajorsHTML = null, iCurriculumMajorsPrint = null;
	private ListBox iCurriculumArea = null, iCurriculumDept = null, iCurriculumMajors = null;
	private CurriculaClassifications iCurriculumClasfTable = null;
	private CheckBox iCurriculaPercent = null;
	private boolean iDefaultAbbv = false, iDefaultName = false;

	private List<AcademicAreaInterface> iAreas = new ArrayList<AcademicAreaInterface>();
	private List<DepartmentInterface> iDepts = new ArrayList<DepartmentInterface>();
	private List<MajorInterface> iMajors = new ArrayList<MajorInterface>();
	private List<AcademicClassificationInterface> iClassifications = new ArrayList<AcademicClassificationInterface>();
	private CurriculumInterface iCurriculum = null;
	
	private CurriculaCourses iCurriculumCourses;
	
	private List<EditFinishedHandler> iEditFinishedHandlers = new ArrayList<EditFinishedHandler>();
	
	public CurriculumEdit() {
		
		iCurriculumPanel = new VerticalPanel();
		
		FlexTable curriculaTable = new FlexTable();
		
		curriculaTable.setStylePrimaryName("unitime-MainTable");
		curriculaTable.addStyleName("unitime-NotPrintableBottomLine");
		curriculaTable.setCellPadding(2);
		curriculaTable.setCellSpacing(0);
		curriculaTable.setWidth("100%");
		
		int idx = 0;
		
		iSave = new Button[] {new Button("<u>S</u>ave"), new Button("<u>S</u>ave")};
		iSave[0].setAccessKey('s');
		iDelete = new Button[] {new Button("<u>D</u>elete"), new Button("<u>D</u>elete")};
		iDelete[0].setAccessKey('d');
		iBack = new Button[] {new Button("<u>B</u>ack"), new Button("<u>B</u>ack")};
		iBack[0].setAccessKey('c');
		iSaveError = new Label[] { new Label(), new Label() };
		iButtons = new HorizontalPanel[] {new HorizontalPanel(), new HorizontalPanel()};
		for (int i = 0; i < 2; i++) {
			iSave[i].setWidth("75px");
			iDelete[i].setWidth("75px");
			iBack[i].setWidth("75px");
			iSaveError[i].setStyleName("unitime-ErrorMessage");
			iButtons[i].add(iSave[i]);
			iSave[i].getElement().getStyle().setMarginRight(4, Unit.PX);
			iButtons[i].add(iDelete[i]);
			iDelete[i].getElement().getStyle().setMarginRight(4, Unit.PX);
			iButtons[i].add(iBack[i]);
			iButtons[i].addStyleName("unitime-NoPrint");
		}
		
		HorizontalPanel titleAndButtons = new HorizontalPanel();
		Label title = new Label("Curriculum Details", false);
		title.setStyleName("unitime-MainHeader");
		titleAndButtons.add(title);
		titleAndButtons.add(iSaveError[0]);
		titleAndButtons.add(iButtons[0]);
		titleAndButtons.setCellHorizontalAlignment(iButtons[0], HasHorizontalAlignment.ALIGN_RIGHT);
		titleAndButtons.setWidth("100%");
		curriculaTable.getFlexCellFormatter().setColSpan(idx, 0, 2);
		curriculaTable.getFlexCellFormatter().setStyleName(idx, 0, "unitime-MainTableHeader");
		curriculaTable.getRowFormatter().setStyleName(idx, "unitime-MainTableHeaderRow");
		curriculaTable.setWidget(idx++, 0, titleAndButtons);
		
		curriculaTable.setText(idx, 0, "Abbreviation:");
		VerticalPanel curriculumAbbvVP = new VerticalPanel();
		iCurriculumAbbv = new MyTextBox();
		iCurriculumAbbv.setWidth("200px");
		iCurriculumAbbv.setMaxLength(20);
		curriculumAbbvVP.add(iCurriculumAbbv);
		iCurriculumAbbvError = new Label();
		iCurriculumAbbvError.setStyleName("unitime-ErrorHint");
		iCurriculumAbbvError.setVisible(false);
		curriculumAbbvVP.add(iCurriculumAbbvError);
		curriculaTable.setWidget(idx++, 1, curriculumAbbvVP);
		iCurriculumAbbv.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iDefaultAbbv = false;
				iCurriculumAbbvError.setVisible(false);
			}
		});

		curriculaTable.setText(idx, 0, "Name:");
		VerticalPanel curriculumNameVP = new VerticalPanel();
		iCurriculumName = new MyTextBox();
		iCurriculumName.setWidth("500px");
		iCurriculumName.setMaxLength(60);
		curriculumNameVP.add(iCurriculumName);
		iCurriculumNameError = new Label();
		iCurriculumNameError.setStyleName("unitime-ErrorHint");
		iCurriculumNameError.setVisible(false);
		curriculumNameVP.add(iCurriculumNameError);
		curriculaTable.setWidget(idx++, 1, curriculumNameVP);
		iCurriculumName.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iDefaultName = false;
				iCurriculumNameError.setVisible(false);
			}
		});

		curriculaTable.setText(idx, 0, "Academic Area:");
		VerticalPanel curriculumAreaVP = new VerticalPanel();
		iCurriculumArea = new ListBox(false);
		iCurriculumArea.setWidth("300px");
		iCurriculumArea.setStyleName("unitime-TextBox");
		iCurriculumArea.setVisibleItemCount(1);
		curriculumAreaVP.add(iCurriculumArea);
		iCurriculumAreaLabel = new Label();
		iCurriculumAreaLabel.setStyleName("unitime-LabelInsteadEdit");
		iCurriculumAreaLabel.setVisible(false);
		curriculumAreaVP.add(iCurriculumAreaLabel);
		iCurriculumAreaError = new Label();
		iCurriculumAreaError.setStyleName("unitime-ErrorHint");
		iCurriculumAreaError.setVisible(false);
		curriculumAreaVP.add(iCurriculumAreaError);
		curriculaTable.setWidget(idx++, 1, curriculumAreaVP);
		iCurriculumArea.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if (iDefaultName || iDefaultAbbv) {
					try {
						AcademicAreaInterface area = iAreas.get(iCurriculumArea.getSelectedIndex());
						if (iDefaultAbbv) iCurriculumAbbv.setText(area.getAbbv());
						if (iDefaultName) iCurriculumName.setText(area.getName());
					} catch (Exception e) {}
				}
				iCurriculumAreaError.setVisible(false);
				loadMajors(true);
			}
		});


		curriculaTable.setText(idx, 0, "Major(s):");
		VerticalPanel curriculumMajorsVP = new VerticalPanel();
		iCurriculumMajors = new ListBox(true);
		iCurriculumMajors.setWidth("300px");
		iCurriculumMajors.setStyleName("unitime-TextBox");
		iCurriculumMajors.setVisibleItemCount(3);
		iCurriculumMajors.setHeight("100%");
		iCurriculumMajors.addStyleName("unitime-NoPrint");
		curriculumMajorsVP.add(iCurriculumMajors);
		iCurriculumMajorsHTML = new HTML();
		iCurriculumMajorsHTML.setStyleName("unitime-LabelInsteadEdit");
		iCurriculumMajorsHTML.setVisible(false);
		iCurriculumMajorsHTML.addStyleName("unitime-NoPrint");
		curriculumMajorsVP.add(iCurriculumMajorsHTML);
		iCurriculumMajorsPrint = new HTML();
		iCurriculumMajorsPrint.setStyleName("unitime-LabelInsteadEdit");
		iCurriculumMajorsPrint.addStyleName("unitime-Print");
		curriculumMajorsVP.add(iCurriculumMajorsPrint);
		curriculaTable.setWidget(idx++, 1, curriculumMajorsVP);
		iCurriculumMajors.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				try {
					String defaultAbbv = "", defaultName = "";
					AcademicAreaInterface area = iAreas.get(iCurriculumArea.getSelectedIndex());
					defaultAbbv = area.getAbbv();
					defaultName = area.getName();
					String majors = "";
					for (int i = 0; i < iCurriculumMajors.getItemCount(); i++) {
						if (iCurriculumMajors.isItemSelected(i)) {
							MajorInterface m = iMajors.get(i);
							if (!defaultAbbv.contains("/")) { defaultAbbv += "/"; defaultName += " / "; }
							else { defaultAbbv += ","; defaultName += ", "; }
							defaultAbbv += m.getCode();
							defaultName += m.getName();
							if (!majors.isEmpty()) majors += "<br>";
							majors += m.getName();
						}
					}
					if (defaultName.length() > 60) defaultName = defaultName.substring(0, 60);
					if (iDefaultAbbv) iCurriculumAbbv.setText(defaultAbbv);
					if (iDefaultName) iCurriculumName.setText(defaultName);
					iCurriculumMajorsPrint.setHTML(majors);
				} catch (Exception e) {}
				loadEnrollments();
			}
		});

		curriculaTable.setText(idx, 0, "Department:");
		VerticalPanel curriculumDeptVP = new VerticalPanel();
		iCurriculumDept = new ListBox(false);
		iCurriculumDept.setWidth("300px");
		iCurriculumDept.setStyleName("unitime-TextBox");
		iCurriculumDept.setVisibleItemCount(1);
		curriculumDeptVP.add(iCurriculumDept);
		iCurriculumDeptLabel = new Label();
		iCurriculumDeptLabel.setStyleName("unitime-LabelInsteadEdit");
		iCurriculumDeptLabel.setVisible(false);
		curriculumDeptVP.add(iCurriculumDeptLabel);
		iCurriculumDeptError = new Label();
		iCurriculumDeptError.setStyleName("unitime-ErrorHint");
		iCurriculumDeptError.setVisible(false);
		curriculumDeptVP.add(iCurriculumDeptError);
		curriculaTable.setWidget(idx++, 1, curriculumDeptVP);
		iCurriculumDept.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCurriculumDeptError.setVisible(false);
			}
		});

		curriculaTable.getFlexCellFormatter().setColSpan(idx, 0, 2);
		curriculaTable.getFlexCellFormatter().setStyleName(idx, 0, "unitime-MainTableHeader");
		curriculaTable.getRowFormatter().setStyleName(idx, "unitime-MainTableHeaderRow");
		curriculaTable.setText(idx++, 0, "Curriculum Classifications");
		
		iCurriculumClasfTable = new CurriculaClassifications();
		
		VerticalPanel curriculumClasfTableVP = new VerticalPanel();
		curriculumClasfTableVP.add(iCurriculumClasfTable);
		iCurriculumClasfTableHint = new Label();
		iCurriculumClasfTableHint.setStyleName("unitime-Hint");
		iCurriculumClasfTableHint.setVisible(true);
		curriculumClasfTableVP.add(iCurriculumClasfTableHint);
		iCurriculumClasfTableError = new Label();
		iCurriculumClasfTableError.setStyleName("unitime-ErrorHint");
		iCurriculumClasfTableError.setVisible(false);
		curriculumClasfTableVP.add(iCurriculumClasfTableError);
		
		iCurriculumClasfTableHint.setText("Show all columns.");
		iCurriculumClasfTableHint.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (iCurriculumClasfTableHint.getText().equals("Show all columns.")) {
					iCurriculumClasfTable.showAllColumns();
					iCurriculumClasfTableHint.setText("Hide empty columns.");
				} else {
					iCurriculumClasfTable.hideEmptyColumns();
					iCurriculumClasfTableHint.setText("Show all columns.");
				}
			}
		});

		curriculaTable.getFlexCellFormatter().setColSpan(idx, 0, 2);
		curriculaTable.setWidget(idx++, 0, curriculumClasfTableVP);
		iCurriculumClasfTable.addExpectedChangedHandler(new CurriculaClassifications.ExpectedChangedHandler() {
			@Override
			public void expectedChanged(ExpectedChangedEvent e) {
				iCurriculumClasfTableError.setVisible(false);
			}
		});
		iCurriculumClasfTable.addNameChangedHandler(new CurriculaClassifications.NameChangedHandler() {
			@Override
			public void nameChanged(NameChangedEvent e) {
				iCurriculumClasfTableError.setVisible(false);
			}
		});
		

		iCurriculumCourses = new CurriculaCourses();

		curriculaTable.getFlexCellFormatter().setColSpan(idx, 0, 2);
		curriculaTable.getFlexCellFormatter().setStyleName(idx, 0, "unitime-MainTableHeader");
		curriculaTable.getRowFormatter().setStyleName(idx, "unitime-MainTableHeaderRow");
		curriculaTable.setText(idx++, 0, "Course Projections");
		
		/*
		curriculaTable.setText(idx, 0, "Display:");
		iCurriculaPercent = new CheckBox("%");
		iCurriculaPercent.setValue(true);
		iCurriculaPercent.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iCurriculumCourses.setPercent(iCurriculaPercent.getValue());
			}
		});
		curriculaTable.setWidget(idx++, 1, iCurriculaPercent);
		
		final ListBox mode = new ListBox(false);
		for (String m: CurriculaCourses.MODES)
			mode.addItem(m);
		mode.setSelectedIndex(0);
		mode.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCurriculumCourses.setMode(mode.getSelectedIndex());
			}
		});
		curriculaTable.setText(idx, 0, "Compare with:");
		curriculaTable.setWidget(idx++, 1, mode);
		 */
		
		curriculaTable.getFlexCellFormatter().setColSpan(idx, 0, 2);
		curriculaTable.setWidget(idx++, 0, iCurriculumCourses);
		
		/*
		final Button add = new Button("+");
		add.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iCurriculumCourses.addBlankLine();
			}
		});
		curriculaTable.getFlexCellFormatter().setColSpan(idx, 0, 2);
		curriculaTable.getFlexCellFormatter().setHorizontalAlignment(idx, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		curriculaTable.setWidget(idx++, 0, add);
		*/
		
		iCurriculumPanel.add(curriculaTable);
		HorizontalPanel errorAndButtons = new HorizontalPanel();
		errorAndButtons.setWidth("100%");
		HTML blank = new HTML("&nbsp;");
		errorAndButtons.add(blank);
		errorAndButtons.setCellWidth(blank, "33%");
		errorAndButtons.setCellHorizontalAlignment(blank, HasHorizontalAlignment.ALIGN_LEFT);
		errorAndButtons.add(iSaveError[1]);
		errorAndButtons.setCellWidth(iSaveError[1], "34%");
		errorAndButtons.setCellHorizontalAlignment(iSaveError[1], HasHorizontalAlignment.ALIGN_CENTER);
		errorAndButtons.add(iButtons[1]);
		errorAndButtons.setCellHorizontalAlignment(iButtons[1], HasHorizontalAlignment.ALIGN_RIGHT);
		errorAndButtons.setCellWidth(iButtons[1], "33%");
		errorAndButtons.addStyleName("unitime-NoPrint");
		iCurriculumPanel.add(errorAndButtons);
		iButtons[1].getElement().getStyle().setMarginTop(2, Unit.PX);
		
		initWidget(iCurriculumPanel);
		
		ClickHandler backHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				EditFinishedEvent e = new EditFinishedEvent();
				for (EditFinishedHandler h: iEditFinishedHandlers) {
					h.onBack(e);
				}
			}
		};
		
		for (int i = 0; i < 2; i++ ) {
			iBack[i].addClickHandler(backHandler);
		}
		
		ClickHandler saveHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (saveCurriculum()) {
					showLoading("Saving curriculum " + iCurriculum.getName() + " ...");
					iService.saveCurriculum(iCurriculum, new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							hideLoading();
							for (int i = 0; i < 2; i++) {
								iSaveError[i].setText("Validation failed (" + caught.getMessage() + ").");
								iSaveError[i].setVisible(true);
							}
						}
						@Override
						public void onSuccess(Boolean result) {
							EditFinishedEvent e = new EditFinishedEvent();
							for (EditFinishedHandler h: iEditFinishedHandlers) {
								h.onSave(e);
							}
							hideLoading();
						}
					});
				} else {
					iSaveError[0].setText("Validation failed, see errors below.");
					iSaveError[1].setText("Validation failed, see errors above.");
					for (int i = 0; i < 2; i++)
						iSaveError[i].setVisible(true);
				}
			}
		};
		
		for (int i = 0; i < 2; i++ ) {
			iSave[i].addClickHandler(saveHandler);
		}
		
		ClickHandler deleteHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!Window.confirm("Do you realy want to delete this curriculum?")) return;
				showLoading("Deleting curriculum " + iCurriculum.getName() + " ...");
				iService.deleteCurriculum(iCurriculum.getId(), new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						for (int i = 0; i < 2; i++) {
							iSaveError[i].setText("Delete failed (" + caught.getMessage() + ").");
							iSaveError[i].setVisible(true);
						}
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
		
		for (int i = 0; i < 2; i++ ) {
			iDelete[i].addClickHandler(deleteHandler);
		}
	}
	
	private void loadCurriculum(boolean detailsEditable) {
		if (iCurriculum.getId() == null) {
			iDefaultAbbv = true; iDefaultName = true;
		} else {
			iDefaultAbbv = false; iDefaultName = false;
		}
		if (iDepts.isEmpty() || iAreas.isEmpty())
			iCurriculum.setEditable(false);
		iCurriculumAbbvError.setVisible(false);
		iCurriculumNameError.setVisible(false);
		iCurriculumAreaError.setVisible(false);
		iCurriculumDeptError.setVisible(false);
		iCurriculumClasfTableError.setVisible(false);
		for (int i = 0; i < 2; i ++) {
			iSaveError[i].setVisible(false);
			iDelete[i].setVisible(iCurriculum.getId() != null && iCurriculum.isEditable());
			iSave[i].setVisible(iCurriculum.isEditable());
		}
		iCurriculumAbbv.setText(iCurriculum.getAbbv());
		iCurriculumAbbv.setEnabled(iCurriculum.isEditable() && detailsEditable);
		iCurriculumName.setText(iCurriculum.getName());
		iCurriculumName.setEnabled(iCurriculum.isEditable() && detailsEditable);
		iCurriculumArea.setSelectedIndex(-1);
		if (iCurriculum.getAcademicArea() != null) {
			for (int i = 0; i < iAreas.size(); i++)
				if (iAreas.get(i).getId().equals(iCurriculum.getAcademicArea().getId()))
					iCurriculumArea.setSelectedIndex(i);
		}
		iCurriculumAreaLabel.setText(iCurriculum.getAcademicArea() == null ? "" : iCurriculum.getAcademicArea().getName());
		if (iCurriculum.isEditable() && detailsEditable) {
			iCurriculumArea.setVisible(true);
			iCurriculumAreaLabel.setVisible(false);
		} else {
			iCurriculumArea.setVisible(false);
			iCurriculumAreaLabel.setVisible(true);
		}
		iCurriculumDept.setSelectedIndex(-1);
		if (iCurriculum.getDepartment() != null) {
			for (int i = 0; i < iDepts.size(); i++)
				if (iDepts.get(i).getId().equals(iCurriculum.getDepartment().getId()))
					iCurriculumDept.setSelectedIndex(i);
		}
		iCurriculumDeptLabel.setText(iCurriculum.getDepartment() == null ? "" : iCurriculum.getDepartment().getLabel());
		if (iCurriculum.isEditable() && detailsEditable) {
			iCurriculumDept.setVisible(true);
			iCurriculumDeptLabel.setVisible(false);
		} else {
			iCurriculumDept.setVisible(false);
			iCurriculumDeptLabel.setVisible(true);
		}
		if (iCurriculum.isEditable() && detailsEditable) {
			iCurriculumMajors.setVisible(true);
			iCurriculumMajorsHTML.setVisible(false);
		} else {
			iCurriculumMajors.setVisible(false);
			iCurriculumMajorsHTML.setVisible(true);
		}
		iCurriculumMajorsHTML.setHTML(iCurriculum.getMajorNames("<br>"));
		iCurriculumMajorsPrint.setHTML(iCurriculum.getMajorNames("<br>"));
		loadMajors(detailsEditable);
		iCurriculumMajors.setEnabled(iCurriculum.isEditable() && detailsEditable);
		iCurriculumClasfTable.populate(iCurriculum.getClassifications());
		iCurriculumClasfTable.setEnabled(iCurriculum.isEditable());
		iCurriculumCourses.populate(iCurriculum);
		for (int col = 0; col < iClassifications.size(); col++) {
			if (iCurriculumClasfTable.getExpected(col) == null)
				iCurriculumCourses.setVisible(col, false);
		}
		if (iCurriculumClasfTableHint.getText().equals("Show all columns."))
			iCurriculumClasfTable.hideEmptyColumns();
		else
			iCurriculumClasfTable.showAllColumns();
	}
	
	public boolean saveCurriculum() {
		boolean ret = true;

		iCurriculum.setAbbv(iCurriculumAbbv.getText());
		if (iCurriculum.getAbbv().isEmpty()) {
			iCurriculumAbbvError.setText("Curriculum abbreviation must be filled in.");
			iCurriculumAbbvError.setVisible(true);
			ret = false;
		}

		iCurriculum.setName(iCurriculumName.getText());
		if (iCurriculum.getName().isEmpty()) {
			iCurriculumNameError.setText("Curriculum name must be filled in.");
			iCurriculumNameError.setVisible(true);
			ret = false;
		}
		
		if (iCurriculumArea.getSelectedIndex() < 0) {
			iCurriculumAreaError.setText("An academic area must be selected.");
			iCurriculumAreaError.setVisible(true);
			ret = false;
		} else {
			AcademicAreaInterface a = new AcademicAreaInterface();
			a.setId(Long.valueOf(iCurriculumArea.getValue(iCurriculumArea.getSelectedIndex())));
			iCurriculum.setAcademicArea(a);
		}
		
		if (iCurriculum.hasMajors()) { iCurriculum.getMajors().clear(); }
		for (int i = 0; i < iCurriculumMajors.getItemCount(); i++) {
			if (iCurriculumMajors.isItemSelected(i)) {
				MajorInterface m = new MajorInterface();
				m.setId(Long.valueOf(iCurriculumMajors.getValue(i)));
				iCurriculum.addMajor(m);
			}
		}
		if (!iCurriculum.hasMajors())
			for (int i = 0; i < iCurriculumMajors.getItemCount(); i++) {
				MajorInterface m = new MajorInterface();
				m.setId(Long.valueOf(iCurriculumMajors.getValue(i)));
				iCurriculum.addMajor(m);
			}
		
		if (iCurriculumMajors.getItemCount() == 0 && iCurriculumArea.getSelectedIndex() >= 0) {
			iCurriculumAreaError.setText("Selected academic area has no majors without a curriculum.");
			iCurriculumAreaError.setVisible(true);
			ret = false;
		}
		
		if (iCurriculumDept.getSelectedIndex() < 0) {
			iCurriculumDeptError.setText("A controlling department must be selected.");
			iCurriculumDeptError.setVisible(true);
			ret = false;
		} else {
			DepartmentInterface d = new DepartmentInterface();
			d.setId(Long.valueOf(iCurriculumDept.getValue(iCurriculumDept.getSelectedIndex())));
			iCurriculum.setDepartment(d);
		}
		
		if (!iCurriculumClasfTable.saveCurriculum(iCurriculum)) {
			ret = false;
		}
		if (!iCurriculum.hasClassifications()) {
			iCurriculumClasfTableError.setText("At least some students must be expected.");
			iCurriculumClasfTableError.setVisible(true);
			ret = false;
		}
		
		if (!iCurriculumCourses.saveCurriculum(iCurriculum)) {
			ret = false;
		}
		
		return ret;
	}

	private void loadMajors(final boolean loadEnrollments) {
		if (iCurriculumArea.getSelectedIndex() >= 0) {
			showLoading("Loading majors ...");
			iService.loadMajors(iCurriculum.getId(), Long.valueOf(iCurriculumArea.getValue(iCurriculumArea.getSelectedIndex())),
					new AsyncCallback<TreeSet<MajorInterface>>() {

						@Override
						public void onFailure(Throwable caught) {
							hideLoading();
						}

						@Override
						public void onSuccess(TreeSet<MajorInterface> result) {
							String defaultAbbv = "", defaultName = "";
							AcademicAreaInterface area = null;
							try {
								area = iAreas.get(iCurriculumArea.getSelectedIndex());
								defaultAbbv = area.getAbbv();
								defaultName = area.getName();
							} catch (Exception e) {}
							
							iMajors.clear(); iMajors.addAll(result);
							iCurriculumMajors.clear();
							int idx = 0;
							boolean allSelected = true;
							for (MajorInterface m: result) {
								iCurriculumMajors.addItem(m.getName(), m.getId().toString());
								if (iCurriculum != null && iCurriculum.hasMajors()) {
									iCurriculumMajors.setItemSelected(idx, iCurriculum.getMajors().contains(m));
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
							if (defaultName.length() > 60) defaultName = defaultName.substring(0, 60);
							iDefaultAbbv = defaultAbbv.equals(iCurriculumAbbv.getText());
							iDefaultName = defaultName.equalsIgnoreCase(iCurriculumName.getText());
							if (!iDefaultAbbv && allSelected && area != null && area.getAbbv().equals(iCurriculumAbbv.getText()))
								iDefaultAbbv = true;
							if (!iDefaultName && allSelected && area != null && area.getName().equalsIgnoreCase(iCurriculumName.getText()))
								iDefaultName = true;
							iCurriculumMajors.setVisibleItemCount(iCurriculumMajors.getItemCount() <= 3 ? 3 : iCurriculumMajors.getItemCount() > 10 ? 10 : iCurriculumMajors.getItemCount());
							if (loadEnrollments) loadEnrollments();
							hideLoading();
						}
					});
		} else {
			iCurriculumMajors.clear();
		}
	}
	
	private void loadEnrollments() {
		if (iCurriculumArea.getSelectedIndex() >= 0) {
			final Long areaId = Long.valueOf(iCurriculumArea.getValue(iCurriculumArea.getSelectedIndex()));
			final List<Long> majorIds = new ArrayList<Long>();
			for (int i = 0; i < iCurriculumMajors.getItemCount(); i++) {
				if (iCurriculumMajors.isItemSelected(i)) majorIds.add(Long.valueOf(iCurriculumMajors.getValue(i)));
			}
			
			if (majorIds.isEmpty()) {
				for (int i = 0; i < iCurriculumMajors.getItemCount(); i++) {
					majorIds.add(Long.valueOf(iCurriculumMajors.getValue(i)));
				}
			}
			if (majorIds.isEmpty()) return;
			
			showLoading("Loading course enrollments ...");
			iService.computeEnrollmentsAndLastLikes(areaId, majorIds, new AsyncCallback<HashMap<String,CurriculumStudentsInterface[]>>() {

				@Override
				public void onFailure(Throwable caught) {
					hideLoading();
				}

				@Override
				public void onSuccess(HashMap<String, CurriculumStudentsInterface[]> result) {
					CurriculumStudentsInterface[] x = result.get("");
					for (int col = 0; col < iClassifications.size(); col++) {
						iCurriculumClasfTable.setEnrollment(col, x == null || x[col] == null ? null : x[col].getEnrollment());
						iCurriculumClasfTable.setLastLike(col, x == null || x[col] == null ? null : x[col].getLastLike());
						iCurriculumClasfTable.setProjection(col, x == null || x[col] == null ? null : x[col].getProjection());
					}
					iCurriculumCourses.updateEnrollmentsAndLastLike(result);
					if (iCurriculumClasfTableHint.getText().equals("Show all columns."))
						iCurriculumClasfTable.hideEmptyColumns();
					else
						iCurriculumClasfTable.showAllColumns();
					hideLoading();
				}
			});
		}
	}
	
	public static class MyTextBox extends TextBox {
		public MyTextBox() {
			super();
			setStyleName("unitime-TextBox");
		}
		
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			if (enabled) {
				getElement().getStyle().setBorderColor(null);
				getElement().getStyle().setBackgroundColor(null);
			} else {
				getElement().getStyle().setBorderColor("transparent");
				getElement().getStyle().setBackgroundColor("transparent");
			}
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
		iCurriculum = new CurriculumInterface();
		iCurriculum.setEditable(true);
		if (iDepts.size() == 1) {
			DepartmentInterface d = new DepartmentInterface();
			d.setId(Long.valueOf(iCurriculumDept.getValue(iCurriculumDept.getSelectedIndex())));
			iCurriculum.setDepartment(d);
		}
		loadCurriculum(true);
	}
	
	public void edit(CurriculumInterface curriculum, boolean detailsEditable) {
		iCurriculum = curriculum;
		loadCurriculum(detailsEditable);
	}

	public void setupAreas(TreeSet<AcademicAreaInterface> result) {
		iAreas.clear(); iAreas.addAll(result);
		iCurriculumArea.clear();
		for (AcademicAreaInterface area: result) {
			iCurriculumArea.addItem(area.getName(), area.getId().toString());
		}
	}
	
	public void setupDepartments(TreeSet<DepartmentInterface> result) {
		iDepts.clear(); iDepts.addAll(result);
		iCurriculumDept.clear();
		for (DepartmentInterface dept: result) {
			iCurriculumDept.addItem(dept.getLabel(), dept.getId().toString());
		}
	}
	
	public void setupClassifications(TreeSet<AcademicClassificationInterface> result) {
		iClassifications.clear(); iClassifications.addAll(result);
		iCurriculumClasfTable.setup(iClassifications);
		iCurriculumCourses.link(iCurriculumClasfTable);
	}
	
	public void showOnlyCourses(TreeSet<CourseInterface> courses) {
		iCurriculumCourses.showOnlyCourses(courses);
	}
}
