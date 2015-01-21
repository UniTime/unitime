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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.solver.interactive.SuggestionsModel;


/** 
 * @author Tomas Muller
 */
public class SuggestionsForm extends ActionForm {
	private static final long serialVersionUID = 3222409910704167703L;
	private String iOp = null;
	private String iFilter = null;
	private boolean iShowFilter = false;
	private boolean iCanAllowBreakHard = false;
	private boolean iAllowBreakHard = false;
	private boolean iDisplayCBS = false;
	private boolean iDisplayPlacements = false;
	private boolean iSimpleMode = false;
	private int iLimit = 100;
	private int iDepth = 2;
	private long iTimeout = 5;
	private Long iId = null;
	private String iFilterText = null;
    private boolean iTimeoutReached = false;
    private long iNrCombinationsConsidered = 0;
    private long iNrSolutions = 0;
    private long iNrSuggestions = 0;
    private int iMinRoomSize = -1;
    private int iMaxRoomSize = -1;
    private boolean iDisplaySuggestions = false;
    private boolean iDisplayConfTable = false;
    private boolean iCanUnassign = false;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null; iFilter = SuggestionsModel.sFilters[0]; iAllowBreakHard = false; iDisplayCBS = false; iCanAllowBreakHard = false; iShowFilter = false; iId = null;
		iTimeoutReached=false; iNrCombinationsConsidered=0; iNrSolutions=0; iNrSuggestions = 0;
		iLimit = 100; iDisplayPlacements = false; iSimpleMode=false; iFilterText = null;
		iMinRoomSize = -1; iMaxRoomSize = -1; iDisplaySuggestions = false; iDisplayConfTable = false;
		iCanUnassign = false;
	}
	
	public void load(SuggestionsModel model) {
		iShowFilter = model.canDisplayFilter();
		iFilter = SuggestionsModel.sFilters[model.getFilter()];
		iCanAllowBreakHard = model.getCanAllowBreakHard();
		iAllowBreakHard = model.getAllowBreakHard();
		iDisplayCBS = model.getDisplayCBS();
		iDepth = model.getDepth();
		iTimeout = model.getTimeout();
		iId = model.getClassId();
		iTimeoutReached = model.getTimeoutReached();
		iNrSolutions = model.getNrSolutions();
		iNrCombinationsConsidered = model.getNrCombinationsConsidered();
		iNrSuggestions = (model.getSuggestions()==null?0:model.getSuggestions().size());
		iDisplayPlacements = model.getDisplayPlacements();
		iSimpleMode = model.getSimpleMode();
		iLimit = model.getLimit();
		iFilterText = model.getFilterText();
		iMinRoomSize = model.getMinRoomSize();
		iMaxRoomSize = model.getMaxRoomSize();
		iDisplayConfTable = model.getDisplayConfTable();
		iDisplaySuggestions = model.getDisplaySuggestions();
	}
	
	public void save(SuggestionsModel model) {
		model.setFilter(getFilterInt());
		model.setAllowBreakHard(getAllowBreakHard());
		model.setDisplayCBS(getDisplayCBS());
		model.setLimit(getLimit());
		model.setDisplayPlacements(getDisplayPlacements());
		model.setSimpleMode(getSimpleMode());
		model.setFilterText(getFilterText());
		model.setMinRoomSize(getMinRoomSize());
		model.setMaxRoomSize(getMaxRoomSize());
		model.setDisplayConfTable(getDisplayConfTable());
		model.setDisplaySuggestions(getDisplaySuggestions());
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	public String getFilter() { return iFilter; }
	public void setFilter(String filter) { iFilter = filter; }
	public String[] getFilters() { return SuggestionsModel.sFilters; }
	public int getFilterInt() {
		for (int i=0;i<SuggestionsModel.sFilters.length;i++)
			if (SuggestionsModel.sFilters[i].equals(iFilter)) return i;
		return 0;
	}
	public boolean getAllowBreakHard() { return iAllowBreakHard; }
	public void setAllowBreakHard(boolean allowBreakHard) { iAllowBreakHard = allowBreakHard; }
	public boolean getDisplayCBS() { return iDisplayCBS; }
	public void setDisplayCBS(boolean displayCBS) { iDisplayCBS = displayCBS; }
	public boolean getDisplayPlacements() { return iDisplayPlacements; }
	public void setDisplayPlacements(boolean displayPlacements) { iDisplayPlacements = displayPlacements; }
	public int getLimit() { return iLimit; }
	public void setLimit(int limit) { iLimit = limit; }
	public boolean getSimpleMode() { return iSimpleMode; }
	public void setSimpleMode(boolean simpleMode) { iSimpleMode = simpleMode; }
	public String getFilterText() { return iFilterText; }
	public void setFilterText(String text) { iFilterText = text; }
	public int getDepth() { return iDepth; }
	public void setDepth(int depth) { iDepth = depth; }
	public long getTimeout() { return iTimeout/1000; }
	public void setTimeout(long timeout) { iTimeout = 1000*timeout; }
	public Long getId() { return iId; }
	public void setId(Long id) { iId = id; }
	public boolean getShowFilter() { return iShowFilter; }
	public void setShowFilter(boolean showFilter) { iShowFilter = showFilter; }
	public boolean getCanAllowBreakHard() { return iCanAllowBreakHard; }
	public void setCanAllowBreakHard(boolean canAllowBreakHard) { iCanAllowBreakHard = canAllowBreakHard; }
    public boolean getTimeoutReached() { return iTimeoutReached; }
    public long getNrCombinationsConsidered() { return iNrCombinationsConsidered; }
    public long getNrSolutions() { return iNrSolutions; }
    public long getNrSuggestions() { return iNrSuggestions; }
	public int getMinRoomSize() { return iMinRoomSize; }
	public int getMaxRoomSize() { return iMaxRoomSize; }
	public void setMinRoomSize(int minRoomSize) { iMinRoomSize = minRoomSize; }
	public void setMaxRoomSize(int maxRoomSize) { iMaxRoomSize = maxRoomSize; }
	public boolean getDisplayConfTable() { return iDisplayConfTable; }
	public void setDisplayConfTable(boolean displayConfTable) { iDisplayConfTable = displayConfTable; }
	public boolean getDisplaySuggestions() { return iDisplaySuggestions; }
	public void setDisplaySuggestions(boolean displaySuggestions) { iDisplaySuggestions = displaySuggestions; }
	public String getMinRoomSizeText() { return (iMinRoomSize<0?"":String.valueOf(iMinRoomSize)); }
	public String getMaxRoomSizeText() { return (iMaxRoomSize<0?"":String.valueOf(iMaxRoomSize)); }
	public void setMinRoomSizeText(String minRoomSizeText) {
		try {
			iMinRoomSize = Integer.parseInt(minRoomSizeText);
		} catch (NumberFormatException e) {
			iMinRoomSize = -1;
		}
	}
	public void setMaxRoomSizeText(String maxRoomSizeText) { 
		try {
			iMaxRoomSize = Integer.parseInt(maxRoomSizeText);
		} catch (NumberFormatException e) {
			iMaxRoomSize = -1;
		}
	}
	
	public boolean isCanUnassign() { return iCanUnassign; }
	public void setCanUnassign(boolean canUnassign) { iCanUnassign = canUnassign; }
}

