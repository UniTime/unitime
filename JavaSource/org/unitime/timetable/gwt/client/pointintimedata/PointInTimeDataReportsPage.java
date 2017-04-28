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
package org.unitime.timetable.gwt.client.pointintimedata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.PITDExecuteRpcRequest;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.PITDParametersInterface;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.PITDParametersRpcRequest;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.PITDQueriesRpcRequest;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.PITDSetBackRpcRequest;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.Report;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.Table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephanie Schluttenhofer
 */
public class PointInTimeDataReportsPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iForm = null;
	private UniTimeHeaderPanel iHeader = null, iTableHeader = null;;
	private UniTimeWidget<ListBox> iReportSelector = null;
	private HTML iDescription = null;
	
	private List<PointInTimeDataReportsInterface.Report> iReports = new ArrayList<PointInTimeDataReportsInterface.Report>();
	private List<PointInTimeDataReportsInterface.Parameter> iParameters = new ArrayList<PointInTimeDataReportsInterface.Parameter>();
	private UniTimeTable<String[]> iTable = new UniTimeTable<String[]>();
	private String iFirstField = null;
	private int iLastSort = 0;
	private String iLastHistory = null;
	
	public PointInTimeDataReportsPage() {
		UniTimePageLabel.getInstance().setPageName(MESSAGES.pagePointInTimeDataReports());
		
		iForm = new SimpleForm(2);
		
		iForm.removeStyleName("unitime-NotPrintableBottomLine");
		
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectFilter());
		iHeader.addButton("execute", MESSAGES.buttonExecute(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iLastSort = 0;
				execute();
			}
		});
		
		iHeader.addButton("print", MESSAGES.buttonPrint(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				PITDExecuteRpcRequest request = new PITDExecuteRpcRequest();
				String id = iReportSelector.getWidget().getValue(iReportSelector.getWidget().getSelectedIndex());
				for (PointInTimeDataReportsInterface.Report q: iReports) {
					if (id.equals(q.getId())) {
						request.setReport(q); break;
					}
				}
				if (request.getReport() == null) {
					iHeader.setErrorMessage(MESSAGES.errorNoReportSelected());
					return;
				}
				final SimpleForm form = new SimpleForm();
				form.addHeaderRow(request.getReport().getName());
				if (!request.getReport().getDescription().isEmpty())
					form.addRow(MESSAGES.propDescription(), new HTML(request.getReport().getDescription()));
				
				for (int i = 0; i < iParameters.size(); i++) {
					PointInTimeDataReportsInterface.Parameter parameter = iParameters.get(i);
					if (request.getReport().parametersContain(parameter.getType())) { 
						if (parameter.isTextField()) {
							TextBox textBox =  ((UniTimeWidget<TextBox>)iForm.getWidget(3 + i, 1)).getWidget();
							String value = textBox.getText();
							if (value.isEmpty()) {
								iHeader.setErrorMessage(MESSAGES.errorItemNotSelected(parameter.getName()));
								return;
							}
							request.addParameter(parameter.getType(), value);
							form.addRow(parameter.getName() + ":", new Label(value, true));							
						} else {
							ListBox list = ((UniTimeWidget<ListBox>)iForm.getWidget(3 + i, 1)).getWidget();
							String value = "";
							String values = "";
							boolean allSelected = true;
							if (list.isMultipleSelect()) {
								for (int j = 0; j < list.getItemCount(); j++)
									if (list.isItemSelected(j)) {
										if (!value.isEmpty()) { value += ","; values += ", "; }
										value += list.getValue(j);
										values += list.getItemText(j);
									} else {
										allSelected = false;
									}
								if (allSelected) values = MESSAGES.itemAll();
							} else if (list.getSelectedIndex() > 0) {
								value = list.getValue(list.getSelectedIndex());
								values = list.getItemText(list.getSelectedIndex());
							}
							if (value.isEmpty()) {
								iHeader.setErrorMessage(MESSAGES.errorItemNotSelected(parameter.getName()));
								return;
							}
							request.addParameter(parameter.getType(), value);
							form.addRow(parameter.getName() + ":", new Label(values, true));
						}
					}
				}
				LoadingWidget.getInstance().show(MESSAGES.waitExecuting(request.getReport().getName()));
				RPC.execute(request, new AsyncCallback<Table>() {
					@Override
					public void onFailure(Throwable caught) {
						iTableHeader.setErrorMessage(caught.getMessage());
						LoadingWidget.getInstance().hide();
					}

					@Override
					public void onSuccess(Table result) {
						LoadingWidget.getInstance().hide();
						if (result == null || result.size() <= 1) {
							iTableHeader.setMessage(MESSAGES.errorNoResults());
							return;
						} 
						final UniTimeTable<String[]> table = new UniTimeTable<String[]>();
						String firstField = null;
						int nrCols = 0;
						for (int i = 0; i < result.size(); i++) {
							String[] row = result.get(i);
							List<Widget> line = new ArrayList<Widget>();
							if (i == 0) {
								firstField = row[0]; nrCols = row.length;
								for (String x: row) {
									final String name = x.replace('_', ' ').trim();
									UniTimeTableHeader h = new UniTimeTableHeader(name, 1);
									line.add(h);
								}
							} else {
								for (String x: row) {
									line.add(new HTML(x == null ? "" : x.replace("\\n", "<br>")));
								}
							}
							table.addRow(i == 0 ? null : row, line);
						}
						if (firstField != null && firstField.startsWith("__"))
							table.setColumnVisible(0, false);
						if (iLastSort != 0 && Math.abs(iLastSort) <= nrCols) {
							table.sort(table.getHeader(Math.abs(iLastSort) - 1), new Comparator<String[]>() {
								@Override
								public int compare(String[] o1, String[] o2) {
									return PointInTimeDataReportsPage.compare(o1, o2, Math.abs(iLastSort) - 1);
								}
							}, iLastSort > 0);
						}
						table.getElement().getStyle().setWidth(1040, Unit.PX);
						
						// Move header row to thead
						Element headerRow = table.getRowFormatter().getElement(0);
						Element tableElement = table.getElement();
						Element thead = DOM.createTHead();
						tableElement.insertFirst(thead);
						headerRow.getParentElement().removeChild(headerRow);
						thead.appendChild(headerRow);
						
						final Element div = DOM.createDiv();
						div.appendChild(form.getElement());
						div.appendChild(table.getElement());
						
						String name = MESSAGES.pagePointInTimeDataReports();
						final String pageName = name;

						ToolBox.print(new ToolBox.Page() {
							@Override
							public String getName() { return pageName; }
							@Override
							public String getUser() { return ""; }
							@Override
							public String getSession() { return ""; }
							@Override
							public Element getBody() { return div; }
						});
						
					}
				});	
			}
		});

		iHeader.addButton("export", MESSAGES.buttonExportCSV(), 85, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String id = iReportSelector.getWidget().getValue(iReportSelector.getWidget().getSelectedIndex());
				PointInTimeDataReportsInterface.Report query = null;
				for (PointInTimeDataReportsInterface.Report q: iReports) {
					if (id.equals(q.getId())) {
						query = q; break;
					}
				}
				if (query == null) {
					iHeader.setErrorMessage(MESSAGES.errorNoReportSelected());
					return;
				}
				String params = "";
				for (int i = 0; i < iParameters.size(); i++) {
					PointInTimeDataReportsInterface.Parameter parameter = iParameters.get(i);
					if (query.parametersContain(parameter.getType())) {
						if (parameter.isTextField()) {
							TextBox textBox =  ((UniTimeWidget<TextBox>)iForm.getWidget(3 + i, 1)).getWidget();
							String value = textBox.getText();
							if (value.isEmpty()) {
								iHeader.setErrorMessage(MESSAGES.errorItemNotSelected(parameter.getName()));
								return;
							}
							if (!params.isEmpty()) params += ":";
							params += (value == null ? "" : value);
						} else {

							ListBox list = ((UniTimeWidget<ListBox>)iForm.getWidget(3 + i, 1)).getWidget();
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
							if (value.isEmpty()) {
								iHeader.setErrorMessage(MESSAGES.errorItemNotSelected(parameter.getName()));
								return;
							}
							if (!params.isEmpty()) params += ":";
							params += (list.isMultipleSelect() && allSelected ? "" : value);
						}
					}
				}
				String reportId = iReportSelector.getWidget().getValue(iReportSelector.getWidget().getSelectedIndex());
				
				RPC.execute(EncodeQueryRpcRequest.encode("output=pitd-report.csv&report=" + reportId + "&params=" + params + "&sort=" + iLastSort), new AsyncCallback<EncodeQueryRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {
					}
					@Override
					public void onSuccess(EncodeQueryRpcResponse result) {
						ToolBox.open(GWT.getHostPageBaseURL() + "export?q=" + result.getQuery());
					}
				});
			}
		});

				

		iForm.addHeaderRow(iHeader);
		iHeader.setEnabled("execute", false);
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export", false);

		iForm.getColumnFormatter().setWidth(0, "120px");
		iForm.getColumnFormatter().setWidth(1, "100%");

		iReportSelector = new UniTimeWidget<ListBox>(new ListBox());
		iForm.addRow(MESSAGES.propReport(), iReportSelector);
		iReportSelector.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iReportSelector.clearHint();
				iReportSelector.setPrintText(iReportSelector.getWidget().getItemText(iReportSelector.getWidget().getSelectedIndex()));
				queryChanged();
			}
		});
		iDescription = new HTML("");
		iForm.addRow(MESSAGES.propDescription(), iDescription);
		iForm.getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
		
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingReports());
		RPC.execute(new PITDParametersRpcRequest(), new AsyncCallback<PITDParametersInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(caught.getMessage());
				LoadingWidget.getInstance().hide();
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(PITDParametersInterface result) {
				iParameters = result.getParameters();
				for (int i = 0; i < iParameters.size(); i++) {
					PointInTimeDataReportsInterface.Parameter parameter = iParameters.get(i);
					if(parameter.isTextField()){
						 TextBox text = new TextBox();
						 if (parameter.getDefaultTextValue() != null){
							 text.setText(parameter.getDefaultTextValue());
							 text.setValue(parameter.getDefaultTextValue());
						 }
						final UniTimeWidget<TextBox> u = new UniTimeWidget<TextBox>(text);
						iForm.addRow(parameter.getName() + ":", u);
						iForm.getCellFormatter().setVerticalAlignment(3 + i, 0, HasVerticalAlignment.ALIGN_TOP);
						iForm.getRowFormatter().setVisible(3 + i, false);
						u.setPrintText(text.getValue());
						text.addChangeHandler(new ChangeHandler() {
							@Override
							public void onChange(ChangeEvent event) {
								u.clearHint();
								String entered = u.getWidget().getValue();
								u.setPrintText(entered);
								iHeader.clearMessage();
							}
						});
					} else {
						ListBox list = new ListBox();
						list.setMultipleSelect(parameter.isMultiSelect());
						if (!parameter.isMultiSelect())
							list.addItem(MESSAGES.itemSelect(), "-1");
						for (PointInTimeDataReportsInterface.IdValue v: parameter.values())
							list.addItem(v.getText(), v.getValue());
						final UniTimeWidget<ListBox> u = new UniTimeWidget<ListBox>(list);
						iForm.addRow(parameter.getName() + ":", u);
						iForm.getCellFormatter().setVerticalAlignment(3 + i, 0, HasVerticalAlignment.ALIGN_TOP);
						iForm.getRowFormatter().setVisible(3 + i, false);
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
				}
				iTableHeader = new UniTimeHeaderPanel(MESSAGES.sectResults());
				iForm.addHeaderRow(iTableHeader);
				iForm.addRow(iTable);
				iForm.addBottomRow(iHeader.clonePanel(""));
				loadQueries(null, true);
			}
		});

		iTable.addMouseClickListener(new UniTimeTable.MouseClickListener<String[]>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<String[]> event) {
				if (event.getRow() > 0 && event.getData() != null) {
					if ("__Class".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "classDetail.do?cid=" + event.getData()[0]);
					else if ("__Offering".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?op=view&io=" + event.getData()[0]);
					else if ("__Subpart".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "schedulingSubpartDetail.do?ssuid=" + event.getData()[0]);
					else if ("__Room".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=rooms&back=1&id=" + event.getData()[0]);
					else if ("__Instructor".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "instructorDetail.do?instructorId=" + event.getData()[0]);
					else if ("__Exam".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "examDetail.do?examId=" + event.getData()[0]);
					else if ("__Event".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=events#event=" + event.getData()[0]);
				}
			}
		});
		
		initWidget(iForm);
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				reload(event.getValue());
			}
		});
	}
	
		
	public void loadQueries(final Long select, final boolean reload) {
		if (!LoadingWidget.getInstance().isShowing())
			LoadingWidget.getInstance().show(MESSAGES.waitLoadingReports());
		RPC.execute(new PITDQueriesRpcRequest(), new AsyncCallback<GwtRpcResponseList<Report>>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(caught.getMessage());
				LoadingWidget.getInstance().hide();
			}
			@Override
			public void onSuccess(GwtRpcResponseList<Report> result) {
				String selected = (select == null ? null : select.toString());
				if (selected == null && iReportSelector.getWidget().getSelectedIndex() >= 0) {
					selected = iReportSelector.getWidget().getValue(iReportSelector.getWidget().getSelectedIndex());
				}
				iReportSelector.getWidget().clear();
				if (result.isEmpty()) {
					iHeader.setErrorMessage(MESSAGES.errorNoReportsAvailable());
				} else {
					iReportSelector.getWidget().addItem(MESSAGES.itemSelect(), "-1");
					iReports = result;
					for (int i = 0; i < result.size(); i++) {
						iReportSelector.getWidget().addItem(result.get(i).getName(), result.get(i).getId().toString());
						if (selected != null && selected.equals(result.get(i).getId().toString()))
							iReportSelector.getWidget().setSelectedIndex(1 + i);
					}
					queryChanged();
				}
				LoadingWidget.getInstance().hide();
				if (reload) reload(History.getToken());
			}
		});
	}
	
	private void queryChanged() {
		iHeader.clearMessage();
		if (iReportSelector.getWidget().getSelectedIndex() <= 0) {
			iHeader.setEnabled("execute", false);
			iDescription.setHTML("");
			for (int i = 0; i < iParameters.size(); i++)
				iForm.getRowFormatter().setVisible(3 + i, false);
		} else {
			iHeader.setEnabled("execute", true);
			String id = iReportSelector.getWidget().getValue(iReportSelector.getWidget().getSelectedIndex());
			for (PointInTimeDataReportsInterface.Report rpt: iReports) {
				if (id.equals(rpt.getId())) {
					iDescription.setHTML(rpt.getDescription());
					for (int i = 0; i < iParameters.size(); i++) {
						PointInTimeDataReportsInterface.Parameter parameter = iParameters.get(i);
						iForm.getRowFormatter().setVisible(3 + i, rpt.parametersContain(parameter.getType()));
					}
				}
			}
		}
	}
	
	public static int compare(String[] a, String[] b, int col) {
		for (int i = 0; i < a.length; i++) {
			int c = (col + i) % a.length;
			try {
				int cmp = Double.valueOf(a[c] == null ? "0" : a[c]).compareTo(Double.valueOf(b[c] == null ? "0" : b[c]));
				if (cmp != 0) return cmp;
			} catch (NumberFormatException e) {
				int cmp = (a[c] == null ? "" : a[c]).compareTo(b[c] == null ? "" : b[c]);
				if (cmp != 0) return cmp;
			}
		}
		return 0;
	}
	
	public void populate(Table result) {
		if (result == null || result.size() <= 1) {
			iTableHeader.setMessage(MESSAGES.errorNoResults());
		} else {
			for (int i = 0; i < result.size(); i++) {
				String[] row = result.get(i);
				List<Widget> line = new ArrayList<Widget>();
				if (i == 0) {
					iFirstField = row[0];
					for (String x: row) {
						final String name = x.replace('_', ' ').trim();
						final UniTimeTableHeader h = new UniTimeTableHeader(name, 1);
						final int col = line.size();
						h.addOperation(new UniTimeTableHeader.Operation() {
							@Override
							public void execute() {
								iTable.sort(col, new Comparator<String[]>() {
									@Override
									public int compare(String[] o1, String[] o2) {
										return PointInTimeDataReportsPage.compare(o1, o2, col);
									}
								});
								iLastSort = (h.getOrder() != null && h.getOrder() ? (1 + col) : -1 - col);
								History.newItem(iLastHistory + ":" + iLastSort, false);
								setBack();
							}
							@Override
							public boolean isApplicable() {
								return true;
							}
							@Override
							public boolean hasSeparator() {
								return false;
							}
							@Override
							public String getName() {
								return MESSAGES.opSortBy(name);
							}
						});
						line.add(h);
					}
				} else {
					for (String x: row) {
						line.add(new HTML(x == null ? "" : x.replace("\\n", "<br>")));
					}
				}
				iTable.addRow(i == 0 ? null : row, line);
			}
			if (iFirstField != null && iFirstField.startsWith("__"))
				iTable.setColumnVisible(0, false);
			iHeader.setEnabled("print", true);
			iHeader.setEnabled("export", iTable.getRowCount() > 1);
			iTableHeader.setMessage(MESSAGES.infoShowingAllLines(result.size() - 1));
			if (iLastSort != 0) {
				iTable.sort(iTable.getHeader(Math.abs(iLastSort) - 1), new Comparator<String[]>() {
					@Override
					public int compare(String[] o1, String[] o2) {
						return PointInTimeDataReportsPage.compare(o1, o2, Math.abs(iLastSort) - 1);
					}
				}, iLastSort > 0);
			}
		}
		setBack();
	}
	
	public void setBack() {
		if (iFirstField == null || !iFirstField.startsWith("__") || iTable.getRowCount() <= 1) return;
		PITDSetBackRpcRequest request = new PITDSetBackRpcRequest();
		for (int i = 1; i < iTable.getRowCount(); i++) {
			String[] row = iTable.getData(i);
			if (row != null) {
				Long id = Long.valueOf(row[0]);
				request.addId(id);
			}
		}
		request.setHistory(History.getToken());
		request.setType(iFirstField);
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
	
	public void reload(String history) {
		if (history == null) return;
		if (history.indexOf('&') >= 0)
			history = history.substring(0, history.indexOf('&')); 
		if (history.isEmpty()) return;
		String[] params = history.split(":");
		String id = params[0];
		PointInTimeDataReportsInterface.Report rpt = null;
		for (int i = 0; i < iReports.size(); i++) {
			PointInTimeDataReportsInterface.Report q = iReports.get(i);
			if (id.equals(q.getId())) {
				rpt = q;
				iReportSelector.getWidget().setSelectedIndex(1 + i);
				queryChanged();
				break;
			}
		}
		if (rpt == null) return;
		int idx = 1;
		for (int i = 0; i < iParameters.size(); i++) {
			PointInTimeDataReportsInterface.Parameter parameter = iParameters.get(i);
			if (rpt.parametersContain(parameter.getType())) {
				String param = params[idx++];
				if (param == null || param.isEmpty()) continue;
				if (parameter.isTextField()) {
					TextBox text = ((UniTimeWidget<TextBox>)iForm.getWidget(3 + i, 1)).getWidget();
					text.setText(param);
				} else {
					ListBox list = ((UniTimeWidget<ListBox>)iForm.getWidget(3 + i, 1)).getWidget();
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
		}
		iLastSort = Integer.parseInt(params[idx++]);
		execute();
	}
	
	private void execute() {
		PITDExecuteRpcRequest request = new PITDExecuteRpcRequest();
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export", false);

		String id = iReportSelector.getWidget().getValue(iReportSelector.getWidget().getSelectedIndex());
		for (PointInTimeDataReportsInterface.Report r: iReports) {
			if (id.equals(r.getId())) {
				request.setReport(r); break;
			}
		}
		if (request.getReport() == null) {
			iHeader.setErrorMessage(MESSAGES.errorNoReportSelected());
			return;
		}
		iLastHistory = request.getReport().getId().toString();
		for (int i = 0; i < iParameters.size(); i++) {
			PointInTimeDataReportsInterface.Parameter parameter = iParameters.get(i);
			if (request.getReport().parametersContain(parameter.getType())) {
				if (parameter.isTextField()) {
					TextBox textBox =  ((UniTimeWidget<TextBox>)iForm.getWidget(3 + i, 1)).getWidget();
					String value = textBox.getText();
					if (value.isEmpty()) {
						iHeader.setErrorMessage(MESSAGES.errorItemNotSelected(parameter.getName()));
						return;
					}
					request.addParameter(parameter.getType(), value);
					iLastHistory += ":" + (value == null ? "" : value);
				} else {
					PointInTimeDataReportsInterface.IdValue o = new PointInTimeDataReportsInterface.IdValue();
					o.setValue(parameter.getType());
					ListBox list = ((UniTimeWidget<ListBox>)iForm.getWidget(3 + i, 1)).getWidget();
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
					if (value.isEmpty()) {
						iHeader.setErrorMessage(MESSAGES.errorItemNotSelected(parameter.getName()));
						return;
					}
					request.addParameter(parameter.getType(), value);
					iLastHistory += ":" + (list.isMultipleSelect() && allSelected ? "" : value);
				}
			}
		}
		
		iTable.clearTable(); iFirstField = null;
		iTableHeader.clearMessage();
		iHeader.clearMessage();
		LoadingWidget.getInstance().show(MESSAGES.waitExecuting(request.getReport().getName()));
		History.newItem(iLastHistory + ":" + iLastSort, false);
		RPC.execute(request, new AsyncCallback<Table>() {
			@Override
			public void onFailure(Throwable caught) {
				iTableHeader.setErrorMessage(caught.getMessage());
				LoadingWidget.getInstance().hide();
			}

			@Override
			public void onSuccess(Table result) {
				populate(result);
				LoadingWidget.getInstance().hide();
			}
		});		
	}
	
}
