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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.cpsolver.coursett.constraint.IgnoreStudentConflictsConstraint;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.hibernate.FlushMode;
import org.hibernate.query.Query;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface;
import org.unitime.timetable.model.base.BaseClass_;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.comparators.NavigationComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.SectioningInfoDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.CommitedClassAssignmentProxy;
import org.unitime.timetable.solver.course.ui.ClassAssignmentInfo;
import org.unitime.timetable.solver.course.ui.ClassInstructorInfo;
import org.unitime.timetable.solver.course.ui.ClassRoomInfo;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.duration.DurationModel;
import org.unitime.timetable.webutil.Navigation;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Entity
@Table(name = "class_")
public class Class_ extends BaseClass_ {
    private static final long serialVersionUID = 1L;
    private static CourseMessages MSG = Localization.create(CourseMessages.class);
    protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	/* [CONSTRUCTOR MARKER BEGIN] */
	public Class_ () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Class_ (java.lang.Long uniqueId) {
		super(uniqueId);
	}

    /* [CONSTRUCTOR MARKER END] */

	@Transient
    public String getCourseName(){
		return getSchedulingSubpart().getCourseName();
	}

	@Transient
    public String getCourseNameWithTitle(){
		return getSchedulingSubpart().getCourseNameWithTitle();
	}

    /**
     *
     * @return
     */
	@Transient
    public String getItypeDesc() {
   		return getSchedulingSubpart().getItypeDesc();
    }

	@Transient
    public Department getManagingDept(){
    	if (super.getManagingDept()==null) return getControllingDept();
        return super.getManagingDept();
    }
    
	@Transient
    public Department getControllingDept() {
		if (getSchedulingSubpart() == null) return null;
    	if (super.getControllingDept() == null) return getSchedulingSubpart().getControllingDept();
    	return super.getControllingDept();
    }
    
    @Override
    public void setManagingDept(Department dept) {
    	if (super.getManagingDept() == null)
    		super.setManagingDept(dept);
    	else
    		setManagingDept(dept, null, Class_DAO.getInstance().getSession());
    }

    public void setManagingDept(Department dept, UserContext user, org.hibernate.Session hibSession) {
        Department oldDept = getManagingDept();
        super.setManagingDept(dept);
        if (dept==null) return;
        if (oldDept!=null && !oldDept.equals(dept) && getAssignments()!=null && !getAssignments().isEmpty()) {
        	boolean deleteEvent = false;
        	String deletedAssignment = null;
            for (Iterator<Assignment> i = getAssignments().iterator(); i.hasNext();) {
                Assignment a = i.next();
                if (!a.getSolution().getOwner().getDepartments().contains(dept)) {
                	if (a.getSolution().isCommited()) {
                		deleteEvent = true;
                		deletedAssignment = a.getPlacement().getName();
                	}
                	hibSession.remove(a);
                	i.remove();
                }
            }
        	ClassEvent event = getEvent();
        	if (event != null) {
        		if (deleteEvent) {
            		if (ApplicationProperty.ClassAssignmentChangePastMeetings.isTrue()) {
                		hibSession.remove(event);
                	} else {
                		Calendar cal = Calendar.getInstance(Locale.US);
                		cal.set(Calendar.HOUR_OF_DAY, 0);
                		cal.set(Calendar.MINUTE, 0);
                		cal.set(Calendar.SECOND, 0);
                		cal.set(Calendar.MILLISECOND, 0);
                		Date today = cal.getTime();

                    	for (Iterator<Meeting> i = event.getMeetings().iterator(); i.hasNext(); )
                    		if (!i.next().getMeetingDate().before(today)) i.remove();
                    	
                    	if (event.getMeetings().isEmpty()) {
                    		hibSession.remove(event);
                    	} else if (user != null) {
                			if (event.getNotes() == null)
                				event.setNotes(new HashSet<EventNote>());
            				EventNote note = new EventNote();
            				note.setEvent(event);
            				note.setNoteType(EventNote.sEventNoteTypeDeletion);
            				note.setTimeStamp(new Date());
            				note.setUser(user.getTrueName());
            				note.setUserId(user.getTrueExternalUserId());
            				note.setTextNote(MSG.classNoteUnassigned(deletedAssignment));
            				note.setMeetings(MSG.classMeetingsNotApplicable());
            				event.getNotes().add(note);
                    		hibSession.persist(event);
                    	}
                	}
            	} else {
            		event.setEventName(getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseName() + " " + getItypeDesc().trim() + " " + getSectionNumberString(hibSession));
            		if (event.getUniqueId() == null)
            			hibSession.persist(event);
            		else
            			hibSession.merge(event);
            	}
            }
        }
    }

    /**
     * Retrieves the department for the subject area of the
     * controlling course offering for the class
     * @return Department object
     */
	@Transient
    public Department getDepartmentForSubjectArea() {
        Department dept = this.getSchedulingSubpart()
			        		.getInstrOfferingConfig()
			        		.getControllingCourseOffering()
			        		.getSubjectArea()
			        		.getDepartment();
        return dept;
    }

	@Transient
    public Session getSession(){
    	return (this.getSchedulingSubpart().getSession());
    }

	@Transient
	public Long getSessionId() {
		return (this.getSchedulingSubpart().getSessionId());
	}

