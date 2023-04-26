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

import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.cpsolver.coursett.model.TimeLocation;
import org.hibernate.FlushMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.ObjectNotFoundException;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.interfaces.ExternalUidLookup;
import org.unitime.timetable.interfaces.ExternalUidLookup.UserInfo;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.base.BaseDepartmentalInstructor;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.NameInterface;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Entity
@Table(name = "departmental_instructor")
public class DepartmentalInstructor extends BaseDepartmentalInstructor implements Comparable, NameInterface {
	private static final long serialVersionUID = 1L;
	protected static GwtConstants CONS = Localization.create(GwtConstants.class);

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
	@Transient
	public String getNameFirst() {
		return nameFirstNameFirst();
	}

	/**
	 * Property nameLast used in JSPs
	 * Gets full name with last name first 
	 * @return
	 */
	@Transient
	public String getNameLast() {
		return nameLastNameFirst();
	}

	/**
	 * 
	 * @return
	 */
	@Transient
	public String getNameLastFirst() {
		return nameLastFirst();
	}

	/**
	 * 
	 * @return
	 */
	@Transient
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
	
	@Transient
    public Set<Location> getAvailableRooms() {
    	return new TreeSet<Location>(DepartmentalInstructorDAO.getInstance().getSession().createQuery(
    			"select distinct r from RoomDept rd inner join rd.room r inner join rd.department d " +
    			"where r.session.uniqueId = :sessionId and (d.uniqueId = :deptId or (d.externalManager = true and d.inheritInstructorPreferences = true))", Location.class
    			).setParameter("sessionId", getDepartment().getSessionId())
    			.setParameter("deptId", getDepartment().getUniqueId()).setCacheable(true).list());
    }
    
	@Transient
    public Set<Building> getAvailableBuildings() {
    	return new TreeSet<Building>(DepartmentalInstructorDAO.getInstance().getSession().createQuery(
    			"select distinct r.building from Room r inner join r.roomDepts rd inner join rd.department d " +
    			"where r.session.uniqueId = :sessionId and (d.uniqueId = :deptId or (d.externalManager = true and d.inheritInstructorPreferences = true))", Building.class
    			).setParameter("sessionId", getDepartment().getSessionId())
    			.setParameter("deptId", getDepartment().getUniqueId()).setCacheable(true).list());
    }
    
	@Transient
    public Set<RoomFeature> getAvailableRoomFeatures() {
    	Set<RoomFeature> features = super.getAvailableRoomFeatures();
    	features.addAll((DepartmentRoomFeature.getAllDepartmentRoomFeatures(getDepartment())));
    	return features;
    }
    
	@Transient
    public Set<RoomGroup> getAvailableRoomGroups() {
    	Set<RoomGroup> groups = super.getAvailableRoomGroups();
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
		return DepartmentalInstructorDAO.getInstance().getSession().createQuery(
						"from DepartmentalInstructor where externalUniqueId=:puid and " +
						"department.session.uniqueId=:sessionId", DepartmentalInstructor.class)
				.setParameter("puid", di.getExternalUniqueId())
				.setParameter("sessionId", sessionId)
				.setCacheable(true).list();
	}
	
	public static List<DepartmentalInstructor> getAllForInstructor(DepartmentalInstructor di) {
		return getAllForInstructor(di, di.getDepartment().getSessionId());
	}
	
	public static List<DepartmentalInstructor> findInstructorsForDepartment(Long departmentId) {
		return  DepartmentalInstructorDAO.getInstance().getSession().createQuery(
				"from DepartmentalInstructor where department.uniqueId = :departmentId order by lastName, firstName, middleName", DepartmentalInstructor.class)
				.setParameter("departmentId", departmentId)
				.setCacheable(true)
				.list();
	}
	
	public static List<DepartmentalInstructor> findInstructorsForSession(Long sessionId) {
		return  DepartmentalInstructorDAO.getInstance().getSession().createQuery(
				"from DepartmentalInstructor where department.session.uniqueId = :sessionId order by lastName, firstName, middleName", DepartmentalInstructor.class)
				.setParameter("sessionId", sessionId)
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
		return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(i.getUniqueId() == null ? -1 : i.getUniqueId());
	}

