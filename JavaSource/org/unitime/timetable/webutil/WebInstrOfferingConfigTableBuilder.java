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
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.jsp.JspWriter;

import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;


/**
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
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
	
	public String buttonsTable(InstrOfferingConfig ioc, SessionContext context) {
		StringBuffer btnTable = new StringBuffer("");
		btnTable.append("<table class='BottomBorder' width='100%'><tr><td width='100%' nowrap>");
		btnTable.append("<DIV class='WelcomeRowHeadNoLine'>");
		String configName = ioc.getName();
	    if (configName==null || configName.trim().length()==0) configName = ioc.getUniqueId().toString();
	    if (ioc.getInstructionalMethod() != null)
	    	btnTable.append(MSG.labelConfigurationWithInstructionalMethod(configName, ioc.getInstructionalMethod().getLabel()));
	    else
	    	btnTable.append(MSG.labelConfiguration(configName));
		btnTable.append("</DIV>");
		btnTable.append("</td><td style='padding-bottom: 3px' nowrap>");
		boolean notOffered = ioc.getInstructionalOffering().isNotOffered().booleanValue();
		if (!notOffered) {
	        btnTable.append("<table border='0' align='right' cellspacing='1' cellpadding='0'>");
	        
	        if (context.hasPermission(ioc, Right.InstrOfferingConfigEdit)) {
		        btnTable.append("<td>");
		        btnTable.append("	<form method='post' action='instructionalOfferingConfigEdit.do' class='FormWithNoPadding'>");
		        btnTable.append("		<input type='hidden' name='configId' value='" + ioc.getUniqueId().toString() + "'>");
		        btnTable.append("		<input type='submit' name='op' value='" + MSG.actionEditConfiguration() + "' title='" + MSG.titleEditConfiguration() + "' class='btn'>");
		        btnTable.append("	</form>");
		        btnTable.append("</td>");
	        }
	        
	        if (context.hasPermission(ioc, Right.MultipleClassSetup)) {
		        btnTable.append("<td>");
		        btnTable.append("	<form method='post' action='instructionalOfferingModify.do' class='FormWithNoPadding'>");
		        btnTable.append("		<input type='hidden' name='uid' value='" + ioc.getUniqueId().toString() + "'>");
		        btnTable.append("		<input type='submit' name='op' value='" + MSG.actionClassSetup() +"' title='" + MSG.titleClassSetup() + "' class='btn'> ");
		        btnTable.append("	</form>");
		        btnTable.append("</td>");
	        }

	        if (context.hasPermission(ioc, Right.AssignInstructors)) {
	        	btnTable.append("<td>");
		        btnTable.append("	<form method='post' action='classInstructorAssignment.do' class='FormWithNoPadding'>");
		        btnTable.append("		<input type='hidden' name='uid' value='" + ioc.getUniqueId().toString() + "'>");
		        btnTable.append("		<input type='submit' name='op' value='" + MSG.actionAssignInstructors() + "' title='" + MSG.titleAssignInstructors() + "' class='btn'> ");
		        btnTable.append("	</form>");
		        btnTable.append("</td>");
	        }

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
            SessionContext context,
            JspWriter outputStream){
    	
    	if (instrOfferingConfigId != null){
	        InstrOfferingConfigDAO iocDao = new InstrOfferingConfigDAO();
	        InstrOfferingConfig ioc = iocDao.get(instrOfferingConfigId);
	        
	        this.htmlTableForInstructionalOfferingConfig(subpartIds, classAssignment, examAssignment, ioc, context, outputStream);
    	}
    }
    
    private void htmlTableForInstructionalOfferingConfig(
    		Vector subpartIds,
    		ClassAssignmentProxy classAssignment,
    		ExamAssignmentProxy examAssignment,
            InstrOfferingConfig ioc, 
            SessionContext context,
            JspWriter outputStream){
    	
    	if (CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.ClassesKeepSort))) {
    		setClassComparator(
    			new ClassCourseComparator(
    					context.getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
    					classAssignment,
    					false
    			)
    		);
    	}

    	if (ioc != null){
	        
	        this.setDisplayDistributionPrefs(false);
	        
	        if (isShowTimetable()) {
	        	boolean hasTimetable = false;
	        	if (context.hasPermission(Right.ClassAssignments) && classAssignment != null) {
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
	        
	        if (getDisplayConfigOpButtons()) {
        		try {
        			outputStream.write(this.buttonsTable(ioc, context));
        		} catch (IOException e) {}
	        }
	        if (StudentClassEnrollment.sessionHasEnrollments(context.getUser().getCurrentAcademicSessionId())) {
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
	        ClassDurationType dtype = ioc.getEffectiveDurationType();
        	TableStream configTable = this.initTable(outputStream, context.getUser().getCurrentAcademicSessionId(), dtype == null ? MSG.columnMinPerWk() : dtype.getLabel());
        	this.buildConfigRow(subpartIds, classAssignment, examAssignment,  configTable, ioc.getInstructionalOffering().getControllingCourseOffering(), ioc, context, !getDisplayConfigOpButtons(), true);
        	configTable.tableComplete();
	    }
    }

    
    
    public void htmlConfigTablesForInstructionalOffering(
    		SessionContext context,
    		ClassAssignmentProxy classAssignment, 
    		ExamAssignmentProxy examAssignment,
            Long instructionalOffering, 
            JspWriter outputStream,
            String backType,
            String backId){
    	
    	setBackType(backType);
        setBackId(backId);    	
    	
        if (CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.ClassesKeepSort))) {
    		setClassComparator(
    			new ClassCourseComparator(
    					context.getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
    					classAssignment,
    					false
    			)
    		);
    	}
    	
       	if (instructionalOffering != null) {
	        InstructionalOfferingDAO iDao = new InstructionalOfferingDAO();
	        InstructionalOffering io = iDao.get(instructionalOffering);
	        
			setUserSettings(context.getUser());
			
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
	        		this.htmlTableForInstructionalOfferingConfig(subpartIds, classAssignment, examAssignment, ioc, context, outputStream);
	        	}
	        }
			
			Navigation.set(context, Navigation.sSchedulingSubpartLevel, subpartIds);
       	}
    }
    
    @Override
    protected TableCell buildPrefGroupLabel(CourseOffering co, PreferenceGroup prefGroup, int indentSpaces, boolean isEditable, String prevLabel, String icon){
    	TableCell cell = super.buildPrefGroupLabel(co, prefGroup, indentSpaces, isEditable, prevLabel, icon);
    	if ("PreferenceGroup".equals(getBackType()) && prefGroup.getUniqueId().toString().equals(getBackId()))
    		cell.addContent("<A name=\"back\"></A>");
    	return cell;
    }
    
    @Override
    protected TableCell buildMinPerWeek(PreferenceGroup prefGroup, boolean isEditable){
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
}    
