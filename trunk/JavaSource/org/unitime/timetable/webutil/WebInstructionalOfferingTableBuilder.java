/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008-2009, UniTime LLC, and individual contributors
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
package org.unitime.timetable.webutil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import net.sf.cpsolver.coursett.model.TimeLocation.IntEnumeration;

import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.commons.web.htmlgen.TableHeaderCell;
import org.unitime.commons.web.htmlgen.TableRow;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.form.InstructionalOfferingListFormInterface;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.util.Constants;


/**
 * @author Stephanie Schluttenhofer
 */
public class WebInstructionalOfferingTableBuilder {
	protected static SimpleDateFormat sDateFormat = new SimpleDateFormat("MM/dd", Locale.US);
	protected static DecimalFormat sRoomRatioFormat = new DecimalFormat("0.00");

    protected static String indent = "&nbsp;&nbsp;&nbsp;&nbsp;";
    protected static String oddRowBGColor = "#DFE7F2";
    protected static String oddRowBGColorChild = "#EFEFEF";
    protected static String oddRowMouseOverBGColor = "#8EACD0";
    protected static String evenRowMouseOverBGColor = "#8EACD0";
    protected String disabledColor = "gray";
    protected static String formName = "instructionalOfferingListForm";
    
    //available columns for table
    protected static String LABEL = "&nbsp;";
    public static final String DIV_SEC = "External Id";
    public static final String DEMAND = "Enrollment";
    public static final String PROJECTED_DEMAND = "Projected Demand";
    public static final String LIMIT = "Limit";
    public static final String ROOM_RATIO = "Room Ratio";
    public static final String MIN_PER_WK = "Mins Per Week";
    public static final String MANAGER = "Manager";
    public static final String DATE_PATTERN = "Date Pattern";
    public static final String TIME_PATTERN = "Time Pattern";
    public static final String INSTRUCTOR = "Instructor";
    public static final String PREFERENCES = "Preferences";
    public static final String TIMETABLE = "Timetable";
    public static final String CREDIT = "Offering Credit";
    public static final String SCHEDULING_SUBPART_CREDIT = "Subpart Credit";
    public static final String SCHEDULE_PRINT_NOTE_FILTER = "Schedule of Classes Notes";
    public static String SCHEDULE_PRINT_NOTE = "Schedule of Classes Notes";
    public static final String NOTE = "Note to Schedule Manager";
    public static final String TITLE = "Title";
    public static final String CONSENT = "Consent";
    public static final String DESIGNATOR_REQ = "Designator Required";
    public static final String EXAM = "Examination";
    public static final String EXAM_NAME = "Name";
    public static final String EXAM_PER = "Period";
    public static final String EXAM_ROOM = "Room";
    
    // Preference Labels
    protected static String TIME = "Time";
    protected static String ROOMGR = "Room&nbsp;Group";
    protected static String BLDG = "Bldg";
    protected static String ROOM = "Room";
    protected static String FEATURES = "Features";
    protected static String DISTRIBUTION = "Distribution";
    protected static String ALL_ROOM = "Room";

    // Timetable Labels
    protected static String ASSIGNED_TIME = "Time";
    protected static String ASSIGNED_ROOM = "Room";
    protected static String ASSIGNED_ROOM_CAPACITY = "Room Cap";

    
    protected static String[] COLUMNS = {LABEL,
            								TITLE,
    										DIV_SEC,
    										DEMAND,
    										PROJECTED_DEMAND,
    										CONSENT,
    										DESIGNATOR_REQ,
    										MIN_PER_WK,
    										LIMIT,
    										ROOM_RATIO,
    										MANAGER,
    										DATE_PATTERN,
    										TIME_PATTERN,
    										PREFERENCES,
    										INSTRUCTOR,
    										TIMETABLE,
    										CREDIT,
    										SCHEDULING_SUBPART_CREDIT,
    										SCHEDULE_PRINT_NOTE,
    										NOTE,
    										EXAM};
    
    //set to false for old behaviour
    protected static final boolean sAggregateRoomPrefs = true;
    
    protected static String[] PREFERENCE_COLUMN_ORDER = ( sAggregateRoomPrefs ?
    		 												new String[] {
    															TIME,
    															ALL_ROOM,
    															DISTRIBUTION
    														}
    													:
    														new String[] {
    															TIME,
    															ROOMGR,
    															BLDG,
    															ROOM,
    															FEATURES,
    															DISTRIBUTION
    														}
    		 											);
    
    protected static String[] TIMETABLE_COLUMN_ORDER = {ASSIGNED_TIME,
														ASSIGNED_ROOM,
														ASSIGNED_ROOM_CAPACITY};
    
    private boolean showLabel;
    private boolean showDivSec;
    private boolean showDemand;
    private boolean showProjectedDemand;
    private boolean showMinPerWk;
    private boolean showLimit;
    private boolean showRoomRatio;
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
    private boolean showDesignatorRequired;
    private boolean showExam;
    private boolean showExamName=true;
    private boolean showExamTimetable;
    
    private boolean iDisplayDistributionPrefs = true;
    private boolean iDisplayTimetable = true;
    
    private String iBackType = null;
    private String iBackId = null;
    
    private Comparator iClassComparator = new ClassComparator(ClassComparator.COMPARE_BY_ITYPE);
    
    // Set whether edit/modify config buttons are displayed
    private boolean displayConfigOpButtons = false;
    
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
    public void setDisplayTimetable(boolean displayTimetable) {
    	iDisplayTimetable = displayTimetable;
    }
    public boolean getDisplayTimetable() { return iDisplayTimetable; }
    
    private boolean iTimeVertical = false;
    public void setTimeVertival(boolean timeVertical) {
    	iTimeVertical = timeVertical;
    }
    public boolean getTimeVertival() {
    	return iTimeVertical;
    }
    private boolean iGridAsText = false;
    public void setGridAsText(boolean gridAsText) {
    	iGridAsText = gridAsText;
    }
    public boolean getGridAsText() {
    	return iGridAsText;
    }
    public String iInstructorNameFormat = "last-first";
    public void setInstructorNameFormat(String instructorNameFormat) {
    	iInstructorNameFormat = instructorNameFormat;
    }
    public String getInstructorNameFormat() {
    	return iInstructorNameFormat;
    }
    public String iDefaultTimeGridSize = null;
    public void setDefaultTimeGridSize(String defaultTimeGridSize) {
    	iDefaultTimeGridSize = defaultTimeGridSize;
    }
    public String getDefaultTimeGridSize() {
    	return iDefaultTimeGridSize;
    }
    
