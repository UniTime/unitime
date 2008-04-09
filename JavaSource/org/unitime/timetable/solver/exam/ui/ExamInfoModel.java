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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;

import net.sf.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import net.sf.cpsolver.coursett.preference.PreferenceCombination;
import net.sf.cpsolver.coursett.preference.SumPreferenceCombination;

import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.ExamInfoForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.solver.exam.ExamSolverProxy;

/**
 * @author Tomas Muller
 */
public class ExamInfoModel implements Serializable {
    private transient ExamSolverProxy iSolver = null;
    private ExamInfo iExam = null;
    private ExamInfoForm iForm = null;
    private ExamProposedChange iChange = null;
    private Collection<ExamAssignmentInfo> iPeriods = null;
    private Vector<ExamRoomInfo> iRooms = null;
    private int iPeriodTableOrd = 0;
    private String iManagerExternalId = null;
    private ExamConflictStatisticsInfo iCbs = null;
    private ExamSuggestionsInfo iSuggestions = null;
    
    public void setSolver(ExamSolverProxy solver) { iSolver = solver; }
    public ExamSolverProxy getSolver() { return iSolver; }
    
    public void clear(TimetableManager manager) {
        iExam = null; iChange = null; iRooms = null; iPeriods = null;
        iManagerExternalId = manager.getExternalUniqueId();
    }
    
    public ExamInfo getExam() {
        return iExam;
    }
    public ExamAssignmentInfo getExamOldAssignment() {
        if (iExam instanceof ExamAssignmentInfo)
            return (ExamAssignmentInfo)iExam;
        return null;
    }
    public ExamAssignmentInfo getExamAssignment() {
        if (iChange!=null && iChange.getConflict(iExam)!=null) return null;
        if (iExam instanceof ExamAssignmentInfo)
            return (ExamAssignmentInfo)iExam;
        return null;
    }
    public boolean isExamAssigned() {
        return getExamAssignment()!=null && getExamAssignment().getPeriodId()!=null; 
    }
    
