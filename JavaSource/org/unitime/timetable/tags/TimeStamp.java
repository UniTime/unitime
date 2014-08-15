/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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

import java.io.IOException;
import java.text.NumberFormat;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.cpsolver.ifs.util.JProf;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtMessages;

/**
 * @author Tomas Muller
 */
public class TimeStamp extends BodyTagSupport {
	private static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static final long serialVersionUID = -1l;

	public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
    	try {
        	Double startTime = (Double)pageContext.getRequest().getAttribute("TimeStamp");
        	if (startTime != null) {
        		double endTime = JProf.currentTimeSec();
        		double diff = endTime - startTime;
        		NumberFormat nf = NumberFormat.getInstance();
        		nf.setMaximumFractionDigits(2);
        		pageContext.getOut().print(MESSAGES.pageGeneratedIn(nf.format(diff)));
        	}
    	} catch (IOException e) {}
		return EVAL_PAGE;
    }

}
