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
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.NonUniqueResultException;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Staff;


/**
 * 
 * @author Timothy Almon, Stephanie Schluttenhofer, Tomas Muller
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
    	trimLeadingZerosFromExternalId = ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue();

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
				if (externalId != null && externalId.length() > 0) {
		            if (trimLeadingZerosFromExternalId){
		            	try {
		            		Integer num = new Integer(externalId);
		            		externalId = num.toString();
						} catch (Exception e) {
							// do nothing
						}
			        }
		            try {
		            	staff = findByExternalId(externalId, dept);
		            } catch (NonUniqueResultException e) {
		            	error("Multiple staff members exist for the external id " + externalId + ", please provide department code.");
		            	continue;
		            }
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
				staff.setAcademicTitle(getOptionalStringAttribute(element, "acadTitle"));
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
				staff.setCampus(campus);
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
