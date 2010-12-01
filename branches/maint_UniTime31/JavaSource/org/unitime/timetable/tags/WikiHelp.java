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
package org.unitime.timetable.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;


/**
 * @author Tomas Muller
 */
public class WikiHelp extends BodyTagSupport {
    private String iWikiUrl = null;
    private boolean iEnabled = false;
    
    public WikiHelp() {
        super();
        iEnabled = "true".equals(ApplicationProperties.getProperty("tmtbl.wiki.help",(iEnabled?"true":"false")));
        iWikiUrl = (String)ApplicationProperties.getProperty("tmtbl.wiki.url",iWikiUrl);
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
                    "onClick=\"var w = window.open('"+url+"','wikiHelp','width=1000,height=600,resizable=yes,scrollbars=yes,toolbar=yes,location=yes,directories=no,status=yes,menubar=no,copyhistory=no'); w.focus();\" " +
                    "title='"+pageName.trim()+" Help'>");
            html.append("<span class='PageTitle'>");
            html.append(pageName.trim());
            html.append("</span>");
            html.append("<img border='0' align='top' src='images/help_icon.gif' alt='"+pageName.trim()+" Help'>");
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
