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
package org.unitime.timetable.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.util.LabelValueBean;
import org.hibernate.Query;
import org.hibernate.Session;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.CourseCreditFormat;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.ReservationType;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;


/**
 * Contains methods on static read-only lookup tables
 * 
 * @author Heston Fernandes
 */
public class LookupTables {
    
    /**
     * Gets the current academic session id for the current session
     * @param request
     * @return
     */
    private static String getAcademicSessionId(HttpServletRequest request) {
        User user = Web.getUser(request.getSession());
		return user.getAttribute(Constants.SESSION_ID_ATTR_NAME).toString();
    }
    
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

    public static void setupDeptsForUser(HttpServletRequest request, User user, Long sessionId, boolean includeExternal) throws Exception {
		ArrayList departments = new ArrayList();
		TreeSet depts = new TreeSet();
		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager owner = tdao.get(new Long(mgrId));

        if (user.getRole().equals(Roles.ADMIN_ROLE) || user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE)) {
			depts = Department.findAllBeingUsed(sessionId);
		} else {
			depts = Department.findAllOwned(sessionId, owner, includeExternal);
		}
			
		for (Iterator i=depts.iterator();i.hasNext();) {
			Department d = (Department)i.next();
			String code = d.getDeptCode().trim();
			String abbv = d.getName().trim();
			if (d.isExternalManager().booleanValue())
				departments.add(new LabelValueBean(code + " - " + abbv + " ("+d.getExternalMgrLabel()+")", code));
			else
				departments.add(new LabelValueBean(code + " - " + abbv, code));
		}
		
