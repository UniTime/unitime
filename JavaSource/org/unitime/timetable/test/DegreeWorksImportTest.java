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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cpsolver.ifs.util.ToolBox;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.purdue.BannerTermProvider;

public class DegreeWorksImportTest {
	protected static Logger sLog = Logger.getLogger(DegreeWorksImportTest.class);
	
	public static int guessEnrollmentFromLastLike(org.hibernate.Session hibSession, Session session, String area, String major, String classification) {
		return ((Number)hibSession.createQuery(
				"select count(distinct d.student) from LastLikeCourseDemand d inner join d.student.academicAreaClassifications aac " +
				"inner join d.student.posMajors m where d.subjectArea.session = :sessionId and " +
				"m.code=:major and aac.academicArea.academicAreaAbbreviation = :area and aac.academicClassification.code = :clasf")
				.setLong("sessionId", session.getUniqueId())
				.setString("area", area)
				.setString("major", major)
				.setString("clasf", classification)
				.uniqueResult()).intValue();
	}
	
	public static int guessEnrollmentFromReal(org.hibernate.Session hibSession, Session session, String area, String major, String classification) {
		return ((Number)hibSession.createQuery(
				"select count(distinct e.student) from StudentClassEnrollment e inner join e.student.academicAreaClassifications aac " +
				"inner join e.student.posMajors m where e.student.session = :sessionId and " +
				"m.code=:major and aac.academicArea.academicAreaAbbreviation = :area and aac.academicClassification.code = :clasf")
				.setLong("sessionId", session.getUniqueId())
				.setString("area", area)
				.setString("major", major)
				.setString("clasf", classification)
				.uniqueResult()).intValue();
	}

	public static int guessEnrollmentFromLastLike(org.hibernate.Session hibSession, CourseOffering co, String area, String major, String classification) {
		if (co.getPermId() == null)
			return ((Number)hibSession.createQuery(
					"select count(distinct d.student) from LastLikeCourseDemand d inner join d.student.academicAreaClassifications aac " +
					"inner join d.student.posMajors m where d.subjectArea.uniqueId = :subjectId and d.courseNbr = :courseNbr and "+
					"m.code=:major and aac.academicArea.academicAreaAbbreviation = :area and aac.academicClassification.code = :clasf")
					.setLong("subjectId", co.getSubjectArea().getUniqueId())
					.setString("courseNbr", co.getCourseNbr())
					.setString("area", area)
					.setString("major", major)
					.setString("clasf", classification)
					.uniqueResult()).intValue();
		else
			return ((Number)hibSession.createQuery(
					"select count(distinct d.student) from LastLikeCourseDemand d inner join d.student.academicAreaClassifications aac " +
					"inner join d.student.posMajors m where d.subjectArea.session.uniqueId = :subjectId and d.coursePermId = :permId and " +
					"m.code=:major and aac.academicArea.academicAreaAbbreviation = :area and aac.academicClassification.code = :clasf")
					.setLong("sessionId", co.getSubjectArea().getSessionId())
					.setString("permId", co.getPermId())
					.setString("area", area)
					.setString("major", major)
					.setString("clasf", classification)
					.uniqueResult()).intValue();
	}
	
	public static int guessEnrollmentFromReal(org.hibernate.Session hibSession, CourseOffering co, String area, String major, String classification) {
		return ((Number)hibSession.createQuery(
				"select count(distinct e.student) from StudentClassEnrollment e inner join e.student.academicAreaClassifications aac " +
				"inner join e.student.posMajors m where e.courseOffering.uniqueId = :courseId and " +
				"m.code=:major and aac.academicArea.academicAreaAbbreviation = :area and aac.academicClassification.code = :clasf")
				.setLong("courseId", co.getUniqueId())
				.setString("area", area)
				.setString("major", major)
				.setString("clasf", classification)
				.uniqueResult()).intValue();
	}
	
