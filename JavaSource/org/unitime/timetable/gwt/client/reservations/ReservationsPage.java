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

import java.util.List;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.Client.GwtPageChangeEvent;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.reservations.ReservationEdit.EditFinishedEvent;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.ReservationService;
import org.unitime.timetable.gwt.services.ReservationServiceAsync;
import org.unitime.timetable.gwt.shared.ReservationInterface;

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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Tomas Muller
 *
 */
public class ReservationsPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private final ReservationServiceAsync iReservationService = GWT.create(ReservationService.class);

	private ReservationFilterBox iFilter = null;
	private Button iSearch = null;
	private Button iNew = null;
	private Button iPrint = null;
	private ReservationTable iReservationTable = null;
	
	private VerticalPanel iReservationPanel = null;
	
	private SimplePanel iPanel = null;
	private HorizontalPanel iFilterPanel = null;
	
	private ReservationEdit iReservationEdit = null;
	private Long iLastReservationId = null;
	
	public ReservationsPage() {
		iPanel = new SimplePanel();
		iPanel.setWidth("100%");
		
		iReservationPanel = new VerticalPanel();
		
		iFilterPanel = new HorizontalPanel();
		iFilterPanel.setSpacing(3);
		
		Label filterLabel = new Label(MESSAGES.propFilter());
		iFilterPanel.add(filterLabel);
		iFilterPanel.setCellVerticalAlignment(filterLabel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iFilter = new ReservationFilterBox();
		iFilterPanel.add(iFilter);
		
		iSearch = new Button(MESSAGES.buttonSearch());
		iSearch.setAccessKey('s');
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iSearch);		
		
		iPrint = new Button(MESSAGES.buttonPrint());
		iPrint.setAccessKey('p');
		iPrint.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iPrint);		

		iNew = new Button(MESSAGES.buttonAddNew());
		iNew.setAccessKey('a');
		iNew.setEnabled(false);
		iNew.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iNew);
		iReservationService.canAddReservation(new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(Boolean result) {
				iNew.setEnabled(result);
			}
		});
				
		iReservationPanel.add(iFilterPanel);
		iReservationPanel.setCellHorizontalAlignment(iFilterPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		iReservationTable = new ReservationTable(true, false);
		iReservationTable.getElement().getStyle().setMarginTop(10, Unit.PX);
		
		iReservationPanel.add(iReservationTable);
		
		iReservationPanel.setWidth("100%");
		
		iPanel.setWidget(iReservationPanel);
		
		iReservationEdit = new ReservationEdit(false);
		
		initWidget(iPanel);
		
		iSearch.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				loadReservations();
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
			iFilter.setValue(Window.Location.getParameter("q"), true);
			loadReservations();
		} else {
			iReservationService.lastReservationFilter(new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String result) {
					if (iFilter.getValue().isEmpty()) {
						iFilter.setValue(result, true);
						if (CONSTANTS.searchWhenPageIsLoaded()) loadReservations();
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
				if (event.getValue() != null && !event.getValue().isEmpty()) {
					iFilter.setValue(event.getValue().replace("%20", " "), true);
					loadReservations();
				}
			}
		});

		
		iReservationTable.addReservationClickHandler(new ReservationTable.ReservationClickHandler() {
			@Override
			public void onClick(ReservationTable.ReservationClickedEvent evt) {
				iReservationEdit.setReservation(evt.getReservation());
				iPanel.setWidget(iReservationEdit);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				iLastReservationId = evt.getReservation().getId();
			}
		});
		
		iNew.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAddReservation());
				iPanel.setWidget(iReservationEdit);
				iReservationEdit.setReservation(null);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				iLastReservationId = null;
			}
		});
		
		iReservationEdit.addEditFinishedHandler(new ReservationEdit.EditFinishedHandler() {
			
			@Override
			public void onSave(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageReservations());
				iPanel.setWidget(iReservationPanel);
				loadReservations();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				iLastReservationId = evt.getReservationId();
			}
			
			@Override
			public void onDelete(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageReservations());
				iPanel.setWidget(iReservationPanel);
				loadReservations();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				iLastReservationId = evt.getReservationId();
			}
			
			@Override
			public void onBack(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageReservations());
				iPanel.setWidget(iReservationPanel);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				iLastReservationId = evt.getReservationId();
				iReservationTable.select(iLastReservationId);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageReservations());
				iPanel.setWidget(iReservationPanel);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
			}

			@Override
			public boolean hasNext(EditFinishedEvent evt) {
				return iReservationTable.getNext(evt.getReservationId()) != null;
			}

			@Override
			public boolean hasPrevious(EditFinishedEvent evt) {
				return iReservationTable.getPrevious(evt.getReservationId()) != null;
			}

			@Override
			public void onNext(EditFinishedEvent evt) {
				ReservationInterface reservation = iReservationTable.getNext(evt.getReservationId());
				if (reservation == null)
					onSave(evt);
				else {
					iReservationEdit.setReservation(reservation);
					iPanel.setWidget(iReservationEdit);
					Client.fireGwtPageChanged(new GwtPageChangeEvent());
					iLastReservationId = reservation.getId();
				}
			}

			@Override
			public void onPrevious(EditFinishedEvent evt) {
				ReservationInterface reservation = iReservationTable.getPrevious(evt.getReservationId());
				if (reservation == null)
					onSave(evt);
				else {
					iReservationEdit.setReservation(reservation);
					iPanel.setWidget(iReservationEdit);
					Client.fireGwtPageChanged(new GwtPageChangeEvent());
					iLastReservationId = reservation.getId();
				}
			}
		});
		
	}
	
	private void loadReservations() {
		if (!iSearch.isEnabled()) return;
		iSearch.setEnabled(false);
		iPrint.setEnabled(false);
		final boolean newEnabled = iNew.isEnabled();
		if (newEnabled)
			iNew.setEnabled(false);
		History.newItem(iFilter.getValue(), false);
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingReservations());
		iReservationTable.query(iFilter.getElementsRequest(), new AsyncCallback<List<ReservationInterface>>() {
			@Override
			public void onSuccess(List<ReservationInterface> result) {
				LoadingWidget.getInstance().hide();
				iSearch.setEnabled(true);
				iPrint.setEnabled(true);
				if (newEnabled)
					iNew.setEnabled(true);
				if (iLastReservationId != null) {
					iReservationTable.select(iLastReservationId);
					iReservationTable.scrollIntoView(iLastReservationId);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iReservationTable.setErrorMessage(MESSAGES.failedToLoadReservations(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadReservations(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}
		});
	}
}
