/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.exam.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.exam.criteria.ExamRotationPenalty;
import org.cpsolver.exam.criteria.InstructorBackToBackConflicts;
import org.cpsolver.exam.criteria.InstructorDirectConflicts;
import org.cpsolver.exam.criteria.InstructorDistanceBackToBackConflicts;
import org.cpsolver.exam.criteria.InstructorMoreThan2ADayConflicts;
import org.cpsolver.exam.criteria.InstructorNotAvailableConflicts;
import org.cpsolver.exam.criteria.PeriodPenalty;
import org.cpsolver.exam.criteria.RoomPenalty;
import org.cpsolver.exam.criteria.RoomSizePenalty;
import org.cpsolver.exam.criteria.RoomSplitPenalty;
import org.cpsolver.exam.criteria.StudentBackToBackConflicts;
import org.cpsolver.exam.criteria.StudentDistanceBackToBackConflicts;
import org.cpsolver.exam.criteria.StudentMoreThan2ADayConflicts;
import org.cpsolver.exam.criteria.StudentNotAvailableConflicts;
import org.cpsolver.exam.model.ExamDistributionConstraint;
import org.cpsolver.exam.model.ExamPlacement;
import org.cpsolver.exam.model.ExamRoomPlacement;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.ToolBox;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;


/**
 * @author Tomas Muller
 */
