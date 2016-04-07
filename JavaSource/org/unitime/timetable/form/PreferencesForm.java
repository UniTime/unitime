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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/**
 * Superclass for implementing Preferences
 * 
 * @author Heston Fernandes, Tomas Muller, Zuzana Mullerova
 */
public class PreferencesForm extends ActionForm {

	private static final long serialVersionUID = -3578647598790726006L;
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	
	// --------------------------------------------------------- Instance Variables
    
	protected String op;
    protected List timePatterns;
    protected List roomGroups;
    protected List roomGroupLevels;
    protected List roomPrefs;
    protected List roomPrefLevels;
    protected List bldgPrefs;
    protected List bldgPrefLevels;
    protected List roomFeaturePrefs;
    protected List roomFeaturePrefLevels;
    protected String timePattern;
    protected List availableTimePatterns;
    protected List distPrefs;
    protected List distPrefLevels;
    protected List datePatternPrefs;
    protected List datePatternPrefLevels;
    protected List coursePrefs;
    protected List coursePrefLevels;
    protected List attributePrefs;
    protected List attributePrefLevels;
    protected String availability;
    
    private String nextId;
    private String previousId;
    
    protected boolean allowHardPrefs;
    
    private boolean hasNotAvailable;

    // --------------------------------------------------------- Classes

    /** Factory to create dynamic list element for Preference */
    protected DynamicListObjectFactory factoryPref = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

    /** Factory to create dynamic list element for Preference Level */
    protected DynamicListObjectFactory factoryPrefLevel = new DynamicListObjectFactory() {
        public Object create() {
            return new String(PreferenceLevel.PREF_LEVEL_NEUTRAL);
        }
    };

    /** Factory to create dynamic list element for Time Pattern */
    protected DynamicListObjectFactory factoryPattern = new DynamicListObjectFactory() {
        public Object create() {
            return new String("-1");
        }
    };


    // --------------------------------------------------------- Methods

