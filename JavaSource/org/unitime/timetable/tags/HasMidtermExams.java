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
