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
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.Client.GwtPageChangeEvent;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.curricula.CurriculumEdit.EditFinishedEvent;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.FilterPanel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Tomas Muller
 */
public class CurriculaPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	public static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);

	private CurriculumFilterBox iFilter = null;
	private AriaButton iSearch = null;
	private AriaButton iNew = null;
	private AriaButton iPrint = null;
	private CurriculaTable iCurriculaTable = null;
	
	private VerticalPanel iCurriculaPanel = null;
	
	private SimplePanel iPanel = null;
	private FilterPanel iFilterPanel = null;
	
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);
	
	private CurriculumEdit iCurriculumPanel = null;
	private ClassificationsEdit iClassificationsEdit = null;
	
	public CurriculaPage() {
		iPanel = new SimplePanel();
		
		iCurriculaPanel = new VerticalPanel();
		
		iFilterPanel = new FilterPanel();
		
		Label filterLabel = new Label(MESSAGES.propFilter());
		iFilterPanel.addLeft(filterLabel);
		
		iFilter = new CurriculumFilterBox();
		iFilterPanel.addLeft(iFilter);
		
		iSearch = new AriaButton(MESSAGES.buttonSearch());
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.addRight(iSearch);		
		
		iPrint = new AriaButton(MESSAGES.buttonPrint());
		iPrint.addStyleName("unitime-NoPrint");
		iFilterPanel.addRight(iPrint);		

		iNew = new AriaButton(MESSAGES.buttonAddNew());
		iNew.setEnabled(false);
		iNew.addStyleName("unitime-NoPrint");
		iFilterPanel.addRight(iNew);
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
		iFilterPanel.addRight(iCurriculaTable.getOperations());
		iCurriculaTable.getOperations().setEnabled(false);
		iCurriculaTable.setVisible(false);
		
		ScrollPanel curriculumTableScroll = new ScrollPanel(iCurriculaTable);
		curriculumTableScroll.addStyleName("unitime-ScrollTable");
		iCurriculaPanel.add(curriculumTableScroll);
		
		iCurriculaPanel.setWidth("100%");
		
		iPanel.setWidget(iCurriculaPanel);
		
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
		
		iClassificationsEdit = new ClassificationsEdit();
		
		initWidget(iPanel);
		
		iSearch.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				loadCurricula();
			}
		});
		
		/*
		iFilter.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					loadCurricula();
			}
		});
		*/
		
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
			iFilter.setValue(Window.Location.getParameter("q"), true);
			loadCurricula();
		} else {
			iService.lastCurriculaFilter(new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String result) {
					if (iFilter.getValue().isEmpty()) {
						iFilter.setValue(result, true);
						if (CONSTANTS.searchWhenPageIsLoaded())
							loadCurricula();
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
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
					showLoading(MESSAGES.waitLoadingCurriculum());
					iService.loadCurriculum(Long.parseLong(command.substring("detail=".length())), new AsyncCallback<CurriculumInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							hideLoading();
						}
						@Override
						public void onSuccess(CurriculumInterface result) {
							iCurriculumPanel.edit(result, true);
							iPanel.setWidget(iCurriculumPanel);
							hideLoading();
							Client.fireGwtPageChanged(new GwtPageChangeEvent());
						}
					});
				} else if ("new".equals(command)) {
					UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAddCurriculum());
					iCurriculumPanel.addNew();
					iPanel.setWidget(iCurriculumPanel);
					Client.fireGwtPageChanged(new GwtPageChangeEvent());
				} else {
					if (!"requests".equals(command))
						iFilter.setValue(command.replace("%20", " "), true);
					loadCurricula();
					if (iCurriculumPanel.isVisible()) {
						UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
						iPanel.setWidget(iCurriculaPanel);
						iCurriculaTable.scrollIntoView();
						Client.fireGwtPageChanged(new GwtPageChangeEvent());
					}
				}
			}
		});

		
		iCurriculaTable.addCurriculumClickHandler(new CurriculaTable.CurriculumClickHandler() {
			@Override
			public void onClick(CurriculaTable.CurriculumClickedEvent evt) {
				showLoading(MESSAGES.waitLoadingCurriculumWithName(evt.getCurriculum().getName()));
				iService.loadCurriculum(evt.getCurriculum().getId(), new AsyncCallback<CurriculumInterface>() {

					@Override
					public void onFailure(Throwable caught) {
						hideLoading();
					}

					@Override
					public void onSuccess(CurriculumInterface result) {
						History.newItem("detail=" + result.getId(), false);
						iCurriculumPanel.edit(result, true);
						iPanel.setWidget(iCurriculumPanel);
						hideLoading();
						Client.fireGwtPageChanged(new GwtPageChangeEvent());
					}
				});
			}
		});
		
		iNew.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				History.newItem("new", false);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAddCurriculum());
				iCurriculumPanel.addNew();
				iPanel.setWidget(iCurriculumPanel);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
			}
		});
		
		iCurriculaTable.setEditClassificationHandler(new CurriculaTable.EditClassificationHandler() {
			
			@Override
			public void doEdit(List<CurriculumInterface> curricula) {
				iClassificationsEdit.setData(curricula);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurriculumRequestedEnrollments());
				iPanel.setWidget(iClassificationsEdit);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem("requests", false);

			}
		});
		
		iCurriculumPanel.addEditFinishedHandler(new CurriculumEdit.EditFinishedHandler() {
			
			@Override
			public void onSave(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
				iPanel.setWidget(iCurriculaPanel);
				loadCurricula();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getValue(), false);
			}
			
			@Override
			public void onDelete(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
				iPanel.setWidget(iCurriculaPanel);
				loadCurricula();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getValue(), false);
			}
			
			@Override
			public void onBack(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
				iPanel.setWidget(iCurriculaPanel);
				iCurriculaTable.scrollIntoView();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getValue(), false);
			}
		});
		
		iClassificationsEdit.addEditFinishedHandler(new ClassificationsEdit.EditFinishedHandler() {
			
			@Override
			public void onSave(ClassificationsEdit.EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
				iPanel.setWidget(iCurriculaPanel);
				loadCurricula();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getValue(), false);
			}
			
			@Override
			public void onBack(ClassificationsEdit.EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
				iPanel.setWidget(iCurriculaPanel);
				iCurriculaTable.scrollIntoView();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getValue(), false);
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
		History.newItem(iFilter.getValue(), false);
		iCurriculaTable.query(iFilter.getElementsRequest(), new AsyncCallback<TreeSet<CurriculumInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(TreeSet<CurriculumInterface> result) {
				iCurriculaTable.setVisible(true);
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
