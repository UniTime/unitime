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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.CourseDemandDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck.EligibilityFlag;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationOperation;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationStatus;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.CancelSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.CancelSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationResponse;
import org.unitime.timetable.interfaces.ExternalClassLookupInterface;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.SpecialRegistrationProvider;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider.EnrollmentRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ApiMode;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CancelledRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Change;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ChangeError;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ChangeOperation;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ChangeStatus;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckEligibilityResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckRestrictionsRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckRestrictionsResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CompletionStatus;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.DeniedRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.EligibilityProblem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Problem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.RequestorRole;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ResponseStatus;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistration;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationCancelResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationResponseList;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationStatusResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SubmitRegistrationResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ValidationMode;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XOverride;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.util.DefaultExternalClassLookup;
import org.unitime.timetable.util.NameFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author Tomas Muller
 */
public class PurdueSpecialRegistrationProvider implements SpecialRegistrationProvider {
	private static Logger sLog = Logger.getLogger(PurdueSpecialRegistrationProvider.class);
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	private ExternalClassLookupInterface iExternalClassLookup;
	
	public PurdueSpecialRegistrationProvider() {
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
		Context cx = new Context();
		cx.getParameters().add("readTimeout", getSpecialRegistrationApiReadTimeout());
		iClient.setContext(cx);
		try {
			String clazz = ApplicationProperty.CustomizationExternalTerm.value();
			if (clazz == null || clazz.isEmpty())
				iExternalTermProvider = new BannerTermProvider();
			else
				iExternalTermProvider = (ExternalTermProvider)Class.forName(clazz).getConstructor().newInstance();
		} catch (Exception e) {
			sLog.error("Failed to create external term provider, using the default one instead.", e);
			iExternalTermProvider = new BannerTermProvider();
		}
		try {
			String clazz = ApplicationProperty.CustomizationExternalClassLookup.value();
			if (clazz == null || clazz.isEmpty())
				iExternalClassLookup = new DefaultExternalClassLookup();
			else
				iExternalClassLookup = (ExternalClassLookupInterface)Class.forName(clazz).getConstructor().newInstance();
		} catch (Exception e) {
			sLog.error("Failed to create external class lookup, using the default one instead.", e);
			iExternalClassLookup = new DefaultExternalClassLookup();
		}
	}
	
	protected String getSpecialRegistrationApiSite() {
		return ApplicationProperties.getProperty("purdue.specreg.site");
	}
	
	protected String getSpecialRegistrationApiReadTimeout() {
		return ApplicationProperties.getProperty("purdue.specreg.readTimeout", "60000");
	}
	
	protected String getSpecialRegistrationApiSiteSubmitRegistration() {
		return ApplicationProperties.getProperty("purdue.specreg.site.submitRegistration", getSpecialRegistrationApiSite() + "/submitRegistration");
	}

	protected String getSpecialRegistrationApiSiteCheckEligibility() {
		return ApplicationProperties.getProperty("purdue.specreg.site.checkEligibility", getSpecialRegistrationApiSite() + "/checkEligibility");
	}
	
	protected String getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus() {
		return ApplicationProperties.getProperty("purdue.specreg.site.checkSpecialRegistrationStatus", getSpecialRegistrationApiSite() + "/checkSpecialRegistrationStatus");
	}
	
	protected String getSpecialRegistrationApiCheckRestrictions() {
		return ApplicationProperties.getProperty("purdue.specreg.site.checkRestrictions", getSpecialRegistrationApiSite() + "/checkRestrictions");
	}
	
	protected String getSpecialRegistrationApiSiteCancelSpecialRegistration() {
		return ApplicationProperties.getProperty("purdue.specreg.site.cancelSpecialRegistration", getSpecialRegistrationApiSite() + "/cancelRegistrationRequestFromUniTime");
	}
	
	protected String getSpecialRegistrationApiKey() {
		return ApplicationProperties.getProperty("purdue.specreg.apiKey");
	}
	
	protected ApiMode getSpecialRegistrationMode() {
		return ApiMode.valueOf(ApplicationProperties.getProperty("purdue.specreg.mode", "REG"));
	}
	
