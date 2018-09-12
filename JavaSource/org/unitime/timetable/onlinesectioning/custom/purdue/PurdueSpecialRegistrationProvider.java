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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.cpsolver.coursett.model.Placement;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.comparators.ClassComparator;
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
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationRequest;
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
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Change;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ChangeError;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ChangeOperation;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckEligibilityResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckRestrictionsRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckRestrictionsResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.EligibilityProblem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Problem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.RequestStatus;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ResponseStatus;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.RestrictionsCheckRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.RestrictionsCheckResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationCancelResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationResponseList;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationStatusResponse;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XOffering;
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
	
	protected String getSpecialRegistrationApiSiteRetrieveRegistration() {
		return ApplicationProperties.getProperty("purdue.specreg.site.retrieveRegistration", getSpecialRegistrationApiSite() + "/retrieveRegistration");
	}
	
	protected String getSpecialRegistrationApiSiteSubmitRegistration() {
		return ApplicationProperties.getProperty("purdue.specreg.site.submitRegistration", getSpecialRegistrationApiSite() + "/submitRegistration");
	}

	protected String getSpecialRegistrationApiSiteCheckEligibility() {
		return ApplicationProperties.getProperty("purdue.specreg.site.checkEligibility", getSpecialRegistrationApiSite() + "/checkEligibility");
	}
	
	protected String getSpecialRegistrationApiSiteGetAllRegistrations() {
		return ApplicationProperties.getProperty("purdue.specreg.site.retrieveAllRegistrations", null); //getSpecialRegistrationApiSite() + "/retrieveAllRegistrations");
	}
	
	protected String getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus() {
		return ApplicationProperties.getProperty("purdue.specreg.site.checkSpecialRegistrationStatus", getSpecialRegistrationApiSite() + "/checkSpecialRegistrationStatus");
	}
	
	protected String getSpecialRegistrationApiValidationSite() {
		return ApplicationProperties.getProperty("purdue.specreg.site.validation", getSpecialRegistrationApiSite() + "/checkRestrictionsForOPEN");
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
	
	protected String getSpecialRegistrationMode() {
		return ApplicationProperties.getProperty("purdue.specreg.mode", "REG");
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
	
	protected String getRequestorType(OnlineSectioningLog.Entity user, XStudent student) {
		if (user == null || user.getExternalId() == null) return null;
		if (user.hasType()) return user.getType().name();
		return (user.getExternalId().equals(student.getExternalId()) ? "STUDENT" : "MANAGER");
	}
	
	protected SpecialRegistrationStatus getStatus(String status) {
		if (RequestStatus.mayEdit.name().equals(status) || RequestStatus.newRequest.name().equals(status) || RequestStatus.draft.name().equals(status))
			return SpecialRegistrationStatus.Draft;
		else if (RequestStatus.approved.name().equals(status))
			return SpecialRegistrationStatus.Approved;
		else if (RequestStatus.cancelled.name().equals(status))
			return SpecialRegistrationStatus.Cancelled;
		else if (RequestStatus.denied.name().equals(status))
			return SpecialRegistrationStatus.Rejected;
		else
			return SpecialRegistrationStatus.Pending;
	}
	
	protected boolean isPending(String status) {
		return status != null && !RequestStatus.cancelled.name().equals(status) && !RequestStatus.approved.name().equals(status) && !RequestStatus.denied.name().equals(status); 
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
	
	protected boolean canCancel(SpecialRegistrationRequest request) {
		if (request.changes != null) {
			for (Change ch: request.changes) {
				if (isPending(ch.status)) return true;
			}
		}
		return false;
	}
	
	protected void buildChangeList(SpecialRegistrationRequest request, OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, Collection<ClassAssignmentInterface.ClassAssignment> assignment, Collection<ErrorMessage> errors) {
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
			
			if (course.hasCredit())
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
								ch.operation = ChangeOperation.ADD.name();
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
								ch.operation = ChangeOperation.DROP.name();
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
				ch.operation = ChangeOperation.ADD.name();
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
							ch.operation = ChangeOperation.DROP.name();
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
				if (added.add(m)) {
					Change ch = new Change();
					ch.subject = m.getCourse().substring(0, m.getCourse().lastIndexOf(' '));
					ch.courseNbr = m.getCourse().substring(m.getCourse().lastIndexOf(' ') + 1);
					ch.crn = m.getSection();
					ch.operation = ChangeOperation.KEEP.name();
					ch.errors = new ArrayList<ChangeError>();
					ChangeError er = new ChangeError();
					er.code = m.getCode();
					er.message = m.getMessage();
					ch.errors.add(er);
					request.changes.add(ch);
				}
				if ("MAXI".equals(m.getCode())) maxi = true;
			}
		}
		
		if (maxi || (student.getMaxCredit() != null && student.getMaxCredit() < maxCredit))
			request.maxCredit = maxCredit;
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
		req.changes = new RestrictionsCheckRequest();
		req.changes.sisId = req.studentId;
		req.changes.term = req.term;
		req.changes.campus = req.campus;
		req.changes.includeReg = "Y";
		req.changes.mode = req.mode;

		Set<String> current = new HashSet<String>();
		Map<String, String> crn2course = new HashMap<String, String>();
		List<String> newCourses = new ArrayList<String>();
		Set<String> adds = new HashSet<String>();
		Map<String, XCourse> courses = new HashMap<String, XCourse>();
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
				if (!current.remove(crn)) {
					req.changes.add(crn);
					crn2course.put(crn, course.getCourseName());
					if (!courses.containsKey(course.getCourseName())) {
						courses.put(course.getCourseName(), course);
						newCourses.add(crn);
					}
					adds.add(crn);
				}
			}
		for (String crn: current)
			req.changes.drop(crn);
		
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
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage());
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
			if (!ResponseStatus.success.name().equals(resp.eligible.status))
				return new SpecialRegistrationEligibilityResponse(false, resp.eligible.message == null || resp.eligible.message.isEmpty() ? "Failed to check student eligibility (" + resp.eligible.status + ")." : resp.eligible.message);
			
			boolean eligible = true;
			if (resp.eligible.data == null || resp.eligible.data.eligible == null || !resp.eligible.data.eligible.booleanValue()) {
				eligible = false;
			}
			
			if (resp.eligible.data != null && resp.eligible.data.eligibilityProblems != null) {
				for (EligibilityProblem p: resp.eligible.data.eligibilityProblems)
					if (message == null)
						message = p.message;
					else
						message += "\n" + p.message;
			}

			if (!eligible)
				return new SpecialRegistrationEligibilityResponse(false, message != null ? message : "Student not eligible.");
		}
		
		SpecialRegistrationEligibilityResponse ret = new SpecialRegistrationEligibilityResponse(true, message);
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
			}
			ret.setErrors(errors);
		}
		
		if (ret.hasErrors() && resp.overrides != null) {
			for (ErrorMessage error: ret.getErrors()) {
				if (!resp.overrides.contains(error.getCode()))
					return new SpecialRegistrationEligibilityResponse(false, "No overrides are allowed for " + error + ".");
				XCourse course = courses.get(error.getCourse());
				if (course != null && !course.isOverrideEnabled(error.getCode()))
					return new SpecialRegistrationEligibilityResponse(false, course.getCourseName() + " does not allow overrides for " + error + ".");
			}
		}
		
		if (resp.cancelRegistrationRequests != null) {
			Set<ErrorMessage> errors = new TreeSet<ErrorMessage>();
			Set<String> denials = new HashSet<String>();
			for (SpecialRegistrationRequest r: resp.cancelRegistrationRequests) {
				ret.addCancelRequestId(r.requestId);
				if (r.changes == null) continue;
				String maxi = null;
				if (r.requestId.equals(input.getRequestId())) {
					for (Change ch: r.changes) {
						if (RequestStatus.denied.name().equals(ch.status)) {
							String course = ch.subject + " " + ch.courseNbr;
							for (ErrorMessage error: ret.getErrors())
								if (course.equals(error.getCourse())) {
									if (ch.errors != null)
										for (ChangeError e: ch.errors) {
											if (e.code.equals(error.getCode()) && denials.add(course + ":" + e.code)) {
												ret.setMessage((ret.hasMessage() ? ret.getMessage() + "\n" : "") + error.getCourse() + " " + error.getMessage() + " has been denied.");
												ret.setCanSubmit(false);
											}
										}
								}
						}
					}
				}
				Set<String> rAdds = new TreeSet<String>(), rDrops = new TreeSet<String>();
				for (Change ch: r.changes) {
					if (ch.subject != null && ch.courseNbr != null) {
						if (isPending(ch.status)) {
							if (ch.errors != null)
								for (ChangeError e: ch.errors) {
									errors.add(new ErrorMessage(ch.subject + " " + ch.courseNbr, ch.crn, e.code, e.message));
								}
						}
						if (ChangeOperation.ADD.name().equals(ch.operation))
							rAdds.add(ch.subject + " " + ch.courseNbr);
						else
							rDrops.add(ch.subject + " " + ch.courseNbr);
					} else if (r.requestId.equals(input.getRequestId()) && isPending(ch.status)) {
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
		return ret;
	}
	
	protected Set<ErrorMessage> validate(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, Collection<ClassAssignment> classAssignments) {
		if (getSpecialRegistrationApiValidationSite() == null) return null;
		RestrictionsCheckRequest req = new RestrictionsCheckRequest();
		req.sisId = getBannerId(student);
		req.term = getBannerTerm(server.getAcademicSession());
		req.campus = getBannerCampus(server.getAcademicSession());
		req.mode = getSpecialRegistrationMode();
		req.includeReg = "Y";
		Set<String> current = new HashSet<String>();
		Map<String, String> crn2course = new HashMap<String, String>();
		List<String> newCourses = new ArrayList<String>();
		Set<String> adds = new HashSet<String>();
		Set<String> courses = new HashSet<String>();
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
						courses.add(course.getCourseName());
					}
				}
			}
		
		if (classAssignments != null)
			for (ClassAssignment ca: classAssignments) {
				if (ca == null || ca.isFreeTime() || ca.getClassId() == null || ca.isDummy() || ca.isTeachingAssignment()) continue;
				XCourse course = server.getCourse(ca.getCourseId());
				if (course == null) continue;
				XOffering offering = server.getOffering(course.getOfferingId());
				if (offering == null) continue;
				XSection section = offering.getSection(ca.getClassId());
				if (section == null) continue;
				String crn = section.getExternalId(course.getCourseId());
				if (!current.remove(crn)) {
					req.add(crn);
					crn2course.put(crn, course.getCourseName());
					if (courses.add(course.getCourseName())) newCourses.add(crn);
					adds.add(crn);
				}
			}
		for (String crn: current)
			req.drop(crn);
		
		Set<ErrorMessage> errors = new TreeSet<ErrorMessage>();
		if (req.actions != null) {
			RestrictionsCheckResponse resp = null;
			ClientResource resource = null;
			try {
				resource = new ClientResource(getSpecialRegistrationApiValidationSite());
				resource.setNext(iClient);
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				Gson gson = getGson(helper);
				if (helper.isDebugEnabled())
					helper.debug("Request: " + gson.toJson(req));
				helper.getAction().addOptionBuilder().setKey("validation_request").setValue(gson.toJson(req));
				long t1 = System.currentTimeMillis();
				
				resource.post(new GsonRepresentation<RestrictionsCheckRequest>(req));
				
				helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
				
				resp = (RestrictionsCheckResponse)new GsonRepresentation<RestrictionsCheckResponse>(resource.getResponseEntity(), RestrictionsCheckResponse.class).getObject();
				if (helper.isDebugEnabled())
					helper.debug("Response: " + gson.toJson(resp));
				helper.getAction().addOptionBuilder().setKey("validation_response").setValue(gson.toJson(resp));
			} catch (SectioningException e) {
				helper.getAction().setApiException(e.getMessage());
				throw (SectioningException)e;
			} catch (Exception e) {
				helper.getAction().setApiException(e.getMessage());
				sLog.error(e.getMessage(), e);
				throw new SectioningException(e.getMessage());
			} finally {
				if (resource != null) {
					if (resource.getResponse() != null) resource.getResponse().release();
					resource.release();
				}
			}
			
			if (resp != null && resp.problems != null)
				for (Problem problem: resp.problems) {
					if ("CLOS".equals(problem.code) && !adds.contains(problem.crn)) {
						// Ignore closed section error on sections that are not being added
					} else if ("MAXI".equals(problem.code) && !newCourses.isEmpty()) {
						// Move max credit error message to the last added course
						String crn = newCourses.remove(newCourses.size() - 1);
						errors.add(new ErrorMessage(crn2course.get(crn), crn, problem.code, problem.message));
					} else {
						errors.add(new ErrorMessage(crn2course.get(problem.crn), problem.crn, problem.code, problem.message));
					}
				}
		}
		
		return errors;
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
			buildChangeList(request, server, helper, student, input.getClassAssignments(), input.hasErrors() ? input.getErrors() : validate(server, helper, student, input.getClassAssignments()));
			// buildChangeList(request, server, helper, student, input.getClassAssignments(), validate(server, helper, student, input.getClassAssignments()));
			request.requestId = input.getRequestId();
			request.mode = getSpecialRegistrationMode(); 
			if (helper.getUser() != null) {
				request.requestorId = getRequestorId(helper.getUser());
				request.requestorRole = getRequestorType(helper.getUser(), student);
			}
			request.notes = input.getNote();
			
			if (request.changes == null || request.changes.isEmpty())
				throw new SectioningException("There are no changes.");

			resource = new ClientResource(getSpecialRegistrationApiSiteSubmitRegistration());
			resource.setNext(iClient);
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			if (input.getRequestKey() != null && !input.getRequestKey().isEmpty())
				resource.addQueryParameter("reqKey", input.getRequestKey());
			
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
			ret.setSuccess(ResponseStatus.success.name().equals(response.status));
			if (response.data != null && !response.data.isEmpty()) {
				ret.setStatus(getStatus(response.data.get(0).status));
				for (SpecialRegistrationRequest r: response.data) {
					if (r.changes != null)
						for (Change ch: r.changes)
							if (ch.errors != null && !ch.errors.isEmpty() && ch.status == null)
								ch.status = RequestStatus.inProgress.name();
					ret.addRequest(convert(server, helper, student, r, false));
				}
			} else {
				ret.setSuccess(false);
			}
			return ret;
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage());
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
	
	protected boolean isCancelled(SpecialRegistrationRequest specialRequest) {
		if (specialRequest.changes != null)
			for (Change change: specialRequest.changes)
				if (RequestStatus.cancelled.name().equals(change.status)) return true;
		return false;
	}
	
	protected RetrieveSpecialRegistrationResponse convert(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, SpecialRegistrationRequest specialRequest, boolean excludeApprovedOrRejected) {
		RetrieveSpecialRegistrationResponse ret = new RetrieveSpecialRegistrationResponse();
		Map<CourseOffering, Set<Class_>> adds = new HashMap<CourseOffering, Set<Class_>>();
		Map<CourseOffering, Set<Class_>> drops = new HashMap<CourseOffering, Set<Class_>>();
		Set<CourseOffering> keeps = new HashSet<CourseOffering>();
		Map<Class_, List<Change>> changes = new HashMap<Class_, List<Change>>();
		TreeSet<CourseOffering> courses = new TreeSet<CourseOffering>();
		SpecialRegistrationStatus status = null;
		String maxi = null;
		String maxStatus = null;
		String maxiNote = null;
		if (specialRequest.changes != null)
			for (Change change: specialRequest.changes) {
				if (change.crn == null || change.crn.isEmpty()) {
					if (change.errors != null)
						for (ChangeError err: change.errors)
							if ("MAXI".equals(err.code)) {
								maxi = err.message;
								maxStatus = change.status;
								maxiNote = change.getLastNote();
							}
					continue;
				}
				for (String crn: change.crn.split(",")) {
					CourseOffering course = findCourseByExternalId(server.getAcademicSession().getUniqueId(), crn);
					List<Class_> classes = findClassesByExternalId(server.getAcademicSession().getUniqueId(), crn);
					if (course != null && classes != null && !classes.isEmpty()) {
						courses.add(course);
						Set<Class_> list = (!ChangeOperation.DROP.name().equals(change.operation) ? adds : drops).get(course);
						if (ChangeOperation.KEEP.name().equals(change.operation)) keeps.add(course);
						if (list == null) {
							list = new TreeSet<Class_>(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
							 (!ChangeOperation.DROP.name().equals(change.operation) ? adds : drops).put(course, list);
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
					ca.setSpecRegOperation(ChangeOperation.ADD.name().equals(change.get(0).operation) ? SpecialRegistrationOperation.Add : SpecialRegistrationOperation.Keep);
					SpecialRegistrationStatus s = null;
					for (Change ch: change)
						if (ch.status != null && !ch.status.isEmpty())
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
					ca.setSubpart(clazz.getSchedulingSubpart().getItypeDesc());
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
					credit = null;
					if (ca.getParentSection() == null)
						ca.setParentSection(course.getConsentType() == null ? null : course.getConsentType().getLabel());
					
					for (Change ch: change) {
						if (ch.errors != null)
							for (ChangeError err: ch.errors) {
								if ("TIME".equals(err.code)) ret.setHasTimeConflict(true);
								if ("CLOS".equals(err.code)) ret.setHasSpaceConflict(true);
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
								if (ch.hasLastNote())
									message += "\n  <span class='note'>" + ch.getLastNote() + "</span>";
								if (ch.status != null)
									message = "<span class='" + ch.status + "'>" + message + "</span>";
								if (ca.hasError())
									ca.setError(ca.getError() + "\n" + message);
								else
									ca.setError(message);
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
						if (ch.status != null && !ch.status.isEmpty())
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
					ca.setSubpart(clazz.getSchedulingSubpart().getItypeDesc());
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
					credit = null;
					if (ca.getParentSection() == null)
						ca.setParentSection(course.getConsentType() == null ? null : course.getConsentType().getLabel());

					for (Change ch: change) {
						if (ch.errors != null)
							for (ChangeError err: ch.errors) {
								if ("TIME".equals(err.code)) ret.setHasTimeConflict(true);
								if ("CLOS".equals(err.code)) ret.setHasSpaceConflict(true);
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
								if (ch.hasLastNote())
									message += "\n  <span class='note'>" + ch.getLastNote() + "</span>";
								if (ch.status != null)
									message = "<span class='" + ch.status + "'>" + message + "</span>";
								if (ca.hasError())
									ca.setError(ca.getError() + "\n" + message);
								else
									ca.setError(message);
								ca.setPinned(true);
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
		ret.setRequestId(specialRequest.requestId);
		ret.setSubmitDate(specialRequest.dateCreated == null ? null : specialRequest.dateCreated.toDate());
		ret.setNote(specialRequest.requestorNotes);
		if (specialRequest.status != null)
			ret.setStatus(getStatus(specialRequest.status));
		else if (status != null)
			ret.setStatus(status);
		else
			ret.setStatus(SpecialRegistrationStatus.Pending);
		ret.setCanCancel(canCancel(specialRequest));
		return ret;
	}
	
	@Override
	public RetrieveSpecialRegistrationResponse retrieveRegistration(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, RetrieveSpecialRegistrationRequest input) throws SectioningException {
		if (student == null) return null;
		ClientResource resource = null;
		try {
			resource = new ClientResource(getSpecialRegistrationApiSiteRetrieveRegistration());
			resource.setNext(iClient);
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			resource.addQueryParameter("reqKey", input.getRequestKey());
			helper.getAction().addOptionBuilder().setKey("reqKey").setValue(input.getRequestKey());

			long t1 = System.currentTimeMillis();
			
			resource.get(MediaType.APPLICATION_JSON);
			
			helper.getAction().setApiGetTime(System.currentTimeMillis() - t1);
			
			SpecialRegistrationResponse response = (SpecialRegistrationResponse)new GsonRepresentation<SpecialRegistrationResponse>(resource.getResponseEntity(), SpecialRegistrationResponse.class).getObject();
			Gson gson = getGson(helper);
			if (helper.isDebugEnabled())
				helper.debug("Response: " + gson.toJson(response));
			helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
			
			if (response.data != null) {
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				String campus = getBannerCampus(session);
				if (response.data.campus != null && !campus.equals(response.data.campus))
					throw new SectioningException("Special registration request is for a different campus (" + response.data.campus + ").");
				if (response.data.term != null && !term.equals(response.data.term))
					throw new SectioningException("Special registration request is for a different term (" + response.data.term + ").");
				if (response.data.studentId != null && !getBannerId(student).equals(response.data.studentId))
					throw new SectioningException("Special registration request is for a different student.");
				return convert(server, helper, student, response.data, false);
			} else if (!ResponseStatus.success.name().equals(response.status)) {
				if (response.message != null && !response.message.isEmpty())
					throw new SectioningException(response.message);
			}
			
			RetrieveSpecialRegistrationResponse ret = new RetrieveSpecialRegistrationResponse();
			ret.setStatus(getStatus(response.status));
			ret.setDescription(response.message != null && !response.message.isEmpty() ? response.message : "New Special Registration");
			// ret.setRequestId(input.getRequestKey());
			
			return ret;
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
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
			if (getSpecialRegistrationApiSiteGetAllRegistrations() != null) {
				resource = new ClientResource(getSpecialRegistrationApiSiteGetAllRegistrations());
				resource.setNext(iClient);
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				String campus = getBannerCampus(session);
				resource.addQueryParameter("term", term);
				resource.addQueryParameter("campus", campus);
				resource.addQueryParameter("studentId", getBannerId(student));
				resource.addQueryParameter("mode", getSpecialRegistrationMode());
				helper.getAction().addOptionBuilder().setKey("term").setValue(term);
				helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
				helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				long t1 = System.currentTimeMillis();
				
				resource.get(MediaType.APPLICATION_JSON);
				
				helper.getAction().setApiGetTime(System.currentTimeMillis() - t1);
				
				SpecialRegistrationResponseList specialRequests = (SpecialRegistrationResponseList)new GsonRepresentation<SpecialRegistrationResponseList>(resource.getResponseEntity(), SpecialRegistrationResponseList.class).getObject();
				Gson gson = getGson(helper);
				if (helper.isDebugEnabled())
					helper.debug("Response: " + gson.toJson(specialRequests));
				helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(specialRequests));
				
				if ((specialRequests.data == null || specialRequests.data.isEmpty()) && !ResponseStatus.success.name().equals(specialRequests.status)) {
					throw new SectioningException(specialRequests.message == null || specialRequests.message.isEmpty() ? "Call failed but no message was given." : specialRequests.message);
				}
				
				if (specialRequests.data != null) {
					List<RetrieveSpecialRegistrationResponse> ret = new ArrayList<RetrieveSpecialRegistrationResponse>(specialRequests.data.size());
					for (SpecialRegistrationRequest specialRequest: specialRequests.data)
						if (specialRequest.requestId != null && !isCancelled(specialRequest))
							ret.add(convert(server, helper, student, specialRequest, false));
					return ret;
				}				
			} else {
				resource = new ClientResource(getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus());
				resource.setNext(iClient);
				
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				String campus = getBannerCampus(session);
				resource.addQueryParameter("term", term);
				resource.addQueryParameter("campus", campus);
				resource.addQueryParameter("studentId", getBannerId(student));
				resource.addQueryParameter("mode", getSpecialRegistrationMode());
				helper.getAction().addOptionBuilder().setKey("term").setValue(term);
				helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
				helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				long t1 = System.currentTimeMillis();
				
				resource.get(MediaType.APPLICATION_JSON);
				
				helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
				
				SpecialRegistrationStatusResponse response = (SpecialRegistrationStatusResponse)new GsonRepresentation<SpecialRegistrationStatusResponse>(resource.getResponseEntity(), SpecialRegistrationStatusResponse.class).getObject();
				Gson gson = getGson(helper);
				
				if (helper.isDebugEnabled())
					helper.debug("Response: " + gson.toJson(response));
				helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
				
				if (response != null && ResponseStatus.success.name().equals(response.status) && response.data != null && response.data.requests != null) {
					List<RetrieveSpecialRegistrationResponse> ret = new ArrayList<RetrieveSpecialRegistrationResponse>(response.data.requests.size());
					for (SpecialRegistrationRequest specialRequest: response.data.requests)
						if (specialRequest.requestId != null && !isCancelled(specialRequest))
							ret.add(convert(server, helper, student, specialRequest, false));
					return ret;
				}
			}
			
			return new ArrayList<RetrieveSpecialRegistrationResponse>();
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage());
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
			resource.addQueryParameter("mode", getSpecialRegistrationMode());
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
			
			if (response != null && ResponseStatus.success.name().equals(response.status)) {
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
				}
				student.setMaxCredit(response.maxCredit);
				server.update(student, false);
			}
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage());
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
				ret.setSuccess(ResponseStatus.success.name().equals(response.status));
				ret.setMessage(response.message);
			}
			return ret;
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage());
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
