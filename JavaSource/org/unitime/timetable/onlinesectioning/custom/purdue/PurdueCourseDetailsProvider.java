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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.CourseUrlProvider;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class PurdueCourseDetailsProvider implements CourseDetailsProvider, CourseUrlProvider {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static Logger sLog = Logger.getLogger(PurdueCourseDetailsProvider.class);

	public static String sUrl = "https://selfservice.mypurdue.purdue.edu/prod/bzwsrch.p_catalog_detail?term=:year:term&subject=:subject&cnbr=:courseNbr&enhanced=Y";
	public static String sDummyUrl = "https://selfservice.mypurdue.purdue.edu/prod/bzwsrch.p_catalog_detail?term=201020&subject=AAE&cnbr=20300&enhanced=Y";
	public static String sContentRE = "(<table [ ]*class=\"[a-z]*\" summary=\"This table lists the course detail for the selected term.\" .*)<table [ ]*class=\"[a-z]*\" summary=\"This is table displays line separator at end of the page.\"";
	public static String[][] sRemoveRE = new String[][] {
		{"(?i)<a href=\"[^>]*\">", "<b>"},
		{"(?i)</a>", "</b>"},
		{"(?i)<span class=[\"]?fieldlabeltext[\"]?>", "<b>"},
		{"(?i)</span>", "</b>"},
		{"(?i) class=\"nttitle\" ", " class=\"unitime-MainTableHeader\" "},
		{"(?i) class=\"datadisplaytable\" ", " class=\"unitime-MainTable\" "},
	};
	
	private String getTerm(AcademicSessionInfo session) throws SectioningException {
		if (session.getTerm().toLowerCase().startsWith("spr")) return "20";
		if (session.getTerm().toLowerCase().startsWith("sum")) return "30";
		if (session.getTerm().toLowerCase().startsWith("fal")) return "10";
		throw new SectioningException(MSG.exceptionCustomCourseDetailsFailed("academic term "+session.getTerm()+" not known"));
	}
	
	private String getYear(AcademicSessionInfo session) throws SectioningException {
		if (session.getTerm().toLowerCase().startsWith("fal"))
			return String.valueOf(Integer.parseInt(session.getYear()) + 1);
		return session.getYear();
	}
	
	@Override
	public URL getCourseUrl(AcademicSessionInfo session, String subject, String courseNbr) throws SectioningException {
		try {
			if (courseNbr.length() > 5) courseNbr = courseNbr.substring(0, 5);
			return new URL(sUrl
				.replace(":year", getYear(session))
				.replace(":term", getTerm(session))
				.replace(":initiative", session.getCampus())
				.replace(":subject", URLEncoder.encode(subject, "utf-8"))
				.replace(":courseNbr", courseNbr));
		} catch (MalformedURLException e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionCustomCourseDetailsFailed("course detail url is wrong"), e);
		} catch (UnsupportedEncodingException e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionCustomCourseDetailsFailed("course detail url is wrong"), e);
		}
	}
	@Override
	public String getDetails(AcademicSessionInfo session, String subject, String courseNbr) throws SectioningException {
		return getDetails(getCourseUrl(session, subject, courseNbr));
	}
	
	protected String getDetails(URL courseUrl) throws SectioningException {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(courseUrl.openStream(), "utf-8"));
			StringBuffer content = new StringBuffer();
			String line;
			while ((line = in.readLine()) != null)
				content.append(line);
			in.close();
			
			Pattern pattern = Pattern.compile(sContentRE, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
			Matcher match = pattern.matcher(content);
			if (!match.find()) throw new SectioningException(MSG.exceptionCustomCourseDetailsFailed("unable to parse <a href='"+courseUrl+"'>course detail page</a>"));
			String table = match.group(1);
			
			for (int i=0; i<sRemoveRE.length; i++)
				table = table.replaceAll(sRemoveRE[i][0], sRemoveRE[i][1]);

			return table;
		} catch (IOException e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionCustomCourseDetailsFailed("unable to read <a href='"+courseUrl+"'>course detail page</a>"));
		}
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(new PurdueCourseDetailsProvider().getDetails(new URL(sDummyUrl)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
