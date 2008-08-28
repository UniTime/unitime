package org.unitime.timetable.webutil;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.EventListForm;
import org.unitime.timetable.form.MeetingListForm;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.util.Constants;

public class CalendarEventTableBuilder extends WebEventTableBuilder {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
    
    public CalendarEventTableBuilder() {
        super();
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));
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
    
    public void printMeeting(PrintWriter out, Meeting meeting) {
        out.println("BEGIN:VEVENT");
        out.println("UID:m"+meeting.getUniqueId());
        out.println("DTSTART:"+df.format(meeting.getStartTime())+"T"+tf.format(meeting.getStartTime())+"Z");
        out.println("DTEND:"+df.format(meeting.getStopTime())+"T"+tf.format(meeting.getStopTime())+"Z");
        out.println("SUMMARY:"+meeting.getEvent().getEventName()+" ("+meeting.getEvent().getEventTypeLabel()+")");
        if (meeting.getLocation()!=null)
            out.println("LOCATION:"+meeting.getLocation().getLabel());
        out.println("STATUS:"+(meeting.isApproved()?"CONFIRMED":"TENTATIVE"));
        out.println("END:VEVENT");
    }
    
    public File calendarTableForEvents (EventListForm form){
        List events = loadEvents(form);
        if (events.isEmpty()) return null;
        
        boolean mainContact = form.isAdmin() || form.isEventManager();
        
        PrintWriter out = null;
        try {
            File file = ApplicationProperties.getTempFile("events", "ics");
            out = new PrintWriter(new FileWriter(file));
            
            out = new PrintWriter(new FileWriter(file));
            out.println("BEGIN:VCALENDAR");
            out.println("VERSION:2.0");
            out.println("CALSCALE:GREGORIAN");
            out.println("METHOD:PUBLISH");
            out.println("X-WR-CALNAME:"+getName(form));
            out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
            out.println("PRODID:-//UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+"/UniTime Events//NONSGML v1.0//EN");

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
                for (Meeting meeting : (Set<Meeting>)event.getMeetings()) printMeeting(out, meeting);
            }

            out.println("END:VEVENT");
            out.flush(); out.close(); out = null;
            
            return file;
        } catch (Exception e) {
            Debug.error(e);
        } finally {
            if (out!=null) { out.flush(); out.close(); }
        }
        return null;
    }

    public File calendarTableForMeetings (MeetingListForm form){
        List meetings = loadMeetings(form);
        
        if (meetings.isEmpty()) return null;
        
        boolean mainContact = form.isAdmin() || form.isEventManager();
        
        PrintWriter out = null;
        try {
            File file = ApplicationProperties.getTempFile("events", "ics");
            out = new PrintWriter(new FileWriter(file));
            
            out = new PrintWriter(new FileWriter(file));
            out.println("BEGIN:VCALENDAR");
            out.println("VERSION:2.0");
            out.println("CALSCALE:GREGORIAN");
            out.println("METHOD:PUBLISH");
            out.println("X-WR-CALNAME:"+getName(form));
            out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
            out.println("PRODID:-//UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+"/UniTime Events//NONSGML v1.0//EN");

            for (Iterator it = meetings.iterator();it.hasNext();){
                Meeting meeting = (Meeting) it.next();
                printMeeting(out, meeting);
            }

            out.println("END:VEVENT");
            out.flush(); out.close(); out = null;
            
            return file;
        } catch (Exception e) {
            Debug.error(e);
        } finally {
            if (out!=null) { out.flush(); out.close(); }
        }
        return null;
    }
}
