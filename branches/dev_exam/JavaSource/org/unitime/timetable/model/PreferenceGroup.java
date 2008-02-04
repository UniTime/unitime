/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.ObjectNotFoundException;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.timetable.model.base.BasePreferenceGroup;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.webutil.RequiredTimeTable;




public abstract class PreferenceGroup extends BasePreferenceGroup {
	private static final long serialVersionUID = 1L;
	
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
    
    public Set getTimePreferences(){
    	return getPreferences(TimePref.class);
    }

    public Set getTimePatterns() {
    	Set timePrefs = getTimePreferences();
    	if (timePrefs==null) return null;
    	TreeSet ret = new TreeSet();
    	for (Iterator i=timePrefs.iterator();i.hasNext();) {
    		TimePref tp = (TimePref)i.next();
    		if (tp.getTimePattern()!=null)
    			ret.add(tp.getTimePattern());
    	}
    	return ret;
    }
    
    /* getEffectiveTimePreferences()
     * @return ArrayList of TimePrefs
     */
    
    public Set getEffectiveTimePreferences(){
    	return effectivePreferences(TimePref.class);
    }
    
    public Set effectiveTimePatterns() {
    	Set timePrefs = getEffectiveTimePreferences();
    	if (timePrefs==null) return null;
    	TreeSet ret = new TreeSet();
    	for (Iterator i=timePrefs.iterator();i.hasNext();) {
    		TimePref tp = (TimePref)i.next();
    		if (tp.getTimePattern()!=null)
    			ret.add(tp.getTimePattern());
    	}
    	return ret;
    }

    /* getRoomPreferences()
     * @return ArrayList of RoomPrefs
     */
    
    public Set getRoomPreferences(){
    	return getPreferences(RoomPref.class);
    }

    /* getEffectiveRoomPreferences()
     * @return ArrayList of RoomPrefs
     */
    
    public Set getEffectiveRoomPreferences(){
    	return effectivePreferences(RoomPref.class);
    }

    /* getBuildingPreferences()
     * @return ArrayList of BuildingPrefs
     */
    
    public Set getBuildingPreferences(){
    	return getPreferences(BuildingPref.class);
    }

    /* getEffectiveBuildingPreferences()
     * @return ArrayList of BuildingPrefs
     */
    
    public Set getEffectiveBuildingPreferences(){
    	return effectivePreferences(BuildingPref.class);
    }

    /* getRoomFeaturePreferences()
     * @return ArrayList of RoomFeaturePrefs
     */
    
    public Set getRoomFeaturePreferences(){
    	return getPreferences(RoomFeaturePref.class);
    }
 
    /* getRoomGroupPreferences()
     * @return ArrayList of RoomGroupPrefs
     */
    
    public Set getRoomGroupPreferences(){
    	return getPreferences(RoomGroupPref.class);
    }

    /* getEffectiveRoomFeaturePreferences()
     * @return ArrayList of RoomFeaturePrefs
     */
    
    public Set getEffectiveRoomFeaturePreferences(){
    	return effectivePreferences(RoomFeaturePref.class);
    }

    /* getDistributionPreferences()
     * @return ArrayList of DistributionPrefs
     */
    
    public Set getDistributionPreferences() {
    	return getPreferences(DistributionPref.class);
    }
   
    /* getEffectiveistributionPreferences()
     * @return ArrayList of DistributionPrefs
     */
    
    public Set getEffectiveDistributionPreferences(){
    	return effectivePreferences(DistributionPref.class);
    }
   
    /* getPrefHtmlForPrefType()
     * @parameter prefName - the Name of the Class of the Preference Type you want to see html for
     * @return - a String of HTML to display the Preference
     */
    
    private String htmlForPrefs(Assignment assignment, Set prefList, boolean timeVertical, boolean gridAsText, String timeGridSize){
       	StringBuffer sb = new StringBuffer();
       	if (prefList != null && !prefList.isEmpty()) {
       		if (prefList.toArray()[0] instanceof TimePref){
       			sb.append(htmlForTimePrefs(assignment, prefList, timeVertical, gridAsText, timeGridSize));
       		} else {
		    	Iterator it = prefList.iterator();
		    	Preference aPref = null;
		    	boolean notFirst = false;
		    	while (it.hasNext()){
		    		if (notFirst){
		    			sb.append("<BR>");
		    		} else {
		    			notFirst = true;
		    		}
		    		aPref = (Preference) it.next();
		    		sb.append(aPref.preferenceHtml());
		    	}
       		}
       	}
    	return (sb.toString());
    }
    
    private String htmlForPrefs(Assignment assignment, Set prefList){
    	return (htmlForTimePrefs(assignment, prefList, false, false, null));
    }
    
