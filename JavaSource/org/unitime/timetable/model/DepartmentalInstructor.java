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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.FlushMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalUidLookup;
import org.unitime.timetable.interfaces.ExternalUidLookup.UserInfo;
import org.unitime.timetable.model.base.BaseDepartmentalInstructor;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.NameInterface;




/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class DepartmentalInstructor extends BaseDepartmentalInstructor implements Comparable, NameInterface {
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
	public static final String sNameFormatLastFist = NameFormat.LAST_FIRST.reference();
	public static final String sNameFormatFirstLast = NameFormat.FIRST_LAST.reference();
	public static final String sNameFormatInitialLast = NameFormat.INITIAL_LAST.reference();
	public static final String sNameFormatLastInitial = NameFormat.LAST_INITIAL.reference();
	public static final String sNameFormatFirstMiddleLast = NameFormat.FIRST_MIDDLE_LAST.reference();
	public static final String sNameFormatLastFirstMiddle = NameFormat.LAST_FIRST_MIDDLE.reference();
	public static final String sNameFormatShort = NameFormat.SHORT.reference();
	

	/**
	 * 
	 * @return
	 */
	public String nameLastNameFirst() {
		return nameLastFirstMiddle();
	}

	/**
	 * 
	 * @return
	 */
	public String nameFirstNameFirst() {
		return Constants.toInitialCase(
				(hasFirstName() ? getFirstName() : "") +
				(hasMiddleName() ? " " + getMiddleName() : "") +
				(hasLastName() ? " " + getLastName() : "")).trim();
	}
	
	public String nameShort() {
		return (hasFirstName() ? getFirstName().substring(0, 1).toUpperCase() + ". " : "") +
				(hasLastName() ? getLastName().substring(0, 1).toUpperCase() + getLastName().substring(1, Math.min(10, getLastName().length())).toLowerCase() : "").trim();
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
		return ((hasFirstName() ? getFirstName().trim().substring(0, 1).toUpperCase() : "") +
				(hasMiddleName() ? " " + getMiddleName().trim().substring(0, 1).toUpperCase() : "") +
				(hasLastName() ? " " + Constants.toInitialCase(getLastName()) : "")).trim();
	}
	
	/**
	 * 
	 * @return
	 */
	private String nameLastFirst() {
		return Constants.toInitialCase(
				(hasLastName() ? getLastName() : "") +
				(hasFirstName() ? ", " + getFirstName() : "")).trim();
	}
	
	public String nameFirstLast() {
		return Constants.toInitialCase(
				(hasFirstName() ? getFirstName() : "") +
				(hasLastName() ? " " + getLastName() : "")).trim();
	}
	
	public String getName(String instructorNameFormat) {
		return NameFormat.fromReference(instructorNameFormat).format(this);
	}

	public boolean hasLastName() {
		return getLastName() != null && !getLastName().isEmpty();
	}
	
	public boolean hasFirstName() {
		return getFirstName() != null && !getFirstName().isEmpty();
	}
	
	public boolean hasMiddleName() {
		return getMiddleName() != null && !getMiddleName().isEmpty();
	}

	private String nameLastFirstMiddle() {
		return Constants.toInitialCase(
				(hasLastName() ? getLastName() : "") +
				(hasFirstName() || hasMiddleName() ? "," : "") +
				(hasFirstName() ? " " + getFirstName() : "") +
				(hasMiddleName() ? " " + getMiddleName() : "")
				).trim();
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
			ret.add(DepartmentalInstructorDAO.getInstance().get(di.getUniqueId()));
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
    		if (right != null && !context.hasPermission(Department.class.equals(right.type()) ? di.getDepartment() : di, right)) continue;
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
    		if (right != null && !context.hasPermission(Department.class.equals(right.type()) ? di.getDepartment() : di, right)) continue;
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
    	Iterator i = null;
    	try {
    		i = getPreferences().iterator();
    	} catch (ObjectNotFoundException e) {
    		Debug.error("Exception "+e.getMessage()+" seen for "+this);
    		new _RootDAO().getSession().refresh(this);
    		if (getPreferences() != null)
    			i = getPreferences().iterator();
    		else
    			i = null;
    	} catch (Exception e){
    		i = null;
    	}
    	if (i == null) return false;
        while (i.hasNext()) {
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
		newDepartmentalInstructor.setAcademicTitle(getAcademicTitle());
		newDepartmentalInstructor.setIgnoreToFar(isIgnoreToFar());
		newDepartmentalInstructor.setNote(getNote());
		newDepartmentalInstructor.setPositionType(getPositionType());
		newDepartmentalInstructor.setEmail(getEmail());
		newDepartmentalInstructor.setRole(getRole());
		newDepartmentalInstructor.setTeachingPreference(getTeachingPreference());
		newDepartmentalInstructor.setMaxLoad(getMaxLoad());
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
    
    public List<Exam> getAllExams() {
        if (getExternalUniqueId()!=null) {
            return (new DepartmentalInstructorDAO()).getSession()
                .createQuery("select distinct x from Exam x inner join x.instructors i where " +
                		"(i.uniqueId=:instructorId or (i.externalUniqueId=:externalId and i.department.session.uniqueId=:sessionId))")
                .setLong("instructorId", getUniqueId())
                .setLong("sessionId", getDepartment().getSession().getUniqueId())
                .setString("externalId", getExternalUniqueId())
                .setCacheable(true).list();
        } else {
            return (new DepartmentalInstructorDAO()).getSession()
            .createQuery("select distinct x from Exam x inner join x.instructors i where i.uniqueId=:instructorId")
            .setLong("instructorId", getUniqueId())
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
    	return ApplicationProperty.InstructorExternalIdLookupClass.value() != null;
    }
    
    public static UserInfo lookupInstructor(String externalId) throws Exception {
    	ExternalUidLookup lookup = null;
        String className = ApplicationProperty.InstructorExternalIdLookupClass.value();
        if (className != null)
        	lookup = (ExternalUidLookup)Class.forName(className).newInstance();
        return (lookup == null ? null : lookup.doLookup(externalId));
    }
    
    public Set<InstructorAttribute> getAttributes(InstructorAttributeType type) {
    	Set<InstructorAttribute> ret = new TreeSet<InstructorAttribute>();
    	for (InstructorAttribute a: getAttributes()) {
    		if (type.equals(a.getType())) ret.add(a);
    	}
    	return ret;
    }
    
    public Set<CourseOffering> getAvailableCourses() {
    	return new TreeSet<CourseOffering>(
    			DepartmentalInstructorDAO.getInstance().getSession().createQuery(
    					"from CourseOffering c where c.subjectArea.department.uniqueId = :departmentId and c.isControl = true and c.instructionalOffering.notOffered = false"
    				).setLong("departmentId", getDepartment().getUniqueId()).setCacheable(true).list()
    			);
    }
    
	public Set getAvailableAttributeTypes() {
		return getDepartment().getAvailableAttributeTypes();
    }

	public Set getAvailableAttributes() {
		return getDepartment().getAvailableAttributes();
    }
	
	@Override
	public boolean isInstructorAssignmentNeeded() { return true; }
}
