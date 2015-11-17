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

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeCourseInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeGroupInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeItemInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreePlaceHolderInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class DegreePlanTable extends UniTimeTable<DegreeItemInterface> implements TakesValue<DegreePlanInterface>{
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	
	private DegreePlanInterface iPlan;
	
	public DegreePlanTable() {
		addStyleName("unitine-DegreePlanTable");
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(""));
		header.add(new UniTimeTableHeader(MESSAGES.colDegreeItemName()));
		header.add(new UniTimeTableHeader(MESSAGES.colDegreeItemDescription()));
		addRow(null, header);
	}

	@Override
	public DegreePlanInterface getValue() {
		return iPlan;
	}

	@Override
	public void setValue(DegreePlanInterface plan) {
		iPlan = plan;
		clearTable(1);
		if (plan.getGroup() != null)
			addGroup(1, plan.getGroup().getMaxDepth(), plan.getGroup());
	}
	
	protected void addGroup(int depth, int maxDepth, DegreeGroupInterface group) {
		if (depth > 1) {
			List<Widget> row = new ArrayList<Widget>();
			P indent = new P("indent");
			for (int d = 1; d < depth - 1; d++)
				indent.add(new Image(RESOURCES.indentMiddleLine()));
			indent.add(new Image(RESOURCES.indentTopLine()));
			for (int d = depth + 1; d <= maxDepth; d++)
				indent.add(new Image(RESOURCES.indentTopSpace()));
			row.add(indent);
			row.add(new GroupTitleCell(group.getDescription()));
			addRow(group, row);
		}
		if (group.hasCourses()) {
			for (DegreeCourseInterface course: group.getCourses()) {
				List<Widget> row = new ArrayList<Widget>();
				P indent = new P("indent");
				for (int d = 1; d < depth; d++)
					indent.add(new Image(RESOURCES.indentMiddleLine()));
				for (int d = depth + 1; d <= maxDepth; d++)
					indent.add(new Image(RESOURCES.indentBlankSpace()));
				row.add(indent);
				row.add(new Label(course.getName()));
				row.add(new Label(course.getTitle() == null ? "" : course.getTitle()));
				addRow(course, row);
			}
		}
		if (group.hasGroups()) {
			for (DegreeGroupInterface g: group.getGroups())
				addGroup(depth + 1, maxDepth, g);
		}
		if (group.hasPlaceHolders()) {
			for (DegreePlaceHolderInterface p: group.getPlaceHolders()) {
				List<Widget> row = new ArrayList<Widget>();
				P indent = new P("indent");
				for (int d = 1; d < depth; d++)
					indent.add(new Image(RESOURCES.indentMiddleLine()));
				for (int d = depth + 1; d <= maxDepth; d++)
					indent.add(new Image(RESOURCES.indentBlankSpace()));
				row.add(indent);
				row.add(new PlaceHolderCell(p.getName()));
				addRow(p, row);
			}
		}
		if (depth > 1 && getRowCount() > 1) {
			P indent = (P)getWidget(getRowCount() - 1, 0);
			indent.remove(depth - 2);
			indent.insert(new Image(RESOURCES.indentLastLine()), depth - 2);
		}
	}
	
	public static class GroupTitleCell extends Label implements UniTimeTable.HasColSpan {
		public GroupTitleCell(String label) {
			super(label);
			addStyleName("grouplabel");
		}

		@Override
		public int getColSpan() { return 2; }
	}
	
	public static class PlaceHolderCell extends Label implements UniTimeTable.HasColSpan {
		public PlaceHolderCell(String label) {
			super(label);
			addStyleName("placeholder");
		}

		@Override
		public int getColSpan() { return 2; }
	}

}
