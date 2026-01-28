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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.ToolBox;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class Banner9CourseDetailsProvider implements CourseDetailsProvider {
	private static final long serialVersionUID = 3824728965799479063L;
	private static Log sLog = LogFactory.getLog(Banner9CourseDetailsProvider.class);
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static CourseMessages CMSG = Localization.create(CourseMessages.class);
	private static GwtMessages GMSG = Localization.create(GwtMessages.class);
	private static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	
	public Banner9CourseDetailsProvider() {
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
		Context cx = new Context();
		cx.getParameters().add("readTimeout", getReadTimeout());
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
	
	protected String getReadTimeout() {
		return ApplicationProperties.getProperty("banner.catalog.readTimeout", "60000");
	}
	
	protected String getApiKey() {
		return ApplicationProperties.getProperty("banner.catalog.apiKey");
	}
	
	protected String getCatalogUrl() {
		return ApplicationProperties.getProperty("banner.catalog.url");
	}
	
	protected String getAccessToken() throws IOException, ResourceException {
		ClientResource resource = null;
		try {
			resource = new ClientResource("https://integrate.elluciancloud.com/auth");
			resource.setNext(iClient);
			ChallengeResponse challenge = new ChallengeResponse(ChallengeScheme.HTTP_OAUTH_BEARER);
			challenge.setRawValue(getApiKey());
			resource.setChallengeResponse(challenge);
			return resource.post(null).getText();
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}
	
	protected String downloadSection(String url) throws IOException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "utf-8"));
			StringBuffer buffer = new StringBuffer();
			String line;
			while ((line = in.readLine()) != null) {
				if (!buffer.isEmpty()) buffer.append("\n");
				buffer.append(line);
			}
			in.close();
			return buffer.toString();
		} finally {
			if (in != null) in.close();
		}
	}
	
	protected Map<?, ?> executeAPI(String api, String accessToken, Map<String, String> params) throws IOException, ResourceException {
		ClientResource resource = null;
		try {
			resource = new ClientResource(api);
			resource.setNext(iClient);
			ChallengeResponse challenge = new ChallengeResponse(ChallengeScheme.HTTP_OAUTH_BEARER);
			challenge.setRawValue(accessToken);
			resource.setChallengeResponse(challenge);
			resource.post(new GsonRepresentation<Map<String, String>>(params), MediaType.APPLICATION_JSON);
			List<Map<?,?>> ret = new GsonRepresentation<List<Map<?,?>>>(resource.getResponseEntity(), ArrayList.class).getObject();
			if (ret == null || ret.isEmpty()) return new HashMap();
			return ret.get(0);
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}
	
	@Override
	public String getDetails(AcademicSessionInfo session, String subject, String courseNbr) throws SectioningException {
		try {
			String accessToken = getAccessToken();
			
			final Map<String, String> params = new HashMap<String, String>();
			params.put("subject", iExternalTermProvider.getExternalSubject(session, subject, courseNbr));
			params.put("courseNumber", iExternalTermProvider.getExternalCourseNumber(session, subject, courseNbr));
			params.put("queryTerm", iExternalTermProvider.getExternalTerm(session));
			
			Map<String, String> params2 = new HashMap<String, String>();
			params2.put("scacrseSubjCode", iExternalTermProvider.getExternalSubject(session, subject, courseNbr));
			params2.put("scacrseCrseNumb", iExternalTermProvider.getExternalCourseNumber(session, subject, courseNbr));
			params2.put("scacrseTermCodeEff", iExternalTermProvider.getExternalTerm(session));

			Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
			cfg.setClassForTemplateLoading(Banner9CourseDetailsProvider.class, "");
			cfg.setLocale(Localization.getJavaLocale());
			cfg.setOutputEncoding("utf-8");
			Template template = cfg.getTemplate("course-details.ftl");
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("msg", MSG);
			input.put("const", CONST);
			input.put("cmsg", CMSG);
			input.put("gmsg", GMSG);
			input.put("session", session);
			Map<?, ?> base = executeAPI("https://integrate.elluciancloud.com/qapi/catalog-course-bases", accessToken, params);
			if (base == null || base.isEmpty())
				return MSG.catalogCourseNotInCatalog(subject, courseNbr);
			input.put("base", base);
			input.put("details", executeAPI("https://integrate.elluciancloud.com/qapi/catalog-course-additional-details", accessToken, params));
			input.put("prerequisites", executeAPI("https://integrate.elluciancloud.com/qapi/catalog-course-requisites-and-equivalents", accessToken, params));
			input.put("restrictions", executeAPI("https://integrate.elluciancloud.com/qapi/catalog-course-restrictions", accessToken, params));
			input.put("descriptors", executeAPI("https://integrate.elluciancloud.com/qapi/catalog-course-descriptors", accessToken, params));
			input.put("fees", executeAPI("https://integrate.elluciancloud.com/qapi/course-detail-information-fee-codes", accessToken, params2));
			input.put("lookup", new Lookup() {
				@Override
				public String getPrereqsFromCatalog() {
					try {
						String base = getCatalogUrl();
						if (base == null || base.isEmpty()) return "";
						String p = "term=" + URLEncoder.encode(params.get("queryTerm"), "utf-8") +
								"&subjectCode=" + URLEncoder.encode(params.get("subject"), "utf-8") +
								"&courseNumber=" + URLEncoder.encode(params.get("courseNumber"), "utf-8");
						return downloadSection(base + "/getPrerequisites?" + p);
					} catch (Exception e) {
						sLog.info("Failed to load catalog section: " + e.getMessage(), e);
						return "Failed to load prerequisites: " + e.getMessage();
					}
				}
			});
			String disclaimer = ApplicationProperties.getProperty("banner.catalog.disclaimer", null);
			if (disclaimer != null)
				input.put("disclaimer", disclaimer); 
			
			StringWriter s = new StringWriter();
			template.process(input, new PrintWriter(s));
			s.flush(); s.close();

			return s.toString();
		} catch (SectioningException e) {
			sLog.info(e.getMessage(), e);
			return MSG.exceptionCustomCourseDetailsFailed(e.getMessage());
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			return MSG.exceptionCustomCourseDetailsFailed(e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		try {
			ToolBox.configureLogging();
			Banner9CourseDetailsProvider p = new Banner9CourseDetailsProvider();
			System.out.println(p.getDetails(new AcademicSessionInfo(-1l, "2026", "Spring", "PWL"), "ECON", "39000"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static interface Lookup {
		public String getPrereqsFromCatalog();
	}

}
