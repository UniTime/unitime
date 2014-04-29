/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
*/
package org.unitime.timetable.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.commons.Debug;

public class Copyright extends BodyTagSupport {
	private static final long serialVersionUID = -4463420165596054395L;
	private boolean iBr = true;
	
	public void setBr(boolean br) { iBr = br; }
	public boolean isBr() { return iBr; }

	public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
    	// WARNING: Changing or removing the following copyright notice will violate the license terms.
    	// If you need a different licensing, please contact us at support@unitime.org
    	String body = 
    		"<a class='unitime-FooterLink' href='http://www.unitime.org' tabIndex='-1'>&copy;&nbsp;2008&nbsp;-&nbsp;2014&nbsp;UniTime&nbsp;LLC</a>," + 
    		(isBr() ? "<br>" : " ") + 
    		"<a class='unitime-FooterLink' href='http://www.unitime.org/uct_license.php' tabIndex='-1'>distributed&nbsp;under&nbsp;GNU&nbsp;General&nbsp;Public&nbsp;License.</a>";
        try {
            pageContext.getOut().print(body);
        }
        catch (Exception e) {
            Debug.error("Could not display copyright notice: " + e.getMessage());
        }
		return EVAL_PAGE;
    }

}
