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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
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
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy.AssignmentInfo;
import org.unitime.timetable.solver.TimetableDatabaseLoader;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DefaultRoomAvailabilityService;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.util.DefaultRoomAvailabilityService.MeetingTimeBlock;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;


/**
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Action(value = "classDetail", results = {
		@Result(name = "displayClass", type = "tiles", location = "classDetail.tiles"),
		@Result(name = "instructionalOfferingSearch", type = "redirect", location = "/instructionalOfferingSearch.action"),
		@Result(name = "addDistributionPrefs", type = "redirect", location = "/distributionPrefs.action", 
			params = { "classId", "${form.classId}", "op", "${op}"}
		),
	})
@TilesDefinition(name = "classDetail.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Class Detail"),
		@TilesPutAttribute(name = "body", value = "/user/classDetail.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
	})
public class ClassDetailAction extends PreferencesAction2<ClassEditForm> {
	private static final long serialVersionUID = -2989783006446330571L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static GwtConstants CONST = Localization.create(GwtConstants.class);

	protected String classId = null;
	protected String op2 = null;

	public String getCid() { return classId; }
	public void setCid(String classId) { this.classId = classId; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }	
	
	public String execute() throws Exception {
    	if (ApplicationProperty.LegacyClassDetail.isFalse()) {
    		String url = "clazz";
    		boolean first = true;
    		for (Enumeration<String> e = getRequest().getParameterNames(); e.hasMoreElements(); ) {
    			String param = e.nextElement();
    			url += (first ? "?" : "&") + param + "=" + URLEncoder.encode(getRequest().getParameter(param), "utf-8");
    			first = false;
    		}
    		response.sendRedirect(url);
			return null;
    	}

		if (form == null) form = new ClassEditForm();
		
		super.execute();
		
		if (classId == null && request.getAttribute("cid") != null)
			classId = (String)request.getAttribute("cid");
		
		if (op == null) op = form.getOp();
        if (op2 != null && !op2.isEmpty()) op = op2;
        
        // Read class id from form
        if (MSG.actionEditClass().equals(op)
        		|| MSG.actionAddDistributionPreference().equals(op)
                || MSG.actionNextClass().equals(op)
                || MSG.actionPreviousClass().equals(op)
                || MSG.actionEditClassInstructorAssignmentPreferences().equals(op)
                ) {
        	classId = form.getClassId().toString();
        } else {
        	form.reset();
        }
        
        Debug.debug("op: " + op);
	    Debug.debug("class: " + classId);
	    
	    // Check class exists
	    if (classId==null || classId.trim().isEmpty())
	    	throw new Exception(MSG.errorClassInfoNotSupplied());

	    sessionContext.checkPermission(classId, "Class_", Right.ClassDetail);

	    boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));

	    // If class id is not null - load class info
        Class_DAO cdao = new Class_DAO();
        Class_ c = cdao.get(Long.valueOf(classId));

        // Edit Preference - Redirect to prefs edit screen
        if (MSG.actionEditClass().equals(op)) {
        	response.sendRedirect( response.encodeURL("classEdit.action?cid=" + c.getUniqueId().toString() + "&sec=" + c.getSectionNumberString() ));
        	return null;
        }
        
        if (MSG.actionEditClassInstructorAssignmentPreferences().equals(op)) {
        	response.sendRedirect( response.encodeURL("classInstrAssgnEdit.do?cid=" + c.getUniqueId().toString() + "&sec=" + c.getSectionNumberString() ));
        	return null;
        }
        
        if (MSG.actionNextClass().equals(op)) {
        	response.sendRedirect(response.encodeURL("classDetail.action?cid="+form.getNextId()));
        	return null;
        }
        
        if (MSG.actionPreviousClass().equals(op)) {
        	response.sendRedirect(response.encodeURL("classDetail.action?cid="+form.getPreviousId()));
        	return null;
        }
        
        // Add Distribution Preference - Redirect to dist prefs screen
        if (MSG.actionAddDistributionPreference().equals(op)) {
            return "addDistributionPrefs";
	    }
        
        // Load form attributes that are constant
        doLoad(c, op);

        // Initialize Preferences for initial load
        form.setAvailableTimePatterns(TimePattern.findApplicable(
        		sessionContext.getUser(),
        		c.getSchedulingSubpart().getMinutesPerWk(),
        		c.effectiveDatePattern(),
        		c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel(),
        		true,
        		c.getManagingDept()));
		Set timePatterns = null;
    	initPrefs(c, null, false);
	    timePatterns = c.effectiveTimePatterns();

	    // Display distribution Prefs
        DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
        String html = tbl.getDistPrefsTableForClass(request, sessionContext, c);
        if (html!=null)
        	request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);

		// Process Preferences Action
		processPrefAction();

        // Generate Time Pattern Grids
		super.generateTimePatternGrids(c, 
				c.getSchedulingSubpart().getMinutesPerWk(),
        		c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel(),
        		c.effectiveDatePattern(),
        		timePatterns, "init", timeVertical, false, null);

		// Instructors
        setupInstructors(request, form, c);
        
        // date Patterns
        setupDatePatterns(request, form, c);

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
                RoomAvailability.getInstance().activate(session.getUniqueId(),bounds[0],bounds[1],RoomAvailabilityInterface.sClassType, false);
                RoomAvailability.setAvailabilityWarning(request, session, true, true);
            }

            ClassAssignmentProxy proxy = getClassAssignmentService().getAssignment();
            if (proxy != null) {
            	AssignmentInfo a = proxy.getAssignment(c);
            	if (a != null && a.isCommitted() && a.getDatePattern() != null) {
            		if (!a.getDatePattern().equals(c.effectiveDatePattern())) {
            			if (a.getDatePattern().getParents() == null || !a.getDatePattern().getParents().contains(c.effectiveDatePattern())) {
            				addFieldError("datePatternChanged", MSG.datePatternCommittedIsDifferent(c.getClassLabel(), a.getDatePattern().getName(), c.effectiveDatePattern().getName()));
            			}
            		}
            	}
            }
            
        	Set<AssignmentInfo> conflicts = (proxy == null ? null : proxy.getConflicts(c.getUniqueId()));
        	if (conflicts != null && !conflicts.isEmpty()) {
        		TreeSet<AssignmentInfo> orderedConflicts = new TreeSet<AssignmentInfo>(new Comparator<AssignmentInfo>() {
        			ClassComparator cc = new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY);
					@Override
					public int compare(AssignmentInfo a1, AssignmentInfo a2) {
						return cc.compare(a1.getClazz(), a2.getClazz());
					}
				});
        		orderedConflicts.addAll(conflicts);
        		WebTable.setOrder(sessionContext,"classDetail.conflictsOrd",request.getParameter("conflicts"),1);
        		final Set<DepartmentalInstructor> conflictingInstructors = new HashSet<DepartmentalInstructor>();
        		WebTable table = new WebTable(8, MSG.sectionTitleClassConflicts(), "classDetail.action?cid=" + c.getUniqueId() + "&conflicts=%%", new String[] {
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
        		for (AssignmentInfo assignment: orderedConflicts) {
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
        					sessionContext.hasPermission(assignment.getClazz(), Right.ClassDetail) ? "onClick=\"document.location='classDetail.action?cid=" + assignment.getClassId() + "';\"": null,
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
        							assignment.getClazz().getSnapshotLimit() == null ? Integer.valueOf(0) :assignment.getClazz().getSnapshotLimit(),
        							assignment.getClazz().instructorText(nameFormat, ","),
        							dp == null ? "" : dp.getName(),
        							new MultiComparable(t == null ? 0 : t.getDayCode(), t == null ? 0 : t.getStartSlot()),
        							roomsText,
        							enrolledText
							});
        		}
        		request.setAttribute("classConflicts", table.printTable(WebTable.getOrder(sessionContext,"classDetail.conflictsOrd")));
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
        					link = "onClick=\"showGwtDialog('Event Detail', 'events?menu=hide#event=" + mtb.getEventId() + "','900','85%');\"";
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
        		request.setAttribute("eventConflicts", table.printTable());
        	}
        } catch (Exception e) {
        	Debug.error("Failed to compute conflicts: " + e.getMessage(), e);
        }

        BackTracker.markForBack(
        		request,
        		"classDetail.action?cid="+form.getClassId(),
        		MSG.backClass(form.getClassName()),
        		true, false);
        
        return "displayClass";
	}

    /**
     * Loads class info into the form
     */
    private void doLoad(Class_ c, String op) {
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
        form.setClassId(c.getUniqueId());
        form.setSection(c.getSectionNumberString());
        form.setClassName(c.getClassLabel());

        SchedulingSubpart ss = c.getSchedulingSubpart();
    	String itypeDesc = c.getItypeDesc();
    	if (ss.getInstrOfferingConfig().getInstructionalOffering().hasMultipleConfigurations())
    		itypeDesc += " [" + ss.getInstrOfferingConfig().getName() + "]";
        form.setItypeDesc(itypeDesc);

        form.setParentClassName(parentClassName);
        form.setParentClassId(parentClassId);
        form.setSubjectAreaId(cco.getSubjectArea().getUniqueId().toString());
        form.setInstrOfferingId(cco.getInstructionalOffering().getUniqueId().toString());
        if (sessionContext.hasPermission(c.getSchedulingSubpart(), Right.SchedulingSubpartDetail))
        	form.setSubpart(c.getSchedulingSubpart().getUniqueId());
        else
        	form.setSubpart(null);
        form.setCourseName(cco.getInstructionalOffering().getCourseName());
        form.setCourseTitle(cco.getTitle());
        form.setIsCrosslisted(Boolean.valueOf(cco.getInstructionalOffering().getCourseOfferings().size()>1));
        form.setAccommodation(StudentAccomodation.toHtml(StudentAccomodation.getAccommodations(c)));
        form.setIsCancelled(c.isCancelled());
        
        // Load from class
	    form.setExpectedCapacity(c.getExpectedCapacity());
	    form.setEnrollment(c.getEnrollment());
	    form.setSnapshotLimit(c.getSnapshotLimit());
        form.setDatePattern(c.getDatePattern()==null?Long.valueOf(-1):c.getDatePattern().getUniqueId());
        form.setDatePatternEditable(ApplicationProperty.WaitListCanChangeDatePattern.isTrue() || c.getEnrollment() == 0 || !c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().effectiveReScheduleNow());
        form.setLms(c.getLms() == null? "" : c.getLms().getLabel());
        if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
        	form.setFundingDept(c.getEffectiveFundingDept().getLabel());
        } else {
        	form.setFundingDept("");
        }
	    form.setNbrRooms(c.getNbrRooms());
	    form.setSplitAttendance(c.isRoomsSplitAttendance());
	    if (c.getNotes()==null)
	    	form.setNotes("");
	    else
	    	form.setNotes(c.getNotes());
	    form.setManagingDept(c.getManagingDept().getUniqueId());
	    form.setControllingDept(c.getControllingDept().getUniqueId());
	    form.setManagingDeptLabel(c.getManagingDept().getManagingDeptLabel());
	    form.setSchedulePrintNote(c.getSchedulePrintNote());
	    form.setClassSuffix(c.getDivSecNumber());
	    form.setMaxExpectedCapacity(c.getMaxExpectedCapacity());
	    form.setRoomRatio(c.getRoomRatio());
	    form.setEnabledForStudentScheduling(c.isEnabledForStudentScheduling());
	    form.setDisplayInstructor(c.isDisplayInstructor());
        form.setMinRoomLimit(c.getMinRoomLimit());

        Class_ next = c.getNextClass(sessionContext, Right.ClassDetail);
        form.setNextId(next==null?null:next.getUniqueId().toString());
        Class_ previous = c.getPreviousClass(sessionContext, Right.ClassDetail);
        form.setPreviousId(previous==null?null:previous.getUniqueId().toString());

	    List instructors = new ArrayList(c.getClassInstructors());
	    Collections.sort(instructors, new InstructorComparator(sessionContext));

	    for(Iterator iter = instructors.iterator(); iter.hasNext(); ) {
	    	ClassInstructor classInstr = (ClassInstructor) iter.next();
	        form.addToInstructors(classInstr);
	    }

        if (c.getNbrRooms()>0) {
    		if (c.hasRoomIndexedPrefs()) {
				StringBuffer rooms = new StringBuffer();
				rooms.append("<table width='100%'>");
    			for (int roomIndex = 0; roomIndex < c.getNbrRooms(); roomIndex++) {
    				rooms.append("<tr><td style='width:50px;' nowrap>" + MSG.itemOnlyRoom(1 + roomIndex) + ":</td><td>");
    				List<RoomLocation> roomLocations = TimetableDatabaseLoader.computeRoomLocations(c, roomIndex);
            		if (roomLocations.isEmpty()) {
            			rooms.append("<font color='red'><b>" + MSG.warnNoRoomsAreAvaliable() + "</b></font>");
            		} else {
        				int idx = 0;
                		for (RoomLocation rl: roomLocations) {
                			if (idx>0) rooms.append(", ");
            				if (idx==6)
            					rooms.append("<span id='room_dots_"+roomIndex+"' onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" style='display:inline'><a onClick=\"document.getElementById('room_dots_"+roomIndex+"').style.display='none';document.getElementById('room_rest_"+roomIndex+"').style.display='inline';\">" + MSG.moreAvailableRooms(roomLocations.size() - 6) + "</a></span><span id='room_rest_"+roomIndex+"' style='display:none'>");
                			rooms.append(
                					"<span style='color:"+PreferenceLevel.int2color(rl.getPreference())+";' " +
                					"onmouseover=\"showGwtRoomHint(this, '" + rl.getId() + "', '" + PreferenceLevel.int2string(rl.getPreference()) + "');\" onmouseout=\"hideGwtRoomHint();\">"+
                					rl.getName()+
                					"</span>");
                			idx++;
                		}
                		if (idx>=6) rooms.append("</span>");
            		}
            		rooms.append("</td></tr>");
    			}
    			rooms.append("</table>");
    			request.setAttribute(Location.AVAILABLE_LOCATIONS_ATTR, rooms.toString());
    		} else {
            	List<RoomLocation> roomLocations = TimetableDatabaseLoader.computeRoomLocations(c);
            	StringBuffer rooms = new StringBuffer();
            	if (roomLocations.isEmpty()) {
            		request.setAttribute(Location.AVAILABLE_LOCATIONS_ATTR, "<font color='red'><b>" + MSG.warnNoRoomsAreAvaliable() + "</b></font>");
            	} else {
            		int idx = 0;
            		for (RoomLocation rl: roomLocations) {
            			if (idx>0) rooms.append(", ");
        				if (idx==6)
        					rooms.append("<span id='room_dots' onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" style='display:inline'><a onClick=\"document.getElementById('room_dots').style.display='none';document.getElementById('room_rest').style.display='inline';\">" + MSG.moreAvailableRooms(roomLocations.size() - 6) + "</a></span><span id='room_rest' style='display:none'>");
            			rooms.append(
            					"<span style='color:"+PreferenceLevel.int2color(rl.getPreference())+";' " +
            					"onmouseover=\"showGwtRoomHint(this, '" + rl.getId() + "', '" + PreferenceLevel.int2string(rl.getPreference()) + "');\" onmouseout=\"hideGwtRoomHint();\">"+
            					rl.getName()+
            					"</span>");
            			idx++;
            		}
            		if (idx>=6) rooms.append("</span>");
    	        	if (roomLocations.size()<c.getNbrRooms().intValue()) {
    	        		request.setAttribute(Location.AVAILABLE_LOCATIONS_ATTR, "<font color='red'><b>" + MSG.warnNotEnoughtRoomsAreAvaliable() + "</b></font> "+rooms);
    	        	} else {
    	        		request.setAttribute(Location.AVAILABLE_LOCATIONS_ATTR, rooms);
    	        	}
            	}
    		}
        }
    }

    /**
     * Set up instructor lists
     * @param request
     * @param form
     * @param errors
     */
    protected void setupInstructors(
            HttpServletRequest request,
            ClassEditForm form,
            Class_ c ) throws Exception {

        List instructors = form.getInstructors();
        if(instructors.size()==0)
            return;

        // Get dept instructor list
        LookupTables.setupInstructors(request, sessionContext, c.getDepartmentForSubjectArea().getUniqueId());
        LookupTables.setupInstructorTeachingResponsibilities(request);	        
    }
        	    
    private void setupDatePatterns(HttpServletRequest request, ClassEditForm form, Class_ c) throws Exception {	    	  
    	DatePattern selectedDatePattern = c.effectiveDatePattern();			
		if (selectedDatePattern != null) {
			List<DatePattern> children = selectedDatePattern.findChildren();
			for (DatePattern dp: children) {
				if (!form.getDatePatternPrefs().contains(
						dp.getUniqueId().toString())) {
					form.addToDatePatternPrefs(dp.getUniqueId()
							.toString(), PreferenceLevel.PREF_LEVEL_NEUTRAL);
				}
			}
			form.sortDatePatternPrefs(form.getDatePatternPrefs(), form.getDatePatternPrefLevels(), children);
		}			
	}
}

