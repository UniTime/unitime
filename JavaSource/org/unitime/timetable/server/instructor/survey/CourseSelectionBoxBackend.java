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
package org.unitime.timetable.server.instructor.survey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.CourseDetail;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.ListAcademicClassifications;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.ListClasses;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.ListCourseOfferings;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.ListCurricula;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.RetrieveCourseDetail;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.CurriculaServlet;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CurriculaException;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

public class CourseSelectionBoxBackend {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	private static void checkPermissions(Long sessionId, SessionContext context) {
		boolean admin = context.hasPermissionAnySession(Right.InstructorSurveyAdmin, new Qualifiable[] { new SimpleQualifier("Session", sessionId)});
		if (!admin)
			context.hasPermissionAnySession(Right.InstructorSurvey, new Qualifiable[] { new SimpleQualifier("Session", sessionId)});
	}

	@GwtRpcImplements(ListCourseOfferings.class)
	public static class ListCoursesBackend implements GwtRpcImplementation<ListCourseOfferings, GwtRpcResponseList<CourseAssignment>> {
		@Override
		public GwtRpcResponseList<CourseAssignment> execute(ListCourseOfferings request, SessionContext context) {
			checkPermissions(request.getSessionId(), context);
			GwtRpcResponseList<CourseAssignment> results = new GwtRpcResponseList<CourseAssignment>();
			for (CourseOffering c: (List<CourseOffering>)CourseOfferingDAO.getInstance().getSession().createQuery(
					"select c from CourseOffering c where " +
					"c.subjectArea.session.uniqueId = :sessionId and (" +
					"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' or lower(c.courseNbr) like :q || '%' " +
					(request.getQuery().length() > 2 ? "or lower(c.title) like '%' || :q || '%'" : "") + ") " +
					"order by case " +
					"when lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' then 0 else 1 end," + // matches on course name first
					"c.subjectArea.subjectAreaAbbreviation, c.courseNbr")
					.setParameter("q", request.getQuery().toLowerCase(), org.hibernate.type.StringType.INSTANCE)
					.setParameter("sessionId", request.getSessionId(), org.hibernate.type.LongType.INSTANCE)
					.setCacheable(true)
					.setMaxResults(request.getLimit() == null || request.getLimit() < 0 ? Integer.MAX_VALUE : request.getLimit())
					.list()) {
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
				course.setCanWaitList(c.getInstructionalOffering().effectiveWaitList());
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
			return results;
		}
	}
	
	private static CourseOffering getCourse(Long sessionId, String courseName, Long courseId) {
		if (courseId != null) {
			CourseOffering co = CourseOfferingDAO.getInstance().get(courseId);
			if (co != null) return co;
		}
		for (CourseOffering co: (List<CourseOffering>)CourseOfferingDAO.getInstance().getSession().createQuery(
				"select c from CourseOffering c where " +
				"c.subjectArea.session.uniqueId = :sessionId and " +
				"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course")
				.setParameter("course", courseName.toLowerCase(), org.hibernate.type.StringType.INSTANCE)
				.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE)
				.setCacheable(true).setMaxResults(1).list()) {
			return co;
		}
		return null;
	}
	
	@GwtRpcImplements(RetrieveCourseDetail.class)
	public static class RetrieveCourseDetailBackend implements GwtRpcImplementation<RetrieveCourseDetail, CourseDetail> {
		@Override
		public CourseDetail execute(RetrieveCourseDetail request, SessionContext context) {
			checkPermissions(request.getSessionId(), context);
			CourseOffering courseOffering = getCourse(request.getSessionId(), request.getCourse(), request.getCourseId());
			if (courseOffering == null) throw new CurriculaException(MESSAGES.errorCourseDoesNotExist(request.getCourse()));
			CourseDetailsProvider provider = Customization.CourseDetailsProvider.getProvider();
			return new CourseDetail(provider.getDetails(
					new AcademicSessionInfo(courseOffering.getSubjectArea().getSession()),
					courseOffering.getSubjectAreaAbbv(), courseOffering.getCourseNbr()));
		}
	}
	
	@GwtRpcImplements(ListClasses.class)
	public static class ListClassesBackend implements GwtRpcImplementation<ListClasses, GwtRpcResponseList<ClassAssignment>> {
		@Override
		public GwtRpcResponseList<ClassAssignment> execute(ListClasses request, SessionContext context) {
			checkPermissions(request.getSessionId(), context);
			CourseOffering courseOffering = getCourse(request.getSessionId(), request.getCourse(), request.getCourseId());
			if (courseOffering == null) throw new CurriculaException(MESSAGES.errorCourseDoesNotExist(request.getCourse()));
			
			if (!context.hasPermission(Right.HasRole)) {
				Session session = SessionDAO.getInstance().get(request.getSessionId());
				if (session != null && !session.canNoRoleReportClass())
					throw new SectioningException(MSG.exceptionClassScheduleNotAvaiable());
			}
			
			GwtRpcResponseList<ClassAssignment> results = new GwtRpcResponseList<ClassAssignment>();
			NameFormat nameFormat = NameFormat.fromReference(UserProperty.NameFormat.get(context.getUser()));

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
				a.setClassNumber(clazz.getSectionNumberString());
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
			return results;
		}
	}
	
	@GwtRpcImplements(ListAcademicClassifications.class)
	public static class ListAcademicClassificationsBackend implements GwtRpcImplementation<ListAcademicClassifications, GwtRpcResponseList<AcademicClassificationInterface>> {
		@Override
		public GwtRpcResponseList<AcademicClassificationInterface> execute(final ListAcademicClassifications request, SessionContext context) {
			checkPermissions(request.getSessionId(), context);
			CurriculaServlet servlet = new CurriculaServlet() {
				@Override
				protected Long getAcademicSessionId() {
					return request.getSessionId();
				}
			};
			TreeSet<AcademicClassificationInterface> ret = servlet.loadAcademicClassifications();
			if (ret == null)
				return null;
			return new GwtRpcResponseList<AcademicClassificationInterface>(ret);
		}
	}

	
	@GwtRpcImplements(ListCurricula.class)
	public static class ListCurriculaBackend implements GwtRpcImplementation<ListCurricula, GwtRpcResponseList<CurriculumInterface>> {

		@Override
		public GwtRpcResponseList<CurriculumInterface> execute(final ListCurricula request, SessionContext context) {
			checkPermissions(request.getSessionId(), context);
			CurriculaServlet servlet = new CurriculaServlet() {
				@Override
				protected Long getAcademicSessionId() {
					return request.getSessionId();
				}
			};
			TreeSet<CurriculumInterface> ret = servlet.findCurriculaForACourse(request.getCourse());
			if (ret == null)
				return null;
			return new GwtRpcResponseList<CurriculumInterface>(ret);
		}

	}
}
