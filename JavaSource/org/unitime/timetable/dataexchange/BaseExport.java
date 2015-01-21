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
