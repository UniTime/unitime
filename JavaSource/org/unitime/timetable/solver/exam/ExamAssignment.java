package org.unitime.timetable.solver.exam;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.LocationDAO;

import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamRoom;

public class ExamAssignment implements Serializable, Comparable {
    protected Long iPeriodId = null;
    protected Vector iRoomIds = null;
    protected String iPeriodPref = null;
    protected Hashtable iRoomPrefs = null;
    protected String iExamLabel = null;
    protected Long iExamId = null;
    protected transient ExamPeriod iPeriod = null;
    protected transient TreeSet iRooms = null;
    protected transient Exam iExam = null; 
    
    public ExamAssignment(ExamPlacement placement) {
        iPeriodId = placement.getPeriod().getId();
        iRoomIds = new Vector(placement.getRooms()==null?0:placement.getRooms().size());
        iPeriodPref = PreferenceLevel.int2prolog(placement.getPeriodPenalty());
        iRoomPrefs = new Hashtable();
        iExamId = ((net.sf.cpsolver.exam.model.Exam)placement.variable()).getId();
        iExamLabel = ((net.sf.cpsolver.exam.model.Exam)placement.variable()).getName();
        if (placement.getRooms()!=null)
            for (Iterator i=placement.getRooms().iterator();i.hasNext();) {
                ExamRoom room = (ExamRoom)i.next();
                iRoomIds.add(room.getId());
                iRoomPrefs.put(room.getId(),PreferenceLevel.int2prolog(((net.sf.cpsolver.exam.model.Exam)placement.variable()).getWeight(room)));
            }
    }
    
    public ExamAssignment(Exam exam) {
        iPeriod = exam.getAssignedPeriod();
        iPeriodId = exam.getAssignedPeriod().getUniqueId();
        iRooms = new TreeSet();
        iRoomIds = new Vector(exam.getAssignedRooms().size());
        iExamId = exam.getUniqueId();
        iExam = exam;
        iExamLabel = exam.getLabel();
        for (Iterator i=exam.getAssignedRooms().iterator();i.hasNext();) {
            Location location = (Location)i.next();
            iRooms.add(location);
            iRoomIds.add(location.getUniqueId());
        }
    }

    public Long getPeriodId() {
        return iPeriodId;
    }
    
    public ExamPeriod getPeriod() {
        if (iPeriod==null)
            iPeriod = new ExamPeriodDAO().get(getPeriodId());
        return iPeriod;
    }
    
    public String getPeriodName() {
        ExamPeriod period = getPeriod();
        return period==null?"":period.getName();
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

    public Vector getRoomIds() {
        return iRoomIds;
    }
    
    public TreeSet getRooms() {
        if (iRooms==null) {
            iRooms = new TreeSet();
            for (Enumeration e=getRoomIds().elements();e.hasMoreElements();) {
                Location location = new LocationDAO().get((Long)e.nextElement());
                iRooms.add(location);
            }
        }
        return iRooms;
    }
    
    public String getRoomsName(String delim) {
        String rooms = "";
        for (Iterator j=getRooms().iterator();j.hasNext();) {
            Location location = (Location)j.next();
            if (rooms.length()>0) rooms+=delim;
            rooms += location.getLabel();
        }
        return rooms;
    }

    public String getRoomsNameWithPref(String delim) {
        String rooms = "";
        for (Iterator j=getRooms().iterator();j.hasNext();) {
            Location location = (Location)j.next();
            if (rooms.length()>0) rooms+=delim;
            String roomPref = (iRoomPrefs==null?null:(String)iRoomPrefs.get(location.getUniqueId()));
            if (roomPref==null) {
                rooms += location.getLabel();
            } else {
                rooms += "<span title='"+PreferenceLevel.prolog2string(roomPref)+" "+location.getLabel()+"' style='color:"+PreferenceLevel.prolog2color(roomPref)+";'>"+
                location.getLabel()+
                "</span>";
            }
        }
        return rooms;
    }
    
    public Long getExamId() {
        return iExamId;
    }
    
    public Exam getExam() {
        if (iExam==null)
            iExam = new ExamDAO().get(iExamId);
        return iExam;
    }
    
    public String getExamName() {
        return (iExamLabel==null?getExam().getLabel():iExamLabel);
    }
    
    public int hashCode() {
        return getExamId().hashCode();
    }
    
    public boolean equals(Object o) {
        if (o==null || !(o instanceof ExamAssignment)) return false;
        return ((ExamAssignment)o).getExamId().equals(getExamId());
    }
    
    public int compareTo(Object o) {
        if (o==null || !(o instanceof ExamAssignment)) return -1;
        ExamAssignment a = (ExamAssignment)o;
        int cmp = getExamName().compareTo(a.getExamName());
        if (cmp!=0) return cmp;
        return getExamId().compareTo(a.getExamId());
    }
    
    public String toString() {
        return getExamName();
    }
}
