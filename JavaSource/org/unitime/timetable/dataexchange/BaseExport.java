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
package org.unitime.timetable.dataexchange;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.unitime.timetable.model.Session;

/**
 * 
 * @author Tomas Muller
 *
 */

public abstract class BaseExport extends DataExchangeHelper {
    protected static Log sLog = LogFactory.getLog(BaseExport.class);
    
    public BaseExport() {
        super();
    }
    
    public void saveXml(String fileName, Session session, Properties parameters) throws Exception {
        debug("Saving "+fileName);
        Document doc = saveXml(session, parameters);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            (new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(doc);
            fos.flush();fos.close();fos=null;
        } finally {
            try {
                if (fos!=null) fos.close();
            } catch (IOException e) {
                fatal("Unable to write file "+fileName+", reason:"+e.getMessage(),e);
                throw e;
            }
        }
    }
    
    public Document saveXml(Session session, Properties parameters) throws Exception {
        Document document = DocumentHelper.createDocument();
        saveXml(document, session, parameters);
        return document;
    }    
    
    public abstract void saveXml(Document document, Session session, Properties parameters) throws Exception;
}
