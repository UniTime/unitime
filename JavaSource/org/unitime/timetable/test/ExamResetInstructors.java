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

import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
public class ExamResetInstructors {
	private static Log sLog = LogFactory.getLog(ExamResetInstructors.class);
	
	public static void doUpdate(Long sessionId, Long examType, boolean override, boolean classOnly, org.hibernate.Session hibSession) {
        for (Exam exam: new TreeSet<Exam>(Exam.findAll(sessionId, examType))) {
            if (!override && !exam.getInstructors().isEmpty()) continue;
            sLog.info("Updating "+exam.getLabel());
            
            for (Iterator i = exam.getInstructors().iterator(); i.hasNext();) {
                DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
                instructor.getExams().remove(exam);
                i.remove();
            }
            
            for (ExamOwner owner: exam.getOwners()) {
            	sLog.info("  owner " + owner.getLabel());
            	Object object = owner.getOwnerObject();
            	if (object instanceof Class_) {
            		for (ClassInstructor instructor: ((Class_)object).getClassInstructors())
            			if (instructor.getPercentShare() >= 100)
            				exam.getInstructors().add(instructor.getInstructor());
            	} else if (object instanceof InstrOfferingConfig && !classOnly) {
                	SchedulingSubpart top = null;
                	for (SchedulingSubpart s: ((InstrOfferingConfig)object).getSchedulingSubparts())
                		if (s.getParentSubpart() == null && (top == null || s.getItype().compareTo(top.getItype()) < 0 || (s.getItype().compareTo(top.getItype()) == 0 && s.getClasses().size() < top.getClasses().size())))
                			top = s;
                	if (top != null)
                		for (Class_ clazz: top.getClasses()) {
                			sLog.info("  using " + clazz.getClassLabel());
                			for (ClassInstructor instructor: clazz.getClassInstructors())
                    			if (instructor.getPercentShare() >= 100)
                    				exam.getInstructors().add(instructor.getInstructor());
                		}
            	} else if (!classOnly) {
            		for (InstrOfferingConfig config: owner.getCourse().getInstructionalOffering().getInstrOfferingConfigs()) {
            			SchedulingSubpart top = null;
                    	for (SchedulingSubpart s: config.getSchedulingSubparts())
                    		if (s.getParentSubpart() == null && (top == null || s.getItype().compareTo(top.getItype()) < 0 || (s.getItype().compareTo(top.getItype()) == 0 && s.getClasses().size() < top.getClasses().size())))
                    			top = s;
                    	if (top != null)
                    		for (Class_ clazz: top.getClasses()) {
                    			sLog.info("  using " + clazz.getClassLabel());
                    			for (ClassInstructor instructor: clazz.getClassInstructors())
                        			if (instructor.getPercentShare() >= 100)
                        				exam.getInstructors().add(instructor.getInstructor());
                    		}
            		}
            	}
            }
            
            if (exam.getInstructors().isEmpty())
                sLog.info("    no instructors");
            else
            	sLog.info("    instructors " + new TreeSet<DepartmentalInstructor>(exam.getInstructors()));
            
            for (DepartmentalInstructor instructor: exam.getInstructors()) {
            	instructor.getExams().add(exam);
            	hibSession.saveOrUpdate(instructor);
            }
            
            hibSession.saveOrUpdate(exam);
        }
        hibSession.flush();
	}
	
	public static void main(String args[]) {
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
            
            Session session = Session.getSessionUsingInitiativeYearTerm(
                    ApplicationProperties.getProperty("initiative", "PWL"),
                    ApplicationProperties.getProperty("year","2013"),
                    ApplicationProperties.getProperty("term","Fall")
                    );
            if (session==null) {
                sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                System.exit(0);
            } else {
                sLog.info("Session: "+session);
            }
            
            ExamType examType = ExamType.findByReference(ApplicationProperties.getProperty("type", "final"));
            boolean override = "true".equals(ApplicationProperties.getProperty("override", "true"));
            boolean classOnly = "true".equals(ApplicationProperties.getProperty("classOnly", "false"));
            
            doUpdate(session.getUniqueId(), examType.getUniqueId(), override, classOnly, new _RootDAO().getSession());

            HibernateUtil.closeHibernate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
