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
package org.unitime.timetable.model;


import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Transient;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.ObjectNotFoundException;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.base.BasePreferenceGroup;
import org.unitime.timetable.model.dao._RootDAO;


/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class PreferenceGroup extends BasePreferenceGroup {
	private static final long serialVersionUID = 1L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public PreferenceGroup () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public PreferenceGroup (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
    /* getTimePreferences()
     * @return ArrayList of TimePrefs
     */
    
	@Transient
    public Set<TimePref> getTimePreferences(){
    	return getPreferences(TimePref.class);
    }

	@Transient
    public Set<TimePattern> getTimePatterns() {
    	Set<TimePref> timePrefs = getTimePreferences();
    	if (timePrefs==null) return null;
    	Set<TimePattern> ret = new TreeSet<>();
    	for (TimePref tp: timePrefs) {
    		if (tp.getTimePattern() != null)
    			ret.add(tp.getTimePattern());
    	}
    	return ret;
    }
    
    /* getEffectiveTimePreferences()
     * @return ArrayList of TimePrefs
     */
    
	@Transient
    public Set getEffectiveTimePreferences(){
    	return effectivePreferences(TimePref.class);
    }
    
    public Set<TimePattern> effectiveTimePatterns() {
    	Set<TimePref> timePrefs = getEffectiveTimePreferences();
    	if (timePrefs==null) return null;
    	TreeSet<TimePattern> ret = new TreeSet<TimePattern>();
    	for (TimePref tp: timePrefs) {
    		if (tp.getTimePattern()!=null)
    			ret.add(tp.getTimePattern());
    	}
    	return ret;
    }

    /* getRoomPreferences()
     * @return ArrayList of RoomPrefs
     */
    
	@Transient
    public Set<RoomPref> getRoomPreferences(){
    	return getPreferences(RoomPref.class);
    }

    /* getEffectiveRoomPreferences()
     * @return ArrayList of RoomPrefs
     */
    
	@Transient
    public Set getEffectiveRoomPreferences(){
    	return effectivePreferences(RoomPref.class);
    }

    /* getBuildingPreferences()
     * @return ArrayList of BuildingPrefs
     */
    
	@Transient
    public Set<BuildingPref> getBuildingPreferences(){
    	return getPreferences(BuildingPref.class);
    }

    /* getEffectiveBuildingPreferences()
     * @return ArrayList of BuildingPrefs
     */
    
	@Transient
    public Set<BuildingPref> getEffectiveBuildingPreferences(){
    	return effectivePreferences(BuildingPref.class);
    }

    /* getRoomFeaturePreferences()
     * @return ArrayList of RoomFeaturePrefs
     */
    
	@Transient
    public Set<RoomFeaturePref> getRoomFeaturePreferences(){
    	return getPreferences(RoomFeaturePref.class);
    }
 
    /* getRoomGroupPreferences()
     * @return ArrayList of RoomGroupPrefs
     */
    
	@Transient
    public Set<RoomGroupPref> getRoomGroupPreferences(){
    	return getPreferences(RoomGroupPref.class);
    }

    /* getEffectiveRoomFeaturePreferences()
     * @return ArrayList of RoomFeaturePrefs
     */
    
	@Transient
    public Set<RoomFeaturePref> getEffectiveRoomFeaturePreferences(){
    	return effectivePreferences(RoomFeaturePref.class);
    }

    /* getDistributionPreferences()
     * @return ArrayList of DistributionPrefs
     */
    
	@Transient
    public Set<DistributionPref> getDistributionPreferences() {
    	return getPreferences(DistributionPref.class);
    }
   
    /* getEffectiveistributionPreferences()
     * @return ArrayList of DistributionPrefs
     */
    
	@Transient
    public Set<DistributionPref> getEffectiveDistributionPreferences(){
    	return effectivePreferences(DistributionPref.class);
    }
   
    /* getPrefHtmlForPrefType()
     * @parameter prefName - the Name of the Class of the Preference Type you want to see html for
     * @return - a String of HTML to display the Preference
     */
    
	@Transient
    public Set<DatePatternPref> getDatePatternPreferences(){
    	return getPreferences(DatePatternPref.class);
    }

	@Transient
    public Class getInstanceOf() {
        return PreferenceGroup.class;
    }
    
    public <T> Set<T> getPreferences(Class<T> type) {
    	return getPreferences(type, null);
    }
    
    public <T> Set<T> getPreferences(Class type, PreferenceGroup appliesTo) {
    	Set ret = new TreeSet();
    	Iterator i = null;
    	try {
    		i = getPreferences().iterator();
    	} catch (ObjectNotFoundException e) {
    		Debug.error("Exception "+e.getMessage()+" seen for "+this);
    		new _RootDAO().getSession().refresh(this);
    		if (getPreferences() != null)
    			i = getPreferences().iterator();
    		else
    			i = null;
    	} catch (Exception e){
    		Debug.error("Exception "+e.getMessage()+" seen for "+this, e);
    		i = new _RootDAO().getSession().createQuery(
    				"select p from Preference p where p.owner.uniqueId = :ownerId and p.class = :type", Preference.class)
    				.setParameter("ownerId", getUniqueId())
    				.setParameter("type", type).list().iterator();
    	}
    	
    	if (i == null){
    		return(ret);
    	}
    	while (i.hasNext()) {
    		Preference preference = (Preference)i.next();
    		if (appliesTo!=null && !preference.appliesTo(appliesTo)) continue;
    		if (type.isInstance(preference))
    			ret.add(preference);
    	}
    	return ret;
    }
    
    public <T> Set<T> effectivePreferences(Class<T> type, Vector leadInstructors) {
    	return effectivePreferences(type, leadInstructors, TimePref.class.equals(type));
    }
    
    public <T> Set<T> effectivePreferences(Class<T> type, Vector leadInstructors, boolean fixDurationInTimePreferences) {
    	return effectivePreferences(type, fixDurationInTimePreferences);
    }
    
    public <T> Set<T> effectivePreferences(Class<T> type) {
    	return effectivePreferences(type, TimePref.class.equals(type));
    }
    
    public <T> Set<T> effectivePreferences(Class<T> type, boolean fixDurationInTimePreferences) {
    	return getPreferences(type, null);
    }
    
    public DatePattern effectiveDatePattern(){
    	return null;
    }
    
	@Transient
    public Set<Location> getAvailableRooms() {
    	return new TreeSet<Location>();
    }
	@Transient
    public Set<Building> getAvailableBuildings() {
    	TreeSet<Building> bldgs = new TreeSet<Building>();
    	for (Location location: getAvailableRooms()) {
    		if (location instanceof Room)
    			bldgs.add(((Room)location).getBuilding());
    	}
    	return bldgs;
    }
	@Transient
    public abstract Session getSession();
    
	@Transient
    public Set<RoomFeature> getAvailableRoomFeatures() {
    	return new TreeSet<RoomFeature>(RoomFeature.getAllGlobalRoomFeatures(getSession()));
    }
	@Transient
    public Set<RoomGroup> getAvailableRoomGroups() {
    	return new TreeSet<RoomGroup>(RoomGroup.getAllGlobalRoomGroups(getSession()));
    }
    
	@Transient
    public Set getAvailableCourses() {
    	return new TreeSet();
    }
    
	@Transient
    public Set getAvailableAttributeTypes() {
    	return new TreeSet();
    }
    
	@Transient
    public Set getAvailableAttributes() {
    	return new TreeSet();
    }
    
	@Transient
    public Set<ExamPeriodPref> getExamPeriodPreferences(){
        return getPreferences(ExamPeriodPref.class);
    }
    
	@Transient
    public Set<ExamPeriodPref> getEffectiveExamPeriodPreferences(){
        return effectivePreferences(ExamPeriodPref.class);
    }
    
	@Transient
    public abstract Department getDepartment();
    
	@Transient
    public boolean isInstructorAssignmentNeeded() { return false; }
	
	public abstract String htmlLabel();
}
