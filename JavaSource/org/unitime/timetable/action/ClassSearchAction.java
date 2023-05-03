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
package org.unitime.timetable.action;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.hibernate.query.Query;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.form.ClassListFormInterface;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.LearningManagementSystemInfo;
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
import org.unitime.timetable.util.IdValue;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.WebClassListTableBuilder;
import org.unitime.timetable.webutil.csv.CsvClassListTableBuilder;
import org.unitime.timetable.webutil.pdf.PdfClassListTableBuilder;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller, Zuzana Mullerova
 */
@Action(value="classSearch", results = {
		@Result(name = "showClassSearch", type = "tiles", location = "classSearch.tiles")
	})
@TilesDefinition(name = "classSearch.tiles", extend =  "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Classes"),
		@TilesPutAttribute(name = "body", value = "/user/classSearch.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
})
public class ClassSearchAction extends UniTimeAction<ClassListForm> {
	private static final long serialVersionUID = -4834379802757297961L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	private String doit;
	private String[] subjectAreaIds;
	private String courseNbr;
	private String loadFilter;
	private boolean showTable = false;
	
	public String getDoit() { return doit; }
	public void setDoit(String doit) { this.doit = doit; }
	public String[] getSubjectAreaIds() { return subjectAreaIds; }
	public void setSubjectAreaIds(String[] subjectAreaIds) { this.subjectAreaIds = subjectAreaIds; }
	public String getCourseNbr() { return courseNbr; }
	public void setCourseNbr(String courseNbr) { this.courseNbr = courseNbr; }
	public String getLoadFilter() { return loadFilter; }
	public void setLoadFilter(String loadFilter) { this.loadFilter = loadFilter; }
	public boolean isShowTable() { return showTable; }
	public void setShowTable(boolean showTable) { this.showTable = showTable; }

	public String execute() throws Exception {
		if (form == null)
			form = new ClassListForm();
		
    	if (getSubjectAreaIds() != null)
    		form.setSubjectAreaIds(getSubjectAreaIds());
    	if (getCourseNbr() != null)
    		form.setCourseNbr(getCourseNbr());
    	
		LookupTables.setupItypes(request,true);
		
    	if (MSG.actionSearchClasses().equals(doit) || "Search".equals(doit))
    		return searchClasses();
    	if (MSG.actionExportPdf().equals(doit))
    		return exportPdf();
    	if (MSG.actionExportCsv().equals(doit))
    		return exportCsv();

        BackTracker.markForBack(request, null, null, false, true);
        
    	sessionContext.checkPermission(Right.Classes);
        
	    Object sas = sessionContext.getAttribute(SessionAttribute.ClassesSubjectAreas);
	    Object cn = sessionContext.getAttribute(SessionAttribute.ClassesCourseNumber);
	    String subjectAreaIds = "";
	    String courseNbr = "";
	    
	    if ( (sas==null || sas.toString().trim().isEmpty()) && (cn==null || cn.toString().trim().isEmpty()) ) {
		    // use session variables from io search  
	        sas = sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
	        cn = sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);	        
	    }
	    
	    request.setAttribute(Department.EXTERNAL_DEPT_ATTR_NAME, Department.findAllExternal(sessionContext.getUser().getCurrentAcademicSessionId()));
        
		ClassSearchAction.setupGeneralFormFilters(sessionContext, form);
		ClassSearchAction.setupClassListSpecificFormFilters(sessionContext, form);

    	if (!sessionContext.hasPermission(Right.Examinations))
    		form.setExams(null);

    	form.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
    	
		if (sas == null && form.getSubjectAreas().size() == 1)
			sas = ((SubjectArea)form.getSubjectAreas().iterator().next()).getUniqueId().toString();
			
        if (Constants.ALL_OPTION_VALUE.equals(sas)) sas=null;

