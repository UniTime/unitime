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
package org.unitime.timetable.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.query.Query;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditFormat;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.comparators.DepartmentalInstructorComparator;
import org.unitime.timetable.model.dao.CourseTypeDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;


/**
 * Contains methods on static read-only lookup tables
 * 
 * @author Heston Fernandes, Tomas Muller
 */
public class LookupTables {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
    
    /**
     * Get Itypes and store it in request object
     * @param request
     * @throws Exception
     */
    public static void setupItypes(HttpServletRequest request, boolean basic) {
        request.setAttribute(ItypeDesc.ITYPE_ATTR_NAME, ItypeDesc.findAll(basic));
    }
 
    /**
     * Get ExternalDepts and store it in request object
     * @param request
     * @throws Exception
     */
    public static void setupExternalDepts(HttpServletRequest request, Long sessionId) throws Exception {
    	request.setAttribute(Department.EXTERNAL_DEPT_ATTR_NAME, Department.findAllExternal(sessionId));
    }

    /**
     * Get all departments that are not external and store it in request object
     * @param request
     * @throws Exception
     */
    public static void setupNonExternalDepts(HttpServletRequest request, Long sessionId) throws Exception {
        request.setAttribute(Department.DEPT_ATTR_NAME, Department.findAllNonExternal(sessionId));
    }

    /**
     * Get All Depts and store it in request object
     * @param request
     * @throws Exception
     */
    public static void setupDepts(HttpServletRequest request, Long sessionId) throws Exception {
        request.setAttribute(Department.DEPT_ATTR_NAME, Department.findAll(sessionId));
    }

    public static void setupDepartments(HttpServletRequest request, SessionContext context, boolean includeExternal) throws Exception {
    	TreeSet<Department> departments = Department.getUserDepartments(context.getUser());
    	if (includeExternal)
    		departments.addAll(Department.findAllExternal(context.getUser().getCurrentAcademicSessionId()));
    	
		List<ComboBoxLookup> deptList = new ArrayList<ComboBoxLookup>();
		for (Department d: departments) {
			String code = d.getDeptCode().trim();
			String abbv = d.getName().trim();
			if (d.isExternalManager())
				deptList.add(new ComboBoxLookup(code + " - " + abbv + " (" + d.getExternalMgrLabel() + ")", code));
			else	
				deptList.add(new ComboBoxLookup(code + " - " + abbv, code)); 
		}
		request.setAttribute(Department.DEPT_ATTR_NAME, deptList);
    }

    public static void setupRooms(HttpServletRequest request, PreferenceGroup pg) throws Exception {
    	request.setAttribute(Room.ROOM_LIST_ATTR_NAME, pg.getAvailableRooms());
    	if (pg instanceof Class_) {
    		Class_ clazz = (Class_)pg;
    		if (clazz.getNbrRooms() > 1) {
    			List<ComboBoxLookup> indexes = new ArrayList<ComboBoxLookup>();
    			indexes.add(new ComboBoxLookup(MSG.itemAllRooms(), "-"));
    			for (int i = 0; i < clazz.getNbrRooms(); i++) {
    				indexes.add(new ComboBoxLookup(MSG.itemOnlyRoom(i + 1), String.valueOf(i)));
    			}
    			request.setAttribute("roomIndexes", indexes);
    		}
    	} else if (pg instanceof SchedulingSubpart) {
    		SchedulingSubpart subpart = (SchedulingSubpart)pg;
    		int maxRooms = subpart.getMaxRooms();
    		if (maxRooms > 1) {
    			List<ComboBoxLookup> indexes = new ArrayList<ComboBoxLookup>();
    			indexes.add(new ComboBoxLookup(MSG.itemAllRooms(), "-"));
    			for (int i = 0; i < maxRooms; i++) {
    				indexes.add(new ComboBoxLookup(MSG.itemOnlyRoom(i + 1), String.valueOf(i)));
    			}
    			request.setAttribute("roomIndexes", indexes);
    		}
    	}
    }
    
    
    public static void setupBldgs(HttpServletRequest request, PreferenceGroup pg) throws Exception {
    	request.setAttribute(Building.BLDG_LIST_ATTR_NAME, pg.getAvailableBuildings());
    }

