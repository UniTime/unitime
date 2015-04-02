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
package org.unitime.timetable.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseExam;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class Exam extends BaseExam implements Comparable<Exam> {
	private static final long serialVersionUID = 1L;
	protected static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);


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
	
	public static final String sSeatingTypes[] = new String[] {MSG.seatingNormal(), MSG.seatingExam()};
	
	public String generateName() {
        StringBuffer sb = new StringBuffer();
        ExamOwner prev = null;
        TreeSet owners = new TreeSet(getOwners());
        if (ApplicationProperty.ExaminationNameExpandCrossListedOfferingsToCourses.isTrue()) {
        	HashSet dummies = new HashSet();
        	for (Iterator i=owners.iterator();i.hasNext();) {
                ExamOwner owner = (ExamOwner)i.next();
                if (owner.getOwnerType() == ExamOwner.sOwnerTypeCourse) continue;
                InstructionalOffering offering = (InstructionalOffering)owner.getCourse().getInstructionalOffering();
                if (offering.getCourseOfferings().size() > 1) {
                	i.remove();
                	for (Iterator j=offering.getCourseOfferings().iterator(); j.hasNext();) {
                		CourseOffering course = (CourseOffering)j.next();
                		ExamOwner dummy = new ExamOwner();
                		dummy.setOwnerId(owner.getOwnerId());
                		dummy.setOwnerType(owner.getOwnerType());
                		dummy.setCourse(course);
                		dummies.add(dummy);
                	}
                }
            }
        	owners.addAll(dummies);
        }
        for (Iterator i=owners.iterator();i.hasNext();) {
            ExamOwner owner = (ExamOwner)i.next();
            Object ownerObject = owner.getOwnerObject();
            if (prev!=null && prev.getCourse().getSubjectArea().equals(owner.getCourse().getSubjectArea())) {
                //same subject area
                if (prev.getCourse().equals(owner.getCourse()) && prev.getOwnerType().equals(owner.getOwnerType())) {
                    //same course number
                    switch (owner.getOwnerType()) {
                    case ExamOwner.sOwnerTypeClass :
                        Class_ clazz = (Class_)ownerObject;
                        if (prev.getOwnerType()==ExamOwner.sOwnerTypeClass && ((Class_)prev.getOwnerObject()).getSchedulingSubpart().equals(clazz.getSchedulingSubpart()))
                            sb.append(owner.genName(ApplicationProperties.getProperty("tmtbl.exam.name.sameSubpart."+ExamOwner.sOwnerTypes[owner.getOwnerType()])));
                        else
                            sb.append(owner.genName(ApplicationProperties.getProperty("tmtbl.exam.name.sameCourse."+ExamOwner.sOwnerTypes[owner.getOwnerType()])));
                        break;
                    case ExamOwner.sOwnerTypeConfig :
                        sb.append(owner.genName(ApplicationProperties.getProperty("tmtbl.exam.name.sameCourse."+ExamOwner.sOwnerTypes[owner.getOwnerType()])));
                        break;
                    }
                } else {
                    //different course number
                    sb.append(owner.genName(ApplicationProperties.getProperty("tmtbl.exam.name.sameSubject."+ExamOwner.sOwnerTypes[owner.getOwnerType()])));
                }
            } else {
                //different subject area
                if (prev!=null) sb.append(prev.genName(ApplicationProperty.ExamNameSeparator.value()));
                sb.append(owner.genName(ApplicationProperties.getProperty("tmtbl.exam.name."+ExamOwner.sOwnerTypes[owner.getOwnerType()])));
            }
            prev = owner;
        }
        String suffix = (prev==null?"":prev.genName(ApplicationProperty.ExamNameSuffix.value()));
        int limit = ApplicationProperty.ExamNameMaxLength.intValue() - suffix.length();
        return (sb.toString().length()<=limit?sb.toString():sb.toString().substring(0,limit-3)+"...")+suffix;
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
	
	public static List findAll(Long sessionId, ExamType examType) {
		return findAll(sessionId, examType.getUniqueId());
	}
	
	public static List findAll(Long sessionId, Long examTypeId) {
	    return new ExamDAO().getSession().createQuery(
	            "select x from Exam x where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId"
	            )
	            .setLong("sessionId", sessionId)
	            .setLong("examTypeId", examTypeId)
	            .setCacheable(true)
	            .list();
	}
	
	public static List findAllMidterm(Long sessionId) {
	    return new ExamDAO().getSession().createQuery(
	            "select x from Exam x where x.session.uniqueId=:sessionId and x.examType.type=:type"
	            )
	            .setLong("sessionId", sessionId)
	            .setInteger("type", ExamType.sExamTypeMidterm)
	            .setCacheable(true)
	            .list();
	}
	
	public static List findAllFinal(Long sessionId) {
	    return new ExamDAO().getSession().createQuery(
	            "select x from Exam x where x.session.uniqueId=:sessionId and x.examType.type=:type"
	            )
	            .setLong("sessionId", sessionId)
	            .setInteger("type", ExamType.sExamTypeFinal)
	            .setCacheable(true)
	            .list();
	}
	
    public static List findExamsOfSubjectArea(Long subjectAreaId, Long examTypeId) {
        return new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.subjectArea.uniqueId=:subjectAreaId and x.examType.uniqueId=:examTypeId")
                .setLong("subjectAreaId", subjectAreaId)
                .setLong("examTypeId", examTypeId)
                .setCacheable(true)
                .list();
    }
    
    public static List findExamsOfSubjectAreaIncludeCrossLists(Long subjectAreaId, Long examTypeId) {
        return new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o inner join o.course.instructionalOffering.courseOfferings co where " +
                "co.subjectArea.uniqueId=:subjectAreaId and x.examType.uniqueId=:examTypeId")
                .setLong("subjectAreaId", subjectAreaId)
                .setLong("examTypeId", examTypeId)
                .setCacheable(true)
                .list();
    }

    public static List findExamsOfCourseOffering(Long courseOfferingId, Long examTypeId) {
        return new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.uniqueId=:courseOfferingId and x.examType.uniqueId=:examTypeId")
                .setLong("courseOfferingId", courseOfferingId)
                .setLong("examTypeId", examTypeId)
                .setCacheable(true)
                .list();
    }
    
    public static List findExamsOfCourse(Long subjectAreaId, String courseNbr, Long examTypeId) {
        if (courseNbr==null || courseNbr.trim().length()==0) return findExamsOfSubjectArea(subjectAreaId, examTypeId);
        return new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.subjectArea.uniqueId=:subjectAreaId and x.examType.uniqueId=:examTypeId and "+
                (courseNbr.indexOf('*')>=0?"o.course.courseNbr like :courseNbr":"o.course.courseNbr=:courseNbr"))
                .setLong("subjectAreaId", subjectAreaId)
                .setString("courseNbr", courseNbr.trim().replaceAll("\\*", "%"))
                .setLong("examTypeId", examTypeId)
                .setCacheable(true)
                .list();
    }
    
    public Set<Student> getStudents() {
        HashSet<Student> students = new HashSet();
        for (Iterator i=getOwners().iterator();i.hasNext();)
            students.addAll(((ExamOwner)i.next()).getStudents());
        return students;
    }
    
    public Collection<StudentClassEnrollment> getStudentClassEnrollments() {
        HashSet<StudentClassEnrollment> enrollments = new HashSet();
        for (ExamOwner owner: getOwners())
            enrollments.addAll(owner.getStudentClassEnrollments());
        return enrollments;
    }
    
    public Set<Long> getStudentIds() {
        HashSet<Long> studentIds = new HashSet();
        for (Iterator i=getOwners().iterator();i.hasNext();)
            studentIds.addAll(((ExamOwner)i.next()).getStudentIds());
        return studentIds;
    }

    public Hashtable<Long, Set<Exam>> getStudentExams() {
        Hashtable<Long, Set<Exam>> studentExams = new Hashtable<Long, Set<Exam>>();
        for (Iterator i=getOwners().iterator();i.hasNext();) {
            ExamOwner owner = (ExamOwner)i.next();
            owner.computeStudentExams(studentExams);
        }
        return studentExams;
    }
    
    public Hashtable<Assignment, Set<Long>> getStudentAssignments() {
        Hashtable<Assignment, Set<Long>> studentAssignments = new Hashtable<Assignment, Set<Long>>();
        for (Iterator i=getOwners().iterator();i.hasNext();) {
            ExamOwner owner = (ExamOwner)i.next();
            owner.computeStudentAssignments(studentAssignments);
        }
        return studentAssignments;
    }
    
    public Hashtable<Meeting, Set<Long>> getOverlappingStudentMeetings(Long periodId) {
        Hashtable<Meeting, Set<Long>> studentMeetings = new Hashtable<Meeting, Set<Long>>();
        for (Iterator i=getOwners().iterator();i.hasNext();) {
            ExamOwner owner = (ExamOwner)i.next();
            owner.computeOverlappingStudentMeetings(studentMeetings, periodId);
        }
        return studentMeetings;
    }

    public int countStudents() {
        int nrStudents = 0;
        for (Iterator i=getOwners().iterator();i.hasNext();)
            nrStudents += ((ExamOwner)i.next()).countStudents();
        return nrStudents;
    }
    
    public int getLimit() {
        int limit = 0;
        for (Iterator i=getOwners().iterator();i.hasNext();)
            limit += ((ExamOwner)i.next()).getLimit();
        return limit;
    }

    public int getSize() {
        if (getExamSize()!=null) return getExamSize().intValue();
        int size = 0;
        for (Iterator i=getOwners().iterator();i.hasNext();)
            size += ((ExamOwner)i.next()).getSize();
        return size;
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
        
        ExamEvent event = getEvent();
        if (event!=null) {
            hibSession.delete(event);
            deleted = true;
        }

        if (deleted && updateExam)
            hibSession.saveOrUpdate(this);
    }
    
    public static void deleteFromExams(org.hibernate.Session hibSession, Integer ownerType, Long ownerId) {
        for (Iterator i=hibSession.createQuery("select o from Exam x inner join x.owners o where "+
                "o.ownerType=:ownerType and o.ownerId=:ownerId")
                .setInteger("ownerType", ownerType)
                .setLong("ownerId", ownerId).list().iterator();i.hasNext();) {
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
        } else if ("DepartmentalInstructor".equals(type)) {
            return new ExamDAO().getSession().createQuery(
                    "select distinct x from Exam x inner join x.instructors xi, DepartmentalInstructor i where "+
                    "i.uniqueId=:instructorId and (xi.uniqueId=i.uniqueId or ("+
                    "i.externalUniqueId is not null and i.externalUniqueId=xi.externalUniqueId " +
                    "and xi.department.session = i.department.session))").
                    setLong("instructorId", id).setCacheable(true).list();
        } else if ("ExamEvent".equals(type)) {
            List ret = new ArrayList();
            ExamEvent event = new ExamEventDAO().get(id);
            if (event!=null && event.getExam()!=null) ret.add(event.getExam());
            return ret;
        } else throw new RuntimeException("Unsupported type "+type);
    }
    
    public static boolean hasTimetable(Long sessionId, Integer examType) {
    	if (sessionId == null) return false;
    	if (examType==null) return hasTimetable(sessionId);
        return ((Number)new ExamDAO().getSession().
                createQuery("select count(x) from Exam x " +
                		"where x.session.uniqueId=:sessionId and " +
                		"x.assignedPeriod!=null and x.examType.type=:examType").
                setLong("sessionId",sessionId).setInteger("examType",examType).setCacheable(true).uniqueResult()).longValue()>0;
    }
    
    public static boolean hasTimetable(Long sessionId) {
    	if (sessionId == null) return false;
        return ((Number)new ExamDAO().getSession().
                createQuery("select count(x) from Exam x " +
                		"where x.session.uniqueId=:sessionId and " +
                		"x.assignedPeriod!=null").
                setLong("sessionId",sessionId).setCacheable(true).uniqueResult()).longValue()>0;
    }

    public static boolean hasMidtermExams(Long sessionId) {
    	if (sessionId == null) return false;
    	return ((Number)new ExamDAO().getSession().
    			createQuery("select count(p) from ExamPeriod p " +
    					"where p.session.uniqueId=:sessionId and "+
    					"p.examType.type = "+ExamType.sExamTypeMidterm).
    			setLong("sessionId", sessionId).setCacheable(true).uniqueResult()).longValue()>0;
    }
    
    public static boolean hasFinalExams(Long sessionId) {
    	if (sessionId == null) return false;
        return ((Number)new ExamDAO().getSession().
                createQuery("select count(p) from ExamPeriod p " +
                        "where p.session.uniqueId=:sessionId and "+
                        "p.examType.type = "+ExamType.sExamTypeFinal).
                setLong("sessionId", sessionId).setCacheable(true).uniqueResult()).longValue()>0;
    }
    
    public static boolean hasExamsOfType(Long sessionId, ExamType type) {
    	if (sessionId == null) return false;
        return ((Number)new ExamDAO().getSession().
                createQuery("select count(p) from ExamPeriod p " +
                        "where p.session.uniqueId=:sessionId and "+
                        "p.examType.uniqueid = :typeId").
                setLong("sessionId", sessionId).setLong("typeId", type.getUniqueId()).setCacheable(true).uniqueResult()).longValue()>0;
    }

    public static Collection<ExamAssignmentInfo> findAssignedExams(Long sessionId, Long examTypeId) {
        Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select x from Exam x where "+
                "x.session.uniqueId=:sessionId and x.assignedPeriod!=null and x.examType.uniqueId=:examTypeId").
                setLong("sessionId", sessionId).
                setLong("examTypeId", examTypeId).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamAssignmentInfo(exam));
        } 
        return ret;
    }
    
    public static Collection<ExamInfo> findUnassignedExams(Long sessionId, Long examTypeId) {
        Vector<ExamInfo> ret = new Vector<ExamInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select x from Exam x where "+
                "x.session.uniqueId=:sessionId and x.assignedPeriod=null and x.examType.uniqueId=:examTypeId").
                setLong("sessionId", sessionId).
                setLong("examTypeId", examTypeId).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamInfo(exam));
        } 
        return ret;
    }
    
    public static Collection<ExamAssignmentInfo> findAssignedExams(Long sessionId, Long subjectAreaId, Long examTypeId) {
        if (subjectAreaId==null || subjectAreaId<0) return findAssignedExams(sessionId, examTypeId);
        Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.subjectArea.uniqueId=:subjectAreaId and "+
                "x.examType.uniqueId=:examTypeId and "+
                "x.session.uniqueId=:sessionId and x.assignedPeriod!=null").
                setLong("sessionId", sessionId).
                setLong("examTypeId", examTypeId).
                setLong("subjectAreaId", subjectAreaId).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamAssignmentInfo(exam));
        } 
        return ret;
    }
    
    public static Collection<ExamInfo> findUnassignedExams(Long sessionId, Long subjectAreaId, Long examTypeId) {
        if (subjectAreaId==null || subjectAreaId<0) return findUnassignedExams(sessionId,examTypeId);
        Vector<ExamInfo> ret = new Vector<ExamInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.subjectArea.uniqueId=:subjectAreaId and "+
                "x.examType.uniqueId=:examTypeId and "+
                "x.session.uniqueId=:sessionId and x.assignedPeriod=null").
                setLong("sessionId", sessionId).
                setLong("examTypeId", examTypeId).
                setLong("subjectAreaId", subjectAreaId).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamInfo(exam));
        } 
        return ret;
    }
    
    public static Collection<ExamAssignmentInfo> findAssignedExamsOfLocation(Long locationId, Long examTypeId) throws Exception {
        Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.assignedRooms r where " +
                "r.uniqueId=:locationId and x.assignedPeriod!=null and "+
                "x.examType.uniqueId=:examTypeId").
                setLong("locationId", locationId).
                setLong("examTypeId", examTypeId).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamAssignmentInfo(exam));
        } 
        return ret;
    }
    
    public static Collection<ExamAssignmentInfo> findAssignedExamsOfInstructor(Long instructorId, Long examTypeId) throws Exception {
        Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
        List exams = new ExamDAO().getSession().createQuery(
                "select distinct x from Exam x inner join x.instructors i where " +
                "i.uniqueId=:instructorId and x.assignedPeriod!=null and "+
                "x.examType.uniqueId=:examTypeId").
                setLong("instructorId", instructorId).
                setLong("examTypeId", examTypeId).
                setCacheable(true).list();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            ret.add(new ExamAssignmentInfo(exam));
        } 
        return ret;
    }
    
    public String assign(ExamAssignmentInfo assignment, String managerExternalId, Session hibSession) {
        Transaction tx = null;
        try {
            tx = hibSession.beginTransaction();
            
            ExamAssignment oldAssignment = new ExamAssignment(this); 
            
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
                if (dc.getOtherExam()==null) continue;
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
                if (dc.getOtherExam()==null) continue;
                ExamConflict conf = new ExamConflict();
                conf.setConflictType(ExamConflict.sConflictTypeDirect);
                conf.setInstructors(getInstructors(hibSession, dc.getStudents()));
                conf.setNrInstructors(conf.getInstructors().size());
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
                conf.setInstructors(getInstructors(hibSession, btb.getStudents()));
                conf.setNrInstructors(conf.getInstructors().size());
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
                conf.setInstructors(getInstructors(hibSession, m2d.getStudents()));
                conf.setNrInstructors(conf.getInstructors().size());
                hibSession.save(conf);
                getConflicts().add(conf);
                for (Iterator j=m2d.getOtherExams().iterator();j.hasNext();) {
                    Exam otherExam = (Exam)((ExamInfo)j.next()).getExam(hibSession);
                    otherExam.getConflicts().add(conf);
                    otherExams.add(otherExam);
                }
            }
            
            ExamEvent event = generateEvent(getEvent(),true);
            if (event!=null) {
                event.setEventName(assignment.getExamName());
                event.setMinCapacity(assignment.getNrStudents());
                event.setMaxCapacity(assignment.getNrStudents());
                EventContact contact = EventContact.findByExternalUniqueId(managerExternalId);
                if (contact==null) {
                    TimetableManager manager = TimetableManager.findByExternalId(managerExternalId);
                    contact = new EventContact();
                    contact.setFirstName(manager.getFirstName());
                    contact.setMiddleName(manager.getMiddleName());
                    contact.setLastName(manager.getLastName());
                    contact.setExternalUniqueId(manager.getExternalUniqueId());
                    contact.setEmailAddress(manager.getEmailAddress());
                    hibSession.save(contact);
                }
                event.setMainContact(contact);
                hibSession.saveOrUpdate(event);
            }
            
            hibSession.update(this);
            for (Iterator i=otherExams.iterator();i.hasNext();)
                hibSession.update((Exam)i.next());
            
            SubjectArea subject = null;
            Department dept = null;
            for (Iterator i=new TreeSet(getOwners()).iterator();i.hasNext();) {
                ExamOwner owner = (ExamOwner)i.next();
                subject = owner.getCourse().getSubjectArea();
                dept = subject.getDepartment();
                break;
            }

            ChangeLog.addChange(hibSession,
                    TimetableManager.findByExternalId(managerExternalId),
                    getSession(),
                    this,
                    assignment.getExamName()+" ("+
                        (oldAssignment.getPeriod()==null?"N/A":oldAssignment.getPeriodAbbreviation()+" "+oldAssignment.getRoomsName(", "))+
                        " &rarr; "+assignment.getPeriodAbbreviation()+" "+assignment.getRoomsName(", ")+")",
                    ChangeLog.Source.EXAM_INFO,
                    ChangeLog.Operation.ASSIGN,
                    subject,
                    dept);

            tx.commit();
            return null;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
            return "Assignment of "+assignment.getExamName()+" to "+assignment.getPeriodAbbreviation()+" "+assignment.getRoomsName(", ")+" failed, reason: "+e.getMessage();
        }
    }
    
    public String unassign(String managerExternalId, Session hibSession) {
        Transaction tx = null;
        try {
            if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                tx = hibSession.beginTransaction();
            
            ExamAssignment oldAssignment = new ExamAssignment(this);
            
            setAssignedPeriod(null);
            if (getAssignedRooms()==null) setAssignedRooms(new HashSet());
            getAssignedRooms().clear();
            setAssignedPreference(null);
            
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

            ExamEvent event = getEvent();
            if (event!=null) hibSession.delete(event);
            
            hibSession.update(this);
            for (Iterator i=otherExams.iterator();i.hasNext();)
                hibSession.update((Exam)i.next());
            
            SubjectArea subject = null;
            Department dept = null;
            for (Iterator i=new TreeSet(getOwners()).iterator();i.hasNext();) {
                ExamOwner owner = (ExamOwner)i.next();
                subject = owner.getCourse().getSubjectArea();
                dept = subject.getDepartment();
                break;
            }
            
            ChangeLog.addChange(hibSession,
                    TimetableManager.findByExternalId(managerExternalId),
                    getSession(),
                    this,
                    getName()+" ("+
                    (oldAssignment.getPeriod()==null?"N/A":oldAssignment.getPeriodAbbreviation()+" "+oldAssignment.getRoomsName(", "))+
                    " &rarr; N/A)",
                    ChangeLog.Source.EXAM_INFO,
                    ChangeLog.Operation.UNASSIGN,
                    subject,
                    dept);

            if (tx!=null) tx.commit();
            return null;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
            return "Unassignment of "+getName()+" failed, reason: "+e.getMessage();
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
    
    public int examOffset() {
    	return (getPrintOffset() == null ? 0 : getPrintOffset());
    }
    
    public ExamEvent generateEvent(ExamEvent event, boolean createNoRoomMeetings) {
        ExamPeriod period = getAssignedPeriod();
        if (period==null) return null;
        if (event==null) {
        	if (getSession().getStatusType().isTestSession()) return null;
            event = (getExamType().getType()==ExamType.sExamTypeFinal?new FinalExamEvent():new MidtermExamEvent());
            event.setExam(this);
            setEvent(event);
        }
        if (event.getMeetings()!=null) 
            event.getMeetings().clear();
        else 
            event.setMeetings(new HashSet());
        boolean created = false;
        for (Iterator i=getAssignedRooms().iterator();i.hasNext();) {
            Location location = (Location)i.next();
            if (location.getPermanentId()!=null) {
                Meeting m = new Meeting();
                m.setMeetingDate(period.getStartDate());
                m.setStartPeriod(period.getExamEventStartSlot());
                m.setStartOffset(period.getExamEventStartOffsetForExam(this));
                m.setStopPeriod(period.getExamEventStopSlot());
                m.setStopOffset(period.getExamEventStopOffsetForExam(this));
                m.setClassCanOverride(false);
                m.setLocationPermanentId(location.getPermanentId());
                m.setStatus(Meeting.Status.APPROVED);
                m.setApprovalDate(new Date());
                m.setEvent(event);
                event.getMeetings().add(m);
                created = true;
            }
        }
        if (!created && createNoRoomMeetings) {
            Meeting m = new Meeting();
            m.setMeetingDate(period.getStartDate());
            m.setStartPeriod(period.getExamEventStartSlot());
            m.setStartOffset(period.getExamEventStartOffsetForExam(this));
            m.setStopPeriod(period.getExamEventStopSlot());
            m.setStopOffset(period.getExamEventStopOffsetForExam(this));
            m.setClassCanOverride(false);
            m.setLocationPermanentId(null);
            m.setStatus(Meeting.Status.APPROVED);
            m.setApprovalDate(new Date());
            m.setEvent(event);
            event.getMeetings().add(m);
        }
        return event;
    }
    
    public ExamPeriod getAveragePeriod() {
        return ExamPeriod.findByIndex(getSession().getUniqueId(), getExamType(), getAvgPeriod());
    }
    
    public static Exam findByIdRolledForwardFrom(Long sessionId, Long uniqueIdRolledForwardFrom) {
        return (Exam)new ExamDAO().
            getSession().
            createQuery("select e from Exam e where e.session.uniqueId=:sessionId and e.uniqueIdRolledForwardFrom=:uniqueIdRolledForwardFrom").
            setLong("sessionId", sessionId.longValue()).
            setLong("uniqueIdRolledForwardFrom", uniqueIdRolledForwardFrom.longValue()).
            setCacheable(true).
            uniqueResult();
    }
    
    
    public void generateDefaultPreferences(boolean override) {
        Set allPeriods = ExamPeriod.findAll(getSession().getUniqueId(), getExamType());
        
        if (getPreferences()==null) setPreferences(new HashSet());
        
        //Prefer overlapping period for evening classes
        PreferenceLevel eveningPref = PreferenceLevel.getPreferenceLevel(ApplicationProperty.ExamDefaultsEveningClassPreference.value(getExamType().getReference()));
        if (!PreferenceLevel.sNeutral.equals(eveningPref.getPrefProlog()) && (override || getPreferences(ExamPeriodPref.class).isEmpty())) {
            int firstEveningPeriod = ApplicationProperty.ExamDefaultsEveningClassStart.intValue(getExamType().getReference()); 
            HashSet<ExamPeriod> periods = new HashSet();
            for (Iterator i=getOwners().iterator();i.hasNext();) {
                ExamOwner owner = (ExamOwner)i.next();
                if (ExamOwner.sOwnerTypeClass!=owner.getOwnerType()) continue;
                Event event = ((Class_)owner.getOwnerObject()).getEvent();
                if (event==null) continue;
                for (Iterator j=event.getMeetings().iterator();j.hasNext();) {
                    Meeting meeting = (Meeting)j.next();
                    if (meeting.getStopPeriod()<=firstEveningPeriod) continue;
                    ExamPeriod lastPeriod = null;
                    for (Iterator k=allPeriods.iterator();k.hasNext();) {
                        ExamPeriod period = (ExamPeriod)k.next();
                        if (period.getDayOfWeek()!=meeting.getDayOfWeek()) continue;
                        if (lastPeriod==null || lastPeriod.getStartSlot()<period.getStartSlot())
                            lastPeriod = period;
                    }
                    if (lastPeriod!=null) periods.add(lastPeriod);
                }
            }
            if (!periods.isEmpty()) {
                for (Iterator i=getPreferences(ExamPeriodPref.class).iterator();i.hasNext();) {
                    ExamPeriodPref pref = (ExamPeriodPref)i.next();
                    if (periods.contains(pref.getExamPeriod())) {
                        periods.remove(pref.getExamPeriod());
                        if (!pref.getPrefLevel().equals(eveningPref)) {
                            pref.setPrefLevel(eveningPref);
                        }
                    } else {
                        getPreferences().remove(pref);
                    }
                }
                for (ExamPeriod period : periods) {
                    ExamPeriodPref pref = new ExamPeriodPref();
                    pref.setPrefLevel(eveningPref);
                    pref.setOwner(this);
                    pref.setExamPeriod(period);
                    getPreferences().add(pref);
                }
            }
        }
        
        //Prefer original room
        PreferenceLevel originalPref = PreferenceLevel.getPreferenceLevel(ApplicationProperty.ExamDefaultsOriginalRoomPreference.value(getExamType().getReference()));
        if (!PreferenceLevel.sNeutral.equals(originalPref.getPrefProlog()) && (override || getPreferences(RoomPref.class).isEmpty())) {
            HashSet<Location> locations = new HashSet();
            for (Iterator i=getOwners().iterator();i.hasNext();) {
                ExamOwner owner = (ExamOwner)i.next();
                if (ExamOwner.sOwnerTypeClass!=owner.getOwnerType()) continue;
                Event event = ((Class_)owner.getOwnerObject()).getEvent();
                if (event==null) continue;
                for (Iterator j=event.getMeetings().iterator();j.hasNext();) {
                    Meeting meeting = (Meeting)j.next();
                    if (meeting.getLocation()!=null && meeting.getLocation().isExamEnabled(getExamType())) locations.add(meeting.getLocation());
                }
            }
            if (!locations.isEmpty()) {
                for (Iterator i=getPreferences(RoomPref.class).iterator();i.hasNext();) {
                    RoomPref pref = (RoomPref)i.next();
                    if (locations.contains(pref.getRoom())) {
                        locations.remove(pref.getRoom());
                        if (!pref.getPrefLevel().equals(originalPref)) {
                            pref.setPrefLevel(originalPref);
                        }
                    } else {
                        getPreferences().remove(pref);
                    }
                }
                for (Location location : locations) {
                    RoomPref pref = new RoomPref();
                    pref.setPrefLevel(originalPref);
                    pref.setOwner(this);
                    pref.setRoom(location);
                    getPreferences().add(pref);
                }
            }
        }
        
        //Prefer original building
        PreferenceLevel originalBuildingPref = PreferenceLevel.getPreferenceLevel(ApplicationProperty.ExamDefaultsOriginalBuildingPreference.value(getExamType().getReference()));
        boolean examRoomsOnly = ApplicationProperty.ExamDefaultsOriginalBuildingOnlyForExamRooms.isTrue(getExamType().getReference());
        if (!PreferenceLevel.sNeutral.equals(originalBuildingPref.getPrefProlog()) && (override || getPreferences(BuildingPref.class).isEmpty())) {
            HashSet<Building> buildings = new HashSet<Building>();
            for (Iterator i=getOwners().iterator();i.hasNext();) {
                ExamOwner owner = (ExamOwner)i.next();
                if (ExamOwner.sOwnerTypeClass!=owner.getOwnerType()) continue;
                Event event = ((Class_)owner.getOwnerObject()).getEvent();
                if (event==null) continue;
                for (Iterator j=event.getMeetings().iterator();j.hasNext();) {
                    Meeting meeting = (Meeting)j.next();
                    Location location = meeting.getLocation();
                    if (location == null || !(location instanceof Room)) continue;
                    if (examRoomsOnly && !location.isExamEnabled(getExamType())) continue;
            		buildings.add(((Room)location).getBuilding());
                }
            }
            if (!buildings.isEmpty()) {
                for (Iterator i=getPreferences(BuildingPref.class).iterator();i.hasNext();) {
                	BuildingPref pref = (BuildingPref)i.next();
                    if (buildings.contains(pref.getBuilding())) {
                    	buildings.remove(pref.getBuilding());
                        if (!pref.getPrefLevel().equals(originalBuildingPref)) {
                            pref.setPrefLevel(originalBuildingPref);
                        }
                    } else {
                        getPreferences().remove(pref);
                    }
                }
                for (Building building : buildings) {
                	BuildingPref pref = new BuildingPref();
                    pref.setPrefLevel(originalBuildingPref);
                    pref.setOwner(this);
                    pref.setBuilding(building);
                    getPreferences().add(pref);
                }
            }
        }
    }
    
    private ExamEvent iEvent = null;
    public ExamEvent getEvent() {
        if (getUniqueId()==null) return null;
        if (iEvent==null) 
            iEvent = (ExamEvent)new ExamDAO().getSession().createQuery(
                "select e from ExamEvent e left join fetch e.meetings m where e.exam.uniqueId=:examId").
                setLong("examId", getUniqueId()).
                setCacheable(true).uniqueResult();
        return iEvent;
    }
    public void setEvent(ExamEvent event) {
        iEvent = event;
    }
    
    public void updateConflicts(Session hibSession) throws Exception {
        Transaction tx = null;
        try {
            if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                tx = hibSession.beginTransaction();
            
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
            
            ExamAssignmentInfo assignment = new ExamAssignmentInfo(this, false);
            
            for (Iterator i=assignment.getDirectConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.DirectConflict dc = (ExamAssignmentInfo.DirectConflict)i.next();
                if (dc.getOtherExam()==null) continue;
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
                if (dc.getOtherExam()==null) continue;
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
            
            if (tx!=null) tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
    }
    
    public Date getEndTime(ExamPeriod period) {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getSession().getExamBeginDate());
        c.add(Calendar.DAY_OF_YEAR, period.getDateOffset());
        int min = period.getStartSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + examOffset() + getLength();
        c.set(Calendar.HOUR, min/60); c.set(Calendar.MINUTE, min%60);
        return c.getTime();
    }

    public Date getStartTime(ExamPeriod period) {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getSession().getExamBeginDate());
        c.add(Calendar.DAY_OF_YEAR, period.getDateOffset());
        int min = period.getStartSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + examOffset();
        c.set(Calendar.HOUR, min/60); c.set(Calendar.MINUTE, min%60);
        return c.getTime();
    }

	@Override
	public Department getDepartment() { return null; }
	
	public DepartmentStatusType effectiveStatusType() {
		return getStatusType() == null ? getSession().getStatusType() : getStatusType();
	}
	
	public boolean canView() {
		DepartmentStatusType type = effectiveStatusType();
		return type != null && (getExamType().getType() == ExamType.sExamTypeFinal ? type.canNoRoleReportExamFinal() : type.canNoRoleReportExamMidterm());
	}

}
