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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SubjectAreaDAO;


/**
 * 
 * @author Timothy Almon
 *
 */
public class SubjectAreaImportDAO extends SubjectAreaDAO {
	TimetableManager manager = null;
	
	public SubjectAreaImportDAO() {
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
	}

	public void loadFromStream(FileInputStream fis) throws Exception {

		Document document = (new SAXReader()).read(fis);
        Element root = document.getRootElement();
        
        loadFromXML(root);
        
    }
    
	public void loadFromXML(Element rootElement, HttpServletRequest request) throws Exception {
		if (request != null){
			HttpSession httpSession = request.getSession();
	        String userId = (String)httpSession.getAttribute("authUserExtId");
	        User user = Web.getUser(httpSession);
	        if (userId!=null) {
	        	manager = TimetableManager.findByExternalId(userId);
	        }
	        if (manager==null && user!=null) {
	            Debug.warning("No authenticated user defined, using "+user.getName());
	        	manager = TimetableManager.getManager(user);
	        }
		}
		loadFromXML(rootElement);
	}

    public void loadFromXML(Element root) throws Exception {

        if (!root.getName().equalsIgnoreCase("subjectAreas")) {
        	throw new Exception("Given XML file is not a SubjectArea load file.");
        }
        
        String campus = root.attributeValue("campus");
        String year   = root.attributeValue("year");
        String term   = root.attributeValue("term");
        String created = root.attributeValue("created");

        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
        if(session == null) {
           	throw new Exception("No session found for the given campus, year, and term.");
        }
        if (manager == null){
        	manager = findDefaultManager();
        }
        if (created != null) {
			ChangeLog.addChange(getSession(), manager, session, session, created, ChangeLog.Source.DATA_IMPORT_SUBJECT_AREAS, ChangeLog.Operation.UPDATE, null, null);
        }
        
        for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            String externalId = element.attributeValue("externalId");
            SubjectArea subjectArea = null;
            if(externalId != null && externalId.length() > 0) {
            	subjectArea = findByExternalId(externalId, session.getSessionId());
            }
            if(subjectArea == null) {
            	subjectArea = new SubjectArea();
                subjectArea.setSession(session);
            }
            else {
            	if("T".equalsIgnoreCase(element.attributeValue("delete"))) {
            		this.delete(subjectArea);
            		continue;
            	}
            }
            subjectArea.setSubjectAreaAbbreviation(element.attributeValue("abbreviation"));
            subjectArea.setLongTitle(element.attributeValue("longTitle"));
            subjectArea.setShortTitle(element.attributeValue("shortTitle"));
            subjectArea.setExternalUniqueId(externalId);
            subjectArea.setScheduleBookOnly(new Boolean(element.attributeValue("schedBookOnly").equalsIgnoreCase("T")));
            subjectArea.setPseudoSubjectArea(new Boolean(element.attributeValue("pseudoSubjArea").equalsIgnoreCase("T")));

            String deptCode = element.attributeValue("department");
            Department department = findByDeptCode(deptCode, session.getSessionId());
            if (department == null) {
               	throw new Exception("No department found for " + deptCode);
            }
            subjectArea.setDepartment(department);

            saveOrUpdate(subjectArea);
        }
	}

	private SubjectArea findByExternalId(String externalId, Long sessionId) {
		return (SubjectArea) this.
			getSession().
			createQuery("select distinct a from SubjectArea as a where a.externalUniqueId=:externalId and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("externalId", externalId).
			setCacheable(true).
			uniqueResult();
	}

	private Department findByDeptCode(String deptCode, Long sessionId) {
		return (Department) this.
			getSession().
			createQuery("select distinct a from Department as a where a.deptCode=:deptCode and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("deptCode", deptCode).
			setCacheable(true).
			uniqueResult();
	}
	
	private TimetableManager findDefaultManager(){
		return((TimetableManager)getSession().createQuery("from TimetableManager as m where m.uniqueId = (select min(tm.uniqueId) from TimetableManager as tm inner join tm.managerRoles as mr inner join mr.role as r where r.reference = 'Administrator')").uniqueResult());
	}

}