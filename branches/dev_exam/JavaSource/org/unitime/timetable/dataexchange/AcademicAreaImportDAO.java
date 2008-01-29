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
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.AcademicAreaDAO;


/**
 * 
 * @author Timothy Almon
 *
 */
public class AcademicAreaImportDAO extends AcademicAreaDAO {

	public AcademicAreaImportDAO() {
		super();
	}

	public void loadFromXML(String filename) throws Exception {

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filename);
			loadFromStream(fis);
		} finally {
			if (fis != null) fis.close();
		}
		return;
	}

	public void loadFromStream(FileInputStream fis) throws Exception {

		Document document = (new SAXReader()).read(fis);
        Element root = document.getRootElement();
        
        loadFromXML(root);
    }
    
    
    public void loadFromXML(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("academicAreas")) {
        	throw new Exception("Given XML file is not an AcademicArea load file.");
        }
        
        String campus = root.attributeValue("campus");
        String year   = root.attributeValue("year");
        String term   = root.attributeValue("term");

        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
        if(session == null) {
           	throw new Exception("No session found for the given campus, year, and term.");
        }
        
        for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            String externalId = element.attributeValue("externalId");
            AcademicArea acadArea = null;
            if(externalId != null && externalId.length() > 0) {
            	acadArea = findByExternalId(externalId, session.getSessionId());
            }
            if(acadArea == null) {
            	acadArea = new AcademicArea();
                acadArea.setSession(session);
            }
            else {
            	if("T".equalsIgnoreCase(element.attributeValue("delete"))) {
            		this.delete(acadArea);
            		continue;
            	}
            }
            acadArea.setAcademicAreaAbbreviation(element.attributeValue("abbreviation"));
            acadArea.setLongTitle(element.attributeValue("longTitle"));
            acadArea.setShortTitle(element.attributeValue("shortTitle"));
            acadArea.setExternalUniqueId(externalId);
            saveOrUpdate(acadArea);
        }
        return;
	}

	private AcademicArea findByExternalId(String externalId, Long sessionId) {
		return (AcademicArea) this.
			getSession().
			createQuery("select distinct a from AcademicArea as a where a.externalUniqueId=:externalId and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("externalId", externalId).
			setCacheable(true).
			uniqueResult();
	}
}