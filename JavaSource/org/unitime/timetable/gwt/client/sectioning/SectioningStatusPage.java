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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaTabBar;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.rooms.RoomHint;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable.TopCell;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasColSpan;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.AriaOperation;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.MenuOperation;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.EnrollmentInfo;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.IdValue;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.SectioningAction;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.StudentInfo;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.SectioningProperties;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentGroupInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessageType;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverPageMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverPageMessagesRequest;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class SectioningStatusPage extends Composite {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	public static final GwtConstants GWT_CONSTANTS = GWT.create(GwtConstants.class);
	public static final GwtMessages GWT_MESSAGES = GWT.create(GwtMessages.class);
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.requestDateFormat());
	private static DateTimeFormat sTSF = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	private static NumberFormat sNF = NumberFormat.getFormat(CONSTANTS.executionTimeFormat());
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);

	private SectioningStatusFilterBox iFilter;

	private Button iSearch = null;
	private Button iExport = null;
	private Button iMore = null;
	private Image iLoadingImage = null;
	private Button iPrevious = null;
	private Label iRange = null;
	private Button iNext = null;
	private P iPaginationButtons = null;

	private VerticalPanel iSectioningPanel = null;
	
	private VerticalPanel iPanel = null;
	private HorizontalPanel iFilterPanel = null;
	
	private UniTimeTable<EnrollmentInfo> iCourseTable = null;
	private UniTimeTable<StudentInfo> iStudentTable = null;
	private UniTimeTable<SectioningAction> iLogTable = null;
	
	private VerticalPanel iCourseTableWithHint = null;
	private VerticalPanel iStudentTableWithHint = null;
	
	private UniTimeDialogBox iEnrollmentDialog = null;
	private EnrollmentTable iEnrollmentTable = null;
	private ScrollPanel iEnrollmentScroll = null;
	private AriaTabBar iTabBar = null;
	private SimplePanel iTabContent = null;
	
	private int iTabIndex = 0;
	private FocusPanel iTabPanelWithFocus = null;
	private SectioningProperties iProperties = null;
	
	private HTML iError = null, iCourseTableHint, iStudentTableHint;
	private String iLastFilterOnEnter = null, iCourseFilter = null;
	private SectioningStatusFilterRpcRequest iCourseFilterRequest = null;
	private Set<StudentStatusInfo> iStates = null;
	private StudentStatusDialog iStudentStatusDialog = null;
	private int iStatusColumn = 0, iNoteColumn = 0, iGroupColumn = 0;
	private Map<String, Integer> iGroupsColumn = new HashMap<String, Integer>();
	private Set<Long> iSelectedStudentIds = new HashSet<Long>();
	private Set<Long> iSelectedCourseIds = new HashSet<Long>();
	private boolean iOnline; 
	
	private List<Operation> iSortOperations = new ArrayList<Operation>();
	
	private List<StudentInfo> iStudentInfos = null;
	private StudentsInfoVisibleColumns iStudentInfoVisibleColumns = null;
	private int iStudentInfosFirstLine = -1;
	
	private List<EnrollmentInfo> iEnrollmentInfos = null;
	private Map<Long, List<EnrollmentInfo>> iClassInfos = new HashMap<Long, List<EnrollmentInfo>>();
	private int iEnrollmentInfosFirstLine = -1;
	
	private List<SectioningAction> iSectioningActions = null;
	private SectioningActionsVisibleColumns iSectioningActionsVisibleColumns = null;
	private int iSectioningActionsFirstLine = -1;
	
	private int iMaxTableLines = CONSTANTS.dashboardMaxLines();
	
	public SectioningStatusPage(boolean online) {
		iOnline = online;
		
		String max = Window.Location.getParameter("pagination");
		if (max != null && !max.isEmpty()) {
			iMaxTableLines = Integer.parseInt(max);
		}

		iPanel = new VerticalPanel();
		iPanel.addStyleName("unitime-SectioningStatusPage");
		iSectioningPanel = new VerticalPanel();
		
		iFilterPanel = new HorizontalPanel();
		iFilterPanel.addStyleName("unitime-SectioningStatusFilter");
		iFilterPanel.setSpacing(3);
		
		Label filterLabel = new Label(MESSAGES.filter());
		iFilterPanel.add(filterLabel);
		iFilterPanel.setCellVerticalAlignment(filterLabel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iFilter = new SectioningStatusFilterBox(online);
		iFilterPanel.add(iFilter);
		
		iSearch = new AriaButton(MESSAGES.buttonSearch());
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iSearch);		
		iFilterPanel.setCellVerticalAlignment(iSearch, HasVerticalAlignment.ALIGN_TOP);
		
		iExport = new AriaButton(MESSAGES.buttonExport());
		iExport.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iExport);
		iFilterPanel.setCellVerticalAlignment(iExport, HasVerticalAlignment.ALIGN_TOP);
		
		iMore = new AriaButton(MESSAGES.buttonMoreOperations());
		iMore.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iMore);
		iFilterPanel.setCellVerticalAlignment(iMore, HasVerticalAlignment.ALIGN_TOP);
		iMore.setVisible(false);
		iMore.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new UniTimeTableHeader.MenuBarWithAccessKeys();
				
				boolean first = true;
				if (iOnline && iTabBar.getSelectedTab() == 1 && iStudentTable.getHeader(0) != null) {
					for (final Operation op: iStudentTable.getHeader(0).getOperations()) {
						if (!op.isApplicable()) continue;
						if (op.hasSeparator() && !first)
							menu.addSeparator();
						first = false;
						if (op instanceof MenuOperation) {
							MenuBar submenu = new MenuBar(true);
							((MenuOperation)op).generate(popup, submenu); op.execute();
							MenuItem item = new MenuItem(op.getName(), true, submenu);
							if (op instanceof AriaOperation)
								Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), ((AriaOperation)op).getAriaLabel());
							else
								Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), UniTimeHeaderPanel.stripAccessKey(op.getName()));
							item.getElement().getStyle().setCursor(Cursor.POINTER);
							menu.addItem(item);
						} else {
							MenuItem item = new MenuItem(op.getName(), true, new Command() {
								@Override
								public void execute() {
									popup.hide();
									op.execute();
								}
							});
							if (op instanceof AriaOperation)
								Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), ((AriaOperation)op).getAriaLabel());
							else
								Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), UniTimeHeaderPanel.stripAccessKey(op.getName()));
							menu.addItem(item);
						}
					}
					
					if (!iSortOperations.isEmpty()) {
						if (!first) menu.addSeparator();
						MenuBar submenu = new MenuBar(true);
						for (final Operation op: iSortOperations) {
							String name = op.getName();
							if (op instanceof HasColumnName)
								name = ((HasColumnName)op).getColumnName();
							MenuItem item = new MenuItem(name, true, new Command() {
								@Override
								public void execute() {
									popup.hide();
									op.execute();
								}
							});
							if (op instanceof AriaOperation)
								Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), ((AriaOperation)op).getAriaLabel());
							else
								Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), UniTimeHeaderPanel.stripAccessKey(op.getName()));
							submenu.addItem(item);
						}
						MenuItem columns = new MenuItem(MESSAGES.opSort(), submenu);
						columns.getElement().getStyle().setCursor(Cursor.POINTER);
						menu.addItem(columns);
					}
				} else {
					if (!iSortOperations.isEmpty()) {
						for (final Operation op: iSortOperations) {
							String name = op.getName();
							MenuItem item = new MenuItem(name, true, new Command() {
								@Override
								public void execute() {
									popup.hide();
									op.execute();
								}
							});
							if (op instanceof AriaOperation)
								Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), ((AriaOperation)op).getAriaLabel());
							else
								Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), UniTimeHeaderPanel.stripAccessKey(op.getName()));
							menu.addItem(item);
						}
					}
				}
				
				popup.add(menu);
				popup.showRelativeTo((UIObject)event.getSource());
				menu.focus();
			}
		});

		iLoadingImage = new Image(RESOURCES.loading_small());
		iLoadingImage.setVisible(false);
		iFilterPanel.add(iLoadingImage);
		iFilterPanel.setCellVerticalAlignment(iLoadingImage, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iSectioningPanel.add(iFilterPanel);
		iSectioningPanel.setCellHorizontalAlignment(iFilterPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		iCourseTable = new UniTimeTable<EnrollmentInfo>(); iCourseTable.addStyleName("unitime-EnrollmentsTable");
		iStudentTable = new UniTimeTable<StudentInfo>(); iStudentTable.addStyleName("unitime-StudentsTable");
		iLogTable = new UniTimeTable<SectioningAction>(); iLogTable.addStyleName("unitime-LogsTable");

		iCourseTableWithHint = new VerticalPanel();
		iCourseTableWithHint.add(iCourseTable);
		iCourseTableHint = new HTML(MESSAGES.sectioningStatusReservationHint());
		iCourseTableHint.setStyleName("unitime-Hint");
		iCourseTableWithHint.add(iCourseTableHint);
		iCourseTableWithHint.setCellHorizontalAlignment(iCourseTableHint, HasHorizontalAlignment.ALIGN_RIGHT);
		
		iStudentTableWithHint = new VerticalPanel();
		iStudentTableWithHint.add(iStudentTable);
		iStudentTableHint = new HTML(MESSAGES.sectioningStatusPriorityHint());
		iStudentTableHint.setStyleName("unitime-Hint");
		iStudentTableWithHint.add(iStudentTableHint);
		iStudentTableWithHint.setCellHorizontalAlignment(iStudentTableHint, HasHorizontalAlignment.ALIGN_RIGHT);
		
		iTabBar = new AriaTabBar();
		iTabBar.addTab(MESSAGES.tabEnrollments(), true);
		iTabBar.addTab(MESSAGES.tabStudents(), true);
		iTabBar.selectTab(0);
		iTabBar.setVisible(false);
		
		iPrevious = new AriaButton(GWT_MESSAGES.buttonPrevious());
		iPrevious.addStyleName("unitime-NoPrint");
		iRange = new Label();
		iRange.addStyleName("range-label");
		iNext = new AriaButton(GWT_MESSAGES.buttonNext());
		iNext.addStyleName("unitime-NoPrint");
		iPrevious.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iTabBar.getSelectedTab() == 0) {
					if (iEnrollmentInfosFirstLine >= iMaxTableLines && iMaxTableLines > 0)
						fillCourseTable(iEnrollmentInfosFirstLine - iMaxTableLines);
				} else if (iTabBar.getSelectedTab() == 1) {
					if (iStudentInfosFirstLine >= iMaxTableLines && iMaxTableLines > 0)
						fillStudentTable(iStudentInfosFirstLine - iMaxTableLines);
				} else {
					if (iSectioningActionsFirstLine >= iMaxTableLines && iMaxTableLines > 0)
						fillLogTable(iSectioningActionsFirstLine - iMaxTableLines);
				}
			}
		});
		iNext.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iTabBar.getSelectedTab() == 0) {
					if (iEnrollmentInfos != null && iMaxTableLines > 0 && iEnrollmentInfosFirstLine + iMaxTableLines < iEnrollmentInfos.size() - 1)
						fillCourseTable(iEnrollmentInfosFirstLine + iMaxTableLines);
				} else if (iTabBar.getSelectedTab() == 1) {
					if (iStudentInfos != null && iMaxTableLines > 0 && iStudentInfosFirstLine + iMaxTableLines < iStudentInfos.size() - 1)
						fillStudentTable(iStudentInfosFirstLine + iMaxTableLines);
				} else {
					if (iSectioningActions != null && iMaxTableLines > 0 && iSectioningActionsFirstLine + iMaxTableLines < iSectioningActions.size() - 1)
						fillLogTable(iSectioningActionsFirstLine + iMaxTableLines);
				}
			}
		});
		
		iPaginationButtons = new P("pagination-buttons");
		iPaginationButtons.add(iPrevious);
		iPaginationButtons.add(iRange);
		iPaginationButtons.add(iNext);
		iTabBar.setRestWidget(iPaginationButtons);
		
		iTabContent = new SimplePanel();
		iTabContent.addStyleName("unitime-TabPanel");
		
		iTabContent.setWidget(iCourseTableWithHint);
		iTabContent.setVisible(false);
		
		iTabBar.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				iTabIndex = event.getSelectedItem();
				iMore.setVisible(
						(iTabIndex == 0 && iCourseTable.getRowCount() > 2) ||
						(iTabIndex == 1 && iStudentTable.getRowCount() > 2) ||
						(iTabIndex == 2 && iLogTable.getRowCount() > 1));
				loadDataIfNeeded();
				iPaginationButtons.setVisible(false);
				if (iTabIndex == 0) {
					iTabContent.setWidget(iCourseTableWithHint);
					iPaginationButtons.setVisible(iEnrollmentInfos != null && iMaxTableLines > 0 && iEnrollmentInfos.size() - 1 > iMaxTableLines);
					iPrevious.setEnabled(iEnrollmentInfos != null && iMaxTableLines > 0 && iEnrollmentInfosFirstLine >= iMaxTableLines);
					iNext.setEnabled(iEnrollmentInfos != null && iMaxTableLines > 0 && iEnrollmentInfosFirstLine + iMaxTableLines < iEnrollmentInfos.size() - 1);
					iRange.setText(iEnrollmentInfos == null ? "" : MESSAGES.pageRange(iEnrollmentInfosFirstLine + 1, Math.min(iEnrollmentInfos.size() - 1, iEnrollmentInfosFirstLine + iMaxTableLines)));
				} else if (iTabIndex == 1) {
					iTabContent.setWidget(iStudentTableWithHint);
					iPaginationButtons.setVisible(iStudentInfos != null && iMaxTableLines > 0 && iStudentInfos.size() - 1 > iMaxTableLines);
					iPrevious.setEnabled(iStudentInfos != null && iMaxTableLines > 0 && iStudentInfosFirstLine >= iMaxTableLines);
					iNext.setEnabled(iStudentInfos != null && iMaxTableLines > 0 && iStudentInfosFirstLine + iMaxTableLines < iStudentInfos.size() - 1);
					iRange.setText(iStudentInfos == null ? "" : MESSAGES.pageRange(iStudentInfosFirstLine + 1, Math.min(iStudentInfos.size() - 1, iStudentInfosFirstLine + iMaxTableLines)));
				} else if (iLogTable != null) {
					iTabContent.setWidget(iLogTable);
					iPaginationButtons.setVisible(iSectioningActions != null && iMaxTableLines > 0 && iSectioningActions.size() > iMaxTableLines);
					iPrevious.setEnabled(iSectioningActions != null && iMaxTableLines > 0 && iSectioningActionsFirstLine >= iMaxTableLines);
					iNext.setEnabled(iSectioningActions != null && iMaxTableLines > 0 && iSectioningActionsFirstLine + iMaxTableLines < iSectioningActions.size() );
					iRange.setText(iSectioningActions == null ? "" : MESSAGES.pageRange(iSectioningActionsFirstLine + 1, Math.min(iSectioningActions.size(), iSectioningActionsFirstLine + iMaxTableLines)));
				}
			}
		});

		iTabPanelWithFocus = new FocusPanel(iTabBar);
		iTabPanelWithFocus.setStyleName("unitime-FocusPanel");
		iSectioningPanel.add(iTabPanelWithFocus);
		iSectioningPanel.add(iTabContent);
	
		iTabPanelWithFocus.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='e' || event.getNativeKeyCode()=='E')) {
					iTabBar.selectTab(0);
					event.preventDefault();
				}
				if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='s' || event.getNativeKeyCode()=='S')) {
					iTabBar.selectTab(1);
					event.preventDefault();
				}
				if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='l' || event.getNativeKeyCode()=='L')) {
					if (iTabBar.getTabCount() >= 3) {
						iTabBar.selectTab(2);
						event.preventDefault();
					}
				}
			}
		});
		iSectioningPanel.setWidth("100%");
		
		iPanel.add(iSectioningPanel);
		
		iError = new HTML();
		iError.setStyleName("unitime-ErrorMessage");
		iError.setVisible(false);
		iPanel.add(iError);
		iPanel.setCellHorizontalAlignment(iError, HasHorizontalAlignment.ALIGN_CENTER);
		
		initWidget(iPanel);
		
		iSearch.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				loadData();
			}
		});
		
		iExport.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				exportData();
			}
		});
		
		iFilter.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					if (iFilter.getValue().equals(iLastFilterOnEnter) && !iFilter.getValue().equals(iCourseFilter))
						loadData();
					else
						iLastFilterOnEnter = iFilter.getValue();
				}
			}
		});
		

		iCourseTable.addMouseClickListener(new MouseClickListener<ClassAssignmentInterface.EnrollmentInfo>() {
			@Override
			public void onMouseClick(final TableEvent<EnrollmentInfo> event) {
				if (event.getData() == null || event.getData().getCourseId() == null) return; // header or footer
				iCourseTable.clearHover();
				setLoading(true);
				final Long id = (event.getData().getConfigId() == null ? event.getData().getOfferingId() : -event.getData().getClazzId());
				iError.setVisible(false);
				if (event.getData().getConfigId() == null)
					LoadingWidget.getInstance().show(MESSAGES.loadingEnrollments(MESSAGES.course(event.getData().getSubject(), event.getData().getCourseNbr())));
				else
					LoadingWidget.getInstance().show(MESSAGES.loadingEnrollments(MESSAGES.clazz(event.getData().getSubject(), event.getData().getCourseNbr(), event.getData().getSubpart(), event.getData().getClazz())));
				if (iOnline) {
					iSectioningService.canApprove(id, new AsyncCallback<List<Long>>() {
						@Override
						public void onSuccess(final List<Long> courseIdsCanApprove) {
							iSectioningService.findEnrollments(iOnline, iCourseFilter, iCourseFilterRequest, event.getData().getCourseId(), event.getData().getClazzId(), new AsyncCallback<List<Enrollment>>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
									setLoading(false);
									iError.setHTML(caught.getMessage());
									iError.setVisible(true);
									ToolBox.checkAccess(caught);
								}
								@Override
								public void onSuccess(List<Enrollment> result) {
									LoadingWidget.getInstance().hide();
									setLoading(false);
									iEnrollmentTable.clear();
									iEnrollmentTable.setId(id);
									iEnrollmentTable.populate(result, courseIdsCanApprove);
									if (event.getData().getConfigId() == null)
										iEnrollmentDialog.setText(MESSAGES.titleEnrollments(MESSAGES.course(event.getData().getSubject(), event.getData().getCourseNbr())));
									else
										iEnrollmentDialog.setText(MESSAGES.titleEnrollments(MESSAGES.clazz(event.getData().getSubject(), event.getData().getCourseNbr(), event.getData().getSubpart(), event.getData().getClazz())));
									iEnrollmentDialog.center();
								}
							});
						}
						
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught);
							setLoading(false);
							iError.setHTML(caught.getMessage());
							iError.setVisible(true);
							ToolBox.checkAccess(caught);
						}
					});					
				} else {
					iSectioningService.findEnrollments(iOnline, iCourseFilter, iCourseFilterRequest, event.getData().getCourseId(), event.getData().getClazzId(), new AsyncCallback<List<Enrollment>>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught);
							setLoading(false);
							iError.setHTML(caught.getMessage());
							iError.setVisible(true);
							ToolBox.checkAccess(caught);
						}
						@Override
						public void onSuccess(List<Enrollment> result) {
							LoadingWidget.getInstance().hide();
							setLoading(false);
							iEnrollmentTable.clear();
							iEnrollmentTable.setId(id);
							iEnrollmentTable.populate(result, null);
							if (event.getData().getConfigId() == null)
								iEnrollmentDialog.setText(MESSAGES.titleEnrollments(MESSAGES.course(event.getData().getSubject(), event.getData().getCourseNbr())));
							else
								iEnrollmentDialog.setText(MESSAGES.titleEnrollments(MESSAGES.clazz(event.getData().getSubject(), event.getData().getCourseNbr(), event.getData().getSubpart(), event.getData().getClazz())));
							iEnrollmentDialog.center();
						}
					});
				}
			}
		});
		
		iStudentTable.addMouseClickListener(new MouseClickListener<StudentInfo>() {
			@Override
			public void onMouseClick(final TableEvent<StudentInfo> event) {
				if (event.getData() == null || event.getData().getStudent() == null) return; // header or footer
				iStudentTable.clearHover();
				LoadingWidget.getInstance().show(MESSAGES.loadingEnrollment(event.getData().getStudent().getName()));
				iError.setVisible(false);
				iEnrollmentTable.showStudentSchedule(event.getData().getStudent(), new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iError.setHTML(caught.getMessage());
						iError.setVisible(true);
					}

					@Override
					public void onSuccess(Boolean result) {
						LoadingWidget.getInstance().hide();
					}
				});
			}
		});
		
		iLogTable.addMouseClickListener(new MouseClickListener<ClassAssignmentInterface.SectioningAction>() {
			@Override
			public void onMouseClick(TableEvent<SectioningAction> event) {
				if (event.getData() != null && event.getData().getProto() != null) {
					final HTML widget = new HTML(event.getData().getProto());
					final ScrollPanel scroll = new ScrollPanel(widget);
					scroll.setHeight(((int)(0.8 * Window.getClientHeight())) + "px");
					scroll.setStyleName("unitime-ScrollPanel");
					final UniTimeDialogBox dialog = new UniTimeDialogBox(true, false);
					dialog.setWidget(scroll);
					dialog.setText(MESSAGES.dialogChangeMessage(event.getData().getStudent().getName()));
					dialog.setEscapeToHide(true);
					dialog.addOpenHandler(new OpenHandler<UniTimeDialogBox>() {
						@Override
						public void onOpen(OpenEvent<UniTimeDialogBox> event) {
							RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
							scroll.setHeight(Math.min(widget.getElement().getScrollHeight(), Window.getClientHeight() * 80 / 100) + "px");
							dialog.setPopupPosition(
									Math.max(Window.getScrollLeft() + (Window.getClientWidth() - dialog.getOffsetWidth()) / 2, 0),
									Math.max(Window.getScrollTop() + (Window.getClientHeight() - dialog.getOffsetHeight()) / 2, 0));
						}
					});
					dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
						@Override
						public void onClose(CloseEvent<PopupPanel> event) {
							iLogTable.clearHover();
							RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
						}
					});
					dialog.center();
				}
			}
		});
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				clearData();
				if (event.getValue().endsWith("@")) {
					iFilter.setValue(event.getValue().substring(0, event.getValue().length() - 1), true);
					iTabBar.selectTab(1);
				} else if (event.getValue().endsWith("$")) {
					iFilter.setValue(event.getValue().substring(0, event.getValue().length() - 1), true);
					iTabBar.selectTab(2);
				} else {
					iFilter.setValue(event.getValue(), true);
					if (iTabIndex != 0)
						iTabBar.selectTab(0);
					else
						loadData();
				}
			}
		});
		
		iEnrollmentTable = new EnrollmentTable(false, iOnline);
		iEnrollmentScroll = new ScrollPanel(iEnrollmentTable);
		iEnrollmentScroll.setHeight(((int)(0.8 * Window.getClientHeight())) + "px");
		iEnrollmentScroll.setStyleName("unitime-ScrollPanel");
		iEnrollmentDialog = new UniTimeDialogBox(true, false);
		iEnrollmentDialog.setEscapeToHide(true);
		iEnrollmentDialog.setWidget(iEnrollmentScroll);
		iEnrollmentDialog.addOpenHandler(new OpenHandler<UniTimeDialogBox>() {
			@Override
			public void onOpen(OpenEvent<UniTimeDialogBox> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
				iEnrollmentScroll.setHeight(Math.min(iEnrollmentTable.getElement().getScrollHeight(), Window.getClientHeight() * 80 / 100) + "px");
				iEnrollmentDialog.setPopupPosition(
						Math.max(Window.getScrollLeft() + (Window.getClientWidth() - iEnrollmentDialog.getOffsetWidth()) / 2, 0),
						Math.max(Window.getScrollTop() + (Window.getClientHeight() - iEnrollmentDialog.getOffsetHeight()) / 2, 0));
			}
		});
		iEnrollmentTable.getHeader().addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iEnrollmentDialog.hide();
			}
		});

		iEnrollmentDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
			}
		});
		
		iSectioningService.getProperties(null, new AsyncCallback<SectioningProperties>() {
			@Override
			public void onSuccess(SectioningProperties result) {
				iProperties = result;
				if (iProperties.isChangeLog() && iOnline)
					iTabBar.addTab(MESSAGES.tabChangeLog(), true);
				checkLastQuery();
			}

			@Override
			public void onFailure(Throwable caught) {
				iError.setHTML(caught.getMessage());
				iError.setVisible(true);
			}
		});
		
		iSectioningService.lookupStudentSectioningStates(new AsyncCallback<List<StudentStatusInfo>>() {

			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(List<StudentStatusInfo> result) {
				iStates = new TreeSet<StudentStatusInfo>(result);
				iStudentStatusDialog = new StudentStatusDialog(iStates);
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
	}
	
	private void checkLastQuery() {
		if (Window.Location.getParameter("q") != null) {
			iFilter.setValue(Window.Location.getParameter("q"), true);
			if (Window.Location.getParameter("t") != null) {
				if ("2".equals(Window.Location.getParameter("t"))) {
					iTabBar.selectTab(1);
				} else {
					iTabBar.selectTab(0);
				}
			} else {
				loadData();
			}
		} else if (Window.Location.getHash() != null && !Window.Location.getHash().isEmpty()) {
			String hash = URL.decode(Window.Location.getHash().substring(1));
			if (!hash.matches("^[0-9]+\\:?[0-9]*@?$")) {
				if (hash.endsWith("@")) {
					iFilter.setValue(hash.substring(0, hash.length() - 1), true);
					iTabBar.selectTab(1);
				} else if (hash.endsWith("$")) {
					iFilter.setValue(hash.substring(0, hash.length() - 1), true);
					iTabBar.selectTab(2);
				} else {
					iFilter.setValue(hash, true);
					loadData();
				}
			}
		} else {
			String q = SectioningStatusCookie.getInstance().getQuery(iOnline);
			if (q != null) iFilter.setValue(q, true);
			int t = SectioningStatusCookie.getInstance().getTab(iOnline);
			if (t >= 0 && t < iTabBar.getTabCount()) {
				iTabBar.selectTab(t, false);
				iTabIndex = -1;
			}
			if (GWT_CONSTANTS.searchWhenPageIsLoaded() && q != null && !q.isEmpty())
				loadData();
		}
	}
	
	private void setLoading(boolean loading) {
		iLoadingImage.setVisible(loading);
		iSearch.setVisible(!loading);
		iExport.setVisible(!loading);
		if (loading) {
			iMore.setVisible(false);
			iNext.setEnabled(false);
			iPrevious.setEnabled(false);
		} else {
			iMore.setVisible(
					(iTabIndex == 0 && iCourseTable.getRowCount() > 2) ||
					(iTabIndex == 1 && iStudentTable.getRowCount() > 2) ||
					(iTabIndex == 2 && iLogTable.getRowCount() > 1));
			if (iTabIndex == 0) {
				iPrevious.setEnabled(iEnrollmentInfos != null && iMaxTableLines > 0 && iEnrollmentInfosFirstLine >= iMaxTableLines);
				iNext.setEnabled(iEnrollmentInfos != null && iMaxTableLines > 0 && iEnrollmentInfosFirstLine + iMaxTableLines < iEnrollmentInfos.size() - 1);
			} else if (iTabIndex == 1) {
				iPrevious.setEnabled(iStudentInfos != null && iMaxTableLines > 0 && iStudentInfosFirstLine >= iMaxTableLines);
				iNext.setEnabled(iStudentInfos != null && iMaxTableLines > 0 && iStudentInfosFirstLine + iMaxTableLines < iStudentInfos.size() - 1);
			} else if (iLogTable != null) {
				iPrevious.setEnabled(iSectioningActions != null && iMaxTableLines > 0 && iSectioningActionsFirstLine >= iMaxTableLines);
				iNext.setEnabled(iSectioningActions != null && iMaxTableLines > 0 && iSectioningActionsFirstLine + iMaxTableLines < iSectioningActions.size() );
			}
		}
	}
	
	protected void clearData() {
		iCourseTable.clearTable();
		iStudentTable.clearTable();
		iLogTable.clearTable();
		iPaginationButtons.setVisible(false);
		iStudentInfos = null; iStudentInfosFirstLine = -1;
		iEnrollmentInfos = null; iEnrollmentInfosFirstLine = -1; iClassInfos.clear();
		iSectioningActions = null; iSectioningActionsFirstLine = -1;
	}
	
	private void loadData() {
		clearData();
		loadDataIfNeeded();
	}
	
	private void loadDataIfNeeded() {
		if (iTabIndex < 0) {
			iTabBar.selectTab(SectioningStatusCookie.getInstance().getTab(iOnline));
			return;
		}
		
		iCourseFilter = iFilter.getValue();
		iCourseFilterRequest = iFilter.getElementsRequest();
		History.newItem(iCourseFilter + (iTabIndex == 1 ? "@" : iTabIndex == 2 ? "$" : ""), false);
		SectioningStatusCookie.getInstance().setQueryTab(iOnline, iFilter.getValue(), iTabIndex);
		
		if (iFilter.isFilterPopupShowing()) iFilter.hideFilterPopup();
		
		if (iTabIndex == 0 && iCourseTable.getRowCount() > 0) return;
		if (iTabIndex == 1 && iStudentTable.getRowCount() > 0) return;
		if (iTabIndex == 2 && iLogTable.getRowCount() > 0) return;
		
		LoadingWidget.getInstance().show(MESSAGES.loadingData());
		setLoading(true);
		iError.setVisible(false);
		if (iTabIndex == 0) {
			iSectioningService.findEnrollmentInfos(iOnline, iCourseFilter, iCourseFilterRequest, null, new AsyncCallback<List<EnrollmentInfo>>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					setLoading(false);
					iError.setHTML(caught.getMessage());
					iError.setVisible(true);
					iTabBar.setVisible(false); iTabContent.setVisible(false);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(List<EnrollmentInfo> result) {
					if (result.isEmpty()) {
						iError.setHTML(MESSAGES.exceptionNoMatchingResultsFound(iCourseFilter));
						iError.setVisible(true);
						iTabBar.setVisible(false); iTabContent.setVisible(false);
					} else {
						populateCourseTable(result);
						iTabBar.setVisible(true); iTabContent.setVisible(true);
					}
					setLoading(false);
					LoadingWidget.getInstance().hide();
				}
			});
		} else if (iTabIndex == 1) {
			if (iOnline) {
				iSectioningService.findStudentInfos(iOnline, iCourseFilter, iCourseFilterRequest, new AsyncCallback<List<StudentInfo>>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						setLoading(false);
						iError.setHTML(caught.getMessage());
						iError.setVisible(true);
						iTabBar.setVisible(false); iTabContent.setVisible(false);
						ToolBox.checkAccess(caught);
					}

					@Override
					public void onSuccess(List<StudentInfo> result) {
						if (result.isEmpty()) {
							iError.setHTML(MESSAGES.exceptionNoMatchingResultsFound(iCourseFilter));
							iError.setVisible(true);
							iTabBar.setVisible(false); iTabContent.setVisible(false);
						} else {
							populateStudentTable(result);
							iTabBar.setVisible(true); iTabContent.setVisible(true);
						}
						setLoading(false);
						LoadingWidget.getInstance().hide();
					}
				});			
			} else {
				iSectioningService.findStudentInfos(iOnline, iCourseFilter, iCourseFilterRequest, new AsyncCallback<List<StudentInfo>>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						setLoading(false);
						iError.setHTML(caught.getMessage());
						iError.setVisible(true);
						iTabBar.setVisible(false); iTabContent.setVisible(false);
						ToolBox.checkAccess(caught);
					}

					@Override
					public void onSuccess(List<StudentInfo> result) {
						if (result.isEmpty()) {
							iError.setHTML(MESSAGES.exceptionNoMatchingResultsFound(iCourseFilter));
							iError.setVisible(true);
							iTabBar.setVisible(false); iTabContent.setVisible(false);
						} else {
							populateStudentTable(result);
							iTabBar.setVisible(true); iTabContent.setVisible(true);
						}
						setLoading(false);
						LoadingWidget.getInstance().hide();
					}
				});
			}
		} else if (iOnline) {
			iSectioningService.changeLog(iCourseFilter, new AsyncCallback<List<SectioningAction>>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					setLoading(false);
					iError.setHTML(caught.getMessage());
					iError.setVisible(true);
					iTabBar.setVisible(false); iTabContent.setVisible(false);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(final List<SectioningAction> result) {
					populateChangeLog(result);
					iTabBar.setVisible(true); iTabContent.setVisible(true);
					setLoading(false);
					LoadingWidget.getInstance().hide();
				}
			});
		}
	}
	
	private void exportData() {
		int tab = iTabIndex;
		if (tab < 0)
			tab = SectioningStatusCookie.getInstance().getTab(iOnline);
		
		String query = "output=student-dashboard.csv&online=" + (iOnline ? 1 : 0) + "&tab=" + tab + "&sort=" + SectioningStatusCookie.getInstance().getSortBy(iOnline, tab);
		if (tab == 0)
			for (Long courseId: iSelectedCourseIds)
				query += "&c=" + courseId;
		if (tab == 1)
			query += "&g=" + SectioningStatusCookie.getInstance().getSortByGroup(iOnline);
		query += "&query=" + URL.encodeQueryString(iFilter.getValue());
		FilterRpcRequest req = iFilter.getElementsRequest();
		if (req.hasOptions()) {
			for (Map.Entry<String, Set<String>> option: req.getOptions().entrySet()) {
				for (String value: option.getValue()) {
					query += "&f:" + option.getKey() + "=" + URL.encodeQueryString(value);
				}
			}
		}
		if (req.getText() != null && !req.getText().isEmpty()) {
			query += "&f:text=" + URL.encodeQueryString(req.getText());
		}
		
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
	
	private List<Widget> line(final EnrollmentInfo e) {
		List<Widget> line = new ArrayList<Widget>();
		if (e.getConfigId() == null) {
			if (e.getCourseId() != null) {
				final Image showDetails = new Image(iClassInfos.containsKey(e.getCourseId()) && iSelectedCourseIds.contains(e.getCourseId()) ? RESOURCES.treeOpen() : RESOURCES.treeClosed());
				showDetails.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						final int row = iCourseTable.getCellForEvent(event).getRowIndex();
						if (row + 1 == iCourseTable.getRowCount() || iCourseTable.getData(row + 1).getConfigId() == null) { // open
							List<EnrollmentInfo> classes = iClassInfos.get(e.getCourseId());
							if (classes != null) {
								iSelectedCourseIds.add(e.getCourseId());
								setLoading(false);
								int r = row + 1;
								for (EnrollmentInfo e: classes) {
									iCourseTable.insertRow(r);
									iCourseTable.setRow(r, e, line(e));
									r++;
								}
							} else {
								setLoading(true);
								iError.setVisible(false);
								showDetails.setResource(RESOURCES.treeOpen());
								iSectioningService.findEnrollmentInfos(iOnline, iCourseFilter, iCourseFilterRequest, e.getCourseId(), new AsyncCallback<List<EnrollmentInfo>>() {
									@Override
									public void onFailure(Throwable caught) {
										setLoading(false);
										iError.setHTML(caught.getMessage());
										iError.setVisible(true);
										ToolBox.checkAccess(caught);
									}

									@Override
									public void onSuccess(List<EnrollmentInfo> result) {
										iClassInfos.put(e.getCourseId(), result);
										iSelectedCourseIds.add(e.getCourseId());
										setLoading(false);
										int r = row + 1;
										for (EnrollmentInfo e: result) {
											iCourseTable.insertRow(r);
											iCourseTable.setRow(r, e, line(e));
											r++;
										}
									}
								});
							}
						} else {
							for (int r = row + 1; r < iCourseTable.getRowCount(); r++) {
								if (iCourseTable.getData(r).getConfigId() == null) break;
								iCourseTable.getRowFormatter().setVisible(r, !iCourseTable.getRowFormatter().isVisible(r));
							}
							if (iSelectedCourseIds.remove(e.getCourseId())) {
								showDetails.setResource(RESOURCES.treeClosed());
							} else {
								iSelectedCourseIds.add(e.getCourseId());
								showDetails.setResource(RESOURCES.treeOpen());
							}
						}
						event.getNativeEvent().stopPropagation();
						event.getNativeEvent().preventDefault();
					}
				});
				line.add(showDetails);
			} else {
				line.add(new Label());
			}
			line.add(new Label(e.getSubject(), false));
			line.add(new Label(e.getCourseNbr(), false));
			line.add(new TitleCell(e.getTitle() == null ? "" : e.getTitle()));
			line.add(new Label(e.getConsent() == null ? "" : e.getConsent(), false));
		} else {
			line.add(new Label());
			line.add(new HTML("&nbsp;&nbsp;" + (e.getSubpart() == null ? "" : e.getIndent() + e.getSubpart()), false));
			line.add(new HTML(e.getClazz() == null ? "" : e.getIndent() + e.getClazz(), false));
			line.add(new Label(e.getAssignment().getDays().isEmpty()  ? "" : e.getAssignment().getDaysString(CONSTANTS.shortDays()) + " " + e.getAssignment().getStartString(CONSTANTS.useAmPm()) + " - " + e.getAssignment().getEndString(CONSTANTS.useAmPm()), false));
			line.add(new Label(!e.getAssignment().hasDatePattern()  ? "" : e.getAssignment().getDatePattern(), false));
			line.add(new RoomsCell(e.getAssignment().getRooms(), ","));
		}
		if (e.getCourseId() == null)
			line.add(new NumberCell(e.getAvailable(), e.getLimit()));
		else
			line.add(new AvailableCell(e));
		line.add(new NumberCell(null, e.getProjection()));
		line.add(new NumberCell(e.getEnrollment(), e.getTotalEnrollment()));
		line.add(new WaitListCell(e));
		line.add(new NumberCell(e.getUnassignedAlternative(), e.getTotalUnassignedAlternative()));
		line.add(new NumberCell(e.getReservation(), e.getTotalReservation()));
		line.add(new NumberCell(e.getConsentNeeded(), e.getTotalConsentNeeded()));
		line.add(new NumberCell(e.getOverrideNeeded(), e.getTotalOverrideNeeded()));
		return line;
	}
	
	public void populateCourseTable(List<EnrollmentInfo> result) {
		iEnrollmentInfos = result; iEnrollmentInfosFirstLine = 0;
		iClassInfos.clear();
		iSelectedCourseIds.clear();
		iSortOperations.clear();
		List<Widget> header = new ArrayList<Widget>();

		UniTimeTableHeader hOperations = new UniTimeTableHeader("");
		header.add(hOperations);

		UniTimeTableHeader hSubject = new UniTimeTableHeader(MESSAGES.colSubject() + "<br>&nbsp;&nbsp;" + MESSAGES.colSubpart());
		header.add(hSubject);
		addSortOperation(hSubject, EnrollmentComparator.SortBy.SUBJECT, MESSAGES.colSubject());
		
		UniTimeTableHeader hCourse = new UniTimeTableHeader(MESSAGES.colCourse() + "<br>" + MESSAGES.colClass());
		header.add(hCourse);
		addSortOperation(hCourse, EnrollmentComparator.SortBy.COURSE, MESSAGES.colCourse());

		UniTimeTableHeader hTitleSubpart = new UniTimeTableHeader(MESSAGES.colTitle() + "<br>" + MESSAGES.colTime());
		header.add(hTitleSubpart);
		addSortOperation(hTitleSubpart, EnrollmentComparator.SortBy.TITLE, MESSAGES.colTitle());

		UniTimeTableHeader hStart = new UniTimeTableHeader("<br>" + MESSAGES.colDate());
		header.add(hStart);

		UniTimeTableHeader hRoom = new UniTimeTableHeader(MESSAGES.colConsent() + "<br>" + MESSAGES.colRoom());
		header.add(hRoom);
		addSortOperation(hRoom, EnrollmentComparator.SortBy.CONSENT, MESSAGES.colConsent());

		UniTimeTableHeader hLimit = new UniTimeTableHeader(MESSAGES.colAvailable());
		hLimit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hLimit);
		addSortOperation(hLimit, EnrollmentComparator.SortBy.LIMIT, MESSAGES.colAvailable());

		UniTimeTableHeader hProjection = new UniTimeTableHeader(MESSAGES.colProjection());
		hProjection.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hProjection);
		addSortOperation(hProjection, EnrollmentComparator.SortBy.PROJECTION, MESSAGES.colProjection());

		UniTimeTableHeader hEnrollment = new UniTimeTableHeader(MESSAGES.colEnrollment());
		hEnrollment.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hEnrollment);
		addSortOperation(hEnrollment, EnrollmentComparator.SortBy.ENROLLMENT, MESSAGES.colEnrollment());

		UniTimeTableHeader hWaitListed = new UniTimeTableHeader(MESSAGES.colWaitListed());
		hWaitListed.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hWaitListed);
		addSortOperation(hWaitListed, EnrollmentComparator.SortBy.WAITLIST, MESSAGES.colWaitListed());

		UniTimeTableHeader hAlternative = new UniTimeTableHeader(MESSAGES.colUnassignedAlternative());
		hAlternative.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hAlternative);
		addSortOperation(hAlternative, EnrollmentComparator.SortBy.ALTERNATIVES, MESSAGES.colUnassignedAlternative().replace("<br>", " "));
		
		UniTimeTableHeader hReserved = new UniTimeTableHeader(MESSAGES.colReserved());
		hReserved.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hReserved);
		addSortOperation(hReserved, EnrollmentComparator.SortBy.RESERVATION, MESSAGES.colReserved());

		UniTimeTableHeader hConsent = new UniTimeTableHeader(MESSAGES.colNeedConsent());
		hConsent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hConsent);
		addSortOperation(hConsent, EnrollmentComparator.SortBy.NEED_CONSENT, MESSAGES.colNeedConsent().replace("<br>", " "));
		
		UniTimeTableHeader hOverride = new UniTimeTableHeader(MESSAGES.colNeedOverride());
		hOverride.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hOverride);
		addSortOperation(hOverride, EnrollmentComparator.SortBy.NEED_OVERRIDE, MESSAGES.colNeedOverride().replace("<br>", " "));

		iCourseTable.addRow(null, header);
		
		boolean hasReservation = false;
		for (EnrollmentInfo e: result) {
			if (AvailableCell.hasReservedSpace(e)) { hasReservation = true; break; }
		}
		
		if (SectioningStatusCookie.getInstance().getSortBy(iOnline, 0) != 0) {
			boolean asc = (SectioningStatusCookie.getInstance().getSortBy(iOnline, 0) > 0);
			EnrollmentComparator.SortBy sort = EnrollmentComparator.SortBy.values()[Math.abs(SectioningStatusCookie.getInstance().getSortBy(iOnline, 0)) - 1];
			UniTimeTableHeader h = null;
			switch (sort) {
			case COURSE: h = hCourse; break;
			case SUBJECT: h = hSubject; break;
			case TITLE: h = hTitleSubpart; break;
			case CONSENT: h = hRoom; break;
			case LIMIT: h = hLimit; break;
			case PROJECTION: h = hProjection; break;
			case ENROLLMENT: h = hEnrollment; break;
			case NEED_CONSENT: h = hConsent; break;
			case RESERVATION: h = hReserved; break;
			case WAITLIST: h = hWaitListed; break;
			case ALTERNATIVES: h = hAlternative; break;
			case NEED_OVERRIDE: h = hOverride; break;
			}
			if (h != null) {
				Collections.sort(result, new EnrollmentComparator(sort, asc));
				if (!asc) Collections.reverse(result);
				h.setOrder(asc);
			}
		}
		
		iCourseTableHint.setVisible(hasReservation);
		
		fillCourseTable(0);
	}
	
	private void fillCourseTable(int firstLine) {
		iEnrollmentInfosFirstLine = firstLine;
		if (iCourseTable.getRowCount() > 0) iCourseTable.clearTable(1);
		for (int line = iEnrollmentInfosFirstLine; line < iEnrollmentInfos.size() - 1 && (iMaxTableLines <= 0 || line < iEnrollmentInfosFirstLine + iMaxTableLines); line++) {
			EnrollmentInfo e = iEnrollmentInfos.get(line);
			iCourseTable.addRow(e, line(e));
			iCourseTable.getRowFormatter().getElement(iCourseTable.getRowCount() - 1).addClassName("course-line");
			if (e.getCourseId() != null && iSelectedCourseIds.contains(e.getCourseId())) {
				List<EnrollmentInfo> classes = iClassInfos.get(e.getCourseId());
				if (classes != null) {
					for (EnrollmentInfo c: classes)
						iCourseTable.addRow(c, line(c));
				}
			}
		}
		if (!iEnrollmentInfos.isEmpty()) {
			EnrollmentInfo e = iEnrollmentInfos.get(iEnrollmentInfos.size() - 1);
			iCourseTable.addRow(e, line(e));
			iCourseTable.getRowFormatter().getElement(iCourseTable.getRowCount() - 1).addClassName("course-line");
		}
		if (iCourseTable.getRowCount() >= 2) {
			for (int c = 0; c < iCourseTable.getCellCount(iCourseTable.getRowCount() - 1); c++)
				iCourseTable.getCellFormatter().setStyleName(iCourseTable.getRowCount() - 1, c, "unitime-TotalRow");
			iCourseTable.getRowFormatter().getElement(iCourseTable.getRowCount() - 1).getStyle().clearBackgroundColor();
		}
		
		iRange.setText(MESSAGES.pageRange(iEnrollmentInfosFirstLine + 1, Math.min(iEnrollmentInfos.size() - 1, iEnrollmentInfosFirstLine + iMaxTableLines)));
		
		iPaginationButtons.setVisible(iEnrollmentInfos.size() - 1 > iMaxTableLines && iMaxTableLines > 0);
		iPrevious.setEnabled(iMaxTableLines > 0 && iEnrollmentInfosFirstLine >= iMaxTableLines);
		iNext.setEnabled(iMaxTableLines > 0 && iEnrollmentInfosFirstLine + iMaxTableLines < iEnrollmentInfos.size() - 1);
	}
	
	public void populateStudentTable(List<StudentInfo> result) {
		iStudentInfos = result; iStudentInfosFirstLine = 0;
		iSortOperations.clear();
		List<Widget> header = new ArrayList<Widget>();
		
		if (iOnline && iProperties != null && iProperties.isCanSelectStudent()) {
			UniTimeTableHeader hSelect = new UniTimeTableHeader("&otimes;", HasHorizontalAlignment.ALIGN_CENTER);
			header.add(hSelect);
			hSelect.setWidth("10px");
			hSelect.addAdditionalStyleName("unitime-NoPrint");
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.selectAll();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return iSelectedStudentIds.size() != iStudentInfoVisibleColumns.selectableStudents;
				}
				@Override
				public void execute() {
					iSelectedStudentIds.clear();
					for (StudentInfo info: iStudentInfos)
						if (info.getStudent() != null && info.getStudent().isCanSelect())
							iSelectedStudentIds.add(info.getStudent().getId());
					for (int row = 0; row < iStudentTable.getRowCount(); row++) {
						Widget w = iStudentTable.getWidget(row, 0);
						if (w instanceof CheckBox) {
							((CheckBox)w).setValue(true);
						}
					}
				}
			});
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.clearAll();
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
					for (int row = 0; row < iStudentTable.getRowCount(); row++) {
						Widget w = iStudentTable.getWidget(row, 0);
						if (w instanceof CheckBox) {
							((CheckBox)w).setValue(false);
						}
					}
				}
			});
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.sendStudentEmail();
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isEmail() && iStudentStatusDialog != null;
				}
				@Override
				public void execute() {
					iStudentStatusDialog.sendStudentEmail(new Command() {
						@Override
						public void execute() {
							List<Long> studentIds = new ArrayList<Long>();
							for (int row = 0; row < iStudentTable.getRowCount(); row++) {
								StudentInfo i = iStudentTable.getData(row);
								if (i != null && i.getStudent() != null && iSelectedStudentIds.contains(i.getStudent().getId())) { 
									studentIds.add(i.getStudent().getId());
									iStudentTable.setWidget(row, iStudentTable.getCellCount(row) - 1, new Image(RESOURCES.loading_small()));
								}
							}
							sendEmail(studentIds.iterator(), iStudentStatusDialog.getSubject(), iStudentStatusDialog.getMessage(), iStudentStatusDialog.getCC(), 0);
						}
					});
				}
			});
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.massCancel();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isMassCancel() && iStudentStatusDialog != null;
				}
				@Override
				public void execute() {
					iStudentStatusDialog.massCancel(new Command() {
						@Override
						public void execute() {
							if (!Window.confirm(MESSAGES.massCancelConfirmation())) return;
							final List<Long> studentIds = new ArrayList<Long>();
							for (int row = 0; row < iStudentTable.getRowCount(); row++) {
								StudentInfo i = iStudentTable.getData(row);
								if (i != null && i.getStudent() != null && iSelectedStudentIds.contains(i.getStudent().getId())) { 
									studentIds.add(i.getStudent().getId());
									iStudentTable.setWidget(row, iStudentTable.getCellCount(row) - 1, new Image(RESOURCES.loading_small()));
								}
							}
							
							LoadingWidget.getInstance().show(MESSAGES.massCanceling());
							iSectioningService.massCancel(studentIds, iStudentStatusDialog.getStatus(),
									iStudentStatusDialog.getSubject(), iStudentStatusDialog.getMessage(), iStudentStatusDialog.getCC(), new AsyncCallback<Boolean>() {

								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
									for (int row = 0; row < iStudentTable.getRowCount(); row++) {
										StudentInfo i = iStudentTable.getData(row);
										if (i != null && i.getStudent() != null && studentIds.contains(i.getStudent().getId())) {
											HTML error = new HTML(caught.getMessage());
											error.setStyleName("unitime-ErrorMessage");
											iStudentTable.setWidget(row, iStudentTable.getCellCount(row) - 1, error);
											i.setEmailDate(null);
										}
									}
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									loadData();
								}
							});
						}
					});
				}
			});
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.requestStudentUpdate();
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isRequestUpdate();
				}
				@Override
				public void execute() {
					List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
					LoadingWidget.getInstance().show(MESSAGES.requestingStudentUpdate());
					iSectioningService.requestStudentUpdate(studentIds, new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught);
						}

						@Override
						public void onSuccess(Boolean result) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.info(MESSAGES.requestStudentUpdateSuccess());
						}
					});
				}
			});
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.checkOverrideStatus();
				}
				@Override
				public boolean hasSeparator() {
					return !iProperties.isRequestUpdate();
				}
				@Override
				public boolean isApplicable() {
					return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isCheckStudentOverrides();
				}
				@Override
				public void execute() {
					List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
					LoadingWidget.getInstance().show(MESSAGES.checkingOverrideStatus());
					iSectioningService.checkStudentOverrides(studentIds, new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught);
						}

						@Override
						public void onSuccess(Boolean result) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.info(MESSAGES.checkStudentOverridesSuccess());
							loadData();
						}
					});
				}
			});
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.validateStudentOverrides();
				}
				@Override
				public boolean hasSeparator() {
					return !iProperties.isRequestUpdate() && !iProperties.isChangeStatus();
				}
				@Override
				public boolean isApplicable() {
					return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isValidateStudentOverrides();
				}
				@Override
				public void execute() {
					List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
					LoadingWidget.getInstance().show(MESSAGES.validatingStudentOverrides());
					iSectioningService.validateStudentOverrides(studentIds, new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught);
						}

						@Override
						public void onSuccess(Boolean result) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.info(MESSAGES.validateStudentOverridesSuccess());
							loadData();
						}
					});
				}
			});
			if (iStates != null) {
				boolean first = true;
				for (final StudentStatusInfo info: iStates) {
					final boolean separator = first;
					first = false;
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return MESSAGES.changeStatusTo(info.getLabel());
						}
						@Override
						public boolean hasSeparator() {
							return separator;
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isChangeStatus();
						}
						@Override
						public void execute() {
							List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
							LoadingWidget.getInstance().show(MESSAGES.changingStatusTo(info.getLabel()));
							iSectioningService.changeStatus(studentIds, null, info.getReference(), new AsyncCallback<Boolean>() {

								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
								}

								@Override
								public void onSuccess(Boolean result) {
									for (int row = 0; row < iStudentTable.getRowCount(); row++) {
										StudentInfo i = iStudentTable.getData(row);
										if (i != null && i.getStudent() != null) {
											Widget w = iStudentTable.getWidget(row, 0);
											if (w instanceof CheckBox && ((CheckBox)w).getValue()) {
												i.setStatus(info.getReference());
												((HTML)iStudentTable.getWidget(row, iStatusColumn)).setHTML(info.getReference());
											}
										}
									}
									LoadingWidget.getInstance().hide();
								}
							});
						}
					});
				}
			}
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.setStudentStatus();
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isChangeStatus() && iStudentStatusDialog != null;
				}
				@Override
				public void execute() {
					iStudentStatusDialog.setStatus(new Command() {
						@Override
						public void execute() {
							final String statusRef = iStudentStatusDialog.getStatus();
							if ("-".equals(statusRef)) return;
							List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
							LoadingWidget.getInstance().show(MESSAGES.changingStatusTo(statusRef));
							iSectioningService.changeStatus(studentIds, null, statusRef, new AsyncCallback<Boolean>() {

								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
								}

								@Override
								public void onSuccess(Boolean result) {
									for (int row = 0; row < iStudentTable.getRowCount(); row++) {
										StudentInfo i = iStudentTable.getData(row);
										if (i != null && i.getStudent() != null) {
											Widget w = iStudentTable.getWidget(row, 0);
											if (w instanceof CheckBox && ((CheckBox)w).getValue()) {
												i.setStatus(statusRef);
												((HTML)iStudentTable.getWidget(row, iStatusColumn)).setHTML(statusRef);
											}
										}
									}
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
					return MESSAGES.setStudentNote();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return iOnline && iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isChangeStatus() && iStudentStatusDialog != null;
				}
				@Override
				public void execute() {
					iStudentStatusDialog.setStudentNote(new Command() {
						@Override
						public void execute() {
							LoadingWidget.getInstance().show(MESSAGES.changingStudentNote());
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
									for (int row = 0; row < iStudentTable.getRowCount(); row++) {
										StudentInfo i = iStudentTable.getData(row);
										if (i != null && i.getStudent() != null) {
											Widget w = iStudentTable.getWidget(row, 0);
											if (w instanceof CheckBox && ((CheckBox)w).getValue()) {
												if (!"-".equals(statusRef)) {
													i.setStatus(statusRef);
													((HTML)iStudentTable.getWidget(row, iStatusColumn)).setHTML(statusRef);
												}
												i.setNote(note);
												if (iNoteColumn >= 0) {
													HTML n = ((HTML)iStudentTable.getWidget(row, iNoteColumn));
													n.setHTML(note); n.setTitle(n.getText());
												}
											}
										}
									}
									LoadingWidget.getInstance().hide();
								}
							});
						}
					});
				}
			});
			hSelect.addOperation(new MenuOperation() {
				@Override
				public String getName() {
					return MESSAGES.opAddToGroup();
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					if (iOnline && iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.hasEditableGroups()) {
						for (int row = 0; row < iStudentTable.getRowCount(); row++) {
							StudentInfo i = iStudentTable.getData(row);
							if (i != null && i.getStudent() != null && iSelectedStudentIds.contains(i.getStudent().getId())) {
								for (StudentGroupInfo g: iProperties.getEditableGroups())
									if (!i.getStudent().hasGroup(g.getReference())) return true;
							}
						}
					}
					return false;
				}
				@Override
				public void execute() {}
				@Override
				public void generate(final PopupPanel popup, MenuBar menu) {
					for (final StudentGroupInfo g: iProperties.getEditableGroups()) {
						boolean canAdd = false;
						for (int row = 0; row < iStudentTable.getRowCount(); row++) {
							StudentInfo i = iStudentTable.getData(row);
							if (i != null && i.getStudent() != null && iSelectedStudentIds.contains(i.getStudent().getId())) {
								if (!i.getStudent().hasGroup(g.getReference())) {
									canAdd = true; break;
								}
							}
						}
						if (!canAdd) continue;
						MenuItem item = new MenuItem(g.getReference() + " - " + g.getLabel() + (g.hasType() ? " (" + g.getType() + ")" : ""), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
								LoadingWidget.getInstance().show(MESSAGES.pleaseWait());
								iSectioningService.changeStudentGroup(studentIds, g.getUniqueId(), false, new AsyncCallback<Boolean>() {
									@Override
									public void onFailure(Throwable caught) {
										LoadingWidget.getInstance().hide();
										UniTimeNotifications.error(caught);
									}

									@Override
									public void onSuccess(Boolean result) {
										for (int row = 0; row < iStudentTable.getRowCount(); row++) {
											StudentInfo i = iStudentTable.getData(row);
											if (i != null && i.getStudent() != null) {
												Widget w = iStudentTable.getWidget(row, 0);
												if (w instanceof CheckBox && ((CheckBox)w).getValue()) {
													i.getStudent().addGroup(g.getType(), g.getReference(), SafeHtmlUtils.htmlEscape(g.getLabel()));
													if (g.hasType()) {
														Integer col = iGroupsColumn.get(g.getType());
														if (col != null)
															((HTML)iStudentTable.getWidget(row, col)).setHTML(i.getStudent().getGroup(g.getType(), "<br>"));
													} else if (iGroupColumn >= 0)
														((HTML)iStudentTable.getWidget(row, iGroupColumn)).setHTML(i.getStudent().getGroup("<br>"));
												}
											}
										}
										LoadingWidget.getInstance().hide();
									}
								});
							}
						});
						menu.addItem(item);
					}
				}
			});
			hSelect.addOperation(new MenuOperation() {
				@Override
				public String getName() {
					return MESSAGES.opRemoveFromGroup();
				}
				@Override
				public boolean hasSeparator() {
					if (iOnline && iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.hasEditableGroups()) {
						for (int row = 0; row < iStudentTable.getRowCount(); row++) {
							StudentInfo i = iStudentTable.getData(row);
							if (i != null && i.getStudent() != null && iSelectedStudentIds.contains(i.getStudent().getId())) {
								for (StudentGroupInfo g: iProperties.getEditableGroups())
									if (!i.getStudent().hasGroup(g.getReference())) return false;
							}
						}
					}
					return true;
				}
				@Override
				public boolean isApplicable() {
					if (iOnline && iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.hasEditableGroups()) {
						for (int row = 0; row < iStudentTable.getRowCount(); row++) {
							StudentInfo i = iStudentTable.getData(row);
							if (i != null && i.getStudent() != null && iSelectedStudentIds.contains(i.getStudent().getId())) {
								for (StudentGroupInfo g: iProperties.getEditableGroups())
									if (i.getStudent().hasGroup(g.getReference())) return true;
							}
						}
					}
					return false;
				}
				@Override
				public void execute() {}
				@Override
				public void generate(final PopupPanel popup, MenuBar menu) {
					for (final StudentGroupInfo g: iProperties.getEditableGroups()) {
						boolean canDrop = false;
						for (int row = 0; row < iStudentTable.getRowCount(); row++) {
							StudentInfo i = iStudentTable.getData(row);
							if (i != null && i.getStudent() != null && iSelectedStudentIds.contains(i.getStudent().getId())) {
								if (i.getStudent().hasGroup(g.getReference())) {
									canDrop = true; break;
								}
							}
						}
						if (!canDrop) continue;
						MenuItem item = new MenuItem(g.getReference() + " - " + g.getLabel() + (g.hasType() ? " (" + g.getType() + ")" : ""), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
								LoadingWidget.getInstance().show(MESSAGES.pleaseWait());
								iSectioningService.changeStudentGroup(studentIds, g.getUniqueId(), true, new AsyncCallback<Boolean>() {
									@Override
									public void onFailure(Throwable caught) {
										LoadingWidget.getInstance().hide();
										UniTimeNotifications.error(caught);
									}

									@Override
									public void onSuccess(Boolean result) {
										for (int row = 0; row < iStudentTable.getRowCount(); row++) {
											StudentInfo i = iStudentTable.getData(row);
											if (i != null && i.getStudent() != null) {
												Widget w = iStudentTable.getWidget(row, 0);
												if (w instanceof CheckBox && ((CheckBox)w).getValue()) {
													i.getStudent().removeGroup(g.getType(), g.getReference());
													if (g.hasType()) {
														Integer col = iGroupsColumn.get(g.getType());
														if (col != null)
															((HTML)iStudentTable.getWidget(row, col)).setHTML(i.getStudent().getGroup(g.getType(), "<br>"));
													} else if (iGroupColumn >= 0)
														((HTML)iStudentTable.getWidget(row, iGroupColumn)).setHTML(i.getStudent().getGroup("<br>"));
												}
											}
										}
										LoadingWidget.getInstance().hide();
									}
								});
							}
						});
						menu.addItem(item);
					}
				}
			});
		}
		
		iStudentInfoVisibleColumns = new StudentsInfoVisibleColumns(result);
		
		UniTimeTableHeader hExtId = null;
		if (iStudentInfoVisibleColumns.hasExtId) {
			hExtId = new UniTimeTableHeader(MESSAGES.colStudentExternalId());
			header.add(hExtId);
			addSortOperation(hExtId, StudentComparator.SortBy.EXTERNAL_ID, MESSAGES.colStudentExternalId());
		}
		
		UniTimeTableHeader hStudent = new UniTimeTableHeader(MESSAGES.colStudent());
		header.add(hStudent);
		addSortOperation(hStudent, StudentComparator.SortBy.STUDENT, MESSAGES.colStudent());
		
		UniTimeTableHeader hTotal = new UniTimeTableHeader("&nbsp;");
		header.add(hTotal);
		
		if (iProperties != null && iProperties.isChangeStatus()) iStudentInfoVisibleColumns.hasNote = true;
		if (iProperties != null && iProperties.hasEditableGroups()) {
			iStudentInfoVisibleColumns.hasGroup = true;
			for (StudentGroupInfo g: iProperties.getEditableGroups())
				if (g.hasType())
					iStudentInfoVisibleColumns.groupTypes.add(g.getType());
		}
		
		UniTimeTableHeader hArea = null, hClasf = null;
		if (iStudentInfoVisibleColumns.hasArea) {
			hArea = new UniTimeTableHeader(MESSAGES.colArea());
			//hArea.setWidth("100px");
			header.add(hArea);
			addSortOperation(hArea, StudentComparator.SortBy.AREA, MESSAGES.colArea());
			
			hClasf = new UniTimeTableHeader(MESSAGES.colClassification());
			//hClasf.setWidth("100px");
			header.add(hClasf);
			addSortOperation(hClasf, StudentComparator.SortBy.CLASSIFICATION, MESSAGES.colClassification());
		}

		UniTimeTableHeader hMajor = null;
		if (iStudentInfoVisibleColumns.hasMajor) {
			hMajor = new UniTimeTableHeader(MESSAGES.colMajor());
			//hMajor.setWidth("100px");
			header.add(hMajor);
			addSortOperation(hMajor, StudentComparator.SortBy.MAJOR, MESSAGES.colMajor());
		}
		
		UniTimeTableHeader hGroup = null;
		if (iStudentInfoVisibleColumns.hasGroup) {
			iGroupColumn = header.size() - 1;
			hGroup = new UniTimeTableHeader(MESSAGES.colGroup());
			//hGroup.setWidth("100px");
			header.add(hGroup);
			addSortOperation(hGroup, StudentComparator.SortBy.GROUP, MESSAGES.colGroup());
		} else {
			iGroupColumn = -1;
		}
		
		iGroupsColumn.clear();
		Map<String, UniTimeTableHeader> hGroups = new HashMap<String, UniTimeTableHeader>();
		for (String type: iStudentInfoVisibleColumns.groupTypes) {
			iGroupsColumn.put(type, header.size() - 1);
			UniTimeTableHeader h = new UniTimeTableHeader(type);
			header.add(h);
			addSortOperation(h, StudentComparator.SortBy.GROUP, MESSAGES.colGroup(), type);
			hGroups.put(type, h);
		}
		
		UniTimeTableHeader hAcmd = null;
		if (iStudentInfoVisibleColumns.hasAcmd) {
			hAcmd = new UniTimeTableHeader(MESSAGES.colAccommodation());
			//hGroup.setWidth("100px");
			header.add(hAcmd);
			addSortOperation(hAcmd, StudentComparator.SortBy.ACCOMODATION, MESSAGES.colAccommodation());
		}
		
		iStatusColumn = header.size() - 1;
		UniTimeTableHeader hStatus = new UniTimeTableHeader(MESSAGES.colStatus());
		//hMajor.setWidth("100px");
		header.add(hStatus);
		addSortOperation(hStatus, StudentComparator.SortBy.STATUS, MESSAGES.colStatus());
		
		UniTimeTableHeader hEnrollment = null;
		if (iStudentInfoVisibleColumns.hasEnrollment) {
			hEnrollment = new UniTimeTableHeader(MESSAGES.colEnrollment());
			hEnrollment.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hEnrollment);
			addSortOperation(hEnrollment, StudentComparator.SortBy.ENROLLMENT, MESSAGES.colEnrollment());
		}
		
		UniTimeTableHeader hWaitlist = null;
		if (iStudentInfoVisibleColumns.hasWaitList) {
			hWaitlist = new UniTimeTableHeader(MESSAGES.colWaitListed());
			hWaitlist.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hWaitlist);
			addSortOperation(hWaitlist, StudentComparator.SortBy.WAITLIST, MESSAGES.colWaitListed());
		}
		
		UniTimeTableHeader hReservation = null;
		if (iStudentInfoVisibleColumns.hasReservation) {
			hReservation = new UniTimeTableHeader(MESSAGES.colReservation());
			hReservation.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hReservation);
			addSortOperation(hReservation, StudentComparator.SortBy.RESERVATION, MESSAGES.colReservation());
		}
		
		UniTimeTableHeader hConsent = null;
		if (iStudentInfoVisibleColumns.hasConsent) {
			hConsent = new UniTimeTableHeader(MESSAGES.colConsent());
			hConsent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hConsent);
			addSortOperation(hConsent, StudentComparator.SortBy.CONSENT, MESSAGES.colConsent());
		}
		
		UniTimeTableHeader hOverride = null;
		if (iStudentInfoVisibleColumns.hasOverride) {
			hOverride = new UniTimeTableHeader(MESSAGES.colPendingOverrides());
			hOverride.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hOverride);
			addSortOperation(hOverride, StudentComparator.SortBy.OVERRIDE, MESSAGES.colPendingOverrides().replace("<br>", " "));
		}
		
		UniTimeTableHeader hCredit = null;
		if (iStudentInfoVisibleColumns.hasCredit) {
			hCredit = new UniTimeTableHeader(MESSAGES.colEnrollCredit());
			hCredit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hCredit);
			addSortOperation(hCredit, StudentComparator.SortBy.CREDIT, MESSAGES.colEnrollCredit().replace("<br>", " "));
		}
		
		UniTimeTableHeader hReqCred = null;
		if (iStudentInfoVisibleColumns.hasReqCred) {
			hReqCred = new UniTimeTableHeader(MESSAGES.colRequestCredit());
			hReqCred.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hReqCred);
			addSortOperation(hReqCred, StudentComparator.SortBy.REQ_CREDIT, MESSAGES.colRequestCredit().replace("<br>", " "));
		}
		
		UniTimeTableHeader hDistConf = null;
		if (iStudentInfoVisibleColumns.hasDistances) {
			hDistConf = new UniTimeTableHeader(MESSAGES.colDistanceConflicts());
			hDistConf.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			header.add(hDistConf);
			addSortOperation(hDistConf, StudentComparator.SortBy.DIST_CONF, MESSAGES.colDistanceConflicts().replace("<br>", " "));
		}
		
		UniTimeTableHeader hShare = null;
		if (iStudentInfoVisibleColumns.hasOverlaps) {
			hShare = new UniTimeTableHeader(MESSAGES.colOverlapMins());
			hShare.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hShare);
			addSortOperation(hShare, StudentComparator.SortBy.OVERLAPS, MESSAGES.colOverlapMins());
		}
		
		UniTimeTableHeader hFTShare = null;
		if (iStudentInfoVisibleColumns.hasFreeTimeOverlaps) {
			hFTShare = new UniTimeTableHeader(MESSAGES.colFreeTimeOverlapMins());
			hFTShare.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hFTShare);
			addSortOperation(hFTShare, StudentComparator.SortBy.FT_OVERLAPS, MESSAGES.colFreeTimeOverlapMins());
		}
		
		UniTimeTableHeader hPrefIMConfs = null;
		if (iStudentInfoVisibleColumns.hasPrefIMConfs) {
			hPrefIMConfs = new UniTimeTableHeader(MESSAGES.colPrefInstrMethConfs());
			hPrefIMConfs.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hPrefIMConfs);
			addSortOperation(hPrefIMConfs, StudentComparator.SortBy.PREF_IM, MESSAGES.colPrefInstrMethConfs().replace("<br>", " "));
		}
		
		UniTimeTableHeader hPrefSecConfs = null;
		if (iStudentInfoVisibleColumns.hasPrefSecConfs) {
			hPrefSecConfs = new UniTimeTableHeader(MESSAGES.colPrefSectionConfs());
			hPrefSecConfs.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hPrefSecConfs);
			addSortOperation(hPrefSecConfs, StudentComparator.SortBy.PREF_SEC, MESSAGES.colPrefSectionConfs().replace("<br>", " "));
		}
		
		UniTimeTableHeader hRequestTS = null;
		if (iStudentInfoVisibleColumns.hasRequestedDate) {
			hRequestTS = new UniTimeTableHeader(MESSAGES.colRequestTimeStamp());
			header.add(hRequestTS);
			addSortOperation(hRequestTS, StudentComparator.SortBy.REQUEST_TS, MESSAGES.colRequestTimeStamp());
		}
		
		UniTimeTableHeader hEnrolledTS = null;
		if (iStudentInfoVisibleColumns.hasEnrolledDate) {
			hEnrolledTS = new UniTimeTableHeader(MESSAGES.colEnrollmentTimeStamp());
			header.add(hEnrolledTS);
			addSortOperation(hEnrolledTS, StudentComparator.SortBy.ENROLLMENT_TS, MESSAGES.colEnrollmentTimeStamp());
		}
		
		UniTimeTableHeader hNote = null;
		if (iOnline && iStudentInfoVisibleColumns.hasNote) {
			iNoteColumn = header.size() - 1;
			hNote = new UniTimeTableHeader(MESSAGES.colStudentNote());
			header.add(hNote);
			addSortOperation(hNote, StudentComparator.SortBy.NOTE, MESSAGES.colStudentNote());
		} else {
			iNoteColumn = -1;
		}
		
		UniTimeTableHeader hEmailTS = null;
		if (iOnline && iStudentInfoVisibleColumns.hasEmailed) {
			hEmailTS = new UniTimeTableHeader(MESSAGES.colEmailTimeStamp());
			header.add(hEmailTS);
			addSortOperation(hEmailTS, StudentComparator.SortBy.EMAIL_TS, MESSAGES.colEmailTimeStamp());
		}
		
		iStudentTable.addRow(null, header);
		
		if (SectioningStatusCookie.getInstance().getSortBy(iOnline, 1) != 0) {
			boolean asc = (SectioningStatusCookie.getInstance().getSortBy(iOnline, 1) > 0);
			String g = SectioningStatusCookie.getInstance().getSortByGroup(iOnline);
			StudentComparator.SortBy sort = StudentComparator.SortBy.values()[Math.abs(SectioningStatusCookie.getInstance().getSortBy(iOnline, 1)) - 1];
			UniTimeTableHeader h = null;
			switch (sort) {
			case ACCOMODATION: h = hAcmd; break;
			case AREA: h = hArea; break;
			case CLASSIFICATION: h = hClasf; break;
			case CONSENT: h = hConsent; break;
			case CREDIT: h = hCredit; break;
			case EMAIL_TS: h = hEmailTS; break;
			case ENROLLMENT: h = hEnrollment; break;
			case ENROLLMENT_TS: h = hEnrolledTS; break;
			case EXTERNAL_ID: h = hExtId; break;
			case GROUP:
				if (g == null || g.isEmpty())
					h = hGroup;
				else
					h = hGroups.get(g);
				break;
			case MAJOR: h = hMajor; break;
			case REQUEST_TS: h = hRequestTS; break;
			case RESERVATION: h = hReservation; break;
			case STATUS: h = hStatus; break;
			case STUDENT: h = hStudent; break;
			case WAITLIST: h = hWaitlist; break;
			case NOTE: h = hNote; break;
			case DIST_CONF: h = hDistConf; break;
			case OVERLAPS: h = hShare; break;
			case FT_OVERLAPS: h = hFTShare; break;
			case PREF_IM: h = hPrefIMConfs; break;
			case PREF_SEC: h = hPrefSecConfs; break;
			case OVERRIDE: h = hOverride; break;
			case REQ_CREDIT: h = hReqCred; break;
			}
			if (h != null) {
				Collections.sort(result, new StudentComparator(sort, asc, g));
				if (!asc) Collections.reverse(result);
				h.setOrder(asc);
			}
		}
		
		Set<Long> newlySelected = new HashSet<Long>();
		for (StudentInfo info: result) {
			if (info.getStudent() != null && info.getStudent().isCanSelect() && iSelectedStudentIds.contains(info.getStudent().getId()))
				newlySelected.add(info.getStudent().getId());
		}
		iSelectedStudentIds.clear();
		iSelectedStudentIds.addAll(newlySelected);
		
		iStudentTableHint.setVisible(iStudentInfoVisibleColumns.hasWaitList);
		
		fillStudentTable(0);
	}
	
	private void fillStudentTable(int firstLine) {
		iStudentInfosFirstLine = firstLine;
		if (iStudentTable.getRowCount() > 0) iStudentTable.clearTable(1);
		for (int line = iStudentInfosFirstLine; line < iStudentInfos.size() - 1 && (iMaxTableLines <= 0 || line < iStudentInfosFirstLine + iMaxTableLines); line++) {
			addStudentTableLine(iStudentInfos.get(line));
		}
		if (!iStudentInfos.isEmpty())
			addStudentTableLine(iStudentInfos.get(iStudentInfos.size() - 1));
		if (iStudentTable.getRowCount() >= 2) {
			for (int c = 0; c < iStudentTable.getCellCount(iStudentTable.getRowCount() - 1); c++)
				iStudentTable.getCellFormatter().setStyleName(iStudentTable.getRowCount() - 1, c, "unitime-TotalRow");
		}
		iRange.setText(MESSAGES.pageRange(iStudentInfosFirstLine + 1, Math.min(iStudentInfos.size() - 1, iStudentInfosFirstLine + iMaxTableLines)));
		
		iPaginationButtons.setVisible(iStudentInfos.size() - 1 > iMaxTableLines && iMaxTableLines > 0);
		iPrevious.setEnabled(iMaxTableLines > 0 && iStudentInfosFirstLine >= iMaxTableLines);
		iNext.setEnabled(iMaxTableLines > 0 && iStudentInfosFirstLine + iMaxTableLines < iStudentInfos.size() - 1);
	}
	
	private void addStudentTableLine(StudentInfo info) {
		List<Widget> line = new ArrayList<Widget>();
		if (info.getStudent() != null) {
			if (iOnline && iProperties != null && iProperties.isCanSelectStudent()) {
				if (info.getStudent().isCanSelect()) {
					CheckBox ch = new CheckBox();
					ch.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							event.stopPropagation();
						}
					});
					final Long sid = info.getStudent().getId();
					if (iSelectedStudentIds.contains(sid)) {
						ch.setValue(true);
					}
					ch.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							event.stopPropagation();
						}
					});
					ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							if (event.getValue())
								iSelectedStudentIds.add(sid);
							else
								iSelectedStudentIds.remove(sid);
						}
					});
					line.add(ch);
				} else {
					line.add(new Label(""));
				}
			}
			if (iStudentInfoVisibleColumns.hasExtId) {
				line.add(new Label(info.getStudent().isCanShowExternalId() ? info.getStudent().getExternalId() : "", false));
			}
			line.add(new TitleCell(info.getStudent().getName()));
			if (iStudentInfoVisibleColumns.hasArea) {
				line.add(new HTML(info.getStudent().getArea("<br>"), false));
				line.add(new HTML(info.getStudent().getClassification("<br>"), false));
			}
			if (iStudentInfoVisibleColumns.hasMajor)
				line.add(new HTML(info.getStudent().getMajor("<br>"), false));
			if (iStudentInfoVisibleColumns.hasGroup)
				line.add(new HTML(info.getStudent().getGroup("<br>"), false));
			for (String type: iStudentInfoVisibleColumns.groupTypes)
				line.add(new HTML(info.getStudent().getGroup(type, "<br>"), false));
			if (iStudentInfoVisibleColumns.hasAcmd)
				line.add(new HTML(info.getStudent().getAccommodation("<br>"), false));
			line.add(new HTML(info.getStatus(), false));
		} else {
			if (iOnline && iProperties != null && iProperties.isCanSelectStudent()) line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasExtId)
				line.add(new TitleCell(MESSAGES.total()));
			else
				line.add(new Label(MESSAGES.total()));
			line.add(new NumberCell(null, iStudentInfos.size() - 1));
			if (iStudentInfoVisibleColumns.hasArea) {
				line.add(new HTML("&nbsp;", false));
				line.add(new HTML("&nbsp;", false));
			}
			if (iStudentInfoVisibleColumns.hasMajor)
				line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasGroup)
				line.add(new HTML("&nbsp;", false));
			for (@SuppressWarnings("unused") String type: iStudentInfoVisibleColumns.groupTypes)
				line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasAcmd)
				line.add(new HTML("&nbsp;", false));
			line.add(new HTML("&nbsp;", false));
		}
		if (iStudentInfoVisibleColumns.hasEnrollment)
			line.add(new NumberCell(info.getEnrollment(), info.getTotalEnrollment()));
		if (iStudentInfoVisibleColumns.hasWaitList)
			line.add(new WaitListCell(info));
		if (iStudentInfoVisibleColumns.hasReservation)
			line.add(new NumberCell(info.getReservation(), info.getTotalReservation()));
		if (iStudentInfoVisibleColumns.hasConsent)
			line.add(new NumberCell(info.getConsentNeeded(), info.getTotalConsentNeeded()));
		if (iStudentInfoVisibleColumns.hasOverride)
			line.add(new NumberCell(info.getOverrideNeeded(), info.getTotalOverrideNeeded()));
		if (iStudentInfoVisibleColumns.hasCredit)
			line.add(new CreditCell(info));
		if (iStudentInfoVisibleColumns.hasReqCred)
			line.add(new RequestCreditCell(info.getRequestCreditMin(), info.getRequestCreditMax(), info.getTotalRequestCreditMin(), info.getTotalRequestCreditMax()));
		if (iStudentInfoVisibleColumns.hasDistances) {
			line.add(new DistanceCell(info.getNrDistanceConflicts(), info.getTotalNrDistanceConflicts(), info.getLongestDistanceMinutes(), info.getTotalLongestDistanceMinutes()));
		}
		if (iStudentInfoVisibleColumns.hasOverlaps)
			line.add(new NumberCell(info.getOverlappingMinutes(), info.getTotalOverlappingMinutes()));
		if (iStudentInfoVisibleColumns.hasFreeTimeOverlaps)
			line.add(new NumberCell(info.getFreeTimeOverlappingMins(), info.getTotalFreeTimeOverlappingMins()));
		if (iStudentInfoVisibleColumns.hasPrefIMConfs)
			line.add(new NumberCell(info.getPrefInstrMethConflict(), info.getTotalPrefInstrMethConflict()));
		if (iStudentInfoVisibleColumns.hasPrefSecConfs)
			line.add(new NumberCell(info.getPrefSectionConflict(), info.getTotalPrefSectionConflict()));
		if (info.getStudent() != null) {
			if (iStudentInfoVisibleColumns.hasRequestedDate)
				line.add(new HTML(info.getRequestedDate() == null ? "&nbsp;" : sDF.format(info.getRequestedDate()), false));
			if (iStudentInfoVisibleColumns.hasEnrolledDate)
				line.add(new HTML(info.getEnrolledDate() == null ? "&nbsp;" : sDF.format(info.getEnrolledDate()), false));
			if (iOnline && iStudentInfoVisibleColumns.hasNote) {
				HTML note = new HTML(info.hasNote() ? info.getNote() : ""); note.addStyleName("student-note");
				if (info.hasNote())
					note.setTitle(note.getText());
				line.add(note);
			}
			if (iOnline && iStudentInfoVisibleColumns.hasEmailed)
				line.add(new HTML(info.getEmailDate() == null ? "&nbsp;" : sDF.format(info.getEmailDate()), false));
		} else {
			if (iStudentInfoVisibleColumns.hasRequestedDate)
				line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasEnrolledDate)
				line.add(new HTML("&nbsp;", false));
			if (iOnline && iStudentInfoVisibleColumns.hasNote)
				line.add(new HTML("&nbsp;", false));
			if (iOnline && iStudentInfoVisibleColumns.hasEmailed)
				line.add(new HTML("&nbsp;", false));
		}
		iStudentTable.addRow(info, line);
	}
	
	public void populateChangeLog(List<SectioningAction> result) {
		iSectioningActions = result; iSectioningActionsFirstLine = 0;
		iSortOperations.clear();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		
		iSectioningActionsVisibleColumns = new SectioningActionsVisibleColumns(result);
		
		UniTimeTableHeader hExtId = null;
		if (iSectioningActionsVisibleColumns.hasExtId) {
			hExtId = new UniTimeTableHeader(MESSAGES.colStudentExternalId());
			header.add(hExtId);
			addSortOperation(hExtId, ChangeLogComparator.SortBy.EXTERNAL_ID, MESSAGES.colStudentExternalId());
		}
		
		UniTimeTableHeader hStudent = new UniTimeTableHeader(MESSAGES.colStudent());
		header.add(hStudent);
		addSortOperation(hStudent, ChangeLogComparator.SortBy.STUDENT, MESSAGES.colStudent());
		
		UniTimeTableHeader hOp = new UniTimeTableHeader(MESSAGES.colOperation());
		header.add(hOp);
		addSortOperation(hOp, ChangeLogComparator.SortBy.OPERATION, MESSAGES.colOperation());
		
		UniTimeTableHeader hTimeStamp = new UniTimeTableHeader(MESSAGES.colTimeStamp());
		header.add(hTimeStamp);
		addSortOperation(hTimeStamp, ChangeLogComparator.SortBy.TIME_STAMP, MESSAGES.colTimeStamp());
		
		UniTimeTableHeader hExecTime = new UniTimeTableHeader(MESSAGES.colExecutionTime());
		header.add(hExecTime);
		addSortOperation(hExecTime, ChangeLogComparator.SortBy.EXEC_TIME, MESSAGES.colExecutionTime());
		
		UniTimeTableHeader hResult = new UniTimeTableHeader(MESSAGES.colResult());
		header.add(hResult);
		addSortOperation(hResult, ChangeLogComparator.SortBy.RESULT, MESSAGES.colResult());
		
		UniTimeTableHeader hUser = new UniTimeTableHeader(MESSAGES.colUser());
		header.add(hUser);
		addSortOperation(hUser, ChangeLogComparator.SortBy.USER, MESSAGES.colUser());
		
		final UniTimeTableHeader hMessage = new UniTimeTableHeader(MESSAGES.colMessage());
		header.add(hMessage);
		addSortOperation(hMessage, ChangeLogComparator.SortBy.MESSAGE, MESSAGES.colMessage());
		
		iLogTable.addRow(null, header);
		
		if (SectioningStatusCookie.getInstance().getSortBy(iOnline, 2) != 0) {
			boolean asc = (SectioningStatusCookie.getInstance().getSortBy(iOnline, 2) > 0);
			ChangeLogComparator.SortBy sort = ChangeLogComparator.SortBy.values()[Math.abs(SectioningStatusCookie.getInstance().getSortBy(iOnline, 2)) - 1];
			UniTimeTableHeader h = null;
			switch (sort) {
			case EXTERNAL_ID: h = hExtId; break;
			case MESSAGE: h = hMessage; break;
			case OPERATION: h = hOp; break;
			case RESULT: h = hResult; break;
			case STUDENT: h = hStudent; break;
			case TIME_STAMP: h = hTimeStamp; break;
			case USER: h = hUser; break;
			}
			if (h != null) {
				Collections.sort(result, new ChangeLogComparator(sort));
				if (!asc) Collections.reverse(result);
				h.setOrder(asc);
			}
		}
		
		fillLogTable(0);
	}
	
	private void fillLogTable(int firstLine) {
		iSectioningActionsFirstLine = firstLine;
		if (iLogTable.getRowCount() > 0) iLogTable.clearTable(1);
		for (int line = iSectioningActionsFirstLine; line < iSectioningActions.size() && (iMaxTableLines <= 0 || line < iSectioningActionsFirstLine + iMaxTableLines); line++) {
			addLogTableLine(iSectioningActions.get(line));
		}
		iRange.setText(MESSAGES.pageRange(iSectioningActionsFirstLine + 1, Math.min(iSectioningActions.size(), iSectioningActionsFirstLine + iMaxTableLines)));
		
		iPaginationButtons.setVisible(iSectioningActions.size() > iMaxTableLines && iMaxTableLines > 0);
		iPrevious.setEnabled(iMaxTableLines > 0 && iSectioningActionsFirstLine >= iMaxTableLines);
		iNext.setEnabled(iMaxTableLines > 0 && iSectioningActionsFirstLine + iMaxTableLines < iSectioningActions.size());
	}
	
	private void addLogTableLine(SectioningAction log) {
		if (iSectioningActionsVisibleColumns.hasExtId) {
			iLogTable.addRow(log,
					new TopCell(log.getStudent().isCanShowExternalId() ? log.getStudent().getExternalId() : ""),
					new TopCell(log.getStudent().getName()),
					new TopCell(log.getOperation()),
					new TopCell(sTSF.format(log.getTimeStamp())),
					new TopCell(log.getWallTime() == null ? "" : sNF.format(0.001 * log.getWallTime())),
					new TopCell(log.getResult()),
					new TopCell(log.getUser() == null ? "" : log.getUser()),
					new HTML(log.getMessage() == null ? "" : log.getMessage())
			);
		} else {
			iLogTable.addRow(log,
					new TopCell(log.getStudent().getName()),
					new TopCell(log.getOperation()),
					new TopCell(sTSF.format(log.getTimeStamp())),
					new TopCell(log.getWallTime() == null ? "" : sNF.format(0.001 * log.getWallTime())),
					new TopCell(log.getResult()),
					new TopCell(log.getUser() == null ? "" : log.getUser()),
					new HTML(log.getMessage() == null ? "" : log.getMessage())
			);
		}
	}
	
	protected void addSortOperation(final UniTimeTableHeader header, final EnrollmentComparator.SortBy sort, final String column) {
		Operation op = new SortOperation() {
			@Override
			public void execute() {
				boolean asc = (header.getOrder() == null ? true : !header.getOrder());
				if (iMaxTableLines > 0 && iEnrollmentInfos.size() > iMaxTableLines) {
					Collections.sort(iEnrollmentInfos, new EnrollmentComparator(sort, asc));
					if (!asc) Collections.reverse(iEnrollmentInfos);
					fillCourseTable(iEnrollmentInfosFirstLine);
					for (int i = 0; i < iCourseTable.getCellCount(0); i++) {
						Widget w = iCourseTable.getWidget(0, i);
						if (w != null && w instanceof UniTimeTableHeader) {
							UniTimeTableHeader h = (UniTimeTableHeader)w;
							h.setOrder(null);
						}
					}
					header.setOrder(asc);
				} else {
					iCourseTable.sort(header, new EnrollmentComparator(sort, asc));
				}
				SectioningStatusCookie.getInstance().setSortBy(iOnline, 0, header.getOrder() ? 1 + sort.ordinal() : -1 - sort.ordinal());
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
				return MESSAGES.sortBy(column);
			}
			@Override
			public String getColumnName() {
				return column;
			}
		};
		header.addOperation(op);
		iSortOperations.add(op);
	}
	
	protected void addSortOperation(final UniTimeTableHeader header, final StudentComparator.SortBy sort, final String column) {
		addSortOperation(header, sort, column, "");
	}
	
	protected void addSortOperation(final UniTimeTableHeader header, final StudentComparator.SortBy sort, final String column, final String group) {
		Operation op = new SortOperation() {
			@Override
			public void execute() {
				boolean asc = (header.getOrder() == null ? true : !header.getOrder());
				if (iMaxTableLines > 0 && iStudentInfos.size() - 1 > iMaxTableLines) {
					Collections.sort(iStudentInfos, new StudentComparator(sort, asc, group));
					if (!asc) Collections.reverse(iStudentInfos);
					fillStudentTable(iStudentInfosFirstLine);
					for (int i = 0; i < iStudentTable.getCellCount(0); i++) {
						Widget w = iStudentTable.getWidget(0, i);
						if (w != null && w instanceof UniTimeTableHeader) {
							UniTimeTableHeader h = (UniTimeTableHeader)w;
							h.setOrder(null);
						}
					}
					header.setOrder(asc);
				} else {
					iStudentTable.sort(header, new StudentComparator(sort, asc, group));
				}
				SectioningStatusCookie.getInstance().setSortBy(iOnline, 1, header.getOrder() ? 1 + sort.ordinal() : -1 - sort.ordinal(), group);
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
				if (sort == StudentComparator.SortBy.GROUP && group != null && !group.isEmpty())
					return MESSAGES.sortBy(group);	
				return MESSAGES.sortBy(column);
			}
			@Override
			public String getColumnName() {
				return column;
			}
		};
		header.addOperation(op);
		iSortOperations.add(op);
	}
	
	public static class SimpleSuggestion implements Suggestion {
		private String iDisplay, iReplace;

		public SimpleSuggestion(String display, String replace) {
			iDisplay = display;
			iReplace = replace;
		}
		
		public SimpleSuggestion(String replace) {
			this(replace, replace);
		}

		public String getDisplayString() {
			return iDisplay;
		}

		public String getReplacementString() {
			return iReplace;
		}
	}
	
	protected void addSortOperation(final UniTimeTableHeader header, final ChangeLogComparator.SortBy sort, final String column) {
		Operation op = new SortOperation() {
			@Override
			public void execute() {
				boolean asc = (header.getOrder() == null ? true : !header.getOrder());
				if (iMaxTableLines > 0 && iSectioningActions.size() > iMaxTableLines) {
					Collections.sort(iSectioningActions, new ChangeLogComparator(sort));
					if (!asc) Collections.reverse(iSectioningActions);
					fillLogTable(iSectioningActionsFirstLine);
					for (int i = 0; i < iLogTable.getCellCount(0); i++) {
						Widget w = iLogTable.getWidget(0, i);
						if (w != null && w instanceof UniTimeTableHeader) {
							UniTimeTableHeader h = (UniTimeTableHeader)w;
							h.setOrder(null);
						}
					}
					header.setOrder(asc);
				} else {
					iLogTable.sort(header, new ChangeLogComparator(sort));
				}
				SectioningStatusCookie.getInstance().setSortBy(iOnline, 2, header.getOrder() ? 1 + sort.ordinal() : -1 - sort.ordinal());
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
				return MESSAGES.sortBy(column);
			}
			@Override
			public String getColumnName() {
				return column;
			}
		};
		header.addOperation(op);
		iSortOperations.add(op);
	}
	
	private static interface SortOperation extends Operation, HasColumnName {}
	
	public class SuggestCallback implements AsyncCallback<List<String[]>> {
		private Request iRequest;
		private Callback iCallback;
		
		public SuggestCallback(Request request, Callback callback) {
			iRequest = request;
			iCallback = callback;
		}
		
		public void onFailure(Throwable caught) {
			ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			// suggestions.add(new SimpleSuggestion("<font color='red'>"+caught.getMessage()+"</font>", ""));
			iCallback.onSuggestionsReady(iRequest, new Response(suggestions));
			ToolBox.checkAccess(caught);
		}

		public void onSuccess(List<String[]> result) {
			ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			for (String[] suggestion: result) {
				suggestions.add(new SimpleSuggestion(suggestion[1], suggestion[0]));
			}
			iCallback.onSuggestionsReady(iRequest, new Response(suggestions));
		}
		
	}
	
	public static class NumberCell extends HTML implements HasCellAlignment {
		
		public NumberCell(Integer value, Integer total) {
			super();
			if (value == null) {
				if (total != null)
					setHTML(total == 0 ? "-" : total < 0 ? "&infin;" : total.toString());
			} else {
				if (value.equals(total))
					setHTML(total == 0 ? "-" : total < 0 ? "&infin;" : total.toString());
				else
					setHTML((value < 0 ? "&infin;" : value.toString()) + " / " + (total < 0 ? "&infin;" : total.toString()));
			}
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class TitleCell extends HTML implements HasColSpan {
		
		public TitleCell(String title) {
			super(title);
		}

		@Override
		public int getColSpan() {
			return 2;
		}
	}
	
	public static class AvailableCell extends HTML implements HasCellAlignment {
		public AvailableCell(EnrollmentInfo e) {
			super();
			int other = (e.getOther() == null ? 0 : e.getOther());
			if (e.getLimit() == null) {
				setHTML("-");
				setTitle(MESSAGES.availableNoLimit());
			} else if (e.getLimit() < 0) {
				if (e.getAvailable() != null && e.getAvailable() == 0) {
					setHTML("&infin;" + MESSAGES.htmlReservationSign());
					setTitle(MESSAGES.availableUnlimitedWithReservation());
				} else {
					setHTML("&infin;");
					setTitle(MESSAGES.availableUnlimited());
				}
			} else {
				if (e.getAvailable() == e.getLimit() - e.getTotalEnrollment() - other) {
					setHTML(e.getAvailable() + " / " + e.getLimit());
					if (e.getAvailable() == 0)
						setTitle(MESSAGES.availableNot(e.getLimit()));
					else
						setTitle(MESSAGES.available(e.getAvailable(), e.getLimit()));
				} else if (e.getAvailable() == 0 && e.getLimit() > e.getTotalEnrollment() + other) {
					setHTML((e.getLimit() - e.getTotalEnrollment() - other) + MESSAGES.htmlReservationSign() + " / " + e.getLimit());
					setTitle(MESSAGES.availableWithReservation(e.getLimit() - e.getTotalEnrollment() - other, e.getLimit()));
				} else {
					setHTML(e.getAvailable() + " + " + (e.getLimit() - e.getTotalEnrollment() - e.getAvailable() - other) + MESSAGES.htmlReservationSign() + " / " + e.getLimit());
					setTitle(MESSAGES.availableSomeReservation(e.getAvailable(), e.getLimit(), e.getLimit() - e.getTotalEnrollment() - e.getAvailable() - other));
				}
			}
		}
		
		public static boolean hasReservedSpace(EnrollmentInfo e) {
			if (e.getLimit() < 0) return false;
			if (e.getLimit() < 0) {
				return e.getAvailable() == 0;
			} else {
				return e.getAvailable() != e.getLimit() - e.getTotalEnrollment() - (e.getOther() == null ? 0 : e.getOther());
			}
			
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class WaitListCell extends HTML implements HasCellAlignment {
		public WaitListCell(int wait, int tWait, int unasg, int tUnasg, Integer topWaitingPriority) {
			super();
			if (tWait == 0 || tWait == tUnasg) {
				// no wait-list or all wait-listed
				if (unasg == tUnasg) {
					setHTML(unasg == 0 ? "-" : String.valueOf(unasg));
				} else {
					setHTML(unasg + " / " + tUnasg);
				}
				if (tWait > 0)
					setHTML(getHTML() + MESSAGES.htmlWaitListSign());
			} else {
				if (wait == tWait && unasg == tUnasg) {
					setHTML(wait == 0 ? String.valueOf(unasg) : wait == unasg ? wait + MESSAGES.htmlWaitListSign() : (unasg - wait) + " + " + wait + MESSAGES.htmlWaitListSign());
				} else {
					setHTML((wait == 0 ? String.valueOf(unasg) : wait == unasg ? wait + MESSAGES.htmlWaitListSign() : (unasg - wait) + " + " + wait + MESSAGES.htmlWaitListSign())
							+ " / " + tUnasg);
				}
			}
			if (topWaitingPriority != null)
				setHTML(getHTML() + " " + MESSAGES.firstWaitListedPrioritySign(topWaitingPriority));
		}
			
		public WaitListCell(StudentInfo e) {
			this(e.hasWaitlist() ? e.getWaitlist() : 0,
				e.hasTotalWaitlist() ? e.getTotalWaitlist() : 0,
				e.hasUnassigned() ? e.getUnassigned() : 0,
				e.hasTotalUnassigned() ? e.getTotalUnassigned() : 0,
				e.getTopWaitingPriority());
		}
		
		public WaitListCell(EnrollmentInfo e) {
			this(e.hasWaitlist() ? e.getWaitlist() : 0,
				e.hasTotalWaitlist() ? e.getTotalWaitlist() : 0,
				e.hasUnassigned() ? e.getUnassignedPrimary() : 0,
				e.hasTotalUnassigned() ? e.getTotalUnassignedPrimary() : 0,
				null);
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class EnrollmentComparator implements Comparator<EnrollmentInfo> {
		public enum SortBy {
			SUBJECT,
			COURSE,
			TITLE,
			CONSENT,
			LIMIT,
			PROJECTION,
			ENROLLMENT,
			WAITLIST,
			RESERVATION,
			NEED_CONSENT,
			ALTERNATIVES,
			NEED_OVERRIDE,
		}
		
		private SortBy iSortBy;
		private boolean iAsc;
		
		public EnrollmentComparator(SortBy sortBy, boolean asc) {
			iSortBy = sortBy;
			iAsc = asc;
		}

		@Override
		public int compare(EnrollmentInfo e1, EnrollmentInfo e2) {
			// Totals line is always last
			if (e1.getCourseId() == null) return (iAsc ? 1 : -1);
			if (e2.getCourseId() == null) return (iAsc ? -1 : 1);
			
			if (e1.getCourseId().equals(e2.getCourseId())) { // Same course
				// Course line first
				if (e1.getConfigId() == null) return (iAsc ? -1 : 1);
				if (e2.getConfigId() == null) return (iAsc ? 1 : -1);
				return compareClasses(e1, e2);
			} else { // Different course
				return compareCourses(e1, e2);
			}
		}
		
		private int compareClasses(EnrollmentInfo e1, EnrollmentInfo e2) {
			return 0;
		}
	
		private int compareCourses(EnrollmentInfo e1, EnrollmentInfo e2) {
			int cmp;
			switch (iSortBy) {
			case SUBJECT:
				break;
			case COURSE:
				cmp = e1.getCourseNbr().compareTo(e2.getCourseNbr());
				if (cmp != 0) return cmp;
				break;
			case TITLE:
				cmp = (e1.getTitle() == null ? "" : e1.getTitle()).compareTo(e2.getTitle() == null ? "" : e2.getTitle());
				if (cmp != 0) return cmp;
				break;
			case CONSENT:
				cmp = (e1.getConsent() == null ? "" : e1.getConsent()).compareTo(e2.getConsent() == null ? "" : e2.getConsent());
				if (cmp != 0) return cmp;
				break;
			case LIMIT:
				cmp = (e1.getAvailable() == null ? new Integer(0) : e1.getAvailable() < 0 ? new Integer(Integer.MAX_VALUE) : e1.getAvailable()).compareTo(
						e2.getAvailable() == null ? 0 : e2.getAvailable() < 0 ? Integer.MAX_VALUE : e2.getAvailable());
				if (cmp != 0) return cmp;
				cmp = (e1.getLimit() == null ? new Integer(0) : e1.getLimit()).compareTo(e2.getLimit() == null ? 0 : e2.getLimit());
				if (cmp != 0) return cmp;
				break;
			case PROJECTION:
				cmp = (e1.getProjection() == null ? new Integer(0) : e1.getProjection()).compareTo(e2.getProjection() == null ? 0 : e2.getProjection());
				if (cmp != 0) return - cmp;
				break;
			case ENROLLMENT:
				cmp = (e1.getEnrollment() == null ? new Integer(0) : e1.getEnrollment()).compareTo(e2.getEnrollment() == null ? 0 : e2.getEnrollment());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalEnrollment() == null ? new Integer(0) : e1.getTotalEnrollment()).compareTo(e2.getTotalEnrollment() == null ? 0 : e2.getTotalEnrollment());
				if (cmp != 0) return - cmp;
				break;
			case WAITLIST:
				cmp = (e1.getWaitlist() == null ? new Integer(0) : e1.getWaitlist()).compareTo(e2.getWaitlist() == null ? 0 : e2.getWaitlist());
				if (cmp != 0) return - cmp;
				cmp = (e1.getUnassignedPrimary() == null ? new Integer(0) : e1.getUnassignedPrimary()).compareTo(e2.getUnassignedPrimary() == null ? 0 : e2.getUnassignedPrimary());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalWaitlist() == null ? new Integer(0) : e1.getTotalWaitlist()).compareTo(e2.getTotalWaitlist() == null ? 0 : e2.getTotalWaitlist());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalUnassignedPrimary() == null ? new Integer(0) : e1.getTotalUnassignedPrimary()).compareTo(e2.getTotalUnassignedPrimary() == null ? 0 : e2.getTotalUnassignedPrimary());
				if (cmp != 0) return - cmp;
				break;
			case RESERVATION:
				cmp = (e1.getReservation() == null ? new Integer(0) : e1.getReservation()).compareTo(e2.getReservation() == null ? 0 : e2.getReservation());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalReservation() == null ? new Integer(0) : e1.getTotalReservation()).compareTo(e2.getTotalReservation() == null ? 0 : e2.getTotalReservation());
				if (cmp != 0) return - cmp;
				break;
			case NEED_CONSENT:
				cmp = (e1.getConsentNeeded() == null ? new Integer(0) : new Integer(e1.getConsentNeeded())).compareTo(e2.getConsentNeeded() == null ? 0 : e2.getConsentNeeded());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalConsentNeeded() == null ? new Integer(0) : new Integer(e1.getTotalConsentNeeded())).compareTo(e2.getTotalConsentNeeded() == null ? 0 : e2.getTotalConsentNeeded());
				if (cmp != 0) return - cmp;
				break;
			case ALTERNATIVES:
				cmp = (e1.getUnassignedAlternative() == null ? new Integer(0) : e1.getUnassignedAlternative()).compareTo(e2.getUnassignedAlternative() == null ? 0 : e2.getUnassignedAlternative());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalUnassignedAlternative() == null ? new Integer(0) : e1.getTotalUnassignedAlternative()).compareTo(e2.getTotalUnassignedAlternative() == null ? 0 : e2.getTotalUnassignedAlternative());
				if (cmp != 0) return - cmp;
				break;
			case NEED_OVERRIDE:
				cmp = (e1.getOverrideNeeded() == null ? new Integer(0) : new Integer(e1.getOverrideNeeded())).compareTo(e2.getOverrideNeeded() == null ? 0 : e2.getOverrideNeeded());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalOverrideNeeded() == null ? new Integer(0) : new Integer(e1.getTotalOverrideNeeded())).compareTo(e2.getTotalOverrideNeeded() == null ? 0 : e2.getTotalOverrideNeeded());
				if (cmp != 0) return - cmp;
				break;
			}
			
			// Default sort
			cmp = e1.getSubject().compareTo(e2.getSubject());
			if (cmp != 0) return cmp;
			
			cmp = e1.getCourseNbr().compareTo(e2.getCourseNbr());
			if (cmp != 0) return cmp;

			return 0;
		}

	}
	
	public static class StudentComparator implements Comparator<StudentInfo> {
		public enum SortBy {
			EXTERNAL_ID,
			STUDENT,
			AREA,
			CLASSIFICATION,
			MAJOR,
			GROUP,
			ACCOMODATION,
			STATUS,
			ENROLLMENT,
			WAITLIST,
			RESERVATION,
			CONSENT,
			CREDIT,
			REQUEST_TS,
			ENROLLMENT_TS,
			EMAIL_TS,
			NOTE,
			DIST_CONF, OVERLAPS,
			FT_OVERLAPS, PREF_IM, PREF_SEC,
			OVERRIDE,
			REQ_CREDIT,
			;
		}
		
		private SortBy iSortBy;
		private boolean iAsc;
		private String iGroup;
		
		public StudentComparator(SortBy sortBy, boolean asc, String group) {
			iSortBy = sortBy;
			iAsc = asc;
			iGroup = group;
		}
		
		protected int doCompare(StudentInfo e1, StudentInfo e2) {
			int cmp;
			switch (iSortBy) {
			case EXTERNAL_ID:
				return (e1.getStudent().isCanShowExternalId() ? e1.getStudent().getExternalId() : "").compareTo(e2.getStudent().isCanShowExternalId() ? e2.getStudent().getExternalId() : "");
			case STUDENT:
				return e1.getStudent().getName().compareTo(e2.getStudent().getName());
			case AREA:
				cmp = e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
				if (cmp != 0) return cmp;
				return e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
			case CLASSIFICATION:
				cmp = e1.getStudent().getClassification("|").compareTo(e2.getStudent().getClassification("|"));
				if (cmp != 0) return cmp;
				cmp = e1.getStudent().getArea("|").compareTo(e2.getStudent().getArea("|"));
				if (cmp != 0) return cmp;
				return e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
			case MAJOR:
				cmp = e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
				if (cmp != 0) return cmp;
				return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
			case GROUP:
				cmp = e1.getStudent().getGroup(iGroup, "|").compareTo(e2.getStudent().getGroup(iGroup, "|"));
				if (cmp != 0) return cmp;
				return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
			case ACCOMODATION:
				cmp = e1.getStudent().getAccommodation("|").compareTo(e2.getStudent().getAccommodation("|"));
				if (cmp != 0) return cmp;
				return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
			case STATUS:
				return (e1.getStatus() == null ? "" : e1.getStatus()).compareToIgnoreCase(e2.getStatus() == null ? "" : e2.getStatus());
			case ENROLLMENT:
				cmp = (e1.getEnrollment() == null ? new Integer(0) : e1.getEnrollment()).compareTo(e2.getEnrollment() == null ? 0 : e2.getEnrollment());
				if (cmp != 0) return - cmp;
				return (e1.getTotalEnrollment() == null ? new Integer(0) : e1.getTotalEnrollment()).compareTo(e2.getTotalEnrollment() == null ? 0 : e2.getTotalEnrollment());
			case WAITLIST:
				cmp = (e1.getUnassigned() == null ? new Integer(0) : e1.getUnassigned()).compareTo(e2.getUnassigned() == null ? 0 : e2.getUnassigned());
				if (cmp != 0) return - cmp;
				cmp = (e1.getWaitlist() == null ? new Integer(0) : e1.getWaitlist()).compareTo(e2.getWaitlist() == null ? 0 : e2.getWaitlist());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalUnassigned() == null ? new Integer(0) : e1.getTotalUnassigned()).compareTo(e2.getTotalUnassigned() == null ? 0 : e2.getTotalUnassigned());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalWaitlist() == null ? new Integer(0) : e1.getTotalWaitlist()).compareTo(e2.getTotalWaitlist() == null ? 0 : e2.getTotalWaitlist());
				if (cmp != 0) return - cmp;
				return (e1.getTopWaitingPriority() == null ? new Integer(Integer.MAX_VALUE) : e1.getTopWaitingPriority()).compareTo(e2.getTopWaitingPriority() == null ? Integer.MAX_VALUE : e2.getTopWaitingPriority());
			case RESERVATION:
				cmp = (e1.getReservation() == null ? new Integer(0) : e1.getReservation()).compareTo(e2.getReservation() == null ? 0 : e2.getReservation());
				if (cmp != 0) return - cmp;
				return (e1.getTotalReservation() == null ? new Integer(0) : e1.getTotalReservation()).compareTo(e2.getTotalReservation() == null ? 0 : e2.getTotalReservation());
			case CONSENT:
				cmp = (e1.getConsentNeeded() == null ? new Integer(0) : e1.getConsentNeeded()).compareTo(e2.getConsentNeeded() == null ? 0 : e2.getConsentNeeded());
				if (cmp != 0) return - cmp;
				return (e1.getTotalConsentNeeded() == null ? new Integer(0) : e1.getTotalConsentNeeded()).compareTo(e2.getTotalConsentNeeded() == null ? 0 : e2.getTotalConsentNeeded());
			case CREDIT:
				cmp = (e1.hasCredit() ? e1.getCredit() : new Float(0f)).compareTo(e2.hasCredit() ? e2.getCredit() : new Float(0f));
				if (cmp != 0) return - cmp;
				return (e1.hasTotalCredit() ? e1.getTotalCredit() : new Float(0f)).compareTo(e2.hasTotalCredit() ? e2.getTotalCredit() : new Float(0f));
			case REQ_CREDIT:
				cmp = (e1.hasRequestCredit() ? new Float(e1.getRequestCreditMin()) : new Float(0f)).compareTo(e2.hasRequestCredit() ? e2.getRequestCreditMin() : 0f);
				if (cmp != 0) return - cmp;
				cmp = (e1.hasRequestCredit() ? new Float(e1.getRequestCreditMax()) : new Float(0f)).compareTo(e2.hasRequestCredit() ? e2.getRequestCreditMax() : 0f);
				if (cmp != 0) return - cmp;
				cmp = (e1.hasTotalRequestCredit() ? new Float(e1.getTotalRequestCreditMin()) : new Float(0f)).compareTo(e2.hasTotalRequestCredit() ? e2.getTotalRequestCreditMin() : 0f);
				if (cmp != 0) return - cmp;
				return (e1.hasTotalRequestCredit() ? new Float(e1.getTotalRequestCreditMax()) : new Float(0f)).compareTo(e2.hasTotalRequestCredit() ? e2.getTotalRequestCreditMax() : 0f);
			case REQUEST_TS:
				return (e1.getRequestedDate() == null ? new Date(0) : e1.getRequestedDate()).compareTo(e2.getRequestedDate() == null ? new Date(0) : e2.getRequestedDate());
			case ENROLLMENT_TS:
				return (e1.getEnrolledDate() == null ? new Date(0) : e1.getEnrolledDate()).compareTo(e2.getEnrolledDate() == null ? new Date(0) : e2.getEnrolledDate());
			case EMAIL_TS:
				return (e1.getEmailDate() == null ? new Date(0) : e1.getEmailDate()).compareTo(e2.getEmailDate() == null ? new Date(0) : e2.getEmailDate());
			case NOTE:
				return (e1.hasNote() ? e1.getNote().compareTo(e2.hasNote() ? e2.getNote() : "") : "".compareTo(e2.hasNote() ? e2.getNote() : ""));
			case DIST_CONF:
				cmp = (e1.hasDistanceConflicts() ? e1.getNrDistanceConflicts() : new Integer(0)).compareTo(e2.hasDistanceConflicts() ? e2.getNrDistanceConflicts() : new Integer(0));
				if (cmp != 0) return - cmp;
				cmp = (e1.hasTotalDistanceConflicts() ? e1.getTotalNrDistanceConflicts() : new Integer(0)).compareTo(e2.hasTotalDistanceConflicts() ? e2.getTotalNrDistanceConflicts() : new Integer(0));
				if (cmp != 0) return - cmp;
				return - (e1.hasDistanceConflicts() ? e1.getLongestDistanceMinutes() : e1.hasTotalDistanceConflicts() ? e1.getTotalLongestDistanceMinutes() : new Integer(0)).compareTo(
						e2.hasDistanceConflicts() ? e2.getLongestDistanceMinutes() : e2.hasTotalDistanceConflicts() ? e2.getTotalLongestDistanceMinutes() : new Integer(0));
			case OVERLAPS:
				cmp = (e1.hasOverlappingMinutes() ? e1.getOverlappingMinutes() : new Integer(0)).compareTo(e2.hasOverlappingMinutes() ? e2.getOverlappingMinutes() : new Integer(0));
				if (cmp != 0) return - cmp;
				return (e1.hasTotalOverlappingMinutes() ? e1.getTotalOverlappingMinutes() : new Integer(0)).compareTo(e2.hasTotalOverlappingMinutes() ? e2.getTotalOverlappingMinutes() : new Integer(0));
			case FT_OVERLAPS:
				cmp = (e1.hasFreeTimeOverlappingMins() ? e1.getFreeTimeOverlappingMins() : new Integer(0)).compareTo(e2.hasFreeTimeOverlappingMins() ? e2.getFreeTimeOverlappingMins() : new Integer(0));
				if (cmp != 0) return - cmp;
				return (e1.hasTotalFreeTimeOverlappingMins() ? e1.getTotalFreeTimeOverlappingMins() : new Integer(0)).compareTo(e2.hasTotalFreeTimeOverlappingMins() ? e2.getTotalFreeTimeOverlappingMins() : new Integer(0));
			case PREF_IM:
				cmp = (e1.hasTotalPrefInstrMethConflict() ? new Integer(e1.getTotalPrefInstrMethConflict() - e1.getPrefInstrMethConflict()) : new Integer(0)).compareTo(e2.hasTotalPrefInstrMethConflict() ? new Integer(e2.getTotalPrefInstrMethConflict() - e2.getPrefInstrMethConflict()) : new Integer(0));
				if (cmp != 0) return - cmp;
				return - (e1.hasTotalPrefInstrMethConflict() ? e1.getTotalPrefInstrMethConflict() : new Integer(0)).compareTo(e2.hasTotalPrefInstrMethConflict() ? e2.getTotalPrefInstrMethConflict() : new Integer(0));
			case PREF_SEC:
				cmp = (e1.hasTotalPrefSectionConflict() ? new Integer(e1.getTotalPrefSectionConflict() - e1.getPrefSectionConflict()) : new Integer(0)).compareTo(e2.hasTotalPrefSectionConflict() ? new Integer(e2.getTotalPrefSectionConflict() - e2.getPrefSectionConflict()) : new Integer(0));
				if (cmp != 0) return - cmp;
				return -(e1.hasTotalPrefSectionConflict() ? e1.getTotalPrefSectionConflict() : new Integer(0)).compareTo(e2.hasTotalPrefSectionConflict() ? e2.getTotalPrefSectionConflict() : new Integer(0));
			case OVERRIDE:
				cmp = (e1.getOverrideNeeded() == null ? new Integer(0) : e1.getOverrideNeeded()).compareTo(e2.getOverrideNeeded() == null ? 0 : e2.getOverrideNeeded());
				if (cmp != 0) return - cmp;
				return (e1.getTotalOverrideNeeded() == null ? new Integer(0) : e1.getTotalOverrideNeeded()).compareTo(e2.getTotalOverrideNeeded() == null ? 0 : e2.getTotalOverrideNeeded());
			default:
				return 0;
			}
		}

		@Override
		public int compare(StudentInfo e1, StudentInfo e2) {
			if (e1.getStudent() == null) return (iAsc ? 1 : -1);
			if (e2.getStudent() == null) return (iAsc ? -1 : 1);
			int cmp = doCompare(e1, e2);
			if (cmp != 0) return cmp;
			cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
			if (cmp != 0) return cmp;
			return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
		}
	}
	
	public static class ChangeLogComparator implements Comparator<SectioningAction> {
		public enum SortBy {
			STUDENT,
			OPERATION,
			TIME_STAMP,
			RESULT,
			USER,
			MESSAGE,
			EXEC_TIME,
			EXTERNAL_ID,
			;
		}
		
		private SortBy iSortBy;
		
		public ChangeLogComparator(SortBy sortBy) {
			iSortBy = sortBy;
		}
		
		public int doCompare(SectioningAction e1, SectioningAction e2) {
			switch (iSortBy) {
			case STUDENT:
				return e1.getStudent().getName().compareTo(e2.getStudent().getName());
			case OPERATION:
				return e1.getOperation().compareTo(e2.getOperation());
			case TIME_STAMP:
				return - e1.getTimeStamp().compareTo(e2.getTimeStamp());
			case EXEC_TIME:
				return - (e1.getWallTime() == null ? new Long(0) : e1.getWallTime()).compareTo(e2.getWallTime() == null ? 0 : e2.getWallTime());
			case RESULT:
				return (e1.getResult() == null ? "" : e1.getResult()).compareTo(e2.getResult() == null ? "" : e2.getResult());
			case MESSAGE:
				return (e1.getMessage() == null ? "" : e1.getMessage()).compareTo(e2.getMessage() == null ? "" : e2.getMessage());
			case EXTERNAL_ID:
				return (e1.getStudent().isCanShowExternalId() ? e1.getStudent().getExternalId() : "").compareTo(e2.getStudent().isCanShowExternalId() ? e2.getStudent().getExternalId() : "");
			default:
				return 0;
			}
		}

		@Override
		public int compare(SectioningAction e1, SectioningAction e2) {
			int cmp = doCompare(e1, e2);
			if (cmp != 0) return cmp;
			return - e1.getTimeStamp().compareTo(e2.getTimeStamp());
		}
	}
	
	private void sendEmail(final Iterator<Long> studentIds, final String subject, final String message, final String cc, final int fails) {
		if (!studentIds.hasNext()) return;
		final Long studentId = studentIds.next();
		iSectioningService.sendEmail(studentId, subject, message, cc, new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
				for (int row = 0; row < iStudentTable.getRowCount(); row++) {
					StudentInfo i = iStudentTable.getData(row);
					if (i != null && i.getStudent() != null && studentId.equals(i.getStudent().getId())) {
						HTML error = new HTML(caught.getMessage());
						error.setStyleName("unitime-ErrorMessage");
						iStudentTable.setWidget(row, iStudentTable.getCellCount(row) - 1, error);
						i.setEmailDate(null);
					}
				}
				if (fails >= 4) {
					while (studentIds.hasNext()) {
						Long sid = studentIds.next();
						for (int row = 0; row < iStudentTable.getRowCount(); row++) {
							StudentInfo i = iStudentTable.getData(row);
							if (i != null && i.getStudent() != null && sid.equals(i.getStudent().getId())) {
								HTML error = new HTML(MESSAGES.exceptionCancelled(caught.getMessage()));
								error.setStyleName("unitime-ErrorMessage");
								iStudentTable.setWidget(row, iStudentTable.getCellCount(row) - 1, error);
								i.setEmailDate(null);
							}
						}
					}
				}
				sendEmail(studentIds, subject, message, cc, fails + 1);
			}

			@Override
			public void onSuccess(Boolean result) {
				for (int row = 0; row < iStudentTable.getRowCount(); row++) {
					StudentInfo i = iStudentTable.getData(row);
					if (i != null && i.getStudent() != null && studentId.equals(i.getStudent().getId())) {
						if (result) {
							i.setEmailDate(new Date());
							iStudentTable.setWidget(row, iStudentTable.getCellCount(row) - 1, new HTML(sDF.format(i.getEmailDate()), false));
						} else {
							HTML error = new HTML(MESSAGES.exceptionNoEmail());
							error.setStyleName("unitime-ErrorMessage");
							iStudentTable.setWidget(row, iStudentTable.getCellCount(row) - 1, error);
							i.setEmailDate(null);
						}
					}
					sendEmail(studentIds, subject, message, cc, fails);
				}
			}
		});
	}
	
	public static class CreditCell extends HTML implements HasCellAlignment {
		private static NumberFormat df = NumberFormat.getFormat("0.#");
		
		public CreditCell(StudentInfo info) {
			super();
			Float value = info.getCredit();
			Float total = info.getTotalCredit();
			if (total != null && total > 0f) {
				if (total.equals(value)) {
					String html = df.format(total);
					if (info.hasIMTotalCredit()) {
						html += " (";
						for (Iterator<String> i = info.getTotalCreditIMs().iterator(); i.hasNext();) {
							String im = i.next();
							html += im + ": " + df.format(info.getIMTotalCredit(im));
							if (i.hasNext()) html += ", ";
						}
						html += ")";
					}
					setHTML(html);
				} else {
					String html = df.format(value) + " / " + df.format(total);
					if (info.hasIMCredit()) {
						html += " (";
						for (Iterator<String> i = info.getCreditIMs().iterator(); i.hasNext();) {
							String im = i.next();
							html += im + ": " + df.format(info.getIMCredit(im));
							if (i.hasNext()) html += ", ";
						}
						html += ")";
					}
					setHTML(html);
				}
			} else {
				setHTML("&nbsp;");
			}
			setWordWrap(false);
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class RequestCreditCell extends HTML implements HasCellAlignment {
		private static NumberFormat df = NumberFormat.getFormat("0.#");
		
		public RequestCreditCell(float min, float max, float totalMin, float totalMax) {
			super();
			if (totalMax > 0f) {
				if (min == totalMin && max == totalMax) {
					setHTML(totalMin == totalMax ? df.format(totalMax) : df.format(totalMin) + " - " + df.format(totalMax));
				} else {
					setHTML((min == max ? df.format(min) : df.format(min) + " - " + df.format(max)) + " / " +
							(totalMin == totalMax ? df.format(totalMin) : df.format(totalMin) + " - " + df.format(totalMax)));
				}
			} else {
				setHTML("&nbsp;");
			}
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class RoomsCell extends P {
		public RoomsCell(List<IdValue> list, String delimiter) {
			super("itemize");
			if (list != null)
				for (Iterator<IdValue> i = list.iterator(); i.hasNext(); ) {
					final P p = new P(DOM.createSpan(), "item");
					final IdValue room = i.next();
					p.setText(room.getValue() + (i.hasNext() ? delimiter : ""));
					if (room.getId() != null) {
						p.addMouseOverHandler(new MouseOverHandler() {
							@Override
							public void onMouseOver(MouseOverEvent event) {
								RoomHint.showHint(p.getElement(), room.getId(), null, null, true);
							}
						});
						p.addMouseOutHandler(new MouseOutHandler() {
							@Override
							public void onMouseOut(MouseOutEvent event) {
								RoomHint.hideHint();
							}
						});
					}
					add(p);
				}
		}	
	}
	
	public static class DistanceCell extends HTML implements HasCellAlignment {
		
		public DistanceCell(Integer nrDist, Integer totalNrDist, Integer distMin, Integer totalDistMin) {
			super();
			if (nrDist == null) {
				if (totalNrDist != null)
					setHTML(totalNrDist == 0 ? "-" : totalNrDist + " " + MESSAGES.distanceConflict(totalDistMin));
			} else {
				if (nrDist.equals(totalNrDist))
					setHTML(totalNrDist == 0 ? "-" : totalNrDist + " " + MESSAGES.distanceConflict(totalDistMin));
				else
					setHTML(nrDist + " / " + totalNrDist + " " + (nrDist == 0 ? MESSAGES.distanceConflict(totalDistMin) : MESSAGES.distanceConflict(distMin)));
			}
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_CENTER;
		}
	}
	
	static class StudentsInfoVisibleColumns {
		boolean hasEnrollment = false, hasWaitList = false,  hasArea = false, hasMajor = false, hasGroup = false, hasAcmd = false, hasReservation = false,
				hasRequestedDate = false, hasEnrolledDate = false, hasConsent = false, hasCredit = false, hasReqCred = false, hasDistances = false, hasOverlaps = false,
				hasFreeTimeOverlaps = false, hasPrefIMConfs = false, hasPrefSecConfs = false, hasNote = false, hasEmailed = false, hasOverride = false, hasExtId = false;
		int selectableStudents = 0;
		Set<String> groupTypes = new TreeSet<String>();
		
		public StudentsInfoVisibleColumns(List<StudentInfo> result) {
			for (StudentInfo e: result) {
				if (e.getStudent() == null) continue;
				if (e.getStudent().isCanSelect()) selectableStudents++;
				// if (e.getStatus() != null) hasStatus = true;
				if (e.getTotalEnrollment() != null && e.getTotalEnrollment() > 0) hasEnrollment = true;
				if (e.getTotalUnassigned() != null && e.getTotalUnassigned() > 0) hasWaitList = true;
				if (e.getStudent().hasArea()) hasArea = true;
				if (e.getStudent().hasMajor()) hasMajor = true;
				if (e.getStudent().hasGroup()) hasGroup = true;
				if (e.getStudent().hasAccommodation()) hasAcmd = true;
				if (e.getTotalReservation() != null && e.getTotalReservation() > 0) hasReservation = true;
				if (e.getRequestedDate() != null) hasRequestedDate = true;
				if (e.getEnrolledDate() != null) hasEnrolledDate = true;
				// if (e.getEmailDate() != null) hasEmailDate = true;
				if (e.getTotalConsentNeeded() != null && e.getTotalConsentNeeded() > 0) hasConsent = true;
				if (e.getTotalOverrideNeeded() != null && e.getTotalOverrideNeeded() > 0) hasOverride = true;
				if (e.hasTotalCredit()) hasCredit = true;
				if (e.hasTotalRequestCredit()) hasReqCred = true;
				if (e.hasTotalDistanceConflicts()) hasDistances = true;
				if (e.hasOverlappingMinutes()) hasOverlaps = true;
				if (e.hasFreeTimeOverlappingMins()) hasFreeTimeOverlaps = true;
				if (e.hasTotalPrefInstrMethConflict()) hasPrefIMConfs = true;
				if (e.hasTotalPrefSectionConflict()) hasPrefSecConfs = true;
				if (e.hasNote()) hasNote = true;
				if (e.getEmailDate() != null) hasEmailed = true;
				if (e.getStudent() != null && e.getStudent().isCanShowExternalId()) hasExtId = true;
				if (e.getStudent().hasGroups()) groupTypes.addAll(e.getStudent().getGroupTypes());
			}
		}
	}
	
	static class SectioningActionsVisibleColumns {
		boolean hasExtId = false;
		
		public SectioningActionsVisibleColumns(List<SectioningAction> result) {
			for (SectioningAction e: result) {
				if (e.getStudent() != null && e.getStudent().isCanShowExternalId()) { hasExtId = true; break; }
			}
		}
	}
}
