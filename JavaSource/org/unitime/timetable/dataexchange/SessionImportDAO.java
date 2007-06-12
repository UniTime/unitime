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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;


/**
 * 
 * @author Timothy Almon
 *
 */
public class SessionImportDAO extends SessionDAO {

	public SessionImportDAO() {
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

        if (!root.getName().equalsIgnoreCase("session")) {
        	throw new Exception("Given XML file is not a Session load file.");
        }

        String campus = root.attributeValue("campus");
        String year   = root.attributeValue("year");
        String term   = root.attributeValue("term");
        DepartmentStatusType statusType = DepartmentStatusType.findByRef("initial");

        for ( Iterator iter = root.elementIterator(); iter.hasNext(); ) {
            Element element = (Element) iter.next();
			String beginDate = element.attributeValue("beginDate");
			if(beginDate == null) {
				throw new Exception("Begin Date is required.");
			}
			String endDate = element.attributeValue("endDate");
			if(endDate == null) {
				throw new Exception("End Date is required.");
			}
			String classesEnd = element.attributeValue("classesEnd");
			if(classesEnd == null) {
				throw new Exception("Classes End Date is required.");
			}
			Session session = new Session();
			session.setAcademicInitiative(campus);
			session.setAcademicYear(year);
			session.setAcademicTerm(term);
			DateFormat df = new SimpleDateFormat("M/d/y k:m:s");
			session.setSessionBeginDateTime(df.parse(beginDate));
			session.setSessionEndDateTime(df.parse(endDate));
			session.setClassesEndDateTime(df.parse(classesEnd));
			session.setStatusType(statusType);
		    saveOrUpdate(session);
        }
	}
}