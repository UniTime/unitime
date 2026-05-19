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
package org.unitime.timetable.gwt.client.exams;

import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamConflictBasedStatisticsRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamConflictStatisticsFilterRequest;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.solver.PageFilter;
import org.unitime.timetable.gwt.client.solver.PreferenceLegend;
import org.unitime.timetable.gwt.client.solver.suggestions.ConflictBasedStatisticsTree;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.ConflictStatisticsFilterResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessageType;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.CBSNode;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Tomas Muller
 */
public class ExamConflictBasedStatisticsPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final ExaminationMessages XMSG = GWT.create(ExaminationMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private PageFilter iFilter;
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private FilterInterface iLastFilter;
	private GwtRpcResponseList<CBSNode> iLastResponse;
	private ConflictStatisticsFilterResponse iFilterResponse;
	private ConflictBasedStatisticsTree iTree;
	private PreferenceLegend iLegend;

	public ExamConflictBasedStatisticsPage() {
		iFilter = new PageFilter();
		iFilter.getHeader().setCollapsible("1".equals(ToolBox.getSessionCookie("Exams.Filter")));
		iFilter.getHeader().addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				ToolBox.setSessionCookie("Exams.Filter", event.getValue() ? "1" : "0");
			}
		});
		
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		iPanel.addRow(iFilter);
		
		iFilter.getFooter().addButton("search", MESSAGES.buttonSearch(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String token = iFilter.getQuery();
				if (!History.getToken().equals(token))
					History.newItem(token, false);
				search(null);
			}
		});
		iFilter.getFooter().setEnabled("search", false);
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ConflictBasedStatisticsPage");
		initWidget(iRootPanel);
		init();
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iFilter.setQuery(event.getValue(), true);
				if (iPanel.getRowCount() > 1)
					search(null);
			}
		});
	}

	protected void init() {
		RPC.execute(new ExamConflictStatisticsFilterRequest(), new AsyncCallback<ConflictStatisticsFilterResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iFilter.getFooter().setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ConflictStatisticsFilterResponse result) {
				iLegend = new PreferenceLegend(result.getSuggestionProperties().getPreferences());
				iFilterResponse = result;
				iFilter.getFooter().clearMessage();
				iFilter.setValue(result);
				iFilter.getFooter().setEnabled("search", true);
				createTriggers();
				if (iFilter.getHeader().isCollapsible() != null && !iFilter.getHeader().isCollapsible())
					search(null);
			}
		});
	}
	
	public static native void createTriggers()/*-{
		$wnd.refreshPage = function() {
		};
	}-*/;
	
	protected void search(final AsyncCallback<Boolean> callback) {
		final ExamConflictBasedStatisticsRequest request = new ExamConflictBasedStatisticsRequest();
		final FilterInterface filter = iFilter.getValue();
		try {
			request.setLimit(Double.valueOf(filter.getParameterValue("limit", "25.0")));
		} catch (NumberFormatException e) {}
		request.setVariableOriented("0".equals(filter.getParameterValue("mode", "0")));
		iFilter.getFooter().clearMessage();
		for (int row = iPanel.getRowCount() - 1; row > 0; row--)
			iPanel.removeRow(row);
		iFilter.getFooter().showLoading();
		iFilter.getFooter().setEnabled("search", false);
		LoadingWidget.showLoading(MESSAGES.waitLoadingData());
		RPC.execute(request, new AsyncCallback<GwtRpcResponseList<CBSNode>>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				iFilter.getFooter().setErrorMessage(MESSAGES.failedToLoadConflictStatistics(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadConflictStatistics(caught.getMessage()), caught);
				iFilter.getFooter().setEnabled("search", true);
				if (callback != null)
					callback.onFailure(caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<CBSNode> result) {
				LoadingWidget.hideLoading();
				iFilter.getFooter().clearMessage();
				populate(filter, result);
				iFilter.getFooter().setEnabled("search", true);
				if (callback != null)
					callback.onSuccess(result != null && !result.isEmpty());
			}
		});
	}
	
	protected void populate(FilterInterface filter, GwtRpcResponseList<CBSNode> response) {
		iLastFilter = filter;
		iLastResponse = response;
		for (int row = iPanel.getRowCount() - 1; row > 0; row--)
			iPanel.removeRow(row);
		
		RootPanel cpm = RootPanel.get("UniTimeGWT:CustomPageMessages");
		if (cpm != null && iFilterResponse != null) {
			cpm.clear();
			if (iFilterResponse.hasPageMessages()) {
				for (final PageMessage pm: iFilterResponse.getPageMessages()) {
					P p = new P(pm.getType() == PageMessageType.ERROR ? "unitime-PageError" : pm.getType() == PageMessageType.WARNING ? "unitime-PageWarn" : "unitime-PageMessage");
					p.setHTML(pm.getMessage());
					if (pm.hasUrl()) {
						p.addStyleName("unitime-ClickablePageMessage");
						p.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (pm.hasUrl()) ToolBox.open(GWT.getHostPageBaseURL() + pm.getUrl());
							}
						});
					}
					cpm.add(p);
				}
			}
		}
		
		if (response == null || response.isEmpty()) {
			iFilter.getFooter().setMessage(MESSAGES.errorConflictStatisticsNoDataReturned());
			return;
		}
		
		if (iTree == null) {
			iTree = new ConflictBasedStatisticsTree(iFilterResponse.getSuggestionProperties()) {
				protected void onClick(ClickEvent event, CBSNode node) {
					if (node.hasLink()) {
						ToolBox.open(GWT.getHostPageBaseURL() + node.getLink());
					} else if (node.hasClassId()) {
						UniTimeFrameDialog.openDialog(XMSG.dialogExamAssign(), "examInfo.action?menu=hide&examId=" + node.getClassId(), "900", "85%");
					} else if (node.hasSelection()) {
						SelectedAssignment selection = node.getSelection();
						UniTimeFrameDialog.openDialog(XMSG.dialogExamAssign(), "examInfo.action?menu=hide&examId=" + selection.getClassId()
							+ "&period=" + selection.getPatternId() + "&room=" + selection.getRoomIds(":") + "&op=Try&reset=1", "900", "85%");
					}
				}
			};
		}
		iTree.setValue(response);
		iPanel.addRow(iTree);
		iPanel.addRow(iLegend);
	}
}
