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
package org.unitime.timetable.webutil.pdf;

import java.awt.Color;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.TreeSet;

import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
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
	        InstructionalMethod im = aClass.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod();
        	if (im != null)
	        	addText(cell, " (" + im.getReference() + ")", false, false, Element.ALIGN_LEFT, color, false);
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
    
    @Override
    protected PdfPCell pdfBuildNote(PreferenceGroup prefGroup, boolean isEditable, UserContext user){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();

    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_) prefGroup;
    		String offeringNote = c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getNotes();
    		String classNote = c.getNotes();
    		String note = (offeringNote == null || offeringNote.isEmpty() ? classNote : offeringNote + (classNote == null || classNote.isEmpty() ? "" : "\n" + classNote));
    		if (note != null && !note.isEmpty()) {
    			if (note.length() <= 30  || user == null || CommonValues.NoteAsFullText.eq(user.getProperty(UserProperty.ManagerNoteDisplay))){
    				addText(cell, note, false, false, Element.ALIGN_LEFT, color, true);
    			} else {
    				if (classNote != null && !classNote.isEmpty()) note = classNote;
    				addText(cell, (note.length() <= 30 ? note : note.substring(0, 30) + "..."), false, false, Element.ALIGN_LEFT, color, true);
    			}
    		}
    	}
    	
        return cell;
    }

}
