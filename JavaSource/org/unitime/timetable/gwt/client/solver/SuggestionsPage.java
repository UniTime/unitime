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
package org.unitime.timetable.gwt.client.solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.solver.suggestions.CBSWidget;
import org.unitime.timetable.gwt.client.solver.suggestions.ConflictsWidget;
import org.unitime.timetable.gwt.client.solver.suggestions.CurrentAssignment;
import org.unitime.timetable.gwt.client.solver.suggestions.SelectedSuggestion;
import org.unitime.timetable.gwt.client.solver.suggestions.SuggestionsPageContext;
import org.unitime.timetable.gwt.client.solver.suggestions.SuggestionsWidget;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassAssignmentDetails;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassAssignmentDetailsRequest;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.MakeAssignmentRequest;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignmentsRequest;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.Suggestion;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SuggestionProperties;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SuggestionPropertiesRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class SuggestionsPage extends SimpleForm {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	public static NumberFormat sDF = NumberFormat.getFormat("0.###");
	
	private SuggestionsPageContext iContext = null;
	private CurrentAssignment iCurrentAssignment = null;
	private SelectedSuggestion iSelectedSuggestion = null;
	private SuggestionsWidget iSuggestions = null;
	private ConflictsWidget iConflicts = null;
	private CBSWidget iCBS = null;
	
	private List<SelectedAssignment> iSelectedAssignments = null;
	
	public SuggestionsPage(final Long classId) {
		addStyleName("unitime-SuggestionsPage");
		removeStyleName("unitime-NotPrintableBottomLine");
		iContext = new SuggestionsPageContext() {
			@Override
			public void select(ClassInfo clazz) {
				setup(clazz.getClassId(), false);
			}
			
			@Override
			public void onSelection(Command undo) {
				selectAssignment(iCurrentAssignment.getSelectedAssignment(), undo);
			}

			@Override
			public void remove(ClassInfo clazz) {
				unselectAssignment(clazz);
			}

			@Override
			public void assign(List<SelectedAssignment> assignment, final UniTimeHeaderPanel panel) {
				MakeAssignmentRequest request = new MakeAssignmentRequest(assignment);
				panel.showLoading();
				RPC.execute(request, new AsyncCallback<GwtRpcResponseNull>() {
					@Override
					public void onFailure(Throwable t) {
						panel.setErrorMessage(MESSAGES.failedToAssign(t.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedToAssign(t.getMessage()), t);
					}

					@Override
					public void onSuccess(GwtRpcResponseNull response) {
						panel.clearMessage();
						if (!closeSuggestionsDialog()) {
							if (iSelectedAssignments != null) iSelectedAssignments.clear();
							iSelectedSuggestion.setValue(null);
							setup(iCurrentAssignment.getValue().getClazz().getClassId(), true);
						}
					}
				});
			}
		};
		
		SelectedAssignment assignment = getLocationAssignment();
		if (assignment != null) {
			iSelectedAssignments = new ArrayList<SelectedAssignment>();
			iSelectedAssignments.add(assignment);
		}
		
		SuggestionPropertiesRequest request = new SuggestionPropertiesRequest();
		if (Location.getParameter("history") != null)
			request.setHistoryId(Long.valueOf(Location.getParameter("history")));
		RPC.execute(request, new AsyncCallback<SuggestionProperties>() {

			@Override
			public void onFailure(Throwable t) {
				UniTimeNotifications.error(MESSAGES.failedToInitialize(t.getMessage()), t);
				ToolBox.checkAccess(t);
			}

			@Override
			public void onSuccess(SuggestionProperties properties) {
				iContext.setSuggestionProperties(properties);
				if (properties.hasSelectedAssignments())
					iSelectedAssignments = properties.getSelectedAssignments();
				if (properties.isSolver()) {
					iSuggestions = new SuggestionsWidget(iContext) {
						@Override
						public void onSelection(Suggestion suggestion) {
							iCurrentAssignment.clearMessage();
							iCurrentAssignment.setShowUnassign(!suggestion.hasDifferentAssignments());
							iCurrentAssignment.setSelectedAssignment(suggestion.getAssignment(iCurrentAssignment.getValue().getClazz().getClassId()));
							iSelectedAssignments = suggestion.getAssignment(false);
							iSelectedSuggestion.setValue(suggestion);
							iSelectedSuggestion.getElement().scrollIntoView();
						}
					};
					addRow(iSuggestions);
					iConflicts = new ConflictsWidget(iContext) {
						@Override
						public void onSelection(ClassAssignmentDetails details) {
							iCurrentAssignment.clearMessage();
							iCurrentAssignment.setSelectedTime(details.getTime(), true);
							iCurrentAssignment.getElement().scrollIntoView();
						}
					};
					addRow(iConflicts);
					iCBS = new CBSWidget(iContext) {
						@Override
						public void onSelection(SelectedAssignment assignment) {
							if (assignment.getClassId().equals(iCurrentAssignment.getValue().getClazz().getClassId())) {
								iCurrentAssignment.clearMessage();
								final SelectedAssignment undo = iCurrentAssignment.getSelectedAssignment();
								iCurrentAssignment.setSelectedAssignment(assignment);
								iCurrentAssignment.getElement().scrollIntoView();
								iContext.onSelection(new Command() {
									@Override
									public void execute() {
										iCurrentAssignment.setSelectedAssignment(undo);
									}
								});
							} else {
								if (iSelectedAssignments == null)
									iSelectedAssignments = new ArrayList<SelectedAssignment>();
								else
									iSelectedAssignments.clear();
								iSelectedAssignments.add(assignment);
								setup(assignment.getClassId(), true);
							}
						}
					};
					addRow(iCBS);
				}
				setup(classId, true);
			}
		});
		
		iCurrentAssignment = new CurrentAssignment(iContext);
		addRow(iCurrentAssignment);
		iSelectedSuggestion = new SelectedSuggestion(iContext);
		addRow(iSelectedSuggestion);
	}
	
	public SelectedAssignment getLocationAssignment() {
		SelectedAssignment ret = new SelectedAssignment();
		if (Location.getParameter("id") != null)
			ret.setClassId(Long.valueOf(Location.getParameter("id")));
		else
			return null;
		if (Location.getParameter("days") != null)
			ret.setDays(Integer.valueOf(Location.getParameter("days")));
		else
			return null;
		if (Location.getParameter("slot") != null)
			ret.setStartSlot(Integer.valueOf(Location.getParameter("slot")));
		else
			return null;
		if (Location.getParameter("pid") != null)
			ret.setPatternId(Long.valueOf(Location.getParameter("pid")));
		else
			return null;
		if (Location.getParameter("did") != null)
			ret.setDatePatternId(Long.valueOf(Location.getParameter("did")));
		else
			return null;
		if (Location.getParameter("room") != null) {
			for (String id: Location.getParameter("room").split(","))
				ret.addRoomId(Long.valueOf(id));
		}
		return ret;
	}
	
	public SuggestionsPage() {
		this(Long.valueOf(Location.getParameter("id")));
	}
	
	protected void setup(final Long classId, final boolean computeSuggestion) {
		LoadingWidget.showLoading(MESSAGES.waitLoadClassDetails());
		RPC.execute(new ClassAssignmentDetailsRequest(classId), new AsyncCallback<ClassAssignmentDetails>() {
			@Override
			public void onFailure(Throwable t) {
				ToolBox.checkAccess(t);
				LoadingWidget.hideLoading();
				UniTimeNotifications.error(MESSAGES.failedToLoadClassDetails(t.getMessage()), t);
			}

			@Override
			public void onSuccess(ClassAssignmentDetails details) {
				LoadingWidget.hideLoading();
				iCurrentAssignment.setValue(details);
				if (iSelectedAssignments != null && !iSelectedAssignments.isEmpty()) {
					for (Iterator<SelectedAssignment> i =  iSelectedAssignments.iterator(); i.hasNext(); ) {
						SelectedAssignment a = i.next();
						if (a.getClassId().equals(classId)) {
							iCurrentAssignment.setSelectedAssignment(a);
							if (iCurrentAssignment.getSelectedAssignment() == null)
								i.remove();
						}
					}
					iCurrentAssignment.setShowUnassign(iSelectedAssignments.isEmpty());
				} else {
					iCurrentAssignment.setShowUnassign(true);
				}
				if (iSuggestions != null) {
					iSuggestions.setVisible(details.isCanUnassign());
					if (details.isCanUnassign()) iSuggestions.setSelection(iSelectedAssignments, classId);
				}
				if (iConflicts != null) {
					iConflicts.setVisible(details.isCanUnassign());
					if (details.isCanUnassign()) iConflicts.setValue(classId);
				}
				if (iCBS != null) {
					iCBS.setVisible(details.isCanUnassign());
					if (details.isCanUnassign()) iCBS.setValue(classId);
				}
				if (computeSuggestion && details.isCanUnassign() && iSelectedAssignments != null && !iSelectedAssignments.isEmpty()) {
					SelectedAssignmentsRequest request = new SelectedAssignmentsRequest(classId);
					for (SelectedAssignment a: iSelectedAssignments)
						request.addAssignment(a);
					computeSuggestion(request, null);
				}
			}
		});
		
	}
	
	protected void computeSuggestion(final SelectedAssignmentsRequest request, final Command undo) {
		iCurrentAssignment.showLoading();
		RPC.execute(request, new AsyncCallback<Suggestion>() {
			@Override
			public void onFailure(Throwable t) {
				if (undo != null) undo.execute();
				iCurrentAssignment.setErrorMessage(MESSAGES.failedToComputeSelectedAssignment(t.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToComputeSelectedAssignment(t.getMessage()), t);
			}

			@Override
			public void onSuccess(Suggestion suggestion) {
				iCurrentAssignment.clearMessage();
				iCurrentAssignment.setShowUnassign(!suggestion.hasDifferentAssignments());
				iSelectedSuggestion.setValue(suggestion);
				if (iSuggestions != null) iSuggestions.setRequest(request);
			}
		});
	}
	
	protected void selectAssignment(SelectedAssignment selection, Command undo) {
		if (selection == null) return;
		SelectedAssignmentsRequest request = new SelectedAssignmentsRequest(selection.getClassId());
		if (iSelectedAssignments != null) {
			for (Iterator<SelectedAssignment> i =  iSelectedAssignments.iterator(); i.hasNext(); ) {
				SelectedAssignment a = i.next();
				if (!a.getClassId().equals(request.getClassId()))
					request.addAssignment(a);
				else
					i.remove();
			}
			iSelectedAssignments.add(selection);
		} else {
			iSelectedAssignments = new ArrayList<SelectedAssignment>();
			iSelectedAssignments.add(selection);
		}
		request.addAssignment(selection);
		computeSuggestion(request, undo);
	}
	
	protected void unselectAssignment(ClassInfo clazz) {
		if (iSelectedAssignments == null || clazz == null) return;
		SelectedAssignmentsRequest request = new SelectedAssignmentsRequest(iCurrentAssignment.getValue().getClazz().getClassId());
		for (Iterator<SelectedAssignment> i =  iSelectedAssignments.iterator(); i.hasNext(); ) {
			SelectedAssignment a = i.next();
			if (!a.getClassId().equals(clazz.getClassId())) {
				request.addAssignment(a);
			} else {
				i.remove();
			}
		}
		if (clazz.equals(iCurrentAssignment.getValue().getClazz())) {
			iCurrentAssignment.setSelectedAssignment(null);
			SelectedAssignment selection = iCurrentAssignment.getValue().getSelection();
			if (selection != null)
				iCurrentAssignment.setSelectedAssignment(selection);
		}
		computeSuggestion(request, null);
	}

	public native boolean closeSuggestionsDialog() /*-{
		if ($wnd.parent) {
			$wnd.parent.hideGwtDialog();
			$wnd.parent.refreshPage();
			return true;
		} else {
			return false;
		}
	}-*/;
}
