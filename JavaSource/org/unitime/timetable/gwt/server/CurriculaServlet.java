/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
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


import org.apache.log4j.Logger;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
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
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumCourseGroup;
import org.unitime.timetable.model.CurriculumProjectionRule;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.PosMajorDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.DefaultCourseDetailsProvider;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.curricula.CurriculumFilterBackend;
import org.unitime.timetable.test.MakeCurriculaFromLastlikeDemands;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
@Service("curricula.gwt")
public class CurriculaServlet implements CurriculaService {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Logger sLog = Logger.getLogger(CurriculaServlet.class);
	private static DecimalFormat sDF = new DecimalFormat("0.0");
	private CourseDetailsProvider iCourseDetailsProvider;
	
	public CurriculaServlet() {}
	
	private CourseDetailsProvider getCourseDetailsProvider() {
		if (iCourseDetailsProvider == null) {
			try {
				String providerClass = ApplicationProperty.CustomizationCourseDetails.value();
				if (providerClass != null)
					iCourseDetailsProvider = (CourseDetailsProvider)Class.forName(providerClass).newInstance();
			} catch (Exception e) {
				sLog.warn("Failed to initialize course detail provider: " + e.getMessage());
				iCourseDetailsProvider = new DefaultCourseDetailsProvider();
			}
		}
		return iCourseDetailsProvider;
	}
	
