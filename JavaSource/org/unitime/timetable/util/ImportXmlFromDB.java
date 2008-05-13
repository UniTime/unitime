/**
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
import org.hibernate.engine.SessionFactoryImplementor;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.DataImportForm;
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
            SessionFactoryImplementor hibSessionFactory = (SessionFactoryImplementor)new _RootDAO().getSession().getSessionFactory();
            Connection connection = hibSessionFactory.getConnectionProvider().getConnection();
            CallableStatement call = connection.prepareCall(fileReceiveSql);
            call.registerOutParameter(1, java.sql.Types.CLOB);
		    call.setString(2, exchangeDir);
            call.setString(3, baseFileName);
            call.execute();
            String response = call.getString(1);
            call.close();
            hibSessionFactory.getConnectionProvider().closeConnection(connection);
            if (response==null || response.length()==0) return;
            StringReader reader = new StringReader(response);
            Document document = (new SAXReader()).read(reader);
            reader.close();
            DataImportForm.importDocument(document, null);           
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
	        HibernateUtil.configureHibernate(new Properties());
	        if (args[0].length() == 0){
	        	throw(new Exception("Please specify a base file name to which '.xml' and '.ready' can be appended."));
	        }
	        importXml(args[0]);
	 	} catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
