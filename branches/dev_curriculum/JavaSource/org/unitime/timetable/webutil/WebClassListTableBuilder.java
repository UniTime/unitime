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
package org.unitime.timetable.webutil;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.unitime.commons.User;
import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.commons.web.htmlgen.TableStream;
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
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.util.Constants;


/**
 * @author Stephanie Schluttenhofer
 *
 */
public class WebClassListTableBuilder extends
		WebInstructionalOfferingTableBuilder {
    public static String STUDENT_SCHEDULE_NOTE = "Student Schedule Note";
    protected String getSchedulePrintNoteLabel(){
    	return STUDENT_SCHEDULE_NOTE;
    }

	/**
	 * 
	 */
	public WebClassListTableBuilder() {
		super();
	}
	
	protected String additionalNote(){
		return(new String());
	}
	
	protected String labelForTable(SubjectArea subjectArea){
		StringBuffer sb = new StringBuffer();
		sb.append("<p style=\"page-break-before: always\" class=\"WelcomeRowHead\"><b><font size=\"+1\">");
		sb.append(subjectArea.getSubjectAreaAbbreviation());
		sb.append(" - ");
		sb.append(subjectArea.getSession().getLabel());
		sb.append(additionalNote());
		sb.append("</font></b></p>");
		return(sb.toString());		
	}
	
	
	public void htmlTableForClasses(HttpSession session, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, ClassListForm form, User user, JspWriter outputStream, String backType, String backId){
        
        this.setVisibleColumns(form);
        setBackType(backType);
        setBackId(backId);
        
        TreeSet classes = (TreeSet) form.getClasses();
    	Navigation.set(session, Navigation.sClassLevel, classes);
        
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
            	ct = 0;
            	try {
					outputStream.print(labelForTable(subjectArea));
				} catch (IOException e) {
					e.printStackTrace();
				}
		        table = this.initTable(outputStream, (Session.getCurrentAcadSession(user) == null?null:Session.getCurrentAcadSession(user).getUniqueId()));
		    }		        
            this.buildClassRow(classAssignment,examAssignment, ++ct, table, c, "", user, prevLabel);
            prevLabel = c.getClassLabel();
        }  
        table.tableComplete();
    }
	
    protected TableCell buildPrefGroupLabel(PreferenceGroup prefGroup, String indentSpaces, boolean isEditable, String prevLabel){
    	if (prefGroup instanceof Class_) {
    		TableCell cell = initNormalCell(indentSpaces, isEditable);
    		Class_ aClass = (Class_) prefGroup;
	    	if(!isEditable){
	    		cell.addContent("<font color='"+disabledColor+"'>");
	    	}
	    	if ("PreferenceGroup".equals(getBackType()) && prefGroup.getUniqueId().toString().equals(getBackId()))
	    		cell.addContent("<A name=\"back\"></A>");
	    	cell.addContent("<b>");
	        cell.addContent("<A name=\"A" + prefGroup.getUniqueId().toString() + "\"></A>");
	        String label = aClass.getClassLabel();
	        String title = aClass.getClassLabelWithTitle();
	        if (prevLabel != null && label.equals(prevLabel)){
	        	label = " &nbsp;";
	        }
			if (!aClass.isDisplayInScheduleBook().booleanValue()){
				title += " - Do Not Display In Schedule Book.";
				label = "<i>" + label + "</i>";
			}
	        cell.addContent(label);
	        cell.setTitle(title);
	        cell.addContent("</b>");
	        cell.setNoWrap(true);
	        if(!isEditable){
	        	cell.addContent("</font>");
	        }
	        return(cell);
        } else {
        	return(super.buildPrefGroupLabel(prefGroup,indentSpaces, isEditable, null));
        }     
    }
	
    public void htmlTableForClasses(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, TreeSet classes, Long subjectAreaId, User user, JspWriter outputStream){
    	Session session = Session.getCurrentAcadSession(user);
    	String[] columns;
         if (StudentClassEnrollment.sessionHasEnrollments(session == null?null:session.getUniqueId())) {
        	String[] tcolumns = {LABEL,
        		DEMAND,
				LIMIT,
				ROOM_RATIO,
				DATE_PATTERN,
				TIME_PATTERN,
				PREFERENCES,
				INSTRUCTOR,
				TIMETABLE,
				SCHEDULE_PRINT_NOTE};
        	columns = tcolumns;
         } else  {
         	String[] tcolumns = {LABEL,
        			LIMIT,
        			ROOM_RATIO,
        			DATE_PATTERN,
        			TIME_PATTERN,
        			PREFERENCES,
        			INSTRUCTOR,
        			TIMETABLE,
        			SCHEDULE_PRINT_NOTE};
            columns = tcolumns;
         };
         setVisibleColumns(columns);

        if (isShowTimetable()) {
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
        	setShowDivSec(hasTimetable);
        }
        setUserSettings(user);
        
		TableStream table = this.initTable(outputStream, (Session.getCurrentAcadSession(user) == null?null:Session.getCurrentAcadSession(user).getUniqueId()));
        Iterator it = classes.iterator();
        Class_ cls = null;
        String prevLabel = null;
        
        int ct = 0;
        while (it.hasNext()){
            cls = (Class_) it.next();
            this.buildClassRow(classAssignment, examAssignment, ++ct, table, cls, "", user, prevLabel);
            prevLabel = cls.getClassLabel();
        }     
        table.tableComplete();
        
    }
    
    public void htmlTableForSubpartClasses(
    		HttpSession session,
    		ClassAssignmentProxy classAssignment, 
    		ExamAssignmentProxy examAssignment,
    		Long schedulingSubpartId,
    		User user, 
    		JspWriter outputStream,
    		String backType,
    		String backId){
    	
        setBackType(backType);
        setBackId(backId);

        if (schedulingSubpartId != null && user != null) {
	    	SchedulingSubpartDAO ssDao = new SchedulingSubpartDAO();
	    	SchedulingSubpart ss = ssDao.get(schedulingSubpartId);
	        TreeSet ts = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
	    	if ("yes".equals(Settings.getSettingValue(user, Constants.SETTINGS_KEEP_SORT))) {
	    		ts = new TreeSet(
	    			new ClassComparator(
	    					UserData.getProperty(user.getId(),"ClassList.sortBy",ClassListForm.sSortByName),
	    					classAssignment,
	    					UserData.getPropertyBoolean(user.getId(),"ClassList.sortByKeepSubparts", false)
	    			)
	    		);
	    	}
	        
	 		ts.addAll(ss.getClasses());
	 		Navigation.set(session, Navigation.sClassLevel, ts);
	        this.htmlTableForClasses(classAssignment, examAssignment, ts, ss.getControllingCourseOffering().getSubjectArea().getUniqueId(), user, outputStream);
    	}
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
