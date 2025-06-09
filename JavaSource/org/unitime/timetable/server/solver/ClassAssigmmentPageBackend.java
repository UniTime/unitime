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
package org.unitime.timetable.server.solver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.cpsolver.coursett.preference.SumPreferenceCombination;
import org.hibernate.LazyInitializationException;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.events.RoomFilterBackend;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ClassAssignmentPageInterface.ChangeInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentPageInterface.ClassAssignmentPageRequest;
import org.unitime.timetable.gwt.shared.ClassAssignmentPageInterface.ClassAssignmentPageResponse;
import org.unitime.timetable.gwt.shared.ClassAssignmentPageInterface.DomainItem;
import org.unitime.timetable.gwt.shared.ClassAssignmentPageInterface.Operation;
import org.unitime.timetable.gwt.shared.ClassAssignmentPageInterface.RoomOrder;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.TableInterface.NaturalOrderComparator;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePattern.DatePatternType;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventDateMapping;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.RoomSharingModel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.ClassDetailBackend;
import org.unitime.timetable.solver.course.ui.ClassAssignment;
import org.unitime.timetable.solver.course.ui.ClassAssignmentInfo;
import org.unitime.timetable.solver.course.ui.ClassDateInfo;
import org.unitime.timetable.solver.course.ui.ClassInfo;
import org.unitime.timetable.solver.course.ui.ClassInstructorInfo;
import org.unitime.timetable.solver.course.ui.ClassProposedChange;
import org.unitime.timetable.solver.course.ui.ClassRoomInfo;
import org.unitime.timetable.solver.course.ui.ClassTimeInfo;
import org.unitime.timetable.solver.course.ui.ClassAssignmentInfo.StudentConflict;
import org.unitime.timetable.util.DefaultRoomAvailabilityService;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.util.duration.DurationModel;

