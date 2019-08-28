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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.Client;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.Builder;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CourseRequestsValidationProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ApiMode;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckEligibilityResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.EligibilityProblem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ResponseStatus;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
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
public class SimplifiedCourseRequestsValidationProvider implements CourseRequestsValidationProvider {
	private static Logger sLog = Logger.getLogger(SimplifiedCourseRequestsValidationProvider.class);
	private static StudentSectioningMessages MESSAGES = Localization.create(StudentSectioningMessages.class);
	protected static Format<Number> sCreditFormat = Formats.getNumberFormat("0.##");
	
	private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	
	public SimplifiedCourseRequestsValidationProvider() {
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
		StudentSectioningStatus dbStatus = StudentSectioningStatus.getStatus(status, server.getAcademicSession().getUniqueId(), helper.getHibSession());
		while (dbStatus != null && dbStatus.isPast() && dbStatus.getFallBackStatus() != null) dbStatus = dbStatus.getFallBackStatus();
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
	
	protected String getBannerId(org.unitime.timetable.model.Student student) {
		String id = student.getExternalUniqueId();
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
	
	@Override
	public void checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, EligibilityCheck check, Student student) throws SectioningException {
		if (student == null || !check.hasFlag(EligibilityCheck.EligibilityFlag.CAN_REGISTER)) return;
		// Do not check eligibility when validation is disabled
		if (!isValidationEnabled(student)) return;
		
		if (isUseXE()) {
			ClientResource resource = null;
			try {
				String pin = helper.getPin();
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
					check.setFlag(EligibilityCheck.EligibilityFlag.CAN_REGISTER, false);
					check.setMessage(MESSAGES.exceptionFailedEligibilityCheck(error));
				}
				if (student.getUniqueId() != null && original != null && original.maxHours != null && original.maxHours > 0 && original.maxHours != student.getMaxCredit()) {
					Student dbStudent = StudentDAO.getInstance().get(student.getUniqueId(), helper.getHibSession());
					dbStudent.setMaxCredit(original.maxHours);
					helper.getHibSession().update(dbStudent);
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
						check.setFlag(EligibilityCheck.EligibilityFlag.CAN_REGISTER, false);
						check.setMessage(MESSAGES.exceptionFailedEligibilityCheck(m));
					}
				}
				
				if (student.getUniqueId() != null && eligibility.maxCredit != null && eligibility.maxCredit > 0 && eligibility.maxCredit != student.getMaxCredit()) {
					Student dbStudent = StudentDAO.getInstance().get(student.getUniqueId(), helper.getHibSession());
					dbStudent.setMaxCredit(eligibility.maxCredit);
					helper.getHibSession().update(dbStudent);
					helper.getHibSession().flush();
					if (!(server instanceof DatabaseServer)) {
						XStudent xs = server.getStudent(student.getUniqueId());
						if (xs != null) {
							xs.setMaxCredit(eligibility.maxCredit);
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
		
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() == 1) {
				RequestedCourse rc = r.getRequestedCourse(0);
				if (rc.getCourseId() != null && !rc.isReadOnly()) {
					request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "NO_ALT",
							ApplicationProperties.getProperty("purdue.specreg.messages.courseHasNoAlt", "No alternative course provided.").replace("{course}", rc.getCourseName()), ORD_UNITIME);
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
		if (minCreditLimit != null && minCredit > 0 && minCredit < Float.parseFloat(minCreditLimit) && (original.getMaxCredit() == null || original.getMaxCredit() > Float.parseFloat(minCreditLimit))) {
			request.setCreditWarning(
					ApplicationProperties.getProperty("purdue.specreg.messages.minCredit",
					"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit))
					);
			request.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_LOW);
		}
		
		Float maxCredit = original.getMaxCredit();
		if (maxCredit == null) maxCredit = Float.parseFloat(ApplicationProperties.getProperty("purdue.specreg.maxCreditDefault", "18"));

		if (maxCredit < request.getCredit()) {
			for (RequestedCourse rc: getOverCreditRequests(request, maxCredit)) {
				request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "CREDIT",
						ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit())), null,
						ORD_UNITIME);
			}
			request.setCreditWarning(ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit())));
			request.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_HIGH);
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
		if (original == null) throw new SectioningException(MESSAGES.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
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
		
		String creditError = null;
		Float maxCredit = original.getMaxCredit();
		if (maxCredit == null) maxCredit = Float.parseFloat(ApplicationProperties.getProperty("purdue.specreg.maxCreditDefault", "18"));
		
		if (maxCredit != null && request.getCredit() > maxCredit) {
			for (RequestedCourse rc: getOverCreditRequests(request, maxCredit)) {
				response.addMessage(rc.getCourseId(), rc.getCourseName(), "CREDIT",
						ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit()))
						, CONF_NONE);
			}
			response.setCreditWarning(ApplicationProperties.getProperty("purdue.specreg.messages.maxCredit",
					"Maximum of {max} credit hours exceeded.")
					.replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit())));
			response.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_HIGH);
			creditError = ApplicationProperties.getProperty("purdue.specreg.messages.maxCreditError",
					"Maximum of {max} credit hours exceeded.\nYou may not be able to get a full schedule.")
					.replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit()));
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
		if (creditError != null) {
			response.addConfirmation(creditError, CONF_UNITIME, 2);
		}
		if (questionNoAlt)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.noAlternatives", (creditError != null ? "\n" : "") +
					"One or more of the newly requested courses have no alternatives provided. You may not be able to get a full schedule because you did not provide an alternative course."),
					CONF_UNITIME, 3);
		if (creditError != null || questionNoAlt)
			response.addConfirmation(ApplicationProperties.getProperty("purdue.specreg.messages.confirmation", "\nDo you want to proceed?"), CONF_UNITIME, 5);

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
}
