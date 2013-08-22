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
