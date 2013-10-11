/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2008-2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ConstantsMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.security.UserContext;



/**
 * Various constants used in timetabling project.
 * @author Tomas Muller
 */
public class Constants extends net.sf.cpsolver.coursett.Constants {
	public static final ConstantsMessages MSG = Localization.create(ConstantsMessages.class);
	public static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

    // --------------------------------------------------------- Class Properties
    
    /** Day names in 3-character format (e.g. Mon) */
    public static String DAY_NAME[] = new String[] {
    	MSG.mon(), MSG.tue(), MSG.wed(), MSG.thu(), MSG.fri(), MSG.sat(), MSG.sun()
        };

    /** Day names */
    public static String DAY_NAMES_FULL[] = new String[] {
    	MSG.monday(), MSG.tuesday(), MSG.wednesday(), MSG.thursday(), MSG.friday(), MSG.saturday(), MSG.sunday()
        };

    public static final int DAY_MON = 0;
    public static final int DAY_TUE = 1;
    public static final int DAY_WED = 2;
    public static final int DAY_THU = 3;
    public static final int DAY_FRI = 4;
    public static final int DAY_SAT = 5;
    public static final int DAY_SUN = 6;
    
    public static int EVENING_SLOTS_FIRST = DAY_SLOTS_LAST + 1;
    public static int EVENING_SLOTS_LAST = (23*60 + 00)/5 - 1; // evening ends at 23:00

    /** version */
    public static String VERSION = "3.5";
    
    /** release date */
    private static String REL_DATE = "${build.date}";
    public static String getReleaseDate() { return REL_DATE.replaceAll("\\$\\{[^\\}]*\\}", "?"); }

    /** build number */
    private static String BLD_NUMBER = "${build.number}";
    public static String getBuildNumber() { return BLD_NUMBER.replaceAll("\\$\\{[^\\}]*\\}", "?"); }
    public static String getVersion() { return VERSION + "." + getBuildNumber(); }

    /** startup date format */
    public static String STARTUP_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";

    /** date format */
    public static String DATE_FORMAT = "EEE, d MMM yyyy";
    public static int TIME_PATTERN_STANDARD = 0;
    public static int TIME_PATTERN_GENERIC = 1;
    public static int TIME_PATTERN_EVENING = 2;
    public static int TIME_PATTERN_SATURDAY = 3;

    /** campus */
    public static String CAMPUS_WLAF = "1";

    /** Request Attribute names */
    public static String SESSION_ID_ATTR_NAME = "acadSessionId";
    public static String ACAD_YRTERM_ATTR_NAME = "acadYearTerm";
    public static String ACAD_YRTERM_LABEL_ATTR_NAME = "acadYearTermLabel";
    public static String TMTBL_MGR_ID_ATTR_NAME = "tmtblManagerId";
    
    /** Request Attribute for jump-to anchor */
    public static String JUMP_TO_ATTR_NAME = "jumpTo";
    
    /** Session Attribute Names */
    public static String DEPT_ID_ATTR_NAME = "deptUniqueId";
    public static String DEPT_CODE_ATTR_NAME = "deptCode";
    public static String CRS_ASGN_LST_SUBJ_AREA_IDS_ATTR_NAME = "crsAsgnLstSubjectAreaIds";
    
    /** LLR Manager Department Codes **/
    public static String[] LLR_DEPTS = { "1994", "1980" };
    
    /** Blank Select Box Label/Value */
    public static String BLANK_OPTION_LABEL = MSG.select();
    public static String BLANK_OPTION_VALUE = ""; 
    public static String ALL_OPTION_LABEL = MSG.all();
    public static String ALL_OPTION_VALUE = "All";
    
    /** Facility group references */
    public static String FACILITY_GROUP_LLR = "LLR";
    public static String FACILITY_GROUP_LAB = "LAB";
    public static String FACILITY_GROUP_DEPT = "DEPT";
    
