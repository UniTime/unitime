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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.services.ReservationService;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.ReservationException;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface.ReservationFilterRpcRequest;
import org.unitime.timetable.interfaces.ExternalCourseOfferingReservationEditAction;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseReservation;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumProjectionRule;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.PosMajorDAO;
import org.unitime.timetable.model.dao.ReservationDAO;
import org.unitime.timetable.model.dao.StudentGroupDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.reservation.ReservationFilterBackend;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@Service("reservation.gwt")
public class ReservationServlet implements ReservationService {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Logger sLog = Logger.getLogger(ReservationServlet.class);

	private @Autowired SessionContext sessionContext;
	private SessionContext getSessionContext() { return sessionContext; }
	
	@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

	@Override
	@PreAuthorize("checkPermission('Reservations')")
	public List<ReservationInterface.Area> getAreas() throws ReservationException, PageAccessException {
		try {
			List<ReservationInterface.Area> results = new ArrayList<ReservationInterface.Area>();
			org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();
			try {
				List<ReservationInterface.IdName> classifications = new ArrayList<ReservationInterface.IdName>();
				for (AcademicClassification classification: (List<AcademicClassification>)hibSession.createQuery(
						"select c from AcademicClassification c where c.session.uniqueId = :sessionId order by c.code, c.name")
						.setLong("sessionId", getAcademicSessionId()).setCacheable(true).list()) {
					ReservationInterface.IdName clasf = new ReservationInterface.IdName();
					clasf.setId(classification.getUniqueId());
					clasf.setName(Constants.curriculaToInitialCase(classification.getName()));
					clasf.setAbbv(classification.getCode());
					classifications.add(clasf);
				}
				for (AcademicArea area: (List<AcademicArea>)hibSession.createQuery(
						"select a from AcademicArea a where a.session.uniqueId = :sessionId order by a.academicAreaAbbreviation, a.title")
						.setLong("sessionId", getAcademicSessionId()).setCacheable(true).list()) {
					ReservationInterface.Area curriculum = new ReservationInterface.Area();
					curriculum.setAbbv(area.getAcademicAreaAbbreviation());
					curriculum.setId(area.getUniqueId());
					curriculum.setName(Constants.curriculaToInitialCase(area.getTitle()));
					for (PosMajor major: area.getPosMajors()) {
						ReservationInterface.IdName mj = new ReservationInterface.IdName();
						mj.setId(major.getUniqueId());
						mj.setAbbv(major.getCode());
						mj.setName(Constants.curriculaToInitialCase(major.getName()));
						curriculum.getMajors().add(mj);
					}
					Collections.sort(curriculum.getMajors());
					curriculum.getClassifications().addAll(classifications);
					results.add(curriculum);
				}
			} finally {
				hibSession.close();
			}
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (ReservationException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new ReservationException(e.getMessage());
		}
	}
	
	private ReservationInterface.Offering convert(InstructionalOffering io, org.hibernate.Session hibSession) throws ReservationException, PageAccessException {
		ReservationInterface.Offering offering = new ReservationInterface.Offering();
		offering.setAbbv(io.getCourseName());
		offering.setName(io.getControllingCourseOffering().getTitle());
		offering.setId(io.getUniqueId());
		offering.setOffered(!io.isNotOffered());
		offering.setUnlockNeeded(permissionOfferingLockNeeded != null && permissionOfferingLockNeeded.check(sessionContext.getUser(), io));
		for (CourseOffering co: io.getCourseOfferings()) {
			ReservationInterface.Course course = new ReservationInterface.Course();
			course.setId(co.getUniqueId());
			course.setAbbv(co.getCourseName());
			course.setName(co.getTitle());
			course.setControl(co.isIsControl());
			course.setLimit(co.getReservation());
			offering.getCourses().add(course);
		}
		List<InstrOfferingConfig> configs = new ArrayList<InstrOfferingConfig>(io.getInstrOfferingConfigs());
		Collections.sort(configs, new InstrOfferingConfigComparator(null));
		for (InstrOfferingConfig ioc: configs) {
			ReservationInterface.Config config = new ReservationInterface.Config();
			config.setId(ioc.getUniqueId());
			config.setName(ioc.getName());
			config.setAbbv(ioc.getName());
			config.setLimit(ioc.isUnlimitedEnrollment() ? null : ioc.getLimit());
			offering.getConfigs().add(config);
			TreeSet<SchedulingSubpart> subparts = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator());
			subparts.addAll(ioc.getSchedulingSubparts());
			for (SchedulingSubpart ss: subparts) {
				ReservationInterface.Subpart subpart = new ReservationInterface.Subpart();
				subpart.setId(ss.getUniqueId());
				String suffix = ss.getSchedulingSubpartSuffix(hibSession);
				subpart.setAbbv(ss.getItypeDesc() + (suffix == null || suffix.isEmpty() ? "" : " " + suffix));
				subpart.setName(ss.getSchedulingSubpartLabel());
				subpart.setConfig(config);
				config.getSubparts().add(subpart);
				if (ss.getParentSubpart() != null)
					subpart.setParentId(ss.getParentSubpart().getUniqueId());
				List<Class_> classes = new ArrayList<Class_>(ss.getClasses());
				Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
				for (Class_ c: classes) {
					ReservationInterface.Clazz clazz = new ReservationInterface.Clazz();
					clazz.setId(c.getUniqueId());
					clazz.setAbbv(ss.getItypeDesc() + " " + c.getSectionNumberString(hibSession));
					clazz.setName(c.getClassLabel(hibSession));
					subpart.getClasses().add(clazz);
					clazz.setSubpart(subpart);
					clazz.setLimit(c.getClassLimit());
					if (c.getParentClass() != null)
						clazz.setParentId(c.getParentClass().getUniqueId());
				}
			}
		}
		return offering;
	}

