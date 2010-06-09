/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.ServletException;

import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.RoomLocation;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.server.custom.CourseDetailsProvider;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CurriculaException;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumCourseGroupInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumCourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.MajorInterface;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumCourseGroup;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.PosMajorDAO;
import org.unitime.timetable.util.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CurriculaServlet extends RemoteServiceServlet implements CurriculaService {
	private static Logger sLog = Logger.getLogger(CurriculaServlet.class);
	private static DecimalFormat sDF = new DecimalFormat("0.0");

	public void init() throws ServletException {
	}
	
	public TreeSet<CurriculumInterface> findCurricula(String filter) throws CurriculaException {
		try {
			sLog.info("findCurricula(filter='" + filter+"')");
			Long s0 = System.currentTimeMillis();
			TreeSet<CurriculumInterface> results = new TreeSet<CurriculumInterface>();
			Query q = new Query(filter);
			getThreadLocalRequest().getSession().setAttribute("Curricula.LastFilter", filter);
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Long sessionId = getAcademicSessionId();
				
				List<Curriculum> curricula = hibSession.createQuery(
						"select distinct c from Curriculum c where c.department.session.uniqueId = :sessionId")
						.setLong("sessionId", sessionId)
						.setCacheable(true).list();
				for (Curriculum c: curricula) {
					if (q.match(new CurriculaMatcher(c))) {
						CurriculumInterface ci = new CurriculumInterface();
						ci.setId(c.getUniqueId());
						ci.setAbbv(c.getAbbv());
						ci.setName(c.getName());
						DepartmentInterface di = new DepartmentInterface();
						di.setId(c.getDepartment().getUniqueId());
						di.setAbbv(c.getDepartment().getAbbreviation());
						di.setCode(c.getDepartment().getDeptCode());
						di.setName(c.getDepartment().getName());
						ci.setDepartment(di);
						AcademicAreaInterface ai = new AcademicAreaInterface();
						ai.setId(c.getAcademicArea().getUniqueId());
						ai.setAbbv(c.getAcademicArea().getAcademicAreaAbbreviation());
						ai.setName(Constants.toInitialCase(c.getAcademicArea().getLongTitle() == null ? c.getAcademicArea().getShortTitle() : c.getAcademicArea().getLongTitle()));
						ci.setAcademicArea(ai);
						for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
							PosMajor major = i.next();
							MajorInterface mi = new MajorInterface();
							mi.setId(major.getUniqueId());
							mi.setCode(major.getCode());
							mi.setName(Constants.toInitialCase(major.getName()));
							ci.addMajor(mi);
						}
						results.add(ci);
					}
				}
			} finally {
				hibSession.close();
			}
			sLog.info("Found " + results.size() + " curricula (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public List<CurriculumClassificationInterface> loadClassifications(List<Long> curriculumIds) throws CurriculaException {
		try {
			sLog.info("loadClassifications(curriculumIds=" + curriculumIds + ")");
			Long s0 = System.currentTimeMillis();
			if (curriculumIds == null || curriculumIds.isEmpty()) return new ArrayList<CurriculumClassificationInterface>();
			List<CurriculumClassificationInterface> results = new ArrayList<CurriculumClassificationInterface>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Long sessionId = getAcademicSessionId();
				for (Long curriculumId: curriculumIds) {
					Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
					if (c == null) throw new CurriculaException("curriculum " + curriculumId + " does not exist anymore, please refresh your data");
					String majorIds = "";
					for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
						PosMajor major = i.next();
						if (!majorIds.isEmpty()) { majorIds += ","; }
						majorIds += major.getUniqueId();
					}
					Hashtable<Long, Integer> enrollments = new Hashtable<Long, Integer>();
					for (Object[] o : (List<Object[]>)hibSession.createQuery(
							"select a.academicClassification.uniqueId, count(distinct s) from Student s inner join s.academicAreaClassifications a " + 
							(majorIds.isEmpty() ? "" : " inner join s.posMajors m ") + "where " +
							"s.session.uniqueId = :sessionId and "+
							"a.academicArea.uniqueId = :areaId " + 
							(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
							"group by a.academicClassification.uniqueId")
							.setLong("sessionId", sessionId)
							.setLong("areaId", c.getAcademicArea().getUniqueId())
							.setCacheable(true).list()) {
						enrollments.put((Long)o[0], ((Number)o[1]).intValue());
					}
					TreeSet<CurriculumClassification> classifications = new TreeSet<CurriculumClassification>(c.getClassifications());
					for (CurriculumClassification clasf: classifications) {
						CurriculumClassificationInterface cfi = new CurriculumClassificationInterface();
						cfi.setId(clasf.getUniqueId());
						cfi.setName(clasf.getName());
						cfi.setCurriculumId(c.getUniqueId());
						cfi.setLastLike(clasf.getLlStudents());
						cfi.setExpected(clasf.getNrStudents());
						cfi.setEnrollment(enrollments.get(clasf.getAcademicClassification().getUniqueId()));
						AcademicClassificationInterface aci = new AcademicClassificationInterface();
						aci.setId(clasf.getAcademicClassification().getUniqueId());
						aci.setName(clasf.getAcademicClassification().getName());
						aci.setCode(clasf.getAcademicClassification().getCode());
						cfi.setAcademicClassification(aci);
						results.add(cfi);
					}
				}
			} finally {
				hibSession.close();
			}
			sLog.info("Loaded " + results.size() + " classifications (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public CurriculumInterface loadCurriculum(Long curriculumId) throws CurriculaException {
		try {
			sLog.info("loadCurriculum(curriculumId=" + curriculumId + ")");
			Long s0 = System.currentTimeMillis();

			Hashtable<Long, Integer> classifications = new Hashtable<Long, Integer>();
			int idx = 0;
			for (AcademicClassificationInterface clasf: loadAcademicClassifications()) {
				classifications.put(clasf.getId(), idx++);
			}

			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Long sessionId = getAcademicSessionId();
				Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
				if (c == null) throw new CurriculaException("curriculum " + curriculumId + " does not exist");
				CurriculumInterface curriculumIfc = new CurriculumInterface();
				curriculumIfc.setId(c.getUniqueId());
				curriculumIfc.setAbbv(c.getAbbv());
				curriculumIfc.setName(c.getName());
				DepartmentInterface deptIfc = new DepartmentInterface();
				deptIfc.setId(c.getDepartment().getUniqueId());
				deptIfc.setAbbv(c.getDepartment().getAbbreviation());
				deptIfc.setCode(c.getDepartment().getDeptCode());
				deptIfc.setName(c.getDepartment().getName());
				curriculumIfc.setDepartment(deptIfc);
				AcademicAreaInterface areaIfc = new AcademicAreaInterface();
				areaIfc.setId(c.getAcademicArea().getUniqueId());
				areaIfc.setAbbv(c.getAcademicArea().getAcademicAreaAbbreviation());
				areaIfc.setName(Constants.toInitialCase(c.getAcademicArea().getLongTitle() == null ? c.getAcademicArea().getShortTitle() : c.getAcademicArea().getLongTitle()));
				curriculumIfc.setAcademicArea(areaIfc);
				String majorIds = "";
				for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
					PosMajor major = i.next();
					MajorInterface majorIfc = new MajorInterface();
					majorIfc.setId(major.getUniqueId());
					majorIfc.setCode(major.getCode());
					majorIfc.setName(Constants.toInitialCase(major.getName()));
					curriculumIfc.addMajor(majorIfc);
					if (!majorIds.isEmpty()) majorIds += ",";
					majorIds += major.getUniqueId();
				}
				
				Hashtable<Long, Hashtable<Long, Integer>> clasf2course2enrl = new Hashtable<Long, Hashtable<Long,Integer>>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, e.courseOffering.uniqueId, count(distinct e.student) from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join e.student.posMajors m ") + "where " +
						"e.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
						"group by a.academicClassification.uniqueId, e.courseOffering.uniqueId")
						.setLong("sessionId", sessionId)
						.setLong("areaId", c.getAcademicArea().getUniqueId())
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					Long courseId = (Long)o[1];
					int enrl = ((Number)o[2]).intValue();
					Hashtable<Long, Integer> course2enrl = clasf2course2enrl.get(clasfId);
					if (course2enrl == null) {
						course2enrl = new Hashtable<Long, Integer>();
						clasf2course2enrl.put(clasfId, course2enrl);
					}
					course2enrl.put(courseId, enrl);
				}
				
				Hashtable<Long, Integer> clasf2enrl = new Hashtable<Long, Integer>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, count(distinct e.student) from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join e.student.posMajors m ") + "where " +
						"e.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
						"group by a.academicClassification.uniqueId")
						.setLong("sessionId", sessionId)
						.setLong("areaId", c.getAcademicArea().getUniqueId())
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					int enrl = ((Number)o[1]).intValue();
					clasf2enrl.put(clasfId, enrl);
				}
				
				Hashtable<Long, CourseInterface> courseId2Interface = new Hashtable<Long, CourseInterface>();
				Hashtable<String, CurriculumCourseGroupInterface> groups = new Hashtable<String, CurriculumCourseGroupInterface>();
				for (Iterator<CurriculumClassification> i = c.getClassifications().iterator(); i.hasNext(); ) {
					CurriculumClassification clasf = i.next();
					CurriculumClassificationInterface clasfIfc = new CurriculumClassificationInterface();
					clasfIfc.setId(clasf.getUniqueId());
					clasfIfc.setName(clasf.getName());
					clasfIfc.setCurriculumId(c.getUniqueId());
					clasfIfc.setLastLike(clasf.getLlStudents());
					clasfIfc.setEnrollment(clasf2enrl.get(clasf.getAcademicClassification().getUniqueId()));
					clasfIfc.setExpected(clasf.getNrStudents());
					AcademicClassificationInterface acadClasfIfc = new AcademicClassificationInterface();
					acadClasfIfc.setId(clasf.getAcademicClassification().getUniqueId());
					acadClasfIfc.setName(clasf.getAcademicClassification().getName());
					acadClasfIfc.setCode(clasf.getAcademicClassification().getCode());
					clasfIfc.setAcademicClassification(acadClasfIfc);
					curriculumIfc.addClassification(clasfIfc);
					Hashtable<Long, Integer> course2enrl = clasf2course2enrl.get(clasf.getAcademicClassification().getUniqueId());
					for (Iterator<CurriculumCourse> j = clasf.getCourses().iterator(); j.hasNext(); ) {
						CurriculumCourse course = j.next();
						CourseInterface courseIfc = courseId2Interface.get(course.getCourse().getUniqueId());
						if (courseIfc == null) {
							courseIfc = new CourseInterface();
							courseIfc.setId(course.getCourse().getUniqueId());
							courseIfc.setCourseName(course.getCourse().getCourseName());
							curriculumIfc.addCourse(courseIfc);
							courseId2Interface.put(course.getCourse().getUniqueId(), courseIfc);
						}
						CurriculumCourseInterface curCourseIfc = new CurriculumCourseInterface();
						curCourseIfc.setId(course.getUniqueId());
						curCourseIfc.setCourseOfferingId(course.getCourse().getUniqueId());
						curCourseIfc.setCurriculumClassificationId(clasf.getUniqueId());
						curCourseIfc.setShare(course.getPercShare());
						curCourseIfc.setCourseName(course.getCourse().getCourseName());
						curCourseIfc.setEnrollment(course2enrl == null ? null : course2enrl.get(course.getCourse().getUniqueId()));
						if (course.getLlShare() != null && clasf.getLlStudents() != null)
							curCourseIfc.setLastLike(Math.round(course.getLlShare() * clasf.getLlStudents()));
						courseIfc.setCurriculumCourse(classifications.get(clasf.getAcademicClassification().getUniqueId()), curCourseIfc);
						
						for (Iterator<CurriculumCourseGroup> k = course.getGroups().iterator(); k.hasNext(); ) {
							CurriculumCourseGroup group = k.next();
							CurriculumCourseGroupInterface g = groups.get(group.getName());
							if (g == null) {
								g = new CurriculumCourseGroupInterface();
								g.setName(group.getName());
								g.setType(group.getType());
								g.setColor(group.getColor());
								groups.put(g.getName(), g);
							}
							courseIfc.addGroup(g);
						}
					}
				}
				
				/*
				if (curriculumIfc.hasClassifications() && curriculumIfc.hasCourses()) {
					int totalLastLike = 0, totalEnrollment = 0;
					for (CurriculumClassificationInterface clasf: curriculumIfc.getClassifications()) {
						if (clasf.getEnrollment() != null) totalEnrollment += clasf.getEnrollment();
						if (clasf.getLastLike() != null) totalLastLike += clasf.getLastLike();
					}
					for (Iterator<CourseInterface> i = curriculumIfc.getCourses().iterator(); i.hasNext(); ) {
						CourseInterface course = i.next();
						int lastLike = 0, enrollment = 0;
						for (CurriculumCourseInterface ci: course.getCurriculumCourses()) {
							if (ci == null) continue;
							if (ci.getEnrollment() != null) enrollment += ci.getEnrollment();
							if (ci.getLastLike() != null) lastLike += ci.getLastLike();
						}
						float enrl = (totalEnrollment == 0 ? 0.0f : 100.0f * enrollment / totalEnrollment);
						float ll = (totalLastLike == 0 ? 0.0f : 100.0f * lastLike / totalLastLike);
						if (enrl < 3.0f && ll < 3.0f) {
							i.remove();
						}
					}
				}
				*/
				
				sLog.info("Loaded 1 curriculum (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
				return curriculumIfc;
			} finally {
				hibSession.close();
			}
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public Boolean saveCurriculum(CurriculumInterface curriculum) throws CurriculaException {
		try {
			sLog.info("saveCurriculum(curriculum=" + curriculum.getId() + ")");
			Long s0 = System.currentTimeMillis();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				Hashtable<String, CourseOffering> courses = new Hashtable<String, CourseOffering>();
				for (CourseInterface course: curriculum.getCourses()) {
					CourseOffering courseOffering = null;
					for (CourseOffering co: (List<CourseOffering>)hibSession.createQuery(
							"select c from CourseOffering c where " +
							"c.subjectArea.session.uniqueId = :sessionId and " +
							"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course")
							.setString("course", course.getCourseName().toLowerCase())
							.setLong("sessionId", sessionId)
							.setCacheable(true).setMaxResults(1).list()) {
						courseOffering = co; break;
					}
					if (courseOffering == null) throw new CurriculaException("course " + course.getCourseName() + " does not exist");
					courses.put(course.getCourseName(), courseOffering);
				}
			
				Curriculum c = null;
				if (curriculum.getId() != null) {
					c = CurriculumDAO.getInstance().get(curriculum.getId(), hibSession);
					if (c == null) throw new CurriculaException("Curriculum " + curriculum.getId() + " no longer exists.");
				} else {
					c = new Curriculum();
				}
				c.setAbbv(curriculum.getAbbv());
				c.setName(curriculum.getName());
				c.setAcademicArea(AcademicAreaDAO.getInstance().get(curriculum.getAcademicArea().getId(), hibSession));
				c.setDepartment(DepartmentDAO.getInstance().get(curriculum.getDepartment().getId(), hibSession));
				if (c.getMajors() == null) {
					c.setMajors(new HashSet());
					for (MajorInterface m: curriculum.getMajors()) {
						c.getMajors().add(PosMajorDAO.getInstance().get(m.getId(), hibSession));
					}
				} else {
					HashSet<PosMajor> remove = new HashSet<PosMajor>(c.getMajors());
					majors: for (MajorInterface m: curriculum.getMajors()) {
						for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext();) {
							PosMajor major = i.next();
							if (major.getUniqueId().equals(m.getId())) {
								remove.remove(major); continue majors;
							}
						}
						c.getMajors().add(PosMajorDAO.getInstance().get(m.getId(), hibSession));
					}
					if (!remove.isEmpty())
						c.getMajors().removeAll(remove);
				}
				hibSession.saveOrUpdate(c);

				Hashtable<Long, CurriculumClassification> classifications = new Hashtable<Long, CurriculumClassification>();
				Hashtable<String, CurriculumCourseGroup> groups = new Hashtable<String, CurriculumCourseGroup>();
				HashSet<CurriculumCourse> remaining = new HashSet<CurriculumCourse>();
				HashSet<CurriculumCourseGroup> remainingGroups = new HashSet<CurriculumCourseGroup>();
				int ord = 0;
				if (c.getClassifications() == null) {
					c.setClassifications(new HashSet());
					for (CurriculumClassificationInterface clasf: curriculum.getClassifications()) {
						CurriculumClassification cl = new CurriculumClassification();
						cl.setAcademicClassification(AcademicClassificationDAO.getInstance().get(clasf.getAcademicClassification().getId()));
						cl.setName(clasf.getName().isEmpty() ? clasf.getAcademicClassification().getCode() : clasf.getName());
						cl.setNrStudents(clasf.getExpected());
						cl.setLlStudents(clasf.getLastLike());
						cl.setCurriculum(c);
						cl.setOrd(ord++);
						c.getClassifications().add(cl);
						classifications.put(cl.getAcademicClassification().getUniqueId(), cl);
						hibSession.saveOrUpdate(cl);
					}
				} else {
					HashSet<CurriculumClassification> remove = new HashSet<CurriculumClassification>(c.getClassifications());
					clasf: for (CurriculumClassificationInterface clasf: curriculum.getClassifications()) {
						for (Iterator<CurriculumClassification> i = c.getClassifications().iterator(); i.hasNext();) {
							CurriculumClassification cl = i.next();
							if (cl.getAcademicClassification().getUniqueId().equals(clasf.getAcademicClassification().getId())) {
								cl.setName(clasf.getName().isEmpty() ? clasf.getAcademicClassification().getCode() : clasf.getName());
								cl.setNrStudents(clasf.getExpected());
								remove.remove(cl);
								classifications.put(cl.getAcademicClassification().getUniqueId(), cl);
								for (Iterator<CurriculumCourse> j = cl.getCourses().iterator(); j.hasNext();) {
									CurriculumCourse cc = j.next();
									for (Iterator<CurriculumCourseGroup> k = cc.getGroups().iterator(); k.hasNext(); ) {
										CurriculumCourseGroup g = k.next();
										groups.put(g.getName(), g);
										remainingGroups.add(g);
									}
									remaining.add(cc);
								}
								hibSession.saveOrUpdate(cl);
								continue clasf;
							}
						}
						CurriculumClassification cl = new CurriculumClassification();
						cl.setAcademicClassification(AcademicClassificationDAO.getInstance().get(clasf.getAcademicClassification().getId()));
						cl.setName(clasf.getName().isEmpty() ? clasf.getAcademicClassification().getCode() : clasf.getName());
						cl.setNrStudents(clasf.getExpected());
						cl.setLlStudents(clasf.getLastLike());
						cl.setOrd(ord++);
						cl.setCurriculum(c);
						c.getClassifications().add(cl);
						classifications.put(cl.getAcademicClassification().getUniqueId(), cl);
						hibSession.saveOrUpdate(cl);
					}
					if (!remove.isEmpty()) {
						for (CurriculumClassification cl: remove) {
							for (Iterator<CurriculumCourse> j = cl.getCourses().iterator(); j.hasNext();) {
								CurriculumCourse cc = j.next();
								for (Iterator<CurriculumCourseGroup> k = cc.getGroups().iterator(); k.hasNext(); ) {
									CurriculumCourseGroup g = k.next();
									groups.put(g.getName(), g);
									remainingGroups.add(g);
								}
								remaining.add(cc);
							}
							c.getClassifications().remove(cl);
							cl.setCurriculum(null);
							hibSession.delete(cl);
						}
					}
				}

				for (CourseInterface course: curriculum.getCourses()) {
					CourseOffering courseOffering = courses.get(course.getCourseName());
					for (CurriculumCourseInterface cc: course.getCurriculumCourses()) {
						if (cc == null) continue;
						CurriculumClassification clasf = classifications.get(cc.getCurriculumClassificationId());
						if (clasf == null) continue;
						CurriculumCourse cx = null;
						if (clasf.getCourses() == null) {
							clasf.setCourses(new HashSet());
						} else {
							for (Iterator<CurriculumCourse> i = clasf.getCourses().iterator(); i.hasNext();) {
								CurriculumCourse x = i.next();
								if (x.getCourse().equals(courseOffering)) {
									x.setPercShare(cc.getShare());
									remaining.remove(x);
									cx = x;
									break;
								}
							}
						}
						if (cx == null) {
							cx = new CurriculumCourse();
							clasf.getCourses().add(cx);
							cx.setClassification(clasf);
							cx.setCourse(courseOffering);
							cx.setPercShare(cc.getShare());
							cx.setOrd(ord++);
							if (cc.getLastLike() != null && clasf.getLlStudents() != null)
								cx.setLlShare(((float)cc.getLastLike()) / clasf.getLlStudents());
						}
						if (course.hasGroups()) {
							if (cx.getGroups() == null) cx.setGroups(new HashSet());
							HashSet<CurriculumCourseGroup> delete = new HashSet<CurriculumCourseGroup>(cx.getGroups());
							for (CurriculumCourseGroupInterface gr: course.getGroups()) {
								CurriculumCourseGroup g = groups.get(gr.getName());
								if (g == null) {
									g = new CurriculumCourseGroup();
									g.setName(gr.getName());
									g.setColor(gr.getColor());
									g.setType(gr.getType());
									g.setCurriculum(c);
									groups.put(g.getName(), g);
									hibSession.saveOrUpdate(g);
								} else {
									g.setName(gr.getName());
									g.setColor(gr.getColor());
									g.setType(gr.getType());
									hibSession.saveOrUpdate(g);
									remainingGroups.remove(g);
								}
								if (!delete.remove(g)) {
									cx.getGroups().add(g);
								}
							}
							if (!delete.isEmpty()) {
								cx.getGroups().removeAll(delete);
							}
						} else if (cx.getGroups() != null && !cx.getGroups().isEmpty()) {
							cx.getGroups().clear();
						}
						hibSession.saveOrUpdate(cx);
					}
				}
				
				if (!remaining.isEmpty()) {
					for (CurriculumCourse cc: remaining) {
						cc.getClassification().getCourses().remove(cc);
						cc.setClassification(null);
						cc.getGroups().clear();
						hibSession.delete(cc);
					}
				}
				
				if (!remainingGroups.isEmpty()) {
					for (CurriculumCourseGroup g: remainingGroups) {
						hibSession.delete(g);
					}
				}
				
				hibSession.saveOrUpdate(c);
				hibSession.flush();
				tx.commit(); tx = null;
			} finally {
				try {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				} catch (Exception e) {}
				hibSession.close();
			}
			sLog.info("Saved 1 curriculum (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public Boolean deleteCurriculum(Long curriculumId) throws CurriculaException {
		try {
			sLog.info("deleteCurriculum(curriculumId=" + curriculumId + ")");
			Long s0 = System.currentTimeMillis();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				if (curriculumId == null) 
					throw new CurriculaException("Unsaved curriculum cannot be deleted.");
				
				Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
				if (c == null) throw new CurriculaException("Curriculum " + curriculumId + " no longer exists.");
				
				hibSession.delete(c);
				hibSession.flush();
				tx.commit(); tx = null;
				
			} finally {
				try {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				} catch (Exception e) {}
				hibSession.close();
			}
			sLog.info("Deleted 1 curriculum (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public TreeSet<AcademicClassificationInterface> loadAcademicClassifications() throws CurriculaException {
		try {
			sLog.info("loadAcademicClassifications()");
			Long s0 = System.currentTimeMillis();

			TreeSet<AcademicClassificationInterface> results = new TreeSet<AcademicClassificationInterface>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Long sessionId = getAcademicSessionId();
				for (AcademicClassification clasf: (List<AcademicClassification>)hibSession.createQuery(
						"select c from AcademicClassification c where c.session.uniqueId = :sessionId")
						.setLong("sessionId", sessionId).setCacheable(true).list()) {
					AcademicClassificationInterface aci = new AcademicClassificationInterface();
					aci.setId(clasf.getUniqueId());
					aci.setName(clasf.getName());
					aci.setCode(clasf.getCode());
					results.add(aci);
				}
			} finally {
				hibSession.close();
			}
			sLog.info("Loaded " + results.size() + " academic classifications (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public TreeSet<CurriculumClassificationInterface> makupClassifications(Long acadAreaId, List<Long> majors, boolean includeCourses) throws CurriculaException {
		try {
			sLog.info("makupClassifications(acadAreaId=" + acadAreaId + ", majors=" + majors + ", includeCourses=" + includeCourses + ")");
			Long s0 = System.currentTimeMillis();

			if (acadAreaId == null) return new TreeSet<CurriculumClassificationInterface>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			TreeSet<CurriculumClassificationInterface> results = new TreeSet<CurriculumClassificationInterface>();
			try {
				Long sessionId = getAcademicSessionId();
				
				Hashtable<Long, Integer> classifications = new Hashtable<Long, Integer>();
				int idx = 0;
				for (AcademicClassificationInterface clasf: loadAcademicClassifications()) {
					classifications.put(clasf.getId(), idx++);
				}
				
				String majorIds = "";
				for (Long majorId: majors) {
					if (!majorIds.isEmpty()) { majorIds += ","; }
					majorIds += majorId;
				}

				Hashtable<Long, Hashtable<CourseInterface, Integer>> clasf2course2enrl = null;
				if (includeCourses) {
					clasf2course2enrl = new Hashtable<Long, Hashtable<CourseInterface,Integer>>();
					for (Object[] o : (List<Object[]>)hibSession.createQuery(
							"select a.academicClassification.uniqueId, e.courseOffering.uniqueId, e.courseOffering.subjectArea.subjectAreaAbbreviation || ' ' || e.courseOffering.courseNbr, count(distinct e.student) from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " + 
							(majorIds.isEmpty() ? "" : " inner join e.student.posMajors m ") + "where " +
							"e.student.session.uniqueId = :sessionId and "+
							"a.academicArea.uniqueId = :areaId " + 
							(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
							"group by a.academicClassification.uniqueId, e.courseOffering.uniqueId, e.courseOffering.subjectArea.subjectAreaAbbreviation, e.courseOffering.courseNbr")
							.setLong("sessionId", sessionId)
							.setLong("areaId", acadAreaId)
							.setCacheable(true).list()) {
						Long clasfId = (Long)o[0];
						Long courseId = (Long)o[1];
						String courseName = (String)o[2];
						int enrl = ((Number)o[3]).intValue();
						CourseInterface course = new CourseInterface();
						course.setId(courseId);
						course.setCourseName(courseName);
						Hashtable<CourseInterface, Integer> course2enrl = clasf2course2enrl.get(clasfId);
						if (course2enrl == null) {
							course2enrl = new Hashtable<CourseInterface, Integer>();
							clasf2course2enrl.put(clasfId, course2enrl);
						}
						course2enrl.put(course, enrl);
					}
				}
				
				Hashtable<Long, Integer> clasf2enrl = new Hashtable<Long, Integer>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, count(distinct e.student) from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join e.student.posMajors m ") + "where " +
						"e.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
						"group by a.academicClassification.uniqueId")
						.setLong("sessionId", sessionId)
						.setLong("areaId", acadAreaId)
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					int enrl = ((Number)o[1]).intValue();
					clasf2enrl.put(clasfId, enrl);
				}
				
				Hashtable<Long, Hashtable<CourseInterface, Integer>> clasf2course2ll = null;
				if (includeCourses) {
					clasf2course2ll = new Hashtable<Long, Hashtable<CourseInterface,Integer>>();
				}
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, co.uniqueId, co.subjectArea.subjectAreaAbbreviation || ' ' || co.courseNbr, count(distinct x.student) from LastLikeCourseDemand x inner join x.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join x.student.posMajors m ") + ", CourseOffering co where " +
						"x.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
						"and co.subjectArea.uniqueId = x.subjectArea.uniqueId and " +
						"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr)) " +
						"group by a.academicClassification.uniqueId, co.uniqueId, co.subjectArea.subjectAreaAbbreviation, co.courseNbr")
						.setLong("sessionId", sessionId)
						.setLong("areaId", acadAreaId)
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					Long courseId = (Long)o[1];
					String courseName = (String)o[2];
					int enrl = ((Number)o[3]).intValue();
					CourseInterface course = new CourseInterface();
					course.setId(courseId);
					course.setCourseName(courseName);
					Hashtable<CourseInterface, Integer> course2enrl = clasf2course2ll.get(clasfId);
					if (course2enrl == null) {
						course2enrl = new Hashtable<CourseInterface, Integer>();
						clasf2course2ll.put(clasfId, course2enrl);
					}
					course2enrl.put(course, enrl);
				}
				
				Hashtable<Long, Integer> clasf2ll = new Hashtable<Long, Integer>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, count(distinct x.student) from LastLikeCourseDemand x inner join x.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join x.student.posMajors m ") + "where " +
						"x.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
						"group by a.academicClassification.uniqueId")
						.setLong("sessionId", sessionId)
						.setLong("areaId", acadAreaId)
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					int enrl = ((Number)o[1]).intValue();
					clasf2ll.put(clasfId, enrl);
				}
				
				for (AcademicClassification clasf: (List<AcademicClassification>)hibSession.createQuery(
						"select c from AcademicClassification c where c.session.uniqueId = :sessionId")
						.setLong("sessionId", sessionId).setCacheable(true).list()) {
					CurriculumClassificationInterface curClasfIfc = new CurriculumClassificationInterface();
					curClasfIfc.setId(null);
					curClasfIfc.setName(clasf.getCode());
					curClasfIfc.setCurriculumId(null);
					curClasfIfc.setLastLike(clasf2ll.get(clasf.getUniqueId()));
					curClasfIfc.setEnrollment(clasf2enrl.get(clasf.getUniqueId()));
					//cfi.setNrStudents(null);
					if (includeCourses) {
						Hashtable<CourseInterface, Integer> lastLike = clasf2course2ll.get(clasf.getUniqueId());
						Hashtable<CourseInterface, Integer> enrollment = clasf2course2enrl.get(clasf.getUniqueId());
						TreeSet<CourseInterface> courses = new TreeSet<CourseInterface>(new Comparator<CourseInterface>() {
							public int compare(CourseInterface c1, CourseInterface c2) {
								int cmp = c1.getCourseName().compareTo(c2.getCourseName());
								if (cmp != 0) return cmp;
								return c1.getId().compareTo(c2.getId());
							}
						});
						if (lastLike != null)
							courses.addAll(lastLike.keySet());
						if (enrollment != null)
							courses.addAll(enrollment.keySet());
						for (CourseInterface co: courses) {
							CurriculumCourseInterface curCourseIfc = new CurriculumCourseInterface();
							curCourseIfc.setId(null);
							curCourseIfc.setCourseOfferingId(co.getId());
							curCourseIfc.setCurriculumClassificationId(clasf.getUniqueId());
							curCourseIfc.setCourseName(co.getCourseName());
							curCourseIfc.setEnrollment(enrollment == null ? null : enrollment.get(co));
							curCourseIfc.setLastLike(lastLike == null ? null : lastLike.get(co));
							if (curCourseIfc.getLastLike() != null && curClasfIfc.getLastLike() != null)
								curCourseIfc.setShare(((float)curCourseIfc.getLastLike()) / curClasfIfc.getLastLike());
							else if (curCourseIfc.getEnrollment() != null && curClasfIfc.getEnrollment() != null)
								curCourseIfc.setShare(((float)curCourseIfc.getEnrollment()) / curClasfIfc.getEnrollment());
							curClasfIfc.addCourse(curCourseIfc);
						}	
					}
					AcademicClassificationInterface acadClasfIfc = new AcademicClassificationInterface();
					acadClasfIfc.setId(clasf.getUniqueId());
					acadClasfIfc.setName(clasf.getName());
					acadClasfIfc.setCode(clasf.getCode());
					curClasfIfc.setAcademicClassification(acadClasfIfc);
					results.add(curClasfIfc);
				}
			} finally {
				hibSession.close();
			}
			sLog.info(results.size() + " classifications made up (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public HashMap<String, Integer[][]> computeEnrollmentsAndLastLikes(Long acadAreaId, List<Long> majors) throws CurriculaException {
		try {
			sLog.info("computeEnrollmentsAndLastLikes(acadAreaId=" + acadAreaId + ", majors=" + majors + ")");
			Long s0 = System.currentTimeMillis();
			if (acadAreaId == null) return new HashMap<String, Integer[][]>();
			Hashtable<Long, Integer> classifications = new Hashtable<Long, Integer>();
			int idx = 0;
			for (AcademicClassificationInterface clasf: loadAcademicClassifications()) {
				classifications.put(clasf.getId(), idx++);
			}
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			HashMap<String, Integer[][]> results = new HashMap<String, Integer[][]>();
			try {
				Long sessionId = getAcademicSessionId();
								
				String majorIds = "";
				for (Long majorId: majors) {
					if (!majorIds.isEmpty()) { majorIds += ","; }
					majorIds += majorId;
				}
				
				Hashtable<Long, Integer> clasf2enrl = new Hashtable<Long, Integer>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, count(distinct e.student) from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join e.student.posMajors m ") + "where " +
						"e.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
						"group by a.academicClassification.uniqueId")
						.setLong("sessionId", sessionId)
						.setLong("areaId", acadAreaId)
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					int enrl = ((Number)o[1]).intValue();
					clasf2enrl.put(clasfId, enrl);
				}

				Hashtable<Long, Hashtable<CourseInterface, Integer>> clasf2course2enrl = new Hashtable<Long, Hashtable<CourseInterface,Integer>>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, e.courseOffering.uniqueId, e.courseOffering.subjectArea.subjectAreaAbbreviation || ' ' || e.courseOffering.courseNbr, count(distinct e.student) from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join e.student.posMajors m ") + "where " +
						"e.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
						"group by a.academicClassification.uniqueId, e.courseOffering.uniqueId, e.courseOffering.subjectArea.subjectAreaAbbreviation, e.courseOffering.courseNbr")
						.setLong("sessionId", sessionId)
						.setLong("areaId", acadAreaId)
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					Long courseId = (Long)o[1];
					String courseName = (String)o[2];
					int enrl = ((Number)o[3]).intValue();
					CourseInterface course = new CourseInterface();
					course.setId(courseId);
					course.setCourseName(courseName);
					Hashtable<CourseInterface, Integer> course2enrl = clasf2course2enrl.get(clasfId);
					if (course2enrl == null) {
						course2enrl = new Hashtable<CourseInterface, Integer>();
						clasf2course2enrl.put(clasfId, course2enrl);
					}
					course2enrl.put(course, enrl);
				}
				
				Hashtable<Long, Integer> clasf2ll = new Hashtable<Long, Integer>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, count(distinct x.student) from LastLikeCourseDemand x inner join x.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join x.student.posMajors m ") + "where " +
						"x.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
						"group by a.academicClassification.uniqueId")
						.setLong("sessionId", sessionId)
						.setLong("areaId", acadAreaId)
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					int enrl = ((Number)o[1]).intValue();
					clasf2ll.put(clasfId, enrl);
				}
				
				Hashtable<Long, Hashtable<CourseInterface, Integer>> clasf2course2ll = new Hashtable<Long, Hashtable<CourseInterface,Integer>>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, co.uniqueId, co.subjectArea.subjectAreaAbbreviation || ' ' || co.courseNbr, count(distinct x.student) from LastLikeCourseDemand x inner join x.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join x.student.posMajors m ") + ", CourseOffering co where " +
						"x.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
						"and co.subjectArea.uniqueId = x.subjectArea.uniqueId and " +
						"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr)) " +
						"group by a.academicClassification.uniqueId, co.uniqueId, co.subjectArea.subjectAreaAbbreviation, co.courseNbr")
						.setLong("sessionId", sessionId)
						.setLong("areaId", acadAreaId)
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					Long courseId = (Long)o[1];
					String courseName = (String)o[2];
					int enrl = ((Number)o[3]).intValue();
					CourseInterface course = new CourseInterface();
					course.setId(courseId);
					course.setCourseName(courseName);
					Hashtable<CourseInterface, Integer> course2enrl = clasf2course2ll.get(clasfId);
					if (course2enrl == null) {
						course2enrl = new Hashtable<CourseInterface, Integer>();
						clasf2course2ll.put(clasfId, course2enrl);
					}
					course2enrl.put(course, enrl);
				}
				
				for (Long clasfId: (List<Long>)hibSession.createQuery(
						"select c.uniqueId from AcademicClassification c where c.session.uniqueId = :sessionId " + 
						"order by c.code, c.name")
						.setLong("sessionId", sessionId).setCacheable(true).list()) {
					
					Integer[][] x = results.get("");
					if (x == null) {
						x = new Integer[classifications.size()][2];
						results.put("", x);
					}
					x[classifications.get(clasfId)][0] = clasf2enrl.get(clasfId);
					x[classifications.get(clasfId)][1] = clasf2ll.get(clasfId);
					
					Hashtable<CourseInterface, Integer> lastLike = clasf2course2ll.get(clasfId);
					Hashtable<CourseInterface, Integer> enrollment = clasf2course2enrl.get(clasfId);
					
					TreeSet<CourseInterface> courses = new TreeSet<CourseInterface>(new Comparator<CourseInterface>() {
						public int compare(CourseInterface c1, CourseInterface c2) {
							int cmp = c1.getCourseName().compareTo(c2.getCourseName());
							if (cmp != 0) return cmp;
							return c1.getId().compareTo(c2.getId());
						}
					});
					
					if (lastLike != null)
						courses.addAll(lastLike.keySet());
					
					if (enrollment != null)
						courses.addAll(enrollment.keySet());
					
					for (CourseInterface co: courses) {
						Integer[][] c = results.get(co.getCourseName());
						if (c == null) {
							c = new Integer[classifications.size()][2];
							results.put(co.getCourseName(), c);
						}
						c[classifications.get(clasfId)][0] = (enrollment == null ? null : enrollment.get(co));
						c[classifications.get(clasfId)][1] = (lastLike == null ? null : lastLike.get(co));
					}
				}
			} finally {
				hibSession.close();
			}
			sLog.info("Found " + results.size() + " courses with enrollments/last-like data (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public TreeSet<AcademicAreaInterface> loadAcademicAreas() throws CurriculaException {
		try {
			sLog.info("loadAcademicAreas()");
			Long s0 = System.currentTimeMillis();
			TreeSet<AcademicAreaInterface> results = new TreeSet<AcademicAreaInterface>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			try {
				List<AcademicArea> areas = hibSession.createQuery(
						"select a from AcademicArea a where a.session.uniqueId = :sessionId order by a.academicAreaAbbreviation, a.longTitle, a.shortTitle")
						.setLong("sessionId", sessionId).setCacheable(true).list();
				for (AcademicArea a: areas) {
					AcademicAreaInterface ai = new AcademicAreaInterface();
					ai.setId(a.getUniqueId());
					ai.setAbbv(a.getAcademicAreaAbbreviation());
					ai.setName(Constants.toInitialCase(a.getLongTitle() == null ? a.getShortTitle() : a.getLongTitle()));
					results.add(ai);
				}
			} finally {
				hibSession.close();
			}
			sLog.info("Loaded " + results.size() + " academic areas (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public TreeSet<MajorInterface> loadMajors(Long curriculumId, Long academicAreaId) throws CurriculaException {
		try {
			sLog.info("loadMajors(academicAreaId=" + academicAreaId + ")");
			Long s0 = System.currentTimeMillis();
			TreeSet<MajorInterface> results = new TreeSet<MajorInterface>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			try {
				AcademicArea area = AcademicAreaDAO.getInstance().get(academicAreaId, hibSession);
				if (area == null) return results;
				TreeSet<PosMajor> majors = new TreeSet<PosMajor>(new Comparator<PosMajor>() {
					public int compare(PosMajor m1, PosMajor m2) {
						int cmp = m1.getName().compareToIgnoreCase(m2.getName());
						if (cmp != 0) return cmp;
						cmp = m1.getCode().compareTo(m2.getCode());
						if (cmp != 0) return cmp;
						return m1.getUniqueId().compareTo(m2.getUniqueId());
					}
				});
				majors.addAll(area.getPosMajors());
				majors.removeAll(
						hibSession.createQuery("select m from Curriculum c inner join c.majors m where c.academicArea = :academicAreaId and c.uniqueId != :curriculumId")
						.setLong("academicAreaId", academicAreaId).setLong("curriculumId", (curriculumId == null ? -1l : curriculumId)).setCacheable(true).list());
				for (PosMajor m: majors) {
					MajorInterface mi = new MajorInterface();
					mi.setId(m.getUniqueId());
					mi.setCode(m.getCode());
					mi.setName(Constants.toInitialCase(m.getName()));
					results.add(mi);
				}
			} finally {
				hibSession.close();
			}
			sLog.info("Loaded " + results.size() + " majors (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public TreeSet<DepartmentInterface> loadDepartments() throws CurriculaException {
		try {
			sLog.info("loadDepartments()");
			Long s0 = System.currentTimeMillis();
			TreeSet<DepartmentInterface> results = new TreeSet<DepartmentInterface>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			try {
				List<Department> depts = hibSession.createQuery(
						"select d from Department d where d.session.uniqueId = :sessionId order by d.deptCode")
						.setLong("sessionId", sessionId).setCacheable(true).list();
				for (Department d: depts) {
					DepartmentInterface di = new DepartmentInterface();
					di.setId(d.getUniqueId());
					di.setCode(d.getDeptCode());
					di.setAbbv(d.getAbbreviation());
					di.setName(d.getName());
					results.add(di);
				}
			} finally {
				hibSession.close();
			}
			sLog.info("Loaded " + results.size() + " departments (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}

	
	public String lastCurriculaFilter() throws CurriculaException {
		sLog.info("lastCurriculaFilter()");
		Long s0 = System.currentTimeMillis();
		String filter = (String)getThreadLocalRequest().getSession().getAttribute("Curricula.LastFilter");
		if (filter == null) {
			filter = "";
			Long sessionId = getAcademicSessionId();
			for (Iterator<Department> i = getManager().getDepartments().iterator(); i.hasNext(); ) {
				Department d = i.next();
				if (d.getSession().getUniqueId().equals(sessionId)) {
					if (!filter.isEmpty()) filter += " or ";
					filter += "dept:" + d.getDeptCode();
				}
			}
		}
		sLog.info("Last filter is '" + filter + "'  (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
		return filter;
	}

	private TimetableManager getManager() {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new CurriculaException("not authenticated");
		TimetableManager manager = TimetableManager.getManager(user);
		if (manager == null) throw new CurriculaException("access denied");
		return manager;
	}
	
	private Long getAcademicSessionId() {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new CurriculaException("not authenticated");
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		if (sessionId == null) throw new CurriculaException("academic session not selected");
		return sessionId;
	}
	
	private class CurriculaMatcher implements Query.TermMatcher {
		private Curriculum iCurriculum;
		
		private CurriculaMatcher(Curriculum c) {
			iCurriculum = c;
		}
		
		public boolean match(String attr, String term) {
			if (term.isEmpty()) return true;
			if (attr == null || "dept".equals(attr)) {
				if (eq(iCurriculum.getDepartment().getDeptCode(), term) ||
					eq(iCurriculum.getDepartment().getAbbreviation(), term) ||
					has(iCurriculum.getDepartment().getName(), term)) return true;
			}
			if (attr == null || "abbv".equals(attr) || "curricula".equals(attr)) {
				if (eq(iCurriculum.getAbbv(), term)) return true;
			}
			if (attr == null || "name".equals(attr) || "curricula".equals(attr)) {
				if (has(iCurriculum.getName(), term)) return true;
			}
			if (attr == null || "area".equals(attr)) {
				if (eq(iCurriculum.getAcademicArea().getAcademicAreaAbbreviation(), term) ||
					has(iCurriculum.getAcademicArea().getShortTitle(), term) ||
					has(iCurriculum.getAcademicArea().getLongTitle(), term)) return true;
			}
			if (attr == null || "major".equals(attr)) {
				for (Iterator<PosMajor> i = iCurriculum.getMajors().iterator(); i.hasNext(); ) {
					PosMajor m = i.next();
					if (eq(m.getCode(), term) || has(m.getName(), term)) return true;
				}
			}
			return false;
		}
		
		private boolean eq(String name, String term) {
			if (name == null) return false;
			return name.equalsIgnoreCase(term);
		}

		private boolean has(String name, String term) {
			if (name == null) return false;
			for (String t: name.split(" "))
				if (t.equalsIgnoreCase(term)) return true;
			return false;
		}
	
	}
	
	public Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(String query, Integer limit) throws CurriculaException {
		try {
			sLog.info("listCourseOfferings(query='" + query + "', limit=" + limit + ")");
			Long s0 = System.currentTimeMillis();
			ArrayList<ClassAssignmentInterface.CourseAssignment> results = new ArrayList<ClassAssignmentInterface.CourseAssignment>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			try {
				for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
						"select c from CourseOffering c where " +
						"c.subjectArea.session.uniqueId = :sessionId and (" +
						"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' " +
						(query.length()>2 ? "or lower(c.title) like '%' || :q || '%'" : "") + ")")
						.setString("q", query.toLowerCase())
						.setLong("sessionId", sessionId)
						.setCacheable(true).setMaxResults(limit == null || limit < 0 ? Integer.MAX_VALUE : limit).list()) {
					CourseAssignment course = new CourseAssignment();
					course.setCourseId(c.getUniqueId());
					course.setSubject(c.getSubjectAreaAbbv());
					course.setCourseNbr(c.getCourseNbr());
					course.setNote(c.getScheduleBookNote());
					course.setTitle(c.getTitle());
					course.setHasUniqueName(true);
					results.add(course);
				}
			} finally {
				hibSession.close();
			}
			sLog.info("Found " + results.size() + " course offerings  (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public String retrieveCourseDetails(String course) throws CurriculaException {
		try {
			sLog.info("retrieveCourseDetails(course='" + course + "')");
			Long s0 = System.currentTimeMillis();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			try {
				CourseOffering courseOffering = null;
				for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
						"select c from CourseOffering c where " +
						"c.subjectArea.session.uniqueId = :sessionId and " +
						"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course")
						.setString("course", course.toLowerCase())
						.setLong("sessionId", sessionId)
						.setCacheable(true).setMaxResults(1).list()) {
					courseOffering = c; break;
				}
				if (courseOffering == null) throw new CurriculaException("course " + course + " does not exist");
				CourseDetailsProvider provider = null;
				try {
					provider = (CourseDetailsProvider)Class.forName(ApplicationProperties.getProperty("unitime.custom.CourseDetailsProvider")).newInstance();
				} catch (Exception e) {
					throw new CurriculaException("course detail interface not provided");
				}
				String details = provider.getDetails(
						new AcademicSessionInfo(courseOffering.getSubjectArea().getSession()),
						courseOffering.getSubjectAreaAbbv(), courseOffering.getCourseNbr());
				sLog.info("Details of length " + details.length() + " retrieved (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
				return details;
			} finally {
				hibSession.close();
			}
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public Collection<ClassAssignmentInterface.ClassAssignment> listClasses(String course) throws CurriculaException {
		try {
			sLog.info("listClasses(course='" + course + "')");
			Long s0 = System.currentTimeMillis();
			ArrayList<ClassAssignmentInterface.ClassAssignment> results = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			try {
				CourseOffering courseOffering = null;
				for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
						"select c from CourseOffering c where " +
						"c.subjectArea.session.uniqueId = :sessionId and " +
						"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course")
						.setString("course", course.toLowerCase())
						.setLong("sessionId", sessionId)
						.setCacheable(true).setMaxResults(1).list()) {
					courseOffering = c; break;
				}
				if (courseOffering == null) throw new CurriculaException("course " + course + " does not exist");
				List<Class_> classes = new ArrayList<Class_>();
				for (Iterator<InstrOfferingConfig> i = courseOffering.getInstructionalOffering().getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
					InstrOfferingConfig config = i.next();
					for (Iterator<SchedulingSubpart> j = config.getSchedulingSubparts().iterator(); j.hasNext(); ) {
						SchedulingSubpart subpart = j.next();
						classes.addAll(subpart.getClasses());
					}
				}
				Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
				for (Class_ clazz: classes) {
					ClassAssignmentInterface.ClassAssignment a = new ClassAssignmentInterface.ClassAssignment();
					a.setClassId(clazz.getUniqueId());
					a.setSubpart(clazz.getSchedulingSubpart().getItypeDesc());
					a.setSection(clazz.getClassSuffix(courseOffering));
					
					Assignment ass = clazz.getCommittedAssignment();
					Placement p = (ass == null ? null : ass.getPlacement());
					
                    int minLimit = clazz.getExpectedCapacity();
                	int maxLimit = clazz.getMaxExpectedCapacity();
                	int limit = maxLimit;
                	if (minLimit < maxLimit && p != null) {
                		int roomLimit = Math.round((clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()) * p.getRoomSize());
                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
                	}
                    if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = -1;
					a.setLimit(new int[] {-1, limit});
					
					if (p != null && p.getTimeLocation() != null) {
						for (DayCode d: DayCode.toDayCodes(p.getTimeLocation().getDayCode()))
							a.addDay(d.getIndex());
						a.setStart(p.getTimeLocation().getStartSlot());
						a.setLength(p.getTimeLocation().getLength());
						a.setBreakTime(p.getTimeLocation().getBreakTime());
						a.setDatePattern(p.getTimeLocation().getDatePatternName());
					}
					if (p != null && p.getRoomLocations() != null) {
						for (Enumeration<RoomLocation> e = p.getRoomLocations().elements(); e.hasMoreElements(); ) {
							RoomLocation rm = e.nextElement();
							a.addRoom(rm.getName());
						}
					}
					if (p != null && p.getRoomLocation() != null) {
						a.addRoom(p.getRoomLocation().getName());
					}
					if (!clazz.getClassInstructors().isEmpty()) {
						for (Iterator<ClassInstructor> i = clazz.getClassInstructors().iterator(); i.hasNext(); ) {
							ClassInstructor instr = i.next();
							a.addInstructor(instr.getInstructor().getName(DepartmentalInstructor.sNameFormatShort));
							a.addInstructoEmailr(instr.getInstructor().getEmail());
						}
					}
					if (clazz.getParentClass() != null)
						a.setParentSection(clazz.getParentClass().getClassSuffix(courseOffering));
					a.setSubpartId(clazz.getSchedulingSubpart().getUniqueId());
					if (a.getParentSection() == null)
						a.setParentSection(courseOffering.getInstructionalOffering().getConsentType() == null ? null : courseOffering.getInstructionalOffering().getConsentType().getLabel());
					//TODO: Do we want to populate expected space?
					a.setExpected(0.0);
					results.add(a);
				}
			} finally {
				hibSession.close();
			}
			sLog.info("Found " + results.size() + " classes (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public String[] getAppliationProperty(String[] name) throws CurriculaException {
		String[] ret = new String[name.length];
		for (int i = 0; i < name.length; i++)
			ret[i] = ApplicationProperties.getProperty(name[i]);
		return ret;
	}

}
