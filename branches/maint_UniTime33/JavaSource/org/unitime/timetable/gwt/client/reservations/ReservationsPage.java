/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.reservations;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.Client.GwtPageChangeEvent;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.reservations.ReservationEdit.EditFinishedEvent;
import org.unitime.timetable.gwt.client.widgets.HorizontalPanelWithHint;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.ReservationService;
import org.unitime.timetable.gwt.services.ReservationServiceAsync;

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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Tomas Muller
 *
 */
public class ReservationsPage extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private final ReservationServiceAsync iReservationService = GWT.create(ReservationService.class);

	private TextBox iFilter = null;
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
		
		iFilterPanel = new HorizontalPanelWithHint(new HTML(
				"Filter reservations by any course number, subject area, department, or reservation type." +
				"<br><br>You can also use the following tags:" +
				"<ul>" +
				"<li><i>subject:</i> subject area name or abbreviation" + 
				"<li><i>dept:</i> department code, name, or abbreviation" + 
				"<li><i>type:</i> reservation type (individual, group, course, curriculum)" +
				"<li><i>student:</i> student name or external id (individual reservations)" +
				"<li><i>group:</i> student group name or abbreviation (student group reservaions)" +
				"<li><i>area:</i> academic area (curriculum reservations)" +
				"<li><i>expiration:</i>, <i>before:</i>, <i>after:</i> expiration date (in MM/dd/yyyy format)" +
				"<li>use <i>expired</i> for expired reservations" +
				"</ul>Use <i>or</i>, <i>and</i>, <i>not</i>, and brackets to build a boolean query." +
				"<br><br>Example: subject:ENGL and (type:individual or type:group) and not expired",
				false));
		iFilterPanel.setSpacing(3);
		
		Label filterLabel = new Label("Filter:");
		iFilterPanel.add(filterLabel);
		iFilterPanel.setCellVerticalAlignment(filterLabel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iFilter = new UniTimeTextBox();
		iFilter.setWidth("400px");
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
		
		iFilter.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
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
			iFilter.setText(Window.Location.getParameter("q"));
			loadReservations();
		} else {
			LoadingWidget.getInstance().show("Loading reservations ...");
			iReservationService.lastReservationFilter(new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String result) {
					if (iFilter.getText().isEmpty()) {
						iFilter.setText(result);
						loadReservations();
					}
					LoadingWidget.getInstance().hide();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					iReservationTable.setErrorMessage("Unable to retrieve reservations (" + caught.getMessage() + ").");
					LoadingWidget.getInstance().hide();
					ToolBox.checkAccess(caught);
				}
				
			});
		}
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (event.getValue() != null && !event.getValue().isEmpty()) {
					iFilter.setText(event.getValue().replace("%20", " "));
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
				UniTimePageLabel.getInstance().setPageName("Add Reservation");
				iPanel.setWidget(iReservationEdit);
				iReservationEdit.setReservation(null);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				iLastReservationId = null;
			}
		});
		
		iReservationEdit.addEditFinishedHandler(new ReservationEdit.EditFinishedHandler() {
			
			@Override
			public void onSave(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName("Reservations");
				iPanel.setWidget(iReservationPanel);
				loadReservations();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				iLastReservationId = evt.getReservationId();
			}
			
			@Override
			public void onDelete(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName("Reservations");
				iPanel.setWidget(iReservationPanel);
				loadReservations();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				iLastReservationId = evt.getReservationId();
			}
			
			@Override
			public void onBack(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName("Reservations");
				iPanel.setWidget(iReservationPanel);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				iLastReservationId = evt.getReservationId();
				iReservationTable.select(iLastReservationId);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				UniTimePageLabel.getInstance().setPageName("Reservations");
				iPanel.setWidget(iReservationPanel);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
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
		History.newItem(iFilter.getText(), false);
		iReservationTable.query(iFilter.getText(), new Command() {
			@Override
			public void execute() {
				iSearch.setEnabled(true);
				iPrint.setEnabled(true);
				if (newEnabled)
					iNew.setEnabled(true);
				if (iLastReservationId != null) {
					iReservationTable.select(iLastReservationId);
					iReservationTable.scrollIntoView(iLastReservationId);
				}
			}
		});
	}
}