    public void setUserSettings(User user) {
		setTimeVertival(RequiredTimeTable.getTimeGridVertical(user));
		setGridAsText(RequiredTimeTable.getTimeGridAsText(user));
		setInstructorNameFormat(Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT));
		setDefaultTimeGridSize(RequiredTimeTable.getTimeGridSize(user));
    }
    
    public boolean isShowConsent() {
        return showConsent;
    }
    public void setShowConsent(boolean showConsent) {
        this.showConsent = showConsent;
    }
    
    public boolean isShowDesignatorRequired() {
        return showDesignatorRequired;
    }
    public void setShowDesignatorRequired(boolean showDesignatorRequired) {
        this.showDesignatorRequired = showDesignatorRequired;
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

    /**
     * 
     */
    public WebInstructionalOfferingTableBuilder() {
        super();
    }
    
    protected String getRowMouseOver(boolean isHeaderRow, boolean isControl){
        return ("this.style.backgroundColor='" 
                + (isHeaderRow ?oddRowMouseOverBGColor:evenRowMouseOverBGColor) 
                + "';this.style.cursor='"
                + (isControl ? "hand" : "default")
                + "';this.style.cursor='"
                + (isControl ? "pointer" : "default")+ "';");
   	
    }
    
    protected String getRowMouseOut(boolean isHeaderRow){
        return ("this.style.backgroundColor='"  + (isHeaderRow ?oddRowBGColor:"transparent") + "';");   	
    }

    protected String getRowMouseOut(boolean isHeaderRow, int ct){        
        return ( "this.style.backgroundColor='"  
                + (isHeaderRow 
                        ? oddRowBGColor 
                        : ( (ct%2==1) 
                                ? oddRowBGColorChild 
                                : "transparent") ) 
                + "';");   	
    }
    
    protected TableRow initRow(boolean isHeaderRow){
        TableRow row = new TableRow();
        if (isHeaderRow){
        	row.setBgColor(oddRowBGColor);
        }
        return (row);
    }
    
    protected TableHeaderCell headerCell(String content, int rowSpan, int colSpan){
    	TableHeaderCell cell = new TableHeaderCell();
    	cell.setRowSpan(rowSpan);
    	cell.setColSpan(colSpan);
    	cell.setAlign("left");
    	cell.setValign("bottom");
    	cell.addContent("<font size=\"-1\">");
    	cell.addContent(content);
    	cell.addContent("</font>");
    	return(cell);
     }
    
    private TableCell initCell(boolean isEditable, String onClick){
        return (initCell(isEditable, onClick, 1, false));
    }

    private TableCell initCell(boolean isEditable, String onClick, int cols){
        return (initCell(isEditable, onClick, cols, false));
    }

    private TableCell initCell(boolean isEditable, String onClick, int cols, boolean nowrap){
        TableCell cell = new TableCell();
        cell.setValign("top");
        if (cols > 1){
            cell.setColSpan(cols);
        }
        if (nowrap){
            cell.setNoWrap(true);
        }
        if (onClick != null && onClick.length() > 0){
        	cell.setOnClick(onClick);
        }
        if (!isEditable){
        	cell.addContent("<font color=" + disabledColor + ">");
        }
        return (cell);
    }

    private void endCell(TableCell cell, boolean isEditable){
        if (!isEditable){
            cell.addContent("</font>");
        }   
    }
   
    protected TableCell initNormalCell(String text, boolean isEditable){
        return (initColSpanCell(text, isEditable, 1));
    }
    
    private TableCell initColSpanCell(String text, boolean isEditable, int cols){
        TableCell cell = initCell(isEditable, null, cols);
        cell.addContent(text);
        endCell(cell, isEditable);
        return (cell);
        
    } 
    //NOTE: if changing column order column order must be changed in
    //		buildTableHeader, addInstrOffrRowsToTable, buildClassOrSubpartRow, and buildConfigRow
    protected void buildTableHeader(TableStream table, Long sessionId){  
    	TableRow row = new TableRow();
    	TableRow row2 = new TableRow();
    	TableHeaderCell cell = null;
    	if (isShowLabel()){
    		cell = this.headerCell(LABEL, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);
    	}
    	if (isShowDivSec()){
    		cell = this.headerCell(DIV_SEC, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);
    	}   	
    	if (isShowDemand()){
    		if (StudentClassEnrollment.sessionHasEnrollments(sessionId)){
        		cell = this.headerCell(DEMAND, 2, 1);
    		} else {
        		cell = this.headerCell(("Last " + DEMAND), 2, 1);    			
    		}
    		cell.addContent("<hr>");
    		row.addContent(cell);
    	}
    	if (isShowProjectedDemand()){
    		cell = this.headerCell(PROJECTED_DEMAND, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);
    	}
    	if (isShowLimit()){
    		cell = this.headerCell(LIMIT, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);
    	}
    	if (isShowRoomRatio()){
    		cell = this.headerCell(ROOM_RATIO, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);
    	}
    	if (isShowManager()){
    		cell = this.headerCell(MANAGER, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);
    	}
    	if (isShowDatePattern()){
    		cell = this.headerCell(DATE_PATTERN, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);
    	}
    	if (isShowMinPerWk()){
    		cell = this.headerCell(MIN_PER_WK, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);
    	}
    	if (isShowTimePattern()){
    		cell = this.headerCell(TIME_PATTERN, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);
    	}
    	if (isShowPreferences()){
	    	row.addContent(headerCell("----" + PREFERENCES + "----", 1, PREFERENCE_COLUMN_ORDER.length + (iDisplayDistributionPrefs?0:-1)));
	    	for(int j = 0; j < PREFERENCE_COLUMN_ORDER.length+(iDisplayDistributionPrefs?0:-1); j++){
	    		row2.addContent(headerCell(PREFERENCE_COLUMN_ORDER[j] + "<hr>", 1, 1));     
	    	}
    	}
    	if (isShowInstructor()){
    		cell = this.headerCell(INSTRUCTOR, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);    		
    	}
    	if (getDisplayTimetable() && isShowTimetable()){
	    	row.addContent(headerCell("--------" + TIMETABLE + "--------", 1, TIMETABLE_COLUMN_ORDER.length));
	    	for(int j = 0; j < TIMETABLE_COLUMN_ORDER.length; j++){
	    		cell = headerCell(TIMETABLE_COLUMN_ORDER[j], 1, 1);
	    		cell.addContent("<hr>");
	    		cell.setNoWrap(true);
	    		row2.addContent(cell);     
	    	}   		
    	}
    	if (isShowTitle()){
    		cell = this.headerCell(TITLE, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);    		
    	}
    	if (isShowCredit()){
    		cell = this.headerCell(CREDIT, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);    		
    	}
    	if (isShowSubpartCredit()){
    		cell = this.headerCell(SCHEDULING_SUBPART_CREDIT, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);    		
    	}
    	if (isShowConsent()){
    		cell = this.headerCell(CONSENT, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);    		
    	}
    	if (isShowDesignatorRequired()){
    		cell = this.headerCell(DESIGNATOR_REQ, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);    		
    	}
    	if (isShowSchedulePrintNote()){
    		cell = this.headerCell(this.getSchedulePrintNoteLabel(), 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);    		
    	}
    	if (isShowNote()){
    		cell = this.headerCell(NOTE, 2, 1);
    		cell.addContent("<hr>");
    		row.addContent(cell);    		
    	}
    	if (isShowExam()) {
    		cell = headerCell("-----------" + EXAM + "--------", 1, (isShowExamName()?1:0)+(isShowExamTimetable()?2:0));
    		cell.setNoWrap(true);
            row.addContent(cell);
            if (isShowExamName()) {
                cell = headerCell(EXAM_NAME, 1, 1);
                cell.addContent("<hr>");
                cell.setNoWrap(true);
                row2.addContent(cell);
            }
            if (isShowExamTimetable()) {
                cell = headerCell(EXAM_PER, 1, 1);
                cell.addContent("<hr>");
                cell.setNoWrap(true);
                row2.addContent(cell);
                cell = headerCell(EXAM_ROOM, 1, 1);
                cell.addContent("<hr>");
                cell.setNoWrap(true);
                row2.addContent(cell);
                
            }
    	}
    	table.addContent(row);
    	table.addContent(row2);
   }
    
    protected String getSchedulePrintNoteLabel(){
    	return(SCHEDULE_PRINT_NOTE);
    }

    private String subjectOnClickAction(Long instrOfferingId){
        return("document.location='instructionalOfferingDetail.do?op=view&io=" + instrOfferingId + "';");
    }
    
    private TableCell subjectAndCourseInfo(InstructionalOffering io, CourseOffering co) {
        TableCell cell = this.initCell(co.isIsControl().booleanValue(), null, 1, true);
    	if ("InstructionalOffering".equals(getBackType()) && io.getUniqueId().toString().equals(getBackId()))
    		cell.addContent("<A name=\"back\"></A>");
    	if ("PreferenceGroup".equals(getBackType())) {
    		for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();) {
    			InstrOfferingConfig ioc = (InstrOfferingConfig)i.next();
    			for (Iterator j=ioc.getSchedulingSubparts().iterator();j.hasNext();) {
    				SchedulingSubpart ss = (SchedulingSubpart)j.next();
    	         	if (ss.getUniqueId().toString().equals(getBackId())) cell.addContent("<A name=\"back\"></A>");
    	         	for (Iterator k=ss.getClasses().iterator();k.hasNext();) {
    	         		Class_ c = (Class_)k.next();
    	         		if (c.getUniqueId().toString().equals(getBackId())) cell.addContent("<A name=\"back\"></A>");
    	         	}
    			}
    		}
    	}
        cell.addContent("<A name=\"A" + io.getUniqueId().toString() + "\"></A>");
        cell.addContent("<A name=\"A" + co.getUniqueId().toString() + "\"></A>");
        cell.addContent(co != null? ("<span title='" + co.getCourseNameWithTitle() + "'><b>" + co.getSubjectAreaAbbv() + "</b>") :"");
        cell.addContent(" ");
        cell.addContent(co!= null? ("<b>" + co.getCourseNbr() + "</b></span>") :"");
        Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator();
        StringBuffer addlCos = new StringBuffer();
        CourseOffering tempCo = null;
        addlCos.append("<font color='"+disabledColor+"'>");
        while(it.hasNext()){
            tempCo = (org.unitime.timetable.model.CourseOffering) it.next();
            addlCos.append("<br>"); 
            addlCos.append(indent);
            //addlCos.append("<A href=\"courseOfferingEdit.do?co=" + tempCo.getUniqueId() + "\">");
            addlCos.append("<span title='" + tempCo.getCourseNameWithTitle() + "'>");
            addlCos.append(tempCo.getSubjectAreaAbbv());
            addlCos.append(" ");
            addlCos.append(tempCo.getCourseNbr());
            addlCos.append("</span>");
            //addlCos.append("</A>");
        }
        addlCos.append("</font>");
        if (tempCo != null){
            cell.addContent(addlCos.toString());
        }
        this.endCell(cell, co.isIsControl().booleanValue());
        return (cell);
    }  
    
    protected TableCell buildPrefGroupLabel(PreferenceGroup prefGroup, String indentSpaces, boolean isEditable, String prevLabel){
    	TableCell cell = initNormalCell(indentSpaces, isEditable);
    	if(!isEditable){
    		cell.addContent("<font color='"+disabledColor+"'>");
    	}
        cell.addContent("<A name=\"A" + prefGroup.getUniqueId().toString() + "\"></A>");
        String label = prefGroup.htmlLabel();
        if (prefGroup instanceof Class_) {
			Class_ aClass = (Class_) prefGroup;
			if (!aClass.isDisplayInScheduleBook().booleanValue()){
				cell.setTitle(aClass.getClassLabelWithTitle() + " - Do Not Display In Schedule Book.");
				label = "<i>" + label + "</i>";
			} else {
				cell.setTitle(aClass.getClassLabelWithTitle());
			}
		}
        if (prevLabel != null && label.equals(prevLabel)){
        	label = " &nbsp;";
        }
        cell.addContent(label);
        cell.setNoWrap(true);
        if(!isEditable){
        	cell.addContent("</font>");
        }
        return(cell);
    }
    
    private TableCell buildDatePatternCell(PreferenceGroup prefGroup, boolean isEditable){
    	DatePattern dp = prefGroup.effectiveDatePattern();
    	TableCell cell = null;
    	if (dp==null) {
    		cell = initNormalCell("", isEditable);
    	} else {
    		cell = initNormalCell("<div title='"+sDateFormat.format(dp.getStartDate())+" - "+sDateFormat.format(dp.getEndDate())+"'>"+dp.getName()+"</div>", isEditable);
    	}
        cell.setAlign("center");
        return(cell);
    }

    private TableCell buildTimePatternCell(PreferenceGroup prefGroup, boolean isEditable){
        TableCell cell = initNormalCell(prefGroup.effectiveTimePatternHtml(), isEditable);
        cell.setAlign("center");
        cell.setNoWrap(true);
        return(cell);
    }
    
    private TableCell buildTimePrefCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
		Assignment a = null;
		if (getDisplayTimetable() && isShowTimetable() && classAssignment!=null && prefGroup instanceof Class_) {
			try {
				a = classAssignment.getAssignment((Class_)prefGroup);
			} catch (Exception e) {
				Debug.error(e);
			}
    	}
    	
    	TableCell cell = initNormalCell(prefGroup.getEffectivePrefHtmlForPrefType(a,TimePref.class, getTimeVertival(), getGridAsText(), getDefaultTimeGridSize()),isEditable);
        cell.setNoWrap(true);
    	return (cell);
    	
    }
    private TableCell buildPreferenceCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, Class prefType, boolean isEditable){
    	if (!isEditable) return initNormalCell("",false);
    	if (TimePref.class.equals(prefType)) {
    		return(buildTimePrefCell(classAssignment,prefGroup, isEditable));
    	} else {
    		TableCell cell = this.initNormalCell(prefGroup.getEffectivePrefHtmlForPrefType(prefType),isEditable);
    		cell.setNoWrap(true);
    		return(cell);
    	}
    	
    }
    
    private TableCell buildPreferenceCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, Class[] prefTypes, boolean isEditable){
    	if (!isEditable) return initNormalCell("",false);
    	StringBuffer pref = new StringBuffer();
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
    		String x = prefGroup.getEffectivePrefHtmlForPrefType(prefType);
    		if (x!=null && x.trim().length()>0) {
    			if (pref.length()>0) pref.append("<BR>");
    			pref.append(x);
    		}
    	}
    	TableCell cell = this.initNormalCell(noRoomPrefs && pref.length()==0?"<i>N/A</i>":pref.toString(),isEditable);
    	cell.setNoWrap(true);
    	return(cell);
    }

    private TableCell buildPrefGroupDemand(PreferenceGroup prefGroup, boolean isEditable){
    	if (prefGroup instanceof Class_) {
			Class_ c = (Class_) prefGroup;
			if (StudentClassEnrollment.sessionHasEnrollments(c.getSessionId())){
				TableCell tc = null;
				if (c.getEnrollment() != null){
					tc = this.initNormalCell(c.getEnrollment().toString(), isEditable);
				} else {
					tc = this.initNormalCell("0", isEditable);				
				}
				tc.setAlign("right");
				return(tc);
			}
		}
    	return(this.initNormalCell("&nbsp;", isEditable));
    }
    
    private TableCell buildPrefGroupProjectedDemand(PreferenceGroup prefGroup, boolean isEditable){
    	return(this.initNormalCell("&nbsp;", isEditable));
    }
    
    private TableCell buildLimit(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	TableCell cell = null;
    	boolean nowrap = false;
    	if (prefGroup instanceof SchedulingSubpart){
	    	SchedulingSubpart ss = (SchedulingSubpart) prefGroup;
	    	boolean unlimited = ss.getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue();
	    	if (!unlimited) {
		    	int limit = (ss.getLimit()==null?0:ss.getLimit().intValue());
		    	int maxExpCap = ss.getMaxExpectedCapacity(); 
		    	if (limit==maxExpCap)
		    		cell = initNormalCell(String.valueOf(limit), isEditable);
		    	else {
		    		cell = initNormalCell(limit+"-"+maxExpCap, isEditable);
		    		nowrap = true;
		    	}
	    	}
	    	else {
	    	    cell = initNormalCell("&nbsp;", isEditable);	    	    
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
                    if (aClass.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings().size()>1) {
                        String title = "";
                        for (Iterator i=aClass.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings().iterator();i.hasNext();) {
                            CourseOffering offering = (CourseOffering)i.next();
                            int limitThisOffering = aClass.getClassLimit(classAssignment,offering);
                            if (limitThisOffering<=0) continue;
                            if (title.length()>0) title+=", ";
                            title += limitThisOffering+" ("+offering.getCourseName()+")";
                        }
                        limitString = "<span title='"+title+"'>"+limitString+"</span>";
                    }
                }
	    		cell = initNormalCell(limitString, isEditable);
	    		if (nowrap) cell.setNoWrap(true);
	    	}
	    	else {
	    	    cell = initNormalCell("&nbsp;", isEditable);	    	    
	    	}
	    		
    	} else {
    		cell = this.initNormalCell("&nbsp;" ,isEditable);
    	}
        cell.setAlign("right");	
        return(cell);
    }
    
    private TableCell buildDivisionSection(PreferenceGroup prefGroup, boolean isEditable){
    	TableCell cell = null;
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		String divSec = aClass.getDivSecNumber();
    		cell = initNormalCell((divSec==null?"&nbsp;":divSec), isEditable);
            cell.setAlign("right");	
    	} else {
    		cell = this.initNormalCell("&nbsp;" ,isEditable);
    	}
    	cell.setNoWrap(true);
        return(cell);
    }

    private TableCell buildInstructor(PreferenceGroup prefGroup, boolean isEditable){
    	TableCell cell = this.initNormalCell("" ,isEditable);
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		String label = aClass.instructorHtml(getInstructorNameFormat());
    		if (!aClass.isDisplayInstructor().booleanValue()){
    			label = "<i>" + label + "</i>";
    		}
    		cell.addContent(label);
            cell.setAlign("left");	
    	} else {
    		cell.addContent(" &nbsp; ");
    	}
        return(cell);
    }

    private TableCell buildCredit(PreferenceGroup prefGroup, boolean isEditable){
    	TableCell cell = this.initNormalCell("" ,isEditable);
    	if (prefGroup instanceof SchedulingSubpart) {
    		SchedulingSubpart ss = (SchedulingSubpart) prefGroup;
     		if (ss.getCredit() != null) {
    			cell.addContent("<span title='"+ss.getCredit().creditText()+"'>"+ss.getCredit().creditAbbv()+"</span>");
    		} else {
    			cell.addContent(" &nbsp; ");
    		}   		
    		
            cell.setAlign("left");	
    	} else {
    		cell.addContent(" &nbsp; ");
    	}
        return(cell);
    }

    private TableCell buildSchedulePrintNote(PreferenceGroup prefGroup, boolean isEditable, User user){
    	TableCell cell = null;
    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_) prefGroup;
    		if (c.getSchedulePrintNote()!=null && c.getSchedulePrintNote().trim().length() != 0) {
    			String note = (c.getSchedulePrintNote().length() <= 20?c.getSchedulePrintNote():c.getSchedulePrintNote().substring(0, 20) + "...");
    			if(!Constants.showPrintNoteAsShortenedText(user) && !Constants.showPrintNoteAsFullText(user) ){
    	    		cell = initNormalCell("<IMG border='0' alt='Has Schedule Print Note' title='" + note + "' align='absmiddle' src='images/Notes.png'>", isEditable);
    	    		cell.setAlign("center");
    			} else if (Constants.showPrintNoteAsShortenedText(user)) {
	    			cell = initNormalCell(note, isEditable);
	    			cell.setAlign("left");
    			} else if (Constants.showPrintNoteAsFullText(user)){
	    			cell = initNormalCell(c.getSchedulePrintNote(), isEditable);
	    			cell.setAlign("left");
    			} 
    		} else {
        		cell = this.initNormalCell("&nbsp;" ,isEditable);
        	}
    	}  else {
       		cell = this.initNormalCell("&nbsp;" ,isEditable);   		
    	}
        return(cell);
    }
    
    private TableCell buildExamName(TreeSet exams, boolean isEditable) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            sb.append("<span "+(Exam.sExamTypeFinal==exam.getExamType()?"style='font-weight:bold;' ":"")+
                    "title='"+exam.getLabel()+" "+Exam.sExamTypes[exam.getExamType()]+" Examination'>");
            sb.append(exam.getLabel());
            if (Exam.sExamTypeFinal==exam.getExamType()) sb.append("</span>");
            if (i.hasNext()) sb.append("<br>");
        }
        TableCell cell = this.initNormalCell(sb.toString() ,isEditable);
        cell.setAlign("left");
        cell.setNoWrap(true);
        return(cell);
    }

    private TableCell buildExamPeriod(ExamAssignmentProxy examAssignment, TreeSet exams, boolean isEditable) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            sb.append("<span "+(Exam.sExamTypeFinal==exam.getExamType()?"style='font-weight:bold;' ":"")+
                    "title='"+exam.getLabel()+" "+Exam.sExamTypes[exam.getExamType()]+" Examination'>");
            if (examAssignment!=null && examAssignment.getExamType()==exam.getExamType()) {
                ExamAssignment ea = examAssignment.getAssignment(exam.getUniqueId());
                if (ea==null && !isShowExamName()) continue;
                sb.append(ea==null?"":ea.getPeriodAbbreviationWithPref());
            } else {
                if (exam.getAssignedPeriod()==null && !isShowExamName()) continue;
                sb.append(exam.getAssignedPeriod()==null?"":exam.getAssignedPeriod().getAbbreviation());
            }
            if (Exam.sExamTypeFinal==exam.getExamType()) sb.append("</span>");
            if (i.hasNext()) sb.append("<br>");
        }
        TableCell cell = this.initNormalCell(sb.toString() ,isEditable);
        cell.setAlign("left");
        cell.setNoWrap(true);
        return(cell);
    }

    private TableCell buildExamRoom(ExamAssignmentProxy examAssignment, TreeSet exams, boolean isEditable) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            sb.append("<span "+(Exam.sExamTypeFinal==exam.getExamType()?"style='font-weight:bold;' ":"")+
                    "title='"+exam.getLabel()+" "+Exam.sExamTypes[exam.getExamType()]+" Examination'>");
            if (examAssignment!=null && examAssignment.getExamType()==exam.getExamType()) {
                ExamAssignment ea = examAssignment.getAssignment(exam.getUniqueId());
                if (ea==null && !isShowExamName()) continue;
                sb.append(ea==null?"":ea.getRoomsNameWithPref(", "));
            } else { 
                if (exam.getAssignedPeriod()==null && !isShowExamName()) continue;
                for (Iterator j=new TreeSet(exam.getAssignedRooms()).iterator();j.hasNext();) {
                    Location location = (Location)j.next();
                    sb.append(location.getLabel());
                    if (j.hasNext()) sb.append(", ");
                }
            }
            if (Exam.sExamTypeFinal==exam.getExamType()) sb.append("</span>");
            if (i.hasNext()) sb.append("<br>");
        }
        TableCell cell = this.initNormalCell(sb.toString() ,isEditable);
        cell.setAlign("left");
        cell.setNoWrap(true);
        return(cell);
    }

    protected TreeSet getExams(Class_ clazz) {
        return new TreeSet(Exam.findAll(ExamOwner.sOwnerTypeClass,clazz.getUniqueId()));
    }

    private TableCell buildSchedulePrintNote(InstructionalOffering io, boolean isEditable, User user){
    	TableCell cell = null;
	    StringBuffer note = new StringBuffer("");
		Set s = io.getCourseOfferings();
		boolean hasNote = false;
		for (Iterator i=s.iterator(); i.hasNext(); ) {
			String crsNote = null;
			CourseOffering coI = (CourseOffering) i.next();
			if (coI.getScheduleBookNote()!=null && coI.getScheduleBookNote().trim().length()>0) {
				hasNote = true;
				if (note.length()>0)
					note.append("<br>");
				if (Constants.showCrsOffrAsFullText(user)){
					note.append(coI.getScheduleBookNote());
				} else {
					if (coI.getScheduleBookNote().length() <= 20){
						note.append(coI.getScheduleBookNote());
					} else {
						note.append(coI.getScheduleBookNote().substring(0, 20) + "...");
					}		
				}
			}
		}
		if (hasNote){
			if (!Constants.showCrsOffrAsShortenedText(user) && !Constants.showCrsOffrAsFullText(user)){
	    		cell = initNormalCell("<IMG border='0' alt='Has Course Offering Note' title='" + note + "' align='absmiddle' src='images/Notes.png'>", isEditable);
	    		cell.setAlign("center");	
			} else {
				if (note.length() == 0){
					note.append("&nbsp;");
				}
				cell = initNormalCell(note.toString(), isEditable);
				cell.setAlign("left");
			}
	
		} else {
			cell = this.initNormalCell("&nbsp;" ,isEditable);
		}
        return(cell);
    }
    
    private TableCell buildNote(PreferenceGroup prefGroup, boolean isEditable, User user){
    	TableCell cell = null;
    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_) prefGroup;
    		if (c.getNotes()!=null) {
    			String note = (c.getNotes().length() <= 20?c.getNotes():c.getNotes().substring(0, 20) + "...");
    			if (Constants.showMgrNoteShortenedText(user)){
        			cell = initNormalCell(note.replaceAll("\n","<br>"), isEditable);
        			cell.setAlign("left");
    			} else if (Constants.showMgrNoteFullText(user)) {
    				cell = initNormalCell(c.getNotes().replaceAll("\n","<br>"), isEditable);
        			cell.setAlign("left");
    			} else {
    	    		cell = initNormalCell("<IMG border='0' alt='Has Note to Mgr' title='" + note + "' align='absmiddle' src='images/Notes.png'>", isEditable);
    	    		cell.setAlign("center");
    			}
    		} else { 
        		cell = this.initNormalCell("&nbsp;" ,isEditable);
        	}
    	} else { 
    		cell = this.initNormalCell("&nbsp;" ,isEditable);
    	}
        return(cell);
    }

    private TableCell buildManager(PreferenceGroup prefGroup, boolean isEditable){
    	TableCell cell = null;
    	Department managingDept = null;
    	if (prefGroup instanceof Class_) {
    		managingDept = ((Class_)prefGroup).getManagingDept();
    	} else if (prefGroup instanceof SchedulingSubpart) {
    		managingDept = ((SchedulingSubpart)prefGroup).getManagingDept();
    	} 
    	cell = initNormalCell(managingDept==null?"&nbsp;":managingDept.getManagingDeptAbbv(), isEditable);
        return(cell);
    }

    private TableCell buildMinPerWeek(PreferenceGroup prefGroup, boolean isEditable){
    	TableCell cell = null;
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		cell = initNormalCell(aClass.getSchedulingSubpart().getMinutesPerWk().toString(), isEditable);
            cell.setAlign("right");	
    	} else if (prefGroup instanceof SchedulingSubpart) {
    			SchedulingSubpart aSchedulingSubpart = (SchedulingSubpart) prefGroup;
        		cell = initNormalCell(aSchedulingSubpart.getMinutesPerWk().toString(), isEditable);
                cell.setAlign("right");	
    	} else {
    		cell = this.initNormalCell("&nbsp;" ,isEditable);
    	}
        return(cell);
    }

    private TableCell buildRoomLimit(PreferenceGroup prefGroup, boolean isEditable, boolean classLimitDisplayed){
    	TableCell cell = null;
    	if (prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
    		if (aClass.getNbrRooms()!=null && aClass.getNbrRooms().intValue()!=1) {
    			if (aClass.getNbrRooms().intValue()==0)
    				cell = initNormalCell("<i>N/A</i>", isEditable);
    			else {
    				String text = aClass.getNbrRooms().toString();
    				text += " at ";
    				if (aClass.getRoomRatio() != null)
    					text += sRoomRatioFormat.format(aClass.getRoomRatio().floatValue());
    				else
    					text += "0";
    				cell = initNormalCell(text, isEditable);
    				cell.setNoWrap(true);
    			}
    		} else {
    			if (aClass.getRoomRatio() != null){
    				if (classLimitDisplayed && aClass.getRoomRatio().equals(new Float(1.0))){
    					cell = initNormalCell("&nbsp;", isEditable);
    				} else {
    					cell = initNormalCell(sRoomRatioFormat.format(aClass.getRoomRatio().floatValue()), isEditable);
    				}
    			} else {
    				if (aClass.getExpectedCapacity() == null){
    					cell = initNormalCell("&nbsp;", isEditable);
    				} else {
    					cell = initNormalCell("0", isEditable);
    				}
    			}
    		}
            cell.setAlign("right");	
    	} else {
    		cell = this.initNormalCell("&nbsp;" ,isEditable);
    	}
        return(cell);
    }
    
    private TableCell buildAssignedTime(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	TableCell cell = null;
    	if (classAssignment!=null && prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		Assignment a = null;
    		AssignmentPreferenceInfo info = null;
    		try {
    			a = classAssignment.getAssignment(aClass);
    			info = classAssignment.getAssignmentInfo(aClass);
    		} catch (Exception e) {
    			Debug.error(e);
    		}
    		if (a!=null) {
    			StringBuffer sb = new StringBuffer();
    			if (info!=null) {
    				sb.append("<font color='"+(isEditable?PreferenceLevel.int2color(info.getTimePreference()):disabledColor)+"'>");
    			}
   				IntEnumeration e = a.getTimeLocation().getDays();
   				while (e.hasMoreElements()){
   					sb.append(Constants.DAY_NAMES_SHORT[(int)e.nextInt()]);
   				}
   				sb.append(" ");
   				sb.append(a.getTimeLocation().getStartTimeHeader());
   				sb.append("-");
   				sb.append(a.getTimeLocation().getEndTimeHeader());
   				if (info!=null)
   					sb.append("</font>");
    			cell = initNormalCell(sb.toString(), isEditable);
    		} else {
    			cell = initNormalCell("&nbsp;", isEditable);
    		}
            cell.setAlign("left");
            cell.setNoWrap(true);
    	} else {
    		cell = this.initNormalCell("&nbsp;" ,isEditable);
    	}
        return(cell);
    }
   
    private TableCell buildAssignedRoom(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable) {
    	TableCell cell = null;
    	if (classAssignment!=null && prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
    		Assignment a = null;
    		AssignmentPreferenceInfo info = null;
    		try {
    			a= classAssignment.getAssignment(aClass);
    			info = classAssignment.getAssignmentInfo(aClass);
    		} catch (Exception e) {
    			Debug.error(e);
    		}
    		if (a!=null) {
    			StringBuffer sb = new StringBuffer();
	    		Iterator it2 = a.getRooms().iterator();
	    		while (it2.hasNext()){
	    			Location room = (Location)it2.next();
	    			if (info!=null)
	    				sb.append("<font color='"+(isEditable?PreferenceLevel.int2color(info.getRoomPreference(room.getUniqueId())):disabledColor)+"'>");
	    			sb.append(room.getLabel());
	   				if (info!=null)
	   					sb.append("</font>");
	    			if (it2.hasNext()){
	        			sb.append("<BR>");
	        		} 
	    		}	
	    		cell = initNormalCell(sb.toString(), isEditable);
    		} else {
    			cell = initNormalCell("&nbsp;", isEditable);
    		}
           cell.setAlign("left");
           cell.setNoWrap(true);
    	} else {
    		cell = this.initNormalCell(" &nbsp;" ,isEditable);
    	}
        return(cell);
    }
    private TableCell buildAssignedRoomCapacity(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable) {
    	TableCell cell = null;
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
    					sb.append("<BR>");
    				} 
				}
				cell = initNormalCell(sb.toString(), isEditable);
    		} else {
    			cell = initNormalCell(" &nbsp;", isEditable);
    		}
           cell.setAlign("right");
           cell.setNoWrap(true);
    	} else {
    		cell = this.initNormalCell("&nbsp;" ,isEditable);
    	}
        return(cell);
    }
    
    //NOTE: if changing column order column order must be changed in
    //		buildTableHeader, addInstrOffrRowsToTable, buildClassOrSubpartRow, and buildConfigRow
    protected void buildClassOrSubpartRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TableRow row, PreferenceGroup prefGroup, String indentSpaces, boolean isEditable, String prevLabel, User user){
    	boolean classLimitDisplayed = false;
    	if (isShowLabel()){
	        row.addContent(this.buildPrefGroupLabel(prefGroup, indentSpaces, isEditable, prevLabel));
    	} 
    	if (isShowDivSec()){
    		row.addContent(this.buildDivisionSection(prefGroup, isEditable));
    	}
    	if (isShowDemand()){
    		row.addContent(this.buildPrefGroupDemand(prefGroup, isEditable));
    	} 
    	if (isShowProjectedDemand()){
    		row.addContent(this.buildPrefGroupProjectedDemand(prefGroup, isEditable));
    	} 
    	if (isShowLimit()){
    		classLimitDisplayed = true;
    		row.addContent(this.buildLimit(classAssignment, prefGroup, isEditable));
       	} 
    	if (isShowRoomRatio()){
    		row.addContent(this.buildRoomLimit(prefGroup, isEditable, classLimitDisplayed));
       	} 
    	if (isShowManager()){
    		row.addContent(this.buildManager(prefGroup, isEditable));
     	} 
    	if (isShowDatePattern()){
    		row.addContent(this.buildDatePatternCell(prefGroup, isEditable));
     	} 
    	if (isShowMinPerWk()){
    		row.addContent(this.buildMinPerWeek(prefGroup, isEditable));
       	} 
    	if (isShowTimePattern()){
    		row.addContent(this.buildTimePatternCell(prefGroup, isEditable));
    	} 
    	if (isShowPreferences()){
	        for (int j = 0; j < PREFERENCE_COLUMN_ORDER.length; j++) {
	        	if (PREFERENCE_COLUMN_ORDER[j].equals(TIME)) {
	        		row.addContent(this.buildPreferenceCell(classAssignment,prefGroup, TimePref.class, isEditable));
	        	} else if (sAggregateRoomPrefs && PREFERENCE_COLUMN_ORDER[j].equals(ALL_ROOM)) {
	        		row.addContent(this.buildPreferenceCell(classAssignment,prefGroup, new Class[] {RoomPref.class, BuildingPref.class, RoomFeaturePref.class, RoomGroupPref.class} , isEditable));
	        	} else if (PREFERENCE_COLUMN_ORDER[j].equals(ROOM)) {
	        		row.addContent(this.buildPreferenceCell(classAssignment,prefGroup, RoomPref.class, isEditable));
	        	} else if (PREFERENCE_COLUMN_ORDER[j].equals(BLDG)) {
	        		row.addContent(this.buildPreferenceCell(classAssignment,prefGroup, BuildingPref.class, isEditable));
	        	} else if (PREFERENCE_COLUMN_ORDER[j].equals(FEATURES)) {
	        		row.addContent(this.buildPreferenceCell(classAssignment,prefGroup, RoomFeaturePref.class, isEditable));
	        	} else if (iDisplayDistributionPrefs && PREFERENCE_COLUMN_ORDER[j].equals(DISTRIBUTION)) {
	        		row.addContent(this.buildPreferenceCell(classAssignment,prefGroup, DistributionPref.class, isEditable));
	        	} else if (PREFERENCE_COLUMN_ORDER[j].equals(ROOMGR)) {
	        		row.addContent(this.buildPreferenceCell(classAssignment,prefGroup, RoomGroupPref.class, isEditable));
	        	}
	        }
    	} 
    	if (isShowInstructor()){
    		row.addContent(this.buildInstructor(prefGroup, isEditable));
    	}
    	if (getDisplayTimetable() && isShowTimetable()){
	        for (int j = 0; j < TIMETABLE_COLUMN_ORDER.length; j++) {
	        	if (TIMETABLE_COLUMN_ORDER[j].equals(ASSIGNED_TIME)){
	        		row.addContent(this.buildAssignedTime(classAssignment, prefGroup, isEditable));
	        	} else if (TIMETABLE_COLUMN_ORDER[j].equals(ASSIGNED_ROOM)){
	        		row.addContent(this.buildAssignedRoom(classAssignment, prefGroup, isEditable));
	        	} else if (TIMETABLE_COLUMN_ORDER[j].equals(ASSIGNED_ROOM_CAPACITY)){
	        		row.addContent(this.buildAssignedRoomCapacity(classAssignment, prefGroup, isEditable));
	        	}
	        }
    	} 
    	if (isShowTitle()){
    		row.addContent(this.initNormalCell("&nbsp;", isEditable));
    	}
    	if (isShowCredit()){
    		row.addContent(this.initNormalCell("&nbsp;", isEditable));
    	} 
    	if (isShowSubpartCredit()){
            row.addContent(this.buildCredit(prefGroup, isEditable));     		
    	} 
    	if (isShowConsent()){
    		row.addContent(this.initNormalCell("&nbsp;", isEditable));
    	}
    	if (isShowDesignatorRequired()){
    		row.addContent(this.initNormalCell("&nbsp;", isEditable));
    	}
    	if (isShowSchedulePrintNote()){
            row.addContent(this.buildSchedulePrintNote(prefGroup, isEditable, user));     		
    	} 
    	if (isShowNote()){
            row.addContent(this.buildNote(prefGroup, isEditable, user));     		
    	}
    	if (isShowExam()) {
    	    if (prefGroup instanceof Class_) {
    	        TreeSet exams = getExams((Class_)prefGroup);
    	        if (isShowExamName()) {
    	            row.addContent(this.buildExamName(exams, isEditable));
    	        }
    	        if (isShowExamTimetable()) {
    	            row.addContent(this.buildExamPeriod(examAssignment, exams, isEditable));
    	            row.addContent(this.buildExamRoom(examAssignment, exams, isEditable));
    	        }
    	    } else {
    	        if (isShowExamName()) {
    	            row.addContent(this.initNormalCell("&nbsp;", isEditable));
    	        }
                if (isShowExamTimetable()) {
                    row.addContent(this.initNormalCell("&nbsp;", isEditable));
                    row.addContent(this.initNormalCell("&nbsp;", isEditable));
                }
    	    }
    	}
    }
    
    private void buildSchedulingSubpartRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TableStream table, SchedulingSubpart ss, String indentSpaces, User user){
    	boolean isHeaderRow = true;
    	TableRow row = this.initRow(isHeaderRow);
        boolean isEditable = ss.isViewableBy(user);
        boolean isOffered = !ss.getInstrOfferingConfig().getInstructionalOffering().isNotOffered().booleanValue();        

        if(isOffered)
            row.setOnMouseOver(this.getRowMouseOver(isHeaderRow, isEditable));
        
        if (isEditable && isOffered) {
        	row.setOnClick("document.location='schedulingSubpartDetail.do?ssuid="+ss.getUniqueId().toString()+ "'");
        }

        if(isOffered)
            row.setOnMouseOut(this.getRowMouseOut(isHeaderRow));
        
        this.buildClassOrSubpartRow(classAssignment, examAssignment, row, ss, indentSpaces, isEditable, null, user);
      table.addContent(row);
    	
    }
    
    private void buildSchedulingSubpartRows(Vector subpartIds, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TableStream table, SchedulingSubpart ss, String indentSpaces, User user){
    	if (subpartIds!=null) subpartIds.add(ss.getUniqueId());
        this.buildSchedulingSubpartRow(classAssignment, examAssignment, table,  ss, indentSpaces, user);
        Set childSubparts = ss.getChildSubparts();
        
		if (childSubparts != null && !childSubparts.isEmpty()){
		    
		    ArrayList childSubpartList = new ArrayList(childSubparts);
		    Collections.sort(childSubpartList, new SchedulingSubpartComparator());
            Iterator it = childSubpartList.iterator();
            SchedulingSubpart child = null;
            
            while (it.hasNext()){              
                child = (SchedulingSubpart) it.next();
                buildSchedulingSubpartRows(subpartIds, classAssignment, examAssignment, table, child, indentSpaces + indent, user);
            }
        }
    }
 
    protected void buildClassRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, int ct, TableStream table, Class_ aClass, String indentSpaces, User user, String prevLabel){
    	boolean isHeaderRow = false;
        boolean isEditable = aClass.isViewableBy(user);
    	TableRow row = this.initRow(isHeaderRow);
        row.setOnMouseOver(this.getRowMouseOver(isHeaderRow, isEditable));
        row.setOnMouseOut(this.getRowMouseOut(isHeaderRow));
       
        if (isEditable) {
            	row.setOnClick("document.location='classDetail.do?cid=" + aClass.getUniqueId().toString() + "&sec=" + aClass.getSectionNumberString() + "'");
        }
        
        this.buildClassOrSubpartRow(classAssignment, examAssignment, row, aClass, indentSpaces, isEditable, prevLabel, user);
        table.addContent(row);
    }
    
    private void buildClassRows(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, int ct, TableStream table, Class_ aClass, String indentSpaces, User user, String prevLabel){

        buildClassRow(classAssignment, examAssignment, ct, table, aClass, indentSpaces, user, prevLabel);
    	Set childClasses = aClass.getChildClasses();

    	if (childClasses != null && !childClasses.isEmpty()){
        
    	    ArrayList childClassesList = new ArrayList(childClasses);
            Collections.sort(childClassesList, iClassComparator);
            
            Iterator it = childClassesList.iterator();
            Class_ child = null;
            String previousLabel = aClass.htmlLabel();
            while (it.hasNext()){              
                child = (Class_) it.next();
                buildClassRows(classAssignment, examAssignment, ct, table, child, indentSpaces + indent, user, previousLabel);
            }
        }
    }


    //NOTE: if changing column order column order must be changed in
    //		buildTableHeader, addInstrOffrRowsToTable, buildClassOrSubpartRow, and buildConfigRow
	protected void buildConfigRow(Vector subpartIds, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TableStream table, InstrOfferingConfig ioc, User user, boolean printConfigLine, boolean printConfigReservation) {
	    boolean isHeaderRow = true;
	    boolean isEditable = ioc.isViewableBy(user);
	    String configName = ioc.getName();
	    boolean unlimited = ioc.isUnlimitedEnrollment().booleanValue();
	    boolean hasConfig = false;
		if (printConfigLine) {
		    TableRow row = this.initRow(isHeaderRow);
	        TableCell cell = null;
        	if (isShowLabel()){
        	    if (configName==null || configName.trim().length()==0)
        	        configName = ioc.getUniqueId().toString();
        	    /*
        	    cell = this.initNormalCell(
        	            indent + "<u>Configuration</u>: <font class='configTitle'>" + configName + "</font> ", 
        	            isEditable);
        	    */
        	    cell = this.initNormalCell(indent + "Configuration " + configName, isEditable);
        	    cell.setNoWrap(true);
        	    row.addContent(cell);

        	}
        	if (isShowDivSec()){
        		row.addContent(initNormalCell("", isEditable));
    		}
        	if (isShowDemand()){
    			row.addContent(initNormalCell("", isEditable));
    		}
        	if (isShowProjectedDemand()){
    			row.addContent(initNormalCell("", isEditable));
    		} 
        	if (isShowLimit()){
    		    cell = this.initNormalCell(
    	            	(unlimited ? "<img src='images/infinity.gif' alt='Unlimited Enrollment' title='Unlimited Enrollment' border='0' align='top'>" : ioc.getLimit().toString()),  
    		            isEditable);
    		    cell.setAlign("right");
	            row.addContent(cell);
        	} 
        	if (isShowRoomRatio()){
                row.addContent(initNormalCell("", isEditable));
        	} 
        	if (isShowManager()){
                row.addContent(initNormalCell("", isEditable));
        	} 
        	if (isShowDatePattern()){
                row.addContent(initNormalCell("", isEditable));
	       	} 
        	if (isShowMinPerWk()){
                row.addContent(initNormalCell("", isEditable));
        	} 
        	if (isShowTimePattern()){
	       		row.addContent(initNormalCell("", isEditable));
        	} 
        	if (isShowPreferences()){
		        for (int j = 0; j < PREFERENCE_COLUMN_ORDER.length + (iDisplayDistributionPrefs?0:-1); j++) {
		            row.addContent(initNormalCell("", isEditable));
		        }
        	} 
        	if (isShowInstructor()){
                row.addContent(initNormalCell("", isEditable));
        	} 
        	if (getDisplayTimetable() && isShowTimetable()){
        		for (int j = 0; j < TIMETABLE_COLUMN_ORDER.length; j++){
        			row.addContent(initNormalCell("", isEditable));
        		}
        	} 
        	if (isShowTitle()){
        		row.addContent(this.initNormalCell("", isEditable));
        	}
        	if (isShowCredit()){
                row.addContent(initNormalCell("", isEditable));
        	} 
        	if (isShowSubpartCredit()){
                row.addContent(initNormalCell("", isEditable));
        	} 
        	if (isShowConsent()){
        		row.addContent(this.initNormalCell("", isEditable));
        	}
        	if (isShowDesignatorRequired()){
        		row.addContent(this.initNormalCell("", isEditable));
        	}
        	if (isShowSchedulePrintNote()){
                row.addContent(initNormalCell("", isEditable));
        	} 
        	if (isShowNote()){
                row.addContent(initNormalCell("", isEditable));
        	}
	        
        	/* -- configuration line is not clickable
		    row.setOnMouseOver(this.getRowMouseOver(isHeaderRow, isEditable));
	        row.setOnMouseOut(this.getRowMouseOut(isHeaderRow));
	        */
        	
            if (isShowExam()) {
                TreeSet exams = new TreeSet(Exam.findAll(ExamOwner.sOwnerTypeConfig,ioc.getUniqueId()));
                if (isShowExamName()) {
                    row.addContent(this.buildExamName(exams, isEditable));
                }
                if (isShowExamTimetable()) {
                    row.addContent(this.buildExamPeriod(examAssignment, exams, isEditable));
                    row.addContent(this.buildExamRoom(examAssignment, exams, isEditable));
                }
            }

	    
	        table.addContent(row);
	        hasConfig = true;
		}
        ArrayList subpartList = new ArrayList(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());
        Iterator it = subpartList.iterator();
        SchedulingSubpart ss = null;
        while(it.hasNext()){
            ss = (SchedulingSubpart) it.next();
            if (ss.getParentSubpart() == null){
                buildSchedulingSubpartRows(subpartIds, classAssignment, examAssignment, table, ss, (hasConfig?indent+indent:indent) , user);
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
						buildClassRows(classAssignment, examAssignment, ++ct, table, c, indent, user, prevLabel);
						prevLabel = c.htmlLabel();
					}
				}
			}
		}
        
        // Config Reservations
		if (printConfigReservation) {
	        ReservationsTableBuilder r = new ReservationsTableBuilder();
	        String resvTable = r.htmlTableForReservations(ioc.effectiveReservations(true, true, true, true, true), true, ioc.isViewableBy(user), ioc.getControllingCourseOffering().isLimitedEditableBy(user));
	        if (resvTable!=null && resvTable.trim().length()>0) {
			    TableRow row = this.initRow(false);
		        TableCell cell = initColSpanCell(r.createTable(resvTable, "margin:0;", null, null), false, 12);
		        row.addContent(cell);
		        table.addContent(row);
	        }
		}
   }

    private void buildConfigRows(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TableStream table, Set instrOfferingConfigs, User user, boolean printConfigLine, boolean printConfigReservation) {
        Iterator it = instrOfferingConfigs.iterator();
        InstrOfferingConfig ioc = null;
        while (it.hasNext()){
            ioc = (InstrOfferingConfig) it.next();
            buildConfigRow(null, classAssignment, examAssignment, table, ioc, user, printConfigLine && instrOfferingConfigs.size()>1, printConfigReservation);
        }
    }

    //NOTE: if changing column order column order must be changed in
    //		buildTableHeader, addInstrOffrRowsToTable, buildClassOrSubpartRow, and buildConfigRow
    private void addInstrOffrRowsToTable(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TableStream table, InstructionalOffering io, Long subjectAreaId, User user){
        CourseOffering co = io.findSortCourseOfferingForSubjectArea(subjectAreaId);
        boolean isEditable = io.isViewableBy(user);
        if (!isEditable){
        	if (io.getInstrOfferingConfigs() != null && io.getInstrOfferingConfigs().size() > 0){
        		boolean canEdit = true;
        		Iterator it = io.getInstrOfferingConfigs().iterator();
        		InstrOfferingConfig ioc = null;
        		while(canEdit && it.hasNext()){
        			ioc = (InstrOfferingConfig) it.next();
        			if(!ioc.isViewableBy(user)){
        				canEdit = false;
        			}
        		}
        		isEditable = canEdit;
        	}
        }
        TableRow row = (this.initRow(true));
        row.setOnMouseOver(this.getRowMouseOver(true, isEditable));
        row.setOnMouseOut(this.getRowMouseOut(true));
        row.setOnClick(subjectOnClickAction(io.getUniqueId()));
        boolean isManagedAs = !co.isIsControl().booleanValue(); 
        
        TableCell cell = null;
    	if (isShowLabel()){
    		row.addContent(subjectAndCourseInfo(io, co));
    	}
    	if (isShowDivSec()){
    		row.addContent(initNormalCell("", isEditable));
		}
    	if (isShowDemand()){
    		String demand = "0";
    		if (StudentClassEnrollment.sessionHasEnrollments(io.getSessionId())){
    			demand = (io.getEnrollment() != null?io.getEnrollment().toString(): "0");		
    		} else {
	    		demand = (io.getDemand() != null?io.getDemand().toString(): "0");
	    		if (co.isIsControl().booleanValue() && !io.isNotOffered().booleanValue() && (io.getDemand()==null || io.getDemand().intValue()==0)) {
	    			demand = "<span style='font-weight:bold;color:red;'>0</span>";
	    		}
    		}
	        cell = initNormalCell(demand, co.isIsControl().booleanValue());
	        cell.setAlign("right");
	        row.addContent(cell);
		}
    	if (isShowProjectedDemand()){
	        cell = initNormalCell((io.getProjectedDemand() != null?io.getProjectedDemand().toString():"0"), co.isIsControl().booleanValue());
	        cell.setAlign("right");
	        row.addContent(cell);
		} 
    	if (isShowLimit()){
			boolean unlimited = false;
			for (Iterator x=io.getInstrOfferingConfigs().iterator();!unlimited && x.hasNext();)
				if ((((InstrOfferingConfig)x.next())).isUnlimitedEnrollment().booleanValue())
					unlimited = true;
			if (unlimited)
				cell = initNormalCell("<img src='images/infinity.gif' alt='Unlimited Enrollment' title='Unlimited Enrollment' border='0' align='top'>", co.isIsControl().booleanValue());
			else
				cell = initNormalCell(io.getLimit() != null?io.getLimit().toString():"", co.isIsControl().booleanValue());
            cell.setAlign("right");
            row.addContent(cell);
    	} 
    	int emptyCells = 0;
    	cell = null;
    	if (isShowRoomRatio()){
    		emptyCells ++;
    	} 
    	if (isShowManager()){
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
    	if (isShowPreferences()) {
    		emptyCells += PREFERENCE_COLUMN_ORDER.length + (iDisplayDistributionPrefs?0:-1);
    	}
    	if (isShowInstructor()){
    		emptyCells ++;
    	}
    	if (getDisplayTimetable() && isShowTimetable()) {
    		emptyCells += TIMETABLE_COLUMN_ORDER.length;
    	}
    	if (emptyCells>0) {
            if (isManagedAs) {
            	if (!isShowTitle() && io.getControllingCourseOffering().getTitle()!=null) {
            		String title = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            		if (co.getTitle()!=null && !co.getTitle().isEmpty()) {
            			title += "<b><font color=\"" + disabledColor + "\">" + co.getTitle() + "</font></b>";
            			title += " (<span title='" + io.getControllingCourseOffering().getCourseNameWithTitle() + "'>Managed As " + io.getControllingCourseOffering().getCourseName() + "</span>)";
            		} else {
            			title = "<span title='" + io.getControllingCourseOffering().getCourseNameWithTitle() + "'>Managed As " + io.getControllingCourseOffering().getCourseName() + "</span>";
            		}
                    for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
                    	CourseOffering x = (CourseOffering)it.next();
                    	title += "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                    	if (x.getTitle()!=null) title += "<font color=\"" + disabledColor + "\">" +x.getTitle() + "</font>";
                    }
                	cell = initNormalCell(title, isEditable);
            	} else {
            		cell = initNormalCell("<span title='" + io.getControllingCourseOffering().getCourseNameWithTitle() + "'>Managed As " + io.getControllingCourseOffering().getCourseName() + "</span>", isEditable);
            	}
            } else {
            	if (!isShowTitle() && io.getControllingCourseOffering().getTitle()!=null) {
            		String title = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            		if (co.isIsControl().booleanValue()) title += "<b>";
            		title += (co.getTitle()==null?"":co.getTitle());
            		if (co.isIsControl().booleanValue()) title += "</b>";
            		for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
                    	CourseOffering x = (CourseOffering)it.next();
                    	title += "<br>";
                    	if (x.getTitle()!=null) {
                    		title += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + x.getTitle();
                    	}
                    }
            		cell = initNormalCell(title, isEditable);
            	} else {
            		cell = initNormalCell("", isEditable);
            	}
            }
            cell.setColSpan(emptyCells);
            cell.setAlign("left");
            row.addContent(cell);
    	}
    	if (isShowTitle()){
    		row.addContent(initNormalCell(io.getControllingCourseOffering().getTitle()!=null ? io.getControllingCourseOffering().getTitle() : "&nbsp;", isEditable));
    	}
    	if (isShowCredit()){
            row.addContent(initNormalCell((io.getCredit()!=null?"<span title='"+io.getCredit().creditText()+"'>"+io.getCredit().creditAbbv()+"</span>":""), isEditable));     		
    	}
    	if (isShowSubpartCredit()){
            row.addContent(initNormalCell("", isEditable));
    	} 
    	if (isShowConsent()){
            row.addContent(initNormalCell(io.getConsentType()!=null ? "<span title='"+io.getConsentType().getLabel()+"'>"+io.getConsentType().getAbbv()+"</span>" : "&nbsp;", isEditable));     		
    	}
    	if (isShowDesignatorRequired()){
    		cell = initNormalCell(io.isDesignatorRequired()!=null && io.isDesignatorRequired().booleanValue()?"<IMG border='0' alt='Yes' title='Designator is required.' align='absmiddle' src='images/tick.gif'>":"", isEditable);
    		cell.setAlign("center");
            row.addContent(cell);
    	}
    	if (isShowSchedulePrintNote()){
            row.addContent(buildSchedulePrintNote(io, isEditable, user));     		
    	}
    	if (isShowNote()){
            row.addContent(initNormalCell("", isEditable));     		
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
            if (isShowExamName()) {
                row.addContent(this.buildExamName(exams, isEditable));
            }
            if (isShowExamTimetable()) {
                row.addContent(this.buildExamPeriod(examAssignment, exams, isEditable));
                row.addContent(this.buildExamRoom(examAssignment, exams, isEditable));
            }
        }
        table.addContent(row);
        if (io.getInstrOfferingConfigs() != null & !io.getInstrOfferingConfigs().isEmpty()){
        	TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
        	configs.addAll(io.getInstrOfferingConfigs());
            buildConfigRows(classAssignment, examAssignment, table, configs, user, true, false);
        }
    }
    
    protected TableStream initTable(JspWriter outputStream, Long sessionId){
    	TableStream table = new TableStream(outputStream);
        table.setWidth("90%");
        table.setBorder(0);
        table.setCellSpacing(0);
        table.setCellPadding(3);
        table.tableDefComplete();
        this.buildTableHeader(table, sessionId);
        return(table);
    }
    
    public void htmlTableForInstructionalOffering(
    		HttpSession session,
    		ClassAssignmentProxy classAssignment, 
    		ExamAssignmentProxy examAssignment,
            Long instructionalOfferingId, 
            User user,
            JspWriter outputStream,
            Comparator classComparator){
    	
    	if (instructionalOfferingId != null && user != null){
	        InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(instructionalOfferingId);
	        Long subjectAreaId = io.getControllingCourseOffering().getSubjectArea().getUniqueId();
	        
	        // Get Configuration
	        TreeSet ts = new TreeSet();
	        ts.add(io);
	        WebInstructionalOfferingTableBuilder iotbl = new WebInstructionalOfferingTableBuilder();
	        iotbl.setDisplayDistributionPrefs(false);
	        setVisibleColumns(COLUMNS);
		    htmlTableForInstructionalOfferings(
		    			session,
				        classAssignment,
				        examAssignment,
				        ts, subjectAreaId, user, false, false, outputStream, classComparator);
    	}
    }
    
    public void htmlTableForInstructionalOfferings(
    		HttpSession session,
            ClassAssignmentProxy classAssignment, 
            ExamAssignmentProxy examAssignment,
            InstructionalOfferingListForm form, 
            Long subjectAreaId, 
            User user,
            boolean displayHeader,
            boolean allCoursesAreGiven,
            JspWriter outputStream,
            String backType,
            String backId){
    	
    	setBackType(backType); setBackId(backId);
    	
    	this.setVisibleColumns(form);
    	htmlTableForInstructionalOfferings(session, classAssignment, examAssignment,
    			(TreeSet) form.getInstructionalOfferings(), 
     			subjectAreaId,
    			user,
    			displayHeader, allCoursesAreGiven,
    			outputStream,
    			new ClassComparator(form.getSortBy(), classAssignment, false)
    	);
   	
    }
    
    protected void htmlTableForInstructionalOfferings(
    		HttpSession session,
            ClassAssignmentProxy classAssignment, 
            ExamAssignmentProxy examAssignment,
            TreeSet insructionalOfferings, 
            Long subjectAreaId, 
            User user,
            boolean displayHeader, boolean allCoursesAreGiven,
            JspWriter outputStream,
            Comparator classComparator){
    	
    	if (classComparator!=null)
    		setClassComparator(classComparator);
        
    	if (isShowTimetable()) {
            boolean hasTimetable = false;
    		try {
    	        TimetableManager manager = TimetableManager.getManager(user);
    			if (manager!=null && manager.canSeeTimetable(Session.getCurrentAcadSession(user), user) && classAssignment!=null) {
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
            } catch (Exception e) {}
            setDisplayTimetable(hasTimetable);
    	}
    	
    	if (isShowExam())
    	    setShowExamTimetable(examAssignment!=null || Exam.hasTimetable((Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME)));
    	
        ArrayList notOfferedOfferings = new ArrayList();
        ArrayList offeredOfferings = new ArrayList();
        ArrayList offeringIds = new ArrayList();
        
        Iterator it = insructionalOfferings.iterator();
        InstructionalOffering io = null;
        boolean hasOfferedCourses = false;
        boolean hasNotOfferedCourses = false;
		setUserSettings(user);
        
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
    		if(displayHeader) {
    		    try {
    		    	if (allCoursesAreGiven)
    		    		outputStream.print("<DIV align=\"right\"><A class=\"l7\" href=\"#notOffered\">Courses Not Offered</A></DIV>");
    			    outputStream.print("<DIV class=\"WelcomeRowHead\"><A name=\"offered\"></A>Offered Courses</DIV>");
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
                  
            if (hasOfferedCourses){
                it = offeredOfferings.iterator();
                TableStream offeredTable = this.initTable(outputStream, (Session.getCurrentAcadSession(user) == null?null:Session.getCurrentAcadSession(user).getUniqueId()));
                
                while (it.hasNext()){
                    io = (InstructionalOffering) it.next();
                    offeringIds.add(io.getUniqueId());
                    	this.addInstrOffrRowsToTable(classAssignment, examAssignment, offeredTable, io, subjectAreaId, user);            	
                }
                offeredTable.tableComplete();
            } else {
                if(displayHeader)
    				try {
    					outputStream.print("<font class=\"error\">There are no courses currently offered for this subject.</font>");
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
            }
        }
        
        if (hasNotOfferedCourses || allCoursesAreGiven) {
            if(displayHeader) {
    	        try {
    				outputStream.print("<br>");
    				if (allCoursesAreGiven)
    					outputStream.print("<DIV align=\"right\"><A class=\"l7\" href=\"#offered\">Offered Courses</A></DIV>");
    		        outputStream.print("<DIV class=\"WelcomeRowHead\"><A name=\"notOffered\"></A>Not Offered Courses</DIV>");
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
            }
            
            if (hasNotOfferedCourses){
                it = notOfferedOfferings.iterator();
                TableStream notOfferedTable = this.initTable(outputStream, (Session.getCurrentAcadSession(user) == null?null:Session.getCurrentAcadSession(user).getUniqueId()));
                while (it.hasNext()){
                    io = (InstructionalOffering) it.next();
                    offeringIds.add(io.getUniqueId());
                    	this.addInstrOffrRowsToTable(classAssignment, examAssignment, notOfferedTable, io, subjectAreaId, user);            	
                }
                notOfferedTable.tableComplete();
            } else {
                if(displayHeader)
    				try {
    					outputStream.print("<font class=\"normal\">&nbsp;<br>All courses are currently being offered for this subject.</font>");
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
            }
        }
        
        Navigation.set(session, Navigation.sInstructionalOfferingLevel, offeringIds);
    }


	protected void setVisibleColumns(InstructionalOfferingListFormInterface form){
		setShowLabel(true);
		setShowDivSec(form.getDivSec().booleanValue());
		setShowDemand(form.getDemand().booleanValue());
		setShowProjectedDemand(form.getProjectedDemand().booleanValue());
		setShowMinPerWk(form.getMinPerWk().booleanValue());
		setShowLimit(form.getLimit().booleanValue());
		setShowRoomRatio(form.getRoomLimit().booleanValue());
		setShowManager(form.getManager().booleanValue());
		setShowDatePattern(form.getDatePattern().booleanValue());
		setShowTimePattern(form.getTimePattern().booleanValue());
		setShowPreferences(form.getPreferences().booleanValue());
		setShowInstructor(form.getInstructor().booleanValue());
		if (form.getTimetable() != null)
			setShowTimetable(form.getTimetable().booleanValue());
		else
			setShowTimetable(false);
		setShowCredit(form.getCredit().booleanValue());
		setShowSubpartCredit(form.getSubpartCredit().booleanValue());
		setShowSchedulePrintNote(form.getSchedulePrintNote().booleanValue());
		setShowNote(form.getNote().booleanValue());
		setShowConsent(form.getConsent().booleanValue());
		setShowDesignatorRequired(form.getDesignatorRequired().booleanValue());
		setShowTitle(form.getTitle().booleanValue());
		if (form.getCanSeeExams()) {
		    setShowExam(form.getExams());
		} else {
		    setShowExam(false);
		}
	}
	
	protected void setVisibleColumns(String[] columns){
		ArrayList a = new ArrayList();
		for (int i = 0 ; i < columns.length; i++){
			a.add(columns[i]);
		}
		
		setShowLabel(a.contains(LABEL));
		setShowDivSec(a.contains(DIV_SEC));
		setShowDemand(a.contains(DEMAND));
		setShowProjectedDemand(a.contains(PROJECTED_DEMAND));
		setShowMinPerWk(a.contains(MIN_PER_WK));
		setShowLimit(a.contains(LIMIT));
		setShowRoomRatio(a.contains(ROOM_RATIO));
		setShowManager(a.contains(MANAGER));
		setShowDatePattern(a.contains(DATE_PATTERN));
		setShowTimePattern(a.contains(TIME_PATTERN));
		setShowPreferences(a.contains(PREFERENCES));
		setShowInstructor(a.contains(INSTRUCTOR));
		setShowTimetable(a.contains(TIMETABLE));
		setShowCredit(a.contains(CREDIT));
		setShowSubpartCredit(a.contains(SCHEDULING_SUBPART_CREDIT));
		setShowSchedulePrintNote(a.contains(SCHEDULE_PRINT_NOTE));
		setShowNote(a.contains(NOTE));
		setShowConsent(a.contains(CONSENT));
		setShowDesignatorRequired(a.contains(DESIGNATOR_REQ));
		setShowTitle(a.contains(TITLE));
		setShowExam(a.contains(EXAM));
		
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
    
    public String getBackType() {
    	return iBackType;
    }
    public void setBackType(String backType) {
    	iBackType = backType;
    }
    public String getBackId() {
    	return iBackId;
    }
    public void setBackId(String backId) {
    	iBackId = backId;
    }
	public boolean isShowSubpartCredit() {
		return showSubpartCredit;
	}
	public void setShowSubpartCredit(boolean showSubpartCredit) {
		this.showSubpartCredit = showSubpartCredit;
	}
	

}
