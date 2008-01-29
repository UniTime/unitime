/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.webutil;

import org.unitime.commons.User;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.util.Constants;


/**
 * Miscellaneous function to generate javascript based on settings
 * 
 * @author Heston Fernandes
 */
public class JavascriptFunctions {

    public static boolean isJsConfirm(User user) {
        String jsConfirm = Settings.getSettingValue(user, Constants.SETTINGS_JS_DIALOGS);
        return (jsConfirm==null || !jsConfirm.equals("no")); 
    }

    /**
     * Returns the javascript variable 'jsConfirm' set to true/false depending
     * on the user setting. This function is called from JSPs and is used to
     * determine whether confirmation dialogs are displayed to the user
     * @param user User Object
     * @return String "var jsConfirm = true;" OR "var jsConfirm = false;"
     */
    public static String getJsConfirm(User user) {
        return "var jsConfirm = "+(isJsConfirm(user)?"true":"false")+";";
    }
    
    public static String getInheritInstructorPreferencesCondition(User user) {
    	String inheritInstrPref = Settings.getSettingValue(user, Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF);
    	if (Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF_YES.equals(inheritInstrPref)) {
    		return "true";
    	} else if (Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF_NO.equals(inheritInstrPref)) {
    		return "false";
    	} else if (Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF_CONFIRM.equals(inheritInstrPref)) {
    		return "confirm('Do you want to apply instructor preferences to this class?')";
    	} else {
    		return "confirm('Do you want to apply instructor preferences to this class?')";
    	}
    }
    
    public static String getCancelInheritInstructorPreferencesCondition(User user) {
    	String inheritInstrPref = Settings.getSettingValue(user, Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF);
    	if (Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF_YES.equals(inheritInstrPref)) {
    		return "true";
    	} else if (Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF_NO.equals(inheritInstrPref)) {
    		return "false";
    	} else if (Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF_CONFIRM.equals(inheritInstrPref)) {
    		return "confirm('Do you want to remove any instructor preferences \\nthat may have been applied to this class?')";
    	} else {
    		return "confirm('Do you want to remove any instructor preferences \\nthat may have been applied to this class?')";
    	}
    }
    
}
