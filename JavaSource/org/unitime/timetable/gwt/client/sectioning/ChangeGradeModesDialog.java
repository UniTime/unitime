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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaSuggestArea;
import org.unitime.timetable.gwt.client.aria.AriaTabBar;
import org.unitime.timetable.gwt.client.aria.AriaTextArea;
import org.unitime.timetable.gwt.client.aria.HasAriaLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.client.widgets.WebTable.Cell;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradeMode;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAvailableGradeModesRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAvailableGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationCreditChange;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationGradeMode;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationGradeModeChange;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationGradeModeChanges;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationStatus;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationVariableCredit;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationVariableCreditChange;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author Tomas Muller
 */
public class ChangeGradeModesDialog extends UniTimeDialogBox {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static GwtMessages GWT_MESSAGES = GWT.create(GwtMessages.class);
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private SimpleForm iForm;
	private WebTable iTable;
	private UniTimeHeaderPanel iButtons;
	private List<GradeModeChange> iChanges = new ArrayList<GradeModeChange>();
	private List<VariableCreditChange> iVarChanges = new ArrayList<VariableCreditChange>();
	private Focusable iFirstChange = null;
	private ScheduleStatus iStatus;
	private ArrayList<ClassAssignmentInterface.ClassAssignment> iEnrollment;
	private List<RetrieveSpecialRegistrationResponse> iApprovals;
	private P iApproval = null;
	private AriaTabBar iCoursesTab = null;
	private P iCoursesTabScroll = null;
	private Map<Integer, String> iTab2Course = new HashMap<Integer, String>();
	private Map<String, String> iCourse2Note = new HashMap<String, String>();
	private AriaTextArea iNote = null;
	private AriaSuggestArea iNoteWithSuggestions;
	private List<CheckBox> iDisclaimers = new ArrayList<CheckBox>();
	private Float iCurrentCredit, iMaxCredit;
	private List<String> iSuggestions = new ArrayList<String>();
	private StudentSectioningContext iContext;
	private P iCourseNotes = null;
	
