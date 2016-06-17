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
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
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
	private TeachingRequestsPagePropertiesResponse iProperties;

	private SimpleForm iForm;
	private ScrollPanel iScroll;
	
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
	
	protected void populate(TeachingRequestInfo request) {
		setText(MESSAGES.dialogTeachingRequestDetail(request.getCourse().getCourseName(), request.getSections().get(0).getSectionType() + (request.getSections().get(0).getExternalId() == null ? "" : " " + request.getSections().get(0).getExternalId())));
		iForm.clear();
		iForm.addHeaderRow(MESSAGES.headerTeachingRequest());
		iForm.addRow(MESSAGES.propCourse(), new Label(request.getCourse().getCourseName()));
		UniTimeTable<SectionInfo> sections = new UniTimeTable<SectionInfo>();
		sections.addStyleName("sections");
		List<UniTimeTableHeader> sectionHeader = new ArrayList<UniTimeTableHeader>();
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colSection()));
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colTime()));
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colDate()));
		sectionHeader.add(new UniTimeTableHeader(MESSAGES.colRoom()));
		sections.addRow(null, sectionHeader);
		for (SectionInfo s: request.getSections()) {
			List<Widget> sectionLine = new ArrayList<Widget>();
			sectionLine.add(new Label(s.getSectionType() + (s.getExternalId() == null ? "" : " " + s.getExternalId())));
			sectionLine.add(new HTML(s.getTime() == null ? SECTMSG.arrangeHours() : s.getTime()));
			sectionLine.add(new HTML(s.getDate() == null ? SECTMSG.noDate() : s.getDate()));
			sectionLine.add(new HTML(s.getRoom() == null ? SECTMSG.noRoom() : s.getRoom()));
			if (s.isCommon())
				for (Widget w: sectionLine) w.addStyleName("common");
			sections.addRow(s, sectionLine);
		}
		iForm.addRow(MESSAGES.propSections(), sections);
		iForm.addRow(MESSAGES.propRequestLoad(), new Label(sTeachingLoadFormat.format(request.getLoad())));
		if (!request.getAttributePreferences().isEmpty())
			iForm.addRow(MESSAGES.propAttributePrefs(), new Pref(request.getAttributePreferences()));
		if (!request.getInstructorPreferences().isEmpty())
			iForm.addRow(MESSAGES.propInstructorPrefs(), new Pref(request.getInstructorPreferences()));
		if (!request.getValues().isEmpty()) {
			iForm.addRow(MESSAGES.propObjectives(), new Objectives(request.getValues()));
		}
		UniTimeTable<InstructorInfo> instructors = new UniTimeTable<InstructorInfo>();
		instructors.addStyleName("instructors");
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
		instructors.addRow(null, instructorsHeader);
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
				instructors.addRow(instructor, instructorLine);
			}
		}
		for (int i = request.getNrAssignedInstructors(); i < request.getNrInstructors(); i++) {
			List<Widget> instructorLine = new ArrayList<Widget>();
			instructorLine.add(new Label((instrIndex++) + "."));
			instructorLine.add(new NotAssignedInstructor());
			instructors.addRow(null, instructorLine);
		}
		if (request.getNrInstructors() <= 1)
			instructors.setColumnVisible(0, false);
		iForm.addRow(MESSAGES.propAssignedInstructors(), instructors);
		if (request.hasDomainValues()) {
			iForm.addHeaderRow(MESSAGES.headerAvailableInstructors());
			UniTimeTable<InstructorInfo> domain = new UniTimeTable<InstructorInfo>();
			domain.addStyleName("instructors");
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
			domain.addRow(null, domainHeader);
			for (InstructorInfo instructor: request.getDomainValues()) {
				List<Widget> domainLine = new ArrayList<Widget>();
				Label extId = new Label(instructor.getExternalId());
				if (instructor.getTeachingPreference() != null && !"0".equals(instructor.getTeachingPreference())) {
					PreferenceInterface pref = iProperties.getPreference(instructor.getTeachingPreference());
					if (pref != null) {
						extId.setTitle(pref.getName() + " " + instructor.getExternalId());
						extId.getElement().getStyle().setColor(pref.getColor());
					}
				}
				domainLine.add(extId);
				Label name = new Label(instructor.getInstructorName());
				if (instructor.getTeachingPreference() != null && !"0".equals(instructor.getTeachingPreference())) {
					PreferenceInterface pref = iProperties.getPreference(instructor.getTeachingPreference());
					if (pref != null) {
						name.setTitle(pref.getName() + " " + instructor.getInstructorName());
						name.getElement().getStyle().setColor(pref.getColor());
					}
				}
				domainLine.add(name);
				domainLine.add(new Label(sTeachingLoadFormat.format(instructor.getAssignedLoad()) + " / " + sTeachingLoadFormat.format(instructor.getMaxLoad())));
				P p = new P("attributes");
				for (AttributeInterface a: instructor.getAttributes()) {
					P i = new P("attribute");
					i.setText(a.getName());
					i.setTitle(a.getName() + " (" + a.getType().getLabel() + ")");
					p.add(i);
				}
				domainLine.add(p);
				domainLine.add(new Pref(instructor.getCoursePreferences()));
				domainLine.add(new TimePreferences(instructor));
				domainLine.add(new Pref(instructor.getDistributionPreferences()));
				for (int i = 0; i < 3; i++)
					if (instructor.hasConflicts()) {
						domainLine.add(new Conflicts(instructor.getConflicts(), i));
					} else {
						domainLine.add(new Label());
					}
				domainLine.add(new Objectives(instructor.getValues()));
				domain.addRow(instructor, domainLine);
			}
			iForm.addRow(domain);
		}
		UniTimeHeaderPanel footer = new UniTimeHeaderPanel();
		footer.addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		iForm.addBottomRow(footer);
	}
	
	public class Pref extends P {
		public Pref(List<PreferenceInfo> prefs) {
			super("preferences");
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
		public Objectives(Map<String, Double> values) {
			super("objective");
			for (String key: new TreeSet<String>(values.keySet())) {
				Double value = values.get(key);
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
		NotAssignedInstructor() {
			super("not-assigned");
			setText(MESSAGES.notAssignedInstructor());
		}

		@Override
		public int getColSpan() {
			return 8;
		}
	}
}
