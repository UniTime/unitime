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
package org.unitime.timetable.gwt.client.teachingschedule;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseGroupDivision;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.ListTeachingSchedules;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.GetSubjectAreas;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.SubjectArea;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseOutListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseOverListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.TeachingScheduleMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class TeachingSchedulesPage extends SimpleForm {
	protected static TeachingScheduleMessages MESSAGES = GWT.create(TeachingScheduleMessages.class);
	protected static GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private UniTimeHeaderPanel iTitleAndButtons;
	private ListBox iSubjectAreas;
	private int iTableRow;
	private UniTimeTable<TableRow> iTable;
	
	public TeachingSchedulesPage() {
		
		iTitleAndButtons = new UniTimeHeaderPanel(MESSAGES.sectCourseMeetings());
		iTitleAndButtons.addButton("search", MESSAGES.buttonSearch(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				search();
			}
		});
		iTitleAndButtons.addButton("add", MESSAGES.buttonAddNew(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open("gwt.jsp?page=editTeachingSchedule&subject=" + iSubjectAreas.getSelectedValue());
			}
		});
		addHeaderRow(iTitleAndButtons);
		
		iSubjectAreas = new ListBox();
		iSubjectAreas.setMultipleSelect(false);
		iSubjectAreas.setWidth("150px");
		iSubjectAreas.setStyleName("unitime-TextBox");
		iSubjectAreas.addItem(MESSAGES.itemAllSubjects(), "");
		RPC.execute(new GetSubjectAreas(), new AsyncCallback<GwtRpcResponseList<SubjectArea>>() {
			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(GwtRpcResponseList<SubjectArea> result) {
				for (SubjectArea sa: result) {
					iSubjectAreas.addItem(sa.getReference(), sa.getId().toString());
				}
				if (Window.Location.getParameter("subject") != null) {
					String subject = Window.Location.getParameter("subject");
					if (subject.isEmpty()) {
						iSubjectAreas.setSelectedIndex(0); search();
					} else {
						for (int i = 1; i < iSubjectAreas.getItemCount(); i++) {
							if (subject.equals(iSubjectAreas.getValue(i)) || subject.equals(iSubjectAreas.getItemText(i))) {
								iSubjectAreas.setSelectedIndex(i); search();
							}
						}
					}
				}
			}
		});
		addRow(MESSAGES.filterSubjectArea(), iSubjectAreas);
		
		iTable = new UniTimeTable<TableRow>();
		iTable.addStyleName("unitime-OfferingDivisions");
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(MESSAGES.colOffering()));
		header.add(new UniTimeTableHeader(MESSAGES.colDivisionName()));
		header.add(new UniTimeTableHeader(MESSAGES.colDivisionType()));
		header.add(new UniTimeTableHeader(MESSAGES.colDivisionAttribute()));
		header.add(new UniTimeTableHeader(MESSAGES.colDivisionClasses()));
		header.add(new UniTimeTableHeader(MESSAGES.colDivisionGroups()));
		header.add(new UniTimeTableHeader(MESSAGES.colDivisionHours()));
		header.add(new UniTimeTableHeader(MESSAGES.colDivisionParallels()));
		for (int i = 4; i < header.size(); i++)
			header.get(i).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		iTable.addRow(null, header);
		iTable.addMouseClickListener(new MouseClickListener<TableRow>() {
			@Override
			public void onMouseClick(TableEvent<TableRow> event) {
				if (event.getData() == null) return;
				ToolBox.open("gwt.jsp?page=teachingSchedule&course=" + event.getData().getOffering().getCourseId());
			}
		});
		
		iTableRow = addRow(iTable);
		getRowFormatter().setVisible(iTableRow, false);
		
		addBottomRow(iTitleAndButtons.clonePanel(""));
		
		iTable.addMouseOverListener(new MouseOverListener<TeachingSchedulesPage.TableRow>() {
			@Override
			public void onMouseOver(TableEvent<TableRow> event) {
				if (event.getData() != null) {
					for (int row = event.getRow() - 1; row > 0; row--) {
						TableRow r = iTable.getData(row);
						if (r != null && r.getOffering().equals(event.getData().getOffering()))
							iTable.getRowFormatter().addStyleName(row, "unitime-TableRowHover");
					}
					for (int row = event.getRow() + 1; row < iTable.getRowCount(); row++) {
						TableRow r = iTable.getData(row);
						if (r != null && r.getOffering().equals(event.getData().getOffering()))
							iTable.getRowFormatter().addStyleName(row, "unitime-TableRowHover");
					}
				}
			}
		});
		iTable.addMouseOutListener(new MouseOutListener<TeachingSchedulesPage.TableRow>() {
			@Override
			public void onMouseOut(TableEvent<TableRow> event) {
				if (event.getData() != null) {
					for (int row = event.getRow() - 1; row > 0; row--) {
						TableRow r = iTable.getData(row);
						if (r != null && r.getOffering().equals(event.getData().getOffering()))
							iTable.getRowFormatter().removeStyleName(row, "unitime-TableRowHover");
					}
					for (int row = event.getRow() + 1; row < iTable.getRowCount(); row++) {
						TableRow r = iTable.getData(row);
						if (r != null && r.getOffering().equals(event.getData().getOffering()))
							iTable.getRowFormatter().removeStyleName(row, "unitime-TableRowHover");
					}
				}
			}
		});
	}
	
	protected void search() {
		getRowFormatter().setVisible(iTableRow, false);
		RPC.execute(new ListTeachingSchedules(iSubjectAreas.getSelectedIndex() == 0 ? null : Long.valueOf(iSubjectAreas.getSelectedValue())), new AsyncCallback<GwtRpcResponseList<TeachingSchedule>>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(caught.getMessage(), caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<TeachingSchedule> result) {
				iTable.clearTable(1);
				for (TeachingSchedule offering: result) {
					int idx = 0;
					for (CourseGroupDivision cd: offering.getDivisions()) {
						addRow(offering, cd, idx);
						idx++;
					}
				}
				getRowFormatter().setVisible(iTableRow, true);				
			}
		});
	}
	
	protected void addRow(TeachingSchedule offering, CourseGroupDivision cd, int idx) {
		List<Widget> line = new ArrayList<Widget>();
		line.add(new Label(idx == 0 ? offering.getCourseName() : "\u00A0"));
		line.add(new Label(cd.getDivision().getName()));
		line.add(new Label(cd.getDivision().getDivisionIndex() == 0 ? cd.getGroup().getType() : "\u00A0"));
		line.add(new Label(cd.getDivision().hasAttributeRef() ? cd.getDivision().getAttributeRef() : "\u00A0"));
		line.add(new NumberLabel(cd.getDivision().getDivisionIndex() == 0 ? cd.getGroup().getNrClasses() : null));
		line.add(new NumberLabel(cd.getDivision().getDivisionIndex() == 0 ? cd.getGroup().getNrGroups() : null));
		line.add(new NumberLabel(cd.getDivision().getHours()));
		line.add(new NumberLabel(cd.getDivision().getNrParalel()));
		if (idx == 0) {
			for (Widget w: line)
				w.addStyleName("first-line");
			if (iTable.getRowCount() > 1)
				for (int c = 0; c < iTable.getCellCount(iTable.getRowCount() - 1); c++)
					iTable.getWidget(iTable.getRowCount() - 1, c).addStyleName("last-line");
		}
		iTable.addRow(new TableRow(offering, cd), line);
	}
	
	static class TableRow {
		TeachingSchedule iOffering;
		CourseGroupDivision iDivision;
		public TableRow(TeachingSchedule offering, CourseGroupDivision division) {
			iOffering = offering;
			iDivision = division;
		}
		public TeachingSchedule getOffering() { return iOffering; }
		public CourseGroupDivision getDivision() { return iDivision; }
	}
	
	public static class NumberLabel extends Label {
		NumberLabel(Integer value) {
			super(value == null ? "\u00A0" : value.toString());
			getElement().getStyle().setTextAlign(TextAlign.RIGHT);
		}
	}

}
