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
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * MyEclipse Struts
 * Creation date: 10-03-2005
 * 
 * XDoclet definition:
 * @struts:action validate="true"
 *
 * @author Tomas Muller
 */
@Service("/itypeDescList")
public class ItypeDescListAction extends Action {
	
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
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
	       String errM = "";
	        
	        // Check if user is logged in
	       sessionContext.checkPermission(Right.InstructionalTypes);

			// Create new table
		    PdfWebTable webTable = new PdfWebTable( 6,
		    	    null,
                    "itypeDescList.do?ord=%%",
		    	    new String[] {"IType", "Abbreviation", "Name", "Reference", "Type", "Parent", "Organized"},
		    	    new String[] {"left", "left","left","left", "left", "left", "center"},
		    	    new boolean[] {true, true, true, true, false, true, true} );
		    
	        PdfWebTable.setOrder(sessionContext,"itypeDescList.ord",request.getParameter("ord"),1);

	        for (Iterator i=ItypeDesc.findAll(false).iterator();i.hasNext();) {
	            ItypeDesc itypeDesc = (ItypeDesc)i.next();
	            
	            // Add to web table
				webTable.addLine(
						sessionContext.hasPermission(itypeDesc, Right.InstructionalTypeEdit) ?  "onclick=\"document.location='itypeDescEdit.do?op=Edit&id="+itypeDesc.getItype()+"';\"" : null,
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
	        	ExportUtils.exportPDF(
	        			webTable,
	        			PdfWebTable.getOrder(sessionContext,"itypeDescList.ord"),
	        			response, "itypes");
	        	return null;
	        }

	        String tblData = webTable.printTable(PdfWebTable.getOrder(sessionContext,"itypeDescList.ord"));
	        request.setAttribute("itypeDescList", errM + tblData);
	        return mapping.findForward("success");

	}

}

