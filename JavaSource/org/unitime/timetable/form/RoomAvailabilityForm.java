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
package org.unitime.timetable.form;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.security.SessionContext;


/** 
 * @author Tomas Muller
 */
public class RoomAvailabilityForm extends ExamReportForm {
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	private static final long serialVersionUID = -7604226806875981047L;
	private String iFilter = null;
    private boolean iIncludeExams = false;
    private boolean iCompare = false;

	@Override
    public void validate(UniTimeAction action) {
		if (getExamType()<0) 
			action.addFieldError("examType", MSG.errorExamTypeIsRequired());
	}
	
    @Override
	public void reset() {
    	super.reset();
    	iIncludeExams = false;
	    iFilter = null;
	    iCompare = false;
	    setExamType(null);
    }

	public String getFilter() { return iFilter; }
	public void setFilter(String filter) { iFilter = filter; }
	public boolean getIncludeExams() { return iIncludeExams; }
	public void setIncludeExams(boolean exams) { iIncludeExams = exams; }
    public boolean getCompare() { return iCompare; }
    public void setCompare(boolean compare) { iCompare = compare; }

	public void load(SessionContext session) {
	    super.load(session);
        setFilter((String)session.getAttribute("RoomAvailability.Filter"));
        setIncludeExams(Boolean.TRUE.equals(session.getAttribute("RoomAvailability.Exams")));
        setCompare(Boolean.TRUE.equals(session.getAttribute("RoomAvailability.Compare")));
    }

    public void save(SessionContext session) {
        super.save(session);
        if (getFilter()==null)
            session.removeAttribute("RoomAvailability.Filter");
        else
            session.setAttribute("RoomAvailability.Filter", getFilter());
        session.setAttribute("RoomAvailability.Exams", getIncludeExams());
        session.setAttribute("RoomAvailability.Compare", getCompare());
    }
}