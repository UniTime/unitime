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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseExam;
import org.unitime.timetable.model.dao.ExamDAO;

public class Exam extends BaseExam {
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
	
	protected boolean canUserEdit(User user) {
        //admin
        if (Roles.ADMIN_ROLE.equals(user.getCurrentRole())) 
            return true;
        
        //timetable manager 
        if (Roles.DEPT_SCHED_MGR_ROLE.equals(user.getCurrentRole()))
            return getSession().getStatusType().canExamEdit();
        
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
            if (prev!=null && prev.getCourse().getSubjectArea().equals(owner.getCourse().getSubjectArea()) && prev.getOwnerType().equals(owner.getOwnerType())) {
                //same subject area
                if (prev.getCourse().equals(owner.getCourse())) {
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
                    sb.append(", "+owner.getCourse().getCourseNbr());
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
                if (prev!=null) sb.append(", ");
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
	
	public static List findAll(Long sessionId) {
	    return new ExamDAO().getSession().createQuery(
	            "select x from Exam x where x.session.uniqueId=:sessionId"
	            )
	            .setLong("sessionId", sessionId)
	            .setCacheable(true)
	            .list();
	}
	
    public static List findExamsOfSubjectArea(Long subjectAreaId) {
        return new ExamDAO().getSession().createQuery(
                "select distinct x from SubjectArea sa inner join sa.courseOfferings co " +
                "left outer join co.instructionalOffering.instrOfferingConfigs ioc " +
                "left outer join ioc.schedulingSubparts ss "+
                "left outer join ss.classes c, "+
                "Exam x inner join x.owners o where " +
                "sa.uniqueId=:subjectAreaId and "+
                "x.session.uniqueId=sa.session.uniqueId and " +
                "ioc.instructionalOffering=co.instructionalOffering and "+
                "("+
                "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=co.instructionalOffering.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                ")")
                .setLong("subjectAreaId", subjectAreaId)
                .setCacheable(true)
                .list();
    }
    
    public static List findExamsOfCourseOffering(Long courseOfferingId) {
        return new ExamDAO().getSession().createQuery(
                "select distinct x from CourseOffering co " +
                "left outer join co.instructionalOffering.instrOfferingConfigs ioc " +
                "left outer join ioc.schedulingSubparts ss "+
                "left outer join ss.classes c, "+
                "Exam x inner join x.owners o where " +
                "co.uniqueId=:courseOfferingId and "+
                "x.session.uniqueId=co.subjectArea.session.uniqueId and " +
                "ioc.instructionalOffering=co.instructionalOffering and "+
                "("+
                "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=co.instructionalOffering.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                ")")
                .setLong("courseOfferingId", courseOfferingId)
                .setCacheable(true)
                .list();
    }
    
    public static List findExamsOfCourse(Long subjectAreaId, String courseNbr) {
        if (courseNbr==null || courseNbr.trim().length()==0) return findExamsOfSubjectArea(subjectAreaId);
        return new ExamDAO().getSession().createQuery(
                "select distinct x from CourseOffering co " +
                "left outer join co.instructionalOffering.instrOfferingConfigs ioc " +
                "left outer join ioc.schedulingSubparts ss "+
                "left outer join ss.classes c, "+
                "Exam x inner join x.owners o where " +
                (courseNbr.indexOf('*')>=0?"co.courseNbr like :courseNbr":"co.courseNbr=:courseNbr")+
                " and co.subjectArea.uniqueId=:subjectAreaId and "+
                "x.session.uniqueId=co.subjectArea.session.uniqueId and " +
                "ioc.instructionalOffering=co.instructionalOffering and "+
                "("+
                "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=co.instructionalOffering.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                ")")
                .setLong("subjectAreaId", subjectAreaId)
                .setString("courseNbr", courseNbr.trim().replaceAll("\\*", "%"))
                .setCacheable(true)
                .list();
    }
    
    public List getStudents() {
        return new ExamDAO().getSession().createQuery(
                "select distinct e.student from " +
                "StudentClassEnrollment e inner join e.clazz c " +
                "inner join c.schedulingSubpart.instrOfferingConfig ioc " +
                "inner join ioc.instructionalOffering io " +
                "inner join io.courseOfferings co, " +
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
    }
    
    public Set effectivePreferences(Class type) {
        if (DistributionPref.class.equals(type)) {
            TreeSet prefs = new TreeSet();
            for (Iterator j=getDistributionObjects().iterator();j.hasNext();) {
                DistributionPref p = ((DistributionObject)j.next()).getDistributionPref();
                prefs.add(p);
            }
            return prefs;
        } else return super.effectivePreferences(type);
    }
}