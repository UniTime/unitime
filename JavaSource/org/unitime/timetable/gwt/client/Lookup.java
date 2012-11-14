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
package org.unitime.timetable.gwt.client;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasColSpan;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasStyleName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.PersonInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @author Tomas Muller
 */
public class Lookup extends UniTimeDialogBox implements HasValue<PersonInterface> {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);

	private VerticalPanel iPanel;
	private UniTimeTable<PersonInterface> iTable;
	private ScrollPanel iScroll;
	private TextBox iQuery;
	private String iOptions;
	private Timer iTimer;
	private String iLastQuery = null;
	private PersonInterface iValue = null;
	
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	public Lookup() {
		super(true, true);
		setText("People Lookup");
		setEscapeToHide(true);
		iPanel = new VerticalPanel();
		iPanel.setSpacing(2);
		iQuery = new TextBox();
		iQuery.setWidth("400px");
		HorizontalPanel queryPanel = new HorizontalPanel();
		Label filterText = new Label("Name:", false);
		filterText.getElement().getStyle().setMarginRight(5, Unit.PX);
		queryPanel.setWidth("75px");
		queryPanel.add(filterText);
		queryPanel.setCellHorizontalAlignment(filterText, HasHorizontalAlignment.ALIGN_RIGHT);
		queryPanel.setCellVerticalAlignment(filterText, HasVerticalAlignment.ALIGN_MIDDLE);
		queryPanel.add(iQuery);
		HTML blank = new HTML("&nbsp;");
		blank.setWidth("75px");
		queryPanel.add(blank);
		iPanel.add(queryPanel);
		iPanel.setCellHorizontalAlignment(queryPanel, HasHorizontalAlignment.ALIGN_CENTER);
		iTable = new UniTimeTable<PersonInterface>();
		iScroll = new ScrollPanel(iTable);
		iScroll.setSize("800px", "500px");
		iScroll.setStyleName("unitime-ScrollPanel");
		iPanel.add(iScroll);
		setWidget(iPanel);
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader("Name"));
		header.add(new UniTimeTableHeader("Email"));
		header.add(new UniTimeTableHeader("Phone"));
		header.add(new UniTimeTableHeader("Department"));
		header.add(new UniTimeTableHeader("Source"));
		iTable.addRow(null, header);
		iTable.setAllowSelection(true);
		iTable.setWidth("100%");
		iTimer = new Timer() {
			@Override
			public void run() {
				update();
			}
		};
		iTable.addMouseClickListener(new MouseClickListener<PersonInterface>() {
			@Override
			public void onMouseClick(TableEvent<PersonInterface> event) {
				if (event.getData() != null) {
					iTimer.cancel();
					Lookup.this.hide();
					setValue(event.getData(), true);
				}
			}
		});
		iQuery.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				iTimer.schedule(500);
			}
		});
		
		sinkEvents(Event.ONKEYDOWN);
	}
	
	private void scrollToSelectedRow() {
		int row = iTable.getSelectedRow();
		if (row < 0) return;
		
		Element scroll = iScroll.getElement();
		
		Element item = iTable.getRowFormatter().getElement(iTable.getSelectedRow());
		if (item==null) return;
		
		int realOffset = 0;
		while (item !=null && !item.equals(scroll)) {
			realOffset += item.getOffsetTop();
			item = item.getOffsetParent();
		}
		
		scroll.setScrollTop(realOffset - scroll.getOffsetHeight() / 2);
	}
	
	public void onBrowserEvent(final Event event) {
		switch (DOM.eventGetType(event)) {
		case Event.ONKEYDOWN:
			if (event.getKeyCode() == KeyCodes.KEY_DOWN && iTable.getRowCount() > 1 && iTable.getData(1) != null) {
				int row = iTable.getSelectedRow();
				if (row >= 0) iTable.setSelected(row, false);
				if (row < 0) { row = 1; }
				else { row ++; }
				if (row >= iTable.getRowCount()) row = 1;
				iTable.setSelected(row, true);
				scrollToSelectedRow();
				event.preventDefault();
				event.stopPropagation();
			}
			if (event.getKeyCode() == KeyCodes.KEY_UP && iTable.getRowCount() > 1 && iTable.getData(1) != null) {
				int row = iTable.getSelectedRow();
				if (row >= 0) iTable.setSelected(row, false);
				if (row < 0) { row = iTable.getRowCount() - 1; }
				else { row --; }
				if (row <= 0) row = iTable.getRowCount() - 1;;
				iTable.setSelected(row, true);
				scrollToSelectedRow();
				event.preventDefault();
				event.stopPropagation();
			}
			if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
				int row = iTable.getSelectedRow();
				if (row >= 0) {
					PersonInterface person = iTable.getData(row);
					if (person != null) {
						iTimer.cancel();
						Lookup.this.hide();
						setValue(person, true);
					}
				}
			}
			break;
		}
	}

	public void setQuery(String query) {
		iQuery.setText(query);
	}
	
	public void setOptions(String options) { iOptions = options; }
	
	public void update() {
		final String q = iQuery.getText().trim();
		if (q.equals(iLastQuery)) return;
		if (q.isEmpty()) {
			iTable.clearTable(1); 
			return;
		}
		iTable.clearTable(1);
		List<Widget> line = new ArrayList<Widget>();
		line.add(new LoadingImage());
		iTable.addRow(null, line);
		RPC.execute(new PersonInterface.LookupRequest(q, iOptions), new AsyncCallback<GwtRpcResponseList<PersonInterface>>() {
			@Override
			public void onSuccess(GwtRpcResponseList<PersonInterface> result) {
				iLastQuery = q;
				iTable.clearTable(1);
				boolean hasId = true;
				for (PersonInterface person: result) {
					List<Widget> line = new ArrayList<Widget>();
					line.add(new Label(person.getName(), false));
					line.add(new Label(person.getEmail(), false));
					line.add(new Label(person.getPhone(), false));
					line.add(new Label(person.getDepartment()));
					line.add(new Label(person.getSource()));
					iTable.addRow(person, line);
					if (person.getId() == null || person.getId().isEmpty() || "null".equals(person.getId())) {
						int row = iTable.getRowCount() - 1;
						for (int col = 0; col < iTable.getCellCount(row); col++)
							iTable.getCellFormatter().addStyleName(row, col, "unitime-Disabled");
						if (hasId) {
							hasId = false;
							if (row > 1)
								for (int col = 0; col < iTable.getCellCount(row); col++)
									iTable.getCellFormatter().addStyleName(row, col, "unitime-TopLineDash");
						}
					}
				}
				if (result.isEmpty()) {
					List<Widget> line = new ArrayList<Widget>();
					line.add(new ErrorLine("No person matching the query found.", false));
					iTable.addRow(null, line);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				iTable.clearTable(1);
				List<Widget> line = new ArrayList<Widget>();
				line.add(new ErrorLine(caught.getMessage(), true));
				iTable.addRow(null, line);
			}
		});
	}
	
	public static class ErrorLine extends Label implements HasColSpan, HasStyleName {
		private boolean iError;
		
		public ErrorLine(String text, boolean error) {
			super(text);
			iError = error;
		}

		@Override
		public int getColSpan() {
			return 5;
		}
		
		@Override
		public String getStyleName() {
			return (iError ? "unitime-ErrorMessage" : "unitime-Message");
		}
	}
	
	public static class LoadingImage extends Image implements HasColSpan, HasCellAlignment {
		public LoadingImage() {
			super(RESOURCES.loading_small());
		}

		@Override
		public int getColSpan() {
			return 5;
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_CENTER;
		}
	}
	
	public static native void createTriggers()/*-{
		$wnd.peopleLookup = function(query, callback, options) {
			@org.unitime.timetable.gwt.client.Lookup::peopleLookup(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(query,callback,options);
		};
	}-*/;
	
	public void center() {
		super.center();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				iLastQuery = null;
				iQuery.setFocus(true);
				iQuery.selectAll();
				update();
			}
		});
	}
	
	public static void peopleLookup(String query, final JavaScriptObject callback, String options) {
		final Lookup lookup = new Lookup();
		if (query != null && !query.trim().isEmpty())
			lookup.setQuery(query);
		lookup.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				if (event.getValue() != null)
					lookup.fireCallback(callback,
						event.getValue().getId(),
						event.getValue().getFirstName(),
						event.getValue().getMiddleName(),
						event.getValue().getLastName(),
						event.getValue().getEmail(),
						event.getValue().getPhone());				
			}
		});
		lookup.setOptions(options);
		lookup.center();
	}
	
	public native void fireCallback(JavaScriptObject callback, String... person)/*-{
		callback(person);
	}-*/;

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<PersonInterface> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public PersonInterface getValue() {
		return iValue;
	}

	@Override
	public void setValue(PersonInterface value) {
		setValue(value, false);
	}

	@Override
	public void setValue(PersonInterface value, boolean fireEvents) {
		iValue = value;
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}
}