	public static List<CourseOffering> getCourses(org.hibernate.Session hibSession, Session session, String subject, String courseNbr) {
		List<CourseOffering> courses = (List<CourseOffering>)hibSession.createQuery(
				"from CourseOffering co where co.subjectArea.session.uniqueId = :sessionId and co.subjectArea.subjectAreaAbbreviation = :subject " +
				"and co.courseNbr like :courseNbr || '%' order by co.courseNbr")
				.setLong("sessionId", session.getUniqueId())
				.setString("subject", subject)
				.setString("courseNbr", courseNbr)
				.list();
		
		// filter out not offered courses, if possible
		boolean hasOffered = false;
		for (Iterator<CourseOffering> i = courses.iterator(); i.hasNext(); ) {
			CourseOffering co = i.next();
			if (!co.getInstructionalOffering().getNotOffered()) hasOffered = true;
		}
		if (hasOffered)
			for (Iterator<CourseOffering> i = courses.iterator(); i.hasNext(); ) {
				CourseOffering co = i.next();
				if (co.getInstructionalOffering().getNotOffered()) i.remove();
			}
		return courses;
	}
	
	public static void main(String[] args) {
		try {
			ToolBox.configureLogging();
			
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
			
            final org.hibernate.Session hibSession = new SessionDAO().getSession();
            
            Session session = Session.getSessionUsingInitiativeYearTerm(
                    ApplicationProperties.getProperty("initiative", "PWL"),
                    ApplicationProperties.getProperty("year","2015"),
                    ApplicationProperties.getProperty("term","Spring")
                    );
            
            if (session==null) {
                sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                System.exit(0);
            } else {
                sLog.info("Session: "+session);
            }
            
            String line;
            int bannerTerm = Integer.parseInt(new BannerTermProvider().getExternalTerm(new AcademicSessionInfo(session)));
            NumberFormat clasfFormat = new DecimalFormat("00");
            sLog.info("Banner term: " + bannerTerm);

            Map<String, String> test = new HashMap<String, String>();
            Map<String, Integer> counts = new HashMap<String, Integer>();
            BufferedReader in = new BufferedReader(new FileReader("/Users/muller/Test/DegreeWorks/Student_counts_201510.csv"));
            while ((line = in.readLine()) != null) {
            	String[] data = line.split(",");
            	if ("SGBSTDN_MAJR_CODE_1".equals(data[0])) continue;
            	String major = data[0];
            	int term = Integer.parseInt(data[2]);
            	if (term > bannerTerm) continue;
            	int code = ("Fall".equals(session.getAcademicTerm()) ? 1 : 2);
            	while (term <= bannerTerm - 100) {
            		code += 2; term += 100;
            	}
            	String key = major + ":" + clasfFormat.format(code);
            	Integer count = counts.get(key);
            	counts.put(key, count == null ? Integer.parseInt(data[4]) : Integer.parseInt(data[4]) + count.intValue());
            	test.put(data[2], clasfFormat.format(code));
            }
            in.close();
            sLog.info("Mapping " + ToolBox.dict2string(test, 2));
			
			in = new BufferedReader(new FileReader("/Users/muller/Test/DegreeWorks/template_data.txt"));
            List<Template> templates = new ArrayList<Template>();
            Template template = null;
            
            Pattern termPattern = Pattern.compile(session.getAcademicTerm() + " Term ([1-9]+)");
            
            while ((line = in.readLine()) != null) {
            	String[] data = line.split("!");
            	
            	String recordType = data[0].split(":")[0];
            	
            	if ("MST".equals(recordType)) {
            		template = new Template(data[0].split(":")[1]);
            		templates.add(template);
            		sLog.info("Processing template " + template.getId() + " (description: " + data[1] + ", active:" + data[2] + ")");
            	} else if ("TERM".equals(recordType)) {
            		Matcher m = termPattern.matcher(data[3]);
            		if (m.matches()) {
            			int code = 2 * (Integer.parseInt(m.group(1)) - 1) + ("Fall".equals(session.getAcademicTerm()) ? 1 : 2);
            			Classification c = new Classification(template, data[1], clasfFormat.format(code));
            			template.addClassification(c);
            			sLog.info("  added classification " + c.getCode());
            		}
            	} else if ("TAG".equals(recordType)) {
            		String code = data[1];
            		String value = data[2];
            		if ("COLLEGE".equals(code)) {
            			sLog.info("    -- academic area " + value);
            			template.setArea(value);
            		}
            		if ("MAJOR".equals(code)) {
            			sLog.info("    -- major " + value);
            			template.setMajor(value);
            		}
            	} else if ("GROUPMST".equals(recordType)) {
            		Classification c = template.getClassification(data[3]);
            		if (c == null) continue;
            		Group g = new Group(c, data[1], "R" + c.getCode(), "REQ");
            		c.addGroup(g);
            		sLog.info("  select all of (" + c + ")");
            	} else if ("CHOICEGROUP".equals(recordType)) {
            		Classification c = template.getClassification(data[3]);
            		if (c == null) continue;
            		Group g = new Group(c, data[1], "O" + c.getCode() + "-" + data[5], "OPT");
            		c.addGroup(g);
            		sLog.info("  select one of (" + c + ")");
            	} else if ("CLASSGROUP".equals(recordType)) {
            		Group g = template.getGroup(data[3]);
            		if (g == null) continue;
            		Group x = new Group(g.getClassification(), data[1], "X" + g.getClassification().getCode() + "-" + data[5], "REQ");
            		x.setParentId(g.getId());
            		g.getClassification().addGroup(x);;
            		sLog.info("    all of (" + x.getClassification() + ")");
            	} else if ("CLASS".equals(recordType)) {
            		Group g = template.getGroup(data[2]);
            		if (g == null) continue;
            		List<CourseOffering> courses = getCourses(hibSession, session, data[5], data[6]);
            		if (courses.isEmpty()) {
            			sLog.info("Course " + data[5] + " " + data[6] + " does not exist.");
            			continue;
            		} else {
            			for (CourseOffering course: courses)
            				if (course.getInstructionalOffering().getNotOffered())
            					sLog.info("Course " + course.getCourseNameWithTitle() + " is not offered.");
            		}
            		Course course = new Course(g, data[5], data[6]);
            		g.addCourse(course);
            		sLog.info("    " + course + " (" + g.getClassification() + ")");
            	} else if ("CLASSGRP".equals(recordType)) {
            		Group g = template.getGroup(data[2]);
            		if (g == null) continue;
            		List<CourseOffering> courses = getCourses(hibSession, session, data[5], data[6]);
            		if (courses.isEmpty()) {
            			sLog.info("Course " + data[5] + " " + data[6] + " does not exist.");
            			continue;
            		}
            		Course course = new Course(g, data[5], data[6]);
            		g.addCourse(course);
            		sLog.info("        " + course + " (" + g.getClassification() + ")");
            	} else if ("PLACEHOLDER".equals(recordType)) {
            		Group g = template.getGroup(data[2]);
            		if (g == null) continue;
            		sLog.info("    placeholder " + data[3] + " (" + data[4] + ", " + g.getClassification() + ")");
            	} else if ("NONCOURSE".equals(recordType)) {
            	} else if ("GPA".equals(recordType)) {
            	} else if ("TEST".equals(recordType)) {
            	} else {
            		sLog.error("  not recognized " + recordType);
            	}
            	
            }
            
            in.close();
            
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("curricula");
            root.addAttribute("campus", session.getAcademicInitiative());
            root.addAttribute("term", session.getAcademicTerm());
            root.addAttribute("year", session.getAcademicYear());
            root.addAttribute("created", new Date().toString());
            
            NumberFormat shareFormat = new DecimalFormat("0.000");
            
            for (Template t: templates) {
            	Element curriculum = root.addElement("curriculum");
    			curriculum.addElement("academicArea").addAttribute("abbreviation", t.getArea());
    			curriculum.addElement("major").addAttribute("code", t.getMajor());
    			curriculum.addAttribute("abbreviation", t.getArea() + "/" + t.getMajor());
            	for (Classification c: t.getClassifications()) {
        			Element clasf = curriculum.addElement("classification");
        			clasf.addElement("academicClassification").addAttribute("code", c.getCode());
        			// int enrollment = guessEnrollmentFromReal(hibSession, session, t.getArea(), t.getMajor(), c.getCode());
        			Integer enrollment = counts.get(t.getMajor() + ":" + c.getCode());
        			if (enrollment == null) enrollment = 0;
        			clasf.addAttribute("enrollment", String.valueOf(enrollment));
        			for (Group g: c.getGroups()) {
        				int mIdx = 0;
        				if (g.hasParentId()) continue;
        				int total = 0;
        				Map<Long, Integer> enrollments = new HashMap<Long, Integer>();
        				Map<String, Integer> grtot = new HashMap<String, Integer>();
        				if ("OPT".equals(g.getType())) {
            				for (Course r: g.getCourses()) {
            					List<CourseOffering> courses = getCourses(hibSession, session, r.getSubject(), r.getCourseNbr());
            					for (CourseOffering co: courses) {
            						int e = guessEnrollmentFromReal(hibSession, co, t.getArea(), t.getMajor(), c.getCode());
            						total += e;
            						enrollments.put(co.getUniqueId(), e);
            						if (co.getCourseNbr().equals(r.getCourseNbr())) break;
            					}
            				}
            				for (Group h: c.getChildGroups(g)) {
            					int m = 0;
            					for (Course r: h.getCourses()) {
            						int tot = 0;
                					List<CourseOffering> courses = getCourses(hibSession, session, r.getSubject(), r.getCourseNbr());
                					for (CourseOffering co: courses) {
                						int e = guessEnrollmentFromReal(hibSession, co, t.getArea(), t.getMajor(), c.getCode());
                						tot += e;
                						enrollments.put(co.getUniqueId(), e);
                						if (co.getCourseNbr().equals(r.getCourseNbr())) break;
                					}
                					if (m < tot) m = tot;
            					}
            					grtot.put(h.getId(), m);
            					total += m;
            				}
        				}
        				int size = g.getCourses().size();
        				for (Group h: c.getChildGroups(g))
        					if (!h.getCourses().isEmpty()) size ++;
        				for (Course r: g.getCourses()) {
        					List<CourseOffering> courses = getCourses(hibSession, session, r.getSubject(), r.getCourseNbr());
        					if (courses.isEmpty()) {
        						sLog.info("Course " + r + " does not exist.");
        						Element ce = clasf.addElement("course").addAttribute("subject", r.getSubject()).addAttribute("courseNbr", r.getCourseNbr());
        						ce.addElement("group").addAttribute("id", g.getId().trim()).addAttribute("name", g.getName()).addAttribute("type", g.getType());
        						if ("REQ".equals(g.getType()))
        							ce.addAttribute("share", "1.0");
        						else if (total > 0)
        							ce.addAttribute("share", "0.0");
        						else
        							ce.addAttribute("share", shareFormat.format(1.0 / size));
        					} else if (courses.size() == 1 || courses.get(0).getCourseNbr().equals(r.getCourseNbr())) {
        						CourseOffering co = courses.get(0);
        						Element ce = clasf.addElement("course").addAttribute("subject", co.getSubjectAreaAbbv()).addAttribute("courseNbr", co.getCourseNbr());
        						ce.addElement("group").addAttribute("id", g.getId().trim()).addAttribute("name", g.getName()).addAttribute("type", g.getType());
        						if ("REQ".equals(g.getType()))
        							ce.addAttribute("share", "1.0");
        						else if (total > 0) {
        							Integer e = enrollments.get(co.getUniqueId());
        							if (e == null)
        								ce.addAttribute("share", "0.0");
        							else {
        								ce.addAttribute("share", shareFormat.format(((double)e) / total));
        								ce.addAttribute("enrollment", e.toString());
        							}
        						} else
        							ce.addAttribute("share", shareFormat.format(1.0 / size));
        					} else {
        						sLog.info("Course " + r + " has multiple matches.");
        						mIdx ++;
        						if ("REQ".equals(g.getType())) {
        							total = 0;
        							for (CourseOffering co: courses) {
                						int e = guessEnrollmentFromReal(hibSession, co, t.getArea(), t.getMajor(), c.getCode());
                						total += e;
                						enrollments.put(co.getUniqueId(), e);
        							}
        						}
        						for (int i = 0; i < courses.size(); i++) {
                    				CourseOffering co = courses.get(i);
                    				sLog.info("  -- " + co.getCourseNameWithTitle());
                        			Element ce = clasf.addElement("course").addAttribute("subject", co.getSubjectAreaAbbv()).addAttribute("courseNbr", co.getCourseNbr());
                        			if (i == 0)
                        				ce.addElement("group").addAttribute("id", g.getId().trim()).addAttribute("name", g.getName()).addAttribute("type", g.getType());
                        			ce.addElement("group").addAttribute("id", g.getId().trim() + "-" + mIdx).addAttribute("name", g.getName() + "_" + mIdx).addAttribute("type", "OPT");
                    				if (total > 0) {
                    					Integer e = enrollments.get(co.getUniqueId());
                    					if (e == null)
                    						ce.addAttribute("share", "0.0");
            							else {
            								ce.addAttribute("share", shareFormat.format(((double)e) / total));
            								ce.addAttribute("enrollment", e.toString());
            							}
                    				} else if ("REQ".equals(g.getType()))
                    					ce.addAttribute("share", shareFormat.format(1.0 / courses.size()));
                    				else
                    					ce.addAttribute("share", shareFormat.format(1.0 / size / courses.size()));
                    			}
        					}
        				}
        				for (Group h: c.getChildGroups(g)) {
        					int tot = grtot.get(h.getId());
        					for (int j = 0; j < h.getCourses().size(); j++) {
        						Course r = h.getCourses().get(j);
        						List<CourseOffering> courses = getCourses(hibSession, session, r.getSubject(), r.getCourseNbr());
            					if (courses.isEmpty()) {
            						sLog.info("Course " + r + " does not exist.");
            						Element ce = clasf.addElement("course").addAttribute("subject", r.getSubject()).addAttribute("courseNbr", r.getCourseNbr());
            						if (j == 0)
            							ce.addElement("group").addAttribute("id", g.getId().trim()).addAttribute("name", g.getName()).addAttribute("type", g.getType());
            						ce.addElement("group").addAttribute("id", h.getId().trim()).addAttribute("name", h.getName()).addAttribute("type", h.getType());
            						if (total > 0)
            							ce.addAttribute("share", shareFormat.format(((double)tot) / total));
            						else
            							ce.addAttribute("share", shareFormat.format(1.0 / size));
            					} else if (courses.size() == 1 || courses.get(0).getCourseNbr().equals(r.getCourseNbr())) {
            						CourseOffering co = courses.get(0);
            						Element ce = clasf.addElement("course").addAttribute("subject", co.getSubjectAreaAbbv()).addAttribute("courseNbr", co.getCourseNbr());
            						if (j == 0)
            							ce.addElement("group").addAttribute("id", g.getId().trim()).addAttribute("name", g.getName()).addAttribute("type", g.getType());
            						ce.addElement("group").addAttribute("id", h.getId().trim()).addAttribute("name", h.getName()).addAttribute("type", h.getType());
            						if (total > 0)
            							ce.addAttribute("share", shareFormat.format(((double)tot) / total));
            						else
            							ce.addAttribute("share", shareFormat.format(1.0 / size));
            					} else {
            						sLog.info("Course " + r + " has multiple matches.");
            						mIdx ++;
            						for (int i = 0; i < courses.size(); i++) {
                        				CourseOffering co = courses.get(i);
                        				sLog.info("  -- " + co.getCourseNameWithTitle());
                            			Element ce = clasf.addElement("course").addAttribute("subject", co.getSubjectAreaAbbv()).addAttribute("courseNbr", co.getCourseNbr());
                						if (j == 0 && i == 0)
                							ce.addElement("group").addAttribute("id", g.getId().trim()).addAttribute("name", g.getName()).addAttribute("type", g.getType());
                            			if (i == 0)
                            				ce.addElement("group").addAttribute("id", h.getId().trim()).addAttribute("name", h.getName()).addAttribute("type", h.getType());
                            			ce.addElement("group").addAttribute("id", h.getId().trim() + "-" + mIdx).addAttribute("name", h.getName() + "_" + mIdx).addAttribute("type", "OPT");
                            			if (total > 0) {
                            				Integer e = enrollments.get(co.getUniqueId());
                        					if (e == null)
                        						ce.addAttribute("share", "0.0");
                							else {
                								ce.addAttribute("share", shareFormat.format(((double)e) / total));
                								ce.addAttribute("enrollment", e.toString());
                							}
                            			} else
                							ce.addAttribute("share", shareFormat.format(1.0 / size / courses.size()));
                        			}
            					}
        					}
        				}
        			}
            	}
            }
            
            FileWriter out = new FileWriter("/Users/muller/Test/DegreeWorks/curricula_" + bannerTerm + ".xml");
            new XMLWriter(out, OutputFormat.createPrettyPrint()).write(document);
            out.flush(); out.close();
            
            HibernateUtil.closeHibernate();
            
			sLog.info("All done.");
		} catch (Exception e) {
        	sLog.error("Test failed: " + e.getMessage(), e);
        }
	}
	
