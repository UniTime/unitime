/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.onlinesectioning.custom;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author Tomas Muller
 */
public class DefaultCourseDetailsProvider implements CourseDetailsProvider {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	private static final long serialVersionUID = 1L;

	@Override
	public String getDetails(AcademicSessionInfo session, String subject, String courseNbr) throws SectioningException {
		CourseOffering course = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), subject, courseNbr);
		if (course == null)
			return MSG.infoCourseDetailsNotAvailable(subject, courseNbr);

		try {
			Configuration cfg = new Configuration();
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

}
