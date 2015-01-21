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


import org.hibernate.HibernateException;
import org.unitime.timetable.model.base.BaseDistributionType;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;




/**
 * @author Tomas Muller
 */
public class DistributionType extends BaseDistributionType implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public DistributionType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public DistributionType (Long uniqueId) {
		super(uniqueId);
	}
	
/*[CONSTRUCTOR MARKER END]*/
	
	public static Set findAll()  throws HibernateException {
		return findAll(false,false);
	}

	public static Set<DistributionType> findAll(boolean instructorPrefOnly, boolean examPref) throws HibernateException {
    	return new TreeSet<DistributionType>((new DistributionTypeDAO()).
			getSession().
			createQuery("select t from DistributionType t where t.examPref="+examPref+(instructorPrefOnly?" and t.instructorPref=true":"")).
			setCacheable(true).
			list());
	}
	
	public static Set<DistributionType> findApplicable(SessionContext context, boolean instructorPrefOnly, boolean examPref) throws Exception {
		if (context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent))
			return findAll(instructorPrefOnly, examPref);
    	TreeSet<DistributionType> ret = new TreeSet<DistributionType>();
    	Set<Department> userDepartments = Department.getUserDepartments(context.getUser()); 
    	types: for (DistributionType dt: findAll(instructorPrefOnly,examPref)) {
    		Set<Department> depts = dt.getDepartments(context.getUser().getCurrentAcademicSessionId());
    		if (depts.isEmpty()) {
    			ret.add(dt);
    		} else {
    			for (Department d: depts)
    				if (userDepartments.contains(d)) {
    					ret.add(dt); continue types;
    				}
    		}
    	}
    	return ret;
    }

	public static Set findApplicable(Department dept, boolean instructorPrefOnly, boolean examPref) throws Exception {
		if (dept==null) return findAll(instructorPrefOnly, examPref);
		TreeSet ret = new TreeSet();
    	for (Iterator i=findAll(instructorPrefOnly, examPref).iterator();i.hasNext();) {
    		DistributionType dt = (DistributionType)i.next();
    		Set depts = dt.getDepartments(dept.getSession().getUniqueId());
    		if (depts.isEmpty() || depts.contains(dept))
    			ret.add(dt);
    	}
    	return ret;
    }
	
	public boolean isApplicable(Department dept) {
		if (getDepartments().isEmpty()) return true;
		Set depts = getDepartments(dept.getSession().getUniqueId()); 
		return depts.isEmpty() || depts.contains(dept);
	}

    /** Request attribute name for available distribution types **/
    public static String DIST_TYPE_ATTR_NAME = "distributionTypeList";
	
    public boolean isAllowed(PreferenceLevel pref) {
    	return (getAllowedPref()==null || getAllowedPref().indexOf(PreferenceLevel.prolog2char(pref.getPrefProlog()))>=0); 
    }
    
    public Set<Department> getDepartments(Long sessionId) {
    	TreeSet<Department> ret = new TreeSet<Department>();
    	for (Department d: getDepartments()) {
    		if (sessionId == null || d.getSession().getUniqueId().equals(sessionId))
    			ret.add(d);
    	}
    	return ret;
    }
    
    public int compareTo(Object o) {
    	if (o==null || !(o instanceof DistributionType)) return -1;
    	DistributionType dt = (DistributionType)o;
    	int cmp = getLabel().compareTo(dt.getLabel());
    	if (cmp!=0) return cmp;
    	return getRequirementId().compareTo(dt.getRequirementId());
    }
    
    public String toString() {
        return getLabel();
    }
}
