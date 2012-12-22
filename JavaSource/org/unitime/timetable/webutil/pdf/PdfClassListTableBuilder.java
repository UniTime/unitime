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
import java.util.Iterator;
import java.util.TreeSet;

import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
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
public class PdfClassListTableBuilder extends PdfInstructionalOfferingTableBuilder {

	public PdfClassListTableBuilder() {
		super();
	}
	
	protected String additionalNote(){
		return(new String());
	}
	
	protected String labelForTable(SubjectArea subjectArea){
		StringBuffer sb = new StringBuffer();
		sb.append(subjectArea.getSubjectAreaAbbreviation());
		sb.append(" - ");
		sb.append(subjectArea.getSession().getLabel());
		sb.append(additionalNote());
		return(sb.toString());		
	}
	
	
	public void pdfTableForClasses(OutputStream out, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, ClassListForm form, SessionContext context) throws Exception {
		setVisibleColumns(form);
        
		TreeSet classes = (TreeSet) form.getClasses();
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
		
        if (isShowExam())
            setShowExamTimetable(examAssignment != null || Exam.hasTimetable(context.getUser().getCurrentAcademicSessionId()));
    
		float[] widths = getWidths();
		float totalWidth = 0;
		for (int i=0;i<widths.length;i++)
			totalWidth += widths[i];

		iDocument = new Document(new Rectangle(60f+totalWidth,60f+0.77f*totalWidth), 30f, 30f, 30f, 30f); 

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
	
    protected PdfPCell pdfBuildPrefGroupLabel(CourseOffering co, PreferenceGroup prefGroup, String indentSpaces, boolean isEditable, String prevLabel) {
    	if (prefGroup instanceof Class_) {
    		Color color = (isEditable?Color.BLACK:Color.GRAY);
    		String label = prefGroup.toString();
        	Class_ aClass = (Class_) prefGroup;
        	label = aClass.getClassLabel(co);
        	if (prevLabel != null && label.equals(prevLabel)){
        		label = "";
        	}
        	PdfPCell cell = createCell();
        	addText(cell, indentSpaces+label, co.isIsControl(), false, Element.ALIGN_LEFT, color, true);
        	return cell;
    	} else return super.pdfBuildPrefGroupLabel(co, prefGroup, indentSpaces, isEditable, null);
    }
    
    protected TreeSet getExams(Class_ clazz) {
        //exams directly attached to the given class
        TreeSet ret = new TreeSet(Exam.findAll(ExamOwner.sOwnerTypeClass, clazz.getUniqueId()));
        //check whether the given class is of the first subpart of the config
        SchedulingSubpart subpart = clazz.getSchedulingSubpart();
        if (subpart.getParentSubpart()!=null) return ret; 
        InstrOfferingConfig config = subpart.getInstrOfferingConfig();
        SchedulingSubpartComparator cmp = new SchedulingSubpartComparator();
        for (Iterator i=config.getSchedulingSubparts().iterator();i.hasNext();) {
            SchedulingSubpart s = (SchedulingSubpart)i.next();
            if (cmp.compare(s,subpart)<0) return ret;
        }
        InstructionalOffering offering = config.getInstructionalOffering();
        //check passed -- add config/offering/course exams to the class exams
        ret.addAll(Exam.findAll(ExamOwner.sOwnerTypeConfig, config.getUniqueId()));
        ret.addAll(Exam.findAll(ExamOwner.sOwnerTypeOffering, offering.getUniqueId()));
        for (Iterator i=offering.getCourseOfferings().iterator();i.hasNext();) {
            CourseOffering co = (CourseOffering)i.next();
            ret.addAll(Exam.findAll(ExamOwner.sOwnerTypeCourse, co.getUniqueId()));
        }
        return ret;
    }

}
