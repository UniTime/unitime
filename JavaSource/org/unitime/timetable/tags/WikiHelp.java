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
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;


/**
 * @author Tomas Muller
 */
public class WikiHelp extends BodyTagSupport {
	private static final long serialVersionUID = -8159352006636444479L;
	private String iWikiUrl = null;
    private boolean iEnabled = false;
    
    public WikiHelp() {
        super();
        iEnabled = ApplicationProperty.PageHelpEnabled.isTrue();
        iWikiUrl = ApplicationProperty.PageHelpUrl.value();
    }

    public void setWikiUrl(String wikiUrl) {
        iWikiUrl = wikiUrl;
    }
    public String getWikiUrl() {
        return iWikiUrl;
    }

    public boolean isEnabled() {
        return iEnabled;
    }
    public void setEnabled(boolean enabled) {
        iEnabled = enabled;
    }
    

    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
    
    public int doEndTag() throws JspException {
        String pageName = (getBodyContent()==null?null:getBodyContent().getString());
        if (pageName==null || pageName.trim().length()==0)
            return EVAL_PAGE;

        StringBuffer html = new StringBuffer();
        
        if (isEnabled() && getWikiUrl()!=null) {
            String url = getWikiUrl() + pageName.trim().replace(' ', '_');
            html.append("<a "+
                    "onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" " +
                    "onClick=\"showGwtDialog('" + pageName.trim() + " Help', '" + url +"', '75%', '75%');\" " +
                    "title='"+pageName.trim()+" Help'>");
            html.append("<span class='PageTitle'>");
            html.append(pageName.trim());
            html.append("</span>");
            html.append("<img border='0' align='top' src='images/help.png' alt='"+pageName.trim()+" Help'>");
            html.append("</a>");
            html.append("&nbsp;");
        } else {
            html.append("<span class='PageTitle'>");
            html.append(pageName.trim());
            html.append("&nbsp;");
            html.append("</span>");
        }
        
        try {
            pageContext.getOut().print(html.toString());
        }
        catch (Exception e) {
            Debug.error("Could not display wiki help: " + e.getMessage());
        }

        return EVAL_PAGE;
    }
    
}
