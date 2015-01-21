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

import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Properties;


import org.cpsolver.ifs.util.ToolBox;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.hibernate.engine.spi.SessionImplementor;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.model.dao._RootDAO;




/**
 * @author Stephanie Schluttenhofer, Tomas Muller
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
