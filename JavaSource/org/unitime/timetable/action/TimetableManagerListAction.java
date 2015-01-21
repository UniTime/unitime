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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;
import org.unitime.timetable.webutil.TimetableManagerBuilder;


/**
 * MyEclipse Struts
 * Creation date: 04-11-2005
 *
 * XDoclet definition:
 * @struts:action
 * @struts:action-forward name="success" path="schedDeputyListTile" redirect="true"
 * @struts:action-forward name="fail" path="/error.jsp" redirect="true"
 *
 * @author Tomas Muller
 */
@Service("/timetableManagerList")
public class TimetableManagerListAction extends Action {
	
	@Autowired SessionContext sessionContext;

    // --------------------------------------------------------- Instance Variables

    // --------------------------------------------------------- Methods

    /**
     * Reads list of schedule deputies and assistants and displays them in the form of a HTML table
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        String errM = "";
        
        // Check permissions
        sessionContext.checkPermission(Right.TimetableManagers);

		WebTable.setOrder(sessionContext,"timetableManagerList.ord",request.getParameter("order"),1);
        
        PdfWebTable table =  new TimetableManagerBuilder().getManagersTable(sessionContext,true);
        int order = WebTable.getOrder(sessionContext,"timetableManagerList.ord");
        String tblData = (order>=1?table.printTable(order):table.printTable());
        
        if ("Export PDF".equals(request.getParameter("op"))) {
        	ExportUtils.exportPDF(
        			new TimetableManagerBuilder().getManagersTable(sessionContext,false),
        			order, response, "managers");
        	return null;
        }
            

        request.setAttribute("schedDeputyList", errM + tblData);
        return mapping.findForward("success");
    }
}
