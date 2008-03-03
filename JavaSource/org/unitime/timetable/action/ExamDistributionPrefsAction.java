/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.DistributionPrefsForm;
import org.unitime.timetable.form.ExamDistributionPrefsForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.ExamDistributionPrefsTableBuilder;

public class ExamDistributionPrefsAction extends Action {

    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        
        User user = Web.getUser(request.getSession()); 
        TimetableManager manager = (user==null?null:TimetableManager.getManager(user)); 
        Session session = (user==null?null:Session.getCurrentAcadSession(user));
        if (user==null || session==null || !manager.canSeeExams(session, user)) throw new Exception ("Access Denied.");
        
        		MessageResources rsc = getResources(request);
		ActionMessages errors = new ActionMessages();
        ExamDistributionPrefsForm frm = (ExamDistributionPrefsForm) form;
        
        String deleteId = request.getParameter("deleteId");
        String deleteType = request.getParameter("deleteType");
        String distPrefId = request.getParameter("dp");
        String reloadCause = request.getParameter("reloadCause");
        String reloadId = request.getParameter("reloadId");
        
		String op = frm.getOp();
		if(op==null || op.trim().length()==0) {
		    op = "view";
		    frm.setOp(op);
		}
		
		if (reloadCause!=null && reloadCause.length()>0) op = "reload";
		
        // Cancel - Display blank form
        if (op.equals(rsc.getMessage("button.cancel"))) {
            frm.reset(mapping, request);
            if (BackTracker.doBack(request, response)) return null;
            op = "view"; //in case no back is available
        }
        
		// Set lookup tables lists
        //LookupTables.setupPrefLevels(request);	 // Preference Levels
        LookupTables.setupExamDistribTypes(request); // Distribution Types

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
                frm.deleteExam(Integer.parseInt(deleteId));
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
        if (op.equals(rsc.getMessage("button.addDistPref"))) {
            Debug.debug("Adding new Class via redirect ...");
	        frm.setDistType(Preference.BLANK_PREF_VALUE);
	        if (request.getAttribute("subjectAreaId")!=null) {
	        	frm.setSubjectArea(0, Long.valueOf(request.getAttribute("subjectAreaId").toString()));
	        }
	        if (request.getAttribute("courseOffrId")!=null) {
	            CourseOffering course = new CourseOfferingDAO().get(Long.valueOf(request.getAttribute("courseOffrId").toString()));
	            frm.setSubjectArea(0, course.getSubjectArea().getUniqueId());
	            frm.setCourseNbr(0, course.getUniqueId());
	        }
	        if (request.getAttribute("examId")!=null) {
	            Exam exam = new ExamDAO().get(Long.valueOf(request.getAttribute("examId").toString()));
	            frm.setExam(0, exam.getUniqueId());
	            frm.setSubjectArea(0, exam.firstSubjectArea().getUniqueId());
                frm.setCourseNbr(0, exam.firstCourseOffering().getUniqueId());
                frm.setExamType(exam.getExamType());
	        }
            frm.getSubjectArea().add(new Long(-1));
            frm.getCourseNbr().add(new Long(-1));
            frm.getExam().add(new Long(-1));
        }
        
        // Add new class
        if(op.equals(rsc.getMessage("button.addExam"))) {
            Debug.debug("Adding new Class ...");
            frm.getSubjectArea().add(new Long(-1));
            frm.getCourseNbr().add(new Long(-1));
            frm.getExam().add(new Long(-1));
        }

