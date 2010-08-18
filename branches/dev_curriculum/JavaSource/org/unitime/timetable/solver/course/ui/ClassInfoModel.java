/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2009 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;

import net.sf.cpsolver.coursett.Constants;
import net.sf.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import net.sf.cpsolver.coursett.preference.PreferenceCombination;
import net.sf.cpsolver.coursett.preference.SumPreferenceCombination;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LazyInitializationException;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ClassInfoForm;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.RoomSharingModel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.solver.course.ui.ClassAssignmentInfo.StudentConflict;
import org.unitime.timetable.util.DefaultRoomAvailabilityService;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller
 */
public class ClassInfoModel implements Serializable {
	private static final long serialVersionUID = 1373805772613891251L;
	private static Log sLog = LogFactory.getLog(ClassInfoModel.class);
    private ClassInfo iClass = null;
    private ClassInfoForm iForm = null;
    private ClassProposedChange iChange = null;
    private Collection<ClassAssignment> iTimes = null;
    private Vector<ClassRoomInfo> iRooms = null;
    private String iManagerExternalId = null;
    private boolean iShowStudentConflicts = "true".equalsIgnoreCase(ApplicationProperties.getProperty("tmtbl.classAssign.showStudentConflicts", "true"));
    private boolean iUnassignConflictingAssignments = false;
    
    public void clear(TimetableManager manager) {
        iClass = null; iChange = null; iRooms = null; iTimes = null; iUnassignConflictingAssignments = false;
        iManagerExternalId = manager.getExternalUniqueId();
    }
    
    public ClassInfo getClazz() {
        return iClass;
    }
    public ClassAssignmentInfo getClassOldAssignment() {
        if (iClass instanceof ClassAssignmentInfo)
            return (ClassAssignmentInfo)iClass;
        return null;
    }
    public ClassAssignmentInfo getClassAssignment() {
        if (iChange!=null && iChange.getConflict(iClass)!=null) return null;
        if (iClass instanceof ClassAssignmentInfo)
            return (ClassAssignmentInfo)iClass;
        return null;
    }
    public boolean isClassAssigned() {
        return getClassAssignment()!=null && getClassAssignment().getTime()!=null; 
    }
    
