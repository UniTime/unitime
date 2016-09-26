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
package org.unitime.timetable.gwt.client.rooms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.SimpleForm.HasMobileScroll;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.LastChangesInterface.ChangeLogInterface;
import org.unitime.timetable.gwt.shared.LastChangesInterface.LastChangesRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class RoomNoteChanges extends Composite implements HasMobileScroll {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	
	private Long iLocationId = null;
	
	private SimpleForm iChangesPanel;
	private UniTimeTable<ChangeLogInterface> iChanges;
	private UniTimeHeaderPanel iHeader;
	private CheckBox iMultiSessionToggle;
	
	public RoomNoteChanges() {
		iChangesPanel = new SimpleForm();
		iChangesPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectRoomNoteHistory());
		iHeader.setCollapsible(LastChangesCookie.getInstance().getShowLastChanges());
		iHeader.setTitleStyleName("unitime3-HeaderTitle");
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				LastChangesCookie.getInstance().setShowLastChanges(event.getValue());
				if (iChanges.getRowCount() == 0)
					refresh();
				else if (iChanges.getRowCount() > 1) {
					for (int row = 1; row < iChanges.getRowCount(); row++) {
						iChanges.getRowFormatter().setVisible(row, event.getValue());
					}
				}
				iMultiSessionToggle.setVisible(event.getValue());
			}
		});
		
		iChangesPanel.addHeaderRow(iHeader);
		iHeader.getElement().getStyle().setMarginTop(10, Unit.PX);
		
		iChanges = new UniTimeTable<ChangeLogInterface>();
		iChanges.setWidth("100%");
		iChangesPanel.addRow(iChanges);
		
		iMultiSessionToggle = new CheckBox(MESSAGES.checkAllSessions());
		iMultiSessionToggle.setValue(LastChangesCookie.getInstance().isMultiSession());
		iMultiSessionToggle.getElement().getStyle().setFloat(Float.RIGHT);
		iChangesPanel.addRow(iMultiSessionToggle);
		iMultiSessionToggle.setVisible(iHeader.isCollapsible());
		
		iMultiSessionToggle.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				LastChangesCookie.getInstance().setMultiSession(event.getValue());
				refresh();
			}
		});
		
		
		initWidget(iChangesPanel);
	}
	
	private void refresh() {
		clear(true);
		if (iLocationId != null) {
			RPC.execute(LastChangesRequest.createRequest("org.unitime.timetable.model.Location", iLocationId, "multi-session", (iMultiSessionToggle.getValue() ? "true" : "false"), "page", "ROOM_EDIT", "operation", "NOTE"), new AsyncCallback<GwtRpcResponseList<ChangeLogInterface>>() {
				@Override
				public void onFailure(Throwable caught) {
					iHeader.setErrorMessage(MESSAGES.failedLoadRoomNoteChanges(caught.getMessage()));
					iHeader.setCollapsible(null);
					LastChangesCookie.getInstance().setShowLastChanges(false);
				}

				@Override
				public void onSuccess(GwtRpcResponseList<ChangeLogInterface> result) {
					if (result.isEmpty()) {
						iHeader.setMessage(MESSAGES.noRoomNoteChanges());
					} else {
						populate(result);
						if (iChanges.getRowCount() > 1) {
							for (int row = 1; row < iChanges.getRowCount(); row++) {
								iChanges.getRowFormatter().setVisible(row, LastChangesCookie.getInstance().getShowLastChanges());
							}
						}
						iHeader.clearMessage();
					}
				}
				
			});
		}
	}
	
	private void clear(boolean loading) {
		for (int row = iChanges.getRowCount() - 1; row >= 0; row--) {
			iChanges.removeRow(row);
		}
		iChanges.clear(true);
		if (loading)
			iHeader.showLoading();
		else
			iHeader.clearMessage();
	}
	
	private Comparator<ChangeLogInterface> comparator(int column, final boolean order) {
		switch (column) {
		case 0:
			return new Comparator<ChangeLogInterface>() {
				@Override
				public int compare(ChangeLogInterface c1, ChangeLogInterface c2) {
					return (order ? -1 : 1) * c1.getDate().compareTo(c2.getDate());
				}
			};
		case 1:
			return new Comparator<ChangeLogInterface>() {
				@Override
				public int compare(ChangeLogInterface c1, ChangeLogInterface c2) {
					int cmp = c1.getSessionDate().compareTo(c2.getSessionDate());
					if (cmp != 0) return (order ? -cmp : cmp);
					cmp = c1.getSessionInitiative().compareTo(c2.getSessionInitiative());
					if (cmp != 0) return (order ? cmp : -cmp);
					cmp = c1.getSession().compareTo(c2.getSession());
					if (cmp != 0) return (order ? cmp : -cmp);
					return (order ? -1 : 1) * c1.getDate().compareTo(c2.getDate());
				}
			};
		case 2:
			return new Comparator<ChangeLogInterface>() {
				@Override
				public int compare(ChangeLogInterface c1, ChangeLogInterface c2) {
					int cmp = (c1.getManager() == null ? "" : c1.getManager()).compareTo(c2.getManager() == null ? "" : c2.getManager());
					if (cmp != 0) return (order ? cmp : -cmp);
					cmp = c1.getSessionDate().compareTo(c2.getSessionDate());
					if (cmp != 0) return (order ? -cmp : cmp);
					cmp = c1.getSessionInitiative().compareTo(c2.getSessionInitiative());
					if (cmp != 0) return (order ? cmp : -cmp);
					cmp = c1.getSession().compareTo(c2.getSession());
					if (cmp != 0) return (order ? cmp : -cmp);
					return (order ? -1 : 1) * c1.getDate().compareTo(c2.getDate());
				}
			};
		case 3:
			return new Comparator<ChangeLogInterface>() {
				@Override
				public int compare(ChangeLogInterface c1, ChangeLogInterface c2) {
					int cmp = new HTML(c1.getObject() == null ? "" : c1.getObject()).getText().compareTo(new HTML(c2.getObject() == null ? "" : c2.getObject()).getText());
					if (cmp != 0) return (order ? cmp : -cmp);
					cmp = c1.getSessionDate().compareTo(c2.getSessionDate());
					if (cmp != 0) return (order ? -cmp : cmp);
					cmp = c1.getSessionInitiative().compareTo(c2.getSessionInitiative());
					if (cmp != 0) return (order ? cmp : -cmp);
					cmp = c1.getSession().compareTo(c2.getSession());
					if (cmp != 0) return (order ? cmp : -cmp);
					return (order ? -1 : 1) * c1.getDate().compareTo(c2.getDate());
				}
			};
		default:
			return new Comparator<ChangeLogInterface>() {
				@Override
				public int compare(ChangeLogInterface c1, ChangeLogInterface c2) {
					return (order ? -1 : 1) * c1.getDate().compareTo(c2.getDate());
				}
			};
		}
	}
	
	private ClickHandler clickHandler(final int column) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeTableHeader header = (UniTimeTableHeader)event.getSource();
				final boolean order = (header.getOrder() == null ? true : !header.getOrder());
				iChanges.sort(header, comparator(column, order));
				LastChangesCookie.getInstance().setSort(column, order);
				header.setOrder(order);
			}
		};
	}
	
	private void populate(GwtRpcResponseList<ChangeLogInterface> logs) {
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		
		header.add(new UniTimeTableHeader(MESSAGES.colDate(), clickHandler(0)));
		header.add(new UniTimeTableHeader(MESSAGES.colAcademicSession(), clickHandler(1)));
		header.add(new UniTimeTableHeader(MESSAGES.colManager(), clickHandler(2)));
		header.add(new UniTimeTableHeader(MESSAGES.colNote(), clickHandler(3)));
		iChanges.addRow(null, header);
		
		for (ChangeLogInterface log: logs) {
			List<Widget> line = new ArrayList<Widget>();
			
			line.add(new Label(sDateFormat.format(log.getDate()), false));
			line.add(new Label(log.getSession(), false));
			line.add(new HTML(log.getManager() == null ? "<i>" + MESSAGES.notApplicable() + "</i>" : log.getManager(), false));
			HTML note = new HTML(log.getObject() == null || log.getObject().isEmpty() || "-".equals(log.getObject()) ? "<i>" + MESSAGES.emptyNote() + "</i>" : log.getObject());
			note.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
			line.add(note);
			
			iChanges.addRow(log, line);
			iChanges.getRowFormatter().setVerticalAlign(iChanges.getRowCount() - 1, HasVerticalAlignment.ALIGN_TOP);
		}
		
		if (LastChangesCookie.getInstance().getSortColumn() >= 0) {
			iChanges.sort((UniTimeTableHeader)null, comparator(LastChangesCookie.getInstance().getSortColumn(), LastChangesCookie.getInstance().getSortOrder()));
			header.get(LastChangesCookie.getInstance().getSortColumn()).setOrder(LastChangesCookie.getInstance().getSortOrder());
		}
		
		iChanges.setColumnVisible(1, iMultiSessionToggle.getValue());
	}
	
	public void insert(final RootPanel panel) {
		load(Long.valueOf(panel.getElement().getInnerText()));
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
	}
	
	public void load(Long locationId) {
		iLocationId = locationId;
		if (LastChangesCookie.getInstance().getShowLastChanges()) {
			refresh();
		} else {
			clear(false);
			iHeader.clearMessage();
			iHeader.setCollapsible(false);
		}
	}
	
	public static class LastChangesCookie {
		private boolean iShowDetails = false;
		private int iSortColumn = -1;
		private boolean iSortOrder = true;
		private boolean iMultiSession = false;
		
		private static LastChangesCookie sInstance = null;
		
		private LastChangesCookie() {
			try {
				String cookie = Cookies.getCookie("UniTime:LastChanges");
				if (cookie != null && cookie.length() > 0) {
					String[] values = cookie.split(":");
					iShowDetails = "T".equals(values[0]);
					iSortColumn = Integer.parseInt(values[1]);
					iSortOrder = "T".equals(values[2]);
					iMultiSession = "T".equals(values[3]);
				}
			} catch (Exception e) {
			}
		}
		
		private void save() {
			String cookie = (iShowDetails ? "T": "F") + ":" + iSortColumn + ":" + (iSortOrder ? "T": "F") + ":" + (iMultiSession ? "T" : "F");
			Cookies.setCookie("UniTime:LastChanges", cookie);
		}
		
		public static LastChangesCookie getInstance() {
			if (sInstance == null)
				sInstance = new LastChangesCookie();
			return sInstance;
		}
		
		public boolean getShowLastChanges() {
			return iShowDetails;
		}
		
		public void setShowLastChanges(boolean details) {
			iShowDetails = details;
			save();
		}
		
		public int getSortColumn() {
			return iSortColumn;
		}
		

		public boolean getSortOrder() {
			return iSortOrder;
		}

		public void setSort(int column, boolean order) {
			iSortColumn = column;
			iSortOrder = order;
			save();
		}
		
		public boolean isMultiSession() {
			return iMultiSession;
		}
		
		public void setMultiSession(boolean multi) {
			iMultiSession = multi;
			save();
		}
	}

}