	/**
	 * 
	 * @param puid
	 * @return
	 */
	public static boolean existInst(String puid) {
		if (puid == null) return false;
		return (DepartmentalInstructorDAO.getInstance().getSession().createQuery(
				"from DepartmentalInstructor where externalUniqueId = :puid", DepartmentalInstructor.class
				).setParameter("puid", puid).setMaxResults(1).uniqueResult() != null);
	}
	
	public static DepartmentalInstructor findByPuidDepartmentId(String puid, Long deptId) {
		return(findByPuidDepartmentId(puid, deptId, (DepartmentalInstructorDAO.getInstance()).getSession()));
	}
	
	public static DepartmentalInstructor findByPuidDepartmentId(String puid, Long deptId, org.hibernate.Session hibSession) {
		try {
		return hibSession.
			createQuery("select d from DepartmentalInstructor d where d.externalUniqueId=:puid and d.department.uniqueId=:deptId", DepartmentalInstructor.class).
			setParameter("puid", puid).
			setParameter("deptId", deptId.longValue()).
			setCacheable(true).
			setHibernateFlushMode(FlushMode.MANUAL).
			uniqueResult();
		} catch (NonUniqueResultException e) {
			Debug.warning("There are two or more instructors with puid "+puid+" for department "+deptId+" -- returning the first one.");
			return hibSession.
				createQuery("select d from DepartmentalInstructor d where d.externalUniqueId=:puid and d.department.uniqueId=:deptId", DepartmentalInstructor.class).
				setParameter("puid", puid).
				setParameter("deptId", deptId.longValue()).
				setCacheable(true).
				setHibernateFlushMode(FlushMode.MANUAL).
				list().get(0);
		}
	}

	public DepartmentalInstructor findThisInstructorInSession(Long sessionId){
		return findThisInstructorInSession(sessionId, (DepartmentalInstructorDAO.getInstance()).getSession());
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
                if (timePref.getPreference() == null || timePref.getPreference().matches("2*")) continue;
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
	
    public static List<DepartmentalInstructor> findAllExamInstructors(Long sessionId, Long examTypeId) {
        return (DepartmentalInstructorDAO.getInstance()).getSession()
                .createQuery("select distinct i from Exam x inner join x.instructors i where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId", DepartmentalInstructor.class)
                .setParameter("sessionId", sessionId)
                .setParameter("examTypeId", examTypeId)
                .setCacheable(true).list();
    }
    
    public List<Exam> getExams(Integer examType) {
        if (getExternalUniqueId()!=null) {
            return (DepartmentalInstructorDAO.getInstance()).getSession()
                .createQuery("select distinct x from Exam x inner join x.instructors i where " +
                		"(i.uniqueId=:instructorId or (i.externalUniqueId=:externalId and i.department.session.uniqueId=:sessionId)) " +
                		"and x.examType.type=:examType", Exam.class)
                .setParameter("instructorId", getUniqueId())
                .setParameter("sessionId", getDepartment().getSession().getUniqueId())
                .setParameter("externalId", getExternalUniqueId())
                .setParameter("examType", examType)
                .setCacheable(true).list();
        } else {
            return (DepartmentalInstructorDAO.getInstance()).getSession()
            .createQuery("select distinct x from Exam x inner join x.instructors i where i.uniqueId=:instructorId and x.examType=:examType", Exam.class)
            .setParameter("instructorId", getUniqueId())
            .setParameter("examType", examType)
            .setCacheable(true).list();
            
        }
    }
    
    public List<Exam> getExams(ExamType examType) {
        if (getExternalUniqueId()!=null) {
            return (DepartmentalInstructorDAO.getInstance()).getSession()
                .createQuery("select distinct x from Exam x inner join x.instructors i where " +
                		"(i.uniqueId=:instructorId or (i.externalUniqueId=:externalId and i.department.session.uniqueId=:sessionId)) " +
                		"and x.examType.uniqueId=:examTypeId", Exam.class)
                .setParameter("instructorId", getUniqueId())
                .setParameter("sessionId", getDepartment().getSession().getUniqueId())
                .setParameter("externalId", getExternalUniqueId())
                .setParameter("examTypeId", examType.getUniqueId())
                .setCacheable(true).list();
        } else {
            return (DepartmentalInstructorDAO.getInstance()).getSession()
            .createQuery("select distinct x from Exam x inner join x.instructors i where i.uniqueId=:instructorId and x.examType.uniqueId=:examTypeId", Exam.class)
            .setParameter("instructorId", getUniqueId())
            .setParameter("examTypeId", examType.getUniqueId())
            .setCacheable(true).list();
            
        }
    }
    
	@Transient
    public List<Exam> getAllExams() {
        if (getExternalUniqueId()!=null) {
            return (DepartmentalInstructorDAO.getInstance()).getSession()
                .createQuery("select distinct x from Exam x inner join x.instructors i where " +
                		"(i.uniqueId=:instructorId or (i.externalUniqueId=:externalId and i.department.session.uniqueId=:sessionId))", Exam.class)
                .setParameter("instructorId", getUniqueId())
                .setParameter("sessionId", getDepartment().getSession().getUniqueId())
                .setParameter("externalId", getExternalUniqueId())
                .setCacheable(true).list();
        } else {
            return (DepartmentalInstructorDAO.getInstance()).getSession()
            .createQuery("select distinct x from Exam x inner join x.instructors i where i.uniqueId=:instructorId", Exam.class)
            .setParameter("instructorId", getUniqueId())
            .setCacheable(true).list();
            
        }
    }
    
	@Transient
    public Collection<Assignment> getCommitedAssignments() {
    	return DepartmentalInstructorDAO.getInstance().getSession().createQuery(
                "select a from Assignment a inner join a.instructors i where " +
                "a.solution.commited=true and i.uniqueId=:instructorId", Assignment.class)
                .setParameter("instructorId", getUniqueId())
                .setCacheable(true).list();
    }

    @Override
	@Transient
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
        	lookup = (ExternalUidLookup)Class.forName(className).getDeclaredConstructor().newInstance();
        return (lookup == null ? null : lookup.doLookup(externalId));
    }
    
