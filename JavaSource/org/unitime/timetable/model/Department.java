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

import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.FlushMode;
import org.unitime.commons.NaturalOrderComparator;
import org.unitime.timetable.model.base.BaseDepartment;
import org.unitime.timetable.model.base.BaseRoomDept;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.UserQualifier;
import org.unitime.timetable.security.rights.Right;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
@Entity
@Table(name = "department")
public class Department extends BaseDepartment implements Comparable<Department>, Qualifiable {
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
    
    @SuppressWarnings("unchecked")
	public static TreeSet<Department> findAll(Long sessionId) {
		return new TreeSet<Department>((DepartmentDAO.getInstance()).
			getSession().
			createQuery("select distinct d from Department as d where d.session.uniqueId=:sessionId", Department.class).
			setParameter("sessionId", sessionId.longValue()).
			setCacheable(true).
			list());
    }
    
	@SuppressWarnings("unchecked")
	public static TreeSet<Department> findAllExternal(Long sessionId) {
		return new TreeSet<Department>((DepartmentDAO.getInstance()).
				getSession().
				createQuery("select distinct d from Department as d where d.externalManager=true and d.session.uniqueId=:sessionId", Department.class).
				setParameter("sessionId", sessionId.longValue()).
				setCacheable(true).
				list());
	}
	
    @SuppressWarnings("unchecked")
	public static TreeSet<Department> findAllNonExternal(Long sessionId) {
        return new TreeSet<Department>((DepartmentDAO.getInstance()).
                getSession().
                createQuery("select distinct d from Department as d where d.externalManager=false and d.session.uniqueId=:sessionId", Department.class).
                setParameter("sessionId", sessionId.longValue()).
                setCacheable(true).
                list());
    }

    /**
     * 
     * @param deptCode
     * @param sessionId
     * @return
     * @throws Exception
     */
	public static Department findByDeptCode(String deptCode, Long sessionId) {
		return(findByDeptCode(deptCode, sessionId, (DepartmentDAO.getInstance()). getSession()));
	}

    /**
     * 
     * @param deptCode
     * @param sessionId
     * @param hibSession
     * @return
     * @throws Exception
     */
	public static Department findByDeptCode(String deptCode, Long sessionId, org.hibernate.Session hibSession) {
		return hibSession.
			createQuery("select distinct d from Department as d where d.deptCode=:deptCode and d.session.uniqueId=:sessionId", Department.class).
			setParameter("sessionId", sessionId).
			setParameter("deptCode", deptCode).
			setCacheable(true).
			setHibernateFlushMode(FlushMode.MANUAL).
			uniqueResult();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Department d) {
        int cmp = Double.compare(
        		isExternalManager() == null ? 0 : isExternalManager() ? 1 : 0,
        		d.isExternalManager() == null ? 0 : d.isExternalManager() ? 1 : 0);
        if (cmp!=0) return cmp;
        cmp = new NaturalOrderComparator().compare(
        		getDeptCode() == null ? "" : getDeptCode(),
        		d.getDeptCode() == null ? "" : d.getDeptCode());
        if (cmp!=0) return cmp;
        cmp = new NaturalOrderComparator().compare(
        		getAbbreviation() == null ? "" : getAbbreviation(),
        		d.getAbbreviation() == null ? "" : d.getAbbreviation());
        if (cmp!=0) return cmp;
		return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(d.getUniqueId() == null ? -1 : d.getUniqueId()); 
	}

	public String htmlLabel(){
		return(this.getDeptCode() + " - " + this.getName());
	}
	
	@Transient
	public String getHtmlTitle() {
		return getDeptCode()+" - "+getName()+(isExternalManager().booleanValue()?" ("+getExternalMgrLabel()+")":"");
	}
	
	@Transient
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
	
	@Transient
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
	
