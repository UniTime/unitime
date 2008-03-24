/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.exam.ui;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import net.sf.cpsolver.exam.model.ExamDistributionConstraint;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamRoom;

/**
 * @author Tomas Muller
 */
public class ExamAssignment extends ExamInfo implements Serializable, Comparable {
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("EEE MM/dd");
    private static SimpleDateFormat sTimeFormat = new SimpleDateFormat("hh:mmaa");
    protected Long iPeriodId = null;
    protected TreeSet<ExamRoomInfo> iRooms = null;
    protected String iPeriodPref = null;
    protected int iPeriodIdx = -1;
    protected transient ExamPeriod iPeriod = null;
    protected ExamInfo iExam = null;
    protected String iDistPref = null;
    
    public ExamAssignment(ExamPlacement placement) {
        this((net.sf.cpsolver.exam.model.Exam)placement.variable(), placement);
    }
    
    public ExamAssignment(net.sf.cpsolver.exam.model.Exam exam, ExamPlacement placement) {
        super(exam);
        if (placement!=null) {
            iPeriodId = placement.getPeriod().getId();
            iPeriodIdx = placement.getPeriod().getIndex();
            iRooms = new TreeSet<ExamRoomInfo>();
            iPeriodPref = PreferenceLevel.int2prolog(placement.getPeriodPenalty());
            if (placement.getRooms()!=null)
                for (Iterator i=placement.getRooms().iterator();i.hasNext();) {
                    ExamRoom room = (ExamRoom)i.next();
                    iRooms.add(new ExamRoomInfo(room, ((net.sf.cpsolver.exam.model.Exam)placement.variable()).getWeight(room)));
                }
            MinMaxPreferenceCombination pc = new MinMaxPreferenceCombination();
            for (Enumeration e=((net.sf.cpsolver.exam.model.Exam)placement.variable()).getDistributionConstraints().elements();e.hasMoreElements();) {
                ExamDistributionConstraint dc = (ExamDistributionConstraint)e.nextElement();
                if (dc.isHard() || dc.isSatisfied()) continue;
                pc.addPreferenceInt(dc.getWeight());
            }
            iDistPref = pc.getPreferenceProlog(); 
        }
    }
    
    public ExamAssignment(Exam exam) {
        super(exam);
        if (exam.getAssignedPeriod()!=null) {
            iPeriod = exam.getAssignedPeriod();
            iPeriodId = exam.getAssignedPeriod().getUniqueId();
            iRooms = new TreeSet<ExamRoomInfo>();
            for (Iterator i=exam.getAssignedRooms().iterator();i.hasNext();) {
                Location location = (Location)i.next();
                iRooms.add(new ExamRoomInfo(location,0));
            }
            if (exam.getAssignedPreference()!=null && exam.getAssignedPreference().length()>0) {
                StringTokenizer stk = new StringTokenizer(exam.getAssignedPreference(),":");
                if (stk.hasMoreTokens())
                    iPeriodPref = stk.nextToken();
                if (stk.hasMoreTokens())
                    iDistPref = stk.nextToken();
                for (Iterator i=iRooms.iterator();i.hasNext() && stk.hasMoreTokens();) {
                    ExamRoomInfo room = (ExamRoomInfo)i.next();
                    room.setPreference(Integer.parseInt(stk.nextToken()));
                }
            }
        }
    }
    
    public ExamAssignment(Exam exam, ExamPeriod period, Collection<ExamRoomInfo> rooms) throws Exception {
        super(exam);
        if (period==null) return;
        if (Constants.SLOT_LENGTH_MIN*period.getLength()<exam.getLength()) throw new Exception("Given period is two short.");
        String iPeriodPref = period.getPrefLevel().getPrefProlog();
        boolean reqPeriod = false;
        for (Iterator i=exam.getPreferences(ExamPeriodPref.class).iterator();i.hasNext();) {
            ExamPeriodPref periodPref = (ExamPeriodPref)i.next();
            if (PreferenceLevel.sRequired.equals(periodPref.getPrefLevel().getPrefProlog())) reqPeriod = true;
            if (periodPref.getExamPeriod().equals(period))
                iPeriodPref = periodPref.getPrefLevel().getPrefProlog();
        }
        if (PreferenceLevel.sProhibited.equals(iPeriodPref)) throw new Exception("Given period is prohibited.");
        if (reqPeriod && !PreferenceLevel.sRequired.equals(iPeriodPref)) throw new Exception("Given period is not required.");
        iPeriod = period;
        iPeriodId = period.getUniqueId();
        iRooms = new TreeSet();
        if (rooms!=null)
            iRooms.addAll(rooms);
        //TODO: distribution preference
    }

    public String getAssignedPreferenceString() {
        String ret = getPeriodPref()+":"+getDistributionPref();
        for (ExamRoomInfo room : getRooms()) {
            ret += ":"+room.getPreference();
        }
        return ret;
    }

    public Long getPeriodId() {
        return iPeriodId;
    }
    
    public ExamPeriod getPeriod() {
        if (iPeriod==null)
            iPeriod = new ExamPeriodDAO().get(getPeriodId());
        return iPeriod;
    }
    
    public ExamPeriod getPeriod(org.hibernate.Session hibSession) {
        return new ExamPeriodDAO().get(getPeriodId(), hibSession);
    }

    public Comparable getPeriodOrd() {
        if (iPeriodIdx>=0) return new Integer(iPeriodIdx);
        else return iPeriod;
    }

