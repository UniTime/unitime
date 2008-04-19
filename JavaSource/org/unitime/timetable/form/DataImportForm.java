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
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.upload.FormFile;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.timetable.dataexchange.AcadAreaReservationImportDAO;
import org.unitime.timetable.dataexchange.AcademicAreaImportDAO;
import org.unitime.timetable.dataexchange.AcademicClassificationImportDAO;
import org.unitime.timetable.dataexchange.BuildingRoomImport;
import org.unitime.timetable.dataexchange.CourseCatalogImportDAO;
import org.unitime.timetable.dataexchange.CourseOfferingImport;
import org.unitime.timetable.dataexchange.DepartmentImportDAO;
import org.unitime.timetable.dataexchange.LastLikeCourseDemandImport;
import org.unitime.timetable.dataexchange.PosMajorImportDAO;
import org.unitime.timetable.dataexchange.PosMinorImportDAO;
import org.unitime.timetable.dataexchange.SessionImportDAO;
import org.unitime.timetable.dataexchange.StaffImport;
import org.unitime.timetable.dataexchange.StudentEnrollmentImport;
import org.unitime.timetable.dataexchange.StudentImport;
import org.unitime.timetable.dataexchange.SubjectAreaImportDAO;


/** 
 * MyEclipse Struts
 * Creation date: 01-24-2007
 * 
 * XDoclet definition:
 * @struts.form name="dataImportForm"
 */
public class DataImportForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables

	/** fileName property */
	private FormFile file;

	private String op;

	// --------------------------------------------------------- Methods

	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
		
        if (file == null || file.getFileSize()<=0) {
        	errors.add("name", new ActionMessage("errors.required", "File") );
        }
        /*
        else {
	        File file = new File(fileName);
	        if(!file.exists()) {
	        	errors.add("notFound", new ActionMessage("errors.fileNotFound", fileName) );
	        }
        }*/

        return errors;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		this.file = null;
	}

	/** 
	 * Returns the fileName.
	 * @return String
	 */
	public FormFile getFile() {
		return file;
	}

	/** 
	 * Set the fileName.
	 * @param fileName The fileName to set
	 */
	public void setFile(FormFile file) {
		this.file = file;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public static void importDocument(Document document, HttpServletRequest request) throws Exception {
        Element root = document.getRootElement();

        if (root.getName().equalsIgnoreCase("academicAreas")) {
        	new AcademicAreaImportDAO().loadFromXML(root);
        }
        else if(root.getName().equalsIgnoreCase("subjectAreas")) {
        	new SubjectAreaImportDAO().loadFromXML(root, request);
        }
        else if(root.getName().equalsIgnoreCase("academicClassifications")) {
        	new AcademicClassificationImportDAO().loadFromXML(root);
        }
        else if(root.getName().equalsIgnoreCase("departments")) {
        	new DepartmentImportDAO().loadFromXML(root, request);
        }
        else if(root.getName().equalsIgnoreCase("posMajors")) {
        	new PosMajorImportDAO().loadFromXML(root);
        }
        else if(root.getName().equalsIgnoreCase("posMinors")) {
        	new PosMinorImportDAO().loadFromXML(root);
        }
        else if(root.getName().equalsIgnoreCase("students")) {
        	new StudentImport().loadXml(root);
        }
        else if(root.getName().equalsIgnoreCase("staff")) {
        	new StaffImport().loadXml(root, request);
        }
        else if(root.getName().equalsIgnoreCase("lastLikeCourseDemand")) {
        	new LastLikeCourseDemandImport().loadXml(root);
        }
        else if(root.getName().equalsIgnoreCase("academicAreaReservations")) {
        	new AcadAreaReservationImportDAO().loadFromXML(root);
        }
        else if(root.getName().equalsIgnoreCase("session")) {
        	new SessionImportDAO().loadFromXML(root);
        }
        else if(root.getName().equalsIgnoreCase("courseCatalog")) {
        	new CourseCatalogImportDAO().loadFromXML(root);
        }
        else if(root.getName().equalsIgnoreCase("buildingsRooms")) {
        	new BuildingRoomImport().loadXml(root, request);
        }
        else if(root.getName().equalsIgnoreCase("offerings")) {
        	new CourseOfferingImport().loadXml(root, request);
        }
        else if(root.getName().equalsIgnoreCase("studentEnrollments")) {
        	new StudentEnrollmentImport().loadXml(root, request);
        }
       else {
        	throw new Exception(root.getName() + " is an unknown data type.");
        }
		
	}
	public void doImport(HttpServletRequest request) throws Exception {
        
		Document document = (new SAXReader()).read(file.getInputStream());
		importDocument(document, request);
	}
}

