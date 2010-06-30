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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumStudentsInterface;
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
import org.unitime.timetable.model.CurriculumProjectionRule;
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
import org.unitime.timetable.test.MakeCurriculaFromLastlikeDemands;
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
	
	private Hashtable<String,HashMap<String, Float>> getRules(org.hibernate.Session hibSession, Long acadAreaId) {
		Hashtable<String,HashMap<String, Float>> clasf2major2proj = new Hashtable<String, HashMap<String,Float>>();
		for (CurriculumProjectionRule rule: (List<CurriculumProjectionRule>)hibSession.createQuery(
				"select r from CurriculumProjectionRule r where r.academicArea.uniqueId=:acadAreaId")
				.setLong("acadAreaId", acadAreaId).setCacheable(true).list()) {
			String majorCode = (rule.getMajor() == null ? "" : rule.getMajor().getCode());
			String clasfCode = rule.getAcademicClassification().getCode();
			Float projection = rule.getProjection();
			HashMap<String, Float> major2proj = clasf2major2proj.get(clasfCode);
			if (major2proj == null) {
				major2proj = new HashMap<String, Float>();
				clasf2major2proj.put(clasfCode, major2proj);
			}
			major2proj.put(majorCode, projection);
		}
		return clasf2major2proj;
	}
	
	private Hashtable<String, Hashtable<String, HashMap<String, Float>>> getRules(org.hibernate.Session hibSession) {
		Hashtable<String, Hashtable<String, HashMap<String, Float>>> area2clasf2major2proj = new Hashtable<String, Hashtable<String,HashMap<String,Float>>>();
		for (CurriculumProjectionRule rule: (List<CurriculumProjectionRule>)hibSession.createQuery(
				"select r from CurriculumProjectionRule r where r.academicArea.session.uniqueId = :sessionId")
				.setLong("sessionId", getAcademicSessionId()).setCacheable(true).list()) {
			String areaAbbv = rule.getAcademicArea().getAcademicAreaAbbreviation();
			String majorCode = (rule.getMajor() == null ? "" : rule.getMajor().getCode());
			String clasfCode = rule.getAcademicClassification().getCode();
			Float projection = rule.getProjection();
			Hashtable<String, HashMap<String, Float>> clasf2major2proj = area2clasf2major2proj.get(areaAbbv);
			if (clasf2major2proj == null) {
				clasf2major2proj = new Hashtable<String, HashMap<String,Float>>();
				area2clasf2major2proj.put(areaAbbv, clasf2major2proj);
			}
			HashMap<String, Float> major2proj = clasf2major2proj.get(clasfCode);
			if (major2proj == null) {
				major2proj = new HashMap<String, Float>();
				clasf2major2proj.put(clasfCode, major2proj);
			}
			major2proj.put(majorCode, projection);
		}
		return area2clasf2major2proj;
	}
	
	public float getProjection(Hashtable<String,HashMap<String, Float>> clasf2major2proj, String majorCode, String clasfCode) {
		if (clasf2major2proj == null || clasf2major2proj.isEmpty()) return 1.0f;
		HashMap<String, Float> major2proj = clasf2major2proj.get(clasfCode);
		if (major2proj == null) return 1.0f;
		Float projection = major2proj.get(majorCode);
		if (projection == null)
			projection = major2proj.get("");
		return (projection == null ? 1.0f : projection);
	}
	
	public List<CurriculumClassificationInterface> loadClassifications(List<Long> curriculumIds) throws CurriculaException {
		try {
			sLog.info("loadClassifications(curriculumIds=" + curriculumIds + ")");
			Long s0 = System.currentTimeMillis();
			if (curriculumIds == null || curriculumIds.isEmpty()) return new ArrayList<CurriculumClassificationInterface>();
			
			List<CurriculumClassificationInterface> results = new ArrayList<CurriculumClassificationInterface>();
			
			TreeSet<AcademicClassificationInterface> academicClassifications = loadAcademicClassifications();

			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Long sessionId = getAcademicSessionId();
				for (Long curriculumId: curriculumIds) {
					Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
					if (c == null) throw new CurriculaException("curriculum " + curriculumId + " does not exist anymore, please refresh your data");
					
					Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, c.getAcademicArea().getUniqueId());
					
					String majorIds = "", majorCodes = "";
					for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
						PosMajor major = i.next();
						if (!majorIds.isEmpty()) { majorIds += ","; majorCodes += ","; }
						majorIds += major.getUniqueId();
						majorCodes += "'" + major.getCode() + "'";
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
					
					Hashtable<String, Hashtable<String, Integer>> clasfMajor2ll = new Hashtable<String, Hashtable<String,Integer>>();
					for (Object[] o : (List<Object[]>)hibSession.createQuery(
							"select f.code, m.code, count(distinct s) from LastLikeCourseDemand x inner join x.student s " +
							"inner join s.academicAreaClassifications a inner join a.academicClassification f inner join s.posMajors m where " +
							"x.subjectArea.session.uniqueId = :sessionId and "+
							"a.academicArea.academicAreaAbbreviation = :acadAbbv " + 
							(majorCodes.isEmpty() ? "" : "and m.code in (" + majorCodes + ") ") +
							"group by f.code, m.code")
							.setLong("sessionId", sessionId)
							.setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation())
							.setCacheable(true).list()) {
						String clasfCode = (String)o[0];
						String majorCode = (String)o[1];
						int enrl = ((Number)o[2]).intValue();
						Hashtable<String, Integer> major2ll = clasfMajor2ll.get(clasfCode);
						if (major2ll == null) {
							major2ll = new Hashtable<String, Integer>();
							clasfMajor2ll.put(clasfCode, major2ll);
						}
						major2ll.put(majorCode, enrl);
					}
					
					TreeSet<CurriculumClassification> classifications = new TreeSet<CurriculumClassification>(c.getClassifications());
					for (CurriculumClassification clasf: classifications) {
						CurriculumClassificationInterface cfi = new CurriculumClassificationInterface();
						cfi.setId(clasf.getUniqueId());
						cfi.setName(clasf.getName());
						cfi.setCurriculumId(c.getUniqueId());
						int lastLike = 0;
						float proj = 0;
						Hashtable<String, Integer> major2ll = clasfMajor2ll.get(clasf.getAcademicClassification().getCode());
						if (major2ll != null) {
							for (Map.Entry<String,Integer> m2l: major2ll.entrySet()) {
								lastLike += m2l.getValue();
								proj += getProjection(rules, m2l.getKey(), clasf.getAcademicClassification().getCode()) * m2l.getValue();
							}
						}
						cfi.setLastLike(lastLike == 0 ? null : lastLike);
						cfi.setProjection(Math.round(proj) == 0 ? null : Math.round(proj));
						cfi.setExpected(clasf.getNrStudents());
						cfi.setEnrollment(clasf2enrl.get(clasf.getAcademicClassification().getUniqueId()));
						AcademicClassificationInterface aci = new AcademicClassificationInterface();
						aci.setId(clasf.getAcademicClassification().getUniqueId());
						aci.setName(clasf.getAcademicClassification().getName());
						aci.setCode(clasf.getAcademicClassification().getCode());
						cfi.setAcademicClassification(aci);
						results.add(cfi);
						academicClassifications.remove(aci);
					}

					if (!academicClassifications.isEmpty()) {
						for (AcademicClassificationInterface clasf: academicClassifications) {
							CurriculumClassificationInterface cfi = new CurriculumClassificationInterface();
							cfi.setName(clasf.getCode());
							cfi.setCurriculumId(c.getUniqueId());
							int lastLike = 0;
							float proj = 0;
							Hashtable<String, Integer> major2ll = clasfMajor2ll.get(clasf.getCode());
							if (major2ll != null) {
								for (Map.Entry<String,Integer> m2l: major2ll.entrySet()) {
									lastLike += m2l.getValue();
									proj += getProjection(rules, m2l.getKey(), clasf.getCode()) * m2l.getValue();
								}
							}
							cfi.setLastLike(lastLike == 0 ? null : lastLike);
							cfi.setProjection(Math.round(proj) == 0 ? null : Math.round(proj));
							cfi.setEnrollment(clasf2enrl.get(clasf.getId()));
							AcademicClassificationInterface aci = new AcademicClassificationInterface();
							aci.setId(clasf.getId());
							aci.setName(clasf.getName());
							aci.setCode(clasf.getCode());
							cfi.setAcademicClassification(aci);
							results.add(cfi);
						}
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

			TreeSet<AcademicClassificationInterface> academicClassifications = loadAcademicClassifications();
			Hashtable<Long, Integer> classifications = new Hashtable<Long, Integer>();
			int idx = 0;
			for (AcademicClassificationInterface clasf: academicClassifications) {
				classifications.put(clasf.getId(), idx++);
			}
			
			User user = Web.getUser(getThreadLocalRequest().getSession());

			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Long sessionId = getAcademicSessionId();
				Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
				if (c == null) throw new CurriculaException("curriculum " + curriculumId + " does not exist");
				Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, c.getAcademicArea().getUniqueId());
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
				String majorIds = "", majorCodes = "";
				for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
					PosMajor major = i.next();
					MajorInterface majorIfc = new MajorInterface();
					majorIfc.setId(major.getUniqueId());
					majorIfc.setCode(major.getCode());
					majorIfc.setName(Constants.toInitialCase(major.getName()));
					curriculumIfc.addMajor(majorIfc);
					if (!majorIds.isEmpty()) { majorIds += ","; majorCodes += ","; }
					majorIds += major.getUniqueId();
					majorCodes += "'" + major.getCode() + "'";
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
				
				Hashtable<String, Hashtable<String, Integer>> clasfMajor2ll = new Hashtable<String, Hashtable<String,Integer>>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select f.code, m.code, count(distinct s) from LastLikeCourseDemand x inner join x.student s " +
						"inner join s.academicAreaClassifications a inner join a.academicClassification f inner join s.posMajors m where " +
						"x.subjectArea.session.uniqueId = :sessionId and "+
						"a.academicArea.academicAreaAbbreviation = :acadAbbv " + 
						(majorCodes.isEmpty() ? "" : "and m.code in (" + majorCodes + ") ") +
						"group by f.code, m.code")
						.setLong("sessionId", sessionId)
						.setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation())
						.setCacheable(true).list()) {
					String clasfCode = (String)o[0];
					String majorCode = (String)o[1];
					int enrl = ((Number)o[2]).intValue();
					Hashtable<String, Integer> major2ll = clasfMajor2ll.get(clasfCode);
					if (major2ll == null) {
						major2ll = new Hashtable<String, Integer>();
						clasfMajor2ll.put(clasfCode, major2ll);
					}
					major2ll.put(majorCode, enrl);
				}
				
				Hashtable<String, Hashtable<String, Hashtable<Long, Integer>>> clasfMajor2course2ll = new Hashtable<String, Hashtable<String, Hashtable<Long,Integer>>>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select f.code, m.code, co.uniqueId, count(distinct s) " +
						"from LastLikeCourseDemand x inner join x.student s inner join s.academicAreaClassifications a inner join a.academicClassification f inner join s.posMajors m, CourseOffering co where " +
						"x.subjectArea.session.uniqueId = :sessionId and "+
						"a.academicArea.academicAreaAbbreviation = :acadAbbv " + 
						(majorCodes.isEmpty() ? "" : "and m.code in (" + majorCodes + ") ") +
						"and co.subjectArea.uniqueId = x.subjectArea.uniqueId and " +
						"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr)) " +
						"group by f.code, m.code, co.uniqueId")
						.setLong("sessionId", sessionId)
						.setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation())
						.setCacheable(true).list()) {
					String clasfCode = (String)o[0];
					String majorCode = (String)o[1];
					Long courseId = (Long)o[2];
					int enrl = ((Number)o[3]).intValue();
					Hashtable<String, Hashtable<Long, Integer>> major2course2ll = clasfMajor2course2ll.get(clasfCode);
					if (major2course2ll == null) {
						major2course2ll = new Hashtable<String, Hashtable<Long,Integer>>();
						clasfMajor2course2ll.put(clasfCode, major2course2ll);
					}
					Hashtable<Long, Integer> course2enrl = major2course2ll.get(clasfCode);
					if (course2enrl == null) {
						course2enrl = new Hashtable<Long, Integer>();
						major2course2ll.put(clasfCode, course2enrl);
					}
					course2enrl.put(courseId, enrl);
				}
				
				Hashtable<Long, CourseInterface> courseId2Interface = new Hashtable<Long, CourseInterface>();
				Hashtable<String, CurriculumCourseGroupInterface> groups = new Hashtable<String, CurriculumCourseGroupInterface>();
				for (Iterator<CurriculumClassification> i = c.getClassifications().iterator(); i.hasNext(); ) {
					CurriculumClassification clasf = i.next();
					CurriculumClassificationInterface clasfIfc = new CurriculumClassificationInterface();
					clasfIfc.setId(clasf.getUniqueId());
					clasfIfc.setName(clasf.getName());
					clasfIfc.setCurriculumId(c.getUniqueId());
					int lastLike = 0;
					float proj = 0;
					Hashtable<String, Integer> major2ll = clasfMajor2ll.get(clasf.getAcademicClassification().getCode());
					if (major2ll != null) {
						for (Map.Entry<String,Integer> m2l: major2ll.entrySet()) {
							lastLike += m2l.getValue();
							proj += getProjection(rules, m2l.getKey(), clasf.getAcademicClassification().getCode()) * m2l.getValue();
						}
					}
					clasfIfc.setLastLike(lastLike == 0 ? null : lastLike);
					clasfIfc.setProjection(Math.round(proj) == 0 ? null : Math.round(proj));
					clasfIfc.setEnrollment(clasf2enrl.get(clasf.getAcademicClassification().getUniqueId()));
					clasfIfc.setExpected(clasf.getNrStudents());
					AcademicClassificationInterface acadClasfIfc = new AcademicClassificationInterface();
					acadClasfIfc.setId(clasf.getAcademicClassification().getUniqueId());
					acadClasfIfc.setName(clasf.getAcademicClassification().getName());
					acadClasfIfc.setCode(clasf.getAcademicClassification().getCode());
					clasfIfc.setAcademicClassification(acadClasfIfc);
					curriculumIfc.addClassification(clasfIfc);
					Hashtable<Long, Integer> course2enrl = clasf2course2enrl.get(clasf.getAcademicClassification().getUniqueId());
					Hashtable<String, Hashtable<Long, Integer>> major2course2ll = clasfMajor2course2ll.get(clasf.getAcademicClassification().getCode());
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
						int courseLastLike = 0;
						float courseProj = 0;
						if (major2course2ll != null) {
							for (Map.Entry<String,Hashtable<Long,Integer>> m2l: major2course2ll.entrySet()) {
								Integer ll = m2l.getValue().get(course.getCourse().getUniqueId());
								if (ll != null) {
									courseLastLike += ll;
									courseProj += getProjection(rules, m2l.getKey(), clasf.getAcademicClassification().getCode());
								}
							}
						}
						curCourseIfc.setLastLike(courseLastLike == 0 ? null : courseLastLike);
						curCourseIfc.setProjection(Math.round(courseProj) == 0 ? null : Math.round(courseProj));
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
					academicClassifications.remove(acadClasfIfc);
				}
				
				if (!academicClassifications.isEmpty()) {
					for (AcademicClassificationInterface clasf: academicClassifications) {
						CurriculumClassificationInterface clasfIfc = new CurriculumClassificationInterface();
						clasfIfc.setName(clasf.getCode());
						clasfIfc.setCurriculumId(c.getUniqueId());
						int lastLike = 0;
						float proj = 0;
						Hashtable<String, Integer> major2ll = clasfMajor2ll.get(clasf.getCode());
						if (major2ll != null) {
							for (Map.Entry<String,Integer> m2l: major2ll.entrySet()) {
								lastLike += m2l.getValue();
								proj += getProjection(rules, m2l.getKey(), clasf.getCode()) * m2l.getValue();
							}
						}
						clasfIfc.setLastLike(lastLike == 0 ? null : lastLike);
						clasfIfc.setProjection(Math.round(proj) == 0 ? null : Math.round(proj));
						clasfIfc.setEnrollment(clasf2enrl.get(clasf.getId()));
						AcademicClassificationInterface acadClasfIfc = new AcademicClassificationInterface();
						acadClasfIfc.setId(clasf.getId());
						acadClasfIfc.setName(clasf.getName());
						acadClasfIfc.setCode(clasf.getCode());
						clasfIfc.setAcademicClassification(acadClasfIfc);
						curriculumIfc.addClassification(clasfIfc);
					}
				}

				
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
	
	public Boolean deleteCurricula(Set<Long> curriculumIds) throws CurriculaException {
		try {
			sLog.info("deleteCurricula(curriculumIds=" + curriculumIds + ")");
			Long s0 = System.currentTimeMillis();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			User user = Web.getUser(getThreadLocalRequest().getSession());
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				for (Long curriculumId: curriculumIds) {
					if (curriculumId == null) 
						throw new CurriculaException("Unsaved curriculum cannot be deleted.");
					
					Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
					if (c == null) throw new CurriculaException("Curriculum " + curriculumId + " no longer exists.");
					
					if (!c.canUserEdit(user)) throw new CurriculaException("You are not authorized to delete curriculum " + c.getAbbv() + ".");
					
					hibSession.delete(c);
				}
				
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
			sLog.info("Deleted " + curriculumIds.size() + " curricula (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}

	public Boolean mergeCurricula(Set<Long> curriculumIds) throws CurriculaException {
		try {
			sLog.info("mergeCurricula(curriculumIds=" + curriculumIds + ")");
			Long s0 = System.currentTimeMillis();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			User user = Web.getUser(getThreadLocalRequest().getSession());
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				Curriculum mergedCurriculum = new Curriculum();
				mergedCurriculum.setMajors(new HashSet());
				mergedCurriculum.setClassifications(new HashSet());
				
				int clasfOrd = 0, courseOrd = 0, cidx = 0;
				Hashtable<Long, CurriculumCourseGroup> groups = new Hashtable<Long, CurriculumCourseGroup>();
				
				for (Long curriculumId: curriculumIds) {
					if (curriculumId == null) 
						throw new CurriculaException("Unsaved curriculum cannot be merged.");
					
					Curriculum curriculum = CurriculumDAO.getInstance().get(curriculumId, hibSession);
					if (curriculum == null) throw new CurriculaException("Curriculum " + curriculumId + " no longer exists.");
					
					if (!curriculum.canUserEdit(user)) throw new CurriculaException("You are not authorized to merge curriculum " + curriculum.getAbbv() + ".");
					
					cidx++;
						
					if (mergedCurriculum.getAcademicArea() == null) {
						mergedCurriculum.setAcademicArea(curriculum.getAcademicArea());
					} else if (!mergedCurriculum.getAcademicArea().equals(curriculum.getAcademicArea()))
						throw new CurriculaException("Selected curricula have different academic areas.");

					if (mergedCurriculum.getDepartment() == null) {
						mergedCurriculum.setDepartment(curriculum.getDepartment());
					} else if (!mergedCurriculum.getDepartment().equals(curriculum.getDepartment()))
						throw new CurriculaException("Selected curricula have different departments.");
						
					mergedCurriculum.getMajors().addAll(curriculum.getMajors());
					
					for (Iterator<CurriculumClassification> i = curriculum.getClassifications().iterator(); i.hasNext(); ) {
						CurriculumClassification clasf = i.next();
						CurriculumClassification mergedClasf = null;
						for (Iterator<CurriculumClassification> j = mergedCurriculum.getClassifications().iterator(); j.hasNext(); ) {
							CurriculumClassification x = j.next();
							if (x.getAcademicClassification().equals(clasf.getAcademicClassification())) {
								mergedClasf = x; break;
							}
						}
						
						if (mergedClasf == null) {
							mergedClasf = new CurriculumClassification();
							mergedClasf.setCurriculum(mergedCurriculum);
							mergedClasf.setAcademicClassification(clasf.getAcademicClassification());
							mergedClasf.setCourses(new HashSet());
							mergedClasf.setName(clasf.getName());
							mergedClasf.setOrd(clasfOrd++);
							mergedClasf.setNrStudents(0);
							mergedCurriculum.getClassifications().add(mergedClasf);
						} else {
							if (!mergedClasf.getName().equals(clasf.getName()))
								mergedClasf.setName(clasf.getAcademicClassification().getCode());
						}
						
						List<CurriculumCourse> remainingMergedCourses = new ArrayList<CurriculumCourse>(mergedClasf.getCourses());
						
						for (Iterator<CurriculumCourse> j = clasf.getCourses().iterator(); j.hasNext(); ) {
							CurriculumCourse course = j.next();
							CurriculumCourse mergedCourse = null;
							for (Iterator<CurriculumCourse> k = remainingMergedCourses.iterator(); k.hasNext(); ) {
								CurriculumCourse x = k.next();
								if (x.getCourse().equals(course.getCourse())) {
									mergedCourse = x;
									k.remove();
									break;
								}
							}
							
							if (mergedCourse == null) {
								mergedCourse = new CurriculumCourse();
								mergedCourse.setClassification(mergedClasf);
								mergedCourse.setCourse(course.getCourse());
								mergedCourse.setPercShare(0f);
								mergedCourse.setOrd(courseOrd++);
								mergedCourse.setGroups(new HashSet());
								mergedClasf.getCourses().add(mergedCourse);
							}
							
							mergedCourse.setPercShare(
									(mergedCourse.getPercShare() * mergedClasf.getNrStudents() +
									(course.getPercShare() == null || clasf.getNrStudents() == null ? 0f : course.getPercShare() * clasf.getNrStudents())) / 
									(mergedClasf.getNrStudents() + (clasf.getNrStudents() == null ? 0 : clasf.getNrStudents()))
									);

							for (Iterator<CurriculumCourseGroup> k = course.getGroups().iterator(); k.hasNext(); ) {
								CurriculumCourseGroup group = k.next();
								CurriculumCourseGroup mergedGroup = groups.get(group.getUniqueId());
								if (mergedGroup == null) {
									mergedGroup = new CurriculumCourseGroup();
									mergedGroup.setColor(null);
									mergedGroup.setType(group.getType());
									mergedGroup.setName(group.getName() + " " + cidx);
									mergedGroup.setCurriculum(mergedCurriculum);
									groups.put(group.getUniqueId(), mergedGroup);
								}
								
								mergedCourse.getGroups().add(mergedGroup);
							}
						}
						
						for (CurriculumCourse mergedCourse: remainingMergedCourses) {
							if (clasf.getNrStudents() != null && clasf.getNrStudents() > 0)
								mergedCourse.setPercShare(
										(mergedCourse.getPercShare() * mergedClasf.getNrStudents()) / 
										(mergedClasf.getNrStudents() + clasf.getNrStudents())
										);
						}
						
						mergedClasf.setNrStudents(mergedClasf.getNrStudents() + (clasf.getNrStudents() == null ? 0 : clasf.getNrStudents()));
						
					}
					
					hibSession.delete(curriculum);
				}
				
				if (mergedCurriculum.getAcademicArea() != null) {
					String abbv = mergedCurriculum.getAcademicArea().getAcademicAreaAbbreviation();
					String name = Constants.toInitialCase(mergedCurriculum.getAcademicArea().getLongTitle() == null ? mergedCurriculum.getAcademicArea().getShortTitle() : mergedCurriculum.getAcademicArea().getLongTitle());
					
					TreeSet<PosMajor> majors = new TreeSet<PosMajor>(new Comparator<PosMajor>() {
						public int compare(PosMajor m1, PosMajor m2) {
							return m1.getCode().compareToIgnoreCase(m2.getCode());
						}
					});
					majors.addAll(mergedCurriculum.getMajors());
					
					for (PosMajor m: majors) {
						if (abbv.indexOf('/') < 0) {
							abbv += "/"; name += " / ";
						} else {
							abbv += ","; name += ", ";
						}
						abbv += m.getCode();
						name += Constants.toInitialCase(m.getName());
					}

					if (abbv.length() > 20) abbv = abbv.substring(0, 20);
					mergedCurriculum.setAbbv(abbv);
					
					if (name.length() > 60) name = name.substring(0, 60);
					mergedCurriculum.setName(name);
					
					hibSession.saveOrUpdate(mergedCurriculum);
					
					for (CurriculumCourseGroup g: groups.values())
						hibSession.saveOrUpdate(g);
				}
				
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
			sLog.info("Merged " + curriculumIds.size() + " curricula (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
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
	
	public HashMap<String, CurriculumStudentsInterface[]> computeEnrollmentsAndLastLikes(Long acadAreaId, List<Long> majors) throws CurriculaException {
		try {
			sLog.info("computeEnrollmentsAndLastLikes(acadAreaId=" + acadAreaId + ", majors=" + majors + ")");
			Long s0 = System.currentTimeMillis();
			if (acadAreaId == null) return new HashMap<String, CurriculumStudentsInterface[]>();
			Hashtable<Long, Integer> classifications = new Hashtable<Long, Integer>();
			int idx = 0;
			for (AcademicClassificationInterface clasf: loadAcademicClassifications()) {
				classifications.put(clasf.getId(), idx++);
			}
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			HashMap<String, CurriculumStudentsInterface[]> results = new HashMap<String, CurriculumStudentsInterface[]>();
			try {
				Long sessionId = getAcademicSessionId();

				Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, acadAreaId);

				AcademicArea acadArea = AcademicAreaDAO.getInstance().get(acadAreaId, hibSession);
								
				String majorIds = "", majorCodes = "";
				for (Long majorId: majors) {
					if (!majorIds.isEmpty()) { majorIds += ","; majorCodes += ","; }
					majorIds += majorId;
					majorCodes += "'" + PosMajorDAO.getInstance().get(majorId,hibSession).getCode() + "'";
				}
				
				Hashtable<Long, Set<Long>> clasf2enrl = new Hashtable<Long, Set<Long>>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, e.student.uniqueId from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join e.student.posMajors m ") + "where " +
						"e.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") "))
						.setLong("sessionId", sessionId)
						.setLong("areaId", acadAreaId)
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					Long studentId = (Long)o[1];
					Set<Long> students = clasf2enrl.get(clasfId);
					if (students == null) {
						students = new HashSet<Long>();
						clasf2enrl.put(clasfId, students);
					}
					students.add(studentId);
				}

				Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> clasf2course2enrl = new Hashtable<Long, Hashtable<CourseInterface,Set<Long>>>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicClassification.uniqueId, e.courseOffering.uniqueId, e.courseOffering.subjectArea.subjectAreaAbbreviation || ' ' || e.courseOffering.courseNbr, e.student.uniqueId from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " + 
						(majorIds.isEmpty() ? "" : " inner join e.student.posMajors m ") + "where " +
						"e.student.session.uniqueId = :sessionId and "+
						"a.academicArea.uniqueId = :areaId " + 
						(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") "))
						.setLong("sessionId", sessionId)
						.setLong("areaId", acadAreaId)
						.setCacheable(true).list()) {
					Long clasfId = (Long)o[0];
					Long courseId = (Long)o[1];
					String courseName = (String)o[2];
					Long studentId = (Long)o[3];
					CourseInterface course = new CourseInterface();
					course.setId(courseId);
					course.setCourseName(courseName);
					Hashtable<CourseInterface, Set<Long>> course2enrl = clasf2course2enrl.get(clasfId);
					if (course2enrl == null) {
						course2enrl = new Hashtable<CourseInterface, Set<Long>>();
						clasf2course2enrl.put(clasfId, course2enrl);
					}
					Set<Long> students = course2enrl.get(course);
					if (students == null) {
						students = new HashSet<Long>();
						course2enrl.put(course, students);
					}
					students.add(studentId);
				}
				
				Hashtable<String, HashMap<String, Set<Long>>> clasf2ll = new Hashtable<String, HashMap<String, Set<Long>>>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select f.code, m.code, s.uniqueId from LastLikeCourseDemand x inner join x.student s " +
						"inner join s.academicAreaClassifications a inner join a.academicClassification f inner join s.posMajors m where " +
						"x.subjectArea.session.uniqueId = :sessionId and "+
						"a.academicArea.academicAreaAbbreviation = :acadAbbv " + 
						(majorCodes.isEmpty() ? "" : "and m.code in (" + majorCodes + ") "))
						.setLong("sessionId", sessionId)
						.setString("acadAbbv", acadArea.getAcademicAreaAbbreviation())
						.setCacheable(true).list()) {
					String clasfCode = (String)o[0];
					String majorCode = (String)o[1];
					Long studentId = (Long)o[2];
					HashMap<String, Set<Long>> major2students = clasf2ll.get(clasfCode);
					if (major2students == null) {
						major2students = new HashMap<String, Set<Long>>();
						clasf2ll.put(clasfCode, major2students);
					}
					Set<Long> students = major2students.get(majorCode);
					if (students == null) {
						students = new HashSet<Long>();
						major2students.put(majorCode, students);
					}
					students.add(studentId);
				}
				
				Hashtable<String, Hashtable<CourseInterface, HashMap<String, Set<Long>>>> clasf2course2ll = new Hashtable<String, Hashtable<CourseInterface,HashMap<String,Set<Long>>>>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select f.code, co.uniqueId, co.subjectArea.subjectAreaAbbreviation || ' ' || co.courseNbr, m.code, s.uniqueId " +
						"from LastLikeCourseDemand x inner join x.student s inner join s.academicAreaClassifications a inner join a.academicClassification f " + 
						"inner join s.posMajors m, CourseOffering co where " +
						"x.subjectArea.session.uniqueId = :sessionId and "+
						"a.academicArea.academicAreaAbbreviation = :acadAbbv " + 
						(majorCodes.isEmpty() ? "" : "and m.code in (" + majorCodes + ") ") +
						"and co.subjectArea.uniqueId = x.subjectArea.uniqueId and " +
						"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr))")
						.setLong("sessionId", sessionId)
						.setString("acadAbbv", acadArea.getAcademicAreaAbbreviation())
						.setCacheable(true).list()) {
					String clasfCode = (String)o[0];
					Long courseId = (Long)o[1];
					String courseName = (String)o[2];
					String majorCode = (String)o[3];
					Long studentId = (Long)o[4];
					CourseInterface course = new CourseInterface();
					course.setId(courseId);
					course.setCourseName(courseName);
					Hashtable<CourseInterface, HashMap<String,Set<Long>>> course2ll = clasf2course2ll.get(clasfCode);
					if (course2ll == null) {
						course2ll = new Hashtable<CourseInterface, HashMap<String,Set<Long>>>();
						clasf2course2ll.put(clasfCode, course2ll);
					}
					HashMap<String,Set<Long>> major2students = course2ll.get(course);
					if (major2students == null) {
						major2students = new HashMap<String, Set<Long>>();
						course2ll.put(course, major2students);
					}
					Set<Long> students = major2students.get(majorCode);
					if (students == null) {
						students = new HashSet<Long>();
						major2students.put(majorCode, students);
					}
					students.add(studentId);
				}
				
				for (AcademicClassification clasf: (List<AcademicClassification>)hibSession.createQuery(
						"select c from AcademicClassification c where c.session.uniqueId = :sessionId " + 
						"order by c.code, c.name")
						.setLong("sessionId", sessionId).setCacheable(true).list()) {
					
					CurriculumStudentsInterface[] x = results.get("");
					if (x == null) {
						x = new CurriculumStudentsInterface[classifications.size()];
						results.put("", x);
					}
					
					int col = classifications.get(clasf.getUniqueId());
					x[col] = new CurriculumStudentsInterface();
					x[col].setProjection(rules.get(clasf.getCode()));
					x[col].setEnrolledStudents(clasf2enrl.get(clasf.getUniqueId()));
					x[col].setLastLikeStudents(clasf2ll.get(clasf.getCode()));
					
					Hashtable<CourseInterface, HashMap<String, Set<Long>>> lastLike = clasf2course2ll.get(clasf.getCode());
					Hashtable<CourseInterface, Set<Long>> enrollment = clasf2course2enrl.get(clasf.getUniqueId());
					
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
						CurriculumStudentsInterface[] c = results.get(co.getCourseName());
						if (c == null) {
							c = new CurriculumStudentsInterface[classifications.size()];
							results.put(co.getCourseName(), c);
						}
						c[col] = new CurriculumStudentsInterface();
						c[col].setProjection(rules == null ? null : rules.get(clasf.getCode()));
						c[col].setEnrolledStudents(enrollment == null ? null : enrollment.get(co));
						c[col].setLastLikeStudents(lastLike == null ? null : lastLike.get(co));
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
		Hashtable<Long, Hashtable<String, Integer>> cur2clasf2ll = new Hashtable<Long, Hashtable<String, Integer>>();
		for (CurriculumCourse course : (List<CurriculumCourse>)hibSession.createQuery(
				"select c from CurriculumCourse c where c.course.uniqueId = :courseId")
				.setLong("courseId", courseOffering.getUniqueId()).setCacheable(true).list()) {
			CurriculumClassification clasf = course.getClassification();
			Curriculum curriculum = clasf.getCurriculum();
			Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, curriculum.getAcademicArea().getUniqueId());
			
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
				
				String majorIds = "", majorCodes = "";
				for (Iterator<PosMajor> i = curriculum.getMajors().iterator(); i.hasNext(); ) {
					PosMajor major = i.next();
					if (!majorIds.isEmpty()) { majorIds += ","; majorCodes += ","; }
					majorIds += major.getUniqueId();
					majorCodes += "'" + major.getCode() + "'";
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
				
				Hashtable<String, Integer> clasf2ll = new Hashtable<String, Integer>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select f.code, count(distinct s) from LastLikeCourseDemand x inner join x.student s " +
						"inner join s.academicAreaClassifications a inner join a.academicClassification f " + 
						(majorCodes.isEmpty() ? "" : " inner join s.posMajors m ") + "where " +
						"x.subjectArea.session.uniqueId = :sessionId and "+
						"a.academicArea.academicAreaAbbreviation = :acadAbbv " + 
						(majorCodes.isEmpty() ? "" : "and m.code in (" + majorCodes + ") ") +
						"group by f.code")
						.setLong("sessionId", courseOffering.getSubjectArea().getSessionId())
						.setString("acadAbbv", curriculum.getAcademicArea().getAcademicAreaAbbreviation())
						.setCacheable(true).list()) {
					String clasfCode = (String)o[0];
					int enrl = ((Number)o[1]).intValue();
					clasf2ll.put(clasfCode, enrl);
				}
				cur2clasf2ll.put(curriculum.getUniqueId(), clasf2ll);

			}
			
			CurriculumClassificationInterface curClasfIfc = new CurriculumClassificationInterface();
			curClasfIfc.setId(clasf.getUniqueId());
			curClasfIfc.setName(clasf.getName());
			curClasfIfc.setCurriculumId(curriculum.getUniqueId());
			curClasfIfc.setLastLike(cur2clasf2ll.get(curriculum.getUniqueId()).get(clasf.getAcademicClassification().getCode()));
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
			float proj = 0.0f;
			Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(curriculum.getAcademicArea().getUniqueId());
			if (major2clasf2ll != null) {
				if (curriculum.getMajors().isEmpty()) {
					for (String majorCode: major2clasf2ll.keySet()) {
						Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(majorCode);
						Integer e = (clasf2ll == null ? null : clasf2ll.get(clasf.getAcademicClassification().getCode()));
						if (e != null) {
							lastLike += e;
							proj += getProjection(rules, majorCode, clasf.getAcademicClassification().getCode()) * e;
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
							proj += getProjection(rules, m.getCode(), clasf.getAcademicClassification().getCode()) * e;
							clasf2ll.remove(clasf.getAcademicClassification().getCode());
						}
					}
				}
			}
			
			if (lastLike > 0)
				curCourseIfc.setLastLike(lastLike);
			if (Math.round(proj) > 0)
				curCourseIfc.setProjection(Math.round(proj));
			
			courseIfc.setCurriculumCourse(classifications.get(clasf.getAcademicClassification().getUniqueId()), curCourseIfc);
		}
		
		for (CurriculumInterface curriculumIfc: results) {
			Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, curriculumIfc.getAcademicArea().getId());
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
				float proj = 0.0f;
				Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(curriculumIfc.getAcademicArea().getId());
				if (major2clasf2ll != null) {
					if (!curriculumIfc.hasMajors()) {
						for (String majorCode: major2clasf2ll.keySet()) {
							Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(majorCode);
							Integer e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
							if (e != null) {
								lastLike += e;
								proj += getProjection(rules, majorCode, clasf.getCode()) * e;
								clasf2ll.remove(clasf.getCode());
							}
						}
					} else {
						for (MajorInterface m: curriculumIfc.getMajors()) {
							Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(m.getCode());
							Integer e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
							if (e != null) {
								lastLike += e;
								proj += getProjection(rules, m.getCode(), clasf.getCode()) * e;
								clasf2ll.remove(clasf.getId());
							}
						}
					}
				}
				
				
				if (enrl > 0 || lastLike > 0 || Math.round(proj) > 0) {
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
					
					if (Math.round(proj) > 0)
						curCourseIfc.setProjection(Math.round(proj));
					
					courseIfc.setCurriculumCourse(classifications.get(clasf.getId()), curCourseIfc);
				}
			}
		}
		
		HashSet<Long> areas = new HashSet<Long>();
		areas.addAll(area2major2clasf2enrl.keySet());
		areas.addAll(area2major2clasf2ll.keySet());
		for (Long areaId: areas) {
			Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, areaId);
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
				float proj = 0.0f;
				Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(areaId);
				if (major2clasf2ll != null) {
					for (String majorCode: major2clasf2ll.keySet()) {
						Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(majorCode);
						Integer e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
						if (e != null) {
							lastLike += e;
							proj += getProjection(rules, majorCode, clasf.getCode()) * e;
						}
					}
				}
				if (enrl > 0 || lastLike > 0 || Math.round(proj) > 0) {
					CurriculumCourseInterface otherCurCourseIfc = new CurriculumCourseInterface();
					otherCurCourseIfc.setCourseOfferingId(courseOffering.getUniqueId());
					otherCurCourseIfc.setCourseName(courseOffering.getCourseName());
					if (enrl > 0)
						otherCurCourseIfc.setEnrollment(enrl);
					if (lastLike > 0)
						otherCurCourseIfc.setLastLike(lastLike);
					if (Math.round(proj) > 0)
						otherCurCourseIfc.setProjection(Math.round(proj));
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
						(query.length()>2 ? "or lower(c.title) like '%' || :q || '%'" : "") + ") " +
						"order by c.subjectArea.subjectAreaAbbreviation, c.courseNbr")
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
						for (RoomLocation rm: p.getRoomLocations()) {
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
	
	public String[] getApplicationProperty(String[] name) throws CurriculaException {
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
	
	public Boolean isAdmin() throws CurriculaException {
		try {
			User user = Web.getUser(getThreadLocalRequest().getSession());
			if (user == null) throw new CurriculaException("not authenticated");
			return Roles.ADMIN_ROLE.equals(user.getRole());
		} catch  (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}

	public HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> loadProjectionRules() throws CurriculaException {
		sLog.info("loadProjectionRules()");
		Long s0 = System.currentTimeMillis();
		try {
			User user = Web.getUser(getThreadLocalRequest().getSession());
			if (user == null)
				throw new CurriculaException("not authenticated");
			if (!Roles.ADMIN_ROLE.equals(user.getRole()) && !Roles.CURRICULUM_MGR_ROLE.equals(user.getRole()) && !Roles.DEPT_SCHED_MGR_ROLE.equals(user.getRole()))
				throw new CurriculaException("not authorized to see curriculum projection rules");

			HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> rules = new HashMap<AcademicAreaInterface, HashMap<MajorInterface,HashMap<AcademicClassificationInterface, Number[]>>>();
			
			TreeSet<AcademicAreaInterface> areas = loadAcademicAreas();
			
			TreeSet<AcademicClassificationInterface> classifications = loadAcademicClassifications();
			
			Long sessionId = getAcademicSessionId();

			MajorInterface dummyMajor = new MajorInterface();
			dummyMajor.setId(-1l);
			dummyMajor.setCode("");
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> areaMajorClasf2ll = new Hashtable<String, Hashtable<String,Hashtable<String,Integer>>>();
				for (Object[] o : (List<Object[]>)hibSession.createQuery(
						"select a.academicAreaAbbreviation, m.code, f.code, count(distinct s) from LastLikeCourseDemand x inner join x.student s " +
						"inner join s.academicAreaClassifications ac inner join ac.academicClassification f inner join ac.academicArea a " +
						"inner join s.posMajors m where x.subjectArea.session.uniqueId = :sessionId " +
						"group by a.academicAreaAbbreviation, m.code, f.code")
						.setLong("sessionId", sessionId)
						.setCacheable(true).list()) {
					String area = (String)o[0];
					String major = (String)o[1];
					String clasf = (String)o[2];
					int students = ((Number)o[3]).intValue();
					Hashtable<String, Hashtable<String, Integer>> majorClasf2ll = areaMajorClasf2ll.get(area);
					if (majorClasf2ll == null) {
						majorClasf2ll = new Hashtable<String, Hashtable<String,Integer>>();
						areaMajorClasf2ll.put(area, majorClasf2ll);
					}
					Hashtable<String, Integer> clasf2ll = majorClasf2ll.get(major);
					if (clasf2ll == null) {
						clasf2ll = new Hashtable<String, Integer>();
						majorClasf2ll.put(major, clasf2ll);
					}
					clasf2ll.put(clasf, students);
				}
				
				Hashtable <Long, MajorInterface> majorLookup = new Hashtable<Long, MajorInterface>();
				Hashtable<Long, List<MajorInterface>> majors = new Hashtable<Long, List<MajorInterface>>();
				for (PosMajor major: (List<PosMajor>)hibSession.createQuery(
						"select m from PosMajor m where m.session.uniqueId=:sessionId").
						setLong("sessionId", sessionId).setCacheable(true).list()) {
					MajorInterface mi = new MajorInterface();
					mi.setId(major.getUniqueId());
					mi.setCode(major.getCode());
					mi.setName(Constants.toInitialCase(major.getName()));
					majorLookup.put(mi.getId(), mi);
					for (Iterator<AcademicArea> i = major.getAcademicAreas().iterator(); i.hasNext(); ) {
						AcademicArea a = i.next();
						List<MajorInterface> majorsOfArea = majors.get(a.getUniqueId());
						if (majorsOfArea == null) {
							majorsOfArea = new ArrayList<MajorInterface>();
							majors.put(a.getUniqueId(), majorsOfArea);
						}
						majorsOfArea.add(mi);
					}
				}
			
				Hashtable <Long, AcademicClassificationInterface> clasfLookup = new Hashtable<Long, AcademicClassificationInterface>();
				for (AcademicClassificationInterface clasf: classifications) {
					clasfLookup.put(clasf.getId(), clasf);
				}

				Hashtable <Long, AcademicAreaInterface> areaLookup = new Hashtable<Long, AcademicAreaInterface>();
				for (AcademicAreaInterface area: areas) {
					areaLookup.put(area.getId(), area);
					HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>> rules4area = rules.get(area);
					if (rules4area == null) {
						rules4area = new HashMap<MajorInterface, HashMap<AcademicClassificationInterface,Number[]>>();
						rules.put(area, rules4area);
					}
					Hashtable<String, Hashtable<String, Integer>> majorClasf2ll = areaMajorClasf2ll.get(area.getAbbv());
					HashMap<AcademicClassificationInterface, Number[]> rules4default = new HashMap<AcademicClassificationInterface, Number[]>();
					rules4area.put(dummyMajor, rules4default);
					for (AcademicClassificationInterface clasf: classifications) {
						int ll = 0;
						if (majorClasf2ll!=null) {
							for (Hashtable<String,Integer> clasf2ll: majorClasf2ll.values()) {
								Integer lastLike = clasf2ll.get(clasf.getCode());
								if (lastLike != null) ll += lastLike;
							}
						}
						rules4default.put(clasf, new Number[] { null, new Integer(ll) });
					}
					List<MajorInterface> majorsOfArea = majors.get(area.getId());
					if (majorsOfArea != null)
						for (MajorInterface major: majorsOfArea) {
							Hashtable<String,Integer> clasf2ll = (majorClasf2ll == null ? null : majorClasf2ll.get(major.getCode()));
							HashMap<AcademicClassificationInterface, Number[]> rules4major = new HashMap<AcademicClassificationInterface, Number[]>();
							rules4area.put(major, rules4major);
							for (AcademicClassificationInterface clasf: classifications) {
								Integer lastLike = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
								rules4major.put(clasf, new Number[] { null, new Integer(lastLike == null ? 0 : lastLike) });
							}
						}
				}
				
				for (CurriculumProjectionRule rule: (List<CurriculumProjectionRule>)hibSession.createQuery(
						"select r from CurriculumProjectionRule r where r.academicArea.session.uniqueId=:sessionId")
						.setLong("sessionId", sessionId).setCacheable(true).list()) {
					try {
						rules.get(areaLookup.get(rule.getAcademicArea().getUniqueId()))
						.get(rule.getMajor() == null ? dummyMajor : majorLookup.get(rule.getMajor().getUniqueId()))
						.get(clasfLookup.get(rule.getAcademicClassification().getUniqueId()))[0] = rule.getProjection();
					} catch (NullPointerException e) {}
				}
			} finally {
				hibSession.close();
			}
			sLog.info("Curriculum projection rules loaded (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return rules;
		} catch  (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public Boolean saveProjectionRules(HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> rules) throws CurriculaException {
		sLog.info("saveProjectionRules()");
		long s0 = System.currentTimeMillis();
		try {
			if (!canEditProjectionRules())
				throw new CurriculaException("not authorized to change curriculum projection rules");

			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				for (CurriculumProjectionRule rule: (List<CurriculumProjectionRule>)hibSession.createQuery(
						"select r from CurriculumProjectionRule r where academicArea.session.uniqueId=:sessionId")
						.setLong("sessionId", sessionId).setCacheable(true).list()) {
					hibSession.delete(rule);
				}
				
				for (Map.Entry<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> a: rules.entrySet()) {
					AcademicArea area = null;
					for (Map.Entry<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>> b: a.getValue().entrySet()) {
						PosMajor major = null;
						for (Map.Entry<AcademicClassificationInterface, Number[]> c: b.getValue().entrySet()) {
							if (c.getValue()[1].intValue() <= 0 || c.getValue()[0] == null) continue;
							
							if (area == null)
								area = AcademicAreaDAO.getInstance().get(a.getKey().getId(), hibSession);
							if (b.getKey().getId() >= 0 && major == null)
								major = PosMajorDAO.getInstance().get(b.getKey().getId());
							AcademicClassification clasf = AcademicClassificationDAO.getInstance().get(c.getKey().getId());
							
							CurriculumProjectionRule r = new CurriculumProjectionRule();
							r.setAcademicArea(area);
							r.setMajor(major);
							r.setAcademicClassification(clasf);
							r.setProjection(c.getValue()[0].floatValue());
							hibSession.saveOrUpdate(r);	
						}
					}
				}
				
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
			sLog.info("Curriculum projection rules saved (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch  (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public Boolean canEditProjectionRules() throws CurriculaException {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null)
			throw new CurriculaException("not authenticated");
		if (!Roles.ADMIN_ROLE.equals(user.getRole()))
			throw new CurriculaException("not authorized to change curriculum projection rules");
		return true;
	}
	
	public Boolean makeupCurriculaFromLastLikeDemands(boolean lastLike) throws CurriculaException {
		sLog.info("makeupCurriculaFromLastLikeDemands(lastLike=" + lastLike + ")");
		long s0 = System.currentTimeMillis();
		try {
			if (!isAdmin())
				throw new CurriculaException("not authorized to (re)create curricula");
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				MakeCurriculaFromLastlikeDemands m = new MakeCurriculaFromLastlikeDemands(sessionId);
				m.update(hibSession, lastLike);

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
			sLog.info("Curricula recreated (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch  (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public Boolean updateCurriculaByProjections(Set<Long> curriculumIds) throws CurriculaException {
		sLog.info("updateCurriculaByProjections(curricula=" + curriculumIds + ")");
		long s0 = System.currentTimeMillis();
		User user = Web.getUser(getThreadLocalRequest().getSession());
		try {
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();

				List<Curriculum> curricula = null;
				if (curriculumIds == null) {
					curricula = hibSession.createQuery(
							"select distinct c from Curriculum c where c.department.session.uniqueId = :sessionId")
					.setLong("sessionId", sessionId)
					.setCacheable(true).list();
				} else {
					curricula = new ArrayList<Curriculum>();
					for (Long id: curriculumIds)
						curricula.add(CurriculumDAO.getInstance().get(id, hibSession));
				}
				
				for (Curriculum c: curricula) {
					if (c == null || !c.canUserEdit(user)) continue;
					
					Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, c.getAcademicArea().getUniqueId());
					
					Hashtable<String, Hashtable<String, Integer>> majorClasf2ll = new Hashtable<String,Hashtable<String,Integer>>();
					for (Object[] o : (List<Object[]>)hibSession.createQuery(
							"select m.code, f.code, count(distinct s) from LastLikeCourseDemand x inner join x.student s " +
							"inner join s.academicAreaClassifications ac inner join ac.academicClassification f inner join ac.academicArea a " +
							"inner join s.posMajors m where x.subjectArea.session.uniqueId = :sessionId and a.uniqueId = :acadAreaId " +
							"group by m.code, f.code")
							.setLong("sessionId", sessionId)
							.setLong("acadAreaId", c.getAcademicArea().getUniqueId())
							.setCacheable(true).list()) {
						String major = (String)o[0];
						String clasf = (String)o[1];
						int students = ((Number)o[2]).intValue();
						Hashtable<String, Integer> clasf2ll = majorClasf2ll.get(major);
						if (clasf2ll == null) {
							clasf2ll = new Hashtable<String, Integer>();
							majorClasf2ll.put(major, clasf2ll);
						}
						clasf2ll.put(clasf, students);
					}

					for (Iterator<CurriculumClassification> i = c.getClassifications().iterator(); i.hasNext(); ) {
						CurriculumClassification clasf = i.next();
						
						float proj = 0.0f;
						for (Iterator<PosMajor> j = c.getMajors().iterator(); j.hasNext(); ) {
							PosMajor m = j.next();
							Hashtable<String, Integer> clasf2ll = majorClasf2ll.get(m.getCode());
							
							Integer lastLike = (clasf2ll == null ? null : clasf2ll.get(clasf.getAcademicClassification().getCode()));
							
							proj += getProjection(rules, m.getCode(), clasf.getAcademicClassification().getCode()) * (lastLike == null ? 0 : lastLike);
						}
						
						clasf.setNrStudents(Math.round(proj));
						hibSession.saveOrUpdate(clasf);
					}
					
				}
				
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
			sLog.info("Curricula update (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch  (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public Boolean populateCourseProjectedDemands(boolean includeOtherStudents) throws CurriculaException {
		sLog.info("populateCourseProjectedDemands(includeOtherStudents=" + includeOtherStudents + ")");
		long s0 = System.currentTimeMillis();
		try {
			if (!isAdmin())
				throw new CurriculaException("not authorized to populate course projected demands");
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				Hashtable<Long, Hashtable<String, Hashtable<String, Hashtable<String, Integer>>>> course2area2major2clasf2ll = null;
				if (includeOtherStudents) {
					course2area2major2clasf2ll = new Hashtable<Long, Hashtable<String, Hashtable<String,Hashtable<String,Integer>>>>();
					for (Object[] o : (List<Object[]>)hibSession.createQuery(
							"select co.uniqueId, r.academicAreaAbbreviation, m.code, f.code, count(distinct s) from " +
							"LastLikeCourseDemand x inner join x.student s inner join s.academicAreaClassifications a inner join s.posMajors m " +
							"inner join a.academicClassification f inner join a.academicArea r, CourseOffering co where " +
							"x.subjectArea.session.uniqueId = :sessionId and co.subjectArea.session.uniqueId = :sessionId and "+
							"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr)) " +
							"group by r.academicAreaAbbreviation, m.code, f.code")
							.setLong("sessionId", sessionId)
							.setCacheable(true).list()) {
						Long courseId = (Long)o[0];
						String areaAbbv = (String)o[1];
						String majorCode = (String)o[2];
						String clasfCode = (String)o[3];
						int lastLike = ((Number)o[4]).intValue();
						Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> area2major2clasf2ll = course2area2major2clasf2ll.get(courseId);
						if (area2major2clasf2ll == null) {
							area2major2clasf2ll = new Hashtable<String, Hashtable<String,Hashtable<String,Integer>>>();
							course2area2major2clasf2ll.put(courseId, area2major2clasf2ll);
						}
						Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(areaAbbv);
						if (major2clasf2ll == null) {
							major2clasf2ll = new Hashtable<String, Hashtable<String,Integer>>();
							area2major2clasf2ll.put(areaAbbv, major2clasf2ll);
						}
						Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(majorCode);
						if (clasf2ll == null) {
							clasf2ll = new Hashtable<String, Integer>();
							major2clasf2ll.put(majorCode, clasf2ll);
						}
						clasf2ll.put(clasfCode, lastLike);
					}
				}
				
				Hashtable<String, Hashtable<String, HashMap<String, Float>>> rules = (includeOtherStudents ? getRules(hibSession) : null);
				
				Hashtable<Long, List<CurriculumCourse>> course2curriculum = new Hashtable<Long, List<CurriculumCourse>>();
				for (CurriculumCourse cc: (List<CurriculumCourse>)hibSession.createQuery(
						"select cc from CurriculumCourse cc where cc.classification.curriculum.academicArea.session.uniqueId = :sessionId")
						.setLong("sessionId", sessionId)
						.setCacheable(true).list()) {
					List<CurriculumCourse> curricula = course2curriculum.get(cc.getCourse().getUniqueId());
					if (curricula == null) {
						curricula = new ArrayList<CurriculumCourse>();
						course2curriculum.put(cc.getCourse().getUniqueId(), curricula);
					}
					curricula.add(cc);
				}
				
				for (CourseOffering courseOffering: (List<CourseOffering>)hibSession.createQuery(
						"select co from CourseOffering co where co.subjectArea.session.uniqueId = :sessionId")
						.setLong("sessionId", sessionId)
						.setCacheable(true).list()) {

					Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> area2major2clasf2ll = (course2area2major2clasf2ll == null ? null : course2area2major2clasf2ll.get(courseOffering.getUniqueId()));
					List<CurriculumCourse> curricula = course2curriculum.get(courseOffering.getUniqueId());
					
					float demand = 0.0f;
					if (curricula != null)
						for (CurriculumCourse curriculum: curricula) {
							demand += curriculum.getPercShare() * curriculum.getClassification().getNrStudents();
							if (area2major2clasf2ll != null) {
								String areaAbbv = curriculum.getClassification().getCurriculum().getAcademicArea().getAcademicAreaAbbreviation();
								Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(areaAbbv);
								if (major2clasf2ll != null) {
									if (curriculum.getClassification().getCurriculum().getMajors().isEmpty()) {
										for (Hashtable<String, Integer> clasf2ll: major2clasf2ll.values()) {
											clasf2ll.remove(curriculum.getClassification().getAcademicClassification().getCode());
										}
									} else {
										for (PosMajor major: (Collection<PosMajor>)curriculum.getClassification().getCurriculum().getMajors()) {
											Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(major.getCode());
											if (clasf2ll != null)
												clasf2ll.remove(curriculum.getClassification().getAcademicClassification().getCode());
										}
									}
								}
							}
						}
					
					if (area2major2clasf2ll != null) {
						for (Map.Entry<String, Hashtable<String, Hashtable<String, Integer>>> areaEmajor2clasf2ll: area2major2clasf2ll.entrySet()) {
							for (Map.Entry<String, Hashtable<String, Integer>> majorEclasf2ll: areaEmajor2clasf2ll.getValue().entrySet()) {
								for (Map.Entry<String, Integer> clasfEll: majorEclasf2ll.getValue().entrySet()) {
									demand += getProjection(rules == null ? null : rules.get(areaEmajor2clasf2ll.getKey()), majorEclasf2ll.getKey(), clasfEll.getKey()) * clasfEll.getValue();
								}
							}
						}
					}
					
					courseOffering.setProjectedDemand(Math.round(demand));
					
					hibSession.saveOrUpdate(courseOffering);
				}
				
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
			sLog.info("Curricula recreated (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch  (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public Boolean populateCourseProjectedDemands(boolean includeOtherStudents, Long offeringId) throws CurriculaException {
		sLog.info("populateCourseProjectedDemands(includeOtherStudents=" + includeOtherStudents + ", offering=" + offeringId +")");
		long s0 = System.currentTimeMillis();
		try {
			User user = Web.getUser(getThreadLocalRequest().getSession());
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(offeringId, hibSession);
				if (offering == null) throw new CurriculaException("offering " + offeringId + " does not exist");
				
				if (!offering.isEditableBy(user)) {
					if (user == null || !Roles.CURRICULUM_MGR_ROLE.equals(user.getRole()) || !getManager().getDepartments().contains(offering.getDepartment()))
						throw new CurriculaException("not authorized to populate course projected demands");
				}
				
				int offeringDemand = 0;
				for (Iterator<CourseOffering> i = offering.getCourseOfferings().iterator(); i.hasNext(); ) {
					CourseOffering courseOffering = i.next();
					
					
					Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> area2major2clasf2ll =  null;
					if (includeOtherStudents) {
						area2major2clasf2ll = new Hashtable<String, Hashtable<String,Hashtable<String,Integer>>>();
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
							String areaAbbv = (String)o[0];
							String majorCode = (String)o[1];
							String clasfCode = (String)o[2];
							int lastLike = ((Number)o[3]).intValue();
							Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(areaAbbv);
							if (major2clasf2ll == null) {
								major2clasf2ll = new Hashtable<String, Hashtable<String,Integer>>();
								area2major2clasf2ll.put(areaAbbv, major2clasf2ll);
							}
							Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(majorCode);
							if (clasf2ll == null) {
								clasf2ll = new Hashtable<String, Integer>();
								major2clasf2ll.put(majorCode, clasf2ll);
							}
							clasf2ll.put(clasfCode, lastLike);
						}
					}
					
					Hashtable<String, Hashtable<String, HashMap<String, Float>>> rules = (includeOtherStudents ? getRules(hibSession) : null);
					
					float demand = 0.0f;
					for (CurriculumCourse curriculum: (List<CurriculumCourse>)hibSession.createQuery(
							"select cc from CurriculumCourse cc where cc.course.uniqueId = :courseId")
							.setLong("courseId", courseOffering.getUniqueId())
							.setCacheable(true).list()) {
						demand += curriculum.getPercShare() * curriculum.getClassification().getNrStudents();
						if (area2major2clasf2ll != null) {
							String areaAbbv = curriculum.getClassification().getCurriculum().getAcademicArea().getAcademicAreaAbbreviation();
							Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(areaAbbv);
							if (major2clasf2ll != null) {
								if (curriculum.getClassification().getCurriculum().getMajors().isEmpty()) {
									for (Hashtable<String, Integer> clasf2ll: major2clasf2ll.values()) {
										clasf2ll.remove(curriculum.getClassification().getAcademicClassification().getCode());
									}
								} else {
									for (PosMajor major: (Collection<PosMajor>)curriculum.getClassification().getCurriculum().getMajors()) {
										Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(major.getCode());
										if (clasf2ll != null)
											clasf2ll.remove(curriculum.getClassification().getAcademicClassification().getCode());
									}
								}
							}
						}
					}
						
						
					if (area2major2clasf2ll != null) {
						for (Map.Entry<String, Hashtable<String, Hashtable<String, Integer>>> areaEmajor2clasf2ll: area2major2clasf2ll.entrySet()) {
							for (Map.Entry<String, Hashtable<String, Integer>> majorEclasf2ll: areaEmajor2clasf2ll.getValue().entrySet()) {
								for (Map.Entry<String, Integer> clasfEll: majorEclasf2ll.getValue().entrySet()) {
									demand += getProjection(rules == null ? null : rules.get(areaEmajor2clasf2ll.getKey()), majorEclasf2ll.getKey(), clasfEll.getKey()) * clasfEll.getValue();
								}
							}
						}
					}
					
					courseOffering.setProjectedDemand(Math.round(demand));
					
					offeringDemand += Math.round(demand);
					
					hibSession.saveOrUpdate(courseOffering);
				}
				
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
			sLog.info("Curricula recreated (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch  (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}


}
