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
package org.unitime.timetable.form;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;

/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class DataImportForm implements UniTimeForm {
	private static final long serialVersionUID = 7165669008085313647L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private transient File iFile;  
    private String iFileContentType;  
    private String iFileFileName;
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
    	ROOM_SHARING("roomSharing", "Room Sharing"),
    	POINT_IN_TIME_DATA("pointInTimeData", "Point-In-Time Data"),
    	PREFERENCES("preferences", "Course Timetabling Preferences"),
    	SESSION_SETUP("sessionSetup", "Academic Session Setup"),
    	STUDENT_ADVISORS("studentAdvisors", "Student Advisors"),
    	STUDENT_STATUSES("studentStatuses", "Student Scheduling Statuses"),
    	INSTRUCTOR_SURVEYS("instructorSurveys", "Instructor Surveys"),
    	SCRIPTS("scripts", "Scripts"),
    	REPORTS("reports", "Reports"),
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
	
    public void validate(UniTimeAction action) {
        if ("Import".equals(iOp) && (iFile == null || !iFile.exists())) {
        	action.addFieldError("form.file", MSG.errorRequiredField(MSG.fieldFile()));
        }
        
        if ("Export".equals(iOp) && getExportType() == null) {
        	action.addFieldError("form.export", MSG.errorNothingToExport());
        }
	}

	public void reset() {
		iFile = null; iFileContentType = null; iFileFileName = null;
		iExport = null;
		iEmail = false;
		iAddress = null;
	}

	public File getFile() { return iFile; }
	public void setFile(File file) {
		if (file != null && file.exists()) {
			try {
				File newFile = new File(file.getParent(), file.getName() + ".lock");
				Files.move(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				iFile = newFile;
			} catch (IOException e) {
				iFile = file;
			}
		} else {
			iFile = file;
		}
	}
	public String getFileContentType() { return iFileContentType; }
	public void setFileContentType(String contentType) { iFileContentType = contentType; }
	public String getFileFileName() { return iFileFileName; }
	public void setFileFileName(String fileName) { iFileFileName = fileName; }
	
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
    	form.iFileContentType = iFileContentType;
    	form.iFileFileName = iFileFileName;
    	form.iOp = iOp;
    	form.iExport = iExport;
        form.iEmail = iEmail;
        form.iAddress = iAddress;
        return form;
    }
    
    public List<ListItem> getExportTypes() {
    	ArrayList<ListItem> items = new ArrayList<ListItem>();
    	items.add(new ListItem("", MSG.itemSelect()));
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

