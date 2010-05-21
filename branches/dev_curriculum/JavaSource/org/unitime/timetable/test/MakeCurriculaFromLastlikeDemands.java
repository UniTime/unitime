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
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;

public class MakeCurriculaFromLastlikeDemands {
    protected static Logger sLog = Logger.getLogger(MakeCurriculaFromLastlikeDemands.class);
    private Long iSessionId = null;
    private float iShareLimit = 0.00f;
    private int iEnrlLimit = 1;
    
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
    
    public Hashtable<AcademicArea, Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>>> loadLastLikeCurricula(org.hibernate.Session hibSession) {
        Hashtable<AcademicArea, Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>>> curricula = new Hashtable();
        List demands = (List)hibSession.createQuery(
                "select a, m, c, d.student.uniqueId from LastLikeCourseDemand d inner join d.student.academicAreaClassifications a inner join d.student.posMajors m, CourseOffering c where "+
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
            PosMajor m = (PosMajor)o[1];
            CourseOffering c = (CourseOffering)o[2];
            Long s = (Long)o[3];
            Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> curacad = curricula.get(a.getAcademicArea());
            if (curacad==null) {
            	curacad = new Hashtable();
            	curricula.put(a.getAcademicArea(), curacad);
            }
            Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>> clasf = curacad.get(m);
            if (clasf==null) {
                clasf = new Hashtable();
                curacad.put(m, clasf);
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
    
    public Hashtable<AcademicArea, Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>>> loadRealCurricula(org.hibernate.Session hibSession) {
        Hashtable<AcademicArea, Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>>> curricula = new Hashtable();
        List demands = (List)hibSession.createQuery(
                "select distinct a, m, c, e.student.uniqueId from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " +
                "inner join e.student.posMajors m inner join e.courseOffering c where "+
                "e.student.session.uniqueId=:sessionId")
                .setLong("sessionId", iSessionId)
                .setFetchSize(1000)
                .list();
        sLog.info("Processing "+demands.size()+" demands...");
        for (Iterator i=demands.iterator();i.hasNext();) {
            Object o[] = (Object[])i.next();
            AcademicAreaClassification a = (AcademicAreaClassification)o[0];
            PosMajor m = (PosMajor)o[1];
            CourseOffering c = (CourseOffering)o[2];
            Long s = (Long)o[3];
            Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> curacad = curricula.get(a.getAcademicArea());
            if (curacad==null) {
            	curacad = new Hashtable();
            	curricula.put(a.getAcademicArea(), curacad);
            }
            Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>> clasf = curacad.get(m);
            if (clasf==null) {
                clasf = new Hashtable();
                curacad.put(m, clasf);
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

    public void update(org.hibernate.Session hibSession, boolean lastLike) {
        Hashtable<AcademicArea, Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>>> curricula = (lastLike ? loadLastLikeCurricula(hibSession) : loadRealCurricula(hibSession));
        Hashtable<AcademicArea, Hashtable<PosMajor, Curriculum>> remainingCurricula = new Hashtable();
        for (Iterator i=hibSession.
                createQuery("select c from Curriculum c where c.academicArea!=null and c.department.session=:sessionId").
                setLong("sessionId", iSessionId).iterate();i.hasNext();) {
            Curriculum c = (Curriculum)i.next();
            Hashtable<PosMajor, Curriculum> majors = new Hashtable<PosMajor, Curriculum>();
            for (Iterator<PosMajor> j = c.getMajors().iterator(); j.hasNext();) {
            	PosMajor m = j.next();
            	majors.put(m, c);
            }
            remainingCurricula.put(c.getAcademicArea(), majors);
        }
        for (Map.Entry<AcademicArea, Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>>> e1 : curricula.entrySet()) {
        	Hashtable<PosMajor, Curriculum> remainingMajors = remainingCurricula.get(e1.getKey());
            for (Map.Entry<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> e2 : e1.getValue().entrySet()) {
            	Curriculum curriculum = (remainingMajors == null ? null : remainingMajors.get(e2.getKey()));
            	if (curriculum != null && curriculum.getMajors().size() > 1) {
            		curriculum.getMajors().remove(e2.getKey());
            		curriculum = null;
            	}
                sLog.info("Updating curriculum "+e1.getKey().getAcademicAreaAbbreviation()+" ("+e1.getKey().getShortTitle()+") - " + e2.getKey().getCode() + " (" + e2.getKey().getName()+ ")");
                Hashtable<Department,Integer> deptCounter = null;
                Hashtable<AcademicClassification, CurriculumClassification> remainingClassifications = new Hashtable();
                if (curriculum==null) {
                    curriculum = new Curriculum();
                    curriculum.setAcademicArea(e1.getKey());
                    curriculum.setAbbv(e1.getKey().getAcademicAreaAbbreviation() + "-" + e2.getKey().getCode());
                    curriculum.setName(e1.getKey().getShortTitle()==null?e1.getKey().getLongTitle():e1.getKey().getShortTitle() + " - " + e2.getKey().getName());
                    curriculum.setClassifications(new HashSet());
                    curriculum.setMajors(new HashSet());
                    curriculum.getMajors().add(e2.getKey());
                    deptCounter = new Hashtable();
                } else {
                	remainingMajors.remove(e2.getKey());
                    for (Iterator i=curriculum.getClassifications().iterator();i.hasNext();) {
                        CurriculumClassification cc = (CurriculumClassification)i.next();
                        remainingClassifications.put(cc.getAcademicClassification(), cc);
                    }
                }
                for (Map.Entry<AcademicClassification, Hashtable<CourseOffering, Set<Long>>> e3 : e2.getValue().entrySet()) {
                    CurriculumClassification clasf = null;
                    for (Iterator i=curriculum.getClassifications().iterator();i.hasNext();) {
                        CurriculumClassification cc = (CurriculumClassification)i.next();
                        if (e3.getKey().equals(cc.getAcademicClassification())) { clasf = cc; break; }
                    }
                    Hashtable<CourseOffering, CurriculumCourse> remainingCourses = new Hashtable();
                    if (clasf==null) {
                        clasf = new CurriculumClassification();
                        clasf.setCurriculum(curriculum); curriculum.getClassifications().add(clasf);
                        clasf.setAcademicClassification(e3.getKey());
                        clasf.setName(e3.getKey().getCode());
                        clasf.setCourses(new HashSet());
                    } else {
                        for (Iterator i=clasf.getCourses().iterator();i.hasNext();) {
                            CurriculumCourse c = (CurriculumCourse)i.next();
                            remainingCourses.put(c.getCourse(), c);
                        }
                        remainingClassifications.remove(clasf.getAcademicClassification());
                    }
                    Set<Long> allStudents = new HashSet();
                    for (Set<Long> students : e3.getValue().values()) allStudents.addAll(students);
                    sLog.info("  "+e3.getKey().getCode()+" ("+e3.getKey().getName()+") -- "+allStudents.size()+" students");
                    if (clasf.getNrStudents()==null) clasf.setNrStudents(allStudents.size());
                    clasf.setLlStudents(allStudents.size());
                    for (Map.Entry<CourseOffering, Set<Long>> e4 : e3.getValue().entrySet()) {
                        CurriculumCourse course = null;
                        for (Iterator i=clasf.getCourses().iterator();i.hasNext();) {
                            CurriculumCourse c = (CurriculumCourse)i.next();
                            if (c.getCourse().equals(e4.getKey())) {
                                course = c; break;
                            }
                        }
                        float share = ((float)e4.getValue().size())/allStudents.size();
                        //sLog.info("      "+e3.getKey().getCourseName()+" has "+e3.getValue().size()+" students ("+new DecimalFormat("0.0").format(100.0*share)+"%)");
                        if (course==null) {
                            if (share<iShareLimit && e4.getValue().size()<iEnrlLimit) continue;
                            course = new CurriculumCourse();
                            course.setClassification(clasf); clasf.getCourses().add(course);
                            course.setCourse(e4.getKey());
                        } else {
                            remainingCourses.remove(course.getCourse());
                        }
                        if (course.getPercShare()==null) course.setPercShare(share);
                        course.setLlShare(share);
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
                    for (Map.Entry<Department,Integer> e3 : deptCounter.entrySet()) {
                        if (dept==null || best<e3.getValue()) {
                            dept = e3.getKey(); best = e3.getValue();
                        }
                    }
                    curriculum.setDepartment(dept);
                }
                hibSession.saveOrUpdate(curriculum);

            }
        }
    }
    
    public static class AcademicAreaMajor {
    	private AcademicArea iArea = null;
    	private PosMajor iMajor = null;
    	
    	AcademicAreaMajor(AcademicArea area, PosMajor major) {
    		iArea = area;
    		iMajor = major;
    	}
    	
    	public AcademicArea getArea() { return iArea; }
    	public PosMajor getMajor() { return iMajor; }
    	
    	public boolean equals(Object o) {
    		if (o == null || !(o instanceof AcademicAreaMajor)) return false;
    		AcademicAreaMajor a = (AcademicAreaMajor)o;
    		return a.getMajor().equals(getMajor()) && a.getArea().equals(getArea());
    	}
    	
    	public int hashCode() {
    		return getArea().hashCode() ^ getMajor().hashCode();
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
                        ApplicationProperties.getProperty("initiative", "PWL"),
                        ApplicationProperties.getProperty("year","2009"),
                        ApplicationProperties.getProperty("term","Fall")
                        );
                
                if (session==null) {
                    sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                    System.exit(0);
                } else {
                    sLog.info("Session: "+session);
                }
                
                new MakeCurriculaFromLastlikeDemands(session.getUniqueId()).update(hibSession, false);
                
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
