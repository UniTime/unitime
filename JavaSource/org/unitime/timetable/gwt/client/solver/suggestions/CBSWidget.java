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
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.CBSNode;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ConflictBasedStatisticsRequest;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class CBSWidget extends SimpleForm implements TakesValue<Long> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private SuggestionsPageContext iContext;
	private UniTimeHeaderPanel iHeader;
	private ConflictBasedStatisticsTree iTree;
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private Long iClassId = null;
	private boolean iCBSComputed = false;
	
	public CBSWidget(SuggestionsPageContext context) {
		super();
		iContext = context;
		iHeader = new UniTimeHeaderPanel(MESSAGES.headerCBS());
		iHeader.setCollapsible(SolverCookie.getInstance().isShowCBS());
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				SolverCookie.getInstance().setShowCBS(event.getValue());
				if (event.getValue()) {
					showCBS();
				} else {
					hideCBS();
				}
			}
		});
		removeStyleName("unitime-NotPrintableBottomLine");
	}
	
	public void onSelection(SelectedAssignment assignment) {}
	
	@Override
	public void setValue(Long classId) {
		iHeader.clearMessage();
		clear();
		iCBSComputed = false;
		if (iTree == null) {
			iTree = new ConflictBasedStatisticsTree(iContext.getProperties()) {
				@Override
				protected void onClick(ClickEvent event, CBSNode node) {
					if (node.hasSelection())
						onSelection(node.getSelection());
					else if (node.hasClassId())
						onSelection(new SelectedAssignment(node.getClassId()));
					else if (node.hasLink())
						openParent(node.getLink());
				}
			};
			iTree.setVisible(false);
		} else {
			iTree.setVisible(false);
		}
		addHeaderRow(iHeader);
		addRow(iTree);
		iClassId = classId;
		if (SolverCookie.getInstance().isShowCBS()) {
			showCBS();
		} else {
			hideCBS();
		}
	}
	
	@Override
	public Long getValue() {
		return iClassId;
	}
	
	protected void hideCBS() {
		iTree.setVisible(false);
	}
	
	protected void showCBS() {
		if (!iCBSComputed)
			computeCBS();
		else
			iTree.setVisible(true);
	}
	
	protected void computeCBS() {
		iCBSComputed = true;
		iTree.setVisible(false);
		iHeader.showLoading();
		
		RPC.execute(new ConflictBasedStatisticsRequest(iClassId), new AsyncCallback<GwtRpcResponseList<CBSNode>>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedToComputeConflicts(caught.getMessage()));
			}

			@Override
			public void onSuccess(GwtRpcResponseList<CBSNode> result) {
				iHeader.clearMessage();
				iTree.setValue(result);
				iTree.setVisible(true);
			}
		});
	}


	public native void openParent(String url) /*-{
		if ($wnd.parent)
			$wnd.parent.location = url;
		else
			$wnd.location = url;
	}-*/;	
}
