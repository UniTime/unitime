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
package org.unitime.timetable.action;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.DepartmentListForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExternalDepartmentStatusType;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


/** 
* MyEclipse Struts
* Creation date: 02-18-2005
* 
* XDoclet definition:
* @struts:action path="/DepartmentList" name="departmentListForm" input="/admin/departmentList.jsp" scope="request" validate="true"
*/
/**
 * @author Tomas Muller
 */
@Service("/departmentList")
public class DepartmentListAction extends Action {
	
	@Autowired SessionContext sessionContext;

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {

		sessionContext.checkPermission(Right.Departments);

		DepartmentListForm departmentListForm = (DepartmentListForm) form;
		departmentListForm.setDepartments(Department.findAll(sessionContext.getUser().getCurrentAcademicSessionId()));
        
        if ("Apply".equals(departmentListForm.getOp())) {
        	sessionContext.getUser().setProperty("Departments.showUnusedDepts", departmentListForm.getShowUnusedDepts() ? "1" : "0");
        } else {
            departmentListForm.setShowUnusedDepts("1".equals(sessionContext.getUser().getProperty("Departments.showUnusedDepts", "0")));
        }

        boolean dispLastChanges = CommonValues.Yes.eq(UserProperty.DisplayLastChanges.get(sessionContext.getUser()));

        if ("Export PDF".equals(request.getParameter("op"))) {
            
            PdfWebTable webTable = new PdfWebTable((dispLastChanges ? 13 : 12), "Department List - " + sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel(),
                    "departmentList.do?ord=%%",
                    (dispLastChanges ? new String[] { "Number", "Abbv", "Name", "External\nManager", "Subjects", "Rooms",
                            "Status", "Dist Pref\nPriority", "Allow\nRequired", "Instructor\nPref", "Events", "Student\nScheduling", "Last\nChange" } 
                    : new String[] { "Number", "Abbreviation", "Name", "External\nManager", "Subjects", "Rooms", "Status",
                            "Dist Pref\nPriority", "Allow\nRequired", "Instructor\nPref", "Events", "Student\nScheduling" }),
                    new String[] { "left", "left", "left", "left", "right", "right", "left", "right", "left", "left", "left", "left", "left" },
                    new boolean[] { true, true, true, true, true, true, true, true, true, true, true, (dispLastChanges ? true: false), false });
            for (Iterator i=departmentListForm.getDepartments().iterator();i.hasNext();) {
                Department d = (Department) i.next();
                if (departmentListForm.getShowUnusedDepts() || !d.getSubjectAreas().isEmpty() || !d.getTimetableManagers().isEmpty() || d.isExternalManager().booleanValue()) {
                    DecimalFormat df5 = new DecimalFormat("####0");

                    String lastChangeStr = null;
                    Long lastChangeCmp = null;
                    if (dispLastChanges) {
                            List changes = ChangeLog.findLastNChanges(d.getSession().getUniqueId(), null, null, d.getUniqueId(), 1);
                            ChangeLog lastChange = (changes==null || changes.isEmpty() ? null : (ChangeLog) changes.get(0));
                            lastChangeStr = (lastChange==null?"":ChangeLog.sDFdate.format(lastChange.getTimeStamp())+" by "+lastChange.getManager().getShortName());
                            lastChangeCmp = new Long(lastChange==null?0:lastChange.getTimeStamp().getTime());
                    }
                    String allowReq = "";
                    int allowReqOrd = 0;
                    if (d.isAllowReqRoom() != null && d.isAllowReqRoom().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += "room";
                    	allowReqOrd += 1;
                    }
                    if (d.isAllowReqTime() != null && d.isAllowReqTime().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += "time";
                    	allowReqOrd += 2;
                    }
                    if (d.isAllowReqDistribution() != null && d.isAllowReqDistribution().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += "distribution";
                    	allowReqOrd += 4;
                    }
                    if (allowReqOrd == 7) allowReq = "all";
                    
                    String dependentStatuses = null;
                    if (d.isExternalManager() && d.getExternalStatusTypes() != null && !d.getExternalStatusTypes().isEmpty()) {
                    	TreeSet<ExternalDepartmentStatusType> set = new TreeSet<ExternalDepartmentStatusType>(new Comparator<ExternalDepartmentStatusType>() {
            				@Override
            				public int compare(ExternalDepartmentStatusType e1, ExternalDepartmentStatusType e2) {
            					return e1.getDepartment().compareTo(e2.getDepartment());
            				}
            			});
                    	set.addAll(d.getExternalStatusTypes());
                    	for (ExternalDepartmentStatusType t: set) {
                    		if (dependentStatuses == null)
                    			dependentStatuses = "    " + t.getDepartment().getDeptCode() + ": " + t.getStatusType().getLabel();
                    		else
                    			dependentStatuses += "\n    " + t.getDepartment().getDeptCode() + ": " + t.getStatusType().getLabel();
                    	}
                    }

                    webTable.addLine(null,
                            new String[] {
                                d.getDeptCode(),
                                d.getAbbreviation(),
                                d.getName(),
                                (d.isExternalManager().booleanValue()?d.getExternalMgrAbbv():""),
                                df5.format(d.getSubjectAreas().size()),
                                df5.format(d.getRoomDepts().size()),
                                (d.getStatusType() == null ? "@@ITALIC " : "")+d.effectiveStatusType().getLabel()+(d.getStatusType() == null?"@@END_ITALIC " : "") + (dependentStatuses == null ? "" : "\n" + dependentStatuses),
                                (d.getDistributionPrefPriority()==null && d.getDistributionPrefPriority().intValue()!=0 ? "" : d.getDistributionPrefPriority().toString()),
                                allowReq,
                                d.isInheritInstructorPreferences() ? "Yes" : "No",
                                d.isAllowEvents() ? "Yes" : "No",
                                d.isAllowStudentScheduling() ? "Yes" : "No",
                                lastChangeStr },
                           new Comparable[] {
                            d.getDeptCode(),
                            d.getAbbreviation(),
                            d.getName(),
                            (d.isExternalManager().booleanValue() ? d.getExternalMgrAbbv() : ""),
                            new Integer(d.getSubjectAreas().size()),
                            new Integer(d.getRoomDepts().size()),
                            d.effectiveStatusType().getOrd(),
                            d.getDistributionPrefPriority(),
                            new Integer(allowReqOrd),
                            d.isInheritInstructorPreferences(),
                            d.isAllowEvents(),
                            d.isAllowStudentScheduling(),
                            lastChangeCmp });
                }
            }
            
            ExportUtils.exportPDF(
            		webTable,
            		WebTable.getOrder(sessionContext, "DepartmentList.ord"),
            		response, "departments");
            return null;
        }
        
		WebTable webTable = new WebTable((dispLastChanges ? 13 : 12), "",
				"departmentList.do?ord=%%",
				(dispLastChanges 
					? new String[] { "Code", "Abbv", "Name", "External<br>Manager", 
									 "Subjects", "Rooms", "Status", "Dist&nbsp;Pref<br>Priority", 
									 "Allow<br>Required", "Instructor<br>Preferences", "Events", "Student<br>Scheduling", "Last<br>Change" } 
					: new String[] { "Code", "Abbreviation", "Name", "External Manager",
									 "Subjects", "Rooms", "Status", "Dist&nbsp;Pref<br>Priority", 
									 "Allow<br>Required", "Instructor<br>Preferences", "Events", "Student<br>Scheduling" }),
				new String[] { "left", "left", "left", "left", "right", "right", "left", "right", "left", "left", "left", "left", "left" },
                new boolean[] { true, true, true, true, true, true, true, true, true, true, true, (dispLastChanges ? true: false), false });
		WebTable.setOrder(sessionContext, "DepartmentList.ord", request.getParameter("ord"), 1);
        webTable.enableHR("#9CB0CE");
        webTable.setRowStyle("white-space: nowrap");
        
        for (Iterator i=departmentListForm.getDepartments().iterator();i.hasNext();) {
            Department d = (Department) i.next();
    		if (departmentListForm.getShowUnusedDepts() || !d.getSubjectAreas().isEmpty()
    			|| !d.getTimetableManagers().isEmpty()
    			|| d.isExternalManager().booleanValue()) {
    				
    			DecimalFormat df5 = new DecimalFormat("####0");

    			String lastChangeStr = null;
    			Long lastChangeCmp = null;
    			if (dispLastChanges) {
    					List changes = ChangeLog.findLastNChanges(d
    							.getSession().getUniqueId(), null, null, d
    							.getUniqueId(), 1);
    					ChangeLog lastChange = (changes == null
    							|| changes.isEmpty() ? null
    							: (ChangeLog) changes.get(0));
    					lastChangeStr = (lastChange == null ? "&nbsp;"
    							: "<span title='"
    							+ lastChange.getLabel()
    							+ "'>"
    							+ ChangeLog.sDFdate.format(lastChange
    							.getTimeStamp())
    							+ " by "
    							+ lastChange.getManager()
    							.getShortName() + "</span>");
    					lastChangeCmp = new Long(lastChange == null ? 0
    							: lastChange.getTimeStamp().getTime());
    			}
    			
                        String allowReq = "";
                        int allowReqOrd = 0;
                        if (d.isAllowReqRoom() != null && d.isAllowReqRoom().booleanValue()) {
                        	if (!allowReq.isEmpty()) allowReq += ", ";
                        	allowReq += "room";
                        	allowReqOrd += 1;
                        }
                        if (d.isAllowReqTime() != null && d.isAllowReqTime().booleanValue()) {
                        	if (!allowReq.isEmpty()) allowReq += ", ";
                        	allowReq += "time";
                        	allowReqOrd += 2;
                        }
                        if (d.isAllowReqDistribution() != null && d.isAllowReqDistribution().booleanValue()) {
                        	if (!allowReq.isEmpty()) allowReq += ", ";
                        	allowReq += "distribution";
                        	allowReqOrd += 4;
                        }
                        if (allowReqOrd == 7) allowReq = "all";
                        if (allowReqOrd == 0) allowReq = "&nbsp;";
                        
                String dependentStatuses = null;
                if (d.isExternalManager() && d.getExternalStatusTypes() != null && !d.getExternalStatusTypes().isEmpty()) {
                	TreeSet<ExternalDepartmentStatusType> set = new TreeSet<ExternalDepartmentStatusType>(new Comparator<ExternalDepartmentStatusType>() {
        				@Override
        				public int compare(ExternalDepartmentStatusType e1, ExternalDepartmentStatusType e2) {
        					return e1.getDepartment().compareTo(e2.getDepartment());
        				}
        			});
                	set.addAll(d.getExternalStatusTypes());
                	for (ExternalDepartmentStatusType t: set) {
                		if (dependentStatuses == null)
                			dependentStatuses = t.getDepartment().getDeptCode() + ": " + t.getStatusType().getLabel();
                		else
                			dependentStatuses += "<br>" + t.getDepartment().getDeptCode() + ": " + t.getStatusType().getLabel();
                	}
                }
                
                boolean editable = sessionContext.hasPermission(d, Right.DepartmentEdit) || sessionContext.hasPermission(d, Right.DepartmentLimitedEdit);

    			webTable.addLine(
    				(editable ? "onClick=\"document.location='departmentEdit.do?op=Edit&id=" + d.getUniqueId() + "';\"" : null),
    				new String[] {
    						d.getDeptCode(),
    						d.getAbbreviation()==null ? "&nbsp;" : d.getAbbreviation(),
    						"<A name='" + d.getUniqueId() + "'>" + d.getName() + "</A>",
    						(d.isExternalManager().booleanValue() 
    							? "<span title='" + d.getExternalMgrLabel()	+ "'>" + d.getExternalMgrAbbv()	+ "</span>"
    							: "&nbsp;"),
    						df5.format(d.getSubjectAreas().size()),
    						df5.format(d.getRoomDepts().size()),
    						(d.getStatusType() == null ? "<i>" : "&nbsp;")
    							+ d.effectiveStatusType().getLabel()
    							+ (d.getStatusType() == null ? "</i>" : "") + (dependentStatuses == null ? "" : "<div style='padding-left:30px;'>" + dependentStatuses + "</div>"),
    						(d.getDistributionPrefPriority() == null && d.getDistributionPrefPriority().intValue() != 0 
    							? "&nbsp;" : d.getDistributionPrefPriority().toString()),
    						allowReq,
    						(d.isInheritInstructorPreferences() ? "<IMG border='0' title='Instructor preferences are to be inherited.' alt='Inherit Instructor Preferences' align='absmiddle' src='images/accept.png'>" : ""),
    						(d.isAllowEvents() ? "<IMG border='0' title='This department has event management enabled.' alt='Event Management' align='absmiddle' src='images/accept.png'>" : ""),
    						(d.isAllowStudentScheduling() ? "<IMG border='0' title='This department has student scheduling enabled.' alt='Student Scheduling' align='absmiddle' src='images/accept.png'>" : ""),
    						lastChangeStr },
    				new Comparable[] {
    						d.getDeptCode(),
    						d.getAbbreviation()==null ? "&nbsp;" : d.getAbbreviation(),
    						d.getName(),
    						(d.isExternalManager()
    						.booleanValue() ? d
    						.getExternalMgrAbbv() : ""),
    						new Integer(d.getSubjectAreas()
    						.size()),
    						new Integer(d.getRoomDepts().size()),
    						d.effectiveStatusType().getOrd(),
    						d.getDistributionPrefPriority(),
    						new Integer(allowReqOrd),
    						d.isInheritInstructorPreferences(),
    						d.isAllowEvents(),
    						d.isAllowStudentScheduling(),
    						lastChangeCmp });
    		}
        }
        
        request.setAttribute("table", webTable.printTable(WebTable.getOrder(sessionContext, "DepartmentList.ord")));
        
		return mapping.findForward("showDepartmentList");
		
	}

}
