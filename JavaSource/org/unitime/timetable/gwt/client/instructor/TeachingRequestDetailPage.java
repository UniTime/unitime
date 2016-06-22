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
import java.util.Map;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.instructor.TeachingRequestsPage.HasRefresh;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.rooms.RoomCookie;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasColSpan;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.ComputeSuggestionsRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SuggestionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SuggestionsResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestDetailRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
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
	
	private Label iCourseLabel, iRequestLoad;
	private Pref iAttributePrefs, iInstructorPrefs;
	private Objectives iObjectives;
	private int iAttributePrefsRow, iInstructorPrefsRow, iObjectivesRow, iAvailableInstructorsRow, iSuggestionsRow, iAssignmentRow;
	private UniTimeTable<SectionInfo> iSectionsTable;
	private UniTimeTable<InstructorInfo> iInstructorsTable;
	private UniTimeTable<InstructorInfo> iAvailableInstructorsTable;
	private UniTimeHeaderPanel iSuggestionsHeader;
	
	private TeachingRequestInfo iRequest;
	private SuggestionInfo iSuggestion;
	private ComputeSuggestionsRequest iSuggestionsRequest;
	private AsyncCallback<SuggestionsResponse> iSuggestionsCallback;
	private UniTimeTable<SuggestionInfo> iSuggestionsTable;
	private UniTimeTable<AssignmentInfo> iAssignmentTable;
	
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
		
		iForm.addHeaderRow(MESSAGES.headerTeachingRequest());
		iCourseLabel = new Label();
		iForm.addRow(MESSAGES.propCourse(), iCourseLabel);
		iSectionsTable = new UniTimeTable<SectionInfo>();
		iSectionsTable.addStyleName("sections");
		List<UniTimeTableHeader> sectionHeader = new ArrayList<UniTimeTableHeader>();
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colSection()));
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colTime()));
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colDate()));
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colRoom()));
		iSectionsTable.addRow(null, sectionHeader);		
		iForm.addRow(MESSAGES.propSections(), iSectionsTable);
		iRequestLoad = new Label();
		iForm.addRow(MESSAGES.propRequestLoad(), iRequestLoad);
		iAttributePrefs = new Pref();
		iAttributePrefsRow = iForm.addRow(MESSAGES.propAttributePrefs(), iAttributePrefs);
		iInstructorPrefs = new Pref();
		iInstructorPrefsRow = iForm.addRow(MESSAGES.propInstructorPrefs(), iInstructorPrefs);
		iObjectives = new Objectives();
		iObjectivesRow = iForm.addRow(MESSAGES.propObjectives(), iObjectives);
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
		iForm.addRow(MESSAGES.propAssignedInstructors(), iInstructorsTable);
		iInstructorsTable.setAllowSelection(true);
		iInstructorsTable.setAllowMultiSelect(false);
		
		iAvailableInstructorsRow = iForm.addHeaderRow(MESSAGES.headerAvailableInstructors());
		iAvailableInstructorsTable = new UniTimeTable<InstructorInfo>();
		iAvailableInstructorsTable.addStyleName("instructors");
		List<UniTimeTableHeader> avInstrHeader = new ArrayList<UniTimeTableHeader>();
		avInstrHeader.add(new UniTimeTableHeader(MESSAGES.colExternalId()));
		avInstrHeader.add(new UniTimeTableHeader(MESSAGES.colNamePerson()));
		avInstrHeader.add(new UniTimeTableHeader(MESSAGES.colAssignedLoad()));
		avInstrHeader.add(new UniTimeTableHeader(MESSAGES.colAttributes()));
		avInstrHeader.add(new UniTimeTableHeader(MESSAGES.colCoursePreferences()));
		avInstrHeader.add(new UniTimeTableHeader(MESSAGES.colTimePreferences()));
		avInstrHeader.add(new UniTimeTableHeader(MESSAGES.colDistributionPreferences()));
		avInstrHeader.add(new UniTimeTableHeader(MESSAGES.colConflictingRequests()));
		avInstrHeader.add(new UniTimeTableHeader("&nbsp;"));
		avInstrHeader.add(new UniTimeTableHeader("&nbsp;"));
		avInstrHeader.add(new UniTimeTableHeader(MESSAGES.colObjectives()));
		iAvailableInstructorsTable.addRow(null, avInstrHeader);
		iForm.addRow(iAvailableInstructorsTable);
		
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
				if (iSuggestionsRequest != null)
					iSuggestionsRequest.setTimeout(2 * iSuggestionsRequest.getTimeout());
				computeSuggestions(true);
			}
		});
		iSuggestionsHeader.addButton("deeper", MESSAGES.buttonSearchDeeper(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iSuggestionsRequest != null)
					iSuggestionsRequest.setMaxDept(1 + iSuggestionsRequest.getMaxDept());
				computeSuggestions(true);
			}
		});
		iSuggestionsHeader.setEnabled("longer", false);
		iSuggestionsHeader.setEnabled("deeper", false);
		
		UniTimeHeaderPanel footer = new UniTimeHeaderPanel();
		footer.addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		iForm.addBottomRow(footer);
		
		iInstructorsTable.addMouseClickListener(new UniTimeTable.MouseClickListener<InstructorInterface.InstructorInfo>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<InstructorInterface.InstructorInfo> event) {
				if (event.getRow() > 1) {
					iInstructorsTable.setSelected(event.getRow(), true);
					if (iAvailableInstructorsTable.getRowCount() > 1) {
						iForm.getRowFormatter().setVisible(iAvailableInstructorsRow, true);
						iForm.getRowFormatter().setVisible(iAvailableInstructorsRow + 1, true);
						
					}
				}
			}
		});
		
		iSuggestionsCallback = new AsyncCallback<SuggestionsResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iSuggestionsHeader.setErrorMessage(MESSAGES.failedToComputeSuggestions(caught.getMessage()));
				iSuggestionsTable.setVisible(false);
			}

			@Override
			public void onSuccess(SuggestionsResponse result) {
				iSuggestionsHeader.clearMessage();
				if (result.hasSuggestions()) {
					iSuggestionsTable.clearTable(1);
					for (SuggestionInfo suggestion: result.getSuggestions()) {
						boolean first = true;
						for (AssignmentInfo assignment: suggestion.getAssignments()) {
							List<Widget> line = new ArrayList<Widget>();
							if (first) {
								Label score = new Label(sSuggestionScoreFormat.format(suggestion.getValue()));
								if (suggestion.getValue() > 0) score.getElement().getStyle().setColor("red");
								if (suggestion.getValue() < 0) score.getElement().getStyle().setColor("green");
								line.add(score);
							} else {
								line.add(new Label());
							}
							line.add(new Label(assignment.getRequest().getCourse().getCourseName()));
							P p = new P("sections");
							for (SectionInfo s: assignment.getRequest().getSections()) {
								P i = new P("section");
								i.setText(s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId()));
								if (s.isCommon()) i.addStyleName("common");
								p.add(i);
							}
							line.add(p);
							InstructorInfo initial = (assignment.getRequest().hasInstructors() ? assignment.getRequest().getInstructors().get(0) : null);
							InstructorInfo current = assignment.getInstructor();
							if (initial == null) {
								Label na = new Label(MESSAGES.notAssigned()); na.addStyleName("not-assigned");
								na.addStyleName("initial");
								line.add(na);
							} else {
								Label extId = new Label(initial.hasExternalId() ? initial.getExternalId() : MESSAGES.noExternalId());
								if (!initial.hasExternalId()) extId.addStyleName("no-extid");
								extId.addStyleName("initial");
								line.add(extId);
							}
							line.add(new HTML(MESSAGES.assignmentArrow()));
							if (current == null) {
								Label na = new Label(MESSAGES.notAssigned()); na.addStyleName("not-assigned");
								na.addStyleName("current");
								line.add(na);
							} else {
								Label extId = new Label(current.hasExternalId() ? current.getExternalId() : MESSAGES.noExternalId());
								if (!current.hasExternalId()) extId.addStyleName("no-extid");
								extId.addStyleName("current");
								line.add(extId);
							}
							if (initial == null) {
								Label na = new Label(MESSAGES.notAssigned()); na.addStyleName("not-assigned");
								na.addStyleName("initial");
								line.add(na);
							} else {
								Label name = new Label(initial.getInstructorName());
								name.addStyleName("initial");
								line.add(name);
							}
							line.add(new HTML(MESSAGES.assignmentArrow()));
							if (current == null) {
								Label na = new Label(MESSAGES.notAssigned()); na.addStyleName("not-assigned");
								na.addStyleName("current");
								line.add(na);
							} else {
								Label name = new Label(current.getInstructorName());
								name.addStyleName("current");
								line.add(name);
							}
							if (first) {
								line.add(new Objectives(suggestion.getValues()));
							}
							int row = iSuggestionsTable.addRow(suggestion, line);
							if (first) {
								for (int i = 0; i < iSuggestionsTable.getCellCount(row); i++)
									iSuggestionsTable.getCellFormatter().addStyleName(row, i, "first-line");
								iSuggestionsTable.getFlexCellFormatter().setRowSpan(row, line.size() - 1, suggestion.getAssignments().size());
								iSuggestionsTable.getFlexCellFormatter().getElement(row, line.size() - 1).getStyle().setBackgroundColor("white");
							}
							first = false;
						}
					}
					if (result.getSuggestions().size() < result.getNrSolutions()) {
						if (result.isTimeoutReached())
							iSuggestionsTable.addRow(null, new Note(9, MESSAGES.suggestionsNoteTimeoutNResults(iSuggestionsRequest.getTimeout() / 1000, result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept(), result.getSuggestions().size(), result.getNrSolutions())));
						else
							iSuggestionsTable.addRow(null, new Note(9, MESSAGES.suggestionsNoteNoTimeoutNResults(result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept(), result.getSuggestions().size(), result.getNrSolutions())));
					} else {
						if (result.isTimeoutReached())
							iSuggestionsTable.addRow(null, new Note(9, MESSAGES.suggestionsNoteTimeoutAllResults(iSuggestionsRequest.getTimeout() / 1000, result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept(), result.getSuggestions().size())));
						else
							iSuggestionsTable.addRow(null, new Note(9, MESSAGES.suggestionsNoteNoTimeoutAllResults(result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept(), result.getSuggestions().size())));						
					}
				} else {
					if (result.isTimeoutReached())
						iSuggestionsTable.addRow(null, new Note(9, MESSAGES.suggestionsNoteTimeoutNoResults(iSuggestionsRequest.getTimeout() / 1000, result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept())));
					else
						iSuggestionsTable.addRow(null, new Note(9, MESSAGES.suggestionsNoteNoTimeoutNoResults(result.getNrCombinationsConsidered(), iSuggestionsRequest.getMaxDept())));
				}
				iSuggestionsTable.setVisible(true);
				iSuggestionsHeader.setEnabled("longer", result.isTimeoutReached());
				iSuggestionsHeader.setEnabled("deeper", true);
			}
		};
	}
	
	public void showDetail(Long id) {
		LoadingWidget.getInstance().show(MESSAGES.waitLoadTeachingRequestDetail());
		ToolBox.setMaxHeight(iScroll.getElement().getStyle(), Math.round(0.9 * Window.getClientHeight()) + "px");
		RPC.execute(new TeachingRequestDetailRequest(id), new AsyncCallback<TeachingRequestInfo>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				UniTimeNotifications.error(MESSAGES.failedToLoadTeachingRequestDetaul(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(TeachingRequestInfo result) {
				LoadingWidget.getInstance().hide();
				populate(result);
				center();
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
			}
		});
	}
	
	protected void computeSuggestions(boolean recompute) {
		if (!recompute || iSuggestionsRequest == null) {
			AssignmentInfo assignment = new AssignmentInfo();
			int row = iInstructorsTable.getSelectedRow();
			assignment.setRequest(iRequest); assignment.setIndex(row <= 0 ? 0 : row - 1); assignment.setInstructor(null);
			SuggestionInfo suggestion = new SuggestionInfo();
			suggestion.addAssignment(assignment);
			iSuggestionsRequest = new ComputeSuggestionsRequest();
			iSuggestionsRequest.setSuggestion(suggestion);
		}
		iForm.getRowFormatter().setVisible(iSuggestionsRow, true);
		iForm.getRowFormatter().setVisible(iSuggestionsRow + 1, true);
		iSuggestionsHeader.showLoading();
		iSuggestionsHeader.setEnabled("longer", false);
		iSuggestionsHeader.setEnabled("deeper", false);
		iSuggestionsTable.setVisible(false);
		RPC.execute(iSuggestionsRequest, iSuggestionsCallback);
	}
	
	protected void populate(TeachingRequestInfo request) {
		iRequest = request;
		setText(MESSAGES.dialogTeachingRequestDetail(request.getCourse().getCourseName(), request.getSections().get(0).getSectionType() + (request.getSections().get(0).getExternalId() == null ? "" : " " + request.getSections().get(0).getExternalId())));
		
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
		iForm.getRowFormatter().setVisible(iAttributePrefsRow, !request.getAttributePreferences().isEmpty());
		iInstructorPrefs.setValue(request.getInstructorPreferences());
		iForm.getRowFormatter().setVisible(iInstructorPrefsRow, !request.getInstructorPreferences().isEmpty());
		iObjectives.setValue(request.getValues());
		iForm.getRowFormatter().setVisible(iObjectivesRow, !request.getValues().isEmpty());
		
		iInstructorsTable.clearTable(1);
		int instrIndex = 1;
		if (request.hasInstructors()) {
			for (InstructorInfo instructor: request.getInstructors()) {
				List<Widget> instructorLine = new ArrayList<Widget>();
				instructorLine.add(new Label((instrIndex++) + "."));
				Label extId = new Label(instructor.getExternalId());
				if (instructor.getTeachingPreference() != null && !"0".equals(instructor.getTeachingPreference())) {
					PreferenceInterface pref = iProperties.getPreference(instructor.getTeachingPreference());
					if (pref != null) {
						extId.setTitle(pref.getName() + " " + instructor.getExternalId());
						extId.getElement().getStyle().setColor(pref.getColor());
					}
				}
				instructorLine.add(extId);
				Label name = new Label(instructor.getInstructorName());
				if (instructor.getTeachingPreference() != null && !"0".equals(instructor.getTeachingPreference())) {
					PreferenceInterface pref = iProperties.getPreference(instructor.getTeachingPreference());
					if (pref != null) {
						name.setTitle(pref.getName() + " " + instructor.getInstructorName());
						name.getElement().getStyle().setColor(pref.getColor());
					}
				}
				instructorLine.add(name);
				instructorLine.add(new Label(sTeachingLoadFormat.format(instructor.getAssignedLoad()) + " / " + sTeachingLoadFormat.format(instructor.getMaxLoad())));
				P p = new P("attributes");
				for (AttributeInterface a: instructor.getAttributes()) {
					P i = new P("attribute");
					i.setText(a.getName());
					i.setTitle(a.getName() + " (" + a.getType().getLabel() + ")");
					p.add(i);
				}
				instructorLine.add(p);
				instructorLine.add(new Pref(instructor.getCoursePreferences()));
				instructorLine.add(new TimePreferences(instructor));
				instructorLine.add(new Pref(instructor.getDistributionPreferences()));
				instructorLine.add(new Objectives(instructor.getValues()));
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
		}

		iAvailableInstructorsTable.clearTable(1);
		if (request.hasDomainValues()) {
			for (InstructorInfo instructor: request.getDomainValues()) {
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
				line.add(p);
				line.add(new Pref(instructor.getCoursePreferences()));
				line.add(new TimePreferences(instructor));
				line.add(new Pref(instructor.getDistributionPreferences()));
				for (int i = 0; i < 3; i++)
					if (instructor.hasConflicts()) {
						line.add(new Conflicts(instructor.getConflicts(), i));
					} else {
						line.add(new Label());
					}
				line.add(new Objectives(instructor.getValues()));
				iAvailableInstructorsTable.addRow(instructor, line);
			}
			iForm.getRowFormatter().setVisible(iAvailableInstructorsRow, iInstructorsTable.getSelectedRow() > 0);
			iForm.getRowFormatter().setVisible(iAvailableInstructorsRow + 1, iInstructorsTable.getSelectedRow() > 0);
		} else {
			iForm.getRowFormatter().setVisible(iAvailableInstructorsRow, false);
			iForm.getRowFormatter().setVisible(iAvailableInstructorsRow + 1, false);
		}
		
		iSuggestionsRequest = null;
		if (request.getNrInstructors() == 1) {
			computeSuggestions(false);
		}
	}
	
	public class Pref extends P {
		public Pref() {
			super("preferences");
		}
		
		public Pref(List<PreferenceInfo> prefs) {
			this();
			setValue(prefs);
		}
		
		public void setValue(List<PreferenceInfo> prefs) {
			clear();
			for (PreferenceInfo p: prefs) {
				P prf = new P("prf");
				prf.setText(p.getOwnerName());
				PreferenceInterface preference = iProperties.getPreference(p.getPreference());
				if (preference != null) {
					prf.setTitle(preference.getName() + " " + p.getOwnerName());
					prf.getElement().getStyle().setColor(preference.getColor());
				}
				add(prf);
			}
		}
	}
	
	public class TimePreferences extends P implements HasRefresh {
		private String iInstructorId = null;
		private String iPattern = null;
		private List<PreferenceInfo> iPreferences = null;
		
		public TimePreferences(InstructorInfo instructor) {
			super("preferences");
			iInstructorId = String.valueOf(instructor.getInstructorId());
			iPattern = instructor.getAvailability();
			iPreferences = instructor.getTimePreferences();
			addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					InstructorAvailabilityHint.showHint(getElement(), iInstructorId, true, iPattern);
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					InstructorAvailabilityHint.hideHint();
				}
			});
			refresh();
		}
		
		@Override
		public void refresh() {
			clear();
			RoomCookie cookie = RoomCookie.getInstance();
			if (iPattern != null && !iPattern.isEmpty() && !cookie.isGridAsText()) {
				final Image availability = new Image(GWT.getHostPageBaseURL() + "pattern?pref=" + iPattern + "&v=" + (cookie.areRoomsHorizontal() ? "0" : "1") + (cookie.hasMode() ? "&s=" + cookie.getMode() : ""));
				availability.setStyleName("grid");
				add(availability);
			} else {
				for (PreferenceInfo p: iPreferences) {
					P prf = new P("prf");
					prf.setText(p.getOwnerName());
					PreferenceInterface preference = iProperties.getPreference(p.getPreference());
					if (preference != null) {
						prf.getElement().getStyle().setColor(preference.getColor());
						prf.setTitle(preference.getName() + " " + p.getOwnerName());
					}
					add(prf);
				}
			}
		}
	}
	
	public class Objectives extends P {
		public Objectives() {
			super("objectives");
		}
			
		public Objectives(Map<String, Double> values) {
			this();
			setValue(values);
		}
		
		public void setValue(Map<String, Double> values) {
			clear();
			for (String key: new TreeSet<String>(values.keySet())) {
				Double value = values.get(key);
				if (value == null || Math.abs(value) < 0.001) continue;
				P obj = new P("objective");
				obj.setText(key + ": " + (value > 0.0 ? "+": "") + sTeachingLoadFormat.format(value));
				if (key.endsWith(" Preferences")) {
					if (value <= -50.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("R").getColor());
					} else if (value <= -2.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("-2").getColor());
					} else if (value < 0.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("-1").getColor());
					} else if (value >= 50.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("P").getColor());
					} else if (value >= 2.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("2").getColor());
					} else if (value > 0.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("1").getColor());
					}
				} else if (value < 0.0) {
					obj.getElement().getStyle().setColor("green");
				} else if (value > 0.0) {
					obj.getElement().getStyle().setColor("red");
				}
				add(obj);
			}
		}
	}
	
	public class Conflicts extends P {
		public Conflicts(List<TeachingRequestInfo> conflicts, int column) {
			super("conflicts");
			for (TeachingRequestInfo conflict: conflicts) {
				int idx = 0;
				for (SectionInfo section: conflict.getSections()) {
					P conf = new P("conflict");
					switch (column) {
					case 0:
						if (idx == 0) conf.setText(conflict.getCourse().getCourseName());
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
						if (idx == 0 && conflict.hasConflict()) conf.setText(conflict.getConflict());
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
	
	public class NotAssignedInstructor extends P implements HasColSpan {
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
	
	public class Note extends P implements HasColSpan {
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
}
