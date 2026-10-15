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
    
}
