/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("/api/enrollments")
public class EnrollmentsConnector extends ApiConnector {
	
	@Override
	public void doGet(ApiHelper helper) throws IOException {
		String eventId = helper.getParameter("eventId");
		if (eventId != null) {
			Event event = EventDAO.getInstance().get(Long.parseLong(eventId), helper.getHibSession());
			if (event == null)
				throw new IllegalArgumentException("Event with the given ID does not exist.");
			
			helper.getSessionContext().checkPermission(event.getSession(), Right.ApiRetrieveEnrollments);

	    	helper.setResponse(convert(event.getStudentClassEnrollments()));
		}
		String classId = helper.getParameter("classId");
		if (classId != null) {
			Class_ clazz = Class_DAO.getInstance().get(Long.parseLong(classId), helper.getHibSession());
			if (clazz == null)
				throw new IllegalArgumentException("Class with the given ID does not exist.");
			
			helper.getSessionContext().checkPermission(clazz.getManagingDept().getSession(), Right.ApiRetrieveEnrollments);

			helper.setResponse(convert(clazz.getStudentEnrollments()));
		}
		String examId = helper.getParameter("examId");
		if (examId != null) {
			Exam exam = ExamDAO.getInstance().get(Long.parseLong(examId), helper.getHibSession());
			if (exam == null)
				throw new IllegalArgumentException("Examination with the given ID does not exist.");
			
			helper.getSessionContext().checkPermission(exam.getSession(), Right.ApiRetrieveEnrollments);

			helper.setResponse(convert(exam.getStudentClassEnrollments()));
		}
		String courseId = helper.getParameter("courseId");
		if (courseId != null) {
			CourseOffering course = CourseOfferingDAO.getInstance().get(Long.parseLong(courseId), helper.getHibSession());
			if (course == null)
				throw new IllegalArgumentException("Course with the given ID does not exist.");
			
			helper.getSessionContext().checkPermission(course.getInstructionalOffering().getSession(), Right.ApiRetrieveEnrollments);

			helper.setResponse(convert(CourseOfferingDAO.getInstance().getSession().createQuery(
					"from StudentClassEnrollment e where e.courseOffering.uniqueId = :courseId"
					).setLong("courseId", course.getUniqueId()).list()));
		}
		String offeringId = helper.getParameter("offeringId");
		if (offeringId != null) {
			InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(Long.parseLong(offeringId), helper.getHibSession());
			if (offering == null)
				throw new IllegalArgumentException("Offering with the given ID does not exist.");
			
			helper.getSessionContext().checkPermission(offering.getSession(), Right.ApiRetrieveEnrollments);

			helper.setResponse(convert(CourseOfferingDAO.getInstance().getSession().createQuery(
					"from StudentClassEnrollment e where e.courseOffering.instructionalOffering.uniqueId = :offeringId"
					).setLong("offeringId", offering.getUniqueId()).list()));
		}
		String configurationId = helper.getParameter("configurationId");
		if (configurationId != null) {
			InstrOfferingConfig config = InstrOfferingConfigDAO.getInstance().get(Long.parseLong(configurationId), helper.getHibSession());
			if (config == null)
				throw new IllegalArgumentException("Configuration with the given ID does not exist.");
			
			helper.getSessionContext().checkPermission(config.getInstructionalOffering().getSession(), Right.ApiRetrieveEnrollments);

			helper.setResponse(convert(CourseOfferingDAO.getInstance().getSession().createQuery(
					"from StudentClassEnrollment e where e.clazz.schedulingSubpart.instrOfferingConfig.uniqueId = :configId"
					).setLong("configId", config.getUniqueId()).list()));
		}
	}
	
	protected List<ClassEnrollmentInfo> convert(Collection<StudentClassEnrollment> enrollments) {
		List<ClassEnrollmentInfo> converted = new ArrayList<ClassEnrollmentInfo>();
		if (enrollments != null)
			for (StudentClassEnrollment enrollment: enrollments)
				converted.add(new ClassEnrollmentInfo(enrollment));
		return converted;
	}
	
	static class ClassEnrollmentInfo {
		Long iStudentId;
		String iExternalId;
		String iFirstName;
		String iMiddleName;
		String iLastName;
		String iTitle;
		String iEmail;
		String iSectioningStatus;
		List<String> iArea;
		List<String> iClassification;
		List<String> iMajor;
		List<String> iMinor;
		List<String> iGroup;
		List<String> iAccomodation;
		Long iCourseId;
		String iSubjectArea;
		String iCourseNumber;
		String iCourseTitle;
		Long iClassId;
		String iSubpart;
		String iSectionNumber;
		String iClassSuffix;
		String iClassExternalId;
		
		ClassEnrollmentInfo(StudentClassEnrollment enrollment) {
			iStudentId = enrollment.getStudent().getUniqueId();
			iExternalId = enrollment.getStudent().getExternalUniqueId();
			iFirstName = enrollment.getStudent().getFirstName();
			iMiddleName = enrollment.getStudent().getMiddleName();
			iLastName = enrollment.getStudent().getLastName();
			iTitle = enrollment.getStudent().getAcademicTitle();
			iEmail = enrollment.getStudent().getEmail();
			if (enrollment.getStudent().getSectioningStatus() != null)
				iSectioningStatus = enrollment.getStudent().getSectioningStatus().getReference();
			for (AcademicAreaClassification aac: enrollment.getStudent().getAcademicAreaClassifications()) {
				if (iArea == null) { iArea = new ArrayList<String>(); iClassification = new ArrayList<String>(); }
				iArea.add(aac.getAcademicArea().getAcademicAreaAbbreviation());
				iClassification.add(aac.getAcademicClassification().getCode());
			}
			for (PosMajor major: enrollment.getStudent().getPosMajors()) {
				if (iMajor == null) iMajor = new ArrayList<String>();
				iMajor.add(major.getCode());
			}
			for (PosMinor minor: enrollment.getStudent().getPosMinors()) {
				if (iMinor == null) iMinor = new ArrayList<String>();
				iMinor.add(minor.getCode());
			}
			for (StudentGroup group: enrollment.getStudent().getGroups()) {
				if (iGroup == null) iGroup = new ArrayList<String>();
				iGroup.add(group.getGroupAbbreviation());
			}
			for (StudentAccomodation accomodation: enrollment.getStudent().getAccomodations()) {
				if (iAccomodation == null) iAccomodation = new ArrayList<String>();
				iAccomodation.add(accomodation.getAbbreviation());
			}
			iCourseId = enrollment.getCourseOffering().getUniqueId();
			iSubjectArea = enrollment.getCourseOffering().getSubjectAreaAbbv();
			iCourseNumber = enrollment.getCourseOffering().getCourseNbr();
			iCourseTitle = enrollment.getCourseOffering().getTitle();
			iClassId = enrollment.getClazz().getUniqueId();
			iSectionNumber = enrollment.getClazz().getSectionNumberString();
			iSubpart = enrollment.getClazz().getSchedulingSubpart().getItypeDesc().trim();
			iClassSuffix = enrollment.getClazz().getClassSuffix(enrollment.getCourseOffering());
			iClassExternalId = enrollment.getClazz().getExternalId(enrollment.getCourseOffering());
		}
		
	}
	
	@Override
	protected String getName() {
		return "enrollments";
	}
}
