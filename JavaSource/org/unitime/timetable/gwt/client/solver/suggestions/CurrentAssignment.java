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
package org.unitime.timetable.gwt.client.solver.suggestions;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.solver.PreferenceLegend;
import org.unitime.timetable.gwt.client.solver.suggestions.SuggestionsPageContext.DateLocations;
import org.unitime.timetable.gwt.client.solver.suggestions.SuggestionsPageContext.RoomLocations;
import org.unitime.timetable.gwt.client.solver.suggestions.SuggestionsPageContext.TimeLocations;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassAssignmentDetails;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.DateInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.RoomInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.TimeInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Tomas Muller
 */
public class CurrentAssignment extends SimpleForm implements TakesValue<ClassAssignmentDetails> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private SuggestionsPageContext iContext = null;
	private ClassAssignmentDetails iDetails;
	private TimeLocations iTimes = null;
	private DateLocations iDates = null;
	private RoomLocations iRooms = null;
	private UniTimeHeaderPanel iHeader = null;
	private int iMessageRow = 0;
	private HTML iMessage = null;
	private UniTimeHeaderPanel iFooter = null;
	private int iFooterRow = 0;
	private int iLegendRow = 0;
	
	public CurrentAssignment(SuggestionsPageContext context) {
		iContext = context;
		iHeader = new UniTimeHeaderPanel();
		removeStyleName("unitime-NotPrintableBottomLine");
		iFooter = new UniTimeHeaderPanel();
		iFooter.addButton("unassign", MESSAGES.buttonUnassign(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<SelectedAssignment> assignments = new ArrayList<SelectedAssignment>();
				assignments.add(new SelectedAssignment(iDetails.getClazz().getClassId()));
				iContext.assign(assignments, iFooter);
			}
		});
	}

	@Override
	public ClassAssignmentDetails getValue() {
		return iDetails;
	}
	
	public void clearMessage() {
		iHeader.clearMessage();
		if (iMessage != null) {
			iMessage.setHTML("");
			getRowFormatter().setVisible(iMessageRow, false);
		}
	}
	
	public void setErrorMessage(String message) {
		iHeader.clearMessage();
		if (iMessage != null) {
			iMessage.setHTML(message);
			getRowFormatter().setVisible(iMessageRow, true);
		}
	}
	
	public void showLoading() {
		iHeader.showLoading();
		if (iMessage != null) {
			iMessage.setHTML("");
			getRowFormatter().setVisible(iMessageRow, false);
		}
	}

	@Override
	public void setValue(ClassAssignmentDetails details) {
		iDetails = details;
		clear();
		iHeader.clearMessage();
		String html = "<a href='classDetail.do?cid=" + details.getClazz().getClassId()+"' class='header-link' target='_blank' title='" + MESSAGES.titleOpenClassDetail(SafeHtmlUtils.htmlEscape(details.getClazz().getName())) + "'>"
			+ details.getClazz().getName() + "</a>";
		iHeader.setHeaderTitle(MESSAGES.headerCurrentAssignment(html));
		addHeaderRow(iHeader);
		if (details.getTime() != null) {
			 if (details.getTime().hasDatePattern()) {
				 addRow(MESSAGES.propAssignedDate(), iContext.createDateLabel(details.getTime().getDatePattern()));
			 }
			 addRow(MESSAGES.propAssignedTime(), iContext.createTimeLabel(details.getTime(), details.getClazz().getClassId(), true));
			 if (details.getRoom() != null)
				 addRow(MESSAGES.propAssignedRooms(), iContext.createRoomsLabel(details.getRoom()));
		} else {
			Label notAssigned = new Label(MESSAGES.classNotAssigned());
			notAssigned.addStyleName("not-assigned");
			addRow(notAssigned);
		}
		if (details.getInstructor() != null)
			addRow(MESSAGES.propInstructor(), iContext.createInstructorsLabel(details.getInstructor()));
		if (details.getInitialTime() != null) {
			addRow(MESSAGES.propInitialAssignment(), iContext.createAssignmentLabel(details.getInitialTime(), details.getInitialRoom(), details.getClazz().getClassId(), details.getNrDates() > 1));
		}
		if (details.hasStudentConflicts()) {
			addRow(MESSAGES.propStudentConflicts(), iContext.createStudentConflicts(details.getStudentConflicts()));
		}
		if (details.hasViolatedDistributionConflicts() || details.hasBtbInstructorConflicts()) {
			addRow(MESSAGES.propViolatedConstraints(), iContext.createViolatedConstraints(details.getDistributionConflicts(), details.getBtbInstructorConflicts()));
		}
		if (details.hasTimes() && details.getNrDates() > 1) {
			iDates = iContext.createDateLocations(details.getTimes());
			addRow(MESSAGES.propDateLocations(), iDates);
			if (details.getAssignedTime() != null && details.getAssignedTime().hasDatePattern())
				iDates.select(details.getAssignedTime().getDatePattern(), false);
			else if (details.getTime() != null && details.getTime().hasDatePattern())
				iDates.select(details.getTime().getDatePattern(), false);
		} else {
			iDates = null;
		}
		if (details.hasTimes()) {
			iTimes = iContext.createTimeLocations(details.getClazz().getClassId(), details.getTimes());
			addRow(MESSAGES.propTimeLocations(), iTimes);
			if (details.getAssignedTime() != null)
				iTimes.select(details.getAssignedTime(), false);
			else if (details.getTime() != null)
				iTimes.select(details.getTime(), false);
		} else {
			iTimes = null;
		}
		if (details.hasRooms()) {
			iRooms = iContext.createRoomLocations(details.getClazz().nrRooms(), details.getRooms());
			addRow(MESSAGES.propRoomLocations(), iRooms);
			if (details.getAssignedRoom() != null)
				for (RoomInfo r: details.getAssignedRoom())
					iRooms.select(r, false);
			else if (details.getRoom() != null)
				for (RoomInfo r: details.getRoom())
					iRooms.select(r, false);
		} else {
			iRooms = null;
		}
		if (details.getClazz().nrRooms() > 0 && details.getClazz().getRoomCapacity() > 0)
			addRow(MESSAGES.propMinimumRoomSize(), new Label(String.valueOf(details.getClazz().getRoomCapacity())));
		if (details.getClazz().getNote() != null && !details.getClazz().getNote().isEmpty()) {
			addRow(MESSAGES.propNote(), new HTML(details.getClazz().getNote()));
		}
		iMessage = new HTML(); iMessage.addStyleName("error-message");
		iMessageRow = addRow(iMessage);
		if (details.getTime() != null && details.isCanUnassign()) {
			iFooterRow = addBottomRow(iFooter);
			iFooter.setEnabled("unassign", true);
		} else {
			iFooterRow = -1;
		}
		iLegendRow = addRow(new PreferenceLegend(iContext.getProperties().getPreferences()));
		getRowFormatter().setVisible(iLegendRow, iFooterRow < 0 || !iFooter.isEnabled("unassign"));
	}
	
	public void setShowUnassign(boolean show) {
		if (show && iFooterRow >= 0) {
			iFooter.setEnabled("unassign", iDetails != null && iDetails.isCanUnassign());
			getRowFormatter().setVisible(iFooterRow, true);
		} else if (!show && iFooterRow >= 0) {
			iFooter.setEnabled("unassign", false);
			getRowFormatter().setVisible(iFooterRow, false);
		}
		getRowFormatter().setVisible(iLegendRow, iFooterRow < 0 || !iFooter.isEnabled("unassign"));
	}
	
	public SelectedAssignment getSelectedAssignment() {
		SelectedAssignment selection = new SelectedAssignment();
		selection.setClassId(iDetails.getClazz().getClassId());
		if (iDates != null) {
			DateInfo date = iDates.getSelectedDate();
			if (date == null) return null;
			selection.setDatePatternId(date.getDatePatternId());
		} else if (iDetails.getNrDates() == 1) {
			selection.setDatePatternId(iDetails.getTimes().get(0).getDatePatternId());
		} else {
			return null;
		}
		if (iTimes != null) {
			TimeInfo time = iTimes.getSelectedTime();
			if (time == null) return null;
			selection.setDays(time.getDays());
			selection.setPatternId(time.getPatternId());
			selection.setStartSlot(time.getStartSlot());
		} else {
			return null;
		}
		if (iRooms != null) {
			if (iRooms.getNrSelectedRooms() == iDetails.getClazz().nrRooms()) {
				for (RoomInfo room: iRooms.getSelectedRooms())
					selection.addRoomId(room.getId());
			} else return null;
		} else if (iDetails.getClazz().nrRooms() == 0) {
		} else {
			return null;
		}
		return selection;
	}
	
	public void setSelectedAssignment(SelectedAssignment assignment) {
		if (iDates != null)
			iDates.select(assignment == null ? null : new DateInfo(assignment), false);
		if (iTimes != null)
			iTimes.select(assignment == null ? (TimeInfo) null : new TimeInfo(assignment), false);
		if (iRooms != null)
			for (int i = 0; i < iDetails.getClazz().nrRooms(); i++) {
				Long roomId = (assignment == null ? null : assignment.getRoomId(i));
				iRooms.select(roomId == null ? (RoomInfo) null : new RoomInfo(roomId), false);
			}
	}
	
	public void setSelectedTime(TimeInfo time, boolean fireUpdate) {
		if (iDates != null)
			iDates.select(time == null ? null : time.getDatePattern(), false);
		if (iTimes != null)
			iTimes.select(time, fireUpdate);
	}
}
