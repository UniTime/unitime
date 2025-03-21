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
package org.unitime.timetable.server.courses;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingConfigInterface;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingDetailResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.ImageInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LinkInteface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.ImageGenerator;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy.AssignmentInfo;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.duration.DurationModel;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.RequiredTimeTable;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
public class InstructionalOfferingTableBuilder extends TableBuilder {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static Formats.Format<Date> sDateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
	protected static DecimalFormat sRoomRatioFormat = new DecimalFormat("0.00");
	
    protected String disabledColor = "gray";
    
    private boolean showLabel;
    private boolean showDivSec;
    private boolean showDemand;
    private boolean showProjectedDemand;
    private boolean showMinPerWk;
    private boolean showLimit;
    private boolean showSnapshotLimit;
    private boolean showRoomRatio;
    private boolean showFundingDepartment;
    private boolean showManager;
    private boolean showDatePattern;
    private boolean showTimePattern;
    private boolean showPreferences;
    private boolean showInstructor;
    private boolean showTimetable;
    private boolean showCredit;
    private boolean showSubpartCredit;
    private boolean showSchedulePrintNote;
    private boolean showNote;
    private boolean showTitle;
    private boolean showConsent;
    private boolean showExam;
    private boolean showExamName=true;
    private boolean showExamTimetable;  
    private boolean showInstructorAssignment;
    private boolean showLms;
    private boolean showWaitlistMode;
    private String filterWaitlist;
    
	private boolean iDisplayDistributionPrefs = true;
    private boolean iDisplayTimetable = true;
    private boolean iDisplayConflicts = false;
    private boolean iDisplayInstructorPrefs = true;
    private boolean iDisplayDatePatternDifferentWarning = false;
    
    private Comparator iClassComparator = new ClassComparator(ClassComparator.COMPARE_BY_ITYPE);
    
    // Set whether edit/modify config buttons are displayed
    private boolean displayConfigOpButtons = false;
    
    public InstructionalOfferingTableBuilder(SessionContext context, String backType, String backId) {
    	super(context, backType, backId);
    }
    
    private Boolean iSessionHasEnrollments = null;
    protected boolean sessionHasEnrollments(Long sessionId) {
    	if (iSessionHasEnrollments == null)
    		iSessionHasEnrollments = StudentClassEnrollment.sessionHasEnrollments(sessionId);
    	return iSessionHasEnrollments;
    }
    
    public void setDisplayConfigOpButtons(boolean displayConfigOpButtons) {
        this.displayConfigOpButtons = displayConfigOpButtons;
    }
    public boolean getDisplayConfigOpButtons() {
        return this.displayConfigOpButtons;
    }
    
    public void setDisplayDistributionPrefs(boolean displayDistributionPrefs) {
    	iDisplayDistributionPrefs = displayDistributionPrefs;
    }
    public boolean getDisplayDistributionPrefs() { return iDisplayDistributionPrefs; }
    public void setDisplayInstructorPrefs(boolean displayInstructorPrefs) {
    	iDisplayInstructorPrefs = displayInstructorPrefs;
    }
    public boolean getDisplayInstructorPrefs() { return iDisplayInstructorPrefs && isShowInstructorAssignment(); }
    public void setDisplayTimetable(boolean displayTimetable) {
    	iDisplayTimetable = displayTimetable;
    }
    public boolean getDisplayTimetable() { return iDisplayTimetable; }
    
    public void setDisplayConflicts(boolean displayConflicts) {
    	iDisplayConflicts = displayConflicts;
    }
    public boolean getDisplayConflicts() { return iDisplayConflicts; }

    public void setDisplayDatePatternDifferentWarning(boolean displayDatePatternDifferentWarning) {
    	iDisplayDatePatternDifferentWarning = displayDatePatternDifferentWarning;
    }
    public boolean getDisplayDatePatternDifferentWarning() { return iDisplayDatePatternDifferentWarning; }

    public int getPreferenceColumns() {
    	if (isShowPreferences())
    		return 2 + (getDisplayDistributionPrefs() ? 1 : 0) + (getDisplayInstructorPrefs() ? 2 : 0);
    	else
    		return (getDisplayInstructorPrefs() ? 2 : 0);
    }
    public Boolean iShowOriginalDivSecs = null;
    public void setShowOriginalDivSecs(boolean showOriginalDivSecs) {
    	iShowOriginalDivSecs = showOriginalDivSecs;
    }
    public boolean isShowOriginalDivSecs() {
    	if (iShowOriginalDivSecs == null) return ApplicationProperty.ClassSetupEditExternalIds.isTrue();
    	return iShowOriginalDivSecs;
    }
     
    public boolean isShowConsent() {
        return showConsent;
    }
    public void setShowConsent(boolean showConsent) {
        this.showConsent = showConsent;
    }
        
    public boolean isShowTitle() {
        return showTitle;
    }
    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public boolean isShowExam() {
        return showExam;
    }
    public void setShowExam(boolean showExam) {
        this.showExam = showExam;
    }

    public boolean isShowExamName() {
        return showExamName;
    }
    public void setShowExamName(boolean showExamName) {
        this.showExamName = showExamName;
    }
    public boolean isShowExamTimetable() {
        return showExamTimetable;
    }
    public void setShowExamTimetable(boolean showExamTimetable) {
        this.showExamTimetable = showExamTimetable;
    }
    
