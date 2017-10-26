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
	        
	        // Export an XML file
	        Document document = DataExchangeHelper.exportDocument(args[1], session, ApplicationProperties.getProperties(), null);
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
