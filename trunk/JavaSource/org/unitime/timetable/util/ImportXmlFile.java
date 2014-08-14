/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import java.io.File;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.DataExchangeHelper;

/**
 * Usage:
 * <code>
 * 		java -Xmx2g -Dtmtbl.custom.properties=~/Tomcat/conf/unitime.properties -cp timetable.jar \
 * 		     org.unitime.timetable.util.ImportXmlFile fileToImport.xml myExternalId
 * </code>
 * Where tmtbl.custom.properties points to UniTime custom properties (if there are any) and the fileToImport.xml is
 * the XML file to import.
 * The second parameter (external id of the timetabling manager under which the import is to be done) is optional.
 *
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class ImportXmlFile {

	public static void main(String[] args) {
		try {
			// Configure logging
	        org.apache.log4j.PropertyConfigurator.configure(ApplicationProperties.getProperties());
	        
	        // Configure hibernate
	        HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
	        
	        // Load an XML file
	        Document document = (new SAXReader()).read(new File(args[0]));
	        
	        // External id of the manager doing the import (can be null)
	        String managerId = (args.length >= 2 ? args[1] : null);
	        
	        // Log writer to print messages from the import (can be null)
	        DataExchangeHelper.LogWriter logger = new DataExchangeHelper.LogWriter() {
	        	@Override
	        	public void println(String message) {
	        		Logger.getLogger(ImportXmlFile.class).info(message);
	        	}
	        };
	        
	        // Import document
	        DataExchangeHelper.importDocument(document, managerId, logger);
	        
	        // Close hibernate
	        HibernateUtil.closeHibernate();
		} catch (Exception e) {
			Logger.getLogger(ImportXmlFile.class).error("Error: " +e.getMessage(), e);
		}
    }
	
}
