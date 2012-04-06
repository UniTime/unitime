/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.curricula;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.Client.GwtPageChangeEvent;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.curricula.CurriculumEdit.EditFinishedEvent;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.HorizontalPanelWithHint;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.DepartmentInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Tomas Muller
 */
public class CurriculaPage extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);

	private TextBox iFilter = null;
	private Button iSearch = null;
	private Button iNew = null;
	private Button iPrint = null;
	private CurriculaTable iCurriculaTable = null;
	
	private VerticalPanel iCurriculaPanel = null;
	
	private VerticalPanel iPanel = null;
	private HorizontalPanel iFilterPanel = null;
	
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);
	
	private CurriculumEdit iCurriculumPanel = null;
	private ClassificationsEdit iClassificationsEdit = null;
	
	public CurriculaPage() {
		iPanel = new VerticalPanel();
		
		iCurriculaPanel = new VerticalPanel();
		
		iFilterPanel = new HorizontalPanelWithHint(new HTML(
				"Filter curricula by any word from the name, code, or abbreviation<br>of a curricula, academic area, major, or department." +
				"<br><br>You can also use the following tags:" +
				"<ul>" +
				"<li><i>abbv:</i> curriculum abbreviation" + 
				"<li><i>name:</i> curriculum name" + 
				"<li><i>area:</i> academic area abbreviation or name" +
				"<li><i>major:</i> major code or name" +
				"<li><i>dept:</i> department code, name, or abbreviation" +
				"</ul>Use <i>or</i>, <i>and</i>, <i>not</i>, and brackets to build a boolean query." +
				"<br><br>Example: area: A and (major: AGFN or major: AGMG)",
				false));
		iFilterPanel.setSpacing(3);
		
		Label filterLabel = new Label("Filter:");
		iFilterPanel.add(filterLabel);
		iFilterPanel.setCellVerticalAlignment(filterLabel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iFilter = new UniTimeTextBox();
		iFilter.setWidth("400px");
		iFilter.setHeight("26px");
		iFilterPanel.add(iFilter);
		
		iSearch = new Button("<u>S</u>earch");
		iSearch.setAccessKey('s');
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iSearch);		
		
		iPrint = new Button("<u>P</u>rint");
		iPrint.setAccessKey('p');
		iPrint.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iPrint);		

		iNew = new Button("<u>A</u>dd New");
		iNew.setAccessKey('a');
		iNew.setEnabled(false);
		iNew.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iNew);
		iService.canAddCurriculum(new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(Boolean result) {
				iNew.setEnabled(result);
			}
		});
				
		iCurriculaPanel.add(iFilterPanel);
		iCurriculaPanel.setCellHorizontalAlignment(iFilterPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		iCurriculaTable = new CurriculaTable();
		iCurriculaTable.getElement().getStyle().setMarginTop(10, Unit.PX);
		iFilterPanel.add(iCurriculaTable.getOperations());
		iCurriculaTable.getOperations().setEnabled(false);
		
		iCurriculaPanel.add(iCurriculaTable);
		
		iCurriculaPanel.setWidth("100%");
		
		iPanel.add(iCurriculaPanel);
		
		iCurriculumPanel = new CurriculumEdit(new CurriculumEdit.NavigationProvider() {
			@Override
			public CurriculumInterface previous(CurriculumInterface curriculum) {
				return iCurriculaTable.previous(curriculum == null ? null : curriculum.getId());
			}
			@Override
			public CurriculumInterface next(CurriculumInterface curriculum) {
				return iCurriculaTable.next(curriculum == null ? null : curriculum.getId());
			}
			@Override
			public void onChange(CurriculumInterface curriculum) {
				if (curriculum.getId() != null)
					History.newItem("detail=" + curriculum.getId(), false);
			}
		});
		iCurriculumPanel.setVisible(false);
		iPanel.add(iCurriculumPanel);
		
		iClassificationsEdit = new ClassificationsEdit();
		iClassificationsEdit.setVisible(false);
		iPanel.add(iClassificationsEdit);
		
		initWidget(iPanel);
		
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
		
		iPrint.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.print();
			}
		});
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				iFilter.setFocus(true);
			}
		});

		if (Window.Location.getParameter("q") != null) {
			iFilter.setText(Window.Location.getParameter("q"));
			loadCurricula();
		} else {
			showLoading("Loading curricula ...");
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
					iCurriculaTable.setError("Unable to retrieve curricula (" + caught.getMessage() + ").");
					hideLoading();
					ToolBox.checkAccess(caught);
				}
				
			});
		}
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (event.getValue() == null || "0:0".equals(event.getValue())) return;
				String command = event.getValue();
				if (command.startsWith("detail=")) {
					showLoading("Loading curriculum ...");
					iService.loadCurriculum(Long.parseLong(command.substring("detail=".length())), new AsyncCallback<CurriculumInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							hideLoading();
						}
						@Override
						public void onSuccess(CurriculumInterface result) {
							iCurriculumPanel.edit(result, true);
							iCurriculaPanel.setVisible(false);
							iCurriculumPanel.setVisible(true);
							hideLoading();
							Client.fireGwtPageChanged(new GwtPageChangeEvent());
						}
					});
				} else if ("new".equals(command)) {
					iCurriculaPanel.setVisible(false);
					UniTimePageLabel.getInstance().setPageName("Add Curriculum");
					iCurriculumPanel.addNew();
					iCurriculumPanel.setVisible(true);
					Client.fireGwtPageChanged(new GwtPageChangeEvent());
				} else {
					if (!"requests".equals(command))
						iFilter.setText(command.replace("%20", " "));
					loadCurricula();
					if (iCurriculumPanel.isVisible()) {
						iCurriculumPanel.setVisible(false);
						UniTimePageLabel.getInstance().setPageName("Curricula");
						iCurriculaPanel.setVisible(true);
						iCurriculaTable.scrollIntoView();
						Client.fireGwtPageChanged(new GwtPageChangeEvent());
					}
				}
			}
		});

		
		iCurriculaTable.addCurriculumClickHandler(new CurriculaTable.CurriculumClickHandler() {
			@Override
			public void onClick(CurriculaTable.CurriculumClickedEvent evt) {
				showLoading("Loading curriculum " + evt.getCurriculum().getName() + " ...");
				iService.loadCurriculum(evt.getCurriculum().getId(), new AsyncCallback<CurriculumInterface>() {

					@Override
					public void onFailure(Throwable caught) {
						hideLoading();
					}

					@Override
					public void onSuccess(CurriculumInterface result) {
						History.newItem("detail=" + result.getId(), false);
						iCurriculumPanel.edit(result, true);
						iCurriculaPanel.setVisible(false);
						iCurriculumPanel.setVisible(true);
						hideLoading();
						Client.fireGwtPageChanged(new GwtPageChangeEvent());
					}
				});
			}
		});
		
		iNew.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				History.newItem("new", false);
				iCurriculaPanel.setVisible(false);
				UniTimePageLabel.getInstance().setPageName("Add Curriculum");
				iCurriculumPanel.addNew();
				iCurriculumPanel.setVisible(true);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
			}
		});
		
		iCurriculaTable.setEditClassificationHandler(new CurriculaTable.EditClassificationHandler() {
			
			@Override
			public void doEdit(List<CurriculumInterface> curricula) {
				iCurriculaPanel.setVisible(false);
				iClassificationsEdit.setData(curricula);
				UniTimePageLabel.getInstance().setPageName("Curriculum Requested Enrollments");
				iClassificationsEdit.setVisible(true);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem("requests", false);

			}
		});
		
		iCurriculumPanel.addEditFinishedHandler(new CurriculumEdit.EditFinishedHandler() {
			
			@Override
			public void onSave(EditFinishedEvent evt) {
				iCurriculumPanel.setVisible(false);
				UniTimePageLabel.getInstance().setPageName("Curricula");
				iCurriculaPanel.setVisible(true);
				loadCurricula();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getText(), false);
			}
			
			@Override
			public void onDelete(EditFinishedEvent evt) {
				iCurriculumPanel.setVisible(false);
				UniTimePageLabel.getInstance().setPageName("Curricula");
				iCurriculaPanel.setVisible(true);
				loadCurricula();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getText(), false);
			}
			
			@Override
			public void onBack(EditFinishedEvent evt) {
				iCurriculumPanel.setVisible(false);
				UniTimePageLabel.getInstance().setPageName("Curricula");
				iCurriculaPanel.setVisible(true);
				iCurriculaTable.scrollIntoView();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getText(), false);
			}
		});
		
		iClassificationsEdit.addEditFinishedHandler(new ClassificationsEdit.EditFinishedHandler() {
			
			@Override
			public void onSave(ClassificationsEdit.EditFinishedEvent evt) {
				iClassificationsEdit.setVisible(false);
				UniTimePageLabel.getInstance().setPageName("Curricula");
				iCurriculaPanel.setVisible(true);
				loadCurricula();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getText(), false);
			}
			
			@Override
			public void onBack(ClassificationsEdit.EditFinishedEvent evt) {
				iClassificationsEdit.setVisible(false);
				UniTimePageLabel.getInstance().setPageName("Curricula");
				iCurriculaPanel.setVisible(true);
				iCurriculaTable.scrollIntoView();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getText(), false);
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
				iCurriculaTable.setup(new ArrayList<AcademicClassificationInterface>(result));
			}
		});
	}
	
	private void loadCurricula() {
		if (!iSearch.isEnabled()) return;
		iSearch.setEnabled(false);
		iPrint.setEnabled(false);
		iCurriculaTable.getOperations().setEnabled(false);
		final boolean newEnabled = iNew.isEnabled();
		if (newEnabled)
			iNew.setEnabled(false);
		History.newItem(iFilter.getText(), false);
		iCurriculaTable.query(iFilter.getText(), new Command() {
			@Override
			public void execute() {
				iSearch.setEnabled(true);
				iPrint.setEnabled(true);
				iCurriculaTable.getOperations().setEnabled(true);
				if (newEnabled)
					iNew.setEnabled(true);
			}
		});
	}

	public void showLoading(String message) { LoadingWidget.getInstance().show(message); }
	
	public void hideLoading() { LoadingWidget.getInstance().hide(); }	
}
