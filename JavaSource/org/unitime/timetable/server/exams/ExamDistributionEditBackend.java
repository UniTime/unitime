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
package org.unitime.timetable.server.exams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ConstantsMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionEditRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionEditRequest.Operation;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionEditResponse;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionObjectInterface;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionsLookupCourses;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionsLookupExams;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging.Level;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.JavascriptFunctions;
import org.unitime.timetable.webutil.BackTracker.BackItem;

@GwtRpcImplements(ExamDistributionEditRequest.class)
public class ExamDistributionEditBackend implements GwtRpcImplementation<ExamDistributionEditRequest, ExamDistributionEditResponse>{
	protected final static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	protected final static ConstantsMessages CMSG = Localization.create(ConstantsMessages.class);
	
	@Override
	public ExamDistributionEditResponse execute(ExamDistributionEditRequest request, SessionContext context) {
		if (request.getPreferenceId() == null)
			context.checkPermission(Right.ExaminationDistributionPreferenceAdd);
		else
			context.checkPermission(request.getPreferenceId(), "DistributionPref", Right.ExaminationDistributionPreferenceEdit);
		
		if (request.getOperation() == Operation.DELETE) {
			deleteDistPref(request.getPreferenceId(), context);
			return null;
		} else if (request.getOperation() == Operation.SAVE) {
			ExamDistributionEditResponse response = request.getData();
			updateDistPref(response, context);
			BackItem back = BackTracker.getBackItem(context, 1);
	    	if (back != null) {
	    		response.setBackTitle(back.getTitle());
	    		response.setBackUrl(back.getUrl() + (request.getPreferenceId() == null ? "" :
	    				(back.getUrl().indexOf('?') >= 0 ? "&" : "?") +
	    				"backId=" + request.getPreferenceId() + "&backType=DistributionPref"));
	    	}
			return response;
		}
		
		ExamDistributionEditResponse response = new ExamDistributionEditResponse();
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false))
			response.addPrefLevel(pref.getUniqueId(), pref.getPrefName(), PreferenceLevel.prolog2char(pref.getPrefProlog()));
		for (DistributionType dt: DistributionType.findAll(false, true, true))
			response.addDistType(dt.getUniqueId(), dt.getLabel(), dt.getDescr(), dt.getAllowedPref());

		for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser(), true))
			response.addSubject(subject.getUniqueId(), subject.getSubjectAreaAbbreviation(), subject.getLabel());

		DistributionPref dp = (request.getPreferenceId() == null ? null : DistributionPrefDAO.getInstance().get(request.getPreferenceId()));
		TreeSet<SubjectArea> userSubjects = SubjectArea.getUserSubjectAreas(context.getUser());
		if (dp != null) {
			response.setPreferenceId(dp.getUniqueId());
			response.setDistTypeId(dp.getDistributionType().getUniqueId());
			if (response.getDistType(dp.getDistributionType().getUniqueId()) == null)
				response.addDistType(dp.getDistributionType().getUniqueId(), dp.getDistributionType().getLabel(), dp.getDistributionType().getDescr(), dp.getDistributionType().getAllowedPref());
			response.setPrefLevelId(dp.getPrefLevel().getUniqueId());
			response.setCanDelete(context.hasPermission(dp, Right.ExaminationDistributionPreferenceDelete));
			for (DistributionObject distObj: dp.getOrderedSetOfDistributionObjects()) {
				if (distObj.getPrefGroup() instanceof Exam) {
					Exam ex = (Exam) distObj.getPrefGroup();
					if (!response.hasExamTypes()) {
						response.addExamType(ex.getExamType().getUniqueId(), ex.getExamType().getLabel(), ex.getExamType().getReference());
						response.setExamTypeId(ex.getExamType().getUniqueId());
					}
					CourseOffering co = null;
					for (ExamOwner owner: new TreeSet<ExamOwner>(ex.getOwners())) {
						if (co == null) co = owner.getCourse();
						if (userSubjects.contains(co.getSubjectArea())) {
							co = owner.getCourse(); break;
						}
					}
					if (co == null) continue;
					ExamDistributionObjectInterface doi = new ExamDistributionObjectInterface();
					doi.setSubjectId(co.getSubjectArea().getUniqueId());
					doi.setSubject(co.getSubjectAreaAbbv());
					doi.setCourseId(co.getUniqueId());
					doi.setCourse(co.getCourseNumberWithTitle());
					doi.setExamId(ex.getUniqueId());
					doi.setExam(ex.getLabel());
					response.addDistributionObject(doi);
				}
			}
		} else {
			response.setCanDelete(false);
		}
		
		if (request.getExamId() != null) {
			Exam ex = ExamDAO.getInstance().get(request.getExamId());
			if (ex != null) {
				response.addExamType(ex.getExamType().getUniqueId(), ex.getExamType().getLabel(), ex.getExamType().getReference());
				response.setExamTypeId(ex.getExamType().getUniqueId());
				CourseOffering co = null;
				for (ExamOwner owner: new TreeSet<ExamOwner>(ex.getOwners())) {
					if (co == null) co = owner.getCourse();
					if (userSubjects.contains(co.getSubjectArea())) {
						co = owner.getCourse(); break;
					}
				}
				if (co != null) {
					ExamDistributionObjectInterface doi = new ExamDistributionObjectInterface();
					doi.setSubjectId(co.getSubjectArea().getUniqueId());
					doi.setSubject(co.getSubjectAreaAbbv());
					doi.setCourseId(co.getUniqueId());
					doi.setCourse(co.getCourseNumberWithTitle());
					doi.setExamId(ex.getUniqueId());
					doi.setExam(ex.getLabel());
					response.addDistributionObject(doi);
				}
			}
		}
		
		if (!response.hasExamTypes()) {
	        for (ExamType type: ExamType.findAllUsedApplicable(context.getUser(), DepartmentStatusType.Status.ExamEdit))
	        	response.addExamType(type.getUniqueId(), type.getLabel(), type.getReference());
	        response.setExamTypeId(request.getTypeId());
		}
		if (response.getExamTypeId() == null) {
			Object et = context.getAttribute(SessionAttribute.ExamType);
			if (et != null)
				response.setExamTypeId(Long.valueOf(et.toString()));
		}
		
		BackItem back = BackTracker.getBackItem(context, 1);
    	if (back != null) {
    		response.setBackTitle(back.getTitle());
    		response.setBackUrl(back.getUrl() + (request.getPreferenceId() == null ? "" :
    				(back.getUrl().indexOf('?') >= 0 ? "&" : "?") +
    				"backId=" + request.getPreferenceId() + "&backType=DistributionPref"));
    	}
    	response.setConfirms(JavascriptFunctions.isJsConfirm(context));

		return response;
	}
	
	protected void deleteDistPref(Long distPrefId, SessionContext context) {
        Transaction tx = null;
        try {
	        DistributionPrefDAO dpDao = DistributionPrefDAO.getInstance();
	        org.hibernate.Session hibSession = dpDao.getSession();
	        tx = hibSession.getTransaction();
	        if (tx==null || !tx.isActive())
	            tx = hibSession.beginTransaction();
	        
            HashSet<Exam> relatedExams = new HashSet<Exam>();
	        DistributionPref dp = dpDao.get(Long.valueOf(distPrefId));
	        
	        context.checkPermission(dp, Right.ExaminationDistributionPreferenceDelete);
	        
	        PreferenceGroup owner = dp.getOwner();
	        owner.getPreferences().remove(dp);
			for (Iterator<DistributionObject> i=dp.getDistributionObjects().iterator();i.hasNext();) {
				DistributionObject dObj = i.next();
				PreferenceGroup pg = dObj.getPrefGroup();
				if (pg instanceof Exam)
					relatedExams.add((Exam)pg);
				pg.getDistributionObjects().remove(dObj);
				hibSession.merge(pg);
			}
	        
	        hibSession.remove(dp);
	        hibSession.merge(owner);
	        
            for (Iterator<Exam> i=relatedExams.iterator();i.hasNext();) {
                Exam ex = i.next();
                ChangeLog.addChange(
                        hibSession, 
                        context, 
                        ex, 
                        ChangeLog.Source.DIST_PREF_EDIT,
                        ChangeLog.Operation.DELETE,
                        ex.firstSubjectArea(),
                        ex.firstDepartment());
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
            throw new GwtRpcException(e.getMessage(), e);
        }
    }
	
	protected void updateDistPref(ExamDistributionEditResponse data, SessionContext context) {
		// Create distribution preference
        DistributionPref dp = null;
        DistributionPrefDAO dpDao = DistributionPrefDAO.getInstance();
        Transaction tx = null;
        org.hibernate.Session hibSession = dpDao.getSession();
        HashSet<Exam> relatedExams = new HashSet<Exam>();
        
        try {
        	tx = hibSession.beginTransaction();
        	
        	if (data.getPreferenceId() != null) {
        		dp = DistributionPrefDAO.getInstance().get(data.getPreferenceId(), hibSession);
        		context.checkPermission(dp, Right.ExaminationDistributionPreferenceEdit);
        		Set<DistributionObject> s = dp.getDistributionObjects();
        		for (Iterator<DistributionObject> i=s.iterator();i.hasNext();) {
        			DistributionObject dObj = i.next();
    				PreferenceGroup pg = dObj.getPrefGroup();
    				if (pg instanceof Exam)
    					relatedExams.add((Exam)pg);
    				pg.getDistributionObjects().remove(dObj);
    				hibSession.remove(dObj);
    			}
    			s.clear();
    			dp.setDistributionObjects(s);
        	} else {
            	dp = new DistributionPref();
            	context.checkPermission(Right.ExaminationDistributionPreferenceAdd);
            }
            
            dp.setDistributionType(DistributionTypeDAO.getInstance().get(data.getDistTypeId(), hibSession));
            dp.setGrouping(-1);
        	dp.setPrefLevel(PreferenceLevelDAO.getInstance().get(data.getPrefLevelId(), hibSession));
        	
        	Session owningSession = null;
        	List<DistributionObject> distributionObjects = new ArrayList<DistributionObject>();
	        // Create distribution objects
        	for (int i = 0; i<data.getDistributionObjects().size(); i++) {
        		ExamDistributionObjectInterface doi = data.getDistributionObjects().get(i);
            	DistributionObject dObj = new DistributionObject();	                

    	    	Exam exam = ExamDAO.getInstance().get(doi.getExamId(), hibSession);
    	    	if (owningSession == null)
    	    		owningSession = exam.getSession();
            	
                relatedExams.add(exam);
    	    	dObj.setPrefGroup(exam);
            
            	dObj.setSequenceNumber(Integer.valueOf(i+1));
            	distributionObjects.add(dObj);
        	}
        	if (owningSession == null)
        		owningSession = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
        
     	    dp.setOwner(owningSession);
        	if (dp.getUniqueId() == null) hibSession.persist(dp);
        	
        	for (DistributionObject dObj: distributionObjects) {
            	dObj.setDistributionPref(dp);
            	dp.addToDistributionObjects(dObj);
            	hibSession.persist(dObj);
            	dObj.getPrefGroup().addToDistributionObjects(dObj);
        	}
        
	        // Save
     	    hibSession.merge(dp);
            
            for (Exam exam: relatedExams) {
                ChangeLog.addChange(
                        hibSession, 
                        context, 
                        exam,
                        ChangeLog.Source.DIST_PREF_EDIT,
                        (data.getPreferenceId() != null ? ChangeLog.Operation.UPDATE : ChangeLog.Operation.CREATE),
                        exam.firstSubjectArea(), 
                        exam.firstDepartment());
            }
            
	       	tx.commit();
	       	hibSession.flush();
    	    hibSession.refresh(dp.getOwner());
    	    data.setPreferenceId(dp.getUniqueId());
        } catch (Exception e) {
        	if (tx!=null) tx.rollback();
        	hibSession.clear();
        	throw new GwtRpcException(e.getMessage(), e);
        } 
	}
	
	@GwtRpcImplements(ExamDistributionsLookupCourses.class)
	@GwtRpcLogging(Level.DISABLED)
	public static class LookupCoursesBackend implements GwtRpcImplementation<ExamDistributionsLookupCourses, GwtRpcResponseList<IdLabel>> {
		@Override
		public GwtRpcResponseList<IdLabel> execute(ExamDistributionsLookupCourses request, SessionContext context) {
			GwtRpcResponseList<IdLabel> ret = new GwtRpcResponseList<IdLabel>();
	        List<Object[]> courseNumbers = CourseOfferingDAO.getInstance().
	                getSession().
	                createQuery("select co.uniqueId, co.courseNbr, co.title from CourseOffering co "+
	                        "where co.subjectArea.uniqueId = :subjectAreaId "+
	                        "and co.instructionalOffering.notOffered = false " +
	                        "order by co.courseNbr ", Object[].class).
	                setFetchSize(200).
	                setCacheable(true).
	                setParameter("subjectAreaId", request.getSubjectId()).
	                list();
	            for (Object[] o : courseNumbers)
	            	ret.add(new IdLabel((Long)o[0], o[1].toString() + (o[2] == null || o[2].toString().isEmpty() ? "" : " - " + o[2]), null));
			return ret;
		}
	}
	
	@GwtRpcImplements(ExamDistributionsLookupExams.class)
	@GwtRpcLogging(Level.DISABLED)
	public static class LookupSubpartsBackend implements GwtRpcImplementation<ExamDistributionsLookupExams, GwtRpcResponseList<IdLabel>> {
		@Override
		public GwtRpcResponseList<IdLabel> execute(ExamDistributionsLookupExams request, SessionContext context) {
			GwtRpcResponseList<IdLabel> ret = new GwtRpcResponseList<IdLabel>();
			TreeSet<Exam> exams = new TreeSet<Exam>(Exam.findExamsOfCourseOffering(request.getCourseId(), request.getExamTypeId()));
	        for (Exam ex: exams)
	        	ret.add(new IdLabel(ex.getUniqueId(), ex.getLabel(), null));
	        return ret;
		}
	}
}
