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
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.unitime.commons.User;
import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.util.Constants;


public class WebInstrOfferingConfigTableBuilder extends
		WebInstructionalOfferingTableBuilder {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
    private String[] COLUMNS = {LABEL,
		MSG.columnExternalId(),
		MSG.columnMinPerWk(),
		MSG.columnLimit(),
		MSG.columnRoomRatio(),
		MSG.columnManager(),
		MSG.columnDatePattern(),
		MSG.columnTimePattern(),
		MSG.columnPreferences(),
		MSG.columnInstructor(),
		MSG.columnTimetable(),
		MSG.columnSubpartCredit()};

	public WebInstrOfferingConfigTableBuilder() {
		super();
	}
	
	public String buttonsTable(InstrOfferingConfig ioc, boolean isEditable, boolean isFullyEditable, boolean isLimitedEditable, boolean isExtManaged){
		StringBuffer btnTable = new StringBuffer("");
		btnTable.append("<table class='BottomBorder' width='100%'><tr><td width='100%' nowrap>");
		btnTable.append("<DIV class='WelcomeRowHeadNoLine'>");
		String configName = ioc.getName();
	    if (configName==null || configName.trim().length()==0) configName = ioc.getUniqueId().toString();
		btnTable.append(MSG.sectionTitleConfiguration() + configName);
		btnTable.append("</DIV>");
		btnTable.append("</td><td style='padding-bottom: 3px' nowrap>");
		boolean notOffered = ioc.getInstructionalOffering().isNotOffered().booleanValue();
		if (!notOffered && (isEditable || isLimitedEditable || isExtManaged)) {
	        btnTable.append("<table border='0' align='right' cellspacing='1' cellpadding='0'>");
	        
	        if (isEditable) {
		        btnTable.append("<td>");
		        btnTable.append("	<form method='post' action='instructionalOfferingConfigEdit.do' class='FormWithNoPadding'>");
		        btnTable.append("		<input type='hidden' name='configId' value='" + ioc.getUniqueId().toString() + "'>");
		        btnTable.append("		<input type='submit' name='op' value='" + MSG.actionEditConfiguration() + "' title='" + MSG.titleEditConfiguration() + "' class='btn'>");
		        btnTable.append("	</form>");
		        btnTable.append("</td>");
	        }
	        
	        if ((isEditable || isExtManaged) && ioc.hasClasses() && !ioc.isUnlimitedEnrollment().booleanValue()) {
		        btnTable.append("<td>");
		        btnTable.append("	<form method='post' action='instructionalOfferingModify.do' class='FormWithNoPadding'>");
		        btnTable.append("		<input type='hidden' name='uid' value='" + ioc.getUniqueId().toString() + "'>");
		        btnTable.append("		<input type='submit' name='op' value='" + MSG.actionClassSetup() +"' title='" + MSG.titleClassSetup() + "' class='btn'> ");
		        btnTable.append("	</form>");
		        btnTable.append("</td>");
	        }

	        if (ioc.hasClasses() && isLimitedEditable) {
	        	btnTable.append("<td>");
		        btnTable.append("	<form method='post' action='classInstructorAssignment.do' class='FormWithNoPadding'>");
		        btnTable.append("		<input type='hidden' name='uid' value='" + ioc.getUniqueId().toString() + "'>");
		        btnTable.append("		<input type='submit' name='op' value='" + MSG.actionAssignInstructors() + "' title='" + MSG.titleAssignInstructors() + "' class='btn'> ");
		        btnTable.append("	</form>");
		        btnTable.append("</td>");
	        }
	        
            /*
	        if (isFullyEditable) { //config is editable PLUS all subparts are editable as well
		        btnTable.append("<td>");
		        btnTable.append("	<form method='post' action='instructionalOfferingConfigEdit.do' class='FormWithNoPadding'>");
		        btnTable.append("		<input type='hidden' name='configId' value='" + ioc.getUniqueId().toString() + "'>");
		        btnTable.append("		<input type='submit' name='op' value='Duplicate' title='Copy as a new Configuration' class='btn'>");
		        btnTable.append("	</form>");
		        btnTable.append("</td>");
	        }
            */
	        
	        //TODO Reservations - functionality to be made visible later
	        /*
	        if (isEditable) {
	        	btnTable.append("<td>");
	        	btnTable.append("	<form method='post' action='reservationAdd.do' class='FormWithNoPadding'>");
	        	btnTable.append("		<input type='hidden' name='ownerId' value='" + ioc.getUniqueId().toString() + "'>");
	        	btnTable.append("		<input type='hidden' name='ownerClassId' value='" + Constants.RESV_OWNER_CONFIG + "'>");
	        	btnTable.append("		<input type='submit' name='op' value='Reservations' title='Manage Reservations' class='btn'> ");
	        	btnTable.append("	</form>");
	        	btnTable.append("</td>");
	        }
	        */

	        btnTable.append("</tr>");
	        btnTable.append("</table>");
	    }
		btnTable.append("</td></tr></table>");		
		return(btnTable.toString());
	}
	
    public void htmlTableForInstructionalOfferingConfig(
    		Vector subpartIds,
    		ClassAssignmentProxy classAssignment, 
    		ExamAssignmentProxy examAssignment,
            Long instrOfferingConfigId, 
            User user,
            JspWriter outputStream){
    	
    	if (instrOfferingConfigId != null && user != null){
	        InstrOfferingConfigDAO iocDao = new InstrOfferingConfigDAO();
	        InstrOfferingConfig ioc = iocDao.get(instrOfferingConfigId);
	        
	        this.htmlTableForInstructionalOfferingConfig(subpartIds, classAssignment, examAssignment, ioc, user, outputStream);
    	}
    }
    
    private void htmlTableForInstructionalOfferingConfig(
    		Vector subpartIds,
    		ClassAssignmentProxy classAssignment,
    		ExamAssignmentProxy examAssignment,
            InstrOfferingConfig ioc, 
            User user,
            JspWriter outputStream){
    	
    	if ("yes".equals(Settings.getSettingValue(user, Constants.SETTINGS_KEEP_SORT))) {
    		setClassComparator(
    			new ClassCourseComparator(
    					UserData.getProperty(user.getId(),"InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
    					classAssignment,
    					false
    			)
    		);
    	}

    	if (ioc != null && user != null){
	        
	        this.setDisplayDistributionPrefs(false);
	        
	        if (isShowTimetable()) {
	        	boolean hasTimetable = false;
	        	try {
	        		TimetableManager manager = TimetableManager.getManager(user);
	        		if (manager!=null && manager.canSeeTimetable(Session.getCurrentAcadSession(user), user) && classAssignment!=null) {
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
	        		}
	        	} catch (Exception e) {}
	        	setDisplayTimetable(hasTimetable);
	        }
	        
	        boolean isEditable = ioc.isEditableBy(user);
	        boolean isFullyEditable = ioc.isEditableBy(user); //config is editable PLUS all subparts are editable as well
	        boolean isExtManaged = false;
	        if (!isEditable) {
	            isExtManaged = ioc.hasExternallyManagedSubparts(user, true);
	        }
	        boolean isLimitedEditable = false;
	        if (ioc.hasClasses()) {
	        	for (Iterator i=ioc.getSchedulingSubparts().iterator();i.hasNext();) {
	        		SchedulingSubpart ss = (SchedulingSubpart)i.next();
	        		if (ss.isLimitedEditable(user)) {
	        			isLimitedEditable = true;
	        		}
	        		if (!ss.isEditableBy(user))
	        			isFullyEditable = false;
	        	}
	        }	        

	        if (getDisplayConfigOpButtons()) {
        		try {
        			outputStream.write(this.buttonsTable(ioc, isEditable, isFullyEditable, isLimitedEditable, isExtManaged));
        		} catch (IOException e) {}
	        }
	        if (StudentClassEnrollment.sessionHasEnrollments(Session.getCurrentAcadSession(user) == null?null:Session.getCurrentAcadSession(user).getUniqueId())){
	            String[] cols = {LABEL,
	            		MSG.columnExternalId(),
	            		MSG.columnMinPerWk(),
	            		MSG.columnDemand(),
	            		MSG.columnLimit(),
	            		MSG.columnRoomRatio(),
	            		MSG.columnManager(),
	            		MSG.columnDatePattern(),
	            		MSG.columnTimePattern(),
	            		MSG.columnPreferences(),
	            		MSG.columnInstructor(),
	            		MSG.columnTimetable(),
	            		MSG.columnSubpartCredit()};
	            setVisibleColumns(cols);
	        } else {
		        setVisibleColumns(COLUMNS);	        	
	        }
        	TableStream configTable = this.initTable(outputStream, (Session.getCurrentAcadSession(user) == null?null:Session.getCurrentAcadSession(user).getUniqueId()));
        	this.buildConfigRow(subpartIds, classAssignment, examAssignment,  configTable, ioc.getInstructionalOffering().getControllingCourseOffering(), ioc, user, !getDisplayConfigOpButtons(), true);
        	configTable.tableComplete();
	    }
    }

    
    
    public void htmlConfigTablesForInstructionalOffering(
    		HttpSession session,
    		ClassAssignmentProxy classAssignment, 
    		ExamAssignmentProxy examAssignment,
            Long instructionalOffering, 
            User user,
            JspWriter outputStream,
            String backType,
            String backId){
    	
    	setBackType(backType);
        setBackId(backId);    	
    	
    	if ("yes".equals(Settings.getSettingValue(user, Constants.SETTINGS_KEEP_SORT))) {
    		setClassComparator(
    			new ClassCourseComparator(
    					UserData.getProperty(user.getId(),"InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
    					classAssignment,
    					false
    			)
    		);
    	}
    	
       	if (instructionalOffering != null && user != null){
	        InstructionalOfferingDAO iDao = new InstructionalOfferingDAO();
	        InstructionalOffering io = iDao.get(instructionalOffering);
	        
			setUserSettings(user);
			
			Vector subpartIds = new Vector();

			if (io.getInstrOfferingConfigs() != null){
	        	TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
	        	configs.addAll(io.getInstrOfferingConfigs());
	        	InstrOfferingConfig ioc = null;
	        	int idx = 0;
	        	for(Iterator it = configs.iterator(); it.hasNext();idx++){
	        		ioc = (InstrOfferingConfig) it.next();
	        		if (idx>0 && getDisplayConfigOpButtons()) {
	        			try {
	        				outputStream.println("<br>");
	        			} catch (IOException e) {}
	        		}
	        		this.htmlTableForInstructionalOfferingConfig(subpartIds, classAssignment, examAssignment, ioc, user, outputStream);
	        	}
	        }
			
			Navigation.set(session, Navigation.sSchedulingSubpartLevel, subpartIds);
       	}
    }
    
    protected TableCell buildPrefGroupLabel(CourseOffering co, PreferenceGroup prefGroup, String indentSpaces, boolean isEditable, String prevLabel){
    	TableCell cell = super.buildPrefGroupLabel(co, prefGroup, indentSpaces, isEditable, prevLabel);
    	if ("PreferenceGroup".equals(getBackType()) && prefGroup.getUniqueId().toString().equals(getBackId()))
    		cell.addContent("<A name=\"back\"></A>");
    	return cell;
    }
}    
