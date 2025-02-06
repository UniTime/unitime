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
package org.unitime.timetable.action.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.convention.annotation.Action;
import org.unitime.commons.Debug;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.model.ItypeDesc;

/**
 * 
 * @author Tomas Muller
 *
 */
@Action(value = "itypesAjax")
public class ITypesAjax extends UniTimeAction<BlankForm> {
    private static final long serialVersionUID = -9199694181125878650L;

	public String execute() throws Exception {
        
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
            boolean basic = "true".equals(request.getParameter("basic"));
            for (Iterator i=ItypeDesc.findAll(basic).iterator();i.hasNext();) {
                ItypeDesc itype = (ItypeDesc)i.next();
                print(out, itype.getItype().toString(), itype.getDesc());
            }
        } catch (Exception e) {
            Debug.error(e);
        }
    }
}
