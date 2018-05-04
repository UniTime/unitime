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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Hibernate;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.springframework.web.util.HtmlUtils;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.base.BaseDistributionPref;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.PreferenceGroupDAO;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class DistributionPref extends BaseDistributionPref {
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	private static final long serialVersionUID = 1L;

	/** Request Attribute name for Dist Prefs **/
	public static final String DIST_PREF_REQUEST_ATTR = "distPrefs";
	
	public static enum Structure {
		AllClasses,
		Progressive,
		GroupsOfTwo,
		GroupsOfThree,
		GroupsOfFour,
		GroupsOfFive,
		Pairwise,
		OneOfEach,
		;
		
		public String getDescription() {
			switch (this) {
			case AllClasses: return MSG.distributionStructureDescriptionAllClasses();
			case Progressive: return MSG.distributionStructureDescriptionProgressive();
			case GroupsOfTwo: return MSG.distributionStructureDescriptionGroupsOfTwo();
			case GroupsOfThree: return MSG.distributionStructureDescriptionGroupsOfThree();
			case GroupsOfFour: return MSG.distributionStructureDescriptionGroupsOfFour();
			case GroupsOfFive: return MSG.distributionStructureDescriptionGroupsOfFive();
			case Pairwise: return MSG.distributionStructureDescriptionPairwise();
			case OneOfEach: return MSG.distributionStructureDescriptionOneOfEach();
			default: return null;
			}
		}
		
		public String getName() {
			switch (this) {
			case AllClasses: return MSG.distributionStructureNameAllClasses();
			case Progressive: return MSG.distributionStructureNameProgressive();
			case GroupsOfTwo: return MSG.distributionStructureNameGroupsOfTwo();
			case GroupsOfThree: return MSG.distributionStructureNameGroupsOfThree();
			case GroupsOfFour: return MSG.distributionStructureNameGroupsOfFour();
			case GroupsOfFive: return MSG.distributionStructureNameGroupsOfFive();
			case Pairwise: return MSG.distributionStructureNamePairwise();
			case OneOfEach: return MSG.distributionStructureNameOneOfEach();
			default: return null;
			}
		}
		
		public String getLabel(String content) {
			switch (this) {
			case AllClasses: return MSG.distributionStructureLabelAllClasses(content);
			case Progressive: return MSG.distributionStructureLabelProgressive(content);
			case GroupsOfTwo: return MSG.distributionStructureLabelGroupsOfTwo(content);
			case GroupsOfThree: return MSG.distributionStructureLabelGroupsOfThree(content);
			case GroupsOfFour: return MSG.distributionStructureLabelGroupsOfFour(content);
			case GroupsOfFive: return MSG.distributionStructureLabelGroupsOfFive(content);
			case Pairwise: return MSG.distributionStructureLabelPairwise(content);
			case OneOfEach: return MSG.distributionStructureLabelOneOfEach(content);
			default: return content + null;
			}
		}

		public String getAbbreviation(String content) {
			switch (this) {
			case AllClasses: return MSG.distributionStructureAbbreviationAllClasses(content);
			case Progressive: return MSG.distributionStructureAbbreviationProgressive(content);
			case GroupsOfTwo: return MSG.distributionStructureAbbreviationGroupsOfTwo(content);
			case GroupsOfThree: return MSG.distributionStructureAbbreviationGroupsOfThree(content);
			case GroupsOfFour: return MSG.distributionStructureAbbreviationGroupsOfFour(content);
			case GroupsOfFive: return MSG.distributionStructureAbbreviationGroupsOfFive(content);
			case Pairwise: return MSG.distributionStructureAbbreviationPairwise(content);
			case OneOfEach: return MSG.distributionStructureAbbreviationOneOfEach(content);
			default: return content + null;
			}
		}
	}
	
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
    	StringBuffer sb = new StringBuffer((abbv ? getAbbreviation() : getLabel()).replaceAll("<","&lt;").replaceAll(">","&gt;"));
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
		return sb.toString();
    }
    
    public String preferenceHtml(String nameFormat, boolean highlightClassPrefs) {
    	StringBuffer sb = new StringBuffer("<span ");
    	String style = "font-weight:bold;";
		if (this.getPrefLevel().getPrefId().intValue() != 4) {
			style += "color:" + this.getPrefLevel().prefcolor() + ";";
		}
		if (this.getOwner() != null && this.getOwner() instanceof Class_ && highlightClassPrefs) {
			style += "background: #ffa;";
		}
		sb.append("style='" + style + "' ");
		String owner = "";
		if (getOwner() != null && getOwner() instanceof Class_) {
			owner = " (" + MSG.prefOwnerClass() + ")";
		} else if (getOwner() != null && getOwner() instanceof SchedulingSubpart) {
			owner = " (" + MSG.prefOwnerSchedulingSubpart() + ")";
		} else if (getOwner() != null && getOwner() instanceof DepartmentalInstructor) {
			owner = " (" +((DepartmentalInstructor)getOwner()).getName(nameFormat != null ? nameFormat : DepartmentalInstructor.sNameFormatShort) + ")";
		} else if (getOwner() != null && getOwner() instanceof Exam) {
			owner = " (" + MSG.prefOwnerExamination() + ")";
		} else if (getOwner() != null && getOwner() instanceof Department) {
			owner = " (" + ((Department)getOwner()).getLabel() + ")";
		} else if (getOwner() != null && getOwner() instanceof Session) {
			owner = " (" + MSG.prefOwnerSession() + ")";
		}
		String hint = HtmlUtils.htmlEscape(getPrefLevel().getPrefName() + " " + getLabel() + owner);
		if (getDistributionObjects()!=null && !getDistributionObjects().isEmpty()) {
			hint += "<ul><li>";
			for (Iterator<DistributionObject> it = getOrderedSetOfDistributionObjects().iterator(); it.hasNext();) {
				DistributionObject distObj = it.next();
				hint += HtmlUtils.htmlEscape(distObj.preferenceText());
				if (it.hasNext())
					hint += "<li>";
			}
			hint += "</ul>";
		} else if (getOwner() instanceof DepartmentalInstructor) {
			hint += "<ul><li>";
			for (Iterator<ClassInstructor> it = ((DepartmentalInstructor)getOwner()).getClasses().iterator(); it.hasNext();) {
				ClassInstructor ci = (ClassInstructor)it.next();
				hint += HtmlUtils.htmlEscape(ci.getClassInstructing().getClassLabel());
				if (it.hasNext())
					hint += "<li>";
			}
			hint += "</ul>";
		}
		String description = preferenceDescription();
		if (description != null && !description.isEmpty())
			hint += "<br>" + HtmlUtils.htmlEscape(description.replace("\'", "\\\'")).replace("\n", "<br>");
		sb.append("onmouseover=\"showGwtHint(this, '" + hint + "');\" onmouseout=\"hideGwtHint();\">");
		sb.append(getAbbreviation());
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
    
    public String getStructureName() {
    	return (getStructure() == null ? null : getStructure().getName());
    }
    
    public String getStructureDescription() {
    	return (getStructure() == null ? null : getStructure().getDescription());
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
        } else if (ownerId != null) {
            sb.append(" and di.department.uniqueId = :ownerId ");
        }

        Query q = (new DistributionPrefDAO()).
            getSession().
            createQuery(sb.toString());
        q.setLong("sessionId", sessionId.longValue());
        if (subjectAreaId!=null) {
            q.setLong("subjectAreaId", subjectAreaId.longValue());
            if (courseNbr!=null && courseNbr.trim().length()>0)
                q.setString("courseNbr", courseNbr.toUpperCase());
        } else if (ownerId!=null) {
            q.setLong("ownerId", ownerId.longValue());
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
    
    public static DistributionPref findByIdRolledForwardFrom(Long uidRolledForwardFrom, Long sessionId) {
        return (DistributionPref)new DistributionPrefDAO().
            getSession().
            createQuery(
                "select dp from DistributionPref dp, Department d where "+
                "dp.uniqueIdRolledForwardFrom=:uidRolledFrom and dp.owner=d and d.session.uniqueId=:sessionId").
            setLong("uidRolledFrom", uidRolledForwardFrom).
            setLong("sessionId", sessionId).
            setCacheable(true).
            uniqueResult(); 
    }
    
    public String toString(){
    	return(preferenceText(false, false, " ", ", ", ""));
    }
    
    public static <E> Enumeration<List<E>> permutations(final List<E> items, final List<Integer> counts) {
        return new Enumeration<List<E>>() {
            int[] p = null;
            int[] b = null;
            int m = 0;
            
            @Override
            public boolean hasMoreElements() {
            	if (p == null) return true;
            	for (int i = 0; i < m; i++)
            		if (p[i] < b[i]) return true;
            	return false;
            }
            
            @Override
            public List<E> nextElement() {
                if (p == null) {
                	m = counts.size();
                    p = new int[m];
                    b = new int[m];
                    int c = 0;
                    for (int i = 0; i < m; i++) {
                        p[i] = c;
                        c += counts.get(i);
                        b[i] = c - 1;
                    }
                } else {
                    for (int i = m - 1; i >= 0; i--) {
                        p[i] = p[i] + 1;
                        for (int j = i + 1; j < m; j++)
                            p[j] = b[j - 1] + 1;
                        if (i == 0 || p[i] <= b[i]) break;
                    }
                }
                List<E> ret = new ArrayList<E>();
                for (int i = 0; i < m; i++)
                    ret.add(items.get(p[i]));
                return ret;
            }
        };
    }
    
    public Structure getStructure() {
    	if (getGrouping() == null || getGrouping() < 0) return null;
    	return Structure.values()[getGrouping()];
    }
    
    public void setStructure(Structure structure) {
    	setGrouping(structure.ordinal());
    }
    
    public String getLabel() {
    	String label = getDistributionType().getLabel();
    	return getStructure() == null ? label : getStructure().getLabel(label);
    }
    
    public String getAbbreviation() {
    	String abbv = getDistributionType().getAbbreviation();
    	return getStructure() == null ? abbv : getStructure().getAbbreviation(abbv);
    }
    
    public Type getType() { return Type.DISTRIBUTION; }
}
