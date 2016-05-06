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
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;

import org.cpsolver.exam.criteria.StudentBackToBackConflicts;
import org.cpsolver.exam.criteria.StudentDistanceBackToBackConflicts;
import org.cpsolver.exam.model.Exam;
import org.cpsolver.exam.model.ExamDistributionConstraint;
import org.cpsolver.exam.model.ExamInstructor;
import org.cpsolver.exam.model.ExamPlacement;
import org.cpsolver.exam.model.ExamStudent;
import org.cpsolver.ifs.assignment.Assignment;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.solver.exam.ExamModel;
import org.unitime.timetable.solver.exam.ExamResourceUnavailability;


/**
 * @author Tomas Muller
 */
public class ExamAssignmentInfo extends ExamAssignment implements Serializable  {
	private static final long serialVersionUID = 6610082675208799753L;
	private TreeSet<DirectConflict> iDirects = new TreeSet();
    private TreeSet<BackToBackConflict> iBackToBacks = new TreeSet();
    private TreeSet<MoreThanTwoADayConflict> iMoreThanTwoADays = new TreeSet();
    private TreeSet<DirectConflict> iInstructorDirects = new TreeSet();
    private TreeSet<BackToBackConflict> iInstructorBackToBacks = new TreeSet();
    private TreeSet<MoreThanTwoADayConflict> iInstructorMoreThanTwoADays = new TreeSet();
    private TreeSet<DistributionConflict> iDistributions = new TreeSet();
    
    public ExamAssignmentInfo(ExamPlacement placement, Assignment<Exam, ExamPlacement> assignment) {
        this((Exam)placement.variable(),placement, assignment);
    }