	public ChangeGradeModesDialog(StudentSectioningContext context, ScheduleStatus status) {
		super(true, true);
		addStyleName("unitime-ChangeGradeModesDialog");
		setText(MESSAGES.dialogChangeGradeMode());
		iContext = context;
		iStatus = status;
		
		iForm = new SimpleForm();
		
		iTable = new WebTable();
		iTable.setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colSubject()),
				new WebTable.Cell(MESSAGES.colCourse()),
				new WebTable.Cell(MESSAGES.colSubpart()),
				new WebTable.Cell(MESSAGES.colClass()),
				new WebTable.Cell(MESSAGES.colDays()),
				new WebTable.Cell(MESSAGES.colStart()),
				new WebTable.Cell(MESSAGES.colEnd()),
				new WebTable.Cell(MESSAGES.colDate()),
				new WebTable.Cell(MESSAGES.colRoom()),
				new WebTable.Cell(MESSAGES.colCredit()),
				new WebTable.Cell(MESSAGES.colPendingCredit()),
				new WebTable.Cell(MESSAGES.colTitleGradeMode()),
				new WebTable.Cell(MESSAGES.colTitlePendingGradeMode())
				));
		iTable.setEmptyMessage(MESSAGES.emptyGradeChanges());
		
		ScrollPanel scroll = new ScrollPanel(iTable);
		scroll.setStyleName("unitime-ScrollPanel");
		scroll.addStyleName("class-table");
		iForm.addRow(scroll);
		
		iButtons = new UniTimeHeaderPanel();
		iButtons.addButton("submit", MESSAGES.buttonSubmitGradeModeChanges(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
				onSubmit();
			}
		});
		iButtons.addButton("cancel", MESSAGES.buttonCloseGradeModeChanges(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		iButtons.setEnabled("submit", false);
		
		iApproval = new P("approval-panel");
		iForm.addRow(iApproval);

		iForm.addBottomRow(iButtons);
		
		iCoursesTab = new AriaTabBar(); iCoursesTab.addStyleName("notes-tab");
		iCoursesTab.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				iCoursesTab.getTabElement(event.getSelectedItem()).scrollIntoView();
				String course = iTab2Course.get(event.getSelectedItem());
				String message = iCourse2Note.get(course);
				boolean show = iNoteWithSuggestions.isSuggestionListShowing();
				if (show) iNoteWithSuggestions.hideSuggestionList();
				iNote.setText(message == null ? "" : message);
				if (show) iNoteWithSuggestions.showSuggestions(iNote.getText());
				iNote.setAriaLabel(ARIA.requestNoteFor(course));
				AriaStatus.getInstance().setHTML(ARIA.requestNoteFor(course));
			}
		});
		iCoursesTabScroll = new P("notes-scroll"); iCoursesTabScroll.add(iCoursesTab);
		iCoursesTabScroll.getElement().getStyle().clearPosition();
		iCoursesTabScroll.getElement().getStyle().clearOverflow();
		
		iNote = new AriaTextArea();
		iNote.setStyleName("unitime-TextArea"); iNote.addStyleName("request-notes");
		iNote.setVisibleLines(5);
		iNote.setCharacterWidth(80);
		iNote.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iCourse2Note.put(iTab2Course.get(iCoursesTab.getSelectedTab()), event.getValue());
			}
		});
		iNote.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (iNoteWithSuggestions.isSuggestionListShowing()) return;
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_TAB && event.getNativeEvent().getShiftKey()) {
					if (iCoursesTab.getSelectedTab() > 0) {
						iCourse2Note.put(iTab2Course.get(iCoursesTab.getSelectedTab()), iNote.getText());
						iCoursesTab.selectTab(iCoursesTab.getSelectedTab() - 1, true);
						event.preventDefault();
					}
				} else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_TAB) {
					if (iCoursesTab.getSelectedTab() + 1 < iCoursesTab.getTabCount()) {
						iCourse2Note.put(iTab2Course.get(iCoursesTab.getSelectedTab()), iNote.getText());
						iCoursesTab.selectTab(iCoursesTab.getSelectedTab() + 1, true);
						event.preventDefault();
					}
				}
			}
		});
		iNoteWithSuggestions = new AriaSuggestArea(iNote, iSuggestions);
		iNoteWithSuggestions.addStyleName("request-note");
		iNoteWithSuggestions.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				String text = iNote.getText();
				if (text.indexOf('<') >= 0 && text.indexOf('>') > text.indexOf('<')) {
					iNote.setSelectionRange(text.indexOf('<'), text.indexOf('>') - text.indexOf('<') + 1);
				}
			}
		});
		iCourseNotes = new P("course-notes");
		iCourseNotes.add(iCoursesTabScroll);
		iCourseNotes.add(iNoteWithSuggestions);
		
		setWidget(iForm);
		
	}
	
	protected GradeMode getPendingGradeMode(Long courseId) {
		if (iApprovals == null) return null;
		for (RetrieveSpecialRegistrationResponse approval: iApprovals) {
			if (approval.getStatus() == SpecialRegistrationStatus.Pending && approval.hasChanges()) {
				for (ClassAssignmentInterface.ClassAssignment clazz: approval.getChanges()) {
					if (clazz.getGradeMode() != null && courseId.equals(clazz.getCourseId())) {
						return clazz.getGradeMode();
					}
				}
			}
		}
		return null;
	}
	
	protected Float getPendingCredit(Long courseId) {
		if (iApprovals == null) return null;
		for (RetrieveSpecialRegistrationResponse approval: iApprovals) {
			if (approval.getStatus() == SpecialRegistrationStatus.Pending && approval.hasChanges()) {
				for (ClassAssignmentInterface.ClassAssignment clazz: approval.getChanges()) {
					if (clazz.getCreditHour() != null && courseId.equals(clazz.getCourseId())) {
						return clazz.getCreditHour();
					}
				}
			}
		}
		return null;
	}
	
	public void changeGradeModes(ArrayList<ClassAssignmentInterface.ClassAssignment> enrollment, List<RetrieveSpecialRegistrationResponse> approvals) {
		LoadingWidget.getInstance().show(MESSAGES.waitRetrieveGradeModes());
		iChanges.clear();
		iVarChanges.clear();
		iFirstChange = null;
		iNote.setValue("");
		iApproval.clear();
		iEnrollment = enrollment;
		iApprovals = approvals;
		iCourse2Note.clear();
		if (iApprovals != null) {
			for (RetrieveSpecialRegistrationResponse approval: iApprovals) {
				if (approval.getStatus() == SpecialRegistrationStatus.Pending && approval.hasChanges()) {
					for (ClassAssignmentInterface.ClassAssignment clazz: approval.getChanges()) {
						if (clazz.getGradeMode() != null || clazz.getCreditHour() != null) {
							String note = approval.getNote(clazz.getCourseName());
							if (note != null) iCourse2Note.put(clazz.getCourseName(), note);
						}
					}
				} else if (approval.getStatus() == SpecialRegistrationStatus.Pending && approval.hasErrors()) {
					String note = approval.getNote("MAXI");
					if (note != null) iCourse2Note.put("MAXI", note);
				}
			}
		}
		iSectioningService.retrieveGradeModes(new RetrieveAvailableGradeModesRequest(iContext), new AsyncCallback<RetrieveAvailableGradeModesResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				iStatus.error(MESSAGES.exceptionRetrieveGradeModes(caught.getMessage()), caught);
				LoadingWidget.getInstance().hide();
			}

			@Override
			public void onSuccess(RetrieveAvailableGradeModesResponse result) {
				ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
				iTable.clearData(true);
				iCurrentCredit = result.getCurrentCredit(); iMaxCredit = result.getMaxCredit();
				iSuggestions.clear();
				if (result.hasSuggestions())
					iSuggestions.addAll(result.getSuggestions());
				
				Long lastCourseId = null;
				GradeModeChange change = null;
				boolean hasPendingCredit = false;
				boolean hasPendingGradeMode = false;
				for (ClassAssignmentInterface.ClassAssignment clazz: iEnrollment) {
					if (clazz == null) continue;
					SpecialRegistrationGradeModeChanges gradeMode = result.get(clazz);
					SpecialRegistrationVariableCreditChange varCredit = result.getVariableCredits(clazz);
					if (gradeMode == null && varCredit == null) continue;
					
					if (clazz.getParentSection() != null && clazz.getParentSection().equals(clazz.getSection())) continue;
					if (clazz.isTeachingAssignment() || clazz.isDummy() || clazz.isFreeTime()) continue;
					
					VariableCreditChange vcc = null;
					Float pendingCredit = null;
					if (varCredit != null) {
						vcc = new VariableCreditChange(clazz, varCredit);
						iVarChanges.add(vcc);
						if (iFirstChange == null) iFirstChange = (Focusable)vcc.getWidget();
						pendingCredit = getPendingCredit(clazz.getCourseId());
						if (pendingCredit != null)
							hasPendingCredit = true;
					}
					
					boolean firstClazz = !clazz.getCourseId().equals(lastCourseId);
					GradeMode pendingGradeMode = null;
					if (gradeMode != null) {
						if (firstClazz) {
							change = new GradeModeChange(clazz, gradeMode);
							iChanges.add(change);
							if (iFirstChange == null) iFirstChange = (Focusable)change.getWidget();
						} else {
							change.addClassAssignment(clazz, gradeMode);
						}
						pendingGradeMode = getPendingGradeMode(clazz.getCourseId());
						if (pendingGradeMode != null) {
							hasPendingGradeMode = true;
							if (pendingGradeMode.getLabel() == null) {
								SpecialRegistrationGradeMode m = gradeMode.getAvailableChange(pendingGradeMode.getCode());
								if (m != null)
									pendingGradeMode.setLabel(m.getLabel());
								else
									pendingGradeMode.setLabel(pendingGradeMode.getCode());
							}
						}
					} else {
						change = null;
					}
					lastCourseId = clazz.getCourseId();

					String style = (firstClazz && !rows.isEmpty() ? "top-border-dashed": "");
					WebTable.Row row = null;
					if (clazz.isAssigned()) {
						row = new WebTable.Row(
								new WebTable.Cell(firstClazz ? clazz.getSubject() : "").aria(clazz.getSubject()),
								new WebTable.Cell(firstClazz ? clazz.getCourseNbr(CONSTANTS.showCourseTitle()) : "").aria(clazz.getCourseNbr(CONSTANTS.showCourseTitle())),
								new WebTable.Cell(clazz.getSubpart()),
								new WebTable.Cell(clazz.getSection()),
								new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())).aria(clazz.getDaysString(CONSTANTS.longDays(), " ")),
								new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())).aria(clazz.getStartStringAria(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())).aria(clazz.getEndStringAria(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getDatePattern()),
								(clazz.hasDistanceConflict() ? new WebTable.RoomCell(clazz.hasLongDistanceConflict() ? RESOURCES.longDistantConflict() : RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
								(vcc != null ? vcc : new WebTable.AbbvTextCell(clazz.getCredit())),
								new WebTable.Cell(pendingCredit == null ? "" : MESSAGES.credit(pendingCredit)),
								(change == null ? new WebTable.Cell("") : firstClazz ? change : new GradeModeLabel(change, gradeMode)),
								new WebTable.Cell(pendingGradeMode == null ? "" : pendingGradeMode.getLabel())
								);
					} else {
						row = new WebTable.Row(
								new WebTable.Cell(firstClazz ? clazz.getSubject() : "").aria(clazz.getSubject()),
								new WebTable.Cell(firstClazz ? clazz.getCourseNbr(CONSTANTS.showCourseTitle()) : "").aria(clazz.getCourseNbr(CONSTANTS.showCourseTitle())),
								new WebTable.Cell(clazz.getSubpart()),
								new WebTable.Cell(clazz.getSection()),
								new WebTable.Cell(MESSAGES.arrangeHours(), 3, null),
								new WebTable.Cell(clazz.getDatePattern()),
								(clazz.hasDistanceConflict() ? new WebTable.RoomCell(clazz.hasLongDistanceConflict() ? RESOURCES.longDistantConflict() : RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
								(vcc != null ? vcc : new WebTable.AbbvTextCell(clazz.getCredit())),
								new WebTable.Cell(pendingCredit == null ? "" : MESSAGES.credit(pendingCredit)),
								(change == null ? new WebTable.Cell("") : firstClazz ? change : new GradeModeLabel(change, gradeMode)),
								new WebTable.Cell(pendingGradeMode == null ? "" : pendingGradeMode.getLabel())
								);
					}
					for (WebTable.Cell cell: row.getCells())
						cell.setStyleName(style);
					rows.add(row);
				};
				
				WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
				int idx = 0;
				for (WebTable.Row row: rows) rowArray[idx++] = row;
				
				iTable.setData(rowArray);
				iTable.setColumnVisible(10, hasPendingCredit);
				iTable.setColumnVisible(12, hasPendingGradeMode);
				
				LoadingWidget.getInstance().hide();
				if (rows.isEmpty()) {
					iStatus.info(MESSAGES.statusNoGradeModeChangesAvailable());
				} else {
					center();
					if (iFirstChange != null)
						iFirstChange.setFocus(true);
					else if (!iChanges.isEmpty())
						((Focusable)iChanges.get(0).getWidget()).setFocus(true);
					else if (!iVarChanges.isEmpty())
						((Focusable)iVarChanges.get(0).getWidget()).setFocus(true);
				}
			}
		});
	}
	
	protected void formChanged() {
		iApproval.clear();
		P ctab = new P("course-table");
		boolean changes = false, approvals = false;
		String lastCourse = (iCoursesTab.getTabCount() == 0 ? null : iTab2Course.get(iCoursesTab.getSelectedTab()));
		while (iCoursesTab.getTabCount() > 0) iCoursesTab.removeTab(0);
		iTab2Course.clear();
		iDisclaimers.clear();
		Set<String> disclaimers = new TreeSet<String>();
		List<String> courses = new ArrayList<String>();
		for (GradeModeChange cell: iChanges) {
			SpecialRegistrationGradeMode change = cell.getChange();
			if (change == null) continue;
			changes = true;
			boolean first = true;
			for (ClassAssignmentInterface.ClassAssignment ca: cell.getClassAssignments()) {
				SpecialRegistrationGradeMode ch = cell.getChange(ca.getExternalId());
				if (ch == null) continue;
				if (ch.getOriginalGradeMode() != null && ch.getOriginalGradeMode().equals(change.getCode())) continue;
				P cn = new P("course-name");
				if (first) cn.setText(ca.getCourseName());
				P sn = new P("section-name");
				sn.setText(ca.getSection());
				P m = new P("approval-message");
				if (ch.hasApprovals()) {
					m.setText(MESSAGES.gradeModeApprovalNeeded(ch.getLabel(), ToolBox.toString(ch.getApprovals())));
					approvals = true;
					if (!courses.contains(ca.getCourseName())) courses.add(ca.getCourseName());
				} else {
					m.setText(MESSAGES.gradeModeNoApprovalNeeded(ch.getLabel()));
				}
				if (ch.hasDisclaimer()) disclaimers.add(ch.getDisclaimer());
				P crow = new P("course-row");
				if (first) crow.addStyleName("first-course-line");
				crow.add(cn);
				crow.add(sn);
				crow.add(m);
				ctab.add(crow);
				first = false;
			}
		}
		P crtab = new P("course-table");
		boolean credChanges = false, credApprovals = false;
		float cred = (iCurrentCredit == null ? 0f: iCurrentCredit.floatValue());
		for (VariableCreditChange cell: iVarChanges) {
			SpecialRegistrationVariableCredit change = cell.getChange();
			if (change == null) continue;
			cred += change.getCreditChange();
			credChanges = true;
			ClassAssignmentInterface.ClassAssignment ca = cell.getClassAssignment();
			P cn = new P("course-name");
			cn.setText(ca.getCourseName());
			P sn = new P("section-name");
			sn.setText(ca.getSection());
			P m = new P("approval-message");
			if (change.hasApprovals()) {
				m.setText(MESSAGES.varCreditApprovalNeeded(change.getCredit(), ToolBox.toString(change.getApprovals())));
				credApprovals = true;
				if (!courses.contains(ca.getCourseName())) courses.add(ca.getCourseName());
			} else {
				m.setText(MESSAGES.varCreditNoApprovalNeeded(change.getCredit()));
			}
			P crow = new P("course-row");
			crow.addStyleName("first-course-line");
			crow.add(cn);
			crow.add(sn);
			crow.add(m);
			crtab.add(crow);
		}
		if (changes) {
			P m = new P("message"); m.setHTML(MESSAGES.gradeModeListChanges()); iApproval.add(m);
			iApproval.add(ctab);
		}
		if (credChanges) {
			P m = new P("message"); m.setHTML(MESSAGES.varCreditListChanges()); iApproval.add(m);
			iApproval.add(crtab);
			if (iMaxCredit != null && cred > iMaxCredit) {
				m = new P("message", "credit-message"); m.setHTML(MESSAGES.varCreditMaxExceeded(cred, iMaxCredit)); iApproval.add(m);
			}
		}
		if (approvals || credApprovals || (credChanges && iMaxCredit != null && cred > iMaxCredit)) {
			P m = new P("message"); m.setHTML(MESSAGES.gradeModeChangesNote()); iApproval.add(m);
			iNote.setText("");
			for (String course: courses) {
				iTab2Course.put(iCoursesTab.getTabCount(), course);
				iCoursesTab.addTab(course);
				if (course.equals(lastCourse)) {
					iCoursesTab.selectTab(iCoursesTab.getTabCount() - 1);
					String message = iCourse2Note.get(course);
					iNote.setText(message == null ? "" : message);
				}
			}
			if (credChanges && iMaxCredit != null && cred > iMaxCredit) {
				iTab2Course.put(iCoursesTab.getTabCount(), "MAXI");
				iCoursesTab.addTab(MESSAGES.tabRequestNoteMaxCredit());
				if ("MAXI".equals(lastCourse)) {
					iCoursesTab.selectTab(iCoursesTab.getTabCount() - 1);
					String message = iCourse2Note.get("MAXI");
					iNote.setText(message == null ? "" : message);
				}
			}
			if (iCoursesTab.getTabCount() > 0 && iCoursesTab.getSelectedTab() < 0) {
				iCoursesTab.selectTab(0);
				String message = iCourse2Note.get(iTab2Course.get(0));
				iNote.setText(message == null ? "" : message);
			}
			iApproval.add(iCourseNotes);
		}
		if (!disclaimers.isEmpty()) {
			P m = new P("message"); m.setHTML(MESSAGES.gradeModeDisclaimers()); iApproval.add(m);
			for (String d: disclaimers) {
				CheckBox ch = new CheckBox(d, true);
				ch.addStyleName("disclaimer-message");
				ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						submitUpdateEnabled();
					}
				});
				iDisclaimers.add(ch);
				iApproval.add(ch);
			}
		}
		if (changes || credChanges) {
			P m = new P("message"); m.setHTML(MESSAGES.gradeModeChangeOptions()); iApproval.add(m);
		}
		iButtons.setEnabled("submit", (changes || credChanges) && disclaimers.isEmpty(), changes || credChanges);
		center();
	}
	
	protected void submitUpdateEnabled() {
		for (CheckBox ch: iDisclaimers)
			if (!ch.getValue()) {
				iButtons.setEnabled("submit", false, true);
				return;
			}
		iButtons.setEnabled("submit", true, true);
	}
	
	protected void onChange(ChangeGradeModesResponse response) {}
	
	protected void onSubmit() {
		ChangeGradeModesRequest request = new ChangeGradeModesRequest(iContext);
		request.setCurrentCredit(iCurrentCredit);
		request.setMaxCredit(iMaxCredit);
		request.setNote(iCourse2Note.get("MAXI"));
		for (GradeModeChange cell: iChanges) {
			SpecialRegistrationGradeMode change = cell.getChange();
			if (change == null) continue;
			SpecialRegistrationGradeModeChange ch = new SpecialRegistrationGradeModeChange();
			ch.setOriginalGradeMode(cell.getGradeModes().get(0).getCurrentGradeMode() == null ? null : cell.getGradeModes().get(0).getCurrentGradeMode().getCode());
			ch.setSelectedGradeMode(change.getCode());
			ch.setSelectedGradeModeDescription(change.getLabel());
			ch.setSubject(cell.getClassAssignments().get(0).getSubject());
			ch.setCourse(cell.getClassAssignments().get(0).getCourseNbr());
			ch.setCredit(cell.getClassAssignments().get(0).getCredit());
			ch.setNote(iCourse2Note.get(cell.getClassAssignments().get(0).getCourseName()));
			for (ClassAssignmentInterface.ClassAssignment ca: cell.getClassAssignments()) {
				SpecialRegistrationGradeMode x = cell.getChange(ca.getExternalId());
				if (x == null) continue;
				if (x.getOriginalGradeMode() != null && x.getOriginalGradeMode().equals(change.getCode())) continue;
				if (x.hasApprovals())
					for (String app: x.getApprovals()) ch.addApproval(app);
				ch.addCrn(ca.getExternalId());
			}
			request.addChange(ch);
		}
		for (VariableCreditChange cell: iVarChanges) {
			SpecialRegistrationVariableCredit change = cell.getChange();
			if (change == null) continue;
			SpecialRegistrationCreditChange ch = new SpecialRegistrationCreditChange();
			ch.setCredit(change.getCredit());
			ch.setOriginalCredit(change.getOriginalCredit());
			ch.setSubject(cell.getClassAssignment().getSubject());
			ch.setCourse(cell.getClassAssignment().getCourseNbr());
			ch.setCrn(cell.getClassAssignment().getExternalId());
			ch.setNote(iCourse2Note.get(cell.getClassAssignment().getCourseName()));
			if (change.hasApprovals())
				for (String app: change.getApprovals()) ch.addApproval(app);
			request.addChange(ch);
		}
		if (request.hasGradeModeChanges() || request.hasCreditChanges()) {
			LoadingWidget.getInstance().show(MESSAGES.waitChangeGradeModes());
			iSectioningService.changeGradeModes(request, new AsyncCallback<ChangeGradeModesResponse>() {
				
				@Override
				public void onSuccess(ChangeGradeModesResponse response) {
					if (response.hasRequests())
						iStatus.info(MESSAGES.statusGradeModeChangesRequested());
					else
						iStatus.info(MESSAGES.statusGradeModeChangesApplied());
					LoadingWidget.getInstance().hide();
					onChange(response);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					iStatus.error(MESSAGES.exceptionChangeGradeModes(caught.getMessage()), caught);
					LoadingWidget.getInstance().hide();
				}
			});
		} else {
			iStatus.info(MESSAGES.statusNoGradeModeChangesMade());
		}
	}
	
	public class GradeModeChange extends Cell implements HasAriaLabel {
		private ListBox iList;
		private List<SpecialRegistrationGradeModeChanges> iGradeMode;
		private List<ClassAssignmentInterface.ClassAssignment> iClasses;
		
		public GradeModeChange(ClassAssignmentInterface.ClassAssignment ca, SpecialRegistrationGradeModeChanges gradeMode) {
			super(null);
			iList = new ListBox();
			iList.addStyleName("grade-mode-list");
			iClasses = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
			iClasses.add(ca);
			iGradeMode = new ArrayList<SpecialRegistrationGradeModeChanges>();
			iGradeMode.add(gradeMode);
			iList.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					formChanged();
				}
			});
			setup();
		}
		
		private void setup() {
			iList.clear();
			SpecialRegistrationGradeMode current = iGradeMode.get(0).getCurrentGradeMode();
			boolean same = true;
			for (SpecialRegistrationGradeModeChanges gm: iGradeMode) {
				SpecialRegistrationGradeMode m = gm.getCurrentGradeMode();
				if (current != null) {
					if (m == null || !current.getCode().equals(m.getCode())) same = false;
				} else {
					if (m != null) same = false;
				}
			}
			if (same) {
				if (current != null) {
					iList.addItem(current.getLabel(), current.getCode());
					iList.setSelectedIndex(0);
				} else {
					iList.addItem(MESSAGES.gradeModeItemNotSet(), "");
					iList.setSelectedIndex(0);
				}
			} else {
				if (current != null) {
					iList.addItem(MESSAGES.gradeModeItemNotSame(current.getLabel()), "");
					iList.setSelectedIndex(0);
				} else {
					iList.addItem(MESSAGES.gradeModeItemNotSame(MESSAGES.gradeModeItemNotSet()), "");
					iList.setSelectedIndex(0);
				}
			}
			if (!same && current != null) {
				boolean canSetCurrent = true;
				for (SpecialRegistrationGradeModeChanges gm: iGradeMode)
					if (gm.getAvailableChange(current.getCode()) == null && !gm.isCurrentGradeMode(current.getCode())) {
						canSetCurrent = false; break;
					}
				if (canSetCurrent)
					iList.addItem(current.getLabel(), current.getCode());
			}
			av: for (SpecialRegistrationGradeMode mode: iGradeMode.get(0).getAvailableChanges()) {
				for (SpecialRegistrationGradeModeChanges gm: iGradeMode)
					if (gm.getAvailableChange(mode.getCode()) == null && !gm.isCurrentGradeMode(mode.getCode())) continue av;
				if (same && current != null && current.getCode().equals(mode.getCode())) continue;
				iList.addItem(mode.getLabel(), mode.getCode());
			}
		}
		
		public void addClassAssignment(ClassAssignmentInterface.ClassAssignment ca, SpecialRegistrationGradeModeChanges gm) {
			iClasses.add(ca); iGradeMode.add(gm);
			setup();
		}
		
		public String getValue() { return iList.getValue(iList.getSelectedIndex()); }
		public Widget getWidget() { return iList; }
		public SpecialRegistrationGradeMode getChange() {
			if (iList.getSelectedIndex() <= 0) return null;
			SpecialRegistrationGradeMode change = iGradeMode.get(0).getAvailableChange(iList.getValue(iList.getSelectedIndex()));
			if (change == null) {
				SpecialRegistrationGradeMode current = iGradeMode.get(0).getCurrentGradeMode();
				if (current != null && current.getCode().equals(iList.getValue(iList.getSelectedIndex())))
					change = current;
			}
			return change;
		}
		public List<ClassAssignmentInterface.ClassAssignment> getClassAssignments() { return iClasses; }
		public List<SpecialRegistrationGradeModeChanges> getGradeModes() { return iGradeMode; }
		public SpecialRegistrationGradeMode getChange(String extId) {
			if (iList.getSelectedIndex() <= 0) return null;
			for (int i = 0; i < iClasses.size(); i++) {
				if (extId.equals(iClasses.get(i).getExternalId()))
					return iGradeMode.get(i).getAvailableChange(iList.getValue(iList.getSelectedIndex()));
			}
			return null;
		}
	}
	
	public static class GradeModeLabel extends Cell {
		private Label iLabel;
		private SpecialRegistrationGradeModeChanges iGradeMode;
		
		public GradeModeLabel(GradeModeChange change, SpecialRegistrationGradeModeChanges gradeMode) {
			super(null);
			iGradeMode = gradeMode;
			iLabel = new Label();
			iLabel.addStyleName("grade-mode-label");
			iLabel.setText(iGradeMode.getCurrentGradeMode() == null ? "" : iGradeMode.getCurrentGradeMode().getLabel());
			final ListBox box = (ListBox)change.getWidget(); 
			box.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					if (box.getSelectedIndex() <= 0) {
						iLabel.setText(iGradeMode.getCurrentGradeMode() == null ? "" : iGradeMode.getCurrentGradeMode().getLabel());
					} else {
						SpecialRegistrationGradeMode m = iGradeMode.getAvailableChange(box.getValue(box.getSelectedIndex()));
						if (m == null) {
							iLabel.setText(iGradeMode.getCurrentGradeMode() == null ? "" : iGradeMode.getCurrentGradeMode().getLabel());
						} else {
							iLabel.setText(m.getLabel());
						}
					}
				}
			});
		}
		
		public String getValue() { return iLabel.getText(); }
		public Widget getWidget() { return iLabel; }
		public void setStyleName(String styleName) {
			super.setStyleName(styleName);
		}
	}
	
	public class VariableCreditChange extends Cell implements HasAriaLabel {
		private ListBox iList;
		private SpecialRegistrationVariableCreditChange iVarCredit;
		private ClassAssignmentInterface.ClassAssignment iClass;
		
		public VariableCreditChange(ClassAssignmentInterface.ClassAssignment ca, SpecialRegistrationVariableCreditChange vcc) {
			super(null);
			iList = new ListBox();
			iList.addStyleName("variable-credit-list");
			iClass = ca;
			iVarCredit = vcc;
			iList.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					formChanged();
				}
			});
			setup();
		}
		
		private void setup() {
			iList.clear();
			boolean hasCredit = false;
			if (iVarCredit.hasAvailableCredits()) {
				for (float cred: iVarCredit.getAvailableCredits()) {
					iList.addItem(MESSAGES.credit(cred), String.valueOf(cred));
					if (iClass.getCreditHour() != null && iClass.getCreditHour() == cred) {
						iList.setSelectedIndex(iList.getItemCount() - 1);
						hasCredit = true;
					} else if (iClass.getCreditHour() == null && iClass.hasCredit() && iClass.guessCreditCount() == cred) {
						iList.setSelectedIndex(iList.getItemCount() - 1);
						hasCredit = true;
					}
				}
			}
			if (!hasCredit) {
				if (iClass.getCreditHour() != null)
					iList.insertItem(MESSAGES.credit(iClass.getCreditHour()), iClass.getCreditHour().toString(), 0);
				else if (iClass.getCredit() != null)
					iList.insertItem(MESSAGES.credit(iClass.guessCreditCount()), String.valueOf(iClass.guessCreditCount()), 0);
				else
					iList.insertItem("","", 0);
				iList.setSelectedIndex(0);
			}
		}
		
		public String getValue() { return iList.getValue(iList.getSelectedIndex()); }
		public Widget getWidget() { return iList; }
		public SpecialRegistrationVariableCredit getChange() {
			String credit = iList.getValue(iList.getSelectedIndex());
			if (credit.isEmpty()) return null;
			if (iClass.getCreditHour() != null) {
				if (iClass.getCreditHour().equals(Float.valueOf(credit))) return null;
			} else if (iClass.getCredit() != null) {
				if (iClass.guessCreditCount() == Float.valueOf(credit)) return null;
			}
			
			SpecialRegistrationVariableCredit change = new SpecialRegistrationVariableCredit(iVarCredit);
			change.setOriginalCredit(iClass.getCreditHour() != null ? iClass.getCreditHour() : iClass.getCredit() != null ? Float.valueOf(iClass.guessCreditCount()) : null);
			change.setCredit(Float.valueOf(credit));
			return change;
		}
		public ClassAssignmentInterface.ClassAssignment getClassAssignment() { return iClass; }
		public SpecialRegistrationVariableCreditChange getVarCredits() { return iVarCredit; }
	}

}
