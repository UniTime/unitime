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
package org.unitime.timetable.server.exams;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.events.RoomFilterBackend.LocationMatcher;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.RoomAvailabilityRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.RoomAvailabilityResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.RoomAvailability;

@GwtRpcImplements(RoomAvailabilityRequest.class)
public class RoomAvailabilityBackend implements GwtRpcImplementation<RoomAvailabilityRequest, RoomAvailabilityResponse>{
	protected static ExaminationMessages MESSAGES = Localization.create(ExaminationMessages.class);
	protected static GwtMessages GWT = Localization.create(GwtMessages.class);
	protected static GwtConstants GWT_CONST = Localization.create(GwtConstants.class);
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	@Override
	public RoomAvailabilityResponse execute(RoomAvailabilityRequest request, SessionContext context) {
		context.checkPermission(Right.RoomAvailability);
		Long sessionId = context.getUser().getCurrentAcademicSessionId();
		
		String examType = request.getFilter().getParameterValue("examType");
		ExamType type = null;
		try {
			type = ExamTypeDAO.getInstance().get(Long.valueOf(examType));
		} catch (Exception e) {}
		if (type == null)
			type = ExamType.findByReference(examType);
		if (type == null)
			throw new GwtRpcException(MESSAGES.messageNoExamType());
		
		for (FilterParameterInterface p: request.getFilter().getParameters()) {
			if ("examType".equals(p.getName())) {
				context.setAttribute(SessionAttribute.ExamType, Long.valueOf(p.getValue() != null ? p.getValue() : p.getDefaultValue()));
			} else if (p.getValue() != null) {
				context.getUser().setProperty("RoomAvailability." + p.getName(), p.getValue());
			}
		}
		
		boolean compare = "1".equals(request.getFilter().getParameterValue("compare", "0"));
		boolean includeExams = "1".equals(request.getFilter().getParameterValue("includeExams", "0"));

		RoomAvailabilityResponse table = new RoomAvailabilityResponse();
		table.setId("RoomAvailability");
		table.setName(MESSAGES.sectRoomAvailability());
		LineInterface header = table.addHeader();
		if (compare) {
			header.addCell(MESSAGES.colRoom());
			header.addCell(MESSAGES.colRoomCapacity()).setTextAlignment(Alignment.RIGHT);
			header.addCell(MESSAGES.colExaminationCapacity()).setTextAlignment(Alignment.RIGHT);
			header.addCell(MESSAGES.colExamination());
			header.addCell(MESSAGES.colExaminationDate());
			header.addCell(MESSAGES.colExaminationTime());
			header.addCell(MESSAGES.colEvent());
			header.addCell(MESSAGES.colEventDate());
			header.addCell(MESSAGES.colEventTime());
		} else {
			header.addCell(MESSAGES.colRoom());
			header.addCell(MESSAGES.colRoomCapacity()).setTextAlignment(Alignment.RIGHT);
			header.addCell(MESSAGES.colExaminationCapacity()).setTextAlignment(Alignment.RIGHT);
			header.addCell(MESSAGES.colEvent());
			header.addCell(MESSAGES.colEventType());
			header.addCell(MESSAGES.colDate());
			header.addCell(MESSAGES.colStartTime());
			header.addCell(MESSAGES.colEndTime());
		}
		for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
		table.setBlankWhenSame(true);
		
		TreeSet<ExamPeriod> periods = ExamPeriod.findAll(sessionId, type);
		if (periods.isEmpty()) {
			table.setErrorMessage(MESSAGES.warnNoExaminationPeriods());
			return table;
		}
		
        Date[] bounds = ExamPeriod.getBounds(SessionDAO.getInstance().get(sessionId), type.getUniqueId());
        Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_MEETING);
        Formats.Format<Date> timeFormat = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
		
		Query roomFilter = null;
		Set<String> featureTypes = null;
		String rf = request.getFilter().getParameterValue("roomFilter", "");
		if (rf != null && !rf.isEmpty()) {
			roomFilter = new Query(rf);
			featureTypes = new HashSet<String>();
			for (RoomFeatureType ft: RoomFeatureTypeDAO.getInstance().findAll())
				featureTypes.add(ft.getReference().toLowerCase().replace(' ', '_'));
		}
		
