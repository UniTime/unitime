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
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.DefaultCourseRequestValidator;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.StudentHoldsCheckProvider;
import org.unitime.timetable.onlinesectioning.custom.StudentPinsProvider;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ApiMode;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckEligibilityResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.EligibilityProblem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ResponseStatus;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;

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
public class SimplifiedCourseRequestsValidationProvider extends DefaultCourseRequestValidator implements StudentHoldsCheckProvider, StudentPinsProvider {
	private static Log sLog = LogFactory.getLog(SimplifiedCourseRequestsValidationProvider.class);

	private Client iClient;
	private ExternalTermProvider iExternalTermProvider;

	public SimplifiedCourseRequestsValidationProvider() {
		iParameterPrefix = "purdue.specreg.";
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

	@Override
	protected boolean isWaitListNoAlts() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.specreg.waitListNoAlts", "false"));
	}

	@Override
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

	public boolean isDisableRegistrationWhenNotEligible() {
		return "true".equals(ApplicationProperties.getProperty("purdue.specreg.disableRegistrationWhenNotEligible", "true"));
	}

	protected boolean isValidationEnabled(org.unitime.timetable.model.Student student) {
		if (student == null) return false;
		StudentSectioningStatus status = student.getEffectiveStatus();
		return status == null || status.hasOption(StudentSectioningStatus.Option.reqval);
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

	@Override
	public String retriveStudentPin(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId student) throws SectioningException {
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

			if (eligibility.data != null && eligibility.data.PIN != null && !eligibility.data.PIN.isEmpty() && !"NA".equals(eligibility.data.PIN)) {
				return eligibility.data.PIN;
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
