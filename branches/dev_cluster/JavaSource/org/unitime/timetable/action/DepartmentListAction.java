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
package org.unitime.timetable.action;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

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
            
            PdfWebTable webTable = new PdfWebTable((dispLastChanges ? 10 : 9), "Department List - " + sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel(),
                    "departmentList.do?ord=%%",
                    (dispLastChanges ? new String[] { "Number", "Abbv", "Name", "External\nManager", "Subjects", "Rooms",
                            "Status", "Dist Pref\nPriority", "Allow\nRequired", "Events", "Last\nChange" } 
                    : new String[] { "Number", "Abbreviation", "Name", "External\nManager", "Subjects", "Rooms", "Status",
                            "Dist Pref\nPriority", "Allow\nRequired", "Events" }),
                    new String[] { "left", "left", "left", "left", "right", "right", "left", "right", "left", "left", "left" },
                    new boolean[] { true, true, true, true, true, true, true, true, true, false });
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

                    webTable.addLine(null,
                            new String[] {
                                d.getDeptCode(),
                                d.getAbbreviation(),
                                d.getName(),
                                (d.isExternalManager().booleanValue()?d.getExternalMgrAbbv():""),
                                df5.format(d.getSubjectAreas().size()),
                                df5.format(d.getRoomDepts().size()),
                                (d.getStatusType() == null ? "@@ITALIC " : "")+d.effectiveStatusType().getLabel()+(d.getStatusType() == null?"@@END_ITALIC " : ""),
                                (d.getDistributionPrefPriority()==null && d.getDistributionPrefPriority().intValue()!=0 ? "" : d.getDistributionPrefPriority().toString()),
                                allowReq,
                                d.isAllowEvents() ? "Yes" : "No",
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
                            d.isAllowEvents(),
                            lastChangeCmp });
                }
            }
            
            ExportUtils.exportPDF(
            		webTable,
            		WebTable.getOrder(sessionContext, "DepartmentList.ord"),
            		response, "departments");
            return null;
        }
        
		WebTable webTable = new WebTable((dispLastChanges ? 10 : 9), "",
				"departmentList.do?ord=%%",
				(dispLastChanges 
					? new String[] { "Code", "Abbv", "Name", "External<br>Manager", 
									 "Subjects", "Rooms", "Status", "Dist&nbsp;Pref Priority", 
									 "Allow Required", "Events", "Last Change" } 
					: new String[] { "Code", "Abbreviation", "Name", "External Manager",
									 "Subjects", "Rooms", "Status", "Dist Pref Priority", 
									 "Allow Required", "Events" }),
				new String[] { "left", "left", "left", "left", "right",	"right", "left", "right", "left", "left", "left" },
				new boolean[] { true, true, true, true, true, true, true, true, true, true, false });
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

    			webTable.addLine(
    				"onClick=\"document.location='departmentEdit.do?op=Edit&id=" + d.getUniqueId() + "';\"",
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
    							+ (d.getStatusType() == null ? "</i>" : ""),
    						(d.getDistributionPrefPriority() == null && d.getDistributionPrefPriority().intValue() != 0 
    							? "&nbsp;" : d.getDistributionPrefPriority().toString()),
    						allowReq,
    						(d.isAllowEvents() ? "<IMG border='0' title='This department has event management enabled.' alt='Event Management' align='absmiddle' src='images/tick.gif'>" : ""),
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
    						d.isAllowEvents(),
    						lastChangeCmp });
    		}
        }
        
        request.setAttribute("table", webTable.printTable(WebTable.getOrder(sessionContext, "DepartmentList.ord")));
        
		return mapping.findForward("showDepartmentList");
		
	}

}
