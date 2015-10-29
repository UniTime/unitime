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
package org.unitime.timetable.test;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
public class AssignFirstAvailableTimePattern {
    protected static Logger sLog = Logger.getLogger(AssignFirstAvailableTimePattern.class);

	public static void main(String[] args) {
        try {
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "DEBUG, A1");
            props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
            props.setProperty("log4j.logger.org.hibernate","INFO");
            props.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            props.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            props.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            props.setProperty("log4j.logger.net","INFO");
            PropertyConfigurator.configure(props);
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

            org.hibernate.Session hibSession = new _RootDAO().getSession();
            
			Session session = Session.getSessionUsingInitiativeYearTerm(
                    ApplicationProperties.getProperty("initiative", "MUNI FI"),
                    ApplicationProperties.getProperty("year","2010"),
                    ApplicationProperties.getProperty("term","Podzim")
                    );
            
            if (session==null) {
                sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                System.exit(0);
            } else {
                sLog.info("Session: "+session);
            }
            
            for (SchedulingSubpart s: (List<SchedulingSubpart>)hibSession.createQuery(
            		"select distinct s from SchedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
            		"co.subjectArea.department.session.uniqueId = :sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	if (s.getTimePreferences().isEmpty()) {
            		List<TimePattern> patterns = TimePattern.findApplicable(session, false, false, false, s.getMinutesPerWk(), s.effectiveDatePattern(), s.getInstrOfferingConfig().getDurationModel(), null);
            		if (patterns.isEmpty()) continue;
            		TimePattern pattern = patterns.get(0);
            		TimePref tp = new TimePref();
            		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
            		tp.setTimePatternModel(pattern.getTimePatternModel());
            		tp.setOwner(s);
            		hibSession.save(tp);
            		sLog.info(s.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getCourseName() + " " + s.getItypeDesc() + " := " + pattern.getName());
            	}
            }
            
            hibSession.flush();
            
            sLog.info("All done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
