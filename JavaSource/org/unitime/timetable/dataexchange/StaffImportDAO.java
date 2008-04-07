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
import org.unitime.timetable.model.PositionCodeType;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.dao.PositionCodeTypeDAO;
import org.unitime.timetable.model.dao.StaffDAO;


/**
 * 
 * @author Timothy Almon
 *
 */
public class StaffImportDAO extends StaffDAO {

	public StaffImportDAO() {
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

        if (!root.getName().equalsIgnoreCase("staff")) {
        	throw new Exception("Given XML file is not a Staff load file.");
        }
        
        for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            String externalId = element.attributeValue("externalId");
            Staff staff = null;
            if(externalId != null && externalId.length() > 0) {
            	staff = findByExternalId(externalId);
            }
            if(staff == null) {
            	staff = new Staff();
            }
            else {
            	if("T".equalsIgnoreCase(element.attributeValue("delete"))) {
            		this.delete(staff);
            		continue;
            	}
            }
            staff.setFirstName(element.attributeValue("firstName"));
            staff.setMiddleName(element.attributeValue("middleName"));
            staff.setLastName(element.attributeValue("lastName"));
            PositionCodeType posCodeType = null;
            String positionCode = element.attributeValue("positionCode");
            if (positionCode != null && positionCode.trim().length() > 0){
            	posCodeType = new PositionCodeTypeDAO().get(positionCode);
            }
            staff.setPositionCode(posCodeType);
            staff.setExternalUniqueId(externalId);
            staff.setDept(element.attributeValue("department"));
            saveOrUpdate(staff);
        }
        return;
	}

	private Staff findByExternalId(String externalId) {
		return (Staff) this.
			getSession().
			createQuery("select distinct a from Staff as a where a.externalUniqueId=:externalId").
			setString("externalId", externalId).
			setCacheable(true).
			uniqueResult();
	}
}