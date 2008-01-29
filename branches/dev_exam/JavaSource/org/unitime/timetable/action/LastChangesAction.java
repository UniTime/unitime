/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.LastChangesForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
public class LastChangesAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LastChangesForm myForm = (LastChangesForm) form;
		
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )
               || !Web.hasRole(request.getSession(), Roles.getAdminRoles()) ) {
            throw new Exception ("Access Denied.");
        }
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if ("Apply".equals(op) || "Export PDF".equals(op)) {
        	myForm.save(request);
        } else {
            myForm.load(request);
        }
        
        Session session = Session.getCurrentAcadSession(Web.getUser(request.getSession()));
        
        request.setAttribute("departments",Department.findAll(session.getUniqueId()));
        request.setAttribute("subjAreas",new TreeSet(SubjectArea.getSubjectAreaList(session.getUniqueId())));
        request.setAttribute("managers",TimetableManager.getManagerList());
        
        WebTable.setOrder(request.getSession(),"lastChanges.ord2",request.getParameter("ord"),1);
        
        WebTable webTable = new WebTable( 7, "Last Changes",
                "lastChanges.do?ord=%%",
                new String[] {"Date", "Department", "Subject", "Manager", "Page", "Object", "Operation"},
                new String[] {"left", "left", "left", "left", "left", "left", "left"},
                new boolean[] { false, true, true, true, true, true, true} );
        
        List changes = ChangeLog.findLastNChanges(
                session.getUniqueId(), 
                (myForm.getManagerId()==null || myForm.getManagerId().longValue()<0?null:myForm.getManagerId()), 
                (myForm.getSubjAreaId()==null || myForm.getSubjAreaId().longValue()<0?null:myForm.getSubjAreaId()),
                (myForm.getDepartmentId()==null || myForm.getDepartmentId().longValue()<0?null:myForm.getDepartmentId()),
                myForm.getN());
        
        if (changes!=null) {
            for (Iterator i=changes.iterator();i.hasNext();)
                printLastChangeTableRow(request, webTable, (ChangeLog)i.next(), true);
        }
        
        request.setAttribute("table", webTable.printTable(WebTable.getOrder(request.getSession(),"lastChanges.ord2")));
        
        if ("Export PDF".equals(op) && changes!=null) {
            PdfWebTable pdfTable = new PdfWebTable( 7, "Last Changes",
                    "lastChanges.do?ord=%%",
                    new String[] {"Date", "Department", "Subject", "Manager", "Page", "Object", "Operation"},
                    new String[] {"left", "left", "left", "left", "left", "left", "left"},
                    new boolean[] { false, true, true, true, true, true, true} );
            for (Iterator i=changes.iterator();i.hasNext();)
                printLastChangeTableRow(request, pdfTable, (ChangeLog)i.next(), false);
            File file = ApplicationProperties.getTempFile("lastChanges", "pdf");
            pdfTable.exportPdf(file, WebTable.getOrder(request.getSession(),"lastChanges.ord2"));
            if (file!=null) request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }
        
        return mapping.findForward("display");
	}
    
    private int printLastChangeTableRow(HttpServletRequest request, WebTable webTable, ChangeLog lastChange, boolean html) {
        if (lastChange==null) return 0;
        webTable.addLine(null,
                new String[] {
                    ChangeLog.sDF.format(lastChange.getTimeStamp()),
                    (lastChange.getDepartment()==null?"":
                        (html?
                                "<span title='"+lastChange.getDepartment().getHtmlTitle()+"'>"+
                                lastChange.getDepartment().getShortLabel()+
                                "</span>":lastChange.getDepartment().getShortLabel())),
                    (lastChange.getSubjectArea()==null?"":lastChange.getSubjectArea().getSubjectAreaAbbreviation()),
                    (html?lastChange.getManager().getShortName():lastChange.getManager().getShortName().replaceAll("&nbsp;"," ")),
                    lastChange.getSourceTitle(request),
                    lastChange.getObjectTitle(),
                    lastChange.getOperationTitle(request)
                    },
                new Comparable[] {
                    new Long(lastChange.getTimeStamp().getTime()),
                    (lastChange.getDepartment()==null?"":lastChange.getDepartment().getDeptCode()),
                    (lastChange.getSubjectArea()==null?"":lastChange.getSubjectArea().getSubjectAreaAbbreviation()),
                    lastChange.getManager().getName(),
                    lastChange.getSourceTitle(request), //new Integer(lastChange.getSource().ordinal()),
                    lastChange.getObjectTitle(),
                    new Integer(lastChange.getOperation().ordinal())
                    });
        return 1;
    }
    
}

