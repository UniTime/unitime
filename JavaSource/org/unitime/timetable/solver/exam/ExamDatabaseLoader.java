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
package org.unitime.timetable.solver.exam;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import net.sf.cpsolver.coursett.preference.PreferenceCombination;
import net.sf.cpsolver.coursett.preference.SumPreferenceCombination;
import net.sf.cpsolver.exam.model.Exam;
import net.sf.cpsolver.exam.model.ExamCourseSection;
import net.sf.cpsolver.exam.model.ExamDistributionConstraint;
import net.sf.cpsolver.exam.model.ExamInstructor;
import net.sf.cpsolver.exam.model.ExamModel;
import net.sf.cpsolver.exam.model.ExamPeriod;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamRoom;
import net.sf.cpsolver.exam.model.ExamStudent;
import net.sf.cpsolver.ifs.model.Constraint;
import net.sf.cpsolver.ifs.util.Progress;
import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class ExamDatabaseLoader extends ExamLoader {
    private static Log sLog = LogFactory.getLog(ExamDatabaseLoader.class);
    private Long iSessionId;
    private int iExamType;
    private boolean iLoadSolution;
    private String iInstructorFormat;
    private Progress iProgress = null;
    private Hashtable iPeriods = new Hashtable();
    private Hashtable iRooms = new Hashtable();
    private Hashtable iExams = new Hashtable();
    private Hashtable iInstructors = new Hashtable();
    private Hashtable iStudents = new Hashtable();
    private Set iAllRooms = null;
    private Set iProhibitedPeriods = new HashSet(); 

    public ExamDatabaseLoader(ExamModel model) {
        super(model);
        iProgress = Progress.getInstance(model);
        iSessionId = model.getProperties().getPropertyLong("General.SessionId",(Long)null);
        iExamType = model.getProperties().getPropertyInt("Exam.Type",org.unitime.timetable.model.Exam.sExamTypeFinal);
        iLoadSolution = model.getProperties().getPropertyBoolean("General.LoadSolution", true);
        iInstructorFormat = getModel().getProperties().getProperty("General.InstructorFormat", DepartmentalInstructor.sNameFormatLastFist);
    }
    
    private String getExamLabel(org.unitime.timetable.model.Exam exam) {
        return "<A href='examDetail.do?examId="+exam.getUniqueId()+"'>"+exam.getLabel()+"</A>";
    }

    public void load() throws Exception {
        iProgress.setStatus("Loading input data ...");
        org.hibernate.Session hibSession = new ExamDAO().getSession();
        Transaction tx = null;
        try {
            tx = hibSession.beginTransaction();
            loadPeriods();
            loadRooms();
            loadExams();
            loadStudents();
            loadDistributions();
            getModel().init();
            assignInitial();
            tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            iProgress.fatal("Unable to load examination problem, reason: "+e.getMessage(),e);
            throw e;
        }
    }
    
    public int pref2weight(String pref) {
        if (PreferenceLevel.sStronglyPreferred.equals(pref))
            return -4;
        if (PreferenceLevel.sPreferred.equals(pref))
            return -1;
        if (PreferenceLevel.sDiscouraged.equals(pref))
            return 1;
        if (PreferenceLevel.sStronglyDiscouraged.equals(pref))
            return 4;
        return 0;
    }
    
    protected void loadPeriods() {
        Set periods = org.unitime.timetable.model.ExamPeriod.findAll(iSessionId, iExamType);
        iProgress.setPhase("Loading periods...", periods.size());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MM/dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mmaa");
        for (Iterator i=periods.iterator();i.hasNext();) {
            iProgress.incProgress();
            org.unitime.timetable.model.ExamPeriod period = (org.unitime.timetable.model.ExamPeriod)i.next();
            String pref = period.getPrefLevel().getPrefProlog();
            //if (PreferenceLevel.sProhibited.equals(pref)) continue;
            ExamPeriod p = getModel().addPeriod(period.getUniqueId(),dateFormat.format(period.getStartDate()), timeFormat.format(period.getStartTime()), Constants.SLOT_LENGTH_MIN*period.getLength(), pref2weight(pref));
            if (PreferenceLevel.sProhibited.equals(pref)) iProhibitedPeriods.add(p);
            iPeriods.put(period.getUniqueId(),p);
        }
    }
    
    protected void loadRooms() {
        iAllRooms = Location.findAllExamLocations(iSessionId,iExamType);
        iProgress.setPhase("Loading rooms...", iAllRooms.size());
        for (Iterator i=iAllRooms.iterator();i.hasNext();) {
            iProgress.incProgress();
            Location location = (Location)i.next();
            ExamRoom room = new ExamRoom(getModel(),
                    location.getUniqueId(),
                    location.getLabel(),
                    location.getCapacity(),
                    location.getExamCapacity(),
                    (location.getCoordinateX()==null?-1:location.getCoordinateX()),
                    (location.getCoordinateY()==null?-1:location.getCoordinateY()));
            getModel().addConstraint(room);
            getModel().getRooms().add(room);
            iRooms.put(new Long(room.getId()),room);
            for (Iterator j=location.getExamPreferences(iExamType).entrySet().iterator();j.hasNext();) {
                Map.Entry entry = (Map.Entry)j.next();
                ExamPeriod period = (ExamPeriod)iPeriods.get(((org.unitime.timetable.model.ExamPeriod)entry.getKey()).getUniqueId());
                String pref = ((PreferenceLevel)entry.getValue()).getPrefProlog();
                if (period==null) continue;
                if (PreferenceLevel.sProhibited.equals(pref))
                    room.setAvailable(period.getIndex(), false);
                else
                    room.setWeight(period.getIndex(), pref2weight(pref));
            }
        }
    }
    
    protected void loadExams() {
        Collection exams = org.unitime.timetable.model.Exam.findAll(iSessionId, iExamType);
        iProgress.setPhase("Loading exams...", exams.size());
        for (Iterator i=exams.iterator();i.hasNext();) {
            iProgress.incProgress();
            org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam)i.next();
            Exam x = new Exam(
                    exam.getUniqueId(),
                    exam.getLabel(),
                    exam.getLength(),
                    false,
                    (exam.getSeatingType()==org.unitime.timetable.model.Exam.sSeatingTypeExam?true:false),
                    exam.getMaxNbrRooms());
            iExams.put(exam.getUniqueId(), x);
            getModel().addVariable(x);
            for (Iterator j=new TreeSet(exam.getOwners()).iterator();j.hasNext();) {
                ExamOwner owner = (ExamOwner)j.next();
                Object ownerObject = owner.getOwnerObject();
                if (ownerObject instanceof Class_) {
                    Class_ clazz = (Class_)ownerObject;
                    ExamCourseSection cs = new ExamCourseSection(x, owner.getUniqueId(), clazz.getClassLabel(), true);
                    x.getCourseSections().add(cs);
                } else if (ownerObject instanceof InstrOfferingConfig) {
                    InstrOfferingConfig config = (InstrOfferingConfig)ownerObject;
                    ExamCourseSection cs = new ExamCourseSection(x, owner.getUniqueId(), config.toString(), false);
                    x.getCourseSections().add(cs);
                } else if (ownerObject instanceof CourseOffering) {
                    CourseOffering course = (CourseOffering)ownerObject;
                    ExamCourseSection cs = new ExamCourseSection(x, owner.getUniqueId(), course.getCourseName(), false);
                    x.getCourseSections().add(cs);
                } else if (ownerObject instanceof InstructionalOffering) {
                    InstructionalOffering offering = (InstructionalOffering)ownerObject;
                    ExamCourseSection cs = new ExamCourseSection(x, owner.getUniqueId(), offering.getCourseName(), false);
                    x.getCourseSections().add(cs);
                }
            }
            boolean hasReqPeriod = false;
            for (Iterator j=iProhibitedPeriods.iterator();j.hasNext();) {
                ExamPeriod period = (ExamPeriod)j.next();
                x.setAvailable(period.getIndex(), false);
            }
            for (Iterator j=exam.getPreferences(ExamPeriodPref.class).iterator();j.hasNext();) {
                ExamPeriodPref periodPref = (ExamPeriodPref)j.next();
                ExamPeriod period = (ExamPeriod)iPeriods.get(periodPref.getExamPeriod().getUniqueId());
                if (period==null) continue;
                String pref = periodPref.getPrefLevel().getPrefProlog();
                if (PreferenceLevel.sRequired.equals(pref)) {
                    if (!hasReqPeriod) 
                        for (Enumeration e=getModel().getPeriods().elements();e.hasMoreElements();) {
                            ExamPeriod p = (ExamPeriod)e.nextElement();
                            x.setAvailable(p.getIndex(), false);
                        }
                    x.setAvailable(period.getIndex(), true);
                    hasReqPeriod = true;
                }
                if (!hasReqPeriod) {
                    if (PreferenceLevel.sProhibited.equals(pref)) {
                        x.setAvailable(period.getIndex(), false);
                    } else {
                        x.setAvailable(period.getIndex(), true);
                        x.setWeight(period.getIndex(), pref2weight(pref));
                    }
                }
            }
            x.setRoomWeights(findRooms(exam));
            
            for (Iterator j=exam.getInstructors().iterator();j.hasNext();)
                loadInstructor((DepartmentalInstructor)j.next()).addVariable(x);

            if (exam.getAssignedPeriod()!=null) {
                boolean fail = false;
                ExamPeriod period = (ExamPeriod)iPeriods.get(exam.getAssignedPeriod().getUniqueId());
                if (period==null) {
                    iProgress.warn("Unable to load assignment of "+getExamLabel(exam)+": "+exam.getAssignedPeriod().getName()+" is not allowed.");
                    fail = true;
                }
                if (!fail && !x.isAvailable(period)) {
                    iProgress.warn("Unable to load assignment of "+getExamLabel(exam)+": "+exam.getAssignedPeriod().getName()+" is prohibited.");
                    fail = true;
                }
                HashSet rooms = new HashSet();
                if (!fail && x.getMaxRooms()>0) {
                    for (Iterator j=exam.getAssignedRooms().iterator();j.hasNext();) {
                        Location location = (Location)j.next();
                        ExamRoom room = (ExamRoom)iRooms.get(location.getUniqueId());
                        if (room==null) {
                            iProgress.warn("Unable to load assignment of "+getExamLabel(exam)+": "+location.getLabel()+" is no longer an examination room.");
                            fail = true; break;
                        }
                        if (!x.getRooms().contains(room)) {
                            iProgress.warn("Unable to load assignment of "+getExamLabel(exam)+": location "+location.getLabel()+" is no longer valid for this exam.");
                            fail = true; break;
                        }
                        rooms.add(room);
                    }
                }
                if (!fail && rooms.size()>x.getMaxRooms()) {
                    iProgress.warn("Unable to load assignment of "+getExamLabel(exam)+": number of assigned rooms exceeds the current limit ("+rooms.size()+">"+x.getMaxRooms()+").");
                    fail = true; 
                }
                if (!fail && !x.isAvailable(period, rooms)) {
                    if (rooms.size()==1)
                        iProgress.warn("Unable to load assignment of "+getExamLabel(exam)+": location "+rooms.iterator().next()+" cannot be used at "+exam.getAssignedPeriod().getName()+".");
                    else
                        iProgress.warn("Unable to load assignment of "+getExamLabel(exam)+": one or more locations "+rooms+" cannot be used at "+exam.getAssignedPeriod().getName()+".");
                    fail = true;
                }
                if (!fail)
                    x.setInitialAssignment(new ExamPlacement(x, period, rooms));
            }
        }
    }
    
    protected ExamInstructor loadInstructor(DepartmentalInstructor instructor) {
        if (instructor.getExternalUniqueId()!=null && instructor.getExternalUniqueId().trim().length()>0) {
            ExamInstructor i = (ExamInstructor)iInstructors.get(instructor.getExternalUniqueId());
            if (i==null) {
                i = new ExamInstructor(getModel(), instructor.getUniqueId(), instructor.getName(iInstructorFormat));
                iInstructors.put(instructor.getExternalUniqueId(), i);
                getModel().addConstraint(i);
                getModel().getInstructors().add(i);
            }
            return i;
        } else {
            ExamInstructor i = (ExamInstructor)iInstructors.get(instructor.getUniqueId());
            if (i==null) {
                i = new ExamInstructor(getModel(), instructor.getUniqueId(), instructor.getName(iInstructorFormat));
                iInstructors.put(instructor.getUniqueId(), i);
                getModel().addConstraint(i);
                getModel().getInstructors().add(i);
            }
            return i;
            
        }
    }
    
    protected Hashtable findRooms(org.unitime.timetable.model.Exam exam) {
        Hashtable rooms = new Hashtable();
        boolean reqRoom = false;
        boolean reqBldg = false;
        boolean reqGroup = false;

        Set groupPrefs = exam.getPreferences(RoomGroupPref.class);
        Set roomPrefs = exam.getPreferences(RoomPref.class);
        Set bldgPrefs = exam.getPreferences(BuildingPref.class);
        Set featurePrefs = exam.getPreferences(RoomFeaturePref.class);
            
        for (Iterator i1=iAllRooms.iterator();i1.hasNext();) {
            Location room = (Location)i1.next();
            ExamRoom roomEx = (ExamRoom)iRooms.get(room.getUniqueId());
            if (roomEx==null) continue;
            boolean add = true;
            
            PreferenceCombination pref = new SumPreferenceCombination();
            
            // --- group preference ----------
            PreferenceCombination groupPref = PreferenceCombination.getDefault();
            for (Iterator i2=groupPrefs.iterator();i2.hasNext();) {
                RoomGroupPref p = (RoomGroupPref)i2.next();
                if (p.getRoomGroup().getRooms().contains(room))
                    groupPref.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
            }
            
            if (groupPref.getPreferenceProlog().equals(PreferenceLevel.sProhibited)) add=false;
            
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
            
            if (roomPref!=null && !roomPref.equals(PreferenceLevel.sProhibited) && !roomPref.equals(PreferenceLevel.sRequired)) 
                pref.addPreferenceProlog(roomPref);

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

            if (bldgPref!=null && !bldgPref.equals(PreferenceLevel.sProhibited) && !bldgPref.equals(PreferenceLevel.sRequired)) 
                pref.addPreferenceProlog(bldgPref);
            
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
            
            int prefInt = pref.getPreferenceInt();
            
            if (!add) continue;
            
            boolean canBeUsed = false;
            for (Enumeration e=getModel().getPeriods().elements();!canBeUsed && e.hasMoreElements();) {
                ExamPeriod period = (ExamPeriod)e.nextElement();
                if (roomEx.isAvailable(period) && (roomPref!=null || roomEx.getWeight(period)!=4)) canBeUsed=true;
            }
            if (!canBeUsed) continue;
            
            rooms.put(roomEx, prefInt);
        }
        return rooms;
    }
    
    protected void loadStudents() {
        loadStudents(
                new ExamDAO().getSession().createQuery(
                "select x.uniqueId, e.student.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.clazz c "+
                "where x.session.uniqueId=:sessionId and x.examType=:examType and "+
                "o.ownerType="+ExamOwner.sOwnerTypeClass+" and "+
                "o.ownerId=c.uniqueId").setLong("sessionId", iSessionId).setInteger("examType", iExamType).list(),
                "class");
        loadStudents(
                new ExamDAO().getSession().createQuery(
                "select x.uniqueId, e.student.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.clazz c " +
                "inner join c.schedulingSubpart.instrOfferingConfig ioc " +
                "where x.session.uniqueId=:sessionId and x.examType=:examType and "+
                "o.ownerType="+ExamOwner.sOwnerTypeConfig+" and "+
                "o.ownerId=ioc.uniqueId").setLong("sessionId", iSessionId).setInteger("examType", iExamType).list(),
                "config");
        loadStudents(
                new ExamDAO().getSession().createQuery(
                "select x.uniqueId, e.student.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.courseOffering co " +
                "where x.session.uniqueId=:sessionId and x.examType=:examType and "+
                "o.ownerType="+ExamOwner.sOwnerTypeCourse+" and "+
                "o.ownerId=co.uniqueId").setLong("sessionId", iSessionId).setInteger("examType", iExamType).list(),
                "course");
        loadStudents(
                new ExamDAO().getSession().createQuery(
                "select x.uniqueId, e.student.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.courseOffering.instructionalOffering io " +
                "where x.session.uniqueId=:sessionId and x.examType=:examType and "+
                "o.ownerType="+ExamOwner.sOwnerTypeOffering+" and "+
                "o.ownerId=io.uniqueId").setLong("sessionId", iSessionId).setInteger("examType", iExamType).list(),
                "offering");
    }
    
    protected void loadStudents(Collection enrl, String phase) {
        iProgress.setPhase("Loading students ("+phase+")...", enrl.size());
        for (Iterator i=enrl.iterator();i.hasNext();) {
            iProgress.incProgress();
            Object[] o = (Object[])i.next();
            Long examId = (Long)o[0];
            Long studentId = (Long)o[1];
            ExamStudent student = (ExamStudent)iStudents.get(studentId);
            if (student==null) {
                student = new ExamStudent(getModel(), studentId);
                getModel().addConstraint(student);
                getModel().getStudents().add(student);
                iStudents.put(studentId, student);
            }
            Exam exam = (Exam)iExams.get(examId);
            if (exam==null) {
                iProgress.warn("Exam "+getExamLabel(new ExamDAO().get(examId))+" not loaded.");
                continue;
            }
            if (!student.variables().contains(exam))
                student.addVariable(exam);
        }
    }
    
    protected void loadDistributions() {
        List distPrefs = new DistributionPrefDAO().getSession().createQuery(
                "select distinct d from DistributionPref d inner join d.distributionObjects o, Exam x where "+
                "d.distributionType.examPref=true and "+
                "o.prefGroup=x and x.session.uniqueId=:sessionId and x.examType=:examType and "+
                "d.owner.uniqueId=:sessionId").
                setLong("sessionId", iSessionId).
                setInteger("examType", iExamType).list();
        iProgress.setPhase("Loading distributions...", distPrefs.size());
        for (Iterator i=distPrefs.iterator();i.hasNext();) {
            iProgress.incProgress();
            DistributionPref pref = (DistributionPref)i.next();
            ExamDistributionConstraint constraint = new ExamDistributionConstraint(pref.getUniqueId(),pref.getDistributionType().getReference(),pref.getPrefLevel().getPrefProlog());
            for (Iterator j=new TreeSet(pref.getDistributionObjects()).iterator();j.hasNext();) {
                DistributionObject distObj = (DistributionObject)j.next();
                Exam exam = (Exam)iExams.get(distObj.getPrefGroup().getUniqueId());
                if (exam==null) {
                    iProgress.warn("Exam "+getExamLabel(new ExamDAO().get(distObj.getPrefGroup().getUniqueId()))+" not loaded.");
                    continue;
                }
                constraint.addVariable(exam);
            }
            if (!constraint.variables().isEmpty()) {
                getModel().addConstraint(constraint);
                getModel().getDistributionConstraints().add(constraint);
            }       
        }
    }
    
    protected void assignInitial() {
        if (iLoadSolution) {
            for (Enumeration e=getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                ExamPlacement placement = (ExamPlacement)exam.getInitialAssignment();
                if (placement==null) continue;
                Set conf = getModel().conflictValues(placement);
                if (!conf.isEmpty()) {
                    for (Iterator i=getModel().conflictConstraints(placement).entrySet().iterator();i.hasNext();) {
                        Map.Entry entry = (Map.Entry)i.next();
                        Constraint constraint = (Constraint)entry.getKey();
                        Set values = (Set)entry.getValue();
                        if (constraint instanceof ExamStudent) {
                            ((ExamStudent)constraint).setAllowDirectConflicts(true);
                            exam.setAllowDirectConflicts(true);
                            for (Iterator j=values.iterator();j.hasNext();)
                                ((Exam)((ExamPlacement)j.next()).variable()).setAllowDirectConflicts(true);
                        }
                    }
                    conf = getModel().conflictValues(placement);
                }
                if (conf.isEmpty()) {
                    exam.assign(0, placement);
                } else {
                    iProgress.warn("Unable to assign "+exam.getInitialAssignment().getName()+" to exam "+exam.getName());
                    iProgress.info("Conflicts:"+ToolBox.dict2string(getModel().conflictConstraints(exam.getInitialAssignment()), 2));
                }
            }
        }
        for (Enumeration e=new Vector(getModel().unassignedVariables()).elements();e.hasMoreElements();) {
            Exam exam = (Exam)e.nextElement();
            if (!exam.hasPreAssignedPeriod()) continue;
            ExamPlacement placement = null;
            if (exam.hasPreAssignedRooms()) {
                placement = new ExamPlacement(exam, exam.getPreAssignedPeriod(), new HashSet(exam.getPreassignedRooms()));
            } else {
                Set bestRooms = exam.findBestAvailableRooms(exam.getPreAssignedPeriod());
                if (bestRooms==null) {
                    iProgress.warn("Unable to assign "+exam.getPreAssignedPeriod()+" to exam "+exam.getName()+" -- no suitable room found.");
                    continue;
                }
                placement = new ExamPlacement(exam, exam.getPreAssignedPeriod(), bestRooms);
            }
            Set conflicts = getModel().conflictValues(placement);
            if (!conflicts.isEmpty()) {
                for (Iterator i=getModel().conflictConstraints(placement).entrySet().iterator();i.hasNext();) {
                    Map.Entry entry = (Map.Entry)i.next();
                    Constraint constraint = (Constraint)entry.getKey();
                    Set values = (Set)entry.getValue();
                    if (constraint instanceof ExamStudent) {
                        ((ExamStudent)constraint).setAllowDirectConflicts(true);
                        exam.setAllowDirectConflicts(true);
                        for (Iterator j=values.iterator();j.hasNext();)
                            ((Exam)((ExamPlacement)j.next()).variable()).setAllowDirectConflicts(true);
                    }
                }
                conflicts = getModel().conflictValues(placement);
            }
            if (conflicts.isEmpty()) {
                exam.assign(0, placement);
            } else {
                iProgress.warn("Unable to assign "+placement.getName()+" to exam "+exam.getName());
                iProgress.info("Conflicts:"+ToolBox.dict2string(getModel().conflictConstraints(exam.getInitialAssignment()), 2));
            }
        }
    }
    
}
