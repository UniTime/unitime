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
package org.unitime.timetable.action;

import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.LookupDispatchAction;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.form.ClassListFormInterface;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.pdf.PdfClassListTableBuilder;


/**
 * @author Stephanie Schluttenhofer
 */

public class ClassSearchAction extends LookupDispatchAction {

	protected Map getKeyMethodMap() {
	      Map map = new HashMap();
	      map.put("button.searchClasses", "searchClasses");
	      map.put("button.exportPDF", "exportPdf");
//	      map.put("button.cancel", "searchClasses");
	      return map;
	}
	
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
		
			if(!Web.isLoggedIn( request.getSession() )) {
	            throw new Exception ("Access Denied.");
	        }
			HttpSession httpSession = request.getSession();
			
		    ClassListForm classListForm = (ClassListForm) form;
		    
		    User user = Web.getUser(request.getSession());
		    LookupTables.setupExternalDepts(request, (Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME));
			setupBasicFormData(classListForm, user);

		    if ("1".equals(request.getParameter("loadFilter"))) {
				setupGeneralFormFilters(httpSession, classListForm);
				setupClassListSpecificFormFilters(httpSession, classListForm);
		    } else {
		    	UserData.setPropertyBoolean(httpSession,"ClassList.divSec",classListForm.getDivSec().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"ClassList.limit",classListForm.getLimit().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"ClassList.roomLimit",classListForm.getRoomLimit().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"ClassList.manager",classListForm.getManager().booleanValue());
			   	UserData.setPropertyBoolean(httpSession,"ClassList.datePattern",classListForm.getDatePattern().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"ClassList.timePattern",classListForm.getTimePattern().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"ClassList.instructor",classListForm.getInstructor().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"ClassList.preferences",classListForm.getPreferences().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"ClassList.timetable",(classListForm.getTimetable()==null?false:classListForm.getTimetable().booleanValue()));
		    	UserData.setPropertyBoolean(httpSession,"ClassList.schedulePrintNote",classListForm.getSchedulePrintNote().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"ClassList.note",classListForm.getNote().booleanValue());
		    	if (classListForm.getCanSeeExams())
		    	    UserData.setPropertyBoolean(httpSession,"ClassList.exams",classListForm.getExams().booleanValue());
		    	UserData.setProperty(httpSession,"ClassList.sortBy", classListForm.getSortBy());
		    	UserData.setProperty(httpSession,"ClassList.filterAssignedRoom", classListForm.getFilterAssignedRoom());		    	
		    	UserData.setProperty(httpSession,"ClassList.filterInstructor", classListForm.getFilterInstructor());		    	
		    	UserData.setProperty(httpSession,"ClassList.filterManager", classListForm.getFilterManager());		
		    	UserData.setProperty(httpSession,"ClassList.filterIType", classListForm.getFilterIType());
		    	UserData.setPropertyInt(httpSession,"ClassList.filterDayCode", classListForm.getFilterDayCode());
		    	UserData.setPropertyInt(httpSession,"ClassList.filterStartSlot", classListForm.getFilterStartSlot());
		    	UserData.setPropertyInt(httpSession,"ClassList.filterLength", classListForm.getFilterLength());
		    	UserData.setPropertyBoolean(httpSession,"ClassList.sortByKeepSubparts", classListForm.getSortByKeepSubparts());
		    }
		    
	        String managerId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
	        TimetableManager manager = (new TimetableManagerDAO()).get(new Long(managerId));
			if (manager==null || !manager.canSeeTimetable(Session.getCurrentAcadSession(user), user))
				classListForm.setTimetable(null);
		    					
			
		    classListForm.setCollections(request, getClasses(classListForm, WebSolver.getClassAssignmentProxy(request.getSession())));
			Collection classes = classListForm.getClasses();
			if (classes.isEmpty()) {
			    ActionMessages errors = new ActionMessages();
			    errors.add("searchResult", new ActionMessage("errors.generic", "No records matching the search criteria were found."));
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
				httpSession.setAttribute(Constants.CRS_LST_SUBJ_AREA_IDS_ATTR_NAME, subjIds);
				httpSession.setAttribute(Constants.CRS_LST_CRS_NBR_ATTR_NAME, classListForm.getCourseNbr());
				//request.setAttribute("hash", "Search");
				
				if ("exportPdf".equals(action)) {
					File pdfFile = 
						(new PdfClassListTableBuilder())
						.pdfTableForClasses(
			    		        WebSolver.getClassAssignmentProxy(request.getSession()),
			    		        WebSolver.getExamSolver(request.getSession()),
			    		        classListForm, 
			    		        Web.getUser(request.getSession()));
					if (pdfFile!=null) request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+pdfFile.getName());
					//response.sendRedirect("temp/"+pdfFile.getName());
				}
				
				BackTracker.markForBack(
						request, 
						"classSearch.do?doit=Search&loadFilter=1"+ids+"&courseNbr="+classListForm.getCourseNbr(), 
						"Classes ("+names+
							(classListForm.getCourseNbr()==null || classListForm.getCourseNbr().length()==0?"":" "+classListForm.getCourseNbr())+
							")", 
						true, true);
				
