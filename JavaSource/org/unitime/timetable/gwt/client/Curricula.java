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
import org.unitime.timetable.gwt.widgets.CurriculumEdit;
import org.unitime.timetable.gwt.widgets.LoadingWidget;
import org.unitime.timetable.gwt.widgets.PageLabel;
import org.unitime.timetable.gwt.widgets.WebTable;
import org.unitime.timetable.gwt.widgets.CurriculumEdit.EditFinishedEvent;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Curricula extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);

	private TextBox iFilter = null;
	private Button iSearch = null;
	private Button iNew = null;
	private WebTable iTable = null;
	
	private VerticalPanel iCurriculaPanel = null;
	
	private AbsolutePanel iPanel = null;
	
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);
	
	private AsyncCallback<List<CurriculumClassificationInterface>> iLoadClassifications;
	
	private List<CurriculumInterface> iData = new ArrayList<CurriculumInterface>();

	private WebTable.Row iLastCourse;
	
	private CurriculumEdit iCurriculumPanel = null;
	
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
		iNew.setEnabled(false);
		filterPanel.add(iNew);
		iService.canAddCurriculum(new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(Boolean result) {
				iNew.setEnabled(result);
			}
		});

		iCurriculaPanel.add(filterPanel);
		iCurriculaPanel.setCellHorizontalAlignment(filterPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		iTable = new WebTable();
		iTable.setHeader(
				new WebTable.Row(
						new WebTable.Cell("Curriculum", 1, "150"),
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
		
		iCurriculumPanel = new CurriculumEdit();
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
				showLoading();
				iService.loadCurriculum(iData.get(event.getRowIdx()).getId(), new AsyncCallback<CurriculumInterface>() {

					@Override
					public void onFailure(Throwable caught) {
						hideLoading();
						iCurriculaPanel.setVisible(true);
					}

					@Override
					public void onSuccess(CurriculumInterface result) {
						iCurriculumPanel.edit(result, true);
						setPageName(result.isEditable() ? "Edit Curriculum" : "Curriculum Details");
						iCurriculumPanel.setVisible(true);
						hideLoading();
					}
				});
			}
		});
		
		iNew.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				iCurriculaPanel.setVisible(false);
				setPageName("New Curriculum");
				iCurriculumPanel.addNew();
				iCurriculumPanel.setVisible(true);
			}
		});
		
		iCurriculumPanel.addEditFinishedHandler(new CurriculumEdit.EditFinishedHandler() {
			
			@Override
			public void onSave(EditFinishedEvent evt) {
				iCurriculumPanel.setVisible(false);
				setPageName("Curricula");
				iCurriculaPanel.setVisible(true);
				loadCurricula();
			}
			
			@Override
			public void onDelete(EditFinishedEvent evt) {
				iCurriculumPanel.setVisible(false);
				setPageName("Curricula");
				iCurriculaPanel.setVisible(true);
				loadCurricula();
			}
			
			@Override
			public void onBack(EditFinishedEvent evt) {
				iCurriculumPanel.setVisible(false);
				setPageName("Curricula");
				iCurriculaPanel.setVisible(true);
			}
		});
		

		iService.loadAcademicAreas(new AsyncCallback<TreeSet<AcademicAreaInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(TreeSet<AcademicAreaInterface> result) {
				iCurriculumPanel.setupAreas(result);
			}
		});
		iService.loadDepartments(new AsyncCallback<TreeSet<DepartmentInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(TreeSet<DepartmentInterface> result) {
				iCurriculumPanel.setupDepartments(result);
			}
		});
		iService.loadAcademicClassifications(new AsyncCallback<TreeSet<AcademicClassificationInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(TreeSet<AcademicClassificationInterface> result) {
				iCurriculumPanel.setupClassifications(result);
			}
		});
	}
	
	private void loadCurricula() {
		if (!iSearch.isEnabled()) return;
		showLoading();
		iSearch.setEnabled(false);
		final boolean newEnabled = iNew.isEnabled();
		if (newEnabled)
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
				if (newEnabled)
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

	public void showLoading() { LoadingWidget.getInstance().show(); }
	
	public void hideLoading() { LoadingWidget.getInstance().hide(); }
	
	public void setPageName(String pageName) {
		((PageLabel)RootPanel.get("UniTimeGWT:Title").getWidget(0)).setPageName(pageName);
	}
	
	
}
