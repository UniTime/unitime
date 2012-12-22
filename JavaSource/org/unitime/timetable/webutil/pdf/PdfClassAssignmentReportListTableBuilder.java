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
package org.unitime.timetable.webutil.pdf;

import java.awt.Color;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import org.unitime.commons.Debug;
import org.unitime.timetable.form.ClassAssignmentsReportForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
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
	
	@Override
	protected PdfPCell pdfBuildDatePatternCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	Assignment a = null;
		if (getDisplayTimetable() && isShowTimetable() && classAssignment!=null && prefGroup instanceof Class_) {
			try {
				a = classAssignment.getAssignment((Class_)prefGroup);
			} catch (Exception e) {
				Debug.error(e);
			}
    	}
    	DatePattern dp = (a != null ? a.getDatePattern() : prefGroup.effectiveDatePattern());
    	PdfPCell cell = createCell();
    	if (dp!=null) {
    		Color color = (isEditable?sEnableColor:sDisableColor);
			addText(cell,dp.getName(), false, false, Element.ALIGN_CENTER, color, true);
    	}
        return cell;
    }

    public void pdfTableForClasses(OutputStream out, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, ClassAssignmentsReportForm form, SessionContext context) throws Exception{
        setVisibleColumns(form);
 		
        Collection classes = (Collection) form.getClasses();
        
        if (isShowTimetable()) {
        	boolean hasTimetable = false;
        	if (context.hasPermission(Right.ClassAssignments) && classAssignment != null) {
            	if (classAssignment instanceof CachedClassAssignmentProxy) {
            		((CachedClassAssignmentProxy)classAssignment).setCache(classes);
            	}
    			for (Iterator i=classes.iterator();i.hasNext();) {
    				Object[] o = (Object[])i.next(); Class_ clazz = (Class_)o[0];
    				if (classAssignment.getAssignment(clazz)!=null) {
    					hasTimetable = true; break;
    				}
    			}
        	}
        	setDisplayTimetable(hasTimetable);
        }
        setUserSettings(context.getUser());
        
        if (examAssignment!=null || Exam.hasTimetable(context.getUser().getCurrentAcademicSessionId())) {
            setShowExam(true);
            setShowExamTimetable(true);
            setShowExamName(false);
        }
        setShowInstructor(true);
        if (StudentClassEnrollment.sessionHasEnrollments(context.getUser().getCurrentAcademicSessionId())) {
        	setShowDemand(true);
        }
        
		float[] widths = getWidths();
		float totalWidth = 0;
		for (int i=0;i<widths.length;i++)
			totalWidth += widths[i];

		iDocument = new Document(new Rectangle(60f+totalWidth,60f+1.30f*totalWidth), 30f, 30f, 30f, 30f); 

		iWriter = PdfEventHandler.initFooter(iDocument, out);
		iDocument.open();

        int ct = 0;
        Iterator it = classes.iterator();
        SubjectArea subjectArea = null;
        String prevLabel = null;
        while (it.hasNext()){
        	Object[] o = (Object[])it.next(); Class_ c = (Class_)o[0]; CourseOffering co = (CourseOffering)o[1];
        	if (subjectArea == null || !subjectArea.getUniqueId().equals(co.getSubjectArea().getUniqueId())){
				if (iPdfTable!=null) {
					iDocument.add(iPdfTable);
					iDocument.newPage();
				}
        		
				iPdfTable = new PdfPTable(getWidths());
				iPdfTable.setWidthPercentage(100);
				iPdfTable.getDefaultCell().setPadding(3);
				iPdfTable.getDefaultCell().setBorderWidth(0);
				iPdfTable.setSplitRows(false);

				subjectArea = co.getSubjectArea();
				ct = 0;

				iDocument.add(new Paragraph(labelForTable(subjectArea), PdfFont.getBigFont(true)));
				iDocument.add(new Paragraph(" "));
				pdfBuildTableHeader(context.getUser().getCurrentAcademicSessionId());
			}
            
            pdfBuildClassRow(classAssignment, examAssignment, ++ct, co, c, "", context, prevLabel);
            prevLabel = c.getClassLabel(co);
        }  
        
        if (iPdfTable!=null)
        	iDocument.add(iPdfTable);

		iDocument.close();
    }
    
    @Override
    protected PdfPCell pdfBuildInstructor(PreferenceGroup prefGroup, boolean isEditable){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();
    	
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		if (aClass.isDisplayInstructor()) {
            	TreeSet sortedInstructors = new TreeSet(new InstructorComparator());
            	sortedInstructors.addAll(aClass.getClassInstructors());
        		for (Iterator i=sortedInstructors.iterator(); i.hasNext();) {
        			ClassInstructor ci = (ClassInstructor)i.next();
            		String label = ci.getInstructor().getName(getInstructorNameFormat());
            		addText(cell, label, false, false, Element.ALIGN_LEFT, color, true);
        		}
    		}
    	}
    	
        return cell;
    }
}
