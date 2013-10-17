/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2009 - 2013, UniTime LLC, and individual contributors
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
