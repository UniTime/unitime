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
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InstructorEditForm;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.interfaces.ExternalUidLookup.UserInfo;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DefaultRoomAvailabilityService;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.IdValue;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;


/** 
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Action(value = "instructorDetail", results = {
		@Result(name = "showInstructorDetail", type = "tiles", location = "instructorDetail.tiles"),
		@Result(name = "backToInstructors", type = "redirect", location = "/instructorSearch.action")
	})
@TilesDefinition(name = "instructorDetail.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Instructor Detail"),
		@TilesPutAttribute(name = "body", value = "/user/instructorDetail.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	})
public class InstructorDetailAction extends PreferencesAction2<InstructorEditForm> {
	private static final long serialVersionUID = -7822668104506918852L;

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static GwtConstants CONST = Localization.create(GwtConstants.class);
	
	protected String instructorId = null;
	protected String op2 = null;

	public String getInstructorId() { return instructorId; }
	public void setInstructorId(String instructorId) { this.instructorId = instructorId; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }	
	
	public String execute() throws Exception{
		if (form == null) form = new InstructorEditForm();
		
		super.execute();
		
		//Read parameters
        if (instructorId == null && request.getAttribute("instructorId") != null)
        	instructorId = (String)request.getAttribute("instructorId");
        if (instructorId == null) instructorId = form.getInstructorId();

        if (op == null) op = form.getOp();
        if (op2 != null && !op2.isEmpty()) op = op2;
        
        if (instructorId == null || instructorId.isEmpty()) {
	    	List<DepartmentalInstructor> instructors = DepartmentalInstructor.getUserInstructors(sessionContext.getUser());
	    	if (instructors != null) {
	    		String deptId = (String)request.getSession().getAttribute(Constants.DEPT_ID_ATTR_NAME);
	    		if (deptId != null)
	    			for (DepartmentalInstructor i: instructors)
		    			if (i.getDepartment().getUniqueId().toString().equals(deptId) && sessionContext.hasPermission(i, Right.InstructorDetail)) {
		    				instructorId = i.getUniqueId().toString();
		    				break;
		    			}
	    		if (instructorId == null || instructorId.isEmpty())
		    		for (DepartmentalInstructor i: instructors)
		    			if (sessionContext.hasPermission(i, Right.InstructorDetail)) {
		    				instructorId = i.getUniqueId().toString();
		    				break;
		    			}
	    	}
	    }
        
        sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorDetail);
        
        // Read instructor id from form
        if (MSG.actionEditInstructor().equals(op)
                || MSG.actionEditInstructorPreferences().equals(op)
                || MSG.actionEditInstructorAssignmentPreferences().equals(op)
                || MSG.actionBackToInstructors().equals(op)
//                || MSG.actionDisplayInstructorPreferences().equals(op)
                || MSG.actionNextInstructor().equals(op)
                || MSG.actionPreviousInstructor().equals(op)
                ) {
        	instructorId = form.getInstructorId();
        } else {
        	form.reset();
        }
        
        Debug.debug("op: " + op);
        Debug.debug("instructor: " + instructorId);

        // Check instructor exists
        if (instructorId==null || instructorId.trim().isEmpty())
        	throw new Exception(MSG.errorNoInstructorId());
	        
        // Cancel - Go back to Instructors List Screen
        if (MSG.actionBackToInstructors().equals(op)) {
        	return "backToInstructors";
        }
        
        // If subpart id is not null - load subpart info
	    DepartmentalInstructorDAO idao = DepartmentalInstructorDAO.getInstance();
	    DepartmentalInstructor inst = idao.get(Long.valueOf(instructorId));
        LookupTables.setupInstructorDistribTypes(request, sessionContext, inst);
        
        // Edit Information - Redirect to info edit screen
        if (MSG.actionEditInstructor().equals(op)) {
        	sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorEdit);
        	response.sendRedirect( response.encodeURL("instructorInfoEdit.action?instructorId="+instructorId) );
        	return null;
        }
	        
        // Edit Preference - Redirect to prefs edit screen
        if (MSG.actionEditInstructorPreferences().equals(op)) {
        	sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorPreferences);
        	response.sendRedirect( response.encodeURL("instructorPrefEdit.action?instructorId="+instructorId) );
        	return null;
        }
        
        if(MSG.actionEditInstructorAssignmentPreferences().equals(op)) {
        	sessionContext.checkPermission(inst.getDepartment(), Right.InstructorAssignmentPreferences);
        	response.sendRedirect( response.encodeURL("instructorAssignmentPref.action?instructorId="+instructorId) );
        	return null;
        }
        
        if (MSG.actionNextInstructor().equals(op)) {
        	response.sendRedirect(response.encodeURL("instructorDetail.action?instructorId="+form.getNextId()));
            return null;
        }
        
        if (MSG.actionPreviousInstructor().equals(op)) {
        	response.sendRedirect(response.encodeURL("instructorDetail.action?instructorId="+form.getPreviousId()));
        	return null;
        }
        
        doLoad(inst);
        
        BackTracker.markForBack(
        		request,
        		"instructorDetail.action?instructorId=" + instructorId,
        		MSG.backInstructor(form.getName()==null?"null":form.getName().trim()),
        		true, sessionContext.hasPermission(Right.HasRole) ? false : true);
	        

        //load class assignments
        Set allClasses = new HashSet();
        for (Iterator i=DepartmentalInstructor.getAllForInstructor(inst, inst.getDepartment().getSession().getUniqueId()).iterator();i.hasNext();) {
            DepartmentalInstructor di = (DepartmentalInstructor)i.next();
            allClasses.addAll(di.getClasses());
        }
		if (!allClasses.isEmpty()) {
			boolean hasTimetable = sessionContext.hasPermission(Right.ClassAssignments);

			WebTable classTable =
		    	(hasTimetable? 
		    			new WebTable( 10,
		    					null,
		    					new String[] {MSG.columnClass(), MSG.columnInstructorCheckConflicts(), MSG.columnInstructorShare(), MSG.columnTeachingResponsibility(),
		    							MSG.columnLimit(), MSG.columnEnrollment(), MSG.columnManager(), MSG.columnAssignedTime(), 
		    							MSG.columnAssignedDatePattern(), MSG.columnAssignedRoom()},
		    					new String[] {"left", "left","left", "left", "left", "left", "left", "left", "left", "left"},
		    					null )
		    	:
	    			new WebTable( 6,
	    					null,
	    					new String[] {MSG.columnClass(), MSG.columnInstructorCheckConflicts(), MSG.columnInstructorShare(), MSG.columnTeachingResponsibility(),
	    							MSG.columnLimit(), MSG.columnManager()},
	    					new String[] {"left", "left","left", "left", "left", "left"},
	    					null )
		    	);
		    
		    String backType = request.getParameter("backType");
		    String backId = request.getParameter("backId");
					
			TreeSet classes = new TreeSet(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_LABEL)));
			classes.addAll(allClasses);
			
			Vector classIds = new Vector(classes.size());
			
			//Get class assignment information
			for (Iterator iterInst = classes.iterator(); iterInst.hasNext();) {
				ClassInstructor ci = (ClassInstructor) iterInst.next();
				Class_ c = ci.getClassInstructing();
				classIds.add(c.getUniqueId());
				
				String limitString = "";
		    	if (!c.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue()) {
		    		if (c.getExpectedCapacity() != null) {
		    			limitString = c.getExpectedCapacity().toString();
		    			if (c.getMaxExpectedCapacity() != null && !c.getMaxExpectedCapacity().equals(c.getExpectedCapacity())){
		    				limitString = limitString + "-" + c.getMaxExpectedCapacity().toString();
		    			}
		    		} else {
		    			limitString = "0";
		    			if (c.getMaxExpectedCapacity() != null && c.getMaxExpectedCapacity().intValue() != 0){
		    				limitString = limitString + "-" + c.getMaxExpectedCapacity().toString();
		    			}
		    		}
		    	}
		    	
		    	String enrollmentString = "";
		    	if (c.getEnrollment() != null) {
		    		enrollmentString = c.getEnrollment().toString();
		    	} else {
		    		enrollmentString = "0";
		    	}
		    	
		    	String managingDept = null;
		    	if (c.getManagingDept()!=null) {
		    		Department d = c.getManagingDept();
		    		managingDept = d.getManagingDeptAbbv();
		    	}
		    	
		    	String assignedTime = "";
		    	String assignedDate = "";
		    	String assignedRoom = "";
		    	ClassAssignmentDetails ca = ClassAssignmentDetails.createClassAssignmentDetails(sessionContext, getCourseTimetablingSolverService().getSolver(), c.getUniqueId(),false);
		    	if (ca == null) {
		    		try {
		    			Assignment a = getClassAssignmentService().getAssignment().getAssignment(c);
		    			if (a.getUniqueId() != null)
		    				ca = ClassAssignmentDetails.createClassAssignmentDetailsFromAssignment(sessionContext, a.getUniqueId(), false);
		    		} catch (Exception e) {}
		    	}
		    	if (ca != null) {
		    		if (ca.getAssignedTime() != null) {
			    		assignedTime = ca.getAssignedTime().toHtml(false, false, true, true);
			    		assignedDate = ca.getAssignedTime().getDatePatternHtml();
		    		}
					if (ca.getAssignedRoom() != null) {
						for (int i=0;i<ca.getAssignedRoom().length;i++) {
							if (i>0) assignedRoom += ", ";
							assignedRoom += ca.getAssignedRoom()[i].toHtml(false,false,true);
						}
					}
		    	}
		    	String icon = null, bgColor = null, title = null;
		    	if (!c.isCancelled() && ci.isLead()) {
		        	Set<Assignment> conflicts = null;
		        	try { conflicts = getClassAssignmentService().getAssignment().getConflicts(c.getUniqueId()); } catch (Exception e) {}
		        	if (conflicts != null && !conflicts.isEmpty()) {
		        		bgColor = "#fff0f0";
		    			String s = "";
		    			for (Assignment x: conflicts) {
		    				if (!s.isEmpty()) s += ", ";
		    				s += (x.getClassName() + " " + x.getPlacement().getName(CONST.useAmPm())).trim();
		    			}
		    			title = MSG.classIsConflicting(c.getClassLabel(), s);
						icon = "<IMG alt='" + MSG.classIsConflicting(c.getClassLabel(), s) + "' title='" + MSG.classIsConflicting(c.getClassLabel(), s) + "' src='images/warning.png' style='margin-left: 1px; margin-right: 3px; vertical-align: top;'>";
		        	} else {
		        		Set<TimeBlock> ec = null;
		        		try { ec = getClassAssignmentService().getAssignment().getConflictingTimeBlocks(c.getUniqueId()); } catch (Exception e) {}
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
		        			bgColor = "#fff0f0";
		        			title = MSG.classIsConflicting(c.getClassLabel(), s);
		            		icon = "<IMG alt='" + MSG.classIsConflicting(c.getClassLabel(), s) + "' title='" + MSG.classIsConflicting(c.getClassLabel(), s) + "' src='images/warning.png' style='margin-left: 1px; margin-right: 3px; vertical-align: top;'>";
		        		}
		        	}			    		
		    	}
	        	
	    		String onClick = null;
	    		if (sessionContext.hasPermission(c, Right.ClassDetail)) {
	    			onClick = "onClick=\"document.location='classDetail.action?cid="+c.getUniqueId()+"';\"";
	    		}
	    		
	    		boolean back = "PreferenceGroup".equals(backType) && c.getUniqueId().toString().equals(backId);
	    		String responsibility = (ci.getResponsibility() == null ? "" : ci.getResponsibility().getLabel());
		    	
	    		WebTableLine line = null;
				if (hasTimetable) {
					line = classTable.addLine(
							onClick,
							new String[] {
								(back?"<A name=\"back\"></A>":"")+
								(icon == null ? "" : icon) + c.getClassLabel(),
								(ci.isLead().booleanValue()?"<IMG border='0' alt='true' align='absmiddle' src='images/accept.png'>":""),
								ci.getPercentShare()+"%",
								responsibility,
								limitString,
								enrollmentString,
								managingDept,
								assignedTime,
								assignedDate,
								assignedRoom
							},
							null,null);
				} else {
					line = classTable.addLine(
							onClick,
							new String[] {
								(back?"<A name=\"back\"></A>":"")+
								c.getClassLabel(),
								(ci.isLead().booleanValue()?"<IMG border='0' alt='true' align='absmiddle' src='images/accept.png'>":""),
								ci.getPercentShare()+"%",
								responsibility,
								limitString,
								managingDept
							},
							null,null);
				}
				
				if (bgColor != null) line.setBgColor(bgColor);
				if (title != null) line.setTitle(title);
				
				if (c.isCancelled()) {
					line.setStyle("color: gray; font-style: italic;");
					line.setTitle(MSG.classNoteCancelled(c.getClassLabel()));
				}
			}
			
			Navigation.set(sessionContext, Navigation.sClassLevel, classIds);

			String tblData = classTable.printTable();
			request.setAttribute("classTable", tblData);
		}
			
		if (ApplicationProperty.RoomAvailabilityIncludeInstructors.isTrue() && inst.getExternalUniqueId() != null && !inst.getExternalUniqueId().isEmpty() &&
				RoomAvailability.getInstance() != null && RoomAvailability.getInstance() instanceof DefaultRoomAvailabilityService) {
				WebTable.setOrder(sessionContext, "instructorUnavailability.ord", request.getParameter("iuord"), 1);
				WebTable eventTable = new WebTable(5, "Instructor Unavailability", "instructorDetail.action?instructorId=" + instructorId + "&iuord=%%", new String[] {"Event", "Type", "Date", "Time", "Room"}, new String[] {"left", "left", "left", "left", "left"}, null);
				
				Formats.Format<Date> dfShort = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
				Formats.Format<Date> dfLong = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_LONG);
						
				org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
				Map<Event, Set<Meeting>> unavailabilities = new HashMap<Event, Set<Meeting>>();
				for (Meeting meeting: hibSession.createQuery(
						"select distinct m from Event e inner join e.meetings m left outer join e.additionalContacts c, Session s " +
						"where type(e) in (CourseEvent, SpecialEvent, UnavailableEvent) and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate " +
						"and s.uniqueId = :sessionId and (e.mainContact.externalUniqueId = :user or c.externalUniqueId = :user) and m.approvalStatus = 1",
						Meeting.class
						)
						.setParameter("sessionId", sessionContext.getUser().getCurrentAcademicSessionId(), org.hibernate.type.LongType.INSTANCE)
						.setParameter("user", inst.getExternalUniqueId(), org.hibernate.type.StringType.INSTANCE)
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
                        String date = m.getDays() + " " + (m.getMeetings().size() == 1 ? dfLong.format(m.getMeetings().first().getMeetingDate()) : dfShort.format(m.getMeetings().first().getMeetingDate()) + " - " + dfLong.format(m.getMeetings().last().getMeetingDate()));
                        String time = m.getMeetings().first().startTime() + " - " + m.getMeetings().first().stopTime();
                        String room = (m.getMeetings().first().getLocation() == null ? "" : m.getMeetings().first().getLocation().getLabelWithHint());
                        eventTable.addLine(
                        		sessionContext.hasPermission(event, Right.EventDetail) ? "onClick=\"showGwtDialog('Event Detail', 'gwt.jsp?page=events&menu=hide#event=" + event.getUniqueId() + "','900','85%');\"" : null,
                        		new String[] {
                        			event.getEventName(),
                        			event.getEventTypeAbbv(),
                        			date, time, room},
                        		new Comparable[] {
                        			event.getEventName(),
                        			event.getEventType(),
                        			m.getMeetings().first().getMeetingDate(),
                        			m.getMeetings().first().getStartPeriod(),
                        			room
                        		});
								
					}
				}
				
				if (!eventTable.getLines().isEmpty())
					request.setAttribute("eventTable", eventTable.printTable(WebTable.getOrder(sessionContext, "instructorUnavailability.ord")));
						
			}
			
			//// Set display distribution to Not Applicable
			/*
			request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, 
					"<FONT color=696969>Distribution Preferences Not Applicable</FONT>");
					*/

			form.setDisplayPrefs(CommonValues.Yes.eq(sessionContext.getUser().getProperty(UserProperty.DispInstructorPrefs)));
			
			if (MSG.actionDisplayInstructorPreferences().equals(op) || "true".equals(request.getParameter("showPrefs"))) { 
				form.setDisplayPrefs(true);
				sessionContext.getUser().setProperty(UserProperty.DispInstructorPrefs, CommonValues.Yes.value());
			}
			
			if (MSG.actionHideInstructorPreferences().equals(op) || "false".equals(request.getParameter("showPrefs"))) {
				form.setDisplayPrefs(false);
				sessionContext.getUser().setProperty(UserProperty.DispInstructorPrefs, CommonValues.No.value());
			}

	        LookupTables.setupInstructorAttributeTypes(request, inst);
	        LookupTables.setupInstructorAttributes(request, inst);

			if (form.isDisplayPrefs()) {
		        // Initialize Preferences for initial load 
		        Set timePatterns = new HashSet();
		        form.setAvailableTimePatterns(null);
		        initPrefs(inst, null, false);
		        timePatterns.add(new TimePattern(Long.valueOf(-1)));
		    	//timePatterns.addAll(TimePattern.findApplicable(request,30,false));
		        
				// Process Preferences Action
				processPrefAction();
				
				// Generate Time Pattern Grids
				//super.generateTimePatternGrids(request, form, inst, timePatterns, "init", timeVertical, false, null);
				for (Preference pref: inst.getPreferences()) {
					if (pref instanceof TimePref) {
						form.setAvailability(((TimePref)pref).getPreference());
						break;
					}
				}
	
		        LookupTables.setupRooms(request, inst);		 // Room Prefs
		        LookupTables.setupBldgs(request, inst);		 // Building Prefs
		        LookupTables.setupRoomFeatures(request, inst); // Preference Levels
		        LookupTables.setupRoomGroups(request, inst);   // Room Groups
		        LookupTables.setupCourses(request, inst); // Courses
			}
			
	        form.setMaxLoad(inst.getMaxLoad() == null ? null : Formats.getNumberFormat("0.##").format(inst.getMaxLoad()));
	        form.setTeachingPreference(inst.getTeachingPreference() == null ? PreferenceLevel.sProhibited : inst.getTeachingPreference().getPrefProlog());
	        if (inst.getMaxLoad() == null && (inst.getTeachingPreference() == null || inst.getTeachingPreference().getPrefProlog().equals(PreferenceLevel.sProhibited)))
	        	form.setTeachingPreference(null);
	        form.clearAttributes();
	        for (InstructorAttribute attribute: inst.getAttributes())
	        	form.setAttribute(attribute.getUniqueId(), true);
			
			DepartmentalInstructor previous = inst.getPreviousDepartmentalInstructor(sessionContext, Right.InstructorDetail);
			form.setPreviousId(previous==null?null:previous.getUniqueId().toString());
			DepartmentalInstructor next = inst.getNextDepartmentalInstructor(sessionContext, Right.InstructorDetail);
			form.setNextId(next==null?null:next.getUniqueId().toString());
			
			if (inst.getExternalUniqueId() != null && !inst.getExternalUniqueId().isEmpty()) {
				List<IdValue> departments = new ArrayList<IdValue>();
				for (DepartmentalInstructor di: DepartmentalInstructorDAO.getInstance().getSession().createQuery(
						"from DepartmentalInstructor i where i.department.session.uniqueId = :sessionId and i.externalUniqueId = :externalId " +
						"order by i.department.deptCode", DepartmentalInstructor.class)
						.setParameter("sessionId", sessionContext.getUser().getCurrentAcademicSessionId(), org.hibernate.type.LongType.INSTANCE)
						.setParameter("externalId", inst.getExternalUniqueId(), org.hibernate.type.StringType.INSTANCE).setCacheable(true).list()) {
					if (sessionContext.hasPermission(di, Right.InstructorDetail)) {
						departments.add(new IdValue(di.getUniqueId(), di.getDepartment().getLabel()));
					}
				}
				if (departments.size() > 1)
					form.setDepartments(departments);
			}
			
	        return "showInstructorDetail";
	}

	private void doLoad(DepartmentalInstructor inst) {
        // populate form
		form.setInstructorId(instructorId);
		
		if (inst.hasUnavailabilities())
			request.setAttribute("unavailableDaysPattern", inst.getUnavailablePatternHtml(false));
		
		NameFormat nameFormat = NameFormat.fromReference(sessionContext.getUser().getProperty(UserProperty.NameFormat));
		form.setName(nameFormat.format(inst));
		
		form.setEmail(inst.getEmail());
		form.setDeptCode(inst.getDepartment().getDeptCode());
		form.setDeptName(inst.getDepartment().getLabel());
		
		InstructorSurvey is = InstructorSurvey.getInstructorSurvey(inst);
		form.setHasInstructorSurvey(is != null);
		if (is == null && inst.getExternalUniqueId() != null && !inst.getExternalUniqueId().isEmpty()) {
			form.setShowInstructorSurvey(sessionContext.hasPermission(inst.getDepartment(), Right.InstructorSurveyAdmin));
			if (sessionContext.getUser().getExternalUserId().equals(inst.getExternalUniqueId()) && sessionContext.hasPermission(Right.InstructorSurvey))
				form.setShowInstructorSurvey(true);
		}
		
		String puid = inst.getExternalUniqueId();
		if (puid != null) {
			form.setPuId(puid);
		}
				
		if (inst.getPositionType() != null) {
			form.setPosType(inst.getPositionType().getLabel().trim());
		}
		
		if (inst.getCareerAcct() != null) {
			form.setCareerAcct(inst.getCareerAcct().trim());
		} else if (DepartmentalInstructor.canLookupInstructor()) {
			try {
				UserInfo user = DepartmentalInstructor.lookupInstructor(puid);
				if (user != null && user.getUserName() != null)
					form.setCareerAcct(user.getUserName());
			} catch (Exception e) {}
		}
		
		if (inst.getNote() != null) {
			form.setNote(inst.getNote().trim());
		}
				
		request.getSession().setAttribute(Constants.DEPT_ID_ATTR_NAME, inst.getDepartment().getUniqueId().toString());
		
		// Check column ordering - default to name
		String orderStr = request.getParameter("order");
		int cols = 2;
		int order = 1;

		if (orderStr != null && orderStr.trim().length() != 0) {
			try {
				order = Integer.parseInt(orderStr);
				if (Math.abs(order) > cols)
					order = 1;
			} catch (Exception e) {
				order = 1;
			}
		}
        
        form.setIgnoreDist(inst.isIgnoreToFar()==null?false:inst.isIgnoreToFar().booleanValue());
	}

}

