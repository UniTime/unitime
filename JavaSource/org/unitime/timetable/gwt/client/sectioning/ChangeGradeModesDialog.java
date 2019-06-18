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
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAvailableGradeModesRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAvailableGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationGradeMode;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationGradeModeChanges;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Focusable;
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
	private P iApproval = null;
	private TextArea iNote = null;
	
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
				//new WebTable.Cell(MESSAGES.colLimit()).aria(ARIA.colLimit()),
				new WebTable.Cell(MESSAGES.colDays()),
				new WebTable.Cell(MESSAGES.colStart()),
				new WebTable.Cell(MESSAGES.colEnd()),
				new WebTable.Cell(MESSAGES.colDate()),
				new WebTable.Cell(MESSAGES.colRoom()),
				// new WebTable.Cell(MESSAGES.colInstructor()),
				// new WebTable.Cell(MESSAGES.colParent()),
				new WebTable.Cell(MESSAGES.colCredit()),
				new WebTable.Cell(MESSAGES.colTitleGradeMode())
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
	
	public void changeGradeModes(Long sessionId, Long studentId, ArrayList<ClassAssignmentInterface.ClassAssignment> enrollment) {
		LoadingWidget.getInstance().show(MESSAGES.waitRetrieveGradeModes());
		iChanges.clear();
		iNote.setValue("");
		iApproval.clear();
		iEnrollment = enrollment;
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
				for (ClassAssignmentInterface.ClassAssignment clazz: iEnrollment) {
					SpecialRegistrationGradeModeChanges gradeMode = result.get(clazz);
					if (gradeMode == null) continue;
					if (clazz.getParentSection() != null && clazz.getParentSection().equals(clazz.getSection())) continue;
					if (clazz.isTeachingAssignment() || clazz.isDummy() || clazz.isFreeTime() || !clazz.isAssigned()) continue;
					
					boolean firstClazz = !clazz.getCourseId().equals(lastCourseId);
					lastCourseId = clazz.getCourseId();
					String style = (firstClazz && !rows.isEmpty() ? "top-border-dashed": "");
					GradeModeChange change = new GradeModeChange(clazz.getCourseName(), clazz.getSection(), clazz.getExternalId(), gradeMode);
					iChanges.add(change);
					WebTable.Row row = new WebTable.Row(
							new WebTable.Cell(firstClazz ? clazz.getSubject() : "").aria(clazz.getSubject()),
							new WebTable.Cell(firstClazz ? CONSTANTS.showCourseTitle() ? clazz.getCourseNameWithTitle() : clazz.getCourseName() : "").aria(CONSTANTS.showCourseTitle() ? clazz.getCourseNameWithTitle() : clazz.getCourseName()),
							new WebTable.Cell(clazz.getSubpart()),
							new WebTable.Cell(clazz.getSection()),
							//new WebTable.Cell(clazz.getLimitString()),
							new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())).aria(clazz.getDaysString(CONSTANTS.longDays(), " ")),
							new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())).aria(clazz.getStartStringAria(CONSTANTS.useAmPm())),
							new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())).aria(clazz.getEndStringAria(CONSTANTS.useAmPm())),
							new WebTable.Cell(clazz.getDatePattern()),
							(clazz.hasDistanceConflict() ? new WebTable.RoomCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
							// new WebTable.InstructorCell(clazz.getInstructors(), null, ", "),
							// new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
							new WebTable.AbbvTextCell(clazz.getCredit()),
							change
							);
					for (WebTable.Cell cell: row.getCells())
						cell.setStyleName(style);
					rows.add(row);
				};
				
				WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
				int idx = 0;
				for (WebTable.Row row: rows) rowArray[idx++] = row;
				
				iTable.setData(rowArray);
				
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
		String last = null;
		for (GradeModeChange cell: iChanges) {
			SpecialRegistrationGradeMode change = cell.getChange();
			if (change == null) continue;
			changes = true;
			P cn = new P("course-name");
			if (last == null || !last.equals(cell.getCourse())) cn.setText(cell.getCourse());
			P sn = new P("section-name");
			sn.setText(cell.getSection());
			P m = new P("approval-message");
			if (change.hasApprovals()) {
				m.setText(MESSAGES.gradeModeApprovalNeeded(ToolBox.toString(change.getApprovals())));
				approvals = true;
			} else {
				m.setText(MESSAGES.gradeModeNoApprovalNeeded());
			}
			P crow = new P("course-row");
			if (last == null || !last.equals(cell.getCourse())) crow.addStyleName("first-course-line");
			crow.add(cn);
			crow.add(sn);
			crow.add(m);
			ctab.add(crow);
			last = cell.getCourse();
		}
		if (changes) {
			P m = new P("message"); m.setHTML(MESSAGES.gradeModeListChanges()); iApproval.add(m);
			iApproval.add(ctab);
			if (approvals) {
				m = new P("message"); m.setHTML(MESSAGES.gradeModeChangesNote()); iApproval.add(m);
				iApproval.add(iNote);
			}
			m = new P("message"); m.setHTML(MESSAGES.gradeModeChangeOptions()); iApproval.add(m);
		}
		iButtons.setEnabled("submit", changes);
	}
	
	protected void onChange(ChangeGradeModesResponse response) {}
	
	protected void onSubmit() {
		ChangeGradeModesRequest request = new ChangeGradeModesRequest();
		request.setNote(iNote.getValue());
		for (GradeModeChange cell: iChanges) {
			SpecialRegistrationGradeMode change = cell.getChange();
			if (change == null) continue;
			request.add(cell.getSectionExternalId(), change);
		}
		if (request.hasGradeModeChanges()) {
			LoadingWidget.getInstance().show(MESSAGES.waitChangeGradeModes());
			iSectioningService.changeGradeModes(request, new AsyncCallback<ChangeGradeModesResponse>() {
				
				@Override
				public void onSuccess(ChangeGradeModesResponse response) {
					iStatus.info(MESSAGES.statusGradeModeChangesDone());
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
		private ListBox iList = new ListBox();
		private SpecialRegistrationGradeModeChanges iGradeMode;
		private String iSectionExternalId;
		private String iCourse, iSection;
		
		public GradeModeChange(String course, String section, String sectionId, SpecialRegistrationGradeModeChanges gradeMode) {
			super(null);
			iList = new ListBox();
			iList.addItem(gradeMode.getCurrentGradeMode().getLabel(), gradeMode.getCurrentGradeMode().getCode());
			iList.setSelectedIndex(0);
			iCourse = course; iSection = section;
			iSectionExternalId = sectionId;
			iGradeMode = gradeMode;
			if (gradeMode.hasAvailableChanges()) {
				for (SpecialRegistrationGradeMode mode: gradeMode.getAvailableChanges()) {
					if (!mode.equals(gradeMode.getCurrentGradeMode()))
						iList.addItem(mode.getLabel(), mode.getCode());
				}
			}
			iList.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					formChanged();
				}
			});
		}
		
		public String getValue() { return iList.getValue(iList.getSelectedIndex()); }
		public Widget getWidget() { return iList; }
		public SpecialRegistrationGradeMode getChange() {
			if (iList.getSelectedIndex() <= 0) return null;
			return iGradeMode.getAvailableChange(iList.getValue(iList.getSelectedIndex()));
		}
		public String getSectionExternalId() { return iSectionExternalId; }
		public String getCourse() { return iCourse; }
		public String getSection() { return iSection; }
	}

}
