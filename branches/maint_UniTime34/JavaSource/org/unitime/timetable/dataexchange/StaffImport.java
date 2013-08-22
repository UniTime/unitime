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
package org.unitime.timetable.dataexchange;

import java.io.FileInputStream;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Staff;


/**
 * 
 * @author Timothy Almon
 *
 */
public class StaffImport extends BaseImport {
	boolean trimLeadingZerosFromExternalId = false;

	public StaffImport() {
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
        
        loadXml(root);
    }
    
	
    public void loadXml(Element root) throws Exception {
		String trimLeadingZeros =
	        ApplicationProperties.getProperty("tmtbl.data.exchange.trim.externalId","false");
		if (trimLeadingZeros.equals("true")){
			trimLeadingZerosFromExternalId = true;
		}

        if (!root.getName().equalsIgnoreCase("staff")) {
        	throw new Exception("Given XML file is not a Staff load file.");
        }
        String campus = root.attributeValue("campus");
        String year   = root.attributeValue("year");
        String term   = root.attributeValue("term");
        String created = root.attributeValue("created");
        String elementName = "staffMember";
        boolean posCodeWarning = false;
		try {
			beginTransaction();

	        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        
	        if (session != null && created != null) {
				ChangeLog.addChange(getHibSession(), getManager(), session, session, created, ChangeLog.Source.DATA_IMPORT_STAFF, ChangeLog.Operation.UPDATE, null, null);
	        }
	       
	        for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
				Element element = (Element) it.next();
				String externalId = getRequiredStringAttribute(element, "externalId", elementName);
				String dept = getOptionalStringAttribute(element, "department");
				Staff staff = null;
				if(externalId != null && externalId.length() > 0) {
		            if (trimLeadingZerosFromExternalId){
		            	try {
		            		Integer num = new Integer(externalId);
		            		externalId = num.toString();
						} catch (Exception e) {
							// do nothing
						}
			        }
	 				staff = findByExternalId(externalId, dept);
				}
				if(staff == null) {
					staff = new Staff();
				}
				else {
					if("T".equalsIgnoreCase(element.attributeValue("delete"))) {
						getHibSession().delete(staff);
						continue;
					}
				}
				staff.setFirstName(getOptionalStringAttribute(element, "firstName"));
				staff.setMiddleName(getOptionalStringAttribute(element, "middleName"));
				staff.setLastName(getRequiredStringAttribute(element, "lastName", elementName));
				PositionType posType = null;
				String positionType = getOptionalStringAttribute(element, "positionType");
				if (positionType != null)
					posType = PositionType.findByRef(positionType);
				if (!posCodeWarning && getOptionalStringAttribute(element, "positionCode") != null) {
					warn("Attribute positionCode is no longer supported, please use positionType attribute instead.");
					posCodeWarning = true;
				}
				staff.setPositionType(posType);
				staff.setExternalUniqueId(externalId);
				if (dept != null)
					staff.setDept(dept);
				String email = getOptionalStringAttribute(element, "email");
				if (email != null)
					staff.setEmail(email);
				getHibSession().saveOrUpdate(staff);
				flushIfNeeded(true);
	        }
			commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}

        return;
        
	}

	private Staff findByExternalId(String externalId, String deptCode) {
		if (deptCode != null)
			return
				(Staff)getHibSession().
				createQuery("select distinct a from Staff as a where a.externalUniqueId=:externalId and a.dept=:deptCode").
				setString("externalId", externalId).
				setString("deptCode", deptCode).
				setCacheable(true).
				uniqueResult();
		else
			return
				(Staff)getHibSession().
				createQuery("select distinct a from Staff as a where a.externalUniqueId=:externalId").
				setString("externalId", externalId).
				setCacheable(true).
				uniqueResult();
	}
	
}
