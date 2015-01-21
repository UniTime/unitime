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

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.security.SessionContext;


/**
 * Miscellaneous function to generate javascript based on settings
 * 
 * @author Heston Fernandes, Tomas Muller, Zuzana Mullerova
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
