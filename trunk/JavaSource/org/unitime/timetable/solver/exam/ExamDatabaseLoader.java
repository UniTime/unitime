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
package org.unitime.timetable.solver.exam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.cpsolver.coursett.preference.SumPreferenceCombination;
import org.cpsolver.exam.model.Exam;
import org.cpsolver.exam.model.ExamDistributionConstraint;
import org.cpsolver.exam.model.ExamInstructor;
import org.cpsolver.exam.model.ExamOwner;
import org.cpsolver.exam.model.ExamPeriod;
import org.cpsolver.exam.model.ExamPeriodPlacement;
import org.cpsolver.exam.model.ExamPlacement;
import org.cpsolver.exam.model.ExamRoom;
import org.cpsolver.exam.model.ExamRoomPlacement;
import org.cpsolver.exam.model.ExamStudent;
import org.cpsolver.exam.model.PredefinedExamRoomSharing;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.CacheMode;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller
 */
public class ExamDatabaseLoader extends ExamLoader {
    private static Log sLog = LogFactory.getLog(ExamDatabaseLoader.class);
    private Long iSessionId;
    private Long iExamTypeId;
    private boolean iLoadSolution;
    private String iInstructorFormat;
    private Progress iProgress = null;
    private Hashtable<Long,ExamPeriod> iPeriods = new Hashtable();
    private Hashtable<Long,ExamRoom> iRooms = new Hashtable();
    private Hashtable<Long,ExamRoom> iPermId2Room = new Hashtable();
    private Hashtable<Long,org.unitime.timetable.model.ExamOwner> iOwners = new Hashtable();
    private Hashtable iExams = new Hashtable();
    private Hashtable iInstructors = new Hashtable();
    private Hashtable iStudents = new Hashtable();
    private Set iAllRooms = null;
    private Set<ExamPeriod> iProhibitedPeriods = new HashSet();
    private PredefinedExamRoomSharing iSharing = null;
    
    private boolean iRoomAvailabilityTimeStampIsSet = false;
    
    public ExamDatabaseLoader(ExamModel model, Assignment<Exam, ExamPlacement> assignment) {
        super(model, assignment);
        iProgress = Progress.getInstance(model);
        iSessionId = model.getProperties().getPropertyLong("General.SessionId",(Long)null);
        iExamTypeId = model.getProperties().getPropertyLong("Exam.Type", null);
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
        hibSession.setCacheMode(CacheMode.IGNORE);
        Transaction tx = null;
        try {
            tx = hibSession.beginTransaction();
            TravelTime.populateTravelTimes(getModel().getDistanceMetric(), iSessionId, hibSession);
            loadPeriods();
            loadRooms();
            
        	RoomAvailabilityInterface availability = null;
        	if (SolverServerImplementation.getInstance() != null)
        		availability = SolverServerImplementation.getInstance().getRoomAvailability();
        	else
        		availability = RoomAvailability.getInstance();
            if (availability != null) loadRoomAvailability(availability);

            loadExams();
            loadStudents();
            loadDistributions();
            ExamType type = ExamTypeDAO.getInstance().get(iExamTypeId, hibSession);
            if ("true".equals(ApplicationProperties.getProperty("tmtbl.exam.eventConflicts."+type.getReference(),"true"))) loadAvailabilitiesFromEvents();
            if ("true".equals(ApplicationProperties.getProperty("tmtbl.exam.sameRoom."+type.getReference(),"false"))) makeupSameRoomConstraints();
            getModel().init();
            checkConsistency();
            assignInitial();
            tx.commit();
        } catch (Exception e) {
            iProgress.fatal("Unable to load examination problem, reason: "+e.getMessage(),e);
            if (tx!=null) tx.rollback();
            throw e;
        } finally {
    		// here we need to close the session since this code may run in a separate thread
    		if (hibSession!=null && hibSession.isOpen()) hibSession.close();
        }
    }
    
