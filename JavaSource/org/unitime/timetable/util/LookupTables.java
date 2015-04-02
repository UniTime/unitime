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
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.util.LabelValueBean;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.CourseCreditFormat;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.dao.CourseTypeDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;


/**
 * Contains methods on static read-only lookup tables
 * 
 * @author Heston Fernandes, Tomas Muller
 */
public class LookupTables {
    
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
    	
		List<LabelValueBean> deptList = new ArrayList<LabelValueBean>();
		for (Department d: departments) {
			String code = d.getDeptCode().trim();
			String abbv = d.getName().trim();
			if (d.isExternalManager())
				deptList.add(new LabelValueBean(code + " - " + abbv + " (" + d.getExternalMgrLabel() + ")", code));
			else	
				deptList.add(new LabelValueBean(code + " - " + abbv, code)); 
		}
		request.setAttribute(Department.DEPT_ATTR_NAME, deptList);
    }

    public static void setupRooms(HttpServletRequest request, PreferenceGroup pg) throws Exception {
    	request.setAttribute(Room.ROOM_LIST_ATTR_NAME, pg.getAvailableRooms());
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
    public static void setupDistribTypes(HttpServletRequest request, SessionContext context) throws Exception {
        request.setAttribute(DistributionType.DIST_TYPE_ATTR_NAME, DistributionType.findApplicable(context, false, false));
    }
    
    public static void setupExamDistribTypes(HttpServletRequest request, SessionContext context) throws Exception {
        request.setAttribute(DistributionType.DIST_TYPE_ATTR_NAME, DistributionType.findApplicable(context, false, true));
    }

    public static void setupInstructorDistribTypes(HttpServletRequest request, SessionContext context) throws Exception {
        request.setAttribute(DistributionType.DIST_TYPE_ATTR_NAME, DistributionType.findApplicable(context, true, false));
    }

    public static void setupExaminationPeriods(HttpServletRequest request, Long sessionId, Long examType) throws Exception {
        request.setAttribute(ExamPeriod.PERIOD_ATTR_NAME, ExamPeriod.findAll(sessionId, examType));
    }

    public static void setupRoomGroups(HttpServletRequest request, PreferenceGroup pg) throws Exception {
        request.setAttribute(RoomGroup.GROUP_LIST_ATTR_NAME, pg.getAvailableRoomGroups());
    }

    public static void setupDatePatterns(HttpServletRequest request, UserContext user, String inheritString, DatePattern inheritedDatePattern, Department department, DatePattern currentDatePattern) {
    	Vector list = new Vector();
    	list.addElement(new IdValue(new Long(-1),inheritString+(inheritedDatePattern==null?"":" ("+inheritedDatePattern.getName()+")")));
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
    public static void setupDatePatterns(HttpServletRequest request, org.unitime.timetable.model.Session acadSession, boolean includeExtended, String inheritString, DatePattern inheritedDatePattern, Department department, DatePattern currentDatePattern) {
    	Vector list = new Vector();
    	list.addElement(new IdValue(new Long(-1),inheritString+(inheritedDatePattern==null?"":" ("+inheritedDatePattern.getName()+")")));
    	try {
    		for (DatePattern dp: DatePattern.findAll(acadSession, includeExtended, department, currentDatePattern))
    			list.addElement(new IdValue(dp.getUniqueId(),dp.getName()));
    	} catch (Exception e) {
    		Debug.error(e);
    	}
    	
    	if (inheritedDatePattern == null && list.size() == 1)
    		request.setAttribute(DatePattern.DATE_PATTERN_LIST_ATTR, null);
    	else
    		request.setAttribute(DatePattern.DATE_PATTERN_LIST_ATTR, list);
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
        query.append(" order by upper(i.lastName), upper(i.firstName) ");
        
        DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
		org.hibernate.Session hibSession = idao.getSession();

		Query q = hibSession.createQuery(query.toString());
		q.setFetchSize(5000);
		q.setCacheable(true);
		q.setLong("acadSessionId", acadSessionId);
        
		List result = q.list();
        Vector v = new Vector(result.size());
        Vector h = new Vector(result.size());
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
    	
		List<CourseOffering> list = new ArrayList<CourseOffering>();
		for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser())) {
			for (CourseOffering co: subject.getCourseOfferings()) {
				if (filter == null || filter.accept(co))
					list.add(co);
			}
		}
	    Collections.sort(list, new CourseOfferingComparator());
	    request.setAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME, list);
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
    	request.setAttribute("examTypes", ExamType.findAllApplicable(user, status));
    }
    
    public static void setupCourseTypes(HttpServletRequest request) {
    	request.setAttribute("courseTypes", CourseTypeDAO.getInstance().findAll(Order.asc("reference")));
    }
}
