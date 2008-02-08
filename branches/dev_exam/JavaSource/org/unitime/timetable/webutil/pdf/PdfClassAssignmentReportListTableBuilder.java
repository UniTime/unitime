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
package org.unitime.timetable.webutil.pdf;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ClassAssignmentsReportForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.PdfEventHandler;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;


/**
 * @author Tomas Muller
 */
public class PdfClassAssignmentReportListTableBuilder extends PdfClassListTableBuilder {
	protected Color sDisableColor = Color.BLACK;

	public PdfClassAssignmentReportListTableBuilder() {
		super();
	}

	
	protected String additionalNote(){
		return(" Room Assignments");
	}

    public File pdfTableForClasses(ClassAssignmentProxy classAssignment, ClassAssignmentsReportForm form, User user){
    	FileOutputStream out = null;
    	try {
            setVisibleColumns(form);
     		
            Collection classes = (Collection) form.getClasses();
            
            if (isShowTimetable()) {
            	boolean hasTimetable = false;
            	try {
            		String managerId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
            		TimetableManager manager = (new TimetableManagerDAO()).get(new Long(managerId));
            		if (manager!=null && manager.canSeeTimetable(Session.getCurrentAcadSession(user), user) && classAssignment!=null) {
		            	if (classAssignment instanceof CachedClassAssignmentProxy) {
		            		((CachedClassAssignmentProxy)classAssignment).setCache(classes);
		            	}
            			for (Iterator i=classes.iterator();i.hasNext();) {
            				Class_ clazz = (Class_)i.next();
            				if (classAssignment.getAssignment(clazz)!=null) {
            					hasTimetable = true; break;
            				}
            			}
            		}
            	} catch (Exception e) {}
            	setDisplayTimetable(hasTimetable);
            }
            setUserSettings(user);
            
            if (Exam.hasTimetable((Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME))) {
                setShowExam(true);
                setShowExamTimetable(true);
                setShowExamName(false);
            }
            
            File file = ApplicationProperties.getTempFile("classassign", "pdf");
	    	
			float[] widths = getWidths();
			float totalWidth = 0;
			for (int i=0;i<widths.length;i++)
				totalWidth += widths[i];

			iDocument = new Document(new Rectangle(60f+totalWidth,60f+1.30f*totalWidth), 30f, 30f, 30f, 30f); 

			out = new FileOutputStream(file);
			iWriter = PdfEventHandler.initFooter(iDocument, out);
			iDocument.open();

			Class_ c = null;
            TableStream table = null;
            int ct = 0;
            Iterator it = classes.iterator();
            SubjectArea subjectArea = null;
            String prevLabel = null;
            while (it.hasNext()){
                c = (Class_) it.next();
                if (subjectArea == null || !subjectArea.getUniqueId().equals(c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().getUniqueId())){
					if (iPdfTable!=null) {
						iDocument.add(iPdfTable);
						iDocument.newPage();
					}
            		
					iPdfTable = new PdfPTable(getWidths());
					iPdfTable.setWidthPercentage(100);
					iPdfTable.getDefaultCell().setPadding(3);
					iPdfTable.getDefaultCell().setBorderWidth(0);
					iPdfTable.setSplitRows(false);

					subjectArea = c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea();
					ct = 0;

					iDocument.add(new Paragraph(labelForTable(subjectArea), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
					iDocument.add(new Paragraph(" "));
					pdfBuildTableHeader();
				}
                
                pdfBuildClassRow(classAssignment,++ct, c, "", user, prevLabel);
                prevLabel = c.getClassLabel();
            }  
            
            if (iPdfTable!=null)
            	iDocument.add(iPdfTable);

			iDocument.close();

			return file;
    	} catch (Exception e) {
    		Debug.error(e);
    	} finally {
        	try {
        		if (out!=null) out.close();
        	} catch (IOException e) {}
    	}
    	return null;
    }
}
