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
package org.unitime.timetable.util;

import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;

/**
 * Usage:
 * <code>
 * 		java -Xmx2g -Dtmtbl.custom.properties=~/Tomcat/conf/unitime.properties -cp timetable.jar \
 * 		     org.unitime.timetable.util.ExportXmlFile session type output.xml
 * </code>
 * Where tmtbl.custom.properties points to UniTime custom properties (if there are any), session is the name of 
 * the academic session (term + year + initiative, e.g., Fall2013PWL), type is the root element of the export
 * (e.g., offerings), and output.xml is the output file.
 *
 * @author Tomas Muller
 */
public class ExportXmlFile {

	public static void main(String[] args) {
		try {
			// Configure logging
	        org.apache.log4j.BasicConfigurator.configure();
	        
	        // Configure hibernate
	        HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
	        
	        // Load academic session
	        Session session = (Session)SessionDAO.getInstance().getSession().createQuery(
	        		"from Session s where s.academicTerm || s.academicYear || s.academicInitiative = :session")
	        		.setString("session", args[0]).uniqueResult();
	        if (session == null)
	        	throw new Exception("Session " + args[0] + " not found.");
	        
	        // Log writer to print messages from the export (can be null)
	        DataExchangeHelper.LogWriter logger = new DataExchangeHelper.LogWriter() {
	        	@Override
	        	public void println(String message) {
	        		Logger.getLogger(ImportXmlFile.class).info(message);
	        	}
	        };

	        // Export an XML file
	        Document document = DataExchangeHelper.exportDocument(args[1], session, ApplicationProperties.getProperties(), logger);
	        if (document==null)
	        	throw new Exception("No XML document has been created.");
	        
	        FileOutputStream fos = new FileOutputStream(args[2]);
	        try {
	        	(new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(document);
	        	fos.flush();
	        } finally {
	        	fos.close();
            }
	        
		} catch (Exception e) {
			Logger.getLogger(ImportXmlFile.class).error("Error: " +e.getMessage(), e);
		} finally {
	        // Close hibernate
	        HibernateUtil.closeHibernate();
		}
    }
	
}
