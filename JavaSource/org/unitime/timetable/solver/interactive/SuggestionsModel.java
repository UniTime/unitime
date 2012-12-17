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
package org.unitime.timetable.solver.interactive;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.solver.SolverProxy;


/**
 * @author Tomas Muller
 */
public class SuggestionsModel implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String sFilters[] = new String[] {"No Restrictions","Same Time","Same Room"};
	public static final int sFilterNoFilter = 0;
	public static final int sFilterSameTime = 1;
	public static final int sFilterSameRoom = 2;
	private boolean iCanAllowBreakHard = false;
	private boolean iAllowBreakHard = false;
	private boolean iDisplayCBS = true;
	private boolean iAllTheSame = true;
	private int iDepth = 2;
	private long iTimeOut = 5000;
	private int iFilter = sFilterNoFilter;
    private boolean iCompute = true;
    private boolean iDisplayFilter = false;
    private boolean iDisplayPlacements = false;
    private int iLimit = 20;
	private String iFilterText = null;
	private boolean iSimpleMode = true;
	private boolean iReversedMode = false;
    private int iMinRoomSize = -1;
    private int iMaxRoomSize = -1;
    private boolean iDisplaySuggestions = true;
    private boolean iDisplayConfTable = false;
    
    public Vector iHints = new Vector();
    public Long iClassId = null;

    private transient TreeSet iSuggestions = null;
	private transient Suggestion iCurrentSuggestion = null;
    private transient boolean iTimeoutReached = false;
    private transient long iNrCombinationsConsidered = 0;
    private transient long iNrSolutions = 0;
    private transient long iNrTries = 0;
    private transient TreeSet iTryAssignments = null;
    private transient Suggestion iSelectedSuggestion = null;
    private transient Suggestion iEmptySuggestion = null;
    private transient Vector iConfTable = null;

    public SuggestionsModel() {
	}

    public void reset(SolverProxy solver) throws Exception { 
        iCanAllowBreakHard = getCanAllowBreakHard(solver);
        if (!iCanAllowBreakHard) iAllowBreakHard = false;
        iSelectedSuggestion = null;
        iDepth = 2; 
        iTimeOut = 5000;
        iCompute = true;
        iHints.clear();
        iDisplayFilter = canDisplayFilter(solver);
        iFilter = sFilterNoFilter;
        iFilterText = null;
        iMinRoomSize = -1;
        iMaxRoomSize = -1;
    }
    
    public boolean canDisplayFilter() { return iDisplayFilter; }
    public boolean isComputed() { return !iCompute; }
    public void recompute() { iCompute = true; }

    public void setFilter(int filter) { 
        if (iFilter != filter) {
            iFilter = filter; 
            iCompute = true;
        }
    }
    public int getFilter() { return iFilter; }
    public void setAllTheSame(boolean allTheSame) { 
        if (iAllTheSame != allTheSame) {
            iAllTheSame = allTheSame;
            iCompute = true;
        }
    }
    public boolean isAllTheSame() { return iAllTheSame; }
    public void setDepth(int depth) { 
        if (iDepth!=depth) {
            iDepth = depth; 
            iCompute = true;
        }
    }
    public int getDepth() { return iDepth; }
    public void incDepth() { 
        iDepth++;
        iCompute = true;
    }
    public long getTimeout() { return iTimeOut; }
    public void setTimeout(long timeout) { 
        if (iTimeOut!=timeout) {
            iTimeOut = timeout; 
            iCompute = true;
        }
    }
    public void doubleTimeout() { 
        iTimeOut *= 2; iCompute = true;
    }    
	
	public boolean getCanAllowBreakHard() { return iCanAllowBreakHard; }
	public void setAllowBreakHard(boolean allowBreakHard) {
		if (iAllowBreakHard!=allowBreakHard) {
			iAllowBreakHard = allowBreakHard;
			iCompute = true;
		}
	}
	public boolean getAllowBreakHard() { return iAllowBreakHard; }
	public boolean getDisplayCBS() { return iDisplayCBS; }
	public void setDisplayCBS(boolean displayCBS) { iDisplayCBS = displayCBS; }
	public boolean getDisplayPlacements() { return iDisplayPlacements; }
	public void setDisplayPlacements(boolean displayPlacements) { 
		iDisplayPlacements = displayPlacements;
		iCompute = true;
	}
	public int getLimit() { return iLimit; }
	public void setLimit(int limit) { iLimit = limit; iCompute = true; }
	public boolean getSimpleMode() { return iSimpleMode; }
	public void setSimpleMode(boolean simpleMode) { iSimpleMode = simpleMode; }
	public boolean getReversedMode() { return iReversedMode; }
	public void setReversedMode(boolean reversedMode) { iReversedMode = reversedMode; }
	public String getFilterText() { return iFilterText; }
	public void setFilterText(String text) { iFilterText = text; iCompute=true; }
	
	public int getMinRoomSize() { return iMinRoomSize; }
	public int getMaxRoomSize() { return iMaxRoomSize; }
	public void setMinRoomSize(int minRoomSize) { iMinRoomSize = minRoomSize; }
	public void setMaxRoomSize(int maxRoomSize) { iMaxRoomSize = maxRoomSize; }
	public boolean getDisplayConfTable() { return iDisplayConfTable; }
	public void setDisplayConfTable(boolean displayConfTable) { iDisplayConfTable = displayConfTable; }
	public boolean getDisplaySuggestions() { return iDisplaySuggestions; }
	public void setDisplaySuggestions(boolean displaySuggestions) { iDisplaySuggestions = displaySuggestions; }

	public void setClassId(Long classId) {
		if (!classId.equals(iClassId)) {
			iClassId = classId;
			iCompute = true;
		}
	}
	public Long getClassId() { return iClassId; }
	
    public Vector getHints() { return iHints; }
    public void addHint(Long classId, int days, int startSlots, List<Long> roomIds, Long patternId, Long datePatternId) {
    	Hint hint = new Hint(classId, days, startSlots, roomIds, patternId, datePatternId);
        Hint prev = null;
        for (Enumeration e=iHints.elements();e.hasMoreElements();) {
            Hint h = (Hint)e.nextElement();
            if (h.getClassId().equals(hint.getClassId())) {
                prev = h; break;
            }
        }
        if (prev!=null) iHints.remove(prev);
        iHints.add(hint);
        iSelectedSuggestion = null;
        iCompute = true;
    }
    public void remHint(Long classId) {
        Hint rem = null;
        for (Enumeration e=iHints.elements();e.hasMoreElements();) {
            Hint h = (Hint)e.nextElement();
            if (h.getClassId().equals(classId)) {
            	rem = h; break;
            }
        }
        if (rem!=null) {
        	iHints.remove(rem);
            iSelectedSuggestion = null;
            iCompute = true;
        }
    }
	
	public boolean getCanAllowBreakHard(SolverProxy solver) throws Exception {
		return (solver == null ? false : solver.getProperties().getPropertyBoolean("General.InteractiveMode", false));
	}
    public boolean canDisplayFilter(SolverProxy solver) { return solver != null;}
	
    public Suggestion getSelectedSuggestion() { return iSelectedSuggestion; }
    public void selectSuggestion(int ord) {
    	iSelectedSuggestion = (Suggestion)iSuggestions.toArray()[ord];
    }
    public void selectPlacement(int ord) {
    	Hint hint = ((Suggestion)iTryAssignments.toArray()[ord]).getHint();
    	if (hint!=null)
    		addHint(hint.getClassId(),hint.getDays(),hint.getStartSlot(),hint.getRoomIds(),hint.getPatternId(),hint.getDatePatternId());
    }
    public Suggestion getEmptySuggestion() { return iEmptySuggestion; }
    
    public boolean compute(SolverProxy solver) throws Exception {
    	if (iCompute==false) return true;
    	if (solver==null) return false;
    	Suggestions suggestions = solver.getSuggestions(this);
    	if (suggestions == null) return false;
    	//solver.getSuggestions(iDepth,iTimeOut,iAllTheSame,iFilter==sFilterSameTime,iFilter==sFilterSameRoom,iAllowBreakHard,iClassId,iHints,iDisplayPlacements,iLimit,iFilterText,iMinRoomSize,iMaxRoomSize,iDisplaySuggestions,iDisplayConfTable);
   		iSuggestions = suggestions.getSuggestions();
   		iTimeoutReached = suggestions.getTimeoutReached();
   		iNrCombinationsConsidered = suggestions.getNrCombinationsConsidered();
   		iNrSolutions = suggestions.getNrSolutions();
   		iCurrentSuggestion = suggestions.currentSuggestion();
   		iTryAssignments = suggestions.tryAllAssignments();
   		iNrTries = suggestions.getNrTries();
   		iEmptySuggestion = suggestions.emptySuggestion();
    	iHints = suggestions.getHints(); //hints with added infos (remote server)
    	iCompute = false;
    	iConfTable = suggestions.getConfTable();
    	return true;
    }
    
    public TreeSet getSuggestions() { return iSuggestions; }
    public Suggestion getCurrentSuggestion() { return iCurrentSuggestion; }
    public TreeSet getTryAssignments() { return iTryAssignments; }
    public boolean getTimeoutReached() { return iTimeoutReached; }
    public long getNrCombinationsConsidered() { return iNrCombinationsConsidered; }
    public long getNrSolutions() { return iNrSolutions; }
    public long getNrTries() { return iNrTries; }
    public Vector getConfTable() { return iConfTable; }
    
    public void save(UserContext user) {
    	user.setProperty("SuggestionsModel.allowBreakHard", getAllowBreakHard() ? "1" : "0");
    	user.setProperty("SuggestionsModel.displayCBS", getDisplayCBS() ? "1" : "0");
    	user.setProperty("SuggestionsModel.displayPlacements", getDisplayPlacements() ? "1" : "0");
    	user.setProperty("SuggestionsModel.simpleMode", getSimpleMode() ? "1" : "0");
    	user.setProperty("SuggestionsModel.limit", String.valueOf(getLimit()));
    	user.setProperty("SuggestionsModel.displayConfTable", getDisplayConfTable() ? "1" : "0");
    	user.setProperty("SuggestionsModel.displaySuggestions",getDisplaySuggestions() ? "1" : "0");
    	user.setProperty("SuggestionsModel.reversedMode", getReversedMode() ? "1" : "0");
    }

    public void load(UserContext context) {
    	setAllowBreakHard(getCanAllowBreakHard() && "1".equals(context.getProperty("SuggestionsModel.allowBreakHard", getAllowBreakHard() ? "1" : "0")));
    	setDisplayCBS("1".equals(context.getProperty("SuggestionsModel.displayCBS", getDisplayCBS() ? "1" : "0")));
    	setDisplayPlacements("1".equals(context.getProperty("SuggestionsModel.displayPlacements", getDisplayPlacements() ? "1" : "0")));
    	setSimpleMode("1".equals(context.getProperty("SuggestionsModel.simpleMode", getSimpleMode() ? "1" : "0")));
    	setLimit(Integer.parseInt(context.getProperty("SuggestionsModel.limit", String.valueOf(getLimit()))));
    	setDisplayConfTable("1".equals(context.getProperty("SuggestionsModel.displayConfTable", getDisplayConfTable() ? "1" : "0")));
    	setDisplaySuggestions("1".equals(context.getProperty("SuggestionsModel.displaySuggestions",getDisplaySuggestions() ? "1" : "0")));
    	setReversedMode("1".equals(context.getProperty("SuggestionsModel.reversedMode", getReversedMode()? "1" : "0")));
    }
    
    public String toString() {
        return "SuggestionModel{\n"+
            "  canAllowBreakHard = "+iCanAllowBreakHard+"\n"+
            "  allowBreakHard = "+iAllowBreakHard+"\n"+
            "  displayCBS = "+iDisplayCBS+"\n"+
            "  allTheSame = "+iAllTheSame+"\n"+
            "  depth = "+iDepth+"\n"+
            "  timeOut = "+iTimeOut+"\n"+
            "  filter = "+iFilter+"\n"+
            "  compute = "+iCompute+"\n"+
            "  displayFilter = "+iDisplayFilter+"\n"+
            "  displayPlacements = "+iDisplayPlacements+"\n"+
            "  limit = "+iLimit+"\n"+
            "  filterText = "+iFilterText+"\n"+
            "  simpleMode = "+iSimpleMode+"\n"+
            "  reversedMode = "+iReversedMode+"\n"+
            "  minRoomSize = "+iMinRoomSize+"\n"+
            "  maxRoomSize = "+iMaxRoomSize+"\n"+
            "  displaySuggestions = "+iDisplaySuggestions+"\n"+
            "  displayConfTable = "+iDisplayConfTable+"\n"+
            "  hints = "+iHints+"\n"+
            "  classId = "+iClassId+"\n"+
            "  suggestions = "+iSuggestions+"\n"+
            "  currentSuggestion = "+iCurrentSuggestion+"\n"+
            "  timeoutReached = "+iTimeoutReached+"\n"+
            "  nrCombinationsConsidered = "+iNrCombinationsConsidered+"\n"+
            "  nrSolutions = "+iNrSolutions+"\n"+
            "  nrTries = "+iNrTries+"\n"+
            "  tryAssignments = "+iTryAssignments+"\n"+
            "  selectedSuggestion = "+iSelectedSuggestion+"\n"+
            "  emptySuggestion = "+iEmptySuggestion+"\n"+
            "  confTable = "+iConfTable+"\n"+
            "}";
    }
}
