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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseSchedulingSubpart;
import org.unitime.timetable.model.comparators.NavigationComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.duration.DurationModel;
import org.unitime.timetable.webutil.Navigation;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
public class SchedulingSubpart extends BaseSchedulingSubpart {
	private static final long serialVersionUID = 1L;

	/** Request Parameter name for Scheduling Subpart List **/
	public static final String SCHED_SUBPART_ATTR_NAME = "schedSubpartList";
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public SchedulingSubpart () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SchedulingSubpart (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/*
	public String getCourseName(){
		return(this.getInstrOfferingConfig().getCourseName());
	}
	*/
	
	public String getCourseNameWithTitle(){
		return(this.getInstrOfferingConfig().getCourseNameWithTitle());
	}

	public CourseOffering getControllingCourseOffering(){
		return(this.getInstrOfferingConfig().getControllingCourseOffering());
	}
	
	public String getItypeDesc() {
		try {
			ItypeDesc itype = getItype();
			return (itype==null?null:itype.getAbbv());
		} catch (Exception e) {
			Debug.error(e);
			return null;
		}
	}
	
	public Department getManagingDept(){
		if (this.getClasses() != null){
			boolean allSame = true;
			Department d = null;
			Department d1 = null;
			Iterator it = this.getClasses().iterator();
			Class_ c = null;
			while (it.hasNext() && allSame){
				c = (Class_) it.next();
				d = c.getManagingDept();
				if (d1 == null){
					d1 = d;
				}
				if (d1 != null && (d == null || d.getUniqueId() == null || !d.getUniqueId().equals(d1.getUniqueId()))){
					allSame = false;
				}
			}
			if (d != null && allSame){
				return(d);
			} else {
				return(this.getControllingDept());
			}
		}
		return(this.getControllingDept());
	}
    
    public Department getControllingDept() {
 		return (this.getInstrOfferingConfig().getDepartment());
	}
	
    /*
	public Session getSession() {
		return (this.getInstrOfferingConfig().getSession());
	}
	
	public Long getSessionId() {
		return (this.getInstrOfferingConfig().getSessionId());
	}
	*/
    
    public Long getSessionId() {
    	return getSession().getUniqueId();
    }
	
	public String htmlLabel(){
		return(this.getItype().getDesc());
	}
	private String htmlForTimePatterns(Set patterns){
		StringBuffer sb = new StringBuffer();
		if (patterns != null){
			Iterator it = patterns.iterator();
			TimePattern t = null;
			while (it.hasNext()){
				t = (TimePattern) it.next();
				sb.append(t.getName());
				if (it.hasNext()) {
					sb.append("<BR>");
				}
			}
		}
		return(sb.toString());
	}
	
	public String effectiveTimePatternHtml(){
		return(htmlForTimePatterns(this.effectiveTimePatterns()));
	}
	
	public String timePatternHtml(){
		return(htmlForTimePatterns(this.getTimePatterns()));
	}

	
	/**
	 * Gets the minimum class limit for the sub class
	 * @return Class Limit (-1 if classes not defined, 0 if no classes set)
	 */
	public int getMinClassLimit() {
	    Set classes = this.getClasses();
        if(classes==null) return -1;
        if(classes.size()==0) return 0;
        
        int limit = 0;
        Iterator iter = classes.iterator();
        while (iter.hasNext()) {
            Class_ c = (Class_) iter.next();
            int ct = c.getExpectedCapacity().intValue();
            if(ct>limit) limit = ct;
        }

        return limit;
	}
	
	/**
	 * Gets the maximum class limit for the sub class
	 * @return Class Limit (-1 if classes not defined, 0 if no classes set)
	 */
	public int getMaxClassLimit() {
	    Set classes = this.getClasses();
        if(classes==null) return -1;
        if(classes.size()==0) return 0;
        
        int limit = 0;
        Iterator iter = classes.iterator();
        while (iter.hasNext()) {
            Class_ c = (Class_) iter.next();
            int ct = c.getMaxExpectedCapacity().intValue();
            if(ct>limit) limit = ct;
        }

        return limit;
	}
	
	/**
	 * Gets the number of classes for a subpart
	 * @return Number of classes (-1 if classes not defined)
	 */
	public int getNumClasses() {
	    Set classes = this.getClasses();
        if(classes==null) return -1;
        return classes.size();	    
	}
	
	public String getSchedulingSubpartLabel() {
		String sufix = getSchedulingSubpartSuffix();
        String cfgName = (getInstrOfferingConfig().getInstructionalOffering().hasMultipleConfigurations()?getInstrOfferingConfig().getName():null);        
		return getCourseName() + " " + this.getItypeDesc().trim() + (sufix==null || sufix.length()==0?"":" ("+sufix+")")+(cfgName==null?"":" ["+cfgName+"]");
	}
	
	/**
	 * Returns String representation of the form {Subj Area} {Crs Nbr} {Itype Desc} 
	 */
	public String toString() {
		return getSchedulingSubpartLabel();
	}
	
	/**
	 * @return Class type to distinguish the sub class in PrefGroup
	 */
	public Class getInstanceOf() {
	    return SchedulingSubpart.class;
	}

    /**
     * Gets the max room ratio among all the classes
     * belonging to the subpart
     * @return max room ratio
     */
    public float getMaxRoomRatio() {
	    Set classes = this.getClasses();
        if(classes==null) return -1;
        if(classes.size()==0) return 1.0f;
        
	    float rc = 0;
	    for (Iterator iter= classes.iterator(); iter.hasNext(); ) {
	        Class_ c = (Class_) iter.next();	        
	        Float rc1 = c.getRoomRatio();
	        if(rc1!=null && rc1.floatValue()>rc)
	            rc = rc1.floatValue();
	    }
	    
        return rc;
    }
    
    /**
     * Gets the max number of rooms among all the classes
     * belonging to the subpart
     * @return max number of rooms
     */
    public int getMaxRooms() {
	    Set classes = this.getClasses();
        if(classes==null) return -1;
        if(classes.size()==0) return -1;
        
        int numRooms = 0;
        Iterator iter = classes.iterator();
        while (iter.hasNext()) {
            Class_ c = (Class_) iter.next();
            int ct = c.getNbrRooms().intValue();
            if(ct>numRooms) numRooms = ct;
        }

        return numRooms;
        
    }
    
    public Set getDistributionPreferences() {
    	TreeSet prefs = new TreeSet();
    	if (getDistributionObjects()!=null) {
    		for (Iterator i=getDistributionObjects().iterator();i.hasNext();) {
    			DistributionObject distObj = (DistributionObject)i.next();
    				prefs.add(distObj.getDistributionPref());
    		}
    	}
    	return prefs;
    }

    
    public Set effectiveDistributionPreferences(Department owningDept) {
    	if (getDistributionObjects()==null) return null;
    	TreeSet prefs = new TreeSet();
    	for (Iterator i=getDistributionObjects().iterator();i.hasNext();) {
    		DistributionObject distObj = (DistributionObject)i.next();
    		DistributionPref pref = distObj.getDistributionPref();
    		if (owningDept==null || owningDept.equals(pref.getOwner()))
    			prefs.add(pref);
    	}
    	return prefs;
    }
    
    protected Set combinePreferences(Class type, Set subpartPrefs, Set parentPrefs) {
		if (TimePref.class.equals(type)) {
			if (parentPrefs == null || parentPrefs.isEmpty() || !getParentSubpart().getMinutesPerWk().equals(getMinutesPerWk())) return subpartPrefs;
			
			Set<TimePattern> tp = new HashSet<TimePattern>();

			Set ret = new TreeSet();
			for (Iterator i = subpartPrefs.iterator(); i.hasNext(); ) {
				TimePref pref = (TimePref) i.next();
				/*
				for (Iterator j = parentPrefs.iterator(); j.hasNext(); ) {
					TimePref p = (TimePref) j.next();
					if (pref.getTimePattern().equals(p.getTimePattern())) {
						pref = (TimePref)pref.clone();
						pref.combineWith(p, false);
					}
				}
				*/
				ret.add(pref);
				tp.add(pref.getTimePattern());
			}
			
			for (Iterator j = parentPrefs.iterator(); j.hasNext();) {
				TimePref p = (TimePref) j.next();
				if (tp.add(p.getTimePattern())) ret.add(p);
			}

			return ret;
		}
		
		if (subpartPrefs == null || subpartPrefs.isEmpty()) return parentPrefs;
		if (parentPrefs == null || parentPrefs.isEmpty()) return subpartPrefs;

		Set ret = new TreeSet(subpartPrefs);
		prefs: for (Iterator i = parentPrefs.iterator(); i.hasNext(); ) {
			Preference parentPref = (Preference)i.next();
			for (Iterator j = ret.iterator(); j.hasNext();) {
				Preference p = (Preference)j.next();
				if (p.isSame(parentPref)) continue prefs;
			}
			ret.add(parentPref);
		}
		return ret;
    }
    
    private Set removeDepartmentalPreferences(Set prefs) {
    	if (prefs==null) return new TreeSet();
    	if (prefs.isEmpty()) return prefs;
    	Set ret = new TreeSet();
		for (Iterator i=prefs.iterator();i.hasNext();) {
			Preference pref = (Preference)i.next();
    		if (pref instanceof RoomPref) {
    			Location loc = ((RoomPref)pref).getRoom();
    			for (RoomDept rd: loc.getRoomDepts())
    				if (rd.getDepartment().equals(getManagingDept())) {
    					ret.add(pref); break;
    				}
    		} else if (pref instanceof BuildingPref) {
    			Building b = ((BuildingPref)pref).getBuilding();
    			if (getAvailableBuildings().contains(b))
    				ret.add(pref);
    		} else if (pref instanceof RoomFeaturePref) {
    			RoomFeature rf = ((RoomFeaturePref)pref).getRoomFeature();
    			if (rf instanceof GlobalRoomFeature)
    				ret.add(pref);
    			else if (rf instanceof DepartmentRoomFeature && ((DepartmentRoomFeature)rf).getDepartment().equals(getManagingDept()))
    				ret.add(pref);
    		} else if (pref instanceof RoomGroupPref) {
    			RoomGroup rg = ((RoomGroupPref)pref).getRoomGroup();
    			if (rg.isGlobal() || getManagingDept().equals(rg.getDepartment()))
    				ret.add(pref);
    		} else {
    			ret.add(pref);
    		}
    	}
    	return ret;
    }

    private Set weakenHardPreferences(Set prefs) {
    	if (prefs==null || prefs.isEmpty()) return prefs;
    	Set ret = new TreeSet();
		for (Iterator i=prefs.iterator();i.hasNext();) {
			Preference pref = (Preference)((Preference)i.next()).clone();
			if (pref.weakenHardPreferences())
				ret.add(pref);
		}
    	return ret;
    }

    private Set removeNeutralPreferences(Set prefs) {
    	if (prefs==null) return new TreeSet();
    	if (prefs.isEmpty()) return prefs;
    	Set ret = new TreeSet(prefs);
		for (Iterator i=ret.iterator();i.hasNext();) {
			Preference pref = (Preference)i.next();
			if (PreferenceLevel.sNeutral.equals(pref.getPrefLevel().getPrefProlog())) i.remove();
		}
		return ret;
    }
    
    private Set fixDurationInTimePreferences(Set prefs) {
    	if (prefs == null || prefs.isEmpty()) return prefs;
    	DatePattern dp = effectiveDatePattern();
    	if (dp == null) return prefs;
    	DurationModel dm = getInstrOfferingConfig().getDurationModel();
    	
    	Set ret = new TreeSet(prefs);
    	List<TimePref> fixed = new ArrayList<TimePref>();
		for (Iterator i=ret.iterator();i.hasNext();) {
			Preference pref = (Preference)i.next();
			if (pref instanceof TimePref) {
				TimePref tp = (TimePref) pref;
				if (tp.getTimePattern().getType() != null && tp.getTimePattern().getType() == TimePattern.sTypeExactTime) continue;
				Set<Integer> days = dm.getDayCodes(getMinutesPerWk(), dp, tp.getTimePattern());
				if (days.isEmpty()) {
					i.remove();
				} else if (days.size() < tp.getTimePattern().getDays().size()) {
					TimePatternModel model = tp.getTimePatternModel();
					boolean req = model.hasRequiredPreferences();
					for (int d = 0; d < model.getNrDays(); d++) {
						if (!days.contains(model.getDayCode(d))) {
							for (int t = 0; t < model.getNrTimes(); t++)
								model.setPreference(d, t, PreferenceLevel.sNotAvailable);
						}
					}
					if (req && !model.hasRequiredPreferences()) {
						for (int d = 0; d < model.getNrDays(); d++)
							if (days.contains(model.getDayCode(d))) {
								for (int t = 0; t < model.getNrTimes(); t++)
									model.setPreference(d, t, PreferenceLevel.sProhibited);
							}
					}
					i.remove();
					TimePref copy = (TimePref)tp.clone();
					copy.setPreference(model.getPreferences());
					fixed.add(copy);
				}
			}
		}
		ret.addAll(fixed);
		return ret;
    }
    
    public boolean canInheritParentPreferences() {
    	return getParentSubpart() != null && getParentSubpart().getItype().equals(getItype()) && ApplicationProperty.PreferencesHierarchicalInheritance.isTrue();
    }
    
    public Set effectivePreferences(Class type, boolean fixDurationInTimePreferences) {
    	if (DistributionPref.class.equals(type)) {
    		return effectiveDistributionPreferences(getManagingDept());
    	}
    	
    	if (InstructorAttributePref.class.equals(type) || InstructorPref.class.equals(type)) {
    		Set<Preference> prefs = new TreeSet<Preference>();
    		List<TeachingRequest> requests = new ArrayList<TeachingRequest>(); 
    		for (Class_ clazz: getClasses())
        		tcr: for (TeachingClassRequest tcr: clazz.getTeachingRequests()) {
        			if (tcr.getAssignInstructor()) {
            			for (TeachingRequest r: requests)
            				if (r.equals(tcr.getTeachingRequest()) || r.canCombine(tcr.getTeachingRequest())) continue tcr;
            			requests.add(tcr.getTeachingRequest());
        				prefs.addAll(tcr.getTeachingRequest().getPreferences(type));
        			}
        		}
    		return prefs;
    	}

    	Set subpartPrefs = getPreferences(type, this);
    	
    	if (canInheritParentPreferences()) {
    		Set parentPrefs = getParentSubpart().effectivePreferences(type);
    		Department mngDept = getManagingDept();

    		if (parentPrefs != null && !parentPrefs.isEmpty() && !mngDept.equals(getParentSubpart().getManagingDept())) {
    			// different managers -> weaken preferences, if needed
    			if (TimePref.class.equals(type)) {
    				if (mngDept.isExternalManager() && !mngDept.isAllowReqTime())
    					parentPrefs = weakenHardPreferences(parentPrefs);
    			} else {
    				if (mngDept.isExternalManager() && !mngDept.isAllowReqRoom())
    					parentPrefs = weakenHardPreferences(parentPrefs);
    			}
    			// remove departmental preferences
        		if (mngDept.isExternalManager()) { 
        			parentPrefs = removeDepartmentalPreferences(parentPrefs);
        		}
    		}
    		
    		Set ret = removeNeutralPreferences(combinePreferences(type, subpartPrefs, parentPrefs));
    		return fixDurationInTimePreferences ? fixDurationInTimePreferences(ret) : ret;
    	}
    	
    	/*
    	if (InstructorPref.class.equals(type)) {
    		Set parentPrefs = new HashSet();
    		for (InstructorCoursePref icp: (List<InstructorCoursePref>)InstructorCoursePrefDAO.getInstance().getSession().createQuery(
    				"from InstructorCoursePref where course.instructionalOffering.uniqueId = :offeringId")
    				.setLong("offeringId", getInstrOfferingConfig().getInstructionalOffering().getUniqueId()).setCacheable(true).list()) {
    			InstructorPref ip = new InstructorPref();
    			ip.setInstructor((DepartmentalInstructor)icp.getOwner());
    			ip.setPrefLevel(icp.getPrefLevel());
    			ip.setOwner(icp.getOwner());
    			parentPrefs.add(ip);
    		}
    		
    		Set ret = removeNeutralPreferences(combinePreferences(type, subpartPrefs, parentPrefs));
    		return fixDurationInTimePreferences ? fixDurationInTimePreferences(ret) : ret;
    	}
		*/
    	
    	return fixDurationInTimePreferences ? fixDurationInTimePreferences(subpartPrefs) : subpartPrefs;
    }
    
    public Set effectivePreferences(Class type, PreferenceGroup appliesTo) {
    	if (appliesTo == null) return effectivePreferences(type);
    	Set ret = new TreeSet();
    	for (Iterator i = effectivePreferences(type, false).iterator(); i.hasNext(); ) {
    		Preference preference = (Preference)i.next();
    		if (!preference.appliesTo(appliesTo)) continue;
    		ret.add(preference);
    	}
    	return ret;
    }
    
	public DatePattern effectiveDatePattern() {
		if (getDatePattern() != null) return getDatePattern();
		if (canInheritParentPreferences()) {
			return getParentSubpart().effectiveDatePattern();
		} else {
			return getSession().getDefaultDatePatternNotNull();
		}
	}

	public Set getAvailableRooms() {
    	Set rooms =  new TreeSet();
        for (Iterator i=getManagingDept().getRoomDepts().iterator();i.hasNext();) {
        	RoomDept roomDept = (RoomDept)i.next();
        	rooms.add(roomDept.getRoom());
        }
        
        return rooms;
    }
	
    public Set getAvailableRoomFeatures() {
    	Set features = super.getAvailableRoomFeatures();
    	Department dept = getManagingDept();
    	if (dept!=null)
    		features.addAll(DepartmentRoomFeature.getAllDepartmentRoomFeatures(dept));
    	return features;
    	
    }
    
    public Set getAvailableRoomGroups() {
    	Set groups = super.getAvailableRoomGroups();
    	Department dept = getManagingDept();
    	if (dept!=null)
    		groups.addAll(RoomGroup.getAllDepartmentRoomGroups(dept));
    	return groups;
    }
    
	public Set getAvailableAttributeTypes() {
		return getControllingDept().getAvailableAttributeTypes();
    }

	public Set getAvailableAttributes() {
		return getControllingDept().getAvailableAttributes();
    }
    
    public SchedulingSubpart getNextSchedulingSubpart(SessionContext context, Right right) {
    	return getNextSchedulingSubpart(context, new NavigationComparator(), right);
    }
    
    public SchedulingSubpart getPreviousSchedulingSubpart(SessionContext context, Right right) {
    	return getPreviousSchedulingSubpart(context, new NavigationComparator(), right);
    }
    
    
    public SchedulingSubpart getNextSchedulingSubpart(SessionContext context, Comparator cmp, Right right) {
    	Long nextId = Navigation.getNext(context, Navigation.sSchedulingSubpartLevel, getUniqueId());
    	if (nextId!=null) {
    		if (nextId.longValue()<0) return null;
    		SchedulingSubpart next = (new SchedulingSubpartDAO()).get(nextId);
    		if (next==null) return null;
    		if (right != null && !context.hasPermission(Department.class.equals(right.type()) ? next.getControllingDept() : next, right)) return next.getNextSchedulingSubpart(context, cmp, right); 
    		return next;
    	}
    	SchedulingSubpart next = null;
    	InstructionalOffering offering = getInstrOfferingConfig().getInstructionalOffering();
    	while (next==null) {
    		if (offering==null) break;
    		for (Iterator i=offering.getInstrOfferingConfigs().iterator();i.hasNext();) {
    			InstrOfferingConfig c = (InstrOfferingConfig)i.next();
    			for (Iterator j=c.getSchedulingSubparts().iterator();j.hasNext();) {
    				SchedulingSubpart s = (SchedulingSubpart)j.next();
    				if (right != null && !context.hasPermission(Department.class.equals(right.type()) ? s.getControllingDept() : s, right)) continue;
    				if (offering.equals(getInstrOfferingConfig().getInstructionalOffering()) && cmp.compare(this, s)>=0) continue;
    				if (next==null || cmp.compare(next,s)>0)
    					next = s;
    			}
        	}
    		offering = offering.getNextInstructionalOffering(context, cmp);
    	}
    	return next;
    }

    public SchedulingSubpart getPreviousSchedulingSubpart(SessionContext context, Comparator cmp, Right right) {
    	Long previousId = Navigation.getPrevious(context, Navigation.sSchedulingSubpartLevel, getUniqueId());
    	if (previousId!=null) {
    		if (previousId.longValue()<0) return null;
    		SchedulingSubpart previous = (new SchedulingSubpartDAO()).get(previousId);
    		if (previous==null) return null;
    		if (right != null && !context.hasPermission(Department.class.equals(right.type()) ? previous.getControllingDept() : previous, right)) return previous.getPreviousSchedulingSubpart(context, cmp, right); 
    		return previous;
    	}
    	SchedulingSubpart previous = null;
    	InstructionalOffering offering = getInstrOfferingConfig().getInstructionalOffering();
    	while (previous==null) {
    		if (offering==null) break;
    		for (Iterator i=offering.getInstrOfferingConfigs().iterator();i.hasNext();) {
    			InstrOfferingConfig c = (InstrOfferingConfig)i.next();
    			for (Iterator j=c.getSchedulingSubparts().iterator();j.hasNext();) {
    				SchedulingSubpart s = (SchedulingSubpart)j.next();
    				if (right != null && !context.hasPermission(Department.class.equals(right.type()) ? s.getControllingDept() : s, right)) continue;
    				if (offering.equals(getInstrOfferingConfig().getInstructionalOffering()) && cmp.compare(this, s)<=0) continue;
    				if (previous==null || cmp.compare(previous,s)<0)
    					previous = s;
    			}
        	}
    		offering = offering.getPreviousInstructionalOffering(context, cmp);
    	}
    	return previous;
    }
    
    public String getSchedulingSubpartSuffix() {
    	return getSchedulingSubpartSuffix(null, true);
    }

    public String getSchedulingSubpartSuffix(org.hibernate.Session hibSession) {
    	return getSchedulingSubpartSuffix(hibSession, true);
    }
    
    public String getSchedulingSubpartSuffix(boolean save) {
    	return getSchedulingSubpartSuffix(null, save);
    }
    
    public String getSchedulingSubpartSuffix(org.hibernate.Session hibSession, boolean save) {
    	String suffix = getSchedulingSubpartSuffixCache();
    	if (suffix!=null) return ("-".equals(suffix)?"":suffix);
    	int nrItypes = 0;
    	int nrItypesBefore = 0;
    	
    	SchedulingSubpartComparator cmp = new SchedulingSubpartComparator();
    	
   		for (Iterator j=getInstrOfferingConfig().getSchedulingSubparts().iterator(); j.hasNext();) {
   			SchedulingSubpart ss = (SchedulingSubpart)j.next();
   			if (ss.getItype().equals(getItype())) {
   				nrItypes++;
   				if (cmp.compare(ss,this)<0) nrItypesBefore++;
   			}
    	}
    	
    	if (nrItypes<=1 || nrItypesBefore<1) 
    		suffix = "";
    	else
    		suffix = String.valueOf((char)('a'+(nrItypesBefore-1)));

    	setSchedulingSubpartSuffixCache(suffix.length()==0?"-":suffix);
    	
    	if (save) {
    		if (hibSession == null) {
        		(new SchedulingSubpartDAO()).getSession().saveOrUpdate(this);
        		(new SchedulingSubpartDAO()).getSession().flush();
    		} else {
    			hibSession.saveOrUpdate(this);
    		}
    	}
    	
    	return suffix;
    }
    
    public void deleteAllDistributionPreferences(org.hibernate.Session hibSession) {
		for (Iterator i3=getClasses().iterator();i3.hasNext();) {
			Class_ c = (Class_)i3.next();
			c.deleteAllDistributionPreferences(hibSession);
		}
    	boolean deleted = false;
    	for (Iterator i=getDistributionObjects().iterator();i.hasNext();) {
    		DistributionObject relatedObject = (DistributionObject)i.next();
    		DistributionPref distributionPref = relatedObject.getDistributionPref();
    		distributionPref.getDistributionObjects().remove(relatedObject);
    		Integer seqNo = relatedObject.getSequenceNumber();
			hibSession.delete(relatedObject);
			deleted = true;
			if (distributionPref.getDistributionObjects().isEmpty()) {
				PreferenceGroup owner = distributionPref.getOwner();
				owner.getPreferences().remove(distributionPref);
				getPreferences().remove(distributionPref);
				hibSession.saveOrUpdate(owner);
				hibSession.delete(distributionPref);
			} else {
				if (seqNo!=null) {
					for (Iterator j=distributionPref.getDistributionObjects().iterator();j.hasNext();) {
						DistributionObject dObj = (DistributionObject)j.next();
						if (seqNo.compareTo(dObj.getSequenceNumber())<0) {
							dObj.setSequenceNumber(new Integer(dObj.getSequenceNumber().intValue()-1));
							hibSession.saveOrUpdate(dObj);
						}
					}
				}
				hibSession.saveOrUpdate(distributionPref);
			}
			i.remove();
    	}
    	if (deleted) hibSession.saveOrUpdate(this);
    }
    
    public int getMaxExpectedCapacity() {
    	int ret = 0;
    	for (Iterator i=getClasses().iterator();i.hasNext();) {
    		Class_ c = (Class_)i.next();
    		if (c.getMaxExpectedCapacity()!=null)
    			ret += c.getMaxExpectedCapacity().intValue();
    		else if (c.getExpectedCapacity()!=null) 
    			ret += c.getExpectedCapacity().intValue();
    	}
    	return ret;
    }
    
    public static List findAll(Long sessionId) {
    	return (new SchedulingSubpartDAO()).
    		getSession().
    		createQuery("select distinct s from SchedulingSubpart s where " +
    				"s.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId").
    		setLong("sessionId",sessionId.longValue()).
    		list();
    }
    
    /**
     * Check if subpart has atleast two classes managed by different departments
     * @return
     */
    public boolean hasMixedManagedClasses() {
    	Department d = null;
    	for (Class_ c: getClasses()) {
    		if (d == null) d = c.getManagingDept();
    		else if (!d.equals(c.getManagingDept())) return true;
    	}
    	return false;
    }
    
    public CourseCreditUnitConfig getCredit(){
    	if(this.getCreditConfigs() == null || this.getCreditConfigs().size() != 1){
    		return(null);
    	} else {
    		return((CourseCreditUnitConfig)this.getCreditConfigs().iterator().next());
    	}
    }
    
    public void setCredit(CourseCreditUnitConfig courseCreditUnitConfig){
    	if (this.getCreditConfigs() == null || this.getCreditConfigs().size() == 0){
    		this.addTocreditConfigs(courseCreditUnitConfig);
    	} else if (!this.getCreditConfigs().contains(courseCreditUnitConfig)){
    		this.getCreditConfigs().clear();
    		this.getCreditConfigs().add(courseCreditUnitConfig);
    	} else {
    		//course already contains this config so we do not need to add it again.
    	}
    }
    
    public Object clone(){
    	SchedulingSubpart newSchedulingSubpart = new SchedulingSubpart();
    	newSchedulingSubpart.setAutoSpreadInTime(isAutoSpreadInTime());
    	if (getCreditConfigs() != null){
    		CourseCreditUnitConfig ccuc = null;
    		CourseCreditUnitConfig newCcuc = null;
    		for (Iterator credIt = getCreditConfigs().iterator(); credIt.hasNext();){
    			ccuc = (CourseCreditUnitConfig) credIt.next();
    			newCcuc = (CourseCreditUnitConfig) ccuc.clone();
    			newCcuc.setOwner(newSchedulingSubpart);
    			newSchedulingSubpart.addTocreditConfigs(newCcuc);
    		}
    	}
    	newSchedulingSubpart.setDatePattern(getDatePattern());
    	newSchedulingSubpart.setItype(getItype());
    	newSchedulingSubpart.setMinutesPerWk(getMinutesPerWk());
    	newSchedulingSubpart.setStudentAllowOverlap(isStudentAllowOverlap());
    	return(newSchedulingSubpart);
    }

    public Object cloneWithPreferences(){
    	SchedulingSubpart newSchedulingSubpart = (SchedulingSubpart)clone();
    	if (getPreferences() != null){
			Preference p = null;
			Preference newPref = null;
			for (Iterator prefIt = getPreferences().iterator(); prefIt.hasNext();){
				p = (Preference) prefIt.next();	
				if (!(p instanceof DistributionPref)) {
					newPref = (Preference)p.clone();
					newPref.setOwner(newSchedulingSubpart);
					newSchedulingSubpart.addTopreferences(newPref);
				}
			}
		}
    	return(newSchedulingSubpart);
    }
   
    public Object cloneDeep(){
    	SchedulingSubpart newSchedulingSubpart = (SchedulingSubpart)cloneWithPreferences();
    	HashMap childClassToParentClass = new HashMap();
    	if (getClasses() != null){
    		Class_ origClass = null;
    		Class_ newClass = null;
    		for (Iterator cIt = getClasses().iterator(); cIt.hasNext();){
    			origClass = (Class_) cIt.next();
    			newClass = (Class_) origClass.cloneWithPreferences();
    			newClass.setSchedulingSubpart(newSchedulingSubpart);
    			newSchedulingSubpart.addToclasses(newClass);
    			newClass.setSectionNumberCache(origClass.getSectionNumberCache());
    			newClass.setUniqueIdRolledForwardFrom(origClass.getUniqueId());
    			if (origClass.getChildClasses() != null){
    				Class_ childClass = null;
    				for (Iterator ccIt = origClass.getChildClasses().iterator(); ccIt.hasNext();){
    					childClass = (Class_) ccIt.next();
    					childClassToParentClass.put(childClass.getUniqueId(), newClass);
    				}
    			}
    		}
    	}
    	if (getChildSubparts() != null){
    		SchedulingSubpart origChildSubpart = null;
    		SchedulingSubpart newChildSubpart = null;
    		for (Iterator ssIt = getChildSubparts().iterator(); ssIt.hasNext();){
    			origChildSubpart = (SchedulingSubpart) ssIt.next();
    			newChildSubpart = (SchedulingSubpart)origChildSubpart.cloneDeep();
    			newChildSubpart.setParentSubpart(newSchedulingSubpart);
    			newSchedulingSubpart.addTochildSubparts(newChildSubpart);
    			if (newChildSubpart.getClasses() != null){
    				Class_ newChildClass = null;
    				Class_ newParentClass = null;
    				for (Iterator nccIt = newChildSubpart.getClasses().iterator(); nccIt.hasNext();){
    					newChildClass = (Class_) nccIt.next();
    					newParentClass = (Class_) childClassToParentClass.get(newChildClass.getUniqueIdRolledForwardFrom());
    					newChildClass.setParentClass(newParentClass);
    					newParentClass.addTochildClasses(newChildClass);
    					newChildClass.setUniqueIdRolledForwardFrom(null);
    				}
    			}
    		}
    	}
    	if (newSchedulingSubpart.getClasses() != null && getParentSubpart() == null){
    		Class_ newClass = null;
    		for (Iterator cIt = getClasses().iterator(); cIt.hasNext();){
    			newClass = (Class_) cIt.next();
    			newClass.setUniqueIdRolledForwardFrom(null);
    		}
    	}	
    	return(newSchedulingSubpart);
    }
    
    public static SchedulingSubpart findByIdRolledForwardFrom(Long sessionId, Long uniqueIdRolledForwardFrom) {
        return (SchedulingSubpart)new SchedulingSubpartDAO().
            getSession().
            createQuery("select ss from SchedulingSubpart ss where ss.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId and ss.uniqueIdRolledForwardFrom=:uniqueIdRolledForwardFrom").
            setLong("sessionId", sessionId.longValue()).
            setLong("uniqueIdRolledForwardFrom", uniqueIdRolledForwardFrom.longValue()).
            setCacheable(true).
            uniqueResult();
    }
    
	@Override
	public Department getDepartment() { return getManagingDept(); }
	
	/**
	 * Check if the given selection is valid.
	 * @param datePattern selected date pattern (alternative pattern sets are allowed)
	 * @param timePattern selected time pattern
	 * @return true if there is a valid combinations of days meeting the given criteria
	 */
	public boolean isValidCombination(DatePattern datePattern, TimePattern timePattern) {
		return getInstrOfferingConfig().getDurationModel().isValidCombination(getMinutesPerWk(), datePattern, timePattern);
	}
	
	/**
	 * Get combinations of days that meet the given selection.
	 * @param datePattern selected date pattern (alternative pattern sets are NOT allowed)
	 * @param timePattern selected time pattern
	 * @return list of day codes (days of week, given by {@link TimePattern#getDays()}) that meet the selected minutes and date pattern
	 */
	public Set<Integer> getDayCodes(DatePattern datePattern, TimePattern timePattern) {
		return getInstrOfferingConfig().getDurationModel().getDayCodes(getMinutesPerWk(), datePattern, timePattern);
	}
	
	/**
	 * Get all dates meeting the given selection.
	 * @param datePattern selected date pattern
	 * @param dayCode selected days of week (alternative pattern sets are NOT allowed)
	 * @param minutesPerMeeting minutes per meeting
	 * @return list of dates that meet the selected minutes and date pattern
	 */
	public List<Date> getDates(DatePattern datePattern, int dayCode, int minutesPerMeeting) {
		return getInstrOfferingConfig().getDurationModel().getDates(getMinutesPerWk(), datePattern, dayCode, minutesPerMeeting);
	}
	
	public boolean isParentOf(SchedulingSubpart subpart) {
		SchedulingSubpart parent = subpart.getParentSubpart();
		return parent != null && (this.equals(parent) || isParentOf(parent)); 
	}
	
	public boolean isInstructorAssignmentNeeded() {
		for (Class_ c: getClasses())
			if (c.isInstructorAssignmentNeeded()) return true;
		return false;
	}
	
	public int getNbrInstructors() {
		int instructors = 0;
		for (Class_ c: getClasses())
			for (TeachingClassRequest tcr: c.getTeachingRequests())
				if (tcr.isAssignInstructor())
					instructors += tcr.getTeachingRequest().getNbrInstructors();
		return instructors;
	}
	
	public float getTeachingLoad() {
		int instructors = 0; float totalLoad = 0f;
		for (Class_ c: getClasses())
			for (TeachingClassRequest tcr: c.getTeachingRequests())
				if (tcr.isAssignInstructor()) {
					instructors += tcr.getTeachingRequest().getNbrInstructors();
					totalLoad += tcr.getTeachingRequest().getNbrInstructors() * tcr.getTeachingRequest().getTeachingLoad();
				}
		return totalLoad / instructors;
	}
}
