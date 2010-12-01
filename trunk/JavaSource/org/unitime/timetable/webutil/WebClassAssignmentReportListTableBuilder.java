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
package org.unitime.timetable.webutil;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.unitime.commons.User;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.timetable.form.ClassAssignmentsReportForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.util.Constants;


/**
 * @author Stephanie Schluttenhofer
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

    public void htmlTableForClasses(HttpSession session, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, ClassAssignmentsReportForm form, User user, JspWriter outputStream, String backType, String backId){
        
        this.setVisibleColumns(form);
        setBackType(backType);
        setBackId(backId);
 		
        Collection classes = (Collection) form.getClasses();
        Navigation.set(session, Navigation.sClassLevel, classes);
        
        if (getDisplayTimetable()) {
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
        
        if (examAssignment!=null || Exam.hasTimetable((Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME))) {
            setShowExam(true);
            setShowExamTimetable(true);
            setShowExamName(false);
        }
        
        Class_ c = null;
        TableStream table = null;
        int ct = 0;
        Iterator it = classes.iterator();
        SubjectArea subjectArea = null;
        String prevLabel = null;
        while (it.hasNext()){
            c = (Class_) it.next();
            if (subjectArea == null || !subjectArea.getUniqueId().equals(c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().getUniqueId())){
            	if(table != null) {
            		table.tableComplete();
	            	try {
						outputStream.print("<br>");
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            	subjectArea = c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea();
            	try {
					outputStream.print(labelForTable(subjectArea));
				} catch (IOException e) {
					e.printStackTrace();
				}
            	ct = 0;
		        table = this.initTable(outputStream, (Session.getCurrentAcadSession(user) == null?null:Session.getCurrentAcadSession(user).getUniqueId()));
		    }
		        
            
            this.buildClassRow(classAssignment,examAssignment, ++ct, table, c, "", user, prevLabel);
            prevLabel = c.getClassLabel();
        }  
        if(table != null)
        	table.tableComplete();      
    }
}
