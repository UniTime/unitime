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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.SponsoringOrgListForm;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Zuzana Mullerova
 */
@Service("/sponsoringOrgList")
public class SponsoringOrgListAction extends Action {
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		SponsoringOrgListForm myForm = (SponsoringOrgListForm) form;
		String op = myForm.getOp();
		
		sessionContext.checkPermission(Right.SponsoringOrganizations);
        
		if("Add Organization".equals(op)) {
			request.setAttribute("op", "add");
			return mapping.findForward("add");
		}
		
		request.setAttribute("table", getTable());
		
		return mapping.findForward("show");
	}
	
	public String getTable() {

		// Create new table
		WebTable table = new WebTable( 2,
		    	    null, 
		    	    new String[] {"Name", "Email"}, 
		    	    new String[] {"left", "left"}, 
		    	    new boolean[] {true, true});    
		
	    for (Iterator i=SponsoringOrganization.findAll().iterator();i.hasNext();) {
	        SponsoringOrganization spor = (SponsoringOrganization) i.next();
			table.addLine(
				(sessionContext.hasPermission(spor, Right.SponsoringOrganizationEdit) ? "onclick=\"document.location='sponsoringOrgEdit.do?op=Edit&id="+spor.getUniqueId()+"';\"" : null),
	        	new String[] {spor.getName(), spor.getEmail()},
	        	new Comparable[] {null, null});
		    }
		    
/*		    if ("Export PDF".equals(request.getParameter("op"))) {
		        File file = ApplicationProperties.getTempFile("itypes", "pdf");
		        webTable.exportPdf(file, PdfWebTable.getOrder(request.getSession(),"itypeDescList.ord"));
		        request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
		    }
		
		    String tblData = webTable.printTable(PdfWebTable.getOrder(request.getSession(),"itypeDescList.ord"));
*/
	    return (table.getLines().isEmpty()?"":table.printTable());
	}
	
}
