/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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

import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseStaff;
import org.unitime.timetable.model.dao.StaffDAO;
import org.unitime.timetable.util.Constants;




public class Staff extends BaseStaff implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Staff () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Staff (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	/**
	 * 
	 * @param deptCode
	 * @return
	 */
	public static List getStaffByDept(String deptCode, Long acadSessionId) throws Exception {	
		if (deptCode == null){
			return(null);
		}
		
		StaffDAO sdao = new StaffDAO();
		String sql = "select distinct s " +
					 "from Staff s " +
					 "where s.dept='" + deptCode + "'" +
					 "  and ( " +
						 "select di.externalUniqueId " +
						 "from DepartmentalInstructor di " +
						 "where di.department.deptCode='" + deptCode + "' " + 
						 "  and di.department.session.uniqueId=" + acadSessionId.toString() +
						 "  and di.externalUniqueId = s.externalUniqueId ) is null";
		Query q = sdao.getSession().createQuery(sql);
		q.setCacheable(true);
		return (q.list());
	}
	

	/**
	 * Search staff list for instructors with matching names
	 * @param fname First Name 
	 * @param lname Last Name
	 * @return
	 */
	public static List findMatchingName(String fname, String lname) {
		List list = null;
	    
		if ( (fname==null || fname.trim().length()==0) 
		        && (lname==null || lname.trim().length()==0) )
		    return list;
		
		Conjunction and = Restrictions.conjunction();
		if (fname!=null && fname.trim().length()>0)
		    and.add(Restrictions.ilike("firstName", fname, MatchMode.START));
		if (lname!=null && lname.trim().length()>0)
		    and.add(Restrictions.ilike("lastName", lname, MatchMode.START));
		
		StaffDAO sdao = new StaffDAO();
		list = sdao.getSession()
					.createCriteria(Staff.class)	
					.add(and)	
					.list();

		Collections.sort(list);
		
		return list;
	}
	
	/**
	 * 
	 * @param o
	 * @return
	 */
	public int compareTo(Object o) {
		if (o==null || !(o instanceof Staff)) return -1;
		Staff i = (Staff)o;
		if (getPositionCode()==null) {
			if (i.getPositionCode()!=null) return 1;
		} else {
			if (i.getPositionCode()==null) return -1;
			int cmp = getPositionCode().getPositionType().getSortOrder().compareTo(i.getPositionCode().getPositionType().getSortOrder());
			if (cmp!=0) return cmp;
		}
		int cmp = nameLastNameFirst().compareToIgnoreCase(i.nameLastNameFirst());
		if (cmp!=0) return cmp;
		return getUniqueId().compareTo(i.getUniqueId());
	}
	
	/**
	 * 
	 * @return
	 */
	public String nameLastNameFirst() {
		return ((getLastName() == null ? "" : getLastName().trim()) + ", "
				+ (getFirstName() == null ? "" : getFirstName().trim()) + " " + (getMiddleName() == null ? ""
				: getMiddleName().trim()));
	}
	
	public String getDeptName(Long sessionId) {
		if (getDept()==null || getDept().length()==0) return "N/A";
		Department d = Department.findByDeptCode(getDept(), sessionId);
		if (d!=null)
			return "<span title='"+d.getHtmlTitle()+"'>"+d.getShortLabel()+"</span>";
		else
			return getDept();
	}
	
	public String getName(User user) {
		return getName(Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT));
	}
	
	public String getName() {
		return Constants.toInitialCase(
				(getLastName()==null?"":getLastName().trim())+", "+
				(getFirstName()==null?"":getFirstName().trim())+
				(getMiddleName()==null?"":" "+getMiddleName().trim()));
	}
	
	public String getName(String instructorNameFormat) {
		if (DepartmentalInstructor.sNameFormatLastFist.equals(instructorNameFormat))
			return Constants.toInitialCase(
					(getLastName()==null?"":getLastName().trim())+", "+
					(getFirstName()==null?"":getFirstName().trim()));
		if (DepartmentalInstructor.sNameFormatFirstLast.equals(instructorNameFormat))
			return Constants.toInitialCase(
					(getFirstName()==null?"":getFirstName().trim())+" "+
					(getLastName()==null?"":getLastName().trim()));
		if (DepartmentalInstructor.sNameFormatInitialLast.equals(instructorNameFormat))
			return (getFirstName()==null?"":this.getFirstName().trim().substring(0,1).toUpperCase())+
					(getMiddleName()==null?"":" "+this.getMiddleName().trim().substring(0,1).toUpperCase())+" "+
					Constants.toInitialCase(getLastName()==null?"":getLastName().trim());
		if (DepartmentalInstructor.sNameFormatLastInitial.equals(instructorNameFormat))
			return Constants.toInitialCase((getLastName()==null?"":getLastName().trim()))+", "+
					(getFirstName()==null?"":this.getFirstName().trim().substring(0,1).toUpperCase())+
					(getMiddleName()==null?"":" "+this.getMiddleName().trim().substring(0,1).toUpperCase());
		if (DepartmentalInstructor.sNameFormatFirstMiddleLast.equals(instructorNameFormat))
			return Constants.toInitialCase(
					(getFirstName()==null?"":getFirstName().trim())+
					(getMiddleName()==null?"":" "+getMiddleName().trim())+" "+
					(getLastName()==null?"":getLastName().trim()));
		if (DepartmentalInstructor.sNameFormatShort.equals(instructorNameFormat)) {
			StringBuffer sb = new StringBuffer();
			if (getFirstName()!=null && getFirstName().length()>0) {
				sb.append(getFirstName().substring(0,1).toUpperCase());
				sb.append(". ");
			}
			if (getLastName()!=null && getLastName().length()>0) {
				sb.append(getLastName().substring(0,1).toUpperCase());
				sb.append(getLastName().substring(1,Math.min(10,getLastName().length())).toLowerCase().trim());
			}
			return sb.toString();
		}
		return Constants.toInitialCase(
				(getFirstName()==null?"":getFirstName().trim())+
				(getMiddleName()==null?"":" "+getMiddleName().trim())+" "+
				(getLastName()==null?"":getLastName().trim()));
	}
	

}
