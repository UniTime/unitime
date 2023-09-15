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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.DistributionPrefsForm;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
@Action(value="distributionPrefs", results = {
		@Result(name = "list", type = "tiles", location = "distributionPrefs.tiles"),
		@Result(name = "add", type = "tiles", location = "addDistributionPref.tiles"),
		@Result(name = "edit", type = "tiles", location = "editDistributionPref.tiles")
	})
@TilesDefinitions(value = {
		@TilesDefinition(name = "distributionPrefs.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Distribution Preferences"),
				@TilesPutAttribute(name = "body", value = "/user/distributionPrefs.jsp"),
				@TilesPutAttribute(name = "showNavigation", value = "true")
		}),
		@TilesDefinition(name = "addDistributionPref.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Add Distribution Preference"),
				@TilesPutAttribute(name = "body", value = "/user/distributionPrefs.jsp"),
				@TilesPutAttribute(name = "showNavigation", value = "true")
		}),
		@TilesDefinition(name = "editDistributionPref.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Edit Distribution Preference"),
				@TilesPutAttribute(name = "body", value = "/user/distributionPrefs.jsp"),
				@TilesPutAttribute(name = "showNavigation", value = "true")
		})
	})
public class DistributionPrefsAction extends UniTimeAction<DistributionPrefsForm> {
	private static final long serialVersionUID = 1926148300437111812L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static GwtConstants CONST = Localization.create(GwtConstants.class);
	protected String op2;
	protected String reloadId;
	protected String reloadCause;
	protected String deleteId;
	protected String deleteType;
	protected String distPrefId;
	
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }
	public String getReloadId() { return reloadId; }
	public void setReloadId(String reloadId) { this.reloadId = reloadId; }
	public String getReloadCause() { return reloadCause; }
	public void setReloadCause(String reloadCause) { this.reloadCause = reloadCause; }
	public String getDeleteId() { return deleteId; }
	public void setDeleteId(String deleteId) { this.deleteId = deleteId; }
	public String getDeleteType() { return deleteType; }
	public void setDeleteType(String deleteType) { this.deleteType = deleteType; }
	public String getDp() { return distPrefId; }
	public void setDp(String distPrefId) { this.distPrefId = distPrefId; }
	
    /** 
     * Method execute
     */
    public String execute() throws Exception {
    	if (form == null) {
    		form = new DistributionPrefsForm();
    	}
    	
    	sessionContext.checkPermission(Right.DistributionPreferences);
    	
		if (op == null) op = form.getOp();
        if (op2 != null && !op2.isEmpty()) op = op2;

		if (op==null || op.trim().isEmpty()) {
			op = "view";		    
		}
		
    	List<ComboBoxLookup> subjects = new ArrayList<ComboBoxLookup>();
        subjects.add(new ComboBoxLookup(Constants.BLANK_OPTION_LABEL, Constants.BLANK_OPTION_VALUE));
        subjects.add(new ComboBoxLookup(Constants.ALL_OPTION_LABEL, Constants.ALL_OPTION_VALUE));
        TreeSet<SubjectArea> userSubjectAreas = SubjectArea.getUserSubjectAreas(sessionContext.getUser());
        for (SubjectArea sa: userSubjectAreas)
        	subjects.add(new ComboBoxLookup(sa.getSubjectAreaAbbreviation(), sa.getUniqueId().toString()));
        form.setFilterSubjectAreas(subjects);
		
		if ("DistTypeChange".equals(request.getParameter("op2")) || "GroupingChange".equals(request.getParameter("op2")))
			op = "reload pref";
		
	    Debug.debug("op: " + op);
		Debug.debug("deleteId: " + deleteId);
		Debug.debug("deleteType: " + deleteType);
		Debug.debug("reloadCause: " + reloadCause);
		Debug.debug("reloadId: " + reloadId);
		Debug.debug("distPrefId: " + distPrefId);
		
		if (reloadCause!=null && reloadCause.length()>0) op = "reload";
		
        // Cancel - Display blank form
        if (MSG.actionCancel().equals(op)) {
        	form.reset();
        	if (BackTracker.doBack(request, response)) return null;
            op = "view"; //in case no back is available
        }

		// Set lookup tables lists
        List<ComboBoxLookup> subjectAreaList = setupSubjectAreas(); // Subject Areas

        // Add / Update distribution pref
        if (MSG.actionSaveNewDistributionPreference().equals(op) || MSG.actionUpdateDistributionPreference().equals(op)) {
            Debug.debug("Saving distribution pref ...");
    		form.setOp(op);
    		form.validate(this);
            if (!hasFieldErrors()) {
            	try {
           			doAddOrUpdate();
           			if (form.getDistPrefId()!=null) {
           				request.setAttribute("backType", "PreferenceGroup");
           				request.setAttribute("backId", form.getDistPrefId());
           			}
	    	        form.reset();
	    	        if (BackTracker.doBack(request, response)) return null;
		            op = "view"; //in case no back is available
           		} catch (Exception e) {
           			Debug.error(e);
           			addFieldError("classes", e.getMessage());
	            }
	        }
        }
        
        // Delete distribution object / pref
        if (MSG.actionDeleteDistributionPreference().equals(op)) {
            if ("distObject".equals(deleteType)) {
                form.removeFromLists(Integer.parseInt(deleteId));
            }
            if("distPref".equals(deleteType)) {
                doDelete(form.getDistPrefId());
                form.reset();            
                if (BackTracker.doBack(request, response)) return null;
	            op = "view"; //in case no back is available
            }
        }
        
        // Add new class - redirect from SchedulingSubpartEdit / ClassEdit
        if (MSG.actionAddDistributionPreference().equals(op) || MSG.actionAddNewDistributionPreference().equals(op)) {
            Debug.debug("Adding new Class via redirect ...");
	        form.setDistType(Preference.BLANK_PREF_VALUE);
	        form.setGrouping(Preference.BLANK_PREF_VALUE);
	        if (request.getParameter("classId") != null) {
	        	Class_ clazz = Class_DAO.getInstance().get(Long.valueOf(request.getParameter("classId")));
	        	if (clazz != null) {
	        		form.addToSubjectArea(clazz.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().getUniqueId().toString());
		        	form.addToItype(clazz.getSchedulingSubpart().getUniqueId().toString());
		        	form.addToCourseNbr(clazz.getSchedulingSubpart().getControllingCourseOffering().getUniqueId().toString());
		        	form.addToClassNumber(clazz.getUniqueId().toString());
			        request.setAttribute("addedClass", ""+(form.getSubjectArea().size()-1));
	        	}
	        } else if (request.getParameter("subpartId") != null) {
	        	SchedulingSubpart subpart = SchedulingSubpartDAO.getInstance().get(Long.valueOf(request.getParameter("subpartId")));
	        	if (subpart != null) {
	        		form.addToSubjectArea(subpart.getControllingCourseOffering().getSubjectArea().getUniqueId().toString());
		        	form.addToItype(subpart.getUniqueId().toString());
		        	form.addToCourseNbr(subpart.getControllingCourseOffering().getUniqueId().toString());
		        	form.addToClassNumber("-1");
			        request.setAttribute("addedClass", ""+(form.getSubjectArea().size()-1));
	        	}
	        } else if (request.getAttribute("subjectAreaId")!=null) {
	        	form.addToSubjectArea(request.getAttribute("subjectAreaId").toString());
	        	form.addToItype(request.getAttribute("schedSubpartId").toString());
	        	form.addToCourseNbr(request.getAttribute("courseOffrId").toString());
	        	form.addToClassNumber(request.getAttribute("classId").toString());
		        request.setAttribute("addedClass", ""+(form.getSubjectArea().size()-1));
	        } else {
	        	String subjectAreaId = form.getFilterSubjectAreaId();
	        	if (Constants.ALL_OPTION_VALUE.equals(subjectAreaId)) subjectAreaId = null;
	        	form.addNewClass(subjectAreaId);
	        }
        }
        
        // Add new class
        if (MSG.actionAddClassToDistribution().equals(op)) {
            Debug.debug("Adding new Class ...");
            String subjAreaId = null;
            if(subjectAreaList.size()==1)
            	subjAreaId = subjectAreaList.get(0).getValue();
            
            form.addNewClass(subjAreaId);
    	    request.setAttribute("addedClass", ""+(form.getSubjectArea().size()-1));
        }
        
        if (MSG.actionSearchDistributionPreferences().equals(op) || MSG.actionExportPdf().equals(op) || MSG.actionExportCsv().equals(op)) {
        	String subjectAreaId = form.getFilterSubjectAreaId();
        	String courseNbr = form.getFilterCourseNbr();
        	if (subjectAreaId!=null && subjectAreaId.length()>0)
        		sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjectAreaId);
        	else
        		sessionContext.removeAttribute(SessionAttribute.OfferingsSubjectArea);
        	if (courseNbr!=null && courseNbr.length()>0)
        		sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, courseNbr);
        	else
        		sessionContext.removeAttribute(SessionAttribute.OfferingsCourseNumber);
        	
        	if (MSG.actionExportPdf().equals(op))
        		op="export"; 
        	else if (MSG.actionExportCsv().equals(op))
        		op="export-csv"; 
        	else 
        		op="view";
        }

        // Load Distribution Pref
        if (op!=null && (op.equals("view") || op.equals("export") || op.equals("export-csv")) && distPrefId!=null && distPrefId.trim().length()>0) {
            Debug.debug("Loading dist pref - " + distPrefId);
            form.reset();
            doLoad(distPrefId);
        }
        
        // Reload 
        if (op!=null && op.equals("reload")) {
            // Subject area changed
            if (reloadCause!=null && reloadCause.equals("subjectArea")) {
	            int index = Integer.parseInt(reloadId);
	            Debug.debug("subj area changed ... " + reloadId + " - " + form.getSubjectArea(index));
	
	            // Reset values to blank
	            form.setCourseNbr(index, Preference.BLANK_PREF_VALUE);
	            form.setItype(index, Preference.BLANK_PREF_VALUE);
	            form.setClassNumber(index, Preference.BLANK_PREF_VALUE);
	        }
            
            // Move Distribution object up one level
            if (reloadCause!=null && reloadCause.equals("moveUp")) {
	            int index = Integer.parseInt(reloadId);
	            Debug.debug("moving up ... " + reloadId);
	            form.swap(index, index-1);
            }
            
            // Move Distribution object down one level
            if (reloadCause!=null && reloadCause.equals("moveDown")) {
	            int index = Integer.parseInt(reloadId);
	            Debug.debug("moving down ... " + reloadId);
	            form.swap(index, index+1);
            }
        }

        // Set up lookup list
        setLookupLists(subjectAreaList); // Distribution Objects

        if (form.getDistType()!=null && !form.getDistType().equals(Preference.BLANK_PREF_VALUE)) {
        	Vector prefs = new Vector();
        	DistributionType dist = (new DistributionTypeDAO().get(Long.valueOf(form.getDistType())));
        	form.setDescription(dist.getDescr());
        	boolean containsPref = false; 
        	for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList()) {
        		if (dist.isAllowed(pref)) {
        			prefs.addElement(pref);
        			if (form.getPrefLevel()!=null && !form.getPrefLevel().equals(Preference.BLANK_PREF_VALUE) && pref.getPrefId().equals(Integer.valueOf(form.getPrefLevel()))) containsPref = true;
        		}
        	}
        	if (!containsPref)
        		form.setPrefLevel(Preference.BLANK_PREF_VALUE);
        	if (prefs.size()==1)
        		form.setPrefLevel(((PreferenceLevel)prefs.firstElement()).getPrefId().toString());
        	request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, prefs);
        	LookupTables.setupDistribTypes(request, sessionContext, dist);
        } else {
        	request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, new Vector(0));
        	form.setDescription("");
            LookupTables.setupDistribTypes(request, sessionContext, null);
        }	    
        
        if (form.getGrouping()!=null && !form.getGrouping().equals(Preference.BLANK_PREF_VALUE)) {
        	form.setGroupingDescription(form.getStructure().getDescription());
        }

        if ("export".equals(op) && (form.getDistPrefId()==null || form.getDistPrefId().length()==0)) {
        	OutputStream out = ExportUtils.getPdfOutputStream(response, "distprefs");
            new DistributionPrefsTableBuilder().getAllDistPrefsTableForCurrentUserAsPdf(out, sessionContext, form.getFilterSubjectAreaId(), form.getFilterCourseNbr());
            out.flush(); out.close();
            return null;
        }
        
        if ("export-csv".equals(op) && (form.getDistPrefId()==null || form.getDistPrefId().length()==0)) {
        	PrintWriter out = ExportUtils.getCsvWriter(response, "distprefs");
            new DistributionPrefsTableBuilder().getAllDistPrefsTableForCurrentUserAsCsv(out, sessionContext, form.getFilterSubjectAreaId(), form.getFilterCourseNbr());
            out.flush(); out.close();
            return null;
        }
        
        request.setAttribute(DistributionPrefsForm.LIST_SIZE_ATTR, (form.getSubjectArea() == null ? 0 : form.getSubjectArea().size()-1));

        if ("view".equals(op) && (form.getDistPrefId()==null || form.getDistPrefId().isEmpty())) {
        	String subject = (String)sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
        	if (subject != null && subject.indexOf(',') >= 0) subject = subject.substring(0, subject.indexOf(','));
        	form.setFilterSubjectAreaId(subject);
        	form.setFilterCourseNbr((String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber));
        	
        	DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
        	if (form.getFilterSubjectAreaId()==null) {
        	    if (sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent))
        	        form.setFilterSubjectAreaId(Constants.BLANK_OPTION_VALUE);
        	    else
        	        form.setFilterSubjectAreaId(Constants.ALL_OPTION_VALUE);        	        
        	}        	
        	
        	String html = tbl.getAllDistPrefsTableForCurrentUser(request, sessionContext, form.getFilterSubjectAreaId(), form.getFilterCourseNbr());
        	if (html!=null)
        		request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);
            BackTracker.markForBack(
            		request,
            		"distributionPrefs.action",
            		MSG.backDistributionPreferences(),
            		true, true);
            return "list";
        }
        
        return (form.getDistPrefId()==null || form.getDistPrefId().length()==0?"add":"edit");
    }

    /**
     * Get Subject Areas for an acad session for a user and store it in request object
     * Gets all subject areas for LLR Manager, Lab Manager and Admin
     */
    public List<ComboBoxLookup> setupSubjectAreas() throws Exception {
        Set subjectAreas = SubjectArea.getUserSubjectAreas(sessionContext.getUser());
        
        if (subjectAreas==null) return null;
        
        List<ComboBoxLookup> v = new ArrayList<ComboBoxLookup>(subjectAreas.size());
        for (Iterator i=subjectAreas.iterator();i.hasNext();) {
           	SubjectArea sa = (SubjectArea)i.next();
           	v.add(new ComboBoxLookup(sa.getSubjectAreaAbbreviation(),sa.getUniqueId().toString()));
    	}
           
        return v;
    }
    
    /**
     * @param index
     */
    private void setLookupLists(List<ComboBoxLookup> subjectAreaList) {
        
        int ct = (form.getSubjectArea() == null ? 0 : form.getSubjectArea().size());
        boolean suffix = ApplicationProperty.DistributionsShowClassSufix.isTrue();
        for(int index=0; index<ct; index++) {
            
	        String subjectAreaId = form.getSubjectArea(index);
	        String courseNbr = form.getCourseNbr(index);
	        String subpart = form.getItype(index);
	        String classNumber = form.getClassNumber(index);
	        
	        Vector crsNumList = null;
	        Vector subpartList = null;
	        Vector classNumList = null;
	        
	        // Process subject area selection
	        if(subjectAreaId!=null) {
	            if(subjectAreaId.equals(Preference.BLANK_PREF_VALUE) || subjectAreaId.equals(Constants.ALL_OPTION_VALUE)) {
	    	        crsNumList = new Vector();
	    	        subpartList = new Vector();
	    	        classNumList = new Vector();
	            }
	            else {
	        		
	        		StringBuffer query = new StringBuffer();
	        		query.append("select co.uniqueId, co.courseNbr, co.title ");
       		        query.append("  from InstructionalOffering as io , CourseOffering co ");
	        		query.append(" where co.subjectArea.uniqueId = :subjectAreaId ");
	        		query.append("       and io.uniqueId = co.instructionalOffering.uniqueId ");
                    query.append("       and io.notOffered = false ");
                    query.append("       and co.isControl = true ");
                    query.append(" order by co.courseNbr ");
                    
	                InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        		org.hibernate.Session hibSession = idao.getSession();

	        		Query q = hibSession.createQuery(query.toString());
	        		q.setFetchSize(200);
	        		q.setCacheable(true);
	        		q.setLong("subjectAreaId", Long.parseLong(subjectAreaId));
	                
	        		List result = q.list();
	                crsNumList = new Vector();
	        		if(result.size()>0) {
	        		    for(int i=0; i<result.size(); i++) {
	        		        Object[] a = (Object[]) result.get(i);
	        		        ComboBoxLookup cbl = new ComboBoxLookup(
	        		                (a[1].toString() + " - " + (a[2] == null?"":a[2].toString())), a[0].toString());
	        		        crsNumList.addElement(cbl);
	        		    }
	        		    
	        		    // Only one record - select it to save time and one more click
	        		    if(result.size()==1) {
	        		        ComboBoxLookup cbl = (ComboBoxLookup) crsNumList.elementAt(0);
	        		        form.setCourseNbr(index, cbl.getValue());
	        		        courseNbr = form.getCourseNbr(index);
	        		    }
	        		}
	                
	    	        // Process course number selection
	                if(courseNbr.equals(Preference.BLANK_PREF_VALUE)) {
	                    subpartList = new Vector();
		    	        classNumList = new Vector();
	                }
	                else {
	                    query = new StringBuffer();
	                    query.append(" select distinct s ");
	                    query.append("   from CourseOffering co, ");
	                    query.append("        InstructionalOffering io, ");
	                    query.append("        InstrOfferingConfig ioc, ");
	                    query.append("        SchedulingSubpart s ");
	                    query.append("  where co.uniqueId=:courseNbr ");
	                    query.append("    and co.instructionalOffering.uniqueId=io.uniqueId ");
	                    query.append("    and ioc.instructionalOffering.uniqueId=io.uniqueId ");
	                    query.append("    and s.instrOfferingConfig.uniqueId=ioc.uniqueId ");	                    
	                    
		        		q = hibSession.createQuery(query.toString());
		        		q.setFetchSize(200);
		        		q.setCacheable(true);
		        		q.setLong("courseNbr", Long.parseLong(courseNbr));
		                
		        		result = new Vector(q.list());
		        		if(result!=null && result.size()>0) {
		        			Collections.sort(result, new SchedulingSubpartComparator(subjectAreaId==null || subjectAreaId.length()==0?null:Long.valueOf(subjectAreaId)));
		        			subpartList = new Vector();
		        		    for(int i=0; i<result.size(); i++) {
		        		        SchedulingSubpart a = (SchedulingSubpart)result.get(i);
		        		        String ssid = a.getUniqueId().toString();
		        		        String name = a.getItype().getAbbv();
		        		        String sufix = a.getSchedulingSubpartSuffix();
		        		        while (a.getParentSubpart()!=null) {
		        		        	name = "\u00A0\u00A0\u00A0\u00A0"+name;
		        		        	a = a.getParentSubpart();
		        		        }
		        		        if (a.getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().size()>1)
		        		        	name += " ["+a.getInstrOfferingConfig().getName()+"]";
		        		        ComboBoxLookup cbl = new ComboBoxLookup(name+(sufix==null || sufix.length()==0?"":" ("+sufix+")"), ssid);
		        		        subpartList.addElement(cbl);
		        		    }
		        		    
		        		    // Only one record - select it to save time and one more click
		        		    if(subparts.size()==1) {
		        		        ComboBoxLookup cbl = (ComboBoxLookup) subpartList.elementAt(0);
		        		        form.setItype(index, cbl.getValue());
		        		        subpart = form.getItype(index);
		        		    }
		        		}
	                    
	                    if (subpartList==null || subpartList.size()==0) {
	                        subpartList = new Vector();
	                        addFieldError("classes", MSG.errorNoSupbartsExist());
	                    }
	                    
	                    // Process subpart selection
		                if(subpart.equals(Preference.BLANK_PREF_VALUE)) {
		                    classNumList = new Vector();
		                }
	                    else {
		                    query = new StringBuffer();
		                    query.append(" select distinct c ");
		                    query.append("   from SchedulingSubpart s, ");
		                    query.append("        Class_ c ");
		                    query.append("  where s.uniqueId=:itype ");
		                    query.append("    and s.uniqueId=c.schedulingSubpart.uniqueId ");	                    
		                    
			        		q = hibSession.createQuery(query.toString());
			        		q.setFetchSize(200);
			        		q.setCacheable(true);
			        		q.setLong("itype", Long.parseLong(subpart));
			        		
			        		result = q.list();
			        		if(result!=null && result.size()>0) {
                                Collections.sort(result, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
                                
			        		    if(classNumber.equals(Preference.BLANK_PREF_VALUE)) 
			        		        form.setClassNumber(index, DistributionPrefsForm.ALL_CLASSES_SELECT);
			        		        
	                            classNumList = new Vector();
				        		classNumList.addElement(new ComboBoxLookup(MSG.dropDistrPrefAll(), "-1"));
			        		    for(int i=0; i<result.size(); i++) {
			        		    	Class_ clazz = (Class_)result.get(i);
			        		        ComboBoxLookup cbl = new ComboBoxLookup(clazz.getSectionNumberString(), clazz.getUniqueId().toString());
			        		    	if (suffix) {
			        		    		String extId = clazz.getClassSuffix(clazz.getSchedulingSubpart().getControllingCourseOffering());
			        		    		if (extId != null && !extId.isEmpty() && !extId.equalsIgnoreCase(clazz.getSectionNumberString()))
			        		    			cbl = new ComboBoxLookup(clazz.getSectionNumberString() + " - " + extId, clazz.getUniqueId().toString());
			        		    	}
			        		        classNumList.addElement(cbl);
			        		    }
			        		}
			        		else {
    	                        classNumList = new Vector();
    	                        addFieldError("classes", MSG.errorNoClassesExist());
	                        }
	                    }
	                }
	            }
	        }

	        // Set drop down lists for a row
            request.setAttribute( 
                    DistributionPrefsForm.SUBJ_AREA_ATTR_LIST + index, subjectAreaList );
            request.setAttribute( 
                    DistributionPrefsForm.CRS_NUM_ATTR_LIST + index, crsNumList );
            request.setAttribute( 
                    DistributionPrefsForm.ITYPE_ATTR_LIST + index, subpartList );
            request.setAttribute( 
                    DistributionPrefsForm.CLASS_NUM_ATTR_LIST + index, classNumList );
        }
    }
    
    /**
     * Loads the form with the data for the distribution pref selected
     * @param distPrefId
     */
    private void doLoad(String distPrefId ) {
 
        // Get distribution pref info
        DistributionPrefDAO dpDao = new DistributionPrefDAO();
        DistributionPref dp = dpDao.get(Long.valueOf(distPrefId));
        form.setDistType(dp.getDistributionType().getUniqueId().toString());
        form.setStructure(dp.getStructure());
        form.setOwner(dp.getOwner().getUniqueId().toString());
        form.setPrefLevel(dp.getPrefLevel().getPrefId().toString());
        form.setDistPrefId(distPrefId);
        
        for (DistributionObject dObj: new TreeSet<DistributionObject>(dp.getDistributionObjects())) {
        	if (dObj.getPrefGroup() instanceof Class_) {
        		Class_ c = (Class_)dObj.getPrefGroup();
        		CourseOffering co = c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
        		form.getSubjectArea().add(co.getSubjectArea().getUniqueId().toString());
                form.getCourseNbr().add(co.getUniqueId().toString());
                form.getItype().add(c.getSchedulingSubpart().getUniqueId().toString());
                form.getClassNumber().add(c.getUniqueId().toString());
        	} else if (dObj.getPrefGroup() instanceof SchedulingSubpart) {
        		SchedulingSubpart ss = (SchedulingSubpart)dObj.getPrefGroup();
        		CourseOffering co = ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
        		form.getSubjectArea().add(co.getSubjectArea().getUniqueId().toString());
                form.getCourseNbr().add(co.getUniqueId().toString());
                form.getItype().add(ss.getUniqueId().toString());
                form.getClassNumber().add("-1");
        	}
        }            
    }
    
    /**
     * Add new distribution pref
     * @param httpSession
     * @param form
     */
    private void doAddOrUpdate() throws Exception {

        String distPrefId = form.getDistPrefId();
        List saList = form.getSubjectArea();
        List suList = form.getItype();
        List clList = form.getClassNumber();            
        
        // Create distribution preference
        DistributionPref dp = null;
        Department oldOwner = null;
        DistributionPrefDAO dpDao = new DistributionPrefDAO();
        Transaction tx = null;
        org.hibernate.Session hibSession = dpDao.getSession();
        HashSet relatedInstructionalOfferings = new HashSet();
        
        try {
        	tx = hibSession.beginTransaction();
        	
        	if(distPrefId!=null && distPrefId.trim().length()>0) {
        		Long distPrefUid = Long.valueOf(distPrefId);
        		if(distPrefUid.longValue()>0) {
        			dp = dpDao.get(distPrefUid, hibSession);
        			Set s = dp.getDistributionObjects();
        			for (Iterator i=s.iterator();i.hasNext();) {
        				DistributionObject dObj = (DistributionObject)i.next();
        				PreferenceGroup pg = dObj.getPrefGroup();
                        relatedInstructionalOfferings.add((pg instanceof Class_ ?((Class_)pg).getSchedulingSubpart():(SchedulingSubpart)pg).getInstrOfferingConfig().getInstructionalOffering());
        				pg.getDistributionObjects().remove(dObj);
        				hibSession.saveOrUpdate(pg);
        			}
        			s.clear();
        			dp.setDistributionObjects(s);
        			oldOwner = (Department)dp.getOwner();
            	}
            } else dp = new DistributionPref();
            
            dp.setDistributionType(new DistributionTypeDAO().get( Long.valueOf(form.getDistType()), hibSession));
            dp.setStructure(form.getStructure());
        	dp.setPrefLevel(PreferenceLevel.getPreferenceLevel( Integer.parseInt(form.getPrefLevel()) ));
        
        	Department owningDept = null;
        
	        // Create distribution objects
     	    for (int i=0; i<saList.size(); i++) {
        	    String su = suList.get(i).toString();
            	String cl = clList.get(i).toString();
            
            	DistributionObject dObj = new DistributionObject();	                
            
	            // Subpart
    	        if(cl.equals(DistributionPrefsForm.ALL_CLASSES_SELECT)) {
        	    	SchedulingSubpart subpart = new SchedulingSubpartDAO().get(Long.valueOf(su), hibSession);
	            	if (owningDept==null) owningDept = subpart.getManagingDept();
    	        	else if (!owningDept.getUniqueId().equals(subpart.getManagingDept().getUniqueId())) {
    	        		if (owningDept.getDistributionPrefPriority().intValue()<subpart.getManagingDept().getDistributionPrefPriority().intValue())
    	        			owningDept = subpart.getManagingDept();
    	        		else if (owningDept.getDistributionPrefPriority().intValue()==subpart.getManagingDept().getDistributionPrefPriority().intValue()) {
    	        			if (!sessionContext.getUser().getCurrentAuthority().hasQualifier(owningDept) && sessionContext.getUser().getCurrentAuthority().hasQualifier(subpart.getManagingDept()))
    	        				owningDept = subpart.getManagingDept();
    	        		}
    	        	}
            	
            		dObj.setPrefGroup(subpart);
                    relatedInstructionalOfferings.add(subpart.getInstrOfferingConfig().getInstructionalOffering());
            	}
            
	            // Class
    	        else {
        	    	Class_ clazz = new Class_DAO().get(Long.valueOf(cl), hibSession);
	            	if (owningDept==null) owningDept = clazz.getManagingDept();
    	        	else if (!owningDept.equals(clazz.getManagingDept())) {
    	        		if (owningDept.getDistributionPrefPriority().intValue()<clazz.getManagingDept().getDistributionPrefPriority().intValue())
    	        			owningDept = clazz.getManagingDept();
    	        		else if (owningDept.getDistributionPrefPriority().intValue()==clazz.getManagingDept().getDistributionPrefPriority().intValue()) {
    	        			if (!sessionContext.getUser().getCurrentAuthority().hasQualifier(owningDept) && sessionContext.getUser().getCurrentAuthority().hasQualifier(clazz.getManagingDept()))
    	        				owningDept = clazz.getManagingDept();
    	        		}
    	        	}
	            	
                    relatedInstructionalOfferings.add(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering());
        	    	dObj.setPrefGroup(clazz);
            	}
            
            	dObj.setSequenceNumber(Integer.valueOf(i+1));
            	dObj.setDistributionPref(dp);
            	dObj.getPrefGroup().getDistributionObjects().add(dObj);
            	
            	dp.addTodistributionObjects(dObj);
        	}
        
     	    dp.setOwner(owningDept);
     	    
     	    /*
     	   
        	if (dp.getOwner()==null)
        		throw new Exception("Creation of such constraint denied: no owner specified.");

    		if (sessionContext.hasPermission(Right.) && !dp.getDistributionType().isApplicable(owningDept)) {
    			throw new Exception("Creation of such constraint denied: distribution preference "+dp.getDistributionType().getLabel()+" not allowed for "+dp.getOwner()+".");
    		}

        		if (!sessionContext.hasPermission(dp.getOwner(), Right.DistributionPreferenceAdd))
	        		throw new Exception("Creation of such constraint denied: unable to create constraint owned by "+dp.getOwner()+".");

	        	if (!sessionContext.getUser().getCurrentAuthority().hasQualifier((Department)dp.getOwner()) && !((Department)dp.getOwner()).effectiveStatusType().canOwnerEdit())
        			throw new Exception("Creation of such constraint denied: unable to create constraint owned by "+dp.getOwner()+".");
        	
        		if (currentMgr.isExternalManager() && !sessionContext.getUser().getCurrentAuthority().hasQualifier((Department)dp.getOwner()))
        			throw new Exception("Creation of such constraint denied: unable to create constraint owned by "+dp.getOwner()+".");
        	
        		Department dept = (Department)dp.getOwner();
        		if (dept.isExternalManager() && !dept.isAllowReqDistribution() && !sessionContext.getUser().getCurrentAuthority().hasQualifier((Department)dp.getOwner())) {
        			if (dp.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) {
        				if (dp.getDistributionType().getAllowedPref().indexOf(PreferenceLevel.sCharLevelStronglyPreferred)>=0)
        					dp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
        				else
        					throw new Exception("Creation of such constraint denied: unable to create "+dp.getPrefLevel().getPrefName()+" constraint owned by "+dp.getOwner()+".");
        			}
            		if (dp.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sProhibited)) {
            			if (dp.getDistributionType().getAllowedPref().indexOf(PreferenceLevel.sCharLevelStronglyDiscouraged)>=0)
            				dp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
            			else
            				throw new Exception("Creation of such constraint denied: unable to create "+dp.getPrefLevel().getPrefName()+" constraint owned by "+dp.getOwner()+".");
            		}
        		}
        	}*/
     	    
     	    sessionContext.checkPermission(dp, Right.DistributionPreferenceEdit);
        
	        // Save
    	    hibSession.saveOrUpdate(dp);
    	    
    	    Permission<InstructionalOffering> permissionOfferingLockNeeded = getPermission("permissionOfferingLockNeeded");
            
    	    List<Long> changedOfferingIds = new ArrayList<Long>();
            for (Iterator i=relatedInstructionalOfferings.iterator();i.hasNext();) {
                InstructionalOffering io = (InstructionalOffering)i.next();
                ChangeLog.addChange(
                        hibSession, 
                        sessionContext, 
                        io, 
                        ChangeLog.Source.DIST_PREF_EDIT,
                        (distPrefId!=null && distPrefId.trim().length()>0?ChangeLog.Operation.UPDATE:ChangeLog.Operation.CREATE),
                        io.getControllingCourseOffering().getSubjectArea(), 
                        null);
                if (permissionOfferingLockNeeded.check(sessionContext.getUser(), io))
                	changedOfferingIds.add(io.getUniqueId());
            }
            if (!changedOfferingIds.isEmpty())
            	StudentSectioningQueue.offeringChanged(hibSession, sessionContext.getUser(), sessionContext.getUser().getCurrentAcademicSessionId(), changedOfferingIds);
            
	       	tx.commit();
	       	hibSession.flush();
    	    hibSession.refresh(dp.getOwner());
    	    if (oldOwner!=null && !oldOwner.equals(dp.getOwner()))
    	    	hibSession.refresh(oldOwner);
    	    form.setDistPrefId(dp.getUniqueId().toString());
        } catch (Exception e) {
        	if (tx!=null) tx.rollback();
        	hibSession.clear();
        	throw e;
        } 
    }
    
    /**
     * Delete distribution pref
     * @param distPrefId
     */
    private void doDelete(String distPrefId) {
        /*
        String query = "delete DistributionPref dp where dp.uniqueId=:distPrefId";

        Query q = hibSession.createQuery(query);
        q.setLong("distPrefId", Long.parseLong(distPrefId));
        q.executeUpdate();
        */
        
        Transaction tx = null;

        try {
            
	        DistributionPrefDAO dpDao = new DistributionPrefDAO();
	        org.hibernate.Session hibSession = dpDao.getSession();
	        tx = hibSession.getTransaction();
	        if (tx==null || !tx.isActive())
	            tx = hibSession.beginTransaction();
	        
            HashSet relatedInstructionalOfferings = new HashSet();
	        DistributionPref dp = dpDao.get(Long.valueOf(distPrefId));
	        
	        sessionContext.checkPermission(dp, Right.DistributionPreferenceDelete);
	        
	        Department dept = (Department) dp.getOwner();
	        dept.getPreferences().remove(dp);
			for (Iterator i=dp.getDistributionObjects().iterator();i.hasNext();) {
				DistributionObject dObj = (DistributionObject)i.next();
				PreferenceGroup pg = dObj.getPrefGroup();
                relatedInstructionalOfferings.add((pg instanceof Class_ ?((Class_)pg).getSchedulingSubpart():(SchedulingSubpart)pg).getInstrOfferingConfig().getInstructionalOffering());
				pg.getDistributionObjects().remove(dObj);
				hibSession.saveOrUpdate(pg);
			}
	        
	        hibSession.delete(dp);
	        hibSession.saveOrUpdate(dept);
	        
	        Permission<InstructionalOffering> permissionOfferingLockNeeded = getPermission("permissionOfferingLockNeeded");
	        
	        List<Long> changedOfferingIds = new ArrayList<Long>();
            for (Iterator i=relatedInstructionalOfferings.iterator();i.hasNext();) {
                InstructionalOffering io = (InstructionalOffering)i.next();
                ChangeLog.addChange(
                        hibSession, 
                        sessionContext, 
                        io, 
                        ChangeLog.Source.DIST_PREF_EDIT,
                        ChangeLog.Operation.DELETE,
                        io.getControllingCourseOffering().getSubjectArea(), 
                        null);
                if (permissionOfferingLockNeeded.check(sessionContext.getUser(), io))
                	changedOfferingIds.add(io.getUniqueId());
            }
            if (!changedOfferingIds.isEmpty())
            	StudentSectioningQueue.offeringChanged(hibSession, sessionContext.getUser(), sessionContext.getUser().getCurrentAcademicSessionId(), changedOfferingIds);

            if (tx!=null && tx.isActive()) 
	            tx.commit();
	        
	        hibSession.flush();
	        hibSession.refresh(dept);
        }
        catch (Exception e) {
            Debug.error(e);
            if (tx!=null && tx.isActive()) 
                tx.rollback();
        }
    }
    
    public String getFocusElement() {
    	if (request.getAttribute("addedClass")!=null)
    		return "subjectArea[" + request.getAttribute("addedClass").toString() + "]";
    	if (request.getAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR)!=null)
    		return null;
    	return "distType";
    }
}
