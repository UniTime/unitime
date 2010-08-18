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
package org.unitime.timetable.action.ajax;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.dao.BuildingDAO;

/**
 * 
 * @author Tomas Muller
 *
 */
public class BuildingCoordsAjax extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        response.addHeader("Content-Type", "text/xml");
        
        ServletOutputStream out = response.getOutputStream();
        
        out.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n");
        out.print("<results>");
        computeResponse(request, out);
        out.print("</results>");
        
        return null;        

    }
    
    protected void print(ServletOutputStream out, String id, String value) throws IOException {
        out.print("<result id=\""+id+"\" value=\""+value+"\" />");
    }
    
    protected void computeResponse(HttpServletRequest request, ServletOutputStream out) throws Exception {
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
