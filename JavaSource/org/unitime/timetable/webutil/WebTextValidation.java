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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unitime.commons.Debug;

/**
 * Miscellaneous functions to try to screen out attempts at cross-site scripting
 * 
 * @author Stephanie Schluttenhofer
 */

public class WebTextValidation {

	private static final String patternStr = ".*[|<>\"+].*";
	private static final String hexPatternStr = ".*%[0-9A-Fa-f][0-9A-Fa-f].*";

	private static final String phonePatternStr = "^[ \t\n\f\r]*[0-9)( \t\n\f\r+][0-9)( \t\n\f\r.,-]+[ \t\n\f\r]*$";
	
	public static boolean isTextValid(String aText, boolean canBeNull){
		if (!canBeNull && (aText == null || aText.trim().length() == 0)){
			return(false);
		}
		
		if (canBeNull && (aText == null || aText.trim().length() == 0)){
			return(true);
		}
		
		String checkText = aText.toUpperCase();
    	try { 
    		Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL + Pattern.CASE_INSENSITIVE + Pattern.UNIX_LINES);
	    	Matcher matcher = pattern.matcher(checkText);
	    	if (matcher.matches()) {
		        return(false);
	    	}
    		Pattern patternHex = Pattern.compile(hexPatternStr, Pattern.DOTALL + Pattern.CASE_INSENSITIVE + Pattern.UNIX_LINES);
	    	Matcher matcherHex = patternHex.matcher(checkText);
	    	if (matcherHex.matches()) {
		        return(false);
	    	}
    	}
    	catch (Exception e) {
			Debug.info("Threw exception " + e.getMessage());
	        return(false);
    	}

		
		return(true);
	}
	
	public static boolean containsOnlyCharactersUsedInPhoneNumbers(String aText, boolean canBeNull){
		if (!canBeNull && (aText == null || aText.trim().length() == 0)){
			return(false);
		}
		if (canBeNull && (aText == null || aText.trim().length() == 0)){
			return(true);
		}
    	try { 
    		Pattern phonePattern = Pattern.compile(phonePatternStr);
	    	Matcher matcher = phonePattern.matcher(aText);
	    	if (matcher.find()) {
		        return(true);
	    	}
    	} catch (Exception e) {
	        return(false);
    	}
		return(false);
	}

	public WebTextValidation() {
		super();
	}
	
}
