/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.dataexchange.DataExchangeHelper;

/**
 * @author says
 *
 */
public class ImportXmlFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
            ToolBox.configureLogging();
            HibernateUtil.configureHibernate(new Properties());
         	String fileName = args[0];
         	Debug.info("filename = " + fileName);
        	FileInputStream fis = null;
        	try {
                fis = new FileInputStream(fileName);
        		Document document = (new SAXReader()).read(fis);
        		DataExchangeHelper.importDocument(document, null, null);           
			} catch (IOException e) {
			    throw e;
			} finally {
			    if (fis != null) {
			        try { fis.close(); } catch (IOException e) {}
			    }
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}