		request.setAttribute(Department.DEPT_ATTR_NAME, departments);
    }

    /**
     * Get Time Patterns and store it in request object
     * @param request
     * @throws Exception
     */
    public static void setupTimePatterns(HttpServletRequest request) throws Exception {
        Vector v = TimePattern.findAll(org.unitime.timetable.model.Session.getCurrentAcadSession(Web.getUser(request.getSession())), Boolean.TRUE);
        request.setAttribute(TimePattern.TIME_PATTERN_ATTR_NAME, v);
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
        Vector v = PreferenceLevel.getPreferenceLevelList(false);
        request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, v);
    }
    
    /**
     * Get Preference Levels and store it in request object (soft preferences only)
     * @param request
     * @throws Exception
     */
    public static void setupPrefLevelsSoftOnly(HttpServletRequest request) throws Exception {
        Vector v = PreferenceLevel.getPreferenceLevelListSoftOnly(false);
        request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, v);
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
    public static void setupDistribTypes(HttpServletRequest request) throws Exception {
        request.setAttribute(DistributionType.DIST_TYPE_ATTR_NAME, DistributionType.findApplicable(request, false, false));
    }
    
    public static void setupExamDistribTypes(HttpServletRequest request) throws Exception {
        request.setAttribute(DistributionType.DIST_TYPE_ATTR_NAME, DistributionType.findApplicable(request, false, true));
    }

    public static void setupInstructorDistribTypes(HttpServletRequest request) throws Exception {
        request.setAttribute(DistributionType.DIST_TYPE_ATTR_NAME, DistributionType.findApplicable(request, true, false));
    }

    public static void setupExaminationPeriods(HttpServletRequest request, Integer examType) throws Exception {
        request.setAttribute(ExamPeriod.PERIOD_ATTR_NAME, ExamPeriod.findAll(request, examType));
    }

    public static void setupRoomGroups(HttpServletRequest request, PreferenceGroup pg) throws Exception {
        request.setAttribute(RoomGroup.GROUP_LIST_ATTR_NAME, pg.getAvailableRoomGroups());
    }

    public static void setupDatePatterns(HttpServletRequest request, String inheritString, DatePattern inheritedDatePattern, Department department, DatePattern currentDatePattern) {
    	User user = Web.getUser(request.getSession());
    	Vector list = new Vector();
    	list.addElement(new IdValue(new Long(-1),inheritString+(inheritedDatePattern==null?"":" ("+inheritedDatePattern.getName()+")")));
    	try {
    		for (Enumeration e=DatePattern.findAll(request, department, currentDatePattern).elements();e.hasMoreElements();) {
    			DatePattern dp = (DatePattern)e.nextElement();
    			list.addElement(new IdValue(dp.getUniqueId(),dp.getName()));
    		}
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
    	User user = Web.getUser(request.getSession());
    	Vector list = new Vector();
    	list.addElement(new IdValue(new Long(-1),inheritString+(inheritedDatePattern==null?"":" ("+inheritedDatePattern.getName()+")")));
    	try {
    		for (Enumeration e=(DatePattern.findAll(acadSession, includeExtended, department, currentDatePattern)).elements();e.hasMoreElements();) {
    			DatePattern dp = (DatePattern)e.nextElement();
    			list.addElement(new IdValue(dp.getUniqueId(),dp.getName()));
    		}
    	} catch (Exception e) {
    		Debug.error(e);
    	}
    	
    	if (inheritedDatePattern==null && list.size()==1)
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
    public static void setupInstructors (
            HttpServletRequest request, Long deptUid ) throws Exception {
        
		StringBuffer query = new StringBuffer("");
		
		if (deptUid!=null)
			query.append(" and i.department.uniqueId = " + deptUid);
		
        getInstructors(request, query);
    }
    
    /**
     * Get Instructors and store it in request object
     * @param request
     * @param deptUids department ids, (null if ALL instructors to be retrieved)
     * @throws Exception
     */
    public static void setupInstructors (
            HttpServletRequest request, Long[] deptUids ) throws Exception {
        
		StringBuffer query = new StringBuffer("");

		if (deptUids!=null && deptUids.length>0) {
			query.append(" and i.department.uniqueId in ( " 
			        + Constants.arrayToStr(deptUids, "", ", ") + " )");
		}
		
        
        getInstructors(request, query);
    }

    /**
     * Executes the query to retrieve instructors
     * @param request
     * @param clause
     * @throws Exception
     */
    private static void getInstructors(HttpServletRequest request, StringBuffer clause) throws Exception {
        String instructorNameFormat = Settings.getSettingValue(Web.getUser(request.getSession()), Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
        
        String acadSessionId = getAcademicSessionId(request);

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
		q.setInteger("acadSessionId", Integer.parseInt(acadSessionId));
        
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
        Vector v = Roles.getRolesList(false);
        request.setAttribute(Roles.ROLES_ATTR_NAME, v);
    }

    /**
     * Gets all controlling course offerings (including not offered) 
     * @param request
     * @throws Exception
     */
    public static void setupCourseOfferings(HttpServletRequest request) throws Exception {
        setupCourseOfferings(request, false, true);
    }

    /**
     * Gets all course offerings (excluding not offered) 
     * @param request
     * @throws Exception
     */
    public static void setupAllOfferedCourseOfferings(HttpServletRequest request) throws Exception {
        setupCourseOfferings(request, true, false);
    }

    /**
     * Gets course offereings based on parameters
     * @param request
     * @param onlyOffered true indicates only retrieve offered courses 
     * @param onlyControlling true indicates retrieve only controlling courses
     * @throws Exception
     */
    private static void setupCourseOfferings(
            HttpServletRequest request, 
            boolean onlyOffered,
            boolean onlyControlling ) throws Exception {
        
        User user = Web.getUser(request.getSession());
		String acadSessionId = getAcademicSessionId(request);
		String mgrId = user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME).toString();
		StringBuffer query = new StringBuffer();

		if( user.getRole().equals(Roles.ADMIN_ROLE) ){			
			query.append("select distinct co ");
		    query.append("  from InstructionalOffering io, CourseOffering co ");
		    query.append("  where io.session.uniqueId=:acadSessionId ");
		    
		    if (onlyControlling)
		        query.append("  and co.isControl=1 ");

		    if (onlyOffered)
		        query.append("  and io.notOffered=0 ");
		    
		    query.append("  and io.uniqueId=co.instructionalOffering.uniqueId ");
	        query.append(" order by co.subjectAreaAbbv, co.courseNbr ");

	        CourseOfferingDAO cdao = new CourseOfferingDAO();
			Session hibSession = cdao.getSession();
			
			Query q = hibSession.createQuery(query.toString());
			q.setFetchSize(5000);
			q.setCacheable(true);
			
			q.setInteger("acadSessionId", Integer.parseInt(acadSessionId));
			
			request.setAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME, q.list());
		}
		else {
		    Vector coList = new Vector();
	        TimetableManagerDAO mgrDao = new TimetableManagerDAO();
		    TimetableManager mgr = mgrDao.get(new Long(mgrId));
		    Set depts = mgr.getDepartments();
		    for (Iterator di = depts.iterator(); di.hasNext();) {
		        Department dept = (Department) di.next();
		        Set subjAreas = dept.getSubjectAreas();
			    for (Iterator si = subjAreas.iterator(); si.hasNext();) {
			        SubjectArea sa = (SubjectArea) si.next();
			        if (!acadSessionId.equals(sa.getSessionId().toString()))
			                continue;
			        Set cos = sa.getCourseOfferings();
				    for (Iterator ci = cos.iterator(); ci.hasNext();) {
				        CourseOffering co = (CourseOffering) ci.next();
				        if (!co.isIsControl().booleanValue()) continue;
				        if (!co.isFullyEditableBy(user)) continue; //i.e., is the user able to make that course not offered?
				        coList.addElement(co);
				    }			        
			    }
		    }
		    Collections.sort(coList, new CourseOfferingComparator());
			request.setAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME, coList);
		    
		}
    }
    
    
    /**
     * Gets the controlling course offerings for a user 
     * @param request
     * @throws Exception
     */
    public static void setupCourseOfferingDemands(HttpServletRequest request, CourseOffering includeCourseOffering) throws Exception {
        User user = Web.getUser(request.getSession());
		Long acadSessionId = Long.valueOf(getAcademicSessionId(request));
		String mgrId = user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME).toString();
		StringBuffer query = new StringBuffer();

		if( user.getRole().equals(Roles.ADMIN_ROLE) ){			
			query.append("select distinct co ");
		    query.append("  from InstructionalOffering io, CourseOffering co ");
		    query.append("  where io.session.uniqueId=:acadSessionId ");
		    query.append("  and co.demand>0 ");
		    query.append("  and io.uniqueId=co.instructionalOffering.uniqueId ");
	        query.append(" order by co.subjectAreaAbbv, co.courseNbr ");

	        CourseOfferingDAO cdao = new CourseOfferingDAO();
			Session hibSession = cdao.getSession();
			
			Query q = hibSession.createQuery(query.toString());
			q.setFetchSize(5000);
			q.setCacheable(false);
			
			q.setLong("acadSessionId", acadSessionId.longValue());
			
			request.setAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME, q.list());
		} else {
		    TreeSet coList = new TreeSet(new CourseOfferingComparator());
	        TimetableManagerDAO mgrDao = new TimetableManagerDAO();
		    TimetableManager mgr = mgrDao.get(new Long(mgrId));
		    Set depts = mgr.departmentsForSession(acadSessionId);
		    for (Iterator di = depts.iterator(); di.hasNext();) {
		        Department dept = (Department) di.next();
		        Set subjAreas = dept.getSubjectAreas();
			    for (Iterator si = subjAreas.iterator(); si.hasNext();) {
			        SubjectArea sa = (SubjectArea) si.next();
				    for (Iterator ci = sa.getCourseOfferings().iterator(); ci.hasNext();) {
				        CourseOffering co = (CourseOffering) ci.next();
				        if (co.getDemand().intValue()>0)
				            coList.add(co);
				    }			        
			    }
		    }
		    if (includeCourseOffering!=null) 
		    	coList.add(includeCourseOffering);
			request.setAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME, coList);
		}
    }

    public static void setupCourseCreditFormats(HttpServletRequest request) throws Exception {
        Vector v = CourseCreditFormat.getCourseCreditFormatList(false);
        request.setAttribute(CourseCreditFormat.COURSE_CREDIT_FORMAT_ATTR_NAME, v);
    }
    
    public static void setupCourseCreditTypes(HttpServletRequest request) throws Exception {
        Vector v = CourseCreditType.getCourseCreditTypeList(false);
        request.setAttribute(CourseCreditType.COURSE_CREDIT_TYPE_ATTR_NAME, v);
    }
    
    public static void setupCourseCreditUnitTypes(HttpServletRequest request) throws Exception {
        Vector v = CourseCreditUnitType.getCourseCreditUnitTypeList(false);
        request.setAttribute(CourseCreditUnitType.COURSE_CREDIT_UNIT_TYPE_ATTR_NAME, v);
    }

    /**
     * Retrieves list of position types 
     * @param request
     * @throws Exception
     */
    public static void setupPositionTypes(HttpServletRequest request) throws Exception{
        Vector v = PositionType.getPositionTypeList(false);
        request.setAttribute(PositionType.POSTYPE_ATTR_NAME, v);
    }

    /**
     * Retrieves list of reservation classifications 
     * @param request
     * @param excludeList Reservation classes to be excluded from the list
     * @throws Exception
     */
    public static void setupReservationClass(
            HttpServletRequest request,
            String[] excludeList ) throws Exception{
        
        Vector v = new Vector();
        String[] rc = Constants.RESV_CLASS_LABELS;
        
        outer: for (int i=0; i<rc.length; i++) {
            String val = rc[i];
        
            if (excludeList!=null && excludeList.length>0) {
                for (int j=0; j<excludeList.length; j++) {
                    if (val.equals(excludeList[j]))
                        continue outer;
                }
            }
            
            v.addElement(new ComboBoxLookup(val, val));
        }
        request.setAttribute(Reservation.RESV_CLASS_REQUEST_ATTR, v);
    }

    /**
     * Retrieves list of reservation priorities 
     * @param request
     * @throws Exception
     */
    public static void setupReservationPriorities(HttpServletRequest request) throws Exception{
        Vector v = new Vector();
        int[] rp = Constants.RESV_PRIORITIES;
        for (int i=0; i<rp.length; i++) {
           v.addElement(new ComboBoxLookup(rp[i]+"", rp[i]+""));
        }
        request.setAttribute(Reservation.RESV_PRIORITY_REQUEST_ATTR, v);
    }

    /**
     * Retrieves list of academic areas for the current academic session 
     * @param request
     * @throws Exception
     */
    public static void setupAcademicAreas(HttpServletRequest request) throws Exception{
		Long acadSessionId = Long.valueOf(getAcademicSessionId(request));
        Vector v = new Vector(AcademicArea.getAcademicAreaList(acadSessionId)); 
        request.setAttribute(AcademicArea.ACAD_AREA_REQUEST_ATTR, v);
    }

    /**
     * Retrieves list of academic classifications for the current academic session
     * @param request
     * @throws Exception
     */
    public static void setupAcademicClassifications(HttpServletRequest request) throws Exception{
		Long acadSessionId = Long.valueOf(getAcademicSessionId(request));
        Vector v = new Vector(AcademicClassification.getAcademicClassificationList(acadSessionId)); 
        request.setAttribute(AcademicClassification.ACAD_CLASS_REQUEST_ATTR, v);
    }

    /**
     * Retrieves list of reservation types
     * @param request
     * @throws Exception
     */
    public static void setupReservationTypes(HttpServletRequest request) throws Exception{
        Vector v = new Vector(ReservationType.getReservationTypeList(false)); 
        request.setAttribute(ReservationType.RESVTYPE_ATTR_NAME, v);
    }

    /**
     * Retrieves list of POS majors for the current academic session
     * @param request
     * @throws Exception
     */
    public static void setupPosMajors(HttpServletRequest request) throws Exception{
		Long acadSessionId = Long.valueOf(getAcademicSessionId(request));
        Vector v = new Vector(PosMajor.getPosMajorList(acadSessionId)); 
        request.setAttribute(PosMajor.POSMAJOR_ATTR_NAME, v);
    }

    /**
     * Retrieves list of student groups for the current academic session
     * @param request
     * @throws Exception
     */
    public static void setupStudentGroups(HttpServletRequest request) throws Exception{
		Long acadSessionId = Long.valueOf(getAcademicSessionId(request));
        Vector v = new Vector(StudentGroup.getStudentGroupList(acadSessionId)); 
        request.setAttribute(StudentGroup.STUGRP_ATTR_NAME, v);
    }

    /**
     * Retrieves list of consent types
     * @param request
     */
    public static void setupConsentType(HttpServletRequest request) {
        Vector v = new Vector(OfferingConsentType.getConsentTypeList(false)); 
        request.setAttribute(OfferingConsentType.CONSENT_TYPE_ATTR_NAME, v);
    }

    /**
     * Retrieves list of timetable managers
     * @param request
     */
    public static void setupTimetableManagers(HttpServletRequest request) {
        Vector v = new Vector(TimetableManager.getManagerList()); 
        request.setAttribute(TimetableManager.MGR_LIST_ATTR_NAME, v);
    }

}
