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

import javax.servlet.jsp.tagext.TagSupport;

import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;


/**
 * @author Tomas Muller
 */
public class CanSeeExams extends TagSupport {
	
    public int doStartTag() {
        try {
            User user = Web.getUser(pageContext.getSession());
            TimetableManager manager = TimetableManager.getManager(user);
            Session session = Session.getCurrentAcadSession(user);
            if (manager.canSeeExams(session, user))
                return EVAL_BODY_INCLUDE;
        } catch (Exception e) {}
        return SKIP_BODY;
    }
    
    public int doEndTag() {
        return EVAL_PAGE;
    }
}