	@SuppressWarnings("unchecked")
	public boolean isRoomSharingColorConflicting(String color) {
		if (getUniqueId() == null) return false;
		boolean ret = false;
		for (String other: DepartmentDAO.getInstance().getSession().createQuery(
				"select distinct x.department.roomSharingColor from Department d inner join d.roomDepts rd inner join rd.room.roomDepts x " +
				"where d.uniqueId = :uniqueId and d != x.department", String.class
				).setParameter("uniqueId", getUniqueId()).setCacheable(true).list()) {
			if (other != null && distance(color, other) < 50) { ret = true; break; }
		}
		return ret;
	}
	
	public boolean isRoomSharingColorConflicting(String color, Collection<Object> otherDepartments) {
		if (isRoomSharingColorConflicting(color)) return true;
		if (otherDepartments!=null && !otherDepartments.isEmpty()) {
			for (Iterator<Object> i=otherDepartments.iterator();i.hasNext();) {
				Object o = i.next();
				BaseDepartment d = null;
				if (o instanceof BaseDepartment) {
					d = (BaseDepartment)o;
				} else if (o instanceof BaseRoomDept) {
					d = ((BaseRoomDept)o).getDepartment();
				} else if (o instanceof Long) {
					d = (DepartmentDAO.getInstance()).get((Long)o);
				}
				if (d==null) continue;
				if (d.equals(this)) continue;
				if (color.equals(d.getRoomSharingColor()))
					return true; 
			}
		}
		return false;
	}