@GwtRpcImplements(ClassAssignmentPageRequest.class)
public class ClassAssigmmentPageBackend implements GwtRpcImplementation<ClassAssignmentPageRequest, ClassAssignmentPageResponse>{
	private static Log sLog = LogFactory.getLog(ClassAssigmmentPageBackend.class);
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public ClassAssignmentPageResponse execute(ClassAssignmentPageRequest request, SessionContext context) {
		if (!request.hasChanges())
			context.checkPermission(request.getSelectedClassId(), Right.ClassAssignment);
		
		if (request.getOfferingId() != null && request.getOperation() == Operation.LOCK) {
			InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(request.getOfferingId());
			context.checkPermission(offering, Right.OfferingCanLock);
        	offering.getSession().lockOffering(offering.getUniqueId());
		}

		if (request.isShowStudentConflicts() == null)
			request.setShowStudentConflicts(ApplicationProperty.ClassAssignmentShowStudentConflicts.isTrue());
		if (request.isUseRealStudents() == null)
			request.setUseRealStudents(StudentClassEnrollment.sessionHasEnrollments(context.getUser().getCurrentAcademicSessionId()));
		
		EventDateMapping.Class2EventDateMap class2eventDates = EventDateMapping.getMapping(context.getUser().getCurrentAcademicSessionId());
		ClassProposedChange proposed = null;
		if (request.hasChanges()) {
			proposed = new ClassProposedChange();
			proposed.setSelected(request.getSelectedClassId());
			for (ChangeInterface ch: request.getChanges()) {
        		Class_ clazz = Class_DAO.getInstance().get(ch.getClassId());
        		if (clazz == null) continue;
        		ClassAssignmentInfo initial = null;
    			Map<ClassAssignment, Set<Long>> conflicts = null;
    			if (request.isShowStudentConflicts() && ApplicationProperty.ClassAssignmentPrefetchConflicts.isTrue())
    				conflicts = ClassInfo.findAllRelatedAssignments(clazz.getUniqueId(), request.isUseRealStudents());
        		if (clazz.getCommittedAssignment() != null)
        			initial = new ClassAssignmentInfo(clazz.getCommittedAssignment(), request.isUseRealStudents(), conflicts);
        		ClassTimeInfo time = null;
        		ClassDateInfo date = null;
        		Collection<ClassRoomInfo> rooms = null;
        		
        		if (ch.hasDate()) {
        			DatePattern dp = DatePatternDAO.getInstance().get(Long.valueOf(ch.getDate()));
        			int datePref = 0;
        			for (DatePatternPref p: clazz.effectivePreferences(DatePatternPref.class))
        				if (p.getDatePattern().equals(dp))
        					datePref = PreferenceLevel.prolog2int(p.getPrefLevel().getPrefProlog());
        			if (dp != null)
        				date = new ClassDateInfo(dp.getUniqueId(), clazz.getUniqueId(), dp.getName(), dp.getPatternBitSet(), datePref); 
        		}
        		
        		if ("null".equals(ch.getTime())) {
        		} else if (ch.hasTime()) {
        			DurationModel dm = clazz.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
        			String[] timeId = ch.getTime().split(":");
        			DatePattern dp = DatePatternDAO.getInstance().get(Long.valueOf(timeId[0]));
        			if (date != null) {
        				dp = date.getDatePattern();
        			} else if (dp != null && dp.getDatePatternType() != DatePatternType.PatternSet) {
        				int datePref = 0;
            			for (DatePatternPref p: clazz.effectivePreferences(DatePatternPref.class))
            				if (p.getDatePattern().equals(dp))
            					datePref = PreferenceLevel.prolog2int(p.getPrefLevel().getPrefProlog());
        				date = new ClassDateInfo(dp.getUniqueId(), clazz.getUniqueId(), dp.getName(), dp.getPatternBitSet(), datePref);
        			}
        			TimePattern tp = TimePatternDAO.getInstance().get(Long.valueOf(timeId[1]));
        			int dayCode = Integer.valueOf(timeId[2]);
        			int startTime = Integer.valueOf(timeId[3]);
        			if (dp != null && tp != null) {
    					int length = tp.getSlotsPerMtg();
    					int minsPerMeeting = tp.getMinPerMtg();
    					int breakTime = tp.getBreakTime();
    					int timePref = 0;
    					for (TimePref p: clazz.effectivePreferences(TimePref.class)) {
    						if (p.getTimePattern().equals(tp)) {
    							TimePatternModel model = p.getTimePatternModel();
    							if (tp.isExactTime()) {
    								if (model.getExactDays() == dayCode && model.getExactStartSlot() == startTime)
    									timePref = PreferenceLevel.prolog2int(p.getPrefLevel().getPrefProlog());
    							} else {
    								for (int d = 0; d < model.getNrDays(); d++)
    									if (model.getDayCode(d) == dayCode)
    										for (int t = 0; t < model.getNrTimes(); t++)
    											if (model.getStartSlot(t) == startTime)
    												timePref = PreferenceLevel.prolog2int(model.getPreference(d, t));
    							}
    						}
    					}
        				if (tp.isExactTime()) {
        	    			minsPerMeeting = dm.getExactTimeMinutesPerMeeting(clazz.getSchedulingSubpart().getMinutesPerWk(), dp, dayCode);
        	        		length = ExactTimeMins.getNrSlotsPerMtg(minsPerMeeting);
        	        		breakTime = ExactTimeMins.getBreakTime(minsPerMeeting);
        				}
        				List<Date> dates = dm.getDates(clazz.getSchedulingSubpart().getMinutesPerWk(), dp, dayCode, minsPerMeeting, class2eventDates);
    	        		time = new ClassTimeInfo(clazz.getUniqueId(),
    	        				dayCode, startTime,
    	        				length, minsPerMeeting, timePref,
    	        				tp,
    	        				date != null ? date : new ClassDateInfo(dp.getUniqueId(), clazz.getUniqueId(), dp.getName(), dp.getPatternBitSet(), 0),
    	        				breakTime, dates);
        			}
        		}
        		
        		if (ch.hasRoom()) {
        			rooms = new TreeSet<ClassRoomInfo>();
        	        for (String token: ch.getRoom().split(":")) {
        	            if (token.trim().isEmpty()) continue;
        	            Location location = LocationDAO.getInstance().get(Long.valueOf(token));
        	            if (location != null)
        	            	rooms.add(new ClassRoomInfo(location, 0));
        	        }
        		}
        		
        		if (ch.getClassId().equals(request.getSelectedClassId()) && initial != null && !"null".equals(ch.getTime())) {
        			if (time == null) time = initial.getTime();
        			if (date == null) date = initial.getDate();
        		}
        		proposed.addChange(new ClassAssignmentInfo(
        				clazz, time, date, rooms, proposed.getAssignmentTable(), request.isUseRealStudents(), conflicts),
        				initial);
			}
		}
		
		Class_ clazz = Class_DAO.getInstance().get(request.getSelectedClassId());
		ClassInfo classInfo = null;
		Map<ClassAssignment, Set<Long>> conflicts = null;
		if (request.isShowStudentConflicts() && ApplicationProperty.ClassAssignmentPrefetchConflicts.isTrue())
			conflicts = ClassInfo.findAllRelatedAssignments(clazz.getUniqueId(), request.isUseRealStudents());
		if (clazz.getCommittedAssignment() != null)
			classInfo = new ClassAssignmentInfo(clazz.getCommittedAssignment(), request.isUseRealStudents(), conflicts);
		else
			classInfo = new ClassInfo(clazz);
		
		if (!request.hasChange(clazz.getUniqueId()) && classInfo instanceof ClassAssignmentInfo) {
			ClassAssignmentInfo initial = (ClassAssignmentInfo)classInfo;
			if (proposed == null) {
				proposed = new ClassProposedChange();
				proposed.setSelected(request.getSelectedClassId());
			}
			proposed.addChange(new ClassAssignmentInfo(
    				clazz, initial.getTime(), initial.getDate(), null, proposed.getAssignmentTable(), request.isUseRealStudents(), conflicts),
    				initial);
		}
		
		update(proposed, classInfo, !request.isKeepConflictingAssignments(), request.isUseRealStudents());
		
		ClassAssignmentPageResponse response = new ClassAssignmentPageResponse();
		response.setKeepConflictingAssignments(request.isKeepConflictingAssignments());
		response.setShowStudentConflicts(request.isShowStudentConflicts());
		response.setUseRealStudents(request.isUseRealStudents());
		response.setSelectedClassId(classInfo.getClassId());
		response.setManagingDeptCode(clazz.getManagingDept().getDeptCode());
		response.setSessionId(clazz.getManagingDept().getSessionId());
		response.setMinRoomCapacity(clazz.getMinRoomLimit());
		response.setNbrRooms(clazz.getNbrRooms());
		response.setRoomSplitAttendance(clazz.isRoomsSplitAttendance());
		
		response.setClassName(classInfo.getClassName());
		response.setProperties(ClassDetailBackend.getProperties(clazz, context));
		
    	String nameFormat = UserProperty.NameFormat.get(context.getUser());
        if (!clazz.getClassInstructors().isEmpty()) {
        	CellInterface cell = new CellInterface();
        	for (ClassInstructor ci: new TreeSet<ClassInstructor>(clazz.getClassInstructors())) {
        		if (!ci.isLead()) continue;
        		cell.add(ci.getInstructor().getName(nameFormat)).setInline(false);
        	}
        	if (cell.hasItems())
        		response.addProperty(MSG.properyConflictCheckedInstructors(), cell);
        }
        
        if (classInfo instanceof ClassAssignmentInfo) {
        	ClassAssignmentInfo initial = (ClassAssignmentInfo)classInfo;
        	if (initial.getDate() != null)
            	response.addProperty(MSG.properyAssignedDates(), initial.getDate().toCell());
        	if (initial.getTime() != null)
        		response.addProperty(MSG.filterAssignedTime(), initial.getTime().toLongCell());
        	if (initial.getRooms() != null && !initial.getRooms().isEmpty()) {
        		CellInterface c = response.addProperty(MSG.filterAssignedRoom());
        		for (ClassRoomInfo r: initial.getRooms())
        			c.addItem(r.toCell()).setInline(false);
        	}
        } else {
        	DatePattern datePattern = clazz.effectiveDatePattern();
        	if (datePattern != null) {
            	CellInterface c = response.addProperty(MSG.propertyDatePattern()).add(datePattern.getName()).add("");
            	if (datePattern.getDatePatternType() != DatePatternType.PatternSet) {
                	c.addClick().setTitle(MSG.sectPreviewOfDatePattern(datePattern.getName()))
            			.addWidget().setId("UniTimeGWT:DatePattern").setContent(datePattern.getPatternText());
                	c.setImage().setSource("images/calendar.png").addStyle("cursor: pointer; padding-left: 5px; vertical-align: bottom;");
            	}
            }
        }
        ClassAssignmentInfo current = null;
        if (proposed != null) {
        	for (ClassAssignmentInfo assignment : proposed.getAssignments())
        		if (assignment.getClassId().equals(clazz.getUniqueId())) {
        			current = assignment;
        			if (assignment.getDate() != null)
                    	response.addProperty(MSG.properySelectedDates(), assignment.getDate().toCell());
                	if (assignment.getTime() != null)
                		response.addProperty(MSG.properySelectedTime(), assignment.getTime().toLongCell());
                	if (assignment.getRooms() != null && !assignment.getRooms().isEmpty()) {
                		CellInterface c = response.addProperty(MSG.properySelectedRoom());
                		for (ClassRoomInfo r: assignment.getRooms())
                			c.addItem(r.toCell()).setInline(false);
                	}
        		}
        	response.setAssignments(generateAssigmentsTable(proposed, context));
        	
        	response.setStudentConflicts(generateStudentConflicts(proposed));
        }
        
        try {
            Collection<ClassAssignment> dates = getDates(classInfo, response.isShowStudentConflicts(), response.isUseRealStudents(), conflicts);
            if (dates == null) {
            	response.addDatesErrorMessage(MSG.messageClassHasNoDatePatternSelected(classInfo.getClassName()));
            } else if (dates.size() > 1) {
            	ClassAssignment ia = (classInfo instanceof ClassAssignment ? (ClassAssignment)classInfo : null);
            	for (ClassAssignment date: dates) {
            		DomainItem item = new DomainItem();
            		item.setId(date.getDateId());
            		item.setCell(date.getDate().toCell());
            		if (response.isShowStudentConflicts())
            			item.setExtra(new CellInterface().setText(date instanceof ClassAssignmentInfo ? "" + ((ClassAssignmentInfo)date).getNrStudentCounflicts() : ""));
            		item.setValue(date instanceof ClassAssignmentInfo ? ((ClassAssignmentInfo)date).getNrStudentCounflicts() : 0);
            		item.setAssigned(ia != null && ia.getDateId().equals(date.getDateId()));
            		item.setSelected(current != null && date.getDateId().equals(current.getDateId()));
            		response.addDate(item);
            	}
            }
        } catch (GwtRpcException e) {
        	response.addDatesErrorMessage(e.getMessage());
        }
        
        try {
            Collection<ClassAssignment> times = getTimes(classInfo, current == null ? null : current.getDate(), response.isShowStudentConflicts(), response.isUseRealStudents(), conflicts, proposed);
            if (times == null) {
            	response.addTimesErrorMessage(MSG.messageClassHasNoTimePatternSelected(classInfo.getClassName()));
            } else if (times.isEmpty()) {
            	response.addTimesErrorMessage(MSG.messageClassHasNoAvailableTime(classInfo.getClassName()));
            } else {
            	ClassAssignment ia = (classInfo instanceof ClassAssignment ? (ClassAssignment)classInfo : null);
            	for (ClassAssignment time: times) {
            		DomainItem item = new DomainItem();
            		item.setId(time.getTimeId());
            		item.setCell(time.getTime().toLongCell());
            		if (response.isShowStudentConflicts())
            			item.setExtra(new CellInterface().setText(time instanceof ClassAssignmentInfo ? "" + ((ClassAssignmentInfo)time).getNrStudentCounflicts() : ""));
            		item.setValue(time instanceof ClassAssignmentInfo ? ((ClassAssignmentInfo)time).getNrStudentCounflicts() : 0);
                	item.setAssigned(ia != null && ia.getTimeId().equals(time.getTimeId()));
                	item.setSelected(current != null && time.getTimeId().equals(current.getTimeId()));
                	response.addTime(item);
            	}
            	if (current != null && current.getTimeId() != null) {
            		DomainItem item = new DomainItem();
            		item.setId("null");
            		item.setCell(new CellInterface().add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;"));
            		if (response.isShowStudentConflicts())
            			item.setExtra(new CellInterface().setText(""));
            		response.addTime(item);	
            	}
            }
        } catch (GwtRpcException e) {
        	response.addTimesErrorMessage(e.getMessage());
        }
        
        if (current != null && current.getTime() != null) {
        	try {
        		RoomFilterRpcRequest filter = request.getRoomFilter();
        		if (filter == null) {
        			filter = new RoomFilterRpcRequest();
        			filter.setOption("department", clazz.getManagingDept().getDeptCode());
        			if (clazz.getNbrRooms() == 1 || !Boolean.TRUE.equals(clazz.isRoomsSplitAttendance()))
        				filter.setOption("size", ">=" + clazz.getMinRoomLimit());
        		} else if (request.getPreviousClassId() == null || !request.getPreviousClassId().equals(request.getSelectedClassId())) {
        			Class_ prev = (request.getPreviousClassId() == null ? null : Class_DAO.getInstance().get(request.getPreviousClassId()));
        			if (prev == null || !prev.getManagingDept().equals(clazz.getManagingDept()) && filter.hasOption("department"))
        				filter.setOption("department", clazz.getManagingDept().getDeptCode());
        			if (clazz.getNbrRooms() == 1 || !Boolean.TRUE.equals(clazz.isRoomsSplitAttendance()))
        				filter.setOption("size", ">=" + clazz.getMinRoomLimit());
        			else
        				filter.setOption("size", null);
        		}
        		Collection<ClassRoomInfo> rooms = getRooms(classInfo, current.getTime(), request.isRoomAllowConflicts(),
        				filter, context, proposed, request.getRoomOrder());
        		if (rooms != null) {
        			ClassAssignment ia = (classInfo instanceof ClassAssignment ? (ClassAssignment)classInfo : null);
                	TableInterface table = new TableInterface();
                	table.setName(MSG.sectionTitleAvailableRoomsForClass(classInfo.getClassName()));
                	for (ClassRoomInfo room: rooms) {
                		DomainItem item = new DomainItem();
                		item.setId(room.getLocationId().toString());
                		item.setCell(room.toCell());
                		item.setExtra(new CellInterface().setText("" + room.getCapacity()));
                		item.setValue(room.getCapacity());
                		item.setAssigned(ia != null && ia.hasRoom(room.getLocationId()));
                		item.setSelected(current != null && current.hasRoom(room.getLocationId()));
                		response.addRoom(item);
                	}
                	if (rooms.isEmpty())
                		response.addRoomsErrorMessage(MSG.messageNoMatchingRoomFound());
        		}
        	} catch (GwtRpcException e) {
        		response.addRoomsErrorMessage(e.getMessage());
        	}
        }
        
		if (request.getOperation() == Operation.ASSIGN && getCanAssign(proposed, context)) {
			String error = assign(proposed, context);
			if (error != null) {
				if (response.getAssignments() != null)
					response.getAssignments().setErrorMessage(error);
				else
					response.setErrorMessage(error);
			} else {
				response.setUrl("clazz?id=" + request.getSelectedClassId());
			}
		}
		
        response.setCanAssign(getCanAssign(proposed, context));
        
		return response;
	}
	
	public TableInterface generateAssigmentsTable(ClassProposedChange proposed, SessionContext context) {
    	TableInterface table = new TableInterface();
    	table.setName(MSG.sectionTitleNewAssignments());
    	LineInterface header = table.addHeader();
    	header.addCell(MSG.columnClass());
    	header.addCell(MSG.columnInstructor());
    	header.addCell(MSG.columnDateChange());
    	header.addCell(MSG.columnTimeChange());
    	header.addCell(MSG.columnRoomChange());
    	for (CellInterface h: header.getCells())
        	h.setClassName("WebTableHeader");
    	for (ClassAssignmentInfo assignment : proposed.getAssignments()) {
    		ClassAssignment initial = proposed.getInitial(assignment);
    		LineInterface line = table.addLine();
    		line.setURL("#id=" + assignment.getClassId());
    		if (assignment.getClassId().equals(proposed.getSelectedClassId()))
    			line.setBgColor("rgb(168,187,225)");
    		CellInterface c = line.addCell();
    		if (proposed.getAssignments().size() > 1)
    			c.add("").setUrl("#delete=" + assignment.getClassId()).setImage().setSource("images/action_delete.png")
					.addStyle("cursor: pointer; padding-right: 5px; vertical-align: bottom;");
    		boolean canAssign = context.hasPermission(assignment.getClazz(), Right.ClassAssignment);
    		if (!canAssign && context.hasPermission(assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering(), Right.OfferingCanLock)) {
    			c.add("").setUrl("#lock=" + assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId())
    				.setImage().setSource("images/error.png").addStyle("cursor: pointer; padding-right: 5px; vertical-align: bottom;")
    				.setTitle(MSG.titleCourseNotLocked(assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseName()));
    		}
			c.add(assignment.getClassName()).setTitle(assignment.getClassTitle());
    		line.addCell(assignment.getLeadingInstructorNames(", "));
    		CellInterface d = line.addCell(); d.setNoWrap(true);
    		if (initial!=null && !initial.getDateId().equals(assignment.getDateId()))
    			d.addItem(initial.getDate().toCell()).add(" \u2192 ");
    		if (initial==null && assignment.getClazz().effectiveDatePattern().getDatePatternType() == DatePatternType.PatternSet)
    			d.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
    			.add(" \u2192 ");
    		d.addItem(assignment.getDate().toCell());
    		CellInterface t = line.addCell(); t.setNoWrap(true);
    		if (initial!=null && !initial.getTimeId().equals(assignment.getTimeId()))
    			t.addItem(initial.getTime().toCell()).add(" \u2192 ");
    		if (initial==null && assignment.getTime() != null)
    			t.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
    			.add(" \u2192 ");
    		if (assignment.getTime() == null)
    			t.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    		else
    			t.addItem(assignment.getTime().toCell());
    		CellInterface r = line.addCell(); r.setNoWrap(true);
    		if (initial!=null && !initial.getRoomIds().equals(assignment.getRoomIds()))
    			r.addItem(initial.toRoomCell()).add(" \u2192 ");
    		if (initial==null && assignment.getNrRooms() > 0 && assignment.getNumberOfRooms() > 0)
    			r.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
    			.add(" \u2192 ");
    		r.addItem(assignment.toRoomCell());
			if (assignment.getNrRooms()!=assignment.getNumberOfRooms()) {
                if (assignment.getClassId().equals(proposed.getSelectedClassId()))
                	r.add(MSG.assignmentRoomSelectBelow()).addStyle("font-style: italic;");
                else
                	r.add(MSG.assignmentRoomNotSelected()).setColor("red").addStyle("font-style: italic;");
			}
			if (assignment.getNumberOfRooms() == 0)
				r.add(MSG.notApplicable()).addStyle("font-style: italic;");
    	}
    	for (ClassAssignment conflict : proposed.getConflicts()) {
    		LineInterface line = table.addLine();
    		if (conflict.getClassId().equals(proposed.getSelectedClassId()))
    			line.setBgColor("rgb(168,187,225)");
    		line.setURL("#id=" + conflict.getClassId());
    		boolean canAssign = context.hasPermission(conflict.getClazz(), Right.ClassAssignment);
    		if (!canAssign && context.hasPermission(conflict.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering(), Right.OfferingCanLock)) {
    			CellInterface c = line.addCell();
    			c.add("").setUrl("#lock=" + conflict.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId())
    				.setImage().setSource("images/error.png").addStyle("cursor: pointer; padding-right: 5px; vertical-align: bottom;")
    				.setTitle(MSG.titleCourseNotLocked(conflict.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseName()));
    			c.add(conflict.getClassName()).setTitle(conflict.getClassTitle());
    		} else {
    			line.addCell(conflict.getClassName()).setTitle(conflict.getClassTitle());
    		}
    		line.addCell(conflict.getLeadingInstructorNames(", "));
    		CellInterface d = line.addCell(); d.setNoWrap(true);
    		d.addItem(conflict.getDate().toCell()).add(" \u2192 ");
    		d.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    		CellInterface t = line.addCell(); t.setNoWrap(true);
    		t.addItem(conflict.getTime().toCell()).add(" \u2192 ");
    		t.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    		CellInterface r = line.addCell(); r.setNoWrap(true);
    		r.addItem(conflict.toRoomCell()).add(" \u2192 ");
			r.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    	}
    	
    	return table;
	}
	
	public TableInterface generateStudentConflicts(ClassProposedChange change) {
		TableInterface table = new TableInterface();
		table.setName(MSG.sectionTitleStudentConflicts());
		
		HashSet<String> ids = new HashSet<String>();
		TreeSet<StudentConflict> ret = new TreeSet<StudentConflict>();
		for (ClassAssignmentInfo assignment: change.getAssignments()) {
			for (StudentConflict conf: assignment.getStudentConflicts()) {
				String id = (assignment.getClassId().compareTo(conf.getOtherClass().getClassId())<0?
						assignment.getClassId()+":"+conf.getOtherClass().getClassId():
			            conf.getOtherClass().getClassId()+":"+assignment.getClassId());
				if (ids.add(id)) ret.add(conf);
			}
		}
		
    	LineInterface header = table.addHeader();
    	header.addCell(MSG.columnStudentConflicts());
    	header.addCell(MSG.columnClass());
    	header.addCell(MSG.columnAssignedDatePattern());
    	header.addCell(MSG.columnAssignedTime());
    	header.addCell(MSG.columnAssignedRoom());
    	for (CellInterface h: header.getCells())
        	h.setClassName("WebTableHeader");

        for (StudentConflict conf: ret) {
        	LineInterface line = table.addLine();
        	line.setURL("#id=" + conf.getOtherClass().getClassId());
        	line.addCell(String.valueOf(conf.getConflictingStudents().size()))
        		.setColor(PreferenceLevel.prolog2color("P"))
        		.addStyle("font-weight:bold;");
        	CellInterface n = line.addCell(); n.setNoWrap(true);
        	n.add(conf.getThisClass().getClassName()).setTitle(conf.getThisClass().getClassTitle()).setInline(false);
        	n.add(conf.getOtherClass().getClassName()).setTitle(conf.getOtherClass().getClassTitle()).setInline(false);
        	CellInterface d = line.addCell(); d.setNoWrap(true);
        	d.addItem(conf.getThisClass().getDate().toCell().setInline(false));
        	d.addItem(conf.getOtherClass().getDate().toCell().setInline(false));
        	CellInterface t = line.addCell(); t.setNoWrap(true);
        	t.addItem(conf.getThisClass().getTime().toCell().setInline(false));
        	t.addItem(conf.getOtherClass().getTime().toCell().setInline(false));
        	CellInterface r = line.addCell(); r.setNoWrap(true);
        	r.addItem(conf.getThisClass().toRoomCell().setInline(false));
        	r.addItem(conf.getOtherClass().toRoomCell().setInline(false));
        }
        
        if (!table.hasLines())
        	table.addLine().addCell(MSG.messageNoStudentConflicts())
        		.setColSpan(5).addStyle("font-style: italic;");
		
		return table;
	}
	
	public void update(ClassProposedChange change, ClassInfo classInfo, boolean unassignConflictingAssignments, boolean isUseRealStudents) {
        if (change==null) return;
        Vector<ClassAssignment> assignments = new Vector(change.getAssignments());
        Hashtable<Long,ClassAssignment> table = change.getAssignmentTable();
        change.getAssignments().clear();
        for (ClassAssignment assignment : assignments) {
            change.getAssignments().add(new ClassAssignmentInfo(assignment.getClazz(),assignment.getTime(),assignment.getDate(),assignment.getRooms(),table, isUseRealStudents, null));
        }
        if (assignments.isEmpty()) {
        	for (Iterator<ClassAssignment> i = change.getConflicts().iterator(); i.hasNext(); ) {
        		ClassAssignment assignment = i.next();
        		if (assignment == null || !assignment.getClassId().equals(classInfo.getClassId())) i.remove();
        	}
        } else {
        	change.getConflicts().clear();
        }
        for (ClassAssignment assignment : change.getAssignments()) {
        	// Skip incomplete assignments (that have no time assigned yet)
        	if (!assignment.hasTime()) continue;
        	
        	// Check for room conflicts
        	if (unassignConflictingAssignments) {
	            if (assignment.getRooms()!=null) for (ClassRoomInfo room : assignment.getRooms()) {
	            	if (!room.isIgnoreRoomChecks()){
		            	for (Assignment a : room.getLocation().getCommitedAssignments()) {
		            		if (a.getClazz().isCancelled()) continue;
		            		if (assignment.getTime().overlaps(new ClassTimeInfo(a)) && !a.getClazz().canShareRoom(assignment.getClazz())) {
		            			if (change.getCurrent(a.getClassId())==null && change.getConflict(a.getClassId())==null)
		            				change.getConflicts().add(new ClassAssignment(a));
		            		}
		            	}
	            	}
	            	if (room.getLocation() instanceof Room) {
						Room r = (Room)room.getLocation(RoomDAO.getInstance().getSession());
						if (r.getParentRoom() != null && !r.getParentRoom().isIgnoreRoomCheck()) {
							for (Assignment a : r.getParentRoom().getCommitedAssignments()) {
			            		if (a.getClazz().isCancelled()) continue;
			            		if (assignment.getTime().overlaps(new ClassTimeInfo(a)) && !a.getClazz().canShareRoom(assignment.getClazz())) {
			            			if (change.getCurrent(a.getClassId())==null && change.getConflict(a.getClassId())==null)
			            				change.getConflicts().add(new ClassAssignment(a));
			            		}
			            	}
						}
						for (Room p: r.getPartitions()) {
							if (!p.isIgnoreRoomCheck())
								for (Assignment a : p.getCommitedAssignments()) {
				            		if (a.getClazz().isCancelled()) continue;
				            		if (assignment.getTime().overlaps(new ClassTimeInfo(a)) && !a.getClazz().canShareRoom(assignment.getClazz())) {
				            			if (change.getCurrent(a.getClassId())==null && change.getConflict(a.getClassId())==null)
				            				change.getConflicts().add(new ClassAssignment(a));
				            		}
				            	}
						}
					}
	            }
	            
	            // Check for instructor conflicts
	            if (assignment.getInstructors()!=null) for (ClassInstructorInfo instructor : assignment.getInstructors()) {
	            	if (!instructor.isLead()) continue;
	            	// check all departmental instructors with the same external id
	            	for (DepartmentalInstructor di: DepartmentalInstructor.getAllForInstructor(instructor.getInstructor().getInstructor())) {
		            	for (ClassInstructor ci : di.getClasses()) {
		            		if (ci.equals(instructor.getInstructor())) continue;
		            		if (!ci.isLead()) continue;
		            		Assignment a = ci.getClassInstructing().getCommittedAssignment();
		            		if (a == null || a.getClazz().isCancelled()) continue;
		            		if (assignment.getTime() != null && assignment.getTime().overlaps(new ClassTimeInfo(a)) && !a.getClazz().canShareInstructor(assignment.getClazz())) {
		            			if (change.getCurrent(a.getClassId())==null && change.getConflict(a.getClassId())==null)
		            				change.getConflicts().add(new ClassAssignment(a));
		            		}
		            	}
	            	}
	            	/*
	            	// Potential speed-up #1) only check the current department instructors
	            	for (ClassInstructor ci : instructor.getInstructor().getInstructor().getClasses()) {
	            		if (ci.equals(instructor.getInstructor())) continue;
	            		Assignment a = ci.getClassInstructing().getCommittedAssignment();
	            		if (a == null) continue;
	            		if (assignment.getTime().overlaps(new ClassTimeInfo(a))) {
	            			if (iChange.getCurrent(a.getClassId())==null && iChange.getConflict(a.getClassId())==null)
	            				iChange.getConflicts().add(new ClassAssignment(a));
	            		}
	            	}
	            	*/
	            	/*
	            	// Potential speed-up #2) use instructor assignments from the solution
	            	for (Assignment a : instructor.getInstructor().getInstructor().getCommitedAssignments()) {
	            		if (assignment.getTime().overlaps(new ClassTimeInfo(a))) {
	            			if (iChange.getCurrent(a.getClassId())==null && iChange.getConflict(a.getClassId())==null)
	            				iChange.getConflicts().add(new ClassAssignment(a));
	            		}
	            	}
	            	*/
	            }
        	}
            // Check the course structure for conflicts
            Class_ clazz = assignment.getClazz(Class_DAO.getInstance().getSession());
            // a) all parents
            Class_ parent = clazz.getParentClass();
            while (parent!=null) {
            	if (change.getCurrent(parent.getUniqueId())==null && change.getConflict(parent.getUniqueId())==null) {
            		Assignment a = parent.getCommittedAssignment();
            		if (a!=null && !a.getClazz().isCancelled() && assignment.getTime().overlaps(new ClassTimeInfo(a))) {
            			change.getConflicts().add(new ClassAssignment(a));
            		}
            	}
            	parent = parent.getParentClass();
            }
            // b) all children
            Queue<Class_> children = new LinkedList();
            try {
            	children.addAll(clazz.getChildClasses());
            } catch (LazyInitializationException e) {
            	sLog.error("This should never happen.");
            	Class_ c = Class_DAO.getInstance().get(assignment.getClassId());
            	children.addAll(c.getChildClasses());
            }
            Class_ child = null;
            while ((child=children.poll())!=null) {
            	if (change.getCurrent(child.getUniqueId())==null && change.getConflict(child.getUniqueId())==null) {
            		Assignment a = child.getCommittedAssignment();
            		if (a!=null && !a.getClazz().isCancelled() && assignment.getTime().overlaps(new ClassTimeInfo(a))) {
            			change.getConflicts().add(new ClassAssignment(a));
            		}
            	}
            	if (!child.getChildClasses().isEmpty())
            		children.addAll(child.getChildClasses());
            }
            // c) all single-class subparts
            for (Iterator i=clazz.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts().iterator(); i.hasNext();) {
            	SchedulingSubpart ss = (SchedulingSubpart)i.next();
            	if (ss.getClasses().size()==1) {
            		child = (Class_)ss.getClasses().iterator().next();
                	if (change.getCurrent(child.getUniqueId())==null && change.getConflict(child.getUniqueId())==null) {
                		Assignment a = child.getCommittedAssignment();
                		if (a!=null && !a.getClazz().isCancelled() && assignment.getTime().overlaps(new ClassTimeInfo(a))) {
                			change.getConflicts().add(new ClassAssignment(a));
                		}
                	}
                	if (!child.getChildClasses().isEmpty())
                		children.addAll(child.getChildClasses());
            	}
            }
                        
            //TODO: Check for other HARD conflicts (e.g., distribution constraints)
        }
    }
	
	public Collection<ClassAssignment> getDates(ClassInfo classInfo, boolean showStudentConflicts, boolean useRealStudents, Map<ClassAssignment, Set<Long>> conflicts) {
		Class_ clazz = classInfo.getClazz();
        DatePattern datePattern = clazz.effectiveDatePattern();
        if (datePattern == null) return null;
		List<ClassAssignment> dates = new ArrayList<ClassAssignment>();
        ClassTimeInfo time = (classInfo instanceof ClassAssignment ? ((ClassAssignment)classInfo).getTime() : null);
        if (datePattern.isPatternSet()) {
        	Set<DatePatternPref> datePatternPrefs = (Set<DatePatternPref>)clazz.effectivePreferences(DatePatternPref.class);
        	boolean hasReq = false;
        	for (DatePatternPref p: datePatternPrefs) {
        		if (PreferenceLevel.sRequired.equals(p.getPrefLevel().getPrefProlog())) { hasReq = true; break; }
        	}
        	for (DatePattern child: datePattern.findChildren()) {
        		String pr = PreferenceLevel.sNeutral;
        		for (DatePatternPref p: datePatternPrefs) {
        			if (p.getDatePattern().equals(child)) pr = p.getPrefLevel().getPrefProlog();
        		}
        		int prVal = 0;
        		if (!PreferenceLevel.sNeutral.equals(pr) && !PreferenceLevel.sRequired.equals(pr)) {
        			prVal = PreferenceLevel.prolog2int(pr);
        		}
    			if (hasReq && !PreferenceLevel.sRequired.equals(pr)) prVal += 100;
    			if (PreferenceLevel.sProhibited.equals(pr)) prVal += 100;
    			if (showStudentConflicts && time != null) {
        			dates.add(new ClassAssignmentInfo(
                			clazz,
                			time,
                			new ClassDateInfo(
                        			child.getUniqueId(),
                        			clazz.getUniqueId(),
                        			child.getName(),
                        			child.getPatternBitSet(),
                        			prVal),
                        	null,
                        	useRealStudents, conflicts));
    			} else {
        			dates.add(new ClassAssignment(
                			clazz,
                			null,
                			new ClassDateInfo(
                        			child.getUniqueId(),
                        			clazz.getUniqueId(),
                        			child.getName(),
                        			child.getPatternBitSet(),
                        			prVal),
                        	null));
    			}
        	}
        } else {
        	if (showStudentConflicts && time != null) {
            	dates.add(new ClassAssignmentInfo(
            			clazz,
            			time,
            			new ClassDateInfo(
                    			datePattern.getUniqueId(),
                    			clazz.getUniqueId(),
                    			datePattern.getName(),
                    			datePattern.getPatternBitSet(),
                    			PreferenceLevel.sIntLevelNeutral),
                    	null,
                    	useRealStudents, conflicts));
        	} else {
            	dates.add(new ClassAssignment(
            			clazz,
            			null,
            			new ClassDateInfo(
                    			datePattern.getUniqueId(),
                    			clazz.getUniqueId(),
                    			datePattern.getName(),
                    			datePattern.getPatternBitSet(),
                    			PreferenceLevel.sIntLevelNeutral),
                    	null));            		
        	}
        }
    	return dates;
    }
	
	public Collection<ClassAssignment> getTimes(ClassInfo classInfo, ClassDateInfo date, boolean showStudentConflicts, boolean useRealStudents, Map<ClassAssignment, Set<Long>> conflicts, ClassProposedChange proposed) {
		Class_ clazz = classInfo.getClazz();
        Set<TimePref> timePrefs = clazz.effectivePreferences(TimePref.class);
        if (timePrefs.isEmpty()) return null;
		if (date == null) {
			date = (classInfo instanceof ClassAssignment ? ((ClassAssignment)classInfo).getDate() : null);
			if (date == null) {
	        	Collection<ClassAssignment> dates = getDates(classInfo, showStudentConflicts, useRealStudents, conflicts);
	            if (dates != null && !dates.isEmpty())
	            	date = dates.iterator().next().getDate();
	        }
		}
		if (date == null) return null;
        DatePattern datePattern = date.getDatePattern();
        Vector<ClassAssignment> times = new Vector<ClassAssignment>();
        boolean onlyReq = false;
        for (Iterator i1=timePrefs.iterator();i1.hasNext();) {
        	TimePref timePref = (TimePref)i1.next();
        	TimePatternModel pattern = timePref.getTimePatternModel();
        	if (pattern.isExactTime() || pattern.countPreferences(PreferenceLevel.sRequired)>0)
        		onlyReq = true;
        }
        if (onlyReq) {
        	sLog.debug("Class "+classInfo.getClassName()+" has required times");
        }
		DurationModel dm = clazz.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
		EventDateMapping.Class2EventDateMap class2eventDates = EventDateMapping.getMapping(clazz.getSessionId());
        for (Iterator i1=timePrefs.iterator();i1.hasNext();) {
        	TimePref timePref = (TimePref)i1.next();
        	TimePatternModel pattern = timePref.getTimePatternModel();
        	if (pattern.isExactTime()) {
    			int minsPerMeeting = dm.getExactTimeMinutesPerMeeting(clazz.getSchedulingSubpart().getMinutesPerWk(), datePattern, pattern.getExactDays());
        		int length = ExactTimeMins.getNrSlotsPerMtg(minsPerMeeting);
        		int breakTime = ExactTimeMins.getBreakTime(minsPerMeeting); 
        		List<Date> dates = dm.getDates(clazz.getSchedulingSubpart().getMinutesPerWk(), datePattern, pattern.getExactDays(), minsPerMeeting, class2eventDates);
        		ClassTimeInfo time = new ClassTimeInfo(clazz.getUniqueId(), pattern.getExactDays(),pattern.getExactStartSlot(),length,minsPerMeeting,PreferenceLevel.sIntLevelNeutral,timePref.getTimePattern(),date,breakTime,dates);
        		if (showStudentConflicts)
        			times.add(new ClassAssignmentInfo(clazz, time, date, null, (proposed == null ? null : proposed.getAssignmentTable()), useRealStudents, conflicts));
        		else
        			times.add(new ClassAssignment(clazz, time, date, null));
                continue;
        	}

            for (int time=0;time<pattern.getNrTimes(); time++) {
            	times: for (int day=0;day<pattern.getNrDays(); day++) {
            		if (!dm.isValidSelection(clazz.getSchedulingSubpart().getMinutesPerWk(), datePattern, timePref.getTimePattern(), pattern.getDayCode(day)))
            			continue;
                    String pref = pattern.getPreference(day,time);
                    if (onlyReq && !pref.equals(PreferenceLevel.sRequired)) {
                        pref = PreferenceLevel.sProhibited;
                    }
                    List<Date> dates = dm.getDates(clazz.getSchedulingSubpart().getMinutesPerWk(), datePattern, pattern.getDayCode(day), timePref.getTimePattern().getMinPerMtg(), class2eventDates);
                    ClassTimeInfo loc = new ClassTimeInfo(
                            clazz.getUniqueId(),
                            pattern.getDayCode(day),
                            pattern.getStartSlot(time),
                            pattern.getSlotsPerMtg(),
                            timePref.getTimePattern().getMinPerMtg(),
                            PreferenceLevel.prolog2int(pref),
                            timePref.getTimePattern(),
                            date,
                            pattern.getBreakTime(),
                            dates);
                    
                    if (proposed!=null) {
                        for (ClassAssignment current : proposed.getAssignments()) {
                        	if (!current.getClassId().equals(classInfo.getClassId())) {
                        		boolean canConflict = false;
                        		if (current.getParents().contains(classInfo.getClassId())) canConflict = true;
                        		if (classInfo.getParents().contains(current.getClassId())) canConflict = true;
                        		if (current.getConfligId().equals(classInfo.getConfligId()) && current.isSingleClass()) canConflict = true;
                        		if (current.shareInstructor(classInfo)) canConflict = true;
                        		if (canConflict && loc.overlaps(current.getTime())) continue times;
                        	}
                        }
                    }
                    
                    if (showStudentConflicts)
                    	 times.add(new ClassAssignmentInfo(clazz, loc, date, null, (proposed==null?null:proposed.getAssignmentTable()), useRealStudents, conflicts));
                    else
                    	times.add(new ClassAssignment(clazz, loc, date, null));
                }
            }
        }
        
        Date[] bounds = DatePattern.getBounds(clazz.getSessionId());
        boolean changePast = ApplicationProperty.ClassAssignmentChangePastMeetings.isTrue();
        boolean ignorePast = ApplicationProperty.ClassAssignmentIgnorePastMeetings.isTrue();
        Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date today = cal.getTime();
		String nameFormat = "last-first";
        if (RoomAvailability.getInstance()!=null) {
        	for (ClassInstructor ci: clazz.getClassInstructors()) {
        		if (!ci.getLead()) continue;
        		Collection<TimeBlock> blocks = RoomAvailability.getInstance().getInstructorAvailability(ci.getInstructor().getUniqueId(), 
        				bounds[0], bounds[1], 
        				RoomAvailabilityInterface.sClassType);
        		if (blocks != null && !blocks.isEmpty()) {
        			Collection<TimeBlock> timesToCheck = null;
            		if (!changePast || ignorePast) {
            			timesToCheck = new Vector();
            			for (TimeBlock time: blocks) {
            				if (!time.getEndTime().before(today))
            					timesToCheck.add(time);
            			}
            		} else {
            			timesToCheck = blocks;
            		}
            		for (Iterator<ClassAssignment> i = times.iterator(); i.hasNext(); ) {
            			ClassAssignment ca = i.next();
            			TimeBlock time = ca.getTime().overlaps(timesToCheck);
                		if (time!=null) {
            				sLog.info(MSG.messageInstructroNotAvailable(ci.getInstructor().getName(nameFormat), ca.getTime().getLongName(), time.getEventName()));
            				i.remove();
            			}
            		}
        		}
        	}
        }
        for (ClassInstructor ci: clazz.getClassInstructors()) {
    		if (!ci.getLead() || !ci.getInstructor().hasUnavailabilities()) continue;
    		Collection<TimeBlock> blocks = ci.getInstructor().listUnavailableDays();
    		if (blocks != null && !blocks.isEmpty()) {
    			Collection<TimeBlock> timesToCheck = null;
        		if (!changePast || ignorePast) {
        			timesToCheck = new Vector();
        			for (TimeBlock time: blocks) {
        				if (!time.getEndTime().before(today))
        					timesToCheck.add(time);
        			}
        		} else {
        			timesToCheck = blocks;
        		}
        		for (Iterator<ClassAssignment> i = times.iterator(); i.hasNext(); ) {
        			ClassAssignment ca = i.next();
        			TimeBlock time = ca.getTime().overlaps(timesToCheck);
            		if (time!=null) {
        				sLog.info(MSG.messageInstructroNotAvailable(ci.getInstructor().getName(nameFormat), ca.getTime().getLongName(), time.getEventName()));
        				i.remove();
        			}
        		}
    		}
        }
        return times;
	}
	
	public Collection<ClassRoomInfo> getRooms(ClassInfo classInfo, ClassTimeInfo period, boolean allowConflicts, RoomFilterRpcRequest filter, SessionContext context, ClassProposedChange proposed, final RoomOrder ord) {
		Class_ clazz = classInfo.getClazz();
		Long departmentId = clazz.getManagingDept().getUniqueId();
		if (clazz.getNbrRooms() == null || clazz.getNbrRooms() == 0) return null;
		List<ClassRoomInfo> rooms = new ArrayList<ClassRoomInfo>();
		
		List<Location> allRooms = new RoomFilterBackend().locations(
				context.getUser().getCurrentAcademicSessionId(), filter, -1,
				new HashMap<Long, Double>(), new EventContext(context, context.getUser().getCurrentAcademicSessionId()));
		Set<Location> availRooms = clazz.getAvailableRooms();
		
    	int minClassLimit = clazz.getExpectedCapacity().intValue();
    	int maxClassLimit = clazz.getMaxExpectedCapacity().intValue();
    	if (maxClassLimit<minClassLimit) maxClassLimit = minClassLimit;
    	float room2limitRatio = clazz.getRoomRatio().floatValue();
    	int roomCapacity = Math.round(minClassLimit<=0?room2limitRatio:room2limitRatio*minClassLimit);
    	//TODO: Use parameters from the default solver configuration
        int discouragedCapacity = (int)Math.round(0.99 * roomCapacity);
        int stronglyDiscouragedCapacity = (int)Math.round(0.98 * roomCapacity);
		
        Set<RoomGroupPref> groupPrefs = clazz.effectivePreferences(RoomGroupPref.class);
        Set<RoomPref> roomPrefs = clazz.effectivePreferences(RoomPref.class);
        Set<BuildingPref> bldgPrefs = clazz.effectivePreferences(BuildingPref.class);
        Set<RoomFeaturePref> featurePrefs = clazz.effectivePreferences(RoomFeaturePref.class);
        
        Hashtable<Location, Integer> filteredRooms = new Hashtable<Location,Integer>();
        Set<Long> permIds = new HashSet<Long>();

		rooms: for (Location room: allRooms) {
			PreferenceCombination pref = new SumPreferenceCombination();

    		if (!availRooms.contains(room)) pref.addPreferenceProlog(PreferenceLevel.sProhibited);
    		
    		RoomSharingModel sharingModel = room.getRoomSharingModel();
            if (sharingModel!=null) {
            	sharing: for (int d = 0; d<Constants.NR_DAYS; d++) {
            		if ((Constants.DAY_CODES[d] & period.getDayCode())==0) continue;
            		int startTime = period.getStartSlot();
            		int endTime = (period.getStartSlot()+period.getLength()-1);
            		for (int t = startTime; t<=endTime; t++) {
            			Long px = Long.valueOf(sharingModel.getPreference(d,t));
            			if (px.equals(RoomSharingModel.sNotAvailablePref)) {
            				if (allowConflicts) {
            					pref.addPreferenceProlog(PreferenceLevel.sProhibited);
            					break sharing;
            				} else {
            					if (allRooms.size() == 1) throw new GwtRpcException("Room "+room.getLabel()+" is not available for "+period.getLongName()+" due to the room sharing preferences.");
                				continue rooms;
            				}
            			}
            			if (px.equals(RoomSharingModel.sFreeForAllPref)) continue;
            			if (!departmentId.equals(px)) {
            				if (allowConflicts) {
            					pref.addPreferenceProlog(PreferenceLevel.sProhibited);
            					break sharing;
            				} else {
            					if (allRooms.size() == 1) throw new GwtRpcException("Room "+room.getLabel()+" is not available for "+period.getLongName()+" due to the room sharing preferences.");
            					continue rooms;
            				}
                        }
                    }
                }
            }

    		
    		// --- group preference ----------
    		PreferenceCombination groupPref = PreferenceCombination.getDefault();
    		boolean reqGroup = false;
    		for (RoomGroupPref p: groupPrefs) {
    			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) reqGroup = true;
    			if (p.getRoomGroup().getRooms().contains(room)) groupPref.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
    		}
    		if (reqGroup) {
    			if (!PreferenceLevel.sRequired.equals(groupPref.getPreferenceProlog()))
    				pref.addPreferenceProlog(PreferenceLevel.sProhibited);
    		} else {
        		pref.addPreferenceProlog(groupPref.getPreferenceProlog());
    		}
    			
            
            // --- room preference ------------
    		String roomPref = null;
    		PreferenceLevel roomPreference = null;
    		for (RoomPref rp: clazz.getManagingDept().getPreferences(RoomPref.class))
    			if (room.equals(rp.getRoom())) {
    				roomPreference = rp.getPrefLevel(); break;
    			}
    		if (roomPreference!=null) {
    			roomPref = roomPreference.getPrefProlog();
			}
			boolean reqRoom = false;
			for (RoomPref p: roomPrefs) {
    			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) reqRoom = true;
    			if (room.equals(p.getRoom())) roomPref = p.getPrefLevel().getPrefProlog();
    		}
			if (reqRoom) {
				if (!PreferenceLevel.sRequired.equals(roomPref))
					pref.addPreferenceProlog(PreferenceLevel.sProhibited);
			} else if (roomPref != null) {
				pref.addPreferenceProlog(roomPref);
			}
			
            // --- building preference ------------
    		Building bldg = (room instanceof Room ? ((Room)room).getBuilding() : null);
    		boolean reqBldg = false;
    		String bldgPref = null;
    		for (BuildingPref p: bldgPrefs) {
    			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) reqBldg = true;
    			if (bldg!=null && bldg.equals(p.getBuilding())) bldgPref = p.getPrefLevel().getPrefProlog();
    		}
    		if (reqBldg) {
    			if (!PreferenceLevel.sRequired.equals(bldgPref))
    				pref.addPreferenceProlog(PreferenceLevel.sProhibited);
    		} else if (bldgPref != null) {
    			pref.addPreferenceProlog(bldgPref);
    		}
            
