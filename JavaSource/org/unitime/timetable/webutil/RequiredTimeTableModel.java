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
package org.unitime.timetable.webutil;

import java.awt.Color;

/**
 * @author Tomas Muller
 */
public interface RequiredTimeTableModel {
	/** Name to be printed above time preferences grid */
	public String getName(); 
	
	/** Number of days (columns) */
	public int getNrDays();
	/** Number of times (rows) */
	public int getNrTimes();
	/** Start time */
	public String getStartTime(int time);
	/** End time */
	public String getEndTime(int time);
	/** Day header, e.g., MWF */
	public String getDayHeader(int day);
	
	/** Set preference */ 
	public void setPreference(int day, int time, String pref);
	/** Get preference */
	public String getPreference(int day, int time);
	/** Get text to be printed on the field */
	public String getFieldText(int day, int time);
	/** Is given field editable */
	public boolean isEditable(int day, int time);
	
	/** Get all preferences, represented as a single string */ 
	public String getPreferences();
	/** Set all preferences, represented as a single string */
	public void setPreferences(String pref);
	
	/** Exact time preference */
	public boolean isExactTime();
    public int getExactDays();
    public int getExactStartSlot();
    public void setExactDays(int days);
    public void setExactStartSlot(int slot);
    
    /** Default preference */
	public String getDefaultPreference();
	
	/** Border of the field (null for default border) */
	public Color getBorder(int day, int time);
	
	/** Preference names ('R', '-2', ...) */
	public String[] getPreferenceNames();
	/** Preference color */
	public Color getPreferenceColor(String pref);
	/** Preference text (to be printed in legend) */
	public String getPreferenceText(String pref);
	public boolean isPreferenceEnabled(String pref);
	
	/** Number of selections*/
	public int getNrSelections();
	public String getSelectionName(int idx);
	public int[] getSelectionLimits(int idx);
	public int getDefaultSelection();
	public void setDefaultSelection(int selection);
	public void setDefaultSelection(String selectionName);
	
	public String getPreferenceCheck();
}
