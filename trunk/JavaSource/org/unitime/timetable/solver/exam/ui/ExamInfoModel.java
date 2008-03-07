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
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import net.sf.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import net.sf.cpsolver.coursett.preference.PreferenceCombination;
import net.sf.cpsolver.coursett.preference.SumPreferenceCombination;

import org.unitime.commons.web.WebTable;
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
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.solver.exam.ExamSolverProxy;

/**
 * @author Tomas Muller
 */
public class ExamInfoModel implements Serializable {
    private transient ExamSolverProxy iSolver = null;
    private ExamInfo iExam = null;
    private ExamAssignmentInfo iExamAssignment = null;
    private Collection<ExamAssignmentInfo> iPeriods = null;
    private Collection<ExamRoomInfo> iRooms = null;
    private int iPeriodTableOrd = 0;
    
    public void setSolver(ExamSolverProxy solver) { iSolver = solver; }
    public ExamSolverProxy getSolver() { return iSolver; }

    public void clear() {
        iExam = null; iExamAssignment = null; iRooms = null; iPeriods = null; 
    }
    
    public ExamInfo getExam() {
        return iExam;
    }
    public ExamAssignmentInfo getExamAssignment() {
        if (iExam instanceof ExamAssignmentInfo)
            return (ExamAssignmentInfo)iExam;
        return null;
    }
    public boolean isExamAssigned() {
        return getExamAssignment()!=null && getExamAssignment().getPeriodId()!=null; 
    }
    
