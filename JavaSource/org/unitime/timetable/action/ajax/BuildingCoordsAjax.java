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
package org.unitime.timetable.action.ajax;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.dao.BuildingDAO;

/**
 * 
 * @author Tomas Muller
 *
 */
@Service("/buildingCoordsAjax")
public class BuildingCoordsAjax extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        response.addHeader("Content-Type", "text/xml; charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        out.print("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
        out.print("<results>");
        computeResponse(request, out);
        out.print("</results>");
        
        return null;        

    }
    
    protected void print(PrintWriter out, String id, String value) throws IOException {
        out.print("<result id=\""+id+"\" value=\""+value+"\" />");
    }
    
    protected void computeResponse(HttpServletRequest request, PrintWriter out) throws Exception {
        try {
            Building building = new BuildingDAO().get(Long.valueOf(request.getParameter("id")));
            print(out, "x", 
                    (building.getCoordinateX()==null ? "" : building.getCoordinateX().toString()));
            print(out, "y", 
                    (building.getCoordinateY()==null ? "" : building.getCoordinateY().toString()));
        } catch (Exception e) {
            Debug.error(e);
            print(out, "x", ""); print(out, "y", "");
        }
    }
}
