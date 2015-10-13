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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.timetable.model.TimetableManager;

/**
 * 
 * @author Tomas Muller, Stephanie Schluttenhofer
 *
 */

public abstract class BaseImport extends DataExchangeHelper {
    protected static Log sLog = LogFactory.getLog(BaseImport.class);
	private TimetableManager iManager = null;

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
    
    protected String getRequiredStringAttribute(Element element, String attributeName, String elementName) throws Exception{		
		String attributeValue = element.attributeValue(attributeName);
		if (attributeValue == null || attributeValue.trim().length() == 0){
			throw new Exception("For element '" + elementName + "' a '" + attributeName + "' is required");
		} else {
			attributeValue = attributeValue.trim().replace('\u0096', ' ').replace('\u0097', ' ');
		}						
		return(attributeValue);
	}
	
	protected String getOptionalStringAttribute(Element element, String attributeName) {		
		String attributeValue = element.attributeValue(attributeName);
		if (attributeValue == null || attributeValue.trim().length() == 0){
			attributeValue = null;
		} else {
			attributeValue = attributeValue.trim().replace('\u0096', ' ').replace('\u0097', ' ');
		}						
		return(attributeValue);		
	}
	
	protected Integer getRequiredIntegerAttribute(Element element, String attributeName, String elementName) throws Exception {
		String attributeStr = getRequiredStringAttribute(element, attributeName, elementName);
		return(new Integer(attributeStr));
	}
	
	protected Integer getOptionalIntegerAttribute(Element element, String attributeName) {
		String attributeStr = getOptionalStringAttribute(element, attributeName);
		if (attributeStr != null){
			return(new Integer(attributeStr));
		} else {
			return(null);
		}
	}
	
	protected Boolean getRequiredBooleanAttribute(Element element, String attributeName, String elementName) throws Exception {
		String attributeStr = getRequiredStringAttribute(element, attributeName, elementName);
		return(new Boolean(attributeStr));
	}
	
	protected Boolean getOptionalBooleanAttribute(Element element, String attributeName) {
		String attributeStr = getOptionalStringAttribute(element, attributeName);
		if (attributeStr != null) {
			return(new Boolean(attributeStr));
		} else {
			return(null);
		}
	}
	
	protected boolean getOptionalBooleanAttribute(Element element, String attributeName, boolean defaultValue) {
		String attributeStr = getOptionalStringAttribute(element, attributeName);
		if (attributeStr != null) {
			return Boolean.parseBoolean(attributeStr);
		} else {
			return defaultValue;
		}
	}
	
	protected TimetableManager getManager() {
	    if (iManager == null) iManager = findDefaultManager(); 
	    return iManager; 
	}
	
	public void setManager(TimetableManager manager) {
	    iManager = manager;
	}
	
	protected TimetableManager findDefaultManager(){
		return((TimetableManager)getHibSession().createQuery("from TimetableManager as m where m.uniqueId = (select min(tm.uniqueId) from TimetableManager as tm inner join tm.managerRoles as mr inner join mr.role as r where r.reference = 'Administrator')").uniqueResult());
	}
	
	protected String trim(String text, String name, int maxLength) {
		if (text != null && text.length() > maxLength) {
			if (name != null)
				info("Attribute " + name + " is too long (" + text + "), it will be trimmed to " + maxLength + " characters.");
			return text.substring(0, maxLength);
		} else {
			return text;
		}
	}

}
