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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.util.Constants;


/**
 * Superclass for implementing Preferences
 * 
 * @author Heston Fernandes, Tomas Muller, Zuzana Mullerova
 */
public class PreferencesForm implements UniTimeForm {

	private static final long serialVersionUID = -3578647598790726006L;
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	
	// --------------------------------------------------------- Instance Variables
    
	protected String op;
    protected List<String> timePatterns;
    protected List<String> roomGroups;
    protected List<String> roomGroupLevels;
    protected List<String> roomPrefs;
    protected List<String> roomPrefLevels;
    protected List<String> bldgPrefs;
    protected List<String> bldgPrefLevels;
    protected List<String> roomFeaturePrefs;
    protected List<String> roomFeaturePrefLevels;
    protected String timePattern;
    protected List<TimePattern> availableTimePatterns;
    protected List<String> distPrefs;
    protected List<String> distPrefLevels;
    protected List<String> datePatternPrefs;
    protected List<String> datePatternPrefLevels;
    protected List<String> coursePrefs;
    protected List<String> coursePrefLevels;
    protected List<String> instructorPrefs;
    protected List<String> instructorPrefLevels;
    protected List<String> attributePrefs;
    protected List<String> attributePrefLevels;
    protected String availability;
    
    private String nextId;
    private String previousId;
    
    protected boolean allowHardPrefs;
    
    private boolean hasNotAvailable;
    
    /**
     * Checks that there are no duplicates and that all prior prefs have a value
     * @param lst List<String> of values
     * @return true if checks ok, false otherwise
     */
    public boolean checkPrefs(List... lst) {
    	if (lst == null || lst.length == 0 || lst[0] == null) return true;
        
        HashMap map = new HashMap();
        for(int i=0; i<lst[0].size(); i++) {
            String value = ((String) lst[0].get(i));
            // No selection made - ignore
            if( value==null || value.trim().equals(Preference.BLANK_PREF_VALUE)) {
                continue;
            }
            // Add additional columns, when present
            for (int j = 1; j < lst.length; j++)
            	value += "|" + ((String)lst[j].get(i));
            
            // Duplicate selection made
            if(map.get(value.trim())!=null) {
                // lst[0].set(i, Preference.BLANK_PREF_VALUE);
                return false;
            }
            map.put(value, value);
        }
        return true;
    }
    
