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

import java.util.TreeSet;

import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassAssignmentDetails;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.Suggestion;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author Tomas Muller
 */
public class SelectedSuggestion extends SimpleForm implements TakesValue<Suggestion> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	
	private SuggestionsPageContext iContext;
	private Suggestion iSuggestion;
	private UniTimeHeaderPanel iFooter = null;

	public SelectedSuggestion(SuggestionsPageContext context) {
		iContext = context;
		removeStyleName("unitime-NotPrintableBottomLine");
		iFooter = new UniTimeHeaderPanel();
		iFooter.addButton("assign", MESSAGES.buttonAssign(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iContext.assign(iSuggestion.getAssignment(true), iFooter);
			}
		});
	}
	
	public UniTimeHeaderPanel getFooter() { return iFooter; }

	@Override
	public void setValue(Suggestion suggestion) {
		iSuggestion = suggestion;
		clear();
		if (suggestion == null || !suggestion.hasDifferentAssignments()) return;
		if (suggestion.hasDifferentAssignments()) {
			AssignmentTable at = new AssignmentTable(iContext.getProperties(), false, true) {
				@Override
				protected void onRemove(ClassAssignmentDetails details) {
					iContext.remove(details.getClazz());
				}
			};
			for (ClassAssignmentDetails d: suggestion.getDifferentAssignments()) {
				at.addRow(d);
				if (d.hasConflict()) at.setColumnVisible(at.getCellIndex(AssignmentTable.AssignmentColumn.CONSTRAINT), true);
			}
			addHeaderRow(MESSAGES.headerSelectedAssignment());
			addRow(at);
			at.addMouseClickListener(new MouseClickListener<ClassAssignmentDetails>() {
				@Override
				public void onMouseClick(TableEvent<ClassAssignmentDetails> event) {
					if (event.getData() != null) iContext.select(event.getData().getClazz());
				}
			});
		}
		if (suggestion.hasUnresolvedConflicts()) {
			AssignmentTable at = new AssignmentTable(iContext.getProperties(), true, false);
			for (ClassAssignmentDetails d: suggestion.getUnresolvedConflicts()) {
				at.addRow(d);
				if (d.hasConflict()) at.setColumnVisible(at.getCellIndex(AssignmentTable.AssignmentColumn.CONSTRAINT), true);
			}
			addHeaderRow(MESSAGES.headerConflictingAssignments());
			addRow(at);
			at.addMouseClickListener(new MouseClickListener<ClassAssignmentDetails>() {
				@Override
				public void onMouseClick(TableEvent<ClassAssignmentDetails> event) {
					if (event.getData() != null) iContext.select(event.getData().getClazz());
				}
			});
		}
		if (suggestion.getUnassignedVariables() != suggestion.getBaseUnassignedVariables())
			addRow(MESSAGES.propNotAssignedClasses(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getUnassignedVariables(),suggestion.getBaseUnassignedVariables()), false));
		for (String criterion: new TreeSet<String>(suggestion.getCriteria().keySet())) {
			double value = suggestion.getCriterion(criterion);
			double base = suggestion.getBaseCriterion(criterion);
			if (value != base)
				addRow(criterion + ":",new HTML(SuggestionsPageContext.dispNumber(value, base), false));
		}
		/*
		if (suggestion.getViolatedStudentConflicts() != suggestion.getBaseViolatedStudentConflicts())
			addRow(MESSAGES.propStudentConflicts(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getViolatedStudentConflicts(),suggestion.getBaseViolatedStudentConflicts()), false));
		if (suggestion.getCommitedStudentConflicts() != suggestion.getBaseCommitedStudentConflicts())
			addRow(MESSAGES.propCommittedStudentConflicts(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getCommitedStudentConflicts(),suggestion.getBaseCommitedStudentConflicts()), false));
		if (suggestion.getHardStudentConflicts() != suggestion.getBaseHardStudentConflicts())
			addRow(MESSAGES.propHardStudentConflicts(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getHardStudentConflicts(),suggestion.getBaseHardStudentConflicts()), false));
		if (suggestion.getGlobalTimePreference() != suggestion.getBaseGlobalTimePreference())
			addRow(MESSAGES.propTimePreferences(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getGlobalTimePreference(),suggestion.getBaseGlobalTimePreference()), false));
		if (suggestion.getGlobalRoomPreference() != suggestion.getBaseGlobalRoomPreference())
			addRow(MESSAGES.propRoomPreferences(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getGlobalRoomPreference(),suggestion.getBaseGlobalRoomPreference()), false));
		if (suggestion.getGlobalGroupConstraintPreference() != suggestion.getBaseGlobalGroupConstraintPreference())
			addRow(MESSAGES.propDistributionPreferences(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getGlobalGroupConstraintPreference(),suggestion.getBaseGlobalGroupConstraintPreference()), false));
		if (suggestion.getInstructorDistancePreference() != suggestion.getBaseInstructorDistancePreference())
			addRow(MESSAGES.propBackToBackInstructorPreferences(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getInstructorDistancePreference(),suggestion.getBaseInstructorDistancePreference()), false));
		if (suggestion.getTooBigRooms() != suggestion.getBaseTooBigRooms())
			addRow(MESSAGES.propTooBigRooms(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getTooBigRooms(),suggestion.getBaseTooBigRooms()), false));
		if (suggestion.getUselessSlots() != suggestion.getBaseUselessSlots())
			addRow(MESSAGES.propUselessSlots(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getUselessSlots(),suggestion.getBaseUselessSlots()), false));
		if (suggestion.getDepartmentSpreadPenalty() != suggestion.getBaseDepartmentSpreadPenalty())
			addRow(MESSAGES.propDepartmentalBalancingPenalty(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getDepartmentSpreadPenalty(),suggestion.getBaseDepartmentSpreadPenalty()), false));
		if (suggestion.getSpreadPenalty() != suggestion.getBaseSpreadPenalty())
			addRow(MESSAGES.propSameSubpartBalancingPenalty(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getSpreadPenalty(),suggestion.getBaseSpreadPenalty()), false));
		if (suggestion.getPerturbationPenalty() != suggestion.getBasePerturbationPenalty())
			addRow(MESSAGES.propPerturbationPenalty(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getPerturbationPenalty(),suggestion.getBasePerturbationPenalty()), false));
		*/
		addRow(MESSAGES.propOverallSolutionValue(), new HTML(SuggestionsPageContext.dispNumber(suggestion.getValue(),suggestion.getBaseValue()), false));
		if (suggestion.hasStudentConflicts())
			addRow(MESSAGES.propStudentConflicts(), iContext.createStudentConflicts(suggestion.getStudentConflicts()));
		if (suggestion.hasViolatedDistributionConflicts() || suggestion.hasBtbInstructorConflicts())
			addRow(MESSAGES.propViolatedConstraints(), iContext.createViolatedConstraints(suggestion.getDistributionConflicts(), suggestion.getBtbInstructorConflicts()));
		iFooter.setEnabled("assign", iSuggestion.isCanAssign());
		iFooter.clearMessage();
		addBottomRow(iFooter);
	}

	@Override
	public Suggestion getValue() {
		return iSuggestion;
	}
}