    /** Pref names */
    public static String PREF_CLASS_NAMES[] = new String[] {
            TimePref.class.getName(),
            RoomPref.class.getName(), 
            BuildingPref.class.getName(), 
            RoomFeaturePref.class.getName(), 
            DistributionPref.class.getName()
        };

    /** Indicates classes are managed by multiple departments */
    public static final long MANAGED_BY_MULTIPLE_DEPTS = 0;

    /** (Http)Request attributes */
    public static final String REQUEST_OPEN_URL = "RqOpenUrl";
    public static final String REQUEST_WARN = "RqWarn";
    public static final String REQUEST_MSSG = "RqMsg";
    
    // --------------------------------------------------------- Methods

    /**
     * Check if string is a positive integer greater or equal to than 0
     * @param str String to be converted to integer
     * @param defaultValue Default value returned if conversion fails
     * @return integer if success, default value if fails
     */
    public static int getPositiveInteger(String str, int defaultValue) {
        try {
            int i = Integer.parseInt(str);
            if(i>=0) return i;
        }
        catch (Exception e) {
            return defaultValue;
        }
        
        return defaultValue;
    }
    
    /**
     * Check if string is a positive float greater or equal to than 0
     * @param str String to be converted to float
     * @param defaultValue Default value returned if conversion fails
     * @return float if success, default value if fails
     */
    public static float getPositiveFloat(String str, float defaultValue) {
        try {
            float i = Float.parseFloat(str);
            if(i>=0) return i;
        }
        catch (Exception e) {
            return defaultValue;
        }
        
        return defaultValue;
    }
    
    /**
     * Filters out possible Cross Site Scripting and SQL Injection characters by allowing only 
     * the following characters in the input A-Z a-z 0-9 @ . ' space _ -
     * @param str Input String
     * @return Filtered String
     */
    public static String filterXSS(String str) {
        if(str!=null)
            str = str.trim().replaceAll("([^A-Za-z0-9@.' _-]+)", "_");
        return str;
    }
    
    /**
     * Converts an array of object to a string representation
     * @param array Array of objects
     * @param encloseBy Each array object will be enclosed by the parameter supplied
     * @param separator Array objects will separated by this separator
     * @return Array converted to a string
     * @throws IllegalArgumentException
     */
    public static String arrayToStr(Object[] array, String encloseBy, String separator) 
    	throws IllegalArgumentException {
        
        if(array==null || array.length==0)
            throw new IllegalArgumentException("Supply a valid array");
        
        if(encloseBy==null)
            throw new IllegalArgumentException("encloseBy cannot be null");
        
        if(separator==null)
            throw new IllegalArgumentException("separator cannot be null");

        StringBuffer str = new StringBuffer("");
        for (int i=0; i<array.length; i++) {
            str.append(encloseBy);
            str.append(array[i].toString());
            str.append(encloseBy);
            if (i<array.length-1)
                str.append(separator);
        }
        
        return str.toString();
    }

	/**
	 * Returns a string with desired character padding before the string representation of the
     * object to make it the desired width. No change if it is already that long
     * or longer.
     * 
     * @param value object to display
     * @param width size of field in which to display the object
   	 * @param pad pad character(s); defaults to space 
     * @return string version of value with spaces before to make it the given width
     * @throws Exception if parameters are not correct
     */
	
  	public static String leftPad(Object value, int width, String pad) 
  		throws Exception {	
    	return pad(1, value, width, pad);
  	}
  	
  
  /**
   * Returns a string with desired character padding after the string representation of the
   * string to make it the desired width. No change if it is already that long
   * or longer. 
   * 
   * @param value object to display
   * @param width size of field in which to display the object
   * @param pad pad character(s); defaults to space
   * @return string version of value with spaces after to make it the given width
   * @throws Exception if parameters are not correct
   */
  
  	public static String rightPad(Object value, int width, String pad) 
  		throws Exception {
    	return pad(2, value, width, pad);
  	}
    
