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

import java.util.List;

import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.solver.SolverCookie;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ComputeSuggestionsRequest;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignmentsRequest;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.Suggestion;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.Suggestions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class SuggestionsWidget extends SimpleForm {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private SuggestionsPageContext iContext;
	private UniTimeHeaderPanel iHeader, iFooter;
	private ComputeSuggestionsRequest iRequest;
	private SuggestionsTable iTable;
	private int iHeaderRow = 0, iFooterRow = 0;
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private boolean iSuggestionsComputed = false;
	private SuggestionsFilter iFilter = null;
	private AriaButton iApply = null;
	
	public SuggestionsWidget(SuggestionsPageContext context) {
		super(3);
		iContext = context;
		iHeader = new UniTimeHeaderPanel(MESSAGES.headerSuggestions());
		iHeader.setCollapsible(SolverCookie.getInstance().isShowSuggestions());
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				SolverCookie.getInstance().setShowSuggestions(event.getValue());
				if (event.getValue()) {
					showSuggestions();
				} else {
					hideSuggestions();
				}
			}
		});
		iFooter = new UniTimeHeaderPanel();
		iFooter.addButton("deeper", MESSAGES.buttonSearchDeeper(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iFilter.removeChip(new Chip("depth", null), false);
				iFilter.addChip(new Chip("depth", "D" + (iRequest.getDepth() + 1)), false);
				iRequest.setDepth(iRequest.getDepth() + 1);
				computeSuggestions();
			}
		});
		iFooter.addButton("longer", MESSAGES.buttonSearchLonger(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iFilter.removeChip(new Chip("timeout", null), false);
				iFilter.addChip(new Chip("timeout", "T" + (2 * iRequest.getTimeLimit() / 1000)), false);
				iRequest.setTimeLimit(2 * iRequest.getTimeLimit());
				computeSuggestions();
			}
		});
		removeStyleName("unitime-NotPrintableBottomLine");
		iApply = new AriaButton(MESSAGES.buttonSearch());
		iApply.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				computeSuggestions();
			}
		});
	}
	
	public void onSelection(Suggestion suggestion) {}
	
	public void clearMessage() {
		iHeader.clearMessage();
		iFooter.clearMessage();
	}
	
	public void setInfoMessage(String message) {
		iHeader.clearMessage();
		iFooter.setMessage(message);
	}
	
	public void showLoading() {
		iHeader.showLoading();
		iFooter.clearMessage();
	}
	
	public void setRequest(SelectedAssignmentsRequest request) {
		setSelection(request.getAssignments(), request.getClassId());
	}
	
	public void setSelection(List<SelectedAssignment> assignments, Long classId) {
		clearMessage();
		if (iFilter == null) {
			iFilter = new SuggestionsFilter();
			iFilter.setValue(SolverCookie.getInstance().getSuggestionsFilter());
		}
		iFilter.setClassId(classId);
		clear();
		iSuggestionsComputed = false;
		if (iTable == null) {
			iTable = new SuggestionsTable(iContext.getProperties(), true);
			iTable.setVisible(false);
			iTable.addMouseClickListener(new MouseClickListener<Suggestion>() {
				@Override
				public void onMouseClick(TableEvent<Suggestion> event) {
					if (event.getData() != null) onSelection(event.getData());
				}
			});
		} else {
			iTable.setVisible(false);
		}
		iHeaderRow = addHeaderRow(iHeader);
		int filterRow = addRow(MESSAGES.propFilter(), iFilter, 1);
		setWidget(filterRow, 2, iApply);
		addRow(iTable);
		iFooter.setEnabled("longer", false);
		iFooter.setEnabled("deeper", false);
		iFooterRow = addBottomRow(iFooter);
		getFlexCellFormatter().removeStyleName(iFooterRow, 0, "unitime-TopLine");
		iRequest = new ComputeSuggestionsRequest(classId, assignments);
		if (SolverCookie.getInstance().isShowSuggestions()) {
			showSuggestions();
		} else {
			hideSuggestions();
		}
	}
	
	protected void hideSuggestions() {
		for (int i = iHeaderRow + 1; i <= iFooterRow; i++)
			getRowFormatter().setVisible(i, false);
	}
	
	protected void showSuggestions() {
		for (int i = iHeaderRow + 1; i <= iFooterRow; i++)
			getRowFormatter().setVisible(i, true);
		if (!iSuggestionsComputed)
			computeSuggestions();
	}
	
	protected void computeSuggestions() {
		iSuggestionsComputed = true;
		iTable.setVisible(false);
		iApply.setEnabled(false);
		showLoading();
		Chip depth = iFilter.getChip("depth");
		if (depth != null)
			iRequest.setDepth(Integer.parseInt(depth.getValue().startsWith("D") || depth.getValue().startsWith("d") ? depth.getValue().substring(1) : depth.getValue()));
		Chip results = iFilter.getChip("results");
		if (results != null)
			iRequest.setLimit(Integer.parseInt(results.getValue().startsWith("R") || results.getValue().startsWith("r") ? results.getValue().substring(1) : results.getValue()));
		Chip timeout = iFilter.getChip("timeout");
		if (timeout != null)
			iRequest.setTimeLimit(1000 * Integer.parseInt(timeout.getValue().startsWith("T") || timeout.getValue().startsWith("t") ? timeout.getValue().substring(1) : timeout.getValue()));
		iRequest.setSameRoom(iFilter.hasChip(new Chip("flag", "Same Room")));
		iRequest.setSameTime(iFilter.hasChip(new Chip("flag", "Same Time")));
		iRequest.setAllowBreakHard(iFilter.hasChip(new Chip("flag", "Allow Break Hard")));
		iRequest.setPlacements(iFilter.hasChip(new Chip("mode", "Placements")));
		iRequest.setFilter(iFilter.getValue());
		iFooter.setEnabled("longer", false);
		iFooter.setEnabled("deeper", false);
		RPC.execute(iRequest, new AsyncCallback<Suggestions>() {

			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedToComputeSuggestions(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToComputeSuggestions(caught.getMessage()), caught);
				iApply.setEnabled(true);
			}

			@Override
			public void onSuccess(Suggestions result) {
				iTable.setValue(result.getSuggestions());
				iTable.setVisible(true);
				iApply.setEnabled(true);
				iFooter.setEnabled("longer", result.isTimeoutReached());
				iFooter.setEnabled("deeper", true);
				if (result.size() == 0) {
					if (result.isTimeoutReached())
						setInfoMessage(MESSAGES.suggestionsNoteTimeoutNoResults(result.getTimeLimit() / 1000, result.getNrCombinationsConsidered(), result.getDepth()));
					else
						setInfoMessage(MESSAGES.suggestionsNoteNoTimeoutNoResults(result.getNrCombinationsConsidered(), result.getDepth()));
				} else if (result.size() < result.getNrSolutions()) {
					if (result.isTimeoutReached())
						setInfoMessage(MESSAGES.suggestionsNoteTimeoutNResults(result.getTimeLimit() / 1000, result.getNrCombinationsConsidered(), result.getDepth(), result.getSuggestions().size(), result.getNrSolutions()));
					else
						setInfoMessage(MESSAGES.suggestionsNoteNoTimeoutNResults(result.getNrCombinationsConsidered(), result.getDepth(), result.getSuggestions().size(), result.getNrSolutions()));
				} else {
					if (result.isTimeoutReached())
						setInfoMessage(MESSAGES.suggestionsNoteTimeoutAllResults(result.getTimeLimit() / 1000, result.getNrCombinationsConsidered(), result.getDepth(), result.getSuggestions().size()));
					else
						setInfoMessage(MESSAGES.suggestionsNoteNoTimeoutAllResults(result.getNrCombinationsConsidered(), result.getDepth(), result.getSuggestions().size()));
				}
			}
		});
	}
}