public class ExamAssignment extends ExamInfo implements Serializable {
	private static final long serialVersionUID = -5726339642542287195L;
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
    private Format<Number> s2Z = Formats.getNumberFormat("00");
    private Format<Date> sDateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD);
    protected Long iPeriodId = null;
    protected TreeSet<ExamRoomInfo> iRooms = null;
    protected String iPeriodPref = null;
    protected int iPeriodIdx = -1;
    protected transient ExamPeriod iPeriod = null;
    protected ExamInfo iExam = null;
    protected String iDistPref = null;
    
    protected int iNrDirectConflicts = 0;
    protected int iNrMoreThanTwoADayConflicts = 0;
    protected int iNrBackToBackConflicts = 0;
    protected int iNrDistanceBackToBackConflicts = 0;
    protected int iPeriodPenalty = 0;
    protected int iRoomSizePenalty = 0;
    protected int iRoomSplitPenalty = 0;
    protected int iRotationPenalty = 0;
    protected int iRoomPenalty = 0;
    protected int iNrInstructorDirectConflicts = 0;
    protected int iNrInstructorMoreThanTwoADayConflicts = 0;
    protected int iNrInstructorBackToBackConflicts = 0;
    protected int iNrInstructorDistanceBackToBackConflicts = 0;
    protected double iValue = 0;
    
    public ExamAssignment(ExamPlacement placement, Assignment<org.cpsolver.exam.model.Exam, ExamPlacement> assignment) {
        this((org.cpsolver.exam.model.Exam)placement.variable(), placement, assignment);
    }
    
    public ExamAssignment(org.cpsolver.exam.model.Exam exam, ExamPlacement placement, Assignment<org.cpsolver.exam.model.Exam, ExamPlacement> assignment) {
        super(exam);
        if (placement!=null) {
            iNrDirectConflicts = (int)exam.getModel().getCriterion(StudentDistanceBackToBackConflicts.class).getValue(assignment, placement, null) +
            					 (int)exam.getModel().getCriterion(StudentNotAvailableConflicts.class).getValue(assignment, placement, null);
            iNrMoreThanTwoADayConflicts = (int)exam.getModel().getCriterion(StudentMoreThan2ADayConflicts.class).getValue(assignment, placement, null);
            iNrBackToBackConflicts = (int)exam.getModel().getCriterion(StudentBackToBackConflicts.class).getValue(assignment, placement, null);
            iNrDistanceBackToBackConflicts = (int)exam.getModel().getCriterion(StudentDistanceBackToBackConflicts.class).getValue(assignment, placement, null);
            iPeriodPenalty = (int)exam.getModel().getCriterion(PeriodPenalty.class).getValue(assignment, placement, null);
            iRoomSizePenalty = (int)exam.getModel().getCriterion(RoomSizePenalty.class).getValue(assignment, placement, null);
            iRoomSplitPenalty = (int)exam.getModel().getCriterion(RoomSplitPenalty.class).getValue(assignment, placement, null);
            iRotationPenalty = (int)exam.getModel().getCriterion(ExamRotationPenalty.class).getValue(assignment, placement, null);
            iRoomPenalty = (int)exam.getModel().getCriterion(RoomPenalty.class).getValue(assignment, placement, null);
            iNrInstructorDirectConflicts = (int)exam.getModel().getCriterion(InstructorDirectConflicts.class).getValue(assignment, placement, null) +
            		(int)exam.getModel().getCriterion(InstructorNotAvailableConflicts.class).getValue(assignment, placement, null);
            iNrInstructorMoreThanTwoADayConflicts = (int)exam.getModel().getCriterion(InstructorMoreThan2ADayConflicts.class).getValue(assignment, placement, null);
            iNrInstructorBackToBackConflicts = (int)exam.getModel().getCriterion(InstructorBackToBackConflicts.class).getValue(assignment, placement, null);
            iNrInstructorDistanceBackToBackConflicts = (int)exam.getModel().getCriterion(InstructorDistanceBackToBackConflicts.class).getValue(assignment, placement, null);
            iValue = placement.toDouble(assignment);
            iPeriodId = placement.getPeriod().getId();
            iPeriodIdx = placement.getPeriod().getIndex();
            iRooms = new TreeSet<ExamRoomInfo>();
            iPeriodPref = (exam.getPeriodPlacements().size()==1?PreferenceLevel.sRequired:PreferenceLevel.int2prolog(iPeriodPenalty));
            if (placement.getRoomPlacements()!=null) {
                boolean reqRoom = placement.getRoomPlacements().size()==exam.getRoomPlacements().size();
                for (Iterator i=placement.getRoomPlacements().iterator();i.hasNext();) {
                    ExamRoomPlacement room = (ExamRoomPlacement)i.next();
                    iRooms.add(new ExamRoomInfo(room.getRoom(), (reqRoom?PreferenceLevel.sIntLevelRequired:room.getPenalty(placement.getPeriod()))));
                }
            }
            MinMaxPreferenceCombination pc = new MinMaxPreferenceCombination();
            for (ExamDistributionConstraint dc: placement.variable().getDistributionConstraints()) {
                if (dc.isHard() || dc.isSatisfied(assignment)) continue;
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
    
    public ExamAssignment(Exam exam, Hashtable<Long, Set<Long>> owner2students, Hashtable<Long,Hashtable<Long,Set<Long>>> onwer2course2students) {
        this(exam);
        if (owner2students != null) createSections(owner2students);
        if (onwer2course2students != null) createSectionsIncludeCrosslistedDummies(onwer2course2students);
    }
    
    public ExamAssignment(Exam exam, ExamPeriod period, Collection<ExamRoomInfo> rooms) throws Exception {
        super(exam);
        if (period==null) return;
        if (Constants.SLOT_LENGTH_MIN*period.getLength()<exam.getLength()) throw new Exception("Given period is two short.");
        iPeriodPref = period.getPrefLevel().getPrefProlog();
        boolean reqPeriod = false;
        boolean hasPeriodPref = false;
        for (Iterator i=exam.getPreferences(ExamPeriodPref.class).iterator();i.hasNext();) {
            ExamPeriodPref periodPref = (ExamPeriodPref)i.next();
            if (PreferenceLevel.sRequired.equals(periodPref.getPrefLevel().getPrefProlog())) reqPeriod = true;
            if (periodPref.getExamPeriod().equals(period)) {
                iPeriodPref = periodPref.getPrefLevel().getPrefProlog();
                hasPeriodPref = true;
            }
        }
        if (exam.getExamType().getType() == ExamType.sExamTypeMidterm && !hasPeriodPref) iPeriodPref=PreferenceLevel.sProhibited;
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
        if (iPeriod==null) {
            if (getPeriodId()==null) return null;
            iPeriod = new ExamPeriodDAO().get(getPeriodId());
        }
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
        if (getPeriod()==null) return "";
        int start = getPeriod().getStartSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + getPrintOffset();
        int end = start + getLength();
        return sDateFormat.format(getPeriod().getStartDate())+" "+Constants.toTime(start)+" - "+Constants.toTime(end);
    }
    
    public String getPeriodNameFixedLength() {
        if (getPeriod()==null) return "";
        int min = getPeriod().getStartSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + getPrintOffset();
        int startHour = min / 60;
        int startMin = min % 60;
        min += getLength();
        int endHour = min / 60;
        int endMin = min % 60;
        return sDateFormat.format(getPeriod().getStartDate())+" "+
            s2Z.format(startHour==0?12:startHour>12?startHour-12:startHour)+":"+s2Z.format(startMin)+(startHour<24 && startHour>=12?"p":"a")+" - "+
            s2Z.format(endHour==0?12:endHour>12?endHour-12:endHour)+":"+s2Z.format(endMin)+(endHour<24 && endHour>=12?"p":"a");
    }
    
    public String getPeriodAbbreviation() {
        if (getPeriod()==null) return "";
        int start = getPeriod().getStartSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + getPrintOffset();
        return sDateFormat.format(getPeriod().getStartDate())+" "+Constants.toTime(start);
    }
    
    public String getPeriodNameWithPref() {
        if (iPeriodPref==null) return getPeriodName();
        return
            "<span style='color:"+PreferenceLevel.prolog2color(iPeriodPref)+";' onmouseover=\"showGwtTimeHint(this, '" + getExamId() + "," + getPeriodId() + "');\" onmouseout=\"hideGwtTimeHint();\">"+
            getPeriodName()+
            "</span>";
    }
    
    public String getPeriodAbbreviationWithPref() {
        if (iPeriodPref==null) return getPeriodAbbreviation();
        return
        	"<span style='color:"+PreferenceLevel.prolog2color(iPeriodPref)+";' onmouseover=\"showGwtTimeHint(this, '" + getExamId() + "," + getPeriodId() + "');\" onmouseout=\"hideGwtTimeHint();\">"+
            getPeriodAbbreviation()+
            "</span>";
    }
    
    public String getDate(boolean pref) {
        if (getPeriod()==null) return "";
        if (!pref || iPeriodPref==null || PreferenceLevel.sNeutral.equals(iPeriodPref)) return sDateFormat.format(getPeriod().getStartDate());
        return
        "<span title='"+PreferenceLevel.prolog2string(iPeriodPref)+" "+getPeriodName()+"' style='color:"+PreferenceLevel.prolog2color(iPeriodPref)+";'>"+
        sDateFormat.format(getPeriod().getStartDate())+
        "</span>";
    }

    public String getTime(boolean pref) {
        if (getPeriod()==null) return "";
        int start = getPeriod().getStartSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + getPrintOffset();
        int end = start + getLength();
        if (!pref || iPeriodPref==null || PreferenceLevel.sNeutral.equals(iPeriodPref))
            return Constants.toTime(start)+" - "+Constants.toTime(end); 
        return 
            "<span title='"+PreferenceLevel.prolog2string(iPeriodPref)+" "+getPeriodName()+"' style='color:"+PreferenceLevel.prolog2color(iPeriodPref)+";'>"+
            Constants.toTime(start)+" - "+Constants.toTime(end)+
            "</span>";
    }
    
    public String getTimeFixedLength() {
        if (getPeriod()==null) return "";
        int min = getPeriod().getStartSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + getPrintOffset();
        int startHour = min / 60;
        int startMin = min % 60;
        min += getLength();
        int endHour = min / 60;
        int endMin = min % 60;
        return 
            s2Z.format(startHour==0?12:startHour>12?startHour-12:startHour)+":"+s2Z.format(startMin)+(startHour<24 && startHour>=12?"p":"a")+" - "+
            s2Z.format(endHour==0?12:endHour>12?endHour-12:endHour)+":"+s2Z.format(endMin)+(endHour<24 && endHour>=12?"p":"a");
    }

    public TreeSet<ExamRoomInfo> getRooms() {
        return iRooms;
    }
    
    public Vector<Long> getRoomIds() {
        Vector<Long> roomIds = new Vector(iRooms==null?0:iRooms.size());
        if (iRooms!=null) for (ExamRoomInfo room : iRooms) roomIds.add(room.getLocationId());
        return roomIds;
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
        if (getPeriod()==null) return "";
        String rooms = "";
        for (ExamRoomInfo room : getRooms()) {
            if (rooms.length()>0) rooms+=delim;
            rooms += (pref?room.toString():room.getName());
        }
        return rooms;
    }
    
    public String getRoomsNameWithHint(boolean pref, String delim) {
        if (getPeriod()==null) return "";
        String rooms = "";
        for (ExamRoomInfo room : getRooms()) {
            if (rooms.length()>0) rooms+=delim;
            rooms += room.getNameWithHint(pref);
        }
        return rooms;
    }
    
    public String getRoomsCapacity(boolean pref, String delim) {
        if (getPeriod()==null) return "";
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
        if (getPeriod()==null) return 0;
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
    
    public void setPeriodPref(String periodPref) {
        iPeriodPref = periodPref;
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
    
    public int compareTo(ExamInfo info) {
        int cmp = getExamName().compareTo(info.getExamName());
        if (cmp!=0) return cmp;
        ExamPeriod otherPeriod = (info instanceof ExamAssignment?((ExamAssignment)info).getPeriod():null);
        if (getPeriod()==null) {
            if (otherPeriod!=null) return -1;
        } else {
            if (otherPeriod==null) return 1;
            cmp = getPeriod().compareTo(otherPeriod);
            if (cmp!=0) return cmp;
        }
        return getExamId().compareTo(info.getExamId());
    }
    
    public int getPlacementNrDirectConflicts() { return iNrDirectConflicts; }
    public int getPlacementNrMoreThanTwoADayConflicts() { return iNrMoreThanTwoADayConflicts; }
    public int getPlacementNrBackToBackConflicts() { return iNrBackToBackConflicts; }
    public int getPlacementNrDistanceBackToBackConflicts() { return iNrDistanceBackToBackConflicts; }
    public int getPlacementPeriodPenalty() { return iPeriodPenalty; }
    public int getPlacementRoomSizePenalty() { return iRoomSizePenalty; }
    public int getPlacementRoomSplitPenalty() { return iRoomSplitPenalty; }
    public int getPlacementRotationPenalty() { return iRotationPenalty; }
    public int getPlacementRoomPenalty() { return iRoomPenalty; }
    public int getPlacementNrInstructorDirectConflicts() { return iNrInstructorDirectConflicts; }
    public int getPlacementNrInstructorMoreThanTwoADayConflicts() { return iNrInstructorMoreThanTwoADayConflicts; }
    public int getPlacementNrInstructorBackToBackConflicts() { return iNrInstructorBackToBackConflicts; }
    public int getPlacementNrInstructorDistanceBackToBackConflicts() { return iNrInstructorDistanceBackToBackConflicts; }
    public double getPlacementValue() { return iValue; }
    
    public int getNrRooms() {
        return (iRooms==null?0:iRooms.size());
    }
    
    public boolean assignmentEquals(ExamAssignment other) {
        if (!getExamId().equals(other.getExamId())) return false;
        return ToolBox.equals(getPeriodId(),other.getPeriodId()) && ToolBox.equals(getRooms(), other.getRooms());
    }
}