	public void fixRoomSharingColor(Collection<Object> otherDepartments) {
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
			DepartmentDAO.getInstance().getSession().merge(this);
			DepartmentDAO.getInstance().getSession().flush();
		}
	}
	
	@SuppressWarnings("unchecked")
	public String getRoomSharingColor(@SuppressWarnings("rawtypes") Collection otherDepartments) {
		if (getRoomSharingColor() == null) {
			setRoomSharingColor(color2hex(RoomSharingModel.sDepartmentColors[0]));
		}
		fixRoomSharingColor(otherDepartments);
		return getRoomSharingColor(); 
	}
	
	@Transient
	public String getManagingDeptLabel(){
		if (isExternalManager().booleanValue()){
			return(getExternalMgrLabel());
		} else {
			return(getDeptCode()+" - "+getName());
		}
	}
	
	@Transient
	public String getManagingDeptAbbv(){
		return "<span title='"+getHtmlTitle()+"'>"+getShortLabel()+"</span>";
	}

	@SuppressWarnings("unchecked")
	@Transient
	public Collection<Class_> getClasses() {
		return (DepartmentDAO.getInstance()).
			getSession().
			createQuery("select distinct c from Class_ as c where c.managingDept.uniqueId=:departmentId or (c.managingDept is null and c.controllingDept.uniqueId=:departmentId)", Class_.class).
			setParameter("departmentId", getUniqueId()).
			list();
	}
	
	@SuppressWarnings("unchecked")
	@Transient
	public Collection<Class_> getClassesFetchWithStructure() {
		return (DepartmentDAO.getInstance()).
			getSession().
			createQuery("select distinct c from Class_ as c " +
				"left join fetch c.childClasses as cc "+
				"left join fetch c.schedulingSubpart as ss "+
				"left join fetch ss.childSubparts as css "+
				"left join fetch ss.instrOfferingConfig as ioc "+
				"left join fetch ioc.instructionalOffering as io "+
				"left join fetch io.courseOfferings as cox "+
				"where c.managingDept.uniqueId=:departmentId or (c.managingDept is null and c.controllingDept.uniqueId=:departmentId)", Class_.class).
				setParameter("departmentId", getUniqueId()).
				list();

	}
	
	@SuppressWarnings("unchecked")
	public Collection<Class_> getNotAssignedClasses(Solution solution) {
		return (DepartmentDAO.getInstance()).
		getSession().
		createQuery(
				"select distinct c from Class_ as c where (c.managingDept.uniqueId=:departmentId or (c.managingDept is null and c.controllingDept.uniqueId=:departmentId)) and "+
				"not exists (from c.assignments as a where a.solution.uniqueId=:solutionId)", Class_.class
				).
		setParameter("departmentId", getUniqueId()).
		setParameter("solutionId", solution.getUniqueId()).
		list();
	}
	
	@SuppressWarnings("unchecked")
	public static TreeSet<Department> findAllBeingUsed(Long sessionId) {
		TreeSet<Department> ret = new TreeSet<Department>(
				(DepartmentDAO.getInstance()).
				getSession().
				createQuery("select distinct d from Department as d inner join d.timetableManagers as m where d.session.uniqueId=:sessionId", Department.class).
				setParameter("sessionId", sessionId.longValue()).
				setCacheable(true).
				list());
		ret.addAll(
				(DepartmentDAO.getInstance()).
				getSession().
				createQuery("select distinct d from Department as d inner join d.roomDepts as r where d.session.uniqueId=:sessionId", Department.class).
				setParameter("sessionId", sessionId.longValue()).
				setCacheable(true).
				list());
		ret.addAll(
				(DepartmentDAO.getInstance()).
				getSession().
				createQuery("select distinct d from Department as d inner join d.subjectAreas as r where d.session.uniqueId=:sessionId", Department.class).
				setParameter("sessionId", sessionId.longValue()).
				setCacheable(true).
				list());
		return ret;
	}
	
	public DepartmentStatusType effectiveStatusType() {
		DepartmentStatusType t = getStatusType();
		if (t!=null) return t;
		return getSession().getStatusType();
	}

	public DepartmentStatusType effectiveStatusType(Department controllingDepartment) {
		if (isExternalManager() && controllingDepartment != null && getExternalStatusTypes() != null) {
			for (ExternalDepartmentStatusType t: getExternalStatusTypes()) {
				if (controllingDepartment.equals(t.getDepartment())) return t.getStatusType();
			}
		}
		return effectiveStatusType();
	}
	
	public DepartmentStatusType effectiveStatusType(UserContext cx) {
		if (isExternalManager() && getExternalStatusTypes() != null && !cx.hasDepartment(getUniqueId())) {
			Integer status = null;
			for (ExternalDepartmentStatusType t: getExternalStatusTypes()) {
				if (cx.hasDepartment(t.getDepartment().getUniqueId())) {
					if (status == null) {
						status = t.getStatusType().getStatus();
					} else {
						status = Integer.valueOf(status | t.getStatusType().getStatus());
					}
				}
			}
			if (status != null) {
				DepartmentStatusType ret = new DepartmentStatusType(); ret.setStatus(status); return ret;
			}
		}
		return effectiveStatusType();
	}
	
	@Transient
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
		d.setAllowReqDistribution(isAllowReqDistribution());
		d.setDeptCode(getDeptCode());
		d.setDistributionPrefPriority(getDistributionPrefPriority());
		d.setExternalManager(isExternalManager());
		d.setExternalMgrAbbv(getExternalMgrAbbv());
		d.setExternalMgrLabel(getExternalMgrLabel());
		d.setExternalUniqueId(getExternalUniqueId());
		d.setName(getName());
		d.setStatusType(getStatusType());
		d.setAllowEvents(isAllowEvents());
		d.setInheritInstructorPreferences(isInheritInstructorPreferences());
		d.setAllowStudentScheduling(isAllowStudentScheduling());
		d.setExternalFundingDept(getExternalFundingDept());
		return d;
	}
	
	public Department findSameDepartmentInSession(Long newSessionId){
		return(findSameDepartmentInSession(newSessionId, (DepartmentDAO.getInstance()).getSession()));
	}

	public Department findSameDepartmentInSession(Long newSessionId, org.hibernate.Session hibSession){
		if (newSessionId == null){
			return(null);
		}
		Department newDept = Department.findByDeptCode(this.getDeptCode(), newSessionId, hibSession);
		if (newDept == null && this.getExternalUniqueId() != null){
			// if a department wasn't found and an external uniqueid exists for this 
			//   department check to see if the new term has a department that matches 
			//   the external unique id
			@SuppressWarnings("unchecked")
			List<Department> l = hibSession.createQuery("from Department where externalUniqueId = :externalUniqueId and session.uniqueId = :newSessionId", Department.class)
				.setParameter("externalUniqueId",  this.getExternalUniqueId())
				.setParameter("newSessionId", newSessionId)
				.setHibernateFlushMode(FlushMode.MANUAL)
				.setCacheable(true).list();

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
	
	@Override
	@Transient
	public Serializable getQualifierId() {
		return getUniqueId();
	}

	@Override
	@Transient
	public String getQualifierType() {
		return getClass().getSimpleName();
	}

	@Override
	@Transient
	public String getQualifierReference() {
		return getDeptCode();
	}

	@Override
	@Transient
	public String getQualifierLabel() {
		return getName();
	}
	
	public static TreeSet<Department> getUserDepartments(UserContext user) {
		return getUserDepartments(user, true);
	}
	
	public static TreeSet<Department> getUserDepartments(UserContext user, boolean usedOnly) {
		TreeSet<Department> departments = new TreeSet<Department>();
		if (user == null || user.getCurrentAuthority() == null) return departments;
		if (user.getCurrentAuthority().hasRight(Right.DepartmentIndependent)) {
			if (usedOnly)
				departments.addAll(Department.findAllBeingUsed(user.getCurrentAcademicSessionId()));
			else
				departments.addAll(Department.findAll(user.getCurrentAcademicSessionId()));
		} else
			for (UserQualifier q: user.getCurrentAuthority().getQualifiers("Department"))
				departments.add(DepartmentDAO.getInstance().get((Long)q.getQualifierId()));
		return departments;
	}
	
	@Override
	@Transient
	public Department getDepartment() { return this; }
	
	@SuppressWarnings("unchecked")
	@Transient
	public Set<InstructorAttributeType> getAvailableAttributeTypes() {
		return new TreeSet<InstructorAttributeType>(DepartmentDAO.getInstance().getSession().createQuery(
        		"select distinct t from InstructorAttribute a inner join a.type t " +
        		"where a.session.uniqueId = :sessionId and (a.department is null or a.department.uniqueId = :departmentId)",
        		InstructorAttributeType.class)
				.setParameter("sessionId", getSessionId()).setParameter("departmentId", getUniqueId()).setCacheable(true).list());
	}

	@SuppressWarnings("unchecked")
	@Transient
	public Set<InstructorAttribute> getAvailableAttributes() {
		return new TreeSet<InstructorAttribute>(DepartmentDAO.getInstance().getSession().createQuery(
        		"select a from InstructorAttribute a " +
        		"where a.session.uniqueId = :sessionId and (a.department is null or a.department.uniqueId = :departmentId)",
        		InstructorAttribute.class)
				.setParameter("sessionId", getSessionId()).setParameter("departmentId", getUniqueId()).setCacheable(true).list());
	}
	
	public static boolean isInstructorSchedulingCommitted(Long departmentId) {
		Number oc = DepartmentDAO.getInstance().getSession().createQuery(
				"select count(oc) from OfferingCoordinator oc where oc.teachingRequest is not null and " +
				"oc.instructor.department.uniqueId = :departmentId", Number.class)
				.setParameter("departmentId", departmentId).
				setCacheable(true).uniqueResult();
		if (oc.intValue() > 0) return true;
		Number ci = DepartmentDAO.getInstance().getSession().createQuery(
				"select count(ci) from ClassInstructor ci where ci.teachingRequest is not null and " +
				"ci.instructor.department.uniqueId = :departmentId", Number.class)
				.setParameter("departmentId", departmentId).
				setCacheable(true).uniqueResult();
		return ci.intValue() > 0;
	}

}