        RoomAvailabilityInterface ra = RoomAvailability.getInstance();
        if (ra==null) return table;
    	String exclude = (includeExams? null :
    		(type.getType()==ExamType.sExamTypeFinal?RoomAvailabilityInterface.sFinalExamType:RoomAvailabilityInterface.sMidtermExamType));
    	
    	ExamAssignmentProxy examAssignment = examinationSolverService.getSolver();
    	if (examAssignment != null && !examAssignment.getExamTypeId().equals(type.getUniqueId())) examAssignment = null;
    	String eventType = (type.getType() == ExamType.sExamTypeFinal ? RoomAvailabilityInterface.sFinalExamType : RoomAvailabilityInterface.sMidtermExamType);
        
    	ra.activate(sessionId, bounds[0], bounds[1], exclude, true);
        for (Location location: Location.findAllExamLocations(sessionId, type)) {
        	if (roomFilter != null && !roomFilter.match(new LocationMatcher(location, featureTypes))) continue;
        	Collection<TimeBlock> events = ra.getRoomAvailability(location.getUniqueId(), bounds[0], bounds[1], exclude);
            if (compare) {
        		TreeSet<ExamAssignment> exams = null;
                if (examAssignment!=null)
                    exams = examAssignment.getExamsOfRoom(location.getUniqueId());
                else {
                    exams = new TreeSet<ExamAssignment>();
                    for (Exam x: ExamDAO.getInstance().getSession().createQuery(
                            "select x from Exam x inner join x.assignedRooms r where x.examType.uniqueId=:examTypeId and r.uniqueId=:locationId", Exam.class).
                            setParameter("examTypeId", type.getUniqueId()).
                            setParameter("locationId", location.getUniqueId()).
                            setCacheable(true).
                            list()) {
                        exams.add(new ExamAssignment(x));
                    }
                }
                if (exams == null) exams = new TreeSet<ExamAssignment>();
                if (events == null) events = new TreeSet<TimeBlock>();
                Hashtable<TimeBlock,ExamAssignment> mapping = new Hashtable<TimeBlock, ExamAssignment>();
                for (TimeBlock event : events) {
                    if (!eventType.equals(event.getEventType())) continue;
                    ExamAssignment match = null;
                    for (ExamAssignment exam : exams) {
                        if (event.getEventName().trim().equalsIgnoreCase(exam.getExamName().trim()) && exam.getPeriod().overlap(event)) { match = exam; break; }
                    }
                    if (match!=null) {
                        mapping.put(event, match); exams.remove(match);
                    }
                }
                for (TimeBlock event : events) {
                    if (!eventType.equals(event.getEventType())) continue;
                    ExamAssignment match = null;
                    for (ExamAssignment exam : exams) {
                        if (event.getEventName().trim().equalsIgnoreCase(exam.getExamName().trim())) { match = exam; break; }
                    }
                    if (match!=null) {
                        mapping.put(event, match); exams.remove(match);
                    }
                }
                for (TimeBlock event : events) {
                    if (!eventType.equals(event.getEventType())) continue;
                    ExamAssignment match = null;
                    for (ExamAssignment exam : exams) {
                        if (exam.getPeriod().overlap(event)) { match = exam; break; }
                    }
                    if (match!=null) {
                        mapping.put(event, match); exams.remove(match);
                    }
                }
                for (TimeBlock event : events) {
                    if (!eventType.equals(event.getEventType())) continue;
                    ExamAssignment match = mapping.get(event);
                    if (match == null) {
                    	LineInterface line = table.addLine();
    	                line.addCell(location.getLabel())
	                		.setComparable(location.getLabel(), event.getStartTime());
    	                line.addCell(location.getCapacity().toString())
    	                	.setTextAlignment(Alignment.RIGHT)
	                		.setComparable(-location.getCapacity(), event.getStartTime());
    	                line.addCell(location.getExamCapacity().toString())
    	                	.setTextAlignment(Alignment.RIGHT)
	                		.setComparable(-location.getExamCapacity(), event.getStartTime());
    	                line.addCell("")
    	                	.setComparable("", location.getLabel(), new Date(0));
    	                line.addCell("")
	                		.setComparable(new Date(0), location.getLabel());
    	                line.addCell("")
                			.setComparable(0, location.getLabel());
    	                line.addCell(event.getEventName())
    	                	.addStyle("background-color:yellow;")
	                		.setComparable(event.getEventName(), location.getLabel(), event.getStartTime());
    	                line.addCell(dateFormat.format(event.getStartTime()))
    	                	.addStyle("background-color:yellow;")
    	                	.setComparable(event.getStartTime(), location.getLabel());
    	                line.addCell(
    	                		timeFormat.format(event.getStartTime()).replaceAll("AM", GWT_CONST.timeShortAm()).replaceAll("PM", GWT_CONST.timeShortPm())
    	                		+ " - " +
    	                		timeFormat.format(event.getEndTime()).replaceAll("AM", GWT_CONST.timeShortAm()).replaceAll("PM", GWT_CONST.timeShortPm()))
    	                	.addStyle("background-color:yellow;")
    	                	.setComparable(event.getStartTime().getTime() % 86400000,location.getLabel());
                    } else {
                    	Calendar c = Calendar.getInstance(); 
                        c.setTime(match.getPeriod().getStartTime()); 
                        c.add(Calendar.MINUTE, match.getPrintOffset());
                        Date startTime = c.getTime();
                        c.add(Calendar.MINUTE, match.getLength());
                        Date endTime = c.getTime();
                        boolean nameMatch = event.getEventName().trim().equalsIgnoreCase(match.getExamName().trim());
                        boolean dateMatch = dateFormat.format(event.getStartTime()).equals(dateFormat.format(match.getPeriod().getStartDate()));
                        Date start = event.getStartTime();
                        int breakTimeStart = match.getPeriod().getEventStartOffset().intValue() * Constants.SLOT_LENGTH_MIN;
                        c = Calendar.getInstance(Locale.US); 
                        c.setTime(start);
                        c.add(Calendar.MINUTE, breakTimeStart);
                        start = c.getTime();

                        Date stop = event.getEndTime();
                        int breakTimeStop = match.getPeriod().getEventStopOffset().intValue() * Constants.SLOT_LENGTH_MIN;
                        c = Calendar.getInstance(Locale.US); 
                        c.setTime(stop);
                        c.add(Calendar.MINUTE, -breakTimeStop);
                        stop = c.getTime();
                        boolean startMatch = start.equals(startTime);
                        boolean endMatch = stop.equals(endTime);
                        if (nameMatch && dateMatch && startMatch && endMatch) continue;
                        
                        LineInterface line = table.addLine();
    	                line.addCell(location.getLabel())
	                		.setComparable(location.getLabel(), event.getStartTime());
    	                line.addCell(location.getCapacity().toString())
    	                	.setTextAlignment(Alignment.RIGHT)
	                		.setComparable(-location.getCapacity(), event.getStartTime());
    	                line.addCell(location.getExamCapacity().toString())
    	                	.setTextAlignment(Alignment.RIGHT)
	                		.setComparable(-location.getExamCapacity(), event.getStartTime());
    	                line.addCell(match.getExamName())
    	                	.addStyle(nameMatch ? "" : "background-color:yellow;")
	                		.setComparable(match.getExamName(), location.getLabel(), match.getPeriod().getStartTime());
    	                line.addCell(dateFormat.format(match.getPeriod().getStartDate()))
    	                	.addStyle(dateMatch ? "" : "background-color:yellow;")
        	                .setComparable(match.getPeriod().getStartTime(), location.getLabel());
    	                CellInterface exTm = line.addCell()
    	                	.setComparable(match.getPeriod().getStartTime().getTime() % 86400000, location.getLabel());
    	                exTm.add(timeFormat.format(startTime).replaceAll("AM", "a").replaceAll("PM", "p"))
    	                	.addStyle(startMatch ? "" : "background-color:yellow;");
    	                exTm.add(" - ");
    	                exTm.add(timeFormat.format(endTime).replaceAll("AM", "a").replaceAll("PM", "p"))
    	                	.addStyle(endMatch ? "" : "background-color:yellow;");
    	                line.addCell(event.getEventName())
	                		.addStyle(nameMatch ? "" : "background-color:yellow;")
	                		.setComparable(event.getEventName(), location.getLabel(), event.getStartTime());
    	                line.addCell(dateFormat.format(event.getStartTime()))
	                		.addStyle(dateMatch ? "" : "background-color:yellow;")
	                		.setComparable(event.getStartTime(), location.getLabel());
    	                CellInterface evTm = line.addCell()
    	                	.setComparable(event.getStartTime().getTime() % 86400000,location.getLabel());
    	                evTm.add(timeFormat.format(event.getStartTime()).replaceAll("AM", GWT_CONST.timeShortAm()).replaceAll("PM", GWT_CONST.timeShortPm()))
    	                	.addStyle(startMatch ? "" : "background-color:yellow;");
    	                evTm.add(" - ");
    	                evTm.add(timeFormat.format(event.getEndTime()).replaceAll("AM", GWT_CONST.timeShortAm()).replaceAll("PM", GWT_CONST.timeShortPm()))
    	                	.addStyle(endMatch ? "" : "background-color:yellow;");
                    }
                }
                for (ExamAssignment exam : exams) {
                	Calendar c = Calendar.getInstance(); 
                    c.setTime(exam.getPeriod().getStartTime()); 
                    c.add(Calendar.MINUTE, exam.getLength());
                    Date endTime = c.getTime();
                	LineInterface line = table.addLine();
	                line.addCell(location.getLabel())
                		.setComparable(location.getLabel(), exam.getPeriod().getStartTime());
	                line.addCell(location.getCapacity().toString())
	                	.setTextAlignment(Alignment.RIGHT)
                		.setComparable(-location.getCapacity(), exam.getPeriod().getStartTime());
	                line.addCell(location.getExamCapacity().toString())
	                	.setTextAlignment(Alignment.RIGHT)
                		.setComparable(-location.getExamCapacity(), exam.getPeriod().getStartTime());
	                line.addCell(exam.getExamName())
                		.addStyle("background-color:yellow;")
                		.setComparable(exam.getExamName(), location.getLabel(), exam.getPeriod().getStartTime());
	                line.addCell(dateFormat.format(exam.getPeriod().getStartDate()))
                		.addStyle("background-color:yellow;")
                		.setComparable(exam.getPeriod().getStartTime(), location.getLabel());
	                line.addCell().add(timeFormat.format(exam.getPeriod().getStartTime()).replaceAll("AM", "a").replaceAll("PM", "p")
	                	+ " - " +
	                	timeFormat.format(endTime).replaceAll("AM", GWT_CONST.timeShortAm()).replaceAll("PM", GWT_CONST.timeShortPm()))
	                	.addStyle("background-color:yellow;")
	                	.setComparable(exam.getPeriod().getStartTime().getTime() % 86400000, location.getLabel());
                }
            } else {
                if (events == null) continue;
	            for (TimeBlock event : events) {
	                boolean overlaps = false;
	                for (ExamPeriod period: periods) {
	                    if (period.overlap(event)) { overlaps = true; break; }
	                }
	                if (!overlaps) continue;
	                LineInterface line = table.addLine();
	                line.addCell(location.getLabel())
	                	.setComparable(location.getLabel(), event.getStartTime());
	                line.addCell(location.getCapacity().toString())
	                	.setTextAlignment(Alignment.RIGHT)
	                	.setComparable(-location.getCapacity(), event.getStartTime());
	                line.addCell(location.getExamCapacity().toString())
	                	.setTextAlignment(Alignment.RIGHT)
	                	.setComparable(-location.getExamCapacity(), event.getStartTime());
	                line.addCell(event.getEventName())
	                	.setComparable(event.getEventName(), location.getLabel(), event.getStartTime());
	                line.addCell(event.getEventType())
	                	.setComparable(event.getEventType(), event.getEventName(), location.getLabel(), event.getStartTime());
	                line.addCell(dateFormat.format(event.getStartTime()))
	                	.setComparable(event.getStartTime(), location.getLabel());
	                line.addCell(timeFormat.format(event.getStartTime()).replaceAll("AM", GWT_CONST.timeShortAm()).replaceAll("PM", GWT_CONST.timeShortPm()))
	                	.setComparable(event.getStartTime().getTime() % 86400000, location.getLabel());
	                line.addCell(timeFormat.format(event.getEndTime()).replaceAll("AM", GWT_CONST.timeShortAm()).replaceAll("PM", GWT_CONST.timeShortPm()))
	                	.setComparable(event.getEndTime().getTime() % 86400000, location.getLabel());
	            }
            }
        }
    	String ts = ra.getTimeStamp(bounds[0], bounds[1], exclude);
    	if (ts != null)
    		table.setInfoMessage(GWT.infoExamSolverRoomAvailabilityLastUpdated(type.getLabel(), ts));
		
		return table;
	}

}
