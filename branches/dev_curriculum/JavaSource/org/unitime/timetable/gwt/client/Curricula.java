/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.MajorInterface;
import org.unitime.timetable.gwt.widgets.CurriculaClassifications;
import org.unitime.timetable.gwt.widgets.CurriculaCourses;
import org.unitime.timetable.gwt.widgets.LoadingWidget;
import org.unitime.timetable.gwt.widgets.PageLabel;
import org.unitime.timetable.gwt.widgets.WebTable;
import org.unitime.timetable.gwt.widgets.CurriculaClassifications.ExpectedChangedEvent;
import org.unitime.timetable.gwt.widgets.CurriculaClassifications.NameChangedEvent;
import org.unitime.timetable.gwt.widgets.CurriculaCourses.CourseChangedEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Curricula extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);

	private TextBox iFilter = null;
	private Button iSearch = null;
	private Button iNew = null;
	private Button[] iSave = null;
	private Button[] iDelete = null;
	private Button[] iBack = null;
	private Label[] iSaveError = null;
	private WebTable iTable = null;
	
	private VerticalPanel iCurriculaPanel = null, iCurriculumPanel = null;
	private AbsolutePanel iPanel = null;
	
	private TextBox iCurriculumAbbv = null, iCurriculumName = null;
	private Label iCurriculumAbbvError = null, iCurriculumNameError = null, iCurriculumAreaError = null, iCurriculumDeptError = null, iCurriculumClasfTableError = null;
	private ListBox iCurriculumArea = null, iCurriculumDept = null, iCurriculumMajors = null;
	private CurriculaClassifications iCurriculumClasfTable = null;
	private CheckBox iCurriculaPercent = null;
	
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);
	
	private AsyncCallback<List<CurriculumClassificationInterface>> iLoadClassifications;
	
	private List<CurriculumInterface> iData = new ArrayList<CurriculumInterface>();
	private List<AcademicAreaInterface> iAreas = new ArrayList<AcademicAreaInterface>();
	private List<DepartmentInterface> iDepts = new ArrayList<DepartmentInterface>();
	private List<MajorInterface> iMajors = new ArrayList<MajorInterface>();
	private List<AcademicClassificationInterface> iClassifications = new ArrayList<AcademicClassificationInterface>();
	private CurriculumInterface iCurriculum = null;
	private HashMap<String,Integer[][]> iLastEnrollment = null;
	
	private CurriculaCourses iCurriculumCourses;
	private WebTable.Row iLastCourse;
	
	public Curricula() {
		iPanel = new AbsolutePanel();
		
		iCurriculaPanel = new VerticalPanel();
		
		HorizontalPanel filterPanel = new HorizontalPanel();
		filterPanel.setSpacing(3);
		
		Label filterLabel = new Label("Filter:");
		filterPanel.add(filterLabel);
		filterPanel.setCellVerticalAlignment(filterLabel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iFilter = new TextBox();
		iFilter.setWidth("400px");
		iFilter.setStyleName("gwt-SuggestBox");
		iFilter.setHeight("26");
		filterPanel.add(iFilter);
	
		iSearch = new Button("<u>S</u>earch");
		iSearch.setAccessKey('s');
		filterPanel.add(iSearch);
		
		iNew = new Button("<u>A</u>dd New");
		iNew.setAccessKey('a');
		filterPanel.add(iNew);

		iCurriculaPanel.add(filterPanel);
		iCurriculaPanel.setCellHorizontalAlignment(filterPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		iTable = new WebTable();
		iTable.setHeader(
				new WebTable.Row(
						new WebTable.Cell("Curricula", 1, "150"),
						new WebTable.Cell("Academic Area", 1, "150"),
						new WebTable.Cell("Major(s)", 1, "200"),
						new WebTable.Cell("Department", 1, "250"),
						new WebTable.Cell("Expected Students", 1, "50"),
						new WebTable.Cell("Enrolled Students", 1, "50"),
						new WebTable.Cell("Last-like Students", 1, "50")
				));
		iTable.setEmptyMessage("No data.");
		
		iCurriculaPanel.add(iTable);
		
		iCurriculaPanel.setWidth("100%");
		
		iPanel.add(iCurriculaPanel);
		
		iCurriculumPanel = new VerticalPanel();
		
		FlexTable curriculaTable = new FlexTable();
		
		curriculaTable.setStylePrimaryName("unitime-MainTable");
		curriculaTable.addStyleName("unitime-BottomLine");
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
		HorizontalPanel buttons[] = new HorizontalPanel[] {new HorizontalPanel(), new HorizontalPanel()};
		for (int i = 0; i < 2; i++) {
			iSave[i].setWidth("75px");
			iDelete[i].setWidth("75px");
			iBack[i].setWidth("75px");
			iSaveError[i].setStyleName("unitime-ErrorMessage");
			buttons[i].add(iSave[i]);
			iSave[i].getElement().getStyle().setMarginRight(4, Unit.PX);
			buttons[i].add(iDelete[i]);
			iDelete[i].getElement().getStyle().setMarginRight(4, Unit.PX);
			buttons[i].add(iBack[i]);
		}
		
		HorizontalPanel titleAndButtons = new HorizontalPanel();
		Label title = new Label("Curriculum Details", false);
		title.setStyleName("unitime-MainHeader");
		titleAndButtons.add(title);
		titleAndButtons.add(iSaveError[0]);
		titleAndButtons.add(buttons[0]);
		titleAndButtons.setCellHorizontalAlignment(buttons[0], HasHorizontalAlignment.ALIGN_RIGHT);
		titleAndButtons.setWidth("100%");
		curriculaTable.getFlexCellFormatter().setColSpan(idx, 0, 2);
		curriculaTable.getFlexCellFormatter().setStyleName(idx, 0, "unitime-MainTableHeader");
		curriculaTable.getRowFormatter().setStyleName(idx, "unitime-MainTableHeaderRow");
		curriculaTable.setWidget(idx++, 0, titleAndButtons);
		
		curriculaTable.setText(idx, 0, "Abbreviation:");
		VerticalPanel curriculumAbbvVP = new VerticalPanel();
		iCurriculumAbbv = new TextBox();
		iCurriculumAbbv.setWidth("200px");
		iCurriculumAbbv.setStyleName("gwt-SuggestBox");
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
				iCurriculumAbbvError.setVisible(false);
			}
		});

		curriculaTable.setText(idx, 0, "Name:");
		VerticalPanel curriculumNameVP = new VerticalPanel();
		iCurriculumName = new TextBox();
		iCurriculumName.setWidth("400px");
		iCurriculumName.setStyleName("gwt-SuggestBox");
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
				iCurriculumNameError.setVisible(false);
			}
		});

		curriculaTable.setText(idx, 0, "Academic Area:");
		VerticalPanel curriculumAreaVP = new VerticalPanel();
		iCurriculumArea = new ListBox(false);
		iCurriculumArea.setWidth("300px");
		iCurriculumArea.setStyleName("gwt-SuggestBox");
		iCurriculumArea.setVisibleItemCount(1);
		curriculumAreaVP.add(iCurriculumArea);
		iCurriculumAreaError = new Label();
		iCurriculumAreaError.setStyleName("unitime-ErrorHint");
		iCurriculumAreaError.setVisible(false);
		curriculumAreaVP.add(iCurriculumAreaError);
		curriculaTable.setWidget(idx++, 1, curriculumAreaVP);
		iService.loadAcademicAreas(new AsyncCallback<TreeSet<AcademicAreaInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(TreeSet<AcademicAreaInterface> result) {
				iAreas.clear(); iAreas.addAll(result);
				iCurriculumArea.clear();
				for (AcademicAreaInterface area: result) {
					iCurriculumArea.addItem(area.getName(), area.getId().toString());
				}
			}
			
		});
		iCurriculumArea.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCurriculumAreaError.setVisible(false);
				loadMajors();
			}
		});


		curriculaTable.setText(idx, 0, "Major(s):");
		iCurriculumMajors = new ListBox(true);
		iCurriculumMajors.setWidth("300px");
		iCurriculumMajors.setStyleName("gwt-SuggestBox");
		iCurriculumMajors.setVisibleItemCount(3);
		iCurriculumMajors.setHeight("100%");
		curriculaTable.setWidget(idx++, 1, iCurriculumMajors);
		iCurriculumMajors.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				loadEnrollments();
			}
		});

		curriculaTable.setText(idx, 0, "Department:");
		VerticalPanel curriculumDeptVP = new VerticalPanel();
		iCurriculumDept = new ListBox(false);
		iCurriculumDept.setWidth("300px");
		iCurriculumDept.setStyleName("gwt-SuggestBox");
		iCurriculumDept.setVisibleItemCount(1);
		curriculumDeptVP.add(iCurriculumDept);
		iCurriculumDeptError = new Label();
		iCurriculumDeptError.setStyleName("unitime-ErrorHint");
		iCurriculumDeptError.setVisible(false);
		curriculumDeptVP.add(iCurriculumDeptError);
		curriculaTable.setWidget(idx++, 1, curriculumDeptVP);
		iService.loadDepartments(new AsyncCallback<TreeSet<DepartmentInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(TreeSet<DepartmentInterface> result) {
				iDepts.clear(); iDepts.addAll(result);
				iCurriculumDept.clear();
				for (DepartmentInterface dept: result) {
					iCurriculumDept.addItem(dept.getLabel(), dept.getId().toString());
				}
			}
			
		});
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
		iCurriculumClasfTableError = new Label();
		iCurriculumClasfTableError.setStyleName("unitime-ErrorHint");
		iCurriculumClasfTableError.setVisible(false);
		curriculumClasfTableVP.add(iCurriculumClasfTableError);

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
		iService.loadAcademicClassifications(new AsyncCallback<TreeSet<AcademicClassificationInterface>>() {

			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(TreeSet<AcademicClassificationInterface> result) {
				iClassifications.clear(); iClassifications.addAll(result);
				iCurriculumClasfTable.setup(iClassifications);
				iCurriculumCourses.link(iCurriculumClasfTable);
			}
		});

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
		errorAndButtons.add(buttons[1]);
		errorAndButtons.setCellHorizontalAlignment(buttons[1], HasHorizontalAlignment.ALIGN_RIGHT);
		errorAndButtons.setCellWidth(buttons[1], "33%");
		iCurriculumPanel.add(errorAndButtons);
		buttons[1].getElement().getStyle().setMarginTop(2, Unit.PX);
		
		iCurriculumPanel.setVisible(false);
		
		iPanel.add(iCurriculumPanel);
		
		initWidget(iPanel);
		
		iLoadClassifications = new AsyncCallback<List<CurriculumClassificationInterface>>() {
			public void onFailure(Throwable caught) {}
			public void onSuccess(List<CurriculumClassificationInterface> classifications) {
				if (iTable.getRows() == null || iTable.getRows().length == 0) return;
				List<CurriculumInterface> curricula = new ArrayList<CurriculumInterface>();
				CurriculumInterface last = null;
				clasf: for (CurriculumClassificationInterface clasf: classifications) {
					if (last != null && last.getId().equals(clasf.getCurriculumId())) {
						last.addClassification(clasf);
						continue clasf;
					}
					for (CurriculumInterface c: iData) {
						if (c.getId().equals(clasf.getCurriculumId())) {
							if (c.hasClassifications()) c.getClassifications().clear();
							c.addClassification(clasf);
							curricula.add(c);
							last = c;
							continue clasf;
						}
					}
				}
				for (CurriculumInterface c: curricula) {
					if (c.getRow() >= iTable.getRows().length) continue;
					WebTable.Row row = iTable.getRows()[c.getRow()];
					if (row.getId().equals(c.getId().toString())) {
						row.setCell(4, new WebTable.Cell(c.getExpectedString()));
						row.setCell(5, new WebTable.Cell(c.getEnrollmentString()));
						row.setCell(6, new WebTable.Cell(c.getLastLikeString()));
					}
				}
				List<Long> noEnrl = new ArrayList<Long>();
				for (CurriculumInterface c: iData) {
					if (!c.hasClassifications()) {
						noEnrl.add(c.getId());
						if (noEnrl.size() == 1) {
							iTable.getRows()[c.getRow()].setCell(5, new WebTable.IconCell(RESOURCES.loading_small(),"Loading...",null));
						}
					}
					if (noEnrl.size() >= 10) break;
				}
				if (!noEnrl.isEmpty())
					iService.loadClassifications(noEnrl, iLoadClassifications);
			}
		};
		
		iSearch.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				loadCurricula();
			}
		});
		
		iFilter.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					loadCurricula();
			}
		});
		
		DeferredCommand.addCommand(new Command() {
			@Override
			public void execute() {
				iFilter.setFocus(true);
			}
		});
		
		showLoading();
		iService.lastCurriculaFilter(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				if (iFilter.getText().isEmpty()) {
					iFilter.setText(result);
					loadCurricula();
				}
				hideLoading();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iTable.setEmptyMessage("<font color='red'>Unable to retrieve curricula (" + caught.getMessage() + ").</font>");
				hideLoading();
			}
			
		});
		
		iTable.addRowClickHandler(new WebTable.RowClickHandler() {
			
			@Override
			public void onRowClick(WebTable.RowClickEvent event) {
				iCurriculaPanel.setVisible(false);
				RootPanel.get("loading").setVisible(true);
				setPageName("Edit Curriculum");
				showLoading();
				iService.loadCurriculum(iData.get(event.getRowIdx()).getId(), new AsyncCallback<CurriculumInterface>() {

					@Override
					public void onFailure(Throwable caught) {
						hideLoading();
						iCurriculaPanel.setVisible(true);
						setPageName("Curricula");
					}

					@Override
					public void onSuccess(CurriculumInterface result) {
						iCurriculum = result;
						loadCurriculum();
						RootPanel.get("loading").setVisible(false);
						iCurriculumPanel.setVisible(true);
						hideLoading();
					}
				});
			}
		});
		
		iNew.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				iCurriculaPanel.setVisible(false);
				setPageName("Edit Curriculum");
				iCurriculum = new CurriculumInterface();
				loadCurriculum();
				RootPanel.get("loading").setVisible(false);
				iCurriculumPanel.setVisible(true);
			}
		});

		
		iCurriculumCourses.addCourseChangedHandler(new CurriculaCourses.CourseChangedHandler() {
			@Override
			public void courseChanged(CourseChangedEvent e) {
				if (iLastEnrollment == null || iLastEnrollment.isEmpty()) return;
				Integer[][] c = iLastEnrollment.get(e.getCourseName());
				for (int col = 0; col < iClassifications.size(); col ++) {
					iCurriculumCourses.setEnrollmentAndLastLike(e.getCourseName(), col,
							c == null || c[col] == null ? null : c[col][0], 
							c == null || c[col] == null ? null : c[col][1]);
				}
				/*
				for (CurriculumClassificationInterface clasf: iLastEnrollment) {
					int col = iCurriculumClasfTable.getColumn(clasf.getAcademicClassification());
					if (col < 0) continue;
					boolean changed = false;
					if (clasf.hasCourses())
						for (CurriculumCourseInterface course: clasf.getCourses()) {
							if (course.getCourseName().equals(e.getCourseName())) {
								iCurriculumCourses.setEnrollmentAndLastLike(
										course.getCourseName(),
										col,
										course.getEnrollment(),
										course.getLastLike());
								changed = true;
							}
						}
					if (!changed)
						iCurriculumCourses.setEnrollmentAndLastLike(e.getCourseName(), col, null, null);
				}*/
			}
		});
		
		ClickHandler backHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iCurriculumPanel.setVisible(false);
				setPageName("Curricula");
				iCurriculum = new CurriculumInterface();
				RootPanel.get("loading").setVisible(false);
				iCurriculaPanel.setVisible(true);
			}
		};
		
		for (int i = 0; i < 2; i++ ) {
			iBack[i].addClickHandler(backHandler);
		}
		
		ClickHandler saveHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (saveCurriculum()) {
					showLoading();
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
							hideLoading();
							iCurriculumPanel.setVisible(false);
							setPageName("Curricula");
							iCurriculum = new CurriculumInterface();
							RootPanel.get("loading").setVisible(false);
							iCurriculaPanel.setVisible(true);
							loadCurricula();
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
				showLoading();
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
						iCurriculumPanel.setVisible(false);
						setPageName("Curricula");
						iCurriculum = new CurriculumInterface();
						RootPanel.get("loading").setVisible(false);
						iCurriculaPanel.setVisible(true);
						loadCurricula();
						hideLoading();
					}
				});
			}
		};
		
		for (int i = 0; i < 2; i++ ) {
			iDelete[i].addClickHandler(deleteHandler);
		}
	}
	
	private void loadCurricula() {
		if (!iSearch.isEnabled()) return;
		showLoading();
		iSearch.setEnabled(false);
		iNew.setEnabled(false);
		iTable.clearData(true);
		iData.clear();
		iTable.setEmptyMessage("Loading data...");
		iService.findCurricula(iFilter.getText(), new AsyncCallback<TreeSet<CurriculumInterface>>() {
			
			@Override
			public void onSuccess(TreeSet<CurriculumInterface> result) {
				iData.clear(); iData.addAll(result);
				if (result.isEmpty()) {
					iTable.setEmptyMessage("No curricula matching the above filter found.");
				} else {
					WebTable.Row data[] = new WebTable.Row[result.size()];
					List<Long> ids = new ArrayList<Long>();
					int idx = 0;
					for (CurriculumInterface curriculum: result) {
						data[idx] = new WebTable.Row(
								new WebTable.Cell(curriculum.getAbbv()),
								new WebTable.Cell(curriculum.getAcademicArea().getName()),
								new WebTable.Cell(curriculum.getMajorNames("<br>")),
								new WebTable.Cell(curriculum.getDepartment().getLabel()),
								new WebTable.Cell(""),
								(idx == 0 ? new WebTable.IconCell(RESOURCES.loading_small(),"Loading...",null) : new WebTable.Cell("")),
								new WebTable.Cell("")
								);
						data[idx].setId(curriculum.getId().toString());
						curriculum.setRow(idx);
						if (ids.size() < 10 && !curriculum.hasClassifications()) ids.add(curriculum.getId());
						idx++;
					}
					iTable.setData(data);
					if (!ids.isEmpty())
						iService.loadClassifications(ids, iLoadClassifications);
				}
				iSearch.setEnabled(true);
				iNew.setEnabled(true);
				hideLoading();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iTable.setEmptyMessage("<font color='red'>Unable to retrieve curricula (" + caught.getMessage() + ").</font>");
				iSearch.setEnabled(true);
				iNew.setEnabled(true);
				hideLoading();
			}
		});
	}
		
	private void loadCurriculum() {
		iCurriculumAbbvError.setVisible(false);
		iCurriculumNameError.setVisible(false);
		iCurriculumAreaError.setVisible(false);
		iCurriculumDeptError.setVisible(false);
		iCurriculumClasfTableError.setVisible(false);
		for (int i = 0; i < 2; i ++) {
			iSaveError[i].setVisible(false);
			iDelete[i].setVisible(iCurriculum.getId() != null);
		}
		iCurriculumAbbv.setText(iCurriculum.getAbbv());
		iCurriculumName.setText(iCurriculum.getName());
		iCurriculumArea.setSelectedIndex(-1);
		if (iCurriculum.getAcademicArea() != null) {
			for (int i = 0; i < iAreas.size(); i++)
				if (iAreas.get(i).getId().equals(iCurriculum.getAcademicArea().getId()))
					iCurriculumArea.setSelectedIndex(i);
		}
		iCurriculumDept.setSelectedIndex(-1);
		if (iCurriculum.getDepartment() != null) {
			for (int i = 0; i < iDepts.size(); i++)
				if (iDepts.get(i).getId().equals(iCurriculum.getDepartment().getId()))
					iCurriculumDept.setSelectedIndex(i);
		}
		loadMajors(); // here we can have false
		iCurriculumClasfTable.populate(iCurriculum.getClassifications());
		iCurriculumCourses.populate(iCurriculum);
		for (int col = 0; col < iClassifications.size(); col++) {
			if (iCurriculumClasfTable.getExpected(col) == null)
				iCurriculumCourses.setVisible(col, false);
		}
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
	
	private void loadMajors() {
		if (iCurriculumArea.getSelectedIndex() >= 0) {
			showLoading();
			iService.loadMajors(iCurriculum.getId(), Long.valueOf(iCurriculumArea.getValue(iCurriculumArea.getSelectedIndex())),
					new AsyncCallback<TreeSet<MajorInterface>>() {

						@Override
						public void onFailure(Throwable caught) {
							hideLoading();
						}

						@Override
						public void onSuccess(TreeSet<MajorInterface> result) {
							iMajors.clear(); iMajors.addAll(result);
							iCurriculumMajors.clear();
							int idx = 0;
							for (MajorInterface m: result) {
								iCurriculumMajors.addItem(m.getName(), m.getId().toString());
								if (iCurriculum != null && iCurriculum.hasMajors()) {
									iCurriculumMajors.setItemSelected(idx, iCurriculum.getMajors().contains(m));
								}
								idx++;
							}
							iCurriculumMajors.setVisibleItemCount(iCurriculumMajors.getItemCount() <= 3 ? 3 : iCurriculumMajors.getItemCount() > 10 ? 10 : iCurriculumMajors.getItemCount());
							loadEnrollments();
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
			
			showLoading();
			iService.computeEnrollmentsAndLastLikes(areaId, majorIds, new AsyncCallback<HashMap<String,Integer[][]>>() {

				@Override
				public void onFailure(Throwable caught) {
					hideLoading();
				}

				@Override
				public void onSuccess(HashMap<String, Integer[][]> result) {
					iLastEnrollment = result;
					Integer[][] x = iLastEnrollment.get("");
					for (int col = 0; col < iClassifications.size(); col++) {
						iCurriculumClasfTable.setEnrollment(col, x == null || x[col] == null ? null : x[col][0]);
						iCurriculumClasfTable.setLastLike(col, x == null || x[col] == null ? null : x[col][1]);
					}
					iCurriculumCourses.updateEnrollmentsAndLastLike(iLastEnrollment);
					hideLoading();
				}
			});
		}
	}

	public void showLoading() { LoadingWidget.getInstance().show(); }
	
	public void hideLoading() { LoadingWidget.getInstance().hide(); }
	
	public void setPageName(String pageName) {
		((PageLabel)RootPanel.get("title").getWidget(0)).setPageName(pageName);
	}

}
