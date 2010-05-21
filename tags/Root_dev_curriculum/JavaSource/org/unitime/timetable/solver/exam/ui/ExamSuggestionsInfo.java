package org.unitime.timetable.solver.exam.ui;

import java.io.Serializable;
import java.util.TreeSet;
import java.util.Vector;

public class ExamSuggestionsInfo implements Serializable {
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
