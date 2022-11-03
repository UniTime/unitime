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
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ConstantsMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.DistributionPrefsForm;
import org.unitime.timetable.form.ExamDistributionPrefsForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.IdValue;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.ExamDistributionPrefsTableBuilder;

/**
 * @author Tomas Muller
 */
@Action(value="examDistributionPrefs", results = {
		@Result(name = "list", type = "tiles", location = "examDistributionPrefs.tiles"),
		@Result(name = "add", type = "tiles", location = "addExamDistributionPref.tiles"),
		@Result(name = "edit", type = "tiles", location = "editExamDistributionPref.tiles")
	})
@TilesDefinitions(value = {
		@TilesDefinition(name = "examDistributionPrefs.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Examination Distribution Preferences"),
				@TilesPutAttribute(name = "body", value = "/user/examDistributionPrefs.jsp"),
				@TilesPutAttribute(name = "showNavigation", value = "true")
		}),
		@TilesDefinition(name = "addExamDistributionPref.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Add Examination Distribution Preference"),
				@TilesPutAttribute(name = "body", value = "/user/examDistributionPrefs.jsp"),
				@TilesPutAttribute(name = "showNavigation", value = "true")
		}),
		@TilesDefinition(name = "editExamDistributionPref.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Edit Examination Distribution Preference"),
				@TilesPutAttribute(name = "body", value = "/user/examDistributionPrefs.jsp"),
				@TilesPutAttribute(name = "showNavigation", value = "true")
		})
	})
public class ExamDistributionPrefsAction extends UniTimeAction<ExamDistributionPrefsForm> {
    private static final long serialVersionUID = -4234228449590216694L;
	protected static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	protected static ConstantsMessages CONST = Localization.create(ConstantsMessages.class);
	protected String reloadId;
	protected String reloadCause;
	protected String deleteId;
	protected String deleteType;
	protected String distPrefId;
	
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