	    // Subject Areas are saved to the session - Perform automatic search
	    if(sas!=null && sas.toString().trim().length() > 0) {
	        subjectAreaIds = sas.toString();
	        
	        try {
	            
		        if(cn!=null && cn.toString().trim().length()>0)
		            courseNbr = cn.toString();
		        
		        Debug.debug("Subject Areas: " + subjectAreaIds);
		        Debug.debug("Course Number: " + courseNbr);
		        
		        form.setSubjectAreaIds(subjectAreaIds.split(","));
		        form.setCourseNbr(courseNbr);
		        
		        Integer maxSubjectsToSearch = ApplicationProperty.MaxSubjectsToSearchAutomatically.intValue();
		        if (maxSubjectsToSearch != null && maxSubjectsToSearch >= 0 && form.getSubjectAreaIds().length > maxSubjectsToSearch) {
		        	setShowTable(false);
		        	return "showClassSearch";
		        }
		        
				StringBuffer ids = new StringBuffer();
				StringBuffer names = new StringBuffer();
				StringBuffer subjIds = new StringBuffer();
				
				form.validate(this);
		    	if (hasFieldErrors()) {
		    		setShowTable(false);
					return "showClassSearch";
		    	}
		    	
				form.setClasses(ClassSearchAction.getClasses(form, WebSolver.getClassAssignmentProxy(request.getSession())));
				Collection classes = form.getClasses();
				if (classes.isEmpty()) {
					addFieldError("searchResult", MSG.errorNoRecords());
					setShowTable(false);
					return "showClassSearch";
				} else {
			        for (int i=0;i<form.getSubjectAreaIds().length;i++) {
						if (i>0) {
							names.append(","); 
							subjIds.append(",");
							}
						ids.append("&subjectAreaIds="+form.getSubjectAreaIds()[i]);
						subjIds.append(form.getSubjectAreaIds()[i]);
						names.append(((SubjectAreaDAO.getInstance()).get(Long.valueOf(form.getSubjectAreaIds()[i]))).getSubjectAreaAbbreviation());
					}
			        BackTracker.markForBack(
							request, 
							"classSearch.action?doit=Search&loadFilter=1&"+ids+"&courseNbr="+URLEncoder.encode(form.getCourseNbr(), "utf-8"),
							MSG.backClasses(names+(form.getCourseNbr()==null || form.getCourseNbr().length()==0?"":" "+form.getCourseNbr())), 
							true, true);
			        setShowTable(true);
			        return "showClassSearch";
				}
	        } catch (NumberFormatException nfe) {
	            Debug.error("Subject Area Ids session attribute is corrupted. Resetting ... ");
	            sessionContext.removeAttribute(SessionAttribute.ClassesSubjectAreas);
	            sessionContext.removeAttribute(SessionAttribute.ClassesCourseNumber);
	        }
	    }