    public boolean isShowFundingDepartment() {
    	if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
    		return showFundingDepartment;
    	} else {
    		return false;
    	}
    }
    public void setShowFundingDepartment(boolean showFundingDepartment) {
        this.showFundingDepartment = showFundingDepartment;
    }
    
    public boolean isShowInstructorAssignment() { return showInstructorAssignment; }
    public void setShowInstructorAssignment(boolean showInstructorAssignment) { this.showInstructorAssignment = showInstructorAssignment; }
    
    public boolean isFilterWaitlist() { return "W".equals(filterWaitlist); }
    public boolean isFilterNonWaitlist() { return "N".equals(filterWaitlist); }
    public boolean isFilterCoursesAllowingReScheduling() { return "R".equals(filterWaitlist); }
    public boolean isFilterNonWaitedCoursesAllowingReScheduling() { return "Z".equals(filterWaitlist); }
    public boolean isFilterCoursesNotAllowingReScheduling() { return "X".equals(filterWaitlist); }
    
    
    public void setFilterWaitlist(String filterWaitlist) { this.filterWaitlist = filterWaitlist; }

    public boolean isShowLms() {
		return showLms;
	}
	public void setShowLms(boolean showLms) {
		this.showLms = showLms;
	}
	
	public boolean isShowWaitlistMode() {
		return showWaitlistMode;
	}
	public void setShowWaitlistMode(boolean showWaitlistMode) {
		this.showWaitlistMode = showWaitlistMode;
	}


    protected LineInterface initRow(boolean isHeaderRow){
    	LineInterface row = new LineInterface();
        if (isHeaderRow) row.setBgColor("#f1f3f9");
        return row;
    }
    
    protected CellInterface headerCell(String content) {
    	return headerCell(content, 1, 1);
    }
    
    protected CellInterface headerCell(String content, int rowSpan, int colSpan){
    	CellInterface cell = new CellInterface();
    	if (rowSpan > 1) cell.setRowSpan(rowSpan);
    	if (colSpan > 1) cell.setColSpan(colSpan);
    	cell.setHorizontalAlignment(Alignment.BOTTOM);
    	cell.setClassName("WebTableHeader");
    	cell.setText(content);
    	return cell;
     }
    
    private CellInterface initCell(boolean isEditable, int cols){
        return initCell(isEditable, cols, false);
    }

    private CellInterface initCell(boolean isEditable, int cols, boolean nowrap){
    	CellInterface cell = new CellInterface();
        if (cols > 1) cell.setColSpan(cols);
        if (nowrap) cell.setNoWrap(true);
        if (!isEditable) cell.setColor(disabledColor);
        return cell;
    }

    protected CellInterface initNormalCell(String text, boolean isEditable){
        return (initColSpanCell(text, isEditable, 1));
    }
    
    private CellInterface initColSpanCell(String text, boolean isEditable, int cols){
        CellInterface cell = initCell(isEditable, cols);
        cell.setText(text);
        return (cell);
        
    } 
    protected void buildTableHeader(TableInterface table, Long sessionId, String durationColName){
    	LineInterface row = new LineInterface();
    	LineInterface row2 = new LineInterface();
    	CellInterface cell = null;
    	if (isSimple()) {
        	if (isShowLabel()) row.addCell(headerCell(null).setWidth(175));
        	if (isShowDivSec()) row.addCell(headerCell(MSG.columnExternalId()).setWidth(80));
        	if (isShowDemand()) {
        		if (sessionHasEnrollments(sessionId))
        			row.addCell(headerCell(MSG.columnDemand()).setTextAlignment(Alignment.RIGHT).setWidth(60));
        		else
        			row.addCell(headerCell(MSG.columnLastDemand()).setTextAlignment(Alignment.RIGHT).setWidth(60));
        	}
        	if (isShowProjectedDemand())
        		row.addCell(headerCell(MSG.columnProjectedDemand()).setTextAlignment(Alignment.RIGHT).setWidth(65));
        	if (isShowLimit())
        		row.addCell(headerCell(MSG.columnLimit()).setTextAlignment(Alignment.RIGHT).setWidth(50));
        	if (isShowSnapshotLimit())
        		row.addCell(headerCell(MSG.columnSnapshotLimit()).setTextAlignment(Alignment.RIGHT).setWidth(50));
        	if (isShowRoomRatio())
        		row.addCell(headerCell(MSG.columnRoomRatio()).setTextAlignment(Alignment.RIGHT).setWidth(50));
        	if (isShowManager()){
        		cell = this.headerCell(MSG.columnManager(), 1, 1).setWidth(75);
        		row.addCell(cell);
        	}
        	if (isShowFundingDepartment())
        		row.addCell(headerCell(MSG.columnFundingDepartment()).setWidth(75));
        	if (isShowDatePattern())
        		row.addCell(headerCell(MSG.columnDatePattern()).setWidth(100));
        	if (isShowMinPerWk())
        		row.addCell(headerCell(durationColName).setTextAlignment(Alignment.RIGHT).setWidth(60));
        	if (isShowTimePattern())
        		row.addCell(headerCell(MSG.columnTimePattern()).setWidth(80));
        	if (isShowPreferences()) {
        		row.addCell(headerCell(MSG.columnTimePref()).setWidth(getGridAsText()?200:100));
        		row.addCell(headerCell(MSG.columnAllRoomPref()).setWidth(150));
        		if (getDisplayDistributionPrefs())
        			row.addCell(headerCell(MSG.columnDistributionPref()).setWidth(200));
        		if (getDisplayInstructorPrefs()) {
            		row.addCell(headerCell(MSG.columnInstructorAttributePref()).setWidth(150));
            		row.addCell(headerCell(MSG.columnInstructorPref()).setWidth(150));
        		}
        	} else if (getDisplayInstructorPrefs()) {
        		row.addCell(headerCell(MSG.columnInstructorAttributePref()).setWidth(150));
        		row.addCell(headerCell(MSG.columnInstructorPref()).setWidth(150));
        	}
        	if (isShowInstructorAssignment()) 
        		row.addCell(headerCell(MSG.columnTeachingLoad()).setWidth(100));
        	if (isShowInstructor())
        		row.addCell(headerCell(MSG.columnInstructor()).setWidth(200));
        	if (getDisplayTimetable() && isShowTimetable()){
        		row.addCell(headerCell(MSG.columnAssignedTime()).setWidth(130));
        		row.addCell(headerCell(MSG.columnAssignedRoom()).setWidth(100));
        		row.addCell(headerCell(MSG.columnAssignedRoomCapacity()).setTextAlignment(Alignment.RIGHT).setWidth(70));
        	}
        	if (isShowTitle())
        		row.addCell(headerCell(MSG.columnTitle()).setWidth(200));
        	if (isShowCredit())
        		row.addCell(headerCell(MSG.columnOfferingCredit()).setWidth(100).setTextAlignment(Alignment.RIGHT));
        	if (isShowSubpartCredit())
        		row.addCell(headerCell(MSG.columnSubpartCredit()).setWidth(100).setTextAlignment(Alignment.RIGHT));
        	if (isShowConsent())
        		row.addCell(headerCell(MSG.columnConsent()).setWidth(100));
        	if (isShowSchedulePrintNote())
        		row.addCell(headerCell(getSchedulePrintNoteLabel()).setWidth(150));
        	if (isShowNote())
        		row.addCell(headerCell(MSG.columnNote()).setWidth(300));
        	if (isShowExam()) {
                if (isShowExamName())
                	row.addCell(headerCell(MSG.columnExam()).setWidth(120));
                if (isShowExamTimetable()) {
                	row.addCell(headerCell(MSG.columnExamPeriod()).setWidth(120));
                	row.addCell(headerCell(MSG.columnExamRoom()).setWidth(80));
                }
        	}
        	if (isShowLms())
        		row.addCell(headerCell(MSG.columnLms()).setWidth(100));
        	if (isShowWaitlistMode())
        		row.addCell(headerCell(MSG.columnWaitlistMode()).setWidth(75));
    	} else {
        	if (isShowLabel()){
        		cell = this.headerCell(null, 2, 1);
        		row.addCell(cell);
        	}
        	if (isShowDivSec()){
        		cell = this.headerCell(MSG.columnExternalId(), 2, 1);
        		row.addCell(cell);
        	}   	
        	if (isShowDemand()){
        		if (sessionHasEnrollments(sessionId)){
            		cell = this.headerCell(MSG.columnDemand(), 2, 1);
        		} else {
            		cell = this.headerCell((MSG.columnLastDemand()), 2, 1);    			
        		}
        		cell.setTextAlignment(Alignment.RIGHT);
        		row.addCell(cell);
        	}
        	if (isShowProjectedDemand()){
        		cell = this.headerCell(MSG.columnProjectedDemand(), 2, 1);
        		cell.setTextAlignment(Alignment.RIGHT);
        		row.addCell(cell);
        	}
        	if (isShowLimit()){
        		cell = this.headerCell(MSG.columnLimit(), 2, 1);
        		cell.setTextAlignment(Alignment.RIGHT);
        		row.addCell(cell);
        	}
        	if (isShowSnapshotLimit()){
        		cell = this.headerCell(MSG.columnSnapshotLimit(), 2, 1);
        		cell.setTextAlignment(Alignment.RIGHT);
        		row.addCell(cell);
        	}
        	if (isShowRoomRatio()){
        		cell = this.headerCell(MSG.columnRoomRatio(), 2, 1);
        		cell.setTextAlignment(Alignment.RIGHT);
        		row.addCell(cell);
        	}
        	if (isShowManager()){
        		cell = this.headerCell(MSG.columnManager(), 2, 1);
        		row.addCell(cell);
        	}
        	if (isShowFundingDepartment()){
        		cell = this.headerCell(MSG.columnFundingDepartment(), 2, 1);
        		row.addCell(cell);
        	}
        	if (isShowDatePattern()){
        		cell = this.headerCell(MSG.columnDatePattern(), 2, 1);
        		row.addCell(cell);
        	}
        	if (isShowMinPerWk()){
        		cell = this.headerCell(durationColName, 2, 1);
        		cell.setTextAlignment(Alignment.RIGHT);
        		row.addCell(cell);
        	}
        	if (isShowTimePattern()){
        		cell = this.headerCell(MSG.columnTimePattern(), 2, 1);
        		row.addCell(cell);
        	}
        	if (isShowPreferences()){
        		cell = headerCell("----" + MSG.columnPreferences() + "----", 1, getPreferenceColumns());
        		cell.setClassName("WebTableHeaderFirstRow");
        		cell.setTextAlignment(Alignment.CENTER);
        		row.addCell(cell);
    	    	cell = headerCell(MSG.columnTimePref(), 1, 1);
        		cell.setClassName("WebTableHeaderSecondRow");
        		row2.addCell(cell);
        		cell = headerCell(MSG.columnAllRoomPref(), 1, 1);
        		cell.setClassName("WebTableHeaderSecondRow");
        		row2.addCell(cell);
        		if (getDisplayDistributionPrefs()) {
        			cell = headerCell(MSG.columnDistributionPref(), 1, 1);
            		cell.setClassName("WebTableHeaderSecondRow");
            		row2.addCell(cell);
        		}
        		if (getDisplayInstructorPrefs()) {
        			cell = headerCell(MSG.columnInstructorAttributePref(), 1, 1);
            		cell.setClassName("WebTableHeaderSecondRow");
            		row2.addCell(cell);
        			cell = headerCell(MSG.columnInstructorPref(), 1, 1);
            		cell.setClassName("WebTableHeaderSecondRow");
            		row2.addCell(cell);
        		}
        	} else if (getDisplayInstructorPrefs()) {
        		cell = headerCell("----" + MSG.columnPreferences() + "----", 1, getPreferenceColumns());
        		cell.setClassName("WebTableHeaderFirstRow");
        		cell.setTextAlignment(Alignment.CENTER);
    	    	row.addCell(cell);
    			cell = headerCell(MSG.columnInstructorAttributePref(), 1, 1);
        		cell.setClassName("WebTableHeaderSecondRow");
        		row2.addCell(cell);
    			cell = headerCell(MSG.columnInstructorPref(), 1, 1);
        		cell.setClassName("WebTableHeaderSecondRow");
        		row2.addCell(cell);    		
        	}
        	if (isShowInstructorAssignment()) {
        		cell = this.headerCell(MSG.columnTeachingLoad(), 2, 1);
        		row.addCell(cell);
        	}
        	if (isShowInstructor()){
        		cell = this.headerCell(MSG.columnInstructor(), 2, 1);
        		row.addCell(cell);
        	}
        	if (getDisplayTimetable() && isShowTimetable()){
    	    	cell = headerCell("--------" + MSG.columnTimetable() + "--------", 1, 3);
    	    	cell.setClassName("WebTableHeaderFirstRow");
        		cell.setTextAlignment(Alignment.CENTER);
        		row.addCell(cell);
        		cell = headerCell(MSG.columnAssignedTime(), 1, 1);
        		cell.setNoWrap(true);
        		cell.setClassName("WebTableHeaderSecondRow");
        		row2.addCell(cell);
        		cell = headerCell(MSG.columnAssignedRoom(), 1, 1);
        		cell.setNoWrap(true);
        		cell.setClassName("WebTableHeaderSecondRow");
        		row2.addCell(cell);
        		cell = headerCell(MSG.columnAssignedRoomCapacity(), 1, 1);
        		cell.setNoWrap(true);
        		cell.setClassName("WebTableHeaderSecondRow");
        		cell.setTextAlignment(Alignment.RIGHT);
        		row2.addCell(cell);
        	}

        	if (isShowTitle()){
        		cell = this.headerCell(MSG.columnTitle(), 2, 1);
        		row.addCell(cell);    		
        	}
        	if (isShowCredit()){
        		cell = this.headerCell(MSG.columnOfferingCredit(), 2, 1).setTextAlignment(Alignment.RIGHT);
        		row.addCell(cell);    		
        	}
        	if (isShowSubpartCredit()){
        		cell = this.headerCell(MSG.columnSubpartCredit(), 2, 1).setTextAlignment(Alignment.RIGHT);
        		row.addCell(cell);    		
        	}
        	if (isShowConsent()){
        		cell = this.headerCell(MSG.columnConsent(), 2, 1);
        		row.addCell(cell);    		
        	}
        	if (isShowSchedulePrintNote()){
        		cell = this.headerCell(this.getSchedulePrintNoteLabel(), 2, 1);
        		row.addCell(cell);    		
        	}
        	if (isShowNote()){
        		cell = this.headerCell(MSG.columnNote(), 2, 1);
        		row.addCell(cell);    		
        	}
        	if (isShowExam()) {
        		cell = headerCell("-----------" + MSG.columnExam() + "--------", 1, (isShowExamName()?1:0)+(isShowExamTimetable()?2:0));
    	    	cell.setClassName("WebTableHeaderFirstRow");
        		cell.setTextAlignment(Alignment.CENTER);
        		cell.setNoWrap(true);
                row.addCell(cell);
                if (isShowExamName()) {
                    cell = headerCell(MSG.columnExamName(), 1, 1);
                    cell.setNoWrap(true);
    	    		cell.setClassName("WebTableHeaderSecondRow");
                    row2.addCell(cell);
                }
                if (isShowExamTimetable()) {
                    cell = headerCell(MSG.columnExamPeriod(), 1, 1);
                    cell.setNoWrap(true);
    	    		cell.setClassName("WebTableHeaderSecondRow");
                    row2.addCell(cell);
                    cell = headerCell(MSG.columnExamRoom(), 1, 1);
                    cell.setNoWrap(true);
    	    		cell.setClassName("WebTableHeaderSecondRow");
                    row2.addCell(cell);
                    
                }
        	}
        	if (isShowLms()) {
        		cell = this.headerCell(MSG.columnLms(), 2, 1);
        		row.addCell(cell);    		
        	}
        	if (isShowWaitlistMode()) {
        		cell = this.headerCell(MSG.columnWaitlistMode(), 2, 1);
        		row.addCell(cell);    		
        	}
    	}
    	table.addHeader(row);
    	if (row2.hasCells())
    		table.addHeader(row2);
    	else
    		for (CellInterface c: row.getCells())
    			c.setRowSpan(1);
   }
    
    protected String getSchedulePrintNoteLabel(){
    	return(MSG.columnSchedulePrintNote());
    }

    private CellInterface subjectAndCourseInfo(InstructionalOffering io, CourseOffering co, boolean isEditable) {
        CellInterface cell = this.initCell(isEditable && co.isIsControl().booleanValue(), 1, true);
    	if ("InstructionalOffering".equals(getBackType()) && io.getUniqueId().toString().equals(getBackId()))
    		cell.addAnchor("back");
    	/*
    	if ("PreferenceGroup".equals(getBackType())) {
    		for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();) {
    			InstrOfferingConfig ioc = (InstrOfferingConfig)i.next();
    			for (Iterator j=ioc.getSchedulingSubparts().iterator();j.hasNext();) {
    				SchedulingSubpart ss = (SchedulingSubpart)j.next();
    	         	if (ss.getUniqueId().toString().equals(getBackId())) cell.addAnchor("back");
    	         	for (Iterator k=ss.getClasses().iterator();k.hasNext();) {
    	         		Class_ c = (Class_)k.next();
    	         		if (c.getUniqueId().toString().equals(getBackId())) cell.addAnchor("back");
    	         	}
    			}
    		}
    	}
    	*/
        cell.addAnchor("A" + io.getUniqueId());
        cell.addAnchor("A" + co.getUniqueId());
        CellInterface c = cell.add(co.getCourseName())
        		.setTitle(co.getCourseNameWithTitle())
        		.addStyle("font-weight: bold;")
        		.setInline(false);
        InstructionalMethod im = (co != null && co.getInstructionalOffering().getInstrOfferingConfigs().size() == 1 ? co.getInstructionalOffering().getInstrOfferingConfigs().iterator().next().getInstructionalMethod() : null);
        if (co != null && co.getCourseType() != null) {
        	if (im != null) {
        		c.add(" (");
        		c.add(co.getCourseType().getReference()).setTitle(co.getCourseType().getLabel());
        		c.add(", ");
        		c.add(im.getReference()).setTitle(im.getLabel());
        		c.add(")");
        	} else {
        		c.add(" (");
        		c.add(co.getCourseType().getReference()).setTitle(co.getCourseType().getLabel());
        		c.add(")");
        	}
        } else if (im != null) {
    		c.add(" (");
    		c.add(im.getReference()).setTitle(im.getLabel());
    		c.add(")");
        }
        for (CourseOffering tempCo: io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId())) {
        	c = cell.add(tempCo.getCourseName())
        		.setColor(disabledColor)
        		.setTitle(tempCo.getCourseNameWithTitle())
        		.setIndent(1)
        		.setInline(false);
        	if (tempCo != null && tempCo.getCourseType() != null) {
        		c.add(" (");
        		c.add(tempCo.getCourseType().getReference()).setTitle(tempCo.getCourseType().getLabel());
        		c.add(")");
        	}
        }
        return cell;
    }  
    
    protected CellInterface buildPrefGroupLabel(CourseOffering co, PreferenceGroup prefGroup, int indentSpaces, boolean isEditable, String prevLabel){
    	CellInterface cell = new CellInterface();
    	if ("PreferenceGroup".equals(getBackType()) && prefGroup.getUniqueId().toString().equals(getBackId()))
    		cell.addAnchor("back");
    	if (indentSpaces > 0)
    		cell.setIndent(indentSpaces);
    	if (!isEditable) cell.setColor(disabledColor);
    	cell.addAnchor("A" + prefGroup.getUniqueId());
        String label = prefGroup.htmlLabel();
        if (prefGroup instanceof Class_) {
			Class_ aClass = (Class_) prefGroup;
			if (!aClass.isEnabledForStudentScheduling().booleanValue()){
				cell.setTitle(MSG.tooltipDisabledForStudentScheduling(aClass.getClassLabelWithTitle(co)));
				cell.addStyle("font-style: italic;");
			} else {
				cell.setTitle(aClass.getClassLabelWithTitle(co));
			}
		}
        if (prevLabel != null && label.equals(prevLabel)){
        	label = "";
        }
        cell.setText(label);
        cell.setNoWrap(true);
        return(cell);
    }
    
    protected CellInterface buildDatePatternCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	AssignmentInfo a = null;
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
    	CellInterface cell = null;
    	if (dp==null) {
    		cell = initNormalCell("", isEditable);
    	} else if (dp.isPatternSet() && isEditable) {
    		cell = initNormalCell(dp.getName(), isEditable);
    		boolean hasReq = false;
			for (Iterator i=prefGroup.effectivePreferences(DatePatternPref.class).iterator(); i.hasNext();) {
				Preference pref = (Preference)i.next();
				if (!hasReq && PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())) {
					hasReq = true;
					if (cell.hasItems()) cell.getItems().clear();
				}
				if (!hasReq || PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())) {
					cell.addItem(preferenceCell(pref).setInline(false));
				}
			}
    	} else {
    		cell = initNormalCell(dp.getName(), isEditable);
    		cell.setTitle(sDateFormat.format(dp.getStartDate())+" - "+sDateFormat.format(dp.getEndDate()));
    		if (p != null && isEditable)
    			cell.setColor(PreferenceLevel.int2color(p.getDatePatternPref()));
    	}
        cell.setTextAlignment(Alignment.CENTER);
        return(cell);
    }

    private CellInterface buildTimePatternCell(PreferenceGroup prefGroup, boolean isEditable){
    	Set<TimePattern> patterns = prefGroup.effectiveTimePatterns();
    	CellInterface cell = new CellInterface();
    	cell.setNoWrap(true); cell.setTextAlignment(Alignment.CENTER);
    	if (!isEditable) cell.setColor(disabledColor);
    	if (patterns == null || patterns.isEmpty()) {
    		if (prefGroup instanceof Class_) {
    			Class_ clazz = (Class_)prefGroup;
    			DurationModel dm = clazz.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
    			Integer ah = dm.getArrangedHours(clazz.getSchedulingSubpart().getMinutesPerWk(), clazz.effectiveDatePattern());
                if (ah == null) {
                	cell.setText(MSG.arrHrs());
                	cell.setTitle(MSG.arrangeHours());
                } else {
                	cell.setText(MSG.arrHrsN(ah));
                	cell.setTitle(MSG.arrangeHoursN(ah));
                }
    		}
    	} else {
    		for (TimePattern tp: patterns) {
    			cell.add(tp.getName()).setInline(false).setTextAlignment(Alignment.CENTER);
    		}
    	}
        return cell;
    }
    
    protected CellInterface cellForTimePrefs(AssignmentInfo assignment, Set<TimePref> timePrefList, final boolean timeVertical, boolean gridAsText, String timeGridSize, boolean highlightClassPrefs){
    	CellInterface cell = new CellInterface();
    	for (TimePref tp: timePrefList) {
    		final RequiredTimeTable rtt = tp.getRequiredTimeTable(assignment == null ? null : assignment.getTimeLocation());
    		String owner = "";
    		if (tp.getOwner() != null && tp.getOwner() instanceof Class_) {
    			owner = " (" + MSG.prefOwnerClass() + ")";
    		} else if (tp.getOwner() != null && tp.getOwner() instanceof SchedulingSubpart) {
    			owner = " (" + MSG.prefOwnerSchedulingSubpart() + ")";
    		} else if (tp.getOwner() != null && tp.getOwner() instanceof DepartmentalInstructor) {
    			owner = " (" + MSG.prefOwnerInstructor() + ")";
    		} else if (tp.getOwner() != null && tp.getOwner() instanceof Exam) {
    			owner = " (" + MSG.prefOwnerExamination() + ")";
    		} else if (tp.getOwner() != null && tp.getOwner() instanceof Department) {
    			owner = " (" + MSG.prefOwnerDepartment() + ")";
    		} else if (tp.getOwner() != null && tp.getOwner() instanceof Session) {
    			owner = " (" + MSG.prefOwnerSession() + ")";
    		} else {
    			owner = " (" + MSG.prefOwnerCombined() + ")";
    		}
    		String hint = rtt.print(false, timeVertical, true, false, rtt.getModel().getName() + owner).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " ");
        	if (gridAsText || rtt.getModel().isExactTime()) {
        		cell.add(rtt.getModel().toString())
        			.setMouseOver("$wnd.showGwtHint($wnd.lastMouseOverElement, $wnd." + hint + ");")
        			.setMouseOut("$wnd.hideGwtHint();")
        			.addStyle(tp.getOwner() != null && tp.getOwner() instanceof Class_ && highlightClassPrefs ? "background: #ffa;" : "")
        			.setInline(false);
        	} else {
        		rtt.getModel().setDefaultSelection(timeGridSize);
        		cell.add(null).setImage(
        				new ImageInterface().setSource("pattern?v=" + (timeVertical ? 1 : 0) + "&s=" + rtt.getModel().getDefaultSelection() + "&tp=" + tp.getTimePattern().getUniqueId() + "&p=" + rtt.getModel().getPreferences() +
            					(assignment == null || assignment.getTimeLocation() == null ? "" : "&as=" + assignment.getTimeLocation().getStartSlot() + "&ad=" + assignment.getTimeLocation().getDayCode()) +
            					(tp.getOwner() != null && tp.getOwner() instanceof Class_ && highlightClassPrefs ? "&hc=1" : "")
            					).setAlt(rtt.getModel().toString())
        						.setGenerator(new ImageGenerator() {
        							public Object generate() {
        								return rtt.createBufferedImage(timeVertical);
        							}
        						}
                				))
        			.setMouseOver("$wnd.showGwtHint($wnd.lastMouseOverElement, $wnd." + hint + ");")
        			.setMouseOut("$wnd.hideGwtHint();")
        			.addStyle("display: inline-block;")
        			.setAria(rtt.getModel().toString());
        	}
    	}
    	cell.setNoWrap(true);
    	return cell;
    }
    
    private CellInterface buildTimePrefCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	AssignmentInfo a = null;
		if (getDisplayTimetable() && isShowTimetable() && classAssignment!=null && prefGroup instanceof Class_) {
			try {
				a = classAssignment.getAssignment((Class_)prefGroup);
			} catch (Exception e) {
				Debug.error(e);
			}
    	}
    	
		
    	CellInterface cell = cellForTimePrefs(a, prefGroup.effectivePreferences(TimePref.class), getTimeVertival(), getGridAsText(), getDefaultTimeGridSize(), isHighlightClassPrefs());
        cell.setNoWrap(true);
    	return (cell);
    	
    }
    
    private CellInterface buildPreferenceCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, Class prefType, boolean isEditable){
    	if (!isEditable) return initNormalCell("",false);
    	if (TimePref.class.equals(prefType)) {
    		return(buildTimePrefCell(classAssignment,prefGroup, isEditable));
    	} else {
    		if (!prefGroup.isInstructorAssignmentNeeded() && (InstructorPref.class.equals(prefType) || InstructorAttributePref.class.equals(prefType))) {
    			return initNormalCell("",false);
    		}
    		CellInterface cell = initNormalCell("", isEditable); cell.setInline(false);
    		for (Object pref: prefGroup.effectivePreferences(prefType))
    			cell.addItem(preferenceCell((Preference)pref));
    		if (!isSimple()) cell.setNoWrap(true);
    		return(cell);
    	}
    	
    }
    
    private CellInterface buildPreferenceCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, Class[] prefTypes, boolean isEditable){
    	if (!isEditable) return initNormalCell("",false);
    	CellInterface cell = initNormalCell("", isEditable);
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
    		for (Object pref: prefGroup.effectivePreferences(prefType))
    			cell.addItem(preferenceCell((Preference)pref));
    	}
    	if (noRoomPrefs && ! cell.hasItems())
    		cell.setText(MSG.notApplicable()).addStyle("font-style: italic;");
    	cell.setNoWrap(true);
    	return(cell);
    }

    private CellInterface buildPrefGroupDemand(PreferenceGroup prefGroup, boolean isEditable){
    	if (prefGroup instanceof Class_) {
			Class_ c = (Class_) prefGroup;
			if (sessionHasEnrollments(c.getSessionId())){
				CellInterface tc = null;
				if (c.getEnrollment() != null){
					tc = this.initNormalCell(c.getEnrollment().toString(), isEditable);
				} else {
					tc = this.initNormalCell("0", isEditable);				
				}
				tc.setTextAlignment(Alignment.RIGHT);
				return(tc);
			}
		}
    	return(this.initNormalCell("", isEditable));
    }
    
    private CellInterface buildPrefGroupProjectedDemand(PreferenceGroup prefGroup, boolean isEditable){
    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_)prefGroup;
    		SectioningInfo i = c.getSectioningInfo();
    		if (i != null && i.getNbrExpectedStudents() != null) {
    			CellInterface cell = initNormalCell(String.valueOf(Math.round(Math.max(0.0, c.getEnrollment() + i.getNbrExpectedStudents()))), isEditable);
    			cell.setTextAlignment(Alignment.RIGHT);	
    			return cell;
    		}
    	}
    	return(this.initNormalCell("", isEditable));
    }
    
    private CellInterface buildSnapshotLimit(PreferenceGroup prefGroup, boolean isEditable){
    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_)prefGroup;
    		if (c.getSnapshotLimit() != null) {
    			CellInterface cell = null;
    			if (c.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment()) {
    				cell = initNormalCell("\u221E", isEditable);
    				cell.addStyle("font-size: +1");
    			} else {
    				cell = initNormalCell(c.getSnapshotLimit().toString(), isEditable);
    			}
    			cell.setTextAlignment(Alignment.RIGHT);	
    			return cell;
    		}
    	}
    	return(this.initNormalCell("", isEditable));
    }

    private CellInterface buildLimit(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	CellInterface cell = null;
    	boolean nowrap = false;
    	if (prefGroup instanceof SchedulingSubpart){
	    	SchedulingSubpart ss = (SchedulingSubpart) prefGroup;
	    	boolean unlimited = ss.getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue();
	    	if (!unlimited) {
		    	int limit = ss.getLimit();
		    	int maxExpCap = ss.getMaxExpectedCapacity(); 
		    	if (limit==maxExpCap)
		    		cell = initNormalCell(String.valueOf(limit), isEditable);
		    	else {
		    		cell = initNormalCell(limit+"-"+maxExpCap, isEditable);
		    		nowrap = true;
		    	}
	    	}
	    	else {
	    	    cell = initNormalCell("", isEditable);	    	    
	    	}
    	} else if (prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
	    	boolean unlimited = aClass.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue();
	    	if (!unlimited) {
	    		String limitString = null;
	    		AssignmentInfo a = null;
                try {
                    if (classAssignment!=null) a = classAssignment.getAssignment(aClass);
                } catch (Exception e) {}
                if (a==null) {
                    if (aClass.getExpectedCapacity() != null){
                        limitString = aClass.getExpectedCapacity().toString();
                        if (aClass.getMaxExpectedCapacity() != null && !aClass.getMaxExpectedCapacity().equals(aClass.getExpectedCapacity())){
                            limitString = limitString + "-" + aClass.getMaxExpectedCapacity().toString();
                            nowrap = true;
                        }
                    } else {
                        limitString = "0";
                        if (aClass.getMaxExpectedCapacity() != null && aClass.getMaxExpectedCapacity().intValue() != 0){
                            limitString = limitString + "-" + aClass.getMaxExpectedCapacity().toString();
                            nowrap = true;
                        }
                    }
                } else {
                    limitString = ""+aClass.getClassLimit(classAssignment);
                }
	    		cell = initNormalCell(limitString, isEditable);
	    		if (nowrap) cell.setNoWrap(true);
	    	}
	    	else {
	    	    cell = initNormalCell("\u221E", isEditable);
	    	    cell.addStyle("font-size: +1");
	    	}
	    		
    	} else {
    		cell = this.initNormalCell("" ,isEditable);
    	}
        cell.setTextAlignment(Alignment.RIGHT);	
        return(cell);
    }
    
    private CellInterface buildDivisionSection(CourseOffering co, PreferenceGroup prefGroup, boolean isEditable){
    	CellInterface cell = null;
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		//String divSec = aClass.getDivSecNumber();
    		String divSec = (isShowOriginalDivSecs() ? aClass.getClassSuffix() : aClass.getClassSuffix(co));
    		cell = initNormalCell((divSec==null?"":divSec), isEditable);
            cell.setTextAlignment(Alignment.RIGHT);	
    	} else {
    		cell = this.initNormalCell("" ,isEditable);
    	}
    	cell.setNoWrap(true);
        return(cell);
    }
    
    protected CellInterface buildInstructorAssignment(PreferenceGroup prefGroup, boolean isEditable){
    	CellInterface cell = this.initNormalCell("" ,isEditable);
    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_) prefGroup;
    		if (c.isInstructorAssignmentNeeded()) {
    			cell.add((c.effectiveNbrInstructors() > 1 ? c.effectiveNbrInstructors() + " \u00d7 " : "") +
    					Formats.getNumberFormat("0.##").format(c.effectiveTeachingLoad()) + " " + MSG.teachingLoadUnits())
    				.setTextAlignment(Alignment.RIGHT)
    				.setInline(false)
    				.setNoWrap(true);
    		} else if (c.getSchedulingSubpart().isInstructorAssignmentNeeded()) {
    			cell.add(MSG.cellNoInstructorAssignment())
    				.setTextAlignment(Alignment.RIGHT)
    				.setInline(false);
    		}
    	} else if (prefGroup instanceof SchedulingSubpart) {
    		SchedulingSubpart ss = (SchedulingSubpart)prefGroup;
    		if (ss.isInstructorAssignmentNeeded()) {
    			cell.add((ss.getNbrInstructors() > 1 ? ss.getNbrInstructors() + " \u00d7 " : "") +
    					Formats.getNumberFormat("0.##").format(ss.getTeachingLoad()) + " " + MSG.teachingLoadUnits())
    				.setTextAlignment(Alignment.RIGHT)
					.setInline(false)
					.setNoWrap(true);
    		}
    	}
        return(cell);
    }
    
    protected CellInterface instructorCell(Class_ clazz, String instructorNameFormat){
    	CellInterface cell = new CellInterface();
    	cell.setNoWrap(false);
    	
    	if (clazz.getClassInstructors()==null) return cell;
    	
    	InstructorComparator ic = new InstructorComparator();
    	if (ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue()) ic.setNameFormat(instructorNameFormat);
    	TreeSet<ClassInstructor> sortedInstructors = new TreeSet<ClassInstructor>(ic);
    	sortedInstructors.addAll(clazz.getClassInstructors());
    	
    	for (ClassInstructor ci: sortedInstructors) {
    		String title = ci.getInstructor().getNameLastFirst();
    		title += " (" + (ci.getResponsibility() == null ? "" : ci.getResponsibility().getLabel() + " ") +
    				ci.getPercentShare()+"%"+(ci.isLead().booleanValue()?", " + MSG.toolTipInstructorLead():"")+")";
    		if (!clazz.isDisplayInstructor().booleanValue()){
    			title += MSG.toolTipInstructorDoNotDisplay();
    		}
    		CellInterface c = cell.add(ci.getInstructor().getName(instructorNameFormat) +
    				(ci.getResponsibility() != null && ci.getResponsibility().getAbbreviation() != null && !ci.getResponsibility().getAbbreviation().isEmpty() ? " (" + ci.getResponsibility().getAbbreviation() + ")" : "")
    				).setTitle(title);
    		if (ci.isLead().booleanValue())
    			c.addStyle("font-weight: bold;");
    		if (!clazz.isDisplayInstructor())
    			c.addStyle("font-style: italic;");
    		c.setInline(false);
    	}
    	
    	return cell;
    }

    protected CellInterface buildInstructor(PreferenceGroup prefGroup, boolean isEditable){
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		CellInterface cell = instructorCell(aClass, getInstructorNameFormat());
    		if (!isEditable)
    			cell.setColor(disabledColor);
    		return cell.setTextAlignment(Alignment.LEFT);
    	} else {
    		return this.initNormalCell("" ,isEditable);
    	}
    }

    private CellInterface buildCredit(PreferenceGroup prefGroup, boolean isEditable){
    	CellInterface cell = this.initNormalCell("" ,isEditable);
    	if (prefGroup instanceof SchedulingSubpart) {
    		SchedulingSubpart ss = (SchedulingSubpart) prefGroup;
     		if (ss.getCredit() != null) {
     			cell.setText(ss.getCredit().creditAbbv());
     			cell.setTitle(ss.getCredit().creditText());
    		}   		
            cell.setTextAlignment(Alignment.RIGHT);	
    	}
        return(cell);
    }

    private CellInterface buildSchedulePrintNote(PreferenceGroup prefGroup, boolean isEditable){
    	CellInterface cell = null;
    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_) prefGroup;
    		if (c.getSchedulePrintNote()!=null && !c.getSchedulePrintNote().trim().isEmpty()) {
    			String note = c.getSchedulePrintNote().replaceAll("\\<.*?\\>", "");
    			if (CommonValues.NoteAsFullText.eq(getUser().getProperty(UserProperty.SchedulePrintNoteDisplay))) {
	    			cell = initNormalCell(c.getSchedulePrintNote(), isEditable);
	    			cell.setTextAlignment(Alignment.LEFT);
    			} else if (CommonValues.NoteAsShortText.eq(getUser().getProperty(UserProperty.SchedulePrintNoteDisplay))) {
        			note = (note.length() <= 20 ? note : note.substring(0, 20) + "...");
    				cell = initNormalCell(note, isEditable);
    				cell.setStyle("white-space: pre-wrap;");
	    			cell.setTextAlignment(Alignment.LEFT);
    			} else {
    				cell = initNormalCell("", isEditable);
    	    		cell.setImage(new ImageInterface().setSource("images/note.png").setTitle(note).setAlt(MSG.altHasSchedulePrintNote()));
    	    		cell.setTextAlignment(Alignment.CENTER);
    			}
    		} else {
        		cell = this.initNormalCell("" ,isEditable);
        	}
    	}  else {
       		cell = this.initNormalCell("" ,isEditable);   		
    	}
        return(cell);
    }
    
    private CellInterface buildExamName(TreeSet<Exam> exams, boolean isEditable) {
        CellInterface cell = new CellInterface();
        if (!isEditable) cell.setColor(disabledColor);
        for (Exam exam: exams) {
            CellInterface c = cell.add(exam.getLabel());
            c.setNoWrap(true);
            c.setInline(false);
            if (ExamType.sExamTypeFinal==exam.getExamType().getType())
            	c.addStyle("font-weight: bold;");
        }
        cell.setTextAlignment(Alignment.LEFT);
        cell.setNoWrap(true);
        return(cell);
    }
    
    protected CellInterface getPeriodCell(ExamAssignment ea) {
    	CellInterface cell = new CellInterface();
    	cell.setText(ea.getPeriodAbbreviation());
    	if (ea.getPeriodPref() != null && !PreferenceLevel.sNeutral.equals(ea.getPeriodPref()))
    		cell.setColor(PreferenceLevel.prolog2color(ea.getPeriodPref()));
    	if (ApplicationProperty.LegacyPeriodPreferences.isTrue()) {
			cell.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement, '" + ea.getExamId() + "," + ea.getPeriodId() + "');");
		} else {
			cell.setMouseOver("$wnd.showGwtExamPeriodPreferencesHint($wnd.lastMouseOverElement,'" + ea.getExamId() + "','" + ea.getPeriodId() + "');");
		}
		cell.setMouseOut("hideGwtTimeHint();");
    	return cell;
    }

    private CellInterface buildExamPeriod(ExamAssignmentProxy examAssignment, TreeSet<Exam> exams, boolean isEditable) {
    	CellInterface cell = new CellInterface();
        if (!isEditable) cell.setColor(disabledColor);
        for (Exam exam: exams) {
            if (examAssignment!=null && examAssignment.getExamTypeId().equals(exam.getExamType().getUniqueId())) {
                ExamAssignment ea = examAssignment.getAssignment(exam.getUniqueId());
                if (ea==null && !isShowExamName()) continue;
                if (ea != null) {
                	CellInterface c = getPeriodCell(ea); 
                	c.setInline(false); c.setNoWrap(true);
                	if (ExamType.sExamTypeFinal==exam.getExamType().getType()) c.addStyle("font-weight: bold;");
                	cell.addItem(c);
                } else {
                	cell.addBlankLine();
                }
            } else {
                if (exam.getAssignedPeriod()==null && !isShowExamName()) continue;
                if (exam.getAssignedPeriod() != null) {
                    CellInterface c = cell.add(exam.getAssignedPeriod().getAbbreviation())
                    	.setTitle(exam.getAssignedPeriod().getName())
                    	.setInline(false)
                    	.setNoWrap(true);
                    if (ExamType.sExamTypeFinal==exam.getExamType().getType()) c.addStyle("font-weight: bold;");
                } else {
                	cell.addBlankLine();
                }
            }
        }
        cell.setTextAlignment(Alignment.LEFT);
        cell.setNoWrap(true);
        return cell;
    }
    
    protected CellInterface getLocationCell(Location location) {
		CellInterface cell = new CellInterface();
    	cell.setText(location.getLabel());
    	cell.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + location.getUniqueId() + "');");
    	cell.setMouseOut("$wnd.hideGwtRoomHint()");
    	return cell;
    }
    
    public CellInterface getLocationCell(ExamRoomInfo room) {
    	CellInterface cell = new CellInterface();
    	cell.setText(room.getName());
    	cell.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
    	cell.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');");
    	cell.setMouseOut("$wnd.hideGwtRoomHint()");
    	return cell;
    }

    private CellInterface buildExamRoom(ExamAssignmentProxy examAssignment, TreeSet<Exam> exams, boolean isEditable) {
    	CellInterface cell = new CellInterface();
        if (!isEditable) cell.setColor(disabledColor);
        for (Exam exam: exams) {
            if (examAssignment!=null && examAssignment.getExamTypeId().equals(exam.getExamType().getUniqueId())) {
                ExamAssignment ea = examAssignment.getAssignment(exam.getUniqueId());
                if (ea==null && !isShowExamName()) continue;
            	CellInterface c = new CellInterface(); c.setInline(false); c.setNoWrap(true);
                if (ea != null && ea.getRooms() != null) {
                    for (ExamRoomInfo room : ea.getRooms()) {
                    	if (c.hasItems()) c.add(", ");
                    	c.addItem(getLocationCell(room));
                    }
                }
            	cell.addItem(c);
            	if (ExamType.sExamTypeFinal==exam.getExamType().getType()) c.addStyle("font-weight: bold;");
            } else {
                if (exam.getAssignedPeriod()==null && !isShowExamName()) continue;
                CellInterface c = new CellInterface(); c.setInline(false); c.setNoWrap(true);
                for (Location location: new TreeSet<Location>(exam.getAssignedRooms())) {
                	if (c.hasItems()) c.add(", ");
                	c.addItem(getLocationCell(location));
                }
            	cell.addItem(c);
            	if (ExamType.sExamTypeFinal==exam.getExamType().getType()) c.addStyle("font-weight: bold;");
            }
        }
        cell.setTextAlignment(Alignment.LEFT);
        cell.setNoWrap(true);
        return cell;
    }

    protected TreeSet getExams(Class_ clazz) {
        return new TreeSet(Exam.findAll(ExamOwner.sOwnerTypeClass,clazz.getUniqueId()));
    }

    private CellInterface buildSchedulePrintNote(InstructionalOffering io, boolean isEditable){
    	CellInterface cell = null;
	    String note = "";
	    for (CourseOffering co: io.getCourseOfferings()) {
	    	if (co.getScheduleBookNote() != null && !co.getScheduleBookNote().trim().isEmpty()) {
	    		if (!note.isEmpty()) note += "\n";
	    		if (CommonValues.NoteAsShortText.eq(getUser().getProperty(UserProperty.CourseOfferingNoteDisplay))) {
	    			String n = co.getScheduleBookNote().replaceAll("\\<.*?\\>", "");
	    			note += (n.length() <= 20 ? n : n.substring(0, 20) + "...");
	    		} else if (CommonValues.NoteAsIcon.eq(getUser().getProperty(UserProperty.CourseOfferingNoteDisplay))) {
	    			note += co.getScheduleBookNote().replaceAll("\\<.*?\\>", "");
				} else {
					note += co.getScheduleBookNote();
				}
	    	}
	    }
		if (note.isEmpty()) {
			cell = this.initNormalCell("" ,isEditable);
		} else {
			if (CommonValues.NoteAsIcon.eq(getUser().getProperty(UserProperty.CourseOfferingNoteDisplay))) {
	    		cell = initNormalCell("", isEditable);
	    		cell.setImage(new ImageInterface().setSource("images/note.png").setTitle(note).setAlt(MSG.altHasCourseOfferingNote()));
	    		cell.setTextAlignment(Alignment.CENTER);	
			} else {
				cell = initNormalCell(note, isEditable);
				cell.setStyle("white-space: pre-wrap;");
				cell.setTextAlignment(Alignment.LEFT);
			}
		}
        return(cell);
    }
    
    protected CellInterface buildNote(PreferenceGroup prefGroup, boolean isEditable){
    	CellInterface cell = null;
    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_) prefGroup;
    		if (c.getNotes() != null && !c.getNotes().trim().isEmpty()) {
    			String note = c.getNotes().replaceAll("\\<.*?\\>", "");
    			if (CommonValues.NoteAsShortText.eq(getUser().getProperty(UserProperty.ManagerNoteDisplay))) {
    				note = (note.length() <= 20 ? note : note.substring(0, 20) + "...");
    				cell = initNormalCell(note, isEditable);
    				cell.setStyle("white-space: pre-wrap;");
        			cell.setTextAlignment(Alignment.LEFT);
    			} else if (CommonValues.NoteAsFullText.eq(getUser().getProperty(UserProperty.ManagerNoteDisplay))) {
    				cell = initNormalCell(c.getNotes(), isEditable);
    				cell.setStyle("white-space: pre-wrap;");
        			cell.setTextAlignment(Alignment.LEFT);
    			} else {
    				cell = initNormalCell("", isEditable);
    	    		cell.setImage(new ImageInterface().setSource("images/note.png").setTitle(note).setAlt(MSG.altHasNoteToMgr()));
    	    		cell.setTextAlignment(Alignment.CENTER);
    			}
    		} else { 
        		cell = this.initNormalCell("" ,isEditable);
        	}
    	} else { 
    		cell = this.initNormalCell("" ,isEditable);
    	}
        return(cell);
    }
    
    private CellInterface buildNote(InstructionalOffering offering, boolean isEditable){
    	CellInterface cell = null;
		if (offering.getNotes() != null && !offering.getNotes().trim().isEmpty()) {
			String note = offering.getNotes().replaceAll("\\<.*?\\>", "");
			if (CommonValues.NoteAsShortText.eq(getUser().getProperty(UserProperty.ManagerNoteDisplay))) {
				note = (note.length() <= 20 ? note : note.substring(0, 20) + "...");
				cell = initNormalCell(note, isEditable);
    			cell.setTextAlignment(Alignment.LEFT);
			} else if (CommonValues.NoteAsFullText.eq(getUser().getProperty(UserProperty.ManagerNoteDisplay))) {
				cell = initNormalCell(offering.getNotes(), isEditable);
				cell.setStyle("white-space: pre-wrap;");
    			cell.setTextAlignment(Alignment.LEFT);
			} else {
	    		cell = initNormalCell("", isEditable);
	    		cell.setImage(new ImageInterface().setSource("images/note.png").setTitle(note).setAlt(MSG.altHasNoteToMgr()));
	    		cell.setTextAlignment(Alignment.CENTER);
			}
		} else { 
    		cell = this.initNormalCell("" ,isEditable);
    	}
        return(cell);
    }

    private CellInterface buildManager(PreferenceGroup prefGroup, boolean isEditable){
    	CellInterface cell = null;
    	Department managingDept = null;
    	if (prefGroup instanceof Class_) {
    		managingDept = ((Class_)prefGroup).getManagingDept();
    	} else if (prefGroup instanceof SchedulingSubpart) {
    		managingDept = ((SchedulingSubpart)prefGroup).getManagingDept();
    	} 
    	cell = initNormalCell(managingDept==null?"":managingDept.getShortLabel(), isEditable);
    	if (managingDept != null)
    		cell.setTitle(managingDept.getManagingDeptLabel());
        return(cell);
    }

    protected CellInterface buildFundingDepartment(PreferenceGroup prefGroup, boolean isEditable){
    	CellInterface cell = null;
    	Department fundingDepartment = null;
    	if (prefGroup instanceof Class_) {
    		fundingDepartment = ((Class_)prefGroup).getEffectiveFundingDept();
    	}
    	cell = initNormalCell(fundingDepartment==null?"":fundingDepartment.getShortLabel(), isEditable);
    	if (fundingDepartment != null)
    		cell.setTitle(fundingDepartment.getManagingDeptLabel());
        return(cell);
    }
    
    protected CellInterface buildMinPerWeek(PreferenceGroup prefGroup, boolean isEditable){
    	CellInterface cell = null;
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		String suffix = "";
    		ClassDurationType dtype = aClass.getSchedulingSubpart().getInstrOfferingConfig().getEffectiveDurationType();
    		if (dtype != null && !dtype.equals(aClass.getSchedulingSubpart().getSession().getDefaultClassDurationType())) {
    			suffix = " " + dtype.getAbbreviation();
    		}
    		cell = initNormalCell(aClass.getSchedulingSubpart().getMinutesPerWk() + suffix, isEditable);
            cell.setTextAlignment(Alignment.RIGHT);	
    	} else if (prefGroup instanceof SchedulingSubpart) {
    			SchedulingSubpart aSchedulingSubpart = (SchedulingSubpart) prefGroup;
        		String suffix = "";
        		ClassDurationType dtype = aSchedulingSubpart.getInstrOfferingConfig().getEffectiveDurationType();
        		if (dtype != null && !dtype.equals(aSchedulingSubpart.getSession().getDefaultClassDurationType())) {
        			suffix = " " + dtype.getAbbreviation();
        		}
        		cell = initNormalCell(aSchedulingSubpart.getMinutesPerWk() + suffix, isEditable);
                cell.setTextAlignment(Alignment.RIGHT);	
    	} else {
    		cell = this.initNormalCell("" ,isEditable);
    	}
        return(cell);
    }

    private CellInterface buildRoomLimit(PreferenceGroup prefGroup, boolean isEditable, boolean classLimitDisplayed){
    	CellInterface cell = null;
    	if (prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
    		if (aClass.getNbrRooms()!=null && aClass.getNbrRooms().intValue()!=1) {
    			if (aClass.getNbrRooms().intValue()==0) {
    				cell = initNormalCell(MSG.notApplicable(), isEditable);
    				cell.addStyle("color: gray; font-style: italic;");
    			} else if (Boolean.TRUE.equals(aClass.isRoomsSplitAttendance())) {
    				cell = initNormalCell(MSG.cellNbrRoomsAndRoomRatioSlitAttendance(aClass.getNbrRooms(), aClass.getRoomRatio() == null ? "0" : sRoomRatioFormat.format(aClass.getRoomRatio())), isEditable);
    				cell.setTitle(MSG.titleNbrRoomsAndRoomRatioSlitAttendance(aClass.getNbrRooms(), aClass.getRoomRatio() == null ? "0" : sRoomRatioFormat.format(aClass.getRoomRatio()))); 
    				cell.setNoWrap(true);
    			} else {
    				cell = initNormalCell(MSG.cellNbrRoomsAndRoomRatio(aClass.getNbrRooms(), aClass.getRoomRatio() == null ? "0" : sRoomRatioFormat.format(aClass.getRoomRatio())), isEditable);
    				cell.setTitle(MSG.titleNbrRoomsAndRoomRatio(aClass.getNbrRooms(), aClass.getRoomRatio() == null ? "0" : sRoomRatioFormat.format(aClass.getRoomRatio())));
    				cell.setNoWrap(true);
    			}
    		} else {
    			if (aClass.getRoomRatio() != null){
    				if (classLimitDisplayed && aClass.getRoomRatio().equals(1f)){
    					cell = initNormalCell("", isEditable);
    				} else {
    					cell = initNormalCell(sRoomRatioFormat.format(aClass.getRoomRatio().floatValue()), isEditable);
    				}
    			} else {
    				if (aClass.getExpectedCapacity() == null){
    					cell = initNormalCell("", isEditable);
    				} else {
    					cell = initNormalCell("0", isEditable);
    				}
    			}
    		}
            cell.setTextAlignment(Alignment.RIGHT);	
    	} else {
    		cell = this.initNormalCell("" ,isEditable);
    	}
        return(cell);
    }
    
    private CellInterface buildAssignedTime(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	CellInterface cell = null;
    	if (classAssignment!=null && prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		AssignmentInfo a = null;
    		AssignmentPreferenceInfo info = null;
    		try {
    			a = classAssignment.getAssignment(aClass);
    			info = classAssignment.getAssignmentInfo(aClass);
    		} catch (Exception e) {
    			Debug.error(e);
    		}
    		if (a!=null) {
    			StringBuffer sb = new StringBuffer();
    			Integer firstDay = ApplicationProperty.TimePatternFirstDayOfWeek.intValue();
    			for (int i = 0; i < CONSTANTS.shortDays().length; i++) {
    				int idx = (firstDay == null ? i : (i + firstDay) % 7);
    				if ((Constants.DAY_CODES[idx] & a.getTimeLocation().getDayCode()) != 0) sb.append(CONSTANTS.shortDays()[idx]);
    			}
   				sb.append(" ");
   				sb.append(a.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm()));
   				sb.append("-");
   				sb.append(a.getTimeLocation().getEndTimeHeader(CONSTANTS.useAmPm()));
    			cell = initNormalCell(sb.toString(), isEditable);
    		} else {
    			cell = initNormalCell("", isEditable);
    		}
            cell.setTextAlignment(Alignment.LEFT);
            cell.setNoWrap(true);
            if (info!=null)
            	cell.setColor(isEditable?PreferenceLevel.int2color(info.getTimePreference()):disabledColor);
    	} else {
    		cell = this.initNormalCell("" ,isEditable);
    	}
        return(cell);
    }
   
    private CellInterface buildAssignedRoom(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable) {
    	CellInterface cell = new CellInterface();
    	if (!isEditable) cell.setColor(disabledColor);
    	if (classAssignment!=null && prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
    		AssignmentInfo a = null;
    		AssignmentPreferenceInfo info = null;
    		try {
    			a= classAssignment.getAssignment(aClass);
    			info = classAssignment.getAssignmentInfo(aClass);
    		} catch (Exception e) {
    			Debug.error(e);
    		}
    		if (a!=null) {
    			for (Location room: a.getRooms()) {
    				if (info!=null)
	    				cell.add(room.getLabel())
	    					.setColor(isEditable?PreferenceLevel.int2color(info.getRoomPreference(room.getUniqueId())):disabledColor)
	    					.setInline(false)
	    					.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getUniqueId() + "', '" + (isEditable ? PreferenceLevel.int2string(info.getRoomPreference(room.getUniqueId())) : "") + "');")
	    					.setMouseOut("$wnd.hideGwtRoomHint();");
	    			else
	    				cell.add(room.getLabel()).setInline(false);
	    		}
    		}
    	}
        cell.setTextAlignment(Alignment.LEFT);
        cell.setNoWrap(true);
        return cell;
    }

    private CellInterface buildAssignedRoomCapacity(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable) {
    	CellInterface cell = new CellInterface();
    	if (!isEditable) cell.setColor(disabledColor);
    	if (classAssignment!=null && prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
    		AssignmentInfo a = null;
   			try {
   				a = classAssignment.getAssignment(aClass);
   			} catch (Exception e) {
   				Debug.error(e);
   			}
	   		if (a!=null) {
	   			for (Location room: a.getRooms()) {
	   				cell.add(room.getCapacity().toString()).setInline(false);
	   			}
    		}
    	}
        cell.setTextAlignment(Alignment.RIGHT);
        cell.setNoWrap(true);
        return cell;
    }
    
    protected CellInterface buildLmsInfo(PreferenceGroup prefGroup, boolean isEditable){
    	CellInterface cell = this.initNormalCell("" ,isEditable);
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
	    	if (LearningManagementSystemInfo.isLmsInfoDefinedForSession(aClass.getSessionId())) {
	    		if (aClass.getLms() != null)
		    		cell.setText(aClass.getLms().getLabel());
	    		cell.setTextAlignment(Alignment.LEFT);
    		}
    	}
        return(cell);
    }

    protected void buildClassOrSubpartRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, LineInterface row, CourseOffering co, PreferenceGroup prefGroup, int indentSpaces, boolean isEditable, String prevLabel){
    	boolean classLimitDisplayed = false;
    	if (isShowLabel()){
	        row.addCell(this.buildPrefGroupLabel(co, prefGroup, indentSpaces, isEditable, prevLabel));
    	} 
    	if (isShowDivSec()){
    		row.addCell(this.buildDivisionSection(co, prefGroup, isEditable));
    	}
    	if (isShowDemand()){
    		row.addCell(this.buildPrefGroupDemand(prefGroup, isEditable));
    	} 
    	if (isShowProjectedDemand()){
    		row.addCell(this.buildPrefGroupProjectedDemand(prefGroup, isEditable));
    	} 
    	if (isShowLimit()){
    		classLimitDisplayed = true;
    		row.addCell(this.buildLimit(classAssignment, prefGroup, isEditable));
       	} 
    	if (isShowSnapshotLimit()){
    		row.addCell(this.buildSnapshotLimit(prefGroup, isEditable));
       	} 
     	if (isShowRoomRatio()){
    		row.addCell(this.buildRoomLimit(prefGroup, isEditable, classLimitDisplayed));
       	} 
    	if (isShowManager()){
    		row.addCell(this.buildManager(prefGroup, isEditable));
     	} 
     	if (isShowFundingDepartment()){
    		row.addCell(this.buildFundingDepartment(prefGroup, isEditable));
       	} 
    	if (isShowDatePattern()){
    		row.addCell(this.buildDatePatternCell(classAssignment,prefGroup, isEditable));
     	} 
    	if (isShowMinPerWk()){
    		row.addCell(this.buildMinPerWeek(prefGroup, isEditable));
       	} 
    	if (isShowTimePattern()){
    		row.addCell(this.buildTimePatternCell(prefGroup, isEditable));
    	} 
    	if (isShowPreferences()){
    		row.addCell(this.buildPreferenceCell(classAssignment,prefGroup, TimePref.class, isEditable));
    		row.addCell(this.buildPreferenceCell(classAssignment,prefGroup, new Class[] {RoomPref.class, BuildingPref.class, RoomFeaturePref.class, RoomGroupPref.class} , isEditable));
    		if (getDisplayDistributionPrefs()) {
    			row.addCell(this.buildPreferenceCell(classAssignment,prefGroup, DistributionPref.class, isEditable));
    		}
    	}
    	if (getDisplayInstructorPrefs()) {
			row.addCell(this.buildPreferenceCell(classAssignment,prefGroup, InstructorAttributePref.class, isEditable));
			row.addCell(this.buildPreferenceCell(classAssignment,prefGroup, InstructorPref.class, isEditable));
    	}
    	if (isShowInstructorAssignment()) {
    		row.addCell(this.buildInstructorAssignment(prefGroup, isEditable));
    	}
    	if (isShowInstructor()){
    		row.addCell(this.buildInstructor(prefGroup, isEditable));
    	}
    	if (getDisplayTimetable() && isShowTimetable()){
    		row.addCell(this.buildAssignedTime(classAssignment, prefGroup, isEditable));
    		row.addCell(this.buildAssignedRoom(classAssignment, prefGroup, isEditable));
    		row.addCell(this.buildAssignedRoomCapacity(classAssignment, prefGroup, isEditable));
    	} 
    	if (isShowTitle()){
    		row.addCell(this.initNormalCell("", isEditable));
    	}
    	if (isShowCredit()){
    		row.addCell(this.initNormalCell("", isEditable));
    	} 
    	if (isShowSubpartCredit()){
            row.addCell(this.buildCredit(prefGroup, isEditable));     		
    	} 
    	if (isShowConsent()){
    		row.addCell(this.initNormalCell("", isEditable));
    	}
    	if (isShowSchedulePrintNote()){
            row.addCell(this.buildSchedulePrintNote(prefGroup, isEditable));     		
    	} 
    	if (isShowNote()){
            row.addCell(this.buildNote(prefGroup, isEditable));
    	}
    	if (isShowExam()) {
    	    if (prefGroup instanceof Class_) {
    	        TreeSet exams = getExams((Class_)prefGroup);
    	        for (Iterator<Exam> i = exams.iterator(); i.hasNext(); ) {
                	if (!getSessionContext().hasPermission(i.next(), Right.ExaminationView))
                		i.remove();
                }
    	        if (isShowExamName()) {
    	            row.addCell(this.buildExamName(exams, isEditable));
    	        }
    	        if (isShowExamTimetable()) {
    	            row.addCell(this.buildExamPeriod(examAssignment, exams, isEditable));
    	            row.addCell(this.buildExamRoom(examAssignment, exams, isEditable));
    	        }
    	    } else {
    	        if (isShowExamName()) {
    	            row.addCell(this.initNormalCell("", isEditable));
    	        }
                if (isShowExamTimetable()) {
                    row.addCell(this.initNormalCell("", isEditable));
                    row.addCell(this.initNormalCell("", isEditable));
                }
    	    }
    	}
    	if (isShowLms()) {
    		row.addCell(this.buildLmsInfo(prefGroup, isEditable));
    	}
    	if (isShowWaitlistMode()) {
    		row.addCell(initNormalCell("", isEditable));        	
        }
    }
    
    private void buildSchedulingSubpartRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TableInterface table, CourseOffering co, SchedulingSubpart ss, int indentSpaces){
    	boolean isEditable = getSessionContext().hasPermission(ss, Right.SchedulingSubpartDetail);
        boolean isOffered = !ss.getInstrOfferingConfig().getInstructionalOffering().isNotOffered();        

    	LineInterface row = this.initRow(isOffered);

        if (isEditable && isOffered)
        	row.setURL("subpart?id="+ss.getUniqueId());
        
        this.buildClassOrSubpartRow(classAssignment, examAssignment, row, co, ss, indentSpaces, isEditable, null);
        if (isSimple() && isOffered) row.setBgColor("#E1E1E1");
        table.addLine(row);
    }
    
    private void buildSchedulingSubpartRows(Vector subpartIds, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TableInterface table, CourseOffering co, SchedulingSubpart ss, int indentSpaces){
    	if (subpartIds!=null) subpartIds.add(ss.getUniqueId());
        this.buildSchedulingSubpartRow(classAssignment, examAssignment, table, co, ss, indentSpaces);
        Set childSubparts = ss.getChildSubparts();
        
		if (childSubparts != null && !childSubparts.isEmpty()){
		    
		    ArrayList childSubpartList = new ArrayList(childSubparts);
		    Collections.sort(childSubpartList, new SchedulingSubpartComparator());
            Iterator it = childSubpartList.iterator();
            SchedulingSubpart child = null;
            
            while (it.hasNext()){              
                child = (SchedulingSubpart) it.next();
                buildSchedulingSubpartRows(subpartIds, classAssignment, examAssignment, table, co, child, indentSpaces + 1);
            }
        }
    }
 
    protected void buildClassRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, int ct, TableInterface table, CourseOffering co, Class_ aClass, int indentSpaces, String prevLabel){
    	boolean isHeaderRow = false;
    	boolean isEditable = getSessionContext().hasPermission(aClass, Right.ClassDetail);
    	LineInterface row = this.initRow(isHeaderRow);
       
        if (isEditable)
        	row.setURL("clazz?id=" + aClass.getUniqueId().toString());

        if (aClass.isCancelled()) {
        	row.setStyle("color: gray; font-style: italic;");
        	row.setTitle(MSG.classNoteCancelled(aClass.getClassLabel(co)));
        }

        if (getDisplayConflicts() && classAssignment != null) {
        	Set<AssignmentInfo> conflicts = null;
        	try { conflicts = classAssignment.getConflicts(aClass.getUniqueId()); } catch (Exception e) {}
        	if (conflicts != null && !conflicts.isEmpty()) {
        		row.setBgColor("#fff0f0");
    			String s = "";
    			for (AssignmentInfo c: conflicts) {
    				if (!s.isEmpty()) s += ", ";
    				s += (c.getClassName() + " " + c.getPlacement().getName(CONSTANTS.useAmPm())).trim();
    			}
				row.setWarning(MSG.classIsConflicting(aClass.getClassLabel(co), s));
        	} else {
        		Set<TimeBlock> ec = null;
        		try { ec = classAssignment.getConflictingTimeBlocks(aClass.getUniqueId()); } catch (Exception e) {}
        		if (ec != null && !ec.isEmpty()) {
        			String s = "";
        			String lastName = null, lastType = null;
        			for (TimeBlock t: ec) {
        				if (lastName == null || !lastName.equals(t.getEventName()) || !lastType.equals(t.getEventType())) {
        					lastName = t.getEventName(); lastType = t.getEventType();
        					if (!s.isEmpty()) s += ", ";
            				s += lastName + " (" + lastType + ")";
        				}
        			}
            		row.setBgColor("#fff0f0");
            		row.setWarning(MSG.classIsConflicting(aClass.getClassLabel(co), s));
        		}
        	}
        }

        if (getDisplayDatePatternDifferentWarning() && classAssignment != null) {
        	AssignmentInfo a = null;
        		boolean changedSinceCommit = false;
        		DatePattern newDp = null;
			try {
				a = classAssignment.getAssignment(aClass);
				if(a != null && a.isCommitted() && a.getDatePattern() != null && !a.getDatePattern().equals(aClass.effectiveDatePattern())) {
					if (aClass.effectiveDatePattern().isPatternSet()
							&& a.getDatePattern().getParents() != null 
							&& a.getDatePattern().getParents().contains(aClass.effectiveDatePattern())) {
						changedSinceCommit = false;
					} else {
						changedSinceCommit = true;
						newDp = aClass.effectiveDatePattern();
					}
				}
			} catch (Exception e) { 
				Debug.error(e);
			}

        	if (changedSinceCommit) {
        		row.setBgColor("#fff0f0");
    			String msg = row.getTitle();
    			if (msg == null) { msg = ""; }
    			else { msg = msg + " "; }
    			msg += MSG.datePatternCommittedIsDifferent(aClass.getClassLabel(co), a.getDatePattern().getName(), newDp.getName());
				row.setWarning(msg);				
        	}
        }

        this.buildClassOrSubpartRow(classAssignment, examAssignment, row, co, aClass, indentSpaces, isEditable && !aClass.isCancelled(), prevLabel);
        table.addLine(row);
    }
    
    private void buildClassRows(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, int ct, TableInterface table, CourseOffering co, Class_ aClass, int indentSpaces, String prevLabel){

        buildClassRow(classAssignment, examAssignment, ct, table, co, aClass, indentSpaces, prevLabel);
    	Set childClasses = aClass.getChildClasses();

    	if (childClasses != null && !childClasses.isEmpty()){
        
    	    ArrayList childClassesList = new ArrayList(childClasses);
            Collections.sort(childClassesList, iClassComparator);
            
            Iterator it = childClassesList.iterator();
            Class_ child = null;
            String previousLabel = aClass.htmlLabel();
            while (it.hasNext()){              
                child = (Class_) it.next();
                buildClassRows(classAssignment, examAssignment, ct, table, co, child, indentSpaces + 1, previousLabel);
            }
        }
    }


	protected void buildConfigRow(Vector subpartIds, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TableInterface table, CourseOffering co, InstrOfferingConfig ioc, boolean printConfigLine, boolean printConfigReservation) {
	    boolean isHeaderRow = true;
	    boolean isEditable = getSessionContext().hasPermission(ioc.getInstructionalOffering(), Right.InstructionalOfferingDetail);
	    String configName = ioc.getName();
	    boolean unlimited = ioc.isUnlimitedEnrollment().booleanValue();
	    boolean hasConfig = false;
		if (printConfigLine) {
		    LineInterface row = this.initRow(isHeaderRow);
	        CellInterface cell = null;
        	if (isShowLabel()){
        	    if (configName==null || configName.trim().length()==0)
        	        configName = ioc.getUniqueId().toString();
        	    if (ioc.getInstructionalMethod() != null)
        	    	cell = this.initNormalCell(MSG.labelConfigurationWithInstructionalMethod(configName, ioc.getInstructionalMethod().getReference()), isEditable)
        	    		.setTitle(ioc.getInstructionalMethod().getLabel());        	   
        	    else
        	    	cell = this.initNormalCell(MSG.labelConfiguration(configName), isEditable);
            	cell.setIndent(1);
        	    cell.setNoWrap(true);
        	    row.addCell(cell);

        	}
        	if (isShowDivSec()){
        		row.addCell(initNormalCell("", isEditable));
    		}
        	if (isShowDemand()){
    			row.addCell(initNormalCell("", isEditable));
    		}
        	if (isShowProjectedDemand()){
    			row.addCell(initNormalCell("", isEditable));
    		} 
        	if (isShowLimit()){
    		    cell = this.initNormalCell(
    	            	(unlimited ? "\u221E" : ioc.getLimit().toString()),  
    		            isEditable);
    		    if (unlimited)
    		    	cell.addStyle("font-size: +1");
    		    cell.setTextAlignment(Alignment.RIGHT);
	            row.addCell(cell);
        	} 
        	if (isShowSnapshotLimit()){
    			row.addCell(initNormalCell("", isEditable));
        	} 
        	if (isShowRoomRatio()){
                row.addCell(initNormalCell("", isEditable));
        	} 
        	if (isShowManager()){
                row.addCell(initNormalCell("", isEditable));
        	} 
        	if (isShowFundingDepartment()){
                row.addCell(initNormalCell("", isEditable));
        	} 
        	if (isShowDatePattern()){
                row.addCell(initNormalCell("", isEditable));
	       	} 
        	if (isShowMinPerWk()){
                row.addCell(initNormalCell("", isEditable));
        	} 
        	if (isShowTimePattern()){
	       		row.addCell(initNormalCell("", isEditable));
        	} 
        	if (isShowPreferences() || getDisplayInstructorPrefs()){
		        for (int j = 0; j < getPreferenceColumns(); j++) {
		            row.addCell(initNormalCell("", isEditable));
		        }
        	} 
        	if (isShowInstructorAssignment()) {
        		row.addCell(initNormalCell("", isEditable));
        	}
        	if (isShowInstructor()){
                row.addCell(initNormalCell("", isEditable));
        	} 
        	if (getDisplayTimetable() && isShowTimetable()){
        		row.addCell(initNormalCell("", isEditable));
        		row.addCell(initNormalCell("", isEditable));
        		row.addCell(initNormalCell("", isEditable));
        	} 
        	if (isShowTitle()){
        		row.addCell(this.initNormalCell("", isEditable));
        	}
        	if (isShowCredit()){
                row.addCell(initNormalCell("", isEditable));
        	} 
        	if (isShowSubpartCredit()){
                row.addCell(initNormalCell("", isEditable));
        	} 
        	if (isShowConsent()){
        		row.addCell(this.initNormalCell("", isEditable));
        	}
        	if (isShowSchedulePrintNote()){
                row.addCell(initNormalCell("", isEditable));
        	} 
        	if (isShowNote()){
                row.addCell(initNormalCell("", isEditable));
        	}
	        
            if (isShowExam()) {
                TreeSet exams = new TreeSet(Exam.findAll(ExamOwner.sOwnerTypeConfig,ioc.getUniqueId()));
                for (Iterator<Exam> i = exams.iterator(); i.hasNext(); ) {
                	if (!getSessionContext().hasPermission(i.next(), Right.ExaminationView))
                		i.remove();
                }
                if (isShowExamName()) {
                    row.addCell(this.buildExamName(exams, isEditable));
                }
                if (isShowExamTimetable()) {
                    row.addCell(this.buildExamPeriod(examAssignment, exams, isEditable));
                    row.addCell(this.buildExamRoom(examAssignment, exams, isEditable));
                }
            }
    		if (isShowLms()) {
                row.addCell(initNormalCell("", isEditable));			
    		}
    		if (isShowWaitlistMode()) {
    			row.addCell(initNormalCell("", isEditable));
    		}
    		if (isSimple()) row.setBgColor("#C8C8C8");
	        table.addLine(row);
	        hasConfig = true;
		}
        ArrayList subpartList = new ArrayList(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());
        Iterator it = subpartList.iterator();
        SchedulingSubpart ss = null;
        while(it.hasNext()){
            ss = (SchedulingSubpart) it.next();
            if (ss.getParentSubpart() == null){
                buildSchedulingSubpartRows(subpartIds, classAssignment, examAssignment, table, co, ss, (hasConfig ? 2 : 1));
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
					Collections.sort(classes,iClassComparator);
					Iterator cIt = classes.iterator();					
					Class_ c = null;
					while (cIt.hasNext()) {
						c = (Class_) cIt.next();
						buildClassRows(classAssignment, examAssignment, ++ct, table, co, c, 1, prevLabel);
						prevLabel = c.htmlLabel();
					}
				}
			}
		}
        

   }

    private void buildConfigRows(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TableInterface table, CourseOffering co, Set instrOfferingConfigs, boolean printConfigLine, boolean printConfigReservation) {
        Iterator it = instrOfferingConfigs.iterator();
        InstrOfferingConfig ioc = null;
        while (it.hasNext()){
            ioc = (InstrOfferingConfig) it.next();
            buildConfigRow(null, classAssignment, examAssignment, table, co, ioc, printConfigLine && instrOfferingConfigs.size()>1, printConfigReservation);
        }
    }

    private void addInstrOffrRowsToTable(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TableInterface table, InstructionalOffering io, Long subjectAreaId){
        CourseOffering co = io.findSortCourseOfferingForSubjectArea(subjectAreaId);
        boolean isEditable = getSessionContext().hasPermission(io, Right.InstructionalOfferingDetail);
        LineInterface row = (this.initRow(true));
        if (isEditable) row.setURL("instructionalOfferingDetail.action?op=view&io=" + io.getUniqueId());
        boolean isManagedAs = !co.isIsControl().booleanValue(); 
        
        CellInterface cell = null;
    	if (isShowLabel()){
    		row.addCell(subjectAndCourseInfo(io, co, isEditable));
    	}
    	if (isShowDivSec()){
    		row.addCell(initNormalCell("", isEditable));
		}
    	if (isShowDemand()){
    		if (sessionHasEnrollments(io.getSessionId())){
    			cell = initNormalCell((io.getEnrollment() != null?io.getEnrollment().toString(): "0"), isEditable && co.isIsControl().booleanValue());
    		} else {
		        cell = initNormalCell((io.getDemand() != null?io.getDemand().toString(): "0"), isEditable && co.isIsControl().booleanValue());
	    		if (co.isIsControl().booleanValue() && !io.isNotOffered().booleanValue() && (io.getDemand()==null || io.getDemand().intValue()==0)) {
	    			cell.setColor("red");
	    			cell.addStyle("='font-weight: bold;");
	    		}
    		}
	        cell.setTextAlignment(Alignment.RIGHT);
	        row.addCell(cell);
		}
    	if (isShowProjectedDemand()){
	        cell = initNormalCell((io.getProjectedDemand() != null?io.getProjectedDemand().toString():"0"), isEditable && co.isIsControl().booleanValue());
	        cell.setTextAlignment(Alignment.RIGHT);
	        row.addCell(cell);
		} 
    	if (isShowLimit()){
			boolean unlimited = false;
			for (Iterator x=io.getInstrOfferingConfigs().iterator();!unlimited && x.hasNext();)
				if ((((InstrOfferingConfig)x.next())).isUnlimitedEnrollment().booleanValue())
					unlimited = true;
			if (unlimited) {
				cell = initNormalCell("\u221E", co.isIsControl().booleanValue());
				cell.addStyle("font-size: +1");
			} else
				cell = initNormalCell(io.getLimit() != null?io.getLimit().toString():"", isEditable && co.isIsControl().booleanValue());
            cell.setTextAlignment(Alignment.RIGHT);
            row.addCell(cell);
    	} 
    	if (isShowSnapshotLimit()){
			boolean unlimited = false;
			for (Iterator x=io.getInstrOfferingConfigs().iterator();!unlimited && x.hasNext();)
				if ((((InstrOfferingConfig)x.next())).isUnlimitedEnrollment().booleanValue())
					unlimited = true;
			if (unlimited) {
				cell = initNormalCell("\u221E", co.isIsControl().booleanValue());
				cell.addStyle("font-size: +1");
			} else
				cell = initNormalCell(io.getSnapshotLimit() != null?io.getSnapshotLimit().toString():"", isEditable && co.isIsControl().booleanValue());
            cell.setTextAlignment(Alignment.RIGHT);
            row.addCell(cell);
    	} 
    	int emptyCells = 0;
    	cell = null;
    	if (isShowRoomRatio()){
    		emptyCells ++;
    	} 
    	if (isShowManager()){
    		emptyCells ++;
    	}
    	if (isShowFundingDepartment()){
    		emptyCells ++;
    	} 
    	if (isShowDatePattern()){
    		emptyCells ++;
       	}
    	if (isShowMinPerWk()){
    		emptyCells ++;
    	} 
    	if (isShowTimePattern()) {
    		emptyCells ++;
    	}
    	if (isShowPreferences() || getDisplayInstructorPrefs()) {
    		emptyCells += getPreferenceColumns();
    	}
    	if (isShowInstructorAssignment()) {
    		emptyCells ++;
    	}
    	if (isShowInstructor()){
    		emptyCells ++;
    	}
    	if (getDisplayTimetable() && isShowTimetable()) {
    		emptyCells += 3;
    	}
    	if (emptyCells>0) {
    		cell = initNormalCell("", isEditable);
            if (isManagedAs) {
            	if (!isShowTitle() && io.getControllingCourseOffering().getTitle()!=null) {
            		if (co.getTitle()!=null && co.getTitle().length()>0) {
            			CellInterface c = cell.add(co.getTitle()).addStyle("font-weight: bold;");
            			c.add(" (");
            			c.add(MSG.crossListManagedAs(io.getControllingCourseOffering().getCourseName()))
            				.setTitle(io.getControllingCourseOffering().getCourseNameWithTitle());
            			c.add(")");
            		} else {
            			cell.add(MSG.crossListManagedAs(io.getControllingCourseOffering().getCourseName()))
        					.setTitle(io.getControllingCourseOffering().getCourseNameWithTitle());
            		}
                    for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
                    	CourseOffering x = (CourseOffering)it.next();
                    	cell.add(x.getTitle())
                    		.setTitle(x.getCourseNameWithTitle())
                    		.setColor(disabledColor)
                    		.setIndent(1)
                    		.setInline(false);
                    }
                    cell.setColor(disabledColor);
            	} else {
            		cell.setIndent(1);
            		cell.setText(MSG.crossListManagedAs(io.getControllingCourseOffering().getCourseName()));
            		cell.setTitle(io.getControllingCourseOffering().getCourseNameWithTitle());
            	}
            } else {
            	if (!isShowTitle() && io.getControllingCourseOffering().getTitle()!=null) {
            		cell.add(co.getTitle()).addStyle("font-weight: bold;");
            		for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
                    	CourseOffering x = (CourseOffering)it.next();
                    	cell.add(x.getTitle())
                		.setTitle(x.getCourseNameWithTitle())
                		.setColor(disabledColor)
                		.setIndent(1)
                		.setInline(false);
                    }

            	}
            }
            cell.setColSpan(emptyCells);
            cell.setTextAlignment(Alignment.LEFT);
            cell.setInline(false);
            row.addCell(cell);
    	}
    	if (isShowTitle()){
    		cell = initNormalCell("", isEditable);
    		if (co.getTitle() != null) {
        		if (isManagedAs) {
        			cell.setText(co.getTitle());
        			cell.setColor(disabledColor);
        		} else
        			cell.setText(co.getTitle());
    		}
    		for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
            	CourseOffering x = (CourseOffering)it.next();
            	cell.add(x.getTitle() == null ? "" : x.getTitle())
            		.setColor(disabledColor)
            		.setInline(false)
            		.setNoWrap(true);
    		}
    		if (io.getCourseOfferings().size() > 1) cell.setNoWrap(true);
    		row.addCell(cell);
    	}
    	if (isShowCredit()){
    		cell = initNormalCell("", isEditable);
			if (co.getCredit() != null) {
				cell.setText(co.getCredit().creditAbbv());
				cell.setTitle(co.getCredit().creditAbbv());
				cell.setTextAlignment(Alignment.RIGHT);
			}
    		if (co.isIsControl())
    			cell.addStyle("font-weight: bold;");
    		else
    			cell.setColor(disabledColor);
    		for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
            	CourseOffering x = (CourseOffering)it.next();
            	if (x.getCredit() != null) {
            		CellInterface c = cell.add(x.getCredit().creditAbbv())
            			.setTitle(x.getCredit().creditText())
            			.setColor(disabledColor)
            			.setInline(false);
            		if (x.isIsControl())
            			c.addStyle("font-weight: bold;");
            	} else {
            		cell.add("").setInline(false);
            	}
    		}
    		if (io.getCourseOfferings().size() > 1) cell.setNoWrap(true);
            row.addCell(cell);   
    	}
    	if (isShowSubpartCredit()){
            row.addCell(initNormalCell("", isEditable));
    	} 
    	if (isShowConsent()){
    		cell = initNormalCell("", isEditable);
    		if (co.isIsControl())
    			cell.addStyle("font-weight: bold;");
    		else
    			cell.setColor(disabledColor);
    		if (co.getConsentType() == null)
    			cell.setText(MSG.noConsentRequired());
    		else {
    			cell.setText(co.getConsentType().getAbbv());
    			cell.setTitle(co.getConsentType().getLabel());
    		}
    		for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
            	CourseOffering x = (CourseOffering)it.next();
            	if (x.getConsentType() != null) {
            		CellInterface c = cell.add(x.getConsentType().getAbbv())
            			.setTitle(x.getConsentType().getLabel())
            			.setColor(disabledColor)
            			.setInline(false);
            		if (x.isIsControl())
            			c.addStyle("font-weight: bold;");
            	} else {
            		cell.add("").setInline(false);
            	}
    		}
    		if (io.getCourseOfferings().size() > 1) cell.setNoWrap(true);
            row.addCell(cell);     		
    	}
    	if (isShowSchedulePrintNote()){
            row.addCell(buildSchedulePrintNote(io, isEditable));     		
    	}
    	if (isShowNote()){
            row.addCell(buildNote(io, isEditable));
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
            	if (!getSessionContext().hasPermission(i.next(), Right.ExaminationView))
            		i.remove();
            }
            if (isShowExamName()) {
                row.addCell(this.buildExamName(exams, isEditable));
            }
            if (isShowExamTimetable()) {
                row.addCell(this.buildExamPeriod(examAssignment, exams, isEditable));
                row.addCell(this.buildExamRoom(examAssignment, exams, isEditable));
            }
        }
        if (isShowLms()) {
            row.addCell(initNormalCell("", isEditable));        	
        }
        if (isShowWaitlistMode()) {
        	switch (io.getEffectiveWaitListMode()) {
        	case Disabled:
        		row.addCell(initNormalCell(MSG.waitListDisabledShort(), isEditable));
        		break;
        	case ReSchedule:
        		row.addCell(initNormalCell(MSG.waitListRescheduleShort(), isEditable));
        		break;
        	case WaitList:
        		row.addCell(initNormalCell(MSG.waitListEnabledShort(), isEditable));
        		break;
        	default:
        		row.addCell(initNormalCell("", isEditable));
        	}
        }
        if (isSimple()) row.setBgColor("#C8C8C8");
        table.addLine(row);
        if (io.getInstrOfferingConfigs() != null & !io.getInstrOfferingConfigs().isEmpty()){
        	TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
        	configs.addAll(io.getInstrOfferingConfigs());
            buildConfigRows(classAssignment, examAssignment, table, io.getControllingCourseOffering(), configs, true, false);
        }
    }
    
    protected TableInterface initTable(Long sessionId, String durationColName){
    	TableInterface table = new TableInterface();
        buildTableHeader(table, sessionId, durationColName);
        return table;
    }
    
    protected TableInterface initTable(Long sessionId){
    	ClassDurationType dtype = ClassDurationType.findDefaultType(sessionId, null);
    	return initTable(sessionId, dtype == null ? MSG.columnMinPerWk() : dtype.getLabel());
    }
    
    public void generateTableForInstructionalOfferings(
            ClassAssignmentProxy classAssignment, 
            ExamAssignmentProxy examAssignment,
            FilterInterface filter, 
            String[] subjectAreaIds, 
            List<TableInterface> tables){
    	
    	this.setVisibleColumns(filter);
    	
		String courseNbr = filter.getParameterValue("courseNbr");
		String waitList = filter.getParameterValue("waitlist");
		boolean allCoursesAreGiven = (courseNbr==null || courseNbr.isEmpty()) &&
				("A".equals(waitList) || waitList == null || waitList.isEmpty());

    	
    	List<Long> navigationOfferingIds = new ArrayList<Long>();
    	
    	for (String subjectAreaId: subjectAreaIds) {
    		generateTableForInstructionalOfferings(classAssignment, examAssignment,
        			InstructionalOffering.search(
        					getCurrentAcademicSessionId(),
        					Long.valueOf(subjectAreaId),
        					filter.getParameterValue("courseNbr"),
        					true, false, false, false, false, false, filter.getParameterValue("waitlist")), 
         			Long.valueOf(subjectAreaId),
         			allCoursesAreGiven,
        			tables,
        			new ClassCourseComparator(filter.getParameterValue("sortBy", "NAME"), classAssignment, false),
        			navigationOfferingIds
        	);
    	}
    	
        Navigation.set(getSessionContext(), Navigation.sInstructionalOfferingLevel, navigationOfferingIds);
    }
    
    protected void generateTableForInstructionalOfferings(
            ClassAssignmentProxy classAssignment, 
            ExamAssignmentProxy examAssignment,
            TreeSet<InstructionalOffering> insructionalOfferings, 
            Long subjectAreaId, 
            boolean allCoursesAreGiven,
            List<TableInterface> tables,
            Comparator classComparator,
            List<Long> navigationOfferingIds) {
    	
    	if (insructionalOfferings == null) return;
    	
    	if (classComparator!=null)
    		setClassComparator(classComparator);
    	
    	SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(Long.valueOf(subjectAreaId));
        
    	if (isShowTimetable()) {
            boolean hasTimetable = false;
            if (getSessionContext().hasPermission(Right.ClassAssignments) && classAssignment != null) {
            	try {
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
            	} catch (Exception e) {}
            }
            setDisplayTimetable(hasTimetable);
    	}
    	
    	if (isShowExam())
    	    setShowExamTimetable(examAssignment!=null || Exam.hasTimetable(getCurrentAcademicSessionId()));
    	
        ArrayList notOfferedOfferings = new ArrayList();
        ArrayList offeredOfferings = new ArrayList();
        
        Iterator it = insructionalOfferings.iterator();
        InstructionalOffering io = null;
        boolean hasOfferedCourses = false;
        boolean hasNotOfferedCourses = false;
        
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
         
        if (hasOfferedCourses || allCoursesAreGiven) {
        	it = offeredOfferings.iterator();
            TableInterface offeredTable = this.initTable(getCurrentAcademicSessionId());
            
            while (it.hasNext()){
                io = (InstructionalOffering) it.next();
                if (navigationOfferingIds != null)
                	navigationOfferingIds.add(io.getUniqueId());
                this.addInstrOffrRowsToTable(classAssignment, examAssignment, offeredTable, io, subjectAreaId);            	
            }
            
            offeredTable.setAnchor("AO" + subjectAreaId);
            if (isFilterWaitlist())
            	offeredTable.setName(MSG.labelOfferedWaitListedCourses(subjectArea.getSubjectAreaAbbreviation()));
	    	else if (isFilterNonWaitlist())
	    		offeredTable.setName( MSG.labelOfferedNotWaitListedCourses(subjectArea.getSubjectAreaAbbreviation()));
	    	else if (isFilterCoursesAllowingReScheduling())
	    		offeredTable.setName(MSG.labelOfferedCoursesAllowingReScheduling(subjectArea.getSubjectAreaAbbreviation()));
	    	else if (isFilterCoursesNotAllowingReScheduling())
	    		offeredTable.setName(MSG.labelOfferedCoursesNotAllowingReScheduling(subjectArea.getSubjectAreaAbbreviation()));
	    	else if (isFilterNonWaitedCoursesAllowingReScheduling())
	    		offeredTable.setName(MSG.labelOfferedNotWaitListedCoursesAllowingReScheduling(subjectArea.getSubjectAreaAbbreviation()));
	    	else
	    		offeredTable.setName(MSG.labelOfferedCourses(subjectArea.getSubjectAreaAbbreviation()));
            
            if(!hasOfferedCourses) {
            	offeredTable.setErrorMessage(MSG.errorNoCoursesOffered(subjectArea.getSubjectAreaAbbreviation()));
            	if (!isSimple()) offeredTable.getHeader().clear();
            }
            
            if (allCoursesAreGiven)
            	offeredTable.addLink(new LinkInteface()
            			.setHref("#AN" + subjectAreaId)
            			.setText(MSG.labelNotOfferedCourses(subjectArea.getSubjectAreaAbbreviation()))
            			);
            tables.add(offeredTable);
        }
        
        if (hasNotOfferedCourses || allCoursesAreGiven) {
            it = notOfferedOfferings.iterator();
            TableInterface notOfferedTable = this.initTable(getCurrentAcademicSessionId());
            while (it.hasNext()){
                io = (InstructionalOffering) it.next();
                if (navigationOfferingIds != null)
                	navigationOfferingIds.add(io.getUniqueId());
                this.addInstrOffrRowsToTable(classAssignment, examAssignment, notOfferedTable, io, subjectAreaId);            	
            }
            notOfferedTable.setAnchor("AN" + subjectAreaId);
            notOfferedTable.setName(MSG.labelNotOfferedCourses(subjectArea.getSubjectAreaAbbreviation()));
            
            if (!hasNotOfferedCourses) {
            	notOfferedTable.setErrorMessage(MSG.errorAllCoursesOffered(subjectArea.getSubjectAreaAbbreviation()));
            	if (!isSimple()) notOfferedTable.getHeader().clear();
            }
            
            if (allCoursesAreGiven)
            	notOfferedTable.addLink(new LinkInteface()
            			.setHref("#AO" + subjectAreaId)
            			.setText(MSG.labelOfferedCourses(subjectArea.getSubjectAreaAbbreviation()))
            			);
            
            tables.add(notOfferedTable);            
        }
        
        if (navigationOfferingIds != null)
        	Navigation.set(getSessionContext(), Navigation.sInstructionalOfferingLevel, navigationOfferingIds);
    }
    
    public void generateConfigTablesForInstructionalOffering(
    		ClassAssignmentProxy classAssignment, 
    		ExamAssignmentProxy examAssignment,
    		InstructionalOffering io,
    		OfferingDetailResponse response){
    	
        if (CommonValues.Yes.eq(getUser().getProperty(UserProperty.ClassesKeepSort))) {
    		setClassComparator(
    			new ClassCourseComparator(
    					getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
    					classAssignment,
    					false
    			)
    		);
    	}
		
		Vector subpartIds = new Vector();
		if (io.getInstrOfferingConfigs() != null){
        	TreeSet<InstrOfferingConfig> configs = new TreeSet<InstrOfferingConfig>(new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
        	configs.addAll(io.getInstrOfferingConfigs());
        	for (InstrOfferingConfig ioc: configs) {
        		response.addConfig(generateTableForInstructionalOfferingConfig(subpartIds, classAssignment, examAssignment, ioc));
        	}
        }
		
		Navigation.set(getSessionContext(), Navigation.sSchedulingSubpartLevel, subpartIds);
    }
    
    private OfferingConfigInterface generateTableForInstructionalOfferingConfig(Vector subpartIds, ClassAssignmentProxy classAssignment,
    		ExamAssignmentProxy examAssignment, InstrOfferingConfig ioc) {
    	
    	if (CommonValues.Yes.eq(getUser().getProperty(UserProperty.ClassesKeepSort))) {
    		setClassComparator(
    			new ClassCourseComparator(
    					getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
    					classAssignment,
    					false
    			)
    		);
    	}
    	
    	OfferingConfigInterface ret = new OfferingConfigInterface();
    	
    	ret.setConfigId(ioc.getUniqueId());
	    if (ioc.getInstructionalMethod() != null)
	    	ret.setName(MSG.labelConfigurationWithInstructionalMethod(ioc.getName(), ioc.getInstructionalMethod().getLabel()));
	    else
	    	ret.setName(MSG.labelConfiguration(ioc.getName()));
		if (!ioc.getInstructionalOffering().isNotOffered()) {
			if (getSessionContext().hasPermission(ioc, Right.InstrOfferingConfigEdit))
				ret.addOperation("config-edit");
			if (getSessionContext().hasPermission(ioc, Right.MultipleClassSetup))
				ret.addOperation("class-setup");
			if (getSessionContext().hasPermission(ioc, Right.AssignInstructors))
				ret.addOperation("assign-instructors");
		}

    	setDisplayDistributionPrefs(false);
		setShowLabel(true);
		setShowDemand(sessionHasEnrollments(getCurrentAcademicSessionId()));
		setShowProjectedDemand(false);
		setShowMinPerWk(true);
		setShowLimit(true);
		setShowSnapshotLimit(ioc.getInstructionalOffering().getSession().getCurrentSnapshotDate() != null);
		setShowRoomRatio(true);
		setShowFundingDepartment(false);
		if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
    		ss: for (SchedulingSubpart ss: ioc.getSchedulingSubparts()) {
	        	for (Class_ c: ss.getClasses())
	        		if (c.getFundingDept() != null) { setShowFundingDepartment(true); break ss; }
	        }
    	}
		setShowManager(true);
		setShowDatePattern(true);
		setShowTimePattern(true);
		setShowPreferences(true);
		setShowInstructor(true);
		setShowTimetable(true);
		setShowCredit(false);
		setShowNote(false);
		setShowConsent(false);
		setShowTitle(false);
		setShowExam(false);
		setShowLms(LearningManagementSystemInfo.isLmsInfoDefinedForSession(ioc.getSessionId()));
		setShowWaitlistMode(false);
		setDisplayConflicts(true);

    	if (isShowTimetable()) {
        	boolean hasTimetable = false;
        	if (getSessionContext().hasPermission(Right.ClassAssignments) && classAssignment != null) {
        		try {
                	if (classAssignment instanceof CachedClassAssignmentProxy) {
                		Vector allClasses = new Vector();
	        			for (Iterator k=ioc.getSchedulingSubparts().iterator();!hasTimetable && k.hasNext();) {
	        				SchedulingSubpart ss = (SchedulingSubpart)k.next();
	        				for (Iterator l=ss.getClasses().iterator();l.hasNext();) {
	        					Class_ clazz = (Class_)l.next();
	        					allClasses.add(clazz);
	        				}
	        			}
                		((CachedClassAssignmentProxy)classAssignment).setCache(allClasses);
                		hasTimetable = !classAssignment.getAssignmentTable(allClasses).isEmpty();
                	} else {
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
        		} catch (Exception e) {}
        	}
        	setDisplayTimetable(hasTimetable);
        }
        setShowDivSec(false);
        setShowInstructorAssignment(false);
        setShowSchedulePrintNote(false);
		setShowSubpartCredit(false);
        CourseOffering co = ioc.getInstructionalOffering().getControllingCourseOffering();
        for (SchedulingSubpart ss: ioc.getSchedulingSubparts()) {
        	if (ss.isInstructorAssignmentNeeded()) setShowInstructorAssignment(true);
        	if (ss.getCredit() != null) setShowSubpartCredit(true);
        	for (Class_ c: ss.getClasses()) {
        		String divSec = (isShowOriginalDivSecs() ? c.getClassSuffix() : c.getClassSuffix(co));
        		if (divSec != null && !divSec.isEmpty()) setShowDivSec(true);
        		if (c.getSchedulePrintNote() != null && !c.getSchedulePrintNote().trim().isEmpty()) setShowSchedulePrintNote(true);
        		if (c.isInstructorAssignmentNeeded()) setShowInstructorAssignment(true);
        	}
        }
        setDisplayInstructorPrefs(false);
        ClassDurationType dtype = ioc.getEffectiveDurationType();
        
        buildTableHeader(ret, getCurrentAcademicSessionId(), dtype == null ? MSG.columnMinPerWk() : dtype.getLabel());
        buildConfigRow(subpartIds, classAssignment, examAssignment, ret, ioc.getInstructionalOffering().getControllingCourseOffering(), ioc, !getDisplayConfigOpButtons(), true);
        ret.setAnchor("ioc" + ioc.getUniqueId());
        
        return ret;
    }


	protected void setVisibleColumns(TableInterface.FilterInterface filter){
		setShowLabel(true);
		setShowDivSec("1".equals(filter.getParameterValue("divSec", "0")));
		setShowDemand("1".equals(filter.getParameterValue("demand", "1")));
		setShowProjectedDemand("1".equals(filter.getParameterValue("projectedDemand", "1")));
		setShowMinPerWk("1".equals(filter.getParameterValue("minPerWk", "1")));
		setShowLimit("1".equals(filter.getParameterValue("limit", "1")));
		setShowSnapshotLimit("1".equals(filter.getParameterValue("snapshotLimit", "1")));
		setShowRoomRatio("1".equals(filter.getParameterValue("roomLimit", "1")));
		setShowFundingDepartment("1".equals(filter.getParameterValue("fundingDepartment", "1")));
		setShowManager("1".equals(filter.getParameterValue("manager", "1")));
		setShowDatePattern("1".equals(filter.getParameterValue("datePattern", "1")));
		setShowTimePattern("1".equals(filter.getParameterValue("timePattern", "1")));
		setShowPreferences("1".equals(filter.getParameterValue("preferences", "1")));
		setShowInstructor("1".equals(filter.getParameterValue("instructor", "1")));
		if (filter.hasParameter("timetable"))
			setShowTimetable("1".equals(filter.getParameterValue("timetable", "1")));
		else
			setShowTimetable(true);
		setShowCredit("1".equals(filter.getParameterValue("credit", "0")));
		setShowSubpartCredit("1".equals(filter.getParameterValue("subpartCredit", "0")));
		setShowSchedulePrintNote("1".equals(filter.getParameterValue("schedulePrintNote", "0")));
		setShowNote("1".equals(filter.getParameterValue("note", "0")));
		setShowConsent("1".equals(filter.getParameterValue("consent", "0")));
		setShowTitle("1".equals(filter.getParameterValue("title", "0")));
		if (filter.hasParameter("exams")) {
		    setShowExam("1".equals(filter.getParameterValue("exams", "1")));
		} else {
		    setShowExam(true);
		}
		if (filter.hasParameter("instructorAssignment")) {
			setShowInstructorAssignment("1".equals(filter.getParameterValue("instructorAssignment", "1")));
		} else {
			setShowInstructorAssignment(false);
		}
		if (filter.hasParameter("lms")) {
			setShowLms("1".equals(filter.getParameterValue("lms", "0")));
		} else {
			setShowLms(false);
		}
		if (filter.hasParameter("waitlist")) {
			setFilterWaitlist(filter.getParameterValue("waitlist"));
		} else {
			setFilterWaitlist(null);
		}
		if (filter.hasParameter("waitlistMode")) {
			setShowWaitlistMode("1".equals(filter.getParameterValue("waitlistMode", "0")));
		} else {
			setShowWaitlistMode(false);
		}
	}
	
	public boolean isShowCredit() {
		return showCredit;
	}
	public void setShowCredit(boolean showCredit) {
		this.showCredit = showCredit;
	}
	public boolean isShowDatePattern() {
		return showDatePattern;
	}
	public void setShowDatePattern(boolean showDatePattern) {
		this.showDatePattern = showDatePattern;
	}
	public boolean isShowDemand() {
		return showDemand;
	}
	public void setShowDemand(boolean showDemand) {
		this.showDemand = showDemand;
	}
	public boolean isShowDivSec() {
		return showDivSec;
	}
	public void setShowDivSec(boolean showDivSec) {
		this.showDivSec = showDivSec;
	}
	public boolean isShowLabel() {
		return showLabel;
	}
	public void setShowLabel(boolean showLabel) {
		this.showLabel = showLabel;
	}
	public boolean isShowLimit() {
		return showLimit;
	}
	public void setShowLimit(boolean showLimit) {
		this.showLimit = showLimit;
	}
	public boolean isShowSnapshotLimit() {
		return showSnapshotLimit;
	}
	public void setShowSnapshotLimit(boolean showSnapshotLimit) {
		this.showSnapshotLimit = showSnapshotLimit;
	}
	public boolean isShowManager() {
		return showManager;
	}
	public void setShowManager(boolean showManager) {
		this.showManager = showManager;
	}
	public boolean isShowMinPerWk() {
		return showMinPerWk;
	}
	public void setShowMinPerWk(boolean showMinPerWk) {
		this.showMinPerWk = showMinPerWk;
	}
	public boolean isShowNote() {
		return showNote;
	}
	public void setShowNote(boolean showNote) {
		this.showNote = showNote;
	}
	public boolean isShowPreferences() {
		return showPreferences;
	}
	public void setShowPreferences(boolean showPreferences) {
		this.showPreferences = showPreferences;
	}
	public boolean isShowProjectedDemand() {
		return showProjectedDemand;
	}
	public void setShowProjectedDemand(boolean showProjectedDemand) {
		this.showProjectedDemand = showProjectedDemand;
	}
	public boolean isShowRoomRatio() {
		return showRoomRatio;
	}
	public void setShowRoomRatio(boolean showRoomRatio) {
		this.showRoomRatio = showRoomRatio;
	}
	public boolean isShowSchedulePrintNote() {
		return showSchedulePrintNote;
	}
	public void setShowSchedulePrintNote(boolean showSchedulePrintNote) {
		this.showSchedulePrintNote = showSchedulePrintNote;
	}
	public boolean isShowTimePattern() {
		return showTimePattern;
	}
	public void setShowTimePattern(boolean showTimePattern) {
		this.showTimePattern = showTimePattern;
	}
	public boolean isShowTimetable() {
		return showTimetable;
	}
	public void setShowTimetable(boolean showTimetable) {
		this.showTimetable = showTimetable;
	}
	public boolean isShowInstructor() {
		return showInstructor;
	}
	public void setShowInstructor(boolean showInstructor) {
		this.showInstructor = showInstructor;
	}

    public Comparator getClassComparator() {
    	return iClassComparator;
    }
    
    public void setClassComparator(Comparator comparator) {
    	iClassComparator = comparator;
    }
    
	public boolean isShowSubpartCredit() {
		return showSubpartCredit;
	}
	public void setShowSubpartCredit(boolean showSubpartCredit) {
		this.showSubpartCredit = showSubpartCredit;
	}
}
