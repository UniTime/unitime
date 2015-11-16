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
package org.unitime.timetable.solver.exam.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.cpsolver.coursett.preference.SumPreferenceCombination;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.ExamInfoForm;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller
 */
public class ExamInfoModel implements Serializable {
	private static final long serialVersionUID = 6594424808143469141L;
	private static Log sLog = LogFactory.getLog(ExamInfoModel.class);
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
    
    public void clear(UserContext user) {
        iExam = null; iChange = null; iRooms = null; iPeriods = null;
        iManagerExternalId = (user == null ? null : user.getExternalUserId());
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
        if (getSolver() != null && getSolver().getExamTypeId().equals(getExam().getExamTypeId())) {
            iChange = getSolver().update(iChange);
        } else {
            Vector<ExamAssignment> assignments = new Vector(iChange.getAssignments());
            Hashtable<Long,ExamAssignment> table = iChange.getAssignmentTable();
            iChange.getAssignments().clear();
            for (ExamAssignment assignment : assignments) {
            	iChange.getAssignments().add(new ExamAssignmentInfo(assignment.getExam(),assignment.getPeriod(),assignment.getRooms(),table));
            }
            iChange.getConflicts().clear();
            for (ExamAssignment assignment : new Vector<ExamAssignment>(iChange.getAssignments())) {
                if (assignment.getRooms() != null) {
                	for (ExamRoomInfo room : assignment.getRooms()) {
                		
                        Set<Long> canShareRoom = (assignment.getRooms().size() == 1 ? getCanShareRoomExams(assignment.getExamId()) : new HashSet<Long>());
                		int size = assignment.getNrStudents();
                		
                		if (!canShareRoom.isEmpty()) {
                    		for (ExamAssignment other: iChange.getAssignments()) {
                    			if (!other.equals(assignment) && other.getPeriodId().equals(assignment.getPeriodId()) && assignment.getRooms().equals(other.getRooms()))
                    				size += other.getNrStudents();
                    		}
                    		if (size > room.getCapacity(assignment)) {
                    			if (!getExam().equals(assignment)) {
                    				iChange.getAssignments().remove(assignment);
                    				iChange.getConflicts().add(new ExamAssignment(assignment.getExam()));
                    			}
                    			continue;
                    		}
                		}
                		
                        for (Exam x: room.getLocation().getExams(assignment.getPeriodId())) {
                            if (iChange.getCurrent(x.getUniqueId()) == null && iChange.getConflict(x.getUniqueId()) == null) {
                            	if (canShareRoom.contains(x.getUniqueId())) {
                            		if (size + x.getSize() <= room.getCapacity(assignment))
                            			size += x.getSize();
                            		else
                            			iChange.getConflicts().add(new ExamAssignment(x));
                            	} else {
                            		iChange.getConflicts().add(new ExamAssignment(x));
                            	}
                            }
                        }
                    }
                }
            }
        }
    }
    