    public ExamAssignmentInfo(Exam exam, ExamPlacement placement, Assignment<Exam, ExamPlacement> assignment) {
        super(exam, placement, assignment);
        if (placement!=null) {
            ExamModel model = (ExamModel)exam.getModel();
            Hashtable<Exam,DirectConflict> directs = new Hashtable();
            for (ExamStudent student: exam.getStudents()) {
                for (Iterator i=student.getExams(assignment, placement.getPeriod()).iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    DirectConflict dc = directs.get(other);
                    if (dc==null) {
                        dc = new DirectConflict(new ExamAssignment(assignment.getValue(other), assignment));
                        directs.put(other, dc);
                    } else dc.incNrStudents();
                    dc.getStudents().add(student.getId());
                }
            }
            iDirects.addAll(directs.values());
            double btbDist = ((StudentDistanceBackToBackConflicts)model.getCriterion(StudentDistanceBackToBackConflicts.class)).getBackToBackDistance();
            boolean dayBreakBackToBack = ((StudentBackToBackConflicts)model.getCriterion(StudentBackToBackConflicts.class)).isDayBreakBackToBack();
            Hashtable<Exam,BackToBackConflict> backToBacks = new Hashtable();
            for (ExamStudent student: exam.getStudents()) {
                if (placement.getPeriod().prev()!=null) {
                    if (dayBreakBackToBack || placement.getPeriod().prev().getDay()==placement.getPeriod().getDay()) {
                        Set exams = student.getExams(assignment, placement.getPeriod().prev());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            if (other.equals(exam)) continue;
                            double distance = placement.getDistanceInMeters(assignment.getValue(other));
                            BackToBackConflict btb = backToBacks.get(other);
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment(assignment.getValue(other), assignment),
                                        (btbDist<0?false:distance>btbDist), distance);
                                backToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(student.getId());
                        }
                    }
                }
                if (placement.getPeriod().next()!=null) {
                    if (dayBreakBackToBack || placement.getPeriod().next().getDay()==placement.getPeriod().getDay()) {
                        Set exams = student.getExams(assignment, placement.getPeriod().next());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            if (other.equals(exam)) continue;
                            BackToBackConflict btb = backToBacks.get(other);
                            double distance = placement.getDistanceInMeters(assignment.getValue(other));
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment(assignment.getValue(other), assignment),
                                        (btbDist<0?false:distance>btbDist), distance);
                                backToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(student.getId());
                        }
                    }
                }
            }
            iBackToBacks.addAll(backToBacks.values());
            Hashtable<String,MoreThanTwoADayConflict> m2ds = new Hashtable();
            for (ExamStudent student: exam.getStudents()) {
                Set exams = student.getExamsADay(assignment, placement.getPeriod());
                int nrExams = exams.size() + (exams.contains(exam)?0:1);
                if (nrExams<=2) continue;
                TreeSet examIds = new TreeSet();
                TreeSet otherExams = new TreeSet();
                for (Iterator i=exams.iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    examIds.add(other.getId());
                    otherExams.add(new ExamAssignment(assignment.getValue(other), assignment));
                }
                MoreThanTwoADayConflict m2d = m2ds.get(examIds.toString());
                if (m2d==null) {
                    m2d = new MoreThanTwoADayConflict(otherExams);
                    m2ds.put(examIds.toString(), m2d);
                } else m2d.incNrStudents();
                m2d.getStudents().add(student.getId());
            }
            iMoreThanTwoADays.addAll(m2ds.values());

            Hashtable<Exam,DirectConflict> idirects = new Hashtable();
            for (ExamInstructor instructor: exam.getInstructors()) {
                for (Iterator i=instructor.getExams(assignment, placement.getPeriod()).iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    DirectConflict dc = idirects.get(other);
                    if (dc==null) {
                        dc = new DirectConflict(new ExamAssignment(assignment.getValue(other), assignment));
                        idirects.put(other, dc);
                    } else dc.incNrStudents();
                    dc.getStudents().add(instructor.getId());
                }
            }
            iInstructorDirects.addAll(idirects.values());

            Hashtable<Exam,BackToBackConflict> ibackToBacks = new Hashtable();
            for (ExamInstructor instructor: exam.getInstructors()) {
                if (placement.getPeriod().prev()!=null) {
                    if (dayBreakBackToBack || placement.getPeriod().prev().getDay()==placement.getPeriod().getDay()) {
                        Set exams = instructor.getExams(assignment, placement.getPeriod().prev());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            if (other.equals(exam)) continue;
                            double distance = placement.getDistanceInMeters(assignment.getValue(other));
                            BackToBackConflict btb = ibackToBacks.get(other);
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment(assignment.getValue(other), assignment),
                                        (btbDist<0?false:distance>btbDist), distance);
                                ibackToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(instructor.getId());
                        }
                    }
                }
                if (placement.getPeriod().next()!=null) {
                    if (dayBreakBackToBack || placement.getPeriod().next().getDay()==placement.getPeriod().getDay()) {
                        Set exams = instructor.getExams(assignment, placement.getPeriod().next());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            if (other.equals(exam)) continue;
                            BackToBackConflict btb = ibackToBacks.get(other);
                            double distance = placement.getDistanceInMeters(assignment.getValue(other));
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment(assignment.getValue(other), assignment),
                                        (btbDist<0?false:distance>btbDist), distance);
                                ibackToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(instructor.getId());
                        }
                    }
                }
            }
            iInstructorBackToBacks.addAll(ibackToBacks.values());
            Hashtable<String,MoreThanTwoADayConflict> im2ds = new Hashtable();
            for (ExamInstructor instructor: exam.getInstructors()) {
                Set exams = instructor.getExamsADay(assignment, placement.getPeriod());
                int nrExams = exams.size() + (exams.contains(exam)?0:1);
                if (nrExams<=2) continue;
                TreeSet examIds = new TreeSet();
                TreeSet otherExams = new TreeSet();
                for (Iterator i=exams.iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    examIds.add(other.getId());
                    otherExams.add(new ExamAssignment(assignment.getValue(other), assignment));
                }
                MoreThanTwoADayConflict m2d = im2ds.get(examIds.toString());
                if (m2d==null) {
                    m2d = new MoreThanTwoADayConflict(otherExams);
                    im2ds.put(examIds.toString(), m2d);
                } else m2d.incNrStudents();
                m2d.getStudents().add(instructor.getId());
            }
            iInstructorMoreThanTwoADays.addAll(im2ds.values());
            computeUnavailablility(exam, model.getUnavailabilities(placement.getPeriod()));
            for (ExamDistributionConstraint dc: exam.getDistributionConstraints()) {
                if (dc.isHard()) {
                    if (dc.inConflict(assignment, placement))
                        iDistributions.add(new DistributionConflict(dc,exam,assignment));
                } else {
                    if (!dc.isSatisfied(assignment, placement))
                        iDistributions.add(new DistributionConflict(dc,exam,assignment));
                }
            }
        }
    }
    
    public ExamAssignmentInfo(org.unitime.timetable.model.Exam exam) {
        this(exam, ApplicationProperty.ExaminationCacheConflicts.isTrue());
    }
    
    public ExamAssignmentInfo(org.unitime.timetable.model.Exam exam, Hashtable<Long,Set<Long>> owner2students, Hashtable<Long,Hashtable<Long,Set<Long>>> onwer2course2students, 
    		Hashtable<Long, Set<org.unitime.timetable.model.Exam>> studentExams, Hashtable<Long, Set<Meeting>> period2meetings, Parameters p) {
        super(exam, owner2students, onwer2course2students);
        Hashtable<Long,Set<org.unitime.timetable.model.Exam>> examStudents = new Hashtable();
        for (ExamSectionInfo section: getSections())
            for (Long studentId : section.getStudentIds())
                examStudents.put(studentId, studentExams.get(studentId));
        generateConflicts(exam, examStudents, null, period2meetings, p, owner2students, onwer2course2students);
    }
    
    public ExamAssignmentInfo(org.unitime.timetable.model.Exam exam, boolean useCache) {
        super(exam);
        if (!useCache) {
            generateConflicts(exam, exam.getStudentExams(), null); 
            return;
        }
        if (exam.getConflicts()!=null && !exam.getConflicts().isEmpty()) {
            for (Iterator i=exam.getConflicts().iterator();i.hasNext();) {
                ExamConflict conf = (ExamConflict)i.next();
                if (conf.isDirectConflict()) {
                    ExamAssignment other = null;
                    for (Iterator j=conf.getExams().iterator();j.hasNext();) {
                        org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)j.next();
                        if (x.equals(exam)) continue;
                        if (x.getAssignedPeriod()!=null) other = new ExamAssignment(x);
                    }
                    if (conf.getNrStudents()>0) {
                        iDirects.add(new DirectConflict(other, conf, true));
                        iNrDirectConflicts += conf.getNrStudents();
                    }
                    if (conf.getNrInstructors()>0) {
                        iInstructorDirects.add(new DirectConflict(other, conf, false));
                        iNrInstructorDirectConflicts += conf.getNrInstructors();
                    }
                } else if (conf.isBackToBackConflict()) {
                    ExamAssignment other = null;
                    for (Iterator j=conf.getExams().iterator();j.hasNext();) {
                        org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)j.next();
                        if (x.equals(exam)) continue;
                        if (x.getAssignedPeriod()!=null) other = new ExamAssignment(x);
                    }
                    if (other==null) continue;
                    if (conf.getNrStudents()>0) {
                        iBackToBacks.add(new BackToBackConflict(other, conf, true));
                        iNrBackToBackConflicts += conf.getNrStudents();
                        if (conf.isDistanceBackToBackConflict()) iNrDistanceBackToBackConflicts += conf.getNrStudents();
                    }
                    if (conf.getNrInstructors()>0) {
                        iInstructorBackToBacks.add(new BackToBackConflict(other, conf, false));
                        iNrInstructorBackToBackConflicts += conf.getNrInstructors();
                        if (conf.isDistanceBackToBackConflict()) iNrInstructorDistanceBackToBackConflicts += conf.getNrInstructors();
                    }
                } else if (conf.isMoreThanTwoADayConflict()) {
                    TreeSet other = new TreeSet();
                    for (Iterator j=conf.getExams().iterator();j.hasNext();) {
                        org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)j.next();
                        if (x.equals(exam)) continue;
                        if (x.getAssignedPeriod()!=null) other.add(new ExamAssignment(x));
                    }
                    if (other.size()<2) continue;
                    if (conf.getNrStudents()>0) {
                        iMoreThanTwoADays.add(new MoreThanTwoADayConflict(other, conf, true));
                        iNrMoreThanTwoADayConflicts += conf.getNrStudents();
                    }
                    if (conf.getNrInstructors()>0) {
                        iInstructorMoreThanTwoADays.add(new MoreThanTwoADayConflict(other, conf, false));
                        iNrInstructorMoreThanTwoADayConflicts += conf.getNrInstructors();
                    }
                }
            }
        }
        for (Iterator i=exam.getDistributionObjects().iterator();i.hasNext();) {
            DistributionObject dObj = (DistributionObject)i.next();
            DistributionPref pref = dObj.getDistributionPref();
            if (!check(pref, exam, getPeriod(), getRooms(), null))
                iDistributions.add(new DistributionConflict(pref, exam));
        }
        if (exam.getAssignedPeriod()!=null && ApplicationProperty.ExaminationConsiderEventConflicts.isTrue(exam.getExamType().getReference())) {
            computeUnavailablility(exam, exam.getAssignedPeriod().getUniqueId());
            for (Iterator i=exam.getInstructors().iterator();i.hasNext();)
                computeUnavailablility((DepartmentalInstructor)i.next(), exam.getAssignedPeriod());
        }
    }
    
    private void computeUnavailablility(Exam exam, Vector<ExamResourceUnavailability> unavailabilities) {
        if (unavailabilities==null || unavailabilities.isEmpty()) return;
        for (ExamResourceUnavailability unavailability : unavailabilities) {
            Vector<Long> commonStudents = new Vector();
            for (ExamStudent student: exam.getStudents()) {
                if (unavailability.getStudentIds().contains(student.getId())) commonStudents.add(student.getId());
            }
            if (!commonStudents.isEmpty())
                iDirects.add(new DirectConflict(unavailability, commonStudents));
            Vector<Long> commonInstructors = new Vector();
            for (ExamInstructor instructor: exam.getInstructors()) {
                if (unavailability.getInstructorIds().contains(instructor.getId())) commonInstructors.add(instructor.getId());
            }
            if (!commonInstructors.isEmpty())
                iInstructorDirects.add(new DirectConflict(unavailability, commonInstructors));
        }
    }
    
    /*
    private void computeUnavailablility(Hashtable<Assignment, Set<Long>> studentAssignments, ExamPeriod period) {
        for (Map.Entry<Assignment, Set<Long>> entry : studentAssignments.entrySet()) {
            if (!period.overlap(entry.getKey())) continue;
            iDirects.add(new DirectConflict(entry.getKey(), entry.getValue()));
        }
    }
    */
    
    private void computeUnavailablility(org.unitime.timetable.model.Exam exam, Long periodId, Hashtable<Long, Set<Meeting>> period2meetings) {
        if (period2meetings==null) {
            computeUnavailablility(exam, periodId);
        } else {
            Set<Meeting> meetings = period2meetings.get(periodId);
            if (meetings!=null) {
                meetings: for (Meeting meeting: meetings) {
                    for (Iterator i=iDirects.iterator();i.hasNext();) {
                        DirectConflict dc = (DirectConflict)i.next();
                        if (meeting.getEvent().getUniqueId().equals(dc.getOtherEventId())) {
                            dc.addMeeting(meeting);
                            continue meetings;
                        }
                    }
                    HashSet<Long> students = new HashSet();
                    for (Iterator i=meeting.getEvent().getStudentIds().iterator();i.hasNext();) {
                        Long studentId = (Long)i.next();
                        for (ExamSectionInfo section: getSections()) 
                            if (section.getStudentIds().contains(studentId)) {
                                students.add(studentId); break;
                            }
                    }
                    iDirects.add(new DirectConflict(meeting, students));
                }
            }
        }
    }

    private void computeUnavailablility(org.unitime.timetable.model.Exam exam, Long periodId) {
        meetings: for (Map.Entry<Meeting, Set<Long>> entry : exam.getOverlappingStudentMeetings(periodId).entrySet()) {
            for (Iterator i=iDirects.iterator();i.hasNext();) {
                DirectConflict dc = (DirectConflict)i.next();
                if (entry.getKey().getEvent().getUniqueId().equals(dc.getOtherEventId())) {
                    dc.addMeeting(entry.getKey());
                    continue meetings;
                }
            }
            iDirects.add(new DirectConflict(entry.getKey(), entry.getValue()));
        }
        meetings: for (Map.Entry<Meeting, Set<Long>> entry : ExamPeriodDAO.getInstance().get(periodId).findOverlappingCourseMeetingsWithReqAttendence(getStudentIds()).entrySet()) {
            for (Iterator i=iDirects.iterator();i.hasNext();) {
                DirectConflict dc = (DirectConflict)i.next();
                if (entry.getKey().getEvent().getUniqueId().equals(dc.getOtherEventId())) {
                    dc.addMeeting(entry.getKey());
                    continue meetings;
                }
            }
            iDirects.add(new DirectConflict(entry.getKey(), entry.getValue()));
        }
    }
    
    private void computeUnavailablility(DepartmentalInstructor instructor, ExamPeriod period, Hashtable<Long, Set<Meeting>> period2meetings) {
        if (period2meetings==null) {
            computeUnavailablility(instructor, period);
        } else {
            Set<Meeting> meetings = period2meetings.get(period.getUniqueId());
            if (meetings!=null) {
                meetings: for (Meeting meeting: meetings) {
                    if (!(meeting.getEvent() instanceof ClassEvent)) continue;
                    Class_ clazz = ((ClassEvent)meeting.getEvent()).getClazz();
                    for (Iterator i=clazz.getClassInstructors().iterator();i.hasNext();) {
                        ClassInstructor ci = (ClassInstructor)i.next();
                        if (ci.isLead() && (ci.getInstructor().getUniqueId().equals(instructor.getUniqueId()) ||
                           (ci.getInstructor().getExternalUniqueId()!=null && ci.getInstructor().getExternalUniqueId().equals(instructor.getExternalUniqueId())))) {
                            for (Iterator j=iInstructorDirects.iterator();j.hasNext();) {
                                DirectConflict dc = (DirectConflict)j.next();
                                if (meeting.getEvent().getUniqueId().equals(dc.getOtherEventId())) {
                                    dc.incNrStudents();
                                    dc.getStudents().add(instructor.getUniqueId());
                                    dc.addMeeting(meeting);
                                    continue meetings;
                                }
                            }
                            DirectConflict dc = new DirectConflict(meeting);
                            dc.getStudents().add(instructor.getUniqueId());
                            iInstructorDirects.add(dc);
                            break;
                        }
                    }
                }
            }
        }

    }
    
    private void computeUnavailablility(DepartmentalInstructor instructor, ExamPeriod period) {
        for (Iterator j=instructor.getClasses().iterator();j.hasNext();) {
            ClassInstructor ci = (ClassInstructor)j.next();
            if (!ci.isLead()) continue;
            meetings: for (Iterator k=period.findOverlappingClassMeetings(ci.getClassInstructing().getUniqueId()).iterator();k.hasNext();) {
                Meeting meeting = (Meeting)k.next();
                for (Iterator i=iInstructorDirects.iterator();i.hasNext();) {
                    DirectConflict dc = (DirectConflict)i.next();
                    if (meeting.getEvent().getUniqueId().equals(dc.getOtherEventId())) {
                        dc.incNrStudents();
                        dc.getStudents().add(instructor.getUniqueId());
                        dc.addMeeting(meeting);
                        continue meetings;
                    }
                }
                DirectConflict dc = new DirectConflict(meeting);
                dc.getStudents().add(instructor.getUniqueId());
                iInstructorDirects.add(dc);
            }
        }
    }

    public boolean check(DistributionPref pref, org.unitime.timetable.model.Exam exam, ExamPeriod assignedPeriod, Collection<ExamRoomInfo> assignedRooms, Hashtable<Long,ExamAssignment> table) {
        if (PreferenceLevel.sNeutral.equals(pref.getPrefLevel().getPrefProlog())) return true;
        boolean positive = 
            PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog()) ||
            PreferenceLevel.sStronglyPreferred.equals(pref.getPrefLevel().getPrefProlog()) ||
            PreferenceLevel.sPreferred.equals(pref.getPrefLevel().getPrefProlog());
        if ("EX_SAME_PER".equals(pref.getDistributionType().getReference())) {
            if (positive) { //same period
                ExamPeriod period = null;
                for (Iterator i=pref.getDistributionObjects().iterator();i.hasNext();) {
                    org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)((DistributionObject)i.next()).getPrefGroup();
                    ExamPeriod p = (x.equals(exam)?assignedPeriod:getAssignedPeriod(x,table));
                    if (p==null) continue;
                    if (period==null) period = p;
                    else if (!period.equals(p)) return false;
                }
                return true;
            } else { //different period
                HashSet periods = new HashSet();
                for (Iterator i=pref.getDistributionObjects().iterator();i.hasNext();) {
                    org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)((DistributionObject)i.next()).getPrefGroup();
                    ExamPeriod p = (x.equals(exam)?assignedPeriod:getAssignedPeriod(x,table));
                    if (p==null) continue;
                    if (!periods.add(p)) return false;
                }
                return true;
            }
        } else if ("EX_PRECEDENCE".equals(pref.getDistributionType().getReference())) {
            TreeSet distObjects = new TreeSet(
                    positive?new Comparator<DistributionObject>() {
                        public int compare(DistributionObject d1, DistributionObject d2) {
                            return d1.getSequenceNumber().compareTo(d2.getSequenceNumber());
                        }
                    }:new Comparator<DistributionObject>() {
                        public int compare(DistributionObject d1, DistributionObject d2) {
                            return d2.getSequenceNumber().compareTo(d1.getSequenceNumber());
                        }
                    });
            distObjects.addAll(pref.getDistributionObjects());
            ExamPeriod prev = null;
            for (Iterator i=distObjects.iterator();i.hasNext();) {
                org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)((DistributionObject)i.next()).getPrefGroup();
                ExamPeriod p = (x.equals(exam)?assignedPeriod:getAssignedPeriod(x,table));
                if (p==null) continue;
                if (prev!=null && prev.compareTo(p)>=0) return false;
                prev = p;
            }
            return true;
        } else if ("EX_SAME_ROOM".equals(pref.getDistributionType().getReference())) {
            if (positive) { //same room
                Collection<ExamRoomInfo> rooms = null;
                for (Iterator i=pref.getDistributionObjects().iterator();i.hasNext();) {
                    org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)((DistributionObject)i.next()).getPrefGroup();
                    Collection<ExamRoomInfo> r = (x.equals(exam)?assignedRooms:getAssignedRooms(x, table));
                    if (r==null) continue;
                    if (rooms==null) rooms = r;
                    else if (!rooms.containsAll(r) && !r.containsAll(rooms)) return false;
                }
                return true;
            } else { //different room
                Collection<ExamRoomInfo> allRooms = new HashSet();
                for (Iterator i=pref.getDistributionObjects().iterator();i.hasNext();) {
                    org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)((DistributionObject)i.next()).getPrefGroup();
                    Collection<ExamRoomInfo> r = (x.equals(exam)?assignedRooms:getAssignedRooms(x, table));
                    if (r==null) continue;
                    for (ExamRoomInfo room : r) {
                        if (!allRooms.add(room)) return false;
                    }
                }
                return true;
            }
        } else if ("EX_SHARE_ROOM".equals(pref.getDistributionType().getReference())) {
        	return true;
        }
        return false;
    }
    
    public static ExamPeriod getAssignedPeriod(org.unitime.timetable.model.Exam exam, Hashtable<Long, ExamAssignment> table) {
        ExamAssignment assignment = (table==null?null:table.get(exam.getUniqueId()));
        return (assignment==null?exam.getAssignedPeriod():assignment.getPeriod());
    }
    
    public static TreeSet<ExamRoomInfo> getAssignedRooms(org.unitime.timetable.model.Exam exam, Hashtable<Long, ExamAssignment> table) {
        ExamAssignment assignment = (table==null?null:table.get(exam.getUniqueId()));
        if (assignment!=null) return assignment.getRooms();
        TreeSet<ExamRoomInfo> rooms = new TreeSet();
        for (Iterator i=exam.getAssignedRooms().iterator();i.hasNext();) {
            Location location = (Location)i.next();
            rooms.add(new ExamRoomInfo(location,0));
        }
        return rooms;
    }
    
    public static ExamAssignment getAssignment(org.unitime.timetable.model.Exam exam, Hashtable<Long, ExamAssignment> table, Hashtable<Long, Set<Long>> owner2students, Hashtable<Long,Hashtable<Long,Set<Long>>> onwer2course2students) {
        ExamAssignment assignment = (table==null?null:table.get(exam.getUniqueId()));
        return (assignment==null?new ExamAssignment(exam, owner2students, onwer2course2students):assignment);
    }

    public ExamAssignmentInfo(org.unitime.timetable.model.Exam exam, ExamPeriod period, Collection<ExamRoomInfo> rooms) throws Exception {
        this(exam, period, rooms, (period==null?null:exam.getStudentExams()), null);
    }
    
    public ExamAssignmentInfo(org.unitime.timetable.model.Exam exam, ExamPeriod period, Collection<ExamRoomInfo> rooms, Hashtable<Long, ExamAssignment> table) throws Exception {
        this(exam, period, rooms, exam.getStudentExams(), table);
    }
    
    
    public ExamAssignmentInfo(org.unitime.timetable.model.Exam exam, ExamPeriod period, Collection<ExamRoomInfo> rooms, Hashtable<Long, Set<org.unitime.timetable.model.Exam>> examStudents, Hashtable<Long, ExamAssignment> table) throws Exception {
        super(exam, period, rooms);
        if (period!=null) generateConflicts(exam, examStudents, table);
    }
   
    public ExamAssignmentInfo(org.unitime.timetable.model.Exam exam, Hashtable<Long, ExamAssignment> table) {
        super(exam);
        generateConflicts(exam, exam.getStudentExams(), table);
    }
    
    public void generateConflicts(org.unitime.timetable.model.Exam exam, Hashtable<Long, Set<org.unitime.timetable.model.Exam>> examStudents, Hashtable<Long, ExamAssignment> table) {
        generateConflicts(exam, examStudents, table, null, new Parameters(exam.getSession().getUniqueId(), exam.getExamType().getUniqueId()), null, null);
    }
    
    public void generateConflicts(org.unitime.timetable.model.Exam exam, Hashtable<Long, Set<org.unitime.timetable.model.Exam>> examStudents, Hashtable<Long, ExamAssignment> table, Hashtable<Long, Set<Meeting>> period2meetings, Parameters p,
    		Hashtable<Long, Set<Long>> owner2students, Hashtable<Long,Hashtable<Long,Set<Long>>> onwer2course2students) {
        if (getPeriod()==null) return;
        
        Hashtable<org.unitime.timetable.model.Exam,DirectConflict> directs = new Hashtable();
        Hashtable<org.unitime.timetable.model.Exam,BackToBackConflict> backToBacks = new Hashtable();
        Hashtable<String,MoreThanTwoADayConflict> m2ds = new Hashtable();
        for (Entry<Long,Set<org.unitime.timetable.model.Exam>> studentExams : examStudents.entrySet()) {
            TreeSet sameDateExams = new TreeSet();
            for (org.unitime.timetable.model.Exam other : studentExams.getValue()) {
                if (other.equals(getExam())) continue;
                ExamPeriod otherPeriod = getAssignedPeriod(other, table);
                if (otherPeriod==null) continue;
                if (getPeriod().equals(otherPeriod)) { //direct conflict
                    DirectConflict dc = directs.get(other);
                    if (dc==null) {
                        dc = new DirectConflict(getAssignment(other, table, owner2students, onwer2course2students));
                        directs.put(other, dc);
                    } else dc.incNrStudents();
                    dc.getStudents().add(studentExams.getKey());
                    iNrDirectConflicts++;
                } else if (p.isBackToBack(getPeriod(),otherPeriod)) {
                    BackToBackConflict btb = backToBacks.get(other);
                    double distance = Location.getDistance(getRooms(), getAssignedRooms(other, table));
                    if (btb==null) {
                        btb = new BackToBackConflict(getAssignment(other, table, owner2students, onwer2course2students), (p.getBackToBackDistance()<0?false:distance>p.getBackToBackDistance()), distance);
                        backToBacks.put(other, btb);
                    } else btb.incNrStudents();
                    btb.getStudents().add(studentExams.getKey());
                    iNrBackToBackConflicts++;
                    if (btb.isDistance()) iNrDistanceBackToBackConflicts++;
                }
                if (getPeriod().getDateOffset().equals(otherPeriod.getDateOffset()))
                    sameDateExams.add(other);
            }
            if (sameDateExams.size()>=2) {
                TreeSet examIds = new TreeSet();
                TreeSet otherExams = new TreeSet();
                for (Iterator j=sameDateExams.iterator();j.hasNext();) {
                    org.unitime.timetable.model.Exam other = (org.unitime.timetable.model.Exam)j.next();
                    examIds.add(other.getUniqueId());
                    otherExams.add(getAssignment(other, table, owner2students, onwer2course2students));
                }
                MoreThanTwoADayConflict m2d = m2ds.get(examIds.toString());
                if (m2d==null) {
                    m2d = new MoreThanTwoADayConflict(otherExams);
                    m2ds.put(examIds.toString(), m2d);
                } else m2d.incNrStudents();
                iNrMoreThanTwoADayConflicts++;
                m2d.getStudents().add(studentExams.getKey());
            }
        }
        iDirects.addAll(directs.values());
        iBackToBacks.addAll(backToBacks.values());
        iMoreThanTwoADays.addAll(m2ds.values());
        
        if (ApplicationProperty.ExaminationConsiderEventConflicts.isTrue(exam.getExamType().getReference()))
            computeUnavailablility(exam,getPeriodId(),period2meetings);
            
        Hashtable<org.unitime.timetable.model.Exam,DirectConflict> idirects = new Hashtable();
        Hashtable<org.unitime.timetable.model.Exam,BackToBackConflict> ibackToBacks = new Hashtable();
        Hashtable<String,MoreThanTwoADayConflict> im2ds = new Hashtable();
        for (Iterator i=getExam().getInstructors().iterator();i.hasNext();) {
            DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
            TreeSet sameDateExams = new TreeSet();
            for (Iterator j=instructor.getExams(getExam().getExamType()).iterator();j.hasNext();) {
                org.unitime.timetable.model.Exam other = (org.unitime.timetable.model.Exam)j.next();
                if (other.equals(getExam())) continue;
                ExamPeriod otherPeriod = getAssignedPeriod(other, table);
                if (otherPeriod==null) continue;
                if (getPeriod().equals(otherPeriod)) { //direct conflict
                    DirectConflict dc = idirects.get(other);
                    if (dc==null) {
                        dc = new DirectConflict(getAssignment(other, table, owner2students, onwer2course2students));
                        idirects.put(other, dc);
                    } else dc.incNrStudents();
                    iNrInstructorDirectConflicts++;
                    dc.getStudents().add(instructor.getUniqueId());
                } else if (p.isBackToBack(getPeriod(),otherPeriod)) {
                    BackToBackConflict btb = ibackToBacks.get(other);
                    double distance = Location.getDistance(getRooms(), getAssignedRooms(other, table));
                    if (btb==null) {
                        btb = new BackToBackConflict(getAssignment(other, table, owner2students, onwer2course2students), (p.getBackToBackDistance()<0?false:distance>p.getBackToBackDistance()), distance);
                        ibackToBacks.put(other, btb);
                    } else btb.incNrStudents();
                    iNrInstructorBackToBackConflicts++;
                    if (btb.isDistance()) iNrInstructorDistanceBackToBackConflicts++;
                    btb.getStudents().add(instructor.getUniqueId());
                }
                if (getPeriod().getDateOffset().equals(otherPeriod.getDateOffset()))
                    sameDateExams.add(other);
            }
            if (ApplicationProperty.ExaminationConsiderEventConflicts.isTrue(exam.getExamType().getReference()))
                computeUnavailablility(instructor, getPeriod(), period2meetings);
            if (sameDateExams.size()>=2) {
                TreeSet examIds = new TreeSet();
                TreeSet otherExams = new TreeSet();
                for (Iterator j=sameDateExams.iterator();j.hasNext();) {
                    org.unitime.timetable.model.Exam other = (org.unitime.timetable.model.Exam)j.next();
                    examIds.add(other.getUniqueId());
                    otherExams.add(getAssignment(other, table, owner2students, onwer2course2students));
                }
                MoreThanTwoADayConflict m2d = im2ds.get(examIds.toString());
                if (m2d==null) {
                    m2d = new MoreThanTwoADayConflict(otherExams);
                    im2ds.put(examIds.toString(), m2d);
                } else m2d.incNrStudents();
                iNrInstructorMoreThanTwoADayConflicts++;
                m2d.getStudents().add(instructor.getUniqueId());
            }
        }
        iInstructorDirects.addAll(idirects.values());
        iInstructorBackToBacks.addAll(ibackToBacks.values());
        iInstructorMoreThanTwoADays.addAll(im2ds.values());   

        for (Iterator i=getExam().getDistributionObjects().iterator();i.hasNext();) {
            DistributionObject dObj = (DistributionObject)i.next();
            DistributionPref pref = dObj.getDistributionPref();
            if (!check(pref, getExam(), getPeriod(), getRooms(), table))
                iDistributions.add(new DistributionConflict(pref, getExam()));
        }
    }
    
    public TreeSet<DirectConflict> getDirectConflicts() {
        return iDirects;
    }

    public TreeSet<BackToBackConflict> getBackToBackConflicts() {
        return iBackToBacks;
    }
    
    public TreeSet<MoreThanTwoADayConflict> getMoreThanTwoADaysConflicts() {
        return iMoreThanTwoADays;
    }
    
    public int getNrDirectConflicts() {
        int ret = 0;
        for (Iterator i=iDirects.iterator();i.hasNext();) {
            DirectConflict dc = (DirectConflict)i.next();
            ret += dc.getNrStudents();
        }
        return ret;
    }

    public int getNrNotAvailableDirectConflicts() {
        int ret = 0;
        for (Iterator i=iDirects.iterator();i.hasNext();) {
            DirectConflict dc = (DirectConflict)i.next();
            if (dc.getOtherExam()==null) ret += dc.getNrStudents();
        }
        return ret;
    }

    public int getNrBackToBackConflicts() {
        int ret = 0;
        for (Iterator i=iBackToBacks.iterator();i.hasNext();) {
            BackToBackConflict btb = (BackToBackConflict)i.next();
            ret += btb.getNrStudents();
        }
        return ret;
    }
    
    public int getNrDistanceBackToBackConflicts() {
        int ret = 0;
        for (Iterator i=iBackToBacks.iterator();i.hasNext();) {
            BackToBackConflict btb = (BackToBackConflict)i.next();
            if (btb.isDistance())
                ret += btb.getNrStudents();
        }
        return ret;
    }

    public int getNrMoreThanTwoConflicts() {
        int ret = 0;
        for (Iterator i=iMoreThanTwoADays.iterator();i.hasNext();) {
            MoreThanTwoADayConflict m2d = (MoreThanTwoADayConflict)i.next();
            ret += m2d.getNrStudents();
        }
        return ret;
    }
    
    public int getNrDirectConflicts(ExamSectionInfo section) {
        int ret = 0;
        for (Iterator i=iDirects.iterator();i.hasNext();) {
            DirectConflict dc = (DirectConflict)i.next();
            for (Enumeration f=dc.getStudents().elements();f.hasMoreElements();)
                if (section.getStudentIds().contains(f.nextElement())) ret++;
        }
        return ret;
    }

    public int getNrBackToBackConflicts(ExamSectionInfo section) {
        int ret = 0;
        for (Iterator i=iBackToBacks.iterator();i.hasNext();) {
            BackToBackConflict btb = (BackToBackConflict)i.next();
            for (Enumeration f=btb.getStudents().elements();f.hasMoreElements();)
                if (section.getStudentIds().contains(f.nextElement())) ret++;
        }
        return ret;
    }
    
    public int getNrDistanceBackToBackConflicts(ExamSectionInfo section) {
        int ret = 0;
        for (Iterator i=iBackToBacks.iterator();i.hasNext();) {
            BackToBackConflict btb = (BackToBackConflict)i.next();
            if (btb.isDistance())
                for (Enumeration f=btb.getStudents().elements();f.hasMoreElements();)
                    if (section.getStudentIds().contains(f.nextElement())) ret++;
        }
        return ret;
    }

    public int getNrMoreThanTwoConflicts(ExamSectionInfo section) {
        int ret = 0;
        for (Iterator i=iMoreThanTwoADays.iterator();i.hasNext();) {
            MoreThanTwoADayConflict m2d = (MoreThanTwoADayConflict)i.next();
            for (Enumeration f=m2d.getStudents().elements();f.hasMoreElements();)
                if (section.getStudentIds().contains(f.nextElement())) ret++;
        }
        return ret;
    }
    
    public TreeSet<DistributionConflict> getDistributionConflicts() {
        return iDistributions;
    }
    
    public String getDistributionConflictsHtml(String delim) {
        String ret = "";
        for (Iterator i=iDistributions.iterator();i.hasNext();) {
            DistributionConflict dc = (DistributionConflict)i.next();
            if (ret.length()>0) ret+=delim;
            ret+=dc.getTypeHtml();
        }
        return ret;
    }
    
    public String getDistributionConflictsList(String delim) {
        String ret = "";
        for (Iterator i=iDistributions.iterator();i.hasNext();) {
            DistributionConflict dc = (DistributionConflict)i.next();
            if (ret.length()>0) ret+=delim;
            ret+=PreferenceLevel.prolog2abbv(dc.getPreference())+" "+dc.getType();
        }
        return ret;
    }

    public int getNrDistributionConflicts() {
        return iDistributions.size();
    }
    
    public boolean getHasConflicts() {
        return !getDirectConflicts().isEmpty() || !getBackToBackConflicts().isEmpty() || !getMoreThanTwoADaysConflicts().isEmpty();
    }
    
    public String getConflictTable() {
        return getConflictTable(true);
    }
    
    public String getConflictTable(boolean header) {
        String ret = "<table border='0' width='100%' cellspacing='0' cellpadding='3'>";
        if (header) {
            ret += "<tr>";
            ret += "<td><i>Students</i></td>";
            ret += "<td><i>Conflict</i></td>";
            ret += "<td><i>Exam</i></td>";
            ret += "<td><i>Period</i></td>";
            ret += "<td><i>Room</i></td>";
            ret += "</tr>";
        }
        for (Iterator i=getDirectConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        for (Iterator i=getMoreThanTwoADaysConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        for (Iterator i=getBackToBackConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        ret += "</table>";
        return ret;
    }
    
    public String getConflictInfoTable() {
        String ret = "<table border='0' width='100%' cellspacing='0' cellpadding='3'>";
        ret += "<tr>";
        ret += "<td><i>Students</i></td>";
        ret += "<td><i>Conflict</i></td>";
        ret += "<td><i>Exam</i></td>";
        ret += "<td><i>Period</i></td>";
        ret += "<td><i>Room</i></td>";
        ret += "</tr>";
        for (DirectConflict dc : getDirectConflicts())
            ret += dc.toString(true);
        for (MoreThanTwoADayConflict m2d : getMoreThanTwoADaysConflicts())
            ret += m2d.toString(true);
        for (BackToBackConflict btb : getBackToBackConflicts()) 
            ret += btb.toString(true);
        ret += "</table>";
        return ret;
    }
    
    public String getDistributionConflictTable() {
        return getDistributionConflictTable(true);
    }
    
    public String getDistributionConflictTable(boolean header) {
        String ret = "<table border='0' width='100%' cellspacing='0' cellpadding='3'>";
        if (header) {
            ret += "<tr>";
            ret += "<td><i>Preference</i></td>";
            ret += "<td><i>Distribution</i></td>";
            ret += "<td><i>Exam</i></td>";
            ret += "<td><i>Period</i></td>";
            ret += "<td><i>Room</i></td>";
            ret += "</tr>";
        }
        for (Iterator i=getDistributionConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        ret += "</table>";
        return ret;
    }
    
    public String getDistributionInfoConflictTable() {
        String ret = "<table border='0' width='100%' cellspacing='0' cellpadding='3'>";
        ret += "<tr>";
        ret += "<td><i>Preference</i></td>";
        ret += "<td><i>Distribution</i></td>";
        ret += "<td><i>Exam</i></td>";
        ret += "<td><i>Period</i></td>";
        ret += "<td><i>Room</i></td>";
        ret += "</tr>";
        for (DistributionConflict dc : getDistributionConflicts())
            ret += dc.toString(true);
        ret += "</table>";
        return ret;
    }
    
    public TreeSet<DirectConflict> getInstructorDirectConflicts() {
        return iInstructorDirects;
    }

    public TreeSet<BackToBackConflict> getInstructorBackToBackConflicts() {
        return iInstructorBackToBacks;
    }
    
    public TreeSet<MoreThanTwoADayConflict> getInstructorMoreThanTwoADaysConflicts() {
        return iInstructorMoreThanTwoADays;
    }
    
    public int getNrInstructorDirectConflicts() {
        int ret = 0;
        for (Iterator i=iInstructorDirects.iterator();i.hasNext();) {
            DirectConflict dc = (DirectConflict)i.next();
            ret += dc.getNrStudents();
        }
        return ret;
    }

    public int getNrInstructorBackToBackConflicts() {
        int ret = 0;
        for (Iterator i=iInstructorBackToBacks.iterator();i.hasNext();) {
            BackToBackConflict btb = (BackToBackConflict)i.next();
            ret += btb.getNrStudents();
        }
        return ret;
    }
    
    public int getNrInstructorDistanceBackToBackConflicts() {
        int ret = 0;
        for (Iterator i=iInstructorBackToBacks.iterator();i.hasNext();) {
            BackToBackConflict btb = (BackToBackConflict)i.next();
            if (btb.isDistance())
                ret += btb.getNrStudents();
        }
        return ret;
    }

    public int getNrInstructorMoreThanTwoConflicts() {
        int ret = 0;
        for (Iterator i=iInstructorMoreThanTwoADays.iterator();i.hasNext();) {
            MoreThanTwoADayConflict m2d = (MoreThanTwoADayConflict)i.next();
            ret += m2d.getNrStudents();
        }
        return ret;
    }
    
    public int getNrInstructorDirectConflicts(ExamSectionInfo section) {
    	return getNrInstructorDirectConflicts();
    }

    public int getNrInstructorBackToBackConflicts(ExamSectionInfo section) {
        return getNrInstructorBackToBackConflicts();
    }
    
    public int getNrInstructorDistanceBackToBackConflicts(ExamSectionInfo section) {
        return getNrInstructorDistanceBackToBackConflicts();
    }

    public int getNrInstructorMoreThanTwoConflicts(ExamSectionInfo section) {
        return getNrInstructorMoreThanTwoConflicts();
    }

    public boolean getHasInstructorConflicts() {
        return !getInstructorDirectConflicts().isEmpty() || !getInstructorBackToBackConflicts().isEmpty() || !getInstructorMoreThanTwoADaysConflicts().isEmpty();
    }
    
    public String getInstructorConflictTable() {
        return getInstructorConflictTable(true);
    }
    
    public String getInstructorConflictTable(boolean header) {
        String ret = "<table border='0' width='100%' cellspacing='0' cellpadding='3'>";
        if (header) {
            ret += "<tr>";
            ret += "<td><i>Instructors</i></td>";
            ret += "<td><i>Conflict</i></td>";
            ret += "<td><i>Exam</i></td>";
            ret += "<td><i>Period</i></td>";
            ret += "<td><i>Room</i></td>";
            ret += "</tr>";
        }
        for (Iterator i=getInstructorDirectConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        for (Iterator i=getInstructorMoreThanTwoADaysConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        for (Iterator i=getInstructorBackToBackConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        ret += "</table>";
        return ret;
    }
    
    public String getInstructorConflictInfoTable() {
        String ret = "<table border='0' width='100%' cellspacing='0' cellpadding='3'>";
        ret += "<tr>";
        ret += "<td><i>Students</i></td>";
        ret += "<td><i>Conflict</i></td>";
        ret += "<td><i>Exam</i></td>";
        ret += "<td><i>Period</i></td>";
        ret += "<td><i>Room</i></td>";
        ret += "</tr>";
        for (DirectConflict dc : getInstructorDirectConflicts())
            ret += dc.toString(true);
        for (MoreThanTwoADayConflict m2d : getInstructorMoreThanTwoADaysConflicts())
            ret += m2d.toString(true);
        for (BackToBackConflict btb : getInstructorBackToBackConflicts()) 
            ret += btb.toString(true);
        ret += "</table>";
        return ret;
    }

    
    
    public static class DirectConflict implements Serializable, Comparable<DirectConflict> {
		private static final long serialVersionUID = 1300925620564937810L;
		protected ExamAssignment iOtherExam = null;
        protected int iNrStudents = 1;
        protected Vector<Long> iStudents = new Vector();
        protected String iOtherEventName = null;
        protected String iOtherEventTime = null;
        protected String iOtherEventDate = null;
        protected String iOtherEventRoom = null;
        protected int iOtherEventSize = 0;
        protected Long iOtherEventId;
        protected transient Event iOtherEvent = null;
        
        protected DirectConflict(ExamAssignment otherExam) {
            iOtherExam = otherExam;
        }
        protected DirectConflict(ExamAssignment otherExam, ExamConflict conflict, boolean students) {
            iOtherExam = otherExam;
            if (students) {
                iNrStudents = conflict.getStudents().size();
                for (Iterator i=conflict.getStudents().iterator();i.hasNext();) {
                    Student student = (Student)i.next();
                    iStudents.add(student.getUniqueId());
                }
            } else {
                iNrStudents = conflict.getInstructors().size();
                for (Iterator i=conflict.getInstructors().iterator();i.hasNext();) {
                    DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
                    iStudents.add(instructor.getUniqueId());
                }
            }
        }
        protected DirectConflict(Meeting otherMeeting) {
            iOtherEvent = otherMeeting.getEvent();
            iOtherEventSize = otherMeeting.getEvent().getStudentIds().size();
            iOtherEventId = otherMeeting.getEvent().getUniqueId();
            iOtherEventName = otherMeeting.getEvent().getEventName();
            iOtherEventDate = otherMeeting.dateStr();
            iOtherEventTime = otherMeeting.startTime()+" - "+otherMeeting.stopTime();
            iOtherEventRoom = otherMeeting.getRoomLabel();
        }
        protected void addMeeting(Meeting otherMeeting) {
            if (otherMeeting.getLocation()!=null)
                iOtherEventRoom += (iOtherEventRoom!=null && iOtherEventRoom.length()>0?", ":"")+otherMeeting.getRoomLabel();
        }
        protected DirectConflict(Meeting otherMeeting,Collection<Long> studentIds) {
            this(otherMeeting);
            iNrStudents = studentIds.size();
            iStudents.addAll(studentIds);
        }
        protected DirectConflict(ExamResourceUnavailability unavailability, Vector<Long> studentIds) {
            iOtherEventId = unavailability.getId();
            iOtherEventSize = unavailability.getSize();
            iOtherEventName = unavailability.getName();
            iOtherEventTime = unavailability.getTime();
            iOtherEventDate = unavailability.getDate();
            iOtherEventRoom = unavailability.getRoom();
            iNrStudents = studentIds.size();
            iStudents = studentIds;
        }
        protected void incNrStudents() {
            iNrStudents++;
        }
        public int getNrStudents() {
            return iNrStudents;
        }
        public Vector<Long> getStudents() {
            return iStudents;
        }
        public ExamAssignment getOtherExam() {
            return iOtherExam;
        }
        public Long getOtherEventId() {
            return iOtherEventId;
        }
        public Event getOtherEvent() {
            if (iOtherEvent!=null) return iOtherEvent;
            if (iOtherEventId==null) return null;
            iOtherEvent = new EventDAO().get(iOtherEventId);
            return iOtherEvent;
        }
        public String getOtherEventName() {
            return iOtherEventName;
        }
        public String getOtherEventRoom() {
            return iOtherEventRoom;
        }
        public String getOtherEventDate() {
            return iOtherEventDate;
        }
        public String getOtherEventTime() {
            return iOtherEventTime;
        }
        public int getOtherEventSize() {
            return iOtherEventSize;
        }
        public boolean isOtherClass() {
            return getOtherEvent()!=null && getOtherEvent().getEventType()==Event.sEventTypeClass;
        }
        public Class_ getOtherClass() {
            if (!isOtherClass()) return null;
            if (!(iOtherEvent instanceof ClassEvent)) 
                iOtherEvent = new ClassEventDAO().get(getOtherEventId()); //proxy
            return ((ClassEvent)iOtherEvent).getClazz();
        }
        public int compareTo(DirectConflict c) {
            int cmp = -Double.compare(getNrStudents(), c.getNrStudents());
            if (cmp!=0) return cmp;
            if (getOtherExam()==null) {
            	if (c.getOtherExam() == null) {
            		cmp = (getOtherEventName() == null ? "" : getOtherEventName()).compareTo(c.getOtherEventName() == null ? "" : c.getOtherEventName());
            		if (cmp != 0) return cmp;
            		return (getOtherEventId() == null ? new Long(0) : getOtherEventId()).compareTo(c.getOtherEventId() == null ? new Long(0) : c.getOtherEventId());
            	} else return -1;
            }
            if (c.getOtherExam()==null) return 1;
            return getOtherExam().compareTo(c.getOtherExam());
        }
        public String toString() {
            return toString(false);
        }
        public String toString(boolean links) {
            String ret = "";
            if (links && getOtherExam()!=null)
                ret += "<tr onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';\" onmouseout=\"this.style.backgroundColor='transparent';\" onclick=\"document.location='examInfo.do?examId="+getOtherExam().getExamId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
            else
                ret += "<tr onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';\" onmouseout=\"this.style.backgroundColor='transparent';\">";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("P")+";'>";
            ret += String.valueOf(getNrStudents());
            ret += "</td>";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("P")+";'>";
            ret += (getOtherExam()==null?(isOtherClass()?"Class":"Event"):"Direct");
            ret += "</td>";
            if (getOtherExam()==null) {
                if (iOtherEventName!=null) {
                    ret += "<td>"+iOtherEventName+"</td>";
                    ret += "<td>"+iOtherEventDate+" "+iOtherEventTime+"</td>";
                    ret += "<td>"+iOtherEventRoom+"</td>";
                } else {
                    ret += "<td colspan='3'>Student/instructor not available for unknown reason.</td>";
                }
            } else {
                ret += "<td>"+getOtherExam().getExamNameHtml()+"</td>";
                ret += "<td>"+getOtherExam().getPeriodAbbreviationWithPref()+"</td>";
                ret += "<td>"+getOtherExam().getRoomsNameWithPref(", ")+"</td>";
            }
            ret += "</tr>";
            return ret;
        }
    }
    
    public static class BackToBackConflict implements Serializable, Comparable<BackToBackConflict> {
		private static final long serialVersionUID = 4953777429653205613L;
		protected ExamAssignment iOtherExam;
        protected int iNrStudents = 1;
        protected boolean iIsDistance = false; 
        protected Vector<Long> iStudents = new Vector();
        protected double iDistance = 0;
        
        protected BackToBackConflict(ExamAssignment otherExam, boolean isDistance, double distance) {
            iOtherExam = otherExam;
            iIsDistance = isDistance;
            iDistance = distance;
        }
        protected BackToBackConflict(ExamAssignment otherExam, ExamConflict conflict, boolean students) {
            iOtherExam = otherExam;
            if (students) {
                iNrStudents = conflict.getStudents().size();
                for (Iterator i=conflict.getStudents().iterator();i.hasNext();) {
                    Student student = (Student)i.next();
                    iStudents.add(student.getUniqueId());
                }
            } else {
                iNrStudents = conflict.getInstructors().size();
                for (Iterator i=conflict.getInstructors().iterator();i.hasNext();) {
                    DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
                    iStudents.add(instructor.getUniqueId());
                }
            }
            iIsDistance = conflict.isDistanceBackToBackConflict();
            iDistance = conflict.getDistance();
        }
        protected void incNrStudents() {
            iNrStudents++;
        }
        public int getNrStudents() {
            return iNrStudents;
        }
        public boolean isDistance() {
            return iIsDistance;
        }
        public ExamAssignment getOtherExam() {
            return iOtherExam;
        }
        public Vector<Long> getStudents() {
            return iStudents;
        }
        public double getDistance() {
            return iDistance;
        }
        public int compareTo(BackToBackConflict c) {
            int cmp = -Double.compare(getNrStudents(), c.getNrStudents());
            if (cmp!=0) return cmp;
            if (isDistance()!=c.isDistance()) return (isDistance()?-1:1);
            return getOtherExam().compareTo(c.getOtherExam());
        }
        public String toString() {
            return toString(false);
        }
        public String toString(boolean links) {
            String ret = "";
            if (links && getOtherExam()!=null)
                ret += "<tr onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';\" onmouseout=\"this.style.backgroundColor='transparent';\" onclick=\"document.location='examInfo.do?examId="+getOtherExam().getExamId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
            else
                ret += "<tr onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';\" onmouseout=\"this.style.backgroundColor='transparent';\">";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("1")+";'>";
            ret += String.valueOf(getNrStudents());
            ret += "</td>";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("1")+";'>";
            ret += "Back-To-Back";
            if (isDistance()) ret+="<br>("+Math.round(10.0*getDistance())+" m)";
            ret += "</td>";
            ret += "<td>"+getOtherExam().getExamNameHtml()+"</td>";
            ret += "<td>"+getOtherExam().getPeriodAbbreviationWithPref()+"</td>";
            ret += "<td>"+getOtherExam().getRoomsNameWithPref(", ")+"</td>";
            ret += "</tr>";
            return ret;
        }
    }

    public static class MoreThanTwoADayConflict implements Serializable, Comparable<MoreThanTwoADayConflict> {
		private static final long serialVersionUID = -8320516715119699996L;
		protected TreeSet<ExamAssignment> iOtherExams;
        protected int iNrStudents = 1;
        protected Vector<Long> iStudents = new Vector();
        
        protected MoreThanTwoADayConflict(TreeSet<ExamAssignment> otherExams) {
            iOtherExams = otherExams;
        }
        protected MoreThanTwoADayConflict(TreeSet<ExamAssignment> otherExams, ExamConflict conflict, boolean students) {
            iOtherExams = otherExams;
            if (students) {
                iNrStudents = conflict.getStudents().size();
                for (Iterator i=conflict.getStudents().iterator();i.hasNext();) {
                    Student student = (Student)i.next();
                    iStudents.add(student.getUniqueId());
                }
            } else {
                iNrStudents = conflict.getInstructors().size();
                for (Iterator i=conflict.getInstructors().iterator();i.hasNext();) {
                    DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
                    iStudents.add(instructor.getUniqueId());
                }
            }
        }
        protected void incNrStudents() {
            iNrStudents++;
        }
        public int getNrStudents() {
            return iNrStudents;
        }
        public Vector<Long> getStudents() {
            return iStudents;
        }
        public TreeSet<ExamAssignment> getOtherExams() {
            return iOtherExams;
        }
        public int compareTo(MoreThanTwoADayConflict c) {
            int cmp = -Double.compare(getNrStudents(), c.getNrStudents());
            if (cmp!=0) return cmp;
            cmp = -Double.compare(getOtherExams().size(), c.getOtherExams().size());
            if (cmp!=0) return cmp;
            Iterator i1 = getOtherExams().iterator(), i2 = c.getOtherExams().iterator();
            while (i1.hasNext()) {
                ExamAssignment a1 = (ExamAssignment)i1.next();
                ExamAssignment a2 = (ExamAssignment)i2.next();
                if (!a1.equals(a2)) return a1.compareTo(a2);
            }
            return 0;
        }
        public String toString() {
            return toString(false);
        }
        public String toString(boolean links) {
            String ret = "";
            String mouseOver = "";
            String mouseOut = "";
            String id = "";
            for (Iterator i=getOtherExams().iterator();i.hasNext();) {
                ExamAssignment a = (ExamAssignment)i.next();
                id+=a.getExamId(); 
                if (i.hasNext()) id+=":";
            }
            int idx = 0;
            Vector<Long> ids = new Vector();
            for (Iterator i=getOtherExams().iterator();i.hasNext();idx++) {
                ExamAssignment a = (ExamAssignment)i.next();
                ids.add(a.getExamId());
                mouseOver += "document.getElementById('"+id+":"+idx+"').style.backgroundColor='rgb(223,231,242)';";
                if (links)
                    mouseOver += "this.style.cursor='hand';this.style.cursor='pointer';";
                mouseOut += "document.getElementById('"+id+":"+idx+"').style.backgroundColor='transparent';";
            }
            idx = 0;
            if (links)
                ret += "<tr id='"+id+":"+idx+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\" onclick=\"document.location='examInfo.do?examId="+ids.elementAt(idx)+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
            else
                ret += "<tr id='"+id+":"+idx+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\">";
            ret += "<td valign='top' rowspan='"+getOtherExams().size()+"' style='font-weight:bold;color:"+PreferenceLevel.prolog2color("2")+";'>";
            ret += String.valueOf(getNrStudents());
            ret += "</td>";
            ret += "<td valign='top' rowspan='"+getOtherExams().size()+"' style='font-weight:bold;color:"+PreferenceLevel.prolog2color("2")+";'>";
            ret += "&gt;2 A Day";
            ret += "</td>";
            for (Iterator i=getOtherExams().iterator();i.hasNext();idx++) {
                ExamAssignment a = (ExamAssignment)i.next();
                ret += "<td>"+a.getExamNameHtml()+"</td>";
                ret += "<td>"+a.getPeriodAbbreviationWithPref()+"</td>";
                ret += "<td>"+a.getRoomsNameWithPref(", ")+"</td>";
                ret += "</tr>";
                if (i.hasNext()) {
                    if (links)
                        ret += "<tr id='"+id+":"+(1+idx)+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\" onclick=\"document.location='examInfo.do?examId="+ids.elementAt(1+idx)+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
                    else
                        ret += "<tr id='"+id+":"+(1+idx)+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\">";
                }
            }
            return ret;
        }
    }
    
    public static class DistributionConflict implements Serializable, Comparable<DistributionConflict> {
		private static final long serialVersionUID = -1985853750381140103L;
		protected TreeSet<ExamInfo> iOtherExams;
        protected String iPreference;
        protected Long iId;
        protected String iType;
        protected transient DistributionPref iPref = null;
        protected DistributionConflict(Long id, String type, TreeSet<ExamInfo> otherExams, String preference) {
            iId = id;
            iType = type;
            iOtherExams = otherExams;
            iPreference = preference;
        }
        protected DistributionConflict(ExamDistributionConstraint dc, Exam exclude, Assignment<Exam, ExamPlacement> assignment) {
            iId = dc.getId();
            iType = dc.getTypeString();
            iOtherExams = new TreeSet();
            for (Exam exam: dc.variables()) {
                if (exam.equals(exclude)) continue;
                iOtherExams.add(assignment.getValue(exam)==null?new ExamInfo(exam):new ExamAssignment(exam,assignment.getValue(exam),assignment));
            }
            iPreference = (dc.isHard()?"R":dc.getWeight()>=2?"-2":"-1");
        }
        protected DistributionConflict(DistributionPref pref, org.unitime.timetable.model.Exam exclude) {
            iPref = pref;
            iId = pref.getUniqueId();
            iType = pref.getDistributionType().getLabel();
            iOtherExams = new TreeSet();
            for (Iterator i=pref.getDistributionObjects().iterator();i.hasNext();) {
                DistributionObject dObj = (DistributionObject)i.next();
                org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam)dObj.getPrefGroup();
                if (exam.equals(exclude)) continue;
                iOtherExams.add(exam.getAssignedPeriod()==null?new ExamInfo(exam):new ExamAssignment(exam));
            }
            iPreference = pref.getPrefLevel().getPrefProlog(); 
        }
        public Long getId() {
            return iId;
        }
        public String getType() {
            return iType;
        }
        public String getTypeHtml() {
            String title = PreferenceLevel.prolog2string(getPreference())+" "+getType()+" with ";
            for (Iterator i=getOtherExams().iterator();i.hasNext();) {
                ExamInfo a = (ExamInfo)i.next();
                title += a.getExamName();
                if (i.hasNext()) title += " and ";
            }
            return "<span style='font-weight:bold;color:"+PreferenceLevel.prolog2color(getPreference())+";' title='"+title+"'>"+iType+"</span>";
        }
        public String getPreference() {
            return iPreference;
        }
        public TreeSet<ExamInfo> getOtherExams() {
            return iOtherExams;
        }
        public int hashCode() {
            return getId().hashCode();
        }
        public boolean equals(Object o) {
            if (o==null || !(o instanceof DistributionConflict)) return false;
            DistributionConflict c = (DistributionConflict)o;
            return getId().equals(c.getId());
        }
        public int compareTo(DistributionConflict c) {
            Iterator i1 = getOtherExams().iterator(), i2 = c.getOtherExams().iterator();
            while (i1.hasNext()) {
                ExamInfo a1 = (ExamInfo)i1.next();
                ExamInfo a2 = (ExamInfo)i2.next();
                if (!a1.equals(a2)) return a1.compareTo(a2);
            }
            return getId().compareTo(c.getId());
        }
        public String toString() {
            return toString(false);
        }
        public String toString(boolean links) {
            String ret = "";
            String mouseOver = "";
            String mouseOut = "";
            String id = "";
            for (Iterator i=getOtherExams().iterator();i.hasNext();) {
                ExamInfo a = (ExamInfo)i.next();
                id+=a.getExamId(); 
                if (i.hasNext()) id+=":";
            }
            int idx = 0;
            Vector<Long> ids = new Vector();
            for (Iterator i=getOtherExams().iterator();i.hasNext();idx++) {
                ExamInfo a = (ExamInfo)i.next();
                ids.add(a.getExamId());
                mouseOver += "document.getElementById('"+id+":"+idx+"').style.backgroundColor='rgb(223,231,242)';";
                if (links)
                    mouseOver += "this.style.cursor='hand';this.style.cursor='pointer';";
                mouseOut += "document.getElementById('"+id+":"+idx+"').style.backgroundColor='transparent';";
            }
            idx = 0;
            if (links)
                ret += "<tr id='"+id+":"+idx+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\" onclick=\"document.location='examInfo.do?examId="+ids.elementAt(idx)+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
            else
                ret += "<tr id='"+id+":"+idx+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\">";
            ret += "<td valign='top' rowspan='"+getOtherExams().size()+"' style='font-weight:bold;color:"+PreferenceLevel.prolog2color(getPreference())+";'>";
            ret += PreferenceLevel.prolog2string(getPreference());
            ret += "</td>";
            ret += "<td valign='top' rowspan='"+getOtherExams().size()+"' style='font-weight:bold;color:"+PreferenceLevel.prolog2color(getPreference())+";'>";
            ret += getType();
            ret += "</td>";
            for (Iterator i=getOtherExams().iterator();i.hasNext();idx++) {
                ExamInfo a = (ExamInfo)i.next();
                ret += "<td>"+a.getExamNameHtml()+"</td>";
                if (a instanceof ExamAssignment) {
                    ExamAssignment ea = (ExamAssignment)a;
                    ret += "<td>"+ea.getPeriodAbbreviationWithPref()+"</td>";
                    ret += "<td>"+ea.getRoomsNameWithPref(", ")+"</td>";
                } else {
                    ret += "<td></td>";
                    ret += "<td></td>";
                }
                ret += "</tr>";
                if (i.hasNext()) {
                    if (links)
                        ret += "<tr id='"+id+":"+(1+idx)+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\" onclick=\"document.location='examInfo.do?examId="+ids.elementAt(1+idx)+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
                    else
                        ret += "<tr id='"+id+":"+(1+idx)+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\">";
                }
            }
            return ret;
        }
        
    }
    
    public static class Parameters {
        private int iBtbDistance = -1;
        private boolean iBtbDayBreak = false;
        private Set iPeriods;
        
        public Parameters(Long sessionId, Long examTypeId) {
            iPeriods = ExamPeriod.findAll(sessionId, examTypeId); 
            
            SolverParameterDef btbDistDef = SolverParameterDef.findByNameType("Exams.BackToBackDistance", SolverParameterGroup.SolverType.EXAM);
            if (btbDistDef!=null && btbDistDef.getDefault()!=null)
                iBtbDistance = Integer.parseInt(btbDistDef.getDefault());
        
            SolverParameterDef btbDayBreakDef = SolverParameterDef.findByNameType("Exams.IsDayBreakBackToBack", SolverParameterGroup.SolverType.EXAM);
            if (btbDayBreakDef!=null && btbDayBreakDef.getDefault()!=null)
                iBtbDayBreak = "true".equals(btbDayBreakDef.getDefault());
        }
        
        public int getBackToBackDistance() { return iBtbDistance; }
        public boolean isDayBreakBackToBack() { return iBtbDayBreak; }

        public boolean isBackToBack(ExamPeriod p1, ExamPeriod p2) {
            if (!isDayBreakBackToBack() && !p1.getDateOffset().equals(p2.getDateOffset())) return false;
            for (Iterator i=iPeriods.iterator();i.hasNext();) {
                ExamPeriod p = (ExamPeriod)i.next();
                if (p1.compareTo(p)<0 && p.compareTo(p2)<0) return false;
                if (p1.compareTo(p)>0 && p.compareTo(p2)>0) return false;
            }
            return true;
        }
    }
    
    /** Generate conflict information only for given student */
    public ExamAssignmentInfo(org.unitime.timetable.model.ExamOwner examOwner, Student student, Set<ExamOwner> examsOfTheSameStudent) {
        super(examOwner.getExam());
        iSections = new Vector();
        HashSet<Long> studentIds = new HashSet(); studentIds.add(student.getUniqueId());
        iSections.add(new ExamSectionInfo(examOwner, studentIds));
        org.unitime.timetable.model.Exam exam = examOwner.getExam();

        if (getPeriod()!=null) {
        	Parameters p = new Parameters(exam.getSession().getUniqueId(), exam.getExamType().getUniqueId());
        	TreeSet sameDateExams = new TreeSet();
            for (ExamOwner studentExamOwner : examsOfTheSameStudent) {
            	org.unitime.timetable.model.Exam other = studentExamOwner.getExam();
            	if (other.equals(getExam())) continue;
                ExamPeriod otherPeriod = other.getAssignedPeriod();
                if (otherPeriod==null) continue;
                if (getPeriod().equals(otherPeriod)) { //direct conflict
                    DirectConflict dc = new DirectConflict(new ExamAssignment(other));
                    dc.getStudents().add(student.getUniqueId());
                    iNrDirectConflicts++;
                    iDirects.add(dc);
                } else if (p.isBackToBack(getPeriod(),otherPeriod)) {
                    ExamAssignment ea = new ExamAssignment(other);
                    double distance = Location.getDistance(getRooms(), ea.getRooms());
                    BackToBackConflict btb = new BackToBackConflict(ea, (p.getBackToBackDistance()<0?false:distance>p.getBackToBackDistance()), distance);
                    btb.getStudents().add(student.getUniqueId());
                    iNrBackToBackConflicts++;
                    if (btb.isDistance()) iNrDistanceBackToBackConflicts++;
                    iBackToBacks.add(btb);
                }
                if (getPeriod().getDateOffset().equals(otherPeriod.getDateOffset()))
                    sameDateExams.add(other);
            }
            if (sameDateExams.size()>=2) {
                TreeSet examIds = new TreeSet();
                TreeSet otherExams = new TreeSet();
                for (Iterator j=sameDateExams.iterator();j.hasNext();) {
                    org.unitime.timetable.model.Exam other = (org.unitime.timetable.model.Exam)j.next();
                    examIds.add(other.getUniqueId());
                    otherExams.add(new ExamAssignment(other));
                }
                MoreThanTwoADayConflict m2d = new MoreThanTwoADayConflict(otherExams);
                iNrMoreThanTwoADayConflicts++;
                m2d.getStudents().add(student.getUniqueId());
                iMoreThanTwoADays.add(m2d);
            }
            
            if (ApplicationProperty.ExaminationConsiderEventConflicts.isTrue(examOwner.getExam().getExamType().getReference())) {
                int nrTravelSlots = ApplicationProperty.ExaminationTravelTimeClass.intValue();
            	for (Iterator i = new ExamDAO().getSession().createQuery(
                		"select m from ClassEvent e inner join e.meetings m, StudentClassEnrollment en "+
                		"where en.student.uniqueId=:studentId and e.clazz=en.clazz and " +
                		"m.meetingDate=:startDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot")
                		.setLong("studentId", student.getUniqueId())
                		.setDate("startDate", getPeriod().getStartDate())
                		.setInteger("startSlot", getPeriod().getStartSlot()-nrTravelSlots)
                		.setInteger("endSlot", getPeriod().getEndSlot()+nrTravelSlots)
                		.setCacheable(true).list().iterator();i.hasNext();) {
            		iDirects.add(new DirectConflict((Meeting)i.next(), studentIds));
            	}
            	for (Iterator i=ExamDAO.getInstance().getSession().createQuery(
                        "select m from "+
                        "CourseEvent e inner join e.meetings m inner join e.relatedCourses o, StudentClassEnrollment s where e.reqAttendance=true and m.approvalStatus = 1 and "+
                        "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId=:studentId and ("+
                        "(o.ownerType=:classType and s.clazz.uniqueId=o.ownerId) or "+
                        "(o.ownerType=:configType and s.clazz.schedulingSubpart.instrOfferingConfig.uniqueId=o.ownerId) or "+
                        "(o.ownerType=:courseType and s.courseOffering.uniqueId=o.ownerId) or "+
                        "(o.ownerType=:offeringType and s.courseOffering.instructionalOffering.uniqueId=o.ownerId))")
                        .setLong("studentId", student.getUniqueId())
                        .setDate("meetingDate", getPeriod().getStartDate())
                        .setInteger("startSlot", getPeriod().getStartSlot()-nrTravelSlots)
                        .setInteger("endSlot", getPeriod().getEndSlot()+nrTravelSlots)
                        .setInteger("classType", ExamOwner.sOwnerTypeClass)
                        .setInteger("configType", ExamOwner.sOwnerTypeConfig)
                        .setInteger("courseType", ExamOwner.sOwnerTypeCourse)
                        .setInteger("offeringType", ExamOwner.sOwnerTypeOffering)
                        .setCacheable(true).list().iterator();i.hasNext();) {
            		iDirects.add(new DirectConflict((Meeting)i.next(), studentIds));
            	}
            }
        }
    }
}
