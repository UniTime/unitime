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

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface;
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
public class TeachingRequestDetails extends SimpleForm implements HasValue<Integer> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static final StudentSectioningMessages SECTMSG = GWT.create(StudentSectioningMessages.class);
	protected static NumberFormat sTeachingLoadFormat = NumberFormat.getFormat(CONSTANTS.teachingLoadFormat());
	protected TeachingRequestsPagePropertiesResponse iProperties;
	
	private Label iCourseLabel, iRequestLoad;
	private PreferenceCell iAttributePrefs, iInstructorPrefs;
	private ObjectivesCell iObjectives;
	private UniTimeTable<SectionInfo> iSectionsTable;
	private UniTimeTable<InstructorInfo> iInstructorsTable;
	
	private int iAttributePrefsRow, iInstructorPrefsRow, iObjectivesRow;
	
	public TeachingRequestDetails(TeachingRequestsPagePropertiesResponse properties) {
		super();
		iProperties = properties;
		removeStyleName("unitime-NotPrintableBottomLine");
		
		addHeaderRow(MESSAGES.headerTeachingRequest());
		iCourseLabel = new Label();
		addRow(MESSAGES.propCourse(), iCourseLabel);
		iSectionsTable = new UniTimeTable<SectionInfo>();
		iSectionsTable.addStyleName("sections");
		List<UniTimeTableHeader> sectionHeader = new ArrayList<UniTimeTableHeader>();
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colSection()));
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colTime()));
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colDate()));
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colRoom()));
		iSectionsTable.addRow(null, sectionHeader);		
		addRow(MESSAGES.propSections(), iSectionsTable);
		iRequestLoad = new Label();
		addRow(MESSAGES.propRequestLoad(), iRequestLoad);
		iAttributePrefs = new PreferenceCell(properties);
		iAttributePrefsRow = addRow(MESSAGES.propAttributePrefs(), iAttributePrefs);
		iInstructorPrefs = new PreferenceCell(properties);
		iInstructorPrefsRow = addRow(MESSAGES.propInstructorPrefs(), iInstructorPrefs);
		iObjectives = new ObjectivesCell(properties);
		iObjectivesRow = addRow(MESSAGES.propObjectives(), iObjectives);
		iInstructorsTable = new UniTimeTable<InstructorInfo>();
		iInstructorsTable.addStyleName("instructors");
		List<UniTimeTableHeader> instructorsHeader = new ArrayList<UniTimeTableHeader>();
		instructorsHeader.add(new UniTimeTableHeader(MESSAGES.colIndex()));
		instructorsHeader.add(new UniTimeTableHeader(MESSAGES.colExternalId()));
		instructorsHeader.add(new UniTimeTableHeader(MESSAGES.colNamePerson()));
		instructorsHeader.add(new UniTimeTableHeader(MESSAGES.colAssignedLoad()));
		instructorsHeader.add(new UniTimeTableHeader(MESSAGES.colAttributes()));
		instructorsHeader.add(new UniTimeTableHeader(MESSAGES.colCoursePreferences()));
		instructorsHeader.add(new UniTimeTableHeader(MESSAGES.colTimePreferences()));
		instructorsHeader.add(new UniTimeTableHeader(MESSAGES.colDistributionPreferences()));
		instructorsHeader.add(new UniTimeTableHeader(MESSAGES.colObjectives()));
		iInstructorsTable.addRow(null, instructorsHeader);
		addRow(MESSAGES.propAssignedInstructors(), iInstructorsTable);
		iInstructorsTable.setAllowSelection(true);
		iInstructorsTable.setAllowMultiSelect(false);
		
		iInstructorsTable.addMouseClickListener(new UniTimeTable.MouseClickListener<InstructorInterface.InstructorInfo>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<InstructorInterface.InstructorInfo> event) {
				if (event.getRow() > 0) {
					iInstructorsTable.setSelected(event.getRow(), true);
					ValueChangeEvent.fire(TeachingRequestDetails.this, event.getRow() - 1);
				}
			}
		});
	}

	public void setRequest(TeachingRequestInfo request, Integer index) {
		iCourseLabel.setText(request.getCourse().getCourseName());
		iRequestLoad.setText(sTeachingLoadFormat.format(request.getLoad()));
		iSectionsTable.clearTable(1);
		for (SectionInfo s: request.getSections()) {
			List<Widget> sectionLine = new ArrayList<Widget>();
			sectionLine.add(new Label(s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId())));
			sectionLine.add(new HTML(s.getTime() == null ? SECTMSG.arrangeHours() : s.getTime()));
			sectionLine.add(new HTML(s.getDate() == null ? SECTMSG.noDate() : s.getDate()));
			sectionLine.add(new HTML(s.getRoom() == null ? SECTMSG.noRoom() : s.getRoom()));
			if (s.isCommon())
				for (Widget w: sectionLine) w.addStyleName("common");
			iSectionsTable.addRow(s, sectionLine);
		}
		iAttributePrefs.setValue(request.getAttributePreferences());
		getRowFormatter().setVisible(iAttributePrefsRow, !request.getAttributePreferences().isEmpty());
		iInstructorPrefs.setValue(request.getInstructorPreferences());
		getRowFormatter().setVisible(iInstructorPrefsRow, !request.getInstructorPreferences().isEmpty());
		iObjectives.setValue(request.getValues());
		getRowFormatter().setVisible(iObjectivesRow, !request.getValues().isEmpty());
		
		iInstructorsTable.clearTable(1);
		int instrIndex = 1;
		if (request.hasInstructors()) {
			for (InstructorInfo instructor: request.getInstructors()) {
				List<Widget> instructorLine = new ArrayList<Widget>();
				instructorLine.add(new Label((instrIndex++) + "."));
				instructorLine.add(new InstructorExternalIdCell(iProperties, instructor));
				instructorLine.add(new InstructorNameCell(iProperties, instructor));
				instructorLine.add(new Label(sTeachingLoadFormat.format(instructor.getAssignedLoad()) + " / " + sTeachingLoadFormat.format(instructor.getMaxLoad())));
				instructorLine.add(new AttributesCell(instructor.getAttributes()));
				instructorLine.add(new PreferenceCell(iProperties, instructor.getCoursePreferences()));
				instructorLine.add(new TimePreferenceCell(iProperties, instructor));
				instructorLine.add(new PreferenceCell(iProperties, instructor.getDistributionPreferences()));
				instructorLine.add(new ObjectivesCell(iProperties, instructor.getValues()));
				iInstructorsTable.addRow(instructor, instructorLine);
			}
		}
		for (int i = request.getNrAssignedInstructors(); i < request.getNrInstructors(); i++) {
			List<Widget> instructorLine = new ArrayList<Widget>();
			instructorLine.add(new Label((instrIndex++) + "."));
			instructorLine.add(new NotAssignedInstructor(8));
			iInstructorsTable.addRow(null, instructorLine);
		}
		if (request.getNrInstructors() <= 1)
			iInstructorsTable.setColumnVisible(0, false);
		if (request.getNrInstructors() == 1) {
			iInstructorsTable.setSelected(1, true);
		} else if (index != null) {
			iInstructorsTable.setSelected(1 + index, true);
		}
	}
	
	public class NotAssignedInstructor extends P implements UniTimeTable.HasColSpan {
		int iColSpan;

		NotAssignedInstructor(int colspan) {
			super("not-assigned");
			iColSpan = colspan;
			setText(MESSAGES.notAssignedInstructor());
		}

		@Override
		public int getColSpan() {
			return iColSpan;
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Integer getValue() {
		int row = iInstructorsTable.getSelectedRow();
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
			int row = iInstructorsTable.getSelectedRow();
			if (row >= 0) iInstructorsTable.setSelected(row, false);
		} else {
			iInstructorsTable.setSelected(value + 1, true);
		}
		if (fireEvents)
			ValueChangeEvent.fire(this, getValue());
	}
}