	private @Autowired SessionContext sessionContext;
	private SessionContext getSessionContext() { return sessionContext; }
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public TreeSet<CurriculumInterface> findCurricula(CurriculumInterface.CurriculumFilterRpcRequest filter) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("findCurricula(filter='" + filter+"')");
			Long s0 = System.currentTimeMillis();
			TreeSet<CurriculumInterface> results = new TreeSet<CurriculumInterface>();
			getSessionContext().setAttribute("Curricula.LastFilter", filter.toQueryString());
			for (Curriculum c: CurriculumFilterBackend.curricula(getSessionContext().getUser().getCurrentAcademicSessionId(), filter.getOptions(), new Query(filter.getText()), -1, null, Department.getUserDepartments(getSessionContext().getUser()))) {
				CurriculumInterface ci = new CurriculumInterface();
				ci.setId(c.getUniqueId());
				ci.setAbbv(c.getAbbv());
				ci.setName(c.getName());
				ci.setEditable(sessionContext.hasPermission(c, Right.CurriculumEdit));
				DepartmentInterface di = new DepartmentInterface();
				di.setId(c.getDepartment().getUniqueId());
				di.setAbbv(c.getDepartment().getAbbreviation());
				di.setCode(c.getDepartment().getDeptCode());
				di.setName(c.getDepartment().getName());
				ci.setDepartment(di);
				AcademicAreaInterface ai = new AcademicAreaInterface();
				ai.setId(c.getAcademicArea().getUniqueId());
				ai.setAbbv(c.getAcademicArea().getAcademicAreaAbbreviation());
				ai.setName(Constants.curriculaToInitialCase(c.getAcademicArea().getTitle()));
				ci.setAcademicArea(ai);
				for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
					PosMajor major = i.next();
					MajorInterface mi = new MajorInterface();
					mi.setId(major.getUniqueId());
					mi.setCode(major.getCode());
					mi.setName(Constants.curriculaToInitialCase(major.getName()));
					ci.addMajor(mi);
				}
				results.add(ci);
			}
			sLog.debug("Found " + results.size() + " curricula (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public List<CurriculumClassificationInterface> loadClassifications(List<Long> curriculumIds) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("loadClassifications(curriculumIds=" + curriculumIds + ")");
			Long s0 = System.currentTimeMillis();
			if (curriculumIds == null || curriculumIds.isEmpty()) return new ArrayList<CurriculumClassificationInterface>();
			
			List<CurriculumClassificationInterface> results = new ArrayList<CurriculumClassificationInterface>();
			
			TreeSet<AcademicClassificationInterface> academicClassifications = loadAcademicClassifications();

			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				for (Long curriculumId: curriculumIds) {
					Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
					if (c == null) throw new CurriculaException(MESSAGES.errorCurriculumDoesNotExist(curriculumId == null ? "null" : curriculumId.toString()));
					
					Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, c.getAcademicArea().getUniqueId());
					
					Hashtable<Long, Integer> clasf2enrl = loadClasf2enrl(hibSession, c);
					
					Hashtable<Long, Integer> clasf2req = loadClasf2req(hibSession, c);
					
					Hashtable<String, Hashtable<String, Integer>> clasfMajor2ll = loadClasfMajor2ll(hibSession, c);
					
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
						cfi.setRequested(clasf2req.get(clasf.getAcademicClassification().getUniqueId()));
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
							cfi.setRequested(clasf2req.get(clasf.getId()));
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
			sLog.debug("Loaded " + results.size() + " classifications (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission(#curriculumId, 'Curriculum', 'CurriculumDetail')")
	public CurriculumInterface loadCurriculum(Long curriculumId) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("loadCurriculum(curriculumId=" + curriculumId + ")");
			Long s0 = System.currentTimeMillis();

			TreeSet<AcademicClassificationInterface> academicClassifications = loadAcademicClassifications();
			Hashtable<Long, Integer> classifications = new Hashtable<Long, Integer>();
			int idx = 0;
			for (AcademicClassificationInterface clasf: academicClassifications) {
				classifications.put(clasf.getId(), idx++);
			}
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
				if (c == null) throw new CurriculaException(MESSAGES.errorCurriculumDoesNotExist(curriculumId == null ? "null" : curriculumId.toString()));
				Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, c.getAcademicArea().getUniqueId());
				CurriculumInterface curriculumIfc = new CurriculumInterface();
				curriculumIfc.setId(c.getUniqueId());
				curriculumIfc.setAbbv(c.getAbbv());
				curriculumIfc.setName(c.getName());
				curriculumIfc.setEditable(sessionContext.hasPermission(c, Right.CurriculumEdit));
				DepartmentInterface deptIfc = new DepartmentInterface();
				deptIfc.setId(c.getDepartment().getUniqueId());
				deptIfc.setAbbv(c.getDepartment().getAbbreviation());
				deptIfc.setCode(c.getDepartment().getDeptCode());
				deptIfc.setName(c.getDepartment().getName());
				curriculumIfc.setDepartment(deptIfc);
				AcademicAreaInterface areaIfc = new AcademicAreaInterface();
				areaIfc.setId(c.getAcademicArea().getUniqueId());
				areaIfc.setAbbv(c.getAcademicArea().getAcademicAreaAbbreviation());
				areaIfc.setName(Constants.curriculaToInitialCase(c.getAcademicArea().getTitle()));
				curriculumIfc.setAcademicArea(areaIfc);
				for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
					PosMajor major = i.next();
					MajorInterface majorIfc = new MajorInterface();
					majorIfc.setId(major.getUniqueId());
					majorIfc.setCode(major.getCode());
					majorIfc.setName(Constants.curriculaToInitialCase(major.getName()));
					curriculumIfc.addMajor(majorIfc);
				}
				
				Hashtable<Long, Hashtable<Long, Integer>> clasf2course2enrl = loadClasfCourse2enrl(hibSession, c);
				
				Hashtable<Long, Hashtable<Long, Integer>> clasf2course2req = loadClasfCourse2req(hibSession, c);
				
				Hashtable<Long, Integer> clasf2enrl = loadClasf2enrl(hibSession, c);
				
				Hashtable<Long, Integer> clasf2req = loadClasf2req(hibSession, c);
				
				Hashtable<String, Hashtable<String, Integer>> clasfMajor2ll = loadClasfMajor2ll(hibSession, c);

				Hashtable<String, Hashtable<String, Hashtable<Long, Integer>>> clasfMajor2course2ll = loadClasfMajorCourse2ll(hibSession, c);
				
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
					clasfIfc.setRequested(clasf2req.get(clasf.getAcademicClassification().getUniqueId()));
					clasfIfc.setExpected(clasf.getNrStudents());
					AcademicClassificationInterface acadClasfIfc = new AcademicClassificationInterface();
					acadClasfIfc.setId(clasf.getAcademicClassification().getUniqueId());
					acadClasfIfc.setName(clasf.getAcademicClassification().getName());
					acadClasfIfc.setCode(clasf.getAcademicClassification().getCode());
					clasfIfc.setAcademicClassification(acadClasfIfc);
					curriculumIfc.addClassification(clasfIfc);
					Hashtable<Long, Integer> course2enrl = clasf2course2enrl.get(clasf.getAcademicClassification().getUniqueId());
					Hashtable<Long, Integer> course2req = clasf2course2req.get(clasf.getAcademicClassification().getUniqueId());
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
						curCourseIfc.setRequested(course2req == null ? null : course2req.get(course.getCourse().getUniqueId()));
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
						clasfIfc.setRequested(clasf2req.get(clasf.getId()));
						AcademicClassificationInterface acadClasfIfc = new AcademicClassificationInterface();
						acadClasfIfc.setId(clasf.getId());
						acadClasfIfc.setName(clasf.getName());
						acadClasfIfc.setCode(clasf.getCode());
						clasfIfc.setAcademicClassification(acadClasfIfc);
						curriculumIfc.addClassification(clasfIfc);
					}
				}
				
				ChangeLog ch = ChangeLog.findLastChange(c);
				if (ch != null) curriculumIfc.setLastChange(ch.getShortLabel());
				
				sLog.debug("Loaded 1 curriculum (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
				return curriculumIfc;
			} finally {
				hibSession.close();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("(#curriculum.id != null and checkPermission(#curriculum.id, 'Curriculum', 'CurriculumEdit')) or (#curriculum.id == null and checkPermission(#curriculum.department.id, 'Department', 'CurriculumAdd'))")
	public Long saveCurriculum(CurriculumInterface curriculum) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("saveCurriculum(curriculum=" + curriculum.getId() + ")");
			Long s0 = System.currentTimeMillis();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				Hashtable<String, CourseOffering> courses = new Hashtable<String, CourseOffering>();
				if (curriculum.hasCourses())
					for (CourseInterface course: curriculum.getCourses()) {
						CourseOffering courseOffering = getCourse(hibSession, course.getCourseName());
						if (courseOffering == null) throw new CurriculaException(MESSAGES.errorCourseDoesNotExist(course.getCourseName()));
						courses.put(course.getCourseName(), courseOffering);
					}
			
				Curriculum c = null;
				if (curriculum.getId() != null) {
					c = CurriculumDAO.getInstance().get(curriculum.getId(), hibSession);
					if (c == null) throw new CurriculaException(MESSAGES.errorCurriculumDoesNotExist(curriculum.getId().toString()));
				} else {
					c = new Curriculum();
				}
				c.setAbbv(curriculum.getAbbv());
				c.setName(curriculum.getName());
				c.setAcademicArea(AcademicAreaDAO.getInstance().get(curriculum.getAcademicArea().getId(), hibSession));
				c.setDepartment(DepartmentDAO.getInstance().get(curriculum.getDepartment().getId(), hibSession));
				if (c.getMajors() == null) {
					c.setMajors(new HashSet());
					if (curriculum.hasMajors()) 
						for (MajorInterface m: curriculum.getMajors()) {
							c.getMajors().add(PosMajorDAO.getInstance().get(m.getId(), hibSession));
						}
				} else {
					HashSet<PosMajor> remove = new HashSet<PosMajor>(c.getMajors());
					if (curriculum.hasMajors()) 
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
				
				Long ret = c.getUniqueId();
				if (ret == null) {
					ret = (Long)hibSession.save(c);
				} else {
					hibSession.update(c);
				}
				
				ChangeLog.addChange(hibSession,
						getSessionContext(),
						c,
						c.getAbbv(),
						Source.CURRICULUM_EDIT, 
						(curriculum.getId() == null ? Operation.CREATE : Operation.UPDATE),
						null,
						c.getDepartment());
				
				hibSession.flush();
				tx.commit(); tx = null;

				sLog.debug("Saved 1 curriculum (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
				return ret;
			} finally {
				try {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				} catch (Exception e) {}
				hibSession.close();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission(#curricula, 'Curriculum', 'CurriculumEdit')")
	public Boolean saveClassifications(List<CurriculumInterface> curricula) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("saveClassifications()");
			Long s0 = System.currentTimeMillis();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				int ord = 0;
				for (CurriculumInterface curriculum: curricula) {
					Curriculum c = CurriculumDAO.getInstance().get(curriculum.getId(), hibSession);
					if (c == null) continue;
					
					HashSet<CurriculumClassification> remove = new HashSet<CurriculumClassification>(c.getClassifications());
					
					clasf: for (CurriculumClassificationInterface clasf: curriculum.getClassifications()) {
						if (clasf.getExpected() == null) continue;
						
						for (Iterator<CurriculumClassification> i = c.getClassifications().iterator(); i.hasNext();) {
							CurriculumClassification cl = i.next();
							if (cl.getAcademicClassification().getUniqueId().equals(clasf.getAcademicClassification().getId())) {
								cl.setNrStudents(clasf.getExpected());
								remove.remove(cl);
								hibSession.saveOrUpdate(cl);
								continue clasf;
							}
						}
						
						CurriculumClassification cl = new CurriculumClassification();
						cl.setAcademicClassification(AcademicClassificationDAO.getInstance().get(clasf.getAcademicClassification().getId()));
						cl.setName(clasf.getAcademicClassification().getCode());
						cl.setNrStudents(clasf.getExpected());
						cl.setOrd(ord++);
						cl.setCurriculum(c);
						c.getClassifications().add(cl);
						hibSession.saveOrUpdate(cl);
					}
					
					for (CurriculumClassification cl: remove) {
						c.getClassifications().remove(cl);
						cl.setCurriculum(null);
						hibSession.delete(cl);
					}
					
					ChangeLog.addChange(hibSession,
							getSessionContext(),
							c,
							c.getAbbv(),
							Source.CUR_CLASF_EDIT, 
							Operation.UPDATE,
							null,
							c.getDepartment());

					hibSession.saveOrUpdate(c);
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
			sLog.debug("Saved classifications for " + curricula.size() + " curricula (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission(#curriculumId, 'Curriculum', 'CurriculumDelete')")
	public Boolean deleteCurriculum(Long curriculumId) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("deleteCurriculum(curriculumId=" + curriculumId + ")");
			Long s0 = System.currentTimeMillis();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				if (curriculumId == null) 
					throw new CurriculaException(MESSAGES.errorCannotDeleteUnsavedCurriculum());
				
				Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
				if (c == null) throw new CurriculaException(MESSAGES.errorCurriculumDoesNotExist(curriculumId.toString()));
				
				ChangeLog.addChange(hibSession,
						getSessionContext(),
						c,
						c.getAbbv(),
						Source.CURRICULUM_EDIT, 
						Operation.DELETE,
						null,
						c.getDepartment());
				
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
			sLog.debug("Deleted 1 curriculum (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission(#curriculumIds, 'Curriculum', 'CurriculumDelete')")
	public Boolean deleteCurricula(Set<Long> curriculumIds) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("deleteCurricula(curriculumIds=" + curriculumIds + ")");
			Long s0 = System.currentTimeMillis();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				for (Long curriculumId: curriculumIds) {
					if (curriculumId == null) 
						throw new CurriculaException(MESSAGES.errorCannotDeleteUnsavedCurriculum());
					
					Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
					if (c == null) throw new CurriculaException(MESSAGES.errorCurriculumDoesNotExist(curriculumId.toString()));
					
					ChangeLog.addChange(hibSession,
							getSessionContext(),
							c,
							c.getAbbv(),
							Source.CURRICULUM_EDIT, 
							Operation.DELETE,
							null,
							c.getDepartment());
					
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
			sLog.debug("Deleted " + curriculumIds.size() + " curricula (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}

	@PreAuthorize("checkPermission(#curriculumIds, 'Curriculum', 'CurriculumMerge')")
	public Boolean mergeCurricula(Set<Long> curriculumIds) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("mergeCurricula(curriculumIds=" + curriculumIds + ")");
			Long s0 = System.currentTimeMillis();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				Curriculum mergedCurriculum = new Curriculum();
				mergedCurriculum.setMajors(new HashSet());
				mergedCurriculum.setClassifications(new HashSet());
				
				int clasfOrd = 0, courseOrd = 0, cidx = 0;
				Hashtable<Long, CurriculumCourseGroup> groups = new Hashtable<Long, CurriculumCourseGroup>();
				
				ArrayList<Curriculum> merged = new ArrayList<Curriculum>();
				
				for (Long curriculumId: curriculumIds) {
					if (curriculumId == null) 
						throw new CurriculaException(MESSAGES.errorCannotMergeUnsavedCurriculum());
					
					Curriculum curriculum = CurriculumDAO.getInstance().get(curriculumId, hibSession);
					if (curriculum == null) throw new CurriculaException(MESSAGES.errorCurriculumDoesNotExist(curriculumId.toString()));
					
					cidx++;
						
					if (mergedCurriculum.getAcademicArea() == null) {
						mergedCurriculum.setAcademicArea(curriculum.getAcademicArea());
					} else if (!mergedCurriculum.getAcademicArea().equals(curriculum.getAcademicArea()))
						throw new CurriculaException(MESSAGES.errorCannotMergeDifferentAcademicAreas());

					if (mergedCurriculum.getDepartment() == null) {
						mergedCurriculum.setDepartment(curriculum.getDepartment());
					} else if (!mergedCurriculum.getDepartment().equals(curriculum.getDepartment()))
						throw new CurriculaException(MESSAGES.errorCannotMergeDifferentDepartments());
						
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
					
					merged.add(curriculum);
				}
				
				if (mergedCurriculum.getAcademicArea() != null) {
					String abbv = mergedCurriculum.getAcademicArea().getAcademicAreaAbbreviation();
					String name = Constants.curriculaToInitialCase(mergedCurriculum.getAcademicArea().getTitle());
					
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
						name += Constants.curriculaToInitialCase(m.getName());
					}

					if (abbv.length() > 20) abbv = abbv.substring(0, 20);
					mergedCurriculum.setAbbv(abbv);
					
					if (name.length() > 60) name = name.substring(0, 60);
					mergedCurriculum.setName(name);
					
					hibSession.saveOrUpdate(mergedCurriculum);
					
					for (CurriculumCourseGroup g: groups.values())
						hibSession.saveOrUpdate(g);
				}
				
				for (Curriculum curriculum: merged) {
					ChangeLog.addChange(hibSession,
							getSessionContext(),
							curriculum,
							curriculum.getAbbv() + " &rarr; " + mergedCurriculum.getAbbv(),
							Source.CURRICULA, 
							Operation.MERGE,
							null,
							curriculum.getDepartment());
					
					hibSession.delete(curriculum);
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
			sLog.debug("Merged " + curriculumIds.size() + " curricula (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public TreeSet<AcademicClassificationInterface> loadAcademicClassifications() throws CurriculaException, PageAccessException {
		try {
			sLog.debug("loadAcademicClassifications()");
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
			sLog.debug("Loaded " + results.size() + " academic classifications (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public HashMap<String, CurriculumStudentsInterface[]> computeEnrollmentsAndLastLikes(Long acadAreaId, List<Long> majors) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("computeEnrollmentsAndLastLikes(acadAreaId=" + acadAreaId + ", majors=" + majors + ")");
			Long s0 = System.currentTimeMillis();
			if (acadAreaId == null) return new HashMap<String, CurriculumStudentsInterface[]>();
			Hashtable<Long, Integer> classificationIndex = new Hashtable<Long, Integer>();
			int idx = 0;
			TreeSet<AcademicClassificationInterface> classifications = loadAcademicClassifications();
			for (AcademicClassificationInterface clasf: classifications) {
				classificationIndex.put(clasf.getId(), idx++);
			}
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			HashMap<String, CurriculumStudentsInterface[]> results = new HashMap<String, CurriculumStudentsInterface[]>();
			try {
				Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, acadAreaId);

				AcademicArea acadArea = AcademicAreaDAO.getInstance().get(acadAreaId, hibSession);
				
				List<PosMajor> posMajors = new ArrayList<PosMajor>();
				for (Long majorId: majors) {
					posMajors.add(PosMajorDAO.getInstance().get(majorId,hibSession));
				}
				
				Hashtable<Long, Set<Long>> clasf2enrl = loadClasf2enrl(hibSession, acadAreaId, majors);
				
				Hashtable<Long, Set<Long>> clasf2req = loadClasf2req(hibSession, acadAreaId, majors);

				Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> clasf2course2enrl = loadClasfCourse2enrl(hibSession, acadAreaId, majors);
				
				Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> clasf2course2req = loadClasfCourse2req(hibSession, acadAreaId, majors);
				
				Hashtable<String, HashMap<String, Set<Long>>> clasf2ll = loadClasfMajor2ll(hibSession, acadArea.getAcademicAreaAbbreviation(), posMajors);
				
				Hashtable<String, Hashtable<CourseInterface, HashMap<String, Set<Long>>>> clasf2course2ll = loadClasfCourseMajor2ll(hibSession, acadArea.getAcademicAreaAbbreviation(), posMajors);
				
				for (AcademicClassificationInterface clasf: classifications) {
					
					CurriculumStudentsInterface[] x = results.get("");
					if (x == null) {
						x = new CurriculumStudentsInterface[classificationIndex.size()];
						results.put("", x);
					}
					
					int col = classificationIndex.get(clasf.getId());
					x[col] = new CurriculumStudentsInterface();
					x[col].setProjection(rules.get(clasf.getCode()));
					x[col].setEnrolledStudents(clasf2enrl.get(clasf.getId()));
					x[col].setLastLikeStudents(clasf2ll.get(clasf.getCode()));
					x[col].setRequestedStudents(clasf2req.get(clasf.getId()));
					
					Hashtable<CourseInterface, HashMap<String, Set<Long>>> lastLike = clasf2course2ll.get(clasf.getCode());
					Hashtable<CourseInterface, Set<Long>> enrollment = clasf2course2enrl.get(clasf.getId());
					Hashtable<CourseInterface, Set<Long>> requested = clasf2course2req.get(clasf.getId());
					
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
					
					if (requested != null)
						courses.addAll(requested.keySet());
					
					for (CourseInterface co: courses) {
						CurriculumStudentsInterface[] c = results.get(co.getCourseName());
						if (c == null) {
							c = new CurriculumStudentsInterface[classificationIndex.size()];
							results.put(co.getCourseName(), c);
						}
						c[col] = new CurriculumStudentsInterface();
						c[col].setProjection(rules == null ? null : rules.get(clasf.getCode()));
						c[col].setEnrolledStudents(enrollment == null ? null : enrollment.get(co));
						c[col].setLastLikeStudents(lastLike == null ? null : lastLike.get(co));
						c[col].setRequestedStudents(requested == null ? null : requested.get(co));
					}
				}
			} finally {
				hibSession.close();
			}
			sLog.debug("Found " + results.size() + " courses with enrollments/last-like data (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	private TreeSet<CurriculumInterface> loadCurriculaForACourse(org.hibernate.Session hibSession, TreeSet<AcademicClassificationInterface> academicClassifications, TreeSet<AcademicAreaInterface> academicAreas, CourseOffering courseOffering) throws CurriculaException, PageAccessException {
		TreeSet<CurriculumInterface> results = new TreeSet<CurriculumInterface>();
		
		Hashtable<Long, Integer> classifications = new Hashtable<Long, Integer>();
		int idx = 0;
		for (AcademicClassificationInterface clasf: academicClassifications) {
			classifications.put(clasf.getId(), idx++);
		}
		
		Hashtable<Long, Hashtable<Long, Hashtable<Long, Integer>>> area2major2clasf2enrl = loadAreaMajorClasf2enrl(hibSession, courseOffering.getUniqueId());
		
		Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> area2major2clasf2ll = loadAreaMajorClasf2ll(hibSession, courseOffering.getUniqueId());
		
		Hashtable<Long, Hashtable<Long, Hashtable<Long, Integer>>> area2major2clasf2req = loadAreaMajorClasf2req(hibSession, courseOffering.getUniqueId());
		
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
				areaIfc.setName(Constants.curriculaToInitialCase(curriculum.getAcademicArea().getTitle()));
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
					mi.setName(Constants.curriculaToInitialCase(major.getName()));
					curriculumIfc.addMajor(mi);
				}
				curricula.put(curriculum.getUniqueId(), curriculumIfc);
				results.add(curriculumIfc);

				cur2clasf2enrl.put(curriculum.getUniqueId(), loadClasf2enrl(hibSession, curriculum));
				
				cur2clasf2ll.put(curriculum.getUniqueId(), loadClasf2ll(hibSession, curriculum));

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
			Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(curriculum.getAcademicArea().getAcademicAreaAbbreviation());
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
			
			int req = 0;
			Hashtable<Long, Hashtable<Long, Integer>> major2clasf2req = area2major2clasf2req.get(curriculum.getAcademicArea().getUniqueId());
			if (major2clasf2req != null) {
				if (curriculum.getMajors().isEmpty()) {
					for (Long majorId: major2clasf2req.keySet()) {
						Hashtable<Long, Integer> clasf2req = major2clasf2req.get(majorId);
						Integer e = (clasf2req == null ? null : clasf2req.get(clasf.getAcademicClassification().getUniqueId()));
						if (e != null) {
							req += e;
							clasf2req.remove(clasf.getAcademicClassification().getUniqueId());
						}
					}
				} else {
					for (Iterator<PosMajor> i = curriculum.getMajors().iterator(); i.hasNext(); ) {
						PosMajor m = i.next();
						Hashtable<Long, Integer> clasf2req = major2clasf2req.get(m.getUniqueId());
						Integer e = (clasf2req == null ? null : clasf2req.get(clasf.getAcademicClassification().getUniqueId()));
						if (e != null) {
							req += e;
							clasf2req.remove(clasf.getAcademicClassification().getUniqueId());
						}
					}
				}
			}
			if (req > 0)
				curCourseIfc.setRequested(req);
			
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
				Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(curriculumIfc.getAcademicArea().getAbbv());
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
								clasf2ll.remove(clasf.getCode());
							}
						}
					}
				}
				
				int req = 0;
				Hashtable<Long, Hashtable<Long, Integer>> major2clasf2req = area2major2clasf2req.get(curriculumIfc.getAcademicArea().getId());
				if (major2clasf2req != null) {
					if (!curriculumIfc.hasMajors()) {
						for (Long majorId: major2clasf2req.keySet()) {
							Hashtable<Long, Integer> clasf2req = major2clasf2req.get(majorId);
							Integer e = (clasf2req == null ? null : clasf2req.get(clasf.getId()));
							if (e != null) {
								req += e;
								clasf2req.remove(clasf.getId());
							}
						}
					} else {
						for (MajorInterface m: curriculumIfc.getMajors()) {
							Hashtable<Long, Integer> clasf2req = major2clasf2req.get(m.getId());
							Integer e = (clasf2req == null ? null : clasf2req.get(clasf.getId()));
							if (e != null) {
								req += e;
								clasf2req.remove(clasf.getId());
							}
						}
					}
				}
				
				
				if (enrl > 0 || lastLike > 0 || Math.round(proj) > 0 || req > 0) {
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
					
					if (req > 0)
						curCourseIfc.setRequested(req);
					
					courseIfc.setCurriculumCourse(classifications.get(clasf.getId()), curCourseIfc);
				}
			}
		}
		
		HashSet<Long> areas = new HashSet<Long>();
		areas.addAll(area2major2clasf2enrl.keySet());
		areas.addAll(area2major2clasf2req.keySet());
		Hashtable<Long, String> areasId2Abbv = new Hashtable<Long, String>();
		for (AcademicAreaInterface area: academicAreas) {
			areasId2Abbv.put(area.getId(), area.getAbbv());
			if (area2major2clasf2ll.containsKey(area.getAbbv())) areas.add(area.getId());
		}
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
				int proj = 0;
				Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(areasId2Abbv.get(areaId));
				if (major2clasf2ll != null) {
					for (String majorCode: major2clasf2ll.keySet()) {
						Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(majorCode);
						Integer e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
						if (e != null) {
							lastLike += e;
							proj += Math.round(getProjection(rules, majorCode, clasf.getCode()) * e);
						}
					}
				}
				int req = 0;
				Hashtable<Long, Hashtable<Long, Integer>> major2clasf2req = area2major2clasf2req.get(areaId);
				if (major2clasf2req != null) {
					for (Long majorId: major2clasf2req.keySet()) {
						Hashtable<Long, Integer> clasf2req = major2clasf2req.get(majorId);
						Integer e = (clasf2req == null ? null : clasf2req.get(clasf.getId()));
						if (e != null) {
							req += e;
						}
					}
				}
				if (enrl > 0 || lastLike > 0 || proj > 0 || req > 0) {
					CurriculumCourseInterface otherCurCourseIfc = new CurriculumCourseInterface();
					otherCurCourseIfc.setCourseOfferingId(courseOffering.getUniqueId());
					otherCurCourseIfc.setCourseName(courseOffering.getCourseName());
					if (enrl > 0)
						otherCurCourseIfc.setEnrollment(enrl);
					if (lastLike > 0)
						otherCurCourseIfc.setLastLike(lastLike);
					if (proj > 0)
						otherCurCourseIfc.setProjection(proj);
					if (req > 0)
						otherCurCourseIfc.setRequested(req);
					otherCourseIfc.setCurriculumCourse(classifications.get(clasf.getId()), otherCurCourseIfc);
					empty = false;
				}
			}
			if (empty) continue;
			AcademicArea a = AcademicAreaDAO.getInstance().get(areaId, hibSession);
			AcademicAreaInterface areaIfc = new AcademicAreaInterface();
			areaIfc.setId(a.getUniqueId());
			areaIfc.setAbbv(a.getAcademicAreaAbbreviation());
			areaIfc.setName(Constants.curriculaToInitialCase(a.getTitle()));
			otherCurriculumIfc.setAcademicArea(areaIfc);
			otherCurriculumIfc.setAbbv(areaIfc.getAbbv());
			otherCurriculumIfc.setName(areaIfc.getName());
			results.add(otherCurriculumIfc);
		}
		
		return results;
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public TreeSet<CurriculumInterface> findCurriculaForACourse(String courseName) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("getCurriculaForACourse(courseName='" + courseName + "')");
			Long s0 = System.currentTimeMillis();
			
			TreeSet<AcademicClassificationInterface> academicClassifications = loadAcademicClassifications();
			TreeSet<AcademicAreaInterface> academicAreas = loadAcademicAreas();

			TreeSet<CurriculumInterface> results = null;
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				CourseOffering courseOffering = getCourse(hibSession, courseName);
				if (courseOffering == null) throw new CurriculaException(MESSAGES.errorCourseDoesNotExist(courseName));
				
				results = loadCurriculaForACourse(hibSession, academicClassifications, academicAreas, courseOffering);
			} finally {
				hibSession.close();
			}
			sLog.debug("Found " + (results == null ? 0 : results.size()) + " curricula (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public TreeSet<CurriculumInterface> findCurriculaForAnInstructionalOffering(Long offeringId) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("findCurriculaForAnOffering(offeringId='" + offeringId + "')");
			Long s0 = System.currentTimeMillis();
			
			TreeSet<AcademicClassificationInterface> academicClassifications = loadAcademicClassifications();
			TreeSet<AcademicAreaInterface> academicAreas = loadAcademicAreas();

			TreeSet<CurriculumInterface> results = null;
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(offeringId, hibSession);
				if (offering == null) throw new CurriculaException(MESSAGES.errorOfferingDoesNotExist(offeringId == null ? "null" : offeringId.toString()));
				
				for (Iterator<CourseOffering> i = offering.getCourseOfferings().iterator(); i.hasNext(); ) {
					CourseOffering courseOffering = i.next();
					if (results == null) {
						results = loadCurriculaForACourse(hibSession, academicClassifications, academicAreas, courseOffering);
					} else {
						TreeSet<CurriculumInterface> curricula = loadCurriculaForACourse(hibSession, academicClassifications, academicAreas, courseOffering);
						curricula: for (CurriculumInterface curriculum: curricula) {
							for (CurriculumInterface result: results) {
								if (ToolBox.equals(curriculum.getId(), result.getId()) && ToolBox.equals(curriculum.getAcademicArea().getId(), result.getAcademicArea().getId())) {
									if (curriculum.hasClassifications())
										for (CurriculumClassificationInterface cc: curriculum.getClassifications()) result.addClassification(cc);
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
			sLog.debug("Found " + results.size() + " curricula (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public TreeSet<AcademicAreaInterface> loadAcademicAreas() throws CurriculaException, PageAccessException {
		try {
			sLog.debug("loadAcademicAreas()");
			Long s0 = System.currentTimeMillis();
			TreeSet<AcademicAreaInterface> results = new TreeSet<AcademicAreaInterface>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			try {
				List<AcademicArea> areas = hibSession.createQuery(
						"select a from AcademicArea a where a.session.uniqueId = :sessionId order by a.academicAreaAbbreviation, a.title")
						.setLong("sessionId", sessionId).setCacheable(true).list();
				for (AcademicArea a: areas) {
					AcademicAreaInterface ai = new AcademicAreaInterface();
					ai.setId(a.getUniqueId());
					ai.setAbbv(a.getAcademicAreaAbbreviation());
					ai.setName(Constants.curriculaToInitialCase(a.getTitle()));
					results.add(ai);
				}
			} finally {
				hibSession.close();
			}
			sLog.debug("Loaded " + results.size() + " academic areas (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public TreeSet<MajorInterface> loadMajors(Long curriculumId, Long academicAreaId) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("loadMajors(academicAreaId=" + academicAreaId + ")");
			Long s0 = System.currentTimeMillis();
			TreeSet<MajorInterface> results = new TreeSet<MajorInterface>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
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
				if (majors.isEmpty()) return null; // Special case: academic area has no majors
				majors.removeAll(
						hibSession.createQuery("select m from Curriculum c inner join c.majors m where c.academicArea = :academicAreaId and c.uniqueId != :curriculumId")
						.setLong("academicAreaId", academicAreaId).setLong("curriculumId", (curriculumId == null ? -1l : curriculumId)).setCacheable(true).list());
				for (PosMajor m: majors) {
					MajorInterface mi = new MajorInterface();
					mi.setId(m.getUniqueId());
					mi.setCode(m.getCode());
					mi.setName(Constants.curriculaToInitialCase(m.getName()));
					results.add(mi);
				}
			} finally {
				hibSession.close();
			}
			sLog.debug("Loaded " + results.size() + " majors (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public TreeSet<DepartmentInterface> loadDepartments() throws CurriculaException, PageAccessException {
		try {
			sLog.debug("loadDepartments()");
			Long s0 = System.currentTimeMillis();
			TreeSet<DepartmentInterface> results = new TreeSet<DepartmentInterface>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				for (Department d: Department.getUserDepartments(getSessionContext().getUser())) {
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
			sLog.debug("Loaded " + results.size() + " departments (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}

	@PreAuthorize("checkPermission('CurriculumView')")
	public String lastCurriculaFilter() throws CurriculaException, PageAccessException {
		sLog.debug("lastCurriculaFilter()");
		Long s0 = System.currentTimeMillis();
		String filter = (String)getSessionContext().getAttribute("Curricula.LastFilter");
		if (filter == null)
			filter = "department:Managed";
		sLog.debug("Last filter is '" + filter + "'  (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
		return filter;
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(String query, Integer limit) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("listCourseOfferings(query='" + query + "', limit=" + limit + ")");
			Long s0 = System.currentTimeMillis();
			ArrayList<ClassAssignmentInterface.CourseAssignment> results = new ArrayList<ClassAssignmentInterface.CourseAssignment>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			try {
				for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
						"select c from CourseOffering c where " +
						"c.subjectArea.session.uniqueId = :sessionId and (" +
						"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' or lower(c.courseNbr) like :q || '%' " +
						(query.length()>2 ? "or lower(c.title) like '%' || :q || '%'" : "") + ") " +
						"order by case " +
						"when lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' then 0 else 1 end," + // matches on course name first
						"c.subjectArea.subjectAreaAbbreviation, c.courseNbr")
						.setString("q", query.toLowerCase())
						.setLong("sessionId", sessionId)
						.setCacheable(true).setMaxResults(limit == null || limit < 0 ? Integer.MAX_VALUE : limit).list()) {
					CourseAssignment course = new CourseAssignment();
					course.setCourseId(c.getUniqueId());
					course.setSubject(c.getSubjectAreaAbbv());
					course.setCourseNbr(c.getCourseNbr());
					course.setNote(c.getScheduleBookNote());
					course.setTitle(c.getTitle());
					if (c.getCredit() != null) {
						course.setCreditText(c.getCredit().creditText());
						course.setCreditAbbv(c.getCredit().creditAbbv());
					}
					course.setHasUniqueName(true);
					boolean unlimited = false;
					int courseLimit = 0;
					for (Iterator<InstrOfferingConfig> i = c.getInstructionalOffering().getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
						InstrOfferingConfig cfg = i.next();
						if (cfg.isUnlimitedEnrollment()) unlimited = true;
						if (cfg.getLimit() != null) courseLimit += cfg.getLimit();
					}
					if (c.getReservation() != null)
						courseLimit = c.getReservation();
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
			sLog.debug("Found " + results.size() + " course offerings  (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public String retrieveCourseDetails(String course) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("retrieveCourseDetails(course='" + course + "')");
			Long s0 = System.currentTimeMillis();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				CourseOffering courseOffering = getCourse(hibSession, course);
				if (courseOffering == null) throw new CurriculaException(MESSAGES.errorCourseDoesNotExist(course));
				String details = getCourseDetailsProvider().getDetails(
						new AcademicSessionInfo(courseOffering.getSubjectArea().getSession()),
						courseOffering.getSubjectAreaAbbv(), courseOffering.getCourseNbr());
				sLog.debug("Details of length " + details.length() + " retrieved (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
				return details;
			} finally {
				hibSession.close();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public Collection<ClassAssignmentInterface.ClassAssignment> listClasses(String course) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("listClasses(course='" + course + "')");
			Long s0 = System.currentTimeMillis();
			ArrayList<ClassAssignmentInterface.ClassAssignment> results = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			NameFormat nameFormat = NameFormat.fromReference(UserProperty.NameFormat.get(sessionContext.getUser()));
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
				if (courseOffering == null) throw new CurriculaException(MESSAGES.errorCourseDoesNotExist(course));
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
					a.setClassNumber(clazz.getSectionNumberString(hibSession));
					a.addNote(clazz.getSchedulePrintNote());
					
					Assignment ass = clazz.getCommittedAssignment();
					Placement p = (ass == null ? null : ass.getPlacement());
					
                    int minLimit = clazz.getExpectedCapacity();
                	int maxLimit = clazz.getMaxExpectedCapacity();
                	int limit = maxLimit;
                	if (minLimit < maxLimit && p != null) {
                		int roomLimit = (int) Math.floor(p.getRoomSize() / (clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()));
                		// int roomLimit = Math.round((clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()) * p.getRoomSize());
                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
                	}
                    if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = -1;
					a.setLimit(new int[] {clazz.getEnrollment(), limit});
					
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
							a.addInstructor(nameFormat.format(instr.getInstructor()));
							a.addInstructoEmail(instr.getInstructor().getEmail());
						}
					}
					if (clazz.getParentClass() != null)
						a.setParentSection(clazz.getParentClass().getClassSuffix(courseOffering));
					a.setSubpartId(clazz.getSchedulingSubpart().getUniqueId());
					if (a.getParentSection() == null)
						a.setParentSection(courseOffering.getConsentType() == null ? null : courseOffering.getConsentType().getLabel());
					results.add(a);
				}
			} finally {
				hibSession.close();
			}
			sLog.debug("Found " + results.size() + " classes (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public String[] getApplicationProperty(String[] name) throws CurriculaException, PageAccessException {
		String[] ret = new String[name.length];
		for (int i = 0; i < name.length; i++)
			ret[i] = ApplicationProperties.getProperty(name[i]);
		return ret;
	}
	
	@PreAuthorize("checkPermission('CurriculumAdd') and checkPermission('CurriculumView')")
	public Boolean canAddCurriculum() throws CurriculaException, PageAccessException {
		return true;
	}
	
	@PreAuthorize("checkPermission('CurriculumAdmin') and checkPermission('CurriculumView')")
	public Boolean isAdmin() throws CurriculaException, PageAccessException {
		return true;
	}

	@PreAuthorize("checkPermission('CurriculumProjectionRulesDetail')")
	public HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> loadProjectionRules() throws CurriculaException, PageAccessException {
		sLog.debug("loadProjectionRules()");
		Long s0 = System.currentTimeMillis();
		try {
			UserContext user = getSessionContext().getUser();
			if (user == null) throw new PageAccessException(
					getSessionContext().isHttpSessionNew() ? MESSAGES.authenticationExpired() : MESSAGES.authenticationRequired());

			HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> rules = new HashMap<AcademicAreaInterface, HashMap<MajorInterface,HashMap<AcademicClassificationInterface, Number[]>>>();
			
			TreeSet<AcademicAreaInterface> areas = loadAcademicAreas();
			
			TreeSet<AcademicClassificationInterface> classifications = loadAcademicClassifications();
			
			Long sessionId = getAcademicSessionId();

			MajorInterface dummyMajor = new MajorInterface();
			dummyMajor.setId(-1l);
			dummyMajor.setCode("");
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> areaMajorClasf2ll = loadAreaMajorClasf2ll(hibSession);
				
				Hashtable <Long, MajorInterface> majorLookup = new Hashtable<Long, MajorInterface>();
				Hashtable<Long, List<MajorInterface>> majors = new Hashtable<Long, List<MajorInterface>>();
				for (PosMajor major: (List<PosMajor>)hibSession.createQuery(
						"select m from PosMajor m where m.session.uniqueId=:sessionId").
						setLong("sessionId", sessionId).setCacheable(true).list()) {
					MajorInterface mi = new MajorInterface();
					mi.setId(major.getUniqueId());
					mi.setCode(major.getCode());
					mi.setName(Constants.curriculaToInitialCase(major.getName()));
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
			sLog.debug("Curriculum projection rules loaded (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return rules;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumProjectionRulesEdit')")
	public Boolean saveProjectionRules(HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> rules) throws CurriculaException, PageAccessException {
		sLog.debug("saveProjectionRules()");
		long s0 = System.currentTimeMillis();
		try {
			if (!canEditProjectionRules())
				throw new CurriculaException(MESSAGES.authenticationInsufficient());

			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				for (CurriculumProjectionRule rule: (List<CurriculumProjectionRule>)hibSession.createQuery(
						"select r from CurriculumProjectionRule r where academicArea.session.uniqueId=:sessionId")
						.setLong("sessionId", sessionId).setCacheable(true).list()) {
					
					ChangeLog.addChange(hibSession,
							getSessionContext(),
							rule,
							rule.getAcademicArea().getAcademicAreaAbbreviation() + (rule.getMajor() == null ? "" : "/" + rule.getMajor().getCode()) + " " + rule.getAcademicClassification().getCode() + ": " + sDF.format(100.0 * rule.getProjection()) + "%",
							Source.CUR_PROJ_RULES, 
							Operation.DELETE,
							null,
							null);

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
							
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									r,
									area.getAcademicAreaAbbreviation() + (major == null ? "" : "/" + major.getCode()) + " " + clasf.getCode() + ": " +
									sDF.format(100.0 * r.getProjection()) + "%",
									Source.CUR_PROJ_RULES, 
									Operation.CREATE,
									null,
									null);
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
			sLog.debug("Curriculum projection rules saved (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumProjectionRulesEdit')")
	public Boolean canEditProjectionRules() throws CurriculaException, PageAccessException {
		return true;
	}
	
	@PreAuthorize("checkPermission('CurriculumAdmin')")
	public Boolean makeupCurriculaFromLastLikeDemands(boolean lastLike) throws CurriculaException, PageAccessException {
		sLog.debug("makeupCurriculaFromLastLikeDemands(lastLike=" + lastLike + ")");
		long s0 = System.currentTimeMillis();
		try {
			if (!isAdmin())
				throw new CurriculaException(MESSAGES.authenticationInsufficient());
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				for (Curriculum c: (List<Curriculum>)hibSession.createQuery("from Curriculum where department.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list()) {
					ChangeLog.addChange(hibSession,
							getSessionContext(),
							c,
							c.getAbbv(),
							Source.CURRICULA, 
							Operation.DELETE,
							null,
							c.getDepartment());

				}

				MakeCurriculaFromLastlikeDemands m = new MakeCurriculaFromLastlikeDemands(sessionId);
				m.update(hibSession, lastLike);
				
				for (Curriculum c: (List<Curriculum>)hibSession.createQuery("from Curriculum where department.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list()) {
					ChangeLog.addChange(hibSession,
							getSessionContext(),
							c,
							c.getAbbv(),
							Source.CURRICULA, 
							Operation.CREATE,
							null,
							c.getDepartment());

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
			sLog.debug("Curricula recreated (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public Boolean updateCurriculaByProjections(Set<Long> curriculumIds, boolean updateCurriculumCourses) throws CurriculaException, PageAccessException {
		sLog.debug("updateCurriculaByProjections(curricula=" + curriculumIds + ", updateCurriculumCourses=" + updateCurriculumCourses + ")");
		long s0 = System.currentTimeMillis();
		try {
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();

				List<Curriculum> curricula = null;
				if (curriculumIds == null) {
					curricula = findAllCurricula(hibSession);
				} else {
					curricula = new ArrayList<Curriculum>();
					for (Long id: curriculumIds)
						curricula.add(CurriculumDAO.getInstance().get(id, hibSession));
				}
				
				for (Curriculum c: curricula) {
					if (c == null || !getSessionContext().hasPermission(c, Right.CurriculumEdit)) continue;
					
					List<AcademicClassification> classifications = (List<AcademicClassification>)hibSession.createQuery(
							"select c from AcademicClassification c where c.session.uniqueId = :sessionId")
							.setLong("sessionId", sessionId).setCacheable(true).list();
					
					Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, c.getAcademicArea().getUniqueId());
					
					Hashtable<String, Hashtable<String, Integer>> clasfMajor2ll = loadClasfMajor2ll(hibSession, c);
					
					int totalProjection = 0;
					for (AcademicClassification acadClasf: classifications) {

						float proj = 0.0f;
						Hashtable<String, Integer> major2ll = clasfMajor2ll.get(acadClasf.getCode());

						if (major2ll != null) {
							for (Map.Entry<String, Integer> entry: major2ll.entrySet()) {
								proj += getProjection(rules, entry.getKey(), acadClasf.getCode()) * entry.getValue();
							}
						}
						
						CurriculumClassification clasf = null;
						for (CurriculumClassification f: c.getClassifications()) {
							if (f.getAcademicClassification().equals(acadClasf)) { clasf = f; break; }
						}
						
						if (clasf == null && Math.round(proj) <= 0) continue;
						
						if (clasf == null) {
							clasf = new CurriculumClassification();
							clasf.setAcademicClassification(acadClasf);
							clasf.setCourses(new HashSet<CurriculumCourse>());
							clasf.setCurriculum(c);
							c.getClassifications().add(clasf);
							clasf.setName(acadClasf.getCode());
							clasf.setOrd(c.getClassifications().size());
						}
						
						clasf.setNrStudents(Math.round(proj));
						totalProjection += Math.round(proj);
						
						hibSession.saveOrUpdate(clasf);
					}
					
					if (updateCurriculumCourses) {
						
						float totalShareLimit = ApplicationProperty.CurriculumLastLikeDemandsTotalShareLimit.floatValue();
						float shareLimit = ApplicationProperty.CurriculumLastLikeDemandsShareLimit.floatValue();
						int enrollmentLimit = ApplicationProperty.CurriculumLastLikeDemandsEnrollmentLimit.intValue();

						Hashtable<String, Hashtable<String, Hashtable<Long, Integer>>> clasfMajorCourse2ll = loadClasfMajorCourse2ll(hibSession, c);
						
						Hashtable<Long, Float> courseTotals = new Hashtable<Long, Float>();
						for (Map.Entry<String, Hashtable<String, Hashtable<Long, Integer>>> clasf: clasfMajorCourse2ll.entrySet()) {
							for (Map.Entry<String, Hashtable<Long, Integer>> major: clasf.getValue().entrySet()) {
								for (Map.Entry<Long, Integer> course: major.getValue().entrySet()) {
									Float total = courseTotals.get(course.getKey());
									courseTotals.put(course.getKey(), getProjection(rules, major.getKey(), clasf.getKey()) * course.getValue() + (total == null ? 0.0f: total));
								}
							}
						}
						
						for (CurriculumClassification clasf: c.getClassifications()) {
							Hashtable<String, Hashtable<Long, Integer>> majorCourse2ll = clasfMajorCourse2ll.get(clasf.getAcademicClassification().getCode());
							
							if (majorCourse2ll == null || clasf.getNrStudents() == 0) {
								for (CurriculumCourse course: clasf.getCourses()) {
									course.setPercShare(0.0f);
									hibSession.saveOrUpdate(course);
								}
								continue;
							}
							
							HashSet<Long> remainingCourses = new HashSet<Long>();
							for (Hashtable<Long, Integer> x: majorCourse2ll.values())
								remainingCourses.addAll(x.keySet());
							
							for (CurriculumCourse course: clasf.getCourses()) {
								float proj = 0.0f;
								for (Map.Entry<String, Hashtable<Long, Integer>> entry: majorCourse2ll.entrySet()) {
									Integer lastLike = entry.getValue().get(course.getCourse().getUniqueId());
									proj += getProjection(rules, entry.getKey(), clasf.getAcademicClassification().getCode()) * (lastLike == null ? 0 : lastLike);
								}
								course.setPercShare(proj / clasf.getNrStudents());
								remainingCourses.remove(course.getCourse().getUniqueId());
								
								hibSession.saveOrUpdate(course);
							}
							
							for (Long courseId: remainingCourses) {
								Float courseTotal = courseTotals.get(courseId);
								float totalShare = (courseTotal == null ? 0.0f : courseTotal) / totalProjection;
								
								if (totalShare < totalShareLimit) continue;
								
								float proj = 0.0f;
								for (Map.Entry<String, Hashtable<Long, Integer>> entry: majorCourse2ll.entrySet()) {
									Integer lastLike = entry.getValue().get(courseId);
									proj += getProjection(rules, entry.getKey(), clasf.getAcademicClassification().getCode()) * (lastLike == null ? 0 : lastLike);
								}
								float share = proj / clasf.getNrStudents();
								
								if (share <= 0.0f || Math.round(proj) < enrollmentLimit || share < shareLimit) continue;
								
								CurriculumCourse course = new CurriculumCourse();
								course.setClassification(clasf);
								clasf.getCourses().add(course);
								course.setOrd(clasf.getCourses().size());
								course.setCourse(CourseOfferingDAO.getInstance().get(courseId, hibSession));
								course.setPercShare(share);
								
								hibSession.saveOrUpdate(course);
							}
						}
					}
					
					ChangeLog.addChange(hibSession,
							getSessionContext(),
							c,
							c.getAbbv(),
							Source.CURRICULA, 
							Operation.UPDATE,
							null,
							c.getDepartment());

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
			sLog.debug("Curricula update (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission('CurriculumAdmin')")
	public Boolean populateCourseProjectedDemands(boolean includeOtherStudents) throws CurriculaException, PageAccessException {
		sLog.debug("populateCourseProjectedDemands(includeOtherStudents=" + includeOtherStudents + ")");
		long s0 = System.currentTimeMillis();
		try {
			if (!isAdmin())
				throw new CurriculaException(MESSAGES.authenticationInsufficient());
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				Long sessionId = getAcademicSessionId();
				
				Hashtable<Long, Hashtable<String, Hashtable<String, Hashtable<String, Integer>>>> course2area2major2clasf2ll = null;
				if (includeOtherStudents) {
					course2area2major2clasf2ll = loadCourseAreaMajorClasf2ll(hibSession);
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
					
					Integer oldDemand = courseOffering.getDemand();

					Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> area2major2clasf2ll = (course2area2major2clasf2ll == null ? null : course2area2major2clasf2ll.get(courseOffering.getUniqueId()));
					List<CurriculumCourse> curricula = course2curriculum.get(courseOffering.getUniqueId());
					
					int demand = 0;
					if (curricula != null)
						for (CurriculumCourse curriculum: curricula) {
							demand += Math.round(curriculum.getPercShare() * curriculum.getClassification().getNrStudents());
							if (area2major2clasf2ll != null) {
								String areaAbbv = curriculum.getClassification().getCurriculum().getAcademicArea().getAcademicAreaAbbreviation();
								Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(areaAbbv);
								if (major2clasf2ll != null) {
									if (curriculum.getClassification().getCurriculum().getMajors().isEmpty()) {
										area2major2clasf2ll.remove(areaAbbv);
										/*
										for (Hashtable<String, Integer> clasf2ll: major2clasf2ll.values()) {
											clasf2ll.remove(curriculum.getClassification().getAcademicClassification().getCode());
										}
										*/
									} else {
										for (PosMajor major: (Collection<PosMajor>)curriculum.getClassification().getCurriculum().getMajors()) {
											major2clasf2ll.remove(major.getCode());
											/*
											Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(major.getCode());
											if (clasf2ll != null)
												clasf2ll.remove(curriculum.getClassification().getAcademicClassification().getCode());
											*/
										}
									}
								}
							}
						}
					
					if (area2major2clasf2ll != null) {
						for (Map.Entry<String, Hashtable<String, Hashtable<String, Integer>>> areaEmajor2clasf2ll: area2major2clasf2ll.entrySet()) {
							for (Map.Entry<String, Hashtable<String, Integer>> majorEclasf2ll: areaEmajor2clasf2ll.getValue().entrySet()) {
								for (Map.Entry<String, Integer> clasfEll: majorEclasf2ll.getValue().entrySet()) {
									demand += Math.round(getProjection(rules == null ? null : rules.get(areaEmajor2clasf2ll.getKey()), majorEclasf2ll.getKey(), clasfEll.getKey()) * clasfEll.getValue());
								}
							}
						}
					}
					
					courseOffering.setProjectedDemand(demand);
					
					if (oldDemand == null || demand != oldDemand) {
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								courseOffering,
								courseOffering.getCourseName() + " projection: " + oldDemand + " &rarr; " + demand,
								Source.CURRICULA, 
								Operation.UPDATE,
								courseOffering.getSubjectArea(),
								courseOffering.getSubjectArea().getDepartment());
					}
					
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
			sLog.debug("Course projected demands updated (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	@PreAuthorize("checkPermission(#offeringId, 'InstructionalOffering', 'InstructionalOfferingDetail') or checkPermission('CurriculumAdmin')")
	public Boolean populateCourseProjectedDemands(boolean includeOtherStudents, Long offeringId) throws CurriculaException, PageAccessException {
		sLog.debug("populateCourseProjectedDemands(includeOtherStudents=" + includeOtherStudents + ", offering=" + offeringId +")");
		long s0 = System.currentTimeMillis();
		try {
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(offeringId, hibSession);
				if (offering == null) throw new CurriculaException(MESSAGES.errorOfferingDoesNotExist(offeringId == null ? "null" : offeringId.toString()));
				
				for (Iterator<CourseOffering> i = offering.getCourseOfferings().iterator(); i.hasNext(); ) {
					CourseOffering courseOffering = i.next();
					
					Integer oldDemand = courseOffering.getDemand();
					
					Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> area2major2clasf2ll =  null;
					if (includeOtherStudents) {
						area2major2clasf2ll = loadAreaMajorClasf2ll(hibSession, courseOffering.getUniqueId());
					}
					
					Hashtable<String, Hashtable<String, HashMap<String, Float>>> rules = (includeOtherStudents ? getRules(hibSession) : null);
					
					int demand = 0;
					for (CurriculumCourse curriculum: (List<CurriculumCourse>)hibSession.createQuery(
							"select cc from CurriculumCourse cc where cc.course.uniqueId = :courseId")
							.setLong("courseId", courseOffering.getUniqueId())
							.setCacheable(true).list()) {
						demand += Math.round(curriculum.getPercShare() * curriculum.getClassification().getNrStudents());
						if (area2major2clasf2ll != null) {
							String areaAbbv = curriculum.getClassification().getCurriculum().getAcademicArea().getAcademicAreaAbbreviation();
							Hashtable<String, Hashtable<String, Integer>> major2clasf2ll = area2major2clasf2ll.get(areaAbbv);
							if (major2clasf2ll != null) {
								if (curriculum.getClassification().getCurriculum().getMajors().isEmpty()) {
									area2major2clasf2ll.remove(areaAbbv);
									/*
									for (Hashtable<String, Integer> clasf2ll: major2clasf2ll.values()) {
										clasf2ll.remove(curriculum.getClassification().getAcademicClassification().getCode());
									}
									*/
								} else {
									for (PosMajor major: (Collection<PosMajor>)curriculum.getClassification().getCurriculum().getMajors()) {
										major2clasf2ll.remove(major.getCode());
										/*
										Hashtable<String, Integer> clasf2ll = major2clasf2ll.get(major.getCode());
										if (clasf2ll != null)
											clasf2ll.remove(curriculum.getClassification().getAcademicClassification().getCode());
										*/
									}
								}
							}
						}
					}
						
						
					if (area2major2clasf2ll != null) {
						for (Map.Entry<String, Hashtable<String, Hashtable<String, Integer>>> areaEmajor2clasf2ll: area2major2clasf2ll.entrySet()) {
							for (Map.Entry<String, Hashtable<String, Integer>> majorEclasf2ll: areaEmajor2clasf2ll.getValue().entrySet()) {
								for (Map.Entry<String, Integer> clasfEll: majorEclasf2ll.getValue().entrySet()) {
									demand += Math.round(getProjection(rules == null ? null : rules.get(areaEmajor2clasf2ll.getKey()), majorEclasf2ll.getKey(), clasfEll.getKey()) * clasfEll.getValue());
								}
							}
						}
					}
					
					courseOffering.setProjectedDemand(demand);
					
					if (oldDemand == null || demand != oldDemand) {
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								courseOffering,
								courseOffering.getCourseName() + " projection: " + oldDemand + " &rarr; " + demand,
								Source.CURRICULA, 
								Operation.UPDATE,
								courseOffering.getSubjectArea(),
								courseOffering.getSubjectArea().getDepartment());
					}
					
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
			sLog.debug("Course projected demands updated (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return null;
		} catch (PageAccessException e) {
			throw e;
		} catch (CurriculaException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}

	/* Support functions (lookups etc.) */
	private Long getAcademicSessionId() {
		return getSessionContext().getUser().getCurrentAcademicSessionId();
	}
	
	private List<Curriculum> findAllCurricula(org.hibernate.Session hibSession) {
		return hibSession.createQuery(
				"select distinct c from Curriculum c where c.department.session.uniqueId = :sessionId")
				.setLong("sessionId", getAcademicSessionId())
				.setCacheable(true).list();
	}
	
	private Hashtable<Long, Integer> loadClasf2enrl(org.hibernate.Session hibSession, Curriculum c) {
		String majorIds = "";
		for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
			PosMajor major = i.next();
			if (!majorIds.isEmpty()) majorIds += ",";
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
				.setLong("sessionId", c.getAcademicArea().getSessionId())
				.setLong("areaId", c.getAcademicArea().getUniqueId())
				.setCacheable(true).list()) {
			Long clasfId = (Long)o[0];
			int enrl = ((Number)o[1]).intValue();
			clasf2enrl.put(clasfId, enrl);
		}
		return clasf2enrl;
	}
	
	private Hashtable<Long, Set<Long>> loadClasf2enrl(org.hibernate.Session hibSession, Long acadAreaId, Collection<Long> majors) {
		String majorIds = "";
		for (Long majorId: majors) {
			if (!majorIds.isEmpty()) majorIds += ",";
			majorIds += majorId;
		}

		Hashtable<Long, Set<Long>> clasf2enrl = new Hashtable<Long, Set<Long>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicClassification.uniqueId, e.student.uniqueId from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " + 
				(majorIds.isEmpty() ? "" : " inner join e.student.posMajors m ") + "where " +
				"e.student.session.uniqueId = :sessionId and "+
				"a.academicArea.uniqueId = :areaId " + 
				(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") "))
				.setLong("sessionId", getAcademicSessionId())
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
		return clasf2enrl;
	}
	
	private Hashtable<String, Hashtable<String, Integer>> loadClasfMajor2ll(org.hibernate.Session hibSession, Curriculum c) {
		String majorCodes = "";
		for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
			PosMajor major = i.next();
			if (!majorCodes.isEmpty()) majorCodes += ",";
			majorCodes += "'" + major.getCode() + "'";
		}
		
		Hashtable<String, Hashtable<String, Integer>> clasfMajor2ll = new Hashtable<String, Hashtable<String,Integer>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select f.code, m.code, count(distinct s) from LastLikeCourseDemand x inner join x.student s " +
				"inner join s.academicAreaClassifications a inner join a.academicClassification f inner join s.posMajors m where " +
				"x.subjectArea.session.uniqueId = :sessionId and "+
				"a.academicArea.academicAreaAbbreviation = :acadAbbv " + 
				(majorCodes.isEmpty() ? "" : "and m.code in (" + majorCodes + ") ") +
				"group by f.code, m.code")
				.setLong("sessionId", c.getAcademicArea().getSessionId())
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
		return clasfMajor2ll;
	}
	
	private Hashtable<String, Integer> loadClasf2ll(org.hibernate.Session hibSession, Curriculum c) {
		String majorCodes = "";
		for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
			PosMajor major = i.next();
			if (!majorCodes.isEmpty()) majorCodes += ",";
			majorCodes += "'" + major.getCode() + "'";
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
				.setLong("sessionId", c.getAcademicArea().getSessionId())
				.setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation())
				.setCacheable(true).list()) {
			String clasfCode = (String)o[0];
			int enrl = ((Number)o[1]).intValue();
			clasf2ll.put(clasfCode, enrl);
		}
		return clasf2ll;
	}
	
	private Hashtable<String, HashMap<String, Set<Long>>> loadClasfMajor2ll(org.hibernate.Session hibSession, String acadAreaAbbv, Collection<PosMajor> majors) {
		String majorCodes = "";
		for (PosMajor major: majors) {
			if (!majorCodes.isEmpty()) majorCodes += ",";
			majorCodes += "'" + major.getCode() + "'";
		}
		
		Hashtable<String, HashMap<String, Set<Long>>> clasf2ll = new Hashtable<String, HashMap<String, Set<Long>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select f.code, m.code, s.uniqueId from LastLikeCourseDemand x inner join x.student s " +
				"inner join s.academicAreaClassifications a inner join a.academicClassification f inner join s.posMajors m where " +
				"x.subjectArea.session.uniqueId = :sessionId and "+
				"a.academicArea.academicAreaAbbreviation = :acadAbbv " + 
				(majorCodes.isEmpty() ? "" : "and m.code in (" + majorCodes + ") "))
				.setLong("sessionId", getAcademicSessionId())
				.setString("acadAbbv", acadAreaAbbv)
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
		
		return clasf2ll;
	}
	
	private Hashtable<Long, Hashtable<Long, Integer>> loadClasfCourse2enrl(org.hibernate.Session hibSession, Curriculum c) {
		String majorIds = "";
		for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
			PosMajor major = i.next();
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
				.setLong("sessionId", c.getAcademicArea().getSessionId())
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
		return clasf2course2enrl;
	}
	
	private Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> loadClasfCourse2enrl(org.hibernate.Session hibSession, Long acadAreaId, Collection<Long> majors) {
		String majorIds = "";
		for (Long majorId: majors) {
			if (!majorIds.isEmpty()) majorIds += ",";
			majorIds += majorId;
		}

		Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> clasf2course2enrl = new Hashtable<Long, Hashtable<CourseInterface,Set<Long>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicClassification.uniqueId, e.courseOffering.uniqueId, e.courseOffering.subjectArea.subjectAreaAbbreviation || ' ' || e.courseOffering.courseNbr, e.student.uniqueId from StudentClassEnrollment e inner join e.student.academicAreaClassifications a " + 
				(majorIds.isEmpty() ? "" : " inner join e.student.posMajors m ") + "where " +
				"e.student.session.uniqueId = :sessionId and "+
				"a.academicArea.uniqueId = :areaId " + 
				(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") "))
				.setLong("sessionId", getAcademicSessionId())
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
		
		return clasf2course2enrl;
	}
	
	private Hashtable<String, Hashtable<String, Hashtable<Long, Integer>>> loadClasfMajorCourse2ll(org.hibernate.Session hibSession, Curriculum c) {
		String majorCodes = "";
		for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
			PosMajor major = i.next();
			if (!majorCodes.isEmpty()) majorCodes += ",";
			majorCodes += "'" + major.getCode() + "'";
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
				.setLong("sessionId", c.getAcademicArea().getSessionId())
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
			Hashtable<Long, Integer> course2enrl = major2course2ll.get(majorCode);
			if (course2enrl == null) {
				course2enrl = new Hashtable<Long, Integer>();
				major2course2ll.put(majorCode, course2enrl);
			}
			course2enrl.put(courseId, enrl);
		}
		return clasfMajor2course2ll;
	}
	
	private Hashtable<String, Hashtable<CourseInterface, HashMap<String, Set<Long>>>> loadClasfCourseMajor2ll(org.hibernate.Session hibSession, String acadAreaAbbv, Collection<PosMajor> majors) {
		String majorCodes = "";
		for (PosMajor major: majors) {
			if (!majorCodes.isEmpty()) majorCodes += ",";
			majorCodes += "'" + major.getCode() + "'";
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
				.setLong("sessionId", getAcademicSessionId())
				.setString("acadAbbv", acadAreaAbbv)
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
		
		return clasf2course2ll;
	}
	
	private Hashtable<Long, Hashtable<Long, Hashtable<Long, Integer>>> loadAreaMajorClasf2enrl(org.hibernate.Session hibSession, Long courseOfferingId) {
		Hashtable<Long, Hashtable<Long, Hashtable<Long, Integer>>> area2major2clasf2enrl = new Hashtable<Long, Hashtable<Long,Hashtable<Long,Integer>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicArea.uniqueId, m.uniqueId, a.academicClassification.uniqueId, count(distinct e.student) " +
				"from StudentClassEnrollment e inner join e.student.academicAreaClassifications a inner join e.student.posMajors m where " +
				"e.courseOffering.uniqueId = :courseId group by a.academicArea.uniqueId, m.uniqueId, a.academicClassification.uniqueId")
				.setLong("courseId", courseOfferingId)
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
		return area2major2clasf2enrl;
	}
	
	private Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> loadAreaMajorClasf2ll(org.hibernate.Session hibSession, Long courseOfferingId) {
		Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> area2major2clasf2ll = new Hashtable<String, Hashtable<String,Hashtable<String,Integer>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select r.academicAreaAbbreviation, m.code, f.code, count(distinct s) from " +
				"LastLikeCourseDemand x inner join x.student s inner join s.academicAreaClassifications a inner join s.posMajors m " +
				"inner join a.academicClassification f inner join a.academicArea r, CourseOffering co where " +
				"x.subjectArea.session.uniqueId = :sessionId and co.uniqueId = :courseId and "+
				"co.subjectArea.uniqueId = x.subjectArea.uniqueId and " +
				"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr)) " +
				"group by r.academicAreaAbbreviation, m.code, f.code")
				.setLong("sessionId", getAcademicSessionId())
				.setLong("courseId", courseOfferingId)
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
		return area2major2clasf2ll;
	}
	
	private Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> loadAreaMajorClasf2ll(org.hibernate.Session hibSession) {
		Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> area2major2clasf2ll = new Hashtable<String, Hashtable<String,Hashtable<String,Integer>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicAreaAbbreviation, m.code, f.code, count(distinct s) from LastLikeCourseDemand x inner join x.student s " +
				"inner join s.academicAreaClassifications ac inner join ac.academicClassification f inner join ac.academicArea a " +
				"inner join s.posMajors m where x.subjectArea.session.uniqueId = :sessionId " +
				"group by a.academicAreaAbbreviation, m.code, f.code")
				.setLong("sessionId", getAcademicSessionId())
				.setCacheable(true).list()) {
			String area = (String)o[0];
			String major = (String)o[1];
			String clasf = (String)o[2];
			int students = ((Number)o[3]).intValue();
			Hashtable<String, Hashtable<String, Integer>> majorClasf2ll = area2major2clasf2ll.get(area);
			if (majorClasf2ll == null) {
				majorClasf2ll = new Hashtable<String, Hashtable<String,Integer>>();
				area2major2clasf2ll.put(area, majorClasf2ll);
			}
			Hashtable<String, Integer> clasf2ll = majorClasf2ll.get(major);
			if (clasf2ll == null) {
				clasf2ll = new Hashtable<String, Integer>();
				majorClasf2ll.put(major, clasf2ll);
			}
			clasf2ll.put(clasf, students);
		}
		return area2major2clasf2ll;
	}
	
	private Hashtable<Long, Hashtable<String, Hashtable<String, Hashtable<String, Integer>>>> loadCourseAreaMajorClasf2ll(org.hibernate.Session hibSession) {
		Hashtable<Long, Hashtable<String, Hashtable<String, Hashtable<String, Integer>>>> course2area2major2clasf2ll = new Hashtable<Long, Hashtable<String, Hashtable<String,Hashtable<String,Integer>>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select co.uniqueId, r.academicAreaAbbreviation, m.code, f.code, count(distinct s) from " +
				"LastLikeCourseDemand x inner join x.student s inner join s.academicAreaClassifications a inner join s.posMajors m " +
				"inner join a.academicClassification f inner join a.academicArea r, CourseOffering co where " +
				"x.subjectArea.session.uniqueId = :sessionId and co.subjectArea.session.uniqueId = :sessionId and "+
				"co.subjectArea.uniqueId = x.subjectArea.uniqueId and " +
				"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr)) " +
				"group by co.uniqueId, r.academicAreaAbbreviation, m.code, f.code")
				.setLong("sessionId", getAcademicSessionId())
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
		return course2area2major2clasf2ll;
	}
	
	private Hashtable<Long, Integer> loadClasf2req(org.hibernate.Session hibSession, Curriculum c) {
		String majorIds = "";
		for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
			PosMajor major = i.next();
			if (!majorIds.isEmpty()) majorIds += ",";
			majorIds += major.getUniqueId();
		}
		Hashtable<Long, Integer> clasf2enrl = new Hashtable<Long, Integer>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicClassification.uniqueId, count(distinct s) from CourseRequest r inner join r.courseDemand.student s inner join s.academicAreaClassifications a " + 
				(majorIds.isEmpty() ? "" : " inner join s.posMajors m ") + "where " +
				"s.session.uniqueId = :sessionId and "+
				"a.academicArea.uniqueId = :areaId " + 
				(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
				"group by a.academicClassification.uniqueId")
				.setLong("sessionId", c.getAcademicArea().getSessionId())
				.setLong("areaId", c.getAcademicArea().getUniqueId())
				.setCacheable(true).list()) {
			Long clasfId = (Long)o[0];
			int enrl = ((Number)o[1]).intValue();
			clasf2enrl.put(clasfId, enrl);
		}
		return clasf2enrl;
	}
	
	private Hashtable<Long, Set<Long>> loadClasf2req(org.hibernate.Session hibSession, Long acadAreaId, Collection<Long> majors) {
		String majorIds = "";
		for (Long majorId: majors) {
			if (!majorIds.isEmpty()) majorIds += ",";
			majorIds += majorId;
		}

		Hashtable<Long, Set<Long>> clasf2enrl = new Hashtable<Long, Set<Long>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicClassification.uniqueId, s.uniqueId from CourseRequest r inner join r.courseDemand.student s inner join s.academicAreaClassifications a " + 
				(majorIds.isEmpty() ? "" : " inner join s.posMajors m ") + "where " +
				"s.session.uniqueId = :sessionId and "+
				"a.academicArea.uniqueId = :areaId " + 
				(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") "))
				.setLong("sessionId", getAcademicSessionId())
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
		return clasf2enrl;
	}
	
	private Hashtable<Long, Hashtable<Long, Integer>> loadClasfCourse2req(org.hibernate.Session hibSession, Curriculum c) {
		String majorIds = "";
		for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
			PosMajor major = i.next();
			if (!majorIds.isEmpty()) majorIds += ",";
			majorIds += major.getUniqueId();
		}

		Hashtable<Long, Hashtable<Long, Integer>> clasf2course2enrl = new Hashtable<Long, Hashtable<Long,Integer>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicClassification.uniqueId, r.courseOffering.uniqueId, count(distinct s) from CourseRequest r inner join r.courseDemand.student s inner join s.academicAreaClassifications a " + 
				(majorIds.isEmpty() ? "" : " inner join s.posMajors m ") + "where " +
				"s.session.uniqueId = :sessionId and "+
				"a.academicArea.uniqueId = :areaId " + 
				(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") ") +
				"group by a.academicClassification.uniqueId, r.courseOffering.uniqueId")
				.setLong("sessionId", c.getAcademicArea().getSessionId())
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
		return clasf2course2enrl;
	}
	
	private Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> loadClasfCourse2req(org.hibernate.Session hibSession, Long acadAreaId, Collection<Long> majors) {
		String majorIds = "";
		for (Long majorId: majors) {
			if (!majorIds.isEmpty()) majorIds += ",";
			majorIds += majorId;
		}

		Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> clasf2course2enrl = new Hashtable<Long, Hashtable<CourseInterface,Set<Long>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicClassification.uniqueId, r.courseOffering.uniqueId, r.courseOffering.subjectArea.subjectAreaAbbreviation || ' ' || r.courseOffering.courseNbr, s.uniqueId from CourseRequest r inner join r.courseDemand.student s inner join s.academicAreaClassifications a " + 
				(majorIds.isEmpty() ? "" : " inner join s.posMajors m ") + "where " +
				"s.session.uniqueId = :sessionId and "+
				"a.academicArea.uniqueId = :areaId " + 
				(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ") "))
				.setLong("sessionId", getAcademicSessionId())
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
		
		return clasf2course2enrl;
	}
	
	private Hashtable<Long, Hashtable<Long, Hashtable<Long, Integer>>> loadAreaMajorClasf2req(org.hibernate.Session hibSession, Long courseOfferingId) {
		Hashtable<Long, Hashtable<Long, Hashtable<Long, Integer>>> area2major2clasf2enrl = new Hashtable<Long, Hashtable<Long,Hashtable<Long,Integer>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicArea.uniqueId, m.uniqueId, a.academicClassification.uniqueId, count(distinct s) " +
				"from CourseRequest r inner join r.courseDemand.student s inner join s.academicAreaClassifications a inner join s.posMajors m where " +
				"r.courseOffering.uniqueId = :courseId group by a.academicArea.uniqueId, m.uniqueId, a.academicClassification.uniqueId")
				.setLong("courseId", courseOfferingId)
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
		return area2major2clasf2enrl;
	}
	
	private CourseOffering getCourse(org.hibernate.Session hibSession, String courseName) {
		for (CourseOffering co: (List<CourseOffering>)hibSession.createQuery(
				"select c from CourseOffering c where " +
				"c.subjectArea.session.uniqueId = :sessionId and " +
				"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course")
				.setString("course", courseName.toLowerCase())
				.setLong("sessionId", getAcademicSessionId())
				.setCacheable(true).setMaxResults(1).list()) {
			return co;
		}
		return null;
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

}
