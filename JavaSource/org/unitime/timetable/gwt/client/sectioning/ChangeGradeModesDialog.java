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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.HasAriaLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.client.widgets.WebTable.Cell;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradeMode;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAvailableGradeModesRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAvailableGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationGradeMode;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationGradeModeChange;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationGradeModeChanges;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationStatus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class ChangeGradeModesDialog extends UniTimeDialogBox {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private SimpleForm iForm;
	private WebTable iTable;
	private UniTimeHeaderPanel iButtons;
	private List<GradeModeChange> iChanges = new ArrayList<GradeModeChange>();
	private ScheduleStatus iStatus;
	private ArrayList<ClassAssignmentInterface.ClassAssignment> iEnrollment;
	private List<RetrieveSpecialRegistrationResponse> iApprovals;
	private P iApproval = null;
	private TextArea iNote = null;
	private List<CheckBox> iDisclaimers = new ArrayList<CheckBox>();
	
	public ChangeGradeModesDialog(ScheduleStatus status) {
		super(true, true);
		addStyleName("unitime-ChangeGradeModesDialog");
		setText(MESSAGES.dialogChangeGradeMode());
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
		iNote = new TextArea();
		iNote.setStyleName("unitime-TextArea"); iNote.addStyleName("request-note");
		iNote.setVisibleLines(5);
		iNote.setCharacterWidth(80);
		
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
	
	public void changeGradeModes(Long sessionId, Long studentId, ArrayList<ClassAssignmentInterface.ClassAssignment> enrollment, List<RetrieveSpecialRegistrationResponse> approvals) {
		LoadingWidget.getInstance().show(MESSAGES.waitRetrieveGradeModes());
		iChanges.clear();
		iNote.setValue("");
		iApproval.clear();
		iEnrollment = enrollment;
		iApprovals = approvals;
		iSectioningService.retrieveGradeModes(new RetrieveAvailableGradeModesRequest(sessionId, studentId), new AsyncCallback<RetrieveAvailableGradeModesResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				iStatus.error(MESSAGES.exceptionRetrieveGradeModes(caught.getMessage()), caught);
				LoadingWidget.getInstance().hide();
			}

			@Override
			public void onSuccess(RetrieveAvailableGradeModesResponse result) {
				ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
				iTable.clearData(true);
				
				Long lastCourseId = null;
				GradeModeChange change = null;
				boolean hasPendingGradeMode = false;
				for (ClassAssignmentInterface.ClassAssignment clazz: iEnrollment) {
					SpecialRegistrationGradeModeChanges gradeMode = result.get(clazz);
					if (gradeMode == null) continue;
					if (clazz.getParentSection() != null && clazz.getParentSection().equals(clazz.getSection())) continue;
					if (clazz.isTeachingAssignment() || clazz.isDummy() || clazz.isFreeTime()) continue;
					
					boolean firstClazz = !clazz.getCourseId().equals(lastCourseId);
					if (firstClazz) {
						change = new GradeModeChange(clazz, gradeMode);
						iChanges.add(change);
					} else {
						change.addClassAssignment(clazz, gradeMode);
					}
					lastCourseId = clazz.getCourseId();
					GradeMode pendingGradeMode = getPendingGradeMode(clazz.getCourseId());
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
					
					String style = (firstClazz && !rows.isEmpty() ? "top-border-dashed": "");
					WebTable.Row row = null;
					if (clazz.isAssigned()) {
						row = new WebTable.Row(
								new WebTable.Cell(firstClazz ? clazz.getSubject() : "").aria(clazz.getSubject()),
								new WebTable.Cell(firstClazz ? CONSTANTS.showCourseTitle() ? clazz.getCourseNameWithTitle() : clazz.getCourseName() : "").aria(CONSTANTS.showCourseTitle() ? clazz.getCourseNameWithTitle() : clazz.getCourseName()),
								new WebTable.Cell(clazz.getSubpart()),
								new WebTable.Cell(clazz.getSection()),
								new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())).aria(clazz.getDaysString(CONSTANTS.longDays(), " ")),
								new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())).aria(clazz.getStartStringAria(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())).aria(clazz.getEndStringAria(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getDatePattern()),
								(clazz.hasDistanceConflict() ? new WebTable.RoomCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
								new WebTable.AbbvTextCell(clazz.getCredit()),
								(firstClazz ? change : new GradeModeLabel(change, gradeMode)),
								new WebTable.Cell(pendingGradeMode == null ? "" : pendingGradeMode.getLabel())
								);
					} else {
						row = new WebTable.Row(
								new WebTable.Cell(firstClazz ? clazz.getSubject() : "").aria(clazz.getSubject()),
								new WebTable.Cell(firstClazz ? CONSTANTS.showCourseTitle() ? clazz.getCourseNameWithTitle() : clazz.getCourseName() : "").aria(CONSTANTS.showCourseTitle() ? clazz.getCourseNameWithTitle() : clazz.getCourseName()),
								new WebTable.Cell(clazz.getSubpart()),
								new WebTable.Cell(clazz.getSection()),
								new WebTable.Cell(MESSAGES.arrangeHours(), 3, null),
								new WebTable.Cell(clazz.getDatePattern()),
								(clazz.hasDistanceConflict() ? new WebTable.RoomCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
								new WebTable.AbbvTextCell(clazz.getCredit()),
								(firstClazz ? change : new GradeModeLabel(change, gradeMode)),
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
				iTable.setColumnVisible(11, hasPendingGradeMode);
				
				LoadingWidget.getInstance().hide();
				if (rows.isEmpty()) {
					iStatus.info(MESSAGES.statusNoGradeModeChangesAvailable());
				} else {
					center();
					if (!iChanges.isEmpty())
						((Focusable)iChanges.get(0).getWidget()).setFocus(true);
				}
			}
		});
	}
	
	protected void formChanged() {
		iApproval.clear();
		P ctab = new P("course-table");
		boolean changes = false, approvals = false;
		iDisclaimers.clear();
		Set<String> disclaimers = new TreeSet<String>();
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
		if (changes) {
			P m = new P("message"); m.setHTML(MESSAGES.gradeModeListChanges()); iApproval.add(m);
			iApproval.add(ctab);
			if (approvals) {
				m = new P("message"); m.setHTML(MESSAGES.gradeModeChangesNote()); iApproval.add(m);
				iApproval.add(iNote);
			}
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
		if (changes) {
			P m = new P("message"); m.setHTML(MESSAGES.gradeModeChangeOptions()); iApproval.add(m);
		}
		iButtons.setEnabled("submit", changes && disclaimers.isEmpty(), changes);
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
		ChangeGradeModesRequest request = new ChangeGradeModesRequest();
		request.setNote(iNote.getValue());
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
		if (request.hasGradeModeChanges()) {
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

}