    public void update() throws Exception {
        if (iChange==null) return;
        Vector<ClassAssignment> assignments = new Vector(iChange.getAssignments());
        Hashtable<Long,ClassAssignment> table = iChange.getAssignmentTable();
        iUnassignConflictingAssignments = !iForm.getKeepConflictingAssignments();
        iChange.getAssignments().clear();
        for (ClassAssignment assignment : assignments) {
            iChange.getAssignments().add(new ClassAssignmentInfo(assignment.getClazz(),assignment.getTime(),assignment.getRooms(),table));
        }
        iChange.getConflicts().clear();
        for (ClassAssignment assignment : iChange.getAssignments()) {
        	// Check for room conflicts
        	if (iUnassignConflictingAssignments){
	            if (assignment.getRooms()!=null) for (ClassRoomInfo room : assignment.getRooms()) {
	            	if (!room.isIgnoreRoomChecks()){
		            	for (Assignment a : room.getLocation().getCommitedAssignments()) {
		            		if (assignment.getTime().overlaps(new ClassTimeInfo(a,0))) {
		            			if (iChange.getCurrent(a.getClassId())==null && iChange.getConflict(a.getClassId())==null)
		            				iChange.getConflicts().add(new ClassAssignment(a));
		            		}
		            	}
	            	}
	            }
	            
	            // Check for instructor conflicts
	            if (assignment.getInstructors()!=null) for (ClassInstructorInfo instructor : assignment.getInstructors()) {
	            	if (!instructor.isLead()) continue;
	            	for (Assignment a : instructor.getInstructor().getInstructor().getCommitedAssignments()) {
	            		if (assignment.getTime().overlaps(new ClassTimeInfo(a,0))) {
	            			if (iChange.getCurrent(a.getClassId())==null && iChange.getConflict(a.getClassId())==null)
	            				iChange.getConflicts().add(new ClassAssignment(a));
	            		}
	            	}
	            }
        	}
            // Check the course structure for conflicts
            Class_ clazz = assignment.getClazz(Class_DAO.getInstance().getSession());
            // a) all parents
            Class_ parent = clazz.getParentClass();
            while (parent!=null) {
            	if (iChange.getCurrent(parent.getUniqueId())==null && iChange.getConflict(parent.getUniqueId())==null) {
            		Assignment a = parent.getCommittedAssignment();
            		if (a!=null && assignment.getTime().overlaps(new ClassTimeInfo(a,0))) {
            			iChange.getConflicts().add(new ClassAssignment(a));
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
            	if (iChange.getCurrent(child.getUniqueId())==null && iChange.getConflict(child.getUniqueId())==null) {
            		Assignment a = child.getCommittedAssignment();
            		if (a!=null && assignment.getTime().overlaps(new ClassTimeInfo(a,0))) {
            			iChange.getConflicts().add(new ClassAssignment(a));
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
                	if (iChange.getCurrent(child.getUniqueId())==null && iChange.getConflict(child.getUniqueId())==null) {
                		Assignment a = child.getCommittedAssignment();
                		if (a!=null && assignment.getTime().overlaps(new ClassTimeInfo(a,0))) {
                			iChange.getConflicts().add(new ClassAssignment(a));
                		}
                	}
                	if (!child.getChildClasses().isEmpty())
                		children.addAll(child.getChildClasses());
            	}
            }
                        
            //TODO: Check for other HARD conflicts (e.g., distribution constraints)
        }
    }
    
    public String assign() {
        if (iChange==null) return "Nothing to assign.";
        sLog.info("About to be assigned: "+iChange);
        org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
        String message = null;
        for (ClassAssignment assignment : iChange.getConflicts()) {
        	try {
        		String m = assignment.getClazz(hibSession).unassignCommited(iManagerExternalId, hibSession);
                if (m!=null) message = (message==null?"":message+"\n")+m;
            } catch (Exception e) {
                message = (message==null?"":message+"\n")+"Unassignment of "+assignment.getClassName()+" failed, reason: "+e.getMessage();
            }
        }
        for (ClassAssignment assignment : iChange.getAssignments()) {
            try {
                String m = assignment.getClazz(hibSession).assignCommited(getAssignmentInfo(assignment), iManagerExternalId, hibSession);
                if (m!=null) message = (message==null?"":message+"\n")+m;
            } catch (Exception e) {
                message = (message==null?"":message+"\n")+"Assignment of "+assignment.getClassName()+" to "+assignment.getTime().getName()+" "+assignment.getRoomNames(", ")+" failed, reason: "+e.getMessage();
            }
        }
        // TODO: Update student sectioning information
        return message;
    }
    
    public boolean getCanAssign() {
        if (iChange==null) return false;
        for (ClassAssignment assignment : iChange.getAssignments())
            if (!assignment.isValid()) return false;
        return true;
    }
    
    public String getAssignConfirm() {
    	return "The selected assignment will be done directly in the database. Are you sure?";
    }
    
    public void setForm(ClassInfoForm form) {
        iForm = form;
    }
    
    public void setClazz(Class_ clazz) {
        iTimes = null; iRooms = null;
        if (clazz.getCommittedAssignment()!=null)
            iClass = new ClassAssignmentInfo(clazz.getCommittedAssignment());
        else  
            iClass = new ClassInfo(clazz);
        if (iChange!=null) {
            iChange.setSelected(clazz.getUniqueId());
            /*
            for (Iterator<ClassAssignmentInfo> i=iChange.getAssignments().iterator();i.hasNext();) {
                ClassAssignmentInfo a = i.next();
                if (!a.isValid()) i.remove();
            }
            */
        }
        if (clazz.getNbrRooms()>0) {
            iForm.setMinRoomSize(String.valueOf(clazz.getMinRoomLimit()));
            iForm.setMaxRoomSize(String.valueOf(Math.max(50,2*clazz.getClassLimit())));
        }
        iForm.setRoomFilter(null);
    }
    
    public ClassAssignmentInfo getAssignmentInfo(ClassAssignment assignment) throws Exception {
        if (assignment instanceof ClassAssignmentInfo) return (ClassAssignmentInfo)assignment;
        if (iChange!=null)
            return new ClassAssignmentInfo(assignment.getClazz(), assignment.getTime(), assignment.getRooms(), iChange.getAssignmentTable());
        else
            return new ClassAssignmentInfo(assignment.getClazz(), assignment.getTime(), assignment.getRooms());
    }
    
    public ClassAssignmentInfo getSelectedAssignment() throws Exception {
        if (iChange==null) return null;
        for (ClassAssignment assignment : iChange.getAssignments())
            if (assignment.getClassId().equals(iClass.getClassId())) return getAssignmentInfo(assignment);
        return null; 
    }
    
    public void setTime(String timeId) throws Exception {
        iRooms = null;
        if (iChange==null) iChange = new ClassProposedChange();
        for (ClassAssignment time : getTimes()) {
            if (timeId.equals(time.getTimeId())) {
                iChange.addChange(
                		new ClassAssignmentInfo(getClazz().getClazz(), time.getTime(), null, iChange.getAssignmentTable()), 
                		getClassOldAssignment());
            }
        }
        if (iChange.isEmpty()) iChange = null; 
        update();
    }
    
    public void delete(long classId) throws Exception {
        if (iChange==null) return;
        for (Iterator<ClassAssignmentInfo> i = iChange.getAssignments().iterator(); i.hasNext();) {
            ClassAssignmentInfo x = (ClassAssignmentInfo)i.next();
            if (x.getClassId().equals(classId)) i.remove();
        }
        update();
    }
    
    public void setRooms(String rooms) throws Exception {
        if (iChange==null) iChange = new ClassProposedChange();
        ClassAssignment assignment = iChange.getCurrent(iClass);
        if (assignment==null && isClassAssigned()) {
            for (ClassAssignment time : getTimes()) {
                if (getClassOldAssignment().getTimeId().equals(time.getTimeId())) {
                    assignment = time;
                    break;
                }
            }
        }
        if (assignment==null) return;
        TreeSet<ClassRoomInfo> assignedRooms = new TreeSet();
        for (StringTokenizer stk=new StringTokenizer(rooms,":");stk.hasMoreTokens();) {
            String token = stk.nextToken();
            if (token.trim().length()==0) continue;
            Long roomId = Long.valueOf(token.substring(0, token.indexOf('@')));
            ClassRoomInfo room = null;
            for (ClassRoomInfo r : getRooms()) {
                if (r.getLocationId().equals(roomId)) { room = r; break; }
            }
            if (room!=null) assignedRooms.add(room);
        }
        iChange.addChange(
        		new ClassAssignmentInfo(getClazz().getClazz(), assignment.getTime(), assignedRooms, iChange.getAssignmentTable()),
        		getClassOldAssignment());
        if (iChange.isEmpty()) iChange = null; 
        update();
    }
    
    public void apply(HttpServletRequest request, ClassInfoForm form) {
        iForm = form;
    }
    
    public void refreshRooms() {
        iRooms = null;
    }
        
    public String getTimesTable() {
    	try {
    		String ret = "";
            ret += "<script language='javascript'>";
            ret += "function timeOver(source, id) { ";
            ret += "    document.getElementById('t'+id).style.backgroundColor='rgb(223,231,242)';";
            if (iShowStudentConflicts)
            	ret += "    document.getElementById('c'+id).style.backgroundColor='rgb(223,231,242)';";
            ret += "    source.style.cursor='hand';source.style.cursor='pointer';";
            ret += "}";
            ret += "function timeOut(id) { ";
            ret += "    var bg = 'transparent';";
            ClassAssignment classAssignment = (iChange==null?null:iChange.getCurrent(iClass));
            if (isClassAssigned() || classAssignment!=null)
            	ret += "    if (id=='"+(classAssignment==null?getClassAssignment():classAssignment).getTimeId()+"') bg='rgb(168,187,225)';";
            ret += "    document.getElementById('t'+id).style.backgroundColor=bg;";
            if (iShowStudentConflicts)
            	ret += "    document.getElementById('c'+id).style.backgroundColor=bg;";
            ret += "}";
            ret += "function timeClick(source, id) { ";
            ret += "    displayLoading();";
            ret += "    document.location='classInfo.do?op=Select&time='+id;";
            ret += "}";
            ret += "</script>";
            ret += "<table border='0' cellspacing='0' cellpadding='3'>";
            int idx = 0;
            int step = 5;
            for (ClassAssignment time : getTimes()) {
                boolean initial = (getClassOldAssignment()!=null && getClassOldAssignment().getTimeId()!=null && getClassOldAssignment().getTimeId().equals(time.getTimeId()));
                if ((idx%step)==0) {
                    if (idx>0) ret +="</tr>";
                    ret += "<tr>";
                }
                String style = "";
                if ((isClassAssigned() || classAssignment!=null) && time.getTimeId().equals((classAssignment==null?getClassAssignment():classAssignment).getTimeId()))
                    style += "background-color:rgb(168,187,225);";
                if (initial)
                    style += "text-decoration:underline;";
                String mouse = 
                    "onMouseOver=\"timeOver(this,'"+time.getTimeId()+"');\" "+
                    "onMouseOut=\"timeOut('"+time.getTimeId()+"');\" "+
                    "onClick=\"timeClick(this,'"+time.getTimeId()+"');\"";
                if (iShowStudentConflicts) {
                	ret += "<td nowrap id='t"+time.getTimeId()+"' " +
                           (style.length()>0?"style='"+style+"' ":"")+mouse+">"+
                           time.getTime().getLongNameHtml()+"</td>";
                    if ((idx%step)<step-1)
                        style += "border-right: #646464 1px dashed;";
                    ret += "<td id='c"+time.getTimeId()+"' "+
                            (style.length()>0?"style='"+style+"' ":"")+mouse+">"+
                            ((ClassAssignmentInfo)time).getNrStudentCounflicts()+"</td>";
                } else {
                    if ((idx%step)<step-1)
                        style += "border-right: #646464 1px dashed;";
                    ret += "<td nowrap id='t"+time.getTimeId()+"' " +
                            (style.length()>0?"style='"+style+"' ":"")+mouse+">"+
                            time.getTime().getLongNameHtml()+"</td>";
                }
                idx ++;
            }
            while ((idx%step)!=0) {
            	if (iShowStudentConflicts)
            		ret += "<td colspan='2'>&nbsp;</td>";
            	else
            		ret += "<td>&nbsp;</td>";
                idx++;
            }
            ret += "</tr>";
            ret += "</table>";
            return ret;
        } catch (Exception e) {
        	iForm.setMessage(e.getMessage());
            sLog.error(e.getMessage(),e);
            return "";
        }
/*    	
        try {
            WebTable table = new WebTable(8, "Available Times for "+getClazz().getClassName(), "classInfo.do?op=Reorder&pord=%%", 
                    new String[] {"Time"},
                    new String[] {"left"},
                    new boolean[] { true});
            ClassAssignmentInfo current = getClassAssignment();
            for (ClassAssignmentInfo time : getTimes()) {
                boolean initial = (getClassOldAssignment()!=null && getClassOldAssignment().getTimeId()!=null && getClassOldAssignment().getTimeId().equals(time.getTimeId()));
                WebTable.WebTableLine line = table.addLine(
                   "onClick=\"displayLoading();document.location='classInfo.do?op=Select&time="+time.getTimeId()+"';\"",
                   new String[] {
                        (initial?"<u>":"")+time.getTime().getLongNameHtml()+(initial?"</u>":"")
                    }, new Comparable[] {
                        time.getTime()
                    });
                ClassAssignment ClassAssignment = (iChange==null?null:iChange.getCurrent(iClass));
                if ((isClassAssigned() || ClassAssignment!=null) && time.getTimeId().equals((ClassAssignment==null?getClassAssignment():ClassAssignment).getTimeId())) {
                    line.setBgColor("rgb(168,187,225)");
                }
            }
            return table.printTable(iTimesTableOrd);
        } catch (Exception e) {
            Debug.error(e);
            return null;
        }
        */
    }
    
    public TreeSet<StudentConflict> getStudentConflicts() {
    	TreeSet<StudentConflict> ret = new TreeSet();
    	if (iChange!=null) {
    		HashSet<String> ids = new HashSet();
    		for (ClassAssignmentInfo assignment:iChange.getAssignments()) {
    			for (StudentConflict conf:assignment.getStudentConflicts()) {
    				String id = (assignment.getClassId().compareTo(conf.getOtherClass().getClassId())<0?
    						assignment.getClassId()+":"+conf.getOtherClass().getClassId():
    			            conf.getOtherClass().getClassId()+":"+assignment.getClassId());
    				if (ids.add(id)) ret.add(conf);
    			}
    		}
    	} else if (getClassAssignment()!=null) {
    		ret.addAll(getClassAssignment().getStudentConflicts());
    	}
    	return ret;
    }
    
    public String getStudentConflictTable() {
        String ret = "<table border='0' width='100%' cellspacing='0' cellpadding='3'>";
        ret += "<tr>";
        ret += "<td><i>Students</i></td>";
        ret += "<td><i>Class</i></td>";
        ret += "<td><i>Time</i></td>";
        ret += "<td><i>Room</i></td>";
        ret += "</tr>";
        boolean empty = true;
        for (StudentConflict conf:getStudentConflicts()) {
        	ret += conf.toHtml2();
        	empty = false;
        }
        if (empty) ret += "<tr><td colspan='4'><i>There are no student conflicts.</i></td></tr>";
        ret += "</table>";
        return ret;
    }

    
    public Collection<ClassAssignment> getTimes() {
        if (iTimes==null) {
            iTimes = new Vector<ClassAssignment>();
            Class_ clazz = getClazz().getClazz();
            Set timePrefs = clazz.effectivePreferences(TimePref.class);
            if (timePrefs.isEmpty()) {
            	iForm.setMessage("Class "+getClazz().getClassName()+" has no time pattern selected.");
            	return iTimes;
            }
            DatePattern datePattern = clazz.effectiveDatePattern();
            if (datePattern==null) {
            	iForm.setMessage("Class "+getClazz().getClassName()+" has no date pattern selected.");
            	return iTimes;
            }
            boolean onlyReq = false;
            for (Iterator i1=timePrefs.iterator();i1.hasNext();) {
            	TimePref timePref = (TimePref)i1.next();
            	TimePatternModel pattern = timePref.getTimePatternModel();
            	if (pattern.isExactTime() || pattern.countPreferences(PreferenceLevel.sRequired)>0)
            		onlyReq = true;
            }
            if (onlyReq) {
            	sLog.debug("Class "+getClazz().getClassName()+" has required times");
            }
            for (Iterator i1=timePrefs.iterator();i1.hasNext();) {
            	TimePref timePref = (TimePref)i1.next();
            	TimePatternModel pattern = timePref.getTimePatternModel();
            	if (pattern.isExactTime()) {
            		int length = ExactTimeMins.getNrSlotsPerMtg(pattern.getExactDays(),clazz.getSchedulingSubpart().getMinutesPerWk().intValue());
            		int breakTime = ExactTimeMins.getBreakTime(pattern.getExactDays(),clazz.getSchedulingSubpart().getMinutesPerWk().intValue()); 
            		ClassTimeInfo time = new ClassTimeInfo(pattern.getExactDays(),pattern.getExactStartSlot(),length,PreferenceLevel.sIntLevelNeutral,timePref.getTimePattern(),datePattern,breakTime);
            		if (iShowStudentConflicts)
            			iTimes.add(new ClassAssignmentInfo(clazz, time, null, (iChange==null?null:iChange.getAssignmentTable())));
            		else
            			iTimes.add(new ClassAssignment(clazz, time, null));
                    continue;
            	}

            	if (clazz.getSchedulingSubpart().getMinutesPerWk().intValue()!=pattern.getMinPerMtg()*pattern.getNrMeetings()) {
            		sLog.warn("Class "+getClazz().getClassName()+" has "+clazz.getSchedulingSubpart().getMinutesPerWk()+" minutes per week, but "+pattern.getName()+" time pattern selected.");
            	}
                
                for (int time=0;time<pattern.getNrTimes(); time++) {
                	times: for (int day=0;day<pattern.getNrDays(); day++) {
                        String pref = pattern.getPreference(day,time);
                        if (onlyReq && !pref.equals(PreferenceLevel.sRequired)) {
                            pref = PreferenceLevel.sProhibited;
                        }
                        ClassTimeInfo loc = new ClassTimeInfo(
                                pattern.getDayCode(day),
                                pattern.getStartSlot(time),
                                pattern.getSlotsPerMtg(),
                                PreferenceLevel.prolog2int(pref),
                                timePref.getTimePattern(),
                                datePattern,
                                pattern.getBreakTime());
                        
                        if (iChange!=null) {
                            for (ClassAssignment current : iChange.getAssignments()) {
                            	if (!current.getClassId().equals(getClazz().getClassId())) {
                            		boolean canConflict = false;
                            		if (current.getParents().contains(getClazz().getClassId())) canConflict = true;
                            		if (getClazz().getParents().contains(current.getClassId())) canConflict = true;
                            		if (current.getConfligId().equals(getClazz().getConfligId()) && current.isSingleClass()) canConflict = true;
                            		if (current.shareInstructor(getClazz())) canConflict = true;
                            		if (canConflict && loc.overlaps(current.getTime())) continue times;
                            	}
                            }
                        }
                        
                        if (iShowStudentConflicts)
                        	 iTimes.add(new ClassAssignmentInfo(clazz, loc, null, (iChange==null?null:iChange.getAssignmentTable())));
                        else
                        	iTimes.add(new ClassAssignment(clazz, loc, null));
                    }
                }
            }
            
            if (iTimes.isEmpty()) {
            	iForm.setMessage("Class "+getClazz().getClassName()+" has no available time.");
            	return iTimes;
            }
        }
        return iTimes;
    }
    
    Hashtable<Long,Hashtable> iRoomPreferences = new Hashtable();
    private PreferenceLevel getRoomPreference(Department department, Long locationId) {
    	Hashtable roomPreferencesThisDept = iRoomPreferences.get(department.getUniqueId());
    	if (roomPreferencesThisDept==null) {
    		roomPreferencesThisDept = new Hashtable();
    		iRoomPreferences.put(department.getUniqueId(), roomPreferencesThisDept);
    		for (Iterator k=department.getPreferences(RoomPref.class).iterator();k.hasNext();) {
    			RoomPref pref = (RoomPref)k.next();
    			roomPreferencesThisDept.put(pref.getRoom().getUniqueId(),pref.getPrefLevel());
    		}
    	}
    	return (PreferenceLevel)roomPreferencesThisDept.get(locationId);
    }
    
    protected List findAllRooms(Long sessionId) {
		String a = "", b = "";
		if (iForm.getRoomFeatures()!=null && iForm.getRoomFeatures().length>0) {
			for (int i=0;i<iForm.getRoomFeatures().length;i++) {
				a+= ", GlobalRoomFeature f"+i;
				b+= " and f"+i+".uniqueId="+iForm.getRoomFeatures()[i]+" and f"+i+" in elements(r.features)";
			}
		}
        if (iForm.getRoomGroups()!=null && iForm.getRoomGroups().length>0) {
            b+= " and (";
            for (int i=0;i<iForm.getRoomGroups().length;i++) {
                if (i>0) b+=" or";
                a+= ", RoomGroup g"+i;
                b+= " (g"+i+".uniqueId="+iForm.getRoomGroups()[i]+" and g"+i+" in elements(r.roomGroups))";
            }
            b+=")";
        }
        if (iForm.getRoomTypes()!=null && iForm.getRoomTypes().length>0) {
            b+= " and r.roomType.uniqueId in (";
            for (int i=0;i<iForm.getRoomTypes().length;i++) {
                if (i>0) b+=",";
                b+= iForm.getRoomTypes()[i];
            }
            b+=")";
        }    	
        String query = "select r from Location r " + a + " where r.session.uniqueId=:sessionId " + b;
        return LocationDAO.getInstance().getSession().createQuery(query).setLong("sessionId", sessionId).setCacheable(true).list();
    }
    
    protected Vector<ClassRoomInfo> findRooms(ClassTimeInfo period, int minRoomSize, int maxRoomSize, String filter, boolean allowConflicts, boolean showAllRooms) {
    	Vector<ClassRoomInfo> rooms = new Vector<ClassRoomInfo>();
        
        Class_ clazz = getClazz().getClazz(Class_DAO.getInstance().getSession());
        int nrRooms = (clazz.getNbrRooms()==null?1:clazz.getNbrRooms().intValue());
        iRoomPreferences.clear();

        Set groupPrefs = clazz.effectivePreferences(RoomGroupPref.class);
        Set roomPrefs = clazz.effectivePreferences(RoomPref.class);
        Set bldgPrefs = clazz.effectivePreferences(BuildingPref.class);
        Set featurePrefs = clazz.effectivePreferences(RoomFeaturePref.class);
                
        if (nrRooms>0) {
        	int minClassLimit = clazz.getExpectedCapacity().intValue();
        	int maxClassLimit = clazz.getMaxExpectedCapacity().intValue();
        	if (maxClassLimit<minClassLimit) maxClassLimit = minClassLimit;
        	float room2limitRatio = clazz.getRoomRatio().floatValue();
        	int roomCapacity = (int)Math.ceil(minClassLimit<=0?room2limitRatio:room2limitRatio*minClassLimit);
        	//TODO: Use parameters from the default solver configuration
            int discouragedCapacity = (int)Math.round(0.99 * roomCapacity);
            int stronglyDiscouragedCapacity = (int)Math.round(0.98 * roomCapacity);
            
    		Calendar cal = Calendar.getInstance(Locale.US);
    		cal.setTime(new Date());
    		cal.set(Calendar.HOUR_OF_DAY, 0);
    		cal.set(Calendar.MINUTE, 0);
    		cal.set(Calendar.SECOND, 0);
    		Date today = cal.getTime();

    		Date[] bounds = DatePattern.getBounds(clazz.getSessionId());
    		
            boolean reqRoom = false;
            boolean reqBldg = false;
            boolean reqGroup = false;

 			Set availRooms = clazz.getAvailableRooms();
        	rooms: for (Iterator i1=availRooms.iterator();i1.hasNext();) {
        		Location room = (Location)i1.next();
        		if (iForm.getRoomTypes()!=null && iForm.getRoomTypes().length>0) {
        			boolean ok = false;
        			for (int i=0;i<iForm.getRoomTypes().length;i++)
        				if (room.getRoomType().getUniqueId().equals(iForm.getRoomTypes()[i])) {
        					ok = true; break;
        				}
        			if (!ok) {
        				i1.remove(); continue rooms;
        			}
        		}
        		if (iForm.getRoomFeatures()!=null && iForm.getRoomFeatures().length>0) {
            		for (int i=0;i<iForm.getRoomFeatures().length;i++)
            			if (!room.hasFeature(iForm.getRoomFeatures()[i])) {
            				i1.remove(); continue rooms;
            			}
        		}
        		if (iForm.getRoomGroups()!=null && iForm.getRoomGroups().length>0) {
        			for (int i=0;i<iForm.getRoomGroups().length;i++)
        				if (room.hasGroup(iForm.getRoomGroups()[i])) continue rooms;
        			i1.remove();
        		}
        	}
 			
 			Set allRooms = availRooms;
 			if (showAllRooms) {
 				allRooms = new TreeSet(availRooms);
 				allRooms.addAll(findAllRooms(getClazz().getClazz().getSessionId()));
 			}
 			
 			Long departmentId = getClazz().getClazz().getManagingDept().getUniqueId();
 			
 			Hashtable<Location,Integer> filteredRooms = new Hashtable();
 			Set<Long> permIds = new HashSet();
        	rooms: for (Iterator i1=allRooms.iterator();i1.hasNext();) {
        		Location room = (Location)i1.next();
        		boolean add=true;
        		
                if (minRoomSize>=0 && room.getCapacity()<minRoomSize) continue;
                if (maxRoomSize>=0 && room.getCapacity()>maxRoomSize) continue;
        		
        		if (!match(room.getLabel(),filter)) continue;
        		
        		PreferenceCombination pref = new SumPreferenceCombination();
        		
        		if (showAllRooms && !availRooms.contains(room)) pref.addPreferenceProlog(PreferenceLevel.sProhibited);
        		
        		RoomSharingModel sharingModel = room.getRoomSharingModel();
                if (sharingModel!=null) {
                	sharing: for (int d = 0; d<Constants.NR_DAYS; d++) {
                		if ((Constants.DAY_CODES[d] & period.getDayCode())==0) continue;
                		int startTime = period.getStartSlot() / 6;
                		int endTime = (period.getStartSlot()+period.getLength()-1) / 6;
                		for (int t = startTime; t<=endTime; t++) {
                			Long px = Long.valueOf(sharingModel.getPreference(d,t));
                			if (px.equals(RoomSharingModel.sNotAvailablePref)) {
                				if (showAllRooms) {
                					pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                					break sharing;
                				} else {
                        			if (room.getLabel().equals(filter)) iForm.setMessage("Room "+room.getLabel()+" is not available for "+period.getLongName()+" due to the room sharing preferences.");
                    				continue rooms;
                				}
                			}
                			if (px.equals(RoomSharingModel.sFreeForAllPref)) continue;
                			if (departmentId!=null && !departmentId.equals(px)) {
                				if (showAllRooms) {
                					pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                					break sharing;
                				} else {
                        			if (room.getLabel().equals(filter)) iForm.setMessage("Room "+room.getLabel()+" is not available for "+period.getLongName()+" due to the room sharing preferences.");
                					continue rooms;
                				}
                            }
                        }
                    }
                }

        		
        		// --- group preference ----------
        		PreferenceCombination groupPref = PreferenceCombination.getDefault();
        		for (Iterator i2=groupPrefs.iterator();i2.hasNext();) {
        			RoomGroupPref p = (RoomGroupPref)i2.next();
        			if (p.getRoomGroup().getRooms().contains(room))
        				groupPref.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
        		}
        		
        		if (groupPref.getPreferenceProlog().equals(PreferenceLevel.sProhibited)) {
                    pref.addPreferenceProlog(PreferenceLevel.sProhibited);
        		}
        		
                if (reqGroup && !groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired)) {
                  	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                }
                
                if (!reqGroup && (groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired))) {
                	reqGroup=true; 
                    for (Enumeration e=rooms.elements();e.hasMoreElements();) {
                    	ClassRoomInfo r = (ClassRoomInfo)e.nextElement();
                        r.setPreference(r.getPreference()+100);
                    }
                }

                if (!groupPref.getPreferenceProlog().equals(PreferenceLevel.sProhibited) && !groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired))
                	pref.addPreferenceProlog(groupPref.getPreferenceProlog());
        		
                
                // --- room preference ------------
        		String roomPref = null;
        		
        		PreferenceLevel roomPreference = getRoomPreference(clazz.getManagingDept(),room.getUniqueId());
        		if (roomPreference!=null) {
        			roomPref = roomPreference.getPrefProlog();
    			}
    			
    			for (Iterator i2=roomPrefs.iterator();i2.hasNext();) {
        			RoomPref p = (RoomPref)i2.next();
        			if (room.equals(p.getRoom())) {
        				roomPref = p.getPrefLevel().getPrefProlog();
        				break;
        			}
        		}
        		
                if (roomPref!=null && roomPref.equals(PreferenceLevel.sProhibited)) {
                    pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                }
                
                if (reqRoom && (roomPref==null || !roomPref.equals(PreferenceLevel.sRequired))) {
                  	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                }
                
                if (!reqRoom && (roomPref!=null && roomPref.equals(PreferenceLevel.sRequired))) {
                    reqRoom=true; 
                    for (Enumeration e=rooms.elements();e.hasMoreElements();) {
                        ClassRoomInfo r = (ClassRoomInfo)e.nextElement();
                        r.setPreference(r.getPreference()+100);
                    }
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
        		
                if (bldgPref!=null && bldgPref.equals(PreferenceLevel.sProhibited)) {
                  	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                }
                
                if (reqBldg && (bldgPref==null || !bldgPref.equals(PreferenceLevel.sRequired))) {
                   	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                }
                
                if (!reqBldg && (bldgPref!=null && bldgPref.equals(PreferenceLevel.sRequired))) {
                    reqBldg = true;
                    for (Enumeration e=rooms.elements();e.hasMoreElements();) {
                        ClassRoomInfo r = (ClassRoomInfo)e.nextElement();
                        r.setPreference(r.getPreference()+100);
                    }
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
                
                if (!acceptableFeatures) {
                  	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                }
                
                
        		// --- room size ----------------- 
                if (room.getCapacity().intValue()<stronglyDiscouragedCapacity) {
              		pref.addPreferenceInt(1000);
                }
                else if (room.getCapacity().intValue()<discouragedCapacity) {
                    pref.addPreferenceProlog(PreferenceLevel.sStronglyDiscouraged);
                }
                else if (room.getCapacity().intValue()<roomCapacity) {
                	pref.addPreferenceProlog(PreferenceLevel.sDiscouraged);
                }
                
                int prefInt = pref.getPreferenceInt();
                
                if (!add) continue;
                
                filteredRooms.put(room, prefInt);
                permIds.add(room.getPermanentId());
            }
 			
            //TODO: This might still be done much faster.
 			Vector <Date>datesToCheck = null;
 			if ("true".equals(ApplicationProperties.getProperty("tmtbl.classAssign.ignorePastMeetings", "true"))) {
 				datesToCheck = new Vector<Date>();
 	 			for(Date aDate : period.getDates()){
 	 				if (aDate.compareTo(today) > 0)
 	 					datesToCheck.add(aDate);
 	 			}
 			} else {
 				datesToCheck = period.getDates();
 			}
            Hashtable<Long,Set<Long>> room2classIds = Location.findClassLocationTable(permIds, period.getStartSlot(), period.getLength(), period.getDates());
            
            Hashtable<Long,Set<Event>> room2events = null;
            if (RoomAvailability.getInstance()!=null && RoomAvailability.getInstance() instanceof DefaultRoomAvailabilityService) {
            	room2events = Location.findEventTable(permIds, period.getStartSlot(), period.getLength(), datesToCheck);
            }

 			rooms: for (Map.Entry<Location, Integer> entry: filteredRooms.entrySet()) {
 				Location room = entry.getKey();
 				int prefInt = entry.getValue();
 				String note = null;
 				
 				Set<Long> classIds = room2classIds.get(room.getPermanentId());
 				if (classIds==null) classIds = new HashSet();
 				
                // Fix the location table with the current assignment
                if (getClassAssignment()!=null && getClassAssignment().hasRoom(room.getUniqueId()) && getClassAssignment().getTime().overlaps(period))
                	classIds.remove(getClassAssignment().getClassId());
                if (iChange!=null) {
                    for (ClassAssignment conflict : iChange.getConflicts()) {
                    	if (conflict.hasRoom(room.getUniqueId()) && conflict.getTime().overlaps(period))
                    		classIds.remove(conflict.getClassId());
                    }
                    for (ClassAssignment current : iChange.getAssignments()) {
                    	ClassAssignment initial = iChange.getInitial(current);
                        if (initial!=null && initial.hasRoom(room.getUniqueId()) && initial.getTime().overlaps(period))
                        	classIds.remove(initial.getClassId());
                    }
                    for (ClassAssignment current : iChange.getAssignments()) {
                        if (!getClazz().getClassId().equals(current.getClassId()) && current.hasRoom(room.getUniqueId()) && current.getTime().overlaps(period))
                        	classIds.add(current.getClassId());
                    }
                }

                if (!allowConflicts && classIds!=null && !classIds.isEmpty()) {
                	Long classId = (Long)classIds.iterator().next();
        			if (room.getLabel().equals(filter)) iForm.setMessage("Room "+room.getLabel()+" is not available for "+period.getLongName()+" due to the class "+Class_DAO.getInstance().get(classId).getClassLabel()+".");
                	continue;
                }
                if (classIds!=null && !classIds.isEmpty()) {
                	prefInt += 10000;
                	note = "Conflicts with "+Class_DAO.getInstance().get(classIds.iterator().next()).getClassLabel();
                }
                if (classIds!=null && iChange!=null) {
                	for (Long classId: classIds) {
                		if (iChange.getCurrent(classId)!=null) {
                        	if (room.getLabel().equals(filter)) iForm.setMessage("Room "+room.getLabel()+" is not available for "+period.getLongName()+" due to the class "+Class_DAO.getInstance().get(classId).getClassLabel()+".");
                        	continue rooms;
                		}
                	}
                }

                if (room2events!=null) {
                	Set<Event> conflicts = room2events.get(room.getPermanentId());
                	if (conflicts!=null && !conflicts.isEmpty()) {
            			if (room.getLabel().equals(filter)) iForm.setMessage("Room "+room.getLabel()+" is not available for "+period.getLongName()+" due to "+conflicts);
        				sLog.info("Room "+room.getLabel()+" is not available for "+period.getLongName()+" due to "+conflicts);
        				continue rooms;
                	}
                } else if (RoomAvailability.getInstance()!=null) {
            		Collection<TimeBlock> times = RoomAvailability.getInstance().getRoomAvailability(
                            room,
                            bounds[0], bounds[1], 
                            RoomAvailabilityInterface.sClassType);
            		if (times != null && !times.isEmpty()) {
                		Collection<TimeBlock> timesToCheck = null;
                		if ("true".equals(ApplicationProperties.getProperty("tmtbl.classAssign.ignorePastMeetings", "true"))) {
                			timesToCheck = new Vector();
                			for (TimeBlock time: times) {
                				if (time.getEndTime().compareTo(today) > 0) 
                					timesToCheck.add(time);
                			}
                		} else {
                			timesToCheck = times;
                		}
                		TimeBlock time = period.overlaps(timesToCheck);
                		if (time!=null) {
                			if (room.getLabel().equals(filter)) iForm.setMessage("Room "+room.getLabel()+" is not available for "+period.getLongName()+" due to "+time);
            				sLog.info("Room "+room.getLabel()+" is not available for "+period.getLongName()+" due to "+time);
            				continue rooms;
                		}
            		}
                }
                
                rooms.addElement(new ClassRoomInfo(room, prefInt, note));
 			}
        }
        
        return rooms;
    }
    
    public String getRoomTable() {
        try {
            Vector<ClassRoomInfo> rooms = getRooms();
            ClassAssignment ClassAssignment = (iChange==null?null:iChange.getCurrent(iClass));
            Collection<ClassRoomInfo> assigned = (ClassAssignment!=null?ClassAssignment.getRooms():isClassAssigned()?getClassAssignment().getRooms():null);
            Collection<ClassRoomInfo> original = (getClassOldAssignment()!=null?getClassOldAssignment().getRooms():null);
            if (rooms==null || rooms.isEmpty()) return "";
            Collections.sort(rooms, new Comparator<ClassRoomInfo>() {
                public int compare(ClassRoomInfo r1, ClassRoomInfo r2) {
                    int cmp = 0;
                    if (ClassInfoForm.sRoomOrdNameAsc.equals(iForm.getRoomOrder())) {
                        cmp = r1.getName().compareTo(r2.getName());
                    } else if (ClassInfoForm.sRoomOrdNameDesc.equals(iForm.getRoomOrder())) {
                        cmp = -r1.getName().compareTo(r2.getName());
                    } else if (ClassInfoForm.sRoomOrdSizeAsc.equals(iForm.getRoomOrder())) {
                        cmp = Double.compare(r1.getCapacity(),r2.getCapacity());
                    } else  if (ClassInfoForm.sRoomOrdSizeDesc.equals(iForm.getRoomOrder())) {
                        cmp = -Double.compare(r1.getCapacity(),r2.getCapacity());
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
            ret += "var sRooms = '";
            if (assigned!=null && assigned.size()>0) {
                for (ClassRoomInfo room : assigned) {
                    ret+=":"+room.getLocationId()+"@"+room.getCapacity();
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
                for (ClassRoomInfo room : assigned) ret+="        roomOut("+room.getLocationId()+");";
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
            ret += "        if (sNrRooms>"+getClazz().getNumberOfRooms()+") {";
            ret += "            var fid = sRooms.substring(1, sRooms.indexOf('@'));";
            ret += "            var fcap = sRooms.substring(sRooms.indexOf('@')+1, sRooms.indexOf(':',1));";
            ret += "            sRooms = sRooms.substring(sRooms.indexOf(':',1));";
            ret += "            sCap -= fcap; sNrRooms--; roomOut(fid);";
            ret += "        };";
            ret += "    }";
            ret += "    roomOut(id);";
            ret += "    if (sNrRooms=="+getClazz().getNumberOfRooms()+") {displayLoading(); document.location='classInfo.do?op=Select&room='+sRooms;}";
            ret += "    var c = document.getElementById('roomCapacityCounter');";
            ret += "    if (c!=null) c.innerHTML = (sCap<"+getClazz().getClassLimit()+"?'<font color=\"red\">'+sCap+'</font>':''+sCap);";
            ret += "}";
            ret += "</script>";
            ret += "<table border='0' cellspacing='0' cellpadding='3'>";
            int idx = 0;
            int step = 6;
            for (ClassRoomInfo room : rooms) {
                if ((idx%step)==0) {
                    if (idx>0) ret +="</tr>";
                    ret += "<tr>";
                }
                String style = "";
                if (assigned!=null && assigned.contains(room))
                    style += "background-color:rgb(168,187,225);";
                if (original!=null && original.contains(room))
                    style += "text-decoration:underline;";
                String mouse = 
                    "onMouseOver=\"roomOver(this,"+room.getLocationId()+");\" "+
                    "onMouseOut=\"roomOut("+room.getLocationId()+");\" "+
                    "onClick=\"roomClick(this,"+room.getLocationId()+","+room.getCapacity()+");\"";
                ret += "<td nowrap id='r"+room.getLocationId()+"' " +
                        (style.length()>0?"style='"+style+"' ":"")+mouse+">"+
                        room.toString()+"</td>";
                if ((idx%step)<step-1)
                    style += "border-right: #646464 1px dashed;";
                ret += "<td id='c"+room.getLocationId()+"' "+
                        (style.length()>0?"style='"+style+"' ":"")+mouse+">"+
                        room.getCapacity()+"</td>";
                idx ++;
            }
            while ((idx%step)!=0) {
                ret += "<td colspan='2'>&nbsp;</td>";
                idx++;
            }
            ret += "</tr>";
            ret += "</table>";
            return ret;
        } catch (Exception e) {
        	iForm.setMessage(e.getMessage());
            sLog.error(e.getMessage(),e);
            return "";
        }
    }
    
    public Vector<ClassRoomInfo> getRooms() {
        // if (getClazz().getClassLimit()==0) return null;
        int minRoomSize = -1;
        try {
            minRoomSize = (iForm.getMinRoomSize()==null || iForm.getMinRoomSize().length()==0 ? -1 : Integer.parseInt(iForm.getMinRoomSize().trim()));
        } catch (Exception e) {}
        int maxRoomSize = -1;
        try {
            maxRoomSize = (iForm.getMaxRoomSize()==null || iForm.getMaxRoomSize().length()==0 ? -1 : Integer.parseInt(iForm.getMaxRoomSize().trim()));
        } catch (Exception e) {}
        try {
            if (getSelectedAssignment()==null && !isClassAssigned()) return null;
            if (iRooms==null) {
                iRooms = findRooms(getSelectedAssignment()!=null?getSelectedAssignment().getTime():getClassAssignment().getTime(),
                        minRoomSize, maxRoomSize, iForm.getRoomFilter(), iForm.getAllowRoomConflict(), iForm.getAllRooms());
            }
            return iRooms;
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            return null;
        }
    }
    
    public int getRoomSize() {
        ClassAssignment classAssignment = (iChange==null?null:iChange.getCurrent(iClass));
        if (classAssignment!=null) return classAssignment.getRoomSize();
        if (isClassAssigned()) return getClassAssignment().getRoomSize();
        return 0;
    }
    
    public ClassProposedChange getChange() {
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
    
    public boolean isKeepConflictingAssignments() {
    	return !iUnassignConflictingAssignments;
    }
}