	static class Template {
		String iId, iArea, iMajor;
		List<Classification> iClassifications = new ArrayList<Classification>();
		
		Template(String id) {
			iId = id;
		}
		
		String getId() { return iId; }
		
		String getArea() { return iArea; }
		void setArea(String area) { iArea = area; }
		
		String getMajor() { return iMajor; }
		void setMajor(String major) { iMajor = major; }

		void addClassification(Classification c) { iClassifications.add(c); }
		Classification getClassification(String id) {
			for (Classification c: iClassifications)
				if (c.getId().equals(id)) return c;
			return null;
		}
		List<Classification> getClassifications() { return iClassifications; }
		
		Group getGroup(String id) {
			for (Classification c: iClassifications)
				for (Group g: c.getGroups())
					if (g.getId().equals(id)) return g;
			return null;
		}

		@Override
		public String toString() { return iArea + "/" + iMajor; }
	}
	
	static class Classification {
		Template iTemplate;
		String iId;
		String iCode;
		List<Group> iGroups = new ArrayList<Group>();
		int iEnrollment = 0;
		
		Classification(Template template, String id, String code) {
			iTemplate = template;
			iId = id; iCode = code;
		}
		
		Template getTemplate() { return iTemplate; }
		String getId() { return iId; }
		String getCode() { return iCode; }
		