    public String assign() {
        if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
            return getSolver().assign(iExamAssignment);
        } else {
            org.hibernate.Session hibSession = new ExamDAO().getSession();
            return getExam().getExam(hibSession).assign(iExamAssignment, hibSession);
        }
    }
    
    public boolean getCanAssign() {
        return iExamAssignment!=null && iExamAssignment.isValid();
    }
    
    public String getAssignConfirm() {
        if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
            return "";
        } else {
            return "The selected assignment will be done directly in the database. Are you sure?";
        }
    }
    
    public void setExam(Exam exam) {
        iPeriods = null; iRooms = null; iExamAssignment = null;
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
    }
    public ExamAssignmentInfo getSelectedAssignment() {
        return iExamAssignment;
    }
    
    public void setPeriod(long periodId) {
        iRooms = null;
        if (isExamAssigned() && getExamAssignment().getPeriodId()==periodId) {
            iExamAssignment = null;
            return;
        }
        for (ExamAssignmentInfo period : getPeriods()) {
            if (periodId==period.getPeriodId()) {
                iExamAssignment = period;
                return;
            }
        }
        iExamAssignment = null;
    }
    
    public void setRooms(String rooms) {
        if (iExamAssignment==null) {
            if (isExamAssigned()) {
                for (ExamAssignmentInfo period : getPeriods()) {
                    if (getExamAssignment().getPeriodId().equals(period.getPeriodId())) {
                        iExamAssignment = period;
                        break;
                    }
                }
            }
        }
        if (iExamAssignment==null) return;
        iExamAssignment.getRooms().clear();
        for (StringTokenizer stk=new StringTokenizer(rooms,":");stk.hasMoreTokens();) {
            String token = stk.nextToken();
            if (token.trim().length()==0) continue;
            Long roomId = Long.valueOf(token.substring(0, token.indexOf('@')));
            ExamRoomInfo room = null;
            for (ExamRoomInfo r : getRooms()) {
                if (r.getLocationId().equals(roomId)) { room = r; break; }
            }
            if (room!=null) iExamAssignment.getRooms().add(room);
        }
    }
    
    public void apply(HttpServletRequest request) {
        if (request.getParameter("pord")!=null)
            iPeriodTableOrd = Integer.parseInt(request.getParameter("pord"));
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
        WebTable table = new WebTable(7, "Available Periods", "examInfo.do?op=Reorder&pord=%%", 
                new String[] {"Available<br>Period", "Student<br>Direct", "Student<br>&gt; 2 A Day","Student<br>Back-To-Back", "Instructor<br>Direct", "Instructor<br>&gt; 2 A Day", "Instructor<br>Back-To-Back"},
                new String[] {"left", "right", "right", "right", "right", "right", "right", "right"},
                new boolean[] { true, true, true, true, true, true, true});
        ExamAssignmentInfo current = getExamAssignment();
        for (ExamAssignmentInfo period : getPeriods()) {
            WebTable.WebTableLine line = table.addLine(
               "onClick=\"document.location='examInfo.do?op=Select&period="+period.getPeriodId()+"';\"",
               new String[] {
                    period.getPeriodAbbreviationWithPref(),
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
                    period.getNrDirectConflicts(),
                    period.getNrMoreThanTwoConflicts(),
                    period.getNrBackToBackConflicts(),
                    period.getNrInstructorDirectConflicts(),
                    period.getNrInstructorMoreThanTwoConflicts(),
                    period.getNrInstructorBackToBackConflicts()
                });
            if ((isExamAssigned() || iExamAssignment!=null) && period.getPeriodId().equals((iExamAssignment==null?getExamAssignment():iExamAssignment).getPeriodId())) {
                line.setBgColor("rgb(168,187,225)");
            }
        }
        return table.printTable(iPeriodTableOrd);
    }
    
    public Collection<ExamAssignmentInfo> getPeriods() {
        if (iPeriods==null) {
            if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
                iPeriods = getSolver().getPeriods(getExam().getExamId());
            } else {
                iPeriods = new Vector<ExamAssignmentInfo>();
                for (Iterator i=ExamPeriod.findAll(getExam().getExam().getSession().getUniqueId(), getExam().getExamType()).iterator();i.hasNext();) {
                    ExamPeriod period = (ExamPeriod)i.next();
                    try {
                        iPeriods.add(new ExamAssignmentInfo(getExam().getExam(), period, new Vector<ExamRoomInfo>()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return iPeriods;
    }
    
    protected Collection<ExamRoomInfo> findRooms(ExamPeriod period) {
        Collection<ExamRoomInfo> rooms = new Vector<ExamRoomInfo>();
        boolean reqRoom = false;
        boolean reqBldg = false;
        boolean reqGroup = false;

        Set groupPrefs = getExam().getExam().getPreferences(RoomGroupPref.class);
        Set roomPrefs = getExam().getExam().getPreferences(RoomPref.class);
        Set bldgPrefs = getExam().getExam().getPreferences(BuildingPref.class);
        Set featurePrefs = getExam().getExam().getPreferences(RoomFeaturePref.class);
            
        for (Iterator i1=Location.findAllAvailableExamLocations(period).iterator();i1.hasNext();) {
            Location room = (Location)i1.next();
            
            int cap = (getExam().getSeatingType()==Exam.sSeatingTypeExam?room.getExamCapacity():room.getCapacity());
            if (cap<getExam().getNrStudents()/getExam().getMaxRooms()) continue;
            if (cap>2*getExam().getNrStudents()) continue;
            
            if (PreferenceLevel.sProhibited.equals(room.getExamPreference(period).getPrefProlog())) continue;
            
            boolean add = true;
            
            PreferenceCombination pref = new SumPreferenceCombination();
            
            pref.addPreferenceProlog(room.getExamPreference(period).getPrefProlog());
            
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
            String roomPref = null;

            for (Iterator i2=roomPrefs.iterator();i2.hasNext();) {
                RoomPref p = (RoomPref)i2.next();
                if (room.equals(p.getRoom())) {
                    roomPref = p.getPrefLevel().getPrefProlog();
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
            
            if (!add) continue;
            
            rooms.add(new ExamRoomInfo(room, pref.getPreferenceInt()));
        }
        
        if (isExamAssigned() && period.getUniqueId().equals(getExamAssignment().getPeriodId()) && getExamAssignment().getRooms()!=null)
            rooms.addAll(getExamAssignment().getRooms());
        
        return rooms;
    }
    
    public String getRoomTable() {
        Collection<ExamRoomInfo> rooms = getRooms();
        Collection<ExamRoomInfo> assigned = (getSelectedAssignment()!=null?getSelectedAssignment().getRooms():isExamAssigned()?getExamAssignment().getRooms():null);
        if (rooms==null || rooms.isEmpty()) return "";
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
        ret += "    if (c!=null) c.innerHtml = (sCap<"+getExam().getNrStudents()+"?'<font color=\\\'red\\\'>'+sCap+'</font>':''+sCap);";
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
    
    public Collection<ExamRoomInfo> getRooms() {
        if (getSelectedAssignment()==null && !isExamAssigned()) return null;
        if (iRooms==null) {
            if (getSolver()!=null && getSolver().getExamType()==getExam().getExamType()) {
                iRooms = getSolver().getRooms(getExam().getExamId(), getSelectedAssignment()!=null?getSelectedAssignment().getPeriodId():getExamAssignment().getPeriodId());
            } else {
                iRooms = findRooms(getSelectedAssignment()!=null?getSelectedAssignment().getPeriod():getExamAssignment().getPeriod());
            }
        }
        return iRooms;
    }
    
    public int getRoomSize() {
        if (iExamAssignment!=null) return iExamAssignment.getRoomSize();
        if (isExamAssigned()) return getExamAssignment().getRoomSize();
        return 0;
    }
}
