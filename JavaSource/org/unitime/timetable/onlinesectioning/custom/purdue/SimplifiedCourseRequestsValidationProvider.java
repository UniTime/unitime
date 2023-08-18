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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.studentsct.extension.StudentQuality;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.online.OnlineReservation;
import org.cpsolver.studentsct.online.OnlineSectioningModel;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestPriority;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.CourseDemand.Critical;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.Builder;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.AdvisorCourseRequestsValidationProvider;
import org.unitime.timetable.onlinesectioning.custom.CourseRequestsValidationProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.StudentHoldsCheckProvider;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ApiMode;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckEligibilityResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.EligibilityProblem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ResponseStatus;
import org.unitime.timetable.onlinesectioning.model.XAdvisorRequest;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XDistributionType;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservationType;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;
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
public class SimplifiedCourseRequestsValidationProvider implements CourseRequestsValidationProvider, StudentHoldsCheckProvider, AdvisorCourseRequestsValidationProvider {
	private static Log sLog = LogFactory.getLog(SimplifiedCourseRequestsValidationProvider.class);
	private static StudentSectioningMessages MESSAGES = Localization.create(StudentSectioningMessages.class);
	protected static final StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	protected static Format<Number> sCreditFormat = Formats.getNumberFormat("0.##");
	
	private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	
	public SimplifiedCourseRequestsValidationProvider() {
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
	}
	
	protected String getSpecialRegistrationApiReadTimeout() {
		return ApplicationProperties.getProperty("purdue.specreg.readTimeout", "60000");
	}
	
	protected String getSpecialRegistrationApiSite() {
		return ApplicationProperties.getProperty("purdue.specreg.site");
	}
	
	protected String getSpecialRegistrationApiSiteCheckEligibility() {
		return ApplicationProperties.getProperty("purdue.specreg.site.checkEligibility", getSpecialRegistrationApiSite() + "/checkEligibility");
	}
	
	protected String getSpecialRegistrationApiKey() {
		return ApplicationProperties.getProperty("purdue.specreg.apiKey");
	}
	
	protected ApiMode getSpecialRegistrationApiMode() {
		return ApiMode.valueOf(ApplicationProperties.getProperty("purdue.specreg.mode.validation", "PREREG"));
	}
	
	protected String getBannerSite() {
		return ApplicationProperties.getProperty("banner.xe.site");
	}
	
	protected String getBannerUser(boolean admin) {
		if (admin) {
			String user = ApplicationProperties.getProperty("banner.xe.admin.user");
			if (user != null) return user;
		}
		return ApplicationProperties.getProperty("banner.xe.user");
	}
	
	protected String getBannerPassword(boolean admin) {
		if (admin) {
			String pwd = ApplicationProperties.getProperty("banner.xe.admin.password");
			if (pwd != null) return pwd;
		}
		return ApplicationProperties.getProperty("banner.xe.password");
	}
	
	protected String getAdminParameter() {
		return ApplicationProperties.getProperty("banner.xe.adminParameter", "systemIn");
	}
	
	protected String getBannerErrors() {
		return ApplicationProperties.getProperty("banner.xe.prereg.errors", "(Holds prevent registration\\.|Student Status prevents registration\\.)");
	}
	
	protected boolean isValidationEnabled(org.unitime.timetable.model.Student student) {
		if (student == null) return false;
		StudentSectioningStatus status = student.getEffectiveStatus();
		return status == null || status.hasOption(StudentSectioningStatus.Option.reqval);
	}
	