	public String execute() throws Exception {
		if (form == null) form = new ExamDistributionPrefsForm();
    	sessionContext.checkPermission(Right.ExaminationDistributionPreferences);
    	
    	if (op == null) op = form.getOp();
    	if (op==null || op.trim().isEmpty()) {
			if (deleteType != null && deleteType.length()>0)
				op = "delete";
			else
				op = "view";
		    form.setOp(op);
		}
		
		if (reloadCause!=null && reloadCause.length()>0) op = "reload";
		
        // Cancel - Display blank form
		if (MSG.actionCancel().equals(op)) {
            form.reset();
            if (BackTracker.doBack(request, response)) return null;
            op = "view"; //in case no back is available
        }

		// Set lookup tables lists
        LookupTables.setupExamTypes(request, sessionContext.getUser(), DepartmentStatusType.Status.ExamTimetable, DepartmentStatusType.Status.ExamView); // Exam Types
        request.setAttribute("examTypesAdd", ExamType.findAllUsedApplicable(sessionContext.getUser(), DepartmentStatusType.Status.ExamTimetable, DepartmentStatusType.Status.ExamEdit));

        // Add / Update distribution pref
        if (MSG.actionSaveNewDistributionPreference().equals(op) || MSG.actionUpdateDistributionPreference().equals(op)) {
            Debug.debug("Saving distribution pref ...");
            sessionContext.setAttribute(SessionAttribute.ExamType, form.getExamType());
            form.validate(this);
            if (!hasFieldErrors()) {
            	try {
           			doAddOrUpdate(form);
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
        if (MSG.actionDeleteDistributionPreference().equals(op) || "delete".equals(op)) {
            if(deleteType.equals("distObject")) {
                form.deleteExam(Integer.parseInt(deleteId));
            }
            if (deleteType.equals("distPref")) {
                distPrefId = form.getDistPrefId();
                doDelete(distPrefId);
                form.reset();            
                if (BackTracker.doBack(request, response)) return null;
	            op = "view"; //in case no back is available
            }
            if (deleteType.equals("examType")) {
            	sessionContext.setAttribute(SessionAttribute.ExamType, form.getExamType());
            }
        }
        
        // Add new class - redirect from SchedulingSubpartEdit / ClassEdit
        if (MSG.actionAddDistributionPreference().equals(op)) {
            Debug.debug("Adding new Class via redirect ...");
	        form.setDistType(Preference.BLANK_PREF_VALUE);
	        if (request.getAttribute("subjectAreaId")!=null) {
	        	form.setSubjectArea(0, Long.valueOf(request.getAttribute("subjectAreaId").toString()));
	        }
	        if (request.getAttribute("courseOffrId")!=null) {
	            CourseOffering course = new CourseOfferingDAO().get(Long.valueOf(request.getAttribute("courseOffrId").toString()));
	            form.setSubjectArea(0, course.getSubjectArea().getUniqueId());
	            form.setCourseNbr(0, course.getUniqueId());
	        }
	        if (request.getParameter("examId")!=null) {
	            Exam exam = new ExamDAO().get(Long.valueOf(request.getParameter("examId")));
	            form.getExam().add(exam.getUniqueId());
	            form.getSubjectArea().add(exam.firstSubjectArea().getUniqueId());
                form.getCourseNbr().add(exam.firstCourseOffering().getUniqueId());
                form.setExamType(exam.getExamType().getUniqueId());
	        }
            form.getSubjectArea().add(Long.valueOf(-1));
            form.getCourseNbr().add(Long.valueOf(-1));
            form.getExam().add(Long.valueOf(-1));
        }
        
        // Add new class
        if (MSG.actionAddExamToDistribution().equals(op)) {
            Debug.debug("Adding new Class ...");
            form.getSubjectArea().add(Long.valueOf(-1));
            form.getCourseNbr().add(Long.valueOf(-1));
            form.getExam().add(Long.valueOf(-1));
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
        	sessionContext.setAttribute(SessionAttribute.ExamType, form.getExamType());
        	if (MSG.actionExportPdf().equals(op))
        		op="export"; 
        	else if (MSG.actionExportCsv().equals(op))
        		op="export-csv"; 
        	else 
        		op="view";
        }

        // Load Distribution Pref
        if (op!=null && (op.equals("view") || op.equals("export") || op.equals("export-csv")) 
                && distPrefId!=null && distPrefId.trim().length()>0) {
            Debug.debug("Loading dist pref - " + distPrefId);
            form.reset();
            doLoad(distPrefId);
        }
        
        // Reload 
        if (op!=null && op.equals("reload")) {
            // Move Distribution object up one level
            if (reloadCause!=null && reloadCause.equals("moveUp")) {
	            int index = Integer.parseInt(reloadId);
	            Debug.debug("moving up ... " + reloadId);
	            form.swapExams(index, index-1);
            }
            
            // Move Distribution object down one level
            if (reloadCause!=null && reloadCause.equals("moveDown")) {
	            int index = Integer.parseInt(reloadId);
	            Debug.debug("moving down ... " + reloadId);
	            form.swapExams(index, index+1);
            }
        }

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
        	LookupTables.setupExamDistribTypes(request, sessionContext, dist);
        } else {
        	request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, new Vector(0));
        	form.setDescription("");
        	LookupTables.setupExamDistribTypes(request, sessionContext, null);
        }	    
        
        if ("export".equals(op) && (form.getDistPrefId()==null || form.getDistPrefId().length()==0)) {
        	OutputStream out = ExportUtils.getPdfOutputStream(response, "distpref");
            new ExamDistributionPrefsTableBuilder().getDistPrefsTableAsPdf(out, request, sessionContext, (Constants.ALL_OPTION_VALUE.equals(form.getFilterSubjectAreaId()) || form.getFilterSubjectAreaId().isEmpty()?null:Long.valueOf(form.getFilterSubjectAreaId())), form.getFilterCourseNbr(), form.getExamType());
            out.flush();out.close();
            return null;
        }

        if ("export-csv".equals(op) && (form.getDistPrefId()==null || form.getDistPrefId().length()==0)) {
        	PrintWriter out = ExportUtils.getCsvWriter(response, "distpref");
            new ExamDistributionPrefsTableBuilder().getDistPrefsTableAsCsv(out, request, sessionContext, (Constants.ALL_OPTION_VALUE.equals(form.getFilterSubjectAreaId()) || form.getFilterSubjectAreaId().isEmpty()?null:Long.valueOf(form.getFilterSubjectAreaId())), form.getFilterCourseNbr(), form.getExamType());
            out.flush();out.close();
            return null;
        }
        
        request.setAttribute(DistributionPrefsForm.LIST_SIZE_ATTR, ""+(form.getSubjectArea().size()-1));
        
        if (form.getExamType() == null) {
            if (sessionContext.getAttribute(SessionAttribute.ExamType) != null)
            	form.setExamType((Long)sessionContext.getAttribute(SessionAttribute.ExamType));
            else {
            	TreeSet<ExamType> types = ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId());
            	if (!types.isEmpty())
            		form.setExamType(types.first().getUniqueId());
            }
        }
        
        form.setFilterSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser(), false));
        
        List<IdValue> subjects = new ArrayList<IdValue>();
        subjects.add(new IdValue(null, CONST.select()));
        if (sessionContext.hasPermission(Right.DepartmentIndependent)) {
        	subjects.add(new IdValue(-1l, CONST.all()));
        }
        for (SubjectArea sa: form.getFilterSubjectAreas())
        	subjects.add(new IdValue(sa.getUniqueId(), sa.getSubjectAreaAbbreviation()));
        getForm().setSubjectAreas(subjects);
        if (form.getFilterSubjectAreas().size()==1) {
            SubjectArea firstSubjectArea = (SubjectArea)form.getFilterSubjectAreas().iterator().next();
            form.setFilterSubjectAreaId(firstSubjectArea.getUniqueId().toString());
        }
        
        if ("view".equals(op) && (form.getDistPrefId()==null || form.getDistPrefId().length()==0)) {
        	String subject = (String)sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
        	if (subject != null && subject.indexOf(',') >= 0) subject = subject.substring(0, subject.indexOf(','));
        	form.setFilterSubjectAreaId(subject);
            form.setFilterCourseNbr((String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber));

            ExamDistributionPrefsTableBuilder tbl = new ExamDistributionPrefsTableBuilder();
        	if (form.getFilterSubjectAreaId()==null) {
        	    if (sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent))
        	        form.setFilterSubjectAreaId(Constants.BLANK_OPTION_VALUE);
        	    else
        	        form.setFilterSubjectAreaId(Constants.ALL_OPTION_VALUE);        	        
        	}
        	
        	if (form.getFilterSubjectAreaId()!=null && form.getFilterSubjectAreaId().length()>0 && !"null".equals(form.getFilterSubjectAreaId())) {
        	    String html = tbl.getDistPrefsTable(request, sessionContext, (Constants.ALL_OPTION_VALUE.equals(form.getFilterSubjectAreaId())?null:Long.valueOf(form.getFilterSubjectAreaId())), form.getFilterCourseNbr(), form.getExamType());
        	    if (html!=null)
        	        request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);
        	} else {
        	    request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, "");
        	}
            BackTracker.markForBack(
            		request,
            		"examDistributionPrefs.action",
            		"Exam Distribution Prefs",
            		true, true);
            return "list";
        }
        
        return (form.getDistPrefId()==null || form.getDistPrefId().length()==0?"add":"edit");
    }

    /**
     * Loads the form with the data for the distribution pref selected
     */
    private void doLoad(String distPrefId ) {
 
    	sessionContext.checkPermission(distPrefId, "DistributionPref", Right.ExaminationDistributionPreferenceDetail);
    	
        // Get distribution pref info
        DistributionPref dp = new DistributionPrefDAO().get(Long.valueOf(distPrefId));
        form.setDistType(dp.getDistributionType().getUniqueId().toString());
        form.setDescription(dp.getDistributionType().getDescr());
        form.setPrefLevel(dp.getPrefLevel().getPrefId().toString());
        form.setDistPrefId(distPrefId);
        
        for (Iterator i=new TreeSet(dp.getDistributionObjects()).iterator();i.hasNext();) {
            DistributionObject distObj = (DistributionObject)i.next();
            Exam exam = (Exam)distObj.getPrefGroup();
            form.getSubjectArea().add(exam.firstSubjectArea().getUniqueId());
            form.getCourseNbr().add(exam.firstCourseOffering().getUniqueId());
            form.getExam().add(exam.getUniqueId());
        }
        
    }
    
    /**
     * Add new distribution pref
     */
    private void doAddOrUpdate(ExamDistributionPrefsForm form) throws Exception {

        String distPrefId = form.getDistPrefId();
        
        if (distPrefId != null && !distPrefId.isEmpty()) {
        	sessionContext.checkPermission(distPrefId, "DistributionPref", Right.ExaminationDistributionPreferenceEdit);
        } else {
        	sessionContext.checkPermission(Right.ExaminationDistributionPreferenceAdd);
        }
        
        // Create distribution preference
        DistributionPref dp = null;
        DistributionPrefDAO dpDao = new DistributionPrefDAO();
        Transaction tx = null;
        org.hibernate.Session hibSession = dpDao.getSession();
        HashSet relatedExams = new HashSet();
        
        try {
        	tx = hibSession.beginTransaction();
        	
        	if (distPrefId != null && !distPrefId.isEmpty()) {
    			dp = dpDao.get(Long.valueOf(distPrefId), hibSession);
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
            } else dp = new DistributionPref();
            
            dp.setDistributionType(new DistributionTypeDAO().get( Long.valueOf(form.getDistType()), hibSession));
            dp.setGrouping(-1);
        	dp.setPrefLevel(PreferenceLevel.getPreferenceLevel( Integer.parseInt(form.getPrefLevel()) ));
        
        	dp.setOwner(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()));
        
        	HashSet addedExams = new HashSet();
        	int idx = 0;
        	for (Iterator i=form.getExam().iterator();i.hasNext();) {
        		Object o = i.next();
        	    Long examId = (o instanceof Long ? (Long) o : Long.valueOf(o.toString()));
        	    if (examId<0) continue;
        	    
        	    Exam exam = new ExamDAO().get(examId, hibSession);
        	    if (exam==null) continue;
        	    if (!addedExams.add(exam)) continue;
        	    relatedExams.add(exam);
        	    
                DistributionObject dObj = new DistributionObject();                 
                dObj.setPrefGroup(exam);
                dObj.setDistributionPref(dp);
                dObj.setSequenceNumber(Integer.valueOf(++idx));
                exam.getDistributionObjects().add(dObj);
                dp.addTodistributionObjects(dObj);
        	}
        
     	     // Save
    	    hibSession.saveOrUpdate(dp);
            
            for (Iterator i=relatedExams.iterator();i.hasNext();) {
                Exam exam = (Exam)i.next();
                ChangeLog.addChange(
                        hibSession, 
                        sessionContext, 
                        exam, 
                        ChangeLog.Source.DIST_PREF_EDIT,
                        (distPrefId!=null && distPrefId.trim().length()>0?ChangeLog.Operation.UPDATE:ChangeLog.Operation.CREATE),
                        exam.firstSubjectArea(), 
                        exam.firstDepartment());
            }
            
	       	tx.commit();
	       	hibSession.flush();
    	    hibSession.refresh(dp.getOwner());
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
        Transaction tx = null;
        
        sessionContext.checkPermission(distPrefId, "DistributionPref", Right.ExaminationDistributionPreferenceDelete);

        try {
            
	        DistributionPrefDAO dpDao = new DistributionPrefDAO();
	        org.hibernate.Session hibSession = dpDao.getSession();
	        tx = hibSession.getTransaction();
	        if (tx==null || !tx.isActive())
	            tx = hibSession.beginTransaction();
	        
            HashSet relatedExams = new HashSet();
	        DistributionPref dp = dpDao.get(Long.valueOf(distPrefId));
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
                        sessionContext, 
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