    /**
     * Returns a string with desired character padding before or after the string representation of the
     * string to make it the desired width. No change if it is already that long
     * or longer. 
     * 
     * @param direction 1=left pad, any other integer=right pad 
     * @param value object to display
     * @param width size of field in which to display the object
     * @param pad pad character(s); defaults to space
     * @return string version of value with spaces after to make it the given width
     * @throws Exception if parameters are not correct
   */
  
  	public static String pad(int direction, Object value, int width, String pad)  
  		throws Exception {
  	    
  	    String value1;

  	    if(value==null)
  	        value1 = "";
  	    else
  	        value1 = value.toString();

  	    if(width<=0)
  	        throw new Exception("Width must be > 0");
  	    
   		if(pad.equals("") || pad==null)
  			pad = " ";
  			
  		int lnCount = width-value1.length();

  		// Left Pad
  		if(direction==1) {
	  	  	for(int i=0;i<lnCount;i++)
  	     	 	value1 = pad + value1;
  		}
  		
  		// Default Right Pad
  		else {
  	  	  	for(int i=0;i<lnCount;i++)
	      		value1 = value1 + pad;
  		}
  		
    	return value1;
  	}
  	
  	/**
  	 * Converts a string to initial case.
  	 * All letters occuring after a space or period are converted to uppercase
  	 * Example JOHN G DOE -> John G Doe
  	 * @param str Input String 
  	 * @return Formatted String
  	 */
  	public static String toInitialCase(String str) {
  	    return toInitialCase(str, new char[] {'-', '\''});
  	}
  	
  	/**
  	 * Converts a string to initial case.
  	 * All letters occuring after a space or period are converted to uppercase
  	 * Example JOHN G DOE -> John G Doe
  	 * @param str Input String
  	 * @param delimiters array of delimiters which should be included with space and period 
  	 * @return Formatted String
  	 */
 	public static String toInitialCase(String str, char[] delimiters) {
  	    if (str==null || str.trim().isEmpty()) return str;
  	    
  	    if (!str.toUpperCase().equals(str) && !str.toLowerCase().equals(str)) return str;

  	    char[] chars = str.toCharArray();
  	    boolean upper = true;
  	    
  	    for (int i=0; i<chars.length; i++) {
  	        if (upper && Character.isLetter(chars[i])) {
  	            chars[i] = Character.toUpperCase(chars[i]);
  	            upper = false;
  	        } 
  	        else {
  	            chars[i] = Character.toLowerCase(chars[i]);
  	        }
  	        
  	        if (!Character.isLetterOrDigit(chars[i])
  	                && chars[i]!='\'') {
  	            upper = true;
  	        }
  	        
  	        if (delimiters!=null && delimiters.length>0) {
  	            for (int j=0; j<delimiters.length; j++) {
  	                if (chars[i] == delimiters[j]) {
  	                    upper = true;
  	                    break;
  	                }
  	            }
  	        }
  	        
  	    }
  	    
  	    str = new String(chars);  	    
  	    return str;
 	}

 	
  	public static String slot2str(int slot) {
  	    return toTime(slot*Constants.SLOT_LENGTH_MIN+Constants.FIRST_SLOT_TIME_MIN);
  	}
  	
  	/**
  	 * Test Method
  	 * @param args
  	 */
  	public static void main(String[] args) {
  	    System.out.println("JOHN G DOE -> " + toInitialCase("JOHN G DOE"));
  	    System.out.println("DOE jOHn G. -> " + toInitialCase("DOE jOHn G."));
  	    System.out.println("jOhN g. dOe -> " + toInitialCase("jOhN g. dOe"));  	    
  	    System.out.println("To be Or NOT to BE.that is THE QUESTION. -> " + toInitialCase("To be Or NOT to BE.that is THE QUESTION."));  	    
  	}