            // --- room features preference --------  
            boolean acceptableFeatures = true;
            PreferenceCombination featurePref = new MinMaxPreferenceCombination();
            for (RoomFeaturePref roomFeaturePref: featurePrefs) {
            	RoomFeature feature = roomFeaturePref.getRoomFeature();
            	String p = roomFeaturePref.getPrefLevel().getPrefProlog();
            	
            	boolean hasFeature = feature.getRooms().contains(room);
                if (p.equals(PreferenceLevel.sProhibited) && hasFeature) {
                    acceptableFeatures=false;
                }
                if (p.equals(PreferenceLevel.sRequired) && !hasFeature) {
                    acceptableFeatures=false;
                }
                if (p!=null && hasFeature && !p.equals(PreferenceLevel.sProhibited) && !p.equals(PreferenceLevel.sRequired)) 
                	featurePref.addPreferenceProlog(p);
            }
            pref.addPreferenceInt(featurePref.getPreferenceInt());
            if (!acceptableFeatures)
              	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
            
            
    		// --- room size -----------------
            if (clazz.getNbrRooms()>1 && Boolean.TRUE.equals(clazz.isRoomsSplitAttendance())) {
            	// split attendance -> skip room check
            } else {
                if (room.getCapacity().intValue()<stronglyDiscouragedCapacity) {
              		pref.addPreferenceInt(1000);
                }
                else if (room.getCapacity().intValue()<discouragedCapacity) {
                    pref.addPreferenceProlog(PreferenceLevel.sStronglyDiscouraged);
                }
                else if (room.getCapacity().intValue()<roomCapacity) {
                	pref.addPreferenceProlog(PreferenceLevel.sDiscouraged);
                }
            }
            
