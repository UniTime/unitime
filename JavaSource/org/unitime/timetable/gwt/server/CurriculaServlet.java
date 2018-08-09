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
import org.unitime.timetable.model.DepartmentStatusType;
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
import org.unitime.timetable.security.permissions.Permission.PermissionDepartment;
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
	protected SessionContext getSessionContext() { return sessionContext; }
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public TreeSet<CurriculumInterface> findCurricula(CurriculumInterface.CurriculumFilterRpcRequest filter) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("findCurricula(filter='" + filter+"')");
			Long s0 = System.currentTimeMillis();
			TreeSet<CurriculumInterface> results = new TreeSet<CurriculumInterface>();
			getSessionContext().setAttribute("Curricula.LastFilter", filter.toQueryString());
			boolean hasSnapshotData = hasSnapshotData(CurriculumDAO.getInstance().getSession(), getAcademicSessionId());
			for (Curriculum c: CurriculumFilterBackend.curricula(getSessionContext().getUser().getCurrentAcademicSessionId(), filter.getOptions(), new Query(filter.getText()), -1, null, Department.getUserDepartments(getSessionContext().getUser()))) {
				CurriculumInterface ci = new CurriculumInterface();
				ci.setId(c.getUniqueId());
				ci.setAbbv(c.getAbbv());
				ci.setName(c.getName());
				ci.setEditable(getSessionContext().hasPermission(c, Right.CurriculumEdit));
				ci.setMultipleMajors(c.isMultipleMajors());
				ci.setSessionHasSnapshotData(hasSnapshotData);
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
			boolean hasSnapshotData = hasSnapshotData(hibSession, getAcademicSessionId());;
			try {
				for (Long curriculumId: curriculumIds) {
					Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
					if (c == null) throw new CurriculaException(MESSAGES.errorCurriculumDoesNotExist(curriculumId == null ? "null" : curriculumId.toString()));
					
					Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, c.getAcademicArea().getUniqueId());
					Hashtable<String,HashMap<String, Float>> snapshotRules = null;
					if (hasSnapshotData) {
						snapshotRules = getSnapshotRules(hibSession, c.getAcademicArea().getUniqueId());
					}
					
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
						float snapshotProj = 0;
						Hashtable<String, Integer> major2ll = clasfMajor2ll.get(clasf.getAcademicClassification().getCode());
						if (major2ll != null) {
							if (c.isMultipleMajors() && c.getMajors().size() > 1) {
								double rule = 1.0f;
								double snapshotRule = 1.0f;
								for (PosMajor m: c.getMajors()) {
									rule *= getProjection(rules, m.getCode(), clasf.getAcademicClassification().getCode());
									if(hasSnapshotData){
										snapshotRule *= getSnapshotProjection(snapshotRules, m.getCode(), clasf.getAcademicClassification().getCode());
									}
								}
								for (Integer ll: major2ll.values())
									lastLike += ll;
								proj = (float) (Math.pow(rule, 1.0 / c.getMajors().size()) * lastLike);
								if(hasSnapshotData) {
									snapshotProj = (float) (Math.pow(snapshotRule, 1.0 / c.getMajors().size()) * lastLike);
								}
							} else {
								for (Map.Entry<String,Integer> m2l: major2ll.entrySet()) {
									lastLike += m2l.getValue();
									proj += getProjection(rules, m2l.getKey(), clasf.getAcademicClassification().getCode()) * m2l.getValue();
									if (hasSnapshotData){
										snapshotProj += getSnapshotProjection(snapshotRules, m2l.getKey(), clasf.getAcademicClassification().getCode()) * m2l.getValue();
									}
								}
							}
						}
						cfi.setLastLike(lastLike == 0 ? null : lastLike);
						cfi.setProjection(Math.round(proj) == 0 ? null : Math.round(proj));
						cfi.setExpected(clasf.getNrStudents());
						
						cfi.setSessionHasSnapshotData(hasSnapshotData);
						if (hasSnapshotData) {
							cfi.setSnapshotProjection(Math.round(snapshotProj) == 0 ? null : Math.round(snapshotProj));
							cfi.setSnapshotExpected(clasf.getSnapshotNrStudents());
						}
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
							float snapshotProj = 0;
							Hashtable<String, Integer> major2ll = clasfMajor2ll.get(clasf.getCode());
							if (major2ll != null) {
								if (c.isMultipleMajors() && major2ll.size() > 1) {
									double rule = 1.0f;
									double snapshotRule = 1.0f;
									for (PosMajor m: c.getMajors()){
										rule *= getProjection(rules, m.getCode(), clasf.getCode());
										if (hasSnapshotData){
											snapshotRule *= getSnapshotProjection(snapshotRules, m.getCode(), clasf.getCode());
										}
									}
									for (Integer ll: major2ll.values())
										lastLike += ll;
									proj = (float) (Math.pow(rule, 1.0 / c.getMajors().size()) * lastLike);
									if (hasSnapshotData){
										snapshotProj = (float) (Math.pow(snapshotRule, 1.0 / c.getMajors().size()) * lastLike);
									}
								}
								for (Map.Entry<String,Integer> m2l: major2ll.entrySet()) {
									lastLike += m2l.getValue();
									proj += getProjection(rules, m2l.getKey(), clasf.getCode()) * m2l.getValue();
									if (hasSnapshotData){
										snapshotProj += getSnapshotProjection(snapshotRules, m2l.getKey(), clasf.getCode()) * m2l.getValue();
									}
								}
							}
							cfi.setLastLike(lastLike == 0 ? null : lastLike);
							cfi.setProjection(Math.round(proj) == 0 ? null : Math.round(proj));
							cfi.setEnrollment(clasf2enrl.get(clasf.getId()));
							cfi.setRequested(clasf2req.get(clasf.getId()));
							
							cfi.setSessionHasSnapshotData(hasSnapshotData);
							if (hasSnapshotData){
								cfi.setSnapshotProjection(Math.round(snapshotProj) == 0 ? null : Math.round(snapshotProj));
							} else {
								cfi.setSnapshotProjection(null);
							}
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
			boolean hasSnapshotData = hasSnapshotData(hibSession, getAcademicSessionId());
			try {
				Curriculum c = CurriculumDAO.getInstance().get(curriculumId, hibSession);
				if (c == null) throw new CurriculaException(MESSAGES.errorCurriculumDoesNotExist(curriculumId == null ? "null" : curriculumId.toString()));
				Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, c.getAcademicArea().getUniqueId());
				Hashtable<String,HashMap<String, Float>> snapshotRules = null;
				if (hasSnapshotData) {
					snapshotRules = getSnapshotRules(hibSession, c.getAcademicArea().getUniqueId());
				}
				
				CurriculumInterface curriculumIfc = new CurriculumInterface();
				curriculumIfc.setId(c.getUniqueId());
				curriculumIfc.setAbbv(c.getAbbv());
				curriculumIfc.setName(c.getName());
				curriculumIfc.setEditable(getSessionContext().hasPermission(c, Right.CurriculumEdit));
				curriculumIfc.setMultipleMajors(c.isMultipleMajors());
				curriculumIfc.setSessionHasSnapshotData(hasSnapshotData);
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
				
				Hashtable<Long, Integer> clasf2enrl = loadClasf2enrl(hibSession, c);
				Hashtable<Long, Integer> clasf2req = loadClasf2req(hibSession, c);
				Hashtable<String, Hashtable<String, Integer>> clasfMajor2ll = loadClasfMajor2ll(hibSession, c);
				
				for (AcademicClassificationInterface clasf: academicClassifications) {
					CurriculumClassificationInterface clasfIfc = new CurriculumClassificationInterface();
					clasfIfc.setName(clasf.getCode());
					clasfIfc.setCurriculumId(c.getUniqueId());
					int lastLike = 0;
					float proj = 0;
					float snapshotProj = 0;
					Hashtable<String, Integer> major2ll = clasfMajor2ll.get(clasf.getCode());
					if (major2ll != null) {
						if (c.isMultipleMajors() && c.getMajors().size() > 1) {
							double rule = 1.0f;
							double snapshotRule = 1.0f;
							for (PosMajor m: c.getMajors()){
								rule *= getProjection(rules, m.getCode(), clasf.getCode());
								if (hasSnapshotData) {
									snapshotRule *= getSnapshotProjection(snapshotRules, m.getCode(), clasf.getCode());
								}
							}
							for (Integer ll: major2ll.values())
								lastLike += ll;
							proj = (float) (Math.pow(rule, 1.0 / c.getMajors().size()) * lastLike);
							if (hasSnapshotData) {
								snapshotProj = (float) (Math.pow(snapshotRule, 1.0 / c.getMajors().size()) * lastLike);
							}
						} else {
							for (Map.Entry<String,Integer> m2l: major2ll.entrySet()) {
								lastLike += m2l.getValue();
								proj += getProjection(rules, m2l.getKey(), clasf.getCode()) * m2l.getValue();
								if (hasSnapshotData) {
									snapshotProj += getSnapshotProjection(snapshotRules, m2l.getKey(), clasf.getCode()) * m2l.getValue();
								}	
							}
						}
					}
					clasfIfc.setLastLike(lastLike == 0 ? null : lastLike);
					clasfIfc.setProjection(Math.round(proj) == 0 ? null : Math.round(proj));
					
					clasfIfc.setSessionHasSnapshotData(hasSnapshotData);
					if (hasSnapshotData) {
						clasfIfc.setSnapshotProjection(Math.round(snapshotProj) == 0 ? null : Math.round(snapshotProj));
					} else {
						clasfIfc.setSnapshotProjection(null);
					}
					clasfIfc.setEnrollment(clasf2enrl.get(clasf.getId()));
					clasfIfc.setRequested(clasf2req.get(clasf.getId()));
					AcademicClassificationInterface acadClasfIfc = new AcademicClassificationInterface();
					acadClasfIfc.setId(clasf.getId());
					acadClasfIfc.setName(clasf.getName());
					acadClasfIfc.setCode(clasf.getCode());
					clasfIfc.setAcademicClassification(acadClasfIfc);
					curriculumIfc.addClassification(clasfIfc);
				}
				
				Hashtable<Long, CurriculumCourseGroupInterface> groups = new Hashtable<Long, CurriculumInterface.CurriculumCourseGroupInterface>();
				
				if (c.isMultipleMajors() && !c.getMajors().isEmpty()) {
					for (Curriculum x: (List<Curriculum>)hibSession.createQuery(
							"select distinct x from Curriculum x, Curriculum c inner join c.majors m where " +
							"c.uniqueId = :curriculumId and x.uniqueId != c.uniqueId and c.academicArea = x.academicArea and (x.majors is empty or m in elements(x.majors))")
							.setLong("curriculumId", c.getUniqueId()).setCacheable(true).list()) {
						if (x.getMajors().size() > 1) continue;
						String template = x.getAcademicArea().getAcademicAreaAbbreviation();
						String groupPrefix = x.getAcademicArea().getAcademicAreaAbbreviation() + " ";
						for (PosMajor major: x.getMajors()) {
							groupPrefix = major.getCode() + " ";
							template = major.getCode();
						}
						for (CurriculumClassification clasf: x.getClassifications()) {
							CurriculumClassificationInterface clasfIfc = curriculumIfc.getClassification(clasf.getAcademicClassification().getUniqueId());
							clasfIfc.setExpected(0);
							if (hasSnapshotData) {
								clasfIfc.setSnapshotExpected(0);
							} else {
								clasfIfc.setSnapshotExpected(null);
							}
							idx = classifications.get(clasf.getAcademicClassification().getUniqueId());
							for (CurriculumCourse course: clasf.getCourses()) {
								CourseInterface courseIfc = curriculumIfc.getCourse(course.getCourse().getUniqueId());
								if (courseIfc == null) {
									courseIfc = new CourseInterface();
									courseIfc.setId(course.getCourse().getUniqueId());
									courseIfc.setCourseName(course.getCourse().getCourseName());
									curriculumIfc.addCourse(courseIfc);
								}
								CurriculumCourseInterface curCourseIfc = courseIfc.getCurriculumCourse(idx);
								if (curCourseIfc == null) {
									curCourseIfc = new CurriculumCourseInterface();
									curCourseIfc.setCourseOfferingId(course.getCourse().getUniqueId());
									curCourseIfc.setCourseName(course.getCourse().getCourseName());
									curCourseIfc.setDefaultShare(course.getPercShare());
									curCourseIfc.setSessionHasSnapshotData(hasSnapshotData);
									if (hasSnapshotData) {
										curCourseIfc.setDefaultSnapshotShare(course.getSnapshotPercShare());
									}
									courseIfc.setCurriculumCourse(idx, curCourseIfc);
									curCourseIfc.addTemplate(template);
								} else { 
									if (course.getPercShare() >= curCourseIfc.getDefaultShare()) {
										curCourseIfc.setDefaultShare(course.getPercShare());
										curCourseIfc.addTemplate(template);
									}
									if (hasSnapshotData && course.getSnapshotPercShare() >= curCourseIfc.getDefaultSnapshotShare()) {
										curCourseIfc.setDefaultSnapshotShare(course.getSnapshotPercShare());
										curCourseIfc.addTemplate(template);
									}
								}
								for (CurriculumCourseGroup group: course.getGroups()) {
									CurriculumCourseGroupInterface g = groups.get(group.getUniqueId());
									if (g == null) {
										g = new CurriculumCourseGroupInterface();
										g.setName(groupPrefix + group.getName());
										g.setType(group.getType());
										g.setEditable(false);
										g.setColor(group.getColor());
										groups.put(group.getUniqueId(), g);
									}
									courseIfc.addGroup(g);
								}
							}
						}
					}
				}
				
				for (CurriculumClassification clasf: c.getClassifications()) {
					CurriculumClassificationInterface clasfIfc = curriculumIfc.getClassification(clasf.getAcademicClassification().getUniqueId());
					clasfIfc.setId(clasf.getUniqueId());
					clasfIfc.setExpected(clasf.getNrStudents());
					if (hasSnapshotData) {
						clasfIfc.setSnapshotExpected(clasf.getSnapshotNrStudents());
					} else {
						clasfIfc.setSnapshotExpected(null);
					}
					idx = classifications.get(clasf.getAcademicClassification().getUniqueId());
					
					for (CurriculumCourse course: clasf.getCourses()) {
						CourseInterface courseIfc = curriculumIfc.getCourse(course.getCourse().getUniqueId());
						if (courseIfc == null) {
							courseIfc = new CourseInterface();
							courseIfc.setId(course.getCourse().getUniqueId());
							courseIfc.setCourseName(course.getCourse().getCourseName());
							curriculumIfc.addCourse(courseIfc);
						}
						CurriculumCourseInterface curCourseIfc = courseIfc.getCurriculumCourse(idx);
						if (curCourseIfc == null) {
							curCourseIfc = new CurriculumCourseInterface();
							curCourseIfc.setCourseOfferingId(course.getCourse().getUniqueId());
							curCourseIfc.setShare(course.getPercShare());
							if (hasSnapshotData) {
								curCourseIfc.setSnapshotShare(course.getSnapshotPercShare());
							}
							curCourseIfc.setCourseName(course.getCourse().getCourseName());
							courseIfc.setCurriculumCourse(idx, curCourseIfc);
						}
						curCourseIfc.setId(course.getUniqueId());
						curCourseIfc.setCurriculumClassificationId(clasf.getUniqueId());
						curCourseIfc.setShare(course.getPercShare());
						
						if (hasSnapshotData) {
							curCourseIfc.setSnapshotShare(course.getSnapshotPercShare());
						}
						for (CurriculumCourseGroup group: course.getGroups()) {
							CurriculumCourseGroupInterface g = groups.get(group.getUniqueId());
							if (g == null) {
								g = new CurriculumCourseGroupInterface();
								g.setName(group.getName());
								g.setType(group.getType());
								g.setEditable(true);
								g.setColor(group.getColor());
								g.setId(group.getUniqueId());
								groups.put(group.getUniqueId(), g);
							}
							courseIfc.addGroup(g);
						}
					}
				}
				
				if (curriculumIfc.hasClassifications() && curriculumIfc.hasCourses()) {
					Hashtable<Long, Hashtable<Long, Integer>> clasf2course2enrl = loadClasfCourse2enrl(hibSession, c);
					Hashtable<Long, Hashtable<Long, Integer>> clasf2course2req = loadClasfCourse2req(hibSession, c);
					Hashtable<String, Hashtable<String, Hashtable<Long, Integer>>> clasfMajor2course2ll = loadClasfMajorCourse2ll(hibSession, c);
					for (CurriculumClassificationInterface clasfIfc: curriculumIfc.getClassifications()) {
						idx = classifications.get(clasfIfc.getAcademicClassification().getId());
						Hashtable<Long, Integer> course2enrl = clasf2course2enrl.get(clasfIfc.getAcademicClassification().getId());
						Hashtable<Long, Integer> course2req = clasf2course2req.get(clasfIfc.getAcademicClassification().getId());
						Hashtable<String, Hashtable<Long, Integer>> major2course2ll = clasfMajor2course2ll.get(clasfIfc.getAcademicClassification().getId());
						for (CourseInterface courseIfc: curriculumIfc.getCourses()) {
							CurriculumCourseInterface curCourseIfc = courseIfc.getCurriculumCourse(idx);
							if (curCourseIfc != null) {
								curCourseIfc.setEnrollment(course2enrl == null ? null : course2enrl.get(courseIfc.getId()));
								curCourseIfc.setRequested(course2req == null ? null : course2req.get(courseIfc.getId()));
								int courseLastLike = 0;
								float courseProj = 0;
								float courseSnapshotProj = 0;
								if (c.isMultipleMajors() && c.getMajors().size() > 2) {
									if (major2course2ll != null) {
										for (Hashtable<Long,Integer> m2l: major2course2ll.values()) {
											Integer ll = m2l.get(courseIfc.getId());
											if (ll != null)
												courseLastLike += ll;
										}
										double rule = 1.0;
										double snapshotRule = 1.0;
										for (PosMajor m: c.getMajors()) {
											rule *= getProjection(rules, m.getCode(), clasfIfc.getAcademicClassification().getCode());
											if (hasSnapshotData) {
												snapshotRule *= getSnapshotProjection(snapshotRules, m.getCode(), clasfIfc.getAcademicClassification().getCode());
											}
										}
										courseProj = (float)(Math.pow(rule, 1.0 / c.getMajors().size()) * courseLastLike);
										if (hasSnapshotData) {
											courseSnapshotProj = (float)(Math.pow(snapshotRule, 1.0 / c.getMajors().size()) * courseLastLike);
										}
									}
								} else {
									if (major2course2ll != null) {
										for (Map.Entry<String,Hashtable<Long,Integer>> m2l: major2course2ll.entrySet()) {
											Integer ll = m2l.getValue().get(courseIfc.getId());
											if (ll != null) {
												courseLastLike += ll;
												courseProj += getProjection(rules, m2l.getKey(), clasfIfc.getAcademicClassification().getCode());
												if (hasSnapshotData) {
													courseSnapshotProj += getSnapshotProjection(snapshotRules, m2l.getKey(), clasfIfc.getAcademicClassification().getCode());
												}
											}
										}
									}
								}
								curCourseIfc.setLastLike(courseLastLike == 0 ? null : courseLastLike);
								curCourseIfc.setProjection(Math.round(courseProj) == 0 ? null : Math.round(courseProj));
								
								curCourseIfc.setSessionHasSnapshotData(hasSnapshotData);
								if (hasSnapshotData) {
									curCourseIfc.setSnapshotProjection(Math.round(courseSnapshotProj) == 0 ? null : Math.round(courseSnapshotProj));
								} else {
									curCourseIfc.setSnapshotProjection(null);
								}
							}
						}
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
	
	@PreAuthorize("checkPermission('CurriculumView')")
	public CurriculumInterface loadTemplate(Long acadAreaId, List<Long> majors) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("loadTemplate(acadAreaId=" + acadAreaId + ", majors= " + majors + ")");
			Long s0 = System.currentTimeMillis();

			TreeSet<AcademicClassificationInterface> academicClassifications = loadAcademicClassifications();
			Hashtable<Long, Integer> classifications = new Hashtable<Long, Integer>();
			int idx = 0;
			for (AcademicClassificationInterface clasf: academicClassifications) {
				classifications.put(clasf.getId(), idx++);
			}
			
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			boolean hasSnapshotData = hasSnapshotData(hibSession, getAcademicSessionId());
			try {
				AcademicArea acadArea = AcademicAreaDAO.getInstance().get(acadAreaId, hibSession);
				
				List<PosMajor> posMajors = new ArrayList<PosMajor>();
				for (Long majorId: majors) {
					posMajors.add(PosMajorDAO.getInstance().get(majorId,hibSession));
				}
				
				CurriculumInterface curriculumIfc = new CurriculumInterface();
				AcademicAreaInterface areaIfc = new AcademicAreaInterface();
				areaIfc.setId(acadArea.getUniqueId());
				areaIfc.setAbbv(acadArea.getAcademicAreaAbbreviation());
				areaIfc.setName(Constants.curriculaToInitialCase(acadArea.getTitle()));
				curriculumIfc.setAcademicArea(areaIfc);
				curriculumIfc.setEditable(true);
				for (Long majorId: majors) {
					PosMajor major = PosMajorDAO.getInstance().get(majorId, hibSession);
					MajorInterface majorIfc = new MajorInterface();
					majorIfc.setId(major.getUniqueId());
					majorIfc.setCode(major.getCode());
					majorIfc.setName(Constants.curriculaToInitialCase(major.getName()));
					curriculumIfc.addMajor(majorIfc);
				}
				
				for (AcademicClassificationInterface clasf: academicClassifications) {
					CurriculumClassificationInterface clasfIfc = new CurriculumClassificationInterface();
					clasfIfc.setName(clasf.getCode());
					clasfIfc.setSessionHasSnapshotData(hasSnapshotData);
					AcademicClassificationInterface acadClasfIfc = new AcademicClassificationInterface();
					acadClasfIfc.setId(clasf.getId());
					acadClasfIfc.setName(clasf.getName());
					acadClasfIfc.setCode(clasf.getCode());
					clasfIfc.setAcademicClassification(acadClasfIfc);
					curriculumIfc.addClassification(clasfIfc);
				}

				Hashtable<Long, CurriculumCourseGroupInterface> groups = new Hashtable<Long, CurriculumInterface.CurriculumCourseGroupInterface>();
				
				List<Curriculum> curricula = new ArrayList<Curriculum>();
				curricula.addAll(hibSession.createQuery(
						"select distinct x from Curriculum x where x.academicArea.uniqueId = :acadAreaId and x.majors is empty")
						.setLong("acadAreaId", acadAreaId).setCacheable(true).list());
				if (!majors.isEmpty())
					curricula.addAll(hibSession.createQuery(
							"select distinct x from Curriculum x inner join x.majors m where " +
							"x.academicArea.uniqueId = :acadAreaId and m.uniqueId in :majorIds")
							.setLong("acadAreaId", acadAreaId).setParameterList("majorIds", majors).setCacheable(true).list());
				
				for (Curriculum x: curricula) {
					if (x.getMajors().size() > 1) continue;
					String template = x.getAcademicArea().getAcademicAreaAbbreviation();
					String groupPrefix = x.getAcademicArea().getAcademicAreaAbbreviation() + " ";
					for (PosMajor major: x.getMajors()) {
						groupPrefix = major.getCode() + " ";
						template = major.getCode();
					}
					for (CurriculumClassification clasf: x.getClassifications()) {
						CurriculumClassificationInterface clasfIfc = curriculumIfc.getClassification(clasf.getAcademicClassification().getUniqueId());
						clasfIfc.setExpected(0);
						if (hasSnapshotData) {
							clasfIfc.setSnapshotExpected(0);
						} else {
							clasfIfc.setSnapshotExpected(null);
						}
						idx = classifications.get(clasf.getAcademicClassification().getUniqueId());
						for (CurriculumCourse course: clasf.getCourses()) {
							CourseInterface courseIfc = curriculumIfc.getCourse(course.getCourse().getUniqueId());
							if (courseIfc == null) {
								courseIfc = new CourseInterface();
								courseIfc.setId(course.getCourse().getUniqueId());
								courseIfc.setCourseName(course.getCourse().getCourseName());
								curriculumIfc.addCourse(courseIfc);
							}
							CurriculumCourseInterface curCourseIfc = courseIfc.getCurriculumCourse(idx);
							if (curCourseIfc == null) {
								curCourseIfc = new CurriculumCourseInterface();
								curCourseIfc.setCourseOfferingId(course.getCourse().getUniqueId());
								curCourseIfc.setCourseName(course.getCourse().getCourseName());
								curCourseIfc.setDefaultShare(course.getPercShare());
								curCourseIfc.setSessionHasSnapshotData(hasSnapshotData);
								if (hasSnapshotData){
									curCourseIfc.setDefaultSnapshotShare(course.getSnapshotPercShare());
								}
								courseIfc.setCurriculumCourse(idx, curCourseIfc);
								curCourseIfc.addTemplate(template);
							} else {
								if (course.getPercShare() >= curCourseIfc.getDefaultShare()) {
									curCourseIfc.setDefaultShare(course.getPercShare());
									curCourseIfc.addTemplate(template);
								}
								if (hasSnapshotData && course.getSnapshotPercShare() >= curCourseIfc.getDefaultSnapshotShare()) {
									curCourseIfc.setDefaultSnapshotShare(course.getSnapshotPercShare());
									curCourseIfc.addTemplate(template);
								}
							}
							for (CurriculumCourseGroup group: course.getGroups()) {
								CurriculumCourseGroupInterface g = groups.get(group.getUniqueId());
								if (g == null) {
									g = new CurriculumCourseGroupInterface();
									g.setName(groupPrefix + group.getName());
									g.setType(group.getType());
									g.setColor(group.getColor());
									g.setEditable(false);
									groups.put(group.getUniqueId(), g);
								}
								courseIfc.addGroup(g);
							}
						}
					}
				}
				
				sLog.debug("Computed 1 template (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
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
				c.setMultipleMajors(curriculum.isMultipleMajors());
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
				
				Boolean multipleMajors = null;
				boolean hasSnapshotData = hasSnapshotData(hibSession, getAcademicSessionId());
				for (Long curriculumId: curriculumIds) {
					if (curriculumId == null) 
						throw new CurriculaException(MESSAGES.errorCannotMergeUnsavedCurriculum());
					
					Curriculum curriculum = CurriculumDAO.getInstance().get(curriculumId, hibSession);
					if (curriculum == null) throw new CurriculaException(MESSAGES.errorCurriculumDoesNotExist(curriculumId.toString()));
					if (multipleMajors == null) multipleMajors = curriculum.isMultipleMajors();
					
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
							
							if (hasSnapshotData) {
								mergedCourse.setSnapshotPercShare(
										(mergedCourse.getSnapshotPercShare() * mergedClasf.getSnapshotNrStudents()
												+ (course.getSnapshotPercShare() == null
														|| clasf.getSnapshotNrStudents() == null ? 0f
																: course.getSnapshotPercShare()
																		* clasf.getSnapshotNrStudents()))
												/ (mergedClasf.getSnapshotNrStudents()
														+ (clasf.getSnapshotNrStudents() == null ? 0
																: clasf.getSnapshotNrStudents())));
							}

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
							if (clasf.getNrStudents() != null && clasf.getNrStudents() > 0) {
								mergedCourse.setPercShare(
										(mergedCourse.getPercShare() * mergedClasf.getNrStudents()) / 
										(mergedClasf.getNrStudents() + clasf.getNrStudents())
										);
								if (hasSnapshotData) {
									mergedCourse.setSnapshotPercShare((mergedCourse.getSnapshotPercShare()
											* mergedClasf.getSnapshotNrStudents())
											/ (mergedClasf.getSnapshotNrStudents() + clasf.getSnapshotNrStudents()));
								}
							}
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
					
					mergedCurriculum.setMultipleMajors(multipleMajors);
					
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
	public HashMap<String, CurriculumStudentsInterface[]> computeEnrollmentsAndLastLikes(Long acadAreaId, List<Long> majors, boolean multipleMajors) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("computeEnrollmentsAndLastLikes(acadAreaId=" + acadAreaId + ", majors=" + majors + ")");
			Long s0 = System.currentTimeMillis();
			if (acadAreaId == null) return new HashMap<String, CurriculumStudentsInterface[]>();
			Hashtable<Long, Integer> classificationIndex = new Hashtable<Long, Integer>();
			int idx = 0;
			boolean hasSnapshotData = hasSnapshotData(CurriculumDAO.getInstance().getSession(), getAcademicSessionId());
			TreeSet<AcademicClassificationInterface> classifications = loadAcademicClassifications();
			for (AcademicClassificationInterface clasf: classifications) {
				classificationIndex.put(clasf.getId(), idx++);
			}
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			HashMap<String, CurriculumStudentsInterface[]> results = new HashMap<String, CurriculumStudentsInterface[]>();
			try {
				Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, acadAreaId);
				Hashtable<String, HashMap<String, Float>> snapshotRules = getSnapshotRules(hibSession, acadAreaId);

				AcademicArea acadArea = AcademicAreaDAO.getInstance().get(acadAreaId, hibSession);
				
				List<PosMajor> posMajors = new ArrayList<PosMajor>();
				for (Long majorId: majors) {
					posMajors.add(PosMajorDAO.getInstance().get(majorId,hibSession));
				}
				
				Hashtable<Long, Set<Long>> clasf2enrl = loadClasf2enrl(hibSession, acadAreaId, majors, multipleMajors);
				
				Hashtable<Long, Set<Long>> clasf2req = loadClasf2req(hibSession, acadAreaId, majors, multipleMajors);

				Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> clasf2course2enrl = loadClasfCourse2enrl(hibSession, acadAreaId, majors, multipleMajors);
				
				Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> clasf2course2req = loadClasfCourse2req(hibSession, acadAreaId, majors, multipleMajors);
				
				Hashtable<String, HashMap<String, Set<Long>>> clasf2ll = loadClasfMajor2ll(hibSession, acadArea.getAcademicAreaAbbreviation(), posMajors, multipleMajors);
				
				Hashtable<String, Hashtable<CourseInterface, HashMap<String, Set<Long>>>> clasf2course2ll = loadClasfCourseMajor2ll(hibSession, acadArea.getAcademicAreaAbbreviation(), posMajors, multipleMajors);
				
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
					x[col].setSnapshotProjection(snapshotRules.get(clasf.getCode()));
					x[col].setSessionHasSnapshotData(hasSnapshotData);
					
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
						c[col].setSnapshotProjection(snapshotRules == null ? null : snapshotRules.get(clasf.getCode()));
						c[col].setSessionHasSnapshotData(hasSnapshotData);
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
		List<CurriculumInterface> results = new ArrayList<CurriculumInterface>();
		
		Hashtable<Long, Integer> classifications = new Hashtable<Long, Integer>();
		int idx = 0;
		for (AcademicClassificationInterface clasf: academicClassifications) {
			classifications.put(clasf.getId(), idx++);
		}
		
		Map<Long, Map<Long, Map<Long, Set<Long>>>> area2major2clasf2enrl = loadAreaMajorClasf2enrl(hibSession, courseOffering.getUniqueId());
		
		Map<String, Map<String, Map<String, Set<Long>>>> area2major2clasf2ll = loadAreaMajorClasf2ll(hibSession, courseOffering.getUniqueId());
		
		Map<Long, Map<Long, Map<Long, Set<Long>>>> area2major2clasf2req = loadAreaMajorClasf2req(hibSession, courseOffering.getUniqueId());
		
		Hashtable<Long, CurriculumInterface> curricula = new Hashtable<Long, CurriculumInterface>();
		Hashtable<Long, Hashtable<Long, Integer>> cur2clasf2enrl = new Hashtable<Long, Hashtable<Long, Integer>>();
		Hashtable<Long, Hashtable<String, Integer>> cur2clasf2ll = new Hashtable<Long, Hashtable<String, Integer>>();
		boolean hasSnapshotData = hasSnapshotData(CurriculumDAO.getInstance().getSession(), getAcademicSessionId());

		for (CurriculumCourse course : (List<CurriculumCourse>)hibSession.createQuery(
				"select c from CurriculumCourse c where c.course.uniqueId = :courseId")
				.setLong("courseId", courseOffering.getUniqueId()).setCacheable(true).list()) {
			CurriculumClassification clasf = course.getClassification();
			Curriculum curriculum = clasf.getCurriculum();
			
			List<Curriculum> children = null;
			if (curriculum.getMajors().isEmpty()) {
				children = hibSession.createQuery("select c from Curriculum c where c.academicArea.uniqueId = :areaId and c.multipleMajors = true and " +
						"(select count(x) from CurriculumCourse x where x.course.uniqueId = :courseId and x.classification.curriculum.uniqueId = c.uniqueId) = 0"
						).setLong("areaId", curriculum.getAcademicArea().getUniqueId()).setLong("courseId", course.getCourse().getUniqueId()).setCacheable(true).list();
			} else if (curriculum.getMajors().size() == 1) {
				children = hibSession.createQuery("select c from Curriculum c inner join c.majors m where c.academicArea.uniqueId = :areaId and m.uniqueId = :majorId and c.multipleMajors = true and " +
						"(select count(x) from CurriculumCourse x where x.course.uniqueId = :courseId and x.classification.curriculum.uniqueId = c.uniqueId) = 0"
						).setLong("areaId", curriculum.getAcademicArea().getUniqueId()).setLong("majorId", curriculum.getMajors().iterator().next().getUniqueId())
						.setLong("courseId", course.getCourse().getUniqueId()).setCacheable(true).list();
			}
			if (children != null && !children.isEmpty()) {
				for (Curriculum child: children) {
					CurriculumClassification childClasf = null;
					for (CurriculumClassification x: child.getClassifications()) {
						if (x.getAcademicClassification().equals(clasf.getAcademicClassification()) && x.getNrStudents() > 0) {
							childClasf = x;
						}
					}
					if (childClasf == null) continue;
					
					// create curriculum interface
					CurriculumInterface curriculumIfc = curricula.get(child.getUniqueId());
					if (curriculumIfc == null) {
						curriculumIfc = new CurriculumInterface();
						curriculumIfc.setId(child.getUniqueId());
						curriculumIfc.setAbbv(child.getAbbv());
						curriculumIfc.setName(child.getName());
						curriculumIfc.setMultipleMajors(child.isMultipleMajors());
						curriculumIfc.setSessionHasSnapshotData(hasSnapshotData);
						AcademicAreaInterface areaIfc = new AcademicAreaInterface();
						areaIfc.setId(child.getAcademicArea().getUniqueId());
						areaIfc.setAbbv(child.getAcademicArea().getAcademicAreaAbbreviation());
						areaIfc.setName(Constants.curriculaToInitialCase(child.getAcademicArea().getTitle()));
						curriculumIfc.setAcademicArea(areaIfc);
						DepartmentInterface deptIfc = new DepartmentInterface();
						deptIfc.setId(child.getDepartment().getUniqueId());
						deptIfc.setAbbv(child.getDepartment().getAbbreviation());
						deptIfc.setCode(child.getDepartment().getDeptCode());
						deptIfc.setName(child.getDepartment().getName());
						curriculumIfc.setDepartment(deptIfc);
						for (Iterator<PosMajor> i = child.getMajors().iterator(); i.hasNext(); ) {
							PosMajor major = i.next();
							MajorInterface mi = new MajorInterface();
							mi.setId(major.getUniqueId());
							mi.setCode(major.getCode());
							mi.setName(Constants.curriculaToInitialCase(major.getName()));
							curriculumIfc.addMajor(mi);
						}
						curricula.put(child.getUniqueId(), curriculumIfc);
						results.add(curriculumIfc);

						cur2clasf2enrl.put(child.getUniqueId(), loadClasf2enrl(hibSession, child));
						
						cur2clasf2ll.put(child.getUniqueId(), loadClasf2ll(hibSession, child));
					}
					
					CurriculumClassificationInterface curClasfIfc = new CurriculumClassificationInterface();
					curClasfIfc.setId(childClasf.getUniqueId());
					curClasfIfc.setName(childClasf.getName());
					curClasfIfc.setCurriculumId(child.getUniqueId());
					curClasfIfc.setLastLike(cur2clasf2ll.get(child.getUniqueId()).get(childClasf.getAcademicClassification().getCode()));
					curClasfIfc.setExpected(childClasf.getNrStudents());
					curClasfIfc.setSessionHasSnapshotData(hasSnapshotData);
					if (hasSnapshotData) {
						curClasfIfc.setSnapshotExpected(childClasf.getSnapshotNrStudents());
					} else {
						curClasfIfc.setSnapshotExpected(null);
					}
					curClasfIfc.setEnrollment(cur2clasf2enrl.get(child.getUniqueId()).get(childClasf.getAcademicClassification().getUniqueId()));
					AcademicClassificationInterface acadClasfIfc = new AcademicClassificationInterface();
					acadClasfIfc.setId(childClasf.getAcademicClassification().getUniqueId());
					acadClasfIfc.setName(childClasf.getAcademicClassification().getName());
					acadClasfIfc.setCode(childClasf.getAcademicClassification().getCode());
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
					
					CurriculumCourseInterface curCourseIfc = courseIfc.getCurriculumCourse(classifications.get(childClasf.getAcademicClassification().getUniqueId()));
					if (curCourseIfc == null) {
						curCourseIfc = new CurriculumCourseInterface();
						curCourseIfc.setCourseOfferingId(course.getCourse().getUniqueId());
						curCourseIfc.setCurriculumClassificationId(childClasf.getUniqueId());
						curCourseIfc.setDefaultShare(course.getPercShare());
						curCourseIfc.setCourseName(course.getCourse().getCourseName());
						curCourseIfc.setSessionHasSnapshotData(hasSnapshotData);
						if (hasSnapshotData) {
							curCourseIfc.setDefaultSnapshotShare(course.getSnapshotPercShare());
						}
					} else {
						if (curCourseIfc.getDefaultShare() < course.getPercShare()) {
							curCourseIfc.setDefaultShare(course.getPercShare());
						}
						if (curCourseIfc.getDefaultSnapshotShare() < course.getSnapshotPercShare()) {
							curCourseIfc.setDefaultSnapshotShare(course.getSnapshotPercShare());
						}
					}

					courseIfc.setCurriculumCourse(classifications.get(childClasf.getAcademicClassification().getUniqueId()), curCourseIfc);
				}
			}			

			// create curriculum interface
			CurriculumInterface curriculumIfc = curricula.get(curriculum.getUniqueId());
			if (curriculumIfc == null) {
				curriculumIfc = new CurriculumInterface();
				curriculumIfc.setId(curriculum.getUniqueId());
				curriculumIfc.setAbbv(curriculum.getAbbv());
				curriculumIfc.setName(curriculum.getName());
				curriculumIfc.setMultipleMajors(curriculum.isMultipleMajors());
				curriculumIfc.setSessionHasSnapshotData(hasSnapshotData);
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
			curClasfIfc.setSessionHasSnapshotData(hasSnapshotData);
			if (hasSnapshotData) {
				curClasfIfc.setSnapshotExpected(clasf.getSnapshotNrStudents());
			} else {
				curClasfIfc.setSnapshotExpected(null);
			}
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
			curCourseIfc.setSessionHasSnapshotData(hasSnapshotData);
			if (hasSnapshotData) {
				curCourseIfc.setSnapshotShare(course.getSnapshotPercShare());
			}
			curCourseIfc.setCourseName(course.getCourse().getCourseName());
			
			courseIfc.setCurriculumCourse(classifications.get(clasf.getAcademicClassification().getUniqueId()), curCourseIfc);
		}
		Collections.sort(results, new Comparator<CurriculumInterface>() {
			@Override
			public int compare(CurriculumInterface c1, CurriculumInterface c2) {
				// multiple majors first
				if (c1.isMultipleMajors() != c2.isMultipleMajors())
					return c1.isMultipleMajors() ? -1 : 1;
				// more majors first
				if ((c1.hasMajors() ? c1.getMajors().size() : 0) != (c2.hasMajors() ? c2.getMajors().size() : 0))
					return (c1.hasMajors() ? c1.getMajors().size() : 0) > (c2.hasMajors() ? c2.getMajors().size() : 0) ? -1 : 1;
				return c1.compareTo(c2);
			}
		});
		
		for (CurriculumInterface curriculumIfc: results) {
			Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, curriculumIfc.getAcademicArea().getId());
			Hashtable<String, HashMap<String, Float>> snapshotRules = null;
			if (hasSnapshotData) {
				snapshotRules = getSnapshotRules(hibSession, curriculumIfc.getAcademicArea().getId());
			}
			for (AcademicClassificationInterface clasf : academicClassifications) {
				int enrl = 0;
				Map<Long, Map<Long, Set<Long>>> major2clasf2enrl = area2major2clasf2enrl.get(curriculumIfc.getAcademicArea().getId());
				if (major2clasf2enrl != null) {
					if (!curriculumIfc.hasMajors()) {
						if (curriculumIfc.isMultipleMajors()) {
							Map<Long, Set<Long>> clasf2enrl = major2clasf2enrl.get(-1l);
							Set<Long> e = (clasf2enrl == null ? null : clasf2enrl.get(clasf.getId()));
							if (e != null) {
								enrl += e.size();
								clasf2enrl.remove(-1l);
							}
						} else {
							Set<Long> s = new HashSet<Long>();
							for (Map<Long, Set<Long>> clasf2enrl: major2clasf2enrl.values()) {
								Set<Long> e = (clasf2enrl == null ? null : clasf2enrl.get(clasf.getId()));
								if (e != null) {
									s.addAll(e);
									clasf2enrl.remove(clasf.getId());
								}
							}
							enrl += s.size();
						}
					} else {
						if (curriculumIfc.isMultipleMajors()) {
							Set<Long> s = null;
							for (MajorInterface m: curriculumIfc.getMajors()) {
								Map<Long, Set<Long>> clasf2enrl = major2clasf2enrl.get(m.getId());
								Set<Long> e = (clasf2enrl == null ? null : clasf2enrl.get(clasf.getId()));
								if (e == null) {
									if (s == null)
										s = new HashSet<Long>();
									else
										s.clear();
								} else {
									if (s == null)
										s = new HashSet<Long>(e);
									else
										s.retainAll(e);
								}
							}
							if (s != null && !s.isEmpty()) {
								enrl += s.size();
								for (MajorInterface m: curriculumIfc.getMajors()) {
									Map<Long, Set<Long>> clasf2enrl = major2clasf2enrl.get(m.getId());
									Set<Long> e = (clasf2enrl == null ? null : clasf2enrl.get(clasf.getId()));
									if (e != null) {
										e.removeAll(s);
										if (e.isEmpty())
											clasf2enrl.remove(clasf.getId());
									}
								}
							}
						} else {
							Set<Long> s = new HashSet();
							for (MajorInterface m: curriculumIfc.getMajors()) {
								Map<Long, Set<Long>> clasf2enrl = major2clasf2enrl.get(m.getId());
								Set<Long> e = (clasf2enrl == null ? null : clasf2enrl.get(clasf.getId()));
								if (e != null) {
									s.addAll(e);
									clasf2enrl.remove(clasf.getId());
								}
							}
							enrl += s.size();
						}
					}
				}
				
				int lastLike = 0;
				float proj = 0.0f;
				float snapshotProj = 0.0f;
				Map<String, Map<String, Set<Long>>> major2clasf2ll = area2major2clasf2ll.get(curriculumIfc.getAcademicArea().getAbbv());
				if (major2clasf2ll != null) {
					if (!curriculumIfc.hasMajors()) {
						if (curriculumIfc.isMultipleMajors()) {
							Map<String, Set<Long>> clasf2ll = major2clasf2ll.get("");
							Set<Long> e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
							if (e != null) {
								lastLike += e.size();
								clasf2ll.remove(-1l);
							}
						} else {
							Set<Long> s = new HashSet<Long>();
							for (Map.Entry<String, Map<String, Set<Long>>> entry : major2clasf2ll.entrySet()) {
								Map<String, Set<Long>> clasf2ll = entry.getValue();
								Set<Long> e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
								if (e != null) {
									int add = 0;
									for (Long id: e)
										if (s.add(id)) add++;
									proj += getProjection(rules, entry.getKey(), clasf.getCode()) * add;
									if (hasSnapshotData) {
										snapshotProj += getSnapshotProjection(snapshotRules, entry.getKey(),
												clasf.getCode()) * add;
									}
									clasf2ll.remove(clasf.getId());
								}
							}
							lastLike += s.size();
						}
					} else {
						if (curriculumIfc.isMultipleMajors()) {
							Set<Long> s = null;
							float p = 1.0f;
							float ssp = 1.0f;
							for (MajorInterface m : curriculumIfc.getMajors()) {
								Map<String, Set<Long>> clasf2ll = major2clasf2ll.get(m.getCode());
								Set<Long> e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
								if (e == null) {
									if (s == null)
										s = new HashSet<Long>();
									else
										s.clear();
								} else {
									if (s == null)
										s = new HashSet<Long>(e);
									else
										s.retainAll(e);
								}
								p *= getProjection(rules, m.getCode(), clasf.getCode());
								if (hasSnapshotData) {
									ssp *= getSnapshotProjection(snapshotRules, m.getCode(), clasf.getCode());
								}
							}
							if (s != null && !s.isEmpty()) {
								lastLike += s.size();
								proj += Math.pow(p, 1.0 / curriculumIfc.getMajors().size()) * s.size();
								if (hasSnapshotData) {
									snapshotProj += Math.pow(ssp, 1.0 / curriculumIfc.getMajors().size()) * s.size();
								}
								for (MajorInterface m: curriculumIfc.getMajors()) {
									Map<String, Set<Long>> clasf2ll = major2clasf2ll.get(m.getCode());
									Set<Long> e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
									if (e != null) {
										e.removeAll(s);
										if (e.isEmpty())
											clasf2ll.remove(clasf.getCode());
									}
								}
							}
						} else {
							Set<Long> s = new HashSet();
							for (MajorInterface m: curriculumIfc.getMajors()) {
								Map<String, Set<Long>> clasf2ll = major2clasf2ll.get(m.getCode());
								Set<Long> e = (clasf2ll == null ? null : clasf2ll.get(clasf.getCode()));
								if (e != null) {
									int add = 0;
									for (Long id: e)
										if (s.add(id)) add ++;
									proj += getProjection(rules, m.getCode(), clasf.getCode()) * add;
									if (hasSnapshotData) {
										snapshotProj += getSnapshotProjection(snapshotRules, m.getCode(),
												clasf.getCode()) * add;
									}
									s.addAll(e);
									clasf2ll.remove(clasf.getCode());
								}
							}
							lastLike += s.size();
						}
					}
				}
				
				int req = 0;
				Map<Long, Map<Long, Set<Long>>> major2clasf2req = area2major2clasf2req.get(curriculumIfc.getAcademicArea().getId());
				if (major2clasf2req != null) {
					if (!curriculumIfc.hasMajors()) {
						if (curriculumIfc.isMultipleMajors()) {
							Map<Long, Set<Long>> clasf2req = major2clasf2req.get(-1l);
							Set<Long> e = (clasf2req == null ? null : clasf2req.get(clasf.getId()));
							if (e != null) {
								req += e.size();
								clasf2req.remove(-1l);
							}
						} else {
							Set<Long> s = new HashSet<Long>();
							for (Map<Long, Set<Long>> clasf2req: major2clasf2req.values()) {
								Set<Long> e = (clasf2req == null ? null : clasf2req.get(clasf.getId()));
								if (e != null) {
									s.addAll(e);
									clasf2req.remove(clasf.getId());
								}
							}
							req += s.size();
						}
					} else {
						if (curriculumIfc.isMultipleMajors()) {
							Set<Long> s = null;
							for (MajorInterface m: curriculumIfc.getMajors()) {
								Map<Long, Set<Long>> clasf2req = major2clasf2req.get(m.getId());
								Set<Long> e = (clasf2req == null ? null : clasf2req.get(clasf.getId()));
								if (e == null) {
									if (s == null)
										s = new HashSet<Long>();
									else
										s.clear();
								} else {
									if (s == null)
										s = new HashSet<Long>(e);
									else
										s.retainAll(e);
								}
							}
							if (s != null && !s.isEmpty()) {
								req += s.size();
								for (MajorInterface m: curriculumIfc.getMajors()) {
									Map<Long, Set<Long>> clasf2req = major2clasf2req.get(m.getId());
									Set<Long> e = (clasf2req == null ? null : clasf2req.get(clasf.getId()));
									if (e != null) {
										e.removeAll(s);
										if (e.isEmpty())
											clasf2req.remove(clasf.getId());
									}
								}
							}
						} else {
							Set<Long> s = new HashSet();
							for (MajorInterface m: curriculumIfc.getMajors()) {
								Map<Long, Set<Long>> clasf2req = major2clasf2req.get(m.getId());
								Set<Long> e = (clasf2req == null ? null : clasf2req.get(clasf.getId()));
								if (e != null) {
									s.addAll(e);
									clasf2req.remove(clasf.getId());
								}
							}
							req += s.size();
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
					
					CurriculumCourseInterface curCourseIfc = courseIfc.getCurriculumCourse(classifications.get(clasf.getId()));
					if (curCourseIfc == null) {
						curCourseIfc = new CurriculumCourseInterface();
						curCourseIfc.setCourseOfferingId(courseOffering.getUniqueId());
						curCourseIfc.setShare(0.0f);
						curCourseIfc.setCourseName(courseOffering.getCourseName());
						courseIfc.setCurriculumCourse(classifications.get(clasf.getId()), curCourseIfc);
					}

					if (enrl > 0)
						curCourseIfc.setEnrollment(enrl);

					if (lastLike > 0)
						curCourseIfc.setLastLike(lastLike);
					
					if (Math.round(proj) > 0)
						curCourseIfc.setProjection(Math.round(proj));
					
					curCourseIfc.setSessionHasSnapshotData(hasSnapshotData);
					if (hasSnapshotData) {
						if (Math.round(snapshotProj) > 0)
							curCourseIfc.setSnapshotProjection(Math.round(snapshotProj));
					} else {
						curCourseIfc.setSnapshotProjection(null);
					}

					if (req > 0)
						curCourseIfc.setRequested(req);
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
			Hashtable<String, HashMap<String, Float>> snapshotRules = null;
			if (hasSnapshotData) {
				snapshotRules = getSnapshotRules(hibSession, areaId);
			}
			boolean empty = true;
			CurriculumInterface otherCurriculumIfc = new CurriculumInterface();
			CourseInterface otherCourseIfc = new CourseInterface();
			otherCourseIfc.setId(courseOffering.getUniqueId());
			otherCourseIfc.setCourseName(courseOffering.getCourseName());
			otherCurriculumIfc.addCourse(otherCourseIfc);
			for (AcademicClassificationInterface clasf: academicClassifications) {
				int enrl = 0;
				Map<Long, Map<Long, Set<Long>>> major2clasf2enrl = area2major2clasf2enrl.get(areaId);
				if (major2clasf2enrl != null) {
					Set<Long> s = new HashSet<Long>();
					for (Map<Long, Set<Long>> clasf2enrl: major2clasf2enrl.values()) {
						Set<Long> e = clasf2enrl.get(clasf.getId());
						if (e != null)
							s.addAll(e);
					}
					enrl += s.size();
				}
				
				int lastLike = 0;
				int proj = 0;
				int snapshotProj = 0;
				Map<String, Map<String, Set<Long>>> major2clasf2ll = area2major2clasf2ll.get(areasId2Abbv.get(areaId));
				if (major2clasf2ll != null) {
					Set<Long> s = new HashSet<Long>();
					for (Map.Entry<String, Map<String, Set<Long>>> entry: major2clasf2ll.entrySet()) {
						Map<String, Set<Long>> clasf2ll = entry.getValue();
						Set<Long> e = clasf2ll.get(clasf.getCode());
						if (e != null) {
							int add = 0;
							for (Long id: e)
								if (s.add(id)) add++;
							proj += Math.round(getProjection(rules, entry.getKey(), clasf.getCode()) * add);
							if (hasSnapshotData) {
								snapshotProj += Math.round(getSnapshotProjection(snapshotRules, entry.getKey(), clasf.getCode()) * add);
							}
						}
					}
					lastLike += s.size();
				}
				
				int req = 0;
				Map<Long, Map<Long, Set<Long>>> major2clasf2req = area2major2clasf2req.get(areaId);
				if (major2clasf2req != null) {
					Set<Long> s = new HashSet<Long>();
					for (Map<Long, Set<Long>> clasf2req: major2clasf2req.values()) {
						Set<Long> e = clasf2req.get(clasf.getId());
						if (e != null)
							s.addAll(e);
					}
					req += s.size();
				}
				
				if (enrl > 0 || lastLike > 0 || proj > 0 || req > 0 || snapshotProj > 0) {
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
					otherCurCourseIfc.setSessionHasSnapshotData(hasSnapshotData);
					if (hasSnapshotData) {
						if (snapshotProj > 0) {
							otherCurCourseIfc.setSnapshotProjection(snapshotProj);
						}
					} else {
						otherCurCourseIfc.setSnapshotProjection(null);
					}
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
		
		return new TreeSet<CurriculumInterface>(results);
	}
	
	@PreAuthorize("checkPermission('CurriculumView') or checkPermission('Reservations')")
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
	public TreeSet<MajorInterface> loadMajors(Long curriculumId, Long academicAreaId, boolean multipleMajors) throws CurriculaException, PageAccessException {
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
				if (!multipleMajors) {
					majors.removeAll(
							hibSession.createQuery("select m from Curriculum c inner join c.majors m where c.academicArea = :academicAreaId and c.uniqueId != :curriculumId and c.multipleMajors = false")
							.setLong("academicAreaId", academicAreaId).setLong("curriculumId", (curriculumId == null ? -1l : curriculumId)).setCacheable(true).list());
				}
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
	
	@Autowired PermissionDepartment permissionDepartment;
	
	@PreAuthorize("checkPermission('CurriculumView') or checkPermission('Reservations')")
	public Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(String query, Integer limit, boolean includeNotOffered, boolean checkDepartment) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("listCourseOfferings(query='" + query + "', limit=" + limit + ")");
			Long s0 = System.currentTimeMillis();
			ArrayList<ClassAssignmentInterface.CourseAssignment> results = new ArrayList<ClassAssignmentInterface.CourseAssignment>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			try {
				for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
						"select c from CourseOffering c where " + (includeNotOffered ? "" : "c.instructionalOffering.notOffered = false and ") +
						"c.subjectArea.session.uniqueId = :sessionId and (" +
						"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' or lower(c.courseNbr) like :q || '%' " +
						(query.length()>2 ? "or lower(c.title) like '%' || :q || '%'" : "") + ") " +
						"order by case " +
						"when lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' then 0 else 1 end," + // matches on course name first
						"c.subjectArea.subjectAreaAbbreviation, c.courseNbr")
						.setString("q", query.toLowerCase())
						.setLong("sessionId", sessionId)
						.setCacheable(true).setMaxResults(limit == null || limit < 0 || checkDepartment? Integer.MAX_VALUE : limit).list()) {
					if (checkDepartment && !permissionDepartment.check(getSessionContext().getUser(), c.getDepartment(), DepartmentStatusType.Status.OwnerLimitedEdit, DepartmentStatusType.Status.ManagerLimitedEdit))
						continue;
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
					course.setHasCrossList(c.getInstructionalOffering().hasCrossList());
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
	
	@PreAuthorize("checkPermission('CurriculumView') or checkPermission('Reservations')")
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
	
	@PreAuthorize("checkPermission('CurriculumView') or checkPermission('Reservations')")
	public Collection<ClassAssignmentInterface.ClassAssignment> listClasses(String course) throws CurriculaException, PageAccessException {
		try {
			sLog.debug("listClasses(course='" + course + "')");
			Long s0 = System.currentTimeMillis();
			ArrayList<ClassAssignmentInterface.ClassAssignment> results = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			NameFormat nameFormat = NameFormat.fromReference(UserProperty.NameFormat.get(getSessionContext().getUser()));
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
					a.setSubpart(clazz.getSchedulingSubpart().getItypeDesc().trim());
					a.setSection(clazz.getClassSuffix(courseOffering));
					a.setExternalId(clazz.getExternalId(courseOffering));
					a.setClassNumber(clazz.getSectionNumberString(hibSession));
					if (a.getSection() == null)
						a.setSection(a.getClassNumber());
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
                    a.setCancelled(clazz.isCancelled());
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
							a.addRoom(rm.getId(), rm.getName());
						}
					}
					if (p != null && p.getRoomLocation() != null) {
						a.addRoom(p.getRoomLocation().getId(), p.getRoomLocation().getName());
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
							if (c.isMultipleMajors() && c.getMajors().size() > 1) {
								double rule = 1.0f;
								for (PosMajor m: c.getMajors()) {
									rule *= getProjection(rules, m.getCode(), acadClasf.getCode());
								}
								for (Integer ll: major2ll.values())
									proj += (float) Math.pow(rule, 1.0 / c.getMajors().size()) * ll;
							} else {
								for (Map.Entry<String, Integer> entry: major2ll.entrySet()) {
									proj += getProjection(rules, entry.getKey(), acadClasf.getCode()) * entry.getValue();
								}
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
							if (c.isMultipleMajors() && c.getMajors().size() > 1) {
								double rule = 1.0;
								for (PosMajor m: c.getMajors())
									rule *= getProjection(rules, m.getCode(), clasf.getKey());
								rule = Math.pow(rule, 1.0 / c.getMajors().size());
								for (Hashtable<Long, Integer> courses: clasf.getValue().values()) {
									for (Map.Entry<Long, Integer> course: courses.entrySet()) {
										Float total = courseTotals.get(course.getKey());
										courseTotals.put(course.getKey(), (float)(rule * course.getValue()) + (total == null ? 0.0f : total));
									}
								}
							} else {
								for (Map.Entry<String, Hashtable<Long, Integer>> major: clasf.getValue().entrySet()) {
									for (Map.Entry<Long, Integer> course: major.getValue().entrySet()) {
										Float total = courseTotals.get(course.getKey());
										courseTotals.put(course.getKey(), getProjection(rules, major.getKey(), clasf.getKey()) * course.getValue() + (total == null ? 0.0f: total));
									}
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
								if (c.isMultipleMajors() && c.getMajors().size() > 1) {
									double rule = 1.0;
									for (PosMajor m: c.getMajors())
										rule *= getProjection(rules, m.getCode(), clasf.getAcademicClassification().getCode());
									rule = Math.pow(rule, 1.0 / c.getMajors().size());
									for (Map.Entry<String, Hashtable<Long, Integer>> entry: majorCourse2ll.entrySet()) {
										Integer lastLike = entry.getValue().get(course.getCourse().getUniqueId());
										if (lastLike != null)
											proj += rule * lastLike;
									}
								} else {
									for (Map.Entry<String, Hashtable<Long, Integer>> entry: majorCourse2ll.entrySet()) {
										Integer lastLike = entry.getValue().get(course.getCourse().getUniqueId());
										proj += getProjection(rules, entry.getKey(), clasf.getAcademicClassification().getCode()) * (lastLike == null ? 0 : lastLike);
									}
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
								if (c.isMultipleMajors() && c.getMajors().size() > 1) {
									double rule = 1.0;
									for (PosMajor m: c.getMajors())
										rule *= getProjection(rules, m.getCode(), clasf.getAcademicClassification().getCode());
									rule = Math.pow(rule, 1.0 / c.getMajors().size());
									for (Map.Entry<String, Hashtable<Long, Integer>> entry: majorCourse2ll.entrySet()) {
										Integer lastLike = entry.getValue().get(courseId);
										if (lastLike != null)
											proj += rule * lastLike;
									}
								} else {
									for (Map.Entry<String, Hashtable<Long, Integer>> entry: majorCourse2ll.entrySet()) {
										Integer lastLike = entry.getValue().get(courseId);
										proj += getProjection(rules, entry.getKey(), clasf.getAcademicClassification().getCode()) * (lastLike == null ? 0 : lastLike);
									}
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
	
	protected boolean isTemplateFor(Curriculum template, Curriculum curriculum) {
		if (!curriculum.isMultipleMajors()) return false;
		if (!curriculum.getAcademicArea().equals(curriculum.getAcademicArea())) return false;
		if (curriculum.getMajors().size() <= template.getMajors().size() || template.getMajors().size() > 1) return false;
		return template.getMajors().isEmpty() || curriculum.getMajors().containsAll(template.getMajors());
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
				
				Map<Long, Map<String, Map<String, Map<String, Set<Long>>>>> course2area2major2clasf2ll = null;
				if (includeOtherStudents) {
					course2area2major2clasf2ll = loadCourseAreaMajorClasf2ll(hibSession);
				}
				
				Hashtable<String, Hashtable<String, HashMap<String, Float>>> rules = (includeOtherStudents ? getRules(hibSession) : null);
				
				List<AcademicClassification> classifications = (List<AcademicClassification>)hibSession.createQuery(
						"select c from AcademicClassification c where c.session.uniqueId = :sessionId")
						.setLong("sessionId", sessionId).setCacheable(true).list();
				
				List<Curriculum> curricula = hibSession.createQuery("from Curriculum c where c.academicArea.session.uniqueId = :sessionId")
						.setLong("sessionId", sessionId).list();
				
				Map<Long, List<Curriculum>> curriculum2children = new HashMap<Long, List<Curriculum>>();
				for (Curriculum c1: curricula) {
					for (Curriculum c2: curricula) {
						if (c1.isTemplateFor(c2)) {
							List<Curriculum> children = curriculum2children.get(c1.getUniqueId());
							if (children == null) {
								children = new ArrayList<Curriculum>();
								curriculum2children.put(c1.getUniqueId(), children);
							}
							children.add(c2);
						}
					}
				}
				
				Map<Long, List<CurriculumCourse>> course2curriculum = new Hashtable<Long, List<CurriculumCourse>>();
				for (CurriculumCourse cc: (List<CurriculumCourse>)hibSession.createQuery(
						"select cc from CurriculumCourse cc where cc.classification.curriculum.academicArea.session.uniqueId = :sessionId")
						.setLong("sessionId", sessionId)
						.setCacheable(true).list()) {
					List<CurriculumCourse> courses = course2curriculum.get(cc.getCourse().getUniqueId());
					if (courses == null) {
						courses = new ArrayList<CurriculumCourse>();
						course2curriculum.put(cc.getCourse().getUniqueId(), courses);
					}
					courses.add(cc);
				}
				
				for (CourseOffering courseOffering: (List<CourseOffering>)hibSession.createQuery(
						"select co from CourseOffering co where co.subjectArea.session.uniqueId = :sessionId")
						.setLong("sessionId", sessionId)
						.setCacheable(true).list()) {
					
					Integer oldDemand = courseOffering.getDemand();

					Map<String, Map<String, Map<String, Set<Long>>>> area2major2clasf2ll = (course2area2major2clasf2ll == null ? null : course2area2major2clasf2ll.get(courseOffering.getUniqueId()));
					
					List<CurriculumCourse> courses = course2curriculum.get(courseOffering.getUniqueId());
					int demand = 0;
					
					Map<Long, Integer> demands = new HashMap<Long, Integer>();
					Map<Long, Integer> defaultDemands = new HashMap<Long, Integer>();
					
					TreeSet<Curriculum> related = new TreeSet<Curriculum>(new Comparator<Curriculum>() {
						public int compare(Curriculum c1, Curriculum c2) {
							// multiple majors first
							if (c1.isMultipleMajors() != c2.isMultipleMajors())
								return c1.isMultipleMajors() ? -1 : 1;
							// more majors first
							if (c1.getMajors().size() != c2.getMajors().size())
								return c1.getMajors().size() > c2.getMajors().size() ? -1 : 1;
							return c1.compareTo(c2);
						}
					});

					if (courses != null)
						for (CurriculumCourse course: courses) {
							CurriculumClassification clasf = course.getClassification();
							Curriculum curriculum = clasf.getCurriculum();
							
							demands.put(clasf.getUniqueId(), (int) Math.round(clasf.getNrStudents() * ((double) course.getPercShare())));
							
							List<Curriculum> children = curriculum2children.get(curriculum.getUniqueId());
							if (children != null && !children.isEmpty()) {
								for (Curriculum child: children) {
									CurriculumClassification childClasf = null;
									for (CurriculumClassification x: child.getClassifications()) {
										if (x.getAcademicClassification().equals(clasf.getAcademicClassification()) && x.getNrStudents() > 0) {
											childClasf = x;
										}
									}
									if (childClasf == null) continue;
									
									Integer previous = defaultDemands.get(childClasf.getUniqueId());
									int current = (int) Math.round(childClasf.getNrStudents() * ((double) course.getPercShare()));
									if (previous == null || current > previous)
										defaultDemands.put(childClasf.getUniqueId(), current);
								}
							}
							related.add(curriculum);
						}
					
					for (Integer d: demands.values())
						demand += d;
					
					for (Map.Entry<Long, Integer> entry: defaultDemands.entrySet())
						if (!demands.containsKey(entry.getKey()))
							demand += entry.getValue();
					
					if (area2major2clasf2ll != null) {
						for (Curriculum curriculum: related) {
							Map<String, Map<String, Set<Long>>> major2clasf2ll = area2major2clasf2ll.get(curriculum.getAcademicArea().getAcademicAreaAbbreviation());
							if (major2clasf2ll != null) {
								if (curriculum.getMajors().isEmpty()) {
									if (curriculum.isMultipleMajors()) {
										Map<String, Set<Long>> clasf2ll = major2clasf2ll.get("");
										if (clasf2ll != null)
											for (AcademicClassification cc: classifications)
												clasf2ll.remove(cc.getCode());
									} else {
										major2clasf2ll.clear();
									}
								} else {
									if (curriculum.isMultipleMajors()) {
										for (AcademicClassification cc: classifications) {
											Set<Long> s = null;
											for (PosMajor m: curriculum.getMajors()) {
												Map<String, Set<Long>> clasf2ll = major2clasf2ll.get(m.getCode());
												Set<Long> e = (clasf2ll == null ? null : clasf2ll.get(cc.getCode()));
												if (e == null) {
													if (s == null)
														s = new HashSet<Long>();
													else
														s.clear();
												} else {
													if (s == null)
														s = new HashSet<Long>(e);
													else
														s.retainAll(e);
												}
											}
											if (s != null && !s.isEmpty()) {
												for (PosMajor m: curriculum.getMajors()) {
													Map<String, Set<Long>> clasf2ll = major2clasf2ll.get(m.getCode());
													Set<Long> e = (clasf2ll == null ? null : clasf2ll.get(cc.getCode()));
													if (e != null) {
														e.removeAll(s);
														if (e.isEmpty())
															clasf2ll.remove(cc.getCode());
													}
												}
											}
										}
									} else {
										for (PosMajor m: curriculum.getMajors())
											major2clasf2ll.remove(m.getCode());
									}
								}
							}
						}

						for (Map.Entry<String, Map<String, Map<String, Set<Long>>>> areaEmajor2clasf2ll: area2major2clasf2ll.entrySet()) {
							for (Map.Entry<String, Map<String, Set<Long>>> majorEclasf2ll: areaEmajor2clasf2ll.getValue().entrySet()) {
								for (Map.Entry<String, Set<Long>> clasfEll: majorEclasf2ll.getValue().entrySet()) {
									demand += Math.round(getProjection(rules == null ? null : rules.get(areaEmajor2clasf2ll.getKey()), majorEclasf2ll.getKey(), clasfEll.getKey()) * clasfEll.getValue().size());	
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
					
					Map<String, Map<String, Map<String, Set<Long>>>> area2major2clasf2ll =  null;
					if (includeOtherStudents) {
						area2major2clasf2ll = loadAreaMajorClasf2ll(hibSession, courseOffering.getUniqueId());
					}
					
					Hashtable<String, Hashtable<String, HashMap<String, Float>>> rules = (includeOtherStudents ? getRules(hibSession) : null);
					
					Map<Long, Integer> demands = new HashMap<Long, Integer>();
					Map<Long, Integer> defaultDemands = new HashMap<Long, Integer>();
					
					TreeSet<Curriculum> curricula = new TreeSet<Curriculum>(new Comparator<Curriculum>() {
						public int compare(Curriculum c1, Curriculum c2) {
							// multiple majors first
							if (c1.isMultipleMajors() != c2.isMultipleMajors())
								return c1.isMultipleMajors() ? -1 : 1;
							// more majors first
							if (c1.getMajors().size() != c2.getMajors().size())
								return c1.getMajors().size() > c2.getMajors().size() ? -1 : 1;
							return c1.compareTo(c2);
						}
					});
					
					for (CurriculumCourse course: (List<CurriculumCourse>)hibSession.createQuery(
							"select cc from CurriculumCourse cc where cc.course.uniqueId = :courseId")
							.setLong("courseId", courseOffering.getUniqueId())
							.setCacheable(true).list()) {
						CurriculumClassification clasf = course.getClassification();
						Curriculum curriculum = clasf.getCurriculum();
						
						demands.put(clasf.getUniqueId(), (int) Math.round(clasf.getNrStudents() * ((double) course.getPercShare())));
						
						List<Curriculum> children = null;
						if (curriculum.getMajors().isEmpty()) {
							children = hibSession.createQuery("select c from Curriculum c where c.academicArea.uniqueId = :areaId and c.multipleMajors = true and " +
									"(select count(x) from CurriculumCourse x where x.course.uniqueId = :courseId and x.classification.curriculum.uniqueId = c.uniqueId) = 0"
									).setLong("areaId", curriculum.getAcademicArea().getUniqueId()).setLong("courseId", course.getCourse().getUniqueId()).setCacheable(true).list();
						} else if (curriculum.getMajors().size() == 1) {
							children = hibSession.createQuery("select c from Curriculum c inner join c.majors m where c.academicArea.uniqueId = :areaId and m.uniqueId = :majorId and c.multipleMajors = true and " +
									"(select count(x) from CurriculumCourse x where x.course.uniqueId = :courseId and x.classification.curriculum.uniqueId = c.uniqueId) = 0"
									).setLong("areaId", curriculum.getAcademicArea().getUniqueId()).setLong("majorId", curriculum.getMajors().iterator().next().getUniqueId())
									.setLong("courseId", course.getCourse().getUniqueId()).setCacheable(true).list();
						}
						if (children != null && !children.isEmpty()) {
							for (Curriculum child: children) {
								CurriculumClassification childClasf = null;
								for (CurriculumClassification x: child.getClassifications()) {
									if (x.getAcademicClassification().equals(clasf.getAcademicClassification()) && x.getNrStudents() > 0) {
										childClasf = x;
									}
								}
								if (childClasf == null) continue;
								
								Integer previous = defaultDemands.get(childClasf.getUniqueId());
								int current = (int) Math.round(childClasf.getNrStudents() * ((double) course.getPercShare()));
								if (previous == null || current > previous)
									defaultDemands.put(childClasf.getUniqueId(), current);
							}
						}
						curricula.add(curriculum);
					}
					
					int demand = 0;
					for (Integer d: demands.values())
						demand += d;
					
					for (Map.Entry<Long, Integer> entry: defaultDemands.entrySet())
						if (!demands.containsKey(entry.getKey()))
							demand += entry.getValue();
					
					if (area2major2clasf2ll != null) {
						List<AcademicClassification> classifications = (List<AcademicClassification>)hibSession.createQuery(
								"select c from AcademicClassification c where c.session.uniqueId = :sessionId")
								.setLong("sessionId", offering.getSessionId()).setCacheable(true).list();
						for (Curriculum curriculum: curricula) {
							Map<String, Map<String, Set<Long>>> major2clasf2ll = area2major2clasf2ll.get(curriculum.getAcademicArea().getAcademicAreaAbbreviation());
							if (major2clasf2ll != null) {
								if (curriculum.getMajors().isEmpty()) {
									if (curriculum.isMultipleMajors()) {
										Map<String, Set<Long>> clasf2ll = major2clasf2ll.get("");
										if (clasf2ll != null)
											for (AcademicClassification cc: classifications)
												clasf2ll.remove(cc.getCode());
									} else {
										major2clasf2ll.clear();
									}
								} else {
									if (curriculum.isMultipleMajors()) {
										for (AcademicClassification cc: classifications) {
											Set<Long> s = null;
											for (PosMajor m: curriculum.getMajors()) {
												Map<String, Set<Long>> clasf2ll = major2clasf2ll.get(m.getCode());
												Set<Long> e = (clasf2ll == null ? null : clasf2ll.get(cc.getCode()));
												if (e == null) {
													if (s == null)
														s = new HashSet<Long>();
													else
														s.clear();
												} else {
													if (s == null)
														s = new HashSet<Long>(e);
													else
														s.retainAll(e);
												}
											}
											if (s != null && !s.isEmpty()) {
												for (PosMajor m: curriculum.getMajors()) {
													Map<String, Set<Long>> clasf2ll = major2clasf2ll.get(m.getCode());
													Set<Long> e = (clasf2ll == null ? null : clasf2ll.get(cc.getCode()));
													if (e != null) {
														e.removeAll(s);
														if (e.isEmpty())
															clasf2ll.remove(cc.getCode());
													}
												}
											}
										}
									} else {
										for (PosMajor m: curriculum.getMajors())
											major2clasf2ll.remove(m.getCode());
									}
								}
							}
						}

						for (Map.Entry<String, Map<String, Map<String, Set<Long>>>> areaEmajor2clasf2ll: area2major2clasf2ll.entrySet()) {
							for (Map.Entry<String, Map<String, Set<Long>>> majorEclasf2ll: areaEmajor2clasf2ll.getValue().entrySet()) {
								for (Map.Entry<String, Set<Long>> clasfEll: majorEclasf2ll.getValue().entrySet()) {
									demand += Math.round(getProjection(rules == null ? null : rules.get(areaEmajor2clasf2ll.getKey()), majorEclasf2ll.getKey(), clasfEll.getKey()) * clasfEll.getValue().size());	
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
	protected Long getAcademicSessionId() {
		return getSessionContext().getUser().getCurrentAcademicSessionId();
	}
	
	private List<Curriculum> findAllCurricula(org.hibernate.Session hibSession) {
		return hibSession.createQuery(
				"select distinct c from Curriculum c where c.department.session.uniqueId = :sessionId")
				.setLong("sessionId", getAcademicSessionId())
				.setCacheable(true).list();
	}
	
	private Hashtable<Long, Integer> loadClasf2enrl(org.hibernate.Session hibSession, Curriculum c) {
		List<Object[]> lines = null;
		String select = "a.academicClassification.uniqueId, count(distinct s)";
		String from = "StudentClassEnrollment e inner join e.student s inner join s.areaClasfMajors a";
		String where = "s.session.uniqueId = :sessionId and a.academicArea.uniqueId = :areaId";
		String group = "a.academicClassification.uniqueId";
		if (c.getMajors().isEmpty()) {
			// students with all majors
			if (!c.isMultipleMajors())
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId())
					.setCacheable(true).list();
		} else if (!c.isMultipleMajors() || c.getMajors().size() == 1) {
			// students with one major
			List<Long> majorIds = new ArrayList<Long>();
			for (PosMajor major: c.getMajors())
				majorIds.add(major.getUniqueId());
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and a.major.uniqueId in :majorIds group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId())
					.setParameterList("majorIds", majorIds)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			Map<String, Long> params = new HashMap<String, Long>();
			int idx = 0;
			for (PosMajor major: c.getMajors()) {
				if (idx == 0) {
					where += " and a.major.uniqueId = :m" + idx;
				} else {
					from += " inner join s.areaClasfMajors a" + idx;
					where += " and a" + idx + ".academicArea.uniqueId = :areaId and a" + idx + ".major.uniqueId = :m" + idx;
				}
				params.put("m" + idx, major.getUniqueId());
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId());
			for (Map.Entry<String, Long> e: params.entrySet())
				q.setLong(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Hashtable<Long, Integer> clasf2enrl = new Hashtable<Long, Integer>();
		if (lines != null)
			for (Object[] o : lines) {
				Long clasfId = (Long)o[0];
				int enrl = ((Number)o[1]).intValue();
				if (clasfId != null)
					clasf2enrl.put(clasfId, enrl);
			}
		return clasf2enrl;
	}
	
	private Hashtable<Long, Set<Long>> loadClasf2enrl(org.hibernate.Session hibSession, Long acadAreaId, Collection<Long> majors, boolean multipleMajors) {
		List<Object[]> lines = null;
		String select = "a.academicClassification.uniqueId, s.uniqueId";
		String from = "StudentClassEnrollment e inner join e.student s inner join s.areaClasfMajors a";
		String where = "s.session.uniqueId = :sessionId and a.academicArea.uniqueId = :areaId";
		if (majors.isEmpty()) {
			// students with all majors
			if (!multipleMajors)
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where)
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId)
					.setCacheable(true).list();
		} else if (!multipleMajors || majors.size() == 1) {
			// students with one major
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and a.major.uniqueId in :majorIds")
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId)
					.setParameterList("majorIds", majors)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			Map<String, Long> params = new HashMap<String, Long>();
			int idx = 0;
			for (Long major: majors) {
				if (idx == 0) {
					where += " and a.major.uniqueId = :m" + idx;
				} else {
					from += " inner join s.areaClasfMajors a" + idx;
					where += " and a" + idx + ".academicArea.uniqueId = :areaId and a" + idx + ".major.uniqueId = :m" + idx;
				}
				params.put("m" + idx, major);
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where)
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId);
			for (Map.Entry<String, Long> e: params.entrySet())
				q.setLong(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Hashtable<Long, Set<Long>> clasf2enrl = new Hashtable<Long, Set<Long>>();
		if (lines != null)
			for (Object[] o : lines) {
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
		List<Object[]> lines = null;
		String select = "f.code, m.code, count(distinct s)";
		String from = "LastLikeCourseDemand x inner join x.student s inner join s.areaClasfMajors a inner join a.academicClassification f inner join a.major m";
		String where = "x.subjectArea.session.uniqueId = :sessionId and a.academicArea.academicAreaAbbreviation = :acadAbbv";
		String group = "f.code, m.code";
		if (c.getMajors().isEmpty()) {
			// students with all majors
			if (!c.isMultipleMajors()) {
				select = "f.code, '', count(distinct s)";
				group = "f.code";
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
						.setLong("sessionId", c.getAcademicArea().getSessionId()).setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation())
						.setCacheable(true).list();
			}
		} else if (!c.isMultipleMajors() || c.getMajors().size() == 1) {
			// students with one major
			List<String> majorCodes = new ArrayList<String>();
			for (PosMajor major: c.getMajors())
				majorCodes.add(major.getCode());
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and m.code in :majorCodes group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation())
					.setParameterList("majorCodes", majorCodes)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			select = "f.code, '', count(distinct s)";
			group = "f.code";
			Map<String, String> params = new HashMap<String, String>();
			int idx = 0;
			for (PosMajor major: c.getMajors()) {
				if (idx == 0) {
					where += " and m.code = :m" + idx;
				} else {
					from += " inner join s.areaClasfMajors a" + idx;
					where += " and a" + idx + ".academicArea.academicAreaAbbreviation = :acadAbbv and a" + idx + ".major.code = :m" + idx;
				}
				params.put("m" + idx, major.getCode());
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation());
			for (Map.Entry<String, String> e: params.entrySet())
				q.setString(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Hashtable<String, Hashtable<String, Integer>> clasfMajor2ll = new Hashtable<String, Hashtable<String,Integer>>();
		if (lines != null)
			for (Object[] o: lines) {
				String clasfCode = (String)o[0];
				if (clasfCode == null) continue;
				String majorCode = (String)o[1];
				if (majorCode == null) majorCode = "";
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
		List<Object[]> lines = null;
		String select = "f.code, count(distinct s)";
		String from = "LastLikeCourseDemand x inner join x.student s inner join s.areaClasfMajors a inner join a.academicClassification f";
		String where = "x.subjectArea.session.uniqueId = :sessionId and a.academicArea.academicAreaAbbreviation = :acadAbbv";
		String group = "f.code";
		if (c.getMajors().isEmpty()) {
			// students with all majors
			if (!c.isMultipleMajors())
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation())
					.setCacheable(true).list();
		} else if (!c.isMultipleMajors() || c.getMajors().size() == 1) {
			// students with one major
			List<String> majorCodes = new ArrayList<String>();
			for (PosMajor major: c.getMajors())
				majorCodes.add(major.getCode());
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and a.major.code in :majorCodes group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation())
					.setParameterList("majorCodes", majorCodes)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			Map<String, String> params = new HashMap<String, String>();
			int idx = 0;
			for (PosMajor major: c.getMajors()) {
				if (idx == 0) {
					where += " and a.major.code = :m" + idx;
				} else {
					from += " inner join s.areaClasfMajors a" + idx;
					where += " and a" + idx + ".academicArea.academicAreaAbbreviation = :acadAbbv and a" + idx + ".major.code = :m" + idx;
				}
				params.put("m" + idx, major.getCode());
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation());
			for (Map.Entry<String, String> e: params.entrySet())
				q.setString(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Hashtable<String, Integer> clasf2ll = new Hashtable<String, Integer>();
		if (lines != null)
			for (Object[] o: lines) {
				String clasfCode = (String)o[0];
				int enrl = ((Number)o[1]).intValue();
				if (clasfCode != null)
					clasf2ll.put(clasfCode, enrl);
			}
		return clasf2ll;
	}
	
	private Hashtable<String, HashMap<String, Set<Long>>> loadClasfMajor2ll(org.hibernate.Session hibSession, String acadAreaAbbv, Collection<PosMajor> majors, boolean multipleMajors) {
		List<Object[]> lines = null;
		String select = "f.code, m.code, s.uniqueId";
		String from = "LastLikeCourseDemand x inner join x.student s inner join s.areaClasfMajors a inner join a.academicClassification f inner join a.major m";
		String where = "x.subjectArea.session.uniqueId = :sessionId and a.academicArea.academicAreaAbbreviation = :acadAbbv";
		if (majors.isEmpty()) {
			// students with all majors
			if (!multipleMajors) {
				select = "f.code, '', s.uniqueId";
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where)
						.setLong("sessionId", getAcademicSessionId()).setString("acadAbbv", acadAreaAbbv)
						.setCacheable(true).list();
			}
		} else if (!multipleMajors || majors.size() == 1) {
			List<String> codes = new ArrayList<String>();
			for (PosMajor major: majors)
				codes.add(major.getCode());
			// students with one major
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and m.code in :majorCodes")
					.setLong("sessionId", getAcademicSessionId()).setString("acadAbbv", acadAreaAbbv)
					.setParameterList("majorCodes", codes)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			select = "f.code, '', s.uniqueId";
			Map<String, String> params = new HashMap<String, String>();
			int idx = 0;
			for (PosMajor major: majors) {
				if (idx == 0) {
					where += " and m.code = :m" + idx;
				} else {
					from += " inner join s.areaClasfMajors a" + idx;
					where += " and a" + idx + ".academicArea.academicAreaAbbreviation = :acadAbbv and a" + idx + ".major.code = :m" + idx;
				}
				params.put("m" + idx, major.getCode());
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where)
					.setLong("sessionId", getAcademicSessionId()).setString("acadAbbv", acadAreaAbbv);
			for (Map.Entry<String, String> e: params.entrySet())
				q.setString(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Hashtable<String, HashMap<String, Set<Long>>> clasf2ll = new Hashtable<String, HashMap<String, Set<Long>>>();
		if (lines != null)
			for (Object[] o : lines) {
				String clasfCode = (String)o[0];
				String majorCode = (String)o[1];
				if (majorCode == null) majorCode = "";
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
		List<Object[]> lines = null;
		String select = "a.academicClassification.uniqueId, e.courseOffering.uniqueId, count(distinct s)";
		String from = "StudentClassEnrollment e inner join e.student s inner join s.areaClasfMajors a inner join a.major m";
		String where = "s.session.uniqueId = :sessionId and a.academicArea.uniqueId = :areaId";
		String group = "a.academicClassification.uniqueId, e.courseOffering.uniqueId";
		if (c.getMajors().isEmpty()) {
			// students with no major
			if (!c.isMultipleMajors())
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId())
					.setCacheable(true).list();
		} else if (!c.isMultipleMajors() || c.getMajors().size() == 1) {
			// students with one major
			List<Long> majorIds = new ArrayList<Long>();
			for (PosMajor major: c.getMajors())
				majorIds.add(major.getUniqueId());
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and m.uniqueId in :majorIds group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId())
					.setParameterList("majorIds", majorIds)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			Map<String, Long> params = new HashMap<String, Long>();
			int idx = 0;
			for (PosMajor major: c.getMajors()) {
				if (idx == 0) {
					where += " and m.uniqueId = :m" + idx;
				} else {
					from += " inner join s.areaClasfMajors a" + idx;
					where += " and a" + idx + ".academicArea.uniqueId = :areaId and a" + idx + ".major.uniqueId = :m" + idx;
					
				}
				params.put("m" + idx, major.getUniqueId());
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId());
			for (Map.Entry<String, Long> e: params.entrySet())
				q.setLong(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Hashtable<Long, Hashtable<Long, Integer>> clasf2course2enrl = new Hashtable<Long, Hashtable<Long,Integer>>();
		if (lines != null)
			for (Object[] o : lines) {
				Long clasfId = (Long)o[0];
				if (clasfId == null) continue;
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
	
	private Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> loadClasfCourse2enrl(org.hibernate.Session hibSession, Long acadAreaId, Collection<Long> majors, boolean multipleMajors) {
		List<Object[]> lines = null;
		String select = "a.academicClassification.uniqueId, e.courseOffering.uniqueId, e.courseOffering.subjectArea.subjectAreaAbbreviation || ' ' || e.courseOffering.courseNbr, s.uniqueId";
		String from = "StudentClassEnrollment e inner join e.student s inner join s.areaClasfMajors a inner join a.major m";
		String where = "s.session.uniqueId = :sessionId and a.academicArea.uniqueId = :areaId";
		if (majors.isEmpty()) {
			// students with no major
			if (!multipleMajors)
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where)
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId)
					.setCacheable(true).list();
		} else if (!multipleMajors || majors.size() == 1) {
			// students with one major
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and m.uniqueId in :majorIds")
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId)
					.setParameterList("majorIds", majors)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			Map<String, Long> params = new HashMap<String, Long>();
			int idx = 0;
			for (Long major: majors) {
				if (idx == 0) {
					where += " and m.uniqueId = :m" + idx;
				} else {
					from += " inner join s.areaClasfMajors a" + idx;
					where += " and a" + idx + ".academicArea.uniqueId = :areaId and a" + idx + ".major.uniqueId = :m" + idx;
				}
				params.put("m" + idx, major);
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where)
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId);
			for (Map.Entry<String, Long> e: params.entrySet())
				q.setLong(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> clasf2course2enrl = new Hashtable<Long, Hashtable<CourseInterface,Set<Long>>>();
		if (lines != null)
			for (Object[] o : lines) {
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
		String select = "f.code, m.code, co.uniqueId, count(distinct s)";
		String from = "CourseOffering co, LastLikeCourseDemand x inner join x.student s inner join s.areaClasfMajors a inner join a.academicClassification f inner join a.major m";
		String[] checks = new String[] {
			"x.subjectArea.session.uniqueId = :sessionId and a.academicArea.academicAreaAbbreviation = :acadAbbv and co.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is not null and co.permId=x.coursePermId",
			"x.subjectArea.session.uniqueId = :sessionId and a.academicArea.academicAreaAbbreviation = :acadAbbv and co.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is null and co.courseNbr=x.courseNbr",
			"x.subjectArea.session.uniqueId = :sessionId and a.academicArea.academicAreaAbbreviation = :acadAbbv and co.demandOffering.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is not null and co.demandOffering.permId=x.coursePermId",
			"x.subjectArea.session.uniqueId = :sessionId and a.academicArea.academicAreaAbbreviation = :acadAbbv and co.demandOffering.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is null and co.demandOffering.courseNbr=x.courseNbr"
		};
		String group = "f.code, m.code, co.uniqueId";
		Hashtable<String, Hashtable<String, Hashtable<Long, Integer>>> clasfMajor2course2ll = new Hashtable<String, Hashtable<String, Hashtable<Long,Integer>>>();
		for (String where: checks) {
			List<Object[]> lines = null;
			if (c.getMajors().isEmpty()) {
				// students with no major
				if (!c.isMultipleMajors()) {
					select = "f.code, '', co.uniqueId, count(distinct s)";
					group = "f.code, co.uniqueId";
					lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
							.setLong("sessionId", c.getAcademicArea().getSessionId()).setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation())
							.setCacheable(true).list();
				}
			} else if (!c.isMultipleMajors() || c.getMajors().size() == 1) {
				// students with one major
				List<String> majorCodes = new ArrayList<String>();
				for (PosMajor major: c.getMajors())
					majorCodes.add(major.getCode());
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and m.code in :majorCodes group by " + group)
						.setLong("sessionId", c.getAcademicArea().getSessionId()).setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation())
						.setParameterList("majorCodes", majorCodes)
						.setCacheable(true).list();
			} else {
				// students with multiple majors
				select = "f.code, '', co.uniqueId, count(distinct s)";
				group = "f.code, co.uniqueId";
				Map<String, String> params = new HashMap<String, String>();
				int idx = 0;
				for (PosMajor major: c.getMajors()) {
					if (idx == 0) {
						where += " and m.code = :m" + idx;
					} else {
						from += " inner join s.areaClasfMajors a" + idx;
						where += " and a" + idx + ".academicArea.academicAreaAbbreviation = :acadAbbv and a" + idx + ".major.code = :m" + idx;
					}
					params.put("m" + idx, major.getCode());
					idx ++;
				}
				org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
						.setLong("sessionId", c.getAcademicArea().getSessionId()).setString("acadAbbv", c.getAcademicArea().getAcademicAreaAbbreviation());
				for (Map.Entry<String, String> e: params.entrySet())
					q.setString(e.getKey(), e.getValue());
				lines = q.setCacheable(true).list();
			}
			if (lines != null)
				for (Object[] o: lines) {
					String clasfCode = (String)o[0];
					if (clasfCode == null) continue;
					String majorCode = (String)o[1];
					if (majorCode == null) majorCode = "";
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
		}
		return clasfMajor2course2ll;
	}
	
	private Hashtable<String, Hashtable<CourseInterface, HashMap<String, Set<Long>>>> loadClasfCourseMajor2ll(org.hibernate.Session hibSession, String acadAreaAbbv, Collection<PosMajor> majors, boolean multipleMajors) {
		String select = "f.code, co.uniqueId, co.subjectArea.subjectAreaAbbreviation || ' ' || co.courseNbr, m.code, s.uniqueId";
		String from = "CourseOffering co, LastLikeCourseDemand x inner join x.student s inner join s.areaClasfMajors a inner join a.academicClassification f inner join a.major m";
		String[] checks = new String[] {
				"x.subjectArea.session.uniqueId = :sessionId and a.academicArea.academicAreaAbbreviation = :acadAbbv and co.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is not null and co.permId=x.coursePermId",
				"x.subjectArea.session.uniqueId = :sessionId and a.academicArea.academicAreaAbbreviation = :acadAbbv and co.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is null and co.courseNbr=x.courseNbr",
				"x.subjectArea.session.uniqueId = :sessionId and a.academicArea.academicAreaAbbreviation = :acadAbbv and co.demandOffering.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is not null and co.demandOffering.permId=x.coursePermId",
				"x.subjectArea.session.uniqueId = :sessionId and a.academicArea.academicAreaAbbreviation = :acadAbbv and co.demandOffering.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is null and co.demandOffering.courseNbr=x.courseNbr"
		};
		Hashtable<String, Hashtable<CourseInterface, HashMap<String, Set<Long>>>> clasf2course2ll = new Hashtable<String, Hashtable<CourseInterface,HashMap<String,Set<Long>>>>();
		for (String where: checks) {
			List<Object[]> lines = new ArrayList<Object[]>();
			if (majors.isEmpty()) {
				// students with no major
				if (!multipleMajors) {
					select = "f.code, co.uniqueId, co.subjectArea.subjectAreaAbbreviation || ' ' || co.courseNbr, '', s.uniqueId";
					lines = hibSession.createQuery("select " + select + " from " + from + " where " + where)
							.setLong("sessionId", getAcademicSessionId()).setString("acadAbbv", acadAreaAbbv)
							.setCacheable(true).list();
				}
			} else if (!multipleMajors || majors.size() == 1) {
				List<String> codes = new ArrayList<String>();
				for (PosMajor major: majors)
					codes.add(major.getCode());
				// students with one major
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and m.code in :majorCodes")
						.setLong("sessionId", getAcademicSessionId()).setString("acadAbbv", acadAreaAbbv)
						.setParameterList("majorCodes", codes)
						.setCacheable(true).list();
			} else {
				// students with multiple majors
				select = "f.code, co.uniqueId, co.subjectArea.subjectAreaAbbreviation || ' ' || co.courseNbr, '', s.uniqueId";
				Map<String, String> params = new HashMap<String, String>();
				int idx = 0;
				for (PosMajor major: majors) {
					if (idx == 0) {
						where += " and m.code = :m" + idx;
					} else {
						from += " inner join s.areaClasfMajors a" + idx;
						where += " and a" + idx + ".academicArea.academicAreaAbbreviation = :acadAbbv and a" + idx + ".major.code = :m" + idx;
					}
					params.put("m" + idx, major.getCode());
					idx ++;
				}
				org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where)
						.setLong("sessionId", getAcademicSessionId()).setString("acadAbbv", acadAreaAbbv);
				for (Map.Entry<String, String> e: params.entrySet())
					q.setString(e.getKey(), e.getValue());
				lines = q.setCacheable(true).list();
			}
			if (lines != null)
				for (Object[] o : lines) {
					String clasfCode = (String)o[0];
					Long courseId = (Long)o[1];
					String courseName = (String)o[2];
					String majorCode = (String)o[3];
					if (majorCode == null) majorCode = "";
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
					if (multipleMajors && majors.size() > 1) {
						for (PosMajor major: majors) {
							Set<Long> students = major2students.get(major.getCode());
							if (students == null) {
								students = new HashSet<Long>();
								major2students.put(major.getCode(), students);
							}
							students.add(studentId);
						}
					} else {
						Set<Long> students = major2students.get(majorCode);
						if (students == null) {
							students = new HashSet<Long>();
							major2students.put(majorCode, students);
						}
						students.add(studentId);
					}
				}			
		}
		
		return clasf2course2ll;
	}
	
	private Map<Long, Map<Long, Map<Long, Set<Long>>>> loadAreaMajorClasf2enrl(org.hibernate.Session hibSession, Long courseOfferingId) {
		Map<Long, Map<Long, Map<Long, Set<Long>>>> area2major2clasf2enrl = new HashMap<Long, Map<Long, Map<Long, Set<Long>>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select distinct a.academicArea.uniqueId, m.uniqueId, a.academicClassification.uniqueId, e.student.uniqueId " +
				"from StudentClassEnrollment e inner join e.student.areaClasfMajors a left outer join a.major m where " +
				"e.courseOffering.uniqueId = :courseId")
				.setLong("courseId", courseOfferingId)
				.setCacheable(true).list()) {
			Long areaId = (Long)o[0];
			Long majorId = (Long)o[1];
			if (majorId == null) majorId = -1l;
			Long clasfId = (Long)o[2];
			Long studentId = (Long)o[3];
			Map<Long, Map<Long, Set<Long>>> major2clasf2enrl = area2major2clasf2enrl.get(areaId);
			if (major2clasf2enrl == null) {
				major2clasf2enrl = new HashMap<Long, Map<Long,Set<Long>>>();
				area2major2clasf2enrl.put(areaId, major2clasf2enrl);
			}
			Map<Long, Set<Long>> clasf2enrl = major2clasf2enrl.get(majorId);
			if (clasf2enrl == null) {
				clasf2enrl = new HashMap<Long, Set<Long>>();
				major2clasf2enrl.put(majorId, clasf2enrl);
			}
			Set<Long> enrl = clasf2enrl.get(clasfId);
			if (enrl == null) {
				enrl = new HashSet<Long>();
				clasf2enrl.put(clasfId, enrl);
			}
			enrl.add(studentId);
		}
		return area2major2clasf2enrl;
	}
	
	private Map<String, Map<String, Map<String, Set<Long>>>> loadAreaMajorClasf2ll(org.hibernate.Session hibSession, Long courseOfferingId) {
		Map<String, Map<String, Map<String, Set<Long>>>> area2major2clasf2ll = new HashMap<String, Map<String, Map<String, Set<Long>>>>();
		String[] checks = new String[] {
				"x.subjectArea.session.uniqueId = :sessionId and co.uniqueId = :courseId and co.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is not null and co.permId=x.coursePermId",
				"x.subjectArea.session.uniqueId = :sessionId and co.uniqueId = :courseId and co.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is null and co.courseNbr=x.courseNbr",
				"x.subjectArea.session.uniqueId = :sessionId and co.uniqueId = :courseId and co.demandOffering.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is not null and co.demandOffering.permId=x.coursePermId",
				"x.subjectArea.session.uniqueId = :sessionId and co.uniqueId = :courseId and co.demandOffering.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is null and co.demandOffering.courseNbr=x.courseNbr"
		};
		for (String where: checks) {
			for (Object[] o : (List<Object[]>)hibSession.createQuery(
					"select distinct r.academicAreaAbbreviation, m.code, f.code, s.uniqueId from " +
					"LastLikeCourseDemand x inner join x.student s inner join s.areaClasfMajors a left outer join a.major m " +
					"inner join a.academicClassification f inner join a.academicArea r, CourseOffering co where " + where)
					.setLong("sessionId", getAcademicSessionId())
					.setLong("courseId", courseOfferingId)
					.setCacheable(true).list()) {
				String areaAbbv = (String)o[0];
				String majorCode = (String)o[1];
				if (majorCode == null) majorCode = "";
				String clasfCode = (String)o[2];
				Long studentId = (Long)o[3];
				Map<String, Map<String, Set<Long>>> major2clasf2ll = area2major2clasf2ll.get(areaAbbv);
				if (major2clasf2ll == null) {
					major2clasf2ll = new HashMap<String, Map<String, Set<Long>>>();
					area2major2clasf2ll.put(areaAbbv, major2clasf2ll);
				}
				Map<String, Set<Long>> clasf2ll = major2clasf2ll.get(majorCode);
				if (clasf2ll == null) {
					clasf2ll = new HashMap<String, Set<Long>>();
					major2clasf2ll.put(majorCode, clasf2ll);
				}
				Set<Long> ll = clasf2ll.get(clasfCode);
				if (ll == null) {
					ll = new HashSet<Long>();
					clasf2ll.put(clasfCode, ll);
				}
				ll.add(studentId);
			}
		}
		return area2major2clasf2ll;
	}
	
	private Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> loadAreaMajorClasf2ll(org.hibernate.Session hibSession) {
		Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> area2major2clasf2ll = new Hashtable<String, Hashtable<String,Hashtable<String,Integer>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicAreaAbbreviation, m.code, f.code, count(distinct s) from LastLikeCourseDemand x inner join x.student s " +
				"inner join s.areaClasfMajors ac inner join ac.academicClassification f inner join ac.academicArea a " +
				"inner join ac.major m where x.subjectArea.session.uniqueId = :sessionId " +
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
	
	private Map<Long, Map<String, Map<String, Map<String, Set<Long>>>>> loadCourseAreaMajorClasf2ll(org.hibernate.Session hibSession) {
		Map<Long, Map<String, Map<String, Map<String, Set<Long>>>>> course2area2major2clasf2ll = new HashMap<Long, Map<String, Map<String, Map<String, Set<Long>>>>>();
		String[] checks = new String[] {
				"x.subjectArea.session.uniqueId = :sessionId and co.subjectArea.session.uniqueId = :sessionId and co.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is not null and co.permId=x.coursePermId",
				"x.subjectArea.session.uniqueId = :sessionId and co.subjectArea.session.uniqueId = :sessionId and co.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is null and co.courseNbr=x.courseNbr",
				"x.subjectArea.session.uniqueId = :sessionId and co.subjectArea.session.uniqueId = :sessionId and co.demandOffering.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is not null and co.demandOffering.permId=x.coursePermId",
				"x.subjectArea.session.uniqueId = :sessionId and co.subjectArea.session.uniqueId = :sessionId and co.demandOffering.subjectArea.uniqueId = x.subjectArea.uniqueId and x.coursePermId is null and co.demandOffering.courseNbr=x.courseNbr",
		};
		for (String where: checks) {
			for (Object[] o : (List<Object[]>)hibSession.createQuery(
					"select distinct co.uniqueId, r.academicAreaAbbreviation, m.code, f.code, s.uniqueId from " +
					"LastLikeCourseDemand x inner join x.student s inner join s.areaClasfMajors a inner join a.major m " +
					"inner join a.academicClassification f inner join a.academicArea r, CourseOffering co where " + where)
					.setLong("sessionId", getAcademicSessionId())
					.setCacheable(true).list()) {
				Long courseId = (Long)o[0];
				String areaAbbv = (String)o[1];
				String majorCode = (String)o[2];
				if (majorCode == null) majorCode = "";
				String clasfCode = (String)o[3];
				Long studentId = (Long)o[4];
				Map<String, Map<String, Map<String, Set<Long>>>> area2major2clasf2ll = course2area2major2clasf2ll.get(courseId);
				if (area2major2clasf2ll == null) {
					area2major2clasf2ll = new HashMap<String, Map<String, Map<String, Set<Long>>>>();
					course2area2major2clasf2ll.put(courseId, area2major2clasf2ll);
				}
				Map<String, Map<String, Set<Long>>> major2clasf2ll = area2major2clasf2ll.get(areaAbbv);
				if (major2clasf2ll == null) {
					major2clasf2ll = new HashMap<String, Map<String, Set<Long>>>();
					area2major2clasf2ll.put(areaAbbv, major2clasf2ll);
				}
				Map<String, Set<Long>> clasf2ll = major2clasf2ll.get(majorCode);
				if (clasf2ll == null) {
					clasf2ll = new HashMap<String, Set<Long>>();
					major2clasf2ll.put(majorCode, clasf2ll);
				}
				Set<Long> ll = clasf2ll.get(clasfCode);
				if (ll == null) {
					ll = new HashSet<Long>();
					clasf2ll.put(clasfCode, ll);
				}
				ll.add(studentId);
			}
		}
		return course2area2major2clasf2ll;
	}
	
	private Hashtable<Long, Integer> loadClasf2req(org.hibernate.Session hibSession, Curriculum c) {
		List<Object[]> lines = null;
		String select = "a.academicClassification.uniqueId, count(distinct s)";
		String from = "CourseRequest r inner join r.courseDemand.student s inner join s.areaClasfMajors a inner join a.major m";
		String where = "s.session.uniqueId = :sessionId and a.academicArea.uniqueId = :areaId";
		String group = "a.academicClassification.uniqueId";
		if (c.getMajors().isEmpty()) {
			// students with no major
			if (!c.isMultipleMajors())
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where  + " group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId())
					.setCacheable(true).list();
		} else if (!c.isMultipleMajors() || c.getMajors().size() == 1) {
			// students with one major
			List<Long> majorIds = new ArrayList<Long>();
			for (PosMajor major: c.getMajors())
				majorIds.add(major.getUniqueId());
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and m.uniqueId in :majorIds  group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId())
					.setParameterList("majorIds", majorIds)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			Map<String, Long> params = new HashMap<String, Long>();
			int idx = 0;
			for (PosMajor major: c.getMajors()) {
				if (idx == 0) {
					where += " and m.uniqueId = :m" + idx;
				} else {
					from += " inner join s.areaClasfMajors a" + idx;
					where += " and a" + idx + ".academicArea.uniqueId = :areaId and a" + idx + ".major.uniqueId = :m" + idx;
				}
				params.put("m" + idx, major.getUniqueId());
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId());
			for (Map.Entry<String, Long> e: params.entrySet())
				q.setLong(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Hashtable<Long, Integer> clasf2enrl = new Hashtable<Long, Integer>();
		if (lines != null)
			for (Object[] o: lines) {
				Long clasfId = (Long)o[0];
				int enrl = ((Number)o[1]).intValue();
				if (clasfId != null)
					clasf2enrl.put(clasfId, enrl);
			}
		return clasf2enrl;
	}
	
	private Hashtable<Long, Set<Long>> loadClasf2req(org.hibernate.Session hibSession, Long acadAreaId, Collection<Long> majors, boolean multipleMajors) {
		List<Object[]> lines = null;
		String select = "a.academicClassification.uniqueId, s.uniqueId";
		String from = "CourseRequest r inner join r.courseDemand.student s inner join s.areaClasfMajors a inner join a.major m";
		String where = "s.session.uniqueId = :sessionId and a.academicArea.uniqueId = :areaId";
		if (majors.isEmpty()) {
			// students with no major
			if (!multipleMajors)
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where)
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId)
					.setCacheable(true).list();
		} else if (!multipleMajors || majors.size() == 1) {
			// students with one major
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and m.uniqueId in :majorIds")
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId)
					.setParameterList("majorIds", majors)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			Map<String, Long> params = new HashMap<String, Long>();
			int idx = 0;
			for (Long major: majors) {
				if (idx == 0) {
					where += " and m.uniqueId = :m" + idx;
				} else {
					from += " inner join s.areaClasfMajors a" + idx;
					where += " and a" + idx + ".academicArea.uniqueId = :areaId and a" + idx + ".major.uniqueId = :m" + idx;
				}
				params.put("m" + idx, major);
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where)
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId);
			for (Map.Entry<String, Long> e: params.entrySet())
				q.setLong(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Hashtable<Long, Set<Long>> clasf2enrl = new Hashtable<Long, Set<Long>>();
		if (lines != null)
			for (Object[] o : lines) {
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
		List<Object[]> lines = null;
		String select = "a.academicClassification.uniqueId, r.courseOffering.uniqueId, count(distinct s)";
		String from = "CourseRequest r inner join r.courseDemand.student s inner join s.areaClasfMajors a inner join a.major m";
		String where = "s.session.uniqueId = :sessionId and a.academicArea.uniqueId = :areaId";
		String group = "a.academicClassification.uniqueId, r.courseOffering.uniqueId";
		if (c.getMajors().isEmpty()) {
			// students with no major
			if (!c.isMultipleMajors())
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId())
					.setCacheable(true).list();
		} else if (!c.isMultipleMajors() || c.getMajors().size() == 1) {
			// students with one major
			List<Long> majorIds = new ArrayList<Long>();
			for (PosMajor major: c.getMajors())
				majorIds.add(major.getUniqueId());
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and m.uniqueId in :majorIds group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId())
					.setParameterList("majorIds", majorIds)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			Map<String, Long> params = new HashMap<String, Long>();
			int idx = 0;
			for (PosMajor major: c.getMajors()) {
				if (idx == 0) {
					where += " and m.uniqueId = :m" + idx;
				} else {
					from += " inner join s.areaClasfMajors a" + idx;
					where += " and a" + idx + ".academicArea.uniqueId = :areaId and a" + idx + ".major.uniqueId = :m" + idx;
				}
				params.put("m" + idx, major.getUniqueId());
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where + " group by " + group)
					.setLong("sessionId", c.getAcademicArea().getSessionId()).setLong("areaId", c.getAcademicArea().getUniqueId());
			for (Map.Entry<String, Long> e: params.entrySet())
				q.setLong(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Hashtable<Long, Hashtable<Long, Integer>> clasf2course2enrl = new Hashtable<Long, Hashtable<Long,Integer>>();
		if (lines != null)
			for (Object[] o: lines) {
				Long clasfId = (Long)o[0];
				if (clasfId == null) continue;
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
	
	private Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> loadClasfCourse2req(org.hibernate.Session hibSession, Long acadAreaId, Collection<Long> majors, boolean multipleMajors) {
		List<Object[]> lines = null;
		String select = "a.academicClassification.uniqueId, r.courseOffering.uniqueId, r.courseOffering.subjectArea.subjectAreaAbbreviation || ' ' || r.courseOffering.courseNbr, s.uniqueId";
		String from = "CourseRequest r inner join r.courseDemand.student s inner join s.areaClasfMajors a inner join a.major m";
		String where = "s.session.uniqueId = :sessionId and a.academicArea.uniqueId = :areaId";
		if (majors.isEmpty()) {
			// students with no major
			if (!multipleMajors)
				lines = hibSession.createQuery("select " + select + " from " + from + " where " + where)
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId)
					.setCacheable(true).list();
		} else if (!multipleMajors || majors.size() == 1) {
			// students with one major
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + " and m.uniqueId in :majorIds")
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId)
					.setParameterList("majorIds", majors)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			Map<String, Long> params = new HashMap<String, Long>();
			int idx = 0;
			for (Long major: majors) {
				if (idx == 0) {
					where += " and m.uniqueId = :m" + idx;
				} else {
					from += " inner join s.areaClasfMajors a" + idx;
					where += " and a.academicArea.uniqueId = :areaId and a" + idx + ".major.uniqueId = :m" + idx;
				}
				params.put("m" + idx, major);
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where)
					.setLong("sessionId", getAcademicSessionId()).setLong("areaId", acadAreaId);
			for (Map.Entry<String, Long> e: params.entrySet())
				q.setLong(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Hashtable<Long, Hashtable<CourseInterface, Set<Long>>> clasf2course2enrl = new Hashtable<Long, Hashtable<CourseInterface,Set<Long>>>();
		if (lines != null)
			for (Object[] o : lines) {
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
	
	private Map<Long, Map<Long, Map<Long, Set<Long>>>> loadAreaMajorClasf2req(org.hibernate.Session hibSession, Long courseOfferingId) {
		Map<Long, Map<Long, Map<Long, Set<Long>>>> area2major2clasf2enrl = new HashMap<Long, Map<Long, Map<Long, Set<Long>>>>();
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select distinct a.academicArea.uniqueId, m.uniqueId, a.academicClassification.uniqueId, s.uniqueId " +
				"from CourseRequest r inner join r.courseDemand.student s inner join s.areaClasfMajors a inner join a.major m where " +
				"r.courseOffering.uniqueId = :courseId")
				.setLong("courseId", courseOfferingId)
				.setCacheable(true).list()) {
			Long areaId = (Long)o[0];
			Long majorId = (Long)o[1];
			if (majorId == null) majorId = -1l;
			Long clasfId = (Long)o[2];
			Long studentId = (Long)o[3];
			Map<Long, Map<Long, Set<Long>>> major2clasf2enrl = area2major2clasf2enrl.get(areaId);
			if (major2clasf2enrl == null) {
				major2clasf2enrl = new HashMap<Long, Map<Long, Set<Long>>>();
				area2major2clasf2enrl.put(areaId, major2clasf2enrl);
			}
			Map<Long, Set<Long>> clasf2enrl = major2clasf2enrl.get(majorId);
			if (clasf2enrl == null) {
				clasf2enrl = new HashMap<Long, Set<Long>>();
				major2clasf2enrl.put(majorId, clasf2enrl);
			}
			Set<Long> enrl = clasf2enrl.get(clasfId);
			if (enrl == null) {
				enrl = new HashSet<Long>();
				clasf2enrl.put(clasfId, enrl);
			}
			enrl.add(studentId);
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

	private boolean hasSnapshotData(org.hibernate.Session hibSession, Long sessionId) {
		Long cnt = (Long) hibSession
				.createQuery(
						"select count(1) from InstructionalOffering io where io.snapshotLimitDate is not null and io.session.uniqueId = :sessId")
				.setLong("sessId", sessionId).setCacheable(true).uniqueResult();
		return (cnt.longValue() > 0);
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

	private Hashtable<String, HashMap<String, Float>> getSnapshotRules(org.hibernate.Session hibSession,
			Long acadAreaId) {
		Hashtable<String, HashMap<String, Float>> clasf2major2ssproj = new Hashtable<String, HashMap<String, Float>>();
		if (hasSnapshotData(hibSession, getAcademicSessionId())) {
			for (CurriculumProjectionRule rule : (List<CurriculumProjectionRule>) hibSession
					.createQuery("select r from CurriculumProjectionRule r where r.academicArea.uniqueId=:acadAreaId")
					.setLong("acadAreaId", acadAreaId).setCacheable(true).list()) {
				String majorCode = (rule.getMajor() == null ? "" : rule.getMajor().getCode());
				String clasfCode = rule.getAcademicClassification().getCode();
				Float snapshotProjection = rule.getSnapshotProjection();
				HashMap<String, Float> major2ssproj = clasf2major2ssproj.get(clasfCode);
				if (major2ssproj == null) {
					major2ssproj = new HashMap<String, Float>();
					clasf2major2ssproj.put(clasfCode, major2ssproj);
				}
				major2ssproj.put(majorCode, snapshotProjection);
			}
		}
		return clasf2major2ssproj;
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

	public float getSnapshotProjection(Hashtable<String, HashMap<String, Float>> clasf2major2ssproj, String majorCode,
			String clasfCode) {
		if (clasf2major2ssproj == null || clasf2major2ssproj.isEmpty())
			return 1.0f;
		HashMap<String, Float> major2ssproj = clasf2major2ssproj.get(clasfCode);
		if (major2ssproj == null)
			return 1.0f;
		Float snapshotProjection = major2ssproj.get(majorCode);
		if (snapshotProjection == null)
			snapshotProjection = major2ssproj.get("");
		return (snapshotProjection == null ? 1.0f : snapshotProjection);
	}

}
