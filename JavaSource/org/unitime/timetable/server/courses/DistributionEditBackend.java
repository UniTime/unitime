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
package org.unitime.timetable.server.courses;

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
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionEditRequest;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionEditRequest.Operation;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionObjectInterface;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionsLookupClasses;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionsLookupCourses;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionsLookupSubparts;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging.Level;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.spring.SpringApplicationContextHolder;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.JavascriptFunctions;
import org.unitime.timetable.webutil.BackTracker.BackItem;

@GwtRpcImplements(DistributionEditRequest.class)
public class DistributionEditBackend implements GwtRpcImplementation<DistributionEditRequest, DistributionEditResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static ConstantsMessages CMSG = Localization.create(ConstantsMessages.class);
	
	@Override
	public DistributionEditResponse execute(DistributionEditRequest request, SessionContext context) {
		if (request.getPreferenceId() == null)
			context.checkPermission(Right.DistributionPreferenceAdd);
		else
			context.checkPermission(request.getPreferenceId(), "DistributionPref", Right.DistributionPreferenceEdit);
		
		if (request.getOperation() == Operation.DELETE) {
			deleteDistPref(request.getPreferenceId(), context);
			return null;
		} else if (request.getOperation() == Operation.SAVE) {
			DistributionEditResponse response = request.getData();
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
		
		DistributionEditResponse response = new DistributionEditResponse();
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false))
			response.addPrefLevel(pref.getUniqueId(), pref.getPrefName(), PreferenceLevel.prolog2char(pref.getPrefProlog()));
		for (DistributionType dt: DistributionType.findAll(false, false, true))
			response.addDistType(dt.getUniqueId(), dt.getLabel(), dt.getDescr(), dt.getAllowedPref());
		for (DistributionPref.Structure str: DistributionPref.Structure.values())
			response.addStructure(str.ordinal(), str.getName(), str.getDescription());

		for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser(), true))
			response.addSubject(subject.getUniqueId(), subject.getSubjectAreaAbbreviation(), subject.getLabel());

		DistributionPref dp = (request.getPreferenceId() == null ? null : DistributionPrefDAO.getInstance().get(request.getPreferenceId()));
		if (dp != null) {
			response.setPreferenceId(dp.getUniqueId());
			response.setDistTypeId(dp.getDistributionType().getUniqueId());
			if (response.getDistType(dp.getDistributionType().getUniqueId()) == null)
				response.addDistType(dp.getDistributionType().getUniqueId(), dp.getDistributionType().getLabel(), dp.getDistributionType().getDescr(), dp.getDistributionType().getAllowedPref());
			response.setPrefLevelId(dp.getPrefLevel().getUniqueId());
			response.setStructureId(dp.getStructure() == null ? null : Long.valueOf(dp.getStructure().ordinal()));
			response.setCanDelete(context.hasPermission(dp, Right.DistributionPreferenceDelete));
			for (DistributionObject distObj: dp.getOrderedSetOfDistributionObjects()) {
				DistributionObjectInterface doi = new DistributionObjectInterface();
				CourseOffering co;
				if (distObj.getPrefGroup() instanceof SchedulingSubpart) {
					SchedulingSubpart ss = (SchedulingSubpart) distObj.getPrefGroup();
					co = ss.getControllingCourseOffering();
					doi.setSubpartId(ss.getUniqueId());
					doi.setSubpart(getSubpartLabel(ss));
					doi.setClassId(-1l);
					doi.setClazz(MSG.dropDistrPrefAll());
				} else if (distObj.getPrefGroup() instanceof Class_) {
					Class_ clazz = (Class_) distObj.getPrefGroup();
					SchedulingSubpart ss = clazz.getSchedulingSubpart();
					co = ss.getControllingCourseOffering();
					doi.setSubpartId(ss.getUniqueId());
					doi.setSubpart(getSubpartLabel(ss));
					doi.setClassId(clazz.getUniqueId());
					doi.setClazz(getClassLabel(clazz));
				} else {
					continue;
				}
				doi.setSubjectId(co.getSubjectArea().getUniqueId());
				doi.setSubject(co.getSubjectAreaAbbv());
				doi.setCourseId(co.getUniqueId());
				doi.setCourse(co.getCourseNumberWithTitle());
				response.addDistributionObject(doi);
			}
		} else {
			response.setCanDelete(false);
		}
		
		if (request.getClassId() != null) {
			Class_ clazz = Class_DAO.getInstance().get(request.getClassId());
			if (clazz != null) {
				DistributionObjectInterface doi = new DistributionObjectInterface();
				SchedulingSubpart ss = clazz.getSchedulingSubpart();
				CourseOffering co = ss.getControllingCourseOffering();
				doi.setSubpartId(ss.getUniqueId());
				doi.setSubpart(getSubpartLabel(ss));
				doi.setClassId(clazz.getUniqueId());
				doi.setClazz(getClassLabel(clazz));
				doi.setSubjectId(co.getSubjectArea().getUniqueId());
				doi.setSubject(co.getSubjectAreaAbbv());
				doi.setCourseId(co.getUniqueId());
				doi.setCourse(co.getCourseNumberWithTitle());
				response.addDistributionObject(doi);
			}
		}
		if (request.getSubpartId() != null) {
			SchedulingSubpart ss = SchedulingSubpartDAO.getInstance().get(request.getSubpartId());
			if (ss != null) {
				DistributionObjectInterface doi = new DistributionObjectInterface();
				CourseOffering co = ss.getControllingCourseOffering();
				doi.setSubpartId(ss.getUniqueId());
				doi.setSubpart(getSubpartLabel(ss));
				doi.setClassId(-1l);
				doi.setClazz(MSG.dropDistrPrefAll());
				doi.setSubjectId(co.getSubjectArea().getUniqueId());
				doi.setSubject(co.getSubjectAreaAbbv());
				doi.setCourseId(co.getUniqueId());
				doi.setCourse(co.getCourseNumberWithTitle());
				response.addDistributionObject(doi);
			}
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
	
	protected static String getSubpartLabel(SchedulingSubpart ss) {
		String subpart = ss.getItypeDesc().trim();
		String suffix = ss.getSchedulingSubpartSuffix();
		if (suffix != null && !suffix.isEmpty())
			subpart += " (" + suffix + ")";
		if (ss.getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().size() > 1)
			subpart += " [" + ss.getInstrOfferingConfig().getName() + "]";
		while (ss.getParentSubpart() != null) {
			subpart = "\u00A0\u00A0" + subpart;
			ss = ss.getParentSubpart();
		}
		return subpart;
	}
	
	protected static String getClassLabel(Class_ c) {
		if (ApplicationProperty.DistributionsShowClassSufix.isTrue()) {
			String extId = c.getClassSuffix(c.getSchedulingSubpart().getControllingCourseOffering());
			return c.getSectionNumberString() + (extId == null || extId.isEmpty() || extId.equalsIgnoreCase(c.getSectionNumberString()) ? "" : " - " + extId);
		} else {
			return c.getSectionNumberString();
		}
	}
	
	protected void deleteDistPref(Long distPrefId, SessionContext context) {
        Transaction tx = null;
        try {
	        DistributionPrefDAO dpDao = DistributionPrefDAO.getInstance();
	        org.hibernate.Session hibSession = dpDao.getSession();
	        tx = hibSession.getTransaction();
	        if (tx==null || !tx.isActive())
	            tx = hibSession.beginTransaction();
	        
            HashSet<InstructionalOffering> relatedInstructionalOfferings = new HashSet<InstructionalOffering>();
	        DistributionPref dp = dpDao.get(Long.valueOf(distPrefId));
	        
	        context.checkPermission(dp, Right.DistributionPreferenceDelete);
	        
	        Department dept = (Department) dp.getOwner();
	        dept.getPreferences().remove(dp);
			for (Iterator<DistributionObject> i=dp.getDistributionObjects().iterator();i.hasNext();) {
				DistributionObject dObj = i.next();
				PreferenceGroup pg = dObj.getPrefGroup();
                relatedInstructionalOfferings.add((pg instanceof Class_ ?((Class_)pg).getSchedulingSubpart():(SchedulingSubpart)pg).getInstrOfferingConfig().getInstructionalOffering());
				pg.getDistributionObjects().remove(dObj);
				hibSession.merge(pg);
			}
	        
	        hibSession.remove(dp);
	        hibSession.merge(dept);
	        
	        Permission<InstructionalOffering> permissionOfferingLockNeeded = getPermission("permissionOfferingLockNeeded");
	        
	        List<Long> changedOfferingIds = new ArrayList<Long>();
            for (Iterator<InstructionalOffering> i=relatedInstructionalOfferings.iterator();i.hasNext();) {
                InstructionalOffering io = i.next();
                ChangeLog.addChange(
                        hibSession, 
                        context, 
                        io, 
                        ChangeLog.Source.DIST_PREF_EDIT,
                        ChangeLog.Operation.DELETE,
                        io.getControllingCourseOffering().getSubjectArea(), 
                        null);
                if (permissionOfferingLockNeeded.check(context.getUser(), io))
                	changedOfferingIds.add(io.getUniqueId());
            }
            if (!changedOfferingIds.isEmpty())
            	StudentSectioningQueue.offeringChanged(hibSession, context.getUser(), context.getUser().getCurrentAcademicSessionId(), changedOfferingIds);

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
	
	protected void updateDistPref(DistributionEditResponse data, SessionContext context) {
		// Create distribution preference
        DistributionPref dp = null;
        Department oldOwner = null;
        DistributionPrefDAO dpDao = DistributionPrefDAO.getInstance();
        Transaction tx = null;
        org.hibernate.Session hibSession = dpDao.getSession();
        HashSet<InstructionalOffering> relatedInstructionalOfferings = new HashSet<InstructionalOffering>();
        
        try {
        	tx = hibSession.beginTransaction();
        	
        	if (data.getPreferenceId() != null) {
        		dp = DistributionPrefDAO.getInstance().get(data.getPreferenceId(), hibSession);
        		Set<DistributionObject> s = dp.getDistributionObjects();
        		for (Iterator<DistributionObject> i=s.iterator();i.hasNext();) {
        			DistributionObject dObj = i.next();
    				PreferenceGroup pg = dObj.getPrefGroup();
                    relatedInstructionalOfferings.add((pg instanceof Class_ ?((Class_)pg).getSchedulingSubpart():(SchedulingSubpart)pg).getInstrOfferingConfig().getInstructionalOffering());
    				pg.getDistributionObjects().remove(dObj);
    				hibSession.remove(dObj);
    			}
    			s.clear();
    			dp.setDistributionObjects(s);
    			oldOwner = (Department)dp.getOwner();
        	} else {
            	dp = new DistributionPref();
            }
            
            dp.setDistributionType(DistributionTypeDAO.getInstance().get(data.getDistTypeId(), hibSession));
            dp.setStructure(DistributionPref.Structure.values()[data.getStructureId().intValue()]);
        	dp.setPrefLevel(PreferenceLevelDAO.getInstance().get(data.getPrefLevelId(), hibSession));
        	
        	Department owningDept = null;
        	List<DistributionObject> distributionObjects = new ArrayList<DistributionObject>();
	        // Create distribution objects
        	for (int i = 0; i<data.getDistributionObjects().size(); i++) {
        		DistributionObjectInterface doi = data.getDistributionObjects().get(i);
            	DistributionObject dObj = new DistributionObject();	                
	            // Subpart
    	        if (doi.getClassId() == null || doi.getClassId() < 0) {
        	    	SchedulingSubpart subpart = SchedulingSubpartDAO.getInstance().get(doi.getSubpartId(), hibSession);
	            	if (owningDept==null) owningDept = subpart.getManagingDept();
    	        	else if (!owningDept.getUniqueId().equals(subpart.getManagingDept().getUniqueId())) {
    	        		if (owningDept.getDistributionPrefPriority().intValue()<subpart.getManagingDept().getDistributionPrefPriority().intValue())
    	        			owningDept = subpart.getManagingDept();
    	        		else if (owningDept.getDistributionPrefPriority().intValue()==subpart.getManagingDept().getDistributionPrefPriority().intValue()) {
    	        			if (!context.getUser().getCurrentAuthority().hasQualifier(owningDept) && context.getUser().getCurrentAuthority().hasQualifier(subpart.getManagingDept()))
    	        				owningDept = subpart.getManagingDept();
    	        		}
    	        	}
            	
            		dObj.setPrefGroup(subpart);
                    relatedInstructionalOfferings.add(subpart.getInstrOfferingConfig().getInstructionalOffering());
            	} else { // Class
        	    	Class_ clazz = new Class_DAO().get(doi.getClassId(), hibSession);
	            	if (owningDept==null) owningDept = clazz.getManagingDept();
    	        	else if (!owningDept.equals(clazz.getManagingDept())) {
    	        		if (owningDept.getDistributionPrefPriority().intValue()<clazz.getManagingDept().getDistributionPrefPriority().intValue())
    	        			owningDept = clazz.getManagingDept();
    	        		else if (owningDept.getDistributionPrefPriority().intValue()==clazz.getManagingDept().getDistributionPrefPriority().intValue()) {
    	        			if (!context.getUser().getCurrentAuthority().hasQualifier(owningDept) && context.getUser().getCurrentAuthority().hasQualifier(clazz.getManagingDept()))
    	        				owningDept = clazz.getManagingDept();
    	        		}
    	        	}
	            	
                    relatedInstructionalOfferings.add(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering());
        	    	dObj.setPrefGroup(clazz);
            	}
            
            	dObj.setSequenceNumber(Integer.valueOf(i+1));
            	distributionObjects.add(dObj);
        	}
        
     	    dp.setOwner(owningDept);
        	if (dp.getUniqueId() == null) hibSession.persist(dp);
        	
        	for (DistributionObject dObj: distributionObjects) {
            	dObj.setDistributionPref(dp);
            	dp.addToDistributionObjects(dObj);
            	hibSession.persist(dObj);
            	dObj.getPrefGroup().addToDistributionObjects(dObj);
        	}
     	    
     	    context.checkPermission(dp, Right.DistributionPreferenceEdit);
        
	        // Save
     	    hibSession.merge(dp);
    	    
    	    Permission<InstructionalOffering> permissionOfferingLockNeeded = getPermission("permissionOfferingLockNeeded");
            
    	    List<Long> changedOfferingIds = new ArrayList<Long>();
            for (Iterator i=relatedInstructionalOfferings.iterator();i.hasNext();) {
                InstructionalOffering io = (InstructionalOffering)i.next();
                ChangeLog.addChange(
                        hibSession, 
                        context, 
                        io, 
                        ChangeLog.Source.DIST_PREF_EDIT,
                        (data.getPreferenceId() != null ? ChangeLog.Operation.UPDATE : ChangeLog.Operation.CREATE),
                        io.getControllingCourseOffering().getSubjectArea(), 
                        null);
                if (permissionOfferingLockNeeded.check(context.getUser(), io))
                	changedOfferingIds.add(io.getUniqueId());
            }
            if (!changedOfferingIds.isEmpty())
            	StudentSectioningQueue.offeringChanged(hibSession, context.getUser(), context.getUser().getCurrentAcademicSessionId(), changedOfferingIds);
            
	       	tx.commit();
	       	hibSession.flush();
    	    hibSession.refresh(dp.getOwner());
    	    if (oldOwner!=null && !oldOwner.equals(dp.getOwner()))
    	    	hibSession.refresh(oldOwner);
    	    data.setPreferenceId(dp.getUniqueId());
        } catch (Exception e) {
        	if (tx!=null) tx.rollback();
        	hibSession.clear();
        	throw e;
        } 
	}
	
	@GwtRpcImplements(DistributionsLookupCourses.class)
	@GwtRpcLogging(Level.DISABLED)
	public static class LookupCoursesBackend implements GwtRpcImplementation<DistributionsLookupCourses, GwtRpcResponseList<IdLabel>> {
		@Override
		public GwtRpcResponseList<IdLabel> execute(DistributionsLookupCourses request, SessionContext context) {
			GwtRpcResponseList<IdLabel> ret = new GwtRpcResponseList<IdLabel>();
	        List<Object[]> courseNumbers = CourseOfferingDAO.getInstance().
	                getSession().
	                createQuery("select co.uniqueId, co.courseNbr, co.title from CourseOffering co "+
	                        "where co.subjectArea.uniqueId = :subjectAreaId "+
	                        "and co.instructionalOffering.notOffered = false and co.isControl = true " +
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
	
	@GwtRpcImplements(DistributionsLookupSubparts.class)
	@GwtRpcLogging(Level.DISABLED)
	public static class LookupSubpartsBackend implements GwtRpcImplementation<DistributionsLookupSubparts, GwtRpcResponseList<IdLabel>> {
		@Override
		public GwtRpcResponseList<IdLabel> execute(DistributionsLookupSubparts request, SessionContext context) {
			GwtRpcResponseList<IdLabel> ret = new GwtRpcResponseList<IdLabel>();
			TreeSet<SchedulingSubpart> subparts = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator(null));
	        subparts.addAll(SchedulingSubpartDAO.getInstance().
	            getSession().
	            createQuery("select distinct s from " +
	                    "SchedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co "+
	                    "where co.uniqueId = :courseOfferingId", SchedulingSubpart.class).
	            setFetchSize(200).
	            setCacheable(true).
	            setParameter("courseOfferingId", request.getCourseId()).
	            list());
	        for (SchedulingSubpart s: subparts)
	        	ret.add(new IdLabel(s.getUniqueId(), getSubpartLabel(s), null));
	        return ret;
		}
	}
	
	@GwtRpcImplements(DistributionsLookupClasses.class)
	@GwtRpcLogging(Level.DISABLED)
	public static class LookupClassesBackend implements GwtRpcImplementation<DistributionsLookupClasses, GwtRpcResponseList<IdLabel>> {
		@Override
		public GwtRpcResponseList<IdLabel> execute(DistributionsLookupClasses request, SessionContext context) {
			GwtRpcResponseList<IdLabel> ret = new GwtRpcResponseList<IdLabel>();
			TreeSet<Class_> classes = new TreeSet<Class_>(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
	        classes.addAll(new Class_DAO().
	            getSession().
	            createQuery("select distinct c from Class_ c "+
	                    "where c.schedulingSubpart.uniqueId=:schedulingSubpartId", Class_.class).
	            setFetchSize(200).
	            setCacheable(true).
	            setParameter("schedulingSubpartId", request.getSubpartId()).
	            list());
	        ret.add(new IdLabel(-1l, MSG.dropDistrPrefAll(), null));
	        for (Class_ c: classes)
	        	ret.add(new IdLabel(c.getUniqueId(), getClassLabel(c), null));
	        return ret;
		}
	}
	
	protected <X> Permission<X> getPermission(String name) {
		return (Permission<X>)SpringApplicationContextHolder.getBean(name);
	}
}