    public void update() throws Exception {
        iSuggestions = null;
        if (iChange==null) return;
        if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
            iChange = getSolver().update(iChange);
        } else {
            Vector<ExamAssignment> assignments = new Vector(iChange.getAssignments());
            Hashtable<Long,ExamAssignment> table = iChange.getAssignmentTable();
            iChange.getAssignments().clear();
            for (ExamAssignment assignment : assignments) {
                iChange.getAssignments().add(new ExamAssignmentInfo(assignment.getExam(),assignment.getPeriod(),assignment.getRooms(),table));
            }
            iChange.getConflicts().clear();
            for (ExamAssignment assignment : iChange.getAssignments()) {
                if (assignment.getRooms()!=null) for (ExamRoomInfo room : assignment.getRooms()) {
                    Exam x = room.getLocation().getExam(assignment.getPeriodId());
                    if (x!=null && iChange.getCurrent(x.getUniqueId())==null && iChange.getConflict(x.getUniqueId())==null) 
                        iChange.getConflicts().add(new ExamAssignment(x));
                }
            }
        }
    }
    
    public String assign() {
        if (iChange==null) return "Nothing to assign.";
        System.out.println("About to be assigned: "+iChange);
        if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
            String message = null;
            for (ExamAssignment assignment : iChange.getConflicts()) {
                String m = getSolver().unassign(assignment);
                if (m!=null) message = (message==null?"":message+"\n")+m;
            }
            for (ExamAssignment assignment : iChange.getAssignments()) {
                if (iChange.getInitial(assignment)!=null) {
                    String m = getSolver().unassign(iChange.getInitial(assignment));
                    if (m!=null) message = (message==null?"":message+"\n")+m;
                }
            }
            for (ExamAssignment assignment : iChange.getAssignments()) {
                String m = getSolver().assign(assignment);
                if (m!=null) message = (message==null?"":message+"\n")+m;
            }
            return message;
        } else {
            org.hibernate.Session hibSession = new ExamDAO().getSession();
            String message = null;
            for (ExamAssignment assignment : iChange.getConflicts()) {
                String m = assignment.getExam(hibSession).unassign(iManagerExternalId, hibSession);
                if (m!=null) message = (message==null?"":message+"\n")+m;
            }
            for (ExamAssignment assignment : iChange.getAssignments()) {
                try {
                    String m = assignment.getExam(hibSession).assign(getAssignmentInfo(assignment), iManagerExternalId, hibSession);
                    if (m!=null) message = (message==null?"":message+"\n")+m;
                } catch (Exception e) {
                    message = (message==null?"":message+"\n")+"Assignment of "+assignment.getExamName()+" to "+assignment.getPeriodAbbreviation()+" "+assignment.getRoomsName(", ")+" failed, reason: "+e.getMessage();
                }
            }
            return message;
        }
    }
    
    public boolean getCanAssign() {
        if (iChange==null) return false;
        for (ExamAssignment assignment : iChange.getAssignments())
            if (!assignment.isValid()) return false;
        return true;
    }
    
    public String getAssignConfirm() {
        if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
            return "Are you sure?";
        } else {
            return "The selected assignment will be done directly in the database. Are you sure?";
        }
    }
    
    public void setForm(ExamInfoForm form) {
        iForm = form;
    }
    
    public void setExam(Exam exam) {
        iPeriods = null; iRooms = null; iCbs = null; iSuggestions = null;
        if (getSolver()!=null && getSolver().getExamType()==exam.getExamType()) {
            iExam = getSolver().getAssignmentInfo(exam.getUniqueId());
            if (iExam==null) iExam = getSolver().getInfo(exam.getUniqueId());
            if (iExam==null) iExam = new ExamInfo(exam);
        } else {
            if (exam.getAssignedPeriod()!=null)
                iExam = new ExamAssignmentInfo(exam);
            else  
                iExam = new ExamInfo(exam);
        }
        if (iChange!=null) {
            for (Iterator<ExamAssignmentInfo> i=iChange.getAssignments().iterator();i.hasNext();) {
                ExamAssignmentInfo a = i.next();
                if (!a.isValid()) i.remove();
            }
        }
        iForm.setMinRoomSize(String.valueOf(iExam.getNrStudents()));
        iForm.setMaxRoomSize(String.valueOf(Math.max(50,2*iExam.getNrStudents())));
        iForm.setRoomFilter(null);
        iForm.setDepth(2);
        iForm.setTimeout(5000);
    }
    
    public ExamAssignmentInfo getAssignmentInfo(ExamAssignment assignment) throws Exception {
        if (assignment instanceof ExamAssignmentInfo) return (ExamAssignmentInfo)assignment;
        if (getSolver()!=null && getSolver().getExamType()==assignment.getExamType())
            return getSolver().getAssignment(assignment.getExamId(), assignment.getPeriodId(), assignment.getRoomIds());
        else {
            if (iChange!=null)
                return new ExamAssignmentInfo(assignment.getExam(), assignment.getPeriod(), assignment.getRooms(), iChange.getAssignmentTable());
            else
                return new ExamAssignmentInfo(assignment.getExam(), assignment.getPeriod(), assignment.getRooms());
        }
    }
    
    public ExamAssignmentInfo getSelectedAssignment() throws Exception {
        if (iChange==null) return null;
        for (ExamAssignment assignment : iChange.getAssignments())
            if (assignment.getExamId().equals(iExam.getExamId())) return getAssignmentInfo(assignment);
        return null; 
    }
    
    public void setPeriod(long periodId) throws Exception {
        iRooms = null;
        if (iChange==null) iChange = new ExamProposedChange();
        for (ExamAssignmentInfo period : getPeriods()) {
            if (periodId==period.getPeriodId()) {
                iChange.addChange(period, getExamOldAssignment());
            }
        }
        if (iChange.isEmpty()) iChange = null; 
        update();
    }
    
    public void delete(long examId) throws Exception {
        if (iChange==null) return;
        for (Iterator<ExamAssignmentInfo> i = iChange.getAssignments().iterator(); i.hasNext();) {
            ExamAssignmentInfo x = (ExamAssignmentInfo)i.next();
            if (x.getExamId().equals(examId)) i.remove();
        }
        update();
    }
    
    public void setRooms(String rooms) throws Exception {
        if (iChange==null) iChange = new ExamProposedChange();
        ExamAssignmentInfo assignment = iChange.getCurrent(iExam);
        if (assignment==null && isExamAssigned()) {
            for (ExamAssignmentInfo period : getPeriods()) {
                if (getExamOldAssignment().getPeriodId().equals(period.getPeriodId())) {
                    assignment = period;
                    break;
                }
            }
        }
        if (assignment==null) return;
        if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
            Vector<Long> assignedRooms = new Vector();
            for (StringTokenizer stk=new StringTokenizer(rooms,":");stk.hasMoreTokens();) {
                String token = stk.nextToken();
                if (token.trim().length()==0) continue;
                assignedRooms.add(Long.valueOf(token.substring(0, token.indexOf('@'))));
            }
            assignment = getSolver().getAssignment(getExam().getExamId(), assignment.getPeriodId(), assignedRooms);
        } else {
            TreeSet<ExamRoomInfo> assignedRooms = new TreeSet();
            for (StringTokenizer stk=new StringTokenizer(rooms,":");stk.hasMoreTokens();) {
                String token = stk.nextToken();
                if (token.trim().length()==0) continue;
                Long roomId = Long.valueOf(token.substring(0, token.indexOf('@')));
                ExamRoomInfo room = null;
                for (ExamRoomInfo r : getRooms()) {
                    if (r.getLocationId().equals(roomId)) { room = r; break; }
                }
                if (room!=null) assignedRooms.add(room);
            }
            assignment = new ExamAssignmentInfo(getExam().getExam(), assignment.getPeriod(), assignedRooms, iChange.getAssignmentTable());
        }
        iChange.addChange(assignment, getExamOldAssignment());
        if (iChange.isEmpty()) iChange = null; 
        update();
    }
    
    public void setSuggestion(int idx) {
        if (iSuggestions==null || iSuggestions.getSuggestions()==null || iSuggestions.getSuggestions().size()<=idx) return;
        iChange = iSuggestions.getSuggestions().elementAt(idx);
    }
    
    public void apply(HttpServletRequest request, ExamInfoForm form) {
        if (request.getParameter("pord")!=null)
            iPeriodTableOrd = Integer.parseInt(request.getParameter("pord"));
        iForm = form;
    }
    
    public void refreshRooms() {
        iRooms = null;
    }
    
    public void refreshSuggestions() {
        iSuggestions = null;
    }
    
    public static String dc2html(boolean html, int conf, int diff) {
        String ret = (conf<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+conf+"</font>":String.valueOf(conf));
        if (html && diff<0)
            ret += "<font color='"+PreferenceLevel.prolog2color("R")+"'> ("+diff+")</font>";
        if (html && diff>0)
            ret += "<font color='"+PreferenceLevel.prolog2color("P")+"'> (+"+diff+")</font>";
        if (!html && diff<0)
            ret += " ("+diff+")";
        if (!html && diff>0)
            ret += " (+"+diff+")";
        return ret;
    }
    
    public static String m2d2html(boolean html, int conf, int diff) {
        String ret = (conf<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+conf+"</font>":String.valueOf(conf));
        if (html && diff<0)
            ret += "<font color='"+PreferenceLevel.prolog2color("-2")+"'> ("+diff+")</font>";
        if (html && diff>0)
            ret += "<font color='"+PreferenceLevel.prolog2color("2")+"'> (+"+diff+")</font>";
        if (!html && diff<0)
            ret += " ("+diff+")";
        if (!html && diff>0)
            ret += " (+"+diff+")";
        return ret;
    }

    public static String btb2html(boolean html, int conf, int diff, int dconf, int ddiff) {
        String ret = (conf<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+conf+"</font>":String.valueOf(conf));
        if (html) {
            if (diff<0) ret += "<font color='"+PreferenceLevel.prolog2color("-1")+"'> ("+diff+"</font>";
            else if (diff>0) ret += "<font color='"+PreferenceLevel.prolog2color("1")+"'> (+"+diff+"</font>";
            else if (ddiff!=0) ret += " ("+String.valueOf(diff);
            if (ddiff<0) ret += "<font color='"+PreferenceLevel.prolog2color("-1")+"'> d:"+ddiff+"</font>";
            if (ddiff>0) ret += "<font color='"+PreferenceLevel.prolog2color("1")+"'> d:+"+ddiff+"</font>";
            if (diff<0) ret += "<font color='"+PreferenceLevel.prolog2color("-1")+"'>)</font>";
            else if (diff>0) ret += "<font color='"+PreferenceLevel.prolog2color("1")+"'>)</font>";
            else if (ddiff!=0) ret += ")";
        } else {
            if (diff<0) ret += " ("+diff;
            else if (diff>0) ret += " (+"+diff;
            else if (ddiff!=0) ret += " ("+String.valueOf(diff);
            if (ddiff<0) ret += " d:"+ddiff;
            if (ddiff>0) ret += " d:+"+ddiff;
            if (diff<0) ret += ")";
            else if (diff>0) ret += ")";
            else if (ddiff!=0) ret += ")";
        }
        return ret;
    }
    
    public String getPeriodsTable() {
        WebTable table = new WebTable(8, "Available Periods for "+getExam().getExamName(), "examInfo.do?op=Reorder&pord=%%", 
                new String[] {"Available<br>Period","Violated<br>Distributions", "Student<br>Direct", "Student<br>&gt; 2 A Day","Student<br>Back-To-Back", "Instructor<br>Direct", "Instructor<br>&gt; 2 A Day", "Instructor<br>Back-To-Back"},
                new String[] {"left", "left", "right", "right", "right", "right", "right", "right", "right"},
                new boolean[] { true, true, true, true, true, true, true, true});
        ExamAssignmentInfo current = getExamAssignment();
        for (ExamAssignmentInfo period : getPeriods()) {
            WebTable.WebTableLine line = table.addLine(
               "onClick=\"document.location='examInfo.do?op=Select&period="+period.getPeriodId()+"';\"",
               new String[] {
                    period.getPeriodAbbreviationWithPref(),
                    period.getDistributionConflictsHtml("<br>"),
                    dc2html(true, period.getNrDirectConflicts(), (current==null?0:period.getNrDirectConflicts()-current.getNrDirectConflicts())),
                    m2d2html(true, period.getNrMoreThanTwoConflicts(), (current==null?0:period.getNrMoreThanTwoConflicts()-current.getNrMoreThanTwoConflicts())),
                    btb2html(true, period.getNrBackToBackConflicts(), (current==null?0:period.getNrBackToBackConflicts()-current.getNrBackToBackConflicts()), 
                            period.getNrDistanceBackToBackConflicts(), (current==null?0:period.getNrDistanceBackToBackConflicts()-current.getNrDistanceBackToBackConflicts())),
                    dc2html(true, period.getNrInstructorDirectConflicts(), (current==null?0:period.getNrInstructorDirectConflicts()-current.getNrInstructorDirectConflicts())),
                    m2d2html(true, period.getNrInstructorMoreThanTwoConflicts(), (current==null?0:period.getNrInstructorMoreThanTwoConflicts()-current.getNrInstructorMoreThanTwoConflicts())),
                    btb2html(true, period.getNrInstructorBackToBackConflicts(), (current==null?0:period.getNrInstructorBackToBackConflicts()-current.getNrInstructorBackToBackConflicts()),
                            period.getNrInstructorDistanceBackToBackConflicts(), (current==null?0:period.getNrInstructorDistanceBackToBackConflicts()-current.getNrInstructorDistanceBackToBackConflicts()))
                }, new Comparable[] {
                    period.getPeriodOrd(),
                    period.getDistributionConflictsList(":"),
                    period.getNrDirectConflicts(),
                    period.getNrMoreThanTwoConflicts(),
                    period.getNrBackToBackConflicts(),
                    period.getNrInstructorDirectConflicts(),
                    period.getNrInstructorMoreThanTwoConflicts(),
                    period.getNrInstructorBackToBackConflicts()
                });
            ExamAssignment examAssignment = (iChange==null?null:iChange.getCurrent(iExam));
            if ((isExamAssigned() || examAssignment!=null) && period.getPeriodId().equals((examAssignment==null?getExamAssignment():examAssignment).getPeriodId())) {
                line.setBgColor("rgb(168,187,225)");
            }
        }
        return table.printTable(iPeriodTableOrd);
    }
    
    public Collection<ExamAssignmentInfo> getPeriods() {
        if (iPeriods==null) {
            if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
                iPeriods = getSolver().getPeriods(getExam().getExamId(), iChange);
            } else {
                try {
                    Hashtable<Long, Set<Exam>> studentExams = getExam().getExam().getStudentExams();
                    Hashtable<Assignment, Set<Long>> studentAssignments = getExam().getExam().getStudentAssignments();
                    iPeriods = new Vector<ExamAssignmentInfo>();
                    for (Iterator i=ExamPeriod.findAll(getExam().getExam().getSession().getUniqueId(), getExam().getExamType()).iterator();i.hasNext();) {
                        ExamPeriod period = (ExamPeriod)i.next();
                        try {
                            iPeriods.add(new ExamAssignmentInfo(getExam().getExam(), period, null, studentExams, studentAssignments, (iChange==null?null:iChange.getAssignmentTable())));
                        } catch (Exception e) {}
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return iPeriods;
    }
    
    protected Vector<ExamRoomInfo> findRooms(ExamPeriod period, int minRoomSize, int maxRoomSize, String filter, boolean allowConflicts) {
        Vector<ExamRoomInfo> rooms = new Vector<ExamRoomInfo>();
        boolean reqRoom = false;
        boolean reqBldg = false;
        boolean reqGroup = false;
        
        Exam exam = getExam().getExam(new ExamDAO().getSession());
        Set groupPrefs = exam.getPreferences(RoomGroupPref.class);
        Set roomPrefs = exam.getPreferences(RoomPref.class);
        Set bldgPrefs = exam.getPreferences(BuildingPref.class);
        Set featurePrefs = exam.getPreferences(RoomFeaturePref.class);
        
        TreeSet locations = Location.findAllExamLocations(period.getSession().getUniqueId(), period.getExamType());
        Hashtable<Long, Long> locationTable = Location.findExamLocationTable(period.getUniqueId());
        
        if (getExamAssignment()!=null) {
            if (getExamAssignment().getPeriod().equals(period) && getExamAssignment().getRooms()!=null)
                for (ExamRoomInfo room : getExamAssignment().getRooms())
                    locationTable.remove(room.getLocationId());
        }
        if (iChange!=null) {
            for (ExamAssignment conflict : iChange.getConflicts()) {
                if (conflict.getPeriod().equals(period) && conflict.getRooms()!=null)
                    for (ExamRoomInfo room : conflict.getRooms())
                        locationTable.remove(room.getLocationId());
            }
            for (ExamAssignment current : iChange.getAssignments()) {
                ExamAssignment initial = iChange.getInitial(current);
                if (initial!=null && initial.getPeriod().equals(period) && initial.getRooms()!=null)
                    for (ExamRoomInfo room : initial.getRooms())
                        locationTable.remove(room.getLocationId());
            }
            for (ExamAssignment current : iChange.getAssignments()) {
                if (!iExam.getExamId().equals(current.getExamId()) && current.getPeriod().equals(period) && current.getRooms()!=null)
                    for (ExamRoomInfo room : current.getRooms())
                        locationTable.put(room.getLocationId(), current.getExamId());
            }
        }
        
        for (Iterator i1=locations.iterator();i1.hasNext();) {
            Location room = (Location)i1.next();
            
            Long examId = locationTable.get(room.getUniqueId());
            if (!allowConflicts && examId!=null) continue;
            if (examId!=null && iChange!=null && iChange.getCurrent(examId)!=null) continue;
            
            int cap = (getExam().getSeatingType()==Exam.sSeatingTypeExam?room.getExamCapacity():room.getCapacity());
            if (minRoomSize>=0 && cap<minRoomSize) continue;
            if (maxRoomSize>=0 && cap>maxRoomSize) continue;
            
            if (PreferenceLevel.sProhibited.equals(room.getExamPreference(period).getPrefProlog())) continue;
            
            if (!match(room.getLabel(),filter)) continue;
            
            boolean shouldNotBeUsed = PreferenceLevel.sStronglyDiscouraged.equals(room.getExamPreference(period).getPrefProlog());
            
            boolean add = true;
            
            PreferenceCombination pref = new SumPreferenceCombination();
            
            // --- group preference ----------
            PreferenceCombination groupPref = PreferenceCombination.getDefault();
            for (Iterator i2=groupPrefs.iterator();i2.hasNext();) {
                RoomGroupPref p = (RoomGroupPref)i2.next();
                if (p.getRoomGroup().getRooms().contains(room))
                    groupPref.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
            }
            
            if (groupPref.getPreferenceProlog().equals(PreferenceLevel.sProhibited)) 
                add=false;
            
            if (reqGroup && !groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired)) add=false;
            
            if (!reqGroup && (groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired))) {
                reqGroup=true; 
                rooms.clear();
            }

            if (!groupPref.getPreferenceProlog().equals(PreferenceLevel.sProhibited) && !groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired))
                pref.addPreferenceProlog(groupPref.getPreferenceProlog());
            
            
            // --- room preference ------------
            String roomPref = room.getExamPreference(period).getPrefProlog();
            
            for (Iterator i2=roomPrefs.iterator();i2.hasNext();) {
                RoomPref p = (RoomPref)i2.next();
                if (room.equals(p.getRoom())) {
                    roomPref = p.getPrefLevel().getPrefProlog();
                    shouldNotBeUsed = false;
                    break;
                }
            }
            
            if (roomPref!=null && roomPref.equals(PreferenceLevel.sProhibited)) add=false;

            if (reqRoom && (roomPref==null || !roomPref.equals(PreferenceLevel.sRequired))) add=false;

            
            if (!reqRoom && (roomPref!=null && roomPref.equals(PreferenceLevel.sRequired))) {
                reqRoom=true; 
                rooms.clear();
            }
            
            if (roomPref!=null && !roomPref.equals(PreferenceLevel.sProhibited) && !roomPref.equals(PreferenceLevel.sRequired)) pref.addPreferenceProlog(roomPref);

            // --- building preference ------------
            Building bldg = (room instanceof Room ? ((Room)room).getBuilding() : null);

            String bldgPref = null;
            for (Iterator i2=bldgPrefs.iterator();i2.hasNext();) {
                BuildingPref p = (BuildingPref)i2.next();
                if (bldg!=null && bldg.equals(p.getBuilding())) {
                    bldgPref = p.getPrefLevel().getPrefProlog();
                    break;
                }
            }
            
            if (bldgPref!=null && bldgPref.equals(PreferenceLevel.sProhibited)) add=false;
            
            if (reqBldg && (bldgPref==null || !bldgPref.equals(PreferenceLevel.sRequired))) add=false;
            
            if (!reqBldg && (bldgPref!=null && bldgPref.equals(PreferenceLevel.sRequired))) {
                reqBldg = true;
                rooms.clear();
            }

            if (bldgPref!=null && !bldgPref.equals(PreferenceLevel.sProhibited) && !bldgPref.equals(PreferenceLevel.sRequired)) pref.addPreferenceProlog(bldgPref);
            
            // --- room features preference --------  
            boolean acceptableFeatures = true;
            PreferenceCombination featurePref = new MinMaxPreferenceCombination();
            for (Iterator i2=featurePrefs.iterator();i2.hasNext();) {
                RoomFeaturePref roomFeaturePref = (RoomFeaturePref)i2.next();
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
            
            if (!acceptableFeatures) add=false;
            
            if (!add || shouldNotBeUsed) continue;
            
            boolean conflict = false;
            
            rooms.add(new ExamRoomInfo(room, (examId==null?0:100)+pref.getPreferenceInt()));
        }
        
        return rooms;
    }
    
    public String getRoomTable() {
        Vector<ExamRoomInfo> rooms = getRooms();
        ExamAssignment examAssignment = (iChange==null?null:iChange.getCurrent(iExam));
        Collection<ExamRoomInfo> assigned = (examAssignment!=null?examAssignment.getRooms():isExamAssigned()?getExamAssignment().getRooms():null);
        if (rooms==null || rooms.isEmpty()) return "";
        Collections.sort(rooms, new Comparator<ExamRoomInfo>() {
            public int compare(ExamRoomInfo r1, ExamRoomInfo r2) {
                int cmp = 0;
                if (ExamInfoForm.sRoomOrdNameAsc.equals(iForm.getRoomOrder())) {
                    cmp = r1.getName().compareTo(r2.getName());
                } else if (ExamInfoForm.sRoomOrdNameDesc.equals(iForm.getRoomOrder())) {
                    cmp = -r1.getName().compareTo(r2.getName());
                } else if (ExamInfoForm.sRoomOrdSizeAsc.equals(iForm.getRoomOrder())) {
                    cmp = Double.compare(r1.getCapacity(getExam()),r2.getCapacity(getExam()));
                } else  if (ExamInfoForm.sRoomOrdSizeDesc.equals(iForm.getRoomOrder())) {
                    cmp = -Double.compare(r1.getCapacity(getExam()),r2.getCapacity(getExam()));
                } else {
                    cmp = r1.getName().compareTo(r2.getName());
                }
                if (cmp!=0) return cmp;
                cmp = r1.getName().compareTo(r2.getName());;
                if (cmp!=0) return cmp;
                return r1.getLocationId().compareTo(r2.getLocationId());
            }
        });
        String ret = "";
        ret += "<script language='javascript'>";
        ret += "function roomOver(source, id) { ";
        ret += "    document.getElementById('r'+id).style.backgroundColor='rgb(223,231,242)';";
        ret += "    document.getElementById('c'+id).style.backgroundColor='rgb(223,231,242)';";
        ret += "    source.style.cursor='hand';source.style.cursor='pointer';";
        ret += "}";
        ret += "var sCap = -1;";
        ret += "var sRooms = ':";
        if (assigned!=null && assigned.size()>0) {
            for (ExamRoomInfo room : assigned) {
                ret+=room.getLocationId()+"@"+room.getCapacity(getExam());
            }
        }
        ret += "';";
        ret += "var sNrRooms = "+(assigned!=null?assigned.size():0)+";";
        ret += "function roomSelected(id) {";
        ret += "    return sRooms.indexOf(':'+id+'@')>=0;";
        ret += "}";
        ret += "function roomOut(id) { ";
        ret += "    var bg = 'transparent';";
        ret += "    if (roomSelected(id)) bg='rgb(168,187,225)';";
        ret += "    document.getElementById('r'+id).style.backgroundColor=bg;";
        ret += "    document.getElementById('c'+id).style.backgroundColor=bg;";
        ret += "}";
        ret += "function roomClick(source, id, cap) { ";
        ret += "    if (sCap<0) {";
        ret += "        sCap = 0; sRooms=''; sNrRooms=0;";
        if (assigned!=null && assigned.size()>0) {
            for (ExamRoomInfo room : assigned) ret+="        roomOut("+room.getLocationId()+");";
        }
        ret += "    }";
        ret += "    var i = sRooms.indexOf(':'+id+'@');";
        ret += "    if (i>=0) {";
        ret += "        var j = sRooms.indexOf(':',i+1);";
        ret += "        sRooms = sRooms.substring(0, i)+(j>=0?sRooms.substring(j):'');";
        ret += "        sCap -= cap; sNrRooms--;";
        ret += "    } else {";
        ret += "        sRooms = sRooms + ':' + id + '@' + cap;";
        ret += "        sCap += cap; sNrRooms++;";
        ret += "        if (sNrRooms>"+getExam().getMaxRooms()+") {";
        ret += "            var fid = sRooms.substring(1, sRooms.indexOf('@'));";
        ret += "            var fcap = sRooms.substring(sRooms.indexOf('@')+1, sRooms.indexOf(':',1));";
        ret += "            sRooms = sRooms.substring(sRooms.indexOf(':',1));";
        ret += "            sCap -= fcap; sNrRooms--; roomOut(fid);";
        ret += "        };";
        ret += "    }";
        ret += "    roomOut(id);";
        ret += "    if (sCap>="+getExam().getNrStudents()+") document.location='examInfo.do?op=Select&room='+sRooms;";
        ret += "    var c = document.getElementById('roomCapacityCounter');";
        ret += "    if (c!=null) c.innerHTML = (sCap<"+getExam().getNrStudents()+"?'<font color=\"red\">'+sCap+'</font>':''+sCap);";
        ret += "}";
        ret += "</script>";
        ret += "<table border='0' cellspacing='0' cellpadding='3'>";
        int idx = 0;
        int step = 6;
        for (ExamRoomInfo room : rooms) {
            if ((idx%step)==0) {
                if (idx>0) ret +="</tr>";
                ret += "<tr>";
            }
            String style = "";
            if (assigned!=null && assigned.contains(room))
                style += "background-color:rgb(168,187,225);";
            String mouse = 
                "onMouseOver=\"roomOver(this,"+room.getLocationId()+");\" "+
                "onMouseOut=\"roomOut("+room.getLocationId()+");\" "+
                "onClick=\"roomClick(this,"+room.getLocationId()+","+room.getCapacity(getExam())+");\"";
            ret += "<td nowrap id='r"+room.getLocationId()+"' " +
                    (style.length()>0?"style='"+style+"' ":"")+mouse+">"+
                    room.toString()+"</td>";
            if ((idx%step)<step-1)
                style += "border-right: #646464 1px dashed;";
            ret += "<td id='c"+room.getLocationId()+"' "+
                    (style.length()>0?"style='"+style+"' ":"")+mouse+">"+
                    room.getCapacity(getExam())+"</td>";
            idx ++;
        }
        while ((idx%step)!=0) {
            ret += "<td colspan='2'>&nbsp;</td>";
            idx++;
        }
        ret += "</tr>";
        ret += "</table>";
        return ret;
    }
    
    public Vector<ExamRoomInfo> getRooms() {
        if (getExam().getMaxRooms()==0) return null;
        int minRoomSize = (iForm.getMinRoomSize()==null || iForm.getMinRoomSize().length()==0 ? -1 : Integer.parseInt(iForm.getMinRoomSize()));
        int maxRoomSize = (iForm.getMaxRoomSize()==null || iForm.getMaxRoomSize().length()==0 ? -1 : Integer.parseInt(iForm.getMaxRoomSize()));
        try {
            if (getSelectedAssignment()==null && !isExamAssigned()) return null;
            if (iRooms==null) {
                if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
                    iRooms = getSolver().getRooms(getExam().getExamId(), getSelectedAssignment()!=null?getSelectedAssignment().getPeriodId():getExamAssignment().getPeriodId(),
                            iChange, minRoomSize, maxRoomSize, iForm.getRoomFilter(), iForm.getAllowRoomConflict());
                } else {
                    iRooms = findRooms(getSelectedAssignment()!=null?getSelectedAssignment().getPeriod():getExamAssignment().getPeriod(),
                            minRoomSize, maxRoomSize, iForm.getRoomFilter(), iForm.getAllowRoomConflict());
                }
            }
            return iRooms;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public int getRoomSize() {
        ExamAssignment examAssignment = (iChange==null?null:iChange.getCurrent(iExam));
        if (examAssignment!=null) return examAssignment.getRoomSize();
        if (isExamAssigned()) return getExamAssignment().getRoomSize();
        return 0;
    }
    
    public ExamProposedChange getChange() {
        if (iChange==null || iChange.getAssignments().isEmpty()) return null;
        return iChange; 
    }
    
    public static boolean match(String name, String filter) {
        if (filter==null || filter.trim().length()==0) return true;
        String n = name.toUpperCase();
        StringTokenizer stk1 = new StringTokenizer(filter.toUpperCase(),";");
        while (stk1.hasMoreTokens()) {
            StringTokenizer stk2 = new StringTokenizer(stk1.nextToken()," ,");
            boolean match = true;
            while (match && stk2.hasMoreTokens()) {
                String token = stk2.nextToken().trim();
                if (token.length()==0) continue;
                if (token.indexOf('*')>=0 || token.indexOf('?')>=0) {
                    try {
                        String tokenRegExp = "\\s+"+token.replaceAll("\\.", "\\.").replaceAll("\\?", ".+").replaceAll("\\*", ".*")+"\\s";
                        if (!Pattern.compile(tokenRegExp).matcher(" "+n+" ").find()) match = false;
                    } catch (PatternSyntaxException e) { match = false; }
                } else if (n.indexOf(token)<0) match = false;
            }
            if (match) return true;
        }
        return false;        
    }
    
    public ExamConflictStatisticsInfo getCbs() {
        if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
            if (iCbs==null) 
                iCbs = getSolver().getCbsInfo(iExam.getExamId());
            return iCbs;
        }
        return null;
    }
    
    public ExamSuggestionsInfo getSuggestions() {
        if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
            if (iSuggestions==null) {
                try {
                    iSuggestions = getSolver().getSuggestions(iExam.getExamId(), iChange, iForm.getFilter(), iForm.getDepth(), iForm.getLimit(), iForm.getTimeout());
                } catch (Exception e) { e.printStackTrace(); }
            }
            return iSuggestions;
        }
        return null;
    }
    
    public boolean getCanComputeSuggestions() {
        return getSolver()!=null && getSolver().getExamType()==getExam().getExamType();
    }
    
    public String getSuggestionTable() {
        ExamSuggestionsInfo suggestions = getSuggestions();
        if (suggestions==null) return null;
        String ret = "<table border='0' cellspacing='0' cellpadding='3' width='100%'>";
        if (suggestions.getSuggestions()!=null && !suggestions.getSuggestions().isEmpty()) {
            ret += "<tr>";
            ret += "<td><i>Value</i></td>";
            ret += "<td><i>Examination</i></td>";
            ret += "<td><i>Period Change</i></td>";
            ret += "<td><i>Room Change</i></td>";
            ret += "<td><i>Direct</i></td>";
            ret += "<td><i>&gt;2 A Day</i></td>";
            ret += "<td><i>BTB</i></td>";
            ret += "</tr>";
            int idx = 0;
            for (ExamProposedChange suggestion : suggestions.getSuggestions()) {
                ret += suggestion.getHtmlLine(idx++);
            }
        }
        ret += "<tr><td colspan='7'><i>"+suggestions.getMessage()+"</i></td></tr>";
        ret += "</table>";
        return ret;
    }
    
    public boolean isSuggestionsTimeoutReached() {
        ExamSuggestionsInfo suggestions = getSuggestions();
        if (suggestions==null) return false;
        return suggestions.getTimeoutReached();
    }
}