    /**
     * Checks that pref levels are selected
     * @param lst List<String> of pref levels
     * @return true if checks ok, false otherwise
     */
    public boolean checkPrefLevels(List<String> lst, List<String> prefList) {
    	if (lst == null) return true;
        
        for(int i=0; i<lst.size(); i++) {
            String id = ((String) prefList.get(i));
            String value = ((String) lst.get(i));
            
            // Ignore blank value of pref level if pref is blank
            if( id==null || id.trim().equals(Preference.BLANK_PREF_VALUE)) {
                continue;
            }
            
            // No selection made
            if( value==null || value.trim().equals(Preference.BLANK_PREF_VALUE)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @return Returns the op.
     */
    public String getOp() {
        return op;
    }
    /**
     * @param op The op to set.
     */
    public void setOp(String op) {
        this.op = op;
    }
    
    /**
     * @return Returns the timePattern.
     */
    public List<String> getTimePatterns() {
    	return timePatterns;
    }

    /**
     * @param timePattern The timePattern to set.
     */
    public void setTimePatterns(List<String> timePatterns) {
        this.timePatterns = timePatterns;
    }
    
    public List<TimePattern> getAvailableTimePatterns() {
        return availableTimePatterns;
    }
    
    public boolean getCanChooseTimePattern() {
    	return availableTimePatterns!=null;
    }

    public List<TimePattern> getAvailableNotSelectedTimePatterns() {
    	if (timePatterns==null || timePatterns.isEmpty()) return getAvailableTimePatterns();
    	Vector ret = new Vector();
    	for (Iterator i=availableTimePatterns.iterator();i.hasNext();) {
    		TimePattern pattern = (TimePattern)i.next();
    		if (!pattern.getTimePatternModel().isExactTime() && timePatterns.contains(pattern.getUniqueId().toString())) continue;
    		ret.add(pattern);
    	}
        return ret;
    }

    public void setAvailableTimePatterns(List<TimePattern> availableTimePatterns) {
        this.availableTimePatterns = availableTimePatterns;
    }

    public String getTimePattern() {
    	return timePattern;
    }
    
    public void setTimePattern(String timePattern) {
    	this.timePattern = timePattern;
    }
    
    public List<String> getRoomGroups() {
		return roomGroups;
	}

	public void setRoomGroups(List<String> roomGroups) {
		this.roomGroups = roomGroups;
	}
    /**
     * @param roomGroups The roomGroups to set.
     */
    public void setRoomGroups(int key, String value) {
        Debug.debug("setting room group: " + key);
        this.roomGroups.set(key, value);
    }
	
	public List<String> getRoomGroupLevels() {
		return roomGroupLevels;
	}
	
	public boolean isRoomGroupDisabled(int idx) {
		String levelId = getRoomGroupLevels(idx);
		return !getAllowHardPrefs() && !Preference.BLANK_PREF_VALUE.equals(levelId) && PreferenceLevel.getPreferenceLevel(Integer.parseInt(levelId)).isHard();
	}
	
	public boolean isRoomDisabled(int idx) {
		String levelId = getRoomPrefLevels(idx);
		return !getAllowHardPrefs() && !Preference.BLANK_PREF_VALUE.equals(levelId) && PreferenceLevel.getPreferenceLevel(Integer.parseInt(levelId)).isHard();
	}
	
	public boolean isRoomFeatureDisabled(int idx) {
		String levelId = getRoomFeaturePrefLevels(idx);
		return !getAllowHardPrefs() && !Preference.BLANK_PREF_VALUE.equals(levelId) && PreferenceLevel.getPreferenceLevel(Integer.parseInt(levelId)).isHard();
	}
	
	public boolean isBuildingDisabled(int idx) {
		String levelId = getBldgPrefLevels(idx);
		return !getAllowHardPrefs() && !Preference.BLANK_PREF_VALUE.equals(levelId) && PreferenceLevel.getPreferenceLevel(Integer.parseInt(levelId)).isHard();
	}
	
	public boolean isDatePatternDisabled(int idx) {
		String levelId = getDatePatternPrefLevels(idx);
		return !getAllowHardPrefs() && !Preference.BLANK_PREF_VALUE.equals(levelId) && PreferenceLevel.getPreferenceLevel(Integer.parseInt(levelId)).isHard();
	}
	
	public boolean isDistPrefDisabled(int idx) {
		String levelId = getDistPrefLevels(idx);
		return !getAllowHardPrefs() && !Preference.BLANK_PREF_VALUE.equals(levelId) && PreferenceLevel.getPreferenceLevel(Integer.parseInt(levelId)).isHard();
	}
	
	public boolean isAttributePrefDisabled(int idx) {
		String levelId = getAttributePrefLevels(idx);
		return !getAllowHardPrefs() && !Preference.BLANK_PREF_VALUE.equals(levelId) && PreferenceLevel.getPreferenceLevel(Integer.parseInt(levelId)).isHard();
	}
	
	public boolean isCoursePrefDisabled(int idx) {
		String levelId = getCoursePrefLevels(idx);
		return !getAllowHardPrefs() && !Preference.BLANK_PREF_VALUE.equals(levelId) && PreferenceLevel.getPreferenceLevel(Integer.parseInt(levelId)).isHard();
	}
	
	public boolean isInstructorPrefDisabled(int idx) {
		String levelId = getInstructorPrefLevels(idx);
		return !getAllowHardPrefs() && !Preference.BLANK_PREF_VALUE.equals(levelId) && PreferenceLevel.getPreferenceLevel(Integer.parseInt(levelId)).isHard();
	}
	
    /**
     * @return Returns the roomGroupLevels.
     */
    public String getRoomGroupLevels(int key) {
        return roomGroupLevels.get(key).toString();
    }
	
    /**
     * @param roomGroup levels The roomGroup Levels to set.
     */
    public void setRoomGroupLevels(int key, String value) {
    	this.roomGroupLevels.set(key, value);
    }
	
    /**
     * @return Returns the roomGroupPrefs.
     */
    public String getRoomGroups(int key) {
        return roomGroups.get(key).toString();
    }

	public void setRoomGroupLevels(List<String> roomGroupLevels) {
		this.roomGroupLevels = roomGroupLevels;
	}

	/**
     * @return Returns the bldgPrefs.
     */
    public List<String> getBldgPrefs() {
        return bldgPrefs;
    }
    /**
     * @return Returns the bldgPrefs.
     */
    public String getBldgPrefs(int key) {
        return bldgPrefs.get(key).toString();
    }
    /**
     * @param bldgPrefs The bldgPrefs to set.
     */
    public void setBldgPrefs(int key, String value) {
        this.bldgPrefs.set(key, value);
    }
    /**
     * @param bldgPrefs The bldgPrefs to set.
     */
    public void setBldgPrefs(List<String> bldgPrefs) {
        this.bldgPrefs = bldgPrefs;
    }
    
	/**
     * @return Returns the distPrefs.
     */
    public List<String> getDistPrefs() {
        return distPrefs;
    }
    /**
     * @return Returns the distPrefs.
     */
    public String getDistPrefs(int key) {
        return distPrefs.get(key).toString();
    }
    /**
     * @param distPrefs The distPrefs to set.
     */
    public void setDistPrefs(int key, String value) {
        this.distPrefs.set(key, value);
    }
    /**
     * @param distPrefs The distPrefs to set.
     */
    public void setDistPrefs(List<String> distPrefs) {
        this.distPrefs = distPrefs;
    }

	/**
     * @return Returns the coursePrefs.
     */
    public List<String> getCoursePrefs() {
        return coursePrefs;
    }
    /**
     * @return Returns the coursePrefs.
     */
    public String getCoursePrefs(int key) {
        return coursePrefs.get(key).toString();
    }
    /**
     * @param coursePrefs The coursePrefs to set.
     */
    public void setCoursePrefs(int key, String value) {
        this.coursePrefs.set(key, value);
    }
    /**
     * @param coursePrefs The coursePrefs to set.
     */
    public void setCoursePrefs(List<String> coursePrefs) {
        this.coursePrefs = coursePrefs;
    }
    
	/**
     * @return Returns the instructorPrefs.
     */
    public List<String> getInstructorPrefs() {
        return instructorPrefs;
    }
    /**
     * @return Returns the instructorPrefs.
     */
    public String getInstructorPrefs(int key) {
        return instructorPrefs.get(key).toString();
    }
    /**
     * @param instructorPrefs The instructorPrefs to set.
     */
    public void setInstructorPrefs(int key, String value) {
        this.instructorPrefs.set(key, value);
    }
    /**
     * @param instructorPrefs The instructorPrefs to set.
     */
    public void setInstructorPrefs(List<String> instructorPrefs) {
        this.instructorPrefs = instructorPrefs;
    }
    
	/**
     * @return Returns the attributePrefs.
     */
    public List<String> getAttributePrefs() {
        return attributePrefs;
    }
    /**
     * @return Returns the attributePrefs.
     */
    public String getAttributePrefs(int key) {
        return attributePrefs.get(key).toString();
    }
    /**
     * @param attributePrefs The attributePrefs to set.
     */
    public void setAttributePrefs(int key, String value) {
        this.attributePrefs.set(key, value);
    }
    /**
     * @param attributePrefs The attributePrefs to set.
     */
    public void setAttributePrefs(List<String> attributePrefs) {
        this.attributePrefs = attributePrefs;
    }

    /**
     * @return Returns the roomPrefs.
     */
    public List<String> getRoomPrefs() {
        return roomPrefs;
    }
    /**
     * @return Returns the roomPrefs.
     */
    public String getRoomPrefs(int key) {
        Debug.debug("getting room pref: " + key);
        return roomPrefs.get(key).toString();
    }
    /**
     * @param roomPrefs The roomPrefs to set.
     */
    public void setRoomPrefs(int key, String value) {
        Debug.debug("setting room pref: " + key);
        this.roomPrefs.set(key, value);
    }
    /**
     * @param roomPrefs The roomPrefs to set.
     */
    public void setRoomPrefs(List<String> roomPrefs) {
        this.roomPrefs = roomPrefs;
    }
    
    /**
     * @return Returns the roomFeaturePrefs.
     */
    public List<String> getRoomFeaturePrefs() {
        return roomFeaturePrefs;
    }
    /**
     * @return Returns the roomFeaturePrefs.
     */
    public String getRoomFeaturePrefs(int key) {
        return roomFeaturePrefs.get(key).toString();
    }
    /**
     * @param roomFeaturePrefs The roomFeaturePrefs to set.
     */
    public void setRoomFeaturePrefs(int key, String value) {
        this.roomFeaturePrefs.set(key, value);
    }
    /**
     * @param roomFeaturePrefs The roomFeaturePrefs to set.
     */
    public void setRoomFeaturePrefs(List<String> roomFeaturePrefs) {
        this.roomFeaturePrefs = roomFeaturePrefs;
    }
    
    /**
     * @return Returns the roomPrefLevels.
     */
    public List<String> getRoomPrefLevels() {
        return roomPrefLevels;
    }
    /**
     * @return Returns the roomPrefLevels.
     */
    public String getRoomPrefLevels(int key) {
        return roomPrefLevels.get(key).toString();
    }
    /**
     * @param roomPrefs The roomPrefLevels to set.
     */
    public void setRoomPrefLevels(int key, String value) {
        this.roomPrefLevels.set(key, value);
    }
    /**
     * @param roomPrefs The roomPrefLevels to set.
     */
    public void setRoomPrefLevels(List<String> roomPrefLevels) {
        this.roomPrefLevels = roomPrefLevels;
    }
    
    /**
     * @return Returns the bldgPrefLevels.
     */
    public List<String> getBldgPrefLevels() {
        return bldgPrefLevels;
    }
    /**
     * @return Returns the bldgPrefLevels.
     */
    public String getBldgPrefLevels(int key) {
        return bldgPrefLevels.get(key).toString();
    }
    /**
     * @param bldgPrefs The bldgPrefLevels to set.
     */
    public void setBldgPrefLevels(int key, String value) {
        this.bldgPrefLevels.set(key, value);
    }
    /**
     * @param bldgPrefs The bldgPrefLevels to set.
     */
    public void setBldgPrefLevels(List<String> bldgPrefLevels) {
        this.bldgPrefLevels = bldgPrefLevels;
    }
    
    /**
     * @return Returns the distPrefLevels.
     */
    public List<String> getDistPrefLevels() {
        return distPrefLevels;
    }
    /**
     * @return Returns the distPrefLevels.
     */
    public String getDistPrefLevels(int key) {
        return distPrefLevels.get(key).toString();
    }
    /**
     * @param distPrefs The distPrefLevels to set.
     */
    public void setDistPrefLevels(int key, String value) {
        this.distPrefLevels.set(key, value);
    }
    /**
     * @param distPrefs The distPrefLevels to set.
     */
    public void setDistPrefLevels(List<String> distPrefLevels) {
        this.distPrefLevels = distPrefLevels;
    }
    
    /**
     * @return Returns the coursePrefLevels.
     */
    public List<String> getCoursePrefLevels() {
        return coursePrefLevels;
    }
    /**
     * @return Returns the coursePrefLevels.
     */
    public String getCoursePrefLevels(int key) {
        return coursePrefLevels.get(key).toString();
    }
    /**
     * @param coursePrefs The coursePrefLevels to set.
     */
    public void setCoursePrefLevels(int key, String value) {
        this.coursePrefLevels.set(key, value);
    }
    /**
     * @param coursePrefs The coursePrefLevels to set.
     */
    public void setCoursePrefLevels(List<String> coursePrefLevels) {
        this.coursePrefLevels = coursePrefLevels;
    }
    
    /**
     * @return Returns the instructorPrefLevels.
     */
    public List<String> getInstructorPrefLevels() {
        return instructorPrefLevels;
    }
    /**
     * @return Returns the instructorPrefLevels.
     */
    public String getInstructorPrefLevels(int key) {
        return instructorPrefLevels.get(key).toString();
    }
    /**
     * @param instructorPrefs The instructorPrefLevels to set.
     */
    public void setInstructorPrefLevels(int key, String value) {
        this.instructorPrefLevels.set(key, value);
    }
    /**
     * @param instructorPrefs The instructorPrefLevels to set.
     */
    public void setInstructorPrefLevels(List<String> instructorPrefLevels) {
        this.instructorPrefLevels = instructorPrefLevels;
    }

    /**
     * @return Returns the attributePrefLevels.
     */
    public List<String> getAttributePrefLevels() {
        return attributePrefLevels;
    }
    /**
     * @return Returns the attributePrefLevels.
     */
    public String getAttributePrefLevels(int key) {
        return attributePrefLevels.get(key).toString();
    }
    /**
     * @param attributePrefs The attributePrefLevels to set.
     */
    public void setAttributePrefLevels(int key, String value) {
        this.attributePrefLevels.set(key, value);
    }
    /**
     * @param attributePrefs The attributePrefLevels to set.
     */
    public void setAttributePrefLevels(List<String> attributePrefLevels) {
        this.attributePrefLevels = attributePrefLevels;
    }
    
    /**
     * @return Returns the roomFeaturePrefLevels.
     */
    public List<String> getRoomFeaturePrefLevels() {
        return roomFeaturePrefLevels;
    }
    /**
     * @return Returns the roomFeaturePrefLevels.
     */
    public String getRoomFeaturePrefLevels(int key) {
        return roomFeaturePrefLevels.get(key).toString();
    }
    /**
     * @param roomFeaturePrefs The roomFeaturePrefLevels to set.
     */
    public void setRoomFeaturePrefLevels(int key, String value) {
        this.roomFeaturePrefLevels.set(key, value);
    }
    /**
     * @param roomFeaturePrefs The roomFeaturePrefLevels to set.
     */
    public void setRoomFeaturePrefLevels(List<String> roomFeaturePrefLevels) {
        this.roomFeaturePrefLevels = roomFeaturePrefLevels;
    }

    
	public List<String> getDatePatternPrefs() {
		return datePatternPrefs;
	}
	
	public String getDatePatternPrefs(int key) {
		return datePatternPrefs.get(key).toString();
	}

	public void setDatePatternPrefs(int key, String value) {
		this.datePatternPrefs.set(key, value);
	}
	public void setDatePatternPrefs(List<String> datePatternPrefs) {
		this.datePatternPrefs = datePatternPrefs;
	}

	public List<String> getDatePatternPrefLevels() {
		return datePatternPrefLevels;
	}
	
	public String getDatePatternPrefLevels(int key) {
		return datePatternPrefLevels.get(key).toString();
	}

	public void setDatePatternPrefLevels(List<String> datePatternPrefLevels) {
		this.datePatternPrefLevels = datePatternPrefLevels;
	}
	
	public void setDatePatternPrefLevels(int key, String value) {
		this.datePatternPrefLevels.set(key, value);
	}

	/**
     * Add a room preference to the existing list
     * @param roomPref Room Id
     * @param level Preference Level
     */
    public void addToRoomPrefs(String roomPref, String level) {
        this.roomPrefs.add(roomPref);
        this.roomPrefLevels.add(level);
    }

    /**
     * Add a room feature preference to the existing list
     * @param roomFeatPref Room Feature Id
     * @param level Preference Level
     */
    public void addToRoomFeatPrefs(String roomFeatPref, String level) {
        this.roomFeaturePrefs.add(roomFeatPref);
        this.roomFeaturePrefLevels.add(level);
    }
    
    /**
     * Add a room group preference to the existing list
     * @param roomGroup Room Feature Id
     * @param level Preference Level
     */
    public void addToRoomGroups(String roomGroup, String level) {
    	this.roomGroups.add(roomGroup);
    	this.roomGroupLevels.add(level);
    }

    /**
     * Add a building preference to the existing list
     * @param bldgPref Building Id
     * @param level Preference Level
     */
    public void addToBldgPrefs(String bldgPref, String level) {
        this.bldgPrefs.add(bldgPref);
        this.bldgPrefLevels.add(level);
    }
    
    /**
     * Add a distribution preference to the existing list
     * @param distPref Dist. pref Id
     * @param level Preference Level
     */
    public void addToDistPrefs(String distPref, String level) {
        this.distPrefs.add(distPref);
        this.distPrefLevels.add(level);
    }
    
    /**
     * Add a course preference to the existing list
     * @param coursePref Course pref Id
     * @param level Preference Level
     */
    public void addToCoursePrefs(String coursePref, String level) {
        this.coursePrefs.add(coursePref);
        this.coursePrefLevels.add(level);
    }
    
    /**
     * Add a instructor preference to the existing list
     * @param instructorPref Course pref Id
     * @param level Preference Level
     */
    public void addToInstructorPrefs(String instructorPref, String level) {
        this.instructorPrefs.add(instructorPref);
        this.instructorPrefLevels.add(level);
    }

    /**
     * Add a attribute preference to the existing list
     * @param attributePref Attribute pref Id
     * @param level Preference Level
     */
    public void addToAttributePrefs(String attributePref, String level) {
        this.attributePrefs.add(attributePref);
        this.attributePrefLevels.add(level);
    }
    
    /**
     * Add a date pattern preference to the existing list
     * @param datePatternPref Date pattern pref Id
     * @param level Preference Level
     */
    public void addToDatePatternPrefs(String datePatternPref, String level) {
        this.datePatternPrefs.add(datePatternPref);
        this.datePatternPrefLevels.add(level);
    }
    
	public void sortDatePatternPrefs(List<String> prefs, List<String> prefLevels, List<DatePattern> patterns) {
		if (prefs == null) return;
		if (prefs.size() == patterns.size()) {
			Collections.sort(patterns); //, new DatePattenNameComparator()
			List<String> newPrefs = new ArrayList<String>();
			List<String> newPrefLevels = new ArrayList<String>();
			newPrefLevels.addAll(prefLevels);
			newPrefs.addAll(prefs);
			for (int i = 0; i < newPrefs.size(); i++) {
				String ith_pattern = patterns.get(i).getUniqueId().toString();
				int indexOfPatternInPrefs = prefs.indexOf(ith_pattern);

				newPrefs.set(i, ith_pattern);
				newPrefLevels.set(i, prefLevels.get(indexOfPatternInPrefs));
			}
			prefs.clear();
			prefLevels.clear();
			prefs.addAll(newPrefs);
			prefLevels.addAll(newPrefLevels);
		}
	}

    public void addBlankPrefRows() {
        for (int i=0; i<Constants.PREF_ROWS_ADDED; i++) {
	        addToBldgPrefs(Preference.BLANK_PREF_VALUE, Preference.BLANK_PREF_VALUE);
	        addToRoomPrefs(Preference.BLANK_PREF_VALUE, Preference.BLANK_PREF_VALUE);
	        addToRoomFeatPrefs(Preference.BLANK_PREF_VALUE, Preference.BLANK_PREF_VALUE);
	        addToRoomGroups(Preference.BLANK_PREF_VALUE, Preference.BLANK_PREF_VALUE);
	        addToDistPrefs(Preference.BLANK_PREF_VALUE, Preference.BLANK_PREF_VALUE);
	        addToCoursePrefs(Preference.BLANK_PREF_VALUE, Preference.BLANK_PREF_VALUE);
	        addToAttributePrefs(Preference.BLANK_PREF_VALUE, Preference.BLANK_PREF_VALUE);
	        addToInstructorPrefs(Preference.BLANK_PREF_VALUE, Preference.BLANK_PREF_VALUE);
        }
    }
    
    /**
     * Clears all preference lists
     */
    public void clearPrefs() {
        this.timePatterns.clear();
        this.roomGroups.clear();
        this.bldgPrefs.clear();
        this.bldgPrefLevels.clear();
        this.distPrefs.clear();
        this.distPrefLevels.clear();
        this.roomPrefs.clear();
        this.roomPrefLevels.clear();
        this.roomFeaturePrefs.clear();
        this.roomFeaturePrefLevels.clear();
        this.datePatternPrefs.clear();
        this.datePatternPrefLevels.clear();
        this.coursePrefs.clear();
        this.coursePrefLevels.clear();
        this.instructorPrefs.clear();
        this.instructorPrefLevels.clear();
        this.attributePrefs.clear();
        this.attributePrefLevels.clear();
    }
    
    public String getNextId() { return nextId; }
    public void setNextId(String nextId) { this.nextId = nextId; }
    public String getPreviousId() { return previousId; }
    public void setPreviousId(String previousId) { this.previousId = previousId; }
    
    public boolean getAllowHardPrefs() { return allowHardPrefs; }
    public void setAllowHardPrefs(boolean allowHardPrefs) { this.allowHardPrefs = allowHardPrefs; }
    
    public boolean getHasNotAvailable() { return hasNotAvailable; }
    public void setHasNotAvailable(boolean hasNotAvailable) { this.hasNotAvailable = hasNotAvailable; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

	@Override
	public void validate(UniTimeAction action) {
        List<String> lst = getRoomGroups();
        if (!checkPrefs(lst)) {
        	action.addFieldError("roomGroups", MSG.errorInvalidRoomGroup());
        }

        if (!checkPrefLevels(getRoomGroupLevels(), lst)) {
        	action.addFieldError("roomGroups", MSG.errorInvalidRoomGroupLevel());
        }
        
        lst = getBldgPrefs();
        if (!checkPrefs(lst)) {
            action.addFieldError("bldgPrefs", MSG.errorInvalidBuildingPreference());
        }

        if (!checkPrefLevels(getBldgPrefLevels(), lst)) {
            action.addFieldError("bldgPrefs", MSG.errorInvalidBuildingPreferenceLevel());
        }
            
        lst = getRoomPrefs();
        if(!checkPrefs(lst)) {
            action.addFieldError("roomPrefs", MSG.errorInvalidRoomPreference());
        }

        if (!checkPrefLevels(getRoomPrefLevels(), lst)) {
            action.addFieldError("roomPrefs", MSG.errorInvalidRoomPreferenceLevel());
        }
        
        lst = getRoomFeaturePrefs();
        if (!checkPrefs(lst)) {
            action.addFieldError("roomFeaturePrefs", MSG.errorInvalidRoomFeaturePreference());
        }

        if (!checkPrefLevels(getRoomFeaturePrefLevels(), lst)) {
            action.addFieldError("roomFeaturePrefs", MSG.errorInvalidRoomFeaturePreferenceLevel());
        }
        
        lst = getDistPrefs();
        if (!checkPrefs(lst)) {
            action.addFieldError("distPrefs", MSG.errorInvalidDistributionPreference());
        }

        if (!checkPrefLevels(getDistPrefLevels(), lst)) {
            action.addFieldError("distPrefs",  MSG.errorInvalidDistributionPreferenceLevel());
        }
        
        lst = getCoursePrefs();
        if (!checkPrefs(lst)) {
            action.addFieldError("coursePrefs", MSG.errorInvalidCoursePreference());
        }

        if (!checkPrefLevels(getCoursePrefLevels(), lst)) {
            action.addFieldError("coursePrefs", MSG.errorInvalidCoursePreferenceLevel());
        }
        
        lst = getInstructorPrefs();
        if (!checkPrefs(lst)) {
            action.addFieldError("instructorPrefs", MSG.errorInvalidInstructorPreference());
        }

        if (!checkPrefLevels(getInstructorPrefLevels(), lst)) {
            action.addFieldError("instructorPrefs", MSG.errorInvalidInstructorPreferenceLevel());
        }
        
        lst = getAttributePrefs();
        if (!checkPrefs(lst)) {
            action.addFieldError("attributePrefs", MSG.errorInvalidAttributePreference());
        }

        if (!checkPrefLevels(getAttributePrefLevels(), lst)) {
            action.addFieldError("attributePrefs",  MSG.errorInvalidAttributePreferenceLevel());
        }


        for (int i=0;i<getTimePatterns().size();i++) {
        	if (action.getRequest().getParameter("p"+i+"_hour")!=null) {
        		boolean daySelected = false;
        		for (int j=0;j<Constants.DAY_CODES.length;j++)
        			if (action.getRequest().getParameter("p"+i+"_d"+j)!=null)
        				daySelected = true;
        		if (!daySelected) {
        			action.addFieldError("timePrefs", "No day is selected in time preferences.");
        			break;
        		}
        		if ("".equals(action.getRequest().getParameter("p"+i+"_hour"))) {
        			action.addFieldError("timePrefs", "No time is selected in time preferences.");
        			break;
        		}
        		if ("".equals(action.getRequest().getParameter("p"+i+"_min"))) {
        			action.addFieldError("timePrefs", "No time is selected in time preferences.");
        			break;
        		}
        		if ("".equals(action.getRequest().getParameter("p"+i+"_morn"))) {
        			action.addFieldError("timePrefs", "No time is selected in time preferences.");
        			break;
        		}
        	}
        }
	}

	@Override
	public void reset() {
		op= "";
        timePattern = null;
        timePatterns = new ArrayList<String>();
        availableTimePatterns = new ArrayList<TimePattern>();
        roomPrefs = new ArrayList<String>();
        roomPrefLevels = new ArrayList<String>();
        bldgPrefs = new ArrayList<String>();
        bldgPrefLevels = new ArrayList<String>();
        roomFeaturePrefs = new ArrayList<String>();
        roomFeaturePrefLevels = new ArrayList<String>();
        roomGroups = new ArrayList<String>();
        roomGroupLevels = new ArrayList<String>();
        distPrefs = new ArrayList<String>();
        distPrefLevels = new ArrayList<String>();
        datePatternPrefs = new ArrayList<String>();
        datePatternPrefLevels = new ArrayList<String>();
        coursePrefs = new ArrayList<String>();
        coursePrefLevels = new ArrayList<String>();
        attributePrefs = new ArrayList<String>();
        attributePrefLevels = new ArrayList<String>();
        instructorPrefs = new ArrayList<String>();
        instructorPrefLevels = new ArrayList<String>();
        nextId = previousId = null;
        allowHardPrefs = true;
        hasNotAvailable = false;
        addBlankPrefRows();
        availability = null;
	}
	
	public boolean hasRequiredDatePatternPref() {
		PreferenceLevel req = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired);
		for (Object prefId: datePatternPrefLevels) {
			if (req.getUniqueId().toString().equals(prefId)) return true;
		}
		return false;
	}
}
