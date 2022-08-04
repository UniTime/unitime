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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasStyleName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.SectioningProperties;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessageType;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverPageMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverPageMessagesRequest;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;

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
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class SectioningReports extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static NumberFormat PF = NumberFormat.getFormat("0.0%");
	private static NumberFormat DF = NumberFormat.getFormat("0.00");
	
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	protected static final StudentSectioningMessages SCT_MSG = GWT.create(StudentSectioningMessages.class);
	
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
	private List<ReportTypeInterface> iReportTypes = null;
	
	private SectioningProperties iSectioningProperties = null;
	private Set<Long> iSelectedStudentIds = new HashSet<Long>();
	private Set<StudentStatusInfo> iStates = null;
	private StudentStatusDialog iStudentStatusDialog = null;
	private Map<Long, List<CheckBox>> iStudentId2Checks = new HashMap<Long, List<CheckBox>>();
	
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
						String text = "";
						boolean number = true;
						int idx = 0;
						for (String t: row.getCell(x).split("\\n")) {
							boolean n = false;
							try {
								Double.parseDouble(t);
								n = true;
							} catch (Exception e) {}
							if (iHead.getCell(x).contains("%") && n)
								t = PF.format(Double.parseDouble(t));
							else if (t.matches("[\\-]?[0-9]+\\.[0-9]+(<br>[\\-]?[0-9]+\\.[0-9]+)*") && n)
								t = DF.format(Double.parseDouble(t));
							else if (t.matches("[\\-]?[0-9]+(,[0-9][0-9][0-9])*(\\.[0-9]+)? ?%?"))
								n = true;
							if (!n) number = false;
							if (idx > 0) text += "<br>";
							text += t;
							idx ++;
						}
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
				ReportTypeInterface type = getReportType(iReportSelector.getWidget().getValue(iReportSelector.getWidget().getSelectedIndex()));
				String query = "output=sct-report.csv&name=" + type.getReference() + "&report=" + type.getImplementation() + "&online=" + (iOnline ? "true" : "false") + "&sort=" + iLastSort;
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
		RPC.execute(new SectioningReportTypesRpcRequest(iOnline), new AsyncCallback<GwtRpcResponseList<ReportTypeInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				iTableHeader.setErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(GwtRpcResponseList<ReportTypeInterface> result) {
				iReportTypes = result;
				iReportSelector.getWidget().addItem(MESSAGES.itemSelect(), "");
				for (ReportTypeInterface type: result)
					iReportSelector.getWidget().addItem(type.getName(), type.getReference());
				reload(History.getToken());
			}
		});
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
						ToolBox.open(GWT.getHostPageBaseURL() + "classDetail.action?cid=" + event.getData().getCell(0));
					else if ("__Offering".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?op=view&io=" + event.getData().getCell(0));
					else if ("__Subpart".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "schedulingSubpartDetail.action?ssuid=" + event.getData().getCell(0));
					else if ("__Room".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=rooms&back=1&id=" + event.getData().getCell(0));
					else if ("__Instructor".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "instructorDetail.action?instructorId=" + event.getData().getCell(0));
					else if ("__Exam".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "examDetail.action?examId=" + event.getData().getCell(0));
					else if ("__Event".equals(iHead.getCell(0)))
						ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=events#event=" + event.getData().getCell(0));
					else if ("__Student".equals(iHead.getCell(0))) {
						EnrollmentTable et = new EnrollmentTable(false, iOnline);
						et.setAdvisorRecommendations(iSectioningProperties != null && iSectioningProperties.isAdvisorCourseRequests());
						et.setEmail(iSectioningProperties != null && iSectioningProperties.isEmail());
						et.showStudentSchedule(Long.valueOf(event.getData().getCell(0)));
					}
				}
			}
		});
		
		if (!online) {
			RPC.execute(new SolverPageMessagesRequest(SolverType.STUDENT), new AsyncCallback<SolverPageMessages>() {

				@Override
				public void onFailure(Throwable caught) {
				}

				@Override
				public void onSuccess(SolverPageMessages response) {
					RootPanel cpm = RootPanel.get("UniTimeGWT:CustomPageMessages");
					if (cpm != null) {
						cpm.clear();
						if (response.hasPageMessages()) {
							for (final PageMessage pm: response.getPageMessages()) {
								P p = new P(pm.getType() == PageMessageType.ERROR ? "unitime-PageError" : pm.getType() == PageMessageType.WARNING ? "unitime-PageWarn" : "unitime-PageMessage");
								p.setHTML(pm.getMessage());
								if (pm.hasUrl()) {
									p.addStyleName("unitime-ClickablePageMessage");
									p.addClickHandler(new ClickHandler() {
										@Override
										public void onClick(ClickEvent event) {
											if (pm.hasUrl()) ToolBox.open(GWT.getHostPageBaseURL() + pm.getUrl());
										}
									});
								}
								cpm.add(p);
							}
						}
					}
				}
			});
		}
		
		iSectioningService.getProperties(null, new AsyncCallback<SectioningProperties>() {
			@Override
			public void onSuccess(SectioningProperties result) {
				iSectioningProperties = result;
				iSectioningService.lookupStudentSectioningStates(new AsyncCallback<List<StudentStatusInfo>>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(List<StudentStatusInfo> result) {
						iStates = new TreeSet<StudentStatusInfo>(result);
						iStudentStatusDialog = new StudentStatusDialog(iStates, new StudentStatusDialog.StudentStatusConfirmation() {
							@Override
							public boolean isAllMyStudents() {
								return false;
							}
							@Override
							public int getStudentCount() {
								return iSelectedStudentIds.size();
							}
						});
					}
				});
			}
			@Override
			public void onFailure(Throwable caught) {}
		});
	}
	
	protected ReportTypeInterface getReportType(String reference) {
		if (iReportTypes == null || reference == null) return null;
		for (ReportTypeInterface type: iReportTypes)
			if (reference.equals(type.getReference())) return type;
		return null;
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
			iStudentId2Checks.clear();
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
			if (iSectioningProperties != null && "__Student".equals(iHead.getCell(0)) && iSectioningProperties.isCanSelectStudent()) {
				UniTimeTableHeader hSelect = new UniTimeTableHeader("&otimes;", HasHorizontalAlignment.ALIGN_CENTER);
				header.set(0, hSelect);
				hSelect.setWidth("10px");
				hSelect.addAdditionalStyleName("unitime-CheckBoxColumn");
				hSelect.addOperation(new Operation() {
					@Override
					public String getName() {
						return SCT_MSG.selectAll();
					}
					@Override
					public boolean hasSeparator() {
						return false;
					}
					@Override
					public boolean isApplicable() {
						return iSelectedStudentIds.size() != iTable.getRowCount() - 1;
					}
					@Override
					public void execute() {
						iSelectedStudentIds.clear();
						for (int row = 0; row < iTable.getRowCount(); row++) {
							Widget w = iTable.getWidget(row, 0);
							if (w instanceof CheckBox) {
								((CheckBox)w).setValue(true);
								iSelectedStudentIds.add(Long.valueOf(iTable.getData(row).getCell(0)));
							}
						}
					}
				});
				hSelect.addOperation(new Operation() {
					@Override
					public String getName() {
						return SCT_MSG.clearAll();
					}
					@Override
					public boolean hasSeparator() {
						return false;
					}
					@Override
					public boolean isApplicable() {
						return iSelectedStudentIds.size() > 0;
					}
					@Override
					public void execute() {
						iSelectedStudentIds.clear();
						for (int row = 0; row < iTable.getRowCount(); row++) {
							Widget w = iTable.getWidget(row, 0);
							if (w instanceof CheckBox) {
								((CheckBox)w).setValue(false);
							}
						}
					}
				});
				hSelect.addOperation(new Operation() {
					@Override
					public String getName() {
						return SCT_MSG.sendStudentEmail();
					}
					@Override
					public boolean hasSeparator() {
						return true;
					}
					@Override
					public boolean isApplicable() {
						return iSelectedStudentIds.size() > 0 && iStudentStatusDialog != null && iSectioningProperties.isEmail();
					}
					@Override
					public void execute() {
						if (!iOnline)
							iStudentStatusDialog.getClassScheduleCheckBox().setVisible(false);
						iStudentStatusDialog.sendStudentEmail(new Command() {
							@Override
							public void execute() {
								List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
								LoadingWidget.getInstance().show(MESSAGES.waitSendingEmail());
								sendEmail(studentIds.iterator(), iStudentStatusDialog.getSubject(), iStudentStatusDialog.getMessage(), iStudentStatusDialog.getCC(), 0,
										iStudentStatusDialog.getIncludeCourseRequests(), iOnline && iStudentStatusDialog.getIncludeClassSchedule(), iStudentStatusDialog.getIncludeAdvisorRequests(),
										iStudentStatusDialog.isOptionalEmailToggle());
							}
						}, iSectioningProperties.getEmailOptionalToggleCaption(), iSectioningProperties.getEmailOptionalToggleDefault());
					}
				});
				hSelect.addOperation(new Operation() {
					@Override
					public String getName() {
						return SCT_MSG.massCancel();
					}
					@Override
					public boolean hasSeparator() {
						return false;
					}
					@Override
					public boolean isApplicable() {
						return iOnline && iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isMassCancel() && iStudentStatusDialog != null;
					}
					@Override
					public void execute() {
						iStudentStatusDialog.massCancel(new Command() {
							@Override
							public void execute() {
								UniTimeConfirmationDialog.confirmFocusNo(SCT_MSG.massCancelConfirmation(), new Command() {
									@Override
									public void execute() {
										final List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
										LoadingWidget.getInstance().show(SCT_MSG.massCanceling());
										iSectioningService.massCancel(studentIds, iStudentStatusDialog.getStatus(),
												iStudentStatusDialog.getSubject(), iStudentStatusDialog.getMessage(), iStudentStatusDialog.getCC(), new AsyncCallback<Boolean>() {
											@Override
											public void onFailure(Throwable caught) {
												LoadingWidget.getInstance().hide();
												UniTimeNotifications.error(caught);
											}

											@Override
											public void onSuccess(Boolean result) {
												LoadingWidget.getInstance().hide();
											}
										});									
									}
								});
							}
						});
					}
				});
				hSelect.addOperation(new Operation() {
					@Override
					public String getName() {
						return SCT_MSG.reloadStudent();
					}
					@Override
					public boolean hasSeparator() {
						return true;
					}
					@Override
					public boolean isApplicable() {
						return iOnline && iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isReloadStudent();
					}
					@Override
					public void execute() {
						List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
						LoadingWidget.getInstance().show(SCT_MSG.reloadingStudent());
						iSectioningService.reloadStudent(studentIds, new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.error(caught);
							}

							@Override
							public void onSuccess(Boolean result) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.info(SCT_MSG.reloadStudentSuccess());
							}
						});
					}
				});
				hSelect.addOperation(new Operation() {
					@Override
					public String getName() {
						return SCT_MSG.requestStudentUpdate();
					}
					@Override
					public boolean hasSeparator() {
						return !iSectioningProperties.isReloadStudent();
					}
					@Override
					public boolean isApplicable() {
						return iOnline && iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isRequestUpdate();
					}
					@Override
					public void execute() {
						List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
						LoadingWidget.getInstance().show(SCT_MSG.requestingStudentUpdate());
						iSectioningService.requestStudentUpdate(studentIds, new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.error(caught);
							}

							@Override
							public void onSuccess(Boolean result) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.info(SCT_MSG.requestStudentUpdateSuccess());
							}
						});
					}
				});
				hSelect.addOperation(new Operation() {
					@Override
					public String getName() {
						return SCT_MSG.checkOverrideStatus();
					}
					@Override
					public boolean hasSeparator() {
						return !iSectioningProperties.isRequestUpdate() && !iSectioningProperties.isReloadStudent();
					}
					@Override
					public boolean isApplicable() {
						return iOnline && iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isCheckStudentOverrides();
					}
					@Override
					public void execute() {
						List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
						LoadingWidget.getInstance().show(SCT_MSG.checkingOverrideStatus());
						iSectioningService.checkStudentOverrides(studentIds, new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.error(caught);
							}

							@Override
							public void onSuccess(Boolean result) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.info(SCT_MSG.checkStudentOverridesSuccess());
							}
						});
					}
				});
				hSelect.addOperation(new Operation() {
					@Override
					public String getName() {
						return SCT_MSG.validateStudentOverrides();
					}
					@Override
					public boolean hasSeparator() {
						return !iSectioningProperties.isRequestUpdate() && !iSectioningProperties.isCheckStudentOverrides() && !iSectioningProperties.isReloadStudent();
					}
					@Override
					public boolean isApplicable() {
						return iOnline && iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isValidateStudentOverrides();
					}
					@Override
					public void execute() {
						List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
						LoadingWidget.getInstance().show(SCT_MSG.validatingStudentOverrides());
						iSectioningService.validateStudentOverrides(studentIds, new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.error(caught);
							}

							@Override
							public void onSuccess(Boolean result) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.info(SCT_MSG.validateStudentOverridesSuccess());
							}
						});
					}
				});
				hSelect.addOperation(new Operation() {
					@Override
					public String getName() {
						return SCT_MSG.validateReCheckCriticalCourses();
					}
					@Override
					public boolean hasSeparator() {
						return !iSectioningProperties.isRequestUpdate() && !iSectioningProperties.isCheckStudentOverrides() && !iSectioningProperties.isValidateStudentOverrides() && !iSectioningProperties.isReloadStudent();
					}
					@Override
					public boolean isApplicable() {
						return iOnline && iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isRecheckCriticalCourses();
					}
					@Override
					public void execute() {
						List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
						LoadingWidget.getInstance().show(SCT_MSG.recheckingCriticalCourses());
						iSectioningService.recheckCriticalCourses(studentIds, new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.error(caught);
							}

							@Override
							public void onSuccess(Boolean result) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.info(SCT_MSG.recheckCriticalCoursesSuccess());
							}
						});
					}
				});
				hSelect.addOperation(new Operation() {
					@Override
					public String getName() {
						return SCT_MSG.setStudentStatus();
					}
					@Override
					public boolean hasSeparator() {
						return true;
					}
					@Override
					public boolean isApplicable() {
						return iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isChangeStatus() && iStudentStatusDialog != null;
					}
					@Override
					public void execute() {
						iStudentStatusDialog.setStatus(new Command() {
							@Override
							public void execute() {
								final String statusRef = iStudentStatusDialog.getStatus();
								if ("-".equals(statusRef)) return;
								List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
								LoadingWidget.getInstance().show(SCT_MSG.changingStatusTo(statusRef));
								iSectioningService.changeStatus(studentIds, null, statusRef, new AsyncCallback<Boolean>() {

									@Override
									public void onFailure(Throwable caught) {
										LoadingWidget.getInstance().hide();
										UniTimeNotifications.error(caught);
									}

									@Override
									public void onSuccess(Boolean result) {
										LoadingWidget.getInstance().hide();
									}
								});
								
							}
						});
					}
				});
				hSelect.addOperation(new Operation() {
					@Override
					public String getName() {
						return SCT_MSG.setStudentNote();
					}
					@Override
					public boolean hasSeparator() {
						return false;
					}
					@Override
					public boolean isApplicable() {
						return iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isChangeStatus() && iStudentStatusDialog != null;
					}
					@Override
					public void execute() {
						iStudentStatusDialog.setStudentNote(new Command() {
							@Override
							public void execute() {
								LoadingWidget.getInstance().show(SCT_MSG.changingStudentNote());
								List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
								final String statusRef = iStudentStatusDialog.getStatus();
								final String note = iStudentStatusDialog.getNote();
								iSectioningService.changeStatus(studentIds, note, statusRef, new AsyncCallback<Boolean>() {
									@Override
									public void onFailure(Throwable caught) {
										LoadingWidget.getInstance().hide();
										UniTimeNotifications.error(caught);
									}

									@Override
									public void onSuccess(Boolean result) {
										LoadingWidget.getInstance().hide();
									}
								});
							}
						});
					}
				});
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
					String text = "";
					boolean number = true;
					int idx = 0;
					for (String t: row.getCell(x).split("\\n")) {
						boolean n = false;
						try {
							Double.parseDouble(t);
							n = true;
						} catch (Exception e) {}
						if (iHead.getCell(x).contains("%") && n)
							t = PF.format(Double.parseDouble(t));
						else if (t.matches("[\\-]?[0-9]+\\.[0-9]+(<br>[\\-]?[0-9]+\\.[0-9]+)*") && n)
							t = DF.format(Double.parseDouble(t));
						else if (t.matches("[\\-]?[0-9]+(,[0-9][0-9][0-9])*(\\.[0-9]+)? ?%?"))
							n = true;
						if (!n) number = false;
						if (idx > 0) text += "<br>";
						text += t;
						idx ++;
					}
					line.add(number ? new NumberCell(hide ? "" : text) : new HTML(hide ? "" : text));
					prevHide = hide;
				}
				if (iSectioningProperties != null && "__Student".equals(iHead.getCell(0)) && iSectioningProperties.isCanSelectStudent()) {
					if (prev == null || !prev.getCell(0).equals(row.getCell(0))) {
						Toggle ch = new Toggle();
						ch.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								event.stopPropagation();
							}
						});
						ch.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								event.stopPropagation();
							}
						});
						final Long sid = Long.valueOf(row.getCell(0));
						List<CheckBox> toggles = iStudentId2Checks.get(sid);
						if (toggles == null) {
							toggles = new ArrayList<CheckBox>();
							iStudentId2Checks.put(sid, toggles);
						}
						toggles.add(ch);
						ch.setValue(iSelectedStudentIds.contains(sid));
						ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								if (event.getValue())
									iSelectedStudentIds.add(sid);
								else
									iSelectedStudentIds.remove(sid);
								if (iStudentId2Checks.get(sid).size() > 1)
									for (CheckBox ch: iStudentId2Checks.get(sid))
										ch.setValue(event.getValue());
							}
						});
						line.set(0, ch);
					} else {
						line.set(0, new HTML());
					}
				}
				int last = iTable.addRow(row, line);
				if (prev != null && !prev.getCell(0).equals(row.getCell(0)))
					for (int c = 0; c < iTable.getCellCount(last); c++)
						iTable.getCellFormatter().addStyleName(last, c, "unitime-TopLineDash");
				prev = row;
			}
			
			iTable.setColumnVisible(0, !iHead.getCell(0).startsWith("__") || (iSectioningProperties != null && "__Student".equals(iHead.getCell(0)) && iSectioningProperties.isCanSelectStudent()));
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
		ReportTypeInterface type = getReportType(params[0]);
		if (type == null) return;
		for (int i = 1; i < iReportSelector.getWidget().getItemCount(); i++) {
			if (type.getReference().equals(iReportSelector.getWidget().getValue(i))) {
				iReportSelector.getWidget().setSelectedIndex(i); break;
			}
		}
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
		
		ReportTypeInterface type = getReportType(iReportSelector.getWidget().getValue(iReportSelector.getWidget().getSelectedIndex()));
		iLastHistory = type.getReference();
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
				iTableHeader.setErrorMessage(caught.getMessage());
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
	
	public static class ReportTypeInterface implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		String iReference, iName, iImplementation;
		String[] iParameters;
		
		public ReportTypeInterface() {}
		public ReportTypeInterface(String reference, String name, String implementation, String... params) {
			iReference = reference; iName = name; iImplementation = implementation; iParameters = params;
		}
		
		public void setReference(String reference) { iReference = reference; }
		public String getReference() { return iReference; }
		public void setName(String name) { iName = name; }
		public String getName() { return iName; }
		public void setImplementation(String implementation) { iImplementation = implementation; }
		public String getImplementation() { return iImplementation; }
		public void setParameters(String... params) { iParameters = params; }
		public String[] getParameters() { return iParameters; }
		
		@Override
		public int hashCode() { return getReference().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ReportTypeInterface)) return false;
			return getReference().equals(((ReportTypeInterface)o).getReference());
		}
	}
	
	public static class SectioningReportTypesRpcRequest implements GwtRpcRequest<GwtRpcResponseList<ReportTypeInterface>> {
		private boolean iOnline = false;
		
		public SectioningReportTypesRpcRequest() {}
		public SectioningReportTypesRpcRequest(boolean online) { iOnline = online; }
		
		public void setOnline(boolean online) { iOnline = online; }
		public boolean isOnline() { return iOnline; }
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
	
	public static class RowData{
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
			if (a.getLevel() != b.getLevel()) {
				RowData aP = a, bP = b;
				while (aP.getLevel() > bP.getLevel()) aP = aP.getParent();
				while (bP.getLevel() > aP.getLevel()) bP = bP.getParent();
				int cmp = aP.compareTo(bP, col);
				if (cmp != 0) return cmp;
			} else if (a.getParent() != null) {
				int cmp = a.getParent().compareTo(b.getParent(), col);
				if (cmp != 0) return cmp;
			}
			try {
				int cmp = Double.valueOf(a.getCell(col) == null ? "0" : a.getCell(col)).compareTo(Double.valueOf(b.getCell(col) == null ? "0" : b.getCell(col)));
				if (cmp != 0) return cmp;
			} catch (NumberFormatException e) {
				int cmp = (a.getCell(col) == null ? "" : a.getCell(col)).compareTo(b.getCell(col) == null ? "" : b.getCell(col));
				if (cmp != 0) return cmp;
			}
			for (int c = 0; c < getLength(); c++) {
				try {
					int cmp = Double.valueOf(a.getCell(c) == null ? "0" : a.getCell(c)).compareTo(Double.valueOf(b.getCell(c) == null ? "0" : b.getCell(c)));
					if (cmp != 0) return cmp;
				} catch (NumberFormatException e) {
					int cmp = (a.getCell(c) == null ? "" : a.getCell(c)).compareTo(b.getCell(c) == null ? "" : b.getCell(c));
					if (cmp != 0) return cmp;
				}
			}
			return 0;
		}
		
	}
	
	private void sendEmail(final Iterator<Long> studentIds, final String subject, final String message, final String cc, final int fails, final boolean courseRequests, final boolean classSchedule, final boolean advisorRequests, final Boolean toggle) {
		if (!studentIds.hasNext()) {
			LoadingWidget.getInstance().hide();
			if (fails == 0) UniTimeNotifications.info(MESSAGES.emailSent());
			return;
		}
		final Long studentId = studentIds.next();
		iSectioningService.sendEmail(null, studentId, subject, message, cc, courseRequests, classSchedule, advisorRequests, toggle, new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				iTableHeader.setErrorMessage(MESSAGES.failedEmail(caught.getMessage()));
				sendEmail(studentIds, subject, message, cc, fails + 1, courseRequests, classSchedule, advisorRequests, toggle);
			}
			@Override
			public void onSuccess(Boolean result) {
				sendEmail(studentIds, subject, message, cc, fails, courseRequests, classSchedule, advisorRequests, toggle);
			}
		});
	}
	
	private static class Toggle extends CheckBox implements HasStyleName {
		@Override
		public String getStyleName() {
			return "unitime-CheckBoxColumn";
		}
	}
}