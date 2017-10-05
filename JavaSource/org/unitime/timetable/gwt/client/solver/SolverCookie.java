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
package org.unitime.timetable.gwt.client.solver;

import java.util.Date;

import org.unitime.timetable.gwt.shared.SolverInterface.ProgressLogLevel;

import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class SolverCookie {
	private static SolverCookie sInstance = null;
	private int iLogLevel = ProgressLogLevel.INFO.ordinal();
	private boolean iTimeGridFilter = true;
	private boolean iAssignedClassesFilter = true;
	private int iAssignedClassesSort = 0;
	private boolean iNotAssignedClassesFilter = true;
	private int iNotAssignedClassesSort = 0;
	private int iSelectedAssignmentsSort = 0, iConflictingAssignmentsSort = 0, iSuggestionsSort = 0, iPlacementsSort = 0, iConflictsSort = 0, iSolutionChangesSort = 0;
	private boolean iShowSuggestions = true;
	private String iSuggestionsFilter = "";
	private boolean iShowConflicts = false;
	private boolean iShowAllStudentConflicts = false, iShowAllDistributionConflicts = false;
	private boolean iShowCBS = false, iShowCBSFilter = true, iSolutionChangesFilter = true;
	
	private SolverCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Solver");
			if (cookie != null) {
				String[] params = cookie.split("\\|");
				int idx = 0;
				iLogLevel = Integer.valueOf(params[idx++]);
				iTimeGridFilter = "1".equals(params[idx++]);
				iAssignedClassesFilter = "1".equals(params[idx++]);
				iAssignedClassesSort = Integer.parseInt(params[idx++]);
				iNotAssignedClassesFilter = "1".equals(params[idx++]);
				iNotAssignedClassesSort = Integer.parseInt(params[idx++]);
				iSelectedAssignmentsSort = Integer.parseInt(params[idx++]);
				iConflictingAssignmentsSort = Integer.parseInt(params[idx++]);
				iSuggestionsSort = Integer.parseInt(params[idx++]);
				iPlacementsSort = Integer.parseInt(params[idx++]);
				iShowSuggestions = "1".equals(params[idx++]);
				iShowConflicts = "1".equals(params[idx++]);
				iConflictsSort = Integer.parseInt(params[idx++]);
				iShowAllStudentConflicts = "1".equals(params[idx++]);
				iShowAllDistributionConflicts = "1".equals(params[idx++]);
				iShowCBS = "1".equals(params[idx++]);
				iShowCBSFilter = "1".equals(params[idx++]);
				iSolutionChangesFilter = "1".equals(params[idx++]);
				iSolutionChangesSort = Integer.parseInt(params[idx++]);
				iSuggestionsFilter = params[idx++];
			}
		} catch (Exception e) {}
	}
	
	private void save() {
		String cookie = iLogLevel + "|" + (iTimeGridFilter ? "1" : "0")
				+ "|" + (iAssignedClassesFilter ? "1" : "0") + "|" + iAssignedClassesSort
				+ "|" + (iNotAssignedClassesFilter ? "1" : "0") + "|" + iNotAssignedClassesSort
				+ "|" + iSelectedAssignmentsSort + "|" + iConflictingAssignmentsSort
				+ "|" + iSuggestionsSort + "|" + iPlacementsSort
				+ "|" + (iShowSuggestions ? "1" : "0")
				+ "|" + (iShowConflicts ? "1" : "0") + "|" + iConflictsSort
				+ "|" + (iShowAllStudentConflicts ? "1" : "0") + "|" + (iShowAllDistributionConflicts ? "1" : "0")
				+ "|" + (iShowCBS ? "1" : "0") + "|" + (iShowCBSFilter ? "1" : "0")
				+ "|" + (iSolutionChangesFilter ? "1" : "0") + "|" + iSolutionChangesSort
				+ "|" + (iSuggestionsFilter == null ? "" : iSuggestionsFilter);
		Date expires = new Date(new Date().getTime() + 604800000l); // expires in 7 days
		Cookies.setCookie("UniTime:Solver", cookie, expires);
	}
	
	public static SolverCookie getInstance() { 
		if (sInstance == null)
			sInstance = new SolverCookie();
		return sInstance;
	}
	
	public int getLogLevel() { return iLogLevel; }
	public void setLogLevel(int level) {
		iLogLevel = level; save();
	}
	
	public boolean isTimeGridFilter() { return iTimeGridFilter; }
	public void setTimeGridFilter(boolean filter) {
		iTimeGridFilter = filter; save();
	}
	
	public boolean isAssignedClassesFilter() { return iAssignedClassesFilter; }
	public void setAssignedClassesFilter(boolean filter) {
		iAssignedClassesFilter = filter; save();
	}
	
	public int getAssignedClassesSort() { return iAssignedClassesSort; }
	public void setAssignedClassesSort(int sort) {
		iAssignedClassesSort = sort; save();
	}
	
	public boolean isNotAssignedClassesFilter() { return iNotAssignedClassesFilter; }
	public void setNotAssignedClassesFilter(boolean filter) {
		iNotAssignedClassesFilter = filter; save();
	}
	
	public int getNotAssignedClassesSort() { return iNotAssignedClassesSort; }
	public void setNotAssignedClassesSort(int sort) {
		iNotAssignedClassesSort = sort; save();
	}
	
	public int getSelectedAssignmentsSort() { return iSelectedAssignmentsSort; }
	public void setSelectedAssignmentsSort(int sort) {
		iSelectedAssignmentsSort = sort; save();
	}

	public int getConflictingAssignmentsSort() { return iConflictingAssignmentsSort; }
	public void setConflictingAssignmentsSort(int sort) {
		iConflictingAssignmentsSort = sort; save();
	}
	
	public int getSuggestionsSort() { return iSuggestionsSort; }
	public void setSuggestionsSort(int sort) {
		iSuggestionsSort = sort; save();
	}
	
	public int getPlacementsSort() { return iPlacementsSort; }
	public void setPlacementsSort(int sort) {
		iPlacementsSort = sort; save();
	}
	
	public boolean isShowSuggestions() { return iShowSuggestions; }
	public void setShowSuggestions(boolean showSuggestions) { iShowSuggestions = showSuggestions; save(); }
	
	public String getSuggestionsFilter() { return iSuggestionsFilter == null ? "" : iSuggestionsFilter; }
	public void setSuggestionsFilter(String filter) { iSuggestionsFilter = filter; save(); }
	
	public boolean isShowConflicts() { return iShowConflicts; }
	public void setShowConflicts(boolean showConflicts) { iShowConflicts = showConflicts; save(); }
	
	public int getConflictsSort() { return iConflictsSort; }
	public void setConflictsSort(int sort) {
		iConflictsSort = sort; save();
	}
	
	public boolean isShowAllStudentConflicts() { return iShowAllStudentConflicts; }
	public void setShowAllStudentConflicts(boolean showAllStudentConflicts) { iShowAllStudentConflicts = showAllStudentConflicts; save(); }
	public boolean isShowAllDistributionConflicts() { return iShowAllDistributionConflicts; }
	public void setShowAllDistributionConflicts(boolean showAllDistributionConflicts) { iShowAllDistributionConflicts = showAllDistributionConflicts; save(); }
	
	public boolean isShowCBS() { return iShowCBS; }
	public void setShowCBS(boolean showCBS) { iShowCBS = showCBS; save(); }
	public boolean isShowCBSFilter() { return iShowCBSFilter; }
	public void setShowCBSFilter(boolean filter) { iShowCBSFilter = filter; save(); }
	
	public boolean isSolutionChangesFilter() { return iSolutionChangesFilter; }
	public void setSolutionChangesFilter(boolean filter) { iSolutionChangesFilter = filter; save(); }
	public int getSolutionChangesSort() { return iSolutionChangesSort; }
	public void setSolutionChangesSort(int sort) { iSolutionChangesSort = sort; save(); }
}
