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
package org.unitime.timetable.test;

import java.text.DecimalFormat;
import java.util.ArrayList;
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
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumCourseGroup;
import org.unitime.timetable.model.CurriculumProjectionRule;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class MakeCurriculaFromLastlikeDemands {
    protected static Logger sLog = Logger.getLogger(MakeCurriculaFromLastlikeDemands.class);
    private Long iSessionId = null;
    private float iShareLimit = 0.00f;
    private int iEnrlLimit = 1;
    private float iTotalShareLimit = 0.03f;
    
    public MakeCurriculaFromLastlikeDemands(Long sessionId, float totalShareLimit, float shareLimit, int enrlLimit) {
        iSessionId = sessionId;
        iTotalShareLimit = totalShareLimit;
        iShareLimit = shareLimit;
        iEnrlLimit = enrlLimit;
    }
    
    public MakeCurriculaFromLastlikeDemands(Long sessionId) {
        this(sessionId,
        		ApplicationProperty.CurriculumLastLikeDemandsTotalShareLimit.floatValue(),
        		ApplicationProperty.CurriculumLastLikeDemandsShareLimit.floatValue(),
        		ApplicationProperty.CurriculumLastLikeDemandsEnrollmentLimit.intValue());
    }
    
    public Hashtable<AcademicArea, Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>>> loadLastLikeCurricula(org.hibernate.Session hibSession) {
        Hashtable<AcademicArea, Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>>> curricula = new Hashtable();
        List demands = (List)hibSession.createQuery(
                "select a2, f2, m2, c, d.student.uniqueId from LastLikeCourseDemand d inner join d.student.academicAreaClassifications a inner join d.student.posMajors m, CourseOffering c," +
                "AcademicArea a2, AcademicClassification f2, PosMajor m2 where "+
                "a2.session.uniqueId=:sessionId and a2.academicAreaAbbreviation=a.academicArea.academicAreaAbbreviation and "+
                "f2.session.uniqueId=:sessionId and f2.code=a.academicClassification.code and " +
                "m2.session.uniqueId=:sessionId and m2.code=m.code and " +
                "d.subjectArea.session.uniqueId=:sessionId and c.subjectArea=d.subjectArea and "+
                "((d.coursePermId=null and c.courseNbr=d.courseNbr) or "+
                " (d.coursePermId!=null and d.coursePermId=c.permId))")
                .setLong("sessionId", iSessionId)
                .setFetchSize(1000)
                .list();
        sLog.info("Processing "+demands.size()+" demands...");
        for (Iterator i=demands.iterator();i.hasNext();) {
            Object o[] = (Object[])i.next();
            AcademicArea a = (AcademicArea)o[0];
            AcademicClassification f = (AcademicClassification)o[1];
            PosMajor m = (PosMajor)o[2];
            CourseOffering c = (CourseOffering)o[3];
            Long s = (Long)o[4];
            Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> curacad = curricula.get(a);
            if (curacad==null) {
            	curacad = new Hashtable();
            	curricula.put(a, curacad);
            }
            Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>> clasf = curacad.get(m);
            if (clasf==null) {
                clasf = new Hashtable();
                curacad.put(m, clasf);
            }
            Hashtable<CourseOffering, Set<Long>> courses = clasf.get(f);
            if (courses==null) {
                courses = new Hashtable();
                clasf.put(f, courses);
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
        		"select distinct a, m, c, s.uniqueId from CourseRequest r inner join r.courseDemand.student s inner join s.academicAreaClassifications a " +
                "inner join s.posMajors m inner join r.courseOffering c where "+
                "s.session.uniqueId=:sessionId")
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
    
	private Hashtable<String,Hashtable<String, Float>> getRules(org.hibernate.Session hibSession, Long acadAreaId) {
		Hashtable<String,Hashtable<String, Float>> clasf2major2proj = new Hashtable<String, Hashtable<String,Float>>();
		for (CurriculumProjectionRule rule: (List<CurriculumProjectionRule>)hibSession.createQuery(
				"select r from CurriculumProjectionRule r where r.academicArea.uniqueId=:acadAreaId")
				.setLong("acadAreaId", acadAreaId).setCacheable(true).list()) {
			String majorCode = (rule.getMajor() == null ? "" : rule.getMajor().getCode());
			String clasfCode = rule.getAcademicClassification().getCode();
			Float projection = rule.getProjection();
			Hashtable<String, Float> major2proj = clasf2major2proj.get(clasfCode);
			if (major2proj == null) {
				major2proj = new Hashtable<String, Float>();
				clasf2major2proj.put(clasfCode, major2proj);
			}
			major2proj.put(majorCode, projection);
		}
		return clasf2major2proj;
	}
	
	public float getProjection(Hashtable<String,Hashtable<String, Float>> clasf2major2proj, String majorCode, String clasfCode) {
		if (clasf2major2proj == null || clasf2major2proj.isEmpty()) return 1.0f;
		Hashtable<String, Float> major2proj = clasf2major2proj.get(clasfCode);
		if (major2proj == null) return 1.0f;
		Float projection = major2proj.get(majorCode);
		if (projection == null)
			projection = major2proj.get("");
		return (projection == null ? 1.0f : projection);
	}

	public void update(org.hibernate.Session hibSession, boolean lastLike) {
    	sLog.info("Deleting existing curricula...");
    	for (Iterator<Curriculum> i = hibSession.createQuery("select c from Curriculum c where c.department.session=:sessionId").
        	setLong("sessionId", iSessionId).list().iterator(); i.hasNext(); ) {
    		hibSession.delete(i.next());
    	}
    	hibSession.flush();
    	
    	sLog.info("Loading " + (lastLike ? "last-like" : "current") + " student enrollments...");
        Hashtable<AcademicArea, Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>>> curricula = (lastLike ? loadLastLikeCurricula(hibSession) : loadRealCurricula(hibSession));
        
        sLog.info("Creating curricula...");
        for (Map.Entry<AcademicArea, Hashtable<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>>> e1 : curricula.entrySet()) {
        	Hashtable<String,Hashtable<String, Float>> rules = getRules(hibSession, e1.getKey().getUniqueId());
            for (Map.Entry<PosMajor, Hashtable<AcademicClassification, Hashtable<CourseOffering, Set<Long>>>> e2 : e1.getValue().entrySet()) {
            	if (!e1.getKey().getPosMajors().contains(e2.getKey())) {
            		sLog.warn("Academic area " + e1.getKey().getAcademicAreaAbbreviation() + " - " + Constants.curriculaToInitialCase(e1.getKey().getTitle()) +
            				" does not contain major " + e2.getKey().getCode() + " - " + Constants.curriculaToInitialCase(e2.getKey().getName()));
            		continue;
            	}
            	
            	sLog.info("Creating curriculum "+e1.getKey().getAcademicAreaAbbreviation()+" ("+e1.getKey().getTitle()+") - " + e2.getKey().getCode() + " (" + e2.getKey().getName()+ ")");
                Hashtable<Department,Integer> deptCounter = new Hashtable<Department, Integer>();

                Curriculum curriculum = new Curriculum();
                curriculum.setAcademicArea(e1.getKey());
                curriculum.setAbbv(e1.getKey().getAcademicAreaAbbreviation() + "/" + e2.getKey().getCode());
                curriculum.setName(Constants.curriculaToInitialCase(e1.getKey().getTitle() == null ? e1.getKey().getTitle() : e1.getKey().getTitle()) + " / " + Constants.curriculaToInitialCase(e2.getKey().getName()));
				if (curriculum.getName().length() > 60) curriculum.setName(curriculum.getName().substring(0, 60));
                curriculum.setMultipleMajors(false);
                curriculum.setClassifications(new HashSet());
                curriculum.setMajors(new HashSet());
                curriculum.getMajors().add(e2.getKey());

                Hashtable<CourseOffering, Set<Long>> courseStudents = new Hashtable<CourseOffering, Set<Long>>();
                Set<Long> studentsThisCurricula = new HashSet<Long>();
                for (Map.Entry<AcademicClassification, Hashtable<CourseOffering, Set<Long>>> e3 : e2.getValue().entrySet()) {
                    for (Map.Entry<CourseOffering, Set<Long>> e4 : e3.getValue().entrySet()) {
                    	studentsThisCurricula.addAll(e4.getValue());
                    	Set<Long> studentsThisCourse = courseStudents.get(e4.getKey());
                    	if (studentsThisCourse == null) {
                    		studentsThisCourse = new HashSet<Long>();
                    		courseStudents.put(e4.getKey(), studentsThisCourse);
                    	}
                    	studentsThisCourse.addAll(e4.getValue());
                    }
                }
                
                for (Map.Entry<AcademicClassification, Hashtable<CourseOffering, Set<Long>>> e3 : e2.getValue().entrySet()) {

                    Set<Long> studentsThisCurriculaClassification = new HashSet();
                    for (Set<Long> students : e3.getValue().values()) studentsThisCurriculaClassification.addAll(students);
                    
                    sLog.info("  "+e3.getKey().getCode()+" ("+e3.getKey().getName()+") -- "+studentsThisCurriculaClassification.size()+" students");
                    int projNrStudents = Math.round(getProjection(rules, e2.getKey().getCode(), e3.getKey().getCode()) * studentsThisCurriculaClassification.size());
                    if (projNrStudents <= 0) continue;
                    
                    CurriculumClassification clasf = new CurriculumClassification();
                    clasf.setCurriculum(curriculum); curriculum.getClassifications().add(clasf);
                    clasf.setAcademicClassification(e3.getKey());
                    clasf.setName(e3.getKey().getCode());
                    clasf.setCourses(new HashSet());
                    clasf.setNrStudents(projNrStudents);
                    
                    for (Map.Entry<CourseOffering, Set<Long>> e4 : e3.getValue().entrySet()) {
                        float share = ((float)e4.getValue().size())/studentsThisCurriculaClassification.size();
                        //sLog.info("      "+e3.getKey().getCourseName()+" has "+e3.getValue().size()+" students ("+new DecimalFormat("0.0").format(100.0*share)+"%)");

                    	if (share < iShareLimit && e4.getValue().size() < iEnrlLimit) continue;
                    	
                    	float totalShare = ((float)courseStudents.get(e4.getKey()).size()) / studentsThisCurricula.size();
                        if (totalShare < iTotalShareLimit) continue;
                        
                        CurriculumCourse course = new CurriculumCourse();
                        course.setClassification(clasf); clasf.getCourses().add(course);
                        course.setCourse(e4.getKey());
                        
                        course.setPercShare(share);
                        
                        Integer cx = deptCounter.get(course.getCourse().getDepartment());
                        deptCounter.put(course.getCourse().getDepartment(), new Integer(courseStudents.get(e4.getKey()).size() + (cx == null ? 0 : cx.intValue())));
                    }
                }
        	
                sortClassifications(curriculum.getClassifications());
                List<CurriculumCourseGroup> createdGroups = new ArrayList<CurriculumCourseGroup>();
                
                Hashtable<CourseOffering, Group[]> course2group = new Hashtable<CourseOffering, Group[]>();
                int id = 0;
                int totalStudents = 0;

                for (Iterator i=curriculum.getClassifications().iterator();i.hasNext();) {
                    CurriculumClassification clasf = (CurriculumClassification)i.next();
                    sortCourses(clasf.getCourses());
                    totalStudents += clasf.getNrStudents();
                    
                    for (Iterator j = clasf.getCourses().iterator(); j.hasNext(); ) {
                    	CurriculumCourse course = (CurriculumCourse)j.next();
                    	Group[] g = course2group.get(course.getCourse());
                    	if (g == null) {
                    		g = new Group[] {
                    					new Group(id++, 0 , course, new HashSet<Long>()),
                    					new Group(id++, 1 , course, new HashSet<Long>())
                    				};
                    		course2group.put(course.getCourse(), g);
                    	} else {
                    		g[0].getCourses().add(course);
                    		g[1].getCourses().add(course);
                    	}
                    }
                }
                
                List<Group> groups = new ArrayList<Group>();
                for (Group[] g: course2group.values()) {
                	CourseOffering course = g[0].getFistCourseOffering();
                	Set<Long> students = courseStudents.get(course);
                	double share = 100.0f * students.size() / totalStudents;
                	if (students.size() > 5 && share >= 5.0f) {
                		g[0].getStudents().addAll(students);
                		groups.add(g[0]);
                		g[1].getStudents().addAll(students);
                		groups.add(g[1]);
                	}
                }
                
            	boolean shrink;
            	do {
            		shrink = false;
            		Group b1 = null, b2 = null;
            		double best = 0.0;
            		for (Group g1: groups) {
            			for (Group g2: groups) {
            				if (g1.getId() <= g2.getId()) continue;
            				double share = g1.share(g2);
            				if (share > best) {
            					b1 = g1; b2 = g2; best = share;
            				}
            			}
            		}
            		if (best >= 0.9) {
            			sLog.info("  -- merge " + new DecimalFormat("0.00").format(100.0 * best) + " "  + b1 + " w " + b2);
            			b1.mergeWith(b2);
            			groups.remove(b2);
            			sLog.info("     result: " + b1);
            			shrink = true;
            		} else {
            			sLog.info("  -- best NOT merge " + new DecimalFormat("0.00").format(100.0 * best) + " "  + b1 + " w " + b2);
            		}
            	} while (shrink);

            	Hashtable<String, Integer> names = new Hashtable<String, Integer>();
            	for (Group g: groups) {
            		if (g.countCourseOfferings() <= 1) continue;
            		sLog.info("  -- " + g);
            		CurriculumCourseGroup gr = new CurriculumCourseGroup();
            		String clasf = g.getLeadingClassificationName();
            		Integer cnt = names.get((g.isSameStudents() ? "R" : "O") + " " + clasf);
            		if (cnt == null)
            			cnt = 1;
            		else
            			cnt ++;
            		gr.setName((g.isSameStudents() ? "R" : "O") + " " + clasf + " " + cnt);
            		names.put((g.isSameStudents() ? "R" : "O") + " " + clasf, cnt);
            		gr.setType(g.getType());
            		gr.setCurriculum(curriculum);
            		createdGroups.add(gr);
            		for (CurriculumCourse course: g.getCourses()) {
            			if (course.getGroups() == null) {
            				course.setGroups(new HashSet());
            			}
            			course.getGroups().add(gr);
            		}
            	}
            	
                Department dept = null; int best = 0;
                for (Map.Entry<Department,Integer> e3 : deptCounter.entrySet()) {
                    if (dept==null || best<e3.getValue()) {
                        dept = e3.getKey(); best = e3.getValue();
                    }
                }
                curriculum.setDepartment(dept);

                if (dept == null) continue;
                
                hibSession.saveOrUpdate(curriculum);
                for (CurriculumCourseGroup g: createdGroups)
                	hibSession.saveOrUpdate(g);

            }
        }
        hibSession.flush();
        sLog.info("All done.");
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
    
    public static class Group {
    	private int iId = 0;
    	private int iType = 0;
    	private List<CurriculumCourse> iCourses = new ArrayList<CurriculumCourse>();
    	private Set<Long> iStudents = new HashSet<Long>();
    	private double iShare = 1.0;
    	
    	public Group(int id, int type, CurriculumCourse course, Set<Long> students) {
    		iId = id;
    		iType = type;
    		iStudents.addAll(students);
    		iCourses.add(course);
    	}
    	
    	public CourseOffering getFistCourseOffering() {
    		return iCourses.get(0).getCourse();
    	}
    	
    	protected HashSet<CourseOffering> courses() {
    		HashSet<CourseOffering> courses = new HashSet<CourseOffering>();
    		for (CurriculumCourse c: iCourses)
    			courses.add(c.getCourse());
    		return courses;
    	}
    	
    	public int countCourseOfferings() {
    		HashSet<CourseOffering> courses = new HashSet<CourseOffering>();
    		for (CurriculumCourse c: iCourses)
    			courses.add(c.getCourse());
    		return courses.size();
    	}
    	
    	protected Hashtable<String, int[]>  getClassifications() {
    		Hashtable<String, int[]> cnt = new Hashtable<String, int[]>();
    		for (CurriculumCourse c: iCourses) {
    			int[] other = cnt.get(c.getClassification().getName());
    			cnt.put(c.getClassification().getName(), new int[] {
    				Math.round(c.getPercShare() * c.getClassification().getNrStudents()) + (other == null ? 0 : other[0]),
    				c.getClassification().getNrStudents() + (other == null ? 0 : other[1])});
    		}
    		return cnt;
    	}
    	
    	public CurriculumClassification getClassification(String name) {
    		for (CurriculumCourse c: iCourses) {
    			if (c.getClassification().getName().equals(name)) return c.getClassification();
    		}
    		return null;
    	}
    	
    	public String getLeadingClassificationName() {
    		Hashtable<String, int[]> cnt = getClassifications();
    		String best = null;
    		int bestValue = 0;
    		for (Map.Entry<String, int[]> e: cnt.entrySet()) {
    			if (e.getValue()[0] > bestValue) {
    				bestValue = e.getValue()[0];
    				best = e.getKey();
    			}
    		}
    		return best;
    	}
    	
    	public float classificationShare(Group g) {
    		Hashtable<String, int[]> a = getClassifications();
    		Hashtable<String, int[]> b = g.getClassifications();
    		int total1 = 0, total2 = 0, share = 0;
    		for (Map.Entry<String, int[]> e: a.entrySet()) {
    			float s1 = ((float)e.getValue()[0]) / e.getValue()[1];
    			int[] c = b.get(e.getKey());
    			float s2 = (c == null ? 0f : ((float)c[0]) / c[1]);
    			int x1 = Math.round(s1 * getClassification(e.getKey()).getNrStudents());
    			int x2 = Math.round(s2 * getClassification(e.getKey()).getNrStudents());
    			share += Math.min(x1, x2);
    			total1 += x1;
    		}
    		for (Map.Entry<String, int[]> e: b.entrySet()) {
    			float s1 = ((float)e.getValue()[0]) / e.getValue()[1];
    			int x1 = Math.round(s1 * g.getClassification(e.getKey()).getNrStudents());
    			total2 += x1;
    		}
    		return ((float)share) / Math.min(total1, total2);
    	}
    	
    	public List<CurriculumCourse> getCourses() {
    		return iCourses;
    	}
    	
    	public int getId() {
    		return iId;
    	}
    	
    	public int getType() {
    		return iType;
    	}
    	
    	public Set<Long> getStudents() {
    		return iStudents;
    	}
    	
    	public boolean isSameStudents() {
    		return iType == 1;
    	}
    	
    	public double getShare() { return iShare; }
    	
    	public double share(Group g) {
    		if (g.getType() != getType()) return 0.0;
    		if (classificationShare(g) < 0.66) return 0.0;
    		int minStudents = Math.min(getStudents().size(), g.getStudents().size());
    		int maxStudents = Math.max(getStudents().size(), g.getStudents().size());
    		int shareStudents = 0;
    		for (Long s: getStudents())
    			if (g.getStudents().contains(s)) shareStudents ++;
    		double share = ((double)shareStudents) / (isSameStudents() ? maxStudents : minStudents);
    		// return getShare() * g.getShare() * (isSameStudents() ? share : 1.0 - share);
    		return isSameStudents() ? share : 1.0 - share;
    	}
    	
    	public void mergeWith(Group g) {
    		int minStudents = Math.min(getStudents().size(), g.getStudents().size());
    		int maxStudents = Math.max(getStudents().size(), g.getStudents().size());
    		int shareStudents = 0;
    		for (Long s: getStudents())
    			if (g.getStudents().contains(s)) shareStudents ++;
    		
    		double share = ((double)shareStudents) / (isSameStudents() ? maxStudents : minStudents);
    		if (!isSameStudents()) share = 1.0 - share;
    		
    		iStudents.addAll(g.getStudents());
    		iCourses.addAll(g.getCourses());
    		iShare *= share * g.getShare();
    	}
    	
    	public String toString() {
    		String courses = "";
    		for (CourseOffering c: courses()) {
    			if (!courses.isEmpty()) courses += " + ";
    			courses += c.getCourseName();
    		}
    		return (isSameStudents() ? "Req" : "Opt")+ "{" + new DecimalFormat("0.0").format(100.0*iShare) + "/" + iStudents.size() + " " + courses + "}";
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
                        ApplicationProperties.getProperty("year","2010"),
                        ApplicationProperties.getProperty("term","Spring")
                        );
                
                if (session==null) {
                    sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                    System.exit(0);
                } else {
                    sLog.info("Session: "+session);
                }
                
                new MakeCurriculaFromLastlikeDemands(session.getUniqueId()).update(hibSession,
                		"last-like".equals(ApplicationProperties.getProperty("tmtbl.curriculum.lldemands.students","last-like")));
                
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
