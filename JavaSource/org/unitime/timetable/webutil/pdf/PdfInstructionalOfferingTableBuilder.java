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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;
import org.unitime.timetable.util.duration.DurationModel;
import org.unitime.timetable.webutil.RequiredTimeTable;
import org.unitime.timetable.webutil.WebInstructionalOfferingTableBuilder;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;




/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class PdfInstructionalOfferingTableBuilder extends WebInstructionalOfferingTableBuilder {
	protected PdfWriter iWriter = null;
	protected Document iDocument = null;
	protected PdfPTable iPdfTable = null;
	
	protected static String indent = "    ";
	protected static String LABEL = " ";

    public PdfInstructionalOfferingTableBuilder() {
        super();
    }
    
    protected static Color sBorderColor = Color.BLACK;
    protected static Color sBgColorClass = Color.WHITE;
    protected static Color sBgColorSubpart = new Color(225,225,225);
    protected static Color sBgColorConfig = new Color(200,200,200);
    protected static Color sBgColorOffering = new Color(200,200,200);
    protected static Color sBgColorHeader = Color.WHITE;
    protected Color iBgColor = Color.WHITE;
    
    protected Color sEnableColor = Color.BLACK;
    protected Color sDisableColor = Color.GRAY;
    
	public PdfPCell createCell() {
		PdfPCell cell = new PdfPCell();
		cell.setBorderColor(sBorderColor);
		cell.setPadding(3);
		cell.setBorderWidth(0);
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBackgroundColor(iBgColor);
		return cell;
	}
	
	public void addText(PdfPCell cell, String text) {
		addText(cell, text, false, false, Element.ALIGN_LEFT, sEnableColor, true);
	}
	
	public void addText(PdfPCell cell, String text, boolean bold, int orientation) {
		addText(cell, text, bold, false, orientation, sEnableColor, true);
	}

	public void addText(PdfPCell cell, String text, int orientation) {
		addText(cell, text, false, false, orientation, sEnableColor, true);
	}
	
	public void addText(PdfPCell cell, String text, boolean bold, boolean italic,  int orientation, Color color, boolean newLine) {
		if (text==null) return;
		if (cell.getPhrase()==null) {
			Chunk ch = new Chunk(text, PdfFont.getFont(bold, italic, color));
			cell.setPhrase(new Paragraph(ch));
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			cell.setHorizontalAlignment(orientation);
		} else {
			cell.getPhrase().add(new Chunk((newLine?"\n":"")+text, PdfFont.getFont(bold, italic, color)));
		}
	}
	
	public int getNrColumns() {
		int ret = 0;
    	if (isShowLabel()) ret+=1;
    	if (isShowDivSec()) ret+=1;
    	if (isShowDemand()) ret+=1;
    	if (isShowProjectedDemand()) ret+=1;
    	if (isShowLimit()) ret+=1;
    	if (isShowRoomRatio()) ret+=1;
    	if (isShowManager()) ret+=1;
    	if (isShowDatePattern()) ret+=1;
    	if (isShowMinPerWk()) ret+=1;
    	if (isShowTimePattern()) ret+=1;
    	if (isShowPreferences()) ret+=getPreferenceColumns();
    	if (isShowInstructorAssignment()) ret+=1;
    	if (isShowInstructor()) ret+=1;
    	if (getDisplayTimetable() && isShowTimetable()) ret+=3;
    	if (isShowTitle()) ret+=1;
    	if (isShowCredit()) ret+=1;
    	if (isShowSubpartCredit()) ret+=1;
    	if (isShowConsent()) ret+=1;
    	if (isShowSchedulePrintNote()) ret+=1;
    	if (isShowNote()) ret+=1;
    	if (isShowExam()) {
    	    if (isShowExamName()) ret+=1;
    	    if (isShowExamTimetable()) ret+=2;
    	}
    	return ret;
	}
	
	public float[] getWidths() {
		float[] width = new float[getNrColumns()]; 
		int idx = 0;
    	if (isShowLabel()) width[idx++] = 175f;
    	if (isShowDivSec()) width[idx++] = 80f;
    	if (isShowDemand()) width[idx++] = 60f;
    	if (isShowProjectedDemand()) width[idx++] = 65f;
    	if (isShowLimit()) width[idx++] = 50f;
    	if (isShowRoomRatio()) width[idx++] = 50f;
    	if (isShowManager()) width[idx++] = 75f;
    	if (isShowDatePattern()) width[idx++] = 100f;
    	if (isShowMinPerWk()) width[idx++] = 60f;
    	if (isShowTimePattern()) width[idx++] = 80f;
    	if (isShowPreferences()) {
			if (getGridAsText()) {
				width[idx++] = 200f;
			} else {
				width[idx++] = 100f;
			}
			width[idx++] = 150f;
			if (getDisplayDistributionPrefs()) {
				width[idx++] = 200f;
			}
			if (getDisplayInstructorPrefs()) {
				width[idx++] = 150f;
				width[idx++] = 150f;
			}
    	}
    	if (isShowInstructorAssignment()) width[idx++] = 100f;
    	if (isShowInstructor()) width[idx++] = 200f;
    	if (getDisplayTimetable() && isShowTimetable()) {
    		width[idx++] = 130f;
    		width[idx++] = 100f;
    		width[idx++] = 70f;
    	}
    	if (isShowTitle()) width[idx++] = 200f;
    	if (isShowCredit()) width[idx++] = 100f;
    	if (isShowSubpartCredit()) width[idx++] = 100f;
    	if (isShowConsent()) width[idx++] = 100f;
    	if (isShowSchedulePrintNote()) width[idx++] = 150f;
    	if (isShowNote()) width[idx++] = 300f;
        if (isShowExam()) {
            if (isShowExamName()) width[idx++] = 120f;
            if (isShowExamTimetable()) {
                width[idx++] = 120f;
                width[idx++] = 80f;
            }
        }
    	return width;
	}
	
    
    protected void pdfBuildTableHeader(Long sessionId) {
    	iBgColor = sBgColorHeader;
    	//first line
    	if (isShowLabel()) {
    		PdfPCell c = createCell();
    		addText(c, LABEL, true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowDivSec()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnExternalId(), true, Element.ALIGN_RIGHT);
    		iPdfTable.addCell(c);
    	}   	
    	if (isShowDemand()){
    		PdfPCell c = createCell();
    		if (StudentClassEnrollment.sessionHasEnrollments(sessionId)){
    			addText(c, MSG.columnDemand(), true, Element.ALIGN_RIGHT);
    		} else {
        		addText(c, MSG.columnLastDemand(), true, Element.ALIGN_RIGHT);    			
    		}
    		iPdfTable.addCell(c);
    	}
    	if (isShowProjectedDemand()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnProjectedDemand(), true, Element.ALIGN_RIGHT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowLimit()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnLimit(), true, Element.ALIGN_RIGHT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowRoomRatio()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnRoomRatio(), true, Element.ALIGN_RIGHT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowManager()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnManager(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowDatePattern()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnDatePattern(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowMinPerWk()){
    		PdfPCell c = createCell();
    		ClassDurationType dtype = ClassDurationType.findDefaultType(sessionId, null);
    		addText(c, dtype == null ? MSG.columnMinPerWk() : dtype.getLabel(), true, Element.ALIGN_RIGHT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowTimePattern()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnTimePattern(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowPreferences()){
    		PdfPCell c = createCell();
    		c.setColspan(getPreferenceColumns());
    		addText(c, "----" + MSG.columnPreferences() + "----", true, Element.ALIGN_CENTER);
    		iPdfTable.addCell(c);
    	}
    	if (isShowInstructorAssignment()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnTeachingLoad(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowInstructor()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnInstructor(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c);
    	}
    	if (getDisplayTimetable() && isShowTimetable()){
    		PdfPCell c = createCell();
    		c.setColspan(3);
    		addText(c, "--------" + MSG.columnTimetable() + "--------", true, Element.ALIGN_CENTER);
    		iPdfTable.addCell(c);
    	}
    	if (isShowTitle()) {
    		PdfPCell c = createCell();
    		addText(c, MSG.columnTitle(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c);
       }
    	if (isShowCredit()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnOfferingCredit(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowSubpartCredit()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnSubpartCredit(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowConsent()) {
    		PdfPCell c = createCell();
    		addText(c, MSG.columnConsent(), true, Element.ALIGN_CENTER);
    		iPdfTable.addCell(c);
    	}
    	if (isShowSchedulePrintNote()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnSchedulePrintNote(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowNote()){
    		PdfPCell c = createCell();
    		addText(c, MSG.columnNote(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c);
    	}
    	if (isShowExam()) {
    	    PdfPCell c = createCell();
    	    c.setColspan((isShowExamName()?1:0)+(isShowExamTimetable()?2:0));
            addText(c, "--------" + MSG.columnExam() + "--------", true, Element.ALIGN_CENTER);
            iPdfTable.addCell(c);
    	}
    	
    	//second line
    	if (isShowLabel()) {
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowDivSec()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}   	
    	if (isShowDemand()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowProjectedDemand()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowLimit()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowRoomRatio()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowManager()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowDatePattern()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowMinPerWk()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowTimePattern()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowPreferences()) {
    		PdfPCell c1 = createCell();
    		c1.setBorderWidthBottom(1);
    		addText(c1, MSG.columnTimePref(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c1);
    		PdfPCell c2 = createCell();
    		c2.setBorderWidthBottom(1);
    		addText(c2, MSG.columnAllRoomPref(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c2);
    		if (getDisplayDistributionPrefs()) {
        		PdfPCell c3 = createCell();
        		c3.setBorderWidthBottom(1);
        		addText(c3, MSG.columnDistributionPref(), true, Element.ALIGN_LEFT);
        		iPdfTable.addCell(c3);
    		}
    		if (getDisplayInstructorPrefs()) {
        		PdfPCell c4 = createCell();
        		c4.setBorderWidthBottom(1);
        		addText(c4, MSG.columnInstructorAttributePref(), true, Element.ALIGN_LEFT);
        		iPdfTable.addCell(c4);
        		PdfPCell c5 = createCell();
        		c5.setBorderWidthBottom(1);
        		addText(c5, MSG.columnInstructorPref(), true, Element.ALIGN_LEFT);
        		iPdfTable.addCell(c5);
    		}
    	}
    	if (isShowInstructorAssignment()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowInstructor()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (getDisplayTimetable() && isShowTimetable()){
    		PdfPCell c1 = createCell();
    		c1.setBorderWidthBottom(1);
    		addText(c1, MSG.columnAssignedTime(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c1);
    		PdfPCell c2 = createCell();
    		c2.setBorderWidthBottom(1);
    		addText(c2, MSG.columnAssignedRoom(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c2);
    		PdfPCell c3 = createCell();
    		c3.setBorderWidthBottom(1);
    		addText(c3, MSG.columnAssignedRoomCapacity(), true, Element.ALIGN_LEFT);
    		iPdfTable.addCell(c3);
    	}
    	if (isShowTitle()) {
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
       }
    	if (isShowCredit()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowSubpartCredit()) {
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowConsent()) {
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowSchedulePrintNote()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
    	if (isShowNote()){
    		PdfPCell c = createCell();
    		c.setBorderWidthBottom(1);
    		iPdfTable.addCell(c);
    	}
        if (isShowExam()) {
            if (isShowExamName()) {
                PdfPCell c = createCell();
                c.setBorderWidthBottom(1);
                addText(c, MSG.columnExamName(), true, Element.ALIGN_LEFT);
                iPdfTable.addCell(c);
            }
            if (isShowExamTimetable()) {
                PdfPCell c = createCell();
                c.setBorderWidthBottom(1);
                addText(c, MSG.columnExamPeriod(), true, Element.ALIGN_LEFT);
                iPdfTable.addCell(c);
                c = createCell();
                c.setBorderWidthBottom(1);
                addText(c, MSG.columnExamRoom(), true, Element.ALIGN_LEFT);
                iPdfTable.addCell(c);
            }
        }
    	iPdfTable.setHeaderRows(2);
   }

    private PdfPCell pdfSubjectAndCourseInfo(InstructionalOffering io, CourseOffering co) {
    	PdfPCell cell = createCell();
        addText(cell, (co != null ? co.getSubjectAreaAbbv()+" "+co.getCourseNbr():""), true, false, Element.ALIGN_LEFT, (co.isIsControl().booleanValue()?sEnableColor:sDisableColor), true);
    	InstructionalMethod im = (co != null && co.getInstructionalOffering().getInstrOfferingConfigs().size() == 1 ? co.getInstructionalOffering().getInstrOfferingConfigs().iterator().next().getInstructionalMethod() : null);
    	if (im != null) {
    		if (co.getCourseType() != null) {
    			addText(cell, " (" + co.getCourseType().getReference() + ", " + im.getReference() + ")", false, false, Element.ALIGN_LEFT, (co.isIsControl().booleanValue()?sEnableColor:sDisableColor), false);
    		} else {
    			addText(cell, " (" + im.getReference() + ")", false, false, Element.ALIGN_LEFT, (co.isIsControl().booleanValue()?sEnableColor:sDisableColor), false);
    		}
    	} else if (co.getCourseType() != null) {
			addText(cell, " (" + co.getCourseType().getReference() + ")", false, false, Element.ALIGN_LEFT, (co.isIsControl().booleanValue()?sEnableColor:sDisableColor), false);
    	}
        for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext(); ) {
        	CourseOffering tempCo = (org.unitime.timetable.model.CourseOffering) it.next();
            addText(cell,  indent+""+tempCo.getSubjectAreaAbbv()+" "+tempCo.getCourseNbr() + " " + (tempCo.getCourseType() != null ? " (" + tempCo.getCourseType().getReference() + ")" : ""), false, false, Element.ALIGN_LEFT, sDisableColor, true);
        }
        return cell;
    }  
    
    protected PdfPCell pdfBuildPrefGroupLabel(CourseOffering co, PreferenceGroup prefGroup, String indentSpaces, boolean isEditable, String prevLabel){
    	Color color = (isEditable?sEnableColor:sDisableColor);
        String label = prefGroup.toString();
        if (prefGroup instanceof Class_) {
        	Class_ aClass = (Class_) prefGroup;
        	label = aClass.getClassLabel(co);
		}
        if (prevLabel != null && label.equals(prevLabel)){
        	label = "";
        }
        PdfPCell cell = createCell();
    	addText(cell, indentSpaces+label, false, false, Element.ALIGN_LEFT, color, true);
        return cell;
    }
    
    protected PdfPCell pdfBuildDatePatternCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	Assignment a = null;
    	AssignmentPreferenceInfo p = null;
		if (getDisplayTimetable() && isShowTimetable() && classAssignment!=null && prefGroup instanceof Class_) {
			try {
				a = classAssignment.getAssignment((Class_)prefGroup);
				p = classAssignment.getAssignmentInfo((Class_)prefGroup);
			} catch (Exception e) {
				Debug.error(e);
			}
    	}
    	DatePattern dp = (a != null ? a.getDatePattern() : prefGroup.effectiveDatePattern());
    	PdfPCell cell = createCell();
    	if (dp!=null) {
    		Color color = (isEditable ? sEnableColor : sDisableColor);
			addText(cell,dp.getName(), false, false, Element.ALIGN_CENTER, color, true);
    		if (dp.getType() == DatePattern.sTypePatternSet && isEditable) {
    			boolean hasReq = false;
    			for (Iterator i=prefGroup.effectivePreferences(DatePatternPref.class).iterator(); i.hasNext();) {
    				Preference pref = (Preference)i.next();
    				if (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())) {
    					hasReq = true; break;
    				}
    			}
    			for (Iterator i=prefGroup.effectivePreferences(DatePatternPref.class).iterator(); i.hasNext();) {
    				Preference pref = (Preference)i.next();
    				if (!hasReq || PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog()))
    					addText(cell,pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText(), false, false, Element.ALIGN_CENTER, (!isEditable ? color : p == null ? pref.getPrefLevel().awtPrefcolor() : PreferenceLevel.int2awtColor(p.getDatePatternPref(), color)), true);
    			}
    		}
    	}
        return cell;
    }

    private PdfPCell pdfBuildTimePatternCell(PreferenceGroup prefGroup, boolean isEditable){
   		Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();
    	for (Iterator i=prefGroup.effectiveTimePatterns().iterator(); i.hasNext();) {
    		TimePattern tp = (TimePattern)i.next();
    		addText(cell, tp.getName(), false, false, Element.ALIGN_CENTER, color, true);  
    	}
        if (prefGroup instanceof Class_ && prefGroup.effectiveTimePatterns().isEmpty()) {
        	Class_ clazz = (Class_)prefGroup;
        	DurationModel dm = clazz.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
        	Integer ah = dm.getArrangedHours(clazz.getSchedulingSubpart().getMinutesPerWk(), clazz.effectiveDatePattern());
            if (ah == null) {
                addText(cell, "Arr Hrs", false, false, Element.ALIGN_CENTER, color, true);
            } else {
                addText(cell, "Arr "+ah+" Hrs", false, false, Element.ALIGN_CENTER, color, true);
            }
        }
        return cell;
    }
    
    private PdfPCell pdfBuildTimePrefCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	Color color = (isEditable?sEnableColor:sDisableColor);
		Assignment a = null;
		if (getDisplayTimetable() && isShowTimetable() && classAssignment!=null && prefGroup instanceof Class_) {
			try {
				a = classAssignment.getAssignment((Class_)prefGroup);
			} catch (Exception e) {
				Debug.error(e);
			}
    	}
		
		PdfPCell cell = createCell();
		
		for (Iterator i=prefGroup.effectivePreferences(TimePref.class).iterator(); i.hasNext();) {
			TimePref tp = (TimePref)i.next();
			RequiredTimeTable rtt = tp.getRequiredTimeTable(a == null ? null : a.getTimeLocation());
			if (getGridAsText()) {
				addText(cell, rtt.getModel().toString().replaceAll(", ", "\n"), false, false, Element.ALIGN_LEFT, color, true);
			} else {
				try {
					rtt.getModel().setDefaultSelection(getDefaultTimeGridSize());
					if (rtt.getModel().isExactTime()) {
						addText(cell, rtt.exactTime(false), false, false, Element.ALIGN_LEFT, color, true);
					} else {
						java.awt.Image awtImage = rtt.createBufferedImage(getTimeVertival());
						Image img = Image.getInstance(awtImage, Color.WHITE);
						Chunk ck = new Chunk(img, 0, 0);
						if (cell.getPhrase()==null) {
							cell.setPhrase(new Paragraph(ck));
							cell.setVerticalAlignment(Element.ALIGN_TOP);
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						} else {
							cell.getPhrase().add(ck);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
    	
    	return cell;
    	
    }
    private PdfPCell pdfBuildPreferenceCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, Class prefType, boolean isEditable){
    	if (!isEditable) return createCell();
    	Color color = (isEditable?sEnableColor:sDisableColor);

    	if (TimePref.class.equals(prefType)) {
    		return pdfBuildTimePrefCell(classAssignment, prefGroup, isEditable);
    	} else if (DistributionPref.class.equals(prefType)) {
        	PdfPCell cell = createCell();
        	for (Iterator i=prefGroup.effectivePreferences(prefType).iterator();i.hasNext();) {
        		DistributionPref pref = (DistributionPref)i.next();
        		addText(cell, pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText(true, true, " (", ", ",")").replaceAll("&lt;","<").replaceAll("&gt;",">"), false, false, Element.ALIGN_LEFT, (!isEditable ? color : pref.getPrefLevel().awtPrefcolor()), true);
        	}
    		return cell ;
    	} else {
        	PdfPCell cell = createCell();
    		if (!prefGroup.isInstructorAssignmentNeeded() && (InstructorPref.class.equals(prefType) || InstructorAttributePref.class.equals(prefType))) {
    			return cell;
    		}
        	for (Iterator i=prefGroup.effectivePreferences(prefType).iterator();i.hasNext();) {
        		Preference pref = (Preference)i.next();
        		addText(cell, pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText(), false, false, Element.ALIGN_LEFT, (!isEditable ? color : pref.getPrefLevel().awtPrefcolor()), true);
        	}
    		return cell ;
    	}
    	
    }
    
    private PdfPCell pdfBuildPreferenceCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, Class[] prefTypes, boolean isEditable){
    	if (!isEditable) return createCell();
    	Color color = (isEditable?sEnableColor:sDisableColor);

    	PdfPCell cell = createCell();
    	boolean noRoomPrefs = false;
    	if (prefGroup instanceof Class_ && ((Class_)prefGroup).getNbrRooms().intValue()==0) {
    		 noRoomPrefs = true;
    	}
        if (prefGroup instanceof SchedulingSubpart && ((SchedulingSubpart)prefGroup).getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue())
            noRoomPrefs = true;
    	for (int i=0;i<prefTypes.length;i++) {
    		Class prefType = prefTypes[i];
    		if (noRoomPrefs) {
    			if (//prefType.equals(RoomPref.class) || 
    				prefType.equals(RoomGroupPref.class) || 
    				prefType.equals(RoomFeaturePref.class) || 
    				prefType.equals(BuildingPref.class))
    				continue;
    		}
    		for (Iterator j=prefGroup.effectivePreferences(prefType).iterator();j.hasNext();) {
    			Preference pref = (Preference)j.next();
    			addText(cell, pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText(), false, false, Element.ALIGN_LEFT, (!isEditable ? color : pref.getPrefLevel().awtPrefcolor()), true);
    		}
    	}
    	if (noRoomPrefs && cell.getPhrase()==null)
    		addText(cell, "N/A", false, true, Element.ALIGN_LEFT, color, true);
		return cell;
    }

    private PdfPCell pdfBuildPrefGroupDemand(PreferenceGroup prefGroup, boolean isEditable){
    	if (prefGroup instanceof Class_) {
			Class_ c = (Class_) prefGroup;
			if (StudentClassEnrollment.sessionHasEnrollments(c.getSessionId())){
				PdfPCell tc = createCell();
				if (c.getEnrollment() != null){
					addText(tc, c.getEnrollment().toString());
				} else {
					addText(tc, "0");
				}
				tc.setHorizontalAlignment(Element.ALIGN_RIGHT);
				return(tc);
			}
		}
    	return createCell();
    }
    
    private PdfPCell pdfBuildPrefGroupProjectedDemand(PreferenceGroup prefGroup, boolean isEditable){
    	PdfPCell cell = createCell();
    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_)prefGroup;
    		SectioningInfo i = c.getSectioningInfo();
    		if (i != null && i.getNbrExpectedStudents() != null) {
    			addText(cell, String.valueOf(Math.round(Math.max(0.0, c.getEnrollment() + i.getNbrExpectedStudents()))));
    			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    		}
    	}
    	return cell;
    }
    
    private PdfPCell pdfBuildLimit(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	Color color = (isEditable?sEnableColor:sDisableColor);

    	PdfPCell cell = createCell();

    	if (prefGroup instanceof SchedulingSubpart){
	    	SchedulingSubpart ss = (SchedulingSubpart) prefGroup;
	    	boolean unlimited = ss.getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue();
	    	if (!unlimited) {
		    	int limit = (ss.getLimit()==null?0:ss.getLimit().intValue());
		    	int maxExpCap = ss.getMaxExpectedCapacity(); 
		    	if (limit==maxExpCap)
		    		addText(cell, String.valueOf(limit), false, false, Element.ALIGN_RIGHT, color, true);
		    	else
		    		addText(cell, limit+"-"+maxExpCap, false, false, Element.ALIGN_RIGHT, color, true);
	    	} 
    	} else if (prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
	    	boolean unlimited = aClass.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue();
	    	if (!unlimited) {
	    		String limitString = null;
                Assignment a = null;
                try {
                    if (classAssignment!=null) a = classAssignment.getAssignment(aClass);
                } catch (Exception e) {}
                if (a==null) {
                    if (aClass.getExpectedCapacity() != null){
                        limitString = aClass.getExpectedCapacity().toString();
                        if (aClass.getMaxExpectedCapacity() != null && !aClass.getMaxExpectedCapacity().equals(aClass.getExpectedCapacity())){
                            limitString = limitString + "-" + aClass.getMaxExpectedCapacity().toString();
                        }
                    } else {
                        limitString = "0";
                        if (aClass.getMaxExpectedCapacity() != null && aClass.getMaxExpectedCapacity().intValue() != 0){
                            limitString = limitString + "-" + aClass.getMaxExpectedCapacity().toString();
                        }
                    }
                } else {
                    limitString = ""+aClass.getClassLimit(classAssignment);
                }
	    		addText(cell, limitString, false, false, Element.ALIGN_RIGHT, color, true);
	    	}
    	} 
    	
        return cell;
    }
    
    private PdfPCell pdfBuildDivisionSection(CourseOffering co, PreferenceGroup prefGroup, boolean isEditable){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();
    	
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		// String divSec = aClass.getDivSecNumber();
    		String divSec = aClass.getClassSuffix(co);
    		if (divSec!=null)
    			addText(cell, divSec, false, false, Element.ALIGN_RIGHT, color, true);
    	}
    	
        return cell;
    }
    
    protected PdfPCell pdfBuildInstructorAssignment(PreferenceGroup prefGroup, boolean isEditable){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();
    	
    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_) prefGroup;
    		if (c.isInstructorAssignmentNeeded()) {
    			addText(cell, (c.effectiveNbrInstructors() > 1 ? c.effectiveNbrInstructors() + " \u00d7 " : "") +
    					Formats.getNumberFormat("0.##").format(c.effectiveTeachingLoad()) + " " + MSG.teachingLoadUnits(),
    					false, false, Element.ALIGN_RIGHT, color, false);
    		} else if (c.getSchedulingSubpart().isInstructorAssignmentNeeded()) {
    			addText(cell, MSG.cellNoInstructorAssignment(), false, false, Element.ALIGN_RIGHT, color, false);
    		}
    	} else if (prefGroup instanceof SchedulingSubpart) {
    		SchedulingSubpart ss = (SchedulingSubpart)prefGroup;
    		if (ss.isInstructorAssignmentNeeded()) {
    			addText(cell, (ss.getNbrInstructors() != null && ss.getNbrInstructors() > 1 ? ss.getNbrInstructors() + " \u00d7 " : "") +
    					Formats.getNumberFormat("0.##").format(ss.getTeachingLoad()) + " " + MSG.teachingLoadUnits(),
    					false, false, Element.ALIGN_RIGHT, color, false);
    		}
    	}
    	
        return cell;
    }

    protected PdfPCell pdfBuildInstructor(PreferenceGroup prefGroup, boolean isEditable){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();
    	
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
        	TreeSet sortedInstructors = new TreeSet(new InstructorComparator());
        	sortedInstructors.addAll(aClass.getClassInstructors());
    		for (Iterator i=sortedInstructors.iterator(); i.hasNext();) {
    			ClassInstructor ci = (ClassInstructor)i.next();
        		String label = ci.getInstructor().getName(getInstructorNameFormat());
        		boolean italic = !aClass.isDisplayInstructor().booleanValue();
        		boolean bold = ci.isLead().booleanValue();
        		addText(cell, label, bold, italic, Element.ALIGN_LEFT, color, true);
    		}
    	}
    	
        return cell;
    }

    private PdfPCell pdfBuildCredit(PreferenceGroup prefGroup, boolean isEditable){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();

    	if (prefGroup instanceof SchedulingSubpart) {
    		SchedulingSubpart ss = (SchedulingSubpart) prefGroup;
    		if (ss.getCredit() != null) {
    			addText(cell, ss.getCredit().creditAbbv(), false, false, Element.ALIGN_LEFT, color, true);
    		}   		
    	}
    	
        return cell;
    }

    private PdfPCell pdfBuildSchedulePrintNote(PreferenceGroup prefGroup, boolean isEditable, UserContext user) {
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();

    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_) prefGroup;
    		if (c.getSchedulePrintNote()!=null) {
    			if (c.getSchedulePrintNote().length() <= 20 || user == null || CommonValues.NoteAsFullText.eq(user.getProperty(UserProperty.SchedulePrintNoteDisplay))){
    				addText(cell, c.getSchedulePrintNote(), false, false, Element.ALIGN_LEFT, color, true);
    			} else {
    				addText(cell, c.getSchedulePrintNote().substring(0,20) + "...", false, false, Element.ALIGN_LEFT, color, true);   				
    			}
    		}
    	}
    	
        return cell;
    }

    private PdfPCell pdfBuildSchedulePrintNote(InstructionalOffering io, boolean isEditable, UserContext user){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();

    	StringBuffer note = new StringBuffer("");
		Set s = io.getCourseOfferings();
		for (Iterator i=s.iterator(); i.hasNext(); ) {
			CourseOffering coI = (CourseOffering) i.next();
			if (coI.getScheduleBookNote()!=null && coI.getScheduleBookNote().trim().length()>0) {
				if (note.length()>0)
					note.append("\n");
				if (coI.getScheduleBookNote().length() <= 20 || Constants.showCrsOffrAsFullText(user)){
					note.append(coI.getScheduleBookNote());
				} else {
					note.append(coI.getScheduleBookNote().substring(0, 20) + "...");
				}
			}
		}
		
		addText(cell, note.toString(), false, false, Element.ALIGN_LEFT, color, true);
        return(cell);
    }    
    
    private PdfPCell pdfBuildNote(PreferenceGroup prefGroup, boolean isEditable, UserContext user){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();

    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_) prefGroup;
    		if (c.getNotes()!=null) {
    			if (c.getNotes().length() <= 30  || user == null || CommonValues.NoteAsFullText.eq(user.getProperty(UserProperty.ManagerNoteDisplay))){
    				addText(cell, c.getNotes(), false, false, Element.ALIGN_LEFT, color, true);
    			} else {
    				addText(cell, c.getNotes().substring(0, 30) + "...", false, false, Element.ALIGN_LEFT, color, true);
    			}
    		}
    	}
    	
        return cell;
    }

    private PdfPCell pdfBuildManager(PreferenceGroup prefGroup, boolean isEditable){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();

    	Department managingDept = null;
    	if (prefGroup instanceof Class_) {
    		managingDept = ((Class_)prefGroup).getManagingDept();
    	} else if (prefGroup instanceof SchedulingSubpart) {
    		managingDept = ((SchedulingSubpart)prefGroup).getManagingDept();
    	}
    	if (managingDept!=null) {
    		addText(cell, managingDept.getShortLabel(), false, false, Element.ALIGN_LEFT, color, true);
    	}

        return cell;
    }

    private PdfPCell pdfBuildMinPerWeek(PreferenceGroup prefGroup, boolean isEditable){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();

    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		String suffix = "";
    		ClassDurationType dtype = aClass.getSchedulingSubpart().getInstrOfferingConfig().getEffectiveDurationType();
    		if (dtype != null && !dtype.equals(aClass.getSchedulingSubpart().getSession().getDefaultClassDurationType())) {
    			suffix = " " + dtype.getAbbreviation();
    		}
    		addText(cell, aClass.getSchedulingSubpart().getMinutesPerWk() + suffix, false, false, Element.ALIGN_RIGHT, color, true);
    	} else if (prefGroup instanceof SchedulingSubpart) {
    		SchedulingSubpart aSchedulingSubpart = (SchedulingSubpart) prefGroup;
    		String suffix = "";
    		ClassDurationType dtype = aSchedulingSubpart.getInstrOfferingConfig().getEffectiveDurationType();
    		if (dtype != null && !dtype.equals(aSchedulingSubpart.getSession().getDefaultClassDurationType())) {
    			suffix = " " + dtype.getAbbreviation();
    		}
    		addText(cell, aSchedulingSubpart.getMinutesPerWk() + suffix, false, false, Element.ALIGN_RIGHT, color, true);
    	} 

        return cell;
    }

    private PdfPCell pdfBuildRoomLimit(PreferenceGroup prefGroup, boolean isEditable, boolean classLimitDisplayed){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();

    	if (prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
    		if (aClass.getNbrRooms()!=null && aClass.getNbrRooms().intValue()!=1) {
    			if (aClass.getNbrRooms().intValue()==0)
    				addText(cell, "N/A", false, true, Element.ALIGN_RIGHT, color, true);
    			else {
    				String text = aClass.getNbrRooms().toString();
    				text += " at ";
    				if (aClass.getRoomRatio() != null)
    					text += sRoomRatioFormat.format(aClass.getRoomRatio().floatValue());
    				else
    					text += "0";
    				addText(cell, text, false, false, Element.ALIGN_RIGHT, color, true);
    			}
    		} else {
    			if (aClass.getRoomRatio() != null){
    				if (classLimitDisplayed && aClass.getRoomRatio().equals(new Float(1.0))){
    					addText(cell, "", false, false, Element.ALIGN_RIGHT, color, true);
    				} else {
    					addText(cell, sRoomRatioFormat.format(aClass.getRoomRatio().floatValue()), false, false, Element.ALIGN_RIGHT, color, true);
    				}
    			} else {
    				if (aClass.getExpectedCapacity() == null){
    					addText(cell, "", false, false, Element.ALIGN_RIGHT, color, true);
    				} else {
    					addText(cell, "0", false, false, Element.ALIGN_RIGHT, color, true);
    				}
    			}
    		}
    	}
    	
        return cell;
    }
    
    private PdfPCell pdfBuildAssignedTime(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();

    	if (classAssignment!=null && prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		Assignment a = null;
    		AssignmentPreferenceInfo p = null;
    		try {
    			a = classAssignment.getAssignment(aClass);
    			p = classAssignment.getAssignmentInfo((Class_)prefGroup);
    		} catch (Exception e) {
    			Debug.error(e);
    		}
    		if (a!=null) {
    			StringBuffer sb = new StringBuffer();
   				Enumeration<Integer> e = a.getTimeLocation().getDays();
   				while (e.hasMoreElements()){
   					sb.append(CONSTANTS.shortDays()[e.nextElement()]);
   				}
   				sb.append(" ");
   				sb.append(a.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm()));
   				sb.append("-");
   				sb.append(a.getTimeLocation().getEndTimeHeader(CONSTANTS.useAmPm()));
   				addText(cell, sb.toString(), false, false, Element.ALIGN_LEFT, (p == null || !isEditable ? color : PreferenceLevel.int2awtColor(p.getTimePreference(), color)), true);
    		} 
    	}
    	
        return cell;
    }
   
    private PdfPCell pdfBuildAssignedRoom(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable) {
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();

    	if (classAssignment!=null && prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		Assignment a = null;
    		AssignmentPreferenceInfo p = null;
    		try {
    			a= classAssignment.getAssignment(aClass);
    			p = classAssignment.getAssignmentInfo((Class_)prefGroup);
    		} catch (Exception e) {
    			Debug.error(e);
    		}
    		if (a!=null) {
    			StringBuffer sb = new StringBuffer();
	    		Iterator it2 = a.getRooms().iterator();
	    		while (it2.hasNext()){
	    			Location room = (Location)it2.next();
	    			sb.append(room.getLabel());
	    			if (it2.hasNext()){
	        			sb.append("\n");
	        		} 
	    		}
	    		addText(cell, sb.toString(), false, false, Element.ALIGN_LEFT, (p == null || !isEditable ? color : PreferenceLevel.int2awtColor(p.getTimePreference(), color)), true);
    		}
    	}
    	
        return cell;
    }
    private PdfPCell pdfBuildAssignedRoomCapacity(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable) {
    	Color color = (isEditable?sEnableColor:sDisableColor);
    	PdfPCell cell = createCell();

    	if (classAssignment!=null && prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
    		Assignment a = null;
   			try {
   				a = classAssignment.getAssignment(aClass);
   			} catch (Exception e) {
   				Debug.error(e);
   			}
	   		if (a!=null) {
	   			StringBuffer sb = new StringBuffer();
				Iterator it2 = a.getRooms().iterator();
				while (it2.hasNext()){
					sb.append(((Location) it2.next()).getCapacity());
					if (it2.hasNext()){
    					sb.append("\n");
    				} 
				}
				addText(cell, sb.toString(), false, false, Element.ALIGN_LEFT, color, true);
    		}
    	}

    	return cell;
    }
    
    private PdfPCell pdfBuildExamName(TreeSet exams, boolean isEditable) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            sb.append(exam.getLabel());
            if (i.hasNext()) sb.append("\n");
        }
        Color color = (isEditable?sEnableColor:sDisableColor);
        PdfPCell cell = createCell();
        addText(cell, sb.toString(), false, false, Element.ALIGN_LEFT, color, true);
        return cell;
    }

    private PdfPCell pdfBuildExamPeriod(ExamAssignmentProxy examAssignment, TreeSet exams, boolean isEditable) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            if (examAssignment!=null && examAssignment.getExamTypeId().equals(exam.getExamType().getUniqueId())) {
                ExamAssignment ea = examAssignment.getAssignment(exam.getUniqueId());
                if (ea==null && !isShowExamName()) continue;
                sb.append(ea==null?"":ea.getPeriodAbbreviation());
            } else {
                if (exam.getAssignedPeriod()==null && !isShowExamName()) continue;
                sb.append(exam.getAssignedPeriod()==null?"":exam.getAssignedPeriod().getAbbreviation());
            }
            if (i.hasNext()) sb.append("\n");
        }
        Color color = (isEditable?sEnableColor:sDisableColor);
        PdfPCell cell = createCell();
        addText(cell, sb.toString(), false, false, Element.ALIGN_LEFT, color, true);
        return cell;
    }

    private PdfPCell pdfBuildExamRoom(ExamAssignmentProxy examAssignment, TreeSet exams, boolean isEditable) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            if (examAssignment!=null && examAssignment.getExamTypeId().equals(exam.getExamType().getUniqueId())) {
                ExamAssignment ea = examAssignment.getAssignment(exam.getUniqueId());
                if (ea==null && !isShowExamName()) continue;
                sb.append(ea==null?"":ea.getRoomsName(", "));
            } else {
                if (exam.getAssignedPeriod()==null && !isShowExamName()) continue;
                for (Iterator j=new TreeSet(exam.getAssignedRooms()).iterator();j.hasNext();) {
                    Location location = (Location)j.next();
                    sb.append(location.getLabel());
                    if (j.hasNext()) sb.append(", ");
                }
            }
            if (i.hasNext()) sb.append("\n");
        }
        Color color = (isEditable?sEnableColor:sDisableColor);
        PdfPCell cell = createCell();
        addText(cell, sb.toString(), false, false, Element.ALIGN_LEFT, color, true);
        return cell;
    }

    
    //NOTE: if changing column order column order must be changed in
    //		buildTableHeader, addInstrOffrRowsToTable, buildClassOrSubpartRow, and buildConfigRow
    protected void pdfBuildClassOrSubpartRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, CourseOffering co, PreferenceGroup prefGroup, String indentSpaces, boolean isEditable, String prevLabel, SessionContext context){
    	boolean classLimitDisplayed = false;
    	if (isShowLabel()){
	        iPdfTable.addCell(pdfBuildPrefGroupLabel(co, prefGroup, indentSpaces, isEditable, prevLabel));
    	} 
    	if (isShowDivSec()){
    		iPdfTable.addCell(pdfBuildDivisionSection(co, prefGroup, isEditable));
    	}
    	if (isShowDemand()){
    		iPdfTable.addCell(pdfBuildPrefGroupDemand(prefGroup, isEditable));
    	} 
    	if (isShowProjectedDemand()){
    		iPdfTable.addCell(pdfBuildPrefGroupProjectedDemand(prefGroup, isEditable));
    	} 
    	if (isShowLimit()){
    		classLimitDisplayed = true;
    		iPdfTable.addCell(pdfBuildLimit(classAssignment, prefGroup, isEditable));
       	} 
    	if (isShowRoomRatio()){
    		iPdfTable.addCell(pdfBuildRoomLimit(prefGroup, isEditable, classLimitDisplayed));
       	} 
    	if (isShowManager()){
    		iPdfTable.addCell(pdfBuildManager(prefGroup, isEditable));
     	} 
    	if (isShowDatePattern()){
    		iPdfTable.addCell(pdfBuildDatePatternCell(classAssignment, prefGroup, isEditable));
     	} 
    	if (isShowMinPerWk()){
    		iPdfTable.addCell(pdfBuildMinPerWeek(prefGroup, isEditable));
       	} 
    	if (isShowTimePattern()){
    		iPdfTable.addCell(pdfBuildTimePatternCell(prefGroup, isEditable));
    	} 
    	if (isShowPreferences()){
    		iPdfTable.addCell(pdfBuildPreferenceCell(classAssignment,prefGroup, TimePref.class, isEditable));
    		iPdfTable.addCell(pdfBuildPreferenceCell(classAssignment,prefGroup, new Class[] {RoomPref.class, BuildingPref.class, RoomFeaturePref.class, RoomGroupPref.class} , isEditable));
    		if (getDisplayDistributionPrefs()) {
    			iPdfTable.addCell(pdfBuildPreferenceCell(classAssignment,prefGroup, DistributionPref.class, isEditable));
    		}
    		if (getDisplayInstructorPrefs()) {
    			iPdfTable.addCell(pdfBuildPreferenceCell(classAssignment,prefGroup, InstructorAttributePref.class, isEditable));
    			iPdfTable.addCell(pdfBuildPreferenceCell(classAssignment,prefGroup, InstructorPref.class, isEditable));
    		}
    	}
    	if (isShowInstructorAssignment()){
    		iPdfTable.addCell(pdfBuildInstructorAssignment(prefGroup, isEditable));
    	}
    	if (isShowInstructor()){
    		iPdfTable.addCell(pdfBuildInstructor(prefGroup, isEditable));
    	}
    	if (getDisplayTimetable() && isShowTimetable()){
    		iPdfTable.addCell(pdfBuildAssignedTime(classAssignment, prefGroup, isEditable));
    		iPdfTable.addCell(pdfBuildAssignedRoom(classAssignment, prefGroup, isEditable));
    		iPdfTable.addCell(pdfBuildAssignedRoomCapacity(classAssignment, prefGroup, isEditable));
    	} 
    	if (isShowTitle()) {
    		iPdfTable.addCell(createCell());
    	}
    	if (isShowCredit()){
    		iPdfTable.addCell(createCell());     		
    	} 
    	if (isShowSubpartCredit()){
    		iPdfTable.addCell(pdfBuildCredit(prefGroup, isEditable));     		
    	} 
    	if (isShowConsent()){
	        iPdfTable.addCell(createCell());
    	} 
    	if (isShowSchedulePrintNote()){
    		iPdfTable.addCell(pdfBuildSchedulePrintNote(prefGroup, isEditable, context.getUser()));     		
    	} 
    	if (isShowNote()){
    		iPdfTable.addCell(pdfBuildNote(prefGroup, isEditable, context.getUser()));     		
    	}
        if (isShowExam()) {
            TreeSet exams = new TreeSet();
            if (prefGroup instanceof Class_) {
                exams = getExams((Class_)prefGroup);
            }
            for (Iterator<Exam> i = exams.iterator(); i.hasNext(); ) {
            	if (!context.hasPermission(i.next(), Right.ExaminationView))
            		i.remove();
            }
            if (isShowExamName()) {
                iPdfTable.addCell(pdfBuildExamName(exams, isEditable));
            }
            if (isShowExamTimetable()) {
                iPdfTable.addCell(pdfBuildExamPeriod(examAssignment, exams, isEditable));
                iPdfTable.addCell(pdfBuildExamRoom(examAssignment, exams, isEditable));
            }
        }
    	
    }
    
    private void pdfBuildSchedulingSubpartRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, CourseOffering co, SchedulingSubpart ss, String indentSpaces, SessionContext context){
        boolean isEditable = context.hasPermission(ss, Right.SchedulingSubpartDetail);
        iBgColor = sBgColorSubpart;
        pdfBuildClassOrSubpartRow(classAssignment, examAssignment, co, ss, indentSpaces, isEditable, null, context);
    }
    
    private void pdfBuildSchedulingSubpartRows(Vector subpartIds, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, CourseOffering co, SchedulingSubpart ss, String indentSpaces, SessionContext context){
    	if (subpartIds!=null) subpartIds.add(ss.getUniqueId());
        pdfBuildSchedulingSubpartRow(classAssignment, examAssignment, co, ss, indentSpaces, context);
        Set childSubparts = ss.getChildSubparts();
        
		if (childSubparts != null && !childSubparts.isEmpty()){
		    
		    ArrayList childSubpartList = new ArrayList(childSubparts);
		    Collections.sort(childSubpartList, new SchedulingSubpartComparator());
            Iterator it = childSubpartList.iterator();
            SchedulingSubpart child = null;
            
            while (it.hasNext()){              
                child = (SchedulingSubpart) it.next();
                pdfBuildSchedulingSubpartRows(subpartIds, classAssignment, examAssignment, co, child, indentSpaces + indent, context);
            }
        }
    }
 
    protected void pdfBuildClassRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, int ct, CourseOffering co, Class_ aClass, String indentSpaces, SessionContext context, String prevLabel){
        boolean isEditable = context.hasPermission(aClass, Right.ClassDetail);
        iBgColor = sBgColorClass;
        pdfBuildClassOrSubpartRow(classAssignment, examAssignment, co, aClass, indentSpaces, isEditable && !aClass.isCancelled(), prevLabel, context);
    }
    
    private void pdfBuildClassRows(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, int ct, CourseOffering co, Class_ aClass, String indentSpaces, SessionContext context, String prevLabel){

        pdfBuildClassRow(classAssignment, examAssignment, ct, co, aClass, indentSpaces, context, prevLabel);
    	Set childClasses = aClass.getChildClasses();

    	if (childClasses != null && !childClasses.isEmpty()){
        
    	    ArrayList childClassesList = new ArrayList(childClasses);
            Collections.sort(childClassesList, getClassComparator());
            
            Iterator it = childClassesList.iterator();
            Class_ child = null;
            String previousLabel = aClass.htmlLabel();
            while (it.hasNext()){              
                child = (Class_) it.next();
                pdfBuildClassRows(classAssignment, examAssignment, ct, co, child, indentSpaces + indent, context, previousLabel);
            }
        }
    }


	protected void pdfBuildConfigRow(Vector subpartIds, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, CourseOffering co, InstrOfferingConfig ioc, SessionContext context, boolean printConfigLine) {
		iBgColor = sBgColorConfig;
	    boolean isEditable = context.hasPermission(ioc.getInstructionalOffering(), Right.InstructionalOfferingDetail);
	    Color color = (isEditable?sEnableColor:sDisableColor);
	    String configName = ioc.getName();
	    boolean unlimited = ioc.isUnlimitedEnrollment().booleanValue();
	    boolean hasConfig = false;
		if (printConfigLine) {
        	if (isShowLabel()) {
        	    if (configName==null || configName.trim().length()==0)
        	        configName = ioc.getUniqueId().toString();
        	    PdfPCell cell = createCell();
        	    addText(cell, indent + (ioc.getInstructionalMethod() == null ? MSG.labelConfiguration(configName) : MSG.labelConfigurationWithInstructionalMethod(configName, ioc.getInstructionalMethod().getReference())),
        	    		false, false, Element.ALIGN_LEFT, color, true);
        	    iPdfTable.addCell(cell);
        	}
        	if (isShowDivSec()){
        	    iPdfTable.addCell(createCell());
    		}
        	if (isShowDemand()){
        	    iPdfTable.addCell(createCell());
    		}
        	if (isShowProjectedDemand()){
        	    iPdfTable.addCell(createCell());
    		} 
        	if (isShowLimit()){
        	    PdfPCell cell = createCell();
        	    addText(cell, (unlimited?"inf":ioc.getLimit().toString()), false, false, Element.ALIGN_RIGHT, color, true);
        	    iPdfTable.addCell(cell);
        	} 
        	if (isShowRoomRatio()){
        	    iPdfTable.addCell(createCell());
        	} 
        	if (isShowManager()){
        	    iPdfTable.addCell(createCell());
        	} 
        	if (isShowDatePattern()){
        	    iPdfTable.addCell(createCell());
	       	} 
        	if (isShowMinPerWk()){
        	    iPdfTable.addCell(createCell());
        	} 
        	if (isShowTimePattern()){
        	    iPdfTable.addCell(createCell());
        	} 
        	if (isShowPreferences()){
		        for (int j = 0; j < getPreferenceColumns(); j++) {
	        	    iPdfTable.addCell(createCell());
		        }
        	}
        	if (isShowInstructorAssignment()){
        	    iPdfTable.addCell(createCell());
        	}
        	if (isShowInstructor()){
        	    iPdfTable.addCell(createCell());
        	} 
        	if (getDisplayTimetable() && isShowTimetable()){
        		iPdfTable.addCell(createCell());
        		iPdfTable.addCell(createCell());
        		iPdfTable.addCell(createCell());
        	} 
        	if (isShowTitle()) {
        		iPdfTable.addCell(createCell());
        	}
        	if (isShowCredit()){
        	    iPdfTable.addCell(createCell());
        	} 
        	if (isShowSubpartCredit()){
        	    iPdfTable.addCell(createCell());
        	} 
        	if (isShowConsent()){
    	        iPdfTable.addCell(createCell());
        	} 
        	if (isShowSchedulePrintNote()){
        	    iPdfTable.addCell(createCell());
        	} 
        	if (isShowNote()){
        	    iPdfTable.addCell(createCell());
        	}

            if (isShowExam()) {
                TreeSet exams = new TreeSet(Exam.findAll(ExamOwner.sOwnerTypeConfig,ioc.getUniqueId()));
                if (isShowExamName()) {
                    iPdfTable.addCell(pdfBuildExamName(exams, isEditable));
                }
                for (Iterator<Exam> i = exams.iterator(); i.hasNext(); ) {
                	if (!context.hasPermission(i.next(), Right.ExaminationView))
                		i.remove();
                }
                if (isShowExamTimetable()) {
                    iPdfTable.addCell(pdfBuildExamPeriod(examAssignment, exams, isEditable));
                    iPdfTable.addCell(pdfBuildExamRoom(examAssignment, exams, isEditable));
                }
            }

	        hasConfig = true;
		}
		
        ArrayList subpartList = new ArrayList(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());
        Iterator it = subpartList.iterator();
        SchedulingSubpart ss = null;
        while(it.hasNext()){
            ss = (SchedulingSubpart) it.next();
            if (ss.getParentSubpart() == null){
                pdfBuildSchedulingSubpartRows(subpartIds, classAssignment, examAssignment, co, ss, (hasConfig?indent+indent:indent) , context);
            }
        }
        it = subpartList.iterator();
        int ct = 0;
        String prevLabel = null;
        while (it.hasNext()) {   	
			ss = (SchedulingSubpart) it.next();
			if (ss.getParentSubpart() == null) {
				if (ss.getClasses() != null) {
					Vector classes = new Vector(ss.getClasses());
					Collections.sort(classes,getClassComparator());
					Iterator cIt = classes.iterator();					
					Class_ c = null;
					while (cIt.hasNext()) {
						c = (Class_) cIt.next();
						pdfBuildClassRows(classAssignment, examAssignment, ++ct, co, c, indent, context, prevLabel);
						prevLabel = c.htmlLabel();
					}
				}
			}
		}
   }

    private void pdfBuildConfigRows(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, CourseOffering co, Set instrOfferingConfigs, SessionContext context, boolean printConfigLine) {
        Iterator it = instrOfferingConfigs.iterator();
        InstrOfferingConfig ioc = null;
        while (it.hasNext()){
            ioc = (InstrOfferingConfig) it.next();
            pdfBuildConfigRow(null, classAssignment, examAssignment, co, ioc, context, printConfigLine && instrOfferingConfigs.size()>1);
        }
    }

    private void pdfAddInstrOffrRowsToTable(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, InstructionalOffering io, Long subjectAreaId, SessionContext context){
    	iBgColor = sBgColorOffering;
        CourseOffering co = io.findSortCourseOfferingForSubjectArea(subjectAreaId);
        boolean isEditable = context.hasPermission(co.getInstructionalOffering(), Right.InstructionalOfferingDetail); 
        boolean isManagedAs = !co.isIsControl().booleanValue();
        Color color = (isEditable ? sEnableColor : sDisableColor);
        
    	if (isShowLabel()){
    		iPdfTable.addCell(pdfSubjectAndCourseInfo(io, co));
    	}
    	if (isShowDivSec()){
    		iPdfTable.addCell(createCell());
		}
    	if (isShowDemand()){
    	    PdfPCell cell = createCell();
    	    if (StudentClassEnrollment.sessionHasEnrollments(io.getSessionId())){
        	    addText(cell, (io.getEnrollment() != null?io.getEnrollment().toString(): "0"), false, false, Element.ALIGN_RIGHT, (co.isIsControl()?color:sDisableColor), true);
    	    } else {
        	    addText(cell, (io.getDemand() != null?io.getDemand().toString(): "0"), false, false, Element.ALIGN_RIGHT, (co.isIsControl()?color:sDisableColor), true);    	    	
    	    }
    	    iPdfTable.addCell(cell);
		}
    	if (isShowProjectedDemand()){
    	    PdfPCell cell = createCell();
    	    addText(cell, (io.getProjectedDemand() != null?io.getProjectedDemand().toString(): "0"), false, false, Element.ALIGN_RIGHT, (co.isIsControl()?color:sDisableColor), true);
    	    iPdfTable.addCell(cell);
		} 
    	if (isShowLimit()){
			boolean unlimited = false;
			for (Iterator x=io.getInstrOfferingConfigs().iterator();!unlimited && x.hasNext();)
				if ((((InstrOfferingConfig)x.next())).isUnlimitedEnrollment().booleanValue())
					unlimited = true;
    	    PdfPCell cell = createCell();
    	    addText(cell, (unlimited?"inf":io.getLimit()==null?"0":io.getLimit().toString()), false, false, Element.ALIGN_RIGHT, (co.isIsControl()?color:sDisableColor), true);
    	    iPdfTable.addCell(cell);
    	} 
    	int emptyCels = 0;
    	if (isShowRoomRatio()){
    		emptyCels ++;
    	} 
    	if (isShowManager()){
    		emptyCels ++;
    	}
    	if (isShowDatePattern()){
    		emptyCels ++;
       	}
    	if (isShowMinPerWk()) {
    		emptyCels ++;
    	} 
    	if (isShowTimePattern()){
    		emptyCels ++;
    	}
    	if (isShowPreferences()){
    		emptyCels += getPreferenceColumns();
    	}
    	if (isShowInstructorAssignment()){
    		emptyCels ++;
    	}
    	if (isShowInstructor()){
    		emptyCels ++;
    	}
    	if (getDisplayTimetable() && isShowTimetable()){
    		emptyCels += 3;
    	} 
    	if (emptyCels>0) {
    		PdfPCell managedCell = createCell();
            if (isManagedAs) {
            	if (!isShowTitle()) {
            		if (co.getTitle()!=null && co.getTitle().length()>0) {
            			addText(managedCell, "     " + co.getTitle()+" (" + MSG.crossListManagedAs(io.getControllingCourseOffering().getCourseName()) + ")", true, false, Element.ALIGN_LEFT, sDisableColor, false);
            		} else {
            			addText(managedCell, "     " + MSG.crossListManagedAs(io.getControllingCourseOffering().getCourseName()), true, false, Element.ALIGN_LEFT, sDisableColor, true);
            		}
                    for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
                    	CourseOffering x = (CourseOffering)it.next();
            			addText(managedCell, (x.getTitle()==null?"":"     "+x.getTitle()), false, false, Element.ALIGN_LEFT, sDisableColor, true);
                    }
            	} else {
        			addText(managedCell, "     " + MSG.crossListManagedAs(io.getControllingCourseOffering().getCourseName()), true, false, Element.ALIGN_LEFT, sDisableColor, true);
            	}
            } else {
            	if (!isShowTitle()) {
            		addText(managedCell, (co.getTitle()==null?"":"      "+co.getTitle()), true, false, Element.ALIGN_LEFT, color, true);
                    for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
                    	CourseOffering x = (CourseOffering)it.next();
            			addText(managedCell, (x.getTitle()==null?"":"      "+x.getTitle()), false, false, Element.ALIGN_LEFT, sDisableColor, true);
                    }
            	}
            }
            managedCell.setColspan(emptyCels);
            iPdfTable.addCell(managedCell);

    	}
    	if (isShowTitle()) {
       	    PdfPCell titleCell = createCell();
       	    addText(titleCell, (co.getTitle()!=null ? "      "+co.getTitle() : ""), false, false, Element.ALIGN_LEFT, color, true);
            for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
            	CourseOffering x = (CourseOffering)it.next();
            	addText(titleCell, (x.getTitle()!=null ? "      "+x.getTitle() : ""), false, false, Element.ALIGN_LEFT, sDisableColor, true);
            }
    	    iPdfTable.addCell(titleCell);
    	}
    	if (isShowCredit()){
    	    PdfPCell cell = createCell();
    	    addText(cell, (co.getCredit() != null ? co.getCredit().creditAbbv() : ""), true, false, Element.ALIGN_LEFT, isManagedAs ? sDisableColor : color, true);
    	    for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
            	CourseOffering x = (CourseOffering)it.next();
            	addText(cell, (x.getCredit() != null ? x.getCredit().creditAbbv() : ""), false, false, Element.ALIGN_LEFT, sDisableColor, true);
    	    }
    	    iPdfTable.addCell(cell);
    	}
    	if (isShowSubpartCredit()) {
    		iPdfTable.addCell(createCell());
    	}
    	if (isShowConsent()){
    	    PdfPCell cell = createCell();
    	    addText(cell, co.getConsentType() != null ? co.getConsentType().getAbbv() : MSG.noConsentRequired(), true, false, Element.ALIGN_LEFT, isManagedAs ? sDisableColor : color, true);
    	    for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
            	CourseOffering x = (CourseOffering)it.next();
            	addText(cell, (x.getConsentType() != null ? x.getConsentType().getAbbv() : MSG.noConsentRequired()), false, false, Element.ALIGN_LEFT, sDisableColor, true);
    	    }
    	    iPdfTable.addCell(cell);
    	} 
    	if (isShowSchedulePrintNote()){
    		iPdfTable.addCell(pdfBuildSchedulePrintNote(io, isEditable, context.getUser()));     		
    	}
    	if (isShowNote()){
    		iPdfTable.addCell(createCell());     		
    	}
        if (isShowExam()) {
            TreeSet exams = new TreeSet(Exam.findAll(ExamOwner.sOwnerTypeOffering,io.getUniqueId()));
            for (Iterator i=io.getCourseOfferings().iterator();i.hasNext();) {
                CourseOffering cox = (CourseOffering)i.next();
                exams.addAll(Exam.findAll(ExamOwner.sOwnerTypeCourse,cox.getUniqueId()));
            }
            if (io.getInstrOfferingConfigs().size()==1) {
                for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();) {
                    InstrOfferingConfig ioc = (InstrOfferingConfig)i.next();
                    exams.addAll(Exam.findAll(ExamOwner.sOwnerTypeConfig,ioc.getUniqueId()));
                }
            }
            for (Iterator<Exam> i = exams.iterator(); i.hasNext(); ) {
            	if (!context.hasPermission(i.next(), Right.ExaminationView))
            		i.remove();
            }
            if (isShowExamName()) {
                iPdfTable.addCell(pdfBuildExamName(exams, isEditable));
            }
            if (isShowExamTimetable()) {
                iPdfTable.addCell(pdfBuildExamPeriod(examAssignment, exams, isEditable));
                iPdfTable.addCell(pdfBuildExamRoom(examAssignment, exams, isEditable));
            }
        }

        if (io.getInstrOfferingConfigs() != null & !io.getInstrOfferingConfigs().isEmpty()){
        	TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
        	configs.addAll(io.getInstrOfferingConfigs());
            pdfBuildConfigRows(classAssignment, examAssignment, io.getControllingCourseOffering(), configs, context, true);
        }
    }

    public void pdfTableForInstructionalOffering(
    		OutputStream out,
    		ClassAssignmentProxy classAssignment, 
    		ExamAssignmentProxy examAssignment,
            Long instructionalOfferingId, 
            SessionContext context,
            Comparator classComparator) throws Exception{
    	
    	if (instructionalOfferingId != null && context != null){
	        InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(instructionalOfferingId);
	        Long subjectAreaId = io.getControllingCourseOffering().getSubjectArea().getUniqueId();
	        
	        // Get Configuration
	        TreeSet ts = new TreeSet();
	        ts.add(io);
	        WebInstructionalOfferingTableBuilder iotbl = new WebInstructionalOfferingTableBuilder();
	        iotbl.setDisplayDistributionPrefs(false);
	        setVisibleColumns(COLUMNS);
	        
	    	iDocument = new Document(PageSize.A4, 30f, 30f, 30f, 30f); 
			iWriter = PdfEventHandler.initFooter(iDocument, out);

		    pdfTableForInstructionalOfferings(out,
				        classAssignment, examAssignment,
				        ts, subjectAreaId, context, false, false, classComparator);
		    
		    iDocument.close();
    	}
    }
    
    public void pdfTableForInstructionalOfferings(
    		OutputStream out,
            ClassAssignmentProxy classAssignment,
            ExamAssignmentProxy examAssignment,
            InstructionalOfferingListForm form, 
            String[] subjectAreaIds, 
            SessionContext context,
            boolean displayHeader,
            boolean allCoursesAreGiven) throws Exception{
    	
    	setVisibleColumns(form);
    	
    	iDocument = new Document(PageSize.A4, 30f, 30f, 30f, 30f); 
		iWriter = PdfEventHandler.initFooter(iDocument, out);

    	for (String subjectAreaId: subjectAreaIds) {
        	pdfTableForInstructionalOfferings(out, classAssignment, examAssignment,
        			form.getInstructionalOfferings(Long.valueOf(subjectAreaId)), 
        			Long.valueOf(subjectAreaId),
        			context,
        			displayHeader, allCoursesAreGiven,
        			new ClassCourseComparator(form.getSortBy(), classAssignment, false));
    	}
   	
		iDocument.close();
    }
    
    protected void pdfTableForInstructionalOfferings(
    		OutputStream out,
            ClassAssignmentProxy classAssignment, 
            ExamAssignmentProxy examAssignment,
            TreeSet insructionalOfferings, 
            Long subjectAreaId, 
            SessionContext context,
            boolean displayHeader, boolean allCoursesAreGiven,
            Comparator classComparator) throws Exception {
    	
    	if (insructionalOfferings == null) return;
    	
    	SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(subjectAreaId);
    	
    	if (classComparator!=null)
    		setClassComparator(classComparator);
    	
    	if (isShowTimetable()) {
    		boolean hasTimetable = false;
    		if (context.hasPermission(Right.ClassAssignments) && classAssignment != null) {
            	if (classAssignment instanceof CachedClassAssignmentProxy) {
            		Vector allClasses = new Vector();
    				for (Iterator i=insructionalOfferings.iterator();!hasTimetable && i.hasNext();) {
    					InstructionalOffering io = (InstructionalOffering)i.next();
    					for (Iterator j=io.getInstrOfferingConfigs().iterator();!hasTimetable && j.hasNext();) {
    						InstrOfferingConfig ioc = (InstrOfferingConfig)j.next();
    						for (Iterator k=ioc.getSchedulingSubparts().iterator();!hasTimetable && k.hasNext();) {
    							SchedulingSubpart ss = (SchedulingSubpart)k.next();
    							for (Iterator l=ss.getClasses().iterator();l.hasNext();) {
    								Class_ clazz = (Class_)l.next();
    								allClasses.add(clazz);
    							}
    						}
    					}
    				}
            		((CachedClassAssignmentProxy)classAssignment).setCache(allClasses);
            		hasTimetable = !classAssignment.getAssignmentTable(allClasses).isEmpty();
            	} else {
    				for (Iterator i=insructionalOfferings.iterator();!hasTimetable && i.hasNext();) {
    					InstructionalOffering io = (InstructionalOffering)i.next();
    					for (Iterator j=io.getInstrOfferingConfigs().iterator();!hasTimetable && j.hasNext();) {
    						InstrOfferingConfig ioc = (InstrOfferingConfig)j.next();
    						for (Iterator k=ioc.getSchedulingSubparts().iterator();!hasTimetable && k.hasNext();) {
    							SchedulingSubpart ss = (SchedulingSubpart)k.next();
    							for (Iterator l=ss.getClasses().iterator();l.hasNext();) {
    								Class_ clazz = (Class_)l.next();
    								if (classAssignment.getAssignment(clazz)!=null) {
    									hasTimetable = true; break;
    								}
    							}
            				}
            			}
            		}
            	}
    		}
    		setDisplayTimetable(hasTimetable);
    	}
    	
        if (isShowExam())
            setShowExamTimetable(examAssignment != null || Exam.hasTimetable(context.getUser().getCurrentAcademicSessionId()));

        ArrayList notOfferedOfferings = new ArrayList();
        ArrayList offeredOfferings = new ArrayList();
        ArrayList offeringIds = new ArrayList();
        
        Iterator it = insructionalOfferings.iterator();
        InstructionalOffering io = null;
        boolean hasOfferedCourses = false;
        boolean hasNotOfferedCourses = false;
		setUserSettings(context.getUser());
        
         while (it.hasNext()){
            io = (InstructionalOffering) it.next();
            if (io.isNotOffered() == null || io.isNotOffered().booleanValue()){
            	hasNotOfferedCourses = true;
            	notOfferedOfferings.add(io);
            } else {
            	hasOfferedCourses = true;
            	offeredOfferings.add(io);
            }
        }
         
    	float[] widths = getWidths();
    	float totalWidth = 0;
    	for (int i=0;i<widths.length;i++)
    		totalWidth += widths[i];
    	
    	
		iDocument.setPageSize(new Rectangle(60f+totalWidth,60f+0.77f*totalWidth));
         
        if (hasOfferedCourses || allCoursesAreGiven) {
        	iPdfTable = new PdfPTable(getWidths());
        	iPdfTable.setWidthPercentage(100);
        	iPdfTable.getDefaultCell().setPadding(3);
        	iPdfTable.getDefaultCell().setBorderWidth(0);
        	iPdfTable.setSplitRows(false);

        	if(displayHeader) {
        		if (!iDocument.isOpen())
        			iDocument.open();
        		else
        			iDocument.newPage();
   		    	iDocument.add(new Paragraph(MSG.labelOfferedCourses(subjectArea.getSubjectAreaAbbreviation()), PdfFont.getBigFont(true)));
    		}
    		pdfBuildTableHeader(context.getUser().getCurrentAcademicSessionId());
                  
            if (hasOfferedCourses){
                it = offeredOfferings.iterator();
                while (it.hasNext()){
                    io = (InstructionalOffering) it.next();
                    offeringIds.add(io.getUniqueId());
                    	pdfAddInstrOffrRowsToTable(classAssignment, examAssignment, io, subjectAreaId, context);            	
                }
            } else {
                if(displayHeader)
                	iDocument.add(new Paragraph(MSG.errorNoCoursesOffered(subjectArea.getSubjectAreaAbbreviation()), PdfFont.getFont(true, false, Color.RED))); 
            }
            
            iDocument.add(iPdfTable);
        }
        
        if (hasNotOfferedCourses || allCoursesAreGiven) {
        	iPdfTable = new PdfPTable(getWidths());
        	iPdfTable.setWidthPercentage(100);
        	iPdfTable.getDefaultCell().setPadding(3);
        	iPdfTable.getDefaultCell().setBorderWidth(0);
        	iPdfTable.setSplitRows(false);

        	if(displayHeader) {
   	        	iDocument.newPage();
   				iDocument.add(new Paragraph(MSG.labelNotOfferedCourses(subjectArea.getSubjectAreaAbbreviation()), PdfFont.getBigFont(true)));
            }
            pdfBuildTableHeader(context.getUser().getCurrentAcademicSessionId());
            
            if (hasNotOfferedCourses){
                it = notOfferedOfferings.iterator();
                while (it.hasNext()){
                    io = (InstructionalOffering) it.next();
                    offeringIds.add(io.getUniqueId());
                    pdfAddInstrOffrRowsToTable(classAssignment, examAssignment, io, subjectAreaId, context);            	
                }
            } else {
                if(displayHeader)
                	iDocument.add(new Paragraph(MSG.errorAllCoursesOffered(subjectArea.getSubjectAreaAbbreviation()), PdfFont.getFont()));
            }
            
    		iDocument.add(iPdfTable);
        }
    }

}
