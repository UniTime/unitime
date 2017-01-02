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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.dao.CurriculumCourseDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.SimpleSequence;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateSequenceModel;

/**
 * @author Maciej Zygmunt
 */
public class AGHCourseDetailsProvider implements CourseDetailsProvider, CourseUrlProvider {
	private static Logger sLog = Logger.getLogger(AGHCourseDetailsProvider.class);
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	private static final long serialVersionUID = 1L;

	
	@Override
	public URL getCourseUrl(AcademicSessionInfo session, String subject, String courseNbr) throws SectioningException {
		CourseOffering course = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), subject,
				courseNbr);
		if (course == null) {
			throw new SectioningException("Course not found: " + subject + " " + courseNbr);
		}
		try {
			String years = findYears(session, subject, courseNbr);
			String url = ApplicationProperty.CustomizationDefaultCourseUrl.value();
			if (url == null || url.isEmpty())
				return null;
			return new URL(url.replace(":years", URLEncoder.encode(years, "utf-8"))
					.replace(":term", URLEncoder.encode(session.getTerm(), "utf-8"))
					.replace(":courseNbr", URLEncoder.encode(course.getExternalUniqueId(), "utf-8")));
		} catch (Exception e) {
			throw new SectioningException("Failed to get course URL: " + e.getMessage(), e);
		}
	}

	private String findClassificationBySessionSubjAreaAbbvCourseNbr(Long sessionId, String subject, String courseNbr) {
		/*
		select cu from CurriculumCourse as cu
		where cu.course.courseNbr=101
		and cu.course.subjectAreaAbbv='BAND'
		and cu.course.instructionalOffering.session.uniqueId=231379
		*/
		return (String)CurriculumCourseDAO.getInstance().createNewSession().createQuery(
				"select max(cu.classification.academicClassification.code) " +
				"from CurriculumCourse as cu " +
				"where cu.course.subjectAreaAbbv = :subjArea " +
				"and cu.course.courseNbr = :crsNbr " +
				"and cu.course.instructionalOffering.session.uniqueId = :acadSessionId")
				.setString("crsNbr", courseNbr)
				.setString("subjArea", subject)
				.setLong("acadSessionId", sessionId)
				.setMaxResults(1).uniqueResult();
	}

	@Override
	public String getDetails(AcademicSessionInfo session, String subject, String courseNbr) throws SectioningException {
		CourseOffering course = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), subject,
				courseNbr);
		if (ApplicationProperty.CustomizationDefaultCourseDetailsDownload.isTrue()) {
			URL url = getApiUrl(session, subject, courseNbr);
			URL courseUrl=getCourseUrl(session, subject, courseNbr);
			if (url != null)
				return downloadDetails(url,courseUrl,course);
		}
		
		if (course == null)
			return MSG.infoCourseDetailsNotAvailable(subject, courseNbr);

		try {

			Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
			cfg.setClassForTemplateLoading(AGHCourseDetailsProvider.class, "");
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
			s.flush();
			s.close();

			return s.toString();
		} catch (TemplateException e) {
			throw new SectioningException(MSG.failedLoadCourseDetails(e.getMessage()));
		} catch (IOException e) {
			throw new SectioningException(MSG.failedLoadCourseDetails(e.getMessage()));
		}
	}

	private URL getApiUrl(AcademicSessionInfo session, String subject, String courseNbr) {

		CourseOffering course = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), subject,
				courseNbr);
		if (course == null) {
			throw new SectioningException("Course not found: " + subject + " " + courseNbr);
		}
		try {
			String url = ApplicationProperties.getConfigProperties().getProperty("unitime.custom.default.course_api_url",
					"http://syllabuskrk.agh.edu.pl/api/:years/modules/:courseNbr");
			if (url == null || url.isEmpty())
				return null;
			String years = findYears(session, subject, courseNbr);
			return new URL(url.replace(":years", URLEncoder.encode(years, "utf-8"))
					.replace(":term", URLEncoder.encode(session.getTerm(), "utf-8"))
					.replace(":courseNbr", URLEncoder.encode(course.getExternalUniqueId(), "utf-8")));
		} catch (Exception e) {
			throw new SectioningException("Failed to get course URL: " + e.getMessage(), e);
		}

	}

	private String findYears(AcademicSessionInfo session, String subject, String courseNbr) {
		String classificationSt=findClassificationBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), subject, courseNbr);
		Integer classification=new Integer( classificationSt.substring(classificationSt.length()-1));
		String years=syllabusLink(new Integer(session.getYear()), session.getTerm(), classification);
		return years;
	}

	public String syllabusLink(Integer year, String term, Integer classification) {
		Integer yearShift, yearLink, sessionShift;
		String syllabusLinkRw = "current_annual"; // newest syllabus
		if (term.toLowerCase().endsWith("zimowy")) sessionShift=0;
		if (term.toLowerCase().endsWith("letni")) sessionShift=1;
		else sessionShift=0;
		/*		
		switch (session) {
		case "Semestr zimowy":
			sessionShift = 0;
			break;
		case "Semestr letni":
			sessionShift = 1;
			break;
		default:
			sessionShift = 0;
			break;

		}
		*/
		// div 2 (two semesters per year)
		yearShift = (classification - sessionShift) / 2;
		// current year minus shift
		yearLink = year - yearShift;
		// link = beginning + next year
		syllabusLinkRw = yearLink.toString() + "-" + new Integer(yearLink+1).toString();

		return syllabusLinkRw;
	}
	private String downloadDetails(URL url, URL courseUrl, CourseOffering utCourse) throws SectioningException {
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("Accept", "application/json");

			Gson gson = new Gson();
			InputStream is = con.getInputStream();
			InputStreamReader reader = new InputStreamReader(is);
			JsonObject syllabusCourse =gson.fromJson(reader, JsonObject.class);
			
			
			JsonObject owner = syllabusCourse.get("owner").getAsJsonObject();
			JsonArray modules = syllabusCourse.get("module_activities").getAsJsonArray();
			JsonArray instructors = syllabusCourse.get("lecturers").getAsJsonArray();
			ArrayList<JsonObject> cList = new ArrayList<JsonObject>();
			for (int i = 0; i < modules.size(); i++) {
				cList.add(modules.get(i).getAsJsonObject());
			}
			
			ArrayList<JsonObject> iList = new ArrayList<JsonObject>();
			for (int i = 0; i < instructors.size(); i++) {
				iList.add(instructors.get(i).getAsJsonObject());
			}
			JsonArray study_plans = syllabusCourse.get("study_plans").getAsJsonArray();
			
			ArrayList<JsonObject> pList = new ArrayList<JsonObject>();
			for (int i = 0; i < study_plans.size(); i++) {
				pList.add(study_plans.get(i).getAsJsonObject());
			}
			
			try {
				DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_23);
				TemplateSequenceModel classList = new SimpleSequence(cList, builder.build());
				TemplateSequenceModel instructorsList = new SimpleSequence(iList, builder.build());
				TemplateSequenceModel plansList = new SimpleSequence(pList, builder.build());
				
				Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
				cfg.setClassForTemplateLoading(AGHCourseDetailsProvider.class, "");
				cfg.setLocale(Localization.getJavaLocale());
				cfg.setOutputEncoding("utf-8");
				Template template = cfg.getTemplate("agh_details.ftl");
				Map<String, Object> input = new HashMap<String, Object>();
				input.put("msg", MSG);
				input.put("const", CONST);
				input.put("url", courseUrl.toString());
				input.put("syllabusCourse", syllabusCourse);
				input.put("utCourse", utCourse);
				input.put("owner", owner);
				input.put("classList", classList);
				input.put("instructorsList", instructorsList);
				input.put("plansList", plansList);
					
				StringWriter s = new StringWriter();
				template.process(input, new PrintWriter(s));
				s.flush();
				s.close();
				return s.toString();
			} catch (TemplateException e) {
				throw new SectioningException(MSG.failedLoadCourseDetails(e.getMessage()));
			} catch (IOException e) {
				throw new SectioningException(MSG.failedLoadCourseDetails(e.getMessage()));
			}

		} catch (IOException e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionCustomCourseDetailsFailed(
					"unable to read <a href='" + url + "'>course detail page</a>"), e);

		} catch (Exception e) {
			e.printStackTrace();
			throw new SectioningException(MSG.exceptionCustomCourseDetailsFailed(
					"unable to read <a href='" + url + "'>course detail page</a>"), e);

		}
	}

	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();
			Debug.info(" - Initializing Hibernate ... ");
			_RootDAO.initialize();

			ApplicationProperties.getConfigProperties().setProperty(
					ApplicationProperty.CustomizationDefaultCourseUrl.key(),
					"http://syllabuskrk.agh.edu.pl/:years/pl/magnesite/modules/:courseNbr");
			ApplicationProperties.getConfigProperties().setProperty("unitime.custom.default.course_api_url",
					"http://syllabuskrk.agh.edu.pl/api/:years/modules/:courseNbr");
			
			
			ApplicationProperties.getDefaultProperties()
					.setProperty(ApplicationProperty.CustomizationDefaultCourseDetailsDownload.key(), "true");

			System.out.println("URL:" + new AGHCourseDetailsProvider()
					.getCourseUrl(new AcademicSessionInfo(231379l, "2015", "Semestr zimowy", "AGH"), "BAND", "101"));

			System.out.println("Details:\n" + new AGHCourseDetailsProvider()
					.getDetails(new AcademicSessionInfo(231379l, "2015", "Semestr zimowy", "AGH"), "BAND", "101"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
