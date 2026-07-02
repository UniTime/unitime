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
package org.unitime.timetable.gwt.client.hql;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLSetBackRpcRequest;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.IdValue;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.Table;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.TestHQLRequest;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.TestHQLRequest.Operation;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.TestHQLResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;

public class TestHQLPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	private SimpleForm iForm = null;
	private UniTimeHeaderPanel iHeader = null, iFooter = null, iTableHeader = null, iTableFooter = null, iGenSQLHeader = null;
	private TextArea iQuery;
	private ResultsTable iTable;
	
	private List<SavedHQLInterface.Option> iOptions = new ArrayList<SavedHQLInterface.Option>();

	private String iLastHistory = null;
	private int iFirstLine = 0;
	private int iLastSort = 0;
	private Label iGeneratedSQL = null;
	private int iTableHeaderLine, iGenSQLHeaderLine;
	
	public TestHQLPage() {
		iForm = new SimpleForm(2);
		iForm.removeStyleName("unitime-NotPrintableBottomLine");
		initWidget(iForm);
		addStyleName("unitime-TestHQLPage");
		
		iHeader = new UniTimeHeaderPanel(COURSE.sectHQL());
		iHeader.addButton("execute", MESSAGES.buttonExecute(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iFirstLine = 0;
				iLastSort = 0;
				iTable.clearTable();
				execute();
			}
		});
		iHeader.addButton("print", MESSAGES.buttonPrint(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final TestHQLRequest request = new TestHQLRequest(Operation.EXECUTE);
				request.setQuery(iQuery.getText());
				if (!request.hasQuery()) {
					iHeader.setErrorMessage(COURSE.errorQueryIsRequired());
					return;
				} else {
					iHeader.clearMessage();
				}
				for (int i = 0; i < iOptions.size(); i++) {
					SavedHQLInterface.Option option = iOptions.get(i);
					if (request.getQuery().contains("%" + option.getType() + "%")) {
						SavedHQLInterface.IdValue o = new SavedHQLInterface.IdValue();
						o.setValue(option.getType());
						ListBox list = ((UniTimeWidget<ListBox>)iForm.getWidget(2 + i, 1)).getWidget();
						String value = "";
						boolean allSelected = true;
						if (list.isMultipleSelect()) {
							for (int j = 0; j < list.getItemCount(); j++)
								if (list.isItemSelected(j)) {
									if (!value.isEmpty()) value += ",";
									value += list.getValue(j);
								} else {
									allSelected = false;
								}
						} else if (list.getSelectedIndex() > 0) {
							value = list.getValue(list.getSelectedIndex());
						}
						if (value.isEmpty() && !list.isMultipleSelect()) {
							iHeader.setErrorMessage(MESSAGES.errorItemNotSelected(option.getName()));
							return;
						}
						request.addOption(option.getType(), list.isMultipleSelect() && allSelected ? "" : value);
					}
				}
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				request.setFromRow(0); request.setMaxRows(-1);
				RPC.execute(request, new AsyncCallback<TestHQLResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						iTableHeader.setErrorMessage(caught.getMessage());
						LoadingWidget.getInstance().hide();
					}

					@Override
					public void onSuccess(TestHQLResponse response) {
						LoadingWidget.getInstance().hide();
						if (!response.hasTable() || response.getTable().size() <=1) {
							iTableHeader.setMessage(MESSAGES.errorNoResults());
							return;
						} 
						Table result = response.getTable();
						final ResultsTable table = new ResultsTable(null);
						table.setData(result);
						if (table.getFirstField() != null && table.getFirstField().startsWith("__"))
							table.setColumnVisible(0, false);
						if (iLastSort != 0 && Math.abs(iLastSort) <= table.getCellCount(0)) {
							table.sort(table.getHeader(Math.abs(iLastSort) - 1), new Comparator<String[]>() {
								@Override
								public int compare(String[] o1, String[] o2) {
									return SavedHQLPage.compare(o1, o2, Math.abs(iLastSort) - 1);
								}
							}, iLastSort > 0);
						}
						table.getElement().getStyle().setWidth(1040, Unit.PX);
						
						final Element div = DOM.createDiv();
						div.appendChild(table.getElement());
						
						ToolBox.print(new ToolBox.Page() {
							@Override
							public String getName() { return MESSAGES.pageTestHQL(); }
							@Override
							public String getUser() { return ""; }
							@Override
							public String getSession() { return ""; }
							@Override
							public Element getBody() { return table.getElement(); }
						});
						
					}
				});	
			}
		});
		iHeader.addButton("export-csv", MESSAGES.buttonExportCSV(), 85, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("hql-test.csv");
			}
		});
		iHeader.addButton("export-xls", MESSAGES.buttonExportXLS(), 85, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("hql-test.xls");
			}
		});
		/*
		iHeader.addButton("clear-cache", COURSE.actionClearCache(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iHeader.clearMessage();
				RPC.execute(new TestHQLRequest(Operation.CLEAR_CACHE), new AsyncCallback<TestHQLResponse>() {
					@Override
					public void onSuccess(TestHQLResponse result) {
						UniTimeNotifications.info(MESSAGES.hibernateCacheCleared());
					}
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(caught.getMessage());
						LoadingWidget.getInstance().hide();
						ToolBox.checkAccess(caught);
					}
				});
			}
		});*/

		iTable = new ResultsTable(iHeader) {
			@Override
			protected void onSort(int lastSort) {
				iLastSort = lastSort;
				History.newItem(iLastHistory + ":" + iFirstLine + ":" + iLastSort, false);
				setBack();
			}
		};
		
		iForm.addHeaderRow(iHeader);
		iHeader.setEnabled("execute", false);
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export-csv", false);
		iHeader.setEnabled("export-xls", false);
		
		iQuery = new TextArea();
		iQuery.addStyleName("unitime-TextArea");
		iQuery.setWidth("800px");
		iQuery.setHeight("300px");
		String q = Window.Location.getParameter("query");
		if (q != null) iQuery.setText(q);
		iForm.addRow(COURSE.propQuery(), iQuery);
		
		iQuery.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				queryChanged();
			}
		});
		
		RPC.execute(new TestHQLRequest(Operation.LOAD), new AsyncCallback<TestHQLResponse>() {
			@Override
			public void onSuccess(TestHQLResponse result) {
				iOptions = result.getOptions();
				iTable.setMaxRows(result.getMaxRows());
				for (int i = 0; i < iOptions.size(); i++) {
					SavedHQLInterface.Option option = iOptions.get(i);
					ListBox list = new ListBox();
					list.setMultipleSelect(option.isMultiSelect());
					if (!option.isMultiSelect())
						list.addItem(MESSAGES.itemSelect(), "-1");
					for (SavedHQLInterface.IdValue v: option.values())
						list.addItem(v.getText(), v.getValue());
					final UniTimeWidget<ListBox> u = new UniTimeWidget<ListBox>(list);
					iForm.addRow(option.getName() + ":", u);
					iForm.getCellFormatter().setVerticalAlignment(2 + i, 0, HasVerticalAlignment.ALIGN_TOP);
					iForm.getRowFormatter().setVisible(2 + i, false);
					if (list.isMultipleSelect()) {
						for (int j = 0; j < list.getItemCount(); j++)
							list.setItemSelected(j, true);
						u.setPrintText(MESSAGES.itemAll());
					} else if (list.getItemCount() == 2) {
						list.setSelectedIndex(1);
						u.setPrintText(list.getItemText(1));
					}
					list.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							u.clearHint();
							String selected = "";
							boolean hasAll = true;
							for (int i = 0; i < u.getWidget().getItemCount(); i++) {
								if (u.getWidget().isItemSelected(i)) {
									if (!selected.isEmpty()) selected += ",";
									selected += u.getWidget().getItemText(i);
								} else hasAll = false;
							}
							if (hasAll && u.getWidget().getItemCount() > 5)
								selected = MESSAGES.itemAll();
							if (selected.length() > 150)
								selected = selected.substring(0, 147) + "...";
							u.setPrintText(selected);
							iHeader.clearMessage();
						}
					});
				}
				iTableHeader = new UniTimeHeaderPanel(MESSAGES.sectResults());
				iTableHeader.addButton("previous", MESSAGES.buttonPrevious(), 75, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						iFirstLine -= iTable.getMaxRows();
						if (iFirstLine < 0) iFirstLine = 0;
						execute();
					}
				});
				iTableHeader.addButton("next", MESSAGES.buttonNext(), 75, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						iFirstLine += iTable.getMaxRows();
						execute();
					}
				});
				iTableHeader.setEnabled("previous", false);
				iTableHeader.setEnabled("next", false);
				iTableHeaderLine = iForm.addHeaderRow(iTableHeader);
				iForm.getRowFormatter().setVisible(iTableHeaderLine, false);
				iTable.addStyleName("unitime-HQLTable");
				ScrollPanel tableScroll = new ScrollPanel(iTable);
				tableScroll.addStyleName("unitime-HQLTablePanel");
				iForm.addRow(tableScroll);
				iTableFooter = iTableHeader.clonePanel("");
				iTableFooter.setVisible(false);
				iForm.addRow(iTableFooter);
				iFooter = iHeader.clonePanel("");
				iForm.addBottomRow(iFooter);
				
				iGenSQLHeader = new UniTimeHeaderPanel(COURSE.sectGeneratedSQL());
				iGenSQLHeaderLine = iForm.addHeaderRow(iGenSQLHeader);
				iForm.getRowFormatter().setVisible(iGenSQLHeaderLine, false);
				iGeneratedSQL = new Label(); iGeneratedSQL.addStyleName("generated-sql");
				ScrollPanel genSqlScroll = new ScrollPanel(iGeneratedSQL);
				genSqlScroll.addStyleName("generated-sql-panel");
				iForm.addRow(genSqlScroll);
				
				queryChanged();
				reload(History.getToken());
			}
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(caught.getMessage());
				LoadingWidget.getInstance().hide();
				ToolBox.checkAccess(caught);
			}
		});
		
		iTable.setVisible(false);
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				reload(event.getValue());
			}
		});
	}
	
	protected void queryChanged() {
		iHeader.setEnabled("execute", iOptions != null);
		for (int i = 0; i < iOptions.size(); i++) {
			SavedHQLInterface.Option option = iOptions.get(i);
			iForm.getRowFormatter().setVisible(2 + i, iQuery.getText().contains("%" + option.getType() + "%"));
		}
	}
	
	protected void execute() {
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export-csv", false);
		iHeader.setEnabled("export-xls", false);
		final TestHQLRequest request = new TestHQLRequest(Operation.EXECUTE);
		request.setQuery(iQuery.getText());
		if (!request.hasQuery()) {
			iHeader.setErrorMessage(COURSE.errorQueryIsRequired());
			return;
		} else {
			iHeader.clearMessage();
		}
		for (int i = 0; i < iOptions.size(); i++) {
			SavedHQLInterface.Option option = iOptions.get(i);
			if (request.getQuery().contains("%" + option.getType() + "%")) {
				SavedHQLInterface.IdValue o = new SavedHQLInterface.IdValue();
				o.setValue(option.getType());
				ListBox list = ((UniTimeWidget<ListBox>)iForm.getWidget(2 + i, 1)).getWidget();
				String value = "";
				boolean allSelected = true;
				if (list.isMultipleSelect()) {
					for (int j = 0; j < list.getItemCount(); j++)
						if (list.isItemSelected(j)) {
							if (!value.isEmpty()) value += ",";
							value += list.getValue(j);
						} else {
							allSelected = false;
						}
				} else if (list.getSelectedIndex() > 0) {
					value = list.getValue(list.getSelectedIndex());
				}
				if (value.isEmpty() && !list.isMultipleSelect()) {
					iHeader.setErrorMessage(MESSAGES.errorItemNotSelected(option.getName()));
					return;
				}
				request.addOption(option.getType(), list.isMultipleSelect() && allSelected ? "" : value);
			}
		}
		iGeneratedSQL.setText("");
		iForm.getRowFormatter().setVisible(iGenSQLHeaderLine, false);
		iTable.clearTable();
		iTableHeader.clearMessage();
		iHeader.clearMessage();
		LoadingWidget.getInstance().show(MESSAGES.waitPlease());
		request.setFromRow(iFirstLine);
		RPC.execute(request, new AsyncCallback<TestHQLResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iTableHeader.setVisible(true);
				iTableHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage());
				LoadingWidget.getInstance().hide();
			}

			@Override
			public void onSuccess(TestHQLResponse result) {
				populate(request, result);
				LoadingWidget.getInstance().hide();
			}
		});
	}
	
	protected void populate(TestHQLRequest request, TestHQLResponse response) {
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export-csv", false);
		iHeader.setEnabled("export-xls", false);
		iForm.getRowFormatter().setVisible(iTableHeaderLine, true);
		if (response.hasTable()) {
			iLastHistory = getQueryHash(request.getQuery());
			if (request.hasOptions())
				for (IdValue id: request.getOptions())
					iLastHistory += ":" + id.getText();
			History.newItem(iLastHistory + ":" + request.getFromRow() + ":" + iLastSort, false);
			Table result = response.getTable();
			if (result == null || result.size() <= 1) {
				iTableHeader.setMessage(MESSAGES.errorNoResults());
				iTableHeader.setEnabled("next", false);
				iTableHeader.setEnabled("previous", false);
				iTableFooter.setVisible(false);
			} else {
				iTable.setData(result);
				iHeader.setEnabled("print", true);
				iHeader.setEnabled("export", iTable.getRowCount() > 1);
				iHeader.setEnabled("exportXls", iTable.getRowCount() > 1);
				if (result.size() == iTable.getMaxRows() + 2)
					iTableHeader.setMessage(MESSAGES.infoShowingLines(iFirstLine + 1, iFirstLine + iTable.getMaxRows()));
				else if (iFirstLine > 0)
					iTableHeader.setMessage(MESSAGES.infoShowingLines(iFirstLine + 1, iFirstLine + result.size() - 1));
				else 
					iTableHeader.setMessage(MESSAGES.infoShowingAllLines(result.size() - 1));
				iTableHeader.setEnabled("next", iTable.getMaxRows() > 0 && result.size() > (iTable.getMaxRows() + 1));
				iTableHeader.setEnabled("previous", iTable.getMaxRows() > 0 && iFirstLine > 0);
				iTableFooter.setVisible(iTableHeader.isEnabled("previous") || iTableHeader.isEnabled("next"));
				if (iLastSort != 0) {
					iTable.sort(iTable.getHeader(Math.abs(iLastSort) - 1), new Comparator<String[]>() {
						@Override
						public int compare(String[] o1, String[] o2) {
							return SavedHQLPage.compare(o1, o2, Math.abs(iLastSort) - 1);
						}
					}, iLastSort > 0);
				}
			}
			iTable.setVisible(iTable.getRowCount() > 0);
			iHeader.setEnabled("print", iTable.getRowCount() > 0);
			iHeader.setEnabled("export-csv", iTable.getRowCount() > 0);
			iHeader.setEnabled("export-xls", iTable.getRowCount() > 0);
			setBack();
		}
		if (response.hasMessage())
			iTableHeader.setMessage(response.getMessage());
		if (response.hasSQL()) {
			iGeneratedSQL.setText(response.getSQL());
			iGenSQLHeader.setVisible(true);
			iForm.getRowFormatter().setVisible(iGenSQLHeaderLine, true);
		} else {
			iGeneratedSQL.setText("");
			iForm.getRowFormatter().setVisible(iGenSQLHeaderLine, false);
		}
	}
	
	public void reload(String history) {
		if (history == null) return;
		if (history.indexOf('&') >= 0)
			history = history.substring(0, history.indexOf('&')); 
		if (history.isEmpty()) return;
		String[] params = history.split(":");
		String hash = params[0];
		String query = getQueryFromHash(hash);
		if (query == null) return;
		iQuery.setText(query);
		queryChanged();
		int idx = 1;
		for (int i = 0; i < iOptions.size(); i++) {
			SavedHQLInterface.Option option = iOptions.get(i);
			if (query.contains("%" + option.getType() + "%")) {
				String param = params[idx++];
				if (param == null || param.isEmpty()) continue;
				ListBox list = ((UniTimeWidget<ListBox>)iForm.getWidget(2 + i, 1)).getWidget();
				if (list.isMultipleSelect()) {
					for (int j = 0; j < list.getItemCount(); j++) {
						String value = list.getValue(j);
						boolean contains = false;
						for (String o: param.split(",")) if (o.equals(value)) { contains = true; break; }
						list.setItemSelected(j, contains);
					}
				} else {
					for (int j = 1; j < list.getItemCount(); j++) {
						if (list.getValue(j).equals(param)) {
							list.setSelectedIndex(j); break;
						}
					}
				}
			}
		}
		iFirstLine = Integer.parseInt(params[idx++]);
		if (params[idx].indexOf('?') > 0)
			params[idx] = params[idx].substring(0, params[idx].indexOf('?'));
		iLastSort = Integer.parseInt(params[idx]);
		execute();
	}
	
	public void setBack() {
		if (iTable.getFirstField() == null || !iTable.getFirstField().startsWith("__") || iTable.getRowCount() <= 1) return;
		HQLSetBackRpcRequest request = new HQLSetBackRpcRequest();
		for (int i = 1; i < iTable.getRowCount(); i++) {
			String[] row = iTable.getData(i);
			if (row != null) {
				Long id = Long.valueOf(row[0]);
				request.addId(id);
			}
		}
		request.setAppearance("TestHQL");
		request.setHistory(History.getToken());
		request.setType(iTable.getFirstField());
		RPC.execute(request, new AsyncCallback<GwtRpcResponseNull>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(GwtRpcResponseNull result) {
			}
		});
	}
	
	protected String getQueryHash(String query) {
		return URL.encodeQueryString(query);
	}
	
	protected String getQueryFromHash(String hash) {
		try {
			return URL.decodeQueryString(hash);
		} catch (Exception e) {
			return hash;
		}
	}
	
	protected void export(String file) {
		if (iQuery.getText().isEmpty()) {
			iHeader.setErrorMessage(COURSE.errorQueryIsRequired());
			return;
		} else {
			iHeader.clearMessage();
		}
		String query = iQuery.getText();
		String params = "";
		for (int i = 0; i < iOptions.size(); i++) {
			SavedHQLInterface.Option option = iOptions.get(i);
			if (query.contains("%" + option.getType() + "%")) {
				ListBox list = ((UniTimeWidget<ListBox>)iForm.getWidget(2 + i, 1)).getWidget();
				String value = "";
				boolean allSelected = true;
				if (list.isMultipleSelect()) {
					for (int j = 0; j < list.getItemCount(); j++)
						if (list.isItemSelected(j)) {
							if (!value.isEmpty()) value += ",";
							value += list.getValue(j);
						} else {
							allSelected = false;
						}
				} else if (list.getSelectedIndex() > 0) {
					value = list.getValue(list.getSelectedIndex());
				}
				if (value.isEmpty() && !list.isMultipleSelect()) {
					iHeader.setErrorMessage(MESSAGES.errorItemNotSelected(option.getName()));
					return;
				}
				if (!params.isEmpty()) params += ":";
				params += (list.isMultipleSelect() && allSelected ? "" : value);
			}
		}
		RPC.execute(EncodeQueryRpcRequest.encode("output=" + file + "&hql=" + URL.encodeQueryString(query) + "&params=" + params + "&sort=" + iLastSort), new AsyncCallback<EncodeQueryRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(EncodeQueryRpcResponse result) {
				ToolBox.open(GWT.getHostPageBaseURL() + result.getExportUrl());
			}
		});
	}
}