	@Override
	@PreAuthorize("checkPermission('Reservations')")
	public ReservationInterface.Offering getOffering(Long offeringId) throws ReservationException, PageAccessException {
		try {
			org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();
			try {
				InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(offeringId, hibSession);
				if (io == null) { throw new ReservationException(MESSAGES.errorOfferingDoesNotExist(offeringId == null ? "null" : offeringId.toString())); }
				return convert(io, hibSession);
			} finally {
				hibSession.close();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (ReservationException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new ReservationException(e.getMessage());
		}
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
	
	@PreAuthorize("checkPermission('Reservations')")
	public ReservationInterface.Offering getOfferingByCourseName(String courseName) throws ReservationException, PageAccessException{
		try {
			org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();
			try {
				CourseOffering co = getCourse(hibSession, courseName);
				if (co == null) { throw new ReservationException(MESSAGES.errorCourseDoesNotExist(courseName)); }
				return convert(co.getInstructionalOffering(), hibSession);
			} finally {
				hibSession.close();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (ReservationException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new ReservationException(e.getMessage());
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
	
	private float getProjection(Hashtable<String,HashMap<String, Float>> clasf2major2proj, String majorCode, String clasfCode) {
		if (clasf2major2proj == null || clasf2major2proj.isEmpty()) return 1.0f;
		HashMap<String, Float> major2proj = clasf2major2proj.get(clasfCode);
		if (major2proj == null) return 1.0f;
		Float projection = major2proj.get(majorCode);
		if (projection == null)
			projection = major2proj.get("");
		return (projection == null ? 1.0f : projection);
	}
	
	private ReservationInterface convert(Reservation reservation, String nameFormat, org.hibernate.Session hibSession) {
		ReservationInterface r = null;
		if (reservation instanceof CourseReservation) {
			CourseOffering co = ((CourseReservation) reservation).getCourse();
			ReservationInterface.Course course = new ReservationInterface.Course();
			course.setId(co.getUniqueId());
			course.setAbbv(co.getCourseName());
			course.setControl(co.isIsControl());
			course.setName(co.getTitle());
			course.setLimit(co.getReservation());
			r = new ReservationInterface.CourseReservation();
			((ReservationInterface.CourseReservation) r).setCourse(course);
			r.setLastLike(co.getDemand());
			r.setEnrollment(co.getEnrollment());
			r.setProjection(co.getProjectedDemand());
		} else if (reservation instanceof IndividualReservation) {
			r = new ReservationInterface.IndividualReservation();
			if (reservation instanceof OverrideReservation) {
				r = new ReservationInterface.OverrideReservation(((OverrideReservation)reservation).getOverrideType());
			}
			String sId = "";
			for (Student student: ((IndividualReservation) reservation).getStudents()) {
				ReservationInterface.IdName s = new ReservationInterface.IdName();
				s.setId(student.getUniqueId());
				s.setAbbv(student.getExternalUniqueId());
				s.setName(student.getName(nameFormat));
				((ReservationInterface.IndividualReservation) r).getStudents().add(s);
				sId += (sId.isEmpty() ? "" : ",") + student.getUniqueId();
			}
			Collections.sort(((ReservationInterface.IndividualReservation) r).getStudents(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getAbbv().compareTo(s2.getAbbv());
				}
			});
			if (!sId.isEmpty()) {
				Number enrollment = (Number)hibSession.createQuery(
						"select count(distinct e.student) " +
						"from StudentClassEnrollment e where " +
						"e.courseOffering.instructionalOffering.uniqueId = :offeringId " +
						"and e.student.uniqueId in (" + sId + ")")
						.setLong("offeringId", reservation.getInstructionalOffering().getUniqueId()).setCacheable(true).uniqueResult();
				if (enrollment.intValue() > 0)
					r.setEnrollment(enrollment.intValue());
			}
		} else if (reservation instanceof CurriculumReservation) {
			CurriculumReservation cr = (CurriculumReservation) reservation;
			r = new ReservationInterface.CurriculumReservation();
			ReservationInterface.Area curriculum = new ReservationInterface.Area();
			curriculum.setId(cr.getArea().getUniqueId());
			curriculum.setAbbv(cr.getArea().getAcademicAreaAbbreviation());
			curriculum.setName(Constants.curriculaToInitialCase(cr.getArea().getTitle()));
			String cfCodes = "";
			String cfIds = "";
			for (AcademicClassification classification: cr.getClassifications()) {
				ReservationInterface.IdName clasf = new ReservationInterface.IdName();
				clasf.setId(classification.getUniqueId());
				clasf.setName(Constants.curriculaToInitialCase(classification.getName()));
				clasf.setAbbv(classification.getCode());
				curriculum.getClassifications().add(clasf);
				cfCodes += (cfCodes.isEmpty() ? "" : ",") + "'" + classification.getCode() + "'";
				cfIds += (cfIds.isEmpty() ? "" : ",") + classification.getUniqueId();
			}
			String mjCodes = "";
			String mjIds = "";
			for (PosMajor major: cr.getMajors()) {
				ReservationInterface.IdName mj = new ReservationInterface.IdName();
				mj.setId(major.getUniqueId());
				mj.setAbbv(major.getCode());
				mj.setName(Constants.curriculaToInitialCase(major.getName()));
				curriculum.getMajors().add(mj);
				mjCodes += (mjCodes.isEmpty() ? "" : ",") + "'" + major.getCode() + "'";
				mjIds += (mjIds.isEmpty() ? "" : ",") + major.getUniqueId();
			}
			Collections.sort(curriculum.getMajors(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getAbbv().compareTo(s2.getAbbv());
					if (cmp != 0) return cmp;
					cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getId().compareTo(s2.getId());
				}
			});
			Collections.sort(curriculum.getClassifications(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getAbbv().compareTo(s2.getAbbv());
					if (cmp != 0) return cmp;
					cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getId().compareTo(s2.getId());
				}
			});
			((ReservationInterface.CurriculumReservation) r).setCurriculum(curriculum);
			Number enrollment = (Number)hibSession.createQuery(
					"select count(distinct e.student) " +
					"from StudentClassEnrollment e inner join e.student.academicAreaClassifications a inner join e.student.posMajors m where " +
					"e.courseOffering.instructionalOffering.uniqueId = :offeringId " +
					"and a.academicArea.uniqueId = :areaId" + 
					(mjIds.isEmpty() ? "" : " and m.uniqueId in (" + mjIds + ")") +
					(cfIds.isEmpty() ? "" : " and a.academicClassification.uniqueId in (" + cfIds + ")"))
					.setLong("offeringId", reservation.getInstructionalOffering().getUniqueId())
					.setLong("areaId", cr.getArea().getUniqueId()).setCacheable(true).uniqueResult();
			if (enrollment.intValue() > 0)
				r.setEnrollment(enrollment.intValue());
			/*
			Number lastLike = (Number)hibSession.createQuery(
					"select count(distinct s) from " +
					"LastLikeCourseDemand x inner join x.student s inner join s.academicAreaClassifications a inner join s.posMajors m " +
					"inner join a.academicClassification f inner join a.academicArea r, CourseOffering co where " +
					"x.subjectArea.session.uniqueId = :sessionId and co.instructionalOffering.uniqueId = :offeringId and "+
					"co.subjectArea.uniqueId = x.subjectArea.uniqueId and " +
					"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr)) " +
					"and r.academicAreaAbbreviation = :areaAbbv" +
					(mjCodes.isEmpty() ? "" : " and m.code in (" + mjCodes + ")") +
					(cfCodes.isEmpty() ? "" : " and f.code in (" + cfCodes + ")"))
					.setLong("sessionId", getAcademicSessionId())
					.setLong("offeringId", reservation.getInstructionalOffering().getUniqueId())
					.setString("areaAbbv", cr.getArea().getAcademicAreaAbbreviation()).uniqueResult();
			r.setLastLike(lastLike.intValue());
			*/
			float projection = 0f;
			int lastLike = 0;
			Hashtable<String,HashMap<String, Float>> rules = getRules(hibSession, cr.getArea().getUniqueId());
			for (Object[] o: (List<Object[]>)hibSession.createQuery(
					"select count(distinct s), m.code, f.code from " +
					"LastLikeCourseDemand x inner join x.student s inner join s.academicAreaClassifications a inner join s.posMajors m " +
					"inner join a.academicClassification f inner join a.academicArea r, CourseOffering co where " +
					"x.subjectArea.session.uniqueId = :sessionId and co.instructionalOffering.uniqueId = :offeringId and "+
					"co.subjectArea.uniqueId = x.subjectArea.uniqueId and " +
					"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr)) " +
					"and r.academicAreaAbbreviation = :areaAbbv" +
					(mjCodes.isEmpty() ? "" : " and m.code in (" + mjCodes + ")") +
					(cfCodes.isEmpty() ? "" : " and f.code in (" + cfCodes + ")") +
					" group by m.code, f.code")
					.setLong("sessionId", getAcademicSessionId())
					.setLong("offeringId", reservation.getInstructionalOffering().getUniqueId())
					.setString("areaAbbv", cr.getArea().getAcademicAreaAbbreviation()).setCacheable(true).list()) {
				int nrStudents = ((Number)o[0]).intValue();
				lastLike += nrStudents;
				projection += getProjection(rules, (String)o[1], (String)o[2]) * nrStudents;
			}
			if (lastLike > 0) {
				r.setLastLike(lastLike);
				r.setProjection(Math.round(projection));
			}
			
		} else if (reservation instanceof StudentGroupReservation) {
			r = new ReservationInterface.GroupReservation();
			StudentGroup sg = ((StudentGroupReservation) reservation).getGroup();
			ReservationInterface.IdName group = new ReservationInterface.IdName();
			group.setId(sg.getUniqueId());
			group.setName(sg.getGroupName());
			group.setAbbv(sg.getGroupAbbreviation());
			group.setLimit(sg.getStudents().size());
			((ReservationInterface.GroupReservation) r).setGroup(group);
			Number enrollment = (Number)hibSession.createQuery(
					"select count(distinct e.student) " +
					"from StudentClassEnrollment e inner join e.student.groups g where " +
					"e.courseOffering.instructionalOffering.uniqueId = :offeringId " +
					"and g.uniqueId = :groupId")
					.setLong("offeringId", reservation.getInstructionalOffering().getUniqueId())
					.setLong("groupId", sg.getUniqueId()).setCacheable(true).uniqueResult();
			if (enrollment.intValue() > 0)
				r.setEnrollment(enrollment.intValue());
		} else {
			throw new ReservationException(MESSAGES.errorUnknownReservationType(reservation.getClass().getName()));
		}
		ReservationInterface.Offering offering = new ReservationInterface.Offering();
		offering.setAbbv(reservation.getInstructionalOffering().getCourseName());
		offering.setName(reservation.getInstructionalOffering().getControllingCourseOffering().getTitle());
		offering.setId(reservation.getInstructionalOffering().getUniqueId());
		offering.setOffered(!reservation.getInstructionalOffering().isNotOffered());
		offering.setUnlockNeeded(permissionOfferingLockNeeded != null && permissionOfferingLockNeeded.check(sessionContext.getUser(), reservation.getInstructionalOffering()));
		r.setOffering(offering);
		for (CourseOffering co: reservation.getInstructionalOffering().getCourseOfferings()) {
			ReservationInterface.Course course = new ReservationInterface.Course();
			course.setId(co.getUniqueId());
			course.setAbbv(co.getCourseName());
			course.setName(co.getTitle());
			course.setControl(co.isIsControl());
			course.setLimit(co.getReservation());
			offering.getCourses().add(course);
		}
		List<InstrOfferingConfig> configs = new ArrayList<InstrOfferingConfig>(reservation.getConfigurations());
		Collections.sort(configs, new InstrOfferingConfigComparator(null));
		for (InstrOfferingConfig ioc: configs) {
			ReservationInterface.Config config = new ReservationInterface.Config();
			config.setId(ioc.getUniqueId());
			config.setName(ioc.getName());
			config.setAbbv(ioc.getName());
			config.setLimit(ioc.isUnlimitedEnrollment() ? null : ioc.getLimit());
			r.getConfigs().add(config);
		}
		List<Class_> classes = new ArrayList<Class_>(reservation.getClasses());
		Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
		for (Class_ c: classes) {
			ReservationInterface.Clazz clazz = new ReservationInterface.Clazz();
			clazz.setId(c.getUniqueId());
			clazz.setAbbv(c.getSchedulingSubpart().getItypeDesc() + " " + c.getSectionNumberString(hibSession));
			clazz.setName(c.getClassLabel(hibSession));
			clazz.setLimit(c.getClassLimit());
			r.getClasses().add(clazz);
		}
		r.setExpirationDate(reservation.getExpirationDate());
		r.setExpired(reservation.isExpired());
		r.setLimit(reservation.getLimit());
		r.setId(reservation.getUniqueId());
		return r;
	}

	@Override
	@PreAuthorize("checkPermission('Reservations')")
	public List<ReservationInterface> getReservations(Long offeringId) throws ReservationException, PageAccessException {
		try {
			List<ReservationInterface> results = new ArrayList<ReservationInterface>();
			org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();
			String nameFormat = UserProperty.NameFormat.get(getSessionContext().getUser());
			try {
				for (Reservation reservation: (List<Reservation>)hibSession.createQuery(
						"select r from Reservation r where r.instructionalOffering.uniqueId = :offeringId")
						.setLong("offeringId", offeringId).setCacheable(true).list()) {
					ReservationInterface r = convert(reservation, nameFormat, hibSession);
					r.setEditable(getSessionContext().hasPermission(reservation, Right.ReservationEdit));
					results.add(r);
				}				
			} finally {
				hibSession.close();
			}
			Collections.sort(results);
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (ReservationException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new ReservationException(e.getMessage());
		}
	}

	@Override
	@PreAuthorize("checkPermission('Reservations')")
	public List<ReservationInterface.IdName> getStudentGroups() throws ReservationException, PageAccessException {
		try {
			List<ReservationInterface.IdName> results = new ArrayList<ReservationInterface.IdName>();
			org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();
			try {
				for (StudentGroup sg: (List<StudentGroup>)hibSession.createQuery(
						"select g from StudentGroup g where g.session.uniqueId = :sessionId order by g.groupName")
						.setLong("sessionId", getAcademicSessionId()).setCacheable(true).list()) {
					ReservationInterface.IdName group = new ReservationInterface.IdName();
					group.setId(sg.getUniqueId());
					group.setName(sg.getGroupAbbreviation());
					group.setAbbv(sg.getGroupName());
					group.setLimit(sg.getStudents().size());
					results.add(group);
				}
			} finally {
				hibSession.close();
			}
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (ReservationException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new ReservationException(e.getMessage());
		}
	}

	@Override
	@PreAuthorize("checkPermission(#reservationId, 'Reservation', 'ReservationEdit')")
	public ReservationInterface getReservation(Long reservationId) throws ReservationException, PageAccessException {
		try {
			org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();
			ReservationInterface r;
			try {
				Reservation reservation = ReservationDAO.getInstance().get(reservationId, hibSession);
				if (reservation == null)
					throw new ReservationException("Reservation not found.");
				r = convert(reservation, UserProperty.NameFormat.get(getSessionContext().getUser()), hibSession);
				r.setEditable(getSessionContext().hasPermission(reservation, Right.ReservationEdit));
			} finally {
				hibSession.close();
			}
			return r;
		} catch (PageAccessException e) {
			throw e;
		} catch (ReservationException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new ReservationException(e.getMessage());
		}
	}
	
	@Override
	@PreAuthorize("(#reservation.id != null and checkPermission(#reservation.id, 'Reservation', 'ReservationEdit')) or (#reservation.id == null and checkPermission(#reservation.offering.id, 'InstructionalOffering', 'ReservationOffering') and checkPermission('ReservationAdd'))")
	public Long save(ReservationInterface reservation) throws ReservationException, PageAccessException {
		try {
			org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();
			UserContext user = getSessionContext().getUser();
			try {
				InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(reservation.getOffering().getId(), hibSession);
				if (offering == null)
					throw new ReservationException(MESSAGES.errorOfferingDoesNotExist(reservation.getOffering().getName()));
				Reservation r = null;
				if (reservation.getId() != null) {
					r = ReservationDAO.getInstance().get(reservation.getId(), hibSession);
				}
				if (r == null) {
					if (reservation instanceof ReservationInterface.OverrideReservation) {
						r = new OverrideReservation();
						((OverrideReservation)r).setOverrideType(((ReservationInterface.OverrideReservation)reservation).getType());
					} else if (reservation instanceof ReservationInterface.IndividualReservation)
						r = new IndividualReservation();
					else if (reservation instanceof ReservationInterface.GroupReservation)
						r = new StudentGroupReservation();
					else if (reservation instanceof ReservationInterface.CurriculumReservation)
						r = new CurriculumReservation();
					else if (reservation instanceof ReservationInterface.CourseReservation)
						r = new CourseReservation();
					else
						throw new ReservationException(MESSAGES.errorUnknownReservationType(reservation.getClass().getName()));
				}
				r.setLimit(r instanceof IndividualReservation ? null : reservation.getLimit());
				r.setExpirationDate(reservation.getExpirationDate());
				r.setInstructionalOffering(offering);
				offering.getReservations().add(r);
				if (r.getClasses() == null)
					r.setClasses(new HashSet<Class_>());
				else
					r.getClasses().clear();
				for (ReservationInterface.Clazz clazz: reservation.getClasses())
					r.getClasses().add(Class_DAO.getInstance().get(clazz.getId(), hibSession));
				if (r.getConfigurations() == null)
					r.setConfigurations(new HashSet<InstrOfferingConfig>());
				else
					r.getConfigurations().clear();
				for (ReservationInterface.Config config: reservation.getConfigs())
					r.getConfigurations().add(InstrOfferingConfigDAO.getInstance().get(config.getId(), hibSession));
				if (r instanceof IndividualReservation) {
					IndividualReservation ir = (IndividualReservation)r;
					if (ir.getStudents() == null)
						ir.setStudents(new HashSet<Student>());
					else
						ir.getStudents().clear();
					for (ReservationInterface.IdName student: ((ReservationInterface.IndividualReservation) reservation).getStudents()) {
						Student s = Student.findByExternalId(offering.getSessionId(), student.getAbbv());
						if (s != null)
							ir.getStudents().add(s);
					}
				} else if (r instanceof CourseReservation) {
					((CourseReservation)r).setCourse(CourseOfferingDAO.getInstance().get(((ReservationInterface.CourseReservation) reservation).getCourse().getId(), hibSession));
				} else if (r instanceof StudentGroupReservation) {
					((StudentGroupReservation)r).setGroup(StudentGroupDAO.getInstance().get(((ReservationInterface.GroupReservation) reservation).getGroup().getId(), hibSession));
				} else if (r instanceof CurriculumReservation) {
					ReservationInterface.Area curriculum = ((ReservationInterface.CurriculumReservation)reservation).getCurriculum();
					CurriculumReservation cr = (CurriculumReservation)r;
					cr.setArea(AcademicAreaDAO.getInstance().get(curriculum.getId(), hibSession));
					if (cr.getMajors() == null)
						cr.setMajors(new HashSet<PosMajor>());
					else
						cr.getMajors().clear();
					for (ReservationInterface.IdName mj: curriculum.getMajors()) {
						cr.getMajors().add(PosMajorDAO.getInstance().get(mj.getId(), hibSession));
					}
					if (cr.getClassifications() == null)
						cr.setClassifications(new HashSet<AcademicClassification>());
					else
						cr.getClassifications().clear();
					for (ReservationInterface.IdName clasf: curriculum.getClassifications()) {
						cr.getClassifications().add(AcademicClassificationDAO.getInstance().get(clasf.getId(), hibSession));
					}
				}
				hibSession.saveOrUpdate(r);
				hibSession.saveOrUpdate(r.getInstructionalOffering());
				if (permissionOfferingLockNeeded.check(user, offering))
					StudentSectioningQueue.offeringChanged(hibSession, user, offering.getSession().getUniqueId(), offering.getUniqueId());
				hibSession.flush();
				
				String className = ApplicationProperty.ExternalActionCourseOfferingReservationEdit.value();
		    	if (className != null && !className.trim().isEmpty()){
		    		ExternalCourseOfferingReservationEditAction editAction = (ExternalCourseOfferingReservationEditAction) Class.forName(className).newInstance();
		    		editAction.performExternalCourseOfferingReservationEditAction(r.getInstructionalOffering(), hibSession);
		    	}
		    	
		        ChangeLog.addChange(
		        		hibSession,
		                sessionContext,
		                r.getInstructionalOffering(),
		                ChangeLog.Source.RESERVATION,
		                reservation.getId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE,
		                r.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
		                r.getInstructionalOffering().getDepartment());
		        hibSession.flush();
		        
				return r.getUniqueId();
			} finally {
				hibSession.close();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (ReservationException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new ReservationException(e.getMessage());
		}
	}
	
	@Override
	@PreAuthorize("checkPermission(#reservationId, 'Reservation', 'ReservationDelete')")
	public Boolean delete(Long reservationId) throws ReservationException, PageAccessException {
		try {
			org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();
			UserContext user = getSessionContext().getUser();
			try {
				Reservation reservation = ReservationDAO.getInstance().get(reservationId, hibSession);
				if (reservation == null)
					return false;
				InstructionalOffering offering = reservation.getInstructionalOffering();
				offering.getReservations().remove(reservation);
				hibSession.delete(reservation);
				hibSession.saveOrUpdate(offering);
				if (permissionOfferingLockNeeded.check(user, offering))
					StudentSectioningQueue.offeringChanged(hibSession, user, offering.getSession().getUniqueId(), offering.getUniqueId());
				hibSession.flush();
				
				String className = ApplicationProperty.ExternalActionCourseOfferingReservationEdit.value();
		    	if (className != null && !className.trim().isEmpty()){
		    		ExternalCourseOfferingReservationEditAction editAction = (ExternalCourseOfferingReservationEditAction) Class.forName(className).newInstance();
		    		editAction.performExternalCourseOfferingReservationEditAction(offering, hibSession);
		    	}
		    	
		    	ChangeLog.addChange(
		        		hibSession,
		                sessionContext,
		                offering,
		                ChangeLog.Source.RESERVATION,
		                ChangeLog.Operation.DELETE,
		                offering.getControllingCourseOffering().getSubjectArea(),
		                offering.getDepartment());
		    	hibSession.flush();
			} finally {
				hibSession.close();
			}
			return true;
		} catch (PageAccessException e) {
			throw e;
		} catch (ReservationException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new ReservationException(e.getMessage());
		}
	}
	
	private Long getAcademicSessionId() throws PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null) throw new PageAccessException(
				getSessionContext().isHttpSessionNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getCurrentAuthority() == null)
			throw new PageAccessException("Insufficient user privileges.");
		Long sessionId = user.getCurrentAcademicSessionId();
		if (sessionId == null) throw new PageAccessException("No academic session is selecgted.");
		return sessionId;
	}

	@Override
	@PreAuthorize("checkPermission('ReservationAdd')")
	public Boolean canAddReservation() throws ReservationException, PageAccessException {
		return true;
	}

	@Override
	@PreAuthorize("checkPermission('Reservations')")
	public List<ReservationInterface> findReservations(ReservationFilterRpcRequest filter) throws ReservationException, PageAccessException {
		try {
			List<ReservationInterface> results = new ArrayList<ReservationInterface>();
			getSessionContext().setAttribute("Reservations.LastFilter", filter.toQueryString());
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			String nameFormat = UserProperty.NameFormat.get(getSessionContext().getUser());
			try {
				for (Reservation reservation: ReservationFilterBackend.reservations(filter, getSessionContext())) {
					ReservationInterface r = convert(reservation, nameFormat, hibSession);
					r.setEditable(getSessionContext().hasPermission(reservation, Right.ReservationEdit));
					results.add(r);
				}
			} finally {
				hibSession.close();
			}
			Collections.sort(results);
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (ReservationException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new ReservationException(e.getMessage());
		}
	}

	@Override
	@PreAuthorize("checkPermission('Reservations')")
	public String lastReservationFilter() throws ReservationException, PageAccessException {
		String filter = (String)getSessionContext().getAttribute("Reservations.LastFilter");
		return (filter == null ? "mode:\"Not Expired\"" : filter);
	}

	@Override
	public List<ReservationInterface.Curriculum> getCurricula(Long offeringId) throws ReservationException, PageAccessException {
		try {
			List<ReservationInterface.Curriculum> results = new ArrayList<ReservationInterface.Curriculum>();
			org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();
			try {
				for (Curriculum c : (List<Curriculum>)hibSession.createQuery(
						"select distinct c.classification.curriculum from CurriculumCourse c where c.course.instructionalOffering = :offeringId ")
						.setLong("offeringId", offeringId).setCacheable(true).list()) {

					ReservationInterface.Curriculum curriculum = new ReservationInterface.Curriculum();
					curriculum.setAbbv(c.getAbbv());
					curriculum.setId(c.getUniqueId());
					curriculum.setName(c.getName());
					
					ReservationInterface.IdName area = new ReservationInterface.IdName();
					area.setAbbv(c.getAcademicArea().getAcademicAreaAbbreviation());
					area.setId(c.getAcademicArea().getUniqueId());
					area.setName(Constants.curriculaToInitialCase(c.getAcademicArea().getTitle()));
					curriculum.setArea(area);
					
					int limit = 0;
					for (CurriculumClassification cc: c.getClassifications()) {
						AcademicClassification classification = cc.getAcademicClassification();
						ReservationInterface.IdName clasf = new ReservationInterface.IdName();
						clasf.setId(classification.getUniqueId());
						clasf.setName(Constants.curriculaToInitialCase(classification.getName()));
						clasf.setAbbv(classification.getCode());
						clasf.setLimit(0);
						curriculum.getClassifications().add(clasf);
						for (CurriculumCourse cr: cc.getCourses())
							if (cr.getCourse().getInstructionalOffering().getUniqueId().equals(offeringId)) {
								limit += Math.round(cr.getPercShare() * cc.getNrStudents());
								clasf.setLimit(clasf.getLimit() + Math.round(cr.getPercShare() * cc.getNrStudents()));
							}
					}
					curriculum.setLimit(limit);
					Collections.sort(curriculum.getMajors());					
					
					for (PosMajor major: c.getMajors()) {
						ReservationInterface.IdName mj = new ReservationInterface.IdName();
						mj.setId(major.getUniqueId());
						mj.setAbbv(major.getCode());
						mj.setName(Constants.curriculaToInitialCase(major.getName()));
						curriculum.getMajors().add(mj);
					}
					Collections.sort(curriculum.getMajors());					
					
					results.add(curriculum);
				}
			} finally {
				hibSession.close();
			}
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (ReservationException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new ReservationException(e.getMessage());
		}
	}
}
