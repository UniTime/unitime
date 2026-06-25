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
package org.unitime.timetable.gwt.client.exams;

import java.util.ArrayList;
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.exams.ExamAssignmentInterface.DomainItem;
import org.unitime.timetable.gwt.client.exams.ExamAssignmentInterface.ExamAssignmentRequest;
import org.unitime.timetable.gwt.client.exams.ExamAssignmentInterface.ExamAssignmentResponse;
import org.unitime.timetable.gwt.client.exams.ExamAssignmentInterface.Operation;
import org.unitime.timetable.gwt.client.exams.ExamAssignmentInterface.RoomOrder;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamConflictBasedStatisticsRequest;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.rooms.RoomFilterBox;
import org.unitime.timetable.gwt.client.solver.suggestions.ConflictBasedStatisticsTree;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessageType;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.CBSNode;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.impl.FocusImpl;

public class ExamAssignmentPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final ExaminationMessages MSG = GWT.create(ExaminationMessages.class);
	private static final CourseMessages CMSG = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter, iSuggestions, iCBS;
	private P iSuggestionsMessage = new P("unitime-Message");
	private TableWidget iSuggestionsTable = new TableWidget();
	private ExamAssignmentRequest iRequest;
	private CheckBox iAllowRoomConflicts = new CheckBox();
	private ListBox iRoomOrder = new ListBox();
	private RoomFilterBox iRoomFilter = null;
	private Long iLastExamId = null;
	private SimpleForm iSuggestionsForm = null;
	private ConflictBasedStatisticsTree iCBSTree;
	private Long iLastCbsId = null;
	
	public ExamAssignmentPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ExamAssignmentPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		if (hasParent())
			iHeader.addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent e) {
					closeDialog();
				}
			});
		else
			iHeader.addButton("close", MESSAGES.buttonBack(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent e) {
					ToolBox.open(GWT.getHostPageBaseURL() + "examination?id=" + iRequest.getSelectedExamId());
				}
			});
		
		iFooter = iHeader.clonePanel();
		
		iRoomOrder.addItem(CMSG.sortRoomNameAsc(), RoomOrder.NAME_ASC.name());
		iRoomOrder.addItem(CMSG.sortRoomNameDesc(), RoomOrder.NAME_DESC.name());
		iRoomOrder.addItem(CMSG.sortRoomSizeAsc(), RoomOrder.SIZE_ASC.name());
		iRoomOrder.addItem(CMSG.sortRoomSizeDesc(), RoomOrder.SIZE_DESC.name());
		iRoomOrder.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				iRequest.setRoomOrder(RoomOrder.valueOf(iRoomOrder.getSelectedValue()));
				ToolBox.setSessionCookie("ExamAssignment.RoomOrder", iRoomOrder.getSelectedValue());
			}
		});
		String selectedRoomOrder = ToolBox.getSessionCookie("ExamAssignment.RoomOrder");
		if (selectedRoomOrder == null || selectedRoomOrder.isEmpty())
			selectedRoomOrder = RoomOrder.NAME_ASC.name();
		for (int i = 0; i < iRoomOrder.getItemCount(); i++)
			if (iRoomOrder.getValue(i).equals(selectedRoomOrder)) {
				iRoomOrder.setSelectedIndex(i); break;
			}
		if ("1".equals(ToolBox.getSessionCookie("ExamAssignment.AllowConflicts")))
			iAllowRoomConflicts.setValue(true);
		iAllowRoomConflicts.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> e) {
				ToolBox.setSessionCookie("ExamAssignment.AllowConflicts", e.getValue() ? "1" : "0");
			}
		});
		
		String id = Window.Location.getParameter("id");
		if (id == null)
			id = Window.Location.getParameter("examId");
		if (id == null || id.isEmpty()) {	
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(MSG.errorNoExamId());
		} else {
			iRequest = new ExamAssignmentRequest();
			iRequest.setSelectedExamId(Long.valueOf(id));
			iRequest.setRoomOrder(RoomOrder.valueOf(iRoomOrder.getSelectedValue()));
			iRequest.setRoomAllowConflicts(iAllowRoomConflicts.getValue());
			String period = Window.Location.getParameter("period");
			if (period != null && !period.isEmpty())
				iRequest.getChange(iRequest.getSelectedExamId()).setPeriod(period);
			String room = Window.Location.getParameter("room");
			if (room != null && !room.isEmpty())
				iRequest.getChange(iRequest.getSelectedExamId()).setRoom(room);
			load(Operation.INIT);
		}
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				Operation op = Operation.UPDATE;
				for (String token: event.getValue().split("&")) {
					if (token.indexOf('=') >= 0) {
						String key = token.substring(0, token.indexOf('='));
						String val = token.substring(token.indexOf('=') + 1);
						if ("period".equals(key)) {
							iRequest.getChange(iRequest.getSelectedExamId()).setPeriod(val);
							iRequest.getChange(iRequest.getSelectedExamId()).setRoom(null);
						} else if ("room".equals(key)) {
							iRequest.getChange(iRequest.getSelectedExamId()).setRoom(val);
						} else if ("exam".equals(key) || "id".equals(key)) {
							iRequest.setSelectedExamId(Long.valueOf(val));
							iSuggestionsTable.setVisible(false);
							iSuggestionsMessage.setVisible(false);
						} else if ("delete".equals(key)) {
							iRequest.removeChange(Long.valueOf(val));
							if (!iRequest.hasChange(iRequest.getSelectedExamId()) && iRequest.hasChanges())
								iRequest.setSelectedExamId(iRequest.getChanges().get(0).getExamId());
							iSuggestionsTable.setVisible(false);
							iSuggestionsMessage.setVisible(false);
						} else if ("suggestion".equals(key)) {
							iRequest.clearChanges();
							for (String change: val.split(";")) {
								String[] parms = change.split(",");
								iRequest.addChange(Long.valueOf(parms[0]), parms[1], parms[2]);
							}
						}
					}
				}
				load(op);
			}
		});
		
		iSuggestionsForm = new SimpleForm();
		iSuggestionsForm.removeStyleName("unitime-NotPrintableBottomLine");
		iSuggestions = new UniTimeHeaderPanel(MSG.sectSuggestions());
		iSuggestionsForm.clear();
		TextBox filter = new TextBox();
		filter.setWidth("560px");
		iSuggestionsForm.addRow(MSG.filterTextFilter(), filter);
		filter.setText(iRequest.getSuggestionFilter() == null ? "" : iRequest.getSuggestionFilter());
		filter.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iRequest.setSuggestionFilter(event.getValue() == null || event.getValue().isEmpty() ? null : event.getValue());
			}
		});
		final NumberBox max = new NumberBox(); max.setDecimal(false); max.setNegative(false);
		iSuggestionsForm.addRow(MSG.filterMaxNumberOfSuggestions(), max);
		max.setValue(iRequest.getSuggestionMax());
		filter.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iRequest.setSuggestionMax(max.toInteger() == null ? 30 : max.toInteger());
			}
		});
		iSuggestionsForm.addRow(iSuggestionsTable);
		iSuggestionsForm.addRow(iSuggestionsMessage);
		iSuggestions.addButton("apply", MSG.buttonSearch(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iRequest.setSuggestionDepth(2);
				iRequest.setSuggestionTimeOut(5);
				loadSuggestions();
			}
		});
		iSuggestions.addButton("longer", MSG.buttonSearchLonger(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iRequest.setSuggestionTimeOut(2 * iRequest.getSuggestionTimeOut());
				loadSuggestions();
			}
		});
		iSuggestions.setEnabled("longer", false);
		iSuggestions.addButton("deeper", MSG.buttonSearchDeeper(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iRequest.setSuggestionDepth(1 + iRequest.getSuggestionDepth());
				loadSuggestions();
			}
		});
		iSuggestions.setEnabled("deeper", false);
		
		iCBS = new UniTimeHeaderPanel(MSG.sectConflictBasedStatistics());
		iCBS.setCollapsible(false);
		iCBS.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iCBSTree.setVisible(event.getValue());
				refreshCBS();
			}
		});
		
		checkParent();
	}
	
	protected void loadSuggestions() {
		iSuggestionsMessage.setVisible(false);
		iSuggestionsTable.setVisible(false);
		iSuggestions.clearMessage();
		iSuggestions.showLoading();
		iSuggestions.setEnabled("apply", false);
		iSuggestions.setEnabled("deeper", false);
		iSuggestions.setEnabled("longer", false);
		iRequest.setOperation(Operation.SUGGESTIONS);
		RPC.execute(iRequest, new AsyncCallback<ExamAssignmentResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iSuggestions.clearMessage();
				iSuggestions.setErrorMessage(MESSAGES.failedToComputeSuggestions(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToComputeSuggestions(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final ExamAssignmentResponse response) {
				iSuggestions.clearMessage();
				if (response.hasSuggestionsMessage()) {
					iSuggestionsMessage.setText(response.getSuggestionsMessage());
					iSuggestionsMessage.setVisible(true);
				} else {
					iSuggestionsMessage.setVisible(false);
				}
				if (response.hasSuggestions()) {
					iSuggestionsTable.setData(response.getSuggestions());
					iSuggestionsTable.setVisible(true);
				} else {
					iSuggestionsTable.setVisible(false);
				}
				iSuggestions.setEnabled("apply", true);
				iSuggestions.setEnabled("deeper", true);
				iSuggestions.setEnabled("longer", response.isSuggestionsTimeoutReached());
			}
		});
	}
	
	protected void load(final Operation op) {
		GwtHint.hideHint();
		final Timer timer = new Timer() {
			@Override
			public void run() {
				LoadingWidget.getInstance().show(op == Operation.ASSIGN ? MESSAGES.waitSavingData() : MESSAGES.waitLoadingData());
			}
		};
		timer.schedule(200);
		iRequest.setOperation(op);
		iRequest.setPreviousExamId(iLastExamId);
		iRequest.setRoomAllowConflicts(iAllowRoomConflicts.getValue());
		if (iRoomFilter != null)
			iRequest.setRoomFilter(iRoomFilter.getElementsRequest());
		else {
			String rf = ToolBox.getSessionCookie("ExamAssignment.RoomFilter");
			if (rf != null && !rf.isEmpty()) {
				RoomFilterRpcRequest filter = new RoomFilterRpcRequest();
				filter.setText(rf);
				iRequest.setRoomFilter(filter);
			}
		}
		RPC.execute(iRequest, new AsyncCallback<ExamAssignmentResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				timer.cancel();
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final ExamAssignmentResponse response) {
				if (response.hasPageMessages()) {
					RootPanel cpm = RootPanel.get("UniTimeGWT:CustomPageMessages");
					if (cpm != null) {
						cpm.clear();
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
				if (response.getSuggestionProperties() != null && response.getSuggestionProperties().isSolver()) {
					iCBSTree = new ConflictBasedStatisticsTree(response.getSuggestionProperties()) {
						protected void onClick(ClickEvent event, CBSNode node) {
							if (node.hasLink()) {
								if (hasParent()) {
									closeDialog();
									openParent(GWT.getHostPageBaseURL() + node.getLink());
								} else {
									ToolBox.open(GWT.getHostPageBaseURL() + node.getLink());
								}
							} else if (node.hasClassId()) {
								iRequest.setSelectedExamId(node.getClassId());
								iSuggestionsTable.setVisible(false);
								iSuggestionsMessage.setVisible(false);
								load(Operation.UPDATE);
							} else if (node.hasSelection()) {
								iRequest.clearChanges();
								SelectedAssignment selection = node.getSelection();
								iRequest.getChange(selection.getClassId()).setPeriod("" + selection.getPatternId());
								iRequest.getChange(selection.getClassId()).setRoom(selection.getRoomIds(":"));
								iRequest.setSelectedExamId(selection.getClassId());
								load(Operation.UPDATE);
							}
						}	
					};
					iCBSTree.setVisible(false);
				}
				
				if (response.hasUrl()) {
					if (hasParent()) {
						closeAndRefresh();
					} else {
						ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					}
					return;
				}
				timer.cancel();
				if (History.getToken() != null && !History.getToken().isEmpty())
					History.newItem("", false);
				LoadingWidget.getInstance().hide();
				iPanel.clear();
				iPanel.addHeaderRow(iHeader);
				
				iHeader.getHeaderTitlePanel().clear();
				Anchor anchor = new Anchor(MSG.sectExamination(response.getExamName()));
				anchor.setHref("examination?id=" + response.getSelectedExamId());
				anchor.setTitle(MSG.hintOpenExaminationDetail(response.getExamName()));
				anchor.setStyleName("l8");
				anchor.setTarget("_blank");
				iHeader.getHeaderTitlePanel().add(anchor);
				iHeader.clearMessage();
				if (response.hasErrorMessage())
					iHeader.setErrorMessage(response.getErrorMessage());

				if (response.hasProperties())
					for (PropertyInterface property: response.getProperties().getProperties())
						iPanel.addRow(property.getName(), new TableWidget.CellWidget(property.getCell(), true));
				
				if (response.hasAssignments()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getAssignments().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getAssignments()));
					if (response.isCanAssign()) {
						hp.addButton("assign", MSG.actionExamAssign(), new ClickHandler() {
							@Override
							public void onClick(ClickEvent e) {
								UniTimeConfirmationDialog.confirm(response.getAssignConfirmation(), new Command() {
									@Override
									public void execute() {
										load(Operation.ASSIGN);
									}
								});
							}
						});
					}
				}
				
				if (response.hasDistributionConflicts()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getDistributionConflicts().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getDistributionConflicts()));
				}

				if (response.hasStudentConflicts()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getStudentConflicts().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getStudentConflicts()));
				}
				
				if (response.hasInstructorConflicts()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getInstructorConflicts().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getInstructorConflicts()));
				}
				
				if (response.hasPeriods() || response.hasPeriodsErrorMessage()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(MSG.sectAvailablePeriodsForExam(response.getExamName()));
					iPanel.addHeaderRow(hp);
					if (response.hasPeriods()) {
						TableWidget periods = new TableWidget(response.getPeriods());
						periods.addStyleName("available-periods");
						iPanel.addRow(periods);
						for (LineInterface line: response.getPeriods().getLines()) {
							if ("unitime-TableRowSelected".equals(line.getClassName())) {
								iRequest.getChange(response.getSelectedExamId()).setPeriod(line.getId() == null ? "null" : line.getId().toString());
								break;
							}
						}
					}
					if (response.hasPeriodsErrorMessage()) {
						P error = new P("unitime-ErrorMessage");
						error.setText(response.getPeriodsErrorMessage());
						int r = iPanel.addRow(error);
						iPanel.getRowFormatter().getElement(r).getStyle().setTextAlign(TextAlign.CENTER);
					}
				}

				if (response.hasRooms() || response.hasRoomsErrorMessage()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(MSG.sectAvailableRoomsForExam(response.getExamName()));
					iPanel.addHeaderRow(hp);
					HTML selected = new HTML();
					selected.addStyleName("selected-total");
					hp.getHeaderTitlePanel().add(selected);
					if (iRoomFilter == null) {
						AcademicSessionProvider session = new AcademicSessionProvider() {
							@Override
							public Long getAcademicSessionId() {
								return response.getSessionId();
							}
							@Override
							public String getAcademicSessionName() {
								return "Current Session";
							}
							@Override
							public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {
							}
							@Override
							public void selectSession(Long sessionId, AsyncCallback<Boolean> callback) {
							}
							@Override
							public AcademicSessionInfo getAcademicSessionInfo() {
								return null;
							}
						};
						iRoomFilter = new RoomFilterBox(session);
						String f = ToolBox.getSessionCookie("ExamAssignment.RoomFilter");
						if (f != null && !f.isEmpty())
							iRoomFilter.setValue(f);
						iRoomFilter.addChip(new Chip("department", response.getExamType()), false);
						if (response.getMinRoomCapacity() != null && (response.getMaxRooms() <= 1 || response.getMinRoomCapacity() < 66))
							iRoomFilter.addChip(new Chip("size", ">=" + response.getMinRoomCapacity()), false);
						else if (response.getMinRoomCapacity() != null && (response.getMaxRooms() > 1))
							iRoomFilter.addChip(new Chip("size", ">=" + (response.getMinRoomCapacity() / response.getMaxRooms())), false);
						iRoomFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
							@Override
							public void onValueChange(ValueChangeEvent<String> e) {
								String ret = "";
								for (Chip chip: iRoomFilter.getChips(null))
									if (!"department".equals(chip.getCommand()) && !"size".equals(chip.getCommand()))
										ret += chip.toString() + " ";
								ret += iRoomFilter.getText();
								ToolBox.setSessionCookie("ExamAssignment.RoomFilter", ret);
							}
						});
					} else {
						if (iLastExamId != null && !iLastExamId.equals(response.getSelectedExamId())) {
							Chip dept = iRoomFilter.getChip("department");
							if (dept != null && !dept.getValue().equals(response.getExamType())) {
								iRoomFilter.removeChip(dept, false);
								iRoomFilter.addChip(new Chip("department", response.getExamType()), false);
							}
							Chip size = iRoomFilter.getChip("size");
							if (size != null) iRoomFilter.removeChip(size, false);
							if (response.getMinRoomCapacity() != null && (response.getMaxRooms() <= 1 || response.getMinRoomCapacity() < 66))
								iRoomFilter.addChip(new Chip("size", ">=" + response.getMinRoomCapacity()), false);
							else if (response.getMinRoomCapacity() != null && (response.getMaxRooms() > 1))
								iRoomFilter.addChip(new Chip("size", ">=" + (response.getMinRoomCapacity() / response.getMaxRooms())), false);

						}
					}
					iPanel.addRow(CMSG.properyRoomFilter(), iRoomFilter);
					hp.addButton("apply", MSG.buttonApply(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent e) {
							load(Operation.UPDATE);
						}
					});
					iPanel.addRow(MSG.filterAllowForConflicts(), iAllowRoomConflicts);
					iPanel.addRow(MSG.filterRoomOrder(), iRoomOrder);
					if (response.hasRooms()) {
						Items rooms = new Items(response.getRooms(), response.getMaxRooms(), "room", selected, response.getMinRoomCapacity());
						iPanel.addRow(rooms);
						if (rooms.hasSelection(response.getMaxRooms(), response.getMinRoomCapacity()))
							iRequest.getChange(response.getSelectedExamId()).setRoom(rooms.getSelection());
					}
					if (response.hasRoomsErrorMessage()) {
						P error = new P("unitime-ErrorMessage");
						error.setText(response.getRoomsErrorMessage());
						int r = iPanel.addRow(error);
						iPanel.getRowFormatter().getElement(r).getStyle().setTextAlign(TextAlign.CENTER);
					}
				}
				
				if (response.isCanShowSuggestions()) {
					iPanel.addHeaderRow(iSuggestions);
					iPanel.addRow(iSuggestionsForm);
					iSuggestions.setEnabled("apply", true);
					iSuggestions.setEnabled("deeper", false);
					iSuggestions.setEnabled("longer", false);
				}
				
				if (response.isCanShowSuggestions() && iCBSTree != null) {
					iPanel.addHeaderRow(iCBS);
					iPanel.addRow(iCBSTree);
					refreshCBS();
				}

				iLastExamId = response.getSelectedExamId();				
				iPanel.addBottomRow(iFooter);
				if (hasParent()) centerDialog();
			}
		});
	}
	
	protected void refreshCBS() {
		if (iCBSTree.isVisible()) {
			if (!iRequest.getSelectedExamId().equals(iLastCbsId)) {
				iCBSTree.setValue(null);
				iLastCbsId = iRequest.getSelectedExamId();
				final ExamConflictBasedStatisticsRequest cbsrq = new ExamConflictBasedStatisticsRequest();
				cbsrq.setVariableOriented(true);
				cbsrq.setClassId(iRequest.getSelectedExamId());
				iCBS.showLoading();
				RPC.execute(cbsrq, new AsyncCallback<GwtRpcResponseList<CBSNode>>() {
					@Override
					public void onFailure(Throwable caught) {
						iCBS.setErrorMessage(MESSAGES.failedToLoadConflictStatistics(caught.getMessage()));
						iCBSTree.setValue(null);
					}
					@Override
					public void onSuccess(GwtRpcResponseList<CBSNode> result) {
						iCBS.clearMessage();
						if (result == null || result.isEmpty()) {
							iCBS.setMessage(MESSAGES.errorConflictStatisticsNoDataReturned());
							iCBSTree.setValue(null);
						} else {
							iCBSTree.setValue(result);
						}
					}
				});				
			}
		}
	}
	
	class Items extends P {
		int iCount;
		String iKey;
		List<Item> iSelection = new ArrayList<Item>();
		HTML iTotal;
		int iDesired;
		
		Items(List<DomainItem> items, int count, String key) {
			this(items, count, key, null, -1);
		}
		
		Items(List<DomainItem> items, int count, String key, HTML total, int desired) {
			iKey = key; iCount = count;
			iTotal = total; iDesired = desired;
			setStyleName("domain-items");
			Item prev = null;
			for (DomainItem item: items) {
				final Item w = new Item(item);
				if (prev != null) {
					prev.setNext(w); w.setPrevious(prev);
				}
				prev = w;
				add(w);
				if (w.isSelected()) {
					iSelection.add(w); 
				}
				w.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent e) {
						// if selected >> deselect
						if (w.isSelected()) {
							w.setSelected(false);
							iSelection.remove(w);
							updateTotal();
							return;
						}
						
						// full selection already >> clear current selection
						if (iSelection.size() >= iCount || getTotal() >= iDesired) {
							for (Item x: iSelection)
								x.setSelected(false);
							iSelection.clear();
						}
						
						// add to selection
						w.setSelected(true);
						iSelection.add(w);
						updateTotal();
						
						// full selection >> update
						if (hasSelection(iCount, iDesired)) {
							if ("period".equals(key)) {
								iRequest.getChange(iRequest.getSelectedExamId()).setPeriod(getSelection());
								iRequest.getChange(iRequest.getSelectedExamId()).setRoom(null);
								if ("null".equals(getSelection()))
									iRequest.getChange(iRequest.getSelectedExamId()).setPeriod(null);
							}
							if ("room".equals(key)) {
								iRequest.getChange(iRequest.getSelectedExamId()).setRoom(getSelection());
							}
							load(Operation.UPDATE);
						}	
					}
				});
			}
			updateTotal();
		}
		
		public String getSelection() {
			String id = null;
			for (Item x: iSelection)
				if (id == null)
					id = x.getValue().getId();
				else
					id += ":" + x.getValue().getId();
			return id;
		}
		
		public boolean hasSelection(int count, int desired) {
			return iSelection.size() >= 1 && iSelection.size() <= count && getTotal() >= desired;
		}
		
		public boolean hasSelection() {
			return !iSelection.isEmpty();
		}
		
		public int getTotal() {
			int total = 0;
			for (Item s: iSelection)
				total += s.getValue().getValue();
			return total;
		}
		
		void updateTotal() {
			if (iTotal == null) return;
			if (iSelection.isEmpty()) {
				iTotal.setText("");
				return;
			}
			int total = 0;
			for (Item s: iSelection)
				total += s.getValue().getValue();
			if (total < iDesired)
				iTotal.setHTML("(" + MSG.hintSelectedSize() + " <span style='color:#ec0000;'>" + total + "</span> " + MSG.hintRoomSizeOfNbrStudents() + " " + iDesired + ")");
			else
				iTotal.setText("(" + MSG.hintSelectedSize() + " " + total + " " + MSG.hintRoomSizeOfNbrStudents() + " " + iDesired + ")");
		}
	}

	static class Item extends P implements Focusable {
		DomainItem iItem;
		Item iPrevious, iNext;
		
		Item(DomainItem item) {
			iItem = item;
			setStyleName("domain-item");
			P first = new P("domain-item-label");
			first.add(new TableWidget.CellWidget(item.getCell()));
			add(first);
			if (iItem.isAssigned())
				first.getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
			if (item.getExtra() != null) {
				P extra = new P("domain-item-extra");
				extra.add(new TableWidget.CellWidget(item.getExtra()));
				add(extra);
				if (iItem.isAssigned())
					extra.getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
			}
			if (iItem.isSelected())
				getElement().getStyle().setBackgroundColor("#b7d4fb");
			addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent e) {
					getElement().getStyle().setBackgroundColor("#d0e4f6");
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent e) {
					if (iItem.isSelected())
						getElement().getStyle().setBackgroundColor("#b7d4fb");
					else
						getElement().getStyle().clearBackgroundColor();
				}
			});
			getElement().setTabIndex(0);
			sinkEvents(Event.ONKEYDOWN);
		}
		
		void setPrevious(Item prev) { iPrevious = prev; }
		void setNext(Item next) { iNext = next; }
		
		public DomainItem getValue() { return iItem; }
		
		public boolean isSelected() { return iItem.isSelected(); }
		public void setSelected(boolean selected) {
			iItem.setSelected(selected);
			if (!"#d0e4f6".equals(getElement().getStyle().getBackgroundColor())) {
				if (iItem.isSelected())
					getElement().getStyle().setBackgroundColor("#b7d4fb");
				else
					getElement().getStyle().clearBackgroundColor();
			}
		}
		
		private Item next(int i, boolean rotate) {
			Item next = iNext;
			for (int k = 0; k < i - 1; k++)
				if (next != null) next = next.iNext;
			if (next == null && rotate) {
				for (Item adept = prev(i, false); adept != null; adept = adept.prev(i, false))
					next = adept;
			}
			return next;
		}
		
		private Item prev(int i, boolean rotate) {
			Item prev = iPrevious;
			for (int k = 0; k < i - 1; k++)
				if (prev != null) prev = prev.iPrevious;
			if (prev == null && rotate) {
				for (Item adept = next(i, false); adept != null; adept = adept.next(i, false))
					prev = adept;
			}
			return prev;
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
			case Event.ONKEYDOWN:
				if (event.getKeyCode() == KeyCodes.KEY_ENTER || event.getKeyCode() == KeyCodes.KEY_SPACE) {
					clickElement(getElement());
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_LEFT) {
					Item next = prev(1, true);
					if (next != null) next.setFocus(true);
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_RIGHT) {
					Item next = next(1, true);
					if (next != null) next.setFocus(true);
					event.stopPropagation();
			    	event.preventDefault();
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP) {
					Item next = prev(4, true);
					if (next != null) next.setFocus(true);
					event.stopPropagation();
			    	event.preventDefault();
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN) {
					Item next = next(4, true);
					if (next != null) next.setFocus(true);
					event.stopPropagation();
			    	event.preventDefault();
				}
				break;
			}
			super.onBrowserEvent(event);
		}
		
		@Override
		public int getTabIndex() {
			return getElement().getTabIndex();
		}

		@Override
		public void setTabIndex(int index) {
			getElement().setTabIndex(index);
		}

		@Override
		public void setAccessKey(char key) {
			FocusImpl.getFocusImplForWidget().setAccessKey(getElement(), key);
		}

		@Override
		public void setFocus(boolean focused) {
			if (focused)
				getElement().focus();
			else
				getElement().blur();
		}

	}
	
	public static native void checkParent() /*-{
		if ($wnd.parent)
			$wnd.parent.hideGwtHint();
	}-*/;
	
	public static native boolean hasParent() /*-{
		return $wnd.parent != null;
	}-*/;

	public static native void closeDialog() /*-{
		$wnd.parent.hideGwtDialog();
	}-*/;
	public static native void closeAndRefresh() /*-{
		$wnd.parent.hideGwtDialog();
		$wnd.parent.refreshPage();
	}-*/;
	public native static void openParent(String url) /*-{
		$wnd.parent.location = url;
	}-*/;
	public static native void clickElement(Element elem) /*-{
		elem.click();
	}-*/;
	public static native void centerDialog() /*-{
		$wnd.parent.centerGwtDialog();
	}-*/;
}