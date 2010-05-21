/**
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
