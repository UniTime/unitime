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
import java.util.Collections;
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
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.heuristics.selection.BranchBoundSelection.BranchBoundNeighbour;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.online.OnlineReservation;
import org.cpsolver.studentsct.online.OnlineSectioningModel;
import org.cpsolver.studentsct.online.selection.MultiCriteriaBranchAndBoundSelection;
import org.cpsolver.studentsct.online.selection.OnlineSectioningSelection;
import org.cpsolver.studentsct.online.selection.SuggestionSelection;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CourseMessage;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CourseRequestsValidationProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Change;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ChangeError;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CourseCredit;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Problem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.RequestStatus;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ResponseStatus;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Schedule;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationResponseList;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationStatusResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ValidationCheckRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ValidationCheckResponse;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XDistributionType;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservationType;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;

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
public class PurdueCourseRequestsValidationProvider implements CourseRequestsValidationProvider {
	private static Logger sLog = Logger.getLogger(PurdueCourseRequestsValidationProvider.class);
	protected static final StudentSectioningMessages MESSAGES = Localization.create(StudentSectioningMessages.class);
	protected static Format<Number> sCreditFormat = Formats.getNumberFormat("0.##");
	
	private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	
	public PurdueCourseRequestsValidationProvider() {
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
	}
	
	protected String getSpecialRegistrationApiSite() {
		return ApplicationProperties.getProperty("purdue.specreg.site");
	}
	
	protected String getSpecialRegistrationApiValidationSite() {
		return ApplicationProperties.getProperty("purdue.specreg.site.validation", getSpecialRegistrationApiSite() + "/checkRestrictionsForSTAR");
	}
	
	protected String getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus() {
		return ApplicationProperties.getProperty("purdue.specreg.site.checkSpecialRegistrationStatus", getSpecialRegistrationApiSite() + "/checkSpecialRegistrationStatus");
	}
	
	protected String getSpecialRegistrationApiSiteSubmitRegistration() {
		return ApplicationProperties.getProperty("purdue.specreg.site.submitRegistration", getSpecialRegistrationApiSite() + "/submitRegistration");
	}
	
	protected String getSpecialRegistrationApiKey() {
		return ApplicationProperties.getProperty("purdue.specreg.apiKey");
	}
	
	protected String getSpecialRegistrationApiMode() {
		return ApplicationProperties.getProperty("purdue.specreg.mode.validation", "STAR");
	}
	
	protected String getBannerId(XStudent student) {
		String id = student.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	protected String getBannerId(org.unitime.timetable.model.Student student) {
		String id = student.getExternalUniqueId();
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
	
	protected String getBannerTerm(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalTerm(session);
	}
	
	protected String getBannerCampus(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalCampus(session);
	}
	
	protected String getExternalSubject(AcademicSessionInfo session, String subjectArea, String courseNumber) {
		return iExternalTermProvider.getExternalSubject(session, subjectArea, courseNumber);
	}
	
	protected String getExternalCourseNumber(AcademicSessionInfo session, String subjectArea, String courseNumber) {
		return iExternalTermProvider.getExternalCourseNumber(session, subjectArea, courseNumber);
	}
	
	protected String getCRN(Section section, Course course) {
		String name = section.getName(course.getId());
		if (name != null && name.indexOf('-') >= 0)
			return name.substring(0, name.indexOf('-'));
		return name;
	}
	
	protected Enrollment firstEnrollment(CourseRequest request, Assignment<Request, Enrollment> assignment, Course course, Config config, HashSet<Section> sections, int idx) {
        if (config.getSubparts().size() == idx) {
        	return new Enrollment(request, request.getCourses().indexOf(course), null, config, new HashSet<SctAssignment>(sections), null);
        } else {
            Subpart subpart = config.getSubparts().get(idx);
            List<Section> sectionsThisSubpart = subpart.getSections();
            List<Section> matchingSectionsThisSubpart = new ArrayList<Section>(subpart.getSections().size());
            for (Section section : sectionsThisSubpart) {
                if (section.isCancelled())
                    continue;
                if (section.getParent() != null && !sections.contains(section.getParent()))
                    continue;
                if (section.isOverlapping(sections))
                    continue;
                matchingSectionsThisSubpart.add(section);
            }
            for (Section section: matchingSectionsThisSubpart) {
                sections.add(section);
                Enrollment e = firstEnrollment(request, assignment, course, config, sections, idx + 1);
                if (e != null) return e;
                sections.remove(section);
            }
        }
        return null;
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
	public void validate(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request, CheckCoursesResponse response) throws SectioningException {
		XStudent original = (request.getStudentId() == null ? null : server.getStudent(request.getStudentId()));
		if (original == null) throw new SectioningException(MESSAGES.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
		
		Integer CONF_NONE = null;
		Integer CONF_UNITIME = new Integer(0);
		Integer CONF_BANNER = new Integer(1);
		
		ClientResource resource = null;
		Map<String, Set<String>> overrides = new HashMap<String, Set<String>>();
		Map<String, Set<String>> deniedOverrides = new HashMap<String, Set<String>>();
		Float maxCredit = null, maxCreditOverride = null;
		try {
			resource = new ClientResource(getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus());
			resource.setNext(iClient);
			
			AcademicSessionInfo session = server.getAcademicSession();
			String term = getBannerTerm(session);
			String campus = getBannerCampus(session);
			resource.addQueryParameter("term", term);
			resource.addQueryParameter("campus", campus);
			resource.addQueryParameter("studentId", getBannerId(original));
			resource.addQueryParameter("mode", getSpecialRegistrationApiMode());
			helper.getAction().addOptionBuilder().setKey("term").setValue(term);
			helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(original));
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			long t1 = System.currentTimeMillis();
			
			resource.get(MediaType.APPLICATION_JSON);
			
			helper.getAction().setApiGetTime(System.currentTimeMillis() - t1);
			
			SpecialRegistrationStatusResponse status = (SpecialRegistrationStatusResponse)new GsonRepresentation<SpecialRegistrationStatusResponse>(resource.getResponseEntity(), SpecialRegistrationStatusResponse.class).getObject();
			Gson gson = getGson(helper);
			
			if (helper.isDebugEnabled())
				helper.debug("Status: " + gson.toJson(status));
			helper.getAction().addOptionBuilder().setKey("status_response").setValue(gson.toJson(status));
			
			if (status != null && status.data != null)
				maxCredit = status.data.maxCredit;
			if (maxCredit == null) maxCredit = Float.parseFloat(ApplicationProperties.getProperty("purdue.specreg.maxCreditDefault", "18"));
			if (status != null && status.data != null && status.data.requests != null) {
				for (SpecialRegistrationRequest r: status.data.requests) {
					if (RequestStatus.denied.name().equals(r.status)) {
						for (Change ch: r.changes) {
							String course = ch.subject + " " + ch.courseNbr;
							Set<String> problems = deniedOverrides.get(course);
							if (problems == null) {
								problems = new TreeSet<String>();
								deniedOverrides.put(course, problems);
							}
							if (ch.errors != null)
								for (ChangeError err: ch.errors) {
									if (err.code != null)
										problems.add(err.code);
								}
						}
						continue;
					}
					if (RequestStatus.cancelled.name().equals(r.status)) continue;
					if (r.changes != null)
						for (Change ch: r.changes) {
							String course = ch.subject + " " + ch.courseNbr;
							Set<String> problems = overrides.get(course);
							if (problems == null) {
								problems = new TreeSet<String>();
								overrides.put(course, problems);
							}
							// Set<String> dp = deniedOverrides.get(course);
							if (ch.errors != null)
								for (ChangeError err: ch.errors) {
									if (err.code != null) {
										problems.add(err.code);
										// if (dp != null) dp.remove(err.code);
									}
								}
						}
					if (r.maxCredit != null && (maxCreditOverride == null || maxCreditOverride < r.maxCredit))
						maxCreditOverride = r.maxCredit;
				}
			}
			
			String maxCreditLimitStr = ApplicationProperties.getProperty("purdue.specreg.maxCreditCheck");
			boolean creditError = false;
			if (maxCreditLimitStr != null) {
				float maxCreditLimit = Float.parseFloat(maxCreditLimitStr);
				if (maxCredit != null && maxCredit > maxCreditLimit) maxCreditLimit = maxCredit;
				if (request.getCredit() > maxCreditLimit) {
					for (RequestedCourse rc: getOverCreditRequests(request, maxCreditLimit))
						response.addError(rc.getCourseId(), rc.getCourseName(), "CREDIT",
								ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCreditLimit)).replace("{credit}", sCreditFormat.format(request.getCredit()))
								);
					creditError = true;
				}
			}
			
			if (!creditError && maxCredit < request.getCredit()) {
				for (RequestedCourse rc: getOverCreditRequests(request, maxCredit))
					response.addMessage(rc.getCourseId(), rc.getCourseName(), "CREDIT",
							ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit())),
							maxCreditOverride == null || maxCreditOverride < request.getCredit() ? CONF_BANNER : CONF_NONE);
			}
			
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage() == null ? "Null" : e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
		
		OnlineSectioningModel model = new OnlineSectioningModel(server.getConfig(), server.getOverExpectedCriterion());
		boolean linkedClassesMustBeUsed = server.getConfig().getPropertyBoolean("LinkedClasses.mustBeUsed", false);
		Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
		
		Student student = new Student(request.getStudentId());
		Map<Long, Section> classTable = new HashMap<Long, Section>();
		Set<XDistribution> distributions = new HashSet<XDistribution>();
		for (CourseRequestInterface.Request c: request.getCourses())
			FindAssignmentAction.addRequest(server, model, assignment, student, original, c, false, false, classTable, distributions, false);
		if (student.getRequests().isEmpty()) return;
		for (CourseRequestInterface.Request c: request.getAlternatives())
			FindAssignmentAction.addRequest(server, model, assignment, student, original, c, true, false, classTable, distributions, false);
		model.addStudent(student);
		model.setDistanceConflict(new DistanceConflict(server.getDistanceMetric(), model.getProperties()));
		model.setTimeOverlaps(new TimeOverlapsCounter(null, model.getProperties()));
		for (XDistribution link: distributions) {
			if (link.getDistributionType() == XDistributionType.LinkedSections) {
				List<Section> sections = new ArrayList<Section>();
				for (Long sectionId: link.getSectionIds()) {
					Section x = classTable.get(sectionId);
					if (x != null) sections.add(x);
				}
				if (sections.size() >= 2)
					model.addLinkedSections(linkedClassesMustBeUsed, sections);
			}
		}
		for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
			Request r = (Request)e.next();
			if (r instanceof CourseRequest) {
				CourseRequest cr = (CourseRequest)r;
				for (Course course: cr.getCourses()) {
					new OnlineReservation(XReservationType.Dummy.ordinal(), -3l, course.getOffering(), -100, true, 1, true, true, true, true) {
						@Override
						public boolean mustBeUsed() { return true; }
					};
					continue;
				}
			}
		}
		