  	/**
  	 * sort a string
  	 * @param data
  	 * @param separator
  	 * @return
  	 */
	public static String sort(String data, String separator) {
		if(separator==null)
			throw new IllegalArgumentException("separator cannot be null");
		
		List list = Arrays.asList(data.split(separator));
		Collections.sort(list);

		return list.toString();
	}

    /**
     * True if string can be parsed to an integer
     * @param str
     * @return
     */
	public static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
		} catch (Exception e) {
			return false;			
		}
		return true;
	}

	/**
	 * True if it can be parsed to a number
	 * @param str
	 * @return
	 */
	public static boolean isNumber(String str) {
		try {
			Double.parseDouble(str);
		} catch (Exception e) {
			return false;			
		}
		return true;
	}
	
	public static final int PREF_ROWS_ADDED = 2;
	
	public static String toTime(int minutesSinceMidnight) {
	    int hour = minutesSinceMidnight/60;
	    int min = minutesSinceMidnight%60;
	    if (CONSTANTS.useAmPm())
	    	return (hour==0?12:hour>12?hour-12:hour)+":"+(min<10?"0":"")+min+(hour<24 && hour>=12?"p":"a");
	    else
	    	return hour + ":" + (min < 10 ? "0" : "") + min;
	}
	
	public static boolean showPrintNoteAsFullText(UserContext user) {
		return user != null && CommonValues.NoteAsFullText.value().equals(user.getProperty(UserProperty.SchedulePrintNoteDisplay));
    }
	
    public static boolean showCrsOffrAsFullText(UserContext user) {
    	return user != null && CommonValues.NoteAsFullText.value().equals(user.getProperty(UserProperty.CourseOfferingNoteDisplay));
    }
    
    public static int getDefaultExamStartOffset(ExamType examType){
      	return getExamOffset("tmtbl.exam.defaultStartOffset." + examType.getReference());
    }
    
    public static int getDefaultExamStopOffset(ExamType examType){
    	return getExamOffset("tmtbl.exam.defaultStopOffset." + examType.getReference());
    }
        
    private static int getExamOffset(String offsetParameterName){
    	int offset = 0;
    	String offsetStr = ApplicationProperties.getProperty(offsetParameterName);
    	if (offsetStr == null || offsetStr.trim().length() == 0){
    		offset = 0;
    	} else {
    		try {
				offset = Integer.parseInt(offsetStr);
			} catch (NumberFormatException e) {
				offset = 0;
			}
			if (offset < 0){
				offset = 0;
			}
    	}
    	return(offset);
    }
    
    public static int getDayOfWeek(Date date) {
    	Calendar c = Calendar.getInstance(Locale.US);
    	c.setTime(date);
		switch (c.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			return DAY_MON;
		case Calendar.TUESDAY:
			return DAY_TUE;
		case Calendar.WEDNESDAY:
			return DAY_WED;
		case Calendar.THURSDAY:
			return DAY_THU;
		case Calendar.FRIDAY:
			return DAY_FRI;
		case Calendar.SATURDAY:
			return DAY_SAT;
		case Calendar.SUNDAY:
			return DAY_SUN;
		default:
			return DAY_MON;
		}
    }
    
	public static int toHour(int slot) {
		int min = slot * SLOT_LENGTH_MIN + FIRST_SLOT_TIME_MIN;
		return min / 60;
	}
	
	public static int toMinute(int slot) {
		int min = slot * SLOT_LENGTH_MIN + FIRST_SLOT_TIME_MIN;
		return min % 60;
	}
	
	private static Boolean sCurriculaToInitialCase = null;
	public static String curriculaToInitialCase(String text) {
		if (sCurriculaToInitialCase == null)
			sCurriculaToInitialCase = "true".equals(ApplicationProperties.getProperty("tmtbl.toInitialCase.curriculum", "false"));
		if (sCurriculaToInitialCase)
			return Constants.toInitialCase(text);
		return text;
	}

}
