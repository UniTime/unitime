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
 * @author Zuzana Mullerova, Tomas Muller
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
