/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.action;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InstructorEditForm;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.interfaces.ExternalUidLookup.UserInfo;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DefaultRoomAvailabilityService;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;


/** 
 * MyEclipse Struts
 * Creation date: 07-18-2006
 * 
 * XDoclet definition:
 * @struts.action path="/instructorDetail" name="instructorEditForm" input="/user/instructorDetail.jsp" scope="request"
 *
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Service("/instructorDetail")
public class InstructorDetailAction extends PreferencesAction {
	public static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		try {
			
	        // Set common lookup tables
	        super.execute(mapping, form, request, response);

			InstructorEditForm frm = (InstructorEditForm) form;
	        MessageResources rsc = getResources(request);
	        ActionMessages errors = new ActionMessages();
	        
	        //Read parameters
	        String instructorId = (request.getParameter("instructorId")==null) 
							        ? (request.getAttribute("instructorId")==null)
							                ? null
							                : request.getAttribute("instructorId").toString()
									: request.getParameter("instructorId");
							                
		    sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorDetail);
	        
		    String op = frm.getOp();
	        // boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));
	        
	        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
	        	op = request.getParameter("op2");

	        //Check op exists
	        if(op==null) 
	            throw new Exception ("Null Operation not supported.");
	        
	        // Read instructor id from form
	        if(op.equals(rsc.getMessage("button.editInstructorInfo"))
	                || op.equals(rsc.getMessage("button.editInstructorPref"))
	                || op.equals(rsc.getMessage("button.backToInstructorList"))
	                || op.equals(rsc.getMessage("button.displayPrefs"))
	                || op.equals(rsc.getMessage("button.nextInstructor"))
	                || op.equals(rsc.getMessage("button.previousInstructor"))
	                ) {
	        	instructorId = frm.getInstructorId();
	        }else {
	        	frm.reset(mapping, request);
	        }
	        
	        Debug.debug("op: " + op);
	        Debug.debug("instructor: " + instructorId);
	        
	        //Check instructor exists
	        if(instructorId==null || instructorId.trim()=="") 
	            throw new Exception ("Instructor Info not supplied.");
	        
	        // Cancel - Go back to Instructors List Screen
	        if(op.equals(rsc.getMessage("button.backToInstructorList")) 
	                && instructorId!=null && instructorId.trim()!="") {
	        	response.sendRedirect( response.encodeURL("instructorList.do"));
	        	return null;
	        }
	        
	        // If subpart id is not null - load subpart info
	        DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
	        DepartmentalInstructor inst = idao.get(new Long(instructorId));
	        
	        //Edit Information - Redirect to info edit screen
	        if(op.equals(rsc.getMessage("button.editInstructorInfo")) 
	                && instructorId!=null && instructorId.trim()!="") {
	        	
			    sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorEdit);

	        	response.sendRedirect( response.encodeURL("instructorInfoEdit.do?instructorId="+instructorId) );
	        	return null;
	        }
	        
	        // Edit Preference - Redirect to prefs edit screen
	        if(op.equals(rsc.getMessage("button.editInstructorPref")) 
	                && instructorId!=null && instructorId.trim()!="") {
	        	
			    sessionContext.checkPermission(instructorId, "DepartmentalInstructor", Right.InstructorPreferences);

	        	response.sendRedirect( response.encodeURL("instructorPrefEdit.do?instructorId="+instructorId) );
	        	return null;
	        }
	        	        
            if (op.equals(rsc.getMessage("button.nextInstructor"))) {
            	response.sendRedirect(response.encodeURL("instructorDetail.do?instructorId="+frm.getNextId()));
            	return null;
            }
            
            if (op.equals(rsc.getMessage("button.previousInstructor"))) {
            	response.sendRedirect(response.encodeURL("instructorDetail.do?instructorId="+frm.getPreviousId()));
            	return null;
            }
	        
	        // Load form attributes that are constant
	        doLoad(request, frm, inst, instructorId);
	        
	        BackTracker.markForBack(
	        		request,
	        		"instructorDetail.do?instructorId=" + instructorId,
	        		"Instructor ("+ (frm.getName()==null?"null":frm.getName().trim()) +")",
	        		true, false);

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
			    			new WebTable( 9,
			    					null,
			    					new String[] {"Class", "Check Conflicts", "Share", "Limit", "Enrollment", "Manager", "Time", "Date", "Room"},
			    					new String[] {"left", "left","left", "left", "left", "left", "left", "left", "left"},
			    					null )
			    	:
		    			new WebTable( 5,
		    					null,
		    					new String[] {"Class", "Check Conflicts", "Share", "Limit", "Manager"},
		    					new String[] {"left", "left","left", "left", "left"},
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
			    	ClassAssignmentDetails ca = ClassAssignmentDetails.createClassAssignmentDetails(sessionContext, courseTimetablingSolverService.getSolver(), c.getUniqueId(),false);
			    	if (ca == null) {
			    		try {
			    			Assignment a = classAssignmentService.getAssignment().getAssignment(c);
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
		    		
		    		String onClick = null;
		    		if (sessionContext.hasPermission(c, Right.ClassDetail)) {
		    			onClick = "onClick=\"document.location='classDetail.do?cid="+c.getUniqueId()+"';\"";
		    		}
		    		
		    		boolean back = "PreferenceGroup".equals(backType) && c.getUniqueId().toString().equals(backId);
			    	
					if (hasTimetable) {
						classTable.addLine(
								onClick,
								new String[] {
									(back?"<A name=\"back\"></A>":"")+
									c.getClassLabel(),
									(ci.isLead().booleanValue()?"<IMG border='0' alt='true' align='absmiddle' src='images/tick.gif'>":""),
									ci.getPercentShare()+"%",
									limitString,
									enrollmentString,
									managingDept,
									assignedTime,
									assignedDate,
									assignedRoom
								},
								null,null);
					} else {
						classTable.addLine(
								onClick,
								new String[] {
									(back?"<A name=\"back\"></A>":"")+
									c.getClassLabel(),
									(ci.isLead().booleanValue()?"<IMG border='0' alt='true' align='absmiddle' src='images/tick.gif'>":""),
									ci.getPercentShare()+"%",
									limitString,
									managingDept
								},
								null,null);
					}
				}
				
				Navigation.set(sessionContext, Navigation.sClassLevel, classIds);

				String tblData = classTable.printTable();
				request.setAttribute("classTable", tblData);
			}
			
			if (ApplicationProperty.RoomAvailabilityIncludeInstructors.isTrue() && inst.getExternalUniqueId() != null && !inst.getExternalUniqueId().isEmpty() &&
				RoomAvailability.getInstance() != null && RoomAvailability.getInstance() instanceof DefaultRoomAvailabilityService) {
				WebTable.setOrder(sessionContext, "instructorUnavailability.ord", request.getParameter("iuord"), 1);
				WebTable eventTable = new WebTable(5, "Instructor Unavailability", "instructorDetail.do?instructorId=" + frm.getInstructorId() + "&iuord=%%", new String[] {"Event", "Type", "Date", "Time", "Room"}, new String[] {"left", "left", "left", "left", "left"}, null);
				
				Formats.Format<Date> dfShort = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
				Formats.Format<Date> dfLong = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_LONG);
						
				org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
				Map<Event, Set<Meeting>> unavailabilities = new HashMap<Event, Set<Meeting>>();
				for (Meeting meeting: (List<Meeting>)hibSession.createQuery(
						"select distinct m from Event e inner join e.meetings m left outer join e.additionalContacts c, Session s " +
						"where e.class in (CourseEvent, SpecialEvent, UnavailableEvent) and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate " +
						"and s.uniqueId = :sessionId and (e.mainContact.externalUniqueId = :user or c.externalUniqueId = :user) and m.approvalStatus = 1"
						)
						.setLong("sessionId", sessionContext.getUser().getCurrentAcademicSessionId())
						.setString("user", inst.getExternalUniqueId())
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

			frm.setDisplayPrefs(CommonValues.Yes.eq(sessionContext.getUser().getProperty(UserProperty.DispInstructorPrefs)));
			
			if (op.equals(rsc.getMessage("button.displayPrefs")) || "true".equals(request.getParameter("showPrefs"))) { 
				frm.setDisplayPrefs(true);
				sessionContext.getUser().setProperty(UserProperty.DispInstructorPrefs, CommonValues.Yes.value());
			}
			
			if (op.equals(rsc.getMessage("button.hidePrefs")) || "false".equals(request.getParameter("showPrefs"))) {
				frm.setDisplayPrefs(false);
				sessionContext.getUser().setProperty(UserProperty.DispInstructorPrefs, CommonValues.No.value());
			}

			if (frm.isDisplayPrefs()) {
		        // Initialize Preferences for initial load 
		        Set timePatterns = new HashSet();
		        frm.setAvailableTimePatterns(null);
		        initPrefs(frm, inst, null, false);
		        timePatterns.add(new TimePattern(new Long(-1)));
		    	//timePatterns.addAll(TimePattern.findApplicable(request,30,false));
		        
				// Process Preferences Action
				processPrefAction(request, frm, errors);
				
				// Generate Time Pattern Grids
				//super.generateTimePatternGrids(request, frm, inst, timePatterns, "init", timeVertical, false, null);
				for (Preference pref: inst.getPreferences()) {
					if (pref instanceof TimePref) {
						frm.setAvailability(((TimePref)pref).getPreference());
						break;
					}
				}
	
		        LookupTables.setupRooms(request, inst);		 // Room Prefs
		        LookupTables.setupBldgs(request, inst);		 // Building Prefs
		        LookupTables.setupRoomFeatures(request, inst); // Preference Levels
		        LookupTables.setupRoomGroups(request, inst);   // Room Groups
			}
			
			DepartmentalInstructor previous = inst.getPreviousDepartmentalInstructor(sessionContext, Right.InstructorDetail);
			frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());
			DepartmentalInstructor next = inst.getNextDepartmentalInstructor(sessionContext, Right.InstructorDetail);
			frm.setNextId(next==null?null:next.getUniqueId().toString());
		
	        return mapping.findForward("showInstructorDetail");
		
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}

	/**
	 * Loads the non-editable instructor info into the form
	 * @param request
	 * @param frm
	 * @param inst
	 * @param instructorId
	 */
	private void doLoad(HttpServletRequest request, InstructorEditForm frm, DepartmentalInstructor inst, String instructorId) {
        // populate form
		frm.setInstructorId(instructorId);
		
		frm.setName( ((inst.getFirstName() == null) ?"": Constants.toInitialCase(inst.getFirstName(), "-".toCharArray()) )+ " " 
        			+ ((inst.getMiddleName() == null) ?"": Constants.toInitialCase(inst.getMiddleName(), "-".toCharArray()) )+ " " 
        			+ Constants.toInitialCase(inst.getLastName(), "-".toCharArray()));
		
		frm.setEmail(inst.getEmail());
		
		String puid = inst.getExternalUniqueId();
		if (puid != null) {
			frm.setPuId(puid);
		}
				
		if (inst.getPositionType() != null) {
			frm.setPosType(inst.getPositionType().getLabel().trim());
		}
		
		if (inst.getCareerAcct() != null) {
			frm.setCareerAcct(inst.getCareerAcct().trim());
		} else if (DepartmentalInstructor.canLookupInstructor()) {
			try {
				UserInfo user = DepartmentalInstructor.lookupInstructor(puid);
				if (user != null && user.getUserName() != null)
					frm.setCareerAcct(user.getUserName());
			} catch (Exception e) {}
		}
		
		if (inst.getNote() != null) {
			frm.setNote(inst.getNote().trim());
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
        
        frm.setIgnoreDist(inst.isIgnoreToFar()==null?false:inst.isIgnoreToFar().booleanValue());
	}
	
	

}

