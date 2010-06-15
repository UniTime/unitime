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
import net.sf.cpsolver.ifs.util.ToolBox;

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
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumCourseGroup;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
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
			User user = Web.getUser(getThreadLocalRequest().getSession());
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
						ci.setEditable(c.canUserEdit(user));
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
					TreeSet<CurriculumClassification> classifications = new TreeSet<CurriculumClassification>(c.getClassifications());
					for (CurriculumClassification clasf: classifications) {
						CurriculumClassificationInterface cfi = new CurriculumClassificationInterface();
						cfi.setId(clasf.getUniqueId());
						cfi.setName(clasf.getName());
						cfi.setCurriculumId(c.getUniqueId());
						cfi.setLastLike(clasf.getLlStudents());
						cfi.setExpected(clasf.getNrStudents());
						cfi.setEnrollment(clasf2enrl.get(clasf.getAcademicClassification().getUniqueId()));
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
			
			User user = Web.getUser(getThreadLocalRequest().getSession());

			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Long sessionId = getAcademicSessionId();
				Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
				if (c == null) throw new CurriculaException("curriculum " + curriculumId + " does not exist");
				CurriculumInterface curriculumIfc = new CurriculumInterface();
				curriculumIfc.setId(c.getUniqueId());
				curriculumIfc.setAbbv(c.getAbbv());
				curriculumIfc.setName(c.getName());
				curriculumIfc.setEditable(c.canUserEdit(user));
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
			User user = Web.getUser(getThreadLocalRequest().getSession());
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				Hashtable<String, CourseOffering> courses = new Hashtable<String, CourseOffering>();
				if (curriculum.hasCourses())
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
				if (!c.canUserEdit(user)) throw new CurriculaException("You are not authorized to " + (c.getUniqueId() == null ? "create" : "update") + " this curriculum.");
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

				if (curriculum.hasCourses())
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
			User user = Web.getUser(getThreadLocalRequest().getSession());
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				if (curriculumId == null) 
					throw new CurriculaException("Unsaved curriculum cannot be deleted.");
				
				Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
				if (c == null) throw new CurriculaException("Curriculum " + curriculumId + " no longer exists.");
				
				if (!c.canUserEdit(user)) throw new CurriculaException("You are not authorized to delete this curriculum.");
				
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

				AcademicArea acadArea = AcademicAreaDAO.getInstance().get(acadAreaId, hibSession);
								
				String majorIds = "", majorCodes = "";
				for (Long majorId: majors) {
					if (!majorIds.isEmpty()) { majorIds += ","; majorCodes += ","; }
					majorIds += majorId;
					majorCodes += "'" + PosMajorDAO.getInstance().get(majorId,hibSession).getCode() + "'";
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
				
				Hashtable<String, Integer> clasf2ll = new Hashtable<String, Integer>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select f.code, count(distinct s) from LastLikeCourseDemand x inner join x.student s " +
						"inner join s.academicAreaClassifications a inner join a.academicClassification f " + 
						(majorCodes.isEmpty() ? "" : " inner join s.posMajors m ") + "where " +
						"x.subjectArea.session.uniqueId = :sessionId and "+
						"a.academicArea.academicAreaAbbreviation = :acadAbbv " + 
						(majorCodes.isEmpty() ? "" : "and m.code in (" + majorCodes + ") ") +
						"group by f.code")
						.setLong("sessionId", sessionId)
						.setString("acadAbbv", acadArea.getAcademicAreaAbbreviation())
						.setCacheable(true).list()) {
					String clasfCode = (String)o[0];
					int enrl = ((Number)o[1]).intValue();
					clasf2ll.put(clasfCode, enrl);
				}
				
				Hashtable<String, Hashtable<CourseInterface, Integer>> clasf2course2ll = new Hashtable<String, Hashtable<CourseInterface,Integer>>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select f.code, co.uniqueId, co.subjectArea.subjectAreaAbbreviation || ' ' || co.courseNbr, count(distinct s) " +
						"from LastLikeCourseDemand x inner join x.student s inner join s.academicAreaClassifications a inner join a.academicClassification f " + 
						(majorCodes.isEmpty() ? "" : " inner join s.posMajors m ") + ", CourseOffering co where " +
						"x.subjectArea.session.uniqueId = :sessionId and "+
						"a.academicArea.academicAreaAbbreviation = :acadAbbv " + 
						(majorCodes.isEmpty() ? "" : "and m.code in (" + majorCodes + ") ") +
						"and co.subjectArea.uniqueId = x.subjectArea.uniqueId and " +
						"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr)) " +
						"group by f.code, co.uniqueId, co.subjectArea.subjectAreaAbbreviation, co.courseNbr")
						.setLong("sessionId", sessionId)
						.setString("acadAbbv", acadArea.getAcademicAreaAbbreviation())
						.setCacheable(true).list()) {
					String clasfCode = (String)o[0];
					Long courseId = (Long)o[1];
					String courseName = (String)o[2];
					int enrl = ((Number)o[3]).intValue();
					CourseInterface course = new CourseInterface();
					course.setId(courseId);
					course.setCourseName(courseName);
					Hashtable<CourseInterface, Integer> course2enrl = clasf2course2ll.get(clasfCode);
					if (course2enrl == null) {
						course2enrl = new Hashtable<CourseInterface, Integer>();
						clasf2course2ll.put(clasfCode, course2enrl);
					}
					course2enrl.put(course, enrl);
				}
				
				for (AcademicClassification clasf: (List<AcademicClassification>)hibSession.createQuery(
						"select c from AcademicClassification c where c.session.uniqueId = :sessionId " + 
						"order by c.code, c.name")
						.setLong("sessionId", sessionId).setCacheable(true).list()) {
					
					Integer[][] x = results.get("");
					if (x == null) {
						x = new Integer[classifications.size()][2];
						results.put("", x);
					}
					x[classifications.get(clasf.getUniqueId())][0] = clasf2enrl.get(clasf.getUniqueId());
					x[classifications.get(clasf.getUniqueId())][1] = clasf2ll.get(clasf.getCode());
					
					Hashtable<CourseInterface, Integer> lastLike = clasf2course2ll.get(clasf.getCode());
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
						Integer[][] c = results.get(co.getCourseName());
						if (c == null) {
							c = new Integer[classifications.size()][2];
							results.put(co.getCourseName(), c);
						}
						c[classifications.get(clasf.getUniqueId())][0] = (enrollment == null ? null : enrollment.get(co));
						c[classifications.get(clasf.getUniqueId())][1] = (lastLike == null ? null : lastLike.get(co));
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
	
	private TreeSet<CurriculumInterface> loadCurriculaForACourse(org.hibernate.Session hibSession, TreeSet<AcademicClassificationInterface> academicClassifications, TreeSet<AcademicAreaInterface> academicAreas, CourseOffering courseOffering) throws CurriculaException {
		TreeSet<CurriculumInterface> results = new TreeSet<CurriculumInterface>();
		
		Hashtable<Long, Integer> classifications = new Hashtable<Long, Integer>();
		int idx = 0;
		for (AcademicClassificationInterface clasf: academicClassifications) {
			classifications.put(clasf.getId(), idx++);
		}
		Hashtable<String, Long> areasAbbv2Id = new Hashtable<String, Long>();
		for (AcademicAreaInterface area: academicAreas) {
			areasAbbv2Id.put(area.getAbbv(), area.getId());
		}
		
		Hashtable<Long, Hashtable<Long, Hashtable<Long, Integer>>> area2major2clasf2enrl = new Hashtable<Long, Hashtable<Long,Hashtable<Long,Integer>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicArea.uniqueId, m.uniqueId, a.academicClassification.uniqueId, count(distinct e.student) " +
				"from StudentClassEnrollment e inner join e.student.academicAreaClassifications a inner join e.student.posMajors m where " +
				"e.courseOffering.uniqueId = :courseId group by a.academicArea.uniqueId, m.uniqueId, a.academicClassification.uniqueId")
				.setLong("courseId", courseOffering.getUniqueId())
				.setCacheable(true).list()) {
			Long areaId = (Long)o[0];
			Long majorId = (Long)o[1];
			Long clasfId = (Long)o[2];
			int enrl = ((Number)o[3]).intValue();
			Hashtable<Long, Hashtable<Long, Integer>> major2clasf2enrl = area2major2clasf2enrl.get(areaId);
			if (major2clasf2enrl == null) {
				major2clasf2enrl = new Hashtable<Long, Hashtable<Long,Integer>>();
				area2major2clasf2enrl.put(areaId, major2clasf2enrl);
			}
			Hashtable<Long, Integer> clasf2enrl = major2clasf2enrl.get(majorId);
			if (clasf2enrl == null) {
				clasf2enrl = new Hashtable<Long, Integer>();
				major2clasf2enrl.put(majorId, clasf2enrl);
			}
			clasf2enrl.put(clasfId, enrl);
		}
		
		Hashtable<Long, Hashtable<String, Hashtable<String, Integer>>> area2major2clasf2ll = new Hashtable<Long, Hashtable<String,Hashtable<String,Integer>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select r.academicAreaAbbreviation, m.code, f.code, count(distinct s) from " +
				"LastLikeCourseDemand x inner join x.student s inner join s.academicAreaClassifications a inner join s.posMajors m " +
				"inner join a.academicClassification f inner join a.academicArea r, CourseOffering co where " +
				"x.subjectArea.session.uniqueId = :sessionId and co.uniqueId = :courseId and "+
				"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr)) " +
				"group by r.academicAreaAbbreviation, m.code, f.code")
				.setLong("sessionId", courseOffering.getSubjectArea().getSessionId())
				.setLong("courseId", courseOffering.getUniqueId())
				.setCacheable(true).list()) {
			Long areaId = areasAbbv2Id.get((String)o[0]);
			if (areaId == null) continue;
			String majorCode = (String)o[1];
			String clasfCode = (String)o[2];
			int lastLike = ((Number)o[3]).intValue();
			Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(areaId);
			if (major2clasf2ll == null) {
				major2clasf2ll = new Hashtable<String, Hashtable<String,Integer>>();
				area2major2clasf2ll.put(areaId, major2clasf2ll);
			}
			Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(majorCode);
			if (clasf2ll == null) {
				clasf2ll = new Hashtable<String, Integer>();
				major2clasf2ll.put(majorCode, clasf2ll);
			}
			clasf2ll.put(clasfCode, lastLike);
		}

		
		Hashtable<Long, CurriculumInterface> curricula = new Hashtable<Long, CurriculumInterface>();
		Hashtable<Long, Hashtable<Long, Integer>> cur2clasf2enrl = new Hashtable<Long, Hashtable<Long, Integer>>();
		for (CurriculumCourse course : (List<CurriculumCourse>)hibSession.createQuery(
				"select c from CurriculumCourse c where c.course.uniqueId = :courseId")
				.setLong("courseId", courseOffering.getUniqueId()).setCacheable(true).list()) {
			CurriculumClassification clasf = course.getClassification();
			Curriculum curriculum = clasf.getCurriculum();
			
			// create curriculum interface
			CurriculumInterface curriculumIfc = curricula.get(curriculum.getUniqueId());
			if (curriculumIfc == null) {
				curriculumIfc = new CurriculumInterface();
				curriculumIfc.setId(curriculum.getUniqueId());
				curriculumIfc.setAbbv(curriculum.getAbbv());
				curriculumIfc.setName(curriculum.getName());
				AcademicAreaInterface areaIfc = new AcademicAreaInterface();
				areaIfc.setId(curriculum.getAcademicArea().getUniqueId());
				areaIfc.setAbbv(curriculum.getAcademicArea().getAcademicAreaAbbreviation());
				areaIfc.setName(Constants.toInitialCase(curriculum.getAcademicArea().getLongTitle() == null ? curriculum.getAcademicArea().getShortTitle() : curriculum.getAcademicArea().getLongTitle()));
				curriculumIfc.setAcademicArea(areaIfc);
				DepartmentInterface deptIfc = new DepartmentInterface();
				deptIfc.setId(curriculum.getDepartment().getUniqueId());
				deptIfc.setAbbv(curriculum.getDepartment().getAbbreviation());
				deptIfc.setCode(curriculum.getDepartment().getDeptCode());
				deptIfc.setName(curriculum.getDepartment().getName());
				curriculumIfc.setDepartment(deptIfc);
				for (Iterator<PosMajor> i = curriculum.getMajors().iterator(); i.hasNext(); ) {
					PosMajor major = i.next();
					MajorInterface mi = new MajorInterface();
					mi.setId(major.getUniqueId());
					mi.setCode(major.getCode());
					mi.setName(Constants.toInitialCase(major.getName()));
					curriculumIfc.addMajor(mi);
				}
				curricula.put(curriculum.getUniqueId(), curriculumIfc);
				results.add(curriculumIfc);
				
				String majorIds = "";
				for (Iterator<PosMajor> i = curriculum.getMajors().iterator(); i.hasNext(); ) {
					PosMajor major = i.next();
					if (!majorIds.isEmpty()) { majorIds += ","; }
					majorIds += major.getUniqueId();
				}

				Hashtable<Long, Integer> clasf2enrl = new Hashtable<Long, Integer>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, count(distinct e.student) from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join e.student.posMajors m ") + "where " +
						"e.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
						"group by a.academicClassification.uniqueId")
						.setLong("sessionId", courseOffering.getSubjectArea().getSessionId())
						.setLong("areaId", curriculum.getAcademicArea().getUniqueId())
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					int enrl = ((Number)o[1]).intValue();
					clasf2enrl.put(clasfId, enrl);
				}
				cur2clasf2enrl.put(curriculum.getUniqueId(), clasf2enrl);
			}
			
			CurriculumClassificationInterface curClasfIfc = new CurriculumClassificationInterface();
			curClasfIfc.setId(clasf.getUniqueId());
			curClasfIfc.setName(clasf.getName());
			curClasfIfc.setCurriculumId(curriculum.getUniqueId());
			curClasfIfc.setLastLike(clasf.getLlStudents());
			curClasfIfc.setExpected(clasf.getNrStudents());
			curClasfIfc.setEnrollment(cur2clasf2enrl.get(curriculum.getUniqueId()).get(clasf.getAcademicClassification().getUniqueId()));
			AcademicClassificationInterface acadClasfIfc = new AcademicClassificationInterface();
			acadClasfIfc.setId(clasf.getAcademicClassification().getUniqueId());
			acadClasfIfc.setName(clasf.getAcademicClassification().getName());
			acadClasfIfc.setCode(clasf.getAcademicClassification().getCode());
			curClasfIfc.setAcademicClassification(acadClasfIfc);
			curriculumIfc.addClassification(curClasfIfc);
			
			CourseInterface courseIfc = null;
			if (curriculumIfc.hasCourses()) {
				courseIfc = curriculumIfc.getCourses().first();
			} else {
				courseIfc = new CourseInterface();
				courseIfc.setId(course.getCourse().getUniqueId());
				courseIfc.setCourseName(course.getCourse().getCourseName());
				curriculumIfc.addCourse(courseIfc);
			}
			
			CurriculumCourseInterface curCourseIfc = new CurriculumCourseInterface();
			curCourseIfc.setId(course.getUniqueId());
			curCourseIfc.setCourseOfferingId(course.getCourse().getUniqueId());
			curCourseIfc.setCurriculumClassificationId(clasf.getUniqueId());
			curCourseIfc.setShare(course.getPercShare());
			curCourseIfc.setCourseName(course.getCourse().getCourseName());
			
			int enrl = 0;
			Hashtable<Long, Hashtable<Long, Integer>> major2clasf2enrl = area2major2clasf2enrl.get(curriculum.getAcademicArea().getUniqueId());
			if (major2clasf2enrl != null) {
				if (curriculum.getMajors().isEmpty()) {
					for (Long majorId: major2clasf2enrl.keySet()) {
						Hashtable<Long, Integer> clasf2enrl = major2clasf2enrl.get(majorId);
						Integer e = (clasf2enrl == null ? null : clasf2enrl.get(clasf.getAcademicClassification().getUniqueId()));
						if (e != null) {
							enrl += e;
							clasf2enrl.remove(clasf.getAcademicClassification().getUniqueId());
						}
					}
				} else {
					for (Iterator<PosMajor> i = curriculum.getMajors().iterator(); i.hasNext(); ) {
						PosMajor m = i.next();
						Hashtable<Long, Integer> clasf2enrl = major2clasf2enrl.get(m.getUniqueId());
						Integer e = (clasf2enrl == null ? null : clasf2enrl.get(clasf.getAcademicClassification().getUniqueId()));
						if (e != null) {
							enrl += e;
							clasf2enrl.remove(clasf.getAcademicClassification().getUniqueId());
						}
					}
				}
			}
			if (enrl > 0)
				curCourseIfc.setEnrollment(enrl);
			
			int lastLike = 0;
			Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(curriculum.getAcademicArea().getUniqueId());
			if (major2clasf2ll != null) {
				if (curriculum.getMajors().isEmpty()) {
					for (String majorCode: major2clasf2ll.keySet()) {
						Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(majorCode);
						Integer e = (clasf2ll == null ? null : clasf2ll.get(clasf.getAcademicClassification().getCode()));
						if (e != null) {
							lastLike += e;
							clasf2ll.remove(clasf.getAcademicClassification().getCode());
						}
					}
				} else {
					for (Iterator<PosMajor> i = curriculum.getMajors().iterator(); i.hasNext(); ) {
						PosMajor m = i.next();
						Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(m.getCode());
						Integer e = (clasf2ll == null ? null : clasf2ll.get(clasf.getAcademicClassification().getCode()));
						if (e != null) {
							lastLike += e;
							clasf2ll.remove(clasf.getAcademicClassification().getCode());
						}
					}
				}
			}
			
			if (lastLike > 0)
				curCourseIfc.setLastLike(lastLike);
			
			courseIfc.setCurriculumCourse(classifications.get(clasf.getAcademicClassification().getUniqueId()), curCourseIfc);
		}
		
		for (CurriculumInterface curriculumIfc: results) {
			for (AcademicClassificationInterface clasf: academicClassifications) {
				int enrl = 0;
				Hashtable<Long, Hashtable<Long, Integer>> major2clasf2enrl = area2major2clasf2enrl.get(curriculumIfc.getAcademicArea().getId());
				if (major2clasf2enrl != null) {
					if (!curriculumIfc.hasMajors()) {
						for (Long majorId: major2clasf2enrl.keySet()) {
							Hashtable<Long, Integer> clasf2enrl = major2clasf2enrl.get(majorId);
							Integer e = (clasf2enrl == null ? null : clasf2enrl.get(clasf.getId()));
							if (e != null) {
								enrl += e;
								clasf2enrl.remove(clasf.getId());
							}
						}
					} else {
						for (MajorInterface m: curriculumIfc.getMajors()) {
							Hashtable<Long, Integer> clasf2enrl = major2clasf2enrl.get(m.getId());
							Integer e = (clasf2enrl == null ? null : clasf2enrl.get(clasf.getId()));
							if (e != null) {
								enrl += e;
								clasf2enrl.remove(clasf.getId());
							}
						}
					}
				}
				
				int lastLike = 0;
				Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(curriculumIfc.getAcademicArea().getId());
				if (major2clasf2ll != null) {
					if (!curriculumIfc.hasMajors()) {
						for (String majorCode: major2clasf2ll.keySet()) {
							Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(majorCode);
							Integer e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
							if (e != null) {
								lastLike += e;
								clasf2ll.remove(clasf.getCode());
							}
						}
					} else {
						for (MajorInterface m: curriculumIfc.getMajors()) {
							Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(m.getCode());
							Integer e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
							if (e != null) {
								lastLike += e;
								clasf2ll.remove(clasf.getId());
							}
						}
					}
				}
				
				
				if (enrl > 0 || lastLike > 0) {
					CourseInterface courseIfc = null;
					if (curriculumIfc.hasCourses()) {
						courseIfc = curriculumIfc.getCourses().first();
					} else {
						courseIfc = new CourseInterface();
						courseIfc.setId(courseOffering.getUniqueId());
						courseIfc.setCourseName(courseOffering.getCourseName());
						curriculumIfc.addCourse(courseIfc);
					}
					
					CurriculumCourseInterface curCourseIfc = new CurriculumCourseInterface();
					curCourseIfc.setCourseOfferingId(courseOffering.getUniqueId());
					curCourseIfc.setShare(0.0f);
					curCourseIfc.setCourseName(courseOffering.getCourseName());

					if (enrl > 0)
						curCourseIfc.setEnrollment(enrl);

					if (lastLike > 0)
						curCourseIfc.setLastLike(lastLike);
					
					courseIfc.setCurriculumCourse(classifications.get(clasf.getId()), curCourseIfc);
				}
			}
		}
		
		HashSet<Long> areas = new HashSet<Long>();
		areas.addAll(area2major2clasf2enrl.keySet());
		areas.addAll(area2major2clasf2ll.keySet());
		for (Long areaId: areas) {
			boolean empty = true;
			CurriculumInterface otherCurriculumIfc = new CurriculumInterface();
			CourseInterface otherCourseIfc = new CourseInterface();
			otherCourseIfc.setId(courseOffering.getUniqueId());
			otherCourseIfc.setCourseName(courseOffering.getCourseName());
			otherCurriculumIfc.addCourse(otherCourseIfc);
			for (AcademicClassificationInterface clasf: academicClassifications) {
				int enrl = 0;
				Hashtable<Long, Hashtable<Long, Integer>> major2clasf2enrl = area2major2clasf2enrl.get(areaId);
				if (major2clasf2enrl != null) {
					for (Long majorId: major2clasf2enrl.keySet()) {
						Hashtable<Long, Integer> clasf2enrl = major2clasf2enrl.get(majorId);
						Integer e = (clasf2enrl == null ? null : clasf2enrl.get(clasf.getId()));
						if (e != null) {
							enrl += e;
						}
					}
				}
				int lastLike = 0;
				Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(areaId);
				if (major2clasf2ll != null) {
					for (String majorCode: major2clasf2ll.keySet()) {
						Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(majorCode);
						Integer e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
						if (e != null) {
							lastLike += e;
						}
					}
				}
				if (enrl > 0 || lastLike > 0) {
					CurriculumCourseInterface otherCurCourseIfc = new CurriculumCourseInterface();
					otherCurCourseIfc.setCourseOfferingId(courseOffering.getUniqueId());
					otherCurCourseIfc.setCourseName(courseOffering.getCourseName());
					if (enrl > 0)
						otherCurCourseIfc.setEnrollment(enrl);
					if (lastLike > 0)
						otherCurCourseIfc.setLastLike(lastLike);
					otherCourseIfc.setCurriculumCourse(classifications.get(clasf.getId()), otherCurCourseIfc);
					empty = false;
				}
			}
			if (empty) continue;
			AcademicArea a = AcademicAreaDAO.getInstance().get(areaId, hibSession);
			AcademicAreaInterface areaIfc = new AcademicAreaInterface();
			areaIfc.setId(a.getUniqueId());
			areaIfc.setAbbv(a.getAcademicAreaAbbreviation());
			areaIfc.setName(Constants.toInitialCase(a.getLongTitle() == null ? a.getShortTitle() : a.getLongTitle()));
			otherCurriculumIfc.setAcademicArea(areaIfc);
			otherCurriculumIfc.setAbbv(areaIfc.getAbbv());
			otherCurriculumIfc.setName(areaIfc.getName());
			results.add(otherCurriculumIfc);
		}
		
		return results;
	}
	
	public TreeSet<CurriculumInterface> findCurriculaForACourse(String courseName) throws CurriculaException {
		try {
			sLog.info("getCurriculaForACourse(courseName='" + courseName + "')");
			Long s0 = System.currentTimeMillis();
			
			TreeSet<AcademicClassificationInterface> academicClassifications = loadAcademicClassifications();
			TreeSet<AcademicAreaInterface> academicAreas = loadAcademicAreas();

			TreeSet<CurriculumInterface> results = null;
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Long sessionId = getAcademicSessionId();
				
				CourseOffering courseOffering = null;
				for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
						"select c from CourseOffering c where " +
						"c.subjectArea.session.uniqueId = :sessionId and " +
						"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course")
						.setString("course", courseName.toLowerCase())
						.setLong("sessionId", sessionId)
						.setCacheable(true).setMaxResults(1).list()) {
					courseOffering = c; break;
				}
				if (courseOffering == null) throw new CurriculaException("course " + courseName + " does not exist");
				
				results = loadCurriculaForACourse(hibSession, academicClassifications, academicAreas, courseOffering);
			} finally {
				hibSession.close();
			}
			sLog.info("Found " + (results == null ? 0 : results.size()) + " curricula (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public TreeSet<CurriculumInterface> findCurriculaForAnInstructionalOffering(Long offeringId) throws CurriculaException {
		try {
			sLog.info("findCurriculaForAnOffering(offeringId='" + offeringId + "')");
			Long s0 = System.currentTimeMillis();
			
			TreeSet<AcademicClassificationInterface> academicClassifications = loadAcademicClassifications();
			TreeSet<AcademicAreaInterface> academicAreas = loadAcademicAreas();

			TreeSet<CurriculumInterface> results = null;
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Long sessionId = getAcademicSessionId();
				
				InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(offeringId, hibSession);
				if (offering == null) throw new CurriculaException("offering " + offeringId + " does not exist");
				
				for (Iterator<CourseOffering> i = offering.getCourseOfferings().iterator(); i.hasNext(); ) {
					CourseOffering courseOffering = i.next();
					if (results == null) {
						results = loadCurriculaForACourse(hibSession, academicClassifications, academicAreas, courseOffering);
					} else {
						TreeSet<CurriculumInterface> curricula = loadCurriculaForACourse(hibSession, academicClassifications, academicAreas, courseOffering);
						curricula: for (CurriculumInterface curriculum: curricula) {
							for (CurriculumInterface result: results) {
								if (ToolBox.equals(curriculum.getId(), result.getId()) && ToolBox.equals(curriculum.getAcademicArea().getId(), result.getAcademicArea().getId())) {
									result.addCourse(curriculum.getCourses().first());
									continue curricula;
								}
							}
							results.add(curriculum);
						}
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
				User user = Web.getUser(getThreadLocalRequest().getSession());
				if (Roles.ADMIN_ROLE.equals(user.getRole())) {
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
				} else {
					for (Iterator<Department> i = getManager().getDepartments().iterator(); i.hasNext();) {
						Department d = i.next();
						if (d.getSession().getUniqueId().equals(sessionId)) {
							DepartmentInterface di = new DepartmentInterface();
							di.setId(d.getUniqueId());
							di.setCode(d.getDeptCode());
							di.setAbbv(d.getAbbreviation());
							di.setName(d.getName());
							results.add(di);
						}
					}
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
		if (user.getRole() == null) throw new CurriculaException("no user role");
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
					boolean unlimited = false;
					int courseLimit = 0;
					for (Iterator<InstrOfferingConfig> i = c.getInstructionalOffering().getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
						InstrOfferingConfig cfg = i.next();
						if (cfg.isUnlimitedEnrollment()) unlimited = true;
						if (cfg.getLimit() != null) courseLimit += cfg.getLimit();
					}
		            for (Iterator<CourseOfferingReservation> k = c.getCourseReservations().iterator(); k.hasNext(); ) {
		            	CourseOfferingReservation reservation = k.next();
		                if (reservation.getCourseOffering().equals(c) && reservation.getReserved()!=null)
		                	courseLimit = reservation.getReserved();
		            }
		            if (courseLimit >= 9999) unlimited = true;
					course.setLimit(unlimited ? -1 : courseLimit);
					course.setProjected(c.getProjectedDemand());
					course.setEnrollment(c.getEnrollment());
					course.setLastLike(c.getDemand());
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
	
	public Boolean canAddCurriculum() throws CurriculaException {
		try {
			User user = Web.getUser(getThreadLocalRequest().getSession());
			if (user == null) throw new CurriculaException("not authenticated");
			return Roles.CURRICULUM_MGR_ROLE.equals(user.getRole()) ||
				Roles.DEPT_SCHED_MGR_ROLE.equals(user.getRole()) ||
				Roles.ADMIN_ROLE.equals(user.getRole());
		} catch  (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}

}
