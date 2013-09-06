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

import javax.servlet.jsp.tagext.TagSupport;

import org.unitime.timetable.model.Exam;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;


/**
 * @author Tomas Muller
 */
public class HasMidtermExams extends TagSupport {
	private static final long serialVersionUID = 7288871888129560846L;

    public SessionContext getSessionContext() {
    	return HttpSessionContext.getSessionContext(pageContext.getServletContext());
    }

    public boolean includeContent() {
        try {
            return getSessionContext().isAuthenticated() && Exam.hasMidtermExams(getSessionContext().getUser().getCurrentAcademicSessionId());
        } catch (Exception e) {}
        return false;
    }
    
	public int doStartTag() {
	    return includeContent()?EVAL_BODY_INCLUDE:SKIP_BODY;
	}
	
    public int doEndTag() {
        return EVAL_PAGE;
    }
}
