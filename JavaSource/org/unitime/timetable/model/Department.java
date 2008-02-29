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

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.criterion.Restrictions;
import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseDepartment;
import org.unitime.timetable.model.base.BaseRoomDept;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


public class Department extends BaseDepartment implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Department () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Department (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
    /** Request attribute name for available departments **/
    public static String DEPT_ATTR_NAME = "deptsList";    
    public static String EXTERNAL_DEPT_ATTR_NAME = "externalDepartments";
    
    public static TreeSet findAll(Long sessionId) {
		return new TreeSet((new DepartmentDAO()).
			getSession().
			createQuery("select distinct d from Department as d where d.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list());
    }
    
	public static TreeSet findAllExternal(Long sessionId) {
		return new TreeSet((new DepartmentDAO()).
				getSession().
				createQuery("select distinct d from Department as d where d.externalManager=1 and d.session.uniqueId=:sessionId").
				setLong("sessionId", sessionId.longValue()).
				setCacheable(true).
				list());
	}
	
    public static TreeSet findAllNonExternal(Long sessionId) {
        return new TreeSet((new DepartmentDAO()).
                getSession().
                createQuery("select distinct d from Department as d where d.externalManager=0 and d.session.uniqueId=:sessionId").
                setLong("sessionId", sessionId.longValue()).
                setCacheable(true).
                list());
    }

    public static TreeSet findAllOwned(Long sessionId, TimetableManager mgr, boolean includeExternal) {
		TreeSet ret = new TreeSet(mgr.departmentsForSession(sessionId));
		if (includeExternal && !mgr.isExternalManager()) ret.addAll(findAllExternal(sessionId));
		return ret;
	}

    /**
     * 
     * @param deptCode
     * @param sessionId
     * @return
     * @throws Exception
     */
	public static Department findByDeptCode(String deptCode, Long sessionId) {
		return (Department)(new DepartmentDAO()).
			getSession().
			createQuery("select distinct d from Department as d where d.deptCode=:deptCode and d.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("deptCode", deptCode).
			setCacheable(true).
			uniqueResult();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
        if (o==null || !(o instanceof Department)) return -1;
        Department d = (Department) o;
        int cmp = Double.compare(
        		isExternalManager()==null?0:isExternalManager().booleanValue()?1:0,
        		d.isExternalManager()==null?0:d.isExternalManager().booleanValue()?1:0);
        if (cmp!=0)
        	return cmp;
        if (getDistributionPrefPriority()!=null && d.getDistributionPrefPriority()!=null) { 
        	cmp = getDistributionPrefPriority().compareTo(d.getDistributionPrefPriority());
        	if (cmp!=0) return cmp;
        }
        if (getDeptCode()!=null && !getDeptCode().equals(d.getDeptCode()))
        	return getDeptCode().compareTo(d.getDeptCode());
		return getUniqueId().compareTo(d.getUniqueId()); 
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.model.PreferenceGroup#canUserEdit(org.unitime.commons.User)
	 */
	protected boolean canUserEdit(User user) {
		TimetableManager tm = TimetableManager.getManager(user);
		if (tm==null) return false;
		
		if (tm.getDepartments().contains(this)) {
			if (isExternalManager().booleanValue() && effectiveStatusType().canManagerEdit())
				return true;
			if (effectiveStatusType().canOwnerEdit()) 
				return true; 
		}
		
		return false;
	}
	
	protected boolean canUserView(User user){
		TimetableManager tm = TimetableManager.getManager(user);
		if (tm==null) return false;
		
		if (tm.getDepartments().contains(this)) {
			if (isExternalManager().booleanValue() && effectiveStatusType().canManagerView())
				return true;
			if (!isExternalManager().booleanValue() && effectiveStatusType().canOwnerView())
				return true; 
		}
		
		return false;
	}
	
	public String htmlLabel(){
		return(this.getDeptCode() + " - " + this.getName());
	}
	
	public String getHtmlTitle() {
		return getDeptCode()+" - "+getName()+(isExternalManager().booleanValue()?" ("+getExternalMgrLabel()+")":"");
	}
	
	public String getShortLabel() {
		if (isExternalManager().booleanValue())
			return getExternalMgrAbbv().trim();
		if (getAbbreviation()!=null && getAbbreviation().trim().length()>0)
			return getAbbreviation().trim();
		return getDeptCode();
	}
	
	public String htmlShortLabel() {
		return
			"<span "+
			"style='color:#"+getRoomSharingColor(null)+";font-weight:bold;' "+
			"title='"+getHtmlTitle()+"'>"+
			getShortLabel()+
			"</span>";
	}
	
	public String toString(){
		return(this.getDeptCode() + " - " + this.getName());
	}
	
	public String getLabel(){
		return(this.getDeptCode() 
		        + " - " + this.getName()) 
		        +  ( (this.isExternalManager().booleanValue()) 
		                ? " ( EXT: " + this.getExternalMgrLabel() + " )"
		                : "" );
	}
	
	public static String color2hex(Color color) {
		return
			(color.getRed()<16?"0":"")+
			Integer.toHexString(color.getRed())+
			(color.getGreen()<16?"0":"")+
			Integer.toHexString(color.getGreen())+
			(color.getBlue()<16?"0":"")+
			Integer.toHexString(color.getBlue());
	}
	
	public static Color hex2color(String hex) {
		if (hex==null || hex.length()!=6) return null;
		return new Color(
				Integer.parseInt(hex.substring(0,2),16),
				Integer.parseInt(hex.substring(2,4),16),
				Integer.parseInt(hex.substring(4,6),16));
	}
	
	private static int distance(String color1, String color2) {
		if (color1.equals(color2)) return 0;
		Color c1 = hex2color(color1);
		Color c2 = hex2color(color2);
		return (int)Math.sqrt(
			((c1.getRed()-c2.getRed())*(c1.getRed()-c2.getRed())) +
			((c1.getGreen()-c2.getGreen())*(c1.getGreen()-c2.getGreen())) +
			((c1.getBlue()-c2.getBlue())*(c1.getBlue()-c2.getBlue())));
	}
	
	public boolean isRoomSharingColorConflicting(String color) {
		for (Iterator i=getRoomDepts().iterator();i.hasNext();) {
			RoomDept rd = (RoomDept)i.next();
			for (Iterator j=rd.getRoom().getRoomDepts().iterator();j.hasNext();) {
				BaseDepartment d = (BaseDepartment)((RoomDept)j.next()).getDepartment();
				if (d.equals(this)) continue;
				if (d.getRoomSharingColor()==null) continue;
				if (distance(color, d.getRoomSharingColor())<50) return true; 
			}
		}
		return false;
	}
	
	public boolean isRoomSharingColorConflicting(String color, Collection otherDepartments) {
		if (isRoomSharingColorConflicting(color)) return true;
		if (otherDepartments!=null && !otherDepartments.isEmpty()) {
			for (Iterator i=otherDepartments.iterator();i.hasNext();) {
				Object o = i.next();
				BaseDepartment d = null;
				if (o instanceof BaseDepartment) {
					d = (BaseDepartment)o;
				} else if (o instanceof BaseRoomDept) {
					d = ((BaseRoomDept)o).getDepartment();
				} else if (o instanceof Long) {
					d = (new DepartmentDAO()).get((Long)o);
				}
				if (d==null) continue;
				if (d.equals(this)) continue;
				if (color.equals(d.getRoomSharingColor()))
					return true; 
			}
		}
		return false;
	}

	public void fixRoomSharingColor(Collection otherDepartments) {
		String color = getRoomSharingColor();
		if (isRoomSharingColorConflicting(color, otherDepartments)) {
			int idx = 0;
			color = color2hex(RoomSharingModel.sDepartmentColors[idx]);
			while (isRoomSharingColorConflicting(color, otherDepartments)) {
				idx++;
				if (idx>=RoomSharingModel.sDepartmentColors.length) {
					color = color2hex(new Color((int)(256.0*Math.random()),(int)(256.0*Math.random()),(int)(256.0*Math.random())));
				} else {
					color = color2hex(RoomSharingModel.sDepartmentColors[idx]);
				}
			}
			setRoomSharingColor(color);
			(new DepartmentDAO()).saveOrUpdate(this);
		}
	}
	
	public String getRoomSharingColor(Collection otherDepartments) {
		if (getRoomSharingColor()==null) {
			setRoomSharingColor(color2hex(RoomSharingModel.sDepartmentColors[0]));
		}
		fixRoomSharingColor(otherDepartments);
		return getRoomSharingColor(); 
	}
	
	public String getManagingDeptLabel(){
		if (isExternalManager().booleanValue()){
			return(getExternalMgrLabel());
		} else {
			return(getDeptCode()+" - "+getName());
		}
	}
	
	public String getManagingDeptAbbv(){
		return "<span title='"+getHtmlTitle()+"'>"+getShortLabel()+"</span>";
	}

	public Collection getClasses() {
		return (new DepartmentDAO()).
			getSession().
			createQuery("select distinct c from Class_ as c where c.managingDept=:departmentId").
			setLong("departmentId", getUniqueId().longValue()).
			list();
	}
	
	public Collection getClassesFetchWithStructure() {
		return (new DepartmentDAO()).
			getSession().
			createQuery("select distinct c from Class_ as c " +
				"left join fetch c.childClasses as cc "+
				"left join fetch c.schedulingSubpart as ss "+
				"left join fetch ss.childSubparts as css "+
				"left join fetch ss.instrOfferingConfig as ioc "+
				"left join fetch ioc.instructionalOffering as io "+
				"left join fetch io.courseOfferings as cox "+
				"where c.managingDept=:departmentId").
				setLong("departmentId", getUniqueId().longValue()).
				list();

	}
	
	public Collection getNotAssignedClasses(Solution solution) {
		return (new DepartmentDAO()).
		getSession().
		createQuery(
				"select distinct c from Class_ as c where c.managingDept=:departmentId and "+
				"not exists (from c.assignments as a where a.solution=:solutionId)"
				).
		setLong("departmentId", getUniqueId().longValue()).
		setInteger("solutionId", solution.getUniqueId().intValue()).
		list();
	}
	
	public static TreeSet findAllBeingUsed(Long sessionId) {
		TreeSet ret = new TreeSet(
				(new DepartmentDAO()).
				getSession().
				createQuery("select distinct d from Department as d inner join d.timetableManagers as m where d.session.uniqueId=:sessionId").
				setLong("sessionId", sessionId.longValue()).
				setCacheable(true).
				list());
		ret.addAll(
				(new DepartmentDAO()).
				getSession().
				createQuery("select distinct d from Department as d inner join d.roomDepts as r where d.session.uniqueId=:sessionId").
				setLong("sessionId", sessionId.longValue()).
				setCacheable(true).
				list());
		return ret;
	}

	public static String[] getDeptCodesForUser(User user, boolean includeExternal) throws Exception {
		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
		boolean isViewAll = user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE) || user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager manager = tdao.get(new Long(mgrId));
		
		String[] depts = new String[] {};
		if (isAdmin || isViewAll) {
			depts = null;
		} else {
			Set departments = findAllOwned(sessionId, manager, includeExternal);
			if (departments!=null) {
				depts = new String[departments.size()];
				int idx = 0;
				for (Iterator i=departments.iterator();i.hasNext();) {
					depts[idx++] = ((Department)i.next()).getDeptCode();
				}
			}
		}
		return depts;
	}
	
	public DepartmentStatusType effectiveStatusType() {
		DepartmentStatusType t = getStatusType();
		if (t!=null) return t;
		return getSession().getStatusType();
	}
	
	public Long getSessionId(){
		if (getSession() != null){
			return(getSession().getUniqueId());
		} else {
			return(null);
		}
	}
	public Object clone() {
		Department d = new Department();
		d.setSession(getSession());
		d.setAbbreviation(getAbbreviation());
		d.setAllowReqRoom(isAllowReqRoom());
		d.setAllowReqTime(isAllowReqTime());
		d.setDeptCode(getDeptCode());
		d.setDistributionPrefPriority(getDistributionPrefPriority());
		d.setExternalManager(isExternalManager());
		d.setExternalMgrAbbv(getExternalMgrAbbv());
		d.setExternalMgrLabel(getExternalMgrLabel());
		d.setExternalUniqueId(getExternalUniqueId());
		d.setName(getName());
		d.setStatusType(getStatusType());
		return d;
	}
	
	public Department findSameDepartmentInSession(Long newSessionId){
		if (newSessionId == null){
			return(null);
		}
		Department newDept = Department.findByDeptCode(this.getDeptCode(), newSessionId);
		if (newDept == null && this.getExternalUniqueId() != null){
			// if a department wasn't found and an external uniqueid exists for this 
			//   department check to see if the new term has a department that matches 
			//   the external unique id
			List l = (new DepartmentDAO()).
			getSession().
			createCriteria(Department.class).
			add(Restrictions.eq("externalUniqueId",this.getExternalUniqueId())).
			add(Restrictions.eq("session.uniqueId", newSessionId)).
			setCacheable(true).list();

			if (l.size() == 1){
				newDept = (Department) l.get(0);
			}
		}
		return(newDept);
	}

	public Department findSameDepartmentInSession(Session newSession){
		if (newSession != null) return(findSameDepartmentInSession(newSession.getUniqueId()));
		else return(null);
	}
	
    public boolean isLimitedEditableBy(User user){
        if (user==null) return false;
        if (user.isAdmin()) return true;
        
        TimetableManager tm = TimetableManager.getManager(user);
        if (tm==null) return false;

        if (!tm.getDepartments().contains(this)) return false;
        
        if (!effectiveStatusType().canOwnerLimitedEdit()) return false;

        return true;
    }

}