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
package org.unitime.timetable.webutil;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import javax.servlet.jsp.JspWriter;

import org.unitime.commons.Debug;
import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.timetable.form.ClassAssignmentsReportForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 *
 */
public class WebClassAssignmentReportListTableBuilder extends WebClassListTableBuilder {

	/**
	 * 
	 */
	public WebClassAssignmentReportListTableBuilder() {
		super();
		disabledColor="black";
	}
	
	protected String additionalNote(){
		return(" Room Assignments");
	}
	
	@Override
	protected TableCell buildDatePatternCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
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
    	TableCell cell = null;
    	if (dp==null) {
    		cell = initNormalCell("", isEditable);
    	} else {
    		cell = initNormalCell("<div title='"+sDateFormat.format(dp.getStartDate())+" - "+sDateFormat.format(dp.getEndDate())+"' " +
    				(p == null ? "" : "style='color:" + PreferenceLevel.int2color(p.getDatePatternPref()) + ";'") +
    				">"+dp.getName()+"</div>", isEditable);
    	}
        cell.setAlign("center");
        return(cell);
	}

    public void htmlTableForClasses(SessionContext context, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, ClassAssignmentsReportForm form, JspWriter outputStream, String backType, String backId){
        
        this.setVisibleColumns(form);
        setBackType(backType);
        setBackId(backId);
 		
        Collection classes = (Collection) form.getClasses();
        Navigation.set(context, Navigation.sClassLevel, classes);
        
        if (getDisplayTimetable()) {
        	boolean hasTimetable = false;
        	if (context.hasPermission(Right.ClassAssignments) && classAssignment != null) {
        		try {
                	if (classAssignment instanceof CachedClassAssignmentProxy) {
                		((CachedClassAssignmentProxy)classAssignment).setCache(classes);
                	}
        			for (Iterator i=classes.iterator();i.hasNext();) {
        				Object[] o = (Object[])i.next(); Class_ clazz = (Class_)o[0];
        				if (classAssignment.getAssignment(clazz)!=null) {
        					hasTimetable = true; break;
        				}
        			}
        		} catch (Exception e) {}
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
        
        TableStream table = null;
        int ct = 0;
        Iterator it = classes.iterator();
        SubjectArea subjectArea = null;
        String prevLabel = null;
        while (it.hasNext()){
        	Object[] o = (Object[])it.next(); Class_ c = (Class_)o[0]; CourseOffering co = (CourseOffering)o[1];
            if (subjectArea == null || !subjectArea.getUniqueId().equals(co.getSubjectArea().getUniqueId())){
            	if(table != null) {
            		table.tableComplete();
	            	try {
						outputStream.print("<br>");
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            	subjectArea = co.getSubjectArea();
            	try {
					outputStream.print(labelForTable(subjectArea));
				} catch (IOException e) {
					e.printStackTrace();
				}
            	ct = 0;
		        table = this.initTable(outputStream, context.getUser().getCurrentAcademicSessionId());
		    }
		        
            
            this.buildClassRow(classAssignment,examAssignment, ++ct, table, co, c, "", context, prevLabel);
            prevLabel = c.getClassLabel(co);
        }  
        if(table != null)
        	table.tableComplete();      
    }
    
    @Override
    protected TableCell buildInstructor(PreferenceGroup prefGroup, boolean isEditable){
    	TableCell cell = this.initNormalCell("" ,isEditable);
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		if (aClass.isDisplayInstructor() && !aClass.getClassInstructors().isEmpty()) {
            	TreeSet sortedInstructors = new TreeSet(new InstructorComparator());
            	sortedInstructors.addAll(aClass.getClassInstructors());
        		for (Iterator i=sortedInstructors.iterator(); i.hasNext();) {
        			ClassInstructor ci = (ClassInstructor)i.next();
            		String label = ci.getInstructor().getName(getInstructorNameFormat());
            		cell.addContent(label + (i.hasNext() ? "<br>" : ""));
        		}
    		} else {
    			cell.addContent(" &nbsp; ");
    		}
            cell.setAlign("left");	
    	} else {
    		cell.addContent(" &nbsp; ");
    	}
        return(cell);    }
}
