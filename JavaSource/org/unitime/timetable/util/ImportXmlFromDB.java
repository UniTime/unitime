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

import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Properties;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.hibernate.engine.spi.SessionImplementor;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.model.dao._RootDAO;




/**
 * @author says
 *
 */
public class ImportXmlFromDB {

	
	public static void importXml(String baseFileName){
     	Debug.info("filename = " + baseFileName);
    	try {
    		String fileReceiveSql =
    	        ApplicationProperties.getProperty("tmtbl.data.exchange.receive.file","{?= call timetable.receive_xml_file.receive_file(?, ?)}");
    		String exchangeDir = 
    	    	ApplicationProperties.getProperty("tmtbl.data.exchange.directory", "LOAD_SMASDEV");
            SessionImplementor session = (SessionImplementor)new _RootDAO().getSession();
            Connection connection = session.getJdbcConnectionAccess().obtainConnection();
            CallableStatement call = connection.prepareCall(fileReceiveSql);
            call.registerOutParameter(1, java.sql.Types.CLOB);
		    call.setString(2, exchangeDir);
            call.setString(3, baseFileName);
            call.execute();
            String response = call.getString(1);
            call.close();
            session.getJdbcConnectionAccess().releaseConnection(connection);
            if (response==null || response.length()==0) return;
            StringReader reader = new StringReader(response);
            Document document = (new SAXReader()).read(reader);
            reader.close();
            DataExchangeHelper.importDocument(document, null, null);           
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args){
		try{
		    ToolBox.configureLogging();
	        if (args[0].length() == 0){
	        	throw(new Exception("Please specify a base file name to which '.xml' and '.ready' can be appended."));
	        }
	        Properties properties = new Properties();
	        properties.put("connection.url", args[1]);
	        HibernateUtil.configureHibernate(properties);
	        importXml(args[0]);
	 	} catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
