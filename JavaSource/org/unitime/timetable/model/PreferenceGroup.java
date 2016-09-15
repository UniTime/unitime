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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.ObjectNotFoundException;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BasePreferenceGroup;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.webutil.RequiredTimeTable;




/**
 * @author Tomas Muller
 */
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
    	return htmlForPrefs(assignment, prefList, timeVertical, gridAsText, timeGridSize, ApplicationProperty.PreferencesHighlighClassPreferences.isTrue());
    }
    
    private String htmlForPrefs(Assignment assignment, Set prefList, boolean timeVertical, boolean gridAsText, String timeGridSize, boolean highlightClassPrefs){
       	StringBuffer sb = new StringBuffer();
       	if (prefList != null && !prefList.isEmpty()) {
       		if (prefList.toArray()[0] instanceof TimePref){
       			sb.append(htmlForTimePrefs(assignment, prefList, timeVertical, gridAsText, timeGridSize, highlightClassPrefs));
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
		    		sb.append(aPref.preferenceHtml(highlightClassPrefs));
		    	}
       		}
       	}
    	return (sb.toString());
    }
    
    private String htmlForPrefs(Assignment assignment, Set prefList){
    	return (htmlForTimePrefs(assignment, prefList, false, false, null, ApplicationProperty.PreferencesHighlighClassPreferences.isTrue()));
    }
    
    private String htmlForTimePrefs(Assignment assignment, Set timePrefList, boolean timeVertical, boolean gridAsText, String timeGridSize, boolean highlightClassPrefs){
    	StringBuffer sb = new StringBuffer();
    	for (Iterator i=timePrefList.iterator();i.hasNext();) {
    		TimePref tp = (TimePref)i.next();
    		RequiredTimeTable rtt = tp.getRequiredTimeTable(assignment == null ? null : assignment.getTimeLocation());
    		String owner = "";
    		if (tp.getOwner() != null && tp.getOwner() instanceof Class_) {
    			owner = " (class)";
    		} else if (tp.getOwner() != null && tp.getOwner() instanceof SchedulingSubpart) {
    			owner = " (scheduling subpart)";
    		} else if (tp.getOwner() != null && tp.getOwner() instanceof DepartmentalInstructor) {
    			owner = " (instructor)";
    		} else if (tp.getOwner() != null && tp.getOwner() instanceof Exam) {
    			owner = " (examination)";
    		} else if (tp.getOwner() != null && tp.getOwner() instanceof Department) {
    			owner = " (department)";
    		} else if (tp.getOwner() != null && tp.getOwner() instanceof Session) {
    			owner = " (session)";
    		} else {
    			owner = " (combined)";
    		}
    		String hint = rtt.print(false, timeVertical, true, false, rtt.getModel().getName() + owner).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " ");
        	if (gridAsText || rtt.getModel().isExactTime()) {
        		sb.append("<span onmouseover=\"showGwtHint(this, " + hint + ");\" onmouseout=\"hideGwtHint();\" "+
        				(tp.getOwner() != null && tp.getOwner() instanceof Class_ && highlightClassPrefs ? " style='background: #ffa;'" : "") +
        				">"+rtt.getModel().toString().replaceAll(", ","<br>")+"</span>");
        	} else {
        		rtt.getModel().setDefaultSelection(timeGridSize);
    			sb.append("<img border='0' src='" +
    					"pattern?v=" + (timeVertical ? 1 : 0) + "&s=" + rtt.getModel().getDefaultSelection() + "&tp=" + tp.getTimePattern().getUniqueId() + "&p=" + rtt.getModel().getPreferences() +
    					(assignment == null || assignment.getTimeLocation() == null ? "" : "&as=" + assignment.getTimeLocation().getStartSlot() + "&ad=" + assignment.getTimeLocation().getDayCode()) +
    					(tp.getOwner() != null && tp.getOwner() instanceof Class_ && highlightClassPrefs ? "&hc=1" : "") +
    					"' onmouseover=\"showGwtHint(this, " + hint + ");\" onmouseout=\"hideGwtHint();\">&nbsp;");
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
    
    public String getEffectivePrefHtmlForPrefType(Class type, boolean highlightClassPrefs) {
    	return (htmlForPrefs(null, effectivePreferences(type), false, false, null, highlightClassPrefs));
    }
    
    public String getEffectivePrefHtmlForPrefType(Class type, boolean timeVertical, boolean gridAsText, String timeGridSize){
    	return (htmlForPrefs(null, effectivePreferences(type), timeVertical, gridAsText, timeGridSize));
    }
    
    public String getEffectivePrefHtmlForPrefType(Class type, String nameFormat, boolean highlightClassPrefs){
    	return (htmlForPrefs(null, effectivePreferences(type), false, false, null, highlightClassPrefs));
    }

    public String getEffectivePrefHtmlForPrefType(Assignment assignment, Class type, boolean timeVertical, boolean gridAsText, String timeGridSize){
    	return (htmlForPrefs(assignment, effectivePreferences(type), timeVertical, gridAsText, timeGridSize));
    }
    
    public String getEffectivePrefHtmlForPrefType(Assignment assignment, Class type, boolean timeVertical, boolean gridAsText, String timeGridSize, boolean highlightClassPrefs){
    	return (htmlForPrefs(assignment, effectivePreferences(type), timeVertical, gridAsText, timeGridSize, highlightClassPrefs));
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
    	return effectivePreferences(type, leadInstructors, TimePref.class.equals(type));
    }
    
    public Set effectivePreferences(Class type, Vector leadInstructors, boolean fixDurationInTimePreferences) {
    	return effectivePreferences(type, fixDurationInTimePreferences);
    }
    
    public Set effectivePreferences(Class type) {
    	return effectivePreferences(type, TimePref.class.equals(type));
    }
    
    public Set effectivePreferences(Class type, boolean fixDurationInTimePreferences) {
    	return getPreferences(type, null);
    }
    
    public DatePattern effectiveDatePattern(){
    	return null;
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
    public abstract Session getSession();
    
    public Set getAvailableRoomFeatures() {
    	return new TreeSet(RoomFeature.getAllGlobalRoomFeatures(getSession()));
    }
    public Set getAvailableRoomGroups() {
    	return new TreeSet(RoomGroup.getAllGlobalRoomGroups(getSession()));
    }
    
    public Set getExamPeriodPreferences(){
        return getPreferences(ExamPeriodPref.class);
    }
    
    public Set getEffectiveExamPeriodPreferences(){
        return effectivePreferences(ExamPeriodPref.class);
    }
    
    public abstract Department getDepartment();
}
