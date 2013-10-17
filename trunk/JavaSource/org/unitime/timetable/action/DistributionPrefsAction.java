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
package org.unitime.timetable.action;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.DistributionPrefsForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;


/** 
 * MyEclipse Struts
 * Creation date: 12-14-2005
 * 
 * XDoclet definition:
 * @struts:action path="/distributionPrefs" name="distributionPrefsForm" input="/user/distributionPrefs.jsp" scope="request"
 *
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
@Service("/distributionPrefs")
public class DistributionPrefsAction extends Action {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;

    // --------------------------------------------------------- Instance Variables

    // --------------------------------------------------------- Methods

    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {
    	
    	sessionContext.checkPermission(Right.DistributionPreferences);

		MessageResources rsc = getResources(request);
		ActionMessages errors = new ActionMessages();
        DistributionPrefsForm frm = (DistributionPrefsForm) form;
        
        String reloadId = request.getParameter("reloadId");
        String reloadCause = request.getParameter("reloadCause");
        String deleteId = request.getParameter("deleteId");
        String deleteType = request.getParameter("deleteType");
        String distPrefId = request.getParameter("dp");
        
		String op = frm.getOp();
		if(op==null || op.trim().length()==0) {
		    op = "view";
		    frm.setOp(op);
		}
		
		frm.setFilterSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
		
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
        if(op.equals(rsc.getMessage("button.cancel"))) {
            frm.reset(mapping, request);
            if (BackTracker.doBack(request, response)) return null;
            op = "view"; //in case no back is available
        }
        
		// Set lookup tables lists
        //LookupTables.setupPrefLevels(request);	 // Preference Levels
        LookupTables.setupDistribTypes(request, sessionContext); // Distribution Types
        Vector subjectAreaList = setupSubjectAreas(request); // Subject Areas

        // Add / Update distribution pref
        if(op.equals(rsc.getMessage("button.save")) || op.equals(rsc.getMessage("button.update")) ) {
            Debug.debug("Saving distribution pref ...");
            errors = frm.validate(mapping, request);
            if(errors.size()==0) {
            	try {
           			doAddOrUpdate(request, frm);
           			if (frm.getDistPrefId()!=null) {
           				request.setAttribute("backType", "PreferenceGroup");
           				request.setAttribute("backId", frm.getDistPrefId());
           			}
	    	        frm.reset(mapping, request);
	    	        if (BackTracker.doBack(request, response)) return null;
		            op = "view"; //in case no back is available
           		} catch (Exception e) {
           			Debug.error(e);
           			errors.add("classes", new ActionMessage("errors.generic", e.getMessage()));
	                saveErrors(request, errors);
	            }
	        }
	        else 
	            saveErrors(request, errors);
        }
        
        // Delete distribution object / pref
        if(op.equals(rsc.getMessage("button.delete"))) {
            if(deleteType.equals("distObject")) {
                frm.removeFromLists(Integer.parseInt(deleteId));
            }
            if(deleteType.equals("distPref")) {
                distPrefId = frm.getDistPrefId();
                doDelete(request, distPrefId);
                frm.reset(mapping, request);            
                if (BackTracker.doBack(request, response)) return null;
	            op = "view"; //in case no back is available
            }
        }
        
        // Add new class - redirect from SchedulingSubpartEdit / ClassEdit
        if(op.equals(rsc.getMessage("button.addDistPref")) || MSG.actionAddDistributionPreference().equals(op)) {
            Debug.debug("Adding new Class via redirect ...");
	        frm.setDistType(Preference.BLANK_PREF_VALUE);
	        frm.setGrouping(Preference.BLANK_PREF_VALUE);
	        if (request.getAttribute("subjectAreaId")!=null) {
	        	frm.addToSubjectArea(request.getAttribute("subjectAreaId").toString());
	        	frm.addToItype(request.getAttribute("schedSubpartId").toString());
	        	frm.addToCourseNbr(request.getAttribute("courseOffrId").toString());
	        	frm.addToClassNumber(request.getAttribute("classId").toString());
		        request.setAttribute("addedClass", ""+(frm.getSubjectArea().size()-1));
	        }
        }
        
        // Add new class
        if(op.equals(rsc.getMessage("button.addClass_"))) {
            Debug.debug("Adding new Class ...");
            String subjAreaId = null;
            if(subjectAreaList.size()==1)
            	subjAreaId = ((ComboBoxLookup)subjectAreaList.elementAt(0)).getValue();
            
            frm.addNewClass(subjAreaId);
    	    request.setAttribute("addedClass", ""+(frm.getSubjectArea().size()-1));
        }
        
        if (op.equals(rsc.getMessage("button.search")) || op.equals(rsc.getMessage("button.exportPDF"))) {
        	String subjectAreaId = frm.getFilterSubjectAreaId();
        	String courseNbr = frm.getFilterCourseNbr();
        	if (subjectAreaId!=null && subjectAreaId.length()>0)
        		sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjectAreaId);
        	else
        		sessionContext.removeAttribute(SessionAttribute.OfferingsSubjectArea);
        	if (courseNbr!=null && courseNbr.length()>0)
        		sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, courseNbr);
        	else
        		sessionContext.removeAttribute(SessionAttribute.OfferingsCourseNumber);
        	
        	if (op.equals(rsc.getMessage("button.exportPDF")))
        		op="export"; 
        	else 
        		op="view";
        }

        // Load Distribution Pref
        if(op!=null && (op.equals("view") || op.equals("export")) 
                && distPrefId!=null && distPrefId.trim().length()>0) {
            Debug.debug("Loading dist pref - " + distPrefId);
            
            frm.reset(mapping, request);
            doLoad(frm, distPrefId);
        }
        
        // Reload 
        if(op!=null && op.equals("reload")) {
            // Subject area changed
            if (reloadCause!=null && reloadCause.equals("subjectArea")) {
	            int index = Integer.parseInt(reloadId);
	            Debug.debug("subj area changed ... " + reloadId + " - " + frm.getSubjectArea(index));
	
	            // Reset values to blank
	            frm.setCourseNbr(index, Preference.BLANK_PREF_VALUE);
	            frm.setItype(index, Preference.BLANK_PREF_VALUE);
	            frm.setClassNumber(index, Preference.BLANK_PREF_VALUE);
	        }
            
            // Move Distribution object up one level
            if (reloadCause!=null && reloadCause.equals("moveUp")) {
	            int index = Integer.parseInt(reloadId);
	            Debug.debug("moving up ... " + reloadId);
	            frm.swap(index, index-1);
            }
            
            // Move Distribution object down one level
            if (reloadCause!=null && reloadCause.equals("moveDown")) {
	            int index = Integer.parseInt(reloadId);
	            Debug.debug("moving down ... " + reloadId);
	            frm.swap(index, index+1);
            }
        }

        // Set up lookup list
        setLookupLists(request, frm, subjectAreaList, errors); // Distribution Objects

        if (frm.getDistType()!=null && !frm.getDistType().equals(Preference.BLANK_PREF_VALUE)) {
        	Vector prefs = new Vector();
        	DistributionType dist = (new DistributionTypeDAO().get(new Long(frm.getDistType())));
        	frm.setDescription(dist.getDescr());
        	boolean containsPref = false; 
        	for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList()) {
        		if (dist.isAllowed(pref)) {
        			prefs.addElement(pref);
        			if (frm.getPrefLevel()!=null && !frm.getPrefLevel().equals(Preference.BLANK_PREF_VALUE) && pref.getPrefId().equals(new Integer(frm.getPrefLevel()))) containsPref = true;
        		}
        	}
        	if (!containsPref)
        		frm.setPrefLevel(Preference.BLANK_PREF_VALUE);
        	if (prefs.size()==1)
        		frm.setPrefLevel(((PreferenceLevel)prefs.firstElement()).getPrefId().toString());
        	request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, prefs);
        } else {
        	request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, new Vector(0));
        	frm.setDescription("");
        }	    
        
        if (frm.getGrouping()!=null && !frm.getGrouping().equals(Preference.BLANK_PREF_VALUE)) {
        	frm.setGroupingDescription(DistributionPref.getGroupingDescription(frm.getGroupingInt()));
        }

        if ("export".equals(op) && (frm.getDistPrefId()==null || frm.getDistPrefId().length()==0)) {
        	OutputStream out = ExportUtils.getPdfOutputStream(response, "distprefs");
        	
            new DistributionPrefsTableBuilder().getAllDistPrefsTableForCurrentUserAsPdf(out, sessionContext, frm.getFilterSubjectAreaId(), frm.getFilterCourseNbr());
            
            return null;
        }
        
        request.setAttribute(DistributionPrefsForm.LIST_SIZE_ATTR, ""+(frm.getSubjectArea().size()-1));

        if ("view".equals(op) && (frm.getDistPrefId()==null || frm.getDistPrefId().length()==0)) {
        	String subject = (String)sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
        	if (subject != null && subject.indexOf(',') >= 0) subject = subject.substring(0, subject.indexOf(','));
        	frm.setFilterSubjectAreaId(subject);
        	frm.setFilterCourseNbr((String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber));
        	
        	DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
        	if (frm.getFilterSubjectAreaId()==null) {
        	    if (sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent))
        	        frm.setFilterSubjectAreaId(Constants.BLANK_OPTION_VALUE);
        	    else
        	        frm.setFilterSubjectAreaId(Constants.ALL_OPTION_VALUE);        	        
        	}        	
        	
        	String html = tbl.getAllDistPrefsTableForCurrentUser(request, sessionContext, frm.getFilterSubjectAreaId(), frm.getFilterCourseNbr());
        	if (html!=null)
        		request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);
            BackTracker.markForBack(
            		request,
            		"distributionPrefs.do",
            		"Distribution Preferences",
            		true, true);
            return mapping.findForward("list");
        }
        
        return mapping.findForward(frm.getDistPrefId()==null || frm.getDistPrefId().length()==0?"add":"edit");
    }

    /**
     * Get Subject Areas for an acad session for a user and store it in request object
     * Gets all subject areas for LLR Manager, Lab Manager and Admin
     * @param request
     * @throws Exception
     */
    public Vector setupSubjectAreas(
            HttpServletRequest request) throws Exception {

        Set subjectAreas = SubjectArea.getUserSubjectAreas(sessionContext.getUser());
        
        if (subjectAreas==null) return null;
        
        Vector v = new Vector(subjectAreas.size());
           for (Iterator i=subjectAreas.iterator();i.hasNext();) {
           	SubjectArea sa = (SubjectArea)i.next();
           	v.addElement(new ComboBoxLookup(sa.getSubjectAreaAbbreviation(),sa.getUniqueId().toString()));
    	}
           
        return v;
    }
    
    /**
     * @param index
     */
    private void setLookupLists(       
            HttpServletRequest request,
            DistributionPrefsForm frm, 
            Vector subjectAreaList, 
            ActionMessages errors ) {
        
        int ct = frm.getSubjectArea().size();        
        for(int index=0; index<ct; index++) {
            
	        String subjectAreaId = frm.getSubjectArea(index);
	        String courseNbr = frm.getCourseNbr(index);
	        String subpart = frm.getItype(index);
	        String classNumber = frm.getClassNumber(index);
	        
	        Vector crsNumList = null;
	        Vector subpartList = null;
	        Vector classNumList = null;
	        
	        // Process subject area selection
	        if(subjectAreaId!=null) {
	            if(subjectAreaId.equals(Preference.BLANK_PREF_VALUE)) {
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
	        		        frm.setCourseNbr(index, cbl.getValue());
	        		        courseNbr = frm.getCourseNbr(index);
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
		        			Collections.sort(result, new SchedulingSubpartComparator(subjectAreaId==null || subjectAreaId.length()==0?null:new Long(subjectAreaId)));
		        			subpartList = new Vector();
		        		    for(int i=0; i<result.size(); i++) {
		        		        SchedulingSubpart a = (SchedulingSubpart)result.get(i);
		        		        String ssid = a.getUniqueId().toString();
		        		        String name = a.getItype().getAbbv();
		        		        String sufix = a.getSchedulingSubpartSuffix();
		        		        while (a.getParentSubpart()!=null) {
		        		        	name = "&nbsp;&nbsp;&nbsp;&nbsp;"+name;
		        		        	a = a.getParentSubpart();
		        		        }
		        		        if (a.getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().size()>1)
		        		        	name += " ["+a.getInstrOfferingConfig().getName()+"]";
		        		        ComboBoxLookup cbl = new ComboBoxLookup(name+(sufix==null || sufix.length()==0?"":" ("+sufix+")"), ssid);
		        		        subpartList.addElement(cbl);
		        		    }
		        		    
		        		    // Only one record - select it to save time and one more click
		        		    if(result.size()==1) {
		        		        ComboBoxLookup cbl = (ComboBoxLookup) subpartList.elementAt(0);
		        		        frm.setItype(index, cbl.getValue());
		        		        subpart = frm.getItype(index);
		        		    }
		        		}
	                    
	                    if(subpartList==null || subpartList.size()==0) {
	                        subpartList = new Vector();
	                        errors.add("classes", 
	                                	new ActionMessage("errors.generic",
	                                	       "No subparts exist for the given course" ) );
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
			        		        frm.setClassNumber(index, DistributionPrefsForm.ALL_CLASSES_SELECT);
			        		        
	                            classNumList = new Vector();
			        		    for(int i=0; i<result.size(); i++) {
			        		    	Class_ clazz = (Class_)result.get(i);
			        		        ComboBoxLookup cbl = new ComboBoxLookup(
			        		        		clazz.getSectionNumberString(), clazz.getUniqueId().toString());
			        		        classNumList.addElement(cbl);
			        		    }
			        		}
			        		else {
    	                        classNumList = new Vector();
    	                        errors.add("classes", 
    	                                	new ActionMessage("errors.generic",
    	                                	       "No classes exist for the given subpart" ) );
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

        saveErrors(request, errors);
    }
    
    /**
     * Loads the form with the data for the distribution pref selected
     * @param frm
     * @param distPrefId
     */
    private void doLoad(
            DistributionPrefsForm frm, 
            String distPrefId ) {
 
        // Get distribution pref info
        DistributionPrefDAO dpDao = new DistributionPrefDAO();
        DistributionPref dp = dpDao.get(new Long(distPrefId));
        frm.setDistType(dp.getDistributionType().getUniqueId().toString());
        frm.setGrouping(dp.getGroupingName());
        frm.setOwner(dp.getOwner().getUniqueId().toString());
        frm.setPrefLevel(dp.getPrefLevel().getPrefId().toString());
        frm.setDistPrefId(distPrefId);
        
        org.hibernate.Session hibSession = dpDao.getSession();

        // Get Subpart Distribution Prefs
        StringBuffer query = new StringBuffer(""); 
        query.append("select sa.uniqueId, co.uniqueId, su.uniqueId, -1,  do.sequenceNumber ");
        query.append("  from ");
        query.append("       DistributionObject do, ");
        query.append("       SchedulingSubpart su, ");
        query.append("       InstrOfferingConfig ioc, ");
        query.append("       InstructionalOffering io, ");
        query.append("       CourseOffering co, ");
        query.append("       SubjectArea sa ");
        query.append(" where co.isControl=true ");
        query.append("   and do.distributionPref.uniqueId=:distPrefId ");
        query.append("   and do.prefGroup.uniqueId=su.uniqueId ");
        query.append("   and su.instrOfferingConfig.uniqueId=ioc.uniqueId ");
        query.append("   and ioc.instructionalOffering.uniqueId=io.uniqueId ");
        query.append("   and co.instructionalOffering.uniqueId=io.uniqueId ");
        query.append("   and co.subjectArea.uniqueId=sa.uniqueId ");

        Query q = hibSession.createQuery(query.toString());
        q.setLong("distPrefId", Long.parseLong(distPrefId));
        List distPrefs1 = q.list();
        
        // Get class Distribution Prefs
        StringBuffer query2 = new StringBuffer(""); 
        query2.append("select sa.uniqueId, co.uniqueId, su.uniqueId, c.uniqueId, do.sequenceNumber ");
        query2.append("  from ");
        query2.append("       DistributionObject do, ");
        query2.append("       Class_ c, ");
        query2.append("       SchedulingSubpart su, ");
        query2.append("       InstrOfferingConfig ioc, ");
        query2.append("       InstructionalOffering io, ");
        query2.append("       CourseOffering co, ");
        query2.append("       SubjectArea sa ");
        query2.append(" where co.isControl=true ");
        query2.append("   and do.distributionPref.uniqueId=:distPrefId ");
        query2.append("   and do.prefGroup.uniqueId=c.uniqueId ");
        query2.append("   and c.schedulingSubpart.uniqueId=su.uniqueId ");
        query2.append("   and su.instrOfferingConfig.uniqueId=ioc.uniqueId ");
        query2.append("   and ioc.instructionalOffering.uniqueId=io.uniqueId ");
        query2.append("   and co.instructionalOffering.uniqueId=io.uniqueId ");
        query2.append("   and co.subjectArea.uniqueId=sa.uniqueId ");

        q = hibSession.createQuery(query2.toString());
        q.setLong("distPrefId", Long.parseLong(distPrefId));
        List distPrefs2 = q.list();
        
        // Combine subparts and classes
        ArrayList distPrefs = new ArrayList();
        if(distPrefs1!=null && distPrefs1.size()>0)
            distPrefs.addAll(distPrefs1);
        if(distPrefs2!=null && distPrefs2.size()>0)
            distPrefs.addAll(distPrefs2);
        
        if(distPrefs!=null && distPrefs.size()>0) {
            Iterator iter = distPrefs.iterator();
            int i = 0;
            while (iter.hasNext()) {
                Object[] rec = (Object[]) iter.next();
                int indx;
                if (rec[4] == null){
                	indx = i;
                } else {
                	indx = ((Integer) rec[4]).intValue() - 1;
                }
                frm.setSubjectArea(indx, rec[0].toString());
                frm.setCourseNbr(indx, rec[1].toString());
                frm.setItype(indx, rec[2].toString());
                frm.setClassNumber(indx, rec[3].toString());
                i++;
            }                
        }            
    }
    
    /**
     * Add new distribution pref
     * @param httpSession
     * @param frm
     */
    private void doAddOrUpdate(
            HttpServletRequest request, 
            DistributionPrefsForm frm ) throws Exception {

        String distPrefId = frm.getDistPrefId();
        List saList = frm.getSubjectArea();
        List suList = frm.getItype();
        List clList = frm.getClassNumber();            
        
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
        		Long distPrefUid = new Long(distPrefId);
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
            
            dp.setDistributionType(new DistributionTypeDAO().get( new Long(frm.getDistType()), hibSession));
            dp.setGrouping(new Integer(frm.getGroupingInt()));
        	dp.setPrefLevel(PreferenceLevel.getPreferenceLevel( Integer.parseInt(frm.getPrefLevel()) ));
        
        	Department owningDept = null;
        
	        // Create distribution objects
     	    for (int i=0; i<saList.size(); i++) {
        	    String su = suList.get(i).toString();
            	String cl = clList.get(i).toString();
            
            	DistributionObject dObj = new DistributionObject();	                
            
	            // Subpart
    	        if(cl.equals(DistributionPrefsForm.ALL_CLASSES_SELECT)) {
        	    	SchedulingSubpart subpart = new SchedulingSubpartDAO().get(new Long(su), hibSession);
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
        	    	Class_ clazz = new Class_DAO().get(new Long(cl), hibSession);
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
            
            	dObj.setSequenceNumber(new Integer(i+1));
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
            }
            
	       	tx.commit();
	       	hibSession.flush();
    	    hibSession.refresh(dp.getOwner());
    	    if (oldOwner!=null && !oldOwner.equals(dp.getOwner()))
    	    	hibSession.refresh(oldOwner);
    	    frm.setDistPrefId(dp.getUniqueId().toString());
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
    private void doDelete(HttpServletRequest request, String distPrefId) {
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
	        if (tx==null 
	                || !tx.isActive()) {
	            tx = hibSession.beginTransaction();
	        	tx.begin();
	        }
	        
            HashSet relatedInstructionalOfferings = new HashSet();
	        DistributionPref dp = dpDao.get(new Long(distPrefId));
	        
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
            }

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
}
