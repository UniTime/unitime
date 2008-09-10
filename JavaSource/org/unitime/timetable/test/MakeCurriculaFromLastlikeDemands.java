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
import org.unitime.timetable.model.Curricula;
import org.unitime.timetable.model.CurriculaClassification;
import org.unitime.timetable.model.CurriculaCourse;
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
             Float.parseFloat(ApplicationProperties.getProperty("tmtbl.curricula.lldemands.shareLimit", "0.05")),
             Integer.parseInt(ApplicationProperties.getProperty("tmtbl.curricula.lldemands.enrlLimit", "20")));
    }
    
    public Hashtable<AcademicArea, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> loadLastLikeCurriculas(org.hibernate.Session hibSession) {
        Hashtable<AcademicArea, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> curriculas = new Hashtable();
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
            Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>> clasf = curriculas.get(a.getAcademicArea());
            if (clasf==null) {
                clasf = new Hashtable();
                curriculas.put(a.getAcademicArea(), clasf);
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
        return curriculas;
    }
    
    private void sortCourses(Set<CurriculaCourse> courses) {
        int ord = 0;
        for (CurriculaCourse c : new TreeSet<CurriculaCourse>(courses))
            c.setOrd(ord++);
    }
    
    private void sortClassifications(Set<CurriculaClassification> classf) {
        int ord = 0;
        for (CurriculaClassification c : new TreeSet<CurriculaClassification>(classf))
            c.setOrd(ord++);
    }

    public void update(org.hibernate.Session hibSession) {
        Hashtable<AcademicArea, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> curriculas = loadLastLikeCurriculas(hibSession);
        Hashtable<AcademicArea, Curricula> remainingCurriculas = new Hashtable();
        for (Iterator i=hibSession.
                createQuery("select c from Curricula c where c.academicArea!=null and c.department.session=:sessionId").
                setLong("sessionId", iSessionId).iterate();i.hasNext();) {
            Curricula c = (Curricula)i.next();
            remainingCurriculas.put(c.getAcademicArea(), c);
        }
        for (Map.Entry<AcademicArea, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> e1 : curriculas.entrySet()) {
            Curricula curricula = remainingCurriculas.get(e1.getKey());
            sLog.info("Updating curricula "+e1.getKey().getAcademicAreaAbbreviation()+" ("+e1.getKey().getShortTitle()+")");
            Hashtable<Department,Integer> deptCounter = null;
            Hashtable<AcademicClassification, CurriculaClassification> remainingClassifications = new Hashtable();
            if (curricula==null) {
                curricula = new Curricula();
                curricula.setAcademicArea(e1.getKey());
                curricula.setAbbv(e1.getKey().getAcademicAreaAbbreviation());
                curricula.setName(e1.getKey().getShortTitle()==null?e1.getKey().getLongTitle():e1.getKey().getShortTitle());
                curricula.setClassifications(new HashSet());
                deptCounter = new Hashtable();
            } else {
                remainingCurriculas.remove(curricula.getAcademicArea());
                for (Iterator i=curricula.getClassifications().iterator();i.hasNext();) {
                    CurriculaClassification cc = (CurriculaClassification)i.next();
                    remainingClassifications.put(cc.getAcademicClassification(), cc);
                }
            }
            for (Map.Entry<AcademicClassification, Hashtable<CourseOffering, Set<Long>>> e2 : e1.getValue().entrySet()) {
                CurriculaClassification clasf = null;
                for (Iterator i=curricula.getClassifications().iterator();i.hasNext();) {
                    CurriculaClassification cc = (CurriculaClassification)i.next();
                    if (e2.getKey().equals(cc.getAcademicClassification())) { clasf = cc; break; }
                }
                Hashtable<CourseOffering, CurriculaCourse> remainingCourses = new Hashtable();
                if (clasf==null) {
                    clasf = new CurriculaClassification();
                    clasf.setCurricula(curricula); curricula.getClassifications().add(clasf);
                    clasf.setAcademicClassification(e2.getKey());
                    clasf.setName(e2.getKey().getCode());
                    clasf.setCourses(new HashSet());
                } else {
                    for (Iterator i=clasf.getCourses().iterator();i.hasNext();) {
                        CurriculaCourse c = (CurriculaCourse)i.next();
                        remainingCourses.put(c.getCourse(), c);
                    }
                    remainingClassifications.remove(clasf.getAcademicClassification());
                }
                Set<Long> allStudents = new HashSet();
                for (Set<Long> students : e2.getValue().values()) allStudents.addAll(students);
                sLog.info("  "+e2.getKey().getCode()+" ("+e2.getKey().getName()+") -- "+allStudents.size()+" students");
                if (clasf.getNrStudents()==null) clasf.setNrStudents(allStudents.size());
                for (Map.Entry<CourseOffering, Set<Long>> e3 : e2.getValue().entrySet()) {
                    CurriculaCourse course = null;
                    for (Iterator i=clasf.getCourses().iterator();i.hasNext();) {
                        CurriculaCourse c = (CurriculaCourse)i.next();
                        if (c.getCourse().equals(e3.getKey())) {
                            course = c; break;
                        }
                    }
                    float share = ((float)e3.getValue().size())/allStudents.size();
                    //sLog.info("      "+e3.getKey().getCourseName()+" has "+e3.getValue().size()+" students ("+new DecimalFormat("0.0").format(100.0*share)+"%)");
                    if (course==null) {
                        if (share<iShareLimit && e3.getValue().size()<iEnrlLimit) continue;
                        course = new CurriculaCourse();
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
            sortClassifications(curricula.getClassifications());
            for (Iterator i=curricula.getClassifications().iterator();i.hasNext();) {
                CurriculaClassification clasf = (CurriculaClassification)i.next();
                sortCourses(clasf.getCourses());
            }
            if (deptCounter!=null) {
                Department dept = null; int best = 0;
                for (Map.Entry<Department,Integer> e2 : deptCounter.entrySet()) {
                    if (dept==null || best<e2.getValue()) {
                        dept = e2.getKey(); best = e2.getValue();
                    }
                }
                curricula.setDepartment(dept);
            }
            hibSession.saveOrUpdate(curricula);
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
