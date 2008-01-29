/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.dataexchange;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 
 * @author Tomas Muller
 *
 */

public abstract class BaseImport extends DataExchangeHelper {
    protected static Log sLog = LogFactory.getLog(BaseImport.class);

    public BaseImport() {
        super();
    }
    
    public void loadXml(String fileName) throws Exception {
        debug("Loading "+fileName);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
            loadXml(fis);
        } catch (IOException e) {
            fatal("Unable to read file "+fileName+", reason:"+e.getMessage(),e);
            throw e;
        } finally {
            if (fis != null) {
                try { fis.close(); } catch (IOException e) {}
            }
        }
    }
    
    public void loadXml(InputStream inputStream) throws Exception {
        try {
            Document document = (new SAXReader()).read(inputStream);
            loadXml(document.getRootElement());
        } catch (DocumentException e) {
            fatal("Unable to parse given XML, reason:"+e.getMessage(), e);
        }
    }    
    
    public abstract void loadXml(Element rootElement) throws Exception;
    
}