    public Set<InstructorAttribute> getAttributes(InstructorAttributeType type) {
    	Set<InstructorAttribute> ret = new TreeSet<InstructorAttribute>();
    	for (InstructorAttribute a: getAttributes()) {
    		if (type.equals(a.getType())) ret.add(a);
    	}
    	return ret;
    }
    
	@Transient
    public Set<CourseOffering> getAvailableCourses() {
    	return new TreeSet<CourseOffering>(
    			DepartmentalInstructorDAO.getInstance().getSession().createQuery(
    					"from CourseOffering c where c.subjectArea.department.uniqueId = :departmentId and c.isControl = true and c.instructionalOffering.notOffered = false", CourseOffering.class
    				).setParameter("departmentId", getDepartment().getUniqueId()).setCacheable(true).list()
    			);
    }
    
	@Transient
	public Set getAvailableAttributeTypes() {
		return getDepartment().getAvailableAttributeTypes();
    }

	@Transient
	public Set getAvailableAttributes() {
		return getDepartment().getAvailableAttributes();
    }
	
	public boolean hasUnavailabilities() {
		if (getUnavailableDays() == null || getUnavailableDays().isEmpty()) return false;
		return getUnavailableDays().indexOf('1') >= 0;
	}
	
	public boolean isUnavailable(int day, int month) {
		if (getUnavailableDays()==null || getUnavailableDays().isEmpty()) return false;
		int idx = getSession().getDayOfYear(day, month)-getUnavailablePatternOffset();
		if (idx<0 || idx>=getUnavailableDays().length()) return false;
		return (getUnavailableDays().charAt(idx)=='1');
	}
	
	public List<TimeBlock> listUnavailableDays() {
		if (!hasUnavailabilities()) return null;
		List<TimeBlock> ret = new ArrayList<TimeBlock>();
		int i = -1;
		Calendar cal = Calendar.getInstance(Locale.US);
		Date start = getUnavailableStartDate();
		while ((i = getUnavailableDays().indexOf('1', i + 1)) >= 0) {
			cal.setTime(start);
			cal.add(Calendar.DAY_OF_YEAR, i);
			ret.add(new UnavailableDay(this, cal.getTime()));
		}
		return ret;
	}
	
