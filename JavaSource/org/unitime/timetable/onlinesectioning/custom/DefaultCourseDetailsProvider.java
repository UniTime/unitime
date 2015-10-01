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
package org.unitime.timetable.onlinesectioning.custom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.purdue.BannerTermProvider;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author Tomas Muller
 */
public class DefaultCourseDetailsProvider implements CourseDetailsProvider, CourseUrlProvider {
	private static Logger sLog = Logger.getLogger(DefaultCourseDetailsProvider.class);
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	private static final long serialVersionUID = 1L;
	private transient ExternalTermProvider iExternalTermProvider = null;
	
	protected String replaceExternal(String url, AcademicSessionInfo session, String subject, String courseNbr) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, UnsupportedEncodingException {
		if (iExternalTermProvider == null) {
			String clazz = ApplicationProperty.CustomizationExternalTerm.value();
			if (clazz == null || clazz.isEmpty()) return url;
			iExternalTermProvider = (ExternalTermProvider)Class.forName(clazz).getConstructor().newInstance();
		}
		return url
				.replace(":xterm", URLEncoder.encode(iExternalTermProvider.getExternalTerm(session), "utf-8"))
				.replace(":xcampus", URLEncoder.encode(iExternalTermProvider.getExternalTerm(session), "utf-8"))
				.replace(":xsubject", URLEncoder.encode(iExternalTermProvider.getExternalSubject(session, subject, courseNbr), "utf-8"))
				.replace(":xcourseNbr", URLEncoder.encode(iExternalTermProvider.getExternalCourseNumber(session, subject, courseNbr), "utf-8"));
	}
	
	protected String getExternalSubject(AcademicSessionInfo session, String subjectArea, String courseNumber) {
		return (iExternalTermProvider == null ? subjectArea : iExternalTermProvider.getExternalSubject(session, subjectArea, courseNumber));
	}

	protected String getExternalCourseNbr(AcademicSessionInfo session, String subjectArea, String courseNumber) {
		return (iExternalTermProvider == null ? courseNumber : iExternalTermProvider.getExternalCourseNumber(session, subjectArea, courseNumber));
	}

	
	@Override
	public URL getCourseUrl(AcademicSessionInfo session, String subject, String courseNbr) throws SectioningException {
		try {
			String url = ApplicationProperty.CustomizationDefaultCourseUrl.value();
			if (url == null || url.isEmpty()) return null;
			return new URL(replaceExternal(
					url.replace(":year", URLEncoder.encode(session.getYear(), "utf-8"))
					   .replace(":term", URLEncoder.encode(session.getTerm(), "utf-8"))
					   .replace(":campus", URLEncoder.encode(session.getCampus(), "utf-8"))
					   .replace(":subject", URLEncoder.encode(subject, "utf-8"))
					   .replace(":courseNbr", URLEncoder.encode(courseNbr, "utf-8")),
					session, subject, courseNbr));
		} catch (Exception e) {
			throw new SectioningException("Failed to get course URL: " + e.getMessage(), e);
		}
	}


	@Override
	public String getDetails(AcademicSessionInfo session, String subject, String courseNbr) throws SectioningException {
		if (ApplicationProperty.CustomizationDefaultCourseDetailsDownload.isTrue()) {
			URL url = getCourseUrl(session, subject, courseNbr);
			if (url != null) return downloadDetails(url);
		}
		CourseOffering course = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), subject, courseNbr);
		if (course == null)
			return MSG.infoCourseDetailsNotAvailable(subject, courseNbr);

		try {
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
			cfg.setClassForTemplateLoading(DefaultCourseDetailsProvider.class, "");
			cfg.setLocale(Localization.getJavaLocale());
			cfg.setOutputEncoding("utf-8");
			Template template = cfg.getTemplate("details.ftl");
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("msg", MSG);
			input.put("const", CONST);
			input.put("session", session);
			input.put("course", course);
			
			StringWriter s = new StringWriter();
			template.process(input, new PrintWriter(s));
			s.flush(); s.close();

			return s.toString();
		} catch (TemplateException e) {
			throw new SectioningException(MSG.failedLoadCourseDetails(e.getMessage()));
		} catch (IOException e) {
			throw new SectioningException(MSG.failedLoadCourseDetails(e.getMessage()));
		}
	}
	
	protected String downloadDetails(URL url) throws SectioningException {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
			StringBuffer content = new StringBuffer();
			String line;
			while ((line = in.readLine()) != null)
				content.append(line);
			in.close();
			
			Pattern pattern = Pattern.compile(ApplicationProperty.CustomizationDefaultCourseDetailsContent.value());
			Matcher match = pattern.matcher(content);
			if (!match.find()) 
				return "<div class='unitime-ErrorMessage'>" + MSG.exceptionCustomCourseDetailsFailed("unable to parse <a href='"+url+"'>course detail page</a>") + "</div>";
			String table = match.group(1);
			
			String modif = ApplicationProperty.CustomizationDefaultCourseDetailsModifiers.value();
			if (modif != null && !modif.isEmpty()) {
				String[] modifiers = modif.split("\\r?\\n");
				for (int i = 0; i + 1 < modifiers.length; i += 2) {
					table = table.replaceAll(modifiers[i], modifiers[i + 1]);
				}
			}
			return table;
		} catch (IOException e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionCustomCourseDetailsFailed("unable to read <a href='"+url+"'>course detail page</a>"), e);
		}
	}
	
	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();

			ApplicationProperties.getDefaultProperties().setProperty(
					ApplicationProperty.CustomizationExternalTerm.key(),
					BannerTermProvider.class.getName());
			ApplicationProperties.getDefaultProperties().setProperty(
					ApplicationProperty.CustomizationDefaultCourseUrl.key(),
					"https://selfservice.mypurdue.purdue.edu/prod/bzwsrch.p_catalog_detail?term=:xterm&subject=:xsubject&cnbr=:xcourseNbr&enhanced=Y");
			ApplicationProperties.getDefaultProperties().setProperty(
					ApplicationProperty.CustomizationDefaultCourseDetailsDownload.key(),
					"true");
			ApplicationProperties.getDefaultProperties().setProperty(
					ApplicationProperty.CustomizationDefaultCourseDetailsContent.key(),
					"(?idm)(<table [ ]*class=\"[a-z]*\" summary=\"This table lists the course detail for the selected term.\" .*)<table [ ]*class=\"[a-z]*\" summary=\"This is table displays line separator at end of the page.\"");
			ApplicationProperties.getDefaultProperties().setProperty(
					ApplicationProperty.CustomizationDefaultCourseDetailsModifiers.key(),
					"(?i)<a href=\"[^>]*\">\n<b>\n"+
					"(?i)</a>\n</b>\n"+
					"(?i)<span class=[\"]?fieldlabeltext[\"]?>\n<b>\n"+
					"(?i)</span>\n</b>\n"+
					"(?i) class=\"nttitle\" \n class=\"unitime-MainTableHeader\" \n"+
					"(?i) class=\"datadisplaytable\" \n class=\"unitime-MainTable\" ");
			
			System.out.println(
					"URL:" + new DefaultCourseDetailsProvider().getCourseUrl(new AcademicSessionInfo(-1l, "2010", "Spring", "PWL"), "AAE", "20300A")
					);
			
			System.out.println(
					"Details:\n" +
					new DefaultCourseDetailsProvider().getDetails(new AcademicSessionInfo(-1l, "2010", "Spring", "PWL"), "AAE", "20300A")
					);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
