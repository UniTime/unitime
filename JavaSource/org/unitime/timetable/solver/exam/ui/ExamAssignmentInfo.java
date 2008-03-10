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
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.Student;

import net.sf.cpsolver.exam.model.Exam;
import net.sf.cpsolver.exam.model.ExamDistributionConstraint;
import net.sf.cpsolver.exam.model.ExamInstructor;
import net.sf.cpsolver.exam.model.ExamModel;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamStudent;

/**
 * @author Tomas Muller
 */
public class ExamAssignmentInfo extends ExamAssignment implements Serializable  {
    private TreeSet iDirects = new TreeSet();
    private TreeSet iBackToBacks = new TreeSet();
    private TreeSet iMoreThanTwoADays = new TreeSet();
    private TreeSet iInstructorDirects = new TreeSet();
    private TreeSet iInstructorBackToBacks = new TreeSet();
    private TreeSet iInstructorMoreThanTwoADays = new TreeSet();
    private TreeSet iDistributions = new TreeSet();
    
    public ExamAssignmentInfo(ExamPlacement placement) {
        this((Exam)placement.variable(),placement);
    }

    public ExamAssignmentInfo(Exam exam, ExamPlacement placement) {
        super(exam, placement);
        if (placement!=null) {
            ExamModel model = (ExamModel)exam.getModel();
            Hashtable directs = new Hashtable();
            for (Enumeration e=exam.getStudents().elements();e.hasMoreElements();) {
                ExamStudent student = (ExamStudent)e.nextElement();
                for (Iterator i=student.getExams(placement.getPeriod()).iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    DirectConflict dc = (DirectConflict)directs.get(other);
                    if (dc==null) {
                        dc = new DirectConflict(new ExamAssignment((ExamPlacement)other.getAssignment()));
                        directs.put(other, dc);
                    } else dc.incNrStudents();
                    dc.getStudents().add(student.getId());
                }
            }
            iDirects.addAll(directs.values());
            int btbDist = model.getBackToBackDistance();
            Hashtable backToBacks = new Hashtable();
            for (Enumeration e=exam.getStudents().elements();e.hasMoreElements();) {
                ExamStudent student = (ExamStudent)e.nextElement();
                if (placement.getPeriod().prev()!=null) {
                    if (model.isDayBreakBackToBack() || placement.getPeriod().prev().getDay()==placement.getPeriod().getDay()) {
                        Set exams = student.getExams(placement.getPeriod().prev());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            double distance = placement.getDistance((ExamPlacement)other.getAssignment());
                            BackToBackConflict btb = (BackToBackConflict)backToBacks.get(other);
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment((ExamPlacement)other.getAssignment()),
                                        (btbDist<0?false:distance>btbDist), distance/5.0);
                                backToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(student.getId());
                        }
                    }
                }
                if (placement.getPeriod().next()!=null) {
                    if (model.isDayBreakBackToBack() || placement.getPeriod().next().getDay()==placement.getPeriod().getDay()) {
                        Set exams = student.getExams(placement.getPeriod().next());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            BackToBackConflict btb = (BackToBackConflict)backToBacks.get(other);
                            double distance = placement.getDistance((ExamPlacement)other.getAssignment());
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment((ExamPlacement)other.getAssignment()),
                                        (btbDist<0?false:distance>btbDist), distance);
                                backToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(student.getId());
                        }
                    }
                }
            }
            iBackToBacks.addAll(backToBacks.values());
            Hashtable m2ds = new Hashtable();
            for (Enumeration e=exam.getStudents().elements();e.hasMoreElements();) {
                ExamStudent student = (ExamStudent)e.nextElement();
                Set exams = student.getExamsADay(placement.getPeriod());
                int nrExams = exams.size() + (exams.contains(exam)?0:1);
                if (nrExams<=2) continue;
                TreeSet examIds = new TreeSet();
                TreeSet otherExams = new TreeSet();
                for (Iterator i=exams.iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    examIds.add(other.getId());
                    otherExams.add(new ExamAssignment((ExamPlacement)other.getAssignment()));
                }
                MoreThanTwoADayConflict m2d = (MoreThanTwoADayConflict)m2ds.get(examIds.toString());
                if (m2d==null) {
                    m2d = new MoreThanTwoADayConflict(otherExams);
                    m2ds.put(examIds.toString(), m2d);
                } else m2d.incNrStudents();
                m2d.getStudents().add(student.getId());
            }
            iMoreThanTwoADays.addAll(m2ds.values());

            Hashtable idirects = new Hashtable();
            for (Enumeration e=exam.getInstructors().elements();e.hasMoreElements();) {
                ExamInstructor instructor = (ExamInstructor)e.nextElement();
                for (Iterator i=instructor.getExams(placement.getPeriod()).iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    DirectConflict dc = (DirectConflict)idirects.get(other);
                    if (dc==null) {
                        dc = new DirectConflict(new ExamAssignment((ExamPlacement)other.getAssignment()));
                        idirects.put(other, dc);
                    } else dc.incNrStudents();
                    dc.getStudents().add(instructor.getId());
                }
            }
            iInstructorDirects.addAll(idirects.values());
            Hashtable ibackToBacks = new Hashtable();
            for (Enumeration e=exam.getInstructors().elements();e.hasMoreElements();) {
                ExamInstructor instructor = (ExamInstructor)e.nextElement();
                if (placement.getPeriod().prev()!=null) {
                    if (model.isDayBreakBackToBack() || placement.getPeriod().prev().getDay()==placement.getPeriod().getDay()) {
                        Set exams = instructor.getExams(placement.getPeriod().prev());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            double distance = placement.getDistance((ExamPlacement)other.getAssignment());
                            BackToBackConflict btb = (BackToBackConflict)ibackToBacks.get(other);
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment((ExamPlacement)other.getAssignment()),
                                        (btbDist<0?false:distance>btbDist), distance/5.0);
                                ibackToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(instructor.getId());
                        }
                    }
                }
                if (placement.getPeriod().next()!=null) {
                    if (model.isDayBreakBackToBack() || placement.getPeriod().next().getDay()==placement.getPeriod().getDay()) {
                        Set exams = instructor.getExams(placement.getPeriod().next());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            BackToBackConflict btb = (BackToBackConflict)ibackToBacks.get(other);
                            double distance = placement.getDistance((ExamPlacement)other.getAssignment());
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment((ExamPlacement)other.getAssignment()),
                                        (btbDist<0?false:distance>btbDist), distance);
                                ibackToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(instructor.getId());
                        }
                    }
                }
            }
            iInstructorBackToBacks.addAll(ibackToBacks.values());
            Hashtable im2ds = new Hashtable();
            for (Enumeration e=exam.getInstructors().elements();e.hasMoreElements();) {
                ExamInstructor instructor = (ExamInstructor)e.nextElement();
                Set exams = instructor.getExamsADay(placement.getPeriod());
                int nrExams = exams.size() + (exams.contains(exam)?0:1);
                if (nrExams<=2) continue;
                TreeSet examIds = new TreeSet();
                TreeSet otherExams = new TreeSet();
                for (Iterator i=exams.iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    examIds.add(other.getId());
                    otherExams.add(new ExamAssignment((ExamPlacement)other.getAssignment()));
                }
                MoreThanTwoADayConflict m2d = (MoreThanTwoADayConflict)im2ds.get(examIds.toString());
                if (m2d==null) {
                    m2d = new MoreThanTwoADayConflict(otherExams);
                    im2ds.put(examIds.toString(), m2d);
                } else m2d.incNrStudents();
                m2d.getStudents().add(instructor.getId());
            }
            iInstructorMoreThanTwoADays.addAll(im2ds.values());
        }
        for (Enumeration e=exam.getDistributionConstraints().elements();e.hasMoreElements();) {
            ExamDistributionConstraint dc = (ExamDistributionConstraint)e.nextElement();
            if (dc.isHard()) {
                if (dc.inConflict(placement))
                    iDistributions.add(new DistributionConflict(dc,exam));
            } else {
                if (!dc.isSatisfied(placement))
                    iDistributions.add(new DistributionConflict(dc,exam));
            }
        }
    }
    
    public ExamAssignmentInfo(org.unitime.timetable.model.Exam exam) {
        super(exam);
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
                    if (other==null) continue;
                    if (conf.getNrStudents()>0)
                        iDirects.add(new DirectConflict(other, conf.getNrStudents()));
                    if (conf.getNrInstructors()>0)
                        iInstructorDirects.add(new DirectConflict(other, conf.getNrInstructors()));
                } else if (conf.isBackToBackConflict()) {
                    ExamAssignment other = null;
                    for (Iterator j=conf.getExams().iterator();j.hasNext();) {
                        org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)j.next();
                        if (x.equals(exam)) continue;
                        if (x.getAssignedPeriod()!=null) other = new ExamAssignment(x);
                    }
                    if (other==null) continue;
                    if (conf.getNrStudents()>0)
                        iBackToBacks.add(new BackToBackConflict(other, conf.getNrStudents(), conf.isDistanceBackToBackConflict(), conf.getDistance()));
                    if (conf.getNrInstructors()>0)
                        iInstructorBackToBacks.add(new BackToBackConflict(other, conf.getNrInstructors(), conf.isDistanceBackToBackConflict(), conf.getDistance()));
                } else if (conf.isMoreThanTwoADayConflict()) {
                    TreeSet other = new TreeSet();
                    for (Iterator j=conf.getExams().iterator();j.hasNext();) {
                        org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)j.next();
                        if (x.equals(exam)) continue;
                        if (x.getAssignedPeriod()!=null) other.add(new ExamAssignment(x));
                    }
                    if (other.size()<2) continue;
                    if (conf.getNrStudents()>0)
                        iMoreThanTwoADays.add(new MoreThanTwoADayConflict(other, conf.getNrStudents()));
                    if (conf.getNrInstructors()>0)
                        iInstructorMoreThanTwoADays.add(new MoreThanTwoADayConflict(other, conf.getNrInstructors()));
                }
            }
        }
        for (Iterator i=exam.getDistributionObjects().iterator();i.hasNext();) {
            DistributionObject dObj = (DistributionObject)i.next();
            DistributionPref pref = dObj.getDistributionPref();
            if (!check(pref, exam, getPeriod(), getRooms()))
                iDistributions.add(new DistributionConflict(pref, exam));
        }
    }
    
    public boolean check(DistributionPref pref, org.unitime.timetable.model.Exam exam, ExamPeriod assignedPeriod, Collection<ExamRoomInfo> assignedRooms) {
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
                    ExamPeriod p = x.getAssignedPeriod();
                    if (x.equals(exam)) p = assignedPeriod;
                    if (p==null) continue;
                    if (period==null) period = p;
                    else if (!period.equals(p)) return false;
                }
                return true;
            } else { //different period
                HashSet periods = new HashSet();
                for (Iterator i=pref.getDistributionObjects().iterator();i.hasNext();) {
                    org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)((DistributionObject)i.next()).getPrefGroup();
                    ExamPeriod p = x.getAssignedPeriod();
                    if (x.equals(exam)) p = assignedPeriod;
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
                ExamPeriod p = x.getAssignedPeriod();
                if (x.equals(exam)) p = assignedPeriod;
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
                    Collection<ExamRoomInfo> r = null;
                    if (x.equals(exam)) {
                        r = assignedRooms;
                    } else {
                        if (x.getAssignedPeriod()==null) continue;
                        r = new ExamAssignment(x).getRooms();
                    }
                    if (r==null) continue;
                    if (rooms==null) rooms = r;
                    else if (!rooms.containsAll(r) || !r.containsAll(rooms)) return false;
                }
                return true;
            } else { //different room
                Collection<ExamRoomInfo> allRooms = new HashSet();
                for (Iterator i=pref.getDistributionObjects().iterator();i.hasNext();) {
                    org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)((DistributionObject)i.next()).getPrefGroup();
                    Collection<ExamRoomInfo> r = null;
                    if (x.equals(exam)) {
                        r = assignedRooms;
                    } else {
                        if (x.getAssignedPeriod()==null) continue;
                        r = new ExamAssignment(x).getRooms();
                    }
                    if (r==null) continue;
                    for (ExamRoomInfo room : r) {
                        if (!allRooms.add(room)) return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    public ExamAssignmentInfo(org.unitime.timetable.model.Exam exam, ExamPeriod period, Collection<ExamRoomInfo> rooms) throws Exception {
        super(exam, period, rooms);
        if (period==null) return;
        
        int btbDist = -1;
        boolean btbDayBreak = false;
        SolverParameterDef btbDistDef = SolverParameterDef.findByName("Exams.BackToBackDistance");
        if (btbDistDef!=null && btbDistDef.getDefault()!=null)
            btbDist = Integer.parseInt(btbDistDef.getDefault());
        
        SolverParameterDef btbDayBreakDef = SolverParameterDef.findByName("Exams.IsDayBreakBackToBack");
        if (btbDayBreakDef!=null && btbDayBreakDef.getDefault()!=null)
            btbDayBreak = "true".equals(btbDayBreakDef.getDefault());
        
        Hashtable directs = new Hashtable();
        Hashtable backToBacks = new Hashtable();
        Hashtable m2ds = new Hashtable();
        for (Iterator i=exam.getStudents().iterator();i.hasNext();) {
            Student student = (Student)i.next();
            TreeSet sameDateExams = new TreeSet();
            for (Iterator j=student.getExams(exam.getExamType()).iterator();j.hasNext();) {
                org.unitime.timetable.model.Exam other = (org.unitime.timetable.model.Exam)j.next();
                if (other.equals(exam) || other.getAssignedPeriod()==null) continue;
                if (period.equals(other.getAssignedPeriod())) { //direct conflict
                    DirectConflict dc = (DirectConflict)directs.get(other);
                    if (dc==null) {
                        dc = new DirectConflict(new ExamAssignment(other));
                        directs.put(other, dc);
                    } else dc.incNrStudents();
                    dc.getStudents().add(student.getUniqueId());
                } else if (period.isBackToBack(other.getAssignedPeriod(),btbDayBreak)) {
                    BackToBackConflict btb = (BackToBackConflict)backToBacks.get(other);
                    double distance = Location.getDistance(rooms, other.getAssignedRooms());
                    if (btb==null) {
                        btb = new BackToBackConflict(new ExamAssignment(other), (btbDist<0?false:distance>btbDist), distance);
                        backToBacks.put(other, btb);
                    } else btb.incNrStudents();
                    btb.getStudents().add(student.getUniqueId());
                }
                if (period.getDateOffset().equals(other.getAssignedPeriod().getDateOffset()))
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
                MoreThanTwoADayConflict m2d = (MoreThanTwoADayConflict)m2ds.get(examIds.toString());
                if (m2d==null) {
                    m2d = new MoreThanTwoADayConflict(otherExams);
                    m2ds.put(examIds.toString(), m2d);
                } else m2d.incNrStudents();
                m2d.getStudents().add(student.getUniqueId());
            }
        }
        iDirects.addAll(directs.values());
        iBackToBacks.addAll(backToBacks.values());
        iMoreThanTwoADays.addAll(m2ds.values());
        
        Hashtable idirects = new Hashtable();
        Hashtable ibackToBacks = new Hashtable();
        Hashtable im2ds = new Hashtable();
        for (Iterator i=exam.getInstructors().iterator();i.hasNext();) {
            DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
            TreeSet sameDateExams = new TreeSet();
            for (Iterator j=instructor.getExams(exam.getExamType()).iterator();j.hasNext();) {
                org.unitime.timetable.model.Exam other = (org.unitime.timetable.model.Exam)j.next();
                if (other.equals(exam) || other.getAssignedPeriod()==null) continue;
                if (period.equals(other.getAssignedPeriod())) { //direct conflict
                    DirectConflict dc = (DirectConflict)idirects.get(other);
                    if (dc==null) {
                        dc = new DirectConflict(new ExamAssignment(other));
                        idirects.put(other, dc);
                    } else dc.incNrStudents();
                    dc.getStudents().add(instructor.getUniqueId());
                } else if (period.isBackToBack(other.getAssignedPeriod(),btbDayBreak)) {
                    BackToBackConflict btb = (BackToBackConflict)ibackToBacks.get(other);
                    double distance = Location.getDistance(rooms, other.getAssignedRooms());
                    if (btb==null) {
                        btb = new BackToBackConflict(new ExamAssignment(other), (btbDist<0?false:distance>btbDist), distance);
                        ibackToBacks.put(other, btb);
                    } else btb.incNrStudents();
                    btb.getStudents().add(instructor.getUniqueId());
                }
                if (period.getDateOffset().equals(other.getAssignedPeriod().getDateOffset()))
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
                MoreThanTwoADayConflict m2d = (MoreThanTwoADayConflict)im2ds.get(examIds.toString());
                if (m2d==null) {
                    m2d = new MoreThanTwoADayConflict(otherExams);
                    im2ds.put(examIds.toString(), m2d);
                } else m2d.incNrStudents();
                m2d.getStudents().add(instructor.getUniqueId());
            }
        }
        iInstructorDirects.addAll(idirects.values());
        iInstructorBackToBacks.addAll(ibackToBacks.values());
        iInstructorMoreThanTwoADays.addAll(im2ds.values());   

        for (Iterator i=exam.getDistributionObjects().iterator();i.hasNext();) {
            DistributionObject dObj = (DistributionObject)i.next();
            DistributionPref pref = dObj.getDistributionPref();
            if (!check(pref, exam, period, rooms))
                iDistributions.add(new DistributionConflict(pref, exam));
        }
    }
    
    public TreeSet getDirectConflicts() {
        return iDirects;
    }

    public TreeSet getBackToBackConflicts() {
        return iBackToBacks;
    }
    
    public TreeSet getMoreThanTwoADaysConflicts() {
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
    
    public TreeSet getDistributionConflicts() {
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
        String ret = "<table border='0' width='95%' cellspacing='0' cellpadding='3'>";
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
    
    public String getDistributionConflictTable() {
        return getDistributionConflictTable(true);
    }
    
    public String getDistributionConflictTable(boolean header) {
        String ret = "<table border='0' width='95%' cellspacing='0' cellpadding='3'>";
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
    
    public TreeSet getInstructorDirectConflicts() {
        return iInstructorDirects;
    }

    public TreeSet getInstructorBackToBackConflicts() {
        return iInstructorBackToBacks;
    }
    
    public TreeSet getInstructorMoreThanTwoADaysConflicts() {
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
    
    public boolean getHasInstructorConflicts() {
        return !getInstructorDirectConflicts().isEmpty() || !getInstructorBackToBackConflicts().isEmpty() || !getInstructorMoreThanTwoADaysConflicts().isEmpty();
    }
    
    public String getInstructorConflictTable() {
        return getInstructorConflictTable(true);
    }
    
    public String getInstructorConflictTable(boolean header) {
        String ret = "<table border='0' width='95%' cellspacing='0' cellpadding='3'>";
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
    
    
    public static class DirectConflict implements Serializable, Comparable {
        protected ExamAssignment iOtherExam;
        protected int iNrStudents = 1;
        protected transient Vector iStudents = new Vector();
        
        protected DirectConflict(ExamAssignment otherExam) {
            iOtherExam = otherExam;
        }
        protected DirectConflict(ExamAssignment otherExam, int nrStudents) {
            iOtherExam = otherExam;
            iNrStudents = nrStudents;
        }
        protected void incNrStudents() {
            iNrStudents++;
        }
        public int getNrStudents() {
            return iNrStudents;
        }
        public Vector getStudents() {
            return iStudents;
        }
        public ExamAssignment getOtherExam() {
            return iOtherExam;
        }
        public int compareTo(Object o) {
            DirectConflict c = (DirectConflict)o;
            int cmp = -Double.compare(getNrStudents(), c.getNrStudents());
            if (cmp!=0) return cmp;
            return getOtherExam().compareTo(c.getOtherExam());
        }
        public String toString() {
            String ret = "";
            ret += "<tr onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';\" onmouseout=\"this.style.backgroundColor='transparent';\">";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("P")+";'>";
            ret += String.valueOf(getNrStudents());
            ret += "</td>";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("P")+";'>";
            ret += "Direct";
            ret += "</td>";
            ret += "<td>"+getOtherExam().getExamNameHtml()+"</td>";
            ret += "<td>"+getOtherExam().getPeriodAbbreviationWithPref()+"</td>";
            ret += "<td>"+getOtherExam().getRoomsNameWithPref(", ")+"</td>";
            ret += "</tr>";
            return ret;
        }
    }
    
    public static class BackToBackConflict implements Serializable, Comparable {
        protected ExamAssignment iOtherExam;
        protected int iNrStudents = 1;
        protected boolean iIsDistance = false; 
        protected transient Vector iStudents = new Vector();
        protected double iDistance = 0;
        
        protected BackToBackConflict(ExamAssignment otherExam, boolean isDistance, double distance) {
            iOtherExam = otherExam;
            iIsDistance = isDistance;
            iDistance = distance;
        }
        protected BackToBackConflict(ExamAssignment otherExam, int nrStudents, boolean isDistance, double distance) {
            iOtherExam = otherExam;
            iNrStudents = nrStudents;
            iIsDistance = isDistance;
            iDistance = distance;
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
        public Vector getStudents() {
            return iStudents;
        }
        public double getDistance() {
            return iDistance;
        }
        public int compareTo(Object o) {
            BackToBackConflict c = (BackToBackConflict)o;
            int cmp = -Double.compare(getNrStudents(), c.getNrStudents());
            if (cmp!=0) return cmp;
            if (isDistance()!=c.isDistance()) return (isDistance()?-1:1);
            return getOtherExam().compareTo(c.getOtherExam());
        }
        public String toString() {
            String ret = "";
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

    public static class MoreThanTwoADayConflict implements Serializable, Comparable {
        protected TreeSet iOtherExams;
        protected int iNrStudents = 1;
        protected transient Vector iStudents = new Vector();
        
        protected MoreThanTwoADayConflict(TreeSet otherExams) {
            iOtherExams = otherExams;
        }
        protected MoreThanTwoADayConflict(TreeSet otherExams, int nrStudents) {
            iOtherExams = otherExams;
            iNrStudents = nrStudents;
        }
        protected void incNrStudents() {
            iNrStudents++;
        }
        public int getNrStudents() {
            return iNrStudents;
        }
        public Vector getStudents() {
            return iStudents;
        }
        public TreeSet getOtherExams() {
            return iOtherExams;
        }
        public int compareTo(Object o) {
            MoreThanTwoADayConflict c = (MoreThanTwoADayConflict)o;
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
            for (Iterator i=getOtherExams().iterator();i.hasNext();idx++) {
                ExamAssignment a = (ExamAssignment)i.next();
                mouseOver += "document.getElementById('"+id+":"+idx+"').style.backgroundColor='rgb(223,231,242)';";
                mouseOut += "document.getElementById('"+id+":"+idx+"').style.backgroundColor='transparent';";
            }
            idx = 0;
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
                if (i.hasNext()) 
                    ret += "<tr id='"+id+":"+(1+idx)+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\">";
            }
            return ret;
        }
    }
    
    public static class DistributionConflict implements Serializable, Comparable {
        protected TreeSet iOtherExams;
        protected String iPreference;
        protected Long iId;
        protected String iType;
        protected transient DistributionPref iPref = null;
        protected DistributionConflict(Long id, String type, TreeSet otherExams, String preference) {
            iId = id;
            iType = type;
            iOtherExams = otherExams;
            iPreference = preference;
        }
        protected DistributionConflict(ExamDistributionConstraint dc, Exam exclude) {
            iId = dc.getId();
            iType = dc.getTypeString();
            iOtherExams = new TreeSet();
            for (Enumeration e=dc.variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                if (exam.equals(exclude)) continue;
                iOtherExams.add(exam.getAssignment()==null?new ExamInfo(exam):new ExamAssignment(exam,(ExamPlacement)exam.getAssignment()));
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
                ExamAssignment a = (ExamAssignment)i.next();
                title += a.getExamName();
                if (i.hasNext()) title += " and ";
            }
            return "<span style='font-weight:bold;color:"+PreferenceLevel.prolog2color(getPreference())+";' title='"+title+"'>"+iType+"</span>";
        }
        public String getPreference() {
            return iPreference;
        }
        public TreeSet getOtherExams() {
            return iOtherExams;
        }
        public int compareTo(Object o) {
            DistributionConflict c = (DistributionConflict)o;
            Iterator i1 = getOtherExams().iterator(), i2 = c.getOtherExams().iterator();
            while (i1.hasNext()) {
                ExamAssignment a1 = (ExamAssignment)i1.next();
                ExamAssignment a2 = (ExamAssignment)i2.next();
                if (!a1.equals(a2)) return a1.compareTo(a2);
            }
            return getId().compareTo(c.getId());
        }
        public String toString() {
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
            for (Iterator i=getOtherExams().iterator();i.hasNext();idx++) {
                ExamAssignment a = (ExamAssignment)i.next();
                mouseOver += "document.getElementById('"+id+":"+idx+"').style.backgroundColor='rgb(223,231,242)';";
                mouseOut += "document.getElementById('"+id+":"+idx+"').style.backgroundColor='transparent';";
            }
            idx = 0;
            ret += "<tr id='"+id+":"+idx+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\">";
            ret += "<td valign='top' rowspan='"+getOtherExams().size()+"' style='font-weight:bold;color:"+PreferenceLevel.prolog2color(getPreference())+";'>";
            ret += PreferenceLevel.prolog2string(getPreference());
            ret += "</td>";
            ret += "<td valign='top' rowspan='"+getOtherExams().size()+"' style='font-weight:bold;color:"+PreferenceLevel.prolog2color(getPreference())+";'>";
            ret += getType();
            ret += "</td>";
            for (Iterator i=getOtherExams().iterator();i.hasNext();idx++) {
                ExamAssignment a = (ExamAssignment)i.next();
                ret += "<td>"+a.getExamNameHtml()+"</td>";
                ret += "<td>"+a.getPeriodAbbreviationWithPref()+"</td>";
                ret += "<td>"+a.getRoomsNameWithPref(", ")+"</td>";
                ret += "</tr>";
                if (i.hasNext()) 
                    ret += "<tr id='"+id+":"+(1+idx)+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\">";
            }
            return ret;
        }
        
    }
}
