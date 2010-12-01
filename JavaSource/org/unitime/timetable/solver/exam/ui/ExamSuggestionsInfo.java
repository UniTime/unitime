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
package org.unitime.timetable.solver.exam.ui;

import java.io.Serializable;
import java.util.TreeSet;
import java.util.Vector;

public class ExamSuggestionsInfo implements Serializable {
	private static final long serialVersionUID = 4755443302436317912L;
	private Vector<ExamProposedChange> iSuggestions = null;
    private String iMessage;
    private boolean iTimeoutReached;
    
    public ExamSuggestionsInfo(TreeSet<ExamProposedChange> suggestions, String message, boolean timeoutReached) {
        iSuggestions = new Vector(suggestions);
        iMessage = message;
        iTimeoutReached = timeoutReached;
    }
    
    public Vector<ExamProposedChange> getSuggestions() { return iSuggestions; }
    public String getMessage() { return iMessage; }
    public boolean getTimeoutReached() { return iTimeoutReached; }
}
