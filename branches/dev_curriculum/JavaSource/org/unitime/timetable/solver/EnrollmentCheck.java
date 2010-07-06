/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;

import net.sf.cpsolver.coursett.constraint.JenrlConstraint;
import net.sf.cpsolver.coursett.model.Configuration;
import net.sf.cpsolver.coursett.model.FinalSectioning;
import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Student;
import net.sf.cpsolver.coursett.model.TimetableModel;
import net.sf.cpsolver.ifs.util.ArrayList;
import net.sf.cpsolver.ifs.util.Progress;

/**
 * @author Tomas Muller
 */
public class EnrollmentCheck {
    private static java.text.DecimalFormat sDoubleFormat = new java.text.DecimalFormat("0.00",new java.text.DecimalFormatSymbols(Locale.US));
    private TimetableModel iModel = null;
    
    public EnrollmentCheck(TimetableModel model) {
        iModel = model;
    }
    
    /** Check validity of JENRL constraints from student enrollments */
    public void checkJenrl(Progress p) {
        try {
            p.setPhase("Checking jenrl ...",iModel.variables().size());
            for (Lecture l1: iModel.variables()) {
                p.incProgress();
                p.debug("Checking "+l1.getName()+" ...");
                for (Lecture l2: iModel.variables()) {
                    if (l1.getId()<l2.getId()) {
                        double jenrl = 0;
                        List<Student> jenrlStudents = new ArrayList<Student>();
                        for (Iterator i3=l1.students().iterator(); i3.hasNext(); ) {
                            Student student = (Student)i3.next();
                            if (l2.students().contains(student)) {
                                jenrl+=student.getJenrlWeight(l1,l2);
                                jenrlStudents.add(student);
                            }
                        }
                        boolean found = false;
                        for (JenrlConstraint j: iModel.getJenrlConstraints()) {
                            Lecture a=(Lecture)j.first();
                            Lecture b=(Lecture)j.second();
                            if ((a.equals(l1) && b.equals(l2)) || (a.equals(l2) && b.equals(l1))) {
                                found = true;
                                if (j.getJenrl()!=(int)Math.ceil(jenrl)) {
                                    p.error("Wrong jenrl between "+getClassLabel(l1)+" and "+getClassLabel(l2)+" (constraint="+j.getJenrl()+" != computed="+jenrl+").<br>"+
                                            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel(l1)+" has students: "+l1.students()+"<br>"+
                                            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel(l2)+" has students: "+l2.students()+"<br>"+
                                            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;intersection: "+jenrlStudents);
                                }
                            }
                        }
                        if (!found && jenrl>0) {
                            p.error("Missing jenrl between "+getClassLabel(l1)+" and "+getClassLabel(l2)+" (computed="+jenrl+").<br>"+
                                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel(l1)+" has students: "+l1.students()+"<br>"+
                                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel(l2)+" has students: "+l2.students()+"<br>"+
                                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;intersection: "+jenrlStudents);
                        }
                    }
                }
            }
        } catch (Exception e) {
            p.error("Unexpected exception: "+e.getMessage(),e);
        }
    }
    
    public void checkEnrollment(Progress p, Student s, Long subpartId, Collection lectures) {
        Lecture enrolled = null;
        for (Iterator i=lectures.iterator();i.hasNext();) {
            Lecture lecture = (Lecture)i.next();
            if (s.getLectures().contains(lecture)) {
                if (enrolled!=null)
                    p.warn("Student "+s.getId()+" enrolled in multiple classes of the same subpart "+getClassLabel(enrolled)+", "+getClassLabel(lecture)+".");
                enrolled = lecture;
            }
        }
        if (enrolled==null) {
            p.warn("Student "+s.getId()+" not enrolled in any class of subpart "+getSubpartLabel(subpartId)+".");
        } else if (enrolled.hasAnyChildren()) {
            for (Enumeration e=enrolled.getChildrenSubpartIds();e.hasMoreElements();) {
                Long sid = (Long)e.nextElement();
                checkEnrollment(p, s, sid, enrolled.getChildren(sid));
            }
        }
    }
    
    private String getClassLabel(Lecture lecture) {
        return "<A href='classDetail.do?cid="+lecture.getClassId()+"'>"+lecture.getName()+"</A>";
    }
    
    private String getSubpartLabel(Long subpartId) {
        SchedulingSubpart subpart = (new SchedulingSubpartDAO()).get(subpartId);
        if (subpart!=null) {
            String suffix = subpart.getSchedulingSubpartSuffix();
            return "<A href='schedulingSubpartDetail.do?ssuid="+subpart.getUniqueId()+"'>"+subpart.getCourseName()+" "+subpart.getItypeDesc().trim()+(suffix==null || suffix.length()==0?"":" ("+suffix+")")+"</A>";
        } else
            return subpartId.toString();
    }
    
    private String getOfferingLabel(Long offeringId) {
        InstructionalOffering offering = (new InstructionalOfferingDAO()).get(offeringId);
        if (offering!=null)
            return "<A href='instructionalOfferingDetail.do?io="+offering.getUniqueId()+"'>"+offering.getCourseName()+"</A>";
        else
            return offeringId.toString();
    }

    private String getOfferingsLabel(Collection offeringIds) {
        StringBuffer sb = new StringBuffer("[");
        if (offeringIds!=null) 
            for (Iterator i=offeringIds.iterator();i.hasNext();) {
                Long offeringId = (Long)i.next();
                sb.append(getOfferingLabel(offeringId));
                if (i.hasNext()) sb.append(", ");
            }
        sb.append("]");
        return sb.toString();
    }
    
    public boolean hasSubpartMixedOwnership(SchedulingSubpart subpart) {
        Department owner = null;
        for (Iterator i=subpart.getClasses().iterator();i.hasNext();) {
            Class_ clazz = (Class_)i.next();
            if (owner==null)
                owner = clazz.getManagingDept();
            else if (!owner.equals(clazz.getManagingDept()))
                return true;
        }
        if (subpart.getParentSubpart()!=null) 
            return hasSubpartMixedOwnership(subpart.getParentSubpart());
        return false;
    }
    
    public boolean hasSubpartMixedOwnership(Lecture lecture) {
        Class_ clazz = (new Class_DAO()).get(lecture.getClassId());
        if (clazz==null) return false;
        SchedulingSubpart subpart = clazz.getSchedulingSubpart();
        if (subpart.getClasses().size()>lecture.sameSubpartLectures().size())
            return true;
        if (lecture.getParent()!=null) {
            return hasSubpartMixedOwnership(lecture.getParent());
        } else {
            if (subpart.getParentSubpart()!=null) 
                return hasSubpartMixedOwnership(subpart.getParentSubpart());
        }
        return false;
    }

    public void checkStudentEnrollments(Progress p) {
        p.setStatus("Student Enrollments Check");
        if (iModel.getViolatedStudentConflicts()!=iModel.countViolatedStudentConflicts()) {
            p.warn("Inconsistent number of student conflits (counter="+iModel.getViolatedStudentConflicts()+", actual="+iModel.countViolatedStudentConflicts()+").");
        }
        if (iModel.getHardStudentConflicts()!=iModel.countHardStudentConflicts()) {
            p.warn("Inconsistent number of hard student conflits (counter="+iModel.getHardStudentConflicts()+", actual="+iModel.countHardStudentConflicts()+").");
        }
        if (iModel.getStudentDistanceConflicts()!=iModel.countStudentDistanceConflicts()) {
            p.warn("Inconsistent number of distance student conflits (counter="+iModel.getStudentDistanceConflicts()+", actual="+iModel.countStudentDistanceConflicts()+").");
        }
        if (iModel.getCommitedStudentConflicts()!=iModel.countCommitedStudentConflicts()) {
            p.warn("Inconsistent number of committed student conflits (counter="+iModel.getCommitedStudentConflicts()+", actual="+iModel.countCommitedStudentConflicts()+").");
        }
        p.setPhase("Checking class limits...", iModel.variables().size());
        for (Lecture lecture: iModel.variables()) {
            p.incProgress();
            p.debug("Checking "+getClassLabel(lecture)+" ... students="+lecture.students().size()+", weighted="+lecture.nrWeightedStudents()+", limit="+lecture.classLimit()+" ("+lecture.minClassLimit()+".."+lecture.maxClassLimit()+")");
            if (lecture.students().isEmpty()) continue;
            double w = 0;
            for (Iterator i = lecture.students().iterator(); i.hasNext();)
                w = Math.max(w, ((Student)i.next()).getOfferingWeight(lecture.getConfiguration().getOfferingId()));
            if (lecture.nrWeightedStudents()-w>FinalSectioning.sEps+lecture.classLimit()) {
                if (hasSubpartMixedOwnership(lecture))
                    p.info("Class limit exceeded for class "+getClassLabel(lecture)+" ("+sDoubleFormat.format(lecture.nrWeightedStudents())+">"+lecture.classLimit()+").");
                else
                    p.warn("Class limit exceeded for class "+getClassLabel(lecture)+" ("+sDoubleFormat.format(lecture.nrWeightedStudents())+">"+lecture.classLimit()+").");
            }
        }
        //iModel.checkJenrl(p);
        p.setPhase("Checking enrollments...", iModel.getAllStudents().size());
        for (Iterator i=iModel.getAllStudents().iterator();i.hasNext();) {
            p.incProgress();
            Student student = (Student)i.next();
            for (Iterator j=student.getLectures().iterator();j.hasNext();) {
                Lecture lecture = (Lecture)j.next();
                if (!student.canEnroll(lecture))
                    p.info("Student "+student.getId()+" enrolled to invalid class "+getClassLabel(lecture)+".");
            }
            if (student.getConfigurations().size()!=student.getOfferings().size()) {
                Vector got = new Vector();
                for (Iterator j=student.getConfigurations().iterator();j.hasNext();) {
                    Configuration cfg = (Configuration)j.next();
                    got.add(cfg.getOfferingId());
                }
                p.warn("Student "+student.getId()+" demands offerings "+getOfferingsLabel(student.getOfferings())+", but got "+getOfferingsLabel(got)+".");
            }
            for (Iterator j=student.getConfigurations().iterator();j.hasNext();) {
                Configuration cfg = (Configuration)j.next();
                for (Enumeration e=cfg.getTopSubpartIds();e.hasMoreElements();) {
                    Long subpartId = (Long)e.nextElement();
                    checkEnrollment(p, student, subpartId, cfg.getTopLectures(subpartId));
                }
            }
        }
    }

}
