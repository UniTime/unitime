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
