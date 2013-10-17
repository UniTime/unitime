/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.webutil.JavascriptFunctions;

/**
 * @author Tomas Muller
 */
public class Confirm extends BodyTagSupport {
	private static final long serialVersionUID = -7277573383061072409L;
	private String iName = null;

    public String getName() { return iName; }
    public void setName(String name) { iName = name; }
    
        
    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
    
    public SessionContext getSessionContext() {
    	return HttpSessionContext.getSessionContext(pageContext.getServletContext());
    }
    
    public int doEndTag() throws JspException {
        try {
            String body = (getBodyContent()==null?null:getBodyContent().getString());
            if (body==null || body.trim().length()==0) body = "Are you sure?";
            pageContext.getOut().println("<SCRIPT language='javascript'>");
            pageContext.getOut().println("<!--");
            pageContext.getOut().println("function "+getName()+"() {");
            if (JavascriptFunctions.isJsConfirm(getSessionContext())) {
                pageContext.getOut().println("return confirm(\""+body+"\");");
            } else {
                pageContext.getOut().println("return true;");
            }
            pageContext.getOut().println("}");
            pageContext.getOut().println("// -->");
            pageContext.getOut().println("</SCRIPT>");
        } catch (Exception e) {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }
        return EVAL_PAGE;
    }   

}
