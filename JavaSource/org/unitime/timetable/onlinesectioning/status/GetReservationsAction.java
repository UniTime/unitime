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
package org.unitime.timetable.onlinesectioning.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideType;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseReservation;
import org.unitime.timetable.model.CurriculumOverrideReservation;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.GroupOverrideReservation;
import org.unitime.timetable.model.IndividualOverrideReservation;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.LearningCommunityReservation;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMajorConcentration;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.CurriculumReservationDAO;
import org.unitime.timetable.model.dao.LearningCommunityReservationDAO;
import org.unitime.timetable.model.dao.StudentGroupReservationDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseReservation;
import org.unitime.timetable.onlinesectioning.model.XCurriculumReservation;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XGroupReservation;
import org.unitime.timetable.onlinesectioning.model.XIndividualReservation;
import org.unitime.timetable.onlinesectioning.model.XLearningCommunityReservation;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XReservationType;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.model.XStudent.XGroup;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class GetReservationsAction implements OnlineSectioningAction<List<ReservationInterface>> {
	private static final long serialVersionUID = 1L;
	private Long iOfferingId;
	
	public GetReservationsAction forOfferingId(Long id) {
		iOfferingId = id; return this;
	}

	@Override
	public List<ReservationInterface> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (server instanceof StudentSolverProxy) {
			return ((StudentSolverProxy)server).getReservations(iOfferingId);
		} else if (server instanceof DatabaseServer) {
			List<ReservationInterface> results = new ArrayList<ReservationInterface>();
			for (Reservation reservation: (List<Reservation>)helper.getHibSession().createQuery(
					"select r from Reservation r where r.instructionalOffering.uniqueId = :offeringId")
					.setLong("offeringId", iOfferingId).setCacheable(true).list()) {
				ReservationInterface r = convert(reservation, helper);
				if (r != null) results.add(r);
			}
			Collections.sort(results);
			return results;
		} else {
			XOffering offering = server.getOffering(iOfferingId);
			if (offering == null) return null;
			XEnrollments enrollments = server.getEnrollments(iOfferingId);
			List<ReservationInterface> results = new ArrayList<ReservationInterface>();
			for (XReservation reservation: offering.getReservations()) {
				ReservationInterface r = convert(offering, reservation, enrollments, server, helper);
				if (r != null) results.add(r);
			}
			Collections.sort(results);
			return results;
		}
	}
	
	private ReservationInterface convert(Reservation reservation, OnlineSectioningHelper helper) {
		ReservationInterface r = null;
		CourseOffering co = reservation.getInstructionalOffering().getControllingCourseOffering();
		if (reservation instanceof CourseReservation) {
			co = ((CourseReservation) reservation).getCourse();
			ReservationInterface.Course course = new ReservationInterface.Course();
			course.setId(co.getUniqueId());
			course.setAbbv(co.getCourseName());
			course.setControl(co.isIsControl());
			course.setName(co.getTitle());
			course.setLimit(co.getReservation());
			r = new ReservationInterface.CourseReservation();
			((ReservationInterface.CourseReservation) r).setCourse(course);
		} else if (reservation instanceof IndividualReservation) {
			r = new ReservationInterface.IndividualReservation();
			if (reservation instanceof OverrideReservation) {
				r = new ReservationInterface.OverrideReservation(((OverrideReservation)reservation).getOverrideType());
			}
			for (Student student: ((IndividualReservation) reservation).getStudents()) {
				ReservationInterface.IdName s = new ReservationInterface.IdName();
				s.setId(student.getUniqueId());
				s.setAbbv(student.getExternalUniqueId());
				s.setName(helper.getStudentNameFormat().format(student));
				((ReservationInterface.IndividualReservation) r).getStudents().add(s);
			}
			Collections.sort(((ReservationInterface.IndividualReservation) r).getStudents(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getAbbv().compareTo(s2.getAbbv());
				}
			});
		} else if (reservation instanceof CurriculumReservation) {
			CurriculumReservation cr = (CurriculumReservation) reservation;
			r = new ReservationInterface.CurriculumReservation();
			ReservationInterface.Areas curriculum = new ReservationInterface.Areas();
			for (AcademicArea area: cr.getAreas()) {
				ReservationInterface.IdName aa = new ReservationInterface.IdName();
				aa.setId(area.getUniqueId());
				aa.setAbbv(area.getAcademicAreaAbbreviation());
				aa.setName(Constants.curriculaToInitialCase(area.getTitle()));
				curriculum.getAreas().add(aa);
			}
			for (AcademicClassification classification: cr.getClassifications()) {
				ReservationInterface.IdName clasf = new ReservationInterface.IdName();
				clasf.setId(classification.getUniqueId());
				clasf.setName(Constants.curriculaToInitialCase(classification.getName()));
				clasf.setAbbv(classification.getCode());
				curriculum.getClassifications().add(clasf);
			}
			for (PosMajor major: cr.getMajors()) {
				ReservationInterface.IdName mj = new ReservationInterface.IdName();
				mj.setId(major.getUniqueId());
				mj.setAbbv(major.getCode());
				mj.setName(Constants.curriculaToInitialCase(major.getName()));
				for (AcademicArea aa: major.getAcademicAreas())
					if (cr.getAreas().contains(aa)) {
						mj.setParentId(aa.getUniqueId());
						break;
					}
				curriculum.getMajors().add(mj);
			}
			for (PosMinor minor: cr.getMinors()) {
				ReservationInterface.IdName mn = new ReservationInterface.IdName();
				mn.setId(minor.getUniqueId());
				mn.setAbbv(minor.getCode());
				mn.setName(Constants.curriculaToInitialCase(minor.getName()));
				for (AcademicArea aa: minor.getAcademicAreas())
					if (cr.getAreas().contains(aa)) {
						mn.setParentId(aa.getUniqueId());
						break;
					}
				curriculum.getMinors().add(mn);
			}
			for (PosMajorConcentration conc: cr.getConcentrations()) {
				ReservationInterface.IdName cc = new ReservationInterface.IdName();
				cc.setId(conc.getUniqueId());
				cc.setAbbv(conc.getCode());
				cc.setName(Constants.curriculaToInitialCase(conc.getName()));
				cc.setParentId(conc.getMajor().getUniqueId());
				curriculum.getConcentrations().add(cc);
			}
			if (curriculum.getAreas().size() > 1)
				Collections.sort(curriculum.getAreas(), new Comparator<ReservationInterface.IdName>() {
					@Override
					public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
						int cmp = s1.getAbbv().compareTo(s2.getAbbv());
						if (cmp != 0) return cmp;
						cmp = s1.getName().compareTo(s2.getName());
						if (cmp != 0) return cmp;
						return s1.getId().compareTo(s2.getId());
					}
				});
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
			Collections.sort(curriculum.getMinors(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getAbbv().compareTo(s2.getAbbv());
					if (cmp != 0) return cmp;
					cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getId().compareTo(s2.getId());
				}
			});
			Collections.sort(curriculum.getConcentrations(), new Comparator<ReservationInterface.IdName>() {
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
		} else if (reservation instanceof LearningCommunityReservation) {
			r = new ReservationInterface.LCReservation();
			
			StudentGroup sg = ((LearningCommunityReservation) reservation).getGroup();
			ReservationInterface.IdName group = new ReservationInterface.IdName();
			group.setId(sg.getUniqueId());
			group.setName(sg.getGroupName());
			group.setAbbv(sg.getGroupAbbreviation());
			group.setLimit(sg.getStudents().size());
			((ReservationInterface.LCReservation) r).setGroup(group);
			
			co = ((LearningCommunityReservation) reservation).getCourse();
			ReservationInterface.Course course = new ReservationInterface.Course();
			course.setId(co.getUniqueId());
			course.setAbbv(co.getCourseName());
			course.setControl(co.isIsControl());
			course.setName(co.getTitle());
			course.setLimit(co.getReservation());
			((ReservationInterface.LCReservation) r).setCourse(course);
		} else if (reservation instanceof StudentGroupReservation) {
			r = new ReservationInterface.GroupReservation();
			StudentGroup sg = ((StudentGroupReservation) reservation).getGroup();
			ReservationInterface.IdName group = new ReservationInterface.IdName();
			group.setId(sg.getUniqueId());
			group.setName(sg.getGroupName());
			group.setAbbv(sg.getGroupAbbreviation());
			group.setLimit(sg.getStudents().size());
			((ReservationInterface.GroupReservation) r).setGroup(group);
		} else {
			return null;
		}
		ReservationInterface.Offering offering = new ReservationInterface.Offering();
		offering.setAbbv(co.getCourseName());
		offering.setName(co.getTitle());
		offering.setId(reservation.getInstructionalOffering().getUniqueId());
		offering.setOffered(!reservation.getInstructionalOffering().isNotOffered());
		r.setOffering(offering);
		boolean showClassSuffixes = ApplicationProperty.ReservationsShowClassSufix.isTrue();
		for (CourseOffering cx: reservation.getInstructionalOffering().getCourseOfferings()) {
			ReservationInterface.Course course = new ReservationInterface.Course();
			course.setId(cx.getUniqueId());
			course.setAbbv(cx.getCourseName());
			course.setName(cx.getTitle());
			course.setControl(cx.isIsControl());
			course.setLimit(cx.getReservation());
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
			clazz.setAbbv(c.getSchedulingSubpart().getItypeDesc() + " " + c.getSectionNumberString(helper.getHibSession()));
			clazz.setName(c.getClassLabel(co, showClassSuffixes));
			clazz.setLimit(c.getClassLimit());
			r.getClasses().add(clazz);
		}
		r.setStartDate(reservation.getStartDate());
		r.setExpirationDate(reservation.getExpirationDate());
		r.setExpired(reservation.isExpired());
		r.setLimit(reservation.getLimit());
		r.setInclusive(reservation.getInclusive());
		r.setId(reservation.getUniqueId());
		r.setOverride(reservation instanceof IndividualOverrideReservation || reservation instanceof GroupOverrideReservation || reservation instanceof CurriculumOverrideReservation);
		r.setAllowOverlaps(reservation.isAllowOverlap());
		r.setMustBeUsed(reservation.isMustBeUsed());
		r.setAlwaysExpired(reservation.isAlwaysExpired());
		r.setOverLimit(reservation.isCanAssignOverLimit());
		
		List<CourseRequest> requests = null;
		if (reservation instanceof CourseReservation) {
			requests = (List<CourseRequest>)helper.getHibSession().createQuery(
					"select cr from CourseRequest cr inner join fetch cr.courseDemand cd inner join fetch cd.student s where " +
					"cr.courseOffering = :courseId"
					).setLong("courseId", ((CourseReservation) reservation).getCourse().getUniqueId()).setCacheable(true).list();
		} else if (reservation instanceof LearningCommunityReservation) {
			requests = (List<CourseRequest>)helper.getHibSession().createQuery(
					"select cr from CourseRequest cr inner join fetch cr.courseDemand cd inner join fetch cd.student s where " +
					"cr.courseOffering = :courseId and s.uniqueId in " +
					"(select s.uniqueId from StudentGroupReservation r inner join r.group.students s where r.uniqueId = :reservationId)"
					).setLong("courseId", ((LearningCommunityReservation) reservation).getCourse().getUniqueId())
					.setLong("reservationId", reservation.getUniqueId()).setCacheable(true).list();
		} else if (reservation instanceof IndividualReservation) {
			requests = (List<CourseRequest>)helper.getHibSession().createQuery(
					"select cr from CourseRequest cr inner join fetch cr.courseDemand cd inner join fetch cd.student s where " +
					"cr.courseOffering.instructionalOffering = :offeringId and s.uniqueId in " +
					"(select s.uniqueId from IndividualReservation r inner join r.students s where r.uniqueId = :reservationId)"
					).setLong("offeringId", reservation.getInstructionalOffering().getUniqueId())
					.setLong("reservationId", reservation.getUniqueId()).setCacheable(true).list();
		} else if (reservation instanceof StudentGroupReservation) {
			requests = (List<CourseRequest>)helper.getHibSession().createQuery(
					"select cr from CourseRequest cr inner join fetch cr.courseDemand cd inner join fetch cd.student s where " +
					"cr.courseOffering.instructionalOffering = :offeringId and s.uniqueId in " +
					"(select s.uniqueId from StudentGroupReservation r inner join r.group.students s where r.uniqueId = :reservationId)"
					).setLong("offeringId", reservation.getInstructionalOffering().getUniqueId())
					.setLong("reservationId", reservation.getUniqueId()).setCacheable(true).list();
		} else {
			requests = (List<CourseRequest>)helper.getHibSession().createQuery(
					"select cr from CourseRequest cr inner join fetch cr.courseDemand cd inner join fetch cd.student s where " +
					"cr.courseOffering.instructionalOffering = :offeringId"
					).setLong("offeringId", reservation.getInstructionalOffering().getUniqueId()).setCacheable(true).list();
		}
		
		int enrolled = 0;
		for (CourseRequest request: requests) {
			if (reservation.isApplicable(request.getCourseDemand().getStudent(), request) && reservation.isMatching(request.getClassEnrollments()))
				enrolled ++;
		}
		r.setEnrollment(enrolled);

		return r;
	}
	
	private ReservationInterface convert(XOffering offering, XReservation reservation, XEnrollments enrollments, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		ReservationInterface r = null;
		XCourse co = offering.getControllingCourse();
		if (reservation instanceof XCourseReservation) {
			co = offering.getCourse(((XCourseReservation) reservation).getCourseId());
			ReservationInterface.Course course = new ReservationInterface.Course();
			course.setId(co.getCourseId());
			course.setAbbv(co.getCourseName());
			course.setControl(co.isControlling());
			course.setName(co.getTitle());
			course.setLimit(co.getLimit() < 0 ? null : co.getLimit());
			r = new ReservationInterface.CourseReservation();
			((ReservationInterface.CourseReservation) r).setCourse(course);
		} else if (reservation instanceof XIndividualReservation) {
			r = new ReservationInterface.IndividualReservation();
			if (reservation.getType() == XReservationType.IndividualOverride) {
				r = new ReservationInterface.OverrideReservation(
						reservation.isAllowOverlap() && reservation.canAssignOverLimit() ? OverrideType.AllowOverLimitTimeConflict :
						reservation.isAllowOverlap() ? OverrideType.AllowTimeConflict :
						reservation.canAssignOverLimit() ? OverrideType.AllowOverLimit : OverrideType.Other);
			}
			for (Long studentId: ((XIndividualReservation) reservation).getStudentIds()) {
				XStudent student = server.getStudent(studentId);
				if (student != null) {
					ReservationInterface.IdName s = new ReservationInterface.IdName();
					s.setId(student.getStudentId());
					s.setAbbv(student.getExternalId());
					s.setName(student.getName());
					((ReservationInterface.IndividualReservation) r).getStudents().add(s);
				}
			}
			Collections.sort(((ReservationInterface.IndividualReservation) r).getStudents(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getAbbv().compareTo(s2.getAbbv());
				}
			});
		} else if (reservation instanceof XCurriculumReservation) {
			XCurriculumReservation cr = (XCurriculumReservation) reservation;
			r = new ReservationInterface.CurriculumReservation();
			ReservationInterface.Areas curriculum = new ReservationInterface.Areas();
			CurriculumReservation ccr = CurriculumReservationDAO.getInstance().get(reservation.getReservationId(), helper.getHibSession());
			if (ccr != null) {
				for (AcademicArea area: ccr.getAreas()) {
					ReservationInterface.IdName aa = new ReservationInterface.IdName();
					aa.setId(area.getUniqueId());
					aa.setAbbv(area.getAcademicAreaAbbreviation());
					aa.setName(Constants.curriculaToInitialCase(area.getTitle()));
					curriculum.getAreas().add(aa);
				}
				for (AcademicClassification classification: ccr.getClassifications()) {
					ReservationInterface.IdName clasf = new ReservationInterface.IdName();
					clasf.setId(classification.getUniqueId());
					clasf.setName(Constants.curriculaToInitialCase(classification.getName()));
					clasf.setAbbv(classification.getCode());
					curriculum.getClassifications().add(clasf);
				}
				for (PosMajor major: ccr.getMajors()) {
					ReservationInterface.IdName mj = new ReservationInterface.IdName();
					mj.setId(major.getUniqueId());
					mj.setAbbv(major.getCode());
					mj.setName(Constants.curriculaToInitialCase(major.getName()));
					for (AcademicArea aa: major.getAcademicAreas())
						if (ccr.getAreas().contains(aa)) {
							mj.setParentId(aa.getUniqueId());
							break;
						}
					curriculum.getMajors().add(mj);
				}
				for (PosMinor minor: ccr.getMinors()) {
					ReservationInterface.IdName mn = new ReservationInterface.IdName();
					mn.setId(minor.getUniqueId());
					mn.setAbbv(minor.getCode());
					mn.setName(Constants.curriculaToInitialCase(minor.getName()));
					for (AcademicArea aa: minor.getAcademicAreas())
						if (ccr.getAreas().contains(aa)) {
							mn.setParentId(aa.getUniqueId());
							break;
						}
					curriculum.getMinors().add(mn);
				}
				for (PosMajorConcentration conc: ccr.getConcentrations()) {
					ReservationInterface.IdName cc = new ReservationInterface.IdName();
					cc.setId(conc.getUniqueId());
					cc.setAbbv(conc.getCode());
					cc.setName(Constants.curriculaToInitialCase(conc.getName()));
					cc.setParentId(conc.getMajor().getUniqueId());
					curriculum.getConcentrations().add(cc);
				}
			} else {
				long areaId = 0;
				for (String area: cr.getAcademicAreas()) {
					ReservationInterface.IdName aa = new ReservationInterface.IdName();
					aa.setId(areaId++);
					aa.setAbbv(area);
					aa.setName(area);
					curriculum.getAreas().add(aa);
				}
				long clasfId = 0;
				for (String classification: cr.getClassifications()) {
					ReservationInterface.IdName clasf = new ReservationInterface.IdName();
					clasf.setId(clasfId++);
					clasf.setAbbv(classification);
					clasf.setName(classification);
					curriculum.getClassifications().add(clasf);
				}
				long majorId = 0, concId = 0;
				for (String major: cr.getMajors()) {
					ReservationInterface.IdName mj = new ReservationInterface.IdName();
					mj.setId(majorId);
					mj.setAbbv(major);
					mj.setName(major);
					curriculum.getMajors().add(mj);
					if (cr.getConcentrations(major) != null)
						for (String conc: cr.getConcentrations(major)) {
							ReservationInterface.IdName cc = new ReservationInterface.IdName();
							cc.setId(concId++);
							cc.setAbbv(conc);
							cc.setParentId(majorId);
							cc.setName(conc);
							curriculum.getConcentrations().add(cc);
						}
					majorId ++;
				}
				for (String minor: cr.getMinors()) {
					ReservationInterface.IdName mn = new ReservationInterface.IdName();
					mn.setAbbv(minor);
					mn.setName(minor);
					curriculum.getMinors().add(mn);
				}
			}
			if (curriculum.getAreas().size() > 1)
				Collections.sort(curriculum.getAreas(), new Comparator<ReservationInterface.IdName>() {
					@Override
					public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
						int cmp = s1.getAbbv().compareTo(s2.getAbbv());
						if (cmp != 0) return cmp;
						cmp = s1.getName().compareTo(s2.getName());
						if (cmp != 0) return cmp;
						return s1.getId().compareTo(s2.getId());
					}
				});
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
			Collections.sort(curriculum.getMinors(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getAbbv().compareTo(s2.getAbbv());
					if (cmp != 0) return cmp;
					cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getId().compareTo(s2.getId());
				}
			});
			Collections.sort(curriculum.getConcentrations(), new Comparator<ReservationInterface.IdName>() {
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
		} else if (reservation instanceof XLearningCommunityReservation) {
			r = new ReservationInterface.LCReservation();
			
			XGroup sg = ((XLearningCommunityReservation) reservation).getGroup();
			ReservationInterface.IdName group = new ReservationInterface.IdName();
			group.setName(sg.getTitle());
			group.setAbbv(sg.getAbbreviation());
			group.setLimit(((XLearningCommunityReservation) reservation).getStudentIds().size());
			((ReservationInterface.LCReservation) r).setGroup(group);
			LearningCommunityReservation lcr = LearningCommunityReservationDAO.getInstance().get(reservation.getReservationId());
			if (lcr != null) {
				group.setId(lcr.getGroup().getUniqueId());
				group.setLimit(lcr.getGroup().getStudents().size());
			}
			
			co = offering.getCourse(((XLearningCommunityReservation) reservation).getCourseId());
			ReservationInterface.Course course = new ReservationInterface.Course();
			course.setId(co.getCourseId());
			course.setAbbv(co.getCourseName());
			course.setControl(co.isControlling());
			course.setName(co.getTitle());
			course.setLimit(co.getLimit() < 0 ? null : co.getLimit());
			((ReservationInterface.LCReservation) r).setCourse(course);
		} else if (reservation instanceof XGroupReservation) {
			r = new ReservationInterface.GroupReservation();
			XGroup sg = ((XGroupReservation) reservation).getGroup();
			ReservationInterface.IdName group = new ReservationInterface.IdName();
			group.setName(sg.getTitle());
			group.setAbbv(sg.getAbbreviation());
			group.setLimit(0);
			StudentGroupReservation gr = StudentGroupReservationDAO.getInstance().get(reservation.getReservationId(), helper.getHibSession());
			if (gr != null) {
				group.setId(gr.getGroup().getUniqueId());
				group.setLimit(gr.getGroup().getStudents().size());
			}
			((ReservationInterface.GroupReservation) r).setGroup(group);
		} else {
			return null;
		}
		ReservationInterface.Offering io = new ReservationInterface.Offering();
		io.setAbbv(co.getCourseName());
		io.setName(co.getTitle());
		io.setId(offering.getOfferingId());
		io.setOffered(true);
		r.setOffering(io);
		for (XCourse cx: offering.getCourses()) {
			ReservationInterface.Course course = new ReservationInterface.Course();
			course.setId(cx.getCourseId());
			course.setAbbv(cx.getCourseName());
			course.setName(cx.getTitle());
			course.setControl(cx.isControlling());
			course.setLimit(cx.getLimit() < 0 ? null : cx.getLimit());
			io.getCourses().add(course);
		}
		for (XConfig ioc: offering.getConfigs()) {
			if (reservation.hasConfigRestriction(ioc.getConfigId())) {
				boolean hasSection = false;
				for (XSubpart subpart: ioc.getSubparts()) {
					for (XSection c: subpart.getSections()) {
						if (reservation.hasSectionRestriction(c.getSectionId())) {
							hasSection = true;
							ReservationInterface.Clazz clazz = new ReservationInterface.Clazz();
							clazz.setId(c.getSectionId());
							clazz.setAbbv(c.getName(co.getCourseId()));
							clazz.setName(subpart.getName() + " " + c.getName(co.getCourseId()));
							clazz.setLimit(c.getLimit() < 0 ? null : c.getLimit());
							r.getClasses().add(clazz);
						}
					}
				}
				if (!hasSection) {
					ReservationInterface.Config config = new ReservationInterface.Config();
					config.setId(ioc.getConfigId());
					config.setName(ioc.getName());
					config.setAbbv(ioc.getName());
					config.setLimit(ioc.getLimit() < 0 ? null : ioc.getLimit());
					r.getConfigs().add(config);
				}
			} else {
				for (XSubpart subpart: ioc.getSubparts()) {
					for (XSection c: subpart.getSections()) {
						if (reservation.hasSectionRestriction(c.getSectionId())) {
							ReservationInterface.Clazz clazz = new ReservationInterface.Clazz();
							clazz.setId(c.getSectionId());
							clazz.setAbbv(c.getName(co.getCourseId()));
							clazz.setName(subpart.getName() + " " + c.getName(co.getCourseId()));
							clazz.setLimit(c.getLimit() < 0 ? null : c.getLimit());
							r.getClasses().add(clazz);
						}
					}
				}
			}
		}
		r.setStartDate(reservation.getStartDate());
		r.setExpirationDate(reservation.getExpirationDate());
		r.setExpired(reservation.isExpired());
		r.setLimit(reservation.getReservationLimit() < 0 ? null : reservation.getReservationLimit());
		r.setInclusive(reservation.isInclusive());
		r.setId(reservation.getReservationId());
		r.setOverride(reservation.getType() == XReservationType.IndividualOverride || reservation.getType() == XReservationType.CurriculumOverride || reservation.getType() == XReservationType.GroupOverride);
		r.setAllowOverlaps(reservation.isAllowOverlap());
		r.setMustBeUsed(reservation.mustBeUsed());
		r.setAlwaysExpired(reservation.isAlwaysExpired());
		r.setOverLimit(reservation.canAssignOverLimit());
		
		int enrolled = 0;
		if (enrollments != null) {
			for (XEnrollment enrollment: enrollments.getEnrollments()) {
				if (reservation.isApplicable(server.getStudent(enrollment.getStudentId()), enrollment) && reservation.isIncluded(enrollment.getConfigId(), offering.getSections(enrollment)))
					enrolled ++;
			}
		}
		r.setEnrollment(enrolled);

		return r;
	}

	@Override
	public String name() {
		return "get-reservations";
	}

}
