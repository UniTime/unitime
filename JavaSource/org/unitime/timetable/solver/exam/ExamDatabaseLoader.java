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
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventType;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import net.sf.cpsolver.coursett.preference.PreferenceCombination;
import net.sf.cpsolver.coursett.preference.SumPreferenceCombination;
import net.sf.cpsolver.exam.model.Exam;
import net.sf.cpsolver.exam.model.ExamDistributionConstraint;
import net.sf.cpsolver.exam.model.ExamInstructor;
import net.sf.cpsolver.exam.model.ExamOwner;
import net.sf.cpsolver.exam.model.ExamPeriod;
import net.sf.cpsolver.exam.model.ExamPeriodPlacement;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamRoom;
import net.sf.cpsolver.exam.model.ExamRoomPlacement;
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
    private Hashtable<Long,ExamPeriod> iPeriods = new Hashtable();
    private Hashtable iRooms = new Hashtable();
    private Hashtable iExams = new Hashtable();
    private Hashtable iInstructors = new Hashtable();
    private Hashtable iStudents = new Hashtable();
    private Set iAllRooms = null;
    private Set<ExamPeriod> iProhibitedPeriods = new HashSet(); 

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

    private String getExamLabel(Exam exam) {
        return "<A href='examDetail.do?examId="+exam.getId()+"'>"+exam.getName()+"</A>";
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
            if (org.unitime.timetable.model.Exam.sExamTypeEvening==iExamType) loadAvailabilitiesFromEvents();//loadAvailabilities();
            getModel().init();
            checkConsistency();
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
        for (Iterator i=periods.iterator();i.hasNext();) {
            iProgress.incProgress();
            org.unitime.timetable.model.ExamPeriod period = (org.unitime.timetable.model.ExamPeriod)i.next();
            String pref = period.getPrefLevel().getPrefProlog();
            //if (PreferenceLevel.sProhibited.equals(pref)) continue;
            ExamPeriod p = getModel().addPeriod(
                    period.getUniqueId(),
                    period.getStartDateLabel(),
                    period.getStartTimeLabel()+" - "+period.getEndTimeLabel(), 
                    Constants.SLOT_LENGTH_MIN*period.getLength(), 
                    pref2weight(pref));
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
                ExamPeriod period = iPeriods.get(((org.unitime.timetable.model.ExamPeriod)entry.getKey()).getUniqueId());
                String pref = ((PreferenceLevel)entry.getValue()).getPrefProlog();
                if (period==null) continue;
                if (PreferenceLevel.sProhibited.equals(pref))
                    room.setAvailable(period.getIndex(), false);
                else
                    room.setPenalty(period.getIndex(), pref2weight(pref));
            }
        }
    }
    
    protected void loadExams() {
        Collection exams = org.unitime.timetable.model.Exam.findAll(iSessionId, iExamType);
        iProgress.setPhase("Loading exams...", exams.size());
        for (Iterator i=exams.iterator();i.hasNext();) {
            iProgress.incProgress();
            org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam)i.next();
            
            Vector periodPlacements = new Vector();
            boolean hasReqPeriod = false;
            Set periodPrefs = exam.getPreferences(ExamPeriodPref.class);
            for (Enumeration e=getModel().getPeriods().elements();e.hasMoreElements(); ) {
                ExamPeriod period = (ExamPeriod)e.nextElement();
                if (iProhibitedPeriods.contains(period) ||  period.getLength()<exam.getLength()) continue;
                String pref = PreferenceLevel.sNeutral;
                for (Iterator j=periodPrefs.iterator();j.hasNext();) {
                    ExamPeriodPref periodPref = (ExamPeriodPref)j.next();
                    if (period.getId().equals(periodPref.getExamPeriod().getUniqueId())) { pref = periodPref.getPrefLevel().getPrefProlog(); break; }
                }
                if (PreferenceLevel.sProhibited.equals(pref)) continue;
                if (PreferenceLevel.sRequired.equals(pref)) {
                    if (!hasReqPeriod) periodPlacements.clear(); 
                    hasReqPeriod = true;
                    periodPlacements.add(new ExamPeriodPlacement(period, 0));
                } else if (!hasReqPeriod) {
                    periodPlacements.add(new ExamPeriodPlacement(period, pref2weight(pref)));
                }
            }
            if (periodPlacements.isEmpty()) {
                iProgress.warn("Exam "+getExamLabel(exam)+" has no period available, it is not loaded.");
                continue;
            }
            
            Exam x = new Exam(
                    exam.getUniqueId(),
                    exam.getLabel(),
                    exam.getLength(),
                    (exam.getSeatingType()==org.unitime.timetable.model.Exam.sSeatingTypeExam),
                    exam.getMaxNbrRooms(),
                    0,
                    periodPlacements,
                    findRooms(exam));
            if (exam.getAvgPeriod()!=null) x.setAveragePeriod(exam.getAvgPeriod());
            x.setModel(getModel());
            
            int minSize = 0;
            Vector<ExamOwner> owners = new Vector();
            for (Iterator j=new TreeSet(exam.getOwners()).iterator();j.hasNext();) {
                org.unitime.timetable.model.ExamOwner owner = (org.unitime.timetable.model.ExamOwner)j.next();
                Object ownerObject = owner.getOwnerObject();
                if (ownerObject instanceof Class_) {
                    Class_ clazz = (Class_)ownerObject;
                    ExamOwner cs = new ExamOwner(x, owner.getUniqueId(), clazz.getClassLabel());
                    minSize += clazz.getClassLimit();
                    x.getOwners().add(cs);
                } else if (ownerObject instanceof InstrOfferingConfig) {
                    InstrOfferingConfig config = (InstrOfferingConfig)ownerObject;
                    ExamOwner cs = new ExamOwner(x, owner.getUniqueId(), config.toString());
                    minSize += config.getLimit();
                    x.getOwners().add(cs);
                } else if (ownerObject instanceof CourseOffering) {
                    CourseOffering course = (CourseOffering)ownerObject;
                    ExamOwner cs = new ExamOwner(x, owner.getUniqueId(), course.getCourseName());
                    if (course.getInstructionalOffering().getCourseOfferings().size()>1) {
                        for (Iterator k=course.getInstructionalOffering().getCourseReservations().iterator();k.hasNext();) {
                            CourseOfferingReservation reservation = (CourseOfferingReservation)k.next();
                            if (reservation.getCourseOffering().equals(course))
                                minSize += reservation.getReserved();
                        }
                    } else {
                        minSize += (course.getInstructionalOffering().getLimit()==null?0:course.getInstructionalOffering().getLimit());
                    }
                    x.getOwners().add(cs);
                } else if (ownerObject instanceof InstructionalOffering) {
                    InstructionalOffering offering = (InstructionalOffering)ownerObject;
                    ExamOwner cs = new ExamOwner(x, owner.getUniqueId(), offering.getCourseName());
                    minSize += (offering.getLimit()==null?0:offering.getLimit());
                    x.getOwners().add(cs);
                }
            }
            
            if (iExamType==org.unitime.timetable.model.Exam.sExamTypeEvening && minSize>0)
                x.setMinSize(minSize);
            
            if (x.getMaxRooms()>0) {
                if (x.getRoomPlacements().isEmpty()) {
                    iProgress.warn("Exam "+getExamLabel(exam)+" has no room available, it is not loaded.");
                    continue;
                }
                boolean hasAssignment = false;
                for (Enumeration ep=x.getPeriodPlacements().elements();!hasAssignment && ep.hasMoreElements();) {
                    ExamPeriodPlacement period = (ExamPeriodPlacement)ep.nextElement();
                    if (x.findRoomsRandom(period)!=null) hasAssignment = true;
                }
                if (!hasAssignment) {
                    iProgress.warn("Exam "+getExamLabel(exam)+" has no available assignment, it is not loaded.");
                    continue;
                }
            }
            
            iExams.put(exam.getUniqueId(), x);
            getModel().addVariable(x);

            
            for (Iterator j=exam.getInstructors().iterator();j.hasNext();)
                loadInstructor((DepartmentalInstructor)j.next()).addVariable(x);

            if (exam.getAssignedPeriod()!=null) {
                boolean fail = false;
                ExamPeriod period = iPeriods.get(exam.getAssignedPeriod().getUniqueId());
                if (period==null) {
                    iProgress.warn("Unable assign exam "+getExamLabel(exam)+" to period "+exam.getAssignedPeriod().getName()+": period not allowed.");
                    fail = true;
                }
                ExamPeriodPlacement periodPlacement = (period==null?null:x.getPeriodPlacement(period)); 
                if (!fail && periodPlacement==null) {
                    iProgress.warn("Unable to assign exam "+getExamLabel(exam)+" to period "+exam.getAssignedPeriod().getName()+": period prohibited.");
                    fail = true;
                }
                HashSet roomPlacements = new HashSet();
                if (!fail && x.getMaxRooms()>0) {
                    for (Iterator j=exam.getAssignedRooms().iterator();j.hasNext();) {
                        Location location = (Location)j.next();
                        ExamRoom room = (ExamRoom)iRooms.get(location.getUniqueId());
                        if (room==null) {
                            iProgress.warn("Unable to assign exam "+getExamLabel(exam)+" to room "+location.getLabel()+": not an examination room.");
                            fail = true; break;
                        } else {
                            ExamRoomPlacement roomPlacement = x.getRoomPlacement(room);
                            if (roomPlacement==null) {
                                iProgress.warn("Unable to assign exam "+getExamLabel(exam)+" to room "+location.getLabel()+": room not valid for this exam.");
                                fail = true; break;
                            } else if (!roomPlacement.isAvailable(period)) {
                                iProgress.warn("Unable to assign exam "+getExamLabel(exam)+" to room "+location.getLabel()+": room not available at "+period+".");
                                fail = true; break;
                            }
                            roomPlacements.add(roomPlacement);
                        }
                    }
                }
                if (!fail && roomPlacements.size()>x.getMaxRooms()) {
                    iProgress.warn("Unable to assign exam "+getExamLabel(exam)+" to room"+(roomPlacements.size()>1?"s":"")+" "+roomPlacements+": number of assigned rooms exceeds the current limit ("+roomPlacements.size()+">"+x.getMaxRooms()+").");
                    fail = true; 
                }
                if (!fail)
                    x.setInitialAssignment(new ExamPlacement(x, periodPlacement, roomPlacements));
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
    
    protected ExamInstructor getInstructor(DepartmentalInstructor instructor) {
        if (instructor.getExternalUniqueId()!=null && instructor.getExternalUniqueId().trim().length()>0) {
            ExamInstructor i = (ExamInstructor)iInstructors.get(instructor.getExternalUniqueId());
            if (i!=null) return i;
        }
        return (ExamInstructor)iInstructors.get(instructor.getUniqueId());
    }

    protected Vector<ExamRoomPlacement> findRooms(org.unitime.timetable.model.Exam exam) {
        Vector<ExamRoomPlacement> rooms = new Vector();
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
            boolean hasStrongDisc = false, allStrongDisc = true;
            for (Enumeration e=getModel().getPeriods().elements();e.hasMoreElements();) {
                ExamPeriod period = (ExamPeriod)e.nextElement();
                if (roomEx.isAvailable(period))
                    if (roomEx.getPenalty(period)==4) hasStrongDisc = true;
                    else allStrongDisc = false;
            }
            //all strongly discouraged and not overridden by room preference -> do not use this room
            if (allStrongDisc && roomPref==null) continue;
            
            //has strongly discouraged and not overridden by room preference -> strongly discouraged periods are not available
            rooms.add(new ExamRoomPlacement(roomEx, prefInt, (hasStrongDisc && roomPref==null?3:100)));
        }
        return rooms;
    }
    
    protected void loadStudents() {
        loadStudents(
                new ExamDAO().getSession().createQuery(
                "select x.uniqueId, o.uniqueId, e.student.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.clazz c "+
                "where x.session.uniqueId=:sessionId and x.examType=:examType and "+
                "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeClass+" and "+
                "o.ownerId=c.uniqueId").setLong("sessionId", iSessionId).setInteger("examType", iExamType).list(),
                "class");
        loadStudents(
                new ExamDAO().getSession().createQuery(
                "select x.uniqueId, o.uniqueId, e.student.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.clazz c " +
                "inner join c.schedulingSubpart.instrOfferingConfig ioc " +
                "where x.session.uniqueId=:sessionId and x.examType=:examType and "+
                "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeConfig+" and "+
                "o.ownerId=ioc.uniqueId").setLong("sessionId", iSessionId).setInteger("examType", iExamType).list(),
                "config");
        loadStudents(
                new ExamDAO().getSession().createQuery(
                "select x.uniqueId, o.uniqueId, e.student.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.courseOffering co " +
                "where x.session.uniqueId=:sessionId and x.examType=:examType and "+
                "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeCourse+" and "+
                "o.ownerId=co.uniqueId").setLong("sessionId", iSessionId).setInteger("examType", iExamType).list(),
                "course");
        loadStudents(
                new ExamDAO().getSession().createQuery(
                "select x.uniqueId, o.uniqueId, e.student.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.courseOffering.instructionalOffering io " +
                "where x.session.uniqueId=:sessionId and x.examType=:examType and "+
                "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeOffering+" and "+
                "o.ownerId=io.uniqueId").setLong("sessionId", iSessionId).setInteger("examType", iExamType).list(),
                "offering");
    }
    
    protected void loadStudents(Collection enrl, String phase) {
        HashSet notLoaded = new HashSet();
        iProgress.setPhase("Loading students ("+phase+")...", enrl.size());
        for (Iterator i=enrl.iterator();i.hasNext();) {
            iProgress.incProgress();
            Object[] o = (Object[])i.next();
            Long examId = (Long)o[0];
            Long ownerId = (Long)o[1];
            Long studentId = (Long)o[2];
            ExamStudent student = (ExamStudent)iStudents.get(studentId);
            if (student==null) {
                student = new ExamStudent(getModel(), studentId);
                getModel().addConstraint(student);
                getModel().getStudents().add(student);
                iStudents.put(studentId, student);
            }
            Exam exam = (Exam)iExams.get(examId);
            if (exam==null) {
                if (notLoaded.add(examId))
                    iProgress.info("Exam "+getExamLabel(new ExamDAO().get(examId))+" not loaded.");
                continue;
            }
            if (!student.variables().contains(exam))
                student.addVariable(exam);
            for (Enumeration e=exam.getOwners().elements();e.hasMoreElements();) {
                ExamOwner owner = (ExamOwner)e.nextElement();
                if (owner.getId()==ownerId) owner.getStudents().add(student);
            }
        }
    }
    
    protected void loadAvailabilities() {
        List committedAssignments = new ExamDAO().getSession().createQuery(
                "select a from Assignment a where a.solution.commited=true and " +
                "a.solution.owner.session.uniqueId=:sessionId").
                setLong("sessionId",iSessionId).list();
        Set periods = org.unitime.timetable.model.ExamPeriod.findAll(iSessionId, iExamType);
        iProgress.setPhase("Loading availabilities...", committedAssignments.size());
        for (Iterator i=committedAssignments.iterator();i.hasNext();) {
            iProgress.incProgress();
            Assignment a = (Assignment)i.next();
            List studentIds = null;
            for (Iterator j=periods.iterator();j.hasNext();) {
                org.unitime.timetable.model.ExamPeriod period = (org.unitime.timetable.model.ExamPeriod)j.next();
                if (period.overlap(a)) {
                    iProgress.debug("Class "+a.getClassName()+" "+a.getPlacement().getLongName()+" overlaps with period "+period.getName());
                    ExamPeriod exPeriod = iPeriods.get(period.getUniqueId());
                    ExamResourceUnavailability unavailability = new ExamResourceUnavailability(exPeriod, a.getUniqueId(), "class", 
                            a.getClassName(), a.getPlacement().getTimeLocation().getDatePatternName(),
                            a.getPlacement().getTimeLocation().getDayHeader()+" "+a.getPlacement().getTimeLocation().getStartTimeHeader()+" - "+a.getPlacement().getTimeLocation().getEndTimeHeader(), 
                            a.getPlacement().getRoomName(", "), a.getClazz().getClassLimit());
                    if (studentIds==null)
                        studentIds = new ExamDAO().getSession().createQuery(
                                "select e.student.uniqueId from "+
                                "StudentClassEnrollment e where e.clazz.uniqueId=:classId").
                                setLong("classId", a.getClassId()).list(); 
                    for (Iterator k=studentIds.iterator();k.hasNext();) {
                        Long studentId = (Long)k.next();
                        ExamStudent student = (ExamStudent)iStudents.get(studentId);
                        if (student!=null) { 
                            student.setAvailable(exPeriod.getIndex(), false);
                            unavailability.getStudentIds().add(student.getId());
                        }
                    }
                    for (Iterator k=a.getClazz().getClassInstructors().iterator();k.hasNext();) {
                        ClassInstructor ci = (ClassInstructor)k.next();
                        if (!ci.isLead()) continue;
                        ExamInstructor instructor = getInstructor(ci.getInstructor());
                        if (instructor!=null) {
                            instructor.setAvailable(exPeriod.getIndex(), false);
                            unavailability.getInstructorIds().add(instructor.getId());
                        }
                    }
                    getModel().addUnavailability(unavailability);
                }
            }
        }
    }
    
    protected void loadAvailabilitiesFromEvents() {
        List overlappingEvents = new EventDAO().getSession().createQuery(
                "select distinct e, p.uniqueId, m from Event e inner join e.meetings m, ExamPeriod p where " +
                "m.eventType.reference=:eventType and p.session.uniqueId=:sessionId and p.examType=:examType and "+
                "p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                "p.session.examBeginDate+p.dateOffset = m.meetingDate"
                )
                .setString("eventType", EventType.sEventTypeClass)
                .setInteger("travelTime", Constants.EXAM_TRAVEL_TIME_SLOTS)
                .setInteger("examType", iExamType)
                .setLong("sessionId", iSessionId)
                .setCacheable(true)
                .list();
        iProgress.setPhase("Loading availabilities...", overlappingEvents.size());
        
        Hashtable<Event, Set<Long>> students = new Hashtable();
        Hashtable<Event, Set<DepartmentalInstructor>> instructors = new Hashtable();
        Hashtable<Event, Hashtable<ExamPeriod,ExamResourceUnavailability>> unavailabilities = new Hashtable();
        
        for (Iterator i=overlappingEvents.iterator();i.hasNext();) {
            iProgress.incProgress();
            Object[] o = (Object[])i.next();
            Event event = (Event)o[0];
            ExamPeriod period = iPeriods.get((Long)o[1]);
            Meeting meeting = (Meeting)o[2];
            if (period==null) continue;
            
            Set<Long> studentsThisEvent = students.get(event);
            if (studentsThisEvent==null) {
                studentsThisEvent = event.getStudentIds();
                students.put(event, studentsThisEvent);
            }
            
            Set<DepartmentalInstructor> instructorsThisEvent = instructors.get(event);
            if (instructorsThisEvent==null) {
                instructorsThisEvent = event.getInstructors();
                instructors.put(event, instructorsThisEvent);
            }
            
            Hashtable<ExamPeriod,ExamResourceUnavailability> unavailabilitiesThisEvent = unavailabilities.get(event);
            if (unavailabilitiesThisEvent==null) {
                unavailabilitiesThisEvent = new Hashtable();
                unavailabilities.put(event, unavailabilitiesThisEvent);
            }
            
            ExamResourceUnavailability unavailability = unavailabilitiesThisEvent.get(period);
            if (unavailability!=null) {
                if (meeting.getLocation()!=null) unavailability.addRoom(meeting.getLocation().getLabel());
                continue;
            }
            unavailability = new ExamResourceUnavailability(
                    period, meeting.getEvent().getUniqueId(), "event", 
                    meeting.getEvent().getEventName(), meeting.dateStr(), meeting.startTime()+" - "+meeting.stopTime(), 
                    meeting.getRoomLabel(), meeting.getEvent().getMaxCapacity());
            unavailabilitiesThisEvent.put(period, unavailability);
            
            for (Long studentId : studentsThisEvent) {
                ExamStudent student = (ExamStudent)iStudents.get(studentId);
                if (student!=null) {
                    student.setAvailable(period.getIndex(), false);
                    unavailability.getStudentIds().add(student.getId());
                }
            }
            
            for (DepartmentalInstructor di : instructorsThisEvent) {
                ExamInstructor instructor = getInstructor(di);
                if (instructor!=null) {
                    instructor.setAvailable(period.getIndex(), false);
                    unavailability.getInstructorIds().add(instructor.getId());
                }
            }
            
            getModel().addUnavailability(unavailability);
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
                    iProgress.info("Exam "+getExamLabel(new ExamDAO().get(distObj.getPrefGroup().getUniqueId()))+" not loaded.");
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
            iProgress.setPhase("Assigning loaded solution...", getModel().variables().size());
            for (Enumeration e=getModel().variables().elements();e.hasMoreElements();) {
                iProgress.incProgress();
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
    }
    
    protected void checkConsistency() {
        iProgress.setPhase("Checking consistency...", getModel().variables().size());
        for (Enumeration e=getModel().variables().elements();e.hasMoreElements();) {
            iProgress.incProgress();
            Exam exam = (Exam)e.nextElement();
            if (exam.getPeriodPlacements().isEmpty()) {
                iProgress.error("Exam "+getExamLabel(exam)+" has not period available.");
                continue;
            }
            if (exam.getMaxRooms()>0) {
                int capacity = 0;
                for (int i = 0; i < Math.min(exam.getMaxRooms(), exam.getRoomPlacements().size()); i++) {
                    ExamRoomPlacement r = (ExamRoomPlacement)exam.getRoomPlacements().elementAt(i);
                    capacity += r.getSize(exam.hasAltSeating());
                }
                if (capacity<exam.getStudents().size()) {
                    iProgress.error("Exam "+getExamLabel(exam)+" has not room placement available.");
                    continue;
                }
                boolean hasValue = false;
                for (Enumeration f=exam.getPeriodPlacements().elements();!hasValue && f.hasMoreElements();) {
                    ExamPeriodPlacement period = (ExamPeriodPlacement)f.nextElement();
                    if (exam.findBestAvailableRooms(period)!=null) hasValue = true;
                }
                if (!hasValue) {
                    iProgress.error("Exam "+getExamLabel(exam)+" has not assignment available.");
                    continue;
                }
            }
        }
    }
    
}