    public int pref2weight(String pref) {
        if (pref==null) return 0;
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
        Set periods = org.unitime.timetable.model.ExamPeriod.findAll(iSessionId, iExamTypeId);
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
        iAllRooms = Location.findAllExamLocations(iSessionId,iExamTypeId);
        iProgress.setPhase("Loading rooms...", iAllRooms.size());
        for (Iterator i=iAllRooms.iterator();i.hasNext();) {
            iProgress.incProgress();
            Location location = (Location)i.next();
            ExamRoom room = new ExamRoom(getModel(),
                    location.getUniqueId(),
                    location.getLabel(),
                    location.getCapacity(),
                    location.getExamCapacity(),
                    location.getCoordinateX(),
                    location.getCoordinateY());
            getModel().addConstraint(room);
            getModel().getRooms().add(room);
            iRooms.put(room.getId(),room);
            if (location.getPermanentId()!=null) iPermId2Room.put(location.getPermanentId(), room);
            for (Iterator j=location.getExamPreferences(iExamTypeId).entrySet().iterator();j.hasNext();) {
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
        if (SolverServerImplementation.getInstance() != null) HibernateUtil.clearCache();
        Collection exams = org.unitime.timetable.model.Exam.findAll(iSessionId, iExamTypeId);
        ExamType type = ExamTypeDAO.getInstance().get(iExamTypeId);
        boolean considerLimit = "true".equals(ApplicationProperties.getProperty("tmtbl.exam.useLimit."+type.getUniqueId(), (type.getType() == ExamType.sExamTypeFinal?"false":"true")));
        iProgress.setPhase("Loading exams...", exams.size());
        for (Iterator i=exams.iterator();i.hasNext();) {
            iProgress.incProgress();
            org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam)i.next();
            
            List<ExamPeriodPlacement> periodPlacements = new ArrayList<ExamPeriodPlacement>();
            boolean hasReqPeriod = false;
            Set periodPrefs = exam.getPreferences(ExamPeriodPref.class);
            for (ExamPeriod period: getModel().getPeriods()) {
                if (iProhibitedPeriods.contains(period) ||  period.getLength()<exam.getLength()) continue;
                String pref = null;
                for (Iterator j=periodPrefs.iterator();j.hasNext();) {
                    ExamPeriodPref periodPref = (ExamPeriodPref)j.next();
                    if (period.getId().equals(periodPref.getExamPeriod().getUniqueId())) { pref = periodPref.getPrefLevel().getPrefProlog(); break; }
                }
                if (type.getType() == ExamType.sExamTypeMidterm && pref==null) continue;
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
                    (exam.getSeatingType().intValue()==org.unitime.timetable.model.Exam.sSeatingTypeExam),
                    exam.getMaxNbrRooms(),
                    0,
                    periodPlacements,
                    findRooms(exam));
            if (type.getType() == ExamType.sExamTypeFinal) {
                if (exam.getAvgPeriod()!=null) x.setAveragePeriod(exam.getAvgPeriod());
                else x.setAveragePeriod(getModel().getPeriods().size()/2);
            }
            x.setModel(getModel());
            
            int minSize = 0;
            for (Iterator j=new TreeSet(exam.getOwners()).iterator();j.hasNext();) {
                org.unitime.timetable.model.ExamOwner owner = (org.unitime.timetable.model.ExamOwner)j.next();
                ExamOwner cs = new ExamOwner(x, owner.getUniqueId(), owner.getLabel());
                iOwners.put(owner.getUniqueId(), owner);
                minSize += owner.getLimit();
                x.getOwners().add(cs);
            }
            x.setSizeOverride(exam.getExamSize());
            x.setPrintOffset(exam.examOffset());
            
            if (considerLimit && minSize>0)
                x.setMinSize(minSize);
            
            if (x.getMaxRooms()>0) {
                if (x.getRoomPlacements().isEmpty()) {
                    iProgress.warn("Exam "+getExamLabel(exam)+" has no room available, it is not loaded.");
                    continue;
                }
                boolean hasAssignment = false;
                for (Iterator<ExamPeriodPlacement> ep=x.getPeriodPlacements().iterator();!hasAssignment && ep.hasNext();) {
                    ExamPeriodPlacement period = ep.next();
                    if (x.findRoomsRandom(getAssignment(), period)!=null) hasAssignment = true;
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
                        ExamRoom room = iRooms.get(location.getUniqueId());
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

    protected List<ExamRoomPlacement> findRooms(org.unitime.timetable.model.Exam exam) {
        List<ExamRoomPlacement> rooms = new ArrayList<ExamRoomPlacement>();
        boolean reqRoom = false;
        boolean reqBldg = false;
        boolean reqGroup = false;

        Set groupPrefs = exam.getPreferences(RoomGroupPref.class);
        Set roomPrefs = exam.getPreferences(RoomPref.class);
        Set bldgPrefs = exam.getPreferences(BuildingPref.class);
        Set featurePrefs = exam.getPreferences(RoomFeaturePref.class);
            
        for (Iterator i1=iAllRooms.iterator();i1.hasNext();) {
            Location room = (Location)i1.next();
            ExamRoom roomEx = iRooms.get(room.getUniqueId());
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
            
            boolean hasStrongDisc = false, allStrongDisc = true;
            for (ExamPeriod period: getModel().getPeriods()) {
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
                "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeClass+" and "+
                "o.ownerId=c.uniqueId").setLong("sessionId", iSessionId).setLong("examTypeId", iExamTypeId).list(),
                "class");
        loadStudents(
                new ExamDAO().getSession().createQuery(
                "select x.uniqueId, o.uniqueId, e.student.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.clazz c " +
                "inner join c.schedulingSubpart.instrOfferingConfig ioc " +
                "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeConfig+" and "+
                "o.ownerId=ioc.uniqueId").setLong("sessionId", iSessionId).setLong("examTypeId", iExamTypeId).list(),
                "config");
        loadStudents(
                new ExamDAO().getSession().createQuery(
                "select x.uniqueId, o.uniqueId, e.student.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.courseOffering co " +
                "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeCourse+" and "+
                "o.ownerId=co.uniqueId").setLong("sessionId", iSessionId).setLong("examTypeId", iExamTypeId).list(),
                "course");
        loadStudents(
                new ExamDAO().getSession().createQuery(
                "select x.uniqueId, o.uniqueId, e.student.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.courseOffering.instructionalOffering io " +
                "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeOffering+" and "+
                "o.ownerId=io.uniqueId").setLong("sessionId", iSessionId).setLong("examTypeId", iExamTypeId).list(),
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
            for (ExamOwner owner: exam.getOwners()) {
                if (owner.getId()==ownerId) owner.getStudents().add(student);
            }
        }
    }
    
    /*
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
    */
    
    protected void loadAvailabilitiesFromEvents() {
        List overlappingClassEvents = 
                new EventDAO().getSession().createQuery(
                        "select distinct e.uniqueId, p.uniqueId, m from ClassEvent e inner join e.meetings m, ExamPeriod p where " +
                        "p.session.uniqueId=:sessionId and p.examType.uniqueId=:examTypeId and "+
                        "p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                        HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate and "+
                        "(exists elements(e.clazz.studentEnrollments) or exists elements(e.clazz.classInstructors))"
                        )
                        .setInteger("travelTime", Integer.parseInt(ApplicationProperties.getProperty("tmtbl.exam.eventConflicts.travelTime.classEvent","6")))
                        .setLong("examTypeId", iExamTypeId)
                        .setLong("sessionId", iSessionId)
                        .setCacheable(true)
                        .list();
        List overlappingCourseEvents =
                new EventDAO().getSession().createQuery(
                        "select distinct e.uniqueId, p.uniqueId, m from CourseEvent e inner join e.meetings m, ExamPeriod p where " +
                        "e.reqAttendance=true and m.approvalStatus = 1 and p.session.uniqueId=:sessionId and p.examType.uniqueId=:examTypeId and "+
                        "p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                        HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate")
                        .setInteger("travelTime", Integer.parseInt(ApplicationProperties.getProperty("tmtbl.exam.eventConflicts.travelTime.courseEvent","0")))
                        .setLong("examTypeId", iExamTypeId)
                        .setLong("sessionId", iSessionId)
                        .setCacheable(true)
                        .list();
        iProgress.setPhase("Loading availabilities...", overlappingClassEvents.size()+overlappingCourseEvents.size());
        
        Hashtable<Long, Set<ExamStudent>> students = new Hashtable();
        for (Iterator i=new EventDAO().getSession().createQuery(
                "select e.uniqueId, s.student.uniqueId from ClassEvent e inner join e.meetings m inner join e.clazz.studentEnrollments s, ExamPeriod p where " +
                "p.session.uniqueId=:sessionId and p.examType.uniqueId=:examTypeId and "+
                "p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate")
                .setInteger("travelTime", Integer.parseInt(ApplicationProperties.getProperty("tmtbl.exam.eventConflicts.travelTime.classEvent","6")))
                .setLong("examTypeId", iExamTypeId)
                .setLong("sessionId", iSessionId)
                .setCacheable(true).list().iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
            Long eventId = (Long)o[0];
            Long studentId = (Long)o[1];
            ExamStudent student = (ExamStudent)iStudents.get(studentId);
            if (student==null) continue;
            Set<ExamStudent> studentsThisEvent = students.get(eventId);
            if (studentsThisEvent==null) { studentsThisEvent=new HashSet(); students.put(eventId, studentsThisEvent); }
            studentsThisEvent.add(student);
        }
        
        Hashtable<Long, Set<ExamInstructor>> instructors = new Hashtable();
        for (Iterator i=new EventDAO().getSession().createQuery(
                "select e.uniqueId, i.instructor from ClassEvent e inner join e.meetings m inner join e.clazz.classInstructors i, ExamPeriod p where " +
                "p.session.uniqueId=:sessionId and p.examType.uniqueId=:examTypeId and i.lead=true and "+
                "p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate")
                .setInteger("travelTime", Integer.parseInt(ApplicationProperties.getProperty("tmtbl.exam.eventConflicts.travelTime.classEvent","6")))
                .setLong("examTypeId", iExamTypeId)
                .setLong("sessionId", iSessionId)
                .setCacheable(true).list().iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
            Long eventId = (Long)o[0];
            ExamInstructor instructor = getInstructor((DepartmentalInstructor)o[1]);
            if (instructor==null) continue;
            Set<ExamInstructor> instructorsThisEvent = instructors.get(eventId);
            if (instructorsThisEvent==null) { instructorsThisEvent=new HashSet(); instructors.put(eventId, instructorsThisEvent); }
            instructorsThisEvent.add(instructor);
        }
        
        Hashtable<Long, Hashtable<ExamPeriod,ExamResourceUnavailability>> unavailabilities = new Hashtable();
        
        for (Iterator i=overlappingClassEvents.iterator();i.hasNext();) {
            iProgress.incProgress();
            Object[] o = (Object[])i.next();
            Long eventId = (Long)o[0];
            ExamPeriod period = iPeriods.get((Long)o[1]);
            Meeting meeting = (Meeting)o[2];
            if (period==null) continue;
            
            Set<ExamStudent> studentsThisEvent = students.get(eventId);
            Set<ExamInstructor> instructorsThisEvent = instructors.get(eventId);
            
            if ((studentsThisEvent==null || studentsThisEvent.isEmpty()) && (instructorsThisEvent==null || instructorsThisEvent.isEmpty())) continue;
            
            Set<Long> studentIds = new HashSet();
            Set<Long> instructorIds = new HashSet();
            
            if (studentsThisEvent!=null) {
                for (ExamStudent student : studentsThisEvent) {
                    student.setAvailable(period.getIndex(), false);
                    studentIds.add(student.getId());
                }
            }
            
            if (instructorsThisEvent!=null) {
                for (ExamInstructor instructor : instructorsThisEvent) {
                    instructor.setAvailable(period.getIndex(), false);
                    instructorIds.add(instructor.getId());
                }
            }
            
            if (studentIds.isEmpty() && instructorIds.isEmpty()) continue;
            
            Hashtable<ExamPeriod,ExamResourceUnavailability> unavailabilitiesThisEvent = unavailabilities.get(eventId);
            if (unavailabilitiesThisEvent==null) {
                unavailabilitiesThisEvent = new Hashtable();
                unavailabilities.put(eventId, unavailabilitiesThisEvent);
            }
            
            ExamRoom room = (meeting.getLocationPermanentId()==null?null:iPermId2Room.get(meeting.getLocationPermanentId()));
            
            ExamResourceUnavailability unavailability = unavailabilitiesThisEvent.get(period);
            if (unavailability!=null) {
                if (room!=null) unavailability.addRoom(room.getName());
                continue;
            }
            
            unavailability = new ExamResourceUnavailability(
                    period, meeting.getEvent().getUniqueId(), "class", 
                    meeting.getEvent().getEventName(), meeting.dateStr(), meeting.startTime()+" - "+meeting.stopTime(), 
                    (room==null?"":room.getName()), (studentsThisEvent==null?0:studentsThisEvent.size()), studentIds, instructorIds);
            
            iProgress.debug("Class event "+meeting.getEvent().getEventName()+" "+meeting.getTimeLabel()+" overlaps with period "+period+" ("+studentIds.size()+" students, "+instructorIds.size()+" instructors)");
            
            unavailabilitiesThisEvent.put(period, unavailability);
            getModel().addUnavailability(unavailability);
        }
        
        for (Iterator i=overlappingCourseEvents.iterator();i.hasNext();) {
            iProgress.incProgress();
            Object[] o = (Object[])i.next();
            Long eventId = (Long)o[0];
            ExamPeriod period = iPeriods.get((Long)o[1]);
            Meeting meeting = (Meeting)o[2];
            if (period==null) continue;

            Set<ExamStudent> studentsThisEvent = students.get(eventId);
            if (studentsThisEvent==null) {
                studentsThisEvent = new HashSet();
                for (Long studentId : meeting.getEvent().getStudentIds()) {
                    ExamStudent student = (ExamStudent)iStudents.get(studentId);
                    if (student!=null) studentsThisEvent.add(student);
                }
                students.put(eventId, studentsThisEvent);
            }

            Set<ExamInstructor> instructorsThisEvent = instructors.get(eventId);
            
            if ((studentsThisEvent==null || studentsThisEvent.isEmpty()) && (instructorsThisEvent==null || instructorsThisEvent.isEmpty())) continue;
            
            Set<Long> studentIds = new HashSet();
            Set<Long> instructorIds = new HashSet();
            
            if (studentsThisEvent!=null) {
                for (ExamStudent student : studentsThisEvent) {
                    student.setAvailable(period.getIndex(), false);
                    studentIds.add(student.getId());
                }
            }
            
            if (instructorsThisEvent!=null) {
                for (ExamInstructor instructor : instructorsThisEvent) {
                    instructor.setAvailable(period.getIndex(), false);
                    instructorIds.add(instructor.getId());
                }
            }
            if (studentIds.isEmpty() && instructorIds.isEmpty()) continue;

            Hashtable<ExamPeriod,ExamResourceUnavailability> unavailabilitiesThisEvent = unavailabilities.get(eventId);
            if (unavailabilitiesThisEvent==null) {
                unavailabilitiesThisEvent = new Hashtable();
                unavailabilities.put(eventId, unavailabilitiesThisEvent);
            }
            
            ExamRoom room = (meeting.getLocationPermanentId()==null?null:iPermId2Room.get(meeting.getLocationPermanentId()));
            
            ExamResourceUnavailability unavailability = unavailabilitiesThisEvent.get(period);
            if (unavailability!=null) {
                if (room!=null) unavailability.addRoom(room.getName());
                continue;
            }
            
            unavailability = new ExamResourceUnavailability(
                    period, meeting.getEvent().getUniqueId(), "event", 
                    meeting.getEvent().getEventName(), meeting.dateStr(), meeting.startTime()+" - "+meeting.stopTime(), 
                    (room==null?"":room.getName()), (studentsThisEvent==null?0:studentsThisEvent.size()), studentIds, instructorIds);
            
            iProgress.debug("Class event "+meeting.getEvent().getEventName()+"/"+meeting.getTimeLabel()+" overlaps with period "+period+" ("+studentIds.size()+" students, "+instructorIds.size()+" instructors)");
            
            unavailabilitiesThisEvent.put(period, unavailability);
            getModel().addUnavailability(unavailability);
        }        
    }
    
    protected void loadDistributions() {
        List distPrefs = new DistributionPrefDAO().getSession().createQuery(
                "select distinct d from DistributionPref d inner join d.distributionObjects o, Exam x where "+
                "d.distributionType.examPref=true and "+
                "o.prefGroup=x and x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                "d.owner.uniqueId=:sessionId").
                setLong("sessionId", iSessionId).
                setLong("examTypeId", iExamTypeId).list();
        iProgress.setPhase("Loading distributions...", distPrefs.size());
        for (Iterator i=distPrefs.iterator();i.hasNext();) {
            iProgress.incProgress();
            DistributionPref pref = (DistributionPref)i.next();
            if ("EX_SHARE_ROOM".equals(pref.getDistributionType().getReference())) {
            	if (iSharing == null) {
            		if (getModel().hasRoomSharing() && getModel().getRoomSharing() instanceof PredefinedExamRoomSharing) {
            			iSharing = (PredefinedExamRoomSharing)getModel().getRoomSharing();
            		} else {
                		iSharing = new PredefinedExamRoomSharing(getModel(), getModel().getProperties());
                		getModel().getProperties().setProperty("Exams.RoomSharingClass", PredefinedExamRoomSharing.class.getName());
                		getModel().setRoomSharing(iSharing);
            		}
            	}
            	List<Exam> exams = new ArrayList<Exam>();
            	for (Iterator j=new TreeSet(pref.getDistributionObjects()).iterator();j.hasNext();) {
                    DistributionObject distObj = (DistributionObject)j.next();
                    Exam exam = (Exam)iExams.get(distObj.getPrefGroup().getUniqueId());
                    if (exam==null) {
                        iProgress.info("Exam "+getExamLabel(new ExamDAO().get(distObj.getPrefGroup().getUniqueId()))+" not loaded.");
                        continue;
                    }
                    exams.add(exam);
            	}
            	for (int a = 0; a < exams.size(); a++) {
            		for (int b = a + 1; b < exams.size(); b++) {
            			iSharing.addPair(exams.get(a), exams.get(b));
            		}
            	}
            } else {
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
    }
    
    protected void assignInitial() {
        if (iLoadSolution) {
            iProgress.setPhase("Assigning loaded solution...", getModel().variables().size());
            for (Exam exam: getModel().variables()) {
                iProgress.incProgress();
                ExamPlacement placement = (ExamPlacement)exam.getInitialAssignment();
                if (placement==null) continue;
                Set conf = getModel().conflictValues(getAssignment(), placement);
                if (!conf.isEmpty()) {
                    for (Iterator i=getModel().conflictConstraints(getAssignment(), placement).entrySet().iterator();i.hasNext();) {
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
                    conf = getModel().conflictValues(getAssignment(), placement);
                }
                if (conf.isEmpty()) {
                	getAssignment().assign(0, placement);
                } else {
                    iProgress.warn("Unable to assign "+exam.getInitialAssignment().getName()+" to exam "+exam.getName());
                    iProgress.info("Conflicts:"+ToolBox.dict2string(getModel().conflictConstraints(getAssignment(), exam.getInitialAssignment()), 2));
                }
            }
        }
    }
    
    protected void checkConsistency() {
        iProgress.setPhase("Checking consistency...", getModel().variables().size());
        for (Exam exam: getModel().variables()) {
            iProgress.incProgress();
           if (exam.getPeriodPlacements().isEmpty()) {
                iProgress.error("Exam "+getExamLabel(exam)+" has no period available.");
                continue;
            }
            if (exam.getMaxRooms()>0) {
                int capacity = 0;
                for (int i = 0; i < Math.min(exam.getMaxRooms(), exam.getRoomPlacements().size()); i++) {
                    ExamRoomPlacement r = (ExamRoomPlacement)exam.getRoomPlacements().get(i);
                    capacity += r.getSize(exam.hasAltSeating());
                }
                if (capacity<exam.getSize()) {
                    iProgress.error("Exam "+getExamLabel(exam)+" has no room placement available.");
                    continue;
                }
                boolean hasValue = false;
                for (Iterator<ExamPeriodPlacement> f=exam.getPeriodPlacements().iterator();!hasValue && f.hasNext();) {
                    ExamPeriodPlacement period = f.next();
                    if (exam.findBestAvailableRooms(getAssignment(), period)!=null) hasValue = true;
                }
                if (!hasValue) {
                    iProgress.error("Exam "+getExamLabel(exam)+" has no assignment available.");
                    continue;
                }
            }
        }
    }
    
    public void loadRoomAvailability(RoomAvailabilityInterface availability) {
        Set periods = org.unitime.timetable.model.ExamPeriod.findAll(iSessionId, iExamTypeId);
        Date[] bounds = org.unitime.timetable.model.ExamPeriod.getBounds(new SessionDAO().get(iSessionId), iExamTypeId);
        ExamType type = ExamTypeDAO.getInstance().get(iExamTypeId);
        String exclude = (type.getType() == ExamType.sExamTypeFinal ? RoomAvailabilityInterface.sFinalExamType : RoomAvailabilityInterface.sMidtermExamType);
        roomAvailabilityActivate(availability, bounds[0],bounds[1],exclude);
        iProgress.setPhase("Loading room availability...", iAllRooms.size());
        for (Iterator i=iAllRooms.iterator();i.hasNext();) {
            iProgress.incProgress();
            Location location = (Location)i.next();
            ExamRoom roomEx = iRooms.get(location.getUniqueId());
            if (roomEx==null) continue;
            Collection<TimeBlock> times = getRoomAvailability(availability, location, bounds[0], bounds[1], exclude);
            if (times==null) continue;
            for (TimeBlock time : times) {
                for (Iterator j=periods.iterator();j.hasNext();) {
                    org.unitime.timetable.model.ExamPeriod period = (org.unitime.timetable.model.ExamPeriod)j.next();
                    ExamPeriod periodEx = iPeriods.get(period.getUniqueId());
                    if (periodEx!=null && period.overlap(time)) roomEx.setAvailable(periodEx, false);
                }
            }
        }
    }
    
    public Collection<TimeBlock> getRoomAvailability(RoomAvailabilityInterface availability, Location location, Date startTime, Date endTime, String exclude) {
        Collection<TimeBlock> ret = null;
        String ts = null;
        try {
        	ret = availability.getRoomAvailability(location, startTime, endTime, exclude);
        	if (!iRoomAvailabilityTimeStampIsSet) ts = availability.getTimeStamp(startTime, endTime, exclude);
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            iProgress.warn("Unable to access room availability service, reason:"+e.getMessage());
        } 
        if (!iRoomAvailabilityTimeStampIsSet) {
            iRoomAvailabilityTimeStampIsSet = true;
            if (ts!=null) {
                getModel().getProperties().setProperty("RoomAvailability.TimeStamp", ts);
                iProgress.info("Using room availability that was updated on "+ts+".");
            } else {
                iProgress.error("Room availability is not available.");
            }
        }
        return ret;
    }
    
    public void roomAvailabilityActivate(RoomAvailabilityInterface availability, Date startTime, Date endTime, String exclude) {
        try {
        	availability.activate(new SessionDAO().get(iSessionId), startTime, endTime, exclude,
        			"true".equals(ApplicationProperties.getProperty("tmtbl.room.availability.solver.waitForSync","true")));
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            iProgress.warn("Unable to access room availability service, reason:"+e.getMessage());
        } 
    }
    
    private boolean sameOwners(Exam x1, Exam x2) {
        if (x1.getOwners().isEmpty() || x1.getOwners().size()!=x2.getOwners().size()) return false;
        owners: for (ExamOwner o1: x1.getOwners()) {
            org.unitime.timetable.model.ExamOwner w1 = iOwners.get(o1.getId());
            for (ExamOwner o2: x2.getOwners()) {
                org.unitime.timetable.model.ExamOwner w2 = iOwners.get(o2.getId());
                if (w1.getOwnerType().equals(w2.getOwnerType()) && w1.getOwnerId().equals(w2.getOwnerId())) continue owners; 
            }
            return false;
        }
        return true;
    }
    
    public void makeupSameRoomConstraints() {
        iProgress.setPhase("Posting same rooms...", getModel().variables().size());
        long dc = 0;
        for (Exam first: getModel().variables()) {
            iProgress.incProgress();
            for (Exam second: getModel().variables()) {
                if (first.getId()>=second.getId() || !sameOwners(first,second)) continue;
                iProgress.debug("Posting same room constraint between "+first.getName()+" and "+second.getName());
                ExamDistributionConstraint constraint = new ExamDistributionConstraint(--dc, ExamDistributionConstraint.sDistSameRoom, false, 4);
                constraint.addVariable(first);
                constraint.addVariable(second);
                getModel().addConstraint(constraint);
                getModel().getDistributionConstraints().add(constraint);
            }
        }
    }
}
