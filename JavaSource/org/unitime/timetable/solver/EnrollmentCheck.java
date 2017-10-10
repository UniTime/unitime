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
package org.unitime.timetable.solver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.cpsolver.coursett.constraint.JenrlConstraint;
import org.cpsolver.coursett.criteria.StudentCommittedConflict;
import org.cpsolver.coursett.criteria.StudentConflict;
import org.cpsolver.coursett.criteria.StudentDistanceConflict;
import org.cpsolver.coursett.criteria.StudentHardConflict;
import org.cpsolver.coursett.model.Configuration;
import org.cpsolver.coursett.model.FinalSectioning;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.Student;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.criteria.Criterion;
import org.cpsolver.ifs.util.Progress;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.CPSolverMessages;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;

/**
 * @author Tomas Muller
 */
public class EnrollmentCheck {
	protected static CPSolverMessages MSG = Localization.create(CPSolverMessages.class);
    private static java.text.DecimalFormat sDoubleFormat = new java.text.DecimalFormat("0.##",new java.text.DecimalFormatSymbols(Locale.US));
    private TimetableModel iModel = null;
    private Assignment<Lecture, Placement> iAssignment = null;
    private int iMessageLevel = Progress.MSGLEVEL_WARN;
    private int iMessageLowerLevel = Progress.MSGLEVEL_INFO;
    
    public EnrollmentCheck(TimetableModel model, Assignment<Lecture, Placement> assignment, int msgLevel) {
        iModel = model;
        iAssignment = assignment;
        setMessageLevel(msgLevel);
    }
    
    public void setMessageLevel(int messageLevel) {
    	iMessageLevel = messageLevel;
    	switch (iMessageLevel) {
    	case Progress.MSGLEVEL_FATAL:
    		iMessageLowerLevel = Progress.MSGLEVEL_ERROR; break;
    	case Progress.MSGLEVEL_ERROR:
    		iMessageLowerLevel = Progress.MSGLEVEL_WARN; break;
    	case Progress.MSGLEVEL_WARN:
    		iMessageLowerLevel = Progress.MSGLEVEL_INFO; break;
    	case Progress.MSGLEVEL_INFO:
    		iMessageLowerLevel = Progress.MSGLEVEL_DEBUG; break;
    	case Progress.MSGLEVEL_DEBUG:
    		iMessageLowerLevel = Progress.MSGLEVEL_TRACE; break;
    	case Progress.MSGLEVEL_TRACE:
    		iMessageLowerLevel = Progress.MSGLEVEL_TRACE; break;
    	}
    }
    
    protected String toString(Collection<Student> students) {
    	if (students == null) return "";
    	String ret = "";
    	for (Student s: students)
    		ret += (ret.isEmpty() ? "" : ", ") + s.getId();
    	return ret;
    }
    
