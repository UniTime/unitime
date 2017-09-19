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

import org.unitime.timetable.gwt.client.solver.SolverCookie;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassAssignmentDetails;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ComputeConflictTableRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class ConflictsWidget extends SimpleForm implements TakesValue<Long> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private SuggestionsPageContext iContext;
	private UniTimeHeaderPanel iHeader;
	private ConflictTable iTable;
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private Long iClassId = null;
	private boolean iConflictsComputed = false;
	
	public ConflictsWidget(SuggestionsPageContext context) {
		super();
		iContext = context;
		iHeader = new UniTimeHeaderPanel(MESSAGES.headerConflicts());
		iHeader.setCollapsible(SolverCookie.getInstance().isShowConflicts());
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				SolverCookie.getInstance().setShowConflicts(event.getValue());
				if (event.getValue()) {
					showConflicts();
				} else {
					hideConflicts();
				}
			}
		});
		removeStyleName("unitime-NotPrintableBottomLine");
	}
	
	public void onSelection(ClassAssignmentDetails conflict) {}
	
	@Override
	public void setValue(Long classId) {
		iHeader.clearMessage();
		clear();
		iConflictsComputed = false;
		if (iTable == null) {
			iTable = new ConflictTable(iContext);
			iTable.setVisible(false);
			iTable.addMouseClickListener(new MouseClickListener<ClassAssignmentDetails>() {
				@Override
				public void onMouseClick(TableEvent<ClassAssignmentDetails> event) {
					if (event.getData() != null) onSelection(event.getData());
				}
			});
		} else {
			iTable.setVisible(false);
		}
		addHeaderRow(iHeader);
		addRow(iTable);
		iClassId = classId;
		if (SolverCookie.getInstance().isShowConflicts()) {
			showConflicts();
		} else {
			hideConflicts();
		}
	}
	
	@Override
	public Long getValue() {
		return iClassId;
	}
	
	protected void hideConflicts() {
		iTable.setVisible(false);
	}
	
	protected void showConflicts() {
		if (!iConflictsComputed)
			computeConflicts();
		else
			iTable.setVisible(true);
	}
	
	protected void computeConflicts() {
		iConflictsComputed = true;
		iTable.setVisible(false);
		iHeader.showLoading();
		
		RPC.execute(new ComputeConflictTableRequest(iClassId), new AsyncCallback<GwtRpcResponseList<ClassAssignmentDetails>>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedToComputeConflicts(caught.getMessage()));
			}

			@Override
			public void onSuccess(GwtRpcResponseList<ClassAssignmentDetails> result) {
				iHeader.clearMessage();
				iTable.setValue(result);
				iTable.setVisible(true);
			}
		});
	}

}
