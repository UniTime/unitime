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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.FlushMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.ExternalUidLookup;
import org.unitime.timetable.interfaces.ExternalUidLookup.UserInfo;
import org.unitime.timetable.model.base.BaseDepartmentalInstructor;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;




/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
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
	
	/** Name Format */
	public static final String sNameFormatLastFist = "last-first";
	public static final String sNameFormatFirstLast = "first-last";
	public static final String sNameFormatInitialLast = "initial-last";
	public static final String sNameFormatLastInitial = "last-initial";
	public static final String sNameFormatFirstMiddleLast = "first-middle-last";
	public static final String sNameFormatLastFirstMiddle = "last-first-middle";
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
		return (getFirstName() == null || getFirstName().isEmpty() ? "" : getFirstName().trim().substring(0, 1).toUpperCase()) +
				(getMiddleName() == null || getMiddleName().isEmpty() ? "" : " " + getMiddleName().trim().substring(0, 1).toUpperCase()) + " " +
				Constants.toInitialCase(getLastName() == null || getLastName().isEmpty() ? "" : getLastName().trim());
	}
	
	/**
	 * 
	 * @return
	 */
	private String nameLastInit() {
		return Constants.toInitialCase(getLastName() == null || getLastName().isEmpty() ? "" : getLastName().trim()) + ", " +
				(getFirstName() == null || getFirstName().isEmpty() ? "" : getFirstName().trim().substring(0, 1).toUpperCase()) +
				(getMiddleName() == null || getMiddleName().isEmpty() ? "" : " " + getMiddleName().trim().substring(0, 1).toUpperCase());
	}

	/**
	 * 
	 * @return
	 */
	private String nameLastFirst() {
		return Constants.toInitialCase(
				(getLastName() == null || getLastName().isEmpty() ? "" : getLastName().trim()) + ", " + 
				(getFirstName() == null || getFirstName().isEmpty() ? "" : getFirstName().trim()));
	}
	
	public String nameFirstLast() {
		return Constants.toInitialCase(
				(getFirstName() == null || getFirstName().isEmpty() ? "" : getFirstName().trim()) + " " +
				(getLastName() == null || getLastName().isEmpty() ? "" : getLastName().trim()));
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
		if (sNameFormatLastFirstMiddle.equals(instructorNameFormat))
			return nameLastFirstMiddle();
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
	 * 
	 * @return
	 */
	private String nameLastFirstMiddle() {
		return (
				Constants.toInitialCase((this.getLastName() == null 
									? ""
				                    : this.getLastName().trim()) + 
				(this.getFirstName() == null && this.getMiddleName() == null
						            ?" "
						            :", ") +
                (this.getFirstName() == null
                					? "" 
                					: this.getFirstName().trim()) + " " + 
				(this.getMiddleName() == null 
									? "" 
				                    : this.getMiddleName().trim())
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
	public static List<DepartmentalInstructor> getAllForInstructor(DepartmentalInstructor di, Long sessionId) {
		if (di.getExternalUniqueId() == null || di.getExternalUniqueId().trim().isEmpty()) {
			ArrayList<DepartmentalInstructor> ret = new ArrayList<DepartmentalInstructor>(1);
			ret.add(di);
			return ret;
		}
		return (List<DepartmentalInstructor>)DepartmentalInstructorDAO.getInstance().getSession().createQuery(
						"from DepartmentalInstructor where externalUniqueId=:puid and " +
						"department.session.uniqueId=:sessionId")
				.setString("puid", di.getExternalUniqueId())
				.setLong("sessionId", sessionId)
				.setCacheable(true).list();
	}
	
	public static List<DepartmentalInstructor> getAllForInstructor(DepartmentalInstructor di) {
		return getAllForInstructor(di, di.getDepartment().getSessionId());
	}
	
	public static List<DepartmentalInstructor> findInstructorsForDepartment(Long departmentId) throws Exception {
		return (List<DepartmentalInstructor>) DepartmentalInstructorDAO.getInstance().getSession().createQuery(
				"from DepartmentalInstructor where department.uniqueId = :departmentId order by lastName, firstName, middleName")
				.setLong("departmentId", departmentId)
				.setCacheable(true)
				.list();
	}
	
	public static List<DepartmentalInstructor> findInstructorsForSession(Long sessionId) throws Exception {
		return (List<DepartmentalInstructor>) DepartmentalInstructorDAO.getInstance().getSession().createQuery(
				"from DepartmentalInstructor where department.session.uniqueId = :sessionId order by lastName, firstName, middleName")
				.setLong("sessionId", sessionId)
				.setCacheable(true)
				.list();
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
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(i.getUniqueId() == null ? -1 : i.getUniqueId());
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
		return(findByPuidDepartmentId(puid, deptId, (new DepartmentalInstructorDAO()).getSession()));
	}
	
	public static DepartmentalInstructor findByPuidDepartmentId(String puid, Long deptId, org.hibernate.Session hibSession) {
		try {
		return (DepartmentalInstructor)hibSession.
			createQuery("select d from DepartmentalInstructor d where d.externalUniqueId=:puid and d.department.uniqueId=:deptId").
			setString("puid", puid).
			setLong("deptId",deptId.longValue()).
			setCacheable(true).
			setFlushMode(FlushMode.MANUAL).
			uniqueResult();
		} catch (NonUniqueResultException e) {
			Debug.warning("There are two or more instructors with puid "+puid+" for department "+deptId+" -- returning the first one.");
			return (DepartmentalInstructor)hibSession.
				createQuery("select d from DepartmentalInstructor d where d.externalUniqueId=:puid and d.department.uniqueId=:deptId").
				setString("puid", puid).
				setLong("deptId",deptId.longValue()).
				setCacheable(true).
				setFlushMode(FlushMode.MANUAL).
				list().get(0);
		}
	}

	public DepartmentalInstructor findThisInstructorInSession(Long sessionId){
		return findThisInstructorInSession(sessionId, (new DepartmentalInstructorDAO()).getSession());
	}
	
	public DepartmentalInstructor findThisInstructorInSession(Long sessionId, org.hibernate.Session hibSession){
		Department newDept = this.getDepartment().findSameDepartmentInSession(sessionId, hibSession);
		if (newDept != null){
			return(findByPuidDepartmentId(this.getExternalUniqueId(), newDept.getUniqueId(), hibSession));
		}
		return(null);
	}

	public DepartmentalInstructor getNextDepartmentalInstructor(SessionContext context, Right right) throws Exception {
    	List instructors = DepartmentalInstructor.findInstructorsForDepartment(getDepartment().getUniqueId());
    	DepartmentalInstructor next = null;
    	for (Iterator i=instructors.iterator();i.hasNext();) {
    		DepartmentalInstructor di = (DepartmentalInstructor)i.next();
    		if (right != null && !context.hasPermission(di, right)) continue;
    		if (this.compareTo(di)>=0) continue;
    		if (next==null || next.compareTo(di)>0)
    			next = di;
    	}
    	return next;
    }
	
    public DepartmentalInstructor getPreviousDepartmentalInstructor(SessionContext context, Right right) throws Exception {
    	List instructors = DepartmentalInstructor.findInstructorsForDepartment(getDepartment().getUniqueId());
    	DepartmentalInstructor prev = null;
    	for (Iterator i=instructors.iterator();i.hasNext();) {
    		DepartmentalInstructor di = (DepartmentalInstructor)i.next();
    		if (right != null && !context.hasPermission(di, right)) continue;
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
		newDepartmentalInstructor.setRole(getRole());
		return(newDepartmentalInstructor);
	}
	
    public static List findAllExamInstructors(Long sessionId, Long examTypeId) {
        return (new DepartmentalInstructorDAO()).getSession()
                .createQuery("select distinct i from Exam x inner join x.instructors i where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId")
                .setLong("sessionId", sessionId)
                .setLong("examTypeId", examTypeId)
                .setCacheable(true).list();
    }
    
    public List<Exam> getExams(Integer examType) {
        if (getExternalUniqueId()!=null) {
            return (new DepartmentalInstructorDAO()).getSession()
                .createQuery("select distinct x from Exam x inner join x.instructors i where " +
                		"(i.uniqueId=:instructorId or (i.externalUniqueId=:externalId and i.department.session.uniqueId=:sessionId)) " +
                		"and x.examType.type=:examType")
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
    
    public List<Exam> getExams(ExamType examType) {
        if (getExternalUniqueId()!=null) {
            return (new DepartmentalInstructorDAO()).getSession()
                .createQuery("select distinct x from Exam x inner join x.instructors i where " +
                		"(i.uniqueId=:instructorId or (i.externalUniqueId=:externalId and i.department.session.uniqueId=:sessionId)) " +
                		"and x.examType.uniqueId=:examTypeId")
                .setLong("instructorId", getUniqueId())
                .setLong("sessionId", getDepartment().getSession().getUniqueId())
                .setString("externalId", getExternalUniqueId())
                .setLong("examTypeId", examType.getUniqueId())
                .setCacheable(true).list();
        } else {
            return (new DepartmentalInstructorDAO()).getSession()
            .createQuery("select distinct x from Exam x inner join x.instructors i where i.uniqueId=:instructorId and x.examType.uniqueId=:examTypeId")
            .setLong("instructorId", getUniqueId())
            .setLong("examTypeId", examType.getUniqueId())
            .setCacheable(true).list();
            
        }
    }
    
    public Collection<Assignment> getCommitedAssignments() {
    	return new DepartmentalInstructorDAO().getSession().createQuery(
                "select a from Assignment a inner join a.instructors i where " +
                "a.solution.commited=true and i.uniqueId=:instructorId")
                .setLong("instructorId", getUniqueId())
                .setCacheable(true).list();
    }

    @Override
    public Session getSession() {
    	return getDepartment().getSession();
    }
    
    public static boolean canLookupInstructor() {
    	return ApplicationProperties.getProperty("tmtbl.instructor.external_id.lookup.class") != null;
    }
    
    public static UserInfo lookupInstructor(String externalId) throws Exception {
    	ExternalUidLookup lookup = null;
        String className = ApplicationProperties.getProperty("tmtbl.instructor.external_id.lookup.class");
        if (className != null)
        	lookup = (ExternalUidLookup)Class.forName(className).newInstance();
        return (lookup == null ? null : lookup.doLookup(externalId));
    }
}
