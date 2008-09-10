package org.unitime.timetable.test;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;

public class MakeCurriculaFromLastlikeDemands {
    protected static Logger sLog = Logger.getLogger(MakeCurriculaFromLastlikeDemands.class);
    private Long iSessionId = null;
    private float iShareLimit = 0.05f;
    private int iEnrlLimit = 20;
    
    public MakeCurriculaFromLastlikeDemands(Long sessionId, float shareLimit, int enrlLimit) {
        iSessionId = sessionId;
        iShareLimit = shareLimit;
        iEnrlLimit = enrlLimit;
    }
    
    public MakeCurriculaFromLastlikeDemands(Long sessionId) {
        this(sessionId,
             Float.parseFloat(ApplicationProperties.getProperty("tmtbl.curriculum.lldemands.shareLimit", "0.05")),
             Integer.parseInt(ApplicationProperties.getProperty("tmtbl.curriculum.lldemands.enrlLimit", "20")));
    }
    
    public Hashtable<AcademicArea, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> loadLastLikeCurricula(org.hibernate.Session hibSession) {
        Hashtable<AcademicArea, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> curricula = new Hashtable();
        List demands = (List)hibSession.createQuery(
                "select a, c, d.student.uniqueId from LastLikeCourseDemand d inner join d.student.academicAreaClassifications a, CourseOffering c where "+
                "d.subjectArea.session.uniqueId=:sessionId and c.subjectArea=d.subjectArea and "+
                "((d.coursePermId=null and c.courseNbr=d.courseNbr) or "+
                " (d.coursePermId!=null and d.coursePermId=c.permId))")
                .setLong("sessionId", iSessionId)
                .setFetchSize(1000)
                .list();
        sLog.info("Processing "+demands.size()+" demands...");
        for (Iterator i=demands.iterator();i.hasNext();) {
            Object o[] = (Object[])i.next();
            AcademicAreaClassification a = (AcademicAreaClassification)o[0];
            CourseOffering c = (CourseOffering)o[1];
            Long s = (Long)o[2];
            Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>> clasf = curricula.get(a.getAcademicArea());
            if (clasf==null) {
                clasf = new Hashtable();
                curricula.put(a.getAcademicArea(), clasf);
            }
            Hashtable<CourseOffering, Set<Long>> courses = clasf.get(a.getAcademicClassification());
            if (courses==null) {
                courses = new Hashtable();
                clasf.put(a.getAcademicClassification(), courses);
            }
            Set<Long> students = courses.get(c);
            if (students==null) {
                students = new HashSet();
                courses.put(c, students);
            }
            students.add(s);
        }
        return curricula;
    }
    
    private void sortCourses(Set<CurriculumCourse> courses) {
        int ord = 0;
        for (CurriculumCourse c : new TreeSet<CurriculumCourse>(courses))
            c.setOrd(ord++);
    }
    
    private void sortClassifications(Set<CurriculumClassification> classf) {
        int ord = 0;
        for (CurriculumClassification c : new TreeSet<CurriculumClassification>(classf))
            c.setOrd(ord++);
    }

