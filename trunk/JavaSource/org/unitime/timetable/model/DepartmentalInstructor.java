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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseDepartmentalInstructor;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.util.Constants;




public class DepartmentalInstructor extends BaseDepartmentalInstructor implements Comparable{
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public DepartmentalInstructor () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public DepartmentalInstructor (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/// Copied from Instructor & InstructorDept
	
	/** Request attribute name for available instructors **/
	public static String INSTR_LIST_ATTR_NAME = "instructorsList";
    public static String INSTR_HAS_PREF_ATTR_NAME = "instructorsHasPrefs";
	
	/** Request attribute name for instructor departments  **/
    public static String INSTRDEPT_LIST_ATTR_NAME = "instructorDeptList";
	
    /** Instructor List **/
    private static Vector instructorDeptList = null;
    
	/** Name Format */
	public static final String sNameFormatLastFist = "last-first";
	public static final String sNameFormatFirstLast = "first-last";
	public static final String sNameFormatInitialLast = "initial-last";
	public static final String sNameFormatLastInitial = "last-initial";
	public static final String sNameFormatFirstMiddleLast = "first-middle-last";
	public static final String sNameFormatShort = "short";
	

	/**
	 * 
	 * @return
	 */
	public String nameLastNameFirst() {
		return ((getLastName() == null ? "" : getLastName().trim()) + ", "
				+ (getFirstName() == null ? "" : getFirstName().trim()) + " " + (getMiddleName() == null ? ""
				: getMiddleName().trim()));
	}

	/**
	 * 
	 * @return
	 */
	public String nameFirstNameFirst() {
		StringBuffer sb = new StringBuffer();
		if (this.getFirstName() != null) {
			sb.append(this.getFirstName());
		}
		if (this.getMiddleName() != null) {
			sb.append(" " + this.getMiddleName());
		}
		if (this.getLastName() != null) {
			sb.append(" " + this.getLastName());
		}
		return (sb.toString());
	}
	
	public String nameShort() {
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

	/**
	 * Property nameFirst used in JSPs
	 * Gets full name with first name first 
	 * @return
	 */
	public String getNameFirst() {
		return nameFirstNameFirst();
	}

	/**
	 * Property nameLast used in JSPs
	 * Gets full name with last name first 
	 * @return
	 */
	public String getNameLast() {
		return nameLastNameFirst();
	}

	/**
	 * 
	 * @return
	 */
	public String getNameLastFirst() {
		return nameLastFirst();
	}

	/**
	 * 
	 * @return
	 */
	public String getNameInitLast() {
		return nameInitLast();
	}

	/**
	 * 
	 * @return
	 */
	private String nameInitLast() {
		return ((this.getFirstName() == null ? "" : this.getFirstName().trim()
				.substring(0, 1).toUpperCase())
				+ (this.getMiddleName() == null ? "" : " " + this.getMiddleName()
						.trim().substring(0, 1).toUpperCase()) + " " + (Constants
				.toInitialCase(this.getLastName() == null ? "" : this
						.getLastName().trim())));
	}
	
	/**
	 * 
	 * @return
	 */
	private String nameLastInit() {
		return (Constants.toInitialCase(
					this.getLastName() == null 
										? "" 
									    : this.getLastName().trim()) + ", " +
				(this.getFirstName() == null ? "" : this.getFirstName().trim()
				.substring(0, 1).toUpperCase())
				+ (this.getMiddleName() == null ? "" : " " + this.getMiddleName()
						.trim().substring(0, 1).toUpperCase()) );
	}

	/**
	 * 
	 * @return
	 */
	private String nameLastFirst() {
		return (Constants.toInitialCase((this.getLastName() == null ? "" : this.getLastName().trim())
				+ ", " + (this.getFirstName() == null ? "" : this
				.getFirstName().trim())));
	}
	
	public String nameFirstLast() {
		return (Constants.toInitialCase((this.getFirstName() == null ? "" : this.getFirstName().trim())
				+ " " + (this.getLastName() == null ? "" : this.getLastName().trim())));
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	public String getName(User user) {
		return getName(Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT));
	}
	
	public String getName(String instructorNameFormat) {
		if (sNameFormatLastFist.equals(instructorNameFormat))
			return getNameLastFirst();
		if (sNameFormatFirstLast.equals(instructorNameFormat))
			return nameFirstLast();
		if (sNameFormatInitialLast.equals(instructorNameFormat))
			return getNameInitLast();
		if (sNameFormatLastInitial.equals(instructorNameFormat))
			return nameLastInit();
		if (sNameFormatFirstMiddleLast.equals(instructorNameFormat))
			return nameFirstMiddleLast();
		if (sNameFormatShort.equals(instructorNameFormat))
			return nameShort();
		return nameFirstMiddleLast();
	}

	/**
	 * 
	 * @return
	 */
	private String nameFirstMiddleLast() {
		return (
				Constants.toInitialCase((this.getFirstName() == null 
									? ""
				                    : this.getFirstName().trim()) + " " +                 
                (this.getMiddleName() == null
                					? "" 
                					: this.getMiddleName().trim()) + " " + 
				(this.getLastName() == null 
									? "" 
				                    : this.getLastName().trim())
				));
	}

	/**
	 * Remove class from instructor list
	 * @param ci
	 */
	public void removeClassInstructor(ClassInstructor classInstr) {
		Set s = this.getClasses();
		for (Iterator iter = s.iterator(); iter.hasNext();) {
			ClassInstructor ci = (ClassInstructor) iter.next();
			if (ci.getUniqueId().intValue() == classInstr.getUniqueId()
					.intValue()) {
				s.remove(ci);
				break;
			}
		}
	}

	protected boolean canUserEdit(User user) {
	    if (user.getRole().equals(Roles.EXAM_MGR_ROLE) && 
	        getDepartment().effectiveStatusType().canExamTimetable()) return true;
		return getDepartment().canUserEdit(user);
	}
	
	protected boolean canUserView(User user){
		return getDepartment().canUserView(user);
	}

	public String htmlLabel(){
		return(this.nameFirstNameFirst() + ", " + this.getDepartment().getDeptCode());
	}
	
    public Set getAvailableRooms() {
    	Set rooms =  new TreeSet();
        for (Iterator i=getDepartment().getRoomDepts().iterator();i.hasNext();) {
        	RoomDept roomDept = (RoomDept)i.next();
        	rooms.add(roomDept.getRoom());
        }
        return rooms;
    }
    
    public Set getAvailableRoomFeatures() {
    	Set features = super.getAvailableRoomFeatures();
    	features.addAll((DepartmentRoomFeature.getAllDepartmentRoomFeatures(getDepartment())));
    	return features;
    }
    
    public Set getAvailableRoomGroups() {
    	Set groups = super.getAvailableRoomGroups();
    	groups.addAll(RoomGroup.getAllDepartmentRoomGroups(getDepartment()));
    	return groups;
    }

	public Set prefsOfTypeForDepartment(Class type, Department dept) {
		if (dept==null || dept.equals(getDepartment()))
			return getPreferences(type);
		else
			return null;
	}	

	/**
	 * 
	 * @param sessionId
	 * @param di
	 * @return
	 */
	public static List getAllForInstructor(DepartmentalInstructor di, Long sessionId) {
		if (di == null || di.getExternalUniqueId()==null || di.getExternalUniqueId().trim().length()==0 ){
		    ArrayList list1 = new ArrayList();
		    list1.add(di);
			return(list1);
		}
		
		DepartmentalInstructorDAO ddao = new DepartmentalInstructorDAO();
		String query = "from DepartmentalInstructor where externalUniqueId=:puid and department.session.uniqueId=:sessionId";
		Query q = ddao.getSession().createQuery(query);
		q.setString("puid", di.getExternalUniqueId());
		q.setLong("sessionId", sessionId.longValue());
		List list = q.list();
		
		if(list == null) {
		    list = new ArrayList();
		    list.add(di);
		}

		return list;
	}
	
	/**
	 * 
	 * @param deptCode
	 * @return
	 */
	public static List getInstructorByDept(Long sessionId, Long deptId) throws Exception {	
		
		StringBuffer query = new StringBuffer();
		query.append("select distinct i ");
	    query.append("  from DepartmentalInstructor i ");
		query.append(" where i.department.session.uniqueId = :acadSessionId ");
		if (deptId!=null)
			query.append(" and i.department.uniqueId = :deptId");
        query.append(" order by i.lastName ");
        
        DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
		org.hibernate.Session hibSession = idao.getSession();

		Query q = hibSession.createQuery(query.toString());
		q.setFetchSize(5000);
		q.setCacheable(true);
		q.setLong("acadSessionId", sessionId.longValue());
		if (deptId!=null)
		    q.setLong("deptId", deptId.longValue());
        
		List result = q.list();
		return result;
	}
	
	/**
	 * 
	 * @param o
	 * @return
	 */
	public int compareTo(Object o) {
		if (o==null || !(o instanceof DepartmentalInstructor)) return -1;
		DepartmentalInstructor i = (DepartmentalInstructor)o;
		int cmp = nameLastNameFirst().compareToIgnoreCase(i.nameLastNameFirst());
		if (cmp!=0) return cmp;
		return getUniqueId().compareTo(i.getUniqueId());
	}

	/**
	 * 
	 * @param puid
	 * @return
	 */
	public static boolean existInst(String puid) {
		if (puid == null){
			return(false);
		}

		DepartmentalInstructorDAO ddao = new DepartmentalInstructorDAO();
		List list = ddao.getSession()
						.createCriteria(DepartmentalInstructor.class)	
						.add(Restrictions.eq("externalUniqueId", puid))	
						.list();
		
		if(list.size() != 0){
			return true;
		} else {
			return false;
		}
	}
	
	public static DepartmentalInstructor findByPuidDepartmentId(String puid, Long deptId) {
		try {
		return (DepartmentalInstructor)(new DepartmentalInstructorDAO()).
			getSession().
			createQuery("select d from DepartmentalInstructor d where d.externalUniqueId=:puid and d.department.uniqueId=:deptId").
			setString("puid", puid).
			setLong("deptId",deptId.longValue()).
			setCacheable(true).
			uniqueResult();
		} catch (NonUniqueResultException e) {
			Debug.warning("There are two or more instructors with puid "+puid+" for department "+deptId+" -- returning the first one.");
			return (DepartmentalInstructor)(new DepartmentalInstructorDAO()).
				getSession().
				createQuery("select d from DepartmentalInstructor d where d.externalUniqueId=:puid and d.department.uniqueId=:deptId").
				setString("puid", puid).
				setLong("deptId",deptId.longValue()).
				setCacheable(true).
				list().get(0);
		}
	}
	
	public DepartmentalInstructor findThisInstructorInSession(Long sessionId){
		Department newDept = this.getDepartment().findSameDepartmentInSession(sessionId);
		if (newDept != null){
			return(findByPuidDepartmentId(this.getExternalUniqueId(), newDept.getUniqueId()));
		}
		return(null);
	}
	
    public DepartmentalInstructor getNextDepartmentalInstructor(HttpSession session, User user, boolean canEdit, boolean canView) throws Exception {
    	List instructors = DepartmentalInstructor.getInstructorByDept(getDepartment().getSessionId(),getDepartment().getUniqueId());
    	DepartmentalInstructor next = null;
    	for (Iterator i=instructors.iterator();i.hasNext();) {
    		DepartmentalInstructor di = (DepartmentalInstructor)i.next();
    		if (canEdit && !di.isEditableBy(user)) continue;
    		if (canView && !di.isViewableBy(user)) continue;
    		if (this.compareTo(di)>=0) continue;
    		if (next==null || next.compareTo(di)>0)
    			next = di;
    	}
    	return next;
    }
	
    public DepartmentalInstructor getPreviousDepartmentalInstructor(HttpSession session, User user, boolean canEdit, boolean canView) throws Exception {
    	List instructors = DepartmentalInstructor.getInstructorByDept(getDepartment().getSessionId(),getDepartment().getUniqueId());
    	DepartmentalInstructor prev = null;
    	for (Iterator i=instructors.iterator();i.hasNext();) {
    		DepartmentalInstructor di = (DepartmentalInstructor)i.next();
    		if (canEdit && !di.isEditableBy(user)) continue;
    		if (canView && !di.isViewableBy(user)) continue;
    		if (this.compareTo(di)<=0) continue;
    		if (prev==null || prev.compareTo(di)<0)
    			prev = di;
    	}
    	return prev;
    }
    
    public String toString() {
        return nameShort();
    }
    
    public boolean hasPreferences() {
        if (getPreferences()==null || getPreferences().isEmpty()) return false;
        for (Iterator i=getPreferences().iterator();i.hasNext();) {
            Preference preference = (Preference)i.next();
            if (preference instanceof TimePref) {
                TimePref timePref = (TimePref)preference;
                if (timePref.getTimePatternModel().isDefault()) continue;
            }
            return true;
        }
        return false;
    }
    
	public Object clone(){
		DepartmentalInstructor newDepartmentalInstructor = new DepartmentalInstructor();
		newDepartmentalInstructor.setCareerAcct(getCareerAcct());
		newDepartmentalInstructor.setDepartment(getDepartment());
		newDepartmentalInstructor.setExternalUniqueId(getExternalUniqueId());
		newDepartmentalInstructor.setFirstName(getFirstName());
		newDepartmentalInstructor.setMiddleName(getMiddleName());
		newDepartmentalInstructor.setLastName(getLastName());
		newDepartmentalInstructor.setIgnoreToFar(isIgnoreToFar());
		newDepartmentalInstructor.setNote(getNote());
		newDepartmentalInstructor.setPositionType(getPositionType());
		newDepartmentalInstructor.setEmail(getEmail());
		return(newDepartmentalInstructor);
	}
	
    public static List findAllExamInstructors(Long sessionId, Integer examType) {
        return (new DepartmentalInstructorDAO()).getSession()
                .createQuery("select distinct i from Exam x inner join x.instructors i where x.session.uniqueId=:sessionId and x.examType=:examType")
                .setLong("sessionId", sessionId)
                .setInteger("examType", examType)
                .setCacheable(true).list();
    }
    
    public List getExams(Integer examType) {
        if (getExternalUniqueId()!=null) {
            return (new DepartmentalInstructorDAO()).getSession()
                .createQuery("select distinct x from Exam x inner join x.instructors i where " +
                		"(i.uniqueId=:instructorId or (i.externalUniqueId=:externalId and i.department.session.uniqueId=:sessionId)) " +
                		"and x.examType=:examType")
                .setLong("instructorId", getUniqueId())
                .setLong("sessionId", getDepartment().getSession().getUniqueId())
                .setString("externalId", getExternalUniqueId())
                .setInteger("examType", examType)
                .setCacheable(true).list();
        } else {
            return (new DepartmentalInstructorDAO()).getSession()
            .createQuery("select distinct x from Exam x inner join x.instructors i where i.uniqueId=:instructorId and x.examType=:examType")
            .setLong("instructorId", getUniqueId())
            .setInteger("examType", examType)
            .setCacheable(true).list();
            
        }
    }
}