        if (op.equals(rsc.getMessage("button.search")) || op.equals(rsc.getMessage("button.exportPDF"))) {
        	String subjectAreaId = frm.getFilterSubjectAreaId();
        	String courseNbr = frm.getFilterCourseNbr();
        	if (subjectAreaId!=null && subjectAreaId.length()>0)
        		request.getSession().setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, subjectAreaId);
        	else
        	    request.getSession().removeAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME);
        	if (courseNbr!=null && courseNbr.length()>0)
        	    request.getSession().setAttribute(Constants.CRS_NBR_ATTR_NAME, courseNbr);
        	else
        	    request.getSession().removeAttribute(Constants.CRS_NBR_ATTR_NAME);
        	request.getSession().setAttribute("Exam.Type", frm.getExamType());
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
            // Move Distribution object up one level
            if (reloadCause!=null && reloadCause.equals("moveUp")) {
	            int index = Integer.parseInt(reloadId);
	            Debug.debug("moving up ... " + reloadId);
	            frm.swapExams(index, index-1);
            }
            
            // Move Distribution object down one level
            if (reloadCause!=null && reloadCause.equals("moveDown")) {
	            int index = Integer.parseInt(reloadId);
	            Debug.debug("moving down ... " + reloadId);
	            frm.swapExams(index, index+1);
            }
        }

        if (frm.getDistType()!=null && !frm.getDistType().equals(Preference.BLANK_PREF_VALUE)) {
        	Vector prefs = new Vector();
        	DistributionType dist = (new DistributionTypeDAO().get(new Long(frm.getDistType())));
        	frm.setDescription(dist.getDescr());
        	boolean containsPref = false; 
        	for (Enumeration e=PreferenceLevel.getPreferenceLevelList(false).elements();e.hasMoreElements();) {
        		PreferenceLevel pref = (PreferenceLevel)e.nextElement();
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
        
        if ("export".equals(op) && (frm.getDistPrefId()==null || frm.getDistPrefId().length()==0)) {
            ExamDistributionPrefsTableBuilder tbl = new ExamDistributionPrefsTableBuilder();
            File file = tbl.getDistPrefsTableAsPdf(request, (Constants.ALL_OPTION_VALUE.equals(frm.getFilterSubjectAreaId())?null:Long.valueOf(frm.getFilterSubjectAreaId())), frm.getFilterCourseNbr(), frm.getExamType());
            if (file!=null) request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
            op = "view";
        }
        
        request.setAttribute(DistributionPrefsForm.LIST_SIZE_ATTR, ""+(frm.getSubjectArea().size()-1));
        
        frm.setFilterSubjectAreas(TimetableManager.getSubjectAreas(user));
        if (frm.getFilterSubjectAreas().size()==1) {
            SubjectArea firstSubjectArea = (SubjectArea)frm.getFilterSubjectAreas().iterator().next();
            frm.setFilterSubjectAreaId(firstSubjectArea.getUniqueId().toString());
        }
        
        frm.setCanAdd(manager.canEditExams(session, user));
        
        if ("view".equals(op) && (frm.getDistPrefId()==null || frm.getDistPrefId().length()==0)) {
            ExamDistributionPrefsTableBuilder tbl = new ExamDistributionPrefsTableBuilder();
        	if (frm.getFilterSubjectAreaId()==null) {
        	    if (Web.getUser(request.getSession()).isAdmin())
        	        frm.setFilterSubjectAreaId(Constants.BLANK_OPTION_VALUE);
        	    else
        	        frm.setFilterSubjectAreaId(Constants.ALL_OPTION_VALUE);        	        
        	}
        	
        	if (frm.getFilterSubjectAreaId()!=null && frm.getFilterSubjectAreaId().length()>0) {
        	    String html = tbl.getDistPrefsTable(request, (Constants.ALL_OPTION_VALUE.equals(frm.getFilterSubjectAreaId())?null:Long.valueOf(frm.getFilterSubjectAreaId())), frm.getFilterCourseNbr(), frm.getExamType());
        	    if (html!=null)
        	        request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);
        	} else {
        	    request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, "");
        	}
            BackTracker.markForBack(
            		request,
            		"examDistributionPrefs.do",
            		"Exam Distribution Prefs",
            		true, true);
            return mapping.findForward("list");
        }
        
        return mapping.findForward(frm.getDistPrefId()==null || frm.getDistPrefId().length()==0?"add":"edit");
    }

    /**
     * Loads the form with the data for the distribution pref selected
     * @param frm
     * @param distPrefId
     */
    private void doLoad(
            ExamDistributionPrefsForm frm, 
            String distPrefId ) {
 
        // Get distribution pref info
        DistributionPref dp = new DistributionPrefDAO().get(new Long(distPrefId));
        frm.setDistType(dp.getDistributionType().getUniqueId().toString());
        frm.setDescription(dp.getDistributionType().getDescr());
        frm.setPrefLevel(dp.getPrefLevel().getPrefId().toString());
        frm.setDistPrefId(distPrefId);
        
        for (Iterator i=new TreeSet(dp.getDistributionObjects()).iterator();i.hasNext();) {
            DistributionObject distObj = (DistributionObject)i.next();
            Exam exam = (Exam)distObj.getPrefGroup();
            frm.getSubjectArea().add(exam.firstSubjectArea().getUniqueId());
            frm.getCourseNbr().add(exam.firstCourseOffering().getUniqueId());
            frm.getExam().add(exam.getUniqueId());
        }
        
    }
    
    /**
     * Add new distribution pref
     * @param httpSession
     * @param frm
     */
    private void doAddOrUpdate(
            HttpServletRequest request, 
            ExamDistributionPrefsForm frm ) throws Exception {

        HttpSession httpSession = request.getSession();
        String distPrefId = frm.getDistPrefId();
        
        // Create distribution preference
        DistributionPref dp = null;
        DistributionPrefDAO dpDao = new DistributionPrefDAO();
        Transaction tx = null;
        org.hibernate.Session hibSession = dpDao.getSession();
        HashSet relatedExams = new HashSet();
        
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
                        relatedExams.add(pg);
        				pg.getDistributionObjects().remove(dObj);
        				hibSession.saveOrUpdate(pg);
        			}
        			s.clear();
        			dp.setDistributionObjects(s);
            	}
            } else dp = new DistributionPref();
            
            dp.setDistributionType(new DistributionTypeDAO().get( new Long(frm.getDistType()), hibSession));
            dp.setGrouping(-1);
        	dp.setPrefLevel(PreferenceLevel.getPreferenceLevel( Integer.parseInt(frm.getPrefLevel()) ));
        
        	Department owningDept = null; boolean sameOwningDept = true;
        	User user = Web.getUser(httpSession);
        	Session session = Session.getCurrentAcadSession(user);
        	String ownerId = (String) user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
        	TimetableManager currentMgr = new TimetableManagerDAO().get(new Long(ownerId), hibSession);
        	
        	dp.setOwner(session);
        
        	HashSet addedExams = new HashSet();
        	int idx = 0;
        	for (Iterator i=frm.getExam().iterator();i.hasNext();) {
        	    Long examId = (Long)i.next();
        	    if (examId<0) continue;
        	    
        	    Exam exam = new ExamDAO().get(examId, hibSession);
        	    if (exam==null) continue;
        	    if (!addedExams.add(exam)) continue;
        	    relatedExams.add(exam);
        	    
                DistributionObject dObj = new DistributionObject();                 
                dObj.setPrefGroup(exam);
                dObj.setDistributionPref(dp);
                dObj.setSequenceNumber(new Integer(++idx));
                exam.getDistributionObjects().add(dObj);
                dp.addTodistributionObjects(dObj);
        	}
        
     	     // Save
    	    hibSession.saveOrUpdate(dp);
            
            for (Iterator i=relatedExams.iterator();i.hasNext();) {
                Exam exam = (Exam)i.next();
                ChangeLog.addChange(
                        hibSession, 
                        request, 
                        exam, 
                        ChangeLog.Source.DIST_PREF_EDIT,
                        (distPrefId!=null && distPrefId.trim().length()>0?ChangeLog.Operation.UPDATE:ChangeLog.Operation.CREATE),
                        exam.firstSubjectArea(), 
                        exam.firstDepartment());
            }
            
	       	tx.commit();
	       	hibSession.flush();
    	    hibSession.refresh(dp.getOwner());
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
        Transaction tx = null;

        try {
            
	        DistributionPrefDAO dpDao = new DistributionPrefDAO();
	        org.hibernate.Session hibSession = dpDao.getSession();
	        tx = hibSession.getTransaction();
	        if (tx==null || !tx.isActive()) {
	            tx = hibSession.beginTransaction();
	        	tx.begin();
	        }
	        
            HashSet relatedExams = new HashSet();
	        DistributionPref dp = dpDao.get(new Long(distPrefId));
	        PreferenceGroup owner = (PreferenceGroup) dp.getOwner();
	        owner.getPreferences().remove(dp);
			for (Iterator i=dp.getDistributionObjects().iterator();i.hasNext();) {
				DistributionObject dObj = (DistributionObject)i.next();
				PreferenceGroup pg = dObj.getPrefGroup();
				relatedExams.add(pg);
				pg.getDistributionObjects().remove(dObj);
				hibSession.saveOrUpdate(pg);
			}
	        
	        hibSession.delete(dp);
	        hibSession.saveOrUpdate(owner);
	        
            for (Iterator i=relatedExams.iterator();i.hasNext();) {
                Exam exam = (Exam)i.next();
                ChangeLog.addChange(
                        hibSession, 
                        request, 
                        exam, 
                        ChangeLog.Source.DIST_PREF_EDIT,
                        ChangeLog.Operation.DELETE,
                        exam.firstSubjectArea(), 
                        exam.firstDepartment());
            }

            if (tx!=null && tx.isActive()) 
	            tx.commit();
	        
	        hibSession.flush();
	        hibSession.refresh(owner);
        }
        catch (Exception e) {
            Debug.error(e);
            if (tx!=null && tx.isActive()) 
                tx.rollback();
        }
    }
}