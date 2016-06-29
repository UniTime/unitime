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
import java.util.Map;
import java.util.Set;
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
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
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
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorAssignmentRequest;
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
import com.google.gwt.dom.client.Element;
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
	private UniTimeTable<SuggestionInfo> iAvailableInstructorsTable;
	private UniTimeHeaderPanel iSuggestionsHeader;
	private UniTimeHeaderPanel iAssignmentHeader;
	
	private TeachingRequestInfo iRequest;
	private ComputeSuggestionsRequest iSuggestionsRequest;
	private UniTimeTable<SuggestionInfo> iSuggestionsTable;
	private UniTimeTable<AssignmentInfo> iAssignmentTable;
	private Objectives iAssignmentObjectives;
	private Label iAssignmentScore;
	
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
		
		iAssignmentHeader = new UniTimeHeaderPanel(MESSAGES.headerSelectedAssignment());
		iAssignmentRow = iForm.addHeaderRow(iAssignmentHeader);
		iAssignmentTable = new UniTimeTable<AssignmentInfo>();
		iAssignmentTable.addStyleName("assignments");
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
				iAssignmentHeader.showLoading();
				iAssignmentHeader.setEnabled("assign", false);
				final InstructorAssignmentRequest request = new InstructorAssignmentRequest();
				for (AssignmentInfo assignment: iAssignmentTable.getData())
					request.addAssignment(assignment);
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
		iForm.addRow(MESSAGES.propSuggestionScore(), iAssignmentScore);
		iAssignmentObjectives = new Objectives();
		iForm.addRow(MESSAGES.propSuggestionObjectives(), iAssignmentObjectives);
		
		iAvailableInstructorsRow = iForm.addHeaderRow(MESSAGES.headerAvailableInstructors());
		iAvailableInstructorsTable = new UniTimeTable<SuggestionInfo>();
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
				if (iSuggestionsRequest != null) {
					iSuggestionsRequest.setTimeout(2 * iSuggestionsRequest.getTimeout());
					computeSuggestions(iSuggestionsRequest, true);
				}
			}
		});
		iSuggestionsHeader.addButton("deeper", MESSAGES.buttonSearchDeeper(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iSuggestionsRequest != null) {
					iSuggestionsRequest.setMaxDept(1 + iSuggestionsRequest.getMaxDept());
					computeSuggestions(iSuggestionsRequest, true);
				}
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
				if (event.getRow() > 0) {
					iInstructorsTable.setSelected(event.getRow(), true);
					if (iAssignmentTable.getRowCount() <= 1) {
						SuggestionInfo suggestion = new SuggestionInfo();
						AssignmentInfo assignment = new AssignmentInfo();
						int row = iInstructorsTable.getSelectedRow();
						assignment.setRequest(iRequest); assignment.setIndex(row <= 0 ? 0 : row - 1);
						assignment.setInstructor(row <= 0 ? null : iInstructorsTable.getData(row));
						suggestion.addAssignment(assignment);
						showAssignment(suggestion);
						iForm.getRowFormatter().setVisible(iAvailableInstructorsRow, true);
						iForm.getRowFormatter().setVisible(iAvailableInstructorsRow + 1, true);
						center();
					}
					computeSuggestions();
				}
			}
		});
		
		iAssignmentTable.addMouseClickListener(new UniTimeTable.MouseClickListener<AssignmentInfo>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<AssignmentInfo> event) {
				if (event.getData() != null)
					populate(event.getData().getRequest(), event.getData().getIndex());
			}
		});
		
		iSuggestionsTable.addMouseClickListener(new UniTimeTable.MouseClickListener<SuggestionInfo>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<SuggestionInfo> event) {
				if (event.getData() != null) {
					showAssignment(event.getData());
					iScroll.scrollToTop();
				}
			}
		});
		
		iAvailableInstructorsTable.addMouseClickListener(new UniTimeTable.MouseClickListener<SuggestionInfo>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<SuggestionInfo> event) {
				if (event.getData() != null) {
					showAssignment(event.getData());
					computeSuggestions();
					iScroll.scrollToTop();
				}
			}
		});
	}
	
	protected void onAssignmentChanged(List<AssignmentInfo> assignments) {}
	
	public void showDetail(Long id) {
		iAssignmentTable.clearTable(1);
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
				populate(result, null);
				center();
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
			}
		});
	}
	
	protected void computeSuggestions() {
		ComputeSuggestionsRequest request = new ComputeSuggestionsRequest();
		for (AssignmentInfo assignment: iAssignmentTable.getData()) {
			if (assignment.getInstructor() != null) {
				request.addAssignment(assignment);
			}
		}
		computeSuggestions(request, false);
	}
	
	protected void computeSuggestions(ComputeSuggestionsRequest request, final boolean scrollToSuggestions) {
		iSuggestionsRequest = request;
		iSuggestionsRequest.setSelectedRequestId(iRequest.getRequestId());
		iSuggestionsRequest.setSelectedIndex(iInstructorsTable.getSelectedRow() - 1);
		iForm.getRowFormatter().setVisible(iSuggestionsRow, true);
		iForm.getRowFormatter().setVisible(iSuggestionsRow + 1, true);
		iSuggestionsHeader.showLoading();
		iSuggestionsHeader.setEnabled("longer", false);
		iSuggestionsHeader.setEnabled("deeper", false);
		iSuggestionsTable.setVisible(false);
		if (iAssignmentTable.getRowCount() > 1) {
			iAssignmentHeader.setEnabled("assign", false);
			iAssignmentHeader.showLoading();
		}
		iForm.getRowFormatter().setVisible(iAvailableInstructorsRow, false);
		iForm.getRowFormatter().setVisible(iAvailableInstructorsRow + 1, false);
		RPC.execute(iSuggestionsRequest, new AsyncCallback<SuggestionsResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iSuggestionsHeader.setErrorMessage(MESSAGES.failedToComputeSuggestions(caught.getMessage()));
				iSuggestionsTable.setVisible(false);
			}

			@Override
			public void onSuccess(SuggestionsResponse result) {
				iSuggestionsHeader.clearMessage();
				iAssignmentHeader.clearMessage();
				showAssignment(result.getCurrentAssignment());
				showSuggestions(result);
				showAvailableInstructors(result.getDomainValues());
				center();
				if (scrollToSuggestions) {
					Element scroll = iScroll.getElement();
					Element item = iForm.getRowFormatter().getElement(iSuggestionsRow);
					int realOffset = 0;
					while (item !=null && !item.equals(scroll)) {
						realOffset += item.getOffsetTop();
						item = item.getOffsetParent();
					}
					scroll.setScrollTop(realOffset - scroll.getOffsetHeight() / 2);
				}
			}
		});		
	}
	
	protected void showSuggestions(SuggestionsResponse result) {
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
					if (initial == null) {
						P na = new P("not-assigned"); na.setText(MESSAGES.notAssigned());
						extIdOld.add(na);
					} else {
						P extId = new P(initial.hasExternalId() ? "extid" : "no-extid");
						extId.setText(initial.hasExternalId() ? initial.getExternalId() : MESSAGES.noExternalId());
						extIdOld.add(extId);
					}
					P extIdArrow = new P(); extIdArrow.setHTML(MESSAGES.assignmentArrow());
					extIdArrows.add(extIdArrow);
					if (current == null) {
						P na = new P("not-assigned"); na.setText(MESSAGES.notAssigned());
						extIdNew.add(na);
					} else {
						P extId = new P(current.hasExternalId() ? "extid" : "no-extid");
						extId.setText(current.hasExternalId() ? current.getExternalId() : MESSAGES.noExternalId());
						extIdNew.add(extId);
					}
					if (initial == null) {
						P na = new P("not-assigned"); na.setText(MESSAGES.notAssigned());
						nameOld.add(na);
					} else {
						P name = new P(); name.setText(initial.getInstructorName());
						nameOld.add(name);
					}
					P nameArrow = new P(); nameArrow.setHTML(MESSAGES.assignmentArrow());
					nameArrows.add(nameArrow);
					if (current == null) {
						P na = new P("not-assigned"); na.setText(MESSAGES.notAssigned());
						nameNew.add(na);
					} else {
						P name = new P(); name.setText(current.getInstructorName());
						nameNew.add(name);
					}
				}
				
				line.add(courses);
				line.add(sections);
				line.add(extIdOld);
				line.add(extIdArrows);
				line.add(extIdNew);
				line.add(nameOld);
				line.add(nameArrows);
				line.add(nameNew);
				line.add(new Objectives(suggestion.getValues()));
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
	
	protected void showAssignment(SuggestionInfo suggestion) {
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
					if (initial == null) {
						P na = new P("not-assigned"); na.setText(MESSAGES.notAssigned());
						line.add(na);
					} else {
						P extId = new P(initial.hasExternalId() ? "extid" : "no-extid");
						extId.setText(initial.hasExternalId() ? initial.getExternalId() : MESSAGES.noExternalId());
						line.add(extId);
					}
					P extIdArrow = new P(); extIdArrow.setHTML(MESSAGES.assignmentArrow());
					line.add(extIdArrow);
				}
				if (current == null) {
					P na = new P("not-assigned"); na.setText(MESSAGES.notAssigned());
					line.add(na);
				} else {
					P extId = new P(current.hasExternalId() ? "extid" : "no-extid");
					extId.setText(current.hasExternalId() ? current.getExternalId() : MESSAGES.noExternalId());
					line.add(extId);
				}
				if ((initial == null && current == null) || (initial != null && initial.equals(current))) {
					line.add(new P());
					line.add(new P());
				} else {
					if (initial == null) {
						P na = new P("not-assigned"); na.setText(MESSAGES.notAssigned());
						line.add(na);
					} else {
						P name = new P(); name.setText(initial.getInstructorName());
						line.add(name);
					}
					P nameArrow = new P(); nameArrow.setHTML(MESSAGES.assignmentArrow());
					line.add(nameArrow);
				}
				if (current == null) {
					P na = new P("not-assigned"); na.setText(MESSAGES.notAssigned());
					line.add(na);
				} else {
					P name = new P(); name.setText(current.getInstructorName());
					line.add(name);
				}
				if (current != null) {
					P p = new P("attributes");
					for (AttributeInterface a: current.getAttributes()) {
						P i = new P("attribute");
						i.setText(a.getName());
						i.setTitle(a.getName() + " (" + a.getType().getLabel() + ")");
						p.add(i);
					}
					line.add(p);
					line.add(new Pref(current.getCoursePreferences()));
					line.add(new TimePreferences(current));
					line.add(new Pref(current.getDistributionPreferences()));
				} else if (initial != null) {
					P p = new P("attributes");
					for (AttributeInterface a: initial.getAttributes()) {
						P i = new P("attribute");
						i.setText(a.getName());
						i.setTitle(a.getName() + " (" + a.getType().getLabel() + ")");
						p.add(i);
					}
					line.add(p);
					line.add(new Pref(initial.getCoursePreferences()));
					line.add(new TimePreferences(initial));
					line.add(new Pref(initial.getDistributionPreferences()));
				} else {
					line.add(new P());
					line.add(new P());
					line.add(new P());
					line.add(new P());
				}
				Objectives obj = new Objectives(initial == null ? null : initial.getValues(), current == null ? null : current.getValues());
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
							computeSuggestions(request, false);
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
		for (int row = iAssignmentRow; row < iAvailableInstructorsRow; row ++)
			iForm.getRowFormatter().setVisible(row, iAssignmentTable.getRowCount() > 1);
		iAssignmentHeader.setEnabled("assign", iAssignmentTable.getRowCount() > 1);
	}
	
	protected void showAvailableInstructors(List<SuggestionInfo> suggestions) {
		iAvailableInstructorsTable.clearTable(1);
		int index = iInstructorsTable.getSelectedRow() - 1;
		if (suggestions != null) {
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
				line.add(p);
				line.add(new Pref(instructor.getCoursePreferences()));
				line.add(new TimePreferences(instructor));
				line.add(new Pref(instructor.getDistributionPreferences()));
				for (int i = 0; i < 3; i++)
					if (!conflicts.isEmpty()) {
						line.add(new Conflicts(conflicts, i));
					} else {
						line.add(new Label());
					}
				line.add(new Objectives(suggestion.getValues()));
				iAvailableInstructorsTable.addRow(suggestion, line);
			}
		}
		iForm.getRowFormatter().setVisible(iAvailableInstructorsRow, iAvailableInstructorsTable.getRowCount() > 1 && iInstructorsTable.getSelectedRow() > 0);
		iForm.getRowFormatter().setVisible(iAvailableInstructorsRow + 1, iAvailableInstructorsTable.getRowCount() > 1 && iInstructorsTable.getSelectedRow() > 0);
	}
	
	protected void populate(TeachingRequestInfo request, Integer index) {
		iRequest = request;
		setText(MESSAGES.dialogTeachingRequestDetail(request.getCourse().getCourseName(), request.getSections().get(0).getSectionType() + (request.getSections().get(0).getExternalId() == null ? "" : " " + request.getSections().get(0).getExternalId())));
		iAssignmentHeader.clearMessage();
		
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
		} else if (index != null) {
			iInstructorsTable.setSelected(1 + index, true);
		}
		
		for (int row = iAssignmentRow; row < iAvailableInstructorsRow; row ++)
			iForm.getRowFormatter().setVisible(row, iAssignmentTable.getRowCount() > 1);
		
		showAvailableInstructors(null);
		
		iSuggestionsRequest = null;
		iSuggestionsTable.clearTable(1);
		iForm.getRowFormatter().setVisible(iSuggestionsRow, false);
		iForm.getRowFormatter().setVisible(iSuggestionsRow + 1, false);
		if (iInstructorsTable.getSelectedRow() > 0) computeSuggestions();
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
		private String iPattern = null;
		private List<PreferenceInfo> iPreferences = null;
		
		public TimePreferences(InstructorInfo instructor) {
			super("preferences");
			iPattern = instructor.getAvailability();
			iPreferences = instructor.getTimePreferences();
			addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					InstructorAvailabilityHint.showHint(getElement(), iPattern, true, null);
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
		
		public Objectives(Map<String, Double> initial, Map<String, Double> current) {
			this();
			setValue(initial, current);
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
		
		public void setValue(Map<String, Double> initial, Map<String, Double> current) {
			clear();
			Set<String> keys = new TreeSet<String>();
			if (initial != null) keys.addAll(initial.keySet());
			if (current != null) keys.addAll(current.keySet());
			for (String key: keys) {
				Double base = (initial == null ? null : initial.get(key));
				Double value = (current == null ? null : current.get(key));
				if (value == null) value = -base;
				else if (base != null) value -= base;
				if (Math.abs(value) < 0.001) continue;
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
		public Conflicts(List<AssignmentInfo> conflicts, int column) {
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
}
