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

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.security.SessionContext;


/**
 * Miscellaneous function to generate javascript based on settings
 * 
 * @author Heston Fernandes
 */
public class JavascriptFunctions {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

    public static boolean isJsConfirm(SessionContext context) {
        return (context.isAuthenticated() ? CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.ConfirmationDialogs)) : true);
    }

    /**
     * Returns the javascript variable 'jsConfirm' set to true/false depending
     * on the user setting. This function is called from JSPs and is used to
     * determine whether confirmation dialogs are displayed to the user
     * @param user User Object
     * @return String "var jsConfirm = true;" OR "var jsConfirm = false;"
     */
    public static String getJsConfirm(SessionContext context) {
    	return "var jsConfirm = "+(isJsConfirm(context) ? "true" : "false")+";";
    }
    
    public static String getInheritInstructorPreferencesCondition(SessionContext context) {
    	String inheritInstrPref = context.getUser().getProperty(UserProperty.InheritInstructorPrefs);
    	if (CommonValues.Always.eq(inheritInstrPref)) {
    		return "true";
    	} else if (CommonValues.Never.eq(inheritInstrPref)) {
    		return "false";
    	} else {
    		return "confirm('"+MSG.confirmApplyInstructorPreferencesToClass()+"')";
    	}
    }
    
    public static String getCancelInheritInstructorPreferencesCondition(SessionContext context) {
    	String inheritInstrPref = context.getUser().getProperty(UserProperty.InheritInstructorPrefs);
    	if (CommonValues.Always.eq(inheritInstrPref)) {
    		return "true";
    	} else if (CommonValues.Never.eq(inheritInstrPref)) {
    		return "false";
    	} else {
    		return "confirm('"+MSG.confirmRemoveInstructorPreferencesFromClass()+"')";
    	}
    }
    
}
