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
package org.unitime.timetable.model;

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.timetable.model.base.BaseCurriculumClassification;



/**
 * @author Tomas Muller
 */
public class CurriculumClassification extends BaseCurriculumClassification implements Comparable<CurriculumClassification> {
	private static Log sLog = LogFactory.getLog(CurriculumClassification.class);
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CurriculumClassification () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CurriculumClassification (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public int compareTo(CurriculumClassification cc) {
		if (getAcademicClassification() != null && cc.getAcademicClassification() != null) {
			int cmp = getAcademicClassification().getCode().compareTo(cc.getAcademicClassification().getCode());
			if (cmp != 0) return cmp;
		}
	    if (getOrd()!=null && cc.getOrd()!=null && !getOrd().equals(cc.getOrd()))
	        return getOrd().compareTo(cc.getOrd());
	    int cmp = getName().compareToIgnoreCase(cc.getName());
	    if (cmp!=0) return cmp;
	    return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(cc.getUniqueId() == null ? -1 : cc.getUniqueId());
	}
	
	public Document getStudentsDocument() {
		if (getStudents() == null) return null;
		try {
			return new SAXReader().read(new StringReader(getStudents()));
		} catch (Exception e) {
			sLog.warn("Failed to load cached students for " + getCurriculum().getAbbv() + " " + getName() + ": " + e.getMessage(), e);
			return null;
		}
	}
	
	public void setStudentsDocument(Document document) {
		try {
			if (document == null) {
				setStudents(null);
			} else {
				StringWriter string = new StringWriter();
				XMLWriter writer = new XMLWriter(string, OutputFormat.createCompactFormat());
				writer.write(document);
				writer.flush(); writer.close();
				setStudents(string.toString());
			}
		} catch (Exception e) {
			sLog.warn("Failed to store cached students for " + getCurriculum().getAbbv() + " " + getName() + ": " + e.getMessage(), e);
		}
	}
}