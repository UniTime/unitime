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
		return(" " + MSG.classAssignmentsAdditionalNote());
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
		        
            
            this.buildClassRow(classAssignment,examAssignment, ++ct, table, co, c, 0, context, prevLabel);
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
    			InstructorComparator ic = new InstructorComparator(); ic.setCompareBy(ic.COMPARE_BY_INDEX);
            	TreeSet sortedInstructors = new TreeSet(ic);
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
