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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class SectioningReports extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static NumberFormat PF = NumberFormat.getFormat("0.0%");
	private static NumberFormat DF = NumberFormat.getFormat("0.00");
	
	private SimpleForm iForm = null;
	private UniTimeHeaderPanel iHeader = null, iTableHeader = null;;
	private UniTimeWidget<ListBox> iReportSelector = null;
	private RowData iHead = null;
	private List<RowData> iData = new ArrayList<RowData>();
	
	private UniTimeTable<RowData> iTable = new UniTimeTable<RowData>();
	private int iFirstLine = 0;
	private int iLastSort = 0;
	private String iLastHistory = null;
	private boolean iOnline = false;
	
	public static enum ReportType {
		TIME_CONFLICTS("Time Conflicts", "org.cpsolver.studentsct.report.SectionConflictTable", "type", "OVERLAPS", "overlapsIncludeAll", "true"),
		AVAILABLE_CONFLICTS("Availability Conflicts", "org.cpsolver.studentsct.report.SectionConflictTable", "type", "UNAVAILABILITIES", "overlapsIncludeAll", "true"),
		SECTION_CONFLICTS("Time & Availability Conflicts", "org.cpsolver.studentsct.report.SectionConflictTable", "type", "OVERLAPS_AND_UNAVAILABILITIES", "overlapsIncludeAll", "true"),
		UNBALANCED_SECTIONS("Unbalanced Classes", "org.cpsolver.studentsct.report.UnbalancedSectionsTable"),
		DISTANCE_CONFLICTS("Distance Conflicts", "org.cpsolver.studentsct.report.DistanceConflictTable"),
		TIME_OVERLAPS("Time Overlaps", "org.cpsolver.studentsct.report.TimeOverlapConflictTable"),
		REQUEST_GROUPS("Request Groups", "org.cpsolver.studentsct.report.RequestGroupTable")
		;
		
		String iName, iImplementation;
		String[] iParameters;
		ReportType(String name, String implementation, String... params) {
			iName = name; iImplementation = implementation; iParameters = params;
		}
		
		public String getName() { return iName; }
		public String getImplementation() { return iImplementation; }
		public String[] getParameters() { return iParameters; }
	}
	
	public SectioningReports(boolean online) {
		iOnline = online;
		iForm = new SimpleForm(2);
		
		iForm.removeStyleName("unitime-NotPrintableBottomLine");
		
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectFilter());
		iHeader.addButton("execute", MESSAGES.buttonExecute(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iFirstLine = 0;
				iLastSort = 0;
				execute();
			}
		});
		
		iHeader.addButton("print", MESSAGES.buttonPrint(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final UniTimeTable<RowData> table = new UniTimeTable<RowData>();
				List<Widget> header = new ArrayList<Widget>();
				for (int i = 0; i < iHead.getLength(); i++) {
					String x = iHead.getCell(i);
					String name = x.replace('_', ' ').trim();
					UniTimeTableHeader h = new UniTimeTableHeader(name.replaceAll("\\n", "<br>"), 1);
					if (i + 1 == iLastSort)
						h.setOrder(true);
					else if (-1 - i == iLastSort)
						h.setOrder(false);
					header.add(h);
				}
				table.addRow(null, header);
				RowData prev = null;
				for (int i = 0; i < iData.size(); i++) {
					RowData row = iData.get(i);
					List<Widget> line = new ArrayList<Widget>();
					boolean prevHide = true;
					for (int x = 0; x < table.getCellCount(0); x++) {
						boolean hide = true;
						if (prev == null || !prevHide || !prev.getCell(x).equals(row.getCell(x))) hide = false;
						String text = row.getCell(x).replaceAll("\\n", "<br>");
						boolean number = false;
						try {
							Double.parseDouble(text);
							number = true;
						} catch (Exception e) {}
						if (iHead.getCell(x).contains("%") && number)
							text = PF.format(Double.parseDouble(text));
						else if (text.matches("[\\-]?[0-9]+\\.[0-9]+") && number)
							text = DF.format(Double.parseDouble(text)); 
						line.add(number ? new NumberCell(hide ? "" : text) : new HTML(hide ? "" : text));
						prevHide = hide;
					}
					int last = table.addRow(row, line);
					if (prev != null && !prev.getCell(0).equals(row.getCell(0)))
						for (int c = 0; c < table.getCellCount(last); c++)
							table.getCellFormatter().addStyleName(last, c, "unitime-TopLineDash");
					prev = row;
				}
				if (iHead.getCell(0).startsWith("__"))
					table.setColumnVisible(0, false);
				table.getElement().getStyle().setWidth(1040, Unit.PX);
				
				// Move header row to thead
				Element headerRow = table.getRowFormatter().getElement(0);
				Element tableElement = table.getElement();
				Element thead = DOM.createTHead();
				tableElement.insertFirst(thead);
				headerRow.getParentElement().removeChild(headerRow);
				thead.appendChild(headerRow);
				

				final String name = iReportSelector.getWidget().getItemText(iReportSelector.getWidget().getSelectedIndex());

				ToolBox.print(new ToolBox.Page() {
					@Override
					public String getName() { return name; }
					@Override
					public String getUser() { return ""; }
					@Override
					public String getSession() { return ""; }
					@Override
					public Element getBody() { return table.getElement(); }
				});
			}
		});

		iHeader.addButton("export", MESSAGES.buttonExportCSV(), 85, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iReportSelector.getWidget().getSelectedIndex() <= 0) {
					iHeader.setErrorMessage(MESSAGES.errorNoReportSelected());
					return;
				}
				ReportType type = ReportType.valueOf(iReportSelector.getWidget().getValue(iReportSelector.getWidget().getSelectedIndex()));
				String query = "output=sct-report.csv&name=" + type.name() + "&report=" + type.getImplementation() + "&online=" + (iOnline ? "true" : "false") + "&sort=" + iLastSort;
				for (int i = 0; i + 1 < type.getParameters().length; i += 2)
					query += "&" + type.getParameters()[i] + "=" + type.getParameters()[i + 1];
				
				RPC.execute(EncodeQueryRpcRequest.encode(query), new AsyncCallback<EncodeQueryRpcResponse>() {
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
		iReportSelector.getWidget().addItem(MESSAGES.itemSelect(), "");
		for (ReportType type: ReportType.values())
			iReportSelector.getWidget().addItem(type.getName(), type.name());
		iForm.addRow(MESSAGES.propReport(), iReportSelector);
		iReportSelector.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iReportSelector.clearHint();
				iReportSelector.setPrintText(iReportSelector.getWidget().getItemText(iReportSelector.getWidget().getSelectedIndex()));
				queryChanged();
			}
		});
		
		iTableHeader = new UniTimeHeaderPanel(MESSAGES.sectResults());
		iTableHeader.addButton("previous", MESSAGES.buttonPrevious(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iFirstLine -= 100;
				populate(false);
				History.newItem(iLastHistory + ":" + iFirstLine + ":" + iLastSort, false);
			}
		});
		iTableHeader.addButton("next", MESSAGES.buttonNext(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iFirstLine += 100;
				populate(false);
				History.newItem(iLastHistory + ":" + iFirstLine + ":" + iLastSort, false);
			}
		});
		iTableHeader.setEnabled("previous", false);
		iTableHeader.setEnabled("next", false);
		iForm.addHeaderRow(iTableHeader);
		iForm.addRow(iTable);
		iForm.addBottomRow(iHeader.clonePanel(""));
		
		initWidget(iForm);
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				reload(event.getValue());
			}
		});
		
		iTable.addMouseClickListener(new UniTimeTable.MouseClickListener<RowData>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<RowData> event) {
				if (event.getRow() > 0 && event.getData() != null) {
					if ("__Class".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "classDetail.do?cid=" + event.getData().getCell(0));
					else if ("__Offering".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?op=view&io=" + event.getData().getCell(0));
					else if ("__Subpart".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "schedulingSubpartDetail.do?ssuid=" + event.getData().getCell(0));
					else if ("__Room".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "roomDetail.do?id=" + event.getData().getCell(0));
					else if ("__Instructor".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "instructorDetail.do?instructorId=" + event.getData().getCell(0));
					else if ("__Exam".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "examDetail.do?examId=" + event.getData().getCell(0));
					else if ("__Event".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=events#event=" + event.getData().getCell(0));
				}
			}
		});
		
		reload(History.getToken());
	}
	
	private void queryChanged() {
		iHeader.clearMessage();
		if (iReportSelector.getWidget().getSelectedIndex() <= 0) {
			iHeader.setEnabled("execute", false);
			iHeader.setEnabled("export", false);
		} else {
			iHeader.setEnabled("execute", true);
			iHeader.setEnabled("export", true);
		}
	}
	
	public void populate(boolean sort) {
		if (iData == null || iData.isEmpty() || iHead == null) {
			iTableHeader.setMessage(MESSAGES.errorNoResults());
		} else {
			if (iLastSort != 0 && sort) {
				final boolean asc = iLastSort > 0;
				final int col = Math.abs(iLastSort) - 1;
				Collections.sort(iData, new Comparator<RowData>() {
					@Override
					public int compare(RowData o1, RowData o2) {
						return (asc ? o1.compareTo(o2, col) : o2.compareTo(o1, col));
					}
				});
			}
			List<Widget> header = new ArrayList<Widget>();
			iTable.clearTable();
			for (int i = 0; i < iHead.getLength(); i++) {
				String x = iHead.getCell(i);
				final String name = x.replace('_', ' ').trim();
				final UniTimeTableHeader h = new UniTimeTableHeader(name.replaceAll("\\n", "<br>"), 1);
				final int col = header.size();
				h.addOperation(new UniTimeTableHeader.Operation() {
					@Override
					public void execute() {
						boolean asc = (h.getOrder() == null ? true : !h.getOrder());
						iLastSort = (asc ? 1 + col : -1 - col);
						populate(true);
						History.newItem(iLastHistory + ":" + iFirstLine + ":" + iLastSort, false);
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
				if (i + 1 == iLastSort)
					h.setOrder(true);
				else if (-1 - i == iLastSort)
					h.setOrder(false);
				header.add(h);
			}
			iTable.addRow(null, header);
			RowData prev = null;
			for (int i = iFirstLine; i < Math.min(iFirstLine + 100, iData.size()); i++ ) {
				RowData row = iData.get(i);
				List<Widget> line = new ArrayList<Widget>();
				boolean prevHide = true;
				for (int x = 0; x < iTable.getCellCount(0); x++) {
					boolean hide = true;
					if (prev == null || !prevHide || !prev.getCell(x).equals(row.getCell(x))) hide = false;
					String text = row.getCell(x).replaceAll("\\n", "<br>");
					boolean number = false;
					try {
						Double.parseDouble(text);
						number = true;
					} catch (Exception e) {}
					if (iHead.getCell(x).contains("%") && number)
						text = PF.format(Double.parseDouble(text));
					else if (text.matches("[\\-]?[0-9]+\\.[0-9]+") && number)
						text = DF.format(Double.parseDouble(text)); 
					line.add(number ? new NumberCell(hide ? "" : text) : new HTML(hide ? "" : text));
					prevHide = hide;
				}
				int last = iTable.addRow(row, line);
				if (prev != null && !prev.getCell(0).equals(row.getCell(0)))
					for (int c = 0; c < iTable.getCellCount(last); c++)
						iTable.getCellFormatter().addStyleName(last, c, "unitime-TopLineDash");
				prev = row;
			}
			iTable.setColumnVisible(0, !iHead.getCell(0).startsWith("__"));
			iHeader.setEnabled("print", true);
			iHeader.setEnabled("export", true);
			if (iData.size() <= 100 && iFirstLine == 0)
				iTableHeader.setMessage(MESSAGES.infoShowingAllLines(iData.size()));
			else
				iTableHeader.setMessage(MESSAGES.infoShowingLines(iFirstLine + 1, Math.min(iFirstLine + 100, iData.size())));
			iTableHeader.setEnabled("next", iFirstLine + 100 < iData.size());
			iTableHeader.setEnabled("previous", iFirstLine > 0);
		}
	}
		
	public void reload(String history) {
		if (history == null) return;
		if (history.indexOf('&') >= 0)
			history = history.substring(0, history.indexOf('&')); 
		if (history.isEmpty()) return;
		String[] params = history.split(":");
		ReportType type = ReportType.valueOf(params[0]);
		if (type == null) return;
		iReportSelector.getWidget().setSelectedIndex(1 + type.ordinal());
		queryChanged();
		iFirstLine = Integer.parseInt(params[1]);
		iLastSort = Integer.parseInt(params[2]);
		execute();
	}
	
	private void execute() {
		iHeader.setEnabled("print", false);
		
		if (iReportSelector.getWidget().getSelectedIndex() <= 0) {
			iHeader.setErrorMessage(MESSAGES.errorNoReportSelected());
			return;
		}
		
		ReportType type = ReportType.valueOf(iReportSelector.getWidget().getValue(iReportSelector.getWidget().getSelectedIndex()));
		iLastHistory = type.name();
		iTable.clearTable();
		iTableHeader.clearMessage();
		iHeader.clearMessage();
		LoadingWidget.getInstance().show(MESSAGES.waitExecuting(type.getName()));
		History.newItem(iLastHistory + ":" + iFirstLine + ":" + iLastSort, false);
		
		SectioningReportRpcRequest request = new SectioningReportRpcRequest();
		request.setParameter("report", type.getImplementation());
		request.setParameter("online", iOnline ? "true" : "false");
		for (int i = 0; i + 1 < type.getParameters().length; i += 2)
			request.setParameter(type.getParameters()[i], type.getParameters()[i + 1]);
		
		RPC.execute(request, new AsyncCallback<SectioningReportRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iTableHeader.setErrorMessage(caught.getMessage(), true);
				LoadingWidget.getInstance().hide();
			}
			@Override
			public void onSuccess(SectioningReportRpcResponse result) {
				iData.clear();
				RowData prev = null;
				for (int i = 0; i < result.getReport().size(); i ++) {
					String[] row = result.getReport().get(i);
					if (row.length == 0) continue;
					RowData data = new RowData(row);
					while (prev != null) {
						if (data.getNrBlanks() > prev.getNrBlanks()) break;
						prev = prev.getParent();
					}
					if (prev != null)
						data.setParent(prev);
					if (i == 0) {
						iHead = data;
					} else {
						iData.add(data);
						prev = data;
					}
				}
				populate(true);
				LoadingWidget.getInstance().hide();
			}
		});
	}
	
	public static class SectioningReportRpcRequest implements GwtRpcRequest<SectioningReportRpcResponse> {
		Map<String, String> iParameters = new HashMap<String, String>();
		public SectioningReportRpcRequest() {}
		
		public void setParameter(String name, String value) { iParameters.put(name, value); }
		
		public Map<String, String> getParameters() { return iParameters; }
		
		@Override
		public String toString() {
			return getParameters().toString();
		}
	}
	
	public static class SectioningReportRpcResponse implements GwtRpcResponse {
		private List<String[]> iReport = new ArrayList<String[]>();
		
		public SectioningReportRpcResponse() {}
		
		public void addLine(String[] line) {
			iReport.add(line);
		}
		
		public List<String[]> getReport() {
			return iReport;
		}
	}
	
	public static class NumberCell extends HTML implements HasCellAlignment {
		public NumberCell(String text) {
			super(text, false);
		}
		
		public NumberCell(int text) {
			super(String.valueOf(text), false);
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class RowData {
		String[] iRow;
		RowData iParent;
		
		public RowData(String[] row) {
			iRow = row;
		}
		
		public String[] getRow() { return iRow; }
		
		public RowData getParent() { return iParent; }
		
		public void setParent(RowData parent) { iParent = parent; }
		
		public boolean isBlank(int col) {
			return getRow().length <= col || getRow()[col] == null || getRow()[col].isEmpty();
		}
		
		public String getCell(int col) {
			if (isBlank(col)) {
				if (getParent() != null)
					return getParent().getCell(col);
				else
					return "";
			} else {
				return getRow()[col];
			}
		}
		
		public int getLevel() {
			return getParent() == null ? 0 : getParent().getLevel() + 1;
		}
		
		public int getLength() {
			return getParent() == null ? getRow().length : getParent().getLength();
		}
		
		public int getNrBlanks() {
			for (int i = 0; i < getLength(); i++)
				if (!isBlank(i)) return i;
			return getLength();
		}
		
		public boolean isAllBlank() {
			return getNrBlanks() == getLength();
		}
		
		public int compareTo(RowData b, int col) {
			RowData a = this;
			while (a.getLevel() > b.getLevel()) a = a.getParent();
			while (b.getLevel() > a.getLevel()) b = b.getParent();
			try {
				int cmp = Double.valueOf(a.getCell(col) == null ? "0" : a.getCell(col)).compareTo(Double.valueOf(b.getCell(col) == null ? "0" : b.getCell(col)));
				if (cmp != 0) return cmp;
			} catch (NumberFormatException e) {
				int cmp = (a.getCell(col) == null ? "" : a.getCell(col)).compareTo(b.getCell(col) == null ? "" : b.getCell(col));
				if (cmp != 0) return cmp;
			}
			return 0;
		}
		
	}
}