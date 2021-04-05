package org.unitime.timetable.onlinesectioning.specreg;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseResponse;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomSpecialRegistrationHolder;
import org.unitime.timetable.onlinesectioning.custom.SpecialRegistrationProvider;
import org.unitime.timetable.onlinesectioning.match.AnyCourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.util.DateUtils;

public class SpecialRegistrationRequestVariableTitleCourse implements OnlineSectioningAction<VariableTitleCourseResponse> {
	private static final long serialVersionUID = 1L;
	private VariableTitleCourseRequest iRequest;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	public SpecialRegistrationRequestVariableTitleCourse withRequest(VariableTitleCourseRequest request) {
		iRequest = request;
		return this;
	}

	public VariableTitleCourseRequest getRequest() { return iRequest; }
	public Long getStudentId() { return iRequest.getStudentId(); }

	@Override
	public VariableTitleCourseResponse execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.lockStudent(getStudentId(), null, name());
		try {
			OnlineSectioningLog.Action.Builder action = helper.getAction();
			
			Session session = SessionDAO.getInstance().get(server.getAcademicSession().getUniqueId(), helper.getHibSession());
			
			XStudent student = server.getStudent(getStudentId());

			action.getStudentBuilder().setUniqueId(student.getStudentId())
				.setExternalId(student.getExternalId())
				.setName(student.getName());
			
			helper.getAction().addOptionBuilder().setKey("course").setValue(getRequest().getCourse().getCourseName());
			if (getRequest().hasTitle())
				helper.getAction().addOptionBuilder().setKey("title").setValue(getRequest().getTitle());
			if (getRequest().getInstructor() != null)
				helper.getAction().addOptionBuilder().setKey("instructor").setValue(getRequest().getInstructor().getName());
			if (getRequest().hasNote())
				helper.getAction().addOptionBuilder().setKey("note").setValue(getRequest().getNote());
			if (getRequest().getCredit() != null)
				helper.getAction().addOptionBuilder().setKey("credit").setValue(getRequest().getCredit().toString());
			if (getRequest().hasGradeMode())
				helper.getAction().addOptionBuilder().setKey("gradeMode").setValue(getRequest().getGradeModeCode());
			if (getRequest().getStartDate() != null && getRequest().getEndDate() != null) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				helper.getAction().addOptionBuilder().setKey("dates").setValue(df.format(getRequest().getStartDate()) + " - " + df.format(getRequest().getEndDate()));
			}
			
			if (getRequest().getInstructor() != null && getRequest().isCheckIfExists()) {
				for (XCourseId courseId: server.findCourses(getRequest().getCourse().getCourseName(), null, new AnyCourseMatcher())) {
					XCourse course = (courseId instanceof XCourse ? (XCourse)courseId : server.getCourse(courseId.getCourseId()));
					if (course.getTitle() != null && course.getTitle().equalsIgnoreCase(getRequest().getTitle())) {
						XOffering offering = server.getOffering(courseId.getOfferingId());
						RequestedCourse rc = new RequestedCourse(courseId.getCourseId(), courseId.getCourseName());
						rc.setCourseTitle(course.getTitle());
						for (XConfig config: offering.getConfigs()) {
							for (XSubpart subpart: config.getSubparts()) {
								for (XSection section: subpart.getSections()) {
									for (XInstructor ins: section.getAllInstructors())
										if (ins.getIntructorId().equals(getRequest().getInstructor().getId()) &&
											getRequest().getStartDate().equals(getFirstDate(section, session)) &&
											getRequest().getEndDate().equals(getLastDate(section, session))) {
											rc.setSelectedClass(section.getSectionId(), section.getName(course.getCourseId()), true, true);
											break;
										}
								}
							}
						}
						if (rc.hasSelectedClasses())
							return new VariableTitleCourseResponse(rc);
					}
				}
			}
			
			SpecialRegistrationProvider specReg = CustomSpecialRegistrationHolder.getProvider();
			if (specReg == null)
				throw new SectioningException(MSG.exceptionNotSupportedFeature());
			
			return specReg.requestVariableTitleCourse(server, helper, student, getRequest());
		} finally {
			lock.release();
		}
	}
	
	protected Date getFirstDate(XSection section, Session session) {
		if (section.getTime() == null) return null;
		Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
		Date start = DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear()); 
    	cal.setTime(start);
    	int idx = section.getTime().getWeeks().nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	return cal.getTime();
	}
	
	protected Date getLastDate(XSection section, Session session) {
		if (section.getTime() == null) return null;
		Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	Date last = DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear()); 
    	cal.setTime(last);
    	int idx = section.getTime().getWeeks().length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	return cal.getTime();
	}
	
	@Override
	public String name() {
		return "req-var-course";
	}
}