            int prefInt = pref.getPreferenceInt();
    		
            filteredRooms.put(room, prefInt);
            permIds.add(room.getPermanentId());
		}
		
		boolean changePast = ApplicationProperty.ClassAssignmentChangePastMeetings.isTrue();
		boolean ignorePast = ApplicationProperty.ClassAssignmentIgnorePastMeetings.isTrue();
		boolean includeSuffix = ApplicationProperty.SolverShowClassSufix.isTrue();
		boolean includeConfig = ApplicationProperty.SolverShowConfiguratioName.isTrue();
		
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date today = cal.getTime();
		
		List<Date> datesToCheck = null;
		if (ignorePast || !changePast) {
			datesToCheck = new ArrayList<Date>();
 			for(Date aDate : period.getDates()){
 				if (aDate.compareTo(today) > 0)
 					datesToCheck.add(aDate);
 			}
		} else {
			datesToCheck = period.getDates();
		}
        Hashtable<Long,Set<Long>> room2classIds = Location.findClassLocationTable(clazz.getSessionId(), permIds, period.getStartSlot(), period.getLength(),
        		changePast ? period.getDates() : datesToCheck);
        
        Hashtable<Long,Set<Event>> room2events = null;
        if (RoomAvailability.getInstance()!=null && RoomAvailability.getInstance() instanceof DefaultRoomAvailabilityService) {
        	room2events = Location.findEventTable(clazz.getSessionId(), permIds, period.getStartSlot(), period.getLength(), datesToCheck);
        }

        ClassAssignmentInfo classAssigment = null;
        if ((proposed == null || proposed.getConflict(classInfo) == null) && classInfo instanceof ClassAssignmentInfo)
        	classAssigment = (ClassAssignmentInfo)classInfo;
        Date[] bounds = DatePattern.getBounds(clazz.getSessionId());
        
        rooms: for (Map.Entry<Location, Integer> entry: filteredRooms.entrySet()) {
			Location room = entry.getKey();
			int prefInt = entry.getValue();
			String note = null;
			
			if (room.isIgnoreRoomCheck()) {
				rooms.add(new ClassRoomInfo(room, prefInt, note));
				continue;
			}
			
			Set<Long> classIds = room2classIds.get(room.getPermanentId());
			if (classIds==null) classIds = new HashSet();
			
			// Fix the location table with the current assignment
			
			if (classAssigment!=null && classAssigment.hasRoom(room.getUniqueId()) && classAssigment.getTime().overlaps(period))
				classIds.remove(classAssigment.getClassId());
			if (proposed!=null) {
	            for (ClassAssignment conflict : proposed.getConflicts()) {
	            	if (conflict.hasRoom(room.getUniqueId()) && conflict.getTime().overlaps(period))
	            		classIds.remove(conflict.getClassId());
	            }
	            for (ClassAssignment current : proposed.getAssignments()) {
	            	ClassAssignment initial = proposed.getInitial(current);
	                if (initial!=null && initial.hasRoom(room.getUniqueId()) && initial.getTime().overlaps(period))
	                	classIds.remove(initial.getClassId());
	            }
	            for (ClassAssignment current : proposed.getAssignments()) {
	                if (!classInfo.getClassId().equals(current.getClassId()) && current.hasRoom(room.getUniqueId()) && current.getTime().overlaps(period))
	                	classIds.add(current.getClassId());
	            }				
			}

	        if (!allowConflicts && classIds!=null && !classIds.isEmpty()) {
	        	for (Long classId: classIds) {
	        		if (!clazz.canShareRoom(classId)) {
	        			if (allRooms.size() == 1) throw new GwtRpcException(MSG.messageRoomNotAvailable(room.getLabel(), period.getLongName(), Class_DAO.getInstance().get(classId).getClassLabel(includeSuffix, includeConfig)));
	        			continue rooms;
	        		}
	        	}
	        }
	        if (allowConflicts && classIds!=null && !classIds.isEmpty()) {
	        	for (Long classId: classIds) {
	        		if (!clazz.canShareRoom(classId)) {
	        			prefInt += 10000;
	        			note = "Conflicts with " + Class_DAO.getInstance().get(classId).getClassLabel(includeSuffix, includeConfig);
	        			break;
	        		}
	        	}
	        }
	        if (classIds!=null && proposed!=null) {
	        	for (Long classId: classIds) {
	        		if (proposed.getCurrent(classId)!=null && !clazz.canShareRoom(classId)) {
	        			if (allRooms.size() == 1) throw new GwtRpcException(MSG.messageRoomNotAvailable(room.getLabel(), period.getLongName(), Class_DAO.getInstance().get(classId).getClassLabel(includeSuffix, includeConfig)));
	                	continue rooms;
	        		}
	        	}
	        }

	        if (room2events!=null) {
	        	Set<Event> conflicts = room2events.get(room.getPermanentId());
	        	if (conflicts!=null && !conflicts.isEmpty()) {
					sLog.info(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), conflicts.iterator().next().getEventName()));
					if (allRooms.size() == 1) throw new GwtRpcException(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), conflicts.iterator().next().getEventName()));
					continue rooms;
	        	}
	        } else if (RoomAvailability.getInstance()!=null) {
	    		Collection<TimeBlock> times = RoomAvailability.getInstance().getRoomAvailability(
	                    room.getUniqueId(),
	                    bounds[0], bounds[1], 
	                    RoomAvailabilityInterface.sClassType);
	    		if (times != null && !times.isEmpty()) {
	        		Collection<TimeBlock> timesToCheck = null;
	        		if (!changePast || ignorePast) {
	        			timesToCheck = new Vector();
	        			for (TimeBlock time: times) {
	        				if (!time.getEndTime().before(today))
	        					timesToCheck.add(time);
	        			}
	        		} else {
	        			timesToCheck = times;
	        		}
	        		TimeBlock time = period.overlaps(timesToCheck);
	        		if (time!=null) {
	        			if (allRooms.size() == 1) throw new GwtRpcException(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), time.getEventName()));
	    				sLog.info(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), time.getEventName()));
	    				continue rooms;
	        		}
	    		}
	        }
        
	        if (room instanceof Room) {
	        	Room r = (Room)room;
	        	if (r.getParentRoom() != null && !r.getParentRoom().isIgnoreRoomCheck()) {
	        		if (room2events!=null) {
	                	Set<Event> conflicts = room2events.get(r.getParentRoom().getPermanentId());
	                	if (conflicts!=null && !conflicts.isEmpty()) {
	                		if (allRooms.size() == 1) throw new GwtRpcException(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), conflicts.iterator().next().getEventName()));
	        				sLog.info(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), conflicts.iterator().next().getEventName()));
	        				continue rooms;
	                	}
	                } else if (RoomAvailability.getInstance()!=null) {
	            		Collection<TimeBlock> times = RoomAvailability.getInstance().getRoomAvailability(
	            				r.getParentRoom().getUniqueId(),
	                            bounds[0], bounds[1], 
	                            RoomAvailabilityInterface.sClassType);
	            		if (times != null && !times.isEmpty()) {
	                		Collection<TimeBlock> timesToCheck = null;
	                		if (!changePast || ignorePast) {
	                			timesToCheck = new Vector();
	                			for (TimeBlock time: times) {
	                				if (!time.getEndTime().before(today))
	                					timesToCheck.add(time);
	                			}
	                		} else {
	                			timesToCheck = times;
	                		}
	                		TimeBlock time = period.overlaps(timesToCheck);
	                		if (time!=null) {
	                			if (allRooms.size() == 1) throw new GwtRpcException(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), time.getEventName()));
	            				sLog.info(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), time.getEventName()));
	            				continue rooms;
	                		}
	            		}
	                }
	        	}
	        	for (Room p: r.getPartitions()) {
	        		if (p.isIgnoreRoomCheck()) continue;
	        		if (room2events!=null) {
	                	Set<Event> conflicts = room2events.get(p.getPermanentId());
	                	if (conflicts!=null && !conflicts.isEmpty()) {
	                		if (allRooms.size() == 1) throw new GwtRpcException(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), conflicts.iterator().next().getEventName()));
	        				sLog.info(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), conflicts.iterator().next().getEventName()));
	        				continue rooms;
	                	}
	                } else if (RoomAvailability.getInstance()!=null) {
	            		Collection<TimeBlock> times = RoomAvailability.getInstance().getRoomAvailability(
	            				p.getUniqueId(),
	                            bounds[0], bounds[1], 
	                            RoomAvailabilityInterface.sClassType);
	            		if (times != null && !times.isEmpty()) {
	                		Collection<TimeBlock> timesToCheck = null;
	                		if (!changePast || ignorePast) {
	                			timesToCheck = new Vector<TimeBlock>();
	                			for (TimeBlock time: times) {
	                				if (!time.getEndTime().before(today))
	                					timesToCheck.add(time);
	                			}
	                		} else {
	                			timesToCheck = times;
	                		}
	                		TimeBlock time = period.overlaps(timesToCheck);
	                		if (time!=null) {
	                			if (allRooms.size() == 1) throw new GwtRpcException(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), time.getEventName()));
	            				sLog.info(MSG.messageRoomNotAvailable2(room.getLabel(), period.getLongName(), time.getEventName()));
	            				continue rooms;
	                		}
	            		}
	                }
	        	}
	        	
	        }
        	rooms.add(new ClassRoomInfo(room, prefInt, note));
        }
        
        Collections.sort(rooms, new Comparator<ClassRoomInfo>() {
			@Override
			public int compare(ClassRoomInfo r1, ClassRoomInfo r2) {
				switch (ord) {
				case NAME_ASC:
					return NaturalOrderComparator.compare(r1.getName(), r2.getName());
				case NAME_DESC:
					return NaturalOrderComparator.compare(r2.getName(), r1.getName());
				case SIZE_ASC:
					if (r1.getCapacity() != r2.getCapacity())
						return r1.getCapacity() < r2.getCapacity() ? -1 : 1;
					return NaturalOrderComparator.compare(r1.getName(), r2.getName());
				case SIZE_DESC:
					if (r1.getCapacity() != r2.getCapacity())
						return r1.getCapacity() > r2.getCapacity() ? -1 : 1;
					return NaturalOrderComparator.compare(r1.getName(), r2.getName());
				default:
					return NaturalOrderComparator.compare(r1.getName(), r2.getName());
				}
			}
		});
		return rooms;
	}
	
	public boolean getCanAssign(ClassProposedChange proposed, SessionContext context) {
        if (proposed==null) return false;
        for (ClassAssignment assignment : proposed.getAssignments()) {
            if (!assignment.isValid()) return false;
            if (!context.hasPermission(assignment.getClazz(), Right.ClassAssignment)) return false; 
        }
        for (ClassAssignment assignment : proposed.getConflicts()) {
        	if (!context.hasPermission(assignment.getClazz(), Right.ClassAssignment)) return false;
        }
        if (ApplicationProperty.ClassAssignmentAllowUnassignments.isFalse() && !proposed.getConflicts().isEmpty()) return false;
        return true;
    }
	
	public String assign(ClassProposedChange proposed, SessionContext context) {
        if (proposed==null) return MSG.errorNothingToAssign();
        if (ApplicationProperty.ClassAssignmentAllowUnassignments.isFalse() && !proposed.getConflicts().isEmpty())
        	return MSG.errorNotAllowedToKeepClassUnassigned();
        sLog.info("About to be assigned: "+proposed);
        org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
        String message = null;
        Map<Long, List<Long>> touchedOfferingIds = new Hashtable<Long, List<Long>>();
        for (ClassAssignment assignment : proposed.getConflicts()) {
        	try {
                Class_ clazz = assignment.getClazz(hibSession);
        		String m = clazz.unassignCommited(context.getUser(), hibSession);
                if (m!=null) message = (message==null?"":message+"\n")+m;
                Long offeringId = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId();
                List<Long> classIds = touchedOfferingIds.get(offeringId);
                if (classIds == null) {
                	classIds = new ArrayList<Long>();
                	touchedOfferingIds.put(offeringId, classIds);
                }
                classIds.add(clazz.getUniqueId());
            } catch (Exception e) {
                message = (message==null?"":message+"\n")+MSG.errorUnassignmentFailed(assignment.getClassName(), e.getMessage());
            }
        }
        for (ClassAssignmentInfo assignment : proposed.getAssignments()) {
            try {
                Class_ clazz = assignment.getClazz(hibSession);
                String m = clazz.assignCommited(assignment, context.getUser(), hibSession);
                if (m!=null) message = (message==null?"":message+"\n")+m;
                Long offeringId = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId();
                List<Long> classIds = touchedOfferingIds.get(offeringId);
                if (classIds == null) {
                	classIds = new ArrayList<Long>();
                	touchedOfferingIds.put(offeringId, classIds);
                }
                classIds.add(clazz.getUniqueId());
            } catch (Exception e) {
                message = (message==null?"":message+"\n")+MSG.errorAssignmentFailed(assignment.getClassName(), assignment.getTime().getName()+" "+assignment.getRoomNames(", "), e.getMessage());
            }
        }
        
        Long sessionId = context.getUser().getCurrentAcademicSessionId();
        Session session = SessionDAO.getInstance().get(sessionId, hibSession);
        if (!session.getStatusType().isTestSession()) {
            if (session.getStatusType().canOnlineSectionStudents()) {
            	List<Long> unlockedOfferings = new ArrayList<Long>();
            	for (Long offeringId: touchedOfferingIds.keySet())
            		if (!session.isOfferingLocked(offeringId))
            			unlockedOfferings.add(offeringId);
            	if (!unlockedOfferings.isEmpty())
            		StudentSectioningQueue.offeringChanged(hibSession, context.getUser(), sessionId, unlockedOfferings);
            } else if (session.getStatusType().canSectionAssistStudents()) {
            	for (Map.Entry<Long, List<Long>> entry: touchedOfferingIds.entrySet()) {
            		if (!session.isOfferingLocked(entry.getKey()))
            			StudentSectioningQueue.classAssignmentChanged(hibSession, context.getUser(), sessionId, entry.getValue());        		
            	}
            }
        }
        hibSession.flush();
        
        return message;
    }

}
