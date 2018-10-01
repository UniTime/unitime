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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ClassEditForm;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableDatabaseLoader;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DefaultRoomAvailabilityService;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.util.DefaultRoomAvailabilityService.MeetingTimeBlock;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;


/**
 * MyEclipse Struts
 * Creation date: 03-29-2006
 *
 * XDoclet definition:
 * @struts.action path="/classDetail" name="classEditForm" attribute="ClassEditForm" input="/user/classEdit.jsp" scope="request"
 *
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Service("/classDetail")
public class ClassDetailAction extends PreferencesAction {

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static GwtConstants CONST = Localization.create(GwtConstants.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	
    // --------------------------------------------------------- Class Constants

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

	        super.execute(mapping, form, request, response);

	        ClassEditForm frm = (ClassEditForm) form;
	        ActionMessages errors = new ActionMessages();

	        // Read parameters
	        String classId =  request.getParameter("cid")==null
								? request.getAttribute("cid") !=null
								        ? request.getAttribute("cid").toString()
								        : null
								: request.getParameter("cid");
	        String op = frm.getOp();

	        // Read class id from form
	        if(op.equals(MSG.actionEditClass())
	        		|| op.equals(MSG.actionAddDistributionPreference())
	                // || op.equals(rsc.getMessage("button.backToInstrOffrDet")) for deletion
	                || op.equals(MSG.actionNextClass())
	                || op.equals(MSG.actionPreviousClass())
	                || op.equals(MSG.actionEditClassInstructorAssignmentPreferences())
	                ) {
	            classId = frm.getClassId().toString();
	        } else {
	        	frm.reset(mapping, request);
	        }

	        Debug.debug("op: " + op);
	        Debug.debug("class: " + classId);

	        // Check class exists
	        if(classId==null || classId.trim().length()==0)
	            throw new Exception ("Class Info not supplied.");

	        sessionContext.checkPermission(classId, "Class_", Right.ClassDetail);
	        
	        boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));

	        // If class id is not null - load class info
	        Class_DAO cdao = new Class_DAO();
	        Class_ c = cdao.get(new Long(classId));

	        // Edit Preference - Redirect to prefs edit screen
	        if(op.equals(MSG.actionEditClass())
	                && classId!=null && classId.trim()!="") {
	        	response.sendRedirect( response.encodeURL("classEdit.do?cid=" + c.getUniqueId().toString() + "&sec=" + c.getSectionNumberString() ));
	        	return null;
	        }
	        
	        if (op.equals(MSG.actionEditClassInstructorAssignmentPreferences()) && classId!=null && !classId.isEmpty()) {
	        	response.sendRedirect( response.encodeURL("classInstrAssgnEdit.do?cid=" + c.getUniqueId().toString() + "&sec=" + c.getSectionNumberString() ));
	        	return null;
	        }

            if (op.equals(MSG.actionNextClass())) {
            	response.sendRedirect(response.encodeURL("classDetail.do?cid="+frm.getNextId()));
            	return null;
            }

            if (op.equals(MSG.actionPreviousClass())) {
            	response.sendRedirect(response.encodeURL("classDetail.do?cid="+frm.getPreviousId()));
            	return null;
            }

			// Add Distribution Preference - Redirect to dist prefs screen
		    if(op.equals(MSG.actionAddDistributionPreference())) {
		        SchedulingSubpart ss = c.getSchedulingSubpart();
		        CourseOffering cco = ss.getInstrOfferingConfig().getControllingCourseOffering();
		        request.setAttribute("subjectAreaId", cco.getSubjectArea().getUniqueId().toString());
		        request.setAttribute("schedSubpartId", ss.getUniqueId().toString());
		        request.setAttribute("courseOffrId", cco.getUniqueId().toString());
		        request.setAttribute("classId", c.getUniqueId().toString());
	            return mapping.findForward("addDistributionPrefs");
		    }

	        // Load form attributes that are constant
	        doLoad(request, frm, c, op);

	        // Initialize Preferences for initial load
	        frm.setAvailableTimePatterns(TimePattern.findApplicable(
	        		sessionContext.getUser(),
	        		c.getSchedulingSubpart().getMinutesPerWk(),
	        		c.effectiveDatePattern(),
	        		c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel(),
	        		true,
	        		c.getManagingDept()));
			Set timePatterns = null;
        	initPrefs(frm, c, null, false);
		    timePatterns = c.effectiveTimePatterns();

		    // Display distribution Prefs
	        DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
	        String html = tbl.getDistPrefsTableForClass(request, sessionContext, c);
	        if (html!=null)
	        	request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);

			// Process Preferences Action
			processPrefAction(request, frm, errors);

	        // Generate Time Pattern Grids
			super.generateTimePatternGrids(request, frm, c, 
					c.getSchedulingSubpart().getMinutesPerWk(),
	        		c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel(),
	        		c.effectiveDatePattern(),
	        		timePatterns, "init", timeVertical, false, null);

			// Instructors
	        setupInstructors(request, frm, c);
	        
	        // date Patterns
	        setupDatePatterns(request, frm, c);

	        LookupTables.setupDatePatterns(request, sessionContext.getUser(), MSG.dropDefaultDatePattern(), c.getSchedulingSubpart().effectiveDatePattern(), c.getManagingDept(), c.effectiveDatePattern());

	        LookupTables.setupRooms(request, c);		 // Room Prefs
	        LookupTables.setupBldgs(request, c);		 // Building Prefs
	        LookupTables.setupRoomFeatures(request, c); // Preference Levels
	        LookupTables.setupRoomGroups(request, c);   // Room Groups
	        LookupTables.setupInstructorAttributes(request, c);   // Instructor Attributes
	        LookupTables.setupInstructors(request, sessionContext, c.getDepartmentForSubjectArea().getUniqueId());
	        
	        try {
	            if (RoomAvailability.getInstance()!=null && !(RoomAvailability.getInstance() instanceof DefaultRoomAvailabilityService)) {
	                Session session = c.getManagingDept().getSession();
	                Date[] bounds = DatePattern.getBounds(session.getUniqueId());
	                RoomAvailability.getInstance().activate(session,bounds[0],bounds[1],RoomAvailabilityInterface.sClassType, false);
	                RoomAvailability.setAvailabilityWarning(request, session, true, true);
	            }

	            ClassAssignmentProxy proxy = classAssignmentService.getAssignment();
	            if (proxy != null ) {
	            	    Assignment a = proxy.getAssignment(c);
	            	    if (a != null && a.getDatePattern() != null) {
	            	    	  if (!a.getDatePattern().equals(c.effectiveDatePattern()) ) {    		  
	            	    		if (a.getDatePattern().getParents() == null || !a.getDatePattern().getParents().contains(c.effectiveDatePattern())) {
	            	    		  errors.add("datePatternChanged", 
	            	                       new ActionMessage(
	            	                               "errors.generic", 
	            	                               MSG.datePatternCommittedIsDifferent(c.getClassLabel(), a.getDatePattern().getName(), c.effectiveDatePattern().getName())));
	            	            saveErrors(request, errors);
	            	    			 }
	            	    	  }
	            	    }
	            }
	        	Set<Assignment> conflicts = (proxy == null ? null : proxy.getConflicts(c.getUniqueId()));
	        	if (conflicts != null && !conflicts.isEmpty()) {
	        		TreeSet<Assignment> orderedConflicts = new TreeSet<Assignment>(new Comparator<Assignment>() {
	        			ClassComparator cc = new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY);
						@Override
						public int compare(Assignment a1, Assignment a2) {
							return cc.compare(a1.getClazz(), a2.getClazz());
						}
					});
	        		orderedConflicts.addAll(conflicts);
	        		WebTable.setOrder(sessionContext,"classDetail.conflictsOrd",request.getParameter("conflicts"),1);
	        		final Set<DepartmentalInstructor> conflictingInstructors = new HashSet<DepartmentalInstructor>();
	        		WebTable table = new WebTable(8, MSG.sectionTitleClassConflicts(), "classDetail.do?cid=" + c.getUniqueId() + "&conflicts=%%", new String[] {
        					MSG.columnClass(),
        					MSG.columnExternalId(),
        					MSG.columnDemand(),
        					MSG.columnSnapshotLimit(),
        					MSG.columnInstructor(),
        					MSG.columnDatePattern(),
        					MSG.columnAssignedTime(),
        					MSG.columnAssignedRoom(),
        					MSG.columnEnrollmentConflict()
	        			}, new String[] {
	        				"left",
	        				"left",
	        				"right",
	        				"right",
	        				"left",
	        				"left",
	        				"left",
	        				"left",
	        				"left"
	        			}, new boolean[] {
	        				true,
	        				true,
	        				true,
	        				true,
	        				true,
	        				true,
	        				true,
	        				true,
	        				true
	        		}) {
	        			@Override
	        			protected boolean isFiltered(int col) {
	        				if (col == 7) return conflictingInstructors.isEmpty();
	        				return false;
	        			}
	        		};
	        		String nameFormat = UserProperty.NameFormat.get(sessionContext.getUser());
	        		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
	        		for (Assignment assignment: orderedConflicts) {
	        			String suffix = assignment.getClazz().getClassSuffix();
	        			DatePattern dp = assignment.getDatePattern();
	        			TimeLocation t = assignment.getTimeLocation();
	        			String time = "";
	        			if (t != null) {
	        				Integer firstDay = ApplicationProperty.TimePatternFirstDayOfWeek.intValue();
	            			for (int i = 0; i < CONST.shortDays().length; i++) {
	            				int idx = (firstDay == null ? i : (i + firstDay) % 7);
	            				if ((Constants.DAY_CODES[idx] & t.getDayCode()) != 0) time += CONST.shortDays()[idx];
	            			}
	           				time += " " + t.getStartTimeHeader(CONST.useAmPm()) + "-" + t.getEndTimeHeader(CONST.useAmPm());
	        			}
	        			String roomsHtml = "", roomsText = "";
	        			for (Location r: assignment.getRooms()) {
	        				if (!roomsHtml.isEmpty()) { roomsHtml += ", "; roomsText += ", "; }
	        				roomsHtml += "<span onmouseover=\"showGwtRoomHint(this, '" + r.getUniqueId() + "', '');\" onmouseout=\"hideGwtRoomHint();\">" + r.getLabel() + "</span>";
	        				roomsText += r.getLabel();
	        			}
	        			String enrolledHtml = "", enrolledText = "";
	        			for (ClassInstructor instructor: c.getClassInstructors()) {
	        				if (!instructor.isLead() || instructor.getInstructor().getExternalUniqueId() == null) continue;
		        			for (StudentClassEnrollment e: assignment.getClazz().getStudentEnrollments()) {
		        				if (instructor.getInstructor().getExternalUniqueId().equals(e.getStudent().getExternalUniqueId())) {
		        					enrolledHtml += (enrolledHtml.isEmpty() ? "" : "<br>") + e.getStudent().getName(nameFormat);
		        					enrolledText += (enrolledText.isEmpty() ? "" : ", ") + e.getStudent().getName(nameFormat);
		        					conflictingInstructors.add(instructor.getInstructor());
		        				}
		        			}
	        			}
	        			table.addLine(
	        					sessionContext.hasPermission(assignment.getClazz(), Right.ClassDetail) ? "onClick=\"document.location='classDetail.do?cid=" + assignment.getClassId() + "';\"": null,
	        					new String[] {
	        							assignment.getClazz().getClassLabel(),
	        							suffix == null ? "&nbsp;" : suffix,
	        							assignment.getClazz().getEnrollment().toString(),
	        							assignment.getClazz().getSnapshotLimit() == null ? "&nbsp;" : assignment.getClazz().getSnapshotLimit().toString(),
	        							assignment.getClazz().instructorHtml(nameFormat),
	        							dp == null ? "&nbsp;" : "<span title='" + dateFormat.format(dp.getStartDate()) + " - " + dateFormat.format(dp.getEndDate()) + "'>" + dp.getName() + "</span>",
	        							time,
	        							roomsHtml,
	        							enrolledHtml
	        					}, new Comparable[] {
	        							assignment.getClazz().getClassLabel(),
	        							suffix == null ? "" : suffix,
	        							assignment.getClazz().getEnrollment(),
	        							assignment.getClazz().getSnapshotLimit() == null ? new Integer(0) :assignment.getClazz().getSnapshotLimit(),
	        							assignment.getClazz().instructorText(nameFormat, ","),
	        							dp == null ? "" : dp.getName(),
	        							new MultiComparable(t == null ? 0 : t.getDayCode(), t == null ? 0 : t.getStartSlot()),
	        							roomsText,
	        							enrolledText
								});
	        		}
	        		request.setAttribute("CLASS_CONFLICTS", table.printTable(WebTable.getOrder(sessionContext,"classDetail.conflictsOrd")));
	        	}
	        	
	        	Set<TimeBlock> ec = (proxy == null ? null : proxy.getConflictingTimeBlocks(c.getUniqueId()));
	        	if (ec != null && !ec.isEmpty()) {
	        		WebTable table = new WebTable(4, MSG.sectionTitleEventConflicts(), new String[] {
        					MSG.columnEventName(),
        					MSG.columnEventType(),
        					MSG.columnEventDate(),
        					MSG.columnEventTime()
	        			}, new String[] {
	        				"left",
	        				"left",
	        				"left",
	        				"left"
	        			}, new boolean[] {
	        				true,
	        				true,
	        				true,
	        				true
	        		});
	        		table.setBlankWhenSame(true);
	        		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_MEETING);
	        		Formats.Format<Date> timeFormat = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
	        		for (TimeBlock block: ec) {
	        			String link = null;
	        			if (block instanceof MeetingTimeBlock) {
	        				MeetingTimeBlock mtb = (MeetingTimeBlock)block;
	        				if (mtb.getEventId() != null && sessionContext.hasPermission(mtb.getEventId(), Right.EventDetail))
	        					link = "onClick=\"showGwtDialog('Event Detail', 'gwt.jsp?page=events&menu=hide#event=" + mtb.getEventId() + "','900','85%');\"";
	        			}
	        			table.addLine(
	        					link,
	        					new String[] {
	        							block.getEventName(),
	        							block.getEventType(),
	        							dateFormat.format(block.getStartTime()),
	        							timeFormat.format(block.getStartTime()) + " - " + timeFormat.format(block.getEndTime())
	        					}, new Comparable[] {
	        							block.getEventName(),
	        							block.getEventType(),
	        							block.getStartTime(),
	        							block.getEndTime()
								});
	        		}
	        		request.setAttribute("EVENT_CONFLICTS", table.printTable());
	        	}
	        } catch (Exception e) {
	        	Debug.error("Failed to compute conflicts: " + e.getMessage(), e);
	        }

	        BackTracker.markForBack(
	        		request,
	        		"classDetail.do?cid="+frm.getClassId(),
	        		MSG.backClass(frm.getClassName()),
	        		true, false);
	        
	        return mapping.findForward("displayClass");

	    	} catch (Exception e) {
	    		Debug.error(e);
	    		throw e;
	    	}
	    }

	    /**
	     * Loads class info into the form
	     * @param request
	     * @param frm
	     * @param c
	     * @param classId
	     */
	    private void doLoad(
	    		HttpServletRequest request,
	            ClassEditForm frm,
	            Class_ c,
	            String op) {

	        String parentClassName = "-";
	        Long parentClassId = null;
	        if (c.getParentClass()!=null) {
	            parentClassName = c.getParentClass().toString();
	            if (sessionContext.hasPermission(c.getParentClass(), Right.ClassDetail))
	            	parentClassId = c.getParentClass().getUniqueId();
	        }

	        CourseOffering cco = c.getSchedulingSubpart().getControllingCourseOffering();

		    // Set Session Variables
	        InstructionalOfferingSearchAction.setLastInstructionalOffering(sessionContext, c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering());

	        // populate form
	        frm.setClassId(c.getUniqueId());
	        frm.setSection(c.getSectionNumberString());
	        frm.setClassName(c.getClassLabel());

	        SchedulingSubpart ss = c.getSchedulingSubpart();
	    	String itypeDesc = c.getItypeDesc();
	    	if (ss.getInstrOfferingConfig().getInstructionalOffering().hasMultipleConfigurations())
	    		itypeDesc += " [" + ss.getInstrOfferingConfig().getName() + "]";
	        frm.setItypeDesc(itypeDesc);

	        frm.setParentClassName(parentClassName);
	        frm.setParentClassId(parentClassId);
	        frm.setSubjectAreaId(cco.getSubjectArea().getUniqueId().toString());
	        frm.setInstrOfferingId(cco.getInstructionalOffering().getUniqueId().toString());
	        if (sessionContext.hasPermission(c.getSchedulingSubpart(), Right.SchedulingSubpartDetail))
	        	frm.setSubpart(c.getSchedulingSubpart().getUniqueId());
	        else
	        	frm.setSubpart(null);
	        frm.setCourseName(cco.getInstructionalOffering().getCourseName());
	        frm.setCourseTitle(cco.getTitle());
	        frm.setIsCrosslisted(new Boolean(cco.getInstructionalOffering().getCourseOfferings().size()>1));
	        frm.setAccommodation(StudentAccomodation.toHtml(StudentAccomodation.getAccommodations(c)));
	        frm.setIsCancelled(c.isCancelled());
	        
	        // Load from class
		    frm.setExpectedCapacity(c.getExpectedCapacity());
		    frm.setEnrollment(c.getEnrollment());
		    frm.setSnapshotLimit(c.getSnapshotLimit());
	        frm.setDatePattern(c.getDatePattern()==null?new Long(-1):c.getDatePattern().getUniqueId());
		    frm.setNbrRooms(c.getNbrRooms());
		    if (c.getNotes()==null)
		    	frm.setNotes("");
		    else
		    	frm.setNotes(c.getNotes().replaceAll("\n","<BR>"));
		    frm.setManagingDept(c.getManagingDept().getUniqueId());
		    frm.setControllingDept(c.getControllingDept().getUniqueId());
		    frm.setManagingDeptLabel(c.getManagingDept().getManagingDeptLabel());
		    frm.setSchedulePrintNote(c.getSchedulePrintNote());
		    frm.setClassSuffix(c.getDivSecNumber());
		    frm.setMaxExpectedCapacity(c.getMaxExpectedCapacity());
		    frm.setRoomRatio(c.getRoomRatio());
		    frm.setEnabledForStudentScheduling(c.isEnabledForStudentScheduling());
		    frm.setDisplayInstructor(c.isDisplayInstructor());
	        frm.setMinRoomLimit(c.getMinRoomLimit());

	        Class_ next = c.getNextClass(sessionContext, Right.ClassDetail);
	        frm.setNextId(next==null?null:next.getUniqueId().toString());
	        Class_ previous = c.getPreviousClass(sessionContext, Right.ClassDetail);
	        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());

		    List instructors = new ArrayList(c.getClassInstructors());
		    Collections.sort(instructors, new InstructorComparator());

		    for(Iterator iter = instructors.iterator(); iter.hasNext(); ) {
		    	ClassInstructor classInstr = (ClassInstructor) iter.next();
		        frm.addToInstructors(classInstr);
		    }

	        if (c.getNbrRooms().intValue()>0) {
	        	List<RoomLocation> roomLocations = TimetableDatabaseLoader.computeRoomLocations(c);
	        	StringBuffer rooms = new StringBuffer();
	        	if (roomLocations.isEmpty()) {
	        		request.setAttribute(Location.AVAILABLE_LOCATIONS_ATTR,
	        				"<font color='red'><b>No rooms are available.</b></font>");
	        	} else {
	        		int idx = 0;
	        		for (RoomLocation rl: roomLocations) {
	        			if (idx>0) rooms.append(", ");
	    				if (idx==4)
	    					rooms.append("<span id='room_dots' onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" style='display:inline'><a onClick=\"document.getElementById('room_dots').style.display='none';document.getElementById('room_rest').style.display='inline';\">...</a></span><span id='room_rest' style='display:none'>");
	        			rooms.append(
	        					"<span style='color:"+PreferenceLevel.int2color(rl.getPreference())+";' " +
	        					"onmouseover=\"showGwtRoomHint(this, '" + rl.getId() + "', '" + PreferenceLevel.int2string(rl.getPreference()) + "');\" onmouseout=\"hideGwtRoomHint();\">"+
	        					rl.getName()+
	        					"</span>");
	        			idx++;
	        		}
	        		if (idx>=4) rooms.append("</span>");
		        	if (roomLocations.size()<c.getNbrRooms().intValue()) {
		        		request.setAttribute(Location.AVAILABLE_LOCATIONS_ATTR,
		        				"<font color='red'><b>Not enough rooms are available:</b></font> "+rooms);
		        	} else {
		        		request.setAttribute(Location.AVAILABLE_LOCATIONS_ATTR,
		        				roomLocations.size()+" ("+rooms+")");
		        	}
	        	}
	        }
	    }

	    /**
	     * Set up instructor lists
	     * @param request
	     * @param frm
	     * @param errors
	     */
	    protected void setupInstructors(
	            HttpServletRequest request,
	            ClassEditForm frm,
	            Class_ c ) throws Exception {

	        List instructors = frm.getInstructors();
	        if(instructors.size()==0)
	            return;

	        // Get dept instructor list
	        LookupTables.setupInstructors(request, sessionContext, c.getDepartmentForSubjectArea().getUniqueId());
	        LookupTables.setupInstructorTeachingResponsibilities(request);	        
	        Vector deptInstrList = (Vector) request.getAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME);

	        // For each instructor set the instructor list
	        for (int i=0; i<instructors.size(); i++) {
    	        request.setAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME + i, deptInstrList);
	        }
	    }
	        	    
	    private void setupDatePatterns(HttpServletRequest request, ClassEditForm frm, Class_ c) throws Exception {	    	  
	    	DatePattern selectedDatePattern = c.effectiveDatePattern();			
			if (selectedDatePattern != null) {
				List<DatePattern> children = selectedDatePattern.findChildren();
				for (DatePattern dp: children) {
					if (!frm.getDatePatternPrefs().contains(
							dp.getUniqueId().toString())) {
						frm.addToDatePatternPrefs(dp.getUniqueId()
								.toString(), PreferenceLevel.PREF_LEVEL_NEUTRAL);
					}
				}
				frm.sortDatePatternPrefs(frm.getDatePatternPrefs(), frm.getDatePatternPrefLevels(), children);
			}			
		}

}

