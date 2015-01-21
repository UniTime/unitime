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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.cpsolver.coursett.preference.SumPreferenceCombination;
import org.hibernate.LazyInitializationException;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ClassInfoForm;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
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
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
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
    private Collection<ClassAssignment> iDates = null;
    private Collection<ClassAssignment> iTimes = null;
    private Vector<ClassRoomInfo> iRooms = null;
    private boolean iShowStudentConflicts = ApplicationProperty.ClassAssignmentShowStudentConflicts.isTrue();
    private boolean iUnassignConflictingAssignments = false;
    private transient SessionContext iContext = null;
    
    public void clear(String userId) {
        iClass = null; iChange = null; iRooms = null; iDates = null; iTimes = null; iUnassignConflictingAssignments = false;
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
    public ClassDateInfo getAssignedDate() {
    	try {
    		ClassAssignmentInfo info = getSelectedAssignment();
    		if (info != null && info.hasDate()) return info.getDate();
    	} catch (Exception e){}
    	DatePattern dp = getClazz().getClazz().effectiveDatePattern();
    	if (dp == null) return null;
    	if (dp.getType() != DatePattern.sTypeAlternate) {
    		return new ClassDateInfo(
    				dp.getUniqueId(),
    				getClazz().getClassId(),
    				dp.getName(),
    				dp.getPatternBitSet(),
    				PreferenceLevel.sIntLevelNeutral);
    	} 
    	return null;
    }
    
    public void update() throws Exception {
        if (iChange==null) return;
        Vector<ClassAssignment> assignments = new Vector(iChange.getAssignments());
        Hashtable<Long,ClassAssignment> table = iChange.getAssignmentTable();
        iUnassignConflictingAssignments = !iForm.getKeepConflictingAssignments();
        iChange.getAssignments().clear();
        for (ClassAssignment assignment : assignments) {
            iChange.getAssignments().add(new ClassAssignmentInfo(assignment.getClazz(),assignment.getTime(),assignment.getDate(),assignment.getRooms(),table));
        }
        if (assignments.isEmpty()) {
        	for (Iterator<ClassAssignment> i = iChange.getConflicts().iterator(); i.hasNext(); ) {
        		ClassAssignment assignment = i.next();
        		if (!assignment.getClassId().equals(getClazz().getClassId())) i.remove();
        	}
        } else {
        	iChange.getConflicts().clear();
        }
        for (ClassAssignment assignment : iChange.getAssignments()) {
        	// Skip incomplete assignments (that have no time assigned yet)
        	if (!assignment.hasTime()) continue;
        	
        	// Check for room conflicts
        	if (iUnassignConflictingAssignments){
	            if (assignment.getRooms()!=null) for (ClassRoomInfo room : assignment.getRooms()) {
	            	if (!room.isIgnoreRoomChecks()){
		            	for (Assignment a : room.getLocation().getCommitedAssignments()) {
		            		if (assignment.getTime().overlaps(new ClassTimeInfo(a))) {
		            			if (iChange.getCurrent(a.getClassId())==null && iChange.getConflict(a.getClassId())==null)
		            				iChange.getConflicts().add(new ClassAssignment(a));
		            		}
		            	}
	            	}
	            }
	            
	            // Check for instructor conflicts
	            if (assignment.getInstructors()!=null) for (ClassInstructorInfo instructor : assignment.getInstructors()) {
	            	if (!instructor.isLead()) continue;
	            	// check all departmental instructors with the same external id
	            	for (DepartmentalInstructor di: DepartmentalInstructor.getAllForInstructor(instructor.getInstructor().getInstructor())) {
		            	for (ClassInstructor ci : di.getClasses()) {
		            		if (ci.equals(instructor.getInstructor())) continue;
		            		Assignment a = ci.getClassInstructing().getCommittedAssignment();
		            		if (a == null) continue;
		            		if (assignment.getTime() != null && assignment.getTime().overlaps(new ClassTimeInfo(a))) {
		            			if (iChange.getCurrent(a.getClassId())==null && iChange.getConflict(a.getClassId())==null)
		            				iChange.getConflicts().add(new ClassAssignment(a));
		            		}
		            	}
	            	}
	            	/*
	            	// Potential speed-up #1) only check the current department instructors
	            	for (ClassInstructor ci : instructor.getInstructor().getInstructor().getClasses()) {
	            		if (ci.equals(instructor.getInstructor())) continue;
	            		Assignment a = ci.getClassInstructing().getCommittedAssignment();
	            		if (a == null) continue;
	            		if (assignment.getTime().overlaps(new ClassTimeInfo(a))) {
	            			if (iChange.getCurrent(a.getClassId())==null && iChange.getConflict(a.getClassId())==null)
	            				iChange.getConflicts().add(new ClassAssignment(a));
	            		}
	            	}
	            	*/
	            	/*
	            	// Potential speed-up #2) use instructor assignments from the solution
	            	for (Assignment a : instructor.getInstructor().getInstructor().getCommitedAssignments()) {
	            		if (assignment.getTime().overlaps(new ClassTimeInfo(a))) {
	            			if (iChange.getCurrent(a.getClassId())==null && iChange.getConflict(a.getClassId())==null)
	            				iChange.getConflicts().add(new ClassAssignment(a));
	            		}
	            	}
	            	*/
	            }
        	}
            // Check the course structure for conflicts
            Class_ clazz = assignment.getClazz(Class_DAO.getInstance().getSession());
            // a) all parents
            Class_ parent = clazz.getParentClass();
            while (parent!=null) {
            	if (iChange.getCurrent(parent.getUniqueId())==null && iChange.getConflict(parent.getUniqueId())==null) {
            		Assignment a = parent.getCommittedAssignment();
            		if (a!=null && assignment.getTime().overlaps(new ClassTimeInfo(a))) {
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
            		if (a!=null && assignment.getTime().overlaps(new ClassTimeInfo(a))) {
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
                		if (a!=null && assignment.getTime().overlaps(new ClassTimeInfo(a))) {
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
    
    public String assign(SessionContext context) {
        if (iChange==null) return "Nothing to assign.";
        if (ApplicationProperty.ClassAssignmentAllowUnassignments.isFalse() && !iChange.getConflicts().isEmpty())
        	return "It is not allowed to keep a class unassigned.";
        sLog.info("About to be assigned: "+iChange);
        org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
        String message = null;
        Map<Long, List<Long>> touchedOfferingIds = new Hashtable<Long, List<Long>>();
        for (ClassAssignment assignment : iChange.getConflicts()) {
        	try {
                Class_ clazz = assignment.getClazz(hibSession);
        		String m = clazz.unassignCommited(context.getUser(), hibSession);
                if (m!=null) message = (message==null?"":message+"\n")+m;
                Long offeringId = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId();
                List<Long> classIds = touchedOfferingIds.get(offeringId);
                if (classIds == null) {
                	classIds = new ArrayList<Long>();
                	touchedOfferingIds.put(offeringId, classIds);
                }
                classIds.add(clazz.getUniqueId());
            } catch (Exception e) {
                message = (message==null?"":message+"\n")+"Unassignment of "+assignment.getClassName()+" failed, reason: "+e.getMessage();
            }
        }
        for (ClassAssignment assignment : iChange.getAssignments()) {
            try {
                Class_ clazz = assignment.getClazz(hibSession);
                String m = clazz.assignCommited(getAssignmentInfo(assignment), context.getUser(), hibSession);
                if (m!=null) message = (message==null?"":message+"\n")+m;
                Long offeringId = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId();
                List<Long> classIds = touchedOfferingIds.get(offeringId);
                if (classIds == null) {
                	classIds = new ArrayList<Long>();
                	touchedOfferingIds.put(offeringId, classIds);
                }
                classIds.add(clazz.getUniqueId());
            } catch (Exception e) {
                message = (message==null?"":message+"\n")+"Assignment of "+assignment.getClassName()+" to "+assignment.getTime().getName()+" "+assignment.getRoomNames(", ")+" failed, reason: "+e.getMessage();
            }
        }
        
        Long sessionId = context.getUser().getCurrentAcademicSessionId();
        Session session = SessionDAO.getInstance().get(sessionId, hibSession);
        if (!session.getStatusType().isTestSession()) {
            if (session.getStatusType().canOnlineSectionStudents()) {
            	List<Long> unlockedOfferings = new ArrayList<Long>();
            	for (Long offeringId: touchedOfferingIds.keySet())
            		if (!session.isOfferingLocked(offeringId))
            			unlockedOfferings.add(offeringId);
            	if (!unlockedOfferings.isEmpty())
            		StudentSectioningQueue.offeringChanged(hibSession, context.getUser(), sessionId, unlockedOfferings);
            } else if (session.getStatusType().canSectionAssistStudents()) {
            	for (Map.Entry<Long, List<Long>> entry: touchedOfferingIds.entrySet()) {
            		if (!session.isOfferingLocked(entry.getKey()))
            			StudentSectioningQueue.classAssignmentChanged(hibSession, context.getUser(), sessionId, entry.getValue());        		
            	}
            }
        }
        hibSession.flush();
        
        return message;
    }
    
    public boolean getCanAssign() {
        if (iChange==null) return false;
        for (ClassAssignment assignment : iChange.getAssignments()) {
            if (!assignment.isValid()) return false;
            if (!getSessionContext().hasPermission(assignment.getClazz(), Right.ClassAssignment)) return false; 
        }
        for (ClassAssignment assignment : iChange.getConflicts()) {
        	if (!getSessionContext().hasPermission(assignment.getClazz(), Right.ClassAssignment)) return false;
        }
        if (ApplicationProperty.ClassAssignmentAllowUnassignments.isFalse() && !iChange.getConflicts().isEmpty()) return false;
        return true;
    }
    
    public String getAssignConfirm() {
    	return "The selected assignment will be done directly in the database. Are you sure?";
    }
    
    public void setForm(ClassInfoForm form) {
        iForm = form;
    }
    
    public void setClazz(Class_ clazz) {
        iDates = null; iTimes = null; iRooms = null;
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
            iForm.setMaxRoomSize(null);
        }
        iForm.setRoomFilter(null);
    }
    
    public ClassAssignmentInfo getAssignmentInfo(ClassAssignment assignment) throws Exception {
        if (assignment instanceof ClassAssignmentInfo) return (ClassAssignmentInfo)assignment;
        if (iChange!=null)
            return new ClassAssignmentInfo(assignment.getClazz(), assignment.getTime(), assignment.getDate(), assignment.getRooms(), iChange.getAssignmentTable());
        else
            return new ClassAssignmentInfo(assignment.getClazz(), assignment.getTime(), assignment.getDate(), assignment.getRooms());
    }
    
    public ClassAssignmentInfo getSelectedAssignment() throws Exception {
        if (iChange==null) return null;
        for (ClassAssignment assignment : iChange.getAssignments())
            if (assignment.getClassId().equals(iClass.getClassId())) return getAssignmentInfo(assignment);
        return null; 
    }
    
    public void setDate(String dateId) throws Exception {
        iRooms = null; iTimes = null;
        if (iChange==null) iChange = new ClassProposedChange();
        ClassTimeInfo time = (getSelectedAssignment() == null ? null : getSelectedAssignment().getTime());
        Collection<ClassRoomInfo> rooms = (getSelectedAssignment() == null ? null : getSelectedAssignment().getRooms());
        for (ClassAssignment date : getDates()) {
            if (dateId.equals(date.getDateId())) {
                iChange.addChange(
                		new ClassAssignmentInfo(getClazz().getClazz(), (time == null ? null : new ClassTimeInfo(time, date.getDate())), date.getDate(), rooms, iChange.getAssignmentTable()), 
                		getClassOldAssignment());
            }
        }
        if (iChange.isEmpty()) iChange = null; 
        update();
    }
    
    public void setTime(String timeId) throws Exception {
        iRooms = null;
        if (iChange==null) iChange = new ClassProposedChange();
		for (ClassAssignment time : getAllTimes()) {
            if (timeId.equals(time.getTimeId())) {
                iChange.addChange(
                		new ClassAssignmentInfo(getClazz().getClazz(), time.getTime(), time.getDate(), null, iChange.getAssignmentTable()), 
                		getClassOldAssignment());
            }
		}       
		if ("-1".equals(timeId)) {
			iChange.addChange(
					new ClassAssignmentInfo(getClazz().getClazz(), null, null, null, iChange.getAssignmentTable()), 
            		getClassOldAssignment());
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
            for (ClassAssignment time : getAllTimes()) {
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
        		new ClassAssignmentInfo(getClazz().getClazz(), assignment.getTime(), assignment.getDate(), assignedRooms, iChange.getAssignmentTable()),
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
    
    public String getDatesTable() {
    	try {
    		String ret = "";
            ret += "<script language='javascript'>";
            ret += "function dateOver(source, id) { ";
            ret += "    document.getElementById('d'+id).style.backgroundColor='rgb(223,231,242)';";
            if (iShowStudentConflicts)
            	ret += "    document.getElementById('dc'+id).style.backgroundColor='rgb(223,231,242)';";
            ret += "    source.style.cursor='hand';source.style.cursor='pointer';";
            ret += "}";
            ret += "function dateOut(id) { ";
            ret += "    var bg = 'transparent';";
            ClassAssignment classAssignment = (iChange==null?null:iChange.getCurrent(iClass));
            if (classAssignment!=null && classAssignment.hasDate())
            	ret += "    if (id=='"+classAssignment.getDateId()+"') bg='rgb(168,187,225)';";
            ret += "    document.getElementById('d'+id).style.backgroundColor=bg;";
            if (iShowStudentConflicts)
            	ret += "    document.getElementById('dc'+id).style.backgroundColor=bg;";
            ret += "}";
            ret += "function dateClick(source, id) { ";
            ret += "    displayLoading();";
            ret += "    document.location='classInfo.do?op=Select&date='+id+'&noCacheTS=" + new Date().getTime()+"';";
            ret += "}";
            ret += "</script>";
            ret += "<table border='0' cellspacing='0' cellpadding='3'>";
            int idx = 0;
            int step = 5;
            for (ClassAssignment date : getDates()) {
                boolean initial = (getClassOldAssignment()!=null && getClassOldAssignment().getDateId()!=null && getClassOldAssignment().getDateId().equals(date.getDateId()));
                if ((idx%step)==0) {
                    if (idx>0) ret +="</tr>";
                    ret += "<tr>";
                }
                String style = "";
                if (classAssignment!=null && date.getDateId().equals(classAssignment.getDateId()))
                    style += "background-color:rgb(168,187,225);";
                if (initial)
                    style += "text-decoration:underline;";
                String mouse = 
                    "onMouseOver=\"dateOver(this,'"+date.getDateId()+"');\" "+
                    "onMouseOut=\"dateOut('"+date.getDateId()+"');\" "+
                    "onClick=\"dateClick(this,'"+date.getDateId()+"');\"";
                if (iShowStudentConflicts) {
                	ret += "<td nowrap id='d"+date.getDateId()+"' " +
                           (style.length()>0?"style='"+style+"' ":"")+mouse+">"+
                           date.getDateNameHtml()+"</td>";
                    if ((idx%step)<step-1)
                        style += "border-right: #646464 1px dashed;";
                    ret += "<td id='dc"+date.getDateId()+"' "+
                            (style.length()>0?"style='"+style+"' ":"")+mouse+">"+
                            (date instanceof ClassAssignmentInfo ? String.valueOf(((ClassAssignmentInfo)date).getNrStudentCounflicts()) : "") +"</td>";
                } else {
                    if ((idx%step)<step-1)
                        style += "border-right: #646464 1px dashed;";
                    ret += "<td nowrap id='d"+date.getDateId()+"' " +
                            (style.length()>0?"style='"+style+"' ":"")+mouse+">"+
                            date.getDateNameHtml()+"</td>";
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
            if (classAssignment != null && classAssignment.hasTime())
            	ret += "    if (id=='"+classAssignment.getTimeId()+"') bg='rgb(168,187,225)';";
            ret += "    document.getElementById('t'+id).style.backgroundColor=bg;";
            if (iShowStudentConflicts)
            	ret += "    document.getElementById('c'+id).style.backgroundColor=bg;";
            ret += "}";
            ret += "function timeClick(source, id) { ";
            ret += "    displayLoading();";
            ret += "    document.location='classInfo.do?op=Select&time='+id+'&noCacheTS=" + new Date().getTime()+"';";
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
                if (classAssignment != null && time.getTimeId().equals(classAssignment.getTimeId()))
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
            if (classAssignment != null) {
                if ((idx%step)==0) {
                    if (idx>0) ret +="</tr>";
                    ret += "<tr>";
                }
                String style = "font-style:italic; color:#c81e14;";
                String mouse = 
                    "onMouseOver=\"timeOver(this,'-1');\" "+
                    "onMouseOut=\"timeOut('-1');\" "+
                    "onClick=\"timeClick(this,'-1');\"";
                if (iShowStudentConflicts) {
                	ret += "<td nowrap id='t-1' " +
                           (style.length()>0?"style='"+style+"' ":"")+mouse+">not-assigned</td>";
                    if ((idx%step)<step-1)
                        style += "border-right: #646464 1px dashed;";
                    ret += "<td id='c-1' "+
                            (style.length()>0?"style='"+style+"' ":"")+mouse+"></td>";
                } else {
                    if ((idx%step)<step-1)
                        style += "border-right: #646464 1px dashed;";
                    ret += "<td nowrap id='t-1' " +
                            (style.length()>0?"style='"+style+"' ":"")+mouse+">not-assigned</td>";
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
        ret += "<td><i>Date</i></td>";
        ret += "<td><i>Time</i></td>";
        ret += "<td><i>Room</i></td>";
        ret += "</tr>";
        boolean empty = true;
        for (StudentConflict conf:getStudentConflicts()) {
        	ret += conf.toHtml2();
        	empty = false;
        }
        if (empty) ret += "<tr><td colspan='5'><i>There are no student conflicts.</i></td></tr>";
        ret += "</table>";
        return ret;
    }
    
    public boolean getShowDates() {
    	return getDates().size() > 1;
    }
    
    public Collection<ClassAssignment> getDates() {
    	if (iDates == null) {
    		iDates = new Vector<ClassAssignment>();
            Class_ clazz = getClazz().getClazz();
            DatePattern datePattern = clazz.effectiveDatePattern();
            if (datePattern==null) {
            	iForm.setMessage("Class "+getClazz().getClassName()+" has no date pattern selected.");
            	return iTimes;
            }
            ClassTimeInfo time = (getClassAssignment() == null ? null : getClassAssignment().getTime());
            if (datePattern.getType() == DatePattern.sTypePatternSet) {
            	Set<DatePatternPref> datePatternPrefs = (Set<DatePatternPref>)clazz.effectivePreferences(DatePatternPref.class);
            	boolean hasReq = false;
            	for (DatePatternPref p: datePatternPrefs) {
            		if (PreferenceLevel.sRequired.equals(p.getPrefLevel().getPrefProlog())) { hasReq = true; break; }
            	}
            	for (DatePattern child: datePattern.findChildren()) {
            		String pr = PreferenceLevel.sNeutral;
            		for (DatePatternPref p: datePatternPrefs) {
            			if (p.getDatePattern().equals(child)) pr = p.getPrefLevel().getPrefProlog();
            		}
            		int prVal = 0;
            		if (!PreferenceLevel.sNeutral.equals(pr) && !PreferenceLevel.sRequired.equals(pr)) {
            			prVal = PreferenceLevel.prolog2int(pr);
            		}
        			if (hasReq && !PreferenceLevel.sRequired.equals(pr)) prVal += 100;
        			if (PreferenceLevel.sProhibited.equals(pr)) prVal += 100;
        			if (iShowStudentConflicts && time != null) {
            			iDates.add(new ClassAssignmentInfo(
                    			clazz,
                    			time,
                    			new ClassDateInfo(
                            			child.getUniqueId(),
                            			clazz.getUniqueId(),
                            			child.getName(),
                            			child.getPatternBitSet(),
                            			prVal),
                            	null));
        			} else {
            			iDates.add(new ClassAssignment(
                    			clazz,
                    			null,
                    			new ClassDateInfo(
                            			child.getUniqueId(),
                            			clazz.getUniqueId(),
                            			child.getName(),
                            			child.getPatternBitSet(),
                            			prVal),
                            	null));
        			}
            	}
            } else {
            	if (iShowStudentConflicts && time != null) {
                	iDates.add(new ClassAssignmentInfo(
                			clazz,
                			time,
                			new ClassDateInfo(
                        			datePattern.getUniqueId(),
                        			clazz.getUniqueId(),
                        			datePattern.getName(),
                        			datePattern.getPatternBitSet(),
                        			PreferenceLevel.sIntLevelNeutral),
                        	null));
            	} else {
                	iDates.add(new ClassAssignment(
                			clazz,
                			null,
                			new ClassDateInfo(
                        			datePattern.getUniqueId(),
                        			clazz.getUniqueId(),
                        			datePattern.getName(),
                        			datePattern.getPatternBitSet(),
                        			PreferenceLevel.sIntLevelNeutral),
                        	null));            		
            	}
            }
    	}
    	return iDates;
    }

    public Collection<ClassAssignment> getTimes() {
        if (iTimes==null) {
            Class_ clazz = getClazz().getClazz();
            Set timePrefs = clazz.effectivePreferences(TimePref.class);
            if (timePrefs.isEmpty()) {
            	iForm.setMessage("Class "+getClazz().getClassName()+" has no time pattern selected.");
            	return iTimes;
            }
            ClassDateInfo date = getAssignedDate();
            if (date == null) {
            	Collection<ClassAssignment> dates = getDates();
                if (dates != null && !dates.isEmpty())
                	date = dates.iterator().next().getDate();
            }
            if (date == null) {
            	iForm.setMessage("Class "+getClazz().getClassName()+" has no date pattern selected.");
            	return iTimes;
            }
            iTimes = getTimes(date);
            if (iTimes.isEmpty())
            	iForm.setMessage("Class "+getClazz().getClassName()+" has no available time.");
        }
        return iTimes;
    }
    
    public Collection<ClassAssignment> getAllTimes() {
    	Vector<ClassAssignment> times = new Vector<ClassAssignment>();
        Class_ clazz = getClazz().getClazz();
        DatePattern datePattern = clazz.effectiveDatePattern();
        if (datePattern == null) return times;
        if (datePattern.getType() == DatePattern.sTypePatternSet) {
        	Set<DatePatternPref> datePatternPrefs = (Set<DatePatternPref>)clazz.effectivePreferences(DatePatternPref.class);
        	boolean hasReq = false;
        	for (DatePatternPref p: datePatternPrefs) {
        		if (PreferenceLevel.sRequired.equals(p.getPrefLevel().getPrefProlog())) { hasReq = true; break; }
        	}
        	for (DatePattern child: datePattern.findChildren()) {
        		String pr = PreferenceLevel.sNeutral;
        		for (DatePatternPref p: datePatternPrefs) {
        			if (p.getDatePattern().equals(child)) pr = p.getPrefLevel().getPrefProlog();
        		}
        		int prVal = 0;
        		if (!PreferenceLevel.sNeutral.equals(pr) && !PreferenceLevel.sRequired.equals(pr)) {
        			prVal = PreferenceLevel.prolog2int(pr);
        		}
    			if (hasReq && !PreferenceLevel.sRequired.equals(pr)) prVal += 100;
    			if (PreferenceLevel.sProhibited.equals(pr)) prVal += 100;
    			times.addAll(getTimes(
    					new ClassDateInfo(
    							child.getUniqueId(),
    	            			clazz.getUniqueId(),
    	            			child.getName(),
    	            			child.getPatternBitSet(),
    	            			prVal)));
        	}
        } else {
        	times.addAll(getTimes(
					new ClassDateInfo(
                			datePattern.getUniqueId(),
                			clazz.getUniqueId(),
                			datePattern.getName(),
                			datePattern.getPatternBitSet(),
                			PreferenceLevel.sIntLevelNeutral
                			)));
        }
    	return times;
    }
    
    public Collection<ClassAssignment> getTimes(ClassDateInfo date) {
        Class_ clazz = getClazz().getClazz();
    	Vector<ClassAssignment> times = new Vector<ClassAssignment>();
        boolean onlyReq = false;
        Set timePrefs = clazz.effectivePreferences(TimePref.class);
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
        		ClassTimeInfo time = new ClassTimeInfo(pattern.getExactDays(),pattern.getExactStartSlot(),length,PreferenceLevel.sIntLevelNeutral,timePref.getTimePattern(),date,breakTime);
        		if (iShowStudentConflicts)
        			times.add(new ClassAssignmentInfo(clazz, time, date, null, (iChange==null?null:iChange.getAssignmentTable())));
        		else
        			times.add(new ClassAssignment(clazz, time, date, null));
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
                            date,
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
                    	 times.add(new ClassAssignmentInfo(clazz, loc, date, null, (iChange==null?null:iChange.getAssignmentTable())));
                    else
                    	times.add(new ClassAssignment(clazz, loc, date, null));
                }
            }
        }
        return times;
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
        	int roomCapacity = Math.round(minClassLimit<=0?room2limitRatio:room2limitRatio*minClassLimit);
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
                		int startTime = period.getStartSlot();
                		int endTime = (period.getStartSlot()+period.getLength()-1);
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
        		boolean reqGroup = false;
        		for (Iterator i2=groupPrefs.iterator();i2.hasNext();) {
        			RoomGroupPref p = (RoomGroupPref)i2.next();
        			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) reqGroup = true;
        			if (p.getRoomGroup().getRooms().contains(room)) groupPref.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
        		}
        		if (reqGroup) {
        			if (!PreferenceLevel.sRequired.equals(groupPref.getPreferenceProlog()))
        				pref.addPreferenceProlog(PreferenceLevel.sProhibited);
        		} else {
            		pref.addPreferenceProlog(groupPref.getPreferenceProlog());
        		}
        			
                
                // --- room preference ------------
        		String roomPref = null;
        		PreferenceLevel roomPreference = getRoomPreference(clazz.getManagingDept(),room.getUniqueId());
        		if (roomPreference!=null) {
        			roomPref = roomPreference.getPrefProlog();
    			}
    			boolean reqRoom = false;
    			for (Iterator i2=roomPrefs.iterator();i2.hasNext();) {
        			RoomPref p = (RoomPref)i2.next();
        			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) reqRoom = true;
        			if (room.equals(p.getRoom())) roomPref = p.getPrefLevel().getPrefProlog();
        		}
    			if (reqRoom) {
    				if (!PreferenceLevel.sRequired.equals(roomPref))
    					pref.addPreferenceProlog(PreferenceLevel.sProhibited);
    			} else if (roomPref != null) {
    				pref.addPreferenceProlog(roomPref);
    			}
    			
                // --- building preference ------------
        		Building bldg = (room instanceof Room ? ((Room)room).getBuilding() : null);
        		boolean reqBldg = false;
        		String bldgPref = null;
        		for (Iterator i2=bldgPrefs.iterator();i2.hasNext();) {
        			BuildingPref p = (BuildingPref)i2.next();
        			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) reqBldg = true;
        			if (bldg!=null && bldg.equals(p.getBuilding())) bldgPref = p.getPrefLevel().getPrefProlog();
        		}
        		if (reqBldg) {
        			if (!PreferenceLevel.sRequired.equals(bldgPref))
        				pref.addPreferenceProlog(PreferenceLevel.sProhibited);
        		} else if (bldgPref != null) {
        			pref.addPreferenceProlog(bldgPref);
        		}
                
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
                if (!acceptableFeatures)
                  	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                
                
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
 			
 			boolean changePast = ApplicationProperty.ClassAssignmentChangePastMeetings.isTrue();
 			boolean ignorePast = ApplicationProperty.ClassAssignmentIgnorePastMeetings.isTrue();

 			Vector <Date>datesToCheck = null;
 			if (ignorePast || !changePast) {
 				datesToCheck = new Vector<Date>();
 	 			for(Date aDate : period.getDates()){
 	 				if (aDate.compareTo(today) > 0)
 	 					datesToCheck.add(aDate);
 	 			}
 			} else {
 				datesToCheck = period.getDates();
 			}
            Hashtable<Long,Set<Long>> room2classIds = Location.findClassLocationTable(clazz.getSessionId(), permIds, period.getStartSlot(), period.getLength(),
            		changePast ? period.getDates() : datesToCheck);
            
            Hashtable<Long,Set<Event>> room2events = null;
            if (RoomAvailability.getInstance()!=null && RoomAvailability.getInstance() instanceof DefaultRoomAvailabilityService) {
            	room2events = Location.findEventTable(clazz.getSessionId(), permIds, period.getStartSlot(), period.getLength(), datesToCheck);
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
                		if (!changePast || ignorePast) {
                			timesToCheck = new Vector();
                			for (TimeBlock time: times) {
                				if (!time.getEndTime().before(today))
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
            ret += "    if (sNrRooms=="+getClazz().getNumberOfRooms()+") {displayLoading(); document.location='classInfo.do?op=Select&room='+sRooms+'&noCacheTS=" + new Date().getTime()+"';}";
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
    	ClassTimeInfo time = null;
    	try {
    		time = (getSelectedAssignment()!=null?getSelectedAssignment().getTime():getClassAssignment().getTime());
    	} catch (Exception e) {}
    	if (time == null) return null;
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
                iRooms = findRooms(time, minRoomSize, maxRoomSize, iForm.getRoomFilter(), iForm.getAllowRoomConflict(), iForm.getAllRooms());
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
        if (iChange==null || iChange.isEmpty()) return null;
        return iChange; 
    }
    
    public boolean isHasChange() {
        return iChange != null && !iChange.isEmpty();
    }
    
    public String getChangeHtmlTable() {
    	if (iChange==null || iChange.isEmpty()) return null;
        return iChange.getHtmlTable(getSessionContext());
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
    
    public void setSessionContext(SessionContext context) { iContext = context; }
    public SessionContext getSessionContext() { return iContext; }
}