    public String assign() {
        if (iChange==null) return "Nothing to assign.";
        sLog.info("About to be assigned: "+iChange);
        if (getSolver() != null && getSolver().getExamTypeId().equals(getExam().getExamTypeId())) {
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
    	if (getSolver() != null && getSolver().getExamTypeId().equals(getExam().getExamTypeId())) {
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
        if (getSolver() != null && getSolver().getExamTypeId().equals(exam.getExamType().getUniqueId())) {
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
            iChange.setSelected(exam.getUniqueId());
            /*
            for (Iterator<ExamAssignmentInfo> i=iChange.getAssignments().iterator();i.hasNext();) {
                ExamAssignmentInfo a = i.next();
                if (!a.isValid()) i.remove();
            }
            */
        }
        if (iExam.getMaxRooms()>0) {
    		iForm.setMinRoomSize(String.valueOf(iExam.getNrStudents()));
    		iForm.setMaxRoomSize(null);
        }
        iForm.setRoomFilter(null);
        iForm.setDepth(2);
        iForm.setTimeout(5000);
    }
    
    public ExamAssignmentInfo getAssignmentInfo(ExamAssignment assignment) throws Exception {
        if (assignment instanceof ExamAssignmentInfo) return (ExamAssignmentInfo)assignment;
        if (getSolver() != null && getSolver().getExamTypeId().equals(getExam().getExamTypeId())) {
            return getSolver().getAssignment(assignment.getExamId(), assignment.getPeriodId(), assignment.getRoomIds());
        } else {
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
        if (getSolver() != null && getSolver().getExamTypeId().equals(getExam().getExamTypeId())) {
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
        try {
            WebTable table = new WebTable(8, "Available Periods for "+getExam().getExamName(), "examInfo.do?op=Reorder&pord=%%&noCacheTS=" + new Date().getTime(), 
                    new String[] {"Available<br>Period","Violated<br>Distributions", "Student<br>Direct", "Student<br>&gt; 2 A Day","Student<br>Back-To-Back", "Instructor<br>Direct", "Instructor<br>&gt; 2 A Day", "Instructor<br>Back-To-Back"},
                    new String[] {"left", "left", "right", "right", "right", "right", "right", "right", "right"},
                    new boolean[] { true, true, true, true, true, true, true, true});
            ExamAssignmentInfo current = getExamAssignment();
            for (ExamAssignmentInfo period : getPeriods()) {
                boolean initial = (getExamOldAssignment()!=null && getExamOldAssignment().getPeriodId()!=null && getExamOldAssignment().getPeriodId().equals(period.getPeriodId()));
                WebTable.WebTableLine line = table.addLine(
                   "onClick=\"displayLoading();document.location='examInfo.do?op=Select&period="+period.getPeriodId()+"&noCacheTS=" + new Date().getTime()+"';\"",
                   new String[] {
                        (initial?"<u>":"")+period.getPeriodAbbreviationWithPref()+(initial?"</u>":""),
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
        } catch (Exception e) {
            Debug.error(e);
            return null;
        }
    }
    
    public Collection<ExamAssignmentInfo> getPeriods() {
        if (iPeriods==null) {
        	if (getSolver() != null && getSolver().getExamTypeId().equals(getExam().getExamTypeId())) {
                iPeriods = getSolver().getPeriods(getExam().getExamId(), iChange);
            } else {
                try {
                    Hashtable<Long, Set<Exam>> studentExams = getExam().getExam().getStudentExams();
                    iPeriods = new Vector<ExamAssignmentInfo>();
                    for (Iterator i=ExamPeriod.findAll(getExam().getExam().getSession().getUniqueId(), getExam().getExamTypeId()).iterator();i.hasNext();) {
                        ExamPeriod period = (ExamPeriod)i.next();
                        try {
                            iPeriods.add(new ExamAssignmentInfo(getExam().getExam(), period, null, studentExams, (iChange==null?null:iChange.getAssignmentTable())));
                        } catch (Exception e) {
                            if (!"Given period is prohibited.".equals(e.getMessage()) && !"Given period is two short.".equals(e.getMessage()))
                                Debug.error(e);
                        }
                    }
                } catch (Exception e) {
                    Debug.error(e);
                }
            }
        }
        return iPeriods;
    }
    
    public TreeSet findAllExamLocations(Long sessionId, Long examTypeId) {
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
        return new TreeSet(
                (new LocationDAO()).getSession()
                .createQuery("select r from Location r inner join r.examTypes x "+a+" where r.session.uniqueId = :sessionId and x.uniqueId = :examTypeId "+b)
                .setLong("sessionId", sessionId).setLong("examTypeId", examTypeId).setCacheable(true).list());
    }
    
    protected void filterRooms() {
    	rooms: for (Iterator<ExamRoomInfo> i1 = iRooms.iterator(); i1.hasNext();) {
        	ExamRoomInfo r = i1.next();
    		if (iForm.getRoomTypes()!=null && iForm.getRoomTypes().length>0) {
    			boolean ok = false;
    			for (int i=0;i<iForm.getRoomTypes().length;i++)
    				if (r.getLocation().getRoomType().getUniqueId().equals(iForm.getRoomTypes()[i])) {
    					ok = true; break;
    				}
    			if (!ok) {
    				i1.remove(); continue rooms;
    			}
    		}
    		if (iForm.getRoomFeatures()!=null && iForm.getRoomFeatures().length>0) {
        		for (int i=0;i<iForm.getRoomFeatures().length;i++)
        			if (!r.getLocation().hasFeature(iForm.getRoomFeatures()[i])) {
        				i1.remove(); continue rooms;
        			}
    		}
    		if (iForm.getRoomGroups()!=null && iForm.getRoomGroups().length>0) {
    			for (int i=0;i<iForm.getRoomGroups().length;i++)
    				if (r.getLocation().hasGroup(iForm.getRoomGroups()[i])) continue rooms;
    			i1.remove();
    		}
    	}
    }
    
    protected Set<Long> getCanShareRoomExams(Long examId) {
    	return new HashSet<Long>(ExamDAO.getInstance().getSession().createQuery(
    			"select o.prefGroup.uniqueId from DistributionPref p inner join p.distributionObjects x inner join p.distributionObjects o " +
    			"where p.distributionType.reference = :shareType and x.prefGroup.uniqueId = :examId and x.prefGroup != o.prefGroup")
    			.setString("shareType", "EX_SHARE_ROOM")
    			.setLong("examId", examId)
    			.setCacheable(true).list());
    }
    
    protected Vector<ExamRoomInfo> findRooms(ExamPeriod period, int minRoomSize, int maxRoomSize, String filter, boolean allowConflicts) {
        Vector<ExamRoomInfo> rooms = new Vector<ExamRoomInfo>();
        boolean reqRoom = false;
        boolean reqBldg = false;
        boolean reqGroup = false;
        
        Exam exam = getExam().getExam(new ExamDAO().getSession());
        Set<Long> canShareRoom = getCanShareRoomExams(getExam().getExamId());
        
        Set groupPrefs = exam.getPreferences(RoomGroupPref.class);
        Set roomPrefs = exam.getPreferences(RoomPref.class);
        Set bldgPrefs = exam.getPreferences(BuildingPref.class);
        Set featurePrefs = exam.getPreferences(RoomFeaturePref.class);
        
        TreeSet locations = findAllExamLocations(period.getSession().getUniqueId(), period.getExamType().getUniqueId());
        Hashtable<Long, Set<Long>> locationTable = Location.findExamLocationTable(period.getUniqueId());
        
        if (getExamAssignment()!=null) {
            if (getExamAssignment().getPeriod().equals(period) && getExamAssignment().getRooms()!=null)
                for (ExamRoomInfo room : getExamAssignment().getRooms()) {
                	Set<Long> exams = locationTable.get(room.getLocationId());
                	if (exams != null) exams.remove(getExam().getExamId());
                }
        }
        if (iChange!=null) {
            for (ExamAssignment conflict : iChange.getConflicts()) {
                if (conflict.getPeriod().equals(period) && conflict.getRooms()!=null)
                    for (ExamRoomInfo room : conflict.getRooms()) {
                    	Set<Long> exams = locationTable.get(room.getLocationId());
                    	if (exams != null) exams.remove(conflict.getExamId());
                    }
            }
            for (ExamAssignment current : iChange.getAssignments()) {
                ExamAssignment initial = iChange.getInitial(current);
                if (initial!=null && initial.getPeriod().equals(period) && initial.getRooms()!=null)
                    for (ExamRoomInfo room : initial.getRooms()) {
                    	Set<Long> exams = locationTable.get(room.getLocationId());
                    	if (exams != null) exams.remove(initial.getExamId());
                    }
            }
            for (ExamAssignment current : iChange.getAssignments()) {
                if (!iExam.getExamId().equals(current.getExamId()) && current.getPeriod().equals(period) && current.getRooms()!=null)
                    for (ExamRoomInfo room : current.getRooms()) {
                    	Set<Long> exams = locationTable.get(room.getLocationId());
                    	if (exams == null) { exams = new HashSet<Long>(); locationTable.put(room.getLocationId(), exams); }
                    	exams.add(current.getExamId());
                    }
            }
        }
        
        rooms: for (Iterator i1=locations.iterator();i1.hasNext();) {
            Location room = (Location)i1.next();
            
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
            
            Set<Long> exams = locationTable.get(room.getUniqueId());
            boolean roomConflict = false;
            if (exams != null && !exams.isEmpty()) {
            	for (Long other: exams) {
            		if (!canShareRoom.contains(other)) {
            			roomConflict = true;
            			if (!allowConflicts) continue rooms;
                		if (iChange != null && iChange.getCurrent(other) != null) continue rooms;
            		}
            	}
            }
            
            int cap = (getExam().getSeatingType()==Exam.sSeatingTypeExam?room.getExamCapacity():room.getCapacity());
            if (minRoomSize>=0 && cap<minRoomSize) continue;
            if (maxRoomSize>=0 && cap>maxRoomSize) continue;
            
            if (PreferenceLevel.sProhibited.equals(room.getExamPreference(period).getPrefProlog())) continue;
            
            if (!match(room.getLabel(),filter)) continue;
            
            if (RoomAvailability.getInstance()!=null) {
                Collection<TimeBlock> times = RoomAvailability.getInstance().getRoomAvailability(
                        room.getUniqueId(),
                        period.getStartTime(), period.getEndTime(), 
                        period.getExamType().getType() == ExamType.sExamTypeFinal ? RoomAvailabilityInterface.sFinalExamType : RoomAvailabilityInterface.sMidtermExamType);
                if (times!=null) for (TimeBlock time : times) {
                    if (period.overlap(time)) {
                        System.out.println("Room "+room.getLabel()+" is not avaiable due to "+time);
                        continue rooms;
                    }
                }
            }
            
            rooms.add(new ExamRoomInfo(room, (roomConflict ? 1000 : 0) + pref.getPreferenceInt()));
        }
        
        return rooms;
    }
    
    public String getRoomTable() {
        try {
            Vector<ExamRoomInfo> rooms = getRooms();
            ExamAssignment examAssignment = (iChange==null?null:iChange.getCurrent(iExam));
            Collection<ExamRoomInfo> assigned = (examAssignment!=null?examAssignment.getRooms():isExamAssigned()?getExamAssignment().getRooms():null);
            Collection<ExamRoomInfo> original = (getExamOldAssignment()!=null?getExamOldAssignment().getRooms():null);
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
            ret += "var sRooms = '";
            if (assigned!=null && assigned.size()>0) {
                for (ExamRoomInfo room : assigned) {
                    ret+=":"+room.getLocationId()+"@"+room.getCapacity(getExam());
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
            ret += "    if (sCap>="+getExam().getNrStudents()+") {displayLoading(); document.location='examInfo.do?op=Select&room='+sRooms+'&noCacheTS=" + new Date().getTime()+"';}";
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
                if (original!=null && original.contains(room))
                    style += "text-decoration:underline;";
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
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            return "";
        }
    }
    
    public Vector<ExamRoomInfo> getRooms() {
        if (getExam().getMaxRooms()==0) return null;
        int minRoomSize = -1;
        try {
            minRoomSize = (iForm.getMinRoomSize()==null || iForm.getMinRoomSize().length()==0 ? -1 : Integer.parseInt(iForm.getMinRoomSize().trim()));
        } catch (Exception e) {}
        int maxRoomSize = -1;
        try {
            maxRoomSize = (iForm.getMaxRoomSize()==null || iForm.getMaxRoomSize().length()==0 ? -1 : Integer.parseInt(iForm.getMaxRoomSize().trim()));
        } catch (Exception e) {}
        try {
            if (getSelectedAssignment()==null && !isExamAssigned()) return null;
            if (iRooms==null) {
            	if (getSolver() != null && getSolver().getExamTypeId().equals(getExam().getExamTypeId())) {
                    iRooms = getSolver().getRooms(getExam().getExamId(), getSelectedAssignment()!=null?getSelectedAssignment().getPeriodId():getExamAssignment().getPeriodId(),
                            iChange, minRoomSize, maxRoomSize, iForm.getRoomFilter(), iForm.getAllowRoomConflict());
                    filterRooms();
                } else {
                    iRooms = findRooms(getSelectedAssignment()!=null?getSelectedAssignment().getPeriod():getExamAssignment().getPeriod(),
                            minRoomSize, maxRoomSize, iForm.getRoomFilter(), iForm.getAllowRoomConflict());
                }
            }
            return iRooms;
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
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
    	if (getSolver() != null && getSolver().getExamTypeId().equals(getExam().getExamTypeId())) {
            if (iCbs==null) 
                iCbs = getSolver().getCbsInfo(iExam.getExamId());
            return iCbs;
        }
        return null;
    }
    
    public ExamSuggestionsInfo getSuggestions() {
    	if (getSolver() != null && getSolver().getExamTypeId().equals(getExam().getExamTypeId())) {
            if (iSuggestions==null) {
                try {
                    iSuggestions = getSolver().getSuggestions(iExam.getExamId(), iChange, iForm.getFilter(), iForm.getDepth(), iForm.getLimit(), iForm.getTimeout());
                } catch (Exception e) { sLog.error(e.getMessage(),e); }
            }
            return iSuggestions;
        }
        return null;
    }
    
    public boolean getCanComputeSuggestions() {
    	return getSolver() != null && getSolver().getExamTypeId().equals(getExam().getExamTypeId());
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
