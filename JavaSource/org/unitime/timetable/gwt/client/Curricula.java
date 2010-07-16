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
import org.unitime.timetable.gwt.widgets.ClassificationsEdit;
import org.unitime.timetable.gwt.widgets.CurriculaTable;
import org.unitime.timetable.gwt.widgets.CurriculumEdit;
import org.unitime.timetable.gwt.widgets.LoadingWidget;
import org.unitime.timetable.gwt.widgets.PageLabel;
import org.unitime.timetable.gwt.widgets.WebTable;
import org.unitime.timetable.gwt.widgets.CurriculumEdit.EditFinishedEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Curricula extends Composite {
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
	
	private AsyncCallback<List<CurriculumClassificationInterface>> iLoadClassifications;
	
	private WebTable.Row iLastCourse;
	
	private CurriculumEdit iCurriculumPanel = null;
	private ClassificationsEdit iClassificationsEdit = null;
	
	public Curricula() {
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
		
		iFilter = new TextBox();
		iFilter.setWidth("400px");
		iFilter.setStyleName("gwt-SuggestBox");
		iFilter.setHeight("26");
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
		//RootPanel.get("UniTimeGWT:TitlePanel").add(iFilterPanel);
		
		iCurriculaTable = new CurriculaTable();
		iCurriculaTable.getElement().getStyle().setMarginTop(10, Unit.PX);
		
		iCurriculaPanel.add(iCurriculaTable);
		
		iCurriculaPanel.setWidth("100%");
		
		iPanel.add(iCurriculaPanel);
		
		iCurriculumPanel = new CurriculumEdit();
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
		
		DeferredCommand.addCommand(new Command() {
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
				}
				
			});
		}
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (event.getValue() != null && !event.getValue().isEmpty()) {
					iFilter.setText(event.getValue().replace("%20", " "));
					loadCurricula();
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
						iCurriculumPanel.edit(result, true);
						iCurriculaPanel.setVisible(false);
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
		
		iCurriculaTable.setEditClassificationHandler(new CurriculaTable.EditClassificationHandler() {
			
			@Override
			public void doEdit(List<CurriculumInterface> curricula) {
				iCurriculaPanel.setVisible(false);
				iClassificationsEdit.setData(curricula);
				setPageName("Curriculum Requested Enrollments");
				iClassificationsEdit.setVisible(true);
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
				iCurriculaTable.scrollIntoView();
			}
		});
		
		iClassificationsEdit.addEditFinishedHandler(new ClassificationsEdit.EditFinishedHandler() {
			
			@Override
			public void onSave(ClassificationsEdit.EditFinishedEvent evt) {
				iClassificationsEdit.setVisible(false);
				setPageName("Curricula");
				iCurriculaPanel.setVisible(true);
				loadCurricula();
			}
			
			@Override
			public void onBack(ClassificationsEdit.EditFinishedEvent evt) {
				iClassificationsEdit.setVisible(false);
				setPageName("Curricula");
				iCurriculaPanel.setVisible(true);
				iCurriculaTable.scrollIntoView();
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
		final boolean newEnabled = iNew.isEnabled();
		if (newEnabled)
			iNew.setEnabled(false);
		History.newItem(iFilter.getText(), false);
		iCurriculaTable.query(iFilter.getText(), new Command() {
			@Override
			public void execute() {
				iSearch.setEnabled(true);
				iPrint.setEnabled(true);
				if (newEnabled)
					iNew.setEnabled(true);
			}
		});
	}

	public void showLoading(String message) { LoadingWidget.getInstance().show(message); }
	
	public void hideLoading() { LoadingWidget.getInstance().hide(); }
	
	public void setPageName(String pageName) {
		((PageLabel)RootPanel.get("UniTimeGWT:Title").getWidget(0)).setPageName(pageName);
	}
	
	public static class HorizontalPanelWithHint extends HorizontalPanel {
		private PopupPanel iHint = null;
		private Timer iShowHint, iHideHint = null;
		
		public HorizontalPanelWithHint(Widget hint) {
			super();
			iHint = new PopupPanel();
			iHint.setWidget(hint);
			iHint.setStyleName("unitime-PopupHint");
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONMOUSEMOVE);
			iShowHint = new Timer() {
				@Override
				public void run() {
					iHint.show();
				}
			};
			iHideHint = new Timer() {
				@Override
				public void run() {
					iHint.hide();
				}
			};
		}
		
		public void onBrowserEvent(Event event) {
			int x = 10 + event.getClientX() + getElement().getOwnerDocument().getScrollLeft();
			int y = 10 + event.getClientY() + getElement().getOwnerDocument().getScrollTop();
			
			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEMOVE:
				if (iHint.isShowing()) {
					iHint.setPopupPosition(x, y);
				} else {
					iShowHint.cancel();
					iHint.setPopupPosition(x, y);
					iShowHint.schedule(1000);
				}
				break;
			case Event.ONMOUSEOUT:
				iShowHint.cancel();
				if (iHint.isShowing())
					iHideHint.schedule(1000);
				break;
			}
		}		
		
	}
	
}
