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
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable.TopCell;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTabPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasColSpan;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.EnrollmentInfo;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.SectioningAction;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.StudentInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.SectioningProperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
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
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.requestDateFormat());
	private static DateTimeFormat sTSF = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);

	private SectioningStatusFilterBox iFilter;

	private Button iSearch = null;
	private Image iLoadingImage = null;

	private VerticalPanel iSectioningPanel = null;
	
	private VerticalPanel iPanel = null;
	private HorizontalPanel iFilterPanel = null;
	
	private UniTimeTable<EnrollmentInfo> iCourseTable = null;
	private UniTimeTable<StudentInfo> iStudentTable = null;
	private UniTimeTable<SectioningAction> iLogTable = null;
	
	private UniTimeDialogBox iEnrollmentDialog = null;
	private EnrollmentTable iEnrollmentTable = null;
	private ScrollPanel iEnrollmentScroll = null;
	private UniTimeTabPanel iTabPanel = null;
	private int iTabIndex = 0;
	private FocusPanel iTabPanelWithFocus = null;
	private SectioningProperties iProperties = null;
	
	private HTML iError = null, iCourseTableHint, iStudentTableHint;
	private String iLastFilterOnEnter = null, iCourseFilter = null;
	private SectioningStatusFilterRpcRequest iCourseFilterRequest = null;
	private Map<String, String> iStates = null;
	private int iStatusColumn = 0;
	private UniTimeTextBox iSubject, iCC;
	private UniTimeWidget<ListBox> iStatus;
	private TextArea iMessage;
	private Set<Long> iSelectedStudentIds = new HashSet<Long>();
	private boolean iOnline; 
	
	public SectioningStatusPage(boolean online) {
		iOnline = online;

		iPanel = new VerticalPanel();
		iSectioningPanel = new VerticalPanel();
		
		iFilterPanel = new HorizontalPanel();
		iFilterPanel.setSpacing(3);
		
		Label filterLabel = new Label(MESSAGES.filter());
		iFilterPanel.add(filterLabel);
		iFilterPanel.setCellVerticalAlignment(filterLabel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iFilter = new SectioningStatusFilterBox(online);
		iFilterPanel.add(iFilter);
		
		iSearch = new Button(MESSAGES.buttonSearch());
		iSearch.setAccessKey('s');
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iSearch);		
		iFilterPanel.setCellVerticalAlignment(iSearch, HasVerticalAlignment.ALIGN_TOP);
		
		iLoadingImage = new Image(RESOURCES.loading_small());
		iLoadingImage.setVisible(false);
		iFilterPanel.add(iLoadingImage);
		iFilterPanel.setCellVerticalAlignment(iLoadingImage, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iSectioningPanel.add(iFilterPanel);
		iSectioningPanel.setCellHorizontalAlignment(iFilterPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		iCourseTable = new UniTimeTable<EnrollmentInfo>();
		iStudentTable = new UniTimeTable<StudentInfo>();
		iLogTable = new UniTimeTable<SectioningAction>();

		VerticalPanel courseTableWithHint = new VerticalPanel();
		courseTableWithHint.add(iCourseTable);
		iCourseTableHint = new HTML(MESSAGES.sectioningStatusReservationHint());
		iCourseTableHint.setStyleName("unitime-Hint");
		courseTableWithHint.add(iCourseTableHint);
		courseTableWithHint.setCellHorizontalAlignment(iCourseTableHint, HasHorizontalAlignment.ALIGN_RIGHT);
		
		VerticalPanel studentTableWithHint = new VerticalPanel();
		studentTableWithHint.add(iStudentTable);
		iStudentTableHint = new HTML(MESSAGES.sectioningStatusPriorityHint());
		iStudentTableHint.setStyleName("unitime-Hint");
		studentTableWithHint.add(iStudentTableHint);
		studentTableWithHint.setCellHorizontalAlignment(iStudentTableHint, HasHorizontalAlignment.ALIGN_RIGHT);

		iTabPanel = new UniTimeTabPanel();
		iTabPanel.add(courseTableWithHint, MESSAGES.tabEnrollments(), true);
		iTabPanel.selectTab(0);
		iTabPanel.add(studentTableWithHint, MESSAGES.tabStudents(), true);
		iTabPanel.setVisible(false);
		
		iTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				iTabIndex = event.getSelectedItem();
				loadDataIfNeeded();
			}
		});

		iTabPanelWithFocus = new FocusPanel(iTabPanel);
		iTabPanelWithFocus.setStyleName("unitime-FocusPanel");
		iSectioningPanel.add(iTabPanelWithFocus);
		
		iTabPanelWithFocus.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='e' || event.getNativeKeyCode()=='E')) {
					iTabPanel.selectTab(0);
					event.preventDefault();
				}
				if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='s' || event.getNativeKeyCode()=='S')) {
					iTabPanel.selectTab(1);
					event.preventDefault();
				}
				if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='l' || event.getNativeKeyCode()=='L')) {
					iTabPanel.selectTab(2);
					event.preventDefault();
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
				iCourseTable.clearTable();
				iStudentTable.clearTable();
				iLogTable.clearTable();
				if (event.getValue().endsWith("@")) {
					iFilter.setValue(event.getValue().substring(0, event.getValue().length() - 1));
					iTabPanel.selectTab(1);
				} else if (event.getValue().endsWith("$")) {
					iFilter.setValue(event.getValue().substring(0, event.getValue().length() - 1));
					iTabPanel.selectTab(2);
				} else {
					iFilter.setValue(event.getValue());
					if (iTabIndex != 0)
						iTabPanel.selectTab(0);
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
		
		
		if (iOnline) {
			iSectioningService.getProperties(null, new AsyncCallback<SectioningProperties>() {
				@Override
				public void onSuccess(SectioningProperties result) {
					iProperties = result;
					if (iProperties.isChangeLog())
						iTabPanel.add(iLogTable, MESSAGES.tabChangeLog(), true);
					checkLastQuery();
				}

				@Override
				public void onFailure(Throwable caught) {
					iError.setHTML(caught.getMessage());
					iError.setVisible(true);
				}
			});
		} else {
			checkLastQuery();
		}
		
		iStatus = new UniTimeWidget<ListBox>(new ListBox());
		iStatus.getWidget().addItem("");
		iStatus.getWidget().setSelectedIndex(0);
		
		iSectioningService.lookupStudentSectioningStates(new AsyncCallback<Map<String,String>>() {

			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(Map<String, String> result) {
				iStates = result;
				for (final String ref: new TreeSet<String>(iStates.keySet())) {
					iStatus.getWidget().addItem(iStates.get(ref), ref);
				}
				for (int i = 0; i < iStatus.getWidget().getItemCount(); i++) {
					if ("Cancelled".equalsIgnoreCase(iStatus.getWidget().getValue(i))) {
						iStatus.getWidget().setSelectedIndex(i);
						break;
					}
				}
			}
		});
		
		iSubject = new UniTimeTextBox(512, 473);
		iSubject.setText(MESSAGES.defaulSubject());
		iCC = new UniTimeTextBox(512, 473);
		iMessage = new TextArea();
		iMessage.setStyleName("unitime-TextArea");
		iMessage.setVisibleLines(10);
		iMessage.setCharacterWidth(80);

	}
	
	private void checkLastQuery() {
		if (Window.Location.getParameter("q") != null) {
			iFilter.setValue(Window.Location.getParameter("q"), true);
			if (Window.Location.getParameter("t") != null) {
				if ("2".equals(Window.Location.getParameter("t"))) {
					iTabPanel.selectTab(1);
				} else {
					iTabPanel.selectTab(0);
				}
			} else {
				loadData();
			}
		} else if (Window.Location.getHash() != null && !Window.Location.getHash().isEmpty()) {
			String hash = URL.decode(Window.Location.getHash().substring(1));
			if (!hash.matches("^[0-9]+\\:?[0-9]*@?$")) {
				if (hash.endsWith("@")) {
					iFilter.setValue(hash.substring(0, hash.length() - 1), true);
					iTabPanel.selectTab(1);
				} else if (hash.endsWith("$")) {
					iFilter.setValue(hash.substring(0, hash.length() - 1), true);
					iTabPanel.selectTab(2);
				} else {
					iFilter.setValue(hash, true);
					loadData();
				}
			}
		} else {
			String q = SectioningStatusCookie.getInstance().getQuery(iOnline);
			if (q != null) iFilter.setValue(q);
			int t = SectioningStatusCookie.getInstance().getTab(iOnline);
			if (t >= 0 && t < iTabPanel.getTabCount())
				iTabPanel.selectTab(t, false);
			if (GWT_CONSTANTS.searchWhenPageIsLoaded() && q != null && !q.isEmpty())
				loadData();
		}
	}
	
	private void setLoading(boolean loading) {
		iLoadingImage.setVisible(loading);
		iSearch.setVisible(!loading);
	}
	
	private void loadData() {
		iCourseTable.clearTable();
		iStudentTable.clearTable();
		iLogTable.clearTable();
		loadDataIfNeeded();
	}
	
	private void loadDataIfNeeded() {
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
					iTabPanel.setVisible(false);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(List<EnrollmentInfo> result) {
					if (result.isEmpty()) {
						iError.setHTML(MESSAGES.exceptionNoMatchingResultsFound(iCourseFilter));
						iError.setVisible(true);
						iTabPanel.setVisible(false);
					} else {
						populateCourseTable(result);
						iTabPanel.setVisible(true);
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
						iTabPanel.setVisible(false);
						ToolBox.checkAccess(caught);
					}

					@Override
					public void onSuccess(List<StudentInfo> result) {
						if (result.isEmpty()) {
							iError.setHTML(MESSAGES.exceptionNoMatchingResultsFound(iCourseFilter));
							iError.setVisible(true);
							iTabPanel.setVisible(false);
						} else {
							populateStudentTable(result);
							iTabPanel.setVisible(true);
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
						iTabPanel.setVisible(false);
						ToolBox.checkAccess(caught);
					}

					@Override
					public void onSuccess(List<StudentInfo> result) {
						if (result.isEmpty()) {
							iError.setHTML(MESSAGES.exceptionNoMatchingResultsFound(iCourseFilter));
							iError.setVisible(true);
							iTabPanel.setVisible(false);
						} else {
							populateStudentTable(result);
							iTabPanel.setVisible(true);
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
					iTabPanel.setVisible(false);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(final List<SectioningAction> result) {
					populateChangeLog(result);
					iTabPanel.setVisible(true);
					setLoading(false);
					LoadingWidget.getInstance().hide();
				}
			});
		}
	}
	
	private List<Widget> line(final EnrollmentInfo e) {
		List<Widget> line = new ArrayList<Widget>();
		if (e.getConfigId() == null) {
			if (e.getCourseId() != null) {
				final Image showDetails = new Image(RESOURCES.treeClosed());
				showDetails.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						final int row = iCourseTable.getCellForEvent(event).getRowIndex();
						if (row + 1 == iCourseTable.getRowCount() || iCourseTable.getData(row + 1).getConfigId() == null) { // open
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
									setLoading(false);
									int r = row + 1;
									for (EnrollmentInfo e: result) {
										iCourseTable.insertRow(r);
										iCourseTable.setRow(r, e, line(e));
										r++;
									}
								}
							});
						} else {
							for (int r = row + 1; r < iCourseTable.getRowCount(); r++) {
								if (iCourseTable.getData(r).getConfigId() == null) break;
								iCourseTable.getRowFormatter().setVisible(r, !iCourseTable.getRowFormatter().isVisible(r));
							}
							showDetails.setResource(RESOURCES.treeClosed());
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
			line.add(new Label(!e.getAssignment().hasRoom() ? "" : e.getAssignment().getRooms(","), false));
		}
		if (e.getCourseId() == null)
			line.add(new NumberCell(e.getAvailable(), e.getLimit()));
		else
			line.add(new AvailableCell(e));
		line.add(new NumberCell(null, e.getProjection()));
		line.add(new NumberCell(e.getEnrollment(), e.getTotalEnrollment()));
		line.add(new WaitListCell(e));
		line.add(new NumberCell(e.getReservation(), e.getTotalReservation()));
		line.add(new NumberCell(e.getConsentNeeded(), e.getTotalConsentNeeded()));
		return line;
	}
	
	public void populateCourseTable(List<EnrollmentInfo> result) {
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
		header.add(hLimit);
		addSortOperation(hLimit, EnrollmentComparator.SortBy.LIMIT, MESSAGES.colAvailable());

		UniTimeTableHeader hProjection = new UniTimeTableHeader(MESSAGES.colProjection());
		header.add(hProjection);
		addSortOperation(hProjection, EnrollmentComparator.SortBy.PROJECTION, MESSAGES.colProjection());

		UniTimeTableHeader hEnrollment = new UniTimeTableHeader(MESSAGES.colEnrollment());
		header.add(hEnrollment);
		addSortOperation(hEnrollment, EnrollmentComparator.SortBy.ENROLLMENT, MESSAGES.colEnrollment());

		UniTimeTableHeader hWaitListed = new UniTimeTableHeader(MESSAGES.colWaitListed());
		header.add(hWaitListed);
		addSortOperation(hWaitListed, EnrollmentComparator.SortBy.WAITLIST, MESSAGES.colWaitListed());

		
		UniTimeTableHeader hReserved = new UniTimeTableHeader(MESSAGES.colReserved());
		header.add(hReserved);
		addSortOperation(hReserved, EnrollmentComparator.SortBy.RESERVATION, MESSAGES.colReserved());

		UniTimeTableHeader hConsent = new UniTimeTableHeader(MESSAGES.colNeedConsent());
		header.add(hConsent);
		addSortOperation(hConsent, EnrollmentComparator.SortBy.NEED_CONSENT, MESSAGES.colNeedConsent().replace("<br>", " "));

		iCourseTable.addRow(null, header);
		
		boolean hasReservation = false;
		for (EnrollmentInfo e: result) {
			iCourseTable.addRow(e, line(e));
			if (!hasReservation && AvailableCell.hasReservedSpace(e)) hasReservation = true;
			iCourseTable.getRowFormatter().getElement(iCourseTable.getRowCount() - 1).getStyle().setBackgroundColor("#f3f3f3");
		}
		
		// Total line
		if (iCourseTable.getRowCount() >= 2) {
			for (int c = 0; c < iCourseTable.getCellCount(iCourseTable.getRowCount() - 1); c++)
				iCourseTable.getCellFormatter().setStyleName(iCourseTable.getRowCount() - 1, c, "unitime-TotalRow");
			iCourseTable.getRowFormatter().getElement(iCourseTable.getRowCount() - 1).getStyle().clearBackgroundColor();
		}
		
		iCourseTableHint.setVisible(hasReservation);
		
		if (SectioningStatusCookie.getInstance().getSortBy(iOnline, 0) != 0) {
			boolean asc = (SectioningStatusCookie.getInstance().getSortBy(iOnline, 0) > 0);
			EnrollmentComparator.SortBy sort = EnrollmentComparator.SortBy.values()[Math.abs(SectioningStatusCookie.getInstance().getSortBy(iOnline, 0)) - 1];
			switch (sort) {
			case COURSE:
				iCourseTable.sort(hCourse, new EnrollmentComparator(sort), asc); break;
			case SUBJECT:
				iCourseTable.sort(hSubject, new EnrollmentComparator(sort), asc); break;
			case TITLE:
				iCourseTable.sort(hTitleSubpart, new EnrollmentComparator(sort), asc); break;
			case CONSENT:
				iCourseTable.sort(hRoom, new EnrollmentComparator(sort), asc); break;
			case LIMIT:
				iCourseTable.sort(hLimit, new EnrollmentComparator(sort), asc); break;
			case PROJECTION:
				iCourseTable.sort(hProjection, new EnrollmentComparator(sort), asc); break;
			case ENROLLMENT:
				iCourseTable.sort(hEnrollment, new EnrollmentComparator(sort), asc); break;
			case NEED_CONSENT:
				iCourseTable.sort(hConsent, new EnrollmentComparator(sort), asc); break;
			case RESERVATION:
				iCourseTable.sort(hReserved, new EnrollmentComparator(sort), asc); break;
			case WAITLIST:
				iCourseTable.sort(hWaitListed, new EnrollmentComparator(sort), asc); break;
			}
		}
	}
	
	public void populateStudentTable(List<StudentInfo> result) {
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
					return iSelectedStudentIds.size() != iStudentTable.getRowCount() + 2;
				}
				@Override
				public void execute() {
					iSelectedStudentIds.clear();
					for (int row = 0; row < iStudentTable.getRowCount(); row++) {
						StudentInfo i = iStudentTable.getData(row);
						if (i != null && i.getStudent() != null) {
							((CheckBox)iStudentTable.getWidget(row, 0)).setValue(true);
							iSelectedStudentIds.add(i.getStudent().getId());
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
						StudentInfo i = iStudentTable.getData(row);
						if (i != null && i.getStudent() != null)
							((CheckBox)iStudentTable.getWidget(row, 0)).setValue(false);
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
					return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isEmail();
				}
				@Override
				public void execute() {
					SimpleForm sf = new SimpleForm();
					final UniTimeHeaderPanel buttons = new UniTimeHeaderPanel();
					sf.removeStyleName("unitime-NotPrintableBottomLine");
					sf.addRow(MESSAGES.emailSubject(), iSubject);
					if (iSubject.getText().isEmpty() || iSubject.getText().equals(MESSAGES.defaulSubjectMassCancel()))
						iSubject.setText(MESSAGES.defaulSubject());
					sf.addRow(MESSAGES.emailCC(), iCC);
					sf.addRow(MESSAGES.emailBody(), iMessage);
					sf.addBottomRow(buttons);
					final UniTimeDialogBox dialog = new UniTimeDialogBox(true, false);
					dialog.setWidget(sf);
					dialog.setText(MESSAGES.sendStudentEmail());
					dialog.setEscapeToHide(true);
					buttons.addButton("send", MESSAGES.emailSend(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							List<Long> studentIds = new ArrayList<Long>();
							for (int row = 0; row < iStudentTable.getRowCount(); row++) {
								StudentInfo i = iStudentTable.getData(row);
								if (i != null && i.getStudent() != null && iSelectedStudentIds.contains(i.getStudent().getId())) { 
									studentIds.add(i.getStudent().getId());
									iStudentTable.setWidget(row, iStudentTable.getCellCount(row) - 1, new Image(RESOURCES.loading_small()));
								}
							}
							dialog.hide();
							sendEmail(studentIds.iterator(), iSubject.getText(), iMessage.getText(), iCC.getText(), 0);
						}
					});
					buttons.addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							dialog.hide();
						}
					});
					dialog.center();
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
					return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isMassCancel();
				}
				@Override
				public void execute() {
					SimpleForm sf = new SimpleForm();
					final UniTimeHeaderPanel buttons = new UniTimeHeaderPanel();
					sf.removeStyleName("unitime-NotPrintableBottomLine");
					if (iSubject.getText().isEmpty() || iSubject.getText().equals(MESSAGES.defaulSubject()))
						iSubject.setText(MESSAGES.defaulSubjectMassCancel());
					sf.addRow(MESSAGES.emailSubject(), iSubject);
					sf.addRow(MESSAGES.emailCC(), iCC);
					sf.addRow(MESSAGES.emailBody(), iMessage);
					sf.addRow(MESSAGES.newStatus(), iStatus);
					sf.addBottomRow(buttons);
					final UniTimeDialogBox dialog = new UniTimeDialogBox(true, false);
					dialog.setWidget(sf);
					dialog.setText(MESSAGES.massCancel());
					dialog.setEscapeToHide(true);
					buttons.addButton("cancel", MESSAGES.buttonMassCancel(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (!Window.confirm(MESSAGES.massCancelConfirmation())) {
								dialog.hide();
								return;
							}
							
							final List<Long> studentIds = new ArrayList<Long>();
							for (int row = 0; row < iStudentTable.getRowCount(); row++) {
								StudentInfo i = iStudentTable.getData(row);
								if (i != null && i.getStudent() != null && iSelectedStudentIds.contains(i.getStudent().getId())) { 
									studentIds.add(i.getStudent().getId());
									iStudentTable.setWidget(row, iStudentTable.getCellCount(row) - 1, new Image(RESOURCES.loading_small()));
								}
							}
							dialog.hide();
							
							LoadingWidget.getInstance().show(MESSAGES.massCanceling());
							iSectioningService.massCancel(studentIds, iStatus.getWidget().getValue(iStatus.getWidget().getSelectedIndex()),
									iSubject.getText(), iMessage.getText(), iCC.getText(), new AsyncCallback<Boolean>() {

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
					buttons.addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							dialog.hide();
						}
					});
					dialog.center();
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
			if (iStates != null) {
				for (final String ref: new TreeSet<String>(iStates.keySet())) {
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return MESSAGES.changeStatusTo(iStates.get(ref));
						}
						@Override
						public boolean hasSeparator() {
							return "".equals(ref);
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() > 0 && iProperties != null && iProperties.isChangeStatus();
						}
						@Override
						public void execute() {
							List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
							LoadingWidget.getInstance().show(MESSAGES.changingStatusTo(iStates.get(ref)));
							iSectioningService.changeStatus(studentIds, ref, new AsyncCallback<Boolean>() {

								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
								}

								@Override
								public void onSuccess(Boolean result) {
									for (int row = 0; row < iStudentTable.getRowCount(); row++) {
										StudentInfo i = iStudentTable.getData(row);
										if (i != null && i.getStudent() != null && ((CheckBox)iStudentTable.getWidget(row, 0)).getValue()) { 
											i.setStatus(ref);
											((HTML)iStudentTable.getWidget(row, iStatusColumn)).setHTML(ref);
										}
									}
									LoadingWidget.getInstance().hide();
								}
							});
						}
					});
				}
			}
		}
		
		boolean hasExtId = false;
		for (ClassAssignmentInterface.StudentInfo e: result) {
			if (e.getStudent() != null && e.getStudent().isCanShowExternalId()) { hasExtId = true; break; }
		}
		
		UniTimeTableHeader hExtId = null;
		if (hasExtId) {
			hExtId = new UniTimeTableHeader(MESSAGES.colStudentExternalId());
			header.add(hExtId);
			addSortOperation(hExtId, StudentComparator.SortBy.EXTERNAL_ID, MESSAGES.colStudentExternalId());
		}
		
		UniTimeTableHeader hStudent = new UniTimeTableHeader(MESSAGES.colStudent());
		header.add(hStudent);
		addSortOperation(hStudent, StudentComparator.SortBy.STUDENT, MESSAGES.colStudent());
		
		UniTimeTableHeader hTotal = new UniTimeTableHeader("&nbsp;");
		header.add(hTotal);
		
		boolean hasEnrollment = false, hasWaitList = false,  hasArea = false, hasMajor = false, hasGroup = false, hasAcmd = false, hasReservation = false,
				hasRequestedDate = false, hasEnrolledDate = false, hasConsent = false, hasCredit = false;
		for (ClassAssignmentInterface.StudentInfo e: result) {
			if (e.getStudent() == null) continue;
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
			if (e.hasTotalCredit()) hasCredit = true;
		}
		
		UniTimeTableHeader hArea = null, hClasf = null;
		if (hasArea) {
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
		if (hasMajor) {
			hMajor = new UniTimeTableHeader(MESSAGES.colMajor());
			//hMajor.setWidth("100px");
			header.add(hMajor);
			addSortOperation(hMajor, StudentComparator.SortBy.MAJOR, MESSAGES.colMajor());
		}
		
		UniTimeTableHeader hGroup = null;
		if (hasGroup) {
			hGroup = new UniTimeTableHeader(MESSAGES.colGroup());
			//hGroup.setWidth("100px");
			header.add(hGroup);
			addSortOperation(hMajor, StudentComparator.SortBy.GROUP, MESSAGES.colGroup());
		}
		
		UniTimeTableHeader hAcmd = null;
		if (hasAcmd) {
			hAcmd = new UniTimeTableHeader(MESSAGES.colAccommodation());
			//hGroup.setWidth("100px");
			header.add(hAcmd);
			addSortOperation(hMajor, StudentComparator.SortBy.ACCOMODATION, MESSAGES.colAccommodation());
		}
		
		iStatusColumn = header.size() - 1;
		UniTimeTableHeader hStatus = new UniTimeTableHeader(MESSAGES.colStatus());
		//hMajor.setWidth("100px");
		header.add(hStatus);
		addSortOperation(hStatus, StudentComparator.SortBy.STATUS, MESSAGES.colStatus());
		
		UniTimeTableHeader hEnrollment = null;
		if (hasEnrollment) {
			hEnrollment = new UniTimeTableHeader(MESSAGES.colEnrollment());
			header.add(hEnrollment);
			addSortOperation(hEnrollment, StudentComparator.SortBy.ENROLLMENT, MESSAGES.colEnrollment());
		}
		
		UniTimeTableHeader hWaitlist = null;
		if (hasWaitList) {
			hWaitlist = new UniTimeTableHeader(MESSAGES.colWaitListed());
			header.add(hWaitlist);
			addSortOperation(hWaitlist, StudentComparator.SortBy.WAITLIST, MESSAGES.colWaitListed());
		}
		
		UniTimeTableHeader hReservation = null;
		if (hasReservation) {
			hReservation = new UniTimeTableHeader(MESSAGES.colReservation());
			header.add(hReservation);
			addSortOperation(hReservation, StudentComparator.SortBy.RESERVATION, MESSAGES.colReservation());
		}
		
		UniTimeTableHeader hConsent = null;
		if (hasConsent) {
			hConsent = new UniTimeTableHeader(MESSAGES.colConsent());
			header.add(hConsent);
			addSortOperation(hConsent, StudentComparator.SortBy.CONSENT, MESSAGES.colConsent());
		}

		UniTimeTableHeader hCredit = null;
		if (hasCredit) {
			hCredit = new UniTimeTableHeader(MESSAGES.colCredit());
			header.add(hCredit);
			addSortOperation(hCredit, StudentComparator.SortBy.CREDIT, MESSAGES.colCredit());
		}
		
		UniTimeTableHeader hRequestTS = null;
		if (hasRequestedDate) {
			hRequestTS = new UniTimeTableHeader(MESSAGES.colRequestTimeStamp());
			header.add(hRequestTS);
			addSortOperation(hRequestTS, StudentComparator.SortBy.REQUEST_TS, MESSAGES.colRequestTimeStamp());
		}
		
		UniTimeTableHeader hEnrolledTS = null;
		if (hasEnrolledDate) {
			hEnrolledTS = new UniTimeTableHeader(MESSAGES.colEnrollmentTimeStamp());
			header.add(hEnrolledTS);
			addSortOperation(hEnrolledTS, StudentComparator.SortBy.ENROLLMENT_TS, MESSAGES.colEnrollmentTimeStamp());
		}
		
		UniTimeTableHeader hEmailTS = null;
		if (iOnline) {
			hEmailTS = new UniTimeTableHeader(MESSAGES.colEmailTimeStamp());
			header.add(hEmailTS);
			addSortOperation(hEmailTS, StudentComparator.SortBy.EMAIL_TS, MESSAGES.colEmailTimeStamp());
		}
		
		iStudentTable.addRow(null, header);
		
		Set<Long> newlySelected = new HashSet<Long>();
		for (StudentInfo info: result) {
			List<Widget> line = new ArrayList<Widget>();
			if (info.getStudent() != null) {
				if (iOnline && iProperties != null && iProperties.isCanSelectStudent()) {
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
						newlySelected.add(sid);
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
				}
				if (hasExtId) {
					line.add(new Label(info.getStudent().isCanShowExternalId() ? info.getStudent().getExternalId() : "", false));
				}
				line.add(new TitleCell(info.getStudent().getName()));
				if (hasArea) {
					line.add(new HTML(info.getStudent().getArea("<br>"), false));
					line.add(new HTML(info.getStudent().getClassification("<br>"), false));
				}
				if (hasMajor)
					line.add(new HTML(info.getStudent().getMajor("<br>"), false));
				if (hasGroup)
					line.add(new HTML(info.getStudent().getGroup("<br>"), false));
				if (hasAcmd)
					line.add(new HTML(info.getStudent().getAccommodation("<br>"), false));
				line.add(new HTML(info.getStatus(), false));
			} else {
				if (iOnline && iProperties != null && iProperties.isCanSelectStudent()) line.add(new HTML("&nbsp;", false));
				if (hasExtId)
					line.add(new TitleCell(MESSAGES.total()));
				else
					line.add(new Label(MESSAGES.total()));
				line.add(new NumberCell(null, result.size() - 1));
				if (hasArea) {
					line.add(new HTML("&nbsp;", false));
					line.add(new HTML("&nbsp;", false));
				}
				if (hasMajor)
					line.add(new HTML("&nbsp;", false));
				if (hasGroup)
					line.add(new HTML("&nbsp;", false));
				if (hasAcmd)
					line.add(new HTML("&nbsp;", false));
				line.add(new HTML("&nbsp;", false));
			}
			if (hasEnrollment)
				line.add(new NumberCell(info.getEnrollment(), info.getTotalEnrollment()));
			if (hasWaitList)
				line.add(new WaitListCell(info));
			if (hasReservation)
				line.add(new NumberCell(info.getReservation(), info.getTotalReservation()));
			if (hasConsent)
				line.add(new NumberCell(info.getConsentNeeded(), info.getTotalConsentNeeded()));
			if (hasCredit)
				line.add(new CreditCell(info.getCredit(), info.getTotalCredit()));
			if (info.getStudent() != null) {
				if (hasRequestedDate)
					line.add(new HTML(info.getRequestedDate() == null ? "&nbsp;" : sDF.format(info.getRequestedDate()), false));
				if (hasEnrolledDate)
					line.add(new HTML(info.getEnrolledDate() == null ? "&nbsp;" : sDF.format(info.getEnrolledDate()), false));
				if (iOnline)
					line.add(new HTML(info.getEmailDate() == null ? "&nbsp;" : sDF.format(info.getEmailDate()), false));
			} else {
				if (hasRequestedDate)
					line.add(new HTML("&nbsp;", false));
				if (hasEnrolledDate)
					line.add(new HTML("&nbsp;", false));
				if (iOnline)
					line.add(new HTML("&nbsp;", false));
			}
			iStudentTable.addRow(info, line);
		}
		iSelectedStudentIds.clear();
		iSelectedStudentIds.addAll(newlySelected);
		
		if (iStudentTable.getRowCount() >= 2) {
			for (int c = 0; c < iStudentTable.getCellCount(iStudentTable.getRowCount() - 1); c++)
				iStudentTable.getCellFormatter().setStyleName(iStudentTable.getRowCount() - 1, c, "unitime-TotalRow");
		}
		
		iStudentTableHint.setVisible(hasWaitList);
		
		if (SectioningStatusCookie.getInstance().getSortBy(iOnline, 1) != 0) {
			boolean asc = (SectioningStatusCookie.getInstance().getSortBy(iOnline, 1) > 0);
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
			case GROUP: h = hGroup; break;
			case MAJOR: h = hMajor; break;
			case REQUEST_TS: h = hRequestTS; break;
			case RESERVATION: h = hReservation; break;
			case STATUS: h = hStatus; break;
			case STUDENT: h = hStudent; break;
			case WAITLIST: h = hWaitlist; break;
			}
			if (h != null)
				iStudentTable.sort(h, new StudentComparator(sort), asc);
		}
	}
	
	public void populateChangeLog(List<SectioningAction> result) {
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		
		UniTimeTableHeader hStudent = new UniTimeTableHeader(MESSAGES.colStudent());
		header.add(hStudent);
		addSortOperation(hStudent, ChangeLogComparator.SortBy.STUDENT, MESSAGES.colStudent());
		
		UniTimeTableHeader hOp = new UniTimeTableHeader(MESSAGES.colOperation());
		header.add(hOp);
		addSortOperation(hOp, ChangeLogComparator.SortBy.OPERATION, MESSAGES.colOperation());
		
		UniTimeTableHeader hTimeStamp = new UniTimeTableHeader(MESSAGES.colTimeStamp());
		header.add(hTimeStamp);
		addSortOperation(hTimeStamp, ChangeLogComparator.SortBy.TIME_STAMP, MESSAGES.colTimeStamp());
		
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
		
		for (ClassAssignmentInterface.SectioningAction log: result) {
			iLogTable.addRow(log,
					new TopCell(log.getStudent().getName()),
					new TopCell(log.getOperation()),
					new TopCell(sTSF.format(log.getTimeStamp())),
					new TopCell(log.getResult()),
					new TopCell(log.getUser() == null ? "" : log.getUser()),
					new HTML(log.getMessage() == null ? "" : log.getMessage())
			);
		}
		
		if (SectioningStatusCookie.getInstance().getSortBy(iOnline, 2) != 0) {
			boolean asc = (SectioningStatusCookie.getInstance().getSortBy(iOnline, 2) > 0);
			ChangeLogComparator.SortBy sort = ChangeLogComparator.SortBy.values()[Math.abs(SectioningStatusCookie.getInstance().getSortBy(iOnline, 2)) - 1];
			UniTimeTableHeader h = null;
			switch (sort) {
			case MESSAGE: h = hMessage; break;
			case OPERATION: h = hOp; break;
			case RESULT: h = hResult; break;
			case STUDENT: h = hStudent; break;
			case TIME_STAMP: h = hTimeStamp; break;
			case USER: h = hUser; break;
			}
			if (h != null)
				iLogTable.sort(h, new ChangeLogComparator(sort), asc);
		}
	}
	
	protected void addSortOperation(final UniTimeTableHeader header, final EnrollmentComparator.SortBy sort, final String column) {
		header.addOperation(new Operation() {
			@Override
			public void execute() {
				iCourseTable.sort(header, new EnrollmentComparator(sort));
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
		});
	}
	
	protected void addSortOperation(final UniTimeTableHeader header, final StudentComparator.SortBy sort, final String column) {
		header.addOperation(new Operation() {
			@Override
			public void execute() {
				iStudentTable.sort(header, new StudentComparator(sort));
				SectioningStatusCookie.getInstance().setSortBy(iOnline, 1, header.getOrder() ? 1 + sort.ordinal() : -1 - sort.ordinal());
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
		});
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
		header.addOperation(new Operation() {
			@Override
			public void execute() {
				iLogTable.sort(header, new ChangeLogComparator(sort));
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
		});
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
				e.hasUnassigned() ? e.getUnassigned() : 0,
				e.hasTotalUnassigned() ? e.getTotalUnassigned() : 0,
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
			NEED_CONSENT
		}
		
		private SortBy iSortBy;
		
		public EnrollmentComparator(SortBy sortBy) {
			iSortBy = sortBy;
		}

		@Override
		public int compare(EnrollmentInfo e1, EnrollmentInfo e2) {
			// Totals line is always last
			if (e1.getCourseId() == null) return 1;
			if (e2.getCourseId() == null) return -1;
			
			if (e1.getCourseId().equals(e2.getCourseId())) { // Same course
				// Course line first
				if (e1.getConfigId() == null) return -1;
				if (e2.getConfigId() == null) return -1;
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
				cmp = (e1.getUnassigned() == null ? new Integer(0) : e1.getUnassigned()).compareTo(e2.getUnassigned() == null ? 0 : e2.getUnassigned());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalWaitlist() == null ? new Integer(0) : e1.getTotalWaitlist()).compareTo(e2.getTotalWaitlist() == null ? 0 : e2.getTotalWaitlist());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalUnassigned() == null ? new Integer(0) : e1.getTotalUnassigned()).compareTo(e2.getTotalUnassigned() == null ? 0 : e2.getTotalUnassigned());
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
			;
		}
		
		private SortBy iSortBy;
		
		public StudentComparator(SortBy sortBy) {
			iSortBy = sortBy;
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
				cmp = e1.getStudent().getGroup("|").compareTo(e2.getStudent().getGroup("|"));
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
			case REQUEST_TS:
				return (e1.getRequestedDate() == null ? new Date(0) : e1.getRequestedDate()).compareTo(e2.getRequestedDate() == null ? new Date(0) : e2.getRequestedDate());
			case ENROLLMENT_TS:
				return (e1.getEnrolledDate() == null ? new Date(0) : e1.getEnrolledDate()).compareTo(e2.getEnrolledDate() == null ? new Date(0) : e2.getEnrolledDate());
			case EMAIL_TS:
				return (e1.getEmailDate() == null ? new Date(0) : e1.getEmailDate()).compareTo(e2.getEmailDate() == null ? new Date(0) : e2.getEmailDate());
			default:
				return 0;
			}
		}

		@Override
		public int compare(StudentInfo e1, StudentInfo e2) {
			if (e1.getStudent() == null) return 1;
			if (e2.getStudent() == null) return -1;
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
			case RESULT:
				return (e1.getResult() == null ? "" : e1.getResult()).compareTo(e2.getResult() == null ? "" : e2.getResult());
			case MESSAGE:
				return (e1.getMessage() == null ? "" : e1.getMessage()).compareTo(e2.getMessage() == null ? "" : e2.getMessage());
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
		
		public CreditCell(Float value, Float total) {
			super();
			if (total != null && total > 0f) {
				if (total.equals(value))
					setHTML(df.format(total));
				else
					setHTML(df.format(value) + " / " + df.format(total));
			} else {
				setHTML("&nbsp;");
			}
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
}
