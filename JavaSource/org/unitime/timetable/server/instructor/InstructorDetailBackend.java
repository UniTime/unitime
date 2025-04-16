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
package org.unitime.timetable.server.instructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorDetailRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorDetailResponse;
import org.unitime.timetable.interfaces.ExternalUidLookup.UserInfo;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.ExaminationsTableBuilder;
import org.unitime.timetable.server.courses.SubpartDetailBackend;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy.AssignmentInfo;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails.RoomInfo;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.DefaultRoomAvailabilityService;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.JavascriptFunctions;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.BackTracker.BackItem;

@GwtRpcImplements(InstructorDetailRequest.class)
public class InstructorDetailBackend implements GwtRpcImplementation<InstructorDetailRequest, InstructorDetailResponse> {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static final GwtMessages GWT = Localization.create(GwtMessages.class);
	protected final static GwtConstants CONST = Localization.create(GwtConstants.class);

	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public InstructorDetailResponse execute(InstructorDetailRequest request, SessionContext context) {
		org.hibernate.Session hibSession = SchedulingSubpartDAO.getInstance().getSession();
		DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(request.getInstructorId(), hibSession);
		context.checkPermission(instructor, Right.InstructorDetail);
		
		String nameFormat = UserProperty.NameFormat.get(context.getUser());
		
		if (request.getAction() == null) {
	        BackTracker.markForBack(context,
	        		"instructor?id=" + request.getInstructorId(),
	        		MSG.backInstructor(instructor.getName(nameFormat)),
	        		true, false);
		} else {
			switch(request.getAction()) {
			}
		}
		
		InstructorDetailResponse response = new InstructorDetailResponse();
		response.setConfirms(JavascriptFunctions.isJsConfirm(context));
		response.setInstructorId(instructor.getUniqueId());
		response.setInstructorName(instructor.getName(nameFormat));
		response.setDepartmentId(instructor.getDepartment().getUniqueId());
		response.setExternalId(instructor.getExternalUniqueId());

		DepartmentalInstructor next = instructor.getNextDepartmentalInstructor(context, Right.InstructorDetail); 
        response.setNextId(next==null ? null : next.getUniqueId());
        DepartmentalInstructor previous = instructor.getPreviousDepartmentalInstructor(context, Right.InstructorDetail); 
        response.setPreviousId(previous == null ? null : previous.getUniqueId());
        
		List<DepartmentalInstructor> other = DepartmentalInstructor.getAllForInstructor(instructor);
		if (other == null || other.size() == 1) {
			response.addProperty(MSG.propertyDepartment()).add(instructor.getDepartment().getDeptCode() + " - " + instructor.getDepartment().getName());
		} else {
			FilterParameterInterface depts = new FilterParameterInterface();
			depts.setName("instructorId");
			depts.setLabel(MSG.propertyDepartment());
			depts.setType("list");
			depts.setCollapsible(false);
			depts.setDefaultValue(instructor.getUniqueId().toString());
			for (DepartmentalInstructor i: other)
				if (context.hasPermission(i, Right.InstructorDetail))
					depts.addOption(i.getUniqueId().toString(), i.getDepartment().getDeptCode() + " - " + i.getDepartment().getName());
			if (!depts.hasOptions() || depts.getOptions().size() <= 1) {
				// no selection
				response.addProperty(MSG.propertyDepartment()).add(instructor.getDepartment().getDeptCode() + " - " + instructor.getDepartment().getName());
			} else {
				response.setDepartmentFilter(depts);
			}
		}
		
		if (instructor.getCareerAcct() != null) {
			response.addProperty(MSG.propertyAccountName()).add(instructor.getCareerAcct());
		} else if (DepartmentalInstructor.canLookupInstructor() && instructor.getExternalUniqueId() != null && !instructor.getExternalUniqueId().isEmpty()) {
			try {
				UserInfo user = DepartmentalInstructor.lookupInstructor(instructor.getExternalUniqueId());
				if (user != null && user.getUserName() != null)
					response.addProperty(MSG.propertyAccountName()).add(user.getUserName());
			} catch (Exception e) {}
		}
		
		if (instructor.getEmail() != null && !instructor.getEmail().isEmpty())
			response.addProperty(MSG.propertyEmail()).add(instructor.getEmail());
		if (instructor.getPositionType() != null)
			response.addProperty(MSG.propertyInstructorPosition()).add(instructor.getPositionType().getLabel());
		if (instructor.getNote() != null && !instructor.getNote().isEmpty())
			response.addProperty(MSG.propertyNote()).add(instructor.getPositionType().getLabel(), true).addStyle("white-space: pre-wrap;");
		if (instructor.isIgnoreToFar()) {
			CellInterface c = response.addProperty(MSG.propertyIgnoreTooFar());
			c.add(MSG.enabled()).setColor("red");
			c.add(" -- ").addStyle("padding-left:10px;");
			c.add(MSG.descriptionInstructorIgnoreTooFar()).addStyle("font-style: italic;");
		}
		if (instructor.getTeachingPreference() != null) {
			response.addProperty(MSG.propertyTeachingPreference()).add(instructor.getTeachingPreference().getPrefName())
				.setColor(PreferenceLevel.prolog2color(instructor.getTeachingPreference().getPrefProlog()));
		}
		if (instructor.getMaxLoad() != null)
			response.addProperty(MSG.propertyMaxLoad()).add(Formats.getNumberFormat("0.##").format(instructor.getMaxLoad()));
		Map<String, TreeSet<String>> attrs = new HashMap<String, TreeSet<String>>();
		for (InstructorAttribute a: instructor.getAttributes()) {
			String key = (a.getType() == null ? "" : a.getType().getLabel());
			TreeSet<String> names = attrs.get(key);
			if (names == null) {
				names = new TreeSet<String>();
				attrs.put(key, names);
			}
			names.add(a.getName());
		}
		for (String name: new TreeSet<String>(attrs.keySet())) {
			CellInterface c = response.addProperty((name.isEmpty() ? MSG.sectionAttributes() : name) + ":");
			for (String a: attrs.get(name))
				c.add(a).addStyle("unitime-InstructorAttribute").setInline(false);
		}
		
		InstructorSurvey is = InstructorSurvey.getInstructorSurvey(instructor);
		if (is != null)
			response.addOperation("has-survey");
		
		TreeSet<ClassInstructor> classes = new TreeSet<ClassInstructor>(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_LABEL)));
        for (DepartmentalInstructor di: DepartmentalInstructor.getAllForInstructor(instructor))
        	classes.addAll(di.getClasses());
		if (!classes.isEmpty()) {
			boolean hasTimetable = context.hasPermission(Right.ClassAssignments);
			
			boolean hasResponsibility = false;
			for (ClassInstructor ci: classes)
				if (ci.getResponsibility() != null) hasResponsibility = true;
			
			TableInterface classTable = new TableInterface();
			classTable.setName(MSG.sectionTitleClassAssignments());
			classTable.setId("InstructorClassAssignments");
			LineInterface header = classTable.addHeader();
			header.addCell(MSG.columnClass()).setSortable(true);
			header.addCell(MSG.columnInstructorCheckConflicts()).setSortable(true).setTextAlignment(Alignment.CENTER);
			header.addCell(MSG.columnInstructorShare()).setSortable(true).setTextAlignment(Alignment.RIGHT);
			if (hasResponsibility)
				header.addCell(MSG.columnTeachingResponsibility()).setSortable(true);
			header.addCell(MSG.columnLimit()).setSortable(true).setTextAlignment(Alignment.RIGHT);
			header.addCell(MSG.columnEnrollment()).setSortable(true).setTextAlignment(Alignment.RIGHT);
			header.addCell(MSG.columnManager()).setSortable(true);
			if (hasTimetable) {
				header.addCell(MSG.columnAssignedTime()).setSortable(true);
				header.addCell(MSG.columnAssignedDatePattern()).setSortable(true);
				header.addCell(MSG.columnAssignedRoom()).setSortable(true);
			}
			for (CellInterface cell: header.getCells()) {
	    		cell.setClassName("WebTableHeader");
	    		cell.setText(cell.getText().replace("<br>", "\n"));
	    	}
			
			List<Long> classIds = new ArrayList<Long>();
			
			boolean showSuffix = ApplicationProperty.InstructorShowClassSufix.isTrue();
			
			for (ClassInstructor ci: classes) {
				Class_ c = ci.getClassInstructing();
				classIds.add(c.getUniqueId());
				
				LineInterface line = classTable.addLine();
				line.addCell(ci.getClassInstructing().getClassLabel(showSuffix));
				if (ci.isLead()) {
					line.addCell().setComparable(1).setTextAlignment(Alignment.CENTER).setImage().setSource("images/accept.png").setAlt(MSG.altYes());
				} else {
					line.addCell().setComparable(0);
				}
				
				line.addCell(ci.getPercentShare() + "%").setComparable(ci.getPercentShare()).setTextAlignment(Alignment.RIGHT);
				
				if (hasResponsibility)
					line.addCell(ci.getResponsibility() == null ? "" : ci.getResponsibility().getLabel());
				
				
		    	if (c.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment()) {
		    		line.addCell("\u221E").setComparable(Integer.MAX_VALUE);
		    	} else {
		    		if (c.getExpectedCapacity() != null) {
		    			if (c.getMaxExpectedCapacity() != null && !c.getMaxExpectedCapacity().equals(c.getExpectedCapacity())){
		    				line.addCell(c.getExpectedCapacity() + " - " + c.getMaxExpectedCapacity()).setComparable(c.getExpectedCapacity()).setTextAlignment(Alignment.RIGHT);
		    			} else {
		    				line.addCell(c.getExpectedCapacity().toString()).setComparable(c.getExpectedCapacity()).setTextAlignment(Alignment.RIGHT);
		    			}
		    		} else {
		    			if (c.getMaxExpectedCapacity() != null && c.getMaxExpectedCapacity() != 0){
		    				line.addCell("0 - " + c.getMaxExpectedCapacity()).setComparable(0).setTextAlignment(Alignment.RIGHT);
		    			} else {
		    				line.addCell("0").setComparable(0).setTextAlignment(Alignment.RIGHT);
		    			}
		    		}
		    	}
		    	
		    	line.addCell(c.getEnrollment() == null ? "0" : c.getEnrollment().toString()).setTextAlignment(Alignment.RIGHT);
		    	
		    	line.addCell(c.getManagingDept().getShortLabel()).setTitle(c.getManagingDept().toString());

		    	if (hasTimetable) {
		    		CellInterface assignedTime = line.addCell();
		    		CellInterface assignedDate = line.addCell();
		    		CellInterface assignedRoom = line.addCell();
		    		ClassAssignmentDetails ca = ClassAssignmentDetails.createClassAssignmentDetails(context, courseTimetablingSolverService.getSolver(), c.getUniqueId(),false);
		    		if (ca == null) {
			    		try {
			    			AssignmentInfo a = classAssignmentService.getAssignment().getAssignment(c);
			    			if (a.getUniqueId() != null)
			    				ca = ClassAssignmentDetails.createClassAssignmentDetailsFromAssignment(context, a.getUniqueId(), false);
			    		} catch (Exception e) {}
			    	}
		    		if (ca != null) {
		    			if (ca.getAssignedTime() != null) {
		    				assignedTime.addItem(ca.getAssignedTime().toCell());
		    				assignedDate.add(ca.getAssignedTime().getDatePatternName())
		    					.setColor(PreferenceLevel.int2color(ca.getAssignedTime().getDatePatternPreference()));
		    			}
		    			if (ca.getAssignedRoom() != null) {
		    				for (RoomInfo room: ca.getAssignedRoom()) {
								if (assignedRoom.hasItems()) assignedRoom.add(", ");
								assignedRoom.addItem(room.toCell());
							}
		    			}
		    		}
		    	}
		    	
		    	if (!c.isCancelled() && ci.isLead()) {
		        	Set<AssignmentInfo> conflicts = null;
		        	try { conflicts = classAssignmentService.getAssignment().getConflicts(c.getUniqueId()); } catch (Exception e) {}
		        	if (conflicts != null && !conflicts.isEmpty()) {
		        		line.setBgColor("#fff0f0");
		    			String s = "";
		    			for (AssignmentInfo x: conflicts) {
		    				if (!s.isEmpty()) s += ", ";
		    				s += (x.getClassName() + " " + x.getPlacement().getName(CONST.useAmPm())).trim();
		    			}
		    			line.setWarning(MSG.classIsConflicting(c.getClassLabel(), s));
		        	} else {
		        		Set<TimeBlock> ec = null;
		        		try { ec = classAssignmentService.getAssignment().getConflictingTimeBlocks(c.getUniqueId()); } catch (Exception e) {}
		        		if (ec != null && !ec.isEmpty()) {
		        			String s = "";
		        			String lastName = null, lastType = null;
		        			for (TimeBlock t: ec) {
		        				if (lastName == null || !lastName.equals(t.getEventName()) || !lastType.equals(t.getEventType())) {
		        					lastName = t.getEventName(); lastType = t.getEventType();
		        					if (!s.isEmpty()) s += ", ";
		            				s += lastName + " (" + lastType + ")";
		        				}
		        			}
		        			line.setBgColor("#fff0f0");
		        			line.setWarning(MSG.classIsConflicting(c.getClassLabel(), s));
		        		}
		        	}			    		
		    	}
		    	
	    		if (context.hasPermission(c, Right.ClassDetail))
	    			line.setURL("clazz?id=" + c.getUniqueId());
	    		
	    		if ("PreferenceGroup".equals(request.getBackType()) && c.getUniqueId().toString().equals(request.getBackId()))
	    			line.getCells().get(0).addAnchor("back");
	    		
				if (c.isCancelled()) {
					line.setStyle("color: gray; font-style: italic;");
					line.setTitle(MSG.classNoteCancelled(c.getClassLabel()));
				}
			}
			Navigation.set(context, Navigation.sClassLevel, classIds);
			response.setClasses(classTable);
		}
		
		if (ApplicationProperty.RoomAvailabilityIncludeInstructors.isTrue() && instructor.getExternalUniqueId() != null && !instructor.getExternalUniqueId().isEmpty() &&
				RoomAvailability.getInstance() != null && RoomAvailability.getInstance() instanceof DefaultRoomAvailabilityService) {
			TableInterface eventTable = new TableInterface();
			eventTable.setName(MSG.sectionInstructorUnavailability());
			eventTable.setId("InstructorEventUnavailability");
			LineInterface header = eventTable.addHeader();
			header.addCell(MSG.columnEventName()).setSortable(true);
			header.addCell(MSG.columnEventType()).setSortable(true);
			header.addCell(MSG.columnEventDate()).setSortable(true);
			header.addCell(MSG.columnEventTime()).setSortable(true);
			header.addCell(MSG.columnEventRoom()).setSortable(true);
			for (CellInterface cell: header.getCells()) {
	    		cell.setClassName("WebTableHeader");
	    		cell.setText(cell.getText().replace("<br>", "\n"));
	    	}
			Formats.Format<Date> dfShort = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
			Formats.Format<Date> dfLong = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_LONG);
			
			Map<Event, Set<Meeting>> unavailabilities = new HashMap<Event, Set<Meeting>>();
			for (Meeting meeting: hibSession.createQuery(
					"select distinct m from Event e inner join e.meetings m left outer join e.additionalContacts c, Session s " +
					"where type(e) in (CourseEvent, SpecialEvent, UnavailableEvent) and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate " +
					"and s.uniqueId = :sessionId and (e.mainContact.externalUniqueId = :user or c.externalUniqueId = :user) and m.approvalStatus = 1",
					Meeting.class
					)
					.setParameter("sessionId", context.getUser().getCurrentAcademicSessionId())
					.setParameter("user", instructor.getExternalUniqueId())
					.setCacheable(true).list()) {
				Set<Meeting> meetings = unavailabilities.get(meeting.getEvent());
				if (meetings == null) {
					meetings = new HashSet<Meeting>();
					unavailabilities.put(meeting.getEvent(), meetings);
				}
				meetings.add(meeting);
			}
			for (Event event: new TreeSet<Event>(unavailabilities.keySet())) {
				for (MultiMeeting m: Event.getMultiMeetings(unavailabilities.get(event))) {
					LineInterface line = eventTable.addLine();
					line.addCell(event.getEventName());
					line.addCell(event.getEventTypeAbbv());
					line.addCell(m.getDays() + " " + (m.getMeetings().size() == 1 ? dfLong.format(m.getMeetings().first().getMeetingDate()) : dfShort.format(m.getMeetings().first().getMeetingDate()) + " - " + dfLong.format(m.getMeetings().last().getMeetingDate())));
					line.addCell(m.getMeetings().first().startTime() + " - " + m.getMeetings().first().stopTime());
					if (m.getMeetings().first().getLocation() == null)
						line.addCell();
					else {
						Location location = m.getMeetings().first().getLocation();
						line.addCell(location.getLabel())
							.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + location.getUniqueId() + "');")
							.setMouseOut("$wnd.hideGwtRoomHint();");
					}
				}
				response.setEvents(eventTable);						
			}
		}
		
		ExaminationsTableBuilder examBuilder = new ExaminationsTableBuilder(context, null, null);
    	response.setExaminations(examBuilder.createExamsTable("DepartmentalInstructor", instructor.getUniqueId(), examinationSolverService.getSolver()));
		
		if (CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.DispInstructorPrefs))) {
			response.setPreferences(getPreferenceTable(context, instructor,
					Preference.Type.ROOM_GROUP, Preference.Type.ROOM, Preference.Type.BUILDING, Preference.Type.ROOM_FEATURE,
					Preference.Type.COURSE, Preference.Type.DISTRIBUTION));
		}
		
		if (context.hasPermission(Right.InstructorScheduling) && context.hasPermission(instructor.getDepartment(), Right.InstructorAssignmentPreferences))
			response.addOperation("teaching-assignments");
		
        if (CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.DisplayLastChanges)))
        	response.setLastChanges(getLastChanges(instructor));
        if (context.hasPermission(Right.InstructorScheduling) && context.hasPermission(instructor.getDepartment(), Right.InstructorAssignmentPreferences))
        	response.addOperation("teaching-assignments");
		if (context.hasPermission(instructor, Right.InstructorEdit))
			response.addOperation("edit");
		if (context.hasPermission(instructor.getDepartment(), Right.InstructorAssignmentPreferences))
			response.addOperation("assignment");
		if (context.hasPermission(instructor, Right.InstructorPreferences))
			response.addOperation("preferences");
		if (context.hasPermission(instructor, Right.InstructorEdit))
			response.addOperation("edit");
		if (instructor.getExternalUniqueId() != null && !instructor.getExternalUniqueId().isEmpty()) {
			if (context.hasPermission(instructor.getDepartment(), Right.InstructorSurveyAdmin))
				response.addOperation("survey");
			else if (context.getUser().getExternalUserId().equals(instructor.getExternalUniqueId()) && context.hasPermission(instructor.getDepartment(), Right.InstructorSurvey))
				response.addOperation("survey");
		}
    	if (response.getPreviousId() != null && context.hasPermission(response.getPreviousId(), "DepartmentalInstructor", Right.InstructorDetail))
    		response.addOperation("previous");
    	if (response.getNextId() != null && context.hasPermission(response.getNextId(), "DepartmentalInstructor", Right.InstructorDetail))
    		response.addOperation("next");
    	BackItem back = BackTracker.getBackItem(context, 2);
    	if (back != null) {
    		response.addOperation("back");
    		response.setBackTitle(back.getTitle());
    		response.setBackUrl(back.getUrl() +
    				(back.getUrl().indexOf('?') >= 0 ? "&" : "?") +
    				"backId=" + instructor.getUniqueId() + "&backType=PreferenceGroup");
    	}
    	
		return response;
	}
	
	public static TableInterface getPreferenceTable(SessionContext context, DepartmentalInstructor instructor, Preference.Type... types) {
		TableInterface preferences = SubpartDetailBackend.getPreferenceTable(context, instructor, types);
		
		for (Preference pref: instructor.getPreferences()) {
			if (pref instanceof TimePref) {
				CellInterface c = null;
				if (preferences == null) {
					preferences = new TableInterface();
					c = preferences.addProperty(MSG.propertyTime());
					preferences.addProperty(SubpartDetailBackend.getLegend(false));
				} else {
					c = new CellInterface();
					PropertyInterface av = new PropertyInterface().setName(MSG.propertyTime()).setCell(c);
					preferences.getProperties().add(0, av);
				}
				c.addWidget().setId("UniTimeGWT:InstructorAvailability").setContent(((TimePref)pref).getPreference());
				break;
			}
		}
		
		if (instructor.hasUnavailabilities()) {
			CellInterface c = null;
			if (preferences == null) {
				preferences = new TableInterface();
				c = preferences.addProperty(MSG.propertyUnavailableDates());
			} else {
				c = new CellInterface();
	        	preferences.getProperties().add(preferences.getProperties().size() - 1,
						new PropertyInterface().setName(MSG.propertyUnavailableDates()).setCell(c));
			}
			c.addWidget().setId("UniTimeGWT:InstructorUnavailability").setContent(instructor.getUnavailablePattern());
		}
		
		return preferences;
	}
	
	protected int printLastChangeTableRow(TableInterface table, ChangeLog lastChange) {
		if (lastChange == null) return 0;
		LineInterface line = table.addLine();
		line.addCell(lastChange.getSourceTitle());
		line.addCell(lastChange.getObjectTitle());
		line.addCell(lastChange.getOperationTitle());
		line.addCell(lastChange.getManager().getShortName());
		line.addCell(ChangeLog.sDF.format(lastChange.getTimeStamp()));
		return 1;
    }
	
	protected ChangeLog combine(ChangeLog c1, ChangeLog c2) {
        if (c1==null) return c2;
        if (c2==null) return c1;
        return (c1.compareTo(c2)<0?c2:c1);
    }
	
	public TableInterface getLastChanges(DepartmentalInstructor inst) {
        if (inst==null) return null;
        
        TableInterface table = new TableInterface();
        int nrChanges = 0;
        
        table.setName(MSG.columnLastChanges());
        LineInterface header = table.addHeader();
        header.addCell(MSG.columnPage());
        header.addCell(MSG.columnObject());
        header.addCell(MSG.columnOperation());
        header.addCell(MSG.columnManager());
        header.addCell(MSG.columnDate());
    	for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    	}
    	
        nrChanges += printLastChangeTableRow(table,
                combine(
                        ChangeLog.findLastChange(inst, ChangeLog.Source.INSTRUCTOR_EDIT),
                        ChangeLog.findLastChange(inst, ChangeLog.Source.INSTRUCTOR_MANAGE)));
        
        nrChanges += printLastChangeTableRow(table,
                ChangeLog.findLastChange(inst, ChangeLog.Source.INSTRUCTOR_PREF_EDIT));
        
        nrChanges += printLastChangeTableRow(table, 
                ChangeLog.findLastChange(inst, ChangeLog.Source.INSTRUCTOR_ASSIGNMENT_PREF_EDIT));

    	if (nrChanges > 0) return table;
    	return null;
	}
}
