package org.unitime.timetable.solver.exam.ui;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.LocationDAO;

import net.sf.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import net.sf.cpsolver.exam.model.ExamDistributionConstraint;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamRoom;
import net.sf.cpsolver.ifs.util.ToolBox;

public class ExamAssignment extends ExamInfo implements Serializable, Comparable {
    protected Long iPeriodId = null;
    protected Vector iRoomIds = null;
    protected String iPeriodPref = null;
    protected int iPeriodIdx = -1;
    protected Hashtable iRoomPrefs = null;
    protected transient ExamPeriod iPeriod = null;
    protected transient TreeSet iRooms = null;
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
            iRoomIds = new Vector(placement.getRooms()==null?0:placement.getRooms().size());
            iPeriodPref = PreferenceLevel.int2prolog(placement.getPeriodPenalty());
            iRoomPrefs = new Hashtable();
            if (placement.getRooms()!=null)
                for (Iterator i=placement.getRooms().iterator();i.hasNext();) {
                    ExamRoom room = (ExamRoom)i.next();
                    iRoomIds.add(room.getId());
                    iRoomPrefs.put(room.getId(),PreferenceLevel.int2prolog(((net.sf.cpsolver.exam.model.Exam)placement.variable()).getWeight(room)));
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
            iRooms = new TreeSet();
            iRoomIds = new Vector(exam.getAssignedRooms().size());
            for (Iterator i=exam.getAssignedRooms().iterator();i.hasNext();) {
                Location location = (Location)i.next();
                iRooms.add(location);
                iRoomIds.add(location.getUniqueId());
            }
            if (exam.getAssignedPreference()!=null && exam.getAssignedPreference().length()>0) {
                iRoomPrefs = new Hashtable();
                StringTokenizer stk = new StringTokenizer(exam.getAssignedPreference(),":");
                if (stk.hasMoreTokens())
                    iPeriodPref = stk.nextToken();
                if (stk.hasMoreTokens())
                    iDistPref = stk.nextToken();
                for (Enumeration e=ToolBox.sortEnumeration(iRoomIds.elements());e.hasMoreElements() && stk.hasMoreTokens();) {
                    Long roomId = (Long)e.nextElement();
                    iRoomPrefs.put(roomId, stk.nextToken());
                }
            }
        }
    }
    
    public String getAssignedPreferenceString() {
        String ret = getPeriodPref()+":"+getDistributionPref();
        for (Enumeration e=ToolBox.sortEnumeration(iRoomIds.elements());e.hasMoreElements();) {
            Long roomId = (Long)e.nextElement();
            ret += ":"+getRoomPref(roomId);
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
    
    public Comparable getPeriodOrd() {
        if (iPeriodIdx>=0) return new Integer(iPeriodIdx);
        else return iPeriod;
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
    
    public String toString() {
        return getExamName()+" "+getPeriodAbbreviation()+" "+getRoomsName(",");
    }
    
    public String getPeriodPref() {
        return (iPeriodPref==null?PreferenceLevel.sNeutral:iPeriodPref);
    }
    
    public String getRoomPref(Long roomId) {
        if (iRoomPrefs==null) return PreferenceLevel.sNeutral;
        String pref = (String)iRoomPrefs.get(roomId);
        return (pref==null?PreferenceLevel.sNeutral:pref);
    }

    public String getDistributionPref() {
        return (iDistPref==null?PreferenceLevel.sNeutral:iDistPref);
    }

    public String getRoomPref() {
        if (iRoomPrefs==null) return null;
        MinMaxPreferenceCombination c = new MinMaxPreferenceCombination();
        for (Enumeration e=iRoomPrefs.elements();e.hasMoreElements();) {
            c.addPreferenceProlog((String)e.nextElement());
        }
        return c.getPreferenceProlog();
    }
}