    /** 
     * Method validate
     * @param mapping
     * @param request
     * @return ActionErrors
     */
    public ActionErrors validate(
        ActionMapping mapping,
        HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();

        List lst = getRoomGroups();
        if(!checkPrefs(lst)) {
            errors.add("roomGroups", 
                    new ActionMessage(
                            "errors.generic",
                            MSG.errorInvalidRoomGroup()) );
        }

        if(!checkPrefLevels(getRoomGroupLevels(), lst))
        {
            errors.add("roomGroups", 
                    new ActionMessage(
                            "errors.generic", 
                            MSG.errorInvalidRoomGroupLevel()) );
        } 
        
        lst = getBldgPrefs();
        if(!checkPrefs(lst)) {
            errors.add("bldgPrefs", 
                    new ActionMessage(
                            "errors.generic", 
                            MSG.errorInvalidBuildingPreference()) );
        }

        if(!checkPrefLevels(getBldgPrefLevels(), lst))
        {
            errors.add("bldgPrefs", 
                    new ActionMessage(
                            "errors.generic", 
                            MSG.errorInvalidBuildingPreferenceLevel()) );
        }            
            
        lst = getRoomPrefs();
        if(!checkPrefs(lst)) {
            errors.add("roomPrefs", 
                    new ActionMessage(
                            "errors.generic", 
                            MSG.errorInvalidRoomPreference()) );
        }

        if(!checkPrefLevels(getRoomPrefLevels(), lst))
        {
            errors.add("roomPrefs", 
                    new ActionMessage(
                            "errors.generic", 
                            MSG.errorInvalidRoomPreferenceLevel()) );
        }            
            
        lst = getRoomFeaturePrefs();
        if(!checkPrefs(lst)) {
            errors.add("roomFeaturePrefs", 
                    new ActionMessage(
                            "errors.generic", 
                            MSG.errorInvalidRoomFeaturePreference()) );
        }

        if(!checkPrefLevels(getRoomFeaturePrefLevels(), lst))
        {
            errors.add("roomFeaturePrefs", 
                    new ActionMessage(
                            "errors.generic",
                            MSG.errorInvalidRoomFeaturePreferenceLevel()) );
        }
        
        lst = getDistPrefs();
        if(!checkPrefs(lst)) {
            errors.add("distPrefs", 
                    new ActionMessage(
                            "errors.generic", 
                            MSG.errorInvalidDistributionPreference()) );
        }

        if(!checkPrefLevels(getDistPrefLevels(), lst))
        {
            errors.add("distPrefs", 
                    new ActionMessage(
                            "errors.generic",
                            MSG.errorInvalidDistributionPreferenceLevel()) );
        }
        
        lst = getCoursePrefs();
        if(!checkPrefs(lst)) {
            errors.add("coursePrefs", 
                    new ActionMessage(
                            "errors.generic", 
                            MSG.errorInvalidCoursePreference()) );
        }

        if(!checkPrefLevels(getCoursePrefLevels(), lst))
        {
            errors.add("coursePrefs", 
                    new ActionMessage(
                            "errors.generic",
                            MSG.errorInvalidCoursePreferenceLevel()) );
        }
        
        lst = getAttributePrefs();
        if(!checkPrefs(lst)) {
            errors.add("attributePrefs", 
                    new ActionMessage(
                            "errors.generic", 
                            MSG.errorInvalidAttributePreference()) );
        }

        if(!checkPrefLevels(getAttributePrefLevels(), lst))
        {
            errors.add("attributePrefs", 
                    new ActionMessage(
                            "errors.generic",
                            MSG.errorInvalidAttributePreferenceLevel()) );
        }


        for (int i=0;i<getTimePatterns().size();i++) {
        	if (request.getParameter("p"+i+"_hour")!=null) {
        		boolean daySelected = false;
        		for (int j=0;j<Constants.DAY_CODES.length;j++)
        			if (request.getParameter("p"+i+"_d"+j)!=null)
        				daySelected = true;
        		if (!daySelected) {
        			errors.add("timePrefs", 
        					new ActionMessage(
        							"errors.generic", 
                        			"No day is selected in time preferences.") );
        			break;
        		}
        		if ("".equals(request.getParameter("p"+i+"_hour"))) {
        			errors.add("timePrefs", 
        					new ActionMessage(
        							"errors.generic", 
                        			"No time is selected in time preferences.") );
        			break;
        		}
        		if ("".equals(request.getParameter("p"+i+"_min"))) {
        			errors.add("timePrefs", 
        					new ActionMessage(
        							"errors.generic", 
        							"No time is selected in time preferences.") );
        			break;
        		}
        		if ("".equals(request.getParameter("p"+i+"_morn"))) {
        			errors.add("timePrefs", 
        					new ActionMessage(
        							"errors.generic", 
        							"No time is selected in time preferences.") );
        			break;
        		}
        	}
        }
            
        return errors;        
    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        op= "";
        timePattern = null;
        timePatterns = DynamicList.getInstance(new ArrayList(), factoryPattern);
        availableTimePatterns = DynamicList.getInstance(new ArrayList(), factoryPattern);
        roomPrefs = DynamicList.getInstance(new ArrayList(), factoryPref);
        roomPrefLevels = DynamicList.getInstance(new ArrayList(), factoryPrefLevel);
        bldgPrefs = DynamicList.getInstance(new ArrayList(), factoryPref);
        bldgPrefLevels = DynamicList.getInstance(new ArrayList(), factoryPrefLevel);
        roomFeaturePrefs = DynamicList.getInstance(new ArrayList(), factoryPref);
        roomFeaturePrefLevels = DynamicList.getInstance(new ArrayList(), factoryPrefLevel);
        roomGroups = DynamicList.getInstance(new ArrayList(), factoryPref);
        roomGroupLevels = DynamicList.getInstance(new ArrayList(), factoryPrefLevel);
        distPrefs = DynamicList.getInstance(new ArrayList(), factoryPref);
        distPrefLevels = DynamicList.getInstance(new ArrayList(), factoryPrefLevel);
        datePatternPrefs = DynamicList.getInstance(new ArrayList(), factoryPref);
        datePatternPrefLevels = DynamicList.getInstance(new ArrayList(), factoryPrefLevel);
        coursePrefs = DynamicList.getInstance(new ArrayList(), factoryPref);
        coursePrefLevels = DynamicList.getInstance(new ArrayList(), factoryPrefLevel);
        attributePrefs = DynamicList.getInstance(new ArrayList(), factoryPref);
        attributePrefLevels = DynamicList.getInstance(new ArrayList(), factoryPrefLevel);
        nextId = previousId = null;
        allowHardPrefs = true;
        hasNotAvailable = false;
        addBlankPrefRows();
        availability = null;
    }

    /**
     * Checks that there are no duplicates and that all prior prefs have a value
     * @param lst List of values
     * @return true if checks ok, false otherwise
     */
    public boolean checkPrefs(List lst) {
        
        HashMap map = new HashMap();
        for(int i=0; i<lst.size(); i++) {
            String value = ((String) lst.get(i));
            // No selection made - ignore            
            if( value==null || value.trim().equals(Preference.BLANK_PREF_VALUE)) {
                continue;
            }
            
            // Duplicate selection made
            if(map.get(value.trim())!=null) {
                lst.set(i, Preference.BLANK_PREF_VALUE);
                return false;
            }
            map.put(value, value);
        }
        return true;
    }
    