    public Set classInstructorPrefsOfType(Class type) {
    	List<DepartmentalInstructor> instructors = getLeadInstructors();
    	if (instructors.isEmpty()) return null;
    	if (InstructorCoursePref.class.equals(type)) return null;
    	Set ret = null;
    	for (DepartmentalInstructor instructor: instructors) {
    		if (ret == null)
    			ret = instructor.getPreferences(type);
    		else
    			ret = combinePreferences(ret, instructor.getPreferences(type));
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
    		} else if (pref instanceof InstructorAttributePref) {
    			InstructorAttribute at = ((InstructorAttributePref)pref).getAttribute();
    			if (at.getDepartment() == null || getManagingDept().equals(at.getDepartment()))
    				ret.add(pref);
    		} else {
    			ret.add(pref);
    		}
    	}
    	return ret;
    }

    private Set<Preference> combinePreferences(Set<Preference> instrPrefs1, Set<Preference> instrPrefs2) {
    	if (instrPrefs1==null || instrPrefs1.isEmpty()) return instrPrefs2;
    	if (instrPrefs2==null || instrPrefs2.isEmpty()) return instrPrefs1;

    	Set<Preference> ret = new TreeSet<Preference>();

    	TimePref tp = null;
    	for (Iterator<Preference> i=instrPrefs1.iterator();i.hasNext();) {
    		Preference p1 = i.next();
    		if (p1 instanceof TimePref) {
    			if (tp==null) {
    				tp = (TimePref)p1.clone();
    			} else tp.combineWith((TimePref)p1,false);
    		} else ret.add(p1);
    	}
    	for (Iterator<Preference> i=instrPrefs2.iterator();i.hasNext();) {
    		Preference p2 = i.next();
    		if (p2 instanceof TimePref) {
    			if (tp==null) {
    				tp = (TimePref)p2.clone();
    			} else tp.combineWith((TimePref)p2,false);
    		} else {
    			Preference p1 = null;
    			for (Iterator<Preference> j=ret.iterator();j.hasNext();) {
    				Preference p = j.next();
    				if (p.isSame(p2, this)) {
    					p1 = p; j.remove(); break;
    				}
    			}
    			if (p1==null) {
    				ret.add(p2);
    			} else {
    				Preference combPref = (Preference)p1.clone();
    				PreferenceCombination com = new MinMaxPreferenceCombination();
    				com.addPreferenceProlog(p1.getPrefLevel().getPrefProlog());
    				com.addPreferenceProlog(p2.getPrefLevel().getPrefProlog());
    				combPref.setPrefLevel(PreferenceLevel.getPreferenceLevel(com.getPreferenceProlog()));
    				ret.add(combPref);
    			}
    		}
		}

    	if (tp!=null) ret.add(tp);

    	return ret;
    }

    private Set combinePreferences(Class type, Set subpartPrefs, Set instrPrefs) {
		if (TimePref.class.equals(type)) {
			if (subpartPrefs==null || subpartPrefs.isEmpty() || instrPrefs==null || instrPrefs.isEmpty()) return subpartPrefs;
			TimePref instrPref = (TimePref)instrPrefs.iterator().next(); //there has to be only one TimePref for instructor/department/session

			Set ret = new TreeSet();
			for (Iterator i=subpartPrefs.iterator();i.hasNext();) {
				TimePref pref = (TimePref)((TimePref)i.next()).clone();
				pref.combineWith(instrPref, false);
				ret.add(pref);
			}

			return ret;
		}

		if (subpartPrefs==null || subpartPrefs.isEmpty()) return instrPrefs;
		if (instrPrefs==null || instrPrefs.isEmpty()) return subpartPrefs;

		Set ret = new TreeSet(subpartPrefs);
		for (Iterator i=instrPrefs.iterator();i.hasNext();) {
			Preference instrPref = (Preference)i.next();
			Preference subpartPref = null;
			for (Iterator j=ret.iterator();j.hasNext();) {
				Preference p = (Preference)j.next();
				if (p.isSame(instrPref, this)) {
					subpartPref = p; j.remove(); break;
				}
			}
			if (subpartPref==null) {
				ret.add(instrPref);
			} else {
				Preference combPref = (Preference)subpartPref.clone();
				PreferenceCombination com = new MinMaxPreferenceCombination();
				com.addPreferenceProlog(instrPref.getPrefLevel().getPrefProlog());
				com.addPreferenceProlog(subpartPref.getPrefLevel().getPrefProlog());
				combPref.setPrefLevel(PreferenceLevel.getPreferenceLevel(com.getPreferenceProlog()));
				ret.add(combPref);
			}
		}
		return ret;
    }

    private Set combinePreferences(Class type, Set classPrefs, Set subpartPrefs, Set instrPrefs) {
		Set ret = new TreeSet(classPrefs);
		Set combined = combinePreferences(type, subpartPrefs, instrPrefs);

		if (combined==null) return ret;

		for (Iterator i=combined.iterator();i.hasNext();) {
			Preference combPref = (Preference)i.next();
			Preference classPref = null;
			for (Iterator j=classPrefs.iterator();j.hasNext();) {
				Preference p = (Preference)j.next();
				if (p.isSame(combPref, this)) {
					classPref = p; break;
				}
			}
			if (classPref==null) ret.add(combPref);
		}

		return ret;
    }

	@Transient
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
    	TreeSet prefs = new TreeSet();
    	if (getDistributionObjects()!=null) {
    		for (Iterator i=getDistributionObjects().iterator();i.hasNext();) {
    			DistributionObject distObj = (DistributionObject)i.next();
    			DistributionPref pref = distObj.getDistributionPref();
    			if (owningDept==null || owningDept.equals(pref.getOwner()))
    				prefs.add(pref);
    		}
    	}
    	if (getSchedulingSubpart().getDistributionObjects()!=null) {
    		for (Iterator i=getSchedulingSubpart().getDistributionObjects().iterator();i.hasNext();) {
    			DistributionObject distObj = (DistributionObject)i.next();
    			DistributionPref pref = distObj.getDistributionPref();
    			if (owningDept==null || owningDept.equals(pref.getOwner()))
    				prefs.add(pref);
    		}
    	}
    	return prefs;
    }
    
    private Set fixDurationInTimePreferences(Set prefs) {
    	if (prefs == null || prefs.isEmpty()) return prefs;
    	DatePattern dp = effectiveDatePattern();
    	if (dp == null) return prefs;
    	DurationModel dm = getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
    	
    	Set ret = new TreeSet(prefs);
    	List<TimePref> fixed = new ArrayList<TimePref>();
		for (Iterator i=ret.iterator();i.hasNext();) {
			Preference pref = (Preference)i.next();
			if (pref instanceof TimePref) {
				TimePref tp = (TimePref) pref;
				if (tp.getTimePattern().getType() != null && tp.getTimePattern().isExactTime()) continue;
				Set<Integer> days = dm.getDayCodes(getSchedulingSubpart().getMinutesPerWk(), dp, tp.getTimePattern());
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

    public Set effectivePreferences(Class type, Vector leadInstructors, boolean fixDurationInTimePreferences) {
    	Department mngDept = getManagingDept();
    	if (DistributionPref.class.equals(type)) {
    		return effectiveDistributionPreferences(mngDept);
    	}
    	
    	if (leadInstructors==null || leadInstructors.isEmpty()) return effectivePreferences(type, fixDurationInTimePreferences);

    	Set instrPrefs = null;
    	for (Enumeration e=leadInstructors.elements();e.hasMoreElements();) {
    		DepartmentalInstructor leadInstructor = (DepartmentalInstructor)e.nextElement();
    		instrPrefs = combinePreferences(instrPrefs, leadInstructor.prefsOfTypeForDepartment(type, getControllingDept()));
    	}
		// weaken instructor preferences, if needed
		if (instrPrefs != null && !instrPrefs.isEmpty()) {
			if (TimePref.class.equals(type)) {
				if (mngDept.isExternalManager() && !mngDept.isAllowReqTime())
					instrPrefs = weakenHardPreferences(instrPrefs);
			} else {
				if (mngDept.isExternalManager() && !mngDept.isAllowReqRoom())
					instrPrefs = weakenHardPreferences(instrPrefs);
			}
		}
		// if external department, remove departmental preferences
		if (instrPrefs != null && !instrPrefs.isEmpty()) { 
			instrPrefs = removeDepartmentalPreferences(instrPrefs);
		}
		
		// take subpart preferences
		Set subpartPrefs = getSchedulingSubpart().effectivePreferences(type, this);
		if (subpartPrefs != null && !subpartPrefs.isEmpty() && !mngDept.equals(getSchedulingSubpart().getManagingDept())) {
			// different managers -> weaken preferences, if needed
			if (TimePref.class.equals(type)) {
				if (mngDept.isExternalManager() && !mngDept.isAllowReqTime())
					subpartPrefs = weakenHardPreferences(subpartPrefs);
			} else {
				if (mngDept.isExternalManager() && !mngDept.isAllowReqRoom())
					subpartPrefs = weakenHardPreferences(subpartPrefs);
			}
			// remove departmental preferences
    		if (instrPrefs != null && !instrPrefs.isEmpty()) { 
    			subpartPrefs = removeDepartmentalPreferences(subpartPrefs);
    		}
		}
		
		Set ret = removeNeutralPreferences(combinePreferences(type, subpartPrefs, instrPrefs));
		return fixDurationInTimePreferences ? fixDurationInTimePreferences(ret) : ret;
    }
    
    public Set effectivePreferences(Class type, boolean fixDurationInTimePreferences) {
    	Department mngDept = getManagingDept();
    	// special handling of distribution preferences
    	if (DistributionPref.class.equals(type)) {
    		Set prefs = effectiveDistributionPreferences(mngDept);
    		if (!mngDept.isExternalManager()) {
    			Set instPref = classInstructorPrefsOfType(type);
    			if (instPref!=null) prefs.addAll(instPref);
    		}
    		return prefs;
    	}
    	
    	if (InstructorAttributePref.class.equals(type) || InstructorPref.class.equals(type)) {
    		Set<Preference> prefs = new TreeSet<Preference>();
    		for (TeachingClassRequest tcr: getTeachingRequests()) {
    			if (tcr.getAssignInstructor())
    				prefs.addAll(tcr.getTeachingRequest().getPreferences(type));
    		}
    		return prefs;
    	}

    	Set classPrefs = getPreferences(type, this);

    	Set instrPrefs = null;
    	// take instructor preferences if allowed
    	if (mngDept.isInheritInstructorPreferences()) {
    		instrPrefs = classInstructorPrefsOfType(type);
    		// weaken instructor preferences, if needed
    		if (instrPrefs != null && !instrPrefs.isEmpty()) {
    			if (mngDept.isExternalManager() && TimePref.class.equals(type)) {
    				if (!mngDept.isAllowReqTime())
    					instrPrefs = weakenHardPreferences(instrPrefs);
    			} else {
    				if (mngDept.isExternalManager() && !mngDept.isAllowReqRoom())
    					instrPrefs = weakenHardPreferences(instrPrefs);
    			}
    		}
    		// if external department, remove departmental preferences
    		if (instrPrefs != null && !instrPrefs.isEmpty()) { 
    			instrPrefs = removeDepartmentalPreferences(instrPrefs);
    		}
    	}

    	boolean hasExactTimePattern = false;
		if (TimePref.class.equals(type)) {
    		if (classPrefs != null && !classPrefs.isEmpty()) {
    			for (Iterator i=classPrefs.iterator();i.hasNext();) {
    				TimePref tp = (TimePref)i.next();
    				if (tp.getTimePattern()!=null && tp.getTimePattern().isExactTime()) {
    					hasExactTimePattern = true; break;
    				}
    			}
    		}
		}
		
		// take subpart preferences
		Set subpartPrefs = (hasExactTimePattern ? null : getSchedulingSubpart().effectivePreferences(type, this));
		if (subpartPrefs != null && !subpartPrefs.isEmpty() && !mngDept.equals(getSchedulingSubpart().getManagingDept())) {
			// different managers -> weaken preferences, if needed
			if (TimePref.class.equals(type)) {
				if (mngDept.isExternalManager() && !mngDept.isAllowReqTime())
					subpartPrefs = weakenHardPreferences(subpartPrefs);
			} else {
				if (mngDept.isExternalManager() && !mngDept.isAllowReqRoom())
					subpartPrefs = weakenHardPreferences(subpartPrefs);
			}
			// remove departmental preferences
    		if (instrPrefs != null && !instrPrefs.isEmpty()) { 
    			subpartPrefs = removeDepartmentalPreferences(subpartPrefs);
    		}
		}
		
		Set ret = removeNeutralPreferences(combinePreferences(type, classPrefs, subpartPrefs, instrPrefs));
		return fixDurationInTimePreferences ? fixDurationInTimePreferences(ret) : ret;
    }

    public String instructorHtml(String instructorNameFormat){
    	StringBuffer sb = new StringBuffer();
    	if (this.getClassInstructors()==null) return "";
    	InstructorComparator ic = new InstructorComparator();
    	if (ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue()) ic.setNameFormat(instructorNameFormat);
    	TreeSet sortedInstructors = new TreeSet(ic);
    	sortedInstructors.addAll(this.getClassInstructors());

    	Iterator it = sortedInstructors.iterator();
    	ClassInstructor ci = null;
    	while (it.hasNext()){
    		ci = (ClassInstructor) it.next();
    		String title = ci.getInstructor().getNameLastFirst();
    		title += " (" + (ci.getResponsibility() == null ? "" : ci.getResponsibility().getLabel() + " ") +
    				ci.getPercentShare()+"%"+(ci.isLead().booleanValue()?", " + MSG.toolTipInstructorLead():"")+")";
    		if (!isDisplayInstructor().booleanValue()){
    			title += MSG.toolTipInstructorDoNotDisplay();
    		}
    		if (ci.isLead().booleanValue()){
    			sb.append("<span style='font-weight:bold;"+(isDisplayInstructor().booleanValue()?"":"font-style:italic;")+"' title='"+title+"'>");
    		} else {
    			sb.append("<span title='"+title+"'>");
    		}
    		sb.append(ci.getInstructor().getName(instructorNameFormat));
    		if (ci.getResponsibility() != null && ci.getResponsibility().getAbbreviation() != null && !ci.getResponsibility().getAbbreviation().isEmpty())
    			sb.append(" (" + ci.getResponsibility().getAbbreviation() + ")");
    		sb.append("</span>");
    		if (it.hasNext()) sb.append("<br>");
    		sb.append("\n");
    	}
    	return (sb.toString());
    }
    
    public String instructorText(String instructorNameFormat, String separator){
    	if (getClassInstructors() == null) return "";
    	
    	InstructorComparator ic = new InstructorComparator();
    	if (ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue()) ic.setNameFormat(instructorNameFormat);
    	TreeSet sortedInstructors = new TreeSet(ic);
    	sortedInstructors.addAll(this.getClassInstructors());

    	StringBuffer sb = new StringBuffer();
    	
    	for (Iterator it = sortedInstructors.iterator(); it.hasNext();) {
    		ClassInstructor ci = (ClassInstructor) it.next();
    		sb.append(ci.getInstructor().getName(instructorNameFormat));
    		if (it.hasNext()) sb.append(separator);
    	}
    	
    	return sb.toString();
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
        if (patterns == null || patterns.isEmpty()) {
        	DurationModel dm = getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
			Integer ah = dm.getArrangedHours(getSchedulingSubpart().getMinutesPerWk(), effectiveDatePattern());
            if (ah == null) {
                sb.append("<span title='" + MSG.arrangeHours() + "'>" + MSG.arrHrs() + "</span>");
            } else {
                sb.append("<span title='" + MSG.arrangeHoursN(ah) + "'>" + MSG.arrHrsN(ah) + "</span>");
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

	@Transient
	public Integer getSectionNumber() {
		return getSectionNumber(null, true);
	}
	
	public Integer getSectionNumber(org.hibernate.Session hibSession) {
		return getSectionNumber(hibSession, true);
	}

	public Integer getSectionNumber(boolean save) {
		return getSectionNumber(null, save);
	}

    public Integer getSectionNumber(org.hibernate.Session hibSession, boolean save) {
    	Integer sectionNumber = getSectionNumberCache();
    	if (sectionNumber!=null) return sectionNumber;

    	Comparator cmp = new Comparator() {
    		public int compare(Object o1, Object o2) {
    			Class_ c1 = (Class_)o1;
    			Class_ c2 = (Class_)o2;
    			if (c1.getParentClass()==null || c2.getParentClass()==null || c1.getParentClass().equals(c2.getParentClass()))
    				return c1.getUniqueId().compareTo(c2.getUniqueId());
    			else
    				return compare(c1.getParentClass(),c2.getParentClass());
    		}
    	};

    	int idx = 0;
    	for (Iterator i=getSchedulingSubpart().getClasses().iterator();i.hasNext();) {
    		Class_ clazz = (Class_)i.next();
    		if (cmp.compare(clazz, this)<0) idx++;
    	}

    	sectionNumber = Integer.valueOf(getSchedulingSubpart().getInstrOfferingConfig().getFirstSectionNumber(getSchedulingSubpart().getItype())+idx);
		setSectionNumberCache(sectionNumber);

    	if (save) {
    		if (hibSession != null) {
    			hibSession.merge(this);
    		} else {
        		(new Class_DAO()).getSession().merge(this);
        		(new Class_DAO()).getSession().flush();
    		}
    	}

    	return sectionNumber;
    }

	@Transient
    public String getSectionNumberString(){
    	return getSectionNumber()+getSchedulingSubpart().getSchedulingSubpartSuffix();
    }

    public String getSectionNumberString(org.hibernate.Session hibSession){
    	return getSectionNumber(hibSession)+getSchedulingSubpart().getSchedulingSubpartSuffix(hibSession);
    }

	@Transient
    public List<DepartmentalInstructor> getLeadInstructors() {
    	List<DepartmentalInstructor> ret = new ArrayList<DepartmentalInstructor>();
    	if (getClassInstructors() == null) {
    		return ret;
    	}
    	for (ClassInstructor classInstructor: getClassInstructors()) {
    		if (classInstructor.isLead().booleanValue()) ret.add(classInstructor.getInstructor());
    	}
    	return ret;
    }

    /**
     * @return Class Label of the form {CourseName} {Itype} {[config]} {Section No.}
     */
	@Transient
    public String getClassLabel() {
        // return getCourseName()+" "+getItypeDesc().trim()+" "+getSectionNumberString();
    	return getClassLabel(getSchedulingSubpart().getControllingCourseOffering());
    }
    
    public String getClassLabel(CourseOffering course, boolean includeSuffix) {
    	if (includeSuffix) {
    		String extId = getClassSuffix(course);
    		return course.getCourseName() + " " + getItypeDesc().trim() + " " + getSectionNumberString() + 
    				(extId == null || extId.isEmpty() || extId.equalsIgnoreCase(getSectionNumberString()) ? "" : " - " + extId);
    	} else {
    		// return course.getCourseName() + " " + getItypeDesc().trim() + " " + getSectionNumberString();
    		return getClassLabel(course);
    	}
    }
    
    public String getClassLabel(CourseOffering course, boolean includeSuffix, boolean includeConfigName) {
    	String label = getClassLabel(course, includeSuffix);
    	if (includeConfigName && course.getInstructionalOffering().getInstrOfferingConfigs().size() > 1)
    		return label + " (" + getSchedulingSubpart().getInstrOfferingConfig().getName() + ")";
    	return label;
    }
    
    public String getClassLabel(boolean includeSuffix, boolean includeConfigName) {
    	return getClassLabel(getSchedulingSubpart().getControllingCourseOffering(), includeSuffix, includeConfigName);
    }
    
    public String getClassLabel(boolean includeSuffix) {
    	return getClassLabel(getSchedulingSubpart().getControllingCourseOffering(), includeSuffix, false);
    }

    public String getClassLabel(org.hibernate.Session hibSession) {
    	if (getSectionNumberCache() == null) {
    		getSectionNumber(hibSession, true);
    		getSchedulingSubpart().getSchedulingSubpartSuffix(hibSession, true);
    	}
    	// return getCourseName()+" "+getItypeDesc().trim()+" "+getSectionNumberString(hibSession);
    	return getClassLabel(getSchedulingSubpart().getControllingCourseOffering());
    }

	@Transient
    public String getClassLabelWithTitle() {
    	// return getCourseNameWithTitle()+" "+getItypeDesc().trim()+" "+getSectionNumberString();
    	return getClassLabelWithTitle(getSchedulingSubpart().getControllingCourseOffering());
    }

    /**
     * @see getClassLabel()
     */
    public String toString() {
        return getClassLabel();
    }

	/**
	 * @return Class type to distinguish the sub class in PrefGroup
	 */
	@Transient
	public Class getInstanceOf() {
	    return Class_.class;
	}

	public String htmlLabel(){
		return(getItypeDesc()+" "+getSectionNumberString());
	}

	public boolean canBeDeleted(){
		if (this.getChildClasses() != null && this.getChildClasses().size() > 0){
			return(false);
		}
		if (this.getParentClass() != null){
			int totalCapacity = 0;
			Class_ c = null;
			for(Iterator it = this.getParentClass().getChildClasses().iterator(); it.hasNext();){
				c = (Class_) it.next();
				totalCapacity += c.getExpectedCapacity().intValue();
			}
			totalCapacity -= this.getExpectedCapacity().intValue();
			if (totalCapacity < this.getParentClass().getExpectedCapacity().intValue()){
				return(false);
			}
		}
		return(true);
	}

    /**
     * Remove class from instructor list
     * @param ci
     */
    public void removeClassInstructor(ClassInstructor classInstr) {
        Set s = this.getClassInstructors();
        for( Iterator iter=s.iterator(); iter.hasNext(); ) {
            ClassInstructor ci = (ClassInstructor) iter.next();
            if(ci.getUniqueId().intValue()==classInstr.getUniqueId().intValue()) {
                s.remove(ci);
                break;
            }
        }
    }

    public DatePattern effectiveDatePattern() {
    	if (getDatePattern()!=null) return getDatePattern();
    	return getSchedulingSubpart().effectiveDatePattern();
    }

	@Transient
    public Set<Location> getAvailableRooms() {

    	Set<Location> rooms =  new TreeSet<Location>();
        for (Iterator i=getManagingDept().getRoomDepts().iterator();i.hasNext();) {
        	RoomDept roomDept = (RoomDept)i.next();
        	rooms.add(roomDept.getRoom());
        }

        return rooms;
    }

	@Transient
    public Set getAvailableRoomFeatures() {
    	Set features = new TreeSet(GlobalRoomFeature.getAllGlobalRoomFeatures(getSession()));
    	Department dept = getManagingDept();
    	if (dept!=null)
    		features.addAll(DepartmentRoomFeature.getAllDepartmentRoomFeatures(dept));
    	return features;

    }

	@Transient
    public Set getAvailableRoomGroups() {
    	Set groups = super.getAvailableRoomGroups();
    	Department dept = getManagingDept();
    	if (dept!=null)
    		groups.addAll(RoomGroup.getAllDepartmentRoomGroups(dept));
    	return groups;
    }
    
	@Transient
	public Set getAvailableAttributeTypes() {
		return getControllingDept().getAvailableAttributeTypes();
    }

	@Transient
	public Set getAvailableAttributes() {
		return getControllingDept().getAvailableAttributes();
    }

    public Class_ getNextClass(SessionContext context, Right right) {
    	return getNextClass(context, new NavigationComparator(), right);
    }

    public Class_ getPreviousClass(SessionContext context, Right right) {
    	return getPreviousClass(context, new NavigationComparator(), right);
    }

    public Class_ getNextClass(SessionContext context, Comparator cmp, Right right) {
    	Long nextId = Navigation.getNext(context, Navigation.sClassLevel, getUniqueId());
    	if (nextId!=null) {
    		if (nextId.longValue()<0) return null;
    		Class_ next = (new Class_DAO()).get(nextId);
    		if (next==null) return null;
    		if (right != null && !context.hasPermission(Department.class.equals(right.type()) ? next.getControllingDept() : next, right)) return next.getNextClass(context, cmp, right);
    		return next;
    	}
    	Class_ next = null;
    	SchedulingSubpart subpart = getSchedulingSubpart();
    	while (next==null) {
    		if (subpart==null) break;
        	for (Iterator i=subpart.getClasses().iterator();i.hasNext();) {
        		Class_ c = (Class_)i.next();
        		if (right != null && !context.hasPermission(Department.class.equals(right.type()) ? c.getControllingDept() : c, right)) continue;
        		if (subpart.equals(getSchedulingSubpart()) && cmp.compare(this, c)>=0) continue;
        		if (next==null || cmp.compare(next,c)>0)
        			next = c;
        	}
    		subpart = subpart.getNextSchedulingSubpart(context, cmp, null);
    	}
    	return next;
    }

    public Class_ getPreviousClass(SessionContext context, Comparator cmp, Right right) {
    	Long previosId = Navigation.getPrevious(context, Navigation.sClassLevel, getUniqueId());
    	if (previosId!=null) {
    		if (previosId.longValue()<0) return null;
    		Class_ previos = (new Class_DAO()).get(previosId);
    		if (previos==null) return null;
    		if (right != null && !context.hasPermission(Department.class.equals(right.type()) ? previos.getControllingDept() : previos, right)) return previos.getPreviousClass(context, cmp, right);
    		return previos;
    	}
    	Class_ previous = null;
    	SchedulingSubpart subpart = getSchedulingSubpart();
    	while (previous==null) {
    		if (subpart==null) break;
        	for (Iterator i=subpart.getClasses().iterator();i.hasNext();) {
        		Class_ c = (Class_)i.next();
        		if (right != null && !context.hasPermission(Department.class.equals(right.type()) ? c.getControllingDept() : c, right)) continue;
        		if (subpart.equals(getSchedulingSubpart()) && cmp.compare(this, c)<=0) continue;
        		if (previous==null || cmp.compare(previous,c)<0)
        			previous = c;
        	}
    		subpart = subpart.getPreviousSchedulingSubpart(context, cmp, null);
    	}
    	return previous;
    }

    /**
     * Deletes all distribution prefs and updates the class_ objects
     * @param hibSession
     */
    public void deleteAllDistributionPreferences(org.hibernate.Session hibSession) {
    	deleteAllDistributionPreferences(hibSession, true);
    }

    /**
     * Deletes all distribution prefs
     * @param hibSession
     * @param updateClass If true then class_ object is updated
     */
    public void deleteAllDistributionPreferences(org.hibernate.Session hibSession, boolean updateClass) {
    	boolean deleted = false;
    	if (getDistributionObjects()==null) return;
    	for (Iterator i=getDistributionObjects().iterator();i.hasNext();) {
    		DistributionObject relatedObject = (DistributionObject)i.next();
    		DistributionPref distributionPref = relatedObject.getDistributionPref();
    		distributionPref.getDistributionObjects().remove(relatedObject);
    		Integer seqNo = relatedObject.getSequenceNumber();
			hibSession.remove(relatedObject);
			deleted = true;
			if (distributionPref.getDistributionObjects().isEmpty()) {
				PreferenceGroup owner = distributionPref.getOwner();
				owner.getPreferences().remove(distributionPref);
				getPreferences().remove(distributionPref);
				hibSession.merge(owner);
				hibSession.remove(distributionPref);
			} else {
				if (seqNo!=null) {
					for (Iterator j=distributionPref.getDistributionObjects().iterator();j.hasNext();) {
						DistributionObject dObj = (DistributionObject)j.next();
						if (seqNo.compareTo(dObj.getSequenceNumber())<0) {
							dObj.setSequenceNumber(Integer.valueOf(dObj.getSequenceNumber().intValue()-1));
							hibSession.merge(dObj);
						}
					}
				}

				if (updateClass)
					hibSession.merge(distributionPref);
			}
			i.remove();
    	}

    	if (deleted && updateClass)
    		hibSession.merge(this);
    }

	@Transient
    public Integer getMinRoomLimit() {
    	int expCap = (getExpectedCapacity()==null?0:getExpectedCapacity().intValue());
    	float roomRatio = (getRoomRatio()==null?0.0f:getRoomRatio().floatValue());
    	return Integer.valueOf(Math.round(expCap<=0?roomRatio:expCap*roomRatio));
    }

    public static List findAll(Long sessionId) {
    	return findAll(Class_DAO.getInstance().getSession(), sessionId);
    }
    
    public static List<Class_> findAll(org.hibernate.Session hibSession, Long sessionId) {
    	return hibSession.
    		createQuery("select distinct c from Class_ c where " +
    				"c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId", Class_.class).
    		setParameter("sessionId", sessionId.longValue()).
    		list();
    }

    public static List findAllForControllingSubjectArea(String subjectAreaAbbv, Long sessionId) {
    	return(findAllForControllingSubjectArea(subjectAreaAbbv, sessionId, (new Class_DAO()).getSession()));
    }

    public static List<Class_> findAllForControllingSubjectArea(String subjectAreaAbbv, Long sessionId, org.hibernate.Session hibSession) {
    	return hibSession.
    		createQuery("select distinct c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co where " +
    				"co.subjectArea.subjectAreaAbbreviation=:subjectAreaAbbv and c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId and co.isControl=true",
    				Class_.class).
    		setParameter("subjectAreaAbbv", subjectAreaAbbv).
    		setParameter("sessionId", sessionId.longValue()).
			setHibernateFlushMode(FlushMode.MANUAL).
    		list();
    }

	@Transient
    public String getDivSecNumber() {
    	if (getParentClass()!=null && getSchedulingSubpart().getItype().equals(getParentClass().getSchedulingSubpart().getItype())) {
    		return getParentClass().getDivSecNumber();
    	}
    	String suffix = getClassSuffix();
    	if (ApplicationProperty.ClassSuffixDivSecFormat.isTrue()) {
    		if (suffix!=null && suffix.length()==6) return suffix.substring(0,3)+"-"+suffix.substring(3,6);
    	}
    	return suffix;
    }

	@Transient
    public int getClassLimit() {
    	return getClassLimit(new CommitedClassAssignmentProxy());
    }
    
    public int getClassLimit(Assignment assignment) {
        int minLimit = getExpectedCapacity();
    	int maxLimit = getMaxExpectedCapacity();
    	int limit = maxLimit;
    	if (minLimit < maxLimit && assignment != null) {
    		int roomLimit = (int) Math.floor(assignment.getPlacement().getRoomSize() / (getRoomRatio() == null ? 1.0f : getRoomRatio()));
    		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
    	}
    	
    	return limit;
    }

    public int getClassLimit(ClassAssignmentProxy proxy) {
    	// min and max limits
        int minLimit = getExpectedCapacity();
    	int maxLimit = getMaxExpectedCapacity();

    	// is there a twiggle room?
    	if (minLimit == maxLimit) return maxLimit;

    	// get assignment
    	ClassAssignmentProxy.AssignmentInfo assignment = null;
    	try {
    		assignment = (proxy == null ? null : proxy.getAssignment(this));
    	} catch (Exception e) {
    		Debug.error(e);
    	}
    	
    	// if there is an assignment, check the room limit
    	if (assignment != null) {
    		int roomLimit = (int) Math.floor(assignment.getPlacement().getRoomSize() / (getRoomRatio() == null ? 1.0f : getRoomRatio()));
    		if (roomLimit < maxLimit) maxLimit = roomLimit;
    	}

    	// if there are children classes ...
    	for (SchedulingSubpart childSubpart: getSchedulingSubpart().getChildSubparts()) {
    		// take all children classes of the same subpart, sum their class limit
    		int childrenLimit = 0;
    		for (Class_ childClass: getChildClasses()) {
    			if (!childClass.getSchedulingSubpart().equals(childSubpart)) continue;
    			childrenLimit += childClass.getClassLimit(proxy);
    		}
    		// children class limit cannot be exceeded
    		if (childrenLimit < maxLimit) maxLimit = childrenLimit;
    	}
    	
    	return Math.max(minLimit, maxLimit);
    }

    public int getClassLimit(CourseOffering offering) {
        return getClassLimit(new CommitedClassAssignmentProxy());
    }
    
    private boolean hasChildClass(Reservation r) {
    	if (r.getClasses().contains(this)) return true;
    	for (Class_ child: getChildClasses())
    		if (child.hasChildClass(r)) return true;
    	return false;
    }
    
    public boolean hasClass(Reservation r) {
    	if (r.getClasses().isEmpty()) return false;
    	Class_ c = this;
    	while (c != null) {
    		if (r.getClasses().contains(c)) return true;
    		c = c.getParentClass();
    	}
    	for (Class_ child: getChildClasses())
    		if (child.hasChildClass(r)) return true;
    	return false;
    }

    /**
     * Delete all objects that have an identifying relationship with a class
     * Usually use this method when one needs to delete a class
     * (in which case set updateClass to false because the class will eventually be deleted)
     * @param hibSession
     * @param updateClass Updates class when all dependent objects are deleted
     */
	public void deleteAllDependentObjects(org.hibernate.Session hibSession, boolean updateClass) {

		// Call individual methods to delete specific collections
	    deleteAllDistributionPreferences(hibSession, updateClass);
	    deleteTeachingRequests(hibSession);
		deleteClassInstructors(hibSession);
		deleteAssignments(hibSession);
		Exam.deleteFromExams(hibSession, this);
		Event.deleteFromEvents(hibSession, this);

		// Add more collection deletes if needed

		if (updateClass)
			hibSession.merge(this);
	}

	/**
	 * Delete all class instructors
	 * @param hibSession
	 */
	public void deleteClassInstructors(org.hibernate.Session hibSession) {
		Set s = getClassInstructors();
		//deleteObjectsFromCollection(hibSession, s);
		if (s==null || s.size()==0) return;

		for (Iterator i=s.iterator(); i.hasNext(); ) {
			ClassInstructor ci = (ClassInstructor) (i.next());

			DepartmentalInstructor di = DepartmentalInstructorDAO.getInstance().get(ci.getInstructor().getUniqueId());
			di.getClasses().remove(ci);
			ci.setInstructor(null);
			ci.setClassInstructing(null);
			hibSession.merge(di);
			hibSession.remove(ci);
			i.remove();
		}
	}
	
	public void deleteTeachingRequests(org.hibernate.Session hibSession) {
		Set<TeachingClassRequest> s = getTeachingRequests();
		//deleteObjectsFromCollection(hibSession, s);
		if (s==null || s.size()==0) return;

		for (Iterator<TeachingClassRequest> i=s.iterator(); i.hasNext(); ) {
			TeachingClassRequest tcr = i.next();
			TeachingRequest tr = tcr.getTeachingRequest();
			tr.getClassRequests().remove(tcr);
			hibSession.remove(tcr);
			if (tr.getClassRequests().isEmpty() && !tr.isAssignCoordinator()) {
				InstructionalOffering offering = tr.getOffering();
				offering.getTeachingRequests().remove(tr);
				hibSession.remove(tr);
			} else {
				hibSession.merge(tr);
			}
			i.remove();
		}
	}

	/**
	 * Delete all class assignments
	 * @param hibSession
	 */
	public void deleteAssignments(org.hibernate.Session hibSession) {
		Set<Assignment> s = getAssignments();
        // Remove all related constraint infos to avoid hibernate cache issues 
        // when an orphaned constraint info is automatically deleted
		for (Assignment ass: s) {
            for (ConstraintInfo ci: ass.getConstraintInfo()) {
            	for (Assignment a: ci.getAssignments()) {
            		if (!a.equals(ass)) {
            			a.getConstraintInfo().remove(ci);
            		}
            	}
            	hibSession.remove(ci);
            }
		}
		deleteObjectsFromCollection(hibSession, s);
	}

	/**
	 * Common method to delete objects from acollection
	 * @param hibSession
	 * @param s Collection from which objects have to be deleted
	 */
	private void deleteObjectsFromCollection(org.hibernate.Session hibSession, Collection s) {
		if (s==null || s.size()==0) return;

		for (Iterator i=s.iterator(); i.hasNext(); ) {
			hibSession.remove(i.next());
			i.remove();
		}
	}
	
	@Transient
	public boolean isOddOrEvenWeeksOnly(){
		if (effectiveDatePattern() != null && effectiveDatePattern().isAlternate()){
			return(true);
		}
		return(false);
			
	}
	
	public Object clone(){
		Class_ newClass = new Class_();
		newClass.setDatePattern(getDatePattern());
		newClass.setEnabledForStudentScheduling(isEnabledForStudentScheduling());
		newClass.setDisplayInstructor(isDisplayInstructor());
		newClass.setExpectedCapacity(getExpectedCapacity());
		newClass.setManagingDept(getManagingDept());
		newClass.setMaxExpectedCapacity(getMaxExpectedCapacity());
		newClass.setNbrRooms(getNbrRooms());
		newClass.setNotes(getNotes());
		newClass.setRoomRatio(getRoomRatio());
		newClass.setSchedulePrintNote(getSchedulePrintNote());
		newClass.setSchedulingSubpart(getSchedulingSubpart());
		newClass.setCancelled(isCancelled());
		newClass.setLmsInfo(getLmsInfo());
		newClass.setFundingDept(getFundingDept());
		return(newClass);
	}
	
	public Object cloneWithPreferences(){
		Class_ newClass = (Class_) this.clone();
		if (getPreferences() != null){
			Preference origPref = null;
			Preference newPref = null;
			for (Iterator prefIt = getPreferences().iterator(); prefIt.hasNext();){
				origPref = (Preference) prefIt.next();	
				if (!(origPref instanceof DistributionPref)) {
					newPref = (Preference)origPref.clone();
					newPref.setOwner(newClass);
					newClass.addToPreferences(newPref);
				}
			}
		}
		if (getClassInstructors() != null && !getClassInstructors().isEmpty()){
			ClassInstructor ci = null;
			ClassInstructor newCi = null;
			for (Iterator ciIt = getClassInstructors().iterator(); ciIt.hasNext();){
				ci = (ClassInstructor) ciIt.next();
				newCi = new ClassInstructor();
				newCi.setClassInstructing(newClass);
				newCi.setInstructor(ci.getInstructor());
				newCi.setLead(ci.isLead());
				newCi.setPercentShare(ci.getPercentShare());
				newCi.setResponsibility(ci.getResponsibility());
				ci.getInstructor().addToClasses(newCi);
				newClass.addToClassInstructors(newCi);
			}
		}
		return(newClass);
	}
	
    public static Class_ findByExternalId(Long sessionId, String externalId) {
        return new Class_DAO().
            getSession().
            createQuery("select c from Class_ c where c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId and c.externalUniqueId=:externalId", Class_.class).
            setParameter("sessionId", sessionId.longValue()).
            setParameter("externalId", externalId).
            setCacheable(true).
            uniqueResult();
    }
    
    public static Class_ findByIdRolledForwardFrom(Long sessionId, Long uniqueIdRolledForwardFrom) {
        return new Class_DAO().
            getSession().
            createQuery("select c from Class_ c where c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId and c.uniqueIdRolledForwardFrom=:uniqueIdRolledForwardFrom", Class_.class).
            setParameter("sessionId", sessionId.longValue()).
            setParameter("uniqueIdRolledForwardFrom", uniqueIdRolledForwardFrom.longValue()).
            setCacheable(true).
            uniqueResult();
    }
    
    private ClassEvent iEvent = null;
	@Transient
    public ClassEvent getEvent() {
        if (iEvent==null) 
            iEvent = new Class_DAO().getSession().createQuery(
                "select e from ClassEvent e left join fetch e.meetings m where e.clazz.uniqueId=:classId", ClassEvent.class).
                setParameter("classId", getUniqueId()).
                setCacheable(true).uniqueResult();
        return iEvent;
    }
    public void setEvent(ClassEvent event) {
        iEvent = event;
    }
	@Transient
    public ClassEvent getCachedEvent() {
    	return iEvent;
    }
    
    public void cancelEvent(UserContext user, org.hibernate.Session hibSession, boolean cancelled) {
        ClassEvent event = getEvent();
        if (event != null) {
    		Calendar cal = Calendar.getInstance(Locale.US);
    		cal.set(Calendar.HOUR_OF_DAY, 0);
    		cal.set(Calendar.MINUTE, 0);
    		cal.set(Calendar.SECOND, 0);
    		cal.set(Calendar.MILLISECOND, 0);
    		Date today = cal.getTime();

        	for (Iterator<Meeting> i = event.getMeetings().iterator(); i.hasNext(); ) {
        		Meeting meeting = i.next();
        		if (meeting.getMeetingDate().before(today) && !ApplicationProperty.ClassAssignmentChangePastMeetings.isTrue())
        			continue;
        		meeting.setStatus(cancelled ? Meeting.Status.CANCELLED : Meeting.Status.APPROVED);
        		hibSession.merge(meeting);
        	}
        	
			if (event.getNotes() == null)
				event.setNotes(new HashSet<EventNote>());
			EventNote note = new EventNote();
			note.setEvent(event);
			note.setNoteType(cancelled ? EventNote.sEventNoteTypeCancel : EventNote.sEventNoteTypeApproval);
			note.setTimeStamp(new Date());
			note.setUser(user.getTrueName());
			note.setUserId(user.getTrueExternalUserId());
			note.setTextNote(cancelled ? MSG.classNoteCancelled(getClassLabel(hibSession)) : MSG.classNoteReopened(getClassLabel(hibSession)));
			if (getCommittedAssignment() == null)
				note.setMeetings(MSG.classMeetingsNotApplicable());
			else
				note.setMeetings(getCommittedAssignment().getPlacement().getLongName(CONSTANTS.useAmPm()));
			event.getNotes().add(note);
    		hibSession.merge(event);
        }
    }
    
    public String unassignCommited(UserContext user, org.hibernate.Session hibSession) {
        Transaction tx = null;
        try {
            if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                tx = hibSession.beginTransaction();

            Assignment oldAssignment = getCommittedAssignment();
            
            if (oldAssignment==null)
            	throw new RuntimeException("Class "+getClassLabel()+" does not have an assignment.");
            
            ClassEvent event = getEvent();
            if (event != null) {
            	if (ApplicationProperty.ClassAssignmentChangePastMeetings.isTrue()) {
            		hibSession.remove(event);
            	} else {
            		Calendar cal = Calendar.getInstance(Locale.US);
            		cal.set(Calendar.HOUR_OF_DAY, 0);
            		cal.set(Calendar.MINUTE, 0);
            		cal.set(Calendar.SECOND, 0);
            		cal.set(Calendar.MILLISECOND, 0);
            		Date today = cal.getTime();

                	for (Iterator<Meeting> i = event.getMeetings().iterator(); i.hasNext(); )
                		if (!i.next().getMeetingDate().before(today)) i.remove();
                	
                	if (event.getMeetings().isEmpty()) {
                		hibSession.remove(event);
                	} else {
            			if (event.getNotes() == null)
            				event.setNotes(new HashSet<EventNote>());
        				EventNote note = new EventNote();
        				note.setEvent(event);
        				note.setNoteType(EventNote.sEventNoteTypeDeletion);
        				note.setTimeStamp(new Date());
        				note.setUser(user.getTrueName());
        				note.setUserId(user.getTrueExternalUserId());
        				note.setTextNote(MSG.classNoteUnassigned(oldAssignment.getPlacement().getName()));
        				note.setMeetings(MSG.classMeetingsNotApplicable());
        				event.getNotes().add(note);
                		hibSession.persist(event);
                	}
            	}
            }

            String old = oldAssignment.getPlacement().getName();
            
            oldAssignment.getSolution().getAssignments().remove(oldAssignment);
            
            // Remove all related constraint infos to avoid hibernate cache issues 
            // when an orphaned constraint info is automatically deleted
            for (ConstraintInfo ci: oldAssignment.getConstraintInfo()) {
            	for (Assignment a: ci.getAssignments()) {
            		if (!a.equals(oldAssignment)) {
            			a.getConstraintInfo().remove(ci);
            		}
            	}
            	hibSession.remove(ci);
            }
            
        	hibSession.remove(oldAssignment);
        	
        	setCommittedAssignment(null);
        	hibSession.merge(this);
        	
            ChangeLog.addChange(hibSession,
                    TimetableManager.findByExternalId(user.getExternalUserId()),
                    getSession(),
                    this,
                    getClassLabel()+" ("+
                    old+
                    " &rarr; N/A)",
                    ChangeLog.Source.CLASS_INFO,
                    ChangeLog.Operation.UNASSIGN,
                    getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(),
                    getManagingDept());

            if (tx!=null) tx.commit();
            
            new _RootDAO().getSession().refresh(this);
            String className = ApplicationProperty.ExternalActionClassEdit.value();
        	if (className != null && className.trim().length() > 0){
            	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
           		editAction.performExternalClassEditAction(this, hibSession);
        	}

            return null;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
            return "Unassignment of "+getClassLabel()+" failed, reason: "+e.getMessage();
        }   
    }

    public String assignCommited(ClassAssignmentInfo assignment, UserContext user, org.hibernate.Session hibSession) {
        Transaction tx = null;
        try {
            if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                tx = hibSession.beginTransaction();

            String old = "N/A";
            
            Assignment oldAssignment = getCommittedAssignment();
            if (oldAssignment!=null) {
                old = oldAssignment.getPlacement().getName();
                
                oldAssignment.getSolution().getAssignments().remove(oldAssignment);
                
                // Remove all related constraint infos to avoid hibernate cache issues 
                // when an orphaned constraint info is automatically deleted
                for (ConstraintInfo ci: oldAssignment.getConstraintInfo()) {
                	for (Assignment a: ci.getAssignments()) {
                		if (!a.equals(oldAssignment)) {
                			a.getConstraintInfo().remove(ci);
                		}
                	}
                	hibSession.remove(ci);
                }
                
            	hibSession.remove(oldAssignment);
            }
            
            SolverGroup group = getManagingDept().getSolverGroup();
            if (group==null) throw new RuntimeException("Department "+getManagingDept().getLabel()+" has no solver group.");
            Solution solution = group.getCommittedSolution();
            if (solution==null) throw new RuntimeException("Solver group "+group.getName()+" has no commited solution.");
            
            DatePattern dp = DatePatternDAO.getInstance().get(assignment.getDate().getId(), hibSession);
            
            Assignment a = new Assignment();
            a.setSolution(solution);
            a.setSlotsPerMtg(assignment.getTime().getNrSlotsPerMeeting());
            a.setBreakTime(assignment.getTime().getBreakTime());
            a.setClazz(this);
            a.setClassName(getClassLabel(ApplicationProperty.SolverShowClassSufix.isTrue(), ApplicationProperty.SolverShowConfiguratioName.isTrue()));
            a.setDays(assignment.getTime().getDayCode());
            a.setRooms(new HashSet());
            a.setInstructors(new HashSet());
            a.setStartSlot(assignment.getTime().getStartSlot());
            a.setTimePattern(assignment.getTime().getTimePattern(hibSession));
            a.setDatePattern(dp != null ? dp : effectiveDatePattern());
            a.setAssignmentInfo(new HashSet());
            
            for (ClassRoomInfo room: assignment.getRooms())
            	a.getRooms().add(room.getLocation(hibSession));
            for (ClassInstructorInfo inst: assignment.getInstructors())
            	if (inst.isLead()) a.getInstructors().add(inst.getInstructor(hibSession).getInstructor());
            
            hibSession.persist(a);

            //TODO: More information should be gathered about the assignment.
            AssignmentPreferenceInfo pref = new AssignmentPreferenceInfo();
            pref.setTimePreference(assignment.getTime().getPreference());
            for (ClassRoomInfo room:assignment.getRooms())
            	pref.setRoomPreference(room.getLocationId(), room.getPreference());
            AssignmentInfo ai = new AssignmentInfo();
			ai.setAssignment(a);
			ai.setDefinition(SolverInfoDef.findByName(hibSession,"AssignmentInfo"));
			ai.setOpt(null);
			ai.setInfo(pref);
			hibSession.persist(ai);
            
			a.getAssignmentInfo().add(ai);
			a.cleastAssignmentInfoCache();
            
            ClassEvent event = getEvent();
            EventDateMapping.Class2EventDateMap class2eventDates = EventDateMapping.getMapping(getSession().getUniqueId());
            event = a.generateCommittedEvent(event, true, class2eventDates);
            if (event != null && !event.getMeetings().isEmpty()) {
    			if (event.getNotes() == null)
    				event.setNotes(new HashSet<EventNote>());
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(event.getUniqueId() == null ? EventNote.sEventNoteTypeCreateEvent : EventNote.sEventNoteTypeEditEvent);
				note.setTimeStamp(new Date());
				note.setUser(user.getTrueName());
				note.setUserId(user.getTrueExternalUserId());
				if (oldAssignment == null)
					note.setTextNote(MSG.classNoteAssigned(a.getPlacement().getName()));
				else
					note.setTextNote(MSG.classNoteReassigned(oldAssignment.getPlacement().getName(), a.getPlacement().getName()));
				note.setMeetings(assignment.getTime().getLongName() + (assignment.getNrRooms() > 0 ? " " + assignment.getRoomNames(", ") : ""));
				event.getNotes().add(note);
				if (event.getUniqueId() == null)
					hibSession.persist(event);
				else
					hibSession.merge(event);
            }
		    if (event != null && event.getMeetings().isEmpty() && event.getUniqueId() != null)
		    	hibSession.remove(event);

            setCommittedAssignment(a);
            hibSession.merge(this);

            ChangeLog.addChange(hibSession,
                    TimetableManager.findByExternalId(user.getExternalUserId()),
                    getSession(),
                    this,
                    getClassLabel()+" ("+
                    old+
                    " &rarr; "+assignment.getTime().getName()+" "+assignment.getRoomNames(", ")+")",
                    ChangeLog.Source.CLASS_INFO,
                    ChangeLog.Operation.ASSIGN,
                    getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(),
                    getManagingDept());
            
            if (tx!=null) tx.commit();
            
            new _RootDAO().getSession().refresh(this);
            String className = ApplicationProperty.ExternalActionClassEdit.value();
        	if (className != null && className.trim().length() > 0){
            	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
           		editAction.performExternalClassEditAction(this, hibSession);
        	}
            
            return null;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
            return "Assignment of "+getClassLabel()+" failed, reason: "+e.getMessage();
        }   
    }
    
	@Transient
    public Collection<Long> getEnrolledStudentIds() {
        return Class_DAO.getInstance().getSession().createQuery(
                "select e.student.uniqueId from StudentClassEnrollment e where "+
                "e.clazz.uniqueId=:classId", Long.class)
                .setParameter("classId", getUniqueId())
                .setCacheable(true).list();

    }
    
    public String buildAssignedTimeHtml(ClassAssignmentProxy proxy){
    	ClassAssignmentProxy.AssignmentInfo a = null;
		StringBuffer sb = new StringBuffer();
		try {
			a = proxy.getAssignment(this);
		} catch (Exception e) {
			Debug.error(e);
		}
		if (a!=null) {
				Integer firstDay = ApplicationProperty.TimePatternFirstDayOfWeek.intValue();
    			for (int i = 0; i < CONSTANTS.shortDays().length; i++) {
    				int idx = (firstDay == null ? i : (i + firstDay) % 7);
    				if ((Constants.DAY_CODES[idx] & a.getTimeLocation().getDayCode()) != 0) sb.append(CONSTANTS.shortDays()[idx]);
    			}
				sb.append(" ");
				sb.append(a.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm()));
				sb.append("-");
				sb.append(a.getTimeLocation().getEndTimeHeader(CONSTANTS.useAmPm()));
		} else {
			if (getEffectiveTimePreferences().isEmpty()){
				DurationModel dm = getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
				Integer ah = dm.getArrangedHours(getSchedulingSubpart().getMinutesPerWk(), effectiveDatePattern());
	            if (ah == null) {
	                sb.append(MSG.arrHrs());
	            } else {
	                sb.append(MSG.arrHrsN(ah));
	            }	
			}
		}
		if (sb.length() == 0){
			sb.append(" ");
		}
	    return(sb.toString());
	}
    
    public String buildAssignedDateHtml(ClassAssignmentProxy proxy){
    	ClassAssignmentProxy.AssignmentInfo a = null;
		try {
			a = proxy.getAssignment(this);
		} catch (Exception e) {
			Debug.error(e);
		}
		if (a != null && a.getTimeLocation() != null && a.getTimeLocation().getDatePatternName() != null)
			return a.getTimeLocation().getDatePatternName();
		return " ";
	}

	public String buildAssignedRoomHtml(ClassAssignmentProxy proxy){
		ClassAssignmentProxy.AssignmentInfo a = null;
		StringBuffer sb = new StringBuffer();
		try {
			a = proxy.getAssignment(this);
		} catch (Exception e) {
			Debug.error(e);
		}
		if (a!=null) {
            boolean first = true;
			for(Location room : a.getRooms()) {
        		if (first) { first = false; } else { sb.append(", "); }
				sb.append(room.getLabel());
			}	
		} else {
			if (getEffectiveTimePreferences().isEmpty()){
	            boolean first = true;
	            for (RoomPref rp : (Set<RoomPref>)getEffectiveRoomPreferences()) {
	            	if (rp.getPrefLevel().getPrefId().toString().equals(PreferenceLevel.PREF_LEVEL_REQUIRED)){
	            		if (first) { first = false; } else { sb.append(", "); }
	            		sb.append(rp.getRoom().getLabel());
	            	}
	            }
			}
		}
		if (sb.length() == 0){
			sb.append(" ");
		}
	    return(sb.toString());
	}

	public String buildInstructorHtml(String nameFormat){
		StringBuffer sb = new StringBuffer();
		if (getClassInstructors() != null && !getClassInstructors().isEmpty()){
			boolean first = true;
			for(ClassInstructor ci : (Set<ClassInstructor>) getClassInstructors()){
				if (first){
					first = false;
				} else {
					sb.append("<br>");
				}
				sb.append(ci.getInstructor().getName(nameFormat));
			}
		} else {
			sb.append(" ");
		}
	    return(sb.toString());
	}

	@Transient
	public static ExternalClassNameHelperInterface getExternalClassNameHelper() {
		return Customization.ClassNamingHelper.getProvider();
	}
    
	public String getClassLabel(CourseOffering courseOffering) {
		return(getExternalClassNameHelper().getClassLabel(this, courseOffering));
	}

	public String getClassSuffix(CourseOffering courseOffering) {
		return(getExternalClassNameHelper().getClassSuffix(this, courseOffering));
	}
	
	public String getClassLabelWithTitle(CourseOffering courseOffering) {
		return(getExternalClassNameHelper().getClassLabelWithTitle(this,courseOffering));
	}

	public String getExternalId(CourseOffering courseOffering) {
		return(getExternalClassNameHelper().getExternalId(this, courseOffering));
	}
	
	public Float getCredit(CourseOffering courseOffering) {
		return getExternalClassNameHelper().getClassCredit(this, courseOffering);
	}
	
	@Transient
	public SectioningInfo getSectioningInfo() {
		return  SectioningInfoDAO.getInstance().getSession().createQuery(
				"select i from SectioningInfo i where i.clazz.uniqueId = :classId", SectioningInfo.class)
				.setParameter("classId", getUniqueId()).setCacheable(true).uniqueResult();
	}

//	/* (non-Javadoc)
//	 * @see org.unitime.timetable.model.base.BaseClass_#getClassSuffix()
//	 */
//	@Override
//	public String getClassSuffix() {
//		return(getClassSuffix(getSchedulingSubpart().getControllingCourseOffering()));
//	}


	@Override
	@Transient
	public Department getDepartment() { return getManagingDept(); }
	
	public List<DistributionPref> getSharedPreferences(Long classId, Long subpartId, String[] preferences,  String[] types) {
		if (classId == null) return null;
		Query<DistributionPref> q1 = Class_DAO.getInstance().getSession().createQuery(
				"select o1.distributionPref from DistributionObject o1, DistributionObject o2" + 
				(subpartId == null ? ", Class_ c2" : "") + " where " +
				"o1.distributionPref = o2.distributionPref and o1.prefGroup.uniqueId in (:c1, :s1)" +
				(subpartId == null ? " and o2.prefGroup.uniqueId in (c2.uniqueId, c2.schedulingSubpart.uniqueId) and c2.uniqueId = :c2" : " and o2.prefGroup.uniqueId in (:c2, :s2)") +
				(preferences == null || preferences.length == 0 ? "" : " and o1.distributionPref.prefLevel.prefProlog " + (preferences.length == 1 ? "=" : "in" ) + " :p") +
				(types == null || types.length == 0 ? "" : " and o1.distributionPref.distributionType.reference " + (types.length == 1 ? "=" : "in") + " :t"),
				DistributionPref.class);
		Query<DistributionPref> q2 = Class_DAO.getInstance().getSession().createQuery(
				"select dp from DistributionPref dp, ClassInstructor c1 inner join c1.instructor.preferences p, ClassInstructor c2 where " +
				"c1.classInstructing.uniqueId = :c1 and c2.classInstructing.uniqueId = :c2 and c1.instructor = c2.instructor and p = dp" +
				(preferences == null || preferences.length == 0 ? "" : " and dp.prefLevel.prefProlog " + (preferences.length == 1 ? "=" : "in" ) + " :p") +
				(types == null || types.length == 0 ? "" : " and dp.distributionType.reference " + (types.length == 1 ? "=" : "in") + " :t"),
				DistributionPref.class);
		q1.setParameter("c1", getUniqueId()).setParameter("s1", getSchedulingSubpart().getUniqueId()).setParameter("c2", classId);
		q2.setParameter("c1", getUniqueId()).setParameter("c2", classId);
		if (subpartId != null)
			q1.setParameter("s2", subpartId);
		if (preferences != null) {
			if (preferences.length == 1) {
				q1.setParameter("p", preferences[0]);
				q2.setParameter("p", preferences[0]);
			} else if (preferences.length > 1) {
				q1.setParameterList("p", preferences, String.class);
				q2.setParameterList("p", preferences, String.class);
			}
		}
		if (types != null) {
			if (types.length == 1) {
				q1.setParameter("t", types[0]);
				q2.setParameter("t", types[0]);
			} else if (types.length > 1) {
				q1.setParameterList("t", types, String.class);
				q2.setParameterList("t", types, String.class);
			}
		}
		
		List<DistributionPref> ret = new ArrayList<DistributionPref>(q1.setCacheable(true).list());
		ret.addAll(q2.setCacheable(true).list());
		
		return ret;
	}

	public boolean canShareRoom(Class_ other) {
		if (other == null) return false;
		return !getSharedPreferences(other.getUniqueId(), other.getSchedulingSubpart().getUniqueId(),
				new String[] { PreferenceLevel.sRequired },
				new String[] { "CAN_SHARE_ROOM", "MEET_WITH", "MEET_WITH_WEEKS"}
				).isEmpty();
	}
	
	public boolean canShareInstructor(Class_ other) {
		if (other == null) return false;
		return !getSharedPreferences(other.getUniqueId(), other.getSchedulingSubpart().getUniqueId(),
				new String[] { PreferenceLevel.sRequired },
				new String[] { "MEET_WITH", "MEET_WITH_WEEKS"}
				).isEmpty();
	}
	
	public boolean canShareRoom(Long other) {
		if (other == null) return false;
		return !getSharedPreferences(other, null,
				new String[] { PreferenceLevel.sRequired },
				new String[] { "CAN_SHARE_ROOM", "MEET_WITH", "MEET_WITH_WEEKS"}
				).isEmpty();
	}
	
	public boolean canShareInstructor(Long other) {
		if (other == null) return false;
		return !getSharedPreferences(other, null,
				new String[] { PreferenceLevel.sRequired },
				new String[] { "MEET_WITH", "MEET_WITH_WEEKS"}
				).isEmpty();
	}
	
	public boolean isToIgnoreStudentConflictsWith(Class_ other) {
		if (other == null) return false;
		return !getSharedPreferences(other.getUniqueId(), other.getSchedulingSubpart().getUniqueId(),
				new String[] { PreferenceLevel.sRequired },
				new String[] { IgnoreStudentConflictsConstraint.REFERENCE}
				).isEmpty();
	}
	
	public boolean isParentOf(Class_ clazz) {
		Class_ parent = clazz.getParentClass();
		return parent != null && (this.equals(parent) || isParentOf(parent)); 
	}
	
	@Transient
	public int getSectioningLimit() {
		if (getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment()) {
        	return -1;
        } else {
        	int limit = getMaxExpectedCapacity();
        	Assignment assignment = getCommittedAssignment();
        	if (getExpectedCapacity() < getMaxExpectedCapacity() && assignment != null && !assignment.getRooms().isEmpty()) {
        		int roomSize = Integer.MAX_VALUE;
        		for (Location room: assignment.getRooms())
        			roomSize = Math.min(roomSize, room.getCapacity() == null ? 0 : room.getCapacity());
        		int roomLimit = (int) Math.floor(roomSize / (getRoomRatio() == null ? 1.0f : getRoomRatio()));
        		limit = Math.min(Math.max(getExpectedCapacity(), roomLimit), getMaxExpectedCapacity());
        	}
            if (limit >= 9999) limit = -1;
            return limit;
        }
	}
	
	@Transient
	private List<Reservation> getSectionReservations() {
		List<Reservation> reservations = new ArrayList<Reservation>();
		for (Reservation reservation: getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getReservations()) {
			if (reservation.getClasses().isEmpty()) continue;
			for (Class_ clazz: reservation.getClasses()) {
				if (this.equals(clazz) || this.isParentOf(clazz)) {
					reservations.add(reservation);
					break;
				}
			}
		}
		return reservations;
	}
	
	@Transient
	public int getUnreservedSectionSpace() {
		if (!isEnabledForStudentScheduling())
			return 0;
		
        // section is unlimited -> there is unreserved space unless there is an unlimited reservation too 
        // (in which case there is no unreserved space)
        if (getSectioningLimit() < 0) {
            // exclude reservations that are not directly set on this section
            for (Reservation r: getSectionReservations()) {
                // ignore expired reservations
                if (r.isExpired()) continue;
                // there is an unlimited reservation -> no unreserved space
                if (r.getReservationLimit() < 0) return 0;
            }
            return Integer.MAX_VALUE;
        }
        
        int available = getSectioningLimit() - getEnrollment();
        // exclude reservations that are not directly set on this section
        for (Reservation r: getSectionReservations()) {
            // ignore expired reservations
            if (r.isExpired()) continue;
            // unlimited reservation -> all the space is reserved
            if (r.getReservationLimit() < 0.0) return 0;
            // compute space that can be potentially taken by this reservation
            int reserved = r.getReservedAvailableSpace();
            // deduct the space from available space
            available -= Math.max(0, reserved);
        }
        
        return available;
    }
	
	@Transient
	public boolean isInstructorAssignmentNeeded() {
		for (TeachingClassRequest tcr: getTeachingRequests())
			if (tcr.isAssignInstructor()) return true;
		return false;
	}
	
	public int effectiveNbrInstructors() {
		int ret = 0;
		for (TeachingClassRequest tcr: getTeachingRequests())
			if (tcr.isAssignInstructor()) ret += tcr.getTeachingRequest().getNbrInstructors();
		return ret;
	}
	
	public float effectiveTeachingLoad() {
		int instructors = 0;
		float totalLoad = 0f;
		for (TeachingClassRequest tcr: getTeachingRequests())
			if (tcr.isAssignInstructor()) {
				instructors += tcr.getTeachingRequest().getNbrInstructors();
				totalLoad += tcr.getTeachingRequest().getNbrInstructors() * tcr.getTeachingRequest().getTeachingLoad();
			}
		return totalLoad / instructors;
	}
	
	public String getClassPrefLabel(CourseOffering co) {
		String label = getClassSuffix(co);
		if (label == null) {
			label = getSchedulingSubpart().getItypeDesc().trim() + " " + getSectionNumberString();
		} else if (label.length() <= 4) {
			label = getSchedulingSubpart().getItypeDesc().trim() + " " + label;
		}
		return label;
	}
	
	@Transient
	public LearningManagementSystemInfo getLms() {
		if (this.getLmsInfo() == null) {
			return(LearningManagementSystemInfo.getDefaultIfExists(getSessionId()));	
		} else {
			return(this.getLmsInfo());
		}
	}
	public void setLms(LearningManagementSystemInfo lms) {
		if (lms != null && !lms.isDefaultLms()) {
			setLmsInfo(lms);
		} else {
			setLmsInfo(null);
		}
	}
	
	@Transient
	public Department getEffectiveFundingDept() {
		if (getFundingDept() == null) {
			return getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getEffectiveFundingDept();
		} else {
			return getFundingDept();
		}
	}
	
	public boolean hasRoomIndexedPrefs() {
		if (getNbrRooms() <= 1) return false;
		for (RoomPref p: effectivePreferences(RoomPref.class))
			if (p.getRoomIndex() != null && p.getRoomIndex() < getNbrRooms()) return true;
		for (BuildingPref p: effectivePreferences(BuildingPref.class))
			if (p.getRoomIndex() != null && p.getRoomIndex() < getNbrRooms()) return true;
		for (RoomFeaturePref p: effectivePreferences(RoomFeaturePref.class))
			if (p.getRoomIndex() != null && p.getRoomIndex() < getNbrRooms()) return true;
		for (RoomGroupPref p: effectivePreferences(RoomGroupPref.class))
			if (p.getRoomIndex() != null && p.getRoomIndex() < getNbrRooms()) return true;
		return false;
	}
	
	public boolean hasLeadInstructor() {
		for (ClassInstructor ci: getClassInstructors())
			if (ci.isLead()) return true;
		return false;
	}
	
	public boolean hasLeadInstructorWithUnavailabilities() {
		for (ClassInstructor ci: getClassInstructors())
			if (ci.isLead() && ci.getInstructor().hasUnavailabilities()) return true;
		return false;
	}

}
