/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.action;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.impl.LocalizedLookupDispatchAction;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.Messages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.form.ClassListFormInterface;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.pdf.PdfClassListTableBuilder;


/**
 * @author Stephanie Schluttenhofer
 */
@Service("/classSearch")
public class ClassSearchAction extends LocalizedLookupDispatchAction {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;
	
	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */

	public ActionForward searchClasses(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return performAction(mapping, form, request, response, "searchClasses");
	}
	
	public ActionForward exportPdf(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		return performAction(mapping, form, request, response, "exportPdf");
	}
	
	
	public ActionForward performAction(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response, String action) throws Exception {
		
    	sessionContext.checkPermission(Right.Classes);
		
    	ClassListForm classListForm = (ClassListForm) form;
    	
    	request.setAttribute(Department.EXTERNAL_DEPT_ATTR_NAME, Department.findAllExternal(sessionContext.getUser().getCurrentAcademicSessionId()));
    	
    	if ("1".equals(request.getParameter("loadFilter"))) {
    		setupGeneralFormFilters(sessionContext, classListForm);
    		setupClassListSpecificFormFilters(sessionContext, classListForm);
    	} else {
    		sessionContext.getUser().setProperty("ClassList.divSec",classListForm.getDivSec() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.demand",classListForm.getDemand() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.demandIsVisible",classListForm.getDemandIsVisible() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.limit",classListForm.getLimit() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.roomLimit",classListForm.getRoomLimit() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.manager",classListForm.getManager() ? "1" : "0");
		   	sessionContext.getUser().setProperty("ClassList.datePattern",classListForm.getDatePattern() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.timePattern",classListForm.getTimePattern() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.instructor",classListForm.getInstructor() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.preferences",classListForm.getPreferences() ? "1" : "0");
	    	if (classListForm.getTimetable() != null)
	    		sessionContext.getUser().setProperty("ClassList.timetable",classListForm.getTimetable() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.schedulePrintNote",classListForm.getSchedulePrintNote() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.note",classListForm.getNote() ? "1" : "0");
	    	if (classListForm.getExams() != null)
	    		sessionContext.getUser().setProperty("ClassList.exams",classListForm.getExams() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.sortBy", classListForm.getSortBy());
	    	sessionContext.getUser().setProperty("ClassList.filterAssignedRoom", classListForm.getFilterAssignedRoom());		    	
	    	sessionContext.getUser().setProperty("ClassList.filterInstructor", classListForm.getFilterInstructor());		    	
	    	sessionContext.getUser().setProperty("ClassList.filterManager", classListForm.getFilterManager());		
	    	sessionContext.getUser().setProperty("ClassList.filterIType", classListForm.getFilterIType());
	    	sessionContext.getUser().setProperty("ClassList.filterDayCode", String.valueOf(classListForm.getFilterDayCode()));
	    	sessionContext.getUser().setProperty("ClassList.filterStartSlot", String.valueOf(classListForm.getFilterStartSlot()));
	    	sessionContext.getUser().setProperty("ClassList.filterLength", String.valueOf(classListForm.getFilterLength()));
	    	sessionContext.getUser().setProperty("ClassList.sortByKeepSubparts", String.valueOf(classListForm.getSortByKeepSubparts()));
	    	sessionContext.getUser().setProperty("ClassList.showCrossListedClasses", String.valueOf(classListForm.getShowCrossListedClasses()));
	    }
    	    	
    	if (!sessionContext.hasPermission(Right.Examinations))
    		classListForm.setExams(null);
    	
    	classListForm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
    	classListForm.setClasses(getClasses(classListForm, WebSolver.getClassAssignmentProxy(request.getSession())));
    	
    	Collection classes = classListForm.getClasses();
		if (classes.isEmpty()) {
		    ActionMessages errors = new ActionMessages();
		    errors.add("searchResult", new ActionMessage("errors.generic", MSG.errorNoRecords()));
		    saveErrors(request, errors);
		    return mapping.findForward("showClassSearch");
		} else {
			StringBuffer ids = new StringBuffer();
			StringBuffer names = new StringBuffer();
			StringBuffer subjIds = new StringBuffer();
			for (int i=0;i<classListForm.getSubjectAreaIds().length;i++) {
				if (i>0) {
					names.append(","); 
					subjIds.append(",");
					}
				ids.append("&subjectAreaIds="+classListForm.getSubjectAreaIds()[i]);
				subjIds.append(classListForm.getSubjectAreaIds()[i]);
				names.append(((new SubjectAreaDAO()).get(new Long(classListForm.getSubjectAreaIds()[i]))).getSubjectAreaAbbreviation());
			}
			sessionContext.setAttribute(SessionAttribute.ClassesSubjectAreas, subjIds);
			sessionContext.setAttribute(SessionAttribute.ClassesCourseNumber, classListForm.getCourseNbr());
			
			if ("exportPdf".equals(action)) {
	    		OutputStream out = ExportUtils.getPdfOutputStream(response, "classes");
				
				new PdfClassListTableBuilder().pdfTableForClasses(out,
						WebSolver.getClassAssignmentProxy(request.getSession()),
			    		WebSolver.getExamSolver(request.getSession()),
			    		classListForm, 
			    		sessionContext);
				
				out.flush(); out.close();
				
				return null;
			}
			
			BackTracker.markForBack(
					request, 
					"classSearch.do?doit=Search&loadFilter=1"+ids+"&courseNbr="+classListForm.getCourseNbr(), 
					MSG.backClasses(names+(classListForm.getCourseNbr()==null || classListForm.getCourseNbr().length()==0?"":" "+classListForm.getCourseNbr())), 
					true, true);
				
			    return mapping.findForward("showClassList");
			}
		}
	
	
	public static void setupGeneralFormFilters(SessionContext sessionContext, ClassListFormInterface form){
		form.setSortBy(sessionContext.getUser().getProperty("ClassList.sortBy", ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)));
		form.setFilterAssignedRoom(sessionContext.getUser().getProperty("ClassList.filterAssignedRoom", ""));
		form.setFilterManager(sessionContext.getUser().getProperty("ClassList.filterManager", ""));
		form.setFilterIType(sessionContext.getUser().getProperty("ClassList.filterIType", ""));
		form.setFilterDayCode(Integer.parseInt(sessionContext.getUser().getProperty("ClassList.filterDayCode", "-1")));
		form.setFilterStartSlot(Integer.parseInt(sessionContext.getUser().getProperty("ClassList.filterStartSlot", "-1")));
		form.setFilterLength(Integer.parseInt(sessionContext.getUser().getProperty("ClassList.filterLength", "-1")));
		form.setSortByKeepSubparts("1".equals(sessionContext.getUser().getProperty("ClassList.sortByKeepSubparts", "1")));
		form.setShowCrossListedClasses("1".equals(sessionContext.getUser().getProperty("ClassList.showCrossListedClasses", "0")));
	
	}
	
	public static void setupClassListSpecificFormFilters(SessionContext sessionContext, ClassListForm form){
		form.setDivSec("1".equals(sessionContext.getUser().getProperty("ClassList.divSec", "0")));	
		form.setLimit("1".equals(sessionContext.getUser().getProperty("ClassList.limit", "1")));
		form.setRoomLimit("1".equals(sessionContext.getUser().getProperty("ClassList.roomLimit", "1")));
		form.setManager("1".equals(sessionContext.getUser().getProperty("ClassList.manager", "1")));	
		form.setDatePattern("1".equals(sessionContext.getUser().getProperty("ClassList.datePattern", "1")));
		form.setTimePattern("1".equals(sessionContext.getUser().getProperty("ClassList.timePattern", "1")));
		form.setInstructor("1".equals(sessionContext.getUser().getProperty("ClassList.instructor", "1")));
		form.setPreferences("1".equals(sessionContext.getUser().getProperty("ClassList.preferences", "1")));
		form.setTimetable("1".equals(sessionContext.getUser().getProperty("ClassList.timetable", "1")));	
		form.setFilterInstructor(sessionContext.getUser().getProperty("ClassList.filterInstructor", ""));
		form.setSchedulePrintNote("1".equals(sessionContext.getUser().getProperty("ClassList.schedulePrintNote", "1")));
		form.setNote("1".equals(sessionContext.getUser().getProperty("ClassList.note", "0")));
	    form.setExams("1".equals(sessionContext.getUser().getProperty("ClassList.exams", "0")));
	    if (StudentClassEnrollment.sessionHasEnrollments(sessionContext.getUser().getCurrentAcademicSessionId())) {
	    	form.setDemandIsVisible(true);
			form.setDemand("1".equals(sessionContext.getUser().getProperty("ClassList.demand", "1")));
	    } else {
		    form.setDemandIsVisible(false);
			form.setDemand(false);
	    }
	    
	}
	
    public static Set getClasses(ClassListFormInterface form, ClassAssignmentProxy classAssignmentProxy) {
		org.hibernate.Session hibSession = (new InstructionalOfferingDAO()).getSession();

		
		boolean doFilterManager = form.getFilterManager()!=null && form.getFilterManager().length()>0;
		Long filterManager = (doFilterManager?Long.valueOf(form.getFilterManager()):null);

        boolean fetchStructure = true;
        boolean fetchCredits = false;//form.getCredit().booleanValue();
        boolean fetchInstructors = false;//form.getInstructor().booleanValue();
        boolean fetchPreferences = false;//form.getPreferences().booleanValue() || form.getTimePattern().booleanValue();
        boolean fetchAssignments = false;//(form.getTimetable()!=null && form.getTimetable().booleanValue());
        
		String[] subjectIds = form.getSubjectAreaIds();
		if (subjectIds != null && subjectIds.length > 0){
			StringBuffer query = new StringBuffer();			
			query.append("select c, co from Class_ as c ");
			
			if (fetchStructure) {
				query.append("left join fetch c.childClasses as cc ");
				query.append("left join fetch c.schedulingSubpart as ss ");
				query.append("left join fetch ss.childSubparts as css ");
				query.append("left join fetch ss.instrOfferingConfig as ioc ");
				query.append("left join fetch ioc.instructionalOffering as io ");
				query.append("left join fetch io.courseOfferings as cox ");
			}
			
			if (fetchCredits)
				query.append("left join fetch ss.creditConfigs as ssc ");
			
			if (fetchPreferences || fetchInstructors) {
				query.append("left join fetch c.classInstructors as ci ");
				query.append("left join fetch ci.instructor as di ");
			}
			
			if (fetchAssignments) {
				query.append("left join fetch c.assignments as ca ");
				query.append("left join fetch ca.rooms as car ");
			}
			
			if (fetchPreferences) {
				query.append("left join fetch c.preferences as cp ");
				query.append("left join fetch ss.preferences as ssp ");
				query.append("left join fetch di.preferences as dip ");
			}
			
			query.append("inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co ");
			//query.append (" where c.schedulingSubpart in ( select ss2 from SchedulingSubpart as ss2 inner join ss2.instrOfferingConfig.instructionalOffering.courseOfferings as co2 ");
			query.append(" where co.subjectArea.uniqueId in ( ");
			boolean first = true;
			for(int i = 0; i < subjectIds.length; i++){
				if (!first){
					query.append(", ");
				} else {
					first = false;
				}
				query.append(subjectIds[i]);
			}
			query.append(") ");			
	        if (form.getCourseNbr() != null && form.getCourseNbr().length() > 0){
	            String courseNbr = form.getCourseNbr();
	            query.append(" and co.courseNbr ");
			    if (courseNbr.indexOf('*')>=0) {
		            query.append(" like '");
		            courseNbr = courseNbr.replace('*', '%');
			    }
			    else {
		            query.append(" = '");
			    }
	            if ("true".equals(ApplicationProperties.getProperty("tmtbl.courseNumber.upperCase", "true")))
	            	courseNbr = courseNbr.toUpperCase();
	            query.append(courseNbr);
	            query.append("'  ");
	        }
	        // query.append(" ) ");

	        if (doFilterManager) {
	        	if (filterManager.longValue()<0) { //all departmental
	        		query.append(" and (c.managingDept is null or c.managingDept in co.subjectArea.department)");
	        	} else {
	        		query.append(" and c.managingDept = "+filterManager);
	        	}
	        }
	        
	        //NOTE: former implementation -- only classes that are editable were displayed on the classes page  
	        /*  
			String[] deptIds = form.getUserDeptIds();
			if (!form.isUserIsAdmin() && !form.isReturnAllControlClassesForSubjects() && deptIds != null){
				query.append(" and ((c.managingDept is not null and c.managingDept.uniqueId in (");
				first = true;
				for(int i = 0; i < deptIds.length; i++){
					if (!first){
						query.append(", ");
					} else {
						first = false;
					}
					query.append(deptIds[i]);
				}
				query.append(")) or (c.managingDept is null and co2.subjectArea.department.uniqueId in (" );
				first = true;
				for(int i = 0; i < deptIds.length; i++){
					if (!first){
						query.append(", ");
					} else {
						first = false;
					}
					query.append(deptIds[i]);
				}
				query.append("))");
				if (form.isSessionInLLREditStatus()){
					query.append(" or (c.managingDept is not null and co2.subjectArea.department.uniqueId in (" );
					first = true;
					for(int i = 0; i < deptIds.length; i++){
						if (!first){
							query.append(", ");
						} else {
							first = false;
						}
						query.append(deptIds[i]);
					}
					query.append("))");
				}
				query.append(")");
			}
			*/
	        
	        // query.append(" ) ");
	        if (!form.getShowCrossListedClasses()) {
	        	query.append(" and co.isControl = true ");
	        }
			Query q = hibSession.createQuery(query.toString());
			q.setFetchSize(1000);
			q.setCacheable(true);
	        TreeSet ts = new TreeSet(new ClassCourseComparator(form.getSortBy(), classAssignmentProxy, form.getSortByKeepSubparts()));
			long sTime = new java.util.Date().getTime();
			
			boolean doFilterInstructor = form.getFilterInstructor()!=null && form.getFilterInstructor().length()>0;
			String filterInstructor = (doFilterInstructor?form.getFilterInstructor().toUpperCase():null);
			
			boolean doFilterAssignedRoom = form.getFilterAssignedRoom()!=null && form.getFilterAssignedRoom().length()>0;
			String filterAssignedRoom = (doFilterAssignedRoom?form.getFilterAssignedRoom().toUpperCase():null);
			
			boolean doFilterIType = form.getFilterIType()!=null && form.getFilterIType().length()>0;
			Integer filterIType = (doFilterIType?Integer.valueOf(form.getFilterIType()):null);
			
			boolean doFilterAssignedTime = ((form.getFilterDayCode()>=0 && form.getFilterStartSlot()>=0 && form.getFilterLength()>=0) || 
											(form.getFilterDayCode()>0 && form.getFilterStartSlot()<0 && form.getFilterLength()<=0));
			TimeLocation filterAssignedTime = (doFilterAssignedTime?
					new TimeLocation(
							(form.getFilterDayCode()==0?255:form.getFilterDayCode()),
							(form.getFilterStartSlot()<0?0:form.getFilterStartSlot()),
							(form.getFilterStartSlot()<0?Constants.SLOTS_PER_DAY:Math.max(5,Constants.SLOT_LENGTH_MIN+form.getFilterLength()-1)/Constants.SLOT_LENGTH_MIN),
							0,0,null,null,null,0):null);
			// days, start time & length selected -> create appropriate time location
			// days, start time selected -> create appropriate time location with 1 slot length
			// start time & length selected -> create time location all days with given start time and length
            // only start time selected -> create time location all days with given start time and 1 slot length
			// only days selected -> create time location of given days all day long (all location assigned in the given days overlap)
			
			List allClasses = q.list();
			
			Debug.debug(" --- Load structure ---");
			for (Iterator i=allClasses.iterator();i.hasNext();) {
				Object[] o = (Object[])i.next(); Class_ c = (Class_)o[0];
				Hibernate.initialize(c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs());
			}
			for (Iterator i=allClasses.iterator();i.hasNext();) {
				Object[] o = (Object[])i.next(); Class_ c = (Class_)o[0];
				Hibernate.initialize(c.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts());
			}
			for (Iterator i=allClasses.iterator();i.hasNext();) {
				Object[] o = (Object[])i.next(); Class_ c = (Class_)o[0];
				Hibernate.initialize(c.getSchedulingSubpart().getClasses());
			}

			Debug.debug(" --- Filter classes ---");
			for (Iterator i=q.list().iterator();i.hasNext();) {
				Object[] o = (Object[])i.next(); Class_ c = (Class_)o[0];
				if (doFilterInstructor) {
					boolean filterLine = true;
					for (Iterator j=c.getClassInstructors().iterator();j.hasNext();) {
						ClassInstructor ci = (ClassInstructor)j.next();
						StringTokenizer stk = new StringTokenizer(filterInstructor," ,");
						boolean containsInstructor = true;
						while (stk.hasMoreTokens()) {
							String token = stk.nextToken();
							boolean containsToken = false;
							if (ci.getInstructor().getFirstName()!=null && ci.getInstructor().getFirstName().toUpperCase().indexOf(token)>=0)
								containsToken = true;
							if (!containsToken && ci.getInstructor().getMiddleName()!=null && ci.getInstructor().getMiddleName().toUpperCase().indexOf(token)>=0)
								containsToken = true;
							if (!containsToken && ci.getInstructor().getLastName()!=null && ci.getInstructor().getLastName().toUpperCase().indexOf(token)>=0)
								containsToken = true;
							if (!containsToken) {
								containsInstructor = false; break;
							}
						}
						if (containsInstructor) {
							filterLine = false; break;
						}
					}
					if (filterLine) {
						hibSession.evict(c);
						continue;
					}
				}
				/*
				if (doFilterManager) {
					if (filterManager.longValue()<0) {
						if (c.getManagingDept()==null || c.getManagingDept().isExternalManager().booleanValue())
							continue;
					} else if (c.getManagingDept()==null || !c.getManagingDept().getUniqueId().equals(filterManager))
						continue;
				}
				*/
				
				if (doFilterIType) {
				    ItypeDesc itype = c.getSchedulingSubpart().getItype();
				    boolean match=false;
				    while (!match && itype!=null) {
				        match = itype.getItype().equals(filterIType);
				        itype = itype.getParent();
				    }
					if (!match) {
						hibSession.evict(c);
						continue;
					}
				}
				
				if (doFilterAssignedTime) {
					try {
						Assignment a = classAssignmentProxy.getAssignment(c);
						if (a==null) {
							hibSession.evict(c);
							continue;
						}
						Placement p = a.getPlacement();
						if (p==null) {
							hibSession.evict(c);
							continue;
						}
						TimeLocation t = p.getTimeLocation();
						if (t==null) {
							hibSession.evict(c);
							continue;
						}
						boolean overlap = t.shareDays(filterAssignedTime) && t.shareHours(filterAssignedTime);
						if (!overlap) {
							hibSession.evict(c);
							continue;
						}
					} catch (Exception e) {
						hibSession.evict(c);
						continue;
					}
				}
				
				if (doFilterAssignedRoom) {
					try {
						Assignment a = classAssignmentProxy.getAssignment(c);
						if (a==null) {
							hibSession.evict(c);
							continue;
						}
						Placement p = a.getPlacement();
						if (p==null || p.getNrRooms()<=0) {
							hibSession.evict(c);
							continue;
						}
						boolean filterLine = true;
						if (p.isMultiRoom()) {
							for (RoomLocation r: p.getRoomLocations()) {
								if (r.getName().toUpperCase().indexOf(filterAssignedRoom)>=0) {
									filterLine = false;
									break;
								}
							}
						} else {
							if (p.getRoomLocation().getName().toUpperCase().indexOf(filterAssignedRoom)>=0) {
								filterLine = false;
							}
						}
						if (filterLine) {
							hibSession.evict(c);
							continue;
						}
					} catch (Exception e) {
						continue;
					}
				}
				
				ts.add(o);
			}
			
			if (form.getInstructor().booleanValue() || form.getPreferences().booleanValue() || form.getTimePattern().booleanValue()) {
				Debug.debug("---- Load Instructors ---- ");
				for (Iterator i=ts.iterator();i.hasNext();) {
					Object[] o = (Object[])i.next(); Class_ c = (Class_)o[0];
					Hibernate.initialize(c.getClassInstructors());
				}
				for (Iterator i=ts.iterator();i.hasNext();) {
					Object[] o = (Object[])i.next(); Class_ c = (Class_)o[0];
					for (Iterator j=c.getClassInstructors().iterator();j.hasNext();) {
						ClassInstructor ci = (ClassInstructor)j.next();
						Hibernate.initialize(ci.getInstructor());
					}
				}
			}
			
			if (form.getPreferences().booleanValue() || form.getTimePattern().booleanValue()) {
				Debug.debug("---- Load Preferences ---- ");
				for (Iterator i=ts.iterator();i.hasNext();) {
					Object[] o = (Object[])i.next(); Class_ c = (Class_)o[0];
					Hibernate.initialize(c.getPreferences());
					Hibernate.initialize(c.getSchedulingSubpart().getPreferences());
					for (Iterator j=c.getClassInstructors().iterator();j.hasNext();) {
						ClassInstructor ci = (ClassInstructor)j.next();
						Hibernate.initialize(ci.getInstructor().getPreferences());
					}
					c.getControllingDept().getPreferences();
					c.getManagingDept().getPreferences();
					Hibernate.initialize(c.getDistributionObjects());
					Hibernate.initialize(c.getSchedulingSubpart().getDistributionObjects());
				}
			}
			
			if (form.getTimetable()!=null && form.getTimetable().booleanValue() && classAssignmentProxy!=null && classAssignmentProxy instanceof Solution) {
				Debug.debug("--- Load Assignments --- ");
				for (Iterator i=ts.iterator();i.hasNext();) {
					Object[] o = (Object[])i.next(); Class_ c = (Class_)o[0];
					try {
						Assignment a = classAssignmentProxy.getAssignment(c);
						if (a!=null)
							Hibernate.initialize(a);
					} catch (Exception e) {}
				}
			}
			
			long eTime = new java.util.Date().getTime();
	        Debug.debug("fetch time = " + (eTime - sTime));
	        Debug.debug("rows = " + ts.size());
	        return (ts);
	        
	        
		} else {
	        	return (new TreeSet());
	    }

    }

	@Override
	protected Messages getMessages() {
		return MSG;
	}

}