		// Single section time conflict check
		boolean questionTimeConflict = false;
		Map<Section, Course> singleSections = new HashMap<Section, Course>();
		for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
			Request r = (Request)e.next();
			if (r.isAlternative()) continue; // no alternate course requests
			if (r instanceof CourseRequest) {
				CourseRequest cr = (CourseRequest)r;
				for (Course course: cr.getCourses()) {
					if (course.getOffering().getConfigs().size() == 1) { // take only single config courses
						for (Subpart subpart: course.getOffering().getConfigs().get(0).getSubparts()) {
							if (subpart.getSections().size() == 1) { // take only single section subparts
								Section section = subpart.getSections().get(0);
								for (Section other: singleSections.keySet()) {
									if (section.isOverlapping(other)) {
										boolean confirm = (original.getRequestForCourse(course.getId()) == null || original.getRequestForCourse(singleSections.get(other).getId()) == null) && (cr.getCourses().size() == 1);
										response.addMessage(course.getId(), course.getName(), "OVERLAP",
												ApplicationProperties.getProperty("purdue.specreg.messages.courseOverlaps", "Conflists with {other}.").replace("{course}", course.getName()).replace("{other}", singleSections.get(other).getName()),
												confirm ? CONF_UNITIME : CONF_NONE);
										if (confirm) questionTimeConflict = true;
									}
								}
								if (cr.getCourses().size() == 1) {
									// remember section when there are no alternative courses provided
									singleSections.put(section, course);
								}
							}
						}
					}
				}
			}
		}
		
		OnlineSectioningSelection selection = null;
		
		if (server.getConfig().getPropertyBoolean("StudentWeights.MultiCriteria", true)) {
			selection = new MultiCriteriaBranchAndBoundSelection(server.getConfig());
		} else {
			selection = new SuggestionSelection(server.getConfig());
		}
		
		selection.setModel(model);
		selection.setPreferredSections(new Hashtable<CourseRequest, Set<Section>>());
		selection.setRequiredSections(new Hashtable<CourseRequest, Set<Section>>());
		selection.setRequiredFreeTimes(new HashSet<FreeTimeRequest>());
		selection.setRequiredUnassinged(new HashSet<CourseRequest>());
		
		BranchBoundNeighbour neighbour = selection.select(assignment, student);
		
		neighbour.assign(assignment, 0);
		
		ValidationCheckRequest req = new ValidationCheckRequest();
		req.studentId = getBannerId(original);
		req.term = getBannerTerm(server.getAcademicSession());
		req.campus = getBannerCampus(server.getAcademicSession());
		req.schedule = new ArrayList<Schedule>();
		req.alternatives = new ArrayList<Schedule>();
		req.mode = getSpecialRegistrationApiMode();
		req.includeReg = "N";
		
		Map<String, XCourseId> crn2course = new HashMap<String, XCourseId>();
		Map<XCourseId, String> course2banner = new HashMap<XCourseId, String>();
		for (Request r: model.variables()) {
			if (r instanceof CourseRequest) {
				CourseRequest cr = (CourseRequest)r;
				Enrollment e = assignment.getValue(cr);
				courses: for (Course course: cr.getCourses()) {
					Schedule s = new Schedule();
					s.subject = getExternalSubject(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
					s.courseNbr = getExternalCourseNumber(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
					s.crns = new TreeSet<String>();
					XCourseId cid = new XCourseId(course);
					course2banner.put(cid, s.subject + " " + s.courseNbr);
					
					// 1. is enrolled 
					if (e != null && course.equals(e.getCourse())) {
						for (Section section: e.getSections()) {
							String crn = getCRN(section, course);
							crn2course.put(crn, cid);
							s.crns.add(crn);
						}
						req.schedule.add(s);
						continue courses;
					}
					
					// 2. has value
					for (Enrollment x: cr.values(assignment)) {
						if (course.equals(x.getCourse())) {
							for (Section section: x.getSections()) {
								String crn = getCRN(section, course);
								crn2course.put(crn, cid);
								s.crns.add(crn);
							}
							req.alternatives.add(s);
							continue courses;
						}
					}
					
					// 3. makup a value
					for (Config config: course.getOffering().getConfigs()) {
						Enrollment x = firstEnrollment(cr, assignment, course, config, new HashSet<Section>(), 0);
						if (x != null) {
							for (Section section: x.getSections()) {
								String crn = getCRN(section, course);
								crn2course.put(crn, cid);
								s.crns.add(crn);
							}
							req.alternatives.add(s);
							continue courses;
						}
					}
				}
			}
		}
		
		ValidationCheckResponse resp = null;
		resource = null;
		try {
			resource = new ClientResource(getSpecialRegistrationApiValidationSite());
			resource.setNext(iClient);
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			Gson gson = getGson(helper);
			if (helper.isDebugEnabled())
				helper.debug("Request: " + gson.toJson(req));
			helper.getAction().addOptionBuilder().setKey("validation_request").setValue(gson.toJson(req));
			long t1 = System.currentTimeMillis();
			
			resource.post(new GsonRepresentation<ValidationCheckRequest>(req));
			
			helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
			
			resp = (ValidationCheckResponse)new GsonRepresentation<ValidationCheckResponse>(resource.getResponseEntity(), ValidationCheckResponse.class).getObject();
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
		
		if (resp == null) return;
		
		if (resp.scheduleRestrictions != null && resp.scheduleRestrictions.problems != null)
			for (Problem problem: resp.scheduleRestrictions.problems) {
				if ("HOLD".equals(problem.code)) {
					response.addError(null, null, problem.code, problem.message);
					//throw new SectioningException(problem.message);
				}
				XCourseId course = crn2course.get(problem.crn);
				if (course == null) continue;
				String bc = course2banner.get(course);
				if ("DUPL".equals(problem.code)) continue;
				if ("MAXI".equals(problem.code)) continue;
				Set<String> problems = (bc == null ? null : overrides.get(bc));
				Set<String> denied = (bc == null ? null : deniedOverrides.get(bc));
				if (denied != null && denied.contains(problem.code))
					response.addError(course.getCourseId(), course.getCourseName(), problem.code, "Denied " + problem.message);
				else {
					response.addMessage(course.getCourseId(), course.getCourseName(), problem.code, problem.message, problems == null || !problems.contains(problem.code) ? CONF_BANNER : CONF_NONE);
				}
			}
		if (resp.alternativesRestrictions != null && resp.alternativesRestrictions.problems != null)
			for (Problem problem: resp.alternativesRestrictions.problems) {
				if ("HOLD".equals(problem.code)) {
					response.addError(null, null, problem.code, problem.message);
					// throw new SectioningException(problem.message);
				}
				XCourseId course = crn2course.get(problem.crn);
				if (course == null) continue;
				String bc = course2banner.get(course);
				Set<String> problems = (bc == null ? null : overrides.get(bc));
				Set<String> denied = (bc == null ? null : deniedOverrides.get(bc));
				if (denied != null && denied.contains(problem.code)) {
					response.addError(course.getCourseId(), course.getCourseName(), problem.code, "Denied " + problem.message);
				} else {
					response.addMessage(course.getCourseId(), course.getCourseName(), problem.code, problem.message, problems == null || !problems.contains(problem.code) ? CONF_BANNER : CONF_NONE);
				}
			}
		
		if (response.hasMessages())
			for (CourseMessage m: response.getMessages()) {
				if (m.getCourse() != null && m.getMessage().indexOf("this section") >= 0)
					m.setMessage(m.getMessage().replace("this section", m.getCourse()));
				if (m.getCourse() != null && m.getMessage().indexOf(" (CRN ") >= 0)
					m.setMessage(m.getMessage().replaceFirst(" \\(CRN [0-9][0-9][0-9][0-9][0-9]\\) ", " "));
			}
		
		boolean questionMinCred = false;
		String minCreditLimit = ApplicationProperties.getProperty("purdue.specreg.minCreditCheck");
		float minCredit = 0;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse())
					if (rc.hasCredit()) {
						minCredit += rc.getCreditMin(); break;
					}
			}
		}
		if (minCreditLimit != null && minCredit < Float.parseFloat(minCreditLimit)) {
			questionMinCred = true;
		}
		
		Set<Long> coursesWithNotAlt = new HashSet<Long>();
		for (XRequest r: original.getRequests()) {
			if (r instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)r;
				if (cr.getCourseIds().size() == 1 && !cr.isAlternative()) coursesWithNotAlt.add(cr.getCourseIds().get(0).getCourseId());
			}
		}
		boolean questionNoAlt = false;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() == 1) {
				RequestedCourse rc = r.getRequestedCourse(0);
				if (rc.getCourseId() != null && !rc.isReadOnly()) {
					response.addMessage(rc.getCourseId(), rc.getCourseName(), "NO_ALT",
							ApplicationProperties.getProperty("purdue.specreg.messages.courseHasNoAlt", "No alternative course provided.").replace("{course}", rc.getCourseName()),
							!coursesWithNotAlt.contains(rc.getCourseId()) ? CONF_UNITIME : CONF_NONE);
					if (!coursesWithNotAlt.contains(rc.getCourseId())) {
						questionNoAlt = true;
					}
				}
			}
		}
		if (response.getConfirms().contains(CONF_BANNER)) {
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.bannerProblemsFound", "The following issues have been detected:"), CONF_BANNER, -1);
			response.addConfirmation("", CONF_BANNER, 1);
			response.addConfirmation(
					ApplicationProperties.getProperty("purdue.specreg.messages.requestOverrides", "It is possible to request overrides for the above problems."),
					CONF_BANNER, 2);
			response.addConfirmation(
					ApplicationProperties.getProperty("purdue.specreg.messages.requestOverridesTail", "Do you want to request overrides?"),
					CONF_BANNER, 3);
		}
		if (response.getConfirms().contains(CONF_UNITIME)) {
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.unitimeProblemsFound", "The following issues have been detected:"), CONF_UNITIME, -1);
			response.addConfirmation("", CONF_UNITIME, 1);
		}
		if (questionMinCred)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.minCredit",
					"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit)),
					CONF_UNITIME, 2);
		if (questionNoAlt)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.noAlternatives", (questionMinCred ? "\n" : "") +
					"One or more of the newly requested courses have no alternatives provided.\nYou may not be able to get a full schedule because you did not provide an alternative course."),
					CONF_UNITIME, 3);
		if (questionTimeConflict)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.timeConflicts", (questionMinCred || questionNoAlt ? "\n" : "") +
					"Two or more single section courses are conflicting with each other.\nYou will likely not be able to get the conflicting course, so please provide an alternative course if possible."),
					CONF_UNITIME, 4);
		if (questionNoAlt || questionMinCred || questionTimeConflict)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.confirmation", "\nDo you want to proceed?"), CONF_UNITIME, 5);
	}

	@Override
	public void dispose() {
		try {
			iClient.stop();
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
		}	
	}

	@Override
	public void submit(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request) throws SectioningException {
		XStudent original = (request.getStudentId() == null ? null : server.getStudent(request.getStudentId()));
		if (original == null) return;

		ClientResource resource = null;
		Map<String, Set<String>> overrides = new HashMap<String, Set<String>>();
		Float maxCredit = null;
		try {
			resource = new ClientResource(getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus());
			resource.setNext(iClient);
			
			AcademicSessionInfo session = server.getAcademicSession();
			String term = getBannerTerm(session);
			String campus = getBannerCampus(session);
			resource.addQueryParameter("term", term);
			resource.addQueryParameter("campus", campus);
			resource.addQueryParameter("studentId", getBannerId(original));
			resource.addQueryParameter("mode", getSpecialRegistrationApiMode());
			helper.getAction().addOptionBuilder().setKey("term").setValue(term);
			helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(original));
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			long t1 = System.currentTimeMillis();
			
			resource.get(MediaType.APPLICATION_JSON);
			
			helper.getAction().setApiGetTime(System.currentTimeMillis() - t1);
			
			SpecialRegistrationStatusResponse status = (SpecialRegistrationStatusResponse)new GsonRepresentation<SpecialRegistrationStatusResponse>(resource.getResponseEntity(), SpecialRegistrationStatusResponse.class).getObject();
			Gson gson = getGson(helper);
			
			if (helper.isDebugEnabled())
				helper.debug("Status: " + gson.toJson(status));
			helper.getAction().addOptionBuilder().setKey("status_response").setValue(gson.toJson(status));
			
			if (status != null && status.data != null) {
				maxCredit = status.data.maxCredit;
				request.setMaxCredit(status.data.maxCredit);
			}
			if (maxCredit == null) maxCredit = Float.parseFloat(ApplicationProperties.getProperty("purdue.specreg.maxCreditDefault", "18"));
			
			if (status != null && status.data != null && status.data.requests != null) {
				for (SpecialRegistrationRequest r: status.data.requests) {
					if (RequestStatus.inProgress.name().equals(r.status) && r.changes != null)
						for (Change ch: r.changes) {
							String course = ch.subject + " " + ch.courseNbr;
							Set<String> problems = overrides.get(course);
							if (problems == null) {
								problems = new TreeSet<String>();
								overrides.put(course, problems);
							}
							if (ch.errors != null)
								for (ChangeError err: ch.errors) {
									if (err.code != null)
										problems.add(err.code);
								}
						}
				}
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
		
		SpecialRegistrationRequest req = new SpecialRegistrationRequest();
		req.studentId = getBannerId(original);
		req.term = getBannerTerm(server.getAcademicSession());
		req.campus = getBannerCampus(server.getAcademicSession());
		req.mode = getSpecialRegistrationApiMode();
		req.changes = new ArrayList<Change>();
		if (helper.getUser() != null) {
			req.requestorId = getRequestorId(helper.getUser());
			req.requestorRole = getRequestorType(helper.getUser(), original);
		}

		if (request.hasConfirmations()) {
			for (CourseRequestInterface.Request c: request.getCourses())
				if (c.hasRequestedCourse()) {
					for (CourseRequestInterface.RequestedCourse rc: c.getRequestedCourse()) {
						XCourseId cid = server.getCourse(rc.getCourseId(), rc.getCourseName());
						if (cid == null) continue;
						XCourse course = (cid instanceof XCourse ? (XCourse)cid : server.getCourse(cid.getCourseId()));
						if (course == null) continue;
						String subject = iExternalTermProvider.getExternalSubject(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
						String courseNbr = iExternalTermProvider.getExternalCourseNumber(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
						overrides.remove(subject + " " + courseNbr);
						List<ChangeError> errors = new ArrayList<ChangeError>();
						for (CourseMessage m: request.getConfirmations()) {
							if ("CREDIT".equals(m.getCode())) continue;
							if ("NO_ALT".equals(m.getCode())) continue;
							if ("OVERLAP".equals(m.getCode())) continue;
							if (!m.hasCourse()) continue;
							if (!m.isError() && (course.getCourseId().equals(m.getCourseId()) || course.getCourseName().equals(m.getCourse()))) {
								ChangeError e = new ChangeError();
								e.code = m.getCode(); e.message = m.getMessage();
								errors.add(e);
							}
						}
						if (!errors.isEmpty()) {
							Change ch = new Change();
							ch.subject = subject;
							ch.courseNbr = courseNbr;
							ch.crn = "";
							ch.errors = errors;
							ch.operation = "ADD";
							req.changes.add(ch);
							
						}
					}
				}
			for (CourseRequestInterface.Request c: request.getAlternatives())
				if (c.hasRequestedCourse()) {
					for (CourseRequestInterface.RequestedCourse rc: c.getRequestedCourse()) {
						XCourseId cid = server.getCourse(rc.getCourseId(), rc.getCourseName());
						if (cid == null) continue;
						XCourse course = (cid instanceof XCourse ? (XCourse)cid : server.getCourse(cid.getCourseId()));
						if (course == null) continue;
						String subject = iExternalTermProvider.getExternalSubject(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
						String courseNbr = iExternalTermProvider.getExternalCourseNumber(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
						overrides.remove(subject + " " + courseNbr);
						List<ChangeError> errors = new ArrayList<ChangeError>();
						for (CourseMessage m: request.getConfirmations()) {
							if ("CREDIT".equals(m.getCode())) continue;
							if ("NO_ALT".equals(m.getCode())) continue;
							if ("OVERLAP".equals(m.getCode())) continue;
							if (!m.hasCourse()) continue;
							if (!m.isError() && (course.getCourseId().equals(m.getCourseId()) || course.getCourseName().equals(m.getCourse()))) {
								ChangeError e = new ChangeError();
								e.code = m.getCode(); e.message = m.getMessage();
								errors.add(e);
							}
						}
						if (!errors.isEmpty()) {
							Change ch = new Change();
							ch.subject = subject;
							ch.courseNbr = courseNbr;
							ch.crn = "";
							ch.errors = errors;
							ch.operation = "ADD";
							req.changes.add(ch);
						}
					}
				}
		}
		if (maxCredit < request.getCredit()) {
			req.maxCredit = request.getCredit();
			req.courseCreditHrs = new ArrayList<CourseCredit>();
			req.alternateCourseCreditHrs = new ArrayList<CourseCredit>();
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse()) {
					CourseCredit cc = null;
					for (RequestedCourse rc: r.getRequestedCourse()) {
						XCourseId cid = server.getCourse(rc.getCourseId(), rc.getCourseName());
						if (cid == null) continue;
						XCourse course = (cid instanceof XCourse ? (XCourse)cid : server.getCourse(cid.getCourseId()));
						if (course == null) continue;
						if (cc == null) {
							cc = new CourseCredit();
							cc.subject = course.getSubjectArea();
							cc.courseNbr = course.getCourseNumber();
							cc.title = course.getTitle();
							cc.creditHrs = (course.hasCredit() ? course.getMinCredit() : 0f);
						} else {
							if (cc.alternatives == null) cc.alternatives = new ArrayList<CourseCredit>();
							CourseCredit acc = new CourseCredit();
							acc.subject = course.getSubjectArea();
							acc.courseNbr = course.getCourseNumber();
							acc.title = course.getTitle();
							acc.creditHrs = (course.hasCredit() ? course.getMinCredit() : 0f);
							cc.alternatives.add(acc);
						}
					}
					if (cc != null) req.courseCreditHrs.add(cc);
				}
			}
			for (CourseRequestInterface.Request r: request.getAlternatives()) {
				if (r.hasRequestedCourse()) {
					CourseCredit cc = null;
					for (RequestedCourse rc: r.getRequestedCourse()) {
						XCourseId cid = server.getCourse(rc.getCourseId(), rc.getCourseName());
						if (cid == null) continue;
						XCourse course = (cid instanceof XCourse ? (XCourse)cid : server.getCourse(cid.getCourseId()));
						if (course == null) continue;
						if (cc == null) {
							cc = new CourseCredit();
							cc.subject = course.getSubjectArea();
							cc.courseNbr = course.getCourseNumber();
							cc.title = course.getTitle();
							cc.creditHrs = (course.hasCredit() ? course.getMinCredit() : 0f);
						} else {
							if (cc.alternatives == null) cc.alternatives = new ArrayList<CourseCredit>();
							CourseCredit acc = new CourseCredit();
							acc.subject = course.getSubjectArea();
							acc.courseNbr = course.getCourseNumber();
							acc.title = course.getTitle();
							acc.creditHrs = (course.hasCredit() ? course.getMinCredit() : 0f);
							cc.alternatives.add(acc);
						}
					}
					if (cc != null) req.alternateCourseCreditHrs.add(cc);
				}
			}
			/*// The following code only adds the courses that show as over the max credit into the request message
			req.courseCreditHrs = new ArrayList<CourseCredit>();
			for (RequestedCourse rc: getOverCreditRequests(request, maxCredit)) {
				XCourseId cid = server.getCourse(rc.getCourseId(), rc.getCourseName());
				if (cid == null) continue;
				XCourse course = (cid instanceof XCourse ? (XCourse)cid : server.getCourse(cid.getCourseId()));
				if (course == null) continue;
				CourseCredit cc = new CourseCredit();
				cc.subject = iExternalTermProvider.getExternalSubject(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
				cc.courseNbr = iExternalTermProvider.getExternalCourseNumber(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
				cc.creditHrs = (course.hasCredit() ? course.getMinCredit() : 0f);
				req.courseCreditHrs.add(cc);
			}
			*/
		}
		
		if (!req.changes.isEmpty() || !overrides.isEmpty() || req.maxCredit != null) {
			resource = null;
			try {
				resource = new ClientResource(getSpecialRegistrationApiSiteSubmitRegistration());
				resource.setNext(iClient);
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				Gson gson = getGson(helper);
				if (helper.isDebugEnabled())
					helper.debug("Request: " + gson.toJson(req));
				helper.getAction().addOptionBuilder().setKey("specreg_request").setValue(gson.toJson(req));
				long t1 = System.currentTimeMillis();
				
				resource.post(new GsonRepresentation<SpecialRegistrationRequest>(req));
				
				helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
				
				SpecialRegistrationResponseList response = (SpecialRegistrationResponseList)new GsonRepresentation<SpecialRegistrationResponseList>(resource.getResponseEntity(), SpecialRegistrationResponseList.class).getObject();
				if (helper.isDebugEnabled())
					helper.debug("Response: " + gson.toJson(response));
				helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
				
				if (!ResponseStatus.success.name().equals(response.status))
					throw new SectioningException(response.message == null || response.message.isEmpty() ? "Failed to request overrides (" + response.status + ")." : response.message);
				
				if (response.data != null) {
					for (CourseRequestInterface.Request c: request.getCourses())
						if (c.hasRequestedCourse()) {
							for (CourseRequestInterface.RequestedCourse rc: c.getRequestedCourse()) {
								XCourseId cid = server.getCourse(rc.getCourseId(), rc.getCourseName());
								if (cid == null) continue;
								XCourse course = (cid instanceof XCourse ? (XCourse)cid : server.getCourse(cid.getCourseId()));
								if (course == null) continue;
								String subject = iExternalTermProvider.getExternalSubject(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
								String courseNbr = iExternalTermProvider.getExternalCourseNumber(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
								for (SpecialRegistrationRequest r: response.data)
									if (r.changes != null)
										for (Change ch: r.changes) {
											if (subject.equals(ch.subject) && courseNbr.equals(ch.courseNbr)) {
												rc.setOverrideTimeStamp(r.dateCreated == null ? null : r.dateCreated.toDate());
												rc.setOverrideExternalId(r.requestId);
												rc.setStatus(RequestStatus.approved.name().equals(r.status) ? RequestedCourseStatus.OVERRIDE_APPROVED :
													RequestStatus.denied.name().equals(r.status) ?  RequestedCourseStatus.OVERRIDE_REJECTED :
													RequestStatus.cancelled.name().equals(r.status) ? RequestedCourseStatus.OVERRIDE_CANCELLED : RequestedCourseStatus.OVERRIDE_PENDING);
											}
										}
							}
						}
					for (CourseRequestInterface.Request c: request.getAlternatives())
						if (c.hasRequestedCourse()) {
							for (CourseRequestInterface.RequestedCourse rc: c.getRequestedCourse()) {
								XCourseId cid = server.getCourse(rc.getCourseId(), rc.getCourseName());
								if (cid == null) continue;
								XCourse course = (cid instanceof XCourse ? (XCourse)cid : server.getCourse(cid.getCourseId()));
								if (course == null) continue;
								String subject = iExternalTermProvider.getExternalSubject(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
								String courseNbr = iExternalTermProvider.getExternalCourseNumber(server.getAcademicSession(), course.getSubjectArea(), course.getCourseNumber());
								for (SpecialRegistrationRequest r: response.data)
									if (r.changes != null)
									for (Change ch: r.changes) {
										if (subject.equals(ch.subject) && courseNbr.equals(ch.courseNbr)) {
											rc.setOverrideTimeStamp(r.dateCreated == null ? null : r.dateCreated.toDate());
											rc.setOverrideExternalId(r.requestId);
											rc.setStatus(RequestStatus.approved.name().equals(r.status) ? RequestedCourseStatus.OVERRIDE_APPROVED :
												RequestStatus.denied.name().equals(r.status) ?  RequestedCourseStatus.OVERRIDE_REJECTED :
												RequestStatus.cancelled.name().equals(r.status) ? RequestedCourseStatus.OVERRIDE_CANCELLED : RequestedCourseStatus.OVERRIDE_PENDING);
										}
									}
							}
						}
					if (req.maxCredit != null) {
						for (SpecialRegistrationRequest r: response.data) {
							if (r.maxCredit != null) {
								request.setMaxCreditOverride(r.maxCredit);
								request.setMaxCreditOverrideExternalId(r.requestId);
								request.setMaxCreditOverrideTimeStamp(r.dateCreated == null ? null : r.dateCreated.toDate());
								request.setMaxCreditOverrideStatus(RequestStatus.approved.name().equals(r.status) ? RequestedCourseStatus.OVERRIDE_APPROVED :
									RequestStatus.denied.name().equals(r.status) ?  RequestedCourseStatus.OVERRIDE_REJECTED :
									RequestStatus.cancelled.name().equals(r.status) ? RequestedCourseStatus.OVERRIDE_CANCELLED : RequestedCourseStatus.OVERRIDE_PENDING);
								break;
							}
						}
					}
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
		
	}
	
	protected List<RequestedCourse> getOverCreditRequests(CourseRequestInterface request, float maxCredit) {
		List<RequestedCourse> ret = new ArrayList<RequestedCourse>();
		// Step 1, only check primary courses
		float primary = 0f;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse(0).hasCredit()) {
				primary += r.getRequestedCourse(0).getCreditMin();
				if (primary > maxCredit)
					ret.add(r.getRequestedCourse(0));
			}
		}
		if (!ret.isEmpty()) return ret;
		// Step 2, check alternatives
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() > 1) {
				float credit = (r.getRequestedCourse(0).hasCredit() ? r.getRequestedCourse(0).getCreditMin() : 0f);
				for (int i = 1; i < r.getRequestedCourse().size(); i++) {
					float alt = (r.getRequestedCourse(i).hasCredit() ? r.getRequestedCourse(i).getCreditMin() : 0f);
					if (primary - credit + alt > maxCredit)
						ret.add(r.getRequestedCourse(i));
				}
			}
		}
		if (!ret.isEmpty()) return ret;
		// Step 3, check alternatives
		List<Float> credits = new ArrayList<Float>();
		float total = 0f;
		RequestedCourse last = null;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse()) {
				Float credit = null;
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasCredit()) {
						if (credit == null || credit < rc.getCreditMin()) {
							credit = rc.getCreditMin();
							if (total + credit > maxCredit) ret.add(rc);
						}
					}
				}
				if (credit != null) {
					credits.add(credit); total += credit;
				}
				last = r.getRequestedCourse(0);
			}
		}
		if (!ret.isEmpty()) return ret;
		// Step 4, check alternate courses
		Collections.sort(credits);
		float low = (credits.isEmpty() ? 0f : credits.get(0));
		RequestedCourse first = null;
		for (CourseRequestInterface.Request r: request.getAlternatives()) {
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasCredit()) {
						if (total + rc.getCreditMin() - low > maxCredit) { ret.add(rc); break; }
					}
				}
				if (first == null)
					first = r.getRequestedCourse(0);
			}
		}
		if (!ret.isEmpty()) return ret;
		// Fall back: return first alternate course or the last requested course
		ret.add(first != null ? first : last);
		return ret;
	}

	@Override
	public void check(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request) throws SectioningException {
		XStudent original = (request.getStudentId() == null ? null : server.getStudent(request.getStudentId()));
		if (original == null) return;
		
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() == 1) {
				RequestedCourse rc = r.getRequestedCourse(0);
				if (rc.getCourseId() != null && !rc.isReadOnly()) {
					request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "NO_ALT",
							ApplicationProperties.getProperty("purdue.specreg.messages.courseHasNoAlt", "No alternative course provided.").replace("{course}", rc.getCourseName()));
				}
			}
		}
		
		if (server instanceof DatabaseServer) {
			Map<Class_, XCourseId> singleSections = new HashMap<Class_, XCourseId>();
			for (XRequest r: original.getRequests()) {
				if (r.isAlternative()) continue; // no alternate course requests
				if (r instanceof XCourseRequest) {
					XCourseRequest cr = (XCourseRequest)r;
					for (XCourseId course: cr.getCourseIds()) {
						InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(course.getOfferingId(), helper.getHibSession());
						if (offering != null && offering.getInstrOfferingConfigs().size() == 1) { // take only single config courses
							for (SchedulingSubpart subpart: offering.getInstrOfferingConfigs().iterator().next().getSchedulingSubparts()) {
								if (subpart.getClasses().size() == 1) { // take only single section subparts
									Class_ clazz = subpart.getClasses().iterator().next();
									if (clazz.getCommittedAssignment() != null) {
										TimeLocation time = clazz.getCommittedAssignment().getTimeLocation();
										for (Class_ other: singleSections.keySet()) {
											if (other.getCommittedAssignment().getTimeLocation().hasIntersection(time) && !clazz.isToIgnoreStudentConflictsWith(other)){
												request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "OVERLAP",
														ApplicationProperties.getProperty("purdue.specreg.messages.courseOverlaps", "Conflists with {other}.").replace("{course}", course.getCourseName()).replace("{other}", singleSections.get(other).getCourseName()));
											}
										}
										if (cr.getCourseIds().size() == 1) {
											// remember section when there are no alternative courses provided
											singleSections.put(clazz, course);
										}
									}
								}
							}
						}
					}
				}
			}
		} else {
			Map<XSection, XCourseId> singleSections = new HashMap<XSection, XCourseId>();
			for (XRequest r: original.getRequests()) {
				if (r.isAlternative()) continue; // no alternate course requests
				if (r instanceof XCourseRequest) {
					XCourseRequest cr = (XCourseRequest)r;
					for (XCourseId course: cr.getCourseIds()) {
						XOffering offering = server.getOffering(course.getOfferingId());
						if (offering != null && offering.getConfigs().size() == 1) { // take only single config courses
							for (XSubpart subpart: offering.getConfigs().get(0).getSubparts()) {
								if (subpart.getSections().size() == 1) { // take only single section subparts
									XSection section = subpart.getSections().get(0);
									for (XSection other: singleSections.keySet()) {
										if (section.isOverlapping(offering.getDistributions(), other)) {
											request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "OVERLAP",
													ApplicationProperties.getProperty("purdue.specreg.messages.courseOverlaps", "Conflists with {other}.").replace("{course}", course.getCourseName()).replace("{other}", singleSections.get(other).getCourseName()));
										}
									}
									if (cr.getCourseIds().size() == 1) {
										// remember section when there are no alternative courses provided
										singleSections.put(section, course);
									}
								}
							}
						}
					}
				}
			}
		}
		
		Map<String, RequestedCourse> rcs = new HashMap<String, RequestedCourse>();
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse())
					if (rc.getOverrideExternalId() != null)
						rcs.put(rc.getOverrideExternalId(), rc);
		}
		for (CourseRequestInterface.Request r: request.getAlternatives()) {
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse())
					if (rc.getOverrideExternalId() != null)
						rcs.put(rc.getOverrideExternalId(), rc);
		}
		if (rcs.isEmpty() && !request.hasMaxCreditOverride()) return;
		
		ClientResource resource = null;
		try {
			resource = new ClientResource(getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus());
			resource.setNext(iClient);
			
			AcademicSessionInfo session = server.getAcademicSession();
			String term = getBannerTerm(session);
			String campus = getBannerCampus(session);
			resource.addQueryParameter("term", term);
			resource.addQueryParameter("campus", campus);
			resource.addQueryParameter("studentId", getBannerId(original));
			resource.addQueryParameter("mode", getSpecialRegistrationApiMode());
			helper.getAction().addOptionBuilder().setKey("term").setValue(term);
			helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(original));
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			long t0 = System.currentTimeMillis();
			
			resource.get(MediaType.APPLICATION_JSON);
			
			helper.getAction().setApiGetTime(System.currentTimeMillis() - t0);
			
			SpecialRegistrationStatusResponse status = (SpecialRegistrationStatusResponse)new GsonRepresentation<SpecialRegistrationStatusResponse>(resource.getResponseEntity(), SpecialRegistrationStatusResponse.class).getObject();
			Gson gson = getGson(helper);
			
			if (helper.isDebugEnabled())
				helper.debug("Status: " + gson.toJson(status));
			helper.getAction().addOptionBuilder().setKey("status_response").setValue(gson.toJson(status));
			
			Float maxCredit = null;
			if (status != null && status.data != null) {
				maxCredit = status.data.maxCredit;
				request.setMaxCredit(status.data.maxCredit);
			}
			if (maxCredit == null) maxCredit = Float.parseFloat(ApplicationProperties.getProperty("purdue.specreg.maxCreditDefault", "18"));
			
			if (status != null && status.data != null && status.data.requests != null) {
				for (SpecialRegistrationRequest r: status.data.requests) {
					if (r.requestId == null) continue;
					if (r.requestId.equals(request.getMaxCreditOverrideExternalId())) {
						if (RequestStatus.denied.name().equals(r.status))
							request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_REJECTED);
						else if (RequestStatus.approved.name().equals(r.status))
							request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_APPROVED);
						else if (RequestStatus.cancelled.name().equals(r.status))
							request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_CANCELLED);
						else
							request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_PENDING);
						if (r.maxCredit != null)
							request.setMaxCreditOverride(r.maxCredit);
					}
					RequestedCourse rc = rcs.get(r.requestId);
					if (rc == null) continue;
					if (rc.getStatus() != RequestedCourseStatus.ENROLLED) {
						if (RequestStatus.denied.name().equals(r.status)) 
							rc.setStatus(RequestedCourseStatus.OVERRIDE_REJECTED);
						else if (RequestStatus.approved.name().equals(r.status))
							rc.setStatus(RequestedCourseStatus.OVERRIDE_APPROVED);
						else if (RequestStatus.cancelled.name().equals(r.status))
							rc.setStatus(RequestedCourseStatus.OVERRIDE_CANCELLED);
						else
							rc.setStatus(RequestedCourseStatus.OVERRIDE_PENDING);
					}
					if (r.changes != null && !RequestStatus.approved.name().equals(r.status))
						for (Change ch: r.changes)
							if (ch.errors != null)
								for (ChangeError er: ch.errors) {
									if (RequestStatus.denied.name().equals(r.status))
										request.addConfirmationError(rc.getCourseId(), rc.getCourseName(), er.code, (RequestStatus.denied.name().equals(r.status) ? "Denied " : "") + er.message);
									else
										request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), er.code, (RequestStatus.denied.name().equals(r.status) ? "Denied " : "") + er.message);
								}
				}
			}
			
			if (maxCredit < request.getCredit()) {
				for (RequestedCourse rc: getOverCreditRequests(request, maxCredit))
					request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "CREDIT",
							ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit())));
			}
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage() == null ? "Null" : e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}
	
	public boolean updateStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, org.unitime.timetable.model.Student student, OnlineSectioningLog.Action.Builder action) throws SectioningException {
		ClientResource resource = null;
		try {
			resource = new ClientResource(getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus());
			resource.setNext(iClient);
			
			AcademicSessionInfo session = (server == null ? new AcademicSessionInfo(student.getSession()) : server.getAcademicSession());
			String term = getBannerTerm(session);
			String campus = getBannerCampus(session);
			resource.addQueryParameter("term", term);
			resource.addQueryParameter("campus", campus);
			resource.addQueryParameter("studentId", getBannerId(student));
			resource.addQueryParameter("mode", getSpecialRegistrationApiMode());
			action.addOptionBuilder().setKey("term").setValue(term);
			action.addOptionBuilder().setKey("campus").setValue(campus);
			action.addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			long t0 = System.currentTimeMillis();
			
			resource.get(MediaType.APPLICATION_JSON);
			
			action.setApiGetTime(System.currentTimeMillis() - t0);
			
			SpecialRegistrationStatusResponse status = (SpecialRegistrationStatusResponse)new GsonRepresentation<SpecialRegistrationStatusResponse>(resource.getResponseEntity(), SpecialRegistrationStatusResponse.class).getObject();
			Gson gson = getGson(helper);
			
			if (helper.isDebugEnabled())
				helper.debug("Status: " + gson.toJson(status));
			action.addOptionBuilder().setKey("status_response").setValue(gson.toJson(status));
			
			boolean changed = false;
			for (CourseDemand cd: student.getCourseDemands()) {
				for (org.unitime.timetable.model.CourseRequest cr: cd.getCourseRequests()) {
					if (cr.getOverrideExternalId() != null) {
						SpecialRegistrationRequest req = null;
						for (SpecialRegistrationRequest r: status.data.requests) {
							if (cr.getOverrideExternalId().equals(r.requestId)) { req = r; break; }
						}
						if (req == null) {
							if (cr.getCourseRequestOverrideStatus() != CourseRequestOverrideStatus.CANCELLED) {
								cr.setCourseRequestOverrideStatus(CourseRequestOverrideStatus.CANCELLED);
								helper.getHibSession().update(cr);
								changed = true;
							}
						} else {
							Integer oldStatus = cr.getOverrideStatus();
							if (RequestStatus.denied.name().equals(req.status))
								cr.setCourseRequestOverrideStatus(CourseRequestOverrideStatus.REJECTED);
							else if (RequestStatus.approved.name().equals(req.status))
								cr.setCourseRequestOverrideStatus(CourseRequestOverrideStatus.APPROVED);
							else if (RequestStatus.cancelled.name().equals(req.status))
								cr.setCourseRequestOverrideStatus(CourseRequestOverrideStatus.CANCELLED);
							else
								cr.setCourseRequestOverrideStatus(CourseRequestOverrideStatus.PENDING);
							if (oldStatus == null || !oldStatus.equals(cr.getOverrideStatus())) {
								helper.getHibSession().update(cr);
								changed = true;
							}
						}
					} else {
						String subject = iExternalTermProvider.getExternalSubject(session, cr.getCourseOffering().getSubjectAreaAbbv(), cr.getCourseOffering().getCourseNbr());
						String courseNbr = iExternalTermProvider.getExternalCourseNumber(session, cr.getCourseOffering().getSubjectAreaAbbv(), cr.getCourseOffering().getCourseNbr());
						SpecialRegistrationRequest req = null;
						for (SpecialRegistrationRequest r: status.data.requests) {
							if (r.requestId == null) continue;
							Change match = null;
							if (r.changes != null)
								for (Change ch: r.changes)
									if (subject.equals(ch.subject) && courseNbr.equals(ch.courseNbr)) { match = ch; break; }
							if (match != null && (req == null || r.dateCreated.isAfter(req.dateCreated))) req = r;
						}
						if (req != null) {
							cr.setOverrideExternalId(req.requestId);
							cr.setOverrideTimeStamp(req.dateCreated.toDate());
							if (RequestStatus.denied.name().equals(req.status))
								cr.setCourseRequestOverrideStatus(CourseRequestOverrideStatus.REJECTED);
							else if (RequestStatus.approved.name().equals(req.status))
								cr.setCourseRequestOverrideStatus(CourseRequestOverrideStatus.APPROVED);
							else if (RequestStatus.cancelled.name().equals(req.status))
								cr.setCourseRequestOverrideStatus(CourseRequestOverrideStatus.CANCELLED);
							else
								cr.setCourseRequestOverrideStatus(CourseRequestOverrideStatus.PENDING);
							helper.getHibSession().update(cr);
							changed = true;
						}
					}
				}
			}
			
			boolean studentChanged = false;
			if (status.data.maxCredit != null && !status.data.maxCredit.equals(student.getMaxCredit())) {
				student.setMaxCredit(status.data.maxCredit);
				studentChanged = true;
			}
			if (student.getOverrideExternalId() != null) {
				SpecialRegistrationRequest req = null;
				for (SpecialRegistrationRequest r: status.data.requests) {
					if (student.getOverrideExternalId().equals(r.requestId)) { req = r; break; }
				}
				if (req == null) {
					student.setOverrideExternalId(null);
					student.setOverrideMaxCredit(null);
					student.setOverrideStatus(null);
					student.setOverrideTimeStamp(null);
					studentChanged = true;
				} else {
					Integer oldStatus = student.getOverrideStatus();
					if (RequestStatus.denied.name().equals(req.status))
						student.setMaxCreditOverrideStatus(CourseRequestOverrideStatus.REJECTED);
					else if (RequestStatus.approved.name().equals(req.status))
						student.setMaxCreditOverrideStatus(CourseRequestOverrideStatus.APPROVED);
					else if (RequestStatus.cancelled.name().equals(req.status))
						student.setMaxCreditOverrideStatus(CourseRequestOverrideStatus.CANCELLED);
					else
						student.setMaxCreditOverrideStatus(CourseRequestOverrideStatus.PENDING);
					if (oldStatus == null || !oldStatus.equals(student.getOverrideStatus()))
						studentChanged = true;
				}
			} else {
				SpecialRegistrationRequest req = null;
				for (SpecialRegistrationRequest r: status.data.requests) {
					if (r.requestId == null || r.maxCredit == null) continue;
					if (req == null || r.dateCreated.isAfter(req.dateCreated)) req = r;
				}
				if (req != null) {
					student.setOverrideExternalId(req.requestId);
					student.setOverrideMaxCredit(req.maxCredit);
					if (RequestStatus.denied.name().equals(req.status))
						student.setMaxCreditOverrideStatus(CourseRequestOverrideStatus.REJECTED);
					else if (RequestStatus.approved.name().equals(req.status))
						student.setMaxCreditOverrideStatus(CourseRequestOverrideStatus.APPROVED);
					else if (RequestStatus.cancelled.name().equals(req.status))
						student.setMaxCreditOverrideStatus(CourseRequestOverrideStatus.CANCELLED);
					else
						student.setMaxCreditOverrideStatus(CourseRequestOverrideStatus.PENDING);
					student.setOverrideTimeStamp(req.dateCreated.toDate());
					studentChanged = true;
				}
			}
			if (studentChanged) helper.getHibSession().update(student);
			
			if (changed || studentChanged) helper.getHibSession().flush();
						
			return changed || studentChanged;
		} catch (SectioningException e) {
			action.setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			action.setApiException(e.getMessage() == null ? "Null" : e.getMessage());
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