    /**
     * Get Preference Levels and store it in request object
     * @param request
     * @throws Exception
     */
    public static void setupPrefLevels(HttpServletRequest request) throws Exception {
        request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, PreferenceLevel.getPreferenceLevelList());
    }
    
    /**
     * Get Preference Levels and store it in request object (soft preferences only)
     * @param request
     * @throws Exception
     */
    public static void setupPrefLevelsSoftOnly(HttpServletRequest request) throws Exception {
        request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, PreferenceLevel.getPreferenceLevelListSoftOnly());
    }

    /**
     * Get Room Features and store it in request object
     * @param request
     * @param preferenceGroup
     * @throws Exception
     */
   
    public static void setupRoomFeatures(HttpServletRequest request, PreferenceGroup pg) throws Exception {
        request.setAttribute(RoomFeature.FEATURE_LIST_ATTR_NAME, pg.getAvailableRoomFeatures());
    }
    
    /**
     * Get Distribution Types and store it in request object
     * @param request
     * @throws Exception
     */
    public static void setupDistribTypes(HttpServletRequest request, SessionContext context, DistributionType current) throws Exception {
        request.setAttribute(DistributionType.DIST_TYPE_ATTR_NAME, DistributionType.findApplicable(context, false, false, current));
    }
    
    public static void setupExamDistribTypes(HttpServletRequest request, SessionContext context, DistributionType current) throws Exception {
        request.setAttribute(DistributionType.DIST_TYPE_ATTR_NAME, DistributionType.findApplicable(context, false, true, current));
    }

    public static void setupInstructorDistribTypes(HttpServletRequest request, SessionContext context, PreferenceGroup pg) throws Exception {
    	Set<DistributionType> types = DistributionType.findApplicable(context, true, false, null);
    	if (pg != null)
    		for (Iterator i = pg.getDistributionPreferences().iterator(); i.hasNext(); ) {
    			DistributionPref dp = (DistributionPref)i.next();
    			if (!types.contains(dp.getDistributionType()))
    				types.add(dp.getDistributionType());
    		}
        request.setAttribute(DistributionType.DIST_TYPE_ATTR_NAME, types);
    }

    public static void setupExaminationPeriods(HttpServletRequest request, Long sessionId, Long examType) throws Exception {
        request.setAttribute(ExamPeriod.PERIOD_ATTR_NAME, ExamPeriod.findAll(sessionId, examType));
    }

    public static void setupRoomGroups(HttpServletRequest request, PreferenceGroup pg) throws Exception {
        request.setAttribute(RoomGroup.GROUP_LIST_ATTR_NAME, pg.getAvailableRoomGroups());
    }

    public static void setupCourses(HttpServletRequest request, PreferenceGroup pg) throws Exception {
        request.setAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME, pg.getAvailableCourses());
    }
    
    public static void setupInstructorAttributes(HttpServletRequest request, PreferenceGroup pg) {
    	request.setAttribute(InstructorAttribute.ATTRIBUTES_LIST_ATTR_NAME, pg.getAvailableAttributes());
    }
    
    public static void setupInstructorAttributeTypes(HttpServletRequest request, PreferenceGroup pg) {
        request.setAttribute(InstructorAttributeType.ATTRIBUTE_TYPES_LIST_ATTR_NAME, pg.getAvailableAttributeTypes());
    }

    public static void setupDatePatterns(HttpServletRequest request, UserContext user, String inheritString, DatePattern inheritedDatePattern, Department department, DatePattern currentDatePattern) {
    	Vector list = new Vector();
    	list.addElement(new IdValue(Long.valueOf(-1),inheritString+(inheritedDatePattern==null?"":" ("+inheritedDatePattern.getName()+")")));
    	try {
    		for (DatePattern dp: DatePattern.findAll(user, department, currentDatePattern))
    			list.addElement(new IdValue(dp.getUniqueId(),dp.getName()));
    	} catch (Exception e) {
    		Debug.error(e);
    	}
    	request.setAttribute(DatePattern.DATE_PATTERN_LIST_ATTR, list);
    }

    /**
     * Get date patterns for a particular session
     * @param request
     * @param inheritString
     * @param inheritedDatePattern
     * @param department
     * @param currentDatePattern
     */
    public static void setupDatePatterns(HttpServletRequest request, Long acadSessionId, boolean includeExtended, String inheritString, DatePattern inheritedDatePattern, Department department, DatePattern currentDatePattern) {
    	Vector list = new Vector();
    	list.addElement(new IdValue(Long.valueOf(-1),inheritString+(inheritedDatePattern==null?"":" ("+inheritedDatePattern.getName()+")")));
    	try {
    		for (DatePattern dp: DatePattern.findAll(acadSessionId, includeExtended, department, currentDatePattern))
    			list.addElement(new IdValue(dp.getUniqueId(),dp.getName()));
    	} catch (Exception e) {
    		Debug.error(e);
    	}
    	
    	if (inheritedDatePattern == null && list.size() == 1)
    		request.setAttribute(DatePattern.DATE_PATTERN_LIST_ATTR, null);
    	else
    		request.setAttribute(DatePattern.DATE_PATTERN_LIST_ATTR, list);
    }

	public static void setupLearningManagementSystemInfos(HttpServletRequest request, UserContext user, boolean includeExtended, String inheritString, LearningManagementSystemInfo inheritedDatePattern) {
	    	Vector list = new Vector();
	    	list.addElement(new IdValue(Long.valueOf(-1),inheritString+(inheritedDatePattern==null?"":" ("+inheritedDatePattern.getLabel()+")")));
	    	try {
	    		for (LearningManagementSystemInfo lms: LearningManagementSystemInfo.findAll(user))
	    			list.addElement(new IdValue(lms.getUniqueId(),lms.getLabel()));
	    	} catch (Exception e) {
	    		Debug.error(e);
	    	}
	    	request.setAttribute(LearningManagementSystemInfo.LEARNING_MANAGEMENT_SYSTEM_LIST_ATTR, list);
    }

    
    /**
     * Get Instructors and store it in request object
     * @param request
     * @param deptUid department id, (null/blank if ALL instructors to be retrieved)
     * @throws Exception
     */
    public static void setupInstructors (HttpServletRequest request, SessionContext context, Long deptUid ) throws Exception {
        
		StringBuffer query = new StringBuffer("");
		
		if (deptUid!=null)
			query.append(" and i.department.uniqueId = " + deptUid);
		
        getInstructors(request, context, query);
    }
    
    /**
     * Get Instructors and store it in request object
     * @param request
     * @param deptUids department ids, (null if ALL instructors to be retrieved)
     * @throws Exception
     */
    public static void setupInstructors (HttpServletRequest request, SessionContext context, Long[] deptUids ) throws Exception {
        
		StringBuffer query = new StringBuffer("");

		if (deptUids!=null && deptUids.length>0) {
			query.append(" and i.department.uniqueId in ( " 
			        + Constants.arrayToStr(deptUids, "", ", ") + " )");
		}
		
        
        getInstructors(request, context, query);
    }

    /**
     * Executes the query to retrieve instructors
     * @param request
     * @param clause
     * @throws Exception
     */
    private static void getInstructors(HttpServletRequest request, SessionContext context, StringBuffer clause) throws Exception {
        String instructorNameFormat = UserProperty.NameFormat.get(context.getUser());
        
        Long acadSessionId = context.getUser().getCurrentAcademicSessionId();

		StringBuffer query = new StringBuffer();
		query.append("select distinct i from DepartmentalInstructor i ");
		query.append(" where i.department.session.uniqueId = :acadSessionId ");
		query.append(clause);
        
        DepartmentalInstructorDAO idao = DepartmentalInstructorDAO.getInstance();
		org.hibernate.Session hibSession = idao.getSession();

		Query<DepartmentalInstructor> q = hibSession.createQuery(query.toString(), DepartmentalInstructor.class);
		q.setFetchSize(5000);
		q.setCacheable(true);
		q.setParameter("acadSessionId", acadSessionId);
        
		List<DepartmentalInstructor> result = q.list();
        Vector<ComboBoxLookup> v = new Vector<ComboBoxLookup>(result.size());
        Vector<Long> h = new Vector<Long>(result.size());
        if (ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue())
        	Collections.sort(result, new DepartmentalInstructorComparator(UserProperty.NameFormat.get(context.getUser())));
        else
        	Collections.sort(result, new DepartmentalInstructorComparator());
	    for (Iterator i=result.iterator();i.hasNext();) {
            DepartmentalInstructor di = (DepartmentalInstructor)i.next();
            String name = di.getName(instructorNameFormat);
            v.addElement(new ComboBoxLookup(name, di.getUniqueId().toString()));
            if (di.hasPreferences())
                h.add(di.getUniqueId());
		}
        
        request.setAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME, v);
        request.setAttribute(DepartmentalInstructor.INSTR_HAS_PREF_ATTR_NAME, h);
    }
    
    /**
     * Read the roles from lookup table and store it in an array list
     * to be used to generate a drop down list of roles
     * @param request HttpServletRequest object
     * @throws Exception
     */
    public static void setupRoles(HttpServletRequest request) throws Exception {
        request.setAttribute(Roles.ROLES_ATTR_NAME, Roles.findAll(true));
    }

    /**
     * Gets course offereings based on parameters
     * @param request
     * @param onlyOffered true indicates only retrieve offered courses 
     * @param onlyControlling true indicates retrieve only controlling courses
     * @throws Exception
     */
    public static void setupCourseOfferings(
            HttpServletRequest request, 
            SessionContext context,
            CourseFilter filter) throws Exception {
    	setupCourseOfferings(request, context, filter, CourseOffering.CRS_OFFERING_LIST_ATTR_NAME);
    }
    
    public static void setupCourseOfferings(
            HttpServletRequest request, 
            SessionContext context,
            CourseFilter filter,
            String attribute) throws Exception {
    	
		List<CourseOffering> list = new ArrayList<CourseOffering>();
		for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser())) {
			Iterator<CourseOffering> i = null;
			try {
				i = subject.getCourseOfferings().iterator();
			}
			catch (ObjectNotFoundException e) {
			    new _RootDAO().getSession().refresh(subject);
			    i = subject.getCourseOfferings().iterator();
			}
			for (;i.hasNext();) {
				CourseOffering co = i.next();
				if (co != null && (filter == null || filter.accept(co)))
					list.add(co);
			}
		}
	    Collections.sort(list, new CourseOfferingComparator());
	    request.setAttribute(attribute, list);
    }
    
    public static interface CourseFilter {
    	public boolean accept(CourseOffering course);
    }

    public static void setupCourseCreditFormats(HttpServletRequest request) throws Exception {
        request.setAttribute(CourseCreditFormat.COURSE_CREDIT_FORMAT_ATTR_NAME, CourseCreditFormat.getCourseCreditFormatList());
    }
    
    public static void setupCourseCreditTypes(HttpServletRequest request) throws Exception {
        request.setAttribute(CourseCreditType.COURSE_CREDIT_TYPE_ATTR_NAME, CourseCreditType.getCourseCreditTypeList());
    }
    
    public static void setupCourseCreditUnitTypes(HttpServletRequest request) throws Exception {
        request.setAttribute(CourseCreditUnitType.COURSE_CREDIT_UNIT_TYPE_ATTR_NAME, CourseCreditUnitType.getCourseCreditUnitTypeList());
    }

    /**
     * Retrieves list of position types 
     * @param request
     * @throws Exception
     */
    public static void setupPositionTypes(HttpServletRequest request) throws Exception{
        request.setAttribute(PositionType.POSTYPE_ATTR_NAME, PositionType.getPositionTypeList());
    }

    /**
     * Retrieves list of consent types
     * @param request
     */
    public static void setupConsentType(HttpServletRequest request) {
        request.setAttribute(OfferingConsentType.CONSENT_TYPE_ATTR_NAME, OfferingConsentType.getConsentTypeList());
    }

    /**
     * Retrieves list of timetable managers
     * @param request
     */
    public static void setupTimetableManagers(HttpServletRequest request) {
        Vector v = new Vector(TimetableManager.getManagerList()); 
        request.setAttribute(TimetableManager.MGR_LIST_ATTR_NAME, v);
    }
    
    public static void setupExamTypes(HttpServletRequest request, Long sessionId) {
    	request.setAttribute("examTypes", sessionId == null ? ExamType.findAll() : ExamType.findAllUsed(sessionId));
    }
    
    public static void setupExamTypes(HttpServletRequest request, UserContext user, DepartmentStatusType.Status... status) {
    	request.setAttribute("examTypes", ExamType.findAllUsedApplicable(user, status));
    }
    
    public static void setupCourseTypes(HttpServletRequest request) {
    	request.setAttribute("courseTypes", CourseTypeDAO.getInstance().getSession().createQuery(
    			"from CourseType order by reference", CourseType.class).setCacheable(true).list());
    }
    
    public static void setupInstructorTeachingResponsibilities(HttpServletRequest request) {
        request.setAttribute("responsibilities", TeachingResponsibility.getInstructorTeachingResponsibilities());
    }
    
    public static void setupCoordinatorTeachingResponsibilities(HttpServletRequest request) {
        request.setAttribute("responsibilities", TeachingResponsibility.getCoordinatorTeachingResponsibilities());
    }
}