	protected boolean isUpdateUniTimeStatuses() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.specreg.updateUniTimeStatuses", "true"));
	}
	
	protected boolean isAllowClosedErrorForAvailableSections() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.specreg.allowClosedWhenAvailable", "false"));
	}
	
	protected String getBannerTerm(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalTerm(session);
	}
	
	protected String getBannerCampus(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalCampus(session);
	}
	
	protected String getBannerId(XStudent student) {
		String id = student.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	protected String getRequestorId(OnlineSectioningLog.Entity user) {
		if (user == null || user.getExternalId() == null) return null;
		String id = user.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	protected RequestorRole getRequestorType(OnlineSectioningLog.Entity user, XStudent student) {
		if (user == null || user.getExternalId() == null) return null;
		if (user.hasType()) {
			switch (user.getType()) {
			case MANAGER: return RequestorRole.MANAGER;
			case STUDENT: return RequestorRole.STUDENT;
			default: return RequestorRole.MANAGER;
			}
		}
		return (user.getExternalId().equals(student.getExternalId()) ? RequestorRole.STUDENT : RequestorRole.MANAGER);
	}
	
	protected SpecialRegistrationStatus getStatus(ChangeStatus status) {
		if (status == null) return SpecialRegistrationStatus.Pending;
		switch (status) {
			case approved: return SpecialRegistrationStatus.Approved;
			case cancelled: return SpecialRegistrationStatus.Cancelled;
			case denied: return SpecialRegistrationStatus.Rejected;
			default: return SpecialRegistrationStatus.Pending;
		}
	}
	
	protected SpecialRegistrationStatus getStatus(CompletionStatus status) {
		if (status == null) return SpecialRegistrationStatus.Pending;
		switch (status) {
			case cancelled: return SpecialRegistrationStatus.Cancelled;
			case completed: return SpecialRegistrationStatus.Approved;
			default: return SpecialRegistrationStatus.Pending;
		}
	}
	
	protected SpecialRegistrationStatus getStatus(SpecialRegistration request) {
		SpecialRegistrationStatus ret = null;
		if (request.changes != null)
			for (Change ch: request.changes)
				ret = combine(ret, getStatus(ch.status));
		if (ret != null) return ret;
		return getStatus(request.completionStatus);
	}
	
	protected int toStatus(ChangeStatus status) {
		if (status == null) return CourseRequestOverrideStatus.PENDING.ordinal();
		switch (status) {
			case approved: return CourseRequestOverrideStatus.APPROVED.ordinal();
			case cancelled: return CourseRequestOverrideStatus.CANCELLED.ordinal();
			case denied: return CourseRequestOverrideStatus.REJECTED.ordinal();
			default: return CourseRequestOverrideStatus.PENDING.ordinal();
		}
	}
	
	protected int toStatus(SpecialRegistrationStatus status) {
		if (status == null) return CourseRequestOverrideStatus.PENDING.ordinal();
		switch (status) {
		case Approved: return CourseRequestOverrideStatus.APPROVED.ordinal();
		case Cancelled: return CourseRequestOverrideStatus.CANCELLED.ordinal();
		case Rejected: return CourseRequestOverrideStatus.REJECTED.ordinal();
		default: return CourseRequestOverrideStatus.PENDING.ordinal();
		}
	}
	
	protected int toStatus(SpecialRegistration request) {
		return toStatus(getStatus(request));
	}
	
	protected boolean isPending(ChangeStatus status) {
		return status != null && status != ChangeStatus.cancelled && status != ChangeStatus.approved && status != ChangeStatus.denied; 
	}
	
	protected SpecialRegistrationStatus combine(SpecialRegistrationStatus s1, SpecialRegistrationStatus s2) {
		if (s1 == null) return s2;
		if (s2 == null) return s1;
		if (s1 == s2) return s1;
		if (s1 == SpecialRegistrationStatus.Draft || s2 == SpecialRegistrationStatus.Draft) return SpecialRegistrationStatus.Draft;
		if (s1 == SpecialRegistrationStatus.Pending || s2 == SpecialRegistrationStatus.Pending) return SpecialRegistrationStatus.Pending;
		if (s1 == SpecialRegistrationStatus.Cancelled || s2 == SpecialRegistrationStatus.Cancelled) return SpecialRegistrationStatus.Cancelled;
		if (s1 == SpecialRegistrationStatus.Rejected || s2 == SpecialRegistrationStatus.Rejected) return SpecialRegistrationStatus.Rejected;
		if (s1 == SpecialRegistrationStatus.Approved || s2 == SpecialRegistrationStatus.Approved) return SpecialRegistrationStatus.Approved;
		return s1;
	}
	
	protected boolean canCancel(SpecialRegistration request) {
		if (request.changes != null) {
			for (Change ch: request.changes) {
				if (isPending(ch.status)) return true;
			}
		}
		return false;
	}
	
	protected void buildChangeList(SpecialRegistration request, OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, Collection<ClassAssignmentInterface.ClassAssignment> assignment, Collection<ErrorMessage> errors, Float credit) {
		request.changes = new ArrayList<Change>();
		float maxCredit = 0f;
		Map<XCourse, List<XSection>> enrollments = new HashMap<XCourse, List<XSection>>();
		Map<Long, XOffering> offerings = new HashMap<Long, XOffering>();
		for (ClassAssignmentInterface.ClassAssignment ca: assignment) {
			// Skip free times and dummy sections
			if (ca == null || ca.isFreeTime() || ca.getClassId() == null || ca.isDummy() || ca.isTeachingAssignment()) continue;
			
			XCourse course = server.getCourse(ca.getCourseId());
			if (course == null)
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(MSG.courseName(ca.getSubject(), ca.getClassNumber())));
			XOffering offering = server.getOffering(course.getOfferingId());
			if (offering == null)
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(MSG.courseName(ca.getSubject(), ca.getClassNumber())));
			
			// Check section limits
			XSection section = offering.getSection(ca.getClassId());
			if (section == null)
				throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
			
			// Check cancelled flag
			if (section.isCancelled()) {
				if (server.getConfig().getPropertyBoolean("Enrollment.CanKeepCancelledClass", false)) {
					boolean contains = false;
					for (XRequest r: student.getRequests())
						if (r instanceof XCourseRequest) {
							XCourseRequest cr = (XCourseRequest)r;
							if (cr.getEnrollment() != null && cr.getEnrollment().getSectionIds().contains(section.getSectionId())) { contains = true; break; }
						}
					if (!contains)
						throw new SectioningException(MSG.exceptionEnrollCancelled(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
				} else {
					throw new SectioningException(MSG.exceptionEnrollCancelled(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
				}
			}
			
			List<XSection> sections = enrollments.get(course);
			if (sections == null) {
				sections = new ArrayList<XSection>();
				enrollments.put(course, sections);
			}
			sections.add(section);
			offerings.put(course.getCourseId(), offering);
		}
		Set<String> crns = new HashSet<String>();
		check: for (Map.Entry<XCourse, List<XSection>> e: enrollments.entrySet()) {
			XCourse course = e.getKey();
			List<XSection> sections = e.getValue();
			
			Float sectionCredit = null;
			for (XSection s: sections) {
				Float creditOverride = s.getCreditOverride(course.getCourseId());
				if (creditOverride != null) {
					sectionCredit = (sectionCredit == null ? 0f : sectionCredit.floatValue()) + creditOverride;
				}
			}
			if (sectionCredit != null)
				maxCredit += sectionCredit;
			else if (course.hasCredit())
				maxCredit += course.getMinCredit();
			
			for (XRequest r: student.getRequests()) {
				if (r instanceof XCourseRequest) {
					XEnrollment enrollment = ((XCourseRequest)r).getEnrollment();
					if (enrollment != null && enrollment.getCourseId().equals(course.getCourseId())) { // course change
						for (XSection s: sections) {
							if (!enrollment.getSectionIds().contains(s.getSectionId())) {
								Change ch = new Change();
								ch.subject = course.getSubjectArea();
								ch.courseNbr = course.getCourseNumber();
								ch.crn = s.getExternalId(course.getCourseId());
								ch.operation = ChangeOperation.ADD;
								ch.credit = course.getCreditAbbv();
								if (crns.add(ch.crn)) request.changes.add(ch);
							}
						}
						for (Long id: enrollment.getSectionIds()) {
							XSection s = offerings.get(course.getCourseId()).getSection(id);
							if (!sections.contains(s)) {
								Change ch = new Change();
								ch.subject = course.getSubjectArea();
								ch.courseNbr = course.getCourseNumber();
								ch.crn = s.getExternalId(course.getCourseId());
								ch.operation = ChangeOperation.DROP;
								ch.credit = course.getCreditAbbv();
								if (crns.add(ch.crn)) request.changes.add(ch);
							}
						}
						continue check;
					}
				}
			}
			
			// new course
			for (XSection section: sections) {
				Change ch = new Change();
				ch.subject = course.getSubjectArea();
				ch.courseNbr = course.getCourseNumber();
				ch.crn = section.getExternalId(course.getCourseId());
				ch.operation = ChangeOperation.ADD;
				ch.credit = course.getCreditAbbv();
				if (crns.add(ch.crn)) request.changes.add(ch);
			}
		}
		
		// drop course
		for (XRequest r: student.getRequests()) {
			if (r instanceof XCourseRequest) {
				XEnrollment enrollment = ((XCourseRequest)r).getEnrollment();
				if (enrollment != null && !offerings.containsKey(enrollment.getCourseId())) {
					XOffering offering = server.getOffering(enrollment.getOfferingId());
					if (offering != null)
						for (XSection section: offering.getSections(enrollment)) {
							XCourse course = offering.getCourse(enrollment.getCourseId());
							Change ch = new Change();
							ch.subject = course.getSubjectArea();
							ch.courseNbr = course.getCourseNumber();
							ch.crn = section.getExternalId(course.getCourseId());
							ch.operation = ChangeOperation.DROP;
							ch.credit = course.getCreditAbbv();
							if (crns.add(ch.crn)) request.changes.add(ch);
						}
				}
			}
		}
		
		boolean maxi = false;
		if (errors != null) {
			Set<ErrorMessage> added = new HashSet<ErrorMessage>();
			for (Change ch: request.changes) {
				for (ErrorMessage m: errors)
					if (ch.crn.equals(m.getSection()) && added.add(m)) {
						if (ch.errors == null) ch.errors = new ArrayList<ChangeError>();
						ChangeError er = new ChangeError();
						er.code = m.getCode();
						er.message = m.getMessage();
						ch.errors.add(er);
					}
			}
			for (ErrorMessage m: errors) {
				if (m.getCourse() != null && added.add(m)) {
					Change ch = new Change();
					ch.subject = m.getCourse().substring(0, m.getCourse().lastIndexOf(' '));
					ch.courseNbr = m.getCourse().substring(m.getCourse().lastIndexOf(' ') + 1);
					ch.crn = m.getSection();
					ch.operation = ChangeOperation.KEEP;
					ch.errors = new ArrayList<ChangeError>();
					ChangeError er = new ChangeError();
					er.code = m.getCode();
					er.message = m.getMessage();
					ch.errors.add(er);
					request.changes.add(ch);
					XCourseId course = server.getCourse(m.getCourse());
					if (course != null) {
						XOffering offering = server.getOffering(course.getOfferingId());
						if (offering != null)
							ch.credit = offering.getCourse(course.getCourseId()).getCreditAbbv();
					}
				}
				if ("MAXI".equals(m.getCode())) maxi = true;
			}
		}
		
		if (credit != null && credit > maxCredit) maxCredit = credit;
		if (maxi || (student.getMaxCredit() != null && student.getMaxCredit() < maxCredit))
			request.maxCredit = maxCredit;
	}
	
	protected CourseRequest.CourseRequestOverrideIntent combine(Change change, CourseRequest.CourseRequestOverrideIntent oldIntent) {
		boolean ex = false;
		if (change.errors != null)
			for (ChangeError e: change.errors)
				if (e.code != null && e.code.startsWith("EX-")) ex = true;
		if (oldIntent != null)
			switch (oldIntent) {
			case EX_ADD:
			case EX_DROP:
			case EX_CHANGE:
				ex = true;
				break;
			}
		if (change.operation == ChangeOperation.ADD) {
			if (oldIntent != null)
				switch (oldIntent) {
				case ADD:
				case EX_ADD:
					return (ex ? CourseRequest.CourseRequestOverrideIntent.EX_ADD : CourseRequest.CourseRequestOverrideIntent.ADD);
				default:
					return (ex ? CourseRequest.CourseRequestOverrideIntent.EX_CHANGE : CourseRequest.CourseRequestOverrideIntent.CHANGE);
				}
			return (ex ? CourseRequest.CourseRequestOverrideIntent.EX_ADD : CourseRequest.CourseRequestOverrideIntent.ADD);
		} else if (change.operation == ChangeOperation.DROP) {
			if (oldIntent != null)
				switch (oldIntent) {
				case DROP:
				case EX_DROP:
					return (ex ? CourseRequest.CourseRequestOverrideIntent.EX_DROP : CourseRequest.CourseRequestOverrideIntent.DROP);
				default:
					return (ex ? CourseRequest.CourseRequestOverrideIntent.EX_CHANGE : CourseRequest.CourseRequestOverrideIntent.CHANGE);
				}
			return (ex ? CourseRequest.CourseRequestOverrideIntent.EX_DROP : CourseRequest.CourseRequestOverrideIntent.DROP);
		} else {
			return oldIntent;
		}
	}

	@Override
	public SpecialRegistrationEligibilityResponse checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, SpecialRegistrationEligibilityRequest input) throws SectioningException {
		if (student == null) return new SpecialRegistrationEligibilityResponse(false, "No student.");
		if (!isSpecialRegistrationEnabled(server, helper, student)) return new SpecialRegistrationEligibilityResponse(false, "Special registration is disabled.");
		
		CheckRestrictionsRequest req = new CheckRestrictionsRequest();
		req.term = getBannerTerm(server.getAcademicSession());
		req.campus = getBannerCampus(server.getAcademicSession());
		req.mode = getSpecialRegistrationMode();
		req.studentId = getBannerId(student);
		req.changes = SpecialRegistrationHelper.createValidationRequest(req, ValidationMode.REG, true);

		Set<String> current = new HashSet<String>();
		Set<String> keep = new HashSet<String>();
		Map<String, String> crn2course = new HashMap<String, String>();
		List<String> newCourses = new ArrayList<String>();
		Set<String> adds = new HashSet<String>();
		Map<String, XCourse> courses = new HashMap<String, XCourse>();
		Map<String, List<XSection>> crn2sections = new HashMap<String, List<XSection>>();
		for (XRequest r: student.getRequests())
			if (r instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)r;
				XEnrollment enr = cr.getEnrollment();
				if (enr != null) {
					XCourse course = server.getCourse(enr.getCourseId());
					if (course == null) continue;
					XOffering offering = server.getOffering(enr.getOfferingId());
					if (offering == null) continue;
					for (Long id: enr.getSectionIds()) {
						XSection section = offering.getSection(id);
						String crn = section.getExternalId(enr.getCourseId());
						current.add(crn);
						crn2course.put(crn, course.getCourseName());
						courses.put(course.getCourseName(), course);
						List<XSection> sections = crn2sections.get(crn);
						if (sections == null) {
							sections = new ArrayList<XSection>();
							crn2sections.put(crn, sections);
						}
						sections.add(section);
					}
				}
			}
		
		if (input.getClassAssignments() != null)
			for (ClassAssignment ca: input.getClassAssignments()) {
				if (ca == null || ca.isFreeTime() || ca.getClassId() == null || ca.isDummy() || ca.isTeachingAssignment()) continue;
				XCourse course = server.getCourse(ca.getCourseId());
				if (course == null) continue;
				XOffering offering = server.getOffering(course.getOfferingId());
				if (offering == null) continue;
				XSection section = offering.getSection(ca.getClassId());
				if (section == null) continue;
				String crn = section.getExternalId(course.getCourseId());
				if (current.contains(crn)) {
					keep.add(crn);
				} else if (adds.add(crn)) {
					SpecialRegistrationHelper.addCrn(req.changes, crn);
					crn2course.put(crn, course.getCourseName());
					if (!courses.containsKey(course.getCourseName())) {
						courses.put(course.getCourseName(), course);
						newCourses.add(crn);
					}
				}
				List<XSection> sections = crn2sections.get(crn);
				if (sections == null) {
					sections = new ArrayList<XSection>();
					crn2sections.put(crn, sections);
				}
				sections.add(section);
			}
		for (String crn: current)
			if (!keep.contains(crn))
				SpecialRegistrationHelper.dropCrn(req.changes, crn);
		
		CheckRestrictionsResponse resp = null;
		ClientResource resource = null;
		try {
			resource = new ClientResource(getSpecialRegistrationApiCheckRestrictions());
			resource.setNext(iClient);
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			Gson gson = getGson(helper);
			if (helper.isDebugEnabled())
				helper.debug("Request: " + gson.toJson(req));
			helper.getAction().addOptionBuilder().setKey("request").setValue(gson.toJson(req));
			long t1 = System.currentTimeMillis();
			
			resource.post(new GsonRepresentation<CheckRestrictionsRequest>(req));
			
			helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
			
			resp = (CheckRestrictionsResponse)new GsonRepresentation<CheckRestrictionsResponse>(resource.getResponseEntity(), CheckRestrictionsResponse.class).getObject();

			if (helper.isDebugEnabled())
				helper.debug("Response: " + gson.toJson(resp));
			helper.getAction().addOptionBuilder().setKey("validation_response").setValue(gson.toJson(resp));
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage() == null ? "null" : e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage() == null ? "null" : e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
		
		String message = null;
		if (resp.eligible != null) {
			if (ResponseStatus.success != resp.status)
				return new SpecialRegistrationEligibilityResponse(false, resp.message == null || resp.message.isEmpty() ? "Failed to check student eligibility (" + resp.status + ")." : resp.message);
			
			boolean eligible = true;
			if (resp.eligible == null || resp.eligible.eligible == null || !resp.eligible.eligible.booleanValue()) {
				eligible = false;
			}
			
			if (resp.eligible != null && resp.eligible.eligibilityProblems != null) {
				for (EligibilityProblem p: resp.eligible.eligibilityProblems)
					if (message == null)
						message = p.message;
					else
						message += "\n" + p.message;
			}

			if (!eligible)
				return new SpecialRegistrationEligibilityResponse(false, message != null ? message : "Student not eligible.");
		}
		
		SpecialRegistrationEligibilityResponse ret = new SpecialRegistrationEligibilityResponse(true, message);
		Set<String> ext = new HashSet<String>();
		if (resp.outJson != null && resp.outJson.problems != null) {
			Set<ErrorMessage> errors = new TreeSet<ErrorMessage>();
			for (Problem problem: resp.outJson.problems) {
				if ("CLOS".equals(problem.code) && !adds.contains(problem.crn)) {
					// Ignore closed section error on sections that are not being added
				} else if ("MAXI".equals(problem.code) && !newCourses.isEmpty()) {
					// Move max credit error message to the last added course
					String crn = newCourses.remove(newCourses.size() - 1);
					errors.add(new ErrorMessage(crn2course.get(crn), crn, problem.code, problem.message));
				} else {
					errors.add(new ErrorMessage(crn2course.get(problem.crn), problem.crn, problem.code, problem.message));
				}
				if (problem.code != null && problem.code.startsWith("EX-")) ext.add(problem.crn);
			}
			ret.setErrors(errors);
			if (resp.outJson.maxHoursCalc != null)
				ret.setCredit(resp.outJson.maxHoursCalc);
		}
		
		Set<ErrorMessage> denied = new TreeSet<ErrorMessage>();
		if (ret.hasErrors()) {
			for (ErrorMessage error: ret.getErrors()) {
				if (resp.overrides != null && !resp.overrides.contains(error.getCode())) {
					ret.setMessage((ret.hasMessage() ? ret.getMessage() + "\n" : "") + "No approvals are allowed for " + error + ".");
					ret.setCanSubmit(false);
					denied.add(new ErrorMessage(error.getCourse(), "", error.getCode(), "Approvals are not allowed for: " + error.getMessage()));
				} else if ("CLOS".equals(error.getCode()) && adds.contains(error.getSection()) && isAllowClosedErrorForAvailableSections()) {
					XCourse course = courses.get(error.getCourse());
					if (course == null) continue;
					// special handing of CLOS errors
					if (ext.contains(error.getSection())) {
						// is extended add: check availability
						XEnrollments enrollments = server.getEnrollments(course.getOfferingId());
						boolean available = true;
						for (XSection section: crn2sections.get(error.getSection())) {
							int enrl = (enrollments != null ? enrollments.countEnrollmentsForSection(section.getSectionId()) : 0);
							if (section.getLimit() >= 0 && section.getLimit() <= enrl) {
								available = false; break;
							}
						}
						if (!available) {
							ret.setMessage((ret.hasMessage() ? ret.getMessage() + "\n" : "") + course.getCourseName() + " does not allow approvals for " + error + ".");
							ret.setCanSubmit(false);
							denied.add(new ErrorMessage(course.getCourseName(), "", error.getCode(), "Approvals are not allowed for: " + error.getMessage()));
						}
					} else {
						// is open registration -- check course settings
						if (course != null && !course.isOverrideEnabled(error.getCode())) {
							ret.setMessage((ret.hasMessage() ? ret.getMessage() + "\n" : "") + course.getCourseName() + " does not allow approvals for " + error + ".");
							ret.setCanSubmit(false);
							denied.add(new ErrorMessage(course.getCourseName(), "", error.getCode(), "Approvals are not allowed for: " + error.getMessage()));
						}
					}
				} else {
					XCourse course = courses.get(error.getCourse());
					if (course != null && !course.isOverrideEnabled(error.getCode())) {
						ret.setMessage((ret.hasMessage() ? ret.getMessage() + "\n" : "") + course.getCourseName() + " does not allow approvals for " + error + ".");
						ret.setCanSubmit(false);
						denied.add(new ErrorMessage(course.getCourseName(), "", error.getCode(), "Approvals are not allowed for: " + error.getMessage()));
					}
				}
			}
		}
		
		Set<String> denials = new HashSet<String>();
		if (resp.deniedRequests != null && !resp.deniedRequests.isEmpty()) {
			for (DeniedRequest r: resp.deniedRequests) {
				String course = null;
				if (r.subject != null) {
					course = r.subject + " " + r.courseNbr;
				} else if (r.crn != null) {
					for (String crn: r.crn.split(",")) {
						CourseOffering c = findCourseByExternalId(server.getAcademicSession().getUniqueId(), crn);
						if (c != null) { course = c.getCourseName(); break; }
					}
				}
				if (course == null) continue;
				if (denials.add(course + ":" + r.code)) {
					ret.setMessage((ret.hasMessage() ? ret.getMessage() + "\n" : "") + course + ": " + r.errorMessage);
					ret.setCanSubmit(false);
					denied.add(new ErrorMessage(course, r.crn, r.code, r.errorMessage));
				}
			}
		}
		
		if (resp.cancelRegistrationRequests != null) {
			Set<ErrorMessage> errors = new TreeSet<ErrorMessage>();
			for (SpecialRegistration r: resp.cancelRegistrationRequests) {
				ret.addCancelRequestId(r.regRequestId);
				if (r.changes == null) continue;
				String maxi = null;
				Set<String> rAdds = new TreeSet<String>(), rDrops = new TreeSet<String>();
				for (Change ch: r.changes) {
					if (ch.subject != null && ch.courseNbr != null) {
						if (isPending(ch.status)) {
							if (ch.errors != null)
								for (ChangeError e: ch.errors) {
									errors.add(new ErrorMessage(ch.subject + " " + ch.courseNbr, ch.crn, e.code, e.message));
								}
						}
						if (ChangeOperation.ADD == ch.operation)
							rAdds.add(ch.subject + " " + ch.courseNbr);
						else
							rDrops.add(ch.subject + " " + ch.courseNbr);
					} else if (r.regRequestId.equals(input.getRequestId()) && isPending(ch.status)) {
						if (ch.errors != null)
							for (ChangeError e: ch.errors)
								if ("MAXI".equals(e.code)) maxi = e.message;
					}
				}
				if (maxi != null)
					for (String c: rAdds)
						if (!rDrops.contains(c))
							errors.add(new ErrorMessage(c, "", "MAXI", maxi));
			}
			if (!errors.isEmpty())
				ret.setCancelErrors(errors);
		}
		if (!denied.isEmpty())
			ret.setDeniedErrors(denied);
		
		return ret;
	}
	
	@Override
	public SubmitSpecialRegistrationResponse submitRegistration(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, SubmitSpecialRegistrationRequest input) throws SectioningException {
		ClientResource resource = null;
		try {
			SpecialRegistrationRequest request = new SpecialRegistrationRequest();
			AcademicSessionInfo session = server.getAcademicSession();
			request.term = getBannerTerm(session);
			request.campus = getBannerCampus(session);
			request.studentId = getBannerId(student);
			buildChangeList(request, server, helper, student, input.getClassAssignments(), input.getErrors(), input.getCredit());
			// buildChangeList(request, server, helper, student, input.getClassAssignments(), validate(server, helper, student, input.getClassAssignments()));
			request.regRequestId = input.getRequestId();
			request.mode = getSpecialRegistrationMode(); 
			if (helper.getUser() != null) {
				request.requestorId = getRequestorId(helper.getUser());
				request.requestorRole = getRequestorType(helper.getUser(), student);
			}
			request.requestorNotes = input.getNote();
			
			if (request.changes == null || request.changes.isEmpty())
				throw new SectioningException("There are no changes.");

			resource = new ClientResource(getSpecialRegistrationApiSiteSubmitRegistration());
			resource.setNext(iClient);
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			Gson gson = getGson(helper);
			if (helper.isDebugEnabled())
				helper.debug("Request: " + gson.toJson(request));
			helper.getAction().addOptionBuilder().setKey("specreg_request").setValue(gson.toJson(request));
			long t1 = System.currentTimeMillis();
			
			resource.post(new GsonRepresentation<SpecialRegistrationRequest>(request));
			
			helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
			
			SpecialRegistrationResponseList response = (SpecialRegistrationResponseList)new GsonRepresentation<SpecialRegistrationResponseList>(resource.getResponseEntity(), SpecialRegistrationResponseList.class).getObject();
			if (helper.isDebugEnabled())
				helper.debug("Response: " + gson.toJson(response));
			helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
			
			SubmitSpecialRegistrationResponse ret = new SubmitSpecialRegistrationResponse();
			ret.setMessage(response.message);
			ret.setSuccess(ResponseStatus.success == response.status);
			if (response.data != null && !response.data.isEmpty()) {
				ret.setStatus(getStatus(response.data.get(0)));
				for (SubmitRegistrationResponse r: response.data) {
					if (r.changes != null)
						for (Change ch: r.changes)
							if (ch.errors != null && !ch.errors.isEmpty() && ch.status == null)
								ch.status = ChangeStatus.inProgress;
					if (r.requestorNotes == null) r.requestorNotes = input.getNote();
					if (r.maxCredit == null && request.maxCredit != null) r.maxCredit = request.maxCredit;
					ret.addRequest(convert(server, helper, student, r, false));
					if (r.cancelledRequests != null)
						for (CancelledRequest c: r.cancelledRequests)
							ret.addCancelledRequest(c.regRequestId);
				}
			} else {
				ret.setSuccess(false);
			}
			
			if (isUpdateUniTimeStatuses() && response.data != null && !response.data.isEmpty()) {
				boolean studentChanged = false;
				for (SubmitRegistrationResponse r: response.data) {
					ChangeStatus maxiStatus = null;
					Map<String, Set<String>> course2errors = new HashMap<String, Set<String>>();
					Map<String, CourseRequest.CourseRequestOverrideIntent> course2intent = new HashMap<String, CourseRequest.CourseRequestOverrideIntent>();
					Map<String, SpecialRegistrationStatus> course2status = new HashMap<String, SpecialRegistrationStatus>();
					if (r.changes != null)
						for (Change ch: r.changes) {
							if (ch.subject != null && ch.courseNbr != null && ch.errors != null && !ch.errors.isEmpty()) {
								String course = ch.subject + " " + ch.courseNbr;
								course2intent.put(course, combine(ch, course2intent.get(course)));
								Set<String> errors = course2errors.get(course);
								if (errors == null) {
									errors = new TreeSet<String>();
									course2errors.put(course, errors);
								}
								for (ChangeError e: ch.errors) {
									if (e.message != null) errors.add(e.message);
								}
								if (ch.status != null) {
									SpecialRegistrationStatus s = course2status.get(course);
									course2status.put(course, s == null ? getStatus(ch.status) : combine(s, getStatus(ch.status)));
								}
							} else if (ch.crn == null && ch.errors != null) {
								for (ChangeError e: ch.errors) {
									if ("MAXI".equals(e.code))
										maxiStatus = ch.status;
								}
							}
						}
					if (r.maxCredit != null) {
						student.setMaxCreditOverride(new XOverride(r.regRequestId, r.dateCreated == null ? new Date() : r.dateCreated.toDate(), maxiStatus != null ? toStatus(maxiStatus) : toStatus(r)));
						Student dbStudent = StudentDAO.getInstance().get(student.getStudentId(), helper.getHibSession());
						if (dbStudent != null) {
							dbStudent.setOverrideStatus(maxiStatus != null ? toStatus(maxiStatus) : toStatus(r));
							dbStudent.setOverrideMaxCredit(r.maxCredit);
							dbStudent.setOverrideExternalId(r.regRequestId);
							dbStudent.setOverrideTimeStamp(r.dateCreated == null ? new Date() : r.dateCreated.toDate());
							helper.getHibSession().update(dbStudent);
						}
						studentChanged = true;
					}
					for (Map.Entry<String, Set<String>> e: course2errors.entrySet()) {
						XCourseRequest cr = student.getRequestForCourseName(e.getKey());
						if (cr != null) {
							String message = "";
							for (String m: e.getValue()) message += (m.isEmpty() ? "" : "\n") + m;
							if (message.length() > 255)
								message = message.substring(0, 252) + "...";
							cr.setEnrollmentMessage(message);
							for (XCourseId course: cr.getCourseIds()) {
								if (course.getCourseName().equals(e.getKey())) {
									cr.setOverride(course, new XOverride(r.regRequestId, r.dateCreated == null ? new Date() : r.dateCreated.toDate(), toStatus(course2status.get(e.getKey()))));
								}
							}
							CourseDemand dbCourseDemand = CourseDemandDAO.getInstance().get(cr.getRequestId(), helper.getHibSession());
							if (dbCourseDemand != null) {
								StudentEnrollmentMessage m = new StudentEnrollmentMessage();
								m.setCourseDemand(dbCourseDemand);
								m.setLevel(0);
								m.setType(0);
								m.setTimestamp(r.dateCreated == null ? new Date() : r.dateCreated.toDate());
								m.setMessage(message);
								m.setOrder(0);
								dbCourseDemand.getEnrollmentMessages().add(m);
								helper.getHibSession().update(dbCourseDemand);
								for (CourseRequest dbCourseRequest: dbCourseDemand.getCourseRequests()) {
									if (dbCourseRequest.getCourseOffering().getCourseName().equals(e.getKey())) {
										dbCourseRequest.setOverrideExternalId(r.regRequestId);
										dbCourseRequest.setOverrideStatus(toStatus(course2status.get(e.getKey())));
										dbCourseRequest.setOverrideTimeStamp(r.dateCreated == null ? new Date() : r.dateCreated.toDate());
										dbCourseRequest.setCourseRequestOverrideIntent(course2intent.get(e.getKey()));
										helper.getHibSession().update(dbCourseRequest);
									}
								}
							}
							studentChanged = true;
						}
					}
					if (ret.hasCancelledRequestIds()) {
						if (student.getMaxCreditOverride() != null && ret.isCancelledRequest(student.getMaxCreditOverride().getExternalId())) {
							student.getMaxCreditOverride().setStatus(CourseRequestOverrideStatus.CANCELLED.ordinal());
							Student dbStudent = StudentDAO.getInstance().get(student.getStudentId(), helper.getHibSession());
							if (dbStudent != null) {
								dbStudent.setOverrideStatus(CourseRequestOverrideStatus.CANCELLED.ordinal());
								helper.getHibSession().update(dbStudent);
							}
							studentChanged = true;
						}
						for (XRequest xr: student.getRequests()) {
							if (xr instanceof XCourseRequest) {
								XCourseRequest cr = (XCourseRequest)xr;
								if (cr.hasOverrides())
									for (Map.Entry<XCourseId, XOverride> e: cr.getOverrides().entrySet()) {
										if (ret.isCancelledRequest(e.getValue().getExternalId())) {
											e.getValue().setStatus(CourseRequestOverrideStatus.CANCELLED.ordinal());
											CourseDemand dbCourseDemand = CourseDemandDAO.getInstance().get(cr.getRequestId(), helper.getHibSession());
											if (dbCourseDemand != null) {
												for (CourseRequest dbCourseRequest: dbCourseDemand.getCourseRequests()) {
													if (dbCourseRequest.getCourseOffering().getUniqueId().equals(e.getKey().getCourseId())) {
														dbCourseRequest.setOverrideStatus(CourseRequestOverrideStatus.CANCELLED.ordinal());
														helper.getHibSession().update(dbCourseRequest);
													}
												}
											}
											studentChanged = true;
										}
									}
							}
						}
					}
				}
				
				if (studentChanged) {
					server.update(student, false);
					helper.getHibSession().flush();
				}
			}
			
			return ret;
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage() == null ? "null" : e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage() == null ? "null" : e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}

	@Override
	public void dispose() {
		try {
			iClient.stop();
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
		}	
	}
	
	protected CourseOffering findCourseByExternalId(Long sessionId, String externalId) {
		return iExternalClassLookup.findCourseByExternalId(sessionId, externalId);
	}
	
	protected List<Class_> findClassesByExternalId(Long sessionId, String externalId) {
		return iExternalClassLookup.findClassesByExternalId(sessionId, externalId);
	}
	
	protected boolean isDrop(XEnrollment enrollment,  List<Change> changes) {
		return false;
	}
	
	protected List<XRequest> getRequests(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, Map<CourseOffering, List<Class_>> adds, Map<CourseOffering, List<Class_>> drops) {
		Student dbStudent = StudentDAO.getInstance().get(student.getStudentId(), helper.getHibSession());
		List<XRequest> requests = new ArrayList<XRequest>();
		Set<CourseOffering> remaining = new HashSet<CourseOffering>(adds.keySet());
		
		for (XRequest request: student.getRequests()) {
			if (request instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)request;
				List<Class_> add = null;
				List<Class_> drop = null;
				XCourseId courseId = null;
				Long configId = null;
				for (XCourseId course: ((XCourseRequest)request).getCourseIds()) {
					for (Map.Entry<CourseOffering, List<Class_>> e: adds.entrySet()) 
						if (course.getCourseId().equals(e.getKey().getUniqueId())) {
							add = e.getValue();
							courseId = course;
							configId = e.getValue().iterator().next().getSchedulingSubpart().getInstrOfferingConfig().getUniqueId();
							remaining.remove(e.getKey());
						}
					for (Map.Entry<CourseOffering, List<Class_>> e: drops.entrySet()) 
						if (course.getCourseId().equals(e.getKey().getUniqueId())) {
							drop = e.getValue();
						}
				}
				if (add == null && drop == null) {
					// no change detected
					requests.add(request);
				} else {
					XEnrollment enrollemnt = cr.getEnrollment();
					Set<Long> classIds = (enrollemnt == null ? new HashSet<Long>() : new HashSet<Long>(enrollemnt.getSectionIds()));
					if (enrollemnt != null) {
						if (courseId != null) { // add -> check course & config
							if (!enrollemnt.getCourseId().equals(courseId.getCourseId()) && drop == null) {
								// different course and no drop -> create new course request
								requests.add(request);
								remaining.add(CourseOfferingDAO.getInstance().get(courseId.getCourseId(), helper.getHibSession()));
								continue;
							} else if (!enrollemnt.getConfigId().equals(configId)) {
								// same course different config -> drop all
								classIds.clear();
							}
						} else {
							courseId = enrollemnt;
							configId = enrollemnt.getConfigId();
						}
					}
					if (add != null)
						for (Class_ c: add) classIds.add(c.getUniqueId());
					if (drop != null)
						for (Class_ c: drop) classIds.remove(c.getUniqueId());
					if (classIds.isEmpty()) {
						requests.add(new XCourseRequest(cr, null));
					} else {
						requests.add(new XCourseRequest(cr, new XEnrollment(dbStudent, courseId, configId, classIds)));
					}
				}
			} else {
				// free time --> no change
				requests.add(request);
			}
		}
		for (CourseOffering course: remaining) {
			Long configId = null;
			Set<Long> classIds = new HashSet<Long>();
			for (Class_ clazz: adds.get(course)) {
				if (configId == null) configId = clazz.getSchedulingSubpart().getInstrOfferingConfig().getUniqueId();
				classIds.add(clazz.getUniqueId());
			}
			XCourseId courseId = new XCourseId(course);
			requests.add(new XCourseRequest(dbStudent, courseId, requests.size(), new XEnrollment(dbStudent, courseId, configId, classIds)));
		}
		return requests;
	}
	
	protected void checkRequests(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, List<XRequest> xrequests, Set<ErrorMessage> errors, boolean allowTimeConf, boolean allowSpaceConf) {
		List<EnrollmentRequest> requests = new ArrayList<EnrollmentRequest>();
		Hashtable<Long, XOffering> courseId2offering = new Hashtable<Long, XOffering>();
		for (XRequest req: xrequests) {
			if (!(req instanceof XCourseRequest)) continue;
			XCourseRequest courseReq = (XCourseRequest)req;
			XEnrollment e = courseReq.getEnrollment();
			if (e == null) continue;
			XCourse course = server.getCourse(e.getCourseId());
			if (course == null)
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(e.getCourseName()));
			EnrollmentRequest request = new EnrollmentRequest(course, new ArrayList<XSection>());
			requests.add(request);
			XOffering offering = server.getOffering(course.getOfferingId());
			if (offering == null)
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(e.getCourseName()));
			for (Long sectionId: e.getSectionIds()) {
				// Check section limits
				XSection section = offering.getSection(sectionId);
				if (section == null)
					throw new SectioningException(MSG.exceptionEnrollNotAvailable(e.getCourseName() + " " + sectionId));
				
				// Check cancelled flag
				if (section.isCancelled()) {
					if (server.getConfig().getPropertyBoolean("Enrollment.CanKeepCancelledClass", false)) {
						boolean contains = false;
						for (XRequest r: student.getRequests())
							if (r instanceof XCourseRequest) {
								XCourseRequest cr = (XCourseRequest)r;
								if (cr.getEnrollment() != null && cr.getEnrollment().getSectionIds().contains(section.getSectionId())) { contains = true; break; }
							}
						if (!contains)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_CANCEL, MSG.exceptionEnrollCancelled(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName(course.getCourseId())))));
					} else
						errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_CANCEL, MSG.exceptionEnrollCancelled(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName(course.getCourseId())))));
				}
				request.getSections().add(section);
				courseId2offering.put(course.getCourseId(), offering);
			}
		}
			
		// Check for NEW and CHANGE deadlines
		check: for (EnrollmentRequest request: requests) {
			XCourse course = request.getCourse();
			List<XSection> sections = request.getSections();

			for (XRequest r: student.getRequests()) {
				if (r instanceof XCourseRequest) {
					XEnrollment enrollment = ((XCourseRequest)r).getEnrollment();
					if (enrollment != null && enrollment.getCourseId().equals(course.getCourseId())) { // course change
						for (XSection s: sections)
							if (!enrollment.getSectionIds().contains(s.getSectionId()) && !server.checkDeadline(course.getCourseId(), s.getTime(), OnlineSectioningServer.Deadline.CHANGE))
								errors.add(new ErrorMessage(course.getCourseName(), s.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_DEADLINE, MSG.exceptionEnrollDeadlineChange(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), s.getSubpartName(), s.getName(course.getCourseId())))));
						continue check;
					}
				}
			}
			
			// new course
			for (XSection section: sections) {
				if (!server.checkDeadline(course.getOfferingId(), section.getTime(), OnlineSectioningServer.Deadline.NEW))
					errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_DEADLINE, MSG.exceptionEnrollDeadlineNew(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName(course.getCourseId())))));
			}
		}
		
		// Check for DROP deadlines
		for (XRequest r: student.getRequests()) {
			if (r instanceof XCourseRequest) {
				XEnrollment enrollment = ((XCourseRequest)r).getEnrollment();
				if (enrollment != null && !courseId2offering.containsKey(enrollment.getCourseId())) {
					XOffering offering = server.getOffering(enrollment.getOfferingId());
					if (offering != null)
						for (XSection section: offering.getSections(enrollment)) {
							if (!server.checkDeadline(offering.getOfferingId(), section.getTime(), OnlineSectioningServer.Deadline.DROP))
								errors.add(new ErrorMessage(enrollment.getCourseName(), section.getExternalId(enrollment.getCourseId()), ErrorMessage.UniTimeCode.UT_DEADLINE, MSG.exceptionEnrollDeadlineDrop(enrollment.getCourseName())));
						}
				}
			}
		}
		
		Hashtable<Long, XConfig> courseId2config = new Hashtable<Long, XConfig>();
		for (EnrollmentRequest request: requests) {
			XCourse course = request.getCourse();
			XOffering offering = courseId2offering.get(course.getCourseId());
			XEnrollments enrollments = server.getEnrollments(course.getOfferingId());
			List<XSection> sections = request.getSections();
			XSubpart subpart = offering.getSubpart(sections.get(0).getSubpartId());
			XConfig config = offering.getConfig(subpart.getConfigId());
			courseId2config.put(course.getCourseId(), config);

			XReservation reservation = null;
			reservations: for (XReservation r: offering.getReservations()) {
				if (!r.isApplicable(student, course)) continue;
				if (r.getLimit() >= 0 && r.getLimit() <= enrollments.countEnrollmentsForReservation(r.getReservationId())) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForReservation(r.getReservationId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain) continue;
				}
				if (!r.getConfigsIds().isEmpty() && !r.getConfigsIds().contains(config.getConfigId())) continue;
				for (XSection section: sections)
					if (r.getSectionIds(section.getSubpartId()) != null && !r.getSectionIds(section.getSubpartId()).contains(section.getSectionId())) continue reservations;
				if (reservation == null || r.compareTo(reservation) < 0)
					reservation = r;
			}
			
			if ((reservation == null || !reservation.canAssignOverLimit()) && !allowSpaceConf) {
				for (XSection section: sections) {
					if (section.getLimit() >= 0 && section.getLimit() <= enrollments.countEnrollmentsForSection(section.getSectionId())) {
						boolean contain = false;
						for (XEnrollment e: enrollments.getEnrollmentsForSection(section.getSectionId()))
							if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
						if (!contain)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_NOT_AVAILABLE, MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName()))));
					}
					if ((reservation == null || !offering.getSectionReservations(section.getSectionId()).contains(reservation)) && offering.getUnreservedSectionSpace(section.getSectionId(), enrollments) <= 0) {
						boolean contain = false;
						for (XEnrollment e: enrollments.getEnrollmentsForSection(section.getSectionId()))
							if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
						if (!contain)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_NOT_AVAILABLE, MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName()))));
					}
				}
				
				if (config.getLimit() >= 0 && config.getLimit() <= enrollments.countEnrollmentsForConfig(config.getConfigId())) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForConfig(config.getConfigId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain)
						for (XSection section: sections)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_NOT_AVAILABLE, MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName()))));
				}
				if ((reservation == null || !offering.getConfigReservations(config.getConfigId()).contains(reservation)) && offering.getUnreservedConfigSpace(config.getConfigId(), enrollments) <= 0) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForConfig(config.getConfigId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain)
						for (XSection section: sections)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_NOT_AVAILABLE, MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName()))));
				}
				
				if (course.getLimit() >= 0 && course.getLimit() <= enrollments.countEnrollmentsForCourse(course.getCourseId())) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForCourse(course.getCourseId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain)
						for (XSection section: sections)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_NOT_AVAILABLE, MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName()))));
				}
			}
		}
		
		for (EnrollmentRequest request: requests) {
			XCourse course = request.getCourse();
			XOffering offering = courseId2offering.get(course.getCourseId());
			List<XSection> sections = request.getSections();
			XSubpart subpart = offering.getSubpart(sections.get(0).getSubpartId());
			XConfig config = offering.getConfig(subpart.getConfigId());
			if (sections.size() < config.getSubparts().size()) {
				for (XSection section: sections)
					errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_STRUCTURE, MSG.exceptionEnrollmentIncomplete(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
			} else if (sections.size() > config.getSubparts().size()) {
				for (XSection section: sections)
					errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_STRUCTURE, MSG.exceptionEnrollmentInvalid(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
			}
			for (XSection s1: sections) {
				for (XSection s2: sections) {
					if (s1.getSectionId() < s2.getSectionId() && s1.isOverlapping(offering.getDistributions(), s2)) {
						errors.add(new ErrorMessage(course.getCourseName(), s1.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_TIME_CNF, MSG.exceptionEnrollmentOverlapping(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
						errors.add(new ErrorMessage(course.getCourseName(), s2.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_TIME_CNF, MSG.exceptionEnrollmentOverlapping(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
					}
					if (!s1.getSectionId().equals(s2.getSectionId()) && s1.getSubpartId().equals(s2.getSubpartId())) {
						errors.add(new ErrorMessage(course.getCourseName(), s1.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_STRUCTURE, MSG.exceptionEnrollmentInvalid(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
						errors.add(new ErrorMessage(course.getCourseName(), s2.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_STRUCTURE, MSG.exceptionEnrollmentInvalid(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
					}
				}
				if (!offering.getSubpart(s1.getSubpartId()).getConfigId().equals(config.getConfigId()))
					errors.add(new ErrorMessage(course.getCourseName(), s1.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_STRUCTURE, MSG.exceptionEnrollmentInvalid(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
			}
			if (!offering.isAllowOverlap(student, config.getConfigId(), course, sections) && !allowTimeConf)
				for (EnrollmentRequest otherRequest: requests) {
					XOffering other = courseId2offering.get(otherRequest.getCourse().getCourseId());
					XConfig otherConfig = courseId2config.get(otherRequest.getCourse().getCourseId());
					if (!other.equals(offering) && !other.isAllowOverlap(student, otherConfig.getConfigId(), otherRequest.getCourse(), otherRequest.getSections())) {
						List<XSection> assignment = otherRequest.getSections();
						for (XSection section: sections)
							if (section.isOverlapping(offering.getDistributions(), assignment))
								errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_TIME_CNF,MSG.exceptionEnrollmentConflicting(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
					}
				}
		}
	}
	
	protected boolean isCancelled(SpecialRegistration specialRequest) {
		if (specialRequest.changes != null)
			for (Change change: specialRequest.changes)
				if (ChangeStatus.cancelled == change.status) return true;
		return false;
	}
	
	protected RetrieveSpecialRegistrationResponse convert(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, SpecialRegistration specialRequest, boolean excludeApprovedOrRejected) {
		RetrieveSpecialRegistrationResponse ret = new RetrieveSpecialRegistrationResponse();
		Map<CourseOffering, Set<Class_>> adds = new HashMap<CourseOffering, Set<Class_>>();
		Map<CourseOffering, Set<Class_>> drops = new HashMap<CourseOffering, Set<Class_>>();
		Set<CourseOffering> keeps = new HashSet<CourseOffering>();
		Map<Class_, List<Change>> changes = new HashMap<Class_, List<Change>>();
		TreeSet<CourseOffering> courses = new TreeSet<CourseOffering>();
		SpecialRegistrationStatus status = null;
		String maxi = null;
		ChangeStatus maxStatus = null;
		String maxiNote = null;
		if (specialRequest.changes != null)
			for (Change change: specialRequest.changes) {
				if (change.crn == null || change.crn.isEmpty()) {
					if (change.errors != null)
						for (ChangeError err: change.errors)
							if ("MAXI".equals(err.code)) {
								maxi = err.message;
								maxStatus = change.status;
								maxiNote = SpecialRegistrationHelper.getLastNote(change);
								ret.setMaxCredit(specialRequest.maxCredit);
								if (specialRequest.maxCredit != null && student.getMaxCredit() != null) {
									DecimalFormat df = new DecimalFormat("0.#");
									maxi = "Maximum hours exceeded. Currently allowed " + df.format(student.getMaxCredit()) + " but needs " + df.format(specialRequest.maxCredit) + ".";
									if (student.getMaxCredit() >= specialRequest.maxCredit && getStatus(change.status) == SpecialRegistrationStatus.Pending)
										maxStatus = ChangeStatus.approved;
								}
							}
					continue;
				}
				for (String crn: change.crn.split(",")) {
					CourseOffering course = findCourseByExternalId(server.getAcademicSession().getUniqueId(), crn);
					List<Class_> classes = findClassesByExternalId(server.getAcademicSession().getUniqueId(), crn);
					if (course != null && classes != null && !classes.isEmpty()) {
						courses.add(course);
						Set<Class_> list = (ChangeOperation.DROP != change.operation ? adds : drops).get(course);
						if (ChangeOperation.KEEP == change.operation) keeps.add(course);
						if (list == null) {
							list = new TreeSet<Class_>(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
							 (ChangeOperation.DROP != change.operation ? adds : drops).put(course, list);
						}
						for (Class_ clazz: classes) {
							list.add(clazz);
							List<Change> ch = changes.get(clazz);
							if (ch == null) { ch = new ArrayList<Change>(); changes.put(clazz, ch); }
							ch.add(change);
						}
					}
					if (change.status != null)
						status = combine(status, getStatus(change.status));
				}
			}
		String desc = "";
		NameFormat nameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingInstructorNameFormat.value());
		for (CourseOffering course: courses) {
			if (!desc.isEmpty()) desc += ", ";
			desc += course.getCourseName();
			if (adds.containsKey(course)) {
				if (drops.containsKey(course)) {
					desc += " (change)";
				} else {
					desc += " (add)";
				}
			} else if (drops.containsKey(course)) {
				desc += " (drop)";
			}
			CourseCreditUnitConfig credit = course.getCredit();
			if (adds.containsKey(course)) {
				for (Class_ clazz: adds.get(course)) {
					ClassAssignment ca = new ClassAssignment();
					List<Change> change = changes.get(clazz);
					ca.setSpecRegOperation(ChangeOperation.ADD == change.get(0).operation ? SpecialRegistrationOperation.Add : SpecialRegistrationOperation.Keep);
					SpecialRegistrationStatus s = null;
					for (Change ch: change)
						if (ch.status != null)
							s = combine(s, getStatus(ch.status));
					ca.setSpecRegStatus(s);
					ca.setCourseId(course.getUniqueId());
					ca.setSubject(course.getSubjectAreaAbbv());
					ca.setCourseNbr(course.getCourseNbr());
					ca.setCourseAssigned(true);
					ca.setTitle(course.getTitle());
					ca.setClassId(clazz.getUniqueId());
					ca.setSection(clazz.getClassSuffix(course));
					if (ca.getSection() == null)
						ca.setSection(clazz.getSectionNumberString(helper.getHibSession()));
					ca.setClassNumber(clazz.getSectionNumberString(helper.getHibSession()));
					ca.setSubpart(clazz.getSchedulingSubpart().getItypeDesc().trim());
					ca.setExternalId(clazz.getExternalId(course));
					if (clazz.getParentClass() != null) {
						ca.setParentSection(clazz.getParentClass().getClassSuffix(course));
						if (ca.getParentSection() == null)
							ca.setParentSection(clazz.getParentClass().getSectionNumberString(helper.getHibSession()));
					}
					if (clazz.getSchedulePrintNote() != null)
						ca.addNote(clazz.getSchedulePrintNote());
					Placement placement = clazz.getCommittedAssignment() == null ? null : clazz.getCommittedAssignment().getPlacement();
					int minLimit = clazz.getExpectedCapacity();
                	int maxLimit = clazz.getMaxExpectedCapacity();
                	int limit = maxLimit;
                	if (minLimit < maxLimit && placement != null) {
                		// int roomLimit = Math.round((enrollment.getClazz().getRoomRatio() == null ? 1.0f : enrollment.getClazz().getRoomRatio()) * placement.getRoomSize());
                		int roomLimit = (int) Math.floor(placement.getRoomSize() / (clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()));
                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
                	}
                    if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = -1;
                    ca.setCancelled(clazz.isCancelled());
					ca.setLimit(new int[] { clazz.getEnrollment(), limit});
					if (placement != null) {
						if (placement.getTimeLocation() != null) {
							for (DayCode d : DayCode.toDayCodes(placement.getTimeLocation().getDayCode()))
								ca.addDay(d.getIndex());
							ca.setStart(placement.getTimeLocation().getStartSlot());
							ca.setLength(placement.getTimeLocation().getLength());
							ca.setBreakTime(placement.getTimeLocation().getBreakTime());
							ca.setDatePattern(placement.getTimeLocation().getDatePatternName());
						}
						if (clazz.getCommittedAssignment() != null)
							for (Location loc: clazz.getCommittedAssignment().getRooms())
								ca.addRoom(loc.getUniqueId(), loc.getLabelWithDisplayName());
					}
					if (clazz.getDisplayInstructor())
						for (ClassInstructor ci : clazz.getClassInstructors()) {
							if (!ci.isLead()) continue;
							ca.addInstructor(nameFormat.format(ci.getInstructor()));
							ca.addInstructoEmail(ci.getInstructor().getEmail() == null ? "" : ci.getInstructor().getEmail());
						}
					if (clazz.getSchedulingSubpart().getCredit() != null) {
						ca.setCredit(clazz.getSchedulingSubpart().getCredit().creditAbbv() + "|" + clazz.getSchedulingSubpart().getCredit().creditText());
					} else if (credit != null) {
						ca.setCredit(credit.creditAbbv() + "|" + credit.creditText());
					}
					Float creditOverride = clazz.getCredit(course);
					if (creditOverride != null) ca.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
					credit = null;
					if (ca.getParentSection() == null)
						ca.setParentSection(course.getConsentType() == null ? null : course.getConsentType().getLabel());
					
					for (Change ch: change) {
						if (ch.errors != null)
							for (ChangeError err: ch.errors) {
								if ("TIME".equals(err.code)) ret.setHasTimeConflict(true);
								if ("CLOS".equals(err.code)) ret.setHasSpaceConflict(true);
								if (err.code != null && err.code.startsWith("EX-")) ret.setExtended(true);
								if ("MAXI".equals(err.code) && maxi != null) continue;
								String message = err.message;
								switch (getStatus(ch.status)) {
								case Approved:
									message = "Approved: " + message;
									if (excludeApprovedOrRejected) continue;
									break;
								case Rejected:
									message = "Denied: " + message;
									if (excludeApprovedOrRejected) continue;
									break;
								}
								if (SpecialRegistrationHelper.hasLastNote(ch))
									message += "\n  <span class='note'>" + SpecialRegistrationHelper.getLastNote(ch) + "</span>";
								if (ch.status != null)
									message = "<span class='" + ch.status + "'>" + message + "</span>";
								if (ca.hasError())
									ca.setError(ca.getError() + "\n" + message);
								else
									ca.setError(message);
								if (isPending(ch.status))
									ret.addError(new ErrorMessage(ch.subject + " " + ch.courseNbr, ch.crn, err.code, err.message));
								ca.setPinned(true);
							}
					}
					
					if (!drops.containsKey(course) && !keeps.contains(course) && maxStatus != null && maxi != null && clazz.getSchedulingSubpart().getParentSubpart() == null) {
						boolean first = true;
						for (SchedulingSubpart ss: clazz.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts()) {
							if (ss.getParentSubpart() == null && ss.getItype().getItype() < clazz.getSchedulingSubpart().getItype().getItype()) { first = false; break; }
						}
						if (first) {
							String message = maxi;
							switch (getStatus(maxStatus)) {
							case Approved:
								message = "Approved: " + message;
								if (excludeApprovedOrRejected) continue;
								break;
							case Rejected:
								message = "Denied: " + message;
								if (excludeApprovedOrRejected) continue;
								break;
							}
							if (maxiNote != null && !maxiNote.toString().isEmpty())
								message += "\n  <span class='note'>" + maxiNote.trim() + "</span>";
							
							if (maxStatus != null)
								message = "<span class='" + maxStatus + "'>" + message + "</span>";
							if (ca.hasError())
								ca.setError(ca.getError() + "\n" + message);
							else
								ca.setError(message);
							if (ca.getSpecRegStatus() == null)
								ca.setSpecRegStatus(getStatus(maxStatus));
							if (isPending(maxStatus))
								ret.addError(new ErrorMessage(course.getCourseName(), "", "MAXI", maxi));
						}
					}
					ret.addChange(ca);
				}
			}
			if (drops.containsKey(course)) {
				for (Class_ clazz: drops.get(course)) {
					ClassAssignment ca = new ClassAssignment();
					List<Change> change = changes.get(clazz);
					ca.setSpecRegOperation(SpecialRegistrationOperation.Drop);
					SpecialRegistrationStatus s = null;
					for (Change ch: change)
						if (ch.status != null)
							s = combine(s, getStatus(ch.status));
					ca.setSpecRegStatus(s);
					ca.setCourseId(course.getUniqueId());
					ca.setSubject(course.getSubjectAreaAbbv());
					ca.setCourseNbr(course.getCourseNbr());
					ca.setCourseAssigned(false);
					ca.setTitle(course.getTitle());
					ca.setClassId(clazz.getUniqueId());
					ca.setSection(clazz.getClassSuffix(course));
					if (ca.getSection() == null)
						ca.setSection(clazz.getSectionNumberString(helper.getHibSession()));
					ca.setClassNumber(clazz.getSectionNumberString(helper.getHibSession()));
					ca.setSubpart(clazz.getSchedulingSubpart().getItypeDesc().trim());
					ca.setExternalId(clazz.getExternalId(course));
					if (clazz.getParentClass() != null) {
						ca.setParentSection(clazz.getParentClass().getClassSuffix(course));
						if (ca.getParentSection() == null)
							ca.setParentSection(clazz.getParentClass().getSectionNumberString(helper.getHibSession()));
					}
					if (clazz.getSchedulePrintNote() != null)
						ca.addNote(clazz.getSchedulePrintNote());
					Placement placement = clazz.getCommittedAssignment() == null ? null : clazz.getCommittedAssignment().getPlacement();
					int minLimit = clazz.getExpectedCapacity();
                	int maxLimit = clazz.getMaxExpectedCapacity();
                	int limit = maxLimit;
                	if (minLimit < maxLimit && placement != null) {
                		// int roomLimit = Math.round((enrollment.getClazz().getRoomRatio() == null ? 1.0f : enrollment.getClazz().getRoomRatio()) * placement.getRoomSize());
                		int roomLimit = (int) Math.floor(placement.getRoomSize() / (clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()));
                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
                	}
                    if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = -1;
                    ca.setCancelled(clazz.isCancelled());
					ca.setLimit(new int[] { clazz.getEnrollment(), limit});
					if (placement != null) {
						if (placement.getTimeLocation() != null) {
							for (DayCode d : DayCode.toDayCodes(placement.getTimeLocation().getDayCode()))
								ca.addDay(d.getIndex());
							ca.setStart(placement.getTimeLocation().getStartSlot());
							ca.setLength(placement.getTimeLocation().getLength());
							ca.setBreakTime(placement.getTimeLocation().getBreakTime());
							ca.setDatePattern(placement.getTimeLocation().getDatePatternName());
						}
						if (clazz.getCommittedAssignment() != null)
							for (Location loc: clazz.getCommittedAssignment().getRooms())
								ca.addRoom(loc.getUniqueId(), loc.getLabelWithDisplayName());
					}
					if (clazz.getDisplayInstructor())
						for (ClassInstructor ci : clazz.getClassInstructors()) {
							if (!ci.isLead()) continue;
							ca.addInstructor(nameFormat.format(ci.getInstructor()));
							ca.addInstructoEmail(ci.getInstructor().getEmail() == null ? "" : ci.getInstructor().getEmail());
						}
					if (clazz.getSchedulingSubpart().getCredit() != null) {
						ca.setCredit(clazz.getSchedulingSubpart().getCredit().creditAbbv() + "|" + clazz.getSchedulingSubpart().getCredit().creditText());
					} else if (credit != null) {
						ca.setCredit(credit.creditAbbv() + "|" + credit.creditText());
					}
					Float creditOverride = clazz.getCredit(course);
					if (creditOverride != null) ca.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
					credit = null;
					if (ca.getParentSection() == null)
						ca.setParentSection(course.getConsentType() == null ? null : course.getConsentType().getLabel());

					for (Change ch: change) {
						if (ch.errors != null)
							for (ChangeError err: ch.errors) {
								if ("TIME".equals(err.code)) ret.setHasTimeConflict(true);
								if ("CLOS".equals(err.code)) ret.setHasSpaceConflict(true);
								if ("MAXI".equals(err.code) && maxi != null) continue;
								String message = err.message;
								switch (getStatus(ch.status)) {
								case Approved:
									message = "Approved: " + message;
									if (excludeApprovedOrRejected) continue;
									break;
								case Rejected:
									message = "Denied: " + message;
									if (excludeApprovedOrRejected) continue;
									break;
								}
								if (SpecialRegistrationHelper.hasLastNote(ch))
									message += "\n  <span class='note'>" + SpecialRegistrationHelper.getLastNote(ch) + "</span>";
								if (ch.status != null)
									message = "<span class='" + ch.status + "'>" + message + "</span>";
								if (ca.hasError())
									ca.setError(ca.getError() + "\n" + message);
								else
									ca.setError(message);
								ca.setPinned(true);
								if (isPending(ch.status))
									ret.addError(new ErrorMessage(ch.subject + " " + ch.courseNbr, ch.crn, err.code, err.message));
							}
					}

					ret.addChange(ca);
				}
			}
		}
		
		/*
		List<XRequest> requests = getRequests(server, helper, student, adds, drops);
		checkRequests(server, helper, student, requests, errors, false, false);
		ret.setClassAssignments(GetAssignment.computeAssignment(server, helper, student, requests, null, errors, true));
		if (helper.getAction().getEnrollmentCount() > 0)
			helper.getAction().getEnrollmentBuilder(helper.getAction().getEnrollmentCount() - 1).setType(OnlineSectioningLog.Enrollment.EnrollmentType.EXTERNAL);
		helper.getAction().clearRequest();
		
		if (ret.hasClassAssignments())
			for (CourseAssignment course: ret.getClassAssignments().getCourseAssignments()) {
				if (course.isFreeTime() || course.isTeachingAssignment()) continue;
				List<Class_> add = null;
				for (Map.Entry<CourseOffering, List<Class_>> e: adds.entrySet())
					if (course.getCourseId().equals(e.getKey().getUniqueId())) { add = e.getValue(); break; }
				if (add != null)
					for (ClassAssignment ca: course.getClassAssignments())
						if (ca.isSaved())
							for (Class_ c: add)
								if (c.getUniqueId().equals(ca.getClassId())) ca.setSaved(false);
			}
		*/

		ret.setDescription(desc);
		ret.setRequestId(specialRequest.regRequestId);
		ret.setSubmitDate(specialRequest.dateCreated == null ? new Date() : specialRequest.dateCreated.toDate());
		ret.setNote(specialRequest.requestorNotes);
		ret.setStatus(getStatus(specialRequest));
		ret.setCanCancel(canCancel(specialRequest));
		return ret;
	}
	
	protected Gson getGson(OnlineSectioningHelper helper) {
		GsonBuilder builder = new GsonBuilder()
		.registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
			@Override
			public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.toString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
			}
		})
		.registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
			@Override
			public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				return new DateTime(json.getAsJsonPrimitive().getAsString(), DateTimeZone.UTC);
			}
		})
		.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(src));
			}
		})
		.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				try {
					return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getAsJsonPrimitive().getAsString());
				} catch (ParseException e) {
					throw new JsonParseException(e.getMessage(), e);
				}
			}
		});
		if (helper.isDebugEnabled()) builder.setPrettyPrinting();
		return builder.create();
	}

	@Override
	public List<RetrieveSpecialRegistrationResponse> retrieveAllRegistrations(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) throws SectioningException {
		if (student == null) return null;
		if (!isSpecialRegistrationEnabled(server, helper, student)) return null;
		ClientResource resource = null;
		try {
			resource = new ClientResource(getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus());
			resource.setNext(iClient);
			
			AcademicSessionInfo session = server.getAcademicSession();
			String term = getBannerTerm(session);
			String campus = getBannerCampus(session);
			resource.addQueryParameter("term", term);
			resource.addQueryParameter("campus", campus);
			resource.addQueryParameter("studentId", getBannerId(student));
			resource.addQueryParameter("mode", getSpecialRegistrationMode().name());
			helper.getAction().addOptionBuilder().setKey("term").setValue(term);
			helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			long t1 = System.currentTimeMillis();
			
			resource.get(MediaType.APPLICATION_JSON);
			
			helper.getAction().setApiGetTime(System.currentTimeMillis() - t1);
			
			SpecialRegistrationStatusResponse response = (SpecialRegistrationStatusResponse)new GsonRepresentation<SpecialRegistrationStatusResponse>(resource.getResponseEntity(), SpecialRegistrationStatusResponse.class).getObject();
			Gson gson = getGson(helper);
			
			if (helper.isDebugEnabled())
				helper.debug("Response: " + gson.toJson(response));
			helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
			
			if (isUpdateUniTimeStatuses() && response.data != null && response.data.requests != null && !response.data.requests.isEmpty()) {
				boolean studentChanged = false;
				Set<String> requestIds = new HashSet<String>();
				for (SpecialRegistration r: response.data.requests) {
					requestIds.add(r.regRequestId);
					if (r.maxCredit != null) {
						// max credit request -> get status
						ChangeStatus maxiStatus = null;
						if (r.changes != null)
							for (Change ch: r.changes) {
								if (ch.crn == null && ch.errors != null) {
									for (ChangeError e: ch.errors) {
										if ("MAXI".equals(e.code))
											maxiStatus = ch.status;
									}
								}
							}
						// check student status
						if (student.getMaxCreditOverride() != null && r.regRequestId.equals(student.getMaxCreditOverride().getExternalId()) && student.getMaxCreditOverride().getStatus() != (maxiStatus != null ? toStatus(maxiStatus) : toStatus(r))) {
							student.getMaxCreditOverride().setStatus(maxiStatus != null ?toStatus(maxiStatus) : toStatus(r));
							Student dbStudent = StudentDAO.getInstance().get(student.getStudentId(), helper.getHibSession());
							if (dbStudent != null) {
								dbStudent.setOverrideStatus(maxiStatus != null ? toStatus(maxiStatus) : toStatus(r));
								helper.getHibSession().update(dbStudent);
							}
							studentChanged = true;
						}
					}
					// check other statuses
					if (r.changes != null) {
						Map<String, SpecialRegistrationStatus> course2status = new HashMap<String, SpecialRegistrationStatus>();
						for (Change ch: r.changes) {
							if (ch.subject != null && ch.courseNbr != null && ch.errors != null && !ch.errors.isEmpty()) {
								String course = ch.subject + " " + ch.courseNbr;
								if (ch.status != null) {
									SpecialRegistrationStatus s = course2status.get(course);
									course2status.put(course, s == null ? getStatus(ch.status) : combine(s, getStatus(ch.status)));
								}
							}
						}
						for (Map.Entry<String, SpecialRegistrationStatus> e: course2status.entrySet()) {
							XCourseRequest cr = student.getRequestForCourseName(e.getKey());
							if (cr != null) {
								XCourseId id = cr.getCourseName(e.getKey());
								XOverride override = cr.getOverride(id);
								if (override != null && r.regRequestId.equals(override.getExternalId()) && toStatus(e.getValue()) != override.getStatus()) {
									override.setStatus(toStatus(e.getValue()));
									CourseDemand dbCourseDemand = CourseDemandDAO.getInstance().get(cr.getRequestId(), helper.getHibSession());
									if (dbCourseDemand != null) {
										for (CourseRequest dbCourseRequest: dbCourseDemand.getCourseRequests()) {
											if (dbCourseRequest.getCourseOffering().getUniqueId().equals(id.getCourseId())) {
												dbCourseRequest.setOverrideStatus(toStatus(e.getValue()));
												helper.getHibSession().update(dbCourseRequest);
											}
										}
									}
									studentChanged = true;
								}
							}
						}
					}
				}
				if (student.getMaxCreditOverride() != null && !requestIds.contains(student.getMaxCreditOverride().getExternalId())) {
					student.setMaxCreditOverride(null);
					Student dbStudent = StudentDAO.getInstance().get(student.getStudentId(), helper.getHibSession());
					if (dbStudent != null) {
						dbStudent.setOverrideStatus(null);
						dbStudent.setOverrideMaxCredit(null);
						dbStudent.setOverrideExternalId(null);
						dbStudent.setOverrideTimeStamp(null);
						helper.getHibSession().update(dbStudent);
					}
					studentChanged = true;
				}
				for (XRequest request: student.getRequests()) {
					if (request instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)request;
						if (cr.hasOverrides())
								for (Iterator<Map.Entry<XCourseId, XOverride>> i = cr.getOverrides().entrySet().iterator(); i.hasNext(); ) {
									Map.Entry<XCourseId, XOverride> e = i.next();
									if (!requestIds.contains(e.getValue().getExternalId())) {
										i.remove();
										CourseDemand dbCourseDemand = CourseDemandDAO.getInstance().get(cr.getRequestId(), helper.getHibSession());
									if (dbCourseDemand != null) {
										for (CourseRequest dbCourseRequest: dbCourseDemand.getCourseRequests()) {
											if (dbCourseRequest.getCourseOffering().getUniqueId().equals(e.getKey().getCourseId())) {
												dbCourseRequest.setOverrideStatus(null);
												dbCourseRequest.setOverrideExternalId(null);
												dbCourseRequest.setOverrideTimeStamp(null);
												dbCourseRequest.setOverrideIntent(null);
												helper.getHibSession().update(dbCourseRequest);
											}
										}
									}
										studentChanged = true;
									}
									
								}
					}
				}
				if (studentChanged) {
					server.update(student, false);
					helper.getHibSession().flush();
				}
			}
			
			if (response != null && ResponseStatus.success == response.status && response.data != null && response.data.requests != null) {
				List<RetrieveSpecialRegistrationResponse> ret = new ArrayList<RetrieveSpecialRegistrationResponse>(response.data.requests.size());
				for (SpecialRegistration specialRequest: response.data.requests)
					if (specialRequest.regRequestId != null && !isCancelled(specialRequest))
						ret.add(convert(server, helper, student, specialRequest, false));
				return ret;
			}
			
			return new ArrayList<RetrieveSpecialRegistrationResponse>();
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage() == null ? "null" : e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage() == null ? "null" : e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}

	@Override
	public void checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, EligibilityCheck check, XStudent student) throws SectioningException {
		if (student == null || !isSpecialRegistrationEnabled(server, helper, student)) {
			check.setFlag(EligibilityFlag.CAN_SPECREG, false);
			return;
		}
		if (!check.hasFlag(EligibilityFlag.CAN_ENROLL) && !"true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.specreg.allowExtended", "false"))) {
			check.setFlag(EligibilityFlag.CAN_SPECREG, false);
			return;
		}
		ClientResource resource = null;
		try {
			Gson gson = getGson(helper);

			resource = new ClientResource(getSpecialRegistrationApiSiteCheckEligibility());
			resource.setNext(iClient);
			
			AcademicSessionInfo session = server.getAcademicSession();
			String term = getBannerTerm(session);
			String campus = getBannerCampus(session);
			resource.addQueryParameter("term", term);
			resource.addQueryParameter("campus", campus);
			resource.addQueryParameter("studentId", getBannerId(student));
			resource.addQueryParameter("mode", getSpecialRegistrationMode().name());
			helper.getAction().addOptionBuilder().setKey("term").setValue(term);
			helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			long t1 = System.currentTimeMillis();
			
			resource.get(MediaType.APPLICATION_JSON);
			
			helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
			
			CheckEligibilityResponse response = (CheckEligibilityResponse)new GsonRepresentation<CheckEligibilityResponse>(resource.getResponseEntity(), CheckEligibilityResponse.class).getObject();
			
			if (helper.isDebugEnabled())
				helper.debug("Response: " + gson.toJson(response));
			helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
			
			check.setOverrideRequestDisclaimer(ApplicationProperties.getProperty("purdue.specreg.disclaimer"));
			
			if (response != null && ResponseStatus.success == response.status) {
				boolean eligible = true;
				if (response.data == null || response.data.eligible == null || !response.data.eligible.booleanValue()) {
					eligible = false;
				}
				check.setFlag(EligibilityFlag.CAN_SPECREG, eligible);
				if (eligible) {
					check.setOverrides(response.overrides);
					check.setFlag(EligibilityFlag.SR_TIME_CONF, check.hasOverride("TIME"));
					check.setFlag(EligibilityFlag.SR_LIMIT_CONF, check.hasOverride("CLOS"));
				}
			} else {
				check.setFlag(EligibilityFlag.CAN_SPECREG, false);
			}
			
			if (response.hasNonCanceledRequest != null && response.hasNonCanceledRequest.booleanValue())
				check.setFlag(EligibilityFlag.HAS_SPECREG, true);
			
			if (response != null && response.maxCredit != null && !response.maxCredit.equals(student.getMaxCredit())) {
				Student dbStudent = StudentDAO.getInstance().get(student.getStudentId(), helper.getHibSession());
				if (dbStudent != null) {
					dbStudent.setMaxCredit(response.maxCredit);
					helper.getHibSession().update(dbStudent);
					helper.getHibSession().flush();
				}
				student.setMaxCredit(response.maxCredit);
				server.update(student, false);
			}
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage() == null ? "null" : e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage() == null ? "null" : e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}
	
	protected boolean isSpecialRegistrationEnabled(org.unitime.timetable.model.Student student) {
		if (student == null) return false;
		StudentSectioningStatus status = student.getEffectiveStatus();
		return status == null || status.hasOption(StudentSectioningStatus.Option.specreg);
	}
	
	protected boolean isSpecialRegistrationEnabled(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) {
		if (student == null) return false;
		String status = student.getStatus();
		if (status == null) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status == null) return true;
		StudentSectioningStatus dbStatus = StudentSectioningStatus.getStatus(status, server.getAcademicSession().getUniqueId(), helper.getHibSession());
		return dbStatus != null && dbStatus.hasOption(StudentSectioningStatus.Option.specreg);
	}

	@Override
	public CancelSpecialRegistrationResponse cancelRegistration(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, CancelSpecialRegistrationRequest request) throws SectioningException {
		ClientResource resource = null;
		try {
			Gson gson = getGson(helper);

			resource = new ClientResource(getSpecialRegistrationApiSiteCancelSpecialRegistration());
			resource.setNext(iClient);
			
			AcademicSessionInfo session = server.getAcademicSession();
			String term = getBannerTerm(session);
			String campus = getBannerCampus(session);
			resource.addQueryParameter("term", term);
			resource.addQueryParameter("campus", campus);
			resource.addQueryParameter("studentId", getBannerId(student));
			resource.addQueryParameter("regRequestId", request.getRequestId());
			helper.getAction().addOptionBuilder().setKey("term").setValue(term);
			helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
			helper.getAction().addOptionBuilder().setKey("regRequestId").setValue(request.getRequestId());
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			long t1 = System.currentTimeMillis();
			
			resource.get(MediaType.APPLICATION_JSON);
			
			helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
			
			SpecialRegistrationCancelResponse response = (SpecialRegistrationCancelResponse)new GsonRepresentation<SpecialRegistrationCancelResponse>(resource.getResponseEntity(), SpecialRegistrationCancelResponse.class).getObject();
			
			if (helper.isDebugEnabled())
				helper.debug("Response: " + gson.toJson(response));
			helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
			
			CancelSpecialRegistrationResponse ret = new CancelSpecialRegistrationResponse();
			if (response != null) {
				ret.setSuccess(ResponseStatus.success == response.status);
				ret.setMessage(response.message);
			}
			
			if (isUpdateUniTimeStatuses() && ret.isSuccess()) {
				boolean studentChanged = false;
				if (student.getMaxCreditOverride() != null && request.getRequestId().equals(student.getMaxCreditOverride().getExternalId())) {
					XOverride override = student.getMaxCreditOverride();
					student.setMaxCreditOverride(new XOverride(override.getExternalId(), override.getTimeStamp(), CourseRequestOverrideStatus.CANCELLED.ordinal()));
					Student dbStudent = StudentDAO.getInstance().get(student.getStudentId(), helper.getHibSession());
					if (dbStudent != null) {
						dbStudent.setOverrideStatus(CourseRequestOverrideStatus.CANCELLED.ordinal());
						helper.getHibSession().update(dbStudent);
					}
					studentChanged = true;
				}
				for (XRequest xr: student.getRequests()) {
					if (xr instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)xr;
						if (cr.hasOverrides())
							for (Map.Entry<XCourseId, XOverride> e: cr.getOverrides().entrySet()) {
								if (request.getRequestId().equals(e.getValue().getExternalId())) {
									e.getValue().setStatus(CourseRequestOverrideStatus.CANCELLED.ordinal());
									CourseDemand dbCourseDemand = CourseDemandDAO.getInstance().get(cr.getRequestId(), helper.getHibSession());
									if (dbCourseDemand != null) {
										for (CourseRequest dbCourseRequest: dbCourseDemand.getCourseRequests()) {
											if (dbCourseRequest.getCourseOffering().getUniqueId().equals(e.getKey().getCourseId())) {
												dbCourseRequest.setOverrideStatus(CourseRequestOverrideStatus.CANCELLED.ordinal());
												helper.getHibSession().update(dbCourseRequest);
											}
										}
									}
									studentChanged = true;
								}
							}
					}
				}
				if (studentChanged) {
					server.update(student, false);
					helper.getHibSession().flush();
				}
			}
			
			return ret;
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage() == null ? "null" : e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage() == null ? "null" : e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}
}
