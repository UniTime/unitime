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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.upload.FormFile;

/** 
 * MyEclipse Struts
 * Creation date: 01-24-2007
 * 
 * XDoclet definition:
 * @struts.form name="dataImportForm"
 *
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class DataImportForm extends ActionForm {
	private static final long serialVersionUID = 7165669008085313647L;
	private FormFile iFile;
	private String iOp;
	private String iExport;
    private boolean iEmail = false;
    private String iAddress = null;
    
    public static enum ExportType {
    	COURSES("offerings", "Course Offerings",
    			"tmtbl.export.timetable", "false",
    			"tmtbl.export.exam.type", "none"),
    	COURSES_WITH_TIME("offerings", "Course Offerings (including course timetable)",
    			"tmtbl.export.timetable", "true",
    			"tmtbl.export.exam.type", "none"),
    	COURSES_WITH_EXAMS("offerings", "Course Offerings (including exams)", 
    			"tmtbl.export.timetable", "false",
    			"tmtbl.export.exam.type", "all"),
    	COURSES_ALL("offerings", "Course Offerings (including course timetable and exams)", 
    			"tmtbl.export.timetable", "true",
    			"tmtbl.export.exam.type", "all"),
    	TIMETABLE("timetable", "Course Timetable"),
    	EXAMS("exams", "Examinations",
    			"tmtbl.export.exam", "true",
    			"tmtbl.export.exam.type", "all"),
    	EXAMS_FINAL("exams", "Examinations (only finals)",
    			"tmtbl.export.exam", "true",
    			"tmtbl.export.exam.type", "final"),
    	EXAMS_MIDTERM("exams", "Examinations (only midterm)",
    			"tmtbl.export.exam", "true",
    			"tmtbl.export.exam.type", "midterm"),
    	CURRICULA("curricula", "Curricula"),
    	STUDENTS("students", "Students"),
    	STUDENT_ENRL("studentEnrollments", "Student class enrollments"),
    	REQUESTS("request", "Student course requests"),
    	RESERVATIONS("reservations", "Reservations"),
    	SESSION("session", "Academic Session"),
    	PERMISSIONS("permissions", "Permissions"),
    	TRAVELTIMES("traveltimes", "Travel Times"),
    	;
    	
    	private String iType, iLabel;
    	private String[] iOptions;
    	ExportType(String type, String label, String... options) {
    		iType = type; iLabel = label; iOptions = options;
    	}
    	public String getType() { return iType; }
    	public String getLabel() { return iLabel; }
    	public void setOptions(Properties config) {
    		for (int i = 0; i < iOptions.length; i += 2)
    			config.put(iOptions[i], iOptions[i + 1]);
    	}
    }
	
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
		
        if ("Import".equals(iOp) && (iFile == null || iFile.getFileSize()<=0)) {
        	errors.add("name", new ActionMessage("errors.required", "File") );
        }
        
        if ("Export".equals(iOp) && getExportType() == null) {
        	errors.add("export", new ActionMessage("errors.generic", "Nothing to export") );
        }
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iFile = null;
		iExport = null;
		iEmail = false;
		iAddress = null;
	}

	public FormFile getFile() { return iFile; }
	public void setFile(FormFile file) { iFile = file; }
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	
    public String getExport() { return iExport; }
    public void setExport(String export) { iExport = export; }
    
    public boolean getEmail() { return iEmail; }
    public void setEmail(boolean email) { iEmail = email; }
    
    public String getAddress() { return iAddress; }
    public void setAddress(String address) { iAddress = address; }

    public Object clone() {
    	DataImportForm form = new DataImportForm();
    	form.iFile = iFile;
    	form.iOp = iOp;
    	form.iExport = iExport;
        form.iEmail = iEmail;
        form.iAddress = iAddress;
        return form;
    }
    
    public List<ListItem> getExportTypes() {
    	ArrayList<ListItem> items = new ArrayList<ListItem>();
    	for (ExportType t: ExportType.values())
    		items.add(new ListItem(t.name(), t.getLabel()));
    	return items;
    }
    
    public ExportType getExportType() {
    	if (getExport() == null || getExport().isEmpty()) return null;
    	return ExportType.valueOf(getExport());
    }
    
    public static class ListItem {
    	String iValue, iText;
    	public ListItem(String value, String text) { iValue = value; iText = text; }
    	public String getValue() { return iValue; }
    	public String getLabel() { return iText; }
    }
}

