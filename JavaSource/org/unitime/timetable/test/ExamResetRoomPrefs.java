package org.unitime.timetable.test;

import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;

public class ExamResetRoomPrefs {
    private static Log sLog = LogFactory.getLog(ExamResetRoomPrefs.class);

    public static void doUpdate(Long sessionId, Integer examType, org.hibernate.Session hibSession) {
        for (Iterator i=new TreeSet(Exam.findAll(sessionId, examType)).iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            sLog.info("Updating "+exam.getLabel());
            for (Iterator j=exam.getPreferences().iterator();j.hasNext();) {
                Preference p = (Preference)j.next();
                if (p instanceof RoomPref) { j.remove(); }
            }
            exam.generateDefaultPreferences(true);
            hibSession.saveOrUpdate(exam);
        }
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
                    ApplicationProperties.getProperty("year","2008"),
                    ApplicationProperties.getProperty("term","Fall")
                    );
            if (session==null) {
                sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                System.exit(0);
            } else {
                sLog.info("Session: "+session);
            }
            
            int examType = (ApplicationProperties.getProperty("type","final").equalsIgnoreCase("final")?Exam.sExamTypeFinal:Exam.sExamTypeMidterm);
            
            org.hibernate.Session hibSession = new _RootDAO().getSession();
            Transaction tx = null;
            try {
                tx = hibSession.beginTransaction();
                
                doUpdate(session.getUniqueId(), examType, hibSession);
                
                tx.commit();
            } catch (Exception e) {
                if (tx!=null) tx.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
