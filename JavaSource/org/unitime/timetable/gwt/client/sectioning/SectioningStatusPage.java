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
import java.util.Collection;
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

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaTabBar;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.reservations.ReservationTable;
import org.unitime.timetable.gwt.client.reservations.ReservationTable.ReservationColumn;
import org.unitime.timetable.gwt.client.rooms.RoomHint;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable.TopCell;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasColSpan;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasStyleChanges;
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
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.AdvisedInfoInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.EnrollmentInfo;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Group;
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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
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
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.TakesValue;
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
	private int iStatusColumn = 0, iNoteColumn = 0, iGroupColumn = 0, iPinColumn = -1;
	private Map<String, Integer> iGroupsColumn = new HashMap<String, Integer>();
	private Set<Long> iSelectedStudentIds = new HashSet<Long>();
	private Set<Long> iSelectedCourseIds = new HashSet<Long>();
	private boolean iOnline; 
	
	private List<Operation> iSortOperations = new ArrayList<Operation>();
	private List<HideOperation> iHideOperations = new ArrayList<HideOperation>();
	
	private List<StudentInfo> iStudentInfos = null;
	private StudentsInfoVisibleColumns iStudentInfoVisibleColumns = null;
	private CourseInfoVisibleColums iCourseInfoVisibleColums = null;
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
				
				if (!iHideOperations.isEmpty()) {
					MenuBar submenu = new MenuBar(true);
					boolean hasHiddenColumn = false;
					for (final HideOperation op: iHideOperations) {
						String name = op.getColumnName();
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
						if (!op.isColumnVisible())
							hasHiddenColumn = true;
					}
					MenuItem columns = new MenuItem(GWT_MESSAGES.opColumns(), submenu);
					columns.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(columns);
					
					if (hasHiddenColumn) {
						submenu.addSeparator();
						submenu.addItem(new MenuItem(MESSAGES.opShowAllColumns(), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								for (HideOperation op: iHideOperations)
									op.showColumn();
							}
						}));
					}
				}
				
				if (iExport.isVisible()) {
					MenuItem item = new MenuItem(UniTimeHeaderPanel.stripAccessKey(GWT_MESSAGES.buttonExportXLS()), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							exportData("xls");
						}
					});
					menu.addItem(item);
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
				exportData("csv");
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
				final Long id = (event.getData().getConfigId() == null || event.getData().getConfigId() < 0l ? event.getData().getOfferingId() : -event.getData().getClazzId());
				iError.setVisible(false);
				if (event.getData().getConfigId() == null || event.getData().getConfigId() < 0l)
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
									if (event.getData().getConfigId() == null || event.getData().getConfigId() < 0l)
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
							if (event.getData().getConfigId() == null || event.getData().getConfigId() < 0l)
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
			public void onMouseClick(final TableEvent<SectioningAction> event) {
				if (event.getData() != null) {
					LoadingWidget.getInstance().show(MESSAGES.loadingChangeLogMessage());
					iSectioningService.getChangeLogMessage(event.getData().getLogId(), new AsyncCallback<String>() {
						@Override
						public void onSuccess(String message) {
							LoadingWidget.getInstance().hide();
							final HTML widget = new HTML(message);
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
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught);
						}
					});
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
				iEnrollmentTable.setEmail(!iOnline && iProperties.isEmail());
				iEnrollmentTable.setAdvisorRecommendations(iProperties.isAdvisorCourseRequests());
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
				iStudentStatusDialog = new StudentStatusDialog(iStates, new StudentStatusDialog.StudentStatusConfirmation() {
					@Override
					public boolean isAllMyStudents() {
						if (iSelectedStudentIds.size() <= 1) return true;
						for (int row = 0; row < iStudentTable.getRowCount(); row++) {
							StudentInfo i = iStudentTable.getData(row);
							if (i != null && i.getStudent() != null) {
								Widget w = iStudentTable.getWidget(row, 0);
								if (w instanceof CheckBox && ((CheckBox)w).getValue()) {
									if (!i.isMyStudent()) return false; 
								}
							}
						}
						return true;
					}
					
					@Override
					public int getStudentCount() {
						return iSelectedStudentIds.size();
					}
				});
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
	
	private void exportData(String format) {
		int tab = iTabIndex;
		if (tab < 0)
			tab = SectioningStatusCookie.getInstance().getTab(iOnline);
		
		String query = "output=student-dashboard." + format + "&online=" + (iOnline ? 1 : 0) + "&tab=" + tab + "&sort=" + SectioningStatusCookie.getInstance().getSortBy(iOnline, tab);
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
									if (e.getConfigId() == -1l)
										iCourseTable.getRowFormatter().getElement(r).addClassName("crosslist-line");
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
											if (e.getConfigId() == -1l)
												iCourseTable.getRowFormatter().getElement(r).addClassName("crosslist-line");
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
		} else if (e.getConfigId() == -1l) {
			line.add(new Label());
			line.add(new HTML(e.getSubject(), false));
			if (e.isControl() != null && e.isControl().booleanValue())
				line.get(line.size() - 1).getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
			line.get(line.size() - 1).getElement().getStyle().setPaddingLeft(5, Unit.PX);
			line.add(new Label(e.getCourseNbr(), false));
			if (e.isControl() != null && e.isControl().booleanValue())
				line.get(line.size() - 1).getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
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
		if (iCourseInfoVisibleColums.hasSnapshot)
			line.add(new NumberCell(null, e.getSnapshot()));
		line.add(new EnrollmentCell(e));
		line.add(new WaitListCell(e));
		line.add(new NumberCell(e.getUnassignedAlternative(), e.getTotalUnassignedAlternative()));
		line.add(new NumberCell(e.getReservation(), e.getTotalReservation()));
		line.add(new NumberCell(e.getConsentNeeded(), e.getTotalConsentNeeded()));
		line.add(new NumberCell(e.getOverrideNeeded(), e.getTotalOverrideNeeded()));
		if (Boolean.TRUE.equals(e.isNoMatch())) {
			for (Widget w : line)
				if (w != null) w.addStyleName("nomatch");
		}
		return line;
	}
	
	public void populateCourseTable(List<EnrollmentInfo> result) {
		iEnrollmentInfos = result; iEnrollmentInfosFirstLine = 0;
		iClassInfos.clear();
		iSelectedCourseIds.clear();
		iSortOperations.clear();
		iHideOperations.clear();
		iCourseInfoVisibleColums = new CourseInfoVisibleColums(result);
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
		addHideOperation(0, hStart, MESSAGES.colDate());

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
		
		UniTimeTableHeader hSnapshot = null;
		if (iCourseInfoVisibleColums.hasSnapshot) {
			hSnapshot = new UniTimeTableHeader(MESSAGES.colSnapshotLimit());
			hSnapshot.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hSnapshot);
			addSortOperation(hSnapshot, EnrollmentComparator.SortBy.LIMIT, MESSAGES.colSnapshotLimit().replace("<br>", " "));
		}

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
			case SNAPSHOT: h = hSnapshot; break;
			}
			if (h != null) {
				Collections.sort(result, new EnrollmentComparator(sort, asc));
				if (!asc) Collections.reverse(result);
				h.setOrder(asc);
			}
		}
		
		iCourseTableHint.setVisible(iCourseInfoVisibleColums.hasReservation);
		
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
					for (EnrollmentInfo c: classes) {
						iCourseTable.addRow(c, line(c));
						if (c.getConfigId() == -1l)
							iCourseTable.getRowFormatter().getElement(iCourseTable.getRowCount() - 1).addClassName("crosslist-line");
					}
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
		for (HideOperation op: iHideOperations)
			op.fixColumnVisibility();
	}
	
	public void populateStudentTable(List<StudentInfo> result) {
		iStudentInfos = result; iStudentInfosFirstLine = 0;
		iSortOperations.clear();
		iHideOperations.clear();
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
							sendEmail(studentIds.iterator(), iStudentStatusDialog.getSubject(), iStudentStatusDialog.getMessage(), iStudentStatusDialog.getCC(), 0,
									iStudentStatusDialog.getIncludeCourseRequests(), iStudentStatusDialog.getIncludeClassSchedule(), iStudentStatusDialog.getIncludeAdvisorRequests(),
									iStudentStatusDialog.isOptionalEmailToggle());
						}
					}, (iProperties == null ? null : iProperties.getEmailOptionalToggleCaption()), (iProperties == null ? false : iProperties.getEmailOptionalToggleDefault()));
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
							UniTimeConfirmationDialog.confirmFocusNo(MESSAGES.massCancelConfirmation(), new Command() {
								@Override
								public void execute() {
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
				}
			});
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.reloadStudent();
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isReloadStudent();
				}
				@Override
				public void execute() {
					List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
					LoadingWidget.getInstance().show(MESSAGES.reloadingStudent());
					iSectioningService.reloadStudent(studentIds, new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught);
						}

						@Override
						public void onSuccess(Boolean result) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.info(MESSAGES.reloadStudentSuccess());
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
					return !iProperties.isReloadStudent();
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
					return !iProperties.isRequestUpdate() && !iProperties.isReloadStudent();
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
					return !iProperties.isRequestUpdate() && !iProperties.isCheckStudentOverrides() && !iProperties.isReloadStudent();
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
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.validateReCheckCriticalCourses();
				}
				@Override
				public boolean hasSeparator() {
					return !iProperties.isRequestUpdate() && !iProperties.isCheckStudentOverrides() && !iProperties.isValidateStudentOverrides() && !iProperties.isReloadStudent();
				}
				@Override
				public boolean isApplicable() {
					return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isRecheckCriticalCourses();
				}
				@Override
				public void execute() {
					List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
					LoadingWidget.getInstance().show(MESSAGES.recheckingCriticalCourses());
					iSectioningService.recheckCriticalCourses(studentIds, new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught);
						}

						@Override
						public void onSuccess(Boolean result) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.info(MESSAGES.recheckCriticalCoursesSuccess());
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
						private void changeStatus() {
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
												i.setStatus(info);
												((HTML)iStudentTable.getWidget(row, iStatusColumn)).setHTML(info.getReference());
											}
										}
									}
									LoadingWidget.getInstance().hide();
								}
							});
						}
						@Override
						public void execute() {
							boolean allMine = true;
							for (int row = 0; row < iStudentTable.getRowCount(); row++) {
								StudentInfo i = iStudentTable.getData(row);
								if (i != null && i.getStudent() != null) {
									Widget w = iStudentTable.getWidget(row, 0);
									if (w instanceof CheckBox && ((CheckBox)w).getValue()) {
										if (!i.isMyStudent()) { allMine = false; break; } 
									}
								}
							}
							if (!allMine) {
								UniTimeConfirmationDialog.confirmFocusNo(MESSAGES.confirmStatusChange(info.getLabel(), iSelectedStudentIds.size()), new Command() {
									@Override
									public void execute() {
										changeStatus();
									}
								});
							} else {
								changeStatus();
							}
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
												i.setStatus(iStudentStatusDialog.getStudentStatusInfo(statusRef));
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
							final StudentStatusInfo status = iStudentStatusDialog.getStudentStatusInfo(statusRef);
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
													i.setStatus(status);
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
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.releaseStudentPin();
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					if (iSelectedStudentIds.isEmpty() || iProperties == null || !iProperties.isReleasePins()) return false;
					if (iStudentInfos != null)
						for (StudentInfo info: iStudentInfos) {
							if (info == null || info.getStudent() == null) continue;
							if (iSelectedStudentIds.contains(info.getStudent().getId())) {
								if (!info.hasPinReleased() && (info.hasPin() || iProperties.isRetrievePins()))
									return true;
							}
						}
					return false;
				}
				@Override
				public void execute() {
					LoadingWidget.getInstance().show(MESSAGES.releasingStudentPins());
					List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
					iSectioningService.releasePins(studentIds, true, new AsyncCallback<Map<Long,String>>() {
						
						@Override
						public void onSuccess(Map<Long, String> result) {
							if (iStudentInfos != null)
								for (StudentInfo info: iStudentInfos) {
									if (info == null || info.getStudent() == null) continue;
									String pin = result.get(info.getStudent().getId());
									if (pin != null) {
										info.setPin(pin);
										info.setPinReleased(true);
									}
								}
							if (iPinColumn >= 0)
								for (int row = 0; row < iStudentTable.getRowCount(); row++) {
									StudentInfo i = iStudentTable.getData(row);
									if (i != null && i.getStudent() != null) {
										String pin = result.get(i.getStudent().getId());
										if (pin != null)
											((Label)iStudentTable.getWidget(row, iPinColumn)).setText(pin);
									}
								}
							LoadingWidget.getInstance().hide();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught);
							
						}
					});
				}
			});
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.suppressStudentPin();
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					if (iSelectedStudentIds.isEmpty() || iProperties == null || !iProperties.isReleasePins()) return false;
					if (iStudentInfos != null)
						for (StudentInfo info: iStudentInfos) {
							if (info == null || info.getStudent() == null) continue;
							if (iSelectedStudentIds.contains(info.getStudent().getId())) {
								if (info.hasPinReleased())
									return true;
							}
						}
					return false;
				}
				@Override
				public void execute() {
					LoadingWidget.getInstance().show(MESSAGES.supressingStudentPins());
					List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
					iSectioningService.releasePins(studentIds, false, new AsyncCallback<Map<Long,String>>() {
						
						@Override
						public void onSuccess(Map<Long, String> result) {
							if (iStudentInfos != null)
								for (StudentInfo info: iStudentInfos) {
									if (info == null || info.getStudent() == null) continue;
									String pin = result.get(info.getStudent().getId());
									if (pin != null) {
										info.setPin(pin);
										info.setPinReleased(false);
									}
								}
							if (iPinColumn >= 0)
								for (int row = 0; row < iStudentTable.getRowCount(); row++) {
									StudentInfo i = iStudentTable.getData(row);
									if (i != null && i.getStudent() != null) {
										String pin = result.get(i.getStudent().getId());
										if (pin != null)
											((Label)iStudentTable.getWidget(row, iPinColumn)).setText("");
									}
								}
							LoadingWidget.getInstance().hide();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught);
							
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
													i.getStudent().addGroup(g.getType(), g.getReference(), g.getLabel());
													if (g.hasType()) {
														Integer col = iGroupsColumn.get(g.getType());
														if (col != null)
															((Groups)iStudentTable.getWidget(row, col)).setValue(i.getStudent().getGroups(g.getType()));
													} else if (iGroupColumn >= 0)
														((Groups)iStudentTable.getWidget(row, iGroupColumn)).setValue(i.getStudent().getGroups());
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
															((Groups)iStudentTable.getWidget(row, col)).setValue(i.getStudent().getGroups(g.getType()));
													} else if (iGroupColumn >= 0)
														((Groups)iStudentTable.getWidget(row, iGroupColumn)).setValue(i.getStudent().getGroups());
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
		
		UniTimeTableHeader hCampus = null;
		if (iStudentInfoVisibleColumns.hasCamp) {
			hCampus = new UniTimeTableHeader(MESSAGES.colCampus());
			//hMajor.setWidth("100px");
			header.add(hCampus);
			addSortOperation(hCampus, StudentComparator.SortBy.CAMPUS, MESSAGES.colCampus());
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
		
		UniTimeTableHeader hDegree = null;
		if (iStudentInfoVisibleColumns.hasDeg) {
			hDegree = new UniTimeTableHeader(MESSAGES.colDegree());
			//hMajor.setWidth("100px");
			header.add(hDegree);
			addSortOperation(hDegree, StudentComparator.SortBy.DEGREE, MESSAGES.colDegree());
		}
		
		UniTimeTableHeader hProgram = null;
		if (iStudentInfoVisibleColumns.hasProg) {
			hProgram = new UniTimeTableHeader(MESSAGES.colProgram());
			//hMajor.setWidth("100px");
			header.add(hProgram);
			addSortOperation(hProgram, StudentComparator.SortBy.PROGRAM, MESSAGES.colProgram());
		}
		
		UniTimeTableHeader hMajor = null;
		if (iStudentInfoVisibleColumns.hasMajor) {
			hMajor = new UniTimeTableHeader(MESSAGES.colMajor());
			//hMajor.setWidth("100px");
			header.add(hMajor);
			addSortOperation(hMajor, StudentComparator.SortBy.MAJOR, MESSAGES.colMajor());
		}
		
		UniTimeTableHeader hConc = null;
		if (iStudentInfoVisibleColumns.hasConc) {
			hConc = new UniTimeTableHeader(MESSAGES.colConcentration());
			//hMajor.setWidth("100px");
			header.add(hConc);
			addSortOperation(hConc, StudentComparator.SortBy.CONCENTRATION, MESSAGES.colConcentration());
		}
		
		UniTimeTableHeader hMinor = null;
		if (iStudentInfoVisibleColumns.hasMinor) {
			hMinor = new UniTimeTableHeader(MESSAGES.colMinor());
			//hMajor.setWidth("100px");
			header.add(hMinor);
			addSortOperation(hMinor, StudentComparator.SortBy.MINOR, MESSAGES.colMinor());
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
		
		UniTimeTableHeader hPin = null;
		if (iStudentInfoVisibleColumns.hasPin) {
			iPinColumn = header.size() - 1;
			hPin = new UniTimeTableHeader(MESSAGES.colStudentPin());
			header.add(hPin);
			addSortOperation(hPin, StudentComparator.SortBy.PIN, MESSAGES.colStudentPin());
		} else {
			iPinColumn = -1;
		}
		
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
		
		UniTimeTableHeader hAdvisor = null;
		if (iStudentInfoVisibleColumns.hasAdvisor) {
			hAdvisor = new UniTimeTableHeader(MESSAGES.colAdvisor());
			header.add(hAdvisor);
			addSortOperation(hAdvisor, StudentComparator.SortBy.ADVISOR, MESSAGES.colAdvisor());
		}

		UniTimeTableHeader hAdvisedCred = null, hMissingCourses = null, hNotAssignedCourses = null;
		if (iStudentInfoVisibleColumns.hasAdvisedInfo) {
			hAdvisedCred = new UniTimeTableHeader(MESSAGES.colAdvisedCredit());
			header.add(hAdvisedCred);
			addSortOperation(hAdvisedCred, StudentComparator.SortBy.ADVISED_CRED, MESSAGES.ordAdvisedCredit());
			addSortOperation(hAdvisedCred, StudentComparator.SortBy.ADVISED_PERC, MESSAGES.ordAdvisedPercentage());
			hMissingCourses = new UniTimeTableHeader(MESSAGES.colMissingCourses());
			header.add(hMissingCourses);
			addSortOperation(hMissingCourses, StudentComparator.SortBy.ADVISED_CRIT, MESSAGES.ordAdvisedCourses());
			hNotAssignedCourses = new UniTimeTableHeader(MESSAGES.colNotAssignedCourses());
			header.add(hNotAssignedCourses);
			addSortOperation(hNotAssignedCourses, StudentComparator.SortBy.ADVISED_ASSGN, MESSAGES.ordNotAssignedCourses());

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
		
		UniTimeTableHeader hPref = null;
		if (iStudentInfoVisibleColumns.hasPref) {
			hPref = new UniTimeTableHeader(MESSAGES.colSchedulingPreference());
			header.add(hPref);
			addSortOperation(hPref, StudentComparator.SortBy.PREF, MESSAGES.colSchedulingPreference());
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
			case CONCENTRATION: h = hConc; break;
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
			case ADVISOR: h = hAdvisor; break;
			case ADVISED_CRED: h = hAdvisedCred; break;
			case ADVISED_PERC: h = hAdvisedCred; break;
			case ADVISED_CRIT: h = hMissingCourses; break;
			case ADVISED_ASSGN: h = hNotAssignedCourses; break;
			case MINOR: h = hMinor; break;
			case DEGREE: h = hDegree; break;
			case PROGRAM: h = hProgram; break;
			case CAMPUS: h = hCampus; break;
			case PREF: h = hPref; break;
			case PIN: h = hPin; break;
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
		
		for (HideOperation op: iHideOperations)
			op.fixColumnVisibility();
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
			if (iStudentInfoVisibleColumns.hasCamp)
				line.add(new ACM(info.getStudent().getCampuses()));
			if (iStudentInfoVisibleColumns.hasArea) {
				line.add(new ACM(info.getStudent().getAreas()));
				line.add(new ACM(info.getStudent().getClassifications()));
			}
			if (iStudentInfoVisibleColumns.hasDeg)
				line.add(new ACM(info.getStudent().getDegrees()));
			if (iStudentInfoVisibleColumns.hasProg)
				line.add(new ACM(info.getStudent().getPrograms()));
			if (iStudentInfoVisibleColumns.hasMajor)
				line.add(new ACM(info.getStudent().getMajors()));
			if (iStudentInfoVisibleColumns.hasConc)
				line.add(new ACM(info.getStudent().getConcentrations()));
			if (iStudentInfoVisibleColumns.hasMinor)
				line.add(new ACM(info.getStudent().getMinors()));
			if (iStudentInfoVisibleColumns.hasGroup)
				line.add(new Groups(info.getStudent().getGroups()));
			for (String type: iStudentInfoVisibleColumns.groupTypes)
				line.add(new Groups(info.getStudent().getGroups(type)));
			if (iStudentInfoVisibleColumns.hasAcmd)
				line.add(new ACM(info.getStudent().getAccommodations()));
			line.add(new HTML(info.getStatus(), false));
			if (iStudentInfoVisibleColumns.hasPin)
				line.add(new Label(info.hasPinReleased() ? info.getPin() : "", false));
		} else {
			if (iOnline && iProperties != null && iProperties.isCanSelectStudent()) line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasExtId)
				line.add(new TitleCell(MESSAGES.total()));
			else
				line.add(new Label(MESSAGES.total()));
			line.add(new NumberCell(null, iStudentInfos.size() - 1));
			if (iStudentInfoVisibleColumns.hasCamp)
				line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasArea) {
				line.add(new HTML("&nbsp;", false));
				line.add(new HTML("&nbsp;", false));
			}
			if (iStudentInfoVisibleColumns.hasDeg)
				line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasProg)
				line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasMajor)
				line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasConc)
				line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasMinor)
				line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasGroup)
				line.add(new HTML("&nbsp;", false));
			for (@SuppressWarnings("unused") String type: iStudentInfoVisibleColumns.groupTypes)
				line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasAcmd)
				line.add(new HTML("&nbsp;", false));
			line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasPin)
				line.add(new HTML("&nbsp;", false));
		}
		if (iStudentInfoVisibleColumns.hasEnrollment)
			line.add(new EnrollmentCell(info));
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
			if (iStudentInfoVisibleColumns.hasAdvisor)
				line.add(new HTML(info.getStudent().getAdvisor("<br>"), false));
			if (iStudentInfoVisibleColumns.hasAdvisedInfo) {
				line.add(new AdvisorInfoCell(info.getAdvisedInfo(), AdvisorInfoCell.Mode.ADVISOR_CREDITS));
				line.add(new AdvisorInfoCell(info.getAdvisedInfo(), AdvisorInfoCell.Mode.MISSING_COURSES));
				line.add(new AdvisorInfoCell(info.getAdvisedInfo(), AdvisorInfoCell.Mode.NOT_ENROLLED_COURSES));
			}
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
			if (iStudentInfoVisibleColumns.hasAdvisor)
				line.add(new HTML("&nbsp;", false));
			if (iStudentInfoVisibleColumns.hasAdvisedInfo) {
				line.add(new HTML("&nbsp;", false));
				line.add(new HTML("&nbsp;", false));
				line.add(new HTML("&nbsp;", false));
			}
			if (iOnline && iStudentInfoVisibleColumns.hasNote)
				line.add(new HTML("&nbsp;", false));
			if (iOnline && iStudentInfoVisibleColumns.hasEmailed)
				line.add(new HTML("&nbsp;", false));
		}
		if (iStudentInfoVisibleColumns.hasPref) {
			HTML html = new HTML(info.hasPreference() ? info.getPreference() : "", false);
			if (info.hasPreference()) html.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
			line.add(html);
		}
		iStudentTable.addRow(info, line);
	}
	
	public void populateChangeLog(List<SectioningAction> result) {
		iSectioningActions = result; iSectioningActionsFirstLine = 0;
		iSortOperations.clear();
		iHideOperations.clear();
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
		final Map<Long, HTML> id2message = new HashMap<Long, HTML>();
		iSectioningActionsFirstLine = firstLine;
		if (iLogTable.getRowCount() > 0) iLogTable.clearTable(1);
		for (int line = iSectioningActionsFirstLine; line < iSectioningActions.size() && (iMaxTableLines <= 0 || line < iSectioningActionsFirstLine + iMaxTableLines); line++) {
			addLogTableLine(iSectioningActions.get(line), id2message);
		}
		iRange.setText(MESSAGES.pageRange(iSectioningActionsFirstLine + 1, Math.min(iSectioningActions.size(), iSectioningActionsFirstLine + iMaxTableLines)));
		
		iPaginationButtons.setVisible(iSectioningActions.size() > iMaxTableLines && iMaxTableLines > 0);
		iPrevious.setEnabled(iMaxTableLines > 0 && iSectioningActionsFirstLine >= iMaxTableLines);
		iNext.setEnabled(iMaxTableLines > 0 && iSectioningActionsFirstLine + iMaxTableLines < iSectioningActions.size());
		
		if (!id2message.isEmpty()) {
			iSectioningService.getChangeLogTexts(new ArrayList<Long>(id2message.keySet()), new AsyncCallback<Map<Long,String>>() {
				@Override
				public void onSuccess(Map<Long, String> result) {
					for (Map.Entry<Long, String> e: result.entrySet()) {
						HTML html = id2message.get(e.getKey());
						if (html != null) html.setHTML(e.getValue());
					}
				}
				@Override
				public void onFailure(Throwable caught) {
				}
			});
		}
		
		for (HideOperation op: iHideOperations)
			op.fixColumnVisibility();
	}
	
	private void addLogTableLine(SectioningAction log, Map<Long,HTML> id2message) {
		HTML message = new HTML(log.getMessage() == null ? "" : log.getMessage());
		id2message.put(log.getLogId(), message);
		message.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
		if (iSectioningActionsVisibleColumns.hasExtId) {
			iLogTable.addRow(log,
					new TopCell(log.getStudent().isCanShowExternalId() ? log.getStudent().getExternalId() : ""),
					new TopCell(log.getStudent().getName()),
					new TopCell(log.getOperation()),
					new TopCell(sTSF.format(log.getTimeStamp())),
					new TopCell(log.getWallTime() == null ? "" : sNF.format(0.001 * log.getWallTime())),
					new TopCell(log.getResult()),
					new TopCell(log.getUser() == null ? "" : log.getUser()),
					message
			);
		} else {
			iLogTable.addRow(log,
					new TopCell(log.getStudent().getName()),
					new TopCell(log.getOperation()),
					new TopCell(sTSF.format(log.getTimeStamp())),
					new TopCell(log.getWallTime() == null ? "" : sNF.format(0.001 * log.getWallTime())),
					new TopCell(log.getResult()),
					new TopCell(log.getUser() == null ? "" : log.getUser()),
					message
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
		if (sort == EnrollmentComparator.SortBy.COURSE) {
			addHideOperation(0, header, MESSAGES.colCourse() + " / " + MESSAGES.colClass());
		} else if (sort == EnrollmentComparator.SortBy.TITLE) {
			addHideOperation(0, header, MESSAGES.colTitle() + " / " + MESSAGES.colTime());
		} else if (sort == EnrollmentComparator.SortBy.CONSENT) {
			addHideOperation(0, header, MESSAGES.colConsent() + " / " + MESSAGES.colRoom());
		} else addHideOperation(0, header, column);
	}
	
	protected <T> void addHideOperation(final int table, final UniTimeTableHeader header, final String column) {
		HideOperation op = new HideOperation() {
			@Override
			public void execute() {
				boolean hidden = SectioningStatusCookie.getInstance().isHidden(iOnline, table, column);
				SectioningStatusCookie.getInstance().setHidden(iOnline, table, column, !hidden);
				fixColumnVisibility();
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
				if (SectioningStatusCookie.getInstance().isHidden(iOnline, table, column))
					return GWT_MESSAGES.opShowItem(column);
				else
					return GWT_MESSAGES.opHideItem(column);
			}
			@Override
			public String getColumnName() {
				if (SectioningStatusCookie.getInstance().isHidden(iOnline, table, column))
					return GWT_MESSAGES.opShow(column);
				else
					return GWT_MESSAGES.opHide(column);
			}
			@Override
			public void fixColumnVisibility() {
				boolean visible = !SectioningStatusCookie.getInstance().isHidden(iOnline, table, column);
				if (table == 0 && iCourseTable.getRowCount() > 0) {
					for (int col = 0; col < iCourseTable.getCellCount(0); col++)
						if (iCourseTable.getWidget(0, col) == header) {
							iCourseTable.setColumnVisible(col, visible);
							break;
						}
				} else if (table == 1 && iStudentTable.getRowCount() > 0) {
					for (int col = 0; col < iStudentTable.getCellCount(0); col++)
						if (iStudentTable.getWidget(0, col) == header) {
							iStudentTable.setColumnVisible(col, visible);
							break;
						}
				} else if (table == 2 && iLogTable.getRowCount() > 0) {
					for (int col = 0; col < iLogTable.getCellCount(0); col++)
						if (iLogTable.getWidget(0, col) == header) {
							iLogTable.setColumnVisible(col, visible);
							break;
						}
				}
			}
			@Override
			public void showColumn() {
				if (SectioningStatusCookie.getInstance().isHidden(iOnline, table, column)) {
					SectioningStatusCookie.getInstance().setHidden(iOnline, table, column, false);
					fixColumnVisibility();
				}
			}
			@Override
			public boolean isColumnVisible() {
				return !SectioningStatusCookie.getInstance().isHidden(iOnline, table, column);
			}
		};
		header.addOperation(op);
		iHideOperations.add(op);
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
				if (sort == StudentComparator.SortBy.GROUP && group != null && !group.isEmpty()) return group;
				return column;
			}
		};
		header.addOperation(op);
		iSortOperations.add(op);
		if (sort == StudentComparator.SortBy.ADVISED_CRED || sort == StudentComparator.SortBy.STUDENT) {
		} else if (sort == StudentComparator.SortBy.ADVISED_PERC) {
			addHideOperation(1, header, MESSAGES.colAdvisedCredit());
		} else if (sort == StudentComparator.SortBy.GROUP && group != null && !group.isEmpty()) {
			addHideOperation(1, header, group);
		} else {
			addHideOperation(1, header, column);
		}
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
		addHideOperation(2, header, column);
	}
	
	private static interface SortOperation extends Operation, HasColumnName {}
	private static interface HideOperation extends Operation, HasColumnName {
		public void fixColumnVisibility();
		public void showColumn();
		public boolean isColumnVisible();
	}
	
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
	
	public static class AdvisorInfoCell extends HTML implements HasCellAlignment, HasStyleChanges {
		String iBackgroundColor = null;
		
		public static enum Mode {
			ADVISOR_CREDITS,
			MISSING_COURSES,
			NOT_ENROLLED_COURSES,
			};
		
		public AdvisorInfoCell(AdvisedInfoInterface value, Mode mode) {
			super();
			addStyleName("advised-info");
			String title = null;
			switch (mode) {
			case ADVISOR_CREDITS:
				if (value != null) {
					if (value.getMinCredit() < value.getMaxCredit()) {
						setHTML(MESSAGES.advisedCreditRange(value.getMinCredit(), value.getMaxCredit()));
						title = MESSAGES.hintAdvisedCredit(MESSAGES.advisedCreditRange(value.getMinCredit(), value.getMaxCredit()));
					} else {
						setHTML(MESSAGES.advisedCredit(value.getMinCredit()));
						title = MESSAGES.hintAdvisedCredit(MESSAGES.advisedCredit(value.getMinCredit()));
					}
				}
				break;
			case MISSING_COURSES:
				if (value != null && value.getMissingCritical() != null && value.getMissingPrimary() != null) {
					if (value.getMissingCritical() > 0) {
						if (value.isAdvisorImportant()) {
							if (value.getMissingPrimary() > value.getMissingCritical()) {
								setHTML(MESSAGES.advisedMissingImportantOther(value.getMissingCritical(), value.getMissingPrimary() - value.getMissingCritical()));
								title = MESSAGES.hintAdvisedMissingImportantOther(value.getMissingCritical(), value.getMissingPrimary() - value.getMissingCritical());
							} else {
								setHTML(MESSAGES.advisedMissingImportant(value.getMissingCritical()));
								title = MESSAGES.hintAdvisedMissingImportant(value.getMissingCritical());
							}
						} else if (value.isAdvisorVital()) {
							if (value.getMissingPrimary() > value.getMissingCritical()) {
								setHTML(MESSAGES.advisedMissingVitalOther(value.getMissingCritical(), value.getMissingPrimary() - value.getMissingCritical()));
								title = MESSAGES.hintAdvisedMissingVitalOther(value.getMissingCritical(), value.getMissingPrimary() - value.getMissingCritical());
							} else {
								setHTML(MESSAGES.advisedMissingVital(value.getMissingCritical()));
								title = MESSAGES.hintAdvisedMissingVital(value.getMissingCritical());
							}
						} else {
							if (value.getMissingPrimary() > value.getMissingCritical()) {
								setHTML(MESSAGES.advisedMissingCriticalOther(value.getMissingCritical(), value.getMissingPrimary() - value.getMissingCritical()));
								title = MESSAGES.hintAdvisedMissingCriticalOther(value.getMissingCritical(), value.getMissingPrimary() - value.getMissingCritical());
							} else {
								setHTML(MESSAGES.advisedMissingCritical(value.getMissingCritical()));
								title = MESSAGES.hintAdvisedMissingCritical(value.getMissingCritical());
							}
						}
					} else if (value.getMissingPrimary() > 0) {
						setHTML(MESSAGES.advisedMissingPrimary(value.getMissingPrimary()));
						title = MESSAGES.hintAdvisedMissingOther(value.getMissingPrimary());
					}
				}
				break;
			case NOT_ENROLLED_COURSES:
				if (value != null && value.getNotAssignedPrimary() != null && value.getNotAssignedCritical() != null) {
					if (value.getNotAssignedCritical() > 0) {
						if (value.isAdvisorImportant()) {
							if (value.getNotAssignedPrimary() > value.getNotAssignedCritical()) {
								setHTML(MESSAGES.advisedNotAssignedImportantOther(value.getNotAssignedCritical(), value.getNotAssignedPrimary() - value.getNotAssignedCritical()));
								title = MESSAGES.hintAdvisedNotAssignedImportantOther(value.getNotAssignedCritical(), value.getNotAssignedPrimary() - value.getNotAssignedCritical());
							} else {
								setHTML(MESSAGES.advisedNotAssignedImportant(value.getNotAssignedCritical()));
								title = MESSAGES.hintAdvisedNotAssignedImportant(value.getNotAssignedCritical());
							}
						} else if (value.isAdvisorVital()) {
							if (value.getNotAssignedPrimary() > value.getNotAssignedCritical()) {
								setHTML(MESSAGES.advisedNotAssignedVitalOther(value.getNotAssignedCritical(), value.getNotAssignedPrimary() - value.getNotAssignedCritical()));
								title = MESSAGES.hintAdvisedNotAssignedVitalOther(value.getNotAssignedCritical(), value.getNotAssignedPrimary() - value.getNotAssignedCritical());
							} else {
								setHTML(MESSAGES.advisedNotAssignedVital(value.getNotAssignedCritical()));
								title = MESSAGES.hintAdvisedNotAssignedVital(value.getNotAssignedCritical());
							}
						} else {
							if (value.getNotAssignedPrimary() > value.getNotAssignedCritical()) {
								setHTML(MESSAGES.advisedNotAssignedCriticalOther(value.getNotAssignedCritical(), value.getNotAssignedPrimary() - value.getNotAssignedCritical()));
								title = MESSAGES.hintAdvisedNotAssignedCriticalOther(value.getNotAssignedCritical(), value.getNotAssignedPrimary() - value.getNotAssignedCritical());
							} else {
								setHTML(MESSAGES.advisedNotAssignedCritical(value.getNotAssignedCritical()));
								title = MESSAGES.hintAdvisedNotAssignedCritical(value.getNotAssignedCritical());
							}
						}
					} else if (value.getNotAssignedPrimary() > 0) {
						setHTML(MESSAGES.advisedNotAssignedPrimary(value.getNotAssignedPrimary()));
						title = MESSAGES.hintAdvisedNotAssignedOther(value.getNotAssignedPrimary());
					}
				}
				break;
			}
			if (value != null && value.hasMessage() && mode != Mode.NOT_ENROLLED_COURSES)
				setTitle((title == null ? "" : title + "\n") + value.getMessage());
			else if (value != null && value.hasNotAssignedMessage() && mode == Mode.NOT_ENROLLED_COURSES)
				setTitle((title == null ? "" : title + "\n") + value.getNotAssignedMessage());
			else if (title != null)
				setTitle(title);
			if (value != null) {
				if (value.getPercentage() <= 0.5f) {
					// from FFCCCC (red) to FFFFCC (yellow)
					iBackgroundColor = "rgb(255," + (204 + Math.round(100f * value.getPercentage())) + ",204)";
				} else {
					// from FFFFCC (yellow) to CCFFCC (green)
					iBackgroundColor = "rgb(" + (305 - Math.round(100f * value.getPercentage())) + ",255,204)";
				}
			}
			if (iBackgroundColor != null && !getHTML().isEmpty() && mode == Mode.ADVISOR_CREDITS)
				getElement().getStyle().setBackgroundColor(iBackgroundColor);
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}

		@Override
		public void applyStyleChanegs(Style style) {
			/*
			if (iBackgroundColor != null)
				style.setBackgroundColor(iBackgroundColor);
			else
				style.clearBackgroundColor();
				*/
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
	
	public class AvailableCell extends HTML implements HasCellAlignment {
		private boolean iMouseOver = false;
		
		public AvailableCell(final EnrollmentInfo e) {
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
			if (e.getOfferingId() != null) {
				addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						iMouseOver = true;
						iSectioningService.getReservations(iOnline, e.getOfferingId(), new AsyncCallback<List<ReservationInterface>>() {
							@Override
							public void onFailure(Throwable caught) {
							}

							@Override
							public void onSuccess(List<ReservationInterface> result) {
								if (result != null)
									for (Iterator<ReservationInterface> i = result.iterator(); i.hasNext(); ) {
										ReservationInterface r = i.next();
										if (r instanceof ReservationInterface.OverrideReservation && !r.isAllowOverlaps() && !r.isMustBeUsed() && !r.isOverLimit()) {
											i.remove();
										} else if (e.getClazzId() != null) {
											boolean match = false;
											for (ReservationInterface.Config c: r.getConfigs()) {
												if (e.getConfigId().equals(c.getId())) {
													match = true; break;
												}
											}
											for (ReservationInterface.Clazz c: r.getClasses()) {
												if (e.getClazzId().equals(c.getId())) {
													match = true; break;
												}
											}
											if (!match) i.remove();
										}
									}
								if (result != null && !result.isEmpty() && iMouseOver) {
									ReservationTable rt = new ReservationTable(false, false);
									rt.populate(result, 5);
									rt.getTable().setColumnVisible(ReservationColumn.LAST_LIKE.ordinal(), false);
									rt.getTable().setColumnVisible(ReservationColumn.PROJECTED_BY_RULE.ordinal(), false);
									rt.getTable().setColumnVisible(ReservationColumn.EXPIRATION_DATE.ordinal(), iOnline);
									rt.getTable().setColumnVisible(ReservationColumn.START_DATE.ordinal(), iOnline);
									GwtHint.showHint(getRowElement(), rt);
								}
							}
						});
					}
				});
				addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						iMouseOver = false;
						GwtHint.hideHint();
					}
				});
			}
		}
		
		public Element getRowElement() {
			Element e = getElement();
			while (!"tr".equalsIgnoreCase(e.getNodeName()) && e.getParentElement() != null) {
				e = e.getParentElement();
			}
			return e;
		}
		
		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
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
	
	public static class WaitListCell extends HTML implements HasCellAlignment {
		public WaitListCell(int wait, int tWait, int noSub, int tNoSub, int unasg, int tUnasg, Integer topWaitingPriority) {
			super();
			if (tNoSub == 0) {
				// no no-subs -- like before
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
			} else if (tWait == 0) {
				// no wait-lists -- like before, but with no-sub
				if (tNoSub == 0 || tNoSub == tUnasg) {
					// no no-sub or all no-subs
					if (unasg == tUnasg) {
						setHTML((unasg == 0 ? "-" : String.valueOf(unasg)) + (tNoSub > 0 ? MESSAGES.htmlNoSubSign() : ""));
					} else {
						setHTML((unasg + " / " + tUnasg) + (tNoSub > 0 ? MESSAGES.htmlNoSubSign() : ""));
					}
				} else {
					if (noSub == tNoSub && unasg == tUnasg) {
						setHTML((noSub == 0 ? String.valueOf(unasg) : noSub == unasg ? noSub + MESSAGES.htmlNoSubSign() : (unasg - noSub) + " + " + noSub + MESSAGES.htmlNoSubSign()));
					} else {
						setHTML(((noSub == 0 ? String.valueOf(unasg) : noSub == unasg ? noSub + MESSAGES.htmlNoSubSign() : (unasg - noSub) + " + " + noSub + MESSAGES.htmlNoSubSign()) + " / " + tUnasg));
					}
				}
			} else {
				if (unasg > noSub + wait)
					setHTML(String.valueOf(unasg - noSub - wait) + (wait > 0 ? " + " + wait + MESSAGES.htmlWaitListSign() : "") + (noSub > 0 ? " + " + noSub + MESSAGES.htmlNoSubSign() : "") + " / " + tUnasg);
				else if (wait > 0)
					setHTML(wait + MESSAGES.htmlWaitListSign() + (noSub > 0 ? " + " + noSub + MESSAGES.htmlNoSubSign() : "") + " / " + tUnasg);
				else if (noSub > 0)
					setHTML(noSub + MESSAGES.htmlNoSubSign() + " / " + tUnasg);
				else if (unasg == tUnasg)
					setHTML((unasg == 0 ? "-" : String.valueOf(unasg)));
				else
					setHTML(unasg + " / " + tUnasg);
			}
			
			if (topWaitingPriority != null)
				setHTML(getHTML() + " " + MESSAGES.firstWaitListedPrioritySign(topWaitingPriority));
		}
			
		public WaitListCell(StudentInfo e) {
			this(e.hasWaitlist() ? e.getWaitlist() : 0,
				e.hasTotalWaitlist() ? e.getTotalWaitlist() : 0,
				e.hasNoSub() ? e.getNoSub() : 0,
				e.hasTotalNoSub() ? e.getTotalNoSub() : 0,
				e.hasUnassigned() ? e.getUnassigned() : 0,
				e.hasTotalUnassigned() ? e.getTotalUnassigned() : 0,
				e.getTopWaitingPriority());
		}
		
		public WaitListCell(EnrollmentInfo e) {
			this(e.hasWaitlist() ? e.getWaitlist() : 0,
				e.hasTotalWaitlist() ? e.getTotalWaitlist() : 0,
				e.hasNoSub() ? e.getNoSub() : 0,
				e.hasTotalNoSub() ? e.getTotalNoSub() : 0,
				e.hasUnassigned() ? e.getUnassignedPrimary() : 0,
				e.hasTotalUnassigned() ? e.getTotalUnassignedPrimary() : 0,
				null);
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class EnrollmentCell extends HTML implements HasCellAlignment {
		public EnrollmentCell(int enrl, int tEnrl, int swap, int tSwap) {
			super();
			if (tSwap == 0 || tSwap == tEnrl) {
				// no wait-list or all wait-listed
				if (enrl == tEnrl) {
					setHTML(enrl == 0 ? "-" : String.valueOf(enrl));
				} else {
					setHTML(enrl + " / " + tEnrl);
				}
				if (tSwap > 0)
					setHTML(getHTML() + MESSAGES.htmlWaitListSign());
			} else if (swap == tSwap && enrl == tEnrl) {
				setHTML(swap == 0 ? String.valueOf(enrl) : swap == enrl ? swap + MESSAGES.htmlWaitListSign() : (enrl - swap) + " + " + swap + MESSAGES.htmlWaitListSign());
			} else {
				setHTML((swap == 0 ? String.valueOf(enrl) : swap == enrl ? swap + MESSAGES.htmlWaitListSign() : (enrl - swap) + " + " + swap + MESSAGES.htmlWaitListSign())
						+ " / " + tEnrl);
			}
		}
			
		public EnrollmentCell(StudentInfo e) {
			this(e.hasEnrollment() ? e.getEnrollment() : 0,
				e.hasTotalEnrollment() ? e.getTotalEnrollment() : 0,
				e.hasSwap() ? e.getSwap() : 0,
				e.hasTotalSwap() ? e.getTotalSwap() : 0);
		}
		
		public EnrollmentCell(EnrollmentInfo e) {
			this(e.hasEnrollment() ? e.getEnrollment() : 0,
				e.hasTotalEnrollment() ? e.getTotalEnrollment() : 0,
				e.hasSwap() ? e.getSwap() : 0,
				e.hasTotalSwap() ? e.getTotalSwap() : 0);
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
			SNAPSHOT,
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
			
			if (e1.getMasterCouresId().equals(e2.getMasterCouresId())) { // Same course
				// Course line first
				if (e1.getConfigId() == null) return (iAsc ? -1 : 1);
				if (e2.getConfigId() == null) return (iAsc ? 1 : -1);
				// Cross-listed courses second
				if (e1.getConfigId() == -1l && e2.getConfigId() != -1l) return (iAsc ? -1 : 1);
				if (e1.getConfigId() != -1l && e2.getConfigId() == -1l) return (iAsc ? 1 : -1);
				// Compare classes
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
				cmp = e1.getMasterCourseNbr().compareTo(e2.getMasterCourseNbr());
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
				cmp = (e1.getAvailable() == null ? Integer.valueOf(0) : e1.getAvailable() < 0 ? Integer.valueOf(Integer.MAX_VALUE) : e1.getAvailable()).compareTo(
						e2.getAvailable() == null ? 0 : e2.getAvailable() < 0 ? Integer.MAX_VALUE : e2.getAvailable());
				if (cmp != 0) return cmp;
				cmp = (e1.getLimit() == null ? Integer.valueOf(0) : e1.getLimit()).compareTo(e2.getLimit() == null ? 0 : e2.getLimit());
				if (cmp != 0) return cmp;
				break;
			case SNAPSHOT:
				cmp = (e1.getSnapshot() == null ? Integer.valueOf(0) : e1.getSnapshot()).compareTo(e2.getSnapshot() == null ? 0 : e2.getSnapshot());
				if (cmp != 0) return cmp;
				cmp = (e1.getLimit() == null ? Integer.valueOf(0) : e1.getLimit()).compareTo(e2.getLimit() == null ? 0 : e2.getLimit());
				if (cmp != 0) return cmp;
				break;
			case PROJECTION:
				cmp = (e1.getProjection() == null ? Integer.valueOf(0) : e1.getProjection()).compareTo(e2.getProjection() == null ? 0 : e2.getProjection());
				if (cmp != 0) return - cmp;
				break;
			case ENROLLMENT:
				cmp = (e1.getEnrollment() == null ? Integer.valueOf(0) : e1.getEnrollment()).compareTo(e2.getEnrollment() == null ? 0 : e2.getEnrollment());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalEnrollment() == null ? Integer.valueOf(0) : e1.getTotalEnrollment()).compareTo(e2.getTotalEnrollment() == null ? 0 : e2.getTotalEnrollment());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalSwap() == null ? Integer.valueOf(0) : e1.getTotalSwap()).compareTo(e2.getTotalSwap() == null ? 0 : e2.getTotalSwap());
				if (cmp != 0) return - cmp;
				break;
			case WAITLIST:
				cmp = (e1.getUnassignedPrimary() == null ? Integer.valueOf(0) : e1.getUnassignedPrimary()).compareTo(e2.getUnassignedPrimary() == null ? 0 : e2.getUnassignedPrimary());
				if (cmp != 0) return - cmp;
				cmp = (e1.getWaitlist() == null ? Integer.valueOf(0) : e1.getWaitlist()).compareTo(e2.getWaitlist() == null ? 0 : e2.getWaitlist());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalUnassignedPrimary() == null ? Integer.valueOf(0) : e1.getTotalUnassignedPrimary()).compareTo(e2.getTotalUnassignedPrimary() == null ? 0 : e2.getTotalUnassignedPrimary());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalWaitlist() == null ? Integer.valueOf(0) : e1.getTotalWaitlist()).compareTo(e2.getTotalWaitlist() == null ? 0 : e2.getTotalWaitlist());
				if (cmp != 0) return - cmp;
				break;
			case RESERVATION:
				cmp = (e1.getReservation() == null ? Integer.valueOf(0) : e1.getReservation()).compareTo(e2.getReservation() == null ? 0 : e2.getReservation());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalReservation() == null ? Integer.valueOf(0) : e1.getTotalReservation()).compareTo(e2.getTotalReservation() == null ? 0 : e2.getTotalReservation());
				if (cmp != 0) return - cmp;
				break;
			case NEED_CONSENT:
				cmp = (e1.getConsentNeeded() == null ? Integer.valueOf(0) : Integer.valueOf(e1.getConsentNeeded())).compareTo(e2.getConsentNeeded() == null ? 0 : e2.getConsentNeeded());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalConsentNeeded() == null ? Integer.valueOf(0) : Integer.valueOf(e1.getTotalConsentNeeded())).compareTo(e2.getTotalConsentNeeded() == null ? 0 : e2.getTotalConsentNeeded());
				if (cmp != 0) return - cmp;
				break;
			case ALTERNATIVES:
				cmp = (e1.getUnassignedAlternative() == null ? Integer.valueOf(0) : e1.getUnassignedAlternative()).compareTo(e2.getUnassignedAlternative() == null ? 0 : e2.getUnassignedAlternative());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalUnassignedAlternative() == null ? Integer.valueOf(0) : e1.getTotalUnassignedAlternative()).compareTo(e2.getTotalUnassignedAlternative() == null ? 0 : e2.getTotalUnassignedAlternative());
				if (cmp != 0) return - cmp;
				break;
			case NEED_OVERRIDE:
				cmp = (e1.getOverrideNeeded() == null ? Integer.valueOf(0) : Integer.valueOf(e1.getOverrideNeeded())).compareTo(e2.getOverrideNeeded() == null ? 0 : e2.getOverrideNeeded());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalOverrideNeeded() == null ? Integer.valueOf(0) : Integer.valueOf(e1.getTotalOverrideNeeded())).compareTo(e2.getTotalOverrideNeeded() == null ? 0 : e2.getTotalOverrideNeeded());
				if (cmp != 0) return - cmp;
				break;
			}
			
			// Default sort
			cmp = e1.getMasterSubject().compareTo(e2.getMasterSubject());
			if (cmp != 0) return cmp;
			
			cmp = e1.getMasterCourseNbr().compareTo(e2.getMasterCourseNbr());
			if (cmp != 0) return cmp;
			
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
			ADVISOR,
			ADVISED_CRED,
			ADVISED_PERC,
			ADVISED_CRIT,
			ADVISED_ASSGN,
			MINOR,
			CONCENTRATION,
			DEGREE,
			PROGRAM,
			CAMPUS,
			PREF,
			PIN,
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
			case MINOR:
				return e1.getStudent().getMinor("|").compareTo(e2.getStudent().getMinor("|"));
			case CONCENTRATION:
				cmp = e1.getStudent().getConcentration("|").compareTo(e2.getStudent().getConcentration("|"));
				if (cmp != 0) return cmp;
				cmp = e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
				if (cmp != 0) return cmp;
				return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
			case DEGREE:
				cmp = e1.getStudent().getDegree("|").compareTo(e2.getStudent().getDegree("|"));
				if (cmp != 0) return cmp;
				return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
			case PROGRAM:
				cmp = e1.getStudent().getProgram("|").compareTo(e2.getStudent().getProgram("|"));
				if (cmp != 0) return cmp;
				return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
			case CAMPUS:
				cmp = e1.getStudent().getCampus("|").compareTo(e2.getStudent().getCampus("|"));
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
				cmp = (e1.getEnrollment() == null ? Integer.valueOf(0) : e1.getEnrollment()).compareTo(e2.getEnrollment() == null ? 0 : e2.getEnrollment());
				if (cmp != 0) return - cmp;
				return (e1.getTotalEnrollment() == null ? Integer.valueOf(0) : e1.getTotalEnrollment()).compareTo(e2.getTotalEnrollment() == null ? 0 : e2.getTotalEnrollment());
			case WAITLIST:
				cmp = (e1.getUnassigned() == null ? Integer.valueOf(0) : e1.getUnassigned()).compareTo(e2.getUnassigned() == null ? 0 : e2.getUnassigned());
				if (cmp != 0) return - cmp;
				cmp = (e1.getWaitlist() == null ? Integer.valueOf(0) : e1.getWaitlist()).compareTo(e2.getWaitlist() == null ? 0 : e2.getWaitlist());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalUnassigned() == null ? Integer.valueOf(0) : e1.getTotalUnassigned()).compareTo(e2.getTotalUnassigned() == null ? 0 : e2.getTotalUnassigned());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalWaitlist() == null ? Integer.valueOf(0) : e1.getTotalWaitlist()).compareTo(e2.getTotalWaitlist() == null ? 0 : e2.getTotalWaitlist());
				if (cmp != 0) return - cmp;
				return (e1.getTopWaitingPriority() == null ? Integer.valueOf(Integer.MAX_VALUE) : e1.getTopWaitingPriority()).compareTo(e2.getTopWaitingPriority() == null ? Integer.MAX_VALUE : e2.getTopWaitingPriority());
			case RESERVATION:
				cmp = (e1.getReservation() == null ? Integer.valueOf(0) : e1.getReservation()).compareTo(e2.getReservation() == null ? 0 : e2.getReservation());
				if (cmp != 0) return - cmp;
				return (e1.getTotalReservation() == null ? Integer.valueOf(0) : e1.getTotalReservation()).compareTo(e2.getTotalReservation() == null ? 0 : e2.getTotalReservation());
			case CONSENT:
				cmp = (e1.getConsentNeeded() == null ? Integer.valueOf(0) : e1.getConsentNeeded()).compareTo(e2.getConsentNeeded() == null ? 0 : e2.getConsentNeeded());
				if (cmp != 0) return - cmp;
				return (e1.getTotalConsentNeeded() == null ? Integer.valueOf(0) : e1.getTotalConsentNeeded()).compareTo(e2.getTotalConsentNeeded() == null ? 0 : e2.getTotalConsentNeeded());
			case CREDIT:
				cmp = (e1.hasCredit() ? e1.getCredit() : Float.valueOf(0f)).compareTo(e2.hasCredit() ? e2.getCredit() : Float.valueOf(0f));
				if (cmp != 0) return - cmp;
				return (e1.hasTotalCredit() ? e1.getTotalCredit() : Float.valueOf(0f)).compareTo(e2.hasTotalCredit() ? e2.getTotalCredit() : Float.valueOf(0f));
			case REQ_CREDIT:
				cmp = (e1.hasRequestCredit() ? Float.valueOf(e1.getRequestCreditMin()) : Float.valueOf(0f)).compareTo(e2.hasRequestCredit() ? e2.getRequestCreditMin() : 0f);
				if (cmp != 0) return - cmp;
				cmp = (e1.hasRequestCredit() ? Float.valueOf(e1.getRequestCreditMax()) : Float.valueOf(0f)).compareTo(e2.hasRequestCredit() ? e2.getRequestCreditMax() : 0f);
				if (cmp != 0) return - cmp;
				cmp = (e1.hasTotalRequestCredit() ? Float.valueOf(e1.getTotalRequestCreditMin()) : Float.valueOf(0f)).compareTo(e2.hasTotalRequestCredit() ? e2.getTotalRequestCreditMin() : 0f);
				if (cmp != 0) return - cmp;
				return (e1.hasTotalRequestCredit() ? Float.valueOf(e1.getTotalRequestCreditMax()) : Float.valueOf(0f)).compareTo(e2.hasTotalRequestCredit() ? e2.getTotalRequestCreditMax() : 0f);
			case REQUEST_TS:
				return (e1.getRequestedDate() == null ? new Date(0) : e1.getRequestedDate()).compareTo(e2.getRequestedDate() == null ? new Date(0) : e2.getRequestedDate());
			case ENROLLMENT_TS:
				return (e1.getEnrolledDate() == null ? new Date(0) : e1.getEnrolledDate()).compareTo(e2.getEnrolledDate() == null ? new Date(0) : e2.getEnrolledDate());
			case EMAIL_TS:
				return (e1.getEmailDate() == null ? new Date(0) : e1.getEmailDate()).compareTo(e2.getEmailDate() == null ? new Date(0) : e2.getEmailDate());
			case NOTE:
				return (e1.hasNote() ? e1.getNote().compareTo(e2.hasNote() ? e2.getNote() : "") : "".compareTo(e2.hasNote() ? e2.getNote() : ""));
			case DIST_CONF:
				cmp = (e1.hasDistanceConflicts() ? e1.getNrDistanceConflicts() : Integer.valueOf(0)).compareTo(e2.hasDistanceConflicts() ? e2.getNrDistanceConflicts() : Integer.valueOf(0));
				if (cmp != 0) return - cmp;
				cmp = (e1.hasTotalDistanceConflicts() ? e1.getTotalNrDistanceConflicts() : Integer.valueOf(0)).compareTo(e2.hasTotalDistanceConflicts() ? e2.getTotalNrDistanceConflicts() : Integer.valueOf(0));
				if (cmp != 0) return - cmp;
				return - (e1.hasDistanceConflicts() ? e1.getLongestDistanceMinutes() : e1.hasTotalDistanceConflicts() ? e1.getTotalLongestDistanceMinutes() : Integer.valueOf(0)).compareTo(
						e2.hasDistanceConflicts() ? e2.getLongestDistanceMinutes() : e2.hasTotalDistanceConflicts() ? e2.getTotalLongestDistanceMinutes() : Integer.valueOf(0));
			case OVERLAPS:
				cmp = (e1.hasOverlappingMinutes() ? e1.getOverlappingMinutes() : Integer.valueOf(0)).compareTo(e2.hasOverlappingMinutes() ? e2.getOverlappingMinutes() : Integer.valueOf(0));
				if (cmp != 0) return - cmp;
				return (e1.hasTotalOverlappingMinutes() ? e1.getTotalOverlappingMinutes() : Integer.valueOf(0)).compareTo(e2.hasTotalOverlappingMinutes() ? e2.getTotalOverlappingMinutes() : Integer.valueOf(0));
			case FT_OVERLAPS:
				cmp = (e1.hasFreeTimeOverlappingMins() ? e1.getFreeTimeOverlappingMins() : Integer.valueOf(0)).compareTo(e2.hasFreeTimeOverlappingMins() ? e2.getFreeTimeOverlappingMins() : Integer.valueOf(0));
				if (cmp != 0) return - cmp;
				return (e1.hasTotalFreeTimeOverlappingMins() ? e1.getTotalFreeTimeOverlappingMins() : Integer.valueOf(0)).compareTo(e2.hasTotalFreeTimeOverlappingMins() ? e2.getTotalFreeTimeOverlappingMins() : Integer.valueOf(0));
			case PREF_IM:
				cmp = (e1.hasTotalPrefInstrMethConflict() ? Integer.valueOf(e1.getTotalPrefInstrMethConflict() - e1.getPrefInstrMethConflict()) : Integer.valueOf(0)).compareTo(e2.hasTotalPrefInstrMethConflict() ? Integer.valueOf(e2.getTotalPrefInstrMethConflict() - e2.getPrefInstrMethConflict()) : Integer.valueOf(0));
				if (cmp != 0) return - cmp;
				return - (e1.hasTotalPrefInstrMethConflict() ? e1.getTotalPrefInstrMethConflict() : Integer.valueOf(0)).compareTo(e2.hasTotalPrefInstrMethConflict() ? e2.getTotalPrefInstrMethConflict() : Integer.valueOf(0));
			case PREF_SEC:
				cmp = (e1.hasTotalPrefSectionConflict() ? Integer.valueOf(e1.getTotalPrefSectionConflict() - e1.getPrefSectionConflict()) : Integer.valueOf(0)).compareTo(e2.hasTotalPrefSectionConflict() ? Integer.valueOf(e2.getTotalPrefSectionConflict() - e2.getPrefSectionConflict()) : Integer.valueOf(0));
				if (cmp != 0) return - cmp;
				return -(e1.hasTotalPrefSectionConflict() ? e1.getTotalPrefSectionConflict() : Integer.valueOf(0)).compareTo(e2.hasTotalPrefSectionConflict() ? e2.getTotalPrefSectionConflict() : Integer.valueOf(0));
			case OVERRIDE:
				cmp = (e1.getOverrideNeeded() == null ? Integer.valueOf(0) : e1.getOverrideNeeded()).compareTo(e2.getOverrideNeeded() == null ? 0 : e2.getOverrideNeeded());
				if (cmp != 0) return - cmp;
				return (e1.getTotalOverrideNeeded() == null ? Integer.valueOf(0) : e1.getTotalOverrideNeeded()).compareTo(e2.getTotalOverrideNeeded() == null ? 0 : e2.getTotalOverrideNeeded());
			case ADVISOR:
				return e1.getStudent().getAdvisor("|").compareTo(e2.getStudent().getAdvisor("|"));
			case ADVISED_PERC:
				return Float.compare(e1.getAdvisedInfo() == null ? -1f : e1.getAdvisedInfo().getPercentage(), e2.getAdvisedInfo() == null ? -1f : e2.getAdvisedInfo().getPercentage());
			case ADVISED_CRED:
				cmp = Float.compare(e1.getAdvisedInfo() == null ? -1f : e1.getAdvisedInfo().getMinCredit(), e2.getAdvisedInfo() == null ? -1f : e2.getAdvisedInfo().getMinCredit());
				if (cmp != 0) return cmp;
				return Float.compare(e1.getAdvisedInfo() == null ? -1f : e1.getAdvisedInfo().getMaxCredit(), e2.getAdvisedInfo() == null ? -1f : e2.getAdvisedInfo().getMaxCredit());
			case ADVISED_CRIT:
				cmp = Integer.compare(e1.getAdvisedInfo() == null ? 0 : e1.getAdvisedInfo().getMissingCritical(), e2.getAdvisedInfo() == null ? 0 : e2.getAdvisedInfo().getMissingCritical());
				if (cmp != 0) return cmp;
				return Integer.compare(e1.getAdvisedInfo() == null ? 0 : e1.getAdvisedInfo().getMissingPrimary(), e2.getAdvisedInfo() == null ? 0 : e2.getAdvisedInfo().getMissingPrimary());
			case ADVISED_ASSGN:
				cmp = Integer.compare(e1.getAdvisedInfo() == null ? 0 : e1.getAdvisedInfo().getNotAssignedCritical(), e2.getAdvisedInfo() == null ? 0 : e2.getAdvisedInfo().getNotAssignedCritical());
				if (cmp != 0) return cmp;
				return Integer.compare(e1.getAdvisedInfo() == null ? 0 : e1.getAdvisedInfo().getNotAssignedPrimary(), e2.getAdvisedInfo() == null ? 0 : e2.getAdvisedInfo().getNotAssignedPrimary());
			case PREF:
				return (e1.hasPreference() ? e1.getPreference() : "").compareTo(e2.hasPreference() ? e2.getPreference() : "");
			case PIN:
				return (e1.hasPinReleased() ? e1.getPin() : "").compareTo(e2.hasPinReleased() ? e2.getPin() : "");
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
				return - (e1.getWallTime() == null ? Long.valueOf(0) : e1.getWallTime()).compareTo(e2.getWallTime() == null ? 0 : e2.getWallTime());
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
	
	private void sendEmail(final Iterator<Long> studentIds, final String subject, final String message, final String cc, final int fails, final boolean courseRequests, final boolean classSchedule, final boolean advisorRequests, final Boolean toggle) {
		if (!studentIds.hasNext()) return;
		final Long studentId = studentIds.next();
		iSectioningService.sendEmail(null, studentId, subject, message, cc, courseRequests, classSchedule, advisorRequests, toggle, "user-dashboard", new AsyncCallback<Boolean>() {

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
				sendEmail(studentIds, subject, message, cc, fails + 1, courseRequests, classSchedule, advisorRequests, toggle);
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
					sendEmail(studentIds, subject, message, cc, fails, courseRequests, classSchedule, advisorRequests, toggle);
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
	
	static class CourseInfoVisibleColums {
		boolean hasReservation = false;
		boolean hasSnapshot = false;
		
		public CourseInfoVisibleColums(List<EnrollmentInfo> result) {
			for (EnrollmentInfo e: result) {
				if (hasReservedSpace(e)) hasReservation = true;
				if (e.getSnapshot() != null) hasSnapshot = true;
			}
		}
	}
	
	static class StudentsInfoVisibleColumns {
		boolean hasEnrollment = false, hasWaitList = false,  hasArea = false, hasMajor = false, hasGroup = false, hasAcmd = false, hasReservation = false,
				hasRequestedDate = false, hasEnrolledDate = false, hasConsent = false, hasCredit = false, hasReqCred = false, hasDistances = false, hasOverlaps = false,
				hasFreeTimeOverlaps = false, hasPrefIMConfs = false, hasPrefSecConfs = false, hasNote = false, hasEmailed = false, hasOverride = false, hasExtId = false,
				hasAdvisor = false, hasAdvisedInfo = false, hasMinor = false, hasConc = false, hasDeg = false, hasProg = false, hasCamp = false, hasPref = false, hasPin = false;
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
				if (e.getStudent().hasAdvisor()) hasAdvisor = true;
				if (e.getAdvisedInfo() != null) hasAdvisedInfo = true;
				if (e.getStudent().hasMinor()) hasMinor = true;
				if (e.getStudent().hasConcentration()) hasConc = true;
				if (e.getStudent().hasDegree()) hasDeg = true;
				if (e.getStudent().hasProgram()) hasProg = true;
				if (e.getStudent().hasCampus()) hasCamp = true;
				if (e.hasPreference()) hasPref = true;
				if (e.hasPinReleased()) hasPin = true;
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
	
	private static class Groups extends P implements TakesValue<Collection<ClassAssignmentInterface.Group>> {
		private Collection<ClassAssignmentInterface.Group> iGroups = null;
		
		private Groups(Collection<ClassAssignmentInterface.Group> groups) {
			setValue(groups);
		}

		@Override
		public void setValue(Collection<Group> value) {
			iGroups = value;
			clear();
			if (iGroups != null && !iGroups.isEmpty()) {
				for (ClassAssignmentInterface.Group group: iGroups) {
					P g = new P(); g.setText(group.getName());
					if (group.hasTitle()) g.setTitle(group.getTitle());
					add(g);
				}
			}
		}

		@Override
		public Collection<Group> getValue() { return iGroups; }
	}
	
	private static class ACM extends P implements TakesValue<Collection<ClassAssignmentInterface.CodeLabel>> {
		private Collection<ClassAssignmentInterface.CodeLabel> iGroups = null;
		
		private ACM(Collection<ClassAssignmentInterface.CodeLabel> groups) {
			setValue(groups);
			getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
		}

		@Override
		public void setValue(Collection<ClassAssignmentInterface.CodeLabel> value) {
			iGroups = value;
			clear();
			if (iGroups != null && !iGroups.isEmpty()) {
				for (ClassAssignmentInterface.CodeLabel group: iGroups) {
					P g = new P();
					if (group.hasCode())
						g.setText(group.getCode());
					else
						g.setHTML("&nbsp;");
					if (group.hasLabel()) g.setTitle(group.getLabel());
					add(g);
				}
			}
		}

		@Override
		public Collection<ClassAssignmentInterface.CodeLabel> getValue() { return iGroups; }
	}
	
	static {
		createTriggers();
	}
	
	public static native void createTriggers()/*-{
		$wnd.gwtPropertyClick = function(source) {
			var textArea = $doc.createElement("textarea");
			try {
				textArea.value = JSON.stringify(JSON.parse(source.innerText), null, 2);  
			} catch (err) {
				textArea.value = source.innerText;
			}
			textArea.style.top = "0";
			textArea.style.left = "0";
			textArea.style.position = "fixed";
			$doc.body.appendChild(textArea);
			textArea.focus();
			textArea.select();
			try {
				$doc.execCommand('copy');
				var resource = @org.unitime.timetable.gwt.client.sectioning.SectioningStatusPage::MESSAGES;
				var msg = resource.@org.unitime.timetable.gwt.resources.StudentSectioningMessages::changeLogPropertyValueCopiedToClipbard()();
				$wnd.gwtShowMessage(msg);
			} catch (err) {
				$wnd.console.error('Oops, unable to copy', err);
			}
			$doc.body.removeChild(textArea);
		};
	}-*/;
}
