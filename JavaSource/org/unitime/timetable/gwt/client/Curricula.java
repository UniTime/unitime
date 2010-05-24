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

import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.widgets.WebTable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Curricula extends Composite {
	
	private TextBox iFilter = null;
	private Button iSearch = null;
	private WebTable iTable = null;
	
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);
	
	private AsyncCallback<List<String[]>> iLoadEnrollments;
	
	public Curricula() {
		VerticalPanel panel = new VerticalPanel();
		
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
		
		panel.add(filterPanel);
		panel.setCellHorizontalAlignment(filterPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		iTable = new WebTable();
		iTable.setHeader(
				new WebTable.Row(
						new WebTable.Cell("Curricula", 1, "200"),
						new WebTable.Cell("Academic Area", 1, "200"),
						new WebTable.Cell("Major(s)", 1, "200"),
						new WebTable.Cell("Department", 1, "200"),
						new WebTable.Cell("Expected Students", 1, "50"),
						new WebTable.Cell("Enrolled Students", 1, "50"),
						new WebTable.Cell("Last-like Students", 1, "50")
				));
		iTable.setEmptyMessage("No data.");
		
		panel.add(iTable);
		
		panel.setWidth("100%");
		
		initWidget(panel);
		
		iLoadEnrollments = new AsyncCallback<List<String[]>>() {
			public void onFailure(Throwable caught) {}
			public void onSuccess(List<String[]> enrollments) {
				if (iTable.getRows() == null || iTable.getRows().length == 0) return;
				List<String> noEnrl = new ArrayList<String>();
				for (String[] enrollment : enrollments) {
					String id = enrollment[0];
					String enrl = enrollment[1];
					boolean match = false;
					for (WebTable.Row row: iTable.getRows()) {
						if (id.equals(row.getId())) {
							row.setCell(5, new WebTable.Cell(enrl));
							match = true;
						} else if (noEnrl.size() < 10 && row.getId() != null && (row.getCells()[5].getValue()==null || row.getCells()[5].getValue().isEmpty())) {
							noEnrl.add(row.getId());
						}
					}
					if (!match) return;
				}
				if (!noEnrl.isEmpty())
					iService.getEnrollment(noEnrl, iLoadEnrollments);
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
	}
	
	private void loadCurricula() {
		iTable.clearData(true);
		iTable.setEmptyMessage("Loading data...");
		iService.findCurricula(iFilter.getText(), new AsyncCallback<List<String[]>>() {
			
			@Override
			public void onSuccess(List<String[]> result) {
				if (result.isEmpty()) {
					iTable.clearData(true);
					iTable.setEmptyMessage("No curricula matching the above filter found.");
				} else {
					WebTable.Row data[] = new WebTable.Row[result.size()];
					List<String> ids = new ArrayList<String>();
					for (int i = 0; i < result.size(); i++) {
						String[] line = result.get(i);
						String[] row = new String[line.length - 1];
						for (int j = 0; j < row.length; j++) row[j] = line[j + 1];
						data[i] = new WebTable.Row(row);
						data[i].setId(line[0]);
						if (ids.size() < 10) ids.add(line[0]);
					}
					iTable.setData(data);
					if (!ids.isEmpty())
						iService.getEnrollment(ids, iLoadEnrollments);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iTable.clearData(true);
				iTable.setEmptyMessage("<font color='red'>Unable to retrieve curricula (" + caught.getMessage() + ").</font>");
			}
		});
	}

}