	protected boolean isValidationEnabled(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) {
		String status = student.getStatus();
		if (status == null) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status == null) return true;
		StudentSectioningStatus dbStatus = StudentSectioningStatus.getPresentStatus(status, server.getAcademicSession().getUniqueId(), helper.getHibSession());
		return dbStatus != null && dbStatus.hasOption(StudentSectioningStatus.Option.reqval);
	}
	
	protected String getBannerTerm(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalTerm(session);
	}
	
	protected String getBannerCampus(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalCampus(session);
	}
	
	protected boolean isBannerAdmin() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.xe.admin", "false"));
	}
	
	protected boolean isPreregAdmin() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.xe.prereg.admin", "false"));
	}
	
	protected boolean isUseXE() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.specReg.XEeligibility", "false"));
	}
	
	protected boolean isCheckForPin() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.specReg.checkForPin", "true"));
	}
	
	protected boolean isWaitListNoAlts() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.specreg.waitListNoAlts", "false"));
	}
	
	protected boolean isAdvisedNoAlts() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.specreg.advisedNoAlts", "true"));
	}
	
	protected String getBannerId(org.unitime.timetable.model.Student student) {
		String id = student.getExternalUniqueId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	protected String getBannerId(XStudentId student) {
		String id = student.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
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
		});
		if (helper.isDebugEnabled()) builder.setPrettyPrinting();
		return builder.create();
	}
	
	public boolean isDisableRegistrationWhenNotEligible() {
		return "true".equals(ApplicationProperties.getProperty("purdue.specreg.disableRegistrationWhenNotEligible", "true"));
	}
	
	@Override
	public void checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, EligibilityCheck check, Student student) throws SectioningException {
		if (student == null || !check.hasFlag(EligibilityCheck.EligibilityFlag.CAN_REGISTER)) return;
		// Do not check eligibility when validation is disabled
		if (!isValidationEnabled(student)) return;
		
		if (isUseXE()) {
			ClientResource resource = null;
			try {
				String pin = helper.getPin();
				if ((pin == null || pin.isEmpty()) && student.hasReleasedPin()) pin = student.getPin();
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				boolean manager = helper.getUser().getType() == OnlineSectioningLog.Entity.EntityType.MANAGER;
				boolean admin = manager && isBannerAdmin();
				if (helper.isDebugEnabled())
					helper.debug("Checking eligility for " + student.getName("last-first-middle") + " (term: " + term + ", id:" + getBannerId(student) + (admin ? ", admin" : pin != null ? ", pin:" + pin : "") + ")");

				// First, check student registration status
				resource = new ClientResource(getBannerSite());
				resource.setNext(iClient);
				resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, getBannerUser(manager), getBannerPassword(manager));
				Gson gson = getGson(helper);
				XEInterface.RegisterResponse original = null;

				resource.addQueryParameter("term", term);
				resource.addQueryParameter("bannerId", getBannerId(student));
				helper.getAction().addOptionBuilder().setKey("term").setValue(term);
				helper.getAction().addOptionBuilder().setKey("bannerId").setValue(getBannerId(student));
				if (admin || isPreregAdmin()) {
					String param = getAdminParameter();
					resource.addQueryParameter(param, "SB");
					helper.getAction().addOptionBuilder().setKey(param).setValue("SB");
				} else if (pin != null && !pin.isEmpty()) {
					resource.addQueryParameter("altPin", pin);
					helper.getAction().addOptionBuilder().setKey("pin").setValue(pin);
				}
				long t0 = System.currentTimeMillis();
				try {
					resource.get(MediaType.APPLICATION_JSON);
				} catch (ResourceException exception) {
					helper.getAction().setApiException(exception.getMessage());
					try {
						XEInterface.ErrorResponse response = new GsonRepresentation<XEInterface.ErrorResponse>(resource.getResponseEntity(), XEInterface.ErrorResponse.class).getObject();
						helper.getAction().addOptionBuilder().setKey("exception").setValue(gson.toJson(response));
						XEInterface.Error error = response.getError();
						if (error != null && error.message != null) {
							throw new SectioningException(error.message);
						} else if (error != null && error.description != null) {
							throw new SectioningException(error.description);
						} else if (error != null && error.errorMessage != null) {
							throw new SectioningException(error.errorMessage);
						} else {
							throw exception;
						}
					} catch (SectioningException e) {
						helper.getAction().setApiException(e.getMessage());
						throw e;
					} catch (Throwable t) {
						throw exception;
					}
				} finally {
					helper.getAction().setApiGetTime(System.currentTimeMillis() - t0);
				}
				List<XEInterface.RegisterResponse> current = new GsonRepresentation<List<XEInterface.RegisterResponse>>(resource.getResponseEntity(), XEInterface.RegisterResponse.TYPE_LIST).getObject();
				helper.getAction().addOptionBuilder().setKey("response").setValue(gson.toJson(current));
				if (current != null && !current.isEmpty())
					original = current.get(0);
				
				// Check status, memorize enrolled sections
				if (original != null && helper.isDebugEnabled())
					helper.debug("Current registration: " + gson.toJson(original));
				if (original != null && original.maxHours != null)
					check.setMaxCredit(original.maxHours);
				
				String bannerErrors = getBannerErrors();
				String error = null;
				if (original != null && original.failureReasons != null) {
					for (String m: original.failureReasons) {
						if (bannerErrors == null || m.matches(bannerErrors)) {
							if (error == null)
								error = m;
							else
								error += (error.endsWith(".") ? " " : ", ") + m;
						}
					}
				}
				if (error != null) {
					if (isDisableRegistrationWhenNotEligible())
						check.setFlag(EligibilityCheck.EligibilityFlag.CAN_REGISTER, helper.isAdmin());
					check.setMessage(MESSAGES.exceptionFailedEligibilityCheck(error));
				}
				if (student.getUniqueId() != null && original != null && original.maxHours != null && original.maxHours > 0 && original.maxHours != student.getMaxCredit()) {
					Student dbStudent = StudentDAO.getInstance().get(student.getUniqueId(), helper.getHibSession());
					dbStudent.setMaxCredit(original.maxHours);
					helper.getHibSession().merge(dbStudent);
					helper.getHibSession().flush();
					if (!(server instanceof DatabaseServer)) {
						XStudent xs = server.getStudent(student.getUniqueId());
						if (xs != null) {
							xs.setMaxCredit(original.maxHours);
							server.update(xs, false);
						}
					}
				}
			} catch (SectioningException e) {
				helper.info("Banner eligibility failed: " + e.getMessage());
				throw e;
			} catch (Exception e) {
				helper.warn("Banner eligibility failed: " + e.getMessage(), e);
				throw new SectioningException(e.getMessage());
			} finally {
				if (resource != null) {
					if (resource.getResponse() != null) resource.getResponse().release();
					resource.release();
				}
			}	
		} else {
			ClientResource resource = null;
			try {
				resource = new ClientResource(getSpecialRegistrationApiSiteCheckEligibility());
				resource.setNext(iClient);
				
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				String campus = getBannerCampus(session);
				resource.addQueryParameter("term", term);
				resource.addQueryParameter("campus", campus);
				resource.addQueryParameter("studentId", getBannerId(student));
				resource.addQueryParameter("mode", getSpecialRegistrationApiMode().name());
				helper.getAction().addOptionBuilder().setKey("term").setValue(term);
				helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
				helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				long t0 = System.currentTimeMillis();
				
				resource.get(MediaType.APPLICATION_JSON);
				
				helper.getAction().setApiGetTime(System.currentTimeMillis() - t0);
				
				CheckEligibilityResponse eligibility = (CheckEligibilityResponse)new GsonRepresentation<CheckEligibilityResponse>(resource.getResponseEntity(), CheckEligibilityResponse.class).getObject();
				Gson gson = getGson(helper);
				
				if (helper.isDebugEnabled())
					helper.debug("Eligibility: " + gson.toJson(eligibility));
				helper.getAction().addOptionBuilder().setKey("response").setValue(gson.toJson(eligibility));
				
				if (ResponseStatus.success != eligibility.status)
					throw new SectioningException(eligibility.message == null || eligibility.message.isEmpty() ? "Failed to check student eligibility (" + eligibility.status + ")." : eligibility.message);
				
				if (eligibility.data != null && eligibility.data.eligibilityProblems != null) {
					String m = null;
					for (EligibilityProblem p: eligibility.data.eligibilityProblems)
						if (m == null)
							m = p.message;
						else
							m += "\n" + p.message;
					if (m != null) {
						if (isDisableRegistrationWhenNotEligible())
							check.setFlag(EligibilityCheck.EligibilityFlag.CAN_REGISTER, helper.isAdmin());
						check.setMessage(MESSAGES.exceptionFailedEligibilityCheck(m));
					}
				}
				String pin = null;
				if (eligibility.data != null && eligibility.data.PIN != null && !eligibility.data.PIN.isEmpty() && !"NA".equals(eligibility.data.PIN))
					pin = eligibility.data.PIN;
				Float maxCredit = null;
				if (eligibility.maxCredit != null && eligibility.maxCredit > 0) {
					maxCredit = eligibility.maxCredit;
					check.setMaxCredit(eligibility.maxCredit);
				}
				if ((maxCredit != null && !maxCredit.equals(student.getMaxCredit())) || (pin != null && !pin.equals(student.getPin()))) {
					Student dbStudent = StudentDAO.getInstance().get(student.getUniqueId(), helper.getHibSession());
					if (maxCredit != null) dbStudent.setMaxCredit(maxCredit);
					if (pin != null) dbStudent.setPin(pin);
					helper.getHibSession().merge(dbStudent);
					helper.getHibSession().flush();
					if (!(server instanceof DatabaseServer)) {
						XStudent xs = server.getStudent(student.getUniqueId());
						if (xs != null) {
							if (maxCredit != null) xs.setMaxCredit(maxCredit);
							if (pin != null) xs.setPin(pin);
							server.update(xs, false);
						}
					}
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
		// Do not check when validation is disabled
		if (!isValidationEnabled(server, helper, original)) return;
		
		Integer ORD_UNITIME = 0;
		
		Set<Long> advisorCoursesNoAlt = new HashSet<Long>();
		if (original.hasAdvisorRequests() && isAdvisedNoAlts())
			for (XAdvisorRequest ar: original.getAdvisorRequests()) {
				int count = 0;
				for (XAdvisorRequest x: original.getAdvisorRequests()) {
					if (x.getPriority() == ar.getPriority()) count ++;
				}
				if (count == 1 && ar.getCourseId() != null) advisorCoursesNoAlt.add(ar.getCourseId().getCourseId());
			}
		else if (original.hasAdvisorRequests() && isWaitListNoAlts())
			for (XAdvisorRequest ar: original.getAdvisorRequests()) {
				if (ar.isWaitListOrNoSub() && !ar.isSubstitute()) {
					int count = 0;
					for (XAdvisorRequest x: original.getAdvisorRequests()) {
						if (x.getPriority() == ar.getPriority() && !x.isSubstitute()) count ++;
					}
					if (count == 1 && ar.getCourseId() != null) advisorCoursesNoAlt.add(ar.getCourseId().getCourseId());
				}
			}
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() == 1) {
				RequestedCourse rc = r.getRequestedCourse(0);
				if (rc.getCourseId() != null && !rc.isReadOnly() && !advisorCoursesNoAlt.contains(rc.getCourseId())) {
					request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "NO_ALT",
							ApplicationProperties.getProperty("purdue.specreg.messages.courseHasNoAlt", "No alternative course provided.").replace("{course}", rc.getCourseName()), ORD_UNITIME);
				}
			}
		}
		
		for (int i = 0; i < request.getCourses().size(); i++) {
			CourseRequestInterface.Request r = request.getCourse(i);
			if (r.hasRequestedCourse() && r.getRequestedCourse(0).isFreeTime()) {
				boolean hasCourse = false;
				for (int j = i + 1; j < request.getCourses().size(); j++) {
					CourseRequestInterface.Request q = request.getCourse(j);
					if (q.hasRequestedCourse() && q.getRequestedCourse(0).isCourse()) {
						hasCourse = true; break;
					}
				}
				if (hasCourse) {
					String free = "";
					for (FreeTime ft: r.getRequestedCourse(0).getFreeTime()) {
						if (!free.isEmpty()) free += ", ";
						free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
					}
					request.addConfirmationMessage(0l, CONSTANTS.freePrefix() + free, "FREE-TIME",
						ApplicationProperties.getProperty("purdue.specreg.messages.freeTimeHighPriority", "High priority free time"), ORD_UNITIME);
				}
			}
		}
		
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
		if (!request.isEmpty()) request.setMaxCreditOverrideStatus(RequestedCourseStatus.SAVED);
		if (minCreditLimit != null && minCredit > 0 && minCredit < Float.parseFloat(minCreditLimit) && (original.getMaxCredit() == null || original.getMaxCredit() > Float.parseFloat(minCreditLimit))) {
			request.setCreditWarning(
					ApplicationProperties.getProperty("purdue.specreg.messages.minCredit",
					"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit))
					);
			request.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_LOW);
		}
		
		Float maxCredit = original.getMaxCredit();
		if (maxCredit == null) maxCredit = Float.parseFloat(ApplicationProperties.getProperty("purdue.specreg.maxCreditDefault", "18"));
		
		Set<Long> advisorWaitListedCourseIds = original.getAdvisorWaitListedCourseIds(server);
		if (maxCredit < request.getCredit(advisorWaitListedCourseIds)) {
			for (RequestedCourse rc: getOverCreditRequests(request, maxCredit)) {
				request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "CREDIT",
						ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(advisorWaitListedCourseIds))), null,
						ORD_UNITIME);
			}
			request.setCreditWarning(ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(advisorWaitListedCourseIds))));
			request.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_HIGH);
		}
		
		StudentSchedulingRule rule = StudentSchedulingRule.getRuleOnline(original, server, helper);
		if (rule != null) {
			for (XRequest r: original.getRequests()) {
				if (r instanceof XCourseRequest) {
					XCourseRequest cr = (XCourseRequest)r;
					for (XCourseId course: cr.getCourseIds()) {
						if (!rule.matchesCourse(course, helper.getHibSession())) {
							request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "NOT-RULE",
									ApplicationProperties.getProperty("purdue.specreg.messages.notMatchingRuleCourse", "No {rule} option.")
									.replace("{rule}", rule.getRuleName())
									.replace("{course}", course.getCourseName()),
									ORD_UNITIME);
							RequestPriority rp = request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName()));
							if (rp != null)
								rp.getRequest().getRequestedCourse(rp.getChoice()).setInactive(true);
						}
					}
				}
			}
		} else {
			String filter = server.getConfig().getProperty("Load.OnlineOnlyStudentFilter", null);
			if (filter != null && !filter.isEmpty()) {
				if (new Query(filter).match(new StudentMatcher(original, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
					// online only
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.OnlineOnlyInstructionalModeRegExp");
					for (XRequest r: original.getRequests()) {
						if (r instanceof XCourseRequest) {
							XCourseRequest cr = (XCourseRequest)r;
							for (XCourseId course: cr.getCourseIds()) {
								if (cn != null && !cn.isEmpty() && !course.getCourseName().matches(cn)) {
									request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
											ApplicationProperties.getProperty("purdue.specreg.messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
											ORD_UNITIME);
									RequestPriority rp = request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName()));
									if (rp != null)
										rp.getRequest().getRequestedCourse(rp.getChoice()).setInactive(true);
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(course.getOfferingId(), helper.getHibSession());
									if (offering != null)
										for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;	
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
												ApplicationProperties.getProperty("purdue.specreg.messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
												ORD_UNITIME);
										RequestPriority rp = request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName()));
										if (rp != null)
											rp.getRequest().getRequestedCourse(rp.getChoice()).setInactive(true);
									}
								}
							}
						}
					}
				} else if (server.getConfig().getPropertyBoolean("Load.OnlineOnlyExclusiveCourses", false)) {
					// exclusive
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.ResidentialInstructionalModeRegExp");
					for (XRequest r: original.getRequests()) {
						if (r instanceof XCourseRequest) {
							XCourseRequest cr = (XCourseRequest)r;
							for (XCourseId course: cr.getCourseIds()) {
								if (cn != null && !cn.isEmpty() && course.getCourseName().matches(cn)) {
									request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
											ApplicationProperties.getProperty("purdue.specreg.messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
											ORD_UNITIME);
									RequestPriority rp = request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName()));
									if (rp != null)
										rp.getRequest().getRequestedCourse(rp.getChoice()).setInactive(true);
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(course.getOfferingId(), helper.getHibSession());
									if (offering != null)
										for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;	
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
												ApplicationProperties.getProperty("purdue.specreg.messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
												ORD_UNITIME);
										RequestPriority rp = request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName()));
										if (rp != null)
											rp.getRequest().getRequestedCourse(rp.getChoice()).setInactive(true);
									}
								}
							}
						}
					}
				}
			}
		}
		
		if (!(server instanceof DatabaseServer)) {
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
													ApplicationProperties.getProperty("purdue.specreg.messages.courseOverlaps", "Conflicts with {other}.").replace("{course}", course.getCourseName()).replace("{other}", singleSections.get(other).getCourseName()),
													ORD_UNITIME);
										}
									}
									if (cr.getCourseIds().size() == 1) {
										// remember section when there are no alternative courses provided
										singleSections.put(section, course);
									}
								}
							}
						}
						if (offering.hasInconsistentRequirements(original, cr, course, server.getAcademicSession()))
							request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "STUD_PREF",
									ApplicationProperties.getProperty("purdue.specreg.messages.inconsistentStudPref", "Not available due to preferences selected.").replace("{course}", course.getCourseName()),
									ORD_UNITIME);
					}
				}
			}
		}
	}

	@Override
	public boolean updateStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, Builder action) throws SectioningException {
		return false;
	}
	
	@Override
	public boolean revalidateStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, Builder action) throws SectioningException {
		return false;
	}
	
	@Override
	public void validate(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request, CheckCoursesResponse response) throws SectioningException {
		XStudent original = (request.getStudentId() == null ? null : server.getStudent(request.getStudentId()));
		if (original == null) throw new PageAccessException(MESSAGES.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
		// Do not validate when validation is disabled
		if (!isValidationEnabled(server, helper, original)) return;
		
		Integer CONF_NONE = null;
		Integer CONF_UNITIME = 0;
		
		Set<Long> coursesWithNotAlt = new HashSet<Long>();
		for (XRequest r: original.getRequests()) {
			if (r instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)r;
				if (cr.getCourseIds().size() == 1 && !cr.isAlternative()) coursesWithNotAlt.add(cr.getCourseIds().get(0).getCourseId());
			}
		}
		Set<Long> advisorCoursesNoAlt = new HashSet<Long>();
		if (original.hasAdvisorRequests() && isAdvisedNoAlts())
			for (XAdvisorRequest ar: original.getAdvisorRequests()) {
				int count = 0;
				for (XAdvisorRequest x: original.getAdvisorRequests()) {
					if (x.getPriority() == ar.getPriority()) count ++;
				}
				if (count == 1 && ar.getCourseId() != null) advisorCoursesNoAlt.add(ar.getCourseId().getCourseId());
			}
		else if (original.hasAdvisorRequests() && isWaitListNoAlts())
			for (XAdvisorRequest ar: original.getAdvisorRequests()) {
				if (ar.isWaitListOrNoSub() && !ar.isSubstitute()) {
					int count = 0;
					for (XAdvisorRequest x: original.getAdvisorRequests()) {
						if (x.getPriority() == ar.getPriority() && !x.isSubstitute()) count ++;
					}
					if (count == 1 && ar.getCourseId() != null) advisorCoursesNoAlt.add(ar.getCourseId().getCourseId());
				}
			}
		boolean questionNoAlt = false;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() == 1) {
				RequestedCourse rc = r.getRequestedCourse(0);
				if (rc.getCourseId() != null && !rc.isReadOnly() && !advisorCoursesNoAlt.contains(rc.getCourseId())) {
					response.addMessage(rc.getCourseId(), rc.getCourseName(), "NO_ALT",
							ApplicationProperties.getProperty("purdue.specreg.messages.courseHasNoAlt", "No alternative course provided.").replace("{course}", rc.getCourseName()),
							!coursesWithNotAlt.contains(rc.getCourseId()) ? CONF_UNITIME : CONF_NONE);
					if (!coursesWithNotAlt.contains(rc.getCourseId())) {
						questionNoAlt = true;
					}
				}
			}
		}
		
		boolean questionTimeConflict = false;
		boolean questionInconStuPref = false;
		if (!(server instanceof DatabaseServer)) {
			OnlineSectioningModel model = new OnlineSectioningModel(server.getConfig(), server.getOverExpectedCriterion());
			model.setDayOfWeekOffset(server.getAcademicSession().getDayOfWeekOffset());
			boolean linkedClassesMustBeUsed = server.getConfig().getPropertyBoolean("LinkedClasses.mustBeUsed", false);
			Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
			
			org.cpsolver.studentsct.model.Student student = new org.cpsolver.studentsct.model.Student(request.getStudentId());
			student.setExternalId(original.getExternalId());
			student.setName(original.getName());
			student.setNeedShortDistances(original.hasAccomodation(server.getDistanceMetric().getShortDistanceAccommodationReference()));
			student.setAllowDisabled(original.isAllowDisabled());
			student.setClassFirstDate(original.getClassStartDate());
			student.setClassLastDate(original.getClassEndDate());
			student.setBackToBackPreference(original.getBackToBackPreference());
			student.setModalityPreference(original.getModalityPreference());
			Map<Long, Section> classTable = new HashMap<Long, Section>();
			Set<XDistribution> distributions = new HashSet<XDistribution>();
			boolean hasAssignment = false;
			for (XRequest reqest: original.getRequests()) {
				if (reqest instanceof XCourseRequest && ((XCourseRequest)reqest).getEnrollment() != null) {
					hasAssignment = true; break;
				}
			}
			for (CourseRequestInterface.Request c: request.getCourses())
				FindAssignmentAction.addRequest(server, model, assignment, student, original, c, false, false, classTable, distributions, hasAssignment, true, helper);
			for (CourseRequestInterface.Request c: request.getAlternatives())
				FindAssignmentAction.addRequest(server, model, assignment, student, original, c, true, false, classTable, distributions, hasAssignment, true, helper);
			model.addStudent(student);
			model.setStudentQuality(new StudentQuality(server.getDistanceMetric(), model.getProperties()));
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
			if ("true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.specreg.dummyReservation", "false"))) {
				for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
					Request r = (Request)e.next();
					if (r instanceof CourseRequest) {
						CourseRequest cr = (CourseRequest)r;
						for (Course course: cr.getCourses()) {
							new OnlineReservation(XReservationType.Dummy.ordinal(), -3l, course.getOffering(), 5000, true, 1, true, true, true, true, true);
							continue;
						}
					}
				}
			}
			
			// Single section time conflict check
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
													ApplicationProperties.getProperty("purdue.specreg.messages.courseOverlaps", "Conflicts with {other}.").replace("{course}", course.getName()).replace("{other}", singleSections.get(other).getName()),
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
			
			// Inconsistent requirements
			for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
				Request r = (Request)e.next();
				if (r instanceof CourseRequest) {
					CourseRequest cr = (CourseRequest)r;
					for (Course course: cr.getCourses()) {
						if (SectioningRequest.hasInconsistentRequirements(cr, course.getId())) {
							boolean confirm = (original.getRequestForCourse(course.getId()) == null);
							response.addMessage(course.getId(), course.getName(), "STUD_PREF",
									ApplicationProperties.getProperty("purdue.specreg.messages.inconsistentStudPref", "Not available due to preferences selected.").replace("{course}", course.getName()),
									confirm ? CONF_UNITIME : CONF_NONE);
							if (confirm) questionInconStuPref = true;
						}
					}
				}
			}
		}
		
		// Check for critical course removals
		boolean questionDropCritical = false;
		boolean dropImportant = false, dropVital = false, dropCritical = false;
		for (XRequest r: original.getRequests()) {
			if (r instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)r;
				if (cr.isCritical() && !cr.isAlternative() && !cr.getCourseIds().isEmpty()) {
					boolean hasCourse = false;
					for (XCourseId course: cr.getCourseIds()) {
						if (request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName())) != null) {
							hasCourse = true; break;
						}
					}
					if (!hasCourse) {
						XCourseId course = cr.getCourseIds().get(0);
						if (cr.getCritical() == 2) {
							response.addMessage(course.getCourseId(), course.getCourseName(), "DROP_CRIT",
									ApplicationProperties.getProperty("purdue.specreg.messages.courseDropCrit", "Important course has been removed.").replace("{course}", course.getCourseName()),
									CONF_UNITIME);
							dropImportant = true;
						} else if (cr.getCritical() == 3) {
							response.addMessage(course.getCourseId(), course.getCourseName(), "DROP_CRIT",
									ApplicationProperties.getProperty("purdue.specreg.messages.courseDropCrit", "Vital course has been removed.").replace("{course}", course.getCourseName()),
									CONF_UNITIME);
							dropVital = true;
						} else {
							response.addMessage(course.getCourseId(), course.getCourseName(), "DROP_CRIT",
									ApplicationProperties.getProperty("purdue.specreg.messages.courseDropCrit", "Critical course has been removed.").replace("{course}", course.getCourseName()),
									CONF_UNITIME);
							dropCritical = true;
						}
						questionDropCritical = true;
					}
				}
			}
		}
		
		// Check for missing critical courses that have been recommended by the advisor
		boolean questionMissingAdvisorCritical = false;
		boolean missCritical = false, missImportant = false, missVital = false;
		CourseDemand.Critical advCritical = CourseDemand.Critical.fromText(ApplicationProperty.AdvisorCourseRequestsAllowCritical.valueOfSession(server.getAcademicSession().getUniqueId()));
		if (original.hasAdvisorRequests()) {
			for (XAdvisorRequest ar: original.getAdvisorRequests()) {
				if (ar.getAlternative() == 0 && !ar.isSubstitute() && ar.isCritical() && ar.hasCourseId()) {
					RequestPriority arp = request.getRequestPriority(new RequestedCourse(ar.getCourseId().getCourseId(), ar.getCourseId().getCourseName()));
					if (arp == null || arp.isAlternative()) {
						boolean hasAlt = false;
						for (XAdvisorRequest alt: original.getAdvisorRequests()) {
							if (alt.getPriority() != ar.getPriority() || !alt.hasCourseId() || alt.isSubstitute() || !alt.isCritical() || ar.getAlternative() == 0) continue;
							RequestPriority altrp = request.getRequestPriority(new RequestedCourse(alt.getCourseId().getCourseId(), alt.getCourseId().getCourseName())); 
							if (altrp != null && !altrp.isAlternative()) {
								hasAlt = true; break;
							}
						}
						if (!hasAlt) {
							if (advCritical == Critical.IMPORTANT || ar.getCritical() == 2) {
								response.addMessage(ar.getCourseId().getCourseId(), ar.getCourseId().getCourseName(), "DROP_CRIT",
										ApplicationProperties.getProperty("purdue.specreg.messages.courseMissingAdvisedCritical", "Missing important course that has been recommended by the advisor.").replace("{course}", ar.getCourseId().getCourseName()),
										CONF_UNITIME);
								missImportant = true;
							} else if (advCritical == Critical.VITAL || ar.getCritical() == 3) {
								response.addMessage(ar.getCourseId().getCourseId(), ar.getCourseId().getCourseName(), "DROP_CRIT",
										ApplicationProperties.getProperty("purdue.specreg.messages.courseMissingAdvisedCritical", "Missing vital course that has been recommended by the advisor.").replace("{course}", ar.getCourseId().getCourseName()),
										CONF_UNITIME);
								missVital = true;
							} else {
								response.addMessage(ar.getCourseId().getCourseId(), ar.getCourseId().getCourseName(), "DROP_CRIT",
										ApplicationProperties.getProperty("purdue.specreg.messages.courseMissingAdvisedCritical", "Missing critical course that has been recommended by the advisor.").replace("{course}", ar.getCourseId().getCourseName()),
										CONF_UNITIME);
								missCritical = true;
							}
							questionMissingAdvisorCritical = true;
						}
					}
				}
			}
		}
		
		boolean questionRestrictionsNotMet = false;
		StudentSchedulingRule rule = StudentSchedulingRule.getRuleOnline(original, server, helper);
		boolean onlineOnly = false;
		if (rule != null) {
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse course: r.getRequestedCourse()) {
						if (course.getCourseId() == null) continue;
						CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
						if (co != null && !rule.matchesCourse(co)) {
							boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
							response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RULE",
									ApplicationProperties.getProperty("purdue.specreg.messages.notMatchingRuleCourse", "No {rule} option.")
									.replace("{rule}", rule.getRuleName())
									.replace("{course}", course.getCourseName()),
									confirm ? CONF_UNITIME : CONF_NONE);
							if (confirm) questionRestrictionsNotMet = true;
						}
					}
			}
		} else {
			String filter = server.getConfig().getProperty("Load.OnlineOnlyStudentFilter", null);
			if (filter != null && !filter.isEmpty()) {
				if (new Query(filter).match(new StudentMatcher(original, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
					// online only
					onlineOnly = true;
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.OnlineOnlyInstructionalModeRegExp");
					for (CourseRequestInterface.Request r: request.getCourses()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && !course.getCourseName().matches(cn)) {
									boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
											ApplicationProperties.getProperty("purdue.specreg.messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
											confirm ? CONF_UNITIME : CONF_NONE);
									if (confirm) questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (config.getInstructionalMethod() == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;	
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
												ApplicationProperties.getProperty("purdue.specreg.messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
												confirm ? CONF_UNITIME : CONF_NONE);
										if (confirm) questionRestrictionsNotMet = true;
									}
								}
							}
					}
					for (CourseRequestInterface.Request r: request.getAlternatives()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && !course.getCourseName().matches(cn)) {
									boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
											ApplicationProperties.getProperty("purdue.specreg.messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
											confirm ? CONF_UNITIME : CONF_NONE);
									if (confirm) questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;	
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
												ApplicationProperties.getProperty("purdue.specreg.messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
												confirm ? CONF_UNITIME : CONF_NONE);
										if (confirm) questionRestrictionsNotMet = true;
									}
								}
							}
					}
				} else if (server.getConfig().getPropertyBoolean("Load.OnlineOnlyExclusiveCourses", false)) {
					// exclusive
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.ResidentialInstructionalModeRegExp");
					for (CourseRequestInterface.Request r: request.getCourses()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && course.getCourseName().matches(cn)) {
									boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
											ApplicationProperties.getProperty("purdue.specreg.messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
											confirm ? CONF_UNITIME : CONF_NONE);
									if (confirm) questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;	
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
												ApplicationProperties.getProperty("purdue.specreg.messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
												confirm ? CONF_UNITIME : CONF_NONE);
										if (confirm) questionRestrictionsNotMet = true;
									}
								}
							}
					}
					for (CourseRequestInterface.Request r: request.getAlternatives()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && course.getCourseName().matches(cn)) {
									boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
											ApplicationProperties.getProperty("purdue.specreg.messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
											confirm ? CONF_UNITIME : CONF_NONE);
									if (confirm) questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;	
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
												ApplicationProperties.getProperty("purdue.specreg.messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
												confirm ? CONF_UNITIME : CONF_NONE);
										if (confirm) questionRestrictionsNotMet = true;
									}
								}
							}
					}
				}
			}			
		}
		
		boolean questionFreeTime = false;
		for (int i = 0; i < request.getCourses().size(); i++) {
			CourseRequestInterface.Request r = request.getCourse(i);
			if (r.hasRequestedCourse() && r.getRequestedCourse(0).isFreeTime()) {
				boolean hasCourse = false;
				for (int j = i + 1; j < request.getCourses().size(); j++) {
					CourseRequestInterface.Request q = request.getCourse(j);
					if (q.hasRequestedCourse() && q.getRequestedCourse(0).isCourse()) {
						hasCourse = true;
					}
				}
				String free = "";
				boolean confirm = false;
				for (FreeTime ft: r.getRequestedCourse(0).getFreeTime()) {
					if (!free.isEmpty()) free += ", ";
					free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
					if (!confirm) {
						XFreeTimeRequest ftr = original.getRequestForFreeTime(ft);
						if (ftr == null) {
							confirm = true;
						} else if (hasCourse) {
							for (int j = i + 1; j < request.getCourses().size(); j++) {
								CourseRequestInterface.Request q = request.getCourse(j);
								if (q.hasRequestedCourse() && q.getRequestedCourse(0).isCourse()) {
									XCourseRequest cr = original.getRequestForCourse(q.getRequestedCourse(0).getCourseId());
									if (cr == null || cr.getPriority() < ftr.getPriority()) {
										confirm = true;
										break;
									}
								}
							}
						}
					}
				}
				if (hasCourse)
					response.addMessage(0l, CONSTANTS.freePrefix() + free, "FREE-TIME",
						ApplicationProperties.getProperty("purdue.specreg.messages.freeTimeHighPriority", "High priority free time"),
						confirm ? CONF_UNITIME : CONF_NONE);
				if (confirm) questionFreeTime = true;
			}
		}
		
		String creditError = null;
		Float maxCredit = original.getMaxCredit();
		if (maxCredit == null) maxCredit = Float.parseFloat(ApplicationProperties.getProperty("purdue.specreg.maxCreditDefault", "18"));
		
		Set<Long> advisorWaitListedCourseIds = original.getAdvisorWaitListedCourseIds(server);
		if (maxCredit != null && request.getCredit(advisorWaitListedCourseIds) > maxCredit) {
			for (RequestedCourse rc: getOverCreditRequests(request, maxCredit)) {
				response.addMessage(rc.getCourseId(), rc.getCourseName(), "CREDIT",
						ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(advisorWaitListedCourseIds)))
						, CONF_NONE);
			}
			response.setCreditWarning(ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit",
					"Maximum of {max} credit hours exceeded.")
					.replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(advisorWaitListedCourseIds))));
			response.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_HIGH);
			creditError = ApplicationProperties.getProperty("purdue.specreg.messages.maxCreditError",
					"Maximum of {max} credit hours exceeded.\nYou may not be able to get a full schedule.")
					.replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(advisorWaitListedCourseIds)));
		}

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
		if (creditError == null && minCreditLimit != null && minCredit < Float.parseFloat(minCreditLimit) && (maxCredit == null || maxCredit > Float.parseFloat(minCreditLimit))) {
			creditError = ApplicationProperties.getProperty("purdue.specreg.messages.minCredit",
					"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit));
			response.setCreditWarning(
					ApplicationProperties.getProperty("purdue.specreg.messages.minCredit",
					"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit))
					);
			response.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_LOW);
		}
		

		if (response.getConfirms().contains(CONF_UNITIME)) {
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.unitimeProblemsFound", "The following issues have been detected:"), CONF_UNITIME, -1);
			response.addConfirmation("", CONF_UNITIME, 1);
		}
		int line = 2;
		if (creditError != null) {
			response.addConfirmation(creditError, CONF_UNITIME, line++);
		}
		if (questionNoAlt)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.noAlternatives", (line > 2 ? "\n" : "") +
					"One or more of the newly requested courses have no alternatives provided. You may not be able to get a full schedule because you did not provide an alternative course."),
					CONF_UNITIME, line ++);
		
		if (questionTimeConflict)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.timeConflicts", (line > 2 ? "\n" : "") +
					"Two or more single section courses are conflicting with each other. You will likely not be able to get the conflicting course, so please provide an alternative course if possible."),
					CONF_UNITIME, line ++);
		
		if (questionInconStuPref)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.inconsistentStudPref", (line > 2 ? "\n" : "") +
					"One or more courses are not available due to the selected preferences."),
					CONF_UNITIME, line ++);
		
		if (questionDropCritical) {
			if (dropVital && !dropCritical && !dropImportant)
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.dropCritical", (line > 2 ? "\n" : "") +
						"One or more vital courses have been removed. This may prohibit progress towards degree. Please consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else if (dropImportant && !dropVital && !dropCritical)
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.dropCritical", (line > 2 ? "\n" : "") +
						"One or more important courses have been removed. This may prohibit progress towards degree. Please consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else if (advCritical != Critical.NORMAL)
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.dropCritical", (line > 2 ? "\n" : "") +
						"One or more critical courses have been removed. This may prohibit progress towards degree. Please consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.dropCritical", (line > 2 ? "\n" : "") +
						"One or more courses that are marked as critical in your degree plan have been removed. This may prohibit progress towards degree. Please consult with your academic advisor."),
						CONF_UNITIME, line ++);
		}
		
		if (questionMissingAdvisorCritical)
			if (advCritical == Critical.IMPORTANT || (missImportant && !missCritical && !missVital))
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.missingAdvisedCritical", (line > 2 ? "\n" : "") +
						"One or more courses that are marked by your advisor as important have not been requested. This may prohibit progress towards degree. Please see you advisor course requests and/or consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else if (advCritical == Critical.VITAL || (missVital && !missCritical && !missImportant))
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.missingAdvisedCritical", (line > 2 ? "\n" : "") +
						"One or more courses that are marked by your advisor as vital have not been requested. This may prohibit progress towards degree. Please see you advisor course requests and/or consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else if (advCritical == Critical.CRITICAL)
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.missingAdvisedCritical", (line > 2 ? "\n" : "") +
						"One or more courses that are marked by your advisor as critical have not been requested. This may prohibit progress towards degree. Please see you advisor course requests and/or consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.missingAdvisedCritical", (line > 2 ? "\n" : "") +
						"One or more courses that are marked as critical in your degree plan and that have been listed by your advisor have not been requested. This may prohibit progress towards degree. Please see you advisor course requests and/or consult with your academic advisor."),
						CONF_UNITIME, line ++);
		
		if (questionRestrictionsNotMet) {
			if (rule != null)
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.ruleNotMet", (line > 2 ? "\n" : "") +
						"One or more of the newly requested courses have no {rule} option at the moment. You may not be able to get a full schedule because becasue you are not allowed to take these courses."
						.replace("{rule}", rule.getRuleName())),
						CONF_UNITIME, line ++);
			else if (onlineOnly)
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.onlineOnlyNotMet", (line > 2 ? "\n" : "") +
					"One or more of the newly requested courses have no online-only option at the moment. You may not be able to get a full schedule because becasue you are not allowed to take these courses."),
					CONF_UNITIME, line ++);
			else
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.residentialNotMet", (line > 2 ? "\n" : "") +
					"One or more of the newly requested courses have no residential option at the moment. You may not be able to get a full schedule because becasue you are not allowed to take these courses."),
					CONF_UNITIME, line ++);
		}
		if (questionFreeTime) {
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.freeTimeRequested", (line > 2 ? "\n" : "") +
					"Free time requests will be considered as time blocks during the pre-registration process. When possible, classes should be avoided during free time. However, if a free time request is placed higher than a course, the course cannot be attended during free time and you may not receive a full schedule."),
					CONF_UNITIME, line ++);
		}
		
		if (line > 2)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.confirmation", "\nDo you want to proceed?"), CONF_UNITIME, line ++);

		Set<Integer> conf = response.getConfirms();
		if (conf.contains(CONF_UNITIME)) {
		response.setConfirmation(CONF_UNITIME, ApplicationProperties.getProperty("purdue.specreg.confirm.unitimeDialogName","Warning Confirmations"),
				(ApplicationProperties.getProperty("purdue.specreg.confirm.unitimeYesButton", "Accept & Submit")),
				ApplicationProperties.getProperty("purdue.specreg.confirm.unitimeNoButton", "Cancel Submit"),
				(ApplicationProperties.getProperty("purdue.specreg.confirm.unitimeYesButtonTitle", "Accept the above warning(s) and submit the Course Requests")),
				ApplicationProperties.getProperty("purdue.specreg.confirm.unitimeNoButtonTitle", "Go back to editing your Course Requests"));
		}
	}
	
	@Override
	public void submit(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request) throws SectioningException {
	}
	
	@Override
	public Collection<Long> updateStudents(OnlineSectioningServer server, OnlineSectioningHelper helper, List<Student> students) throws SectioningException {
		return null;
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
	public String getStudentHoldError(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId student) throws SectioningException {
		if (isUseXE()) {
			ClientResource resource = null;
			try {
				String pin = helper.getPin();
				if ((pin == null || pin.isEmpty()) && student instanceof XStudent && ((XStudent)student).hasReleasedPin())
					pin =  ((XStudent)student).getPin();
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				boolean manager = helper.getUser().getType() == OnlineSectioningLog.Entity.EntityType.MANAGER;
				boolean admin = manager && isBannerAdmin();
				if (helper.isDebugEnabled())
					helper.debug("Checking eligility for " + student.getName() + " (term: " + term + ", id:" + getBannerId(student) + (admin ? ", admin" : pin != null ? ", pin:" + pin : "") + ")");

				// First, check student registration status
				resource = new ClientResource(getBannerSite());
				resource.setNext(iClient);
				resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, getBannerUser(manager), getBannerPassword(manager));
				Gson gson = getGson(helper);
				XEInterface.RegisterResponse original = null;

				resource.addQueryParameter("term", term);
				resource.addQueryParameter("bannerId", getBannerId(student));
				helper.getAction().addOptionBuilder().setKey("term").setValue(term);
				helper.getAction().addOptionBuilder().setKey("bannerId").setValue(getBannerId(student));
				if (admin || isPreregAdmin()) {
					String param = getAdminParameter();
					resource.addQueryParameter(param, "SB");
					helper.getAction().addOptionBuilder().setKey(param).setValue("SB");
				} else if (pin != null && !pin.isEmpty()) {
					resource.addQueryParameter("altPin", pin);
					helper.getAction().addOptionBuilder().setKey("pin").setValue(pin);
				}
				long t0 = System.currentTimeMillis();
				try {
					resource.get(MediaType.APPLICATION_JSON);
				} catch (ResourceException exception) {
					helper.getAction().setApiException(exception.getMessage());
					try {
						XEInterface.ErrorResponse response = new GsonRepresentation<XEInterface.ErrorResponse>(resource.getResponseEntity(), XEInterface.ErrorResponse.class).getObject();
						helper.getAction().addOptionBuilder().setKey("exception").setValue(gson.toJson(response));
						XEInterface.Error error = response.getError();
						if (error != null && error.message != null) {
							throw new SectioningException(error.message);
						} else if (error != null && error.description != null) {
							throw new SectioningException(error.description);
						} else if (error != null && error.errorMessage != null) {
							throw new SectioningException(error.errorMessage);
						} else {
							throw exception;
						}
					} catch (SectioningException e) {
						helper.getAction().setApiException(e.getMessage());
						throw e;
					} catch (Throwable t) {
						throw exception;
					}
				} finally {
					if (!helper.getAction().hasApiGetTime())
						helper.getAction().setApiGetTime(System.currentTimeMillis() - t0);
				}
				List<XEInterface.RegisterResponse> current = new GsonRepresentation<List<XEInterface.RegisterResponse>>(resource.getResponseEntity(), XEInterface.RegisterResponse.TYPE_LIST).getObject();
				helper.getAction().addOptionBuilder().setKey("holds-response").setValue(gson.toJson(current));
				if (current != null && !current.isEmpty())
					original = current.get(0);
				
				// Check status, memorize enrolled sections
				if (original != null && helper.isDebugEnabled())
					helper.debug("Current registration: " + gson.toJson(original));
				
				String bannerErrors = getBannerErrors();
				String error = null;
				if (original != null && original.failureReasons != null) {
					for (String m: original.failureReasons) {
						if (bannerErrors == null || m.matches(bannerErrors)) {
							if (error == null)
								error = m;
							else
								error += (error.endsWith(".") ? " " : ", ") + m;
						}
					}
				}
				return error;
			} catch (SectioningException e) {
				helper.info("Banner eligibility failed: " + e.getMessage());
				throw e;
			} catch (Exception e) {
				helper.warn("Banner eligibility failed: " + e.getMessage(), e);
				throw new SectioningException(e.getMessage());
			} finally {
				if (resource != null) {
					if (resource.getResponse() != null) resource.getResponse().release();
					resource.release();
				}
			}	
		} else {
			ClientResource resource = null;
			try {
				resource = new ClientResource(getSpecialRegistrationApiSiteCheckEligibility());
				resource.setNext(iClient);
				
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				String campus = getBannerCampus(session);
				resource.addQueryParameter("term", term);
				resource.addQueryParameter("campus", campus);
				resource.addQueryParameter("studentId", getBannerId(student));
				resource.addQueryParameter("mode", getSpecialRegistrationApiMode().name());
				helper.getAction().addOptionBuilder().setKey("term").setValue(term);
				helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
				helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				long t0 = System.currentTimeMillis();
				
				resource.get(MediaType.APPLICATION_JSON);
				
				if (!helper.getAction().hasApiGetTime())
					helper.getAction().setApiGetTime(System.currentTimeMillis() - t0);
				
				CheckEligibilityResponse eligibility = (CheckEligibilityResponse)new GsonRepresentation<CheckEligibilityResponse>(resource.getResponseEntity(), CheckEligibilityResponse.class).getObject();
				Gson gson = getGson(helper);
				
				if (helper.isDebugEnabled())
					helper.debug("Eligibility: " + gson.toJson(eligibility));
				helper.getAction().addOptionBuilder().setKey("holds-response").setValue(gson.toJson(eligibility));
				
				if (ResponseStatus.success != eligibility.status)
					throw new SectioningException(eligibility.message == null || eligibility.message.isEmpty() ? "Failed to check student eligibility (" + eligibility.status + ")." : eligibility.message);
				
				if (isCheckForPin() && eligibility.data != null && eligibility.data.PIN != null && !eligibility.data.PIN.isEmpty() && !"NA".equals(eligibility.data.PIN)) {
					helper.getAction().addOptionBuilder().setKey("PIN").setValue(eligibility.data.PIN);
				}

				if (eligibility.data != null && eligibility.data.eligibilityProblems != null) {
					String m = null;
					for (EligibilityProblem p: eligibility.data.eligibilityProblems)
						if (m == null)
							m = p.message;
						else
							m += "\n" + p.message;
					return m;
				}

				return null;
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
	}
	
	protected boolean isAdvisorValidationEnabled(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, String status) {
		if (status == null) status = student.getStatus();
		else if (status.isEmpty()) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status == null) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status == null) return true;
		StudentSectioningStatus dbStatus = StudentSectioningStatus.getPresentStatus(status, server.getAcademicSession().getUniqueId(), helper.getHibSession());
		return dbStatus != null && dbStatus.hasOption(StudentSectioningStatus.Option.reqval) || dbStatus.hasOption(StudentSectioningStatus.Option.specreg);
	}

	@Override
	public void validateAdvisorRecommendations(OnlineSectioningServer server, OnlineSectioningHelper helper, AdvisingStudentDetails details, CheckCoursesResponse response) throws SectioningException {
		XStudent original = (details.getStudentId() == null ? null : server.getStudent(details.getStudentId()));
		if (original == null) throw new PageAccessException(MESSAGES.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
		// Do not validate when validation is disabled
		if (!isAdvisorValidationEnabled(server, helper, original, details.getStatus() == null ? null : details.getStatus().getReference())) return;
		CourseRequestInterface request = details.getRequest();
		
		Integer CONF_UNITIME = 0;
		
		Set<Long> courseIds = new HashSet<Long>();
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasCourseId() && !courseIds.add(rc.getCourseId())) {
						response.addError(rc.getCourseId(), rc.getCourseName(), "DUPL",
								ApplicationProperties.getProperty("purdue.specreg.messages.duplicateCourse", "Course {course} used multiple times.").replace("{course}", rc.getCourseName())
								);
						if (!response.hasErrorMessage())
							response.setErrorMessage(ApplicationProperties.getProperty("purdue.specreg.messages.duplicateCourse", "Course {course} used multiple times.").replace("{course}", rc.getCourseName()));
					}
				}
		}
		
		boolean questionNoAlt = false;
		if (!isAdvisedNoAlts())
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse() && r.getRequestedCourse().size() == 1) {
					if ((r.isWaitList() || r.isNoSub()) && isWaitListNoAlts()) continue;
					RequestedCourse rc = r.getRequestedCourse(0);
					if (rc.getCourseId() != null && !rc.isReadOnly()) {
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "NO_ALT",
								ApplicationProperties.getProperty("purdue.specreg.messages.courseHasNoAlt", "No alternative course provided.").replace("{course}", rc.getCourseName()),
								CONF_UNITIME);
						questionNoAlt = true;
					}
				}
			}
		
		boolean questionTimeConflict = false;
		boolean questionInconStuPref = false;
		if (!(server instanceof DatabaseServer)) {
			OnlineSectioningModel model = new OnlineSectioningModel(server.getConfig(), server.getOverExpectedCriterion());
			model.setDayOfWeekOffset(server.getAcademicSession().getDayOfWeekOffset());
			boolean linkedClassesMustBeUsed = server.getConfig().getPropertyBoolean("LinkedClasses.mustBeUsed", false);
			Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
			
			org.cpsolver.studentsct.model.Student student = new org.cpsolver.studentsct.model.Student(request.getStudentId());
			student.setExternalId(original.getExternalId());
			student.setName(original.getName());
			student.setNeedShortDistances(original.hasAccomodation(server.getDistanceMetric().getShortDistanceAccommodationReference()));
			student.setAllowDisabled(original.isAllowDisabled());
			student.setClassFirstDate(original.getClassStartDate());
			student.setClassLastDate(original.getClassEndDate());
			student.setBackToBackPreference(original.getBackToBackPreference());
			student.setModalityPreference(original.getModalityPreference());
			Map<Long, Section> classTable = new HashMap<Long, Section>();
			Set<XDistribution> distributions = new HashSet<XDistribution>();
			boolean hasAssignment = false;
			for (XRequest reqest: original.getRequests()) {
				if (reqest instanceof XCourseRequest && ((XCourseRequest)reqest).getEnrollment() != null) {
					hasAssignment = true; break;
				}
			}
			for (CourseRequestInterface.Request c: request.getCourses())
				FindAssignmentAction.addRequest(server, model, assignment, student, original, c, false, false, classTable, distributions, hasAssignment, true, helper);
			for (CourseRequestInterface.Request c: request.getAlternatives())
				FindAssignmentAction.addRequest(server, model, assignment, student, original, c, true, false, classTable, distributions, hasAssignment, true, helper);
			model.addStudent(student);
			model.setStudentQuality(new StudentQuality(server.getDistanceMetric(), model.getProperties()));
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
			if ("true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.specreg.dummyReservation", "false"))) {
				for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
					Request r = (Request)e.next();
					if (r instanceof CourseRequest) {
						CourseRequest cr = (CourseRequest)r;
						for (Course course: cr.getCourses()) {
							new OnlineReservation(XReservationType.Dummy.ordinal(), -3l, course.getOffering(), 5000, true, 1, true, true, true, true, true);
							continue;
						}
					}
				}
			}
			
			// Single section time conflict check
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
											response.addMessage(course.getId(), course.getName(), "OVERLAP",
													ApplicationProperties.getProperty("purdue.specreg.messages.courseOverlaps", "Conflicts with {other}.").replace("{course}", course.getName()).replace("{other}", singleSections.get(other).getName()),
													CONF_UNITIME);
											questionTimeConflict = true;
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
			
			// Inconsistent requirements
			for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
				Request r = (Request)e.next();
				if (r instanceof CourseRequest) {
					CourseRequest cr = (CourseRequest)r;
					for (Course course: cr.getCourses()) {
						if (SectioningRequest.hasInconsistentRequirements(cr, course.getId())) {
							response.addMessage(course.getId(), course.getName(), "STUD_PREF",
									ApplicationProperties.getProperty("purdue.specreg.messages.inconsistentStudPref", "Not available due to preferences selected.").replace("{course}", course.getName()),
									CONF_UNITIME);
							questionInconStuPref = true;
						}
					}
				}
			}
		}
		
		boolean questionRestrictionsNotMet = false;
		StudentSchedulingRule rule = StudentSchedulingRule.getRuleOnline(original, server, helper);
		boolean onlineOnly = false;
		if (rule != null) {
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse course: r.getRequestedCourse()) {
						if (course.getCourseId() == null) continue;
						CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
						if (co != null && !rule.matchesCourse(co)) {
							response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RULE",
									ApplicationProperties.getProperty("purdue.specreg.messages.notMatchingRuleCourse", "No {rule} option.")
									.replace("{rule}", rule.getRuleName())
									.replace("{course}", course.getCourseName()),
									CONF_UNITIME);
							questionRestrictionsNotMet = true;
						}
					}
			}
		} else {
			String filter = server.getConfig().getProperty("Load.OnlineOnlyStudentFilter", null);
			if (filter != null && !filter.isEmpty()) {
				if (new Query(filter).match(new StudentMatcher(original, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
					// online only
					onlineOnly = true;
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.OnlineOnlyInstructionalModeRegExp");
					for (CourseRequestInterface.Request r: request.getCourses()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && !course.getCourseName().matches(cn)) {
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
											ApplicationProperties.getProperty("purdue.specreg.messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
											CONF_UNITIME);
									questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (config.getInstructionalMethod() == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;	
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
												ApplicationProperties.getProperty("purdue.specreg.messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
												CONF_UNITIME);
										questionRestrictionsNotMet = true;
									}
								}
							}
					}
					for (CourseRequestInterface.Request r: request.getAlternatives()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && !course.getCourseName().matches(cn)) {
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
											ApplicationProperties.getProperty("purdue.specreg.messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
											CONF_UNITIME);
									questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;	
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
												ApplicationProperties.getProperty("purdue.specreg.messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
												CONF_UNITIME);
										questionRestrictionsNotMet = true;
									}
								}
							}
					}
				} else if (server.getConfig().getPropertyBoolean("Load.OnlineOnlyExclusiveCourses", false)) {
					// exclusive
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.ResidentialInstructionalModeRegExp");
					for (CourseRequestInterface.Request r: request.getCourses()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && course.getCourseName().matches(cn)) {
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
											ApplicationProperties.getProperty("purdue.specreg.messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
											CONF_UNITIME);
									questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;	
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
												ApplicationProperties.getProperty("purdue.specreg.messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
												CONF_UNITIME);
										questionRestrictionsNotMet = true;
									}
								}
							}
					}
					for (CourseRequestInterface.Request r: request.getAlternatives()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && course.getCourseName().matches(cn)) {
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
											ApplicationProperties.getProperty("purdue.specreg.messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
											CONF_UNITIME);
									questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;	
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
												ApplicationProperties.getProperty("purdue.specreg.messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
												CONF_UNITIME);
										questionRestrictionsNotMet = true;
									}
								}
							}
					}
				}
			}
		}

		
		boolean questionFreeTime = false;
		for (int i = 0; i < request.getCourses().size(); i++) {
			CourseRequestInterface.Request r = request.getCourse(i);
			if (r.hasRequestedCourse() && r.getRequestedCourse(0).isFreeTime()) {
				boolean hasCourse = false;
				for (int j = i + 1; j < request.getCourses().size(); j++) {
					CourseRequestInterface.Request q = request.getCourse(j);
					if (q.hasRequestedCourse() && q.getRequestedCourse(0).hasCourseId()) {
						hasCourse = true;
					}
				}
				String free = "";
				for (FreeTime ft: r.getRequestedCourse(0).getFreeTime()) {
					if (!free.isEmpty()) free += ", ";
					free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
				}
				if (hasCourse)
					response.addMessage(0l, CONSTANTS.freePrefix() + free, "FREE-TIME",
						ApplicationProperties.getProperty("purdue.specreg.messages.freeTimeHighPriority", "High priority free time"),
						CONF_UNITIME);
				questionFreeTime = true;
			}
		}
		
		String creditError = null;
		Float maxCredit = original.getMaxCredit();
		if (maxCredit == null) maxCredit = Float.parseFloat(ApplicationProperties.getProperty("purdue.specreg.maxCreditDefault", "18"));
		
		request.setWaitListMode(details.getWaitListMode());
		if (maxCredit != null && request.getCredit(null) > maxCredit) {
			for (RequestedCourse rc: getOverCreditRequests(request, maxCredit)) {
				response.addMessage(rc.getCourseId(), rc.getCourseName(), "CREDIT",
						ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(null)))
						, CONF_UNITIME);
			}
			response.setCreditWarning(ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit",
					"Maximum of {max} credit hours exceeded.")
					.replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(null))));
			response.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_HIGH);
			creditError = ApplicationProperties.getProperty("purdue.specreg.messages.acr.maxCreditError",
					"Maximum of {max} credit hours exceeded.\nThe student may not be able to get a full schedule.")
					.replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(null)));
		}

		String minCreditLimit = ApplicationProperties.getProperty("purdue.specreg.minCreditCheck");
		float minCredit = 0;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasAdvisorCredit()) {
				minCredit += r.getAdvisorCreditMin();
			} else if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse())
					if (rc.hasCredit()) {
						minCredit += rc.getCreditMin(); break;
					}
			}
		}
		if (creditError == null && minCreditLimit != null && minCredit < Float.parseFloat(minCreditLimit) && (maxCredit == null || maxCredit > Float.parseFloat(minCreditLimit))) {
			creditError = ApplicationProperties.getProperty("purdue.specreg.messages.minCredit",
					"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit));
			response.setCreditWarning(
					ApplicationProperties.getProperty("purdue.specreg.messages.minCredit",
					"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit))
					);
			response.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_LOW);
		}
		

		if (response.getConfirms().contains(CONF_UNITIME)) {
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.unitimeProblemsFound", "The following issues have been detected:"), CONF_UNITIME, -1);
			response.addConfirmation("", CONF_UNITIME, 1);
		}
		int line = 2;
		if (creditError != null) {
			response.addConfirmation(creditError, CONF_UNITIME, line ++);
		}
		if (questionNoAlt)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.acr.noAlternatives", (line > 2 ? "\n" : "") +
					"One or more of the recommended courses have no alternatives provided. The student may not be able to get a full schedule."),
					CONF_UNITIME, line ++);
		if (questionTimeConflict)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.acr.timeConflicts", (line > 2 ? "\n" : "") +
					"Two or more single section courses are conflicting with each other. The student will likely not be able to get the conflicting course, so please provide an alternative course if possible."),
					CONF_UNITIME, line ++);
		if (questionInconStuPref)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.acr.inconsistentStudPref", (line > 2 ? "\n" : "") +
					"One or more courses are not available due to the selected preferences."),
					CONF_UNITIME, line ++);
		if (questionRestrictionsNotMet) {
			if (rule != null)
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.acr.ruleNotMet", (line > 2 ? "\n" : "") +
						"One or more of the recommended courses have no {rule} option at the moment. The student may not be able to get a full schedule."
						.replace("{rule}", rule.getRuleName())),
						CONF_UNITIME, 5);
			else if (onlineOnly)
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.acr.onlineOnlyNotMet", (line > 2 ? "\n" : "") +
					"One or more of the recommended courses have no online-only option at the moment. The student may not be able to get a full schedule."),
					CONF_UNITIME, 5);
			else
				response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.acr.residentialNotMet", (line > 2 ? "\n" : "") +
					"One or more of the recommended courses have no residential option at the moment. The student may not be able to get a full schedule."),
					CONF_UNITIME, 5);
		}
		if (questionFreeTime) {
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.acr.freeTimeRequested", (line > 2 ? "\n" : "") +
					"Free time requests will be considered as time blocks during the pre-registration process. When possible, classes should be avoided during free time. However, if a free time request is placed higher than a course, the course cannot be attended during free time and the student may not receive a full schedule."),
					CONF_UNITIME, 6);
		}
		
		if (line > 2)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.confirmation", "\nDo you want to proceed?"), CONF_UNITIME, 7);

		Set<Integer> conf = response.getConfirms();
		if (conf.contains(CONF_UNITIME)) {
		response.setConfirmation(CONF_UNITIME, ApplicationProperties.getProperty("purdue.specreg.confirm.acr.unitimeDialogName","Warning Confirmations"),
				(ApplicationProperties.getProperty("purdue.specreg.confirm.acr.unitimeYesButton", "Accept & Submit")),
				ApplicationProperties.getProperty("purdue.specreg.confirm.acr.unitimeNoButton", "Cancel Submit"),
				(ApplicationProperties.getProperty("purdue.specreg.confirm.acr.unitimeYesButtonTitle", "Accept the above warning(s) and submit the Advisor Course Recommendations")),
				ApplicationProperties.getProperty("purdue.specreg.confirm.acr.unitimeNoButtonTitle", "Go back to editing your Advisor Course Recommendations"));
		}
	}
}