		setShowTable(false);
		return "showClassSearch";
	}
	
	public String searchClasses() throws Exception {
		return performAction("searchClasses");
	}
	
	public String exportPdf() throws Exception {
		return performAction("exportPdf");
	}
	
	public String exportCsv() throws Exception {
		return performAction("exportCsv");
	}
	
	public String performAction(String action) throws Exception {
    	sessionContext.checkPermission(Right.Classes);
    	
    	request.setAttribute(Department.EXTERNAL_DEPT_ATTR_NAME, Department.findAllExternal(sessionContext.getUser().getCurrentAcademicSessionId()));
    	
    	if ("1".equals(getLoadFilter())) {
    		setupGeneralFormFilters(sessionContext, form);
    		setupClassListSpecificFormFilters(sessionContext, form);
    	} else {
    		sessionContext.getUser().setProperty("ClassList.divSec",form.getDivSec() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.demand",form.getDemand() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.demandIsVisible",form.getDemandIsVisible() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.limit",form.getLimit() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.snapshotLimit",form.getSnapshotLimit() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.roomLimit",form.getRoomLimit() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.manager",form.getManager() ? "1" : "0");
		   	sessionContext.getUser().setProperty("ClassList.datePattern",form.getDatePattern() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.timePattern",form.getTimePattern() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.instructor",form.getInstructor() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.instructorAssignment",form.getInstructorAssignment() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.lms", (form.getLms() == null ? null : form.getLms() ? "1" : "0"));
	    	sessionContext.getUser().setProperty("ClassList.preferences",form.getPreferences() ? "1" : "0");
	    	if (form.getTimetable() != null)
	    		sessionContext.getUser().setProperty("ClassList.timetable",form.getTimetable() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.schedulePrintNote",form.getSchedulePrintNote() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.note",form.getNote() ? "1" : "0");
	    	if (form.getExams() != null)
	    		sessionContext.getUser().setProperty("ClassList.exams",form.getExams() ? "1" : "0");
	    	
	    	sessionContext.getUser().setProperty("ClassList.fundingDepartment", (form.getFundingDepartment() == null ? "0" : form.getFundingDepartment() ? "1" : "0"));	    	
	    	sessionContext.getUser().setProperty("ClassList.sortBy", form.getSortBy());
	    	sessionContext.getUser().setProperty("ClassList.filterAssignedRoom", form.getFilterAssignedRoom());		    	
	    	sessionContext.getUser().setProperty("ClassList.filterInstructor", form.getFilterInstructor());		    	
	    	sessionContext.getUser().setProperty("ClassList.filterManager", form.getFilterManager());		
	    	sessionContext.getUser().setProperty("ClassList.filterIType", form.getFilterIType());
	    	sessionContext.getUser().setProperty("ClassList.filterDayCode", String.valueOf(form.getFilterDayCode()));
	    	sessionContext.getUser().setProperty("ClassList.filterStartSlot", String.valueOf(form.getFilterStartSlot()));
	    	sessionContext.getUser().setProperty("ClassList.filterLength", String.valueOf(form.getFilterLength()));
	    	sessionContext.getUser().setProperty("ClassList.sortByKeepSubparts", form.getSortByKeepSubparts() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.showCrossListedClasses", form.getShowCrossListedClasses() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.filterNeedInstructor",form.getFilterNeedInstructor() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassList.includeCancelledClasses",form.getIncludeCancelledClasses() ? "1" : "0");
	    }
    	    	
    	if (!sessionContext.hasPermission(Right.Examinations))
    		form.setExams(null);
  
		if (!ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) 
			form.setFundingDepartment(false);

    	form.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
    	
    	form.validate(this);
    	if (hasFieldErrors()) {
    		setShowTable(false);
			return "showClassSearch";
    	}
    	
    	form.setClasses(getClasses(form, WebSolver.getClassAssignmentProxy(request.getSession())));
    	Collection classes = form.getClasses();
    	if (classes.isEmpty()) {
			addFieldError("searchResult", MSG.errorNoRecords());
			setShowTable(false);
			return "showClassSearch";
		} else {
			StringBuffer ids = new StringBuffer();
			StringBuffer names = new StringBuffer();
			StringBuffer subjIds = new StringBuffer();
			for (int i=0;i<form.getSubjectAreaIds().length;i++) {
				if (i>0) {
					names.append(","); 
					subjIds.append(",");
					}
				ids.append("&subjectAreaIds="+form.getSubjectAreaIds()[i]);
				subjIds.append(form.getSubjectAreaIds()[i]);
				names.append(((SubjectAreaDAO.getInstance()).get(Long.valueOf(form.getSubjectAreaIds()[i]))).getSubjectAreaAbbreviation());
			}
			sessionContext.setAttribute(SessionAttribute.ClassesSubjectAreas, subjIds);
			sessionContext.setAttribute(SessionAttribute.ClassesCourseNumber, form.getCourseNbr());
			
			if ("exportPdf".equals(action)) {
	    		OutputStream out = ExportUtils.getPdfOutputStream(response, "classes");
				
				new PdfClassListTableBuilder().pdfTableForClasses(out,
						WebSolver.getClassAssignmentProxy(request.getSession()),
			    		WebSolver.getExamSolver(request.getSession()),
			    		form, 
			    		sessionContext);
				
				out.flush(); out.close();
				
				return null;
			}
			
			if ("exportCsv".equals(action)) {
	    		PrintWriter out = ExportUtils.getCsvWriter(response, "classes");
				
				new CsvClassListTableBuilder().csvTableForClasses(out,
						WebSolver.getClassAssignmentProxy(request.getSession()),
			    		WebSolver.getExamSolver(request.getSession()),
			    		form, 
			    		sessionContext);
				
				out.flush(); out.close();
				
				return null;
			}

			BackTracker.markForBack(
					request, 
					"classSearch.action?doit=Search&loadFilter=1"+ids+"&courseNbr="+URLEncoder.encode(form.getCourseNbr(), "utf-8"), 
					MSG.backClasses(names+(form.getCourseNbr()==null || form.getCourseNbr().length()==0?"":" "+form.getCourseNbr())), 
					true, true);
			setShowTable(true);
			return "showClassSearch";
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
		form.setSnapshotLimit("1".equals(sessionContext.getUser().getProperty("ClassList.snapshotLimit", "1")));
		form.setRoomLimit("1".equals(sessionContext.getUser().getProperty("ClassList.roomLimit", "1")));
		form.setManager("1".equals(sessionContext.getUser().getProperty("ClassList.manager", "1")));	
		form.setDatePattern("1".equals(sessionContext.getUser().getProperty("ClassList.datePattern", "1")));
		form.setTimePattern("1".equals(sessionContext.getUser().getProperty("ClassList.timePattern", "1")));
		form.setInstructor("1".equals(sessionContext.getUser().getProperty("ClassList.instructor", "1")));
		form.setInstructorAssignment("1".equals(sessionContext.getUser().getProperty("ClassList.instructorAssignment", "1")));
		if (LearningManagementSystemInfo.isLmsInfoDefinedForSession(sessionContext.getUser().getCurrentAcademicSessionId()))
			form.setLms("1".equals(sessionContext.getUser().getProperty("ClassList.lms", "1")));
		else
			form.setLms(null);
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
		form.setFilterNeedInstructor("1".equals(sessionContext.getUser().getProperty("ClassList.filterNeedInstructor", "0")));
		form.setIncludeCancelledClasses("1".equals(sessionContext.getUser().getProperty("ClassList.includeCancelledClasses", "1")));
		form.setFundingDepartment("1".equals(sessionContext.getUser().getProperty("ClassList.fundingDepartment", "0")));
	}
	
    public static Set getClasses(ClassListFormInterface form, ClassAssignmentProxy classAssignmentProxy) {
		org.hibernate.Session hibSession = (InstructionalOfferingDAO.getInstance()).getSession();

		
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
			String courseNbr = form.getCourseNbr();
			if (ApplicationProperty.CourseOfferingTitleSearch.isTrue() && courseNbr != null && courseNbr.length() > 2) {
				if (courseNbr.indexOf('*') >= 0) {
					query.append(" and (co.courseNbr like :courseNbr or lower(co.title) like lower(:courseNbr))");
				} else {
					query.append(" and (co.courseNbr = :courseNbr or lower(co.title) like ('%' || lower(:courseNbr) || '%'))");
				}
			} else if (form.getCourseNbr() != null && form.getCourseNbr().length() > 0){
				if (courseNbr.indexOf('*') >= 0) {
					query.append(" and co.courseNbr like :courseNbr ");
				} else {
					query.append(" and co.courseNbr = :courseNbr ");
				}
	        }

	        if (doFilterManager) {
	        	if (filterManager.longValue()<0) { //all departmental
	        		query.append(" and c.managingDept = co.subjectArea.department");
	        	} else {
	        		query.append(" and c.managingDept.uniqueId = "+filterManager);
	        	}
	        }
			
	        if (!form.getShowCrossListedClasses()) {
	        	query.append(" and co.isControl = true ");
	        }
	        if (!form.getIncludeCancelledClasses() || form.getFilterNeedInstructor()) {
	        	query.append(" and c.cancelled = false");
	        }
	        if (form.getFilterNeedInstructor()) {
	        	query.append(" and (select sum(tr.teachingRequest.nbrInstructors) from TeachingClassRequest tr where tr.assignInstructor = true and  tr.teachingClass = c) > 0");
	        }
			Query<Object[]> q = hibSession.createQuery(query.toString(), Object[].class);
			q.setFetchSize(1000);
			if (courseNbr != null && courseNbr.length() > 0) {
				if (ApplicationProperty.CourseOfferingNumberUpperCase.isTrue())
	            	courseNbr = courseNbr.toUpperCase();
				q.setParameter("courseNbr", courseNbr.replace('*', '%'));
			}
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
			
			Debug.debug(" --- Filter classes ---");
			for (Object[] o: q.list()) {
				Class_ c = (Class_)o[0];
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
						continue;
					}
				}
				
				if (doFilterIType) {
				    ItypeDesc itype = c.getSchedulingSubpart().getItype();
				    boolean match=false;
				    while (!match && itype!=null) {
				        match = itype.getItype().equals(filterIType);
				        itype = itype.getParent();
				    }
					if (!match) {
						continue;
					}
				}
				
				if (doFilterAssignedTime) {
					try {
						Assignment a = classAssignmentProxy.getAssignment(c);
						if (a==null) {
							continue;
						}
						Placement p = a.getPlacement();
						if (p==null) {
							continue;
						}
						TimeLocation t = p.getTimeLocation();
						if (t==null) {
							continue;
						}
						boolean overlap = t.shareDays(filterAssignedTime) && t.shareHours(filterAssignedTime);
						if (!overlap) {
							continue;
						}
					} catch (Exception e) {
						continue;
					}
				}
				
				if (doFilterAssignedRoom) {
					try {
						Assignment a = classAssignmentProxy.getAssignment(c);
						if (a==null) {
							continue;
						}
						Placement p = a.getPlacement();
						if (p==null || p.getNrRooms()<=0) {
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
							continue;
						}
					} catch (Exception e) {
						continue;
					}
				}
				
				ts.add(o);
			}
			
			long eTime = new java.util.Date().getTime();
	        Debug.debug("fetch time = " + (eTime - sTime));
	        Debug.debug("rows = " + ts.size());
	        return (ts);
	        
	        
		} else {
	        	return (new TreeSet());
	    }

    }
    
	
	public List<IdValue> getManagers() {
		List<IdValue> ret = new ArrayList<IdValue>();
		ret.add(new IdValue(null, MSG.dropManagerAll()));
		ret.add(new IdValue(-2l, MSG.dropDeptDepartment()));
		for (Department d: (TreeSet<Department>)request.getAttribute(Department.EXTERNAL_DEPT_ATTR_NAME))
			ret.add(new IdValue(d.getUniqueId(), d.getManagingDeptLabel()));
		return ret;
	}
	
	public String printTable() throws Exception {
		new WebClassListTableBuilder().htmlTableForClasses(
				sessionContext,
				getClassAssignmentService().getAssignment(),
				getExaminationSolverService().getSolver(),
				form,
				getPageContext().getOut(),
				request.getParameter("backType"),
				request.getParameter("backId")
			);
		return "";
	}
}
