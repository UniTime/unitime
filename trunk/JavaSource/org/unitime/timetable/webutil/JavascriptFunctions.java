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

import org.unitime.commons.User;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.util.Constants;


/**
 * Miscellaneous function to generate javascript based on settings
 * 
 * @author Heston Fernandes
 */
public class JavascriptFunctions {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

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
    		return "confirm('"+MSG.confirmApplyInstructorPreferencesToClass()+"')";
    	} else {
    		return "confirm('"+MSG.confirmApplyInstructorPreferencesToClass()+"')";
    	}
    }
    
    public static String getCancelInheritInstructorPreferencesCondition(User user) {
    	String inheritInstrPref = Settings.getSettingValue(user, Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF);
    	if (Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF_YES.equals(inheritInstrPref)) {
    		return "true";
    	} else if (Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF_NO.equals(inheritInstrPref)) {
    		return "false";
    	} else if (Constants.SETTINGS_INHERIT_INSTRUCTOR_PREF_CONFIRM.equals(inheritInstrPref)) {
    		return "confirm('"+MSG.confirmRemoveInstructorPreferencesFromClass()+"')";
    	} else {
    		return "confirm('"+MSG.confirmRemoveInstructorPreferencesFromClass()+"')";
    	}
    }
    
}