	public List<TimeLocation> listUnavailableTimes() {
		if (getUnavailableDays()==null || getUnavailableDays().isEmpty()) return null;
        List<TimeLocation> ret = new ArrayList<TimeLocation>();
        for (int i = 0; i < 7; i++) {
        	BitSet weekCode = getUnavailableBitSet(i);
        	if (!weekCode.isEmpty()) {
        		String name = "";
        		int idx = -1;
        		Calendar cal = Calendar.getInstance(Locale.US);
        		Date start = DateUtils.getDate(1, getSession().getPatternStartMonth(), getSession().getSessionStartYear());
        		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_SHORT);
                while ((idx = weekCode.nextSetBit(1 + idx)) >= 0) {
                	cal.setTime(start);
        			cal.add(Calendar.DAY_OF_YEAR, idx);
        			name += (name.isEmpty() ? "" : ", ") + df.format(cal.getTime());
                }
        		ret.add(new TimeLocation(Constants.DAY_CODES[i], 0, 288, 0, 0.0, null, name, weekCode, 0));
        	}
        }
        return ret;
	}
	
	public BitSet getUnavailableBitSet(int targetDow) {
		if (getUnavailableDays()==null || getUnavailableOffset()==null) return null;
		int startMonth = getSession().getPatternStartMonth();
		int endMonth = getSession().getPatternEndMonth();
		int size = getSession().getDayOfYear(0,endMonth+1)-getSession().getDayOfYear(1,startMonth);
		BitSet ret = new BitSet(size);
		int offset = getUnavailablePatternOffset() - getSession().getDayOfYear(1,startMonth);
		int dayOfWeekOffset = Constants.getDayOfWeek(DateUtils.getDate(1, getSession().getPatternStartMonth(), getSession().getSessionStartYear()));		
		for (int i=0;i<getUnavailableDays().length();i++) {
			int dow = (i + offset + dayOfWeekOffset) % 7;
			if (getUnavailableDays().charAt(i)=='1' && i+offset >= 0 && dow == targetDow)
				ret.set(i+offset);
		}
		return ret;
	}
	
	public static List<DepartmentalInstructor> getUserInstructors(UserContext user) {
		if (user == null || user.getCurrentAcademicSessionId() == null || user.getExternalUserId() == null) return null;
		return DepartmentalInstructorDAO.getInstance().getSession().createQuery(
				"from DepartmentalInstructor i where i.department.session.uniqueId = :sessionId and i.externalUniqueId = :externalId " +
				"order by i.department.deptCode", DepartmentalInstructor.class)
				.setParameter("sessionId", user.getCurrentAcademicSessionId())
				.setParameter("externalId", user.getExternalUserId()).setCacheable(true).list();
	}
	
	@Transient
	public int getUnavailablePatternOffset() {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(getSession().getSessionBeginDateTime());
		int beginDate = cal.get(Calendar.DAY_OF_YEAR);
		return beginDate-(getUnavailableOffset()==null?0:getUnavailableOffset().intValue())-1;
	}
	
	@Transient
	public String getUnavailablePatternArray() {
		StringBuffer sb = new StringBuffer("[");
		int startMonth = getSession().getPatternStartMonth();
		int endMonth = getSession().getPatternEndMonth();
		int year = getSession().getSessionStartYear();
		for (int m=startMonth;m<=endMonth;m++) {
			if (m!=startMonth) sb.append(",");
			sb.append("[");
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
			for (int d=1;d<=daysOfMonth;d++) {
				if (d>1) sb.append(",");
				sb.append(isUnavailable(d,m)?"'1'":"'0'");
			}
			sb.append("]");
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Transient
	public String getUnavailableBorderArray() {
		int startMonth = getSession().getPatternStartMonth();
		int endMonth = getSession().getPatternEndMonth();
		int year = getSession().getSessionStartYear();
		StringBuffer sb = new StringBuffer("[");
		for (int m=startMonth;m<=endMonth;m++) {
			if (m!=startMonth) sb.append(",");
			sb.append("[");
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
			for (int d=1;d<=daysOfMonth;d++) {
				if (d>1) sb.append(",");
				String border = getSession().getBorder(d,m);
				sb.append(border);
			}
			sb.append("]");
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Transient
	public String getUnavailablePatternHtml() {
		return getUnavailablePatternHtml(true, true);
	}
	
	public String getUnavailablePatternHtml(boolean editable) {
		return getUnavailablePatternHtml(editable, true);
	}
    
	public String getUnavailablePatternHtml(boolean editable, boolean includeScript) {
		StringBuffer sb = new StringBuffer(); 
        if (includeScript)
            sb.append("<script language='JavaScript' type='text/javascript' src='scripts/datepatt.js'></script>\n");
		sb.append("<script language='JavaScript'>\n");
		sb.append("var CAL_WEEKDAYS = [");
		for (int i = 0; i < 7; i++) {
			if (i > 0) sb.append(", ");
			sb.append("\"" + CONS.days()[(i + 6) % 7] + "\"");
		}
		sb.append("];\n");
		sb.append("var CAL_MONTHS = [");
		for (int i = 0; i < 12; i++) {
			if (i > 0) sb.append(", ");
			sb.append("\"" + Month.of(1 + i).getDisplayName(TextStyle.FULL_STANDALONE, Localization.getJavaLocale()) + "\"");
		}
		sb.append("];\n");
		sb.append(
			"calGenerate2("+getSession().getSessionStartYear()+",\n\t"+
				(getSession().getPatternStartMonth()) +",\n\t"+
				(getSession().getPatternEndMonth())+",\n\t"+
				getUnavailablePatternArray()+",\n\t"+
				"['1','0'],\n\t"+
				"['" + MSG.dateNotAvailable() + "','" + MSG.dateAvailable() + "'],\n\t"+
				"['rgb(150,150,150)','rgb(240,240,240)'],\n\t"+
				"'1',\n\t"+
				getUnavailableBorderArray()+","+getSession().getColorArray()+","+editable+","+editable+");\n");
		sb.append("</script>\n");
		return sb.toString();
	}
	
	public void setUnavailablePatternAndOffset(HttpServletRequest request) {
		int startMonth = getSession().getPatternStartMonth();
		int endMonth = getSession().getPatternEndMonth();
		int firstOne = 0, lastOne = 0;
		int year = getSession().getSessionStartYear();
		StringBuffer sb = null;
		int idx = getSession().getDayOfYear(1,startMonth);
		for (int m=startMonth;m<=endMonth;m++) {
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
			int yr = DateUtils.calculateActualYear(m, year);
			for (int d=1;d<=daysOfMonth;d++) {
				String unavailable = request.getParameter("cal_val_"+yr+"_"+((12+m)%12)+"_"+d);
				if (unavailable!=null) {
					if (sb!=null || !unavailable.equals("0")) {
						if (sb==null) {
							firstOne = idx; sb = new StringBuffer();
						}
						sb.append(unavailable);
					}
					if (!unavailable.equals("0")) lastOne=idx;
				}
				idx++;
			}
		}
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(getSession().getSessionBeginDateTime());
		if (sb!=null) {
			setUnavailableDays(sb.substring(0,lastOne-firstOne+1));
			setUnavailableOffset(Integer.valueOf(cal.get(Calendar.DAY_OF_YEAR)-firstOne-1));
		} else {
			setUnavailableDays(null); setUnavailableOffset(null);
		}
	}
	
	@Transient
	public Date getUnavailableStartDate() {
		if (getUnavailableDays()==null || getUnavailableOffset()==null) return null;
		int idx = getUnavailableDays().indexOf('1');
		if (idx<0) return getSession().getSessionBeginDateTime();
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(getSession().getSessionBeginDateTime());
		cal.add(Calendar.DAY_OF_YEAR, idx - getUnavailableOffset().intValue());
		return cal.getTime();
	}

	@Transient
	public Date getUnavailableEndDate() {
		if (getUnavailableDays()==null || getUnavailableOffset()==null) return null;
		int idx = getUnavailableDays().lastIndexOf('1');
		if (idx<0) return getSession().getSessionEndDateTime();
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(getSession().getSessionBeginDateTime());
		cal.add(Calendar.DAY_OF_YEAR, idx - getUnavailableOffset().intValue());
		return cal.getTime();
	}
	
	@Transient
	public Map<Date, Date> getUnavailablePatternDateStringHashMaps() {
		Calendar startDate = Calendar.getInstance(Locale.US);
		startDate.setTime(getUnavailableStartDate());
		Calendar endDate = Calendar.getInstance(Locale.US);
		endDate.setTime(getUnavailableEndDate());

		int startMonth = startDate.get(Calendar.MONTH);
		int endMonth = endDate.get(Calendar.MONTH);
		int startYear = startDate.get(Calendar.YEAR);
		int endYear = endDate.get(Calendar.YEAR);
		if (endYear > startYear){
			endMonth += (12 * (endYear - startYear));
		}
		
		Map<Date, Date> mapStartToEndDate = new HashMap<Date, Date>();
		Date first = null, previous = null;
		char[] ptrn = getUnavailableDays().toCharArray();
		int charPosition = 0;
		int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK);
		Calendar cal = Calendar.getInstance(Locale.US);

		for (int m=startMonth;m<=endMonth;m++) {
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, startYear);
			int d;
			if (m == startMonth){
				d = startDate.get(Calendar.DAY_OF_MONTH);
			} else {
				d = 1;
			}
			for (;d<=daysOfMonth && charPosition < ptrn.length ;d++) {
				if (ptrn[charPosition] == '1' || (first != null && dayOfWeek == Calendar.SUNDAY && charPosition + 1 < ptrn.length && ptrn[1 + charPosition] == '1')) {
					if (first==null){
						//first = ((m<0?12+m:m%12)+1)+"/"+d+"/"+((m>=12)?startYear+1:startYear);
						cal.setTime(getUnavailableStartDate());
						cal.add(Calendar.DAY_OF_YEAR, charPosition);
						first = cal.getTime();
					}
				} else {
					if (first!=null) {
						mapStartToEndDate.put(first, previous);
						first=null;
					}
				}
				//previous = ((m<0?12+m:m%12)+1)+"/"+d+"/"+((m>=12)?startYear+1:startYear);
				cal.setTime(getUnavailableStartDate());
				cal.add(Calendar.DAY_OF_YEAR, charPosition);
				previous = cal.getTime();
				
				charPosition++;
				dayOfWeek++;
				if (dayOfWeek > Calendar.SATURDAY){
					dayOfWeek = Calendar.SUNDAY;
				}
			}
		}
		if (first!=null) {
			mapStartToEndDate.put(first, previous);
			first=null;
		}
		return(mapStartToEndDate);
	}
	
	public String getUnavailableDaysText(String separator) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		Map<Date, Date> dates = getUnavailablePatternDateStringHashMaps();
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_SHORT);
		for (Date startDate: new TreeSet<Date>(dates.keySet())) {
			Date endDate = dates.get(startDate);
			String startDateStr = df.format(startDate);
			String endDateStr = df.format(endDate);
			if (first){
				first = false;
			} else {
				sb.append(separator);
			}
			sb.append(startDateStr);
			if (!startDateStr.equals(endDateStr)) {
				sb.append("-" + endDateStr);
			}
		}
		return sb.toString();
	}
	
	public static class UnavailableDay implements TimeBlock, Comparable<TimeBlock> {
		private static final long serialVersionUID = 1L;
		private Date iDate;
		private Long iEventId;
		private UnavailableDay(DepartmentalInstructor instructor, Date date) {
			iEventId = - instructor.getUniqueId();
			iDate = date;
		}
		@Override
		public Long getEventId() { return iEventId; }
		@Override
		public String getEventName() { return MSG.instructorNotAvailableName(); }
		@Override
		public String getEventType() { return MSG.instructorNotAvailableType(); }
		@Override
		public Date getStartTime() { return iDate; }
		@Override
		public Date getEndTime() { return new Date(iDate.getTime() + 86400000l); }
		@Override
		public int compareTo(TimeBlock block) {
            int cmp = getStartTime().compareTo(block.getStartTime());
            if (cmp!=0) return cmp;
            cmp = getEndTime().compareTo(block.getEndTime());
            if (cmp!=0) return cmp;
            return getEventName().compareTo(block.getEventName());
        }
		@Override
		public String toString() {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy");
            return getEventName()+" ("+getEventType()+") "+df.format(getStartTime());
        }
	}
}
