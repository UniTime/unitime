/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.webutil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.cpsolver.ifs.util.CSVFile;
import net.sf.cpsolver.ifs.util.CSVFile.CSVField;

import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.EventListForm;
import org.unitime.timetable.form.MeetingListForm;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;

public class CsvEventTableBuilder extends WebEventTableBuilder {
    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat tf = new SimpleDateFormat("hh:mmaa");
    
    public CsvEventTableBuilder() {
        super();
    }
    
    public int getMaxResults() {
    	return 1500;
    }
    
    public String getName(EventListForm form) {
        String type = "";
        if (form.getEventTypes().length<5) {
            for (int i=0;i<form.getEventTypes().length;i++) {
                if (type.length()>0) type += ", ";
                type += Event.sEventTypesAbbv[form.getEventTypes()[i]];
            }
            type += " ";
        }
        String name;
        switch (form.getMode()) {
            case EventListForm.sModeAllApprovedEvents : name = "Approved "+type+"Events"; break;
            case EventListForm.sModeAllConflictingEvents : name = "Conflicting "+type+"Events"; break;
            case EventListForm.sModeAllEvents : name = type+"Events"; break;
            case EventListForm.sModeAllEventsWaitingApproval : name = type+"Events Awaiting Approval"; break;
            case EventListForm.sModeEvents4Approval : name = type+"Events Awaiting My Approval"; break;
            case EventListForm.sModeMyEvents : name = "My "+type+"Events"; break;
            default : name = "Events";
        }
        if (form.getEventDateFrom()!=null && form.getEventDateFrom().trim().length()>0) {
            if (form.getEventDateTo()!=null && form.getEventDateTo().trim().length()>0) {
                name += " Between "+form.getEventDateFrom()+" And "+form.getEventDateTo();
            }
            name += " From "+form.getEventDateFrom();
        } else if (form.getEventDateTo()!=null && form.getEventDateTo().trim().length()>0) {
            name += " Till "+form.getEventDateTo();
        }
        return name;
    }
    
    public void printMeeting(CSVFile csv, Meeting meeting) {
    	
    	String cap = "";
    	int minCap = (meeting.getEvent().getMinCapacity() == null ? -1 : meeting.getEvent().getMinCapacity());
    	int maxCap = (meeting.getEvent().getMaxCapacity() == null ? -1 : meeting.getEvent().getMaxCapacity());
    	if (minCap >= 0 && maxCap >= 0) {
    		if (minCap == maxCap) cap = String.valueOf(minCap);
    		else cap = minCap + "-" + maxCap;
    	}
    	
    	int enrl = 0;
    	if (Event.sEventTypeClass == meeting.getEvent().getEventType()) {
    		ClassEvent ce = new ClassEventDAO().get(meeting.getEvent().getUniqueId());
			if (ce.getClazz().getEnrollment() != null) {
				enrl = ce.getClazz().getEnrollment();
			}
		} else if (Event.sEventTypeFinalExam == meeting.getEvent().getEventType() || Event.sEventTypeMidtermExam == meeting.getEvent().getEventType()) {
			ExamEvent ee = new ExamEventDAO().get(meeting.getEvent().getUniqueId());
			enrl = ee.getExam().countStudents();
		}  else if (Event.sEventTypeCourse == meeting.getEvent().getEventType()) {
			CourseEvent ce = new CourseEventDAO().get(meeting.getEvent().getUniqueId());
			for (RelatedCourseInfo rci: ce.getRelatedCourses())
				enrl += rci.countStudents();
		}
    		
    	csv.addLine(new CSVField[] {
    			new CSVField(meeting.getEvent().getEventName()),
    			new CSVField(df.format(meeting.getMeetingDate())),
    			new CSVField(meeting.startTime()),
    			new CSVField(meeting.stopTime()),
    			new CSVField(meeting.getRoomLabel()),
    			new CSVField(meeting.getEvent().getEventTypeAbbv()),
    			new CSVField(meeting.getApprovedDate() == null ? "" : df.format(meeting.getApprovedDate())),
    			new CSVField(meeting.getEvent().getMainContact() == null ? "" : meeting.getEvent().getMainContact().getName().trim()),
    			new CSVField(meeting.getEvent().getMainContact() == null ? "" : meeting.getEvent().getMainContact().getEmailAddress()),
    			new CSVField(enrl),
    			new CSVField(cap),
    			new CSVField(meeting.getEvent().getSponsoringOrganization() == null ? "" : meeting.getEvent().getSponsoringOrganization().getName())
    	});
    }
    
    public File csvTableForEvents (EventListForm form){
        List events = loadEvents(form);
        if (events.isEmpty()) return null;
        
        try {
            CSVFile csv = new CSVFile();
            
            csv.setHeader(new CSVField[] {
            		new CSVField("Name"),
            		new CSVField("Date"),
            		new CSVField("Start"),
            		new CSVField("End"),
            		new CSVField("Location"),
            		new CSVField("Type"),
            		new CSVField("Approved"),
            		new CSVField("Main Contact"),
            		new CSVField("Email"),
            		new CSVField("Enrollment"),
            		new CSVField("Attend/Limit"),
            		new CSVField("Sponsoring Org")
            		});
 
            for (Iterator it = events.iterator();it.hasNext();){
                Event event = (Event) it.next();
                if (form.getMode()==EventListForm.sModeEvents4Approval) {
                    boolean myApproval = false;
                    for (Iterator j=event.getMeetings().iterator();j.hasNext();) {
                        Meeting m = (Meeting)j.next();
                        if (m.getApprovedDate()==null && m.getLocation()!=null && form.getManagingDepartments().contains(m.getLocation().getControllingDepartment())) {
                            myApproval = true; break;
                        }
                    }
                    if (!myApproval) continue;
                }
                for (Meeting meeting : (Set<Meeting>)event.getMeetings()) printMeeting(csv, meeting);
            }

            File file = ApplicationProperties.getTempFile("events", "csv");
            csv.save(file);
            return file;
        } catch (Exception e) {
            Debug.error(e);
        } finally {
        }
        return null;
    }

    public File csvTableForMeetings (MeetingListForm form){
        List meetings = loadMeetings(form);
        
        if (meetings.isEmpty()) return null;
        
        try {
            CSVFile csv = new CSVFile();
            
            csv.setHeader(new CSVField[] {
            		new CSVField("Name"),
            		new CSVField("Date"),
            		new CSVField("Start"),
            		new CSVField("End"),
            		new CSVField("Location"),
            		new CSVField("Type"),
            		new CSVField("Approved"),
            		new CSVField("Main Contact"),
            		new CSVField("Email"),
            		new CSVField("Enrollment"),
            		new CSVField("Attend/Limit"),
            		new CSVField("Sponsoring Org")
            		});
            
            for (Iterator it = meetings.iterator();it.hasNext();) {
                Meeting meeting = (Meeting) it.next();
                printMeeting(csv, meeting);
            }

            File file = ApplicationProperties.getTempFile("events", "csv");
            csv.save(file);
            return file;
        } catch (Exception e) {
            Debug.error(e);
        } finally {
        }
        return null;
    }


}