    private String htmlForTimePrefs(Assignment assignment, Set timePrefList, boolean timeVertical, boolean gridAsText, String timeGridSize){
    	StringBuffer sb = new StringBuffer();
    	for (Iterator i=timePrefList.iterator();i.hasNext();) {
    		TimePref tp = (TimePref)i.next();
    		RequiredTimeTable rtt = tp.getRequiredTimeTable(assignment);
        	if (gridAsText) {
    			String title = tp.getTimePattern().getName();
    			if (assignment!=null)
    				title += ", assigned "+assignment.getPlacement().getName();
        		sb.append("<span title='"+title+"'>"+rtt.getModel().toString().replaceAll(", ","<br>")+"</span>");
        	} else {
        		rtt.getModel().setDefaultSelection(timeGridSize);
    			File imageFileName = null;
    			try {
    				imageFileName = rtt.createImage(timeVertical);
    			} catch (IOException ex) {
    				ex.printStackTrace();
    			}
    			String title = rtt.getModel().toString();
    			if (assignment!=null)
    				title += ", assigned "+assignment.getPlacement().getName();
    			if (imageFileName!=null)
    				sb.append("<img border='0' src='temp/"+(imageFileName.getName())+"' title='"+title+"'>&nbsp;");
    			else
    				sb.append("<span title='"+title+"'>"+rtt.getModel().toString()+"</span>");
        	}
			if (i.hasNext()) sb.append("<br>");
    	}
    	return sb.toString();
    }
    
    public abstract String htmlLabel();
    
    public String effectiveTimePatternHtml(){
    	return(new String());
    }
    
    public String getPrefHtmlForPrefType(Class type){
    	return (htmlForPrefs(null, getPreferences(type)));
    }
    
    public String getEffectivePrefHtmlForPrefType(Class type){
    	return (htmlForPrefs(null, effectivePreferences(type), false, false, null));
    }
    
    public String getEffectivePrefHtmlForPrefType(Class type, boolean timeVertical, boolean gridAsText, String timeGridSize){
    	return (htmlForPrefs(null, effectivePreferences(type), timeVertical, gridAsText, timeGridSize));
    }

    public String getEffectivePrefHtmlForPrefType(Assignment assignment, Class type, boolean timeVertical, boolean gridAsText, String timeGridSize){
    	return (htmlForPrefs(assignment, effectivePreferences(type), timeVertical, gridAsText, timeGridSize));
    }

    /*
    public Set effectivePreferences(){
    	return(this.getPreferences());
    }
    */
    
    private HashMap timePrefHash(Collection timePrefList){
    	HashMap hm = new HashMap();
    	Iterator it = timePrefList.iterator();
    	TimePref t = null;
    	while (it.hasNext()){
    		t = (TimePref) it.next();
    		if (hm.containsKey(t.getTimePattern())){
    			((ArrayList) hm.get(t.getTimePattern())).add(t);
    		} else {
    			ArrayList a = new ArrayList();
    			a.add(t);
    			hm.put(t.getTimePattern(),a);
    		}
    	}
    	return(hm);
    }
    
    protected abstract boolean canUserEdit(User user);
    
    protected abstract boolean canUserView(User user);
   
    public boolean isEditableBy(User user){
    	if (user == null){
    		return(false);
    	}
    	if (user.isAdmin()){
    		return(true);
    	} 
    	if (user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE))
    		return false;
    	return(this.canUserEdit(user));
    }
    
    public boolean isViewableBy(User user){
    	if(user == null){
    		return(false);
    	}
    	if (user.isAdmin()){
    		return(true);
    	}
    	if (user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE))
    		return true;
    	if (this.canUserEdit(user)){
    		return(true);
    	}
    	return(this.canUserView(user));
    }
    
    public Class getInstanceOf() {
        return PreferenceGroup.class;
    }
    
    public Set getPreferences(Class type) {
    	return getPreferences(type, null);
    }
    
    public Set getPreferences(Class type, PreferenceGroup appliesTo) {
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
    		i = null;
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
    
    public Set effectivePreferences(Class type, Vector leadInstructors) {
    	return effectivePreferences(type);
    }
    
    public Set effectivePreferences(Class type) {
    	return getPreferences(type, null);
    }
    
    public DatePattern effectiveDatePattern(){
    	return null;
    }
    
    public boolean canUseHardTimePreferences(User user) {
    	return true;
    }
    
    public boolean canUseHardRoomPreferences(User user) {
        return true;
    }

    public Set getAvailableRooms() {
    	return new TreeSet();
    }
    public Set getAvailableBuildings() {
    	TreeSet bldgs = new TreeSet();
    	for (Iterator i=getAvailableRooms().iterator();i.hasNext();) {
    		Location location = (Location)i.next();
    		if (location instanceof Room)
    			bldgs.add(((Room)location).getBuilding());
    	}
    	return bldgs;
    }
    public Set getAvailableRoomFeatures() {
    	return new TreeSet(RoomFeature.getAllGlobalRoomFeatures());
    }
    public Set getAvailableRoomGroups() {
    	return new TreeSet(RoomGroup.getAllGlobalRoomGroups());
    }
    
    public Set getExamPeriodPreferences(){
        return getPreferences(ExamPeriodPref.class);
    }
    
    public Set getEffectiveExamPeriodPreferences(){
        return effectivePreferences(ExamPeriodPref.class);
    }
}