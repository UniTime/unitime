/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Hibernate;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseDistributionPref;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.PreferenceGroupDAO;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class DistributionPref extends BaseDistributionPref {
	private static final long serialVersionUID = 1L;

	/** Request Attribute name for Dist Prefs **/
	public static final String DIST_PREF_REQUEST_ATTR = "distPrefs";
	
	public static final int sGroupingNone = 0;
	public static final int sGroupingProgressive = 1;
	public static final int sGroupingByTwo = 2;
	public static final int sGroupingByThree = 3;
	public static final int sGroupingByFour = 4;
	public static final int sGroupingByFive = 5;
	public static final int sGroupingPairWise = 6;
	
	//TODO put this stuff into the database (as a some kind of DistributionPreferenceGroupingType object)
	public static String[] sGroupings = new String[] { "All Classes", "Progressive", "Groups of Two", "Groups of Three", "Groups of Four", "Groups of Five", "Pairwise"};
	public static String[] sGroupingsSufix = new String[] {""," Progressive"," Groups of Two"," Groups of Three"," Groups of Four"," Groups of Five", " Pairwise"};
	public static String[] sGroupingsSufixShort = new String[] {""," Prg"," Go2"," Go3"," Go4"," Go5", " Pair"};
	public static String[] sGroupingsDescription = new String[] {
		//All Classes
		"The constraint will apply to all classes in the selected distribution set. "+
		"For example, a Back-to-Back constraint among three classes seeks to place all three classes "+
		"sequentially in time such that there are no intervening class times (transition time between "+
		"classes is taken into account, e.g., if the first class ends at 8:20, the second has to start at 8:30).",
		//Progressive
		"The distribution constraint is created between classes in one scheduling subpart and the "+
		"appropriate class(es) in one or more other subparts. This structure links child and parent "+
		"classes together if subparts have been grouped. Otherwise the first class in one subpart is "+
		"linked to the the first class in the second subpart, etc.",
		//Groups of Two
		"The distribution constraint is applied only on subsets containing two classes in the selected "+
		"distribution set.  A constraint is posted between the first two classes (in the order listed), "+
		"then between the second two classes, etc.",
		//Groups of Three
		"The distribution constraint is applied only on subsets containing three classes in the selected "+
		"distribution set.  A constraint is posted between the first three classes (in the order listed), "+
		"then between the second three classes, etc.",
		//Groups of Four
		"The distribution constraint is applied only on subsets containing four classes in the selected "+
		"distribution set.  A constraint is posted between the first four classes (in the order listed), "+
		"then between the second four classes, etc.",
		//Groups of Five
		"The distribution constraint is applied only on subsets containing five classes in the selected "+
		"distribution set.  A constraint is posted between the first five classes (in the order listed), "+
		"then between the second five classes, etc.",
		//Pairwise
		"The distribution constraint is created between every pair of classes in the selected distribution set. "+
		"Therefore, if n classes are in the set, n(n-1)/2 constraints will be posted among the classes. "+
		"This structure should not be used with \"required\" or \"prohibited\" preferences on sets containing "+
		"more than a few classes."
	};
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public DistributionPref () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public DistributionPref (java.lang.Long uniqueId) {
		super(uniqueId);
	}
	
/*[CONSTRUCTOR MARKER END]*/
	
	public String preferenceText() {
		return preferenceText(false, false, "<BR>", "<BR>", "");
	}

    public String preferenceText(boolean includeDistrObjects, boolean abbv, String objQuotationLeft, String objSeparator, String objQuotationRight) {
    	StringBuffer sb = new StringBuffer();
    	if (abbv) {
    		sb.append(getDistributionType().getAbbreviation().replaceAll("<","&lt;").replaceAll(">","&gt;"));
    		if (!new Integer(-1).equals(getGrouping()))
    		    sb.append(sGroupingsSufixShort[getGrouping()==null?0:getGrouping().intValue()]);
    	} else {
    		sb.append(getDistributionType().getLabel());
    		if (!new Integer(-1).equals(getGrouping()))
    		    sb.append(sGroupingsSufix[getGrouping()==null?0:getGrouping().intValue()]);
    	}
    	if (includeDistrObjects) {
    		if (getDistributionObjects()!=null && !getDistributionObjects().isEmpty()) {
    			sb.append(objQuotationLeft);
    			for (Iterator it=getOrderedSetOfDistributionObjects().iterator();it.hasNext();) {
    				DistributionObject distObj = (DistributionObject) it.next();;
    				sb.append(distObj.preferenceText());
    				if (it.hasNext())
    					sb.append(objSeparator);
    			}
    			sb.append(objQuotationRight);
    		} else if (getOwner() instanceof DepartmentalInstructor) {
    			sb.append(" (" + ((DepartmentalInstructor)getOwner()).getName(DepartmentalInstructor.sNameFormatShort) + ")");
    			if (!abbv) {
        			sb.append(objQuotationLeft);
        			for (Iterator it=((DepartmentalInstructor)getOwner()).getClasses().iterator();it.hasNext();) {
        				ClassInstructor ci = (ClassInstructor)it.next();
        				sb.append(ci.getClassInstructing().getClassLabel());
        				if (it.hasNext())
        					sb.append(objSeparator);
        			}
        			sb.append(objQuotationRight);
    			}
    		}
    	}
    	return(sb.toString());
    }
    
    public String preferenceHtml() {
    	StringBuffer sb = new StringBuffer();
    	String color = getPrefLevel().prefcolor();
    	if (PreferenceLevel.sNeutral.equals(getPrefLevel().getPrefProlog()))
    		color = "gray";
    	sb.append("<span style='color:"+color+";font-weight:bold;' onmouseover=\"showGwtHint(this, '" + 
    			getPrefLevel().getPrefName() + " " + preferenceText(true,false,"<ul><li>","<li>","</ul>")
    			+ "');\" onmouseout=\"hideGwtHint();\">" );
    	sb.append(preferenceText(false,true, "", "", ""));
    	sb.append("</span>");
    	return sb.toString();
    }
    
	/**
	 * @param schedulingSubpart_
	 * @return
	 */
	public boolean appliesTo(SchedulingSubpart schedulingSubpart) {
		if (this.getDistributionObjects()==null) return false;
		for (Iterator it=this.getDistributionObjects().iterator();it.hasNext();) {
			DistributionObject dObj = (DistributionObject) it.next();
			
			//SchedulingSubpart check
			//no checking whether dObj.getPrefGroup() is SchedulingSubpart not needed since all PreferenceGroups have unique ids
			if (dObj.getPrefGroup().getUniqueId().equals(schedulingSubpart.getUniqueId())) return true;
		}
		return false;
	}

	/**
	 * @param aClass
	 * @return
	 */
	public boolean appliesTo(Class_ aClass) {
		if (this.getDistributionObjects()==null) return false;
		Iterator it = null;
		try {
			it = getDistributionObjects().iterator();
		} catch (ObjectNotFoundException e) {
			Debug.error("Exception "+e.getMessage()+" seen for "+this);
    		new _RootDAO().getSession().refresh(this);
   			it = getDistributionObjects().iterator();
		}
		while (it.hasNext()) {
			DistributionObject dObj = (DistributionObject) it.next();
			
			//Class_ check
			//no checking whether dObj.getPrefGroup() is Class_ not needed since all PreferenceGroups have unique ids
			if (dObj.getPrefGroup().getUniqueId().equals(aClass.getUniqueId())) return true;
			
			//SchedulingSubpart check
			SchedulingSubpart ss = null;
			if (Hibernate.isInitialized(dObj.getPrefGroup())) {
				if (dObj.getPrefGroup() instanceof SchedulingSubpart) {
					ss = (SchedulingSubpart) dObj.getPrefGroup();
				}
			} else {
				//dObj.getPrefGroup() is a proxy -> try to load it
				PreferenceGroup pg = (new PreferenceGroupDAO()).get(dObj.getPrefGroup().getUniqueId());
				if (pg!=null && pg instanceof SchedulingSubpart)
					ss = (SchedulingSubpart)pg;
			}
			if (ss!=null && ss.getClasses()!=null && ss.getClasses().size()>0) {
				for (Iterator it2 = ss.getClasses().iterator();it2.hasNext();)
					if (((Class_)it2.next()).getUniqueId().equals(aClass.getUniqueId())) return true;
			}
		}
		return false;
	}
	
	// overide default
	public boolean appliesTo(PreferenceGroup group) {
		if (group instanceof Class_)
			return appliesTo((Class_)group);
		if (group instanceof SchedulingSubpart)
			return appliesTo((SchedulingSubpart)group);
		return false;
	}

    public int compareTo(Object o) {
   		DistributionPref p = (DistributionPref)o;
   		int cmp = getDistributionType().getReference().compareTo(p.getDistributionType().getReference()); 
   		if (cmp!=0) return cmp;
   		
   		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(p.getUniqueId() == null ? -1 : p.getUniqueId());
   }
    
    public Object clone() {
    	DistributionPref pref = new DistributionPref();
    	pref.setPrefLevel(getPrefLevel());
    	pref.setDistributionObjects(new HashSet<DistributionObject>(getDistributionObjects()));
    	pref.setDistributionType(getDistributionType());
    	return pref;
    }
    public boolean isSame(Preference other) {
    	return equals(other);
    }
    /** Ordered set of distribution objects */
    public Set<DistributionObject> getOrderedSetOfDistributionObjects() {
    	try {
    		return new TreeSet<DistributionObject>(getDistributionObjects());
    	} catch (ObjectNotFoundException ex) {
    		(new DistributionPrefDAO()).getSession().refresh(this);
    		return new TreeSet<DistributionObject>(getDistributionObjects());
    	}
    }
    
    public String getGroupingName() {
        if (new Integer(-1).equals(getGrouping())) return null;
    	return sGroupings[getGrouping()==null?0:getGrouping().intValue()];
    }
    public String getGroupingSufix() {
        if (new Integer(-1).equals(getGrouping())) return null;
    	return sGroupingsSufix[getGrouping()==null?0:getGrouping().intValue()];
    }
    
    public static String getGroupingDescription(int grouping) {
    	return sGroupingsDescription[grouping];
    }
    
    public static Collection getPreferences(Long sessionId, Long ownerId, boolean useControllingCourseOfferingManager, Long uniqueId) {
    	return getPreferences(sessionId, ownerId, useControllingCourseOfferingManager, uniqueId, null, null);
    }
    
    public static Collection getPreferences(Long sessionId, Long ownerId, boolean useControllingCourseOfferingManager, Long uniqueId, Long subjectAreaId, String courseNbr) {
    	if (sessionId==null) return null;
    	StringBuffer sb = new StringBuffer();
    	sb.append("select distinct dp ");
    	sb.append(" from ");
    	sb.append(" DistributionPref as dp ");
    	sb.append(" inner join dp.distributionObjects as do, ");
    	sb.append(" Class_ as c ");
    	sb.append(" inner join c.schedulingSubpart as ss inner join ss.instrOfferingConfig.instructionalOffering as io ");
    	if(subjectAreaId != null || ownerId != null){
    		sb.append(" inner join io.courseOfferings as co ");
    	}
    	sb.append("where ");
    	sb.append(" (c.uniqueId = do.prefGroup.uniqueId or ss.uniqueId = do.prefGroup.uniqueId) and ");
    	sb.append(" io.session.uniqueId = :sessionId ");
    	if (ownerId != null){
    		sb.append(" and (");
    		sb.append("((c.managingDept is not null and c.managingDept.uniqueId = :ownerId )");
    		sb.append(" or (c.managingDept is null ");
    		sb.append(" and co.isControl = true ");
    		sb.append("and co.subjectArea.department.uniqueId = :ownerId))");
    		if (useControllingCourseOfferingManager)	{
    			sb.append(" or (co.isControl = true");
    			sb.append(" and co.subjectArea.department.uniqueId = :ownerId)");
    		}
    		sb.append(")");
    	}
    	if (uniqueId != null){
    		sb.append(" and (c.uniqueId = :uniqueId or ss.uniqueId = :uniqueId or io.uniqueId = :uniqueId))");
    	}
    	if(subjectAreaId != null){
    	    sb.append(" and co.subjectArea.uniqueId=:subjectAreaId ");
    	    
    		if (courseNbr!=null && courseNbr.trim().length()>0) {
    		    sb.append(" and co.courseNbr ");
    		    if (courseNbr.indexOf('*')>=0) {
    	            sb.append(" like ");
    	            courseNbr = courseNbr.replace('*', '%').toUpperCase();
    		    }
    		    else {
    	            sb.append(" = ");
    		    }
                sb.append(":courseNbr");
    		}		
    	}
	
    	Query q = (new DistributionPrefDAO()).
			getSession().
			createQuery(sb.toString());
    	q.setLong("sessionId", sessionId.longValue());
    	if (ownerId!=null)
    		q.setLong("ownerId", ownerId.longValue());
    	if (uniqueId!=null)
    		q.setLong("uniqueId", uniqueId.longValue());
    	if (subjectAreaId!=null) {
    		q.setLong("subjectAreaId", subjectAreaId.longValue());
    		if (courseNbr!=null && courseNbr.trim().length()>0)
    		    q.setString("courseNbr", courseNbr.toUpperCase());
    	}
    	return q.list();
    }
    
    public static Collection getInstructorPreferences(Long sessionId, Long ownerId, Long subjectAreaId, String courseNbr) {
        if (sessionId==null) return null;
        StringBuffer sb = new StringBuffer();
        sb.append("select distinct dp ");
        sb.append(" from ");
        sb.append(" DistributionPref as dp, ");
        sb.append(" DepartmentalInstructor as di ");
        if (subjectAreaId!=null) {
            sb.append(" inner join di.classes as ci inner join ci.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co ");
        }
        sb.append("where ");
        sb.append(" dp.owner = di ");
        sb.append(" and di.department.session.uniqueId = :sessionId ");
        if (subjectAreaId!=null) {
            sb.append(" and ci.lead = true ");
            sb.append(" and co.isControl = true ");
            sb.append(" and co.subjectArea.uniqueId = :subjectAreaId ");
            if (courseNbr!=null && courseNbr.trim().length()>0) {
                sb.append(" and co.courseNbr ");
                if (courseNbr.indexOf('*')>=0) {
                    sb.append(" like ");
                    courseNbr = courseNbr.replace('*', '%').toUpperCase();
                } else {
                    sb.append(" = ");
                }
                sb.append(":courseNbr");
            }       
        }
        if (ownerId != null) {
            sb.append(" and di.department.uniqueId = :ownerId ");
        }

        Query q = (new DistributionPrefDAO()).
            getSession().
            createQuery(sb.toString());
        q.setLong("sessionId", sessionId.longValue());
        if (ownerId!=null)
            q.setLong("ownerId", ownerId.longValue());
        if (subjectAreaId!=null) {
            q.setLong("subjectAreaId", subjectAreaId.longValue());
            if (courseNbr!=null && courseNbr.trim().length()>0)
                q.setString("courseNbr", courseNbr.toUpperCase());
        }
        return q.list();
    }
    
    public boolean weakenHardPreferences() {
    	if (PreferenceLevel.sRequired.equals(getPrefLevel().getPrefProlog())) {
    		if (getDistributionType().getAllowedPref().indexOf(PreferenceLevel.sCharLevelStronglyPreferred)>=0)
    			setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
    		else
    			return false;
    	}
    	if (PreferenceLevel.sProhibited.equals(getPrefLevel().getPrefProlog())) {
    		if (getDistributionType().getAllowedPref().indexOf(PreferenceLevel.sCharLevelStronglyDiscouraged)>=0)
    			setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
    		else
    			return false;
    	}
    	return true;
    }
    
    public static DistributionPref findByIdRolledForwardFrom(Long uidRolledForwardFrom) {
        return (DistributionPref)new DistributionPrefDAO().
            getSession().
            createQuery(
                "select dp from DistributionPref dp where "+
                "dp.uniqueIdRolledForwardFrom=:uidRolledFrom").
            setLong("uidRolledFrom", uidRolledForwardFrom).
            setCacheable(true).
            uniqueResult(); 
    }
    
    public String toString(){
    	return(preferenceText(false, false, " ", ", ", ""));
    }
}
