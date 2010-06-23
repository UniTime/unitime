/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.PdfEventHandler;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;


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
	
	
	public File pdfTableForClasses(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, ClassListForm form, User user) {
		FileOutputStream out = null;
		try {
			setVisibleColumns(form);
	        
			TreeSet classes = (TreeSet) form.getClasses();
			if (isShowTimetable()) {
				boolean hasTimetable = false;
				try {
					TimetableManager manager = TimetableManager.getManager(user);
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
			
	        if (isShowExam())
	            setShowExamTimetable(examAssignment!=null || Exam.hasTimetable((Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME)));
        
			File file = ApplicationProperties.getTempFile("classes", "pdf");
    	
			float[] widths = getWidths();
			float totalWidth = 0;
			for (int i=0;i<widths.length;i++)
				totalWidth += widths[i];

			iDocument = new Document(new Rectangle(60f+totalWidth,60f+0.77f*totalWidth), 30f, 30f, 30f, 30f); 

			out = new FileOutputStream(file);
			iWriter = PdfEventHandler.initFooter(iDocument, out);
			iDocument.open();
        

			Class_ c = null;
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
					pdfBuildTableHeader(Session.getCurrentAcadSession(user) == null?null:Session.getCurrentAcadSession(user).getUniqueId());
				}
				pdfBuildClassRow(classAssignment, examAssignment, ++ct, c, "", user, prevLabel);
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
	
    protected PdfPCell pdfBuildPrefGroupLabel(PreferenceGroup prefGroup, String indentSpaces, boolean isEditable, String prevLabel) {
    	if (prefGroup instanceof Class_) {
    		BaseColor color = (isEditable?BaseColor.BLACK:BaseColor.GRAY);
    		String label = prefGroup.toString();
        	Class_ aClass = (Class_) prefGroup;
        	label = aClass.getClassLabel();
        	if (prevLabel != null && label.equals(prevLabel)){
        		label = "";
        	}
        	PdfPCell cell = createCell();
        	addText(cell, indentSpaces+label, false, false, Element.ALIGN_LEFT, color, true);
        	return cell;
    	} else return super.pdfBuildPrefGroupLabel(prefGroup, indentSpaces, isEditable, null);
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