    /**
     * Checks that pref levels are selected
     * @param lst List of pref levels
     * @return true if checks ok, false otherwise
     */
    public boolean checkPrefLevels(List lst, List prefList) {
        
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
    public List getTimePatterns() {
        return (timePatterns==null?new Vector():timePatterns);
    }

    /**
     * @param timePattern The timePattern to set.
     */
    public void setTimePatterns(List timePatterns) {
        this.timePatterns = timePatterns;
    }
    
    public List getAvailableTimePatterns() {
        return availableTimePatterns;
    }
    
    public boolean getCanChooseTimePattern() {
    	return availableTimePatterns!=null;
    }

    public List getAvailableNotSelectedTimePatterns() {
    	if (timePatterns==null || timePatterns.isEmpty()) return getAvailableTimePatterns();
    	Vector ret = new Vector();
    	for (Iterator i=availableTimePatterns.iterator();i.hasNext();) {
    		TimePattern pattern = (TimePattern)i.next();
    		if (!pattern.getTimePatternModel().isExactTime() && timePatterns.contains(pattern.getUniqueId().toString())) continue;
    		ret.add(pattern);
    	}
        return ret;
    }

    public void setAvailableTimePatterns(List availableTimePatterns) {
        this.availableTimePatterns = availableTimePatterns;
    }

    public String getTimePattern() {
    	return timePattern;
    }
    
    public void setTimePattern(String timePattern) {
    	this.timePattern = timePattern;
    }
    
    public List getRoomGroups() {
		return roomGroups;
	}

	public void setRoomGroups(List roomGroups) {
		this.roomGroups = roomGroups;
	}
    /**
     * @param roomGroups The roomGroups to set.
     */
    public void setRoomGroups(int key, Object value) {
        Debug.debug("setting room group: " + key);
        this.roomGroups.set(key, value);
    }
	
	public List getRoomGroupLevels() {
		return roomGroupLevels;
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
    public void setRoomGroupLevels(int key, Object value) {
    	this.roomGroupLevels.set(key, value);
    }
	
    /**
     * @return Returns the roomGroupPrefs.
     */
    public String getRoomGroups(int key) {
        return roomGroups.get(key).toString();
    }

	public void setRoomGroupLevels(List roomGroupLevels) {
		this.roomGroupLevels = roomGroupLevels;
	}

	/**
     * @return Returns the bldgPrefs.
     */
    public List getBldgPrefs() {
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
    public void setBldgPrefs(int key, Object value) {
        this.bldgPrefs.set(key, value);
    }
    /**
     * @param bldgPrefs The bldgPrefs to set.
     */
    public void setBldgPrefs(List bldgPrefs) {
        this.bldgPrefs = bldgPrefs;
    }
    
	/**
     * @return Returns the distPrefs.
     */
    public List getDistPrefs() {
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
    public void setDistPrefs(int key, Object value) {
        this.distPrefs.set(key, value);
    }
    /**
     * @param distPrefs The distPrefs to set.
     */
    public void setDistPrefs(List distPrefs) {
        this.distPrefs = distPrefs;
    }

	/**
     * @return Returns the coursePrefs.
     */
    public List getCoursePrefs() {
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
    public void setCoursePrefs(int key, Object value) {
        this.coursePrefs.set(key, value);
    }
    /**
     * @param coursePrefs The coursePrefs to set.
     */
    public void setCoursePrefs(List coursePrefs) {
        this.coursePrefs = coursePrefs;
    }
    
	/**
     * @return Returns the attributePrefs.
     */
    public List getAttributePrefs() {
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
    public void setAttributePrefs(int key, Object value) {
        this.attributePrefs.set(key, value);
    }
    /**
     * @param attributePrefs The attributePrefs to set.
     */
    public void setAttributePrefs(List attributePrefs) {
        this.attributePrefs = attributePrefs;
    }

    /**
     * @return Returns the roomPrefs.
     */
    public List getRoomPrefs() {
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
    public void setRoomPrefs(int key, Object value) {
        Debug.debug("setting room pref: " + key);
        this.roomPrefs.set(key, value);
    }
    /**
     * @param roomPrefs The roomPrefs to set.
     */
    public void setRoomPrefs(List roomPrefs) {
        this.roomPrefs = roomPrefs;
    }
    
    /**
     * @return Returns the roomFeaturePrefs.
     */
    public List getRoomFeaturePrefs() {
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
    public void setRoomFeaturePrefs(int key, Object value) {
        this.roomFeaturePrefs.set(key, value);
    }
    /**
     * @param roomFeaturePrefs The roomFeaturePrefs to set.
     */
    public void setRoomFeaturePrefs(List roomFeaturePrefs) {
        this.roomFeaturePrefs = roomFeaturePrefs;
    }
    
    /**
     * @return Returns the roomPrefLevels.
     */
    public List getRoomPrefLevels() {
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
    public void setRoomPrefLevels(int key, Object value) {
        this.roomPrefLevels.set(key, value);
    }
    /**
     * @param roomPrefs The roomPrefLevels to set.
     */
    public void setRoomPrefLevels(List roomPrefLevels) {
        this.roomPrefLevels = roomPrefLevels;
    }
    
    /**
     * @return Returns the bldgPrefLevels.
     */
    public List getBldgPrefLevels() {
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
    public void setBldgPrefLevels(int key, Object value) {
        this.bldgPrefLevels.set(key, value);
    }
    /**
     * @param bldgPrefs The bldgPrefLevels to set.
     */
    public void setBldgPrefLevels(List bldgPrefLevels) {
        this.bldgPrefLevels = bldgPrefLevels;
    }
    
    /**
     * @return Returns the distPrefLevels.
     */
    public List getDistPrefLevels() {
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
    public void setDistPrefLevels(int key, Object value) {
        this.distPrefLevels.set(key, value);
    }
    /**
     * @param distPrefs The distPrefLevels to set.
     */
    public void setDistPrefLevels(List distPrefLevels) {
        this.distPrefLevels = distPrefLevels;
    }
    
    /**
     * @return Returns the coursePrefLevels.
     */
    public List getCoursePrefLevels() {
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
    public void setCoursePrefLevels(int key, Object value) {
        this.coursePrefLevels.set(key, value);
    }
    /**
     * @param coursePrefs The coursePrefLevels to set.
     */
    public void setCoursePrefLevels(List coursePrefLevels) {
        this.coursePrefLevels = coursePrefLevels;
    }

    /**
     * @return Returns the attributePrefLevels.
     */
    public List getAttributePrefLevels() {
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
    public void setAttributePrefLevels(int key, Object value) {
        this.attributePrefLevels.set(key, value);
    }
    /**
     * @param attributePrefs The attributePrefLevels to set.
     */
    public void setAttributePrefLevels(List attributePrefLevels) {
        this.attributePrefLevels = attributePrefLevels;
    }
    
    /**
     * @return Returns the roomFeaturePrefLevels.
     */
    public List getRoomFeaturePrefLevels() {
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
    public void setRoomFeaturePrefLevels(int key, Object value) {
        this.roomFeaturePrefLevels.set(key, value);
    }
    /**
     * @param roomFeaturePrefs The roomFeaturePrefLevels to set.
     */
    public void setRoomFeaturePrefLevels(List roomFeaturePrefLevels) {
        this.roomFeaturePrefLevels = roomFeaturePrefLevels;
    }

    
	public List getDatePatternPrefs() {
		return datePatternPrefs;
	}
	
	public String getDatePatternPrefs(int key) {
		return datePatternPrefs.get(key).toString();
	}

	public void setDatePatternPrefs(int key, Object value) {
		this.datePatternPrefs.set(key, value);
	}
	public void setDatePatternPrefs(List datePatternPrefs) {
		this.datePatternPrefs = datePatternPrefs;
	}

	public List getDatePatternPrefLevels() {
		return datePatternPrefLevels;
	}
	
	public String getDatePatternPrefLevels(int key) {
		return datePatternPrefLevels.get(key).toString();
	}

	public void setDatePatternPrefLevels(List datePatternPrefLevels) {
		this.datePatternPrefLevels = datePatternPrefLevels;
	}
	
	public void setDatePatternPrefLevels(int key, Object value) {
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
    
	public void sortDatePatternPrefs(List prefs, List prefLevels,
			List<DatePattern> patterns) {
		if (prefs.size() == patterns.size()) {
			Collections.sort(patterns); //, new DatePattenNameComparator()
			List newPrefs = DynamicList.getInstance(new ArrayList(),
					factoryPref);
			List newPrefLevels = DynamicList.getInstance(new ArrayList(),
					factoryPrefLevel);
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
}
