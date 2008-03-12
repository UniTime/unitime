/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseExam;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;

public class Exam extends BaseExam implements Comparable<Exam> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Exam () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Exam (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static final int sSeatingTypeNormal = 0;
	public static final int sSeatingTypeExam = 1;
	
	public static final String sSeatingTypes[] = new String[] {"Normal","Exam"};
	
	public static final int sExamTypeFinal = 0;
	public static final int sExamTypeEvening = 1;
	
	public static final String sExamTypes[] = new String[] {"Final", "Evening"};
	
	protected boolean canUserEdit(User user) {
        //admin
        if (Roles.ADMIN_ROLE.equals(user.getCurrentRole())) 
            return true;
        
        //timetable manager 
        if (Roles.DEPT_SCHED_MGR_ROLE.equals(user.getCurrentRole())) {
            if (!getSession().getStatusType().canExamEdit())
                return false;
            for (Iterator i=getOwners().iterator();i.hasNext();) {
                ExamOwner owner = (ExamOwner)i.next();
                if (!owner.getCourse().getDepartment().canUserEdit(user)) return false;
            }
            return true;
        }
        
        //exam manager
        if (Roles.EXAM_MGR_ROLE.equals(user.getCurrentRole()))
            return getSession().getStatusType().canExamTimetable();
        
        return false;
	}

	protected boolean canUserView(User user) {
	    //can edit -> can view
        if (canUserEdit(user)) return true;
        
        //admin or exam manager
	    if (Roles.ADMIN_ROLE.equals(user.getCurrentRole()) || Roles.EXAM_MGR_ROLE.equals(user.getCurrentRole())) 
	        return true;
	    
        //timetable manager or view all 
	    if (Roles.DEPT_SCHED_MGR_ROLE.equals(user.getCurrentRole()) || Roles.VIEW_ALL_ROLE.equals(user.getCurrentRole()))
	        return getSession().getStatusType().canExamView();
	    
	    return false;
	}
	
	public String generateName() {
        StringBuffer sb = new StringBuffer();
        ExamOwner prev = null;
        for (Iterator i=new TreeSet(getOwners()).iterator();i.hasNext();) {
            ExamOwner owner = (ExamOwner)i.next();
            Object ownerObject = owner.getOwnerObject();
            if (prev!=null && prev.getCourse().getSubjectArea().equals(owner.getCourse().getSubjectArea())) {
                //same subject area
                if (prev.getCourse().equals(owner.getCourse()) && prev.getOwnerType().equals(owner.getOwnerType())) {
                    //same course number
                    switch (owner.getOwnerType()) {
                    case ExamOwner.sOwnerTypeConfig :
                        sb.append(", ["+((InstrOfferingConfig)ownerObject).getName()+"]");
                        break;
                    case ExamOwner.sOwnerTypeClass :
                        Class_ clazz = (Class_)ownerObject;
                        if (prev.getOwnerType()==ExamOwner.sOwnerTypeClass && ((Class_)prev.getOwnerObject()).getSchedulingSubpart().equals(clazz.getSchedulingSubpart())) {
                            //same subpart
                            sb.append(", "+clazz.getSectionNumberString());
                        } else
                            sb.append(", "+clazz.getItypeDesc()+" "+clazz.getSectionNumberString());
                        break;
                    }
                } else {
                    //different course number
                    sb.append("; "+owner.getCourse().getCourseNbr());
                    switch (owner.getOwnerType()) {
                    case ExamOwner.sOwnerTypeConfig :
                        sb.append(" ["+((InstrOfferingConfig)ownerObject).getName()+"]");
                        break;
                    case ExamOwner.sOwnerTypeClass :
                        Class_ clazz = (Class_)ownerObject;
                        sb.append(" "+clazz.getItypeDesc()+" "+clazz.getSectionNumberString());
                        break;
                    }
                }
            } else {
                //different subject area
                if (prev!=null) sb.append("; ");
                switch (owner.getOwnerType()) {
                case ExamOwner.sOwnerTypeConfig :
                    InstrOfferingConfig config = (InstrOfferingConfig)ownerObject;
                    sb.append(config.getControllingCourseOffering().getCourseName()+" ["+config.getName()+"]");
                    break;
                case ExamOwner.sOwnerTypeClass :
                    Class_ clazz = (Class_)ownerObject;
                    sb.append(clazz.getClassLabel());
                    break;
                default :
                    sb.append(owner.getCourse().getCourseName());
                }
            }
            prev = owner;
        }
	    return sb.toString();
	}
	
	public String getLabel() {
	    String name = getName();
	    if (name!=null) return name;
	    return generateName();
	}
	
	public String htmlLabel(){
	    return getLabel();
	}
	
	public Vector getOwnerObjects() {
	    Vector ret = new Vector();
	    for (Iterator i=new TreeSet(getOwners()).iterator();i.hasNext();) {
	        ExamOwner owner = (ExamOwner)i.next();
	        ret.add(owner.getOwnerObject());
	    }
	    return ret;
	}
	
	public ExamOwner firstOwner() {
	    ExamOwner ret = null;
	    for (Iterator i=getOwners().iterator();i.hasNext();) {
            ExamOwner owner = (ExamOwner)i.next();
            if (ret == null || ret.compareTo(owner)>0)
                ret = owner;
	    }
	    return ret;
	}
	
	public static List findAll(Long sessionId, Integer examType) {
	    return new ExamDAO().getSession().createQuery(
	            "select x from Exam x where x.session.uniqueId=:sessionId and x.examType=:examType"
	            )
	            .setLong("sessionId", sessionId)
	            .setInteger("examType", examType)
	            .setCacheable(true)
	            .list();
	}
	
    public static List findExamsOfSubjectArea(Long subjectAreaId, Integer examType) {
        return new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.subjectArea.uniqueId=:subjectAreaId and x.examType=:examType")
                .setLong("subjectAreaId", subjectAreaId)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list();
    }
    
    public static List findExamsOfCourseOffering(Long courseOfferingId, Integer examType) {
        return new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.uniqueId=:courseOfferingId and x.examType=:examType")
                .setLong("courseOfferingId", courseOfferingId)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list();
    }
    
    public static List findExamsOfCourse(Long subjectAreaId, String courseNbr, Integer examType) {
        if (courseNbr==null || courseNbr.trim().length()==0) return findExamsOfSubjectArea(subjectAreaId, examType);
        return new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.subjectArea.uniqueId=:subjectAreaId and x.examType=:examType and "+
                (courseNbr.indexOf('*')>=0?"o.course.courseNbr like :courseNbr":"o.course.courseNbr=:courseNbr"))
                .setLong("subjectAreaId", subjectAreaId)
                .setString("courseNbr", courseNbr.trim().replaceAll("\\*", "%"))
                .setInteger("examType", examType)
                .setCacheable(true)
                .list();
    }
    
    public Set getStudents() {
        HashSet students = new HashSet();
        for (Iterator i=getOwners().iterator();i.hasNext();)
            students.addAll(((ExamOwner)i.next()).getStudents());
        return students;
        /*
        return new ExamDAO().getSession().createQuery(
                "select distinct e.student from " +
                "StudentClassEnrollment e inner join e.clazz c " +
                "inner join c.schedulingSubpart.instrOfferingConfig ioc " +
                "inner join e.courseOffering co "+
                "inner join co.instructionalOffering io, " +
                "Exam x inner join x.owners o "+
                "where x.uniqueId=:examId and ("+
                "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=io.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                ")")
                .setLong("examId", getUniqueId())
                .setCacheable(true)
                .list();
                */
    }
    
    public int countStudents() {
        int nrStudents = 0;
        for (Iterator i=getOwners().iterator();i.hasNext();)
            nrStudents += ((ExamOwner)i.next()).countStudents();
        return nrStudents;
        /*
        return ((Number)new ExamDAO().getSession().createQuery(
                "select count(distinct e.student) from " +
                "StudentClassEnrollment e inner join e.clazz c " +
                "inner join c.schedulingSubpart.instrOfferingConfig ioc " +
                "inner join e.courseOffering co "+
                "inner join co.instructionalOffering io, " +
                "Exam x inner join x.owners o "+
                "where x.uniqueId=:examId and ("+
                "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=io.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                ")")
                .setLong("examId", getUniqueId())
                .setCacheable(true)
                .uniqueResult()).intValue();
                */
    }
    
    public Set effectivePreferences(Class type) {
        if (DistributionPref.class.equals(type)) {
            TreeSet prefs = new TreeSet();
            try {
                if (getDistributionObjects()==null) return prefs;
                for (Iterator j=getDistributionObjects().iterator();j.hasNext();) {
                    DistributionPref p = ((DistributionObject)j.next()).getDistributionPref();
                    prefs.add(p);
                }
            } catch (ObjectNotFoundException e) {
                new _RootDAO().getSession().refresh(this);
                for (Iterator j=getDistributionObjects().iterator();j.hasNext();) {
                    DistributionPref p = ((DistributionObject)j.next()).getDistributionPref();
                    prefs.add(p);
                }
            }
            return prefs;
        } else return super.effectivePreferences(type);
    }
    
    public Set getAvailableRooms() {
        return Location.findAllExamLocations(getSession().getUniqueId(), getExamType());
    }
    
    public SubjectArea firstSubjectArea() {
        for (Iterator i=new TreeSet(getOwners()).iterator();i.hasNext();) {
            ExamOwner owner = (ExamOwner)i.next();
            return owner.getCourse().getSubjectArea();
        }
        return null;
    }

    public CourseOffering firstCourseOffering() {
        for (Iterator i=new TreeSet(getOwners()).iterator();i.hasNext();) {
            ExamOwner owner = (ExamOwner)i.next();
            return owner.getCourse();
        }
        return null;
    }

    public Department firstDepartment() {
        for (Iterator i=new TreeSet(getOwners()).iterator();i.hasNext();) {
            ExamOwner owner = (ExamOwner)i.next();
            return owner.getCourse().getDepartment();
        }
        return null;
    }
    
    public String toString() {
        return getLabel();
    }
    
    public int compareTo(Exam exam) {
        Iterator i1 = new TreeSet(getOwners()).iterator();
        Iterator i2 = new TreeSet(exam.getOwners()).iterator();
        while (i1.hasNext() && i2.hasNext()) {
            ExamOwner o1 = (ExamOwner)i1.next();
            ExamOwner o2 = (ExamOwner)i2.next();
            int cmp = o1.compareTo(o2);
            if (cmp!=0) return cmp;
        }
        return (i1.hasNext()?1:i2.hasNext()?-1:getUniqueId().compareTo(exam.getUniqueId()));
    }
    
    public void deleteDependentObjects(org.hibernate.Session hibSession, boolean updateExam) {
        boolean deleted = false;
        if (getDistributionObjects()==null) return;
        for (Iterator i=getDistributionObjects().iterator();i.hasNext();) {
            DistributionObject relatedObject = (DistributionObject)i.next();
            DistributionPref distributionPref = relatedObject.getDistributionPref();
            distributionPref.getDistributionObjects().remove(relatedObject);
            Integer seqNo = relatedObject.getSequenceNumber();
            hibSession.delete(relatedObject);
            deleted = true;
            if (distributionPref.getDistributionObjects().isEmpty()) {
                PreferenceGroup owner = distributionPref.getOwner();
                owner.getPreferences().remove(distributionPref);
                getPreferences().remove(distributionPref);
                hibSession.saveOrUpdate(owner);
                hibSession.delete(distributionPref);
            } else {
                if (seqNo!=null) {
                    for (Iterator j=distributionPref.getDistributionObjects().iterator();j.hasNext();) {
                        DistributionObject dObj = (DistributionObject)j.next();
                        if (seqNo.compareTo(dObj.getSequenceNumber())<0) {
                            dObj.setSequenceNumber(new Integer(dObj.getSequenceNumber().intValue()-1));
                            hibSession.saveOrUpdate(dObj);
                        }
                    }
                }

                if (updateExam)
                    hibSession.saveOrUpdate(distributionPref);
            }
            i.remove();
        }

        if (deleted && updateExam)
            hibSession.saveOrUpdate(this);
    }
    
    public static void deleteFromExams(org.hibernate.Session hibSession, Integer ownerType, Long ownerId) {
        for (Iterator i=hibSession.createQuery("select o from Exam x inner join x.owners o where "+
                "o.ownerType=:ownerType and o.ownerId=:ownerId")
                .setInteger("ownerType", ownerType)
                .setLong("ownerId", ownerId).iterate();i.hasNext();) {
            ExamOwner owner = (ExamOwner)i.next();
            Exam exam = owner.getExam();
            exam.getOwners().remove(owner);
            hibSession.delete(owner);
            if (exam.getOwners().isEmpty()) {
                exam.deleteDependentObjects(hibSession, false);
                hibSession.delete(exam);
            } else {
                hibSession.saveOrUpdate(exam);
            }
        }
    }
    
    public static void deleteFromExams(org.hibernate.Session hibSession, Class_ clazz) {
        deleteFromExams(hibSession, ExamOwner.sOwnerTypeClass, clazz.getUniqueId());
    }
    public static void deleteFromExams(org.hibernate.Session hibSession, InstrOfferingConfig config) {
        deleteFromExams(hibSession, ExamOwner.sOwnerTypeConfig, config.getUniqueId());
    }
    public static void deleteFromExams(org.hibernate.Session hibSession, InstructionalOffering offering) {
        deleteFromExams(hibSession, ExamOwner.sOwnerTypeOffering, offering.getUniqueId());
    }
    public static void deleteFromExams(org.hibernate.Session hibSession, CourseOffering course) {
        deleteFromExams(hibSession, ExamOwner.sOwnerTypeCourse, course.getUniqueId());
    }
    
    public static List findAll(int ownerType, Long ownerId) {  
        return new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where "+
                "o.ownerType=:ownerType and o.ownerId=:ownerId").
                setInteger("ownerType", ownerType).
                setLong("ownerId", ownerId).
                setCacheable(true).list();
    }
    
    
    public static List findAllRelated(String type, Long id) {
        if ("Class_".equals(type)) {
            return new ExamDAO().getSession().createQuery(
                    "select distinct x from Exam x inner join x.owners o inner join o.course co "+
                    "inner join co.instructionalOffering io "+
                    "inner join io.instrOfferingConfigs ioc " +
                    "inner join ioc.schedulingSubparts ss "+
                    "inner join ss.classes c where "+
                    "c.uniqueId=:classId and ("+
                    "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=io.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                    ")").
                    setLong("classId", id).setCacheable(true).list();
        } else if ("SchedulingSubpart".equals(type)) {
            return new ExamDAO().getSession().createQuery(
                    "select distinct x from Exam x inner join x.owners o inner join o.course co "+
                    "inner join co.instructionalOffering io "+
                    "inner join io.instrOfferingConfigs ioc " +
                    "inner join ioc.schedulingSubparts ss "+
                    "left outer join ss.classes c where "+
                    "ss.uniqueId=:subpartId and ("+
                    "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=io.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                    ")").
                    setLong("subpartId", id).setCacheable(true).list();
        } else if ("CourseOffering".equals(type)) {
            return new ExamDAO().getSession().createQuery(
                    "select distinct x from Exam x inner join x.owners o inner join o.course co "+
                    "left outer join co.instructionalOffering io "+
                    "left outer join io.instrOfferingConfigs ioc " +
                    "left outer join ioc.schedulingSubparts ss "+
                    "left outer join ss.classes c where "+
                    "co.uniqueId=:courseOfferingId and ("+
                    "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=io.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                    ")").
                    setLong("courseOfferingId", id).setCacheable(true).list();
        } else if ("InstructionalOffering".equals(type)) {
            return new ExamDAO().getSession().createQuery(
                    "select distinct x from Exam x inner join x.owners o inner join o.course co "+
                    "inner join co.instructionalOffering io "+
                    "left outer join io.instrOfferingConfigs ioc " +
                    "left outer join ioc.schedulingSubparts ss "+
                    "left outer join ss.classes c where "+
                    "io.uniqueId=:instructionalOfferingId and ("+
                    "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=io.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                    ")").
                    setLong("instructionalOfferingId", id).setCacheable(true).list();
        } else if ("InstrOfferingConfig".equals(type)) {
            return new ExamDAO().getSession().createQuery(
                    "select distinct x from Exam x inner join x.owners o inner join o.course co "+
                    "inner join co.instructionalOffering io "+
                    "inner join io.instrOfferingConfigs ioc " +
                    "left outer join ioc.schedulingSubparts ss "+
                    "left outer join ss.classes c where "+
                    "ioc.uniqueId=:instrOfferingConfigId and ("+
                    "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=io.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                    "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                    ")").
                    setLong("instrOfferingConfigId", id).setCacheable(true).list();
        } else throw new RuntimeException("Unsupported type "+type);
    }
    
    public static boolean hasTimetable(Long sessionId, Integer examType) {
    	if (examType==null) return hasTimetable(sessionId);
        return ((Number)new ExamDAO().getSession().
                createQuery("select count(x) from Exam x " +
                		"where x.session.uniqueId=:sessionId and " +
                		"x.assignedPeriod!=null and x.examType=:examType").
                setLong("sessionId",sessionId).uniqueResult()).longValue()>0;
    }
    
    public static boolean hasTimetable(Long sessionId) {
        return ((Number)new ExamDAO().getSession().
                createQuery("select count(x) from Exam x " +
                		"where x.session.uniqueId=:sessionId and " +
                		"x.assignedPeriod!=null").
                setLong("sessionId",sessionId).uniqueResult()).longValue()>0;
    }

    public static boolean hasEveningExams(Long sessionId) {
    	return ((Number)new ExamDAO().getSession().
    			createQuery("select count(p) from ExamPeriod p " +
    					"where p.session.uniqueId=:sessionId and "+
    					"p.examType = "+sExamTypeEvening).
    			setLong("sessionId", sessionId).uniqueResult()).longValue()>0;
    }
    
    public static boolean hasFinalExams(Long sessionId) {
        return ((Number)new ExamDAO().getSession().
                createQuery("select count(p) from ExamPeriod p " +
                        "where p.session.uniqueId=:sessionId and "+
                        "p.examType = "+sExamTypeFinal).
                setLong("sessionId", sessionId).uniqueResult()).longValue()>0;
    }

    public static Collection<ExamAssignmentInfo> findAssignedExams(Long sessionId, Integer examType) {
        Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select x from Exam x where "+
                "x.session.uniqueId=:sessionId and x.assignedPeriod!=null and x.examType=:examType").
                setLong("sessionId", sessionId).
                setInteger("examType", examType).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamAssignmentInfo(exam));
        } 
        return ret;
    }
    
    public static Collection<ExamInfo> findUnassignedExams(Long sessionId, Integer examType) {
        Vector<ExamInfo> ret = new Vector<ExamInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select x from Exam x where "+
                "x.session.uniqueId=:sessionId and x.assignedPeriod=null and x.examType=:examType").
                setLong("sessionId", sessionId).
                setInteger("examType", examType).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamInfo(exam));
        } 
        return ret;
    }
    
    public static Collection<ExamAssignmentInfo> findAssignedExams(Long sessionId, Long subjectAreaId, Integer examType) {
        if (subjectAreaId==null || subjectAreaId<0) return findAssignedExams(sessionId, examType);
        Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.subjectArea.uniqueId=:subjectAreaId and "+
                "x.examType=:examType and "+
                "x.session.uniqueId=:sessionId and x.assignedPeriod!=null").
                setLong("sessionId", sessionId).
                setLong("examType", examType).
                setLong("subjectAreaId", subjectAreaId).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamAssignmentInfo(exam));
        } 
        return ret;
    }
    
    public static Collection<ExamInfo> findUnassignedExams(Long sessionId, Long subjectAreaId, Integer examType) {
        if (subjectAreaId==null || subjectAreaId<0) return findUnassignedExams(sessionId,examType);
        Vector<ExamInfo> ret = new Vector<ExamInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.subjectArea.uniqueId=:subjectAreaId and "+
                "x.examType=:examType and "+
                "x.session.uniqueId=:sessionId and x.assignedPeriod=null").
                setLong("sessionId", sessionId).
                setLong("examType", examType).
                setLong("subjectAreaId", subjectAreaId).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamInfo(exam));
        } 
        return ret;
    }
    
    public static Collection<ExamAssignmentInfo> findAssignedExamsOfLocation(Long locationId, Integer examType) throws Exception {
        Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.assignedRooms r where " +
                "r.uniqueId=:locationId and x.assignedPeriod!=null and "+
                "x.examType=:examType").
                setLong("locationId", locationId).
                setInteger("examType", examType).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamAssignmentInfo(exam));
        } 
        return ret;
    }
    
    public static Collection<ExamAssignmentInfo> findAssignedExamsOfInstructor(Long instructorId, Integer examType) throws Exception {
        Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.instructors i where " +
                "i.uniqueId=:instructorId and x.assignedPeriod!=null and "+
                "x.examType=:examType").
                setLong("instructorId", instructorId).
                setInteger("examType", examType).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamAssignmentInfo(exam));
        } 
        return ret;
    }
    
    public String assign(ExamAssignmentInfo assignment, Session hibSession) {
        Transaction tx = null;
        try {
            tx = hibSession.beginTransaction();
            
            setAssignedPeriod(assignment.getPeriod(hibSession));
            if (getAssignedRooms()==null) setAssignedRooms(new HashSet());
            getAssignedRooms().clear();
            for (ExamRoomInfo room : assignment.getRooms())
                getAssignedRooms().add(room.getLocation(hibSession));
            setAssignedPreference(assignment.getAssignedPreferenceString());
            
            HashSet otherExams = new HashSet();
            
            for (Iterator j=getConflicts().iterator();j.hasNext();) {
                ExamConflict conf = (ExamConflict)j.next();
                for (Iterator i=conf.getExams().iterator();i.hasNext();) {
                    Exam x = (Exam)i.next();
                    if (!x.equals(this)) {
                        x.getConflicts().remove(conf);
                        otherExams.add(x);
                    }
                }
                hibSession.delete(conf);
                j.remove();
            }

            for (Iterator i=assignment.getDirectConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.DirectConflict dc = (ExamAssignmentInfo.DirectConflict)i.next();
                ExamConflict conf = new ExamConflict();
                conf.setConflictType(ExamConflict.sConflictTypeDirect);
                conf.setStudents(getStudents(hibSession, dc.getStudents()));
                conf.setNrStudents(conf.getStudents().size());
                hibSession.save(conf);
                getConflicts().add(conf);
                Exam other = dc.getOtherExam().getExam(hibSession);
                other.getConflicts().add(conf);
                otherExams.add(other);
            }
            for (Iterator i=assignment.getBackToBackConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.BackToBackConflict btb = (ExamAssignmentInfo.BackToBackConflict)i.next();
                ExamConflict conf = new ExamConflict();
                conf.setConflictType(btb.isDistance()?ExamConflict.sConflictTypeBackToBackDist:ExamConflict.sConflictTypeBackToBack);
                conf.setDistance(btb.getDistance());
                conf.setStudents(getStudents(hibSession, btb.getStudents()));
                conf.setNrStudents(conf.getStudents().size());
                hibSession.save(conf);
                getConflicts().add(conf);
                Exam other = btb.getOtherExam().getExam(hibSession);
                other.getConflicts().add(conf);
                otherExams.add(other);
            }
            for (Iterator i=assignment.getMoreThanTwoADaysConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.MoreThanTwoADayConflict m2d = (ExamAssignmentInfo.MoreThanTwoADayConflict)i.next();
                ExamConflict conf = new ExamConflict();
                conf.setConflictType(ExamConflict.sConflictTypeMoreThanTwoADay);
                conf.setStudents(getStudents(hibSession, m2d.getStudents()));
                conf.setNrStudents(conf.getStudents().size());
                hibSession.save(conf);
                getConflicts().add(conf);
                for (Iterator j=m2d.getOtherExams().iterator();j.hasNext();) {
                    Exam otherExam = (Exam)((ExamInfo)j.next()).getExam(hibSession);
                    otherExam.getConflicts().add(conf);
                    otherExams.add(otherExam);
                }
            }
            for (Iterator i=assignment.getInstructorDirectConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.DirectConflict dc = (ExamAssignmentInfo.DirectConflict)i.next();
                ExamConflict conf = new ExamConflict();
                conf.setConflictType(ExamConflict.sConflictTypeDirect);
                conf.setStudents(getInstructors(hibSession, dc.getStudents()));
                conf.setNrStudents(conf.getStudents().size());
                hibSession.save(conf);
                getConflicts().add(conf);
                Exam other = dc.getOtherExam().getExam(hibSession);
                other.getConflicts().add(conf);
                otherExams.add(other);
            }
            for (Iterator i=assignment.getInstructorBackToBackConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.BackToBackConflict btb = (ExamAssignmentInfo.BackToBackConflict)i.next();
                ExamConflict conf = new ExamConflict();
                conf.setConflictType(btb.isDistance()?ExamConflict.sConflictTypeBackToBackDist:ExamConflict.sConflictTypeBackToBack);
                conf.setDistance(btb.getDistance());
                conf.setStudents(getInstructors(hibSession, btb.getStudents()));
                conf.setNrStudents(conf.getStudents().size());
                hibSession.save(conf);
                getConflicts().add(conf);
                Exam other = btb.getOtherExam().getExam(hibSession);
                other.getConflicts().add(conf);
                otherExams.add(other);
            }
            for (Iterator i=assignment.getInstructorMoreThanTwoADaysConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.MoreThanTwoADayConflict m2d = (ExamAssignmentInfo.MoreThanTwoADayConflict)i.next();
                ExamConflict conf = new ExamConflict();
                conf.setConflictType(ExamConflict.sConflictTypeMoreThanTwoADay);
                conf.setStudents(getInstructors(hibSession, m2d.getStudents()));
                conf.setNrStudents(conf.getStudents().size());
                hibSession.save(conf);
                getConflicts().add(conf);
                for (Iterator j=m2d.getOtherExams().iterator();j.hasNext();) {
                    Exam otherExam = (Exam)((ExamInfo)j.next()).getExam(hibSession);
                    otherExam.getConflicts().add(conf);
                    otherExams.add(otherExam);
                }
            }
            
            hibSession.update(this);
            for (Iterator i=otherExams.iterator();i.hasNext();)
                hibSession.update((Exam)i.next());

            tx.commit();
            return null;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
            return "Assignment failed, reason: "+e.getMessage();
        }
    }
    
    protected HashSet getStudents(org.hibernate.Session hibSession, Collection studentIds) {
        HashSet students = new HashSet();
        if (studentIds==null || studentIds.isEmpty()) return students;
        for (Iterator i=studentIds.iterator();i.hasNext();) {
            Long studentId = (Long)i.next();
            Student student = new StudentDAO().get(studentId, hibSession);
            if (student!=null) students.add(student);
        }
        return students;
    }

    protected HashSet getInstructors(org.hibernate.Session hibSession, Collection instructorIds) {
        HashSet instructors = new HashSet();
        if (instructorIds==null || instructorIds.isEmpty()) return instructors;
        for (Iterator i=instructorIds.iterator();i.hasNext();) {
            Long instructorId = (Long)i.next();
            DepartmentalInstructor instructor = new DepartmentalInstructorDAO().get(instructorId, hibSession);
            if (instructor!=null) instructors.add(instructor);
        }
        return instructors;
    }    
}