    /** Check validity of JENRL constraints from student enrollments */
    public void checkJenrl(Progress p) {
        try {
            p.setPhase(MSG.phaseCheckingJenrl(),iModel.variables().size());
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
                                    p.error(MSG.warnWrongJenrl(getClassLabel(l1), getClassLabel(l2), j.getJenrl(), (long)Math.ceil(jenrl), toString(l1.students()), toString(l2.students()), toString(jenrlStudents)));
                                }
                            }
                        }
                        if (!found && jenrl>0) {
                            p.error(MSG.warnMissingJenrl(getClassLabel(l1), getClassLabel(l2), Math.round(jenrl), toString(l1.students()), toString(l2.students()), toString(jenrlStudents)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            p.error(MSG.warnUnexpectedException(e.getMessage()), e);
        }
    }
    
    public void checkEnrollment(Progress p, Student s, Long subpartId, Collection lectures) {
        Lecture enrolled = null;
        for (Iterator i=lectures.iterator();i.hasNext();) {
            Lecture lecture = (Lecture)i.next();
            if (s.getLectures().contains(lecture)) {
                if (enrolled!=null)
                    p.message(iMessageLevel, MSG.warnStudentInMultipleClasses(s.getId(), getClassLabel(enrolled), getClassLabel(lecture)));
                enrolled = lecture;
            }
        }
        if (enrolled==null) {
            p.message(iMessageLevel, MSG.warnStudentInNoClasses(s.getId(), getSubpartLabel(subpartId)));
        } else if (enrolled.hasAnyChildren()) {
            for (Long sid: enrolled.getChildrenSubpartIds()) {
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
        
        DecimalFormat df = new DecimalFormat("0.##");
        Criterion<Lecture, Placement> sc = iModel.getCriterion(StudentConflict.class);
        if (sc.getValue(iAssignment) != sc.getValue(iAssignment, iModel.variables())) {
            p.message(iMessageLevel, MSG.warnWrongStudentConflictCount(df.format(sc.getValue(iAssignment)), df.format(sc.getValue(iAssignment, iModel.variables()))));
        }
        
        Criterion<Lecture, Placement> shc = iModel.getCriterion(StudentHardConflict.class);
        if (shc.getValue(iAssignment) != shc.getValue(iAssignment, iModel.variables())) {
            p.message(iMessageLevel, MSG.warnWrongHardStudentConflictCount(df.format(shc.getValue(iAssignment)), df.format(shc.getValue(iAssignment, iModel.variables()))));
        }
        
        Criterion<Lecture, Placement> sdc = iModel.getCriterion(StudentDistanceConflict.class);
        if (sdc.getValue(iAssignment) != sdc.getValue(iAssignment, iModel.variables())) {
            p.message(iMessageLevel, MSG.warnWrongDistanceStudentConflictCount(df.format(sdc.getValue(iAssignment)), df.format(sdc.getValue(iAssignment, iModel.variables()))));
        }
        
        Criterion<Lecture, Placement> scc = iModel.getCriterion(StudentCommittedConflict.class);
        if (scc.getValue(iAssignment) != scc.getValue(iAssignment, iModel.variables())) {
            p.message(iMessageLevel, MSG.warnWrongCommittedStudentConflictCount(df.format(scc.getValue(iAssignment)), df.format(scc.getValue(iAssignment, iModel.variables()))));
        }
        
        p.setPhase(MSG.phaseCheckingClassLimits(), iModel.variables().size());
        for (Lecture lecture: iModel.variables()) {
            p.incProgress();
            p.debug("Checking "+getClassLabel(lecture)+" ... students="+lecture.students().size()+", weighted="+lecture.nrWeightedStudents()+", limit="+lecture.classLimit(iAssignment)+" ("+lecture.minClassLimit()+".."+lecture.maxClassLimit()+")");
            if (lecture.students().isEmpty()) continue;
            double w = 0;
            for (Iterator i = lecture.students().iterator(); i.hasNext();)
                w = Math.max(w, ((Student)i.next()).getOfferingWeight(lecture.getConfiguration().getOfferingId()));
            if (lecture.nrWeightedStudents() - w + FinalSectioning.sEps > lecture.classLimit(iAssignment)) {
                if (hasSubpartMixedOwnership(lecture))
                    p.message(iMessageLowerLevel, MSG.warnClassLimitOver(getClassLabel(lecture), sDoubleFormat.format(lecture.nrWeightedStudents()), lecture.classLimit(iAssignment)));
                else
                    p.message(iMessageLevel, MSG.warnClassLimitOver(getClassLabel(lecture), sDoubleFormat.format(lecture.nrWeightedStudents()), lecture.classLimit(iAssignment)));
            }
        }
        // checkJenrl(p);
        p.setPhase(MSG.phaseCheckingEnrollments(), iModel.getAllStudents().size());
        for (Iterator i=iModel.getAllStudents().iterator();i.hasNext();) {
            p.incProgress();
            Student student = (Student)i.next();
            for (Iterator j=student.getLectures().iterator();j.hasNext();) {
                Lecture lecture = (Lecture)j.next();
                if (!student.canEnroll(lecture))
                    p.message(iMessageLowerLevel, MSG.warnStudentInInvalidClass(student.getId(), getClassLabel(lecture)));
            }
            if (student.getConfigurations().size()!=student.getOfferings().size()) {
                Vector got = new Vector();
                for (Iterator j=student.getConfigurations().iterator();j.hasNext();) {
                    Configuration cfg = (Configuration)j.next();
                    got.add(cfg.getOfferingId());
                }
                p.message(iMessageLevel, MSG.warnStudentInWrongCourses(student.getId(), getOfferingsLabel(student.getOfferings()), getOfferingsLabel(got)));
            }
            for (Iterator j=student.getConfigurations().iterator();j.hasNext();) {
                Configuration cfg = (Configuration)j.next();
                for (Long subpartId: cfg.getTopSubpartIds()) {
                    checkEnrollment(p, student, subpartId, cfg.getTopLectures(subpartId));
                }
            }
        }
    }

}
