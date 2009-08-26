/**
 * 
 */
package org.unitime.timetable.util;

import java.util.Properties;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.ExamExport;
import org.unitime.timetable.model.Session;

/**
 * @author says
 *
 */
public class ExportExamRequests {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
            ToolBox.configureLogging();
    		Properties properties = new Properties();
    		properties.put("connection.url", ApplicationProperties
    				.getProperty("connection.url"));
    		properties.put("connection.username", ApplicationProperties
    				.getProperty("connection.username"));
    		properties.put("connection.password", ApplicationProperties
    				.getProperty("connection.password"));
    		HibernateUtil.configureHibernate(properties);

     		ExamExport erae = new ExamExport(args[3], "true".equalsIgnoreCase(args[5]));
    		Session session = Session.getSessionUsingInitiativeYearTerm(args[0],
    				args[1], args[2]);

	        String fileName = args[4];
         	Debug.info("filename = " + fileName);
     		
    		erae.saveXml(fileName, session, new Properties());
                         
    }
	
}