    public String getPeriodName() {
        Date startTime = getPeriod().getStartTime();
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(startTime);
        cal.add(Calendar.MINUTE, getLength());
        Date endTime = cal.getTime();
        return sDateFormat.format(startTime)+" "+sTimeFormat.format(startTime)+" - "+sTimeFormat.format(endTime);
    }
    
    public String getPeriodAbbreviation() {
        ExamPeriod period = getPeriod();
        return period==null?"":period.getAbbreviation();
    }

    public String getPeriodNameWithPref() {
        if (iPeriodPref==null || PreferenceLevel.sNeutral.equals(iPeriodPref)) return getPeriodName();
        return
            "<span title='"+PreferenceLevel.prolog2string(iPeriodPref)+" "+getPeriodName()+"' style='color:"+PreferenceLevel.prolog2color(iPeriodPref)+";'>"+
            getPeriodName()+
            "</span>";
    }
    
    public String getPeriodAbbreviationWithPref() {
        if (iPeriodPref==null || PreferenceLevel.sNeutral.equals(iPeriodPref)) return getPeriodAbbreviation();
        return
            "<span title='"+PreferenceLevel.prolog2string(iPeriodPref)+" "+getPeriodName()+"' style='color:"+PreferenceLevel.prolog2color(iPeriodPref)+";'>"+
            getPeriodAbbreviation()+
            "</span>";
    }
    
    public String getDate(boolean pref) {
        if (!pref || iPeriodPref==null || PreferenceLevel.sNeutral.equals(iPeriodPref)) return sDateFormat.format(getPeriod().getStartDate());
        return
        "<span title='"+PreferenceLevel.prolog2string(iPeriodPref)+" "+getPeriodName()+"' style='color:"+PreferenceLevel.prolog2color(iPeriodPref)+";'>"+
        sDateFormat.format(getPeriod().getStartDate())+
        "</span>";
    }

    public String getTime(boolean pref) {
        Date startTime = getPeriod().getStartTime();
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(startTime);
        cal.add(Calendar.MINUTE, getLength());
        Date endTime = cal.getTime();
        if (!pref || iPeriodPref==null || PreferenceLevel.sNeutral.equals(iPeriodPref)) 
            return sTimeFormat.format(startTime) + " - "+ sTimeFormat.format(endTime);
        return
        "<span title='"+PreferenceLevel.prolog2string(iPeriodPref)+" "+getPeriodName()+"' style='color:"+PreferenceLevel.prolog2color(iPeriodPref)+";'>"+
        sTimeFormat.format(startTime)+" - "+sTimeFormat.format(endTime)+
        "</span>";
    }

    public TreeSet<ExamRoomInfo> getRooms() {
        return iRooms;
    }
    
    public boolean hasRoom(Long locationId) {
        if (iRooms==null) return false;
        for (ExamRoomInfo room : getRooms()) 
            if (room.getLocationId().equals(locationId)) return true;
        return false;
    }
    
    public String getRoomsName(String delim) {
        return getRoomsName(false, delim);
    }

    public String getRoomsNameWithPref(String delim) {
        return getRoomsName(true, delim);
    }
    
    public String getRoomsName(boolean pref, String delim) {
        String rooms = "";
        for (ExamRoomInfo room : getRooms()) {
            if (rooms.length()>0) rooms+=delim;
            rooms += (pref?room.toString():room.getName());
        }
        return rooms;
    }
    
    public String getRoomsCapacity(boolean pref, String delim) {
        String rooms = "";
        for (ExamRoomInfo room : getRooms()) {
            if (rooms.length()>0) rooms+=delim;
            if (!pref)
                rooms += room.getCapacity();
            else
                rooms += "<span style='color:"+PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference()))+";' >"+room.getCapacity()+"</span>";
        }
        return rooms;
    }

    public int getRoomsCapacity() {
        int cap = 0;
        for (ExamRoomInfo room : getRooms()) cap += room.getCapacity();
        return cap;
    }

    public String toString() {
        return getExamName()+" "+getPeriodAbbreviation()+" "+getRoomsName(",");
    }
    
    public String getPeriodPref() {
        return (iPeriodPref==null?PreferenceLevel.sNeutral:iPeriodPref);
    }
    
    public String getDistributionPref() {
        return (iDistPref==null?PreferenceLevel.sNeutral:iDistPref);
    }
    
    public String getRoomPref(Long locationId) {
        for (ExamRoomInfo room : getRooms()) {
            if (room.getLocationId().equals(locationId)) return PreferenceLevel.int2prolog(room.getPreference());
        }
        return PreferenceLevel.sNeutral;
    }

    public String getRoomPref() {
        MinMaxPreferenceCombination c = new MinMaxPreferenceCombination();
        for (Iterator j=getRooms().iterator();j.hasNext();) {
            ExamRoomInfo room = (ExamRoomInfo)j.next();
            c.addPreferenceInt(room.getPreference());
        }
        return c.getPreferenceProlog();
    }
    
    public boolean isValid() {
        if (getMaxRooms()>0 && (getRooms()==null || getRooms().isEmpty())) return false;
        return true;
    }
    
    public int getRoomSize() {
        if (getRooms()==null) return 0;
        int roomSize = 0;
        for (ExamRoomInfo room : getRooms())
            roomSize += room.getCapacity(this);
        return roomSize;
    }
}
