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
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.ComputeSuggestionsRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorAssignmentRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SuggestionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SuggestionsResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingAssignmentsDetailRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestDetailRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */

public class TeachingRequestDetailPage extends UniTimeDialogBox {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static final StudentSectioningMessages SECTMSG = GWT.create(StudentSectioningMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static NumberFormat sTeachingLoadFormat = NumberFormat.getFormat(CONSTANTS.teachingLoadFormat());
	protected static NumberFormat sSuggestionScoreFormat = NumberFormat.getFormat(CONSTANTS.suggestionScoreFormat());
	private TeachingRequestsPagePropertiesResponse iProperties;

	private SimpleForm iForm;
	private ScrollPanel iScroll;
	
	private TeachingRequestDetails iRequestDetails = null;
	private InstructorDetails iInstructorDetails = null;
	private int iDetailsRow;
	
	private UniTimeHeaderPanel iAssignmentHeader;
	private UniTimeTable<AssignmentInfo> iAssignmentTable;
	private Label iAssignmentScore;
	private ObjectivesCell iAssignmentObjectives;
	private int iAssignmentRow;
	private int iAssignmentScoreRow, iAssignmentObjectivesRow;
	private CheckBox iIgnoreConflicts;
	private int iIgnoreConflictsRow = -1;
	
	private UniTimeHeaderPanel iDomainHeader;
	private int iDomainRow;
	private UniTimeTable<SuggestionInfo> iDomainTable;
	
	private int iSuggestionsRow;
	private UniTimeHeaderPanel iSuggestionsHeader;
	
	private TeachingRequestInfo iRequest;
	private InstructorInfo iInstructor;
	private ComputeSuggestionsRequest iSuggestionsRequest;
	private UniTimeTable<SuggestionInfo> iSuggestionsTable;
	
	public TeachingRequestDetailPage(TeachingRequestsPagePropertiesResponse properties) {
		super(true, true);
		setEscapeToHide(true);
		addStyleName("unitime-TeachingRequestDetail");
		iForm = new SimpleForm();
		iForm.addStyleName("detail");
		iScroll = new ScrollPanel(iForm);
		iScroll.setStyleName("scroll");
		setWidget(iScroll);
		iProperties = properties;
		addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
			}
		});
		
		setupDetails();
		setupSelectedAssignments();
		setupDomain();
		setupSuggestions();

		UniTimeHeaderPanel footer = new UniTimeHeaderPanel();
		footer.addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		iForm.addBottomRow(footer);
	}
	
	protected void onAssignmentChanged(List<AssignmentInfo> assignments) {}
	
	public void showRequestDetail(Long id) {
		iAssignmentTable.clearTable(1);
		LoadingWidget.getInstance().show(MESSAGES.waitLoadTeachingRequestDetail());
		ToolBox.setMaxHeight(iScroll.getElement().getStyle(), Math.round(0.9 * Window.getClientHeight()) + "px");
		RPC.execute(new TeachingRequestDetailRequest(id), new AsyncCallback<TeachingRequestInfo>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				UniTimeNotifications.error(MESSAGES.failedToLoadTeachingRequestDetail(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(TeachingRequestInfo result) {
				LoadingWidget.getInstance().hide();
				populate(result, null, null);
				GwtHint.hideHint();
				center();
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
			}
		});
	}
	
	public void showInstructorDetail(Long id) {
		iAssignmentTable.clearTable(1);
		LoadingWidget.getInstance().show(MESSAGES.waitLoadTeachingRequestDetail());
		ToolBox.setMaxHeight(iScroll.getElement().getStyle(), Math.round(0.9 * Window.getClientHeight()) + "px");
		RPC.execute(new TeachingAssignmentsDetailRequest(id), new AsyncCallback<InstructorInfo>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				UniTimeNotifications.error(MESSAGES.failedToLoadTeachingRequestDetail(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(InstructorInfo result) {
				LoadingWidget.getInstance().hide();
				populate(null, null, result);
				GwtHint.hideHint();
				center();
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
			}
		});
	}
	
	protected void computeSuggestions(final Integer scrollToRow) {
		// if (!iProperties.isHasSolver()) return;
		ComputeSuggestionsRequest request = new ComputeSuggestionsRequest();
		for (AssignmentInfo assignment: iAssignmentTable.getData()) {
			if (assignment.getInstructor() != null) {
				request.addAssignment(assignment);
			}
		}
		computeSuggestions(request, scrollToRow);
	}
	
	protected void scrollToRow(Integer row) {
		if (row != null) {
			Element scroll = iScroll.getElement();
			Element item = iForm.getRowFormatter().getElement(row);
			int realOffset = 0;
			while (item !=null && !item.equals(scroll)) {
				realOffset += item.getOffsetTop();
				item = item.getOffsetParent();
			}
			scroll.setScrollTop(realOffset - scroll.getOffsetHeight() / 2);
		}
	}
	
	protected void computeSuggestions(final ComputeSuggestionsRequest request, final Integer scrollToRow) {
		// if (!iProperties.isHasSolver()) return;
		iSuggestionsRequest = request;
		if (iRequest != null) {
			iSuggestionsRequest.setSelectedRequestId(iRequest.getRequestId());
			iSuggestionsRequest.setSelectedIndex(iRequestDetails.getValue());
		} else {
			iSuggestionsRequest.setSelectedInstructorId(iInstructor.getInstructorId());
			Integer index = iInstructorDetails.getValue();
			if (index != null && index < iInstructor.getAssignedRequests().size()) { 
				iSuggestionsRequest.setSelectedRequestId(iInstructor.getAssignedRequests().get(index).getRequestId());
			}
		}
		if (iProperties.isHasSolver()) {
			if (request.isComputeSuggestions()) showSuggestionsLoading();
			if (request.isComputeDomain()) showDomainLoading();
		} else {
			showDomainLoading();
			hideSuggestions();
		}
		if (request.isComputeDomain() && request.isComputeSuggestions())
			showAssignmentLoading();
		RPC.execute(iSuggestionsRequest, new AsyncCallback<SuggestionsResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iSuggestionsHeader.setErrorMessage(MESSAGES.failedToComputeSuggestions(caught.getMessage()));
				iSuggestionsTable.setVisible(false);
			}

			@Override
			public void onSuccess(SuggestionsResponse result) {
				showAssignment(result.getCurrentAssignment());
				if (request.isComputeSuggestions())
					showSuggestions(result);
				if (request.isComputeDomain()) {
					showDomain(result.getDomainValues(), result.getDomainSize());
					iDomainHeader.setEnabled("more", result.hasDomainValues() && result.getDomainValues().size() < result.getDomainSize());
				}
				center();
				scrollToRow(scrollToRow);
			}
		});		
	}
	
	protected void hideSuggestions() {
		iSuggestionsTable.clearTable(1);
		iForm.getRowFormatter().setVisible(iSuggestionsRow, false);
		iForm.getRowFormatter().setVisible(iSuggestionsRow + 1, false);
	}
	
	protected void showSuggestionsLoading() {
		iForm.getRowFormatter().setVisible(iSuggestionsRow, true);
		iForm.getRowFormatter().setVisible(iSuggestionsRow + 1, true);
		iSuggestionsHeader.showLoading();
		iSuggestionsHeader.setEnabled("longer", false);
		iSuggestionsHeader.setEnabled("deeper", false);
		iSuggestionsTable.setVisible(false);
	}
	
	protected void showSuggestions(SuggestionsResponse result) {
		if (!iProperties.isHasSolver()) {
			hideSuggestions();
			return;
		}
		iSuggestionsHeader.clearMessage();
		iSuggestionsTable.clearTable(1);
		if (result.hasSuggestions()) {
			for (SuggestionInfo suggestion: result.getSuggestions()) {
				List<Widget> line = new ArrayList<Widget>();
				Label score = new Label(sSuggestionScoreFormat.format(suggestion.getValue()));
				if (suggestion.getValue() > 0) score.getElement().getStyle().setColor("red");
				if (suggestion.getValue() < 0) score.getElement().getStyle().setColor("green");
				line.add(score);
				
				P courses = new P();
				P sections = new P();
				P extIdOld = new P("initial");
				P extIdArrows = new P("arrow");
				P extIdNew = new P("current");
				P nameOld = new P("initial");
				P nameArrows = new P("arrow");
				P nameNew = new P("current");
				for (AssignmentInfo assignment: suggestion.getAssignments()) {
					P course = new P("course"); course.setText(assignment.getRequest().getCourse().getCourseName());
					courses.add(course);
					P section = new P("sections");
					for (Iterator<SectionInfo> i = assignment.getRequest().getSections().iterator(); i.hasNext(); ) {
						SectionInfo s = i.next();
						P p = new P("section");
						p.setText(s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId()) + (i.hasNext() ? "," : ""));
						if (s.isCommon()) p.addStyleName("common");
						section.add(p);
					}
					sections.add(section);
					InstructorInfo initial = assignment.getRequest().getInstructor(assignment.getIndex());
					InstructorInfo current = assignment.getInstructor();
					extIdOld.add(new InstructorExternalIdCell(iProperties, initial));
					P extIdArrow = new P(); extIdArrow.setHTML(MESSAGES.assignmentArrow()); extIdArrows.add(extIdArrow);
					extIdNew.add(new InstructorExternalIdCell(iProperties, current));
					nameOld.add(new InstructorNameCell(iProperties, initial));
					P nameArrow = new P(); nameArrow.setHTML(MESSAGES.assignmentArrow()); nameArrows.add(nameArrow);
					nameNew.add(new InstructorNameCell(iProperties, current));
				}
				
				line.add(courses);
				line.add(sections);
				line.add(extIdOld);
				line.add(extIdArrows);
				line.add(extIdNew);
				line.add(nameOld);
				line.add(nameArrows);
				line.add(nameNew);
				line.add(new ObjectivesCell(iProperties, suggestion.getValues()));
				iSuggestionsTable.addRow(suggestion, line);
			}
			if (result.getSuggestions().size() < result.getNrSolutions()) {
				if (result.isTimeoutReached())
					iSuggestionsTable.addRow(null, new Note(10, MESSAGES.suggestionsNoteTimeoutNResults(iSuggestionsRequest.getTimeout() / 1000, result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept(), result.getSuggestions().size(), result.getNrSolutions())));
				else
					iSuggestionsTable.addRow(null, new Note(10, MESSAGES.suggestionsNoteNoTimeoutNResults(result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept(), result.getSuggestions().size(), result.getNrSolutions())));
			} else {
				if (result.isTimeoutReached())
					iSuggestionsTable.addRow(null, new Note(10, MESSAGES.suggestionsNoteTimeoutAllResults(iSuggestionsRequest.getTimeout() / 1000, result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept(), result.getSuggestions().size())));
				else
					iSuggestionsTable.addRow(null, new Note(10, MESSAGES.suggestionsNoteNoTimeoutAllResults(result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept(), result.getSuggestions().size())));						
			}
		} else {
			if (result.isTimeoutReached())
				iSuggestionsTable.addRow(null, new Note(10, MESSAGES.suggestionsNoteTimeoutNoResults(iSuggestionsRequest.getTimeout() / 1000, result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept())));
			else
				iSuggestionsTable.addRow(null, new Note(10, MESSAGES.suggestionsNoteNoTimeoutNoResults(result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept())));
		}
		iSuggestionsTable.setVisible(true);
		iSuggestionsHeader.setEnabled("longer", result.isTimeoutReached());
		iSuggestionsHeader.setEnabled("deeper", true);
	}
	
	protected void showAssignmentLoading() {
		if (iAssignmentTable.getRowCount() > 1) {
			iAssignmentHeader.setEnabled("assign", false);
			iAssignmentHeader.showLoading();
		}
	}
	
	protected void showAssignment(SuggestionInfo suggestion) {
		iAssignmentHeader.clearMessage();
		iAssignmentTable.clearTable(1);
		if (suggestion != null) {
			for (final AssignmentInfo assignment: suggestion.getAssignments()) {
				List<Widget> line = new ArrayList<Widget>();
				P buttons = new P("buttons");
				line.add(buttons);
				P course = new P("course"); course.setText(assignment.getRequest().getCourse().getCourseName());
				line.add(course);
				P section = new P("sections");
				for (Iterator<SectionInfo> i = assignment.getRequest().getSections().iterator(); i.hasNext(); ) {
					SectionInfo s = i.next();
					P p = new P("section");
					p.setText(s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId()) + (i.hasNext() ? "," : ""));
					if (s.isCommon()) p.addStyleName("common");
					section.add(p);
				}
				line.add(section);
				InstructorInfo initial = assignment.getRequest().getInstructor(assignment.getIndex());
				InstructorInfo current = assignment.getInstructor();
				if ((initial == null && current == null) || (initial != null && initial.equals(current))) {
					line.add(new P());
					line.add(new P());
				} else {
					P extIdOld = new InstructorExternalIdCell(iProperties, initial); extIdOld.addStyleName("initial"); line.add(extIdOld);
					P extIdArrow = new P(); extIdArrow.setHTML(MESSAGES.assignmentArrow()); line.add(extIdArrow);
				}
				InstructorExternalIdCell extIdNew = new InstructorExternalIdCell(iProperties, current); extIdNew.addStyleName("current"); line.add(extIdNew);
				if ((initial == null && current == null) || (initial != null && initial.equals(current))) {
					line.add(new P());
					line.add(new P());
				} else {
					InstructorNameCell nameOld = new InstructorNameCell(iProperties, initial); nameOld.addStyleName("initial"); line.add(nameOld);
					P nameArrow = new P(); nameArrow.setHTML(MESSAGES.assignmentArrow()); line.add(nameArrow);
				}
				InstructorNameCell nameNew = new InstructorNameCell(iProperties, current); nameNew.addStyleName("current"); line.add(nameNew);
				if (current != null) {
					line.add(new AttributesCell(current.getAttributes()));
					line.add(new PreferenceCell(iProperties, current.getCoursePreferences()));
					line.add(new TimePreferenceCell(iProperties, current));
					line.add(new PreferenceCell(iProperties, current.getDistributionPreferences()));
				} else if (initial != null) {
					line.add(new AttributesCell(initial.getAttributes()));
					line.add(new PreferenceCell(iProperties, initial.getCoursePreferences()));
					line.add(new TimePreferenceCell(iProperties, initial));
					line.add(new PreferenceCell(iProperties, initial.getDistributionPreferences()));
				} else {
					line.add(new P());
					line.add(new P());
					line.add(new P());
					line.add(new P());
				}
				ObjectivesCell obj = new ObjectivesCell(iProperties, initial == null ? null : initial.getValues(), current == null ? null : current.getValues());
				if (assignment.hasConflicts()) {
					P confs = new P("conflicts");
					for (String text: assignment.getConflicts()) {
						P conf = new P("conflict"); conf.setText(text);
						confs.add(conf);
					}
					obj.add(confs);
				}
				line.add(obj);
				if (current != null) {
					Image delete = new Image(RESOURCES.delete());
					delete.addStyleName("delete");
					delete.setTitle(MESSAGES.titleDeleteRow());
					delete.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							event.preventDefault(); event.stopPropagation();
							ComputeSuggestionsRequest request = new ComputeSuggestionsRequest();
							for (AssignmentInfo a: iAssignmentTable.getData()) {
								if (a.getInstructor() != null && !a.equals(assignment)) {
									request.addAssignment(a);
								}
							}
							computeSuggestions(request, iAssignmentRow);
						}
					});
					buttons.add(delete);
				}
				iAssignmentTable.addRow(assignment, line);
			}
			iAssignmentScore.setText(sSuggestionScoreFormat.format(suggestion.getValue()));
			if (suggestion.getValue() > 0) iAssignmentScore.getElement().getStyle().setColor("red");
			if (suggestion.getValue() < 0) iAssignmentScore.getElement().getStyle().setColor("green");
			iAssignmentObjectives.setValue(suggestion.getValues());
		}
		iForm.getRowFormatter().setVisible(iAssignmentRow, iAssignmentTable.getRowCount() > 1);
		iForm.getRowFormatter().setVisible(iAssignmentRow + 1, iAssignmentTable.getRowCount() > 1);
		iForm.getRowFormatter().setVisible(iAssignmentScoreRow, iAssignmentTable.getRowCount() > 1 && suggestion != null && suggestion.getValue() != 0.0);
		iForm.getRowFormatter().setVisible(iAssignmentObjectivesRow, iAssignmentTable.getRowCount() > 1 && suggestion != null && suggestion.hasValues());
		if (iIgnoreConflicts != null) {
			iForm.getRowFormatter().setVisible(iIgnoreConflictsRow, iAssignmentTable.getRowCount() > 1);
			if (iIgnoreConflicts.getValue())
				for (int i = 1; i < iAssignmentTable.getRowCount(); i++) {
					AssignmentInfo ai = iAssignmentTable.getData(i);
					if (ai != null && ai.getInstructor() == null) iAssignmentTable.getRowFormatter().setVisible(i, false);
				}
		}
		iAssignmentHeader.setEnabled("assign", iAssignmentTable.getRowCount() > 1);
	}
	
	protected void hideDomain() {
		iForm.getRowFormatter().setVisible(iDomainRow, false);
		iForm.getRowFormatter().setVisible(iDomainRow + 1, false);
	}
	
	protected void showDomainLoading() {
		iDomainHeader.setEnabled("more", false);
		iForm.getRowFormatter().setVisible(iDomainRow, true);
		iForm.getRowFormatter().setVisible(iDomainRow + 1, true);
		if (iRequest != null) {
			iDomainHeader.setHeaderTitle(MESSAGES.headerAvailableInstructors());
		} else {
			iDomainHeader.setHeaderTitle(MESSAGES.headerAvailableAssignments());
		}
		iDomainHeader.showLoading();
		iDomainTable.setVisible(false);
	}
	
	protected void showDomain(List<SuggestionInfo> suggestions, int domainSize) {
		iDomainTable.setVisible(true);
		iDomainHeader.clearMessage();
		iDomainTable.clearTable(0);
		if (iRequest != null) {
			iDomainHeader.setHeaderTitle(MESSAGES.headerAvailableInstructors());
			List<UniTimeTableHeader> domainHeader = new ArrayList<UniTimeTableHeader>();
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colExternalId()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colNamePerson()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colAssignedLoad()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colAttributes()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colCoursePreferences()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colTimePreferences()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colDistributionPreferences()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colConflictingRequests()));
			domainHeader.add(new UniTimeTableHeader("&nbsp;"));
			domainHeader.add(new UniTimeTableHeader("&nbsp;"));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colObjectives()));
			iDomainTable.addRow(null, domainHeader);
			if (suggestions != null && iRequestDetails.getValue() != null) {
				int index = iRequestDetails.getValue();	
				for (SuggestionInfo suggestion: suggestions) {
					InstructorInfo instructor = null;
					List<AssignmentInfo> conflicts = new ArrayList<AssignmentInfo>();
					for (AssignmentInfo assignment: suggestion.getAssignments()) {
						if (assignment.getRequest().equals(iRequest) && assignment.getIndex() == index)
							instructor = assignment.getInstructor();
						if (assignment.getInstructor() == null)
							conflicts.add(assignment);
					}
					if (instructor == null) continue;
					List<Widget> line = new ArrayList<Widget>();
					Label extId = new Label(instructor.getExternalId());
					if (instructor.getTeachingPreference() != null && !"0".equals(instructor.getTeachingPreference())) {
						PreferenceInterface pref = iProperties.getPreference(instructor.getTeachingPreference());
						if (pref != null) {
							extId.setTitle(pref.getName() + " " + instructor.getExternalId());
							extId.getElement().getStyle().setColor(pref.getColor());
						}
					}
					line.add(extId);
					Label name = new Label(instructor.getInstructorName());
					if (instructor.getTeachingPreference() != null && !"0".equals(instructor.getTeachingPreference())) {
						PreferenceInterface pref = iProperties.getPreference(instructor.getTeachingPreference());
						if (pref != null) {
							name.setTitle(pref.getName() + " " + instructor.getInstructorName());
							name.getElement().getStyle().setColor(pref.getColor());
						}
					}
					line.add(name);
					line.add(new Label(sTeachingLoadFormat.format(instructor.getAssignedLoad()) + " / " + sTeachingLoadFormat.format(instructor.getMaxLoad())));
					P p = new P("attributes");
					for (AttributeInterface a: instructor.getAttributes()) {
						P i = new P("attribute");
						i.setText(a.getName());
						i.setTitle(a.getName() + " (" + a.getType().getLabel() + ")");
						p.add(i);
					}
					line.add(new AttributesCell(instructor.getAttributes()));
					line.add(new PreferenceCell(iProperties, instructor.getCoursePreferences()));
					line.add(new TimePreferenceCell(iProperties, instructor));
					line.add(new PreferenceCell(iProperties, instructor.getDistributionPreferences()));
					for (int i = 0; i < 3; i++)
						if (!conflicts.isEmpty()) {
							line.add(new RequestConflicts(conflicts, i));
						} else {
							line.add(new Label());
						}
					line.add(new ObjectivesCell(iProperties, suggestion.getValues()));
					iDomainTable.addRow(suggestion, line);
				}
				if (suggestions.size() < domainSize) {
					iDomainTable.addRow(null, new Note(11, MESSAGES.domainNinstructors(suggestions.size(), domainSize)));
				}
			}
		} else {
			iDomainHeader.setHeaderTitle(MESSAGES.headerAvailableAssignments());
			List<UniTimeTableHeader> domainHeader = new ArrayList<UniTimeTableHeader>();
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colCourse()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colSection()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colTime()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colDate()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colRoom()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colTeachingLoad()));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colConflictingRequests()));
			domainHeader.add(new UniTimeTableHeader("&nbsp;"));
			domainHeader.add(new UniTimeTableHeader("&nbsp;"));
			domainHeader.add(new UniTimeTableHeader(MESSAGES.colObjectives()));
			iDomainTable.addRow(null, domainHeader);
			if (suggestions != null) {
				for (SuggestionInfo suggestion: suggestions) {
					TeachingRequestInfo request = null;
					List<AssignmentInfo> conflicts = new ArrayList<AssignmentInfo>();
					for (AssignmentInfo assignment: suggestion.getAssignments()) {
						if (iInstructor.equals(assignment.getInstructor()))
							request = assignment.getRequest();
						if (assignment.getInstructor() == null)
							conflicts.add(assignment);
					}
					if (request == null) continue;
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
					for (int i = 0; i < 3; i++)
						if (!conflicts.isEmpty()) {
							line.add(new InstructorConflicts(conflicts, i));
						} else {
							line.add(new Label());
						}
					line.add(new ObjectivesCell(iProperties, suggestion.getValues()));
					iDomainTable.addRow(suggestion, line);
				}
				if (suggestions.size() < domainSize) {
					iDomainTable.addRow(null, new Note(10, MESSAGES.domainNassignments(suggestions.size(), domainSize)));
				}
			}
		}
		iForm.getRowFormatter().setVisible(iDomainRow, iDomainTable.getRowCount() > 1);
		iForm.getRowFormatter().setVisible(iDomainRow + 1, iDomainTable.getRowCount() > 1);
	}
	
	protected void populate(TeachingRequestInfo request, Integer index, InstructorInfo instructor) {
		if (request != null) {
			iRequest = request;
			iInstructor = null;
			iForm.setWidget(iDetailsRow, 0, iRequestDetails);
			List<String> sections = new ArrayList<String>();
			for (SectionInfo s: request.getSections()) {
				if (!s.isCommon())
					sections.add(s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId()));
			}
			setText(MESSAGES.dialogTeachingRequestDetail(request.getCourse().getCourseName(), ToolBox.toString(sections)));
			iRequestDetails.setRequest(request, index);
		} else {
			iRequest = null; iInstructor = instructor;
			iForm.setWidget(iDetailsRow, 0, iInstructorDetails);
			setText(MESSAGES.dialogDetailsOf(instructor.getInstructorName()));
			iInstructorDetails.setInstructor(instructor);
		}
		
		iAssignmentHeader.clearMessage();
		iForm.getRowFormatter().setVisible(iAssignmentRow, iAssignmentTable.getRowCount() > 1);
		iForm.getRowFormatter().setVisible(iAssignmentRow + 1, iAssignmentTable.getRowCount() > 1);
		iForm.getRowFormatter().setVisible(iAssignmentScoreRow, iAssignmentTable.getRowCount() > 1 && !iAssignmentScore.getText().isEmpty() && sSuggestionScoreFormat.parse(iAssignmentScore.getText()) != 0.0);
		iForm.getRowFormatter().setVisible(iAssignmentObjectivesRow, iAssignmentTable.getRowCount() > 1 && iAssignmentObjectives.getValue() != null && !iAssignmentObjectives.getValue().isEmpty());
		if (iIgnoreConflicts != null)
			iForm.getRowFormatter().setVisible(iIgnoreConflictsRow, iAssignmentTable.getRowCount() > 1);

		hideDomain();
		
		iSuggestionsRequest = null;
		hideDomain();
		hideSuggestions();
		
		if (iRequest != null && iRequestDetails.getValue() != null)
			computeSuggestions(null);
		else if (iInstructor != null)
			computeSuggestions(null);
	}
	
	public class RequestConflicts extends P {
		public RequestConflicts(List<AssignmentInfo> conflicts, int column) {
			super("conflicts");
			for (AssignmentInfo conflict: conflicts) {
				int idx = 0;
				for (SectionInfo section: conflict.getRequest().getSections()) {
					P conf = new P("conflict");
					switch (column) {
					case 0:
						if (idx == 0) conf.setText(conflict.getRequest().getCourse().getCourseName());
						else conf.setHTML("<br>");
						break;
					case 1:
						conf.setText(section.getSectionType() + (section.getExternalId() == null ? "" : " " + section.getExternalId()));
						break;
					case 2:
						conf.setHTML(section.getTime() == null ? SECTMSG.arrangeHours() : section.getTime());
						break;
					case 3:
						conf.setHTML(section.getDate() == null ? SECTMSG.noDate() : section.getDate());
						break;
					case 4:
						conf.setHTML(section.getRoom() == null ? SECTMSG.noRoom() : section.getRoom());
						break;
					case 5:
						if (idx == 0 && conflict.hasConflicts()) conf.setText(conflict.getConflicts(", "));
						else conf.setHTML("<br>");
						break;
					}
					if (section.isCommon()) conf.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
					idx ++;
					add(conf);
				}
			}
		}
	}
	
	public class InstructorConflicts extends P {
		public InstructorConflicts(List<AssignmentInfo> conflicts, int column) {
			super("conflicts");
			for (AssignmentInfo conflict: conflicts) {
				P conf = new P("conflict");
				int idx = 0;
				for (Iterator<SectionInfo> i = conflict.getRequest().getSections().iterator(); i.hasNext(); ) {
					SectionInfo section = i.next();
					P sct = new P("section");
					switch (column) {
					case 0:
						if (idx == 0) sct.setText(conflict.getRequest().getCourse().getCourseName());
						break;
					case 1:
						sct.setText(section.getSectionType() + (section.getExternalId() == null ? "" : " " + section.getExternalId()) + (i.hasNext() ? "," : ""));
						break;
					case 2:
						if (idx == 0) {
							InstructorInfo instructor = conflict.getRequest().getInstructor(conflict.getIndex());
							if (instructor != null) sct.setText(instructor.getInstructorName());
						}
						break;
					}
					if (section.isCommon()) sct.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
					idx ++;
					conf.add(sct);
				}
				add(conf);
			}
		}
	}
	
	public class Note extends P implements UniTimeTable.HasColSpan {
		int iColSpan;
		
		Note(int colspan, String message) {
			super("note");
			iColSpan = colspan;
			setText(message);
		}

		@Override
		public int getColSpan() {
			return iColSpan;
		}
	}
	
	protected void setupDetails() {
		iRequestDetails = new TeachingRequestDetails(iProperties);
		iDetailsRow = iForm.addRow(iRequestDetails);
		iInstructorDetails = new InstructorDetails(iProperties);
		iRequestDetails.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				if (event.getValue() != null) {
					if (iSuggestionsRequest != null && iRequest.getRequestId().equals(iSuggestionsRequest.getSelectedRequestId()) && event.getValue() == iSuggestionsRequest.getSelectedIndex()) {
						InstructorInfo instructor = iRequest.getInstructor(event.getValue());
						if (instructor != null) {
							RPC.execute(new TeachingAssignmentsDetailRequest(instructor.getInstructorId()), new AsyncCallback<InstructorInfo>() {
								@Override
								public void onFailure(Throwable caught) {
									computeSuggestions(iAssignmentRow);
								}
								@Override
								public void onSuccess(InstructorInfo result) {
									populate(null, null, result);
								}
							});
							return;
						}
					}
					computeSuggestions(iAssignmentRow);
				}
			}
		});
		iInstructorDetails.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				if (event.getValue() != null && event.getValue() < iInstructor.getAssignedRequests().size()) {
					TeachingRequestInfo req = iInstructor.getAssignedRequests().get(event.getValue());
					if (iSuggestionsRequest != null && iInstructor.getInstructorId().equals(iSuggestionsRequest.getSelectedInstructorId()) && req.getRequestId().equals(iSuggestionsRequest.getSelectedRequestId())) {
						RPC.execute(new TeachingRequestDetailRequest(req.getRequestId()), new AsyncCallback<TeachingRequestInfo>() {
							@Override
							public void onFailure(Throwable caught) {
								computeSuggestions(iAssignmentRow);
							}
							@Override
							public void onSuccess(TeachingRequestInfo result) {
								InstructorInfo instructor = result.getInstructor(iInstructor.getInstructorId());
								populate(result, instructor == null ? null : new Integer(instructor.getAssignmentIndex()), null);
							}
						});
						return;
					}
					computeSuggestions(iAssignmentRow);
				}
			}
		});
	}
	
	protected void setupSelectedAssignments() {
		iAssignmentHeader = new UniTimeHeaderPanel(MESSAGES.headerSelectedAssignment());
		iAssignmentRow = iForm.addHeaderRow(iAssignmentHeader);
		iAssignmentTable = new UniTimeTable<AssignmentInfo>();
		iAssignmentTable.addStyleName("selected");
		List<UniTimeTableHeader> asgHeader = new ArrayList<UniTimeTableHeader>();
		asgHeader.add(new UniTimeTableHeader("&nbsp;"));
		asgHeader.add(new UniTimeTableHeader(MESSAGES.colCourse()));
		asgHeader.add(new UniTimeTableHeader(MESSAGES.colSection()));
		asgHeader.add(new UniTimeTableHeader(MESSAGES.colExternalId(), 3));
		asgHeader.add(new UniTimeTableHeader(MESSAGES.colNamePerson(), 3));
		asgHeader.add(new UniTimeTableHeader(MESSAGES.colAttributes()));
		asgHeader.add(new UniTimeTableHeader(MESSAGES.colCoursePreferences()));
		asgHeader.add(new UniTimeTableHeader(MESSAGES.colTimePreferences()));
		asgHeader.add(new UniTimeTableHeader(MESSAGES.colDistributionPreferences()));
		asgHeader.add(new UniTimeTableHeader(MESSAGES.colObjectives()));
		iAssignmentTable.addRow(null, asgHeader);
		iForm.addRow(iAssignmentTable);
		iAssignmentHeader.addButton("assign", MESSAGES.buttonAssign(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!iProperties.isHasSolver() && !Window.confirm(MESSAGES.confirmInstructorAssignmentChangesNoSolver())) return;
				iAssignmentHeader.showLoading();
				iAssignmentHeader.setEnabled("assign", false);
				final InstructorAssignmentRequest request = new InstructorAssignmentRequest();
				for (AssignmentInfo assignment: iAssignmentTable.getData())
					request.addAssignment(assignment);
				if (iIgnoreConflicts != null) request.setIgnoreConflicts(iIgnoreConflicts.getValue());
				RPC.execute(request, new AsyncCallback<GwtRpcResponseNull>() {
					@Override
					public void onFailure(Throwable caught) {
						iAssignmentHeader.setErrorMessage(caught.getMessage());
					}

					@Override
					public void onSuccess(GwtRpcResponseNull result) {
						iAssignmentHeader.clearMessage();
						hide();
						onAssignmentChanged(request.getAssignments());
					}
				});
			}
		});
		iAssignmentHeader.setEnabled("assign", false);
		iAssignmentScore = new Label();
		iAssignmentScoreRow = iForm.addRow(MESSAGES.propSuggestionScore(), iAssignmentScore);
		iAssignmentObjectives = new ObjectivesCell(iProperties);
		iAssignmentObjectivesRow = iForm.addRow(MESSAGES.propSuggestionObjectives(), iAssignmentObjectives);
		iAssignmentTable.addMouseClickListener(new UniTimeTable.MouseClickListener<AssignmentInfo>() {
			@Override
			public void onMouseClick(final UniTimeTable.TableEvent<AssignmentInfo> event) {
				if (event.getData() != null) {
					TeachingRequestInfo request = event.getData().getRequest();
					Integer index = event.getData().getIndex();
					InstructorInfo instructor = event.getData().getInstructor();
					if (instructor == null) instructor = event.getData().getRequest().getInstructor(event.getData().getIndex());
					if (iRequest != null) {
						if (request.equals(iRequest) && index.equals(iRequestDetails.getValue()) && instructor != null) {
							RPC.execute(new TeachingAssignmentsDetailRequest(instructor.getInstructorId()), new AsyncCallback<InstructorInfo>() {
								@Override
								public void onFailure(Throwable caught) {
									populate(event.getData().getRequest(), event.getData().getIndex(), null);
								}
								@Override
								public void onSuccess(InstructorInfo result) {
									populate(null, null, result);
								}
							});
						} else {
							populate(event.getData().getRequest(), event.getData().getIndex(), null);
						}
					} else if (instructor != null) {
						if (instructor.equals(iInstructor)) {
							populate(event.getData().getRequest(), event.getData().getIndex(), null);
						} else {
							RPC.execute(new TeachingAssignmentsDetailRequest(instructor.getInstructorId()), new AsyncCallback<InstructorInfo>() {
								@Override
								public void onFailure(Throwable caught) {
									populate(event.getData().getRequest(), event.getData().getIndex(), null);
								}
								@Override
								public void onSuccess(InstructorInfo result) {
									populate(null, null, result);
								}
							});
						}
						// populate(null, null, event.getData().getInstructor());
					} else {
						populate(event.getData().getRequest(), event.getData().getIndex(), null);
						//populate(null, null, event.getData().getRequest().getInstructor(event.getData().getIndex()));
					}
				}
			}
		});
		if (!iProperties.isHasSolver()) {
			iIgnoreConflicts = new CheckBox(MESSAGES.checkIgnoreInstructorAssignmentConflicts());
			iIgnoreConflicts.addStyleName("ignore-conflicts");
			iIgnoreConflictsRow = iForm.addRow(iIgnoreConflicts);
			iIgnoreConflicts.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					for (int i = 1; i < iAssignmentTable.getRowCount(); i++) {
						AssignmentInfo ai = iAssignmentTable.getData(i);
						if (ai != null && ai.getInstructor() == null) iAssignmentTable.getRowFormatter().setVisible(i, !event.getValue());
					}
				}
			});
		}
	}
	
	protected void setupDomain() {
		iDomainHeader = new UniTimeHeaderPanel(MESSAGES.headerAvailableInstructors());
		iDomainRow = iForm.addHeaderRow(iDomainHeader);
		iDomainTable = new UniTimeTable<SuggestionInfo>();
		iDomainTable.addStyleName("domain");
		iForm.addRow(iDomainTable);
		iDomainTable.addMouseClickListener(new UniTimeTable.MouseClickListener<SuggestionInfo>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<SuggestionInfo> event) {
				if (event.getData() != null) {
					showAssignment(event.getData());
					computeSuggestions(iAssignmentRow);
					iScroll.scrollToTop();
				}
			}
		});
		iDomainHeader.addButton("more", MESSAGES.buttonMoreAssignments(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iSuggestionsRequest != null) {
					iSuggestionsRequest.setMaxDomain(2 * iSuggestionsRequest.getMaxDomain());
					iSuggestionsRequest.setComputeDomain();
					computeSuggestions(iSuggestionsRequest, iDomainRow);
				}
			}
		});
		iDomainHeader.setEnabled("more", false);
	}
	
	protected void setupSuggestions() {
		iSuggestionsHeader = new UniTimeHeaderPanel(MESSAGES.headerSuggestions());
		iSuggestionsRow = iForm.addHeaderRow(iSuggestionsHeader);
		iSuggestionsTable = new UniTimeTable<SuggestionInfo>();
		iSuggestionsTable.setVisible(false);
		iSuggestionsTable.addStyleName("suggestions");
		List<UniTimeTableHeader> sgHeader = new ArrayList<UniTimeTableHeader>();
		sgHeader.add(new UniTimeTableHeader(MESSAGES.colScore()));
		sgHeader.add(new UniTimeTableHeader(MESSAGES.colCourse()));
		sgHeader.add(new UniTimeTableHeader(MESSAGES.colSection()));
		sgHeader.add(new UniTimeTableHeader(MESSAGES.colExternalId(), 3));
		sgHeader.add(new UniTimeTableHeader(MESSAGES.colNamePerson(), 3));
		sgHeader.add(new UniTimeTableHeader(MESSAGES.colObjectives()));
		iSuggestionsTable.addRow(null, sgHeader);
		iForm.addRow(iSuggestionsTable);
		iSuggestionsHeader.addButton("longer", MESSAGES.buttonSearchLonger(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iSuggestionsRequest != null) {
					iSuggestionsRequest.setTimeout(2 * iSuggestionsRequest.getTimeout());
					iSuggestionsRequest.setComputeSuggestions();
					computeSuggestions(iSuggestionsRequest, iSuggestionsRow);
				}
			}
		});
		iSuggestionsHeader.addButton("deeper", MESSAGES.buttonSearchDeeper(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iSuggestionsRequest != null) {
					iSuggestionsRequest.setMaxDept(1 + iSuggestionsRequest.getMaxDept());
					iSuggestionsRequest.setComputeSuggestions();
					computeSuggestions(iSuggestionsRequest, iSuggestionsRow);
				}
			}
		});
		iSuggestionsHeader.setEnabled("longer", false);
		iSuggestionsHeader.setEnabled("deeper", false);
		iSuggestionsTable.addMouseClickListener(new UniTimeTable.MouseClickListener<SuggestionInfo>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<SuggestionInfo> event) {
				if (event.getData() != null) {
					showAssignment(event.getData());
					iScroll.scrollToTop();
				}
			}
		});
	}
}
