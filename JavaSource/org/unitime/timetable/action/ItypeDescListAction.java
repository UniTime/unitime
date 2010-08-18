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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * MyEclipse Struts
 * Creation date: 10-03-2005
 * 
 * XDoclet definition:
 * @struts:action validate="true"
 */
public class ItypeDescListAction extends Action {

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
	       String errM = "";
	        
	        // Check if user is logged in
		    HttpSession webSession = request.getSession();
	        if(!Web.isLoggedIn( webSession )) {
	            throw new Exception ("Access Denied.");
	        }

			// Create new table
		    PdfWebTable webTable = new PdfWebTable( 6,
		    	    null,
                    "itypeDescList.do?ord=%%",
		    	    new String[] {"IType", "Abbreviation", "Name", "Reference", "Type", "Parent", "Organized"},
		    	    new String[] {"left", "left","left","left", "left", "left", "center"},
		    	    new boolean[] {true, true, true, true, false, true, true} );
		    
	        PdfWebTable.setOrder(request.getSession(),"itypeDescList.ord",request.getParameter("ord"),1);

	        for (Iterator i=ItypeDesc.findAll(false).iterator();i.hasNext();) {
	            ItypeDesc itypeDesc = (ItypeDesc)i.next();
	            
	            // Add to web table
				webTable.addLine(
				        "onclick=\"document.location='itypeDescEdit.do?op=Edit&id="+itypeDesc.getItype()+"';\"",
			        	new String[] {itypeDesc.getItype().toString(),
			        					itypeDesc.getAbbv(), 
			        					itypeDesc.getDesc(),
			        					(itypeDesc.getSis_ref()==null?"":itypeDesc.getSis_ref()),
			        					itypeDesc.getBasicType(),
			        					(itypeDesc.getParent()==null?"":itypeDesc.getParent().getDesc()),
			        					(itypeDesc.isOrganized()?"yes":"no")},
			        	new Comparable[] {itypeDesc.getItype(),
				                        itypeDesc.getAbbv(),
				                        itypeDesc.getDesc(),
				                        (itypeDesc.getSis_ref()==null?"":itypeDesc.getSis_ref()),
				                        itypeDesc.getBasic(),
				                        (itypeDesc.getParent()==null?new Integer(-1):itypeDesc.getParent().getItype()),
				                        (itypeDesc.isOrganized()?0:1)});
	        }
	        
	        if ("Export PDF".equals(request.getParameter("op"))) {
	            File file = ApplicationProperties.getTempFile("itypes", "pdf");
	            webTable.exportPdf(file, PdfWebTable.getOrder(request.getSession(),"itypeDescList.ord"));
	            request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
	        }

	        String tblData = webTable.printTable(PdfWebTable.getOrder(request.getSession(),"itypeDescList.ord"));
	        request.setAttribute("itypeDescList", errM + tblData);
	        return mapping.findForward("success");

	}

}

