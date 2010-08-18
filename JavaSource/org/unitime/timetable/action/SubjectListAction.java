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
import org.unitime.timetable.form.SubjectListForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
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
* @struts:action path="/subjectList" name="subjectListForm" input="/admin/subjectList.jsp" scope="request" validate="true"
*/
public class SubjectListAction extends Action {

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
	    SubjectListForm subjectListForm = (SubjectListForm) form;
		subjectListForm.setSubjects(SubjectArea.getSubjectAreaList(Session.getCurrentAcadSession(user).getUniqueId()));
		
        if ("Export PDF".equals(request.getParameter("op"))) {
            boolean dispLastChanges = (!"no".equals(Settings.getSettingValue(user, Constants.SETTINGS_DISP_LAST_CHANGES)));
            
            PdfWebTable webTable = new PdfWebTable((dispLastChanges?7:6),
                    "Subject Area List - "+user.getAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME),
                    "subjectList.do?ord=%%",
                    (dispLastChanges?
                        new String[] {"Abbv", "Title", "Department", "Managers", "Sched Book\nOnly", "Pseudo","Last Change"}:
                        new String[] {"Abbv", "Title", "Departmnet", "Managers", "Sched Book\nOnly", "Pseudo"}),
                    new String[] {"left", "left","left","left","left","left","right"},
                    new boolean[] {true, true, true, true, true, true, false} );
            for (Iterator i=subjectListForm.getSubjects().iterator();i.hasNext();) {
                SubjectArea s = (SubjectArea) i.next();
                Department d = s.getDepartment();
                String sdName = "";
                for (Iterator it = s.getManagers().iterator(); it.hasNext();) {
                    TimetableManager mgr = (TimetableManager) it.next();
                    if (sdName.length() > 0)
                        sdName = sdName + "\n";
                    sdName = sdName + mgr.getFirstName() + " " + mgr.getLastName();
                }

                String lastChangeStr = null;
                Long lastChangeCmp = null;
                if (dispLastChanges) {
                    List changes = ChangeLog.findLastNChanges(d.getSession().getUniqueId(), null, null, d.getUniqueId(), 1);
                    ChangeLog lastChange =  (changes == null || changes.isEmpty() ? null : (ChangeLog) changes.get(0));
                    lastChangeStr =  (lastChange == null ? "" : ChangeLog.sDFdate.format(lastChange.getTimeStamp()) + " by " + lastChange.getManager().getShortName());
                    lastChangeCmp = new Long( lastChange == null ? 0 : lastChange.getTimeStamp().getTime());
                }

                webTable.addLine(
                    null,
                    new String[] { 
                        s.getSubjectAreaAbbreviation(),
                        s.getLongTitle(),
                        (d == null) ? "" : d.getDeptCode()+(d.getAbbreviation()==null?"":": "+d.getAbbreviation().trim()),
                        (sdName == null || sdName.trim().length()==0) ? "" : sdName,
                        s.isScheduleBookOnly().booleanValue() ? "Yes":"No",
                        s.isPseudoSubjectArea().booleanValue() ? "Yes":"No", 
                        lastChangeStr },
                    new Comparable[] { 
                        s.getSubjectAreaAbbreviation(),
                        s.getLongTitle(),
                        (d == null) ? "" : d.getDeptCode(),
                        sdName,
                        s.isScheduleBookOnly().toString(),
                        s.isPseudoSubjectArea().toString(),
                        lastChangeCmp });
            }

            File file = ApplicationProperties.getTempFile("subjects", "pdf");
            webTable.exportPdf(file, WebTable.getOrder(request.getSession(), "SubjectList.ord"));
            request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }
        
        return mapping.findForward("showSubjectList");
		
	}

}
