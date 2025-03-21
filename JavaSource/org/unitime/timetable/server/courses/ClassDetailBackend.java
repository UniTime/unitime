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
package org.unitime.timetable.server.courses;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassDetailReponse;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassDetailRequest;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.StudentAccomodation.AccommodationCounter;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableDatabaseLoader;
import org.unitime.timetable.solver.ClassAssignmentProxy.AssignmentInfo;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails.InstructorInfo;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails.RoomInfo;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.DefaultRoomAvailabilityService.MeetingTimeBlock;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.JavascriptFunctions;
import org.unitime.timetable.webutil.BackTracker.BackItem;

@GwtRpcImplements(ClassDetailRequest.class)
public class ClassDetailBackend implements GwtRpcImplementation<ClassDetailRequest, ClassDetailReponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static final GwtMessages GWT = Localization.create(GwtMessages.class);
	protected final static GwtConstants CONST = Localization.create(GwtConstants.class);
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	@Override
	public ClassDetailReponse execute(ClassDetailRequest request, SessionContext context) {
		org.hibernate.Session hibSession = SchedulingSubpartDAO.getInstance().getSession();
		Class_ clazz = Class_DAO.getInstance().get(request.getClassId(), hibSession);
		context.checkPermission(clazz, Right.ClassDetail);
		
		if (request.getAction() == null) {
	        BackTracker.markForBack(context,
	        		"clazz?id=" + request.getClassId(),
	        		MSG.backClass(clazz.getClassLabel()),
	        		true, false);
		} else {
			switch(request.getAction()) {
			}
		}

		ClassDetailReponse response = new ClassDetailReponse();
		response.setConfirms(JavascriptFunctions.isJsConfirm(context));
		
		SchedulingSubpart ss = clazz.getSchedulingSubpart();
		InstrOfferingConfig ioc = ss.getInstrOfferingConfig();
		InstructionalOffering io = ioc.getInstructionalOffering();
		
		response.setClassId(clazz.getUniqueId());
		response.setClassName(clazz.getSectionNumberString()); 
		response.setSubpartId(ss.getUniqueId());
		String label = ss.getItype().getAbbv();
		SchedulingSubpart parent = ss.getParentSubpart();
		while (parent != null) {
			label = parent.getItype().getAbbv() + " - " + label;
			parent = parent.getParentSubpart();
		}
        if (io.hasMultipleConfigurations())
        	label += " [" + ioc.getName() + "]";
		response.setSubparName(label);
		response.setOfferingId(ss.getInstrOfferingConfig().getInstructionalOffering().getUniqueId());
		response.setCourseName(ss.getCourseNameWithTitle());
		
        Class_ next = clazz.getNextClass(context, Right.ClassDetail); 
        response.setNextId(next==null ? null : next.getUniqueId());
        Class_ previous = clazz.getPreviousClass(context, Right.ClassDetail); 
        response.setPreviousId(previous == null ? null : previous.getUniqueId());
        
        if (clazz.isCancelled())
        	response.addProperty("").setText(MSG.classNoteCancelled(clazz.getClassLabel()))
        		.setColor("red").addStyle("font-weight: bold;");
		
		response.addProperty(MSG.filterManager()).add(clazz.getManagingDept().getManagingDeptLabel());
        if (clazz.getParentClass() != null) {
        	CellInterface c = response.addProperty(MSG.propertyParentClass()).setText(clazz.getParentClass().getClassLabel());
        	if (context.hasPermission(clazz.getParentClass(), Right.ClassDetail)) {
        		c.setUrl("clazz?id=" + clazz.getParentClass().getUniqueId());
        		c.setClassName("link");
        	}
        }
        
        if (clazz.getClassSuffix() != null && !clazz.getClassSuffix().isEmpty())
        	response.addProperty(MSG.propertyExternalId()).add(clazz.getClassSuffix());
        
        if (clazz.getEnrollment() != null)
        	response.addProperty(MSG.propertyEnrollment()).add(clazz.getEnrollment().toString());
        
        if (clazz.getNbrRooms() > 0) {
        	if (clazz.getExpectedCapacity() == clazz.getMaxExpectedCapacity())
        		response.addProperty(MSG.propertyClassLimit()).add(clazz.getExpectedCapacity().toString());
        	else {
        		response.addProperty(MSG.propertyMinimumClassLimit()).add(clazz.getExpectedCapacity().toString());
        		response.addProperty(MSG.propertyMaximumClassLimit()).add(clazz.getMaxExpectedCapacity().toString());
        	}
        }
        if (clazz.getSnapshotLimit() != null)
        	response.addProperty(MSG.propertySnapshotLimit()).add(clazz.getSnapshotLimit().toString());
        response.addProperty(MSG.propertyNumberOfRooms()).add(clazz.getNbrRooms().toString());
        if (clazz.getNbrRooms() != 0) {
        	CellInterface c = response.addProperty(MSG.propertyRoomRatio());
        	c.add(clazz.getRoomRatio().toString());
        	c.add("( " + MSG.propertyMinimumRoomCapacity() + " " + clazz.getMinRoomLimit()).addStyle("padding-left: 20px;");
        	if (clazz.getNbrRooms() > 1) {
        		if (clazz.isRoomsSplitAttendance())
        			c.add(" " + MSG.descClassMultipleRoomsSplitAttendance());
        		else
        			c.add(" " + MSG.descClassMultipleRoomsAlternativeAttendance());
        	}
        	c.add(")");
        }
        if (clazz.getNbrRooms() > 1)
        	response.addProperty(MSG.propertyRoomSplitAttendance()).add(
        			clazz.isRoomsSplitAttendance() ? MSG.descriptionClassMultipleRoomsSplitAttendance()
        					: MSG.descriptionClassMultipleRoomsAlternativeAttendance(), true);
        if (clazz.getLms() != null)
        	response.addProperty(MSG.propertyLms()).add(clazz.getLms().getLabel());
        
        if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
        	Department d = clazz.getEffectiveFundingDept();
        	if (d != null)
        		response.addProperty(MSG.propertyFundingDept()).add(d.getLabel());
        }
        
        DatePattern datePattern = clazz.getDatePattern();
        if (datePattern != null) {
        	CellInterface c = response.addProperty(MSG.propertyDatePattern()).add(datePattern.getName());
        	c.add("").setMouseClick("$wnd.showGwtDialog('" + MSG.sectPreviewOfDatePattern(datePattern.getName()) + "', 'dispDatePattern.action?id=" + datePattern.getUniqueId() + "&classId=" + clazz.getUniqueId() + "','840','520');")
        	.setImage().setSource("images/calendar.png").addStyle("cursor: pointer; padding-left: 5px; vertical-align: bottom;");
        } else {
        	datePattern = clazz.effectiveDatePattern();
        	if (datePattern != null) {
            	CellInterface c = response.addProperty(MSG.propertyDatePattern()).add(MSG.dropDefaultDatePattern() + " (" + datePattern.getName() + ")");
            	c.add("").setMouseClick("$wnd.showGwtDialog('" + MSG.sectPreviewOfDatePattern(datePattern.getName()) + "', 'dispDatePattern.action?id=" + datePattern.getUniqueId() + "&classId=" + clazz.getUniqueId() + "','840','520');")
            	.setImage().setSource("images/calendar.png").addStyle("cursor: pointer; padding-left: 5px; vertical-align: bottom;");
        	}
        }
        
        CellInterface cDi = response.addProperty(MSG.propertyDisplayInstructors());
        if (clazz.isDisplayInstructor()) {
        	cDi.addImage().setSource("images/accept.png")
        		.setAlt(MSG.titleInstructorDisplayed())
        		.setTitle(MSG.titleInstructorDisplayed());
        } else {
        	cDi.addImage().setSource("images/cross.png")
        		.setAlt(MSG.titleInstructorNotDisplayed())
        		.setTitle(MSG.titleInstructorNotDisplayed());
        }
        
        CellInterface cSS = response.addProperty(MSG.propertyEnabledForStudentScheduling());
        if (clazz.isEnabledForStudentScheduling()) {
        	cSS.addImage().setSource("images/accept.png")
    			.setAlt(MSG.titleEnabledForStudentScheduling())
    			.setTitle(MSG.titleEnabledForStudentScheduling());
        } else {
        	cSS.addImage().setSource("images/cross.png")
    			.setAlt(MSG.titleNotEnabledForStudentScheduling())
    			.setTitle(MSG.titleNotEnabledForStudentScheduling());
        }
        
        if (clazz.getSchedulePrintNote() != null && !clazz.getSchedulePrintNote().isEmpty()) {
        	response.addProperty(MSG.propertyStudentScheduleNote())
        		.setHtml(clazz.getSchedulePrintNote()).addStyle("white-space: pre-wrap;");
        }
        
        if (clazz.getNotes() != null && !clazz.getNotes().isEmpty()) {
        	response.addProperty(MSG.propertyRequestsNotes())
        		.setHtml(clazz.getNotes()).addStyle("white-space: pre-wrap;");
        }
    
    	String nameFormat = UserProperty.NameFormat.get(context.getUser());
        if (!clazz.getClassInstructors().isEmpty()) {
        	List<TeachingResponsibility> responsibilities = TeachingResponsibility.getInstructorTeachingResponsibilities();
        	TableInterface table = new TableInterface();
            LineInterface header = table.addHeader();
            header.addCell(MSG.columnInstructorName());
            header.addCell(MSG.columnInstructorShare()).setTextAlignment(Alignment.RIGHT);
            header.addCell(MSG.columnInstructorCheckConflicts()).setTextAlignment(Alignment.CENTER);
            if (!responsibilities.isEmpty())
            	header.addCell(MSG.columnTeachingResponsibility());
        	for (CellInterface cell: header.getCells()) {
        		cell.setClassName("WebTableHeader");
        		cell.setText(cell.getText().replace("<br>", "\n"));
        	}
        	for (ClassInstructor ci: new TreeSet<ClassInstructor>(clazz.getClassInstructors())) {
        		LineInterface line = table.addLine();
        		line.setURL("instructorDetail.action?instructorId=" + ci.getInstructor().getUniqueId() + "&deptId=" + ci.getInstructor().getDepartment().getUniqueId());
        		line.addCell(ci.getInstructor().getName(nameFormat));
        		line.addCell(ci.getPercentShare().toString()).setTextAlignment(Alignment.RIGHT);
        		if (ci.isLead())
        			line.addCell().setTextAlignment(Alignment.CENTER).addImage().setSource("images/accept.png").setTitle(MSG.toolTipInstructorLead());
        		else
        			line.addCell();
        		if (!responsibilities.isEmpty())
        			line.addCell(ci.getResponsibility() == null ? null : ci.getResponsibility().getLabel());
        	}
        	table.setStyle("width: 50%;");
        	response.addProperty(MSG.propertyInstructors()).setTable(table);
        }
        
        List<AccommodationCounter> acc = StudentAccomodation.getAccommodations(clazz);
        if (acc != null && !acc.isEmpty()) {
        	CellInterface c = response.addProperty(MSG.propertyAccommodations());
        	TableInterface table = new TableInterface();
        	for (AccommodationCounter ac: acc)
        		table.addProperty(ac.getAccommodation().getName() + ":").setText(String.valueOf(ac.getCount()));
        	c.setTable(table);
        }
        
        if (CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.DisplayLastChanges))) {
        	ChangeLog cl = ChangeLog.findLastChange(ss);
        	if (cl != null)
        		response.addProperty(GWT.propLastChange()).add(cl.getShortLabel());
        	else
        		response.addProperty(GWT.propLastChange()).add(GWT.notApplicable()).addStyle("font-style: italic;");
        }
        
        ClassAssignmentProxy proxy = classAssignmentService.getAssignment();
        if (proxy != null) {
        	AssignmentInfo a = proxy.getAssignment(clazz);
        	if (a != null && a.isCommitted() && a.getDatePattern() != null) {
        		if (!a.getDatePattern().equals(clazz.effectiveDatePattern())) {
        			if (a.getDatePattern().getParents() == null || !a.getDatePattern().getParents().contains(clazz.effectiveDatePattern())) {
        				response.getProperties().getProperties().add(0,
        					new PropertyInterface().setCell(new CellInterface()
        						.setText(MSG.datePatternCommittedIsDifferent(clazz.getClassLabel(), a.getDatePattern().getName(), clazz.effectiveDatePattern().getName()))
        						.setColor("red")));
        			}
        		}
        	}
        }
        
    	response.setConflicts(getConflictTable(context, proxy, clazz));
    	response.setEventConflicts(getEventConflictTable(context, proxy, clazz));

        response.setPreferences(getPreferenceTable(context, clazz, Preference.Type.DATE, Preference.Type.TIME,
        		Preference.Type.ROOM_GROUP, Preference.Type.ROOM, Preference.Type.BUILDING, Preference.Type.ROOM_FEATURE));

        
        if (context.hasPermission(Right.ClassAssignments)) {
			ClassAssignmentDetails ca = ClassAssignmentDetails.createClassAssignmentDetails(context, courseTimetablingSolverService.getSolver(), clazz.getUniqueId(), true);
			if (ca != null) {
				response.setTimetable(getAssignmentTable(context, courseTimetablingSolverService.getSolver(), ca));
			} else {
				ClassAssignmentProxy cap = classAssignmentService.getAssignment();
				if (cap != null) {
					AssignmentInfo assignment = cap.getAssignment(clazz);
					if (assignment!=null && assignment.getUniqueId()!=null) {
						ca = ClassAssignmentDetails.createClassAssignmentDetailsFromAssignment(context, assignment.getUniqueId(), true);
						if (ca != null) {
							response.setTimetable(getAssignmentTable(context, courseTimetablingSolverService.getSolver(), ca));
						}
					}
				}
			}
		}
		
    	ExaminationsTableBuilder examBuilder = new ExaminationsTableBuilder(context, null, null);
    	response.setExaminations(examBuilder.createExamsTable("Class_", clazz.getUniqueId(), examinationSolverService.getSolver()));
	    
    	DistributionsTableBuilder distBuilder = new DistributionsTableBuilder(context, null, null);
    	response.setDistributions(distBuilder.getDistPrefsTableForClass(clazz));
    	
    	BackItem back = BackTracker.getBackItem(context, 2);
    	if (back != null) {
    		response.addOperation("back");
    		response.setBackTitle(back.getTitle());
    		response.setBackUrl(back.getUrl() +
    				(back.getUrl().indexOf('?') >= 0 ? "&" : "?") +
    				"backId=" + clazz.getUniqueId() + "&backType=PreferenceGroup");
    	}
    	if (response.getPreviousId() != null && context.hasPermission(response.getPreviousId(), "Class_", Right.ClassDetail))
    		response.addOperation("previous");
    	if (response.getNextId() != null && context.hasPermission(response.getNextId(), "Class_", Right.ClassDetail))
    		response.addOperation("next");
    	if (context.hasPermission(Right.ExaminationAdd))
    		response.addOperation("add-exam");
    	if (context.hasPermission(clazz.getManagingDept(), Right.DistributionPreferenceAdd) && context.hasPermission(clazz, Right.DistributionPreferenceClass))
    		response.addOperation("add-distribution");
    	if (context.hasPermission(clazz, Right.ClassEdit))
    		response.addOperation("edit");
    	if (context.hasPermission(clazz, Right.ClassAssignment))
    		response.addOperation("assign");
		
		return response;
	}
	
	public static CellInterface toCell(ClassAssignmentDetails other) {
		CellInterface c = new CellInterface();
		c.add(other.getClazz().getName()).setUrl("clazz?id=" + other.getClazz().getClassId()).setClassName("link");
		c.add(" ");
		c.add(other.getTime().getDaysName() + " " + other.getTime().getStartTime() + " - " + other.getTime().getEndTime())
			.setColor(PreferenceLevel.int2color(other.getTime().getPref()))
			.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement, '" + other.getClazz().getClassId() + "," + other.getTime().getDays() + "," + other.getTime().getStartSlot() + "');")
			.setMouseOut("$wnd.hideGwtTimeHint();");
		c.add(" ");
		c.add(other.getTime().getDatePatternName()).setColor(PreferenceLevel.int2color(other.getTime().getDatePatternPreference()));
        for (int i=0;i<other.getRoom().length;i++) {
        	if (i>0) c.add(", ");
        	else c.add(" ");
        	RoomInfo room = other.getRoom()[i];
        	c.add(room.getName())
				.setColor(PreferenceLevel.int2color(room.getPref()))
				.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getId() + "', '" + PreferenceLevel.int2string(room.getPref()) + "');")
				.setMouseOut("$wnd.hideGwtRoomHint();");
        }
		return c;
	}
	
	public static TableInterface getAssignmentTable(SessionContext context, SolverProxy solver, ClassAssignmentDetails ca) {
		TableInterface table = new TableInterface();
		table.setName(MSG.sectionTitleTimetable());
		if (ca.getTime() == null) {
			table.addProperty((String)null).setText(MSG.messageNotAssigned()).addStyle("font-style: italic;");
		} else {
			table.addProperty(MSG.propertyDate()).add(ca.getAssignedTime().getDatePatternName())
				.setColor(PreferenceLevel.int2color(ca.getAssignedTime().getDatePatternPreference()));
			table.addProperty(MSG.propertyTime()).add(
						ca.getAssignedTime().getDaysName() + " " + ca.getAssignedTime().getStartTime() +
						" - " + ca.getAssignedTime().getEndTime())
					.setColor(PreferenceLevel.int2color(ca.getAssignedTime().getPref()))
					.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement, '" + ca.getClazz().getClassId() + "," + ca.getAssignedTime().getDays() + "," + ca.getAssignedTime().getStartSlot() + "');")
					.setMouseOut("$wnd.hideGwtTimeHint();");
			if (ca.getAssignedRoom() != null) {
				CellInterface c = table.addProperty(MSG.propertyRoom());
				for (RoomInfo room: ca.getAssignedRoom()) {
					if (c.hasItems()) c.add(", ");
					c.add(room.getName())
						.setColor(PreferenceLevel.int2color(room.getPref()))
						.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getId() + "', '" + PreferenceLevel.int2string(room.getPref()) + "');")
						.setMouseOut("$wnd.hideGwtRoomHint();");
				}
			}
		}
		if (ca.getInstructor()!=null) {
			CellInterface c = table.addProperty(MSG.propertyInstructor());
			for (InstructorInfo instructor: ca.getInstructor()) {
				if (c.hasItems()) c.add(", ");
				c.add(instructor.getName()).setUrl("instructorDetail.action?instructorId=" + instructor.getId()).setClassName("link");
			}
			if (!ca.getBtbInstructors().isEmpty()) {
				for (Enumeration e=ca.getBtbInstructors().elements();e.hasMoreElements();) {
					ClassAssignmentDetails.BtbInstructorInfo btb = (ClassAssignmentDetails.BtbInstructorInfo)e.nextElement();
					CellInterface o = table.addProperty("");
					try {
						ClassAssignmentDetails other = ClassAssignmentDetails.createClassAssignmentDetails(context, solver, btb.getOtherClassId(), false);
						o.add(PreferenceLevel.int2string(btb.getPreference())).setColor(PreferenceLevel.int2color(btb.getPreference()));
						o.add(" ");
						o.addItem(toCell(other));
					} catch (Exception ex) {
						Debug.error(ex);
					}
				}
			}
		}
		if (ca.getInitialTime()!=null) {
			CellInterface c = table.addProperty(MSG.propertyInitialAssignment());
			if (ca.isInitial()) {
				c.add(MSG.messageThisOne()).addStyle("font-style: italic;");
			} else {
				c.add(ca.getInitialTime().getDaysName() + " " + ca.getInitialTime().getStartTime() + " - " + ca.getInitialTime().getEndTime())
					.setColor(PreferenceLevel.int2color(ca.getInitialTime().getPref()))
					.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement, '" + ca.getClazz().getClassId() + "," + ca.getInitialTime().getDays() + "," + ca.getInitialTime().getStartSlot() + "');")
					.setMouseOut("$wnd.hideGwtTimeHint();");
				c.add(" ");
				c.add(ca.getInitialTime().getDatePatternName()).setColor(PreferenceLevel.int2color(ca.getInitialTime().getDatePatternPreference()));
				for (RoomInfo room: ca.getInitialRoom()) {
					if (c.hasItems() && c.getItems().size() > 3) c.add(", ");
					else c.add(" ");
					c.add(room.getName())
					.setColor(PreferenceLevel.int2color(room.getPref()))
					.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getId() + "', '" + PreferenceLevel.int2string(room.getPref()) + "');")
					.setMouseOut("$wnd.hideGwtRoomHint();");
				}
			}
		}
		
		if (!ca.getStudentConflicts().isEmpty()) {
			CellInterface c = table.addProperty(MSG.propertyStudentConflicts());
			Collections.sort(ca.getStudentConflicts(), new ClassAssignmentDetails.StudentConflictInfoComparator(context, solver));
			for (Enumeration e=ca.getStudentConflicts().elements();e.hasMoreElements();) {
				ClassAssignmentDetails.StudentConflictInfo std = (ClassAssignmentDetails.StudentConflictInfo)e.nextElement();
				if (std.getOther() == null) std.createOther(context, solver);
				ClassAssignmentDetails other = std.getOther();
				CellInterface s = c.add(null).setInline(false);
				List<String> props = new ArrayList<String>();
				if (std.getInfo().isCommited()) props.add(GWT.studentConflictCommitted());
		        if (std.getInfo().isFixed()) props.add(GWT.studentConflictFixed());
		        else if (std.getInfo().isHard()) props.add(GWT.studentConflictHard());
		        if (std.getInfo().isDistance()) props.add(GWT.studentConflictDistance());
		        if (std.getInfo().isCommited())
		        	other.getClazz().setPref(PreferenceLevel.sRequired);
		        if (std.getInfo().isImportant()) props.add(GWT.studentConflictImportant());
		        if (std.getInfo().isInstructor()) props.add(GWT.studentConflictInstructor());
				s.add(new DecimalFormat("0").format(std.getInfo().getJenrl()));
				s.add("\u00d7 ");
				s.addItem(toCell(other));
		        if (!props.isEmpty())
		        	s.add(" " + props).addStyle("font-style: italic;");
		        if (std.getInfo().getCurriculumText() != null && !std.getInfo().getCurriculumText().isEmpty())
		        	s.add(" " + std.getInfo().getCurriculumText()).addStyle("font-style: italic;");
			}
		}
		
		if (ca.hasViolatedGroupConstraint()) {
			CellInterface c = table.addProperty(MSG.propertyViolatedConstraints());
			for (Enumeration e=ca.getGroupConstraints().elements();e.hasMoreElements();) {
				ClassAssignmentDetails.DistributionInfo gc = (ClassAssignmentDetails.DistributionInfo)e.nextElement();
				if (gc.getInfo().isSatisfied()) continue;
				CellInterface g = c.add(null);
				g.add(PreferenceLevel.prolog2string(gc.getInfo().getPreference())).setColor(PreferenceLevel.prolog2color(gc.getInfo().getPreference()));
				g.add(" " + gc.getInfo().getName());
				for (Enumeration f=gc.getClassIds().elements();f.hasMoreElements();) {
					Long classId = (Long)f.nextElement();
					ClassAssignmentDetails other = ClassAssignmentDetails.createClassAssignmentDetails(context, solver, classId, false);
					g.addItem(toCell(other).setInline(false));
				}
			}
		}
		return table;
    }
	
	public static TableInterface getConflictTable(SessionContext context, ClassAssignmentProxy proxy, Class_ clazz) {
    	Set<AssignmentInfo> conflicts = (proxy == null ? null : proxy.getConflicts(clazz.getUniqueId()));
    	if (conflicts == null || conflicts.isEmpty())  return null;
    	String nameFormat = UserProperty.NameFormat.get(context.getUser());
    	
		TreeSet<AssignmentInfo> orderedConflicts = new TreeSet<AssignmentInfo>(new Comparator<AssignmentInfo>() {
			ClassComparator cc = new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY);
			@Override
			public int compare(AssignmentInfo a1, AssignmentInfo a2) {
				return cc.compare(a1.getClazz(), a2.getClazz());
			}
		});
		orderedConflicts.addAll(conflicts);
		
		boolean hasSnapshotLimit = false;
		for (AssignmentInfo assignment: orderedConflicts) {
			if (assignment.getClazz().getSnapshotLimit() != null) {
				hasSnapshotLimit = true;
				break;
			}
		}
		
		boolean hasConflictingInstructors = false;
		check: for (AssignmentInfo assignment: orderedConflicts) {
			for (ClassInstructor instructor: clazz.getClassInstructors()) {
				if (!instructor.isLead() || instructor.getInstructor().getExternalUniqueId() == null) continue;
    			for (StudentClassEnrollment e: assignment.getClazz().getStudentEnrollments()) {
    				if (instructor.getInstructor().getExternalUniqueId().equals(e.getStudent().getExternalUniqueId())) {
    					hasConflictingInstructors = true;
    					break check;
    				}
    			}
			}
		}
		
		TableInterface table = new TableInterface();
		table.setName(MSG.sectionTitleClassConflicts());
		LineInterface header = table.addHeader();
		header.addCell(MSG.columnClass());
		header.addCell(MSG.columnExternalId());
		header.addCell(MSG.columnDemand()).setTextAlignment(Alignment.RIGHT);
		if (hasSnapshotLimit) header.addCell(MSG.columnSnapshotLimit()).setTextAlignment(Alignment.RIGHT);
		header.addCell(MSG.columnInstructor());
		header.addCell(MSG.columnDatePattern());
		header.addCell(MSG.columnAssignedTime());
		header.addCell(MSG.columnAssignedRoom());
		if (hasConflictingInstructors)
			header.addCell(MSG.columnEnrollmentConflict());
		for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    	}
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
			CellInterface room = new CellInterface();
			for (Location r: assignment.getRooms()) {
				if (room.hasItems()) room.add(", ");
				CellInterface c = room.add(r.getLabel());
				c.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + r.getUniqueId() + "', '');");
		    	c.setMouseOut("$wnd.hideGwtRoomHint();");
			}
			CellInterface instr = new CellInterface();
			CellInterface enrolled = new CellInterface();
			for (ClassInstructor ci: clazz.getClassInstructors()) {
				CellInterface c = instr.add(ci.getInstructor().getName(nameFormat)).setInline(false);
				String title = ci.getInstructor().getNameLastFirst();
	    		title += " (" + (ci.getResponsibility() == null ? "" : ci.getResponsibility().getLabel() + " ") +
	    				ci.getPercentShare()+"%"+(ci.isLead().booleanValue()?", " + MSG.toolTipInstructorLead():"")+")";
	    		if (!ci.getClassInstructing().isDisplayInstructor()) {
	    			c.addStyle("font-style: italic;");
	    			title += MSG.toolTipInstructorDoNotDisplay();
	    		}
	    		c.setTitle(title);
	    		if (ci.getResponsibility() != null && ci.getResponsibility().getAbbreviation() != null && !ci.getResponsibility().getAbbreviation().isEmpty())
	    			c.add(" (" + ci.getResponsibility().getAbbreviation() + ")");

				if (!ci.isLead() || ci.getInstructor().getExternalUniqueId() == null) continue;
    			for (StudentClassEnrollment e: assignment.getClazz().getStudentEnrollments()) {
    				if (ci.getInstructor().getExternalUniqueId().equals(e.getStudent().getExternalUniqueId())) {
    					enrolled.add(e.getStudent().getName(nameFormat)).setInline(false);
    				}
    			}
			}
			LineInterface line = table.addLine();
			line.setURL("clazz?id=" + assignment.getClazz().getUniqueId());
			line.addCell(assignment.getClazz().getClassLabel());
			line.addCell(suffix);
			line.addCell(assignment.getClazz().getEnrollment().toString());
			if (hasSnapshotLimit)
				line.addCell(assignment.getClazz().getSnapshotLimit() == null ? null : assignment.getClazz().getSnapshotLimit().toString());
			line.addCell(instr);
			if (dp == null)
				line.addCell();
			else
				line.addCell(dp.getName()).setTitle(dateFormat.format(dp.getStartDate()) + " - " + dateFormat.format(dp.getEndDate()));
			line.addCell(time);
			line.addCell(room);
			if (hasConflictingInstructors)
				line.addCell(enrolled);
		}
		return table;
	}
	
	public static TableInterface getEventConflictTable(SessionContext context, ClassAssignmentProxy proxy, Class_ clazz) {
    	Set<TimeBlock> ec = (proxy == null ? null : proxy.getConflictingTimeBlocks(clazz.getUniqueId()));
    	if (ec == null || ec.isEmpty()) return null;
    	TableInterface table = new TableInterface();
		table.setName(MSG.sectionTitleEventConflicts());
		LineInterface header = table.addHeader();
		header.addCell(MSG.columnEventName());
		header.addCell(MSG.columnEventType());
		header.addCell(MSG.columnEventDate());
		header.addCell(MSG.columnEventTime());
		for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    	}
		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_MEETING);
		Formats.Format<Date> timeFormat = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
		for (TimeBlock block: ec) {
			LineInterface line = table.addLine();
			if (block instanceof MeetingTimeBlock) {
				MeetingTimeBlock mtb = (MeetingTimeBlock)block;
				if (mtb.getEventId() != null && context.hasPermission(mtb.getEventId(), Right.EventDetail))
					line.setURL("events#event=" + mtb.getEventId());
			}
			line.addCell(block.getEventName());
			line.addCell(block.getEventType());
			line.addCell(dateFormat.format(block.getStartTime()));
			line.addCell(timeFormat.format(block.getStartTime()) + " - " + timeFormat.format(block.getEndTime()));
			
		}
		return table;
	}
	
	
	public static TableInterface getPreferenceTable(SessionContext context, Class_ clazz, Preference.Type... types) {
		TableInterface preferences = SubpartDetailBackend.getPreferenceTable(context, clazz, types);
        if (preferences != null) {
            if (clazz.getNbrRooms()>0) {
        		if (clazz.hasRoomIndexedPrefs()) {
        			TableInterface table = new TableInterface();
        			for (int roomIndex = 0; roomIndex < clazz.getNbrRooms(); roomIndex++) {
        				CellInterface cell = table.addProperty(MSG.itemOnlyRoom(1 + roomIndex) + ":");
        				List<RoomLocation> roomLocations = TimetableDatabaseLoader.computeRoomLocations(clazz, roomIndex);
                		if (roomLocations.isEmpty()) {
                			cell.add(MSG.warnNoRoomsAreAvaliable()).setColor("red").addStyle("font-weight: bold;");
                		} else {
            				int idx = 0;
                    		for (RoomLocation rl: roomLocations) {
                    			if (idx>0) cell.add(", ");
                				if (idx==6)
                					cell = cell.add(MSG.moreAvailableRooms(roomLocations.size() - 6)).setDots(true);
                				CellInterface c = cell.add(rl.getName());
                				c.setColor(PreferenceLevel.int2color(rl.getPreference()));
                				c.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + rl.getId() + "', '" + PreferenceLevel.int2string(rl.getPreference()) + "');");
                				c.setMouseOut("$wnd.hideGwtRoomHint();");                						
                				idx++;
                    		}
                		}
        			}
        			preferences.getProperties().add(preferences.getProperties().size() - 1,
        					new PropertyInterface().setName(MSG.propertyAvailableRooms()).setCell(new CellInterface().setTable(table)));
        		} else {
                	List<RoomLocation> roomLocations = TimetableDatabaseLoader.computeRoomLocations(clazz);
                	CellInterface cell = new CellInterface();
                	preferences.getProperties().add(preferences.getProperties().size() - 1,
        					new PropertyInterface().setName(MSG.propertyAvailableRooms()).setCell(cell));
                	if (roomLocations.isEmpty()) {
            			cell.add(MSG.warnNoRoomsAreAvaliable()).setColor("red").addStyle("font-weight: bold;");
            		} else {
        				int idx = 0;
                		for (RoomLocation rl: roomLocations) {
                			if (idx>0) cell.add(", ");
            				if (idx==6)
            					cell = cell.add(MSG.moreAvailableRooms(roomLocations.size() - 6)).setDots(true);
            				CellInterface c = cell.add(rl.getName());
            				c.setColor(PreferenceLevel.int2color(rl.getPreference()));
            				c.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + rl.getId() + "', '" + PreferenceLevel.int2string(rl.getPreference()) + "');");
            				c.setMouseOut("$wnd.hideGwtRoomHint();");                						
            				idx++;
                		}
            		}
        		}
            }
        }		
		return preferences;
	}
}
