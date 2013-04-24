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
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
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
* @struts:action path="/subjectList" name="subjectListForm" input="/admin/subjectList.jsp" scope="request" validate="true"
*/
@Service("/subjectList")
public class SubjectListAction extends Action {
	
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

		sessionContext.checkPermission(Right.SubjectAreas);

		List<SubjectArea> subjects = SubjectArea.getSubjectAreaList(sessionContext.getUser().getCurrentAcademicSessionId());
		
		boolean dispLastChanges = CommonValues.Yes.eq(UserProperty.DisplayLastChanges.get(sessionContext.getUser()));
        
        if ("Export PDF".equals(request.getParameter("op"))) {
        	PdfWebTable webTable = new PdfWebTable((dispLastChanges?5:4),
                    "Subject Area List - " + sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel(),
                    "subjectList.do?ord=%%",
                    (dispLastChanges?
                        new String[] {"Abbv", "Title", "Department", "Managers", "Last Change"}:
                        new String[] {"Abbv", "Title", "Departmnet", "Managers"}),
                    new String[] {"left", "left","left","left", "right"},
                    new boolean[] {true, true, true, true, false} );
            for (SubjectArea s: subjects) {
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
                        s.getTitle(),
                        (d == null) ? "" : d.getDeptCode()+(d.getAbbreviation()==null?"":": "+d.getAbbreviation().trim()),
                        (sdName == null || sdName.trim().length()==0) ? "" : sdName,
                        lastChangeStr },
                    new Comparable[] { 
                        s.getSubjectAreaAbbreviation(),
                        s.getTitle(),
                        (d == null) ? "" : d.getDeptCode(),
                        sdName,
                        lastChangeCmp });
            }

            ExportUtils.exportPDF(webTable, WebTable.getOrder(sessionContext, "SubjectList.ord"), response, "subjects");
            return null;
        }
        
        WebTable webTable = new WebTable( 
    	    (dispLastChanges?5:4),
    	    "",
    	    "subjectList.do?ord=%%",
    	    (dispLastChanges?
    		    new String[] {"Abbv", "Title", "Department", "Managers", "Last Change"}:
    		    new String[] {"Abbv", "Title", "Department", "Managers"}),
    	    new String[] {"left", "left","left","left","right"},
    	    new boolean[] {true, true, true, true, false} );
        webTable.enableHR("#9CB0CE");
        webTable.setRowStyle("white-space: nowrap");
        WebTable.setOrder(sessionContext,"SubjectList.ord",request.getParameter("ord"),1);
        
    	for (SubjectArea s: subjects) {
        	Department d = s.getDepartment();
        	String sdName = "";
        	for (Iterator it = s.getManagers().iterator(); it.hasNext();) {
        		TimetableManager mgr = (TimetableManager) it.next();
        		if (sdName.length() > 0)
        			sdName = sdName + "<BR>";
        		sdName = sdName + mgr.getFirstName() + " " + mgr.getLastName();
        	}

        	String lastChangeStr = null;
        	Long lastChangeCmp = null;
        	if (dispLastChanges) {
        		List changes = ChangeLog.findLastNChanges(
        			d.getSession().getUniqueId(), null, null, d.getUniqueId(), 1);
        		ChangeLog lastChange = 
        			(changes == null || changes.isEmpty() ? null : (ChangeLog) changes.get(0));
        		lastChangeStr = 
        		(lastChange == null 
        			? "&nbsp;"
        			: "<span title='"
        				+ lastChange.getLabel()
        				+ "'>"
        				+ ChangeLog.sDFdate.format(lastChange
        						.getTimeStamp()) + " by "
        				+ lastChange.getManager().getShortName()
        				+ "</span>");
        		lastChangeCmp = new Long(
        			lastChange == null ? 0 : lastChange.getTimeStamp().getTime());
        	}

        	webTable.addLine(
        		"onClick=\"document.location.href='subjectAreaEdit.do?op=edit&id=" + s.getUniqueId() + "'\"",
        		new String[] { 
        			"<A name='" + s.getUniqueId() + "'>" + s.getSubjectAreaAbbreviation() + "</A>",
        			s.getTitle(),
        			(d == null) ? "&nbsp;" : "<span title='"+d.getHtmlTitle()+"'>"+
                                    d.getDeptCode()+(d.getAbbreviation()==null?"":": "+d.getAbbreviation().trim())+
                                    "</span>",
        			(sdName == null || sdName.trim().length()==0) ? "&nbsp;" : sdName,
        			lastChangeStr },
        		new Comparable[] { 
        			s.getSubjectAreaAbbreviation(),
        			s.getTitle(),
        			(d == null) ? "" : d.getDeptCode(),
        			sdName,
        			lastChangeCmp });
        }
    	
    	request.setAttribute("table", webTable.printTable(WebTable.getOrder(sessionContext, "SubjectList.ord")));
        
        return mapping.findForward("showSubjectList");
		
	}

}
