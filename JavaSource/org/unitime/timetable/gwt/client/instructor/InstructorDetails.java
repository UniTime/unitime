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
package org.unitime.timetable.gwt.client.instructor;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget.InstructorAvailabilityModel;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.ClassInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class InstructorDetails extends SimpleForm implements HasValue<Integer>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static final StudentSectioningMessages SECTMSG = GWT.create(StudentSectioningMessages.class);
	protected static NumberFormat sTeachingLoadFormat = NumberFormat.getFormat(CONSTANTS.teachingLoadFormat());
	protected TeachingRequestsPagePropertiesResponse iProperties;
	
	private InstructorExternalIdCell iExternalId;
	private InstructorNameCell iName;
	private Label iAssignedLoad;
	private AttributesCell iAttributes;
	private PreferenceCell iCoursePrefs, iDistPrefs;
	private InstructorAvailabilityWidget iTimePrefs;
	private ObjectivesCell iObjectives;
	
	private UniTimeTable<ClassInfo> iEnrollmentsTable;
	private UniTimeTable<TeachingRequestInfo> iRequestsTable;
	
	private int iAttributesRow, iCoursePrefsRow, iEnrollmentsRow, iDistPrefsRow, iRequestsRow, iObjectivesRow;
	
	public InstructorDetails(TeachingRequestsPagePropertiesResponse properties) {
		iProperties = properties;
		removeStyleName("unitime-NotPrintableBottomLine");
		
		addHeaderRow(MESSAGES.headerInstructor());
		iExternalId = new InstructorExternalIdCell(properties);
		addRow(MESSAGES.propExternalId(), iExternalId);
		iName = new InstructorNameCell(properties);
		addRow(MESSAGES.propInstructorName(), iName);
		iAssignedLoad = new Label();
		addRow(MESSAGES.propAssignedLoad(), iAssignedLoad);
		iAttributes = new AttributesCell();
		iAttributesRow = addRow(MESSAGES.propInstructorAttributes(), iAttributes);
		iCoursePrefs = new PreferenceCell(properties); 
		iCoursePrefsRow = addRow(MESSAGES.propCoursePreferences(), iCoursePrefs);
		iTimePrefs = new InstructorAvailabilityWidget(); 
		addRow(MESSAGES.propTimePreferences(), iTimePrefs);
		
		iEnrollmentsTable = new UniTimeTable<ClassInfo>();
		iEnrollmentsTable.addStyleName("enrollments");
		List<UniTimeTableHeader> enrlHeader = new ArrayList<UniTimeTableHeader>();
		enrlHeader.add(new UniTimeTableHeader(MESSAGES.colCourse()));
		enrlHeader.add(new UniTimeTableHeader(MESSAGES.colSection()));
		enrlHeader.add(new UniTimeTableHeader(MESSAGES.colTime()));
		enrlHeader.add(new UniTimeTableHeader(MESSAGES.colDate()));
		enrlHeader.add(new UniTimeTableHeader(MESSAGES.colRoom()));
		enrlHeader.add(new UniTimeTableHeader(MESSAGES.colRole()));
		iEnrollmentsTable.addRow(null, enrlHeader);
		iEnrollmentsRow = addRow(MESSAGES.propEnrollments(), iEnrollmentsTable);

		iDistPrefs = new PreferenceCell(properties); 
		iDistPrefsRow = addRow(MESSAGES.propDistributionPreferences(), iDistPrefs);
		
		iRequestsTable = new UniTimeTable<TeachingRequestInfo>();
		iRequestsTable.addStyleName("assignments");
		List<UniTimeTableHeader> reqHeader = new ArrayList<UniTimeTableHeader>();
		reqHeader.add(new UniTimeTableHeader(MESSAGES.colCourse()));
		reqHeader.add(new UniTimeTableHeader(MESSAGES.colSection()));
		reqHeader.add(new UniTimeTableHeader(MESSAGES.colTime()));
		reqHeader.add(new UniTimeTableHeader(MESSAGES.colDate()));
		reqHeader.add(new UniTimeTableHeader(MESSAGES.colRoom()));
		reqHeader.add(new UniTimeTableHeader(MESSAGES.colTeachingLoad()));
		reqHeader.add(new UniTimeTableHeader(MESSAGES.colAttributePreferences()));
		reqHeader.add(new UniTimeTableHeader(MESSAGES.colInstructorPreferences()));
		reqHeader.add(new UniTimeTableHeader(MESSAGES.colObjectives()));
		iRequestsTable.addRow(null, reqHeader);
		iRequestsRow = addRow(MESSAGES.propAssignments(), iRequestsTable);
		
		iObjectives = new ObjectivesCell(properties);
		iObjectivesRow = addRow(MESSAGES.propObjectives(), iObjectives);
		iRequestsTable.setAllowSelection(true);
		iRequestsTable.setAllowMultiSelect(false);
		
		iRequestsTable.addMouseClickListener(new UniTimeTable.MouseClickListener<InstructorInterface.TeachingRequestInfo>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<InstructorInterface.TeachingRequestInfo> event) {
				if (event.getRow() > 0) {
					iRequestsTable.setSelected(event.getRow(), true);
					ValueChangeEvent.fire(InstructorDetails.this, event.getRow() - 1);
				}
			}
		});
	}
	
	public void setInstructor(InstructorInfo instructor) {
		iExternalId.setValue(instructor);
		iName.setValue(instructor);
		iAssignedLoad.setText(sTeachingLoadFormat.format(instructor.getAssignedLoad()) + " / " + sTeachingLoadFormat.format(instructor.getMaxLoad()));
		iAttributes.clear();
		if (instructor.getAttributes().isEmpty()) {
			getRowFormatter().setVisible(iAttributesRow, false);
		} else {
			iAttributes.setValue(instructor.getAttributes());
			getRowFormatter().setVisible(iAttributesRow, true);
		}
		
		if (instructor.getCoursePreferences().isEmpty()) {
			iCoursePrefs.clear();
			getRowFormatter().setVisible(iCoursePrefsRow, false);
		} else {
			iCoursePrefs.setValue(instructor.getCoursePreferences());
			getRowFormatter().setVisible(iCoursePrefsRow, true);
		}
		
		InstructorAvailabilityModel model = iProperties.getInstructorAvailabilityModel();
		model.setPattern(instructor.getAvailability());
		iTimePrefs.setValue(model);
		
		if (instructor.getDistributionPreferences().isEmpty()) {
			iDistPrefs.clear();
			getRowFormatter().setVisible(iDistPrefsRow, false);
		} else {
			iDistPrefs.setValue(instructor.getDistributionPreferences());
			getRowFormatter().setVisible(iDistPrefsRow, true);
		}
		
		iEnrollmentsTable.clearTable(1);
		if (instructor.getEnrollments().isEmpty()) {
			getRowFormatter().setVisible(iEnrollmentsRow, false);
		} else {
			for (ClassInfo e: instructor.getEnrollments()) {
				List<Widget> line = new ArrayList<Widget>();
				line.add(new Label(e.getCourse()));
				line.add(new Label(e.getType() + (e.getExternalId() == null ? "" : " " + e.getExternalId())));
				line.add(new HTML(e.getTime() == null ? SECTMSG.arrangeHours() : e.getTime()));
				line.add(new HTML(e.getDate() == null ? SECTMSG.noDate() : e.getDate()));
				line.add(new HTML(e.getRoom() == null ? SECTMSG.noRoom() : e.getRoom()));
				line.add(new Label(e.isInstructor() ? MESSAGES.enrollmentRoleInstructor() : MESSAGES.enrollmentRoleStudent()));
				iEnrollmentsTable.addRow(e, line);
			}
			getRowFormatter().setVisible(iEnrollmentsRow, true);
		}
		
		iRequestsTable.clearTable(1); iObjectives.clear();
		if (instructor.getAssignedRequests().isEmpty()) {
			getRowFormatter().setVisible(iRequestsRow, false);
			getRowFormatter().setVisible(iObjectivesRow, false);
		} else {
			for (TeachingRequestInfo request: instructor.getAssignedRequests()) {
				List<Widget> line = new ArrayList<Widget>();
				P course = new P("course"); course.setText(request.getCourse().getCourseName());
				line.add(course);
				P section = new P("sections"), time = new P("times"), date = new P("dates"), room = new P("rooms");
				for (SectionInfo s: request.getSections()) {
					P p = new P("section");
					p.setText(s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId()));
					if (s.isCommon()) p.addStyleName("common");
					section.add(p);
					P t = new P("time");
					t.setHTML(s.getTime() == null ? SECTMSG.arrangeHours() : s.getTime());
					if (s.isCommon()) t.addStyleName("common");
					time.add(t);
					P d = new P("date");
					d.setHTML(s.getDate() == null ? SECTMSG.noDate() : s.getDate());
					if (s.isCommon()) d.addStyleName("common");
					date.add(d);
					P r = new P("room");
					r.setHTML(s.getRoom() == null ? SECTMSG.noRoom() : s.getRoom());
					if (s.isCommon()) r.addStyleName("common");
					room.add(r);
				}
				line.add(section);
				line.add(time);
				line.add(date);
				line.add(room);
				line.add(new Label(sTeachingLoadFormat.format(request.getLoad())));
				line.add(new PreferenceCell(iProperties, request.getAttributePreferences()));
				line.add(new PreferenceCell(iProperties, request.getInstructorPreferences()));
				line.add(new ObjectivesCell(iProperties, request.getValues()));
				int row = iRequestsTable.addRow(request, line);
				if (request.isConflict())
					iRequestsTable.getRowFormatter().addStyleName(row, "enrollment-conflict");
			}
			iObjectives.setValue(instructor.getValues());
			getRowFormatter().setVisible(iRequestsRow, true);
			getRowFormatter().setVisible(iObjectivesRow, true);
		}
	}
	

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Integer getValue() {
		int row = iRequestsTable.getSelectedRow();
		if (row < 1) return null;
		return row - 1;
	}

	@Override
	public void setValue(Integer value) {
		setValue(value, false);
	}

	@Override
	public void setValue(Integer value, boolean fireEvents) {
		if (value == null) {
			int row = iRequestsTable.getSelectedRow();
			if (row >= 0) iRequestsTable.setSelected(row, false);
		} else {
			iRequestsTable.setSelected(value + 1, true);
		}
		if (fireEvents)
			ValueChangeEvent.fire(this, getValue());
	}
}
