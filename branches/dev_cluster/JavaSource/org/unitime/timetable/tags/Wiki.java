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
package org.unitime.timetable.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;


/**
 * @author Tomas Muller
 */
public class Wiki extends BodyTagSupport {
	private static final long serialVersionUID = 1018397983833478965L;
	private String iWikiUrl = null;
    private boolean iEnabled = false;
    
    public Wiki() {
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
            String img = ((HttpServletRequest)pageContext.getRequest()).getContextPath()+"/images/help_icon.gif";
            String url = getWikiUrl() + pageName.trim().replace(' ', '_');
            html.append("<img border='0' align='top' src='"+img+"' alt='"+pageName.trim()+" Help' " +
                    "onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" " +
                    "onClick=\"showGwtDialog('" + pageName.trim() + " Help', '" + url +"', '75%', '75%');\" " +
                    "title='"+pageName.trim()+" Help'>");
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