			    return mapping.findForward("showClassList");
			}
		}
	
	
	public static void setupBasicFormData(ClassListFormInterface form, User user) throws Exception{

		form.setUserIsAdmin(user.isAdmin());

		Session session = Session.getCurrentAcadSession(user);
		if (!user.isAdmin()) {
			TimetableManager tm = TimetableManager.getManager(user);
			Department dept = null;
			Set depts = tm.departmentsForSession(session.getUniqueId());
			if (tm != null && depts != null && depts.size() > 0){
				String[] l = new String[depts.size()];
				int i = 0;
				for(Iterator it = depts.iterator(); it.hasNext();){
					dept = (Department) it.next();
					l[i] = dept.getUniqueId().toString();
					i++;
				}					
				form.setUserDeptIds(l);
			}
		}	
	}
	
	public static void setupGeneralFormFilters(HttpSession httpSession, ClassListFormInterface form){
		form.setSortBy(UserData.getProperty(httpSession, "ClassList.sortBy", ClassListForm.sSortByName));
		form.setFilterAssignedRoom(UserData.getProperty(httpSession, "ClassList.filterAssignedRoom", ""));
		form.setFilterManager(UserData.getProperty(httpSession, "ClassList.filterManager", ""));
		form.setFilterIType(UserData.getProperty(httpSession, "ClassList.filterIType", ""));
		form.setFilterDayCode(UserData.getPropertyInt(httpSession, "ClassList.filterDayCode", -1));
		form.setFilterStartSlot(UserData.getPropertyInt(httpSession, "ClassList.filterStartSlot", -1));
		form.setFilterLength(UserData.getPropertyInt(httpSession, "ClassList.filterLength", -1));
		form.setSortByKeepSubparts(UserData.getPropertyBoolean(httpSession, "ClassList.sortByKeepSubparts", true));
	
	}
	
	public static void setupClassListSpecificFormFilters(HttpSession httpSession, ClassListForm form){
		form.setDivSec(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.divSec", false)));	
		form.setLimit(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.limit", true)));
		form.setRoomLimit(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.roomLimit", true)));
		form.setManager(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.manager", true)));	
		form.setDatePattern(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.datePattern", true)));
		form.setTimePattern(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.timePattern", true)));
		form.setInstructor(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.instructor", true)));
		form.setPreferences(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.preferences", true)));
		form.setTimetable(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.timetable", true)));	
		form.setFilterInstructor(UserData.getProperty(httpSession, "ClassList.filterInstructor", ""));
		form.setSchedulePrintNote(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.schedulePrintNote", true)));
		form.setNote(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.note", false)));
		try {
		    User user = Web.getUser(httpSession);
		    TimetableManager manager = TimetableManager.getManager(user);
		    Session session = Session.getCurrentAcadSession(user);
		    if (manager.canSeeExams(session, user)) {
		        form.setCanSeeExams(Boolean.TRUE);
		        form.setExams(new Boolean(UserData.getPropertyBoolean(httpSession,"ClassList.exams", false)));
		    } else {
		        form.setCanSeeExams(Boolean.FALSE);
		    }
		} catch (Exception e) {}
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
			query.append("select c from Class_ as c ");
			
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
			query.append (" where c.schedulingSubpart in ( select ss2 from SchedulingSubpart as ss2 inner join ss2.instrOfferingConfig.instructionalOffering.courseOfferings as co2 ");
			query.append(" where co2.subjectArea.uniqueId in ( ");
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
	            query.append(" and co2.courseNbr ");
			    if (courseNbr.indexOf('*')>=0) {
		            query.append(" like '");
		            courseNbr = courseNbr.replace('*', '%').toUpperCase();
			    }
			    else {
		            query.append(" = '");
			    }
	            
	            query.append(courseNbr.toUpperCase());
	            query.append("'  ");
	        }
	        query.append(" and co2.isControl = true ) ");

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
	        
	        query.append(" and co.isControl = true ) ");
			Query q = hibSession.createQuery(query.toString());
			q.setFetchSize(1000);
			q.setCacheable(true);
	        TreeSet ts = new TreeSet(new ClassComparator(form.getSortBy(), classAssignmentProxy, form.getSortByKeepSubparts()));
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
				Class_ c = (Class_)i.next();
				Hibernate.initialize(c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs());
			}
			for (Iterator i=allClasses.iterator();i.hasNext();) {
				Class_ c = (Class_)i.next();
				Hibernate.initialize(c.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts());
			}
			for (Iterator i=allClasses.iterator();i.hasNext();) {
				Class_ c = (Class_)i.next();
				Hibernate.initialize(c.getSchedulingSubpart().getClasses());
			}

			Debug.debug(" --- Filter classes ---");
			for (Iterator i=q.list().iterator();i.hasNext();) {
				Class_ c = (Class_)i.next();
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
							for (Enumeration e=p.getRoomLocations().elements();e.hasMoreElements();) {
								RoomLocation r = (RoomLocation)e.nextElement();
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
				
				ts.add(c);
			}
			
			if (form.getInstructor().booleanValue() || form.getPreferences().booleanValue() || form.getTimePattern().booleanValue()) {
				Debug.debug("---- Load Instructors ---- ");
				for (Iterator i=ts.iterator();i.hasNext();) {
					Class_ c = (Class_)i.next();
					Hibernate.initialize(c.getClassInstructors());
				}
				for (Iterator i=ts.iterator();i.hasNext();) {
					Class_ c = (Class_)i.next();
					for (Iterator j=c.getClassInstructors().iterator();j.hasNext();) {
						ClassInstructor ci = (ClassInstructor)j.next();
						Hibernate.initialize(ci.getInstructor());
					}
				}
			}
			
			if (form.getPreferences().booleanValue() || form.getTimePattern().booleanValue()) {
				Debug.debug("---- Load Preferences ---- ");
				for (Iterator i=ts.iterator();i.hasNext();) {
					Class_ c = (Class_)i.next();
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
					Class_ c = (Class_)i.next();
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



}