		void addGroup(Group c) { iGroups.add(c); }
		Group getGroup(String id) {
			for (Group c: iGroups)
				if (c.getId().equals(id)) return c;
			return null;
		}
		List<Group> getGroups() { return iGroups; }
		
		List<Group> getChildGroups(Group group) {
			List<Group> ret = new ArrayList<Group>();
			for (Group g: iGroups)
				if (group.getId().equals(g.getParentId()))
					ret.add(g);
			return ret;
		}
		
		int getEnrollment() { return iEnrollment; }
		void setEnrollment(int enrollment) { iEnrollment = enrollment; }
		
		@Override
		public String toString() { return iCode; }
	}
	
	static class Group {
		Classification iClassification;
		String iId;
		String iName;
		String iType;
		String iParentId = null;
		List<Course> iCourses = new ArrayList<Course>();
		
		Group(Classification classification, String id, String name, String type) {
			iClassification = classification;
			iId = id;
			iName = name;
			iType = type;
		}
		
		Classification getClassification() { return iClassification; }
		String getId() { return iId; }
		String getName() { return iName; }
		String getType() { return iType; }
		
		boolean hasParentId() { return iParentId != null; }
		String getParentId() { return iParentId; }
		void setParentId(String parentId) { iParentId = parentId; }
		
		void addCourse(Course course) { iCourses.add(course); }
		List<Course> getCourses() { return iCourses; }
		
		@Override
		public String toString() { return iName; }
	}
	
	static class Course {
		Group iGroup;
		String iSubject;
		String iCourseNbr;
		float iShare = 1.0f;
		
		Course(Group group, String subject, String courseNbr) {
			iGroup = group;
			iSubject = subject; iCourseNbr = courseNbr;
		}
		
		Group getGroup() { return iGroup; }
		String getSubject() { return iSubject; }
		String getCourseNbr() { return iCourseNbr; }
		
		void setShare(float share) { iShare = share; }
		float getShare() { return iShare; }
		
		@Override
		public String toString() { return getSubject() + " " + getCourseNbr(); }
	}
}
