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
package org.unitime.timetable.action;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.HibernateException;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.DepartmentListForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.util.Constants;
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
public class DepartmentListAction extends Action {

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

	    HttpSession webSession = request.getSession();
        if(!Web.isLoggedIn( webSession )) {
            throw new Exception ("Access Denied.");
        }
        
	    User user = Web.getUser(webSession);	    
		DepartmentListForm departmentListForm = (DepartmentListForm) form;
		departmentListForm.setDepartments(Department.findAll(Session.getCurrentAcadSession(user).getUniqueId()));
        
        if ("Apply".equals(departmentListForm.getOp())) {
            UserData.setPropertyBoolean(webSession,"Departments.showUnusedDepts", departmentListForm.getShowUnusedDepts());
        } else {
            departmentListForm.setShowUnusedDepts(UserData.getPropertyBoolean(webSession, "Departments.showUnusedDepts", false));
        }

        if ("Export PDF".equals(request.getParameter("op"))) {
            boolean dispLastChanges = (!"no".equals(Settings.getSettingValue(user, Constants.SETTINGS_DISP_LAST_CHANGES)));
            
            PdfWebTable webTable = new PdfWebTable((dispLastChanges ? 10 : 9), "Department List - "+Web.getUser(webSession).getAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME),
                    "departmentList.do?ord=%%",
                    (dispLastChanges ? new String[] { "Number", "Abbv", "Name", "External\nManager", "Subjects", "Rooms",
                            "Status", "Dist Pref\nPriority", "Allow\nRequired", "Last\nChange" } 
                    : new String[] { "Number", "Abbreviation", "Name", "External\nManager", "Subjects", "Rooms", "Status",
                            "Dist Pref\nPriority", "Allow\nRequired" }),
                    new String[] { "left", "left", "left", "left", "right", "right", "left", "right", "left", "left" },
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
                    if (d.isAllowReqRoom() != null
                            && d.isAllowReqRoom().booleanValue()) {
                            if (d.isAllowReqTime() != null
                                    && d.isAllowReqTime().booleanValue()) {
                                allowReq = "both";
                                allowReqOrd = 3;
                            } else {
                                allowReq = "room";
                                allowReqOrd = 2;
                            }
                        } else if (d.isAllowReqTime() != null
                            && d.isAllowReqTime().booleanValue()) {
                            allowReq = "time";
                            allowReqOrd = 1;
                        }

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
                                allowReq, lastChangeStr },
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
                            lastChangeCmp });
                }
            }

            File file = ApplicationProperties.getTempFile("departments", "pdf");
            webTable.exportPdf(file, WebTable.getOrder(request.getSession(), "DepartmentList.ord"));
            request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }
        
		return mapping.findForward("showDepartmentList");
		
	}

}