    public void update(org.hibernate.Session hibSession) {
        Hashtable<AcademicArea, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> curricula = loadLastLikeCurricula(hibSession);
        Hashtable<AcademicArea, Curriculum> remainingCurricula = new Hashtable();
        for (Iterator i=hibSession.
                createQuery("select c from Curriculum c where c.academicArea!=null and c.department.session=:sessionId").
                setLong("sessionId", iSessionId).iterate();i.hasNext();) {
            Curriculum c = (Curriculum)i.next();
            remainingCurricula.put(c.getAcademicArea(), c);
        }
        for (Map.Entry<AcademicArea, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> e1 : curricula.entrySet()) {
            Curriculum curriculum = remainingCurricula.get(e1.getKey());
            sLog.info("Updating curriculum "+e1.getKey().getAcademicAreaAbbreviation()+" ("+e1.getKey().getShortTitle()+")");
            Hashtable<Department,Integer> deptCounter = null;
            Hashtable<AcademicClassification, CurriculumClassification> remainingClassifications = new Hashtable();
            if (curriculum==null) {
                curriculum = new Curriculum();
                curriculum.setAcademicArea(e1.getKey());
                curriculum.setAbbv(e1.getKey().getAcademicAreaAbbreviation());
                curriculum.setName(e1.getKey().getShortTitle()==null?e1.getKey().getLongTitle():e1.getKey().getShortTitle());
                curriculum.setClassifications(new HashSet());
                deptCounter = new Hashtable();
            } else {
                remainingCurricula.remove(curriculum.getAcademicArea());
                for (Iterator i=curriculum.getClassifications().iterator();i.hasNext();) {
                    CurriculumClassification cc = (CurriculumClassification)i.next();
                    remainingClassifications.put(cc.getAcademicClassification(), cc);
                }
            }
            for (Map.Entry<AcademicClassification, Hashtable<CourseOffering, Set<Long>>> e2 : e1.getValue().entrySet()) {
                CurriculumClassification clasf = null;
                for (Iterator i=curriculum.getClassifications().iterator();i.hasNext();) {
                    CurriculumClassification cc = (CurriculumClassification)i.next();
                    if (e2.getKey().equals(cc.getAcademicClassification())) { clasf = cc; break; }
                }
                Hashtable<CourseOffering, CurriculumCourse> remainingCourses = new Hashtable();
                if (clasf==null) {
                    clasf = new CurriculumClassification();
                    clasf.setCurriculum(curriculum); curriculum.getClassifications().add(clasf);
                    clasf.setAcademicClassification(e2.getKey());
                    clasf.setName(e2.getKey().getCode());
                    clasf.setCourses(new HashSet());
                } else {
                    for (Iterator i=clasf.getCourses().iterator();i.hasNext();) {
                        CurriculumCourse c = (CurriculumCourse)i.next();
                        remainingCourses.put(c.getCourse(), c);
                    }
                    remainingClassifications.remove(clasf.getAcademicClassification());
                }
                Set<Long> allStudents = new HashSet();
                for (Set<Long> students : e2.getValue().values()) allStudents.addAll(students);
                sLog.info("  "+e2.getKey().getCode()+" ("+e2.getKey().getName()+") -- "+allStudents.size()+" students");
                if (clasf.getNrStudents()==null) clasf.setNrStudents(allStudents.size());
                for (Map.Entry<CourseOffering, Set<Long>> e3 : e2.getValue().entrySet()) {
                    CurriculumCourse course = null;
                    for (Iterator i=clasf.getCourses().iterator();i.hasNext();) {
                        CurriculumCourse c = (CurriculumCourse)i.next();
                        if (c.getCourse().equals(e3.getKey())) {
                            course = c; break;
                        }
                    }
                    float share = ((float)e3.getValue().size())/allStudents.size();
                    //sLog.info("      "+e3.getKey().getCourseName()+" has "+e3.getValue().size()+" students ("+new DecimalFormat("0.0").format(100.0*share)+"%)");
                    if (course==null) {
                        if (share<iShareLimit && e3.getValue().size()<iEnrlLimit) continue;
                        course = new CurriculumCourse();
                        course.setClassification(clasf); clasf.getCourses().add(course);
                        course.setCourse(e3.getKey());
                    } else {
                        remainingCourses.remove(course.getCourse());
                    }
                    if (course.getPercShare()==null) course.setPercShare(share);
                    if (deptCounter!=null) {
                        Integer cx = deptCounter.get(course.getCourse().getDepartment());
                        deptCounter.put(course.getCourse().getDepartment(), new Integer(1+(cx==null?0:cx.intValue())));
                    }
                }
            }
            sortClassifications(curriculum.getClassifications());
            for (Iterator i=curriculum.getClassifications().iterator();i.hasNext();) {
                CurriculumClassification clasf = (CurriculumClassification)i.next();
                sortCourses(clasf.getCourses());
            }
            if (deptCounter!=null) {
                Department dept = null; int best = 0;
                for (Map.Entry<Department,Integer> e2 : deptCounter.entrySet()) {
                    if (dept==null || best<e2.getValue()) {
                        dept = e2.getKey(); best = e2.getValue();
                    }
                }
                curriculum.setDepartment(dept);
            }
            hibSession.saveOrUpdate(curriculum);
        }
    }
    
    public static void main(String args[]) {
        try {
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "DEBUG, A1");
            props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
            props.setProperty("log4j.logger.org.hibernate","INFO");
            props.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            props.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            props.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            props.setProperty("log4j.logger.net","INFO");
            PropertyConfigurator.configure(props);
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

            org.hibernate.Session hibSession = new _RootDAO().getSession();
            
            Transaction tx = null;
            try {
                tx = hibSession.beginTransaction();

                Session session = Session.getSessionUsingInitiativeYearTerm(
                        ApplicationProperties.getProperty("initiative", "puWestLafayetteTrdtn"),
                        ApplicationProperties.getProperty("year","2008"),
                        ApplicationProperties.getProperty("term","Spr")
                        );
                
                if (session==null) {
                    sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                    System.exit(0);
                } else {
                    sLog.info("Session: "+session);
                }
                
                new MakeCurriculaFromLastlikeDemands(session.getUniqueId()).update(hibSession);
                
                tx.commit();
            } catch (Exception e) {
                if (tx!=null) tx.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
